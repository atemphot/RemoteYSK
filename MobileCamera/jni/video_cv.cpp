#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <assert.h>
#include <unistd.h>

#ifdef ANDROID_NDK
#include <jni.h>
#include "jni_func.h"
#include "jni_if.h"
#endif



typedef	unsigned char	byte;
typedef	unsigned short	word;
typedef	unsigned int	dword;

typedef	unsigned char	BYTE;
typedef	unsigned short	WORD;
typedef	unsigned int	DWORD;

typedef	signed char BOOL;
#define TRUE	1
#define FALSE	0

#define NO_ERROR	0


#include "colorspace.h"


#if defined(ANDROID_NDK)
#include <android/log.h>
#else
#define __android_log_print(level, tag, format, ...) printf(format, ## __VA_ARGS__)
#endif


///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
#ifndef NO_OPENCV

#include <cv.h>
#include <cxcore.h>
#include <highgui.h>
#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>


#define PixelFormat_RGB_565			4	//4
#define PixelFormat_RGBA_8888		1	//1
#define PixelFormat_YCbCr_422_SP	16  //16,NV16
#define PixelFormat_YCbCr_420_SP	17	//17,NV21
#define PixelFormat_YCbCr_422_I		20	//20,YUY2

#define PixelFormat_YV12			842094169  //ImageFormat.YV12
#define PixelFormat_32BGRA			0x42475241


/* 手势指令 ###################################################### */

#define GC_MAX_DETECT_IMAGE_NUM		1000
#define GC_MAX_DETECT_REGION_NUM	20
#define GC_REGION_X_Y_DIFF			46
#define GC_REGION_WIDTH_DIFF		36
#define GC_REGION_HEIGHT_DIFF		150


typedef enum _tag_gc_result_type {
	GC_RESULT_TYPE_INVALID = 0,
	GC_RESULT_TYPE_ARM = 1,
	GC_RESULT_TYPE_DISARM = 2,
} GC_RESULT_TYPE;

typedef enum _tag_gc_obj_type {
	GC_OBJ_TYPE_NONE = 0,
	GC_OBJ_TYPE_FIST = 1,
	GC_OBJ_TYPE_PALM = 2,
} GC_OBJ_TYPE;

typedef struct _tag_gc_detect_region {
	CvRect rect;
	GC_OBJ_TYPE objs[GC_MAX_DETECT_IMAGE_NUM];
} GC_DETECT_REGION;


static BOOL gc_bInit = FALSE;
static CvHaarClassifierCascade* cascade_fist;
static CvHaarClassifierCascade* cascade_palm;

static int gcDetectImageNum;
static GC_DETECT_REGION gcDetectRegions[GC_MAX_DETECT_REGION_NUM];
static int gcDetectRegionNum;


// 判断两个矩形是否就是同一个区域
static BOOL is_same_region(CvRect *rect1, CvRect *rect2)
{
	//左下角的点
	int x1 = rect1->x;
	int y1 = rect1->y + rect1->height;
	int x2 = rect2->x;
	int y2 = rect2->y + rect2->height;

	if (abs(x1 - x2) <= GC_REGION_X_Y_DIFF && abs(y1 - y2) <= GC_REGION_X_Y_DIFF)
	{
		if (abs(rect1->width - rect2->width) <= GC_REGION_WIDTH_DIFF)
		{
			if (abs(rect1->height - rect2->height) <= GC_REGION_HEIGHT_DIFF)
			{
				return TRUE;
			}
		}
	}
	return FALSE;
}

// 判断某一个区域中是否包含手势指令
static GC_RESULT_TYPE region_contains_gc(GC_DETECT_REGION *r)
{
	GC_OBJ_TYPE prev = GC_OBJ_TYPE_NONE;
	for (int j = 0; j < gcDetectImageNum + 1; j++)
	{
		if (r->objs[j] == GC_OBJ_TYPE_FIST)
		{
			if (GC_OBJ_TYPE_NONE == prev) {
				prev = GC_OBJ_TYPE_FIST;
			}
			else if (GC_OBJ_TYPE_PALM == prev) {
				return GC_RESULT_TYPE_DISARM;
			}
			else if (GC_OBJ_TYPE_FIST == prev) {
				continue;
			}
		}
		else if (r->objs[j] == GC_OBJ_TYPE_PALM)
		{
			if (GC_OBJ_TYPE_NONE == prev) {
				prev = GC_OBJ_TYPE_PALM;
			}
			else if (GC_OBJ_TYPE_FIST == prev) {
				return GC_RESULT_TYPE_ARM;
			}
			else if (GC_OBJ_TYPE_PALM == prev) {
				continue;
			}
		}
		else {
			continue;
		}
	}
	return GC_RESULT_TYPE_INVALID;
}

