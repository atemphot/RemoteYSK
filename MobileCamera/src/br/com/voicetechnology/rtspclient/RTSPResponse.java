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

import br.com.voicetechnology.rtspclient.concepts.Response;

public class RTSPResponse extends RTSPMessage implements Response
{
	private int status;

	private String text;

	public RTSPResponse()
	{
	}

	public RTSPResponse(String line)
	{
		setLine(line);
		line = line.substring(line.indexOf(' ') + 1);
		status = Integer.parseInt(line.substring(0, line.indexOf(' ')));
		text = line.substring(line.indexOf(' ') + 1);
	}

	public int getStatusCode()
	{
		return status;
	}
	
	public String getStatusText()
	{
		return text;
	}

	public void setLine(int statusCode, String statusText)
	{
		status = statusCode;
		text = statusText;
		super.setLine(RTSP_VERSION_TOKEN + ' ' + status + ' ' + text);
	}
}
