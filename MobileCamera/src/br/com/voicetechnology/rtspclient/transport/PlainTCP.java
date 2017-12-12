/*
   Copyright 2010 Voice Technology Ind. e Com. Ltda.
 
   This file is part of RTSPClientLib.

    RTSPClientLib is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    RTSPClientLib is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with RTSPClientLib.  If not, see <http://www.gnu.org/licenses/>.

*/
package br.com.voicetechnology.rtspclient.transport;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URI;

import br.com.voicetechnology.rtspclient.MissingHeaderException;
import br.com.voicetechnology.rtspclient.concepts.Message;
import br.com.voicetechnology.rtspclient.concepts.Transport;
import br.com.voicetechnology.rtspclient.concepts.TransportListener;

class TransportThread extends Thread
{
	private final PlainTCP transport;

	private volatile SafeTransportListener listener;

	public TransportThread(PlainTCP transport, TransportListener listener)
	{
		this.transport = transport;
		this.listener = new SafeTransportListener(listener);
	}

	public SafeTransportListener getListener()
	{
		return listener;
	}

	public void setListener(TransportListener listener)
	{
		listener = new SafeTransportListener(listener);
	}

	@Override
	public void run()
	{
		listener.connected(transport);
		byte[] buffer = new byte[2048];
		int read = -1;
		while(transport.isConnected())
		{
			try
			{
				read = transport.receive(buffer);
				if(read == -1)
				{
					transport.setConnected(false);
					listener.remoteDisconnection(transport);
				} else
					listener.dataReceived(transport, buffer, read);
			} catch(IOException e)
			{
				listener.error(transport, e);
			}
		}
	}
}

public class PlainTCP implements Transport
{
	private Socket socket;

	private TransportThread thread;

	private TransportListener transportListener;

	private volatile boolean connected;

	public PlainTCP()
	{
	}

	public void connect(URI to) throws IOException
	{
		if(connected)
			throw new IllegalStateException("Socket is still open. Close it first");
		int port = to.getPort();
		if(port == -1) port = 554;
		InetAddress serverAddr = InetAddress.getByName(to.getHost().toString());
		socket = new Socket(serverAddr, port);
		setConnected(true);
		thread = new TransportThread(this, transportListener);
		thread.start();
	}

	public void disconnect()
	{
		setConnected(false);
		try
		{
			socket.close();
		} catch(IOException e)
		{
		}
	}

	public boolean isConnected()
	{
		return connected;
	}

	public synchronized void sendMessage(Message message) throws IOException,
			MissingHeaderException
	{
		socket.getOutputStream().write(message.getBytes());
		thread.getListener().dataSent(this);
	}

	public void setTransportListener(TransportListener listener)
	{
		transportListener = listener;
		if(thread != null)
			thread.setListener(listener);
	}

	public void setUserData(Object data)
	{
	}

	int receive(byte[] data) throws IOException
	{
		return socket.getInputStream().read(data);
	}

	void setConnected(boolean connected)
	{
		this.connected = connected;
	}
}