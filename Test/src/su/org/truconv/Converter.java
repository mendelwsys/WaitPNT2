package su.org.truconv;


import ru.ts.toykernel.storages.mem.MemEditableStorageLr;
import ru.ts.toykernel.consts.KernelConst;
import ru.ts.toykernel.geom.IEditableGisObject;
import ru.ts.toykernel.attrs.def.DefaultAttrsImpl;
import ru.ts.toykernel.attrs.def.DefAttrImpl;
import ru.ts.gisutils.algs.common.MPoint;

import java.io.*;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: 22.04.2005
 * Time: 10:56:56
 * конвертация для карт с помощью серегенного дигитайзера
 */
public class Converter
{

	public static final String optarr[] = {	"-iresf", "-isX", "-isY", "-iiX", "-iiY", "-inorm",
											"-itpf", "-oresf", "-oixf", "-otpf", "-onsf","-imnms",
											"-ortf","-oshft","-oipnt","-oipX","-oipY"
	};

	//-inormMSK\Normalizer.dmap -iresfMSK\Moscow.dmap -itpfstreettypes.txt -isX500 -isY500 -iiX15 -iiY15 -oixfMSK\res\index.res -oresfMSK\res\map.res  -otpfMSK\res\streettypes.res -onsfMSK\res\streets.res -oshftMSK\res\shifts.res
	public static final int O_if = 0;  //Файл с оцифрованными ресурсами который надо конвертить в мидлетовыский ресурс
	public static final int O_isX = 1; //Размер карты по X
	public static final int O_isY = 2; //Размер карты по Y
	public static final int O_iiX = 3; //Размер сегмента карты по X (для построения индекса)
	public static final int O_iiY = 4; //Размер сегмента карты по Y (для построения индекса)
	public static final int O_inorm = 5;//Файл нормализатора карты (вспом. файл в ресурсы не входит)
	public static final int O_itpf = 6; //Файл с именами типов графических объектов
	public static final int O_oresf = 7;//Выходной файл ресурсов
	public static final int O_oixf = 8; //Выходной файл ресурсов индекса
	public static final int O_otpf = 9; //Выходной файл ресурса типа графического объекта
	public static final int O_onsf = 10;// Выходной файл с ресурсом имен станций
	public static final int O_imnms = 11;//Входной Файл с ресурсом имен станций
	public static final int O_ortf = 12;//Файл с ресурсом времен переходов
	public static final int O_oshft = 13;//Файл с ресурсом сдвигов
	public static final int O_oipnt = 14;//Файл с ресурсом делителей сдвига начальной точки относительно центра карта
	public static final int O_oipX = 15; //Размер сегмента карты по X (для построения индекса)
	public static final int O_oipY = 16; //Размер сегмента карты по Y (для построения индекса)
	protected static final String EOF = "EOF";
	final static byte spaces[] = {' ', '\t', '\n', 0x0D};
	final static byte addLiterals[] = {'$', '.', '-'};
	static public Hashtable options = new Hashtable();
	protected  Hashtable spacesH = new Hashtable();
	protected  String regexp = "[$]";
	protected  int notline = 0;
	protected  String[] prefixes;
	boolean isendl;
	private Hashtable addLiteralsH = new Hashtable();
	public Converter()
	{
		for (int i = 0; i < spaces.length; i++)
			spacesH.put(new Integer(spaces[i]), new Integer(i));

		for (int i = 0; i < addLiterals.length; i++)
			addLiteralsH.put(new Integer(addLiterals[i]), new Integer(i));
	}

	public static void main(String[] args) throws Exception
	{
		Converter.TranslateOption(args);

		new Converter().doJobConverter(); //Конвертер
//		new ConverterImpl().doJobMConverter(); //Конвертер схемы метро

	}

