package com.wangling.remotephone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.wangling.remotephone.R;

public class BootUpReceiver extends BroadcastReceiver
{
        public void onReceive(Context context, Intent intent)
        {
        	if (true == intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
        		AppSettings.SaveSoftwareKeyLongValue(context, AppSettings.STRING_REGKEY_NAME_LAST_ONLINE_TIME, 0);
        	}
        	
        	int tmpVal = AppSettings.GetSoftwareKeyDwordValue(context, AppSettings.STRING_REGKEY_NAME_AUTO_START, 1);
        	if (1 == tmpVal)
        	{
	            Intent i = new Intent(context, MobileCameraService.class);
	            context.startService(i);
        	}
        }
}
