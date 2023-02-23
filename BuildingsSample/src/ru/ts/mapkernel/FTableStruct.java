package ru.ts.mapkernel;

import ru.ts.toykernel.gui.deftable.TDefaultHeader2;
import ru.ts.utils.gui.tables.IHeaderSupplyer;
import su.mwlib.utils.Enc;
import su.org.imglab.utils.TableStruct;
import su.org.imglab.utils.CSVTableStruct;
import su.org.ms.parsers.mathcalc.Parser;
import su.org.ms.parsers.mathcalc.IGetFormulaByName;
import su.org.ms.parsers.common.ParserException;

import java.util.*;
import java.util.List;
import java.util.regex.PatternSyntaxException;
import java.awt.*;

import ru.ts.utils.data.Pair;
import ru.ts.mapkernel.geom.AttrsConst;


import ru.ts.utils.gui.tables.THeader;
import ru.ts.utils.gui.tables.TNode;

import javax.swing.table.TableModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: vladm
 * Date: 09.12.2008
 * Time: 17:08:58
 * Таблица основных активов
 */
public class FTableStruct extends TableStruct implements IHeaderSupplyer
{
    //Дополнительные имена столбцов для сцылки на объекты на карте
    public static final String TBLLAYRID="TBLLAYRID";//идентикатор слоя в котором находится объект
    public static final String CURVEID="CURVEID";//Идентикатор объекта на слое
    public static final String TBLLAYRID_ORIG="TBLLAYRID_ORIG";//идентикатор слоя в котором находится объект остановочного пункта оригинала
    public static final String CURVEID_ORIG="CURVEID_ORIG";//Идентикатор объекта на слое объект остановочного пункта оригинала
    public static final String BIND_ORIG="BIND_ORIG";//Код по которому связан объект с оригиналом
    public static final String[] ADDCOLS=new String[]{TBLLAYRID,CURVEID,TBLLAYRID_ORIG,CURVEID_ORIG,BIND_ORIG};
    public static final String FNAME = "fname";
    public static final String FN = "fn";
    //Дополнительные атрибуты в создаваемых слоях
    public static final String ATTR_ORIG_LRID = AttrsConst.ATTR_CURVE_CUSTOM + "_ORIG_LRID";
    public static final String ATTR_ORIG_OBJID = AttrsConst.ATTR_CURVE_CUSTOM + "_ORIG_OBJID";
    public static final String ATTR_CNT_OBJ = AttrsConst.ATTR_CURVE_CUSTOM + "_CNT_OBJ";
    public static final String ATTR_LR_GEN = AttrsConst.ATTR_LAYER_CUSTOM + "_LR_GEN";
    private static final String ERR_AGR = Enc.get("INVALID_VALUE_");
    public static String PHIDE="PHIDE";//является ли поле скрытым
    private static String [] CHKONZ_HEADERSNAME={Enc.get("RESIDUAL_VALUE__THOUSAND_RUBLES_"),Enc.get("PERCENT_WEAR___")};
    protected List<Integer> isview;//индекс отображнеи для фильтрации строк
    protected Pair<String,Boolean> sort=null;
    Random rnd=new Random();
    private boolean isfilterset=false;
    private Collection<String> agrigatefield;//поля по которым надо производить агрегацию
    private List<String> rtuple;//Enc.get("AGGREGATED") кортеж
    private THeader[] actheaders;//заголоки активов


    public FTableStruct()
    {

    }

    public FTableStruct(TableStruct table)
    {
        super((table.getData()!=null)?new HashMap<String, List<String>>(table.getData()):null,((table.getHeaders()!=null)?new LinkedList<String>(table.getHeaders()):null));
        initAddCols();
        clearFilter();
    }

    /**
     * Отдать коды станций по привязонному объекту
     * @param boundst - привязанная станция
     * @return - [Код дороги,Код станции ESR,Контрольный код]
     */
    static  public String[] getCodesByStation(String boundst)
    {
        String[] rv=null;
        if (boundst != null && boundst.indexOf(";") < 0)
        {
            rv= new String[3];

            int index0 = boundst.indexOf("_") + 1;
            int index1 = boundst.indexOf("#");

            rv[0] = boundst.substring(0,index0-1).trim();
            rv[1] = boundst.substring(index0, index1).trim();
            rv[2] = boundst.substring(index1+1).trim();
        }
        return rv;

    }

