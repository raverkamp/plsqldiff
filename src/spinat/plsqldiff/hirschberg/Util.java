package spinat.plsqldiff.hirschberg;


public class Util {

    static Object[] sub(Object[] a, int start, int end) {
        Object[] res = new Object[end - start];
        System.arraycopy(a, start, res, 0, end - start);
        return res;
    }

    static Object[] rev(Object[] a) {
        Object[] res = new Object[a.length];
        for (int i = 0; i < a.length; i++) {
            res[i] = a[a.length - i - 1];
        }
        return res;
    }

    static void print(int[] x) {
        for (int i = 0; i < x.length; i++) {
            System.out.print("" + x[i] + " ");
        }
        System.out.println();
    }

    static void print(Object[] x) {
        for (Object x1 : x) {
            System.out.print("" + x1 + " ");
        }

        System.out.println();
    }
}