	static public void TranslateOption(String arg[])
	{
		if (arg == null)
			return;
		for (int i = 0; i < arg.length; i++)
		{
			if (arg[i] != null)
			{
				for (int j = 0; j < optarr.length; j++)
					if (arg[i].startsWith(optarr[j]))
					{
						String opt = arg[i].substring(optarr[j].length());
						options.put(optarr[j], opt);
						break;
					}
			}
		}


	}

	private boolean isAllowedLiteral(byte ch)
	{

		return ('A' <= ch && ch <= 'Z') ||
				('a' <= ch && ch <= 'z') ||
				('0' <= ch && ch <= '9') ||
				('('==ch || ch==')' || ch==',' || ch=='?') ||

				((byte) 0xC0 <= ch && ch <= (byte) 0xDF) ||
				((byte) 0xE0 <= ch && ch <= (byte) 0xFF) ||
				(addLiteralsH.get(new Integer(ch))) != null;
	}

	public String getNextTokenByEndl(DataInputStream dis) throws IOException
	{
		boolean isToken = false;
		String token = "";
		byte[] btToken = new byte[100];
		int length = 0;
		String inencoding = "WINDOWS-1251";
		try
		{
			for (; ;)
			{

				byte ch = dis.readByte();
				if (spacesH.get(new Integer(ch)) != null)
				{
					if (ch == 0x0D && dis.readByte() != '\n')
						throw new IOException();

					if (isToken && (ch == 0x0D || ch == '\n'))
					{
						token += new String(btToken, 0, length, inencoding);
						return token;
					}
					if (isToken)
					{
						if (length >= 100)
						{
							token += new String(btToken, 0, length, inencoding);
							length = 0;
						}
						btToken[length] = ch;
						length++;
					}
				}
				else if (!isToken && isAllowedLiteral(ch))
				{
					isToken = true;
					btToken[0] = ch;
					length = 1;
				}
				else if (isToken && isAllowedLiteral(ch))
				{
					if (length >= 100)
					{
						token += new String(btToken, 0, length, inencoding);
						length = 0;
					}
					btToken[length] = ch;
					length++;
				}
				else
				{
					System.out.println("ch = " + new String(new byte[]{ch}, inencoding));
					throw new IOException("Error Literal");
				}
			}
		}
		catch (EOFException e)
		{
			if (isToken)
			{
				token += new String(btToken, 0, length, inencoding);
				return token;
			}
			else
				return EOF;
		}

	}

	public String getNextToken(DataInputStream dis) throws IOException
	{
		isendl=false;
		boolean isToken = false;
		String token = "";
		byte[] btToken = new byte[100];
		int length = 0;
		try
		{
			for (; ;)
			{

				byte ch = dis.readByte();
				if (spacesH.get(new Integer(ch)) != null)
				{

					if (ch == 0x0D && !(isendl=(dis.readByte() == '\n')))
						throw new IOException();


					if (isToken)
					{
						token += new String(btToken, 0, length,"WINDOWS-1251");
						return token;
					}
				}
				else if (!isToken && isAllowedLiteral(ch))
				{
					isToken = true;
					btToken[0] = ch;
					length = 1;
				}
				else if (isToken && isAllowedLiteral(ch))
				{
					if (length >= 100)
					{
						token += new String(btToken, 0, length,"WINDOWS-1251");
						length = 0;
					}
					btToken[length] = ch;
					length++;
				}
				else
					throw new IOException("Error Literal");
			}
		}
		catch (EOFException e)
		{
			if (isToken)
			{
				token += new String(btToken, 0, length,"WINDOWS-1251");
				return token;
			}
			else
				return EOF;
		}

	}

