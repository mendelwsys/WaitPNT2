package ru.ts.gis.legend;

import javax.swing.*;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: ALEXEEV
 * Date: 12.12.2008
 * Time: 15:31:14
 * To change this template use File | Settings | File Templates.
 */
public class TestLegend {
    public static void main(String[] args)
    {
        JFrame frame=new JFrame("Test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(200,300);
        JPanel panel=new JPanel()
        {
            public void paint(Graphics g)
            {
                Ellipse ellipse=new Ellipse(10,10,15,15);
                ellipse.draw(g);
                NumberedRect rect=new NumberedRect("56",Color.RED,Color.WHITE,Color.BLACK,100,100,60,60);
                rect.draw(g);
                Star star=new Star(Color.RED,Color.WHITE,50,50,50);
                star.draw(g);
            }
        };
        frame.getContentPane().add(panel);
        frame.setVisible(true);
    }
}
