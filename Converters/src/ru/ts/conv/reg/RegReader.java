package ru.ts.conv.reg;

import su.org.coder.utils.SerialUtils;

import java.util.*;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.io.DataInputStream;
import java.io.IOException;

import ru.tg.db.DBUtils;

/**
 * Created by IntelliJ IDEA.
 * User: vladm
 * Date: 22.06.2011
 * Time: 17:23:50
 * Читать записи и аттрибуты из БД Регион
 */
public class RegReader
{
	public static final String ID_ICON="ID_Icon";

	public static IconRec loadIcon(Connection conn,String ID_Icon,String base_path) throws Exception
	{
		String sql;
		if (ID_Icon !=null)
			sql = "SELECT * FROM Icons WHERE " +ID_ICON + "='" + ID_Icon+"'";
		else
			sql="SELECT * FROM Icons";

		Collection<IconRec> objs = DBUtils.setObjectsBySQL(conn, sql, new IconRec());

		if (objs.size()>0)
		{
			Iterator<IconRec> iter = objs.iterator();
			final IconRec rec = iter.next();

			if (rec!=null)
			{//Загружаем иконки

				sql = "SELECT Image FROM Icon_Image WHERE " +ID_ICON + "='" + ID_Icon+"'";

				Statement stmt = null;
				ResultSet rs = null;

				try
				{
					stmt = conn.createStatement();
				}
				finally
				{
					if (rs!=null)
						rs.close();

					if (stmt!=null)
						stmt.close();
				}
			}

			return rec;
		}
		return null;
	}

	public static Map<String,IconRec> geticonId2IconRec(Connection conn) throws Exception
	{
		Statement stmt = null;
		ResultSet rs = null;

		stmt = conn.createStatement();

//Получить гео-объекты
		Map<String,IconRec> iconId2IconRec = new HashMap<String,IconRec>(); //Мново геообъектов для отображения на карте

		try
		{
			final String sql ="SELECT  * FROM Icons ";
			rs = stmt.executeQuery(sql);
			while (rs.next())
			{
				IconRec icorec=new IconRec();
				icorec.ID_Icon=rs.getString(1);
				icorec.Name=rs.getString(2);
				icorec.Path=rs.getString(3);
				iconId2IconRec.put(icorec.ID_Icon,icorec);
			}
		}
		finally
		{
			if (rs!=null)
				rs.close();
			if (stmt!=null)
				stmt.close();
		}
		return iconId2IconRec;
	}

	public static Collection<Param> getObjectParams(Connection conn,String ID_Attribute,String paramName) throws Exception
	{

		Statement stmt = null;
		ResultSet rs = null;

		stmt = conn.createStatement();

//Получить гео-объекты
		//Map<String,List<Param>> id2params = new HashMap<String,List<Param>>(); //Мново геообъектов для отображения на карте
		Collection<Param> paramlist = new LinkedList<Param>();

		try
		{
			final String sql ="SELECT  p.ID_Attribute as ID_Attribute,p.ID_Param as ID_Param, Param_Name, Type, IsSuffix,ValueParam FROM Params p, Types_Description td WHERE p.ID_Attribute = "+ID_Attribute +
					"AND " +
					"p.ID_Param = td.ID_Param "+((paramName==null)?"":(" AND Param_Name='" +paramName+"'"));

			rs = stmt.executeQuery(sql);
			Param prm = new Param();
			while (DBUtils.setObjectBySQL(rs,prm)!=null)
			{
//				List<Param> params=id2params.get(prm.ID_Attribute);
//				if (params==null)
//					id2params.put(prm.ID_Attribute,params=new LinkedList<Param>());
				paramlist.add(prm);
				prm = new Param();
			}
		}
		finally
		{
			if (rs!=null)
				rs.close();
			if (stmt!=null)
				stmt.close();
		}

		return paramlist;
	}

	public static Map<String,List<Record>> getType2Objects(Connection conn) throws Exception
	{

		Statement stmt = null;
		ResultSet rs = null;

		stmt = conn.createStatement();

//Получить гео-объекты
		Map<String,List<Record>> type2Objects = new HashMap<String,List<Record>>(); //Мново геообъектов для отображения на карте

		try
		{
			final String sql ="SELECT  ID_Attribute,ID_Type_Attr, Image_Points,ID_High, L1, L2 FROM Attribute ";
			rs = stmt.executeQuery(sql);
			while (rs.next())
			{
				String id_attr=rs.getString(1);
				String type_attr=rs.getString(2);
				DataInputStream dis = new DataInputStream(rs.getBinaryStream(3));
				String high = rs.getString(4);
				String L1 = rs.getString(5);
				String L2 = rs.getString(6);

				List<double[]> ll=new LinkedList<double[]>();
				while (true)
				try
				{
					dis.readInt();
					double[] ds=new double[5];
					for (int ix=0;ix<ds.length;ix++)
					{
						byte[] bt=new byte[8];
						dis.readFully(bt);
						ds[ix]= SerialUtils.unserializedbl64(bt);
					}
					ll.add(ds);
				}
				catch (IOException e)
				{
					break;
				}

				double[] dres=new double[5*ll.size()];
				for (int k = 0,j=0; k < ll.size(); k++)
				{
					double[] doubles = ll.get(k);
					System.arraycopy(doubles,0,dres,j,doubles.length);
					j+=doubles.length;
				}



				Record obj = new Record(id_attr,type_attr, high,L1,L2, dres);

				List<Record> lobjs = type2Objects.get(type_attr);
				if (lobjs==null)
					type2Objects.put(type_attr,lobjs=new LinkedList<Record>());
				lobjs.add(obj);
			}
		}
		finally
		{
			if (rs!=null)
				rs.close();
			if (stmt!=null)
				stmt.close();
		}
		return type2Objects;
	}

	//Отдать имя атрибута -> значение параметра

	public static class IconRec
	{
		String ID_Icon;
		String Name;
		String Path;
	}

	public static class Record
	{
		public static final int XDIM=0;
		public static final int YDIM=1;
		public static final int ZDIM=2;
		public static final int ADIM=3;
		public static final int LDIM=4;
		public static final int SZ_DIMS = 5;
		public String attrid;
		public String typeId;
		public String ID_High;
		public String L1;
		public String L2;
		public double[] xyzal;

		public Record(String attrid,String typeId, String ID_High,String L1,String L2,double[] xyzal)
		{
			this.attrid = attrid;
			this.typeId = typeId;
			this.ID_High = ID_High;
			this.L1 = L1;
			this.L2 = L2;
			this.xyzal = xyzal;
		}

		/**
		 * Отдать координаты изиерения
		 * @param ix - индекс измерения
		 * @return - массив измерений
		 */
		public double[] getDimARRAY(int ix)
		{
			double [] rv = new double[xyzal.length/SZ_DIMS];

			for (int i = ix,j=0;i < xyzal.length;j++, i+= SZ_DIMS)
				rv[j]=xyzal[i];

			return rv;
		}
	}

	public static class Param
	{
		public String ID_Attribute;
		public String ID_Param;
		public String Param_Name;
		public String Type;
		public String IsSuffix;

		public String ValueParam;
	}

}
