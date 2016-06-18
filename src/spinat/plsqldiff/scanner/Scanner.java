package spinat.plsqldiff.scanner;

import java.util.ArrayList;

public final class Scanner {

    // a simple scanner for a PL/SQL like language
    
    static boolean is_white_space(char c) {
        return Character.isWhitespace(c) || Character.isISOControl(c);
    }

    private static class LineCol {

        public final int line;
        public final int col;

        public LineCol(int line, int col) {
            this.line = line;
            this.col = col;
        }
    }

    private static class ScanRes {

        public final Token token;
        public final int start;
        public final int line;
        public final int col;

        public ScanRes(Token token,
                int start,
                int line,
                int col) {
            this.token = token;
            this.start = start;
            this.line = line;
            this.col = col;
        }
    }

    static class Scan1Class {

        final String str;
        final int start;
        final int istart;
        final int line;
        final int col;
        final int len;
        final int no;

        public Scan1Class(String str, int start, int istart, int line, int col, int no) {
            this.str = str;
            this.start = start;
            this.istart = istart;
            this.line = line;
            this.col = col;
            this.len = str.length();
            this.no = no;
        }

        char get(int pos) {
            if (pos >= len) {
                throw new RuntimeException("unexpected end of string");
            } else {
                return str.charAt(pos);
            }
        }

        int scan_string(int pos) {
            if (get(pos) != '\'') {
                throw new RuntimeException("BUG");
            }
            int x = pos + 1;
            while (true) {
                if (x >= len) {
                    return -1;
                }
                char c = get(x);
                if (c == '\'') {
                    if ((x + 1) < len && get(x + 1) == '\'') {
                        x = x + 2;
                    } else {
                        return x + 1;
                    }
                } else {
                    x = x + 1;
                }
            }
        }

        int scan_qident(int pos) {
            if (get(pos) != '"') {
                throw new RuntimeException("BUG");
            }
            int x = pos + 1;
            while (true) {
                if (x >= len) {
                    return -1;
                }
                if (get(x) == '"') {
                    return x + 1;
                } else {
                    x = x + 1;
                }
            }
        }

        int scan_ml_comment(int start) {
            if (get(start) != '/' || get(start + 1) != '*') {
                throw new RuntimeException("BUG");
            }
            int x = start + 2;
            while (true) {
                if (x >= len - 2) {
                    return -1;
                }
                if (get(x) == '*' && get(x + 1) == '/') {
                    return x + 2;
                } else {
                    x = x + 1;
                }
            }
        }

        int scan_eol_comment(int start) {
            assert get(start) == '-' && get(start + 1) == '-';
            int x = start + 2;
            while (true) {
                if (x >= len) {
                    return x;
                }
                if (get(x) == (char) 10) {
                    return x; // a new token for a newline
                } else {
                    x++;
                }
            }
        }

        boolean ident_member(char c) {
            return Character.isLetterOrDigit(c)
                    || (c == '$') || (c == '#') || c == '_';
        }

        int scan_ident(int start) {
            int x = start;
            while (true) {
                if (x >= len) {
                    return x;
                }
                if (ident_member(get(x))) {
                    x++;
                } else {
                    return x;
                }
            }
        }

        int scan_int(int start) {
            int x = start;
            while (true) {
                if (x >= len) {
                    return x;
                } else if (Character.isDigit(get(x))) {
                    x = x + 1;
                } else {
                    return x;
                }
            }
        }

        int scan_ws(int start) {
            int x = start;
            while (true) {
                if (x >= len) {
                    return x;
                } else if (get(x) == (char) 10) {
                    // newlines have their own token
                    return x;
                } else if (is_white_space(get(x))) {
                    x++;
                } else {
                    return x;
                }
            }
        }

        LineCol advPos(int start_line, int start_col, int from, int to) {
            int position = from;
            int end_line = start_line;
            int end_col = start_col;
            while (true) {
                if (position >= to) {
                    return new LineCol(end_line, end_col);
                }
                if (this.get(position) == (char) 10) {
                    end_line++;
                    end_col = 0;
                   position++;
                } else {
                    position++;
                    end_col++;
                }
            }
        }

