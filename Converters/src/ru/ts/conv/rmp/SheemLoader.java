package ru.ts.conv.rmp;

import ru.ts.toykernel.consts.KernelConst;
import ru.ts.csvreader.ReaderUtils;
import ru.ts.conv.IColorSheemLoader;

import java.util.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Загрузчик таблицы цветовой схемы
 */
public class SheemLoader
		implements IColorSheemLoader
{

	//Извесные имена столбцов
	final public static String RGN ="RGN"; //Геометрический тип объекта
	final public static String NTYPE="NTYPE";//Номер типа

	final public static String COLOR_LINE= KernelConst.ATTR_COLOR_LINE;
	final public static String COLOR_FILL= KernelConst.ATTR_COLOR_FILL;

	final public static String LINE_STYLE= KernelConst.ATTR_LINE_STYLE;
	final public static String LINE_THICKNESS= KernelConst.ATTR_LINE_THICKNESS;
	final public static String TEXT_MODE="TEXT_MODE";
	final public static String IMAGE="FIMAGE";
	final public static String CENTRALIMGPNT=KernelConst.ATTR_IMG_CENTRALPOINT;


	final public static String LRNAME= KernelConst.LAYER_NAME;

	final public static String AS_NAME = "AsName";
	static protected SheemLoader sheem;

	private SheemLoader(){}

	public static IColorSheemLoader getInstance()
	{
		if (sheem==null)
			sheem=new SheemLoader();
		return sheem;
	}

	static public Map<String, List<String>> loadScheem(String del, List<String> headers,String colorschemePath) throws Exception
	{
		BufferedReader br=new BufferedReader(new FileReader(colorschemePath));
		return loadScheem(del,headers,br);
	}

	static public Map<String, List<String>> loadScheem(String del, List<String> headers, BufferedReader br)
			throws IOException
	{
		String bln = null;
		{
			bln = br.readLine();//HEADERS
			{
				StringTokenizer st = new StringTokenizer(bln, del);
				try
				{
					while (st.hasMoreTokens())
						headers.add(st.nextToken().trim());
				}
				catch (NoSuchElementException e)
				{//
				}
			}
		}

		//Формат Название столбца ->Столбец
		Map<String, List<String>> mp = new HashMap<String, List<String>>();

		ReaderUtils.readTuples(del, headers, br, mp);

		Set<String> stringSet = new HashSet<String>(mp.keySet());
		for (String s : stringSet)
			mp.put(s, new ArrayList<String>(mp.get(s)));
		return mp;
	}

	public void reset()
	{
		//To change body of implemented methods use File | Settings | File Templates.
	}

	public int findByKey(String key) throws Exception
	{
		return 0;
	}

	public String getObjectType()
	{
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	public String getLineColor()
	{
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	public String getFillColor()
	{
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	public String getLineStroke()
	{
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	public String getLineThickness()
	{
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	public String getGroupName()
	{
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	public boolean hasNext()
	{
		return false;  //To change body of implemented methods use File | Settings | File Templates.
	}

	public Object next()
	{
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	public void remove()
	{
		//To change body of implemented methods use File | Settings | File Templates.
	}
}
