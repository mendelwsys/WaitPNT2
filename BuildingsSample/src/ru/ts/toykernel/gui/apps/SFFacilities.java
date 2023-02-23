package ru.ts.toykernel.gui.apps;

import ru.ts.factory.IParam;
import ru.ts.gisutils.algs.common.MRect;
import ru.ts.mapkernel.FTableStruct;
import ru.ts.res.ImgResources;
import ru.ts.toykernel.attrs.def.DefAttrImpl;
import ru.ts.toykernel.consts.KernelConst;
import ru.ts.toykernel.converters.ILinearConverter;
import ru.ts.toykernel.converters.IProjConverter;
import ru.ts.toykernel.drawcomp.IDrawObjRule;
import ru.ts.toykernel.drawcomp.ILayer;
import ru.ts.toykernel.factory.BaseInitAble;
import ru.ts.toykernel.filters.DefMBBFilter;
import ru.ts.toykernel.filters.DefSelFilterByKeys;
import ru.ts.toykernel.filters.IBaseFilter;
import ru.ts.toykernel.filters.stream.NodeFilter;
import ru.ts.toykernel.geom.IBaseGisObject;
import ru.ts.toykernel.gui.IApplication;
import ru.ts.toykernel.gui.IViewControl;
import ru.ts.toykernel.gui.panels.BasePicture;
import ru.ts.toykernel.gui.panels.FViewPicturePanel;
import ru.ts.toykernel.pcntxt.IProjContext;
import ru.ts.toykernel.pcntxt.gui.defmetainfo.MainformMonitor;
import ru.ts.toykernel.plugins.IGuiModule;
import ru.ts.toykernel.plugins.IModule;
import ru.ts.toykernel.plugins.gissearch.GisSearch;
import ru.ts.utils.data.InParams;
import ru.ts.utils.data.Pair;
import ru.ts.utils.gui.elems.EmptyProgress;
import ru.ts.utils.gui.elems.IViewProgress;
import ru.ts.apps.bldapp.rule.AssetRule;
import su.mwlib.utils.Enc;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.util.*;
import java.util.List;



/**
 * Created by IntelliJ IDEA.
 * User: VLADM
 * Date: 12.03.2007
 * Time: 17:02:46
 */
