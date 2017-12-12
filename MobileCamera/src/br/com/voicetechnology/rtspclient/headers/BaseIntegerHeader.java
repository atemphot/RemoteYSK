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
package br.com.voicetechnology.rtspclient.headers;

import br.com.voicetechnology.rtspclient.concepts.Header;

public class BaseIntegerHeader extends Header
{
	
	private int value;
	
	public BaseIntegerHeader(String name)
	{
		super(name);
		String text = getRawValue();
		if(text != null) value = Integer.parseInt(text);
	}
	
	public BaseIntegerHeader(String name, int value)
	{
		super(name);
		setValue(value);
	}

	public BaseIntegerHeader(String name, String header)
	{
		super(header);
		checkName(name);
		value = Integer.parseInt(getRawValue());
	}

	public final void setValue(int newValue)
	{
		value = newValue;
		setRawValue(String.valueOf(value));
	}
	
	public final int getValue()
	{
		return value;
	}
}
