package org.telegram.p005ui;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.TL_error;

/* renamed from: org.telegram.ui.ChatActivity$$Lambda$58 */
final /* synthetic */ class ChatActivity$$Lambda$58 implements RequestDelegate {
    static final RequestDelegate $instance = new ChatActivity$$Lambda$58();

    private ChatActivity$$Lambda$58() {
    }

    public void run(TLObject tLObject, TL_error tL_error) {
        AndroidUtilities.runOnUIThread(new ChatActivity$$Lambda$74(tLObject));
    }
}
