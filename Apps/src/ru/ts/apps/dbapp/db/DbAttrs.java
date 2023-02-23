package ru.ts.apps.dbapp.db;

import ru.ts.toykernel.attrs.def.DefaultAttrsImpl;
import ru.ts.toykernel.attrs.def.DefAttrImpl;
import ru.ts.toykernel.attrs.def.DefaultGisObjectAttrs;
import ru.ts.toykernel.attrs.IAttrs;
import ru.ts.toykernel.attrs.IDefAttr;
import ru.tg.db.PollConnection;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;
import java.util.HashSet;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

/**
 * Db attributes
 */
public class DbAttrs extends DefaultGisObjectAttrs
{

	/**
	 * create attribute of gis object
	 *
	 * @param defAttrs - default attribute values
	 * @param objectId - object idenifier to which attributes belongs to
	 */
	public DbAttrs(IAttrs defAttrs, String objectId)
	{
		super(defAttrs, objectId);
	}

	protected IAttrs getAttrFromDb(String objectId)
	{
		IAttrs rv=new DefaultAttrsImpl();

		Statement stmt = null;
		ResultSet rs = null;
		Connection conn = null;
		try
		{
			conn=PollConnection.getConnection();

			stmt = conn.createStatement();
			rs = stmt.executeQuery("SELECT * FROM objAttrs where objectId='"+ objectId+"'");
			while (rs.next())
			{
				ResultSetMetaData metadata = rs.getMetaData();
				int clcnt=metadata.getColumnCount();
				for (int i = 1; i <= clcnt; i++)
				{
					String columnName = metadata.getColumnName(i);
					String val = rs.getString(i);
					rv.put(columnName,new DefAttrImpl(columnName,val));
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			PollConnection.closeResources(stmt, rs);
			PollConnection.ReleaseConnection(conn);
		}
		return rv;
	}

	protected IDefAttr updateAttrDb(String objectId,IDefAttr attr)
	{
		throw new UnsupportedOperationException();
	}

	public IDefAttr get(Object key)
	{
		IAttrs dbAttrs = getAttrFromDb(objectId);
		IDefAttr rv=dbAttrs.get(key);
		if (rv==null)
			rv=super.get(key);
		return rv;
	}

	public  IDefAttr put(String key,IDefAttr attr)
	{
		IAttrs dbAttrs = getAttrFromDb(objectId);
		IDefAttr rv=dbAttrs.get(key);
		if (rv!=null)
			return updateAttrDb(objectId,attr);
		else
			return super.put(key,attr);
	}

	public Collection<IDefAttr> values()
	{
		Collection<IDefAttr> rv=new LinkedList<IDefAttr>(super.values());
		IAttrs dbAttrs = getAttrFromDb(objectId);
		rv.addAll(dbAttrs.values());
		return rv;
	}

	public Set<String> keySet()
	{
		Set<String> rv=new HashSet<String>(super.keySet());
		IAttrs dbAttrs = getAttrFromDb(objectId);
		rv.addAll(dbAttrs.keySet());
		return rv;
	}

	public int size()
	{
		return keySet().size();
	}
}
