package org.telegram.messenger;

import org.telegram.tgnet.TLRPC.TL_emojiKeywordsDifference;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$MediaDataController$EvTxiVNHRw13fRGs7CTexXVtmQQ implements Runnable {
    private final /* synthetic */ MediaDataController f$0;
    private final /* synthetic */ TL_emojiKeywordsDifference f$1;
    private final /* synthetic */ String f$2;

    public /* synthetic */ -$$Lambda$MediaDataController$EvTxiVNHRw13fRGs7CTexXVtmQQ(MediaDataController mediaDataController, TL_emojiKeywordsDifference tL_emojiKeywordsDifference, String str) {
        this.f$0 = mediaDataController;
        this.f$1 = tL_emojiKeywordsDifference;
        this.f$2 = str;
    }

    public final void run() {
        this.f$0.lambda$putEmojiKeywords$119$MediaDataController(this.f$1, this.f$2);
    }
}
