package su.org.imglab.clengine;

import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: vladm
 * Date: 12.09.2008
 * Time: 15:45:11
 * Константы
 */
public class AttrsConst
{
//	public List<Map<String, Object>> getCurveAttrs() TODO код для представления системных атрибутов в читабельном виде
//	{
//
//		List<Map<String, Object>> retVal = new LinkedList<Map<String, Object>>();
//		try
//		{
//			IAttrs attrs = getObjAttrs();
//			for (IDefAttr iDefAttr : attrs.values())
//			{
//				Map<String, Object> row = new HashMap<String, Object>();
//				row.put(AttrsConst.Curve.ATTRNAME, iDefAttr.getName());
//				row.put(AttrsConst.Curve.ATTRVALUE, iDefAttr.getValue());
//				retVal.add(row);
//			}
//		}
//		catch (Exception e)
//		{
//			e.printStackTrace();
//		}
//		return retVal;
//	}


	public static final String ATTR_INAREA_MSG = "INAREA_MSG";
	public static final String ATTR_OUTAREA_MSG = "OUTAREA_MSG";
	
	public static final String ATTR_CURVE_BOUND = "CURVE_BOUND"; //Код станции в формате код дороги_код нси#контрольный разряд
	public static final String ATTR_CURVE_BOUNDE = "CURVE_BOUND_E"; //Код станции экспресс
	public static final String ATTR_CURVE_ISCALC = "CURVE_CALC"; //Станция является вычисленной
	public static final String ATTR_CURVE_AMBIGUITY = "CURVE_AMBIGUITY";//Станция имеет не уникальное имя


	public static final String ATTR_CURVE_CUSTOM = "CURVE_CUSTOM";//С этого аттрибута начинаются все вычисленные станции
    public static final String ATTR_LAYER_CUSTOM = "LAYER_CUSTOM";//С этого аттрибута начинаются все вычисленные атрибуты для слоев

    public static class Curve
	{
		public static final String ATTRNAME = "ATTRNAME";
		public static final String ATTRVALUE = "ATTRVALUE";

		public static HashMap<String, String> mapCodePair = new HashMap<String, String>();

		static
		{
			mapCodePair.put(AttrsConst.ATTR_INAREA_MSG, "Событие входа в зону");
			mapCodePair.put(AttrsConst.ATTR_OUTAREA_MSG, "Событие выхода из зоны");

			mapCodePair.put(AttrsConst.ATTR_CURVE_BOUND, "Код станции");
			mapCodePair.put(AttrsConst.ATTR_CURVE_AMBIGUITY, "Не уникальное имя");
		}
	}
}
