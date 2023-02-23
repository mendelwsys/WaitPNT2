package ru.ts.conv.rdxf;

import ru.ts.utils.data.InParams;
import ru.ts.utils.data.Pair;
import ru.ts.conv.rshp.InParamsBaseConv;
import ru.ts.conv.ConvUtils;
import ru.ts.conv.ICompiler;
import ru.ts.toykernel.app.xml.IXMLBuilderContext;
import ru.ts.toykernel.xml.IXMLBuilder;
import ru.ts.toykernel.consts.KernelConst;
import ru.ts.xml.IXMLObjectDesc;

import java.util.HashSet;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: vladm
 * Date: 25.02.2012
 * Time: 10:14:32
 * Конвертер из dxf формата
 */
public class DXFConverter
{
	public static void main(String[] args) throws Exception
	{
		InParams inParams = new ru.ts.conv.rshp.InParamsBaseConv();
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
