package org.telegram.messenger;

import java.util.ArrayList;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$MediaDataController$LRfUPhzRo4MDxhhikU_xP9_zxaI implements Runnable {
    private final /* synthetic */ MediaDataController f$0;
    private final /* synthetic */ ArrayList f$1;
    private final /* synthetic */ ArrayList f$2;
    private final /* synthetic */ int f$3;
    private final /* synthetic */ int f$4;

    public /* synthetic */ -$$Lambda$MediaDataController$LRfUPhzRo4MDxhhikU_xP9_zxaI(MediaDataController mediaDataController, ArrayList arrayList, ArrayList arrayList2, int i, int i2) {
        this.f$0 = mediaDataController;
        this.f$1 = arrayList;
        this.f$2 = arrayList2;
        this.f$3 = i;
        this.f$4 = i2;
    }

    public final void run() {
        this.f$0.lambda$putFeaturedStickersToCache$25$MediaDataController(this.f$1, this.f$2, this.f$3, this.f$4);
    }
}
