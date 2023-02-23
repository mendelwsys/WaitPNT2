package ru.ts.toykernel.drawcomp.rules.dxfex;

import ru.ts.toykernel.drawcomp.rules.def.CnStyleRuleImpl;
import ru.ts.toykernel.drawcomp.rules.def.CommonStyle;
import ru.ts.toykernel.drawcomp.IParamPainter;
import ru.ts.toykernel.drawcomp.ILayer;
import ru.ts.toykernel.drawcomp.painters.dxf.ArcPainter;
import ru.ts.toykernel.drawcomp.painters.dxf.InsertPainter;
import ru.ts.toykernel.consts.INameConverter;
import ru.ts.toykernel.geom.IBaseGisObject;
import ru.ts.toykernel.attrs.IDefAttr;
import ru.ts.toykernel.gui.IView;
import ru.ts.factory.IFactory;

import java.util.Map;
import java.util.HashMap;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: vladm
 * Date: 03.03.2012
 * Time: 11:55:10
 * Правило для расширения рисования dxf элементов, таких как полукруги, линии безье,
 * ru.ts.toykernel.drawcomp.rules.dxfex.DxfCnStyleRule
 */
public class DxfCnStyleRule extends CnStyleRuleImpl
{

	
	public static final String ARC="arc"; //Дуга
	public static final String ARCF="arcf";//Дуга заполненная

	public static final String INSERT="INSERT";//Вставка для блока отображения
	public static final String VIEWBLOCK_TAGNAME = "viewblock"; //Блок отображения

	protected String arc;
	protected String arcf;

	protected Map<String,IView> name2view = new HashMap<String,IView>(); //Мно-во блоков, используется для отображения блоков при рисовании

	protected void initAll(CommonStyle defStyle, Map<String, IFactory<IParamPainter>> paintersFactory, INameConverter nameConverter,String attrasname)
	{
		super.initAll(defStyle, paintersFactory, nameConverter,attrasname);
		arc = this.nameConverter.codeAttrNm2StorAttrNm(ARC);
		arcf = this.nameConverter.codeAttrNm2StorAttrNm(ARCF);


	}

	public Object init(Object obj) throws Exception
	{
		IDefAttr attr = (IDefAttr) obj;
		if (attr.getName().equalsIgnoreCase(VIEWBLOCK_TAGNAME))
		{ //Создадим набор блоков отображения связанный с этим правилом
			IView view=(IView)attr.getValue();
			name2view.put(view.getObjName(),view);
		}
		else
			return super.init(obj);
		return null;
	}

	protected void otherPainter(Paint paintfill, Integer linecolor, Stroke stroke, Integer radPnt, ILayer layer, IBaseGisObject obj, Integer composite) throws Exception
	{
		String geotype = obj.getGeotype();
		if (geotype.equals(arc))
			arcPainter(paintfill, linecolor, stroke, radPnt, layer, obj, composite);
		else if (geotype.equals(INSERT))
			insPainter(paintfill, linecolor, stroke, radPnt, layer, obj, composite);
//		else if (geotype.equals(arcf))
//		{
//
//		}
		else
			throw new Exception("Unsupported object type:" + obj.getGeotype());
	}

	protected void insPainter(Paint paintfill, Integer linecolor, Stroke stroke, Integer radPnt, ILayer lr, IBaseGisObject obj, Integer composite)
			throws Exception
	{
		IParamPainter rv = null;
		getInstancePainter(paintersClass.get(obj.getGeotype()), obj.getGeotype(), InsertPainter.class);
		rv = cacheParamPainter;
		setPainterParams(rv, paintfill, linecolor, stroke, radPnt, composite);
	}

	protected void arcPainter(Paint paintfill, Integer linecolor, Stroke stroke, Integer radPnt, ILayer lr, IBaseGisObject obj, Integer composite)
			throws Exception
	{
		IParamPainter rv = null;
		getInstancePainter(paintersClass.get(obj.getGeotype()), obj.getGeotype(), ArcPainter.class);
		rv = cacheParamPainter;
		setPainterParams(rv, paintfill, linecolor, stroke, radPnt, composite);
	}
}
