package su.org.imglab.clengine.formatprovider.kml.gui;

import ru.ts.panels.ColorPanel;
import ru.ts.toykernel.pcntxt.gui.defmetainfo.MainformMonitor;
import ru.ts.utils.IOperation;
import ru.ts.toykernel.drawcomp.rules.def.CommonStyle;

import javax.swing.*;
import java.awt.event.*;
import java.io.File;

public class LoadKMLDlg
        extends JDialog {
    public boolean isOk = false;
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField kmlFile;
    private JTextField styleFile;
    private JTextField colorLine;
    private JTextField colorFill;
    private JTextField lineStyle;
    private JTextField scaleFrom;
    private JTextField scaleTo;
    private JButton browseStyle;
    private JButton browseKML;
    private ColorPanel colorLinePane;
    private ColorPanel colorFillPane;
    private JTextField indexFile;
    private JButton browseIndex;
    private JTextField linethickness;

    public LoadKMLDlg() {

        setContentPane(contentPane);
        setTitle("Загрузка из KML");
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

// call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

// call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        browseKML.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                File file = IOperation.getFilePath(MainformMonitor.frame, "Открыть", "KML файл", "xml", null);
                if (file != null)
                    kmlFile.setText(file.getAbsolutePath());
            }
        });

        browseIndex.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                File file = IOperation.getFilePath(MainformMonitor.frame, "Открыть", "Файл индексов", "xml", null);
                if (file != null)
                    indexFile.setText(file.getAbsolutePath());
            }
        });

        browseStyle.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                File file = IOperation.getFilePath(MainformMonitor.frame, "Открыть", "Файл стилей", "xml", null);
                if (file != null)
                    styleFile.setText(file.getAbsolutePath());
            }
        });

        colorLine.addFocusListener(new MyFocusAdapter(colorLinePane, colorLine));
        colorFill.addFocusListener(new MyFocusAdapter(colorFillPane, colorFill));
    }

    public static void main(String[] args) {
        LoadKMLDlg dialog = new LoadKMLDlg();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }

    public void setData(KMLFiles data) {
        kmlFile.setText(data.getKmlfile());
        styleFile.setText(data.getStylefile());
        indexFile.setText(data.getIndexfile());
    }

    public void getData(KMLFiles data) {
        data.setKmlfile(kmlFile.getText());
        data.setStylefile(styleFile.getText());
        data.setIndexfile(indexFile.getText());
    }

    public boolean isModified(KMLFiles data) {
        if (kmlFile.getText() != null ? !kmlFile.getText().equals(data.getKmlfile()) : data.getKmlfile() != null)
            return true;
        if (styleFile.getText() != null ? !styleFile.getText().equals(
                data.getStylefile()) : data.getStylefile() != null) return true;
        if (indexFile.getText() != null ? !indexFile.getText().equals(
                data.getIndexfile()) : data.getIndexfile() != null) return true;
        return false;
    }

    private void onOK() {
        isOk = true;
        dispose();
    }

    private void onCancel() {
// add your code here if necessary
        dispose();
    }

    public void setData(CommonStyle data) {
        String colorLine = data.getsHexColorLine();

        this.colorLine.setText(colorLine);
        colorLinePane.setColor(colorLine);
        String colorFill = data.getsHexColorFill();
        this.colorFill.setText(colorFill);
        colorFillPane.setColor(colorFill);

        scaleTo.setText(data.getScaleHiRange());
        scaleFrom.setText(data.getScaleLowRange());

        lineStyle.setText(data.getsHexLineStyle());
        linethickness.setText(data.getsLineThickness());

    }

    public void getData(CommonStyle data) {
        data.setsHexColorLine(colorLine.getText());
        data.setsHexColorFill(colorFill.getText());
        data.setsHexLineStyle(lineStyle.getText());

        data.setLowRange(scaleFrom.getText());
        data.setHiRange(scaleTo.getText());
        data.setsLineThickness(linethickness.getText());
    }

    public boolean isModified(CommonStyle data) {
        if (colorLine.getText() != null ? !colorLine.getText().equals(
                data.getsHexColorLine()) : data.getsHexColorLine() != null) return true;
        if (colorFill.getText() != null ? !colorFill.getText().equals(
                data.getsHexColorFill()) : data.getsHexColorFill() != null) return true;
        if (lineStyle.getText() != null ? !lineStyle.getText().equals(
                data.getsHexLineStyle()) : data.getsHexLineStyle() != null) return true;
        if (scaleFrom.getText() != null ? !scaleFrom.getText().equals(
                data.getScaleLowRange()) : data.getScaleLowRange() != null) return true;
        if (scaleTo.getText() != null ? !scaleTo.getText().equals(
                data.getScaleHiRange()) : data.getScaleHiRange() != null) return true;
        if (linethickness.getText() != null ? !linethickness.getText().equals(
                data.getsLineThickness()) : data.getsLineThickness() != null) return true;

        return false;
    }

    class MyFocusAdapter extends FocusAdapter {
        private ColorPanel colorPane;
        private JTextField colorText;

        public MyFocusAdapter(ColorPanel colorLinePane, JTextField colorLine) {

            this.colorPane = colorLinePane;
            this.colorText = colorLine;
        }

        public void focusGained(FocusEvent e) {
            super.focusGained(e);
        }

        public void focusLost(FocusEvent e) {
            super.focusLost(e);

            try {
                String strcolor = colorText.getText();

                int color = (int) Long.parseLong(strcolor, 16);
                colorPane.setColor(color);
                colorPane.repaint();
            }
            catch (NumberFormatException e1) {
                colorText.setText("");
                e1.printStackTrace();
            }
        }
    }

}
