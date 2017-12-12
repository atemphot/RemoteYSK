package com.wangling.remotephone;

import java.io.File;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.Gravity;
import android.widget.Toast;
import com.wangling.remotephone.R;


public class SharedFuncLib {
	
	public static final String ANYPC_LOCAL_LAN		="[LOCAL_LAN]";
	
	public static final String SYS_TEMP_USER = "Assistant";
	public static final String SYS_TEMP_PASSWORD = "ykz123";
	
	public static final int USE_CAMERA_ID = 0;
	
	public static final int ARDUINO_SPEED_BASE = 245;
	
	public static final int SERIAL_PORT_BAUDRATE = 115200;
	
	/* Definition of SOCKET_TYPE */
	public static final int SOCKET_TYPE_UNKNOWN = 0;
	public static final int SOCKET_TYPE_UDT = 1;
	public static final int SOCKET_TYPE_TCP = 2;
	
	/* Definition of CtrlCmd Result Code */
	public static final short CTRLCMD_RESULT_NG = 0;//0x0000
	public static final short CTRLCMD_RESULT_OK = 1;//0x0001
	

	/* Definition of AV flags */
	public static final byte AV_FLAGS_VIDEO_ENABLE		=0x01;  /* bit 0 */
	public static final byte AV_FLAGS_AUDIO_ENABLE		=0x02;  /* bit 1 */
	public static final byte AV_FLAGS_VIDEO_RELIABLE	=	0x04;  /* bit 2 */
	public static final byte AV_FLAGS_AUDIO_REDUNDANCE	=0x08;  /* bit 3 */
	public static final byte AV_FLAGS_VIDEO_HWACCE		=0x10;  /* bit 4 */
	public static final byte AV_FLAGS_AUDIO_HWACCE		=0x20;  /* bit 5 */
	public static final byte AV_FLAGS_VIDEO_H264		=0x40;  /* bit 6 */
	public static final byte AV_FLAGS_AUDIO_G729A		=(byte) 0x80;  /* bit 7 */

	/* Definition of AV video mode */
	public static final byte AV_VIDEO_MODE_NULL			=0x00;
	public static final byte AV_VIDEO_MODE_X264			=0x01;
	public static final byte AV_VIDEO_MODE_FF263		=0x02;
	public static final byte AV_VIDEO_MODE_FF264		=0x03;

	/* Definition of AV video size */
	public static final byte AV_VIDEO_SIZE_NULL			=0x00;
	public static final byte AV_VIDEO_SIZE_QCIF			=0x01;  /* 176 x 144 */
	public static final byte AV_VIDEO_SIZE_QVGA			=0x02;  /* 320 x 240 */
	public static final byte AV_VIDEO_SIZE_CIF			=0x03;  /* 352 x 288 */
	public static final byte AV_VIDEO_SIZE_480x320		=0x04;  /* 480 x 320 */
	public static final byte AV_VIDEO_SIZE_VGA			=0x05;  /* 640 x 480 */


	/* Definition of AV Control */
	public static final short AV_CONTRL_TURN_CENTER		=0x0000;
	public static final short AV_CONTRL_TURN_UP			=0x0001;
	public static final short AV_CONTRL_TURN_DOWN		=	0x0002;
	public static final short AV_CONTRL_TURN_LEFT		=	0x0003;
	public static final short AV_CONTRL_TURN_RIGHT		=0x0004;
	public static final short AV_CONTRL_ZOOM_IN			=0x0005;
	public static final short AV_CONTRL_ZOOM_OUT		=	0x0006;
	public static final short AV_CONTRL_RECORDVOL_UP	=	0x0007;
	public static final short AV_CONTRL_RECORDVOL_DOWN	=0x0008;
	public static final short AV_CONTRL_RECORDVOL_SET	=	0x0009;  /* contrl_param */
	public static final short AV_CONTRL_FLASH_ON		=	0x000a;
	public static final short AV_CONTRL_FLASH_OFF		=	0x000b;
	public static final short AV_CONTRL_LEFT_SERVO		=0x000c;  /* contrl_param */
	public static final short AV_CONTRL_RIGHT_SERVO		=0x000d;  /* contrl_param */
	public static final short AV_CONTRL_SEARCH_POWER	=0x000e;
	public static final short AV_CONTRL_TAKE_PICTURE	=0x000f;

