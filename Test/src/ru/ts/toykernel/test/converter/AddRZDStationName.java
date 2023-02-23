package ru.ts.toykernel.test.converter;

import ru.ts.toykernel.proj.stream.def.StreamProjImpl;
import ru.ts.toykernel.drawcomp.rules.def.stream.CnStyleRuleFactory;
import ru.ts.toykernel.drawcomp.rules.def.stream.InscriptionRule;
import ru.ts.toykernel.drawcomp.rules.def.CnStyleRuleImpl;
import ru.ts.toykernel.drawcomp.rules.def.CommonStyle;
import ru.ts.toykernel.drawcomp.ILayer;
import ru.ts.toykernel.drawcomp.layers.def.DrawOnlyLayer;
import ru.ts.toykernel.storages.mem.MemStorageLr;
import ru.ts.toykernel.storages.INodeStorage;
import ru.ts.toykernel.storages.mem.NodeStorageImpl;
import ru.ts.toykernel.attrs.def.DefaultAttrsImpl;
import ru.ts.toykernel.attrs.def.DefAttrImpl;
import ru.ts.toykernel.attrs.IDefAttr;
import ru.ts.toykernel.consts.KernelConst;
import ru.ts.toykernel.geom.IBaseGisObject;
import ru.ts.toykernel.filters.stream.NodeFilter2;
import ru.ts.factory.DefIFactory;
import ru.ts.apps.bldapp.rule.AssetRule;


import java.io.*;
import java.util.List;
import java.util.Iterator;
import java.util.LinkedList;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: vladm
 * Date: 17.03.2009
 * Time: 16:53:55
 * To change this template use File | Settings | File Templates.
 */
public class AddRZDStationName
{
	public static void main(String[] args) throws Exception
	{
		String attrasname = KernelConst.ATTR_CURVE_NAME;
		StreamProjImpl proj = new StreamProjImpl(args[0],	new CnStyleRuleFactory(),null,new DefIFactory<INodeStorage>()
			{
				public INodeStorage createByTypeName(String typeStorage) throws Exception
				{
					if (typeStorage.equalsIgnoreCase(NodeStorageImpl.TYPENAME))
						return new NodeStorageImpl();
					return new MemStorageLr();
				}
			},null,null,null,null);
        proj.loadFromStream(new DataInputStream(new BufferedInputStream(new FileInputStream(args[0]))));

		List<ILayer> ll = proj.getLayerList();
		for (ILayer iLayer : ll)
		{
			Iterator<IBaseGisObject> it=iLayer.getStorage().getAllObjects();
			while (it.hasNext())
			{
				IBaseGisObject iBaseGisObject = it.next();
				IDefAttr curveName;
				if ((curveName=iBaseGisObject.getObjAttrs().get(attrasname))!=null && curveName.getValue()!=null && curveName.getValue().toString().equalsIgnoreCase("noname"))
					iBaseGisObject.getObjAttrs().remove(attrasname);
			}

		}

		for (ILayer iLayer : ll)
			if (iLayer.getLrAttrs().get("LAYER_CUSTOM_LR_GEN")!=null)
			{
				CnStyleRuleImpl oldRule = (CnStyleRuleImpl)iLayer.getDrawRule();
				AssetRule newRule = new AssetRule();
				newRule.setDefStyle(oldRule.getDefStyle());
				iLayer.setDrawRule(newRule);
			}

		NodeFilter2 grpFilter=new NodeFilter2(new LinkedList<String>());

		for (ILayer iLayer : ll)
		{
			IDefAttr attr;
			if ((attr=iLayer.getLrAttrs().get(KernelConst.LAYER_NAME))!=null && attr.getValue()!=null && attr.getValue().toString().toUpperCase().contains(" ЖД"))
			{
				INodeStorage storage = (INodeStorage) iLayer.getStorage();
				grpFilter.getNodesId().add(storage.getNodeId());
			}
		}

		DefaultAttrsImpl lrAttrs = new DefaultAttrsImpl();
		lrAttrs.put(KernelConst.LAYER_NAME,new DefAttrImpl(KernelConst.LAYER_NAME,"Надписи"));
		lrAttrs.put(KernelConst.LAYER_VISIBLE,new DefAttrImpl(KernelConst.LAYER_VISIBLE,true));
		ll.add(new DrawOnlyLayer(proj.getStorage(),grpFilter,lrAttrs,new InscriptionRule(new CommonStyle(0xff220044,0xbdAAAAAA),new Font("sunserif", Font.BOLD, 10))));
		proj.getProjMetaInfo().setBackgroundColor(0xFFFFFFFF);
		DataOutputStream os = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(args[0] + "_exp")));
		proj.savetoStream(os);
		os.flush();
		os.close();
	}
}
