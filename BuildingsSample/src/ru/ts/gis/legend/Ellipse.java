package ru.ts.gis.legend;

import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: ALEXEEV
 * Date: 12.12.2008
 * Time: 15:30:55
 * To change this template use File | Settings | File Templates.
 */
public class Ellipse extends Figure implements Drawable {

    public Ellipse()
    {
      super();
    }
    public Ellipse(int x0, int y0, int width, int height) {
       /*
        setX0(x0);
        setY0(y0);
        setWidth(width);
        setHeight(height);
        */
        super(x0,y0,width,height);
    }
    public Ellipse(Color bgColor, Color boundColor, int x0, int y0, int width, int height) {
        /*
        setBgColor(bgColor);
        setBoundColor(boundColor);
        setX0(x0);
        setY0(y0);
        setWidth(width);
        setHeight(height);
        */
        super(bgColor,boundColor,x0,y0,width,height);
    }

    public void draw(Graphics g)
    {
        Color tempColor=g.getColor();
        g.setColor(bgColor);
        g.fillOval(x0,y0,width,height);
        g.setColor(boundColor);
        g.drawOval(x0,y0,width,height);
        g.setColor(tempColor);
    }

}
