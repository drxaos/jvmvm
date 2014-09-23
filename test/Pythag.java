import net.sf.jauvm.interpretable;
import net.sf.jauvm.util.Ambivalence;

public class Pythag {
    public static final int N = 25;

    public static @interpretable void main(String... args) {
        final Ambivalence<Integer> amb = new Ambivalence<Integer>();

        final Runnable runnable = new Runnable() {
            public @interpretable void run() {
                int h = amb.choose(range(1, N));
                int a = amb.choose(range(1, h));
                int b = amb.choose(range(a, h));

                amb.require(sqr(h) == sqr(a) + sqr(b));

                System.out.printf("%2d^2 + %2d^2 = %2d^2\n", a, b, h);
            }
        };

        amb.cover(runnable);
    }

    private static Integer[] range(Integer min, Integer max) {
        Integer[] r = new Integer[max - min];
        for (int i = 0; i < r.length; i++) r[i] = min + i;
        return r;
    }

    private static int sqr(int i) {
        return i * i;
    }
}
