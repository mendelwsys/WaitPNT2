package ru.ts.toykernel.plugins.analitics;

import ru.ts.gisutils.algs.common.IMCoordinate;
import ru.ts.gisutils.algs.common.MPoint;
import ru.ts.gisutils.algs.common.MPointZM;
import ru.ts.gisutils.potentialmap.ColorScheme;
import ru.ts.gisutils.potentialmap.Legend;
import ru.ts.mapkernel.FTableStruct;
import ru.ts.toykernel.drawcomp.ILayer;
import ru.ts.toykernel.drawcomp.layers.ReliefLayer;
import ru.ts.toykernel.gui.IViewPort;
import ru.ts.toykernel.gui.apps.SFFacilities;
import ru.ts.toykernel.pcntxt.IProjContext;
import ru.ts.toykernel.pcntxt.gui.defmetainfo.MainformMonitor;
import ru.ts.toykernel.storages.INodeStorage;
import ru.ts.utils.data.Pair;
import su.mwlib.utils.Enc;
import su.org.imglab.clengine.SetFilter;
import su.org.ms.parsers.mathcalc.Parser;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;

public class FAnalitModule extends AnalitModule
{

    public static final String MENU_HIDE_ANALIT = Enc.get("HIDE_ANALYTICS");
    public static final String MENU_SHOW_ANALIT = Enc.get("FIELD_ANALYTICS");
    private Parser pr;

    public JMenu addMenu(JMenu inmenu) throws Exception
    {
        final SFFacilities app = ((SFFacilities) mainmodule.getApplication());
        final FTableStruct acttbl = app.getTable();

        {
            final JMenuItem menuItem = new JMenuItem(MENU_SHOW_ANALIT, KeyEvent.VK_C);
            inmenu.add(menuItem);
            menuItem.addActionListener(new ActionListener()
            {
                SetFilter dialog = null;
                public void actionPerformed(ActionEvent e)
                {
                    if (dialog==null)
                    {
                        dialog = new SetFilter(acttbl.getViewHeaders());
                        dialog.setTitle(Enc.get("SET_FIELD_TO_DISPLAY"));
                        dialog.pack();
                    }
                    dialog.setVisible(true);
                    Pair<String, String> field_formula = dialog.getFileld_formula();
                    if (field_formula!=null)
                    {
                        try
                        {
                            if (pr==null)
                                pr = Parser.createParser(new String[]{});

                            Map<String, List<Pair<String, Double>>> analitparams = acttbl.setAnalitByHeader(pr, field_formula);
                            FReliefProvider reliefProvider = (FReliefProvider) FAnalitModule.this.reliefProvider;
                            if (reliefProvider==null)
                            {
                                ILayer analitLr=getLayerByName(projcontext,showLayerName);
                                if (analitLr instanceof ReliefLayer)
                                    reliefProvider = (FReliefProvider) ((ReliefLayer)analitLr).getReliefProvider();
                            }

                            if (reliefProvider!=null)
                                reliefProvider.setAnalitParams(analitparams);



                            String defTitle = app.getDefaultTitle();
                            MainformMonitor.frame.setTitle(defTitle+Enc.get("___ANALYTICS___")+field_formula.first);
                            fanalitcs = true;
                            mainmodule.refresh(null);
                        }
                        catch (Exception ex)
                        {
                            handleModuleException(ex);
                        }
                    }
                }
            });
        }

        {
            final JMenuItem menuItem = new JMenuItem(MENU_HIDE_ANALIT, KeyEvent.VK_S);
            inmenu.add(menuItem);
            menuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    try {
                        fanalitcs = false;
                        mainmodule.refresh(null);
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
            });
        }
        return inmenu;
    }

    private void handleModuleException(final Exception ex)
    {
        ByteArrayOutputStream barr = new ByteArrayOutputStream();
        ex.printStackTrace(new PrintStream(barr,true));
        JOptionPane.showMessageDialog(MainformMonitor.frame,new String(barr.toByteArray()),Enc.get("FILTERING_ERROR"), JOptionPane.ERROR_MESSAGE);
    }

    public Object[] init(Object... objs) throws Exception
    {
        super.init(objs);
        return null;
    }

    @Override
    public void registerListeners(JComponent component) throws Exception {
        super.registerListeners(component);
        component.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {

            }

            @Override
            public void mouseMoved(MouseEvent e)
            {
                if (fanalitcs)
                {
                    FReliefLayer lr= (FReliefLayer) getLayerByName(projcontext,showLayerName);
                    lr.setAnalitPointValueByMouse(e.getPoint());
                }
            }
        });
    }

    public void paintMe(Graphics g) throws Exception
    {
        super.paintMe(g);
        if (fanalitcs)
        {
            IViewPort viewPort = mainmodule.getViewPort();
            FReliefLayer lr= (FReliefLayer) getLayerByName(projcontext,showLayerName);
            Point drawSize = viewPort.getDrawSize();
            BufferedImage buffImageLegend = lr.getLegent(new int[]{drawSize.x, drawSize.y});
            if (buffImageLegend!=null)
            {
                int ystart = drawSize.y - buffImageLegend.getHeight()-20;
                MPoint point = new MPoint(drawSize.x - buffImageLegend.getWidth(), ystart<0?0:ystart);
                g.drawImage(buffImageLegend, (int) point.x, (int) point.y, new ImageObserver()
                {
                    @Override
                    public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height)
                    {
                        return false;
                    }
                });
            }
            lr.drawAnalitPointValue(g, new ImageObserver() {
                @Override
                public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
                    return false;
                }
            },new double[]{0,0});
        }
    }

    @Override
    public boolean shouldIRepaint() throws Exception
    {
        if (fanalitcs)
        {

            FReliefLayer lr= (FReliefLayer) getLayerByName(projcontext,showLayerName);
            return lr.shouldIRepaint();
        }
        return false;
    }

}
