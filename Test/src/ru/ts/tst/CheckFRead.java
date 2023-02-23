package ru.ts.tst;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.FileOutputStream;

/**
 * Created by IntelliJ IDEA.
 * User: vladm
 * Date: 07.07.2011
 * Time: 17:51:30
 * To change this template use File | Settings | File Templates.
 */
public class CheckFRead
{
	private static final int BUFF_SZ = 100;

	public static void main(String[] args) throws Exception
	{

		new Thread()
		{
			public void run()
			{
				try
				{
					for (int i = 0; i < 10; i++)
					{
						startRead(i*10);
						Thread.sleep(10);
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
				}
			}
		}.start();
		Thread.sleep(10);
		for (int i = 0; i < 10; i++)
		{
			startRead(i);
			Thread.sleep(10);
		}
	}

	private static void startRead(int ix)
			throws IOException
	{
		FileInputStream ex = new FileInputStream("G:\\BACK_UP\\$D\\Program Files\\Apache Software Foundation\\Tomcat 5.5\\bin\\projs\\pskov\\0_[POI]_0x1000.gm");
		//FileOutputStream ex2 = new FileInputStream("G:\\BACK_UP\\$D\\Program Files\\Apache Software Foundation\\Tomcat 5.5\\bin\\projs\\pskov\\0_[POI]_0x1000.gm");
		System.out.println("ix = " + ix);
		int r=0;
		byte[] b = new byte[BUFF_SZ];
		while((r+=ex.read(b))< BUFF_SZ)
		{
			System.out.println("r = " + r);
		}
		System.out.println("b = " + new String(b)+" "+ ix);
		//ex.close();
	}
}
