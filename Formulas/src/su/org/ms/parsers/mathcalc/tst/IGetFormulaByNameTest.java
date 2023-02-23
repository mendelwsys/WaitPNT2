package su.org.ms.parsers.mathcalc.tst;

import su.org.ms.parsers.mathcalc.IGetFormulaByName;
import su.org.ms.parsers.mathcalc.MathInvoker;
import su.org.ms.parsers.common.ParserException;
import su.org.ms.utils.Pair;

import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: 11.08.2007
 * Time: 20:39:29
 *
 */
public class IGetFormulaByNameTest implements IGetFormulaByName
{

	HashMap<String, Pair<String,Double>> hm = new HashMap<String, Pair<String,Double>>();

	public IGetFormulaByNameTest()
	{
		double i3= 3;
		double d121=2.1;
		double _i221=221;
		double _d221=0.21;

		hm.put("i3", new Pair<String,Double>("3",i3));

		hm.put("d121",  new Pair<String,Double>("2.1",d121));
		hm.put("_i221",  new Pair<String,Double>("221",_i221));
		hm.put("_d221",  new Pair<String,Double>("0.21",_d221));




		hm.put("tst0", new Pair<String,Double>("d121 / _i221",d121 / _i221));
//		hm.put("tst1", new Pair<String,Double>("-1.0*d121 / 23.1",-1.0*d121 / 23.1));

		hm.put("tst1", new Pair<String,Double>("12.3-23+d121 * _i221",12.3-23+d121 * _i221));
//		hm.put("tst2", new Pair<String,Double>("-(12.3- -23+d121) * -_i221",-(12.3- -23+d121) * -_i221));
//		hm.put("tst2", new Pair<String,Double>("(12.3- 23+d121) * _i221",(12.3- 23+d121) * _i221));
		hm.put("tst2", new Pair<String,Double>("12.3+ 23-d121 / _i221",12.3+ 23-d121 / _i221));

		hm.put("tst3", new Pair<String,Double>("(d121+23-13.13) / 11.1",(d121+23-13.13) / 11.1));

//		hm.put("tst3", new Pair<String,Double>("((12.3-23)*d121) * _d221",((12.3-23)*d121) * _d221));
//		hm.put("tst4", new Pair<String,Double>("-random()",-Math.random()));
		hm.put("tst4", new Pair<String,Double>("random()",Math.random()));

//		hm.put("tst5", new Pair<String,Double>("-testit(12.1,-13.1,14.1)",-MathInvoker.MathExtender.testit(12.1,-13.1,14.1)));
		hm.put("tst5", new Pair<String,Double>("testit(12.1,13.1,14.1)",MathInvoker.MathExtender.testit(12.1,13.1,14.1)));

//		hm.put("tst6", new Pair<String,Double>("testit(-_i221,d121,-_d221)", MathInvoker.MathExtender.testit(-_i221,d121,-_d221)));


		hm.put("tst6", new Pair<String,Double>("2*(sin(d121+0.2*2))",2*(Math.sin(d121+0.2*2))));
		hm.put("tst7", new Pair<String,Double>("(2+(sin(d121+0.2*2)))+1.1*4-1",(2+(Math.sin(d121+0.2*2)))+1.1*4-1));
		hm.put("tst8", new Pair<String,Double>("(2+sin(d121+0.2*2))+1.1*4-1",(2+Math.sin(d121+0.2*2))+1.1*4-1));


		hm.put("tst9", new Pair<String,Double>("_i221+sin(cos(0.2*(d121+1))+_d221)",_i221+Math.sin(Math.cos(0.2*(d121+1))+_d221)));

		hm.put("tst10", new Pair<String,Double>("3*4+sin(2.1)*13+11",3*4+Math.sin(2.1)*13+11));

		hm.put("tst11", new Pair<String,Double>("2*max(_d221,_i221)*12.3-23+d121*i3 /  _i221/i3",2*Math.max(_d221,_i221)*12.3-23+d121*i3 /  _i221/i3));

		hm.put("tst12", new Pair<String,Double>("12*PI+23",12*Math.PI+23));
		hm.put("tst13", new Pair<String,Double>("12*sin(PI/2+0.12)+23",12*Math.sin(Math.PI/2+0.12)+23));
//		hm.put("tst14", new Pair<String,Double>("12*-sin(PI/2+0.12)+-23?12.0/15.0+13.0:17.0",12*-Math.sin(Math.PI/2+0.12)+-23!=0?12.0/15.0+13.0:17.0)); //Проверка условий 1
		hm.put("tst14", new Pair<String,Double>("12*sin(PI/2+0.12)+23?12.0/15.0+13.0:17.0",(12*Math.sin(Math.PI/2+0.12)+23!=0)?12.0/15.0+13.0:17.0)); //Проверка условий 1

		hm.put("tst15", new Pair<String,Double>("12*sin(PI/2)-12?12.0/15.0+13.0:17.0",12*Math.sin(Math.PI/2)-12!=0?12.0/15.0+13.0:17.0)); //Проверка условий 2
		hm.put("tst16", new Pair<String,Double>("12*cos(PI/2)-12+4*3?12.0/15+13:2*max(_d221,_i221)*12.3-23+d121*i3 /  _i221/i3",12*Math.cos(Math.PI/2)-12+4*3!=0?12.0/15+13:2*Math.max(_d221,_i221)*12.3-23+d121*i3 /  _i221/i3)); //Проверка условий 3

		hm.put("tst17", new Pair<String,Double>("(17.1-_d221+d121) * _d221",(17.1-_d221+d121) * _d221));

//		hm.put("tst17", new Pair<String,Double>("12.3-23!=d121 * _i221",(12.3-23 != d121 * _i221)?1.0:0.0));

		hm.put("tst18", new Pair<String,Double>("12.3-23==d121 * _i221",(12.3-23 == d121 * _i221)?1.0:0.0));
		hm.put("tst19", new Pair<String,Double>("((12.3-23)!=d121) ? 12.3-12.0!=d121 * _i221?3:33*33:0-3",(12.3-23!=d121) ? ((12.3-12.0!=d121 * _i221)?3.0:33.0*33):0-3));

		hm.put("tst20", new Pair<String,Double>("(17.1-_d221+d121) * _d221",(17.1-_d221+d121) * _d221));
		hm.put("tst21", new Pair<String,Double>("-testit(_d221,-_i221,_d221)", -MathInvoker.MathExtender.testit(_d221,-_i221,_d221)));

//		hm.put("tst22", new Pair<String,Double>("(12.0/_d221-13.1)?31.2:43.2+-13.0",
//												 (12.0/_d221-13.1>0)?31.2:43.2+-13.0)); //Проверка условий 2

		hm.put("tst22", new Pair<String,Double>("-12*sin(-PI/2)+-12.34>0?12.0/_d221-13.1?31.2:43.2+-13.0:-17.0",
												 -12*Math.sin(-Math.PI/3.1)+-12.34>0?12.0/_d221-13.1!=0?31.2:43.2+-13.0:-17.0)); //Проверка условий 2

		hm.put("tst23", new Pair<String,Double>("-12*sin(-PI/2)+-12.34<0?12.0/_d221-13.1?(_d221*_i221/d121-+12.45?11.0:13.2):43.2+-13.0:-17.0",
												 -12*Math.sin(-Math.PI/3.1)+-12.34<0?12.0/_d221-13.1!=0?(_d221*_i221/d121-+12.45!=0?11.0:13.2):43.2+-13.0:-17.0)); //Проверка условий 2

		hm.put("tst24", new Pair<String,Double>("-12*sin(-PI/2)+-12.34<0?12.0/_d221-13.1?(_d221*_i221/d121-+12.45==0?11.0:13.2):43.2+-13.0:-17.0",
												 -12*Math.sin(-Math.PI/3.1)+-12.34<0?12.0/_d221-13.1!=0?(_d221*_i221/d121-+12.45==0?11.0:13.2):43.2+-13.0:-17.0)); //Проверка условий 2


	}

	public String[] getTestNames()
	{
		int sz = 25;
		String[] retVal=new String[sz];
		for (int i = 0; i < sz;i++)
			  retVal[i]="tst"+i;
		return retVal;
	}

	public double getTestValueByName(String parName)  throws ParserException
	{
		Pair<String,Double> retVal = hm.get(parName);
		if (retVal!=null)
			return retVal.second;
		throw new ParserException("Unknown symbol:" + parName);
	}

	public String getFormulaByName(String parName)  throws ParserException
	{
		Pair<String,Double> retVal = hm.get(parName);
		if (retVal!=null)
			return retVal.first;
		throw new ParserException("Unknown symbol:" + parName);
	}
}
