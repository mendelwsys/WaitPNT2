package su.org.susgsm.readers.polish;

import su.org.susgsm.readers.Data;

import java.util.LinkedList;
import java.util.List;


/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: 09.03.2008
 * Time: 11:43:47
 * To change this template use File | Settings | File Templates.
 */
public class Utils
{
	private Utils(){}

	public static Data[] generateDataArrayByString(String str)
	{
		List<Data> rv= new LinkedList<Data>();

		while (str.length()>0)
		{
			int si=str.indexOf('(')+1;
			int ei=str.indexOf(',',si);
			int ei2=str.indexOf(')',ei);
			try
			{
				rv.add(
			new Data(Double.parseDouble(str.substring(si,ei)),
						Double.parseDouble(str.substring(ei+1,ei2))));
			}
			catch (NumberFormatException e)
			{
				e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
			}
			str=str.substring(ei2+1);
		}
		return rv.toArray(new Data[0]);
	}

	public static String toString(Data data)
	{
			return "(lat(S):"+data.getLat()+",lon(D):"+data.getLon()+")";
	}



}
