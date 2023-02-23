package su.org.susgsm.readers;

/**
 * Данные о координатах объекта, широта, долгота
 */
public class Data
{
	protected double[] latlon=new double[2];

	public Data(Data data)
	{
		latlon[0]=data.getLat();
		latlon[1]=data.getLon();
	}
	
	public Data(double lat,double lon)
	{
		latlon[0]=lat;
		latlon[1]=lon;
	}
	public Data(double[] latlon)
	{
		this.latlon=latlon;
	}

	public double getLat() {return latlon[0];}
	public double getLon() {return latlon[1];}
}
