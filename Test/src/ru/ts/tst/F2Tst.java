package ru.ts.tst;

import java.io.*;

/**
 * Created by IntelliJ IDEA.
 * User: vladm
 * Date: 03.07.2010
 * Time: 19:14:03
 * To change this template use File | Settings | File Templates.
 */
public class F2Tst
{
	public static void main(String[] args) throws Exception
	{
		//new File("D:\\MAPDIR\\MP\\x.txt").setWritable();
		FileInputStream fis = new FileInputStream("D:\\MAPDIR\\MP\\x.txt");
		System.out.println("1:"+new BufferedReader(new InputStreamReader(fis)).readLine());

		new Thread()
		{
			public void run()
			{
				try
				{

					FileOutputStream fis2 = new FileOutputStream("D:\\MAPDIR\\MP\\x.txt",true);
					fis2.write(1);
//					FileInputStream fis2 = new FileInputStream("D:\\MAPDIR\\MP\\x.txt");
//					System.out.println("2:"+new BufferedReader(new InputStreamReader(fis2)).readLine());

				}
				catch (IOException e)
				{
					e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
				}
			}
		}.start();

		Thread.sleep(1000);

	}
}
