package org.droidplanner.connection;


import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;

import com.wangling.remotephone.SerialPort;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;

public class SerialConnection extends MAVLinkConnection {

	private Context mContext = null;
	private Handler mMainHandler = null;
	private SerialPort mSerialPort = null;
	private InputStream mInput = null;
	private OutputStream mOutput = null;

	public SerialConnection(Context context, Handler mainHandler, SerialPort serialPort) {
		super(context);
		
		mContext = context;
		mMainHandler = mainHandler;
		mSerialPort = serialPort;
	}

	@Override
	protected void openConnection() throws IOException {
		if (mSerialPort == null) {
			throw new IOException();
		}
		mInput = mSerialPort.getInputStream();
		mOutput = mSerialPort.getOutputStream();
	}

	@Override
	protected void readDataBlock() throws IOException {
		int ret = 0;
		try {
			ret = 0;
			ret = mInput.read(readData, 0, readData.length);
		} catch (InterruptedIOException e) {
			if (ret < 0) {
				ret = 0;
			}
		}
		if (ret > 0)
		{
			iavailable = ret;
		}
		else {
			iavailable = 0;
			try {Thread.sleep(2);}catch (Exception e) {}
		}
	}

	@Override
	protected void sendBuffer(byte[] buffer) throws IOException {
		if (buffer.length <= 256) {
			mOutput.write(buffer, 0, buffer.length);
		}
		else
		{
			int remain = buffer.length;
			int toSend = 0;
			do {
				toSend = (remain > 256 ? 256 : remain);
				mOutput.write(buffer, buffer.length - remain, toSend);
				remain -= toSend;
				try {Thread.sleep(2);}catch (Exception e) {}
			} while (remain > 0);
		}
	}

	@Override
	protected void closeConnection() throws IOException {
		mInput.close();
		mOutput.close();
		mSerialPort.close();
		mSerialPort = null;
	}

	@Override
	protected void getPreferences(SharedPreferences prefs) {
	}
}
