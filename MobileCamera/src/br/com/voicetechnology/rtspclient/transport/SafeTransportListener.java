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
package br.com.voicetechnology.rtspclient.transport;

import br.com.voicetechnology.rtspclient.concepts.Message;
import br.com.voicetechnology.rtspclient.concepts.Transport;
import br.com.voicetechnology.rtspclient.concepts.TransportListener;

/**
 * Auxiliary class to make listener calls.
 * 
 * @author paulo
 * 
 */
class SafeTransportListener implements TransportListener
{
	private final TransportListener behaviour;

	public SafeTransportListener(TransportListener theBehaviour)
	{
		behaviour = theBehaviour;
	}

	public void connected(Transport t)
	{
		if(behaviour != null)
			try
			{
				behaviour.connected(t);
			} catch(Throwable error)
			{
				behaviour.error(t, error);
			}
	}

	public void dataReceived(Transport t, byte[] data, int size)
	{
		if(behaviour != null)
			try
			{
				behaviour.dataReceived(t, data, size);
			} catch(Throwable error)
			{
				behaviour.error(t, error);
			}
	}

	public void dataSent(Transport t)
	{
		// TODO Auto-generated method stub
		if(behaviour != null)
			try
			{
				behaviour.dataSent(t);
			} catch(Throwable error)
			{
				behaviour.error(t, error);
			}

	}

	public void error(Transport t, Throwable error)
	{
		if(behaviour != null)
			behaviour.error(t, error);
	}

	public void error(Transport t, Message message, Throwable error)
	{
		if(behaviour != null)
			behaviour.error(t, message, error);
	}

	public void remoteDisconnection(Transport t)
	{
		if(behaviour != null)
			try
			{
				behaviour.remoteDisconnection(t);
			} catch(Throwable error)
			{
				behaviour.error(t, error);
			}
	}

}
