package com.wangling.remotephone;

import android.app.AlertDialog;
import android.app.TabActivity;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.Toast;
import com.wangling.remotephone.R;


public class SettingsActivity extends TabActivity {

	private SettingsActivity _instance = null;
	private int comments_id = 0;
	private TabHost mTabHost;
	
	private String[] alarmMethodArray = null;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.settings);
        
        Bundle extras = getIntent().getExtras();
        comments_id = extras.getInt("comments_id");
        
        _instance = this;
        
        //Stop Service...
        //Intent intent = new Intent(this, MobileCameraService.class);
    	//stopService(intent);
    	
        
        mTabHost = this.getTabHost();
        LayoutInflater.from(this).inflate(R.layout.settings, mTabHost.getTabContentView(), true);
        
        mTabHost.setBackgroundColor(Color.argb(255, 0x75, 0x75, 0x75));
        
        mTabHost.addTab(
        		mTabHost.newTabSpec("Tab-Basic")
        		.setIndicator(getResources().getString(R.string.ui_tab_basic), getResources().getDrawable(android.R.drawable.ic_menu_info_details))
        		.setContent(R.id.id_layout_tab_basic)
        		);
        
        mTabHost.addTab(
        		mTabHost.newTabSpec("Tab-Advanced")
        		.setIndicator(getResources().getString(R.string.ui_tab_advanced), getResources().getDrawable(android.R.drawable.ic_menu_info_details))
        		.setContent(R.id.id_layout_tab_advanced)
        		);
        
        mTabHost.addTab(
        		mTabHost.newTabSpec("Tab-Phone")
        		.setIndicator(getResources().getString(R.string.ui_tab_phone), getResources().getDrawable(android.R.drawable.ic_menu_call))
        		.setContent(R.id.id_layout_tab_phone)
        		);
        
        
        int tmpVal;
        Button btn = (Button)findViewById(R.id.id_btn_mobcam_id);
        if (null != btn) {
        	if (0 == comments_id) {
        		btn.setText(getResources().getString(R.string.ui_text_mobcam_id_0));
        	}
        	else {
        		btn.setText(String.format(getResources().getString(R.string.ui_text_mobcam_id_1), comments_id));
        	}
        	btn.setEnabled(false);
        }
        
        
        findViewById(R.id.id_checkbox_hide_ui).setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		if (0 == AppSettings.GetSoftwareKeyDwordValue(_instance, AppSettings.STRING_REGKEY_NAME_ALLOW_HIDE_UI, 1))
            	{
	        		if (((CheckBox)findViewById(R.id.id_checkbox_hide_ui)).isChecked())
	        		{
	        			SharedFuncLib.MyMessageBox(_instance, 
	        					getResources().getString(R.string.app_name), 
	            				getResources().getString(R.string.msg_level_too_low_for_allow_hide));
	        			//((CheckBox)findViewById(R.id.id_checkbox_hide_ui)).setChecked(false);
	        		}
            	}
        	}
        });
        
        findViewById(R.id.id_btn_search_bt).setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		BluetoothAdapter _bluetooth = BluetoothAdapter.getDefaultAdapter();
        		if (_bluetooth == null) {
    				SharedFuncLib.MyMessageTip(_instance, _instance.getResources().getString(R.string.msg_no_bluetooth_adapter));
    				return;
    			}
        		
        		if (!_bluetooth.isEnabled()) {
    				_bluetooth.enable();				
    				do {
    					try {
    						Thread.sleep(200);
    					} catch (InterruptedException e) {}
    				} while (!_bluetooth.isEnabled());
    			}
        		
        		/* Select device for list */
				Intent intent = new Intent(_instance, DiscoveryActivity.class);
				startActivityForResult(intent, REQUEST_DISCOVERY);
        	}
        });
        
        
        /* Tab basic */
        ((EditText)findViewById(R.id.id_edit_mobcam_name)).setText(AppSettings.GetSoftwareKeyValue(_instance, AppSettings.STRING_REGKEY_NAME_NODENAME, ""));
        ((EditText)findViewById(R.id.id_edit_mobcam_password)).setText(AppSettings.GetSoftwareKeyValue(_instance, AppSettings.STRING_REGKEY_NAME_PASSWORD, ""));
           
        tmpVal = AppSettings.GetSoftwareKeyDwordValue(_instance, AppSettings.STRING_REGKEY_NAME_AUTO_START, 1);
        ((CheckBox)findViewById(R.id.id_checkbox_auto_start)).setChecked(1 == tmpVal);
        ((CheckBox)findViewById(R.id.id_checkbox_auto_start)).setVisibility(View.GONE);////
        
        tmpVal = AppSettings.GetSoftwareKeyDwordValue(_instance, AppSettings.STRING_REGKEY_NAME_HIDE_UI, 0);
        ((CheckBox)findViewById(R.id.id_checkbox_hide_ui)).setChecked(1 == tmpVal);
        ((CheckBox)findViewById(R.id.id_checkbox_hide_ui)).setVisibility(View.GONE);////
        
        tmpVal = AppSettings.GetSoftwareKeyDwordValue(_instance, AppSettings.STRING_REGKEY_NAME_WITHUAV, 0);
        ((CheckBox)findViewById(R.id.id_checkbox_with_uav)).setChecked(1 == tmpVal);
        
        tmpVal = AppSettings.GetSoftwareKeyDwordValue(_instance, AppSettings.STRING_REGKEY_NAME_TAILSITTER, 0);
        ((CheckBox)findViewById(R.id.id_checkbox_with_tail_sitter)).setChecked(1 == tmpVal);
        
        tmpVal = AppSettings.GetSoftwareKeyDwordValue(_instance, AppSettings.STRING_REGKEY_NAME_TAILSITTER_SW_GPSALTI, 0);
        ((EditText)findViewById(R.id.id_edit_sw_gpsalti)).setText(String.format("%d", tmpVal));
        
        tmpVal = AppSettings.GetSoftwareKeyDwordValue(_instance, AppSettings.STRING_REGKEY_NAME_TAILSITTER_SW_GNDSPEED, 0);
        ((EditText)findViewById(R.id.id_edit_sw_gndspeed)).setText(String.format("%d", tmpVal));
        
        
        String []capMethodArray = new String[4];
        capMethodArray[0] = getResources().getString(R.string.ui_text_cap_method_auto);
        capMethodArray[1] = getResources().getString(R.string.ui_text_cap_method_adb);
        capMethodArray[2] = getResources().getString(R.string.ui_text_cap_method_flinger);
        capMethodArray[3] = getResources().getString(R.string.ui_text_cap_method_fb);
        
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, capMethodArray);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); 
        
        Spinner spinnerCap = (Spinner)findViewById(R.id.id_spinner_cap_method);
        spinnerCap.setAdapter(adapter);
        spinnerCap.setSelection( AppSettings.GetSoftwareKeyDwordValue(_instance, AppSettings.STRING_REGKEY_NAME_CAPMETHOD, 0) );
        
        
        int video_enc = AppSettings.GetSoftwareKeyDwordValue(_instance, AppSettings.STRING_REGKEY_NAME_VIDEOENC, 0);
        switch (video_enc)
    	{
    	case 0://none
    		((RadioGroup)findViewById(R.id.radiogroup_video_enc)).check(R.id.radio_video_enc_a);
    		break;
    	case 1://common ha
    		((RadioGroup)findViewById(R.id.radiogroup_video_enc)).check(R.id.radio_video_enc_b);
    		break;
    	case 2://
    		((RadioGroup)findViewById(R.id.radiogroup_video_enc)).check(R.id.radio_video_enc_c);
    		break;
    	default:
    		((RadioGroup)findViewById(R.id.radiogroup_video_enc)).check(R.id.radio_video_enc_a);
    		break;
    	}
        
        tmpVal = AppSettings.GetSoftwareKeyDwordValue(_instance, AppSettings.STRING_REGKEY_NAME_VIDEOUV, 0);
        ((CheckBox)findViewById(R.id.id_checkbox_video_uv)).setChecked(1 == tmpVal);
        
        
        /* Tab advanced */
        String bt_addr = AppSettings.GetSoftwareKeyValue(_instance, AppSettings.STRING_REGKEY_NAME_BT_ADDRESS, "");
        if (bt_addr.equals("")) {
        	((EditText)findViewById(R.id.id_edit_bt_addr)).setText(_instance.getResources().getString(R.string.ui_text_no_bt_addr));
        }
        else {
        	((EditText)findViewById(R.id.id_edit_bt_addr)).setText(bt_addr);
        }
        
        
        ((EditText)findViewById(R.id.id_edit_serial_port_dev)).setText(AppSettings.GetSoftwareKeyValue(_instance, AppSettings.STRING_REGKEY_NAME_SERIAL_PORT, ""));
        
        ((EditText)findViewById(R.id.id_edit_qx_app_key)).setText(AppSettings.GetSoftwareKeyValue(_instance, AppSettings.STRING_REGKEY_NAME_QX_APP_KEY, MobileCameraService.APP_KEY_default));
        ((EditText)findViewById(R.id.id_edit_qx_app_secret)).setText(AppSettings.GetSoftwareKeyValue(_instance, AppSettings.STRING_REGKEY_NAME_QX_APP_SECRET, MobileCameraService.APP_SECRET_default));
        
        
        tmpVal = AppSettings.GetSoftwareKeyDwordValue(_instance, AppSettings.STRING_REGKEY_NAME_RED_ALARM_ENABLED, 0);
        ((CheckBox)findViewById(R.id.id_checkbox_enable_red_alarm)).setChecked(1 == tmpVal);
        
        
        alarmMethodArray = new String[2];
        alarmMethodArray[0] = getResources().getString(R.string.ui_select_method_email);
        alarmMethodArray[1] = getResources().getString(R.string.ui_select_method_sms);
        
        ArrayAdapter<String> adapter0 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, alarmMethodArray);
        adapter0.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); 
        
        Spinner spinner0 = (Spinner)findViewById(R.id.id_spinner_red_alarm_method);
        spinner0.setAdapter(adapter0);
        spinner0.setSelection( AppSettings.GetSoftwareKeyDwordValue(_instance, AppSettings.STRING_REGKEY_NAME_RED_ALARM_METHOD, 0) );

        
        tmpVal = AppSettings.GetSoftwareKeyDwordValue(_instance, AppSettings.STRING_REGKEY_NAME_SR_ENABLED, 0);
        ((CheckBox)findViewById(R.id.id_checkbox_enable_sr)).setChecked(1 == tmpVal);
        
        
        /* Tab phone */
        tmpVal = AppSettings.GetSoftwareKeyDwordValue(_instance, AppSettings.STRING_REGKEY_NAME_ENABLE_EMAIL, 1);
        ((CheckBox)findViewById(R.id.id_checkbox_enable_email)).setChecked(1 == tmpVal);
        ((CheckBox)findViewById(R.id.id_checkbox_enable_email)).setVisibility(View.GONE);////
        
        ((EditText)findViewById(R.id.id_edit_email_address)).setText(AppSettings.GetSoftwareKeyValue(_instance, AppSettings.STRING_REGKEY_NAME_EMAILADDRESS, ""));
        ((EditText)findViewById(R.id.id_edit_sms_phone_num)).setText(AppSettings.GetSoftwareKeyValue(_instance, AppSettings.STRING_REGKEY_NAME_SMSPHONENUM, ""));
    }
	
    
    private void saveSettings()
    {
    	int tmpVal;
    	int tmpId;
    	
    	/* Tab basic */
    	AppSettings.SaveSoftwareKeyValue(_instance, AppSettings.STRING_REGKEY_NAME_NODENAME, ((EditText)findViewById(R.id.id_edit_mobcam_name)).getText().toString());
    	AppSettings.SaveSoftwareKeyValue(_instance, AppSettings.STRING_REGKEY_NAME_PASSWORD, ((EditText)findViewById(R.id.id_edit_mobcam_password)).getText().toString());
    	
    	tmpVal = ((CheckBox)findViewById(R.id.id_checkbox_auto_start)).isChecked() ? 1 : 0;
    	AppSettings.SaveSoftwareKeyDwordValue(_instance, AppSettings.STRING_REGKEY_NAME_AUTO_START, tmpVal);
    	
    	tmpVal = ((CheckBox)findViewById(R.id.id_checkbox_hide_ui)).isChecked() ? 1 : 0;
    	AppSettings.SaveSoftwareKeyDwordValue(_instance, AppSettings.STRING_REGKEY_NAME_HIDE_UI, tmpVal);
    	
    	
    	Spinner spinnerCap = (Spinner)findViewById(R.id.id_spinner_cap_method);
        AppSettings.SaveSoftwareKeyDwordValue(_instance, AppSettings.STRING_REGKEY_NAME_CAPMETHOD, spinnerCap.getSelectedItemPosition() );
    	
    	
    	tmpVal = ((CheckBox)findViewById(R.id.id_checkbox_with_uav)).isChecked() ? 1 : 0;
    	AppSettings.SaveSoftwareKeyDwordValue(_instance, AppSettings.STRING_REGKEY_NAME_WITHUAV, tmpVal);
    	
    	tmpVal = ((CheckBox)findViewById(R.id.id_checkbox_with_tail_sitter)).isChecked() ? 1 : 0;
    	AppSettings.SaveSoftwareKeyDwordValue(_instance, AppSettings.STRING_REGKEY_NAME_TAILSITTER, tmpVal);
    	
    	tmpVal = -1;
    	try {
    		tmpVal = Integer.parseInt( ((EditText)findViewById(R.id.id_edit_sw_gpsalti)).getText().toString() );
    	} catch (Exception e) {
    		tmpVal = -1;
    	}
    	if (tmpVal > 0) {
    		AppSettings.SaveSoftwareKeyDwordValue(_instance, AppSettings.STRING_REGKEY_NAME_TAILSITTER_SW_GPSALTI, tmpVal);
    	}
    	
    	tmpVal = -1;
    	try {
    		tmpVal = Integer.parseInt( ((EditText)findViewById(R.id.id_edit_sw_gndspeed)).getText().toString() );
    	} catch (Exception e) {
    		tmpVal = -1;
    	}
    	if (tmpVal > 0) {
    		AppSettings.SaveSoftwareKeyDwordValue(_instance, AppSettings.STRING_REGKEY_NAME_TAILSITTER_SW_GNDSPEED, tmpVal);
    	}
    	
    	
    	tmpId = ((RadioGroup)findViewById(R.id.radiogroup_video_enc)).getCheckedRadioButtonId();
    	switch (tmpId)
    	{
    	case R.id.radio_video_enc_a:
    		AppSettings.SaveSoftwareKeyDwordValue(_instance, AppSettings.STRING_REGKEY_NAME_VIDEOENC, 0);
    		break;
    	case R.id.radio_video_enc_b:
    		AppSettings.SaveSoftwareKeyDwordValue(_instance, AppSettings.STRING_REGKEY_NAME_VIDEOENC, 1);
    		break;
    	case R.id.radio_video_enc_c:
    		AppSettings.SaveSoftwareKeyDwordValue(_instance, AppSettings.STRING_REGKEY_NAME_VIDEOENC, 2);
    		break;
    	}
    	
    	tmpVal = ((CheckBox)findViewById(R.id.id_checkbox_video_uv)).isChecked() ? 1 : 0;
    	AppSettings.SaveSoftwareKeyDwordValue(_instance, AppSettings.STRING_REGKEY_NAME_VIDEOUV, tmpVal);
    	
    	
    	/* Tab advanced */
    	AppSettings.SaveSoftwareKeyValue(_instance, AppSettings.STRING_REGKEY_NAME_BT_ADDRESS, ((EditText)findViewById(R.id.id_edit_bt_addr)).getText().toString());
    	
    	AppSettings.SaveSoftwareKeyValue(_instance, AppSettings.STRING_REGKEY_NAME_SERIAL_PORT, ((EditText)findViewById(R.id.id_edit_serial_port_dev)).getText().toString());
    	
    	AppSettings.SaveSoftwareKeyValue(_instance, AppSettings.STRING_REGKEY_NAME_QX_APP_KEY, ((EditText)findViewById(R.id.id_edit_qx_app_key)).getText().toString());
    	AppSettings.SaveSoftwareKeyValue(_instance, AppSettings.STRING_REGKEY_NAME_QX_APP_SECRET, ((EditText)findViewById(R.id.id_edit_qx_app_secret)).getText().toString());
    	
    	tmpVal = ((CheckBox)findViewById(R.id.id_checkbox_enable_red_alarm)).isChecked() ? 1 : 0;
    	AppSettings.SaveSoftwareKeyDwordValue(_instance, AppSettings.STRING_REGKEY_NAME_RED_ALARM_ENABLED, tmpVal);
    	
    	Spinner spinner0 = (Spinner)findViewById(R.id.id_spinner_red_alarm_method);
        AppSettings.SaveSoftwareKeyDwordValue(_instance, AppSettings.STRING_REGKEY_NAME_RED_ALARM_METHOD, spinner0.getSelectedItemPosition() );
    	
    	tmpVal = ((CheckBox)findViewById(R.id.id_checkbox_enable_sr)).isChecked() ? 1 : 0;
    	AppSettings.SaveSoftwareKeyDwordValue(_instance, AppSettings.STRING_REGKEY_NAME_SR_ENABLED, tmpVal);
    	
    	
    	/* Tab phone */
    	tmpVal = ((CheckBox)findViewById(R.id.id_checkbox_enable_email)).isChecked() ? 1 : 0;
    	AppSettings.SaveSoftwareKeyDwordValue(_instance, AppSettings.STRING_REGKEY_NAME_ENABLE_EMAIL, tmpVal);
    	
    	AppSettings.SaveSoftwareKeyValue(_instance, AppSettings.STRING_REGKEY_NAME_EMAILADDRESS, ((EditText)findViewById(R.id.id_edit_email_address)).getText().toString());
    	AppSettings.SaveSoftwareKeyValue(_instance, AppSettings.STRING_REGKEY_NAME_SMSPHONENUM, ((EditText)findViewById(R.id.id_edit_sms_phone_num)).getText().toString());
    	
    	
    	//Start service...
    	//Intent intent = new Intent(this, MobileCameraService.class);
    	//startService(intent);
    }
    
    
    static final int REQUEST_DISCOVERY = 0x01010101;
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
    	if (requestCode == REQUEST_DISCOVERY && resultCode == RESULT_OK) {
    		String bt_addr = AppSettings.GetSoftwareKeyValue(_instance, AppSettings.STRING_REGKEY_NAME_BT_ADDRESS, "");
            if (bt_addr.equals("")) {
            	((EditText)findViewById(R.id.id_edit_bt_addr)).setText(_instance.getResources().getString(R.string.ui_text_no_bt_addr));
            }
            else {
            	((EditText)findViewById(R.id.id_edit_bt_addr)).setText(bt_addr);
            }
		}
    }
    
    
    @Override  
    public boolean onKeyDown(int keyCode, KeyEvent event) {   
           
        if(keyCode == KeyEvent.KEYCODE_BACK){   

            AlertDialog.Builder builder = new AlertDialog.Builder(_instance);
            builder.setTitle(_instance.getResources().getString(R.string.app_name));
            builder.setMessage(_instance.getResources().getString(R.string.msg_save_settings_or_not));
            builder.setPositiveButton(_instance.getResources().getString(R.string.ui_yes_btn),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            //setTitle("点击了对话框上的Button1");
                        	
                        	saveSettings();                        	
                        	dialog.dismiss();
                        	_instance.finish();
                        }
                    });
            builder.setNegativeButton(_instance.getResources().getString(R.string.ui_no_btn),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            //setTitle("点击了对话框上的Button3");
                        	
                        	dialog.dismiss();
                        	_instance.finish();
                        }
                    });
            builder.show();
        	
            return true;   
        }else{         
            return super.onKeyDown(keyCode, event);
        }   
    }
}

