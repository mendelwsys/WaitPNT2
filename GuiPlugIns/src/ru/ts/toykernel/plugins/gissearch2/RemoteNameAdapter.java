package ru.ts.toykernel.plugins.gissearch2;

import ru.ts.utils.data.IReadStorage;
import ru.ts.toykernel.plugins.gissearch2.gsconn.IIStorage;
import ru.ts.toykernel.plugins.gissearch2.gsconn.ProxyIStorage;
import ru.ts.toykernel.plugins.IModule;
import ru.ts.toykernel.proj.ICliConfigProvider;

import java.io.IOException;

import su.org.coder.utils.SysCoderEx;
import su.org.coder.multiplexer.protocols.IMediator;
import su.org.coder.multiplexer.client.ClientMediatorConnector;

/**
 * адаптер строк для апплета
 */
public class RemoteNameAdapter
		implements IReadStorage<String>

{
	private IIStorage proxy;
	public RemoteNameAdapter(IModule cmdprovider, ICliConfigProvider conf_provider) throws IOException, SysCoderEx
	{
		IMediator mediator = new MediatorImpl(cmdprovider,conf_provider);
		this.proxy = ProxyIStorage.bind(new ClientMediatorConnector(mediator),"NameStorImpl");
	}
	public int getCurpos()
	{
		try
		{
			return proxy.getCurpos();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return 0;
	}

	public String get(int i)
	{
		try
		{
			return proxy.get(i);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (SysCoderEx sysCoderEx)
		{
			sysCoderEx.printStackTrace();
		}
		return "";
	}

	public boolean hasMoreElements()
	{
		try
		{
			return proxy.hasMoreElements();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (SysCoderEx sysCoderEx)
		{
			sysCoderEx.printStackTrace();
		}
		return false;
	}

	public boolean hasPrevElements()
	{
		try
		{
			return proxy.hasPrevElements();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (SysCoderEx sysCoderEx)
		{
			sysCoderEx.printStackTrace();
		}
		return false;
	}

	public String nextElement()
	{
		try
		{
			return proxy.nextElement();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (SysCoderEx sysCoderEx)
		{
			sysCoderEx.printStackTrace();
		}
		return "";
	}

	public void setposon(int pos)
	{
		try
		{
			proxy.setposon(pos);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (SysCoderEx sysCoderEx)
		{
			sysCoderEx.printStackTrace();
		}
	}

	public String prevElement()
	{
		try
		{
			return proxy.prevElement();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (SysCoderEx sysCoderEx)
		{
			sysCoderEx.printStackTrace();
		}
		return "";
	}

	public int size()
	{
		try
		{
			return proxy.size();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (SysCoderEx sysCoderEx)
		{
			sysCoderEx.printStackTrace();
		}
		return 0;
	}

	public String remove(int i)
	{
		throw new UnsupportedOperationException("remove is not supported");
	}
}
