package org.telegram.messenger;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import org.telegram.messenger.FileLoadOperation.FileLoadOperationDelegate;
import org.telegram.messenger.FileUploadOperation.FileUploadOperationDelegate;
import org.telegram.messenger.exoplayer2.util.MimeTypes;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.Document;
import org.telegram.tgnet.TLRPC.DocumentAttribute;
import org.telegram.tgnet.TLRPC.FileLocation;
import org.telegram.tgnet.TLRPC.InputEncryptedFile;
import org.telegram.tgnet.TLRPC.InputFile;
import org.telegram.tgnet.TLRPC.Message;
import org.telegram.tgnet.TLRPC.PhotoSize;
import org.telegram.tgnet.TLRPC.TL_documentAttributeFilename;
import org.telegram.tgnet.TLRPC.TL_fileLocationUnavailable;
import org.telegram.tgnet.TLRPC.TL_messageMediaDocument;
import org.telegram.tgnet.TLRPC.TL_messageMediaPhoto;
import org.telegram.tgnet.TLRPC.TL_messageMediaWebPage;
import org.telegram.tgnet.TLRPC.TL_messageService;
import org.telegram.tgnet.TLRPC.TL_photoCachedSize;

public class FileLoader {
    private static volatile FileLoader Instance = null;
    public static final int MEDIA_DIR_AUDIO = 1;
    public static final int MEDIA_DIR_CACHE = 4;
    public static final int MEDIA_DIR_DOCUMENT = 3;
    public static final int MEDIA_DIR_IMAGE = 0;
    public static final int MEDIA_DIR_VIDEO = 2;
    private LinkedList<FileLoadOperation> audioLoadOperationQueue = new LinkedList();
    private int currentAudioLoadOperationsCount = 0;
    private int currentLoadOperationsCount = 0;
    private int currentPhotoLoadOperationsCount = 0;
    private int currentUploadOperationsCount = 0;
    private int currentUploadSmallOperationsCount = 0;
    private FileLoaderDelegate delegate = null;
    private volatile DispatchQueue fileLoaderQueue = new DispatchQueue("fileUploadQueue");
    private ConcurrentHashMap<String, FileLoadOperation> loadOperationPaths = new ConcurrentHashMap();
    private LinkedList<FileLoadOperation> loadOperationQueue = new LinkedList();
    private HashMap<Integer, File> mediaDirs = null;
    private LinkedList<FileLoadOperation> photoLoadOperationQueue = new LinkedList();
    private ConcurrentHashMap<String, FileUploadOperation> uploadOperationPaths = new ConcurrentHashMap();
    private ConcurrentHashMap<String, FileUploadOperation> uploadOperationPathsEnc = new ConcurrentHashMap();
    private LinkedList<FileUploadOperation> uploadOperationQueue = new LinkedList();
    private HashMap<String, Long> uploadSizes = new HashMap();
    private LinkedList<FileUploadOperation> uploadSmallOperationQueue = new LinkedList();

    public interface FileLoaderDelegate {
        void fileDidFailedLoad(String str, int i);

        void fileDidFailedUpload(String str, boolean z);

        void fileDidLoaded(String str, File file, int i);

        void fileDidUploaded(String str, InputFile inputFile, InputEncryptedFile inputEncryptedFile, byte[] bArr, byte[] bArr2, long j);

        void fileLoadProgressChanged(String str, float f);

        void fileUploadProgressChanged(String str, float f, boolean z);
    }

    public static FileLoader getInstance() {
        FileLoader localInstance = Instance;
        if (localInstance == null) {
            synchronized (FileLoader.class) {
                try {
                    localInstance = Instance;
                    if (localInstance == null) {
                        FileLoader localInstance2 = new FileLoader();
                        try {
                            Instance = localInstance2;
                            localInstance = localInstance2;
                        } catch (Throwable th) {
                            Throwable th2 = th;
                            localInstance = localInstance2;
                            throw th2;
                        }
                    }
                } catch (Throwable th3) {
                    th2 = th3;
                    throw th2;
                }
            }
        }
        return localInstance;
    }

    public void setMediaDirs(HashMap<Integer, File> dirs) {
        this.mediaDirs = dirs;
    }

    public File checkDirectory(int type) {
        return (File) this.mediaDirs.get(Integer.valueOf(type));
    }

