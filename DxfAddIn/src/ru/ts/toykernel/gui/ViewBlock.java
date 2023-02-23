package ru.ts.toykernel.gui;

import ru.ts.toykernel.attrs.IDefAttr;
import ru.ts.toykernel.consts.KernelConst;
import ru.ts.toykernel.converters.IProjConverter;
import ru.ts.toykernel.pcntxt.IProjContext;
import ru.ts.xml.IXMLObjectDesc;
import ru.ts.factory.IObjectDesc;
import ru.ts.gisutils.algs.common.MPoint;

import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: VMendelevich
 * Date: 14.03.2012
 * Time: 17:28:38
 * Block of View
 * ru.ts.toykernel.gui.ViewBlock 
 */
public class ViewBlock implements IView
{
	protected IProjConverter converter;
	protected IProjContext projectctx;
	protected IXMLObjectDesc desc;
	protected String objName;
	//TODO Возможно сделать минусовыми а во время прорисовки слоев не использовать фильтр при таких установках
	private int width=1024*1024;
	private int height=1024*1024;

	public String getObjName()
	{
		return objName;
	}

	public Object[] init(Object... objs) throws Exception
	{
		for (Object obj : objs)
		{
			IDefAttr attr=(IDefAttr)obj;
			if (attr.getName().equalsIgnoreCase(KernelConst.OBJNAME))
				objName = (String) attr.getValue();
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
		if (attr.getName().equalsIgnoreCase(KernelConst.CONVERTER_TAGNAME))
			converter=(IProjConverter)attr.getValue();
		else if (attr.getName().equalsIgnoreCase(KernelConst.PROJCTXT_TAGNAME))
			projectctx=(IProjContext)attr.getValue();
		else if (attr.getName().equalsIgnoreCase(KernelConst.INITSZ))
		{
			try
			{
				String sz=(String)attr.getValue();
				String[] splsz = sz.split(" ");
				setSize(Integer.parseInt(splsz[0].trim()),Integer.parseInt(splsz[1].trim()));
			}
			catch (Exception e)
			{//
				e.printStackTrace();
			}
		}
		return null;
	}

	public IObjectDesc getObjectDescriptor()
	{
		return desc;
	}

	public IProjContext getProjContext() throws Exception
	{
		return projectctx;
	}

	public IViewPort getViewPort() throws Exception
	{
		return new IViewPort()
		{
			public Point getDrawSize() throws Exception
			{
				Dimension dm = getSize();
				Point rv = new Point((int) Math.ceil(dm.getWidth()), (int) Math.ceil(dm.getHeight()));
				converter.getAsShiftConverter().setViewSize(new MPoint(rv));
				return rv;
			}

			public IProjConverter getCopyConverter()
			{
				return (IProjConverter) converter.createCopyConverter();
			}

			public void setCopyConverter(IProjConverter _converer) throws Exception
			{
				converter.setByConverter(_converer);
			}

		};
	}

	public void setSize(int width, int height)
	{

		this.width = width;
		this.height = height;
	}
	public Dimension getSize()
	{
		return new Dimension(width,height);
	}
}
