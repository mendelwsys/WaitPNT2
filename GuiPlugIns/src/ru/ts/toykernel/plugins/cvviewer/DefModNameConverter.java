package ru.ts.toykernel.plugins.cvviewer;

import ru.ts.toykernel.consts.DefBaseNameConverter;
import su.mwlib.utils.Enc;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: 14.03.2009
 * Time: 19:23:59
 * Default module name converter
 */
public class DefModNameConverter extends DefBaseNameConverter
{
	private static final String ATTRNAME_NM = Enc.get("ATTRIBUTE_NAME");
	private static final String ATTRVALUE_NM = Enc.get("ATTRIBUTE_VALUE");
	private static final String CAPSOBJOPTIONS_NM =Enc.get("OBJECT_OPTIONS");
	
	public String codeAttrNm2ViewNm(String attrName)
	{
		if (attrName.equals(ModuleConst.ATTRNAME))
			return ATTRNAME_NM;
		else if (attrName.equals(ModuleConst.ATTRVALUE))
			return ATTRVALUE_NM;
		else if (attrName.equals(ModuleConst.CAPSOBJOPTIONS))
			return CAPSOBJOPTIONS_NM;
		return null;
	}

	public String codeAttrNm2StorAttrNm(String attrName)
	{
		if (
			attrName.equals(ModuleConst.ATTRNAME)
			||
			attrName.equals(ModuleConst.ATTRVALUE)
			||
			attrName.equals(ModuleConst.CAPSOBJOPTIONS)
			)
			return attrName;
		return null;
	}

	public String storAttrNm2codeAttrNm(String attrName)
	{
		if (
			attrName.equals(ModuleConst.ATTRNAME)
			||
			attrName.equals(ModuleConst.ATTRVALUE)
			||
			attrName.equals(ModuleConst.CAPSOBJOPTIONS)
			)
			return attrName;
		return null;
	}

}