    private void initAddCols()
    {
        if (data!=null)
        {
            headersprop=new HashMap<String, Map<String, Object>>();
            //проинициализировать дополнителдьные колонки таблицы
            Iterator<List<String>> dtit = data.values().iterator();
            int rows=0;
            if (dtit.hasNext())
                rows=dtit.next().size();
            for (String addcol : ADDCOLS)
            {
                LinkedList<String> llval = new LinkedList<String>();
                data.put(addcol, llval);
                for (int i = 0; i < rows; i++)
                    llval.add("");
                headers.add(addcol);
                HashMap<String, Object> colprop = new HashMap<String, Object>();
                headersprop.put(addcol, colprop);
                colprop.put(PHIDE,true);
            }
        }
    }

    public boolean isHide(String colname)
    {
        return getProperty(colname,PHIDE)!=null;
    }

    public void setAddCols(int index,Pair<String,String> orig_id,Pair<String,String> id,String bind)
    {
        data.get(TBLLAYRID_ORIG).set(index,orig_id.first);
        data.get(FTableStruct.CURVEID_ORIG).set(index,orig_id.second);

        data.get(TBLLAYRID).set(index,id.first);
        data.get(FTableStruct.CURVEID).set(index,id.second);

        data.get(BIND_ORIG).set(index,bind);
    }

    public void loadFromfile(String fileName,String chasetName) throws Exception
    {
        CSVTableStruct struct = new CSVTableStruct();
        struct.loadSVC(fileName,chasetName);

        this.data=struct.getData();
        clearFilter();

        this.headersprop=struct.getHeadersprop();
        this.headers=struct.getHeaders();
    }

    /**
     * @return Возврат заголовков в порядке их появления
     */
    public List<String> getViewHeaders()
    {
        List<String> rv= new LinkedList<String>();
        for (String header : headers)
        {
            if (!isHide(header))
                rv.add(header);
        }
        return rv;
    }

    private String createNameStab()
    {
        String rv="__NAMESTAB__";

        while (headers.contains(rv))
            rv+=rnd.nextInt()%10;
        return rv;
    }

    public Map<String,List<Pair<String,Double>>> setAnalitByHeader(Parser pr,Pair<String, String>  headname_formula) throws Exception
    {

        List<String> actvalues = data.get(headname_formula.first);
        List<String> layerIds = data.get(TBLLAYRID);
        List<String> curveIds = data.get(CURVEID);


        Map<String,List<Pair<String,Double>>> rv= new HashMap<String,List<Pair<String,Double>>>();

        for (int rowindex : isview)
        {
            final String actval = actvalues.get(rowindex).replace(",",".");
            try
            {
                Double.parseDouble(actval);

                final String namestab =createNameStab();
                double calcvalue = pr.calculate
                        (
                                namestab +headname_formula.second,
                                new IGetFormulaByName()
                                {

                                    public String getFormulaByName(String parName) throws ParserException
                                    {
                                        if (parName.equals(namestab))
                                            return actval;
                                        throw new ParserException("Unknown symbol:" + parName);
                                    }
                                });//Вычислить значение формулы

                String layerId = layerIds.get(rowindex);
                List<Pair<String,Double>> lcurveId_val=rv.get(layerId);
                if (lcurveId_val==null)
                {
                    lcurveId_val=new LinkedList<Pair<String,Double>>();
                    rv.put(layerId,lcurveId_val);
                }
                lcurveId_val.add(new Pair<String,Double>(curveIds.get(rowindex),calcvalue));
            }
            catch (NumberFormatException e)
            {
                throw new Exception(e);
            }
        }
        return rv;
    }

