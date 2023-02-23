package ru.ts.toykernel.plugins.facils.tables;

import ru.ts.toykernel.plugins.facils.utils.CSVTableStruct;
import su.mwlib.utils.Enc;


/**
 * Created by IntelliJ IDEA.
 * User: vladm
 * Date: 09.12.2008
 * Time: 17:58:03
 * Исходная таблица активов
 */
public class RAWFTable extends CSVTableStruct
{
    public static String FN4LN = Enc.get("FIXED_ASSET");//По значению этого поля именуем слой при создании
    public static String STNM =Enc.get("LOCATION");//По значению этого поля ищем станцию
    public static String DORNM =Enc.get("RAILROAD");//По значению этого поля тоже ищем станцию

    public RAWFTable(String fname, String charsetName) throws Exception
    {
        super(fname, charsetName);
    }
}
