package ru.ts.mapkernel.geom;

import su.mwlib.utils.Enc;

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
	public static final String POINT = "Point";
	public static final String LINESTRING = "LineString";
	public static final String LINEARRING = "LinearRing";
	public static final String DEFAULTYPE = "LineString";
	public static final String ATTR_CURVE_NAME = "CURVE_NAME";
	public static final String ATTR_INAREA_MSG = "INAREA_MSG";
	public static final String ATTR_OUTAREA_MSG = "OUTAREA_MSG";
	public static final String ATTR_CURVE_BOUND = "CURVE_BOUND"; //Код станции в формате код дороги_код нси#контрольный разряд
	public static final String ATTR_CURVE_BOUNDE = "CURVE_BOUND_E"; //Код станции экспресс
	public static final String ATTR_CURVE_ISCALC = "CURVE_CALC"; //Станция является вычисленной
	public static final String ATTR_CURVE_AMBIGUITY = "CURVE_AMBIGUITY";//Станция имеет не уникальное имя
	//Стили прорисовок
	public static final String ATTR_COLOR_LINE = "COLOR_LINE";
	public static final String ATTR_LINE_STYLE = "LINE_STYLE";
	public static final String ATTR_COLOR_FILL = "COLOR_FILL";
	public static final String ATTR_LINE_THICKNESS = "LINE_THICKNESS";
	public static final String ATTR_SCALE_THICKNESS = "ATTR_SCALE_THICKNESS";
	public static final String ATTR_CURVE_CUSTOM = "CURVE_CUSTOM";//С этого аттрибута начинаются все вычисленные станции
    public static final String ATTR_LAYER_CUSTOM = "LAYER_CUSTOM";//С этого аттрибута начинаются все вычисленные атрибуты для слоев
	public static String[] types = {LINESTRING, LINEARRING, POINT};//Типы гео объектов

    public static class Curve
	{
		public static final String ATTRNAME = "ATTRNAME";
		public static final String ATTRVALUE = "ATTRVALUE";

		public static HashMap<String, String> mapCodePair = new HashMap<String, String>();

		static
		{
			mapCodePair.put(AttrsConst.ATTR_CURVE_NAME, Enc.get("OBJECT_NAME"));
			mapCodePair.put(AttrsConst.ATTR_INAREA_MSG, Enc.get("ZONE_ENTRY_EVENT"));
			mapCodePair.put(AttrsConst.ATTR_OUTAREA_MSG, Enc.get("ZONE_EXIT_EVENT"));

			mapCodePair.put(AttrsConst.ATTR_CURVE_BOUND, Enc.get("STATION_CODE"));
			mapCodePair.put(AttrsConst.ATTR_CURVE_AMBIGUITY, Enc.get("NOT_A_UNIQUE_NAME"));
		}
	}
}
