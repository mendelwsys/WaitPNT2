package ru.ts.toykernel.gui.panels;

import ru.ts.apps.bldapp.rule.AssetRule;
import ru.ts.factory.IParam;
import ru.ts.gis.legend.Ellipse;
import ru.ts.gis.legend.Star;
import ru.ts.toykernel.consts.KernelConst;
import ru.ts.toykernel.drawcomp.IDrawObjRule;
import ru.ts.toykernel.drawcomp.ILayer;
import ru.ts.toykernel.drawcomp.rules.def.CommonStyle;
import ru.ts.utils.data.Pair;
import su.mwlib.utils.Enc;
import su.org.imglab.clengine.mapkernel.IPict;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class FViewPicturePanel extends ViewPicturePanel2
{
    public static final int DEF_DRAWWIDTH = 1280;//Используется внешне для установки начальных размеров окна
    public static final int DEF_DRAWHEIGHT = 960; //Используется внешне для установки начальных размеров окна
    public static final String DEFAULT_PICT = "DEFAULT_PICT";
    public static final String VOK_2 = Enc.get("STATION_2_CLASS");
    public static final String VOK = Enc.get("STATION");
    public static final String BLD_PASS = Enc.get("PASSENGER_BUILDING");
    public static final String PAV_PASS = Enc.get("PASSENGER_PAVILION");
    public static final String PAV_TICK = Enc.get("TICKET_PAVILION");
    public static final String PAV_OTHER = Enc.get("OTHER_PAVILION");
    public static final String BLD_OTHER = Enc.get("OTHER_BUILDING");
    static Map<String, IPict> lrn2pict=new HashMap<String, IPict>();

    static
    {
        lrn2pict.put(DEFAULT_PICT,new IPict() //Рисование по умолчанию
        {
            public void drawObject(Graphics graphics, Point central, boolean isselected)
            {
                int[] szXY = getSizeXY();
                int sizeX = szXY[0];
                int sizeY = szXY[1];
                Color color=graphics.getColor();
                if (!isselected)
                    graphics.setColor(new Color(0xFF888888));
                graphics.fillOval((int)central.x-sizeX/2,(int)central.y-sizeY/2,sizeX,sizeY);
                graphics.setColor(color);
            }

            public int[] getSizeXY()
            {
                return new int[]{10,10};
            }
        });

        //Павильон пасажирский ffeeeeee
        lrn2pict.put(VOK_2,new IPict()
        {
            public int[] getSizeXY()
            {
                return new int[]{40,40};
            }

            public void drawObject(Graphics graphics, Point central, boolean isselected)
            {


                Color color=graphics.getColor();
                Color bgColor =color;
                int size = getSizeXY()[0]/2; //ffff3333
                if (!isselected)
                    bgColor=new Color(0xFF888888);


                Star star=new Star(bgColor,Color.BLACK,central.x,central.y,size);
                star.draw(graphics);

                graphics.setColor(color);
                new IPict()
                {
                    public void drawObject(Graphics graphics, Point central, boolean isselected)
                    {
                        Color color=graphics.getColor();
                        Color bgColor =color;
                        int size = getSizeXY()[0]/2; //ffff3333
                        if (!isselected)
                            bgColor=new Color(0xFF888888);


                        Star star=new Star(bgColor,Color.BLACK,central.x,central.y,size);
                        star.draw(graphics);

                        graphics.setColor(color);

                    }

                    public int[] getSizeXY()
                    {
                        return new int[]{20,20};
                    }
                }.drawObject(graphics,central, isselected);
            }
        });

        lrn2pict.put(VOK,new IPict()
        {

            public int[] getSizeXY()
            {
                return new int[]{30,30};
            }

            public void drawObject(Graphics graphics, Point central, boolean isselected)
            {

                Color color=graphics.getColor();
                Color bgColor =color;
                int size = getSizeXY()[0]/2; //ffff3333
                if (!isselected)
                    bgColor=new Color(0xFF888888);
                Star star=new Star(bgColor,Color.BLACK,central.x,central.y,size);
                star.draw(graphics);

                graphics.setColor(color);
            }
        });


        lrn2pict.put(BLD_PASS,new IPict()
        {

            public int[] getSizeXY()
            {
                return new int[]{16,16};
            }

            public void drawObject(Graphics graphics, Point central, boolean isselected)
            {

                int size = getSizeXY()[0]; //ffdd5555
                Color color=graphics.getColor();
                if (!isselected)
                    graphics.setColor(new Color(0xFF888888));

                int[] xPoint=new int[]{(int)central.x- size/2,(int)central.x- size/2,(int)central.x+ size/2,(int)central.x+ size/2,(int)central.x- size/2};
                int[] yPoint=new int[]{(int)central.y- size/2,(int)central.y+ size/2,(int)central.y+ size/2,(int)central.y- size/2,(int)central.y- size/2};
                graphics.fillPolygon(xPoint,yPoint,5);
                graphics.setColor(new Color(0xFF111111));
                graphics.drawPolyline(xPoint,yPoint,5);
                graphics.setColor(color);
            }
        });

        lrn2pict.put(PAV_TICK,new IPict()
        {

            public int[] getSizeXY()
            {
                return new int[]{10,17};
            }

            public void drawObject(Graphics graphics, Point central, boolean isselected)
            {

                Color color=graphics.getColor();
                Color bgColor =color;
                if (!isselected)
                    bgColor=new Color(0xFF888888);

                int[] szXY = getSizeXY();
                int sizeX = szXY[0];//ffeeeeee
                int sizeY = szXY[1];

                Ellipse ellipse=new Ellipse(bgColor,Color.BLACK,central.x-sizeX/2,central.y-sizeY/2,sizeX,sizeY);
                ellipse.draw(graphics);
                graphics.setColor(color);

            }
        });

        lrn2pict.put(PAV_OTHER,new IPict()
        {

            public int[] getSizeXY()
            {
                return new int[]{9,15};
            }

            public void drawObject(Graphics graphics, Point central, boolean isselected)
            {

                Color color=graphics.getColor();
                Color bgColor =color;
                if (!isselected)
                    bgColor=new Color(0xFF888888);

                int[] szXY = getSizeXY();
                int sizeX = szXY[0];//ffeeeeee
                int sizeY = szXY[1];

                Ellipse ellipse=new Ellipse(bgColor,Color.BLACK,central.x-sizeX/2,central.y-sizeY/2,sizeX,sizeY);
                ellipse.draw(graphics);
                graphics.setColor(color);

            }
        });



        lrn2pict.put(PAV_PASS,new IPict()
        {
            public int[] getSizeXY()
            {
                return new int[]{17,17};
            }

            public void drawObject(Graphics graphics, Point central, boolean isselected)
            {

                int size = getSizeXY()[0]; //ffdd5555
                Color color=graphics.getColor();
                if (!isselected)
                    graphics.setColor(new Color(0xFF888888));

                int[] xPoint=new int[]{(int)central.x- size/2,(int)central.x,(int)central.x+ size/2,(int)central.x,(int)central.x- size/2};
                int[] yPoint=new int[]{(int)central.y,(int)central.y- size/2,(int)central.y,(int)central.y+ size/2,(int)central.y};
                graphics.fillPolygon(xPoint,yPoint,5);
                graphics.setColor(new Color(0xFF111111));
                graphics.drawPolyline(xPoint,yPoint,5);
                graphics.setColor(color);

            }
        });



        lrn2pict.put(BLD_OTHER,new IPict()
        {
            public int[] getSizeXY()
            {
                return new int[]{10,10};
            }

            public void drawObject(Graphics graphics, Point central, boolean isselected)
            {

                int size = getSizeXY()[0]; //ffdd5555
                Color color=graphics.getColor();
                if (!isselected)
                    graphics.setColor(new Color(0xFF888888));

                int[] xPoint=new int[]{(int)central.x- size/2,(int)central.x,(int)central.x+ size/2,(int)central.x,(int)central.x- size/2};
                int[] yPoint=new int[]{(int)central.y,(int)central.y- size/2,(int)central.y,(int)central.y+ size/2,(int)central.y};
                graphics.fillPolygon(xPoint,yPoint,5);
                graphics.setColor(new Color(0xFF111111));
                graphics.drawPolyline(xPoint,yPoint,5);
                graphics.setColor(color);
            }
        });

    }

    protected BufferedImage buffimglegend;//Буффер легенд
    private boolean islegend=false;

    static public BufferedImage getLegends(Graphics graph, java.util.List<ILayer> layers)
    {
        BufferedImage bi = null;
        {
//Подсчет сколько места будет занимать легенда
            Font f = graph.getFont();
            f=f.deriveFont(Font.BOLD);
            FontMetrics fm = graph.getFontMetrics(f);
            int maxCntralX=0;
            int maxdY=0;

            java.util.List<Pair<String,Color>> actives=new LinkedList<Pair<String,Color>>();

            for (ILayer layer : layers)
            {
                if (layer.isVisible())
                {
                    String layerName = (String) layer.getLrAttrs().get(KernelConst.LAYER_NAME).getValue();
                    IDrawObjRule drawRule = layer.getDrawRule();
                    String ruleType = drawRule.getRuleType();
                    if (ruleType.equals(AssetRule.RULETYPENAME))
                    {
                        List<IParam> params = drawRule.getObjectDescriptor().getParams();
                        Color color = null;
                        for (IParam param : params)
                        {
                            if (CommonStyle.COLOR_FILL.equals(param.getName())) {
                                String s = param.getValue().toString();
                                try {
                                    long res=Long.parseLong(s,16);
                                    color=new Color((int)res,true);
                                    break;
                                } catch (NumberFormatException e)
                                { //
                                }
                            }
                        }
                        if (color!=null)
                            actives.add(
                                    new Pair<String, Color>
                                            (
                                                    layerName,
                                                    color
                                            ));
                    }
                }
            }

            if (actives.size()>0)
            {
                for (Pair<String,Color> active : actives)
                {
                    IPict pict = lrn2pict.get(active.first);
                    if (pict==null)
                        pict=lrn2pict.get(DEFAULT_PICT);
                    int[] szXY = pict.getSizeXY();
                    int centralX=szXY[0]/2+5;
                    if (maxCntralX<centralX)
                        maxCntralX=centralX;

                    Rectangle2D rect = fm.getStringBounds(active.first, graph);
                    int dY=(int)Math.ceil(Math.max(szXY[1], rect.getHeight())+10);
                    if (maxdY<dY)
                        maxdY=dY;
                }

                int maxX=0;
                int maxY=0;
                for (Pair<String,Color> active : actives)
                {
                    Rectangle2D rect = fm.getStringBounds(active.first, graph);
                    IPict pict = lrn2pict.get(active.first);
                    if (pict==null)
                        pict=lrn2pict.get(DEFAULT_PICT);
                    int[] szXY = pict.getSizeXY();
                    int x = (int) Math.ceil(rect.getWidth()) + maxCntralX+szXY[0]/2 + 15;
                    if (maxX < x)
                        maxX = x;
                    maxY += maxdY+10;
                }

                bi = new BufferedImage(maxX, maxY, BufferedImage.TYPE_INT_ARGB);
                Graphics2D big = bi.createGraphics();
                big.setFont(f);
                big.setColor(new Color(0xccaaaaaa,true));
                big.fillRect(0,0,maxX,maxY);

                int curY=0;
                for (Pair<String,Color> active : actives)
                {
                    IPict pict=lrn2pict.get(active.first);
                    if (pict==null)
                        pict=lrn2pict.get(DEFAULT_PICT);
                    int[] szXY = pict.getSizeXY();
    //                Rectangle2D rect = fm.getStringBounds(active.first, graph);
    //                big.setColor(new Color(0xffff5511,true));
                    big.setColor(active.second);
                    pict.drawObject(big, new Point(maxCntralX,curY+5+maxdY/2),true);
                    big.setColor(new Color(0xff111111,true));
                    big.drawString(active.first,maxCntralX+szXY[0]/2+10,curY+5+maxdY/2);
                    curY+=maxdY+10;
                }
            }
        }
        return bi;
    }

    public boolean setlegend()
    {
        boolean rv=this.islegend;
        this.islegend=!rv;
        repaint();
        return rv;
    }

    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        //TODO Получить и прорисовать легенду
        if (islegend)
        {
            try {
                List<ILayer> layers = projectctx.getLayerList();
                BufferedImage bi=getLegends(g,layers);
                if (bi!=null)
                {
                    int legX = (int) this.getWidth() - bi.getWidth() - 20;
                    Point drawSz = getViewPort().getDrawSize();
                    int heigth = drawSz.y;
                    if (heigth <= bi.getHeight() + 10)
                        g.drawImage(bi, legX, 0, this);
                    else
                        g.drawImage(bi, legX, heigth - (bi.getHeight() + 10), this);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }


}
