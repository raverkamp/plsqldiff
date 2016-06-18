package spinat.plsqldiff.compare;

import spinat.plsqldiff.scanner.Token;

public class DisplayedToken {
    public final Token token;
    public final boolean highlight; //means the token is msimatched

    public DisplayedToken(Token t, boolean hot) {
        this.token = t;
        this.highlight = hot;
    }
}
