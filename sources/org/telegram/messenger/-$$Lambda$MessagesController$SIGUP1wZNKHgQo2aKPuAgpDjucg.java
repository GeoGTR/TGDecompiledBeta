package org.telegram.messenger;

import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$MessagesController$SIGUP1wZNKHgQo2aKPuAgpDjucg implements OnCancelListener {
    private final /* synthetic */ MessagesController f$0;
    private final /* synthetic */ int f$1;

    public /* synthetic */ -$$Lambda$MessagesController$SIGUP1wZNKHgQo2aKPuAgpDjucg(MessagesController messagesController, int i) {
        this.f$0 = messagesController;
        this.f$1 = i;
    }

    public final void onCancel(DialogInterface dialogInterface) {
        this.f$0.lambda$convertToMegaGroup$165$MessagesController(this.f$1, dialogInterface);
    }
}
