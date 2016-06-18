package spinat.plsqldiff.compare;

import java.util.ArrayList;

public class ARow {

    public final ArrayList<DisplayedToken> leftTokens;
    public final ArrayList<DisplayedToken> rightTokens;
    public final int leftLineNo;
    public final int rightLineNo;

    public ARow(ArrayList<DisplayedToken> leftTokens,
            ArrayList<DisplayedToken> rightTokens,
            int leftLineNo,
            int rightLineNo) {
        this.leftTokens = leftTokens;
        this.rightTokens = rightTokens;
        this.leftLineNo = leftLineNo;
        this.rightLineNo = rightLineNo;
    }
}
