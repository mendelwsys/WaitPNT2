package ru.ts.conv.rmp;

import ru.ts.utils.data.InParams;
import ru.ts.utils.data.Pair;
import ru.ts.toykernel.app.xml.IXMLBuilderContext;
import ru.ts.toykernel.consts.KernelConst;
import ru.ts.toykernel.xml.IXMLBuilder;
import ru.ts.xml.IXMLObjectDesc;
import ru.ts.conv.ICompiler;
import ru.ts.conv.ConvUtils;

import java.util.List;
import java.util.HashSet;

/**
 * Конвертер  
 */
public class MPConverter
{

//  FileInputStream is = new FileInputStream("D:\\MAPDIR\\MP_TOWNS\\Rostov-na-Donu.mp"); //Файл преобразования
//  String pname ="rost"; // Имя проекта
//  FileInputStream is = new FileInputStream("D:\\MAPDIR\\MP_TOWNS\\krasnodar.mp"); //Файл преобразования
//  String pname ="krs"; // Имя проекта
//	FileInputStream is = new FileInputStream("D:\\MAPDIR\\POLY\\Moskva.mp"); //Файл преобразования
//	String pname ="msk"; // Имя проекта
//  String fwebxml = "D:\\MAPDIR\\MP\\apppart.xml";//
//  String fwebxml = "D:\\MAPDIR\\MP\\webpart.xml";//


	public static void main(String[] args) throws Exception
	{
		InParams inParams = new InParamsBaseConv();
		inParams.translateOptions(args);
		String infile=inParams.get(InParamsBaseConv.O_desc);
		String encoding=inParams.get(InParamsBaseConv.O_encoding);
		String compilername=inParams.get(InParamsBaseConv.O_compiler);

		inParams.addParam(new Pair<String,Object>(inParams.getParamNameByIx(InParamsBaseConv.O_trpardst),new HashSet()));
//------------------  Загрузка описателей необходимых объектов для генерации проекта ---------------------------------//
		IXMLBuilderContext bcontext = ConvUtils.createXMLContext(infile, encoding);
//-----------------------------  Секция вторичных параметров ---------------------------------------------------------//
		IXMLBuilder builders = bcontext.getBuilderByTagName(KernelConst.COMPILLER_TAGNAME);


		List<IXMLObjectDesc> paramlist = builders.getParamDescs();
		IXMLObjectDesc compilerdesc=null;
		for (IXMLObjectDesc param : paramlist)
			if (compilername != null && param.getObjname().equals(compilername))
			{
				compilerdesc=param;
				break;
			}

		ICompiler compiler= (ICompiler) bcontext.getBuilderByTagName(compilerdesc.getTagname()).initByDescriptor(compilerdesc, null);



		compiler.translate(bcontext,inParams);
	}

}
