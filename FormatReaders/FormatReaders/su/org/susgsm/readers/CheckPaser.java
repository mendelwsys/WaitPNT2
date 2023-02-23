package su.org.susgsm.readers;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: 12.04.2008
 * Time: 14:55:25
 *
 */
public class CheckPaser
{
	public static void main(String[] args)
	{
		String strName="SDJGKLD EK. (kskjfjlkldsk)";
		int pindex=strName.indexOf(".");
		if (pindex>0)
		{
			int spindex=-1;
			int currindex=-1;
			while ((currindex=strName.indexOf(" ",spindex))>0 && pindex>currindex)
				spindex=currindex+1;
			if (spindex>0)
			{
				String stype=strName.substring(spindex,pindex);
				String newstrName=strName.substring(0,spindex).trim()+" "+strName.substring(pindex+1).trim();
				System.out.println("newstrName = " + newstrName);
				System.out.println("stype = " + stype);

//				objs.put(newstrName,objs.remove(strName));
//				strtypes.add(stype);
			}
		}

	}
}
