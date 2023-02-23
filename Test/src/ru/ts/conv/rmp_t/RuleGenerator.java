package ru.ts.conv.rmp_t;

import ru.ts.utils.data.Pair;
import ru.ts.toykernel.attrs.def.DefAttrImpl;
import ru.ts.toykernel.xml.def.ParamDescriptor;
import ru.ts.toykernel.drawcomp.rules.def.CnStyleRuleImpl;
import ru.ts.toykernel.drawcomp.rules.def.CommonStyle;
import ru.ts.xml.IXMLObjectDesc;
import ru.ts.conv.rmp.SheemLoader;

import java.util.List;
import java.util.Arrays;

/**
 * Генератор описателей правил
 */
public class RuleGenerator extends Generator
{
	public static final String DEF_LORANGE = "-1";
	public static final String DEF_HIRANGE = "-1";
	public static final String DEF_COLORLINE = "0";
	public static final String DEF_COLORFILL = "0";
	public static final String DEF_LINESTYLE = "0";
	public static final String DEF_LINETHICKNESS = "1";

	public RuleGenerator()
	{
		super("rule", CnStyleRuleImpl.class);
	}

	public IXMLObjectDesc getRuleDesc(ISheemProvider analizer, Pair<String, String> rgn2type,Integer level)
	{
		Pair<String,String> pr=analizer.getHiLoRange(level);
		if (pr==null)
			pr= new Pair<String,String>(DEF_HIRANGE,DEF_LORANGE);

		String val;
		String colorLine = ((val=analizer.getScheemValByPair(rgn2type, SheemLoader.COLOR_LINE))==null)?DEF_COLORLINE:val;
		String colorFill =((val=analizer.getScheemValByPair(rgn2type,SheemLoader.COLOR_FILL))==null)?DEF_COLORFILL:val;
		String lineStyle = ((val=analizer.getScheemValByPair(rgn2type,SheemLoader.LINE_STYLE))==null)?DEF_LINESTYLE:val;
		String lineThickness =((val=analizer.getScheemValByPair(rgn2type,SheemLoader.LINE_THICKNESS))==null)?DEF_LINETHICKNESS:val;

		List ruleparam = Arrays.asList
		(
				new DefAttrImpl(CommonStyle.HI_RANGE, pr.first),
				new DefAttrImpl(CommonStyle.LOW_RANGE, pr.second),
				new DefAttrImpl(CommonStyle.COLOR_LINE, colorLine),
				new DefAttrImpl(CommonStyle.COLOR_FILL, colorFill),
				new DefAttrImpl(CommonStyle.LINE_STYLE, lineStyle),
				new DefAttrImpl(CommonStyle.LINE_THICKNESS, lineThickness)
		);

		return new ParamDescriptor(getTag(), "R_" + analizer.getStorNameByPair(level, rgn2type),
				getClassName(), null, ruleparam, -1);
	}

}
