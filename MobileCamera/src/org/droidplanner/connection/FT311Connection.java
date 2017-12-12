package org.droidplanner.connection;


import java.io.IOException;

import com.wangling.remotephone.FT311UARTInterface;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;

public class FT311Connection extends MAVLinkConnection {

	private Context mContext = null;
	private Handler mMainHandler = null;
	private FT311UARTInterface mFT311Uart = null;

	public FT311Connection(Context context, Handler mainHandler, FT311UARTInterface ft311Uart) {
		super(context);
		
		mContext = context;
		mMainHandler = mainHandler;
		mFT311Uart = ft311Uart;
	}

	@Override
	protected void openConnection() throws IOException {
		mMainHandler.post(new Runnable(){
			@Override
			public void run() {
				mFT311Uart.ResumeAccessory();
				mFT311Uart.ResetBuffer();
			}
		});
		try {Thread.sleep(200);}catch (Exception e) {}
		mFT311Uart.SetConfig(57600,(byte)8,(byte)1,(byte)0,(byte)0);
	}

	@Override
	protected void readDataBlock() throws IOException {
		int[] actualNumBytes = new int[1];
		actualNumBytes[0] = 0;
		byte status = mFT311Uart.ReadData(readData.length, readData, actualNumBytes);
		if (status == 0x00 && actualNumBytes[0] > 0)
		{
			iavailable = actualNumBytes[0];
		}
		else {
			iavailable = 0;
			try {Thread.sleep(2);}catch (Exception e) {}
		}
	}

	@Override
	protected void sendBuffer(byte[] buffer) throws IOException {
		if (buffer.length <= 256) {
			mFT311Uart.SendDataEx(buffer.length, 0, buffer);
		}
		else
		{
			int remain = buffer.length;
			int toSend = 0;
			do {
				toSend = (remain > 256 ? 256 : remain);
				mFT311Uart.SendDataEx(toSend, buffer.length - remain, buffer);
				remain -= toSend;
				try {Thread.sleep(2);}catch (Exception e) {}
			} while (remain > 0);
		}
	}

	@Override
	protected void closeConnection() throws IOException {
		mMainHandler.post(new Runnable(){
			@Override
			public void run() {
				mFT311Uart.DestroyAccessory(true);
			}
		});
	}

	@Override
	protected void getPreferences(SharedPreferences prefs) {
	}
}