	public void doJobNormalizer(String[] args) throws IOException
	{

		String nextToken;
		FileInputStream is = null;
		DataInputStream dis = null;

		Double minX = null;
		Double minY = null;
		Double maxX = null;
		Double maxY = null;

		OutputStream os = new FileOutputStream(args[0]);
		DataOutputStream dos = new DataOutputStream(os);

		for (int j = 1; j < args.length; j++)
		{
			String infileName = args[j];
			try
			{
				File infile = new File(infileName);
				is = new FileInputStream(infile);
				dis = new DataInputStream(is);
				nextToken = getNextToken(dis);
//Считываем координаты в память
				int size = Integer.valueOf(nextToken).intValue();
				for (int i = 0; i < size; i++)
				{

					nextToken = getNextToken(dis);
					double coordinatesX = Double.valueOf(nextToken).doubleValue();
					if (minX == null || minX.doubleValue() > coordinatesX)
						minX = new Double(coordinatesX);
					if (maxX == null || maxX.doubleValue() < coordinatesX)
						maxX = new Double(coordinatesX);

					nextToken = getNextToken(dis);
					double coordinatesY = Double.valueOf(nextToken).doubleValue();
					coordinatesY *= -1;
					if (minY == null || minY.doubleValue() > coordinatesY)
						minY = new Double(coordinatesY);
					if (maxY == null || maxY.doubleValue() < coordinatesY)
						maxY = new Double(coordinatesY);
					getNextToken(dis);
				}
			}
			finally
			{
				try
				{
					if (dis != null)
						dis.close();
				}
				catch (IOException e)
				{
				}

				try
				{
					if (is != null)
						is.close();
				}
				catch (IOException e)
				{
				}
			}
		}
		try
		{
			dos.writeDouble(minX.doubleValue());
			dos.writeDouble(minY.doubleValue());
			dos.writeDouble(maxX.doubleValue());
			dos.writeDouble(maxY.doubleValue());
		}
		finally
		{
			try
			{
				if (dos != null)
					dos.close();
			}
			catch (IOException e)
			{
			}

			try
			{
				if (os != null)
					os.close();
			}
			catch (IOException e)
			{
			}
		}

	}

