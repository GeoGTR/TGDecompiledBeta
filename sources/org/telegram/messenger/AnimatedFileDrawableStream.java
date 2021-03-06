package org.telegram.messenger;

import java.util.concurrent.CountDownLatch;
import org.telegram.tgnet.TLRPC$Document;

public class AnimatedFileDrawableStream implements FileLoadOperationStream {
    private volatile boolean canceled;
    private CountDownLatch countDownLatch;
    private int currentAccount;
    private TLRPC$Document document;
    private String finishedFilePath;
    private boolean finishedLoadingFile;
    private boolean ignored;
    private int lastOffset;
    private FileLoadOperation loadOperation;
    private ImageLocation location;
    private Object parentObject;
    private boolean preview;
    private final Object sync = new Object();
    private boolean waitingForLoad;

    public AnimatedFileDrawableStream(TLRPC$Document tLRPC$Document, ImageLocation imageLocation, Object obj, int i, boolean z) {
        this.document = tLRPC$Document;
        this.location = imageLocation;
        this.parentObject = obj;
        this.currentAccount = i;
        this.preview = z;
        this.loadOperation = FileLoader.getInstance(i).loadStreamFile(this, this.document, this.location, this.parentObject, 0, this.preview);
    }

    public boolean isFinishedLoadingFile() {
        return this.finishedLoadingFile;
    }