    public File getDirectory(int type) {
        File dir = (File) this.mediaDirs.get(Integer.valueOf(type));
        if (dir == null && type != 4) {
            dir = (File) this.mediaDirs.get(Integer.valueOf(4));
        }
        try {
            if (!dir.isDirectory()) {
                dir.mkdirs();
            }
        } catch (Exception e) {
        }
        return dir;
    }

    public void cancelUploadFile(final String location, final boolean enc) {
        this.fileLoaderQueue.postRunnable(new Runnable() {
            public void run() {
                FileUploadOperation operation;
                if (enc) {
                    operation = (FileUploadOperation) FileLoader.this.uploadOperationPathsEnc.get(location);
                } else {
                    operation = (FileUploadOperation) FileLoader.this.uploadOperationPaths.get(location);
                }
                FileLoader.this.uploadSizes.remove(location);
                if (operation != null) {
                    FileLoader.this.uploadOperationPathsEnc.remove(location);
                    FileLoader.this.uploadOperationQueue.remove(operation);
                    FileLoader.this.uploadSmallOperationQueue.remove(operation);
                    operation.cancel();
                }
            }
        });
    }

    public void checkUploadNewDataAvailable(String location, boolean encrypted, long finalSize) {
        final boolean z = encrypted;
        final String str = location;
        final long j = finalSize;
        this.fileLoaderQueue.postRunnable(new Runnable() {
            public void run() {
                FileUploadOperation operation;
                if (z) {
                    operation = (FileUploadOperation) FileLoader.this.uploadOperationPathsEnc.get(str);
                } else {
                    operation = (FileUploadOperation) FileLoader.this.uploadOperationPaths.get(str);
                }
                if (operation != null) {
                    operation.checkNewDataAvailable(j);
                } else if (j != 0) {
                    FileLoader.this.uploadSizes.put(str, Long.valueOf(j));
                }
            }
        });
    }

    public void uploadFile(String location, boolean encrypted, boolean small, int type) {
        uploadFile(location, encrypted, small, 0, type);
    }

