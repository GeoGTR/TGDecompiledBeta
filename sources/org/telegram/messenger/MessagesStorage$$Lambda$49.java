package org.telegram.messenger;

final /* synthetic */ class MessagesStorage$$Lambda$49 implements Runnable {
    private final MessagesStorage arg$1;
    private final int arg$2;
    private final int arg$3;

    MessagesStorage$$Lambda$49(MessagesStorage messagesStorage, int i, int i2) {
        this.arg$1 = messagesStorage;
        this.arg$2 = i;
        this.arg$3 = i2;
    }

    public void run() {
        this.arg$1.lambda$updateChatPinnedMessage$72$MessagesStorage(this.arg$2, this.arg$3);
    }
}
