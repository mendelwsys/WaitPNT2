package ru.ts.conv.rmp_t;

import ru.ts.conv.rmp.SheemLoader;

import java.util.*;
import java.io.PrintWriter;
import java.io.File;

/**
 * Вспомогательный	класс для генерации схеы преобразования
 */

public class SheemGenerator
{
	private static final String TYPE = "[POLYGON]";
	private static final String TYPE1 = "rgn80";

	public static void main(String[] args) throws Exception
	{
		List<String> headerssheem = new LinkedList<String>();
		String dlshm=",";
		Map<String, List<String>> tblsheem = SheemLoader.loadScheem(dlshm, headerssheem, "D:\\MAPDIR\\MP\\ncommon.shm");

		List<String> rgns=tblsheem.get("RGN");
		List<String> types=tblsheem.get("NTYPE");
		List<String> colorl=(tblsheem.get("COLOR_LINE"));
		List<String> colorf=(tblsheem.get("COLOR_FILL"));
		List<String> lineth=tblsheem.get("LINE_THICKNESS");
		List<String> lname=tblsheem.get("LAYERNAME");


		Map<Integer,List<Integer>> type2index=new HashMap<Integer,List<Integer>>();
		for (int i = 0; i < types.size(); i++)
		{
			String type = types.get(i);
			String rgn = rgns.get(i);
			if (rgn.equalsIgnoreCase(TYPE1) || rgn.equalsIgnoreCase(TYPE))
			{
				int itype = Integer.parseInt(type.substring(2), 16);
				List<Integer> ll=type2index.get(itype);
				if (ll==null)
					type2index.put(itype,ll=new LinkedList<Integer>());
				ll.add(i);
			}
		}

		List<String> hrgb = new LinkedList<String>();
		Map<String, List<String>> tblreplace = SheemLoader.loadScheem(dlshm, hrgb, "D:\\MAPDIR\\MP\\CONVERTSUPPORT\\poly.txt");

		List<String> rptypes=tblreplace.get("NTYPE");
		List<String> rR=tblreplace.get("R");
		List<String> rG=tblreplace.get("G");
		List<String> rB=tblreplace.get("B");
		List<String> lrName=tblreplace.get("LAYERNAME");


		Map<String,Integer> addhs=new HashMap<String,Integer>();

		for (int i = 0; i < rptypes.size(); i++)
		{
			String rptype = rptypes.get(i);
			int type = Integer.parseInt(rptype,16);
			List<Integer> lshmindex = type2index.get(type);
			if (lshmindex!=null)
			{
				for (Integer shmindex : lshmindex)
				{
					int irR=Integer.parseInt(rR.get(i));
					int irG=Integer.parseInt(rG.get(i));
					int irB=Integer.parseInt(rB.get(i));

					int color= (irR<<16)|(irG<<8)|irB;
					color=color|0xff000000;

					colorf.remove(shmindex.intValue());
					colorl.remove(shmindex.intValue());


					colorf.add(shmindex, Integer.toHexString(color));
					colorl.add(shmindex, Integer.toHexString(color));

				}
			}
			else
				addhs.put("0x"+rptype,i);
		}

		for (String type : addhs.keySet())
		{
			rgns.add(TYPE);
			types.add(type);

			int ix=addhs.get(type);

			int irR=Integer.parseInt(rR.get(ix));
			int irG=Integer.parseInt(rG.get(ix));
			int irB=Integer.parseInt(rB.get(ix));

			int color= (irR<<16)|(irG<<8)|irB;
			color=color|0xff000000;

			colorl.add(Integer.toHexString(color));
			colorf.add(Integer.toHexString(color));
			lineth.add("1");
			lname.add(lrName.get(ix));
		}

		PrintWriter pr=new PrintWriter(new File("D:\\MAPDIR\\MP\\n_ncommon.shm"));
		pr.write("RGN,NTYPE,COLOR_LINE,COLOR_FILL,LINE_THICKNESS,LAYERNAME\n");
		for (int i = 0; i < rgns.size(); i++)
			pr.write(rgns.get(i)+dlshm+types.get(i)+dlshm+colorl.get(i)+dlshm+colorf.get(i)+dlshm+lineth.get(i)+dlshm+lname.get(i)+"\n");
		pr.flush();
		pr.close();
		
//		System.out.println("addhs.size() = " + addhs.size());
	}
}
