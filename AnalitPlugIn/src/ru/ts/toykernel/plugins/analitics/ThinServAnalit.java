package ru.ts.toykernel.plugins.analitics;

import ru.ts.toykernel.plugins.*;
import ru.ts.utils.IOperation;

import java.util.Map;
import java.io.InputStream;


/**
 * Серверный модуль аналитики
 * (Отвечает за инициализацию и обработку команд)
 * ru.ts.toykernel.plugins.analitics.ThinServAnalit
 */
public class ThinServAnalit extends BaseAnalitModule implements IThinModule
{
	private Map<String, String> mapnames2JSelemnts;


	public IAnswerBean execute(ICommandBean cmd) throws Exception
	{
		if (cmd.getCodeActivator() == ICommandBean.BRWCLI)
		{
			performCommand(cmd);
			return new AnswerBean(cmd, ANALIT + "##", new byte[0]);
		}
		else
			return super.execute(cmd);
	}


	public String getJSSrcModuleRef(String session_id)
	{
		return "<script type=\"text/javascript\" src='getjssrc.jsp?modname="+getModuleName()+ "&sess="+session_id +"'></script>";
	}

	public String getJSSrc() throws Exception
	{
		return IOperation.getTxtContentByResFile(getClass(),"js/analitcmd.js","UTF8");
	}


	public String getJSInitCode(Map<String, String> params) throws Exception
	{
		return IOperation.getTxtContentByResFile(getClass(),"js/initcode.js","UTF8");
	}

	public void initJSParams(Map<String, String> mapnames2JSelemnts)
	{
		this.mapnames2JSelemnts = mapnames2JSelemnts;
	}

	public InputStream getResourceStreamByParemters(String name, String typename)
	{
		return null;
	}

	public String getCommands()
	{
		return ANALIT + "#";
	}

	public void addModuleListener(Listeners.IModuleListener modelistener) throws Exception
	{
		throw new UnsupportedOperationException();
	}
}
