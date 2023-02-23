package ru.ts.forms;

import ru.ts.toykernel.gui.util.GuiFormEncoder;
import su.mwlib.utils.Enc;

import javax.swing.*;
import java.awt.event.*;

public class FrmLogin extends JDialog {
    public boolean isOk;
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField txtName;
    private JPasswordField pswPass;

    public FrmLogin(String title, String name) {
        isOk = false;
        this.setTitle(title);
        txtName.setText(name);

        setContentPane(contentPane);
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
        GuiFormEncoder.getInstance().rec(contentPane);
    }

    public static void main(String[] args) {
        FrmLogin dialog = new FrmLogin(Enc.get("USER"), "");
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }

    public String getName() {
        return this.txtName.getText();
    }

    public String getPass() {
        return new String(this.pswPass.getPassword());
    }

    private void onOK() {
        isOk = true;
        dispose();
    }

    private void onCancel() {
// add your code here if necessary
        dispose();
    }

}
