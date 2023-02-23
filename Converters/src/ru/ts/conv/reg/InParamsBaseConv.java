package ru.ts.conv.reg;

import ru.ts.utils.data.InParams;

public class InParamsBaseConv extends InParams
{

	// имена используемых параметров (префиксы в командной строке)
	public static final String optarr[] =
			{
				"-in",
				"-pdir",
				"-projname",
				"-desc",
				"-pname",

				"-metaname",
				"-encoding",
				"-compname",

				"-textrule",
				"-drawrule",

				"-pimg",

				"-transname",
				"-trparsrc",//Список доп. параметров преобразовании координатной проекции - вход  (передается програмно через список)
				"-trpardst",//Список доп. параметров преобразовании координатной проекции -выход  (передается програмно через список)
				"-axis"//Генерировать карту относительно оси дороги

			};
	// значения параметров по умолчанию
	public static final String defarr[] =
	{
			"", //Имя входного файла
			"",
			"",
			"",//Обязательный входящий файл параметр apppart2.xml
			"",
			"",
			"WINDOWS-1251", //Система кодирования входного файла по умолчанию
			"comp0", //Имя Компилятора который будет использован по умолчанию
	};
	public static final String comments[] =
	{
			"input access file ",
			"Project folder",
			"Project Name",
			"Convertor descriptor, xml file tells how to convert input file to ToyGis project",
			"Project Name",
			"",
			"",
			"",
			"Rule of text drawing",
			"Rule of object drawing",
			"images full path for project"
	};
	public static final int O_in=0;//Входной файл аксеса
	public static final int O_pdir=1;//Каталог файлов проекта, куда сохраняется проект
	public static final int O_pname=2;//Имя проекта
	public static final int O_desc=3;//Имя файла описания проекта
	public static final int O_projname=4;//Имя проекта
	public static final int O_metaname=5;//Имя описателя метаинформацииы
	public static final int O_encoding=6;//Система кодирования
	public static final int O_compiler =7;//Имя преобразователя
	public static final int O_textrule =8;//Правило отображения текста
	public static final int O_drawrule =9;//Правило отображения объектов
	public static final int O_pimage = 10;//Папка где будут лежат изображения проекта
	public static final int O_transname=11;//Имя преобразователя координат
	public static final int O_trparsrc = 12;//Список доп. параметров преобразовании координатной проекции - вход  (передается програмно через список)
	public static final int O_trpardst = 13;//Список доп. параметров преобразовании координатной проекции - выход  (передается програмно через список)
	public static final int O_axis = 14;
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