public class SFFacilities
        extends BaseInitAble
        implements IApplication
{

    public static final String FILE_SUB_MENU_NAME = Enc.get("FILE");
    public static final String EXIT_MENU_NAME = Enc.get("EXIT");
    public static final String VIEW_SUB_MENU_NAME = Enc.get("DISPLAY");
    public static final String SHOW_LEGENT_MENU_NAME = Enc.get("SHOW_LEGEND");
    public static final String HIDE_LEGEND_MENU_NAME = Enc.get("HIDE_LEGEND");
    public static final String APP_CAPTION = Enc.get("FIXED_ASSETS_V1_1");
    public static final String CSV_PATH_2_TAB_FILE = "csvPath2TabFile";
    static final String uiName = UIManager.getSystemLookAndFeelClassName();
    protected List<IGuiModule> modulelist = new LinkedList<IGuiModule>();
//    private FacilitiesPP picturePanel;
    protected FViewPicturePanel picturePanel;
    boolean ischangeselblock = false;//Флаг для предотвращения множественных вызовов методов при смене выбранных столбцов
    private JPanel mainPanel;
    private JPanel editPanel;
    private JLabel crdStatus;
    private JLabel scaleStatus;
    private JTable actives;
    private JSplitPane spliter;
    private JScrollPane scrollpanel;
    private JToolBar maintoolbar;
    private IViewProgress progress;
    private String csvPath2TabFile;
    private FTableStruct acttbl;//Таблица активов

    public static ImageIcon loadIconFromResources(String icon_path, String altText) {
        final URL imageURL = SFFacilities.class.getResource(icon_path);
        if (imageURL != null)
            return new ImageIcon(imageURL, altText);
        else
            return null;
    }

    @Override
    public Object init(Object obj) throws Exception
    {
            IParam attr=(IParam)obj;
            if (attr.getName().equalsIgnoreCase(KernelConst.VIEWCNTRL_TAGNAME) && attr.getValue() instanceof BasePicture)
            {
//                picturePanel = (BasePicture) attr.getValue();
                picturePanel = (FViewPicturePanel) attr.getValue(); //TODO !!!через плагин!!!
                picturePanel.setApplication(this);
            }
            else if (attr.getName().equalsIgnoreCase(CSV_PATH_2_TAB_FILE))
                csvPath2TabFile=attr.getValue().toString();
            else if (attr.getName().equalsIgnoreCase(KernelConst.PLUGIN_TAGNAME))
                modulelist.add((IGuiModule) attr.getValue());
            return null;
    }

    public void addProjectContext(IProjContext project, IProjConverter converter) throws Exception
    {
        picturePanel.setAllowDraw(false);
        progress.setCurrentOperation("Init modules");
        progress.setProgress((0.8 / modulelist.size()) * progress.getMaxProgress());
        picturePanel.setProjectContext(project, converter, false);
        for (int i = 0; i < modulelist.size(); i++)
        {
            IModule module = modulelist.get(i);
            module.init(new Object[]{new DefAttrImpl(KernelConst.VIEWCNTRL_TAGNAME,picturePanel)});//TODO ВВести в модули функцию reinit()
            //для переинициализации модулей???
            module.registerNameConverter(picturePanel.getProjContext().getNameConverter(), null);
            progress.setProgress((0.8 * (i + 1) / modulelist.size()) * progress.getMaxProgress());
        }
        MainformMonitor.frame.setTitle(getDefaultTitle(project));
        progress.setProgress(1.1 * progress.getMaxProgress());
        picturePanel.setAllowDraw(true);
        picturePanel.refresh(null);
    }

    public String getDefaultTitle()
    {
        return getDefaultTitle(picturePanel.getProjContext());
    }

    protected String getDefaultTitle(IProjContext project)
    {
        return getAppCaption() + " - [" + project.getProjMetaInfo().getProjName() + "]";
    }

    public List<IProjContext> getIProjContexts() throws Exception
    {
        return new LinkedList<IProjContext>(Arrays.asList(picturePanel.getProjContext()));
    }

    public IViewControl getViewControl(IProjContext project)
    {
        return picturePanel;
    }

    public JLabel getScaleStatus() {
        return scaleStatus;
    }

    public Dimension getSizeSplit() {
        return spliter.getSize();
    }

    protected String getAppCaption()
    {
        return APP_CAPTION;
    }

    private JMenu getMenuByName(JMenuBar menuBar, String menuname)
    {
        JMenu menu;
        int ln = menuBar.getMenuCount();
        menu = null;
        for (int i = 0; i < ln; i++)
        {
            menu = menuBar.getMenu(i);
            if (menu.getText().equals(menuname))
                break;
            else
                menu = null;
        }
        return menu;
    }

    @Override
    public void startApp(InParams params, IViewProgress progress) throws Exception
    {
        this.progress = progress;
        if (progress == null)
            progress = new EmptyProgress();

        progress.setCurrentOperation("Init modules");
        progress.setProgress(0);


        try {
            UIManager.setLookAndFeel(uiName);
        }
        catch (Exception ex) {
            System.err.println("Can't set GUI style:" + ex.getMessage());
        }

        MainformMonitor.frame = new JFrame(getAppCaption());
        ImageIcon icon = ImgResources.getIconByName("images/Globus16x16.gif", "MainIcon");
        if (icon != null)
            MainformMonitor.frame.setIconImage(icon.getImage());
        MainformMonitor.form = this;

//        picturePanel = new FacilitiesPP(app.get(InParamsApp.optarr[InParamsApp.O_proj]), actives, true, spliter, app.get(InParamsApp.optarr[InParamsApp.O_bg]));

        editPanel.setLayout(new BorderLayout());
        editPanel.add(picturePanel);

//Установить слушателей мыши
        picturePanel.setPictureListeners(crdStatus,scaleStatus);

        maintoolbar.setMargin(new Insets(0, 0, 0, 0));
        maintoolbar.setBorder(new EmptyBorder(0, 0, 0, 0));

        loadCSVFile(
                csvPath2TabFile,
                //"C:\\PapaWK\\arj\\JOB_SEARCH\\MYGIS\\MAPDIR\\FASILPRJ\\octtable.csv",
                actives);
//создаем Меню  исходя из модулей
        JMenuBar menuBar = new JMenuBar();
        JMenuItem menuItem;
        JMenu menu;

        for (IGuiModule plugin : new LinkedList<IGuiModule>(modulelist))
        {
            String menuname = plugin.getMenuName();
            if (menuname!=null)
            {
                menu = getMenuByName(menuBar, menuname);
                if (menu == null)
                {
                    menu = new JMenu(menuname);
                    menuBar.add(menu);
                }
                plugin.addMenu(menu);
            }
            plugin.addInToolBar(maintoolbar);
            plugin.registerListeners(picturePanel);
            plugin.registerNameConverter(picturePanel.getProjContext().getNameConverter(), null);
            if (!picturePanel.getGuiModules().contains(plugin))
                picturePanel.getGuiModules().add(plugin);
        }


        menu = getMenuByName(menuBar, FILE_SUB_MENU_NAME);
        if (menu == null)
        {
            menu = new JMenu(FILE_SUB_MENU_NAME);
            menuBar.add(menu, 0);
        }
        if (menu.getMenuComponentCount() > 0)
            menu.add(new JSeparator());

        menuItem = new JMenuItem(EXIT_MENU_NAME, KeyEvent.VK_E);
        menu.add(menuItem);
        menuItem.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                System.exit(0);
            }
        });

