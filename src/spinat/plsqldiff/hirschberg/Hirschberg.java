package spinat.plsqldiff.hirschberg;

import java.util.ArrayList;

public class Hirschberg {
     // class for macthing sequences
    // http://en.wikipedia.org/wiki/Hirschberg%27s_algorithm
    // this is independenzt of PL/SQL etc.
    // cost of edit operations is defined by a Matcher Object
    
    final Matcher matcher;
    final Matcher switchedMatcher;

    // matcher defines the cost for edit operations
    public Hirschberg(Matcher m) {
        this.matcher = m;
        // needed so that a part of the algorithm does not have to be 
        // written twice
        switchedMatcher = new Matcher() {

            @Override
            public int match(Object o1, Object o2) {
                return matcher.match(o2,o1);
            }

            @Override
            public int ins1(Object o1) {
                return matcher.ins2(o1);
            }

            @Override
            public int ins2(Object o1) {
                return matcher.ins1(o1);
            }
        };
        
    }

    /*
     function NWScore(X,Y)
     Score(0,0) = 0
     for j=1 to length(Y)
     Score(0,j) = Score(0,j-1) + Ins(Yj)
     for i=1 to length(X)
     Score(i,0) = Score(i-1,0) + Del(Xi)
     for j=1 to length(Y)
     scoreSub = Score(i-1,j-1) + Sub(Xi, Yj)
     scoreDel = Score(i-1,j) + Del(Xi)
     scoreIns = Score(i,j-1) + Ins(Yj)
     Score(i,j) = max(scoreSub, scoreDel, scoreIns)
     end
     end
     for j=0 to length(Y)
     LastLine(j) = Score(length(X),j)
     return LastLine
     */
    
    // res  is the result
    // res[i] = LEV DIST of x and y[0:i-1]
    // this is computed incrementally starting with
    //  x[0:0]
    int[] nwScore(Object[] x, Object[] y) {
        int[] lastscore = new int[y.length + 1];
        int[] cscore = new int[y.length + 1];
        int[] swap;
        lastscore[0] = 0;
        for (int j = 1; j < y.length + 1; j++) {
            lastscore[j] = lastscore[j - 1] + matcher.ins1(y[j - 1]);//weight ins (y[j-1])
        }
       
        // lastcore conatins the LV dist of x[0:i-1] against the prefixes of y
        //   lastscore[j] = LVD(x[0:i-1],y[0:j-1]
        for (int i = 1; i < x.length + 1; i++) {
            cscore[0] = lastscore[0] + 1; //weight del x[i]
            for (int j = 1; j < y.length + 1; j++) {
                int sub = lastscore[j - 1] + matcher.match(x[i - 1], y[j - 1]);  //+ sub(x[i-1],y[j-1])
                int del = lastscore[j] + matcher.ins2(x[i - 1]); //  1; // weight del xi-1
                int ins = cscore[j - 1] + matcher.ins1(y[j - 1]); //  1; //weight ins yj-1
                cscore[j] = Math.min(ins, Math.min(sub, del));
            }
            swap = lastscore;
            lastscore = cscore;
            cscore = swap;
        }
        return lastscore;
    }

    /*
     function Hirschberg(X,Y)
     Z = ""
     W = ""
     if length(X) == 0 or length(Y) == 0
     if length(X) == 0
     for i=1 to length(Y)
     Z = Z + '-'
     W = W + Yi
     end
     else if length(Y) == 0
     for i=1 to length(X)
     Z = Z + Xi
     W = W + '-'
     end
     end
     else if length(X) == 1 or length(Y) == 1
     (Z,W) = (Z,W) + NeedlemanWunsch(X,Y)
     else
     xlen = length(X)
     xmid = length(X)/2
     ylen = length(Y)
 
     ScoreL = NWScore(X1:xmid, Y)
     ScoreR = NWScore(rev(Xxmid+1:xlen), rev(Y))
     ymid = PartitionY(ScoreL, ScoreR)
 
     (Z,W) = (Z,W) + Hirschberg(X1:xmid, y1:ymid)
     (Z,W) = (Z,W) + Hirschberg(Xxmid+1:xlen, Yymid+1:ylen)
     end
     return (Z,W)
     */
    
    
    // compute the match for x and y 
    //   the matchings are added to the lists z and w
    //  not the objects are stored in z and w but the indexes
    //   -1 if there is no match
    //    that is the reason for the parameters xstart and ystart:
    //    x and y are sub arrays of the initial arrays, and xstart and ystart
    //    are their start in the inital arrays
    // the objects in x and y do not need to have the same type!!!!
    
