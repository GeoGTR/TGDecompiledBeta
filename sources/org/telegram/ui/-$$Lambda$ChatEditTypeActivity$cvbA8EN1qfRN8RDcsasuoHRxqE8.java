package org.telegram.ui;

import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.TL_error;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$ChatEditTypeActivity$cvbA8EN1qfRN8RDcsasuoHRxqE8 implements RequestDelegate {
    private final /* synthetic */ ChatEditTypeActivity f$0;

    public /* synthetic */ -$$Lambda$ChatEditTypeActivity$cvbA8EN1qfRN8RDcsasuoHRxqE8(ChatEditTypeActivity chatEditTypeActivity) {
        this.f$0 = chatEditTypeActivity;
    }

    public final void run(TLObject tLObject, TL_error tL_error) {
        this.f$0.lambda$onFragmentCreate$2$ChatEditTypeActivity(tLObject, tL_error);
    }
}
