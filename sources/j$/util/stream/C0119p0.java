package j$.util.stream;

import j$.util.function.C;
import j$.util.function.CLASSNAMEe;
import j$.util.function.CLASSNAMEh;
import j$.util.function.Consumer;
import j$.util.k;
import j$.util.stream.A2;
import j$.util.stream.S2;

/* renamed from: j$.util.stream.p0  reason: case insensitive filesystem */
public final /* synthetic */ class CLASSNAMEp0 implements A2.g {
    public final /* synthetic */ S2.d a;

    public /* synthetic */ CLASSNAMEp0(S2.d dVar) {
        this.a = dVar;
    }

    public /* synthetic */ void accept(double d) {
        k.c(this);
        throw null;
    }

    public /* synthetic */ void accept(int i) {
        k.a(this);
        throw null;
    }

    public final void accept(long j) {
        this.a.accept(j);
    }

    /* renamed from: b */
    public /* synthetic */ void accept(Long l) {
        Q1.c(this, l);
    }

    public Consumer f(Consumer consumer) {
        consumer.getClass();
        return new CLASSNAMEe(this, consumer);
    }

    public C g(C c) {
        c.getClass();
        return new CLASSNAMEh(this, c);
    }

    public void m() {
    }

    public void n(long j) {
    }

    public /* synthetic */ boolean p() {
        return false;
    }
}
