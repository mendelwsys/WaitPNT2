package ru.ts.conv.rmp;

import ru.ts.utils.data.InParams;

public class InParamsBaseConv extends InParams
{

	// имена используемых параметров (префиксы в командной строке)
	public static final String optarr[] =
			{
				"-in", "-cshm",    "-dlshm","-sсtbl", "-dlsc",
				"-pdir","-projname","-desc","-pname","-transname",
				"-metaname","-encoding","-compname","-pperm",
					"-textrule",
					"-drawrule",
					"-trparsrc",//Список доп. параметров преобразовании координатной проекции - вход  (передается програмно через список)
					"-trpardst",//Список доп. параметров преобразовании координатной проекции -выход  (передается програмно через список)
					"-pimg",
					"-asname"//Использовать данный аттрибут как имя объектов (аттрибут по умолчанию по которому производится поиск объектов)
			};
	// значения параметров по умолчанию
	public static final String defarr[] =
	{
			"", //Имя входного файла
			"",
			"",
			"",
			"",
			"",
			"",
			"",//Обязательный входящий файл параметр apppart2.xml
			"",
			"",
			"",
			"WINDOWS-1251", //Система кодирования входного файла по умолчанию
			"comp0", //Имя Компилятора который будет использован по умолчанию
	};
	public static final String comments[] =
	{
			"input file in polish format (MP)",
			"color input sheem",
			"sheem delimiter",
			"scale table",
			"",
			"",
			"Project Name",
			"Convertor descriptor, xml file tells how to convert input file to ToyGis project",
			"Project Name",
			"",
			"",
			"",
			"",
			"Rule of text drawing",
			"Rule of object drawing",
			"",
			"",
			"",
			"images full path for project",
			""
	};
	public static final int O_in=0;//Входной файл
	public static final int O_cshm=1;//Входной файл описание атрибутивной схемы отображения объектов
	public static final int O_dlshm=2;//Разделитель в описании атрибутивной схемы отображения объектов
	public static final int O_sсtbl=3;//Входной файл описания таблицы объектов
	public static final int O_dlsc=4;//Разделитель в описании таблицы объектов
	public static final int O_pdir=5;//Каталог файлов проекта, куда сохраняется проект
	public static final int O_pname=6;//Имя проекта
	public static final int O_desc=7;//Имя файла описания проекта
	public static final int O_projname=8;//Имя проекта
	public static final int O_transname=9;//Имя преобразователя координат
	public static final int O_metaname=10;//Имя описателя метаинформацииы
	public static final int O_encoding=11;//Система кодирования
	public static final int O_compiler =12;//Имя преобразователя
	public static final int O_pperm =13;//Пикселей на метр
	public static final int O_textrule =14;//Правило отображения текста
	public static final int O_drawrule =15;//Правило отображения объектов
	public static final int O_trparsrc = 16;//Список доп. параметров преобразовании координатной проекции - вход  (передается програмно через список)
	public static final int O_trpardst = 17;//Список доп. параметров преобразовании координатной проекции - выход  (передается програмно через список)
	public static final int O_pimage = 18;//Папка где будут лежат изображения проекта
	public static final int O_asname = 19;//Использовать данный аттрибут как имя объектов (аттрибут по умолчанию по которому производится поиск объектов)
	public InParamsBaseConv()
	{
		super(optarr, defarr,comments);
	}

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
	
}