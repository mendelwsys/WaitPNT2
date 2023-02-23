package su.org.imglab.clengine;

import ru.ts.toykernel.gui.util.GuiFormEncoder;
import su.mwlib.utils.Enc;
import su.org.imglab.utils.ScrollablePicture;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class ViewImage extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton prevImg;
    private JButton nextImg;
    private JPanel mainpanel;
    private int index=0;

    private boolean _isImageReady;
    public ViewImage(String title,final String pathName,final String[] images)
    {
        buttonOK.setText(Enc.get("CLOSE"));
        for (String image : images)
            if (new File(pathName + "/" + image).exists()) {
                _isImageReady = true;
                break;
            }

        setTitle(title);
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        final ScrollablePicture picture = new ScrollablePicture(new ImageIcon(pathName + "/" + images[index]), 5);
        nextImg.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                index++;
                index=index%images.length;
                picture.setIcon(new ImageIcon(pathName + "/" + images[index]));
                ViewImage.this.repaint();
            }
        });

        prevImg.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                index--;
                if (index<0)
                    index=images.length-1;
                picture.setIcon(new ImageIcon(pathName + "/" + images[index]));
                ViewImage.this.repaint();
            }
        });
        if (images.length<2)
        {
            prevImg.setVisible(false);
            nextImg.setVisible(false);
        }
        mainpanel.setLayout(new BorderLayout());
        mainpanel.add(new JScrollPane(picture));
        GuiFormEncoder.getInstance().rec(mainpanel);
    }

    public static void main(String[] args) throws Exception
    {
        //new File("C:\\MAPDIR\\VOK_IMG\\").list();
        ViewImage dialog = new ViewImage(Enc.get("VIEW_PHOTOS"),"C:\\MAPDIR\\VOK_IMG\\",new String[]{Enc.get("148_KM__PRIOZERSK___KUZNECHNOYE_JPG")});
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }

    public boolean isImageReady()
    {
        return _isImageReady;
    }

    private void onOK() {
// add your code here
        dispose();
    }
}
