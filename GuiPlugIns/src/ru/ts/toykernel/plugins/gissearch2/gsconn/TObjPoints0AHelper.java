package ru.ts.toykernel.plugins.gissearch2.gsconn;
import su.org.coder.utils.*;

import java.io.*;

public class TObjPoints0AHelper implements ISerializeHelper
{

	ISerializeHelper _tObjPoint1Ah = new TObjPoint1AHelper();

	public byte[]  serialChannelObj(Object toArray) throws IOException
	{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos=new DataOutputStream(bos);
	    serialChannelObj(toArray,dos);
		return bos.toByteArray();
	}

	public Object createChannelObj(byte[] fromArray) throws IOException
	{
		if (fromArray==null)
			return null;
		DataInputStream dis=new DataInputStream(new ByteArrayInputStream(fromArray, 0, fromArray.length));
		return createChannelObj(dis);
	}

	public Object createChannelObj(DataInputStream dis) throws IOException
	{
		if (dis.readByte()==1)
			return null;
		TObjPoints st = new TObjPoints();
		st.objs=(TObjPoint[])_tObjPoint1Ah.createChannelObj(dis);
		return st;
	}

	public void serialChannelObj(Object toArray, DataOutputStream dos) throws IOException
	{
		if (toArray==null)
		{
			dos.writeByte(1);
			return;
		}
		dos.writeByte(0);
		TObjPoints st = (TObjPoints) toArray;
		_tObjPoint1Ah.serialChannelObj( st.objs,dos);
	}

	public String toPrintableString(Object toPrintable)
	{
		if (toPrintable instanceof TObjPoints)
		{
			TObjPoints st = (TObjPoints) toPrintable;
			return
				_tObjPoint1Ah.toPrintableString( st.objs)+
				"\n";
		}
		else if (toPrintable==null)
			return "toPrintable is null\n";
		return "toPrintable has wrong type\n";
	}
}
