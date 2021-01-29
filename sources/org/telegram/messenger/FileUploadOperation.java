package org.telegram.messenger;

import android.content.SharedPreferences;
import android.util.SparseArray;
import android.util.SparseIntArray;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC$InputEncryptedFile;
import org.telegram.tgnet.TLRPC$InputFile;
import org.telegram.tgnet.TLRPC$TL_boolTrue;
import org.telegram.tgnet.TLRPC$TL_error;
import org.telegram.tgnet.TLRPC$TL_inputEncryptedFileBigUploaded;
import org.telegram.tgnet.TLRPC$TL_inputEncryptedFileUploaded;
import org.telegram.tgnet.TLRPC$TL_inputFile;
import org.telegram.tgnet.TLRPC$TL_inputFileBig;

public class FileUploadOperation {
    private static final int initialRequestsCount = 8;
    private static final int initialRequestsSlowNetworkCount = 1;
    private static final int maxUploadParts = 4000;
    private static final int maxUploadingKBytes = 2048;
    private static final int maxUploadingSlowNetworkKBytes = 32;
    private static final int minUploadChunkSize = 128;
    private static final int minUploadChunkSlowNetworkSize = 32;
    private long availableSize;
    private SparseArray<UploadCachedResult> cachedResults = new SparseArray<>();
    private int currentAccount;
    private long currentFileId;
    private int currentPartNum;
    private int currentType;
    private int currentUploadRequetsCount;
    private FileUploadOperationDelegate delegate;
    private int estimatedSize;
    private String fileKey;
    private int fingerprint;
    private ArrayList<byte[]> freeRequestIvs;
    private boolean isBigFile;
    private boolean isEncrypted;
    private boolean isLastPart;
    private byte[] iv;
    private byte[] ivChange;
    private byte[] key;
    private int lastSavedPartNum;
    private int maxRequestsCount;
    private boolean nextPartFirst;
    private int operationGuid;
    private SharedPreferences preferences;
    private byte[] readBuffer;
    private long readBytesCount;
    private int requestNum;
    private SparseIntArray requestTokens = new SparseIntArray();
    private int saveInfoTimes;
    private boolean slowNetwork;
    private boolean started;
    private int state;
    private RandomAccessFile stream;
    private long totalFileSize;
    private int totalPartsCount;
    private int uploadChunkSize = 65536;
    private boolean uploadFirstPartLater;
    private int uploadStartTime;
    private long uploadedBytesCount;
    private String uploadingFilePath;

    public interface FileUploadOperationDelegate {
        void didChangedUploadProgress(FileUploadOperation fileUploadOperation, long j, long j2);

        void didFailedUploadingFile(FileUploadOperation fileUploadOperation);

        void didFinishUploadingFile(FileUploadOperation fileUploadOperation, TLRPC$InputFile tLRPC$InputFile, TLRPC$InputEncryptedFile tLRPC$InputEncryptedFile, byte[] bArr, byte[] bArr2);
    }

    private static class UploadCachedResult {
        /* access modifiers changed from: private */
        public long bytesOffset;
        /* access modifiers changed from: private */
        public byte[] iv;

        private UploadCachedResult() {
        }
    }

    public FileUploadOperation(int i, String str, boolean z, int i2, int i3) {
        this.currentAccount = i;
        this.uploadingFilePath = str;
        this.isEncrypted = z;
        this.estimatedSize = i2;
        this.currentType = i3;
        this.uploadFirstPartLater = i2 != 0 && !z;
    }

    public long getTotalFileSize() {
        return this.totalFileSize;
    }

    public void setDelegate(FileUploadOperationDelegate fileUploadOperationDelegate) {
        this.delegate = fileUploadOperationDelegate;
    }