static int find_the_region(CvRect *_rect)
{
	for(int i = 0; i < gcDetectRegionNum; i++)
	{
		if (TRUE == is_same_region(_rect, &(gcDetectRegions[i].rect)))
		{
			return i;
		}
	}
	return -1;
}

static int add_a_region(CvRect *_rect)
{
	int ret;
	if (gcDetectRegionNum < GC_MAX_DETECT_REGION_NUM)
	{
		ret = gcDetectRegionNum;
		gcDetectRegions[ret].rect = *_rect;
		gcDetectRegionNum += 1;
		return ret;
	}
	return -1;
}

static GC_RESULT_TYPE gc_on_detect_obj(GC_OBJ_TYPE obj_type, CvSeq* faces, int scale)
{
	int i;
	
	__android_log_print(ANDROID_LOG_INFO, "gc_detect", "objects=%d...\n", faces->total);
	
	if (faces->total <= 0) {
		return GC_RESULT_TYPE_INVALID;
	}
	
	if_gc_detect_obj(obj_type);
	
	for(i = 0; i < faces->total; i++)
    {
        CvRect obj_rect = *(CvRect*)cvGetSeqElem( faces, i );
        if (scale != 1) {
        	obj_rect.x = obj_rect.x * scale;
        	obj_rect.y = obj_rect.y * scale;
        	obj_rect.width = obj_rect.width * scale;
        	obj_rect.height = obj_rect.height * scale;
        }
		int index = find_the_region(&obj_rect);
		if (index < 0) {
			index = add_a_region(&obj_rect);
		}
		if (index < 0) {
			continue;
		}
		gcDetectRegions[index].objs[gcDetectImageNum] = obj_type;
    }
	
	if (gcDetectImageNum > 1)
	{
		for (i = 0; i < gcDetectRegionNum; i++)
		{
			GC_RESULT_TYPE result = region_contains_gc(&(gcDetectRegions[i]));
			if (GC_RESULT_TYPE_INVALID != result) return result;
		}//for
	}
	else {
		return GC_RESULT_TYPE_INVALID;
	}
}

//读取训练好的分类器。
static CvHaarClassifierCascade* gc_load_object_detector(const char* cascade_path)
{
    return (CvHaarClassifierCascade*)cvLoad(cascade_path);
}

static GC_RESULT_TYPE gc_detect_and_draw_objects(IplImage* image,
								GC_OBJ_TYPE type,
                              CvHaarClassifierCascade* cascade,
                              bool do_pyramids)
{
	GC_RESULT_TYPE result;
    IplImage* small_image = image;
    CvMemStorage* storage = cvCreateMemStorage(0); //创建动态内存
    CvSeq* faces;
    int i, scale = 1;

    /* if the flag is specified, down-scale the 输入图像 to get a
       performance boost w/o loosing quality (perhaps) */
    if( do_pyramids )
    {
        small_image = cvCreateImage( cvSize(image->width/2, image->height/2), IPL_DEPTH_8U, 3 );
        cvPyrDown( image, small_image, CV_GAUSSIAN_5x5 );//函数 cvPyrDown 使用 Gaussian 金字塔分解对输入图像向下采样。首先它对输入图像用指定滤波器进行卷积，然后通过拒绝偶数的行与列来下采样图像。
        scale = 2;
    }

    /* use the fastest variant */
    faces = cvHaarDetectObjects( small_image, cascade, storage, 1.2, 2, CV_HAAR_DO_CANNY_PRUNING);

	result = gc_on_detect_obj(type, faces, scale);

    if( small_image != image )
        cvReleaseImage( &small_image );

    cvReleaseMemStorage( &storage );  //释放动态内存
    return result;
}