//        menuItem = new JMenuItem(Enc.get("UPLOAD_PROJECT"), KeyEvent.VK_C);
//        menu.add(menuItem);
//        menuItem.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                try {
//                    File path = IOperation.getFilePath(MainformMonitor.frame, Enc.get("UPLOAD_PROJECT"), "", "prj", null);
//                    if (path!=null)
//                        picturePanel.loadProject(path.getAbsolutePath(), actives);
//                }
//                catch (Exception e1) {//
//                }
//            }
//        });
//        menuItem = new JMenuItem(Enc.get("SAVE_PROJECT"), KeyEvent.VK_C);
//        menu.add(menuItem);
//        menuItem.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                try {
//                    File path = IOperation.getFilePath(MainformMonitor.frame,  Enc.get("SAVE_PROJECT"), "", "prj", null);
//                    if (path!=null)
//                        picturePanel.saveProject(path.getAbsolutePath());
//                }
//                catch (Exception e1) {//
//                }
//            }
//        });
//        menuBar.add(menu);

//        menu = new JMenu(Enc.get("SEARCH"));
//
//        menuItem = new JMenuItem(Enc.get("FIND_ITEM_BY_NAME"), KeyEvent.VK_C);
//        menu.add(menuItem);
//        menuItem.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                picturePanel.searchGraphObject();
//            }
//        });
//        menuBar.add(menu);


        menu = getMenuByName(menuBar, VIEW_SUB_MENU_NAME);
        if (menu == null)
        {
            menu = new JMenu(VIEW_SUB_MENU_NAME);
            menuBar.add(menu, 0);
        }

//!!!TODO Сделать через плагин!!!
        menu.add(new JSeparator());
        final JMenuItem menuItem_l = new JMenuItem(SHOW_LEGENT_MENU_NAME, KeyEvent.VK_C);
        menu.add(menuItem_l);
        menuItem_l.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!picturePanel.setlegend())
                    menuItem_l.setText(HIDE_LEGEND_MENU_NAME);
                else
                    menuItem_l.setText(SHOW_LEGENT_MENU_NAME);
            }
        });
        menuBar.add(menu);

        menu = new JMenu(Enc.get("HELP"));

        menuItem = new JMenuItem(Enc.get("ABOUT"), KeyEvent.VK_F1);
        menu.add(menuItem);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    URL imageURL = SFFacilities.class.getResource("images/" + "About24.gif");
                    ImageIcon icon = null;
                    if (imageURL != null)
                        icon = new ImageIcon(imageURL, Enc.get("ABOUT___"));
                    JOptionPane.showMessageDialog(null, new String[]{
                            "ToyGIS (Demo) ",
                            Enc.get("VERSION_V1_1"),
//              Enc.get("DEVELOPERS_VLAD__YUGL__SYGSKY"),
                            Enc.get("TO_BE_DISPLAYED_ON_THE_MAP_"),
                            Enc.get("ANALYTICAL_INFORMATION_ON_FIXED_ASSETS_")
                            },
                            Enc.get("PROGRAM_SUMMARY"),
                            JOptionPane.INFORMATION_MESSAGE, icon
                    );
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        menuBar.add(menu);

        MainformMonitor.frame.setTitle(getDefaultTitle(picturePanel.getProjContext()));
        MainformMonitor.frame.setJMenuBar(menuBar);

        MainformMonitor.frame.setContentPane(mainPanel);
        MainformMonitor.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        picturePanel.setFocusable(true);
        MainformMonitor.frame.pack();

