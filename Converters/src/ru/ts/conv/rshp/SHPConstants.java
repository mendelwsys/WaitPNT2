package ru.ts.conv.rshp;

/**
 * Константы для чтения файлов shp
 */
public class SHPConstants
{
	public static final String ORIG_TYPE="origtype";//Оригинальный тип объекта, прописывается в аттрибутах

	public static final int MNM=9994;
	
	//Возможные типы SHP файла	
	public static final int SHP_NULL=0;
	public static final int SHP_Point=1;
 	public static final int SHP_PolyLine=3;
	public static final int SHP_Polygon=5;
	public static final int SHP_MultiPoint=8;
	public static final int SHP_PointZ=11;
	public static final int SHP_PolyLineZ=13;
	public static final int SHP_PolygonZ=15;
	public static final int SHP_MultiPointZ=18;
	public static final int SHP_PointM=21;
	public static final int SHP_PolyLineM=23;
	public static final int SHP_PolygonM=25;
	public static final int SHP_MultiPointM=28;
	public static final int SHP_MultiPatch=31;
	


}
