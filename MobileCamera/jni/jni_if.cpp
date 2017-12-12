#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <assert.h>
#include <jni.h>
#include "jni_func.h"
#include "jni_if.h"

#include <android/log.h>


static JavaVM* g_vm = NULL;
static jobject g_objCv = NULL;



extern "C"
jint JNI_OnLoad(JavaVM* vm, void* reserved)
{
	JNIEnv* env = NULL;
	
	__android_log_print(ANDROID_LOG_INFO, "mobcam_if", "Cv:JNI_OnLoad()");
	
	if ((vm)->GetEnv((void**) &env, JNI_VERSION_1_6) != JNI_OK) 
	{
		__android_log_print(ANDROID_LOG_INFO, "JNI_OnLoad", "(vm)->GetEnv() failed!");
		return -1;
	}
	g_vm = vm;
	
//	if (!registerNatives(env))
//	{
//		//зЂВс
//		return -1;
//	}
	
	/* success -- return valid version number */
	return JNI_VERSION_1_6;
}

extern "C"
void
MAKE_JNI_FUNC_NAME_FOR_MobileCameraActivity(SetThisObjectCv)
	(JNIEnv* env, jobject thiz)
{
	__android_log_print(ANDROID_LOG_INFO, "mobcam_if", "Cv:MobileCameraActivity_SetThisObjectCv()");
	g_objCv = (env)->NewGlobalRef(thiz);
}

extern "C"
void JNI_OnUnload(JavaVM* vm, void* reserved)
{
	__android_log_print(ANDROID_LOG_INFO, "mobcam_if", "Cv:JNI_OnUnload()");
	
	JNIEnv* env = NULL;
	if ((vm)->GetEnv((void**) &env, JNI_VERSION_1_6) != JNI_OK) 
	{
		return;
	}
}


void if_contrl_turn_up_little()
{
	__android_log_print(ANDROID_LOG_INFO, "mobcam_if", "if_contrl_turn_up_little()");
	
	int status;
	bool isAttached = false;
	JNIEnv *env = NULL;

	status = g_vm->GetEnv((void**)&env, JNI_VERSION_1_6);
	if(status != JNI_OK)
	{
		status = g_vm->AttachCurrentThread(&env, NULL);
		if(status != JNI_OK)
		{
			return;
		}
		isAttached = true;
	}
	
	
	jclass cls = (env)->GetObjectClass(g_objCv);
	jmethodID mid = (env)->GetStaticMethodID(cls, "j_contrl_turn_up_little", "()V");

	(env)->CallStaticVoidMethod(cls, mid);
	if (isAttached) {// From native thread
		g_vm->DetachCurrentThread();
	}
}

void if_contrl_turn_down_little()
{
	__android_log_print(ANDROID_LOG_INFO, "mobcam_if", "if_contrl_turn_down_little()");
	
	int status;
	bool isAttached = false;
	JNIEnv *env = NULL;

	status = g_vm->GetEnv((void**)&env, JNI_VERSION_1_6);
	if(status != JNI_OK)
	{
		status = g_vm->AttachCurrentThread(&env, NULL);
		if(status != JNI_OK)
		{
			return;
		}
		isAttached = true;
	}
	
	
	jclass cls = (env)->GetObjectClass(g_objCv);
	jmethodID mid = (env)->GetStaticMethodID(cls, "j_contrl_turn_down_little", "()V");

	(env)->CallStaticVoidMethod(cls, mid);
	if (isAttached) {// From native thread
		g_vm->DetachCurrentThread();
	}
}

void if_contrl_move_advance_little(int param)
{
	__android_log_print(ANDROID_LOG_INFO, "mobcam_if", "if_contrl_move_advance_little(%d)", param);
	
	int status;
	bool isAttached = false;
	JNIEnv *env = NULL;

	status = g_vm->GetEnv((void**)&env, JNI_VERSION_1_6);
	if(status != JNI_OK)
	{
		status = g_vm->AttachCurrentThread(&env, NULL);
		if(status != JNI_OK)
		{
			return;
		}
		isAttached = true;
	}
	
	
	jclass cls = (env)->GetObjectClass(g_objCv);
	jmethodID mid = (env)->GetStaticMethodID(cls, "j_contrl_move_advance_little", "(I)V");

	(env)->CallStaticVoidMethod(cls, mid, param);
	if (isAttached) {// From native thread
		g_vm->DetachCurrentThread();
	}
}

