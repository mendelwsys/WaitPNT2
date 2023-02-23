package ru.ts.toykernel.plugins.facils;

import ru.ts.toykernel.factory.BaseInitAble;
import ru.ts.toykernel.plugins.*;
import ru.ts.toykernel.plugins.facils.tables.FTableStruct;
import ru.ts.toykernel.consts.INameConverter;
import ru.ts.toykernel.consts.KernelConst;
import ru.ts.toykernel.shared.PathObject;
import ru.ts.toykernel.attrs.IDefAttr;
import ru.ts.factory.IFactory;

import javax.swing.table.TableModel;
import java.util.*;

/**
 * Модуль обеспечивающий передачу таблицы на клиент
 */
public class FacilBaseModule extends BaseInitAble implements IModule
{
	public static final String METRICREQ = "METRICREQ";//получить метрику таблицы


	public static final String FILLREQ = "FILLREQ";//Заполнить таблицу


	public static final String OBJBYRID = "OBJBYRID";//Запрос по идентификатору ряда таблицы (rowid)


	public static final String OIDS = "OIDS"; //Позиционировать таблицу согласно идентификаторам


	public static final String ADDFILTERBYIDS = "ADDFILTERBYIDS";//Запрос на установку фильтра на таблицу по идентификаторам объектов

	//public static final String FILTERBYRECT = "FILTERBYRECT";//Запрос на установку фильтра на таблицу по прямоугольнику


	public static final String MODULENAME = "BASE_FACIL";

	protected FTableStruct acttbl;

	protected String db_file;
	protected String db_encode;
	private IModule searchmodule;
	private int nrows = 10;

	public Object[] init(Object... objs) throws Exception
	{
		super.init(objs);

		acttbl = new FTableStruct();
		acttbl.loadFromfile(db_file, db_encode);
		return null;
	}


	public Object init(Object obj) throws Exception
	{
		//Загрузка таблицы данных в модуль, вместе с информацией связи.
		//На клиент подается только отображения полей и идентификаторы столбцов, которые представляют собой порядковые
		// номера в исходной таблицы без фильтрации
		IDefAttr attr = (IDefAttr) obj;
		if (attr.getName().equalsIgnoreCase(KernelConst.DIR_TAGNAME))
			db_file = ((PathObject) attr.getValue()).getFolderlayers();
		else if (attr.getName().equalsIgnoreCase("ENCODE"))
			db_encode = (String) attr.getValue();
		else if (attr.getName().equalsIgnoreCase("DB_FILE"))
			db_file = (String) attr.getValue();
		else if (attr.getName().equalsIgnoreCase(KernelConst.PLUGIN_TAGNAME))
			this.searchmodule = (IModule) attr.getValue();
		return null;
	}


	public String getModuleName()
	{
		return MODULENAME;
	}

