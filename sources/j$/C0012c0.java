package j$;

import j$.util.function.z;
import java.util.function.IntToLongFunction;

/* renamed from: j$.c0  reason: case insensitive filesystem */
public final /* synthetic */ class CLASSNAMEc0 implements z {
    final /* synthetic */ IntToLongFunction a;

    private /* synthetic */ CLASSNAMEc0(IntToLongFunction intToLongFunction) {
        this.a = intToLongFunction;
    }

    public static /* synthetic */ z a(IntToLongFunction intToLongFunction) {
        if (intToLongFunction == null) {
            return null;
        }
        return intToLongFunction instanceof CLASSNAMEd0 ? ((CLASSNAMEd0) intToLongFunction).a : new CLASSNAMEc0(intToLongFunction);
    }

    public /* synthetic */ long applyAsLong(int i) {
        return this.a.applyAsLong(i);
    }
}
