package su.org.imglab.clengine.utils;

import java.util.Hashtable;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: vladm
 * Date: 02.10.2007
 * Time: 14:56:07
 * To change this template use File | Settings | File Templates.
 */
public class InParamsProj
{
	public static final String optarr[] = {"-nfl","-nprj"};
	public static final String defarr[] =
	{
			"",
			"",
	};

	public static final int O_nfl=0;//Файл проекции
	public static final int O_nprj=1;//Имя проекции

	static public HashMap<String,String> options = new HashMap<String,String>();


	static
	{
		init();
	}

	static void init()
	{
		for (int i = 0; i < optarr.length; i++)
			options.put(optarr[i],defarr[i]);
	}


	static public void TranslateOption(String arg[])
	{
		if (arg == null)
			return;
		init();
		for (int i = 0; i < arg.length; i++)
		{
			if (arg[i] != null)
			{
				for (int j = 0; j < optarr.length; j++)
					if (arg[i].startsWith(optarr[j]))
					{
						String opt = arg[i].substring(optarr[j].length());
						options.put(optarr[j], opt);
						break;
					}
			}
		}
	}

}