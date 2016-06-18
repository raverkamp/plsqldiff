package spinat.plsqldiff.hirschberg;

public interface Matcher {
    int match(Object o1,Object o2);
    int ins1(Object o2);
    int ins2(Object o1);
}
