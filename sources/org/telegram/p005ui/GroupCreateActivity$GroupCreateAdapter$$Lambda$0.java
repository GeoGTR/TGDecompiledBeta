package org.telegram.p005ui;

import java.util.ArrayList;
import org.telegram.p005ui.GroupCreateActivity.GroupCreateAdapter;

/* renamed from: org.telegram.ui.GroupCreateActivity$GroupCreateAdapter$$Lambda$0 */
final /* synthetic */ class GroupCreateActivity$GroupCreateAdapter$$Lambda$0 implements Runnable {
    private final GroupCreateAdapter arg$1;
    private final ArrayList arg$2;
    private final ArrayList arg$3;

    GroupCreateActivity$GroupCreateAdapter$$Lambda$0(GroupCreateAdapter groupCreateAdapter, ArrayList arrayList, ArrayList arrayList2) {
        this.arg$1 = groupCreateAdapter;
        this.arg$2 = arrayList;
        this.arg$3 = arrayList2;
    }

    public void run() {
        this.arg$1.mo18400x38var_e(this.arg$2, this.arg$3);
    }
}
