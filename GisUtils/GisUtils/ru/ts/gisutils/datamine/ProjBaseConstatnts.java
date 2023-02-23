package ru.ts.gisutils.datamine;

public class ProjBaseConstatnts
{

	//Known units of translation
	public static final String GEORADIANS="GEORADIANS";//Radians
	public static final String DEGREE ="DEGREE";//Radians
	public static final String METERS="METERS";//METERS
	public static final String USER="USER";//Unknown


	public static String getNameUnitsByUnitsName(String unitsname)
	{
		if (unitsname.equals(DEGREE))
			return "Градусы";
		if (unitsname.equals(GEORADIANS))
			return "Радиан";
		else if (unitsname.equals(METERS))
			return "Метр";
		else
			return unitsname;
	}

}
