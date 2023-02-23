package ru.ts.toykernel.test.converter.tonewformat;

import ru.ts.toykernel.storages.mem.NodeStorageImpl;
import ru.ts.toykernel.storages.INodeStorage;

import java.io.DataOutputStream;
import java.io.PrintWriter;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.LinkedList;

/**
 * Конвертер в описатель xml
 */
public class NodeStorageImplConvert extends NodeStorageImpl
{
	public void savetoStream(DataOutputStream dos) throws Exception
	{
		List<String> l_set=new LinkedList<String>(storages.keySet());

		PrintWriter prnwr = new PrintWriter(new OutputStreamWriter(dos,"WINDOWS-1251"));
		prnwr.println("<storages>");

		for (int i = 0; i < l_set.size(); i++)
		{
			String lr_id = l_set.get(i);
			INodeStorage nodeStorage = storages.get(lr_id);

			prnwr.println("\t<storage>");
			prnwr.println("\t\t<storage-name>"+nodeStorage.getNodeId()+"</storage-name>");
			prnwr.println("\t\t<class-name>\n\t\t\t"+nodeStorage.getClass().getCanonicalName()+"\n\t\t</class-name>");
			prnwr.println("\t\t<params>");
			if (nodeStorage instanceof MemStorageLrConverter)
			{
				prnwr.println("\t\t\t<param Val=\""+((MemStorageLrConverter)nodeStorage).getTargetfolder()+"\"/>");
				nodeStorage.savetoStream(dos);
			}
			prnwr.println("\t\t</params>");
			prnwr.println("\t</storage>");
		}
		prnwr.println("\t<storage>");
		prnwr.println("\t\t<storage-name>MAIN_STORAGE</storage-name>");
		prnwr.println("\t\t<class-name>\n\t\t\t"+getClass().getCanonicalName()+"\n\t\t</class-name>");
		prnwr.println("\t\t<params>");
			for (int i = 0; i < l_set.size(); i++)
			{
				String lr_id = l_set.get(i);
				INodeStorage nodeStorage = storages.get(lr_id);
				prnwr.println("\t\t\t<storage>"+nodeStorage.getNodeId()+"</storage>");
			}
		prnwr.println("\t\t</params>");
		prnwr.println("\t</storage>");

		prnwr.println("</storages>");
		prnwr.flush();
	}

}