//
//  0: OK
// -1: NG
int PutVideoGCData(const BYTE *pData, int len, int format, int width, int height)
{
	int ret;
	BYTE *pYV12;
	
	if (FALSE == gc_bInit) {
		return -1;
	}
	
	if (gcDetectImageNum >= GC_MAX_DETECT_IMAGE_NUM) {
		return -1;
	}
	
	if (pData == NULL) {
		return -1;
	}
    
	pYV12 = (BYTE *)malloc(width * height + (width * height >> 1));
	if (pYV12 == NULL) {
		return -1;
	}
	
	int nWidth = width;
	int nHeight = height;
	int plane = nWidth * nHeight;
	
	if (PixelFormat_YCbCr_420_SP == format)
	{
		memcpy(pYV12, pData, plane);
		for(int i = 0; i < plane/4; i++)
		{
			pYV12[plane + plane/4 + i] = pData[plane + i * 2];
			pYV12[plane + i] = pData[plane + i * 2 + 1];
		}
	}
	else if (PixelFormat_YV12 == format)
	{
		memcpy(pYV12,                   pData,                   plane);  /* Y */
		memcpy(pYV12 + plane + plane/4, pData + plane,           plane/4);/* V */
		memcpy(pYV12 + plane,           pData + plane + plane/4, plane/4);/* U */
	}
	else if (PixelFormat_YCbCr_422_SP == format)
	{
		int row, volume;
		memcpy(pYV12, pData, plane);
		for(int i = 0; i < plane/4; i++)
		{
			row = i/(nWidth/2);
			volume = i%(nWidth/2);
			/*V*/ pYV12[plane + plane/4 + i] = ( pData[plane + row * 2 * nWidth + 2 * volume]      +  pData[plane + (row * 2 + 1) * nWidth + 2 * volume]      )/2;
			/*U*/ pYV12[plane + i]           = ( pData[plane + row * 2 * nWidth + 2 * volume + 1]  +  pData[plane + (row * 2 + 1) * nWidth + 2 * volume + 1]  )/2;
		}
	}
	else if (PixelFormat_YCbCr_422_I == format)//YUY2
	{
		yuyv_to_yv12_c((__u8 *)pData, nWidth * 2, 
						pYV12, /* Y */
						pYV12 + plane + (plane >> 2), /* V */
						pYV12 + plane, /* U */
						nWidth, (nWidth >> 1), 
						nWidth, nHeight, 0);
	}
	else if (PixelFormat_RGBA_8888 == format)
	{
		rgba_to_yv12_c((__u8 *)pData, nWidth * 4, 
						pYV12, /* Y */
						pYV12 + plane + (plane >> 2), /* V */
						pYV12 + plane, /* U */
					   nWidth, (nWidth >> 1), 
					   nWidth, nHeight, 0);
	}
	else if (PixelFormat_RGB_565 == format)
	{
		rgb565_to_yv12_c((__u8 *)pData, nWidth * 2, 
						pYV12, /* Y */
						pYV12 + plane + (plane >> 2), /* V */
						pYV12 + plane, /* U */
						nWidth, (nWidth >> 1), 
						nWidth, nHeight, 0);
	}
	else {
		free(pYV12);
		return -1;
	}
	
	
	char jpg_file[256];
	IplImage *pFrame = cvCreateImage(cvSize(nWidth, nHeight), IPL_DEPTH_8U, 3);
	BYTE *pDst = (BYTE *) pFrame->imageData;
	
	assert(pFrame->imageSize == nWidth * nHeight * 3);
	//__android_log_print(ANDROID_LOG_INFO, "gc_detect", "nChannels=%d, alphaChannel=%d, depth=%d, colorModel=%s, channelSeq=%s, widthStep=%d\n", 
	//	pFrame->nChannels, pFrame->alphaChannel, pFrame->depth, pFrame->colorModel, pFrame->channelSeq, pFrame->widthStep);////Debug
	
#if 1
	yv12_to_bgr_c(pDst, nWidth * 3, 
				pYV12, /* Y */
				pYV12 + plane, /* U */ 
				pYV12 + plane + (plane >> 2), /* V */ 
				nWidth, (nWidth >> 1), 
				nWidth, nHeight, 0);
#else
	yv12_to_bgr_c(pDst, nWidth * 3, 
				pYV12, /* Y */
				pYV12 + plane + (plane >> 2), /* V */ 
				pYV12 + plane, /* U */ 
				nWidth, (nWidth >> 1), 
				nWidth, nHeight, 0);
#endif


	cvSetImageROI(pFrame, cvRect(nWidth/3, nHeight/6, nWidth/3, nHeight*2/3));
	IplImage *pFrame2 = cvCreateImage(cvSize(nWidth/3, nHeight*2/3), IPL_DEPTH_8U, 3);
	cvCopy(pFrame, pFrame2, 0);
	cvResetImageROI(pFrame);

#ifdef PLATFORM_ARMV7
	GC_RESULT_TYPE result = gc_detect_and_draw_objects( pFrame2, GC_OBJ_TYPE_FIST, cascade_fist, false );
	if (GC_RESULT_TYPE_INVALID == result) result = gc_detect_and_draw_objects( pFrame2, GC_OBJ_TYPE_PALM, cascade_palm, false );
#else
	GC_RESULT_TYPE result = gc_detect_and_draw_objects( pFrame2, GC_OBJ_TYPE_FIST, cascade_fist, true );
	if (GC_RESULT_TYPE_INVALID == result) result = gc_detect_and_draw_objects( pFrame2, GC_OBJ_TYPE_PALM, cascade_palm, true );
#endif

	__android_log_print(ANDROID_LOG_INFO, "gc_detect", "gc_detect_and_draw_objects()=%d...\n", result);

	gcDetectImageNum += 1;
	if (GC_RESULT_TYPE_INVALID == result)
	{
		if (gcDetectImageNum >= GC_MAX_DETECT_IMAGE_NUM) {
			if_gc_invalid();
		}
	}
	else if (GC_RESULT_TYPE_ARM == result) {
		for(int i = 0; i < gcDetectRegionNum; i++ )
		{
			/* extract the rectanlges only */
			CvRect face_rect = gcDetectRegions[i].rect;
			cvRectangle( pFrame2, cvPoint(face_rect.x,face_rect.y),
						 cvPoint((face_rect.x+face_rect.width),
								 (face_rect.y+face_rect.height)),
						 CV_RGB(0,255,0), 2 );
		}
		sprintf(jpg_file, "/data/data/com.wangling.remotephone/remotephone/gc_arm_%d.jpg", gcDetectImageNum);
		cvSaveImage(jpg_file, pFrame2);
		if_gc_arm();
	}
	else if (GC_RESULT_TYPE_DISARM == result) {
		for(int i = 0; i < gcDetectRegionNum; i++ )
		{
			/* extract the rectanlges only */
			CvRect face_rect = gcDetectRegions[i].rect;
			cvRectangle( pFrame2, cvPoint(face_rect.x,face_rect.y),
						 cvPoint((face_rect.x+face_rect.width),
								 (face_rect.y+face_rect.height)),
						 CV_RGB(0,255,0), 2 );
		}
		sprintf(jpg_file, "/data/data/com.wangling.remotephone/remotephone/gc_disarm_%d.jpg", gcDetectImageNum);
		cvSaveImage(jpg_file, pFrame2);
		if_gc_disarm();
	}


	cvReleaseImage(&pFrame2);

	cvReleaseImage(&pFrame);
	free(pYV12);
	return 0;
}

