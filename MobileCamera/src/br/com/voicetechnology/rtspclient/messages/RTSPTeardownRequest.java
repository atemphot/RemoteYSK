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
package br.com.voicetechnology.rtspclient.messages;

import java.net.URISyntaxException;

import br.com.voicetechnology.rtspclient.MissingHeaderException;
import br.com.voicetechnology.rtspclient.RTSPRequest;
import br.com.voicetechnology.rtspclient.concepts.Client;
import br.com.voicetechnology.rtspclient.concepts.Response;
import br.com.voicetechnology.rtspclient.headers.SessionHeader;

public class RTSPTeardownRequest extends RTSPRequest
{

	public RTSPTeardownRequest()
	{
		super();
	}

	public RTSPTeardownRequest(String messageLine) throws URISyntaxException
	{
		super(messageLine);
	}

	@Override
	public byte[] getBytes() throws MissingHeaderException
	{
		getHeader(SessionHeader.NAME);
		return super.getBytes();
	}
	
	@Override
	public void handleResponse(Client client, Response response)
	{
		super.handleResponse(client, response);
		if(response.getStatusCode() == 200) client.setSession(null);
		client.getTransport().disconnect();
	}
}
