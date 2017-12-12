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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import android.util.Base64;
import br.com.voicetechnology.rtspclient.concepts.Client;
import br.com.voicetechnology.rtspclient.concepts.ClientListener;
import br.com.voicetechnology.rtspclient.concepts.Header;
import br.com.voicetechnology.rtspclient.concepts.Message;
import br.com.voicetechnology.rtspclient.concepts.MessageBuffer;
import br.com.voicetechnology.rtspclient.concepts.MessageFactory;
import br.com.voicetechnology.rtspclient.concepts.Request;
import br.com.voicetechnology.rtspclient.concepts.Response;
import br.com.voicetechnology.rtspclient.concepts.Transport;
import br.com.voicetechnology.rtspclient.concepts.TransportListener;
import br.com.voicetechnology.rtspclient.concepts.Request.Method;
import br.com.voicetechnology.rtspclient.headers.AuthorizationHeader;
import br.com.voicetechnology.rtspclient.headers.RangeHeader;
import br.com.voicetechnology.rtspclient.headers.SessionHeader;
import br.com.voicetechnology.rtspclient.headers.TransportHeader;
import br.com.voicetechnology.rtspclient.headers.TransportHeader.LowerTransport;
import br.com.voicetechnology.rtspclient.messages.RTSPOptionsRequest;

public class RTSPClient implements Client, TransportListener
{
	private Transport transport;

	private MessageFactory messageFactory;

	private MessageBuffer messageBuffer;

	private volatile int cseq;

	private SessionHeader session;

	private String request_uri;
	private boolean isDigest = false;
	private String username;
	private String password;
	private String realm;
	private String nonce;
	
	/**
	 * URI kept from last setup.
	 */
	private URI uri;

	private Map<Integer, Request> outstanding;

	private ClientListener clientListener;

	public RTSPClient()
	{
		messageFactory = new RTSPMessageFactory();
		cseq = 0;
		outstanding = new HashMap<Integer, Request>();
		messageBuffer = new MessageBuffer();
	}

	public void setAuthentication(String _request_uri, boolean _isDigest, String _username, String _password, String _realm, String _nonce)
	{
		request_uri = _request_uri;
		isDigest = _isDigest;
		username = _username;
		password = _password;
		realm = _realm;
		nonce = _nonce;
	}
	
	public Transport getTransport()
	{
		return transport;
	}

	public void setSession(SessionHeader session)
	{
		this.session = session;
	}

	public MessageFactory getMessageFactory()
	{
		return messageFactory;
	}

	public URI getURI()
	{
		return uri;
	}

	public void options(String uri, URI endpoint) throws URISyntaxException,
			IOException
	{
		try
		{
			RTSPOptionsRequest message = (RTSPOptionsRequest) messageFactory
					.outgoingRequest(uri, Method.OPTIONS, nextCSeq());
			if(!getTransport().isConnected())
				message.addHeader(new Header("Connection", "close"));
			send(message, endpoint);
		} catch(MissingHeaderException e)
		{
			if(clientListener != null)
				clientListener.generalError(this, e);
		}
	}

	public void play(String uri2) throws IOException
	{
		RangeHeader rH = new RangeHeader("npt=0.000-");
		try
		{
			String authHeader = null;
			if (false == isDigest)
			{
				String tmp = username + ":" + password;
				authHeader = "Basic " + Base64.encodeToString(tmp.getBytes(), Base64.NO_WRAP);
			}
			else
			{
				String tmp1 = md5(username + ":" + realm + ":" + password);
				String tmp2 = md5("PLAY" + ":" + request_uri);
				String resp = md5(tmp1 + ":" + nonce + ":" + tmp2);
				authHeader = "Digest username=\"" + username + "\", realm=\"" + realm + "\", nonce=\"" + nonce + "\", uri=\"" + request_uri + "\", response=\"" + resp + "\"";
			}
			
			String s1 = session.getRawValue();
			if(s1.indexOf(";")!=-1)
				{
				s1=s1.substring(0,s1.indexOf(";"));
				session.setRawValue(s1);
				send(messageFactory.outgoingRequest(uri2, Method.PLAY,
						nextCSeq(),
						session,rH,new AuthorizationHeader(authHeader)));
				}
			else send(messageFactory.outgoingRequest(uri2, Method.PLAY,
					nextCSeq(),
					session,rH,new AuthorizationHeader(authHeader)));
		} catch(Exception e)
		{
			if(clientListener != null)
				clientListener.generalError(this, e);
		}
	}