void gc_detect_init()
{
	if (gc_bInit) {
		return;
	}
	__android_log_print(ANDROID_LOG_INFO, "gc_detect", "gc_detect_init()...\n");
	
	colorspace_init();
	
	cascade_fist = gc_load_object_detector("/data/data/com.wangling.remotephone/remotephone/fist.dat");
	cascade_palm = gc_load_object_detector("/data/data/com.wangling.remotephone/remotephone/palm.dat");
	
	gcDetectImageNum = 0;
	gcDetectRegionNum = 0;
	for (int i = 0; i < GC_MAX_DETECT_REGION_NUM; i++)
	{
		memset(&(gcDetectRegions[i].rect), 0, sizeof(gcDetectRegions[i].rect));
		for (int j = 0; j < GC_MAX_DETECT_IMAGE_NUM; j++)
		{
			gcDetectRegions[i].objs[j] = GC_OBJ_TYPE_NONE;
		}
	}
	
	gc_bInit = TRUE;
}

void gc_detect_uninit()
{	
	if (gc_bInit)
	{
		gc_bInit = FALSE;
		__android_log_print(ANDROID_LOG_INFO, "gc_detect", "gc_detect_uninit()...\n");
		usleep(5500000);//important!!!
		
		cvReleaseHaarClassifierCascade( &cascade_fist );
		cvReleaseHaarClassifierCascade( &cascade_palm );
	}
}


#ifdef ANDROID_NDK

extern "C"
jint
MAKE_JNI_FUNC_NAME_FOR_MobileCameraActivity(PutVideoGCData)
	(JNIEnv* env, jobject thiz, jbyteArray data, jint len, jint format, jint width, jint height)
{
	int ret;
	
	jbyte *pData = (env)->GetByteArrayElements(data, NULL);
	if (pData == NULL) {
		return -1;
	}
	
	ret = PutVideoGCData((const BYTE *)pData, len, format, width, height);
	
	(env)->ReleaseByteArrayElements(data, pData, 0);
	return ret;
}

extern "C"
void
MAKE_JNI_FUNC_NAME_FOR_MobileCameraActivity(VideoGCInit)
	(JNIEnv* env, jobject thiz)
{
	gc_detect_init();
}

extern "C"
void
MAKE_JNI_FUNC_NAME_FOR_MobileCameraActivity(VideoGCUninit)
	(JNIEnv* env, jobject thiz)
{
	gc_detect_uninit();
}

#endif



/* 运动跟踪 ###################################################### */

#define MT_TURN_UP		0x01
#define MT_TURN_DOWN	0x02
#define MT_TURN_LEFT	0x04
#define MT_TURN_RIGHT	0x08
#define MT_TURN_ADVANCE	0x10
#define MT_TURN_BACK	0x20
#define MT_NO_FACE		0xff

