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

import java.net.URI;

import br.com.voicetechnology.rtspclient.RTSPClient;
import br.com.voicetechnology.rtspclient.concepts.Client;
import br.com.voicetechnology.rtspclient.concepts.ClientListener;
import br.com.voicetechnology.rtspclient.concepts.Request;
import br.com.voicetechnology.rtspclient.concepts.Response;
import br.com.voicetechnology.rtspclient.transport.PlainTCP;

public class OPTIONSTest implements ClientListener
{
	public static void main(String[] args) throws Throwable
	{
		new OPTIONSTest();
	}

	private OPTIONSTest() throws Exception
	{
		RTSPClient client = new RTSPClient();

		client.setTransport(new PlainTCP());
		client.setClientListener(this);
		client.options("*", new URI("rtsp://rmv8.bbc.net.uk/1xtra/"));
	}

	public void requestFailed(Client client, Request request, Throwable cause)
	{
		System.out.println("Request failed \n" + request);
	}

	public void response(Client client, Request request, Response response)
	{
		System.out.println("Got response: \n" + response);
		System.out.println("for the request: \n" + request);
	}

	public void generalError(Client client, Throwable error)
	{
		error.printStackTrace();
	}

	public void mediaDescriptor(Client client, String descriptor)
	{
		// TODO Auto-generated method stub
		
	}
}