        // create a token of tokentype, its string respresenttation starts 
        // starts at this.start and ends at next (next is after the last character
        ScanRes tokx(TokenType tokenType, int next) {
            LineCol lc = advPos(this.line, this.col, start, next);
            String s = str.substring(start, next);
            String svalue;
            switch (tokenType) {
                case SString:
                    svalue = sValueString(s);
                    break;
                case QString:
                    svalue = sValueQString(s);
                    break;
                case Ident:
                    svalue = s.toUpperCase();
                    break;
                case QIdent:
                    svalue = sValueQIdentifier(s);
                    break;
                default:
                    svalue = s;
            }
            return new ScanRes(new Token(tokenType, start, line, col,
                    s, svalue, no), next, lc.line, lc.col);
        }

        /* scan exponent of float,  epos is the position of e */
        int scan_expo(int epos) {
            int p;
            if (epos + 1 < len && (get(epos + 1) == '+' || get(epos + 1) == '-')) {
                p = epos + 2;
            } else {
                p = epos + 1;
            }
            if (p < len && Character.isDigit(get(p))) {
                return scan_int(p);
            } else {
                throw new RuntimeException("bad expo token");
            }
        }

        int scan_float_rest(int dotpos) {
            // dotpos is position of dot !
            assert get(dotpos) == '.';

            int p = scan_int(dotpos + 1);
            if (p < this.len && get(p) == 'E' || get(p) == 'e') {
                return scan_expo(p);
            } else {
                return p;
            }
        }


        /* for i in 1.. 2 loop null; end loop; 
           so a string starting with "1." is not necessarily a float !
        */
        ScanRes scan_number(int start) {
            int x = scan_int(start);
            /* we only have to check for the second "." */
            if (x >= len || (x + 1 < len && get(x + 1) == '.')) {
                return tokx(TokenType.Int, x);
            } else {
                int p;
                if (get(x) == '.') {
                    p = scan_int(x + 1);
                } else {
                    p = x;
                }
                if (p < len && ('E' == get(p) || 'e' == get(p))) {
                    return tokx(TokenType.Float, scan_expo(p));
                } else {
                    if (p == x) { // no .
                        return tokx(TokenType.Int, x);
                    } else {
                        return tokx(TokenType.Float, p);
                    }
                }
            }
        }

        // scan a special PL/SQL string, the ones like q'[...]'
        int qString(int start) {
            char ende;
            char c = get(start + 2);
            // what are the rules for the end marker?
            switch (c) {
                case '[':
                    ende = ']';
                    break;
                case '(':
                    ende = ')';
                    break;
                case '{':
                    ende = '}';
                    break;
                case '<':
                    ende = '>';
                    break;
                default:
                    ende = c;
            }
            int i = start + 2;
            while (true) {
                if (get(i) == ende && get(i + 1) == '\'') {
                    return i + 2;
                } else {
                    i++;
                }
            }
        }

        // create a token for a string of size 2
        ScanRes tok2(TokenType tt) {
            return tokx(tt, start + 2);
        }

        // create a token for a string of size 1
        ScanRes tok1(TokenType tt) {
            return tokx(tt, start + 1);
        }

        static String sValueString(String s) {
            String a = s.substring(1, s.length() - 1);
            return a.replace("''", "'");
        }

        static String sValueQIdentifier(String s) {
            return s.substring(1, s.length() - 1);
        }

        static String sValueQString(String s) {
            return s.substring(3, s.length() - 2);
        }