#define IMAGE_CENTER_LINE_X  320
#define IMAGE_LEFT_LINE_X   (IMAGE_CENTER_LINE_X - 35)
#define IMAGE_RIGHT_LINE_X  (IMAGE_CENTER_LINE_X + 35)
#define KEEP_STILL_X_DIFF1	-10
#define KEEP_STILL_X_DIFF2	10

#define IMAGE_CENTER_LINE_Y  240
#define IMAGE_UP_LINE_Y   (IMAGE_CENTER_LINE_Y - 35)
#define IMAGE_DOWN_LINE_Y  (IMAGE_CENTER_LINE_Y + 35)
#define KEEP_STILL_Y_DIFF1	-10
#define KEEP_STILL_Y_DIFF2	10

#define IMAGE_CENTER_CX			140
#define IMAGE_SMALL_CX			(IMAGE_CENTER_CX - 30)
#define IMAGE_BIG_CX			(IMAGE_CENTER_CX + 30)
#define KEEP_STILL_CX_DIFF1		-15
#define KEEP_STILL_CX_DIFF2		15

static BOOL mt_bInit = FALSE;
static CvHaarClassifierCascade* cascade_face;
static int mt_prev_x = -1;
static int mt_prev_y = -1;
static int mt_prev_cx = -1;