    public void start() {
        if (this.state == 0) {
            this.state = 1;
            Utilities.stageQueue.postRunnable(new Runnable() {
                public final void run() {
                    FileUploadOperation.this.lambda$start$0$FileUploadOperation();
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$start$0 */
    public /* synthetic */ void lambda$start$0$FileUploadOperation() {
        this.preferences = ApplicationLoader.applicationContext.getSharedPreferences("uploadinfo", 0);
        this.slowNetwork = ApplicationLoader.isConnectionSlow();
        if (BuildVars.LOGS_ENABLED) {
            FileLog.d("start upload on slow network = " + this.slowNetwork);
        }
        int i = this.slowNetwork ? 1 : 8;
        for (int i2 = 0; i2 < i; i2++) {
            startUploadRequest();
        }
    }

    /* access modifiers changed from: protected */
    public void onNetworkChanged(boolean z) {
        if (this.state == 1) {
            Utilities.stageQueue.postRunnable(new Runnable(z) {
                public final /* synthetic */ boolean f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    FileUploadOperation.this.lambda$onNetworkChanged$1$FileUploadOperation(this.f$1);
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onNetworkChanged$1 */
    public /* synthetic */ void lambda$onNetworkChanged$1$FileUploadOperation(boolean z) {
        int i;
        if (this.slowNetwork != z) {
            this.slowNetwork = z;
            if (BuildVars.LOGS_ENABLED) {
                FileLog.d("network changed to slow = " + this.slowNetwork);
            }
            int i2 = 0;
            while (true) {
                i = 1;
                if (i2 >= this.requestTokens.size()) {
                    break;
                }
                ConnectionsManager.getInstance(this.currentAccount).cancelRequest(this.requestTokens.valueAt(i2), true);
                i2++;
            }
            this.requestTokens.clear();
            cleanup();
            this.isLastPart = false;
            this.nextPartFirst = false;
            this.requestNum = 0;
            this.currentPartNum = 0;
            this.readBytesCount = 0;
            this.uploadedBytesCount = 0;
            this.saveInfoTimes = 0;
            this.key = null;
            this.iv = null;
            this.ivChange = null;
            this.currentUploadRequetsCount = 0;
            this.lastSavedPartNum = 0;
            this.uploadFirstPartLater = false;
            this.cachedResults.clear();
            this.operationGuid++;
            if (!this.slowNetwork) {
                i = 8;
            }
            for (int i3 = 0; i3 < i; i3++) {
                startUploadRequest();
            }
        }
    }

    public void cancel() {
        if (this.state != 3) {
            this.state = 2;
            Utilities.stageQueue.postRunnable(new Runnable() {
                public final void run() {
                    FileUploadOperation.this.lambda$cancel$2$FileUploadOperation();
                }
            });
            this.delegate.didFailedUploadingFile(this);
            cleanup();
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$cancel$2 */
    public /* synthetic */ void lambda$cancel$2$FileUploadOperation() {
        for (int i = 0; i < this.requestTokens.size(); i++) {
            ConnectionsManager.getInstance(this.currentAccount).cancelRequest(this.requestTokens.valueAt(i), true);
        }
    }

    private void cleanup() {
        if (this.preferences == null) {
            this.preferences = ApplicationLoader.applicationContext.getSharedPreferences("uploadinfo", 0);
        }
        SharedPreferences.Editor edit = this.preferences.edit();
        SharedPreferences.Editor remove = edit.remove(this.fileKey + "_time");
        SharedPreferences.Editor remove2 = remove.remove(this.fileKey + "_size");
        SharedPreferences.Editor remove3 = remove2.remove(this.fileKey + "_uploaded");
        SharedPreferences.Editor remove4 = remove3.remove(this.fileKey + "_id");
        SharedPreferences.Editor remove5 = remove4.remove(this.fileKey + "_iv");
        SharedPreferences.Editor remove6 = remove5.remove(this.fileKey + "_key");
        remove6.remove(this.fileKey + "_ivc").commit();
        try {
            RandomAccessFile randomAccessFile = this.stream;
            if (randomAccessFile != null) {
                randomAccessFile.close();
                this.stream = null;
            }
        } catch (Exception e) {
            FileLog.e((Throwable) e);
        }
    }

    /* access modifiers changed from: protected */
    public void checkNewDataAvailable(long j, long j2) {
        Utilities.stageQueue.postRunnable(new Runnable(j2, j) {
            public final /* synthetic */ long f$1;
            public final /* synthetic */ long f$2;

            {
                this.f$1 = r2;
                this.f$2 = r4;
            }

            public final void run() {
                FileUploadOperation.this.lambda$checkNewDataAvailable$3$FileUploadOperation(this.f$1, this.f$2);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$checkNewDataAvailable$3 */
    public /* synthetic */ void lambda$checkNewDataAvailable$3$FileUploadOperation(long j, long j2) {
        if (!(this.estimatedSize == 0 || j == 0)) {
            this.estimatedSize = 0;
            this.totalFileSize = j;
            calcTotalPartsCount();
            if (!this.uploadFirstPartLater && this.started) {
                storeFileUploadInfo();
            }
        }
        if (j <= 0) {
            j = j2;
        }
        this.availableSize = j;
        if (this.currentUploadRequetsCount < this.maxRequestsCount) {
            startUploadRequest();
        }
    }

    private void storeFileUploadInfo() {
        SharedPreferences.Editor edit = this.preferences.edit();
        edit.putInt(this.fileKey + "_time", this.uploadStartTime);
        edit.putLong(this.fileKey + "_size", this.totalFileSize);
        edit.putLong(this.fileKey + "_id", this.currentFileId);
        edit.remove(this.fileKey + "_uploaded");
        if (this.isEncrypted) {
            edit.putString(this.fileKey + "_iv", Utilities.bytesToHex(this.iv));
            edit.putString(this.fileKey + "_ivc", Utilities.bytesToHex(this.ivChange));
            edit.putString(this.fileKey + "_key", Utilities.bytesToHex(this.key));
        }
        edit.commit();
    }

    private void calcTotalPartsCount() {
        if (!this.uploadFirstPartLater) {
            long j = this.totalFileSize;
            int i = this.uploadChunkSize;
            this.totalPartsCount = ((int) ((j + ((long) i)) - 1)) / i;
        } else if (this.isBigFile) {
            long j2 = this.totalFileSize;
            int i2 = this.uploadChunkSize;
            this.totalPartsCount = (((int) (((j2 - ((long) i2)) + ((long) i2)) - 1)) / i2) + 1;
        } else {
            int i3 = this.uploadChunkSize;
            this.totalPartsCount = (((int) (((this.totalFileSize - 1024) + ((long) i3)) - 1)) / i3) + 1;
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r19v0, resolved type: org.telegram.tgnet.TLRPC$TL_upload_saveFilePart} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r19v1, resolved type: org.telegram.tgnet.TLRPC$TL_upload_saveFilePart} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v7, resolved type: org.telegram.tgnet.TLRPC$TL_upload_saveBigFilePart} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r19v2, resolved type: org.telegram.tgnet.TLRPC$TL_upload_saveFilePart} */
    /* JADX WARNING: Multi-variable type inference failed */
    /* JADX WARNING: Removed duplicated region for block: B:122:0x02b7 A[Catch:{ Exception -> 0x04bd }] */
    /* JADX WARNING: Removed duplicated region for block: B:125:0x02bd A[Catch:{ Exception -> 0x04bd }] */
    /* JADX WARNING: Removed duplicated region for block: B:137:0x02fb  */
    /* JADX WARNING: Removed duplicated region for block: B:149:0x0339 A[Catch:{ Exception -> 0x04bd }] */
    /* JADX WARNING: Removed duplicated region for block: B:85:0x01fe A[Catch:{ Exception -> 0x04bd }] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void startUploadRequest() {
        /*
            r27 = this;
            r11 = r27
            int r0 = r11.state
            r1 = 1
            if (r0 == r1) goto L_0x0008
            return
        L_0x0008:
            r11.started = r1     // Catch:{ Exception -> 0x04bd }
            java.io.RandomAccessFile r0 = r11.stream     // Catch:{ Exception -> 0x04bd }
            r3 = 1024(0x400, float:1.435E-42)
            r4 = 0
            r5 = 0
            r7 = 32
            r8 = 0
            if (r0 != 0) goto L_0x0363
            java.io.File r9 = new java.io.File     // Catch:{ Exception -> 0x04bd }
            java.lang.String r0 = r11.uploadingFilePath     // Catch:{ Exception -> 0x04bd }
            r9.<init>(r0)     // Catch:{ Exception -> 0x04bd }
            android.net.Uri r0 = android.net.Uri.fromFile(r9)     // Catch:{ Exception -> 0x04bd }
            boolean r0 = org.telegram.messenger.AndroidUtilities.isInternalUri((android.net.Uri) r0)     // Catch:{ Exception -> 0x04bd }
            java.lang.String r10 = "trying to upload internal file"
            if (r0 != 0) goto L_0x035d
            java.io.RandomAccessFile r0 = new java.io.RandomAccessFile     // Catch:{ Exception -> 0x04bd }
            java.lang.String r12 = "r"
            r0.<init>(r9, r12)     // Catch:{ Exception -> 0x04bd }
            r11.stream = r0     // Catch:{ Exception -> 0x04bd }
            java.lang.Class<java.io.FileDescriptor> r0 = java.io.FileDescriptor.class
            java.lang.String r12 = "getInt$"
            java.lang.Class[] r13 = new java.lang.Class[r8]     // Catch:{ all -> 0x0053 }
            java.lang.reflect.Method r0 = r0.getDeclaredMethod(r12, r13)     // Catch:{ all -> 0x0053 }
            java.io.RandomAccessFile r12 = r11.stream     // Catch:{ all -> 0x0053 }
            java.io.FileDescriptor r12 = r12.getFD()     // Catch:{ all -> 0x0053 }
            java.lang.Object[] r13 = new java.lang.Object[r8]     // Catch:{ all -> 0x0053 }
            java.lang.Object r0 = r0.invoke(r12, r13)     // Catch:{ all -> 0x0053 }
            java.lang.Integer r0 = (java.lang.Integer) r0     // Catch:{ all -> 0x0053 }
            int r0 = r0.intValue()     // Catch:{ all -> 0x0053 }
            boolean r0 = org.telegram.messenger.AndroidUtilities.isInternalUri((int) r0)     // Catch:{ all -> 0x0053 }
            goto L_0x0058
        L_0x0053:
            r0 = move-exception
            org.telegram.messenger.FileLog.e((java.lang.Throwable) r0)     // Catch:{ Exception -> 0x04bd }
            r0 = 0
        L_0x0058:
            if (r0 != 0) goto L_0x0357
            int r0 = r11.estimatedSize     // Catch:{ Exception -> 0x04bd }
            if (r0 == 0) goto L_0x0062
            long r9 = (long) r0     // Catch:{ Exception -> 0x04bd }
            r11.totalFileSize = r9     // Catch:{ Exception -> 0x04bd }
            goto L_0x0068
        L_0x0062:
            long r9 = r9.length()     // Catch:{ Exception -> 0x04bd }
            r11.totalFileSize = r9     // Catch:{ Exception -> 0x04bd }
        L_0x0068:
            long r9 = r11.totalFileSize     // Catch:{ Exception -> 0x04bd }
            r12 = 10485760(0xa00000, double:5.180654E-317)
            int r0 = (r9 > r12 ? 1 : (r9 == r12 ? 0 : -1))
            if (r0 <= 0) goto L_0x0073
            r11.isBigFile = r1     // Catch:{ Exception -> 0x04bd }
        L_0x0073:
            boolean r0 = r11.slowNetwork     // Catch:{ Exception -> 0x04bd }
            if (r0 == 0) goto L_0x007a
            r12 = 32
            goto L_0x007c
        L_0x007a:
            r12 = 128(0x80, double:6.32E-322)
        L_0x007c:
            r14 = 4096000(0x3e8000, double:2.023693E-317)
            long r9 = r9 + r14
            r16 = 1
            long r9 = r9 - r16
            long r9 = r9 / r14
            long r9 = java.lang.Math.max(r12, r9)     // Catch:{ Exception -> 0x04bd }
            int r0 = (int) r9     // Catch:{ Exception -> 0x04bd }
            r11.uploadChunkSize = r0     // Catch:{ Exception -> 0x04bd }
            int r0 = r3 % r0
            r9 = 64
            if (r0 == 0) goto L_0x009d
            r0 = 64
        L_0x0094:
            int r10 = r11.uploadChunkSize     // Catch:{ Exception -> 0x04bd }
            if (r10 <= r0) goto L_0x009b
            int r0 = r0 * 2
            goto L_0x0094
        L_0x009b:
            r11.uploadChunkSize = r0     // Catch:{ Exception -> 0x04bd }
        L_0x009d:
            boolean r0 = r11.slowNetwork     // Catch:{ Exception -> 0x04bd }
            if (r0 == 0) goto L_0x00a4
            r0 = 32
            goto L_0x00a6
        L_0x00a4:
            r0 = 2048(0x800, float:2.87E-42)
        L_0x00a6:
            int r10 = r11.uploadChunkSize     // Catch:{ Exception -> 0x04bd }
            int r0 = r0 / r10
            int r0 = java.lang.Math.max(r1, r0)     // Catch:{ Exception -> 0x04bd }
            r11.maxRequestsCount = r0     // Catch:{ Exception -> 0x04bd }
            boolean r0 = r11.isEncrypted     // Catch:{ Exception -> 0x04bd }
            if (r0 == 0) goto L_0x00cb
            java.util.ArrayList r0 = new java.util.ArrayList     // Catch:{ Exception -> 0x04bd }
            int r10 = r11.maxRequestsCount     // Catch:{ Exception -> 0x04bd }
            r0.<init>(r10)     // Catch:{ Exception -> 0x04bd }
            r11.freeRequestIvs = r0     // Catch:{ Exception -> 0x04bd }
            r0 = 0
        L_0x00bd:
            int r10 = r11.maxRequestsCount     // Catch:{ Exception -> 0x04bd }
            if (r0 >= r10) goto L_0x00cb
            java.util.ArrayList<byte[]> r10 = r11.freeRequestIvs     // Catch:{ Exception -> 0x04bd }
            byte[] r12 = new byte[r7]     // Catch:{ Exception -> 0x04bd }
            r10.add(r12)     // Catch:{ Exception -> 0x04bd }
            int r0 = r0 + 1
            goto L_0x00bd
        L_0x00cb:
            int r0 = r11.uploadChunkSize     // Catch:{ Exception -> 0x04bd }
            int r0 = r0 * 1024
            r11.uploadChunkSize = r0     // Catch:{ Exception -> 0x04bd }
            r27.calcTotalPartsCount()     // Catch:{ Exception -> 0x04bd }
            int r0 = r11.uploadChunkSize     // Catch:{ Exception -> 0x04bd }
            byte[] r0 = new byte[r0]     // Catch:{ Exception -> 0x04bd }
            r11.readBuffer = r0     // Catch:{ Exception -> 0x04bd }
            java.lang.StringBuilder r0 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x04bd }
            r0.<init>()     // Catch:{ Exception -> 0x04bd }
            java.lang.String r10 = r11.uploadingFilePath     // Catch:{ Exception -> 0x04bd }
            r0.append(r10)     // Catch:{ Exception -> 0x04bd }
            boolean r10 = r11.isEncrypted     // Catch:{ Exception -> 0x04bd }
            if (r10 == 0) goto L_0x00eb
            java.lang.String r10 = "enc"
            goto L_0x00ed
        L_0x00eb:
            java.lang.String r10 = ""
        L_0x00ed:
            r0.append(r10)     // Catch:{ Exception -> 0x04bd }
            java.lang.String r0 = r0.toString()     // Catch:{ Exception -> 0x04bd }
            java.lang.String r0 = org.telegram.messenger.Utilities.MD5(r0)     // Catch:{ Exception -> 0x04bd }
            r11.fileKey = r0     // Catch:{ Exception -> 0x04bd }
            android.content.SharedPreferences r0 = r11.preferences     // Catch:{ Exception -> 0x04bd }
            java.lang.StringBuilder r10 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x04bd }
            r10.<init>()     // Catch:{ Exception -> 0x04bd }
            java.lang.String r12 = r11.fileKey     // Catch:{ Exception -> 0x04bd }
            r10.append(r12)     // Catch:{ Exception -> 0x04bd }
            java.lang.String r12 = "_size"
            r10.append(r12)     // Catch:{ Exception -> 0x04bd }
            java.lang.String r10 = r10.toString()     // Catch:{ Exception -> 0x04bd }
            long r12 = r0.getLong(r10, r5)     // Catch:{ Exception -> 0x04bd }
            long r14 = java.lang.System.currentTimeMillis()     // Catch:{ Exception -> 0x04bd }
            r16 = 1000(0x3e8, double:4.94E-321)
            long r14 = r14 / r16
            int r0 = (int) r14     // Catch:{ Exception -> 0x04bd }
            r11.uploadStartTime = r0     // Catch:{ Exception -> 0x04bd }
            boolean r0 = r11.uploadFirstPartLater     // Catch:{ Exception -> 0x04bd }
            if (r0 != 0) goto L_0x02ba
            boolean r0 = r11.nextPartFirst     // Catch:{ Exception -> 0x04bd }
            if (r0 != 0) goto L_0x02ba
            int r0 = r11.estimatedSize     // Catch:{ Exception -> 0x04bd }
            if (r0 != 0) goto L_0x02ba
            long r14 = r11.totalFileSize     // Catch:{ Exception -> 0x04bd }
            int r0 = (r12 > r14 ? 1 : (r12 == r14 ? 0 : -1))
            if (r0 != 0) goto L_0x02ba
            android.content.SharedPreferences r0 = r11.preferences     // Catch:{ Exception -> 0x04bd }
            java.lang.StringBuilder r10 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x04bd }
            r10.<init>()     // Catch:{ Exception -> 0x04bd }
            java.lang.String r12 = r11.fileKey     // Catch:{ Exception -> 0x04bd }
            r10.append(r12)     // Catch:{ Exception -> 0x04bd }
            java.lang.String r12 = "_id"
            r10.append(r12)     // Catch:{ Exception -> 0x04bd }
            java.lang.String r10 = r10.toString()     // Catch:{ Exception -> 0x04bd }
            long r12 = r0.getLong(r10, r5)     // Catch:{ Exception -> 0x04bd }
            r11.currentFileId = r12     // Catch:{ Exception -> 0x04bd }
            android.content.SharedPreferences r0 = r11.preferences     // Catch:{ Exception -> 0x04bd }
            java.lang.StringBuilder r10 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x04bd }
            r10.<init>()     // Catch:{ Exception -> 0x04bd }
            java.lang.String r12 = r11.fileKey     // Catch:{ Exception -> 0x04bd }
            r10.append(r12)     // Catch:{ Exception -> 0x04bd }
            java.lang.String r12 = "_time"
            r10.append(r12)     // Catch:{ Exception -> 0x04bd }
            java.lang.String r10 = r10.toString()     // Catch:{ Exception -> 0x04bd }
            int r0 = r0.getInt(r10, r8)     // Catch:{ Exception -> 0x04bd }
            android.content.SharedPreferences r10 = r11.preferences     // Catch:{ Exception -> 0x04bd }
            java.lang.StringBuilder r12 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x04bd }
            r12.<init>()     // Catch:{ Exception -> 0x04bd }
            java.lang.String r13 = r11.fileKey     // Catch:{ Exception -> 0x04bd }
            r12.append(r13)     // Catch:{ Exception -> 0x04bd }
            java.lang.String r13 = "_uploaded"
            r12.append(r13)     // Catch:{ Exception -> 0x04bd }
            java.lang.String r12 = r12.toString()     // Catch:{ Exception -> 0x04bd }
            long r12 = r10.getLong(r12, r5)     // Catch:{ Exception -> 0x04bd }
            boolean r10 = r11.isEncrypted     // Catch:{ Exception -> 0x04bd }
            if (r10 == 0) goto L_0x01d9
            android.content.SharedPreferences r10 = r11.preferences     // Catch:{ Exception -> 0x04bd }
            java.lang.StringBuilder r14 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x04bd }
            r14.<init>()     // Catch:{ Exception -> 0x04bd }
            java.lang.String r15 = r11.fileKey     // Catch:{ Exception -> 0x04bd }
            r14.append(r15)     // Catch:{ Exception -> 0x04bd }
            java.lang.String r15 = "_iv"
            r14.append(r15)     // Catch:{ Exception -> 0x04bd }
            java.lang.String r14 = r14.toString()     // Catch:{ Exception -> 0x04bd }
            java.lang.String r10 = r10.getString(r14, r4)     // Catch:{ Exception -> 0x04bd }
            android.content.SharedPreferences r14 = r11.preferences     // Catch:{ Exception -> 0x04bd }
            java.lang.StringBuilder r15 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x04bd }
            r15.<init>()     // Catch:{ Exception -> 0x04bd }
            java.lang.String r3 = r11.fileKey     // Catch:{ Exception -> 0x04bd }
            r15.append(r3)     // Catch:{ Exception -> 0x04bd }
            java.lang.String r3 = "_key"
            r15.append(r3)     // Catch:{ Exception -> 0x04bd }
            java.lang.String r3 = r15.toString()     // Catch:{ Exception -> 0x04bd }
            java.lang.String r3 = r14.getString(r3, r4)     // Catch:{ Exception -> 0x04bd }
            if (r10 == 0) goto L_0x01d7
            if (r3 == 0) goto L_0x01d7
            byte[] r3 = org.telegram.messenger.Utilities.hexToBytes(r3)     // Catch:{ Exception -> 0x04bd }
            r11.key = r3     // Catch:{ Exception -> 0x04bd }
            byte[] r3 = org.telegram.messenger.Utilities.hexToBytes(r10)     // Catch:{ Exception -> 0x04bd }
            r11.iv = r3     // Catch:{ Exception -> 0x04bd }
            byte[] r10 = r11.key     // Catch:{ Exception -> 0x04bd }
            if (r10 == 0) goto L_0x01d7
            if (r3 == 0) goto L_0x01d7
            int r10 = r10.length     // Catch:{ Exception -> 0x04bd }
            if (r10 != r7) goto L_0x01d7
            int r10 = r3.length     // Catch:{ Exception -> 0x04bd }
            if (r10 != r7) goto L_0x01d7
            byte[] r10 = new byte[r7]     // Catch:{ Exception -> 0x04bd }
            r11.ivChange = r10     // Catch:{ Exception -> 0x04bd }
            java.lang.System.arraycopy(r3, r8, r10, r8, r7)     // Catch:{ Exception -> 0x04bd }
            goto L_0x01d9
        L_0x01d7:
            r3 = 1
            goto L_0x01da
        L_0x01d9:
            r3 = 0
        L_0x01da:
            if (r3 != 0) goto L_0x02ba
            if (r0 == 0) goto L_0x02ba
            boolean r10 = r11.isBigFile     // Catch:{ Exception -> 0x04bd }
            if (r10 == 0) goto L_0x01ec
            int r14 = r11.uploadStartTime     // Catch:{ Exception -> 0x04bd }
            r15 = 86400(0x15180, float:1.21072E-40)
            int r14 = r14 - r15
            if (r0 >= r14) goto L_0x01ec
        L_0x01ea:
            r0 = 0
            goto L_0x01fc
        L_0x01ec:
            if (r10 != 0) goto L_0x01fc
            float r14 = (float) r0     // Catch:{ Exception -> 0x04bd }
            int r15 = r11.uploadStartTime     // Catch:{ Exception -> 0x04bd }
            float r15 = (float) r15     // Catch:{ Exception -> 0x04bd }
            r17 = 1168687104(0x45a8CLASSNAME, float:5400.0)
            float r15 = r15 - r17
            int r14 = (r14 > r15 ? 1 : (r14 == r15 ? 0 : -1))
            if (r14 >= 0) goto L_0x01fc
            goto L_0x01ea
        L_0x01fc:
            if (r0 == 0) goto L_0x02b7
            int r0 = (r12 > r5 ? 1 : (r12 == r5 ? 0 : -1))
            if (r0 <= 0) goto L_0x02ba
            r11.readBytesCount = r12     // Catch:{ Exception -> 0x04bd }
            int r0 = r11.uploadChunkSize     // Catch:{ Exception -> 0x04bd }
            long r14 = (long) r0     // Catch:{ Exception -> 0x04bd }
            long r14 = r12 / r14
            int r0 = (int) r14     // Catch:{ Exception -> 0x04bd }
            r11.currentPartNum = r0     // Catch:{ Exception -> 0x04bd }
            if (r10 != 0) goto L_0x0278
            r0 = 0
        L_0x020f:
            long r12 = (long) r0     // Catch:{ Exception -> 0x04bd }
            long r14 = r11.readBytesCount     // Catch:{ Exception -> 0x04bd }
            int r10 = r11.uploadChunkSize     // Catch:{ Exception -> 0x04bd }
            r18 = r3
            long r2 = (long) r10     // Catch:{ Exception -> 0x04bd }
            long r14 = r14 / r2
            int r2 = (r12 > r14 ? 1 : (r12 == r14 ? 0 : -1))
            if (r2 >= 0) goto L_0x02aa
            java.io.RandomAccessFile r2 = r11.stream     // Catch:{ Exception -> 0x04bd }
            byte[] r3 = r11.readBuffer     // Catch:{ Exception -> 0x04bd }
            int r2 = r2.read(r3)     // Catch:{ Exception -> 0x04bd }
            boolean r3 = r11.isEncrypted     // Catch:{ Exception -> 0x04bd }
            if (r3 == 0) goto L_0x0232
            int r3 = r2 % 16
            if (r3 == 0) goto L_0x0232
            int r3 = r2 % 16
            int r3 = 16 - r3
            int r3 = r3 + r8
            goto L_0x0233
        L_0x0232:
            r3 = 0
        L_0x0233:
            org.telegram.tgnet.NativeByteBuffer r10 = new org.telegram.tgnet.NativeByteBuffer     // Catch:{ Exception -> 0x04bd }
            int r12 = r2 + r3
            r10.<init>((int) r12)     // Catch:{ Exception -> 0x04bd }
            int r13 = r11.uploadChunkSize     // Catch:{ Exception -> 0x04bd }
            if (r2 != r13) goto L_0x0245
            int r13 = r11.totalPartsCount     // Catch:{ Exception -> 0x04bd }
            int r14 = r11.currentPartNum     // Catch:{ Exception -> 0x04bd }
            int r14 = r14 + r1
            if (r13 != r14) goto L_0x0247
        L_0x0245:
            r11.isLastPart = r1     // Catch:{ Exception -> 0x04bd }
        L_0x0247:
            byte[] r13 = r11.readBuffer     // Catch:{ Exception -> 0x04bd }
            r10.writeBytes(r13, r8, r2)     // Catch:{ Exception -> 0x04bd }
            boolean r2 = r11.isEncrypted     // Catch:{ Exception -> 0x04bd }
            if (r2 == 0) goto L_0x0270
            r2 = 0
        L_0x0251:
            if (r2 >= r3) goto L_0x0259
            r10.writeByte((int) r8)     // Catch:{ Exception -> 0x04bd }
            int r2 = r2 + 1
            goto L_0x0251
        L_0x0259:
            java.nio.ByteBuffer r2 = r10.buffer     // Catch:{ Exception -> 0x04bd }
            byte[] r3 = r11.key     // Catch:{ Exception -> 0x04bd }
            byte[] r13 = r11.ivChange     // Catch:{ Exception -> 0x04bd }
            r22 = 1
            r23 = 1
            r24 = 0
            r19 = r2
            r20 = r3
            r21 = r13
            r25 = r12
            org.telegram.messenger.Utilities.aesIgeEncryption(r19, r20, r21, r22, r23, r24, r25)     // Catch:{ Exception -> 0x04bd }
        L_0x0270:
            r10.reuse()     // Catch:{ Exception -> 0x04bd }
            int r0 = r0 + 1
            r3 = r18
            goto L_0x020f
        L_0x0278:
            r18 = r3
            java.io.RandomAccessFile r0 = r11.stream     // Catch:{ Exception -> 0x04bd }
            r0.seek(r12)     // Catch:{ Exception -> 0x04bd }
            boolean r0 = r11.isEncrypted     // Catch:{ Exception -> 0x04bd }
            if (r0 == 0) goto L_0x02aa
            android.content.SharedPreferences r0 = r11.preferences     // Catch:{ Exception -> 0x04bd }
            java.lang.StringBuilder r2 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x04bd }
            r2.<init>()     // Catch:{ Exception -> 0x04bd }
            java.lang.String r3 = r11.fileKey     // Catch:{ Exception -> 0x04bd }
            r2.append(r3)     // Catch:{ Exception -> 0x04bd }
            java.lang.String r3 = "_ivc"
            r2.append(r3)     // Catch:{ Exception -> 0x04bd }
            java.lang.String r2 = r2.toString()     // Catch:{ Exception -> 0x04bd }
            java.lang.String r0 = r0.getString(r2, r4)     // Catch:{ Exception -> 0x04bd }
            if (r0 == 0) goto L_0x02b2
            byte[] r0 = org.telegram.messenger.Utilities.hexToBytes(r0)     // Catch:{ Exception -> 0x04bd }
            r11.ivChange = r0     // Catch:{ Exception -> 0x04bd }
            if (r0 == 0) goto L_0x02ad
            int r0 = r0.length     // Catch:{ Exception -> 0x04bd }
            if (r0 == r7) goto L_0x02aa
            goto L_0x02ad
        L_0x02aa:
            r3 = r18
            goto L_0x02bb
        L_0x02ad:
            r11.readBytesCount = r5     // Catch:{ Exception -> 0x04bd }
            r11.currentPartNum = r8     // Catch:{ Exception -> 0x04bd }
            goto L_0x02ba
        L_0x02b2:
            r11.readBytesCount = r5     // Catch:{ Exception -> 0x04bd }
            r11.currentPartNum = r8     // Catch:{ Exception -> 0x04bd }
            goto L_0x02ba
        L_0x02b7:
            r18 = r3
            goto L_0x02aa
        L_0x02ba:
            r3 = 1
        L_0x02bb:
            if (r3 == 0) goto L_0x02f7
            boolean r0 = r11.isEncrypted     // Catch:{ Exception -> 0x04bd }
            if (r0 == 0) goto L_0x02e0
            byte[] r0 = new byte[r7]     // Catch:{ Exception -> 0x04bd }
            r11.iv = r0     // Catch:{ Exception -> 0x04bd }
            byte[] r2 = new byte[r7]     // Catch:{ Exception -> 0x04bd }
            r11.key = r2     // Catch:{ Exception -> 0x04bd }
            byte[] r2 = new byte[r7]     // Catch:{ Exception -> 0x04bd }
            r11.ivChange = r2     // Catch:{ Exception -> 0x04bd }
            java.security.SecureRandom r2 = org.telegram.messenger.Utilities.random     // Catch:{ Exception -> 0x04bd }
            r2.nextBytes(r0)     // Catch:{ Exception -> 0x04bd }
            java.security.SecureRandom r0 = org.telegram.messenger.Utilities.random     // Catch:{ Exception -> 0x04bd }
            byte[] r2 = r11.key     // Catch:{ Exception -> 0x04bd }
            r0.nextBytes(r2)     // Catch:{ Exception -> 0x04bd }
            byte[] r0 = r11.iv     // Catch:{ Exception -> 0x04bd }
            byte[] r2 = r11.ivChange     // Catch:{ Exception -> 0x04bd }
            java.lang.System.arraycopy(r0, r8, r2, r8, r7)     // Catch:{ Exception -> 0x04bd }
        L_0x02e0:
            java.security.SecureRandom r0 = org.telegram.messenger.Utilities.random     // Catch:{ Exception -> 0x04bd }
            long r2 = r0.nextLong()     // Catch:{ Exception -> 0x04bd }
            r11.currentFileId = r2     // Catch:{ Exception -> 0x04bd }
            boolean r0 = r11.nextPartFirst     // Catch:{ Exception -> 0x04bd }
            if (r0 != 0) goto L_0x02f7
            boolean r0 = r11.uploadFirstPartLater     // Catch:{ Exception -> 0x04bd }
            if (r0 != 0) goto L_0x02f7
            int r0 = r11.estimatedSize     // Catch:{ Exception -> 0x04bd }
            if (r0 != 0) goto L_0x02f7
            r27.storeFileUploadInfo()     // Catch:{ Exception -> 0x04bd }
        L_0x02f7:
            boolean r0 = r11.isEncrypted     // Catch:{ Exception -> 0x04bd }
            if (r0 == 0) goto L_0x032d
            java.lang.String r0 = "MD5"
            java.security.MessageDigest r0 = java.security.MessageDigest.getInstance(r0)     // Catch:{ Exception -> 0x0329 }
            byte[] r2 = new byte[r9]     // Catch:{ Exception -> 0x0329 }
            byte[] r3 = r11.key     // Catch:{ Exception -> 0x0329 }
            java.lang.System.arraycopy(r3, r8, r2, r8, r7)     // Catch:{ Exception -> 0x0329 }
            byte[] r3 = r11.iv     // Catch:{ Exception -> 0x0329 }
            java.lang.System.arraycopy(r3, r8, r2, r7, r7)     // Catch:{ Exception -> 0x0329 }
            byte[] r0 = r0.digest(r2)     // Catch:{ Exception -> 0x0329 }
            r2 = 0
        L_0x0312:
            r3 = 4
            if (r2 >= r3) goto L_0x032d
            int r3 = r11.fingerprint     // Catch:{ Exception -> 0x0329 }
            byte r9 = r0[r2]     // Catch:{ Exception -> 0x0329 }
            int r10 = r2 + 4
            byte r10 = r0[r10]     // Catch:{ Exception -> 0x0329 }
            r9 = r9 ^ r10
            r9 = r9 & 255(0xff, float:3.57E-43)
            int r10 = r2 * 8
            int r9 = r9 << r10
            r3 = r3 | r9
            r11.fingerprint = r3     // Catch:{ Exception -> 0x0329 }
            int r2 = r2 + 1
            goto L_0x0312
        L_0x0329:
            r0 = move-exception
            org.telegram.messenger.FileLog.e((java.lang.Throwable) r0)     // Catch:{ Exception -> 0x04bd }
        L_0x032d:
            long r2 = r11.readBytesCount     // Catch:{ Exception -> 0x04bd }
            r11.uploadedBytesCount = r2     // Catch:{ Exception -> 0x04bd }
            int r0 = r11.currentPartNum     // Catch:{ Exception -> 0x04bd }
            r11.lastSavedPartNum = r0     // Catch:{ Exception -> 0x04bd }
            boolean r0 = r11.uploadFirstPartLater     // Catch:{ Exception -> 0x04bd }
            if (r0 == 0) goto L_0x0363
            boolean r0 = r11.isBigFile     // Catch:{ Exception -> 0x04bd }
            if (r0 == 0) goto L_0x034b
            java.io.RandomAccessFile r0 = r11.stream     // Catch:{ Exception -> 0x04bd }
            int r2 = r11.uploadChunkSize     // Catch:{ Exception -> 0x04bd }
            long r2 = (long) r2     // Catch:{ Exception -> 0x04bd }
            r0.seek(r2)     // Catch:{ Exception -> 0x04bd }
            int r0 = r11.uploadChunkSize     // Catch:{ Exception -> 0x04bd }
            long r2 = (long) r0     // Catch:{ Exception -> 0x04bd }
            r11.readBytesCount = r2     // Catch:{ Exception -> 0x04bd }
            goto L_0x0354
        L_0x034b:
            java.io.RandomAccessFile r0 = r11.stream     // Catch:{ Exception -> 0x04bd }
            r2 = 1024(0x400, double:5.06E-321)
            r0.seek(r2)     // Catch:{ Exception -> 0x04bd }
            r11.readBytesCount = r2     // Catch:{ Exception -> 0x04bd }
        L_0x0354:
            r11.currentPartNum = r1     // Catch:{ Exception -> 0x04bd }
            goto L_0x0363
        L_0x0357:
            java.lang.Exception r0 = new java.lang.Exception     // Catch:{ Exception -> 0x04bd }
            r0.<init>(r10)     // Catch:{ Exception -> 0x04bd }
            throw r0     // Catch:{ Exception -> 0x04bd }
        L_0x035d:
            java.lang.Exception r0 = new java.lang.Exception     // Catch:{ Exception -> 0x04bd }
            r0.<init>(r10)     // Catch:{ Exception -> 0x04bd }
            throw r0     // Catch:{ Exception -> 0x04bd }
        L_0x0363:
            int r0 = r11.estimatedSize     // Catch:{ Exception -> 0x04bd }
            if (r0 == 0) goto L_0x0374
            long r2 = r11.readBytesCount     // Catch:{ Exception -> 0x04bd }
            int r0 = r11.uploadChunkSize     // Catch:{ Exception -> 0x04bd }
            long r9 = (long) r0     // Catch:{ Exception -> 0x04bd }
            long r2 = r2 + r9
            long r9 = r11.availableSize     // Catch:{ Exception -> 0x04bd }
            int r0 = (r2 > r9 ? 1 : (r2 == r9 ? 0 : -1))
            if (r0 <= 0) goto L_0x0374
            return
        L_0x0374:
            boolean r0 = r11.nextPartFirst     // Catch:{ Exception -> 0x04bd }
            if (r0 == 0) goto L_0x0397
            java.io.RandomAccessFile r0 = r11.stream     // Catch:{ Exception -> 0x04bd }
            r0.seek(r5)     // Catch:{ Exception -> 0x04bd }
            boolean r0 = r11.isBigFile     // Catch:{ Exception -> 0x04bd }
            if (r0 == 0) goto L_0x038a
            java.io.RandomAccessFile r0 = r11.stream     // Catch:{ Exception -> 0x04bd }
            byte[] r2 = r11.readBuffer     // Catch:{ Exception -> 0x04bd }
            int r0 = r0.read(r2)     // Catch:{ Exception -> 0x04bd }
            goto L_0x0394
        L_0x038a:
            java.io.RandomAccessFile r0 = r11.stream     // Catch:{ Exception -> 0x04bd }
            byte[] r2 = r11.readBuffer     // Catch:{ Exception -> 0x04bd }
            r3 = 1024(0x400, float:1.435E-42)
            int r0 = r0.read(r2, r8, r3)     // Catch:{ Exception -> 0x04bd }
        L_0x0394:
            r11.currentPartNum = r8     // Catch:{ Exception -> 0x04bd }
            goto L_0x039f
        L_0x0397:
            java.io.RandomAccessFile r0 = r11.stream     // Catch:{ Exception -> 0x04bd }
            byte[] r2 = r11.readBuffer     // Catch:{ Exception -> 0x04bd }
            int r0 = r0.read(r2)     // Catch:{ Exception -> 0x04bd }
        L_0x039f:
            r2 = -1
            if (r0 != r2) goto L_0x03a3
            return
        L_0x03a3:
            boolean r3 = r11.isEncrypted     // Catch:{ Exception -> 0x04bd }
            if (r3 == 0) goto L_0x03b1
            int r3 = r0 % 16
            if (r3 == 0) goto L_0x03b1
            int r3 = r0 % 16
            int r3 = 16 - r3
            int r3 = r3 + r8
            goto L_0x03b2
        L_0x03b1:
            r3 = 0
        L_0x03b2:
            org.telegram.tgnet.NativeByteBuffer r5 = new org.telegram.tgnet.NativeByteBuffer     // Catch:{ Exception -> 0x04bd }
            int r6 = r0 + r3
            r5.<init>((int) r6)     // Catch:{ Exception -> 0x04bd }
            boolean r9 = r11.nextPartFirst     // Catch:{ Exception -> 0x04bd }
            if (r9 != 0) goto L_0x03cc
            int r9 = r11.uploadChunkSize     // Catch:{ Exception -> 0x04bd }
            if (r0 != r9) goto L_0x03cc
            int r9 = r11.estimatedSize     // Catch:{ Exception -> 0x04bd }
            if (r9 != 0) goto L_0x03d7
            int r9 = r11.totalPartsCount     // Catch:{ Exception -> 0x04bd }
            int r10 = r11.currentPartNum     // Catch:{ Exception -> 0x04bd }
            int r10 = r10 + r1
            if (r9 != r10) goto L_0x03d7
        L_0x03cc:
            boolean r9 = r11.uploadFirstPartLater     // Catch:{ Exception -> 0x04bd }
            if (r9 == 0) goto L_0x03d5
            r11.nextPartFirst = r1     // Catch:{ Exception -> 0x04bd }
            r11.uploadFirstPartLater = r8     // Catch:{ Exception -> 0x04bd }
            goto L_0x03d7
        L_0x03d5:
            r11.isLastPart = r1     // Catch:{ Exception -> 0x04bd }
        L_0x03d7:
            byte[] r9 = r11.readBuffer     // Catch:{ Exception -> 0x04bd }
            r5.writeBytes(r9, r8, r0)     // Catch:{ Exception -> 0x04bd }
            boolean r9 = r11.isEncrypted     // Catch:{ Exception -> 0x04bd }
            if (r9 == 0) goto L_0x0414
            r4 = 0
        L_0x03e1:
            if (r4 >= r3) goto L_0x03e9
            r5.writeByte((int) r8)     // Catch:{ Exception -> 0x04bd }
            int r4 = r4 + 1
            goto L_0x03e1
        L_0x03e9:
            java.nio.ByteBuffer r3 = r5.buffer     // Catch:{ Exception -> 0x04bd }
            byte[] r4 = r11.key     // Catch:{ Exception -> 0x04bd }
            byte[] r9 = r11.ivChange     // Catch:{ Exception -> 0x04bd }
            r21 = 1
            r22 = 1
            r23 = 0
            r18 = r3
            r19 = r4
            r20 = r9
            r24 = r6
            org.telegram.messenger.Utilities.aesIgeEncryption(r18, r19, r20, r21, r22, r23, r24)     // Catch:{ Exception -> 0x04bd }
            java.util.ArrayList<byte[]> r3 = r11.freeRequestIvs     // Catch:{ Exception -> 0x04bd }
            java.lang.Object r3 = r3.get(r8)     // Catch:{ Exception -> 0x04bd }
            byte[] r3 = (byte[]) r3     // Catch:{ Exception -> 0x04bd }
            byte[] r4 = r11.ivChange     // Catch:{ Exception -> 0x04bd }
            java.lang.System.arraycopy(r4, r8, r3, r8, r7)     // Catch:{ Exception -> 0x04bd }
            java.util.ArrayList<byte[]> r4 = r11.freeRequestIvs     // Catch:{ Exception -> 0x04bd }
            r4.remove(r8)     // Catch:{ Exception -> 0x04bd }
            r6 = r3
            goto L_0x0415
        L_0x0414:
            r6 = r4
        L_0x0415:
            boolean r3 = r11.isBigFile     // Catch:{ Exception -> 0x04bd }
            if (r3 == 0) goto L_0x0437
            org.telegram.tgnet.TLRPC$TL_upload_saveBigFilePart r3 = new org.telegram.tgnet.TLRPC$TL_upload_saveBigFilePart     // Catch:{ Exception -> 0x04bd }
            r3.<init>()     // Catch:{ Exception -> 0x04bd }
            int r4 = r11.currentPartNum     // Catch:{ Exception -> 0x04bd }
            r3.file_part = r4     // Catch:{ Exception -> 0x04bd }
            long r9 = r11.currentFileId     // Catch:{ Exception -> 0x04bd }
            r3.file_id = r9     // Catch:{ Exception -> 0x04bd }
            int r7 = r11.estimatedSize     // Catch:{ Exception -> 0x04bd }
            if (r7 == 0) goto L_0x042d
            r3.file_total_parts = r2     // Catch:{ Exception -> 0x04bd }
            goto L_0x0431
        L_0x042d:
            int r2 = r11.totalPartsCount     // Catch:{ Exception -> 0x04bd }
            r3.file_total_parts = r2     // Catch:{ Exception -> 0x04bd }
        L_0x0431:
            r3.bytes = r5     // Catch:{ Exception -> 0x04bd }
            r19 = r3
            r9 = r4
            goto L_0x0449
        L_0x0437:
            org.telegram.tgnet.TLRPC$TL_upload_saveFilePart r3 = new org.telegram.tgnet.TLRPC$TL_upload_saveFilePart     // Catch:{ Exception -> 0x04bd }
            r3.<init>()     // Catch:{ Exception -> 0x04bd }
            int r2 = r11.currentPartNum     // Catch:{ Exception -> 0x04bd }
            r3.file_part = r2     // Catch:{ Exception -> 0x04bd }
            long r9 = r11.currentFileId     // Catch:{ Exception -> 0x04bd }
            r3.file_id = r9     // Catch:{ Exception -> 0x04bd }
            r3.bytes = r5     // Catch:{ Exception -> 0x04bd }
            r9 = r2
            r19 = r3
        L_0x0449:
            boolean r2 = r11.isLastPart     // Catch:{ Exception -> 0x04bd }
            if (r2 == 0) goto L_0x045f
            boolean r2 = r11.nextPartFirst     // Catch:{ Exception -> 0x04bd }
            if (r2 == 0) goto L_0x045f
            r11.nextPartFirst = r8     // Catch:{ Exception -> 0x04bd }
            int r2 = r11.totalPartsCount     // Catch:{ Exception -> 0x04bd }
            int r2 = r2 - r1
            r11.currentPartNum = r2     // Catch:{ Exception -> 0x04bd }
            java.io.RandomAccessFile r2 = r11.stream     // Catch:{ Exception -> 0x04bd }
            long r3 = r11.totalFileSize     // Catch:{ Exception -> 0x04bd }
            r2.seek(r3)     // Catch:{ Exception -> 0x04bd }
        L_0x045f:
            long r2 = r11.readBytesCount     // Catch:{ Exception -> 0x04bd }
            long r4 = (long) r0     // Catch:{ Exception -> 0x04bd }
            long r2 = r2 + r4
            r11.readBytesCount = r2     // Catch:{ Exception -> 0x04bd }
            int r2 = r11.currentPartNum
            int r2 = r2 + r1
            r11.currentPartNum = r2
            int r2 = r11.currentUploadRequetsCount
            int r2 = r2 + r1
            r11.currentUploadRequetsCount = r2
            int r12 = r11.requestNum
            int r1 = r12 + 1
            r11.requestNum = r1
            int r1 = r9 + r0
            long r13 = (long) r1
            int r1 = r19.getObjectSize()
            r2 = 4
            int r4 = r1 + 4
            int r3 = r11.operationGuid
            boolean r1 = r11.slowNetwork
            if (r1 == 0) goto L_0x0488
            r25 = 4
            goto L_0x048f
        L_0x0488:
            int r1 = r12 % 4
            int r1 = r1 << 16
            r2 = r2 | r1
            r25 = r2
        L_0x048f:
            int r1 = r11.currentAccount
            org.telegram.tgnet.ConnectionsManager r18 = org.telegram.tgnet.ConnectionsManager.getInstance(r1)
            org.telegram.messenger.-$$Lambda$FileUploadOperation$06_JEIqHeimaCLASSNAMEYhLgoyYozb2A r20 = new org.telegram.messenger.-$$Lambda$FileUploadOperation$06_JEIqHeimaCLASSNAMEYhLgoyYozb2A
            r1 = r20
            r2 = r27
            r5 = r6
            r6 = r12
            r7 = r0
            r8 = r9
            r9 = r13
            r1.<init>(r3, r4, r5, r6, r7, r8, r9)
            r21 = 0
            org.telegram.messenger.-$$Lambda$FileUploadOperation$runCwhotZAORV0DsK4gN0v7nL14 r0 = new org.telegram.messenger.-$$Lambda$FileUploadOperation$runCwhotZAORV0DsK4gN0v7nL14
            r0.<init>()
            r23 = 0
            r24 = 2147483647(0x7fffffff, float:NaN)
            r26 = 1
            r22 = r0
            int r0 = r18.sendRequest(r19, r20, r21, r22, r23, r24, r25, r26)
            android.util.SparseIntArray r1 = r11.requestTokens
            r1.put(r12, r0)
            return
        L_0x04bd:
            r0 = move-exception
            org.telegram.messenger.FileLog.e((java.lang.Throwable) r0)
            r1 = 4
            r11.state = r1
            org.telegram.messenger.FileUploadOperation$FileUploadOperationDelegate r0 = r11.delegate
            r0.didFailedUploadingFile(r11)
            r27.cleanup()
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.messenger.FileUploadOperation.startUploadRequest():void");
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$startUploadRequest$4 */
    public /* synthetic */ void lambda$startUploadRequest$4$FileUploadOperation(int i, int i2, byte[] bArr, int i3, int i4, int i5, long j, TLObject tLObject, TLRPC$TL_error tLRPC$TL_error) {
        long j2;
        TLRPC$InputEncryptedFile tLRPC$InputEncryptedFile;
        TLRPC$InputFile tLRPC$InputFile;
        int i6 = i2;
        byte[] bArr2 = bArr;
        int i7 = i5;
        TLObject tLObject2 = tLObject;
        if (i == this.operationGuid) {
            int currentNetworkType = tLObject2 != null ? tLObject2.networkType : ApplicationLoader.getCurrentNetworkType();
            int i8 = this.currentType;
            if (i8 == 50331648) {
                StatsController.getInstance(this.currentAccount).incrementSentBytesCount(currentNetworkType, 3, (long) i6);
            } else if (i8 == 33554432) {
                StatsController.getInstance(this.currentAccount).incrementSentBytesCount(currentNetworkType, 2, (long) i6);
            } else if (i8 == 16777216) {
                StatsController.getInstance(this.currentAccount).incrementSentBytesCount(currentNetworkType, 4, (long) i6);
            } else if (i8 == 67108864) {
                StatsController.getInstance(this.currentAccount).incrementSentBytesCount(currentNetworkType, 5, (long) i6);
            }
            if (bArr2 != null) {
                this.freeRequestIvs.add(bArr2);
            }
            this.requestTokens.delete(i3);
            if (!(tLObject2 instanceof TLRPC$TL_boolTrue)) {
                this.state = 4;
                this.delegate.didFailedUploadingFile(this);
                cleanup();
            } else if (this.state == 1) {
                this.uploadedBytesCount += (long) i4;
                int i9 = this.estimatedSize;
                if (i9 != 0) {
                    j2 = Math.max(this.availableSize, (long) i9);
                } else {
                    j2 = this.totalFileSize;
                }
                this.delegate.didChangedUploadProgress(this, this.uploadedBytesCount, j2);
                int i10 = this.currentUploadRequetsCount - 1;
                this.currentUploadRequetsCount = i10;
                if (this.isLastPart && i10 == 0 && this.state == 1) {
                    this.state = 3;
                    if (this.key == null) {
                        if (this.isBigFile) {
                            tLRPC$InputFile = new TLRPC$TL_inputFileBig();
                        } else {
                            tLRPC$InputFile = new TLRPC$TL_inputFile();
                            tLRPC$InputFile.md5_checksum = "";
                        }
                        tLRPC$InputFile.parts = this.currentPartNum;
                        tLRPC$InputFile.id = this.currentFileId;
                        String str = this.uploadingFilePath;
                        tLRPC$InputFile.name = str.substring(str.lastIndexOf("/") + 1);
                        this.delegate.didFinishUploadingFile(this, tLRPC$InputFile, (TLRPC$InputEncryptedFile) null, (byte[]) null, (byte[]) null);
                        cleanup();
                    } else {
                        if (this.isBigFile) {
                            tLRPC$InputEncryptedFile = new TLRPC$TL_inputEncryptedFileBigUploaded();
                        } else {
                            tLRPC$InputEncryptedFile = new TLRPC$TL_inputEncryptedFileUploaded();
                            tLRPC$InputEncryptedFile.md5_checksum = "";
                        }
                        tLRPC$InputEncryptedFile.parts = this.currentPartNum;
                        tLRPC$InputEncryptedFile.id = this.currentFileId;
                        tLRPC$InputEncryptedFile.key_fingerprint = this.fingerprint;
                        this.delegate.didFinishUploadingFile(this, (TLRPC$InputFile) null, tLRPC$InputEncryptedFile, this.key, this.iv);
                        cleanup();
                    }
                    int i11 = this.currentType;
                    if (i11 == 50331648) {
                        StatsController.getInstance(this.currentAccount).incrementSentItemsCount(ApplicationLoader.getCurrentNetworkType(), 3, 1);
                    } else if (i11 == 33554432) {
                        StatsController.getInstance(this.currentAccount).incrementSentItemsCount(ApplicationLoader.getCurrentNetworkType(), 2, 1);
                    } else if (i11 == 16777216) {
                        StatsController.getInstance(this.currentAccount).incrementSentItemsCount(ApplicationLoader.getCurrentNetworkType(), 4, 1);
                    } else if (i11 == 67108864) {
                        StatsController.getInstance(this.currentAccount).incrementSentItemsCount(ApplicationLoader.getCurrentNetworkType(), 5, 1);
                    }
                } else if (i10 < this.maxRequestsCount) {
                    if (this.estimatedSize == 0 && !this.uploadFirstPartLater && !this.nextPartFirst) {
                        if (this.saveInfoTimes >= 4) {
                            this.saveInfoTimes = 0;
                        }
                        int i12 = this.lastSavedPartNum;
                        if (i7 == i12) {
                            this.lastSavedPartNum = i12 + 1;
                            long j3 = j;
                            while (true) {
                                UploadCachedResult uploadCachedResult = this.cachedResults.get(this.lastSavedPartNum);
                                if (uploadCachedResult == null) {
                                    break;
                                }
                                j3 = uploadCachedResult.bytesOffset;
                                bArr2 = uploadCachedResult.iv;
                                this.cachedResults.remove(this.lastSavedPartNum);
                                this.lastSavedPartNum++;
                            }
                            boolean z = this.isBigFile;
                            if ((z && j3 % 1048576 == 0) || (!z && this.saveInfoTimes == 0)) {
                                SharedPreferences.Editor edit = this.preferences.edit();
                                edit.putLong(this.fileKey + "_uploaded", j3);
                                if (this.isEncrypted) {
                                    edit.putString(this.fileKey + "_ivc", Utilities.bytesToHex(bArr2));
                                }
                                edit.commit();
                            }
                        } else {
                            UploadCachedResult uploadCachedResult2 = new UploadCachedResult();
                            long unused = uploadCachedResult2.bytesOffset = j;
                            if (bArr2 != null) {
                                byte[] unused2 = uploadCachedResult2.iv = new byte[32];
                                System.arraycopy(bArr2, 0, uploadCachedResult2.iv, 0, 32);
                            }
                            this.cachedResults.put(i7, uploadCachedResult2);
                        }
                        this.saveInfoTimes++;
                    }
                    startUploadRequest();
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$startUploadRequest$6 */
    public /* synthetic */ void lambda$startUploadRequest$6$FileUploadOperation() {
        Utilities.stageQueue.postRunnable(new Runnable() {
            public final void run() {
                FileUploadOperation.this.lambda$null$5$FileUploadOperation();
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$null$5 */
    public /* synthetic */ void lambda$null$5$FileUploadOperation() {
        if (this.currentUploadRequetsCount < this.maxRequestsCount) {
            startUploadRequest();
        }
    }
}
