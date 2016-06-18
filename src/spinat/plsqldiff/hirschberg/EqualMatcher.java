package spinat.plsqldiff.hirschberg;

public class EqualMatcher implements Matcher{

    final int inscost;
    final int missmatchcost;
    public EqualMatcher(int missmatchcost,int inscost) {
        this.inscost = inscost;
        this.missmatchcost = missmatchcost;
    }
    
    @Override
    public int match(Object o1, Object o2) {
        if (o1!=null ) {
          return (o1.equals(o2)) ? 0 : missmatchcost;
        } else if (o2==null) {
            return 0;
        } else {
            return missmatchcost;
        }
    }

    @Override
    public int ins1(Object o2) {
        return inscost;
    }

    @Override
    public int ins2(Object o1) {
        return inscost;
    }
}