	public void record() throws IOException
	{
		throw new UnsupportedOperationException(
				"Recording is not supported in current version.");
	}

	public void setClientListener(ClientListener listener)
	{
		clientListener = listener;
	}

	public ClientListener getClientListener()
	{
		return clientListener;
	}

	public void setTransport(Transport transport)
	{
		this.transport = transport;
		transport.setTransportListener(this);
	}
	
	private String md5(String string) {
	    byte[] hash;
	    try {
	        hash = MessageDigest.getInstance("MD5").digest(string.getBytes("UTF-8"));
	    } catch (NoSuchAlgorithmException e) {
	        throw new RuntimeException("Huh, MD5 should be supported?", e);
	    } catch (UnsupportedEncodingException e) {
	        throw new RuntimeException("Huh, UTF-8 should be supported?", e);
	    }
	    
	    StringBuilder hex = new StringBuilder(hash.length * 2);
	    for (byte b : hash) {
	        if ((b & 0xFF) < 0x10) hex.append("0");
	        hex.append(Integer.toHexString(b & 0xFF));
	    }
	    return hex.toString();
	}
	
	public void describe(URI uri) throws IOException
	{
		this.uri = uri;
		try
		{
			String authHeader = null;
			if (false == isDigest)
			{
				String tmp = username + ":" + password;
				authHeader = "Basic " + Base64.encodeToString(tmp.getBytes(), Base64.NO_WRAP);
			}
			else
			{
				String tmp1 = md5(username + ":" + realm + ":" + password);
				String tmp2 = md5("DESCRIBE" + ":" + request_uri);
				String resp = md5(tmp1 + ":" + nonce + ":" + tmp2);
				authHeader = "Digest username=\"" + username + "\", realm=\"" + realm + "\", nonce=\"" + nonce + "\", uri=\"" + request_uri + "\", response=\"" + resp + "\"";
			}
			
			send(messageFactory.outgoingRequest(uri.toString(), Method.DESCRIBE,
					nextCSeq(),
					new Header("Accept", "application/sdp"),
					new AuthorizationHeader(authHeader)));
		} catch(Exception e)
		{
			if(clientListener != null)
				clientListener.generalError(this, e);
		}
	}

	public void setup(URI uri, String uri2, int localPort) throws IOException
	{
		this.uri = uri;
		try
		{
			String authHeader = null;
			if (false == isDigest)
			{
				String tmp = username + ":" + password;
				authHeader = "Basic " + Base64.encodeToString(tmp.getBytes(), Base64.NO_WRAP);
			}
			else
			{
				String tmp1 = md5(username + ":" + realm + ":" + password);
				String tmp2 = md5("SETUP" + ":" + request_uri);
				String resp = md5(tmp1 + ":" + nonce + ":" + tmp2);
				authHeader = "Digest username=\"" + username + "\", realm=\"" + realm + "\", nonce=\"" + nonce + "\", uri=\"" + request_uri + "\", response=\"" + resp + "\"";
			}
			
			String portParam = "client_port=" + localPort + "-" + (1 + localPort);
			
			send(getSetup(uri2, localPort, 
					new AuthorizationHeader(authHeader),
					new TransportHeader(
					LowerTransport.DEFAULT, "unicast", portParam), session));
		} catch(Exception e)
		{
			if(clientListener != null)
				clientListener.generalError(this, e);
		}
	}

	public void setup(URI uri, String uri2, int localPort, String resource) throws IOException
	{
		this.uri = uri;
		try
		{
			String authHeader = null;
			if (false == isDigest)
			{
				String tmp = username + ":" + password;
				authHeader = "Basic " + Base64.encodeToString(tmp.getBytes(), Base64.NO_WRAP);
			}
			else
			{
				String tmp1 = md5(username + ":" + realm + ":" + password);
				String tmp2 = md5("SETUP" + ":" + request_uri);
				String resp = md5(tmp1 + ":" + nonce + ":" + tmp2);
				authHeader = "Digest username=\"" + username + "\", realm=\"" + realm + "\", nonce=\"" + nonce + "\", uri=\"" + request_uri + "\", response=\"" + resp + "\"";
			}
			
			String portParam = "client_port=" + localPort + "-" + (1 + localPort);
			String finalURI = uri2;
			
			if(resource != null && !resource.equals("*"))
			{
				if(finalURI.endsWith("/"))finalURI +=resource;
				else finalURI += '/' + resource;
			}
				
			send(getSetup(finalURI, localPort, 
					new AuthorizationHeader(authHeader),
					new TransportHeader(
					LowerTransport.DEFAULT, "unicast", portParam), session));
		} catch(Exception e)
		{
			if(clientListener != null)
				clientListener.generalError(this, e);
		}
	}

