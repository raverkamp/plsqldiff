package spinat.plsqldiff.compare;

import java.util.List;

public class CompareResult {
    public final int distance;
    public final List lines1;
    public final List lines2;

    public CompareResult(int distance, List lines1, List lines2) {
        this.distance = distance;
        this.lines1 = lines1;
        this.lines2 = lines2;
    }
    
}
