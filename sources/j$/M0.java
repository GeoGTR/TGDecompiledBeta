package j$;

import j$.util.function.BiConsumer;
import j$.util.function.Function;
import j$.util.function.J;
import j$.util.function.n;
import j$.util.stream.CLASSNAMEm1;
import java.util.Set;
import java.util.stream.Collector;

public final /* synthetic */ class M0 implements CLASSNAMEm1 {
    final /* synthetic */ Collector a;

    private /* synthetic */ M0(Collector collector) {
        this.a = collector;
    }

    public static /* synthetic */ CLASSNAMEm1 a(Collector collector) {
        if (collector == null) {
            return null;
        }
        return collector instanceof N0 ? ((N0) collector).a : new M0(collector);
    }

    public /* synthetic */ BiConsumer accumulator() {
        return CLASSNAMEt.b(this.a.accumulator());
    }

    public /* synthetic */ Set characteristics() {
        return this.a.characteristics();
    }

    public /* synthetic */ n combiner() {
        return CLASSNAMEx.b(this.a.combiner());
    }

    public /* synthetic */ Function finisher() {
        return P.c(this.a.finisher());
    }

    public /* synthetic */ J supplier() {
        return C0.a(this.a.supplier());
    }
}
