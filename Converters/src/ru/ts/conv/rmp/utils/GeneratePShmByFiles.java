package ru.ts.conv.rmp.utils;

import ru.ts.utils.data.InParams;
import ru.ts.utils.RawImageOperations;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.awt.image.BufferedImage;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: vladm
 * Date: 04.06.2011
 * Time: 7:14:22
 * Генерирует записи для файла схемы n_ncommon.shm по именам файла
 * имена файла имют вид тип_код_название (p_660a_Forest),
 */
public class GeneratePShmByFiles
{

	// имена используемых параметров (префиксы в командной строке)
	public static final String optarr[] =
			{
					"-di",
					"-fo",
			};
	// значения параметров по умолчанию
	public static final String defarr[] =
			{
					"", //Имя входного файла
					""
			};
	public static final String comments[] =
			{
					"input folder",
					"out file",
			};
	public static final int O_di = 0;//Входной каталог
	public static final int O_fo = 1;//Выходной файл
	final public static String typevar = "$type";
	final public static String codevar = "$code";

//	public static String headers = "RGN,NTYPE,COLOR_LINE,COLOR_FILL,LINE_THICKNESS,LAYERNAME,TEXT_MODE,FONT_NAME,FONT_COLOR,FONT_STYLE,STROKE,TEXTURE,FIMAGE";
	final public static String namelrvar = "$namelr";
	final public static String pfname = "$pfname";
	final public static String template = typevar + ",0x" + codevar + ",0,0,1," + namelrvar + ",,,,,,," + pfname + ",";

	static public String getOName(int optIdx)
	{
		try
		{
			return optarr[optIdx];
		}
		catch (Exception e)
		{//
		}
		return null;
	}

	public static void main(String[] args) throws Exception
	{
		InParams inParams = new InParamsGen();
		inParams.translateOptions(args);
		String infolder = inParams.get(O_di);
		String fout = inParams.get(O_fo);

		String ffiles = infolder + "\\XXXXX\\";
		File[] flsr = new File(ffiles).listFiles();

		for (File file : flsr)
		{
			String name=file.getName();
			name=name.replace(".png",".jpg");
			final File file1 = new File(infolder + File.separator + name);
			if (file1.isFile())
			{
				file1.delete();
			}
		}



		FileWriter fos=null;
		try
		{
			fos = new FileWriter(fout);

			File[] fls = new File(infolder).listFiles();
			for (File fl : fls)
			{
				String fname = fl.getName();

				int ixtype = fname.indexOf("_");
				if (ixtype <= 0)
					continue;
				String type = fname.substring(0, ixtype);
				if (type.equals("p"))
				{
					String currentstring = template;
					currentstring = currentstring.replace(typevar, "[POI]");

					int ixcode = fname.indexOf("_", ixtype + 1);
					if (ixcode <= 0)
						continue;
					String code = fname.substring(ixtype + 1, ixcode);
					try
					{
						Long.parseLong(code, 16);
					}
					catch (NumberFormatException e)
					{
						continue;
					}
					int ixnmlr = fname.indexOf(".", ixcode + 1);
					if (ixnmlr <= 0)
						continue;
					String namelr = fname.substring(ixcode + 1, ixnmlr);

					currentstring = currentstring.replace(codevar, code);

					currentstring = currentstring.replace(namelrvar, namelr);

					currentstring = currentstring.replace(pfname, fname);
					{
						try
						{
							BufferedImage img= ImageIO.read(new File(ffiles + File.separator + fname));
							int[][] data = RawImageOperations.getImgByRaster(img.getData());
							Point pnt = RawImageOperations.getMidleByImage(data);
							currentstring+=pnt.x+";"+pnt.y+",";
						}
						catch (IOException e)
						{
							currentstring +=",";
							e.printStackTrace();
						}
					}
					fos.write(currentstring+"\n");
				}
			}
		}
		finally
		{
			if (fos!=null)
				fos.close();
		}



	}

	public static class InParamsGen extends InParams
	{

		public InParamsGen()
		{
			super(optarr, defarr, comments);
		}
	}
}