	public IAnswerBean execute(ICommandBean cmd) throws Exception
	{

		if (METRICREQ.equalsIgnoreCase(cmd.getCommand()))
		{
			TableModel tm = acttbl.getTableModel();
			int rowcnt = tm.getRowCount();
			int colcnt = tm.getColumnCount();
			return new AnswerBean(cmd, METRICREQ + "#" + String.valueOf(rowcnt) + ";" + String.valueOf(colcnt) + "#", new byte[0]);
		}
		else if (OBJBYRID.equalsIgnoreCase(cmd.getCommand()))
		{ //Обработка команды позиционирования по клику на таблице
			String srowid = cmd.getParamByName("OBJBYRID");
			try
			{
				Integer rowid = null;
				if (srowid != null && srowid.length() > 0)
					rowid = Math.abs(Integer.parseInt(srowid));

				Map<String, String> tuple = acttbl.getTupleByIndex(rowid);

				String curveId = tuple.get(FTableStruct.CURVEID);
				String layerId = tuple.get(FTableStruct.TBLLAYRID);

				String newCurveId = layerId + "#$" + curveId;
				Map<String, String[]> map = new HashMap<String, String[]>();
				map.put("OIDS", new String[]{newCurveId});
				return searchmodule.execute(new CommandBean("OIDS", map, cmd.getCodeActivator(), cmd.getSessionId()));
			}
			catch (NumberFormatException e)
			{//
			}
		}

		else if (OIDS.equalsIgnoreCase(cmd.getCommand()))
		{
			//Поиск идентификатора в таблице
			String oids = cmd.getParamByName(OIDS);
			String[] aroid = new String[0];
			if (oids != null)
			{
//				oids = oids.replaceAll("[@]", "#");//Заменяем знак клиента на знак сервер (TODO Надо как-то обходится без этого,
//				//TODO либо на клиенте после прасинга убирать /, либо как-то все-так принять протокол связи то-же и с ;, и еще пробленые литеры как у нас ришаются при передаче?)
				aroid = oids.split(";");

				//
				List<String> curveIds = acttbl.getColumn(FTableStruct.CURVEID);
				List<String> tableIds = acttbl.getColumn(FTableStruct.TBLLAYRID);
				int i = 0;
				br0:
				for (; i < tableIds.size(); i++)
				{
					String curveId = curveIds.get(i);
					String tblId = tableIds.get(i);
					for (String incurve : aroid)
					{
						if (incurve.equals(tblId + "#$" + curveId))
							break br0;
					}
				}
				if (i < tableIds.size())
				{
					Set<Integer> set = new HashSet<Integer>();
					set.add(i);
					return refreshWebTable(cmd, this.nrows, i, false, set);
				}
			}
		}
		else if (FILLREQ.equalsIgnoreCase(cmd.getCommand()))
		{

			String snrow = cmd.getParamByName("NROW");
			int nrows = 10;
			String ssrow = cmd.getParamByName("SROW");
			int starpos = 0;
			String shreq = cmd.getParamByName("HREQ");
			boolean hreq = false;

			try
			{
				if (snrow != null && snrow.length() > 0)
					nrows = Integer.parseInt(snrow);
			}
			catch (NumberFormatException e)
			{//
			}

			try
			{
				if (ssrow != null && ssrow.length() > 0)
					starpos = Integer.parseInt(ssrow);
			}
			catch (NumberFormatException e)
			{//

			}

			try
			{
				if (shreq != null && shreq.length() > 0)
					hreq = shreq.equalsIgnoreCase("true");
			}
			catch (NumberFormatException e)
			{//

			}

			return refreshWebTable(cmd, nrows, starpos, hreq, null);
		}
		return new AnswerBean(cmd, "", new byte[0]);
	}

	private IAnswerBean refreshWebTable(ICommandBean cmd, int nrows, int starpos, boolean hreq, Collection<Integer> selrows)
	{
		StringBuffer sbuff = new StringBuffer();

		TableModel tm = acttbl.getTableModel();
		int totalrows = tm.getRowCount();
		int colcnt = tm.getColumnCount();
		this.nrows = nrows;

		sbuff.append(String.valueOf(hreq)).append(';');

		sbuff.append(String.valueOf(starpos)).append(';');
		sbuff.append(String.valueOf(nrows)).append(';');
		sbuff.append(String.valueOf(totalrows)).append(';');

		if (hreq)
		{
			sbuff.append(String.valueOf(colcnt)).append(';');
			List<String> headlist = acttbl.getViewHeaders();
			for (String head : headlist)
				sbuff.append(head).append(';');
		}

		for (int i = starpos; i < totalrows; i++)
		{
			if (i >= starpos + nrows)
				break;

			if (selrows != null && selrows.contains(i))
				sbuff.append(-i).append(";");//Идентификатор ряда таблицы
			else
				sbuff.append(i).append(";");//Идентификатор ряда таблицы

			int j = 0;
			for (; j < colcnt; j++)
			{

				String val = (String) tm.getValueAt(i, j);
				int ix;
				if ((ix = val.indexOf("#")) >= 0)
					val = val.substring(0, ix);
				sbuff.append(val).append(";");
			}
		}
		return new AnswerBean(cmd, FILLREQ + "#" + sbuff.toString() + "#", new byte[0]);
	}

	public void registerNameConverter(INameConverter nameConverter, IFactory<INameConverter> factory) throws Exception
	{
	}

	public void unload()
	{
	}
}
