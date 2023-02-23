package ru.ts.toykernel.plugins.gissearch2.gsconn;
import su.org.coder.utils.*;

import java.io.*;

public class FoundObj0AHelper implements ISerializeHelper
{

	ISerializeHelper _string1Ah = new String1AHelper();
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
		FoundObj st = new FoundObj();
		st.objids=(String[])_string1Ah.createChannelObj(dis);
		st.nameobj=(String)_string0Ah.createChannelObj(dis);
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
		FoundObj st = (FoundObj) toArray;
		_string1Ah.serialChannelObj( st.objids,dos);
		_string0Ah.serialChannelObj( st.nameobj,dos);
	}

	public String toPrintableString(Object toPrintable)
	{
		if (toPrintable instanceof FoundObj)
		{
			FoundObj st = (FoundObj) toPrintable;
			return
				_string1Ah.toPrintableString( st.objids)+
				"nameobj:"+st.nameobj+" "+
				"\n";
		}
		else if (toPrintable==null)
			return "toPrintable is null\n";
		return "toPrintable has wrong type\n";
	}
}
