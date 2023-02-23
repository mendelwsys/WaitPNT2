/*
 * Created on 10.05.2007
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package ru.ts.tac;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * @author apozdnev
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class TAC2 extends JFrame implements MouseMotionListener, MouseListener,
        ChangeListener, ActionListener {

    /*
     * (non-Javadoc)
     *
     * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
     */
    ArrayList points = new ArrayList();
    Point2D.Double prevpoint;
    double initarea;
    JPanel jp2 = new JPanel();
    JTextField js = new JTextField();

    /**
     * @throws java.awt.HeadlessException
     */
    public TAC2() throws HeadlessException {
        super();
        // TODO Auto-generated constructor stub
        init();
    }

    /**
     * @param gc
     */
    public TAC2(GraphicsConfiguration gc) {
        super(gc);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param title
     * @throws java.awt.HeadlessException
     */
    public TAC2(String title) throws HeadlessException {
        super(title);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param title
     * @param gc
     */
    public TAC2(String title, GraphicsConfiguration gc) {
        super(title, gc);
        // TODO Auto-generated constructor stub
    }

    public static void main(String[] args) {
        TAC2 vr = new TAC2();
        vr.show();
    }

    /*
     * (non-Javadoc)
     *
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
        // TODO Auto-generated method stub
        GeneralPath gp = new GeneralPath();
        gp.moveTo((float) ((Point2D.Double) points.get(0)).x,
                (float) ((Point2D.Double) points.get(0)).y);
        for (int i = 1; i < points.size(); i++) {
            gp.lineTo((float) ((Point2D.Double) points.get(i)).x,
                    (float) ((Point2D.Double) points.get(i)).y);
        }
        String text = js.getText();
        Graphics2D g = (Graphics2D) jp2.getGraphics();

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
                RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        //g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        //g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,RenderingHints.VALUE_STROKE_NORMALIZE);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        //FontRenderContext frc= g.getFontRenderContext();

        //TextPathLayout tpl = new TextPathLayout();
        Font df = g.getFont();
        // df=df.deriveFont(24f);
        // g.setFont(df);
        // Shape tac=tpl.layoutGlyphVector(gv,gp);

        FontRenderContext frc = g.getFontRenderContext();
        //System.out.println(frc.usesFractionalMetrics());
        GlyphVector gv = df.createGlyphVector(frc, text);
        //System.out.println(frc.isAntiAliased());
        TextStroke ts = new TextStroke(gv, frc, TextStroke.ALIGN_MIDDLE, 10f,3,text);
        Shape tac = ts.createStrokedShape(gp);
        g.drawString(text, 10, 10);
        g.fill(tac);
    }

    public void mouseDragged(MouseEvent e) {
        // TODO Auto-generated method stub
        Point p = e.getPoint();
        if (prevpoint == null) {

            prevpoint = new Point2D.Double(p.x, p.y);
            points.add(new Point2D.Double(p.x, p.y));
            return;
        }

        points.add(new Point2D.Double(p.x, p.y));
        prevpoint = new Point2D.Double(p.x, p.y);
        drawInitLine();
    }

    /*
     * (non-Javadoc)
     *
     * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
     */
    public void mouseMoved(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    void init() {
        //this.getContentPane().setLayout(new BorderLayout());

        //	JPanel jp1 = new JPanel();

        jp2.addMouseMotionListener(this);
        jp2.addMouseListener(this);

        final JPanel panel = new JPanel(new GridBagLayout());
        final JPanel panel2 = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        GridBagConstraints d = new GridBagConstraints();
        c.weightx = 1;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        panel.add(jp2, c);
        d.weightx = 1;
        d.weighty = 1;
        //d.fill = GridBagConstraints.NONE;

        JButton send = new JButton("Отправить");
        send.setMaximumSize(new Dimension(50, 25));
        //d.gridx++;
        d.fill = GridBagConstraints.HORIZONTAL;
        d.weightx = 1024;
        panel2.add(js, d);
        //d.gridx++;
        d.weightx = 1;
        send.addActionListener(this);
        d.fill = GridBagConstraints.NONE;
        panel2.add(send, d);
        //pntConsumer.add(panel2, d);
        //d.weighty = 1024;
        //d.fill = GridBagConstraints.BOTH;
        //d.gridx++;
        c.gridx++;
        c.weighty = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        panel.add(panel2, c);
        this.getContentPane().add(panel);

        //	jp1.add(js);

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
     */
    public void mousePressed(MouseEvent e) {
        // TODO Auto-generated method stub
        points.clear();
        jp2.getGraphics().clearRect(jp2.getX(), jp2.getY(), jp2.getWidth(),
                jp2.getHeight());
        prevpoint = null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
     */
    public void mouseReleased(MouseEvent e) {
        // TODO Auto-generated method stub
        //initarea = calculateArea(points);
        //drawVR();
        //drawDP();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
     */
    public void mouseClicked(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
     */
    public void mouseEntered(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
     */
    public void mouseExited(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
     */
    public void stateChanged(ChangeEvent e) {
        // TODO Auto-generated method stub
        //drawVR();
        //drawDP();
    }

    void drawInitLine() {
        for (int i = 1; i < points.size(); i++) {
            Point2D.Double prevpoint = (Point2D.Double) points.get(i - 1);
            Point2D.Double thispoint = (Point2D.Double) points.get(i);
            Graphics g = jp2.getGraphics();
            g.setColor(Color.black);
            g.drawLine((int) prevpoint.x, (int) prevpoint.y, (int) thispoint.x,
                    (int) thispoint.y);
            g.fillOval((int) (prevpoint.x - 2.5), (int) (prevpoint.y - 2.5), 5,
                    5);
            g.fillOval((int) (thispoint.x - 2.5), (int) (thispoint.y - 2.5), 5,
                    5);
        }
    }

    Point2D.Double findIntersectionPoint(Line2D.Double l1, Line2D.Double l2) {
        double x1 = l1.x1;
        double y1 = l1.y1;
        double x2 = l1.x2;
        double y2 = l1.y2;
        double x3 = l2.x1;
        double y3 = l2.y1;
        double x4 = l2.x2;
        double y4 = l2.y2;
        double ua = ((x4 - x3) * (y1 - y3) - (y4 - y3) * (x1 - x3))
                / ((y4 - y3) * (x2 - x1) - (x4 - x3) * (y2 - y1));
        double x = x1 + ua * (x2 - x1);
        double y = y1 + ua * (y2 - y1);

        return new Point2D.Double(x, y);

    }
}