    public void uploadFile(String location, boolean encrypted, boolean small, int estimatedSize, int type) {
        if (location != null) {
            final boolean z = encrypted;
            final String str = location;
            final int i = estimatedSize;
            final int i2 = type;
            final boolean z2 = small;
            this.fileLoaderQueue.postRunnable(new Runnable() {
                public void run() {
                    if (z) {
                        if (FileLoader.this.uploadOperationPathsEnc.containsKey(str)) {
                            return;
                        }
                    } else if (FileLoader.this.uploadOperationPaths.containsKey(str)) {
                        return;
                    }
                    int esimated = i;
                    if (!(esimated == 0 || ((Long) FileLoader.this.uploadSizes.get(str)) == null)) {
                        esimated = 0;
                        FileLoader.this.uploadSizes.remove(str);
                    }
                    FileUploadOperation operation = new FileUploadOperation(str, z, esimated, i2);
                    if (z) {
                        FileLoader.this.uploadOperationPathsEnc.put(str, operation);
                    } else {
                        FileLoader.this.uploadOperationPaths.put(str, operation);
                    }
                    operation.setDelegate(new FileUploadOperationDelegate() {
                        public void didFinishUploadingFile(FileUploadOperation operation, InputFile inputFile, InputEncryptedFile inputEncryptedFile, byte[] key, byte[] iv) {
                            final InputFile inputFile2 = inputFile;
                            final InputEncryptedFile inputEncryptedFile2 = inputEncryptedFile;
                            final byte[] bArr = key;
                            final byte[] bArr2 = iv;
                            final FileUploadOperation fileUploadOperation = operation;
                            FileLoader.this.fileLoaderQueue.postRunnable(new Runnable() {
                                public void run() {
                                    if (z) {
                                        FileLoader.this.uploadOperationPathsEnc.remove(str);
                                    } else {
                                        FileLoader.this.uploadOperationPaths.remove(str);
                                    }
                                    FileUploadOperation operation;
                                    if (z2) {
                                        FileLoader.this.currentUploadSmallOperationsCount = FileLoader.this.currentUploadSmallOperationsCount - 1;
                                        if (FileLoader.this.currentUploadSmallOperationsCount < 1) {
                                            operation = (FileUploadOperation) FileLoader.this.uploadSmallOperationQueue.poll();
                                            if (operation != null) {
                                                FileLoader.this.currentUploadSmallOperationsCount = FileLoader.this.currentUploadSmallOperationsCount + 1;
                                                operation.start();
                                            }
                                        }
                                    } else {
                                        FileLoader.this.currentUploadOperationsCount = FileLoader.this.currentUploadOperationsCount - 1;
                                        if (FileLoader.this.currentUploadOperationsCount < 1) {
                                            operation = (FileUploadOperation) FileLoader.this.uploadOperationQueue.poll();
                                            if (operation != null) {
                                                FileLoader.this.currentUploadOperationsCount = FileLoader.this.currentUploadOperationsCount + 1;
                                                operation.start();
                                            }
                                        }
                                    }
                                    if (FileLoader.this.delegate != null) {
                                        FileLoader.this.delegate.fileDidUploaded(str, inputFile2, inputEncryptedFile2, bArr, bArr2, fileUploadOperation.getTotalFileSize());
                                    }
                                }
                            });
                        }

                        public void didFailedUploadingFile(FileUploadOperation operation) {
                            FileLoader.this.fileLoaderQueue.postRunnable(new Runnable() {
                                public void run() {
                                    if (z) {
                                        FileLoader.this.uploadOperationPathsEnc.remove(str);
                                    } else {
                                        FileLoader.this.uploadOperationPaths.remove(str);
                                    }
                                    if (FileLoader.this.delegate != null) {
                                        FileLoader.this.delegate.fileDidFailedUpload(str, z);
                                    }
                                    FileUploadOperation operation;
                                    if (z2) {
                                        FileLoader.this.currentUploadSmallOperationsCount = FileLoader.this.currentUploadSmallOperationsCount - 1;
                                        if (FileLoader.this.currentUploadSmallOperationsCount < 1) {
                                            operation = (FileUploadOperation) FileLoader.this.uploadSmallOperationQueue.poll();
                                            if (operation != null) {
                                                FileLoader.this.currentUploadSmallOperationsCount = FileLoader.this.currentUploadSmallOperationsCount + 1;
                                                operation.start();
                                                return;
                                            }
                                            return;
                                        }
                                        return;
                                    }
                                    FileLoader.this.currentUploadOperationsCount = FileLoader.this.currentUploadOperationsCount - 1;
                                    if (FileLoader.this.currentUploadOperationsCount < 1) {
                                        operation = (FileUploadOperation) FileLoader.this.uploadOperationQueue.poll();
                                        if (operation != null) {
                                            FileLoader.this.currentUploadOperationsCount = FileLoader.this.currentUploadOperationsCount + 1;
                                            operation.start();
                                        }
                                    }
                                }
                            });
                        }

                        public void didChangedUploadProgress(FileUploadOperation operation, float progress) {
                            if (FileLoader.this.delegate != null) {
                                FileLoader.this.delegate.fileUploadProgressChanged(str, progress, z);
                            }
                        }
                    });
                    if (z2) {
                        if (FileLoader.this.currentUploadSmallOperationsCount < 1) {
                            FileLoader.this.currentUploadSmallOperationsCount = FileLoader.this.currentUploadSmallOperationsCount + 1;
                            operation.start();
                            return;
                        }
                        FileLoader.this.uploadSmallOperationQueue.add(operation);
                    } else if (FileLoader.this.currentUploadOperationsCount < 1) {
                        FileLoader.this.currentUploadOperationsCount = FileLoader.this.currentUploadOperationsCount + 1;
                        operation.start();
                    } else {
                        FileLoader.this.uploadOperationQueue.add(operation);
                    }
                }
            });
        }
    }

    public void cancelLoadFile(Document document) {
        cancelLoadFile(document, null, null);
    }

    public void cancelLoadFile(PhotoSize photo) {
        cancelLoadFile(null, photo.location, null);
    }

    public void cancelLoadFile(FileLocation location, String ext) {
        cancelLoadFile(null, location, ext);
    }

