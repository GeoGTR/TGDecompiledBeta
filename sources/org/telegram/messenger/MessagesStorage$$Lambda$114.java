package org.telegram.messenger;

import org.telegram.tgnet.TLRPC.ChatFull;

final /* synthetic */ class MessagesStorage$$Lambda$114 implements Runnable {
    private final MessagesStorage arg$1;
    private final ChatFull arg$2;

    MessagesStorage$$Lambda$114(MessagesStorage messagesStorage, ChatFull chatFull) {
        this.arg$1 = messagesStorage;
        this.arg$2 = chatFull;
    }

    public void run() {
        this.arg$1.lambda$null$72$MessagesStorage(this.arg$2);
    }
}
