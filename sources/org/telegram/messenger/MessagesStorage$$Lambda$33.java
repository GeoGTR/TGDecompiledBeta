package org.telegram.messenger;

final /* synthetic */ class MessagesStorage$$Lambda$33 implements Runnable {
    private final MessagesStorage arg$1;
    private final long arg$2;

    MessagesStorage$$Lambda$33(MessagesStorage messagesStorage, long j) {
        this.arg$1 = messagesStorage;
        this.arg$2 = j;
    }

    public void run() {
        this.arg$1.lambda$markMessageAsMention$52$MessagesStorage(this.arg$2);
    }
}
