package org.telegram.ui;

import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.TL_error;

final /* synthetic */ class ChatUsersActivity$$Lambda$11 implements Runnable {
    private final ChatUsersActivity arg$1;
    private final TL_error arg$2;
    private final TLObject arg$3;

    ChatUsersActivity$$Lambda$11(ChatUsersActivity chatUsersActivity, TL_error tL_error, TLObject tLObject) {
        this.arg$1 = chatUsersActivity;
        this.arg$2 = tL_error;
        this.arg$3 = tLObject;
    }

    public void run() {
        this.arg$1.lambda$null$21$ChatUsersActivity(this.arg$2, this.arg$3);
    }
}
