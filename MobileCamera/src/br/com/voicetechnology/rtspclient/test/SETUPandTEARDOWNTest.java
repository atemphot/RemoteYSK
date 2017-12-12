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
package br.com.voicetechnology.rtspclient.test;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import br.com.voicetechnology.rtspclient.RTSPClient;
import br.com.voicetechnology.rtspclient.concepts.Client;
import br.com.voicetechnology.rtspclient.concepts.ClientListener;
import br.com.voicetechnology.rtspclient.concepts.Request;
import br.com.voicetechnology.rtspclient.concepts.Response;
import br.com.voicetechnology.rtspclient.transport.PlainTCP;

public class SETUPandTEARDOWNTest implements ClientListener
{
	private final static String TARGET_URI = "rtsp://192.168.0.102/sample_50kbit.3gp";

	public static void main(String[] args) throws Throwable
	{
		new SETUPandTEARDOWNTest();
	}

	private final List<String> resourceList;
	
	private String controlURI;

	private int port;

	protected SETUPandTEARDOWNTest() throws Exception
	{
		RTSPClient client = new RTSPClient();

		client.setTransport(new PlainTCP());
		client.setClientListener(this);
		client.describe(new URI(TARGET_URI));
		resourceList = Collections.synchronizedList(new LinkedList<String>());
		port = 2000;
	}

	public void requestFailed(Client client, Request request, Throwable cause)
	{
		System.out.println("Request failed \n" + request);
		cause.printStackTrace();
	}

	public void response(Client client, Request request, Response response)
	{
		try
		{
			System.out.println("Got response: \n" + response);
			System.out.println("for the request: \n" + request);
			if(response.getStatusCode() == 200)
			{
				switch(request.getMethod())
				{
				case DESCRIBE:
					System.out.println(resourceList);
					if(resourceList.get(0).equals("*"))
					{
						controlURI = request.getURI();
						resourceList.remove(0);
					}
					if(resourceList.size() > 0)
						client.setup(new URI(controlURI), "ipcamera", nextPort(), resourceList
								.remove(0));
					else
						client.setup(new URI(controlURI), "ipcamera", nextPort());
					break;

				case SETUP:
					//sets up next session or ends everything.
					if(resourceList.size() > 0)
						client.setup(new URI(controlURI), "ipcamera", nextPort(), resourceList
								.remove(0));
					else
						sessionSet(client);
					break;
				}
			} else
				client.teardown();
		} catch(Throwable t)
		{
			generalError(client, t);
		}
	}

	public void generalError(Client client, Throwable error)
	{
		error.printStackTrace();
	}

	public void mediaDescriptor(Client client, String descriptor)
	{
		// searches for control: session and media arguments.
		final String target = "control:";
		System.out.println("Session Descriptor\n" + descriptor);
		int position = -1;
		while((position = descriptor.indexOf(target)) > -1)
		{
			descriptor = descriptor.substring(position + target.length());
			resourceList.add(descriptor.substring(0, descriptor.indexOf('\r')));
		}
	}
	
	protected void sessionSet(Client client) throws IOException
	{
		client.teardown();	
	}

	private int nextPort()
	{
		return (port += 2) - 2;
	}
}