package org.telegram.messenger;

import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.KeyboardButton;
import org.telegram.tgnet.TLRPC.TL_error;
import org.telegram.ui.ChatActivity;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$SendMessagesHelper$dI2Pjo3q3FWw7MFM1jQfwpbp8Kg implements RequestDelegate {
    private final /* synthetic */ SendMessagesHelper f$0;
    private final /* synthetic */ String f$1;
    private final /* synthetic */ boolean f$2;
    private final /* synthetic */ MessageObject f$3;
    private final /* synthetic */ KeyboardButton f$4;
    private final /* synthetic */ ChatActivity f$5;

    public /* synthetic */ -$$Lambda$SendMessagesHelper$dI2Pjo3q3FWw7MFM1jQfwpbp8Kg(SendMessagesHelper sendMessagesHelper, String str, boolean z, MessageObject messageObject, KeyboardButton keyboardButton, ChatActivity chatActivity) {
        this.f$0 = sendMessagesHelper;
        this.f$1 = str;
        this.f$2 = z;
        this.f$3 = messageObject;
        this.f$4 = keyboardButton;
        this.f$5 = chatActivity;
    }

    public final void run(TLObject tLObject, TL_error tL_error) {
        this.f$0.lambda$sendCallback$17$SendMessagesHelper(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, tLObject, tL_error);
    }
}