static unsigned char mt_on_detect_obj(CvSeq* faces, int scale)
{
	unsigned char turn = 0;
	int saved_s = 2147483647;
	int saved_x = -1;
	int saved_y = -1;
	int saved_cx = -1;
	
	__android_log_print(ANDROID_LOG_INFO, "mt_detect", "mt_on_detect_obj(faces=%d)...\n", faces->total);
	
	if (faces->total <= 0) {
		return MT_NO_FACE;
	}
	
    for (int i = 0; i < faces->total; i++ )
    {
    	int s;
    	int x,y,cx;
    	CvRect obj_rect = *(CvRect*)cvGetSeqElem( faces, i );
    	x = (obj_rect.x + obj_rect.width/2)*scale;
    	y = (obj_rect.y + obj_rect.height/2)*scale;
    	cx = obj_rect.width * scale;
    	if (mt_prev_x == -1 || mt_prev_y == -1) {
    		if (faces->total == 1) {//设立初始坐标，要求只检测到一个目标
    			mt_prev_x = x;
    			mt_prev_y = y;
    			mt_prev_cx = cx;
    		}
    		return 0;
    	}
    	//找出移动最少的那个目标
    	s = abs(x - mt_prev_x) * abs(x - mt_prev_x) + abs(y - mt_prev_y) * abs(y - mt_prev_y);
    	if (s < saved_s) {
    		saved_s = s;
    		saved_x = x;
    		saved_y = y;
    		saved_cx = cx;
    	}
    }//for
    
    int saved_prev_x = mt_prev_x;
    int saved_prev_y = mt_prev_y;
    int saved_prev_cx = mt_prev_cx;
    mt_prev_x = saved_x;
    mt_prev_y = saved_y;
    mt_prev_cx = saved_cx;
    
    if (saved_x - saved_prev_x < KEEP_STILL_X_DIFF1)//目标向左运动
    {
    	if (saved_x < IMAGE_CENTER_LINE_X) {//位置在左半部
    		__android_log_print(ANDROID_LOG_INFO, "mt_detect", "pre=%d, curr=%d, <<<======  left, move->left\n", saved_prev_x, saved_x);
    		turn |= MT_TURN_LEFT;
    	}
    	else {//位置在右半部
    		__android_log_print(ANDROID_LOG_INFO, "mt_detect", "pre=%d, curr=%d, *********  right, move->left\n", saved_prev_x, saved_x);
    	}
    }
    else if (saved_x - saved_prev_x > KEEP_STILL_X_DIFF2)//目标向右运动
    {
    	if (saved_x < IMAGE_CENTER_LINE_X) {//位置在左半部
    		__android_log_print(ANDROID_LOG_INFO, "mt_detect", "pre=%d, curr=%d, *********  left, move->right\n", saved_prev_x, saved_x);
    	}
    	else {//位置在右半部
    		__android_log_print(ANDROID_LOG_INFO, "mt_detect", "pre=%d, curr=%d, ======>>>  right, move->right\n", saved_prev_x, saved_x);
    		turn |= MT_TURN_RIGHT;
    	}
    }
    else {//目标静止
    	if (saved_x < IMAGE_LEFT_LINE_X) {
    		__android_log_print(ANDROID_LOG_INFO, "mt_detect", "pre=%d, curr=%d, <<<======  left, no move\n", saved_prev_x, saved_x);
    		turn |= MT_TURN_LEFT;
    	}
    	else if (saved_x > IMAGE_RIGHT_LINE_X) {
    		__android_log_print(ANDROID_LOG_INFO, "mt_detect", "pre=%d, curr=%d, ======>>>  right, no move\n", saved_prev_x, saved_x);
    		turn |= MT_TURN_RIGHT;
    	}
    	else {
    		__android_log_print(ANDROID_LOG_INFO, "mt_detect", "pre=%d, curr=%d, *********  center, no move\n", saved_prev_x, saved_x);
    	}
    }
    
    
    if (saved_y - saved_prev_y < KEEP_STILL_Y_DIFF1)//目标向上运动
    {
    	if (saved_y < IMAGE_CENTER_LINE_Y) {//位置在上半部
    		__android_log_print(ANDROID_LOG_INFO, "mt_detect_y", "pre=%d, curr=%d, |^|^|^|^|  up, move->up\n", saved_prev_y, saved_y);
    		turn |= MT_TURN_UP;
    	}
    	else {//位置在下半部
    		__android_log_print(ANDROID_LOG_INFO, "mt_detect_y", "pre=%d, curr=%d, *********  down, move->up\n", saved_prev_y, saved_y);
    	}
    }
    else if (saved_y - saved_prev_y > KEEP_STILL_Y_DIFF2)//目标向下运动
    {
    	if (saved_y < IMAGE_CENTER_LINE_Y) {//位置在上半部
    		__android_log_print(ANDROID_LOG_INFO, "mt_detect_y", "pre=%d, curr=%d, *********  up, move->down\n", saved_prev_y, saved_y);
    	}
    	else {//位置在下半部
    		__android_log_print(ANDROID_LOG_INFO, "mt_detect_y", "pre=%d, curr=%d, VVVVVVVVV  down, move->down\n", saved_prev_y, saved_y);
    		turn |= MT_TURN_DOWN;
    	}
    }
    else {//目标静止
    	if (saved_y < IMAGE_UP_LINE_Y) {
    		__android_log_print(ANDROID_LOG_INFO, "mt_detect_y", "pre=%d, curr=%d, |^|^|^|^|  up, no move\n", saved_prev_y, saved_y);
    		turn |= MT_TURN_UP;
    	}
    	else if (saved_y > IMAGE_DOWN_LINE_Y) {
    		__android_log_print(ANDROID_LOG_INFO, "mt_detect_y", "pre=%d, curr=%d, VVVVVVVVV  down, no move\n", saved_prev_y, saved_y);
    		turn |= MT_TURN_DOWN;
    	}
    	else {
    		__android_log_print(ANDROID_LOG_INFO, "mt_detect_y", "pre=%d, curr=%d, *********  center, no move\n", saved_prev_y, saved_y);
    	}
    }
    
    
    if (saved_cx - saved_prev_cx < KEEP_STILL_CX_DIFF1)//目标向后运动
    {
    	if (saved_cx < IMAGE_CENTER_CX) {//位置靠后
    		__android_log_print(ANDROID_LOG_INFO, "mt_detect_cx", "pre=%d, curr=%d, ADVANCE  back, move->back\n", saved_prev_cx, saved_cx);
    		turn |= MT_TURN_ADVANCE;
    	}
    	else {//位置靠前
    		__android_log_print(ANDROID_LOG_INFO, "mt_detect_cx", "pre=%d, curr=%d, *******  advance, move->back\n", saved_prev_cx, saved_cx);
    	}
    }
    else if (saved_cx - saved_prev_cx > KEEP_STILL_CX_DIFF2)//目标向前运动
    {
    	if (saved_cx < IMAGE_CENTER_CX) {//位置靠后
    		__android_log_print(ANDROID_LOG_INFO, "mt_detect_cx", "pre=%d, curr=%d, *******  back, move->advance\n", saved_prev_cx, saved_cx);
    	}
    	else {//位置靠前
    		__android_log_print(ANDROID_LOG_INFO, "mt_detect_cx", "pre=%d, curr=%d, BACK    advance, move->advance\n", saved_prev_cx, saved_cx);
    		turn |= MT_TURN_BACK;
    	}
    }
    else {//目标静止
    	if (saved_cx < IMAGE_SMALL_CX) {
    		__android_log_print(ANDROID_LOG_INFO, "mt_detect_cx", "pre=%d, curr=%d, ADVANCE  back, no move\n", saved_prev_cx, saved_cx);
    		turn |= MT_TURN_ADVANCE;
    	}
    	else if (saved_cx > IMAGE_BIG_CX) {
    		__android_log_print(ANDROID_LOG_INFO, "mt_detect_cx", "pre=%d, curr=%d, BACK    advance, no move\n", saved_prev_cx, saved_cx);
    		turn |= MT_TURN_BACK;
    	}
    	else {
    		__android_log_print(ANDROID_LOG_INFO, "mt_detect_cx", "pre=%d, curr=%d, *******  center, no move\n", saved_prev_cx, saved_cx);
    	}
    }
    
    return turn;
}

