package org.telegram.p005ui;

import org.telegram.p005ui.PassportActivity.CLASSNAME.CLASSNAME;
import org.telegram.p005ui.PassportActivity.ErrorRunnable;
import org.telegram.p005ui.PassportActivity.PassportActivityDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.TL_error;
import org.telegram.tgnet.TLRPC.TL_secureRequiredType;

/* renamed from: org.telegram.ui.PassportActivity$19$1$$Lambda$4 */
final /* synthetic */ class PassportActivity$19$1$$Lambda$4 implements Runnable {
    private final CLASSNAME arg$1;
    private final TLObject arg$2;
    private final String arg$3;
    private final TL_secureRequiredType arg$4;
    private final PassportActivityDelegate arg$5;
    private final TL_error arg$6;
    private final ErrorRunnable arg$7;

    PassportActivity$19$1$$Lambda$4(CLASSNAME CLASSNAME, TLObject tLObject, String str, TL_secureRequiredType tL_secureRequiredType, PassportActivityDelegate passportActivityDelegate, TL_error tL_error, ErrorRunnable errorRunnable) {
        this.arg$1 = CLASSNAME;
        this.arg$2 = tLObject;
        this.arg$3 = str;
        this.arg$4 = tL_secureRequiredType;
        this.arg$5 = passportActivityDelegate;
        this.arg$6 = tL_error;
        this.arg$7 = errorRunnable;
    }

    public void run() {
        this.arg$1.lambda$null$1$PassportActivity$19$1(this.arg$2, this.arg$3, this.arg$4, this.arg$5, this.arg$6, this.arg$7);
    }
}
