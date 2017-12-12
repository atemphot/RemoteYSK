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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import br.com.voicetechnology.rtspclient.MissingHeaderException;
import br.com.voicetechnology.rtspclient.headers.SessionHeader;

/**
 * Models a simple RTSPClient.<br>
 * it MUST keep track of CSeq to match requests and responses.
 * 
 * @author paulo
 * 
 */
public interface Client
{

	void setTransport(Transport transport);

	Transport getTransport();

	void setClientListener(ClientListener listener);

	ClientListener getClientListener();

	void setSession(SessionHeader session);

	MessageFactory getMessageFactory();

	URI getURI();

	void describe(URI uri) throws IOException;

	/**
	 * Sets up a session
	 * 
	 * @param uri
	 *          base URI for the request.
	 * @param localPort
	 *          Port for RTP stream. RTCP port is derived by adding 1 to this
	 *          port.
	 */
	void setup(URI uri, String uri2, int localPort) throws IOException;

	/**
	 * Sets up a session with a specific resource. If a session has been
	 * previously established, a call to this method will set up a different
	 * resource with the same session identifier as the previous one.
	 * 
	 * @see #setup(URI, int)
	 * @param resource
	 *          resource derived from SDP (via control: attribute). the final URI
	 *          will be <code>uri + '/' + resource</code>.
	 */
	void setup(URI uri, String uri2, int localPort, String resource) throws IOException;

	void teardown();

	void play(String uri2) throws IOException;

	void record() throws IOException;

	void options(String uri, URI endpoint) throws URISyntaxException, IOException;

	/**
	 * Sends a message and, if message is a {@link Request}, store it as an
	 * outstanding request.
	 * 
	 * @param message
	 * @throws IOException
	 * @throws MissingHeaderException
	 *           Malformed message, lacking mandatory header.
	 */
	void send(Message message) throws IOException, MissingHeaderException;

	/**
	 * 
	 * @return value of CSeq for next packet.
	 */
	int nextCSeq();
}
