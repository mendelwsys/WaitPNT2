package ru.ts.toykernel.proj;

import ru.ts.toykernel.factory.BaseInitAble;
import ru.ts.toykernel.conf.ServerConfigProvider;
import ru.ts.utils.data.Pair;

import java.util.List;
import java.util.Arrays;
import java.util.LinkedList;

/**
 * Stab Config provider
 *
 * 
 */
public class StabConfigRovider extends BaseInitAble implements ICliConfigProvider
{

	public static ServerConfigProvider provider;
	public String session;

	public StabConfigRovider()
	{
		System.out.println("provider = " + provider);
	}

	public static ServerConfigProvider getProvider() //TODO Тестовая функция уйдет
	{
		return StabConfigRovider.provider;
	}

	public static void setProvider(ServerConfigProvider serverver) //TODO Тестовая функция уйдет
	{
		StabConfigRovider.provider = serverver;
	}

	public String getSession()
	{
		return session;
	}

	public List<String> getProjects() throws Exception
	{

		return new LinkedList<String>(Arrays.asList(new String[]{"TestApp"})); 
	}

	public List<String> getApplications(String projname) throws Exception
	{
		return provider.getApplications();
	}

	public Pair<Boolean,String> getDescriptorByAppInfo(String projname, String appname) throws Exception
	{
		return provider.getDescriptorByAppInfo(appname);
	}

	public String openSession(String projname,String appInfo) throws Exception
	{
		return session=provider.openSession(appInfo);
	}

	public Object init(Object obj) throws Exception
	{
		System.out.println("Call init");
		return null;
	}
}
