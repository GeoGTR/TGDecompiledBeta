package j$.util;

import j$.util.function.C;
import j$.util.function.Consumer;
import j$.util.function.q;
import j$.util.function.w;
import java.util.Iterator;

public interface t<T, T_CONS> extends Iterator<T>, Iterator {

    public interface a extends t<Double, q> {
        void e(q qVar);

        void forEachRemaining(Consumer consumer);

        Double next();

        double nextDouble();
    }

    public interface b extends t<Integer, w> {
        void c(w wVar);

        void forEachRemaining(Consumer consumer);

        Integer next();

        int nextInt();
    }

    public interface c extends t<Long, C> {
        void d(C c);

        void forEachRemaining(Consumer consumer);

        Long next();

        long nextLong();
    }

    void forEachRemaining(Object obj);
}
