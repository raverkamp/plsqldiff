package spinat.plsqldiff.compare;

import spinat.plsqldiff.compare.GenHTML;
import spinat.plsqldiff.compare.Comparer;
import spinat.plsqldiff.compare.CompareResult;
import spinat.plsqldiff.compare.DiffTestCases.String2;
import java.awt.Desktop;
import java.io.File;
import org.junit.Test;
import spinat.plsqldiff.scanner.Token;

public class compareTest {

    public compareTest() {
    }

    public static void compareStrings(String s1, String s2) throws Exception {
        CompareResult cres = Comparer.compare(s1, s2);
        File f = java.io.File.createTempFile("diff-", ".html");
        GenHTML.printDiff(f.getCanonicalPath(), cres);
        Desktop.getDesktop().open(f);
    }

    
    
    public static void compareStrings(String2 s) throws Exception {
        compareStrings(s.f1,s.f2);
    }

    @Test
    public void test_csv() throws Exception {
        spinat.plsqldiff.Main.mainx(new String[]{"-html","example\\csv1.pck", 
            "example\\csv2.pck"});
    } 
    
    @Test
    public void test_csv_equal() throws Exception {
        spinat.plsqldiff.Main.mainx(new String[]{"-html","example\\csv1.pck", 
            "example\\csv1.pck"});
    }

    @Test
    public void test_plog() throws Exception {
        String f1 = "example\\plog-1.pck";
        String f2 = "example\\plog-2.pck";
        spinat.plsqldiff.Main.mainx(new String[]{"-html",f1, f2});
    }

    @Test
    public void testScanner() {
        String s =
                "a b c /*xxx */ as -- bla\n   @ avc";
        Token[] ts = spinat.plsqldiff.scanner.Scanner.scanAll(s);
        for (Token t : ts) {
            System.out.println(t);
        }
    }

    @Test
    public void testx() throws Exception {
        compareStrings(DiffTestCases.testx());
    }

    @Test
    public void testChristian() throws Exception {
        compareStrings(DiffTestCases.testChristian());
    }
}