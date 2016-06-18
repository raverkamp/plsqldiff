package spinat.plsqldiff.compare.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Vector;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.*;
import javax.swing.border.Border;

public class ChooseTwoFilesDialog {

    // the result of choosing two files
    public static class TwoFiles {

        public final File file1;
        public final File file2;

        public TwoFiles(File file1, File file2) {
            this.file1 = file1;
            this.file2 = file2;
        }
    }

    public static TwoFiles choose(JFrame f, final String preferencesKey) {
        if (SwingUtilities.isEventDispatchThread()) {
            ChooseTwoFilesDialog f2 = new ChooseTwoFilesDialog(f, preferencesKey);
            f2.dialog.setVisible(true);
            return f2.result;
        } else {
            final TwoFiles[] f2 = new TwoFiles[1];
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        f2[0] = ChooseTwoFilesDialog.choose(null, preferencesKey);
                    }
                });
            } catch (InterruptedException | InvocationTargetException ex) {
                throw new RuntimeException(ex);
            }
            return f2[0];
        }
    }

    final JDialog dialog;
    boolean wasok;
    final JComboBox txtRight;
    final JComboBox txtLeft;
    final Preferences preferencesNode;

    // for the result of the dialog
    TwoFiles result;

    public ChooseTwoFilesDialog(JFrame f, String preferencesKey) {
        preferencesNode = getNode(preferencesKey);

        dialog = new JDialog(f);
        final JPanel p = new JPanel();

        dialog.setContentPane(p);
        dialog.setModal(true);
        dialog.setTitle("Select Files");

        p.setLayout(new BorderLayout());

        JLabel lbLeft = new JLabel("Left:");
        JLabel lbRight = new JLabel("Right:");

        JPanel gp = new JPanel();
        p.add(gp, BorderLayout.CENTER);
        gp.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();

        Insets ins = new Insets(4, 4, 4, 4);

        c.anchor = GridBagConstraints.LINE_END;
        c.insets = ins;
        c.weightx = 0;
        c.gridx = 0;

        c.gridy = 0;
        gp.add(lbLeft, c);

        c.gridy = 1;
        gp.add(lbRight, c);

        c.anchor = GridBagConstraints.LINE_START;
        c.weightx = 1;
        c.gridx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridy = 0;

        Border border0 = BorderFactory.createEmptyBorder(20, 20, 20, 20);
        gp.setBorder(BorderFactory.createTitledBorder(border0, "Select files to compare"));

        ArrayList<String> a = getArrayList(preferencesNode.node("left"));
        this.txtLeft = new JComboBox(new Vector(a));
        txtLeft.setEditable(true);
        gp.add(txtLeft, c);

        c.gridy = 1;
        a = getArrayList(preferencesNode.node("right"));
        this.txtRight = new JComboBox(new Vector(a));
        this.txtRight.setEditable(true);
        gp.add(this.txtRight, c);

        c.anchor = GridBagConstraints.LINE_START;
        c.insets = ins;
        c.weightx = 0;
        c.gridx = 2;
        c.fill = 0;

        c.gridy = 0;
        JButton btnLeft = new JButton("Browse");
        btnLeft.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                browseForFile(txtLeft);
            }
        });
        gp.add(btnLeft, c);

        c.gridy = 1;
        JButton btnRight = new JButton("Browse");
        btnRight.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                browseForFile(txtRight);
            }
        });
        gp.add(btnRight, c);

        JButton btnOk = new JButton();
        btnOk.setText("Ok");
        JButton btnCancel = new JButton();
        btnCancel.setText("Cancel");

        btnCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                result = null;
                dialog.setVisible(false);
            }
        });

        btnOk.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ChooseTwoFilesDialog.this.actionOkButton(e);
            }
        });

        Box b = Box.createHorizontalBox();

        b.add(Box.createHorizontalGlue());
        b.add(Box.createVerticalStrut(btnOk.getPreferredSize().height + 2 * 20));
        b.add(btnOk);
        b.add(Box.createHorizontalStrut(20));
        b.add(btnCancel);
        b.add(Box.createHorizontalStrut(20));
        p.add(b, BorderLayout.SOUTH);

        dialog.pack();
        Dimension dp = dialog.getPreferredSize();
        Dimension dmin = dialog.getMinimumSize();
        Dimension dmax = dialog.getMaximumSize();

        dialog.setMinimumSize(new Dimension(dmin.width, dp.height));
        dialog.setMaximumSize(new Dimension(dmax.width, dp.height));

        dialog.pack();
        dialog.setLocationRelativeTo(null);
    }

    ActionListener okActionListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            actionOkButton(e);
        }
    ;

    };

    void actionOkButton(ActionEvent e) {
        String sleft = getJComboString(txtLeft);
        if (!checkFile(sleft)) {
            JOptionPane.showMessageDialog(dialog, "This is not a file:\n" + sleft, "Not a File", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String sright = getJComboString(txtRight);
        if (!checkFile(sright)) {
            JOptionPane.showMessageDialog(dialog, "This is not a file:\n" + sright, "Not a File", JOptionPane.ERROR_MESSAGE);
            return;
        }
        dialog.setVisible(false);
       
        ArrayList<String> lleft = getArrayList(preferencesNode.node("left"));
        adjoin(lleft, sleft, 10);
        putArrayList(preferencesNode.node("left"), lleft);

        ArrayList<String> lright = getArrayList(preferencesNode.node("right"));
        adjoin(lright, sright, 10);
        putArrayList(preferencesNode.node("right"), lright);

        File file1 = new File(sleft);
        File file2 = new File(sright);
        result = new TwoFiles(file1, file2);
    }

    static void adjoin(ArrayList<String> a, String s, int n) {
        if (a.contains(s)) {
            a.remove(s);
            a.add(0, s);
        } else {
            a.add(0, s);
            if (a.size() > n) {
                a.remove(a.size() - 1);
            }
        }
    }

    static void browseForFile(JComboBox tf) {
        String s = getJComboString(tf);
        File f = new File(s);
        JFileChooser jf = new JFileChooser(f);
        int res = jf.showOpenDialog(null);
        if (res == JFileChooser.APPROVE_OPTION) {
            tf.setSelectedItem(jf.getSelectedFile().getAbsolutePath());
        }
    }

    static boolean checkFile(String f) {
        File file = new File(f);
        if (file.isFile() && file.exists()) {
            return true;
        } else {
            return false;
        }
    }

    static Preferences getNode(String preferencesKey) {
        Preferences p = Preferences.userRoot();
        Preferences p2 = p.node(preferencesKey);
        return p2;
    }

    static String getJComboString(JComboBox b) {
        Object o = b.getSelectedItem();
        if (o == null) {
            return "";
        } else {
            return (String) o;
        }
    }

    static void putArrayList(Preferences p, ArrayList<String> a) {
        try {
            p.clear();
            p.putInt("size", a.size());
            for (int i = 0; i < a.size(); i++) {
                p.put("" + i, a.get(i));
            }
        } catch (BackingStoreException e) {
            e.printStackTrace(System.err);
        }
    }

    static ArrayList<String> getArrayList(Preferences p) {
        ArrayList<String> res = new ArrayList<>();
        try {
            int s = p.getInt("size", 0);
            for (int i = 0; i < s; i++) {
                String r = p.get("" + i, null);
                if (r != null) {
                    res.add(r);
                }
            }
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
        return res;
    }

}
