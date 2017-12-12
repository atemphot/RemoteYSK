package com.wangling.remotephone;

import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;


public class DiscoveryActivity  extends ListActivity
{
	private Handler _handler = new Handler();
	/* Get Default Adapter */
	private BluetoothAdapter _bluetooth = BluetoothAdapter.getDefaultAdapter();
	/* Storage the BT devices */
	private List<BluetoothDevice> _devices = new ArrayList<BluetoothDevice>();
	/* Discovery is Finished */
	private volatile boolean _discoveryFinished = false;
	
	private Runnable _discoveryWorkder = new Runnable() {
		public void run() 
		{
			/* Start search device */
			_bluetooth.startDiscovery();
			Log.d("EF-BTBee", ">>Starting Discovery");
			for (;;) 
			{
				if (_discoveryFinished) 
				{
					Log.d("EF-BTBee", ">>Finished");
					break;
				}
				try 
				{
					Thread.sleep(200);
				} catch (InterruptedException e){}
			}
		}
	};
	
	final Runnable auto_select_runnable = new Runnable() {
		public void run()
		{
			try
			{
				int count = _devices.size();
				if (count > 0)
				{
					String saved = AppSettings.GetSoftwareKeyValue(getBaseContext(), AppSettings.STRING_REGKEY_NAME_BT_ADDRESS, "");
					for (int i = 0; i < count; i++)
					{
						if (true == _devices.get(i).getAddress().equalsIgnoreCase(saved))
						{
							Log.d("EF-BTBee", ">>Auto select device");
							Intent result = new Intent();
							result.putExtra(BluetoothDevice.EXTRA_DEVICE, _devices.get(i));
							setResult(RESULT_OK, result);
							_devices.clear();
							finish();
							return;
						}
					}
				}
				
				onBtnBtSearch();
				
			} catch (Exception e) {e.printStackTrace();}
		}
	};
	
	/**
	 * Receiver
	 * When the discovery finished be called.
	 */
	private BroadcastReceiver _foundReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			/* get the search results */
			BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
			/* add to list */
			_devices.add(device);
			/* show the devices list */
			showDevices();
		}
	};
	private BroadcastReceiver _discoveryReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent)  
		{
			/* unRegister Receiver */
			Log.d("EF-BTBee", ">>unregisterReceiver");
			unregisterReceiver(_foundReceiver);
			unregisterReceiver(_discoveryReceiver);
			_discoveryFinished = true;
			
			_handler.removeCallbacks(auto_select_runnable);
			_handler.postDelayed(auto_select_runnable, 5000);
			
			((Button)findViewById(R.id.bt_search_btn)).setEnabled(true);
		}
	};
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND, WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
		setContentView(R.layout.discovery);
		
		/* BT isEnable */
		while (!_bluetooth.isEnabled())
		{
			Log.w("EF-BTBee", ">>BTBee is disabled, wait...");
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {}
		}
		
		findViewById(R.id.bt_search_btn).setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		onBtnBtSearch();
        	}
        });
		
		/* show a dialog "Scanning..." */
		onBtnBtSearch();
	}

	/* Show devices list */
	protected void showDevices()
	{
		List<String> list = new ArrayList<String>();
		if(_devices.size() > 0)
		{	
			for (int i = 0, size = _devices.size(); i < size; ++i)
			{
				StringBuilder b = new StringBuilder();
				BluetoothDevice d = _devices.get(i);
				b.append(d.getAddress());
				b.append('\n');
				b.append(d.getName());
				String s = b.toString();
				list.add(s);
			}
		}
		Log.d("EF-BTBee", ">>showDevices");
		final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, list);
		_handler.post(new Runnable() {
			public void run()
			{
				setListAdapter(adapter);
			}
		});
	}
	
	/* Select device */
	protected void onListItemClick(ListView l, View v, int position, long id)
	{
		_handler.removeCallbacks(auto_select_runnable);
		AppSettings.SaveSoftwareKeyValue(getBaseContext(), AppSettings.STRING_REGKEY_NAME_BT_ADDRESS, _devices.get(position).getAddress());
		
		Log.d("EF-BTBee", ">>Click device");
		Intent result = new Intent();
		result.putExtra(BluetoothDevice.EXTRA_DEVICE, _devices.get(position));
		setResult(RESULT_OK, result);
		_devices.clear();
		finish();
	}
	
	private void onBtnBtSearch()
	{
		_handler.removeCallbacks(auto_select_runnable);
		
		((Button)findViewById(R.id.bt_search_btn)).setEnabled(false);
		
		/* Register Receiver*/
		IntentFilter discoveryFilter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		registerReceiver(_discoveryReceiver, discoveryFilter);
		IntentFilter foundFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		registerReceiver(_foundReceiver, foundFilter);
		
		_discoveryFinished = false;
		
		_devices.clear();
		showDevices();
		
		SamplesUtils.indeterminate(DiscoveryActivity.this, _handler, "Bluetooth Scanning...", _discoveryWorkder, new OnDismissListener() {
			public void onDismiss(DialogInterface dialog)
			{

				for (; _bluetooth.isDiscovering();)
				{
					_bluetooth.cancelDiscovery();
				}
				
				_discoveryFinished = true;
				
				((Button)findViewById(R.id.bt_search_btn)).setEnabled(true);
			}
		}, true);
	}
}