        // try to scan based on the first character
        ScanRes check1() {
            char c = get(start);
            switch (c) {
                case '\n':
                    return tok1(TokenType.Newline);
                case '\'': {
                    int x = scan_string(start);
                    if (x >= 0) {
                        return tokx(TokenType.SString, x);
                    } else {
                        return tokx(TokenType.Schrott, len);
                    }
                }
                case '"': {
                    int x = scan_qident(start);
                    if (x >= 0) {
                        return tokx(TokenType.QIdent, x);
                    } else {
                        return tokx(TokenType.Schrott, len);
                    }
                }
                case '(':
                    return tok1(TokenType.LParen);
                case ')':
                    return tok1(TokenType.RParen);
                case '>':
                    return tok1(TokenType.Greater);
                case '<':
                    return tok1(TokenType.Less);
                case '-':
                    return tok1(TokenType.Minus);
                case '+':
                    return tok1(TokenType.Plus);
                case '.':
                    return tok1(TokenType.Dot);
                case '/':
                    return tok1(TokenType.Div);
                case '*':
                    return tok1(TokenType.Mul);
                case ',':
                    return tok1(TokenType.Comma);
                case ';':
                    return tok1(TokenType.Semi);
                case '=':
                    return tok1(TokenType.Equal);
                case '%':
                    return tok1(TokenType.Percent);
                case ':':
                    return tok1(TokenType.Colon);
                case '!':
                    return tok1(TokenType.Exclamation);
                default:
                    if (Character.isDigit(c)) {
                        return scan_number(start);
                    } else if (is_white_space(c)) {
                        return tokx(TokenType.WhiteSpace, scan_ws(start));
                    } else if (Character.isLetter(c)) {
                        return tokx(TokenType.Ident, scan_ident(start));
                    } else {
                        //System.err.println("strange char at : " + line + ", " + col + ": " + c);
                        return tok1(TokenType.StrangeChar);
                    }
            }
        }

        // scan one token from this string, beginning at start
        // first try the well known prefixes with two characters
        ScanRes scan1() {
            if (this.start <= this.len - 2) {
                String a = this.str.substring(this.start, this.start + 2);
                if (a.equals("=>")) {
                    return tok2(TokenType.Arrow);
                }
                if (a.equals("<=")) {
                    return tok2(TokenType.LEqual);
                }
                if (a.equals(">=")) {
                    return tok2(TokenType.GEqual);
                }
                if (a.equals(":=")) {
                    return tok2(TokenType.Assign);
                }
                if (a.equals("<>")) {
                    return tok2(TokenType.NEqual);
                }
                if (a.equals("!=")) {
                    return tok2(TokenType.NEqual);
                }
                if (a.equals("||")) {
                    return tok2(TokenType.StringAdd);
                }
                if (a.equals("--")) {
                    return tokx(TokenType.EOLineComment, scan_eol_comment(start));
                }
                if (a.equals("/*")) {

                    int x = scan_ml_comment(start);
                    if (x >= 0) {
                        return tokx(TokenType.MultiLineComment, x);
                    } else {
                        return tokx(TokenType.Schrott, len);
                    }

                }
                if (a.equals("**")) {
                    return tok2(TokenType.Power);
                }
                if (a.equals("..")) {
                    return tok2(TokenType.DotDot);
                }
                if (a.equals("<<")) {
                    return tok2(TokenType.LabelStart);
                }
                if (a.equals(">>")) {
                    return tok2(TokenType.LabelEnd);
                }
                if (a.equals("q'")) {
                    return tokx(TokenType.QString, qString(start));
                }
                if (get(start) == '.' && Character.isDigit(get(start + 1))) {
                    return tokx(TokenType.Float, scan_float_rest(start));
                } else {
                    return check1();
                }
            } else {
                // try to scan based on the first character
                return check1();
            }
        }
    }

    public static Token[] scanAll(String s) {
        int len = s.length();
        ArrayList<Token> res = new ArrayList();
        int pos = 0;
        int ipos = 0;
        int line = 1; // lines are one based
        int col = 0;
        int no = 0;
        while (true) {
            if (pos >= len) {
                res.add(new Token(TokenType.TheEnd, pos, line, col, "", "", no));
                break;
            }
            Scan1Class sc = new Scan1Class(s, pos, ipos, line, col, no);
            ScanRes sr = sc.scan1();
            res.add(sr.token);
            pos = sr.start;
            line = sr.line;
            col = sr.col;
            if (sr.token.isRelevant()) {
                ipos = sr.start;
            } else {
                //ipos = ipos;
            }
            no++;
        }
        return res.toArray(new Token[0]);
    }
}
