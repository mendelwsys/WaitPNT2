package ru.ts.toykernel.plugins;

import ru.ts.utils.data.Pair;

import java.util.List;
import java.util.Map;

/**
 * Интерфейс доступа к командам от удаленного
 * клиента или сервера
 */
public interface ICommandBean
{
	//коды источников команд
	static final int FATCLI=0;//Тослтый клиент
	static final int APPCLI=1;//Клиент аплетный
	static final int BRWCLI=2; //Клиент браузерный

	/**
	 * Отдать идентификатор сессии
	 * @return идентификатор сессии
	 */
	String getSessionId();

	void setSessionId( String sessionid);

	/**
	 * @return имя команды
	 */
	String getCommand();

	/**
	 * Установить команду
	 * @param command
	 */
	void setCommand(String command);

	/**
	 *
	 * @param paramname
	 * @return
	 */
	String getParamByName(String paramname);

	/**
	 *
	 * @param paramname
	 * @return
	 */
	byte[] getbinParams(String paramname);

	/**
	 * @return Источник команды
	 */
	int getCodeActivator();

	Map getParamMap();
}
