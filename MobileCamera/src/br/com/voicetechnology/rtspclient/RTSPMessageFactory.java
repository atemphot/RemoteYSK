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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.R.array;
import br.com.voicetechnology.rtspclient.concepts.Content;
import br.com.voicetechnology.rtspclient.concepts.Header;
import br.com.voicetechnology.rtspclient.concepts.Message;
import br.com.voicetechnology.rtspclient.concepts.MessageBuffer;
import br.com.voicetechnology.rtspclient.concepts.MessageFactory;
import br.com.voicetechnology.rtspclient.concepts.Request;
import br.com.voicetechnology.rtspclient.concepts.Response;
import br.com.voicetechnology.rtspclient.concepts.Request.Method;
import br.com.voicetechnology.rtspclient.headers.CSeqHeader;
import br.com.voicetechnology.rtspclient.headers.ContentEncodingHeader;
import br.com.voicetechnology.rtspclient.headers.ContentLengthHeader;
import br.com.voicetechnology.rtspclient.headers.ContentTypeHeader;
import br.com.voicetechnology.rtspclient.headers.SessionHeader;
import br.com.voicetechnology.rtspclient.messages.RTSPDescribeRequest;
import br.com.voicetechnology.rtspclient.messages.RTSPOptionsRequest;
import br.com.voicetechnology.rtspclient.messages.RTSPPlayRequest;
import br.com.voicetechnology.rtspclient.messages.RTSPSetupRequest;
import br.com.voicetechnology.rtspclient.messages.RTSPTeardownRequest;

public class RTSPMessageFactory implements MessageFactory
{
	private static Map<String, Constructor<? extends Header>> headerMap;
	private static Map<Request.Method, Class<? extends Request>> requestMap;

	static
	{
		headerMap = new HashMap<String, Constructor<? extends Header>>();
		requestMap = new HashMap<Request.Method, Class<? extends Request>>();

		try
		{
			putHeader(CSeqHeader.class);
			putHeader(ContentEncodingHeader.class);
			putHeader(ContentLengthHeader.class);
			putHeader(ContentTypeHeader.class);
			putHeader(SessionHeader.class);

			requestMap.put(Method.OPTIONS, RTSPOptionsRequest.class);
			requestMap.put(Method.SETUP, RTSPSetupRequest.class);
			requestMap.put(Method.TEARDOWN, RTSPTeardownRequest.class);
			requestMap.put(Method.DESCRIBE, RTSPDescribeRequest.class);
			requestMap.put(Method.PLAY, RTSPPlayRequest.class);
		} catch(Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void putHeader(Class<? extends Header> cls) throws Exception
	{
		headerMap.put(cls.getDeclaredField("NAME").get(null).toString()
				.toLowerCase(), cls.getConstructor(String.class));
	}

	public static String sdpmsg;
	public static boolean flagMalFormedPacket=false;
	ArrayList<String> aSdp = new ArrayList<String>();
	public void incomingMessage(MessageBuffer buffer)
			throws InvalidMessageException
	{
//		ByteArrayInputStream in = new ByteArrayInputStream(buffer.getData(), buffer
//				.getOffset(), buffer.getLength());
		ByteArrayInputStream in = new ByteArrayInputStream(buffer.getData());
		int initial = in.available();
		Message message = null;

		try
		{
			// message line.
			String line = readLine(in);
			if(line.startsWith(Message.RTSP_TOKEN))
			{
				message = new RTSPResponse(line);
			} 
			else
			{
				Method method = Method.valueOf(line.substring(0, line.indexOf(' ')));
				Class<? extends Request> cls = requestMap.get(method);
				if(cls != null)
					message = cls.getConstructor(String.class).newInstance(line);
				else
					message = new RTSPRequest(line);
			}

			while(true)
			{
				line = readLine(in);
				if(in == null)
					throw new IncompleteMessageException();
				if(line.length() == 0)
				{
					break;
				}
				else{
				Constructor<? extends Header> c = headerMap.get(line.substring(0,
						line.indexOf(':')).toLowerCase());
				if(c != null)
					message.addHeader(c.newInstance(line));
				else
					message.addHeader(new Header(line));
				}
			}
			buffer.setMessage(message);

			try
			{
				int length = ((ContentLengthHeader) message
						.getHeader(ContentLengthHeader.NAME)).getValue();
				int left_len = in.available();
				if(in.available()==0 && length>0)
				{
					flagMalFormedPacket=true;
				}
				else if(in.available() < length)
					throw new IncompleteMessageException();
				else{
					Content content = new Content();
					content.setDescription(message);
					byte[] data = new byte[length];
					in.read(data);
					content.setBytes(data);
					message.setEntityMessage(new RTSPEntityMessage(message, content));
					flagMalFormedPacket=false;
				}
			} catch(MissingHeaderException e)
			{
			}

		} catch(Exception e)
		{
			throw new InvalidMessageException(e);
		} finally
		{
			if(!flagMalFormedPacket){
			buffer.setused(initial - in.available());
			try
			{
				in.close();
			} catch(IOException e)
			{
			}
			}
		}
	}

	public Request outgoingRequest(String uri, Method method, int cseq,
			Header... extras) throws URISyntaxException
	{
		Class<? extends Request> cls = requestMap.get(method);
		Request message;
		try
		{
			message = cls != null ? cls.newInstance() : new RTSPRequest();
		} catch(Exception e)
		{
			throw new RuntimeException(e);
		}
		message.setLine(uri, method);
		fillMessage(message, cseq, extras);

		return message;
	}

	public Request outgoingRequest(Content body, String uri, Method method,
			int cseq, Header... extras) throws URISyntaxException
	{
		Message message = outgoingRequest(uri, method, cseq, extras);
		return (Request) message.setEntityMessage(new RTSPEntityMessage(message,
				body));
	}

	public Response outgoingResponse(int code, String text, int cseq,
			Header... extras)
	{
		RTSPResponse message = new RTSPResponse();
		message.setLine(code, text);
		fillMessage(message, cseq, extras);

		return message;
	}

	public Response outgoingResponse(Content body, int code, String text,
			int cseq, Header... extras)
	{
		Message message = outgoingResponse(code, text, cseq, extras);
		return (Response) message.setEntityMessage(new RTSPEntityMessage(message, body));
	}

	private void fillMessage(Message message, int cseq, Header[] extras)
	{
		message.addHeader(new CSeqHeader(cseq));

		for(Header h : extras)
			message.addHeader(h);
	}

	private String readLine(InputStream in) throws IOException
	{
		int ch = 0;
		StringBuilder b = new StringBuilder();
		for(ch = in.read(); ch != -1 && ch != 0x0d && ch != 0x0a; ch = in.read())
			b.append((char) ch);
		if(ch == -1)
			return null;
		in.read();
		return b.toString();
	}
}
