package org.telegram.messenger;

import j$.util.Comparator;
import j$.util.function.Function;
import j$.util.function.ToDoubleFunction;
import j$.util.function.ToIntFunction;
import j$.util.function.ToLongFunction;
import java.util.Comparator;
import org.telegram.messenger.FileLoadOperation;

/* renamed from: org.telegram.messenger.-$$Lambda$FileLoadOperation$2mjoFen-MksdDHICaACSx8uqV1c  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$FileLoadOperation$2mjoFenMksdDHICaACSx8uqV1c implements Comparator, j$.util.Comparator {
    public static final /* synthetic */ $$Lambda$FileLoadOperation$2mjoFenMksdDHICaACSx8uqV1c INSTANCE = new $$Lambda$FileLoadOperation$2mjoFenMksdDHICaACSx8uqV1c();

    private /* synthetic */ $$Lambda$FileLoadOperation$2mjoFenMksdDHICaACSx8uqV1c() {
    }

    public final int compare(Object obj, Object obj2) {
        return FileLoadOperation.lambda$removePart$0((FileLoadOperation.Range) obj, (FileLoadOperation.Range) obj2);
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
