package org.telegram.ui;

import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.TL_error;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$ChannelAdminLogActivity$nOrNBbemB40UwQEfR8VXtwD17_w implements Runnable {
    private final /* synthetic */ ChannelAdminLogActivity f$0;
    private final /* synthetic */ TL_error f$1;
    private final /* synthetic */ TLObject f$2;

    public /* synthetic */ -$$Lambda$ChannelAdminLogActivity$nOrNBbemB40UwQEfR8VXtwD17_w(ChannelAdminLogActivity channelAdminLogActivity, TL_error tL_error, TLObject tLObject) {
        this.f$0 = channelAdminLogActivity;
        this.f$1 = tL_error;
        this.f$2 = tLObject;
    }

    public final void run() {
        this.f$0.lambda$null$12$ChannelAdminLogActivity(this.f$1, this.f$2);
    }
}
