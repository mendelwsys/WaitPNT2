package ru.ts.toykernel.pcntxt.gui.defmetainfo;

import java.util.Map;

import ru.ts.gisutils.datamine.ProjBaseConstatnts;
import ru.ts.utils.gui.tables.IHeaderSupplyer;
import ru.ts.utils.gui.tables.TNode;
import ru.ts.toykernel.gui.deftable.*;
import ru.ts.utils.gui.tables.THeader;
import su.mwlib.utils.Enc;

/**
 * Заголовки модуля проекций
 */
public class ProjectionHeaders
{
	public static final String SRCMEASURE = "SRCMEASURE";
	public static final String DSTMEASURE = "DSTMEASURE";
	public static final String DIRECTION = "DIRECTION";
	public static final String WKT = "WKT";
	//Преобразование применяется к координатм проекта, т.е. преобразователь
	private static final THeader[] optionsheaders =
			{
					new TDefaultHeader(new TNode(Enc.get("PROJECT_UNIT")), SRCMEASURE,false, String.class)
					{
						public Object getValueAt(int col,int row, Object data)
						{
							Map<String,Object> lrattr = (Map<String,Object>) data;
							return ProjBaseConstatnts.getNameUnitsByUnitsName(lrattr.get(paramname).toString());
						}

						public boolean setValueAt(Object val, int col,int row, Object data)
						{
							return false;
						}

					}
					,
					new TDefaultHeader(new TNode(Enc.get("PROJECTION_UNIT")), DSTMEASURE,false, String.class)
					{
						public Object getValueAt(int col,int row, Object data)
						{
							Map<String,Object> lrattr = (Map<String,Object>) data;
							return ProjBaseConstatnts.getNameUnitsByUnitsName(lrattr.get(paramname).toString());
						}

						public boolean setValueAt(Object val, int col,int row, Object data)
						{
							return false;
						}
					},

					new TDefaultHeader(new TNode("WKT"), WKT, true,String.class),
			};

	public static IHeaderSupplyer getOptionsHeaderSupplyer()
	{
		return new IHeaderSupplyer()
		{
			public THeader[] getOptionsRepresent()
			{
				return optionsheaders;
			}
		};
	}

}
