package ru.ts.conv.utils;

import ru.ts.utils.data.InParams;
import ru.ts.utils.data.Pair;
import ru.ts.toykernel.app.xml.IXMLBuilderContext;
import ru.ts.toykernel.xml.IXMLBuilder;
import ru.ts.conv.ConvUtils;
import ru.ts.xml.IXMLObjectDesc;
import ru.ts.factory.IParam;

import java.util.*;
import java.io.FileOutputStream;

/**
 * Created by IntelliJ IDEA.
 * User: vladm
 * Date: 04.07.2011
 * Time: 12:23:05
 * Unite two projects into one
 */
public class UniteProjects
{

	public static void main(String[] args) throws Exception
	{
		InParamsApp params = new InParamsApp();
		params.translateOptions(args);

		String infile1 = params.get(InParamsApp.optarr[InParamsApp.O_in1]);
		String infile2 = params.get(InParamsApp.optarr[InParamsApp.O_in2]);
		String out = params.get(InParamsApp.optarr[InParamsApp.O_out]);
		String encoding = params.get(InParamsApp.optarr[InParamsApp.O_encoding]);
		String[] remtags = params.get(InParamsApp.optarr[InParamsApp.O_remtags]).split(",|;");
		String[] remobjs = params.get(InParamsApp.optarr[InParamsApp.O_remobjm]).split(",|;");


		Map<String,Collection<Pair<String,Pair<String,String>>>> tag2union_pair = new HashMap<String,Collection<Pair<String,Pair<String,String>>>>();


		{
			String[] sunionobjs = params.get(InParamsApp.optarr[InParamsApp.O_unionobj]).split(",|;");
			for (int i = 0; i < sunionobjs.length; i+=4)
			{

				String tag =  sunionobjs[i];
				Collection<Pair<String, Pair<String, String>>> ll = tag2union_pair.get(tag);
				if (ll==null)
					tag2union_pair.put(tag,ll= new LinkedList());
				ll.add(new Pair(sunionobjs[i+3],new Pair(sunionobjs[i+1], sunionobjs[i+2])));
			}
		}



		Set<String> sremtags = new HashSet<String>(Arrays.asList(remtags));
		Set<String> sremobjs = new HashSet<String>(Arrays.asList(remobjs));


		IXMLBuilderContext bcontext1 = ConvUtils.createXMLContext(infile1, encoding);
		IXMLBuilderContext bcontext2 = ConvUtils.createXMLContext(infile2, encoding);




		Collection<String> tags2 = bcontext2.getTagNames();
		for (String tag2 : tags2)
		{
			System.out.println("process tag2 = " + tag2);
			if (!sremtags.contains(tag2))
			{
				List<IXMLObjectDesc> desc1 = getDescriptors(bcontext1, tag2);
				List<IXMLObjectDesc> desc2 = getDescriptors(bcontext2, tag2);

				for (IXMLObjectDesc ixmlObjectDesc : desc2)
				{
					String addobjname = ixmlObjectDesc.getObjname();
					if (!sremobjs.contains(ixmlObjectDesc.getObjname()) && getDescByName(desc1, addobjname) == null)
						desc1.add(ixmlObjectDesc);
					else
						System.out.println("addobjname = " + addobjname + " tag2 " + tag2 + " skipped");
				}
			}


//			if (tag2.equalsIgnoreCase("projcont"))
//			{
//				String objname = "tver";
//				Set<String> uniontags = new HashSet<String>(Arrays.asList("layer"));
//				unionTags(bcontext1, bcontext2, tag2, objname, uniontags);
//			}
		}

//Union the descriptors
		for (String tagname : tag2union_pair.keySet())
		{
			Collection<Pair<String,Pair<String,String>>> ll = tag2union_pair.get(tagname);
			for (Pair<String, Pair<String, String>> tag2objspair : ll)
			{
				String objname1=tag2objspair.second.first;
				String objname2=tag2objspair.second.second;

				objname1 = getObjectName(bcontext1, tagname, objname1);
				if (objname2==null || objname2.length()==0)
					objname2=objname1;
				else
					objname2 = getObjectName(bcontext1, tagname, objname2);
				Set<String> uniontags = new HashSet<String>(Arrays.asList(tag2objspair.first));
				unionTags(bcontext1, bcontext2, tagname, objname1,objname2,uniontags);

			}
		}




		String xml = bcontext1.getFullXML(encoding, true);
		FileOutputStream fw = null;
		try
		{

			fw = new FileOutputStream(out);
			fw.write(xml.getBytes(encoding));
		}
		finally
		{
			if (fw != null)
				fw.close();
		}


	}

