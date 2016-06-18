package spinat.plsqldiff.compare;

import spinat.plsqldiff.hirschberg.Hirschberg;
import spinat.plsqldiff.hirschberg.Matcher;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import java.util.ArrayList;

import spinat.plsqldiff.scanner.Token;
import spinat.plsqldiff.scanner.TokenType;

public class Comparer {

    static final Matcher tokenMatcher = new TokenMatcher();

    static String readFile(File f) throws IOException {
        StringBuilder b = new StringBuilder();
        FileReader r = new FileReader(f);
        while (true) {
            int x = r.read();
            if (x < 0) {
                break;
            }
            b.append((char) x);
        }
        return b.toString();
    }

    static Token[] relevantTokens(Token[] tokens) {
        ArrayList<Token> at1 = new ArrayList();
        for (Token t : tokens) {
            if (!t.ignore()) {
                at1.add(t);
            }
        }
        Token[] rt1 = at1.toArray(new Token[0]);
        return rt1;
    }

    public static CompareResult compare(File file1, File file2) throws IOException {
        String src1 = readFile(file1);
        String src2 = readFile(file2);
        return compare(src1, src2);
    }

    public static CompareResult compare(String src1, String src2) {
        Token[] t1 = spinat.plsqldiff.scanner.Scanner.scanAll(src1);
        Token[] relevantTokens1 = relevantTokens(t1);

        Token[] t2 = spinat.plsqldiff.scanner.Scanner.scanAll(src2);
        Token[] relevantTokens2 = relevantTokens(t2);

        spinat.plsqldiff.hirschberg.Hirschberg hb2 = new Hirschberg(tokenMatcher);
        Hirschberg.HirschbergResult r = hb2.hirschberg(relevantTokens1, relevantTokens2);
        Hirschberg.Int2[] res = r.match;
        // res contains pairs of integers, these integers are positions
        // into the input arrays rt1 and rt2
        // an entry in res contains matched positions
        //  or not a match if one of the entries is -1, meaning that there is no match
        ArrayList<MatchedTokens> res2 = new ArrayList<>();
        for (Hirschberg.Int2 i2 : res) {
            Token token1 = null;
            if (i2.f1 >= 0) {
                token1 = relevantTokens1[i2.f1];
            }
            Token token2 = null;
            if (i2.f2 >= 0) {
                token2 = relevantTokens2[i2.f2];
            }
            MatchedTokens x = new MatchedTokens(token1, token2);
            res2.add(x);
        }
        // res 2 contains now the matched tokens
        // these token have now to be placed into lines
        Tuple2<ArrayList<ArrayList<DisplayedToken>>> lines = placeIntoLines(t1, t2, res2);

        return new CompareResult(r.distance, lines.o1, lines.o2);


    }

    // at line pos add token t to lines,
    // if necessary pad the arraylist lines with empty entries
    static void add(ArrayList<ArrayList<DisplayedToken>> lines, int pos, Token t, boolean hot) {
        int s = lines.size();
        for (int i = s; i <= pos; i++) {
            lines.add(new ArrayList<DisplayedToken>());
        }
        lines.get(pos).add(new DisplayedToken(t, hot));
    }

    static int spillTillLine(int tp, int maxLine, ArrayList target_lines, Token[] tokens, int baseline) {
        int i = tp;
        while (true) {
            if (tokens[i].line < maxLine) {
                if (tokens[i].tokenType != TokenType.Newline) {
                    add(target_lines, tokens[i].getLineno() + baseline, tokens[i], false);
                }
                i++;
            } else {
                return i;
            }
        }
    }

    static void spillTillTokenPos(int tp, int maxTokenPos, ArrayList target_lines, Token[] tokens, int baseline) {
        int i = tp;
        while (true) {
            if (i == maxTokenPos) {
                break;
            }
            if (i >= tokens.length) {
                //System.out.println("xx" + tp + "/" + tokens.length + "/" + maxTokenPos);
            }
            if (tokens[i].tokenType == TokenType.Newline) {
                i++;
                continue; //System.out.println("treffer");      
            }

            //spill t1s[i] mit pos (t1s[i].line -oline1) + line
            add(target_lines, tokens[i].getLineno() + baseline, tokens[i], false);
            i++;
        }

    }

    // place the tokens into lines
    // ensure that matched (relevant!) tokens are in the same line
    //  if two tokens in one file/side are not on the same line originally
    //  they are not placed in the same line
    public static Tuple2<ArrayList<ArrayList<DisplayedToken>>> placeIntoLines(Token[] left_tokens, Token[] right_tokens, ArrayList<MatchedTokens> res) {
        ArrayList left_lines = new ArrayList();
        ArrayList right_lines = new ArrayList();
        int line = 0;
        int oline_left = 1;
        int oline_right = 1;
        int tp_left = 0;// token pos
        int tp_right = 0; // token pos
        for (MatchedTokens mt : res) {
            // first spill every token till the current one i.e. i2.f1

            if (mt.left != null) {
                tp_left = spillTillLine(tp_left, mt.left.line, left_lines, left_tokens, line - oline_left);
            }
            if (mt.right != null) {
                tp_right = spillTillLine(tp_right, mt.right.line, right_lines, right_tokens, line - oline_right);
            }

            if (mt.left != null && mt.right != null) {
                Token t1 = mt.left;
                Token t2 = mt.right;
                int l1 = (t1.getLineno() - oline_left) + line;
                int l2 = (t2.getLineno() - oline_right) + line;
                oline_left = t1.getLineno();
                oline_right = t2.getLineno();
                line = Math.max(l1, l2);

                spillTillTokenPos(tp_left, t1.no, left_lines, left_tokens, line - oline_left);
                spillTillTokenPos(tp_right, t2.no, right_lines, right_tokens, line - oline_right);

                // add if there is a match or not
                boolean hot = tokenMatcher.match(t1, t2) > 0;

                add(left_lines, line, t1, hot);
                add(right_lines, line, t2, hot);
                tp_left = t1.no + 1;
                tp_right = t2.no + 1;

            } else if (mt.left != null && mt.right == null) {
                Token t1 = mt.left;
                int l1 = (t1.getLineno() - oline_left) + line;
                spillTillTokenPos(tp_left, t1.no, left_lines, left_tokens, line - oline_left);
                add(left_lines, l1, t1, true);

                tp_left = t1.no + 1;

            } else if (mt.left == null && mt.right != null) {
                Token t2 = mt.right;
                int l2 = (t2.getLineno() - oline_right) + line;
                spillTillTokenPos(tp_right, t2.no, right_lines, right_tokens, line - oline_right);
                tp_right = t2.no + 1;
                add(right_lines, l2, t2, true);
            } else {
                throw new Error("BUG");
            }

        }
        return new Tuple2(left_lines, right_lines);
    }
}
