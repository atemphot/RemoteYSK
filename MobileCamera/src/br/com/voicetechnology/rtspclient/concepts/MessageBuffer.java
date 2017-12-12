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

import java.util.ArrayList;

public class MessageBuffer
{
	/**
	 * buffer for received data
	 */
//	private byte[] data;

	/**
	 * offset for starting useful area
	 */
	private int offset;

	/**
	 * length of useful portion.
	 */
	private int length;

	/**
	 * Used (read) buffer.
	 */
	private int used;

	/**
	 * {@link Message} created during last parsing.
	 */
	private Message message;

	ArrayList<byte[]> data = new ArrayList<byte[]>();
	/**
	 * Adds more data to buffer and ensures the sequence [data, newData] is
	 * contiguous.
	 * 
	 * @param newData data to be added to the buffer.
	 */
	public void addData(byte[] newData, int newLength)
	{
		byte[] temp =new byte[newLength];
		System.arraycopy(newData, 0, temp, 0, newLength);
		data.add(temp);
		
//		if(data == null)
//		{
//			data = newData;
//			length = newLength;
//			offset = 0;
//		} else
//		{
//			// buffer seems to be small.
//			if((data.length - offset - length) < newLength)
//			{
//				// try to sequeeze data at the beginning of the buffer only if current
//				// buffer does not overlap
//				if(offset >= length && (data.length - length) >= newLength)
//				{
//					System.arraycopy(data, offset, data, 0, length);
//					offset = 0;
//				} else
//				{ // worst-case scenario, a new buffer will have to be created
//					byte[] temp = new byte[data.length + newLength];
//					System.arraycopy(data, offset, temp, 0, length);
//					offset = 0;
//					data = temp;
//				}
//			}
//			// there's room for everything - just copy
//			System.arraycopy(newData, 0, data, offset + length, newLength);
//			length += newLength;
//		}
	}

	/**
	 * Discards used portions of the buffer.
	 */
	public void discardData()
	{
		data.clear();
	}

	public byte[] getData()
	{
		int size=0,offset=0;
		for(int i=0;i<data.size();i++)
			size +=data.get(i).length; 
		byte[] temp = new byte[size];
		for(int i=0;i<data.size();i++)
		{		System.arraycopy(data.get(i), 0, temp, offset,data.get(i).length); 
			offset+=data.get(i).length;
		}
		
		return temp;
	}

	public int getOffset()
	{
		return offset;
	}

	public int getLength()
	{
		return data.size();
	}

	public void setMessage(Message message)
	{
		this.message = message;
	}

	public Message getMessage()
	{
		return message;
	}

	public void setused(int used)
	{
		this.used = used;
	}
	public void setoffset(int offset)
	{
		this.offset = offset;
	}
}