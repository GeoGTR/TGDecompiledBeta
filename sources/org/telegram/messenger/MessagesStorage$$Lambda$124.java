package org.telegram.messenger;

import java.util.ArrayList;

final /* synthetic */ class MessagesStorage$$Lambda$124 implements Runnable {
    private final ArrayList arg$1;

    MessagesStorage$$Lambda$124(ArrayList arrayList) {
        this.arg$1 = arrayList;
    }

    public void run() {
        NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.wallpapersDidLoad, this.arg$1);
    }
}
