package su.org.ms.utils;

public class Pair<F, S>
{
	public F first;
	public S second;

	public Pair(F first, S second)
	{
		this.first = first;
		this.second = second;
	}
	public String toString()
	{
		return (first!=null?first.toString():"null")+"_"+(second!=null?second.toString():"null");
	}

	public boolean equals(Object o)
	{
		if (o instanceof Pair)
		{
			Pair pr=(Pair)o;
			return (first!=null?first.equals(pr.first):first==pr.first) &&
					(second!=null?second.equals(pr.second):second==pr.second);
		}
		return false;
	}

	public int hashCode()
	{
		if (first==null || second==null)
			System.out.println("first = " + first);
		return (first.toString()+"_"+second.toString()).hashCode();
	}
	
}
