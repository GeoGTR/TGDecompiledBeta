package org.telegram.messenger;

import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;

final /* synthetic */ class MessagesController$$Lambda$150 implements OnCancelListener {
    private final MessagesController arg$1;
    private final int arg$2;

    MessagesController$$Lambda$150(MessagesController messagesController, int i) {
        this.arg$1 = messagesController;
        this.arg$2 = i;
    }

    public void onCancel(DialogInterface dialogInterface) {
        this.arg$1.lambda$null$245$MessagesController(this.arg$2, dialogInterface);
    }
}