	public static final short AV_CONTRL_SYSTEM_SHUTDOWN	=	0x0021;
	public static final short AV_CONTRL_SYSTEM_REBOOT	=	0x0022;

	public static final short AV_CONTRL_DPAD_UP			=AV_CONTRL_TURN_UP;
	public static final short AV_CONTRL_DPAD_DOWN		=AV_CONTRL_TURN_DOWN;
	public static final short AV_CONTRL_DPAD_LEFT		=AV_CONTRL_TURN_LEFT;
	public static final short AV_CONTRL_DPAD_RIGHT		=AV_CONTRL_TURN_RIGHT;
	public static final short AV_CONTRL_JOYSTICK1		=	0x0031;  /* L|angle */
	public static final short AV_CONTRL_JOYSTICK2		=	0x0032;  /* L|angle */ //throttle
	public static final short AV_CONTRL_BUTTON_A		=	0x0033;
	public static final short AV_CONTRL_BUTTON_B		=	0x0034;
	public static final short AV_CONTRL_BUTTON_X		=	0x0035;
	public static final short AV_CONTRL_BUTTON_Y		=	0x0036;
	public static final short AV_CONTRL_BUTTON_L1		=	0x0037;
	public static final short AV_CONTRL_BUTTON_L2		=	0x0038;  /* 0,1 */
	public static final short AV_CONTRL_BUTTON_R1		=	0x0039;
	public static final short AV_CONTRL_BUTTON_R2		=	0x003a;  /* 0,1 */


	/* TLV Types */
	public static final short TLV_TYPE_BATTERY1_REMAIN	=0x0000; //百分比
	public static final short TLV_TYPE_BATTERY2_REMAIN	=0x0001; //百分比
	public static final short TLV_TYPE_TEMP				=0x0002;
	public static final short TLV_TYPE_HUMI				=0x0003;
	public static final short TLV_TYPE_MQX				=0x0004;
	public static final short TLV_TYPE_SIGNAL_STRENGTH	=0x0005; //百分比
	public static final short TLV_TYPE_GPS_COUNT		=0x0006;
	public static final short TLV_TYPE_GPS_LONG			=0x0007;
	public static final short TLV_TYPE_GPS_LATI			=0x0008;
	public static final short TLV_TYPE_GPS_ALTI			=0x0009;
	public static final short TLV_TYPE_TOTAL_TIME		=0x000A;
	public static final short TLV_TYPE_AIR_SPEED		=0x000B; //空速
	public static final short TLV_TYPE_GND_SPEED		=0x000C; //地速
	public static final short TLV_TYPE_DIST				=0x000D; //from home
	public static final short TLV_TYPE_HEIGHT			=0x000E; //相对高度，height
	public static final short TLV_TYPE_CLIMB_RATE		=0x000F;
	public static final short TLV_TYPE_BATTERY1_VOLTAGE	=0x0010;
	public static final short TLV_TYPE_BATTERY1_CURRENT	=0x0011;
	public static final short TLV_TYPE_ORIE_X			=0x0012;
	public static final short TLV_TYPE_ORIE_Y			=0x0013;
	public static final short TLV_TYPE_ORIE_Z			=0x0014;
	public static final short TLV_TYPE_USER_A			=0x0015;//飞行器是否解锁
	public static final short TLV_TYPE_USER_B			=0x0016;//飞行模式
	public static final short TLV_TYPE_USER_C			=0x0017;//安卓网络方式:0->Unknown, 1->WiFi, 2->2G, 3->3G, 4->4G, 5->5G
	public static final short TLV_TYPE_OOO				=0x0018;
	public static final short TLV_TYPE_RC1				=0x0019;
	public static final short TLV_TYPE_RC2				=0x001A;
	public static final short TLV_TYPE_RC3				=0x001B;
	public static final short TLV_TYPE_RC4				=0x001C;
	public static final short TLV_TYPE_COUNT		=0x001D;

	public static final double TLV_VALUE_TIMES = 100000.0f;
	
	
	/* Definition of func_flags */
	public static final byte FUNC_FLAGS_AV			=0x01;
	public static final byte FUNC_FLAGS_VNC			=0x02;
	public static final byte FUNC_FLAGS_FT			=0x04;
	public static final byte FUNC_FLAGS_ADB			=0x08;
	public static final byte FUNC_FLAGS_WEBMONI		=0x10;
	public static final byte FUNC_FLAGS_SHELL		=0x20;
	public static final byte FUNC_FLAGS_HASROOT		=0x40;
	public static final byte FUNC_FLAGS_ACTIVATED	=(byte) 0x80;

	
	/* Definition of RF 315/433M */
	public static final int RF_315M = 315;
	public static final int RF_433M = 433;
	
