package org.telegram.p005ui;

import org.telegram.p005ui.PassportActivity.CLASSNAME;

/* renamed from: org.telegram.ui.PassportActivity$8$$Lambda$1 */
final /* synthetic */ class PassportActivity$8$$Lambda$1 implements Runnable {
    private final CLASSNAME arg$1;
    private final byte[] arg$2;
    private final String arg$3;

    PassportActivity$8$$Lambda$1(CLASSNAME CLASSNAME, byte[] bArr, String str) {
        this.arg$1 = CLASSNAME;
        this.arg$2 = bArr;
        this.arg$3 = str;
    }

    public void run() {
        this.arg$1.lambda$generateNewSecret$8$PassportActivity$8(this.arg$2, this.arg$3);
    }
}
