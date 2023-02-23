package ru.ts.toykernel.plugins.gissearch2.gsconn;
public class FoundObj 
{
	public String nameobj;
	public String[] objids;

	public FoundObj ()
	{
	}

	public FoundObj (
			String nameobj,
			String[] objids )
	{
			this.nameobj=nameobj;
			this.objids=objids;
	}

}
