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
package br.com.voicetechnology.rtspclient.concepts;

import br.com.voicetechnology.rtspclient.MissingHeaderException;
import br.com.voicetechnology.rtspclient.headers.ContentEncodingHeader;
import br.com.voicetechnology.rtspclient.headers.ContentTypeHeader;

public class Content
{
	private String type;

	private String encoding;

	private byte[] content;

	public void setDescription(Message message) throws MissingHeaderException
	{
		type = message.getHeader(ContentTypeHeader.NAME).getRawValue();
		try
		{
			encoding = message.getHeader(ContentEncodingHeader.NAME).getRawValue();
		} catch(MissingHeaderException e)
		{
		}
	}

	public String getType()
	{
		return type;
	}

	public void setType(String type)
	{
		this.type = type;
	}

	public String getEncoding()
	{
		return encoding;
	}

	public void setEncoding(String encoding)
	{
		this.encoding = encoding;
	}

	public byte[] getBytes()
	{
		return content;
	}

	public void setBytes(byte[] content)
	{
		this.content = content;
	}
}