    /**
     *  Добавить фильтр по формуле
     * @param pr - парсер арифмитических формул
     * @param headname_formula - пара <Имя столбца,формула по которой надо расчитывать выражение>
     * @return Возврат отфильтрованных объектов
     * @throws Exception -
     */
    public Map<String,List<String>> addFilterByFormula(Parser pr,final Pair<String,String> headname_formula) throws Exception
    {
        if (!isfilterset)
        {
            resetView();
            isfilterset=true;
        }

        List<String> actvalues = data.get(headname_formula.first);

        for (int i = 0; i < actvalues.size(); i++)
        {
            final String actval = actvalues.get(i).replace(",",".");
            try
            {
                Double.parseDouble(actval);

                final String namestab =createNameStab();

                double calcvalue = pr.calculate(namestab +headname_formula.second, new IGetFormulaByName()
                {

                    public String getFormulaByName(String parName) throws ParserException
                    {
                        if (parName.equals(namestab) || parName.equalsIgnoreCase(FN) || parName.equalsIgnoreCase(FNAME))
                            return actval;
                        throw new ParserException("Unknown symbol:" + parName);
                    }
                });//Вычислить значение формулы
                boolean iscont = isview.contains(i);
                if (calcvalue!=0 && !iscont)
                    isview.add(i);
                else if (iscont)
                    isview.remove(new Integer(i));
            }
            catch (NumberFormatException e)
            {//
            }
        }

        return getGeoIndexByfilter();
    }

    /**
     *  Добавить фильтр по регулярному выражению
     * @param headname_formula - пара <Имя столбца,регулярное выражение>
     * @return Возврат отфильтрованных объектов
     */
    public Map<String,List<String>>  addFilterByRegExpr(Pair<String,String> headname_formula)
    {
        if (!isfilterset)
        {
            resetView();
            isfilterset=true;
        }

        List<String> actvalues = data.get(headname_formula.first);

        for (int i = 0; i < actvalues.size(); i++)
        {
            try
            {
                String actval = actvalues.get(i);

                boolean isstrfit=actval.matches(headname_formula.second);


                boolean iscont = isview.contains(i);
                if (isstrfit && !iscont)
                    isview.add(i);
                else if (iscont)
                    isview.remove(new Integer(i));
            }
            catch (PatternSyntaxException e)
            {//
            }
        }
        return getGeoIndexByfilter();
    }

    /**
     *
     * @return вернуть индекс по установленному на таблице фильтру <идентфикатор слоя> -> <список идентификаторов объектов на слое>
     */
    public Map<String, List<String>> getGeoIndexByfilter()
    {
        List<String> layerIds = data.get(TBLLAYRID);
        List<String> curveIds = data.get(CURVEID);
        Map<String,List<String>> rv= new HashMap<String,List<String>>();

        for (int rowindex : isview)
        {
            String layerId = layerIds.get(rowindex);
            String curveId = curveIds.get(rowindex);

            List<String> objlist = rv.get(layerId);
            if (objlist == null)
            {
                objlist = new LinkedList<String>();
                rv.put(layerId, objlist);
            }
            objlist.add(curveId);
        }
        return rv;
    }

    public void clearFilter()
    {
        this.isview=new LinkedList<Integer>();
        this.rtuple=null;

        if (data!=null && data.keySet().iterator().hasNext())
        {
            List<String> ll = data.get(data.keySet().iterator().next());
            if (ll!=null)
                for (int i=0;i<ll.size();i++)
                    isview.add(i);
        }
        isfilterset=false;
    }

    /**
     * Добавить фильтр по индексу
     * @param hi слой->(список объектов на слое)
     */
    public void addFilterByIndex(Map<String,List<String>> hi)
    {
        if (data!=null)
        {
            List<String> layerIds = data.get(TBLLAYRID);
            List<String> curveIds = data.get(CURVEID);
            if (!isfilterset)
            {
                resetView();
                isfilterset=true;
            }
            for (int i = 0; i < layerIds.size(); i++)
            {
                if (!isview.contains(i))
                {
                    String layerId = layerIds.get(i);
                    String curveId = curveIds.get(i);
                    List<String> filtercurves = hi.get(layerId);
                    if (filtercurves!=null && filtercurves.contains(curveId))
                        isview.add(i);
                }
            }
        }
    }

    public Pair<String,Boolean> getForvard()
    {
        return sort;
    }

