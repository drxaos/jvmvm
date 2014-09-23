import net.sf.jauvm.interpretable;
import net.sf.jauvm.util.Generator;

public class Fib {
    public static final int N = 10000;

    public static @interpretable void main(String... args) {
        final Generator<Integer> fib = new Generator<Integer>() {
            protected @interpretable void run(Object... args) {
                int n = (Integer) args[0];
                int x = 0, y = 1, z;
                while (true) {
                    z = x + y;
                    y = x;
                    x = z;
                    if (z > n) return;
                    else yield(z);
                }
            }
        };

        for (Integer i : fib.generate(N)) System.out.println(i);
    }
}
