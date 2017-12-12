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

/**
 * Listener for transport events. Implementations of {@link Transport}, when
 * calling a listener method, must catch all errors and submit them to the
 * error() method.
 * 
 * @author paulo
 * 
 */
public interface TransportListener
{
	void connected(Transport t) throws Throwable;

	void error(Transport t, Throwable error);

	void error(Transport t, Message message, Throwable error);

	void remoteDisconnection(Transport t) throws Throwable;

	void dataReceived(Transport t, byte[] data, int size) throws Throwable;

	void dataSent(Transport t) throws Throwable;
}