	public static final int RF_CODEC_2262 = 2262;
	public static final int RF_CODEC_1527 = 1527;
	
	public static final int RF_R2262_4M7 = 44;
	public static final int RF_R2262_3M3 = 32;
	public static final int RF_R2262_2M0 = 20;
	public static final int RF_R2262_1M2 = 12;
	
	public static final int RF_R1527_330K = 32;
	
	public static final int RF0_TYPE_OFFSET = 10;
	
	public static final String str_rf_addr = "HFHFLHFX";//A7-A0, total:2187
	

	public static void MyMessageBox(Context context, String title, String msg)
	{
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(msg);
        builder.setPositiveButton(context.getResources().getString(R.string.ui_ok_btn),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                    }
                });
        builder.show();
	}
	
	public static void MyMessageTip(Context context, String msg)
	{
		Toast toast = Toast.makeText(context, msg, Toast.LENGTH_LONG);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
	}
	
    public static void setUint16Val(short val, byte[] buff, int offset)
    {
    	buff[offset] = (byte)((val >>> 8) & 0xff);
    	buff[offset + 1] = (byte)(val & 0xff);
    }
    
    public static void setUint32Val(int val, byte[] buff, int offset)
    {
    	buff[offset] = (byte)((val >>> 24) & 0xff);
    	buff[offset + 1] = (byte)((val >> 16) & 0xff);
    	buff[offset + 2] = (byte)((val >> 8) & 0xff);
    	buff[offset + 3] = (byte)(val & 0xff);
    }
    
    
	/////////////////////////////////////////////////////////////////////
	public static native int getAppVersion();
	public static native int getLowestLevelForAv();
	public static native int getLowestLevelForVnc();
	public static native String phpMd5(String strSrc);
	public static native int CtrlCmdHELLO(int type, int fhandle, String strPass/* EncPass */, int[] arrResults/* [0]:result_code [1]:dwServerVersion [2]:bFuncFlags */);
	public static native int CtrlCmdRUN(int type, int fhandle, String strCmd);
	public static native int CtrlCmdPROXY(int type, int fhandle, int wTcpPort);
	public static native int CtrlCmdARM(int type, int fhandle);
	public static native int CtrlCmdDISARM(int type, int fhandle);
	public static native int CtrlCmdAVSTART(int type, int fhandle, byte flags, byte video_size, byte video_framerate, int audio_channel, int video_channel);
	public static native int CtrlCmdAVSTOP(int type, int fhandle);
	public static native int CtrlCmdAVSWITCH(int type, int fhandle, int video_channel);
	public static native int CtrlCmdAVCONTRL(int type, int fhandle, int contrl, int contrl_param);
	//public static native int CtrlCmdVOICE(int type, int fhandle, byte[] data, int len);
	public static native int CtrlCmdBYE(int type, int fhandle);
	public static native int CtrlCmdSendNULL(int type, int fhandle);
	
	public static native void ProxyClientStartSlave(int wLocalTcpPort);
	public static native void ProxyClientStartProxy(int ftype, int fhandle, boolean bAutoClose, int wLocalTcpPort);
	public static native void ProxyClientAllQuit();
	public static native void ProxyClientClearQuitFlag();
	
	public static native void SendVoice(int type, int fhandle, byte[] data, int len);
	
	public static native void TLVRecvStart();
	public static native void TLVRecvStop();
	public static native void TLVRecvSetPeriod(int miniSec);
	public static native double[] GetSensorData();
	
	public static native void AudioRecvStart();
	public static native void AudioRecvStop();
	public static native byte[] AudioRecvGetData(int[] arrBreak);
	
	public static native void FF264RecvStart();
	public static native void FF264RecvStop();
	public static native void FF264RecvSetVflip();
	public static native int[] FF264RecvGetData(int[] arrBreak);
	
	public static native void FF263RecvStart();
	public static native void FF263RecvStop();
	public static native void FF263RecvSetVflip();
	public static native int[] FF263RecvGetData(int[] arrBreak);
}