    private void cancelLoadFile(final Document document, final FileLocation location, final String locationExt) {
        if (location != null || document != null) {
            this.fileLoaderQueue.postRunnable(new Runnable() {
                public void run() {
                    String fileName = null;
                    if (location != null) {
                        fileName = FileLoader.getAttachFileName(location, locationExt);
                    } else if (document != null) {
                        fileName = FileLoader.getAttachFileName(document);
                    }
                    if (fileName != null) {
                        FileLoadOperation operation = (FileLoadOperation) FileLoader.this.loadOperationPaths.remove(fileName);
                        if (operation != null) {
                            if (MessageObject.isVoiceDocument(document)) {
                                if (!FileLoader.this.audioLoadOperationQueue.remove(operation)) {
                                    FileLoader.this.currentAudioLoadOperationsCount = FileLoader.this.currentAudioLoadOperationsCount - 1;
                                }
                            } else if (location != null) {
                                if (!FileLoader.this.photoLoadOperationQueue.remove(operation)) {
                                    FileLoader.this.currentPhotoLoadOperationsCount = FileLoader.this.currentPhotoLoadOperationsCount - 1;
                                }
                            } else if (!FileLoader.this.loadOperationQueue.remove(operation)) {
                                FileLoader.this.currentLoadOperationsCount = FileLoader.this.currentLoadOperationsCount - 1;
                            }
                            operation.cancel();
                        }
                    }
                }
            });
        }
    }

    public boolean isLoadingFile(final String fileName) {
        final Semaphore semaphore = new Semaphore(0);
        final Boolean[] result = new Boolean[1];
        this.fileLoaderQueue.postRunnable(new Runnable() {
            public void run() {
                result[0] = Boolean.valueOf(FileLoader.this.loadOperationPaths.containsKey(fileName));
                semaphore.release();
            }
        });
        try {
            semaphore.acquire();
        } catch (Throwable e) {
            FileLog.e("tmessages", e);
        }
        return result[0].booleanValue();
    }

    public void loadFile(PhotoSize photo, String ext, boolean cacheOnly) {
        boolean z;
        FileLocation fileLocation = photo.location;
        int i = photo.size;
        if (cacheOnly || ((photo != null && photo.size == 0) || photo.location.key != null)) {
            z = true;
        } else {
            z = false;
        }
        loadFile(null, fileLocation, ext, i, false, z);
    }

    public void loadFile(Document document, boolean force, boolean cacheOnly) {
        boolean z;
        if (cacheOnly || !(document == null || document.key == null)) {
            z = true;
        } else {
            z = false;
        }
        loadFile(document, null, null, 0, force, z);
    }

    public void loadFile(FileLocation location, String ext, int size, boolean cacheOnly) {
        boolean z = cacheOnly || size == 0 || !(location == null || location.key == null);
        loadFile(null, location, ext, size, true, z);
    }

