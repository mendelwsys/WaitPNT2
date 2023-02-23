package ru.ts.toykernel.plugins.cvviewer;

import ru.ts.utils.gui.tables.IHeaderSupplyer;
import ru.ts.utils.gui.tables.THeader;
import ru.ts.utils.gui.tables.TNode;
import ru.ts.toykernel.gui.deftable.TDefaultHeader2;
import ru.ts.toykernel.consts.INameConverter;
import su.mwlib.utils.Enc;

import javax.swing.*;
import java.util.List;
import java.util.Map;

/**
 * Default header supplyer for this module
 */
public class DefHeaderSupplyer implements IHeaderSupplyer
{
	private THeader[] optionsrepresent;

	public DefHeaderSupplyer(INameConverter nameConverter)
	{
		optionsrepresent = new THeader[]
			{
					new TDefaultHeader2(new TNode(nameConverter.codeAttrNm2ViewNm(ModuleConst.ATTRNAME)), ModuleConst.ATTRNAME,false, String.class)
					{
						public boolean setValueAt(Object val, int col, int row, Object data)
						{
							Map<String, List<String>> lrattr = (Map<String, List<String>>) data;
							int index=-1;
							if (val==null || val.toString().length()==0 || ((index=lrattr.get(paramname).indexOf(val))>=0 && index!=row))
							{
								JOptionPane.showMessageDialog(null, Enc.get("ATTRIBUTE_NAME")+((val!=null)?Enc.get("ALREADY_IN_USE"):Enc.get("EMPTY")), Enc.get("ERROR"), JOptionPane.ERROR_MESSAGE);
								return false;
							}
							else
							{
								lrattr.get(paramname).set(row, (String) val);
								return true;
							}
						}

					},
					new TDefaultHeader2(new TNode(nameConverter.codeAttrNm2ViewNm(ModuleConst.ATTRVALUE)), ModuleConst.ATTRVALUE,false,String.class),
			};
	}

	public THeader[] getOptionsRepresent()
	{
		return optionsrepresent;
	}
}
