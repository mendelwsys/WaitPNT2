package ru.ts.toykernel.plugins.gissearch2.gsconn;

import su.org.coder.utils.*;

import java.io.*;

import java.util.Vector;


public abstract class SkelIStorage
		implements IIStorage ,IInvoker
{
	private static String typeName="IStorage";
	private static ThreadLocal callCtx = new ThreadLocal();
	protected long lastApply=System.currentTimeMillis();
	protected boolean onService=true;
	protected ISerializeHelper _string0Ah = new String0AHelper();
	private short typeID = TypeId.getNextId();
	private IIStorage impl=this;

	protected Object getCallCtx()
	{
		return callCtx.get();
	}

	protected void setCallCtx(Object attr)
	{
		callCtx.set(attr);//Установить контекст вызова
	}

	public CallMessageImpl invoke(CallMessageImpl in, Vector attr) throws IOException, SysCoderEx
	{
		entranceNotify();
		try
		{
//Идентификатор typeID уникален для каждого экземпляра класса в пределах процесса
//поэтому невозможно что бы сообщение предназначалось вышестоящим объектам,
//а было обработано по ошибке нижестоящим
		if (in.typeID != typeID)
			throw new SysCoderEx(Constants.ERR_TYPEID, Constants.ERR_TYPEID_MSG); //Здесь нормальная реакция интерсептора
		setCallCtx(attr);
		DataInputStream dis =(in.bmessage!=null)?
				new DataInputStream(new ByteArrayInputStream(in.bmessage, 0, in.bmessage.length)):null;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		CallMessageImpl retMessage = new CallMessageImpl(in.command==Constants.INVOKE_VAL?Constants.RET_VAL:Constants.RET_VAL_NORET, in.typeID, in.objID, in.methodCode, null);
		switch (in.methodCode)
		{
			case 1 :
			{
				int getCurpos0AretVal = impl.getCurpos();
				dos.write(SerialUtils.serialint32(getCurpos0AretVal));
				retMessage.bmessage = bos.toByteArray();
			}
			break;
			case 2 :
			{
				int i = SerialUtils.unserialint(dis,4);
				String get0AretVal = impl.get(i);
				_string0Ah.serialChannelObj(get0AretVal,dos);
				retMessage.bmessage = bos.toByteArray();
			}
			break;
			case 3 :
			{
				boolean hasMoreElements0AretVal = impl.hasMoreElements();
				dos.writeBoolean(hasMoreElements0AretVal);
				retMessage.bmessage = bos.toByteArray();
			}
			break;
			case 4 :
			{
				boolean hasPrevElements0AretVal = impl.hasPrevElements();
				dos.writeBoolean(hasPrevElements0AretVal);
				retMessage.bmessage = bos.toByteArray();
			}
			break;
			case 5 :
			{
				String nextElement0AretVal = impl.nextElement();
				_string0Ah.serialChannelObj(nextElement0AretVal,dos);
				retMessage.bmessage = bos.toByteArray();
			}
			break;
			case 6 :
			{
				int pos = SerialUtils.unserialint(dis,4);
				int setposon0AretVal = impl.setposon(pos);
				dos.write(SerialUtils.serialint32(setposon0AretVal));
				retMessage.bmessage = bos.toByteArray();
			}
			break;
			case 7 :
			{
				String prevElement0AretVal = impl.prevElement();
				_string0Ah.serialChannelObj(prevElement0AretVal,dos);
				retMessage.bmessage = bos.toByteArray();
			}
			break;
			case 8 :
			{
				int size0AretVal = impl.size();
				dos.write(SerialUtils.serialint32(size0AretVal));
				retMessage.bmessage = bos.toByteArray();
			}
			break;
			default:
				throw new SysCoderEx(Constants.ERR_OPER, Constants.ERR_OPER_MSG);
		}
		return retMessage;
		}
		catch (IOException e)
		{
			setOffLine();
			throw e;
		}
		finally
		{
    		leaveNotify ();
		}
	}

	public String getTypeName()
	{
		return typeName;
	}

	public short getTypeID()
	{
		return typeID;
	}

	public boolean isOnService() throws SysCoderEx
	{
		return onService;
	}
	protected void setOffLine()
	{
		onService=false;
	}
	protected void entranceNotify () throws SysCoderEx
	{
		if (!onService)
			throw new SysCoderEx(Constants.ERR_NO_SERVICE, Constants.ERR_NO_SERVICE_MSG);//Объект отключился
//Установка последнего времени обращения
		lastApply=System.currentTimeMillis();
	}
	protected void leaveNotify ()
	{
		
	}
}
