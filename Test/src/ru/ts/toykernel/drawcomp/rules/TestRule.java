package ru.ts.toykernel.drawcomp.rules;

import ru.ts.toykernel.drawcomp.rules.def.CommonStyle;
import ru.ts.toykernel.drawcomp.rules.def.stream.CnSerialDrawRule;
import ru.ts.toykernel.drawcomp.painters.def.symbols.StarSymbol;
import ru.ts.toykernel.drawcomp.IParamPainter;
import ru.ts.toykernel.drawcomp.ILayer;
import ru.ts.toykernel.geom.IBaseGisObject;
import ru.ts.toykernel.attrs.IDefAttr;
import ru.ts.toykernel.consts.INameConverter;
import ru.ts.factory.IFactory;

import java.awt.*;
import java.util.Map;

/**
 * Тестовое правило, для спец рисования слова москва
 */
public class TestRule extends CnSerialDrawRule
{
	public static final String RULETYPENAME ="TEST_RL1";
	public TestRule()
	{
	}

	public TestRule(CommonStyle defStyle)
	{
		super(defStyle);
	}

	public TestRule(CommonStyle defStyle, Font ft)
	{
		super(defStyle, ft);
	}

	public TestRule(CommonStyle defStyle, Font ft, Map<String, IFactory<IParamPainter>> paintersClass, INameConverter nameConverter)
	{
		super(defStyle, ft, paintersClass, nameConverter);
	}

	public TestRule(CommonStyle defStyle, Map<String, IFactory<IParamPainter>> paintersClass, INameConverter storNm2attrNm)
	{
		super(defStyle, paintersClass, storNm2attrNm);
	}

	public String getRuleType()
	{
		return RULETYPENAME;
	}

	protected void pointPainter(Paint paintfill, Integer linecolor, Stroke stroke, Integer radPnt, ILayer lr, IBaseGisObject obj, Integer composite, Image pointImg, Point central) throws Exception
	{
		//cacheParamPainter=new StarSymbol(new Color(0xFFAA3030,true),new Color(linecolor),new BasicStroke(1),10);
		//cacheParamPainter=new NumberedRect("7",new Color(0xFFCCCC30),new Color(linecolor),new Color(0xFFCC2020),12,12);
		IDefAttr defName = null;
		if (attrasname!=null)
			defName = obj.getObjAttrs().get((nameConverter.codeAttrNm2StorAttrNm(attrasname)));
		if (defName!=null && defName.getValue()!=null)
		{
			String name = defName.getValue().toString();
			if (name.length()>0 && name.toLowerCase().contains("москва"))
			{
				cacheParamPainter=new StarSymbol(new Color(0xFFDD50DD),new Color(linecolor),new BasicStroke(1),10);
				return;
			}
		}
		if (cacheParamPainter instanceof StarSymbol)
			cacheParamPainter=null;
		super.pointPainter(paintfill,linecolor, stroke,radPnt, lr, obj, composite, null, central);
	}
}
