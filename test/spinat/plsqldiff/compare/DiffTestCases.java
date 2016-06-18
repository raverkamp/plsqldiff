package spinat.plsqldiff.compare;

public class DiffTestCases {

    public static class String2 {

        public final String f1;
        public final String f2;

        public String2(String f1, String f2) {
            this.f1 = f1;
            this.f2 = f2;
        }
    }

    public static String2 testChristian() throws Exception {
        String s1 = "BEGIN \n"
                + "dbms_output.put_line('Test'); \n"
                + "dbms_output.put_line (q'[Test ]'); \n"
                + "dbms_output.put_line (q'[Test ]'); \n"
                + "dbms_output.put_line ('Test , genau') ; \n"
                + "dbms_output.put_line ('SELECT COUNT(*) \n"
                + "                         FROM user_objects \n"
                + "                        WHERE object_type = ''TABLE'' \n"
                + "                      ');\n"
                + "--\n"
                + "sync \"ABC\" 'xyz'  \"ABC\"\n"
                + " sync2 q'<a'b'c>'";

        String s2 = " BEGIN \n"
                + "-- \n"
                + "dbms_output.put_line('test');\n"
                + "dbms_output.put_line ('Test ') ;\n"
                + "dbms_output.put_line (q['Test -- Dieses ist ein Kommentar]') ;\n"
                + "dbms_output.put_line ('Test /* Dieses ist ein Kommentar */, genau') ;\n"
                + "dbms_output.put_line ('SELECT COUNT(*) FROM user_objects WHERE object_type = ''TABLE''');"
                + "\n"
                + " sync abc   q'[xyz]' \"AbC\"\n"
                + " sync2 'a''b''c'";
        return new String2(s1, s2);
    }
    

    public static String2 testx() throws Exception {
        String s1 = "a b c x\n"
                + " q'[a]' v \"W\"  \n"
                + " <<BLA>> @ <<bla>> \n"
                + "^ #+\n"
                + "1..2 2.3  4.5  >="
                + " a x y z e r r r r r\nx"
                + " a s b\n"
                + "sync \"ABC\" 'xyz'  \"ABC\"\n"
                + " sync2 q'<a'b'c>'";
        String s2 = "a\n b\n c\nx\n"
                + " q'[ab]' v \"W\"  \n"
                + " <<bla>> @ <<blaa>>  \n" // a label is not a single token
                + "! # +\n"
                + "1..3 2.3  4.501 >= \n"
                + "a r x  "
                + "a /* a\n1\n3\n3\n4 */ s /*\b\n1\n2\n3*/b\n"
                + "sync abc   q'[xyz]' \"AbC\"\n"
                + " sync2 'a''b''c'";
        return new String2(s1, s2);
    }
    
    public static String2 endOfString() {
        String s1 = "a b c \n x 'abc\n l';";
        String s2 = "a b c \n x 'abc\n l;";
        return new String2(s1,s2);
    }
    
    public static String2 endOfQIdent() {
        String s1 = "a b c \n x \"abc\n l\"';";
        String s2 = "a b c \n x \"abc\n l';";
        
        return new String2(s1,s2);
    }
    
    public static String2 endOfComment() {
        String s1 = "a b c \n x /*abc\n l*/';";
        String s2 = "a b c \n x /*'abc\n l';";
        
        return new String2(s1,s2);
    }
    
     public static String2 leftEmpty() {
        String s1 = "";
        String s2 = "a b c \n x /*'abc\n l';";
        return new String2(s1,s2);
    }
     
     public static String2 rightEmpty() {
         String s1 = "a b c \n x /*'abc\n l';";
        String s2 = "";
        
        return new String2(s1,s2);
    }
}
