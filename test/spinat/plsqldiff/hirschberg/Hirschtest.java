/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package spinat.plsqldiff.hirschberg;

import spinat.plsqldiff.hirschberg.Matcher;
import spinat.plsqldiff.hirschberg.Hirschberg;
import spinat.plsqldiff.hirschberg.EqualMatcher;
import java.util.ArrayList;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author rav
 */
public class Hirschtest {

    public Hirschtest() {
    }
    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    // @Test
    // public void hello() {}

    public static void printMatchings(Object[] o1, Object[] o2, Hirschberg.Int2[] m) {
        System.out.println("-----------------------");
        for (int i = 0; i < m.length; i++) {
            System.out.print(("" + i + " " + m[i] + "        ").substring(0, 10) + ": ");
            if (m[i].f1 == -1) {
                System.out.print("          ");
            } else {
                String s = o1[m[i].f1].toString();
                String a = s + "          ";
                System.out.print(a.substring(0, 10));
            }
            System.out.print(" : ");
            if (m[i].f2 != -1) {
                String s = o2[m[i].f2].toString();
                String a = s + "          ";
                System.out.print(a.substring(0, 10));
            }
            System.out.println();
        }
    }

    static int computeMatchCost(Matcher m, Hirschberg.Int2[] s, Object[] x, Object[] y) {
        int res = 0;
        for (Hirschberg.Int2 i2 : s) {
            if (i2.f1 >= 0 && i2.f2 >= 0) {
                res = res + m.match(x[i2.f1], y[i2.f2]);
            } else if (i2.f1 < 0) {
                res = res + m.ins1(y[i2.f2]);
            } else if (i2.f2 < 0) {
                res = res + m.ins2(x[i2.f1]);
            } else {
                throw new RuntimeException("aua");
            }
        }
        return res;
    }

    static void hirschberg(Object[] x, Object[] y) {
        Matcher m = new EqualMatcher(2, 1);
        Hirschberg hb2 = new Hirschberg(m);
        Hirschberg.HirschbergResult res = hb2.hirschberg(x, y);
        int[] score = hb2.nwScore(x, y);
        int score1 = score[score.length - 1];
        printMatchings(x, y, res.match);
        int score2 = computeMatchCost(m, res.match, x, y);
        if (score1 != score2) {
            throw new RuntimeException("auaaaa");
        }
    }

    static void hirschberg(Matcher m, Object[] x, Object[] y) {
        Hirschberg hb2 = new Hirschberg(m);
        Hirschberg.HirschbergResult res = hb2.hirschberg(x, y);
        int xi=-1;
        int yi=-1;
        for(Hirschberg.Int2 r : res.match) {
            if(r.f1>=0) {
                if (xi!=r.f1-1) {
                    throw new RuntimeException("aua");
                }
                xi=r.f1;
            }
            if(r.f2>=0) {
                if (yi!=r.f2-1) {
                    throw new RuntimeException("aua");
                }
                yi=r.f2;
            }
        }
        if (xi!=x.length-1) {
             throw new RuntimeException("aua");
        }
        if (yi!=y.length-1) {
             throw new RuntimeException("aua");
        }
        int[] score = hb2.nwScore(x, y);
        int score1 = score[score.length - 1];
        printMatchings(x, y, res.match);
        int score2 = computeMatchCost(m, res.match, x, y);
        if (score1 != score2) {
            throw new RuntimeException("auaaaa");
        }
    }

    @Test
    public void test1() {
        hirschberg(new Object[]{}, new Object[]{});
        hirschberg(new Object[]{1}, new Object[]{});
        hirschberg(new Object[]{1}, new Object[]{1});
        hirschberg(new Object[]{1}, new Object[]{2});
        hirschberg(new Object[]{1}, new Object[]{7});
        hirschberg(new Object[]{1, 2}, new Object[]{7, 2});

        hirschberg(new Object[]{1, 2}, new Object[]{1, 7});

        hirschberg(new Object[]{1, 2, 3, 4}, new Object[]{1, 2, 3, 4});
        hirschberg(new Object[]{9, 2, 3, 4}, new Object[]{1, 2, 3, 4});
        hirschberg(new Object[]{1, 2, 3, 4}, new Object[]{1, 2, 5, 6, 7, 3, 4});
        hirschberg(new Object[]{1, 2, 3, 4, 5, 6, 7}, new Object[]{1, 2, 3, 99, 5, 6, 7});
        hirschberg(new Object[]{1, 2, 3, 4, 5, 6, 7}, new Object[]{1, 2, 3, 4, 99, 5, 6, 7});
        hirschberg(new Object[]{1, 2, 3, 4, 5, 6, 7}, new Object[]{1, 2, 3, 99, 4, 5, 6, 7});
        hirschberg(new Object[]{99, 1, 2, 3, 4, 5, 6, 7}, new Object[]{1, 2, 3, 4, 5, 6, 7});
        hirschberg(new Object[]{99, 2, 3, 4, 5, 6, 99}, new Object[]{1, 2, 3, 4, 5, 6, 7});

        hirschberg(new Object[]{1, 2, 3, 4, 5, 6, 7}, new Object[]{4, 5, 6, 7, 8, 9, 10});
        hirschberg(new Object[]{4, -5, 6}, new Object[]{4, 5, 6});
        hirschberg(new Object[]{1, 2, 3, 4, -5, 6, 7}, new Object[]{4, 5, 6, 7, 8, 9, 10});
        hirschberg(new Object[]{1, 2, 3, 4, 5, 6, 7, 8, 9}, new Object[]{1, 2, 3, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 5, 6, 7, 8, 9});

    }

    @Test
    public void bigtest() {
        int n = 100000;
        Object[] x = new Object[n];
        Object[] y = new Object[n];
        for (int i = 0; i < n; i++) {
            x[i] = i;
            if (Math.random() < 0.9) {
                y[i] = i;
            } else {
                y[i] = i + 100000;
            }
        }
        ArrayList<Integer> a1 = new ArrayList<Integer>();
        ArrayList<Integer> a2 = new ArrayList<Integer>();
        Hirschberg hb2 = new Hirschberg(new EqualMatcher(2, 1));
        hb2.hirschberg(x, 0, y, 0, a1, a2);
        System.out.println("++++-----------------");
        // for(int i=0;i<a1.size();i++) {
        //    System.out.println("" + a1.get(i)+ "    " + a2.get(i));
        //}   
    }

    @Test
    public void randomTest() {
        for (int j = 0; j < 100; j++) {
            for (Matcher m : new Matcher[]{new EqualMatcher(1, 1), new EqualMatcher(3, 1)}) {

                ArrayList a = new ArrayList();
                ArrayList b = new ArrayList();
                for (int i = 0; i < 100; i++) {

                    a.add(Math.round(Math.random()));
                    if (Math.random() <= 0.9) {
                        b.add(Math.round(Math.random()));
                    }
                }
                Object[] x = a.toArray();
                Object[] y = b.toArray();
                hirschberg(m, x, y);
            }
        }
    }
}