package org.telegram.messenger;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory.Options;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaCodecInfo;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.support.v13.view.inputmethod.InputContentInfoCompat;
import android.text.TextUtils;
import android.util.LongSparseArray;
import android.util.SparseArray;
import android.webkit.MimeTypeMap;
import android.widget.Toast;
import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.MediaBox;
import com.coremedia.iso.boxes.MediaHeaderBox;
import com.coremedia.iso.boxes.TimeToSampleBox;
import com.coremedia.iso.boxes.TrackBox;
import com.coremedia.iso.boxes.TrackHeaderBox;
import com.googlecode.mp4parser.util.Matrix;
import com.googlecode.mp4parser.util.Path;
import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel.MapMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.telegram.messenger.MediaController.SearchImage;
import org.telegram.messenger.NotificationCenter.NotificationCenterDelegate;
import org.telegram.messenger.audioinfo.AudioInfo;
import org.telegram.messenger.beta.R;
import org.telegram.messenger.exoplayer2.DefaultRenderersFactory;
import org.telegram.messenger.exoplayer2.util.MimeTypes;
import org.telegram.messenger.support.SparseLongArray;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.NativeByteBuffer;
import org.telegram.tgnet.QuickAckDelegate;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.BotInlineResult;
import org.telegram.tgnet.TLRPC.Chat;
import org.telegram.tgnet.TLRPC.ChatFull;
import org.telegram.tgnet.TLRPC.Document;
import org.telegram.tgnet.TLRPC.DocumentAttribute;
import org.telegram.tgnet.TLRPC.EncryptedChat;
import org.telegram.tgnet.TLRPC.FileLocation;
import org.telegram.tgnet.TLRPC.InputDocument;
import org.telegram.tgnet.TLRPC.InputEncryptedFile;
import org.telegram.tgnet.TLRPC.InputFile;
import org.telegram.tgnet.TLRPC.InputMedia;
import org.telegram.tgnet.TLRPC.InputPeer;
import org.telegram.tgnet.TLRPC.KeyboardButton;
import org.telegram.tgnet.TLRPC.Message;
import org.telegram.tgnet.TLRPC.MessageEntity;
import org.telegram.tgnet.TLRPC.MessageFwdHeader;
import org.telegram.tgnet.TLRPC.MessageMedia;
import org.telegram.tgnet.TLRPC.Peer;
import org.telegram.tgnet.TLRPC.PhotoSize;
import org.telegram.tgnet.TLRPC.ReplyMarkup;
import org.telegram.tgnet.TLRPC.TL_botInlineMediaResult;
import org.telegram.tgnet.TLRPC.TL_botInlineMessageMediaAuto;
import org.telegram.tgnet.TLRPC.TL_botInlineMessageMediaContact;
import org.telegram.tgnet.TLRPC.TL_botInlineMessageMediaGeo;
import org.telegram.tgnet.TLRPC.TL_botInlineMessageMediaVenue;
import org.telegram.tgnet.TLRPC.TL_botInlineMessageText;
import org.telegram.tgnet.TLRPC.TL_decryptedMessage;
import org.telegram.tgnet.TLRPC.TL_decryptedMessageActionAbortKey;
import org.telegram.tgnet.TLRPC.TL_decryptedMessageActionAcceptKey;
import org.telegram.tgnet.TLRPC.TL_decryptedMessageActionCommitKey;
import org.telegram.tgnet.TLRPC.TL_decryptedMessageActionDeleteMessages;
import org.telegram.tgnet.TLRPC.TL_decryptedMessageActionFlushHistory;
import org.telegram.tgnet.TLRPC.TL_decryptedMessageActionNoop;
import org.telegram.tgnet.TLRPC.TL_decryptedMessageActionNotifyLayer;
import org.telegram.tgnet.TLRPC.TL_decryptedMessageActionReadMessages;
import org.telegram.tgnet.TLRPC.TL_decryptedMessageActionRequestKey;
import org.telegram.tgnet.TLRPC.TL_decryptedMessageActionResend;
import org.telegram.tgnet.TLRPC.TL_decryptedMessageActionScreenshotMessages;
import org.telegram.tgnet.TLRPC.TL_decryptedMessageActionSetMessageTTL;
import org.telegram.tgnet.TLRPC.TL_decryptedMessageActionTyping;
import org.telegram.tgnet.TLRPC.TL_decryptedMessageMediaDocument;
import org.telegram.tgnet.TLRPC.TL_decryptedMessageMediaPhoto;
import org.telegram.tgnet.TLRPC.TL_decryptedMessageMediaVideo;
import org.telegram.tgnet.TLRPC.TL_document;
import org.telegram.tgnet.TLRPC.TL_documentAttributeAnimated;
import org.telegram.tgnet.TLRPC.TL_documentAttributeAudio;
import org.telegram.tgnet.TLRPC.TL_documentAttributeFilename;
import org.telegram.tgnet.TLRPC.TL_documentAttributeImageSize;
import org.telegram.tgnet.TLRPC.TL_documentAttributeSticker;
import org.telegram.tgnet.TLRPC.TL_documentAttributeVideo;
import org.telegram.tgnet.TLRPC.TL_documentAttributeVideo_layer65;
import org.telegram.tgnet.TLRPC.TL_error;
import org.telegram.tgnet.TLRPC.TL_fileLocationUnavailable;
import org.telegram.tgnet.TLRPC.TL_game;
import org.telegram.tgnet.TLRPC.TL_geoPoint;
import org.telegram.tgnet.TLRPC.TL_inputDocument;
import org.telegram.tgnet.TLRPC.TL_inputEncryptedFile;
import org.telegram.tgnet.TLRPC.TL_inputMediaDocument;
import org.telegram.tgnet.TLRPC.TL_inputMediaGame;
import org.telegram.tgnet.TLRPC.TL_inputMediaPhoto;
import org.telegram.tgnet.TLRPC.TL_inputMediaUploadedDocument;
import org.telegram.tgnet.TLRPC.TL_inputMediaUploadedPhoto;
import org.telegram.tgnet.TLRPC.TL_inputPeerChannel;
import org.telegram.tgnet.TLRPC.TL_inputPeerEmpty;
import org.telegram.tgnet.TLRPC.TL_inputPeerUser;
import org.telegram.tgnet.TLRPC.TL_inputPhoto;
import org.telegram.tgnet.TLRPC.TL_inputSingleMedia;
import org.telegram.tgnet.TLRPC.TL_inputStickerSetEmpty;
import org.telegram.tgnet.TLRPC.TL_keyboardButtonBuy;
import org.telegram.tgnet.TLRPC.TL_keyboardButtonGame;
import org.telegram.tgnet.TLRPC.TL_message;
import org.telegram.tgnet.TLRPC.TL_messageActionScreenshotTaken;
import org.telegram.tgnet.TLRPC.TL_messageEncryptedAction;
import org.telegram.tgnet.TLRPC.TL_messageEntityBold;
import org.telegram.tgnet.TLRPC.TL_messageEntityCode;
import org.telegram.tgnet.TLRPC.TL_messageEntityItalic;
import org.telegram.tgnet.TLRPC.TL_messageEntityPre;
import org.telegram.tgnet.TLRPC.TL_messageEntityTextUrl;
import org.telegram.tgnet.TLRPC.TL_messageEntityUrl;
import org.telegram.tgnet.TLRPC.TL_messageFwdHeader;
import org.telegram.tgnet.TLRPC.TL_messageMediaContact;
import org.telegram.tgnet.TLRPC.TL_messageMediaDocument;
import org.telegram.tgnet.TLRPC.TL_messageMediaEmpty;
import org.telegram.tgnet.TLRPC.TL_messageMediaGame;
import org.telegram.tgnet.TLRPC.TL_messageMediaGeo;
import org.telegram.tgnet.TLRPC.TL_messageMediaGeoLive;
import org.telegram.tgnet.TLRPC.TL_messageMediaInvoice;
import org.telegram.tgnet.TLRPC.TL_messageMediaPhoto;
import org.telegram.tgnet.TLRPC.TL_messageMediaVenue;
import org.telegram.tgnet.TLRPC.TL_messageMediaWebPage;
import org.telegram.tgnet.TLRPC.TL_messageService;
import org.telegram.tgnet.TLRPC.TL_messages_botCallbackAnswer;
import org.telegram.tgnet.TLRPC.TL_messages_editMessage;
import org.telegram.tgnet.TLRPC.TL_messages_forwardMessages;
import org.telegram.tgnet.TLRPC.TL_messages_getBotCallbackAnswer;
import org.telegram.tgnet.TLRPC.TL_messages_messages;
import org.telegram.tgnet.TLRPC.TL_messages_sendBroadcast;
import org.telegram.tgnet.TLRPC.TL_messages_sendEncryptedMultiMedia;
import org.telegram.tgnet.TLRPC.TL_messages_sendMedia;
import org.telegram.tgnet.TLRPC.TL_messages_sendMessage;
import org.telegram.tgnet.TLRPC.TL_messages_sendMultiMedia;
import org.telegram.tgnet.TLRPC.TL_messages_sendScreenshotNotification;
import org.telegram.tgnet.TLRPC.TL_messages_uploadMedia;
import org.telegram.tgnet.TLRPC.TL_payments_getPaymentForm;
import org.telegram.tgnet.TLRPC.TL_payments_getPaymentReceipt;
import org.telegram.tgnet.TLRPC.TL_payments_paymentForm;
import org.telegram.tgnet.TLRPC.TL_payments_paymentReceipt;
import org.telegram.tgnet.TLRPC.TL_peerChannel;
import org.telegram.tgnet.TLRPC.TL_peerUser;
import org.telegram.tgnet.TLRPC.TL_photo;
import org.telegram.tgnet.TLRPC.TL_photoCachedSize;
import org.telegram.tgnet.TLRPC.TL_photoSize;
import org.telegram.tgnet.TLRPC.TL_photoSizeEmpty;
import org.telegram.tgnet.TLRPC.TL_updateMessageID;
import org.telegram.tgnet.TLRPC.TL_updateNewChannelMessage;
import org.telegram.tgnet.TLRPC.TL_updateNewMessage;
import org.telegram.tgnet.TLRPC.TL_updateShortSentMessage;
import org.telegram.tgnet.TLRPC.TL_user;
import org.telegram.tgnet.TLRPC.TL_userContact_old2;
import org.telegram.tgnet.TLRPC.TL_webPagePending;
import org.telegram.tgnet.TLRPC.Update;
import org.telegram.tgnet.TLRPC.Updates;
import org.telegram.tgnet.TLRPC.User;
import org.telegram.tgnet.TLRPC.WebPage;
import org.telegram.tgnet.TLRPC.messages_Messages;
import org.telegram.ui.ActionBar.AlertDialog.Builder;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ChatActivity;
import org.telegram.ui.Components.AlertsCreator;
import org.telegram.ui.PaymentFormActivity;

public class SendMessagesHelper implements NotificationCenterDelegate {
    private static volatile SendMessagesHelper[] Instance = new SendMessagesHelper[3];
    private static DispatchQueue mediaSendQueue = new DispatchQueue("mediaSendQueue");
    private static ThreadPoolExecutor mediaSendThreadPool;
    private int currentAccount;
    private ChatFull currentChatInfo = null;
    private HashMap<String, ArrayList<DelayedMessage>> delayedMessages = new HashMap();
    private LocationProvider locationProvider = new LocationProvider(new C05331());
    private SparseArray<Message> sendingMessages = new SparseArray();
    private SparseArray<MessageObject> unsentMessages = new SparseArray();
    private HashMap<String, Boolean> waitingForCallback = new HashMap();
    private HashMap<String, MessageObject> waitingForLocation = new HashMap();

    /* renamed from: org.telegram.messenger.SendMessagesHelper$2 */
    class C05432 implements Runnable {
        C05432() {
        }

        public void run() {
            NotificationCenter.getInstance(SendMessagesHelper.this.currentAccount).addObserver(SendMessagesHelper.this, NotificationCenter.FileDidUpload);
            NotificationCenter.getInstance(SendMessagesHelper.this.currentAccount).addObserver(SendMessagesHelper.this, NotificationCenter.FileDidFailUpload);
            NotificationCenter.getInstance(SendMessagesHelper.this.currentAccount).addObserver(SendMessagesHelper.this, NotificationCenter.FilePreparingStarted);
            NotificationCenter.getInstance(SendMessagesHelper.this.currentAccount).addObserver(SendMessagesHelper.this, NotificationCenter.FileNewChunkAvailable);
            NotificationCenter.getInstance(SendMessagesHelper.this.currentAccount).addObserver(SendMessagesHelper.this, NotificationCenter.FilePreparingFailed);
            NotificationCenter.getInstance(SendMessagesHelper.this.currentAccount).addObserver(SendMessagesHelper.this, NotificationCenter.httpFileDidFailedLoad);
            NotificationCenter.getInstance(SendMessagesHelper.this.currentAccount).addObserver(SendMessagesHelper.this, NotificationCenter.httpFileDidLoaded);
            NotificationCenter.getInstance(SendMessagesHelper.this.currentAccount).addObserver(SendMessagesHelper.this, NotificationCenter.FileDidLoaded);
            NotificationCenter.getInstance(SendMessagesHelper.this.currentAccount).addObserver(SendMessagesHelper.this, NotificationCenter.FileDidFailedLoad);
        }
    }

    protected class DelayedMessage {
        public EncryptedChat encryptedChat;
        public HashMap<Object, Object> extraHashMap;
        public int finalGroupMessage;
        public long groupId;
        public String httpLocation;
        public FileLocation location;
        public ArrayList<MessageObject> messageObjects;
        public ArrayList<Message> messages;
        public MessageObject obj;
        public String originalPath;
        public ArrayList<String> originalPaths;
        public long peer;
        ArrayList<DelayedMessageSendAfterRequest> requests;
        public TLObject sendEncryptedRequest;
        public TLObject sendRequest;
        public int type;
        public boolean upload;
        public VideoEditedInfo videoEditedInfo;

        public DelayedMessage(long peer) {
            this.peer = peer;
        }

        public void addDelayedRequest(TLObject req, MessageObject msgObj, String originalPath) {
            DelayedMessageSendAfterRequest request = new DelayedMessageSendAfterRequest();
            request.request = req;
            request.msgObj = msgObj;
            request.originalPath = originalPath;
            if (this.requests == null) {
                this.requests = new ArrayList();
            }
            this.requests.add(request);
        }

        public void addDelayedRequest(TLObject req, ArrayList<MessageObject> msgObjs, ArrayList<String> originalPaths) {
            DelayedMessageSendAfterRequest request = new DelayedMessageSendAfterRequest();
            request.request = req;
            request.msgObjs = msgObjs;
            request.originalPaths = originalPaths;
            if (this.requests == null) {
                this.requests = new ArrayList();
            }
            this.requests.add(request);
        }

        public void sendDelayedRequests() {
            if (this.requests == null) {
                return;
            }
            if (this.type == 4 || this.type == 0) {
                int size = this.requests.size();
                for (int a = 0; a < size; a++) {
                    DelayedMessageSendAfterRequest request = (DelayedMessageSendAfterRequest) this.requests.get(a);
                    if (request.request instanceof TL_messages_sendEncryptedMultiMedia) {
                        SecretChatHelper.getInstance(SendMessagesHelper.this.currentAccount).performSendEncryptedRequest((TL_messages_sendEncryptedMultiMedia) request.request, this);
                    } else if (request.request instanceof TL_messages_sendMultiMedia) {
                        SendMessagesHelper.this.performSendMessageRequestMulti((TL_messages_sendMultiMedia) request.request, request.msgObjs, request.originalPaths);
                    } else {
                        SendMessagesHelper.this.performSendMessageRequest(request.request, request.msgObj, request.originalPath);
                    }
                }
                this.requests = null;
            }
        }

        public void markAsError() {
            if (this.type == 4) {
                for (int a = 0; a < this.messageObjects.size(); a++) {
                    MessageObject obj = (MessageObject) this.messageObjects.get(a);
                    MessagesStorage.getInstance(SendMessagesHelper.this.currentAccount).markMessageAsSendError(obj.messageOwner);
                    obj.messageOwner.send_state = 2;
                    NotificationCenter.getInstance(SendMessagesHelper.this.currentAccount).postNotificationName(NotificationCenter.messageSendError, Integer.valueOf(obj.getId()));
                    SendMessagesHelper.this.processSentMessage(obj.getId());
                }
                SendMessagesHelper.this.delayedMessages.remove("group_" + this.groupId);
            } else {
                MessagesStorage.getInstance(SendMessagesHelper.this.currentAccount).markMessageAsSendError(this.obj.messageOwner);
                this.obj.messageOwner.send_state = 2;
                NotificationCenter.getInstance(SendMessagesHelper.this.currentAccount).postNotificationName(NotificationCenter.messageSendError, Integer.valueOf(this.obj.getId()));
                SendMessagesHelper.this.processSentMessage(this.obj.getId());
            }
            sendDelayedRequests();
        }
    }

    protected class DelayedMessageSendAfterRequest {
        public MessageObject msgObj;
        public ArrayList<MessageObject> msgObjs;
        public String originalPath;
        public ArrayList<String> originalPaths;
        public TLObject request;

        protected DelayedMessageSendAfterRequest() {
        }
    }

    public static class LocationProvider {
        private LocationProviderDelegate delegate;
        private GpsLocationListener gpsLocationListener = new GpsLocationListener();
        private Location lastKnownLocation;
        private LocationManager locationManager;
        private Runnable locationQueryCancelRunnable;
        private GpsLocationListener networkLocationListener = new GpsLocationListener();

        /* renamed from: org.telegram.messenger.SendMessagesHelper$LocationProvider$1 */
        class C05621 implements Runnable {
            C05621() {
            }

            public void run() {
                if (LocationProvider.this.locationQueryCancelRunnable == this) {
                    if (LocationProvider.this.delegate != null) {
                        if (LocationProvider.this.lastKnownLocation != null) {
                            LocationProvider.this.delegate.onLocationAcquired(LocationProvider.this.lastKnownLocation);
                        } else {
                            LocationProvider.this.delegate.onUnableLocationAcquire();
                        }
                    }
                    LocationProvider.this.cleanup();
                }
            }
        }

        private class GpsLocationListener implements LocationListener {
            private GpsLocationListener() {
            }

            public void onLocationChanged(Location location) {
                if (location != null && LocationProvider.this.locationQueryCancelRunnable != null) {
                    if (BuildVars.LOGS_ENABLED) {
                        FileLog.m0d("found location " + location);
                    }
                    LocationProvider.this.lastKnownLocation = location;
                    if (location.getAccuracy() < 100.0f) {
                        if (LocationProvider.this.delegate != null) {
                            LocationProvider.this.delegate.onLocationAcquired(location);
                        }
                        if (LocationProvider.this.locationQueryCancelRunnable != null) {
                            AndroidUtilities.cancelRunOnUIThread(LocationProvider.this.locationQueryCancelRunnable);
                        }
                        LocationProvider.this.cleanup();
                    }
                }
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        }

        public interface LocationProviderDelegate {
            void onLocationAcquired(Location location);

            void onUnableLocationAcquire();
        }

        public LocationProvider(LocationProviderDelegate locationProviderDelegate) {
            this.delegate = locationProviderDelegate;
        }

        public void setDelegate(LocationProviderDelegate locationProviderDelegate) {
            this.delegate = locationProviderDelegate;
        }

        private void cleanup() {
            this.locationManager.removeUpdates(this.gpsLocationListener);
            this.locationManager.removeUpdates(this.networkLocationListener);
            this.lastKnownLocation = null;
            this.locationQueryCancelRunnable = null;
        }

        public void start() {
            if (this.locationManager == null) {
                this.locationManager = (LocationManager) ApplicationLoader.applicationContext.getSystemService("location");
            }
            try {
                this.locationManager.requestLocationUpdates("gps", 1, 0.0f, this.gpsLocationListener);
            } catch (Throwable e) {
                FileLog.m3e(e);
            }
            try {
                this.locationManager.requestLocationUpdates("network", 1, 0.0f, this.networkLocationListener);
            } catch (Throwable e2) {
                FileLog.m3e(e2);
            }
            try {
                this.lastKnownLocation = this.locationManager.getLastKnownLocation("gps");
                if (this.lastKnownLocation == null) {
                    this.lastKnownLocation = this.locationManager.getLastKnownLocation("network");
                }
            } catch (Throwable e22) {
                FileLog.m3e(e22);
            }
            if (this.locationQueryCancelRunnable != null) {
                AndroidUtilities.cancelRunOnUIThread(this.locationQueryCancelRunnable);
            }
            this.locationQueryCancelRunnable = new C05621();
            AndroidUtilities.runOnUIThread(this.locationQueryCancelRunnable, DefaultRenderersFactory.DEFAULT_ALLOWED_VIDEO_JOINING_TIME_MS);
        }

        public void stop() {
            if (this.locationManager != null) {
                if (this.locationQueryCancelRunnable != null) {
                    AndroidUtilities.cancelRunOnUIThread(this.locationQueryCancelRunnable);
                }
                cleanup();
            }
        }
    }

    private static class MediaSendPrepareWorker {
        public volatile TL_photo photo;
        public CountDownLatch sync;

        private MediaSendPrepareWorker() {
        }
    }

    public static class SendingMediaInfo {
        public String caption;
        public ArrayList<MessageEntity> entities;
        public boolean isVideo;
        public ArrayList<InputDocument> masks;
        public String path;
        public SearchImage searchImage;
        public int ttl;
        public Uri uri;
        public VideoEditedInfo videoEditedInfo;
    }

    /* renamed from: org.telegram.messenger.SendMessagesHelper$1 */
    class C05331 implements LocationProviderDelegate {
        C05331() {
        }

        public void onLocationAcquired(Location location) {
            SendMessagesHelper.this.sendLocation(location);
            SendMessagesHelper.this.waitingForLocation.clear();
        }

        public void onUnableLocationAcquire() {
            HashMap<String, MessageObject> waitingForLocationCopy = new HashMap(SendMessagesHelper.this.waitingForLocation);
            NotificationCenter.getInstance(SendMessagesHelper.this.currentAccount).postNotificationName(NotificationCenter.wasUnableToFindCurrentLocation, waitingForLocationCopy);
            SendMessagesHelper.this.waitingForLocation.clear();
        }
    }

    static {
        int cores;
        if (VERSION.SDK_INT >= 17) {
            cores = Runtime.getRuntime().availableProcessors();
        } else {
            cores = 2;
        }
        mediaSendThreadPool = new ThreadPoolExecutor(cores, cores, 60, TimeUnit.SECONDS, new LinkedBlockingQueue());
    }

    public static SendMessagesHelper getInstance(int num) {
        SendMessagesHelper localInstance = Instance[num];
        if (localInstance == null) {
            synchronized (SendMessagesHelper.class) {
                try {
                    localInstance = Instance[num];
                    if (localInstance == null) {
                        SendMessagesHelper[] sendMessagesHelperArr = Instance;
                        SendMessagesHelper localInstance2 = new SendMessagesHelper(num);
                        try {
                            sendMessagesHelperArr[num] = localInstance2;
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

    public SendMessagesHelper(int instance) {
        this.currentAccount = instance;
        AndroidUtilities.runOnUIThread(new C05432());
    }

    public void cleanup() {
        this.delayedMessages.clear();
        this.unsentMessages.clear();
        this.sendingMessages.clear();
        this.waitingForLocation.clear();
        this.waitingForCallback.clear();
        this.currentChatInfo = null;
        this.locationProvider.stop();
    }

    public void setCurrentChatInfo(ChatFull info) {
        this.currentChatInfo = info;
    }

    public void didReceivedNotification(int id, int account, Object... args) {
        String location;
        ArrayList<DelayedMessage> arr;
        int a;
        DelayedMessage message;
        int index;
        MessageObject messageObject;
        if (id == NotificationCenter.FileDidUpload) {
            location = args[0];
            InputFile file = args[1];
            InputEncryptedFile encryptedFile = args[2];
            arr = (ArrayList) this.delayedMessages.get(location);
            if (arr != null) {
                a = 0;
                while (a < arr.size()) {
                    message = (DelayedMessage) arr.get(a);
                    InputMedia media = null;
                    if (message.sendRequest instanceof TL_messages_sendMedia) {
                        media = ((TL_messages_sendMedia) message.sendRequest).media;
                    } else if (message.sendRequest instanceof TL_messages_sendBroadcast) {
                        media = ((TL_messages_sendBroadcast) message.sendRequest).media;
                    } else if (message.sendRequest instanceof TL_messages_sendMultiMedia) {
                        media = (InputMedia) message.extraHashMap.get(location);
                    }
                    if (file != null && media != null) {
                        if (message.type == 0) {
                            media.file = file;
                            performSendMessageRequest(message.sendRequest, message.obj, message.originalPath, message, true);
                        } else if (message.type == 1) {
                            if (media.file == null) {
                                media.file = file;
                                if (media.thumb != null || message.location == null) {
                                    performSendMessageRequest(message.sendRequest, message.obj, message.originalPath);
                                } else {
                                    performSendDelayedMessage(message);
                                }
                            } else {
                                media.thumb = file;
                                media.flags |= 4;
                                performSendMessageRequest(message.sendRequest, message.obj, message.originalPath);
                            }
                        } else if (message.type == 2) {
                            if (media.file == null) {
                                media.file = file;
                                if (media.thumb != null || message.location == null) {
                                    performSendMessageRequest(message.sendRequest, message.obj, message.originalPath);
                                } else {
                                    performSendDelayedMessage(message);
                                }
                            } else {
                                media.thumb = file;
                                media.flags |= 4;
                                performSendMessageRequest(message.sendRequest, message.obj, message.originalPath);
                            }
                        } else if (message.type == 3) {
                            media.file = file;
                            performSendMessageRequest(message.sendRequest, message.obj, message.originalPath);
                        } else if (message.type == 4) {
                            if (!(media instanceof TL_inputMediaUploadedDocument)) {
                                media.file = file;
                                uploadMultiMedia(message, media, null, location);
                            } else if (media.file == null) {
                                media.file = file;
                                index = message.messageObjects.indexOf((MessageObject) message.extraHashMap.get(location + "_i"));
                                message.location = (FileLocation) message.extraHashMap.get(location + "_t");
                                stopVideoService(((MessageObject) message.messageObjects.get(index)).messageOwner.attachPath);
                                if (media.thumb != null || message.location == null) {
                                    uploadMultiMedia(message, media, null, location);
                                } else {
                                    performSendDelayedMessage(message, index);
                                }
                            } else {
                                media.thumb = file;
                                media.flags |= 4;
                                uploadMultiMedia(message, media, null, (String) message.extraHashMap.get(location + "_o"));
                            }
                        }
                        arr.remove(a);
                        a--;
                    } else if (!(encryptedFile == null || message.sendEncryptedRequest == null)) {
                        TL_decryptedMessage decryptedMessage = null;
                        if (message.type == 4) {
                            TL_messages_sendEncryptedMultiMedia req = (TL_messages_sendEncryptedMultiMedia) message.sendEncryptedRequest;
                            InputEncryptedFile inputEncryptedFile = (InputEncryptedFile) message.extraHashMap.get(location);
                            index = req.files.indexOf(inputEncryptedFile);
                            if (index >= 0) {
                                req.files.set(index, encryptedFile);
                                if (inputEncryptedFile.id == 1) {
                                    messageObject = (MessageObject) message.extraHashMap.get(location + "_i");
                                    message.location = (FileLocation) message.extraHashMap.get(location + "_t");
                                    stopVideoService(((MessageObject) message.messageObjects.get(index)).messageOwner.attachPath);
                                }
                                decryptedMessage = (TL_decryptedMessage) req.messages.get(index);
                            }
                        } else {
                            decryptedMessage = message.sendEncryptedRequest;
                        }
                        if (decryptedMessage != null) {
                            if ((decryptedMessage.media instanceof TL_decryptedMessageMediaVideo) || (decryptedMessage.media instanceof TL_decryptedMessageMediaPhoto) || (decryptedMessage.media instanceof TL_decryptedMessageMediaDocument)) {
                                decryptedMessage.media.size = (int) ((Long) args[5]).longValue();
                            }
                            decryptedMessage.media.key = (byte[]) args[3];
                            decryptedMessage.media.iv = (byte[]) args[4];
                            if (message.type == 4) {
                                uploadMultiMedia(message, null, encryptedFile, location);
                            } else {
                                SecretChatHelper.getInstance(this.currentAccount).performSendEncryptedRequest(decryptedMessage, message.obj.messageOwner, message.encryptedChat, encryptedFile, message.originalPath, message.obj);
                            }
                        }
                        arr.remove(a);
                        a--;
                    }
                    a++;
                }
                if (arr.isEmpty()) {
                    this.delayedMessages.remove(location);
                }
            }
        } else if (id == NotificationCenter.FileDidFailUpload) {
            location = (String) args[0];
            boolean enc = ((Boolean) args[1]).booleanValue();
            arr = (ArrayList) this.delayedMessages.get(location);
            if (arr != null) {
                a = 0;
                while (a < arr.size()) {
                    DelayedMessage obj = (DelayedMessage) arr.get(a);
                    if ((enc && obj.sendEncryptedRequest != null) || !(enc || obj.sendRequest == null)) {
                        obj.markAsError();
                        arr.remove(a);
                        a--;
                    }
                    a++;
                }
                if (arr.isEmpty()) {
                    this.delayedMessages.remove(location);
                }
            }
        } else if (id == NotificationCenter.FilePreparingStarted) {
            messageObject = (MessageObject) args[0];
            if (messageObject.getId() != 0) {
                finalPath = args[1];
                arr = (ArrayList) this.delayedMessages.get(messageObject.messageOwner.attachPath);
                if (arr != null) {
                    a = 0;
                    while (a < arr.size()) {
                        message = (DelayedMessage) arr.get(a);
                        if (message.type == 4) {
                            index = message.messageObjects.indexOf(messageObject);
                            message.location = (FileLocation) message.extraHashMap.get(messageObject.messageOwner.attachPath + "_t");
                            performSendDelayedMessage(message, index);
                            arr.remove(a);
                            break;
                        } else if (message.obj == messageObject) {
                            message.videoEditedInfo = null;
                            performSendDelayedMessage(message);
                            arr.remove(a);
                            break;
                        } else {
                            a++;
                        }
                    }
                    if (arr.isEmpty()) {
                        this.delayedMessages.remove(messageObject.messageOwner.attachPath);
                    }
                }
            }
        } else if (id == NotificationCenter.FileNewChunkAvailable) {
            messageObject = (MessageObject) args[0];
            if (messageObject.getId() != 0) {
                finalPath = (String) args[1];
                long availableSize = ((Long) args[2]).longValue();
                long finalSize = ((Long) args[3]).longValue();
                FileLoader.getInstance(this.currentAccount).checkUploadNewDataAvailable(finalPath, ((int) messageObject.getDialogId()) == 0, availableSize, finalSize);
                if (finalSize != 0) {
                    arr = (ArrayList) this.delayedMessages.get(messageObject.messageOwner.attachPath);
                    if (arr != null) {
                        for (a = 0; a < arr.size(); a++) {
                            message = (DelayedMessage) arr.get(a);
                            ArrayList messages;
                            if (message.type == 4) {
                                for (b = 0; b < message.messageObjects.size(); b++) {
                                    MessageObject obj2 = (MessageObject) message.messageObjects.get(b);
                                    if (obj2 == messageObject) {
                                        obj2.videoEditedInfo = null;
                                        obj2.messageOwner.params.remove("ve");
                                        obj2.messageOwner.media.document.size = (int) finalSize;
                                        messages = new ArrayList();
                                        messages.add(obj2.messageOwner);
                                        MessagesStorage.getInstance(this.currentAccount).putMessages(messages, false, true, false, 0);
                                        break;
                                    }
                                }
                            } else if (message.obj == messageObject) {
                                message.obj.videoEditedInfo = null;
                                message.obj.messageOwner.params.remove("ve");
                                message.obj.messageOwner.media.document.size = (int) finalSize;
                                messages = new ArrayList();
                                messages.add(message.obj.messageOwner);
                                MessagesStorage.getInstance(this.currentAccount).putMessages(messages, false, true, false, 0);
                                return;
                            }
                        }
                    }
                }
            }
        } else if (id == NotificationCenter.FilePreparingFailed) {
            messageObject = (MessageObject) args[0];
            if (messageObject.getId() != 0) {
                finalPath = (String) args[1];
                stopVideoService(messageObject.messageOwner.attachPath);
                arr = (ArrayList) this.delayedMessages.get(finalPath);
                if (arr != null) {
                    a = 0;
                    while (a < arr.size()) {
                        message = (DelayedMessage) arr.get(a);
                        if (message.type == 4) {
                            for (b = 0; b < message.messages.size(); b++) {
                                if (message.messageObjects.get(b) == messageObject) {
                                    message.markAsError();
                                    arr.remove(a);
                                    a--;
                                    break;
                                }
                            }
                        } else if (message.obj == messageObject) {
                            message.markAsError();
                            arr.remove(a);
                            a--;
                        }
                        a++;
                    }
                    if (arr.isEmpty()) {
                        this.delayedMessages.remove(finalPath);
                    }
                }
            }
        } else if (id == NotificationCenter.httpFileDidLoaded) {
            path = args[0];
            arr = (ArrayList) this.delayedMessages.get(path);
            if (arr != null) {
                for (a = 0; a < arr.size(); a++) {
                    message = (DelayedMessage) arr.get(a);
                    int fileType = -1;
                    if (message.type == 0) {
                        fileType = 0;
                        messageObject = message.obj;
                    } else if (message.type == 2) {
                        fileType = 1;
                        messageObject = message.obj;
                    } else if (message.type == 4) {
                        messageObject = (MessageObject) message.extraHashMap.get(path);
                        if (messageObject.getDocument() != null) {
                            fileType = 1;
                        } else {
                            fileType = 0;
                        }
                    } else {
                        messageObject = null;
                    }
                    final File cacheFile;
                    if (fileType == 0) {
                        cacheFile = new File(FileLoader.getDirectory(4), Utilities.MD5(path) + "." + ImageLoader.getHttpUrlExtension(path, "file"));
                        Utilities.globalQueue.postRunnable(new Runnable() {
                            public void run() {
                                final TL_photo photo = SendMessagesHelper.this.generatePhotoSizes(cacheFile.toString(), null);
                                AndroidUtilities.runOnUIThread(new Runnable() {
                                    public void run() {
                                        if (photo != null) {
                                            messageObject.messageOwner.media.photo = photo;
                                            messageObject.messageOwner.attachPath = cacheFile.toString();
                                            ArrayList messages = new ArrayList();
                                            messages.add(messageObject.messageOwner);
                                            MessagesStorage.getInstance(SendMessagesHelper.this.currentAccount).putMessages(messages, false, true, false, 0);
                                            NotificationCenter.getInstance(SendMessagesHelper.this.currentAccount).postNotificationName(NotificationCenter.updateMessageMedia, messageObject.messageOwner);
                                            message.location = ((PhotoSize) photo.sizes.get(photo.sizes.size() - 1)).location;
                                            message.httpLocation = null;
                                            if (message.type == 4) {
                                                SendMessagesHelper.this.performSendDelayedMessage(message, message.messageObjects.indexOf(messageObject));
                                                return;
                                            } else {
                                                SendMessagesHelper.this.performSendDelayedMessage(message);
                                                return;
                                            }
                                        }
                                        if (BuildVars.LOGS_ENABLED) {
                                            FileLog.m1e("can't load image " + path + " to file " + cacheFile.toString());
                                        }
                                        message.markAsError();
                                    }
                                });
                            }
                        });
                    } else if (fileType == 1) {
                        cacheFile = new File(FileLoader.getDirectory(4), Utilities.MD5(path) + ".gif");
                        Utilities.globalQueue.postRunnable(new Runnable() {
                            public void run() {
                                boolean z = true;
                                final Document document = message.obj.getDocument();
                                if (document.thumb.location instanceof TL_fileLocationUnavailable) {
                                    try {
                                        Bitmap bitmap = ImageLoader.loadBitmap(cacheFile.getAbsolutePath(), null, 90.0f, 90.0f, true);
                                        if (bitmap != null) {
                                            if (message.sendEncryptedRequest == null) {
                                                z = false;
                                            }
                                            document.thumb = ImageLoader.scaleAndSaveImage(bitmap, 90.0f, 90.0f, 55, z);
                                            bitmap.recycle();
                                        }
                                    } catch (Throwable e) {
                                        document.thumb = null;
                                        FileLog.m3e(e);
                                    }
                                    if (document.thumb == null) {
                                        document.thumb = new TL_photoSizeEmpty();
                                        document.thumb.type = "s";
                                    }
                                }
                                AndroidUtilities.runOnUIThread(new Runnable() {
                                    public void run() {
                                        message.httpLocation = null;
                                        message.obj.messageOwner.attachPath = cacheFile.toString();
                                        message.location = document.thumb.location;
                                        ArrayList messages = new ArrayList();
                                        messages.add(messageObject.messageOwner);
                                        MessagesStorage.getInstance(SendMessagesHelper.this.currentAccount).putMessages(messages, false, true, false, 0);
                                        SendMessagesHelper.this.performSendDelayedMessage(message);
                                        NotificationCenter.getInstance(SendMessagesHelper.this.currentAccount).postNotificationName(NotificationCenter.updateMessageMedia, message.obj.messageOwner);
                                    }
                                });
                            }
                        });
                    }
                }
                this.delayedMessages.remove(path);
            }
        } else if (id == NotificationCenter.FileDidLoaded) {
            path = (String) args[0];
            arr = (ArrayList) this.delayedMessages.get(path);
            if (arr != null) {
                for (a = 0; a < arr.size(); a++) {
                    performSendDelayedMessage((DelayedMessage) arr.get(a));
                }
                this.delayedMessages.remove(path);
            }
        } else if (id == NotificationCenter.httpFileDidFailedLoad || id == NotificationCenter.FileDidFailedLoad) {
            path = (String) args[0];
            arr = (ArrayList) this.delayedMessages.get(path);
            if (arr != null) {
                for (a = 0; a < arr.size(); a++) {
                    ((DelayedMessage) arr.get(a)).markAsError();
                }
                this.delayedMessages.remove(path);
            }
        }
    }

    public void cancelSendingMessage(MessageObject object) {
        ArrayList<String> keysToRemove = new ArrayList();
        boolean enc = false;
        for (Entry<String, ArrayList<DelayedMessage>> entry : this.delayedMessages.entrySet()) {
            ArrayList<DelayedMessage> messages = (ArrayList) entry.getValue();
            int a = 0;
            while (a < messages.size()) {
                DelayedMessage message = (DelayedMessage) messages.get(a);
                if (message.type == 4) {
                    int index = -1;
                    MessageObject messageObject = null;
                    for (int b = 0; b < message.messageObjects.size(); b++) {
                        messageObject = (MessageObject) message.messageObjects.get(b);
                        if (messageObject.getId() == object.getId()) {
                            index = b;
                            break;
                        }
                    }
                    if (index >= 0) {
                        message.messageObjects.remove(index);
                        message.messages.remove(index);
                        message.originalPaths.remove(index);
                        if (message.sendRequest != null) {
                            ((TL_messages_sendMultiMedia) message.sendRequest).multi_media.remove(index);
                        } else {
                            TL_messages_sendEncryptedMultiMedia request = (TL_messages_sendEncryptedMultiMedia) message.sendEncryptedRequest;
                            request.messages.remove(index);
                            request.files.remove(index);
                        }
                        MediaController.getInstance().cancelVideoConvert(object);
                        String keyToRemove = (String) message.extraHashMap.get(messageObject);
                        if (keyToRemove != null) {
                            keysToRemove.add(keyToRemove);
                        }
                        if (message.messageObjects.isEmpty()) {
                            message.sendDelayedRequests();
                        } else {
                            if (message.finalGroupMessage == object.getId()) {
                                MessageObject prevMessage = (MessageObject) message.messageObjects.get(message.messageObjects.size() - 1);
                                message.finalGroupMessage = prevMessage.getId();
                                prevMessage.messageOwner.params.put("final", "1");
                                messages_Messages messagesRes = new TL_messages_messages();
                                messagesRes.messages.add(prevMessage.messageOwner);
                                MessagesStorage.getInstance(this.currentAccount).putMessages(messagesRes, message.peer, -2, 0, false);
                            }
                            sendReadyToSendGroup(message, false, true);
                        }
                    }
                } else if (message.obj.getId() == object.getId()) {
                    messages.remove(a);
                    message.sendDelayedRequests();
                    MediaController.getInstance().cancelVideoConvert(message.obj);
                    if (messages.size() == 0) {
                        keysToRemove.add(entry.getKey());
                        if (message.sendEncryptedRequest != null) {
                            enc = true;
                        }
                    }
                } else {
                    a++;
                }
            }
        }
        for (a = 0; a < keysToRemove.size(); a++) {
            String key = (String) keysToRemove.get(a);
            if (key.startsWith("http")) {
                ImageLoader.getInstance().cancelLoadHttpFile(key);
            } else {
                FileLoader.getInstance(this.currentAccount).cancelUploadFile(key, enc);
            }
            stopVideoService(key);
            this.delayedMessages.remove(key);
        }
        ArrayList<Integer> messages2 = new ArrayList();
        messages2.add(Integer.valueOf(object.getId()));
        MessagesController.getInstance(this.currentAccount).deleteMessages(messages2, null, null, object.messageOwner.to_id.channel_id, false);
    }

    public boolean retrySendMessage(MessageObject messageObject, boolean unsent) {
        if (messageObject.getId() >= 0) {
            return false;
        }
        if (messageObject.messageOwner.action instanceof TL_messageEncryptedAction) {
            EncryptedChat encryptedChat = MessagesController.getInstance(this.currentAccount).getEncryptedChat(Integer.valueOf((int) (messageObject.getDialogId() >> 32)));
            if (encryptedChat == null) {
                MessagesStorage.getInstance(this.currentAccount).markMessageAsSendError(messageObject.messageOwner);
                messageObject.messageOwner.send_state = 2;
                NotificationCenter.getInstance(this.currentAccount).postNotificationName(NotificationCenter.messageSendError, Integer.valueOf(messageObject.getId()));
                processSentMessage(messageObject.getId());
                return false;
            }
            if (messageObject.messageOwner.random_id == 0) {
                messageObject.messageOwner.random_id = getNextRandomId();
            }
            if (messageObject.messageOwner.action.encryptedAction instanceof TL_decryptedMessageActionSetMessageTTL) {
                SecretChatHelper.getInstance(this.currentAccount).sendTTLMessage(encryptedChat, messageObject.messageOwner);
            } else if (messageObject.messageOwner.action.encryptedAction instanceof TL_decryptedMessageActionDeleteMessages) {
                SecretChatHelper.getInstance(this.currentAccount).sendMessagesDeleteMessage(encryptedChat, null, messageObject.messageOwner);
            } else if (messageObject.messageOwner.action.encryptedAction instanceof TL_decryptedMessageActionFlushHistory) {
                SecretChatHelper.getInstance(this.currentAccount).sendClearHistoryMessage(encryptedChat, messageObject.messageOwner);
            } else if (messageObject.messageOwner.action.encryptedAction instanceof TL_decryptedMessageActionNotifyLayer) {
                SecretChatHelper.getInstance(this.currentAccount).sendNotifyLayerMessage(encryptedChat, messageObject.messageOwner);
            } else if (messageObject.messageOwner.action.encryptedAction instanceof TL_decryptedMessageActionReadMessages) {
                SecretChatHelper.getInstance(this.currentAccount).sendMessagesReadMessage(encryptedChat, null, messageObject.messageOwner);
            } else if (messageObject.messageOwner.action.encryptedAction instanceof TL_decryptedMessageActionScreenshotMessages) {
                SecretChatHelper.getInstance(this.currentAccount).sendScreenshotMessage(encryptedChat, null, messageObject.messageOwner);
            } else if (!((messageObject.messageOwner.action.encryptedAction instanceof TL_decryptedMessageActionTyping) || (messageObject.messageOwner.action.encryptedAction instanceof TL_decryptedMessageActionResend))) {
                if (messageObject.messageOwner.action.encryptedAction instanceof TL_decryptedMessageActionCommitKey) {
                    SecretChatHelper.getInstance(this.currentAccount).sendCommitKeyMessage(encryptedChat, messageObject.messageOwner);
                } else if (messageObject.messageOwner.action.encryptedAction instanceof TL_decryptedMessageActionAbortKey) {
                    SecretChatHelper.getInstance(this.currentAccount).sendAbortKeyMessage(encryptedChat, messageObject.messageOwner, 0);
                } else if (messageObject.messageOwner.action.encryptedAction instanceof TL_decryptedMessageActionRequestKey) {
                    SecretChatHelper.getInstance(this.currentAccount).sendRequestKeyMessage(encryptedChat, messageObject.messageOwner);
                } else if (messageObject.messageOwner.action.encryptedAction instanceof TL_decryptedMessageActionAcceptKey) {
                    SecretChatHelper.getInstance(this.currentAccount).sendAcceptKeyMessage(encryptedChat, messageObject.messageOwner);
                } else if (messageObject.messageOwner.action.encryptedAction instanceof TL_decryptedMessageActionNoop) {
                    SecretChatHelper.getInstance(this.currentAccount).sendNoopMessage(encryptedChat, messageObject.messageOwner);
                }
            }
            return true;
        }
        if (messageObject.messageOwner.action instanceof TL_messageActionScreenshotTaken) {
            sendScreenshotMessage(MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf((int) messageObject.getDialogId())), messageObject.messageOwner.reply_to_msg_id, messageObject.messageOwner);
        }
        if (unsent) {
            this.unsentMessages.put(messageObject.getId(), messageObject);
        }
        sendMessage(messageObject);
        return true;
    }

    protected void processSentMessage(int id) {
        int prevSize = this.unsentMessages.size();
        this.unsentMessages.remove(id);
        if (prevSize != 0 && this.unsentMessages.size() == 0) {
            checkUnsentMessages();
        }
    }

    public void processForwardFromMyName(MessageObject messageObject, long did) {
        if (messageObject != null) {
            ArrayList<MessageObject> arrayList;
            if (messageObject.messageOwner.media == null || (messageObject.messageOwner.media instanceof TL_messageMediaEmpty) || (messageObject.messageOwner.media instanceof TL_messageMediaWebPage) || (messageObject.messageOwner.media instanceof TL_messageMediaGame) || (messageObject.messageOwner.media instanceof TL_messageMediaInvoice)) {
                if (messageObject.messageOwner.message != null) {
                    ArrayList<MessageEntity> entities;
                    WebPage webPage = null;
                    if (messageObject.messageOwner.media instanceof TL_messageMediaWebPage) {
                        webPage = messageObject.messageOwner.media.webpage;
                    }
                    if (messageObject.messageOwner.entities == null || messageObject.messageOwner.entities.isEmpty()) {
                        entities = null;
                    } else {
                        entities = new ArrayList();
                        for (int a = 0; a < messageObject.messageOwner.entities.size(); a++) {
                            MessageEntity entity = (MessageEntity) messageObject.messageOwner.entities.get(a);
                            if ((entity instanceof TL_messageEntityBold) || (entity instanceof TL_messageEntityItalic) || (entity instanceof TL_messageEntityPre) || (entity instanceof TL_messageEntityCode) || (entity instanceof TL_messageEntityTextUrl)) {
                                entities.add(entity);
                            }
                        }
                    }
                    sendMessage(messageObject.messageOwner.message, did, messageObject.replyMessageObject, webPage, true, entities, null, null);
                } else if (((int) did) != 0) {
                    arrayList = new ArrayList();
                    arrayList.add(messageObject);
                    sendMessage(arrayList, did);
                }
            } else if (messageObject.messageOwner.media.photo instanceof TL_photo) {
                sendMessage((TL_photo) messageObject.messageOwner.media.photo, null, did, messageObject.replyMessageObject, messageObject.messageOwner.message, messageObject.messageOwner.entities, null, null, messageObject.messageOwner.media.ttl_seconds);
            } else if (messageObject.messageOwner.media.document instanceof TL_document) {
                sendMessage((TL_document) messageObject.messageOwner.media.document, null, messageObject.messageOwner.attachPath, did, messageObject.replyMessageObject, messageObject.messageOwner.message, messageObject.messageOwner.entities, null, null, messageObject.messageOwner.media.ttl_seconds);
            } else if ((messageObject.messageOwner.media instanceof TL_messageMediaVenue) || (messageObject.messageOwner.media instanceof TL_messageMediaGeo)) {
                sendMessage(messageObject.messageOwner.media, did, messageObject.replyMessageObject, null, null);
            } else if (messageObject.messageOwner.media.phone_number != null) {
                User user = new TL_userContact_old2();
                user.phone = messageObject.messageOwner.media.phone_number;
                user.first_name = messageObject.messageOwner.media.first_name;
                user.last_name = messageObject.messageOwner.media.last_name;
                user.id = messageObject.messageOwner.media.user_id;
                sendMessage(user, did, messageObject.replyMessageObject, null, null);
            } else if (((int) did) != 0) {
                arrayList = new ArrayList();
                arrayList.add(messageObject);
                sendMessage(arrayList, did);
            }
        }
    }

    public void sendScreenshotMessage(User user, int messageId, Message resendMessage) {
        if (user != null && messageId != 0 && user.id != UserConfig.getInstance(this.currentAccount).getClientUserId()) {
            Message message;
            TL_messages_sendScreenshotNotification req = new TL_messages_sendScreenshotNotification();
            req.peer = new TL_inputPeerUser();
            req.peer.access_hash = user.access_hash;
            req.peer.user_id = user.id;
            if (resendMessage != null) {
                message = resendMessage;
                req.reply_to_msg_id = messageId;
                req.random_id = resendMessage.random_id;
            } else {
                message = new TL_messageService();
                message.random_id = getNextRandomId();
                message.dialog_id = (long) user.id;
                message.unread = true;
                message.out = true;
                int newMessageId = UserConfig.getInstance(this.currentAccount).getNewMessageId();
                message.id = newMessageId;
                message.local_id = newMessageId;
                message.from_id = UserConfig.getInstance(this.currentAccount).getClientUserId();
                message.flags |= 256;
                message.flags |= 8;
                message.reply_to_msg_id = messageId;
                message.to_id = new TL_peerUser();
                message.to_id.user_id = user.id;
                message.date = ConnectionsManager.getInstance(this.currentAccount).getCurrentTime();
                message.action = new TL_messageActionScreenshotTaken();
                UserConfig.getInstance(this.currentAccount).saveConfig(false);
            }
            req.random_id = message.random_id;
            MessageObject newMsgObj = new MessageObject(this.currentAccount, message, false);
            newMsgObj.messageOwner.send_state = 1;
            ArrayList<MessageObject> objArr = new ArrayList();
            objArr.add(newMsgObj);
            MessagesController.getInstance(this.currentAccount).updateInterfaceWithMessages(message.dialog_id, objArr);
            NotificationCenter.getInstance(this.currentAccount).postNotificationName(NotificationCenter.dialogsNeedReload, new Object[0]);
            ArrayList arr = new ArrayList();
            arr.add(message);
            MessagesStorage.getInstance(this.currentAccount).putMessages(arr, false, true, false, 0);
            performSendMessageRequest(req, newMsgObj, null);
        }
    }

    public void sendSticker(Document document, long peer, MessageObject replyingMessageObject) {
        if (document != null) {
            if (((int) peer) == 0) {
                if (MessagesController.getInstance(this.currentAccount).getEncryptedChat(Integer.valueOf((int) (peer >> 32))) != null) {
                    Document newDocument = new TL_document();
                    newDocument.id = document.id;
                    newDocument.access_hash = document.access_hash;
                    newDocument.date = document.date;
                    newDocument.mime_type = document.mime_type;
                    newDocument.size = document.size;
                    newDocument.dc_id = document.dc_id;
                    newDocument.attributes = new ArrayList(document.attributes);
                    if (newDocument.mime_type == null) {
                        newDocument.mime_type = TtmlNode.ANONYMOUS_REGION_ID;
                    }
                    if (document.thumb instanceof TL_photoSize) {
                        File file = FileLoader.getPathToAttach(document.thumb, true);
                        if (file.exists()) {
                            try {
                                int len = (int) file.length();
                                byte[] arr = new byte[((int) file.length())];
                                new RandomAccessFile(file, "r").readFully(arr);
                                newDocument.thumb = new TL_photoCachedSize();
                                newDocument.thumb.location = document.thumb.location;
                                newDocument.thumb.size = document.thumb.size;
                                newDocument.thumb.f25w = document.thumb.f25w;
                                newDocument.thumb.f24h = document.thumb.f24h;
                                newDocument.thumb.type = document.thumb.type;
                                newDocument.thumb.bytes = arr;
                            } catch (Throwable e) {
                                FileLog.m3e(e);
                            }
                        }
                    }
                    if (newDocument.thumb == null) {
                        newDocument.thumb = new TL_photoSizeEmpty();
                        newDocument.thumb.type = "s";
                    }
                    document = newDocument;
                } else {
                    return;
                }
            }
            if (document instanceof TL_document) {
                sendMessage((TL_document) document, null, null, peer, replyingMessageObject, null, null, null, null, 0);
            }
        }
    }

    public int sendMessage(ArrayList<MessageObject> messages, long peer) {
        if (messages == null || messages.isEmpty()) {
            return 0;
        }
        int lower_id = (int) peer;
        int sendResult = 0;
        int a;
        if (lower_id != 0) {
            Chat chat;
            final Peer to_id = MessagesController.getInstance(this.currentAccount).getPeer((int) peer);
            boolean isMegagroup = false;
            boolean isSignature = false;
            boolean canSendStickers = true;
            boolean canSendMedia = true;
            boolean canSendPreview = true;
            if (lower_id <= 0) {
                chat = MessagesController.getInstance(this.currentAccount).getChat(Integer.valueOf(-lower_id));
                if (ChatObject.isChannel(chat)) {
                    isMegagroup = chat.megagroup;
                    isSignature = chat.signatures;
                    if (chat.banned_rights != null) {
                        canSendStickers = !chat.banned_rights.send_stickers;
                        canSendMedia = !chat.banned_rights.send_media;
                        canSendPreview = !chat.banned_rights.embed_links;
                    }
                }
            } else if (MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(lower_id)) == null) {
                return 0;
            }
            LongSparseArray<Long> groupsMap = new LongSparseArray();
            ArrayList<MessageObject> objArr = new ArrayList();
            ArrayList<Message> arr = new ArrayList();
            ArrayList<Long> randomIds = new ArrayList();
            ArrayList<Integer> ids = new ArrayList();
            LongSparseArray<Message> messagesByRandomIds = new LongSparseArray();
            InputPeer inputPeer = MessagesController.getInstance(this.currentAccount).getInputPeer(lower_id);
            int myId = UserConfig.getInstance(this.currentAccount).getClientUserId();
            boolean toMyself = peer == ((long) myId);
            a = 0;
            while (a < messages.size()) {
                MessageObject msgObj = (MessageObject) messages.get(a);
                if (msgObj.getId() > 0 && !msgObj.needDrawBluredPreview()) {
                    if (canSendStickers || !(msgObj.isSticker() || msgObj.isGif() || msgObj.isGame())) {
                        if (canSendMedia || !((msgObj.messageOwner.media instanceof TL_messageMediaPhoto) || (msgObj.messageOwner.media instanceof TL_messageMediaDocument))) {
                            MessageFwdHeader messageFwdHeader;
                            boolean groupedIdChanged = false;
                            Message newMsg = new TL_message();
                            boolean forwardFromSaved = msgObj.getDialogId() == ((long) myId) && msgObj.messageOwner.from_id == UserConfig.getInstance(this.currentAccount).getClientUserId();
                            if (msgObj.isForwarded()) {
                                newMsg.fwd_from = new TL_messageFwdHeader();
                                newMsg.fwd_from.flags = msgObj.messageOwner.fwd_from.flags;
                                newMsg.fwd_from.from_id = msgObj.messageOwner.fwd_from.from_id;
                                newMsg.fwd_from.date = msgObj.messageOwner.fwd_from.date;
                                newMsg.fwd_from.channel_id = msgObj.messageOwner.fwd_from.channel_id;
                                newMsg.fwd_from.channel_post = msgObj.messageOwner.fwd_from.channel_post;
                                newMsg.fwd_from.post_author = msgObj.messageOwner.fwd_from.post_author;
                                newMsg.flags = 4;
                            } else if (!forwardFromSaved) {
                                newMsg.fwd_from = new TL_messageFwdHeader();
                                newMsg.fwd_from.channel_post = msgObj.getId();
                                messageFwdHeader = newMsg.fwd_from;
                                messageFwdHeader.flags |= 4;
                                if (msgObj.isFromUser()) {
                                    newMsg.fwd_from.from_id = msgObj.messageOwner.from_id;
                                    messageFwdHeader = newMsg.fwd_from;
                                    messageFwdHeader.flags |= 1;
                                } else {
                                    newMsg.fwd_from.channel_id = msgObj.messageOwner.to_id.channel_id;
                                    messageFwdHeader = newMsg.fwd_from;
                                    messageFwdHeader.flags |= 2;
                                    if (msgObj.messageOwner.post && msgObj.messageOwner.from_id > 0) {
                                        newMsg.fwd_from.from_id = msgObj.messageOwner.from_id;
                                        messageFwdHeader = newMsg.fwd_from;
                                        messageFwdHeader.flags |= 1;
                                    }
                                }
                                if (msgObj.messageOwner.post_author != null) {
                                    newMsg.fwd_from.post_author = msgObj.messageOwner.post_author;
                                    messageFwdHeader = newMsg.fwd_from;
                                    messageFwdHeader.flags |= 8;
                                } else if (!msgObj.isOutOwner() && msgObj.messageOwner.from_id > 0 && msgObj.messageOwner.post) {
                                    User signUser = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(msgObj.messageOwner.from_id));
                                    if (signUser != null) {
                                        newMsg.fwd_from.post_author = ContactsController.formatName(signUser.first_name, signUser.last_name);
                                        messageFwdHeader = newMsg.fwd_from;
                                        messageFwdHeader.flags |= 8;
                                    }
                                }
                                newMsg.date = msgObj.messageOwner.date;
                                newMsg.flags = 4;
                            }
                            if (peer == ((long) myId) && newMsg.fwd_from != null) {
                                messageFwdHeader = newMsg.fwd_from;
                                messageFwdHeader.flags |= 16;
                                newMsg.fwd_from.saved_from_msg_id = msgObj.getId();
                                newMsg.fwd_from.saved_from_peer = msgObj.messageOwner.to_id;
                            }
                            if (canSendPreview || !(msgObj.messageOwner.media instanceof TL_messageMediaWebPage)) {
                                newMsg.media = msgObj.messageOwner.media;
                            } else {
                                newMsg.media = new TL_messageMediaEmpty();
                            }
                            if (newMsg.media != null) {
                                newMsg.flags |= 512;
                            }
                            if (isMegagroup) {
                                newMsg.flags |= Integer.MIN_VALUE;
                            }
                            if (msgObj.messageOwner.via_bot_id != 0) {
                                newMsg.via_bot_id = msgObj.messageOwner.via_bot_id;
                                newMsg.flags |= 2048;
                            }
                            newMsg.message = msgObj.messageOwner.message;
                            newMsg.fwd_msg_id = msgObj.getId();
                            newMsg.attachPath = msgObj.messageOwner.attachPath;
                            newMsg.entities = msgObj.messageOwner.entities;
                            if (!newMsg.entities.isEmpty()) {
                                newMsg.flags |= 128;
                            }
                            if (newMsg.attachPath == null) {
                                newMsg.attachPath = TtmlNode.ANONYMOUS_REGION_ID;
                            }
                            int newMessageId = UserConfig.getInstance(this.currentAccount).getNewMessageId();
                            newMsg.id = newMessageId;
                            newMsg.local_id = newMessageId;
                            newMsg.out = true;
                            long lastGroupedId = msgObj.messageOwner.grouped_id;
                            if (lastGroupedId != 0) {
                                Long gId = (Long) groupsMap.get(msgObj.messageOwner.grouped_id);
                                if (gId == null) {
                                    gId = Long.valueOf(Utilities.random.nextLong());
                                    groupsMap.put(msgObj.messageOwner.grouped_id, gId);
                                }
                                newMsg.grouped_id = gId.longValue();
                                newMsg.flags |= 131072;
                            }
                            if (a != messages.size() - 1) {
                                if (((MessageObject) messages.get(a + 1)).messageOwner.grouped_id != msgObj.messageOwner.grouped_id) {
                                    groupedIdChanged = true;
                                }
                            }
                            if (to_id.channel_id == 0 || isMegagroup) {
                                newMsg.from_id = UserConfig.getInstance(this.currentAccount).getClientUserId();
                                newMsg.flags |= 256;
                            } else {
                                newMsg.from_id = isSignature ? UserConfig.getInstance(this.currentAccount).getClientUserId() : -to_id.channel_id;
                                newMsg.post = true;
                            }
                            if (newMsg.random_id == 0) {
                                newMsg.random_id = getNextRandomId();
                            }
                            randomIds.add(Long.valueOf(newMsg.random_id));
                            messagesByRandomIds.put(newMsg.random_id, newMsg);
                            ids.add(Integer.valueOf(newMsg.fwd_msg_id));
                            newMsg.date = ConnectionsManager.getInstance(this.currentAccount).getCurrentTime();
                            if (!(inputPeer instanceof TL_inputPeerChannel)) {
                                if ((msgObj.messageOwner.flags & 1024) != 0) {
                                    newMsg.views = msgObj.messageOwner.views;
                                    newMsg.flags |= 1024;
                                }
                                newMsg.unread = true;
                            } else if (isMegagroup) {
                                newMsg.unread = true;
                            } else {
                                newMsg.views = 1;
                                newMsg.flags |= 1024;
                            }
                            newMsg.dialog_id = peer;
                            newMsg.to_id = to_id;
                            if (MessageObject.isVoiceMessage(newMsg) || MessageObject.isRoundVideoMessage(newMsg)) {
                                newMsg.media_unread = true;
                            }
                            if (msgObj.messageOwner.to_id instanceof TL_peerChannel) {
                                newMsg.ttl = -msgObj.messageOwner.to_id.channel_id;
                            }
                            MessageObject messageObject = new MessageObject(this.currentAccount, newMsg, true);
                            messageObject.messageOwner.send_state = 1;
                            objArr.add(messageObject);
                            arr.add(newMsg);
                            putToSendingMessages(newMsg);
                            if (BuildVars.LOGS_ENABLED) {
                                FileLog.m0d("forward message user_id = " + inputPeer.user_id + " chat_id = " + inputPeer.chat_id + " channel_id = " + inputPeer.channel_id + " access_hash = " + inputPeer.access_hash);
                            }
                            if (!((groupedIdChanged && arr.size() > 0) || arr.size() == 100 || a == messages.size() - 1)) {
                                if (a != messages.size() - 1) {
                                    if (((MessageObject) messages.get(a + 1)).getDialogId() == msgObj.getDialogId()) {
                                    }
                                }
                            }
                            MessagesStorage.getInstance(this.currentAccount).putMessages(new ArrayList(arr), false, true, false, 0);
                            MessagesController.getInstance(this.currentAccount).updateInterfaceWithMessages(peer, objArr);
                            NotificationCenter.getInstance(this.currentAccount).postNotificationName(NotificationCenter.dialogsNeedReload, new Object[0]);
                            UserConfig.getInstance(this.currentAccount).saveConfig(false);
                            final TL_messages_forwardMessages req = new TL_messages_forwardMessages();
                            req.to_peer = inputPeer;
                            req.grouped = lastGroupedId != 0;
                            if (req.to_peer instanceof TL_inputPeerChannel) {
                                req.silent = MessagesController.getNotificationsSettings(this.currentAccount).getBoolean("silent_" + peer, false);
                            }
                            if (msgObj.messageOwner.to_id instanceof TL_peerChannel) {
                                chat = MessagesController.getInstance(this.currentAccount).getChat(Integer.valueOf(msgObj.messageOwner.to_id.channel_id));
                                req.from_peer = new TL_inputPeerChannel();
                                req.from_peer.channel_id = msgObj.messageOwner.to_id.channel_id;
                                if (chat != null) {
                                    req.from_peer.access_hash = chat.access_hash;
                                }
                            } else {
                                req.from_peer = new TL_inputPeerEmpty();
                            }
                            req.random_id = randomIds;
                            req.id = ids;
                            boolean z = messages.size() == 1 && ((MessageObject) messages.get(0)).messageOwner.with_my_score;
                            req.with_my_score = z;
                            final ArrayList<Message> newMsgObjArr = arr;
                            final ArrayList<MessageObject> newMsgArr = objArr;
                            final LongSparseArray<Message> messagesByRandomIdsFinal = messagesByRandomIds;
                            final boolean isMegagroupFinal = isMegagroup;
                            final long j = peer;
                            final boolean z2 = toMyself;
                            ConnectionsManager.getInstance(this.currentAccount).sendRequest(req, new RequestDelegate() {
                                public void run(TLObject response, TL_error error) {
                                    int a;
                                    final Message newMsgObj;
                                    if (error == null) {
                                        Update update;
                                        SparseLongArray newMessagesByIds = new SparseLongArray();
                                        Updates updates = (Updates) response;
                                        a = 0;
                                        while (a < updates.updates.size()) {
                                            update = (Update) updates.updates.get(a);
                                            if (update instanceof TL_updateMessageID) {
                                                TL_updateMessageID updateMessageID = (TL_updateMessageID) update;
                                                newMessagesByIds.put(updateMessageID.id, updateMessageID.random_id);
                                                updates.updates.remove(a);
                                                a--;
                                            }
                                            a++;
                                        }
                                        Integer value = (Integer) MessagesController.getInstance(SendMessagesHelper.this.currentAccount).dialogs_read_outbox_max.get(Long.valueOf(j));
                                        if (value == null) {
                                            value = Integer.valueOf(MessagesStorage.getInstance(SendMessagesHelper.this.currentAccount).getDialogReadMax(true, j));
                                            MessagesController.getInstance(SendMessagesHelper.this.currentAccount).dialogs_read_outbox_max.put(Long.valueOf(j), value);
                                        }
                                        int sentCount = 0;
                                        a = 0;
                                        while (a < updates.updates.size()) {
                                            update = (Update) updates.updates.get(a);
                                            if ((update instanceof TL_updateNewMessage) || (update instanceof TL_updateNewChannelMessage)) {
                                                Message message;
                                                updates.updates.remove(a);
                                                a--;
                                                if (update instanceof TL_updateNewMessage) {
                                                    TL_updateNewMessage updateNewMessage = (TL_updateNewMessage) update;
                                                    message = updateNewMessage.message;
                                                    MessagesController.getInstance(SendMessagesHelper.this.currentAccount).processNewDifferenceParams(-1, updateNewMessage.pts, -1, updateNewMessage.pts_count);
                                                } else {
                                                    TL_updateNewChannelMessage updateNewChannelMessage = (TL_updateNewChannelMessage) update;
                                                    message = updateNewChannelMessage.message;
                                                    MessagesController.getInstance(SendMessagesHelper.this.currentAccount).processNewChannelDifferenceParams(updateNewChannelMessage.pts, updateNewChannelMessage.pts_count, message.to_id.channel_id);
                                                    if (isMegagroupFinal) {
                                                        message.flags |= Integer.MIN_VALUE;
                                                    }
                                                }
                                                ImageLoader.saveMessageThumbs(message);
                                                message.unread = value.intValue() < message.id;
                                                if (z2) {
                                                    message.out = true;
                                                    message.unread = false;
                                                    message.media_unread = false;
                                                }
                                                long random_id = newMessagesByIds.get(message.id);
                                                if (random_id != 0) {
                                                    newMsgObj = (Message) messagesByRandomIdsFinal.get(random_id);
                                                    if (newMsgObj != null) {
                                                        int index = newMsgObjArr.indexOf(newMsgObj);
                                                        if (index != -1) {
                                                            MessageObject msgObj = (MessageObject) newMsgArr.get(index);
                                                            newMsgObjArr.remove(index);
                                                            newMsgArr.remove(index);
                                                            final int oldId = newMsgObj.id;
                                                            final ArrayList<Message> sentMessages = new ArrayList();
                                                            sentMessages.add(message);
                                                            newMsgObj.id = message.id;
                                                            sentCount++;
                                                            SendMessagesHelper.this.updateMediaPaths(msgObj, message, null, true);
                                                            MessagesStorage.getInstance(SendMessagesHelper.this.currentAccount).getStorageQueue().postRunnable(new Runnable() {

                                                                /* renamed from: org.telegram.messenger.SendMessagesHelper$5$1$1 */
                                                                class C05481 implements Runnable {
                                                                    C05481() {
                                                                    }

                                                                    public void run() {
                                                                        newMsgObj.send_state = 0;
                                                                        DataQuery.getInstance(SendMessagesHelper.this.currentAccount).increasePeerRaiting(j);
                                                                        NotificationCenter.getInstance(SendMessagesHelper.this.currentAccount).postNotificationName(NotificationCenter.messageReceivedByServer, Integer.valueOf(oldId), Integer.valueOf(message.id), message, Long.valueOf(j));
                                                                        SendMessagesHelper.this.processSentMessage(oldId);
                                                                        SendMessagesHelper.this.removeFromSendingMessages(oldId);
                                                                    }
                                                                }

                                                                public void run() {
                                                                    MessagesStorage.getInstance(SendMessagesHelper.this.currentAccount).updateMessageStateAndId(newMsgObj.random_id, Integer.valueOf(oldId), newMsgObj.id, 0, false, to_id.channel_id);
                                                                    MessagesStorage.getInstance(SendMessagesHelper.this.currentAccount).putMessages(sentMessages, true, false, false, 0);
                                                                    AndroidUtilities.runOnUIThread(new C05481());
                                                                }
                                                            });
                                                        }
                                                    }
                                                }
                                            }
                                            a++;
                                        }
                                        if (!updates.updates.isEmpty()) {
                                            MessagesController.getInstance(SendMessagesHelper.this.currentAccount).processUpdates(updates, false);
                                        }
                                        StatsController.getInstance(SendMessagesHelper.this.currentAccount).incrementSentItemsCount(ConnectionsManager.getCurrentNetworkType(), 1, sentCount);
                                    } else {
                                        final TL_error tL_error = error;
                                        AndroidUtilities.runOnUIThread(new Runnable() {
                                            public void run() {
                                                AlertsCreator.processError(SendMessagesHelper.this.currentAccount, tL_error, null, req, new Object[0]);
                                            }
                                        });
                                    }
                                    for (a = 0; a < newMsgObjArr.size(); a++) {
                                        newMsgObj = (Message) newMsgObjArr.get(a);
                                        MessagesStorage.getInstance(SendMessagesHelper.this.currentAccount).markMessageAsSendError(newMsgObj);
                                        AndroidUtilities.runOnUIThread(new Runnable() {
                                            public void run() {
                                                newMsgObj.send_state = 2;
                                                NotificationCenter.getInstance(SendMessagesHelper.this.currentAccount).postNotificationName(NotificationCenter.messageSendError, Integer.valueOf(newMsgObj.id));
                                                SendMessagesHelper.this.processSentMessage(newMsgObj.id);
                                                SendMessagesHelper.this.removeFromSendingMessages(newMsgObj.id);
                                            }
                                        });
                                    }
                                }
                            }, 68);
                            if (a != messages.size() - 1) {
                                objArr = new ArrayList();
                                arr = new ArrayList();
                                randomIds = new ArrayList();
                                ids = new ArrayList();
                                messagesByRandomIds = new LongSparseArray();
                            }
                        } else if (sendResult == 0) {
                            sendResult = 2;
                        }
                    } else if (sendResult == 0) {
                        sendResult = 1;
                    }
                }
                a++;
            }
            return sendResult;
        }
        for (a = 0; a < messages.size(); a++) {
            processForwardFromMyName((MessageObject) messages.get(a), peer);
        }
        return 0;
    }

    public int editMessage(MessageObject messageObject, String message, boolean searchLinks, final BaseFragment fragment, ArrayList<MessageEntity> entities, final Runnable callback) {
        boolean z = false;
        if (fragment == null || fragment.getParentActivity() == null || callback == null) {
            return 0;
        }
        final TL_messages_editMessage req = new TL_messages_editMessage();
        req.peer = MessagesController.getInstance(this.currentAccount).getInputPeer((int) messageObject.getDialogId());
        req.message = message;
        req.flags |= 2048;
        req.id = messageObject.getId();
        if (!searchLinks) {
            z = true;
        }
        req.no_webpage = z;
        if (entities != null) {
            req.entities = entities;
            req.flags |= 8;
        }
        return ConnectionsManager.getInstance(this.currentAccount).sendRequest(req, new RequestDelegate() {

            /* renamed from: org.telegram.messenger.SendMessagesHelper$6$1 */
            class C05531 implements Runnable {
                C05531() {
                }

                public void run() {
                    callback.run();
                }
            }

            public void run(TLObject response, final TL_error error) {
                AndroidUtilities.runOnUIThread(new C05531());
                if (error == null) {
                    MessagesController.getInstance(SendMessagesHelper.this.currentAccount).processUpdates((Updates) response, false);
                } else {
                    AndroidUtilities.runOnUIThread(new Runnable() {
                        public void run() {
                            AlertsCreator.processError(SendMessagesHelper.this.currentAccount, error, fragment, req, new Object[0]);
                        }
                    });
                }
            }
        });
    }

    private void sendLocation(Location location) {
        MessageMedia mediaGeo = new TL_messageMediaGeo();
        mediaGeo.geo = new TL_geoPoint();
        mediaGeo.geo.lat = location.getLatitude();
        mediaGeo.geo._long = location.getLongitude();
        for (Entry<String, MessageObject> entry : this.waitingForLocation.entrySet()) {
            MessageObject messageObject = (MessageObject) entry.getValue();
            sendMessage(mediaGeo, messageObject.getDialogId(), messageObject, null, null);
        }
    }

    public void sendCurrentLocation(MessageObject messageObject, KeyboardButton button) {
        if (messageObject != null && button != null) {
            this.waitingForLocation.put(messageObject.getDialogId() + "_" + messageObject.getId() + "_" + Utilities.bytesToHex(button.data) + "_" + (button instanceof TL_keyboardButtonGame ? "1" : "0"), messageObject);
            this.locationProvider.start();
        }
    }

    public boolean isSendingCurrentLocation(MessageObject messageObject, KeyboardButton button) {
        if (messageObject == null || button == null) {
            return false;
        }
        return this.waitingForLocation.containsKey(messageObject.getDialogId() + "_" + messageObject.getId() + "_" + Utilities.bytesToHex(button.data) + "_" + (button instanceof TL_keyboardButtonGame ? "1" : "0"));
    }

    public void sendNotificationCallback(long dialogId, int msgId, byte[] data) {
        final long j = dialogId;
        final int i = msgId;
        final byte[] bArr = data;
        AndroidUtilities.runOnUIThread(new Runnable() {
            public void run() {
                int lowerId = (int) j;
                final String key = j + "_" + i + "_" + Utilities.bytesToHex(bArr) + "_" + 0;
                SendMessagesHelper.this.waitingForCallback.put(key, Boolean.valueOf(true));
                if (lowerId > 0) {
                    if (MessagesController.getInstance(SendMessagesHelper.this.currentAccount).getUser(Integer.valueOf(lowerId)) == null) {
                        User user = MessagesStorage.getInstance(SendMessagesHelper.this.currentAccount).getUserSync(lowerId);
                        if (user != null) {
                            MessagesController.getInstance(SendMessagesHelper.this.currentAccount).putUser(user, true);
                        }
                    }
                } else if (MessagesController.getInstance(SendMessagesHelper.this.currentAccount).getChat(Integer.valueOf(-lowerId)) == null) {
                    Chat chat = MessagesStorage.getInstance(SendMessagesHelper.this.currentAccount).getChatSync(-lowerId);
                    if (chat != null) {
                        MessagesController.getInstance(SendMessagesHelper.this.currentAccount).putChat(chat, true);
                    }
                }
                TL_messages_getBotCallbackAnswer req = new TL_messages_getBotCallbackAnswer();
                req.peer = MessagesController.getInstance(SendMessagesHelper.this.currentAccount).getInputPeer(lowerId);
                req.msg_id = i;
                req.game = false;
                if (bArr != null) {
                    req.flags |= 1;
                    req.data = bArr;
                }
                ConnectionsManager.getInstance(SendMessagesHelper.this.currentAccount).sendRequest(req, new RequestDelegate() {

                    /* renamed from: org.telegram.messenger.SendMessagesHelper$7$1$1 */
                    class C05561 implements Runnable {
                        C05561() {
                        }

                        public void run() {
                            SendMessagesHelper.this.waitingForCallback.remove(key);
                        }
                    }

                    public void run(TLObject response, TL_error error) {
                        AndroidUtilities.runOnUIThread(new C05561());
                    }
                }, 2);
                MessagesController.getInstance(SendMessagesHelper.this.currentAccount).markDialogAsRead(j, i, i, 0, false, 0, true);
            }
        });
    }

    public void sendCallback(boolean cache, MessageObject messageObject, KeyboardButton button, ChatActivity parentFragment) {
        if (messageObject != null && button != null && parentFragment != null) {
            boolean cacheFinal;
            int type;
            if (button instanceof TL_keyboardButtonGame) {
                cacheFinal = false;
                type = 1;
            } else {
                cacheFinal = cache;
                if (button instanceof TL_keyboardButtonBuy) {
                    type = 2;
                } else {
                    type = 0;
                }
            }
            final String key = messageObject.getDialogId() + "_" + messageObject.getId() + "_" + Utilities.bytesToHex(button.data) + "_" + type;
            this.waitingForCallback.put(key, Boolean.valueOf(true));
            final MessageObject messageObject2 = messageObject;
            final KeyboardButton keyboardButton = button;
            final ChatActivity chatActivity = parentFragment;
            RequestDelegate requestDelegate = new RequestDelegate() {
                public void run(final TLObject response, TL_error error) {
                    AndroidUtilities.runOnUIThread(new Runnable() {
                        public void run() {
                            SendMessagesHelper.this.waitingForCallback.remove(key);
                            if (cacheFinal && response == null) {
                                SendMessagesHelper.this.sendCallback(false, messageObject2, keyboardButton, chatActivity);
                            } else if (response == null) {
                            } else {
                                if (!(keyboardButton instanceof TL_keyboardButtonBuy)) {
                                    TL_messages_botCallbackAnswer res = response;
                                    if (!(cacheFinal || res.cache_time == 0)) {
                                        MessagesStorage.getInstance(SendMessagesHelper.this.currentAccount).saveBotCache(key, res);
                                    }
                                    int uid;
                                    User user;
                                    if (res.message != null) {
                                        if (!res.alert) {
                                            uid = messageObject2.messageOwner.from_id;
                                            if (messageObject2.messageOwner.via_bot_id != 0) {
                                                uid = messageObject2.messageOwner.via_bot_id;
                                            }
                                            String name = null;
                                            if (uid > 0) {
                                                user = MessagesController.getInstance(SendMessagesHelper.this.currentAccount).getUser(Integer.valueOf(uid));
                                                if (user != null) {
                                                    name = ContactsController.formatName(user.first_name, user.last_name);
                                                }
                                            } else {
                                                Chat chat = MessagesController.getInstance(SendMessagesHelper.this.currentAccount).getChat(Integer.valueOf(-uid));
                                                if (chat != null) {
                                                    name = chat.title;
                                                }
                                            }
                                            if (name == null) {
                                                name = "bot";
                                            }
                                            chatActivity.showAlert(name, res.message);
                                        } else if (chatActivity.getParentActivity() != null) {
                                            Builder builder = new Builder(chatActivity.getParentActivity());
                                            builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
                                            builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), null);
                                            builder.setMessage(res.message);
                                            chatActivity.showDialog(builder.create());
                                        }
                                    } else if (res.url != null && chatActivity.getParentActivity() != null) {
                                        uid = messageObject2.messageOwner.from_id;
                                        if (messageObject2.messageOwner.via_bot_id != 0) {
                                            uid = messageObject2.messageOwner.via_bot_id;
                                        }
                                        user = MessagesController.getInstance(SendMessagesHelper.this.currentAccount).getUser(Integer.valueOf(uid));
                                        boolean verified = user != null && user.verified;
                                        if (keyboardButton instanceof TL_keyboardButtonGame) {
                                            TL_game game = messageObject2.messageOwner.media instanceof TL_messageMediaGame ? messageObject2.messageOwner.media.game : null;
                                            if (game != null) {
                                                boolean z;
                                                ChatActivity chatActivity = chatActivity;
                                                MessageObject messageObject = messageObject2;
                                                String str = res.url;
                                                if (verified || !MessagesController.getNotificationsSettings(SendMessagesHelper.this.currentAccount).getBoolean("askgame_" + uid, true)) {
                                                    z = false;
                                                } else {
                                                    z = true;
                                                }
                                                chatActivity.showOpenGameAlert(game, messageObject, str, z, uid);
                                                return;
                                            }
                                            return;
                                        }
                                        chatActivity.showOpenUrlAlert(res.url, false);
                                    }
                                } else if (response instanceof TL_payments_paymentForm) {
                                    TL_payments_paymentForm form = response;
                                    MessagesController.getInstance(SendMessagesHelper.this.currentAccount).putUsers(form.users, false);
                                    chatActivity.presentFragment(new PaymentFormActivity(form, messageObject2));
                                } else if (response instanceof TL_payments_paymentReceipt) {
                                    chatActivity.presentFragment(new PaymentFormActivity(messageObject2, (TL_payments_paymentReceipt) response));
                                }
                            }
                        }
                    });
                }
            };
            if (cacheFinal) {
                MessagesStorage.getInstance(this.currentAccount).getBotCache(key, requestDelegate);
            } else if (!(button instanceof TL_keyboardButtonBuy)) {
                TL_messages_getBotCallbackAnswer req = new TL_messages_getBotCallbackAnswer();
                req.peer = MessagesController.getInstance(this.currentAccount).getInputPeer((int) messageObject.getDialogId());
                req.msg_id = messageObject.getId();
                req.game = button instanceof TL_keyboardButtonGame;
                if (button.data != null) {
                    req.flags |= 1;
                    req.data = button.data;
                }
                ConnectionsManager.getInstance(this.currentAccount).sendRequest(req, requestDelegate, 2);
            } else if ((messageObject.messageOwner.media.flags & 4) == 0) {
                TL_payments_getPaymentForm req2 = new TL_payments_getPaymentForm();
                req2.msg_id = messageObject.getId();
                ConnectionsManager.getInstance(this.currentAccount).sendRequest(req2, requestDelegate, 2);
            } else {
                TL_payments_getPaymentReceipt req3 = new TL_payments_getPaymentReceipt();
                req3.msg_id = messageObject.messageOwner.media.receipt_msg_id;
                ConnectionsManager.getInstance(this.currentAccount).sendRequest(req3, requestDelegate, 2);
            }
        }
    }

    public boolean isSendingCallback(MessageObject messageObject, KeyboardButton button) {
        if (messageObject == null || button == null) {
            return false;
        }
        int type;
        if (button instanceof TL_keyboardButtonGame) {
            type = 1;
        } else if (button instanceof TL_keyboardButtonBuy) {
            type = 2;
        } else {
            type = 0;
        }
        return this.waitingForCallback.containsKey(messageObject.getDialogId() + "_" + messageObject.getId() + "_" + Utilities.bytesToHex(button.data) + "_" + type);
    }

    public void sendGame(InputPeer peer, TL_inputMediaGame game, long random_id, long taskId) {
        Throwable e;
        long newTaskId;
        if (peer != null && game != null) {
            TL_messages_sendMedia request = new TL_messages_sendMedia();
            request.peer = peer;
            if (request.peer instanceof TL_inputPeerChannel) {
                request.silent = MessagesController.getNotificationsSettings(this.currentAccount).getBoolean("silent_" + peer.channel_id, false);
            }
            request.random_id = random_id != 0 ? random_id : getNextRandomId();
            request.message = TtmlNode.ANONYMOUS_REGION_ID;
            request.media = game;
            if (taskId == 0) {
                NativeByteBuffer data = null;
                try {
                    NativeByteBuffer data2 = new NativeByteBuffer(((peer.getObjectSize() + game.getObjectSize()) + 4) + 8);
                    try {
                        data2.writeInt32(3);
                        data2.writeInt64(random_id);
                        peer.serializeToStream(data2);
                        game.serializeToStream(data2);
                        data = data2;
                    } catch (Exception e2) {
                        e = e2;
                        data = data2;
                        FileLog.m3e(e);
                        newTaskId = MessagesStorage.getInstance(this.currentAccount).createPendingTask(data);
                        ConnectionsManager.getInstance(this.currentAccount).sendRequest(request, new RequestDelegate() {
                            public void run(TLObject response, TL_error error) {
                                if (error == null) {
                                    MessagesController.getInstance(SendMessagesHelper.this.currentAccount).processUpdates((Updates) response, false);
                                }
                                if (newTaskId != 0) {
                                    MessagesStorage.getInstance(SendMessagesHelper.this.currentAccount).removePendingTask(newTaskId);
                                }
                            }
                        });
                    }
                } catch (Exception e3) {
                    e = e3;
                    FileLog.m3e(e);
                    newTaskId = MessagesStorage.getInstance(this.currentAccount).createPendingTask(data);
                    ConnectionsManager.getInstance(this.currentAccount).sendRequest(request, /* anonymous class already generated */);
                }
                newTaskId = MessagesStorage.getInstance(this.currentAccount).createPendingTask(data);
            } else {
                newTaskId = taskId;
            }
            ConnectionsManager.getInstance(this.currentAccount).sendRequest(request, /* anonymous class already generated */);
        }
    }

    public void sendMessage(MessageObject retryMessageObject) {
        sendMessage(null, null, null, null, null, null, null, null, retryMessageObject.getDialogId(), retryMessageObject.messageOwner.attachPath, null, null, true, retryMessageObject, null, retryMessageObject.messageOwner.reply_markup, retryMessageObject.messageOwner.params, 0);
    }

    public void sendMessage(User user, long peer, MessageObject reply_to_msg, ReplyMarkup replyMarkup, HashMap<String, String> params) {
        sendMessage(null, null, null, null, null, user, null, null, peer, null, reply_to_msg, null, true, null, null, replyMarkup, params, 0);
    }

    public void sendMessage(TL_document document, VideoEditedInfo videoEditedInfo, String path, long peer, MessageObject reply_to_msg, String caption, ArrayList<MessageEntity> entities, ReplyMarkup replyMarkup, HashMap<String, String> params, int ttl) {
        sendMessage(null, caption, null, null, videoEditedInfo, null, document, null, peer, path, reply_to_msg, null, true, null, entities, replyMarkup, params, ttl);
    }

    public void sendMessage(String message, long peer, MessageObject reply_to_msg, WebPage webPage, boolean searchLinks, ArrayList<MessageEntity> entities, ReplyMarkup replyMarkup, HashMap<String, String> params) {
        sendMessage(message, null, null, null, null, null, null, null, peer, null, reply_to_msg, webPage, searchLinks, null, entities, replyMarkup, params, 0);
    }

    public void sendMessage(MessageMedia location, long peer, MessageObject reply_to_msg, ReplyMarkup replyMarkup, HashMap<String, String> params) {
        sendMessage(null, null, location, null, null, null, null, null, peer, null, reply_to_msg, null, true, null, null, replyMarkup, params, 0);
    }

    public void sendMessage(TL_game game, long peer, ReplyMarkup replyMarkup, HashMap<String, String> params) {
        sendMessage(null, null, null, null, null, null, null, game, peer, null, null, null, true, null, null, replyMarkup, params, 0);
    }

    public void sendMessage(TL_photo photo, String path, long peer, MessageObject reply_to_msg, String caption, ArrayList<MessageEntity> entities, ReplyMarkup replyMarkup, HashMap<String, String> params, int ttl) {
        sendMessage(null, caption, null, photo, null, null, null, null, peer, path, reply_to_msg, null, true, null, entities, replyMarkup, params, ttl);
    }

    private void sendMessage(java.lang.String r68, java.lang.String r69, org.telegram.tgnet.TLRPC.MessageMedia r70, org.telegram.tgnet.TLRPC.TL_photo r71, org.telegram.messenger.VideoEditedInfo r72, org.telegram.tgnet.TLRPC.User r73, org.telegram.tgnet.TLRPC.TL_document r74, org.telegram.tgnet.TLRPC.TL_game r75, long r76, java.lang.String r78, org.telegram.messenger.MessageObject r79, org.telegram.tgnet.TLRPC.WebPage r80, boolean r81, org.telegram.messenger.MessageObject r82, java.util.ArrayList<org.telegram.tgnet.TLRPC.MessageEntity> r83, org.telegram.tgnet.TLRPC.ReplyMarkup r84, java.util.HashMap<java.lang.String, java.lang.String> r85, int r86) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Unknown predecessor block by arg (r28_40 'delayedMessage' org.telegram.messenger.SendMessagesHelper$DelayedMessage) in PHI: PHI: (r28_41 'delayedMessage' org.telegram.messenger.SendMessagesHelper$DelayedMessage) = (r28_40 'delayedMessage' org.telegram.messenger.SendMessagesHelper$DelayedMessage), (r28_42 'delayedMessage' org.telegram.messenger.SendMessagesHelper$DelayedMessage) binds: {(r28_40 'delayedMessage' org.telegram.messenger.SendMessagesHelper$DelayedMessage)=B:400:?, (r28_42 'delayedMessage' org.telegram.messenger.SendMessagesHelper$DelayedMessage)=B:874:0x1a41}
	at jadx.core.dex.instructions.PhiInsn.replaceArg(PhiInsn.java:79)
	at jadx.core.dex.visitors.ModVisitor.processInvoke(ModVisitor.java:222)
	at jadx.core.dex.visitors.ModVisitor.replaceStep(ModVisitor.java:83)
	at jadx.core.dex.visitors.ModVisitor.visit(ModVisitor.java:68)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:60)
	at jadx.core.ProcessClass.process(ProcessClass.java:39)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:282)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
*/
        /*
        r67 = this;
        r8 = 0;
        r4 = (r76 > r8 ? 1 : (r76 == r8 ? 0 : -1));
        if (r4 != 0) goto L_0x0007;
    L_0x0006:
        return;
    L_0x0007:
        if (r68 != 0) goto L_0x000e;
    L_0x0009:
        if (r69 != 0) goto L_0x000e;
    L_0x000b:
        r69 = "";
    L_0x000e:
        r51 = 0;
        if (r85 == 0) goto L_0x0028;
    L_0x0012:
        r4 = "originalPath";
        r0 = r85;
        r4 = r0.containsKey(r4);
        if (r4 == 0) goto L_0x0028;
    L_0x001d:
        r4 = "originalPath";
        r0 = r85;
        r51 = r0.get(r4);
        r51 = (java.lang.String) r51;
    L_0x0028:
        r46 = 0;
        r48 = 0;
        r28 = 0;
        r63 = -1;
        r0 = r76;
        r0 = (int) r0;
        r42 = r0;
        r4 = 32;
        r8 = r76 >> r4;
        r0 = (int) r8;
        r35 = r0;
        r38 = 0;
        r16 = 0;
        if (r42 == 0) goto L_0x00a4;
    L_0x0042:
        r0 = r67;
        r4 = r0.currentAccount;
        r4 = org.telegram.messenger.MessagesController.getInstance(r4);
        r0 = r42;
        r57 = r4.getInputPeer(r0);
    L_0x0050:
        r58 = 0;
        if (r42 != 0) goto L_0x00a7;
    L_0x0054:
        r0 = r67;
        r4 = r0.currentAccount;
        r4 = org.telegram.messenger.MessagesController.getInstance(r4);
        r6 = java.lang.Integer.valueOf(r35);
        r16 = r4.getEncryptedChat(r6);
        if (r16 != 0) goto L_0x00cb;
    L_0x0066:
        if (r82 == 0) goto L_0x0006;
    L_0x0068:
        r0 = r67;
        r4 = r0.currentAccount;
        r4 = org.telegram.messenger.MessagesStorage.getInstance(r4);
        r0 = r82;
        r6 = r0.messageOwner;
        r4.markMessageAsSendError(r6);
        r0 = r82;
        r4 = r0.messageOwner;
        r6 = 2;
        r4.send_state = r6;
        r0 = r67;
        r4 = r0.currentAccount;
        r4 = org.telegram.messenger.NotificationCenter.getInstance(r4);
        r6 = org.telegram.messenger.NotificationCenter.messageSendError;
        r8 = 1;
        r8 = new java.lang.Object[r8];
        r9 = 0;
        r11 = r82.getId();
        r11 = java.lang.Integer.valueOf(r11);
        r8[r9] = r11;
        r4.postNotificationName(r6, r8);
        r4 = r82.getId();
        r0 = r67;
        r0.processSentMessage(r4);
        goto L_0x0006;
    L_0x00a4:
        r57 = 0;
        goto L_0x0050;
    L_0x00a7:
        r0 = r57;
        r4 = r0 instanceof org.telegram.tgnet.TLRPC.TL_inputPeerChannel;
        if (r4 == 0) goto L_0x00cb;
    L_0x00ad:
        r0 = r67;
        r4 = r0.currentAccount;
        r4 = org.telegram.messenger.MessagesController.getInstance(r4);
        r0 = r57;
        r6 = r0.channel_id;
        r6 = java.lang.Integer.valueOf(r6);
        r26 = r4.getChat(r6);
        if (r26 == 0) goto L_0x0253;
    L_0x00c3:
        r0 = r26;
        r4 = r0.megagroup;
        if (r4 != 0) goto L_0x0253;
    L_0x00c9:
        r38 = 1;
    L_0x00cb:
        if (r82 == 0) goto L_0x0344;
    L_0x00cd:
        r0 = r82;	 Catch:{ Exception -> 0x0213 }
        r0 = r0.messageOwner;	 Catch:{ Exception -> 0x0213 }
        r46 = r0;	 Catch:{ Exception -> 0x0213 }
        r4 = r82.isForwarded();	 Catch:{ Exception -> 0x0213 }
        if (r4 == 0) goto L_0x0257;	 Catch:{ Exception -> 0x0213 }
    L_0x00d9:
        r63 = 4;	 Catch:{ Exception -> 0x0213 }
    L_0x00db:
        r0 = r46;	 Catch:{ Exception -> 0x0213 }
        r8 = r0.random_id;	 Catch:{ Exception -> 0x0213 }
        r14 = 0;	 Catch:{ Exception -> 0x0213 }
        r4 = (r8 > r14 ? 1 : (r8 == r14 ? 0 : -1));	 Catch:{ Exception -> 0x0213 }
        if (r4 != 0) goto L_0x00ed;	 Catch:{ Exception -> 0x0213 }
    L_0x00e5:
        r8 = r67.getNextRandomId();	 Catch:{ Exception -> 0x0213 }
        r0 = r46;	 Catch:{ Exception -> 0x0213 }
        r0.random_id = r8;	 Catch:{ Exception -> 0x0213 }
    L_0x00ed:
        if (r85 == 0) goto L_0x0122;	 Catch:{ Exception -> 0x0213 }
    L_0x00ef:
        r4 = "bot";	 Catch:{ Exception -> 0x0213 }
        r0 = r85;	 Catch:{ Exception -> 0x0213 }
        r4 = r0.containsKey(r4);	 Catch:{ Exception -> 0x0213 }
        if (r4 == 0) goto L_0x0122;	 Catch:{ Exception -> 0x0213 }
    L_0x00fa:
        if (r16 == 0) goto L_0x0714;	 Catch:{ Exception -> 0x0213 }
    L_0x00fc:
        r4 = "bot_name";	 Catch:{ Exception -> 0x0213 }
        r0 = r85;	 Catch:{ Exception -> 0x0213 }
        r4 = r0.get(r4);	 Catch:{ Exception -> 0x0213 }
        r4 = (java.lang.String) r4;	 Catch:{ Exception -> 0x0213 }
        r0 = r46;	 Catch:{ Exception -> 0x0213 }
        r0.via_bot_name = r4;	 Catch:{ Exception -> 0x0213 }
        r0 = r46;	 Catch:{ Exception -> 0x0213 }
        r4 = r0.via_bot_name;	 Catch:{ Exception -> 0x0213 }
        if (r4 != 0) goto L_0x0118;	 Catch:{ Exception -> 0x0213 }
    L_0x0111:
        r4 = "";	 Catch:{ Exception -> 0x0213 }
        r0 = r46;	 Catch:{ Exception -> 0x0213 }
        r0.via_bot_name = r4;	 Catch:{ Exception -> 0x0213 }
    L_0x0118:
        r0 = r46;	 Catch:{ Exception -> 0x0213 }
        r4 = r0.flags;	 Catch:{ Exception -> 0x0213 }
        r4 = r4 | 2048;	 Catch:{ Exception -> 0x0213 }
        r0 = r46;	 Catch:{ Exception -> 0x0213 }
        r0.flags = r4;	 Catch:{ Exception -> 0x0213 }
    L_0x0122:
        r0 = r85;	 Catch:{ Exception -> 0x0213 }
        r1 = r46;	 Catch:{ Exception -> 0x0213 }
        r1.params = r0;	 Catch:{ Exception -> 0x0213 }
        if (r82 == 0) goto L_0x0130;	 Catch:{ Exception -> 0x0213 }
    L_0x012a:
        r0 = r82;	 Catch:{ Exception -> 0x0213 }
        r4 = r0.resendAsIs;	 Catch:{ Exception -> 0x0213 }
        if (r4 != 0) goto L_0x0183;	 Catch:{ Exception -> 0x0213 }
    L_0x0130:
        r0 = r67;	 Catch:{ Exception -> 0x0213 }
        r4 = r0.currentAccount;	 Catch:{ Exception -> 0x0213 }
        r4 = org.telegram.tgnet.ConnectionsManager.getInstance(r4);	 Catch:{ Exception -> 0x0213 }
        r4 = r4.getCurrentTime();	 Catch:{ Exception -> 0x0213 }
        r0 = r46;	 Catch:{ Exception -> 0x0213 }
        r0.date = r4;	 Catch:{ Exception -> 0x0213 }
        r0 = r57;	 Catch:{ Exception -> 0x0213 }
        r4 = r0 instanceof org.telegram.tgnet.TLRPC.TL_inputPeerChannel;	 Catch:{ Exception -> 0x0213 }
        if (r4 == 0) goto L_0x074a;	 Catch:{ Exception -> 0x0213 }
    L_0x0146:
        if (r38 == 0) goto L_0x0157;	 Catch:{ Exception -> 0x0213 }
    L_0x0148:
        r4 = 1;	 Catch:{ Exception -> 0x0213 }
        r0 = r46;	 Catch:{ Exception -> 0x0213 }
        r0.views = r4;	 Catch:{ Exception -> 0x0213 }
        r0 = r46;	 Catch:{ Exception -> 0x0213 }
        r4 = r0.flags;	 Catch:{ Exception -> 0x0213 }
        r4 = r4 | 1024;	 Catch:{ Exception -> 0x0213 }
        r0 = r46;	 Catch:{ Exception -> 0x0213 }
        r0.flags = r4;	 Catch:{ Exception -> 0x0213 }
    L_0x0157:
        r0 = r67;	 Catch:{ Exception -> 0x0213 }
        r4 = r0.currentAccount;	 Catch:{ Exception -> 0x0213 }
        r4 = org.telegram.messenger.MessagesController.getInstance(r4);	 Catch:{ Exception -> 0x0213 }
        r0 = r57;	 Catch:{ Exception -> 0x0213 }
        r6 = r0.channel_id;	 Catch:{ Exception -> 0x0213 }
        r6 = java.lang.Integer.valueOf(r6);	 Catch:{ Exception -> 0x0213 }
        r26 = r4.getChat(r6);	 Catch:{ Exception -> 0x0213 }
        if (r26 == 0) goto L_0x0183;	 Catch:{ Exception -> 0x0213 }
    L_0x016d:
        r0 = r26;	 Catch:{ Exception -> 0x0213 }
        r4 = r0.megagroup;	 Catch:{ Exception -> 0x0213 }
        if (r4 == 0) goto L_0x072d;	 Catch:{ Exception -> 0x0213 }
    L_0x0173:
        r0 = r46;	 Catch:{ Exception -> 0x0213 }
        r4 = r0.flags;	 Catch:{ Exception -> 0x0213 }
        r6 = -NUM; // 0xffffffff80000000 float:-0.0 double:NaN;	 Catch:{ Exception -> 0x0213 }
        r4 = r4 | r6;	 Catch:{ Exception -> 0x0213 }
        r0 = r46;	 Catch:{ Exception -> 0x0213 }
        r0.flags = r4;	 Catch:{ Exception -> 0x0213 }
        r4 = 1;	 Catch:{ Exception -> 0x0213 }
        r0 = r46;	 Catch:{ Exception -> 0x0213 }
        r0.unread = r4;	 Catch:{ Exception -> 0x0213 }
    L_0x0183:
        r0 = r46;	 Catch:{ Exception -> 0x0213 }
        r4 = r0.flags;	 Catch:{ Exception -> 0x0213 }
        r4 = r4 | 512;	 Catch:{ Exception -> 0x0213 }
        r0 = r46;	 Catch:{ Exception -> 0x0213 }
        r0.flags = r4;	 Catch:{ Exception -> 0x0213 }
        r0 = r76;	 Catch:{ Exception -> 0x0213 }
        r2 = r46;	 Catch:{ Exception -> 0x0213 }
        r2.dialog_id = r0;	 Catch:{ Exception -> 0x0213 }
        if (r79 == 0) goto L_0x01bf;	 Catch:{ Exception -> 0x0213 }
    L_0x0195:
        if (r16 == 0) goto L_0x0751;	 Catch:{ Exception -> 0x0213 }
    L_0x0197:
        r0 = r79;	 Catch:{ Exception -> 0x0213 }
        r4 = r0.messageOwner;	 Catch:{ Exception -> 0x0213 }
        r8 = r4.random_id;	 Catch:{ Exception -> 0x0213 }
        r14 = 0;	 Catch:{ Exception -> 0x0213 }
        r4 = (r8 > r14 ? 1 : (r8 == r14 ? 0 : -1));	 Catch:{ Exception -> 0x0213 }
        if (r4 == 0) goto L_0x0751;	 Catch:{ Exception -> 0x0213 }
    L_0x01a3:
        r0 = r79;	 Catch:{ Exception -> 0x0213 }
        r4 = r0.messageOwner;	 Catch:{ Exception -> 0x0213 }
        r8 = r4.random_id;	 Catch:{ Exception -> 0x0213 }
        r0 = r46;	 Catch:{ Exception -> 0x0213 }
        r0.reply_to_random_id = r8;	 Catch:{ Exception -> 0x0213 }
        r0 = r46;	 Catch:{ Exception -> 0x0213 }
        r4 = r0.flags;	 Catch:{ Exception -> 0x0213 }
        r4 = r4 | 8;	 Catch:{ Exception -> 0x0213 }
        r0 = r46;	 Catch:{ Exception -> 0x0213 }
        r0.flags = r4;	 Catch:{ Exception -> 0x0213 }
    L_0x01b7:
        r4 = r79.getId();	 Catch:{ Exception -> 0x0213 }
        r0 = r46;	 Catch:{ Exception -> 0x0213 }
        r0.reply_to_msg_id = r4;	 Catch:{ Exception -> 0x0213 }
    L_0x01bf:
        if (r84 == 0) goto L_0x01d3;	 Catch:{ Exception -> 0x0213 }
    L_0x01c1:
        if (r16 != 0) goto L_0x01d3;	 Catch:{ Exception -> 0x0213 }
    L_0x01c3:
        r0 = r46;	 Catch:{ Exception -> 0x0213 }
        r4 = r0.flags;	 Catch:{ Exception -> 0x0213 }
        r4 = r4 | 64;	 Catch:{ Exception -> 0x0213 }
        r0 = r46;	 Catch:{ Exception -> 0x0213 }
        r0.flags = r4;	 Catch:{ Exception -> 0x0213 }
        r0 = r84;	 Catch:{ Exception -> 0x0213 }
        r1 = r46;	 Catch:{ Exception -> 0x0213 }
        r1.reply_markup = r0;	 Catch:{ Exception -> 0x0213 }
    L_0x01d3:
        if (r42 == 0) goto L_0x0959;	 Catch:{ Exception -> 0x0213 }
    L_0x01d5:
        r4 = 1;	 Catch:{ Exception -> 0x0213 }
        r0 = r35;	 Catch:{ Exception -> 0x0213 }
        if (r0 != r4) goto L_0x091b;	 Catch:{ Exception -> 0x0213 }
    L_0x01da:
        r0 = r67;	 Catch:{ Exception -> 0x0213 }
        r4 = r0.currentChatInfo;	 Catch:{ Exception -> 0x0213 }
        if (r4 != 0) goto L_0x075d;	 Catch:{ Exception -> 0x0213 }
    L_0x01e0:
        r0 = r67;	 Catch:{ Exception -> 0x0213 }
        r4 = r0.currentAccount;	 Catch:{ Exception -> 0x0213 }
        r4 = org.telegram.messenger.MessagesStorage.getInstance(r4);	 Catch:{ Exception -> 0x0213 }
        r0 = r46;	 Catch:{ Exception -> 0x0213 }
        r4.markMessageAsSendError(r0);	 Catch:{ Exception -> 0x0213 }
        r0 = r67;	 Catch:{ Exception -> 0x0213 }
        r4 = r0.currentAccount;	 Catch:{ Exception -> 0x0213 }
        r4 = org.telegram.messenger.NotificationCenter.getInstance(r4);	 Catch:{ Exception -> 0x0213 }
        r6 = org.telegram.messenger.NotificationCenter.messageSendError;	 Catch:{ Exception -> 0x0213 }
        r8 = 1;	 Catch:{ Exception -> 0x0213 }
        r8 = new java.lang.Object[r8];	 Catch:{ Exception -> 0x0213 }
        r9 = 0;	 Catch:{ Exception -> 0x0213 }
        r0 = r46;	 Catch:{ Exception -> 0x0213 }
        r11 = r0.id;	 Catch:{ Exception -> 0x0213 }
        r11 = java.lang.Integer.valueOf(r11);	 Catch:{ Exception -> 0x0213 }
        r8[r9] = r11;	 Catch:{ Exception -> 0x0213 }
        r4.postNotificationName(r6, r8);	 Catch:{ Exception -> 0x0213 }
        r0 = r46;	 Catch:{ Exception -> 0x0213 }
        r4 = r0.id;	 Catch:{ Exception -> 0x0213 }
        r0 = r67;	 Catch:{ Exception -> 0x0213 }
        r0.processSentMessage(r4);	 Catch:{ Exception -> 0x0213 }
        goto L_0x0006;
    L_0x0213:
        r31 = move-exception;
        r12 = r48;
    L_0x0216:
        org.telegram.messenger.FileLog.m3e(r31);
        r0 = r67;
        r4 = r0.currentAccount;
        r4 = org.telegram.messenger.MessagesStorage.getInstance(r4);
        r0 = r46;
        r4.markMessageAsSendError(r0);
        if (r12 == 0) goto L_0x022d;
    L_0x0228:
        r4 = r12.messageOwner;
        r6 = 2;
        r4.send_state = r6;
    L_0x022d:
        r0 = r67;
        r4 = r0.currentAccount;
        r4 = org.telegram.messenger.NotificationCenter.getInstance(r4);
        r6 = org.telegram.messenger.NotificationCenter.messageSendError;
        r8 = 1;
        r8 = new java.lang.Object[r8];
        r9 = 0;
        r0 = r46;
        r11 = r0.id;
        r11 = java.lang.Integer.valueOf(r11);
        r8[r9] = r11;
        r4.postNotificationName(r6, r8);
        r0 = r46;
        r4 = r0.id;
        r0 = r67;
        r0.processSentMessage(r4);
        goto L_0x0006;
    L_0x0253:
        r38 = 0;
        goto L_0x00cb;
    L_0x0257:
        r0 = r82;	 Catch:{ Exception -> 0x0213 }
        r4 = r0.type;	 Catch:{ Exception -> 0x0213 }
        if (r4 != 0) goto L_0x0281;	 Catch:{ Exception -> 0x0213 }
    L_0x025d:
        r0 = r82;	 Catch:{ Exception -> 0x0213 }
        r4 = r0.messageOwner;	 Catch:{ Exception -> 0x0213 }
        r4 = r4.media;	 Catch:{ Exception -> 0x0213 }
        r4 = r4 instanceof org.telegram.tgnet.TLRPC.TL_messageMediaGame;	 Catch:{ Exception -> 0x0213 }
        if (r4 == 0) goto L_0x027a;	 Catch:{ Exception -> 0x0213 }
    L_0x0267:
        r63 = 0;	 Catch:{ Exception -> 0x0213 }
    L_0x0269:
        if (r85 == 0) goto L_0x00db;	 Catch:{ Exception -> 0x0213 }
    L_0x026b:
        r4 = "query_id";	 Catch:{ Exception -> 0x0213 }
        r0 = r85;	 Catch:{ Exception -> 0x0213 }
        r4 = r0.containsKey(r4);	 Catch:{ Exception -> 0x0213 }
        if (r4 == 0) goto L_0x00db;	 Catch:{ Exception -> 0x0213 }
    L_0x0276:
        r63 = 9;	 Catch:{ Exception -> 0x0213 }
        goto L_0x00db;	 Catch:{ Exception -> 0x0213 }
    L_0x027a:
        r0 = r46;	 Catch:{ Exception -> 0x0213 }
        r0 = r0.message;	 Catch:{ Exception -> 0x0213 }
        r68 = r0;	 Catch:{ Exception -> 0x0213 }
        goto L_0x0267;	 Catch:{ Exception -> 0x0213 }
    L_0x0281:
        r0 = r82;	 Catch:{ Exception -> 0x0213 }
        r4 = r0.type;	 Catch:{ Exception -> 0x0213 }
        r6 = 4;	 Catch:{ Exception -> 0x0213 }
        if (r4 != r6) goto L_0x0291;	 Catch:{ Exception -> 0x0213 }
    L_0x0288:
        r0 = r46;	 Catch:{ Exception -> 0x0213 }
        r0 = r0.media;	 Catch:{ Exception -> 0x0213 }
        r70 = r0;	 Catch:{ Exception -> 0x0213 }
        r63 = 1;	 Catch:{ Exception -> 0x0213 }
        goto L_0x0269;	 Catch:{ Exception -> 0x0213 }
    L_0x0291:
        r0 = r82;	 Catch:{ Exception -> 0x0213 }
        r4 = r0.type;	 Catch:{ Exception -> 0x0213 }
        r6 = 1;	 Catch:{ Exception -> 0x0213 }
        if (r4 != r6) goto L_0x02a6;	 Catch:{ Exception -> 0x0213 }
    L_0x0298:
        r0 = r46;	 Catch:{ Exception -> 0x0213 }
        r4 = r0.media;	 Catch:{ Exception -> 0x0213 }
        r4 = r4.photo;	 Catch:{ Exception -> 0x0213 }
        r0 = r4;	 Catch:{ Exception -> 0x0213 }
        r0 = (org.telegram.tgnet.TLRPC.TL_photo) r0;	 Catch:{ Exception -> 0x0213 }
        r71 = r0;	 Catch:{ Exception -> 0x0213 }
        r63 = 2;	 Catch:{ Exception -> 0x0213 }
        goto L_0x0269;	 Catch:{ Exception -> 0x0213 }
    L_0x02a6:
        r0 = r82;	 Catch:{ Exception -> 0x0213 }
        r4 = r0.type;	 Catch:{ Exception -> 0x0213 }
        r6 = 3;	 Catch:{ Exception -> 0x0213 }
        if (r4 == r6) goto L_0x02b6;	 Catch:{ Exception -> 0x0213 }
    L_0x02ad:
        r0 = r82;	 Catch:{ Exception -> 0x0213 }
        r4 = r0.type;	 Catch:{ Exception -> 0x0213 }
        r6 = 5;	 Catch:{ Exception -> 0x0213 }
        if (r4 == r6) goto L_0x02b6;	 Catch:{ Exception -> 0x0213 }
    L_0x02b4:
        if (r72 == 0) goto L_0x02c4;	 Catch:{ Exception -> 0x0213 }
    L_0x02b6:
        r63 = 3;	 Catch:{ Exception -> 0x0213 }
        r0 = r46;	 Catch:{ Exception -> 0x0213 }
        r4 = r0.media;	 Catch:{ Exception -> 0x0213 }
        r4 = r4.document;	 Catch:{ Exception -> 0x0213 }
        r0 = r4;	 Catch:{ Exception -> 0x0213 }
        r0 = (org.telegram.tgnet.TLRPC.TL_document) r0;	 Catch:{ Exception -> 0x0213 }
        r74 = r0;	 Catch:{ Exception -> 0x0213 }
        goto L_0x0269;	 Catch:{ Exception -> 0x0213 }
    L_0x02c4:
        r0 = r82;	 Catch:{ Exception -> 0x0213 }
        r4 = r0.type;	 Catch:{ Exception -> 0x0213 }
        r6 = 12;	 Catch:{ Exception -> 0x0213 }
        if (r4 != r6) goto L_0x02ff;	 Catch:{ Exception -> 0x0213 }
    L_0x02cc:
        r64 = new org.telegram.tgnet.TLRPC$TL_userRequest_old2;	 Catch:{ Exception -> 0x0213 }
        r64.<init>();	 Catch:{ Exception -> 0x0213 }
        r0 = r46;	 Catch:{ Exception -> 0x1a1f }
        r4 = r0.media;	 Catch:{ Exception -> 0x1a1f }
        r4 = r4.phone_number;	 Catch:{ Exception -> 0x1a1f }
        r0 = r64;	 Catch:{ Exception -> 0x1a1f }
        r0.phone = r4;	 Catch:{ Exception -> 0x1a1f }
        r0 = r46;	 Catch:{ Exception -> 0x1a1f }
        r4 = r0.media;	 Catch:{ Exception -> 0x1a1f }
        r4 = r4.first_name;	 Catch:{ Exception -> 0x1a1f }
        r0 = r64;	 Catch:{ Exception -> 0x1a1f }
        r0.first_name = r4;	 Catch:{ Exception -> 0x1a1f }
        r0 = r46;	 Catch:{ Exception -> 0x1a1f }
        r4 = r0.media;	 Catch:{ Exception -> 0x1a1f }
        r4 = r4.last_name;	 Catch:{ Exception -> 0x1a1f }
        r0 = r64;	 Catch:{ Exception -> 0x1a1f }
        r0.last_name = r4;	 Catch:{ Exception -> 0x1a1f }
        r0 = r46;	 Catch:{ Exception -> 0x1a1f }
        r4 = r0.media;	 Catch:{ Exception -> 0x1a1f }
        r4 = r4.user_id;	 Catch:{ Exception -> 0x1a1f }
        r0 = r64;	 Catch:{ Exception -> 0x1a1f }
        r0.id = r4;	 Catch:{ Exception -> 0x1a1f }
        r63 = 6;
        r73 = r64;
        goto L_0x0269;
    L_0x02ff:
        r0 = r82;	 Catch:{ Exception -> 0x0213 }
        r4 = r0.type;	 Catch:{ Exception -> 0x0213 }
        r6 = 8;	 Catch:{ Exception -> 0x0213 }
        if (r4 == r6) goto L_0x031f;	 Catch:{ Exception -> 0x0213 }
    L_0x0307:
        r0 = r82;	 Catch:{ Exception -> 0x0213 }
        r4 = r0.type;	 Catch:{ Exception -> 0x0213 }
        r6 = 9;	 Catch:{ Exception -> 0x0213 }
        if (r4 == r6) goto L_0x031f;	 Catch:{ Exception -> 0x0213 }
    L_0x030f:
        r0 = r82;	 Catch:{ Exception -> 0x0213 }
        r4 = r0.type;	 Catch:{ Exception -> 0x0213 }
        r6 = 13;	 Catch:{ Exception -> 0x0213 }
        if (r4 == r6) goto L_0x031f;	 Catch:{ Exception -> 0x0213 }
    L_0x0317:
        r0 = r82;	 Catch:{ Exception -> 0x0213 }
        r4 = r0.type;	 Catch:{ Exception -> 0x0213 }
        r6 = 14;	 Catch:{ Exception -> 0x0213 }
        if (r4 != r6) goto L_0x032e;	 Catch:{ Exception -> 0x0213 }
    L_0x031f:
        r0 = r46;	 Catch:{ Exception -> 0x0213 }
        r4 = r0.media;	 Catch:{ Exception -> 0x0213 }
        r4 = r4.document;	 Catch:{ Exception -> 0x0213 }
        r0 = r4;	 Catch:{ Exception -> 0x0213 }
        r0 = (org.telegram.tgnet.TLRPC.TL_document) r0;	 Catch:{ Exception -> 0x0213 }
        r74 = r0;	 Catch:{ Exception -> 0x0213 }
        r63 = 7;	 Catch:{ Exception -> 0x0213 }
        goto L_0x0269;	 Catch:{ Exception -> 0x0213 }
    L_0x032e:
        r0 = r82;	 Catch:{ Exception -> 0x0213 }
        r4 = r0.type;	 Catch:{ Exception -> 0x0213 }
        r6 = 2;	 Catch:{ Exception -> 0x0213 }
        if (r4 != r6) goto L_0x0269;	 Catch:{ Exception -> 0x0213 }
    L_0x0335:
        r0 = r46;	 Catch:{ Exception -> 0x0213 }
        r4 = r0.media;	 Catch:{ Exception -> 0x0213 }
        r4 = r4.document;	 Catch:{ Exception -> 0x0213 }
        r0 = r4;	 Catch:{ Exception -> 0x0213 }
        r0 = (org.telegram.tgnet.TLRPC.TL_document) r0;	 Catch:{ Exception -> 0x0213 }
        r74 = r0;	 Catch:{ Exception -> 0x0213 }
        r63 = 8;	 Catch:{ Exception -> 0x0213 }
        goto L_0x0269;	 Catch:{ Exception -> 0x0213 }
    L_0x0344:
        if (r68 == 0) goto L_0x0411;	 Catch:{ Exception -> 0x0213 }
    L_0x0346:
        if (r16 == 0) goto L_0x03ed;	 Catch:{ Exception -> 0x0213 }
    L_0x0348:
        r47 = new org.telegram.tgnet.TLRPC$TL_message_secret;	 Catch:{ Exception -> 0x0213 }
        r47.<init>();	 Catch:{ Exception -> 0x0213 }
        r46 = r47;	 Catch:{ Exception -> 0x0213 }
    L_0x034f:
        if (r16 == 0) goto L_0x036c;	 Catch:{ Exception -> 0x0213 }
    L_0x0351:
        r0 = r80;	 Catch:{ Exception -> 0x0213 }
        r4 = r0 instanceof org.telegram.tgnet.TLRPC.TL_webPagePending;	 Catch:{ Exception -> 0x0213 }
        if (r4 == 0) goto L_0x036c;	 Catch:{ Exception -> 0x0213 }
    L_0x0357:
        r0 = r80;	 Catch:{ Exception -> 0x0213 }
        r4 = r0.url;	 Catch:{ Exception -> 0x0213 }
        if (r4 == 0) goto L_0x03f6;	 Catch:{ Exception -> 0x0213 }
    L_0x035d:
        r49 = new org.telegram.tgnet.TLRPC$TL_webPageUrlPending;	 Catch:{ Exception -> 0x0213 }
        r49.<init>();	 Catch:{ Exception -> 0x0213 }
        r0 = r80;	 Catch:{ Exception -> 0x0213 }
        r4 = r0.url;	 Catch:{ Exception -> 0x0213 }
        r0 = r49;	 Catch:{ Exception -> 0x0213 }
        r0.url = r4;	 Catch:{ Exception -> 0x0213 }
        r80 = r49;	 Catch:{ Exception -> 0x0213 }
    L_0x036c:
        if (r80 != 0) goto L_0x03fa;	 Catch:{ Exception -> 0x0213 }
    L_0x036e:
        r4 = new org.telegram.tgnet.TLRPC$TL_messageMediaEmpty;	 Catch:{ Exception -> 0x0213 }
        r4.<init>();	 Catch:{ Exception -> 0x0213 }
        r0 = r46;	 Catch:{ Exception -> 0x0213 }
        r0.media = r4;	 Catch:{ Exception -> 0x0213 }
    L_0x0377:
        if (r85 == 0) goto L_0x040d;	 Catch:{ Exception -> 0x0213 }
    L_0x0379:
        r4 = "query_id";	 Catch:{ Exception -> 0x0213 }
        r0 = r85;	 Catch:{ Exception -> 0x0213 }
        r4 = r0.containsKey(r4);	 Catch:{ Exception -> 0x0213 }
        if (r4 == 0) goto L_0x040d;	 Catch:{ Exception -> 0x0213 }
    L_0x0384:
        r63 = 9;	 Catch:{ Exception -> 0x0213 }
    L_0x0386:
        r0 = r68;	 Catch:{ Exception -> 0x0213 }
        r1 = r46;	 Catch:{ Exception -> 0x0213 }
        r1.message = r0;	 Catch:{ Exception -> 0x0213 }
    L_0x038c:
        if (r83 == 0) goto L_0x03a4;	 Catch:{ Exception -> 0x0213 }
    L_0x038e:
        r4 = r83.isEmpty();	 Catch:{ Exception -> 0x0213 }
        if (r4 != 0) goto L_0x03a4;	 Catch:{ Exception -> 0x0213 }
    L_0x0394:
        r0 = r83;	 Catch:{ Exception -> 0x0213 }
        r1 = r46;	 Catch:{ Exception -> 0x0213 }
        r1.entities = r0;	 Catch:{ Exception -> 0x0213 }
        r0 = r46;	 Catch:{ Exception -> 0x0213 }
        r4 = r0.flags;	 Catch:{ Exception -> 0x0213 }
        r4 = r4 | 128;	 Catch:{ Exception -> 0x0213 }
        r0 = r46;	 Catch:{ Exception -> 0x0213 }
        r0.flags = r4;	 Catch:{ Exception -> 0x0213 }
    L_0x03a4:
        if (r69 == 0) goto L_0x06e9;	 Catch:{ Exception -> 0x0213 }
    L_0x03a6:
        r0 = r69;	 Catch:{ Exception -> 0x0213 }
        r1 = r46;	 Catch:{ Exception -> 0x0213 }
        r1.message = r0;	 Catch:{ Exception -> 0x0213 }
    L_0x03ac:
        r0 = r46;	 Catch:{ Exception -> 0x0213 }
        r4 = r0.attachPath;	 Catch:{ Exception -> 0x0213 }
        if (r4 != 0) goto L_0x03b9;	 Catch:{ Exception -> 0x0213 }
    L_0x03b2:
        r4 = "";	 Catch:{ Exception -> 0x0213 }
        r0 = r46;	 Catch:{ Exception -> 0x0213 }
        r0.attachPath = r4;	 Catch:{ Exception -> 0x0213 }
    L_0x03b9:
        r0 = r67;	 Catch:{ Exception -> 0x0213 }
        r4 = r0.currentAccount;	 Catch:{ Exception -> 0x0213 }
        r4 = org.telegram.messenger.UserConfig.getInstance(r4);	 Catch:{ Exception -> 0x0213 }
        r4 = r4.getNewMessageId();	 Catch:{ Exception -> 0x0213 }
        r0 = r46;	 Catch:{ Exception -> 0x0213 }
        r0.id = r4;	 Catch:{ Exception -> 0x0213 }
        r0 = r46;	 Catch:{ Exception -> 0x0213 }
        r0.local_id = r4;	 Catch:{ Exception -> 0x0213 }
        r4 = 1;	 Catch:{ Exception -> 0x0213 }
        r0 = r46;	 Catch:{ Exception -> 0x0213 }
        r0.out = r4;	 Catch:{ Exception -> 0x0213 }
        if (r38 == 0) goto L_0x06f8;	 Catch:{ Exception -> 0x0213 }
    L_0x03d4:
        if (r57 == 0) goto L_0x06f8;	 Catch:{ Exception -> 0x0213 }
    L_0x03d6:
        r0 = r57;	 Catch:{ Exception -> 0x0213 }
        r4 = r0.channel_id;	 Catch:{ Exception -> 0x0213 }
        r4 = -r4;	 Catch:{ Exception -> 0x0213 }
        r0 = r46;	 Catch:{ Exception -> 0x0213 }
        r0.from_id = r4;	 Catch:{ Exception -> 0x0213 }
    L_0x03df:
        r0 = r67;	 Catch:{ Exception -> 0x0213 }
        r4 = r0.currentAccount;	 Catch:{ Exception -> 0x0213 }
        r4 = org.telegram.messenger.UserConfig.getInstance(r4);	 Catch:{ Exception -> 0x0213 }
        r6 = 0;	 Catch:{ Exception -> 0x0213 }
        r4.saveConfig(r6);	 Catch:{ Exception -> 0x0213 }
        goto L_0x00db;	 Catch:{ Exception -> 0x0213 }
    L_0x03ed:
        r47 = new org.telegram.tgnet.TLRPC$TL_message;	 Catch:{ Exception -> 0x0213 }
        r47.<init>();	 Catch:{ Exception -> 0x0213 }
        r46 = r47;	 Catch:{ Exception -> 0x0213 }
        goto L_0x034f;	 Catch:{ Exception -> 0x0213 }
    L_0x03f6:
        r80 = 0;	 Catch:{ Exception -> 0x0213 }
        goto L_0x036c;	 Catch:{ Exception -> 0x0213 }
    L_0x03fa:
        r4 = new org.telegram.tgnet.TLRPC$TL_messageMediaWebPage;	 Catch:{ Exception -> 0x0213 }
        r4.<init>();	 Catch:{ Exception -> 0x0213 }
        r0 = r46;	 Catch:{ Exception -> 0x0213 }
        r0.media = r4;	 Catch:{ Exception -> 0x0213 }
        r0 = r46;	 Catch:{ Exception -> 0x0213 }
        r4 = r0.media;	 Catch:{ Exception -> 0x0213 }
        r0 = r80;	 Catch:{ Exception -> 0x0213 }
        r4.webpage = r0;	 Catch:{ Exception -> 0x0213 }
        goto L_0x0377;	 Catch:{ Exception -> 0x0213 }
    L_0x040d:
        r63 = 0;	 Catch:{ Exception -> 0x0213 }
        goto L_0x0386;	 Catch:{ Exception -> 0x0213 }
    L_0x0411:
        if (r70 == 0) goto L_0x043f;	 Catch:{ Exception -> 0x0213 }
    L_0x0413:
        if (r16 == 0) goto L_0x0433;	 Catch:{ Exception -> 0x0213 }
    L_0x0415:
        r47 = new org.telegram.tgnet.TLRPC$TL_message_secret;	 Catch:{ Exception -> 0x0213 }
        r47.<init>();	 Catch:{ Exception -> 0x0213 }
        r46 = r47;	 Catch:{ Exception -> 0x0213 }
    L_0x041c:
        r0 = r70;	 Catch:{ Exception -> 0x0213 }
        r1 = r46;	 Catch:{ Exception -> 0x0213 }
        r1.media = r0;	 Catch:{ Exception -> 0x0213 }
        if (r85 == 0) goto L_0x043b;	 Catch:{ Exception -> 0x0213 }
    L_0x0424:
        r4 = "query_id";	 Catch:{ Exception -> 0x0213 }
        r0 = r85;	 Catch:{ Exception -> 0x0213 }
        r4 = r0.containsKey(r4);	 Catch:{ Exception -> 0x0213 }
        if (r4 == 0) goto L_0x043b;	 Catch:{ Exception -> 0x0213 }
    L_0x042f:
        r63 = 9;	 Catch:{ Exception -> 0x0213 }
        goto L_0x038c;	 Catch:{ Exception -> 0x0213 }
    L_0x0433:
        r47 = new org.telegram.tgnet.TLRPC$TL_message;	 Catch:{ Exception -> 0x0213 }
        r47.<init>();	 Catch:{ Exception -> 0x0213 }
        r46 = r47;	 Catch:{ Exception -> 0x0213 }
        goto L_0x041c;	 Catch:{ Exception -> 0x0213 }
    L_0x043b:
        r63 = 1;	 Catch:{ Exception -> 0x0213 }
        goto L_0x038c;	 Catch:{ Exception -> 0x0213 }
    L_0x043f:
        if (r71 == 0) goto L_0x04e5;	 Catch:{ Exception -> 0x0213 }
    L_0x0441:
        if (r16 == 0) goto L_0x04b1;	 Catch:{ Exception -> 0x0213 }
    L_0x0443:
        r47 = new org.telegram.tgnet.TLRPC$TL_message_secret;	 Catch:{ Exception -> 0x0213 }
        r47.<init>();	 Catch:{ Exception -> 0x0213 }
        r46 = r47;	 Catch:{ Exception -> 0x0213 }
    L_0x044a:
        r4 = new org.telegram.tgnet.TLRPC$TL_messageMediaPhoto;	 Catch:{ Exception -> 0x0213 }
        r4.<init>();	 Catch:{ Exception -> 0x0213 }
        r0 = r46;	 Catch:{ Exception -> 0x0213 }
        r0.media = r4;	 Catch:{ Exception -> 0x0213 }
        r0 = r46;	 Catch:{ Exception -> 0x0213 }
        r4 = r0.media;	 Catch:{ Exception -> 0x0213 }
        r6 = r4.flags;	 Catch:{ Exception -> 0x0213 }
        r6 = r6 | 3;	 Catch:{ Exception -> 0x0213 }
        r4.flags = r6;	 Catch:{ Exception -> 0x0213 }
        if (r83 == 0) goto L_0x0465;	 Catch:{ Exception -> 0x0213 }
    L_0x045f:
        r0 = r83;	 Catch:{ Exception -> 0x0213 }
        r1 = r46;	 Catch:{ Exception -> 0x0213 }
        r1.entities = r0;	 Catch:{ Exception -> 0x0213 }
    L_0x0465:
        if (r86 == 0) goto L_0x047f;	 Catch:{ Exception -> 0x0213 }
    L_0x0467:
        r0 = r46;	 Catch:{ Exception -> 0x0213 }
        r4 = r0.media;	 Catch:{ Exception -> 0x0213 }
        r0 = r86;	 Catch:{ Exception -> 0x0213 }
        r4.ttl_seconds = r0;	 Catch:{ Exception -> 0x0213 }
        r0 = r86;	 Catch:{ Exception -> 0x0213 }
        r1 = r46;	 Catch:{ Exception -> 0x0213 }
        r1.ttl = r0;	 Catch:{ Exception -> 0x0213 }
        r0 = r46;	 Catch:{ Exception -> 0x0213 }
        r4 = r0.media;	 Catch:{ Exception -> 0x0213 }
        r6 = r4.flags;	 Catch:{ Exception -> 0x0213 }
        r6 = r6 | 4;	 Catch:{ Exception -> 0x0213 }
        r4.flags = r6;	 Catch:{ Exception -> 0x0213 }
    L_0x047f:
        r0 = r46;	 Catch:{ Exception -> 0x0213 }
        r4 = r0.media;	 Catch:{ Exception -> 0x0213 }
        r0 = r71;	 Catch:{ Exception -> 0x0213 }
        r4.photo = r0;	 Catch:{ Exception -> 0x0213 }
        if (r85 == 0) goto L_0x04b9;	 Catch:{ Exception -> 0x0213 }
    L_0x0489:
        r4 = "query_id";	 Catch:{ Exception -> 0x0213 }
        r0 = r85;	 Catch:{ Exception -> 0x0213 }
        r4 = r0.containsKey(r4);	 Catch:{ Exception -> 0x0213 }
        if (r4 == 0) goto L_0x04b9;	 Catch:{ Exception -> 0x0213 }
    L_0x0494:
        r63 = 9;	 Catch:{ Exception -> 0x0213 }
    L_0x0496:
        if (r78 == 0) goto L_0x04bc;	 Catch:{ Exception -> 0x0213 }
    L_0x0498:
        r4 = r78.length();	 Catch:{ Exception -> 0x0213 }
        if (r4 <= 0) goto L_0x04bc;	 Catch:{ Exception -> 0x0213 }
    L_0x049e:
        r4 = "http";	 Catch:{ Exception -> 0x0213 }
        r0 = r78;	 Catch:{ Exception -> 0x0213 }
        r4 = r0.startsWith(r4);	 Catch:{ Exception -> 0x0213 }
        if (r4 == 0) goto L_0x04bc;	 Catch:{ Exception -> 0x0213 }
    L_0x04a9:
        r0 = r78;	 Catch:{ Exception -> 0x0213 }
        r1 = r46;	 Catch:{ Exception -> 0x0213 }
        r1.attachPath = r0;	 Catch:{ Exception -> 0x0213 }
        goto L_0x038c;	 Catch:{ Exception -> 0x0213 }
    L_0x04b1:
        r47 = new org.telegram.tgnet.TLRPC$TL_message;	 Catch:{ Exception -> 0x0213 }
        r47.<init>();	 Catch:{ Exception -> 0x0213 }
        r46 = r47;	 Catch:{ Exception -> 0x0213 }
        goto L_0x044a;	 Catch:{ Exception -> 0x0213 }
    L_0x04b9:
        r63 = 2;	 Catch:{ Exception -> 0x0213 }
        goto L_0x0496;	 Catch:{ Exception -> 0x0213 }
    L_0x04bc:
        r0 = r71;	 Catch:{ Exception -> 0x0213 }
        r4 = r0.sizes;	 Catch:{ Exception -> 0x0213 }
        r0 = r71;	 Catch:{ Exception -> 0x0213 }
        r6 = r0.sizes;	 Catch:{ Exception -> 0x0213 }
        r6 = r6.size();	 Catch:{ Exception -> 0x0213 }
        r6 = r6 + -1;	 Catch:{ Exception -> 0x0213 }
        r4 = r4.get(r6);	 Catch:{ Exception -> 0x0213 }
        r4 = (org.telegram.tgnet.TLRPC.PhotoSize) r4;	 Catch:{ Exception -> 0x0213 }
        r0 = r4.location;	 Catch:{ Exception -> 0x0213 }
        r41 = r0;	 Catch:{ Exception -> 0x0213 }
        r4 = 1;	 Catch:{ Exception -> 0x0213 }
        r0 = r41;	 Catch:{ Exception -> 0x0213 }
        r4 = org.telegram.messenger.FileLoader.getPathToAttach(r0, r4);	 Catch:{ Exception -> 0x0213 }
        r4 = r4.toString();	 Catch:{ Exception -> 0x0213 }
        r0 = r46;	 Catch:{ Exception -> 0x0213 }
        r0.attachPath = r4;	 Catch:{ Exception -> 0x0213 }
        goto L_0x038c;	 Catch:{ Exception -> 0x0213 }
    L_0x04e5:
        if (r75 == 0) goto L_0x0510;	 Catch:{ Exception -> 0x0213 }
    L_0x04e7:
        r47 = new org.telegram.tgnet.TLRPC$TL_message;	 Catch:{ Exception -> 0x0213 }
        r47.<init>();	 Catch:{ Exception -> 0x0213 }
        r4 = new org.telegram.tgnet.TLRPC$TL_messageMediaGame;	 Catch:{ Exception -> 0x1a26 }
        r4.<init>();	 Catch:{ Exception -> 0x1a26 }
        r0 = r47;	 Catch:{ Exception -> 0x1a26 }
        r0.media = r4;	 Catch:{ Exception -> 0x1a26 }
        r0 = r47;	 Catch:{ Exception -> 0x1a26 }
        r4 = r0.media;	 Catch:{ Exception -> 0x1a26 }
        r0 = r75;	 Catch:{ Exception -> 0x1a26 }
        r4.game = r0;	 Catch:{ Exception -> 0x1a26 }
        if (r85 == 0) goto L_0x1a49;	 Catch:{ Exception -> 0x1a26 }
    L_0x04ff:
        r4 = "query_id";	 Catch:{ Exception -> 0x1a26 }
        r0 = r85;	 Catch:{ Exception -> 0x1a26 }
        r4 = r0.containsKey(r4);	 Catch:{ Exception -> 0x1a26 }
        if (r4 == 0) goto L_0x1a49;
    L_0x050a:
        r63 = 9;
        r46 = r47;
        goto L_0x038c;
    L_0x0510:
        if (r73 == 0) goto L_0x0593;
    L_0x0512:
        if (r16 == 0) goto L_0x0587;
    L_0x0514:
        r47 = new org.telegram.tgnet.TLRPC$TL_message_secret;	 Catch:{ Exception -> 0x0213 }
        r47.<init>();	 Catch:{ Exception -> 0x0213 }
        r46 = r47;	 Catch:{ Exception -> 0x0213 }
    L_0x051b:
        r4 = new org.telegram.tgnet.TLRPC$TL_messageMediaContact;	 Catch:{ Exception -> 0x0213 }
        r4.<init>();	 Catch:{ Exception -> 0x0213 }
        r0 = r46;	 Catch:{ Exception -> 0x0213 }
        r0.media = r4;	 Catch:{ Exception -> 0x0213 }
        r0 = r46;	 Catch:{ Exception -> 0x0213 }
        r4 = r0.media;	 Catch:{ Exception -> 0x0213 }
        r0 = r73;	 Catch:{ Exception -> 0x0213 }
        r6 = r0.phone;	 Catch:{ Exception -> 0x0213 }
        r4.phone_number = r6;	 Catch:{ Exception -> 0x0213 }
        r0 = r46;	 Catch:{ Exception -> 0x0213 }
        r4 = r0.media;	 Catch:{ Exception -> 0x0213 }
        r0 = r73;	 Catch:{ Exception -> 0x0213 }
        r6 = r0.first_name;	 Catch:{ Exception -> 0x0213 }
        r4.first_name = r6;	 Catch:{ Exception -> 0x0213 }
        r0 = r46;	 Catch:{ Exception -> 0x0213 }
        r4 = r0.media;	 Catch:{ Exception -> 0x0213 }
        r0 = r73;	 Catch:{ Exception -> 0x0213 }
        r6 = r0.last_name;	 Catch:{ Exception -> 0x0213 }
        r4.last_name = r6;	 Catch:{ Exception -> 0x0213 }
        r0 = r46;	 Catch:{ Exception -> 0x0213 }
        r4 = r0.media;	 Catch:{ Exception -> 0x0213 }
        r0 = r73;	 Catch:{ Exception -> 0x0213 }
        r6 = r0.id;	 Catch:{ Exception -> 0x0213 }
        r4.user_id = r6;	 Catch:{ Exception -> 0x0213 }
        r0 = r46;	 Catch:{ Exception -> 0x0213 }
        r4 = r0.media;	 Catch:{ Exception -> 0x0213 }
        r4 = r4.first_name;	 Catch:{ Exception -> 0x0213 }
        if (r4 != 0) goto L_0x0561;	 Catch:{ Exception -> 0x0213 }
    L_0x0554:
        r0 = r46;	 Catch:{ Exception -> 0x0213 }
        r4 = r0.media;	 Catch:{ Exception -> 0x0213 }
        r6 = "";	 Catch:{ Exception -> 0x0213 }
        r4.first_name = r6;	 Catch:{ Exception -> 0x0213 }
        r0 = r73;	 Catch:{ Exception -> 0x0213 }
        r0.first_name = r6;	 Catch:{ Exception -> 0x0213 }
    L_0x0561:
        r0 = r46;	 Catch:{ Exception -> 0x0213 }
        r4 = r0.media;	 Catch:{ Exception -> 0x0213 }
        r4 = r4.last_name;	 Catch:{ Exception -> 0x0213 }
        if (r4 != 0) goto L_0x0576;	 Catch:{ Exception -> 0x0213 }
    L_0x0569:
        r0 = r46;	 Catch:{ Exception -> 0x0213 }
        r4 = r0.media;	 Catch:{ Exception -> 0x0213 }
        r6 = "";	 Catch:{ Exception -> 0x0213 }
        r4.last_name = r6;	 Catch:{ Exception -> 0x0213 }
        r0 = r73;	 Catch:{ Exception -> 0x0213 }
        r0.last_name = r6;	 Catch:{ Exception -> 0x0213 }
    L_0x0576:
        if (r85 == 0) goto L_0x058f;	 Catch:{ Exception -> 0x0213 }
    L_0x0578:
        r4 = "query_id";	 Catch:{ Exception -> 0x0213 }
        r0 = r85;	 Catch:{ Exception -> 0x0213 }
        r4 = r0.containsKey(r4);	 Catch:{ Exception -> 0x0213 }
        if (r4 == 0) goto L_0x058f;	 Catch:{ Exception -> 0x0213 }
    L_0x0583:
        r63 = 9;	 Catch:{ Exception -> 0x0213 }
        goto L_0x038c;	 Catch:{ Exception -> 0x0213 }
    L_0x0587:
        r47 = new org.telegram.tgnet.TLRPC$TL_message;	 Catch:{ Exception -> 0x0213 }
        r47.<init>();	 Catch:{ Exception -> 0x0213 }
        r46 = r47;	 Catch:{ Exception -> 0x0213 }
        goto L_0x051b;	 Catch:{ Exception -> 0x0213 }
    L_0x058f:
        r63 = 6;	 Catch:{ Exception -> 0x0213 }
        goto L_0x038c;	 Catch:{ Exception -> 0x0213 }
    L_0x0593:
        if (r74 == 0) goto L_0x038c;	 Catch:{ Exception -> 0x0213 }
    L_0x0595:
        if (r16 == 0) goto L_0x068b;	 Catch:{ Exception -> 0x0213 }
    L_0x0597:
        r47 = new org.telegram.tgnet.TLRPC$TL_message_secret;	 Catch:{ Exception -> 0x0213 }
        r47.<init>();	 Catch:{ Exception -> 0x0213 }
        r46 = r47;	 Catch:{ Exception -> 0x0213 }
    L_0x059e:
        r4 = new org.telegram.tgnet.TLRPC$TL_messageMediaDocument;	 Catch:{ Exception -> 0x0213 }
        r4.<init>();	 Catch:{ Exception -> 0x0213 }
        r0 = r46;	 Catch:{ Exception -> 0x0213 }
        r0.media = r4;	 Catch:{ Exception -> 0x0213 }
        r0 = r46;	 Catch:{ Exception -> 0x0213 }
        r4 = r0.media;	 Catch:{ Exception -> 0x0213 }
        r6 = r4.flags;	 Catch:{ Exception -> 0x0213 }
        r6 = r6 | 3;	 Catch:{ Exception -> 0x0213 }
        r4.flags = r6;	 Catch:{ Exception -> 0x0213 }
        if (r86 == 0) goto L_0x05cb;	 Catch:{ Exception -> 0x0213 }
    L_0x05b3:
        r0 = r46;	 Catch:{ Exception -> 0x0213 }
        r4 = r0.media;	 Catch:{ Exception -> 0x0213 }
        r0 = r86;	 Catch:{ Exception -> 0x0213 }
        r4.ttl_seconds = r0;	 Catch:{ Exception -> 0x0213 }
        r0 = r86;	 Catch:{ Exception -> 0x0213 }
        r1 = r46;	 Catch:{ Exception -> 0x0213 }
        r1.ttl = r0;	 Catch:{ Exception -> 0x0213 }
        r0 = r46;	 Catch:{ Exception -> 0x0213 }
        r4 = r0.media;	 Catch:{ Exception -> 0x0213 }
        r6 = r4.flags;	 Catch:{ Exception -> 0x0213 }
        r6 = r6 | 4;	 Catch:{ Exception -> 0x0213 }
        r4.flags = r6;	 Catch:{ Exception -> 0x0213 }
    L_0x05cb:
        r0 = r46;	 Catch:{ Exception -> 0x0213 }
        r4 = r0.media;	 Catch:{ Exception -> 0x0213 }
        r0 = r74;	 Catch:{ Exception -> 0x0213 }
        r4.document = r0;	 Catch:{ Exception -> 0x0213 }
        if (r85 == 0) goto L_0x0694;	 Catch:{ Exception -> 0x0213 }
    L_0x05d5:
        r4 = "query_id";	 Catch:{ Exception -> 0x0213 }
        r0 = r85;	 Catch:{ Exception -> 0x0213 }
        r4 = r0.containsKey(r4);	 Catch:{ Exception -> 0x0213 }
        if (r4 == 0) goto L_0x0694;	 Catch:{ Exception -> 0x0213 }
    L_0x05e0:
        r63 = 9;	 Catch:{ Exception -> 0x0213 }
    L_0x05e2:
        if (r72 == 0) goto L_0x05fb;	 Catch:{ Exception -> 0x0213 }
    L_0x05e4:
        r65 = r72.getString();	 Catch:{ Exception -> 0x0213 }
        if (r85 != 0) goto L_0x05f1;	 Catch:{ Exception -> 0x0213 }
    L_0x05ea:
        r52 = new java.util.HashMap;	 Catch:{ Exception -> 0x0213 }
        r52.<init>();	 Catch:{ Exception -> 0x0213 }
        r85 = r52;	 Catch:{ Exception -> 0x0213 }
    L_0x05f1:
        r4 = "ve";	 Catch:{ Exception -> 0x0213 }
        r0 = r85;	 Catch:{ Exception -> 0x0213 }
        r1 = r65;	 Catch:{ Exception -> 0x0213 }
        r0.put(r4, r1);	 Catch:{ Exception -> 0x0213 }
    L_0x05fb:
        if (r16 == 0) goto L_0x06b4;	 Catch:{ Exception -> 0x0213 }
    L_0x05fd:
        r0 = r74;	 Catch:{ Exception -> 0x0213 }
        r4 = r0.dc_id;	 Catch:{ Exception -> 0x0213 }
        if (r4 <= 0) goto L_0x06b4;	 Catch:{ Exception -> 0x0213 }
    L_0x0603:
        r4 = org.telegram.messenger.MessageObject.isStickerDocument(r74);	 Catch:{ Exception -> 0x0213 }
        if (r4 != 0) goto L_0x06b4;	 Catch:{ Exception -> 0x0213 }
    L_0x0609:
        r4 = org.telegram.messenger.FileLoader.getPathToAttach(r74);	 Catch:{ Exception -> 0x0213 }
        r4 = r4.toString();	 Catch:{ Exception -> 0x0213 }
        r0 = r46;	 Catch:{ Exception -> 0x0213 }
        r0.attachPath = r4;	 Catch:{ Exception -> 0x0213 }
    L_0x0615:
        if (r16 == 0) goto L_0x038c;	 Catch:{ Exception -> 0x0213 }
    L_0x0617:
        r4 = org.telegram.messenger.MessageObject.isStickerDocument(r74);	 Catch:{ Exception -> 0x0213 }
        if (r4 == 0) goto L_0x038c;	 Catch:{ Exception -> 0x0213 }
    L_0x061d:
        r20 = 0;	 Catch:{ Exception -> 0x0213 }
    L_0x061f:
        r0 = r74;	 Catch:{ Exception -> 0x0213 }
        r4 = r0.attributes;	 Catch:{ Exception -> 0x0213 }
        r4 = r4.size();	 Catch:{ Exception -> 0x0213 }
        r0 = r20;	 Catch:{ Exception -> 0x0213 }
        if (r0 >= r4) goto L_0x038c;	 Catch:{ Exception -> 0x0213 }
    L_0x062b:
        r0 = r74;	 Catch:{ Exception -> 0x0213 }
        r4 = r0.attributes;	 Catch:{ Exception -> 0x0213 }
        r0 = r20;	 Catch:{ Exception -> 0x0213 }
        r23 = r4.get(r0);	 Catch:{ Exception -> 0x0213 }
        r23 = (org.telegram.tgnet.TLRPC.DocumentAttribute) r23;	 Catch:{ Exception -> 0x0213 }
        r0 = r23;	 Catch:{ Exception -> 0x0213 }
        r4 = r0 instanceof org.telegram.tgnet.TLRPC.TL_documentAttributeSticker;	 Catch:{ Exception -> 0x0213 }
        if (r4 == 0) goto L_0x06e5;	 Catch:{ Exception -> 0x0213 }
    L_0x063d:
        r0 = r74;	 Catch:{ Exception -> 0x0213 }
        r4 = r0.attributes;	 Catch:{ Exception -> 0x0213 }
        r0 = r20;	 Catch:{ Exception -> 0x0213 }
        r4.remove(r0);	 Catch:{ Exception -> 0x0213 }
        r24 = new org.telegram.tgnet.TLRPC$TL_documentAttributeSticker_layer55;	 Catch:{ Exception -> 0x0213 }
        r24.<init>();	 Catch:{ Exception -> 0x0213 }
        r0 = r74;	 Catch:{ Exception -> 0x0213 }
        r4 = r0.attributes;	 Catch:{ Exception -> 0x0213 }
        r0 = r24;	 Catch:{ Exception -> 0x0213 }
        r4.add(r0);	 Catch:{ Exception -> 0x0213 }
        r0 = r23;	 Catch:{ Exception -> 0x0213 }
        r4 = r0.alt;	 Catch:{ Exception -> 0x0213 }
        r0 = r24;	 Catch:{ Exception -> 0x0213 }
        r0.alt = r4;	 Catch:{ Exception -> 0x0213 }
        r0 = r23;	 Catch:{ Exception -> 0x0213 }
        r4 = r0.stickerset;	 Catch:{ Exception -> 0x0213 }
        if (r4 == 0) goto L_0x06da;	 Catch:{ Exception -> 0x0213 }
    L_0x0662:
        r0 = r23;	 Catch:{ Exception -> 0x0213 }
        r4 = r0.stickerset;	 Catch:{ Exception -> 0x0213 }
        r4 = r4 instanceof org.telegram.tgnet.TLRPC.TL_inputStickerSetShortName;	 Catch:{ Exception -> 0x0213 }
        if (r4 == 0) goto L_0x06bc;	 Catch:{ Exception -> 0x0213 }
    L_0x066a:
        r0 = r23;	 Catch:{ Exception -> 0x0213 }
        r4 = r0.stickerset;	 Catch:{ Exception -> 0x0213 }
        r0 = r4.short_name;	 Catch:{ Exception -> 0x0213 }
        r45 = r0;	 Catch:{ Exception -> 0x0213 }
    L_0x0672:
        r4 = android.text.TextUtils.isEmpty(r45);	 Catch:{ Exception -> 0x0213 }
        if (r4 != 0) goto L_0x06cf;	 Catch:{ Exception -> 0x0213 }
    L_0x0678:
        r4 = new org.telegram.tgnet.TLRPC$TL_inputStickerSetShortName;	 Catch:{ Exception -> 0x0213 }
        r4.<init>();	 Catch:{ Exception -> 0x0213 }
        r0 = r24;	 Catch:{ Exception -> 0x0213 }
        r0.stickerset = r4;	 Catch:{ Exception -> 0x0213 }
        r0 = r24;	 Catch:{ Exception -> 0x0213 }
        r4 = r0.stickerset;	 Catch:{ Exception -> 0x0213 }
        r0 = r45;	 Catch:{ Exception -> 0x0213 }
        r4.short_name = r0;	 Catch:{ Exception -> 0x0213 }
        goto L_0x038c;	 Catch:{ Exception -> 0x0213 }
    L_0x068b:
        r47 = new org.telegram.tgnet.TLRPC$TL_message;	 Catch:{ Exception -> 0x0213 }
        r47.<init>();	 Catch:{ Exception -> 0x0213 }
        r46 = r47;	 Catch:{ Exception -> 0x0213 }
        goto L_0x059e;	 Catch:{ Exception -> 0x0213 }
    L_0x0694:
        r4 = org.telegram.messenger.MessageObject.isVideoDocument(r74);	 Catch:{ Exception -> 0x0213 }
        if (r4 != 0) goto L_0x06a2;	 Catch:{ Exception -> 0x0213 }
    L_0x069a:
        r4 = org.telegram.messenger.MessageObject.isRoundVideoDocument(r74);	 Catch:{ Exception -> 0x0213 }
        if (r4 != 0) goto L_0x06a2;	 Catch:{ Exception -> 0x0213 }
    L_0x06a0:
        if (r72 == 0) goto L_0x06a6;	 Catch:{ Exception -> 0x0213 }
    L_0x06a2:
        r63 = 3;	 Catch:{ Exception -> 0x0213 }
        goto L_0x05e2;	 Catch:{ Exception -> 0x0213 }
    L_0x06a6:
        r4 = org.telegram.messenger.MessageObject.isVoiceDocument(r74);	 Catch:{ Exception -> 0x0213 }
        if (r4 == 0) goto L_0x06b0;	 Catch:{ Exception -> 0x0213 }
    L_0x06ac:
        r63 = 8;	 Catch:{ Exception -> 0x0213 }
        goto L_0x05e2;	 Catch:{ Exception -> 0x0213 }
    L_0x06b0:
        r63 = 7;	 Catch:{ Exception -> 0x0213 }
        goto L_0x05e2;	 Catch:{ Exception -> 0x0213 }
    L_0x06b4:
        r0 = r78;	 Catch:{ Exception -> 0x0213 }
        r1 = r46;	 Catch:{ Exception -> 0x0213 }
        r1.attachPath = r0;	 Catch:{ Exception -> 0x0213 }
        goto L_0x0615;	 Catch:{ Exception -> 0x0213 }
    L_0x06bc:
        r0 = r67;	 Catch:{ Exception -> 0x0213 }
        r4 = r0.currentAccount;	 Catch:{ Exception -> 0x0213 }
        r4 = org.telegram.messenger.DataQuery.getInstance(r4);	 Catch:{ Exception -> 0x0213 }
        r0 = r23;	 Catch:{ Exception -> 0x0213 }
        r6 = r0.stickerset;	 Catch:{ Exception -> 0x0213 }
        r8 = r6.id;	 Catch:{ Exception -> 0x0213 }
        r45 = r4.getStickerSetName(r8);	 Catch:{ Exception -> 0x0213 }
        goto L_0x0672;	 Catch:{ Exception -> 0x0213 }
    L_0x06cf:
        r4 = new org.telegram.tgnet.TLRPC$TL_inputStickerSetEmpty;	 Catch:{ Exception -> 0x0213 }
        r4.<init>();	 Catch:{ Exception -> 0x0213 }
        r0 = r24;	 Catch:{ Exception -> 0x0213 }
        r0.stickerset = r4;	 Catch:{ Exception -> 0x0213 }
        goto L_0x038c;	 Catch:{ Exception -> 0x0213 }
    L_0x06da:
        r4 = new org.telegram.tgnet.TLRPC$TL_inputStickerSetEmpty;	 Catch:{ Exception -> 0x0213 }
        r4.<init>();	 Catch:{ Exception -> 0x0213 }
        r0 = r24;	 Catch:{ Exception -> 0x0213 }
        r0.stickerset = r4;	 Catch:{ Exception -> 0x0213 }
        goto L_0x038c;	 Catch:{ Exception -> 0x0213 }
    L_0x06e5:
        r20 = r20 + 1;	 Catch:{ Exception -> 0x0213 }
        goto L_0x061f;	 Catch:{ Exception -> 0x0213 }
    L_0x06e9:
        r0 = r46;	 Catch:{ Exception -> 0x0213 }
        r4 = r0.message;	 Catch:{ Exception -> 0x0213 }
        if (r4 != 0) goto L_0x03ac;	 Catch:{ Exception -> 0x0213 }
    L_0x06ef:
        r4 = "";	 Catch:{ Exception -> 0x0213 }
        r0 = r46;	 Catch:{ Exception -> 0x0213 }
        r0.message = r4;	 Catch:{ Exception -> 0x0213 }
        goto L_0x03ac;	 Catch:{ Exception -> 0x0213 }
    L_0x06f8:
        r0 = r67;	 Catch:{ Exception -> 0x0213 }
        r4 = r0.currentAccount;	 Catch:{ Exception -> 0x0213 }
        r4 = org.telegram.messenger.UserConfig.getInstance(r4);	 Catch:{ Exception -> 0x0213 }
        r4 = r4.getClientUserId();	 Catch:{ Exception -> 0x0213 }
        r0 = r46;	 Catch:{ Exception -> 0x0213 }
        r0.from_id = r4;	 Catch:{ Exception -> 0x0213 }
        r0 = r46;	 Catch:{ Exception -> 0x0213 }
        r4 = r0.flags;	 Catch:{ Exception -> 0x0213 }
        r4 = r4 | 256;	 Catch:{ Exception -> 0x0213 }
        r0 = r46;	 Catch:{ Exception -> 0x0213 }
        r0.flags = r4;	 Catch:{ Exception -> 0x0213 }
        goto L_0x03df;	 Catch:{ Exception -> 0x0213 }
    L_0x0714:
        r4 = "bot";	 Catch:{ Exception -> 0x0213 }
        r0 = r85;	 Catch:{ Exception -> 0x0213 }
        r4 = r0.get(r4);	 Catch:{ Exception -> 0x0213 }
        r4 = (java.lang.String) r4;	 Catch:{ Exception -> 0x0213 }
        r4 = org.telegram.messenger.Utilities.parseInt(r4);	 Catch:{ Exception -> 0x0213 }
        r4 = r4.intValue();	 Catch:{ Exception -> 0x0213 }
        r0 = r46;	 Catch:{ Exception -> 0x0213 }
        r0.via_bot_id = r4;	 Catch:{ Exception -> 0x0213 }
        goto L_0x0118;	 Catch:{ Exception -> 0x0213 }
    L_0x072d:
        r4 = 1;	 Catch:{ Exception -> 0x0213 }
        r0 = r46;	 Catch:{ Exception -> 0x0213 }
        r0.post = r4;	 Catch:{ Exception -> 0x0213 }
        r0 = r26;	 Catch:{ Exception -> 0x0213 }
        r4 = r0.signatures;	 Catch:{ Exception -> 0x0213 }
        if (r4 == 0) goto L_0x0183;	 Catch:{ Exception -> 0x0213 }
    L_0x0738:
        r0 = r67;	 Catch:{ Exception -> 0x0213 }
        r4 = r0.currentAccount;	 Catch:{ Exception -> 0x0213 }
        r4 = org.telegram.messenger.UserConfig.getInstance(r4);	 Catch:{ Exception -> 0x0213 }
        r4 = r4.getClientUserId();	 Catch:{ Exception -> 0x0213 }
        r0 = r46;	 Catch:{ Exception -> 0x0213 }
        r0.from_id = r4;	 Catch:{ Exception -> 0x0213 }
        goto L_0x0183;	 Catch:{ Exception -> 0x0213 }
    L_0x074a:
        r4 = 1;	 Catch:{ Exception -> 0x0213 }
        r0 = r46;	 Catch:{ Exception -> 0x0213 }
        r0.unread = r4;	 Catch:{ Exception -> 0x0213 }
        goto L_0x0183;	 Catch:{ Exception -> 0x0213 }
    L_0x0751:
        r0 = r46;	 Catch:{ Exception -> 0x0213 }
        r4 = r0.flags;	 Catch:{ Exception -> 0x0213 }
        r4 = r4 | 8;	 Catch:{ Exception -> 0x0213 }
        r0 = r46;	 Catch:{ Exception -> 0x0213 }
        r0.flags = r4;	 Catch:{ Exception -> 0x0213 }
        goto L_0x01b7;	 Catch:{ Exception -> 0x0213 }
    L_0x075d:
        r59 = new java.util.ArrayList;	 Catch:{ Exception -> 0x0213 }
        r59.<init>();	 Catch:{ Exception -> 0x0213 }
        r0 = r67;	 Catch:{ Exception -> 0x07a6 }
        r4 = r0.currentChatInfo;	 Catch:{ Exception -> 0x07a6 }
        r4 = r4.participants;	 Catch:{ Exception -> 0x07a6 }
        r4 = r4.participants;	 Catch:{ Exception -> 0x07a6 }
        r4 = r4.iterator();	 Catch:{ Exception -> 0x07a6 }
    L_0x076e:
        r6 = r4.hasNext();	 Catch:{ Exception -> 0x07a6 }
        if (r6 == 0) goto L_0x07ad;	 Catch:{ Exception -> 0x07a6 }
    L_0x0774:
        r53 = r4.next();	 Catch:{ Exception -> 0x07a6 }
        r53 = (org.telegram.tgnet.TLRPC.ChatParticipant) r53;	 Catch:{ Exception -> 0x07a6 }
        r0 = r67;	 Catch:{ Exception -> 0x07a6 }
        r6 = r0.currentAccount;	 Catch:{ Exception -> 0x07a6 }
        r6 = org.telegram.messenger.MessagesController.getInstance(r6);	 Catch:{ Exception -> 0x07a6 }
        r0 = r53;	 Catch:{ Exception -> 0x07a6 }
        r8 = r0.user_id;	 Catch:{ Exception -> 0x07a6 }
        r8 = java.lang.Integer.valueOf(r8);	 Catch:{ Exception -> 0x07a6 }
        r60 = r6.getUser(r8);	 Catch:{ Exception -> 0x07a6 }
        r0 = r67;	 Catch:{ Exception -> 0x07a6 }
        r6 = r0.currentAccount;	 Catch:{ Exception -> 0x07a6 }
        r6 = org.telegram.messenger.MessagesController.getInstance(r6);	 Catch:{ Exception -> 0x07a6 }
        r0 = r60;	 Catch:{ Exception -> 0x07a6 }
        r54 = r6.getInputUser(r0);	 Catch:{ Exception -> 0x07a6 }
        if (r54 == 0) goto L_0x076e;	 Catch:{ Exception -> 0x07a6 }
    L_0x079e:
        r0 = r59;	 Catch:{ Exception -> 0x07a6 }
        r1 = r54;	 Catch:{ Exception -> 0x07a6 }
        r0.add(r1);	 Catch:{ Exception -> 0x07a6 }
        goto L_0x076e;	 Catch:{ Exception -> 0x07a6 }
    L_0x07a6:
        r31 = move-exception;	 Catch:{ Exception -> 0x07a6 }
        r58 = r59;	 Catch:{ Exception -> 0x07a6 }
        r12 = r48;	 Catch:{ Exception -> 0x07a6 }
        goto L_0x0216;	 Catch:{ Exception -> 0x07a6 }
    L_0x07ad:
        r4 = new org.telegram.tgnet.TLRPC$TL_peerChat;	 Catch:{ Exception -> 0x07a6 }
        r4.<init>();	 Catch:{ Exception -> 0x07a6 }
        r0 = r46;	 Catch:{ Exception -> 0x07a6 }
        r0.to_id = r4;	 Catch:{ Exception -> 0x07a6 }
        r0 = r46;	 Catch:{ Exception -> 0x07a6 }
        r4 = r0.to_id;	 Catch:{ Exception -> 0x07a6 }
        r0 = r42;	 Catch:{ Exception -> 0x07a6 }
        r4.chat_id = r0;	 Catch:{ Exception -> 0x07a6 }
        r58 = r59;
    L_0x07c0:
        r4 = 1;
        r0 = r35;
        if (r0 == r4) goto L_0x07d6;
    L_0x07c5:
        r4 = org.telegram.messenger.MessageObject.isVoiceMessage(r46);	 Catch:{ Exception -> 0x0213 }
        if (r4 != 0) goto L_0x07d1;	 Catch:{ Exception -> 0x0213 }
    L_0x07cb:
        r4 = org.telegram.messenger.MessageObject.isRoundVideoMessage(r46);	 Catch:{ Exception -> 0x0213 }
        if (r4 == 0) goto L_0x07d6;	 Catch:{ Exception -> 0x0213 }
    L_0x07d1:
        r4 = 1;	 Catch:{ Exception -> 0x0213 }
        r0 = r46;	 Catch:{ Exception -> 0x0213 }
        r0.media_unread = r4;	 Catch:{ Exception -> 0x0213 }
    L_0x07d6:
        r4 = 1;	 Catch:{ Exception -> 0x0213 }
        r0 = r46;	 Catch:{ Exception -> 0x0213 }
        r0.send_state = r4;	 Catch:{ Exception -> 0x0213 }
        r12 = new org.telegram.messenger.MessageObject;	 Catch:{ Exception -> 0x0213 }
        r0 = r67;	 Catch:{ Exception -> 0x0213 }
        r4 = r0.currentAccount;	 Catch:{ Exception -> 0x0213 }
        r6 = 1;	 Catch:{ Exception -> 0x0213 }
        r0 = r46;	 Catch:{ Exception -> 0x0213 }
        r12.<init>(r4, r0, r6);	 Catch:{ Exception -> 0x0213 }
        r0 = r79;	 Catch:{ Exception -> 0x0dc0 }
        r12.replyMessageObject = r0;	 Catch:{ Exception -> 0x0dc0 }
        r4 = r12.isForwarded();	 Catch:{ Exception -> 0x0dc0 }
        if (r4 != 0) goto L_0x080a;	 Catch:{ Exception -> 0x0dc0 }
    L_0x07f1:
        r4 = r12.type;	 Catch:{ Exception -> 0x0dc0 }
        r6 = 3;	 Catch:{ Exception -> 0x0dc0 }
        if (r4 == r6) goto L_0x07fd;	 Catch:{ Exception -> 0x0dc0 }
    L_0x07f6:
        if (r72 != 0) goto L_0x07fd;	 Catch:{ Exception -> 0x0dc0 }
    L_0x07f8:
        r4 = r12.type;	 Catch:{ Exception -> 0x0dc0 }
        r6 = 2;	 Catch:{ Exception -> 0x0dc0 }
        if (r4 != r6) goto L_0x080a;	 Catch:{ Exception -> 0x0dc0 }
    L_0x07fd:
        r0 = r46;	 Catch:{ Exception -> 0x0dc0 }
        r4 = r0.attachPath;	 Catch:{ Exception -> 0x0dc0 }
        r4 = android.text.TextUtils.isEmpty(r4);	 Catch:{ Exception -> 0x0dc0 }
        if (r4 != 0) goto L_0x080a;	 Catch:{ Exception -> 0x0dc0 }
    L_0x0807:
        r4 = 1;	 Catch:{ Exception -> 0x0dc0 }
        r12.attachPathExists = r4;	 Catch:{ Exception -> 0x0dc0 }
    L_0x080a:
        r4 = r12.videoEditedInfo;	 Catch:{ Exception -> 0x0dc0 }
        if (r4 == 0) goto L_0x0814;	 Catch:{ Exception -> 0x0dc0 }
    L_0x080e:
        if (r72 != 0) goto L_0x0814;	 Catch:{ Exception -> 0x0dc0 }
    L_0x0810:
        r0 = r12.videoEditedInfo;	 Catch:{ Exception -> 0x0dc0 }
        r72 = r0;	 Catch:{ Exception -> 0x0dc0 }
    L_0x0814:
        r32 = 0;	 Catch:{ Exception -> 0x0dc0 }
        r39 = 0;	 Catch:{ Exception -> 0x0dc0 }
        if (r85 == 0) goto L_0x084d;	 Catch:{ Exception -> 0x0dc0 }
    L_0x081a:
        r4 = "groupId";	 Catch:{ Exception -> 0x0dc0 }
        r0 = r85;	 Catch:{ Exception -> 0x0dc0 }
        r34 = r0.get(r4);	 Catch:{ Exception -> 0x0dc0 }
        r34 = (java.lang.String) r34;	 Catch:{ Exception -> 0x0dc0 }
        if (r34 == 0) goto L_0x0840;	 Catch:{ Exception -> 0x0dc0 }
    L_0x0827:
        r4 = org.telegram.messenger.Utilities.parseLong(r34);	 Catch:{ Exception -> 0x0dc0 }
        r32 = r4.longValue();	 Catch:{ Exception -> 0x0dc0 }
        r0 = r32;	 Catch:{ Exception -> 0x0dc0 }
        r2 = r46;	 Catch:{ Exception -> 0x0dc0 }
        r2.grouped_id = r0;	 Catch:{ Exception -> 0x0dc0 }
        r0 = r46;	 Catch:{ Exception -> 0x0dc0 }
        r4 = r0.flags;	 Catch:{ Exception -> 0x0dc0 }
        r6 = 131072; // 0x20000 float:1.83671E-40 double:6.47582E-319;	 Catch:{ Exception -> 0x0dc0 }
        r4 = r4 | r6;	 Catch:{ Exception -> 0x0dc0 }
        r0 = r46;	 Catch:{ Exception -> 0x0dc0 }
        r0.flags = r4;	 Catch:{ Exception -> 0x0dc0 }
    L_0x0840:
        r4 = "final";	 Catch:{ Exception -> 0x0dc0 }
        r0 = r85;	 Catch:{ Exception -> 0x0dc0 }
        r4 = r0.get(r4);	 Catch:{ Exception -> 0x0dc0 }
        if (r4 == 0) goto L_0x0a61;	 Catch:{ Exception -> 0x0dc0 }
    L_0x084b:
        r39 = 1;	 Catch:{ Exception -> 0x0dc0 }
    L_0x084d:
        r8 = 0;	 Catch:{ Exception -> 0x0dc0 }
        r4 = (r32 > r8 ? 1 : (r32 == r8 ? 0 : -1));	 Catch:{ Exception -> 0x0dc0 }
        if (r4 != 0) goto L_0x0a65;	 Catch:{ Exception -> 0x0dc0 }
    L_0x0853:
        r50 = new java.util.ArrayList;	 Catch:{ Exception -> 0x0dc0 }
        r50.<init>();	 Catch:{ Exception -> 0x0dc0 }
        r0 = r50;	 Catch:{ Exception -> 0x0dc0 }
        r0.add(r12);	 Catch:{ Exception -> 0x0dc0 }
        r5 = new java.util.ArrayList;	 Catch:{ Exception -> 0x0dc0 }
        r5.<init>();	 Catch:{ Exception -> 0x0dc0 }
        r0 = r46;	 Catch:{ Exception -> 0x0dc0 }
        r5.add(r0);	 Catch:{ Exception -> 0x0dc0 }
        r0 = r67;	 Catch:{ Exception -> 0x0dc0 }
        r4 = r0.currentAccount;	 Catch:{ Exception -> 0x0dc0 }
        r4 = org.telegram.messenger.MessagesStorage.getInstance(r4);	 Catch:{ Exception -> 0x0dc0 }
        r6 = 0;	 Catch:{ Exception -> 0x0dc0 }
        r7 = 1;	 Catch:{ Exception -> 0x0dc0 }
        r8 = 0;	 Catch:{ Exception -> 0x0dc0 }
        r9 = 0;	 Catch:{ Exception -> 0x0dc0 }
        r4.putMessages(r5, r6, r7, r8, r9);	 Catch:{ Exception -> 0x0dc0 }
        r0 = r67;	 Catch:{ Exception -> 0x0dc0 }
        r4 = r0.currentAccount;	 Catch:{ Exception -> 0x0dc0 }
        r4 = org.telegram.messenger.MessagesController.getInstance(r4);	 Catch:{ Exception -> 0x0dc0 }
        r0 = r76;	 Catch:{ Exception -> 0x0dc0 }
        r2 = r50;	 Catch:{ Exception -> 0x0dc0 }
        r4.updateInterfaceWithMessages(r0, r2);	 Catch:{ Exception -> 0x0dc0 }
        r0 = r67;	 Catch:{ Exception -> 0x0dc0 }
        r4 = r0.currentAccount;	 Catch:{ Exception -> 0x0dc0 }
        r4 = org.telegram.messenger.NotificationCenter.getInstance(r4);	 Catch:{ Exception -> 0x0dc0 }
        r6 = org.telegram.messenger.NotificationCenter.dialogsNeedReload;	 Catch:{ Exception -> 0x0dc0 }
        r8 = 0;	 Catch:{ Exception -> 0x0dc0 }
        r8 = new java.lang.Object[r8];	 Catch:{ Exception -> 0x0dc0 }
        r4.postNotificationName(r6, r8);	 Catch:{ Exception -> 0x0dc0 }
        r29 = r28;
    L_0x0897:
        r4 = org.telegram.messenger.BuildVars.LOGS_ENABLED;	 Catch:{ Exception -> 0x0c48 }
        if (r4 == 0) goto L_0x08e5;	 Catch:{ Exception -> 0x0c48 }
    L_0x089b:
        if (r57 == 0) goto L_0x08e5;	 Catch:{ Exception -> 0x0c48 }
    L_0x089d:
        r4 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x0c48 }
        r4.<init>();	 Catch:{ Exception -> 0x0c48 }
        r6 = "send message user_id = ";	 Catch:{ Exception -> 0x0c48 }
        r4 = r4.append(r6);	 Catch:{ Exception -> 0x0c48 }
        r0 = r57;	 Catch:{ Exception -> 0x0c48 }
        r6 = r0.user_id;	 Catch:{ Exception -> 0x0c48 }
        r4 = r4.append(r6);	 Catch:{ Exception -> 0x0c48 }
        r6 = " chat_id = ";	 Catch:{ Exception -> 0x0c48 }
        r4 = r4.append(r6);	 Catch:{ Exception -> 0x0c48 }
        r0 = r57;	 Catch:{ Exception -> 0x0c48 }
        r6 = r0.chat_id;	 Catch:{ Exception -> 0x0c48 }
        r4 = r4.append(r6);	 Catch:{ Exception -> 0x0c48 }
        r6 = " channel_id = ";	 Catch:{ Exception -> 0x0c48 }
        r4 = r4.append(r6);	 Catch:{ Exception -> 0x0c48 }
        r0 = r57;	 Catch:{ Exception -> 0x0c48 }
        r6 = r0.channel_id;	 Catch:{ Exception -> 0x0c48 }
        r4 = r4.append(r6);	 Catch:{ Exception -> 0x0c48 }
        r6 = " access_hash = ";	 Catch:{ Exception -> 0x0c48 }
        r4 = r4.append(r6);	 Catch:{ Exception -> 0x0c48 }
        r0 = r57;	 Catch:{ Exception -> 0x0c48 }
        r8 = r0.access_hash;	 Catch:{ Exception -> 0x0c48 }
        r4 = r4.append(r8);	 Catch:{ Exception -> 0x0c48 }
        r4 = r4.toString();	 Catch:{ Exception -> 0x0c48 }
        org.telegram.messenger.FileLog.m0d(r4);	 Catch:{ Exception -> 0x0c48 }
    L_0x08e5:
        if (r63 == 0) goto L_0x08f1;	 Catch:{ Exception -> 0x0c48 }
    L_0x08e7:
        r4 = 9;	 Catch:{ Exception -> 0x0c48 }
        r0 = r63;	 Catch:{ Exception -> 0x0c48 }
        if (r0 != r4) goto L_0x0c4d;	 Catch:{ Exception -> 0x0c48 }
    L_0x08ed:
        if (r68 == 0) goto L_0x0c4d;	 Catch:{ Exception -> 0x0c48 }
    L_0x08ef:
        if (r16 == 0) goto L_0x0c4d;	 Catch:{ Exception -> 0x0c48 }
    L_0x08f1:
        if (r16 != 0) goto L_0x0b8e;	 Catch:{ Exception -> 0x0c48 }
    L_0x08f3:
        if (r58 == 0) goto L_0x0b04;	 Catch:{ Exception -> 0x0c48 }
    L_0x08f5:
        r7 = new org.telegram.tgnet.TLRPC$TL_messages_sendBroadcast;	 Catch:{ Exception -> 0x0c48 }
        r7.<init>();	 Catch:{ Exception -> 0x0c48 }
        r55 = new java.util.ArrayList;	 Catch:{ Exception -> 0x0c48 }
        r55.<init>();	 Catch:{ Exception -> 0x0c48 }
        r20 = 0;	 Catch:{ Exception -> 0x0c48 }
    L_0x0901:
        r4 = r58.size();	 Catch:{ Exception -> 0x0c48 }
        r0 = r20;	 Catch:{ Exception -> 0x0c48 }
        if (r0 >= r4) goto L_0x0ae7;	 Catch:{ Exception -> 0x0c48 }
    L_0x0909:
        r4 = org.telegram.messenger.Utilities.random;	 Catch:{ Exception -> 0x0c48 }
        r8 = r4.nextLong();	 Catch:{ Exception -> 0x0c48 }
        r4 = java.lang.Long.valueOf(r8);	 Catch:{ Exception -> 0x0c48 }
        r0 = r55;	 Catch:{ Exception -> 0x0c48 }
        r0.add(r4);	 Catch:{ Exception -> 0x0c48 }
        r20 = r20 + 1;
        goto L_0x0901;
    L_0x091b:
        r0 = r67;	 Catch:{ Exception -> 0x0213 }
        r4 = r0.currentAccount;	 Catch:{ Exception -> 0x0213 }
        r4 = org.telegram.messenger.MessagesController.getInstance(r4);	 Catch:{ Exception -> 0x0213 }
        r0 = r42;	 Catch:{ Exception -> 0x0213 }
        r4 = r4.getPeer(r0);	 Catch:{ Exception -> 0x0213 }
        r0 = r46;	 Catch:{ Exception -> 0x0213 }
        r0.to_id = r4;	 Catch:{ Exception -> 0x0213 }
        if (r42 <= 0) goto L_0x07c0;	 Catch:{ Exception -> 0x0213 }
    L_0x092f:
        r0 = r67;	 Catch:{ Exception -> 0x0213 }
        r4 = r0.currentAccount;	 Catch:{ Exception -> 0x0213 }
        r4 = org.telegram.messenger.MessagesController.getInstance(r4);	 Catch:{ Exception -> 0x0213 }
        r6 = java.lang.Integer.valueOf(r42);	 Catch:{ Exception -> 0x0213 }
        r60 = r4.getUser(r6);	 Catch:{ Exception -> 0x0213 }
        if (r60 != 0) goto L_0x094c;	 Catch:{ Exception -> 0x0213 }
    L_0x0941:
        r0 = r46;	 Catch:{ Exception -> 0x0213 }
        r4 = r0.id;	 Catch:{ Exception -> 0x0213 }
        r0 = r67;	 Catch:{ Exception -> 0x0213 }
        r0.processSentMessage(r4);	 Catch:{ Exception -> 0x0213 }
        goto L_0x0006;	 Catch:{ Exception -> 0x0213 }
    L_0x094c:
        r0 = r60;	 Catch:{ Exception -> 0x0213 }
        r4 = r0.bot;	 Catch:{ Exception -> 0x0213 }
        if (r4 == 0) goto L_0x07c0;	 Catch:{ Exception -> 0x0213 }
    L_0x0952:
        r4 = 0;	 Catch:{ Exception -> 0x0213 }
        r0 = r46;	 Catch:{ Exception -> 0x0213 }
        r0.unread = r4;	 Catch:{ Exception -> 0x0213 }
        goto L_0x07c0;	 Catch:{ Exception -> 0x0213 }
    L_0x0959:
        r4 = new org.telegram.tgnet.TLRPC$TL_peerUser;	 Catch:{ Exception -> 0x0213 }
        r4.<init>();	 Catch:{ Exception -> 0x0213 }
        r0 = r46;	 Catch:{ Exception -> 0x0213 }
        r0.to_id = r4;	 Catch:{ Exception -> 0x0213 }
        r0 = r16;	 Catch:{ Exception -> 0x0213 }
        r4 = r0.participant_id;	 Catch:{ Exception -> 0x0213 }
        r0 = r67;	 Catch:{ Exception -> 0x0213 }
        r6 = r0.currentAccount;	 Catch:{ Exception -> 0x0213 }
        r6 = org.telegram.messenger.UserConfig.getInstance(r6);	 Catch:{ Exception -> 0x0213 }
        r6 = r6.getClientUserId();	 Catch:{ Exception -> 0x0213 }
        if (r4 != r6) goto L_0x09da;	 Catch:{ Exception -> 0x0213 }
    L_0x0974:
        r0 = r46;	 Catch:{ Exception -> 0x0213 }
        r4 = r0.to_id;	 Catch:{ Exception -> 0x0213 }
        r0 = r16;	 Catch:{ Exception -> 0x0213 }
        r6 = r0.admin_id;	 Catch:{ Exception -> 0x0213 }
        r4.user_id = r6;	 Catch:{ Exception -> 0x0213 }
    L_0x097e:
        if (r86 == 0) goto L_0x09e5;	 Catch:{ Exception -> 0x0213 }
    L_0x0980:
        r0 = r86;	 Catch:{ Exception -> 0x0213 }
        r1 = r46;	 Catch:{ Exception -> 0x0213 }
        r1.ttl = r0;	 Catch:{ Exception -> 0x0213 }
    L_0x0986:
        r0 = r46;	 Catch:{ Exception -> 0x0213 }
        r4 = r0.ttl;	 Catch:{ Exception -> 0x0213 }
        if (r4 == 0) goto L_0x07c0;	 Catch:{ Exception -> 0x0213 }
    L_0x098c:
        r0 = r46;	 Catch:{ Exception -> 0x0213 }
        r4 = r0.media;	 Catch:{ Exception -> 0x0213 }
        r4 = r4.document;	 Catch:{ Exception -> 0x0213 }
        if (r4 == 0) goto L_0x07c0;	 Catch:{ Exception -> 0x0213 }
    L_0x0994:
        r4 = org.telegram.messenger.MessageObject.isVoiceMessage(r46);	 Catch:{ Exception -> 0x0213 }
        if (r4 == 0) goto L_0x0a12;	 Catch:{ Exception -> 0x0213 }
    L_0x099a:
        r30 = 0;	 Catch:{ Exception -> 0x0213 }
        r20 = 0;	 Catch:{ Exception -> 0x0213 }
    L_0x099e:
        r0 = r46;	 Catch:{ Exception -> 0x0213 }
        r4 = r0.media;	 Catch:{ Exception -> 0x0213 }
        r4 = r4.document;	 Catch:{ Exception -> 0x0213 }
        r4 = r4.attributes;	 Catch:{ Exception -> 0x0213 }
        r4 = r4.size();	 Catch:{ Exception -> 0x0213 }
        r0 = r20;	 Catch:{ Exception -> 0x0213 }
        if (r0 >= r4) goto L_0x09ca;	 Catch:{ Exception -> 0x0213 }
    L_0x09ae:
        r0 = r46;	 Catch:{ Exception -> 0x0213 }
        r4 = r0.media;	 Catch:{ Exception -> 0x0213 }
        r4 = r4.document;	 Catch:{ Exception -> 0x0213 }
        r4 = r4.attributes;	 Catch:{ Exception -> 0x0213 }
        r0 = r20;	 Catch:{ Exception -> 0x0213 }
        r23 = r4.get(r0);	 Catch:{ Exception -> 0x0213 }
        r23 = (org.telegram.tgnet.TLRPC.DocumentAttribute) r23;	 Catch:{ Exception -> 0x0213 }
        r0 = r23;	 Catch:{ Exception -> 0x0213 }
        r4 = r0 instanceof org.telegram.tgnet.TLRPC.TL_documentAttributeAudio;	 Catch:{ Exception -> 0x0213 }
        if (r4 == 0) goto L_0x0a0f;	 Catch:{ Exception -> 0x0213 }
    L_0x09c4:
        r0 = r23;	 Catch:{ Exception -> 0x0213 }
        r0 = r0.duration;	 Catch:{ Exception -> 0x0213 }
        r30 = r0;	 Catch:{ Exception -> 0x0213 }
    L_0x09ca:
        r0 = r46;	 Catch:{ Exception -> 0x0213 }
        r4 = r0.ttl;	 Catch:{ Exception -> 0x0213 }
        r6 = r30 + 1;	 Catch:{ Exception -> 0x0213 }
        r4 = java.lang.Math.max(r4, r6);	 Catch:{ Exception -> 0x0213 }
        r0 = r46;	 Catch:{ Exception -> 0x0213 }
        r0.ttl = r4;	 Catch:{ Exception -> 0x0213 }
        goto L_0x07c0;	 Catch:{ Exception -> 0x0213 }
    L_0x09da:
        r0 = r46;	 Catch:{ Exception -> 0x0213 }
        r4 = r0.to_id;	 Catch:{ Exception -> 0x0213 }
        r0 = r16;	 Catch:{ Exception -> 0x0213 }
        r6 = r0.participant_id;	 Catch:{ Exception -> 0x0213 }
        r4.user_id = r6;	 Catch:{ Exception -> 0x0213 }
        goto L_0x097e;	 Catch:{ Exception -> 0x0213 }
    L_0x09e5:
        r0 = r16;	 Catch:{ Exception -> 0x0213 }
        r4 = r0.ttl;	 Catch:{ Exception -> 0x0213 }
        r0 = r46;	 Catch:{ Exception -> 0x0213 }
        r0.ttl = r4;	 Catch:{ Exception -> 0x0213 }
        r0 = r46;	 Catch:{ Exception -> 0x0213 }
        r4 = r0.ttl;	 Catch:{ Exception -> 0x0213 }
        if (r4 == 0) goto L_0x0986;	 Catch:{ Exception -> 0x0213 }
    L_0x09f3:
        r0 = r46;	 Catch:{ Exception -> 0x0213 }
        r4 = r0.media;	 Catch:{ Exception -> 0x0213 }
        if (r4 == 0) goto L_0x0986;	 Catch:{ Exception -> 0x0213 }
    L_0x09f9:
        r0 = r46;	 Catch:{ Exception -> 0x0213 }
        r4 = r0.media;	 Catch:{ Exception -> 0x0213 }
        r0 = r46;	 Catch:{ Exception -> 0x0213 }
        r6 = r0.ttl;	 Catch:{ Exception -> 0x0213 }
        r4.ttl_seconds = r6;	 Catch:{ Exception -> 0x0213 }
        r0 = r46;	 Catch:{ Exception -> 0x0213 }
        r4 = r0.media;	 Catch:{ Exception -> 0x0213 }
        r6 = r4.flags;	 Catch:{ Exception -> 0x0213 }
        r6 = r6 | 4;	 Catch:{ Exception -> 0x0213 }
        r4.flags = r6;	 Catch:{ Exception -> 0x0213 }
        goto L_0x0986;	 Catch:{ Exception -> 0x0213 }
    L_0x0a0f:
        r20 = r20 + 1;	 Catch:{ Exception -> 0x0213 }
        goto L_0x099e;	 Catch:{ Exception -> 0x0213 }
    L_0x0a12:
        r4 = org.telegram.messenger.MessageObject.isVideoMessage(r46);	 Catch:{ Exception -> 0x0213 }
        if (r4 != 0) goto L_0x0a1e;	 Catch:{ Exception -> 0x0213 }
    L_0x0a18:
        r4 = org.telegram.messenger.MessageObject.isRoundVideoMessage(r46);	 Catch:{ Exception -> 0x0213 }
        if (r4 == 0) goto L_0x07c0;	 Catch:{ Exception -> 0x0213 }
    L_0x0a1e:
        r30 = 0;	 Catch:{ Exception -> 0x0213 }
        r20 = 0;	 Catch:{ Exception -> 0x0213 }
    L_0x0a22:
        r0 = r46;	 Catch:{ Exception -> 0x0213 }
        r4 = r0.media;	 Catch:{ Exception -> 0x0213 }
        r4 = r4.document;	 Catch:{ Exception -> 0x0213 }
        r4 = r4.attributes;	 Catch:{ Exception -> 0x0213 }
        r4 = r4.size();	 Catch:{ Exception -> 0x0213 }
        r0 = r20;	 Catch:{ Exception -> 0x0213 }
        if (r0 >= r4) goto L_0x0a4e;	 Catch:{ Exception -> 0x0213 }
    L_0x0a32:
        r0 = r46;	 Catch:{ Exception -> 0x0213 }
        r4 = r0.media;	 Catch:{ Exception -> 0x0213 }
        r4 = r4.document;	 Catch:{ Exception -> 0x0213 }
        r4 = r4.attributes;	 Catch:{ Exception -> 0x0213 }
        r0 = r20;	 Catch:{ Exception -> 0x0213 }
        r23 = r4.get(r0);	 Catch:{ Exception -> 0x0213 }
        r23 = (org.telegram.tgnet.TLRPC.DocumentAttribute) r23;	 Catch:{ Exception -> 0x0213 }
        r0 = r23;	 Catch:{ Exception -> 0x0213 }
        r4 = r0 instanceof org.telegram.tgnet.TLRPC.TL_documentAttributeVideo;	 Catch:{ Exception -> 0x0213 }
        if (r4 == 0) goto L_0x0a5e;	 Catch:{ Exception -> 0x0213 }
    L_0x0a48:
        r0 = r23;	 Catch:{ Exception -> 0x0213 }
        r0 = r0.duration;	 Catch:{ Exception -> 0x0213 }
        r30 = r0;	 Catch:{ Exception -> 0x0213 }
    L_0x0a4e:
        r0 = r46;	 Catch:{ Exception -> 0x0213 }
        r4 = r0.ttl;	 Catch:{ Exception -> 0x0213 }
        r6 = r30 + 1;	 Catch:{ Exception -> 0x0213 }
        r4 = java.lang.Math.max(r4, r6);	 Catch:{ Exception -> 0x0213 }
        r0 = r46;	 Catch:{ Exception -> 0x0213 }
        r0.ttl = r4;	 Catch:{ Exception -> 0x0213 }
        goto L_0x07c0;
    L_0x0a5e:
        r20 = r20 + 1;
        goto L_0x0a22;
    L_0x0a61:
        r39 = 0;
        goto L_0x084d;
    L_0x0a65:
        r4 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x0dc0 }
        r4.<init>();	 Catch:{ Exception -> 0x0dc0 }
        r6 = "group_";	 Catch:{ Exception -> 0x0dc0 }
        r4 = r4.append(r6);	 Catch:{ Exception -> 0x0dc0 }
        r0 = r32;	 Catch:{ Exception -> 0x0dc0 }
        r4 = r4.append(r0);	 Catch:{ Exception -> 0x0dc0 }
        r40 = r4.toString();	 Catch:{ Exception -> 0x0dc0 }
        r0 = r67;	 Catch:{ Exception -> 0x0dc0 }
        r4 = r0.delayedMessages;	 Catch:{ Exception -> 0x0dc0 }
        r0 = r40;	 Catch:{ Exception -> 0x0dc0 }
        r22 = r4.get(r0);	 Catch:{ Exception -> 0x0dc0 }
        r22 = (java.util.ArrayList) r22;	 Catch:{ Exception -> 0x0dc0 }
        if (r22 == 0) goto L_0x1a45;	 Catch:{ Exception -> 0x0dc0 }
    L_0x0a89:
        r4 = 0;	 Catch:{ Exception -> 0x0dc0 }
        r0 = r22;	 Catch:{ Exception -> 0x0dc0 }
        r4 = r0.get(r4);	 Catch:{ Exception -> 0x0dc0 }
        r0 = r4;	 Catch:{ Exception -> 0x0dc0 }
        r0 = (org.telegram.messenger.SendMessagesHelper.DelayedMessage) r0;	 Catch:{ Exception -> 0x0dc0 }
        r28 = r0;	 Catch:{ Exception -> 0x0dc0 }
        r29 = r28;
    L_0x0a97:
        if (r29 != 0) goto L_0x1a41;
    L_0x0a99:
        r28 = new org.telegram.messenger.SendMessagesHelper$DelayedMessage;	 Catch:{ Exception -> 0x0c48 }
        r0 = r28;	 Catch:{ Exception -> 0x0c48 }
        r1 = r67;	 Catch:{ Exception -> 0x0c48 }
        r2 = r76;	 Catch:{ Exception -> 0x0c48 }
        r0.<init>(r2);	 Catch:{ Exception -> 0x0c48 }
        r4 = 4;
        r0 = r28;	 Catch:{ Exception -> 0x0dc0 }
        r0.type = r4;	 Catch:{ Exception -> 0x0dc0 }
        r0 = r32;	 Catch:{ Exception -> 0x0dc0 }
        r2 = r28;	 Catch:{ Exception -> 0x0dc0 }
        r2.groupId = r0;	 Catch:{ Exception -> 0x0dc0 }
        r4 = new java.util.ArrayList;	 Catch:{ Exception -> 0x0dc0 }
        r4.<init>();	 Catch:{ Exception -> 0x0dc0 }
        r0 = r28;	 Catch:{ Exception -> 0x0dc0 }
        r0.messageObjects = r4;	 Catch:{ Exception -> 0x0dc0 }
        r4 = new java.util.ArrayList;	 Catch:{ Exception -> 0x0dc0 }
        r4.<init>();	 Catch:{ Exception -> 0x0dc0 }
        r0 = r28;	 Catch:{ Exception -> 0x0dc0 }
        r0.messages = r4;	 Catch:{ Exception -> 0x0dc0 }
        r4 = new java.util.ArrayList;	 Catch:{ Exception -> 0x0dc0 }
        r4.<init>();	 Catch:{ Exception -> 0x0dc0 }
        r0 = r28;	 Catch:{ Exception -> 0x0dc0 }
        r0.originalPaths = r4;	 Catch:{ Exception -> 0x0dc0 }
        r4 = new java.util.HashMap;	 Catch:{ Exception -> 0x0dc0 }
        r4.<init>();	 Catch:{ Exception -> 0x0dc0 }
        r0 = r28;	 Catch:{ Exception -> 0x0dc0 }
        r0.extraHashMap = r4;	 Catch:{ Exception -> 0x0dc0 }
        r0 = r16;	 Catch:{ Exception -> 0x0dc0 }
        r1 = r28;	 Catch:{ Exception -> 0x0dc0 }
        r1.encryptedChat = r0;	 Catch:{ Exception -> 0x0dc0 }
    L_0x0ad9:
        if (r39 == 0) goto L_0x0ae3;	 Catch:{ Exception -> 0x0dc0 }
    L_0x0adb:
        r0 = r46;	 Catch:{ Exception -> 0x0dc0 }
        r4 = r0.id;	 Catch:{ Exception -> 0x0dc0 }
        r0 = r28;	 Catch:{ Exception -> 0x0dc0 }
        r0.finalGroupMessage = r4;	 Catch:{ Exception -> 0x0dc0 }
    L_0x0ae3:
        r29 = r28;
        goto L_0x0897;
    L_0x0ae7:
        r0 = r68;	 Catch:{ Exception -> 0x0c48 }
        r7.message = r0;	 Catch:{ Exception -> 0x0c48 }
        r0 = r58;	 Catch:{ Exception -> 0x0c48 }
        r7.contacts = r0;	 Catch:{ Exception -> 0x0c48 }
        r4 = new org.telegram.tgnet.TLRPC$TL_inputMediaEmpty;	 Catch:{ Exception -> 0x0c48 }
        r4.<init>();	 Catch:{ Exception -> 0x0c48 }
        r7.media = r4;	 Catch:{ Exception -> 0x0c48 }
        r0 = r55;	 Catch:{ Exception -> 0x0c48 }
        r7.random_id = r0;	 Catch:{ Exception -> 0x0c48 }
        r4 = 0;	 Catch:{ Exception -> 0x0c48 }
        r0 = r67;	 Catch:{ Exception -> 0x0c48 }
        r0.performSendMessageRequest(r7, r12, r4);	 Catch:{ Exception -> 0x0c48 }
        r28 = r29;	 Catch:{ Exception -> 0x0c48 }
        goto L_0x0006;	 Catch:{ Exception -> 0x0c48 }
    L_0x0b04:
        r7 = new org.telegram.tgnet.TLRPC$TL_messages_sendMessage;	 Catch:{ Exception -> 0x0c48 }
        r7.<init>();	 Catch:{ Exception -> 0x0c48 }
        r0 = r68;	 Catch:{ Exception -> 0x0c48 }
        r7.message = r0;	 Catch:{ Exception -> 0x0c48 }
        if (r82 != 0) goto L_0x0b8c;	 Catch:{ Exception -> 0x0c48 }
    L_0x0b0f:
        r4 = 1;	 Catch:{ Exception -> 0x0c48 }
    L_0x0b10:
        r7.clear_draft = r4;	 Catch:{ Exception -> 0x0c48 }
        r0 = r46;	 Catch:{ Exception -> 0x0c48 }
        r4 = r0.to_id;	 Catch:{ Exception -> 0x0c48 }
        r4 = r4 instanceof org.telegram.tgnet.TLRPC.TL_peerChannel;	 Catch:{ Exception -> 0x0c48 }
        if (r4 == 0) goto L_0x0b3f;	 Catch:{ Exception -> 0x0c48 }
    L_0x0b1a:
        r0 = r67;	 Catch:{ Exception -> 0x0c48 }
        r4 = r0.currentAccount;	 Catch:{ Exception -> 0x0c48 }
        r4 = org.telegram.messenger.MessagesController.getNotificationsSettings(r4);	 Catch:{ Exception -> 0x0c48 }
        r6 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x0c48 }
        r6.<init>();	 Catch:{ Exception -> 0x0c48 }
        r8 = "silent_";	 Catch:{ Exception -> 0x0c48 }
        r6 = r6.append(r8);	 Catch:{ Exception -> 0x0c48 }
        r0 = r76;	 Catch:{ Exception -> 0x0c48 }
        r6 = r6.append(r0);	 Catch:{ Exception -> 0x0c48 }
        r6 = r6.toString();	 Catch:{ Exception -> 0x0c48 }
        r8 = 0;	 Catch:{ Exception -> 0x0c48 }
        r4 = r4.getBoolean(r6, r8);	 Catch:{ Exception -> 0x0c48 }
        r7.silent = r4;	 Catch:{ Exception -> 0x0c48 }
    L_0x0b3f:
        r0 = r57;	 Catch:{ Exception -> 0x0c48 }
        r7.peer = r0;	 Catch:{ Exception -> 0x0c48 }
        r0 = r46;	 Catch:{ Exception -> 0x0c48 }
        r8 = r0.random_id;	 Catch:{ Exception -> 0x0c48 }
        r7.random_id = r8;	 Catch:{ Exception -> 0x0c48 }
        r0 = r46;	 Catch:{ Exception -> 0x0c48 }
        r4 = r0.reply_to_msg_id;	 Catch:{ Exception -> 0x0c48 }
        if (r4 == 0) goto L_0x0b5b;	 Catch:{ Exception -> 0x0c48 }
    L_0x0b4f:
        r4 = r7.flags;	 Catch:{ Exception -> 0x0c48 }
        r4 = r4 | 1;	 Catch:{ Exception -> 0x0c48 }
        r7.flags = r4;	 Catch:{ Exception -> 0x0c48 }
        r0 = r46;	 Catch:{ Exception -> 0x0c48 }
        r4 = r0.reply_to_msg_id;	 Catch:{ Exception -> 0x0c48 }
        r7.reply_to_msg_id = r4;	 Catch:{ Exception -> 0x0c48 }
    L_0x0b5b:
        if (r81 != 0) goto L_0x0b60;	 Catch:{ Exception -> 0x0c48 }
    L_0x0b5d:
        r4 = 1;	 Catch:{ Exception -> 0x0c48 }
        r7.no_webpage = r4;	 Catch:{ Exception -> 0x0c48 }
    L_0x0b60:
        if (r83 == 0) goto L_0x0b72;	 Catch:{ Exception -> 0x0c48 }
    L_0x0b62:
        r4 = r83.isEmpty();	 Catch:{ Exception -> 0x0c48 }
        if (r4 != 0) goto L_0x0b72;	 Catch:{ Exception -> 0x0c48 }
    L_0x0b68:
        r0 = r83;	 Catch:{ Exception -> 0x0c48 }
        r7.entities = r0;	 Catch:{ Exception -> 0x0c48 }
        r4 = r7.flags;	 Catch:{ Exception -> 0x0c48 }
        r4 = r4 | 8;	 Catch:{ Exception -> 0x0c48 }
        r7.flags = r4;	 Catch:{ Exception -> 0x0c48 }
    L_0x0b72:
        r4 = 0;	 Catch:{ Exception -> 0x0c48 }
        r0 = r67;	 Catch:{ Exception -> 0x0c48 }
        r0.performSendMessageRequest(r7, r12, r4);	 Catch:{ Exception -> 0x0c48 }
        if (r82 != 0) goto L_0x0b88;	 Catch:{ Exception -> 0x0c48 }
    L_0x0b7a:
        r0 = r67;	 Catch:{ Exception -> 0x0c48 }
        r4 = r0.currentAccount;	 Catch:{ Exception -> 0x0c48 }
        r4 = org.telegram.messenger.DataQuery.getInstance(r4);	 Catch:{ Exception -> 0x0c48 }
        r6 = 0;	 Catch:{ Exception -> 0x0c48 }
        r0 = r76;	 Catch:{ Exception -> 0x0c48 }
        r4.cleanDraft(r0, r6);	 Catch:{ Exception -> 0x0c48 }
    L_0x0b88:
        r28 = r29;	 Catch:{ Exception -> 0x0c48 }
        goto L_0x0006;	 Catch:{ Exception -> 0x0c48 }
    L_0x0b8c:
        r4 = 0;	 Catch:{ Exception -> 0x0c48 }
        goto L_0x0b10;	 Catch:{ Exception -> 0x0c48 }
    L_0x0b8e:
        r0 = r16;	 Catch:{ Exception -> 0x0c48 }
        r4 = r0.layer;	 Catch:{ Exception -> 0x0c48 }
        r4 = org.telegram.messenger.AndroidUtilities.getPeerLayerVersion(r4);	 Catch:{ Exception -> 0x0c48 }
        r6 = 73;	 Catch:{ Exception -> 0x0c48 }
        if (r4 < r6) goto L_0x0c39;	 Catch:{ Exception -> 0x0c48 }
    L_0x0b9a:
        r7 = new org.telegram.tgnet.TLRPC$TL_decryptedMessage;	 Catch:{ Exception -> 0x0c48 }
        r7.<init>();	 Catch:{ Exception -> 0x0c48 }
    L_0x0b9f:
        r0 = r46;	 Catch:{ Exception -> 0x0c48 }
        r4 = r0.ttl;	 Catch:{ Exception -> 0x0c48 }
        r7.ttl = r4;	 Catch:{ Exception -> 0x0c48 }
        if (r83 == 0) goto L_0x0bb7;	 Catch:{ Exception -> 0x0c48 }
    L_0x0ba7:
        r4 = r83.isEmpty();	 Catch:{ Exception -> 0x0c48 }
        if (r4 != 0) goto L_0x0bb7;	 Catch:{ Exception -> 0x0c48 }
    L_0x0bad:
        r0 = r83;	 Catch:{ Exception -> 0x0c48 }
        r7.entities = r0;	 Catch:{ Exception -> 0x0c48 }
        r4 = r7.flags;	 Catch:{ Exception -> 0x0c48 }
        r4 = r4 | 128;	 Catch:{ Exception -> 0x0c48 }
        r7.flags = r4;	 Catch:{ Exception -> 0x0c48 }
    L_0x0bb7:
        r0 = r46;	 Catch:{ Exception -> 0x0c48 }
        r8 = r0.reply_to_random_id;	 Catch:{ Exception -> 0x0c48 }
        r14 = 0;	 Catch:{ Exception -> 0x0c48 }
        r4 = (r8 > r14 ? 1 : (r8 == r14 ? 0 : -1));	 Catch:{ Exception -> 0x0c48 }
        if (r4 == 0) goto L_0x0bcd;	 Catch:{ Exception -> 0x0c48 }
    L_0x0bc1:
        r0 = r46;	 Catch:{ Exception -> 0x0c48 }
        r8 = r0.reply_to_random_id;	 Catch:{ Exception -> 0x0c48 }
        r7.reply_to_random_id = r8;	 Catch:{ Exception -> 0x0c48 }
        r4 = r7.flags;	 Catch:{ Exception -> 0x0c48 }
        r4 = r4 | 8;	 Catch:{ Exception -> 0x0c48 }
        r7.flags = r4;	 Catch:{ Exception -> 0x0c48 }
    L_0x0bcd:
        if (r85 == 0) goto L_0x0bed;	 Catch:{ Exception -> 0x0c48 }
    L_0x0bcf:
        r4 = "bot_name";	 Catch:{ Exception -> 0x0c48 }
        r0 = r85;	 Catch:{ Exception -> 0x0c48 }
        r4 = r0.get(r4);	 Catch:{ Exception -> 0x0c48 }
        if (r4 == 0) goto L_0x0bed;	 Catch:{ Exception -> 0x0c48 }
    L_0x0bda:
        r4 = "bot_name";	 Catch:{ Exception -> 0x0c48 }
        r0 = r85;	 Catch:{ Exception -> 0x0c48 }
        r4 = r0.get(r4);	 Catch:{ Exception -> 0x0c48 }
        r4 = (java.lang.String) r4;	 Catch:{ Exception -> 0x0c48 }
        r7.via_bot_name = r4;	 Catch:{ Exception -> 0x0c48 }
        r4 = r7.flags;	 Catch:{ Exception -> 0x0c48 }
        r4 = r4 | 2048;	 Catch:{ Exception -> 0x0c48 }
        r7.flags = r4;	 Catch:{ Exception -> 0x0c48 }
    L_0x0bed:
        r0 = r46;	 Catch:{ Exception -> 0x0c48 }
        r8 = r0.random_id;	 Catch:{ Exception -> 0x0c48 }
        r7.random_id = r8;	 Catch:{ Exception -> 0x0c48 }
        r0 = r68;	 Catch:{ Exception -> 0x0c48 }
        r7.message = r0;	 Catch:{ Exception -> 0x0c48 }
        if (r80 == 0) goto L_0x0c40;	 Catch:{ Exception -> 0x0c48 }
    L_0x0bf9:
        r0 = r80;	 Catch:{ Exception -> 0x0c48 }
        r4 = r0.url;	 Catch:{ Exception -> 0x0c48 }
        if (r4 == 0) goto L_0x0c40;	 Catch:{ Exception -> 0x0c48 }
    L_0x0bff:
        r4 = new org.telegram.tgnet.TLRPC$TL_decryptedMessageMediaWebPage;	 Catch:{ Exception -> 0x0c48 }
        r4.<init>();	 Catch:{ Exception -> 0x0c48 }
        r7.media = r4;	 Catch:{ Exception -> 0x0c48 }
        r4 = r7.media;	 Catch:{ Exception -> 0x0c48 }
        r0 = r80;	 Catch:{ Exception -> 0x0c48 }
        r6 = r0.url;	 Catch:{ Exception -> 0x0c48 }
        r4.url = r6;	 Catch:{ Exception -> 0x0c48 }
        r4 = r7.flags;	 Catch:{ Exception -> 0x0c48 }
        r4 = r4 | 512;	 Catch:{ Exception -> 0x0c48 }
        r7.flags = r4;	 Catch:{ Exception -> 0x0c48 }
    L_0x0c14:
        r0 = r67;	 Catch:{ Exception -> 0x0c48 }
        r4 = r0.currentAccount;	 Catch:{ Exception -> 0x0c48 }
        r6 = org.telegram.messenger.SecretChatHelper.getInstance(r4);	 Catch:{ Exception -> 0x0c48 }
        r8 = r12.messageOwner;	 Catch:{ Exception -> 0x0c48 }
        r10 = 0;	 Catch:{ Exception -> 0x0c48 }
        r11 = 0;	 Catch:{ Exception -> 0x0c48 }
        r9 = r16;	 Catch:{ Exception -> 0x0c48 }
        r6.performSendEncryptedRequest(r7, r8, r9, r10, r11, r12);	 Catch:{ Exception -> 0x0c48 }
        if (r82 != 0) goto L_0x0c35;	 Catch:{ Exception -> 0x0c48 }
    L_0x0c27:
        r0 = r67;	 Catch:{ Exception -> 0x0c48 }
        r4 = r0.currentAccount;	 Catch:{ Exception -> 0x0c48 }
        r4 = org.telegram.messenger.DataQuery.getInstance(r4);	 Catch:{ Exception -> 0x0c48 }
        r6 = 0;	 Catch:{ Exception -> 0x0c48 }
        r0 = r76;	 Catch:{ Exception -> 0x0c48 }
        r4.cleanDraft(r0, r6);	 Catch:{ Exception -> 0x0c48 }
    L_0x0c35:
        r28 = r29;	 Catch:{ Exception -> 0x0c48 }
        goto L_0x0006;	 Catch:{ Exception -> 0x0c48 }
    L_0x0c39:
        r7 = new org.telegram.tgnet.TLRPC$TL_decryptedMessage_layer45;	 Catch:{ Exception -> 0x0c48 }
        r7.<init>();	 Catch:{ Exception -> 0x0c48 }
        goto L_0x0b9f;	 Catch:{ Exception -> 0x0c48 }
    L_0x0c40:
        r4 = new org.telegram.tgnet.TLRPC$TL_decryptedMessageMediaEmpty;	 Catch:{ Exception -> 0x0c48 }
        r4.<init>();	 Catch:{ Exception -> 0x0c48 }
        r7.media = r4;	 Catch:{ Exception -> 0x0c48 }
        goto L_0x0c14;	 Catch:{ Exception -> 0x0c48 }
    L_0x0c48:
        r31 = move-exception;	 Catch:{ Exception -> 0x0c48 }
        r28 = r29;	 Catch:{ Exception -> 0x0c48 }
        goto L_0x0216;	 Catch:{ Exception -> 0x0c48 }
    L_0x0c4d:
        r4 = 1;	 Catch:{ Exception -> 0x0c48 }
        r0 = r63;	 Catch:{ Exception -> 0x0c48 }
        if (r0 < r4) goto L_0x0c57;	 Catch:{ Exception -> 0x0c48 }
    L_0x0c52:
        r4 = 3;	 Catch:{ Exception -> 0x0c48 }
        r0 = r63;	 Catch:{ Exception -> 0x0c48 }
        if (r0 <= r4) goto L_0x0c6a;	 Catch:{ Exception -> 0x0c48 }
    L_0x0c57:
        r4 = 5;	 Catch:{ Exception -> 0x0c48 }
        r0 = r63;	 Catch:{ Exception -> 0x0c48 }
        if (r0 < r4) goto L_0x0c62;	 Catch:{ Exception -> 0x0c48 }
    L_0x0c5c:
        r4 = 8;	 Catch:{ Exception -> 0x0c48 }
        r0 = r63;	 Catch:{ Exception -> 0x0c48 }
        if (r0 <= r4) goto L_0x0c6a;	 Catch:{ Exception -> 0x0c48 }
    L_0x0c62:
        r4 = 9;	 Catch:{ Exception -> 0x0c48 }
        r0 = r63;	 Catch:{ Exception -> 0x0c48 }
        if (r0 != r4) goto L_0x18a8;	 Catch:{ Exception -> 0x0c48 }
    L_0x0c68:
        if (r16 == 0) goto L_0x18a8;	 Catch:{ Exception -> 0x0c48 }
    L_0x0c6a:
        if (r16 != 0) goto L_0x125c;	 Catch:{ Exception -> 0x0c48 }
    L_0x0c6c:
        r36 = 0;	 Catch:{ Exception -> 0x0c48 }
        r4 = 1;	 Catch:{ Exception -> 0x0c48 }
        r0 = r63;	 Catch:{ Exception -> 0x0c48 }
        if (r0 != r4) goto L_0x0d0a;	 Catch:{ Exception -> 0x0c48 }
    L_0x0c73:
        r0 = r70;	 Catch:{ Exception -> 0x0c48 }
        r4 = r0 instanceof org.telegram.tgnet.TLRPC.TL_messageMediaVenue;	 Catch:{ Exception -> 0x0c48 }
        if (r4 == 0) goto L_0x0cf0;	 Catch:{ Exception -> 0x0c48 }
    L_0x0c79:
        r36 = new org.telegram.tgnet.TLRPC$TL_inputMediaVenue;	 Catch:{ Exception -> 0x0c48 }
        r36.<init>();	 Catch:{ Exception -> 0x0c48 }
        r0 = r70;	 Catch:{ Exception -> 0x0c48 }
        r4 = r0.address;	 Catch:{ Exception -> 0x0c48 }
        r0 = r36;	 Catch:{ Exception -> 0x0c48 }
        r0.address = r4;	 Catch:{ Exception -> 0x0c48 }
        r0 = r70;	 Catch:{ Exception -> 0x0c48 }
        r4 = r0.title;	 Catch:{ Exception -> 0x0c48 }
        r0 = r36;	 Catch:{ Exception -> 0x0c48 }
        r0.title = r4;	 Catch:{ Exception -> 0x0c48 }
        r0 = r70;	 Catch:{ Exception -> 0x0c48 }
        r4 = r0.provider;	 Catch:{ Exception -> 0x0c48 }
        r0 = r36;	 Catch:{ Exception -> 0x0c48 }
        r0.provider = r4;	 Catch:{ Exception -> 0x0c48 }
        r0 = r70;	 Catch:{ Exception -> 0x0c48 }
        r4 = r0.venue_id;	 Catch:{ Exception -> 0x0c48 }
        r0 = r36;	 Catch:{ Exception -> 0x0c48 }
        r0.venue_id = r4;	 Catch:{ Exception -> 0x0c48 }
        r4 = "";	 Catch:{ Exception -> 0x0c48 }
        r0 = r36;	 Catch:{ Exception -> 0x0c48 }
        r0.venue_type = r4;	 Catch:{ Exception -> 0x0c48 }
    L_0x0ca5:
        r4 = new org.telegram.tgnet.TLRPC$TL_inputGeoPoint;	 Catch:{ Exception -> 0x0c48 }
        r4.<init>();	 Catch:{ Exception -> 0x0c48 }
        r0 = r36;	 Catch:{ Exception -> 0x0c48 }
        r0.geo_point = r4;	 Catch:{ Exception -> 0x0c48 }
        r0 = r36;	 Catch:{ Exception -> 0x0c48 }
        r4 = r0.geo_point;	 Catch:{ Exception -> 0x0c48 }
        r0 = r70;	 Catch:{ Exception -> 0x0c48 }
        r6 = r0.geo;	 Catch:{ Exception -> 0x0c48 }
        r8 = r6.lat;	 Catch:{ Exception -> 0x0c48 }
        r4.lat = r8;	 Catch:{ Exception -> 0x0c48 }
        r0 = r36;	 Catch:{ Exception -> 0x0c48 }
        r4 = r0.geo_point;	 Catch:{ Exception -> 0x0c48 }
        r0 = r70;	 Catch:{ Exception -> 0x0c48 }
        r6 = r0.geo;	 Catch:{ Exception -> 0x0c48 }
        r8 = r6._long;	 Catch:{ Exception -> 0x0c48 }
        r4._long = r8;	 Catch:{ Exception -> 0x0c48 }
        r28 = r29;
    L_0x0cc8:
        if (r58 == 0) goto L_0x106d;
    L_0x0cca:
        r56 = new org.telegram.tgnet.TLRPC$TL_messages_sendBroadcast;	 Catch:{ Exception -> 0x0dc0 }
        r56.<init>();	 Catch:{ Exception -> 0x0dc0 }
        r55 = new java.util.ArrayList;	 Catch:{ Exception -> 0x0dc0 }
        r55.<init>();	 Catch:{ Exception -> 0x0dc0 }
        r20 = 0;	 Catch:{ Exception -> 0x0dc0 }
    L_0x0cd6:
        r4 = r58.size();	 Catch:{ Exception -> 0x0dc0 }
        r0 = r20;	 Catch:{ Exception -> 0x0dc0 }
        if (r0 >= r4) goto L_0x102b;	 Catch:{ Exception -> 0x0dc0 }
    L_0x0cde:
        r4 = org.telegram.messenger.Utilities.random;	 Catch:{ Exception -> 0x0dc0 }
        r8 = r4.nextLong();	 Catch:{ Exception -> 0x0dc0 }
        r4 = java.lang.Long.valueOf(r8);	 Catch:{ Exception -> 0x0dc0 }
        r0 = r55;	 Catch:{ Exception -> 0x0dc0 }
        r0.add(r4);	 Catch:{ Exception -> 0x0dc0 }
        r20 = r20 + 1;
        goto L_0x0cd6;
    L_0x0cf0:
        r0 = r70;	 Catch:{ Exception -> 0x0c48 }
        r4 = r0 instanceof org.telegram.tgnet.TLRPC.TL_messageMediaGeoLive;	 Catch:{ Exception -> 0x0c48 }
        if (r4 == 0) goto L_0x0d04;	 Catch:{ Exception -> 0x0c48 }
    L_0x0cf6:
        r36 = new org.telegram.tgnet.TLRPC$TL_inputMediaGeoLive;	 Catch:{ Exception -> 0x0c48 }
        r36.<init>();	 Catch:{ Exception -> 0x0c48 }
        r0 = r70;	 Catch:{ Exception -> 0x0c48 }
        r4 = r0.period;	 Catch:{ Exception -> 0x0c48 }
        r0 = r36;	 Catch:{ Exception -> 0x0c48 }
        r0.period = r4;	 Catch:{ Exception -> 0x0c48 }
        goto L_0x0ca5;	 Catch:{ Exception -> 0x0c48 }
    L_0x0d04:
        r36 = new org.telegram.tgnet.TLRPC$TL_inputMediaGeoPoint;	 Catch:{ Exception -> 0x0c48 }
        r36.<init>();	 Catch:{ Exception -> 0x0c48 }
        goto L_0x0ca5;	 Catch:{ Exception -> 0x0c48 }
    L_0x0d0a:
        r4 = 2;	 Catch:{ Exception -> 0x0c48 }
        r0 = r63;	 Catch:{ Exception -> 0x0c48 }
        if (r0 == r4) goto L_0x0d17;	 Catch:{ Exception -> 0x0c48 }
    L_0x0d0f:
        r4 = 9;	 Catch:{ Exception -> 0x0c48 }
        r0 = r63;	 Catch:{ Exception -> 0x0c48 }
        if (r0 != r4) goto L_0x0e07;	 Catch:{ Exception -> 0x0c48 }
    L_0x0d15:
        if (r71 == 0) goto L_0x0e07;	 Catch:{ Exception -> 0x0c48 }
    L_0x0d17:
        r0 = r71;	 Catch:{ Exception -> 0x0c48 }
        r8 = r0.access_hash;	 Catch:{ Exception -> 0x0c48 }
        r14 = 0;	 Catch:{ Exception -> 0x0c48 }
        r4 = (r8 > r14 ? 1 : (r8 == r14 ? 0 : -1));	 Catch:{ Exception -> 0x0c48 }
        if (r4 != 0) goto L_0x0ddf;	 Catch:{ Exception -> 0x0c48 }
    L_0x0d21:
        r36 = new org.telegram.tgnet.TLRPC$TL_inputMediaUploadedPhoto;	 Catch:{ Exception -> 0x0c48 }
        r36.<init>();	 Catch:{ Exception -> 0x0c48 }
        if (r86 == 0) goto L_0x0d3e;	 Catch:{ Exception -> 0x0c48 }
    L_0x0d28:
        r0 = r86;	 Catch:{ Exception -> 0x0c48 }
        r1 = r36;	 Catch:{ Exception -> 0x0c48 }
        r1.ttl_seconds = r0;	 Catch:{ Exception -> 0x0c48 }
        r0 = r86;	 Catch:{ Exception -> 0x0c48 }
        r1 = r46;	 Catch:{ Exception -> 0x0c48 }
        r1.ttl = r0;	 Catch:{ Exception -> 0x0c48 }
        r0 = r36;	 Catch:{ Exception -> 0x0c48 }
        r4 = r0.flags;	 Catch:{ Exception -> 0x0c48 }
        r4 = r4 | 2;	 Catch:{ Exception -> 0x0c48 }
        r0 = r36;	 Catch:{ Exception -> 0x0c48 }
        r0.flags = r4;	 Catch:{ Exception -> 0x0c48 }
    L_0x0d3e:
        if (r85 == 0) goto L_0x0d89;	 Catch:{ Exception -> 0x0c48 }
    L_0x0d40:
        r4 = "masks";	 Catch:{ Exception -> 0x0c48 }
        r0 = r85;	 Catch:{ Exception -> 0x0c48 }
        r43 = r0.get(r4);	 Catch:{ Exception -> 0x0c48 }
        r43 = (java.lang.String) r43;	 Catch:{ Exception -> 0x0c48 }
        if (r43 == 0) goto L_0x0d89;	 Catch:{ Exception -> 0x0c48 }
    L_0x0d4d:
        r61 = new org.telegram.tgnet.SerializedData;	 Catch:{ Exception -> 0x0c48 }
        r4 = org.telegram.messenger.Utilities.hexToBytes(r43);	 Catch:{ Exception -> 0x0c48 }
        r0 = r61;	 Catch:{ Exception -> 0x0c48 }
        r0.<init>(r4);	 Catch:{ Exception -> 0x0c48 }
        r4 = 0;	 Catch:{ Exception -> 0x0c48 }
        r0 = r61;	 Catch:{ Exception -> 0x0c48 }
        r27 = r0.readInt32(r4);	 Catch:{ Exception -> 0x0c48 }
        r20 = 0;	 Catch:{ Exception -> 0x0c48 }
    L_0x0d61:
        r0 = r20;	 Catch:{ Exception -> 0x0c48 }
        r1 = r27;	 Catch:{ Exception -> 0x0c48 }
        if (r0 >= r1) goto L_0x0d7f;	 Catch:{ Exception -> 0x0c48 }
    L_0x0d67:
        r0 = r36;	 Catch:{ Exception -> 0x0c48 }
        r4 = r0.stickers;	 Catch:{ Exception -> 0x0c48 }
        r6 = 0;	 Catch:{ Exception -> 0x0c48 }
        r0 = r61;	 Catch:{ Exception -> 0x0c48 }
        r6 = r0.readInt32(r6);	 Catch:{ Exception -> 0x0c48 }
        r8 = 0;	 Catch:{ Exception -> 0x0c48 }
        r0 = r61;	 Catch:{ Exception -> 0x0c48 }
        r6 = org.telegram.tgnet.TLRPC.InputDocument.TLdeserialize(r0, r6, r8);	 Catch:{ Exception -> 0x0c48 }
        r4.add(r6);	 Catch:{ Exception -> 0x0c48 }
        r20 = r20 + 1;	 Catch:{ Exception -> 0x0c48 }
        goto L_0x0d61;	 Catch:{ Exception -> 0x0c48 }
    L_0x0d7f:
        r0 = r36;	 Catch:{ Exception -> 0x0c48 }
        r4 = r0.flags;	 Catch:{ Exception -> 0x0c48 }
        r4 = r4 | 1;	 Catch:{ Exception -> 0x0c48 }
        r0 = r36;	 Catch:{ Exception -> 0x0c48 }
        r0.flags = r4;	 Catch:{ Exception -> 0x0c48 }
    L_0x0d89:
        if (r29 != 0) goto L_0x1a3d;	 Catch:{ Exception -> 0x0c48 }
    L_0x0d8b:
        r28 = new org.telegram.messenger.SendMessagesHelper$DelayedMessage;	 Catch:{ Exception -> 0x0c48 }
        r0 = r28;	 Catch:{ Exception -> 0x0c48 }
        r1 = r67;	 Catch:{ Exception -> 0x0c48 }
        r2 = r76;	 Catch:{ Exception -> 0x0c48 }
        r0.<init>(r2);	 Catch:{ Exception -> 0x0c48 }
        r4 = 0;
        r0 = r28;	 Catch:{ Exception -> 0x0dc0 }
        r0.type = r4;	 Catch:{ Exception -> 0x0dc0 }
        r0 = r28;	 Catch:{ Exception -> 0x0dc0 }
        r0.obj = r12;	 Catch:{ Exception -> 0x0dc0 }
        r0 = r51;	 Catch:{ Exception -> 0x0dc0 }
        r1 = r28;	 Catch:{ Exception -> 0x0dc0 }
        r1.originalPath = r0;	 Catch:{ Exception -> 0x0dc0 }
    L_0x0da5:
        if (r78 == 0) goto L_0x0dc3;	 Catch:{ Exception -> 0x0dc0 }
    L_0x0da7:
        r4 = r78.length();	 Catch:{ Exception -> 0x0dc0 }
        if (r4 <= 0) goto L_0x0dc3;	 Catch:{ Exception -> 0x0dc0 }
    L_0x0dad:
        r4 = "http";	 Catch:{ Exception -> 0x0dc0 }
        r0 = r78;	 Catch:{ Exception -> 0x0dc0 }
        r4 = r0.startsWith(r4);	 Catch:{ Exception -> 0x0dc0 }
        if (r4 == 0) goto L_0x0dc3;	 Catch:{ Exception -> 0x0dc0 }
    L_0x0db8:
        r0 = r78;	 Catch:{ Exception -> 0x0dc0 }
        r1 = r28;	 Catch:{ Exception -> 0x0dc0 }
        r1.httpLocation = r0;	 Catch:{ Exception -> 0x0dc0 }
        goto L_0x0cc8;	 Catch:{ Exception -> 0x0dc0 }
    L_0x0dc0:
        r31 = move-exception;	 Catch:{ Exception -> 0x0dc0 }
        goto L_0x0216;	 Catch:{ Exception -> 0x0dc0 }
    L_0x0dc3:
        r0 = r71;	 Catch:{ Exception -> 0x0dc0 }
        r4 = r0.sizes;	 Catch:{ Exception -> 0x0dc0 }
        r0 = r71;	 Catch:{ Exception -> 0x0dc0 }
        r6 = r0.sizes;	 Catch:{ Exception -> 0x0dc0 }
        r6 = r6.size();	 Catch:{ Exception -> 0x0dc0 }
        r6 = r6 + -1;	 Catch:{ Exception -> 0x0dc0 }
        r4 = r4.get(r6);	 Catch:{ Exception -> 0x0dc0 }
        r4 = (org.telegram.tgnet.TLRPC.PhotoSize) r4;	 Catch:{ Exception -> 0x0dc0 }
        r4 = r4.location;	 Catch:{ Exception -> 0x0dc0 }
        r0 = r28;	 Catch:{ Exception -> 0x0dc0 }
        r0.location = r4;	 Catch:{ Exception -> 0x0dc0 }
        goto L_0x0cc8;
    L_0x0ddf:
        r44 = new org.telegram.tgnet.TLRPC$TL_inputMediaPhoto;	 Catch:{ Exception -> 0x0c48 }
        r44.<init>();	 Catch:{ Exception -> 0x0c48 }
        r4 = new org.telegram.tgnet.TLRPC$TL_inputPhoto;	 Catch:{ Exception -> 0x0c48 }
        r4.<init>();	 Catch:{ Exception -> 0x0c48 }
        r0 = r44;	 Catch:{ Exception -> 0x0c48 }
        r0.id = r4;	 Catch:{ Exception -> 0x0c48 }
        r0 = r44;	 Catch:{ Exception -> 0x0c48 }
        r4 = r0.id;	 Catch:{ Exception -> 0x0c48 }
        r0 = r71;	 Catch:{ Exception -> 0x0c48 }
        r8 = r0.id;	 Catch:{ Exception -> 0x0c48 }
        r4.id = r8;	 Catch:{ Exception -> 0x0c48 }
        r0 = r44;	 Catch:{ Exception -> 0x0c48 }
        r4 = r0.id;	 Catch:{ Exception -> 0x0c48 }
        r0 = r71;	 Catch:{ Exception -> 0x0c48 }
        r8 = r0.access_hash;	 Catch:{ Exception -> 0x0c48 }
        r4.access_hash = r8;	 Catch:{ Exception -> 0x0c48 }
        r36 = r44;	 Catch:{ Exception -> 0x0c48 }
        r28 = r29;	 Catch:{ Exception -> 0x0c48 }
        goto L_0x0cc8;	 Catch:{ Exception -> 0x0c48 }
    L_0x0e07:
        r4 = 3;	 Catch:{ Exception -> 0x0c48 }
        r0 = r63;	 Catch:{ Exception -> 0x0c48 }
        if (r0 != r4) goto L_0x0eb2;	 Catch:{ Exception -> 0x0c48 }
    L_0x0e0c:
        r0 = r74;	 Catch:{ Exception -> 0x0c48 }
        r8 = r0.access_hash;	 Catch:{ Exception -> 0x0c48 }
        r14 = 0;	 Catch:{ Exception -> 0x0c48 }
        r4 = (r8 > r14 ? 1 : (r8 == r14 ? 0 : -1));	 Catch:{ Exception -> 0x0c48 }
        if (r4 != 0) goto L_0x0e8a;	 Catch:{ Exception -> 0x0c48 }
    L_0x0e16:
        r36 = new org.telegram.tgnet.TLRPC$TL_inputMediaUploadedDocument;	 Catch:{ Exception -> 0x0c48 }
        r36.<init>();	 Catch:{ Exception -> 0x0c48 }
        r0 = r74;	 Catch:{ Exception -> 0x0c48 }
        r4 = r0.mime_type;	 Catch:{ Exception -> 0x0c48 }
        r0 = r36;	 Catch:{ Exception -> 0x0c48 }
        r0.mime_type = r4;	 Catch:{ Exception -> 0x0c48 }
        r0 = r74;	 Catch:{ Exception -> 0x0c48 }
        r4 = r0.attributes;	 Catch:{ Exception -> 0x0c48 }
        r0 = r36;	 Catch:{ Exception -> 0x0c48 }
        r0.attributes = r4;	 Catch:{ Exception -> 0x0c48 }
        r4 = org.telegram.messenger.MessageObject.isRoundVideoDocument(r74);	 Catch:{ Exception -> 0x0c48 }
        if (r4 != 0) goto L_0x0e44;	 Catch:{ Exception -> 0x0c48 }
    L_0x0e31:
        if (r72 == 0) goto L_0x0e3f;	 Catch:{ Exception -> 0x0c48 }
    L_0x0e33:
        r0 = r72;	 Catch:{ Exception -> 0x0c48 }
        r4 = r0.muted;	 Catch:{ Exception -> 0x0c48 }
        if (r4 != 0) goto L_0x0e44;	 Catch:{ Exception -> 0x0c48 }
    L_0x0e39:
        r0 = r72;	 Catch:{ Exception -> 0x0c48 }
        r4 = r0.roundVideo;	 Catch:{ Exception -> 0x0c48 }
        if (r4 != 0) goto L_0x0e44;	 Catch:{ Exception -> 0x0c48 }
    L_0x0e3f:
        r4 = 1;	 Catch:{ Exception -> 0x0c48 }
        r0 = r36;	 Catch:{ Exception -> 0x0c48 }
        r0.nosound_video = r4;	 Catch:{ Exception -> 0x0c48 }
    L_0x0e44:
        if (r86 == 0) goto L_0x0e5c;	 Catch:{ Exception -> 0x0c48 }
    L_0x0e46:
        r0 = r86;	 Catch:{ Exception -> 0x0c48 }
        r1 = r36;	 Catch:{ Exception -> 0x0c48 }
        r1.ttl_seconds = r0;	 Catch:{ Exception -> 0x0c48 }
        r0 = r86;	 Catch:{ Exception -> 0x0c48 }
        r1 = r46;	 Catch:{ Exception -> 0x0c48 }
        r1.ttl = r0;	 Catch:{ Exception -> 0x0c48 }
        r0 = r36;	 Catch:{ Exception -> 0x0c48 }
        r4 = r0.flags;	 Catch:{ Exception -> 0x0c48 }
        r4 = r4 | 2;	 Catch:{ Exception -> 0x0c48 }
        r0 = r36;	 Catch:{ Exception -> 0x0c48 }
        r0.flags = r4;	 Catch:{ Exception -> 0x0c48 }
    L_0x0e5c:
        if (r29 != 0) goto L_0x1a39;	 Catch:{ Exception -> 0x0c48 }
    L_0x0e5e:
        r28 = new org.telegram.messenger.SendMessagesHelper$DelayedMessage;	 Catch:{ Exception -> 0x0c48 }
        r0 = r28;	 Catch:{ Exception -> 0x0c48 }
        r1 = r67;	 Catch:{ Exception -> 0x0c48 }
        r2 = r76;	 Catch:{ Exception -> 0x0c48 }
        r0.<init>(r2);	 Catch:{ Exception -> 0x0c48 }
        r4 = 1;
        r0 = r28;	 Catch:{ Exception -> 0x0dc0 }
        r0.type = r4;	 Catch:{ Exception -> 0x0dc0 }
        r0 = r28;	 Catch:{ Exception -> 0x0dc0 }
        r0.obj = r12;	 Catch:{ Exception -> 0x0dc0 }
        r0 = r51;	 Catch:{ Exception -> 0x0dc0 }
        r1 = r28;	 Catch:{ Exception -> 0x0dc0 }
        r1.originalPath = r0;	 Catch:{ Exception -> 0x0dc0 }
    L_0x0e78:
        r0 = r74;	 Catch:{ Exception -> 0x0dc0 }
        r4 = r0.thumb;	 Catch:{ Exception -> 0x0dc0 }
        r4 = r4.location;	 Catch:{ Exception -> 0x0dc0 }
        r0 = r28;	 Catch:{ Exception -> 0x0dc0 }
        r0.location = r4;	 Catch:{ Exception -> 0x0dc0 }
        r0 = r72;	 Catch:{ Exception -> 0x0dc0 }
        r1 = r28;	 Catch:{ Exception -> 0x0dc0 }
        r1.videoEditedInfo = r0;	 Catch:{ Exception -> 0x0dc0 }
        goto L_0x0cc8;
    L_0x0e8a:
        r44 = new org.telegram.tgnet.TLRPC$TL_inputMediaDocument;	 Catch:{ Exception -> 0x0c48 }
        r44.<init>();	 Catch:{ Exception -> 0x0c48 }
        r4 = new org.telegram.tgnet.TLRPC$TL_inputDocument;	 Catch:{ Exception -> 0x0c48 }
        r4.<init>();	 Catch:{ Exception -> 0x0c48 }
        r0 = r44;	 Catch:{ Exception -> 0x0c48 }
        r0.id = r4;	 Catch:{ Exception -> 0x0c48 }
        r0 = r44;	 Catch:{ Exception -> 0x0c48 }
        r4 = r0.id;	 Catch:{ Exception -> 0x0c48 }
        r0 = r74;	 Catch:{ Exception -> 0x0c48 }
        r8 = r0.id;	 Catch:{ Exception -> 0x0c48 }
        r4.id = r8;	 Catch:{ Exception -> 0x0c48 }
        r0 = r44;	 Catch:{ Exception -> 0x0c48 }
        r4 = r0.id;	 Catch:{ Exception -> 0x0c48 }
        r0 = r74;	 Catch:{ Exception -> 0x0c48 }
        r8 = r0.access_hash;	 Catch:{ Exception -> 0x0c48 }
        r4.access_hash = r8;	 Catch:{ Exception -> 0x0c48 }
        r36 = r44;	 Catch:{ Exception -> 0x0c48 }
        r28 = r29;	 Catch:{ Exception -> 0x0c48 }
        goto L_0x0cc8;	 Catch:{ Exception -> 0x0c48 }
    L_0x0eb2:
        r4 = 6;	 Catch:{ Exception -> 0x0c48 }
        r0 = r63;	 Catch:{ Exception -> 0x0c48 }
        if (r0 != r4) goto L_0x0ed8;	 Catch:{ Exception -> 0x0c48 }
    L_0x0eb7:
        r36 = new org.telegram.tgnet.TLRPC$TL_inputMediaContact;	 Catch:{ Exception -> 0x0c48 }
        r36.<init>();	 Catch:{ Exception -> 0x0c48 }
        r0 = r73;	 Catch:{ Exception -> 0x0c48 }
        r4 = r0.phone;	 Catch:{ Exception -> 0x0c48 }
        r0 = r36;	 Catch:{ Exception -> 0x0c48 }
        r0.phone_number = r4;	 Catch:{ Exception -> 0x0c48 }
        r0 = r73;	 Catch:{ Exception -> 0x0c48 }
        r4 = r0.first_name;	 Catch:{ Exception -> 0x0c48 }
        r0 = r36;	 Catch:{ Exception -> 0x0c48 }
        r0.first_name = r4;	 Catch:{ Exception -> 0x0c48 }
        r0 = r73;	 Catch:{ Exception -> 0x0c48 }
        r4 = r0.last_name;	 Catch:{ Exception -> 0x0c48 }
        r0 = r36;	 Catch:{ Exception -> 0x0c48 }
        r0.last_name = r4;	 Catch:{ Exception -> 0x0c48 }
        r28 = r29;	 Catch:{ Exception -> 0x0c48 }
        goto L_0x0cc8;	 Catch:{ Exception -> 0x0c48 }
    L_0x0ed8:
        r4 = 7;	 Catch:{ Exception -> 0x0c48 }
        r0 = r63;	 Catch:{ Exception -> 0x0c48 }
        if (r0 == r4) goto L_0x0ee3;	 Catch:{ Exception -> 0x0c48 }
    L_0x0edd:
        r4 = 9;	 Catch:{ Exception -> 0x0c48 }
        r0 = r63;	 Catch:{ Exception -> 0x0c48 }
        if (r0 != r4) goto L_0x0fb0;	 Catch:{ Exception -> 0x0c48 }
    L_0x0ee3:
        r0 = r74;	 Catch:{ Exception -> 0x0c48 }
        r8 = r0.access_hash;	 Catch:{ Exception -> 0x0c48 }
        r14 = 0;	 Catch:{ Exception -> 0x0c48 }
        r4 = (r8 > r14 ? 1 : (r8 == r14 ? 0 : -1));	 Catch:{ Exception -> 0x0c48 }
        if (r4 != 0) goto L_0x0f88;	 Catch:{ Exception -> 0x0c48 }
    L_0x0eed:
        if (r16 != 0) goto L_0x0f46;	 Catch:{ Exception -> 0x0c48 }
    L_0x0eef:
        if (r51 == 0) goto L_0x0f46;	 Catch:{ Exception -> 0x0c48 }
    L_0x0ef1:
        r4 = r51.length();	 Catch:{ Exception -> 0x0c48 }
        if (r4 <= 0) goto L_0x0f46;	 Catch:{ Exception -> 0x0c48 }
    L_0x0ef7:
        r4 = "http";	 Catch:{ Exception -> 0x0c48 }
        r0 = r51;	 Catch:{ Exception -> 0x0c48 }
        r4 = r0.startsWith(r4);	 Catch:{ Exception -> 0x0c48 }
        if (r4 == 0) goto L_0x0f46;	 Catch:{ Exception -> 0x0c48 }
    L_0x0f02:
        if (r85 == 0) goto L_0x0f46;	 Catch:{ Exception -> 0x0c48 }
    L_0x0f04:
        r36 = new org.telegram.tgnet.TLRPC$TL_inputMediaGifExternal;	 Catch:{ Exception -> 0x0c48 }
        r36.<init>();	 Catch:{ Exception -> 0x0c48 }
        r4 = "url";	 Catch:{ Exception -> 0x0c48 }
        r0 = r85;	 Catch:{ Exception -> 0x0c48 }
        r4 = r0.get(r4);	 Catch:{ Exception -> 0x0c48 }
        r4 = (java.lang.String) r4;	 Catch:{ Exception -> 0x0c48 }
        r6 = "\\|";	 Catch:{ Exception -> 0x0c48 }
        r21 = r4.split(r6);	 Catch:{ Exception -> 0x0c48 }
        r0 = r21;	 Catch:{ Exception -> 0x0c48 }
        r4 = r0.length;	 Catch:{ Exception -> 0x0c48 }
        r6 = 2;	 Catch:{ Exception -> 0x0c48 }
        if (r4 != r6) goto L_0x0f32;	 Catch:{ Exception -> 0x0c48 }
    L_0x0f21:
        r0 = r36;	 Catch:{ Exception -> 0x0c48 }
        r0 = (org.telegram.tgnet.TLRPC.TL_inputMediaGifExternal) r0;	 Catch:{ Exception -> 0x0c48 }
        r4 = r0;	 Catch:{ Exception -> 0x0c48 }
        r6 = 0;	 Catch:{ Exception -> 0x0c48 }
        r6 = r21[r6];	 Catch:{ Exception -> 0x0c48 }
        r4.url = r6;	 Catch:{ Exception -> 0x0c48 }
        r4 = 1;	 Catch:{ Exception -> 0x0c48 }
        r4 = r21[r4];	 Catch:{ Exception -> 0x0c48 }
        r0 = r36;	 Catch:{ Exception -> 0x0c48 }
        r0.f21q = r4;	 Catch:{ Exception -> 0x0c48 }
    L_0x0f32:
        r28 = r29;
    L_0x0f34:
        r0 = r74;	 Catch:{ Exception -> 0x0dc0 }
        r4 = r0.mime_type;	 Catch:{ Exception -> 0x0dc0 }
        r0 = r36;	 Catch:{ Exception -> 0x0dc0 }
        r0.mime_type = r4;	 Catch:{ Exception -> 0x0dc0 }
        r0 = r74;	 Catch:{ Exception -> 0x0dc0 }
        r4 = r0.attributes;	 Catch:{ Exception -> 0x0dc0 }
        r0 = r36;	 Catch:{ Exception -> 0x0dc0 }
        r0.attributes = r4;	 Catch:{ Exception -> 0x0dc0 }
        goto L_0x0cc8;
    L_0x0f46:
        r36 = new org.telegram.tgnet.TLRPC$TL_inputMediaUploadedDocument;	 Catch:{ Exception -> 0x0c48 }
        r36.<init>();	 Catch:{ Exception -> 0x0c48 }
        if (r86 == 0) goto L_0x0f63;	 Catch:{ Exception -> 0x0c48 }
    L_0x0f4d:
        r0 = r86;	 Catch:{ Exception -> 0x0c48 }
        r1 = r36;	 Catch:{ Exception -> 0x0c48 }
        r1.ttl_seconds = r0;	 Catch:{ Exception -> 0x0c48 }
        r0 = r86;	 Catch:{ Exception -> 0x0c48 }
        r1 = r46;	 Catch:{ Exception -> 0x0c48 }
        r1.ttl = r0;	 Catch:{ Exception -> 0x0c48 }
        r0 = r36;	 Catch:{ Exception -> 0x0c48 }
        r4 = r0.flags;	 Catch:{ Exception -> 0x0c48 }
        r4 = r4 | 2;	 Catch:{ Exception -> 0x0c48 }
        r0 = r36;	 Catch:{ Exception -> 0x0c48 }
        r0.flags = r4;	 Catch:{ Exception -> 0x0c48 }
    L_0x0f63:
        r28 = new org.telegram.messenger.SendMessagesHelper$DelayedMessage;	 Catch:{ Exception -> 0x0c48 }
        r0 = r28;	 Catch:{ Exception -> 0x0c48 }
        r1 = r67;	 Catch:{ Exception -> 0x0c48 }
        r2 = r76;	 Catch:{ Exception -> 0x0c48 }
        r0.<init>(r2);	 Catch:{ Exception -> 0x0c48 }
        r0 = r51;	 Catch:{ Exception -> 0x0dc0 }
        r1 = r28;	 Catch:{ Exception -> 0x0dc0 }
        r1.originalPath = r0;	 Catch:{ Exception -> 0x0dc0 }
        r4 = 2;	 Catch:{ Exception -> 0x0dc0 }
        r0 = r28;	 Catch:{ Exception -> 0x0dc0 }
        r0.type = r4;	 Catch:{ Exception -> 0x0dc0 }
        r0 = r28;	 Catch:{ Exception -> 0x0dc0 }
        r0.obj = r12;	 Catch:{ Exception -> 0x0dc0 }
        r0 = r74;	 Catch:{ Exception -> 0x0dc0 }
        r4 = r0.thumb;	 Catch:{ Exception -> 0x0dc0 }
        r4 = r4.location;	 Catch:{ Exception -> 0x0dc0 }
        r0 = r28;	 Catch:{ Exception -> 0x0dc0 }
        r0.location = r4;	 Catch:{ Exception -> 0x0dc0 }
        goto L_0x0f34;
    L_0x0f88:
        r44 = new org.telegram.tgnet.TLRPC$TL_inputMediaDocument;	 Catch:{ Exception -> 0x0c48 }
        r44.<init>();	 Catch:{ Exception -> 0x0c48 }
        r4 = new org.telegram.tgnet.TLRPC$TL_inputDocument;	 Catch:{ Exception -> 0x0c48 }
        r4.<init>();	 Catch:{ Exception -> 0x0c48 }
        r0 = r44;	 Catch:{ Exception -> 0x0c48 }
        r0.id = r4;	 Catch:{ Exception -> 0x0c48 }
        r0 = r44;	 Catch:{ Exception -> 0x0c48 }
        r4 = r0.id;	 Catch:{ Exception -> 0x0c48 }
        r0 = r74;	 Catch:{ Exception -> 0x0c48 }
        r8 = r0.id;	 Catch:{ Exception -> 0x0c48 }
        r4.id = r8;	 Catch:{ Exception -> 0x0c48 }
        r0 = r44;	 Catch:{ Exception -> 0x0c48 }
        r4 = r0.id;	 Catch:{ Exception -> 0x0c48 }
        r0 = r74;	 Catch:{ Exception -> 0x0c48 }
        r8 = r0.access_hash;	 Catch:{ Exception -> 0x0c48 }
        r4.access_hash = r8;	 Catch:{ Exception -> 0x0c48 }
        r36 = r44;	 Catch:{ Exception -> 0x0c48 }
        r28 = r29;	 Catch:{ Exception -> 0x0c48 }
        goto L_0x0cc8;	 Catch:{ Exception -> 0x0c48 }
    L_0x0fb0:
        r4 = 8;	 Catch:{ Exception -> 0x0c48 }
        r0 = r63;	 Catch:{ Exception -> 0x0c48 }
        if (r0 != r4) goto L_0x1a35;	 Catch:{ Exception -> 0x0c48 }
    L_0x0fb6:
        r0 = r74;	 Catch:{ Exception -> 0x0c48 }
        r8 = r0.access_hash;	 Catch:{ Exception -> 0x0c48 }
        r14 = 0;	 Catch:{ Exception -> 0x0c48 }
        r4 = (r8 > r14 ? 1 : (r8 == r14 ? 0 : -1));	 Catch:{ Exception -> 0x0c48 }
        if (r4 != 0) goto L_0x1003;	 Catch:{ Exception -> 0x0c48 }
    L_0x0fc0:
        r36 = new org.telegram.tgnet.TLRPC$TL_inputMediaUploadedDocument;	 Catch:{ Exception -> 0x0c48 }
        r36.<init>();	 Catch:{ Exception -> 0x0c48 }
        r0 = r74;	 Catch:{ Exception -> 0x0c48 }
        r4 = r0.mime_type;	 Catch:{ Exception -> 0x0c48 }
        r0 = r36;	 Catch:{ Exception -> 0x0c48 }
        r0.mime_type = r4;	 Catch:{ Exception -> 0x0c48 }
        r0 = r74;	 Catch:{ Exception -> 0x0c48 }
        r4 = r0.attributes;	 Catch:{ Exception -> 0x0c48 }
        r0 = r36;	 Catch:{ Exception -> 0x0c48 }
        r0.attributes = r4;	 Catch:{ Exception -> 0x0c48 }
        if (r86 == 0) goto L_0x0fed;	 Catch:{ Exception -> 0x0c48 }
    L_0x0fd7:
        r0 = r86;	 Catch:{ Exception -> 0x0c48 }
        r1 = r36;	 Catch:{ Exception -> 0x0c48 }
        r1.ttl_seconds = r0;	 Catch:{ Exception -> 0x0c48 }
        r0 = r86;	 Catch:{ Exception -> 0x0c48 }
        r1 = r46;	 Catch:{ Exception -> 0x0c48 }
        r1.ttl = r0;	 Catch:{ Exception -> 0x0c48 }
        r0 = r36;	 Catch:{ Exception -> 0x0c48 }
        r4 = r0.flags;	 Catch:{ Exception -> 0x0c48 }
        r4 = r4 | 2;	 Catch:{ Exception -> 0x0c48 }
        r0 = r36;	 Catch:{ Exception -> 0x0c48 }
        r0.flags = r4;	 Catch:{ Exception -> 0x0c48 }
    L_0x0fed:
        r28 = new org.telegram.messenger.SendMessagesHelper$DelayedMessage;	 Catch:{ Exception -> 0x0c48 }
        r0 = r28;	 Catch:{ Exception -> 0x0c48 }
        r1 = r67;	 Catch:{ Exception -> 0x0c48 }
        r2 = r76;	 Catch:{ Exception -> 0x0c48 }
        r0.<init>(r2);	 Catch:{ Exception -> 0x0c48 }
        r4 = 3;
        r0 = r28;	 Catch:{ Exception -> 0x0dc0 }
        r0.type = r4;	 Catch:{ Exception -> 0x0dc0 }
        r0 = r28;	 Catch:{ Exception -> 0x0dc0 }
        r0.obj = r12;	 Catch:{ Exception -> 0x0dc0 }
        goto L_0x0cc8;
    L_0x1003:
        r44 = new org.telegram.tgnet.TLRPC$TL_inputMediaDocument;	 Catch:{ Exception -> 0x0c48 }
        r44.<init>();	 Catch:{ Exception -> 0x0c48 }
        r4 = new org.telegram.tgnet.TLRPC$TL_inputDocument;	 Catch:{ Exception -> 0x0c48 }
        r4.<init>();	 Catch:{ Exception -> 0x0c48 }
        r0 = r44;	 Catch:{ Exception -> 0x0c48 }
        r0.id = r4;	 Catch:{ Exception -> 0x0c48 }
        r0 = r44;	 Catch:{ Exception -> 0x0c48 }
        r4 = r0.id;	 Catch:{ Exception -> 0x0c48 }
        r0 = r74;	 Catch:{ Exception -> 0x0c48 }
        r8 = r0.id;	 Catch:{ Exception -> 0x0c48 }
        r4.id = r8;	 Catch:{ Exception -> 0x0c48 }
        r0 = r44;	 Catch:{ Exception -> 0x0c48 }
        r4 = r0.id;	 Catch:{ Exception -> 0x0c48 }
        r0 = r74;	 Catch:{ Exception -> 0x0c48 }
        r8 = r0.access_hash;	 Catch:{ Exception -> 0x0c48 }
        r4.access_hash = r8;	 Catch:{ Exception -> 0x0c48 }
        r36 = r44;
        r28 = r29;
        goto L_0x0cc8;
    L_0x102b:
        r0 = r58;	 Catch:{ Exception -> 0x0dc0 }
        r1 = r56;	 Catch:{ Exception -> 0x0dc0 }
        r1.contacts = r0;	 Catch:{ Exception -> 0x0dc0 }
        r0 = r36;	 Catch:{ Exception -> 0x0dc0 }
        r1 = r56;	 Catch:{ Exception -> 0x0dc0 }
        r1.media = r0;	 Catch:{ Exception -> 0x0dc0 }
        r0 = r55;	 Catch:{ Exception -> 0x0dc0 }
        r1 = r56;	 Catch:{ Exception -> 0x0dc0 }
        r1.random_id = r0;	 Catch:{ Exception -> 0x0dc0 }
        r4 = "";	 Catch:{ Exception -> 0x0dc0 }
        r0 = r56;	 Catch:{ Exception -> 0x0dc0 }
        r0.message = r4;	 Catch:{ Exception -> 0x0dc0 }
        if (r28 == 0) goto L_0x104c;	 Catch:{ Exception -> 0x0dc0 }
    L_0x1046:
        r0 = r56;	 Catch:{ Exception -> 0x0dc0 }
        r1 = r28;	 Catch:{ Exception -> 0x0dc0 }
        r1.sendRequest = r0;	 Catch:{ Exception -> 0x0dc0 }
    L_0x104c:
        r7 = r56;	 Catch:{ Exception -> 0x0dc0 }
        if (r82 != 0) goto L_0x105e;	 Catch:{ Exception -> 0x0dc0 }
    L_0x1050:
        r0 = r67;	 Catch:{ Exception -> 0x0dc0 }
        r4 = r0.currentAccount;	 Catch:{ Exception -> 0x0dc0 }
        r4 = org.telegram.messenger.DataQuery.getInstance(r4);	 Catch:{ Exception -> 0x0dc0 }
        r6 = 0;	 Catch:{ Exception -> 0x0dc0 }
        r0 = r76;	 Catch:{ Exception -> 0x0dc0 }
        r4.cleanDraft(r0, r6);	 Catch:{ Exception -> 0x0dc0 }
    L_0x105e:
        r8 = 0;	 Catch:{ Exception -> 0x0dc0 }
        r4 = (r32 > r8 ? 1 : (r32 == r8 ? 0 : -1));	 Catch:{ Exception -> 0x0dc0 }
        if (r4 == 0) goto L_0x11bb;	 Catch:{ Exception -> 0x0dc0 }
    L_0x1064:
        r0 = r67;	 Catch:{ Exception -> 0x0dc0 }
        r1 = r28;	 Catch:{ Exception -> 0x0dc0 }
        r0.performSendDelayedMessage(r1);	 Catch:{ Exception -> 0x0dc0 }
        goto L_0x0006;	 Catch:{ Exception -> 0x0dc0 }
    L_0x106d:
        r8 = 0;	 Catch:{ Exception -> 0x0dc0 }
        r4 = (r32 > r8 ? 1 : (r32 == r8 ? 0 : -1));	 Catch:{ Exception -> 0x0dc0 }
        if (r4 == 0) goto L_0x1131;	 Catch:{ Exception -> 0x0dc0 }
    L_0x1073:
        r0 = r28;	 Catch:{ Exception -> 0x0dc0 }
        r4 = r0.sendRequest;	 Catch:{ Exception -> 0x0dc0 }
        if (r4 == 0) goto L_0x10d7;	 Catch:{ Exception -> 0x0dc0 }
    L_0x1079:
        r0 = r28;	 Catch:{ Exception -> 0x0dc0 }
        r0 = r0.sendRequest;	 Catch:{ Exception -> 0x0dc0 }
        r56 = r0;	 Catch:{ Exception -> 0x0dc0 }
        r56 = (org.telegram.tgnet.TLRPC.TL_messages_sendMultiMedia) r56;	 Catch:{ Exception -> 0x0dc0 }
    L_0x1081:
        r0 = r28;	 Catch:{ Exception -> 0x0dc0 }
        r4 = r0.messageObjects;	 Catch:{ Exception -> 0x0dc0 }
        r4.add(r12);	 Catch:{ Exception -> 0x0dc0 }
        r0 = r28;	 Catch:{ Exception -> 0x0dc0 }
        r4 = r0.messages;	 Catch:{ Exception -> 0x0dc0 }
        r0 = r46;	 Catch:{ Exception -> 0x0dc0 }
        r4.add(r0);	 Catch:{ Exception -> 0x0dc0 }
        r0 = r28;	 Catch:{ Exception -> 0x0dc0 }
        r4 = r0.originalPaths;	 Catch:{ Exception -> 0x0dc0 }
        r0 = r51;	 Catch:{ Exception -> 0x0dc0 }
        r4.add(r0);	 Catch:{ Exception -> 0x0dc0 }
        r37 = new org.telegram.tgnet.TLRPC$TL_inputSingleMedia;	 Catch:{ Exception -> 0x0dc0 }
        r37.<init>();	 Catch:{ Exception -> 0x0dc0 }
        r0 = r46;	 Catch:{ Exception -> 0x0dc0 }
        r8 = r0.random_id;	 Catch:{ Exception -> 0x0dc0 }
        r0 = r37;	 Catch:{ Exception -> 0x0dc0 }
        r0.random_id = r8;	 Catch:{ Exception -> 0x0dc0 }
        r0 = r36;	 Catch:{ Exception -> 0x0dc0 }
        r1 = r37;	 Catch:{ Exception -> 0x0dc0 }
        r1.media = r0;	 Catch:{ Exception -> 0x0dc0 }
        r0 = r69;	 Catch:{ Exception -> 0x0dc0 }
        r1 = r37;	 Catch:{ Exception -> 0x0dc0 }
        r1.message = r0;	 Catch:{ Exception -> 0x0dc0 }
        if (r83 == 0) goto L_0x10cb;	 Catch:{ Exception -> 0x0dc0 }
    L_0x10b5:
        r4 = r83.isEmpty();	 Catch:{ Exception -> 0x0dc0 }
        if (r4 != 0) goto L_0x10cb;	 Catch:{ Exception -> 0x0dc0 }
    L_0x10bb:
        r0 = r83;	 Catch:{ Exception -> 0x0dc0 }
        r1 = r37;	 Catch:{ Exception -> 0x0dc0 }
        r1.entities = r0;	 Catch:{ Exception -> 0x0dc0 }
        r0 = r37;	 Catch:{ Exception -> 0x0dc0 }
        r4 = r0.flags;	 Catch:{ Exception -> 0x0dc0 }
        r4 = r4 | 1;	 Catch:{ Exception -> 0x0dc0 }
        r0 = r37;	 Catch:{ Exception -> 0x0dc0 }
        r0.flags = r4;	 Catch:{ Exception -> 0x0dc0 }
    L_0x10cb:
        r0 = r56;	 Catch:{ Exception -> 0x0dc0 }
        r4 = r0.multi_media;	 Catch:{ Exception -> 0x0dc0 }
        r0 = r37;	 Catch:{ Exception -> 0x0dc0 }
        r4.add(r0);	 Catch:{ Exception -> 0x0dc0 }
        r7 = r56;	 Catch:{ Exception -> 0x0dc0 }
        goto L_0x105e;	 Catch:{ Exception -> 0x0dc0 }
    L_0x10d7:
        r56 = new org.telegram.tgnet.TLRPC$TL_messages_sendMultiMedia;	 Catch:{ Exception -> 0x0dc0 }
        r56.<init>();	 Catch:{ Exception -> 0x0dc0 }
        r0 = r57;	 Catch:{ Exception -> 0x0dc0 }
        r1 = r56;	 Catch:{ Exception -> 0x0dc0 }
        r1.peer = r0;	 Catch:{ Exception -> 0x0dc0 }
        r0 = r46;	 Catch:{ Exception -> 0x0dc0 }
        r4 = r0.to_id;	 Catch:{ Exception -> 0x0dc0 }
        r4 = r4 instanceof org.telegram.tgnet.TLRPC.TL_peerChannel;	 Catch:{ Exception -> 0x0dc0 }
        if (r4 == 0) goto L_0x1111;	 Catch:{ Exception -> 0x0dc0 }
    L_0x10ea:
        r0 = r67;	 Catch:{ Exception -> 0x0dc0 }
        r4 = r0.currentAccount;	 Catch:{ Exception -> 0x0dc0 }
        r4 = org.telegram.messenger.MessagesController.getNotificationsSettings(r4);	 Catch:{ Exception -> 0x0dc0 }
        r6 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x0dc0 }
        r6.<init>();	 Catch:{ Exception -> 0x0dc0 }
        r8 = "silent_";	 Catch:{ Exception -> 0x0dc0 }
        r6 = r6.append(r8);	 Catch:{ Exception -> 0x0dc0 }
        r0 = r76;	 Catch:{ Exception -> 0x0dc0 }
        r6 = r6.append(r0);	 Catch:{ Exception -> 0x0dc0 }
        r6 = r6.toString();	 Catch:{ Exception -> 0x0dc0 }
        r8 = 0;	 Catch:{ Exception -> 0x0dc0 }
        r4 = r4.getBoolean(r6, r8);	 Catch:{ Exception -> 0x0dc0 }
        r0 = r56;	 Catch:{ Exception -> 0x0dc0 }
        r0.silent = r4;	 Catch:{ Exception -> 0x0dc0 }
    L_0x1111:
        r0 = r46;	 Catch:{ Exception -> 0x0dc0 }
        r4 = r0.reply_to_msg_id;	 Catch:{ Exception -> 0x0dc0 }
        if (r4 == 0) goto L_0x1129;	 Catch:{ Exception -> 0x0dc0 }
    L_0x1117:
        r0 = r56;	 Catch:{ Exception -> 0x0dc0 }
        r4 = r0.flags;	 Catch:{ Exception -> 0x0dc0 }
        r4 = r4 | 1;	 Catch:{ Exception -> 0x0dc0 }
        r0 = r56;	 Catch:{ Exception -> 0x0dc0 }
        r0.flags = r4;	 Catch:{ Exception -> 0x0dc0 }
        r0 = r46;	 Catch:{ Exception -> 0x0dc0 }
        r4 = r0.reply_to_msg_id;	 Catch:{ Exception -> 0x0dc0 }
        r0 = r56;	 Catch:{ Exception -> 0x0dc0 }
        r0.reply_to_msg_id = r4;	 Catch:{ Exception -> 0x0dc0 }
    L_0x1129:
        r0 = r56;	 Catch:{ Exception -> 0x0dc0 }
        r1 = r28;	 Catch:{ Exception -> 0x0dc0 }
        r1.sendRequest = r0;	 Catch:{ Exception -> 0x0dc0 }
        goto L_0x1081;	 Catch:{ Exception -> 0x0dc0 }
    L_0x1131:
        r56 = new org.telegram.tgnet.TLRPC$TL_messages_sendMedia;	 Catch:{ Exception -> 0x0dc0 }
        r56.<init>();	 Catch:{ Exception -> 0x0dc0 }
        r0 = r57;	 Catch:{ Exception -> 0x0dc0 }
        r1 = r56;	 Catch:{ Exception -> 0x0dc0 }
        r1.peer = r0;	 Catch:{ Exception -> 0x0dc0 }
        r0 = r46;	 Catch:{ Exception -> 0x0dc0 }
        r4 = r0.to_id;	 Catch:{ Exception -> 0x0dc0 }
        r4 = r4 instanceof org.telegram.tgnet.TLRPC.TL_peerChannel;	 Catch:{ Exception -> 0x0dc0 }
        if (r4 == 0) goto L_0x116b;	 Catch:{ Exception -> 0x0dc0 }
    L_0x1144:
        r0 = r67;	 Catch:{ Exception -> 0x0dc0 }
        r4 = r0.currentAccount;	 Catch:{ Exception -> 0x0dc0 }
        r4 = org.telegram.messenger.MessagesController.getNotificationsSettings(r4);	 Catch:{ Exception -> 0x0dc0 }
        r6 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x0dc0 }
        r6.<init>();	 Catch:{ Exception -> 0x0dc0 }
        r8 = "silent_";	 Catch:{ Exception -> 0x0dc0 }
        r6 = r6.append(r8);	 Catch:{ Exception -> 0x0dc0 }
        r0 = r76;	 Catch:{ Exception -> 0x0dc0 }
        r6 = r6.append(r0);	 Catch:{ Exception -> 0x0dc0 }
        r6 = r6.toString();	 Catch:{ Exception -> 0x0dc0 }
        r8 = 0;	 Catch:{ Exception -> 0x0dc0 }
        r4 = r4.getBoolean(r6, r8);	 Catch:{ Exception -> 0x0dc0 }
        r0 = r56;	 Catch:{ Exception -> 0x0dc0 }
        r0.silent = r4;	 Catch:{ Exception -> 0x0dc0 }
    L_0x116b:
        r0 = r46;	 Catch:{ Exception -> 0x0dc0 }
        r4 = r0.reply_to_msg_id;	 Catch:{ Exception -> 0x0dc0 }
        if (r4 == 0) goto L_0x1183;	 Catch:{ Exception -> 0x0dc0 }
    L_0x1171:
        r0 = r56;	 Catch:{ Exception -> 0x0dc0 }
        r4 = r0.flags;	 Catch:{ Exception -> 0x0dc0 }
        r4 = r4 | 1;	 Catch:{ Exception -> 0x0dc0 }
        r0 = r56;	 Catch:{ Exception -> 0x0dc0 }
        r0.flags = r4;	 Catch:{ Exception -> 0x0dc0 }
        r0 = r46;	 Catch:{ Exception -> 0x0dc0 }
        r4 = r0.reply_to_msg_id;	 Catch:{ Exception -> 0x0dc0 }
        r0 = r56;	 Catch:{ Exception -> 0x0dc0 }
        r0.reply_to_msg_id = r4;	 Catch:{ Exception -> 0x0dc0 }
    L_0x1183:
        r0 = r46;	 Catch:{ Exception -> 0x0dc0 }
        r8 = r0.random_id;	 Catch:{ Exception -> 0x0dc0 }
        r0 = r56;	 Catch:{ Exception -> 0x0dc0 }
        r0.random_id = r8;	 Catch:{ Exception -> 0x0dc0 }
        r0 = r36;	 Catch:{ Exception -> 0x0dc0 }
        r1 = r56;	 Catch:{ Exception -> 0x0dc0 }
        r1.media = r0;	 Catch:{ Exception -> 0x0dc0 }
        r0 = r69;	 Catch:{ Exception -> 0x0dc0 }
        r1 = r56;	 Catch:{ Exception -> 0x0dc0 }
        r1.message = r0;	 Catch:{ Exception -> 0x0dc0 }
        if (r83 == 0) goto L_0x11af;	 Catch:{ Exception -> 0x0dc0 }
    L_0x1199:
        r4 = r83.isEmpty();	 Catch:{ Exception -> 0x0dc0 }
        if (r4 != 0) goto L_0x11af;	 Catch:{ Exception -> 0x0dc0 }
    L_0x119f:
        r0 = r83;	 Catch:{ Exception -> 0x0dc0 }
        r1 = r56;	 Catch:{ Exception -> 0x0dc0 }
        r1.entities = r0;	 Catch:{ Exception -> 0x0dc0 }
        r0 = r56;	 Catch:{ Exception -> 0x0dc0 }
        r4 = r0.flags;	 Catch:{ Exception -> 0x0dc0 }
        r4 = r4 | 8;	 Catch:{ Exception -> 0x0dc0 }
        r0 = r56;	 Catch:{ Exception -> 0x0dc0 }
        r0.flags = r4;	 Catch:{ Exception -> 0x0dc0 }
    L_0x11af:
        if (r28 == 0) goto L_0x11b7;	 Catch:{ Exception -> 0x0dc0 }
    L_0x11b1:
        r0 = r56;	 Catch:{ Exception -> 0x0dc0 }
        r1 = r28;	 Catch:{ Exception -> 0x0dc0 }
        r1.sendRequest = r0;	 Catch:{ Exception -> 0x0dc0 }
    L_0x11b7:
        r7 = r56;	 Catch:{ Exception -> 0x0dc0 }
        goto L_0x105e;	 Catch:{ Exception -> 0x0dc0 }
    L_0x11bb:
        r4 = 1;	 Catch:{ Exception -> 0x0dc0 }
        r0 = r63;	 Catch:{ Exception -> 0x0dc0 }
        if (r0 != r4) goto L_0x11c8;	 Catch:{ Exception -> 0x0dc0 }
    L_0x11c0:
        r4 = 0;	 Catch:{ Exception -> 0x0dc0 }
        r0 = r67;	 Catch:{ Exception -> 0x0dc0 }
        r0.performSendMessageRequest(r7, r12, r4);	 Catch:{ Exception -> 0x0dc0 }
        goto L_0x0006;	 Catch:{ Exception -> 0x0dc0 }
    L_0x11c8:
        r4 = 2;	 Catch:{ Exception -> 0x0dc0 }
        r0 = r63;	 Catch:{ Exception -> 0x0dc0 }
        if (r0 != r4) goto L_0x11eb;	 Catch:{ Exception -> 0x0dc0 }
    L_0x11cd:
        r0 = r71;	 Catch:{ Exception -> 0x0dc0 }
        r8 = r0.access_hash;	 Catch:{ Exception -> 0x0dc0 }
        r14 = 0;	 Catch:{ Exception -> 0x0dc0 }
        r4 = (r8 > r14 ? 1 : (r8 == r14 ? 0 : -1));	 Catch:{ Exception -> 0x0dc0 }
        if (r4 != 0) goto L_0x11e0;	 Catch:{ Exception -> 0x0dc0 }
    L_0x11d7:
        r0 = r67;	 Catch:{ Exception -> 0x0dc0 }
        r1 = r28;	 Catch:{ Exception -> 0x0dc0 }
        r0.performSendDelayedMessage(r1);	 Catch:{ Exception -> 0x0dc0 }
        goto L_0x0006;	 Catch:{ Exception -> 0x0dc0 }
    L_0x11e0:
        r9 = 0;	 Catch:{ Exception -> 0x0dc0 }
        r10 = 0;	 Catch:{ Exception -> 0x0dc0 }
        r11 = 1;	 Catch:{ Exception -> 0x0dc0 }
        r6 = r67;	 Catch:{ Exception -> 0x0dc0 }
        r8 = r12;	 Catch:{ Exception -> 0x0dc0 }
        r6.performSendMessageRequest(r7, r8, r9, r10, r11);	 Catch:{ Exception -> 0x0dc0 }
        goto L_0x0006;	 Catch:{ Exception -> 0x0dc0 }
    L_0x11eb:
        r4 = 3;	 Catch:{ Exception -> 0x0dc0 }
        r0 = r63;	 Catch:{ Exception -> 0x0dc0 }
        if (r0 != r4) goto L_0x120b;	 Catch:{ Exception -> 0x0dc0 }
    L_0x11f0:
        r0 = r74;	 Catch:{ Exception -> 0x0dc0 }
        r8 = r0.access_hash;	 Catch:{ Exception -> 0x0dc0 }
        r14 = 0;	 Catch:{ Exception -> 0x0dc0 }
        r4 = (r8 > r14 ? 1 : (r8 == r14 ? 0 : -1));	 Catch:{ Exception -> 0x0dc0 }
        if (r4 != 0) goto L_0x1203;	 Catch:{ Exception -> 0x0dc0 }
    L_0x11fa:
        r0 = r67;	 Catch:{ Exception -> 0x0dc0 }
        r1 = r28;	 Catch:{ Exception -> 0x0dc0 }
        r0.performSendDelayedMessage(r1);	 Catch:{ Exception -> 0x0dc0 }
        goto L_0x0006;	 Catch:{ Exception -> 0x0dc0 }
    L_0x1203:
        r4 = 0;	 Catch:{ Exception -> 0x0dc0 }
        r0 = r67;	 Catch:{ Exception -> 0x0dc0 }
        r0.performSendMessageRequest(r7, r12, r4);	 Catch:{ Exception -> 0x0dc0 }
        goto L_0x0006;	 Catch:{ Exception -> 0x0dc0 }
    L_0x120b:
        r4 = 6;	 Catch:{ Exception -> 0x0dc0 }
        r0 = r63;	 Catch:{ Exception -> 0x0dc0 }
        if (r0 != r4) goto L_0x1218;	 Catch:{ Exception -> 0x0dc0 }
    L_0x1210:
        r4 = 0;	 Catch:{ Exception -> 0x0dc0 }
        r0 = r67;	 Catch:{ Exception -> 0x0dc0 }
        r0.performSendMessageRequest(r7, r12, r4);	 Catch:{ Exception -> 0x0dc0 }
        goto L_0x0006;	 Catch:{ Exception -> 0x0dc0 }
    L_0x1218:
        r4 = 7;	 Catch:{ Exception -> 0x0dc0 }
        r0 = r63;	 Catch:{ Exception -> 0x0dc0 }
        if (r0 != r4) goto L_0x123b;	 Catch:{ Exception -> 0x0dc0 }
    L_0x121d:
        r0 = r74;	 Catch:{ Exception -> 0x0dc0 }
        r8 = r0.access_hash;	 Catch:{ Exception -> 0x0dc0 }
        r14 = 0;	 Catch:{ Exception -> 0x0dc0 }
        r4 = (r8 > r14 ? 1 : (r8 == r14 ? 0 : -1));	 Catch:{ Exception -> 0x0dc0 }
        if (r4 != 0) goto L_0x1232;	 Catch:{ Exception -> 0x0dc0 }
    L_0x1227:
        if (r28 == 0) goto L_0x1232;	 Catch:{ Exception -> 0x0dc0 }
    L_0x1229:
        r0 = r67;	 Catch:{ Exception -> 0x0dc0 }
        r1 = r28;	 Catch:{ Exception -> 0x0dc0 }
        r0.performSendDelayedMessage(r1);	 Catch:{ Exception -> 0x0dc0 }
        goto L_0x0006;	 Catch:{ Exception -> 0x0dc0 }
    L_0x1232:
        r0 = r67;	 Catch:{ Exception -> 0x0dc0 }
        r1 = r51;	 Catch:{ Exception -> 0x0dc0 }
        r0.performSendMessageRequest(r7, r12, r1);	 Catch:{ Exception -> 0x0dc0 }
        goto L_0x0006;	 Catch:{ Exception -> 0x0dc0 }
    L_0x123b:
        r4 = 8;	 Catch:{ Exception -> 0x0dc0 }
        r0 = r63;	 Catch:{ Exception -> 0x0dc0 }
        if (r0 != r4) goto L_0x0006;	 Catch:{ Exception -> 0x0dc0 }
    L_0x1241:
        r0 = r74;	 Catch:{ Exception -> 0x0dc0 }
        r8 = r0.access_hash;	 Catch:{ Exception -> 0x0dc0 }
        r14 = 0;	 Catch:{ Exception -> 0x0dc0 }
        r4 = (r8 > r14 ? 1 : (r8 == r14 ? 0 : -1));	 Catch:{ Exception -> 0x0dc0 }
        if (r4 != 0) goto L_0x1254;	 Catch:{ Exception -> 0x0dc0 }
    L_0x124b:
        r0 = r67;	 Catch:{ Exception -> 0x0dc0 }
        r1 = r28;	 Catch:{ Exception -> 0x0dc0 }
        r0.performSendDelayedMessage(r1);	 Catch:{ Exception -> 0x0dc0 }
        goto L_0x0006;	 Catch:{ Exception -> 0x0dc0 }
    L_0x1254:
        r4 = 0;	 Catch:{ Exception -> 0x0dc0 }
        r0 = r67;	 Catch:{ Exception -> 0x0dc0 }
        r0.performSendMessageRequest(r7, r12, r4);	 Catch:{ Exception -> 0x0dc0 }
        goto L_0x0006;
    L_0x125c:
        r0 = r16;	 Catch:{ Exception -> 0x0c48 }
        r4 = r0.layer;	 Catch:{ Exception -> 0x0c48 }
        r4 = org.telegram.messenger.AndroidUtilities.getPeerLayerVersion(r4);	 Catch:{ Exception -> 0x0c48 }
        r6 = 73;	 Catch:{ Exception -> 0x0c48 }
        if (r4 < r6) goto L_0x139d;	 Catch:{ Exception -> 0x0c48 }
    L_0x1268:
        r7 = new org.telegram.tgnet.TLRPC$TL_decryptedMessage;	 Catch:{ Exception -> 0x0c48 }
        r7.<init>();	 Catch:{ Exception -> 0x0c48 }
        r8 = 0;	 Catch:{ Exception -> 0x0c48 }
        r4 = (r32 > r8 ? 1 : (r32 == r8 ? 0 : -1));	 Catch:{ Exception -> 0x0c48 }
        if (r4 == 0) goto L_0x127e;	 Catch:{ Exception -> 0x0c48 }
    L_0x1273:
        r0 = r32;	 Catch:{ Exception -> 0x0c48 }
        r7.grouped_id = r0;	 Catch:{ Exception -> 0x0c48 }
        r4 = r7.flags;	 Catch:{ Exception -> 0x0c48 }
        r6 = 131072; // 0x20000 float:1.83671E-40 double:6.47582E-319;	 Catch:{ Exception -> 0x0c48 }
        r4 = r4 | r6;	 Catch:{ Exception -> 0x0c48 }
        r7.flags = r4;	 Catch:{ Exception -> 0x0c48 }
    L_0x127e:
        r0 = r46;	 Catch:{ Exception -> 0x0c48 }
        r4 = r0.ttl;	 Catch:{ Exception -> 0x0c48 }
        r7.ttl = r4;	 Catch:{ Exception -> 0x0c48 }
        if (r83 == 0) goto L_0x1296;	 Catch:{ Exception -> 0x0c48 }
    L_0x1286:
        r4 = r83.isEmpty();	 Catch:{ Exception -> 0x0c48 }
        if (r4 != 0) goto L_0x1296;	 Catch:{ Exception -> 0x0c48 }
    L_0x128c:
        r0 = r83;	 Catch:{ Exception -> 0x0c48 }
        r7.entities = r0;	 Catch:{ Exception -> 0x0c48 }
        r4 = r7.flags;	 Catch:{ Exception -> 0x0c48 }
        r4 = r4 | 128;	 Catch:{ Exception -> 0x0c48 }
        r7.flags = r4;	 Catch:{ Exception -> 0x0c48 }
    L_0x1296:
        r0 = r46;	 Catch:{ Exception -> 0x0c48 }
        r8 = r0.reply_to_random_id;	 Catch:{ Exception -> 0x0c48 }
        r14 = 0;	 Catch:{ Exception -> 0x0c48 }
        r4 = (r8 > r14 ? 1 : (r8 == r14 ? 0 : -1));	 Catch:{ Exception -> 0x0c48 }
        if (r4 == 0) goto L_0x12ac;	 Catch:{ Exception -> 0x0c48 }
    L_0x12a0:
        r0 = r46;	 Catch:{ Exception -> 0x0c48 }
        r8 = r0.reply_to_random_id;	 Catch:{ Exception -> 0x0c48 }
        r7.reply_to_random_id = r8;	 Catch:{ Exception -> 0x0c48 }
        r4 = r7.flags;	 Catch:{ Exception -> 0x0c48 }
        r4 = r4 | 8;	 Catch:{ Exception -> 0x0c48 }
        r7.flags = r4;	 Catch:{ Exception -> 0x0c48 }
    L_0x12ac:
        r4 = r7.flags;	 Catch:{ Exception -> 0x0c48 }
        r4 = r4 | 512;	 Catch:{ Exception -> 0x0c48 }
        r7.flags = r4;	 Catch:{ Exception -> 0x0c48 }
        if (r85 == 0) goto L_0x12d2;	 Catch:{ Exception -> 0x0c48 }
    L_0x12b4:
        r4 = "bot_name";	 Catch:{ Exception -> 0x0c48 }
        r0 = r85;	 Catch:{ Exception -> 0x0c48 }
        r4 = r0.get(r4);	 Catch:{ Exception -> 0x0c48 }
        if (r4 == 0) goto L_0x12d2;	 Catch:{ Exception -> 0x0c48 }
    L_0x12bf:
        r4 = "bot_name";	 Catch:{ Exception -> 0x0c48 }
        r0 = r85;	 Catch:{ Exception -> 0x0c48 }
        r4 = r0.get(r4);	 Catch:{ Exception -> 0x0c48 }
        r4 = (java.lang.String) r4;	 Catch:{ Exception -> 0x0c48 }
        r7.via_bot_name = r4;	 Catch:{ Exception -> 0x0c48 }
        r4 = r7.flags;	 Catch:{ Exception -> 0x0c48 }
        r4 = r4 | 2048;	 Catch:{ Exception -> 0x0c48 }
        r7.flags = r4;	 Catch:{ Exception -> 0x0c48 }
    L_0x12d2:
        r0 = r46;	 Catch:{ Exception -> 0x0c48 }
        r8 = r0.random_id;	 Catch:{ Exception -> 0x0c48 }
        r7.random_id = r8;	 Catch:{ Exception -> 0x0c48 }
        r4 = "";	 Catch:{ Exception -> 0x0c48 }
        r7.message = r4;	 Catch:{ Exception -> 0x0c48 }
        r4 = 1;	 Catch:{ Exception -> 0x0c48 }
        r0 = r63;	 Catch:{ Exception -> 0x0c48 }
        if (r0 != r4) goto L_0x13ad;	 Catch:{ Exception -> 0x0c48 }
    L_0x12e2:
        r0 = r70;	 Catch:{ Exception -> 0x0c48 }
        r4 = r0 instanceof org.telegram.tgnet.TLRPC.TL_messageMediaVenue;	 Catch:{ Exception -> 0x0c48 }
        if (r4 == 0) goto L_0x13a4;	 Catch:{ Exception -> 0x0c48 }
    L_0x12e8:
        r4 = new org.telegram.tgnet.TLRPC$TL_decryptedMessageMediaVenue;	 Catch:{ Exception -> 0x0c48 }
        r4.<init>();	 Catch:{ Exception -> 0x0c48 }
        r7.media = r4;	 Catch:{ Exception -> 0x0c48 }
        r4 = r7.media;	 Catch:{ Exception -> 0x0c48 }
        r0 = r70;	 Catch:{ Exception -> 0x0c48 }
        r6 = r0.address;	 Catch:{ Exception -> 0x0c48 }
        r4.address = r6;	 Catch:{ Exception -> 0x0c48 }
        r4 = r7.media;	 Catch:{ Exception -> 0x0c48 }
        r0 = r70;	 Catch:{ Exception -> 0x0c48 }
        r6 = r0.title;	 Catch:{ Exception -> 0x0c48 }
        r4.title = r6;	 Catch:{ Exception -> 0x0c48 }
        r4 = r7.media;	 Catch:{ Exception -> 0x0c48 }
        r0 = r70;	 Catch:{ Exception -> 0x0c48 }
        r6 = r0.provider;	 Catch:{ Exception -> 0x0c48 }
        r4.provider = r6;	 Catch:{ Exception -> 0x0c48 }
        r4 = r7.media;	 Catch:{ Exception -> 0x0c48 }
        r0 = r70;	 Catch:{ Exception -> 0x0c48 }
        r6 = r0.venue_id;	 Catch:{ Exception -> 0x0c48 }
        r4.venue_id = r6;	 Catch:{ Exception -> 0x0c48 }
    L_0x130f:
        r4 = r7.media;	 Catch:{ Exception -> 0x0c48 }
        r0 = r70;	 Catch:{ Exception -> 0x0c48 }
        r6 = r0.geo;	 Catch:{ Exception -> 0x0c48 }
        r8 = r6.lat;	 Catch:{ Exception -> 0x0c48 }
        r4.lat = r8;	 Catch:{ Exception -> 0x0c48 }
        r4 = r7.media;	 Catch:{ Exception -> 0x0c48 }
        r0 = r70;	 Catch:{ Exception -> 0x0c48 }
        r6 = r0.geo;	 Catch:{ Exception -> 0x0c48 }
        r8 = r6._long;	 Catch:{ Exception -> 0x0c48 }
        r4._long = r8;	 Catch:{ Exception -> 0x0c48 }
        r0 = r67;	 Catch:{ Exception -> 0x0c48 }
        r4 = r0.currentAccount;	 Catch:{ Exception -> 0x0c48 }
        r6 = org.telegram.messenger.SecretChatHelper.getInstance(r4);	 Catch:{ Exception -> 0x0c48 }
        r8 = r12.messageOwner;	 Catch:{ Exception -> 0x0c48 }
        r10 = 0;	 Catch:{ Exception -> 0x0c48 }
        r11 = 0;	 Catch:{ Exception -> 0x0c48 }
        r9 = r16;	 Catch:{ Exception -> 0x0c48 }
        r6.performSendEncryptedRequest(r7, r8, r9, r10, r11, r12);	 Catch:{ Exception -> 0x0c48 }
        r28 = r29;
    L_0x1336:
        r8 = 0;
        r4 = (r32 > r8 ? 1 : (r32 == r8 ? 0 : -1));
        if (r4 == 0) goto L_0x138b;
    L_0x133c:
        r0 = r28;	 Catch:{ Exception -> 0x0dc0 }
        r4 = r0.sendEncryptedRequest;	 Catch:{ Exception -> 0x0dc0 }
        if (r4 == 0) goto L_0x1897;	 Catch:{ Exception -> 0x0dc0 }
    L_0x1342:
        r0 = r28;	 Catch:{ Exception -> 0x0dc0 }
        r0 = r0.sendEncryptedRequest;	 Catch:{ Exception -> 0x0dc0 }
        r56 = r0;	 Catch:{ Exception -> 0x0dc0 }
        r56 = (org.telegram.tgnet.TLRPC.TL_messages_sendEncryptedMultiMedia) r56;	 Catch:{ Exception -> 0x0dc0 }
    L_0x134a:
        r0 = r28;	 Catch:{ Exception -> 0x0dc0 }
        r4 = r0.messageObjects;	 Catch:{ Exception -> 0x0dc0 }
        r4.add(r12);	 Catch:{ Exception -> 0x0dc0 }
        r0 = r28;	 Catch:{ Exception -> 0x0dc0 }
        r4 = r0.messages;	 Catch:{ Exception -> 0x0dc0 }
        r0 = r46;	 Catch:{ Exception -> 0x0dc0 }
        r4.add(r0);	 Catch:{ Exception -> 0x0dc0 }
        r0 = r28;	 Catch:{ Exception -> 0x0dc0 }
        r4 = r0.originalPaths;	 Catch:{ Exception -> 0x0dc0 }
        r0 = r51;	 Catch:{ Exception -> 0x0dc0 }
        r4.add(r0);	 Catch:{ Exception -> 0x0dc0 }
        r4 = 1;	 Catch:{ Exception -> 0x0dc0 }
        r0 = r28;	 Catch:{ Exception -> 0x0dc0 }
        r0.upload = r4;	 Catch:{ Exception -> 0x0dc0 }
        r0 = r56;	 Catch:{ Exception -> 0x0dc0 }
        r4 = r0.messages;	 Catch:{ Exception -> 0x0dc0 }
        r4.add(r7);	 Catch:{ Exception -> 0x0dc0 }
        r10 = new org.telegram.tgnet.TLRPC$TL_inputEncryptedFile;	 Catch:{ Exception -> 0x0dc0 }
        r10.<init>();	 Catch:{ Exception -> 0x0dc0 }
        r4 = 3;	 Catch:{ Exception -> 0x0dc0 }
        r0 = r63;	 Catch:{ Exception -> 0x0dc0 }
        if (r0 != r4) goto L_0x18a4;	 Catch:{ Exception -> 0x0dc0 }
    L_0x1379:
        r8 = 1;	 Catch:{ Exception -> 0x0dc0 }
    L_0x137b:
        r10.id = r8;	 Catch:{ Exception -> 0x0dc0 }
        r0 = r56;	 Catch:{ Exception -> 0x0dc0 }
        r4 = r0.files;	 Catch:{ Exception -> 0x0dc0 }
        r4.add(r10);	 Catch:{ Exception -> 0x0dc0 }
        r0 = r67;	 Catch:{ Exception -> 0x0dc0 }
        r1 = r28;	 Catch:{ Exception -> 0x0dc0 }
        r0.performSendDelayedMessage(r1);	 Catch:{ Exception -> 0x0dc0 }
    L_0x138b:
        if (r82 != 0) goto L_0x0006;	 Catch:{ Exception -> 0x0dc0 }
    L_0x138d:
        r0 = r67;	 Catch:{ Exception -> 0x0dc0 }
        r4 = r0.currentAccount;	 Catch:{ Exception -> 0x0dc0 }
        r4 = org.telegram.messenger.DataQuery.getInstance(r4);	 Catch:{ Exception -> 0x0dc0 }
        r6 = 0;	 Catch:{ Exception -> 0x0dc0 }
        r0 = r76;	 Catch:{ Exception -> 0x0dc0 }
        r4.cleanDraft(r0, r6);	 Catch:{ Exception -> 0x0dc0 }
        goto L_0x0006;
    L_0x139d:
        r7 = new org.telegram.tgnet.TLRPC$TL_decryptedMessage_layer45;	 Catch:{ Exception -> 0x0c48 }
        r7.<init>();	 Catch:{ Exception -> 0x0c48 }
        goto L_0x127e;	 Catch:{ Exception -> 0x0c48 }
    L_0x13a4:
        r4 = new org.telegram.tgnet.TLRPC$TL_decryptedMessageMediaGeoPoint;	 Catch:{ Exception -> 0x0c48 }
        r4.<init>();	 Catch:{ Exception -> 0x0c48 }
        r7.media = r4;	 Catch:{ Exception -> 0x0c48 }
        goto L_0x130f;	 Catch:{ Exception -> 0x0c48 }
    L_0x13ad:
        r4 = 2;	 Catch:{ Exception -> 0x0c48 }
        r0 = r63;	 Catch:{ Exception -> 0x0c48 }
        if (r0 == r4) goto L_0x13ba;	 Catch:{ Exception -> 0x0c48 }
    L_0x13b2:
        r4 = 9;	 Catch:{ Exception -> 0x0c48 }
        r0 = r63;	 Catch:{ Exception -> 0x0c48 }
        if (r0 != r4) goto L_0x14de;	 Catch:{ Exception -> 0x0c48 }
    L_0x13b8:
        if (r71 == 0) goto L_0x14de;	 Catch:{ Exception -> 0x0c48 }
    L_0x13ba:
        r0 = r71;	 Catch:{ Exception -> 0x0c48 }
        r4 = r0.sizes;	 Catch:{ Exception -> 0x0c48 }
        r6 = 0;	 Catch:{ Exception -> 0x0c48 }
        r62 = r4.get(r6);	 Catch:{ Exception -> 0x0c48 }
        r62 = (org.telegram.tgnet.TLRPC.PhotoSize) r62;	 Catch:{ Exception -> 0x0c48 }
        r0 = r71;	 Catch:{ Exception -> 0x0c48 }
        r4 = r0.sizes;	 Catch:{ Exception -> 0x0c48 }
        r0 = r71;	 Catch:{ Exception -> 0x0c48 }
        r6 = r0.sizes;	 Catch:{ Exception -> 0x0c48 }
        r6 = r6.size();	 Catch:{ Exception -> 0x0c48 }
        r6 = r6 + -1;	 Catch:{ Exception -> 0x0c48 }
        r25 = r4.get(r6);	 Catch:{ Exception -> 0x0c48 }
        r25 = (org.telegram.tgnet.TLRPC.PhotoSize) r25;	 Catch:{ Exception -> 0x0c48 }
        org.telegram.messenger.ImageLoader.fillPhotoSizeWithBytes(r62);	 Catch:{ Exception -> 0x0c48 }
        r4 = new org.telegram.tgnet.TLRPC$TL_decryptedMessageMediaPhoto;	 Catch:{ Exception -> 0x0c48 }
        r4.<init>();	 Catch:{ Exception -> 0x0c48 }
        r7.media = r4;	 Catch:{ Exception -> 0x0c48 }
        r4 = r7.media;	 Catch:{ Exception -> 0x0c48 }
        r0 = r69;	 Catch:{ Exception -> 0x0c48 }
        r4.caption = r0;	 Catch:{ Exception -> 0x0c48 }
        r0 = r62;	 Catch:{ Exception -> 0x0c48 }
        r4 = r0.bytes;	 Catch:{ Exception -> 0x0c48 }
        if (r4 == 0) goto L_0x147b;	 Catch:{ Exception -> 0x0c48 }
    L_0x13ef:
        r4 = r7.media;	 Catch:{ Exception -> 0x0c48 }
        r4 = (org.telegram.tgnet.TLRPC.TL_decryptedMessageMediaPhoto) r4;	 Catch:{ Exception -> 0x0c48 }
        r0 = r62;	 Catch:{ Exception -> 0x0c48 }
        r6 = r0.bytes;	 Catch:{ Exception -> 0x0c48 }
        r4.thumb = r6;	 Catch:{ Exception -> 0x0c48 }
    L_0x13f9:
        r4 = r7.media;	 Catch:{ Exception -> 0x0c48 }
        r0 = r62;	 Catch:{ Exception -> 0x0c48 }
        r6 = r0.f24h;	 Catch:{ Exception -> 0x0c48 }
        r4.thumb_h = r6;	 Catch:{ Exception -> 0x0c48 }
        r4 = r7.media;	 Catch:{ Exception -> 0x0c48 }
        r0 = r62;	 Catch:{ Exception -> 0x0c48 }
        r6 = r0.f25w;	 Catch:{ Exception -> 0x0c48 }
        r4.thumb_w = r6;	 Catch:{ Exception -> 0x0c48 }
        r4 = r7.media;	 Catch:{ Exception -> 0x0c48 }
        r0 = r25;	 Catch:{ Exception -> 0x0c48 }
        r6 = r0.f25w;	 Catch:{ Exception -> 0x0c48 }
        r4.f16w = r6;	 Catch:{ Exception -> 0x0c48 }
        r4 = r7.media;	 Catch:{ Exception -> 0x0c48 }
        r0 = r25;	 Catch:{ Exception -> 0x0c48 }
        r6 = r0.f24h;	 Catch:{ Exception -> 0x0c48 }
        r4.f15h = r6;	 Catch:{ Exception -> 0x0c48 }
        r4 = r7.media;	 Catch:{ Exception -> 0x0c48 }
        r0 = r25;	 Catch:{ Exception -> 0x0c48 }
        r6 = r0.size;	 Catch:{ Exception -> 0x0c48 }
        r4.size = r6;	 Catch:{ Exception -> 0x0c48 }
        r0 = r25;	 Catch:{ Exception -> 0x0c48 }
        r4 = r0.location;	 Catch:{ Exception -> 0x0c48 }
        r4 = r4.key;	 Catch:{ Exception -> 0x0c48 }
        if (r4 == 0) goto L_0x142f;	 Catch:{ Exception -> 0x0c48 }
    L_0x1429:
        r8 = 0;	 Catch:{ Exception -> 0x0c48 }
        r4 = (r32 > r8 ? 1 : (r32 == r8 ? 0 : -1));	 Catch:{ Exception -> 0x0c48 }
        if (r4 == 0) goto L_0x14a1;	 Catch:{ Exception -> 0x0c48 }
    L_0x142f:
        if (r29 != 0) goto L_0x1a31;	 Catch:{ Exception -> 0x0c48 }
    L_0x1431:
        r28 = new org.telegram.messenger.SendMessagesHelper$DelayedMessage;	 Catch:{ Exception -> 0x0c48 }
        r0 = r28;	 Catch:{ Exception -> 0x0c48 }
        r1 = r67;	 Catch:{ Exception -> 0x0c48 }
        r2 = r76;	 Catch:{ Exception -> 0x0c48 }
        r0.<init>(r2);	 Catch:{ Exception -> 0x0c48 }
        r0 = r16;	 Catch:{ Exception -> 0x0dc0 }
        r1 = r28;	 Catch:{ Exception -> 0x0dc0 }
        r1.encryptedChat = r0;	 Catch:{ Exception -> 0x0dc0 }
        r4 = 0;	 Catch:{ Exception -> 0x0dc0 }
        r0 = r28;	 Catch:{ Exception -> 0x0dc0 }
        r0.type = r4;	 Catch:{ Exception -> 0x0dc0 }
        r0 = r51;	 Catch:{ Exception -> 0x0dc0 }
        r1 = r28;	 Catch:{ Exception -> 0x0dc0 }
        r1.originalPath = r0;	 Catch:{ Exception -> 0x0dc0 }
        r0 = r28;	 Catch:{ Exception -> 0x0dc0 }
        r0.sendEncryptedRequest = r7;	 Catch:{ Exception -> 0x0dc0 }
        r0 = r28;	 Catch:{ Exception -> 0x0dc0 }
        r0.obj = r12;	 Catch:{ Exception -> 0x0dc0 }
    L_0x1455:
        r4 = android.text.TextUtils.isEmpty(r78);	 Catch:{ Exception -> 0x0dc0 }
        if (r4 != 0) goto L_0x1486;	 Catch:{ Exception -> 0x0dc0 }
    L_0x145b:
        r4 = "http";	 Catch:{ Exception -> 0x0dc0 }
        r0 = r78;	 Catch:{ Exception -> 0x0dc0 }
        r4 = r0.startsWith(r4);	 Catch:{ Exception -> 0x0dc0 }
        if (r4 == 0) goto L_0x1486;	 Catch:{ Exception -> 0x0dc0 }
    L_0x1466:
        r0 = r78;	 Catch:{ Exception -> 0x0dc0 }
        r1 = r28;	 Catch:{ Exception -> 0x0dc0 }
        r1.httpLocation = r0;	 Catch:{ Exception -> 0x0dc0 }
    L_0x146c:
        r8 = 0;	 Catch:{ Exception -> 0x0dc0 }
        r4 = (r32 > r8 ? 1 : (r32 == r8 ? 0 : -1));	 Catch:{ Exception -> 0x0dc0 }
        if (r4 != 0) goto L_0x1336;	 Catch:{ Exception -> 0x0dc0 }
    L_0x1472:
        r0 = r67;	 Catch:{ Exception -> 0x0dc0 }
        r1 = r28;	 Catch:{ Exception -> 0x0dc0 }
        r0.performSendDelayedMessage(r1);	 Catch:{ Exception -> 0x0dc0 }
        goto L_0x1336;
    L_0x147b:
        r4 = r7.media;	 Catch:{ Exception -> 0x0c48 }
        r4 = (org.telegram.tgnet.TLRPC.TL_decryptedMessageMediaPhoto) r4;	 Catch:{ Exception -> 0x0c48 }
        r6 = 0;	 Catch:{ Exception -> 0x0c48 }
        r6 = new byte[r6];	 Catch:{ Exception -> 0x0c48 }
        r4.thumb = r6;	 Catch:{ Exception -> 0x0c48 }
        goto L_0x13f9;
    L_0x1486:
        r0 = r71;	 Catch:{ Exception -> 0x0dc0 }
        r4 = r0.sizes;	 Catch:{ Exception -> 0x0dc0 }
        r0 = r71;	 Catch:{ Exception -> 0x0dc0 }
        r6 = r0.sizes;	 Catch:{ Exception -> 0x0dc0 }
        r6 = r6.size();	 Catch:{ Exception -> 0x0dc0 }
        r6 = r6 + -1;	 Catch:{ Exception -> 0x0dc0 }
        r4 = r4.get(r6);	 Catch:{ Exception -> 0x0dc0 }
        r4 = (org.telegram.tgnet.TLRPC.PhotoSize) r4;	 Catch:{ Exception -> 0x0dc0 }
        r4 = r4.location;	 Catch:{ Exception -> 0x0dc0 }
        r0 = r28;	 Catch:{ Exception -> 0x0dc0 }
        r0.location = r4;	 Catch:{ Exception -> 0x0dc0 }
        goto L_0x146c;
    L_0x14a1:
        r10 = new org.telegram.tgnet.TLRPC$TL_inputEncryptedFile;	 Catch:{ Exception -> 0x0c48 }
        r10.<init>();	 Catch:{ Exception -> 0x0c48 }
        r0 = r25;	 Catch:{ Exception -> 0x0c48 }
        r4 = r0.location;	 Catch:{ Exception -> 0x0c48 }
        r8 = r4.volume_id;	 Catch:{ Exception -> 0x0c48 }
        r10.id = r8;	 Catch:{ Exception -> 0x0c48 }
        r0 = r25;	 Catch:{ Exception -> 0x0c48 }
        r4 = r0.location;	 Catch:{ Exception -> 0x0c48 }
        r8 = r4.secret;	 Catch:{ Exception -> 0x0c48 }
        r10.access_hash = r8;	 Catch:{ Exception -> 0x0c48 }
        r4 = r7.media;	 Catch:{ Exception -> 0x0c48 }
        r0 = r25;	 Catch:{ Exception -> 0x0c48 }
        r6 = r0.location;	 Catch:{ Exception -> 0x0c48 }
        r6 = r6.key;	 Catch:{ Exception -> 0x0c48 }
        r4.key = r6;	 Catch:{ Exception -> 0x0c48 }
        r4 = r7.media;	 Catch:{ Exception -> 0x0c48 }
        r0 = r25;	 Catch:{ Exception -> 0x0c48 }
        r6 = r0.location;	 Catch:{ Exception -> 0x0c48 }
        r6 = r6.iv;	 Catch:{ Exception -> 0x0c48 }
        r4.iv = r6;	 Catch:{ Exception -> 0x0c48 }
        r0 = r67;	 Catch:{ Exception -> 0x0c48 }
        r4 = r0.currentAccount;	 Catch:{ Exception -> 0x0c48 }
        r6 = org.telegram.messenger.SecretChatHelper.getInstance(r4);	 Catch:{ Exception -> 0x0c48 }
        r8 = r12.messageOwner;	 Catch:{ Exception -> 0x0c48 }
        r11 = 0;	 Catch:{ Exception -> 0x0c48 }
        r9 = r16;	 Catch:{ Exception -> 0x0c48 }
        r6.performSendEncryptedRequest(r7, r8, r9, r10, r11, r12);	 Catch:{ Exception -> 0x0c48 }
    L_0x14da:
        r28 = r29;	 Catch:{ Exception -> 0x0c48 }
        goto L_0x1336;	 Catch:{ Exception -> 0x0c48 }
    L_0x14de:
        r4 = 3;	 Catch:{ Exception -> 0x0c48 }
        r0 = r63;	 Catch:{ Exception -> 0x0c48 }
        if (r0 != r4) goto L_0x1639;	 Catch:{ Exception -> 0x0c48 }
    L_0x14e3:
        r0 = r74;	 Catch:{ Exception -> 0x0c48 }
        r4 = r0.thumb;	 Catch:{ Exception -> 0x0c48 }
        org.telegram.messenger.ImageLoader.fillPhotoSizeWithBytes(r4);	 Catch:{ Exception -> 0x0c48 }
        r4 = org.telegram.messenger.MessageObject.isNewGifDocument(r74);	 Catch:{ Exception -> 0x0c48 }
        if (r4 != 0) goto L_0x14f6;	 Catch:{ Exception -> 0x0c48 }
    L_0x14f0:
        r4 = org.telegram.messenger.MessageObject.isRoundVideoDocument(r74);	 Catch:{ Exception -> 0x0c48 }
        if (r4 == 0) goto L_0x15d2;	 Catch:{ Exception -> 0x0c48 }
    L_0x14f6:
        r4 = new org.telegram.tgnet.TLRPC$TL_decryptedMessageMediaDocument;	 Catch:{ Exception -> 0x0c48 }
        r4.<init>();	 Catch:{ Exception -> 0x0c48 }
        r7.media = r4;	 Catch:{ Exception -> 0x0c48 }
        r4 = r7.media;	 Catch:{ Exception -> 0x0c48 }
        r0 = r74;	 Catch:{ Exception -> 0x0c48 }
        r6 = r0.attributes;	 Catch:{ Exception -> 0x0c48 }
        r4.attributes = r6;	 Catch:{ Exception -> 0x0c48 }
        r0 = r74;	 Catch:{ Exception -> 0x0c48 }
        r4 = r0.thumb;	 Catch:{ Exception -> 0x0c48 }
        if (r4 == 0) goto L_0x15c7;	 Catch:{ Exception -> 0x0c48 }
    L_0x150b:
        r0 = r74;	 Catch:{ Exception -> 0x0c48 }
        r4 = r0.thumb;	 Catch:{ Exception -> 0x0c48 }
        r4 = r4.bytes;	 Catch:{ Exception -> 0x0c48 }
        if (r4 == 0) goto L_0x15c7;	 Catch:{ Exception -> 0x0c48 }
    L_0x1513:
        r4 = r7.media;	 Catch:{ Exception -> 0x0c48 }
        r4 = (org.telegram.tgnet.TLRPC.TL_decryptedMessageMediaDocument) r4;	 Catch:{ Exception -> 0x0c48 }
        r0 = r74;	 Catch:{ Exception -> 0x0c48 }
        r6 = r0.thumb;	 Catch:{ Exception -> 0x0c48 }
        r6 = r6.bytes;	 Catch:{ Exception -> 0x0c48 }
        r4.thumb = r6;	 Catch:{ Exception -> 0x0c48 }
    L_0x151f:
        r4 = r7.media;	 Catch:{ Exception -> 0x0c48 }
        r0 = r69;	 Catch:{ Exception -> 0x0c48 }
        r4.caption = r0;	 Catch:{ Exception -> 0x0c48 }
        r4 = r7.media;	 Catch:{ Exception -> 0x0c48 }
        r6 = "video/mp4";	 Catch:{ Exception -> 0x0c48 }
        r4.mime_type = r6;	 Catch:{ Exception -> 0x0c48 }
        r4 = r7.media;	 Catch:{ Exception -> 0x0c48 }
        r0 = r74;	 Catch:{ Exception -> 0x0c48 }
        r6 = r0.size;	 Catch:{ Exception -> 0x0c48 }
        r4.size = r6;	 Catch:{ Exception -> 0x0c48 }
        r20 = 0;	 Catch:{ Exception -> 0x0c48 }
    L_0x1536:
        r0 = r74;	 Catch:{ Exception -> 0x0c48 }
        r4 = r0.attributes;	 Catch:{ Exception -> 0x0c48 }
        r4 = r4.size();	 Catch:{ Exception -> 0x0c48 }
        r0 = r20;	 Catch:{ Exception -> 0x0c48 }
        if (r0 >= r4) goto L_0x156c;	 Catch:{ Exception -> 0x0c48 }
    L_0x1542:
        r0 = r74;	 Catch:{ Exception -> 0x0c48 }
        r4 = r0.attributes;	 Catch:{ Exception -> 0x0c48 }
        r0 = r20;	 Catch:{ Exception -> 0x0c48 }
        r23 = r4.get(r0);	 Catch:{ Exception -> 0x0c48 }
        r23 = (org.telegram.tgnet.TLRPC.DocumentAttribute) r23;	 Catch:{ Exception -> 0x0c48 }
        r0 = r23;	 Catch:{ Exception -> 0x0c48 }
        r4 = r0 instanceof org.telegram.tgnet.TLRPC.TL_documentAttributeVideo;	 Catch:{ Exception -> 0x0c48 }
        if (r4 == 0) goto L_0x1600;	 Catch:{ Exception -> 0x0c48 }
    L_0x1554:
        r4 = r7.media;	 Catch:{ Exception -> 0x0c48 }
        r0 = r23;	 Catch:{ Exception -> 0x0c48 }
        r6 = r0.f18w;	 Catch:{ Exception -> 0x0c48 }
        r4.f16w = r6;	 Catch:{ Exception -> 0x0c48 }
        r4 = r7.media;	 Catch:{ Exception -> 0x0c48 }
        r0 = r23;	 Catch:{ Exception -> 0x0c48 }
        r6 = r0.f17h;	 Catch:{ Exception -> 0x0c48 }
        r4.f15h = r6;	 Catch:{ Exception -> 0x0c48 }
        r4 = r7.media;	 Catch:{ Exception -> 0x0c48 }
        r0 = r23;	 Catch:{ Exception -> 0x0c48 }
        r6 = r0.duration;	 Catch:{ Exception -> 0x0c48 }
        r4.duration = r6;	 Catch:{ Exception -> 0x0c48 }
    L_0x156c:
        r4 = r7.media;	 Catch:{ Exception -> 0x0c48 }
        r0 = r74;	 Catch:{ Exception -> 0x0c48 }
        r6 = r0.thumb;	 Catch:{ Exception -> 0x0c48 }
        r6 = r6.f24h;	 Catch:{ Exception -> 0x0c48 }
        r4.thumb_h = r6;	 Catch:{ Exception -> 0x0c48 }
        r4 = r7.media;	 Catch:{ Exception -> 0x0c48 }
        r0 = r74;	 Catch:{ Exception -> 0x0c48 }
        r6 = r0.thumb;	 Catch:{ Exception -> 0x0c48 }
        r6 = r6.f25w;	 Catch:{ Exception -> 0x0c48 }
        r4.thumb_w = r6;	 Catch:{ Exception -> 0x0c48 }
        r0 = r74;	 Catch:{ Exception -> 0x0c48 }
        r4 = r0.key;	 Catch:{ Exception -> 0x0c48 }
        if (r4 == 0) goto L_0x158c;	 Catch:{ Exception -> 0x0c48 }
    L_0x1586:
        r8 = 0;	 Catch:{ Exception -> 0x0c48 }
        r4 = (r32 > r8 ? 1 : (r32 == r8 ? 0 : -1));	 Catch:{ Exception -> 0x0c48 }
        if (r4 == 0) goto L_0x1604;	 Catch:{ Exception -> 0x0c48 }
    L_0x158c:
        if (r29 != 0) goto L_0x1a2d;	 Catch:{ Exception -> 0x0c48 }
    L_0x158e:
        r28 = new org.telegram.messenger.SendMessagesHelper$DelayedMessage;	 Catch:{ Exception -> 0x0c48 }
        r0 = r28;	 Catch:{ Exception -> 0x0c48 }
        r1 = r67;	 Catch:{ Exception -> 0x0c48 }
        r2 = r76;	 Catch:{ Exception -> 0x0c48 }
        r0.<init>(r2);	 Catch:{ Exception -> 0x0c48 }
        r0 = r16;	 Catch:{ Exception -> 0x0dc0 }
        r1 = r28;	 Catch:{ Exception -> 0x0dc0 }
        r1.encryptedChat = r0;	 Catch:{ Exception -> 0x0dc0 }
        r4 = 1;	 Catch:{ Exception -> 0x0dc0 }
        r0 = r28;	 Catch:{ Exception -> 0x0dc0 }
        r0.type = r4;	 Catch:{ Exception -> 0x0dc0 }
        r0 = r28;	 Catch:{ Exception -> 0x0dc0 }
        r0.sendEncryptedRequest = r7;	 Catch:{ Exception -> 0x0dc0 }
        r0 = r51;	 Catch:{ Exception -> 0x0dc0 }
        r1 = r28;	 Catch:{ Exception -> 0x0dc0 }
        r1.originalPath = r0;	 Catch:{ Exception -> 0x0dc0 }
        r0 = r28;	 Catch:{ Exception -> 0x0dc0 }
        r0.obj = r12;	 Catch:{ Exception -> 0x0dc0 }
    L_0x15b2:
        r0 = r72;	 Catch:{ Exception -> 0x0dc0 }
        r1 = r28;	 Catch:{ Exception -> 0x0dc0 }
        r1.videoEditedInfo = r0;	 Catch:{ Exception -> 0x0dc0 }
        r8 = 0;	 Catch:{ Exception -> 0x0dc0 }
        r4 = (r32 > r8 ? 1 : (r32 == r8 ? 0 : -1));	 Catch:{ Exception -> 0x0dc0 }
        if (r4 != 0) goto L_0x1336;	 Catch:{ Exception -> 0x0dc0 }
    L_0x15be:
        r0 = r67;	 Catch:{ Exception -> 0x0dc0 }
        r1 = r28;	 Catch:{ Exception -> 0x0dc0 }
        r0.performSendDelayedMessage(r1);	 Catch:{ Exception -> 0x0dc0 }
        goto L_0x1336;
    L_0x15c7:
        r4 = r7.media;	 Catch:{ Exception -> 0x0c48 }
        r4 = (org.telegram.tgnet.TLRPC.TL_decryptedMessageMediaDocument) r4;	 Catch:{ Exception -> 0x0c48 }
        r6 = 0;	 Catch:{ Exception -> 0x0c48 }
        r6 = new byte[r6];	 Catch:{ Exception -> 0x0c48 }
        r4.thumb = r6;	 Catch:{ Exception -> 0x0c48 }
        goto L_0x151f;	 Catch:{ Exception -> 0x0c48 }
    L_0x15d2:
        r4 = new org.telegram.tgnet.TLRPC$TL_decryptedMessageMediaVideo;	 Catch:{ Exception -> 0x0c48 }
        r4.<init>();	 Catch:{ Exception -> 0x0c48 }
        r7.media = r4;	 Catch:{ Exception -> 0x0c48 }
        r0 = r74;	 Catch:{ Exception -> 0x0c48 }
        r4 = r0.thumb;	 Catch:{ Exception -> 0x0c48 }
        if (r4 == 0) goto L_0x15f5;	 Catch:{ Exception -> 0x0c48 }
    L_0x15df:
        r0 = r74;	 Catch:{ Exception -> 0x0c48 }
        r4 = r0.thumb;	 Catch:{ Exception -> 0x0c48 }
        r4 = r4.bytes;	 Catch:{ Exception -> 0x0c48 }
        if (r4 == 0) goto L_0x15f5;	 Catch:{ Exception -> 0x0c48 }
    L_0x15e7:
        r4 = r7.media;	 Catch:{ Exception -> 0x0c48 }
        r4 = (org.telegram.tgnet.TLRPC.TL_decryptedMessageMediaVideo) r4;	 Catch:{ Exception -> 0x0c48 }
        r0 = r74;	 Catch:{ Exception -> 0x0c48 }
        r6 = r0.thumb;	 Catch:{ Exception -> 0x0c48 }
        r6 = r6.bytes;	 Catch:{ Exception -> 0x0c48 }
        r4.thumb = r6;	 Catch:{ Exception -> 0x0c48 }
        goto L_0x151f;	 Catch:{ Exception -> 0x0c48 }
    L_0x15f5:
        r4 = r7.media;	 Catch:{ Exception -> 0x0c48 }
        r4 = (org.telegram.tgnet.TLRPC.TL_decryptedMessageMediaVideo) r4;	 Catch:{ Exception -> 0x0c48 }
        r6 = 0;	 Catch:{ Exception -> 0x0c48 }
        r6 = new byte[r6];	 Catch:{ Exception -> 0x0c48 }
        r4.thumb = r6;	 Catch:{ Exception -> 0x0c48 }
        goto L_0x151f;	 Catch:{ Exception -> 0x0c48 }
    L_0x1600:
        r20 = r20 + 1;	 Catch:{ Exception -> 0x0c48 }
        goto L_0x1536;	 Catch:{ Exception -> 0x0c48 }
    L_0x1604:
        r10 = new org.telegram.tgnet.TLRPC$TL_inputEncryptedFile;	 Catch:{ Exception -> 0x0c48 }
        r10.<init>();	 Catch:{ Exception -> 0x0c48 }
        r0 = r74;	 Catch:{ Exception -> 0x0c48 }
        r8 = r0.id;	 Catch:{ Exception -> 0x0c48 }
        r10.id = r8;	 Catch:{ Exception -> 0x0c48 }
        r0 = r74;	 Catch:{ Exception -> 0x0c48 }
        r8 = r0.access_hash;	 Catch:{ Exception -> 0x0c48 }
        r10.access_hash = r8;	 Catch:{ Exception -> 0x0c48 }
        r4 = r7.media;	 Catch:{ Exception -> 0x0c48 }
        r0 = r74;	 Catch:{ Exception -> 0x0c48 }
        r6 = r0.key;	 Catch:{ Exception -> 0x0c48 }
        r4.key = r6;	 Catch:{ Exception -> 0x0c48 }
        r4 = r7.media;	 Catch:{ Exception -> 0x0c48 }
        r0 = r74;	 Catch:{ Exception -> 0x0c48 }
        r6 = r0.iv;	 Catch:{ Exception -> 0x0c48 }
        r4.iv = r6;	 Catch:{ Exception -> 0x0c48 }
        r0 = r67;	 Catch:{ Exception -> 0x0c48 }
        r4 = r0.currentAccount;	 Catch:{ Exception -> 0x0c48 }
        r6 = org.telegram.messenger.SecretChatHelper.getInstance(r4);	 Catch:{ Exception -> 0x0c48 }
        r8 = r12.messageOwner;	 Catch:{ Exception -> 0x0c48 }
        r11 = 0;	 Catch:{ Exception -> 0x0c48 }
        r9 = r16;	 Catch:{ Exception -> 0x0c48 }
        r6.performSendEncryptedRequest(r7, r8, r9, r10, r11, r12);	 Catch:{ Exception -> 0x0c48 }
        r28 = r29;	 Catch:{ Exception -> 0x0c48 }
        goto L_0x1336;	 Catch:{ Exception -> 0x0c48 }
    L_0x1639:
        r4 = 6;	 Catch:{ Exception -> 0x0c48 }
        r0 = r63;	 Catch:{ Exception -> 0x0c48 }
        if (r0 != r4) goto L_0x167d;	 Catch:{ Exception -> 0x0c48 }
    L_0x163e:
        r4 = new org.telegram.tgnet.TLRPC$TL_decryptedMessageMediaContact;	 Catch:{ Exception -> 0x0c48 }
        r4.<init>();	 Catch:{ Exception -> 0x0c48 }
        r7.media = r4;	 Catch:{ Exception -> 0x0c48 }
        r4 = r7.media;	 Catch:{ Exception -> 0x0c48 }
        r0 = r73;	 Catch:{ Exception -> 0x0c48 }
        r6 = r0.phone;	 Catch:{ Exception -> 0x0c48 }
        r4.phone_number = r6;	 Catch:{ Exception -> 0x0c48 }
        r4 = r7.media;	 Catch:{ Exception -> 0x0c48 }
        r0 = r73;	 Catch:{ Exception -> 0x0c48 }
        r6 = r0.first_name;	 Catch:{ Exception -> 0x0c48 }
        r4.first_name = r6;	 Catch:{ Exception -> 0x0c48 }
        r4 = r7.media;	 Catch:{ Exception -> 0x0c48 }
        r0 = r73;	 Catch:{ Exception -> 0x0c48 }
        r6 = r0.last_name;	 Catch:{ Exception -> 0x0c48 }
        r4.last_name = r6;	 Catch:{ Exception -> 0x0c48 }
        r4 = r7.media;	 Catch:{ Exception -> 0x0c48 }
        r0 = r73;	 Catch:{ Exception -> 0x0c48 }
        r6 = r0.id;	 Catch:{ Exception -> 0x0c48 }
        r4.user_id = r6;	 Catch:{ Exception -> 0x0c48 }
        r0 = r67;	 Catch:{ Exception -> 0x0c48 }
        r4 = r0.currentAccount;	 Catch:{ Exception -> 0x0c48 }
        r13 = org.telegram.messenger.SecretChatHelper.getInstance(r4);	 Catch:{ Exception -> 0x0c48 }
        r15 = r12.messageOwner;	 Catch:{ Exception -> 0x0c48 }
        r17 = 0;	 Catch:{ Exception -> 0x0c48 }
        r18 = 0;	 Catch:{ Exception -> 0x0c48 }
        r14 = r7;	 Catch:{ Exception -> 0x0c48 }
        r19 = r12;	 Catch:{ Exception -> 0x0c48 }
        r13.performSendEncryptedRequest(r14, r15, r16, r17, r18, r19);	 Catch:{ Exception -> 0x0c48 }
        r28 = r29;	 Catch:{ Exception -> 0x0c48 }
        goto L_0x1336;	 Catch:{ Exception -> 0x0c48 }
    L_0x167d:
        r4 = 7;	 Catch:{ Exception -> 0x0c48 }
        r0 = r63;	 Catch:{ Exception -> 0x0c48 }
        if (r0 == r4) goto L_0x168a;	 Catch:{ Exception -> 0x0c48 }
    L_0x1682:
        r4 = 9;	 Catch:{ Exception -> 0x0c48 }
        r0 = r63;	 Catch:{ Exception -> 0x0c48 }
        if (r0 != r4) goto L_0x17fd;	 Catch:{ Exception -> 0x0c48 }
    L_0x1688:
        if (r74 == 0) goto L_0x17fd;	 Catch:{ Exception -> 0x0c48 }
    L_0x168a:
        r4 = org.telegram.messenger.MessageObject.isStickerDocument(r74);	 Catch:{ Exception -> 0x0c48 }
        if (r4 == 0) goto L_0x170e;	 Catch:{ Exception -> 0x0c48 }
    L_0x1690:
        r4 = new org.telegram.tgnet.TLRPC$TL_decryptedMessageMediaExternalDocument;	 Catch:{ Exception -> 0x0c48 }
        r4.<init>();	 Catch:{ Exception -> 0x0c48 }
        r7.media = r4;	 Catch:{ Exception -> 0x0c48 }
        r4 = r7.media;	 Catch:{ Exception -> 0x0c48 }
        r0 = r74;	 Catch:{ Exception -> 0x0c48 }
        r8 = r0.id;	 Catch:{ Exception -> 0x0c48 }
        r4.id = r8;	 Catch:{ Exception -> 0x0c48 }
        r4 = r7.media;	 Catch:{ Exception -> 0x0c48 }
        r0 = r74;	 Catch:{ Exception -> 0x0c48 }
        r6 = r0.date;	 Catch:{ Exception -> 0x0c48 }
        r4.date = r6;	 Catch:{ Exception -> 0x0c48 }
        r4 = r7.media;	 Catch:{ Exception -> 0x0c48 }
        r0 = r74;	 Catch:{ Exception -> 0x0c48 }
        r8 = r0.access_hash;	 Catch:{ Exception -> 0x0c48 }
        r4.access_hash = r8;	 Catch:{ Exception -> 0x0c48 }
        r4 = r7.media;	 Catch:{ Exception -> 0x0c48 }
        r0 = r74;	 Catch:{ Exception -> 0x0c48 }
        r6 = r0.mime_type;	 Catch:{ Exception -> 0x0c48 }
        r4.mime_type = r6;	 Catch:{ Exception -> 0x0c48 }
        r4 = r7.media;	 Catch:{ Exception -> 0x0c48 }
        r0 = r74;	 Catch:{ Exception -> 0x0c48 }
        r6 = r0.size;	 Catch:{ Exception -> 0x0c48 }
        r4.size = r6;	 Catch:{ Exception -> 0x0c48 }
        r4 = r7.media;	 Catch:{ Exception -> 0x0c48 }
        r0 = r74;	 Catch:{ Exception -> 0x0c48 }
        r6 = r0.dc_id;	 Catch:{ Exception -> 0x0c48 }
        r4.dc_id = r6;	 Catch:{ Exception -> 0x0c48 }
        r4 = r7.media;	 Catch:{ Exception -> 0x0c48 }
        r0 = r74;	 Catch:{ Exception -> 0x0c48 }
        r6 = r0.attributes;	 Catch:{ Exception -> 0x0c48 }
        r4.attributes = r6;	 Catch:{ Exception -> 0x0c48 }
        r0 = r74;	 Catch:{ Exception -> 0x0c48 }
        r4 = r0.thumb;	 Catch:{ Exception -> 0x0c48 }
        if (r4 != 0) goto L_0x1703;	 Catch:{ Exception -> 0x0c48 }
    L_0x16d5:
        r4 = r7.media;	 Catch:{ Exception -> 0x0c48 }
        r4 = (org.telegram.tgnet.TLRPC.TL_decryptedMessageMediaExternalDocument) r4;	 Catch:{ Exception -> 0x0c48 }
        r6 = new org.telegram.tgnet.TLRPC$TL_photoSizeEmpty;	 Catch:{ Exception -> 0x0c48 }
        r6.<init>();	 Catch:{ Exception -> 0x0c48 }
        r4.thumb = r6;	 Catch:{ Exception -> 0x0c48 }
        r4 = r7.media;	 Catch:{ Exception -> 0x0c48 }
        r4 = (org.telegram.tgnet.TLRPC.TL_decryptedMessageMediaExternalDocument) r4;	 Catch:{ Exception -> 0x0c48 }
        r4 = r4.thumb;	 Catch:{ Exception -> 0x0c48 }
        r6 = "s";	 Catch:{ Exception -> 0x0c48 }
        r4.type = r6;	 Catch:{ Exception -> 0x0c48 }
    L_0x16eb:
        r0 = r67;	 Catch:{ Exception -> 0x0c48 }
        r4 = r0.currentAccount;	 Catch:{ Exception -> 0x0c48 }
        r13 = org.telegram.messenger.SecretChatHelper.getInstance(r4);	 Catch:{ Exception -> 0x0c48 }
        r15 = r12.messageOwner;	 Catch:{ Exception -> 0x0c48 }
        r17 = 0;	 Catch:{ Exception -> 0x0c48 }
        r18 = 0;	 Catch:{ Exception -> 0x0c48 }
        r14 = r7;	 Catch:{ Exception -> 0x0c48 }
        r19 = r12;	 Catch:{ Exception -> 0x0c48 }
        r13.performSendEncryptedRequest(r14, r15, r16, r17, r18, r19);	 Catch:{ Exception -> 0x0c48 }
        r28 = r29;	 Catch:{ Exception -> 0x0c48 }
        goto L_0x1336;	 Catch:{ Exception -> 0x0c48 }
    L_0x1703:
        r4 = r7.media;	 Catch:{ Exception -> 0x0c48 }
        r4 = (org.telegram.tgnet.TLRPC.TL_decryptedMessageMediaExternalDocument) r4;	 Catch:{ Exception -> 0x0c48 }
        r0 = r74;	 Catch:{ Exception -> 0x0c48 }
        r6 = r0.thumb;	 Catch:{ Exception -> 0x0c48 }
        r4.thumb = r6;	 Catch:{ Exception -> 0x0c48 }
        goto L_0x16eb;	 Catch:{ Exception -> 0x0c48 }
    L_0x170e:
        r0 = r74;	 Catch:{ Exception -> 0x0c48 }
        r4 = r0.thumb;	 Catch:{ Exception -> 0x0c48 }
        org.telegram.messenger.ImageLoader.fillPhotoSizeWithBytes(r4);	 Catch:{ Exception -> 0x0c48 }
        r4 = new org.telegram.tgnet.TLRPC$TL_decryptedMessageMediaDocument;	 Catch:{ Exception -> 0x0c48 }
        r4.<init>();	 Catch:{ Exception -> 0x0c48 }
        r7.media = r4;	 Catch:{ Exception -> 0x0c48 }
        r4 = r7.media;	 Catch:{ Exception -> 0x0c48 }
        r0 = r74;	 Catch:{ Exception -> 0x0c48 }
        r6 = r0.attributes;	 Catch:{ Exception -> 0x0c48 }
        r4.attributes = r6;	 Catch:{ Exception -> 0x0c48 }
        r4 = r7.media;	 Catch:{ Exception -> 0x0c48 }
        r0 = r69;	 Catch:{ Exception -> 0x0c48 }
        r4.caption = r0;	 Catch:{ Exception -> 0x0c48 }
        r0 = r74;	 Catch:{ Exception -> 0x0c48 }
        r4 = r0.thumb;	 Catch:{ Exception -> 0x0c48 }
        if (r4 == 0) goto L_0x17b4;	 Catch:{ Exception -> 0x0c48 }
    L_0x1730:
        r0 = r74;	 Catch:{ Exception -> 0x0c48 }
        r4 = r0.thumb;	 Catch:{ Exception -> 0x0c48 }
        r4 = r4.bytes;	 Catch:{ Exception -> 0x0c48 }
        if (r4 == 0) goto L_0x17b4;	 Catch:{ Exception -> 0x0c48 }
    L_0x1738:
        r4 = r7.media;	 Catch:{ Exception -> 0x0c48 }
        r4 = (org.telegram.tgnet.TLRPC.TL_decryptedMessageMediaDocument) r4;	 Catch:{ Exception -> 0x0c48 }
        r0 = r74;	 Catch:{ Exception -> 0x0c48 }
        r6 = r0.thumb;	 Catch:{ Exception -> 0x0c48 }
        r6 = r6.bytes;	 Catch:{ Exception -> 0x0c48 }
        r4.thumb = r6;	 Catch:{ Exception -> 0x0c48 }
        r4 = r7.media;	 Catch:{ Exception -> 0x0c48 }
        r0 = r74;	 Catch:{ Exception -> 0x0c48 }
        r6 = r0.thumb;	 Catch:{ Exception -> 0x0c48 }
        r6 = r6.f24h;	 Catch:{ Exception -> 0x0c48 }
        r4.thumb_h = r6;	 Catch:{ Exception -> 0x0c48 }
        r4 = r7.media;	 Catch:{ Exception -> 0x0c48 }
        r0 = r74;	 Catch:{ Exception -> 0x0c48 }
        r6 = r0.thumb;	 Catch:{ Exception -> 0x0c48 }
        r6 = r6.f25w;	 Catch:{ Exception -> 0x0c48 }
        r4.thumb_w = r6;	 Catch:{ Exception -> 0x0c48 }
    L_0x1758:
        r4 = r7.media;	 Catch:{ Exception -> 0x0c48 }
        r0 = r74;	 Catch:{ Exception -> 0x0c48 }
        r6 = r0.size;	 Catch:{ Exception -> 0x0c48 }
        r4.size = r6;	 Catch:{ Exception -> 0x0c48 }
        r4 = r7.media;	 Catch:{ Exception -> 0x0c48 }
        r0 = r74;	 Catch:{ Exception -> 0x0c48 }
        r6 = r0.mime_type;	 Catch:{ Exception -> 0x0c48 }
        r4.mime_type = r6;	 Catch:{ Exception -> 0x0c48 }
        r0 = r74;	 Catch:{ Exception -> 0x0c48 }
        r4 = r0.key;	 Catch:{ Exception -> 0x0c48 }
        if (r4 != 0) goto L_0x17c8;	 Catch:{ Exception -> 0x0c48 }
    L_0x176e:
        r28 = new org.telegram.messenger.SendMessagesHelper$DelayedMessage;	 Catch:{ Exception -> 0x0c48 }
        r0 = r28;	 Catch:{ Exception -> 0x0c48 }
        r1 = r67;	 Catch:{ Exception -> 0x0c48 }
        r2 = r76;	 Catch:{ Exception -> 0x0c48 }
        r0.<init>(r2);	 Catch:{ Exception -> 0x0c48 }
        r0 = r51;	 Catch:{ Exception -> 0x0dc0 }
        r1 = r28;	 Catch:{ Exception -> 0x0dc0 }
        r1.originalPath = r0;	 Catch:{ Exception -> 0x0dc0 }
        r0 = r28;	 Catch:{ Exception -> 0x0dc0 }
        r0.sendEncryptedRequest = r7;	 Catch:{ Exception -> 0x0dc0 }
        r4 = 2;	 Catch:{ Exception -> 0x0dc0 }
        r0 = r28;	 Catch:{ Exception -> 0x0dc0 }
        r0.type = r4;	 Catch:{ Exception -> 0x0dc0 }
        r0 = r28;	 Catch:{ Exception -> 0x0dc0 }
        r0.obj = r12;	 Catch:{ Exception -> 0x0dc0 }
        r0 = r16;	 Catch:{ Exception -> 0x0dc0 }
        r1 = r28;	 Catch:{ Exception -> 0x0dc0 }
        r1.encryptedChat = r0;	 Catch:{ Exception -> 0x0dc0 }
        if (r78 == 0) goto L_0x17ab;	 Catch:{ Exception -> 0x0dc0 }
    L_0x1794:
        r4 = r78.length();	 Catch:{ Exception -> 0x0dc0 }
        if (r4 <= 0) goto L_0x17ab;	 Catch:{ Exception -> 0x0dc0 }
    L_0x179a:
        r4 = "http";	 Catch:{ Exception -> 0x0dc0 }
        r0 = r78;	 Catch:{ Exception -> 0x0dc0 }
        r4 = r0.startsWith(r4);	 Catch:{ Exception -> 0x0dc0 }
        if (r4 == 0) goto L_0x17ab;	 Catch:{ Exception -> 0x0dc0 }
    L_0x17a5:
        r0 = r78;	 Catch:{ Exception -> 0x0dc0 }
        r1 = r28;	 Catch:{ Exception -> 0x0dc0 }
        r1.httpLocation = r0;	 Catch:{ Exception -> 0x0dc0 }
    L_0x17ab:
        r0 = r67;	 Catch:{ Exception -> 0x0dc0 }
        r1 = r28;	 Catch:{ Exception -> 0x0dc0 }
        r0.performSendDelayedMessage(r1);	 Catch:{ Exception -> 0x0dc0 }
        goto L_0x1336;
    L_0x17b4:
        r4 = r7.media;	 Catch:{ Exception -> 0x0c48 }
        r4 = (org.telegram.tgnet.TLRPC.TL_decryptedMessageMediaDocument) r4;	 Catch:{ Exception -> 0x0c48 }
        r6 = 0;	 Catch:{ Exception -> 0x0c48 }
        r6 = new byte[r6];	 Catch:{ Exception -> 0x0c48 }
        r4.thumb = r6;	 Catch:{ Exception -> 0x0c48 }
        r4 = r7.media;	 Catch:{ Exception -> 0x0c48 }
        r6 = 0;	 Catch:{ Exception -> 0x0c48 }
        r4.thumb_h = r6;	 Catch:{ Exception -> 0x0c48 }
        r4 = r7.media;	 Catch:{ Exception -> 0x0c48 }
        r6 = 0;	 Catch:{ Exception -> 0x0c48 }
        r4.thumb_w = r6;	 Catch:{ Exception -> 0x0c48 }
        goto L_0x1758;	 Catch:{ Exception -> 0x0c48 }
    L_0x17c8:
        r10 = new org.telegram.tgnet.TLRPC$TL_inputEncryptedFile;	 Catch:{ Exception -> 0x0c48 }
        r10.<init>();	 Catch:{ Exception -> 0x0c48 }
        r0 = r74;	 Catch:{ Exception -> 0x0c48 }
        r8 = r0.id;	 Catch:{ Exception -> 0x0c48 }
        r10.id = r8;	 Catch:{ Exception -> 0x0c48 }
        r0 = r74;	 Catch:{ Exception -> 0x0c48 }
        r8 = r0.access_hash;	 Catch:{ Exception -> 0x0c48 }
        r10.access_hash = r8;	 Catch:{ Exception -> 0x0c48 }
        r4 = r7.media;	 Catch:{ Exception -> 0x0c48 }
        r0 = r74;	 Catch:{ Exception -> 0x0c48 }
        r6 = r0.key;	 Catch:{ Exception -> 0x0c48 }
        r4.key = r6;	 Catch:{ Exception -> 0x0c48 }
        r4 = r7.media;	 Catch:{ Exception -> 0x0c48 }
        r0 = r74;	 Catch:{ Exception -> 0x0c48 }
        r6 = r0.iv;	 Catch:{ Exception -> 0x0c48 }
        r4.iv = r6;	 Catch:{ Exception -> 0x0c48 }
        r0 = r67;	 Catch:{ Exception -> 0x0c48 }
        r4 = r0.currentAccount;	 Catch:{ Exception -> 0x0c48 }
        r6 = org.telegram.messenger.SecretChatHelper.getInstance(r4);	 Catch:{ Exception -> 0x0c48 }
        r8 = r12.messageOwner;	 Catch:{ Exception -> 0x0c48 }
        r11 = 0;	 Catch:{ Exception -> 0x0c48 }
        r9 = r16;	 Catch:{ Exception -> 0x0c48 }
        r6.performSendEncryptedRequest(r7, r8, r9, r10, r11, r12);	 Catch:{ Exception -> 0x0c48 }
        r28 = r29;	 Catch:{ Exception -> 0x0c48 }
        goto L_0x1336;	 Catch:{ Exception -> 0x0c48 }
    L_0x17fd:
        r4 = 8;	 Catch:{ Exception -> 0x0c48 }
        r0 = r63;	 Catch:{ Exception -> 0x0c48 }
        if (r0 != r4) goto L_0x14da;	 Catch:{ Exception -> 0x0c48 }
    L_0x1803:
        r28 = new org.telegram.messenger.SendMessagesHelper$DelayedMessage;	 Catch:{ Exception -> 0x0c48 }
        r0 = r28;	 Catch:{ Exception -> 0x0c48 }
        r1 = r67;	 Catch:{ Exception -> 0x0c48 }
        r2 = r76;	 Catch:{ Exception -> 0x0c48 }
        r0.<init>(r2);	 Catch:{ Exception -> 0x0c48 }
        r0 = r16;	 Catch:{ Exception -> 0x0dc0 }
        r1 = r28;	 Catch:{ Exception -> 0x0dc0 }
        r1.encryptedChat = r0;	 Catch:{ Exception -> 0x0dc0 }
        r0 = r28;	 Catch:{ Exception -> 0x0dc0 }
        r0.sendEncryptedRequest = r7;	 Catch:{ Exception -> 0x0dc0 }
        r0 = r28;	 Catch:{ Exception -> 0x0dc0 }
        r0.obj = r12;	 Catch:{ Exception -> 0x0dc0 }
        r4 = 3;	 Catch:{ Exception -> 0x0dc0 }
        r0 = r28;	 Catch:{ Exception -> 0x0dc0 }
        r0.type = r4;	 Catch:{ Exception -> 0x0dc0 }
        r4 = new org.telegram.tgnet.TLRPC$TL_decryptedMessageMediaDocument;	 Catch:{ Exception -> 0x0dc0 }
        r4.<init>();	 Catch:{ Exception -> 0x0dc0 }
        r7.media = r4;	 Catch:{ Exception -> 0x0dc0 }
        r4 = r7.media;	 Catch:{ Exception -> 0x0dc0 }
        r0 = r74;	 Catch:{ Exception -> 0x0dc0 }
        r6 = r0.attributes;	 Catch:{ Exception -> 0x0dc0 }
        r4.attributes = r6;	 Catch:{ Exception -> 0x0dc0 }
        r4 = r7.media;	 Catch:{ Exception -> 0x0dc0 }
        r0 = r69;	 Catch:{ Exception -> 0x0dc0 }
        r4.caption = r0;	 Catch:{ Exception -> 0x0dc0 }
        r0 = r74;	 Catch:{ Exception -> 0x0dc0 }
        r4 = r0.thumb;	 Catch:{ Exception -> 0x0dc0 }
        if (r4 == 0) goto L_0x1883;	 Catch:{ Exception -> 0x0dc0 }
    L_0x183c:
        r0 = r74;	 Catch:{ Exception -> 0x0dc0 }
        r4 = r0.thumb;	 Catch:{ Exception -> 0x0dc0 }
        r4 = r4.bytes;	 Catch:{ Exception -> 0x0dc0 }
        if (r4 == 0) goto L_0x1883;	 Catch:{ Exception -> 0x0dc0 }
    L_0x1844:
        r4 = r7.media;	 Catch:{ Exception -> 0x0dc0 }
        r4 = (org.telegram.tgnet.TLRPC.TL_decryptedMessageMediaDocument) r4;	 Catch:{ Exception -> 0x0dc0 }
        r0 = r74;	 Catch:{ Exception -> 0x0dc0 }
        r6 = r0.thumb;	 Catch:{ Exception -> 0x0dc0 }
        r6 = r6.bytes;	 Catch:{ Exception -> 0x0dc0 }
        r4.thumb = r6;	 Catch:{ Exception -> 0x0dc0 }
        r4 = r7.media;	 Catch:{ Exception -> 0x0dc0 }
        r0 = r74;	 Catch:{ Exception -> 0x0dc0 }
        r6 = r0.thumb;	 Catch:{ Exception -> 0x0dc0 }
        r6 = r6.f24h;	 Catch:{ Exception -> 0x0dc0 }
        r4.thumb_h = r6;	 Catch:{ Exception -> 0x0dc0 }
        r4 = r7.media;	 Catch:{ Exception -> 0x0dc0 }
        r0 = r74;	 Catch:{ Exception -> 0x0dc0 }
        r6 = r0.thumb;	 Catch:{ Exception -> 0x0dc0 }
        r6 = r6.f25w;	 Catch:{ Exception -> 0x0dc0 }
        r4.thumb_w = r6;	 Catch:{ Exception -> 0x0dc0 }
    L_0x1864:
        r4 = r7.media;	 Catch:{ Exception -> 0x0dc0 }
        r0 = r74;	 Catch:{ Exception -> 0x0dc0 }
        r6 = r0.mime_type;	 Catch:{ Exception -> 0x0dc0 }
        r4.mime_type = r6;	 Catch:{ Exception -> 0x0dc0 }
        r4 = r7.media;	 Catch:{ Exception -> 0x0dc0 }
        r0 = r74;	 Catch:{ Exception -> 0x0dc0 }
        r6 = r0.size;	 Catch:{ Exception -> 0x0dc0 }
        r4.size = r6;	 Catch:{ Exception -> 0x0dc0 }
        r0 = r51;	 Catch:{ Exception -> 0x0dc0 }
        r1 = r28;	 Catch:{ Exception -> 0x0dc0 }
        r1.originalPath = r0;	 Catch:{ Exception -> 0x0dc0 }
        r0 = r67;	 Catch:{ Exception -> 0x0dc0 }
        r1 = r28;	 Catch:{ Exception -> 0x0dc0 }
        r0.performSendDelayedMessage(r1);	 Catch:{ Exception -> 0x0dc0 }
        goto L_0x1336;	 Catch:{ Exception -> 0x0dc0 }
    L_0x1883:
        r4 = r7.media;	 Catch:{ Exception -> 0x0dc0 }
        r4 = (org.telegram.tgnet.TLRPC.TL_decryptedMessageMediaDocument) r4;	 Catch:{ Exception -> 0x0dc0 }
        r6 = 0;	 Catch:{ Exception -> 0x0dc0 }
        r6 = new byte[r6];	 Catch:{ Exception -> 0x0dc0 }
        r4.thumb = r6;	 Catch:{ Exception -> 0x0dc0 }
        r4 = r7.media;	 Catch:{ Exception -> 0x0dc0 }
        r6 = 0;	 Catch:{ Exception -> 0x0dc0 }
        r4.thumb_h = r6;	 Catch:{ Exception -> 0x0dc0 }
        r4 = r7.media;	 Catch:{ Exception -> 0x0dc0 }
        r6 = 0;	 Catch:{ Exception -> 0x0dc0 }
        r4.thumb_w = r6;	 Catch:{ Exception -> 0x0dc0 }
        goto L_0x1864;	 Catch:{ Exception -> 0x0dc0 }
    L_0x1897:
        r56 = new org.telegram.tgnet.TLRPC$TL_messages_sendEncryptedMultiMedia;	 Catch:{ Exception -> 0x0dc0 }
        r56.<init>();	 Catch:{ Exception -> 0x0dc0 }
        r0 = r56;	 Catch:{ Exception -> 0x0dc0 }
        r1 = r28;	 Catch:{ Exception -> 0x0dc0 }
        r1.sendEncryptedRequest = r0;	 Catch:{ Exception -> 0x0dc0 }
        goto L_0x134a;
    L_0x18a4:
        r8 = 0;
        goto L_0x137b;
    L_0x18a8:
        r4 = 4;
        r0 = r63;
        if (r0 != r4) goto L_0x198c;
    L_0x18ad:
        r7 = new org.telegram.tgnet.TLRPC$TL_messages_forwardMessages;	 Catch:{ Exception -> 0x0c48 }
        r7.<init>();	 Catch:{ Exception -> 0x0c48 }
        r0 = r57;	 Catch:{ Exception -> 0x0c48 }
        r7.to_peer = r0;	 Catch:{ Exception -> 0x0c48 }
        r0 = r82;	 Catch:{ Exception -> 0x0c48 }
        r4 = r0.messageOwner;	 Catch:{ Exception -> 0x0c48 }
        r4 = r4.with_my_score;	 Catch:{ Exception -> 0x0c48 }
        r7.with_my_score = r4;	 Catch:{ Exception -> 0x0c48 }
        r0 = r82;	 Catch:{ Exception -> 0x0c48 }
        r4 = r0.messageOwner;	 Catch:{ Exception -> 0x0c48 }
        r4 = r4.ttl;	 Catch:{ Exception -> 0x0c48 }
        if (r4 == 0) goto L_0x1952;	 Catch:{ Exception -> 0x0c48 }
    L_0x18c6:
        r0 = r67;	 Catch:{ Exception -> 0x0c48 }
        r4 = r0.currentAccount;	 Catch:{ Exception -> 0x0c48 }
        r4 = org.telegram.messenger.MessagesController.getInstance(r4);	 Catch:{ Exception -> 0x0c48 }
        r0 = r82;	 Catch:{ Exception -> 0x0c48 }
        r6 = r0.messageOwner;	 Catch:{ Exception -> 0x0c48 }
        r6 = r6.ttl;	 Catch:{ Exception -> 0x0c48 }
        r6 = -r6;	 Catch:{ Exception -> 0x0c48 }
        r6 = java.lang.Integer.valueOf(r6);	 Catch:{ Exception -> 0x0c48 }
        r26 = r4.getChat(r6);	 Catch:{ Exception -> 0x0c48 }
        r4 = new org.telegram.tgnet.TLRPC$TL_inputPeerChannel;	 Catch:{ Exception -> 0x0c48 }
        r4.<init>();	 Catch:{ Exception -> 0x0c48 }
        r7.from_peer = r4;	 Catch:{ Exception -> 0x0c48 }
        r4 = r7.from_peer;	 Catch:{ Exception -> 0x0c48 }
        r0 = r82;	 Catch:{ Exception -> 0x0c48 }
        r6 = r0.messageOwner;	 Catch:{ Exception -> 0x0c48 }
        r6 = r6.ttl;	 Catch:{ Exception -> 0x0c48 }
        r6 = -r6;	 Catch:{ Exception -> 0x0c48 }
        r4.channel_id = r6;	 Catch:{ Exception -> 0x0c48 }
        if (r26 == 0) goto L_0x18f9;	 Catch:{ Exception -> 0x0c48 }
    L_0x18f1:
        r4 = r7.from_peer;	 Catch:{ Exception -> 0x0c48 }
        r0 = r26;	 Catch:{ Exception -> 0x0c48 }
        r8 = r0.access_hash;	 Catch:{ Exception -> 0x0c48 }
        r4.access_hash = r8;	 Catch:{ Exception -> 0x0c48 }
    L_0x18f9:
        r0 = r82;	 Catch:{ Exception -> 0x0c48 }
        r4 = r0.messageOwner;	 Catch:{ Exception -> 0x0c48 }
        r4 = r4.to_id;	 Catch:{ Exception -> 0x0c48 }
        r4 = r4 instanceof org.telegram.tgnet.TLRPC.TL_peerChannel;	 Catch:{ Exception -> 0x0c48 }
        if (r4 == 0) goto L_0x1928;	 Catch:{ Exception -> 0x0c48 }
    L_0x1903:
        r0 = r67;	 Catch:{ Exception -> 0x0c48 }
        r4 = r0.currentAccount;	 Catch:{ Exception -> 0x0c48 }
        r4 = org.telegram.messenger.MessagesController.getNotificationsSettings(r4);	 Catch:{ Exception -> 0x0c48 }
        r6 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x0c48 }
        r6.<init>();	 Catch:{ Exception -> 0x0c48 }
        r8 = "silent_";	 Catch:{ Exception -> 0x0c48 }
        r6 = r6.append(r8);	 Catch:{ Exception -> 0x0c48 }
        r0 = r76;	 Catch:{ Exception -> 0x0c48 }
        r6 = r6.append(r0);	 Catch:{ Exception -> 0x0c48 }
        r6 = r6.toString();	 Catch:{ Exception -> 0x0c48 }
        r8 = 0;	 Catch:{ Exception -> 0x0c48 }
        r4 = r4.getBoolean(r6, r8);	 Catch:{ Exception -> 0x0c48 }
        r7.silent = r4;	 Catch:{ Exception -> 0x0c48 }
    L_0x1928:
        r4 = r7.random_id;	 Catch:{ Exception -> 0x0c48 }
        r0 = r46;	 Catch:{ Exception -> 0x0c48 }
        r8 = r0.random_id;	 Catch:{ Exception -> 0x0c48 }
        r6 = java.lang.Long.valueOf(r8);	 Catch:{ Exception -> 0x0c48 }
        r4.add(r6);	 Catch:{ Exception -> 0x0c48 }
        r4 = r82.getId();	 Catch:{ Exception -> 0x0c48 }
        if (r4 < 0) goto L_0x195a;	 Catch:{ Exception -> 0x0c48 }
    L_0x193b:
        r4 = r7.id;	 Catch:{ Exception -> 0x0c48 }
        r6 = r82.getId();	 Catch:{ Exception -> 0x0c48 }
        r6 = java.lang.Integer.valueOf(r6);	 Catch:{ Exception -> 0x0c48 }
        r4.add(r6);	 Catch:{ Exception -> 0x0c48 }
    L_0x1948:
        r4 = 0;	 Catch:{ Exception -> 0x0c48 }
        r0 = r67;	 Catch:{ Exception -> 0x0c48 }
        r0.performSendMessageRequest(r7, r12, r4);	 Catch:{ Exception -> 0x0c48 }
        r28 = r29;	 Catch:{ Exception -> 0x0c48 }
        goto L_0x0006;	 Catch:{ Exception -> 0x0c48 }
    L_0x1952:
        r4 = new org.telegram.tgnet.TLRPC$TL_inputPeerEmpty;	 Catch:{ Exception -> 0x0c48 }
        r4.<init>();	 Catch:{ Exception -> 0x0c48 }
        r7.from_peer = r4;	 Catch:{ Exception -> 0x0c48 }
        goto L_0x18f9;	 Catch:{ Exception -> 0x0c48 }
    L_0x195a:
        r0 = r82;	 Catch:{ Exception -> 0x0c48 }
        r4 = r0.messageOwner;	 Catch:{ Exception -> 0x0c48 }
        r4 = r4.fwd_msg_id;	 Catch:{ Exception -> 0x0c48 }
        if (r4 == 0) goto L_0x1972;	 Catch:{ Exception -> 0x0c48 }
    L_0x1962:
        r4 = r7.id;	 Catch:{ Exception -> 0x0c48 }
        r0 = r82;	 Catch:{ Exception -> 0x0c48 }
        r6 = r0.messageOwner;	 Catch:{ Exception -> 0x0c48 }
        r6 = r6.fwd_msg_id;	 Catch:{ Exception -> 0x0c48 }
        r6 = java.lang.Integer.valueOf(r6);	 Catch:{ Exception -> 0x0c48 }
        r4.add(r6);	 Catch:{ Exception -> 0x0c48 }
        goto L_0x1948;	 Catch:{ Exception -> 0x0c48 }
    L_0x1972:
        r0 = r82;	 Catch:{ Exception -> 0x0c48 }
        r4 = r0.messageOwner;	 Catch:{ Exception -> 0x0c48 }
        r4 = r4.fwd_from;	 Catch:{ Exception -> 0x0c48 }
        if (r4 == 0) goto L_0x1948;	 Catch:{ Exception -> 0x0c48 }
    L_0x197a:
        r4 = r7.id;	 Catch:{ Exception -> 0x0c48 }
        r0 = r82;	 Catch:{ Exception -> 0x0c48 }
        r6 = r0.messageOwner;	 Catch:{ Exception -> 0x0c48 }
        r6 = r6.fwd_from;	 Catch:{ Exception -> 0x0c48 }
        r6 = r6.channel_post;	 Catch:{ Exception -> 0x0c48 }
        r6 = java.lang.Integer.valueOf(r6);	 Catch:{ Exception -> 0x0c48 }
        r4.add(r6);	 Catch:{ Exception -> 0x0c48 }
        goto L_0x1948;	 Catch:{ Exception -> 0x0c48 }
    L_0x198c:
        r4 = 9;	 Catch:{ Exception -> 0x0c48 }
        r0 = r63;	 Catch:{ Exception -> 0x0c48 }
        if (r0 != r4) goto L_0x1a1b;	 Catch:{ Exception -> 0x0c48 }
    L_0x1992:
        r7 = new org.telegram.tgnet.TLRPC$TL_messages_sendInlineBotResult;	 Catch:{ Exception -> 0x0c48 }
        r7.<init>();	 Catch:{ Exception -> 0x0c48 }
        r0 = r57;	 Catch:{ Exception -> 0x0c48 }
        r7.peer = r0;	 Catch:{ Exception -> 0x0c48 }
        r0 = r46;	 Catch:{ Exception -> 0x0c48 }
        r8 = r0.random_id;	 Catch:{ Exception -> 0x0c48 }
        r7.random_id = r8;	 Catch:{ Exception -> 0x0c48 }
        r0 = r46;	 Catch:{ Exception -> 0x0c48 }
        r4 = r0.reply_to_msg_id;	 Catch:{ Exception -> 0x0c48 }
        if (r4 == 0) goto L_0x19b3;	 Catch:{ Exception -> 0x0c48 }
    L_0x19a7:
        r4 = r7.flags;	 Catch:{ Exception -> 0x0c48 }
        r4 = r4 | 1;	 Catch:{ Exception -> 0x0c48 }
        r7.flags = r4;	 Catch:{ Exception -> 0x0c48 }
        r0 = r46;	 Catch:{ Exception -> 0x0c48 }
        r4 = r0.reply_to_msg_id;	 Catch:{ Exception -> 0x0c48 }
        r7.reply_to_msg_id = r4;	 Catch:{ Exception -> 0x0c48 }
    L_0x19b3:
        r0 = r46;	 Catch:{ Exception -> 0x0c48 }
        r4 = r0.to_id;	 Catch:{ Exception -> 0x0c48 }
        r4 = r4 instanceof org.telegram.tgnet.TLRPC.TL_peerChannel;	 Catch:{ Exception -> 0x0c48 }
        if (r4 == 0) goto L_0x19e0;	 Catch:{ Exception -> 0x0c48 }
    L_0x19bb:
        r0 = r67;	 Catch:{ Exception -> 0x0c48 }
        r4 = r0.currentAccount;	 Catch:{ Exception -> 0x0c48 }
        r4 = org.telegram.messenger.MessagesController.getNotificationsSettings(r4);	 Catch:{ Exception -> 0x0c48 }
        r6 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x0c48 }
        r6.<init>();	 Catch:{ Exception -> 0x0c48 }
        r8 = "silent_";	 Catch:{ Exception -> 0x0c48 }
        r6 = r6.append(r8);	 Catch:{ Exception -> 0x0c48 }
        r0 = r76;	 Catch:{ Exception -> 0x0c48 }
        r6 = r6.append(r0);	 Catch:{ Exception -> 0x0c48 }
        r6 = r6.toString();	 Catch:{ Exception -> 0x0c48 }
        r8 = 0;	 Catch:{ Exception -> 0x0c48 }
        r4 = r4.getBoolean(r6, r8);	 Catch:{ Exception -> 0x0c48 }
        r7.silent = r4;	 Catch:{ Exception -> 0x0c48 }
    L_0x19e0:
        r4 = "query_id";	 Catch:{ Exception -> 0x0c48 }
        r0 = r85;	 Catch:{ Exception -> 0x0c48 }
        r4 = r0.get(r4);	 Catch:{ Exception -> 0x0c48 }
        r4 = (java.lang.String) r4;	 Catch:{ Exception -> 0x0c48 }
        r4 = org.telegram.messenger.Utilities.parseLong(r4);	 Catch:{ Exception -> 0x0c48 }
        r8 = r4.longValue();	 Catch:{ Exception -> 0x0c48 }
        r7.query_id = r8;	 Catch:{ Exception -> 0x0c48 }
        r4 = "id";	 Catch:{ Exception -> 0x0c48 }
        r0 = r85;	 Catch:{ Exception -> 0x0c48 }
        r4 = r0.get(r4);	 Catch:{ Exception -> 0x0c48 }
        r4 = (java.lang.String) r4;	 Catch:{ Exception -> 0x0c48 }
        r7.id = r4;	 Catch:{ Exception -> 0x0c48 }
        if (r82 != 0) goto L_0x1a15;	 Catch:{ Exception -> 0x0c48 }
    L_0x1a04:
        r4 = 1;	 Catch:{ Exception -> 0x0c48 }
        r7.clear_draft = r4;	 Catch:{ Exception -> 0x0c48 }
        r0 = r67;	 Catch:{ Exception -> 0x0c48 }
        r4 = r0.currentAccount;	 Catch:{ Exception -> 0x0c48 }
        r4 = org.telegram.messenger.DataQuery.getInstance(r4);	 Catch:{ Exception -> 0x0c48 }
        r6 = 0;	 Catch:{ Exception -> 0x0c48 }
        r0 = r76;	 Catch:{ Exception -> 0x0c48 }
        r4.cleanDraft(r0, r6);	 Catch:{ Exception -> 0x0c48 }
    L_0x1a15:
        r4 = 0;	 Catch:{ Exception -> 0x0c48 }
        r0 = r67;	 Catch:{ Exception -> 0x0c48 }
        r0.performSendMessageRequest(r7, r12, r4);	 Catch:{ Exception -> 0x0c48 }
    L_0x1a1b:
        r28 = r29;
        goto L_0x0006;
    L_0x1a1f:
        r31 = move-exception;
        r12 = r48;
        r73 = r64;
        goto L_0x0216;
    L_0x1a26:
        r31 = move-exception;
        r12 = r48;
        r46 = r47;
        goto L_0x0216;
    L_0x1a2d:
        r28 = r29;
        goto L_0x15b2;
    L_0x1a31:
        r28 = r29;
        goto L_0x1455;
    L_0x1a35:
        r28 = r29;
        goto L_0x0cc8;
    L_0x1a39:
        r28 = r29;
        goto L_0x0e78;
    L_0x1a3d:
        r28 = r29;
        goto L_0x0da5;
    L_0x1a41:
        r28 = r29;
        goto L_0x0ad9;
    L_0x1a45:
        r29 = r28;
        goto L_0x0a97;
    L_0x1a49:
        r46 = r47;
        goto L_0x038c;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.messenger.SendMessagesHelper.sendMessage(java.lang.String, java.lang.String, org.telegram.tgnet.TLRPC$MessageMedia, org.telegram.tgnet.TLRPC$TL_photo, org.telegram.messenger.VideoEditedInfo, org.telegram.tgnet.TLRPC$User, org.telegram.tgnet.TLRPC$TL_document, org.telegram.tgnet.TLRPC$TL_game, long, java.lang.String, org.telegram.messenger.MessageObject, org.telegram.tgnet.TLRPC$WebPage, boolean, org.telegram.messenger.MessageObject, java.util.ArrayList, org.telegram.tgnet.TLRPC$ReplyMarkup, java.util.HashMap, int):void");
    }

    private void performSendDelayedMessage(DelayedMessage message) {
        performSendDelayedMessage(message, -1);
    }

    private void performSendDelayedMessage(DelayedMessage message, int index) {
        String location;
        if (message.type == 0) {
            if (message.httpLocation != null) {
                putToDelayedMessages(message.httpLocation, message);
                ImageLoader.getInstance().loadHttpFile(message.httpLocation, "file", this.currentAccount);
            } else if (message.sendRequest != null) {
                location = FileLoader.getPathToAttach(message.location).toString();
                putToDelayedMessages(location, message);
                FileLoader.getInstance(this.currentAccount).uploadFile(location, false, true, 16777216);
            } else {
                location = FileLoader.getPathToAttach(message.location).toString();
                if (!(message.sendEncryptedRequest == null || message.location.dc_id == 0)) {
                    File file = new File(location);
                    if (!file.exists()) {
                        location = FileLoader.getPathToAttach(message.location, true).toString();
                        file = new File(location);
                    }
                    if (!file.exists()) {
                        putToDelayedMessages(FileLoader.getAttachFileName(message.location), message);
                        FileLoader.getInstance(this.currentAccount).loadFile(message.location, "jpg", 0, 0);
                        return;
                    }
                }
                putToDelayedMessages(location, message);
                FileLoader.getInstance(this.currentAccount).uploadFile(location, true, true, 16777216);
            }
        } else if (message.type == 1) {
            if (message.videoEditedInfo == null || !message.videoEditedInfo.needConvert()) {
                if (message.videoEditedInfo != null) {
                    if (message.videoEditedInfo.file != null) {
                        if (message.sendRequest instanceof TL_messages_sendMedia) {
                            media = ((TL_messages_sendMedia) message.sendRequest).media;
                        } else {
                            media = ((TL_messages_sendBroadcast) message.sendRequest).media;
                        }
                        media.file = message.videoEditedInfo.file;
                        message.videoEditedInfo.file = null;
                    } else if (message.videoEditedInfo.encryptedFile != null) {
                        TL_decryptedMessage decryptedMessage = message.sendEncryptedRequest;
                        decryptedMessage.media.size = (int) message.videoEditedInfo.estimatedSize;
                        decryptedMessage.media.key = message.videoEditedInfo.key;
                        decryptedMessage.media.iv = message.videoEditedInfo.iv;
                        SecretChatHelper.getInstance(this.currentAccount).performSendEncryptedRequest(decryptedMessage, message.obj.messageOwner, message.encryptedChat, message.videoEditedInfo.encryptedFile, message.originalPath, message.obj);
                        message.videoEditedInfo.encryptedFile = null;
                        return;
                    }
                }
                if (message.sendRequest != null) {
                    if (message.sendRequest instanceof TL_messages_sendMedia) {
                        media = ((TL_messages_sendMedia) message.sendRequest).media;
                    } else {
                        media = ((TL_messages_sendBroadcast) message.sendRequest).media;
                    }
                    if (media.file == null) {
                        location = message.obj.messageOwner.attachPath;
                        document = message.obj.getDocument();
                        if (location == null) {
                            location = FileLoader.getDirectory(4) + "/" + document.id + ".mp4";
                        }
                        putToDelayedMessages(location, message);
                        if (message.obj.videoEditedInfo == null || !message.obj.videoEditedInfo.needConvert()) {
                            FileLoader.getInstance(this.currentAccount).uploadFile(location, false, false, ConnectionsManager.FileTypeVideo);
                            return;
                        } else {
                            FileLoader.getInstance(this.currentAccount).uploadFile(location, false, false, document.size, ConnectionsManager.FileTypeVideo);
                            return;
                        }
                    }
                    location = FileLoader.getDirectory(4) + "/" + message.location.volume_id + "_" + message.location.local_id + ".jpg";
                    putToDelayedMessages(location, message);
                    FileLoader.getInstance(this.currentAccount).uploadFile(location, false, true, 16777216);
                    return;
                }
                location = message.obj.messageOwner.attachPath;
                document = message.obj.getDocument();
                if (location == null) {
                    location = FileLoader.getDirectory(4) + "/" + document.id + ".mp4";
                }
                if (message.sendEncryptedRequest == null || document.dc_id == 0 || new File(location).exists()) {
                    putToDelayedMessages(location, message);
                    if (message.obj.videoEditedInfo == null || !message.obj.videoEditedInfo.needConvert()) {
                        FileLoader.getInstance(this.currentAccount).uploadFile(location, true, false, ConnectionsManager.FileTypeVideo);
                        return;
                    } else {
                        FileLoader.getInstance(this.currentAccount).uploadFile(location, true, false, document.size, ConnectionsManager.FileTypeVideo);
                        return;
                    }
                }
                putToDelayedMessages(FileLoader.getAttachFileName(document), message);
                FileLoader.getInstance(this.currentAccount).loadFile(document, true, 0);
                return;
            }
            location = message.obj.messageOwner.attachPath;
            document = message.obj.getDocument();
            if (location == null) {
                location = FileLoader.getDirectory(4) + "/" + document.id + ".mp4";
            }
            putToDelayedMessages(location, message);
            MediaController.getInstance().scheduleVideoConvert(message.obj);
        } else if (message.type == 2) {
            if (message.httpLocation != null) {
                putToDelayedMessages(message.httpLocation, message);
                ImageLoader.getInstance().loadHttpFile(message.httpLocation, "gif", this.currentAccount);
            } else if (message.sendRequest != null) {
                if (message.sendRequest instanceof TL_messages_sendMedia) {
                    media = ((TL_messages_sendMedia) message.sendRequest).media;
                } else {
                    media = ((TL_messages_sendBroadcast) message.sendRequest).media;
                }
                if (media.file == null) {
                    boolean z;
                    location = message.obj.messageOwner.attachPath;
                    putToDelayedMessages(location, message);
                    FileLoader instance = FileLoader.getInstance(this.currentAccount);
                    if (message.sendRequest == null) {
                        z = true;
                    } else {
                        z = false;
                    }
                    instance.uploadFile(location, z, false, ConnectionsManager.FileTypeFile);
                } else if (media.thumb == null && message.location != null) {
                    location = FileLoader.getDirectory(4) + "/" + message.location.volume_id + "_" + message.location.local_id + ".jpg";
                    putToDelayedMessages(location, message);
                    FileLoader.getInstance(this.currentAccount).uploadFile(location, false, true, 16777216);
                }
            } else {
                location = message.obj.messageOwner.attachPath;
                document = message.obj.getDocument();
                if (message.sendEncryptedRequest == null || document.dc_id == 0 || new File(location).exists()) {
                    putToDelayedMessages(location, message);
                    FileLoader.getInstance(this.currentAccount).uploadFile(location, true, false, ConnectionsManager.FileTypeFile);
                    return;
                }
                putToDelayedMessages(FileLoader.getAttachFileName(document), message);
                FileLoader.getInstance(this.currentAccount).loadFile(document, true, 0);
            }
        } else if (message.type == 3) {
            location = message.obj.messageOwner.attachPath;
            putToDelayedMessages(location, message);
            FileLoader.getInstance(this.currentAccount).uploadFile(location, message.sendRequest == null, true, ConnectionsManager.FileTypeAudio);
        } else if (message.type == 4) {
            boolean add = index < 0;
            if (message.location != null || message.httpLocation != null || message.upload || index >= 0) {
                if (index < 0) {
                    index = message.messageObjects.size() - 1;
                }
                MessageObject messageObject = (MessageObject) message.messageObjects.get(index);
                if (messageObject.getDocument() != null) {
                    if (message.videoEditedInfo != null) {
                        location = messageObject.messageOwner.attachPath;
                        document = messageObject.getDocument();
                        if (location == null) {
                            location = FileLoader.getDirectory(4) + "/" + document.id + ".mp4";
                        }
                        putToDelayedMessages(location, message);
                        message.extraHashMap.put(messageObject, location);
                        message.extraHashMap.put(location + "_i", messageObject);
                        if (message.location != null) {
                            message.extraHashMap.put(location + "_t", message.location);
                        }
                        MediaController.getInstance().scheduleVideoConvert(messageObject);
                    } else {
                        document = messageObject.getDocument();
                        String documentLocation = messageObject.messageOwner.attachPath;
                        if (documentLocation == null) {
                            documentLocation = FileLoader.getDirectory(4) + "/" + document.id + ".mp4";
                        }
                        if (message.sendRequest != null) {
                            media = ((TL_inputSingleMedia) ((TL_messages_sendMultiMedia) message.sendRequest).multi_media.get(index)).media;
                            if (media.file == null) {
                                putToDelayedMessages(documentLocation, message);
                                message.extraHashMap.put(messageObject, documentLocation);
                                message.extraHashMap.put(documentLocation, media);
                                message.extraHashMap.put(documentLocation + "_i", messageObject);
                                if (message.location != null) {
                                    message.extraHashMap.put(documentLocation + "_t", message.location);
                                }
                                if (messageObject.videoEditedInfo == null || !messageObject.videoEditedInfo.needConvert()) {
                                    FileLoader.getInstance(this.currentAccount).uploadFile(documentLocation, false, false, ConnectionsManager.FileTypeVideo);
                                } else {
                                    FileLoader.getInstance(this.currentAccount).uploadFile(documentLocation, false, false, document.size, ConnectionsManager.FileTypeVideo);
                                }
                            } else {
                                location = FileLoader.getDirectory(4) + "/" + message.location.volume_id + "_" + message.location.local_id + ".jpg";
                                putToDelayedMessages(location, message);
                                message.extraHashMap.put(location + "_o", documentLocation);
                                message.extraHashMap.put(messageObject, location);
                                message.extraHashMap.put(location, media);
                                FileLoader.getInstance(this.currentAccount).uploadFile(location, false, true, 16777216);
                            }
                        } else {
                            TL_messages_sendEncryptedMultiMedia request = (TL_messages_sendEncryptedMultiMedia) message.sendEncryptedRequest;
                            putToDelayedMessages(documentLocation, message);
                            message.extraHashMap.put(messageObject, documentLocation);
                            message.extraHashMap.put(documentLocation, request.files.get(index));
                            message.extraHashMap.put(documentLocation + "_i", messageObject);
                            if (message.location != null) {
                                message.extraHashMap.put(documentLocation + "_t", message.location);
                            }
                            if (messageObject.videoEditedInfo == null || !messageObject.videoEditedInfo.needConvert()) {
                                FileLoader.getInstance(this.currentAccount).uploadFile(documentLocation, true, false, ConnectionsManager.FileTypeVideo);
                            } else {
                                FileLoader.getInstance(this.currentAccount).uploadFile(documentLocation, true, false, document.size, ConnectionsManager.FileTypeVideo);
                            }
                        }
                    }
                    message.videoEditedInfo = null;
                    message.location = null;
                } else if (message.httpLocation != null) {
                    putToDelayedMessages(message.httpLocation, message);
                    message.extraHashMap.put(messageObject, message.httpLocation);
                    message.extraHashMap.put(message.httpLocation, messageObject);
                    ImageLoader.getInstance().loadHttpFile(message.httpLocation, "file", this.currentAccount);
                    message.httpLocation = null;
                } else {
                    TLObject inputMedia;
                    if (message.sendRequest != null) {
                        inputMedia = ((TL_inputSingleMedia) ((TL_messages_sendMultiMedia) message.sendRequest).multi_media.get(index)).media;
                    } else {
                        inputMedia = (TLObject) ((TL_messages_sendEncryptedMultiMedia) message.sendEncryptedRequest).files.get(index);
                    }
                    location = FileLoader.getDirectory(4) + "/" + message.location.volume_id + "_" + message.location.local_id + ".jpg";
                    putToDelayedMessages(location, message);
                    message.extraHashMap.put(location, inputMedia);
                    message.extraHashMap.put(messageObject, location);
                    FileLoader.getInstance(this.currentAccount).uploadFile(location, message.sendEncryptedRequest != null, true, 16777216);
                    message.location = null;
                }
                message.upload = false;
            } else if (!message.messageObjects.isEmpty()) {
                putToSendingMessages(((MessageObject) message.messageObjects.get(message.messageObjects.size() - 1)).messageOwner);
            }
            sendReadyToSendGroup(message, add, true);
        }
    }

    private void uploadMultiMedia(final DelayedMessage message, final InputMedia inputMedia, InputEncryptedFile inputEncryptedFile, String key) {
        int a;
        if (inputMedia != null) {
            TL_messages_sendMultiMedia multiMedia = message.sendRequest;
            for (a = 0; a < multiMedia.multi_media.size(); a++) {
                if (((TL_inputSingleMedia) multiMedia.multi_media.get(a)).media == inputMedia) {
                    putToSendingMessages((Message) message.messages.get(a));
                    NotificationCenter.getInstance(this.currentAccount).postNotificationName(NotificationCenter.FileUploadProgressChanged, key, Float.valueOf(1.0f), Boolean.valueOf(false));
                    break;
                }
            }
            TL_messages_uploadMedia req = new TL_messages_uploadMedia();
            req.media = inputMedia;
            req.peer = ((TL_messages_sendMultiMedia) message.sendRequest).peer;
            ConnectionsManager.getInstance(this.currentAccount).sendRequest(req, new RequestDelegate() {
                public void run(final TLObject response, TL_error error) {
                    AndroidUtilities.runOnUIThread(new Runnable() {
                        public void run() {
                            InputMedia newInputMedia = null;
                            if (response != null) {
                                MessageMedia messageMedia = response;
                                if ((inputMedia instanceof TL_inputMediaUploadedPhoto) && (messageMedia instanceof TL_messageMediaPhoto)) {
                                    InputMedia inputMediaPhoto = new TL_inputMediaPhoto();
                                    inputMediaPhoto.id = new TL_inputPhoto();
                                    inputMediaPhoto.id.id = messageMedia.photo.id;
                                    inputMediaPhoto.id.access_hash = messageMedia.photo.access_hash;
                                    newInputMedia = inputMediaPhoto;
                                } else if ((inputMedia instanceof TL_inputMediaUploadedDocument) && (messageMedia instanceof TL_messageMediaDocument)) {
                                    InputMedia inputMediaDocument = new TL_inputMediaDocument();
                                    inputMediaDocument.id = new TL_inputDocument();
                                    inputMediaDocument.id.id = messageMedia.document.id;
                                    inputMediaDocument.id.access_hash = messageMedia.document.access_hash;
                                    newInputMedia = inputMediaDocument;
                                }
                            }
                            if (newInputMedia != null) {
                                if (inputMedia.ttl_seconds != 0) {
                                    newInputMedia.ttl_seconds = inputMedia.ttl_seconds;
                                    newInputMedia.flags |= 1;
                                }
                                TL_messages_sendMultiMedia req = message.sendRequest;
                                for (int a = 0; a < req.multi_media.size(); a++) {
                                    if (((TL_inputSingleMedia) req.multi_media.get(a)).media == inputMedia) {
                                        ((TL_inputSingleMedia) req.multi_media.get(a)).media = newInputMedia;
                                        break;
                                    }
                                }
                                SendMessagesHelper.this.sendReadyToSendGroup(message, false, true);
                                return;
                            }
                            message.markAsError();
                        }
                    });
                }
            });
        } else if (inputEncryptedFile != null) {
            TL_messages_sendEncryptedMultiMedia multiMedia2 = message.sendEncryptedRequest;
            for (a = 0; a < multiMedia2.files.size(); a++) {
                if (multiMedia2.files.get(a) == inputEncryptedFile) {
                    putToSendingMessages((Message) message.messages.get(a));
                    NotificationCenter.getInstance(this.currentAccount).postNotificationName(NotificationCenter.FileUploadProgressChanged, key, Float.valueOf(1.0f), Boolean.valueOf(false));
                    break;
                }
            }
            sendReadyToSendGroup(message, false, true);
        }
    }

    private void sendReadyToSendGroup(DelayedMessage message, boolean add, boolean check) {
        if (message.messageObjects.isEmpty()) {
            message.markAsError();
            return;
        }
        String key = "group_" + message.groupId;
        if (message.finalGroupMessage == ((MessageObject) message.messageObjects.get(message.messageObjects.size() - 1)).getId()) {
            if (add) {
                this.delayedMessages.remove(key);
                MessagesStorage.getInstance(this.currentAccount).putMessages(message.messages, false, true, false, 0);
                MessagesController.getInstance(this.currentAccount).updateInterfaceWithMessages(message.peer, message.messageObjects);
                NotificationCenter.getInstance(this.currentAccount).postNotificationName(NotificationCenter.dialogsNeedReload, new Object[0]);
            }
            int a;
            if (message.sendRequest instanceof TL_messages_sendMultiMedia) {
                TL_messages_sendMultiMedia request = message.sendRequest;
                a = 0;
                while (a < request.multi_media.size()) {
                    InputMedia inputMedia = ((TL_inputSingleMedia) request.multi_media.get(a)).media;
                    if (!(inputMedia instanceof TL_inputMediaUploadedPhoto) && !(inputMedia instanceof TL_inputMediaUploadedDocument)) {
                        a++;
                    } else {
                        return;
                    }
                }
                if (check) {
                    DelayedMessage maxDelayedMessage = findMaxDelayedMessageForMessageId(message.finalGroupMessage, message.peer);
                    if (maxDelayedMessage != null) {
                        maxDelayedMessage.addDelayedRequest(message.sendRequest, message.messageObjects, message.originalPaths);
                        if (message.requests != null) {
                            maxDelayedMessage.requests.addAll(message.requests);
                            return;
                        }
                        return;
                    }
                }
            }
            TL_messages_sendEncryptedMultiMedia request2 = message.sendEncryptedRequest;
            a = 0;
            while (a < request2.files.size()) {
                if (!(((InputEncryptedFile) request2.files.get(a)) instanceof TL_inputEncryptedFile)) {
                    a++;
                } else {
                    return;
                }
            }
            if (message.sendRequest instanceof TL_messages_sendMultiMedia) {
                performSendMessageRequestMulti((TL_messages_sendMultiMedia) message.sendRequest, message.messageObjects, message.originalPaths);
            } else {
                SecretChatHelper.getInstance(this.currentAccount).performSendEncryptedRequest((TL_messages_sendEncryptedMultiMedia) message.sendEncryptedRequest, message);
            }
            message.sendDelayedRequests();
        } else if (add) {
            putToDelayedMessages(key, message);
        }
    }

    protected void stopVideoService(final String path) {
        MessagesStorage.getInstance(this.currentAccount).getStorageQueue().postRunnable(new Runnable() {

            /* renamed from: org.telegram.messenger.SendMessagesHelper$11$1 */
            class C05151 implements Runnable {
                C05151() {
                }

                public void run() {
                    NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.stopEncodingService, path, Integer.valueOf(SendMessagesHelper.this.currentAccount));
                }
            }

            public void run() {
                AndroidUtilities.runOnUIThread(new C05151());
            }
        });
    }

    protected void putToSendingMessages(Message message) {
        this.sendingMessages.put(message.id, message);
    }

    protected void removeFromSendingMessages(int mid) {
        this.sendingMessages.remove(mid);
    }

    public boolean isSendingMessage(int mid) {
        return this.sendingMessages.indexOfKey(mid) >= 0;
    }

    private void performSendMessageRequestMulti(final TL_messages_sendMultiMedia req, final ArrayList<MessageObject> msgObjs, final ArrayList<String> originalPaths) {
        for (int a = 0; a < msgObjs.size(); a++) {
            putToSendingMessages(((MessageObject) msgObjs.get(a)).messageOwner);
        }
        ConnectionsManager.getInstance(this.currentAccount).sendRequest((TLObject) req, new RequestDelegate() {
            public void run(final TLObject response, final TL_error error) {
                AndroidUtilities.runOnUIThread(new Runnable() {
                    public void run() {
                        int i;
                        final Message newMsgObj;
                        boolean isSentError = false;
                        if (error == null) {
                            SparseArray<Message> newMessages = new SparseArray();
                            LongSparseArray<Integer> newIds = new LongSparseArray();
                            Updates updates = (Updates) response;
                            ArrayList<Update> updatesArr = ((Updates) response).updates;
                            int a = 0;
                            while (a < updatesArr.size()) {
                                Update update = (Update) updatesArr.get(a);
                                if (update instanceof TL_updateMessageID) {
                                    TL_updateMessageID updateMessageID = (TL_updateMessageID) update;
                                    newIds.put(updateMessageID.random_id, Integer.valueOf(updateMessageID.id));
                                    updatesArr.remove(a);
                                    a--;
                                } else if (update instanceof TL_updateNewMessage) {
                                    final TL_updateNewMessage newMessage = (TL_updateNewMessage) update;
                                    newMessages.put(newMessage.message.id, newMessage.message);
                                    Utilities.stageQueue.postRunnable(new Runnable() {
                                        public void run() {
                                            MessagesController.getInstance(SendMessagesHelper.this.currentAccount).processNewDifferenceParams(-1, newMessage.pts, -1, newMessage.pts_count);
                                        }
                                    });
                                    updatesArr.remove(a);
                                    a--;
                                } else if (update instanceof TL_updateNewChannelMessage) {
                                    final TL_updateNewChannelMessage newMessage2 = (TL_updateNewChannelMessage) update;
                                    newMessages.put(newMessage2.message.id, newMessage2.message);
                                    Utilities.stageQueue.postRunnable(new Runnable() {
                                        public void run() {
                                            MessagesController.getInstance(SendMessagesHelper.this.currentAccount).processNewChannelDifferenceParams(newMessage2.pts, newMessage2.pts_count, newMessage2.message.to_id.channel_id);
                                        }
                                    });
                                    updatesArr.remove(a);
                                    a--;
                                }
                                a++;
                            }
                            for (i = 0; i < msgObjs.size(); i++) {
                                MessageObject msgObj = (MessageObject) msgObjs.get(i);
                                String originalPath = (String) originalPaths.get(i);
                                newMsgObj = msgObj.messageOwner;
                                final int oldId = newMsgObj.id;
                                ArrayList<Message> sentMessages = new ArrayList();
                                String attachPath = newMsgObj.attachPath;
                                Integer id = (Integer) newIds.get(newMsgObj.random_id);
                                if (id == null) {
                                    isSentError = true;
                                    break;
                                }
                                Message message = (Message) newMessages.get(id.intValue());
                                if (message == null) {
                                    isSentError = true;
                                    break;
                                }
                                sentMessages.add(message);
                                newMsgObj.id = message.id;
                                if ((newMsgObj.flags & Integer.MIN_VALUE) != 0) {
                                    message.flags |= Integer.MIN_VALUE;
                                }
                                Integer value = (Integer) MessagesController.getInstance(SendMessagesHelper.this.currentAccount).dialogs_read_outbox_max.get(Long.valueOf(message.dialog_id));
                                if (value == null) {
                                    value = Integer.valueOf(MessagesStorage.getInstance(SendMessagesHelper.this.currentAccount).getDialogReadMax(message.out, message.dialog_id));
                                    MessagesController.getInstance(SendMessagesHelper.this.currentAccount).dialogs_read_outbox_max.put(Long.valueOf(message.dialog_id), value);
                                }
                                message.unread = value.intValue() < message.id;
                                SendMessagesHelper.this.updateMediaPaths(msgObj, message, originalPath, false);
                                if (null == null) {
                                    StatsController.getInstance(SendMessagesHelper.this.currentAccount).incrementSentItemsCount(ConnectionsManager.getCurrentNetworkType(), 1, 1);
                                    newMsgObj.send_state = 0;
                                    NotificationCenter.getInstance(SendMessagesHelper.this.currentAccount).postNotificationName(NotificationCenter.messageReceivedByServer, Integer.valueOf(oldId), Integer.valueOf(newMsgObj.id), newMsgObj, Long.valueOf(newMsgObj.dialog_id));
                                    final ArrayList<Message> arrayList = sentMessages;
                                    MessagesStorage.getInstance(SendMessagesHelper.this.currentAccount).getStorageQueue().postRunnable(new Runnable() {

                                        /* renamed from: org.telegram.messenger.SendMessagesHelper$12$1$3$1 */
                                        class C05181 implements Runnable {
                                            C05181() {
                                            }

                                            public void run() {
                                                DataQuery.getInstance(SendMessagesHelper.this.currentAccount).increasePeerRaiting(newMsgObj.dialog_id);
                                                NotificationCenter.getInstance(SendMessagesHelper.this.currentAccount).postNotificationName(NotificationCenter.messageReceivedByServer, Integer.valueOf(oldId), Integer.valueOf(newMsgObj.id), newMsgObj, Long.valueOf(newMsgObj.dialog_id));
                                                SendMessagesHelper.this.processSentMessage(oldId);
                                                SendMessagesHelper.this.removeFromSendingMessages(oldId);
                                            }
                                        }

                                        public void run() {
                                            MessagesStorage.getInstance(SendMessagesHelper.this.currentAccount).updateMessageStateAndId(newMsgObj.random_id, Integer.valueOf(oldId), newMsgObj.id, 0, false, newMsgObj.to_id.channel_id);
                                            MessagesStorage.getInstance(SendMessagesHelper.this.currentAccount).putMessages(arrayList, true, false, false, 0);
                                            AndroidUtilities.runOnUIThread(new C05181());
                                        }
                                    });
                                }
                            }
                            final Updates updates2 = updates;
                            Utilities.stageQueue.postRunnable(new Runnable() {
                                public void run() {
                                    MessagesController.getInstance(SendMessagesHelper.this.currentAccount).processUpdates(updates2, false);
                                }
                            });
                        } else {
                            AlertsCreator.processError(SendMessagesHelper.this.currentAccount, error, null, req, new Object[0]);
                            isSentError = true;
                        }
                        if (isSentError) {
                            for (i = 0; i < msgObjs.size(); i++) {
                                newMsgObj = ((MessageObject) msgObjs.get(i)).messageOwner;
                                MessagesStorage.getInstance(SendMessagesHelper.this.currentAccount).markMessageAsSendError(newMsgObj);
                                newMsgObj.send_state = 2;
                                NotificationCenter.getInstance(SendMessagesHelper.this.currentAccount).postNotificationName(NotificationCenter.messageSendError, Integer.valueOf(newMsgObj.id));
                                SendMessagesHelper.this.processSentMessage(newMsgObj.id);
                                SendMessagesHelper.this.removeFromSendingMessages(newMsgObj.id);
                            }
                        }
                    }
                });
            }
        }, null, 68);
    }

    private void performSendMessageRequest(TLObject req, MessageObject msgObj, String originalPath) {
        performSendMessageRequest(req, msgObj, originalPath, null, false);
    }

    private DelayedMessage findMaxDelayedMessageForMessageId(int messageId, long dialogId) {
        DelayedMessage maxDelayedMessage = null;
        int maxDalyedMessageId = Integer.MIN_VALUE;
        for (Entry<String, ArrayList<DelayedMessage>> entry : this.delayedMessages.entrySet()) {
            ArrayList<DelayedMessage> messages = (ArrayList) entry.getValue();
            int size = messages.size();
            for (int a = 0; a < size; a++) {
                DelayedMessage delayedMessage = (DelayedMessage) messages.get(a);
                if ((delayedMessage.type == 4 || delayedMessage.type == 0) && delayedMessage.peer == dialogId) {
                    int mid = 0;
                    if (delayedMessage.obj != null) {
                        mid = delayedMessage.obj.getId();
                    } else if (!(delayedMessage.messageObjects == null || delayedMessage.messageObjects.isEmpty())) {
                        mid = ((MessageObject) delayedMessage.messageObjects.get(delayedMessage.messageObjects.size() - 1)).getId();
                    }
                    if (mid != 0 && mid > messageId && maxDelayedMessage == null && maxDalyedMessageId < mid) {
                        maxDelayedMessage = delayedMessage;
                        maxDalyedMessageId = mid;
                    }
                }
            }
        }
        return maxDelayedMessage;
    }

    private void performSendMessageRequest(TLObject req, MessageObject msgObj, String originalPath, DelayedMessage parentMessage, boolean check) {
        int i;
        if (check) {
            DelayedMessage maxDelayedMessage = findMaxDelayedMessageForMessageId(msgObj.getId(), msgObj.getDialogId());
            if (maxDelayedMessage != null) {
                maxDelayedMessage.addDelayedRequest(req, msgObj, originalPath);
                if (parentMessage != null && parentMessage.requests != null) {
                    maxDelayedMessage.requests.addAll(parentMessage.requests);
                    return;
                }
                return;
            }
        }
        final Message newMsgObj = msgObj.messageOwner;
        putToSendingMessages(newMsgObj);
        ConnectionsManager instance = ConnectionsManager.getInstance(this.currentAccount);
        final TLObject tLObject = req;
        final MessageObject messageObject = msgObj;
        final String str = originalPath;
        RequestDelegate anonymousClass13 = new RequestDelegate() {
            public void run(final TLObject response, final TL_error error) {
                AndroidUtilities.runOnUIThread(new Runnable() {
                    public void run() {
                        boolean isSentError = false;
                        if (error == null) {
                            int i;
                            int oldId = newMsgObj.id;
                            boolean isBroadcast = tLObject instanceof TL_messages_sendBroadcast;
                            ArrayList<Message> sentMessages = new ArrayList();
                            String attachPath = newMsgObj.attachPath;
                            Message message;
                            if (response instanceof TL_updateShortSentMessage) {
                                TL_updateShortSentMessage res = (TL_updateShortSentMessage) response;
                                message = newMsgObj;
                                Message message2 = newMsgObj;
                                i = res.id;
                                message2.id = i;
                                message.local_id = i;
                                newMsgObj.date = res.date;
                                newMsgObj.entities = res.entities;
                                newMsgObj.out = res.out;
                                if (res.media != null) {
                                    newMsgObj.media = res.media;
                                    message = newMsgObj;
                                    message.flags |= 512;
                                    ImageLoader.saveMessageThumbs(newMsgObj);
                                }
                                if ((res.media instanceof TL_messageMediaGame) && !TextUtils.isEmpty(res.message)) {
                                    newMsgObj.message = res.message;
                                }
                                if (!newMsgObj.entities.isEmpty()) {
                                    message = newMsgObj;
                                    message.flags |= 128;
                                }
                                final TL_updateShortSentMessage tL_updateShortSentMessage = res;
                                Utilities.stageQueue.postRunnable(new Runnable() {
                                    public void run() {
                                        MessagesController.getInstance(SendMessagesHelper.this.currentAccount).processNewDifferenceParams(-1, tL_updateShortSentMessage.pts, tL_updateShortSentMessage.date, tL_updateShortSentMessage.pts_count);
                                    }
                                });
                                sentMessages.add(newMsgObj);
                            } else if (response instanceof Updates) {
                                Updates updates = (Updates) response;
                                ArrayList<Update> updatesArr = ((Updates) response).updates;
                                Message message3 = null;
                                int a = 0;
                                while (a < updatesArr.size()) {
                                    Update update = (Update) updatesArr.get(a);
                                    if (update instanceof TL_updateNewMessage) {
                                        final TL_updateNewMessage newMessage = (TL_updateNewMessage) update;
                                        message3 = newMessage.message;
                                        sentMessages.add(message3);
                                        Utilities.stageQueue.postRunnable(new Runnable() {
                                            public void run() {
                                                MessagesController.getInstance(SendMessagesHelper.this.currentAccount).processNewDifferenceParams(-1, newMessage.pts, -1, newMessage.pts_count);
                                            }
                                        });
                                        updatesArr.remove(a);
                                        break;
                                    } else if (update instanceof TL_updateNewChannelMessage) {
                                        final TL_updateNewChannelMessage newMessage2 = (TL_updateNewChannelMessage) update;
                                        message3 = newMessage2.message;
                                        sentMessages.add(message3);
                                        if ((newMsgObj.flags & Integer.MIN_VALUE) != 0) {
                                            message = newMessage2.message;
                                            message.flags |= Integer.MIN_VALUE;
                                        }
                                        Utilities.stageQueue.postRunnable(new Runnable() {
                                            public void run() {
                                                MessagesController.getInstance(SendMessagesHelper.this.currentAccount).processNewChannelDifferenceParams(newMessage2.pts, newMessage2.pts_count, newMessage2.message.to_id.channel_id);
                                            }
                                        });
                                        updatesArr.remove(a);
                                    } else {
                                        a++;
                                    }
                                }
                                if (message3 != null) {
                                    ImageLoader.saveMessageThumbs(message3);
                                    Integer value = (Integer) MessagesController.getInstance(SendMessagesHelper.this.currentAccount).dialogs_read_outbox_max.get(Long.valueOf(message3.dialog_id));
                                    if (value == null) {
                                        value = Integer.valueOf(MessagesStorage.getInstance(SendMessagesHelper.this.currentAccount).getDialogReadMax(message3.out, message3.dialog_id));
                                        MessagesController.getInstance(SendMessagesHelper.this.currentAccount).dialogs_read_outbox_max.put(Long.valueOf(message3.dialog_id), value);
                                    }
                                    message3.unread = value.intValue() < message3.id;
                                    newMsgObj.id = message3.id;
                                    SendMessagesHelper.this.updateMediaPaths(messageObject, message3, str, false);
                                } else {
                                    isSentError = true;
                                }
                                final Updates updates2 = updates;
                                Utilities.stageQueue.postRunnable(new Runnable() {
                                    public void run() {
                                        MessagesController.getInstance(SendMessagesHelper.this.currentAccount).processUpdates(updates2, false);
                                    }
                                });
                            }
                            if (MessageObject.isLiveLocationMessage(newMsgObj)) {
                                LocationController.getInstance(SendMessagesHelper.this.currentAccount).addSharingLocation(newMsgObj.dialog_id, newMsgObj.id, newMsgObj.media.period, newMsgObj);
                            }
                            if (!isSentError) {
                                int i2;
                                StatsController.getInstance(SendMessagesHelper.this.currentAccount).incrementSentItemsCount(ConnectionsManager.getCurrentNetworkType(), 1, 1);
                                newMsgObj.send_state = 0;
                                NotificationCenter instance = NotificationCenter.getInstance(SendMessagesHelper.this.currentAccount);
                                i = NotificationCenter.messageReceivedByServer;
                                Object[] objArr = new Object[4];
                                objArr[0] = Integer.valueOf(oldId);
                                if (isBroadcast) {
                                    i2 = oldId;
                                } else {
                                    i2 = newMsgObj.id;
                                }
                                objArr[1] = Integer.valueOf(i2);
                                objArr[2] = newMsgObj;
                                objArr[3] = Long.valueOf(newMsgObj.dialog_id);
                                instance.postNotificationName(i, objArr);
                                i = oldId;
                                final boolean z = isBroadcast;
                                final ArrayList<Message> arrayList = sentMessages;
                                final String str = attachPath;
                                MessagesStorage.getInstance(SendMessagesHelper.this.currentAccount).getStorageQueue().postRunnable(new Runnable() {

                                    /* renamed from: org.telegram.messenger.SendMessagesHelper$13$1$5$1 */
                                    class C05261 implements Runnable {
                                        C05261() {
                                        }

                                        public void run() {
                                            if (z) {
                                                for (int a = 0; a < arrayList.size(); a++) {
                                                    Message message = (Message) arrayList.get(a);
                                                    ArrayList<MessageObject> arr = new ArrayList();
                                                    MessageObject messageObject = new MessageObject(SendMessagesHelper.this.currentAccount, message, false);
                                                    arr.add(messageObject);
                                                    MessagesController.getInstance(SendMessagesHelper.this.currentAccount).updateInterfaceWithMessages(messageObject.getDialogId(), arr, true);
                                                }
                                                NotificationCenter.getInstance(SendMessagesHelper.this.currentAccount).postNotificationName(NotificationCenter.dialogsNeedReload, new Object[0]);
                                            }
                                            DataQuery.getInstance(SendMessagesHelper.this.currentAccount).increasePeerRaiting(newMsgObj.dialog_id);
                                            NotificationCenter instance = NotificationCenter.getInstance(SendMessagesHelper.this.currentAccount);
                                            int i = NotificationCenter.messageReceivedByServer;
                                            Object[] objArr = new Object[4];
                                            objArr[0] = Integer.valueOf(i);
                                            objArr[1] = Integer.valueOf(z ? i : newMsgObj.id);
                                            objArr[2] = newMsgObj;
                                            objArr[3] = Long.valueOf(newMsgObj.dialog_id);
                                            instance.postNotificationName(i, objArr);
                                            SendMessagesHelper.this.processSentMessage(i);
                                            SendMessagesHelper.this.removeFromSendingMessages(i);
                                        }
                                    }

                                    public void run() {
                                        MessagesStorage.getInstance(SendMessagesHelper.this.currentAccount).updateMessageStateAndId(newMsgObj.random_id, Integer.valueOf(i), z ? i : newMsgObj.id, 0, false, newMsgObj.to_id.channel_id);
                                        MessagesStorage.getInstance(SendMessagesHelper.this.currentAccount).putMessages(arrayList, true, false, z, 0);
                                        if (z) {
                                            ArrayList currentMessage = new ArrayList();
                                            currentMessage.add(newMsgObj);
                                            MessagesStorage.getInstance(SendMessagesHelper.this.currentAccount).putMessages(currentMessage, true, false, false, 0);
                                        }
                                        AndroidUtilities.runOnUIThread(new C05261());
                                        if (MessageObject.isVideoMessage(newMsgObj) || MessageObject.isRoundVideoMessage(newMsgObj) || MessageObject.isNewGifMessage(newMsgObj)) {
                                            SendMessagesHelper.this.stopVideoService(str);
                                        }
                                    }
                                });
                            }
                        } else {
                            AlertsCreator.processError(SendMessagesHelper.this.currentAccount, error, null, tLObject, new Object[0]);
                            isSentError = true;
                        }
                        if (isSentError) {
                            MessagesStorage.getInstance(SendMessagesHelper.this.currentAccount).markMessageAsSendError(newMsgObj);
                            newMsgObj.send_state = 2;
                            NotificationCenter.getInstance(SendMessagesHelper.this.currentAccount).postNotificationName(NotificationCenter.messageSendError, Integer.valueOf(newMsgObj.id));
                            SendMessagesHelper.this.processSentMessage(newMsgObj.id);
                            if (MessageObject.isVideoMessage(newMsgObj) || MessageObject.isRoundVideoMessage(newMsgObj) || MessageObject.isNewGifMessage(newMsgObj)) {
                                SendMessagesHelper.this.stopVideoService(newMsgObj.attachPath);
                            }
                            SendMessagesHelper.this.removeFromSendingMessages(newMsgObj.id);
                        }
                    }
                });
            }
        };
        QuickAckDelegate anonymousClass14 = new QuickAckDelegate() {
            public void run() {
                final int msg_id = newMsgObj.id;
                AndroidUtilities.runOnUIThread(new Runnable() {
                    public void run() {
                        newMsgObj.send_state = 0;
                        NotificationCenter.getInstance(SendMessagesHelper.this.currentAccount).postNotificationName(NotificationCenter.messageReceivedByAck, Integer.valueOf(msg_id));
                    }
                });
            }
        };
        if (req instanceof TL_messages_sendMessage) {
            i = 128;
        } else {
            i = 0;
        }
        instance.sendRequest(req, anonymousClass13, anonymousClass14, i | 68);
        if (parentMessage != null) {
            parentMessage.sendDelayedRequests();
        }
    }

    private void updateMediaPaths(MessageObject newMsgObj, Message sentMessage, String originalPath, boolean post) {
        Message newMsg = newMsgObj.messageOwner;
        if (sentMessage != null) {
            int a;
            PhotoSize size;
            PhotoSize size2;
            String fileName;
            String fileName2;
            File cacheFile;
            File cacheFile2;
            if ((sentMessage.media instanceof TL_messageMediaPhoto) && sentMessage.media.photo != null && (newMsg.media instanceof TL_messageMediaPhoto) && newMsg.media.photo != null) {
                if (sentMessage.media.ttl_seconds == 0) {
                    MessagesStorage.getInstance(this.currentAccount).putSentFile(originalPath, sentMessage.media.photo, 0);
                }
                if (newMsg.media.photo.sizes.size() == 1 && (((PhotoSize) newMsg.media.photo.sizes.get(0)).location instanceof TL_fileLocationUnavailable)) {
                    newMsg.media.photo.sizes = sentMessage.media.photo.sizes;
                } else {
                    for (a = 0; a < sentMessage.media.photo.sizes.size(); a++) {
                        size = (PhotoSize) sentMessage.media.photo.sizes.get(a);
                        if (!(size == null || size.location == null || (size instanceof TL_photoSizeEmpty) || size.type == null)) {
                            int b = 0;
                            while (b < newMsg.media.photo.sizes.size()) {
                                size2 = (PhotoSize) newMsg.media.photo.sizes.get(b);
                                if (size2 == null || size2.location == null || size2.type == null || !((size2.location.volume_id == -2147483648L && size.type.equals(size2.type)) || (size.f25w == size2.f25w && size.f24h == size2.f24h))) {
                                    b++;
                                } else {
                                    fileName = size2.location.volume_id + "_" + size2.location.local_id;
                                    fileName2 = size.location.volume_id + "_" + size.location.local_id;
                                    if (!fileName.equals(fileName2)) {
                                        cacheFile = new File(FileLoader.getDirectory(4), fileName + ".jpg");
                                        if (sentMessage.media.ttl_seconds != 0 || (sentMessage.media.photo.sizes.size() != 1 && size.f25w <= 90 && size.f24h <= 90)) {
                                            cacheFile2 = new File(FileLoader.getDirectory(4), fileName2 + ".jpg");
                                        } else {
                                            cacheFile2 = FileLoader.getPathToAttach(size);
                                        }
                                        cacheFile.renameTo(cacheFile2);
                                        ImageLoader.getInstance().replaceImageInCache(fileName, fileName2, size.location, post);
                                        size2.location = size.location;
                                        size2.size = size.size;
                                    }
                                }
                            }
                        }
                    }
                }
                sentMessage.message = newMsg.message;
                sentMessage.attachPath = newMsg.attachPath;
                newMsg.media.photo.id = sentMessage.media.photo.id;
                newMsg.media.photo.access_hash = sentMessage.media.photo.access_hash;
            } else if ((sentMessage.media instanceof TL_messageMediaDocument) && sentMessage.media.document != null && (newMsg.media instanceof TL_messageMediaDocument) && newMsg.media.document != null) {
                DocumentAttribute attribute;
                if (MessageObject.isVideoMessage(sentMessage)) {
                    if (sentMessage.media.ttl_seconds == 0) {
                        MessagesStorage.getInstance(this.currentAccount).putSentFile(originalPath, sentMessage.media.document, 2);
                    }
                    sentMessage.attachPath = newMsg.attachPath;
                } else if (!(MessageObject.isVoiceMessage(sentMessage) || MessageObject.isRoundVideoMessage(sentMessage) || sentMessage.media.ttl_seconds != 0)) {
                    MessagesStorage.getInstance(this.currentAccount).putSentFile(originalPath, sentMessage.media.document, 1);
                }
                size2 = newMsg.media.document.thumb;
                size = sentMessage.media.document.thumb;
                if (size2 != null && size2.location != null && size2.location.volume_id == -2147483648L && size != null && size.location != null && !(size instanceof TL_photoSizeEmpty) && !(size2 instanceof TL_photoSizeEmpty)) {
                    fileName = size2.location.volume_id + "_" + size2.location.local_id;
                    fileName2 = size.location.volume_id + "_" + size.location.local_id;
                    if (!fileName.equals(fileName2)) {
                        new File(FileLoader.getDirectory(4), fileName + ".jpg").renameTo(new File(FileLoader.getDirectory(4), fileName2 + ".jpg"));
                        ImageLoader.getInstance().replaceImageInCache(fileName, fileName2, size.location, post);
                        size2.location = size.location;
                        size2.size = size.size;
                    }
                } else if (size2 != null && MessageObject.isStickerMessage(sentMessage) && size2.location != null) {
                    size.location = size2.location;
                } else if ((size2 != null && (size2.location instanceof TL_fileLocationUnavailable)) || (size2 instanceof TL_photoSizeEmpty)) {
                    newMsg.media.document.thumb = sentMessage.media.document.thumb;
                }
                newMsg.media.document.dc_id = sentMessage.media.document.dc_id;
                newMsg.media.document.id = sentMessage.media.document.id;
                newMsg.media.document.access_hash = sentMessage.media.document.access_hash;
                byte[] oldWaveform = null;
                for (a = 0; a < newMsg.media.document.attributes.size(); a++) {
                    attribute = (DocumentAttribute) newMsg.media.document.attributes.get(a);
                    if (attribute instanceof TL_documentAttributeAudio) {
                        oldWaveform = attribute.waveform;
                        break;
                    }
                }
                newMsg.media.document.attributes = sentMessage.media.document.attributes;
                if (oldWaveform != null) {
                    for (a = 0; a < newMsg.media.document.attributes.size(); a++) {
                        attribute = (DocumentAttribute) newMsg.media.document.attributes.get(a);
                        if (attribute instanceof TL_documentAttributeAudio) {
                            attribute.waveform = oldWaveform;
                            attribute.flags |= 4;
                        }
                    }
                }
                newMsg.media.document.size = sentMessage.media.document.size;
                newMsg.media.document.mime_type = sentMessage.media.document.mime_type;
                if ((sentMessage.flags & 4) == 0 && MessageObject.isOut(sentMessage)) {
                    if (MessageObject.isNewGifDocument(sentMessage.media.document)) {
                        DataQuery.getInstance(this.currentAccount).addRecentGif(sentMessage.media.document, sentMessage.date);
                    } else if (MessageObject.isStickerDocument(sentMessage.media.document)) {
                        DataQuery.getInstance(this.currentAccount).addRecentSticker(0, sentMessage.media.document, sentMessage.date, false);
                    }
                }
                if (newMsg.attachPath == null || !newMsg.attachPath.startsWith(FileLoader.getDirectory(4).getAbsolutePath())) {
                    sentMessage.attachPath = newMsg.attachPath;
                    sentMessage.message = newMsg.message;
                    return;
                }
                cacheFile = new File(newMsg.attachPath);
                cacheFile2 = FileLoader.getPathToAttach(sentMessage.media.document, sentMessage.media.ttl_seconds != 0);
                if (!cacheFile.renameTo(cacheFile2)) {
                    sentMessage.attachPath = newMsg.attachPath;
                    sentMessage.message = newMsg.message;
                } else if (MessageObject.isVideoMessage(sentMessage)) {
                    newMsgObj.attachPathExists = true;
                } else {
                    newMsgObj.mediaExists = newMsgObj.attachPathExists;
                    newMsgObj.attachPathExists = false;
                    newMsg.attachPath = TtmlNode.ANONYMOUS_REGION_ID;
                    if (originalPath != null) {
                        if (originalPath.startsWith("http")) {
                            MessagesStorage.getInstance(this.currentAccount).addRecentLocalFile(originalPath, cacheFile2.toString(), newMsg.media.document);
                        }
                    }
                }
            } else if ((sentMessage.media instanceof TL_messageMediaContact) && (newMsg.media instanceof TL_messageMediaContact)) {
                newMsg.media = sentMessage.media;
            } else if (sentMessage.media instanceof TL_messageMediaWebPage) {
                newMsg.media = sentMessage.media;
            } else if (sentMessage.media instanceof TL_messageMediaGame) {
                newMsg.media = sentMessage.media;
                if ((newMsg.media instanceof TL_messageMediaGame) && !TextUtils.isEmpty(sentMessage.message)) {
                    newMsg.entities = sentMessage.entities;
                    newMsg.message = sentMessage.message;
                }
            }
        }
    }

    private void putToDelayedMessages(String location, DelayedMessage message) {
        ArrayList<DelayedMessage> arrayList = (ArrayList) this.delayedMessages.get(location);
        if (arrayList == null) {
            arrayList = new ArrayList();
            this.delayedMessages.put(location, arrayList);
        }
        arrayList.add(message);
    }

    protected ArrayList<DelayedMessage> getDelayedMessages(String location) {
        return (ArrayList) this.delayedMessages.get(location);
    }

    protected long getNextRandomId() {
        long val = 0;
        while (val == 0) {
            val = Utilities.random.nextLong();
        }
        return val;
    }

    public void checkUnsentMessages() {
        MessagesStorage.getInstance(this.currentAccount).getUnsentMessages(1000);
    }

    protected void processUnsentMessages(ArrayList<Message> messages, ArrayList<User> users, ArrayList<Chat> chats, ArrayList<EncryptedChat> encryptedChats) {
        final ArrayList<User> arrayList = users;
        final ArrayList<Chat> arrayList2 = chats;
        final ArrayList<EncryptedChat> arrayList3 = encryptedChats;
        final ArrayList<Message> arrayList4 = messages;
        AndroidUtilities.runOnUIThread(new Runnable() {
            public void run() {
                MessagesController.getInstance(SendMessagesHelper.this.currentAccount).putUsers(arrayList, true);
                MessagesController.getInstance(SendMessagesHelper.this.currentAccount).putChats(arrayList2, true);
                MessagesController.getInstance(SendMessagesHelper.this.currentAccount).putEncryptedChats(arrayList3, true);
                for (int a = 0; a < arrayList4.size(); a++) {
                    SendMessagesHelper.this.retrySendMessage(new MessageObject(SendMessagesHelper.this.currentAccount, (Message) arrayList4.get(a), false), true);
                }
            }
        });
    }

    public TL_photo generatePhotoSizes(String path, Uri imageUri) {
        Bitmap bitmap = ImageLoader.loadBitmap(path, imageUri, (float) AndroidUtilities.getPhotoSize(), (float) AndroidUtilities.getPhotoSize(), true);
        if (bitmap == null && AndroidUtilities.getPhotoSize() != 800) {
            bitmap = ImageLoader.loadBitmap(path, imageUri, 800.0f, 800.0f, true);
        }
        ArrayList<PhotoSize> sizes = new ArrayList();
        PhotoSize size = ImageLoader.scaleAndSaveImage(bitmap, 90.0f, 90.0f, 55, true);
        if (size != null) {
            sizes.add(size);
        }
        size = ImageLoader.scaleAndSaveImage(bitmap, (float) AndroidUtilities.getPhotoSize(), (float) AndroidUtilities.getPhotoSize(), 80, false, 101, 101);
        if (size != null) {
            sizes.add(size);
        }
        if (bitmap != null) {
            bitmap.recycle();
        }
        if (sizes.isEmpty()) {
            return null;
        }
        UserConfig.getInstance(this.currentAccount).saveConfig(false);
        TL_photo photo = new TL_photo();
        photo.date = ConnectionsManager.getInstance(this.currentAccount).getCurrentTime();
        photo.sizes = sizes;
        return photo;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static boolean prepareSendingDocumentInternal(int currentAccount, String path, String originalPath, Uri uri, String mime, long dialog_id, MessageObject reply_to_msg, CharSequence caption, ArrayList<MessageEntity> entities) {
        Throwable e;
        TL_document tL_document;
        TL_documentAttributeFilename fileName;
        Object obj;
        String mimeType;
        Bitmap bitmap;
        Options bmOptions;
        RandomAccessFile randomAccessFile;
        ByteBuffer buffer;
        TL_documentAttributeSticker attributeSticker;
        TL_documentAttributeImageSize attributeImageSize;
        final String captionFinal;
        final HashMap<String, String> params;
        final TL_document documentFinal;
        final String pathFinal;
        final int i;
        final long j;
        final MessageObject messageObject;
        final ArrayList<MessageEntity> arrayList;
        Throwable th;
        if ((path == null || path.length() == 0) && uri == null) {
            return false;
        }
        if (uri != null && AndroidUtilities.isInternalUri(uri)) {
            return false;
        }
        if (path != null && AndroidUtilities.isInternalUri(Uri.fromFile(new File(path)))) {
            return false;
        }
        MimeTypeMap myMime = MimeTypeMap.getSingleton();
        TL_documentAttributeAudio attributeAudio = null;
        String extension = null;
        if (uri != null) {
            boolean hasExt = false;
            if (mime != null) {
                extension = myMime.getExtensionFromMimeType(mime);
            }
            if (extension == null) {
                extension = "txt";
            } else {
                hasExt = true;
            }
            path = MediaController.copyFileToCache(uri, extension);
            if (path == null) {
                return false;
            }
            if (!hasExt) {
                extension = null;
            }
        }
        File file = new File(path);
        if (!file.exists() || file.length() == 0) {
            return false;
        }
        boolean sendNew;
        boolean isEncrypted = ((int) dialog_id) == 0;
        boolean allowSticker = !isEncrypted;
        String name = file.getName();
        String ext = TtmlNode.ANONYMOUS_REGION_ID;
        if (extension != null) {
            ext = extension;
        } else {
            int idx = path.lastIndexOf(46);
            if (idx != -1) {
                ext = path.substring(idx + 1);
            }
        }
        String extL = ext.toLowerCase();
        String permormer = null;
        String title = null;
        boolean isVoice = false;
        int duration = 0;
        if (!extL.equals("mp3")) {
            if (!extL.equals("m4a")) {
                if (!extL.equals("opus")) {
                    if (!extL.equals("ogg")) {
                    }
                }
                MediaMetadataRetriever mediaMetadataRetriever = null;
                try {
                    MediaMetadataRetriever mediaMetadataRetriever2 = new MediaMetadataRetriever();
                    try {
                        mediaMetadataRetriever2.setDataSource(file.getAbsolutePath());
                        String d = mediaMetadataRetriever2.extractMetadata(9);
                        if (d != null) {
                            duration = (int) Math.ceil((double) (((float) Long.parseLong(d)) / 1000.0f));
                            title = mediaMetadataRetriever2.extractMetadata(7);
                            permormer = mediaMetadataRetriever2.extractMetadata(2);
                        }
                        if (extL.equals("ogg") && MediaController.isOpusFile(file.getAbsolutePath()) == 1) {
                            isVoice = true;
                        }
                        if (mediaMetadataRetriever2 != null) {
                            try {
                                mediaMetadataRetriever2.release();
                            } catch (Throwable e2) {
                                FileLog.m3e(e2);
                            }
                        }
                    } catch (Exception e3) {
                        e2 = e3;
                        mediaMetadataRetriever = mediaMetadataRetriever2;
                        try {
                            FileLog.m3e(e2);
                            if (mediaMetadataRetriever != null) {
                                try {
                                    mediaMetadataRetriever.release();
                                } catch (Throwable e22) {
                                    FileLog.m3e(e22);
                                }
                            }
                            if (duration != 0) {
                                attributeAudio = new TL_documentAttributeAudio();
                                attributeAudio.duration = duration;
                                attributeAudio.title = title;
                                attributeAudio.performer = permormer;
                                if (attributeAudio.title == null) {
                                    attributeAudio.title = TtmlNode.ANONYMOUS_REGION_ID;
                                }
                                attributeAudio.flags |= 1;
                                if (attributeAudio.performer == null) {
                                    attributeAudio.performer = TtmlNode.ANONYMOUS_REGION_ID;
                                }
                                attributeAudio.flags |= 2;
                                if (isVoice) {
                                    attributeAudio.voice = true;
                                }
                            }
                            sendNew = false;
                            if (originalPath != null) {
                                if (!originalPath.endsWith("attheme")) {
                                    sendNew = true;
                                } else if (attributeAudio == null) {
                                    originalPath = originalPath + MimeTypes.BASE_TYPE_AUDIO + file.length();
                                } else {
                                    originalPath = originalPath + TtmlNode.ANONYMOUS_REGION_ID + file.length();
                                }
                            }
                            tL_document = null;
                            tL_document = (TL_document) MessagesStorage.getInstance(currentAccount).getSentFile(originalPath, isEncrypted ? 1 : 4);
                            tL_document = (TL_document) MessagesStorage.getInstance(currentAccount).getSentFile(path + file.length(), isEncrypted ? 1 : 4);
                            if (tL_document == null) {
                                tL_document = new TL_document();
                                tL_document.id = 0;
                                tL_document.date = ConnectionsManager.getInstance(currentAccount).getCurrentTime();
                                fileName = new TL_documentAttributeFilename();
                                fileName.file_name = name;
                                tL_document.attributes.add(fileName);
                                tL_document.size = (int) file.length();
                                tL_document.dc_id = 0;
                                if (attributeAudio != null) {
                                    tL_document.attributes.add(attributeAudio);
                                }
                                if (ext.length() == 0) {
                                    obj = -1;
                                    switch (extL.hashCode()) {
                                        case 109967:
                                            if (extL.equals("ogg")) {
                                                obj = 2;
                                                break;
                                            }
                                            break;
                                        case 3145576:
                                            if (extL.equals("flac")) {
                                                obj = 3;
                                                break;
                                            }
                                            break;
                                        case 3418175:
                                            if (extL.equals("opus")) {
                                                obj = 1;
                                                break;
                                            }
                                            break;
                                        case 3645340:
                                            if (extL.equals("webp")) {
                                                obj = null;
                                                break;
                                            }
                                            break;
                                    }
                                    switch (obj) {
                                        case null:
                                            tL_document.mime_type = "image/webp";
                                            break;
                                        case 1:
                                            tL_document.mime_type = MimeTypes.AUDIO_OPUS;
                                            break;
                                        case 2:
                                            tL_document.mime_type = "audio/ogg";
                                            break;
                                        case 3:
                                            tL_document.mime_type = MimeTypes.AUDIO_FLAC;
                                            break;
                                        default:
                                            mimeType = myMime.getMimeTypeFromExtension(extL);
                                            if (mimeType == null) {
                                                tL_document.mime_type = "application/octet-stream";
                                                break;
                                            }
                                            tL_document.mime_type = mimeType;
                                            break;
                                    }
                                }
                                tL_document.mime_type = "application/octet-stream";
                                if (tL_document.mime_type.equals("image/gif")) {
                                    try {
                                        bitmap = ImageLoader.loadBitmap(file.getAbsolutePath(), null, 90.0f, 90.0f, true);
                                        if (bitmap != null) {
                                            fileName.file_name = "animation.gif";
                                            tL_document.thumb = ImageLoader.scaleAndSaveImage(bitmap, 90.0f, 90.0f, 55, isEncrypted);
                                            bitmap.recycle();
                                        }
                                    } catch (Throwable e222) {
                                        FileLog.m3e(e222);
                                    }
                                }
                                bmOptions = new Options();
                                try {
                                    bmOptions.inJustDecodeBounds = true;
                                    randomAccessFile = new RandomAccessFile(path, "r");
                                    buffer = randomAccessFile.getChannel().map(MapMode.READ_ONLY, 0, (long) path.length());
                                    Utilities.loadWebpImage(null, buffer, buffer.limit(), bmOptions, true);
                                    randomAccessFile.close();
                                } catch (Throwable e2222) {
                                    FileLog.m3e(e2222);
                                }
                                attributeSticker = new TL_documentAttributeSticker();
                                attributeSticker.alt = TtmlNode.ANONYMOUS_REGION_ID;
                                attributeSticker.stickerset = new TL_inputStickerSetEmpty();
                                tL_document.attributes.add(attributeSticker);
                                attributeImageSize = new TL_documentAttributeImageSize();
                                attributeImageSize.w = bmOptions.outWidth;
                                attributeImageSize.h = bmOptions.outHeight;
                                tL_document.attributes.add(attributeImageSize);
                                if (tL_document.thumb == null) {
                                    tL_document.thumb = new TL_photoSizeEmpty();
                                    tL_document.thumb.type = "s";
                                }
                            }
                            captionFinal = caption == null ? caption.toString() : TtmlNode.ANONYMOUS_REGION_ID;
                            params = new HashMap();
                            if (originalPath != null) {
                                params.put("originalPath", originalPath);
                            }
                            documentFinal = tL_document;
                            pathFinal = path;
                            i = currentAccount;
                            j = dialog_id;
                            messageObject = reply_to_msg;
                            arrayList = entities;
                            AndroidUtilities.runOnUIThread(new Runnable() {
                                public void run() {
                                    SendMessagesHelper.getInstance(i).sendMessage(documentFinal, null, pathFinal, j, messageObject, captionFinal, arrayList, null, params, 0);
                                }
                            });
                            return true;
                        } catch (Throwable th2) {
                            th = th2;
                            if (mediaMetadataRetriever != null) {
                                try {
                                    mediaMetadataRetriever.release();
                                } catch (Throwable e22222) {
                                    FileLog.m3e(e22222);
                                }
                            }
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        mediaMetadataRetriever = mediaMetadataRetriever2;
                        if (mediaMetadataRetriever != null) {
                            mediaMetadataRetriever.release();
                        }
                        throw th;
                    }
                } catch (Exception e4) {
                    e22222 = e4;
                    FileLog.m3e(e22222);
                    if (mediaMetadataRetriever != null) {
                        mediaMetadataRetriever.release();
                    }
                    if (duration != 0) {
                        attributeAudio = new TL_documentAttributeAudio();
                        attributeAudio.duration = duration;
                        attributeAudio.title = title;
                        attributeAudio.performer = permormer;
                        if (attributeAudio.title == null) {
                            attributeAudio.title = TtmlNode.ANONYMOUS_REGION_ID;
                        }
                        attributeAudio.flags |= 1;
                        if (attributeAudio.performer == null) {
                            attributeAudio.performer = TtmlNode.ANONYMOUS_REGION_ID;
                        }
                        attributeAudio.flags |= 2;
                        if (isVoice) {
                            attributeAudio.voice = true;
                        }
                    }
                    sendNew = false;
                    if (originalPath != null) {
                        if (!originalPath.endsWith("attheme")) {
                            sendNew = true;
                        } else if (attributeAudio == null) {
                            originalPath = originalPath + TtmlNode.ANONYMOUS_REGION_ID + file.length();
                        } else {
                            originalPath = originalPath + MimeTypes.BASE_TYPE_AUDIO + file.length();
                        }
                    }
                    tL_document = null;
                    if (isEncrypted) {
                    }
                    tL_document = (TL_document) MessagesStorage.getInstance(currentAccount).getSentFile(originalPath, isEncrypted ? 1 : 4);
                    if (isEncrypted) {
                    }
                    tL_document = (TL_document) MessagesStorage.getInstance(currentAccount).getSentFile(path + file.length(), isEncrypted ? 1 : 4);
                    if (tL_document == null) {
                        tL_document = new TL_document();
                        tL_document.id = 0;
                        tL_document.date = ConnectionsManager.getInstance(currentAccount).getCurrentTime();
                        fileName = new TL_documentAttributeFilename();
                        fileName.file_name = name;
                        tL_document.attributes.add(fileName);
                        tL_document.size = (int) file.length();
                        tL_document.dc_id = 0;
                        if (attributeAudio != null) {
                            tL_document.attributes.add(attributeAudio);
                        }
                        if (ext.length() == 0) {
                            obj = -1;
                            switch (extL.hashCode()) {
                                case 109967:
                                    if (extL.equals("ogg")) {
                                        obj = 2;
                                        break;
                                    }
                                    break;
                                case 3145576:
                                    if (extL.equals("flac")) {
                                        obj = 3;
                                        break;
                                    }
                                    break;
                                case 3418175:
                                    if (extL.equals("opus")) {
                                        obj = 1;
                                        break;
                                    }
                                    break;
                                case 3645340:
                                    if (extL.equals("webp")) {
                                        obj = null;
                                        break;
                                    }
                                    break;
                            }
                            switch (obj) {
                                case null:
                                    tL_document.mime_type = "image/webp";
                                    break;
                                case 1:
                                    tL_document.mime_type = MimeTypes.AUDIO_OPUS;
                                    break;
                                case 2:
                                    tL_document.mime_type = "audio/ogg";
                                    break;
                                case 3:
                                    tL_document.mime_type = MimeTypes.AUDIO_FLAC;
                                    break;
                                default:
                                    mimeType = myMime.getMimeTypeFromExtension(extL);
                                    if (mimeType == null) {
                                        tL_document.mime_type = mimeType;
                                        break;
                                    }
                                    tL_document.mime_type = "application/octet-stream";
                                    break;
                            }
                        }
                        tL_document.mime_type = "application/octet-stream";
                        if (tL_document.mime_type.equals("image/gif")) {
                            bitmap = ImageLoader.loadBitmap(file.getAbsolutePath(), null, 90.0f, 90.0f, true);
                            if (bitmap != null) {
                                fileName.file_name = "animation.gif";
                                tL_document.thumb = ImageLoader.scaleAndSaveImage(bitmap, 90.0f, 90.0f, 55, isEncrypted);
                                bitmap.recycle();
                            }
                        }
                        bmOptions = new Options();
                        bmOptions.inJustDecodeBounds = true;
                        randomAccessFile = new RandomAccessFile(path, "r");
                        buffer = randomAccessFile.getChannel().map(MapMode.READ_ONLY, 0, (long) path.length());
                        Utilities.loadWebpImage(null, buffer, buffer.limit(), bmOptions, true);
                        randomAccessFile.close();
                        attributeSticker = new TL_documentAttributeSticker();
                        attributeSticker.alt = TtmlNode.ANONYMOUS_REGION_ID;
                        attributeSticker.stickerset = new TL_inputStickerSetEmpty();
                        tL_document.attributes.add(attributeSticker);
                        attributeImageSize = new TL_documentAttributeImageSize();
                        attributeImageSize.w = bmOptions.outWidth;
                        attributeImageSize.h = bmOptions.outHeight;
                        tL_document.attributes.add(attributeImageSize);
                        if (tL_document.thumb == null) {
                            tL_document.thumb = new TL_photoSizeEmpty();
                            tL_document.thumb.type = "s";
                        }
                    }
                    if (caption == null) {
                    }
                    params = new HashMap();
                    if (originalPath != null) {
                        params.put("originalPath", originalPath);
                    }
                    documentFinal = tL_document;
                    pathFinal = path;
                    i = currentAccount;
                    j = dialog_id;
                    messageObject = reply_to_msg;
                    arrayList = entities;
                    AndroidUtilities.runOnUIThread(/* anonymous class already generated */);
                    return true;
                }
                if (duration != 0) {
                    attributeAudio = new TL_documentAttributeAudio();
                    attributeAudio.duration = duration;
                    attributeAudio.title = title;
                    attributeAudio.performer = permormer;
                    if (attributeAudio.title == null) {
                        attributeAudio.title = TtmlNode.ANONYMOUS_REGION_ID;
                    }
                    attributeAudio.flags |= 1;
                    if (attributeAudio.performer == null) {
                        attributeAudio.performer = TtmlNode.ANONYMOUS_REGION_ID;
                    }
                    attributeAudio.flags |= 2;
                    if (isVoice) {
                        attributeAudio.voice = true;
                    }
                }
                sendNew = false;
                if (originalPath != null) {
                    if (!originalPath.endsWith("attheme")) {
                        sendNew = true;
                    } else if (attributeAudio == null) {
                        originalPath = originalPath + MimeTypes.BASE_TYPE_AUDIO + file.length();
                    } else {
                        originalPath = originalPath + TtmlNode.ANONYMOUS_REGION_ID + file.length();
                    }
                }
                tL_document = null;
                if (!(sendNew || isEncrypted)) {
                    if (isEncrypted) {
                    }
                    tL_document = (TL_document) MessagesStorage.getInstance(currentAccount).getSentFile(originalPath, isEncrypted ? 1 : 4);
                    if (!(tL_document != null || path.equals(originalPath) || isEncrypted)) {
                        if (isEncrypted) {
                        }
                        tL_document = (TL_document) MessagesStorage.getInstance(currentAccount).getSentFile(path + file.length(), isEncrypted ? 1 : 4);
                    }
                }
                if (tL_document == null) {
                    tL_document = new TL_document();
                    tL_document.id = 0;
                    tL_document.date = ConnectionsManager.getInstance(currentAccount).getCurrentTime();
                    fileName = new TL_documentAttributeFilename();
                    fileName.file_name = name;
                    tL_document.attributes.add(fileName);
                    tL_document.size = (int) file.length();
                    tL_document.dc_id = 0;
                    if (attributeAudio != null) {
                        tL_document.attributes.add(attributeAudio);
                    }
                    if (ext.length() == 0) {
                        obj = -1;
                        switch (extL.hashCode()) {
                            case 109967:
                                if (extL.equals("ogg")) {
                                    obj = 2;
                                    break;
                                }
                                break;
                            case 3145576:
                                if (extL.equals("flac")) {
                                    obj = 3;
                                    break;
                                }
                                break;
                            case 3418175:
                                if (extL.equals("opus")) {
                                    obj = 1;
                                    break;
                                }
                                break;
                            case 3645340:
                                if (extL.equals("webp")) {
                                    obj = null;
                                    break;
                                }
                                break;
                        }
                        switch (obj) {
                            case null:
                                tL_document.mime_type = "image/webp";
                                break;
                            case 1:
                                tL_document.mime_type = MimeTypes.AUDIO_OPUS;
                                break;
                            case 2:
                                tL_document.mime_type = "audio/ogg";
                                break;
                            case 3:
                                tL_document.mime_type = MimeTypes.AUDIO_FLAC;
                                break;
                            default:
                                mimeType = myMime.getMimeTypeFromExtension(extL);
                                if (mimeType == null) {
                                    tL_document.mime_type = "application/octet-stream";
                                    break;
                                }
                                tL_document.mime_type = mimeType;
                                break;
                        }
                    }
                    tL_document.mime_type = "application/octet-stream";
                    if (tL_document.mime_type.equals("image/gif")) {
                        bitmap = ImageLoader.loadBitmap(file.getAbsolutePath(), null, 90.0f, 90.0f, true);
                        if (bitmap != null) {
                            fileName.file_name = "animation.gif";
                            tL_document.thumb = ImageLoader.scaleAndSaveImage(bitmap, 90.0f, 90.0f, 55, isEncrypted);
                            bitmap.recycle();
                        }
                    }
                    if (tL_document.mime_type.equals("image/webp") && allowSticker) {
                        bmOptions = new Options();
                        bmOptions.inJustDecodeBounds = true;
                        randomAccessFile = new RandomAccessFile(path, "r");
                        buffer = randomAccessFile.getChannel().map(MapMode.READ_ONLY, 0, (long) path.length());
                        Utilities.loadWebpImage(null, buffer, buffer.limit(), bmOptions, true);
                        randomAccessFile.close();
                        if (bmOptions.outWidth != 0 && bmOptions.outHeight != 0 && bmOptions.outWidth <= 800 && bmOptions.outHeight <= 800) {
                            attributeSticker = new TL_documentAttributeSticker();
                            attributeSticker.alt = TtmlNode.ANONYMOUS_REGION_ID;
                            attributeSticker.stickerset = new TL_inputStickerSetEmpty();
                            tL_document.attributes.add(attributeSticker);
                            attributeImageSize = new TL_documentAttributeImageSize();
                            attributeImageSize.w = bmOptions.outWidth;
                            attributeImageSize.h = bmOptions.outHeight;
                            tL_document.attributes.add(attributeImageSize);
                        }
                    }
                    if (tL_document.thumb == null) {
                        tL_document.thumb = new TL_photoSizeEmpty();
                        tL_document.thumb.type = "s";
                    }
                }
                if (caption == null) {
                }
                params = new HashMap();
                if (originalPath != null) {
                    params.put("originalPath", originalPath);
                }
                documentFinal = tL_document;
                pathFinal = path;
                i = currentAccount;
                j = dialog_id;
                messageObject = reply_to_msg;
                arrayList = entities;
                AndroidUtilities.runOnUIThread(/* anonymous class already generated */);
                return true;
            }
        }
        AudioInfo audioInfo = AudioInfo.getAudioInfo(file);
        if (!(audioInfo == null || audioInfo.getDuration() == 0)) {
            permormer = audioInfo.getArtist();
            title = audioInfo.getTitle();
        }
        if (duration != 0) {
            attributeAudio = new TL_documentAttributeAudio();
            attributeAudio.duration = duration;
            attributeAudio.title = title;
            attributeAudio.performer = permormer;
            if (attributeAudio.title == null) {
                attributeAudio.title = TtmlNode.ANONYMOUS_REGION_ID;
            }
            attributeAudio.flags |= 1;
            if (attributeAudio.performer == null) {
                attributeAudio.performer = TtmlNode.ANONYMOUS_REGION_ID;
            }
            attributeAudio.flags |= 2;
            if (isVoice) {
                attributeAudio.voice = true;
            }
        }
        sendNew = false;
        if (originalPath != null) {
            if (!originalPath.endsWith("attheme")) {
                sendNew = true;
            } else if (attributeAudio == null) {
                originalPath = originalPath + TtmlNode.ANONYMOUS_REGION_ID + file.length();
            } else {
                originalPath = originalPath + MimeTypes.BASE_TYPE_AUDIO + file.length();
            }
        }
        tL_document = null;
        if (isEncrypted) {
        }
        tL_document = (TL_document) MessagesStorage.getInstance(currentAccount).getSentFile(originalPath, isEncrypted ? 1 : 4);
        if (isEncrypted) {
        }
        tL_document = (TL_document) MessagesStorage.getInstance(currentAccount).getSentFile(path + file.length(), isEncrypted ? 1 : 4);
        if (tL_document == null) {
            tL_document = new TL_document();
            tL_document.id = 0;
            tL_document.date = ConnectionsManager.getInstance(currentAccount).getCurrentTime();
            fileName = new TL_documentAttributeFilename();
            fileName.file_name = name;
            tL_document.attributes.add(fileName);
            tL_document.size = (int) file.length();
            tL_document.dc_id = 0;
            if (attributeAudio != null) {
                tL_document.attributes.add(attributeAudio);
            }
            if (ext.length() == 0) {
                obj = -1;
                switch (extL.hashCode()) {
                    case 109967:
                        if (extL.equals("ogg")) {
                            obj = 2;
                            break;
                        }
                        break;
                    case 3145576:
                        if (extL.equals("flac")) {
                            obj = 3;
                            break;
                        }
                        break;
                    case 3418175:
                        if (extL.equals("opus")) {
                            obj = 1;
                            break;
                        }
                        break;
                    case 3645340:
                        if (extL.equals("webp")) {
                            obj = null;
                            break;
                        }
                        break;
                }
                switch (obj) {
                    case null:
                        tL_document.mime_type = "image/webp";
                        break;
                    case 1:
                        tL_document.mime_type = MimeTypes.AUDIO_OPUS;
                        break;
                    case 2:
                        tL_document.mime_type = "audio/ogg";
                        break;
                    case 3:
                        tL_document.mime_type = MimeTypes.AUDIO_FLAC;
                        break;
                    default:
                        mimeType = myMime.getMimeTypeFromExtension(extL);
                        if (mimeType == null) {
                            tL_document.mime_type = mimeType;
                            break;
                        }
                        tL_document.mime_type = "application/octet-stream";
                        break;
                }
            }
            tL_document.mime_type = "application/octet-stream";
            if (tL_document.mime_type.equals("image/gif")) {
                bitmap = ImageLoader.loadBitmap(file.getAbsolutePath(), null, 90.0f, 90.0f, true);
                if (bitmap != null) {
                    fileName.file_name = "animation.gif";
                    tL_document.thumb = ImageLoader.scaleAndSaveImage(bitmap, 90.0f, 90.0f, 55, isEncrypted);
                    bitmap.recycle();
                }
            }
            bmOptions = new Options();
            bmOptions.inJustDecodeBounds = true;
            randomAccessFile = new RandomAccessFile(path, "r");
            buffer = randomAccessFile.getChannel().map(MapMode.READ_ONLY, 0, (long) path.length());
            Utilities.loadWebpImage(null, buffer, buffer.limit(), bmOptions, true);
            randomAccessFile.close();
            attributeSticker = new TL_documentAttributeSticker();
            attributeSticker.alt = TtmlNode.ANONYMOUS_REGION_ID;
            attributeSticker.stickerset = new TL_inputStickerSetEmpty();
            tL_document.attributes.add(attributeSticker);
            attributeImageSize = new TL_documentAttributeImageSize();
            attributeImageSize.w = bmOptions.outWidth;
            attributeImageSize.h = bmOptions.outHeight;
            tL_document.attributes.add(attributeImageSize);
            if (tL_document.thumb == null) {
                tL_document.thumb = new TL_photoSizeEmpty();
                tL_document.thumb.type = "s";
            }
        }
        if (caption == null) {
        }
        params = new HashMap();
        if (originalPath != null) {
            params.put("originalPath", originalPath);
        }
        documentFinal = tL_document;
        pathFinal = path;
        i = currentAccount;
        j = dialog_id;
        messageObject = reply_to_msg;
        arrayList = entities;
        AndroidUtilities.runOnUIThread(/* anonymous class already generated */);
        return true;
    }

    public static void prepareSendingDocument(String path, String originalPath, Uri uri, String mine, long dialog_id, MessageObject reply_to_msg, InputContentInfoCompat inputContent) {
        if ((path != null && originalPath != null) || uri != null) {
            ArrayList<String> paths = new ArrayList();
            ArrayList<String> originalPaths = new ArrayList();
            ArrayList<Uri> uris = null;
            if (uri != null) {
                uris = new ArrayList();
                uris.add(uri);
            }
            if (path != null) {
                paths.add(path);
                originalPaths.add(originalPath);
            }
            prepareSendingDocuments(paths, originalPaths, uris, mine, dialog_id, reply_to_msg, inputContent);
        }
    }

    public static void prepareSendingAudioDocuments(ArrayList<MessageObject> messageObjects, long dialog_id, MessageObject reply_to_msg) {
        final int currentAccount = UserConfig.selectedAccount;
        final ArrayList<MessageObject> arrayList = messageObjects;
        final long j = dialog_id;
        final MessageObject messageObject = reply_to_msg;
        new Thread(new Runnable() {
            public void run() {
                int size = arrayList.size();
                for (int a = 0; a < size; a++) {
                    final MessageObject messageObject = (MessageObject) arrayList.get(a);
                    String originalPath = messageObject.messageOwner.attachPath;
                    File f = new File(originalPath);
                    boolean isEncrypted = ((int) j) == 0;
                    if (originalPath != null) {
                        originalPath = originalPath + MimeTypes.BASE_TYPE_AUDIO + f.length();
                    }
                    TL_document tL_document = null;
                    if (!isEncrypted) {
                        tL_document = (TL_document) MessagesStorage.getInstance(currentAccount).getSentFile(originalPath, !isEncrypted ? 1 : 4);
                    }
                    if (tL_document == null) {
                        tL_document = messageObject.messageOwner.media.document;
                    }
                    if (isEncrypted) {
                        if (MessagesController.getInstance(currentAccount).getEncryptedChat(Integer.valueOf((int) (j >> 32))) == null) {
                            return;
                        }
                    }
                    final HashMap<String, String> params = new HashMap();
                    if (originalPath != null) {
                        params.put("originalPath", originalPath);
                    }
                    final TL_document documentFinal = tL_document;
                    AndroidUtilities.runOnUIThread(new Runnable() {
                        public void run() {
                            SendMessagesHelper.getInstance(currentAccount).sendMessage(documentFinal, null, messageObject.messageOwner.attachPath, j, messageObject, null, null, null, params, 0);
                        }
                    });
                }
            }
        }).start();
    }

    public static void prepareSendingDocuments(ArrayList<String> paths, ArrayList<String> originalPaths, ArrayList<Uri> uris, String mime, long dialog_id, MessageObject reply_to_msg, InputContentInfoCompat inputContent) {
        if (paths != null || originalPaths != null || uris != null) {
            if (paths == null || originalPaths == null || paths.size() == originalPaths.size()) {
                final int currentAccount = UserConfig.selectedAccount;
                final ArrayList<String> arrayList = paths;
                final ArrayList<String> arrayList2 = originalPaths;
                final String str = mime;
                final long j = dialog_id;
                final MessageObject messageObject = reply_to_msg;
                final ArrayList<Uri> arrayList3 = uris;
                final InputContentInfoCompat inputContentInfoCompat = inputContent;
                new Thread(new Runnable() {

                    /* renamed from: org.telegram.messenger.SendMessagesHelper$18$1 */
                    class C05311 implements Runnable {
                        C05311() {
                        }

                        public void run() {
                            try {
                                Toast.makeText(ApplicationLoader.applicationContext, LocaleController.getString("UnsupportedAttachment", R.string.UnsupportedAttachment), 0).show();
                            } catch (Throwable e) {
                                FileLog.m3e(e);
                            }
                        }
                    }

                    public void run() {
                        int a;
                        boolean error = false;
                        if (arrayList != null) {
                            for (a = 0; a < arrayList.size(); a++) {
                                if (!SendMessagesHelper.prepareSendingDocumentInternal(currentAccount, (String) arrayList.get(a), (String) arrayList2.get(a), null, str, j, messageObject, null, null)) {
                                    error = true;
                                }
                            }
                        }
                        if (arrayList3 != null) {
                            for (a = 0; a < arrayList3.size(); a++) {
                                if (!SendMessagesHelper.prepareSendingDocumentInternal(currentAccount, null, null, (Uri) arrayList3.get(a), str, j, messageObject, null, null)) {
                                    error = true;
                                }
                            }
                        }
                        if (inputContentInfoCompat != null) {
                            inputContentInfoCompat.releasePermission();
                        }
                        if (error) {
                            AndroidUtilities.runOnUIThread(new C05311());
                        }
                    }
                }).start();
            }
        }
    }

    public static void prepareSendingPhoto(String imageFilePath, Uri imageUri, long dialog_id, MessageObject reply_to_msg, CharSequence caption, ArrayList<MessageEntity> entities, ArrayList<InputDocument> stickers, InputContentInfoCompat inputContent, int ttl) {
        SendingMediaInfo info = new SendingMediaInfo();
        info.path = imageFilePath;
        info.uri = imageUri;
        if (caption != null) {
            info.caption = caption.toString();
        }
        info.entities = entities;
        info.ttl = ttl;
        if (!(stickers == null || stickers.isEmpty())) {
            info.masks = new ArrayList(stickers);
        }
        ArrayList<SendingMediaInfo> infos = new ArrayList();
        infos.add(info);
        prepareSendingMedia(infos, dialog_id, reply_to_msg, inputContent, false, false);
    }

    public static void prepareSendingBotContextResult(BotInlineResult result, HashMap<String, String> params, long dialog_id, MessageObject reply_to_msg) {
        if (result != null) {
            final int currentAccount = UserConfig.selectedAccount;
            if (result.send_message instanceof TL_botInlineMessageMediaAuto) {
                final BotInlineResult botInlineResult = result;
                final long j = dialog_id;
                final HashMap<String, String> hashMap = params;
                final MessageObject messageObject = reply_to_msg;
                new Thread(new Runnable() {
                    public void run() {
                        String finalPath = null;
                        TL_document document = null;
                        TL_photo photo = null;
                        TL_game game = null;
                        if (!(botInlineResult instanceof TL_botInlineMediaResult)) {
                            if (botInlineResult.content != null) {
                                File file = new File(FileLoader.getDirectory(4), Utilities.MD5(botInlineResult.content.url) + "." + ImageLoader.getHttpUrlExtension(botInlineResult.content.url, "file"));
                                if (file.exists()) {
                                    finalPath = file.getAbsolutePath();
                                } else {
                                    finalPath = botInlineResult.content.url;
                                }
                                String str = botInlineResult.type;
                                Object obj = -1;
                                switch (str.hashCode()) {
                                    case -1890252483:
                                        if (str.equals("sticker")) {
                                            obj = 4;
                                            break;
                                        }
                                        break;
                                    case 102340:
                                        if (str.equals("gif")) {
                                            obj = 5;
                                            break;
                                        }
                                        break;
                                    case 3143036:
                                        if (str.equals("file")) {
                                            obj = 2;
                                            break;
                                        }
                                        break;
                                    case 93166550:
                                        if (str.equals(MimeTypes.BASE_TYPE_AUDIO)) {
                                            obj = null;
                                            break;
                                        }
                                        break;
                                    case 106642994:
                                        if (str.equals("photo")) {
                                            obj = 6;
                                            break;
                                        }
                                        break;
                                    case 112202875:
                                        if (str.equals(MimeTypes.BASE_TYPE_VIDEO)) {
                                            obj = 3;
                                            break;
                                        }
                                        break;
                                    case 112386354:
                                        if (str.equals("voice")) {
                                            obj = 1;
                                            break;
                                        }
                                        break;
                                }
                                int[] wh;
                                switch (obj) {
                                    case null:
                                    case 1:
                                    case 2:
                                    case 3:
                                    case 4:
                                    case 5:
                                        document = new TL_document();
                                        document.id = 0;
                                        document.size = 0;
                                        document.dc_id = 0;
                                        document.mime_type = botInlineResult.content.mime_type;
                                        document.date = ConnectionsManager.getInstance(currentAccount).getCurrentTime();
                                        TL_documentAttributeFilename fileName = new TL_documentAttributeFilename();
                                        document.attributes.add(fileName);
                                        str = botInlineResult.type;
                                        obj = -1;
                                        switch (str.hashCode()) {
                                            case -1890252483:
                                                if (str.equals("sticker")) {
                                                    obj = 5;
                                                    break;
                                                }
                                                break;
                                            case 102340:
                                                if (str.equals("gif")) {
                                                    obj = null;
                                                    break;
                                                }
                                                break;
                                            case 3143036:
                                                if (str.equals("file")) {
                                                    obj = 3;
                                                    break;
                                                }
                                                break;
                                            case 93166550:
                                                if (str.equals(MimeTypes.BASE_TYPE_AUDIO)) {
                                                    obj = 2;
                                                    break;
                                                }
                                                break;
                                            case 112202875:
                                                if (str.equals(MimeTypes.BASE_TYPE_VIDEO)) {
                                                    obj = 4;
                                                    break;
                                                }
                                                break;
                                            case 112386354:
                                                if (str.equals("voice")) {
                                                    obj = 1;
                                                    break;
                                                }
                                                break;
                                        }
                                        Bitmap bitmap;
                                        TL_documentAttributeAudio audio;
                                        switch (obj) {
                                            case null:
                                                fileName.file_name = "animation.gif";
                                                if (finalPath.endsWith("mp4")) {
                                                    document.mime_type = MimeTypes.VIDEO_MP4;
                                                    document.attributes.add(new TL_documentAttributeAnimated());
                                                } else {
                                                    document.mime_type = "image/gif";
                                                }
                                                try {
                                                    if (finalPath.endsWith("mp4")) {
                                                        bitmap = ThumbnailUtils.createVideoThumbnail(finalPath, 1);
                                                    } else {
                                                        bitmap = ImageLoader.loadBitmap(finalPath, null, 90.0f, 90.0f, true);
                                                    }
                                                    if (bitmap != null) {
                                                        document.thumb = ImageLoader.scaleAndSaveImage(bitmap, 90.0f, 90.0f, 55, false);
                                                        bitmap.recycle();
                                                        break;
                                                    }
                                                } catch (Throwable e) {
                                                    FileLog.m3e(e);
                                                    break;
                                                }
                                                break;
                                            case 1:
                                                audio = new TL_documentAttributeAudio();
                                                audio.duration = MessageObject.getInlineResultDuration(botInlineResult);
                                                audio.voice = true;
                                                fileName.file_name = "audio.ogg";
                                                document.attributes.add(audio);
                                                document.thumb = new TL_photoSizeEmpty();
                                                document.thumb.type = "s";
                                                break;
                                            case 2:
                                                audio = new TL_documentAttributeAudio();
                                                audio.duration = MessageObject.getInlineResultDuration(botInlineResult);
                                                audio.title = botInlineResult.title;
                                                audio.flags |= 1;
                                                if (botInlineResult.description != null) {
                                                    audio.performer = botInlineResult.description;
                                                    audio.flags |= 2;
                                                }
                                                fileName.file_name = "audio.mp3";
                                                document.attributes.add(audio);
                                                document.thumb = new TL_photoSizeEmpty();
                                                document.thumb.type = "s";
                                                break;
                                            case 3:
                                                int idx = botInlineResult.content.mime_type.lastIndexOf(47);
                                                if (idx == -1) {
                                                    fileName.file_name = "file";
                                                    break;
                                                } else {
                                                    fileName.file_name = "file." + botInlineResult.content.mime_type.substring(idx + 1);
                                                    break;
                                                }
                                            case 4:
                                                fileName.file_name = "video.mp4";
                                                TL_documentAttributeVideo attributeVideo = new TL_documentAttributeVideo();
                                                wh = MessageObject.getInlineResultWidthAndHeight(botInlineResult);
                                                attributeVideo.w = wh[0];
                                                attributeVideo.h = wh[1];
                                                attributeVideo.duration = MessageObject.getInlineResultDuration(botInlineResult);
                                                attributeVideo.supports_streaming = true;
                                                document.attributes.add(attributeVideo);
                                                try {
                                                    if (botInlineResult.thumb != null) {
                                                        bitmap = ImageLoader.loadBitmap(new File(FileLoader.getDirectory(4), Utilities.MD5(botInlineResult.thumb.url) + "." + ImageLoader.getHttpUrlExtension(botInlineResult.thumb.url, "jpg")).getAbsolutePath(), null, 90.0f, 90.0f, true);
                                                        if (bitmap != null) {
                                                            document.thumb = ImageLoader.scaleAndSaveImage(bitmap, 90.0f, 90.0f, 55, false);
                                                            bitmap.recycle();
                                                            break;
                                                        }
                                                    }
                                                } catch (Throwable e2) {
                                                    FileLog.m3e(e2);
                                                    break;
                                                }
                                                break;
                                            case 5:
                                                TL_documentAttributeSticker attributeSticker = new TL_documentAttributeSticker();
                                                attributeSticker.alt = TtmlNode.ANONYMOUS_REGION_ID;
                                                attributeSticker.stickerset = new TL_inputStickerSetEmpty();
                                                document.attributes.add(attributeSticker);
                                                TL_documentAttributeImageSize attributeImageSize = new TL_documentAttributeImageSize();
                                                wh = MessageObject.getInlineResultWidthAndHeight(botInlineResult);
                                                attributeImageSize.w = wh[0];
                                                attributeImageSize.h = wh[1];
                                                document.attributes.add(attributeImageSize);
                                                fileName.file_name = "sticker.webp";
                                                try {
                                                    if (botInlineResult.thumb != null) {
                                                        bitmap = ImageLoader.loadBitmap(new File(FileLoader.getDirectory(4), Utilities.MD5(botInlineResult.thumb.url) + "." + ImageLoader.getHttpUrlExtension(botInlineResult.thumb.url, "webp")).getAbsolutePath(), null, 90.0f, 90.0f, true);
                                                        if (bitmap != null) {
                                                            document.thumb = ImageLoader.scaleAndSaveImage(bitmap, 90.0f, 90.0f, 55, false);
                                                            bitmap.recycle();
                                                            break;
                                                        }
                                                    }
                                                } catch (Throwable e22) {
                                                    FileLog.m3e(e22);
                                                    break;
                                                }
                                                break;
                                        }
                                        if (fileName.file_name == null) {
                                            fileName.file_name = "file";
                                        }
                                        if (document.mime_type == null) {
                                            document.mime_type = "application/octet-stream";
                                        }
                                        if (document.thumb == null) {
                                            document.thumb = new TL_photoSize();
                                            wh = MessageObject.getInlineResultWidthAndHeight(botInlineResult);
                                            document.thumb.f25w = wh[0];
                                            document.thumb.f24h = wh[1];
                                            document.thumb.size = 0;
                                            document.thumb.location = new TL_fileLocationUnavailable();
                                            document.thumb.type = "x";
                                            break;
                                        }
                                        break;
                                    case 6:
                                        if (file.exists()) {
                                            photo = SendMessagesHelper.getInstance(currentAccount).generatePhotoSizes(finalPath, null);
                                        }
                                        if (photo == null) {
                                            photo = new TL_photo();
                                            photo.date = ConnectionsManager.getInstance(currentAccount).getCurrentTime();
                                            TL_photoSize photoSize = new TL_photoSize();
                                            wh = MessageObject.getInlineResultWidthAndHeight(botInlineResult);
                                            photoSize.w = wh[0];
                                            photoSize.h = wh[1];
                                            photoSize.size = 1;
                                            photoSize.location = new TL_fileLocationUnavailable();
                                            photoSize.type = "x";
                                            photo.sizes.add(photoSize);
                                            break;
                                        }
                                        break;
                                    default:
                                        break;
                                }
                            }
                        } else if (botInlineResult.type.equals("game")) {
                            if (((int) j) != 0) {
                                game = new TL_game();
                                game.title = botInlineResult.title;
                                game.description = botInlineResult.description;
                                game.short_name = botInlineResult.id;
                                game.photo = botInlineResult.photo;
                                if (botInlineResult.document instanceof TL_document) {
                                    game.document = botInlineResult.document;
                                    game.flags |= 1;
                                }
                            } else {
                                return;
                            }
                        } else if (botInlineResult.document != null) {
                            if (botInlineResult.document instanceof TL_document) {
                                document = botInlineResult.document;
                            }
                        } else if (botInlineResult.photo != null && (botInlineResult.photo instanceof TL_photo)) {
                            photo = (TL_photo) botInlineResult.photo;
                        }
                        final String finalPathFinal = finalPath;
                        final TL_document finalDocument = document;
                        final TL_photo finalPhoto = photo;
                        final TL_game finalGame = game;
                        if (!(hashMap == null || botInlineResult.content == null)) {
                            hashMap.put("originalPath", botInlineResult.content.url);
                        }
                        AndroidUtilities.runOnUIThread(new Runnable() {
                            public void run() {
                                if (finalDocument != null) {
                                    SendMessagesHelper.getInstance(currentAccount).sendMessage(finalDocument, null, finalPathFinal, j, messageObject, botInlineResult.send_message.message, botInlineResult.send_message.entities, botInlineResult.send_message.reply_markup, hashMap, 0);
                                } else if (finalPhoto != null) {
                                    SendMessagesHelper.getInstance(currentAccount).sendMessage(finalPhoto, botInlineResult.content != null ? botInlineResult.content.url : null, j, messageObject, botInlineResult.send_message.message, botInlineResult.send_message.entities, botInlineResult.send_message.reply_markup, hashMap, 0);
                                } else if (finalGame != null) {
                                    SendMessagesHelper.getInstance(currentAccount).sendMessage(finalGame, j, botInlineResult.send_message.reply_markup, hashMap);
                                }
                            }
                        });
                    }
                }).run();
            } else if (result.send_message instanceof TL_botInlineMessageText) {
                boolean z;
                WebPage webPage = null;
                if (((int) dialog_id) == 0) {
                    for (int a = 0; a < result.send_message.entities.size(); a++) {
                        MessageEntity entity = (MessageEntity) result.send_message.entities.get(a);
                        if (entity instanceof TL_messageEntityUrl) {
                            webPage = new TL_webPagePending();
                            webPage.url = result.send_message.message.substring(entity.offset, entity.offset + entity.length);
                            break;
                        }
                    }
                }
                SendMessagesHelper instance = getInstance(currentAccount);
                String str = result.send_message.message;
                if (result.send_message.no_webpage) {
                    z = false;
                } else {
                    z = true;
                }
                instance.sendMessage(str, dialog_id, reply_to_msg, webPage, z, result.send_message.entities, result.send_message.reply_markup, params);
            } else if (result.send_message instanceof TL_botInlineMessageMediaVenue) {
                MessageMedia venue = new TL_messageMediaVenue();
                venue.geo = result.send_message.geo;
                venue.address = result.send_message.address;
                venue.title = result.send_message.title;
                venue.provider = result.send_message.provider;
                venue.venue_id = result.send_message.venue_id;
                String str2 = result.send_message.venue_type;
                venue.venue_id = str2;
                venue.venue_type = str2;
                if (venue.venue_type == null) {
                    venue.venue_type = TtmlNode.ANONYMOUS_REGION_ID;
                }
                getInstance(currentAccount).sendMessage(venue, dialog_id, reply_to_msg, result.send_message.reply_markup, (HashMap) params);
            } else if (result.send_message instanceof TL_botInlineMessageMediaGeo) {
                MessageMedia location;
                if (result.send_message.period != 0) {
                    location = new TL_messageMediaGeoLive();
                    location.period = result.send_message.period;
                    location.geo = result.send_message.geo;
                    getInstance(currentAccount).sendMessage(location, dialog_id, reply_to_msg, result.send_message.reply_markup, (HashMap) params);
                    return;
                }
                location = new TL_messageMediaGeo();
                location.geo = result.send_message.geo;
                getInstance(currentAccount).sendMessage(location, dialog_id, reply_to_msg, result.send_message.reply_markup, (HashMap) params);
            } else if (result.send_message instanceof TL_botInlineMessageMediaContact) {
                User user = new TL_user();
                user.phone = result.send_message.phone_number;
                user.first_name = result.send_message.first_name;
                user.last_name = result.send_message.last_name;
                getInstance(currentAccount).sendMessage(user, dialog_id, reply_to_msg, result.send_message.reply_markup, (HashMap) params);
            }
        }
    }

    private static String getTrimmedString(String src) {
        String result = src.trim();
        if (result.length() == 0) {
            return result;
        }
        while (src.startsWith("\n")) {
            src = src.substring(1);
        }
        while (src.endsWith("\n")) {
            src = src.substring(0, src.length() - 1);
        }
        return src;
    }

    public static void prepareSendingText(final String text, final long dialog_id) {
        final int currentAccount = UserConfig.selectedAccount;
        MessagesStorage.getInstance(currentAccount).getStorageQueue().postRunnable(new Runnable() {

            /* renamed from: org.telegram.messenger.SendMessagesHelper$20$1 */
            class C05351 implements Runnable {

                /* renamed from: org.telegram.messenger.SendMessagesHelper$20$1$1 */
                class C05341 implements Runnable {
                    C05341() {
                    }

                    public void run() {
                        String textFinal = SendMessagesHelper.getTrimmedString(text);
                        if (textFinal.length() != 0) {
                            int count = (int) Math.ceil((double) (((float) textFinal.length()) / 4096.0f));
                            for (int a = 0; a < count; a++) {
                                SendMessagesHelper.getInstance(currentAccount).sendMessage(textFinal.substring(a * 4096, Math.min((a + 1) * 4096, textFinal.length())), dialog_id, null, null, true, null, null, null);
                            }
                        }
                    }
                }

                C05351() {
                }

                public void run() {
                    AndroidUtilities.runOnUIThread(new C05341());
                }
            }

            public void run() {
                Utilities.stageQueue.postRunnable(new C05351());
            }
        });
    }

    public static void prepareSendingMedia(ArrayList<SendingMediaInfo> media, long dialog_id, MessageObject reply_to_msg, InputContentInfoCompat inputContent, boolean forceDocument, boolean groupPhotos) {
        if (!media.isEmpty()) {
            final int currentAccount = UserConfig.selectedAccount;
            final ArrayList<SendingMediaInfo> arrayList = media;
            final long j = dialog_id;
            final boolean z = forceDocument;
            final boolean z2 = groupPhotos;
            final MessageObject messageObject = reply_to_msg;
            final InputContentInfoCompat inputContentInfoCompat = inputContent;
            mediaSendQueue.postRunnable(new Runnable() {

                /* renamed from: org.telegram.messenger.SendMessagesHelper$21$2 */
                class C05372 implements Runnable {
                    final /* synthetic */ TL_document val$documentFinal;
                    final /* synthetic */ SendingMediaInfo val$info;
                    final /* synthetic */ HashMap val$params;
                    final /* synthetic */ String val$pathFinal;

                    C05372(TL_document tL_document, String str, SendingMediaInfo sendingMediaInfo, HashMap hashMap) {
                        this.val$documentFinal = tL_document;
                        this.val$pathFinal = str;
                        this.val$info = sendingMediaInfo;
                        this.val$params = hashMap;
                    }

                    public void run() {
                        SendMessagesHelper.getInstance(currentAccount).sendMessage(this.val$documentFinal, null, this.val$pathFinal, j, messageObject, this.val$info.caption, this.val$info.entities, null, this.val$params, 0);
                    }
                }

                /* renamed from: org.telegram.messenger.SendMessagesHelper$21$3 */
                class C05383 implements Runnable {
                    final /* synthetic */ SendingMediaInfo val$info;
                    final /* synthetic */ boolean val$needDownloadHttpFinal;
                    final /* synthetic */ HashMap val$params;
                    final /* synthetic */ TL_photo val$photoFinal;

                    C05383(TL_photo tL_photo, boolean z, SendingMediaInfo sendingMediaInfo, HashMap hashMap) {
                        this.val$photoFinal = tL_photo;
                        this.val$needDownloadHttpFinal = z;
                        this.val$info = sendingMediaInfo;
                        this.val$params = hashMap;
                    }

                    public void run() {
                        String str;
                        SendMessagesHelper instance = SendMessagesHelper.getInstance(currentAccount);
                        TL_photo tL_photo = this.val$photoFinal;
                        if (this.val$needDownloadHttpFinal) {
                            str = this.val$info.searchImage.imageUrl;
                        } else {
                            str = null;
                        }
                        instance.sendMessage(tL_photo, str, j, messageObject, this.val$info.caption, this.val$info.entities, null, this.val$params, this.val$info.ttl);
                    }
                }

                /* renamed from: org.telegram.messenger.SendMessagesHelper$21$4 */
                class C05394 implements Runnable {
                    final /* synthetic */ String val$finalPath;
                    final /* synthetic */ SendingMediaInfo val$info;
                    final /* synthetic */ HashMap val$params;
                    final /* synthetic */ Bitmap val$thumbFinal;
                    final /* synthetic */ String val$thumbKeyFinal;
                    final /* synthetic */ VideoEditedInfo val$videoEditedInfo;
                    final /* synthetic */ TL_document val$videoFinal;

                    C05394(Bitmap bitmap, String str, TL_document tL_document, VideoEditedInfo videoEditedInfo, String str2, SendingMediaInfo sendingMediaInfo, HashMap hashMap) {
                        this.val$thumbFinal = bitmap;
                        this.val$thumbKeyFinal = str;
                        this.val$videoFinal = tL_document;
                        this.val$videoEditedInfo = videoEditedInfo;
                        this.val$finalPath = str2;
                        this.val$info = sendingMediaInfo;
                        this.val$params = hashMap;
                    }

                    public void run() {
                        if (!(this.val$thumbFinal == null || this.val$thumbKeyFinal == null)) {
                            ImageLoader.getInstance().putImageToCache(new BitmapDrawable(this.val$thumbFinal), this.val$thumbKeyFinal);
                        }
                        SendMessagesHelper.getInstance(currentAccount).sendMessage(this.val$videoFinal, this.val$videoEditedInfo, this.val$finalPath, j, messageObject, this.val$info.caption, this.val$info.entities, null, this.val$params, this.val$info.ttl);
                    }
                }

                /* renamed from: org.telegram.messenger.SendMessagesHelper$21$5 */
                class C05405 implements Runnable {
                    final /* synthetic */ SendingMediaInfo val$info;
                    final /* synthetic */ HashMap val$params;
                    final /* synthetic */ TL_photo val$photoFinal;

                    C05405(TL_photo tL_photo, SendingMediaInfo sendingMediaInfo, HashMap hashMap) {
                        this.val$photoFinal = tL_photo;
                        this.val$info = sendingMediaInfo;
                        this.val$params = hashMap;
                    }

                    public void run() {
                        SendMessagesHelper.getInstance(currentAccount).sendMessage(this.val$photoFinal, null, j, messageObject, this.val$info.caption, this.val$info.entities, null, this.val$params, this.val$info.ttl);
                    }
                }

                /* renamed from: org.telegram.messenger.SendMessagesHelper$21$6 */
                class C05416 implements Runnable {
                    final /* synthetic */ long val$lastGroupIdFinal;

                    C05416(long j) {
                        this.val$lastGroupIdFinal = j;
                    }

                    public void run() {
                        SendMessagesHelper instance = SendMessagesHelper.getInstance(currentAccount);
                        ArrayList<DelayedMessage> arrayList = (ArrayList) instance.delayedMessages.get("group_" + this.val$lastGroupIdFinal);
                        if (arrayList != null && !arrayList.isEmpty()) {
                            DelayedMessage message = (DelayedMessage) arrayList.get(0);
                            MessageObject prevMessage = (MessageObject) message.messageObjects.get(message.messageObjects.size() - 1);
                            message.finalGroupMessage = prevMessage.getId();
                            prevMessage.messageOwner.params.put("final", "1");
                            messages_Messages messagesRes = new TL_messages_messages();
                            messagesRes.messages.add(prevMessage.messageOwner);
                            MessagesStorage.getInstance(currentAccount).putMessages(messagesRes, message.peer, -2, 0, false);
                            instance.sendReadyToSendGroup(message, true, true);
                        }
                    }
                }

                public void run() {
                    /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Unknown predecessor block by arg (r75_3 'thumbFile' java.io.File) in PHI: PHI: (r75_5 'thumbFile' java.io.File) = (r75_2 'thumbFile' java.io.File), (r75_3 'thumbFile' java.io.File), (r75_4 'thumbFile' java.io.File) binds: {(r75_2 'thumbFile' java.io.File)=B:96:0x024b, (r75_3 'thumbFile' java.io.File)=B:98:0x028a, (r75_4 'thumbFile' java.io.File)=B:99:0x028c}
	at jadx.core.dex.instructions.PhiInsn.replaceArg(PhiInsn.java:79)
	at jadx.core.dex.visitors.ModVisitor.processInvoke(ModVisitor.java:222)
	at jadx.core.dex.visitors.ModVisitor.replaceStep(ModVisitor.java:83)
	at jadx.core.dex.visitors.ModVisitor.visit(ModVisitor.java:68)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:60)
	at jadx.core.ProcessClass.process(ProcessClass.java:39)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:282)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
*/
                    /*
                    r79 = this;
                    r34 = java.lang.System.currentTimeMillis();
                    r0 = r79;
                    r4 = r1;
                    r38 = r4.size();
                    r0 = r79;
                    r4 = r2;
                    r4 = (int) r4;
                    if (r4 != 0) goto L_0x00a7;
                L_0x0013:
                    r50 = 1;
                L_0x0015:
                    r43 = 0;
                    if (r50 == 0) goto L_0x003d;
                L_0x0019:
                    r0 = r79;
                    r4 = r2;
                    r10 = 32;
                    r4 = r4 >> r10;
                    r0 = (int) r4;
                    r48 = r0;
                    r0 = r79;
                    r4 = r4;
                    r4 = org.telegram.messenger.MessagesController.getInstance(r4);
                    r5 = java.lang.Integer.valueOf(r48);
                    r42 = r4.getEncryptedChat(r5);
                    if (r42 == 0) goto L_0x003d;
                L_0x0035:
                    r0 = r42;
                    r4 = r0.layer;
                    r43 = org.telegram.messenger.AndroidUtilities.getPeerLayerVersion(r4);
                L_0x003d:
                    if (r50 == 0) goto L_0x0045;
                L_0x003f:
                    r4 = 73;
                    r0 = r43;
                    if (r0 < r4) goto L_0x0162;
                L_0x0045:
                    r0 = r79;
                    r4 = r5;
                    if (r4 != 0) goto L_0x0162;
                L_0x004b:
                    r0 = r79;
                    r4 = r6;
                    if (r4 == 0) goto L_0x0162;
                L_0x0051:
                    r78 = new java.util.HashMap;
                    r78.<init>();
                    r31 = 0;
                L_0x0058:
                    r0 = r31;
                    r1 = r38;
                    if (r0 >= r1) goto L_0x0164;
                L_0x005e:
                    r0 = r79;
                    r4 = r1;
                    r0 = r31;
                    r8 = r4.get(r0);
                    r8 = (org.telegram.messenger.SendMessagesHelper.SendingMediaInfo) r8;
                    r4 = r8.searchImage;
                    if (r4 != 0) goto L_0x00a4;
                L_0x006e:
                    r4 = r8.isVideo;
                    if (r4 != 0) goto L_0x00a4;
                L_0x0072:
                    r0 = r8.path;
                    r58 = r0;
                    r0 = r8.path;
                    r73 = r0;
                    if (r73 != 0) goto L_0x008c;
                L_0x007c:
                    r4 = r8.uri;
                    if (r4 == 0) goto L_0x008c;
                L_0x0080:
                    r4 = r8.uri;
                    r73 = org.telegram.messenger.AndroidUtilities.getPath(r4);
                    r4 = r8.uri;
                    r58 = r4.toString();
                L_0x008c:
                    if (r73 == 0) goto L_0x00ab;
                L_0x008e:
                    r4 = ".gif";
                    r0 = r73;
                    r4 = r0.endsWith(r4);
                    if (r4 != 0) goto L_0x00a4;
                L_0x0099:
                    r4 = ".webp";
                    r0 = r73;
                    r4 = r0.endsWith(r4);
                    if (r4 == 0) goto L_0x00ab;
                L_0x00a4:
                    r31 = r31 + 1;
                    goto L_0x0058;
                L_0x00a7:
                    r50 = 0;
                    goto L_0x0015;
                L_0x00ab:
                    if (r73 != 0) goto L_0x00c1;
                L_0x00ad:
                    r4 = r8.uri;
                    if (r4 == 0) goto L_0x00c1;
                L_0x00b1:
                    r4 = r8.uri;
                    r4 = org.telegram.messenger.MediaController.isGif(r4);
                    if (r4 != 0) goto L_0x00a4;
                L_0x00b9:
                    r4 = r8.uri;
                    r4 = org.telegram.messenger.MediaController.isWebp(r4);
                    if (r4 != 0) goto L_0x00a4;
                L_0x00c1:
                    if (r73 == 0) goto L_0x013f;
                L_0x00c3:
                    r72 = new java.io.File;
                    r72.<init>(r73);
                    r4 = new java.lang.StringBuilder;
                    r4.<init>();
                    r0 = r58;
                    r4 = r4.append(r0);
                    r10 = r72.length();
                    r4 = r4.append(r10);
                    r5 = "_";
                    r4 = r4.append(r5);
                    r10 = r72.lastModified();
                    r4 = r4.append(r10);
                    r58 = r4.toString();
                L_0x00ee:
                    r61 = 0;
                    if (r50 != 0) goto L_0x0126;
                L_0x00f2:
                    r4 = r8.ttl;
                    if (r4 != 0) goto L_0x0126;
                L_0x00f6:
                    r0 = r79;
                    r4 = r4;
                    r5 = org.telegram.messenger.MessagesStorage.getInstance(r4);
                    if (r50 != 0) goto L_0x0142;
                L_0x0100:
                    r4 = 0;
                L_0x0101:
                    r0 = r58;
                    r61 = r5.getSentFile(r0, r4);
                    r61 = (org.telegram.tgnet.TLRPC.TL_photo) r61;
                    if (r61 != 0) goto L_0x0126;
                L_0x010b:
                    r4 = r8.uri;
                    if (r4 == 0) goto L_0x0126;
                L_0x010f:
                    r0 = r79;
                    r4 = r4;
                    r5 = org.telegram.messenger.MessagesStorage.getInstance(r4);
                    r4 = r8.uri;
                    r10 = org.telegram.messenger.AndroidUtilities.getPath(r4);
                    if (r50 != 0) goto L_0x0144;
                L_0x011f:
                    r4 = 0;
                L_0x0120:
                    r61 = r5.getSentFile(r10, r4);
                    r61 = (org.telegram.tgnet.TLRPC.TL_photo) r61;
                L_0x0126:
                    r77 = new org.telegram.messenger.SendMessagesHelper$MediaSendPrepareWorker;
                    r4 = 0;
                    r0 = r77;
                    r0.<init>();
                    r0 = r78;
                    r1 = r77;
                    r0.put(r8, r1);
                    if (r61 == 0) goto L_0x0146;
                L_0x0137:
                    r0 = r61;
                    r1 = r77;
                    r1.photo = r0;
                    goto L_0x00a4;
                L_0x013f:
                    r58 = 0;
                    goto L_0x00ee;
                L_0x0142:
                    r4 = 3;
                    goto L_0x0101;
                L_0x0144:
                    r4 = 3;
                    goto L_0x0120;
                L_0x0146:
                    r4 = new java.util.concurrent.CountDownLatch;
                    r5 = 1;
                    r4.<init>(r5);
                    r0 = r77;
                    r0.sync = r4;
                    r4 = org.telegram.messenger.SendMessagesHelper.mediaSendThreadPool;
                    r5 = new org.telegram.messenger.SendMessagesHelper$21$1;
                    r0 = r79;
                    r1 = r77;
                    r5.<init>(r1, r8);
                    r4.execute(r5);
                    goto L_0x00a4;
                L_0x0162:
                    r78 = 0;
                L_0x0164:
                    r46 = 0;
                    r52 = 0;
                    r64 = 0;
                    r67 = 0;
                    r65 = 0;
                    r66 = 0;
                    r44 = 0;
                    r63 = 0;
                    r31 = 0;
                L_0x0176:
                    r0 = r31;
                    r1 = r38;
                    if (r0 >= r1) goto L_0x0aa9;
                L_0x017c:
                    r0 = r79;
                    r4 = r1;
                    r0 = r31;
                    r8 = r4.get(r0);
                    r8 = (org.telegram.messenger.SendMessagesHelper.SendingMediaInfo) r8;
                    r0 = r79;
                    r4 = r6;
                    if (r4 == 0) goto L_0x01a9;
                L_0x018e:
                    if (r50 == 0) goto L_0x0196;
                L_0x0190:
                    r4 = 73;
                    r0 = r43;
                    if (r0 < r4) goto L_0x01a9;
                L_0x0196:
                    r4 = 1;
                    r0 = r38;
                    if (r0 <= r4) goto L_0x01a9;
                L_0x019b:
                    r4 = r63 % 10;
                    if (r4 != 0) goto L_0x01a9;
                L_0x019f:
                    r4 = org.telegram.messenger.Utilities.random;
                    r46 = r4.nextLong();
                    r52 = r46;
                    r63 = 0;
                L_0x01a9:
                    r4 = r8.searchImage;
                    if (r4 == 0) goto L_0x0532;
                L_0x01ad:
                    r4 = r8.searchImage;
                    r4 = r4.type;
                    r5 = 1;
                    if (r4 != r5) goto L_0x03b0;
                L_0x01b4:
                    r9 = new java.util.HashMap;
                    r9.<init>();
                    r40 = 0;
                    r4 = r8.searchImage;
                    r4 = r4.document;
                    r4 = r4 instanceof org.telegram.tgnet.TLRPC.TL_document;
                    if (r4 == 0) goto L_0x0329;
                L_0x01c3:
                    r4 = r8.searchImage;
                    r0 = r4.document;
                    r40 = r0;
                    r40 = (org.telegram.tgnet.TLRPC.TL_document) r40;
                    r4 = 1;
                    r0 = r40;
                    r37 = org.telegram.messenger.FileLoader.getPathToAttach(r0, r4);
                L_0x01d2:
                    if (r40 != 0) goto L_0x02fb;
                L_0x01d4:
                    r4 = r8.searchImage;
                    r4 = r4.localUrl;
                    if (r4 == 0) goto L_0x01e4;
                L_0x01da:
                    r4 = "url";
                    r5 = r8.searchImage;
                    r5 = r5.localUrl;
                    r9.put(r4, r5);
                L_0x01e4:
                    r75 = 0;
                    r40 = new org.telegram.tgnet.TLRPC$TL_document;
                    r40.<init>();
                    r4 = 0;
                    r0 = r40;
                    r0.id = r4;
                    r0 = r79;
                    r4 = r4;
                    r4 = org.telegram.tgnet.ConnectionsManager.getInstance(r4);
                    r4 = r4.getCurrentTime();
                    r0 = r40;
                    r0.date = r4;
                    r45 = new org.telegram.tgnet.TLRPC$TL_documentAttributeFilename;
                    r45.<init>();
                    r4 = "animation.gif";
                    r0 = r45;
                    r0.file_name = r4;
                    r0 = r40;
                    r4 = r0.attributes;
                    r0 = r45;
                    r4.add(r0);
                    r4 = r8.searchImage;
                    r4 = r4.size;
                    r0 = r40;
                    r0.size = r4;
                    r4 = 0;
                    r0 = r40;
                    r0.dc_id = r4;
                    r4 = r37.toString();
                    r5 = "mp4";
                    r4 = r4.endsWith(r5);
                    if (r4 == 0) goto L_0x0387;
                L_0x0230:
                    r4 = "video/mp4";
                    r0 = r40;
                    r0.mime_type = r4;
                    r0 = r40;
                    r4 = r0.attributes;
                    r5 = new org.telegram.tgnet.TLRPC$TL_documentAttributeAnimated;
                    r5.<init>();
                    r4.add(r5);
                L_0x0243:
                    r4 = r37.exists();
                    if (r4 == 0) goto L_0x0390;
                L_0x0249:
                    r75 = r37;
                L_0x024b:
                    if (r75 != 0) goto L_0x028e;
                L_0x024d:
                    r4 = new java.lang.StringBuilder;
                    r4.<init>();
                    r5 = r8.searchImage;
                    r5 = r5.thumbUrl;
                    r5 = org.telegram.messenger.Utilities.MD5(r5);
                    r4 = r4.append(r5);
                    r5 = ".";
                    r4 = r4.append(r5);
                    r5 = r8.searchImage;
                    r5 = r5.thumbUrl;
                    r10 = "jpg";
                    r5 = org.telegram.messenger.ImageLoader.getHttpUrlExtension(r5, r10);
                    r4 = r4.append(r5);
                    r74 = r4.toString();
                    r75 = new java.io.File;
                    r4 = 4;
                    r4 = org.telegram.messenger.FileLoader.getDirectory(r4);
                    r0 = r75;
                    r1 = r74;
                    r0.<init>(r4, r1);
                    r4 = r75.exists();
                    if (r4 != 0) goto L_0x028e;
                L_0x028c:
                    r75 = 0;
                L_0x028e:
                    if (r75 == 0) goto L_0x02bd;
                L_0x0290:
                    r4 = r75.getAbsolutePath();	 Catch:{ Exception -> 0x03a4 }
                    r5 = "mp4";	 Catch:{ Exception -> 0x03a4 }
                    r4 = r4.endsWith(r5);	 Catch:{ Exception -> 0x03a4 }
                    if (r4 == 0) goto L_0x0394;	 Catch:{ Exception -> 0x03a4 }
                L_0x029d:
                    r4 = r75.getAbsolutePath();	 Catch:{ Exception -> 0x03a4 }
                    r5 = 1;	 Catch:{ Exception -> 0x03a4 }
                    r36 = android.media.ThumbnailUtils.createVideoThumbnail(r4, r5);	 Catch:{ Exception -> 0x03a4 }
                L_0x02a6:
                    if (r36 == 0) goto L_0x02bd;	 Catch:{ Exception -> 0x03a4 }
                L_0x02a8:
                    r4 = NUM; // 0x42b40000 float:90.0 double:5.529052754E-315;	 Catch:{ Exception -> 0x03a4 }
                    r5 = NUM; // 0x42b40000 float:90.0 double:5.529052754E-315;	 Catch:{ Exception -> 0x03a4 }
                    r10 = 55;	 Catch:{ Exception -> 0x03a4 }
                    r0 = r36;	 Catch:{ Exception -> 0x03a4 }
                    r1 = r50;	 Catch:{ Exception -> 0x03a4 }
                    r4 = org.telegram.messenger.ImageLoader.scaleAndSaveImage(r0, r4, r5, r10, r1);	 Catch:{ Exception -> 0x03a4 }
                    r0 = r40;	 Catch:{ Exception -> 0x03a4 }
                    r0.thumb = r4;	 Catch:{ Exception -> 0x03a4 }
                    r36.recycle();	 Catch:{ Exception -> 0x03a4 }
                L_0x02bd:
                    r0 = r40;
                    r4 = r0.thumb;
                    if (r4 != 0) goto L_0x02fb;
                L_0x02c3:
                    r4 = new org.telegram.tgnet.TLRPC$TL_photoSize;
                    r4.<init>();
                    r0 = r40;
                    r0.thumb = r4;
                    r0 = r40;
                    r4 = r0.thumb;
                    r5 = r8.searchImage;
                    r5 = r5.width;
                    r4.f25w = r5;
                    r0 = r40;
                    r4 = r0.thumb;
                    r5 = r8.searchImage;
                    r5 = r5.height;
                    r4.f24h = r5;
                    r0 = r40;
                    r4 = r0.thumb;
                    r5 = 0;
                    r4.size = r5;
                    r0 = r40;
                    r4 = r0.thumb;
                    r5 = new org.telegram.tgnet.TLRPC$TL_fileLocationUnavailable;
                    r5.<init>();
                    r4.location = r5;
                    r0 = r40;
                    r4 = r0.thumb;
                    r5 = "x";
                    r4.type = r5;
                L_0x02fb:
                    r6 = r40;
                    r4 = r8.searchImage;
                    r0 = r4.imageUrl;
                    r59 = r0;
                    if (r37 != 0) goto L_0x03aa;
                L_0x0305:
                    r4 = r8.searchImage;
                    r7 = r4.imageUrl;
                L_0x0309:
                    if (r9 == 0) goto L_0x031b;
                L_0x030b:
                    r4 = r8.searchImage;
                    r4 = r4.imageUrl;
                    if (r4 == 0) goto L_0x031b;
                L_0x0311:
                    r4 = "originalPath";
                    r5 = r8.searchImage;
                    r5 = r5.imageUrl;
                    r9.put(r4, r5);
                L_0x031b:
                    r4 = new org.telegram.messenger.SendMessagesHelper$21$2;
                    r5 = r79;
                    r4.<init>(r6, r7, r8, r9);
                    org.telegram.messenger.AndroidUtilities.runOnUIThread(r4);
                L_0x0325:
                    r31 = r31 + 1;
                    goto L_0x0176;
                L_0x0329:
                    if (r50 != 0) goto L_0x034a;
                L_0x032b:
                    r0 = r79;
                    r4 = r4;
                    r5 = org.telegram.messenger.MessagesStorage.getInstance(r4);
                    r4 = r8.searchImage;
                    r10 = r4.imageUrl;
                    if (r50 != 0) goto L_0x0385;
                L_0x0339:
                    r4 = 1;
                L_0x033a:
                    r39 = r5.getSentFile(r10, r4);
                    r39 = (org.telegram.tgnet.TLRPC.Document) r39;
                    r0 = r39;
                    r4 = r0 instanceof org.telegram.tgnet.TLRPC.TL_document;
                    if (r4 == 0) goto L_0x034a;
                L_0x0346:
                    r40 = r39;
                    r40 = (org.telegram.tgnet.TLRPC.TL_document) r40;
                L_0x034a:
                    r4 = new java.lang.StringBuilder;
                    r4.<init>();
                    r5 = r8.searchImage;
                    r5 = r5.imageUrl;
                    r5 = org.telegram.messenger.Utilities.MD5(r5);
                    r4 = r4.append(r5);
                    r5 = ".";
                    r4 = r4.append(r5);
                    r5 = r8.searchImage;
                    r5 = r5.imageUrl;
                    r10 = "jpg";
                    r5 = org.telegram.messenger.ImageLoader.getHttpUrlExtension(r5, r10);
                    r4 = r4.append(r5);
                    r51 = r4.toString();
                    r37 = new java.io.File;
                    r4 = 4;
                    r4 = org.telegram.messenger.FileLoader.getDirectory(r4);
                    r0 = r37;
                    r1 = r51;
                    r0.<init>(r4, r1);
                    goto L_0x01d2;
                L_0x0385:
                    r4 = 4;
                    goto L_0x033a;
                L_0x0387:
                    r4 = "image/gif";
                    r0 = r40;
                    r0.mime_type = r4;
                    goto L_0x0243;
                L_0x0390:
                    r37 = 0;
                    goto L_0x024b;
                L_0x0394:
                    r4 = r75.getAbsolutePath();	 Catch:{ Exception -> 0x03a4 }
                    r5 = 0;	 Catch:{ Exception -> 0x03a4 }
                    r10 = NUM; // 0x42b40000 float:90.0 double:5.529052754E-315;	 Catch:{ Exception -> 0x03a4 }
                    r11 = NUM; // 0x42b40000 float:90.0 double:5.529052754E-315;	 Catch:{ Exception -> 0x03a4 }
                    r14 = 1;	 Catch:{ Exception -> 0x03a4 }
                    r36 = org.telegram.messenger.ImageLoader.loadBitmap(r4, r5, r10, r11, r14);	 Catch:{ Exception -> 0x03a4 }
                    goto L_0x02a6;
                L_0x03a4:
                    r41 = move-exception;
                    org.telegram.messenger.FileLog.m3e(r41);
                    goto L_0x02bd;
                L_0x03aa:
                    r7 = r37.toString();
                    goto L_0x0309;
                L_0x03b0:
                    r57 = 1;
                    r61 = 0;
                    if (r50 != 0) goto L_0x03cf;
                L_0x03b6:
                    r4 = r8.ttl;
                    if (r4 != 0) goto L_0x03cf;
                L_0x03ba:
                    r0 = r79;
                    r4 = r4;
                    r5 = org.telegram.messenger.MessagesStorage.getInstance(r4);
                    r4 = r8.searchImage;
                    r10 = r4.imageUrl;
                    if (r50 != 0) goto L_0x052f;
                L_0x03c8:
                    r4 = 0;
                L_0x03c9:
                    r61 = r5.getSentFile(r10, r4);
                    r61 = (org.telegram.tgnet.TLRPC.TL_photo) r61;
                L_0x03cf:
                    if (r61 != 0) goto L_0x04cb;
                L_0x03d1:
                    r4 = new java.lang.StringBuilder;
                    r4.<init>();
                    r5 = r8.searchImage;
                    r5 = r5.imageUrl;
                    r5 = org.telegram.messenger.Utilities.MD5(r5);
                    r4 = r4.append(r5);
                    r5 = ".";
                    r4 = r4.append(r5);
                    r5 = r8.searchImage;
                    r5 = r5.imageUrl;
                    r10 = "jpg";
                    r5 = org.telegram.messenger.ImageLoader.getHttpUrlExtension(r5, r10);
                    r4 = r4.append(r5);
                    r51 = r4.toString();
                    r37 = new java.io.File;
                    r4 = 4;
                    r4 = org.telegram.messenger.FileLoader.getDirectory(r4);
                    r0 = r37;
                    r1 = r51;
                    r0.<init>(r4, r1);
                    r4 = r37.exists();
                    if (r4 == 0) goto L_0x042f;
                L_0x0410:
                    r4 = r37.length();
                    r10 = 0;
                    r4 = (r4 > r10 ? 1 : (r4 == r10 ? 0 : -1));
                    if (r4 == 0) goto L_0x042f;
                L_0x041a:
                    r0 = r79;
                    r4 = r4;
                    r4 = org.telegram.messenger.SendMessagesHelper.getInstance(r4);
                    r5 = r37.toString();
                    r10 = 0;
                    r61 = r4.generatePhotoSizes(r5, r10);
                    if (r61 == 0) goto L_0x042f;
                L_0x042d:
                    r57 = 0;
                L_0x042f:
                    if (r61 != 0) goto L_0x04cb;
                L_0x0431:
                    r4 = new java.lang.StringBuilder;
                    r4.<init>();
                    r5 = r8.searchImage;
                    r5 = r5.thumbUrl;
                    r5 = org.telegram.messenger.Utilities.MD5(r5);
                    r4 = r4.append(r5);
                    r5 = ".";
                    r4 = r4.append(r5);
                    r5 = r8.searchImage;
                    r5 = r5.thumbUrl;
                    r10 = "jpg";
                    r5 = org.telegram.messenger.ImageLoader.getHttpUrlExtension(r5, r10);
                    r4 = r4.append(r5);
                    r51 = r4.toString();
                    r37 = new java.io.File;
                    r4 = 4;
                    r4 = org.telegram.messenger.FileLoader.getDirectory(r4);
                    r0 = r37;
                    r1 = r51;
                    r0.<init>(r4, r1);
                    r4 = r37.exists();
                    if (r4 == 0) goto L_0x0481;
                L_0x0470:
                    r0 = r79;
                    r4 = r4;
                    r4 = org.telegram.messenger.SendMessagesHelper.getInstance(r4);
                    r5 = r37.toString();
                    r10 = 0;
                    r61 = r4.generatePhotoSizes(r5, r10);
                L_0x0481:
                    if (r61 != 0) goto L_0x04cb;
                L_0x0483:
                    r61 = new org.telegram.tgnet.TLRPC$TL_photo;
                    r61.<init>();
                    r0 = r79;
                    r4 = r4;
                    r4 = org.telegram.tgnet.ConnectionsManager.getInstance(r4);
                    r4 = r4.getCurrentTime();
                    r0 = r61;
                    r0.date = r4;
                    r62 = new org.telegram.tgnet.TLRPC$TL_photoSize;
                    r62.<init>();
                    r4 = r8.searchImage;
                    r4 = r4.width;
                    r0 = r62;
                    r0.w = r4;
                    r4 = r8.searchImage;
                    r4 = r4.height;
                    r0 = r62;
                    r0.h = r4;
                    r4 = 0;
                    r0 = r62;
                    r0.size = r4;
                    r4 = new org.telegram.tgnet.TLRPC$TL_fileLocationUnavailable;
                    r4.<init>();
                    r0 = r62;
                    r0.location = r4;
                    r4 = "x";
                    r0 = r62;
                    r0.type = r4;
                    r0 = r61;
                    r4 = r0.sizes;
                    r0 = r62;
                    r4.add(r0);
                L_0x04cb:
                    if (r61 == 0) goto L_0x0325;
                L_0x04cd:
                    r12 = r61;
                    r13 = r57;
                    r9 = new java.util.HashMap;
                    r9.<init>();
                    r4 = r8.searchImage;
                    r4 = r4.imageUrl;
                    if (r4 == 0) goto L_0x04e6;
                L_0x04dc:
                    r4 = "originalPath";
                    r5 = r8.searchImage;
                    r5 = r5.imageUrl;
                    r9.put(r4, r5);
                L_0x04e6:
                    r0 = r79;
                    r4 = r6;
                    if (r4 == 0) goto L_0x0521;
                L_0x04ec:
                    r63 = r63 + 1;
                    r4 = "groupId";
                    r5 = new java.lang.StringBuilder;
                    r5.<init>();
                    r10 = "";
                    r5 = r5.append(r10);
                    r0 = r46;
                    r5 = r5.append(r0);
                    r5 = r5.toString();
                    r9.put(r4, r5);
                    r4 = 10;
                    r0 = r63;
                    if (r0 == r4) goto L_0x0516;
                L_0x0510:
                    r4 = r38 + -1;
                    r0 = r31;
                    if (r0 != r4) goto L_0x0521;
                L_0x0516:
                    r4 = "final";
                    r5 = "1";
                    r9.put(r4, r5);
                    r52 = 0;
                L_0x0521:
                    r10 = new org.telegram.messenger.SendMessagesHelper$21$3;
                    r11 = r79;
                    r14 = r8;
                    r15 = r9;
                    r10.<init>(r12, r13, r14, r15);
                    org.telegram.messenger.AndroidUtilities.runOnUIThread(r10);
                    goto L_0x0325;
                L_0x052f:
                    r4 = 3;
                    goto L_0x03c9;
                L_0x0532:
                    r4 = r8.isVideo;
                    if (r4 == 0) goto L_0x0845;
                L_0x0536:
                    r74 = 0;
                    r76 = 0;
                    r0 = r79;
                    r4 = r5;
                    if (r4 == 0) goto L_0x0786;
                L_0x0540:
                    r19 = 0;
                L_0x0542:
                    r0 = r79;
                    r4 = r5;
                    if (r4 != 0) goto L_0x081a;
                L_0x0548:
                    if (r19 != 0) goto L_0x0555;
                L_0x054a:
                    r4 = r8.path;
                    r5 = "mp4";
                    r4 = r4.endsWith(r5);
                    if (r4 == 0) goto L_0x081a;
                L_0x0555:
                    r0 = r8.path;
                    r60 = r0;
                    r0 = r8.path;
                    r58 = r0;
                    r72 = new java.io.File;
                    r0 = r72;
                    r1 = r58;
                    r0.<init>(r1);
                    r70 = 0;
                    r56 = 0;
                    r4 = new java.lang.StringBuilder;
                    r4.<init>();
                    r0 = r58;
                    r4 = r4.append(r0);
                    r10 = r72.length();
                    r4 = r4.append(r10);
                    r5 = "_";
                    r4 = r4.append(r5);
                    r10 = r72.lastModified();
                    r4 = r4.append(r10);
                    r58 = r4.toString();
                    if (r19 == 0) goto L_0x0612;
                L_0x0592:
                    r0 = r19;
                    r0 = r0.muted;
                    r56 = r0;
                    r4 = new java.lang.StringBuilder;
                    r4.<init>();
                    r0 = r58;
                    r4 = r4.append(r0);
                    r0 = r19;
                    r10 = r0.estimatedDuration;
                    r4 = r4.append(r10);
                    r5 = "_";
                    r4 = r4.append(r5);
                    r0 = r19;
                    r10 = r0.startTime;
                    r4 = r4.append(r10);
                    r5 = "_";
                    r4 = r4.append(r5);
                    r0 = r19;
                    r10 = r0.endTime;
                    r5 = r4.append(r10);
                    r0 = r19;
                    r4 = r0.muted;
                    if (r4 == 0) goto L_0x0797;
                L_0x05cf:
                    r4 = "_m";
                L_0x05d2:
                    r4 = r5.append(r4);
                    r58 = r4.toString();
                    r0 = r19;
                    r4 = r0.resultWidth;
                    r0 = r19;
                    r5 = r0.originalWidth;
                    if (r4 == r5) goto L_0x0602;
                L_0x05e4:
                    r4 = new java.lang.StringBuilder;
                    r4.<init>();
                    r0 = r58;
                    r4 = r4.append(r0);
                    r5 = "_";
                    r4 = r4.append(r5);
                    r0 = r19;
                    r5 = r0.resultWidth;
                    r4 = r4.append(r5);
                    r58 = r4.toString();
                L_0x0602:
                    r0 = r19;
                    r4 = r0.startTime;
                    r10 = 0;
                    r4 = (r4 > r10 ? 1 : (r4 == r10 ? 0 : -1));
                    if (r4 < 0) goto L_0x079c;
                L_0x060c:
                    r0 = r19;
                    r0 = r0.startTime;
                    r70 = r0;
                L_0x0612:
                    r40 = 0;
                    if (r50 != 0) goto L_0x062d;
                L_0x0616:
                    r4 = r8.ttl;
                    if (r4 != 0) goto L_0x062d;
                L_0x061a:
                    r0 = r79;
                    r4 = r4;
                    r5 = org.telegram.messenger.MessagesStorage.getInstance(r4);
                    if (r50 != 0) goto L_0x07a0;
                L_0x0624:
                    r4 = 2;
                L_0x0625:
                    r0 = r58;
                    r40 = r5.getSentFile(r0, r4);
                    r40 = (org.telegram.tgnet.TLRPC.TL_document) r40;
                L_0x062d:
                    if (r40 != 0) goto L_0x0720;
                L_0x062f:
                    r4 = r8.path;
                    r0 = r70;
                    r74 = org.telegram.messenger.SendMessagesHelper.createVideoThumbnail(r4, r0);
                    if (r74 != 0) goto L_0x0640;
                L_0x0639:
                    r4 = r8.path;
                    r5 = 1;
                    r74 = android.media.ThumbnailUtils.createVideoThumbnail(r4, r5);
                L_0x0640:
                    r4 = NUM; // 0x42b40000 float:90.0 double:5.529052754E-315;
                    r5 = NUM; // 0x42b40000 float:90.0 double:5.529052754E-315;
                    r10 = 55;
                    r0 = r74;
                    r1 = r50;
                    r69 = org.telegram.messenger.ImageLoader.scaleAndSaveImage(r0, r4, r5, r10, r1);
                    if (r74 == 0) goto L_0x0654;
                L_0x0650:
                    if (r69 == 0) goto L_0x0654;
                L_0x0652:
                    r74 = 0;
                L_0x0654:
                    r40 = new org.telegram.tgnet.TLRPC$TL_document;
                    r40.<init>();
                    r0 = r69;
                    r1 = r40;
                    r1.thumb = r0;
                    r0 = r40;
                    r4 = r0.thumb;
                    if (r4 != 0) goto L_0x07a3;
                L_0x0665:
                    r4 = new org.telegram.tgnet.TLRPC$TL_photoSizeEmpty;
                    r4.<init>();
                    r0 = r40;
                    r0.thumb = r4;
                    r0 = r40;
                    r4 = r0.thumb;
                    r5 = "s";
                    r4.type = r5;
                L_0x0677:
                    r4 = "video/mp4";
                    r0 = r40;
                    r0.mime_type = r4;
                    r0 = r79;
                    r4 = r4;
                    r4 = org.telegram.messenger.UserConfig.getInstance(r4);
                    r5 = 0;
                    r4.saveConfig(r5);
                    if (r50 == 0) goto L_0x07b5;
                L_0x068c:
                    r4 = 66;
                    r0 = r43;
                    if (r0 < r4) goto L_0x07ae;
                L_0x0692:
                    r32 = new org.telegram.tgnet.TLRPC$TL_documentAttributeVideo;
                    r32.<init>();
                L_0x0697:
                    r0 = r40;
                    r4 = r0.attributes;
                    r0 = r32;
                    r4.add(r0);
                    if (r19 == 0) goto L_0x0801;
                L_0x06a2:
                    r4 = r19.needConvert();
                    if (r4 == 0) goto L_0x0801;
                L_0x06a8:
                    r0 = r19;
                    r4 = r0.muted;
                    if (r4 == 0) goto L_0x07c1;
                L_0x06ae:
                    r0 = r40;
                    r4 = r0.attributes;
                    r5 = new org.telegram.tgnet.TLRPC$TL_documentAttributeAnimated;
                    r5.<init>();
                    r4.add(r5);
                    r4 = r8.path;
                    r0 = r32;
                    r1 = r19;
                    org.telegram.messenger.SendMessagesHelper.fillVideoAttribute(r4, r0, r1);
                    r0 = r32;
                    r4 = r0.w;
                    r0 = r19;
                    r0.originalWidth = r4;
                    r0 = r32;
                    r4 = r0.h;
                    r0 = r19;
                    r0.originalHeight = r4;
                    r0 = r19;
                    r4 = r0.resultWidth;
                    r0 = r32;
                    r0.w = r4;
                    r0 = r19;
                    r4 = r0.resultHeight;
                    r0 = r32;
                    r0.h = r4;
                L_0x06e3:
                    r0 = r19;
                    r4 = r0.estimatedSize;
                    r4 = (int) r4;
                    r0 = r40;
                    r0.size = r4;
                    r4 = new java.lang.StringBuilder;
                    r4.<init>();
                    r5 = "-2147483648_";
                    r4 = r4.append(r5);
                    r5 = org.telegram.messenger.SharedConfig.getLastLocalId();
                    r4 = r4.append(r5);
                    r5 = ".mp4";
                    r4 = r4.append(r5);
                    r45 = r4.toString();
                    r37 = new java.io.File;
                    r4 = 4;
                    r4 = org.telegram.messenger.FileLoader.getDirectory(r4);
                    r0 = r37;
                    r1 = r45;
                    r0.<init>(r4, r1);
                    org.telegram.messenger.SharedConfig.saveConfig();
                    r60 = r37.getAbsolutePath();
                L_0x0720:
                    r18 = r40;
                    r59 = r58;
                    r20 = r60;
                    r9 = new java.util.HashMap;
                    r9.<init>();
                    r16 = r74;
                    r17 = r76;
                    if (r58 == 0) goto L_0x0739;
                L_0x0731:
                    r4 = "originalPath";
                    r0 = r58;
                    r9.put(r4, r0);
                L_0x0739:
                    if (r56 != 0) goto L_0x0776;
                L_0x073b:
                    r0 = r79;
                    r4 = r6;
                    if (r4 == 0) goto L_0x0776;
                L_0x0741:
                    r63 = r63 + 1;
                    r4 = "groupId";
                    r5 = new java.lang.StringBuilder;
                    r5.<init>();
                    r10 = "";
                    r5 = r5.append(r10);
                    r0 = r46;
                    r5 = r5.append(r0);
                    r5 = r5.toString();
                    r9.put(r4, r5);
                    r4 = 10;
                    r0 = r63;
                    if (r0 == r4) goto L_0x076b;
                L_0x0765:
                    r4 = r38 + -1;
                    r0 = r31;
                    if (r0 != r4) goto L_0x0776;
                L_0x076b:
                    r4 = "final";
                    r5 = "1";
                    r9.put(r4, r5);
                    r52 = 0;
                L_0x0776:
                    r14 = new org.telegram.messenger.SendMessagesHelper$21$4;
                    r15 = r79;
                    r21 = r8;
                    r22 = r9;
                    r14.<init>(r16, r17, r18, r19, r20, r21, r22);
                    org.telegram.messenger.AndroidUtilities.runOnUIThread(r14);
                    goto L_0x0325;
                L_0x0786:
                    r4 = r8.videoEditedInfo;
                    if (r4 == 0) goto L_0x0790;
                L_0x078a:
                    r0 = r8.videoEditedInfo;
                    r19 = r0;
                L_0x078e:
                    goto L_0x0542;
                L_0x0790:
                    r4 = r8.path;
                    r19 = org.telegram.messenger.SendMessagesHelper.createCompressionSettings(r4);
                    goto L_0x078e;
                L_0x0797:
                    r4 = "";
                    goto L_0x05d2;
                L_0x079c:
                    r70 = 0;
                    goto L_0x0612;
                L_0x07a0:
                    r4 = 5;
                    goto L_0x0625;
                L_0x07a3:
                    r0 = r40;
                    r4 = r0.thumb;
                    r5 = "s";
                    r4.type = r5;
                    goto L_0x0677;
                L_0x07ae:
                    r32 = new org.telegram.tgnet.TLRPC$TL_documentAttributeVideo_layer65;
                    r32.<init>();
                    goto L_0x0697;
                L_0x07b5:
                    r32 = new org.telegram.tgnet.TLRPC$TL_documentAttributeVideo;
                    r32.<init>();
                    r4 = 1;
                    r0 = r32;
                    r0.supports_streaming = r4;
                    goto L_0x0697;
                L_0x07c1:
                    r0 = r19;
                    r4 = r0.estimatedDuration;
                    r10 = 1000; // 0x3e8 float:1.401E-42 double:4.94E-321;
                    r4 = r4 / r10;
                    r4 = (int) r4;
                    r0 = r32;
                    r0.duration = r4;
                    r0 = r19;
                    r4 = r0.rotationValue;
                    r5 = 90;
                    if (r4 == r5) goto L_0x07dd;
                L_0x07d5:
                    r0 = r19;
                    r4 = r0.rotationValue;
                    r5 = 270; // 0x10e float:3.78E-43 double:1.334E-321;
                    if (r4 != r5) goto L_0x07ef;
                L_0x07dd:
                    r0 = r19;
                    r4 = r0.resultHeight;
                    r0 = r32;
                    r0.w = r4;
                    r0 = r19;
                    r4 = r0.resultWidth;
                    r0 = r32;
                    r0.h = r4;
                    goto L_0x06e3;
                L_0x07ef:
                    r0 = r19;
                    r4 = r0.resultWidth;
                    r0 = r32;
                    r0.w = r4;
                    r0 = r19;
                    r4 = r0.resultHeight;
                    r0 = r32;
                    r0.h = r4;
                    goto L_0x06e3;
                L_0x0801:
                    r4 = r72.exists();
                    if (r4 == 0) goto L_0x0810;
                L_0x0807:
                    r4 = r72.length();
                    r4 = (int) r4;
                    r0 = r40;
                    r0.size = r4;
                L_0x0810:
                    r4 = r8.path;
                    r5 = 0;
                    r0 = r32;
                    org.telegram.messenger.SendMessagesHelper.fillVideoAttribute(r4, r0, r5);
                    goto L_0x0720;
                L_0x081a:
                    r0 = r79;
                    r0 = r4;
                    r21 = r0;
                    r0 = r8.path;
                    r22 = r0;
                    r0 = r8.path;
                    r23 = r0;
                    r24 = 0;
                    r25 = 0;
                    r0 = r79;
                    r0 = r2;
                    r26 = r0;
                    r0 = r79;
                    r0 = r7;
                    r28 = r0;
                    r0 = r8.caption;
                    r29 = r0;
                    r0 = r8.entities;
                    r30 = r0;
                    org.telegram.messenger.SendMessagesHelper.prepareSendingDocumentInternal(r21, r22, r23, r24, r25, r26, r28, r29, r30);
                    goto L_0x0325;
                L_0x0845:
                    r0 = r8.path;
                    r58 = r0;
                    r0 = r8.path;
                    r73 = r0;
                    if (r73 != 0) goto L_0x085f;
                L_0x084f:
                    r4 = r8.uri;
                    if (r4 == 0) goto L_0x085f;
                L_0x0853:
                    r4 = r8.uri;
                    r73 = org.telegram.messenger.AndroidUtilities.getPath(r4);
                    r4 = r8.uri;
                    r58 = r4.toString();
                L_0x085f:
                    r49 = 0;
                    r0 = r79;
                    r4 = r5;
                    if (r4 == 0) goto L_0x08aa;
                L_0x0867:
                    r49 = 1;
                    r4 = new java.io.File;
                    r0 = r73;
                    r4.<init>(r0);
                    r44 = org.telegram.messenger.FileLoader.getFileExtension(r4);
                L_0x0874:
                    if (r49 == 0) goto L_0x0919;
                L_0x0876:
                    if (r64 != 0) goto L_0x088c;
                L_0x0878:
                    r64 = new java.util.ArrayList;
                    r64.<init>();
                    r67 = new java.util.ArrayList;
                    r67.<init>();
                    r65 = new java.util.ArrayList;
                    r65.<init>();
                    r66 = new java.util.ArrayList;
                    r66.<init>();
                L_0x088c:
                    r0 = r64;
                    r1 = r73;
                    r0.add(r1);
                    r0 = r67;
                    r1 = r58;
                    r0.add(r1);
                    r4 = r8.caption;
                    r0 = r65;
                    r0.add(r4);
                    r4 = r8.entities;
                    r0 = r66;
                    r0.add(r4);
                    goto L_0x0325;
                L_0x08aa:
                    if (r73 == 0) goto L_0x08d7;
                L_0x08ac:
                    r4 = ".gif";
                    r0 = r73;
                    r4 = r0.endsWith(r4);
                    if (r4 != 0) goto L_0x08c2;
                L_0x08b7:
                    r4 = ".webp";
                    r0 = r73;
                    r4 = r0.endsWith(r4);
                    if (r4 == 0) goto L_0x08d7;
                L_0x08c2:
                    r4 = ".gif";
                    r0 = r73;
                    r4 = r0.endsWith(r4);
                    if (r4 == 0) goto L_0x08d3;
                L_0x08cd:
                    r44 = "gif";
                L_0x08d0:
                    r49 = 1;
                    goto L_0x0874;
                L_0x08d3:
                    r44 = "webp";
                    goto L_0x08d0;
                L_0x08d7:
                    if (r73 != 0) goto L_0x0874;
                L_0x08d9:
                    r4 = r8.uri;
                    if (r4 == 0) goto L_0x0874;
                L_0x08dd:
                    r4 = r8.uri;
                    r4 = org.telegram.messenger.MediaController.isGif(r4);
                    if (r4 == 0) goto L_0x08fb;
                L_0x08e5:
                    r49 = 1;
                    r4 = r8.uri;
                    r58 = r4.toString();
                    r4 = r8.uri;
                    r5 = "gif";
                    r73 = org.telegram.messenger.MediaController.copyFileToCache(r4, r5);
                    r44 = "gif";
                    goto L_0x0874;
                L_0x08fb:
                    r4 = r8.uri;
                    r4 = org.telegram.messenger.MediaController.isWebp(r4);
                    if (r4 == 0) goto L_0x0874;
                L_0x0903:
                    r49 = 1;
                    r4 = r8.uri;
                    r58 = r4.toString();
                    r4 = r8.uri;
                    r5 = "webp";
                    r73 = org.telegram.messenger.MediaController.copyFileToCache(r4, r5);
                    r44 = "webp";
                    goto L_0x0874;
                L_0x0919:
                    if (r73 == 0) goto L_0x09bd;
                L_0x091b:
                    r72 = new java.io.File;
                    r72.<init>(r73);
                    r4 = new java.lang.StringBuilder;
                    r4.<init>();
                    r0 = r58;
                    r4 = r4.append(r0);
                    r10 = r72.length();
                    r4 = r4.append(r10);
                    r5 = "_";
                    r4 = r4.append(r5);
                    r10 = r72.lastModified();
                    r4 = r4.append(r10);
                    r58 = r4.toString();
                L_0x0946:
                    r61 = 0;
                    if (r78 == 0) goto L_0x09c5;
                L_0x094a:
                    r0 = r78;
                    r77 = r0.get(r8);
                    r77 = (org.telegram.messenger.SendMessagesHelper.MediaSendPrepareWorker) r77;
                    r0 = r77;
                    r0 = r0.photo;
                    r61 = r0;
                    if (r61 != 0) goto L_0x0967;
                L_0x095a:
                    r0 = r77;	 Catch:{ Exception -> 0x09c0 }
                    r4 = r0.sync;	 Catch:{ Exception -> 0x09c0 }
                    r4.await();	 Catch:{ Exception -> 0x09c0 }
                L_0x0961:
                    r0 = r77;
                    r0 = r0.photo;
                    r61 = r0;
                L_0x0967:
                    if (r61 == 0) goto L_0x0a75;
                L_0x0969:
                    r12 = r61;
                    r9 = new java.util.HashMap;
                    r9.<init>();
                    r4 = r8.masks;
                    if (r4 == 0) goto L_0x0a13;
                L_0x0974:
                    r4 = r8.masks;
                    r4 = r4.isEmpty();
                    if (r4 != 0) goto L_0x0a13;
                L_0x097c:
                    r4 = 1;
                L_0x097d:
                    r0 = r61;
                    r0.has_stickers = r4;
                    if (r4 == 0) goto L_0x0a24;
                L_0x0983:
                    r68 = new org.telegram.tgnet.SerializedData;
                    r4 = r8.masks;
                    r4 = r4.size();
                    r4 = r4 * 20;
                    r4 = r4 + 4;
                    r0 = r68;
                    r0.<init>(r4);
                    r4 = r8.masks;
                    r4 = r4.size();
                    r0 = r68;
                    r0.writeInt32(r4);
                    r33 = 0;
                L_0x09a1:
                    r4 = r8.masks;
                    r4 = r4.size();
                    r0 = r33;
                    if (r0 >= r4) goto L_0x0a16;
                L_0x09ab:
                    r4 = r8.masks;
                    r0 = r33;
                    r4 = r4.get(r0);
                    r4 = (org.telegram.tgnet.TLRPC.InputDocument) r4;
                    r0 = r68;
                    r4.serializeToStream(r0);
                    r33 = r33 + 1;
                    goto L_0x09a1;
                L_0x09bd:
                    r58 = 0;
                    goto L_0x0946;
                L_0x09c0:
                    r41 = move-exception;
                    org.telegram.messenger.FileLog.m3e(r41);
                    goto L_0x0961;
                L_0x09c5:
                    if (r50 != 0) goto L_0x09fb;
                L_0x09c7:
                    r4 = r8.ttl;
                    if (r4 != 0) goto L_0x09fb;
                L_0x09cb:
                    r0 = r79;
                    r4 = r4;
                    r5 = org.telegram.messenger.MessagesStorage.getInstance(r4);
                    if (r50 != 0) goto L_0x0a0f;
                L_0x09d5:
                    r4 = 0;
                L_0x09d6:
                    r0 = r58;
                    r61 = r5.getSentFile(r0, r4);
                    r61 = (org.telegram.tgnet.TLRPC.TL_photo) r61;
                    if (r61 != 0) goto L_0x09fb;
                L_0x09e0:
                    r4 = r8.uri;
                    if (r4 == 0) goto L_0x09fb;
                L_0x09e4:
                    r0 = r79;
                    r4 = r4;
                    r5 = org.telegram.messenger.MessagesStorage.getInstance(r4);
                    r4 = r8.uri;
                    r10 = org.telegram.messenger.AndroidUtilities.getPath(r4);
                    if (r50 != 0) goto L_0x0a11;
                L_0x09f4:
                    r4 = 0;
                L_0x09f5:
                    r61 = r5.getSentFile(r10, r4);
                    r61 = (org.telegram.tgnet.TLRPC.TL_photo) r61;
                L_0x09fb:
                    if (r61 != 0) goto L_0x0967;
                L_0x09fd:
                    r0 = r79;
                    r4 = r4;
                    r4 = org.telegram.messenger.SendMessagesHelper.getInstance(r4);
                    r5 = r8.path;
                    r10 = r8.uri;
                    r61 = r4.generatePhotoSizes(r5, r10);
                    goto L_0x0967;
                L_0x0a0f:
                    r4 = 3;
                    goto L_0x09d6;
                L_0x0a11:
                    r4 = 3;
                    goto L_0x09f5;
                L_0x0a13:
                    r4 = 0;
                    goto L_0x097d;
                L_0x0a16:
                    r4 = "masks";
                    r5 = r68.toByteArray();
                    r5 = org.telegram.messenger.Utilities.bytesToHex(r5);
                    r9.put(r4, r5);
                L_0x0a24:
                    if (r58 == 0) goto L_0x0a2e;
                L_0x0a26:
                    r4 = "originalPath";
                    r0 = r58;
                    r9.put(r4, r0);
                L_0x0a2e:
                    r0 = r79;
                    r4 = r6;
                    if (r4 == 0) goto L_0x0a69;
                L_0x0a34:
                    r63 = r63 + 1;
                    r4 = "groupId";
                    r5 = new java.lang.StringBuilder;
                    r5.<init>();
                    r10 = "";
                    r5 = r5.append(r10);
                    r0 = r46;
                    r5 = r5.append(r0);
                    r5 = r5.toString();
                    r9.put(r4, r5);
                    r4 = 10;
                    r0 = r63;
                    if (r0 == r4) goto L_0x0a5e;
                L_0x0a58:
                    r4 = r38 + -1;
                    r0 = r31;
                    if (r0 != r4) goto L_0x0a69;
                L_0x0a5e:
                    r4 = "final";
                    r5 = "1";
                    r9.put(r4, r5);
                    r52 = 0;
                L_0x0a69:
                    r4 = new org.telegram.messenger.SendMessagesHelper$21$5;
                    r0 = r79;
                    r4.<init>(r12, r8, r9);
                    org.telegram.messenger.AndroidUtilities.runOnUIThread(r4);
                    goto L_0x0325;
                L_0x0a75:
                    if (r64 != 0) goto L_0x0a8b;
                L_0x0a77:
                    r64 = new java.util.ArrayList;
                    r64.<init>();
                    r67 = new java.util.ArrayList;
                    r67.<init>();
                    r65 = new java.util.ArrayList;
                    r65.<init>();
                    r66 = new java.util.ArrayList;
                    r66.<init>();
                L_0x0a8b:
                    r0 = r64;
                    r1 = r73;
                    r0.add(r1);
                    r0 = r67;
                    r1 = r58;
                    r0.add(r1);
                    r4 = r8.caption;
                    r0 = r65;
                    r0.add(r4);
                    r4 = r8.entities;
                    r0 = r66;
                    r0.add(r4);
                    goto L_0x0325;
                L_0x0aa9:
                    r4 = 0;
                    r4 = (r52 > r4 ? 1 : (r52 == r4 ? 0 : -1));
                    if (r4 == 0) goto L_0x0abd;
                L_0x0aaf:
                    r54 = r52;
                    r4 = new org.telegram.messenger.SendMessagesHelper$21$6;
                    r0 = r79;
                    r1 = r54;
                    r4.<init>(r1);
                    org.telegram.messenger.AndroidUtilities.runOnUIThread(r4);
                L_0x0abd:
                    r0 = r79;
                    r4 = r8;
                    if (r4 == 0) goto L_0x0aca;
                L_0x0ac3:
                    r0 = r79;
                    r4 = r8;
                    r4.releasePermission();
                L_0x0aca:
                    if (r64 == 0) goto L_0x0b20;
                L_0x0acc:
                    r4 = r64.isEmpty();
                    if (r4 != 0) goto L_0x0b20;
                L_0x0ad2:
                    r31 = 0;
                L_0x0ad4:
                    r4 = r64.size();
                    r0 = r31;
                    if (r0 >= r4) goto L_0x0b20;
                L_0x0adc:
                    r0 = r79;
                    r0 = r4;
                    r21 = r0;
                    r0 = r64;
                    r1 = r31;
                    r22 = r0.get(r1);
                    r22 = (java.lang.String) r22;
                    r0 = r67;
                    r1 = r31;
                    r23 = r0.get(r1);
                    r23 = (java.lang.String) r23;
                    r24 = 0;
                    r0 = r79;
                    r0 = r2;
                    r26 = r0;
                    r0 = r79;
                    r0 = r7;
                    r28 = r0;
                    r0 = r65;
                    r1 = r31;
                    r29 = r0.get(r1);
                    r29 = (java.lang.CharSequence) r29;
                    r0 = r66;
                    r1 = r31;
                    r30 = r0.get(r1);
                    r30 = (java.util.ArrayList) r30;
                    r25 = r44;
                    org.telegram.messenger.SendMessagesHelper.prepareSendingDocumentInternal(r21, r22, r23, r24, r25, r26, r28, r29, r30);
                    r31 = r31 + 1;
                    goto L_0x0ad4;
                L_0x0b20:
                    r4 = org.telegram.messenger.BuildVars.LOGS_ENABLED;
                    if (r4 == 0) goto L_0x0b41;
                L_0x0b24:
                    r4 = new java.lang.StringBuilder;
                    r4.<init>();
                    r5 = "total send time = ";
                    r4 = r4.append(r5);
                    r10 = java.lang.System.currentTimeMillis();
                    r10 = r10 - r34;
                    r4 = r4.append(r10);
                    r4 = r4.toString();
                    org.telegram.messenger.FileLog.m0d(r4);
                L_0x0b41:
                    return;
                    */
                    throw new UnsupportedOperationException("Method not decompiled: org.telegram.messenger.SendMessagesHelper.21.run():void");
                }
            });
        }
    }

    private static void fillVideoAttribute(String videoPath, TL_documentAttributeVideo attributeVideo, VideoEditedInfo videoEditedInfo) {
        Throwable e;
        MediaPlayer mp;
        Throwable th;
        boolean infoObtained = false;
        MediaMetadataRetriever mediaMetadataRetriever = null;
        try {
            MediaMetadataRetriever mediaMetadataRetriever2 = new MediaMetadataRetriever();
            try {
                mediaMetadataRetriever2.setDataSource(videoPath);
                String width = mediaMetadataRetriever2.extractMetadata(18);
                if (width != null) {
                    attributeVideo.w = Integer.parseInt(width);
                }
                String height = mediaMetadataRetriever2.extractMetadata(19);
                if (height != null) {
                    attributeVideo.h = Integer.parseInt(height);
                }
                String duration = mediaMetadataRetriever2.extractMetadata(9);
                if (duration != null) {
                    attributeVideo.duration = (int) Math.ceil((double) (((float) Long.parseLong(duration)) / 1000.0f));
                }
                if (VERSION.SDK_INT >= 17) {
                    String rotation = mediaMetadataRetriever2.extractMetadata(24);
                    if (rotation != null) {
                        int val = Utilities.parseInt(rotation).intValue();
                        if (videoEditedInfo != null) {
                            videoEditedInfo.rotationValue = val;
                        } else if (val == 90 || val == 270) {
                            int temp = attributeVideo.w;
                            attributeVideo.w = attributeVideo.h;
                            attributeVideo.h = temp;
                        }
                    }
                }
                infoObtained = true;
                if (mediaMetadataRetriever2 != null) {
                    try {
                        mediaMetadataRetriever2.release();
                    } catch (Throwable e2) {
                        FileLog.m3e(e2);
                        mediaMetadataRetriever = mediaMetadataRetriever2;
                    }
                }
                mediaMetadataRetriever = mediaMetadataRetriever2;
            } catch (Exception e3) {
                e2 = e3;
                mediaMetadataRetriever = mediaMetadataRetriever2;
                try {
                    FileLog.m3e(e2);
                    if (mediaMetadataRetriever != null) {
                        try {
                            mediaMetadataRetriever.release();
                        } catch (Throwable e22) {
                            FileLog.m3e(e22);
                        }
                    }
                    if (infoObtained) {
                        try {
                            mp = MediaPlayer.create(ApplicationLoader.applicationContext, Uri.fromFile(new File(videoPath)));
                            if (mp == null) {
                                attributeVideo.duration = (int) Math.ceil((double) (((float) mp.getDuration()) / 1000.0f));
                                attributeVideo.w = mp.getVideoWidth();
                                attributeVideo.h = mp.getVideoHeight();
                                mp.release();
                            }
                        } catch (Throwable e222) {
                            FileLog.m3e(e222);
                            return;
                        }
                    }
                } catch (Throwable th2) {
                    th = th2;
                    if (mediaMetadataRetriever != null) {
                        try {
                            mediaMetadataRetriever.release();
                        } catch (Throwable e2222) {
                            FileLog.m3e(e2222);
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                mediaMetadataRetriever = mediaMetadataRetriever2;
                if (mediaMetadataRetriever != null) {
                    mediaMetadataRetriever.release();
                }
                throw th;
            }
        } catch (Exception e4) {
            e2222 = e4;
            FileLog.m3e(e2222);
            if (mediaMetadataRetriever != null) {
                mediaMetadataRetriever.release();
            }
            if (infoObtained) {
                mp = MediaPlayer.create(ApplicationLoader.applicationContext, Uri.fromFile(new File(videoPath)));
                if (mp == null) {
                    attributeVideo.duration = (int) Math.ceil((double) (((float) mp.getDuration()) / 1000.0f));
                    attributeVideo.w = mp.getVideoWidth();
                    attributeVideo.h = mp.getVideoHeight();
                    mp.release();
                }
            }
        }
        if (infoObtained) {
            mp = MediaPlayer.create(ApplicationLoader.applicationContext, Uri.fromFile(new File(videoPath)));
            if (mp == null) {
                attributeVideo.duration = (int) Math.ceil((double) (((float) mp.getDuration()) / 1000.0f));
                attributeVideo.w = mp.getVideoWidth();
                attributeVideo.h = mp.getVideoHeight();
                mp.release();
            }
        }
    }

    private static Bitmap createVideoThumbnail(String filePath, long time) {
        Bitmap bitmap = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(filePath);
            bitmap = retriever.getFrameAtTime(time, 1);
            try {
                retriever.release();
            } catch (RuntimeException e) {
            }
        } catch (Exception e2) {
            try {
                retriever.release();
            } catch (RuntimeException e3) {
            }
        } catch (Throwable th) {
            try {
                retriever.release();
            } catch (RuntimeException e4) {
            }
            throw th;
        }
        if (bitmap == null) {
            return null;
        }
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int max = Math.max(width, height);
        if (max > 90) {
            float scale = 90.0f / ((float) max);
            Bitmap scaled = Bitmaps.createScaledBitmap(bitmap, Math.round(((float) width) * scale), Math.round(((float) height) * scale), true);
            if (scaled != bitmap) {
                bitmap.recycle();
                bitmap = scaled;
            }
        }
        Bitmap bitmap2 = bitmap;
        return bitmap;
    }

    private static VideoEditedInfo createCompressionSettings(String videoPath) {
        TrackHeaderBox trackHeaderBox = null;
        int originalBitrate = 0;
        int bitrate = 0;
        float videoDuration = 0.0f;
        long videoFramesSize = 0;
        long audioFramesSize = 0;
        int videoFramerate = 25;
        IsoFile isoFile = new IsoFile(videoPath);
        List<Box> boxes = Path.getPaths(isoFile, "/moov/trak/");
        if (Path.getPath(isoFile, "/moov/trak/mdia/minf/stbl/stsd/mp4a/") == null && BuildVars.LOGS_ENABLED) {
            FileLog.m0d("video hasn't mp4a atom");
        }
        if (Path.getPath(isoFile, "/moov/trak/mdia/minf/stbl/stsd/avc1/") == null) {
            if (BuildVars.LOGS_ENABLED) {
                FileLog.m0d("video hasn't avc1 atom");
            }
            return null;
        }
        int b = 0;
        while (b < boxes.size()) {
            int a;
            TrackBox trackBox = (TrackBox) ((Box) boxes.get(b));
            long sampleSizes = 0;
            long trackBitrate = 0;
            MediaBox mediaBox = null;
            MediaHeaderBox mediaHeaderBox = null;
            try {
                mediaBox = trackBox.getMediaBox();
                mediaHeaderBox = mediaBox.getMediaHeaderBox();
                for (long j : mediaBox.getMediaInformationBox().getSampleTableBox().getSampleSizeBox().getSampleSizes()) {
                    sampleSizes += j;
                }
                videoDuration = ((float) mediaHeaderBox.getDuration()) / ((float) mediaHeaderBox.getTimescale());
                trackBitrate = (long) ((int) (((float) (8 * sampleSizes)) / videoDuration));
            } catch (Throwable e) {
                FileLog.m3e(e);
            }
            try {
                TrackHeaderBox headerBox = trackBox.getTrackHeaderBox();
                if (headerBox.getWidth() == 0.0d || headerBox.getHeight() == 0.0d) {
                    audioFramesSize += sampleSizes;
                } else if (trackHeaderBox == null || trackHeaderBox.getWidth() < headerBox.getWidth() || trackHeaderBox.getHeight() < headerBox.getHeight()) {
                    trackHeaderBox = headerBox;
                    bitrate = (int) ((trackBitrate / 100000) * 100000);
                    originalBitrate = bitrate;
                    if (bitrate > 900000) {
                        bitrate = 900000;
                    }
                    videoFramesSize += sampleSizes;
                    if (!(mediaBox == null || mediaHeaderBox == null)) {
                        TimeToSampleBox timeToSampleBox = mediaBox.getMediaInformationBox().getSampleTableBox().getTimeToSampleBox();
                        if (timeToSampleBox != null) {
                            List<TimeToSampleBox.Entry> entries = timeToSampleBox.getEntries();
                            long delta = 0;
                            int size = Math.min(entries.size(), 11);
                            for (a = 1; a < size; a++) {
                                delta += ((TimeToSampleBox.Entry) entries.get(a)).getDelta();
                            }
                            if (delta != 0) {
                                videoFramerate = (int) (((double) mediaHeaderBox.getTimescale()) / ((double) (delta / ((long) (size - 1)))));
                            } else {
                                continue;
                            }
                        } else {
                            continue;
                        }
                    }
                }
                b++;
            } catch (Throwable e2) {
                FileLog.m3e(e2);
                return null;
            }
        }
        if (trackHeaderBox == null) {
            if (BuildVars.LOGS_ENABLED) {
                FileLog.m0d("video hasn't trackHeaderBox atom");
            }
            return null;
        }
        int compressionsCount;
        if (VERSION.SDK_INT < 18) {
            try {
                MediaCodecInfo codecInfo = MediaController.selectCodec("video/avc");
                if (codecInfo == null) {
                    if (BuildVars.LOGS_ENABLED) {
                        FileLog.m0d("no codec info for video/avc");
                    }
                    return null;
                }
                String name = codecInfo.getName();
                if (name.equals("OMX.google.h264.encoder") || name.equals("OMX.ST.VFM.H264Enc") || name.equals("OMX.Exynos.avc.enc") || name.equals("OMX.MARVELL.VIDEO.HW.CODA7542ENCODER") || name.equals("OMX.MARVELL.VIDEO.H264ENCODER") || name.equals("OMX.k3.video.encoder.avc") || name.equals("OMX.TI.DUCATI1.VIDEO.H264E")) {
                    if (BuildVars.LOGS_ENABLED) {
                        FileLog.m0d("unsupported encoder = " + name);
                    }
                    return null;
                } else if (MediaController.selectColorFormat(codecInfo, "video/avc") == 0) {
                    if (BuildVars.LOGS_ENABLED) {
                        FileLog.m0d("no color format for video/avc");
                    }
                    return null;
                }
            } catch (Exception e3) {
                return null;
            }
        }
        videoDuration *= 1000.0f;
        VideoEditedInfo videoEditedInfo = new VideoEditedInfo();
        videoEditedInfo.startTime = -1;
        videoEditedInfo.endTime = -1;
        videoEditedInfo.bitrate = bitrate;
        videoEditedInfo.originalPath = videoPath;
        videoEditedInfo.framerate = videoFramerate;
        videoEditedInfo.estimatedDuration = (long) Math.ceil((double) videoDuration);
        int width = (int) trackHeaderBox.getWidth();
        videoEditedInfo.originalWidth = width;
        videoEditedInfo.resultWidth = width;
        width = (int) trackHeaderBox.getHeight();
        videoEditedInfo.originalHeight = width;
        videoEditedInfo.resultHeight = width;
        Matrix matrix = trackHeaderBox.getMatrix();
        if (matrix.equals(Matrix.ROTATE_90)) {
            videoEditedInfo.rotationValue = 90;
        } else if (matrix.equals(Matrix.ROTATE_180)) {
            videoEditedInfo.rotationValue = 180;
        } else if (matrix.equals(Matrix.ROTATE_270)) {
            videoEditedInfo.rotationValue = 270;
        } else {
            videoEditedInfo.rotationValue = 0;
        }
        int selectedCompression = MessagesController.getGlobalMainSettings().getInt("compress_video2", 1);
        if (videoEditedInfo.originalWidth > 1280 || videoEditedInfo.originalHeight > 1280) {
            compressionsCount = 5;
        } else if (videoEditedInfo.originalWidth > 848 || videoEditedInfo.originalHeight > 848) {
            compressionsCount = 4;
        } else if (videoEditedInfo.originalWidth > 640 || videoEditedInfo.originalHeight > 640) {
            compressionsCount = 3;
        } else if (videoEditedInfo.originalWidth > 480 || videoEditedInfo.originalHeight > 480) {
            compressionsCount = 2;
        } else {
            compressionsCount = 1;
        }
        if (selectedCompression >= compressionsCount) {
            selectedCompression = compressionsCount - 1;
        }
        if (selectedCompression != compressionsCount - 1) {
            float maxSize;
            int targetBitrate;
            float scale;
            switch (selectedCompression) {
                case 0:
                    maxSize = 432.0f;
                    targetBitrate = 400000;
                    break;
                case 1:
                    maxSize = 640.0f;
                    targetBitrate = 900000;
                    break;
                case 2:
                    maxSize = 848.0f;
                    targetBitrate = 1100000;
                    break;
                default:
                    targetBitrate = 2500000;
                    maxSize = 1280.0f;
                    break;
            }
            if (videoEditedInfo.originalWidth > videoEditedInfo.originalHeight) {
                scale = maxSize / ((float) videoEditedInfo.originalWidth);
            } else {
                scale = maxSize / ((float) videoEditedInfo.originalHeight);
            }
            videoEditedInfo.resultWidth = Math.round((((float) videoEditedInfo.originalWidth) * scale) / 2.0f) * 2;
            videoEditedInfo.resultHeight = Math.round((((float) videoEditedInfo.originalHeight) * scale) / 2.0f) * 2;
            if (bitrate != 0) {
                bitrate = Math.min(targetBitrate, (int) (((float) originalBitrate) / scale));
                videoFramesSize = (long) ((((float) (bitrate / 8)) * videoDuration) / 1000.0f);
            }
        }
        if (selectedCompression == compressionsCount - 1) {
            videoEditedInfo.resultWidth = videoEditedInfo.originalWidth;
            videoEditedInfo.resultHeight = videoEditedInfo.originalHeight;
            videoEditedInfo.bitrate = originalBitrate;
            videoEditedInfo.estimatedSize = (long) ((int) new File(videoPath).length());
            return videoEditedInfo;
        }
        videoEditedInfo.bitrate = bitrate;
        videoEditedInfo.estimatedSize = (long) ((int) (audioFramesSize + videoFramesSize));
        videoEditedInfo.estimatedSize += (videoEditedInfo.estimatedSize / 32768) * 16;
        return videoEditedInfo;
    }

    public static void prepareSendingVideo(String videoPath, long estimatedSize, long duration, int width, int height, VideoEditedInfo info, long dialog_id, MessageObject reply_to_msg, CharSequence caption, ArrayList<MessageEntity> entities, int ttl) {
        if (videoPath != null && videoPath.length() != 0) {
            final int currentAccount = UserConfig.selectedAccount;
            final VideoEditedInfo videoEditedInfo = info;
            final String str = videoPath;
            final long j = dialog_id;
            final long j2 = duration;
            final int i = ttl;
            final int i2 = height;
            final int i3 = width;
            final long j3 = estimatedSize;
            final CharSequence charSequence = caption;
            final MessageObject messageObject = reply_to_msg;
            final ArrayList<MessageEntity> arrayList = entities;
            new Thread(new Runnable() {
                public void run() {
                    final VideoEditedInfo videoEditedInfo = videoEditedInfo != null ? videoEditedInfo : SendMessagesHelper.createCompressionSettings(str);
                    boolean isEncrypted = ((int) j) == 0;
                    boolean isRound = videoEditedInfo != null && videoEditedInfo.roundVideo;
                    Bitmap thumb = null;
                    String thumbKey = null;
                    if (videoEditedInfo != null || str.endsWith("mp4") || isRound) {
                        String path = str;
                        String originalPath = str;
                        File file = new File(originalPath);
                        long startTime = 0;
                        originalPath = originalPath + file.length() + "_" + file.lastModified();
                        if (videoEditedInfo != null) {
                            if (!isRound) {
                                originalPath = originalPath + j2 + "_" + videoEditedInfo.startTime + "_" + videoEditedInfo.endTime + (videoEditedInfo.muted ? "_m" : TtmlNode.ANONYMOUS_REGION_ID);
                                if (videoEditedInfo.resultWidth != videoEditedInfo.originalWidth) {
                                    originalPath = originalPath + "_" + videoEditedInfo.resultWidth;
                                }
                            }
                            startTime = videoEditedInfo.startTime >= 0 ? videoEditedInfo.startTime : 0;
                        }
                        TL_document tL_document = null;
                        if (!isEncrypted && i == 0) {
                            tL_document = (TL_document) MessagesStorage.getInstance(currentAccount).getSentFile(originalPath, !isEncrypted ? 2 : 5);
                        }
                        if (tL_document == null) {
                            TL_documentAttributeVideo attributeVideo;
                            thumb = SendMessagesHelper.createVideoThumbnail(str, startTime);
                            if (thumb == null) {
                                thumb = ThumbnailUtils.createVideoThumbnail(str, 1);
                            }
                            PhotoSize size = ImageLoader.scaleAndSaveImage(thumb, 90.0f, 90.0f, 55, isEncrypted);
                            if (!(thumb == null || size == null)) {
                                if (!isRound) {
                                    thumb = null;
                                } else if (isEncrypted) {
                                    Utilities.blurBitmap(thumb, 7, VERSION.SDK_INT < 21 ? 0 : 1, thumb.getWidth(), thumb.getHeight(), thumb.getRowBytes());
                                    thumbKey = String.format(size.location.volume_id + "_" + size.location.local_id + "@%d_%d_b2", new Object[]{Integer.valueOf((int) (((float) AndroidUtilities.roundMessageSize) / AndroidUtilities.density)), Integer.valueOf((int) (((float) AndroidUtilities.roundMessageSize) / AndroidUtilities.density))});
                                } else {
                                    Utilities.blurBitmap(thumb, 3, VERSION.SDK_INT < 21 ? 0 : 1, thumb.getWidth(), thumb.getHeight(), thumb.getRowBytes());
                                    thumbKey = String.format(size.location.volume_id + "_" + size.location.local_id + "@%d_%d_b", new Object[]{Integer.valueOf((int) (((float) AndroidUtilities.roundMessageSize) / AndroidUtilities.density)), Integer.valueOf((int) (((float) AndroidUtilities.roundMessageSize) / AndroidUtilities.density))});
                                }
                            }
                            tL_document = new TL_document();
                            tL_document.thumb = size;
                            if (tL_document.thumb == null) {
                                tL_document.thumb = new TL_photoSizeEmpty();
                                tL_document.thumb.type = "s";
                            } else {
                                tL_document.thumb.type = "s";
                            }
                            tL_document.mime_type = MimeTypes.VIDEO_MP4;
                            UserConfig.getInstance(currentAccount).saveConfig(false);
                            if (isEncrypted) {
                                EncryptedChat encryptedChat = MessagesController.getInstance(currentAccount).getEncryptedChat(Integer.valueOf((int) (j >> 32)));
                                if (encryptedChat != null) {
                                    if (AndroidUtilities.getPeerLayerVersion(encryptedChat.layer) >= 66) {
                                        attributeVideo = new TL_documentAttributeVideo();
                                    } else {
                                        attributeVideo = new TL_documentAttributeVideo_layer65();
                                    }
                                } else {
                                    return;
                                }
                            }
                            attributeVideo = new TL_documentAttributeVideo();
                            attributeVideo.supports_streaming = true;
                            attributeVideo.round_message = isRound;
                            tL_document.attributes.add(attributeVideo);
                            if (videoEditedInfo == null || !videoEditedInfo.needConvert()) {
                                if (file.exists()) {
                                    tL_document.size = (int) file.length();
                                }
                                SendMessagesHelper.fillVideoAttribute(str, attributeVideo, null);
                            } else {
                                if (videoEditedInfo.muted) {
                                    tL_document.attributes.add(new TL_documentAttributeAnimated());
                                    SendMessagesHelper.fillVideoAttribute(str, attributeVideo, videoEditedInfo);
                                    videoEditedInfo.originalWidth = attributeVideo.w;
                                    videoEditedInfo.originalHeight = attributeVideo.h;
                                    attributeVideo.w = videoEditedInfo.resultWidth;
                                    attributeVideo.h = videoEditedInfo.resultHeight;
                                } else {
                                    attributeVideo.duration = (int) (j2 / 1000);
                                    if (videoEditedInfo.rotationValue == 90 || videoEditedInfo.rotationValue == 270) {
                                        attributeVideo.w = i2;
                                        attributeVideo.h = i3;
                                    } else {
                                        attributeVideo.w = i3;
                                        attributeVideo.h = i2;
                                    }
                                }
                                tL_document.size = (int) j3;
                                file = new File(FileLoader.getDirectory(4), "-2147483648_" + SharedConfig.getLastLocalId() + ".mp4");
                                SharedConfig.saveConfig();
                                path = file.getAbsolutePath();
                            }
                        }
                        final TL_document videoFinal = tL_document;
                        String originalPathFinal = originalPath;
                        final String finalPath = path;
                        final HashMap<String, String> params = new HashMap();
                        final Bitmap thumbFinal = thumb;
                        final String thumbKeyFinal = thumbKey;
                        final String captionFinal = charSequence != null ? charSequence.toString() : TtmlNode.ANONYMOUS_REGION_ID;
                        if (originalPath != null) {
                            params.put("originalPath", originalPath);
                        }
                        AndroidUtilities.runOnUIThread(new Runnable() {
                            public void run() {
                                if (!(thumbFinal == null || thumbKeyFinal == null)) {
                                    ImageLoader.getInstance().putImageToCache(new BitmapDrawable(thumbFinal), thumbKeyFinal);
                                }
                                SendMessagesHelper.getInstance(currentAccount).sendMessage(videoFinal, videoEditedInfo, finalPath, j, messageObject, captionFinal, arrayList, null, params, i);
                            }
                        });
                        return;
                    }
                    SendMessagesHelper.prepareSendingDocumentInternal(currentAccount, str, str, null, null, j, messageObject, charSequence, arrayList);
                }
            }).start();
        }
    }
}