    public void sortByColumnName(String columname,boolean _forvard)
    {
        sort=new Pair<String,Boolean>(columname,_forvard);
        List<String> strs=data.get(columname);
        try
        {
            List<Pair<Double,Integer>> srtd=new ArrayList<Pair<Double,Integer>>(isview.size());
            for (int index : isview)
            {
                String val = strs.get(index);
                val=val.replace(",",".");
                srtd.add(new Pair<Double,Integer>(Double.parseDouble(val),index));
            }
            Collections.sort(srtd,new Comparator<Pair<Double,Integer>>()
            {
                public int compare(Pair<Double, Integer> o1, Pair<Double, Integer> o2)
                {
                    return (sort.second)?o1.first.compareTo(o2.first):o2.first.compareTo(o1.first);
                }
            });
            resetView();
            for (Pair<Double, Integer> d2i : srtd)
                isview.add(d2i.second);
        }
        catch (NumberFormatException e)
        {

            List<Pair<String,Integer>> srti=new ArrayList<Pair<String,Integer>>(isview.size());
            for (int index : isview)
                srti.add(new Pair<String,Integer>(strs.get(index),index));
            Collections.sort(srti,new Comparator<Pair<String,Integer>>()
            {
                public int compare(Pair<String, Integer> o1, Pair<String, Integer> o2)
                {
                    return (sort.second)?o1.first.compareTo(o2.first):o2.first.compareTo(o1.first);
                }
            });
            resetView();
            for (Pair<String, Integer> s2i : srti)
                isview.add(s2i.second);
        }
    }

    private void resetView()
    {
        isview.clear();
        rtuple=null;
    }

    public TableCellRenderer getTableRenderer()
    {
        return new StattioTableRenderer();
    }

    public boolean isAgrigate()
    {
        return agrigatefield !=null && agrigatefield.size()>0;
    }

    public void setAgrigate(Collection<String> agrigatefield)
    {
        this.agrigatefield = agrigatefield;
        rtuple=null;
    }

    public List<String> getAgrigateTuple()
    {
        if (rtuple==null)
            rtuple=getAgrigateTuple(agrigatefield);
        return rtuple;
    }

    public List<String> getAgrigateTuple(Collection<String> agrigatefield)
    {
        List<String> rtuple=new ArrayList<String>(headers.size());
        for (String fildname : headers)
        {
            br:
            if (agrigatefield.contains(fildname))
            {
                List<String> vals=data.get(fildname);
                double sumval=0;
                int maxmultcnt=0;
                for (int i = 0; i < isview.size(); i++)
                {
                    Integer index = isview.get(i);
                    String sval = vals.get(index);
                    try
                    {
                        if (sval != null && sval.length() > 0)
                        {


                            sval = sval.replace(",", ".");
                            String[] strs=sval.split("[\\.]");
                            if (strs.length!=2)
                                sumval += Long.parseLong(sval);//Только целая часть
                            else
                            {
                                int multcnt=strs[1].length();
                                if (maxmultcnt<multcnt)
                                    maxmultcnt=multcnt;
                                long mult=1;
                                for (int j = 0; j < multcnt; j++)
                                    mult*=10;
                                sumval*=mult;
                                sumval += Long.parseLong(strs[0]+strs[1]);
                                sumval/=mult;
                            }

//                             String teststr=String.valueOf(sumval);
//                             if (strs.length>1)
//                             {
//                                 if (strs[1].length()>3)
//                                     System.out.println(strs[1]);
//                             }

                        }
                    }
                    catch (NumberFormatException e)
                    {
                        rtuple.add(ERR_AGR+sval+" :" + i);
                        break br;
                    }
                }

                String rv = String.valueOf(sumval);
                int i=rv.indexOf(".");
                if (i>0)
                {
                    if (maxmultcnt>0)
                        rv=rv.substring(0,Math.min(i+maxmultcnt+1,rv.length()));
                    else
                        rv=rv.substring(0,Math.min(i,rv.length()));
                }
                rtuple.add(rv);
            }
            else
                rtuple.add("");
        }
        if (rtuple.get(0).length()==0)
            rtuple.set(0,Enc.get("TOTAL")+":");
        return rtuple;
    }

    public TableModel getTableModel()
    {
        return new AbstractTableModel()
        {
            public int getColumnCount()
            {
                return getOptionsRepresent().length;
            }

            public int getRowCount()
            {

                return isview.size()+(isAgrigate()?1:0);
            }

            public void setValueAt(Object obj, int row, int col)
            {
                if (!isAgrigate() || row<isview.size())
                {
                    THeader tblheader = getOptionsRepresent()[col];
                    tblheader.setValueAt(obj, col,isview.get(row), data);
                }
            }

            public boolean isCellEditable(int rowIndex, int columnIndex)
            {
                return  (!isAgrigate() || rowIndex<isview.size());
            }

            public Class getColumnClass( int col )
            {
                THeader tblheader = getOptionsRepresent()[ col ];
                return tblheader.getClassValue();
            }

            public Object getValueAt( int row, int col )
            {
                if (!isAgrigate() || row<isview.size())
                {
                    THeader tblheader = getOptionsRepresent()[ col ];
                    return tblheader.getValueAt( col, isview.get(row), data);
                }
                else
                {
                    return getAgrigateTuple().get(col);
                }
            }

            public String getColumnName( int col )
            {
                return getOptionsRepresent()[ col ].getNameField( 0 );
            }

        };

    }