    private void loadFile(Document document, FileLocation location, String locationExt, int locationSize, boolean force, boolean cacheOnly) {
        final FileLocation fileLocation = location;
        final String str = locationExt;
        final Document document2 = document;
        final boolean z = force;
        final int i = locationSize;
        final boolean z2 = cacheOnly;
        this.fileLoaderQueue.postRunnable(new Runnable() {
            public void run() {
                String fileName = null;
                if (fileLocation != null) {
                    fileName = FileLoader.getAttachFileName(fileLocation, str);
                } else if (document2 != null) {
                    fileName = FileLoader.getAttachFileName(document2);
                }
                if (fileName != null && !fileName.contains("-2147483648")) {
                    FileLoadOperation operation = (FileLoadOperation) FileLoader.this.loadOperationPaths.get(fileName);
                    if (operation == null) {
                        File tempDir = FileLoader.this.getDirectory(4);
                        File storeDir = tempDir;
                        int type = 4;
                        if (fileLocation != null) {
                            operation = new FileLoadOperation(fileLocation, str, i);
                            type = 0;
                        } else if (document2 != null) {
                            operation = new FileLoadOperation(document2);
                            if (MessageObject.isVoiceDocument(document2)) {
                                type = 1;
                            } else if (MessageObject.isVideoDocument(document2)) {
                                type = 2;
                            } else {
                                type = 3;
                            }
                        }
                        if (!z2) {
                            storeDir = FileLoader.this.getDirectory(type);
                        }
                        operation.setPaths(storeDir, tempDir);
                        final String finalFileName = fileName;
                        final int finalType = type;
                        operation.setDelegate(new FileLoadOperationDelegate() {
                            public void didFinishLoadingFile(FileLoadOperation operation, File finalFile) {
                                if (FileLoader.this.delegate != null) {
                                    FileLoader.this.delegate.fileDidLoaded(finalFileName, finalFile, finalType);
                                }
                                FileLoader.this.checkDownloadQueue(document2, fileLocation, finalFileName);
                            }

                            public void didFailedLoadingFile(FileLoadOperation operation, int reason) {
                                FileLoader.this.checkDownloadQueue(document2, fileLocation, finalFileName);
                                if (FileLoader.this.delegate != null) {
                                    FileLoader.this.delegate.fileDidFailedLoad(finalFileName, reason);
                                }
                            }

                            public void didChangedLoadProgress(FileLoadOperation operation, float progress) {
                                if (FileLoader.this.delegate != null) {
                                    FileLoader.this.delegate.fileLoadProgressChanged(finalFileName, progress);
                                }
                            }
                        });
                        FileLoader.this.loadOperationPaths.put(fileName, operation);
                        int maxCount = z ? 3 : 1;
                        if (type == 1) {
                            if (FileLoader.this.currentAudioLoadOperationsCount < maxCount) {
                                if (operation.start()) {
                                    FileLoader.this.currentAudioLoadOperationsCount = FileLoader.this.currentAudioLoadOperationsCount + 1;
                                }
                            } else if (z) {
                                FileLoader.this.audioLoadOperationQueue.add(0, operation);
                            } else {
                                FileLoader.this.audioLoadOperationQueue.add(operation);
                            }
                        } else if (fileLocation != null) {
                            if (FileLoader.this.currentPhotoLoadOperationsCount < maxCount) {
                                if (operation.start()) {
                                    FileLoader.this.currentPhotoLoadOperationsCount = FileLoader.this.currentPhotoLoadOperationsCount + 1;
                                }
                            } else if (z) {
                                FileLoader.this.photoLoadOperationQueue.add(0, operation);
                            } else {
                                FileLoader.this.photoLoadOperationQueue.add(operation);
                            }
                        } else if (FileLoader.this.currentLoadOperationsCount < maxCount) {
                            if (operation.start()) {
                                FileLoader.this.currentLoadOperationsCount = FileLoader.this.currentLoadOperationsCount + 1;
                            }
                        } else if (z) {
                            FileLoader.this.loadOperationQueue.add(0, operation);
                        } else {
                            FileLoader.this.loadOperationQueue.add(operation);
                        }
                    } else if (z) {
                        LinkedList<FileLoadOperation> downloadQueue;
                        operation.setForceRequest(true);
                        if (MessageObject.isVoiceDocument(document2)) {
                            downloadQueue = FileLoader.this.audioLoadOperationQueue;
                        } else if (fileLocation != null) {
                            downloadQueue = FileLoader.this.photoLoadOperationQueue;
                        } else {
                            downloadQueue = FileLoader.this.loadOperationQueue;
                        }
                        if (downloadQueue != null) {
                            int index = downloadQueue.indexOf(operation);
                            if (index > 0) {
                                downloadQueue.remove(index);
                                downloadQueue.add(0, operation);
                            }
                        }
                    }
                }
            }
        });
    }

