package ru.ts.toykernel.test.converter.tonewformat;

import ru.ts.toykernel.storages.mem.MemStorageLr2;
import ru.ts.toykernel.storages.INodeStorage;
import ru.ts.toykernel.storages.InitStorageException;
import ru.ts.toykernel.attrs.AObjAttrsFactory;
import ru.ts.toykernel.attrs.IAttrs;
import ru.ts.toykernel.attrs.IDefAttr;
import ru.ts.toykernel.consts.INameConverter;
import ru.ts.utils.logger.SimpleLogger;
import ru.ts.utils.data.Pair;
import ru.ts.gisutils.algs.common.MRect;

import java.io.*;
import java.util.Set;
import java.util.HashSet;

/**
 * 
 */
public class MemStorageLrConverter extends MemStorageLr2
{
	protected String targetfolder;

	public MemStorageLrConverter(AObjAttrsFactory attrsfactory, IAttrs defAttrs, INameConverter storNm2CodeNm, String folderlayers)
			throws InitStorageException
	{
		super(attrsfactory, defAttrs, storNm2CodeNm, folderlayers);
	}

	public MemStorageLrConverter(String folderlayers,String targetfolder)
			throws InitStorageException
	{
		super(folderlayers);
		this.targetfolder = targetfolder;
		new File(this.targetfolder).mkdir();
	}

	public String getTargetfolder()
	{
		return targetfolder;
	}

	public void setTargetfolder(String targetfolder)
	{
		this.targetfolder = targetfolder;
	}

	public void savetoStream(DataOutputStream dos) throws Exception
	{
//		if (false)
		{
			File fl = new File(flnm);
			long ln= fl.length();
			dos=new DataOutputStream(new FileOutputStream(targetfolder+"/"+nodeId+".gm"));
			//Сохраняем байтовый массив
			BufferedInputStream fin = new BufferedInputStream(new FileInputStream(fl));
			byte[] curveArray=new byte[8*1024];
			int sz = 0;
			int count = 0;
			while ((sz += count) < ln && (count = fin.read(curveArray)) >= 0)
				dos.write(curveArray, 0, count);
			if (count < 0)
			{
				SimpleLogger.Singleton.getLoger().getLog().println("Error reading map form file");
				throw new EOFException("Error reading map form file");
			}
			fin.close();
			dos.flush();
			dos.close();

			dos=new DataOutputStream(new FileOutputStream(targetfolder+"/"+nodeId+".ix"));
//Сохраняем графические объекты
			dos.writeInt(graphobkects.size());
			for (String key : curveorder)//Кривые Сохраняются в порядке рисования
			{
				String[] lrId2curveId = key.split("[" + INodeStorage.GROUP_SEPARATOR + "]");
				dos.writeUTF(lrId2curveId[2]);
				Pair<Integer, MRect> index2mbb = graphobkects.get(key);
				dos.writeInt(index2mbb.first);
				dos.writeDouble(index2mbb.second.p1.x);
				dos.writeDouble(index2mbb.second.p1.y);
				dos.writeDouble(index2mbb.second.p4.x);
				dos.writeDouble(index2mbb.second.p4.y);
			}

			dos.flush();
			dos.close();

			dos=new DataOutputStream(new FileOutputStream(targetfolder+"/"+nodeId+".dat"));
//сначала напишем атрибуты объектов по умолчанию
			IAttrs attrs=getDefAttrs();
			dos.writeInt(attrs.size());
			for (String attrkey : attrs.keySet())
			{
				ObjectOutputStream obis = new ObjectOutputStream(dos);
				obis.writeUTF(attrs.get(attrkey).getName());
				obis.writeObject(attrs.get(attrkey).getValue());
				obis.flush();
			}
//допишем атрибуты объектов
			for (String curveId : curveorder)
			{
				int objsizeattrs = 0;
				IAttrs objattrs = pool.get(curveId);

				Set<String> objkeyattrs = null; //формируем те ключи объекты по которым отличаются от объектов по умолчанию
				if (objattrs!=null)
				{
					objkeyattrs = new HashSet<String>();
					for (String keyattr : objattrs.keySet())
					{
						IDefAttr iDefAttr = defAttrs.get(keyattr);
						if (iDefAttr == null || !iDefAttr.equals(objattrs.get(keyattr)))
							objkeyattrs.add(keyattr);
					}
					objsizeattrs = objkeyattrs.size();
				}
				dos.writeInt(objsizeattrs);//кол-во атрибутов
				if (objsizeattrs > 0)
				{
					dos.writeInt(graphobkects.get(curveId).first);//индекс кривой которой принадлежат аттрибуты
					for (String keyattr : objkeyattrs)
					{
						IDefAttr objAttr = objattrs.get(keyattr);
						ObjectOutputStream objos = new ObjectOutputStream(dos);
						objos.writeUTF(objAttr.getName());
						objos.writeObject(objAttr.getValue());
						objos.flush();
					}
				}
			}
			dos.flush();
			dos.close();
		}
	}

}
