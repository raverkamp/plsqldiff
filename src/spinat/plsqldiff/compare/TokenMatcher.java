package spinat.plsqldiff.compare;

import spinat.plsqldiff.hirschberg.Matcher;
import spinat.plsqldiff.scanner.Token;
import spinat.plsqldiff.scanner.TokenType;

public final class TokenMatcher implements Matcher {

    // this class depends on the fact that the scanner
    // interns the strings value and ivalue! Thus we can use == to comapre strings
    @Override 
    public int match(Object o1, Object o2) {
        Token t1 = (Token) o1;
        Token t2 = (Token) o2;
        if ((t1.tokenType == TokenType.Ident || t1.tokenType == TokenType.QIdent) &&
                (t2.tokenType == TokenType.Ident || t2.tokenType == TokenType.QIdent)) {
            // svalue is interned !
            if (t1.svalue == t2.svalue) {
                return 0;
            } else {
                return 1;
            }
        }
        if ((t1.tokenType == TokenType.SString || t1.tokenType == TokenType.QString) &&
                (t2.tokenType == TokenType.SString || t2.tokenType == TokenType.QString)) {
            // svalue is interned !
            if (t1.svalue == t2.svalue) {
                return 0;
            } else {
                return 1;
            }
        }
        if (t1.tokenType == t2.tokenType && t1.svalue == t2.svalue) {
            // svalue is interned !
            return 0;
        } else {
            return 1;
        }
    }

    @Override
    public int ins1(Object o2) {
        return 1;
    }

    @Override
    public int ins2(Object o1) {
        return 1;
    }
}
