package ru.ts.conv.reg;

import ru.ts.xml.IXMLObjectDesc;
import ru.ts.toykernel.consts.KernelConst;
import ru.ts.toykernel.attrs.def.DefAttrImpl;
import ru.ts.factory.IParam;
import ru.ts.utils.data.InParams;

import java.util.List;

/**
 * Генератор для веба
 * ru.ts.conv.reg.RegWebAppGenerator
 */
public class RegWebAppGenerator extends RegAppGenerator
{

	protected String storprov="T_STORPOJPROV";
	protected String convprov="T_CONVPROV";

	protected void generateSuffix(StringBuffer sbuffer)
			throws Exception
	{
		String webtemlplugname = "mmap";

		InParamsConv inparams = new InParamsConv();
		inparams.translateOptions(this.inparams.getArgs());
//Сначала прасим конфигурационный файл
		for (IParam addparam : addparams)
		{
			if (addparam.getName().equalsIgnoreCase(InParamsConv.getOName(InParamsConv.O_wnmmap).substring(1)))
				webtemlplugname = (String) addparam.getValue();
			else if (addparam.getName().equalsIgnoreCase(InParamsConv.getOName(InParamsConv.O_convprov).substring(1)))
				convprov = (String) addparam.getValue();
			else if (addparam.getName().equalsIgnoreCase(InParamsConv.getOName(InParamsConv.O_storprov).substring(1)))
				storprov = (String) addparam.getValue();
		}

//Потом смотрим не изменил ли user значения полей в этом файле
		String val = null;
		if ((val = inparams.get(InParamsConv.O_wnmmap)) != null && val.length() > 0)
			webtemlplugname = val;
		if ((val = inparams.get(InParamsConv.O_convprov)) != null && val.length() > 0)
			convprov = val;
		if ((val = inparams.get(InParamsConv.O_storprov)) != null && val.length() > 0)
			storprov = val;


		String sprovName = pname +"_SPROV";
		String cprovName = pname +"_CPROV";


		IXMLObjectDesc stroprovdesc= createTemplateDescByName(bcontext, KernelConst.STORPROVIDER_TAGNAME,storprov);
		sbuffer.append("<").append(stroprovdesc.getTagname()).append("s>\n");
		stroprovdesc.setObjname(sprovName);
		List<IParam> lpstorprov = stroprovdesc.getParams();
		lpstorprov.add(new DefAttrImpl(null,projconvdesc));
		lpstorprov.add(new  DefAttrImpl(null,projcntx));
		sbuffer.append(stroprovdesc.getXMLDescriptor("\t"));
		sbuffer.append("</").append(stroprovdesc.getTagname()).append("s>\n");

		IXMLObjectDesc convprovdesc= createTemplateDescByName(bcontext,KernelConst.CONVPROVIDER_TAGNAME,convprov);
		sbuffer.append("<").append(convprovdesc.getTagname()).append("s>\n");
		convprovdesc.setObjname(cprovName);
		convprovdesc.getParams().add(new  DefAttrImpl(null,servcnvdesc));
		sbuffer.append(convprovdesc.getXMLDescriptor("\t"));
		sbuffer.append("</").append(convprovdesc.getTagname()).append("s>\n");


		IXMLObjectDesc selstorage= createTemplateDescByName(bcontext,KernelConst.STORAGE_TAGNAME,"SelectStorage");
		generateXML(sbuffer, selstorage);

		IXMLObjectDesc rule_in_desc= createTemplateDescByName(bcontext,KernelConst.RULE_TAGNAME,"rule_in");
		generateXML(sbuffer, rule_in_desc);

		IXMLObjectDesc rule_other_desc= createTemplateDescByName(bcontext,KernelConst.RULE_TAGNAME,"rule_other");
		generateXML(sbuffer, rule_other_desc);

		IXMLObjectDesc layer_other_desc= createTemplateDescByName(bcontext,KernelConst.LAYER_TAGNAME,"layer_other");
		generateXML(sbuffer, layer_other_desc);

		IXMLObjectDesc layer_in_desc= createTemplateDescByName(bcontext,KernelConst.LAYER_TAGNAME,"layer_in");
		generateXML(sbuffer, layer_in_desc);

		IXMLObjectDesc projdesc= createTemplateDescByName(bcontext,KernelConst.PROJCTXT_TAGNAME,"SellAreaProj");
		generateXML(sbuffer, projdesc);

		IXMLObjectDesc selectdesc= createTemplateDescByName(bcontext,KernelConst.STORPROVIDER_TAGNAME,"SELECTPROV");
		if (selectdesc!=null)
		{
			sbuffer.append("<").append(selectdesc.getTagname()).append("s>\n");
			List<IParam> lpselect = selectdesc.getParams();
			lpselect.add(new  DefAttrImpl(null,projconvdesc));
			sbuffer.append(selectdesc.getXMLDescriptor("\t"));
			sbuffer.append("</").append(selectdesc.getTagname()).append("s>\n");
		}


		IXMLObjectDesc mmapdesc= createTemplateDescByName(bcontext,KernelConst.PLUGIN_TAGNAME,webtemlplugname);
		sbuffer.append("<").append(mmapdesc.getTagname()).append("s>\n");
		List<IParam> lpmmap = mmapdesc.getParams();
		lpmmap.add(new  DefAttrImpl("bindp",scaleBpoint[1].x+" "+scaleBpoint[1].y));
		lpmmap.add(new  DefAttrImpl(null,convprovdesc));
		lpmmap.add(new  DefAttrImpl(null,stroprovdesc));
		lpmmap.add(new  DefAttrImpl(null,selectdesc));

		sbuffer.append(mmapdesc.getXMLDescriptor("\t"));
		sbuffer.append("</").append(mmapdesc.getTagname()).append("s>\n");

		IXMLObjectDesc smoddesc= createTemplateDescByName(bcontext,KernelConst.PLUGIN_TAGNAME,"selectmod");
		generateXML(sbuffer, smoddesc);
	}

	private void generateXML(StringBuffer sbuffer, IXMLObjectDesc smoddesc)
			throws Exception
	{
		if (smoddesc!=null)
		{
			sbuffer.append("<").append(smoddesc.getTagname()).append("s>\n");
			sbuffer.append(smoddesc.getXMLDescriptor("\t"));
			sbuffer.append("</").append(smoddesc.getTagname()).append("s>\n");
		}
	}

	private static class InParamsConv extends InParamsBaseConv
	{
		public static final int O_wnmmap = InParamsBaseConv.optarr.length;//Файл
		public static final int O_storprov =O_wnmmap+1;//Имя описателя провайдера для проекта
		public static final int O_convprov =O_wnmmap+2;//Имя описателя провайдера для конвертера
		public static String optarr[] =
				{
						"-wnmmap",
						"-storprov",
						"-convprov"
				};
		public static  String defarr[] =
				{
						"",  //Здесь указывается то что будет возвращать конвертер параметров если пользователь ничего не укажет
						"",
						""
				};
		public static String comments[] =
				{
						"name of mmap PlugIn",
						"",
						""
				};

		static
		{
			optarr= InParams.mergeArrays(InParamsBaseConv.optarr,optarr);
			defarr= InParams.mergeArrays(InParamsBaseConv.defarr,defarr);
			comments= InParams.mergeArrays(InParamsBaseConv.comments,comments);
		}

		InParamsConv()
		{
			setArrays(optarr,defarr,comments);
		}

		static public String getOName(int optIdx)
		{
			try
			{
				return optarr[optIdx];
			}
			catch (Exception e)
			{//
			}
			return null;
		}


	}

}