	public void doJobConverter() throws Exception
	{

		String nextToken = null;

		FileInputStream is = null;
		DataInputStream dis = null;


		String opttp = (String) options.get(optarr[O_itpf]);
		if (opttp != null)
		{
			File modfile = new File(opttp);
			dis = new DataInputStream(new FileInputStream(modfile));
			List<String> namemod = new LinkedList<String>();
			while (!(nextToken = getNextTokenByEndl(dis)).equals(EOF))
				namemod.add(nextToken);
			prefixes=namemod.toArray(new String[namemod.size()]);
			dis.close();
		}

		File infile = new File((String) options.get(optarr[O_if]));
		is = new FileInputStream(infile);
		dis = new DataInputStream(is);
		nextToken = getNextToken(dis);
//Считываем координаты в память
		int size = Integer.valueOf(nextToken).intValue();

		double coordinatesX[] = new double[size];
		double coordinatesY[] = new double[size];

		Double minX = null;
		Double minY = null;
		Double maxX = null;
		Double maxY = null;
//		String snorm = (String) options.get(optarr[O_inorm]);

		for (int i = 0; i < size; i++)
		{

			nextToken = getNextToken(dis);
			coordinatesX[i] = Double.valueOf(nextToken).doubleValue();

//			if (snorm == null)
			{
				if (minX == null || minX.doubleValue() > coordinatesX[i])
					minX = new Double(coordinatesX[i]);
				if (maxX == null || maxX.doubleValue() < coordinatesX[i])
					maxX = new Double(coordinatesX[i]);
			}

			nextToken = getNextToken(dis);
			coordinatesY[i] = Double.valueOf(nextToken).doubleValue();
			coordinatesY[i] *= -1;

//			if (snorm == null)
			{
				if (minY == null || minY.doubleValue() > coordinatesY[i])
					minY = new Double(coordinatesY[i]);
				if (maxY == null || maxY.doubleValue() < coordinatesY[i])
					maxY = new Double(coordinatesY[i]);
			}
			getNextToken(dis);
		}

		MemEditableStorageLr memstor = new MemEditableStorageLr("G:\\BACK_UP\\$D\\MAPDIR\\TRUCONV\\", "streets");
		DefaultAttrsImpl objatrrs = new DefaultAttrsImpl();

		int cntobj=0;
		while (!(nextToken = getNextTokenByEndl(dis)).equals(EOF))
		{
			cntobj++;
			String[] streetarr = nextToken.split(regexp);
			int dotssize = Integer.valueOf(streetarr[0]).intValue();
			LinkedList<List[]> clusters = new LinkedList<List[]>();
			for (int i = 0; i < dotssize; i++)
			{
				LinkedList<Integer> curclst = new LinkedList();
				LinkedList<Integer> width = new LinkedList();

				nextToken = getNextToken(dis);
				curclst.add(Integer.valueOf(nextToken));
				nextToken = getNextToken(dis);
				curclst.add(Integer.valueOf(nextToken));

				width.add(Integer.valueOf(getNextToken(dis)));
				clusters.add(new LinkedList[]{curclst, width});
			}
			convertToLineList(clusters);

			if (clusters.size() > 1)
				notline++;

			byte index = Integer.valueOf(streetarr[1]).byteValue();

			if (!streetarr[2].equals("zzz")) //TODO проверка объема при исключении zzz
			{

				IEditableGisObject est = memstor.createObject(KernelConst.LINESTRING);
				int segnumber=0;
				for (List[] cluster : clusters)
				{
					List<Integer> crdsix=cluster[0];
					MPoint[] segment = new MPoint[crdsix.size()];
					for (int i = 0; i < crdsix.size(); i++)
					{
						Integer ix = crdsix.get(i);
						segment[i] = new MPoint(coordinatesX[ix],coordinatesY[ix]);
					}
					est.addSegment(segnumber, segment);
					segnumber++;
				}
				//Добавить аттрибуты
				if (streetarr.length<3)
					System.out.println("streetarr = " + streetarr);
				if (index<prefixes.length && index>=0)
					objatrrs.put(KernelConst.ATTR_CURVE_NAME, new DefAttrImpl(KernelConst.ATTR_CURVE_NAME,streetarr[2]+" "+prefixes[index]));
				else
					objatrrs.put(KernelConst.ATTR_CURVE_NAME, new DefAttrImpl(KernelConst.ATTR_CURVE_NAME,streetarr[2]));
				est.setCurveAttrs(objatrrs);
			}
			if (cntobj % 1000 == 0)
				memstor.commit();
		}
		memstor.commit();

		dis.close();
		is.close();
	}

//	/**
//	 * Конвертер станций метро
//	 * @throws IOException
//	 */
//	public void doJobMStConverter() throws IOException
//	{
//		FileInputStream is = null;
//		DataInputStream dis = null;
//
//
//		File infile = new File((String) options.get(optarr[O_if]));
//		is = new FileInputStream(infile);
//		dis = new DataInputStream(is);
//
////Считываем координаты в память
//		int size = Integer.parseInt(getNextToken(dis));
//
//		Double minX = null;
//		Double minY = null;
//
////		Double maxX = null;
////		Double maxY = null;
//
//		String snorm = (String) options.get(optarr[O_inorm]);
//		File normfile = new File(snorm);
//		FileInputStream isn = new FileInputStream(normfile);
//		DataInputStream disn = new DataInputStream(isn);
//		minX = new Double(disn.readDouble());
//		minY = new Double(disn.readDouble());
////		maxX = new Double(disn.readDouble());
////		maxY = new Double(disn.readDouble());
//		disn.close();
//		isn.close();
//
//		Vector vctr= new Vector();
//		{
//			double coordinatesX[] = new double[size];
//			double coordinatesY[] = new double[size];
////			short shifts[] = new short[size];
//
//            double tmp_dbl=Double.parseDouble(getNextToken(dis));
//			for (int i = 0; i < size; i++)
//			{
//
//				coordinatesX[i] = tmp_dbl;
//				coordinatesY[i] = Double.valueOf(getNextToken(dis)).doubleValue();
//				coordinatesY[i] *= -1;
//
//				getNextToken(dis);
//
////Нормализация к нулю
////				double lengthX = maxX.doubleValue() - minX.doubleValue();
////				double lengthY = maxY.doubleValue() - minY.doubleValue();
////				double canvasX = lengthX / 10;
////				double canvasY = lengthY / 10;
//
//				double kX = 10;//lengthX / canvasX;
//				double kY = 10;//lengthY / canvasY;
//
//				coordinatesX[i] -= minX.doubleValue();
//				coordinatesX[i] /= kX;
//				coordinatesY[i] -= minY.doubleValue();
//				coordinatesY[i] /= kY;
//
//
//				String nextToken = getNextToken(dis);
//				String nameStation="";
//				while(true)
//				try
//				{
//					if (nextToken.equals(EOF))
//						break;
//					tmp_dbl = Double.parseDouble(nextToken);
//					break;
//				}
//				catch (NumberFormatException e)
//				{
//					if (nameStation.length()>0)
//						nameStation+=" "+nextToken;
//					else
//						nameStation=nextToken;
//
//					nextToken=getNextToken(dis);
//				}
//
//
//				MStattionImplConv tmpSt=new MStattionImplConv(nameStation,(short )coordinatesX[i],(short )coordinatesY[i]);
//				int j=0;
//				for (; j < vctr.size(); j++)
//				{
//					MStattionImplConv iMapObject = ((MStattionImplConv)vctr.get(j));
//					if(iMapObject.getObjectName(null).equals(tmpSt.getObjectName(null)))
//					{
//						iMapObject.addXY((short)coordinatesX[i],(short)coordinatesY[i]);
//						break;
//					}
//				}
//				if (j==vctr.size())
//					vctr.add(tmpSt);
//			}
//		}
//
//		Collections.sort(vctr, ConvStreet.getComparator());
//
//
//
//		File nmlfile = new File((String) options.get(optarr[O_onsf]));
//		nmlfile.createNewFile();
//		FileOutputStream nos = new FileOutputStream(nmlfile);
//		DataOutputStream ndos = new DataOutputStream(nos);
//
//		File oufile = new File((String) options.get(optarr[O_oresf]));
//		oufile.createNewFile();
//		FileOutputStream os = new FileOutputStream(oufile);
//		DataOutputStream dos = new DataOutputStream(os);
//
//
////Записываем файл с позицией станции метро и с их именами
//		dos.writeInt(0);dos.writeInt(0);//Для того что бы соблюсти формат
//		for (int i = 0; i < vctr.size(); i++)
//		{
//			MStattionImplConv imapobj = ((MStattionImplConv) vctr.get(i));
//			imapobj.setShiftName((short) ndos.size());
//			imapobj.sentToOutputStream(dos);
//
//			byte[] codebytes=Bukovki2.inCode(imapobj.getObjectName(null));
//			ndos.writeByte(codebytes.length);
//			ndos.write(codebytes);
//
//			System.out.println("objectName: "+imapobj.getObjectName(null));
//		}
//
//
//		os.flush();
//		dos.close();
//		os.close();
//
//		nos.flush();
//		nos.close();
//		ndos.close();
//
//		System.out.println("Stations:" + vctr.size() + " NOT IN LINE Streets:" + notline);
//
//
////TODO Посмотреть как проверить линии метро
//		System.out.print("Reading MMap....");
//		is = new FileInputStream(oufile);
//		Structures.readStMetro(new MStattionImpl(), is, null);
//		is.close();
//		System.out.println("Ok");
//
//	}