    private void checkDownloadQueue(final Document document, final FileLocation location, final String arg1) {
        this.fileLoaderQueue.postRunnable(new Runnable() {
            public void run() {
                FileLoadOperation operation = (FileLoadOperation) FileLoader.this.loadOperationPaths.remove(arg1);
                int maxCount;
                if (MessageObject.isVoiceDocument(document)) {
                    if (operation != null) {
                        if (operation.wasStarted()) {
                            FileLoader.this.currentAudioLoadOperationsCount = FileLoader.this.currentAudioLoadOperationsCount - 1;
                        } else {
                            FileLoader.this.audioLoadOperationQueue.remove(operation);
                        }
                    }
                    while (!FileLoader.this.audioLoadOperationQueue.isEmpty()) {
                        if (((FileLoadOperation) FileLoader.this.audioLoadOperationQueue.get(0)).isForceRequest()) {
                            maxCount = 3;
                        } else {
                            maxCount = 1;
                        }
                        if (FileLoader.this.currentAudioLoadOperationsCount < maxCount) {
                            operation = (FileLoadOperation) FileLoader.this.audioLoadOperationQueue.poll();
                            if (operation != null && operation.start()) {
                                FileLoader.this.currentAudioLoadOperationsCount = FileLoader.this.currentAudioLoadOperationsCount + 1;
                            }
                        } else {
                            return;
                        }
                    }
                } else if (location != null) {
                    if (operation != null) {
                        if (operation.wasStarted()) {
                            FileLoader.this.currentPhotoLoadOperationsCount = FileLoader.this.currentPhotoLoadOperationsCount - 1;
                        } else {
                            FileLoader.this.photoLoadOperationQueue.remove(operation);
                        }
                    }
                    while (!FileLoader.this.photoLoadOperationQueue.isEmpty()) {
                        if (((FileLoadOperation) FileLoader.this.photoLoadOperationQueue.get(0)).isForceRequest()) {
                            maxCount = 3;
                        } else {
                            maxCount = 1;
                        }
                        if (FileLoader.this.currentPhotoLoadOperationsCount < maxCount) {
                            operation = (FileLoadOperation) FileLoader.this.photoLoadOperationQueue.poll();
                            if (operation != null && operation.start()) {
                                FileLoader.this.currentPhotoLoadOperationsCount = FileLoader.this.currentPhotoLoadOperationsCount + 1;
                            }
                        } else {
                            return;
                        }
                    }
                } else {
                    if (operation != null) {
                        if (operation.wasStarted()) {
                            FileLoader.this.currentLoadOperationsCount = FileLoader.this.currentLoadOperationsCount - 1;
                        } else {
                            FileLoader.this.loadOperationQueue.remove(operation);
                        }
                    }
                    while (!FileLoader.this.loadOperationQueue.isEmpty()) {
                        if (((FileLoadOperation) FileLoader.this.loadOperationQueue.get(0)).isForceRequest()) {
                            maxCount = 3;
                        } else {
                            maxCount = 1;
                        }
                        if (FileLoader.this.currentLoadOperationsCount < maxCount) {
                            operation = (FileLoadOperation) FileLoader.this.loadOperationQueue.poll();
                            if (operation != null && operation.start()) {
                                FileLoader.this.currentLoadOperationsCount = FileLoader.this.currentLoadOperationsCount + 1;
                            }
                        } else {
                            return;
                        }
                    }
                }
            }
        });
    }

    public void setDelegate(FileLoaderDelegate delegate) {
        this.delegate = delegate;
    }

    public static String getMessageFileName(Message message) {
        if (message == null) {
            return "";
        }
        ArrayList<PhotoSize> sizes;
        PhotoSize sizeFull;
        if (message instanceof TL_messageService) {
            if (message.action.photo != null) {
                sizes = message.action.photo.sizes;
                if (sizes.size() > 0) {
                    sizeFull = getClosestPhotoSizeWithSize(sizes, AndroidUtilities.getPhotoSize());
                    if (sizeFull != null) {
                        return getAttachFileName(sizeFull);
                    }
                }
            }
        } else if (message.media instanceof TL_messageMediaDocument) {
            return getAttachFileName(message.media.document);
        } else {
            if (message.media instanceof TL_messageMediaPhoto) {
                sizes = message.media.photo.sizes;
                if (sizes.size() > 0) {
                    sizeFull = getClosestPhotoSizeWithSize(sizes, AndroidUtilities.getPhotoSize());
                    if (sizeFull != null) {
                        return getAttachFileName(sizeFull);
                    }
                }
            } else if (message.media instanceof TL_messageMediaWebPage) {
                if (message.media.webpage.photo != null) {
                    sizes = message.media.webpage.photo.sizes;
                    if (sizes.size() > 0) {
                        sizeFull = getClosestPhotoSizeWithSize(sizes, AndroidUtilities.getPhotoSize());
                        if (sizeFull != null) {
                            return getAttachFileName(sizeFull);
                        }
                    }
                } else if (message.media.webpage.document != null) {
                    return getAttachFileName(message.media.webpage.document);
                }
            }
        }
        return "";
    }

