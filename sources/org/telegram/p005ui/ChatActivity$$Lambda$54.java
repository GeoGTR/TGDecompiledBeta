package org.telegram.p005ui;

import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.TL_error;

/* renamed from: org.telegram.ui.ChatActivity$$Lambda$54 */
final /* synthetic */ class ChatActivity$$Lambda$54 implements RequestDelegate {
    private final ChatActivity arg$1;

    ChatActivity$$Lambda$54(ChatActivity chatActivity) {
        this.arg$1 = chatActivity;
    }

    public void run(TLObject tLObject, TL_error tL_error) {
        this.arg$1.lambda$startEditingMessageObject$70$ChatActivity(tLObject, tL_error);
    }
}
