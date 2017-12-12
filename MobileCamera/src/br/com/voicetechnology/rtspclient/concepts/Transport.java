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

import br.com.voicetechnology.rtspclient.MissingHeaderException;

/**
 * This interface defines a transport protocol (TCP, UDP) or method (HTTP
 * tunneling). Transport also MUST enqueue a command if a connection is busy at
 * the moment it is issued.
 * 
 * @author paulo
 */
public interface Transport
{
	void connect(URI to) throws IOException;

	void disconnect();

	void sendMessage(Message message) throws IOException, MissingHeaderException;

	void setTransportListener(TransportListener listener);

	void setUserData(Object data);

	boolean isConnected();
}
