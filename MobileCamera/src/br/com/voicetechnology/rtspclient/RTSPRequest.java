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
package br.com.voicetechnology.rtspclient;

import java.net.URI;
import java.net.URISyntaxException;

import br.com.voicetechnology.rtspclient.concepts.Client;
import br.com.voicetechnology.rtspclient.concepts.Message;
import br.com.voicetechnology.rtspclient.concepts.Request;
import br.com.voicetechnology.rtspclient.concepts.Response;

public class RTSPRequest extends RTSPMessage implements Request
{
	private Method method;

	private String uri;

	public RTSPRequest()
	{
	}

	public RTSPRequest(String messageLine) throws URISyntaxException
	{
		String[] parts = messageLine.split(" ");
		setLine(parts[0], Method.valueOf(parts[1]));
	}

	public void setLine(String uri, Method method) throws URISyntaxException
	{
		this.method = method;
		this.uri = new URI(uri).toString();
		;

		super.setLine(method.toString() + ' ' + uri + ' ' + RTSP_VERSION_TOKEN);
	}

	public Method getMethod()
	{
		return method;
	}

	public String getURI()
	{
		return uri;
	}

	public void handleResponse(Client client, Response response)
	{
		if(testForClose(client, this) || testForClose(client, response))
			client.getTransport().disconnect();
	}

	protected void setURI(String uri)
	{
		this.uri = uri;
	}

	protected void setMethod(Method method)
	{
		this.method = method;
	}

	private boolean testForClose(Client client, Message message)
	{
		try
		{
			return message.getHeader("Connection").getRawValue().equalsIgnoreCase("close");
		} catch(MissingHeaderException e)
		{
		} catch(Exception e)
		{
			client.getClientListener().generalError(client, e);
		}
		return false;
	}
}