package spinat.plsqldiff.compare;

import spinat.plsqldiff.Main;
import spinat.plsqldiff.compare.DiffTestCases.String2;
import spinat.plsqldiff.compare.gui.DiffView;
import spinat.plsqldiff.compare.gui.ChooseTwoFilesDialog;
import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.junit.Test;


/**
 *
 * @author rav
 */
public class GuiCompareTest {

    public final java.util.concurrent.CountDownLatch cdl = new CountDownLatch(1);

    public GuiCompareTest() {
    }

    public void showDiff(final List<ArrayList<DisplayedToken>> a1, final List<ArrayList<DisplayedToken>> a2) {
        printDiff(a1, a2);

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    JFrame f = new JFrame();
                    f.setLayout(new BorderLayout());

                    DiffView dd = new DiffView();
                    f.add(dd, BorderLayout.CENTER);
                    f.pack();

                    f.setSize(600, 400);
                    f.setVisible(true);
                    //f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    f.addWindowListener(new WindowAdapter() {
                        @Override
                        public void windowClosing(WindowEvent e) {
                            cdl.countDown();
                        }
                    });
                    f.setVisible(true);
                    dd.setData(a1, a2,"left","right");

                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }

            }
        });

        // System.console().readLine();
    }

    public static void printLine(ArrayList<DisplayedToken> l) {
        if (l.isEmpty()) {
            System.out.print("    : ");
        } else {
            System.out.print("" + l.get(0).token.line + ": ");
        }
        for (DisplayedToken t : l) {
            System.out.print(t.token.value);
        }
        System.out.println();
    }

    public static void printDiff(List<ArrayList<DisplayedToken>> a1,
            List<ArrayList<DisplayedToken>> a2) {
        for (int i = 0; i < a1.size(); i++) {
            System.out.println("------------------------------------");
            printLine(a1.get(i));
            printLine(a2.get(i));
        }
    }

    @Test
    public void test_plog() throws Exception {
        Main.mainx(new String[]{"-gui", "example\\plog-1.pck", "example\\plog-2.pck"});
    }
    @Test
    public void test_csv() throws Exception {
        Main.mainx(new String[]{"-gui", "example\\csv1.pck", "example\\csv2.pck"});
    }

    
    void doTestCase(String2 s) {
        CompareResult cres = Comparer.compare(s.f1, s.f2);
        List<ArrayList<DisplayedToken>> a1 = cres.lines1;
        List<ArrayList<DisplayedToken>> a2 = cres.lines2;
        showDiff(a1, a2);
        try {
            cdl.await();
        } catch (InterruptedException ex) {
           throw new RuntimeException(ex);
        }
    }
    
    @Test
    public void test2() throws Exception {
       
        String s1 = "a e\nc\nx";
        String s2 = "b e\nc\ny";
        doTestCase(new String2(s1,s2));
    }

    @Test
    public void testChristian() throws Exception {
        doTestCase(DiffTestCases.testChristian());
    }

    @Test
    public void chooseTest() throws Exception {
       System.out.println(ChooseTwoFilesDialog.choose(null,"choose-test"));
    }
    
    @Test
    public void testGuiMain() throws Exception {
      Main.mainx(new String[]{"-gui"});
    }
    
    @Test 
    public void testStringEnd() throws Exception {
        doTestCase(DiffTestCases.endOfString());
    }
    
    @Test 
    public void testQIdentEnd() throws Exception {
        doTestCase(DiffTestCases.endOfQIdent());
    }
    
    @Test 
    public void testCommentEnd() throws Exception {
        doTestCase(DiffTestCases.endOfComment());
    }
    
    @Test 
    public void testLeftEmpty() throws Exception {
        doTestCase(DiffTestCases.leftEmpty());
    }
    
    @Test 
    public void testRightEmpty() throws Exception {
        doTestCase(DiffTestCases.rightEmpty());
    }
}