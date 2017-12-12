package com.wangling.remotephone;

import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;

import com.wangling.remotephone.R;

public class SmsComeReceiver extends BroadcastReceiver
{
	private static final String TAG = "SmsComeReceiver";
	
	
	private void delete_the_sms(Context context, String remote_num, String content)
	{
		try {
			context.getContentResolver().delete(Uri.parse("content://sms"), "body=\"" + content + "\"", null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
    private boolean isAuthPhone(Context context, String incomingNumber)
    {
    	boolean bAuthPhone = false;
		String smsPhoneNum = AppSettings.GetSoftwareKeyValue(context, AppSettings.STRING_REGKEY_NAME_SMSPHONENUM, "");
		String phoneNum = smsPhoneNum;
		phoneNum = phoneNum.replace(" ", ";");
		phoneNum = phoneNum.replace(",", ";");
		String[] numArray = phoneNum.split(";");
		for (int i = 0; i < numArray.length; i++)
	    {
	    	numArray[i] = numArray[i].trim();
	    	if (false == numArray[i].equals("")) {
	    		if (incomingNumber.equals(numArray[i]) || 
	    				( incomingNumber.startsWith("+") && incomingNumber.contains(numArray[i]) ) ||
	    				( numArray[i].startsWith("+")    && numArray[i].contains(incomingNumber) )     ) {
	    			bAuthPhone = true;
	    			break;
	    		}
	    	}
	    }
		return bAuthPhone;
    }
    
    private void sendSMS(Context context, String phoneNumber, String message)
    {   
    	phoneNumber = phoneNumber.replace(" ", ";");
    	phoneNumber = phoneNumber.replace(",", ";");
        String[] numArray = phoneNumber.split(";");
    	
	    // ---sends an SMS message to another device---   
	    SmsManager sms = SmsManager.getDefault();   
	  
	    for (int i = 0; i < numArray.length; i++)
	    {
	    	numArray[i] = numArray[i].trim();
	    	if (numArray[i].equals("")) {
	    		continue;
	    	}
	    	
	        ArrayList<String> msgs = sms.divideMessage(message);   
	        for (String msg : msgs) {   
	        	Log.d(TAG, "sms.sendTextMessage(" + numArray[i] + ", " + msg + ")...");
	        	sms.sendTextMessage(numArray[i], null, msg, null, null);
	        	delete_the_sms(context, numArray[i], msg);
	        }     
	    }
    }
    
    private String GenerateLocationMsg(Context context)
    {
    	LocationManager locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
        boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        
        if (gpsEnabled)
        {
        	Location lastLoc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
    		if (null != lastLoc)
    		{
    			Log.d(TAG, "GPS_PROVIDER " + lastLoc.toString());
    			String strGPSLongi = String.format("%.4f", lastLoc.getLongitude());
    			String strGPSLati  = String.format("%.4f", lastLoc.getLatitude());
            	return String.format(context.getResources().getString(R.string.msg_sms_location_format), "http://ykz.e2eye.com/LocMap.php?lati=" + strGPSLati + "&longi=" + strGPSLongi);
    		}
        }
        
        if (networkEnabled)
        {
	    	Location lastLoc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			if (null != lastLoc)
			{
				Log.d(TAG, "NETWORK_PROVIDER " + lastLoc.toString());
				String strGPSLongi = String.format("%.4f", lastLoc.getLongitude());
				String strGPSLati  = String.format("%.4f", lastLoc.getLatitude());
	        	return String.format(context.getResources().getString(R.string.msg_sms_location_format), "http://ykz.e2eye.com/LocMap.php?lati=" + strGPSLati + "&longi=" + strGPSLongi);
			}
        }
        
        return "http://ykz.e2eye.com/cloudctrl/LocationMap.php";
    }
    
    public void onReceive(Context context, Intent intent)
    {
    	int tmpVal = AppSettings.GetSoftwareKeyDwordValue(context, AppSettings.STRING_REGKEY_NAME_AUTO_START, 1);
    	if (1 == tmpVal)
    	{
	        Intent i = new Intent(context, MobileCameraService.class);
	        context.startService(i);
    	}
        
        Object[] pdus = (Object[])(intent.getExtras().get("pdus"));//获取短信内容
        for(Object pdu : pdus){
            byte[] data = (byte[])pdu;//获取单条短信内容，短信内容以pdu格式存在
            
            SmsMessage message = null;
            String sender = "";
            String content = "";
            try {
	            message = SmsMessage.createFromPdu(data);//使用pdu格式的短信数据生成短信对象
	            sender = message.getOriginatingAddress();//获取短信的发送者
	            content = message.getMessageBody();//获取短信的内容
            } catch (Exception e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    			continue;
    		}
            
            Log.d("SmsComeReceiver", "Recv SMS:<" + sender + ">" + content);
            
            if (isAuthPhone(context, sender))
    		{
            	abortBroadcast();
            	
            	content = content.trim();
            	
				if (content.equalsIgnoreCase(context.getResources().getString(R.string.msg_sms_cmd_location)))
				{
					try {
						sendSMS(context, sender, GenerateLocationMsg(context));
					} catch (Exception e) {
		    			// TODO Auto-generated catch block
		    			e.printStackTrace();
		    		}
				}
				
				delete_the_sms(context, sender, content);
    		}
        }//for
    }
}
