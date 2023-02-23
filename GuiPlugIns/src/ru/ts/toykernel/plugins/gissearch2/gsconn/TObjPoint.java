package ru.ts.toykernel.plugins.gissearch2.gsconn;
public class TObjPoint 
{
	public String objId;
	public TPoint[] pnts;

	public TObjPoint ()
	{
	}

	public TObjPoint (
			String objId,
			TPoint[] pnts )
	{
			this.objId=objId;
			this.pnts=pnts;
	}

}