void if_contrl_move_back_little(int param)
{
	__android_log_print(ANDROID_LOG_INFO, "mobcam_if", "if_contrl_move_back_little(%d)", param);
	
	int status;
	bool isAttached = false;
	JNIEnv *env = NULL;

	status = g_vm->GetEnv((void**)&env, JNI_VERSION_1_6);
	if(status != JNI_OK)
	{
		status = g_vm->AttachCurrentThread(&env, NULL);
		if(status != JNI_OK)
		{
			return;
		}
		isAttached = true;
	}
	
	
	jclass cls = (env)->GetObjectClass(g_objCv);
	jmethodID mid = (env)->GetStaticMethodID(cls, "j_contrl_move_back_little", "(I)V");

	(env)->CallStaticVoidMethod(cls, mid, param);
	if (isAttached) {// From native thread
		g_vm->DetachCurrentThread();
	}
}

void if_contrl_move_advance_left_little(int param)
{
	__android_log_print(ANDROID_LOG_INFO, "mobcam_if", "if_contrl_move_advance_left_little(%d)", param);
	
	int status;
	bool isAttached = false;
	JNIEnv *env = NULL;

	status = g_vm->GetEnv((void**)&env, JNI_VERSION_1_6);
	if(status != JNI_OK)
	{
		status = g_vm->AttachCurrentThread(&env, NULL);
		if(status != JNI_OK)
		{
			return;
		}
		isAttached = true;
	}
	
	
	jclass cls = (env)->GetObjectClass(g_objCv);
	jmethodID mid = (env)->GetStaticMethodID(cls, "j_contrl_move_advance_left_little", "(I)V");

	(env)->CallStaticVoidMethod(cls, mid, param);
	if (isAttached) {// From native thread
		g_vm->DetachCurrentThread();
	}
}

void if_contrl_move_advance_right_little(int param)
{
	__android_log_print(ANDROID_LOG_INFO, "mobcam_if", "if_contrl_move_advance_right_little(%d)", param);
	
	int status;
	bool isAttached = false;
	JNIEnv *env = NULL;

	status = g_vm->GetEnv((void**)&env, JNI_VERSION_1_6);
	if(status != JNI_OK)
	{
		status = g_vm->AttachCurrentThread(&env, NULL);
		if(status != JNI_OK)
		{
			return;
		}
		isAttached = true;
	}
	
	
	jclass cls = (env)->GetObjectClass(g_objCv);
	jmethodID mid = (env)->GetStaticMethodID(cls, "j_contrl_move_advance_right_little", "(I)V");

	(env)->CallStaticVoidMethod(cls, mid, param);
	if (isAttached) {// From native thread
		g_vm->DetachCurrentThread();
	}
}

void if_contrl_move_back_left_little(int param)
{
	__android_log_print(ANDROID_LOG_INFO, "mobcam_if", "if_contrl_move_back_left_little(%d)", param);
	
	int status;
	bool isAttached = false;
	JNIEnv *env = NULL;

	status = g_vm->GetEnv((void**)&env, JNI_VERSION_1_6);
	if(status != JNI_OK)
	{
		status = g_vm->AttachCurrentThread(&env, NULL);
		if(status != JNI_OK)
		{
			return;
		}
		isAttached = true;
	}
	
	
	jclass cls = (env)->GetObjectClass(g_objCv);
	jmethodID mid = (env)->GetStaticMethodID(cls, "j_contrl_move_back_left_little", "(I)V");

	(env)->CallStaticVoidMethod(cls, mid, param);
	if (isAttached) {// From native thread
		g_vm->DetachCurrentThread();
	}
}

