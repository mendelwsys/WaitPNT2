package ru.ts.tapp;

import su.web.WebUtils;

import java.net.URL;
import java.net.HttpURLConnection;
import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * Пример как надо использовать keep-alive
 */
public class THTTPAlive
{
	public static void main(String[] args) throws Exception
	{

		URL urlDivider = new URL("http://localhost:80/applet/tst/jspgetter.jsp");


		for (int i=0;i<100;i++)
			communicate(urlDivider);

		System.in.read();
	}

	public static void communicate(URL urlDivider)
			throws Exception
	{
		DataInputStream dis=null;
		DataOutputStream dos = null;

		try
		{
			HttpURLConnection urlConnection = (HttpURLConnection) urlDivider.openConnection();
			urlConnection.setRequestProperty("Connection", "keep-alive");
			urlConnection.setRequestMethod("POST");

			urlConnection.setDoOutput(true);
			urlConnection.setDoInput(true);

			dos = new DataOutputStream(urlConnection.getOutputStream());
			StringBuffer str = new StringBuffer("Hello session");
			for (int i=0;i<11;i++)
				str.append(str.toString());

			dos.writeUTF(str.toString());

			dis=new DataInputStream(urlConnection.getInputStream());
			System.out.println("Answer:"+dis.readUTF());
		}
		finally
		{
//			if (dos!=null)
//				dos.close();
			if (dis!=null)
				dis.close();
		}
	}
}
