package ru.ts.toykernel.filters;

import ru.ts.toykernel.factory.BaseInitAble;
import ru.ts.toykernel.geom.IBaseGisObject;
import ru.ts.toykernel.attrs.IDefAttr;
import ru.ts.factory.IParam;

import su.org.ms.parsers.mathcalc.Parser;
import su.org.ms.parsers.mathcalc.IGetFormulaByName;
import su.org.ms.parsers.common.ParserException;

/**
 * Фильтр по формуле
 * ru.ts.toykernel.filters.DefSelFilterByFormula
 */

public class DefSelFilterByFormula extends BaseInitAble implements IFormulaFilter
{
	public static final String TYPENAME = "DefSelFilterByFormula";

	public String formula; //Формула
	private Parser pr;
	private boolean negate=false;

	public DefSelFilterByFormula() throws Exception
	{

	}

	public DefSelFilterByFormula(String formula) throws Exception
	{
		this.formula=formula;
	}

	public String getFormula()
	{
		return formula;
	}

	public void setFormula(String formula)
	{
		this.formula = formula;
	}

	public Object init(Object obj) throws Exception
	{
		IParam attr=(IParam)obj;
		if (attr.getName().equalsIgnoreCase("FORMULA"))
			formula = (String) attr.getValue();
		else if (attr.getName().equalsIgnoreCase("ISNEGATE"))
			negate = Boolean.valueOf((String) attr.getValue());
		return null;
	}

	public boolean acceptObject(final IBaseGisObject obj) throws Exception
	{
		if (pr == null)
			pr = Parser.createParser(new String[0]);

		IGetFormulaByName f2name = new IGetFormulaByName()
		{
			public String getFormulaByName(String parName) throws ParserException
			{
				IDefAttr iDefAttr = obj.getObjAttrs().get(parName);
				if (iDefAttr != null)
					return (String) iDefAttr.getValue();
				throw new ParserException("Can't define paramter with name:" + parName);
			}
		};
		double calcvalue = 0;
		try
		{
			calcvalue = pr.calculate(formula, f2name);
		}
		catch (Exception e)
		{//
			System.out.println("Calc Error");
		}
		return negate ^ calcvalue!=0;
	}

	public String getTypeName()
	{
		return TYPENAME;
	}
}