package spinat.plsqldiff;


import java.awt.Desktop;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.concurrent.CountDownLatch;
import javax.swing.SwingUtilities;
import spinat.plsqldiff.compare.CompareResult;
import spinat.plsqldiff.compare.Comparer;
import spinat.plsqldiff.compare.GenHTML;

import spinat.plsqldiff.compare.gui.MainFrame;
import spinat.plsqldiff.compare.gui.ChooseTwoFilesDialog;

public class Main {

    public static int msgAndExit(String s) {
        System.out.println(s);
        return 2;
    }
    
    final static String preferencesKey = "spinat.plsqldiff";

    // compare tow files with PL/SQL code and list real differences
    //  i.e. differences besides layout and comments
    //  an html file with the differences highlighted is shown
    //   in the standard browser
    //   the differences is computd by tokenizing the soource and
    //   discarding whitespace any comment tokens
    //    identifiers (variible names and keywords) are compared case insensitive
    //    BEGIN and begin are considered as equal
    //    but: "A" and A flagged as different
    //      also 1 and 1.0 are flagged as different
    // the return status of the program is 0 if the two files match
    // the return status of the program is 1 if the two files do not match
    public static int mainx(String[] args) throws Exception {

        final String what;

        int p = 0;
        if (args.length > 0) {
            if (args[p].toLowerCase().equals("-html")) {
                what = "html";
                p++;
            } else if (args[p].toLowerCase().equals("-gui")) {
                what = "gui";
                p++;
            } else if (args[p].toLowerCase().equals("-nooutput")) {
                what = "nix";
                p++;
            } else if (args[p].startsWith("-")) {
                return msgAndExit("unknown option: " + args[p]);
            } else {
                what = "gui";
            }
        } else {
            what = "gui";
        }

        final File file1; // = new File(args[p + 0]);
        final File file2; // = new File(args[p + 1]);
        if (args.length - p == 2) {
            file1 = new File(args[p + 0]);
            file2 = new File(args[p + 1]);
        } else if (args.length - p == 0) {
            ChooseTwoFilesDialog.TwoFiles f2 = ChooseTwoFilesDialog.choose(null,preferencesKey);
            if (f2 == null) {
                return 100;
            }
            file1 = f2.file1;
            file2 = f2.file2;
        } else {
            return msgAndExit("one or none file!");
        }
        if (!file1.exists()) {
            return msgAndExit("File with name " + file1.toString() + " does not exist");
        }
        if (!file2.exists()) {
            return msgAndExit("File with name " + file2.toString() + " does not exist");
        }

        CompareResult cres = Comparer.compare(file1, file2);

        if (what.equals("gui")) {
            java.util.concurrent.CountDownLatch cdl = new CountDownLatch(1);
            showDiffWithSwing(cres, cdl, file1, file2);
            cdl.await();
            return 0;
        } else {
            if (what.equals("html")) {
                File f = java.io.File.createTempFile("diff-", ".html");
                GenHTML.printDiff(f.getCanonicalPath(), cres, file1.getAbsolutePath(), file2.getAbsolutePath());
                Desktop.getDesktop().open(f);
            }
            if (cres.distance > 0) {
                System.out.println("FAIL: files are not equivalent, edit distance: " + cres.distance);
                return 1;
            } else {
                System.out.println("OK: files are equivalent");
                return 0;
            }
        }
    }

    public static void main(String[] args) throws Exception {
        try {
            int res = mainx(args);
            System.exit(res);
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            System.exit(100);
        }
    }

    public static void showDiffWithSwing(final CompareResult cres,
            final java.util.concurrent.CountDownLatch cdl,
            final File file1,
            final File file2)
            throws Exception {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    MainFrame f = new MainFrame(preferencesKey);
                    f.addWindowListener(new WindowAdapter() {
                        @Override
                        public void windowClosing(WindowEvent e) {
                            cdl.countDown();
                        }
                    });
                    f.setData(cres, file1, file2);
                    f.setVisible(true);
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
    }
}
