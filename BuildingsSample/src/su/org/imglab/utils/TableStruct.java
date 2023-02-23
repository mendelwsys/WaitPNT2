package su.org.imglab.utils;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: vladm
 * Date: 09.12.2008
 * Time: 16:59:35
 * Таблица для простой обработки данных в памяти
 */
public class TableStruct
{
    protected Map<String,Map<String,Object>> headersprop;//=new HashMap<String,Map<String,Object>>();//Свойства столбцов Имя Столбца -> Отношение<Название свойства,Объект отображающее это свойство>
    protected Map<String,List<String>> data;//=new HashMap<String,List<String>>();//Имя столбца -> список данных в столбце
    protected List<String> headers;
    

    public TableStruct(){}

    public TableStruct(Map<String, List<String>> data,List<String> headers)
    {
        this.data = data;
        this.headers = headers;
        headersprop=null;
    }
    public TableStruct(Map<String, Map<String, Object>> headersprop, Map<String, List<String>> data,List<String> headers)
    {
        this.headersprop = headersprop;
        this.data = data;
        this.headers = headers;
    }

    public Map<String, List<String>> getData()
    {
        return data;
    }

    public Map<String,String> getTupleByIndex(int index)
    {
        Map<String,String> rv=null;
        if (data!=null)
        {
            rv=new HashMap<String,String>();
            for (String header : data.keySet())
                rv.put(header,data.get(header).get(index));
        }
        return rv;
    }

    public Map<String, Map<String, Object>> getHeadersprop()
    {
        return headersprop;
    }

    /**
     * @return Возврат заголовков в порядке их появления
     */
    public List<String> getHeaders()
    {
        return headers;
    }

//    public List<String> getHeaders()
//    {
//        List<String> rv=new LinkedList<String>();
//        if (data!=null)
//            for (String header : data.keySet())
//                rv.add(header);
//        return rv;
//    }

    public Object getProperty(String colname,String propname)
    {
        if (headersprop!=null)
        {
            Map<String, Object> map = headersprop.get(colname);
            if (map!=null)
                return map.get(propname);
        }
        return null;
    }

    /**
     * Get data in column
     * @param clname - column name
     * @return - data list in column
     */
    public List<String> getColumn(String clname)
    {
        return data.get(clname);
    }
}
