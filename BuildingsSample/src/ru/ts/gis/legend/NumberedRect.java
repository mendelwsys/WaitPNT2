package ru.ts.gis.legend;

import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: ALEXEEV
 * Date: 12.12.2008
 * Time: 16:53:53
 * To change this template use File | Settings | File Templates.
 */
public class NumberedRect extends Figure implements Drawable{

    private String number;
    private Color fgColor;
    public NumberedRect(String number,Color fgColor)
    {
        super();
        this.number=number;
        this.fgColor=fgColor;
    }
    public NumberedRect(String number,Color fgColor,int x0, int y0, int width, int height)
    {
       super(x0,y0,width,height);
       this.number=number;
       this.fgColor=fgColor;
    }
    public NumberedRect(String number,Color fgColor,Color bgColor, Color boundColor, int x0, int y0, int width, int height) {

        super(bgColor,boundColor,x0,y0,width,height);
        this.number=number;
        this.fgColor=fgColor;
    }

    public void draw(Graphics g) {
       Color tempColor=g.getColor();
       Font tempFont=g.getFont();
       g.setColor(bgColor);
       g.fillRect(x0,y0,width,height);
       g.setColor(boundColor);
       g.drawRect(x0,y0,width,height);
       g.setColor(fgColor);
       FontInfo fi=calculateFont(g);
       g.setFont(fi.font);

       g.drawString(number,x0+(int)(width/2.0-fi.swidth/2.0),y0+(int)(height/2.0+fi.sheight/2.0));
       g.setColor(tempColor);
       g.setFont(tempFont);
    }

    private FontInfo calculateFont(Graphics g)
    {
          int i=10;
          Font f=new Font("sunserif",Font.BOLD,i);
          Font previousFont=f;
          FontMetrics fm=g.getFontMetrics(f);
          while(fm.stringWidth(number)<(int)(0.8*width) && fm.getHeight()<(int)(0.9*height))
          {
              previousFont=f;
              i+=2;
              f=new Font("sunserif",Font.BOLD,i);
              fm=g.getFontMetrics(f);
          }
        FontInfo fi=new FontInfo();
        fm=g.getFontMetrics(previousFont);
        fi.font=previousFont;
        fi.sheight=fm.getHeight();
        fi.swidth=fm.stringWidth(number);
        return fi;
    }

    public class FontInfo
    {
        int swidth;
        int sheight;
        Font font;

    }
}