	/**
	 * Сохранить делители сдвигов карты относительна центра
	 * @throws IOException
	 */
	public void savePntDelimeters() throws IOException
	{
		DataOutputStream dos = new DataOutputStream(new FileOutputStream((String) options.get(optarr[O_oipnt])));
		int divx=Integer.parseInt((String) options.get(optarr[O_oipX]));
		int divy=Integer.parseInt((String) options.get(optarr[O_oipY]));
		dos.writeInt(divx);
		dos.writeInt(divy);
		dos.close();
	}


//	/**
//	 * Конвертер для схемы метро
//	 * @throws IOException
//	 */
//	public void doJobMConverter() throws IOException
//	{
//
//		FileInputStream is = null;
//		DataInputStream dis = null;
//
////Создаем пул имен со смещением в файле имен
//		Vector nmVector=new Vector();
//		File infile = new File((String) options.get(optarr[O_imnms]));
//		is = new FileInputStream(infile);
//		{
//			try
//			{
//				int tmpln=0;
//				do
//				{
//					byte[] len={0};
//					int ln=is.read(len);
//					if (ln==-1)
//						break;
//					byte[] name = new byte[len[0]];
//					ln+=is.read(name);
//					nmVector.addElement(new Object[]{Bukovki2.deCode(name,0,name.length),new Short((short) tmpln)});
//					tmpln+=ln;
//					System.out.println("ln:"+tmpln);
//				} while (true);
//			}
//			catch (IOException e)
//			{
//			}
//		}
//		is.close();
//
//
//		infile = new File((String) options.get(optarr[O_if]));
//		is = new FileInputStream(infile);
//		dis = new DataInputStream(is);
////Считываем координаты в память
//		int size = Integer.parseInt(getNextToken(dis));
//
//		double coordinatesX[] = new double[size];
//		double coordinatesY[] = new double[size];
//		short shifts[] = new short[size];
//
//		Double minX = null;
//		Double minY = null;
//		Double maxX = null;
//		Double maxY = null;
//		String snorm = (String) options.get(optarr[O_inorm]);
//		for (int i = 0; i < size; i++)
//			shifts[i]=-1;
//
//
//		double tmp_value=Double.parseDouble(getNextToken(dis));
//		for (int i = 0; i < size; i++)
//		{
//
//			coordinatesX[i] =tmp_value;
//
//
//			if (snorm == null)
//			{
//				if (minX == null || minX.doubleValue() > coordinatesX[i])
//					minX = new Double(coordinatesX[i]);
//				if (maxX == null || maxX.doubleValue() < coordinatesX[i])
//					maxX = new Double(coordinatesX[i]);
//			}
//
//			coordinatesY[i] = Double.parseDouble(getNextToken(dis));
//			coordinatesY[i] *= -1;
//
//
//
//			String nameStation="";
//			String deb_token="";
//
//			getNextToken(dis);
//
//			while(true)
//			try
//			{
//
//				if (i<size-1)
//				{
//					deb_token=getNextToken(dis);
//					tmp_value = Double.parseDouble(deb_token);
//				}
//				else if (i==size-1)
//				{
//				 	deb_token=getNextToken(dis);
//					if (!isendl)
//						 throw new NumberFormatException();//Добавим к имени еще одно слово
//
//					if (nameStation.length()>0)
//						nameStation+=" "+deb_token;
//					else
//						nameStation=deb_token;
//
//				}
//
//
//				if (nameStation.length()>0)
//				{
//					//Поиск имени в пуле имен
//					int j = 0;
//					for (; j < nmVector.size(); j++)
//					{
//						Object[] objects = (Object[]) nmVector.get(j);
//						if (((String)objects[0]).equalsIgnoreCase(nameStation))
//						{
//							shifts[i]=((Short)objects[1]).shortValue();
//							break;
//						}
//					}
//					if (j== nmVector.size())
//						System.out.println("Error of finding name: "+nameStation);
//				}
//				break;
//			}
//			catch (NumberFormatException e)
//			{
//				if (nameStation.length()>0)
//					nameStation+=" "+deb_token;
//				else
//				    nameStation=deb_token;
//			}
//
//
//			System.out.println("name["+i+"]: "+nameStation);
//			System.out.println("coordinatesX["+i+"]: " +coordinatesX[i]);
//			System.out.println("coordinatesY["+i+"]: "+coordinatesY[i]);
//
//			if (snorm == null)
//			{
//				if (minY == null || minY.doubleValue() > coordinatesY[i])
//					minY = new Double(coordinatesY[i]);
//				if (maxY == null || maxY.doubleValue() < coordinatesY[i])
//					maxY = new Double(coordinatesY[i]);
//			}
//		}
//
//		if (snorm != null)
//		{
//			File normfile = new File(snorm);
//			FileInputStream isn = new FileInputStream(normfile);
//			DataInputStream disn = new DataInputStream(isn);
//			minX = new Double(disn.readDouble());
//			minY = new Double(disn.readDouble());
//			maxX = new Double(disn.readDouble());
//			maxY = new Double(disn.readDouble());
//			disn.close();
//			isn.close();
//		}
//
////Нормализация координат к нулю
//		double lengthX = maxX.doubleValue() - minX.doubleValue();
//		double lengthY = maxY.doubleValue() - minY.doubleValue();
//
//		double kX = 4.4;//lengthX / canvasX;
//		double kY = 3.8;//lengthY / canvasY;
//
//		double canvasX = lengthX / kX;
//		double canvasY = lengthY / kY;
//
//
//		for (int i = 0; i < size; i++)
//		{
//			coordinatesX[i] -= minX.doubleValue();
//			coordinatesX[i] /= kX;
//			coordinatesY[i] -= minY.doubleValue();
//			coordinatesY[i] /= kY;
//		}
//
//		LinkedList veclayer = new LinkedList();
//
//
//		String nextToken = null;
//
//		while (!(nextToken = getNextTokenByEndl(dis)).equals(EOF))
//		{
//			String[] streetarr = nextToken.split(regexp);
//			int dotssize = Integer.valueOf(streetarr[0]).intValue();
//			LinkedList clusters = new LinkedList();
//			for (int i = 0; i < dotssize; i++)
//			{
//				LinkedList curclst = new LinkedList();
//				LinkedList width = new LinkedList();
//
//				nextToken = getNextToken(dis);
//				curclst.add(Integer.valueOf(nextToken));
//				nextToken = getNextToken(dis);
//				curclst.add(Integer.valueOf(nextToken));
//				width.add(Integer.valueOf(getNextToken(dis)));
//				clusters.add(new LinkedList[]{curclst, width});
//			}
//
//			convertToLineList(clusters);
//
//			if (clusters.size() > 1)
//				System.out.println("Clusters more the one");
//
//			short[][] loc_shifts=new short[clusters.size()][];
//			for (int i = 0; i < clusters.size(); i++)
//			{
//				LinkedList[] linkedLists = (LinkedList[]) clusters.get(i);
//				loc_shifts[i]=new short[linkedLists[0].size()];
//				for (int j = 0; j < linkedLists[0].size(); j++)
//				{
//					Integer itg=(Integer) linkedLists[0].get(j);
//					loc_shifts[i][j]=shifts[itg.intValue()-1];
//				}
//			}
//
//			byte index = Integer.valueOf(streetarr[1]).byteValue();
//			if (loc_shifts==null)
//				System.out.println("Error in program or data2");
//
//			ConvMLine cnvLine = new ConvMLine(streetarr[2],new String[]{streetarr[3],streetarr[4],streetarr[5]},clusters, coordinatesX, coordinatesY, index,loc_shifts);
//			veclayer.addLast(cnvLine);
//		}
//
//		dis.close();
//		is.close();
//
//		System.out.println("PointsInClusters:" + ConvStreet.countPoints);
//
//		Collections.sort(veclayer, ConvStreet.getComparator());
//
////Записать имена линий метро в файл
//		File nmlfile = new File((String) options.get(optarr[O_onsf]));
//		nmlfile.createNewFile();
//		FileOutputStream os = new FileOutputStream(nmlfile);
//		DataOutputStream dos = new DataOutputStream(os);
//
//		short shiftname = (short) dos.size();
//		for (int i = 0; i < veclayer.size(); i++)
//		{
//			ConvStreet street = (ConvStreet) veclayer.get(i);
//			street.setShiftName(shiftname);
//			byte[] codebytes = Bukovki2.inCode(street.nameStreet);
//			System.out.println(street.nameStreet + " " + i);
//			dos.writeByte(codebytes.length);
//			dos.write(codebytes);
//			shiftname = (short) dos.size();
//		}
//		dos.close();
//		os.close();
//
//		File oufile = new File((String) options.get(optarr[O_oresf]));
//		oufile.createNewFile();
//		os = new FileOutputStream(oufile);
//		dos = new DataOutputStream(os);
//		dos.writeInt((int) canvasX);
//		dos.writeInt((int) canvasY);
//		for (int i = 0; i < veclayer.size(); i++)
//		{
//			StreetImpl streetimpl = ((StreetImpl) veclayer.get(i));
//			System.out.println(((int) streetimpl.shiftname) & 0x0FFFF);
//			streetimpl.sentToOutputStream(dos);
//		}
//		os.flush();
//		dos.close();
//		os.close();
//		System.out.println("Metro Lines:" + veclayer.size() + " NOT IN LINE Streets:" + notline);
//
////TODO Посмотреть как проверить метро
//		System.out.print("Reading MMap....");
//		dis = new DataInputStream(new FileInputStream(oufile));
//		int mapWidth = dis.readInt();
//		int mapHeigth = dis.readInt();
//		Vector vector = Structures.readMapObj(new MLineImpl(), dis, null);
//		dis.close();
//		System.out.println("Ok");
//
//	}

