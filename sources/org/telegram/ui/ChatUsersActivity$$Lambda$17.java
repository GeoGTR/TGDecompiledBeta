package org.telegram.ui;

import org.telegram.tgnet.TLRPC.Updates;

final /* synthetic */ class ChatUsersActivity$$Lambda$17 implements Runnable {
    private final ChatUsersActivity arg$1;
    private final Updates arg$2;

    ChatUsersActivity$$Lambda$17(ChatUsersActivity chatUsersActivity, Updates updates) {
        this.arg$1 = chatUsersActivity;
        this.arg$2 = updates;
    }

    public void run() {
        this.arg$1.lambda$null$13$ChatUsersActivity(this.arg$2);
    }
}
