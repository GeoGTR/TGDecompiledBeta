package org.telegram.p005ui;

import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

/* renamed from: org.telegram.ui.PollCreateActivity$$Lambda$2 */
final /* synthetic */ class PollCreateActivity$$Lambda$2 implements OnClickListener {
    private final PollCreateActivity arg$1;

    PollCreateActivity$$Lambda$2(PollCreateActivity pollCreateActivity) {
        this.arg$1 = pollCreateActivity;
    }

    public void onClick(DialogInterface dialogInterface, int i) {
        this.arg$1.lambda$checkDiscard$2$PollCreateActivity(dialogInterface, i);
    }
}
