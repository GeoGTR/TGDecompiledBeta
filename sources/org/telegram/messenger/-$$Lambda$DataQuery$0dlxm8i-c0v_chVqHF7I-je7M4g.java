package org.telegram.messenger;

import java.util.ArrayList;
import org.telegram.tgnet.TLRPC.messages_Messages;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$DataQuery$0dlxm8i-c0v_chVqHF7I-je7M4g implements Runnable {
    private final /* synthetic */ DataQuery f$0;
    private final /* synthetic */ messages_Messages f$1;
    private final /* synthetic */ int f$2;
    private final /* synthetic */ long f$3;
    private final /* synthetic */ ArrayList f$4;
    private final /* synthetic */ int f$5;
    private final /* synthetic */ int f$6;
    private final /* synthetic */ boolean f$7;

    public /* synthetic */ -$$Lambda$DataQuery$0dlxm8i-c0v_chVqHF7I-je7M4g(DataQuery dataQuery, messages_Messages messages_messages, int i, long j, ArrayList arrayList, int i2, int i3, boolean z) {
        this.f$0 = dataQuery;
        this.f$1 = messages_messages;
        this.f$2 = i;
        this.f$3 = j;
        this.f$4 = arrayList;
        this.f$5 = i2;
        this.f$6 = i3;
        this.f$7 = z;
    }

    public final void run() {
        this.f$0.lambda$processLoadedMedia$59$DataQuery(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6, this.f$7);
    }
}
