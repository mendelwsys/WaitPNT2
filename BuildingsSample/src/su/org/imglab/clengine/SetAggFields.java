package su.org.imglab.clengine;

import javax.swing.*;
import java.awt.event.*;
import java.util.List;
import java.util.LinkedList;

public class SetAggFields extends JDialog {
    boolean isOk = false;
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JButton toagg;
    private JButton tonotagg;
    private JList notagglist;
    private JList agglist;
    private List<String> fieldlist;
    private List<String> valagglist = new LinkedList<String>();
    private List<String> fieldlist_b;
    private List<String> valagglist_b;
    public SetAggFields(List<String> fieldlist) {
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

        toagg.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int[] selindices = notagglist.getSelectedIndices();
                for (int i = 1; i < selindices.length; i++)
                    selindices[i] -= i;
                for (int selindex : selindices) {
                    saveFieldsLists();
                    valagglist.add(SetAggFields.this.fieldlist.remove(selindex));
                }
                if (selindices.length > 0) {
                    notagglist.setListData(SetAggFields.this.fieldlist.toArray());
                    agglist.setListData(valagglist.toArray());
                }
            }
        });

        this.fieldlist = fieldlist;
        notagglist.setListData(fieldlist.toArray());
        tonotagg.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int[] selindices = agglist.getSelectedIndices();
                for (int i = 1; i < selindices.length; i++)
                    selindices[i] -= i;
                for (int selindex : selindices) {
                    saveFieldsLists();
                    SetAggFields.this.fieldlist.add(valagglist.remove(selindex));
                }
                if (selindices.length > 0) {
                    notagglist.setListData(SetAggFields.this.fieldlist.toArray());
                    agglist.setListData(valagglist.toArray());
                }

            }
        });
    }

    public static void main(String[] args) {
        SetAggFields dialog = new SetAggFields(new LinkedList<String>());
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }

    public List<String> getValagglist() {
        if (isOk)
            return valagglist;
        return null;
    }

    private void saveFieldsLists() {
        if (fieldlist_b == null && valagglist_b == null) {
            fieldlist_b = new LinkedList<String>();
            fieldlist_b.addAll(this.fieldlist);
            valagglist_b = new LinkedList<String>();
            valagglist_b.addAll(valagglist);
        }
    }

    private void onOK() {
        valagglist_b = null;
        fieldlist_b = null;

        isOk = true;
        dispose();
    }

    private void onCancel() {
        //Откатываем все измения
        if (fieldlist_b != null && valagglist_b != null) {
            fieldlist.clear();
            fieldlist.addAll(fieldlist_b);

            valagglist.clear();
            valagglist.addAll(valagglist_b);
        }

        notagglist.setListData(fieldlist.toArray());
        agglist.setListData(valagglist.toArray());
        valagglist_b = null;
        fieldlist_b = null;

        isOk = false;
        dispose();
    }

}
