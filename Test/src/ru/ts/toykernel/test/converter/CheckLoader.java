package ru.ts.toykernel.test.converter;

import ru.ts.toykernel.proj.stream.def.StreamProjImpl;
import ru.ts.toykernel.drawcomp.rules.def.stream.CnStyleRuleFactory;
import ru.ts.toykernel.storages.INodeStorage;
import ru.ts.toykernel.storages.mem.MemStorageLr2;
import ru.ts.toykernel.storages.mem.NodeStorageImpl;
import ru.ts.factory.DefIFactory;

import java.io.*;

/**
 * Created by IntelliJ IDEA.
 * User: vladm
 * Date: 17.03.2009
 * Time: 16:53:55
 * To change this template use File | Settings | File Templates.
 */
public class CheckLoader
{
	public static void main(final String[] args) throws Exception
	{
		StreamProjImpl proj = new StreamProjImpl(args[0],	new CnStyleRuleFactory(),null,new DefIFactory<INodeStorage>()
			{
				public INodeStorage createByTypeName(String typeStorage) throws Exception
				{
					if (typeStorage.equalsIgnoreCase(NodeStorageImpl.TYPENAME))
						return new NodeStorageImpl();
					return new MemStorageLr2(args[0]);
				}
			},null,null,null,null);
		
		proj.loadFromStream(new DataInputStream(new BufferedInputStream(new FileInputStream(args[0]))));

//		ByteArrayOutputStream bos = new ByteArrayOutputStream();
//		ISerializer serializer = (ISerializer) pcntxt.getStorage();
//		serializer.savetoStream(new DataOutputStream(bos));
//		serializer.loadFromStream(new DataInputStream(new ByteArrayInputStream(bos.toByteArray())));

		DataOutputStream os = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(args[0] + "_exp")));
		proj.savetoStream(os);
		os.flush();
		os.close();
	}
}