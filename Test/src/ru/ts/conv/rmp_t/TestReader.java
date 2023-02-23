package ru.ts.conv.rmp_t;

import su.org.susgsm.readers.polish.MpRecord;

import java.util.Iterator;
import java.io.BufferedInputStream;
import java.io.FileInputStream;

import ru.ts.conv.rmp.MPImporter;

/**
 * Created by IntelliJ IDEA.
 * User: vladm
 * Date: 14.06.2010
 * Time: 11:05:13
 * To change this template use File | Settings | File Templates.
 */
public class TestReader
{
	public static void main(String[] args) throws Exception
	{
		String encoding = "WINDOWS-1251";

		FileInputStream is = new FileInputStream("D:\\MAPDIR\\POLY\\Moskva.mp"); //Файл преобразования

		MPImporter imp = new MPImporter();

		Iterator<MpRecord> itr = imp.getMPByFileName(new BufferedInputStream(is), encoding, null);

		long starttime = System.currentTimeMillis();

		int i=0;
		while (itr.hasNext())
		{
			i++;
			itr.next();
			if (i%10000==0)
				System.out.println("i = " + i);	
		}
		System.out.println("delta = " + (System.currentTimeMillis()-starttime));
	}
}
