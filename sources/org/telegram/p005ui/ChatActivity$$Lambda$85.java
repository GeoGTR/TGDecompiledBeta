package org.telegram.p005ui;

import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.TL_error;
import org.telegram.tgnet.TLRPC.TL_messages_getWebPagePreview;

/* renamed from: org.telegram.ui.ChatActivity$$Lambda$85 */
final /* synthetic */ class ChatActivity$$Lambda$85 implements Runnable {
    private final ChatActivity arg$1;
    private final TL_error arg$2;
    private final TLObject arg$3;
    private final TL_messages_getWebPagePreview arg$4;

    ChatActivity$$Lambda$85(ChatActivity chatActivity, TL_error tL_error, TLObject tLObject, TL_messages_getWebPagePreview tL_messages_getWebPagePreview) {
        this.arg$1 = chatActivity;
        this.arg$2 = tL_error;
        this.arg$3 = tLObject;
        this.arg$4 = tL_messages_getWebPagePreview;
    }

    public void run() {
        this.arg$1.lambda$null$45$ChatActivity(this.arg$2, this.arg$3, this.arg$4);
    }
}
