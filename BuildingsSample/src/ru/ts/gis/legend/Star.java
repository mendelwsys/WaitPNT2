package ru.ts.gis.legend;

import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: ALEXEEV
 * Date: 15.12.2008
 * Time: 15:35:28
 * To change this template use File | Settings | File Templates.
 */
public class Star extends Figure implements Drawable {


    private int radius;
    private int R;
    public Star() {
        calculateR();
    }

    public Star(int x0, int y0, int radius) {
        super(x0, y0, radius, radius);
        setRadius(radius);
    }

    public Star(Color bgColor, Color boundColor, int x0, int y0, int radius) {
        super(bgColor, boundColor, x0, y0, radius, radius);
        setRadius(radius);
    }

    public void draw(Graphics g) {
       int[] x=new int[10];
       int[] y=new int[10];
       /*
       for(int i=0;i<5;i++)
       {
          if((i%2)==0)
          {
              x[i/2]=x0-(int)(radius*Math.sin(2*i*Math.PI/5));
              y[i/2]=y0-(int)(radius*Math.cos(2*i*Math.PI/5));
          }
          else
          {
             if(i==1)
             {
                 x[3]=x0-(int)(radius*Math.sin(2*i*Math.PI/5));
                 y[3]=y0-(int)(radius*Math.cos(2*i*Math.PI/5));
             }
             else
             {
                 x[4]=x0-(int)(radius*Math.sin(2*i*Math.PI/5));
                 y[4]=y0-(int)(radius*Math.cos(2*i*Math.PI/5));
             }
          }
       }
        */


        for(int i=0;i<x.length;i++)
        {
           if((i%2)==0)
            {
                x[i]=x0-(int)(radius*Math.sin(2*i*Math.PI/x.length));
                y[i]=y0-(int)(radius*Math.cos(2*i*Math.PI/x.length));
            }
           else
            {
               x[i]=x0-(int)(R*Math.sin(2*i*Math.PI/x.length));
               y[i]=y0-(int)(R*Math.cos(2*i*Math.PI/x.length));
            }
        }
        Color tempColor=g.getColor();
        g.setColor(bgColor);
        g.fillPolygon(x,y,x.length);
        g.setColor(boundColor);
        g.drawPolygon(x,y,x.length);
        g.setColor(tempColor);
    }
    public void calculateR()
    {
        double  sin18=Math.sin(Math.PI/10);
        double  ctg72=1.0/Math.tan(2*Math.PI/5);
        double  backSin=1.0-sin18;
        double square=sin18*sin18+backSin*backSin*ctg72*ctg72;
        R=(int)(radius*Math.sqrt(square));
    }
     public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
        calculateR();
    }
}
