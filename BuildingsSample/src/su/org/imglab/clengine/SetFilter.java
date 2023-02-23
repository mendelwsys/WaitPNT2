package su.org.imglab.clengine;

import ru.ts.toykernel.gui.util.GuiFormEncoder;
import ru.ts.utils.data.Pair;
import su.mwlib.utils.Enc;

import javax.swing.*;
import java.awt.event.*;
import java.util.List;
import java.util.LinkedList;

public class SetFilter extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JComboBox tableField;
    private JTextField formula;
    private JLabel formlabel;

    private Pair<String, String> fileld_formula;

    public SetFilter(List<String> colnames) {

        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);


        for (String colname : colnames)
            tableField.addItem(colname);

        if (colnames.size() > 0)
            tableField.setSelectedIndex(0);

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

        GuiFormEncoder.getInstance().rec(contentPane);
    }

    public static void main(String[] args) {
        SetFilter dialog = new SetFilter(new LinkedList<String>());
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }

    public void setFromulaLabel(String text) {
        formlabel.setText(text);
    }

    public Pair<String, String> getFileld_formula() {
        return fileld_formula;
    }

    private void onOK() {
        fileld_formula = new Pair<String, String>(tableField.getSelectedItem().toString(), formula.getText());
        dispose();
    }

    private void onCancel() {
        fileld_formula = null;
        dispose();
    }

}