	private static String getObjectName(IXMLBuilderContext bcontext, String tagname, String objname)
	{
		if (objname.startsWith("$"))
		{
			int ix= Integer.parseInt(objname.substring(1));
			List<IXMLObjectDesc> desc1 = getDescriptors(bcontext, tagname);
			objname=desc1.get(ix).getObjname();
		}
		return objname;
	}

	private static void unionTags(IXMLBuilderContext bcontext1, IXMLBuilderContext bcontext2, String tag, String objname, Set<String> uniontags)
	{
		unionTags(bcontext1, bcontext2, tag, objname, objname, uniontags);
	}

	private static void unionTags(IXMLBuilderContext bcontext1, IXMLBuilderContext bcontext2, String tag, String objname1, String objname2, Set<String> uniontags)
	{
		List<IXMLObjectDesc> desc1 = getDescriptors(bcontext1, tag);
		IXMLObjectDesc mn1 = getDescByName(desc1, objname1);
		List<IParam> pars1 = mn1.getParams();

		List<IXMLObjectDesc> desc2 = getDescriptors(bcontext2, tag);
		IXMLObjectDesc mn2 = getDescByName(desc2, objname2);

		List<IParam> pars2 = mn2.getParams();
		for (IParam par : pars2)
			if (uniontags.contains(par.getName()))
				pars1.add(par);
	}

	private static IXMLObjectDesc getDescByName(List<IXMLObjectDesc> desc, String objname)
	{
		IXMLObjectDesc mn = null;
		for (IXMLObjectDesc ixmlObjectDesc : desc)
			if (ixmlObjectDesc.getObjname().equalsIgnoreCase(objname))
			{
				mn = ixmlObjectDesc;
				break;
			}
		return mn;
	}

	private static List<IXMLObjectDesc> getDescriptors(IXMLBuilderContext bcontext, String tag)
	{
		IXMLBuilder builder1 = bcontext.getBuilderByTagName(tag);
		return builder1.getParamDescs();
	}

	public static class InParamsApp extends InParams
	{
		// имена используемых параметров (префиксы в командной строке)
		public static final String optarr[] = {"-in1", "-in2", "-out", "-encoding", "-remtags",
				"-remobjm","-unionobj"};
		// значения параметров по умолчанию
		public static final String defarr[] =
				{
						"G:\\BACK_UP\\$D\\MAPDIR\\TVER\\TVERMAP\\tver.xml", //Тестовый вариант
						"G:\\BACK_UP\\$D\\MAPDIR\\TVER\\TVERAUTO\\tver.xml",//Тестовый вариант
						"G:\\BACK_UP\\$D\\MAPDIR\\TVER\\tverall.xml",//Тестовый вариант

//						"G:\\BACK_UP\\$D\\MAPDIR\\MSK\\MSKMAP\\msk.xml",//Тестовый вариант
//						"G:\\BACK_UP\\$D\\MAPDIR\\MSK\\MSKAUTO\\msk.xml", //Тестовый вариант
//						"G:\\BACK_UP\\$D\\MAPDIR\\MSK\\mskall.xml",//Тестовый вариант


						"WINDOWS-1251", //Система кодирования входного файла по умолчанию
						"metainfo,transformer,converter,projcont,viewctrl,application",
						"MAIN_STORAGE",
						"storage,MAIN_STORAGE,,storage,projcont,$0,,layer"
				};

		public static final int O_in1 = 0;//Проект 1
		public static final int O_in2 = 1;//Проект 2
		public static final int O_out = 2;//Результат объединения двух проектов
		public static final int O_encoding = 3;//Система кодирования
		public static final int O_remtags = 4;//Не включать перечисленные  через запятую таги из проекта in2 в проект out

		public static final int O_remobjm = 5;//Не включать перечисленные  через запятую объектыиз проекта in2 в проект out
		public static final int O_unionobj =6;//Не включать перечисленные  через запятую объектыиз проекта in2 в проект out

		public InParamsApp()
		{
			super(optarr, defarr);
		}
	}
}
