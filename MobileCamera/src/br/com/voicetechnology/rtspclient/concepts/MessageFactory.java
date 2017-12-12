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

import java.net.URISyntaxException;

import br.com.voicetechnology.rtspclient.InvalidMessageException;

/**
 * A RTSP message factory to build objects from incoming messages or to
 * initialize outgoing messages correctly.
 * 
 * 
 * @author paulo
 * 
 */
public interface MessageFactory
{

	/**
	 * 
	 * @param message
	 */
	void incomingMessage(MessageBuffer message) throws InvalidMessageException;

	Request outgoingRequest(String uri, Request.Method method, int cseq,
			Header... extras) throws URISyntaxException;

	Request outgoingRequest(Content body, String uri, Request.Method method,
			int cseq, Header... extras) throws URISyntaxException;

	Response outgoingResponse(int code, String message, int cseq, Header... extras);

	Response outgoingResponse(Content body, int code, String text, int cseq,
			Header... extras);
}