	private boolean createCluster(LinkedList[] clst1, LinkedList[] clst2)
	{
		Object fstp1 = clst1[0].getFirst();
		Object lst1 = clst1[0].getLast();

		Object fstp2 = clst2[0].getFirst();
		Object lst2 = clst2[0].getLast();

		if (fstp1.equals(fstp2))
		{
			clst1[0].removeFirst();
			while (clst2[0].size() > 0)
				clst1[0].addFirst(clst2[0].removeFirst());
			while (clst2[1].size() > 0)
				clst1[1].addFirst(clst2[1].removeFirst());

		}
		else if (fstp1.equals(lst2))
		{
			clst1[0].removeFirst();
			clst1[0].addAll(0, clst2[0]);
			clst1[1].addAll(0, clst2[1]);
		}
		else if (lst1.equals(fstp2))
		{
			clst1[0].removeLast();
			clst1[0].addAll(clst2[0]);
			clst1[1].addAll(clst2[1]);
		}
		else if (lst1.equals(lst2))
		{
			clst1[0].removeLast();
			while (clst2[0].size() > 0)
				clst1[0].addLast(clst2[0].removeLast());
			while (clst2[1].size() > 0)
				clst1[1].addLast(clst2[1].removeLast());
		}
		else
			return false;
		return true;
	}

	protected  boolean convertToLineList(LinkedList<List[]> clusters)
	{

		LinkedList<List[]> tmpclusters = new LinkedList<List[]>();

		tmpclusters.addFirst(clusters.removeFirst());
		//Пока не останется ни одного кластера или их кол-во перестанет уменьшаться
		int oldsize = clusters.size();
		int currentsize = oldsize;
		int iteration = 0;


		while (currentsize > 0)
		{
			LinkedList[] curcl1 = (LinkedList[]) tmpclusters.getFirst();
			LinkedList[] curcl2 = (LinkedList[]) clusters.removeFirst();

			if (!createCluster(curcl1, curcl2))
				clusters.addLast(curcl2);

			currentsize = clusters.size();
			if (iteration == oldsize)
			{
				if (currentsize == oldsize)
				{
					tmpclusters.addFirst(clusters.removeFirst());
					currentsize--;
				}
				oldsize = currentsize;
				iteration = 0;
			}
			iteration++;
		}
		clusters.addAll(tmpclusters);
		return clusters.size() > 1;
	}

}
