package ru.ts.toykernel.loaders;

import ru.ts.factory.IInitAble;
import ru.ts.factory.IObjectDesc;
import ru.ts.gisutils.common.Sys;
import ru.ts.xml.IXMLObjectDesc;
import ru.ts.toykernel.attrs.IDefAttr;
import ru.ts.toykernel.consts.KernelConst;

import java.net.URLClassLoader;
import java.net.URL;
import java.net.URLStreamHandlerFactory;

/**
 *  Initable class loader
 * ru.ts.toykernel.loaders.UrlLoader
 *
 */
public class UrlLoader extends URLClassLoader  implements IInitAble
{
	protected IXMLObjectDesc desc;
	protected String ObjName;

	public UrlLoader()
	{
		this(new URL[]{});
	}

	public UrlLoader(URL[] urls)
	{
		super(urls);
	}

	public UrlLoader(URL[] urls, ClassLoader parent)
	{
		super(urls, parent);
	}

	public UrlLoader(URL[] urls, ClassLoader parent, URLStreamHandlerFactory factory)
	{
		super(urls, parent, factory);
	}

	public String getObjName()
	{
		return ObjName;
	}

	public Object[] init(Object... objs) throws Exception
	{
		for (Object obj : objs)
		{
			IDefAttr attr=(IDefAttr)obj;
			if (attr.getName().equalsIgnoreCase(KernelConst.OBJNAME))
				ObjName = (String) attr.getValue();
			else if (attr.getName().equalsIgnoreCase(KernelConst.DESCRIPTOR))
				this.desc=(IXMLObjectDesc)attr.getValue();
			else
				init(obj);
		}
		return null;
	}

	public Object init(Object obj) throws Exception
	{
		IDefAttr attr=(IDefAttr)obj;
		String spec = attr.getValue().toString();
		if (spec.indexOf("./")>0)
		{
			String dir=System.getProperty("user.dir");
			spec=spec.replace("./",dir+"/");
		}
		addURL(new URL(spec));
		return null;
	}

	public IObjectDesc getObjectDescriptor()
	{
		return desc;
	}

}
