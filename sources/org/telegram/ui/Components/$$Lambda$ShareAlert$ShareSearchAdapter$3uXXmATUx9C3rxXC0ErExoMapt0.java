package org.telegram.ui.Components;

import j$.util.Comparator;
import j$.util.function.Function;
import j$.util.function.ToDoubleFunction;
import j$.util.function.ToIntFunction;
import j$.util.function.ToLongFunction;
import java.util.Comparator;
import org.telegram.ui.Components.ShareAlert;

/* renamed from: org.telegram.ui.Components.-$$Lambda$ShareAlert$ShareSearchAdapter$3uXXmATUx9C3rxXC0ErExoMapt0  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$ShareAlert$ShareSearchAdapter$3uXXmATUx9C3rxXC0ErExoMapt0 implements Comparator, j$.util.Comparator {
    public static final /* synthetic */ $$Lambda$ShareAlert$ShareSearchAdapter$3uXXmATUx9C3rxXC0ErExoMapt0 INSTANCE = new $$Lambda$ShareAlert$ShareSearchAdapter$3uXXmATUx9C3rxXC0ErExoMapt0();

    private /* synthetic */ $$Lambda$ShareAlert$ShareSearchAdapter$3uXXmATUx9C3rxXC0ErExoMapt0() {
    }

    public final int compare(Object obj, Object obj2) {
        return ShareAlert.ShareSearchAdapter.lambda$null$0((ShareAlert.ShareSearchAdapter.DialogSearchResult) obj, (ShareAlert.ShareSearchAdapter.DialogSearchResult) obj2);
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
