package org.telegram.messenger;

import android.util.LongSparseArray;
import android.util.SparseArray;
import java.util.ArrayList;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$MessagesController$aIzxVN1fNX7ISomIdQom833zn_c implements Runnable {
    private final /* synthetic */ MessagesController f$0;
    private final /* synthetic */ int f$1;
    private final /* synthetic */ ArrayList f$2;
    private final /* synthetic */ LongSparseArray f$3;
    private final /* synthetic */ LongSparseArray f$4;
    private final /* synthetic */ LongSparseArray f$5;
    private final /* synthetic */ boolean f$6;
    private final /* synthetic */ ArrayList f$7;
    private final /* synthetic */ ArrayList f$8;
    private final /* synthetic */ SparseArray f$9;

    public /* synthetic */ -$$Lambda$MessagesController$aIzxVN1fNX7ISomIdQom833zn_c(MessagesController messagesController, int i, ArrayList arrayList, LongSparseArray longSparseArray, LongSparseArray longSparseArray2, LongSparseArray longSparseArray3, boolean z, ArrayList arrayList2, ArrayList arrayList3, SparseArray sparseArray) {
        this.f$0 = messagesController;
        this.f$1 = i;
        this.f$2 = arrayList;
        this.f$3 = longSparseArray;
        this.f$4 = longSparseArray2;
        this.f$5 = longSparseArray3;
        this.f$6 = z;
        this.f$7 = arrayList2;
        this.f$8 = arrayList3;
        this.f$9 = sparseArray;
    }

    public final void run() {
        this.f$0.lambda$processUpdateArray$250$MessagesController(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6, this.f$7, this.f$8, this.f$9);
    }
}
