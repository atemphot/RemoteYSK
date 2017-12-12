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

import br.com.voicetechnology.rtspclient.concepts.Content;
import br.com.voicetechnology.rtspclient.concepts.EntityMessage;
import br.com.voicetechnology.rtspclient.concepts.Message;
import br.com.voicetechnology.rtspclient.headers.ContentEncodingHeader;
import br.com.voicetechnology.rtspclient.headers.ContentLengthHeader;
import br.com.voicetechnology.rtspclient.headers.ContentTypeHeader;

public class RTSPEntityMessage implements EntityMessage
{
	private Content content;

	private final Message message;

	public RTSPEntityMessage(Message message)
	{
		this.message = message;
	}
	
	public RTSPEntityMessage(Message message, Content body)
	{
		this(message);
		setContent(body);
	}
	
	public Message getMessage()
	{
		return message;
	};

	public byte[] getBytes() throws MissingHeaderException
	{
		message.getHeader(ContentTypeHeader.NAME);
		message.getHeader(ContentLengthHeader.NAME);
		return content.getBytes();
	}

	public Content getContent()
	{
		return content;
	}

	public void setContent(Content content)
	{
		if(content == null) throw new NullPointerException();
		this.content = content;
		message.addHeader(new ContentTypeHeader(content.getType()));
		if(content.getEncoding() != null)
			message.addHeader(new ContentEncodingHeader(content.getEncoding()));
		message.addHeader(new ContentLengthHeader(content.getBytes().length));
	}
	
	public boolean isEntity()
	{
		return content != null;
	}
}
