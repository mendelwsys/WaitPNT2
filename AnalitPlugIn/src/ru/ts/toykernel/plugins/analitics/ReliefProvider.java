package ru.ts.toykernel.plugins.analitics;

import ru.ts.toykernel.factory.BaseInitAble;
import ru.ts.toykernel.geom.IBaseGisObject;
import ru.ts.toykernel.geom.IGisObject;
import ru.ts.toykernel.converters.ILinearConverter;
import ru.ts.toykernel.attrs.IDefAttr;
import ru.ts.toykernel.attrs.AObjAttrsFactory;
import ru.ts.gisutils.algs.common.IMCoordinate;
import ru.ts.gisutils.algs.common.MPoint;
import ru.ts.gisutils.algs.common.MPointZM;

import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;

import su.org.ms.parsers.mathcalc.Parser;
import su.org.ms.parsers.mathcalc.IGetFormulaByName;
import su.org.ms.parsers.common.ParserException;

/**
 * Базовый класс для построения рельефа
 * ru.ts.toykernel.plugins.analitics.ReliefProvider
 */
public class ReliefProvider
		extends BaseInitAble implements IReliefProvider
{
	protected String attrformula;
	protected AObjAttrsFactory factory;
	protected Parser pr;


	public ReliefProvider()
	{
	}

	public ReliefProvider(String attrformula, AObjAttrsFactory factory, Parser pr)
	{
		this.attrformula = attrformula;
		this.factory = factory;
		this.pr = pr;
	}

	public AObjAttrsFactory getObjFactory()
	{
		return factory;
	}


	public Object init(Object obj) throws Exception
	{
		IDefAttr attr = (IDefAttr) obj;
		if (attr.getName().equalsIgnoreCase("ATTRFORMULA"))
			attrformula = (String) attr.getValue();
		else if (attr.getName().equalsIgnoreCase(ModuleConst.OBJATTRFACTORY_TAGNAME))
			factory=(AObjAttrsFactory) attr.getValue();
		return null;
	}

	public String getAttrFormula()
	{
		return attrformula;
	}

	@Override
	public void setAttrFormula(String formula) {
		this.attrformula=formula;
	}

	public IMCoordinate[] getRelief(Iterator<IBaseGisObject> baseObjects, ILinearConverter converter) throws Exception
	{
		if (pr == null)
			pr = Parser.createParser(new String[0]);
		List<IMCoordinate> rl = new LinkedList<IMCoordinate>();
		while (baseObjects.hasNext())
		{
			final IBaseGisObject iBaseGisObject = baseObjects.next();
			IGetFormulaByName f2name = new IGetFormulaByName()
			{
				public String getFormulaByName(String parName) throws ParserException
				{
					IDefAttr iDefAttr = iBaseGisObject.getObjAttrs().get(parName);
					if (iDefAttr != null)
						return (String) iDefAttr.getValue();
					throw new ParserException("Can't define paramter with name:" + parName);
				}
			};
			double calcvalue = pr.calculate(attrformula, f2name);

			MPoint pnt = ((IGisObject) iBaseGisObject).getMidlePoint();
			MPointZM npnt = new MPointZM(converter.getDstPointByPoint(pnt));
			npnt.setM(calcvalue);
			rl.add(npnt);
		}
		return rl.toArray(new IMCoordinate[rl.size()]);
	}

	public AObjAttrsFactory getFactory()
	{
		return factory;
	}

	public Parser getPr()
	{
		return pr;
	}
}