//        MainformMonitor.frame.setSize(picturePanel.getSizeX(), picturePanel.getSizeY());
//        editPanel.setSize(picturePanel.getSizeX(), picturePanel.getSizeY());
        MainformMonitor.frame.setSize(FViewPicturePanel.DEF_DRAWWIDTH, FViewPicturePanel.DEF_DRAWHEIGHT);
        editPanel.setSize(FViewPicturePanel.DEF_DRAWWIDTH, FViewPicturePanel.DEF_DRAWHEIGHT);

        MainformMonitor.frame.setVisible(true);
        picturePanel.requestFocus();
        spliter.setDividerLocation(0.75);

        progress.setCurrentOperation("End loading projectctx");
        progress.setProgress(1.1 * progress.getMaxProgress());

        if (maintoolbar.getComponentCount()==0)
            maintoolbar.setVisible(false);
    }

    public FTableStruct getTable() {

        return acttbl;
    }

    public void setFilterByKeys(Map<String, List<String>> lr2objs) throws Exception
    {
        Set<String> keys =  new HashSet<String>();
        for (String lrName : lr2objs.keySet())
        {
            List<String> objs = lr2objs.get(lrName);
            for (String obj : objs) {
                keys.add(lrName+"#$"+obj);
            }
        }

        DefSelFilterByKeys filter = new DefSelFilterByKeys();
        filter.setKeySet(keys);
        setMapFilter(filter);
    }

    public void setSelectedByRect(MRect drwRect) throws Exception
    {
        if (drwRect!=null)
        {
            //формируем фильтр по точке
            ILinearConverter linearConverter = picturePanel.getViewPort().getCopyConverter();
            MRect proj_rect = linearConverter.getRectByDstRect(drwRect,null);
            //получаем все объекты попавшие в этот фильтр
            IBaseFilter filter = new DefMBBFilter(proj_rect);
            Set<String> layersNames = setMapFilter(filter);
//            List<ILayer> layerList = picturePanel.getProjContext().getLayerList();
//            Set<String> layersNames = new HashSet<String>();
//            for (ILayer iLayer : layerList)
//            {
//                IDrawObjRule rule = iLayer.getDrawRule();
//                if (rule instanceof AssetRule) {
//                    ((AssetRule) rule).setSelFilter(filter);
//                    String nodeId = ((NodeFilter) iLayer.getFilters().get(0)).getNodeId();
//                    layersNames.add(nodeId);
//                }
//            }

            Iterator<IBaseGisObject> itobj = picturePanel.getProjContext().getStorage().filterObjs(filter);
            Map<String,List<String>> hi =  new HashMap<String, List<String>>();
            while (itobj.hasNext()) {
                IBaseGisObject next = itobj.next();
                String curveId=next.getCurveId();
                String[] lr2id=curveId.split("#\\$");
                if (lr2id!=null && lr2id.length>=2 && layersNames.contains(lr2id[0]))
                {
                    List<String> curveIds = hi.get(lr2id[0]);
                    if (curveIds==null)
                            hi.put(lr2id[0],curveIds=new LinkedList<String>());
                    curveIds.add(lr2id[1]);
                }
            }
            acttbl.clearFilter();
            acttbl.addFilterByIndex(hi);
        }
        else
            resetFilter();
    }

    public void refreshAll()
    {
        refreshTable();
        picturePanel.refresh(null);
    }

    public void resetFilter()
    {
        setMapFilter(null);
        acttbl.clearFilter();
        MainformMonitor.frame.setTitle(getDefaultTitle());
    }

    public Set<String> setMapFilter(IBaseFilter filter)
    {

        Set<String> layersNames = new HashSet<String>();
        List<ILayer> layerList = picturePanel.getProjContext().getLayerList();
        for (ILayer iLayer : layerList)
        {
            IDrawObjRule rule = iLayer.getDrawRule();
            if (rule instanceof AssetRule) {
                ((AssetRule) rule).setSelFilter(filter);
                String nodeId = ((NodeFilter) iLayer.getFilters().get(0)).getNodeId();
                layersNames.add(nodeId);
            }
        }
        return layersNames;
    }

    public void setSelectByIndices(Map<String, List<String>> selIndices)
    {
        if (actives != null && !ischangeselblock)
        {
            List<Integer> indexsel = acttbl.getRowIndexByIndices(selIndices);
            ischangeselblock = true;
            Iterator<Integer> iter = indexsel.iterator();
            if (iter.hasNext()) {
                int index = iter.next();
                actives.getSelectionModel().setSelectionInterval(index, index);
                actives.scrollRectToVisible(actives.getCellRect(index, 0, true));
            }
            ischangeselblock = false;
        }
    }

    public void setSelectByIndices(Pair<String, String> selIndices) throws Exception
    {
        for (IGuiModule iGuiModule : modulelist)
        {
            if (iGuiModule instanceof GisSearch)
            {
                GisSearch gisSearch = (GisSearch) iGuiModule;
                gisSearch.resetSelection();
                gisSearch.setViewByCurveId(selIndices.first+"#$"+selIndices.second);
                break;
            }
        }
    }




    /**
         * Функция для загрузки проекта
         *
         * @param  - путь к файлу csv c описанием основных средств
         * @param actives	  -
         * @throws Exception -
         */
    public void loadCSVFile(String csv_path, final JTable actives) throws Exception
    {
//        String filepath = Files.getDirectory(projDescPath);
        {
//            ini.load(projDescPath);
//            String card_file = ini.get(MAIN_SECTION, CARD_FILE, "");
//            lrstr = new DataInputStream(new FileInputStream(filepath + "/" + card_file));
//            HashSet<String> excludname = new HashSet<String>();
//            excludname.add("NONAME");
//            replaceLayers(lrstr, lfactory, excludname);
//            String  = ini.get(MAIN_SECTION, DB_FILE, "");
//            String db_encode = ini.get(MAIN_SECTION, DB_ENCODE, "UTF-8");

            String db_encode = "UTF-8";
            acttbl = new FTableStruct();
            acttbl.loadFromfile(csv_path, db_encode);

            actives.setModel(acttbl.getTableModel());
            actives.setDefaultRenderer(java.lang.Object.class, acttbl.getTableRenderer());

            actives.getSelectionModel().addListSelectionListener(
                    new ListSelectionListener()
                    {
                        int curentindex = -1;

                        public void valueChanged(ListSelectionEvent e)
                        {
                            if (ischangeselblock)
                                curentindex = -1;
                            else
                            {
                                int selindex = acttbl.getIndexDataByIndexRow(actives.getSelectedRow());
                                if (selindex >= 0 && selindex != curentindex)
                                {
                                    curentindex = selindex;
                                    Map<String, String> tuple = acttbl.getTupleByIndex(curentindex);
                                    if (tuple != null)
                                    {
                                        try
                                        {
                                            ischangeselblock = true;
                                            setSelectByIndices(new Pair<String, String>(tuple.get(FTableStruct.TBLLAYRID), tuple.get(FTableStruct.CURVEID)));
                                        }
                                        catch (Exception ex)
                                        {
                                            ex.printStackTrace();
                                        }
                                        finally
                                        {
                                            ischangeselblock = false;
                                        }
                                    }
                                }
                            }
                        }
                    });


            final JTableHeader tableHeader = actives.getTableHeader();
            tableHeader.addMouseListener(new MouseListener()
            {

                public void mouseClicked(MouseEvent e)
                {
                    if (e.getButton()== MouseEvent.BUTTON3)
                    {
                        int index = tableHeader.columnAtPoint(e.getPoint());
                        if (index>=0)
                        {
                            String colname=actives.getColumnName(index);
                            Pair<String, Boolean> frsort = acttbl.getForvard();
                            if (frsort!=null && colname.equals(frsort.first))
                                acttbl.sortByColumnName(colname,!frsort.second);
                            else
                                acttbl.sortByColumnName(colname,true);
                        }
                        refreshTable();
                    }
                }

                public void mousePressed(MouseEvent e)
                {
                }

                public void mouseReleased(MouseEvent e)
                {
                }

                public void mouseEntered(MouseEvent e)
                {
                }

                public void mouseExited(MouseEvent e)
                {
                }
            });
//            this.actives = actives;
//            this.refresh();
        }

    }

    public void refreshTable() {
        if (acttbl != null && actives != null)
        {
            actives.revalidate();
            actives.repaint();
        }
    }


}

