package su.org.ms.parsers.mathcalc;

import java.lang.reflect.Method;
import java.lang.reflect.Field;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: 11.08.2007
 * Time: 22:06:47
 * To change this template use File | Settings | File Templates.
 */
public class MathInvoker
{

	public static double invoke(String fname, Double... args) throws Exception
	{
		Class[] cls = new Class[args.length];
		for (int i = 0; i < cls.length; i++)
			cls[i] = double.class;
		Method mt;
		if (args.length <= 2)
			mt = Math.class.getDeclaredMethod(fname, cls);
		else
			mt = MathExtender.class.getDeclaredMethod(fname, cls);

		return (Double) mt.invoke(null, args);
	}

	public static double getConstant(String fname) throws Exception
	{
		Field fld;
		fld = Math.class.getDeclaredField(fname);

		return fld.getDouble(null);
	}

	public static void main(String[] args) throws Exception
	{
//		System.out.println("obj = " + invoke("random"));
		System.out.println("pi = " + getConstant("PI"));

	}

	public static class MathExtender
	{
		static public double testit(double a1, double a2, double a3)
		{
			return a1 + a2 + a3;
		}
	}

}
