package org.telegram.ui;

import j$.util.Comparator;
import j$.util.function.Function;
import j$.util.function.ToDoubleFunction;
import j$.util.function.ToIntFunction;
import j$.util.function.ToLongFunction;
import java.util.Comparator;
import org.telegram.ui.ActionBar.Theme;

/* renamed from: org.telegram.ui.-$$Lambda$ThemeActivity$UM18aTc2VkvPUFpY4hL9yY8ZWDA  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$ThemeActivity$UM18aTc2VkvPUFpY4hL9yY8ZWDA implements Comparator, j$.util.Comparator {
    public static final /* synthetic */ $$Lambda$ThemeActivity$UM18aTc2VkvPUFpY4hL9yY8ZWDA INSTANCE = new $$Lambda$ThemeActivity$UM18aTc2VkvPUFpY4hL9yY8ZWDA();

    private /* synthetic */ $$Lambda$ThemeActivity$UM18aTc2VkvPUFpY4hL9yY8ZWDA() {
    }

    public final int compare(Object obj, Object obj2) {
        return C$r8$backportedMethods$utility$Integer$2$compare.compare(((Theme.ThemeInfo) obj).sortIndex, ((Theme.ThemeInfo) obj2).sortIndex);
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
