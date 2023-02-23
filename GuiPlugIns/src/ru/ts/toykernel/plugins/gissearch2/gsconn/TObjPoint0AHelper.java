package ru.ts.toykernel.plugins.gissearch2.gsconn;
import su.org.coder.utils.*;

import java.io.*;

public class TObjPoint0AHelper implements ISerializeHelper
{

	ISerializeHelper _tPoint1Ah = new TPoint1AHelper();
	ISerializeHelper _string0Ah = new String0AHelper();

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
		TObjPoint st = new TObjPoint();
		st.pnts=(TPoint[])_tPoint1Ah.createChannelObj(dis);
		st.objId=(String)_string0Ah.createChannelObj(dis);
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
		TObjPoint st = (TObjPoint) toArray;
		_tPoint1Ah.serialChannelObj( st.pnts,dos);
		_string0Ah.serialChannelObj( st.objId,dos);
	}

	public String toPrintableString(Object toPrintable)
	{
		if (toPrintable instanceof TObjPoint)
		{
			TObjPoint st = (TObjPoint) toPrintable;
			return
				_tPoint1Ah.toPrintableString( st.pnts)+
				"objId:"+st.objId+" "+
				"\n";
		}
		else if (toPrintable==null)
			return "toPrintable is null\n";
		return "toPrintable has wrong type\n";
	}
}
