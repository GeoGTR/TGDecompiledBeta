package org.telegram.messenger;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$MediaController$aNYrkuu1k-Q9tWrqTPM_3Nml9Zg implements Runnable {
    private final /* synthetic */ MediaController f$0;
    private final /* synthetic */ int f$1;
    private final /* synthetic */ long f$2;
    private final /* synthetic */ MessageObject f$3;

    public /* synthetic */ -$$Lambda$MediaController$aNYrkuu1k-Q9tWrqTPM_3Nml9Zg(MediaController mediaController, int i, long j, MessageObject messageObject) {
        this.f$0 = mediaController;
        this.f$1 = i;
        this.f$2 = j;
        this.f$3 = messageObject;
    }

    public final void run() {
        this.f$0.lambda$startRecording$16$MediaController(this.f$1, this.f$2, this.f$3);
    }
}