	public void teardown()
	{
		if(session == null)
			return;
		try
		{
			String authHeader = null;
			if (false == isDigest)
			{
				String tmp = username + ":" + password;
				authHeader = "Basic " + Base64.encodeToString(tmp.getBytes(), Base64.NO_WRAP);
			}
			else
			{
				String tmp1 = md5(username + ":" + realm + ":" + password);
				String tmp2 = md5("TEARDOWN" + ":" + request_uri);
				String resp = md5(tmp1 + ":" + nonce + ":" + tmp2);
				authHeader = "Digest username=\"" + username + "\", realm=\"" + realm + "\", nonce=\"" + nonce + "\", uri=\"" + request_uri + "\", response=\"" + resp + "\"";
			}
			
			send(messageFactory.outgoingRequest(uri.toString(), Method.TEARDOWN,
					nextCSeq(), session, 
					new AuthorizationHeader(authHeader),
					new Header("Connection", "close")));
		} catch(Exception e)
		{
			if(clientListener != null)
				clientListener.generalError(this, e);
		}
	}

	public void connected(Transport t) throws Throwable
	{
	}

	public void dataReceived(Transport t, byte[] data, int size) throws Throwable
	{
		messageBuffer.addData(data, size);
		int len = messageBuffer.getLength();
		int len_s = size;
//		
		while(messageBuffer.getLength() > 0)
		{
			messageFactory.incomingMessage(messageBuffer);
			if(RTSPMessageFactory.flagMalFormedPacket)
			{
				break;
			}
			else
			{
				try
				{
					messageBuffer.discardData();
					Message message = messageBuffer.getMessage();
					if(message instanceof Request)
						send(messageFactory.outgoingResponse(405, "Method Not Allowed",
								message.getCSeq().getValue()));
					else
					{
						Request request = null;
						synchronized(outstanding)
						{
							request = outstanding.remove(message.getCSeq().getValue());
						}
						Response response = (Response) message;
						request.handleResponse(this, response);
						clientListener.response(this, request, response);
					}
				} catch(IncompleteMessageException ie)
				{
					break;
				} catch(InvalidMessageException e)
				{
					messageBuffer.discardData();
					if(clientListener != null)
						clientListener.generalError(this, e.getCause());
				}
			}
		}
			
	}

	public void dataSent(Transport t) throws Throwable
	{
	}

	public void error(Transport t, Throwable error)
	{
		clientListener.generalError(this, error);
	}

	public void error(Transport t, Message message, Throwable error)
	{
		clientListener.requestFailed(this, (Request) message, error);
	}

	public void remoteDisconnection(Transport t) throws Throwable
	{
		synchronized(outstanding)
		{
			for(Map.Entry<Integer, Request> request : outstanding.entrySet())
				clientListener.requestFailed(this, request.getValue(),
						new SocketException("Socket has been closed"));
		}
	}

	public int nextCSeq()
	{
		return cseq++;
	}

	public void send(Message message) throws IOException, MissingHeaderException
	{
		send(message, uri);
	}

	private void send(Message message, URI endpoint) throws IOException,
			MissingHeaderException
	{
		if(!transport.isConnected())
			transport.connect(endpoint);

		if(message instanceof Request)
		{
			Request request = (Request) message;
			synchronized(outstanding)
			{
				outstanding.put(message.getCSeq().getValue(), request);
			}
			try
			{
				transport.sendMessage(message);
			} catch(IOException e)
			{
				clientListener.requestFailed(this, request, e);
			}
		} else
			transport.sendMessage(message);
	}

	private Request getSetup(String uri, int localPort, Header... headers)
			throws URISyntaxException
	{
		return getMessageFactory().outgoingRequest(uri, Method.SETUP, nextCSeq(),
				headers);
	}
}