    public static File getPathToMessage(Message message) {
        if (message == null) {
            return new File("");
        }
        ArrayList<PhotoSize> sizes;
        PhotoSize sizeFull;
        if (message instanceof TL_messageService) {
            if (message.action.photo != null) {
                sizes = message.action.photo.sizes;
                if (sizes.size() > 0) {
                    sizeFull = getClosestPhotoSizeWithSize(sizes, AndroidUtilities.getPhotoSize());
                    if (sizeFull != null) {
                        return getPathToAttach(sizeFull);
                    }
                }
            }
        } else if (message.media instanceof TL_messageMediaDocument) {
            return getPathToAttach(message.media.document);
        } else {
            if (message.media instanceof TL_messageMediaPhoto) {
                sizes = message.media.photo.sizes;
                if (sizes.size() > 0) {
                    sizeFull = getClosestPhotoSizeWithSize(sizes, AndroidUtilities.getPhotoSize());
                    if (sizeFull != null) {
                        return getPathToAttach(sizeFull);
                    }
                }
            } else if (message.media instanceof TL_messageMediaWebPage) {
                if (message.media.webpage.document != null) {
                    return getPathToAttach(message.media.webpage.document);
                }
                if (message.media.webpage.photo != null) {
                    sizes = message.media.webpage.photo.sizes;
                    if (sizes.size() > 0) {
                        sizeFull = getClosestPhotoSizeWithSize(sizes, AndroidUtilities.getPhotoSize());
                        if (sizeFull != null) {
                            return getPathToAttach(sizeFull);
                        }
                    }
                }
            }
        }
        return new File("");
    }

    public static File getPathToAttach(TLObject attach) {
        return getPathToAttach(attach, null, false);
    }

    public static File getPathToAttach(TLObject attach, boolean forceCache) {
        return getPathToAttach(attach, null, forceCache);
    }

    public static File getPathToAttach(TLObject attach, String ext, boolean forceCache) {
        File dir = null;
        if (forceCache) {
            dir = getInstance().getDirectory(4);
        } else if (attach instanceof Document) {
            Document document = (Document) attach;
            if (document.key != null) {
                dir = getInstance().getDirectory(4);
            } else if (MessageObject.isVoiceDocument(document)) {
                dir = getInstance().getDirectory(1);
            } else if (MessageObject.isVideoDocument(document)) {
                dir = getInstance().getDirectory(2);
            } else {
                dir = getInstance().getDirectory(3);
            }
        } else if (attach instanceof PhotoSize) {
            PhotoSize photoSize = (PhotoSize) attach;
            if (photoSize.location == null || photoSize.location.key != null || ((photoSize.location.volume_id == -2147483648L && photoSize.location.local_id < 0) || photoSize.size < 0)) {
                dir = getInstance().getDirectory(4);
            } else {
                dir = getInstance().getDirectory(0);
            }
        } else if (attach instanceof FileLocation) {
            FileLocation fileLocation = (FileLocation) attach;
            if (fileLocation.key != null || (fileLocation.volume_id == -2147483648L && fileLocation.local_id < 0)) {
                dir = getInstance().getDirectory(4);
            } else {
                dir = getInstance().getDirectory(0);
            }
        }
        if (dir == null) {
            return new File("");
        }
        return new File(dir, getAttachFileName(attach, ext));
    }

    public static PhotoSize getClosestPhotoSizeWithSize(ArrayList<PhotoSize> sizes, int side) {
        return getClosestPhotoSizeWithSize(sizes, side, false);
    }

    public static PhotoSize getClosestPhotoSizeWithSize(ArrayList<PhotoSize> sizes, int side, boolean byMinSide) {
        if (sizes == null || sizes.isEmpty()) {
            return null;
        }
        int lastSide = 0;
        PhotoSize closestObject = null;
        for (int a = 0; a < sizes.size(); a++) {
            PhotoSize obj = (PhotoSize) sizes.get(a);
            if (obj != null) {
                int currentSide;
                if (byMinSide) {
                    currentSide = obj.h >= obj.w ? obj.w : obj.h;
                    if (closestObject == null || ((side > 100 && closestObject.location != null && closestObject.location.dc_id == Integer.MIN_VALUE) || (obj instanceof TL_photoCachedSize) || (side > lastSide && lastSide < currentSide))) {
                        closestObject = obj;
                        lastSide = currentSide;
                    }
                } else {
                    currentSide = obj.w >= obj.h ? obj.w : obj.h;
                    if (closestObject == null || ((side > 100 && closestObject.location != null && closestObject.location.dc_id == Integer.MIN_VALUE) || (obj instanceof TL_photoCachedSize) || (currentSide <= side && lastSide < currentSide))) {
                        closestObject = obj;
                        lastSide = currentSide;
                    }
                }
            }
        }
        return closestObject;
    }

    public static String getFileExtension(File file) {
        String name = file.getName();
        try {
            return name.substring(name.lastIndexOf(46) + 1);
        } catch (Exception e) {
            return "";
        }
    }

