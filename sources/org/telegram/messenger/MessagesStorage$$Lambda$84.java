package org.telegram.messenger;

final /* synthetic */ class MessagesStorage$$Lambda$84 implements Runnable {
    private final MessagesStorage arg$1;
    private final int arg$2;

    MessagesStorage$$Lambda$84(MessagesStorage messagesStorage, int i) {
        this.arg$1 = messagesStorage;
        this.arg$2 = i;
    }

    public void run() {
        this.arg$1.lambda$putMessagesInternal$114$MessagesStorage(this.arg$2);
    }
}
