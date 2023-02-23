package ru.ts.toykernel.plugins.analitics;

import ru.ts.toykernel.attrs.AObjAttrsFactory;
import ru.ts.toykernel.consts.KernelConst;
import ru.ts.factory.IInitAble;
import ru.ts.factory.IObjectDesc;
import ru.ts.factory.IParam;
import ru.ts.xml.IXMLObjectDesc;

import java.util.List;
import java.util.LinkedList;

/**
 * Фабрика аттрибутов объектов, которую надо переопределять для того
 * что бы задать аттрибуты над которыми будет производится вычисления параметров отображение аналитики
 */
 public abstract class AnalitObjAttrsFactory  extends AObjAttrsFactory implements IInitAble
{
	protected String ObjName;
	protected IXMLObjectDesc desc;

	public IObjectDesc getObjectDescriptor()
	{
		return desc;
	}

	public String getObjName()
	{
		return ObjName;
	}

	public Object[] init(Object... objs) throws Exception
	{
		List olds= new LinkedList();
		for (Object obj : objs)
		{
			IParam attr=(IParam)obj;
			if (attr.getName().equalsIgnoreCase(KernelConst.OBJNAME))
				ObjName = (String) attr.getValue();
			else if (attr.getName().equalsIgnoreCase(KernelConst.DESCRIPTOR))
				this.desc=(IXMLObjectDesc)attr.getValue();
			else
			{
				Object oobj=init(obj);
				if (oobj!=null)
					olds.add(oobj);
			}
		}
		return olds.toArray(new Object[olds.size()]);
	}

	public Object init(Object obj) throws Exception
	{
		return null;
	}
}
