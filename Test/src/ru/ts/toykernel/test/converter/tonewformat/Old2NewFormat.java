package ru.ts.toykernel.test.converter.tonewformat;

import ru.ts.toykernel.proj.stream.def.StreamProjImpl;
import ru.ts.toykernel.drawcomp.rules.def.stream.CnStyleRuleFactory;
import ru.ts.toykernel.drawcomp.rules.def.stream.InscriptionRule;
import ru.ts.toykernel.drawcomp.IDrawObjRule;
import ru.ts.toykernel.drawcomp.ILayer;
import ru.ts.factory.DefIFactory;
import ru.ts.toykernel.storages.INodeStorage;
import ru.ts.toykernel.storages.mem.NodeStorageImpl;

import java.io.*;
import java.util.List;

/**
 * Разделение файла на геометрию, атрибуты и индексы
 */
public class Old2NewFormat
{
	public static void main(final String[] args) throws Exception
	{
		final String nm = new File(args[0]).getName();

		StreamProjImpl proj = new StreamProjImplConvert(
		args[0],
		new CnStyleRuleFactory()
		{
			 public IDrawObjRule createByTypeName(String typeRule) throws Exception
			 {
				 if (typeRule.equals(InscriptionRule.RULETYPENAME))
					 return new InscriptionRule();
				 return super.createByTypeName(typeRule);
			 }
		},
		null,
		new DefIFactory<INodeStorage>()
		{
				public INodeStorage createByTypeName(String typeStorage) throws Exception
				{
					if (typeStorage.equalsIgnoreCase(NodeStorageImpl.TYPENAME))
						return new NodeStorageImplConvert();

					return new MemStorageLrConverter(args[0],args[1]+"\\"+nm);
				}
		},
			null,null,null,null
		);
        proj.loadFromStream(new DataInputStream(new BufferedInputStream(new FileInputStream(args[0]))));

//		List<ILayer> ll = proj.getLayerList();
//		ll.get(0).getStorage().ge

		DataOutputStream os = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(args[0] + "_exp")));
		proj.savetoStream(os);
		os.flush();
		os.close();
	}

}
