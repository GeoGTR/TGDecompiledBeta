package org.telegram.messenger;

import org.telegram.messenger.ImageLoader.C04452;

final /* synthetic */ class ImageLoader$2$$Lambda$4 implements Runnable {
    private final C04452 arg$1;
    private final String arg$2;
    private final int arg$3;
    private final int arg$4;

    ImageLoader$2$$Lambda$4(C04452 c04452, String str, int i, int i2) {
        this.arg$1 = c04452;
        this.arg$2 = str;
        this.arg$3 = i;
        this.arg$4 = i2;
    }

    public void run() {
        this.arg$1.lambda$fileDidFailedLoad$6$ImageLoader$2(this.arg$2, this.arg$3, this.arg$4);
    }
}
