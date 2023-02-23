package ru.ts.gis.legend;

import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: ALEXEEV
 * Date: 12.12.2008
 * Time: 16:26:06
 * To change this template use File | Settings | File Templates.
 */
public abstract class Figure {
    public static int MAX_WIDTH=500;
    public static int MAX_HEIGHT=500;
    protected Color bgColor=Color.YELLOW;
    protected Color boundColor=Color.BLACK;
    protected int x0;
    protected int y0;
    protected int width;
    protected int height;
    public Figure()
    {

    }
     public Figure(int x0, int y0, int width, int height) {

        setX0(x0);
        setY0(y0);
        setWidth(width);
        setHeight(height);
    }
    public Figure(Color bgColor, Color boundColor, int x0, int y0, int width, int height) {
        setBgColor(bgColor);
        setBoundColor(boundColor);
        setX0(x0);
        setY0(y0);
        setWidth(width);
        setHeight(height);
    }
     public Color getBgColor() {
        return bgColor;
    }

    public void setBgColor(Color bgColor) {
        this.bgColor = bgColor;
    }

    public Color getBoundColor() {
        return boundColor;
    }

    public void setBoundColor(Color boundColor) {
        this.boundColor = boundColor;
    }

    public int getX0() {
        return x0;
    }

    public void setX0(int x0) {
        this.x0 = x0;
    }

    public int getY0() {
        return y0;
    }

    public void setY0(int y0) {
        this.y0 = y0;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        if(width<MAX_WIDTH)
         this.width = width;
    }

    public int getHeight() {
         return height;
    }

    public void setHeight(int height) {
         if(height<MAX_HEIGHT)
          this.height = height;
    }
}