void if_contrl_move_back_right_little(int param)
{
	__android_log_print(ANDROID_LOG_INFO, "mobcam_if", "if_contrl_move_back_right_little(%d)", param);
	
	int status;
	bool isAttached = false;
	JNIEnv *env = NULL;

	status = g_vm->GetEnv((void**)&env, JNI_VERSION_1_6);
	if(status != JNI_OK)
	{
		status = g_vm->AttachCurrentThread(&env, NULL);
		if(status != JNI_OK)
		{
			return;
		}
		isAttached = true;
	}
	
	
	jclass cls = (env)->GetObjectClass(g_objCv);
	jmethodID mid = (env)->GetStaticMethodID(cls, "j_contrl_move_back_right_little", "(I)V");

	(env)->CallStaticVoidMethod(cls, mid, param);
	if (isAttached) {// From native thread
		g_vm->DetachCurrentThread();
	}
}

void if_gc_arm()
{
	__android_log_print(ANDROID_LOG_INFO, "mobcam_if", "if_gc_arm()");
	
	int status;
	bool isAttached = false;
	JNIEnv *env = NULL;

	status = g_vm->GetEnv((void**)&env, JNI_VERSION_1_6);
	if(status != JNI_OK)
	{
		status = g_vm->AttachCurrentThread(&env, NULL);
		if(status != JNI_OK)
		{
			return;
		}
		isAttached = true;
	}
	
	
	jclass cls = (env)->GetObjectClass(g_objCv);
	jmethodID mid = (env)->GetStaticMethodID(cls, "j_gc_arm", "()V");

	(env)->CallStaticVoidMethod(cls, mid);
	if (isAttached) {// From native thread
		g_vm->DetachCurrentThread();
	}
}

void if_gc_disarm()
{
	__android_log_print(ANDROID_LOG_INFO, "mobcam_if", "if_gc_disarm()");
	
	int status;
	bool isAttached = false;
	JNIEnv *env = NULL;

	status = g_vm->GetEnv((void**)&env, JNI_VERSION_1_6);
	if(status != JNI_OK)
	{
		status = g_vm->AttachCurrentThread(&env, NULL);
		if(status != JNI_OK)
		{
			return;
		}
		isAttached = true;
	}
	
	
	jclass cls = (env)->GetObjectClass(g_objCv);
	jmethodID mid = (env)->GetStaticMethodID(cls, "j_gc_disarm", "()V");

	(env)->CallStaticVoidMethod(cls, mid);
	if (isAttached) {// From native thread
		g_vm->DetachCurrentThread();
	}
}

void if_gc_invalid()
{
	__android_log_print(ANDROID_LOG_INFO, "mobcam_if", "if_gc_invalid()");
	
	int status;
	bool isAttached = false;
	JNIEnv *env = NULL;

	status = g_vm->GetEnv((void**)&env, JNI_VERSION_1_6);
	if(status != JNI_OK)
	{
		status = g_vm->AttachCurrentThread(&env, NULL);
		if(status != JNI_OK)
		{
			return;
		}
		isAttached = true;
	}
	
	
	jclass cls = (env)->GetObjectClass(g_objCv);
	jmethodID mid = (env)->GetStaticMethodID(cls, "j_gc_invalid", "()V");

	(env)->CallStaticVoidMethod(cls, mid);
	if (isAttached) {// From native thread
		g_vm->DetachCurrentThread();
	}
}

void if_gc_detect_obj(int obj_type)
{
	__android_log_print(ANDROID_LOG_INFO, "mobcam_if", "if_gc_detect_obj()");
	
	int status;
	bool isAttached = false;
	JNIEnv *env = NULL;

	status = g_vm->GetEnv((void**)&env, JNI_VERSION_1_6);
	if(status != JNI_OK)
	{
		status = g_vm->AttachCurrentThread(&env, NULL);
		if(status != JNI_OK)
		{
			return;
		}
		isAttached = true;
	}
	
	
	jclass cls = (env)->GetObjectClass(g_objCv);
	jmethodID mid = (env)->GetStaticMethodID(cls, "j_gc_detect_obj", "(I)V");

	(env)->CallStaticVoidMethod(cls, mid, obj_type);
	if (isAttached) {// From native thread
		g_vm->DetachCurrentThread();
	}
}