    public void save2File(String filename) throws Exception
    {
        new CSVTableStruct(headersprop,data,headers).save2SVC(filename);
    }

    public  THeader[] getOptionsRepresent()
    {
        if (actheaders ==null)
            actheaders =_getOptionsRepresent();
        return actheaders;

    }

    public THeader[] _getOptionsRepresent()
    {
        List<THeader> lheader=new LinkedList<THeader>();

        for (String headname : headers)
        {
            Map<String, Object> props = null;
            if (headersprop!=null)
                props = headersprop.get(headname);

            if (props==null || props.get(PHIDE)==null || !("true".equalsIgnoreCase(props.get(PHIDE).toString())))
                lheader.add(new TDefaultHeader2(new TNode(headname), headname,false, String.class));
        }
        return lheader.toArray(new THeader[lheader.size()]);
    }

    public List<Integer> getRowIndexByIndices(Map<String, List<String>> selIndices)
    {
        List<Integer> rv=new LinkedList<Integer>();
        for (String lrid : selIndices.keySet())
        {
            List<String> intbllrs = data.get(TBLLAYRID);
            for (int i = 0; i < intbllrs.size(); i++)
            {
                String intbllr = intbllrs.get(i);
                if (intbllr.equals(lrid) && selIndices.get(intbllr).indexOf(data.get(CURVEID).get(i))>=0)
                {
                    //Если идентикаторы слоев совпадают и середи выбранных объектов находится идентификатор объекта отдать
                    int rowindex=-1;
                    if ((rowindex=isview.indexOf(i))>=0)
                        rv.add(rowindex);
                }
            }
        }
        return rv;
    }

    public int getIndexDataByIndexRow(int indexrow)
    {
        return isview.get(indexrow);
    }

    class StattioTableRenderer
            extends JLabel
            implements TableCellRenderer
    {


        private Font normalfont=null;

        private Color getColor(int row,
                               int column)
        {

            if (!isAgrigate() || row<isview.size())
            {
                int index=isview.get(row);
                String origlr=data.get(TBLLAYRID_ORIG).get(index);


                if (origlr==null || origlr.equals(""))
                    return new Color(0xffff7f7f);

                String origcurveId=data.get(CURVEID_ORIG).get(index);
                if (origcurveId==null || origcurveId.equals(""))
                    return new Color(0xffff7f7f);


                for (String chkheader : CHKONZ_HEADERSNAME)
                {
                    int colindex = headers.indexOf(chkheader);
                    if (colindex == column)
                    {
                        String val = data.get(chkheader).get(index);
                        val = val.replace(",", ".");
                        try
                        {
                            if (Double.parseDouble(val) != 0)
                                break;
                        }
                        catch (NumberFormatException e)
                        {//
                        }
                        return new Color(0xffff7f7f);
                    }
                }
            }
            else
            {
                List<String> tuple=getAgrigateTuple();
                if (tuple.get(column).contains(ERR_AGR))
                    return new Color(0xffff7f7f);
                else
                    return Color.GREEN;
            }

            return null;
        }

        public Component getTableCellRendererComponent
                (JTable table,
                 Object value,
                 boolean isSelected,
                 boolean hasFocus,
                 int row,
                 int column)
        {

            if (normalfont==null)
                normalfont=getFont();

            String s = value.toString();
            setText(s);
            if (isSelected)
                setBackground(table.getSelectionBackground());
            else
            {
                Color clr = getColor(row, column);
                if (clr == null)
                    setBackground(table.getBackground());
                else
                    setBackground(clr);
            }

            setOpaque(true);

            if (isAgrigate() && row>=isview.size())
                setFont(getFont().deriveFont(Font.BOLD));
            else if(normalfont!=null)
                setFont(normalfont);
            return this;
        }
    }
}
