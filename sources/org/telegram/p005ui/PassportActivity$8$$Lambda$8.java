package org.telegram.p005ui;

import org.telegram.p005ui.PassportActivity.CLASSNAME;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.TL_error;

/* renamed from: org.telegram.ui.PassportActivity$8$$Lambda$8 */
final /* synthetic */ class PassportActivity$8$$Lambda$8 implements Runnable {
    private final CLASSNAME arg$1;
    private final TLObject arg$2;
    private final TL_error arg$3;

    PassportActivity$8$$Lambda$8(CLASSNAME CLASSNAME, TLObject tLObject, TL_error tL_error) {
        this.arg$1 = CLASSNAME;
        this.arg$2 = tLObject;
        this.arg$3 = tL_error;
    }

    public void run() {
        this.arg$1.lambda$null$12$PassportActivity$8(this.arg$2, this.arg$3);
    }
}
