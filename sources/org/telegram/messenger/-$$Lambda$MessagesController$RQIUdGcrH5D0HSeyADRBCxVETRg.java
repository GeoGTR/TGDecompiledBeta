package org.telegram.messenger;

import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.TL_error;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$MessagesController$RQIUdGcrH5D0HSeyADRBCxVETRg implements RequestDelegate {
    private final /* synthetic */ MessagesController f$0;
    private final /* synthetic */ String f$1;

    public /* synthetic */ -$$Lambda$MessagesController$RQIUdGcrH5D0HSeyADRBCxVETRg(MessagesController messagesController, String str) {
        this.f$0 = messagesController;
        this.f$1 = str;
    }

    public final void run(TLObject tLObject, TL_error tL_error) {
        this.f$0.lambda$registerForPush$189$MessagesController(this.f$1, tLObject, tL_error);
    }
}
