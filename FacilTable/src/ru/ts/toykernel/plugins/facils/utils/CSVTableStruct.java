package ru.ts.toykernel.plugins.facils.utils;


import java.util.*;
import java.io.*;

import ru.ts.csvreader.ReaderUtils;

/**
 * Created by IntelliJ IDEA.
 * User: vladm
 * Date: 09.12.2008
 * Time: 17:51:44
 * Nf,kbwdf
 */
public class CSVTableStruct extends TableStruct
{

    private String del=";";

    public CSVTableStruct()
    {

    }

    //Загрузить таблицу из файла
    public CSVTableStruct(String fname, String charsetName) throws Exception
    {
        this.headers = new LinkedList<String>();
        this.data=loadNSI(del,fname, headers, charsetName);

        if (this.headers==null)
        {
            this.headers = new LinkedList<String>();
            for (String header : data.keySet())
                this.headers.add(header);
        }
    }

    public CSVTableStruct(Map<String, Map<String, Object>> headersprop, Map<String, List<String>> data,List<String> headers)
    {
        super(headersprop, data,headers);
        if (this.headers==null)
        {
            this.headers = new LinkedList<String>();
            for (String header : data.keySet())
                this.headers.add(header);
        }
    }

    public static Map<String, List<String>> loadNSI(String del,String filename, List<String> headers, String charsetName) throws IOException
    {
        BufferedReader br = null;
        br = new BufferedReader(new InputStreamReader(new FileInputStream(filename), charsetName));
        Map<String, List<String>> rawact = ReaderUtils.readNCI(del, headers, br);
        br.close();
        return rawact;
    }

    public String getDel()
    {
        return del;
    }

    public void setDel(String del)
    {
        this.del = del;
    }

    public void save2SVC(String filename) throws Exception
    {
        if (headersprop!=null)
        {
            PrintWriter prw=null;
            try
            {
                String del= null;
                if (headersprop!=null && headersprop.size()>0)
                {
                    prw=new PrintWriter(new FileOutputStream(filename+".desc"));
                    del = "";
                    for (String headername : headersprop.keySet())
                    {
                       prw.print(del+headername);
                       if (del.length()==0)
                        del=this.del;
                    }
                    prw.println();
                    prw.flush();
                    for (String headername : headersprop.keySet())
                    {
                        Map<String, Object> hedprops = headersprop.get(headername);
                        for (String hedprop : hedprops.keySet())
                              prw.println(hedprop+this.del+hedprops.get(hedprop));
                    }
                    prw.println();
                    prw.flush();
                    prw.close();
                }

                del="";
                prw=new PrintWriter(new FileOutputStream(filename));
                for (String headername : headers)
                {
                   prw.print(del+headername);
                   if (del.length()==0)
                        del=this.del;
                }

                prw.println();
                prw.println();
                prw.println();


                Iterator<List<String>> it = data.values().iterator();
                int size=0;
                if (it.hasNext())
                    size=it.next().size();

                for (int  i=0;i<size;i++)
                {
                    del="";
                    for (String headername : headers)
                    {
                        prw.print(del+data.get(headername).get(i));
                        if (del.length()==0)
                            del=this.del;
                    }
                    prw.println();
                }
                prw.flush();
            }
            finally
            {
                if (prw!=null)
                    prw.close();
            }

        }
    }

    public void loadSVC(String filename,String charsetName) throws Exception
    {
        this.headers = new LinkedList<String>();
        this.data=loadNSI(del,filename, headers, charsetName);
        File file_desc = new File(filename + ".desc");
        if (file_desc.exists() && file_desc.isFile())
        {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file_desc), charsetName));
            String brn=br.readLine();
            List<String> headprop = new LinkedList<String>();
            ReaderUtils.readTuple(brn,del, headprop);
            headersprop=new HashMap<String,Map<String,Object>>();

            for (String head : headprop)
            {
                brn=br.readLine();
                List<String> proppairs = new LinkedList<String>();
                ReaderUtils.readTuple(brn,del, proppairs);
                Map<String,Object> propspr=new HashMap<String,Object>();
                for (int i = 0; i < proppairs.size(); i+=2)
                {
                    String propname =  proppairs.get(i);
                    String propval =  proppairs.get(i+1);
                    propspr.put(propname,propval);
               }
               headersprop.put(head,propspr);
            }
        }
    }

}