//读取训练好的分类器。
static CvHaarClassifierCascade* mt_load_object_detector(const char* cascade_path)
{
    return (CvHaarClassifierCascade*)cvLoad(cascade_path);
}

static unsigned char mt_detect_and_draw_objects(IplImage* image,
                              CvHaarClassifierCascade* cascade,
                              bool do_pyramids)
{
	unsigned char turn;
    IplImage* small_image = image;
    CvMemStorage* storage = cvCreateMemStorage(0); //创建动态内存
    CvSeq* faces;
    int i, scale = 1;

    /* if the flag is specified, down-scale the 输入图像 to get a
       performance boost w/o loosing quality (perhaps) */
    if( do_pyramids )
    {
        small_image = cvCreateImage( cvSize(image->width/2, image->height/2), IPL_DEPTH_8U, 3 );
        cvPyrDown( image, small_image, CV_GAUSSIAN_5x5 );//函数 cvPyrDown 使用 Gaussian 金字塔分解对输入图像向下采样。首先它对输入图像用指定滤波器进行卷积，然后通过拒绝偶数的行与列来下采样图像。
        scale = 2;
    }

    /* use the fastest variant */
    faces = cvHaarDetectObjects( small_image, cascade, storage, 1.2, 2, CV_HAAR_DO_CANNY_PRUNING);

	turn = mt_on_detect_obj(faces, scale);

    if( small_image != image )
        cvReleaseImage( &small_image );

    cvReleaseMemStorage( &storage );  //释放动态内存
    return turn;
}


//
//  0: OK
// -1: NG
int PutVideoMTData(const BYTE *pData, int len, int format, int width, int height)
{
	int ret;
	BYTE *pYV12;
	
	if (FALSE == mt_bInit) {
		return -1;
	}
	
	if (pData == NULL) {
		return -1;
	}
    
	pYV12 = (BYTE *)malloc(width * height + (width * height >> 1));
	if (pYV12 == NULL) {
		return -1;
	}
	
	int nWidth = width;
	int nHeight = height;
	int plane = nWidth * nHeight;
	
	if (PixelFormat_YCbCr_420_SP == format)
	{
		memcpy(pYV12, pData, plane);
		for(int i = 0; i < plane/4; i++)
		{
			pYV12[plane + plane/4 + i] = pData[plane + i * 2];
			pYV12[plane + i] = pData[plane + i * 2 + 1];
		}
	}
	else if (PixelFormat_YV12 == format)
	{
		memcpy(pYV12,                   pData,                   plane);  /* Y */
		memcpy(pYV12 + plane + plane/4, pData + plane,           plane/4);/* V */
		memcpy(pYV12 + plane,           pData + plane + plane/4, plane/4);/* U */
	}
	else if (PixelFormat_YCbCr_422_SP == format)
	{
		int row, volume;
		memcpy(pYV12, pData, plane);
		for(int i = 0; i < plane/4; i++)
		{
			row = i/(nWidth/2);
			volume = i%(nWidth/2);
			/*V*/ pYV12[plane + plane/4 + i] = ( pData[plane + row * 2 * nWidth + 2 * volume]      +  pData[plane + (row * 2 + 1) * nWidth + 2 * volume]      )/2;
			/*U*/ pYV12[plane + i]           = ( pData[plane + row * 2 * nWidth + 2 * volume + 1]  +  pData[plane + (row * 2 + 1) * nWidth + 2 * volume + 1]  )/2;
		}
	}
	else if (PixelFormat_YCbCr_422_I == format)//YUY2
	{
		yuyv_to_yv12_c((__u8 *)pData, nWidth * 2, 
						pYV12, /* Y */
						pYV12 + plane + (plane >> 2), /* V */
						pYV12 + plane, /* U */
						nWidth, (nWidth >> 1), 
						nWidth, nHeight, 0);
	}
	else if (PixelFormat_RGBA_8888 == format)
	{
		rgba_to_yv12_c((__u8 *)pData, nWidth * 4, 
						pYV12, /* Y */
						pYV12 + plane + (plane >> 2), /* V */
						pYV12 + plane, /* U */
					   nWidth, (nWidth >> 1), 
					   nWidth, nHeight, 0);
	}
	else if (PixelFormat_RGB_565 == format)
	{
		rgb565_to_yv12_c((__u8 *)pData, nWidth * 2, 
						pYV12, /* Y */
						pYV12 + plane + (plane >> 2), /* V */
						pYV12 + plane, /* U */
						nWidth, (nWidth >> 1), 
						nWidth, nHeight, 0);
	}
	else {
		free(pYV12);
		return -1;
	}
	
	
	char jpg_file[256];
	IplImage *pFrame = cvCreateImage(cvSize(nWidth, nHeight), IPL_DEPTH_8U, 3);
	BYTE *pDst = (BYTE *) pFrame->imageData;
	
	assert(pFrame->imageSize == nWidth * nHeight * 3);
	//__android_log_print(ANDROID_LOG_INFO, "mt_detect", "nChannels=%d, alphaChannel=%d, depth=%d, colorModel=%s, channelSeq=%s, widthStep=%d\n", 
	//	pFrame->nChannels, pFrame->alphaChannel, pFrame->depth, pFrame->colorModel, pFrame->channelSeq, pFrame->widthStep);////Debug
	
#if 1
	yv12_to_bgr_c(pDst, nWidth * 3, 
				pYV12, /* Y */
				pYV12 + plane, /* U */ 
				pYV12 + plane + (plane >> 2), /* V */ 
				nWidth, (nWidth >> 1), 
				nWidth, nHeight, 0);
#else
	yv12_to_bgr_c(pDst, nWidth * 3, 
				pYV12, /* Y */
				pYV12 + plane + (plane >> 2), /* V */ 
				pYV12 + plane, /* U */ 
				nWidth, (nWidth >> 1), 
				nWidth, nHeight, 0);
#endif


	unsigned char turn = mt_detect_and_draw_objects( pFrame, cascade_face, true );
	if (turn == MT_NO_FACE)
	{
		//if_vision_navigation_obj(0);
	}
	else
	{
		if (turn & MT_TURN_UP) {
			if_contrl_turn_up_little();
		}
		else if (turn & MT_TURN_DOWN) {
			if_contrl_turn_down_little();
		}
		
		if (turn & MT_TURN_ADVANCE)
		{
			if (turn & MT_TURN_LEFT) {
				if_contrl_move_advance_left_little(-1);
			}
			else if (turn & MT_TURN_RIGHT) {
				if_contrl_move_advance_right_little(-1);
			}
			else {
				if_contrl_move_advance_little(-1);
			}
		}
		else if (turn & MT_TURN_BACK)
		{
			if (turn & MT_TURN_LEFT) {
				if_contrl_move_back_left_little(-1);
			}
			else if (turn & MT_TURN_RIGHT) {
				if_contrl_move_back_right_little(-1);
			}
			else {
				if_contrl_move_back_little(-1);
			}
		}
		
		//if_vision_navigation_obj(mt_prev_cx);
	}

	cvReleaseImage(&pFrame);
	free(pYV12);
	return 0;
}

