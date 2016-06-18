package spinat.plsqldiff.compare.gui;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.File;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import spinat.plsqldiff.compare.CompareResult;
import spinat.plsqldiff.compare.Comparer;

public class MainFrame extends JFrame {

    final JLabel header;
    final DiffView diffview;
    // needed for storing the history of the compared files
    final String preferencesKey;
    
    public MainFrame(String prefencesKey) {
        this.preferencesKey = prefencesKey;
        this.setLayout(new BorderLayout());
        header = new JLabel();
        header.setBorder(BorderFactory.createEmptyBorder(10, 40, 10, 0));
        this.add(header, BorderLayout.NORTH);
        diffview = new DiffView();
        this.add(diffview, BorderLayout.CENTER);

        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("File");
        menuBar.add(menu);
        JMenuItem iopen = new JMenuItem("Open");
        menu.add(iopen);
        
        iopen.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                open();
            }
        });
        JMenuItem iexit = new JMenuItem("Exit");
        iexit.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        menu.add(iexit);
        this.setJMenuBar(menuBar);
        this.setExtendedState(this.getExtendedState() | Frame.MAXIMIZED_BOTH);
    }

    void open() {
        ChooseTwoFilesDialog.TwoFiles f2 = ChooseTwoFilesDialog.choose(null,this.preferencesKey);
        if (f2 == null) {
            return;
        }
        File file1 = f2.file1;
        File file2 = f2.file2;
        final CompareResult cres;
        try {
            cres = Comparer.compare(file1, file2);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        this.setData(cres, file1, file2);
    }

    public void setData(CompareResult cres, File file1, File file2) {
        final String txt;
        if (cres.distance == 0) {
            txt = "The files match.";
        } else {
            txt = "Edit distance is " + cres.distance;
        }
        this.header.setText(txt);
        String n1 = file1.getName();
        String n2 = file2.getName();
        this.setTitle("plsqldiff - [" + n1 + " - " + n2 + "]");
        diffview.setData(cres.lines1, cres.lines2, file1.getAbsolutePath(), file2.getAbsolutePath());
    }

}
