package spinat.plsqldiff.compare;

import spinat.plsqldiff.scanner.Token;

public class MatchedTokens {
    public final Token left;
    public final Token right;

    public MatchedTokens(Token left, Token right) {
        this.left = left;
        this.right = right;
    }
    
}
