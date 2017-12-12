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
import br.com.voicetechnology.rtspclient.headers.CSeqHeader;

/**
 * Models a RTSP Message
 * 
 * @author paulo
 * 
 */
public interface Message
{
	static String RTSP_TOKEN = "RTSP/";

	static String RTSP_VERSION = "1.0";

	static String RTSP_VERSION_TOKEN = RTSP_TOKEN + RTSP_VERSION;

	/**
	 * 
	 * @return the Message line (the first line of the message)
	 */
	String getLine();

	/**
	 * Returns a header, if exists
	 * 
	 * @param name
	 *          Name of the header to be searched
	 * @return value of that header
	 * @throws MissingHeaderException
	 */
	Header getHeader(String name) throws MissingHeaderException;

	/**
	 * Convenience method to get CSeq.
	 * 
	 * @return
	 */
	CSeqHeader getCSeq();

	/**
	 * 
	 * @return all headers in the message, except CSeq
	 */
	Header[] getHeaders();

	/**
	 * Adds a new header or replaces if one already exists. If header to be added
	 * is a CSeq, implementation MUST keep reference of this header.
	 * 
	 * @param header
	 */
	void addHeader(Header header);

	/**
	 * 
	 * @return message as a byte array, ready for transmission.
	 */
	byte[] getBytes() throws MissingHeaderException;

	/**
	 * 
	 * @return Entity part of message, it exists.
	 */
	EntityMessage getEntityMessage();

	/**
	 * 
	 * @param entity
	 *          adds an entity part to the message.
	 * @return this, for easier construction.
	 */
	Message setEntityMessage(EntityMessage entity);
}
