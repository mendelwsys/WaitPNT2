package su.org.imglab.clengine;

import ru.ts.utils.data.Pair;

/**
 * Created by IntelliJ IDEA.
 * User: vladm
 * Date: 12.09.2008
 * Time: 15:52:41
 * To change this template use File | Settings | File Templates.
 */
public class CodePair
{
	Pair<String, String> pr;

	CodePair(String code, String name)
	{
		pr = new Pair<String, String>(code, name);
	}

	public String toString()
	{
		return pr.second;
	}

	public String getCode()
	{
		return pr.first;
	}
}
