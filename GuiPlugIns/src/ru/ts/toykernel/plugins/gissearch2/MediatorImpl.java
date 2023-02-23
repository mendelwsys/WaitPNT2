package ru.ts.toykernel.plugins.gissearch2;

import su.org.coder.utils.SysCoderEx;
import su.org.coder.multiplexer.protocols.IMediator;

import java.io.*;
import java.util.HashMap;

import ru.ts.toykernel.plugins.*;
import ru.ts.toykernel.proj.ICliConfigProvider;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;

/**
 * Принцип действия такой:
 *
 * Канал передачи сначала получает выходной поток с помощью getOutputStream
 * записывает  туда данные обмена, после чего получает входной поток getInputStream
 * и читает peer's answer (мы же на запрос входного потока передаем все что записал сервер в выходной поток и читает его ответ,
 * после чего возвращаем ответ каналау, но уже в буфере) Т.о. весь протокол соедиения и вызова функций point2point
 * производится через HTTP туннель (надо отметить что все прокси стабы генерируются  стандартным образом и не чем не
 * отличаются от использования прямы сокетных или HTTP каналов)
 *
 */
public class MediatorImpl implements IMediator
{
	private static final String RSTOR = "RSTOR";
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    private IModule cmdprovider;
	private ICliConfigProvider conf_provider;


	public MediatorImpl(IModule cmdprovider, ICliConfigProvider conf_provider)
    {

        this.cmdprovider = cmdprovider;
		this.conf_provider = conf_provider;
	}
    public InputStream getInputStream() throws IOException, SysCoderEx
    {
		try
		{
			HashMap<String, byte[]> parmap = new HashMap<String, byte[]>();
			ICommandBean cmdbean=new CommandBean(RSTOR,parmap, ICommandBean.APPCLI,"");
			parmap.put(RSTOR, Base64.encode(bos.toByteArray()).getBytes());
			IAnswerBean answer = execute(cmdbean);
			return new ByteArrayInputStream(answer.getbAnswer());
		}
		catch (Exception e)
		{
			throw new IOException(e.getMessage());
		}
	}

    public OutputStream getOutputStream() throws IOException
    {
        bos.flush();
        bos.close();
        bos.reset();
        return bos;
    }

	public IAnswerBean execute(ICommandBean cmd) throws Exception
	{
		String session = conf_provider.getSession();
		if (session!=null)
		{
			cmd.setSessionId(session);
			return cmdprovider.execute(cmd);
		}
		return new AnswerBean(cmd,"",new byte[]{});
	}

}
