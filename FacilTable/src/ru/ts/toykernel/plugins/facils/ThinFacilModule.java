package ru.ts.toykernel.plugins.facils;

import ru.ts.toykernel.plugins.IThinModule;
import ru.ts.toykernel.plugins.Listeners;
import ru.ts.utils.IOperation;

import java.util.Map;
import java.io.InputStream;
import java.io.IOException;

/**
 * ru.ts.toykernel.plugins.facils.ThinFacilModule
 */
public class ThinFacilModule extends FacilBaseModule implements IThinModule
{
	private Map<String, String> mapnames2JSelemnts;


	public String getJSSrcModuleRef(String session_id)
	{
		return "<script type=\"text/javascript\" src='getjssrc.jsp?modname="+getModuleName()+ "&sess="+session_id +"'></script>";
	}

	public String getJSSrc() throws Exception
	{
		return IOperation.getTxtContentByResFile(getClass(),"js/facils3.js","UTF8");
	}

	public String getJSInitCode(Map<String, String> params) throws IOException
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
		return  FILLREQ+ "#"+METRICREQ+"#"+OBJBYRID+"#"+OIDS+"#";
	}

	public void addModuleListener(Listeners.IModuleListener modelistener) throws Exception
	{
		throw new UnsupportedOperationException();
	}
}
