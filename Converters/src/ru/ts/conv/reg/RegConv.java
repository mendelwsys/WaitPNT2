package ru.ts.conv.reg;

/**
 * Created by IntelliJ IDEA.
 * User: vladm
 * Date: 29.06.2011
 * Time: 12:30:57
 * To change this template use File | Settings | File Templates.
 */
public class RegConv
{


//	final static double Pi = 3.14159265358979323846;
//	final static double Degree = Pi/180.0;
	final static double a = 6378137.0;             //(WGS84 semimajor)

//final double es = 298.257223563;      //(WGS84 inverse flattening parameter)
//final double e =  ;   //

//!!!!!!!!!!!!!!!!!! here PE90 flattening with WGS84 axes
	final static double es = 6.69342749e-3;
	final static double e =  0.08181336987314;     //=sqrt(es);
	final static double es2 =es*es;
	final static double es3 =es2*es;

//final double k0 = 0.994;
	final static double k0 = 1.0;                  //as for Gauss-Krueger

	static double M(double phi)
	{
		if (phi == 0.0)
			return 0.0;
		else {
			return a * (
				( 1.0 - es/4.0 - 3.0*es*es/64.0 - 5.0*es*es*es/256.0 ) * phi -
				( 3.0*es/8.0 + 3.0*es*es/32.0 + 45.0*es*es*es/1024.0 ) * Math.sin(2.0 * phi) +
				( 15.0*es*es/256.0 + 45.0*es*es*es/1024.0 ) * Math.sin(4.0 * phi) -
				( 35.0*es*es*es/3072.0 ) * Math.sin(6.0 * phi) );
		}
	}

	public static  void MAPtoGEO(double x, double y, double lat0, double lon0, double[] lat, double[] lon)
	{
		double	lambda0 = lon0 ;
		double	phi0 = lat0;




		double
			m0 = M(phi0),
			et2 = es / (1. - es),
			m = m0 + y / k0,
			e1 = (1.- Math.sqrt(1.- es)) / (1.+ Math.sqrt(1.- es)),
			mu = m / (a * (1.- es/4.- 3.* es2/64.- 5.* es3/256.)),

			phi1 = mu + (3.* e1/2.- 27.* Math.pow(e1, 3.)/32.) * Math.sin(2.* mu)
				+ (21.* e1*e1/16.- 55.* Math.pow(e1, 4.)/32.)
				* Math.sin(4.* mu) + 151.* Math.pow(e1, 3.)/96.* Math.sin(6.* mu)
				+ 1097.* Math.pow(e1, 4.)/512.* Math.sin(8.* mu),

			c1 = et2 * Math.pow(Math.cos(phi1), 2.),
			t1 = Math.pow(Math.tan(phi1), 2.),
			n1 = a / Math.sqrt(1 - es * Math.pow(Math.sin(phi1), 2.)),
			r1 = a * (1.- es) / Math.pow(1.- es * Math.pow(Math.sin(phi1), 2.), 1.5),
			d = x / (n1 * k0);

		lat[0] = phi1 - n1 * Math.tan(phi1) / r1
				* (d*d / 2.- (5.+ 3.* t1 + 10.* c1 - 4.* c1*c1 - 9.* et2)
				* Math.pow(d, 4.) / 24.+ (61.+ 90.* t1 + 298.* c1 + 45.* t1*t1
				- 252.* et2 - 3.* c1*c1) * Math.pow(d, 6.) / 720.);
		lon[0] = lambda0 + (d - (1.+ 2.* t1 + c1) * Math.pow(d, 3.)/6.
				+ (5.-2.* c1 + 28.* t1 - 3.* c1*c1 + 8.* et2 + 24.* t1*t1)
				* Math.pow(d, 5.)/120.) / Math.cos(phi1);
	}

	void GEOtoMAP(double latitude, double longitude, double lat, double lon, double[] x, double[] y)
	{
// (lat,lon)->(XY на плоскости)

		//Warning!!! (lat >= -80. degree) && (lat <= 84. degree) !!!!!!!!!

		double	et2,n,t,tp,c,A,A2,A3;
		double	phi= Math.abs(lat),
				lambda=lon;

		double	phi0=Math.abs(latitude),
				lambda0=longitude;

		double	m0=M(phi0),
				m=M(phi);

		et2=es/(1.-es);
		A=Math.cos(phi);
		c=et2*A*A;
		A=(lambda-lambda0)*A;
		A2=A*A;
		A3=A2*A;
		n=Math.sin(phi);
		n=a/Math.sqrt(1-es*n*n);
		tp=Math.tan(phi);
		t=tp*tp;

		x[0] = k0*n*(A + (1. - t + c)*A3/6. + (5. - 18.*t + t*t + 72.*c - 58.*et2)*A3*A2 / 120.);
		y[0] = k0*(m - m0 + n*tp*(A2/2. + (5. - t + 9.*c + 4*c*c)*A2*A2/24. + (61. - 58.*t + t*t + 600.*c - 330.*et2)*A3*A3 /720.));
	}

}
