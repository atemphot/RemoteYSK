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

import java.util.ArrayList;
import java.util.List;

import br.com.voicetechnology.rtspclient.concepts.EntityMessage;
import br.com.voicetechnology.rtspclient.concepts.Header;
import br.com.voicetechnology.rtspclient.concepts.Message;
import br.com.voicetechnology.rtspclient.headers.CSeqHeader;

public abstract class RTSPMessage implements Message
{
	private String line;

	private List<Header> headers;

	private CSeqHeader cseq;
	
	private EntityMessage entity;

	public RTSPMessage()
	{
		headers = new ArrayList<Header>();
	}

	public byte[] getBytes() throws MissingHeaderException
	{
		getHeader(CSeqHeader.NAME);
		addHeader(new Header("User-Agent", "RTSPClientLib/Java"));
		byte[] message = toString().getBytes();
		if(getEntityMessage() != null)
		{
			byte[] body = entity.getBytes();
			byte[] full = new byte[message.length + body.length];
			System.arraycopy(message, 0, full, 0, message.length);
			System.arraycopy(body, 0, full, message.length, body.length);
			message = full;
		}
		return message;
	}

	public Header getHeader(final String name) throws MissingHeaderException
	{
		int index = headers.indexOf(new Object() {
			@Override
			public boolean equals(Object obj)
			{
				return name.equalsIgnoreCase(((Header) obj).getName());
			}
		});
		if(index == -1)
			throw new MissingHeaderException(name);
		return headers.get(index);
	}

	public Header[] getHeaders()
	{
		return headers.toArray(new Header[headers.size()]);
	}

	public CSeqHeader getCSeq()
	{
		return cseq;
	}

	public String getLine()
	{
		return line;
	}

	public void setLine(String line)
	{
		this.line = line;
	}
	
	public void addHeader(Header header)
	{
		if(header == null) return;
		if(header instanceof CSeqHeader)
			cseq = (CSeqHeader) header;
		int index = headers.indexOf(header);
		if(index > -1)
			headers.remove(index);
		else
			index = headers.size();
		headers.add(index, header);
	}
	
	public EntityMessage getEntityMessage()
	{
		return entity;
	}
	
	public Message setEntityMessage(EntityMessage entity)
	{
		this.entity = entity;
		return this;
	}
	
	@Override
	public String toString()
	{
		StringBuilder buffer = new StringBuilder();
		buffer.append(getLine()).append("\r\n");
		for(Header header : headers)
			buffer.append(header).append("\r\n");
		buffer.append("\r\n");
		return buffer.toString();
	}
}
