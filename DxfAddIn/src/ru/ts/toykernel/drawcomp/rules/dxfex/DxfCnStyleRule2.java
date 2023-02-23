package ru.ts.toykernel.drawcomp.rules.dxfex;

import ru.ts.toykernel.drawcomp.rules.def.CommonStyle;
import ru.ts.toykernel.drawcomp.rules.def.CnStyleRuleImplEx;
import ru.ts.toykernel.drawcomp.IParamPainter;
import ru.ts.toykernel.drawcomp.painters.dxf.ArcPainter;
import ru.ts.toykernel.drawcomp.painters.dxf.InsertPainter2;
import ru.ts.toykernel.drawcomp.painters.dxf.DimPainter;
import ru.ts.toykernel.drawcomp.painters.def.DefPointTextPainter;
import ru.ts.toykernel.drawcomp.painters.def.DefScaledInscriptPainter;
import ru.ts.toykernel.consts.INameConverter;
import ru.ts.factory.IFactory;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: vladm
 * Date: 03.03.2012
 * Time: 11:55:10
 * The rule for drawing dxf elemnts, arcs, bize line, insert an so on
 * Правило для расширения рисования dxf элементов, таких как полукруги, линии безье, insert etc.
 * ru.ts.toykernel.drawcomp.rules.dxfex.DxfCnStyleRule2
 */
public class DxfCnStyleRule2 extends CnStyleRuleImplEx
{
	public static final String ARC="ARC"; //Дуга
	public static final String ARCF="ARCF";//Дуга заполненная (!!!TODO Еще не реализована!!!)
	public static final String INSERT="INSERT";//Вставка для блока отображения
	public static final String DIMENTION="DIMENTION";//Вставка для блока отображения

	protected String arc;
	protected String arcf;
	protected String insert;
	protected String dimention;


	protected void initAll(CommonStyle defStyle, Map<String, IFactory<IParamPainter>> paintersFactory, INameConverter nameConverter,String attrasname)
	{
		super.initAll(defStyle, paintersFactory, nameConverter,attrasname);
		final INameConverter l_nameConverter = getNameConverter();
		arc = l_nameConverter.codeAttrNm2StorAttrNm(ARC);
		arcf = l_nameConverter.codeAttrNm2StorAttrNm(ARCF);
		insert=l_nameConverter.codeAttrNm2StorAttrNm(INSERT);
		dimention=l_nameConverter.codeAttrNm2StorAttrNm(DIMENTION);
	}

	protected Class getDefPointTextPainter()
	{
		return DefScaledInscriptPainter.class;
	}


	protected Class getDefOtherPainter(String otype)
	{
		if (otype.equals(arc))
			return ArcPainter.class;
		else if (otype.equals(insert))
			return InsertPainter2.class;
		else  if (otype.equals(dimention))
			return InsertPainter2.class;
//			return DimPainter.class;
		return null;
	}
}