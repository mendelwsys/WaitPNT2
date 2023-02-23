package ru.ts.toykernel.plugins.consts;

import ru.ts.toykernel.consts.DefNameConverter;
import ru.ts.toykernel.consts.KernelConst;
import su.mwlib.utils.Enc;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: 07.03.2009
 * Time: 19:18:57
 * Extends known attribute visualization (TODO Каждый модуль будет предоставлять эту конвертацию)
 */
public class DefNameConverter2 extends DefNameConverter
{
	private static final String LAYERNAMEHNAME = Enc.get("LAYER_NAME");
	private static final String VISIBLEHNAME = Enc.get("VISIBILITY");

	private static final String ATTRIBUTEHNAME = Enc.get("OBJECT_NAME");
	private static final String ATTRIMGNAME = Enc.get("OBJECT_PHOTO");

	public String codeAttrNm2ViewNm(String attrName)
	{
		if (attrName.equals(KernelConst.ATTR_CURVE_NAME))
			return ATTRIBUTEHNAME;
		if (attrName.equals(KernelConst.ATTR_IMG_REF))
			return ATTRIMGNAME;
		if (attrName.equals(KernelConst.LAYER_NAME))
			return LAYERNAMEHNAME;
		if (attrName.equals(KernelConst.LAYER_VISIBLE))
			return VISIBLEHNAME;
		return super.codeAttrNm2ViewNm(attrName);
	}

}
