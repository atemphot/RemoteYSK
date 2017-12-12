package com.wangling.remotephone;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import com.wangling.remotephone.R;


public class AppSettings {

	private static final String fileName = "remotephone_settings";
	
	
	/* For MobCam */
	public static final String STRING_REGKEY_NAME_USB_ROOT_INSTALL = "UsbRootInstall";//int:0,1  ReadOnly
	public static final String STRING_REGKEY_NAME_AUTO_START = "AutoStart";//int:0,1
	public static final String STRING_REGKEY_NAME_HIDE_UI = "HideUi";//int:0,1
	public static final String STRING_REGKEY_NAME_CAMID = "CamId";//int:val
	public static final String STRING_REGKEY_NAME_NODENAME = "NodeName";
	public static final String STRING_REGKEY_NAME_NODEID = "NodeId";
	public static final String STRING_REGKEY_NAME_PASSWORD = "Password";
	public static final String STRING_REGKEY_NAME_CAPMETHOD = "CapMethod";//int:0,1,2,3
	public static final String STRING_REGKEY_NAME_PICROTATION = "PicRotation";//int:0,90,180
	public static final String STRING_REGKEY_NAME_ALLOW_HIDE_UI = "AllowHideUi";//int:0,1
	public static final String STRING_REGKEY_NAME_SAVED_MAC = "SavedMac";
	
	public static final String STRING_REGKEY_NAME_LAST_ONLINE_TIME = "LastOnlineTime";//long
	public static final String STRING_REGKEY_NAME_LAST_OUT_CALL_TIME = "LastOutCallTime";//long
	public static final String STRING_REGKEY_NAME_LAST_OUT_SMS_TIME  = "LastOutSmsTime";//long
	public static final String STRING_REGKEY_NAME_LAST_IN_CALL_TIME = "LastInCallTime";//long
	public static final String STRING_REGKEY_NAME_LAST_IN_SMS_TIME  = "LastInSmsTime";//long
	
	/* For alarm settings */
	public static final String STRING_REGKEY_NAME_ENABLE_EMAIL = "EnableEmail";//int:0,1
	public static final String STRING_REGKEY_NAME_EMAILADDRESS = "EmailAddress";
	public static final String STRING_REGKEY_NAME_SMSPHONENUM = "SMSPhoneNum";
	
	public static final String STRING_REGKEY_NAME_RED_ALARM_ENABLED = "RedAlarmEnabled";//int:0,1
	public static final String STRING_REGKEY_NAME_RED_ALARM_METHOD = "RedAlarmMethod";//int:def_level
	
	public static final String STRING_REGKEY_NAME_SR_ENABLED = "SREnabled";//int:0,1
	
	public static final String STRING_REGKEY_NAME_WITHUAV = "WithUAV";//int:0,1
	public static final String STRING_REGKEY_NAME_TAILSITTER = "TailSitter";//int:0,1
	public static final String STRING_REGKEY_NAME_TAILSITTER_SW_GPSALTI = "SW_GPSALTI";//int Ã×
	public static final String STRING_REGKEY_NAME_TAILSITTER_SW_GNDSPEED = "SW_GNDSPEED";//int ÀåÃ×Ã¿Ãë
	
	public static final String STRING_REGKEY_NAME_VIDEOENC = "VideoEnc";//int:0,1,2
	public static final String STRING_REGKEY_NAME_VIDEOUV = "VideoUV";//int:0,1
	
	public static final String STRING_REGKEY_NAME_BT_ADDRESS = "BtAddress";
	public static final String STRING_REGKEY_NAME_SERIAL_PORT = "SerialPort";//String:/dev/ttyS4
	public static final String STRING_REGKEY_NAME_QX_APP_KEY = "QX_AppKey";
	public static final String STRING_REGKEY_NAME_QX_APP_SECRET = "QX_AppSecret";
	
	
	public static String GetSoftwareKeyValue(Context context, String keyName, String defValue)
	{
		SharedPreferences preferences = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
		return preferences.getString(keyName, defValue);
	}
	
	public static int GetSoftwareKeyDwordValue(Context context, String keyName, int defValue)
	{
		SharedPreferences preferences = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
		return preferences.getInt(keyName, defValue);
	}
	
	public static long GetSoftwareKeyLongValue(Context context, String keyName, long defValue)
	{
		SharedPreferences preferences = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
		return preferences.getLong(keyName, defValue);
	}
	
	public static void SaveSoftwareKeyValue(Context context, String keyName, String value)
	{
		SharedPreferences preferences = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
		Editor editor = preferences.edit();
		editor.putString(keyName, value);
		editor.commit();
	}
	
	public static void SaveSoftwareKeyDwordValue(Context context, String keyName, int value)
	{
		SharedPreferences preferences = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
		Editor editor = preferences.edit();
		editor.putInt(keyName, value);
		editor.commit();
	}
	
	public static void SaveSoftwareKeyLongValue(Context context, String keyName, long value)
	{
		SharedPreferences preferences = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
		Editor editor = preferences.edit();
		editor.putLong(keyName, value);
		editor.commit();
	}
}
