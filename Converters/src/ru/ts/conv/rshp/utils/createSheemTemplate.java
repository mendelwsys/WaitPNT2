package ru.ts.conv.rshp.utils;

import com.linuxense.javadbf.DBFReader;
import com.linuxense.javadbf.DBFField;

import java.io.*;
import java.util.*;

import ru.ts.conv.rshp.SHPImporter;
import ru.ts.conv.rshp.ReadShp;

/**
 * Created by IntelliJ IDEA.
 * User: vladm
 * Date: 03.07.2011
 * Time: 12:42:26
 * Генератор таблицы схемы стилей для рисования объектов сгенерированных
 */
public class createSheemTemplate
{


	protected static final String defcharset = "WINDOWS-1251";
	static final String headerTail =
	"TYPEOBJ,AsName,HiRange,LowRange,Visible,COLOR_LINE,COLOR_FILL,LINE_THICKNESS,LAYERNAME,TEXT_MODE,FONT_NAME,FONT_COLOR,FONT_STYLE,STROKE,TEXTURE,FIMAGE,IMG_CENTRALPOINT";
	static final String templatetailvals ="$0,,-1,-1,true,ff000000,ff000000,1,$1,,,,,,,$2,,";

	public static void main(String[] args) throws Exception
	{


		String indir="G:\\BACK_UP\\$D\\MAPDIR\\TVER\\SHPTVER\\";//Входные параметры - каталог где лежат шейпы
		String charset = defcharset;//Кодировка для чтения bdf
		String attrsCriterias="GRMN_TYPE";//
		String out="G:\\BACK_UP\\$D\\MAPDIR\\TVER\\n_ncommon.shm";


		Map<String,String> regexp2Attrname = new HashMap<String,String>();

		regexp2Attrname.put("\\$1","GRMN_TYPE");
		regexp2Attrname.put("\\$2","");


//		String[] headersTail=headerTail.split(",|;");
//		String [] templateval = templatetailvals.split(",|;");

		String[] attrsnameCriteria = attrsCriterias.split(",|;");




		StringBuffer outputbuffer = new StringBuffer();
		//Set header for table
		for (String attr : attrsnameCriteria)
			outputbuffer.append(attr).append(",");
		outputbuffer.append(headerTail).append("\n");


		File infolder = new File(indir);
		String[] fls = infolder.list(new FilenameFilter()
		{

			public boolean accept(File dir, String name)
			{
				//TODO Set suffix by input params
				return name.endsWith(".shp");
			}
		});

		Map<Integer, String> shp2geotype=new HashMap<Integer, String>();
		SHPImporter.setShape2Geotype(shp2geotype);

		Map<String,StringBuffer> typeobj2tablepart= new HashMap<String,StringBuffer>();

		Set<String> uniqcriterion= new HashSet<String>();

		for (String mainfile : fls)
		{
			String dbffile=mainfile.substring(0, mainfile.indexOf(".shp"))+".dbf";
			ReadShp.HeaderOfMainFile rh = (new ReadShp()).new HeaderOfMainFile();

			DataInputStream dis = null;
			InputStream dbfis = null;
			try
			{
				dis = new DataInputStream(new FileInputStream(indir + "/"+mainfile));
				rh.loadFromStream(dis);

				//Читаем аттрибуты и заполняем пулл пустыми строками?
				dbfis = new FileInputStream(indir+"/"+dbffile); // take dbf file as program argument
				DBFReader reader = new DBFReader(dbfis);
				reader.setCharactersetName(charset);

				int numberOfFields = reader.getFieldCount();
				Map<String,Integer> attrname2ix=new HashMap<String,Integer>();
				Collection<String> attrs=new HashSet<String>(Arrays.asList(attrsnameCriteria));
				attrs.addAll(regexp2Attrname.values());
				for (int ix = 0; ix < numberOfFields; ix++)
				{
					DBFField field = reader.getField(ix);
					String fname = field.getName();
					if (attrs.contains(fname))
						attrname2ix.put(fname,ix);
				}

				String objtypename = shp2geotype.get(rh.shptype);
				StringBuffer bufstr = typeobj2tablepart.get(objtypename);
				if (bufstr==null)
					typeobj2tablepart.put(objtypename,bufstr=new StringBuffer());

				int reccount = reader.getRecordCount();
				for (int ki=0;ki< reccount;ki++)
				{
					if (ki%1113 == 0)
						System.out.println("anailze records = " + ki+" of "+reccount+" in file:"+dbffile);

					Object[] rawObjects;
					if ((rawObjects = reader.nextRecord()) != null)
					{


						StringBuffer criteraibuffer=new StringBuffer();
						for (String attrname : attrsnameCriteria)
						{
							Integer ix=attrname2ix.get(attrname);
							if (ix==null)
								continue;
							String val = "";
							if (rawObjects[ix] != null)
								 val = String.valueOf(rawObjects[ix]).trim();
							criteraibuffer.append(val).append(",");
						}


						String criterion = criteraibuffer.toString()+","+objtypename;
						if (uniqcriterion.contains(criterion))
							continue; //if key already in key set, analyse next record

						uniqcriterion.add(criterion);
						bufstr.append(criteraibuffer.toString());
						String tailvals= templatetailvals;
						tailvals=tailvals.replaceAll("\\$0",objtypename);
						for (String key : regexp2Attrname.keySet())
						{
							String attrname=regexp2Attrname.get(key);
							Integer ix=attrname2ix.get(attrname);
							String val = "";
							if (ix!=null && rawObjects[ix] != null)
								val = String.valueOf(rawObjects[ix]).trim();
							tailvals=tailvals.replaceAll(key,val);
						}
						bufstr.append(tailvals).append("\n");
					}

				}
			}
			finally
			{
				try
				{
					if (dbfis!=null)
						dbfis.close();
				}
				catch (IOException e)
				{//
				}
				try
				{
					if (dis!=null)
						dis.close();
				}
				catch (IOException e)
				{//
				}
			}
		}

		String[] typesobject = SHPImporter.classictypeorder;
		for (String typeobject : typesobject)
		{
			StringBuffer sbuffer=typeobj2tablepart.get(typeobject);
			if (sbuffer!=null)
				outputbuffer.append(sbuffer).append("\n");
		}
		//Write out it to disk
		FileWriter fos = new FileWriter(out);
		fos.write(outputbuffer.toString());
		fos.close();
	}
}
