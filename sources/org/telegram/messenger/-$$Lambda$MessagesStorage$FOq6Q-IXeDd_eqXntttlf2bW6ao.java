package org.telegram.messenger;

import java.util.concurrent.CountDownLatch;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$MessagesStorage$FOq6Q-IXeDd_eqXntttlf2bW6ao implements Runnable {
    private final /* synthetic */ MessagesStorage f$0;
    private final /* synthetic */ boolean f$1;
    private final /* synthetic */ long f$2;
    private final /* synthetic */ Integer[] f$3;
    private final /* synthetic */ CountDownLatch f$4;

    public /* synthetic */ -$$Lambda$MessagesStorage$FOq6Q-IXeDd_eqXntttlf2bW6ao(MessagesStorage messagesStorage, boolean z, long j, Integer[] numArr, CountDownLatch countDownLatch) {
        this.f$0 = messagesStorage;
        this.f$1 = z;
        this.f$2 = j;
        this.f$3 = numArr;
        this.f$4 = countDownLatch;
    }

    public final void run() {
        this.f$0.lambda$getDialogReadMax$144$MessagesStorage(this.f$1, this.f$2, this.f$3, this.f$4);
    }
}
