package ru.ts.conv.reg_t;


import su.org.coder.utils.SerialUtils;

import java.sql.*;
import java.util.*;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.File;
import java.lang.reflect.Field;

import ru.ts.toykernel.storages.mem.MemEditableStorageLr;
import ru.ts.toykernel.consts.KernelConst;
import ru.ts.toykernel.geom.IEditableGisObject;
import ru.ts.toykernel.attrs.def.DefaultAttrsImpl;
import ru.ts.toykernel.attrs.def.DefAttrImpl;

/**
 * Created by IntelliJ IDEA.
 * User: vladm
 * Date: 20.06.2011
 * Time: 12:04:03
 * Генератор слоев и описания проекта 
 */
public class Access2Access
{

	private static Connection _getConnection( String driver, String prefix,
	        String dbPath, Properties props ) throws SQLException,
	        InstantiationException, IllegalAccessException,
	        ClassNotFoundException

	{
		Class.forName( driver ).newInstance();
		if ( props != null )
			return DriverManager.getConnection( prefix + dbPath, props );
		else
			return DriverManager.getConnection( prefix + dbPath );
	}

	public static void save2Icons(String[] args) throws Exception
	{
		Properties props = new Properties();
		props.setProperty( "charSet", "windows-1251" );
		Connection conn=_getConnection( "sun.jdbc.odbc.JdbcOdbcDriver",
			"jdbc:odbc:Driver={Microsoft Access Driver (*.mdb)};DBQ=", "G:\\DataBaseRegion\\mos.accdb", props );

		Statement stmt = null;
		ResultSet rs = null;

		stmt = conn.createStatement();
//		final String sql = "SELECT t.Param_Name,t.ID_Type_Attr, a.L1,a.L2 FROM Attribute a ,Types_Description t " +
//				"WHERE a.ID_Type_Attr=t.ID_Type_Attr";

//		final String sql = "SELECT a.ID_Type_Attr, a.L1,a.L2 FROM Attribute a" +
//				" WHERE a.ID_Type_Attr not in " +
//				"( SELECT ID_Type_Attr FROM Types_Description )";


		final String sql ="SELECT  ID_Icon,Image FROM Icon_Image ";
		rs = stmt.executeQuery(sql);
		while (rs.next())
		{
			String id_attr=rs.getString(1);
			DataInputStream dis = new DataInputStream(rs.getBinaryStream(2));
			String baseDir = "G:\\BACK_UP\\$D\\MAPDIR\\ICO\\";
			final FileOutputStream fos = new FileOutputStream(baseDir + id_attr + ".ico");

			byte[] bt=new byte[100];
			int a=0;
			while((a=dis.read(bt))>0)
				fos.write(bt,0,a);
			fos.close();

			dis.close();
		}
	}

	public static GroupDesc getDesc(Connection conn,String ID_Type_Attr) throws Exception
	{
		return setObjectBySQL(conn,"SELECT  * FROM Group_Description gd where gd.ID_Type_Attr="+ID_Type_Attr,new GroupDesc());
	}

