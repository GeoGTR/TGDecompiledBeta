package org.telegram.messenger;

import org.telegram.tgnet.TLRPC.EncryptedChat;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$MessagesStorage$WjmzaNMoaj5gwo5VkkK5s4gBFCs implements Runnable {
    private final /* synthetic */ MessagesStorage f$0;
    private final /* synthetic */ EncryptedChat f$1;
    private final /* synthetic */ boolean f$2;

    public /* synthetic */ -$$Lambda$MessagesStorage$WjmzaNMoaj5gwo5VkkK5s4gBFCs(MessagesStorage messagesStorage, EncryptedChat encryptedChat, boolean z) {
        this.f$0 = messagesStorage;
        this.f$1 = encryptedChat;
        this.f$2 = z;
    }

    public final void run() {
        this.f$0.lambda$updateEncryptedChatSeq$100$MessagesStorage(this.f$1, this.f$2);
    }
}
