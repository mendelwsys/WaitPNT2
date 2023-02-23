package ru.ts.toykernel.plugins.facils.tables;


import java.util.Map;
import java.util.HashMap;
import java.util.List;

import ru.ts.toykernel.plugins.facils.utils.CSVTableStruct;
import su.mwlib.utils.Enc;

/**
 * Created by IntelliJ IDEA.
 * User: vladm
 * Date: 09.12.2008
 * Time: 17:58:26
 * Таблица справочника дорог
 */
public class NSIRoadsTable extends CSVTableStruct
{
    public static String  code= Enc.get("CODE");
    public static String  roadname=Enc.get("NAME");

    private Map<String,String> roadname2code=new HashMap<String,String>();

    public NSIRoadsTable(String fname, String charsetName) throws Exception
    {
        super(fname, charsetName);
        List<String> codes = data.get(code);
        List<String> roadnames=data.get(roadname);

        for (int i = 0; i < roadnames.size(); i++)
            roadname2code.put(roadnames.get(i),codes.get(i).trim());
    }

    public String getRoadCodeByName(String roadname)
    {
            return roadname2code.get(roadname);
    }


}