    public String getFinishedFilePath() {
        return this.finishedFilePath;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x000e, code lost:
        r0 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x000f, code lost:
        if (r0 != 0) goto L_0x0082;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:?, code lost:
        r1 = r12.loadOperation.getDownloadedLengthFromOffset(r13, r14);
        r0 = r1[0];
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x001c, code lost:
        if (r12.finishedLoadingFile != false) goto L_0x0030;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0020, code lost:
        if (r1[1] == 0) goto L_0x0030;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0022, code lost:
        r12.finishedLoadingFile = true;
        r12.finishedFilePath = r12.loadOperation.getCacheFileFinal().getAbsolutePath();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0030, code lost:
        if (r0 != 0) goto L_0x000f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0038, code lost:
        if (r12.loadOperation.isPaused() != false) goto L_0x0042;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x003c, code lost:
        if (r12.lastOffset != r13) goto L_0x0042;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0040, code lost:
        if (r12.preview == false) goto L_0x0055;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0042, code lost:
        org.telegram.messenger.FileLoader.getInstance(r12.currentAccount).loadStreamFile(r12, r12.document, r12.location, r12.parentObject, r13, r12.preview);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0055, code lost:
        r1 = r12.sync;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0057, code lost:
        monitor-enter(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x005a, code lost:
        if (r12.canceled == false) goto L_0x005e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x005c, code lost:
        monitor-exit(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x005d, code lost:
        return 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x005e, code lost:
        r12.countDownLatch = new java.util.concurrent.CountDownLatch(1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x0065, code lost:
        monitor-exit(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x0068, code lost:
        if (r12.preview != false) goto L_0x0075;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x006a, code lost:
        org.telegram.messenger.FileLoader.getInstance(r12.currentAccount).setLoadingVideo(r12.document, false, true);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x0075, code lost:
        r12.waitingForLoad = true;
        r12.countDownLatch.await();
        r12.waitingForLoad = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x0082, code lost:
        r12.lastOffset = r13 + r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x0086, code lost:
        r13 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x0087, code lost:
        org.telegram.messenger.FileLog.e((java.lang.Throwable) r13);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:0x008a, code lost:
        return r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x000b, code lost:
        if (r14 != 0) goto L_0x000e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x000d, code lost:
        return 0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int read(int r13, int r14) {
        /*
            r12 = this;
            java.lang.Object r0 = r12.sync
            monitor-enter(r0)
            boolean r1 = r12.canceled     // Catch:{ all -> 0x008b }
            r2 = 0
            if (r1 == 0) goto L_0x000a
            monitor-exit(r0)     // Catch:{ all -> 0x008b }
            return r2
        L_0x000a:
            monitor-exit(r0)     // Catch:{ all -> 0x008b }
            if (r14 != 0) goto L_0x000e
            return r2
        L_0x000e:
            r0 = 0
        L_0x000f:
            if (r0 != 0) goto L_0x0082
            org.telegram.messenger.FileLoadOperation r1 = r12.loadOperation     // Catch:{ Exception -> 0x0086 }
            int[] r1 = r1.getDownloadedLengthFromOffset(r13, r14)     // Catch:{ Exception -> 0x0086 }
            r0 = r1[r2]     // Catch:{ Exception -> 0x0086 }
            boolean r3 = r12.finishedLoadingFile     // Catch:{ Exception -> 0x0086 }
            r4 = 1
            if (r3 != 0) goto L_0x0030
            r1 = r1[r4]     // Catch:{ Exception -> 0x0086 }
            if (r1 == 0) goto L_0x0030
            r12.finishedLoadingFile = r4     // Catch:{ Exception -> 0x0086 }
            org.telegram.messenger.FileLoadOperation r1 = r12.loadOperation     // Catch:{ Exception -> 0x0086 }
            java.io.File r1 = r1.getCacheFileFinal()     // Catch:{ Exception -> 0x0086 }
            java.lang.String r1 = r1.getAbsolutePath()     // Catch:{ Exception -> 0x0086 }
            r12.finishedFilePath = r1     // Catch:{ Exception -> 0x0086 }
        L_0x0030:
            if (r0 != 0) goto L_0x000f
            org.telegram.messenger.FileLoadOperation r1 = r12.loadOperation     // Catch:{ Exception -> 0x0086 }
            boolean r1 = r1.isPaused()     // Catch:{ Exception -> 0x0086 }
            if (r1 != 0) goto L_0x0042
            int r1 = r12.lastOffset     // Catch:{ Exception -> 0x0086 }
            if (r1 != r13) goto L_0x0042
            boolean r1 = r12.preview     // Catch:{ Exception -> 0x0086 }
            if (r1 == 0) goto L_0x0055
        L_0x0042:
            int r1 = r12.currentAccount     // Catch:{ Exception -> 0x0086 }
            org.telegram.messenger.FileLoader r5 = org.telegram.messenger.FileLoader.getInstance(r1)     // Catch:{ Exception -> 0x0086 }
            org.telegram.tgnet.TLRPC$Document r7 = r12.document     // Catch:{ Exception -> 0x0086 }
            org.telegram.messenger.ImageLocation r8 = r12.location     // Catch:{ Exception -> 0x0086 }
            java.lang.Object r9 = r12.parentObject     // Catch:{ Exception -> 0x0086 }
            boolean r11 = r12.preview     // Catch:{ Exception -> 0x0086 }
            r6 = r12
            r10 = r13
            r5.loadStreamFile(r6, r7, r8, r9, r10, r11)     // Catch:{ Exception -> 0x0086 }
        L_0x0055:
            java.lang.Object r1 = r12.sync     // Catch:{ Exception -> 0x0086 }
            monitor-enter(r1)     // Catch:{ Exception -> 0x0086 }
            boolean r3 = r12.canceled     // Catch:{ all -> 0x007f }
            if (r3 == 0) goto L_0x005e
            monitor-exit(r1)     // Catch:{ all -> 0x007f }
            return r2
        L_0x005e:
            java.util.concurrent.CountDownLatch r3 = new java.util.concurrent.CountDownLatch     // Catch:{ all -> 0x007f }
            r3.<init>(r4)     // Catch:{ all -> 0x007f }
            r12.countDownLatch = r3     // Catch:{ all -> 0x007f }
            monitor-exit(r1)     // Catch:{ all -> 0x007f }
            boolean r1 = r12.preview     // Catch:{ Exception -> 0x0086 }
            if (r1 != 0) goto L_0x0075
            int r1 = r12.currentAccount     // Catch:{ Exception -> 0x0086 }
            org.telegram.messenger.FileLoader r1 = org.telegram.messenger.FileLoader.getInstance(r1)     // Catch:{ Exception -> 0x0086 }
            org.telegram.tgnet.TLRPC$Document r3 = r12.document     // Catch:{ Exception -> 0x0086 }
            r1.setLoadingVideo(r3, r2, r4)     // Catch:{ Exception -> 0x0086 }
        L_0x0075:
            r12.waitingForLoad = r4     // Catch:{ Exception -> 0x0086 }
            java.util.concurrent.CountDownLatch r1 = r12.countDownLatch     // Catch:{ Exception -> 0x0086 }
            r1.await()     // Catch:{ Exception -> 0x0086 }
            r12.waitingForLoad = r2     // Catch:{ Exception -> 0x0086 }
            goto L_0x000f
        L_0x007f:
            r13 = move-exception
            monitor-exit(r1)     // Catch:{ all -> 0x007f }
            throw r13     // Catch:{ Exception -> 0x0086 }
        L_0x0082:
            int r13 = r13 + r0
            r12.lastOffset = r13     // Catch:{ Exception -> 0x0086 }
            goto L_0x008a
        L_0x0086:
            r13 = move-exception
            org.telegram.messenger.FileLog.e((java.lang.Throwable) r13)
        L_0x008a:
            return r0
        L_0x008b:
            r13 = move-exception
            monitor-exit(r0)     // Catch:{ all -> 0x008b }
            goto L_0x008f
        L_0x008e:
            throw r13
        L_0x008f:
            goto L_0x008e
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.messenger.AnimatedFileDrawableStream.read(int, int):int");
    }

    public void cancel() {
        cancel(true);
    }

    public void cancel(boolean z) {
        synchronized (this.sync) {
            CountDownLatch countDownLatch2 = this.countDownLatch;
            if (countDownLatch2 != null) {
                countDownLatch2.countDown();
                if (z && !this.canceled && !this.preview) {
                    FileLoader.getInstance(this.currentAccount).removeLoadingVideo(this.document, false, true);
                }
            }
            this.canceled = true;
        }
    }

    public void reset() {
        synchronized (this.sync) {
            this.canceled = false;
        }
    }

    public TLRPC$Document getDocument() {
        return this.document;
    }

    public ImageLocation getLocation() {
        return this.location;
    }

    public Object getParentObject() {
        return this.document;
    }

    public boolean isPreview() {
        return this.preview;
    }

    public int getCurrentAccount() {
        return this.currentAccount;
    }

    public boolean isWaitingForLoad() {
        return this.waitingForLoad;
    }

    public void newDataAvailable() {
        CountDownLatch countDownLatch2 = this.countDownLatch;
        if (countDownLatch2 != null) {
            countDownLatch2.countDown();
        }
    }
}
