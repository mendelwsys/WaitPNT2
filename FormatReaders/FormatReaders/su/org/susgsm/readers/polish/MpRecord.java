package su.org.susgsm.readers.polish;

import ru.ts.utils.data.Pair;

import java.util.List;
import java.util.LinkedList;
import java.io.BufferedReader;
import java.io.IOException;

import su.org.susgsm.readers.ParseRecException;

/**
 * Запись польского формата
 */
public class MpRecord
{
	public static final String KEYLINE = "[POLYLINE]";
	public static final String KEYPNT = "[POI]";
	public static final String KEYGON = "[POLYGON]";

	public static final String KEYEND = "[END]";

	public static final String HEADERSTART ="[IMG ID]";
	public static final String HEADEREND ="[END-IMG ID]";


	public static final String[] arrtype = new String[]{KEYLINE, KEYPNT, KEYGON};
	protected String startRecLine;
	protected String endRecLine;
	protected List<String> startcomments = new LinkedList<String>(); //Список комментариев
	protected List<Pair<String, String>> parnames = new LinkedList<Pair<String, String>>();//Пара параметр -> значение

	public MpRecord()
	{

	}


	public MpRecord(List<String> startcomments, List<Pair<String, String>> parnames)
	{
		if (startcomments!=null)
			this.startcomments = startcomments;
		if (parnames!=null)
			this.parnames = parnames;
	}
	public MpRecord(MpRecord obj)
	{
		this.startcomments=new LinkedList<String>(obj.startcomments);
		for (Pair<String, String> l_parname : obj.parnames)
			this.parnames.add(new Pair<String, String>(l_parname.first,l_parname.second));
	}

	public String getStartRecLine()
	{
		return startRecLine;
	}

	public String getEndRecLine()
	{
		return endRecLine;
	}

	public List<String> getStartcomments()
	{
		return startcomments;
	}

	public List<Pair<String, String>> getParnames()
	{
		return parnames;
	}

	public String getParamByName(String parmame)
	{
		for (Pair<String, String> l_parname : parnames)
			if (l_parname.first.equalsIgnoreCase(parmame))
				return l_parname.second;
		return null;
	}

	public boolean parseLine(String[] spl)
	{
		return false;
	}
	
	public MpRecord initByReader(BufferedReader reader,String line) throws IOException, ParseRecException
	{

		startRecLine=line;
		while (!(line = reader.readLine()).trim().startsWith("["))
		{
			String[] spl = line.split("=");
			if (spl == null || spl[0] == null)
				throw new ParseRecException();
			if (spl.length > 1 && !parseLine(spl))
				parnames.add(new Pair<String, String>(spl[0], spl[1]));
		}
		endRecLine=line;
		return this;
	}

}
