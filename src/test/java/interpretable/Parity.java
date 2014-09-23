package interpretable;

import net.sf.jauvm.interpretable;

public class Parity {
    public static final int N = 100000;

    public static @interpretable void main(String... args) {
        System.out.printf("isEven(%d) = %b\n", N, isEven(N));
        System.out.printf("isOdd(%d) = %b\n", N, isOdd(N));
    }

    public static @interpretable boolean isEven(int n) {
        if (n == 0) return true;
        return isOdd(n - 1);
    }

    public static @interpretable boolean isOdd(int n) {
        if (n == 0) return false;
        return isEven(n - 1);
    }
}
