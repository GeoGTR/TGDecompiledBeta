package org.telegram.ui;

import j$.util.Comparator;
import j$.util.function.Function;
import j$.util.function.ToDoubleFunction;
import j$.util.function.ToIntFunction;
import j$.util.function.ToLongFunction;
import java.util.Comparator;

/* renamed from: org.telegram.ui.-$$Lambda$ChatActivity$0Nhh04ZE-G9bqrArZKNGAFj6Ppk  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$ChatActivity$0Nhh04ZEG9bqrArZKNGAFj6Ppk implements Comparator, j$.util.Comparator {
    public static final /* synthetic */ $$Lambda$ChatActivity$0Nhh04ZEG9bqrArZKNGAFj6Ppk INSTANCE = new $$Lambda$ChatActivity$0Nhh04ZEG9bqrArZKNGAFj6Ppk();

    private /* synthetic */ $$Lambda$ChatActivity$0Nhh04ZEG9bqrArZKNGAFj6Ppk() {
    }

    public final int compare(Object obj, Object obj2) {
        return ((Integer) obj2).compareTo((Integer) obj);
    }

    public /* synthetic */ Comparator reversed() {
        return Comparator.CC.$default$reversed(this);
    }

    public /* synthetic */ java.util.Comparator thenComparing(Function function) {
        return Comparator.CC.$default$thenComparing((java.util.Comparator) this, function);
    }

    public /* synthetic */ java.util.Comparator thenComparing(Function function, java.util.Comparator comparator) {
        return Comparator.CC.$default$thenComparing(this, function, comparator);
    }

    public /* synthetic */ java.util.Comparator thenComparing(java.util.Comparator comparator) {
        return Comparator.CC.$default$thenComparing((java.util.Comparator) this, comparator);
    }

    public /* synthetic */ java.util.Comparator thenComparingDouble(ToDoubleFunction toDoubleFunction) {
        return Comparator.CC.$default$thenComparingDouble(this, toDoubleFunction);
    }

    public /* synthetic */ java.util.Comparator thenComparingInt(ToIntFunction toIntFunction) {
        return Comparator.CC.$default$thenComparingInt(this, toIntFunction);
    }

    public /* synthetic */ java.util.Comparator thenComparingLong(ToLongFunction toLongFunction) {
        return Comparator.CC.$default$thenComparingLong(this, toLongFunction);
    }
}
