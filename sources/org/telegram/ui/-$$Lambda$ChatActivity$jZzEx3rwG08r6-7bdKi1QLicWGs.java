package org.telegram.ui;

import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.TL_error;
import org.telegram.tgnet.TLRPC.TL_messages_getWebPagePreview;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$ChatActivity$jZzEx3rwG08r6-7bdKi1QLicWGs implements RequestDelegate {
    private final /* synthetic */ ChatActivity f$0;
    private final /* synthetic */ TL_messages_getWebPagePreview f$1;

    public /* synthetic */ -$$Lambda$ChatActivity$jZzEx3rwG08r6-7bdKi1QLicWGs(ChatActivity chatActivity, TL_messages_getWebPagePreview tL_messages_getWebPagePreview) {
        this.f$0 = chatActivity;
        this.f$1 = tL_messages_getWebPagePreview;
    }

    public final void run(TLObject tLObject, TL_error tL_error) {
        this.f$0.lambda$null$49$ChatActivity(this.f$1, tLObject, tL_error);
    }
}