	public static <T> T setObjectBySQL(Connection conn,String sql,T obj)
			throws Exception
	{

		Statement stmt = null;
		ResultSet rs = null;

		try
		{
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);

			final Class<GroupDesc> aClass = GroupDesc.class;
			Field[] fld= aClass.getFields();

			for (Field field : fld)
				field.set(obj,rs.getString(field.getName()));
			return obj;
		}
		finally
		{
			if (rs!=null)
				rs.close();
			if (stmt!=null)
				stmt.close();
		}
	}

	public static void main(String[] args) throws Exception
	{
//		Connection conn=_getConnection( "com.hxtt.sql.access.AccessDriver",
//			        "jdbc:access:/", "G:\\DataBaseRegion\\mos.accdb", null );

		Properties props = new Properties();
		props.setProperty( "charSet", "windows-1251" );
		Connection conn=_getConnection( "sun.jdbc.odbc.JdbcOdbcDriver",
			"jdbc:odbc:Driver={Microsoft Access Driver (*.mdb)};DBQ=", "G:\\DataBaseRegion\\mos.accdb", props );

		Statement stmt = null;
		ResultSet rs = null;

		stmt = conn.createStatement();

		final String sql ="SELECT  Video_Name,Local_Camera_Data,ID_Survey_Item FROM Survey_Item ";
		rs = stmt.executeQuery(sql);
		while (rs.next())
		{
			String fname=rs.getString(1);
			DataInputStream dis = new DataInputStream(rs.getBinaryStream(2));
			File fl = new File("G:\\" + fname);
			File file = fl.getParentFile();
			try
			{
				file.mkdirs();
				if (!fl.createNewFile())
				{
					System.out.println("Error on create 1 fname = " + fname+" : "+rs.getString(3));
					continue;
				}
			}
			catch (IOException e)
			{
				System.out.println("Error on create 2 fname = " + fname+" : "+rs.getString(3));
				continue;
			}

			FileOutputStream fos = new FileOutputStream(fl);


			byte[] buffer=new byte[10*1024*1024];

			int retval=0;
			while((retval=dis.read(buffer)) >0)
				fos.write(buffer,0,retval);
			fos.close();
			dis.close();
		}

	}

	public static void _main(String[] args) throws Exception
	{
//		Connection conn=_getConnection( "com.hxtt.sql.access.AccessDriver",
//			        "jdbc:access:/", "G:\\DataBaseRegion\\mos.accdb", null );

		Properties props = new Properties();
		props.setProperty( "charSet", "windows-1251" );
		Connection conn=_getConnection( "sun.jdbc.odbc.JdbcOdbcDriver",
			"jdbc:odbc:Driver={Microsoft Access Driver (*.mdb)};DBQ=", "G:\\DataBaseRegion\\mos.accdb", props );

		Statement stmt = null;
		ResultSet rs = null;

		stmt = conn.createStatement();
//		final String sql = "SELECT t.Param_Name,t.ID_Type_Attr, a.L1,a.L2 FROM Attribute a ,Types_Description t " +
//				"WHERE a.ID_Type_Attr=t.ID_Type_Attr";

//		final String sql = "SELECT a.ID_Type_Attr, a.L1,a.L2 FROM Attribute a" +
//				" WHERE a.ID_Type_Attr not in " +
//				"( SELECT ID_Type_Attr FROM Types_Description )";

//Получить гео-объекты
		int i;


		Map<String,List<GeoObject>> type2Objects = new HashMap<String,List<GeoObject>>(); //Мново геообъектов для отображения на карте

		try
		{
			final String sql ="SELECT  ID_Attribute,ID_Type_Attr, Image_Points FROM Attribute ";
			rs = stmt.executeQuery(sql);
			i = 0;
			while (rs.next())
			{



				String id_attr=rs.getString(1);
				String type_attr=rs.getString(2);
				DataInputStream dis = new DataInputStream(rs.getBinaryStream(3));


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
						ds[ix]=SerialUtils.unserializedbl64(bt);
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


				GeoObject obj = new GeoObject(id_attr, null, type_attr, dres);

				List<GeoObject> lobjs = type2Objects.get(type_attr);
				if (lobjs==null)
					type2Objects.put(type_attr,lobjs=new LinkedList<GeoObject>());
				lobjs.add(obj);
				i++;
			}
		}
		finally
		{
			if (rs!=null)
				rs.close();
			if (stmt!=null)
				stmt.close();
		}

		save2Storage(type2Objects);
		//CalcScaleAndShift(type2Objects);
		System.out.println("i = " + i);
	}

	public static void save2Storage(Map<String,List<GeoObject>> type2Objects) throws Exception
	{
		for (String typeId : type2Objects.keySet())
		{
//TODO Записать имя типа и создать еще
			MemEditableStorageLr memstor = new MemEditableStorageLr("G:\\BACK_UP\\$D\\MAPDIR\\REG\\", typeId);
			DefaultAttrsImpl defattrs = new DefaultAttrsImpl();
////			defattrs.put(SHPConstants.ORIG_TYPE,new DefAttrImpl(SHPConstants.ORIG_TYPE,String.valueOf(readShp.mfh.shptype)));
			memstor.setDefAttrs(defattrs);


			List<GeoObject> objs = type2Objects.get(typeId);
			for (GeoObject gobj : objs)
			{

				IEditableGisObject obj = memstor.createObject(KernelConst.LINESTRING);
				final double[] xdim = gobj.getDimARRAY(GeoObject.XDIM);
				final double[] ydim = gobj.getDimARRAY(GeoObject.YDIM);
				obj.addSegment(obj.getSegsNumbers(), xdim, ydim);

				DefaultAttrsImpl objatrrs = new DefaultAttrsImpl();

//				{
//					DefAttrImpl attr = new DefAttrImpl("ZDIM", gobj.getDimARRAY(Record.ZDIM));
//					objatrrs.put(attr.getName(),attr);
//				}
//
//				{
//					DefAttrImpl attr = new DefAttrImpl("ADIM", gobj.getDimARRAY(Record.ADIM));
//					objatrrs.put(attr.getName(),attr);
//				}
//
//				{
//					DefAttrImpl attr = new DefAttrImpl("LDIM", gobj.getDimARRAY(Record.LDIM));
//					objatrrs.put(attr.getName(),attr);
//				}

				{
					DefAttrImpl attr = new DefAttrImpl("TNAME", gobj.typeName);
					objatrrs.put(attr.getName(),attr);
				}

				{
					DefAttrImpl attr = new DefAttrImpl("ATTRID", gobj.attrid);
					objatrrs.put(attr.getName(),attr);
				}

				obj.setCurveAttrs(objatrrs);
			}
			memstor.commit();
		}




	}

	private static void CalcScaleAndShift(Map<String, List<GeoObject>> type2Objects)
	{
		List<GeoObject> objs_0303 = type2Objects.get("0303");

		double xmin=0;
		double xmax=0;
		double ymin=0;
		double ymax=0;


		double xsumm=0;
		double ysumm=0;
		int cnt=0;
		for (GeoObject geoObject : objs_0303)
		{
			final double[] xdim = geoObject.getDimARRAY(GeoObject.XDIM);
			final double[] ydim = geoObject.getDimARRAY(GeoObject.YDIM);


			for (int i = 0; i < xdim.length; i++)
			{
				if (cnt==0)
				{
					xmax=xmin=xdim[i];
					ymax=ymin=ydim[i];
				}

				else
				{
					if (xmax<xdim[i])
						xmax=xdim[i];
					if (xmin>xdim[i])
						xmin=xdim[i];

					if (ymax<ydim[i])
						ymax=ydim[i];
					if (ymin>ydim[i])
						ymin=ydim[i];
				}

				xsumm += xdim[i];
				ysumm += ydim[i];
				cnt++;
			}
		}

		System.out.println("XMidl = " + xsumm/cnt);
		System.out.println("YMidl = " + ysumm/cnt);

		System.out.println("XSCale = " + Math.abs(xmin-xmax)/1024);
		System.out.println("YSCale = " + Math.abs(ymin-ymax)/1024);
	}

	public static class GroupDesc
	{
		public String Item_Name;
		public String ID_Type_Attr;
		public String TypeShape;

		public String PenColor;
		public String PenStroke;
		public String PenWidth;

		public String BrushColor;
		public String ConturStroke;
	}

	public static class GeoObject
	{
		public static final int XDIM=0;
		public static final int YDIM=1;
		public static final int ZDIM=2;
		public static final int ADIM=3;
		public static final int LDIM=4;
		public static final int SZ_DIMS = 5;
		public String attrid;
		public String typeName;
		public String typeId;
		public double[] xyzal;

		public GeoObject(String attrid,String typeName, String typeId, double[] xyzal)
		{
			this.attrid = attrid;
			this.typeName = typeName;
			this.typeId = typeId;
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
}