void mt_detect_init()
{
	if (mt_bInit) {
		return;
	}
	__android_log_print(ANDROID_LOG_INFO, "mt_detect", "mt_detect_init()...\n");
	
	colorspace_init();
	
	cascade_face = mt_load_object_detector("/data/data/com.wangling.remotephone/remotephone/haarcascade_frontalface_alt2.xml");
	mt_prev_x = -1;
	mt_prev_y = -1;
	mt_prev_cx = -1;
	
	mt_bInit = TRUE;
}

void mt_detect_uninit()
{	
	if (mt_bInit)
	{
		mt_bInit = FALSE;
		__android_log_print(ANDROID_LOG_INFO, "mt_detect", "mt_detect_uninit()...\n");
		usleep(5500000);//important!!!
		
		cvReleaseHaarClassifierCascade( &cascade_face );
	}
}

////////////////////////////////////////////////////////////////////////////////

#ifdef ANDROID_NDK

extern "C"
jint
MAKE_JNI_FUNC_NAME_FOR_MobileCameraActivity(PutVideoMTData)
	(JNIEnv* env, jobject thiz, jbyteArray data, jint len, jint format, jint width, jint height)
{
	int ret = -1;
	
	jbyte *pData = (env)->GetByteArrayElements(data, NULL);
	if (pData == NULL) {
		return -1;
	}
	
	if (mt_bInit) {
		ret = PutVideoMTData((const BYTE *)pData, len, format, width, height);
	}
	
	(env)->ReleaseByteArrayElements(data, pData, 0);
	return ret;
}

extern "C"
void
MAKE_JNI_FUNC_NAME_FOR_MobileCameraActivity(VideoMTInit)
	(JNIEnv* env, jobject thiz)
{
	mt_detect_init();
}

extern "C"
void
MAKE_JNI_FUNC_NAME_FOR_MobileCameraActivity(VideoMTUninit)
	(JNIEnv* env, jobject thiz)
{
	mt_detect_uninit();
}

#endif


#endif /* NO_OPENCV */