    int hirschberg(Object[] x, int xstart,Object[] y,int ystart, ArrayList<Integer> z, ArrayList<Integer> w) {
       // matching is simple if one of the arrays has length 0
        if (x.length == 0) {
            int cost=0;
            for (int i = 0; i < y.length; i++) {
                z.add(-1);
                w.add(i+ystart);
                cost+=matcher.ins1(x[i]);
            }
            return cost;
        } else if (y.length == 0) {
            int cost=0;
            for (int i = 0; i < x.length; i++) {
                z.add(i+xstart);
                w.add(-1);
                cost+= matcher.ins2(x[i]);
            }
            return cost;
           // matching is simple if one of the arrays has length 1
        } else if (x.length == 1 || y.length == 1) {
            return needlemanWunsch(x,xstart, y,ystart,z, w);
        } else {
            // split x into two parts xl and xr and try to find the partition
            // of y into yl and yr such that
            //  LEVD(xl,yl) + LEVD(xr,yr) is minimal
            int xlen = x.length;
            int xmid = xlen / 2;
           // int ylen = y.length;
            int[] scorel = nwScore(Util.sub(x, 0, xmid), y);
            int[] scorer = nwScore(Util.rev(Util.sub(x, xmid, xlen)), Util.rev(y));

            //scorel[i] = score for  LEV DIST of x and y[0:(i-1)] sub(y,0,i)
            //scorer[i] = score for LEV DIST of rev(x) and rev(y)[0:(i-1)] 
            //                         which is sub(rev(y),0,i)
            //                         which is sub(y, y.length-i,y.length)
            //    
            int k = -1;
            int v =  Integer.MAX_VALUE; // scorel[0] + scorer[scorer.length - 1];
            for (int i = 0; i < scorel.length; i++) {
                if (scorel[i] + scorer[scorer.length-1 - i] < v) {
                    k = i;
                    v = scorel[i] + scorer[scorer.length-1 - i];
                }
            }
           
           int cost1 = hirschberg(Util.sub(x, 0, xmid),xstart, Util.sub(y, 0, k),ystart, z, w);
           int cost2 = hirschberg(Util.sub(x, xmid, x.length),xstart+xmid, Util.sub(y, k, y.length),ystart+k, z, w);
           // just to make sure
           if (cost1+cost2!= v) {
               throw new RuntimeException("BUG: costs do not match");
           }
           // return the cost
           return v;
        }
    }
    
    public static class Int2 {
        public final int f1;
        public final int f2;
        public Int2(int f1,int f2) {
            this.f1= f1;
            this.f2 = f2;
        }
        
        @Override
        public String toString() {
            return "("+f1+","+f2+")";
        }
    }
    
    
    public static class HirschbergResult {
        public final int distance;
        public final Int2[] match ;
        public HirschbergResult(int distance,Int2[] match) {
            this.distance = distance;
            this.match = match;
        }
    }
    
    public HirschbergResult hirschberg(Object[] x,Object[] y) {
      ArrayList<Integer> z = new ArrayList();
      ArrayList<Integer> w = new ArrayList();
      int distance = hirschberg(x, 0, y, 0, z, w);
      Int2[] res = new Int2[z.size()];
      for(int i=0;i<z.size();i++) {
          res[i]=new Int2(z.get(i),w.get(i));
      }
      return new HirschbergResult(distance,res);
    }
    
    
    // the best match is the one where 
    int needlemanWunsch(Object[] x, int xstart, Object[] y, int ystart, ArrayList<Integer> z, ArrayList<Integer> w) {
        int cost =0;
        final Matcher ma;
        if (x.length == 1) {
            ma=matcher;
        } else if (y.length == 1) {
            Object[] swapx = x;
            ArrayList<Integer> swapz = z;
            int swapstart = xstart;
            x = y;
            y = swapx;
            xstart = ystart;
            ystart = swapstart;
            z = w;
            w = swapz;
            ma = switchedMatcher;
        } else {
            throw new Error("BUG");
        }
        // was ist moeglich?
        // match mit einem Wert der am besten past oder einfuegen loeschen?
        // simple first match wins
        Object a = x[0];
        int inscost = ma.match(a, y[0]) - ma.ins1(y[0]);
        int inspos = 0;
        for (int i = 1; i < y.length; i++) {
            int c = ma.match(a, y[i]) - ma.ins1(y[i]);
            if (c < inscost) {
                inscost = c;
                inspos = i;
            }
        }
        if (inscost >= ma.ins2(a)) {
            z.add(xstart);
            w.add(-1);
            cost+= ma.ins2(x[0]);
            for (int i = 0; i < y.length; i++) {
                z.add(-1);
                w.add(i + ystart);
                cost+= ma.ins1(y[i]);
            }
        } else {
            for (int i = 0; i < y.length; i++) {
                if (i == inspos) {
                    z.add(xstart);
                    cost+= ma.match(x[0], y[i]);
                } else {
                    z.add(-1);
                    cost+= ma.ins1(y[i]);
                }
                w.add(i + ystart);
            }
        }
        return cost;
    }
}
