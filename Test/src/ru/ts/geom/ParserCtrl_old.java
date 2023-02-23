package ru.ts.geom;

import ru.ts.utils.data.SimpleAutomaton;


/**
 * Created by IntelliJ IDEA.
 * User: vladm
 * Date: 10.11.2006
 * Time: 17:40:02
 * To change this template use File | Settings | File Templates.
 */
public class ParserCtrl_old
		extends SimpleAutomaton
{
	public static final String  S_INIT = "INIT"; //- Начальное состояние
	public static final String  S_NAME_WT = "NAME_WT";
	public static final String  S_NAME_DEF = "NAME_DEF";
	public static final String  S_DESCRIPT_WT = "DESCRIPT_WT";
	public static final String  S_DESCRIPT_DEF = "DESCRIPT_DEF";
	public static final String  S_COORDINATE_WT = "COORDINATE_WT";
	public static final String  S_COORDINATE_DEF = "COORDINATE_DEF"; //END OF STATE


	public static final String  S_ERROR = "ERROR";

//Действия
	public static final String T0 = "NAME_OP"; //Тег имени открыт
	public static final String T1 = "NAME_CL"; //Тег имени закрыт

	public static final String T2 = "DESCRIPTION_OP"; //Тег имени открыт
	public static final String T3 = "DESCRIPTION_CL"; //Тег имени закрыт

	public static final String T4 = "COORDINATES_OP"; //Тег имени открыт
	public static final String T5 = "COORDINATES_CL"; //Тег имени закрыт


	public static final String T6 = "PLACEMARK_OP"; //Инициализировать открытие
	public static final String T7 = "IGNORE_TAG";//ИГНОРИРОВАТЬ ТЕГ

	static  final String[] states = {S_INIT,S_NAME_WT,S_NAME_DEF,S_DESCRIPT_WT,S_DESCRIPT_DEF,S_COORDINATE_WT,S_COORDINATE_DEF,S_ERROR};
	static  final String[] actions = {T0,T1,T2, T3, T4,T5,T6,T7};
//Таблица переходов
	static  final String[][] transmitTable={
//			{S_INIT,         S_NAME_WT,      S_NAME_DEF,     S_DESCRIPT_WT,   S_DESCRIPT_DEF,  S_COORDINATE_WT,  S_COORDINATE_DEF, S_ERROR};
			{S_NAME_WT,      S_ERROR,        S_ERROR,        S_ERROR,         S_ERROR,           S_ERROR,          S_ERROR,         S_ERROR}, //T0
			{S_ERROR,        S_NAME_DEF,     S_ERROR,        S_ERROR,         S_ERROR,           S_ERROR,          S_ERROR,         S_ERROR}, //T1
			{S_DESCRIPT_WT,  S_ERROR,        S_DESCRIPT_WT,  S_ERROR,         S_ERROR,           S_ERROR,          S_ERROR,         S_ERROR}, //T2
			{S_DESCRIPT_DEF, S_ERROR,        S_ERROR,        S_DESCRIPT_DEF,  S_ERROR,           S_ERROR,          S_ERROR,         S_ERROR}, //T3
			{S_COORDINATE_WT,S_ERROR,        S_COORDINATE_WT,S_ERROR,         S_COORDINATE_WT,   S_ERROR,          S_ERROR,         S_ERROR}, //T4
			{S_ERROR,        S_ERROR,        S_ERROR,        S_ERROR,         S_ERROR,           S_COORDINATE_DEF, S_ERROR,         S_ERROR}, //T5
			{S_INIT,         S_INIT,         S_INIT,         S_INIT,          S_INIT,            S_INIT,			S_INIT,          S_INIT},  //T6
			{S_INIT,         S_NAME_WT,      S_NAME_DEF,     S_DESCRIPT_WT,   S_DESCRIPT_DEF,    S_COORDINATE_WT,  S_COORDINATE_DEF,S_ERROR}};//T7
	public String currentLayerId;
	public String contents;
	public String nameobject;
	private String currentState= S_COORDINATE_DEF;


	public ParserCtrl_old()
	{
		super (states, actions, transmitTable);
	}

	public String getCurrentState()
	{
		return currentState;
	}

   	public void resetState()
	   {
		   currentState= S_INIT;
	   }

	public void performActionByTag(String tag, boolean isOpen)
	{
		String cmd=tag+"_"+((isOpen)?"OP":"CL");
		for (int i = 0; i < actions.length; i++)
		{
			if (actions[i].equalsIgnoreCase(cmd))
			{
				currentState=this.getNextState(currentState,actions[i]);
				if (actions[i].equals(T6))
				{
					currentLayerId="";
					contents="";
					nameobject="";
				}

				return;
			}
		}
		currentState=this.getNextState(currentState,T7);
	}

	public void setString(String str)
	{
		if (currentState.equalsIgnoreCase(S_COORDINATE_WT))
			contents+=str;
		else if (currentState.equalsIgnoreCase(S_NAME_WT))
			nameobject+=str;
		else if (currentState.equalsIgnoreCase(S_DESCRIPT_WT))
			currentLayerId+=str;

	}

}