    public static String getDocumentFileName(Document document) {
        if (document != null) {
            if (document.file_name != null) {
                return document.file_name;
            }
            for (int a = 0; a < document.attributes.size(); a++) {
                DocumentAttribute documentAttribute = (DocumentAttribute) document.attributes.get(a);
                if (documentAttribute instanceof TL_documentAttributeFilename) {
                    return documentAttribute.file_name;
                }
            }
        }
        return "";
    }

    public static String getDocumentExtension(Document document) {
        String fileName = getDocumentFileName(document);
        int idx = fileName.lastIndexOf(46);
        String ext = null;
        if (idx != -1) {
            ext = fileName.substring(idx + 1);
        }
        if (ext == null || ext.length() == 0) {
            ext = document.mime_type;
        }
        if (ext == null) {
            ext = "";
        }
        return ext.toUpperCase();
    }

    public static String getAttachFileName(TLObject attach) {
        return getAttachFileName(attach, null);
    }

    public static String getAttachFileName(TLObject attach, String ext) {
        Object obj = -1;
        if (attach instanceof Document) {
            Document document = (Document) attach;
            String docExt = null;
            if (null == null) {
                docExt = getDocumentFileName(document);
                if (docExt != null) {
                    int idx = docExt.lastIndexOf(46);
                    if (idx != -1) {
                        docExt = docExt.substring(idx);
                    }
                }
                docExt = "";
            }
            if (docExt.length() <= 1) {
                if (document.mime_type != null) {
                    String str = document.mime_type;
                    switch (str.hashCode()) {
                        case 187091926:
                            if (str.equals("audio/ogg")) {
                                int i = 1;
                                break;
                            }
                            break;
                        case 1331848029:
                            if (str.equals(MimeTypes.VIDEO_MP4)) {
                                obj = null;
                                break;
                            }
                            break;
                    }
                    switch (obj) {
                        case null:
                            docExt = ".mp4";
                            break;
                        case 1:
                            docExt = ".ogg";
                            break;
                        default:
                            docExt = "";
                            break;
                    }
                }
                docExt = "";
            }
            if (document.version == 0) {
                if (docExt.length() > 1) {
                    return document.dc_id + "_" + document.id + docExt;
                }
                return document.dc_id + "_" + document.id;
            } else if (docExt.length() > 1) {
                return document.dc_id + "_" + document.id + "_" + document.version + docExt;
            } else {
                return document.dc_id + "_" + document.id + "_" + document.version;
            }
        } else if (attach instanceof PhotoSize) {
            PhotoSize photo = (PhotoSize) attach;
            if (photo.location == null || (photo.location instanceof TL_fileLocationUnavailable)) {
                return "";
            }
            r5 = new StringBuilder().append(photo.location.volume_id).append("_").append(photo.location.local_id).append(".");
            if (ext == null) {
                ext = "jpg";
            }
            return r5.append(ext).toString();
        } else if (!(attach instanceof FileLocation)) {
            return "";
        } else {
            if (attach instanceof TL_fileLocationUnavailable) {
                return "";
            }
            FileLocation location = (FileLocation) attach;
            r5 = new StringBuilder().append(location.volume_id).append("_").append(location.local_id).append(".");
            if (ext == null) {
                ext = "jpg";
            }
            return r5.append(ext).toString();
        }
    }

    public void deleteFiles(final ArrayList<File> files, final int type) {
        if (files != null && !files.isEmpty()) {
            this.fileLoaderQueue.postRunnable(new Runnable() {
                public void run() {
                    for (int a = 0; a < files.size(); a++) {
                        File file = (File) files.get(a);
                        if (file.exists()) {
                            try {
                                if (!file.delete()) {
                                    file.deleteOnExit();
                                }
                            } catch (Throwable e) {
                                FileLog.e("tmessages", e);
                            }
                        }
                        try {
                            File qFile = new File(file.getParentFile(), "q_" + file.getName());
                            if (qFile.exists() && !qFile.delete()) {
                                qFile.deleteOnExit();
                            }
                        } catch (Throwable e2) {
                            FileLog.e("tmessages", e2);
                        }
                    }
                    if (type == 2) {
                        ImageLoader.getInstance().clearMemory();
                    }
                }
            });
        }
    }
}
