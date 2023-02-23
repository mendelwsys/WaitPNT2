package ru.ts.conv.reg;

import ru.ts.conv.IColorSheemLoader;
import ru.tg.db.DBUtils;

import java.util.Collection;
import java.util.Iterator;
import java.sql.Connection;

/**
 * Created by IntelliJ IDEA.
 * User: vladm
 * Date: 22.06.2011
 * Time: 15:56:54
 * Объект содержит цветовую схему для раскраски карты
 */
public class SheemLoader implements IColorSheemLoader
{

	public static final String KEY_COLNAME="ID_Type_Attr";
	static protected SheemLoader sheem;
	protected Collection<GroupDesc> objs;
	protected Iterator<GroupDesc> objsiter;
	private GroupDesc objcurent;
	private Connection conn;

	private SheemLoader(Connection conn) throws Exception
	{
		this.conn = conn;
	}

	public static IColorSheemLoader getInstance(Connection conn) throws Exception
	{
		if (sheem==null)
			sheem=new SheemLoader(conn);
		return sheem;
	}

	public boolean hasNext()
	{
		return objsiter.hasNext();
	}

	public Object next()
	{
		return objcurent=objsiter.next();
	}

	public void remove()
	{
		objsiter.remove();
	}

	public void reset()
	{
		objsiter = objs.iterator();
		objcurent=null;
	}

	public int findByKey(String key) throws Exception
	{
		final String sql;
		if (key !=null)
			sql = "SELECT * FROM Group_Description WHERE " + KEY_COLNAME + "='" + key+"'";
		else
			sql="SELECT * FROM Group_Description";

		objs = DBUtils.setObjectsBySQL(conn, sql, new GroupDesc());
		objsiter = objs.iterator();
		return objs.size();
	}

	public String getObjectType()
	{
		return objcurent.TypeShape;
	}

	public String getLineColor()
	{
		return getHexVal(objcurent.PenColor);
	}

	public String getFillColor()
	{

		return getHexVal(objcurent.BrushColor);
	}

	private String getHexVal(String color)
	{
		int bc=0;
		try
		{
			bc=Integer.parseInt(color);
			int r=bc&0xFF;
			int g=(bc>>8)&0xFF;
			int b=(bc>>16)&0xFF;

			bc=(r<<16)|(g<<8)|b;
			bc|=0xFF000000;
		}
		catch (NumberFormatException e)
		{
			e.printStackTrace();
		}
		return Integer.toHexString(bc);
	}

	public String getLineStroke()
	{
		return objcurent.PenStroke;
	}

	public String getLineThickness()
	{
		return objcurent.PenWidth;
	}

	public String getGroupName()
	{
		return objcurent.Item_Name;
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

		public String ID_Icon;//Идентификатор иконы
	}

}
