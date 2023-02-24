package ru.ts.toykernel.plugins.analitics;

import ru.ts.gisutils.algs.common.IMCoordinate;
import ru.ts.gisutils.algs.common.MPoint;
import ru.ts.gisutils.potentialmap.ColorScheme;
import ru.ts.gisutils.potentialmap.Legend;
import ru.ts.toykernel.converters.ILinearConverter;
import ru.ts.toykernel.converters.IProjConverter;
import ru.ts.toykernel.drawcomp.layers.ReliefLayer;
import ru.ts.toykernel.geom.IBaseGisObject;
import ru.ts.toykernel.gui.IViewPort;
import su.mwlib.utils.Enc;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.util.Iterator;

public class FReliefLayer extends ReliefLayer {
    protected PointWithName potencialpoint;
    volatile protected boolean repaintMe = false;
    protected PointWithName[] ppoints;
    protected double maxvalue=0;
    protected IProjConverter _copyConverter;
    protected Point _drawSize;
    protected BufferedImage _bufferedImage;
    volatile protected boolean repaintIt=false;

    protected Iterator<IBaseGisObject> getVisibleObjects(Graphics graphics, ILinearConverter converter,
                                                         Point drawSize) throws Exception {
        return storage.getAllObjects();
    }

    public void  resetLayer()
    {
        repaintIt=true;
    }

    public BufferedImage getLegent(int[] size)
    {
        BufferedImage rv = new BufferedImage(size[0], size[1], BufferedImage.TYPE_INT_ARGB);

        Legend legend = mapbuilder.getLegend();

        int legend_heigth=legend.get_height(rv);
        int legend_width=legend.get_width(rv);

//		legend.set_right(-(legend_width +20)*100/size[0]);
        legend.set_right(-90);
        legend.set_bottom(-10);
        legend.drawLegend = true;
        ColorScheme colors = new ColorScheme(ColorScheme.palRainbow2, maxvalue);
        mapbuilder.drawLegend(rv, colors, maxvalue);

        Font font = rv.getGraphics().getFont();
        int  fontsize=Math.min( 12, font.getSize());

//		BufferedImage image = rv.getSubimage(0, size[1] - legend_heigth-(int)Math.ceil(size[1]*0.1)- 8-fontsize, Math.min((int)(size[0]*0.1)+5*legend_width,size[0]), legend_heigth+16+2*fontsize);
//		try
//		{
//			ImageIO.write(image,"PNG",new File("D:/img.png"));
//		}
//		catch (IOException e)
//		{
//			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//		}
//		return image;
        return rv.getSubimage(0, size[1] - legend_heigth-(int)Math.ceil(size[1]*0.1)- 8-fontsize, Math.min((int)(size[0]*0.1)+5*legend_width,size[0]), legend_heigth+16+2*fontsize);
    }

    /**
     * Установить аналитическую точку
     *
     * @param mousepnt - точка мыши
     * @return Если есть чего рисовать возвращает true, в потивном случае возвращает false
     */
    public boolean setAnalitPointValueByMouse(Point mousepnt)
    {
        int index = -1;
        if (mapbuilder != null)
        {
            double pot = mapbuilder.getPotential(mousepnt.y, mousepnt.x);
            if (pot > 0)
                index = mapbuilder.getIndex(mousepnt.y, mousepnt.x);
        }

        synchronized (this)
        {
            PointWithName l_potencialpoint=potencialpoint;
            if (index >= 0)
            {
                potencialpoint = ppoints[index];
//                potencialpoint = new PointWithName(mousepnt.x, mousepnt.y, ppoints[index].getM(), potentialPoint.getDrawname());
            }
            else
                potencialpoint = null;
            if (!repaintMe)
                this.repaintMe=(potencialpoint!=l_potencialpoint);
        }
        return potencialpoint != null;
    }

    public synchronized void resetRepaint()
    {
        this.repaintMe =  false;
    }

    public synchronized boolean shouldIRepaint()
    {
        return this.repaintMe;
    }

    public IMCoordinate[] convertPoint4Draw(IMCoordinate[] points)
    {
        this.ppoints =new PointWithName[points.length];
        for (int i = 0, pointsLength = points.length; i < pointsLength; i++)
        {
            IMCoordinate ppoint = points[i];
            if (maxvalue < ppoint.getM())
                maxvalue = ppoint.getM();
            this.ppoints[i] = new PointWithName((PointWithName) ppoint);
            if (ppoint.getM() >1)
                ppoint.setM(Math.log(ppoint.getM()));
            else
                ppoint.setM(0);
        }
        return points;
    }

    public void drawAnalitPointValue(Graphics graph, ImageObserver observ, double[] XY)
    {

        PointWithName potencialpoint=null;
        synchronized (this)
        {
            if (this.potencialpoint != null)
                potencialpoint = new PointWithName(this.potencialpoint);
        }

        if (potencialpoint != null)
        {

            if ((int) XY[0] == 0 && (int) XY[1] == 0)
            {
                String tn;
                Color drwtxtcolor=Color.GREEN;
                Color backcolor = Color.DARK_GRAY;
                if (potencialpoint.getM() >= 0)
                {
                    String stname=potencialpoint.getPntName();
                    tn = String.valueOf(((int) (potencialpoint.getM() * 100)) / 100.0) + "/" + (stname != null ? stname : Enc.get("NONAME"));
                }
                else
                {
                    tn = potencialpoint.getPntName();
                }
                Font f = graph.getFont();
                FontMetrics fm = graph.getFontMetrics(f);
                Rectangle2D rect = fm.getStringBounds(tn, graph);
                BufferedImage bi = new BufferedImage((int) (rect.getWidth() + 2), (int) (rect.getHeight() + 2),
                        BufferedImage.TYPE_INT_RGB);
                Graphics graphics = bi.getGraphics();
                graphics.setFont(f);
                graphics.setColor(backcolor);
                graphics.fillRect(0, 0, bi.getWidth(), bi.getHeight());
                graphics.setColor(drwtxtcolor);
                graphics.drawString(tn, 1, bi.getHeight() - 2);
                graph.drawImage(bi, (int) potencialpoint.x, (int) (potencialpoint.y - bi.getHeight()),
                        observ);
            }
        }
    }

    protected BufferedImage makeBufferedImage(Graphics graphics, IViewPort viewPort) throws Exception
    {
        resetRepaint();
        if (
                !repaintIt &&
                        (this._bufferedImage!=null &&
                        viewPort.getDrawSize().equals(_drawSize))
            )
        {
            IProjConverter copyConverter = viewPort.getCopyConverter();
            MPoint new_proj_Point = copyConverter.getPointByDstPoint(new MPoint());
            MPoint old_proj_Point = _copyConverter.getPointByDstPoint(new MPoint());
            if (old_proj_Point.equals(new_proj_Point))
                return _bufferedImage;
        }

        //Redraw relief layer
        {
                MPoint projPoint=null;
                synchronized (this)
                {
                    if (potencialpoint != null && _copyConverter!=null)
                           projPoint = _copyConverter.getPointByDstPoint(potencialpoint);
                }

               _copyConverter=viewPort.getCopyConverter();
               _drawSize = viewPort.getDrawSize();
               _bufferedImage=super.makeBufferedImage(graphics,viewPort);
               synchronized (this)
               {
                   if (potencialpoint!=null && projPoint!=null)
                   {
                       Point drwPoint = _copyConverter.getDstPointByPoint(projPoint);
                       potencialpoint.x=drwPoint.x;
                       potencialpoint.y=drwPoint.y;
                   }
               }
        }
        repaintIt=false;

        return _bufferedImage;
    }


}