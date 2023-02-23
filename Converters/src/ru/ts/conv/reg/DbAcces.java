package ru.ts.conv.reg;

import ru.tg.db.DBUtils;

import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;

/**
 * Created by IntelliJ IDEA.
 * User: vladm
 * Date: 28.06.2011
 * Time: 20:28:44
 *
 */
public class DbAcces
{
	public static Map<String,Survey_ItemRec> key2Survey_ItemRec = null;

	static double[] checkColleaction(Collection<Survey_ItemRec> coll) throws Exception
	{
	    //Вообще говоря считам что у всей коллекции должны быть одинаковые данные по широте и долготе
		double[] rrv=null;
		for (Survey_ItemRec survey_itemRec : coll)
		{
			double[] rv = survey_itemRec.getLatLng();
			if (rrv==null)
			{
				rrv=rv;
				break;//TODO to speed up this code
			}
			else
			{
				if (rrv[0]-rv[0]!=0 ||
						rrv[1]-rv[1]!=0)
					throw new Exception("Can't define unambiguously latitude or lngitude");
			}
		}
		return rrv;
	}

	public static double[] getLatLong(String key, Connection conn) throws Exception
	{
		if (key2Survey_ItemRec== null)
			getSurvey_Item2( conn);
		Survey_ItemRec survey_itemRec = key2Survey_ItemRec.get(key);
		return survey_itemRec.getLatLng();
	}

	public static void getSurvey_Item2( Connection conn) throws Exception
	{
		key2Survey_ItemRec=new HashMap<String,Survey_ItemRec>();

		final String sql;
		sql="SELECT * FROM Survey_Item";
		Statement stmt = null;
		ResultSet rs = null;

		try
		{
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			Survey_ItemRec obj=new Survey_ItemRec();
			while(DBUtils.setObjectBySQL(rs,obj)!=null)
			{
				if (!key2Survey_ItemRec.containsKey(obj.ID_High))
				{
					key2Survey_ItemRec.put(obj.ID_High,obj);
					obj=new Survey_ItemRec();
				}
			}
		}
		finally
		{
			if (rs!=null)
				rs.close();
			if (stmt!=null)
				stmt.close();
		}
	}

	public static Collection<Survey_ItemRec> getSurvey_Item(String key, Connection conn) throws Exception
	{
		final String sql;
		if (key !=null)
			sql = "SELECT * FROM Survey_Item WHERE ID_High=" + key;
		else
			sql="SELECT * FROM Survey_Item";

		return DBUtils.setObjectsBySQL(conn, sql, new Survey_ItemRec());
	}

	public static class Survey_ItemRec
	{

		public String ID_Survey_Item;
		public String ID_High;
		public String latitude;
		public String longitude;

		public double[] getLatLng()
		{
			return new double[]{Double.parseDouble(latitude),Double.parseDouble(longitude)};
		}
	}

}
