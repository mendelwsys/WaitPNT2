package ru.ts.tapp;

import su.org.coder.utils.String0AHelper;

import java.io.IOException;

import org.apache.xerces.utils.Base64;


/**
 * Показывает не еквивалентность кодирования разными кодерами BASE64!!!!
  */
public class Encoding
{
	public static void main(String[] args) throws IOException
	{
		byte[] rv = new String0AHelper().serialChannelObj("СУЙДА Павильон билетно-кассовый");
		String enc=com.sun.org.apache.xerces.internal.impl.dv.util.Base64.encode(rv);
		byte[] bt = com.sun.org.apache.xerces.internal.impl.dv.util.Base64.decode(enc);
		boolean b = new String(enc.getBytes("WINDOWS-1251")).equals(enc);

		System.out.println("b = " + b);
		byte[] bt2=Base64.decode(enc.getBytes());

		String oname= (String) new String0AHelper().createChannelObj(bt2);
		System.out.println("oname = " + oname);
	}
}
