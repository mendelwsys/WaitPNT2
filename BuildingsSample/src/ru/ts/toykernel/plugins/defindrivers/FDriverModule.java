package ru.ts.toykernel.plugins.defindrivers;

import ru.ts.gisutils.algs.common.MRect;
import ru.ts.toykernel.converters.IProjConverter;
import ru.ts.toykernel.gui.apps.SFFacilities;
import ru.ts.utils.gui.elems.IActionPerform;
import su.mwlib.utils.Enc;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.EventObject;

public class FDriverModule extends DriverModule
{

    public static final int WK_MODE = 0x1;
    public static final int FL_MODE = 0x2;
    public static final String VIEW_SUB_MENU_NAME = Enc.get("DISPLAY");
    public static final String MENU_RESET = Enc.get("RESET_FILTER");
    public static final String MENU_HEADER_WK = Enc.get("GEO_FILTERING_MODE");
    public static final String MENU_HEADER_FL = Enc.get("STANDARD_MODE");
    protected int mode = WK_MODE;
    protected IActionPerform actionSetP2Select = new IActionPerform()
    {
        public void actionPerformed(String fromState, EventObject event)
        {
            MRect l_smallRect;
            SFFacilities app = (SFFacilities) mainmodule.getApplication();
            synchronized (FDriverModule.this)
            {
                l_smallRect = smallRect;
            }

            if (l_smallRect != null)
            {
                try
                {
                    if (Math.abs(l_smallRect.p1.x - l_smallRect.p4.x) >= 30 && Math.abs(l_smallRect.p1.x - l_smallRect.p4.x) >= 30)
                        app.setSelectedByRect(smallRect);
                    else
                    {
                        smallRect = null;
                        app.resetFilter();
                    }
                    app.refreshAll();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }

            synchronized (FDriverModule.this)
            {
                smallRect = null;
            }
        }
    };
    protected IActionPerform[] actions_select = {
            actionSetP1Scale,
            actionMoveScale,
            actionSetP2Select
    };

    public String getMenuName()
    {
        return VIEW_SUB_MENU_NAME;
    }

    public JMenu addMenu(JMenu inmenu) throws Exception
    {
        if (inmenu.getMenuComponentCount()>0)
            inmenu.addSeparator();
        {

            final JMenuItem menuItem = new JMenuItem(getMenuText(), KeyEvent.VK_F);
            inmenu.add(menuItem);
            menuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (mode == WK_MODE)
                        setMode(FL_MODE);
                    else
                        setMode(WK_MODE);
                    menuItem.setText(getMenuText());
                }
            });
        }
        {
            final JMenuItem menuItem = new JMenuItem(MENU_RESET, KeyEvent.VK_F);
            inmenu.add(menuItem);
            menuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e)
                {
                    try
                    {
                        SFFacilities app = (SFFacilities) mainmodule.getApplication();
                        app.resetFilter();
                        app.refreshAll();
                    }
                    catch (Exception ex)
                    {
                        ex.printStackTrace();
                    }
                }
            });
        }
        return super.addMenu(inmenu);
    }

    private String getMenuText() {
        return (mode==WK_MODE)?MENU_HEADER_WK:MENU_HEADER_FL;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode)
    {
        this.mode = mode;
    }

    protected ViewEventDriver getViewEventDriver()
    {
        return new ViewEventDriver() {
            public void mousePressed(MouseEvent event)
            {
                if (mode != WK_MODE)
                {
                    IActionPerform[] l_actions_scale = actions_scale;
                    actions_scale = actions_select;
                    super.mousePressed(event);
                    actions_scale=l_actions_scale;
                }
                else
                    super.mousePressed(event);
            }
        };
    }

}
