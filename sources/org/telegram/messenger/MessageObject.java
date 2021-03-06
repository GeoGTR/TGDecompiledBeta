package org.telegram.messenger;

import android.content.Context;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.Base64;
import android.util.SparseArray;
import java.io.BufferedReader;
import java.io.File;
import java.io.StringReader;
import java.net.URLEncoder;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Pattern;
import org.telegram.PhoneFormat.PhoneFormat;
import org.telegram.messenger.Emoji;
import org.telegram.messenger.SvgHelper;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.SerializedData;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC$BotInlineResult;
import org.telegram.tgnet.TLRPC$Chat;
import org.telegram.tgnet.TLRPC$DecryptedMessageAction;
import org.telegram.tgnet.TLRPC$Document;
import org.telegram.tgnet.TLRPC$DocumentAttribute;
import org.telegram.tgnet.TLRPC$FileLocation;
import org.telegram.tgnet.TLRPC$InputStickerSet;
import org.telegram.tgnet.TLRPC$KeyboardButton;
import org.telegram.tgnet.TLRPC$Message;
import org.telegram.tgnet.TLRPC$MessageAction;
import org.telegram.tgnet.TLRPC$MessageEntity;
import org.telegram.tgnet.TLRPC$MessageFwdHeader;
import org.telegram.tgnet.TLRPC$MessageMedia;
import org.telegram.tgnet.TLRPC$Page;
import org.telegram.tgnet.TLRPC$PageBlock;
import org.telegram.tgnet.TLRPC$Peer;
import org.telegram.tgnet.TLRPC$Photo;
import org.telegram.tgnet.TLRPC$PhotoSize;
import org.telegram.tgnet.TLRPC$PollResults;
import org.telegram.tgnet.TLRPC$TL_channelAdminLogEvent;
import org.telegram.tgnet.TLRPC$TL_chatAdminRights;
import org.telegram.tgnet.TLRPC$TL_decryptedMessageActionScreenshotMessages;
import org.telegram.tgnet.TLRPC$TL_decryptedMessageActionSetMessageTTL;
import org.telegram.tgnet.TLRPC$TL_document;
import org.telegram.tgnet.TLRPC$TL_documentAttributeAnimated;
import org.telegram.tgnet.TLRPC$TL_documentAttributeAudio;
import org.telegram.tgnet.TLRPC$TL_documentAttributeHasStickers;
import org.telegram.tgnet.TLRPC$TL_documentAttributeImageSize;
import org.telegram.tgnet.TLRPC$TL_documentAttributeSticker;
import org.telegram.tgnet.TLRPC$TL_documentAttributeVideo;
import org.telegram.tgnet.TLRPC$TL_documentEmpty;
import org.telegram.tgnet.TLRPC$TL_documentEncrypted;
import org.telegram.tgnet.TLRPC$TL_fileLocationUnavailable;
import org.telegram.tgnet.TLRPC$TL_game;
import org.telegram.tgnet.TLRPC$TL_inputStickerSetEmpty;
import org.telegram.tgnet.TLRPC$TL_inputStickerSetShortName;
import org.telegram.tgnet.TLRPC$TL_keyboardButtonBuy;
import org.telegram.tgnet.TLRPC$TL_keyboardButtonRow;
import org.telegram.tgnet.TLRPC$TL_message;
import org.telegram.tgnet.TLRPC$TL_messageActionChatEditPhoto;
import org.telegram.tgnet.TLRPC$TL_messageActionEmpty;
import org.telegram.tgnet.TLRPC$TL_messageActionHistoryClear;
import org.telegram.tgnet.TLRPC$TL_messageActionLoginUnknownLocation;
import org.telegram.tgnet.TLRPC$TL_messageActionPhoneCall;
import org.telegram.tgnet.TLRPC$TL_messageActionUserUpdatedPhoto;
import org.telegram.tgnet.TLRPC$TL_messageEmpty;
import org.telegram.tgnet.TLRPC$TL_messageEncryptedAction;
import org.telegram.tgnet.TLRPC$TL_messageEntityItalic;
import org.telegram.tgnet.TLRPC$TL_messageEntityMentionName;
import org.telegram.tgnet.TLRPC$TL_messageForwarded_old2;
import org.telegram.tgnet.TLRPC$TL_messageMediaContact;
import org.telegram.tgnet.TLRPC$TL_messageMediaDice;
import org.telegram.tgnet.TLRPC$TL_messageMediaDocument;
import org.telegram.tgnet.TLRPC$TL_messageMediaEmpty;
import org.telegram.tgnet.TLRPC$TL_messageMediaGame;
import org.telegram.tgnet.TLRPC$TL_messageMediaGeo;
import org.telegram.tgnet.TLRPC$TL_messageMediaGeoLive;
import org.telegram.tgnet.TLRPC$TL_messageMediaInvoice;
import org.telegram.tgnet.TLRPC$TL_messageMediaPhoto;
import org.telegram.tgnet.TLRPC$TL_messageMediaPoll;
import org.telegram.tgnet.TLRPC$TL_messageMediaUnsupported;
import org.telegram.tgnet.TLRPC$TL_messageMediaVenue;
import org.telegram.tgnet.TLRPC$TL_messageMediaWebPage;
import org.telegram.tgnet.TLRPC$TL_messageReactions;
import org.telegram.tgnet.TLRPC$TL_messageReplies;
import org.telegram.tgnet.TLRPC$TL_messageReplyHeader;
import org.telegram.tgnet.TLRPC$TL_messageService;
import org.telegram.tgnet.TLRPC$TL_message_secret;
import org.telegram.tgnet.TLRPC$TL_pageBlockCollage;
import org.telegram.tgnet.TLRPC$TL_pageBlockPhoto;
import org.telegram.tgnet.TLRPC$TL_pageBlockSlideshow;
import org.telegram.tgnet.TLRPC$TL_pageBlockVideo;
import org.telegram.tgnet.TLRPC$TL_peerChannel;
import org.telegram.tgnet.TLRPC$TL_peerChat;
import org.telegram.tgnet.TLRPC$TL_peerUser;
import org.telegram.tgnet.TLRPC$TL_photo;
import org.telegram.tgnet.TLRPC$TL_photoCachedSize;
import org.telegram.tgnet.TLRPC$TL_photoEmpty;
import org.telegram.tgnet.TLRPC$TL_photoSizeEmpty;
import org.telegram.tgnet.TLRPC$TL_pollAnswer;
import org.telegram.tgnet.TLRPC$TL_pollAnswerVoters;
import org.telegram.tgnet.TLRPC$TL_reactionCount;
import org.telegram.tgnet.TLRPC$TL_replyInlineMarkup;
import org.telegram.tgnet.TLRPC$TL_webPage;
import org.telegram.tgnet.TLRPC$TL_webPageAttributeTheme;
import org.telegram.tgnet.TLRPC$User;
import org.telegram.tgnet.TLRPC$VideoSize;
import org.telegram.tgnet.TLRPC$WebDocument;
import org.telegram.tgnet.TLRPC$WebPage;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.ChatMessageCell;
import org.telegram.ui.Components.URLSpanNoUnderlineBold;

public class MessageObject {
    private static final int LINES_PER_BLOCK = 10;
    public static final int MESSAGE_SEND_STATE_EDITING = 3;
    public static final int MESSAGE_SEND_STATE_SENDING = 1;
    public static final int MESSAGE_SEND_STATE_SEND_ERROR = 2;
    public static final int MESSAGE_SEND_STATE_SENT = 0;
    public static final int POSITION_FLAG_BOTTOM = 8;
    public static final int POSITION_FLAG_LEFT = 1;
    public static final int POSITION_FLAG_RIGHT = 2;
    public static final int POSITION_FLAG_TOP = 4;
    public static final int TYPE_ANIMATED_STICKER = 15;
    public static final int TYPE_PHOTO = 1;
    public static final int TYPE_POLL = 17;
    public static final int TYPE_ROUND_VIDEO = 5;
    public static final int TYPE_STICKER = 13;
    public static final int TYPE_VIDEO = 3;
    static final String[] excludeWords = {" vs. ", " vs ", " versus ", " ft. ", " ft ", " featuring ", " feat. ", " feat ", " presents ", " pres. ", " pres ", " and ", " & ", " . "};
    public static Pattern instagramUrlPattern;
    public static Pattern urlPattern;
    public static Pattern videoTimeUrlPattern;
    public boolean animateComments;
    public boolean attachPathExists;
    public int audioPlayerDuration;
    public float audioProgress;
    public int audioProgressMs;
    public int audioProgressSec;
    public StringBuilder botButtonsLayout;
    public float bufferedProgress;
    public Boolean cachedIsSupergroup;
    public boolean cancelEditing;
    public CharSequence caption;
    public ArrayList<TLRPC$TL_pollAnswer> checkedVotes;
    public int contentType;
    public int currentAccount;
    public TLRPC$TL_channelAdminLogEvent currentEvent;
    public String customReplyName;
    public String dateKey;
    public boolean deleted;
    public CharSequence editingMessage;
    public ArrayList<TLRPC$MessageEntity> editingMessageEntities;
    public boolean editingMessageSearchWebPage;
    public TLRPC$Document emojiAnimatedSticker;
    public String emojiAnimatedStickerColor;
    private int emojiOnlyCount;
    public long eventId;
    public float forceSeekTo;
    public boolean forceUpdate;
    private float generatedWithDensity;
    private int generatedWithMinSize;
    public float gifState;
    public boolean hadAnimationNotReadyLoading;
    public boolean hasRtl;
    public ArrayList<String> highlightedWords;
    public boolean isDateObject;
    public boolean isRestrictedMessage;
    private int isRoundVideoCached;
    public int lastLineWidth;
    private boolean layoutCreated;
    public int linesCount;
    public CharSequence linkDescription;
    public long loadedFileSize;
    public boolean loadingCancelled;
    public boolean localChannel;
    public boolean localEdit;
    public long localGroupId;
    public String localName;
    public long localSentGroupId;
    public boolean localSupergroup;
    public int localType;
    public String localUserName;
    public boolean mediaExists;
    public TLRPC$Message messageOwner;
    public CharSequence messageText;
    public String messageTrimmedToHighlight;
    public String monthKey;
    public SvgHelper.SvgDrawable pathThumb;
    public ArrayList<TLRPC$PhotoSize> photoThumbs;
    public ArrayList<TLRPC$PhotoSize> photoThumbs2;
    public TLObject photoThumbsObject;
    public TLObject photoThumbsObject2;
    public long pollLastCheckTime;
    public boolean pollVisibleOnScreen;
    public String previousAttachPath;
    public TLRPC$MessageMedia previousMedia;
    public String previousMessage;
    public ArrayList<TLRPC$MessageEntity> previousMessageEntities;
    public MessageObject replyMessageObject;
    public boolean resendAsIs;
    public boolean scheduled;
    public boolean shouldRemoveVideoEditedInfo;
    public int stableId;
    public int textHeight;
    public ArrayList<TextLayoutBlock> textLayoutBlocks;
    public int textWidth;
    public float textXOffset;
    public int type;
    public boolean useCustomPhoto;
    public CharSequence vCardData;
    public VideoEditedInfo videoEditedInfo;
    public boolean viewsReloaded;
    public int wantedBotKeyboardWidth;
    public boolean wasJustSent;
    public boolean wasUnread;

    public void checkForScam() {
    }

    public static class VCardData {
        private String company;
        private ArrayList<String> emails = new ArrayList<>();
        private ArrayList<String> phones = new ArrayList<>();

        public static CharSequence parse(String str) {
            int i;
            boolean z;
            VCardData vCardData;
            byte[] decodeQuotedPrintable;
            try {
                BufferedReader bufferedReader = new BufferedReader(new StringReader(str));
                z = false;
                vCardData = null;
                String str2 = null;
                while (true) {
                    String readLine = bufferedReader.readLine();
                    if (readLine == null) {
                        break;
                    } else if (!readLine.startsWith("PHOTO")) {
                        if (readLine.indexOf(58) >= 0) {
                            if (readLine.startsWith("BEGIN:VCARD")) {
                                vCardData = new VCardData();
                            } else if (readLine.startsWith("END:VCARD") && vCardData != null) {
                                z = true;
                            }
                        }
                        if (str2 != null) {
                            readLine = str2 + readLine;
                            str2 = null;
                        }
                        if (readLine.contains("=QUOTED-PRINTABLE")) {
                            if (readLine.endsWith("=")) {
                                str2 = readLine.substring(0, readLine.length() - 1);
                            }
                        }
                        int indexOf = readLine.indexOf(":");
                        int i2 = 2;
                        String[] strArr = indexOf >= 0 ? new String[]{readLine.substring(0, indexOf), readLine.substring(indexOf + 1).trim()} : new String[]{readLine.trim()};
                        if (strArr.length >= 2) {
                            if (vCardData != null) {
                                if (strArr[0].startsWith("ORG")) {
                                    String[] split = strArr[0].split(";");
                                    int length = split.length;
                                    int i3 = 0;
                                    String str3 = null;
                                    String str4 = null;
                                    while (i3 < length) {
                                        String[] split2 = split[i3].split("=");
                                        if (split2.length == i2) {
                                            if (split2[0].equals("CHARSET")) {
                                                str4 = split2[1];
                                            } else if (split2[0].equals("ENCODING")) {
                                                str3 = split2[1];
                                            }
                                        }
                                        i3++;
                                        i2 = 2;
                                    }
                                    vCardData.company = strArr[1];
                                    if (!(str3 == null || !str3.equalsIgnoreCase("QUOTED-PRINTABLE") || (decodeQuotedPrintable = AndroidUtilities.decodeQuotedPrintable(AndroidUtilities.getStringBytes(vCardData.company))) == null || decodeQuotedPrintable.length == 0)) {
                                        vCardData.company = new String(decodeQuotedPrintable, str4);
                                    }
                                    vCardData.company = vCardData.company.replace(';', ' ');
                                } else if (strArr[0].startsWith("TEL")) {
                                    if (strArr[1].length() > 0) {
                                        vCardData.phones.add(strArr[1]);
                                    }
                                } else if (strArr[0].startsWith("EMAIL")) {
                                    String str5 = strArr[1];
                                    if (str5.length() > 0) {
                                        vCardData.emails.add(str5);
                                    }
                                }
                            }
                        }
                    }
                }
                bufferedReader.close();
            } catch (Exception e) {
                FileLog.e((Throwable) e);
            } catch (Throwable unused) {
                return null;
            }
            if (!z) {
                return null;
            }
            StringBuilder sb = new StringBuilder();
            for (int i4 = 0; i4 < vCardData.phones.size(); i4++) {
                if (sb.length() > 0) {
                    sb.append(10);
                }
                String str6 = vCardData.phones.get(i4);
                if (!str6.contains("#")) {
                    if (!str6.contains("*")) {
                        sb.append(PhoneFormat.getInstance().format(str6));
                    }
                }
                sb.append(str6);
            }
            for (i = 0; i < vCardData.emails.size(); i++) {
                if (sb.length() > 0) {
                    sb.append(10);
                }
                sb.append(PhoneFormat.getInstance().format(vCardData.emails.get(i)));
            }
            if (!TextUtils.isEmpty(vCardData.company)) {
                if (sb.length() > 0) {
                    sb.append(10);
                }
                sb.append(vCardData.company);
            }
            return sb;
        }
    }

    public static class TextLayoutBlock {
        public int charactersEnd;
        public int charactersOffset;
        public byte directionFlags;
        public int height;
        public int heightByOffset;
        public StaticLayout textLayout;
        public float textYOffset;

        public boolean isRtl() {
            byte b = this.directionFlags;
            return (b & 1) != 0 && (b & 2) == 0;
        }
    }

    public static class GroupedMessagePosition {
        public float aspectRatio;
        public boolean edge;
        public int flags;
        public boolean last;
        public int leftSpanOffset;
        public byte maxX;
        public byte maxY;
        public byte minX;
        public byte minY;
        public float ph;
        public int pw;
        public float[] siblingHeights;
        public int spanSize;

        public void set(int i, int i2, int i3, int i4, int i5, float f, int i6) {
            this.minX = (byte) i;
            this.maxX = (byte) i2;
            this.minY = (byte) i3;
            this.maxY = (byte) i4;
            this.pw = i5;
            this.spanSize = i5;
            this.ph = f;
            this.flags = (byte) i6;
        }
    }

    public static class GroupedMessages {
        public long groupId;
        public boolean hasCaption;
        public boolean hasSibling;
        public boolean isDocuments;
        private int maxSizeWidth = 800;
        public ArrayList<MessageObject> messages = new ArrayList<>();
        public ArrayList<GroupedMessagePosition> posArray = new ArrayList<>();
        public HashMap<MessageObject, GroupedMessagePosition> positions = new HashMap<>();
        public final TransitionParams transitionParams = new TransitionParams();

        private static class MessageGroupedLayoutAttempt {
            public float[] heights;
            public int[] lineCounts;

            public MessageGroupedLayoutAttempt(int i, int i2, float f, float f2) {
                this.lineCounts = new int[]{i, i2};
                this.heights = new float[]{f, f2};
            }

            public MessageGroupedLayoutAttempt(int i, int i2, int i3, float f, float f2, float f3) {
                this.lineCounts = new int[]{i, i2, i3};
                this.heights = new float[]{f, f2, f3};
            }

            public MessageGroupedLayoutAttempt(int i, int i2, int i3, int i4, float f, float f2, float f3, float f4) {
                this.lineCounts = new int[]{i, i2, i3, i4};
                this.heights = new float[]{f, f2, f3, f4};
            }
        }

        private float multiHeight(float[] fArr, int i, int i2) {
            float f = 0.0f;
            while (i < i2) {
                f += fArr[i];
                i++;
            }
            return ((float) this.maxSizeWidth) / f;
        }

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v10, resolved type: int} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v11, resolved type: byte} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v87, resolved type: byte} */
        /* JADX WARNING: Code restructure failed: missing block: B:23:0x0063, code lost:
            if ((r8 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaInvoice) == false) goto L_0x0067;
         */
        /* JADX WARNING: Multi-variable type inference failed */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void calculate() {
            /*
                r37 = this;
                r0 = r37
                java.util.ArrayList<org.telegram.messenger.MessageObject$GroupedMessagePosition> r1 = r0.posArray
                r1.clear()
                java.util.HashMap<org.telegram.messenger.MessageObject, org.telegram.messenger.MessageObject$GroupedMessagePosition> r1 = r0.positions
                r1.clear()
                r1 = 800(0x320, float:1.121E-42)
                r0.maxSizeWidth = r1
                java.util.ArrayList<org.telegram.messenger.MessageObject> r1 = r0.messages
                int r1 = r1.size()
                r2 = 1
                if (r1 > r2) goto L_0x001a
                return
            L_0x001a:
                java.lang.StringBuilder r4 = new java.lang.StringBuilder
                r4.<init>()
                r5 = 0
                r0.hasSibling = r5
                r0.hasCaption = r5
                r6 = 1065353216(0x3var_, float:1.0)
                r7 = 0
                r8 = 0
                r9 = 1065353216(0x3var_, float:1.0)
                r10 = 0
                r11 = 0
            L_0x002c:
                r12 = 1067030938(0x3var_a, float:1.2)
                if (r7 >= r1) goto L_0x00d7
                java.util.ArrayList<org.telegram.messenger.MessageObject> r13 = r0.messages
                java.lang.Object r13 = r13.get(r7)
                org.telegram.messenger.MessageObject r13 = (org.telegram.messenger.MessageObject) r13
                if (r7 != 0) goto L_0x0076
                boolean r11 = r13.isOutOwner()
                if (r11 != 0) goto L_0x0067
                org.telegram.tgnet.TLRPC$Message r8 = r13.messageOwner
                org.telegram.tgnet.TLRPC$MessageFwdHeader r14 = r8.fwd_from
                if (r14 == 0) goto L_0x004b
                org.telegram.tgnet.TLRPC$Peer r14 = r14.saved_from_peer
                if (r14 != 0) goto L_0x0065
            L_0x004b:
                org.telegram.tgnet.TLRPC$Peer r14 = r8.from_id
                boolean r14 = r14 instanceof org.telegram.tgnet.TLRPC$TL_peerUser
                if (r14 == 0) goto L_0x0067
                org.telegram.tgnet.TLRPC$Peer r14 = r8.peer_id
                int r15 = r14.channel_id
                if (r15 != 0) goto L_0x0065
                int r14 = r14.chat_id
                if (r14 != 0) goto L_0x0065
                org.telegram.tgnet.TLRPC$MessageMedia r8 = r8.media
                boolean r14 = r8 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaGame
                if (r14 != 0) goto L_0x0065
                boolean r8 = r8 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaInvoice
                if (r8 == 0) goto L_0x0067
            L_0x0065:
                r8 = 1
                goto L_0x0068
            L_0x0067:
                r8 = 0
            L_0x0068:
                boolean r14 = r13.isMusic()
                if (r14 != 0) goto L_0x0074
                boolean r14 = r13.isDocument()
                if (r14 == 0) goto L_0x0076
            L_0x0074:
                r0.isDocuments = r2
            L_0x0076:
                java.util.ArrayList<org.telegram.tgnet.TLRPC$PhotoSize> r14 = r13.photoThumbs
                int r15 = org.telegram.messenger.AndroidUtilities.getPhotoSize()
                org.telegram.tgnet.TLRPC$PhotoSize r14 = org.telegram.messenger.FileLoader.getClosestPhotoSizeWithSize(r14, r15)
                org.telegram.messenger.MessageObject$GroupedMessagePosition r15 = new org.telegram.messenger.MessageObject$GroupedMessagePosition
                r15.<init>()
                int r3 = r1 + -1
                if (r7 != r3) goto L_0x008b
                r3 = 1
                goto L_0x008c
            L_0x008b:
                r3 = 0
            L_0x008c:
                r15.last = r3
                if (r14 != 0) goto L_0x0093
                r3 = 1065353216(0x3var_, float:1.0)
                goto L_0x009a
            L_0x0093:
                int r3 = r14.w
                float r3 = (float) r3
                int r14 = r14.h
                float r14 = (float) r14
                float r3 = r3 / r14
            L_0x009a:
                r15.aspectRatio = r3
                int r12 = (r3 > r12 ? 1 : (r3 == r12 ? 0 : -1))
                if (r12 <= 0) goto L_0x00a7
                java.lang.String r3 = "w"
                r4.append(r3)
                goto L_0x00b9
            L_0x00a7:
                r12 = 1061997773(0x3f4ccccd, float:0.8)
                int r3 = (r3 > r12 ? 1 : (r3 == r12 ? 0 : -1))
                if (r3 >= 0) goto L_0x00b4
                java.lang.String r3 = "n"
                r4.append(r3)
                goto L_0x00b9
            L_0x00b4:
                java.lang.String r3 = "q"
                r4.append(r3)
            L_0x00b9:
                float r3 = r15.aspectRatio
                float r9 = r9 + r3
                r12 = 1073741824(0x40000000, float:2.0)
                int r3 = (r3 > r12 ? 1 : (r3 == r12 ? 0 : -1))
                if (r3 <= 0) goto L_0x00c3
                r10 = 1
            L_0x00c3:
                java.util.HashMap<org.telegram.messenger.MessageObject, org.telegram.messenger.MessageObject$GroupedMessagePosition> r3 = r0.positions
                r3.put(r13, r15)
                java.util.ArrayList<org.telegram.messenger.MessageObject$GroupedMessagePosition> r3 = r0.posArray
                r3.add(r15)
                java.lang.CharSequence r3 = r13.caption
                if (r3 == 0) goto L_0x00d3
                r0.hasCaption = r2
            L_0x00d3:
                int r7 = r7 + 1
                goto L_0x002c
            L_0x00d7:
                boolean r3 = r0.isDocuments
                r7 = 1120403456(0x42CLASSNAME, float:100.0)
                r13 = 1000(0x3e8, float:1.401E-42)
                r14 = 3
                if (r3 == 0) goto L_0x011a
                r3 = 0
            L_0x00e1:
                if (r3 >= r1) goto L_0x0119
                java.util.ArrayList<org.telegram.messenger.MessageObject$GroupedMessagePosition> r4 = r0.posArray
                java.lang.Object r4 = r4.get(r3)
                org.telegram.messenger.MessageObject$GroupedMessagePosition r4 = (org.telegram.messenger.MessageObject.GroupedMessagePosition) r4
                int r8 = r4.flags
                r8 = r8 | r14
                r4.flags = r8
                if (r3 != 0) goto L_0x00f7
                r8 = r8 | 4
                r4.flags = r8
                goto L_0x0101
            L_0x00f7:
                int r9 = r1 + -1
                if (r3 != r9) goto L_0x0101
                r8 = r8 | 8
                r4.flags = r8
                r4.last = r2
            L_0x0101:
                r4.edge = r2
                r4.aspectRatio = r6
                r4.minX = r5
                r4.maxX = r5
                byte r8 = (byte) r3
                r4.minY = r8
                r4.maxY = r8
                r4.spanSize = r13
                int r8 = r0.maxSizeWidth
                r4.pw = r8
                r4.ph = r7
                int r3 = r3 + 1
                goto L_0x00e1
            L_0x0119:
                return
            L_0x011a:
                if (r8 == 0) goto L_0x0125
                int r3 = r0.maxSizeWidth
                int r3 = r3 + -50
                r0.maxSizeWidth = r3
                r3 = 250(0xfa, float:3.5E-43)
                goto L_0x0127
            L_0x0125:
                r3 = 200(0xc8, float:2.8E-43)
            L_0x0127:
                r8 = 1123024896(0x42var_, float:120.0)
                int r15 = org.telegram.messenger.AndroidUtilities.dp(r8)
                int r8 = org.telegram.messenger.AndroidUtilities.dp(r8)
                float r8 = (float) r8
                android.graphics.Point r13 = org.telegram.messenger.AndroidUtilities.displaySize
                int r12 = r13.x
                int r13 = r13.y
                int r12 = java.lang.Math.min(r12, r13)
                float r12 = (float) r12
                int r13 = r0.maxSizeWidth
                float r13 = (float) r13
                float r12 = r12 / r13
                float r8 = r8 / r12
                int r8 = (int) r8
                r12 = 1109393408(0x42200000, float:40.0)
                int r12 = org.telegram.messenger.AndroidUtilities.dp(r12)
                float r12 = (float) r12
                android.graphics.Point r13 = org.telegram.messenger.AndroidUtilities.displaySize
                int r6 = r13.x
                int r13 = r13.y
                int r6 = java.lang.Math.min(r6, r13)
                float r6 = (float) r6
                int r13 = r0.maxSizeWidth
                float r2 = (float) r13
                float r6 = r6 / r2
                float r12 = r12 / r6
                int r2 = (int) r12
                float r6 = (float) r13
                r12 = 1145798656(0x444b8000, float:814.0)
                float r6 = r6 / r12
                float r13 = (float) r1
                float r9 = r9 / r13
                int r7 = org.telegram.messenger.AndroidUtilities.dp(r7)
                float r7 = (float) r7
                float r7 = r7 / r12
                r12 = 4
                r13 = 2
                if (r10 != 0) goto L_0x057f
                if (r1 == r13) goto L_0x0172
                if (r1 == r14) goto L_0x0172
                if (r1 != r12) goto L_0x057f
            L_0x0172:
                r10 = 1137410048(0x43cb8000, float:407.0)
                if (r1 != r13) goto L_0x02a3
                java.util.ArrayList<org.telegram.messenger.MessageObject$GroupedMessagePosition> r2 = r0.posArray
                java.lang.Object r2 = r2.get(r5)
                org.telegram.messenger.MessageObject$GroupedMessagePosition r2 = (org.telegram.messenger.MessageObject.GroupedMessagePosition) r2
                java.util.ArrayList<org.telegram.messenger.MessageObject$GroupedMessagePosition> r7 = r0.posArray
                r14 = 1
                java.lang.Object r7 = r7.get(r14)
                org.telegram.messenger.MessageObject$GroupedMessagePosition r7 = (org.telegram.messenger.MessageObject.GroupedMessagePosition) r7
                java.lang.String r4 = r4.toString()
                java.lang.String r14 = "ww"
                boolean r15 = r4.equals(r14)
                if (r15 == 0) goto L_0x01f3
                double r12 = (double) r9
                r19 = 4608983858650965606(0x3ffNUM, double:1.4)
                double r5 = (double) r6
                java.lang.Double.isNaN(r5)
                double r5 = r5 * r19
                int r9 = (r12 > r5 ? 1 : (r12 == r5 ? 0 : -1))
                if (r9 <= 0) goto L_0x01f3
                float r5 = r2.aspectRatio
                float r6 = r7.aspectRatio
                float r9 = r5 - r6
                double r12 = (double) r9
                r19 = 4596373779694328218(0x3fCLASSNAMEa, double:0.2)
                int r9 = (r12 > r19 ? 1 : (r12 == r19 ? 0 : -1))
                if (r9 >= 0) goto L_0x01f3
                int r4 = r0.maxSizeWidth
                float r8 = (float) r4
                float r8 = r8 / r5
                float r4 = (float) r4
                float r4 = r4 / r6
                float r4 = java.lang.Math.min(r4, r10)
                float r4 = java.lang.Math.min(r8, r4)
                int r4 = java.lang.Math.round(r4)
                float r4 = (float) r4
                r5 = 1145798656(0x444b8000, float:814.0)
                float r4 = r4 / r5
                r20 = 0
                r21 = 0
                r22 = 0
                r23 = 0
                int r5 = r0.maxSizeWidth
                r26 = 7
                r19 = r2
                r24 = r5
                r25 = r4
                r19.set(r20, r21, r22, r23, r24, r25, r26)
                r22 = 1
                r23 = 1
                int r2 = r0.maxSizeWidth
                r26 = 11
                r19 = r7
                r24 = r2
                r19.set(r20, r21, r22, r23, r24, r25, r26)
                r2 = 0
                goto L_0x02a0
            L_0x01f3:
                boolean r5 = r4.equals(r14)
                if (r5 != 0) goto L_0x0264
                java.lang.String r5 = "qq"
                boolean r4 = r4.equals(r5)
                if (r4 == 0) goto L_0x0202
                goto L_0x0264
            L_0x0202:
                int r4 = r0.maxSizeWidth
                float r5 = (float) r4
                r6 = 1053609165(0x3ecccccd, float:0.4)
                float r5 = r5 * r6
                float r4 = (float) r4
                float r6 = r2.aspectRatio
                float r4 = r4 / r6
                r9 = 1065353216(0x3var_, float:1.0)
                float r6 = r9 / r6
                float r10 = r7.aspectRatio
                float r9 = r9 / r10
                float r6 = r6 + r9
                float r4 = r4 / r6
                int r4 = java.lang.Math.round(r4)
                float r4 = (float) r4
                float r4 = java.lang.Math.max(r5, r4)
                int r4 = (int) r4
                int r5 = r0.maxSizeWidth
                int r5 = r5 - r4
                if (r5 >= r8) goto L_0x022a
                int r5 = r8 - r5
                int r4 = r4 - r5
                r5 = r8
            L_0x022a:
                float r6 = (float) r5
                float r8 = r2.aspectRatio
                float r6 = r6 / r8
                float r8 = (float) r4
                float r9 = r7.aspectRatio
                float r8 = r8 / r9
                float r6 = java.lang.Math.min(r6, r8)
                int r6 = java.lang.Math.round(r6)
                float r6 = (float) r6
                r8 = 1145798656(0x444b8000, float:814.0)
                float r6 = java.lang.Math.min(r8, r6)
                float r6 = r6 / r8
                r20 = 0
                r21 = 0
                r22 = 0
                r23 = 0
                r26 = 13
                r19 = r2
                r24 = r5
                r25 = r6
                r19.set(r20, r21, r22, r23, r24, r25, r26)
                r20 = 1
                r21 = 1
                r26 = 14
                r19 = r7
                r24 = r4
                r19.set(r20, r21, r22, r23, r24, r25, r26)
                goto L_0x029f
            L_0x0264:
                int r4 = r0.maxSizeWidth
                r5 = 2
                int r4 = r4 / r5
                float r5 = (float) r4
                float r6 = r2.aspectRatio
                float r6 = r5 / r6
                float r8 = r7.aspectRatio
                float r5 = r5 / r8
                r8 = 1145798656(0x444b8000, float:814.0)
                float r5 = java.lang.Math.min(r5, r8)
                float r5 = java.lang.Math.min(r6, r5)
                int r5 = java.lang.Math.round(r5)
                float r5 = (float) r5
                float r5 = r5 / r8
                r20 = 0
                r21 = 0
                r22 = 0
                r23 = 0
                r26 = 13
                r19 = r2
                r24 = r4
                r25 = r5
                r19.set(r20, r21, r22, r23, r24, r25, r26)
                r20 = 1
                r21 = 1
                r26 = 14
                r19 = r7
                r19.set(r20, r21, r22, r23, r24, r25, r26)
            L_0x029f:
                r2 = 1
            L_0x02a0:
                r14 = r2
                goto L_0x07a5
            L_0x02a3:
                r5 = 1141264221(0x44064f5d, float:537.24005)
                if (r1 != r14) goto L_0x03e0
                java.util.ArrayList<org.telegram.messenger.MessageObject$GroupedMessagePosition> r6 = r0.posArray
                r9 = 0
                java.lang.Object r6 = r6.get(r9)
                org.telegram.messenger.MessageObject$GroupedMessagePosition r6 = (org.telegram.messenger.MessageObject.GroupedMessagePosition) r6
                java.util.ArrayList<org.telegram.messenger.MessageObject$GroupedMessagePosition> r12 = r0.posArray
                r13 = 1
                java.lang.Object r12 = r12.get(r13)
                org.telegram.messenger.MessageObject$GroupedMessagePosition r12 = (org.telegram.messenger.MessageObject.GroupedMessagePosition) r12
                java.util.ArrayList<org.telegram.messenger.MessageObject$GroupedMessagePosition> r13 = r0.posArray
                r14 = 2
                java.lang.Object r13 = r13.get(r14)
                org.telegram.messenger.MessageObject$GroupedMessagePosition r13 = (org.telegram.messenger.MessageObject.GroupedMessagePosition) r13
                char r4 = r4.charAt(r9)
                r9 = 110(0x6e, float:1.54E-43)
                if (r4 != r9) goto L_0x0373
                float r4 = r12.aspectRatio
                int r5 = r0.maxSizeWidth
                float r5 = (float) r5
                float r5 = r5 * r4
                float r7 = r13.aspectRatio
                float r7 = r7 + r4
                float r5 = r5 / r7
                int r4 = java.lang.Math.round(r5)
                float r4 = (float) r4
                float r4 = java.lang.Math.min(r10, r4)
                r5 = 1145798656(0x444b8000, float:814.0)
                float r7 = r5 - r4
                float r5 = (float) r8
                int r8 = r0.maxSizeWidth
                float r8 = (float) r8
                r9 = 1056964608(0x3var_, float:0.5)
                float r8 = r8 * r9
                float r9 = r13.aspectRatio
                float r9 = r9 * r4
                float r10 = r12.aspectRatio
                float r10 = r10 * r7
                float r9 = java.lang.Math.min(r9, r10)
                int r9 = java.lang.Math.round(r9)
                float r9 = (float) r9
                float r8 = java.lang.Math.min(r8, r9)
                float r5 = java.lang.Math.max(r5, r8)
                int r5 = (int) r5
                float r8 = r6.aspectRatio
                r9 = 1145798656(0x444b8000, float:814.0)
                float r8 = r8 * r9
                float r2 = (float) r2
                float r8 = r8 + r2
                int r2 = r0.maxSizeWidth
                int r2 = r2 - r5
                float r2 = (float) r2
                float r2 = java.lang.Math.min(r8, r2)
                int r2 = java.lang.Math.round(r2)
                r20 = 0
                r21 = 0
                r22 = 0
                r23 = 1
                r25 = 1065353216(0x3var_, float:1.0)
                r26 = 13
                r19 = r6
                r24 = r2
                r19.set(r20, r21, r22, r23, r24, r25, r26)
                r20 = 1
                r21 = 1
                r23 = 0
                r8 = 1145798656(0x444b8000, float:814.0)
                float r7 = r7 / r8
                r26 = 6
                r19 = r12
                r24 = r5
                r25 = r7
                r19.set(r20, r21, r22, r23, r24, r25, r26)
                r20 = 0
                r22 = 1
                r23 = 1
                r8 = 1145798656(0x444b8000, float:814.0)
                float r4 = r4 / r8
                r26 = 10
                r19 = r13
                r25 = r4
                r19.set(r20, r21, r22, r23, r24, r25, r26)
                int r8 = r0.maxSizeWidth
                r13.spanSize = r8
                r9 = 2
                float[] r10 = new float[r9]
                r9 = 0
                r10[r9] = r4
                r4 = 1
                r10[r4] = r7
                r6.siblingHeights = r10
                if (r11 == 0) goto L_0x036b
                int r8 = r8 - r5
                r6.spanSize = r8
                goto L_0x0370
            L_0x036b:
                int r8 = r8 - r2
                r12.spanSize = r8
                r13.leftSpanOffset = r2
            L_0x0370:
                r0.hasSibling = r4
                goto L_0x03dd
            L_0x0373:
                int r2 = r0.maxSizeWidth
                float r2 = (float) r2
                float r4 = r6.aspectRatio
                float r2 = r2 / r4
                float r2 = java.lang.Math.min(r2, r5)
                int r2 = java.lang.Math.round(r2)
                float r2 = (float) r2
                r4 = 1145798656(0x444b8000, float:814.0)
                float r2 = r2 / r4
                r20 = 0
                r21 = 1
                r22 = 0
                r23 = 0
                int r4 = r0.maxSizeWidth
                r26 = 7
                r19 = r6
                r24 = r4
                r25 = r2
                r19.set(r20, r21, r22, r23, r24, r25, r26)
                int r4 = r0.maxSizeWidth
                r5 = 2
                int r4 = r4 / r5
                r5 = 1145798656(0x444b8000, float:814.0)
                float r2 = r5 - r2
                float r6 = (float) r4
                float r8 = r12.aspectRatio
                float r8 = r6 / r8
                float r9 = r13.aspectRatio
                float r6 = r6 / r9
                float r6 = java.lang.Math.min(r8, r6)
                int r6 = java.lang.Math.round(r6)
                float r6 = (float) r6
                float r2 = java.lang.Math.min(r2, r6)
                float r2 = r2 / r5
                int r5 = (r2 > r7 ? 1 : (r2 == r7 ? 0 : -1))
                if (r5 >= 0) goto L_0x03bf
                r2 = r7
            L_0x03bf:
                r20 = 0
                r21 = 0
                r22 = 1
                r23 = 1
                r26 = 9
                r19 = r12
                r24 = r4
                r25 = r2
                r19.set(r20, r21, r22, r23, r24, r25, r26)
                r20 = 1
                r21 = 1
                r26 = 10
                r19 = r13
                r19.set(r20, r21, r22, r23, r24, r25, r26)
            L_0x03dd:
                r14 = 1
                goto L_0x07a5
            L_0x03e0:
                java.util.ArrayList<org.telegram.messenger.MessageObject$GroupedMessagePosition> r6 = r0.posArray
                r9 = 0
                java.lang.Object r6 = r6.get(r9)
                org.telegram.messenger.MessageObject$GroupedMessagePosition r6 = (org.telegram.messenger.MessageObject.GroupedMessagePosition) r6
                java.util.ArrayList<org.telegram.messenger.MessageObject$GroupedMessagePosition> r10 = r0.posArray
                r12 = 1
                java.lang.Object r10 = r10.get(r12)
                org.telegram.messenger.MessageObject$GroupedMessagePosition r10 = (org.telegram.messenger.MessageObject.GroupedMessagePosition) r10
                java.util.ArrayList<org.telegram.messenger.MessageObject$GroupedMessagePosition> r12 = r0.posArray
                r13 = 2
                java.lang.Object r12 = r12.get(r13)
                org.telegram.messenger.MessageObject$GroupedMessagePosition r12 = (org.telegram.messenger.MessageObject.GroupedMessagePosition) r12
                java.util.ArrayList<org.telegram.messenger.MessageObject$GroupedMessagePosition> r13 = r0.posArray
                java.lang.Object r13 = r13.get(r14)
                org.telegram.messenger.MessageObject$GroupedMessagePosition r13 = (org.telegram.messenger.MessageObject.GroupedMessagePosition) r13
                char r4 = r4.charAt(r9)
                r9 = 119(0x77, float:1.67E-43)
                r14 = 1051260355(0x3ea8f5c3, float:0.33)
                if (r4 != r9) goto L_0x04c8
                int r2 = r0.maxSizeWidth
                float r2 = (float) r2
                float r4 = r6.aspectRatio
                float r2 = r2 / r4
                float r2 = java.lang.Math.min(r2, r5)
                int r2 = java.lang.Math.round(r2)
                float r2 = (float) r2
                r4 = 1145798656(0x444b8000, float:814.0)
                float r2 = r2 / r4
                r20 = 0
                r21 = 2
                r22 = 0
                r23 = 0
                int r4 = r0.maxSizeWidth
                r26 = 7
                r19 = r6
                r24 = r4
                r25 = r2
                r19.set(r20, r21, r22, r23, r24, r25, r26)
                int r4 = r0.maxSizeWidth
                float r4 = (float) r4
                float r5 = r10.aspectRatio
                float r6 = r12.aspectRatio
                float r5 = r5 + r6
                float r6 = r13.aspectRatio
                float r5 = r5 + r6
                float r4 = r4 / r5
                int r4 = java.lang.Math.round(r4)
                float r4 = (float) r4
                float r5 = (float) r8
                int r6 = r0.maxSizeWidth
                float r6 = (float) r6
                r8 = 1053609165(0x3ecccccd, float:0.4)
                float r6 = r6 * r8
                float r8 = r10.aspectRatio
                float r8 = r8 * r4
                float r6 = java.lang.Math.min(r6, r8)
                float r6 = java.lang.Math.max(r5, r6)
                int r6 = (int) r6
                int r8 = r0.maxSizeWidth
                float r8 = (float) r8
                float r8 = r8 * r14
                float r5 = java.lang.Math.max(r5, r8)
                float r8 = r13.aspectRatio
                float r8 = r8 * r4
                float r5 = java.lang.Math.max(r5, r8)
                int r5 = (int) r5
                int r8 = r0.maxSizeWidth
                int r8 = r8 - r6
                int r8 = r8 - r5
                r9 = 1114112000(0x42680000, float:58.0)
                int r14 = org.telegram.messenger.AndroidUtilities.dp(r9)
                if (r8 >= r14) goto L_0x0489
                int r14 = org.telegram.messenger.AndroidUtilities.dp(r9)
                int r14 = r14 - r8
                int r8 = org.telegram.messenger.AndroidUtilities.dp(r9)
                int r9 = r14 / 2
                int r6 = r6 - r9
                int r14 = r14 - r9
                int r5 = r5 - r14
            L_0x0489:
                r24 = r6
                r6 = 1145798656(0x444b8000, float:814.0)
                float r2 = r6 - r2
                float r2 = java.lang.Math.min(r2, r4)
                float r2 = r2 / r6
                int r4 = (r2 > r7 ? 1 : (r2 == r7 ? 0 : -1))
                if (r4 >= 0) goto L_0x049a
                r2 = r7
            L_0x049a:
                r20 = 0
                r21 = 0
                r22 = 1
                r23 = 1
                r26 = 9
                r19 = r10
                r25 = r2
                r19.set(r20, r21, r22, r23, r24, r25, r26)
                r20 = 1
                r21 = 1
                r26 = 8
                r19 = r12
                r24 = r8
                r19.set(r20, r21, r22, r23, r24, r25, r26)
                r20 = 2
                r21 = 2
                r26 = 10
                r19 = r13
                r24 = r5
                r19.set(r20, r21, r22, r23, r24, r25, r26)
                r2 = 2
                goto L_0x02a0
            L_0x04c8:
                float r4 = r10.aspectRatio
                r5 = 1065353216(0x3var_, float:1.0)
                float r4 = r5 / r4
                float r7 = r12.aspectRatio
                float r7 = r5 / r7
                float r4 = r4 + r7
                float r7 = r13.aspectRatio
                float r7 = r5 / r7
                float r4 = r4 + r7
                r5 = 1145798656(0x444b8000, float:814.0)
                float r4 = r5 / r4
                int r4 = java.lang.Math.round(r4)
                int r4 = java.lang.Math.max(r8, r4)
                float r7 = (float) r15
                float r8 = (float) r4
                float r9 = r10.aspectRatio
                float r9 = r8 / r9
                float r9 = java.lang.Math.max(r7, r9)
                float r9 = r9 / r5
                float r9 = java.lang.Math.min(r14, r9)
                float r15 = r12.aspectRatio
                float r8 = r8 / r15
                float r7 = java.lang.Math.max(r7, r8)
                float r7 = r7 / r5
                float r7 = java.lang.Math.min(r14, r7)
                r8 = 1065353216(0x3var_, float:1.0)
                float r8 = r8 - r9
                float r8 = r8 - r7
                float r14 = r6.aspectRatio
                float r5 = r5 * r14
                float r2 = (float) r2
                float r5 = r5 + r2
                int r2 = r0.maxSizeWidth
                int r2 = r2 - r4
                float r2 = (float) r2
                float r2 = java.lang.Math.min(r5, r2)
                int r2 = java.lang.Math.round(r2)
                r20 = 0
                r21 = 0
                r22 = 0
                r23 = 2
                float r5 = r9 + r7
                float r25 = r5 + r8
                r26 = 13
                r19 = r6
                r24 = r2
                r19.set(r20, r21, r22, r23, r24, r25, r26)
                r20 = 1
                r21 = 1
                r23 = 0
                r26 = 6
                r19 = r10
                r24 = r4
                r25 = r9
                r19.set(r20, r21, r22, r23, r24, r25, r26)
                r20 = 0
                r22 = 1
                r23 = 1
                r26 = 2
                r19 = r12
                r25 = r7
                r19.set(r20, r21, r22, r23, r24, r25, r26)
                int r5 = r0.maxSizeWidth
                r12.spanSize = r5
                r22 = 2
                r23 = 2
                r26 = 10
                r19 = r13
                r25 = r8
                r19.set(r20, r21, r22, r23, r24, r25, r26)
                int r5 = r0.maxSizeWidth
                r13.spanSize = r5
                if (r11 == 0) goto L_0x0566
                int r5 = r5 - r4
                r6.spanSize = r5
                goto L_0x056d
            L_0x0566:
                int r5 = r5 - r2
                r10.spanSize = r5
                r12.leftSpanOffset = r2
                r13.leftSpanOffset = r2
            L_0x056d:
                r2 = 3
                float[] r2 = new float[r2]
                r4 = 0
                r2[r4] = r9
                r4 = 1
                r2[r4] = r7
                r5 = 2
                r2[r5] = r8
                r6.siblingHeights = r2
                r0.hasSibling = r4
                goto L_0x029f
            L_0x057f:
                java.util.ArrayList<org.telegram.messenger.MessageObject$GroupedMessagePosition> r2 = r0.posArray
                int r2 = r2.size()
                float[] r4 = new float[r2]
                r5 = 0
            L_0x0588:
                if (r5 >= r1) goto L_0x05cb
                r6 = 1066192077(0x3f8ccccd, float:1.1)
                int r6 = (r9 > r6 ? 1 : (r9 == r6 ? 0 : -1))
                if (r6 <= 0) goto L_0x05a4
                java.util.ArrayList<org.telegram.messenger.MessageObject$GroupedMessagePosition> r6 = r0.posArray
                java.lang.Object r6 = r6.get(r5)
                org.telegram.messenger.MessageObject$GroupedMessagePosition r6 = (org.telegram.messenger.MessageObject.GroupedMessagePosition) r6
                float r6 = r6.aspectRatio
                r10 = 1065353216(0x3var_, float:1.0)
                float r6 = java.lang.Math.max(r10, r6)
                r4[r5] = r6
                goto L_0x05b6
            L_0x05a4:
                r10 = 1065353216(0x3var_, float:1.0)
                java.util.ArrayList<org.telegram.messenger.MessageObject$GroupedMessagePosition> r6 = r0.posArray
                java.lang.Object r6 = r6.get(r5)
                org.telegram.messenger.MessageObject$GroupedMessagePosition r6 = (org.telegram.messenger.MessageObject.GroupedMessagePosition) r6
                float r6 = r6.aspectRatio
                float r6 = java.lang.Math.min(r10, r6)
                r4[r5] = r6
            L_0x05b6:
                r6 = 1059760867(0x3f2aaae3, float:0.66667)
                r13 = 1071225242(0x3fd9999a, float:1.7)
                r14 = r4[r5]
                float r13 = java.lang.Math.min(r13, r14)
                float r6 = java.lang.Math.max(r6, r13)
                r4[r5] = r6
                int r5 = r5 + 1
                goto L_0x0588
            L_0x05cb:
                java.util.ArrayList r5 = new java.util.ArrayList
                r5.<init>()
                r6 = 1
            L_0x05d1:
                if (r6 >= r2) goto L_0x05ef
                int r10 = r2 - r6
                r13 = 3
                if (r6 > r13) goto L_0x05ec
                if (r10 <= r13) goto L_0x05db
                goto L_0x05ec
            L_0x05db:
                org.telegram.messenger.MessageObject$GroupedMessages$MessageGroupedLayoutAttempt r13 = new org.telegram.messenger.MessageObject$GroupedMessages$MessageGroupedLayoutAttempt
                r14 = 0
                float r15 = r0.multiHeight(r4, r14, r6)
                float r14 = r0.multiHeight(r4, r6, r2)
                r13.<init>(r6, r10, r15, r14)
                r5.add(r13)
            L_0x05ec:
                int r6 = r6 + 1
                goto L_0x05d1
            L_0x05ef:
                r6 = 1
            L_0x05f0:
                int r10 = r2 + -1
                if (r6 >= r10) goto L_0x0631
                r10 = 1
            L_0x05f5:
                int r13 = r2 - r6
                if (r10 >= r13) goto L_0x062e
                int r13 = r13 - r10
                r14 = 3
                if (r6 > r14) goto L_0x062b
                r15 = 1062836634(0x3var_a, float:0.85)
                int r15 = (r9 > r15 ? 1 : (r9 == r15 ? 0 : -1))
                if (r15 >= 0) goto L_0x0606
                r15 = 4
                goto L_0x0607
            L_0x0606:
                r15 = 3
            L_0x0607:
                if (r10 > r15) goto L_0x062b
                if (r13 <= r14) goto L_0x060c
                goto L_0x062b
            L_0x060c:
                org.telegram.messenger.MessageObject$GroupedMessages$MessageGroupedLayoutAttempt r14 = new org.telegram.messenger.MessageObject$GroupedMessages$MessageGroupedLayoutAttempt
                r15 = 0
                float r23 = r0.multiHeight(r4, r15, r6)
                int r15 = r6 + r10
                float r24 = r0.multiHeight(r4, r6, r15)
                float r25 = r0.multiHeight(r4, r15, r2)
                r19 = r14
                r20 = r6
                r21 = r10
                r22 = r13
                r19.<init>(r20, r21, r22, r23, r24, r25)
                r5.add(r14)
            L_0x062b:
                int r10 = r10 + 1
                goto L_0x05f5
            L_0x062e:
                int r6 = r6 + 1
                goto L_0x05f0
            L_0x0631:
                r6 = 1
            L_0x0632:
                int r9 = r2 + -2
                if (r6 >= r9) goto L_0x0685
                r9 = 1
            L_0x0637:
                int r10 = r2 - r6
                if (r9 >= r10) goto L_0x0681
                r13 = 1
            L_0x063c:
                int r14 = r10 - r9
                if (r13 >= r14) goto L_0x067d
                int r14 = r14 - r13
                r15 = 3
                if (r6 > r15) goto L_0x0675
                if (r9 > r15) goto L_0x0675
                if (r13 > r15) goto L_0x0675
                if (r14 <= r15) goto L_0x064b
                goto L_0x0675
            L_0x064b:
                org.telegram.messenger.MessageObject$GroupedMessages$MessageGroupedLayoutAttempt r15 = new org.telegram.messenger.MessageObject$GroupedMessages$MessageGroupedLayoutAttempt
                r12 = 0
                float r33 = r0.multiHeight(r4, r12, r6)
                int r12 = r6 + r9
                float r34 = r0.multiHeight(r4, r6, r12)
                r19 = r10
                int r10 = r12 + r13
                float r35 = r0.multiHeight(r4, r12, r10)
                float r36 = r0.multiHeight(r4, r10, r2)
                r28 = r15
                r29 = r6
                r30 = r9
                r31 = r13
                r32 = r14
                r28.<init>(r29, r30, r31, r32, r33, r34, r35, r36)
                r5.add(r15)
                goto L_0x0677
            L_0x0675:
                r19 = r10
            L_0x0677:
                int r13 = r13 + 1
                r10 = r19
                r12 = 4
                goto L_0x063c
            L_0x067d:
                int r9 = r9 + 1
                r12 = 4
                goto L_0x0637
            L_0x0681:
                int r6 = r6 + 1
                r12 = 4
                goto L_0x0632
            L_0x0685:
                int r2 = r0.maxSizeWidth
                r6 = 3
                int r2 = r2 / r6
                r6 = 4
                int r2 = r2 * 4
                float r2 = (float) r2
                r12 = 0
                r13 = 0
                r14 = 0
            L_0x0690:
                int r15 = r5.size()
                if (r12 >= r15) goto L_0x0713
                java.lang.Object r15 = r5.get(r12)
                org.telegram.messenger.MessageObject$GroupedMessages$MessageGroupedLayoutAttempt r15 = (org.telegram.messenger.MessageObject.GroupedMessages.MessageGroupedLayoutAttempt) r15
                r17 = 2139095039(0x7f7fffff, float:3.4028235E38)
                r6 = 0
                r20 = 0
            L_0x06a2:
                float[] r9 = r15.heights
                int r10 = r9.length
                if (r6 >= r10) goto L_0x06b8
                r10 = r9[r6]
                float r20 = r20 + r10
                r10 = r9[r6]
                int r10 = (r10 > r17 ? 1 : (r10 == r17 ? 0 : -1))
                if (r10 >= 0) goto L_0x06b5
                r9 = r9[r6]
                r17 = r9
            L_0x06b5:
                int r6 = r6 + 1
                goto L_0x06a2
            L_0x06b8:
                float r20 = r20 - r2
                float r6 = java.lang.Math.abs(r20)
                int[] r9 = r15.lineCounts
                int r10 = r9.length
                r20 = r2
                r2 = 1
                r23 = r5
                if (r10 <= r2) goto L_0x06f5
                r10 = 0
                r5 = r9[r10]
                r10 = r9[r2]
                if (r5 > r10) goto L_0x06ee
                int r5 = r9.length
                r10 = 2
                if (r5 <= r10) goto L_0x06df
                r5 = r9[r2]
                r2 = r9[r10]
                if (r5 > r2) goto L_0x06da
                goto L_0x06df
            L_0x06da:
                r2 = 1067030938(0x3var_a, float:1.2)
                r5 = 3
                goto L_0x06f2
            L_0x06df:
                int r2 = r9.length
                r5 = 3
                if (r2 <= r5) goto L_0x06ea
                r2 = r9[r10]
                r9 = r9[r5]
                if (r2 <= r9) goto L_0x06ea
                goto L_0x06ef
            L_0x06ea:
                r2 = 1067030938(0x3var_a, float:1.2)
                goto L_0x06f9
            L_0x06ee:
                r5 = 3
            L_0x06ef:
                r2 = 1067030938(0x3var_a, float:1.2)
            L_0x06f2:
                float r6 = r6 * r2
                goto L_0x06f9
            L_0x06f5:
                r2 = 1067030938(0x3var_a, float:1.2)
                r5 = 3
            L_0x06f9:
                float r9 = (float) r8
                int r9 = (r17 > r9 ? 1 : (r17 == r9 ? 0 : -1))
                if (r9 >= 0) goto L_0x0702
                r9 = 1069547520(0x3fCLASSNAME, float:1.5)
                float r6 = r6 * r9
            L_0x0702:
                if (r13 == 0) goto L_0x0708
                int r9 = (r6 > r14 ? 1 : (r6 == r14 ? 0 : -1))
                if (r9 >= 0) goto L_0x070a
            L_0x0708:
                r14 = r6
                r13 = r15
            L_0x070a:
                int r12 = r12 + 1
                r2 = r20
                r5 = r23
                r6 = 4
                goto L_0x0690
            L_0x0713:
                if (r13 != 0) goto L_0x0716
                return
            L_0x0716:
                r2 = 0
                r5 = 0
                r9 = 0
            L_0x0719:
                int[] r6 = r13.lineCounts
                int r8 = r6.length
                if (r9 >= r8) goto L_0x07a4
                r6 = r6[r9]
                float[] r8 = r13.heights
                r8 = r8[r9]
                int r10 = r0.maxSizeWidth
                int r12 = r6 + -1
                int r5 = java.lang.Math.max(r5, r12)
                r14 = r10
                r10 = 0
                r15 = 0
            L_0x072f:
                if (r10 >= r6) goto L_0x078d
                r16 = r4[r2]
                r17 = r4
                float r4 = r16 * r8
                int r4 = (int) r4
                int r14 = r14 - r4
                r16 = r5
                java.util.ArrayList<org.telegram.messenger.MessageObject$GroupedMessagePosition> r5 = r0.posArray
                java.lang.Object r5 = r5.get(r2)
                r27 = r5
                org.telegram.messenger.MessageObject$GroupedMessagePosition r27 = (org.telegram.messenger.MessageObject.GroupedMessagePosition) r27
                r20 = r6
                if (r9 != 0) goto L_0x074b
                r5 = 4
                goto L_0x074c
            L_0x074b:
                r5 = 0
            L_0x074c:
                int[] r6 = r13.lineCounts
                int r6 = r6.length
                r18 = 1
                int r6 = r6 + -1
                if (r9 != r6) goto L_0x0757
                r5 = r5 | 8
            L_0x0757:
                if (r10 != 0) goto L_0x075f
                r5 = r5 | 1
                if (r11 == 0) goto L_0x075f
                r15 = r27
            L_0x075f:
                if (r10 != r12) goto L_0x076a
                r5 = r5 | 2
                if (r11 != 0) goto L_0x076a
                r34 = r5
                r15 = r27
                goto L_0x076c
            L_0x076a:
                r34 = r5
            L_0x076c:
                r6 = 1145798656(0x444b8000, float:814.0)
                float r5 = r8 / r6
                float r33 = java.lang.Math.max(r7, r5)
                r28 = r10
                r29 = r10
                r30 = r9
                r31 = r9
                r32 = r4
                r27.set(r28, r29, r30, r31, r32, r33, r34)
                int r2 = r2 + 1
                int r10 = r10 + 1
                r5 = r16
                r4 = r17
                r6 = r20
                goto L_0x072f
            L_0x078d:
                r17 = r4
                r16 = r5
                r6 = 1145798656(0x444b8000, float:814.0)
                int r4 = r15.pw
                int r4 = r4 + r14
                r15.pw = r4
                int r4 = r15.spanSize
                int r4 = r4 + r14
                r15.spanSize = r4
                int r9 = r9 + 1
                r4 = r17
                goto L_0x0719
            L_0x07a4:
                r14 = r5
            L_0x07a5:
                r5 = 0
            L_0x07a6:
                if (r5 >= r1) goto L_0x0822
                java.util.ArrayList<org.telegram.messenger.MessageObject$GroupedMessagePosition> r2 = r0.posArray
                java.lang.Object r2 = r2.get(r5)
                org.telegram.messenger.MessageObject$GroupedMessagePosition r2 = (org.telegram.messenger.MessageObject.GroupedMessagePosition) r2
                if (r11 == 0) goto L_0x07c6
                byte r4 = r2.minX
                if (r4 != 0) goto L_0x07bb
                int r4 = r2.spanSize
                int r4 = r4 + r3
                r2.spanSize = r4
            L_0x07bb:
                int r4 = r2.flags
                r6 = 2
                r4 = r4 & r6
                if (r4 == 0) goto L_0x07c4
                r4 = 1
                r2.edge = r4
            L_0x07c4:
                r6 = 1
                goto L_0x07dd
            L_0x07c6:
                r6 = 2
                byte r4 = r2.maxX
                if (r4 == r14) goto L_0x07d0
                int r4 = r2.flags
                r4 = r4 & r6
                if (r4 == 0) goto L_0x07d5
            L_0x07d0:
                int r4 = r2.spanSize
                int r4 = r4 + r3
                r2.spanSize = r4
            L_0x07d5:
                int r4 = r2.flags
                r6 = 1
                r4 = r4 & r6
                if (r4 == 0) goto L_0x07dd
                r2.edge = r6
            L_0x07dd:
                java.util.ArrayList<org.telegram.messenger.MessageObject> r4 = r0.messages
                java.lang.Object r4 = r4.get(r5)
                org.telegram.messenger.MessageObject r4 = (org.telegram.messenger.MessageObject) r4
                if (r11 != 0) goto L_0x081c
                boolean r4 = r4.needDrawAvatarInternal()
                if (r4 == 0) goto L_0x081c
                boolean r4 = r2.edge
                if (r4 == 0) goto L_0x0802
                int r4 = r2.spanSize
                r7 = 1000(0x3e8, float:1.401E-42)
                if (r4 == r7) goto L_0x07fb
                int r4 = r4 + 108
                r2.spanSize = r4
            L_0x07fb:
                int r4 = r2.pw
                int r4 = r4 + 108
                r2.pw = r4
                goto L_0x081c
            L_0x0802:
                int r4 = r2.flags
                r7 = 2
                r4 = r4 & r7
                if (r4 == 0) goto L_0x081d
                int r4 = r2.spanSize
                r8 = 1000(0x3e8, float:1.401E-42)
                if (r4 == r8) goto L_0x0813
                int r4 = r4 + -108
                r2.spanSize = r4
                goto L_0x081f
            L_0x0813:
                int r4 = r2.leftSpanOffset
                if (r4 == 0) goto L_0x081f
                int r4 = r4 + 108
                r2.leftSpanOffset = r4
                goto L_0x081f
            L_0x081c:
                r7 = 2
            L_0x081d:
                r8 = 1000(0x3e8, float:1.401E-42)
            L_0x081f:
                int r5 = r5 + 1
                goto L_0x07a6
            L_0x0822:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: org.telegram.messenger.MessageObject.GroupedMessages.calculate():void");
        }

        public MessageObject findPrimaryMessageObject() {
            if (!this.messages.isEmpty() && this.positions.isEmpty()) {
                calculate();
            }
            for (int i = 0; i < this.messages.size(); i++) {
                MessageObject messageObject = this.messages.get(i);
                GroupedMessagePosition groupedMessagePosition = this.positions.get(messageObject);
                if (groupedMessagePosition != null && (groupedMessagePosition.flags & 5) != 0) {
                    return messageObject;
                }
            }
            return null;
        }

        public static class TransitionParams {
            public boolean backgroundChangeBounds;
            public int bottom;
            public float captionEnterProgress = 1.0f;
            public ChatMessageCell cell;
            public boolean drawBackgroundForDeletedItems;
            public boolean drawCaptionLayout;
            public boolean isNewGroup;
            public int left;
            public float offsetBottom;
            public float offsetLeft;
            public float offsetRight;
            public float offsetTop;
            public boolean pinnedBotton;
            public boolean pinnedTop;
            public int right;
            public int top;

            public void reset() {
                this.captionEnterProgress = 1.0f;
                this.offsetBottom = 0.0f;
                this.offsetTop = 0.0f;
                this.offsetRight = 0.0f;
                this.offsetLeft = 0.0f;
                this.backgroundChangeBounds = false;
            }
        }
    }

    public MessageObject(int i, TLRPC$Message tLRPC$Message, String str, String str2, String str3, boolean z, boolean z2, boolean z3, boolean z4) {
        this.type = 1000;
        this.forceSeekTo = -1.0f;
        this.localType = z ? 2 : 1;
        this.currentAccount = i;
        this.localName = str2;
        this.localUserName = str3;
        this.messageText = str;
        this.messageOwner = tLRPC$Message;
        this.localChannel = z2;
        this.localSupergroup = z3;
        this.localEdit = z4;
    }

    public MessageObject(int i, TLRPC$Message tLRPC$Message, AbstractMap<Integer, TLRPC$User> abstractMap, boolean z, boolean z2) {
        this(i, tLRPC$Message, abstractMap, (AbstractMap<Integer, TLRPC$Chat>) null, z, z2);
    }

    public MessageObject(int i, TLRPC$Message tLRPC$Message, SparseArray<TLRPC$User> sparseArray, boolean z, boolean z2) {
        this(i, tLRPC$Message, sparseArray, (SparseArray<TLRPC$Chat>) null, z, z2);
    }

    public MessageObject(int i, TLRPC$Message tLRPC$Message, boolean z, boolean z2) {
        this(i, tLRPC$Message, (MessageObject) null, (AbstractMap<Integer, TLRPC$User>) null, (AbstractMap<Integer, TLRPC$Chat>) null, (SparseArray<TLRPC$User>) null, (SparseArray<TLRPC$Chat>) null, z, z2, 0);
    }

    public MessageObject(int i, TLRPC$Message tLRPC$Message, MessageObject messageObject, boolean z, boolean z2) {
        this(i, tLRPC$Message, messageObject, (AbstractMap<Integer, TLRPC$User>) null, (AbstractMap<Integer, TLRPC$Chat>) null, (SparseArray<TLRPC$User>) null, (SparseArray<TLRPC$Chat>) null, z, z2, 0);
    }

    public MessageObject(int i, TLRPC$Message tLRPC$Message, AbstractMap<Integer, TLRPC$User> abstractMap, AbstractMap<Integer, TLRPC$Chat> abstractMap2, boolean z, boolean z2) {
        this(i, tLRPC$Message, abstractMap, abstractMap2, z, z2, 0);
    }

    public MessageObject(int i, TLRPC$Message tLRPC$Message, SparseArray<TLRPC$User> sparseArray, SparseArray<TLRPC$Chat> sparseArray2, boolean z, boolean z2) {
        this(i, tLRPC$Message, (MessageObject) null, (AbstractMap<Integer, TLRPC$User>) null, (AbstractMap<Integer, TLRPC$Chat>) null, sparseArray, sparseArray2, z, z2, 0);
    }

    public MessageObject(int i, TLRPC$Message tLRPC$Message, AbstractMap<Integer, TLRPC$User> abstractMap, AbstractMap<Integer, TLRPC$Chat> abstractMap2, boolean z, boolean z2, long j) {
        this(i, tLRPC$Message, (MessageObject) null, abstractMap, abstractMap2, (SparseArray<TLRPC$User>) null, (SparseArray<TLRPC$Chat>) null, z, z2, j);
    }

    public MessageObject(int i, TLRPC$Message tLRPC$Message, MessageObject messageObject, AbstractMap<Integer, TLRPC$User> abstractMap, AbstractMap<Integer, TLRPC$Chat> abstractMap2, SparseArray<TLRPC$User> sparseArray, SparseArray<TLRPC$Chat> sparseArray2, boolean z, boolean z2, long j) {
        TLRPC$User tLRPC$User;
        SparseArray<TLRPC$Chat> sparseArray3;
        AbstractMap<Integer, TLRPC$Chat> abstractMap3;
        TextPaint textPaint;
        int i2;
        TLRPC$Message tLRPC$Message2 = tLRPC$Message;
        AbstractMap<Integer, TLRPC$User> abstractMap4 = abstractMap;
        SparseArray<TLRPC$User> sparseArray4 = sparseArray;
        boolean z3 = z;
        this.type = 1000;
        this.forceSeekTo = -1.0f;
        Theme.createChatResources((Context) null, true);
        this.currentAccount = i;
        this.messageOwner = tLRPC$Message2;
        this.replyMessageObject = messageObject;
        this.eventId = j;
        this.wasUnread = !tLRPC$Message2.out && tLRPC$Message2.unread;
        TLRPC$Message tLRPC$Message3 = tLRPC$Message2.replyMessage;
        if (tLRPC$Message3 != null) {
            MessageObject messageObject2 = r2;
            MessageObject messageObject3 = new MessageObject(i, tLRPC$Message3, (MessageObject) null, abstractMap, abstractMap2, sparseArray, sparseArray2, false, z2, j);
            this.replyMessageObject = messageObject2;
        }
        TLRPC$Peer tLRPC$Peer = tLRPC$Message2.from_id;
        if (tLRPC$Peer instanceof TLRPC$TL_peerUser) {
            tLRPC$User = getUser(abstractMap4, sparseArray4, tLRPC$Peer.user_id);
            abstractMap3 = abstractMap2;
            sparseArray3 = sparseArray2;
        } else {
            abstractMap3 = abstractMap2;
            sparseArray3 = sparseArray2;
            tLRPC$User = null;
        }
        updateMessageText(abstractMap4, abstractMap3, sparseArray4, sparseArray3);
        setType();
        measureInlineBotButtons();
        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        gregorianCalendar.setTimeInMillis(((long) this.messageOwner.date) * 1000);
        int i3 = gregorianCalendar.get(6);
        int i4 = gregorianCalendar.get(1);
        int i5 = gregorianCalendar.get(2);
        this.dateKey = String.format("%d_%02d_%02d", new Object[]{Integer.valueOf(i4), Integer.valueOf(i5), Integer.valueOf(i3)});
        this.monthKey = String.format("%d_%02d", new Object[]{Integer.valueOf(i4), Integer.valueOf(i5)});
        createMessageSendInfo();
        generateCaption();
        boolean z4 = z;
        if (z4) {
            if (this.messageOwner.media instanceof TLRPC$TL_messageMediaGame) {
                textPaint = Theme.chat_msgGameTextPaint;
            } else {
                textPaint = Theme.chat_msgTextPaint;
            }
            int[] iArr = allowsBigEmoji() ? new int[1] : null;
            this.messageText = Emoji.replaceEmoji(this.messageText, textPaint.getFontMetricsInt(), AndroidUtilities.dp(20.0f), false, iArr);
            checkEmojiOnly(iArr);
            this.emojiAnimatedSticker = null;
            if (this.emojiOnlyCount == 1) {
                TLRPC$MessageMedia tLRPC$MessageMedia = tLRPC$Message2.media;
                if (!(tLRPC$MessageMedia instanceof TLRPC$TL_messageMediaWebPage) && !(tLRPC$MessageMedia instanceof TLRPC$TL_messageMediaInvoice) && tLRPC$Message2.entities.isEmpty()) {
                    CharSequence charSequence = this.messageText;
                    int indexOf = TextUtils.indexOf(charSequence, "🏻");
                    if (indexOf >= 0) {
                        this.emojiAnimatedStickerColor = "_c1";
                        charSequence = charSequence.subSequence(0, indexOf);
                    } else {
                        indexOf = TextUtils.indexOf(charSequence, "🏼");
                        if (indexOf >= 0) {
                            this.emojiAnimatedStickerColor = "_c2";
                            charSequence = charSequence.subSequence(0, indexOf);
                        } else {
                            indexOf = TextUtils.indexOf(charSequence, "🏽");
                            if (indexOf >= 0) {
                                this.emojiAnimatedStickerColor = "_c3";
                                charSequence = charSequence.subSequence(0, indexOf);
                            } else {
                                indexOf = TextUtils.indexOf(charSequence, "🏾");
                                if (indexOf >= 0) {
                                    this.emojiAnimatedStickerColor = "_c4";
                                    charSequence = charSequence.subSequence(0, indexOf);
                                } else {
                                    indexOf = TextUtils.indexOf(charSequence, "🏿");
                                    if (indexOf >= 0) {
                                        this.emojiAnimatedStickerColor = "_c5";
                                        charSequence = charSequence.subSequence(0, indexOf);
                                    } else {
                                        this.emojiAnimatedStickerColor = "";
                                    }
                                }
                            }
                        }
                    }
                    if (!TextUtils.isEmpty(this.emojiAnimatedStickerColor) && (i2 = indexOf + 2) < this.messageText.length()) {
                        StringBuilder sb = new StringBuilder();
                        sb.append(charSequence.toString());
                        CharSequence charSequence2 = this.messageText;
                        sb.append(charSequence2.subSequence(i2, charSequence2.length()).toString());
                        charSequence = sb.toString();
                    }
                    if (TextUtils.isEmpty(this.emojiAnimatedStickerColor) || EmojiData.emojiColoredMap.contains(charSequence.toString())) {
                        this.emojiAnimatedSticker = MediaDataController.getInstance(this.currentAccount).getEmojiAnimatedSticker(charSequence);
                    }
                }
            }
            if (this.emojiAnimatedSticker == null) {
                generateLayout(tLRPC$User);
            } else {
                this.type = 1000;
                if (isSticker()) {
                    this.type = 13;
                } else if (isAnimatedSticker()) {
                    this.type = 15;
                }
            }
            createPathThumb();
        }
        this.layoutCreated = z4;
        generateThumbs(false);
        if (z2) {
            checkMediaExistance();
        }
    }

    private void createPathThumb() {
        TLRPC$Document document = getDocument();
        if (document != null) {
            this.pathThumb = DocumentObject.getSvgThumb(document, "chat_serviceBackground", 1.0f);
        }
    }

    private void createDateArray(int i, TLRPC$TL_channelAdminLogEvent tLRPC$TL_channelAdminLogEvent, ArrayList<MessageObject> arrayList, HashMap<String, ArrayList<MessageObject>> hashMap) {
        if (hashMap.get(this.dateKey) == null) {
            hashMap.put(this.dateKey, new ArrayList());
            TLRPC$TL_message tLRPC$TL_message = new TLRPC$TL_message();
            tLRPC$TL_message.message = LocaleController.formatDateChat((long) tLRPC$TL_channelAdminLogEvent.date);
            tLRPC$TL_message.id = 0;
            tLRPC$TL_message.date = tLRPC$TL_channelAdminLogEvent.date;
            MessageObject messageObject = new MessageObject(i, tLRPC$TL_message, false, false);
            messageObject.type = 10;
            messageObject.contentType = 1;
            messageObject.isDateObject = true;
            arrayList.add(messageObject);
        }
    }

    private void checkEmojiOnly(int[] iArr) {
        TextPaint textPaint;
        int i;
        if (iArr != null) {
            if (iArr[0] >= 1 && iArr[0] <= 3) {
                int i2 = iArr[0];
                if (i2 == 1) {
                    textPaint = Theme.chat_msgTextPaintOneEmoji;
                    i = AndroidUtilities.dp(32.0f);
                    this.emojiOnlyCount = 1;
                } else if (i2 != 2) {
                    textPaint = Theme.chat_msgTextPaintThreeEmoji;
                    i = AndroidUtilities.dp(24.0f);
                    this.emojiOnlyCount = 3;
                } else {
                    textPaint = Theme.chat_msgTextPaintTwoEmoji;
                    int dp = AndroidUtilities.dp(28.0f);
                    this.emojiOnlyCount = 2;
                    i = dp;
                }
                CharSequence charSequence = this.messageText;
                Emoji.EmojiSpan[] emojiSpanArr = (Emoji.EmojiSpan[]) ((Spannable) charSequence).getSpans(0, charSequence.length(), Emoji.EmojiSpan.class);
                if (emojiSpanArr != null && emojiSpanArr.length > 0) {
                    for (Emoji.EmojiSpan replaceFontMetrics : emojiSpanArr) {
                        replaceFontMetrics.replaceFontMetrics(textPaint.getFontMetricsInt(), i);
                    }
                }
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:173:0x048f  */
    /* JADX WARNING: Removed duplicated region for block: B:178:0x04a3  */
    /* JADX WARNING: Removed duplicated region for block: B:181:0x04a9 A[LOOP:0: B:159:0x0466->B:181:0x04a9, LOOP_END] */
    /* JADX WARNING: Removed duplicated region for block: B:633:0x119a  */
    /* JADX WARNING: Removed duplicated region for block: B:636:0x11e8  */
    /* JADX WARNING: Removed duplicated region for block: B:638:0x11eb  */
    /* JADX WARNING: Removed duplicated region for block: B:645:0x124e  */
    /* JADX WARNING: Removed duplicated region for block: B:648:0x1257  */
    /* JADX WARNING: Removed duplicated region for block: B:664:0x04c5 A[EDGE_INSN: B:664:0x04c5->B:183:0x04c5 ?: BREAK  , SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:666:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public MessageObject(int r27, org.telegram.tgnet.TLRPC$TL_channelAdminLogEvent r28, java.util.ArrayList<org.telegram.messenger.MessageObject> r29, java.util.HashMap<java.lang.String, java.util.ArrayList<org.telegram.messenger.MessageObject>> r30, org.telegram.tgnet.TLRPC$Chat r31, int[] r32) {
        /*
            r26 = this;
            r0 = r26
            r1 = r28
            r2 = r29
            r3 = r30
            r4 = r31
            r26.<init>()
            r5 = 1000(0x3e8, float:1.401E-42)
            r0.type = r5
            r5 = -1082130432(0xffffffffbvar_, float:-1.0)
            r0.forceSeekTo = r5
            r0.currentEvent = r1
            r5 = r27
            r0.currentAccount = r5
            int r6 = r1.user_id
            if (r6 <= 0) goto L_0x002e
            org.telegram.messenger.MessagesController r5 = org.telegram.messenger.MessagesController.getInstance(r27)
            int r6 = r1.user_id
            java.lang.Integer r6 = java.lang.Integer.valueOf(r6)
            org.telegram.tgnet.TLRPC$User r5 = r5.getUser(r6)
            goto L_0x002f
        L_0x002e:
            r5 = 0
        L_0x002f:
            java.util.GregorianCalendar r6 = new java.util.GregorianCalendar
            r6.<init>()
            int r8 = r1.date
            long r8 = (long) r8
            r10 = 1000(0x3e8, double:4.94E-321)
            long r8 = r8 * r10
            r6.setTimeInMillis(r8)
            r8 = 6
            int r8 = r6.get(r8)
            r9 = 1
            int r10 = r6.get(r9)
            r11 = 2
            int r6 = r6.get(r11)
            r12 = 3
            java.lang.Object[] r13 = new java.lang.Object[r12]
            java.lang.Integer r14 = java.lang.Integer.valueOf(r10)
            r15 = 0
            r13[r15] = r14
            java.lang.Integer r14 = java.lang.Integer.valueOf(r6)
            r13[r9] = r14
            java.lang.Integer r8 = java.lang.Integer.valueOf(r8)
            r13[r11] = r8
            java.lang.String r8 = "%d_%02d_%02d"
            java.lang.String r8 = java.lang.String.format(r8, r13)
            r0.dateKey = r8
            java.lang.Object[] r8 = new java.lang.Object[r11]
            java.lang.Integer r10 = java.lang.Integer.valueOf(r10)
            r8[r15] = r10
            java.lang.Integer r6 = java.lang.Integer.valueOf(r6)
            r8[r9] = r6
            java.lang.String r6 = "%d_%02d"
            java.lang.String r6 = java.lang.String.format(r6, r8)
            r0.monthKey = r6
            org.telegram.tgnet.TLRPC$TL_peerChannel r6 = new org.telegram.tgnet.TLRPC$TL_peerChannel
            r6.<init>()
            int r8 = r4.id
            r6.channel_id = r8
            org.telegram.tgnet.TLRPC$ChannelAdminLogEventAction r8 = r1.action
            boolean r10 = r8 instanceof org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionChangeTitle
            java.lang.String r13 = ""
            java.lang.String r14 = "un1"
            if (r10 == 0) goto L_0x00c6
            org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionChangeTitle r8 = (org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionChangeTitle) r8
            java.lang.String r6 = r8.new_value
            boolean r8 = r4.megagroup
            if (r8 == 0) goto L_0x00b0
            r8 = 2131625338(0x7f0e057a, float:1.8877881E38)
            java.lang.Object[] r10 = new java.lang.Object[r9]
            r10[r15] = r6
            java.lang.String r6 = "EventLogEditedGroupTitle"
            java.lang.String r6 = org.telegram.messenger.LocaleController.formatString(r6, r8, r10)
            java.lang.CharSequence r6 = replaceWithLink(r6, r14, r5)
            r0.messageText = r6
            goto L_0x00c3
        L_0x00b0:
            r8 = 2131625334(0x7f0e0576, float:1.8877873E38)
            java.lang.Object[] r10 = new java.lang.Object[r9]
            r10[r15] = r6
            java.lang.String r6 = "EventLogEditedChannelTitle"
            java.lang.String r6 = org.telegram.messenger.LocaleController.formatString(r6, r8, r10)
            java.lang.CharSequence r6 = replaceWithLink(r6, r14, r5)
            r0.messageText = r6
        L_0x00c3:
            r7 = r13
            goto L_0x1195
        L_0x00c6:
            boolean r10 = r8 instanceof org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionChangePhoto
            if (r10 == 0) goto L_0x0165
            org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionChangePhoto r8 = (org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionChangePhoto) r8
            org.telegram.tgnet.TLRPC$TL_messageService r6 = new org.telegram.tgnet.TLRPC$TL_messageService
            r6.<init>()
            r0.messageOwner = r6
            org.telegram.tgnet.TLRPC$Photo r10 = r8.new_photo
            boolean r10 = r10 instanceof org.telegram.tgnet.TLRPC$TL_photoEmpty
            if (r10 == 0) goto L_0x0104
            org.telegram.tgnet.TLRPC$TL_messageActionChatDeletePhoto r8 = new org.telegram.tgnet.TLRPC$TL_messageActionChatDeletePhoto
            r8.<init>()
            r6.action = r8
            boolean r6 = r4.megagroup
            if (r6 == 0) goto L_0x00f4
            r6 = 2131625394(0x7f0e05b2, float:1.8877995E38)
            java.lang.String r8 = "EventLogRemovedWGroupPhoto"
            java.lang.String r6 = org.telegram.messenger.LocaleController.getString(r8, r6)
            java.lang.CharSequence r6 = replaceWithLink(r6, r14, r5)
            r0.messageText = r6
            goto L_0x00c3
        L_0x00f4:
            r6 = 2131625388(0x7f0e05ac, float:1.8877983E38)
            java.lang.String r8 = "EventLogRemovedChannelPhoto"
            java.lang.String r6 = org.telegram.messenger.LocaleController.getString(r8, r6)
            java.lang.CharSequence r6 = replaceWithLink(r6, r14, r5)
            r0.messageText = r6
            goto L_0x00c3
        L_0x0104:
            org.telegram.tgnet.TLRPC$TL_messageActionChatEditPhoto r10 = new org.telegram.tgnet.TLRPC$TL_messageActionChatEditPhoto
            r10.<init>()
            r6.action = r10
            org.telegram.tgnet.TLRPC$Message r6 = r0.messageOwner
            org.telegram.tgnet.TLRPC$MessageAction r6 = r6.action
            org.telegram.tgnet.TLRPC$Photo r8 = r8.new_photo
            r6.photo = r8
            boolean r6 = r4.megagroup
            if (r6 == 0) goto L_0x013d
            boolean r6 = r26.isVideoAvatar()
            if (r6 == 0) goto L_0x012d
            r6 = 2131625339(0x7f0e057b, float:1.8877883E38)
            java.lang.String r8 = "EventLogEditedGroupVideo"
            java.lang.String r6 = org.telegram.messenger.LocaleController.getString(r8, r6)
            java.lang.CharSequence r6 = replaceWithLink(r6, r14, r5)
            r0.messageText = r6
            goto L_0x00c3
        L_0x012d:
            r6 = 2131625337(0x7f0e0579, float:1.887788E38)
            java.lang.String r8 = "EventLogEditedGroupPhoto"
            java.lang.String r6 = org.telegram.messenger.LocaleController.getString(r8, r6)
            java.lang.CharSequence r6 = replaceWithLink(r6, r14, r5)
            r0.messageText = r6
            goto L_0x00c3
        L_0x013d:
            boolean r6 = r26.isVideoAvatar()
            if (r6 == 0) goto L_0x0154
            r6 = 2131625335(0x7f0e0577, float:1.8877875E38)
            java.lang.String r8 = "EventLogEditedChannelVideo"
            java.lang.String r6 = org.telegram.messenger.LocaleController.getString(r8, r6)
            java.lang.CharSequence r6 = replaceWithLink(r6, r14, r5)
            r0.messageText = r6
            goto L_0x00c3
        L_0x0154:
            r6 = 2131625333(0x7f0e0575, float:1.887787E38)
            java.lang.String r8 = "EventLogEditedChannelPhoto"
            java.lang.String r6 = org.telegram.messenger.LocaleController.getString(r8, r6)
            java.lang.CharSequence r6 = replaceWithLink(r6, r14, r5)
            r0.messageText = r6
            goto L_0x00c3
        L_0x0165:
            boolean r10 = r8 instanceof org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionParticipantJoin
            r7 = 2131625360(0x7f0e0590, float:1.8877926E38)
            java.lang.String r15 = "EventLogGroupJoined"
            r11 = 2131625326(0x7f0e056e, float:1.8877857E38)
            java.lang.String r9 = "EventLogChannelJoined"
            if (r10 == 0) goto L_0x018f
            boolean r6 = r4.megagroup
            if (r6 == 0) goto L_0x0183
            java.lang.String r6 = org.telegram.messenger.LocaleController.getString(r15, r7)
            java.lang.CharSequence r6 = replaceWithLink(r6, r14, r5)
            r0.messageText = r6
            goto L_0x00c3
        L_0x0183:
            java.lang.String r6 = org.telegram.messenger.LocaleController.getString(r9, r11)
            java.lang.CharSequence r6 = replaceWithLink(r6, r14, r5)
            r0.messageText = r6
            goto L_0x00c3
        L_0x018f:
            boolean r10 = r8 instanceof org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionParticipantLeave
            if (r10 == 0) goto L_0x01cf
            org.telegram.tgnet.TLRPC$TL_messageService r6 = new org.telegram.tgnet.TLRPC$TL_messageService
            r6.<init>()
            r0.messageOwner = r6
            org.telegram.tgnet.TLRPC$TL_messageActionChatDeleteUser r7 = new org.telegram.tgnet.TLRPC$TL_messageActionChatDeleteUser
            r7.<init>()
            r6.action = r7
            org.telegram.tgnet.TLRPC$Message r6 = r0.messageOwner
            org.telegram.tgnet.TLRPC$MessageAction r6 = r6.action
            int r7 = r1.user_id
            r6.user_id = r7
            boolean r6 = r4.megagroup
            if (r6 == 0) goto L_0x01be
            r6 = 2131625365(0x7f0e0595, float:1.8877936E38)
            java.lang.String r7 = "EventLogLeftGroup"
            java.lang.String r6 = org.telegram.messenger.LocaleController.getString(r7, r6)
            java.lang.CharSequence r6 = replaceWithLink(r6, r14, r5)
            r0.messageText = r6
            goto L_0x00c3
        L_0x01be:
            r6 = 2131625364(0x7f0e0594, float:1.8877934E38)
            java.lang.String r7 = "EventLogLeftChannel"
            java.lang.String r6 = org.telegram.messenger.LocaleController.getString(r7, r6)
            java.lang.CharSequence r6 = replaceWithLink(r6, r14, r5)
            r0.messageText = r6
            goto L_0x00c3
        L_0x01cf:
            boolean r10 = r8 instanceof org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionParticipantInvite
            java.lang.String r12 = "un2"
            if (r10 == 0) goto L_0x023d
            org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionParticipantInvite r8 = (org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionParticipantInvite) r8
            org.telegram.tgnet.TLRPC$TL_messageService r6 = new org.telegram.tgnet.TLRPC$TL_messageService
            r6.<init>()
            r0.messageOwner = r6
            org.telegram.tgnet.TLRPC$TL_messageActionChatAddUser r10 = new org.telegram.tgnet.TLRPC$TL_messageActionChatAddUser
            r10.<init>()
            r6.action = r10
            int r6 = r0.currentAccount
            org.telegram.messenger.MessagesController r6 = org.telegram.messenger.MessagesController.getInstance(r6)
            org.telegram.tgnet.TLRPC$ChannelParticipant r10 = r8.participant
            int r10 = r10.user_id
            java.lang.Integer r10 = java.lang.Integer.valueOf(r10)
            org.telegram.tgnet.TLRPC$User r6 = r6.getUser(r10)
            org.telegram.tgnet.TLRPC$Message r10 = r0.messageOwner
            org.telegram.tgnet.TLRPC$Peer r10 = r10.from_id
            boolean r11 = r10 instanceof org.telegram.tgnet.TLRPC$TL_peerUser
            if (r11 == 0) goto L_0x0226
            org.telegram.tgnet.TLRPC$ChannelParticipant r8 = r8.participant
            int r8 = r8.user_id
            int r10 = r10.user_id
            if (r8 != r10) goto L_0x0226
            boolean r6 = r4.megagroup
            if (r6 == 0) goto L_0x0217
            java.lang.String r6 = org.telegram.messenger.LocaleController.getString(r15, r7)
            java.lang.CharSequence r6 = replaceWithLink(r6, r14, r5)
            r0.messageText = r6
            goto L_0x00c3
        L_0x0217:
            r6 = 2131625326(0x7f0e056e, float:1.8877857E38)
            java.lang.String r6 = org.telegram.messenger.LocaleController.getString(r9, r6)
            java.lang.CharSequence r6 = replaceWithLink(r6, r14, r5)
            r0.messageText = r6
            goto L_0x00c3
        L_0x0226:
            r7 = 2131625316(0x7f0e0564, float:1.8877837E38)
            java.lang.String r8 = "EventLogAdded"
            java.lang.String r7 = org.telegram.messenger.LocaleController.getString(r8, r7)
            java.lang.CharSequence r6 = replaceWithLink(r7, r12, r6)
            r0.messageText = r6
            java.lang.CharSequence r6 = replaceWithLink(r6, r14, r5)
            r0.messageText = r6
            goto L_0x00c3
        L_0x023d:
            boolean r7 = r8 instanceof org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionParticipantToggleAdmin
            java.lang.String r9 = "%1$s"
            r15 = 32
            r10 = 10
            if (r7 != 0) goto L_0x0eee
            boolean r7 = r8 instanceof org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionParticipantToggleBan
            if (r7 == 0) goto L_0x025f
            r7 = r8
            org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionParticipantToggleBan r7 = (org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionParticipantToggleBan) r7
            org.telegram.tgnet.TLRPC$ChannelParticipant r7 = r7.prev_participant
            boolean r7 = r7 instanceof org.telegram.tgnet.TLRPC$TL_channelParticipantAdmin
            if (r7 == 0) goto L_0x025f
            r7 = r8
            org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionParticipantToggleBan r7 = (org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionParticipantToggleBan) r7
            org.telegram.tgnet.TLRPC$ChannelParticipant r7 = r7.new_participant
            boolean r7 = r7 instanceof org.telegram.tgnet.TLRPC$TL_channelParticipant
            if (r7 == 0) goto L_0x025f
            goto L_0x0eee
        L_0x025f:
            boolean r7 = r8 instanceof org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionDefaultBannedRights
            if (r7 == 0) goto L_0x03f5
            org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionDefaultBannedRights r8 = (org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionDefaultBannedRights) r8
            org.telegram.tgnet.TLRPC$TL_message r6 = new org.telegram.tgnet.TLRPC$TL_message
            r6.<init>()
            r0.messageOwner = r6
            org.telegram.tgnet.TLRPC$TL_chatBannedRights r6 = r8.prev_banned_rights
            org.telegram.tgnet.TLRPC$TL_chatBannedRights r7 = r8.new_banned_rights
            java.lang.StringBuilder r8 = new java.lang.StringBuilder
            r9 = 2131625329(0x7f0e0571, float:1.8877863E38)
            java.lang.String r12 = "EventLogDefaultPermissions"
            java.lang.String r9 = org.telegram.messenger.LocaleController.getString(r12, r9)
            r8.<init>(r9)
            if (r6 != 0) goto L_0x0285
            org.telegram.tgnet.TLRPC$TL_chatBannedRights r6 = new org.telegram.tgnet.TLRPC$TL_chatBannedRights
            r6.<init>()
        L_0x0285:
            if (r7 != 0) goto L_0x028c
            org.telegram.tgnet.TLRPC$TL_chatBannedRights r7 = new org.telegram.tgnet.TLRPC$TL_chatBannedRights
            r7.<init>()
        L_0x028c:
            boolean r9 = r6.send_messages
            boolean r12 = r7.send_messages
            if (r9 == r12) goto L_0x02b5
            r8.append(r10)
            r8.append(r10)
            boolean r9 = r7.send_messages
            if (r9 != 0) goto L_0x029f
            r9 = 43
            goto L_0x02a1
        L_0x029f:
            r9 = 45
        L_0x02a1:
            r8.append(r9)
            r8.append(r15)
            r9 = 2131625401(0x7f0e05b9, float:1.8878009E38)
            java.lang.String r12 = "EventLogRestrictedSendMessages"
            java.lang.String r9 = org.telegram.messenger.LocaleController.getString(r12, r9)
            r8.append(r9)
            r9 = 1
            goto L_0x02b6
        L_0x02b5:
            r9 = 0
        L_0x02b6:
            boolean r12 = r6.send_stickers
            boolean r14 = r7.send_stickers
            if (r12 != r14) goto L_0x02ce
            boolean r12 = r6.send_inline
            boolean r14 = r7.send_inline
            if (r12 != r14) goto L_0x02ce
            boolean r12 = r6.send_gifs
            boolean r14 = r7.send_gifs
            if (r12 != r14) goto L_0x02ce
            boolean r12 = r6.send_games
            boolean r14 = r7.send_games
            if (r12 == r14) goto L_0x02f2
        L_0x02ce:
            if (r9 != 0) goto L_0x02d4
            r8.append(r10)
            r9 = 1
        L_0x02d4:
            r8.append(r10)
            boolean r12 = r7.send_stickers
            if (r12 != 0) goto L_0x02de
            r12 = 43
            goto L_0x02e0
        L_0x02de:
            r12 = 45
        L_0x02e0:
            r8.append(r12)
            r8.append(r15)
            r12 = 2131625403(0x7f0e05bb, float:1.8878013E38)
            java.lang.String r14 = "EventLogRestrictedSendStickers"
            java.lang.String r12 = org.telegram.messenger.LocaleController.getString(r14, r12)
            r8.append(r12)
        L_0x02f2:
            boolean r12 = r6.send_media
            boolean r14 = r7.send_media
            if (r12 == r14) goto L_0x031c
            if (r9 != 0) goto L_0x02fe
            r8.append(r10)
            r9 = 1
        L_0x02fe:
            r8.append(r10)
            boolean r12 = r7.send_media
            if (r12 != 0) goto L_0x0308
            r12 = 43
            goto L_0x030a
        L_0x0308:
            r12 = 45
        L_0x030a:
            r8.append(r12)
            r8.append(r15)
            r12 = 2131625400(0x7f0e05b8, float:1.8878007E38)
            java.lang.String r14 = "EventLogRestrictedSendMedia"
            java.lang.String r12 = org.telegram.messenger.LocaleController.getString(r14, r12)
            r8.append(r12)
        L_0x031c:
            boolean r12 = r6.send_polls
            boolean r14 = r7.send_polls
            if (r12 == r14) goto L_0x0346
            if (r9 != 0) goto L_0x0328
            r8.append(r10)
            r9 = 1
        L_0x0328:
            r8.append(r10)
            boolean r12 = r7.send_polls
            if (r12 != 0) goto L_0x0332
            r12 = 43
            goto L_0x0334
        L_0x0332:
            r12 = 45
        L_0x0334:
            r8.append(r12)
            r8.append(r15)
            r12 = 2131625402(0x7f0e05ba, float:1.887801E38)
            java.lang.String r14 = "EventLogRestrictedSendPolls"
            java.lang.String r12 = org.telegram.messenger.LocaleController.getString(r14, r12)
            r8.append(r12)
        L_0x0346:
            boolean r12 = r6.embed_links
            boolean r14 = r7.embed_links
            if (r12 == r14) goto L_0x0370
            if (r9 != 0) goto L_0x0352
            r8.append(r10)
            r9 = 1
        L_0x0352:
            r8.append(r10)
            boolean r12 = r7.embed_links
            if (r12 != 0) goto L_0x035c
            r12 = 43
            goto L_0x035e
        L_0x035c:
            r12 = 45
        L_0x035e:
            r8.append(r12)
            r8.append(r15)
            r12 = 2131625399(0x7f0e05b7, float:1.8878005E38)
            java.lang.String r14 = "EventLogRestrictedSendEmbed"
            java.lang.String r12 = org.telegram.messenger.LocaleController.getString(r14, r12)
            r8.append(r12)
        L_0x0370:
            boolean r12 = r6.change_info
            boolean r14 = r7.change_info
            if (r12 == r14) goto L_0x039a
            if (r9 != 0) goto L_0x037c
            r8.append(r10)
            r9 = 1
        L_0x037c:
            r8.append(r10)
            boolean r12 = r7.change_info
            if (r12 != 0) goto L_0x0386
            r12 = 43
            goto L_0x0388
        L_0x0386:
            r12 = 45
        L_0x0388:
            r8.append(r12)
            r8.append(r15)
            r12 = 2131625395(0x7f0e05b3, float:1.8877997E38)
            java.lang.String r14 = "EventLogRestrictedChangeInfo"
            java.lang.String r12 = org.telegram.messenger.LocaleController.getString(r14, r12)
            r8.append(r12)
        L_0x039a:
            boolean r12 = r6.invite_users
            boolean r14 = r7.invite_users
            if (r12 == r14) goto L_0x03c4
            if (r9 != 0) goto L_0x03a6
            r8.append(r10)
            r9 = 1
        L_0x03a6:
            r8.append(r10)
            boolean r12 = r7.invite_users
            if (r12 != 0) goto L_0x03b0
            r12 = 43
            goto L_0x03b2
        L_0x03b0:
            r12 = 45
        L_0x03b2:
            r8.append(r12)
            r8.append(r15)
            r12 = 2131625396(0x7f0e05b4, float:1.8877999E38)
            java.lang.String r14 = "EventLogRestrictedInviteUsers"
            java.lang.String r12 = org.telegram.messenger.LocaleController.getString(r14, r12)
            r8.append(r12)
        L_0x03c4:
            boolean r6 = r6.pin_messages
            boolean r12 = r7.pin_messages
            if (r6 == r12) goto L_0x03ed
            if (r9 != 0) goto L_0x03cf
            r8.append(r10)
        L_0x03cf:
            r8.append(r10)
            boolean r6 = r7.pin_messages
            if (r6 != 0) goto L_0x03d9
            r10 = 43
            goto L_0x03db
        L_0x03d9:
            r10 = 45
        L_0x03db:
            r8.append(r10)
            r8.append(r15)
            r6 = 2131625397(0x7f0e05b5, float:1.8878E38)
            java.lang.String r7 = "EventLogRestrictedPinMessages"
            java.lang.String r6 = org.telegram.messenger.LocaleController.getString(r7, r6)
            r8.append(r6)
        L_0x03ed:
            java.lang.String r6 = r8.toString()
            r0.messageText = r6
            goto L_0x00c3
        L_0x03f5:
            boolean r7 = r8 instanceof org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionParticipantToggleBan
            java.lang.String r11 = "Hours"
            java.lang.String r15 = "Minutes"
            if (r7 == 0) goto L_0x06ef
            org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionParticipantToggleBan r8 = (org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionParticipantToggleBan) r8
            org.telegram.tgnet.TLRPC$TL_message r6 = new org.telegram.tgnet.TLRPC$TL_message
            r6.<init>()
            r0.messageOwner = r6
            int r6 = r0.currentAccount
            org.telegram.messenger.MessagesController r6 = org.telegram.messenger.MessagesController.getInstance(r6)
            org.telegram.tgnet.TLRPC$ChannelParticipant r7 = r8.prev_participant
            int r7 = r7.user_id
            java.lang.Integer r7 = java.lang.Integer.valueOf(r7)
            org.telegram.tgnet.TLRPC$User r6 = r6.getUser(r7)
            org.telegram.tgnet.TLRPC$ChannelParticipant r7 = r8.prev_participant
            org.telegram.tgnet.TLRPC$TL_chatBannedRights r7 = r7.banned_rights
            org.telegram.tgnet.TLRPC$ChannelParticipant r8 = r8.new_participant
            org.telegram.tgnet.TLRPC$TL_chatBannedRights r8 = r8.banned_rights
            boolean r12 = r4.megagroup
            if (r12 == 0) goto L_0x06b8
            if (r8 == 0) goto L_0x0432
            boolean r12 = r8.view_messages
            if (r12 == 0) goto L_0x0432
            if (r7 == 0) goto L_0x06b8
            int r12 = r8.until_date
            int r14 = r7.until_date
            if (r12 == r14) goto L_0x06b8
        L_0x0432:
            if (r8 == 0) goto L_0x04b5
            boolean r12 = org.telegram.messenger.AndroidUtilities.isBannedForever(r8)
            if (r12 != 0) goto L_0x04b5
            java.lang.StringBuilder r12 = new java.lang.StringBuilder
            r12.<init>()
            int r14 = r8.until_date
            int r10 = r1.date
            int r14 = r14 - r10
            int r10 = r14 / 60
            r21 = 60
            int r10 = r10 / 60
            int r10 = r10 / 24
            int r22 = r10 * 60
            int r22 = r22 * 60
            int r22 = r22 * 24
            int r14 = r14 - r22
            int r22 = r14 / 60
            int r2 = r22 / 60
            int r22 = r2 * 60
            int r22 = r22 * 60
            int r14 = r14 - r22
            int r14 = r14 / 60
            r22 = r13
            r3 = 0
            r13 = 3
            r16 = 0
        L_0x0466:
            if (r3 >= r13) goto L_0x04c5
            if (r3 != 0) goto L_0x0475
            if (r10 == 0) goto L_0x0486
            java.lang.String r13 = "Days"
            java.lang.String r13 = org.telegram.messenger.LocaleController.formatPluralString(r13, r10)
        L_0x0472:
            int r16 = r16 + 1
            goto L_0x0487
        L_0x0475:
            r13 = 1
            if (r3 != r13) goto L_0x047f
            if (r2 == 0) goto L_0x0486
            java.lang.String r13 = org.telegram.messenger.LocaleController.formatPluralString(r11, r2)
            goto L_0x0472
        L_0x047f:
            if (r14 == 0) goto L_0x0486
            java.lang.String r13 = org.telegram.messenger.LocaleController.formatPluralString(r15, r14)
            goto L_0x0472
        L_0x0486:
            r13 = 0
        L_0x0487:
            r25 = r16
            r16 = r2
            r2 = r25
            if (r13 == 0) goto L_0x04a3
            int r23 = r12.length()
            if (r23 <= 0) goto L_0x049d
            r23 = r10
            java.lang.String r10 = ", "
            r12.append(r10)
            goto L_0x049f
        L_0x049d:
            r23 = r10
        L_0x049f:
            r12.append(r13)
            goto L_0x04a5
        L_0x04a3:
            r23 = r10
        L_0x04a5:
            r10 = 2
            if (r2 != r10) goto L_0x04a9
            goto L_0x04c5
        L_0x04a9:
            int r3 = r3 + 1
            r10 = r23
            r13 = 3
            r25 = r16
            r16 = r2
            r2 = r25
            goto L_0x0466
        L_0x04b5:
            r22 = r13
            java.lang.StringBuilder r12 = new java.lang.StringBuilder
            r2 = 2131627814(0x7f0e0var_, float:1.8882903E38)
            java.lang.String r3 = "UserRestrictionsUntilForever"
            java.lang.String r2 = org.telegram.messenger.LocaleController.getString(r3, r2)
            r12.<init>(r2)
        L_0x04c5:
            r2 = 2131625404(0x7f0e05bc, float:1.8878015E38)
            java.lang.String r3 = "EventLogRestrictedUntil"
            java.lang.String r2 = org.telegram.messenger.LocaleController.getString(r3, r2)
            int r3 = r2.indexOf(r9)
            java.lang.StringBuilder r9 = new java.lang.StringBuilder
            r10 = 2
            java.lang.Object[] r10 = new java.lang.Object[r10]
            org.telegram.tgnet.TLRPC$Message r11 = r0.messageOwner
            java.util.ArrayList<org.telegram.tgnet.TLRPC$MessageEntity> r11 = r11.entities
            java.lang.String r3 = r0.getUserName(r6, r11, r3)
            r6 = 0
            r10[r6] = r3
            java.lang.String r3 = r12.toString()
            r6 = 1
            r10[r6] = r3
            java.lang.String r2 = java.lang.String.format(r2, r10)
            r9.<init>(r2)
            if (r7 != 0) goto L_0x04f7
            org.telegram.tgnet.TLRPC$TL_chatBannedRights r7 = new org.telegram.tgnet.TLRPC$TL_chatBannedRights
            r7.<init>()
        L_0x04f7:
            if (r8 != 0) goto L_0x04fe
            org.telegram.tgnet.TLRPC$TL_chatBannedRights r8 = new org.telegram.tgnet.TLRPC$TL_chatBannedRights
            r8.<init>()
        L_0x04fe:
            boolean r2 = r7.view_messages
            boolean r3 = r8.view_messages
            if (r2 == r3) goto L_0x052b
            r2 = 10
            r9.append(r2)
            r9.append(r2)
            boolean r2 = r8.view_messages
            if (r2 != 0) goto L_0x0513
            r2 = 43
            goto L_0x0515
        L_0x0513:
            r2 = 45
        L_0x0515:
            r9.append(r2)
            r2 = 32
            r9.append(r2)
            r2 = 2131625398(0x7f0e05b6, float:1.8878003E38)
            java.lang.String r3 = "EventLogRestrictedReadMessages"
            java.lang.String r2 = org.telegram.messenger.LocaleController.getString(r3, r2)
            r9.append(r2)
            r2 = 1
            goto L_0x052c
        L_0x052b:
            r2 = 0
        L_0x052c:
            boolean r3 = r7.send_messages
            boolean r6 = r8.send_messages
            if (r3 == r6) goto L_0x055a
            r3 = 10
            if (r2 != 0) goto L_0x053a
            r9.append(r3)
            r2 = 1
        L_0x053a:
            r9.append(r3)
            boolean r3 = r8.send_messages
            if (r3 != 0) goto L_0x0544
            r3 = 43
            goto L_0x0546
        L_0x0544:
            r3 = 45
        L_0x0546:
            r9.append(r3)
            r3 = 32
            r9.append(r3)
            r3 = 2131625401(0x7f0e05b9, float:1.8878009E38)
            java.lang.String r6 = "EventLogRestrictedSendMessages"
            java.lang.String r3 = org.telegram.messenger.LocaleController.getString(r6, r3)
            r9.append(r3)
        L_0x055a:
            boolean r3 = r7.send_stickers
            boolean r6 = r8.send_stickers
            if (r3 != r6) goto L_0x0572
            boolean r3 = r7.send_inline
            boolean r6 = r8.send_inline
            if (r3 != r6) goto L_0x0572
            boolean r3 = r7.send_gifs
            boolean r6 = r8.send_gifs
            if (r3 != r6) goto L_0x0572
            boolean r3 = r7.send_games
            boolean r6 = r8.send_games
            if (r3 == r6) goto L_0x059a
        L_0x0572:
            r3 = 10
            if (r2 != 0) goto L_0x057a
            r9.append(r3)
            r2 = 1
        L_0x057a:
            r9.append(r3)
            boolean r3 = r8.send_stickers
            if (r3 != 0) goto L_0x0584
            r3 = 43
            goto L_0x0586
        L_0x0584:
            r3 = 45
        L_0x0586:
            r9.append(r3)
            r3 = 32
            r9.append(r3)
            r3 = 2131625403(0x7f0e05bb, float:1.8878013E38)
            java.lang.String r6 = "EventLogRestrictedSendStickers"
            java.lang.String r3 = org.telegram.messenger.LocaleController.getString(r6, r3)
            r9.append(r3)
        L_0x059a:
            boolean r3 = r7.send_media
            boolean r6 = r8.send_media
            if (r3 == r6) goto L_0x05c8
            r3 = 10
            if (r2 != 0) goto L_0x05a8
            r9.append(r3)
            r2 = 1
        L_0x05a8:
            r9.append(r3)
            boolean r3 = r8.send_media
            if (r3 != 0) goto L_0x05b2
            r3 = 43
            goto L_0x05b4
        L_0x05b2:
            r3 = 45
        L_0x05b4:
            r9.append(r3)
            r3 = 32
            r9.append(r3)
            r3 = 2131625400(0x7f0e05b8, float:1.8878007E38)
            java.lang.String r6 = "EventLogRestrictedSendMedia"
            java.lang.String r3 = org.telegram.messenger.LocaleController.getString(r6, r3)
            r9.append(r3)
        L_0x05c8:
            boolean r3 = r7.send_polls
            boolean r6 = r8.send_polls
            if (r3 == r6) goto L_0x05f6
            r3 = 10
            if (r2 != 0) goto L_0x05d6
            r9.append(r3)
            r2 = 1
        L_0x05d6:
            r9.append(r3)
            boolean r3 = r8.send_polls
            if (r3 != 0) goto L_0x05e0
            r3 = 43
            goto L_0x05e2
        L_0x05e0:
            r3 = 45
        L_0x05e2:
            r9.append(r3)
            r3 = 32
            r9.append(r3)
            r3 = 2131625402(0x7f0e05ba, float:1.887801E38)
            java.lang.String r6 = "EventLogRestrictedSendPolls"
            java.lang.String r3 = org.telegram.messenger.LocaleController.getString(r6, r3)
            r9.append(r3)
        L_0x05f6:
            boolean r3 = r7.embed_links
            boolean r6 = r8.embed_links
            if (r3 == r6) goto L_0x0624
            r3 = 10
            if (r2 != 0) goto L_0x0604
            r9.append(r3)
            r2 = 1
        L_0x0604:
            r9.append(r3)
            boolean r3 = r8.embed_links
            if (r3 != 0) goto L_0x060e
            r3 = 43
            goto L_0x0610
        L_0x060e:
            r3 = 45
        L_0x0610:
            r9.append(r3)
            r3 = 32
            r9.append(r3)
            r3 = 2131625399(0x7f0e05b7, float:1.8878005E38)
            java.lang.String r6 = "EventLogRestrictedSendEmbed"
            java.lang.String r3 = org.telegram.messenger.LocaleController.getString(r6, r3)
            r9.append(r3)
        L_0x0624:
            boolean r3 = r7.change_info
            boolean r6 = r8.change_info
            if (r3 == r6) goto L_0x0652
            r3 = 10
            if (r2 != 0) goto L_0x0632
            r9.append(r3)
            r2 = 1
        L_0x0632:
            r9.append(r3)
            boolean r3 = r8.change_info
            if (r3 != 0) goto L_0x063c
            r3 = 43
            goto L_0x063e
        L_0x063c:
            r3 = 45
        L_0x063e:
            r9.append(r3)
            r3 = 32
            r9.append(r3)
            r3 = 2131625395(0x7f0e05b3, float:1.8877997E38)
            java.lang.String r6 = "EventLogRestrictedChangeInfo"
            java.lang.String r3 = org.telegram.messenger.LocaleController.getString(r6, r3)
            r9.append(r3)
        L_0x0652:
            boolean r3 = r7.invite_users
            boolean r6 = r8.invite_users
            if (r3 == r6) goto L_0x0680
            r3 = 10
            if (r2 != 0) goto L_0x0660
            r9.append(r3)
            r2 = 1
        L_0x0660:
            r9.append(r3)
            boolean r3 = r8.invite_users
            if (r3 != 0) goto L_0x066a
            r3 = 43
            goto L_0x066c
        L_0x066a:
            r3 = 45
        L_0x066c:
            r9.append(r3)
            r3 = 32
            r9.append(r3)
            r3 = 2131625396(0x7f0e05b4, float:1.8877999E38)
            java.lang.String r6 = "EventLogRestrictedInviteUsers"
            java.lang.String r3 = org.telegram.messenger.LocaleController.getString(r6, r3)
            r9.append(r3)
        L_0x0680:
            boolean r3 = r7.pin_messages
            boolean r6 = r8.pin_messages
            if (r3 == r6) goto L_0x06b0
            if (r2 != 0) goto L_0x068e
            r2 = 10
            r9.append(r2)
            goto L_0x0690
        L_0x068e:
            r2 = 10
        L_0x0690:
            r9.append(r2)
            boolean r2 = r8.pin_messages
            if (r2 != 0) goto L_0x069a
            r10 = 43
            goto L_0x069c
        L_0x069a:
            r10 = 45
        L_0x069c:
            r9.append(r10)
            r2 = 32
            r9.append(r2)
            r2 = 2131625397(0x7f0e05b5, float:1.8878E38)
            java.lang.String r3 = "EventLogRestrictedPinMessages"
            java.lang.String r2 = org.telegram.messenger.LocaleController.getString(r3, r2)
            r9.append(r2)
        L_0x06b0:
            java.lang.String r2 = r9.toString()
            r0.messageText = r2
            goto L_0x07d6
        L_0x06b8:
            r22 = r13
            if (r8 == 0) goto L_0x06cc
            if (r7 == 0) goto L_0x06c2
            boolean r2 = r8.view_messages
            if (r2 == 0) goto L_0x06cc
        L_0x06c2:
            r2 = 2131625327(0x7f0e056f, float:1.8877859E38)
            java.lang.String r3 = "EventLogChannelRestricted"
            java.lang.String r2 = org.telegram.messenger.LocaleController.getString(r3, r2)
            goto L_0x06d5
        L_0x06cc:
            r2 = 2131625328(0x7f0e0570, float:1.887786E38)
            java.lang.String r3 = "EventLogChannelUnrestricted"
            java.lang.String r2 = org.telegram.messenger.LocaleController.getString(r3, r2)
        L_0x06d5:
            int r3 = r2.indexOf(r9)
            r7 = 1
            java.lang.Object[] r8 = new java.lang.Object[r7]
            org.telegram.tgnet.TLRPC$Message r7 = r0.messageOwner
            java.util.ArrayList<org.telegram.tgnet.TLRPC$MessageEntity> r7 = r7.entities
            java.lang.String r3 = r0.getUserName(r6, r7, r3)
            r6 = 0
            r8[r6] = r3
            java.lang.String r2 = java.lang.String.format(r2, r8)
            r0.messageText = r2
            goto L_0x07d6
        L_0x06ef:
            r22 = r13
            boolean r2 = r8 instanceof org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionUpdatePinned
            if (r2 == 0) goto L_0x0774
            org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionUpdatePinned r8 = (org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionUpdatePinned) r8
            org.telegram.tgnet.TLRPC$Message r2 = r8.message
            java.lang.String r3 = "EventLogUnpinnedMessages"
            if (r5 == 0) goto L_0x074d
            int r6 = r5.id
            r7 = 136817688(0x827aCLASSNAME, float:5.045703E-34)
            if (r6 != r7) goto L_0x074d
            org.telegram.tgnet.TLRPC$MessageFwdHeader r6 = r2.fwd_from
            if (r6 == 0) goto L_0x074d
            org.telegram.tgnet.TLRPC$Peer r6 = r6.from_id
            boolean r6 = r6 instanceof org.telegram.tgnet.TLRPC$TL_peerChannel
            if (r6 == 0) goto L_0x074d
            int r6 = r0.currentAccount
            org.telegram.messenger.MessagesController r6 = org.telegram.messenger.MessagesController.getInstance(r6)
            org.telegram.tgnet.TLRPC$Message r7 = r8.message
            org.telegram.tgnet.TLRPC$MessageFwdHeader r7 = r7.fwd_from
            org.telegram.tgnet.TLRPC$Peer r7 = r7.from_id
            int r7 = r7.channel_id
            java.lang.Integer r7 = java.lang.Integer.valueOf(r7)
            org.telegram.tgnet.TLRPC$Chat r6 = r6.getChat(r7)
            org.telegram.tgnet.TLRPC$Message r7 = r8.message
            boolean r8 = r7 instanceof org.telegram.tgnet.TLRPC$TL_messageEmpty
            if (r8 != 0) goto L_0x073f
            boolean r7 = r7.pinned
            if (r7 != 0) goto L_0x072f
            goto L_0x073f
        L_0x072f:
            r3 = 2131625369(0x7f0e0599, float:1.8877944E38)
            java.lang.String r7 = "EventLogPinnedMessages"
            java.lang.String r3 = org.telegram.messenger.LocaleController.getString(r7, r3)
            java.lang.CharSequence r3 = replaceWithLink(r3, r14, r6)
            r0.messageText = r3
            goto L_0x07a9
        L_0x073f:
            r7 = 2131625417(0x7f0e05c9, float:1.8878041E38)
            java.lang.String r3 = org.telegram.messenger.LocaleController.getString(r3, r7)
            java.lang.CharSequence r3 = replaceWithLink(r3, r14, r6)
            r0.messageText = r3
            goto L_0x07a9
        L_0x074d:
            boolean r6 = r2 instanceof org.telegram.tgnet.TLRPC$TL_messageEmpty
            if (r6 != 0) goto L_0x0766
            boolean r6 = r2.pinned
            if (r6 != 0) goto L_0x0756
            goto L_0x0766
        L_0x0756:
            r3 = 2131625369(0x7f0e0599, float:1.8877944E38)
            java.lang.String r6 = "EventLogPinnedMessages"
            java.lang.String r3 = org.telegram.messenger.LocaleController.getString(r6, r3)
            java.lang.CharSequence r3 = replaceWithLink(r3, r14, r5)
            r0.messageText = r3
            goto L_0x07a9
        L_0x0766:
            r6 = 2131625417(0x7f0e05c9, float:1.8878041E38)
            java.lang.String r3 = org.telegram.messenger.LocaleController.getString(r3, r6)
            java.lang.CharSequence r3 = replaceWithLink(r3, r14, r5)
            r0.messageText = r3
            goto L_0x07a9
        L_0x0774:
            boolean r2 = r8 instanceof org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionStopPoll
            if (r2 == 0) goto L_0x07ad
            org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionStopPoll r8 = (org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionStopPoll) r8
            org.telegram.tgnet.TLRPC$Message r2 = r8.message
            org.telegram.tgnet.TLRPC$MessageMedia r3 = r2.media
            boolean r6 = r3 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaPoll
            if (r6 == 0) goto L_0x079a
            org.telegram.tgnet.TLRPC$TL_messageMediaPoll r3 = (org.telegram.tgnet.TLRPC$TL_messageMediaPoll) r3
            org.telegram.tgnet.TLRPC$Poll r3 = r3.poll
            boolean r3 = r3.quiz
            if (r3 == 0) goto L_0x079a
            r3 = 2131625408(0x7f0e05c0, float:1.8878023E38)
            java.lang.String r6 = "EventLogStopQuiz"
            java.lang.String r3 = org.telegram.messenger.LocaleController.getString(r6, r3)
            java.lang.CharSequence r3 = replaceWithLink(r3, r14, r5)
            r0.messageText = r3
            goto L_0x07a9
        L_0x079a:
            r3 = 2131625407(0x7f0e05bf, float:1.8878021E38)
            java.lang.String r6 = "EventLogStopPoll"
            java.lang.String r3 = org.telegram.messenger.LocaleController.getString(r6, r3)
            java.lang.CharSequence r3 = replaceWithLink(r3, r14, r5)
            r0.messageText = r3
        L_0x07a9:
            r7 = r22
            goto L_0x1196
        L_0x07ad:
            boolean r2 = r8 instanceof org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionToggleSignatures
            if (r2 == 0) goto L_0x07da
            org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionToggleSignatures r8 = (org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionToggleSignatures) r8
            boolean r2 = r8.new_value
            if (r2 == 0) goto L_0x07c7
            r2 = 2131625414(0x7f0e05c6, float:1.8878035E38)
            java.lang.String r3 = "EventLogToggledSignaturesOn"
            java.lang.String r2 = org.telegram.messenger.LocaleController.getString(r3, r2)
            java.lang.CharSequence r2 = replaceWithLink(r2, r14, r5)
            r0.messageText = r2
            goto L_0x07d6
        L_0x07c7:
            r2 = 2131625413(0x7f0e05c5, float:1.8878033E38)
            java.lang.String r3 = "EventLogToggledSignaturesOff"
            java.lang.String r2 = org.telegram.messenger.LocaleController.getString(r3, r2)
            java.lang.CharSequence r2 = replaceWithLink(r2, r14, r5)
            r0.messageText = r2
        L_0x07d6:
            r7 = r22
            goto L_0x1195
        L_0x07da:
            boolean r2 = r8 instanceof org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionToggleInvites
            if (r2 == 0) goto L_0x0804
            org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionToggleInvites r8 = (org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionToggleInvites) r8
            boolean r2 = r8.new_value
            if (r2 == 0) goto L_0x07f4
            r2 = 2131625412(0x7f0e05c4, float:1.8878031E38)
            java.lang.String r3 = "EventLogToggledInvitesOn"
            java.lang.String r2 = org.telegram.messenger.LocaleController.getString(r3, r2)
            java.lang.CharSequence r2 = replaceWithLink(r2, r14, r5)
            r0.messageText = r2
            goto L_0x07d6
        L_0x07f4:
            r2 = 2131625411(0x7f0e05c3, float:1.887803E38)
            java.lang.String r3 = "EventLogToggledInvitesOff"
            java.lang.String r2 = org.telegram.messenger.LocaleController.getString(r3, r2)
            java.lang.CharSequence r2 = replaceWithLink(r2, r14, r5)
            r0.messageText = r2
            goto L_0x07d6
        L_0x0804:
            boolean r2 = r8 instanceof org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionDeleteMessage
            if (r2 == 0) goto L_0x081c
            org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionDeleteMessage r8 = (org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionDeleteMessage) r8
            org.telegram.tgnet.TLRPC$Message r2 = r8.message
            r3 = 2131625330(0x7f0e0572, float:1.8877865E38)
            java.lang.String r6 = "EventLogDeletedMessages"
            java.lang.String r3 = org.telegram.messenger.LocaleController.getString(r6, r3)
            java.lang.CharSequence r3 = replaceWithLink(r3, r14, r5)
            r0.messageText = r3
            goto L_0x07a9
        L_0x081c:
            boolean r2 = r8 instanceof org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionChangeLinkedChat
            if (r2 == 0) goto L_0x08c4
            r2 = r8
            org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionChangeLinkedChat r2 = (org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionChangeLinkedChat) r2
            int r2 = r2.new_value
            org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionChangeLinkedChat r8 = (org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionChangeLinkedChat) r8
            int r3 = r8.prev_value
            boolean r6 = r4.megagroup
            if (r6 == 0) goto L_0x0878
            if (r2 != 0) goto L_0x0853
            int r2 = r0.currentAccount
            org.telegram.messenger.MessagesController r2 = org.telegram.messenger.MessagesController.getInstance(r2)
            java.lang.Integer r3 = java.lang.Integer.valueOf(r3)
            org.telegram.tgnet.TLRPC$Chat r2 = r2.getChat(r3)
            r3 = 2131625390(0x7f0e05ae, float:1.8877987E38)
            java.lang.String r6 = "EventLogRemovedLinkedChannel"
            java.lang.String r3 = org.telegram.messenger.LocaleController.getString(r6, r3)
            java.lang.CharSequence r3 = replaceWithLink(r3, r14, r5)
            r0.messageText = r3
            java.lang.CharSequence r2 = replaceWithLink(r3, r12, r2)
            r0.messageText = r2
            goto L_0x07d6
        L_0x0853:
            int r3 = r0.currentAccount
            org.telegram.messenger.MessagesController r3 = org.telegram.messenger.MessagesController.getInstance(r3)
            java.lang.Integer r2 = java.lang.Integer.valueOf(r2)
            org.telegram.tgnet.TLRPC$Chat r2 = r3.getChat(r2)
            r3 = 2131625321(0x7f0e0569, float:1.8877847E38)
            java.lang.String r6 = "EventLogChangedLinkedChannel"
            java.lang.String r3 = org.telegram.messenger.LocaleController.getString(r6, r3)
            java.lang.CharSequence r3 = replaceWithLink(r3, r14, r5)
            r0.messageText = r3
            java.lang.CharSequence r2 = replaceWithLink(r3, r12, r2)
            r0.messageText = r2
            goto L_0x07d6
        L_0x0878:
            if (r2 != 0) goto L_0x089f
            int r2 = r0.currentAccount
            org.telegram.messenger.MessagesController r2 = org.telegram.messenger.MessagesController.getInstance(r2)
            java.lang.Integer r3 = java.lang.Integer.valueOf(r3)
            org.telegram.tgnet.TLRPC$Chat r2 = r2.getChat(r3)
            r3 = 2131625391(0x7f0e05af, float:1.8877989E38)
            java.lang.String r6 = "EventLogRemovedLinkedGroup"
            java.lang.String r3 = org.telegram.messenger.LocaleController.getString(r6, r3)
            java.lang.CharSequence r3 = replaceWithLink(r3, r14, r5)
            r0.messageText = r3
            java.lang.CharSequence r2 = replaceWithLink(r3, r12, r2)
            r0.messageText = r2
            goto L_0x07d6
        L_0x089f:
            int r3 = r0.currentAccount
            org.telegram.messenger.MessagesController r3 = org.telegram.messenger.MessagesController.getInstance(r3)
            java.lang.Integer r2 = java.lang.Integer.valueOf(r2)
            org.telegram.tgnet.TLRPC$Chat r2 = r3.getChat(r2)
            r3 = 2131625322(0x7f0e056a, float:1.8877849E38)
            java.lang.String r6 = "EventLogChangedLinkedGroup"
            java.lang.String r3 = org.telegram.messenger.LocaleController.getString(r6, r3)
            java.lang.CharSequence r3 = replaceWithLink(r3, r14, r5)
            r0.messageText = r3
            java.lang.CharSequence r2 = replaceWithLink(r3, r12, r2)
            r0.messageText = r2
            goto L_0x07d6
        L_0x08c4:
            boolean r2 = r8 instanceof org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionTogglePreHistoryHidden
            if (r2 == 0) goto L_0x08f0
            org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionTogglePreHistoryHidden r8 = (org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionTogglePreHistoryHidden) r8
            boolean r2 = r8.new_value
            if (r2 == 0) goto L_0x08df
            r2 = 2131625409(0x7f0e05c1, float:1.8878025E38)
            java.lang.String r3 = "EventLogToggledInvitesHistoryOff"
            java.lang.String r2 = org.telegram.messenger.LocaleController.getString(r3, r2)
            java.lang.CharSequence r2 = replaceWithLink(r2, r14, r5)
            r0.messageText = r2
            goto L_0x07d6
        L_0x08df:
            r2 = 2131625410(0x7f0e05c2, float:1.8878027E38)
            java.lang.String r3 = "EventLogToggledInvitesHistoryOn"
            java.lang.String r2 = org.telegram.messenger.LocaleController.getString(r3, r2)
            java.lang.CharSequence r2 = replaceWithLink(r2, r14, r5)
            r0.messageText = r2
            goto L_0x07d6
        L_0x08f0:
            boolean r2 = r8 instanceof org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionChangeAbout
            if (r2 == 0) goto L_0x097b
            boolean r2 = r4.megagroup
            if (r2 == 0) goto L_0x08fe
            r2 = 2131625336(0x7f0e0578, float:1.8877877E38)
            java.lang.String r3 = "EventLogEditedGroupDescription"
            goto L_0x0903
        L_0x08fe:
            r2 = 2131625332(0x7f0e0574, float:1.8877869E38)
            java.lang.String r3 = "EventLogEditedChannelDescription"
        L_0x0903:
            java.lang.String r2 = org.telegram.messenger.LocaleController.getString(r3, r2)
            java.lang.CharSequence r2 = replaceWithLink(r2, r14, r5)
            r0.messageText = r2
            org.telegram.tgnet.TLRPC$TL_message r2 = new org.telegram.tgnet.TLRPC$TL_message
            r2.<init>()
            r3 = 0
            r2.out = r3
            r2.unread = r3
            org.telegram.tgnet.TLRPC$TL_peerUser r3 = new org.telegram.tgnet.TLRPC$TL_peerUser
            r3.<init>()
            r2.from_id = r3
            int r7 = r1.user_id
            r3.user_id = r7
            r2.peer_id = r6
            int r3 = r1.date
            r2.date = r3
            org.telegram.tgnet.TLRPC$ChannelAdminLogEventAction r3 = r1.action
            r6 = r3
            org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionChangeAbout r6 = (org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionChangeAbout) r6
            java.lang.String r6 = r6.new_value
            r2.message = r6
            org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionChangeAbout r3 = (org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionChangeAbout) r3
            java.lang.String r3 = r3.prev_value
            boolean r3 = android.text.TextUtils.isEmpty(r3)
            if (r3 != 0) goto L_0x0970
            org.telegram.tgnet.TLRPC$TL_messageMediaWebPage r3 = new org.telegram.tgnet.TLRPC$TL_messageMediaWebPage
            r3.<init>()
            r2.media = r3
            org.telegram.tgnet.TLRPC$TL_webPage r6 = new org.telegram.tgnet.TLRPC$TL_webPage
            r6.<init>()
            r3.webpage = r6
            org.telegram.tgnet.TLRPC$MessageMedia r3 = r2.media
            org.telegram.tgnet.TLRPC$WebPage r3 = r3.webpage
            r6 = 10
            r3.flags = r6
            r7 = r22
            r3.display_url = r7
            r3.url = r7
            r6 = 2131625370(0x7f0e059a, float:1.8877946E38)
            java.lang.String r8 = "EventLogPreviousGroupDescription"
            java.lang.String r6 = org.telegram.messenger.LocaleController.getString(r8, r6)
            r3.site_name = r6
            org.telegram.tgnet.TLRPC$MessageMedia r3 = r2.media
            org.telegram.tgnet.TLRPC$WebPage r3 = r3.webpage
            org.telegram.tgnet.TLRPC$ChannelAdminLogEventAction r6 = r1.action
            org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionChangeAbout r6 = (org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionChangeAbout) r6
            java.lang.String r6 = r6.prev_value
            r3.description = r6
            goto L_0x1196
        L_0x0970:
            r7 = r22
            org.telegram.tgnet.TLRPC$TL_messageMediaEmpty r3 = new org.telegram.tgnet.TLRPC$TL_messageMediaEmpty
            r3.<init>()
            r2.media = r3
            goto L_0x1196
        L_0x097b:
            r7 = r22
            boolean r2 = r8 instanceof org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionChangeUsername
            if (r2 == 0) goto L_0x0a83
            org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionChangeUsername r8 = (org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionChangeUsername) r8
            java.lang.String r2 = r8.new_value
            boolean r3 = android.text.TextUtils.isEmpty(r2)
            if (r3 != 0) goto L_0x09a5
            boolean r3 = r4.megagroup
            if (r3 == 0) goto L_0x0995
            r3 = 2131625320(0x7f0e0568, float:1.8877845E38)
            java.lang.String r8 = "EventLogChangedGroupLink"
            goto L_0x099a
        L_0x0995:
            r3 = 2131625319(0x7f0e0567, float:1.8877843E38)
            java.lang.String r8 = "EventLogChangedChannelLink"
        L_0x099a:
            java.lang.String r3 = org.telegram.messenger.LocaleController.getString(r8, r3)
            java.lang.CharSequence r3 = replaceWithLink(r3, r14, r5)
            r0.messageText = r3
            goto L_0x09be
        L_0x09a5:
            boolean r3 = r4.megagroup
            if (r3 == 0) goto L_0x09af
            r3 = 2131625389(0x7f0e05ad, float:1.8877985E38)
            java.lang.String r8 = "EventLogRemovedGroupLink"
            goto L_0x09b4
        L_0x09af:
            r3 = 2131625387(0x7f0e05ab, float:1.887798E38)
            java.lang.String r8 = "EventLogRemovedChannelLink"
        L_0x09b4:
            java.lang.String r3 = org.telegram.messenger.LocaleController.getString(r8, r3)
            java.lang.CharSequence r3 = replaceWithLink(r3, r14, r5)
            r0.messageText = r3
        L_0x09be:
            org.telegram.tgnet.TLRPC$TL_message r3 = new org.telegram.tgnet.TLRPC$TL_message
            r3.<init>()
            r8 = 0
            r3.out = r8
            r3.unread = r8
            org.telegram.tgnet.TLRPC$TL_peerUser r8 = new org.telegram.tgnet.TLRPC$TL_peerUser
            r8.<init>()
            r3.from_id = r8
            int r9 = r1.user_id
            r8.user_id = r9
            r3.peer_id = r6
            int r6 = r1.date
            r3.date = r6
            boolean r6 = android.text.TextUtils.isEmpty(r2)
            if (r6 != 0) goto L_0x0a03
            java.lang.StringBuilder r6 = new java.lang.StringBuilder
            r6.<init>()
            java.lang.String r8 = "https://"
            r6.append(r8)
            int r8 = r0.currentAccount
            org.telegram.messenger.MessagesController r8 = org.telegram.messenger.MessagesController.getInstance(r8)
            java.lang.String r8 = r8.linkPrefix
            r6.append(r8)
            java.lang.String r8 = "/"
            r6.append(r8)
            r6.append(r2)
            java.lang.String r2 = r6.toString()
            r3.message = r2
            goto L_0x0a05
        L_0x0a03:
            r3.message = r7
        L_0x0a05:
            org.telegram.tgnet.TLRPC$TL_messageEntityUrl r2 = new org.telegram.tgnet.TLRPC$TL_messageEntityUrl
            r2.<init>()
            r6 = 0
            r2.offset = r6
            java.lang.String r6 = r3.message
            int r6 = r6.length()
            r2.length = r6
            java.util.ArrayList<org.telegram.tgnet.TLRPC$MessageEntity> r6 = r3.entities
            r6.add(r2)
            org.telegram.tgnet.TLRPC$ChannelAdminLogEventAction r2 = r1.action
            org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionChangeUsername r2 = (org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionChangeUsername) r2
            java.lang.String r2 = r2.prev_value
            boolean r2 = android.text.TextUtils.isEmpty(r2)
            if (r2 != 0) goto L_0x0a79
            org.telegram.tgnet.TLRPC$TL_messageMediaWebPage r2 = new org.telegram.tgnet.TLRPC$TL_messageMediaWebPage
            r2.<init>()
            r3.media = r2
            org.telegram.tgnet.TLRPC$TL_webPage r6 = new org.telegram.tgnet.TLRPC$TL_webPage
            r6.<init>()
            r2.webpage = r6
            org.telegram.tgnet.TLRPC$MessageMedia r2 = r3.media
            org.telegram.tgnet.TLRPC$WebPage r2 = r2.webpage
            r6 = 10
            r2.flags = r6
            r2.display_url = r7
            r2.url = r7
            r6 = 2131625371(0x7f0e059b, float:1.8877948E38)
            java.lang.String r8 = "EventLogPreviousLink"
            java.lang.String r6 = org.telegram.messenger.LocaleController.getString(r8, r6)
            r2.site_name = r6
            org.telegram.tgnet.TLRPC$MessageMedia r2 = r3.media
            org.telegram.tgnet.TLRPC$WebPage r2 = r2.webpage
            java.lang.StringBuilder r6 = new java.lang.StringBuilder
            r6.<init>()
            java.lang.String r8 = "https://"
            r6.append(r8)
            int r8 = r0.currentAccount
            org.telegram.messenger.MessagesController r8 = org.telegram.messenger.MessagesController.getInstance(r8)
            java.lang.String r8 = r8.linkPrefix
            r6.append(r8)
            java.lang.String r8 = "/"
            r6.append(r8)
            org.telegram.tgnet.TLRPC$ChannelAdminLogEventAction r8 = r1.action
            org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionChangeUsername r8 = (org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionChangeUsername) r8
            java.lang.String r8 = r8.prev_value
            r6.append(r8)
            java.lang.String r6 = r6.toString()
            r2.description = r6
            goto L_0x0a80
        L_0x0a79:
            org.telegram.tgnet.TLRPC$TL_messageMediaEmpty r2 = new org.telegram.tgnet.TLRPC$TL_messageMediaEmpty
            r2.<init>()
            r3.media = r2
        L_0x0a80:
            r2 = r3
            goto L_0x1196
        L_0x0a83:
            boolean r2 = r8 instanceof org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionEditMessage
            if (r2 == 0) goto L_0x0be0
            org.telegram.tgnet.TLRPC$TL_message r2 = new org.telegram.tgnet.TLRPC$TL_message
            r2.<init>()
            r3 = 0
            r2.out = r3
            r2.unread = r3
            org.telegram.tgnet.TLRPC$TL_peerUser r3 = new org.telegram.tgnet.TLRPC$TL_peerUser
            r3.<init>()
            r2.from_id = r3
            int r8 = r1.user_id
            r3.user_id = r8
            r2.peer_id = r6
            int r3 = r1.date
            r2.date = r3
            org.telegram.tgnet.TLRPC$ChannelAdminLogEventAction r3 = r1.action
            r6 = r3
            org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionEditMessage r6 = (org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionEditMessage) r6
            org.telegram.tgnet.TLRPC$Message r6 = r6.new_message
            org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionEditMessage r3 = (org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionEditMessage) r3
            org.telegram.tgnet.TLRPC$Message r3 = r3.prev_message
            org.telegram.tgnet.TLRPC$MessageMedia r8 = r6.media
            if (r8 == 0) goto L_0x0b6d
            boolean r9 = r8 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaEmpty
            if (r9 != 0) goto L_0x0b6d
            boolean r8 = r8 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaWebPage
            if (r8 != 0) goto L_0x0b6d
            java.lang.String r8 = r6.message
            java.lang.String r9 = r3.message
            boolean r8 = android.text.TextUtils.equals(r8, r9)
            r9 = 1
            r8 = r8 ^ r9
            org.telegram.tgnet.TLRPC$MessageMedia r9 = r6.media
            java.lang.Class r9 = r9.getClass()
            org.telegram.tgnet.TLRPC$MessageMedia r10 = r3.media
            java.lang.Class r10 = r10.getClass()
            if (r9 != r10) goto L_0x0afa
            org.telegram.tgnet.TLRPC$MessageMedia r9 = r6.media
            org.telegram.tgnet.TLRPC$Photo r10 = r9.photo
            if (r10 == 0) goto L_0x0ae5
            org.telegram.tgnet.TLRPC$MessageMedia r11 = r3.media
            org.telegram.tgnet.TLRPC$Photo r11 = r11.photo
            if (r11 == 0) goto L_0x0ae5
            long r12 = r10.id
            long r10 = r11.id
            int r15 = (r12 > r10 ? 1 : (r12 == r10 ? 0 : -1))
            if (r15 != 0) goto L_0x0afa
        L_0x0ae5:
            org.telegram.tgnet.TLRPC$Document r9 = r9.document
            if (r9 == 0) goto L_0x0af8
            org.telegram.tgnet.TLRPC$MessageMedia r10 = r3.media
            org.telegram.tgnet.TLRPC$Document r10 = r10.document
            if (r10 == 0) goto L_0x0af8
            long r11 = r9.id
            long r9 = r10.id
            int r13 = (r11 > r9 ? 1 : (r11 == r9 ? 0 : -1))
            if (r13 == 0) goto L_0x0af8
            goto L_0x0afa
        L_0x0af8:
            r9 = 0
            goto L_0x0afb
        L_0x0afa:
            r9 = 1
        L_0x0afb:
            if (r9 == 0) goto L_0x0b0f
            if (r8 == 0) goto L_0x0b0f
            r9 = 2131625341(0x7f0e057d, float:1.8877887E38)
            java.lang.String r10 = "EventLogEditedMediaCaption"
            java.lang.String r9 = org.telegram.messenger.LocaleController.getString(r10, r9)
            java.lang.CharSequence r9 = replaceWithLink(r9, r14, r5)
            r0.messageText = r9
            goto L_0x0b30
        L_0x0b0f:
            if (r8 == 0) goto L_0x0b21
            r9 = 2131625331(0x7f0e0573, float:1.8877867E38)
            java.lang.String r10 = "EventLogEditedCaption"
            java.lang.String r9 = org.telegram.messenger.LocaleController.getString(r10, r9)
            java.lang.CharSequence r9 = replaceWithLink(r9, r14, r5)
            r0.messageText = r9
            goto L_0x0b30
        L_0x0b21:
            r9 = 2131625340(0x7f0e057c, float:1.8877885E38)
            java.lang.String r10 = "EventLogEditedMedia"
            java.lang.String r9 = org.telegram.messenger.LocaleController.getString(r10, r9)
            java.lang.CharSequence r9 = replaceWithLink(r9, r14, r5)
            r0.messageText = r9
        L_0x0b30:
            org.telegram.tgnet.TLRPC$MessageMedia r9 = r6.media
            r2.media = r9
            if (r8 == 0) goto L_0x0bcc
            org.telegram.tgnet.TLRPC$TL_webPage r8 = new org.telegram.tgnet.TLRPC$TL_webPage
            r8.<init>()
            r9.webpage = r8
            org.telegram.tgnet.TLRPC$MessageMedia r8 = r2.media
            org.telegram.tgnet.TLRPC$WebPage r8 = r8.webpage
            r9 = 2131625366(0x7f0e0596, float:1.8877938E38)
            java.lang.String r10 = "EventLogOriginalCaption"
            java.lang.String r9 = org.telegram.messenger.LocaleController.getString(r10, r9)
            r8.site_name = r9
            java.lang.String r8 = r3.message
            boolean r8 = android.text.TextUtils.isEmpty(r8)
            if (r8 == 0) goto L_0x0b64
            org.telegram.tgnet.TLRPC$MessageMedia r3 = r2.media
            org.telegram.tgnet.TLRPC$WebPage r3 = r3.webpage
            r8 = 2131625367(0x7f0e0597, float:1.887794E38)
            java.lang.String r9 = "EventLogOriginalCaptionEmpty"
            java.lang.String r8 = org.telegram.messenger.LocaleController.getString(r9, r8)
            r3.description = r8
            goto L_0x0bcc
        L_0x0b64:
            org.telegram.tgnet.TLRPC$MessageMedia r8 = r2.media
            org.telegram.tgnet.TLRPC$WebPage r8 = r8.webpage
            java.lang.String r3 = r3.message
            r8.description = r3
            goto L_0x0bcc
        L_0x0b6d:
            r8 = 2131625342(0x7f0e057e, float:1.887789E38)
            java.lang.String r9 = "EventLogEditedMessages"
            java.lang.String r8 = org.telegram.messenger.LocaleController.getString(r9, r8)
            java.lang.CharSequence r8 = replaceWithLink(r8, r14, r5)
            r0.messageText = r8
            org.telegram.tgnet.TLRPC$MessageAction r8 = r6.action
            boolean r8 = r8 instanceof org.telegram.tgnet.TLRPC$TL_messageActionGroupCall
            if (r8 == 0) goto L_0x0b8b
            org.telegram.tgnet.TLRPC$TL_messageMediaEmpty r2 = new org.telegram.tgnet.TLRPC$TL_messageMediaEmpty
            r2.<init>()
            r6.media = r2
            r2 = r6
            goto L_0x0bcc
        L_0x0b8b:
            java.lang.String r8 = r6.message
            r2.message = r8
            org.telegram.tgnet.TLRPC$TL_messageMediaWebPage r8 = new org.telegram.tgnet.TLRPC$TL_messageMediaWebPage
            r8.<init>()
            r2.media = r8
            org.telegram.tgnet.TLRPC$TL_webPage r9 = new org.telegram.tgnet.TLRPC$TL_webPage
            r9.<init>()
            r8.webpage = r9
            org.telegram.tgnet.TLRPC$MessageMedia r8 = r2.media
            org.telegram.tgnet.TLRPC$WebPage r8 = r8.webpage
            r9 = 2131625368(0x7f0e0598, float:1.8877942E38)
            java.lang.String r10 = "EventLogOriginalMessages"
            java.lang.String r9 = org.telegram.messenger.LocaleController.getString(r10, r9)
            r8.site_name = r9
            java.lang.String r8 = r3.message
            boolean r8 = android.text.TextUtils.isEmpty(r8)
            if (r8 == 0) goto L_0x0bc4
            org.telegram.tgnet.TLRPC$MessageMedia r3 = r2.media
            org.telegram.tgnet.TLRPC$WebPage r3 = r3.webpage
            r8 = 2131625367(0x7f0e0597, float:1.887794E38)
            java.lang.String r9 = "EventLogOriginalCaptionEmpty"
            java.lang.String r8 = org.telegram.messenger.LocaleController.getString(r9, r8)
            r3.description = r8
            goto L_0x0bcc
        L_0x0bc4:
            org.telegram.tgnet.TLRPC$MessageMedia r8 = r2.media
            org.telegram.tgnet.TLRPC$WebPage r8 = r8.webpage
            java.lang.String r3 = r3.message
            r8.description = r3
        L_0x0bcc:
            org.telegram.tgnet.TLRPC$ReplyMarkup r3 = r6.reply_markup
            r2.reply_markup = r3
            org.telegram.tgnet.TLRPC$MessageMedia r3 = r2.media
            org.telegram.tgnet.TLRPC$WebPage r3 = r3.webpage
            if (r3 == 0) goto L_0x1196
            r6 = 10
            r3.flags = r6
            r3.display_url = r7
            r3.url = r7
            goto L_0x1196
        L_0x0be0:
            boolean r2 = r8 instanceof org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionChangeStickerSet
            if (r2 == 0) goto L_0x0CLASSNAME
            r2 = r8
            org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionChangeStickerSet r2 = (org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionChangeStickerSet) r2
            org.telegram.tgnet.TLRPC$InputStickerSet r2 = r2.new_stickerset
            org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionChangeStickerSet r8 = (org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionChangeStickerSet) r8
            org.telegram.tgnet.TLRPC$InputStickerSet r3 = r8.new_stickerset
            if (r2 == 0) goto L_0x0CLASSNAME
            boolean r2 = r2 instanceof org.telegram.tgnet.TLRPC$TL_inputStickerSetEmpty
            if (r2 == 0) goto L_0x0bf4
            goto L_0x0CLASSNAME
        L_0x0bf4:
            r2 = 2131625325(0x7f0e056d, float:1.8877855E38)
            java.lang.String r3 = "EventLogChangedStickersSet"
            java.lang.String r2 = org.telegram.messenger.LocaleController.getString(r3, r2)
            java.lang.CharSequence r2 = replaceWithLink(r2, r14, r5)
            r0.messageText = r2
            goto L_0x1195
        L_0x0CLASSNAME:
            r2 = 2131625393(0x7f0e05b1, float:1.8877993E38)
            java.lang.String r3 = "EventLogRemovedStickersSet"
            java.lang.String r2 = org.telegram.messenger.LocaleController.getString(r3, r2)
            java.lang.CharSequence r2 = replaceWithLink(r2, r14, r5)
            r0.messageText = r2
            goto L_0x1195
        L_0x0CLASSNAME:
            boolean r2 = r8 instanceof org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionChangeLocation
            if (r2 == 0) goto L_0x0c4e
            org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionChangeLocation r8 = (org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionChangeLocation) r8
            org.telegram.tgnet.TLRPC$ChannelLocation r2 = r8.new_value
            boolean r3 = r2 instanceof org.telegram.tgnet.TLRPC$TL_channelLocationEmpty
            if (r3 == 0) goto L_0x0CLASSNAME
            r2 = 2131625392(0x7f0e05b0, float:1.887799E38)
            java.lang.String r3 = "EventLogRemovedLocation"
            java.lang.String r2 = org.telegram.messenger.LocaleController.getString(r3, r2)
            java.lang.CharSequence r2 = replaceWithLink(r2, r14, r5)
            r0.messageText = r2
            goto L_0x1195
        L_0x0CLASSNAME:
            org.telegram.tgnet.TLRPC$TL_channelLocation r2 = (org.telegram.tgnet.TLRPC$TL_channelLocation) r2
            r3 = 2131625323(0x7f0e056b, float:1.887785E38)
            r6 = 1
            java.lang.Object[] r8 = new java.lang.Object[r6]
            java.lang.String r2 = r2.address
            r6 = 0
            r8[r6] = r2
            java.lang.String r2 = "EventLogChangedLocation"
            java.lang.String r2 = org.telegram.messenger.LocaleController.formatString(r2, r3, r8)
            java.lang.CharSequence r2 = replaceWithLink(r2, r14, r5)
            r0.messageText = r2
            goto L_0x1195
        L_0x0c4e:
            boolean r2 = r8 instanceof org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionToggleSlowMode
            r3 = 3600(0xe10, float:5.045E-42)
            if (r2 == 0) goto L_0x0c9b
            org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionToggleSlowMode r8 = (org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionToggleSlowMode) r8
            int r2 = r8.new_value
            if (r2 != 0) goto L_0x0c6b
            r2 = 2131625415(0x7f0e05c7, float:1.8878037E38)
            java.lang.String r3 = "EventLogToggledSlowmodeOff"
            java.lang.String r2 = org.telegram.messenger.LocaleController.getString(r3, r2)
            java.lang.CharSequence r2 = replaceWithLink(r2, r14, r5)
            r0.messageText = r2
            goto L_0x1195
        L_0x0c6b:
            r6 = 60
            if (r2 >= r6) goto L_0x0CLASSNAME
            java.lang.String r3 = "Seconds"
            java.lang.String r2 = org.telegram.messenger.LocaleController.formatPluralString(r3, r2)
            goto L_0x0CLASSNAME
        L_0x0CLASSNAME:
            if (r2 >= r3) goto L_0x0c7e
            int r2 = r2 / r6
            java.lang.String r2 = org.telegram.messenger.LocaleController.formatPluralString(r15, r2)
            goto L_0x0CLASSNAME
        L_0x0c7e:
            int r2 = r2 / r6
            int r2 = r2 / r6
            java.lang.String r2 = org.telegram.messenger.LocaleController.formatPluralString(r11, r2)
        L_0x0CLASSNAME:
            r3 = 2131625416(0x7f0e05c8, float:1.887804E38)
            r6 = 1
            java.lang.Object[] r8 = new java.lang.Object[r6]
            r6 = 0
            r8[r6] = r2
            java.lang.String r2 = "EventLogToggledSlowmodeOn"
            java.lang.String r2 = org.telegram.messenger.LocaleController.formatString(r2, r3, r8)
            java.lang.CharSequence r2 = replaceWithLink(r2, r14, r5)
            r0.messageText = r2
            goto L_0x1195
        L_0x0c9b:
            boolean r2 = r8 instanceof org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionStartGroupCall
            if (r2 == 0) goto L_0x0cb0
            r2 = 2131625406(0x7f0e05be, float:1.887802E38)
            java.lang.String r3 = "EventLogStartedVoiceChat"
            java.lang.String r2 = org.telegram.messenger.LocaleController.getString(r3, r2)
            java.lang.CharSequence r2 = replaceWithLink(r2, r14, r5)
            r0.messageText = r2
            goto L_0x1195
        L_0x0cb0:
            boolean r2 = r8 instanceof org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionDiscardGroupCall
            if (r2 == 0) goto L_0x0cc5
            r2 = 2131625347(0x7f0e0583, float:1.88779E38)
            java.lang.String r3 = "EventLogEndedVoiceChat"
            java.lang.String r2 = org.telegram.messenger.LocaleController.getString(r3, r2)
            java.lang.CharSequence r2 = replaceWithLink(r2, r14, r5)
            r0.messageText = r2
            goto L_0x1195
        L_0x0cc5:
            boolean r2 = r8 instanceof org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionParticipantMute
            if (r2 == 0) goto L_0x0cf4
            org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionParticipantMute r8 = (org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionParticipantMute) r8
            int r2 = r0.currentAccount
            org.telegram.messenger.MessagesController r2 = org.telegram.messenger.MessagesController.getInstance(r2)
            org.telegram.tgnet.TLRPC$TL_groupCallParticipant r3 = r8.participant
            int r3 = r3.user_id
            java.lang.Integer r3 = java.lang.Integer.valueOf(r3)
            org.telegram.tgnet.TLRPC$User r2 = r2.getUser(r3)
            r3 = 2131625419(0x7f0e05cb, float:1.8878045E38)
            java.lang.String r6 = "EventLogVoiceChatMuted"
            java.lang.String r3 = org.telegram.messenger.LocaleController.getString(r6, r3)
            java.lang.CharSequence r3 = replaceWithLink(r3, r14, r5)
            r0.messageText = r3
            java.lang.CharSequence r2 = replaceWithLink(r3, r12, r2)
            r0.messageText = r2
            goto L_0x1195
        L_0x0cf4:
            boolean r2 = r8 instanceof org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionParticipantUnmute
            if (r2 == 0) goto L_0x0d23
            org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionParticipantUnmute r8 = (org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionParticipantUnmute) r8
            int r2 = r0.currentAccount
            org.telegram.messenger.MessagesController r2 = org.telegram.messenger.MessagesController.getInstance(r2)
            org.telegram.tgnet.TLRPC$TL_groupCallParticipant r3 = r8.participant
            int r3 = r3.user_id
            java.lang.Integer r3 = java.lang.Integer.valueOf(r3)
            org.telegram.tgnet.TLRPC$User r2 = r2.getUser(r3)
            r3 = 2131625421(0x7f0e05cd, float:1.887805E38)
            java.lang.String r6 = "EventLogVoiceChatUnmuted"
            java.lang.String r3 = org.telegram.messenger.LocaleController.getString(r6, r3)
            java.lang.CharSequence r3 = replaceWithLink(r3, r14, r5)
            r0.messageText = r3
            java.lang.CharSequence r2 = replaceWithLink(r3, r12, r2)
            r0.messageText = r2
            goto L_0x1195
        L_0x0d23:
            boolean r2 = r8 instanceof org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionToggleGroupCallSetting
            if (r2 == 0) goto L_0x0d4f
            org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionToggleGroupCallSetting r8 = (org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionToggleGroupCallSetting) r8
            boolean r2 = r8.join_muted
            if (r2 == 0) goto L_0x0d3e
            r2 = 2131625420(0x7f0e05cc, float:1.8878047E38)
            java.lang.String r3 = "EventLogVoiceChatNotAllowedToSpeak"
            java.lang.String r2 = org.telegram.messenger.LocaleController.getString(r3, r2)
            java.lang.CharSequence r2 = replaceWithLink(r2, r14, r5)
            r0.messageText = r2
            goto L_0x1195
        L_0x0d3e:
            r2 = 2131625418(0x7f0e05ca, float:1.8878043E38)
            java.lang.String r3 = "EventLogVoiceChatAllowedToSpeak"
            java.lang.String r2 = org.telegram.messenger.LocaleController.getString(r3, r2)
            java.lang.CharSequence r2 = replaceWithLink(r2, r14, r5)
            r0.messageText = r2
            goto L_0x1195
        L_0x0d4f:
            boolean r2 = r8 instanceof org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionParticipantJoinByInvite
            if (r2 == 0) goto L_0x0d66
            org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionParticipantJoinByInvite r8 = (org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionParticipantJoinByInvite) r8
            r2 = 2131624125(0x7f0e00bd, float:1.887542E38)
            java.lang.String r3 = "ActionInviteUser"
            java.lang.String r2 = org.telegram.messenger.LocaleController.getString(r3, r2)
            java.lang.CharSequence r2 = replaceWithLink(r2, r14, r5)
            r0.messageText = r2
            goto L_0x1195
        L_0x0d66:
            boolean r2 = r8 instanceof org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionExportedInviteDelete
            if (r2 == 0) goto L_0x0d87
            org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionExportedInviteDelete r8 = (org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionExportedInviteDelete) r8
            r2 = 2131624116(0x7f0e00b4, float:1.8875403E38)
            r3 = 1
            java.lang.Object[] r6 = new java.lang.Object[r3]
            org.telegram.tgnet.TLRPC$TL_chatInviteExported r3 = r8.invite
            java.lang.String r3 = r3.link
            r8 = 0
            r6[r8] = r3
            java.lang.String r3 = "ActionDeletedInviteLink"
            java.lang.String r2 = org.telegram.messenger.LocaleController.formatString(r3, r2, r6)
            java.lang.CharSequence r2 = replaceWithLink(r2, r14, r5)
            r0.messageText = r2
            goto L_0x1195
        L_0x0d87:
            boolean r2 = r8 instanceof org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionExportedInviteRevoke
            if (r2 == 0) goto L_0x0da8
            org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionExportedInviteRevoke r8 = (org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionExportedInviteRevoke) r8
            r2 = 2131624149(0x7f0e00d5, float:1.887547E38)
            r3 = 1
            java.lang.Object[] r6 = new java.lang.Object[r3]
            org.telegram.tgnet.TLRPC$TL_chatInviteExported r3 = r8.invite
            java.lang.String r3 = r3.link
            r8 = 0
            r6[r8] = r3
            java.lang.String r3 = "ActionRevokedInviteLink"
            java.lang.String r2 = org.telegram.messenger.LocaleController.formatString(r3, r2, r6)
            java.lang.CharSequence r2 = replaceWithLink(r2, r14, r5)
            r0.messageText = r2
            goto L_0x1195
        L_0x0da8:
            boolean r2 = r8 instanceof org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionExportedInviteEdit
            if (r2 == 0) goto L_0x0dfb
            org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionExportedInviteEdit r8 = (org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionExportedInviteEdit) r8
            org.telegram.tgnet.TLRPC$TL_chatInviteExported r2 = r8.prev_invite
            java.lang.String r2 = r2.link
            if (r2 == 0) goto L_0x0dd9
            org.telegram.tgnet.TLRPC$TL_chatInviteExported r3 = r8.new_invite
            java.lang.String r3 = r3.link
            boolean r2 = r2.equals(r3)
            if (r2 == 0) goto L_0x0dd9
            r2 = 2131624118(0x7f0e00b6, float:1.8875407E38)
            r3 = 1
            java.lang.Object[] r6 = new java.lang.Object[r3]
            org.telegram.tgnet.TLRPC$TL_chatInviteExported r3 = r8.prev_invite
            java.lang.String r3 = r3.link
            r8 = 0
            r6[r8] = r3
            java.lang.String r3 = "ActionEditedInviteLinkToSame"
            java.lang.String r2 = org.telegram.messenger.LocaleController.formatString(r3, r2, r6)
            java.lang.CharSequence r2 = replaceWithLink(r2, r14, r5)
            r0.messageText = r2
            goto L_0x1195
        L_0x0dd9:
            r2 = 2131624117(0x7f0e00b5, float:1.8875405E38)
            r3 = 2
            java.lang.Object[] r3 = new java.lang.Object[r3]
            org.telegram.tgnet.TLRPC$TL_chatInviteExported r6 = r8.prev_invite
            java.lang.String r6 = r6.link
            r9 = 0
            r3[r9] = r6
            org.telegram.tgnet.TLRPC$TL_chatInviteExported r6 = r8.new_invite
            java.lang.String r6 = r6.link
            r8 = 1
            r3[r8] = r6
            java.lang.String r6 = "ActionEditedInviteLink"
            java.lang.String r2 = org.telegram.messenger.LocaleController.formatString(r6, r2, r3)
            java.lang.CharSequence r2 = replaceWithLink(r2, r14, r5)
            r0.messageText = r2
            goto L_0x1195
        L_0x0dfb:
            boolean r2 = r8 instanceof org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionParticipantVolume
            if (r2 == 0) goto L_0x0e51
            org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionParticipantVolume r8 = (org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionParticipantVolume) r8
            org.telegram.tgnet.TLRPC$TL_groupCallParticipant r2 = r8.participant
            int r2 = org.telegram.messenger.ChatObject.getParticipantVolume(r2)
            double r2 = (double) r2
            r9 = 4636737291354636288(0xNUM, double:100.0)
            java.lang.Double.isNaN(r2)
            double r2 = r2 / r9
            int r6 = r0.currentAccount
            org.telegram.messenger.MessagesController r6 = org.telegram.messenger.MessagesController.getInstance(r6)
            org.telegram.tgnet.TLRPC$TL_groupCallParticipant r8 = r8.participant
            int r8 = r8.user_id
            java.lang.Integer r8 = java.lang.Integer.valueOf(r8)
            org.telegram.tgnet.TLRPC$User r6 = r6.getUser(r8)
            r9 = 1
            java.lang.Object[] r10 = new java.lang.Object[r9]
            r16 = 0
            int r9 = (r2 > r16 ? 1 : (r2 == r16 ? 0 : -1))
            if (r9 <= 0) goto L_0x0e30
            r8 = 4607182418800017408(0x3ffNUM, double:1.0)
            double r2 = java.lang.Math.max(r2, r8)
            goto L_0x0e32
        L_0x0e30:
            r2 = 0
        L_0x0e32:
            int r2 = (int) r2
            java.lang.Integer r2 = java.lang.Integer.valueOf(r2)
            r3 = 0
            r10[r3] = r2
            java.lang.String r2 = "ActionVolumeChanged"
            r3 = 2131624165(0x7f0e00e5, float:1.8875502E38)
            java.lang.String r2 = org.telegram.messenger.LocaleController.formatString(r2, r3, r10)
            java.lang.CharSequence r2 = replaceWithLink(r2, r14, r5)
            r0.messageText = r2
            java.lang.CharSequence r2 = replaceWithLink(r2, r12, r6)
            r0.messageText = r2
            goto L_0x1195
        L_0x0e51:
            boolean r2 = r8 instanceof org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionChangeHistoryTTL
            if (r2 == 0) goto L_0x0ed7
            org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionChangeHistoryTTL r8 = (org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionChangeHistoryTTL) r8
            boolean r2 = r4.megagroup
            if (r2 != 0) goto L_0x0e83
            int r2 = r8.new_value
            if (r2 == 0) goto L_0x0e76
            r3 = 2131624151(0x7f0e00d7, float:1.8875474E38)
            r6 = 1
            java.lang.Object[] r8 = new java.lang.Object[r6]
            java.lang.String r2 = org.telegram.messenger.LocaleController.formatTTLString(r2)
            r6 = 0
            r8[r6] = r2
            java.lang.String r2 = "ActionTTLChannelChanged"
            java.lang.String r2 = org.telegram.messenger.LocaleController.formatString(r2, r3, r8)
            r0.messageText = r2
            goto L_0x1195
        L_0x0e76:
            r2 = 2131624152(0x7f0e00d8, float:1.8875476E38)
            java.lang.String r3 = "ActionTTLChannelDisabled"
            java.lang.String r2 = org.telegram.messenger.LocaleController.getString(r3, r2)
            r0.messageText = r2
            goto L_0x1195
        L_0x0e83:
            int r2 = r8.new_value
            if (r2 != 0) goto L_0x0e98
            r2 = 2131624153(0x7f0e00d9, float:1.8875478E38)
            java.lang.String r3 = "ActionTTLDisabled"
            java.lang.String r2 = org.telegram.messenger.LocaleController.getString(r3, r2)
            java.lang.CharSequence r2 = replaceWithLink(r2, r14, r5)
            r0.messageText = r2
            goto L_0x1195
        L_0x0e98:
            r6 = 86400(0x15180, float:1.21072E-40)
            if (r2 <= r6) goto L_0x0ea8
            r3 = 86400(0x15180, float:1.21072E-40)
            int r2 = r2 / r3
            java.lang.String r3 = "Days"
            java.lang.String r2 = org.telegram.messenger.LocaleController.formatPluralString(r3, r2)
            goto L_0x0ec0
        L_0x0ea8:
            if (r2 < r3) goto L_0x0eb0
            int r2 = r2 / r3
            java.lang.String r2 = org.telegram.messenger.LocaleController.formatPluralString(r11, r2)
            goto L_0x0ec0
        L_0x0eb0:
            r3 = 60
            if (r2 < r3) goto L_0x0eba
            int r2 = r2 / r3
            java.lang.String r2 = org.telegram.messenger.LocaleController.formatPluralString(r15, r2)
            goto L_0x0ec0
        L_0x0eba:
            java.lang.String r3 = "Seconds"
            java.lang.String r2 = org.telegram.messenger.LocaleController.formatPluralString(r3, r2)
        L_0x0ec0:
            r3 = 2131624150(0x7f0e00d6, float:1.8875472E38)
            r6 = 1
            java.lang.Object[] r8 = new java.lang.Object[r6]
            r6 = 0
            r8[r6] = r2
            java.lang.String r2 = "ActionTTLChanged"
            java.lang.String r2 = org.telegram.messenger.LocaleController.formatString(r2, r3, r8)
            java.lang.CharSequence r2 = replaceWithLink(r2, r14, r5)
            r0.messageText = r2
            goto L_0x1195
        L_0x0ed7:
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "unsupported "
            r2.append(r3)
            org.telegram.tgnet.TLRPC$ChannelAdminLogEventAction r3 = r1.action
            r2.append(r3)
            java.lang.String r2 = r2.toString()
            r0.messageText = r2
            goto L_0x1195
        L_0x0eee:
            r7 = r13
            boolean r2 = r8 instanceof org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionParticipantToggleAdmin
            if (r2 == 0) goto L_0x0efa
            org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionParticipantToggleAdmin r8 = (org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionParticipantToggleAdmin) r8
            org.telegram.tgnet.TLRPC$ChannelParticipant r2 = r8.prev_participant
            org.telegram.tgnet.TLRPC$ChannelParticipant r3 = r8.new_participant
            goto L_0x0var_
        L_0x0efa:
            org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionParticipantToggleBan r8 = (org.telegram.tgnet.TLRPC$TL_channelAdminLogEventActionParticipantToggleBan) r8
            org.telegram.tgnet.TLRPC$ChannelParticipant r2 = r8.prev_participant
            org.telegram.tgnet.TLRPC$ChannelParticipant r3 = r8.new_participant
        L_0x0var_:
            org.telegram.tgnet.TLRPC$TL_message r6 = new org.telegram.tgnet.TLRPC$TL_message
            r6.<init>()
            r0.messageOwner = r6
            int r6 = r0.currentAccount
            org.telegram.messenger.MessagesController r6 = org.telegram.messenger.MessagesController.getInstance(r6)
            int r8 = r2.user_id
            java.lang.Integer r8 = java.lang.Integer.valueOf(r8)
            org.telegram.tgnet.TLRPC$User r6 = r6.getUser(r8)
            boolean r8 = r2 instanceof org.telegram.tgnet.TLRPC$TL_channelParticipantCreator
            if (r8 != 0) goto L_0x0var_
            boolean r8 = r3 instanceof org.telegram.tgnet.TLRPC$TL_channelParticipantCreator
            if (r8 == 0) goto L_0x0var_
            r2 = 2131625324(0x7f0e056c, float:1.8877853E38)
            java.lang.String r3 = "EventLogChangedOwnership"
            java.lang.String r2 = org.telegram.messenger.LocaleController.getString(r3, r2)
            int r3 = r2.indexOf(r9)
            java.lang.StringBuilder r8 = new java.lang.StringBuilder
            r9 = 1
            java.lang.Object[] r10 = new java.lang.Object[r9]
            org.telegram.tgnet.TLRPC$Message r9 = r0.messageOwner
            java.util.ArrayList<org.telegram.tgnet.TLRPC$MessageEntity> r9 = r9.entities
            java.lang.String r3 = r0.getUserName(r6, r9, r3)
            r6 = 0
            r10[r6] = r3
            java.lang.String r2 = java.lang.String.format(r2, r10)
            r8.<init>(r2)
            goto L_0x118f
        L_0x0var_:
            org.telegram.tgnet.TLRPC$TL_chatAdminRights r8 = r2.admin_rights
            org.telegram.tgnet.TLRPC$TL_chatAdminRights r10 = r3.admin_rights
            if (r8 != 0) goto L_0x0var_
            org.telegram.tgnet.TLRPC$TL_chatAdminRights r8 = new org.telegram.tgnet.TLRPC$TL_chatAdminRights
            r8.<init>()
        L_0x0var_:
            if (r10 != 0) goto L_0x0var_
            org.telegram.tgnet.TLRPC$TL_chatAdminRights r10 = new org.telegram.tgnet.TLRPC$TL_chatAdminRights
            r10.<init>()
        L_0x0var_:
            boolean r11 = r10.other
            if (r11 == 0) goto L_0x0var_
            r11 = 2131625381(0x7f0e05a5, float:1.8877968E38)
            java.lang.String r12 = "EventLogPromotedNoRights"
            java.lang.String r11 = org.telegram.messenger.LocaleController.getString(r12, r11)
            goto L_0x0f6e
        L_0x0var_:
            r11 = 2131625372(0x7f0e059c, float:1.887795E38)
            java.lang.String r12 = "EventLogPromoted"
            java.lang.String r11 = org.telegram.messenger.LocaleController.getString(r12, r11)
        L_0x0f6e:
            int r9 = r11.indexOf(r9)
            java.lang.StringBuilder r12 = new java.lang.StringBuilder
            r13 = 1
            java.lang.Object[] r14 = new java.lang.Object[r13]
            org.telegram.tgnet.TLRPC$Message r13 = r0.messageOwner
            java.util.ArrayList<org.telegram.tgnet.TLRPC$MessageEntity> r13 = r13.entities
            java.lang.String r6 = r0.getUserName(r6, r13, r9)
            r9 = 0
            r14[r9] = r6
            java.lang.String r6 = java.lang.String.format(r11, r14)
            r12.<init>(r6)
            java.lang.String r6 = "\n"
            r12.append(r6)
            java.lang.String r2 = r2.rank
            java.lang.String r6 = r3.rank
            boolean r2 = android.text.TextUtils.equals(r2, r6)
            if (r2 != 0) goto L_0x0fe4
            java.lang.String r2 = r3.rank
            boolean r2 = android.text.TextUtils.isEmpty(r2)
            if (r2 == 0) goto L_0x0fbe
            r2 = 10
            r12.append(r2)
            r6 = 45
            r12.append(r6)
            r9 = 32
            r12.append(r9)
            r3 = 2131625384(0x7f0e05a8, float:1.8877974E38)
            java.lang.String r11 = "EventLogPromotedRemovedTitle"
            java.lang.String r3 = org.telegram.messenger.LocaleController.getString(r11, r3)
            r12.append(r3)
            r2 = 43
            goto L_0x0fe8
        L_0x0fbe:
            r2 = 10
            r6 = 45
            r9 = 32
            r12.append(r2)
            r2 = 43
            r12.append(r2)
            r12.append(r9)
            r9 = 2131625386(0x7f0e05aa, float:1.8877978E38)
            r11 = 1
            java.lang.Object[] r13 = new java.lang.Object[r11]
            java.lang.String r3 = r3.rank
            r11 = 0
            r13[r11] = r3
            java.lang.String r3 = "EventLogPromotedTitle"
            java.lang.String r3 = org.telegram.messenger.LocaleController.formatString(r3, r9, r13)
            r12.append(r3)
            goto L_0x0fe8
        L_0x0fe4:
            r2 = 43
            r6 = 45
        L_0x0fe8:
            boolean r3 = r8.change_info
            boolean r9 = r10.change_info
            if (r3 == r9) goto L_0x101a
            r3 = 10
            r12.append(r3)
            boolean r3 = r10.change_info
            if (r3 == 0) goto L_0x0ffa
            r3 = 43
            goto L_0x0ffc
        L_0x0ffa:
            r3 = 45
        L_0x0ffc:
            r12.append(r3)
            r3 = 32
            r12.append(r3)
            boolean r3 = r4.megagroup
            if (r3 == 0) goto L_0x100e
            r3 = 2131625377(0x7f0e05a1, float:1.887796E38)
            java.lang.String r9 = "EventLogPromotedChangeGroupInfo"
            goto L_0x1013
        L_0x100e:
            r3 = 2131625376(0x7f0e05a0, float:1.8877958E38)
            java.lang.String r9 = "EventLogPromotedChangeChannelInfo"
        L_0x1013:
            java.lang.String r3 = org.telegram.messenger.LocaleController.getString(r9, r3)
            r12.append(r3)
        L_0x101a:
            boolean r3 = r4.megagroup
            if (r3 != 0) goto L_0x106e
            boolean r3 = r8.post_messages
            boolean r9 = r10.post_messages
            if (r3 == r9) goto L_0x1046
            r3 = 10
            r12.append(r3)
            boolean r3 = r10.post_messages
            if (r3 == 0) goto L_0x1030
            r3 = 43
            goto L_0x1032
        L_0x1030:
            r3 = 45
        L_0x1032:
            r12.append(r3)
            r3 = 32
            r12.append(r3)
            r3 = 2131625383(0x7f0e05a7, float:1.8877972E38)
            java.lang.String r9 = "EventLogPromotedPostMessages"
            java.lang.String r3 = org.telegram.messenger.LocaleController.getString(r9, r3)
            r12.append(r3)
        L_0x1046:
            boolean r3 = r8.edit_messages
            boolean r9 = r10.edit_messages
            if (r3 == r9) goto L_0x106e
            r3 = 10
            r12.append(r3)
            boolean r3 = r10.edit_messages
            if (r3 == 0) goto L_0x1058
            r3 = 43
            goto L_0x105a
        L_0x1058:
            r3 = 45
        L_0x105a:
            r12.append(r3)
            r3 = 32
            r12.append(r3)
            r3 = 2131625379(0x7f0e05a3, float:1.8877964E38)
            java.lang.String r9 = "EventLogPromotedEditMessages"
            java.lang.String r3 = org.telegram.messenger.LocaleController.getString(r9, r3)
            r12.append(r3)
        L_0x106e:
            boolean r3 = r8.delete_messages
            boolean r9 = r10.delete_messages
            if (r3 == r9) goto L_0x1096
            r3 = 10
            r12.append(r3)
            boolean r3 = r10.delete_messages
            if (r3 == 0) goto L_0x1080
            r3 = 43
            goto L_0x1082
        L_0x1080:
            r3 = 45
        L_0x1082:
            r12.append(r3)
            r3 = 32
            r12.append(r3)
            r3 = 2131625378(0x7f0e05a2, float:1.8877962E38)
            java.lang.String r9 = "EventLogPromotedDeleteMessages"
            java.lang.String r3 = org.telegram.messenger.LocaleController.getString(r9, r3)
            r12.append(r3)
        L_0x1096:
            boolean r3 = r8.add_admins
            boolean r9 = r10.add_admins
            if (r3 == r9) goto L_0x10be
            r3 = 10
            r12.append(r3)
            boolean r3 = r10.add_admins
            if (r3 == 0) goto L_0x10a8
            r3 = 43
            goto L_0x10aa
        L_0x10a8:
            r3 = 45
        L_0x10aa:
            r12.append(r3)
            r3 = 32
            r12.append(r3)
            r3 = 2131625373(0x7f0e059d, float:1.8877952E38)
            java.lang.String r9 = "EventLogPromotedAddAdmins"
            java.lang.String r3 = org.telegram.messenger.LocaleController.getString(r9, r3)
            r12.append(r3)
        L_0x10be:
            boolean r3 = r8.anonymous
            boolean r9 = r10.anonymous
            if (r3 == r9) goto L_0x10e6
            r3 = 10
            r12.append(r3)
            boolean r3 = r10.anonymous
            if (r3 == 0) goto L_0x10d0
            r3 = 43
            goto L_0x10d2
        L_0x10d0:
            r3 = 45
        L_0x10d2:
            r12.append(r3)
            r3 = 32
            r12.append(r3)
            r3 = 2131625385(0x7f0e05a9, float:1.8877976E38)
            java.lang.String r9 = "EventLogPromotedSendAnonymously"
            java.lang.String r3 = org.telegram.messenger.LocaleController.getString(r9, r3)
            r12.append(r3)
        L_0x10e6:
            boolean r3 = r4.megagroup
            if (r3 == 0) goto L_0x113a
            boolean r3 = r8.ban_users
            boolean r9 = r10.ban_users
            if (r3 == r9) goto L_0x1112
            r3 = 10
            r12.append(r3)
            boolean r3 = r10.ban_users
            if (r3 == 0) goto L_0x10fc
            r3 = 43
            goto L_0x10fe
        L_0x10fc:
            r3 = 45
        L_0x10fe:
            r12.append(r3)
            r3 = 32
            r12.append(r3)
            r3 = 2131625375(0x7f0e059f, float:1.8877956E38)
            java.lang.String r9 = "EventLogPromotedBanUsers"
            java.lang.String r3 = org.telegram.messenger.LocaleController.getString(r9, r3)
            r12.append(r3)
        L_0x1112:
            boolean r3 = r8.manage_call
            boolean r9 = r10.manage_call
            if (r3 == r9) goto L_0x113a
            r3 = 10
            r12.append(r3)
            boolean r3 = r10.manage_call
            if (r3 == 0) goto L_0x1124
            r3 = 43
            goto L_0x1126
        L_0x1124:
            r3 = 45
        L_0x1126:
            r12.append(r3)
            r3 = 32
            r12.append(r3)
            r3 = 2131625380(0x7f0e05a4, float:1.8877966E38)
            java.lang.String r9 = "EventLogPromotedManageCall"
            java.lang.String r3 = org.telegram.messenger.LocaleController.getString(r9, r3)
            r12.append(r3)
        L_0x113a:
            boolean r3 = r8.invite_users
            boolean r9 = r10.invite_users
            if (r3 == r9) goto L_0x1162
            r3 = 10
            r12.append(r3)
            boolean r3 = r10.invite_users
            if (r3 == 0) goto L_0x114c
            r3 = 43
            goto L_0x114e
        L_0x114c:
            r3 = 45
        L_0x114e:
            r12.append(r3)
            r3 = 32
            r12.append(r3)
            r3 = 2131625374(0x7f0e059e, float:1.8877954E38)
            java.lang.String r9 = "EventLogPromotedAddUsers"
            java.lang.String r3 = org.telegram.messenger.LocaleController.getString(r9, r3)
            r12.append(r3)
        L_0x1162:
            boolean r3 = r4.megagroup
            if (r3 == 0) goto L_0x118e
            boolean r3 = r8.pin_messages
            boolean r8 = r10.pin_messages
            if (r3 == r8) goto L_0x118e
            r3 = 10
            r12.append(r3)
            boolean r3 = r10.pin_messages
            if (r3 == 0) goto L_0x1178
            r10 = 43
            goto L_0x117a
        L_0x1178:
            r10 = 45
        L_0x117a:
            r12.append(r10)
            r2 = 32
            r12.append(r2)
            r2 = 2131625382(0x7f0e05a6, float:1.887797E38)
            java.lang.String r3 = "EventLogPromotedPinMessages"
            java.lang.String r2 = org.telegram.messenger.LocaleController.getString(r3, r2)
            r12.append(r2)
        L_0x118e:
            r8 = r12
        L_0x118f:
            java.lang.String r2 = r8.toString()
            r0.messageText = r2
        L_0x1195:
            r2 = 0
        L_0x1196:
            org.telegram.tgnet.TLRPC$Message r3 = r0.messageOwner
            if (r3 != 0) goto L_0x11a1
            org.telegram.tgnet.TLRPC$TL_messageService r3 = new org.telegram.tgnet.TLRPC$TL_messageService
            r3.<init>()
            r0.messageOwner = r3
        L_0x11a1:
            org.telegram.tgnet.TLRPC$Message r3 = r0.messageOwner
            java.lang.CharSequence r6 = r0.messageText
            java.lang.String r6 = r6.toString()
            r3.message = r6
            org.telegram.tgnet.TLRPC$Message r3 = r0.messageOwner
            org.telegram.tgnet.TLRPC$TL_peerUser r6 = new org.telegram.tgnet.TLRPC$TL_peerUser
            r6.<init>()
            r3.from_id = r6
            org.telegram.tgnet.TLRPC$Message r3 = r0.messageOwner
            org.telegram.tgnet.TLRPC$Peer r6 = r3.from_id
            int r8 = r1.user_id
            r6.user_id = r8
            int r6 = r1.date
            r3.date = r6
            r6 = 0
            r8 = r32[r6]
            int r9 = r8 + 1
            r32[r6] = r9
            r3.id = r8
            long r8 = r1.id
            r0.eventId = r8
            r3.out = r6
            org.telegram.tgnet.TLRPC$TL_peerChannel r8 = new org.telegram.tgnet.TLRPC$TL_peerChannel
            r8.<init>()
            r3.peer_id = r8
            org.telegram.tgnet.TLRPC$Message r3 = r0.messageOwner
            org.telegram.tgnet.TLRPC$Peer r8 = r3.peer_id
            int r4 = r4.id
            r8.channel_id = r4
            r3.unread = r6
            org.telegram.messenger.MediaController r3 = org.telegram.messenger.MediaController.getInstance()
            boolean r4 = r2 instanceof org.telegram.tgnet.TLRPC$TL_messageEmpty
            if (r4 == 0) goto L_0x11e9
            r2 = 0
        L_0x11e9:
            if (r2 == 0) goto L_0x124e
            r2.out = r6
            r4 = r32[r6]
            int r8 = r4 + 1
            r32[r6] = r8
            r2.id = r4
            int r4 = r2.flags
            r4 = r4 & -9
            r2.flags = r4
            r6 = 0
            r2.reply_to = r6
            r8 = -32769(0xffffffffffff7fff, float:NaN)
            r4 = r4 & r8
            r2.flags = r4
            org.telegram.messenger.MessageObject r4 = new org.telegram.messenger.MessageObject
            int r8 = r0.currentAccount
            r19 = 0
            r20 = 0
            r21 = 1
            r22 = 1
            long r9 = r0.eventId
            r16 = r4
            r17 = r8
            r18 = r2
            r23 = r9
            r16.<init>(r17, r18, r19, r20, r21, r22, r23)
            int r2 = r4.contentType
            if (r2 < 0) goto L_0x1246
            boolean r2 = r3.isPlayingMessage(r4)
            if (r2 == 0) goto L_0x1233
            org.telegram.messenger.MessageObject r2 = r3.getPlayingMessageObject()
            float r8 = r2.audioProgress
            r4.audioProgress = r8
            int r2 = r2.audioProgressSec
            r4.audioProgressSec = r2
        L_0x1233:
            int r2 = r0.currentAccount
            r8 = r29
            r9 = r30
            r0.createDateArray(r2, r1, r8, r9)
            int r2 = r29.size()
            r10 = 1
            int r2 = r2 - r10
            r8.add(r2, r4)
            goto L_0x1253
        L_0x1246:
            r8 = r29
            r9 = r30
            r2 = -1
            r0.contentType = r2
            goto L_0x1253
        L_0x124e:
            r8 = r29
            r9 = r30
            r6 = 0
        L_0x1253:
            int r2 = r0.contentType
            if (r2 < 0) goto L_0x12c1
            int r2 = r0.currentAccount
            r0.createDateArray(r2, r1, r8, r9)
            int r1 = r29.size()
            r2 = 1
            int r1 = r1 - r2
            r8.add(r1, r0)
            java.lang.CharSequence r1 = r0.messageText
            if (r1 != 0) goto L_0x126b
            r0.messageText = r7
        L_0x126b:
            r26.setType()
            r26.measureInlineBotButtons()
            r26.generateCaption()
            org.telegram.tgnet.TLRPC$Message r1 = r0.messageOwner
            org.telegram.tgnet.TLRPC$MessageMedia r1 = r1.media
            boolean r1 = r1 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaGame
            if (r1 == 0) goto L_0x127f
            android.text.TextPaint r1 = org.telegram.ui.ActionBar.Theme.chat_msgGameTextPaint
            goto L_0x1281
        L_0x127f:
            android.text.TextPaint r1 = org.telegram.ui.ActionBar.Theme.chat_msgTextPaint
        L_0x1281:
            boolean r2 = r26.allowsBigEmoji()
            if (r2 == 0) goto L_0x128b
            r2 = 1
            int[] r7 = new int[r2]
            goto L_0x128c
        L_0x128b:
            r7 = r6
        L_0x128c:
            java.lang.CharSequence r2 = r0.messageText
            android.graphics.Paint$FontMetricsInt r1 = r1.getFontMetricsInt()
            r4 = 1101004800(0x41a00000, float:20.0)
            int r4 = org.telegram.messenger.AndroidUtilities.dp(r4)
            r6 = 0
            java.lang.CharSequence r1 = org.telegram.messenger.Emoji.replaceEmoji(r2, r1, r4, r6, r7)
            r0.messageText = r1
            r0.checkEmojiOnly(r7)
            boolean r1 = r3.isPlayingMessage(r0)
            if (r1 == 0) goto L_0x12b4
            org.telegram.messenger.MessageObject r1 = r3.getPlayingMessageObject()
            float r2 = r1.audioProgress
            r0.audioProgress = r2
            int r1 = r1.audioProgressSec
            r0.audioProgressSec = r1
        L_0x12b4:
            r0.generateLayout(r5)
            r1 = 1
            r0.layoutCreated = r1
            r1 = 0
            r0.generateThumbs(r1)
            r26.checkMediaExistance()
        L_0x12c1:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.messenger.MessageObject.<init>(int, org.telegram.tgnet.TLRPC$TL_channelAdminLogEvent, java.util.ArrayList, java.util.HashMap, org.telegram.tgnet.TLRPC$Chat, int[]):void");
    }

    private String getUserName(TLRPC$User tLRPC$User, ArrayList<TLRPC$MessageEntity> arrayList, int i) {
        String formatName = tLRPC$User == null ? "" : ContactsController.formatName(tLRPC$User.first_name, tLRPC$User.last_name);
        if (i >= 0) {
            TLRPC$TL_messageEntityMentionName tLRPC$TL_messageEntityMentionName = new TLRPC$TL_messageEntityMentionName();
            tLRPC$TL_messageEntityMentionName.user_id = tLRPC$User.id;
            tLRPC$TL_messageEntityMentionName.offset = i;
            tLRPC$TL_messageEntityMentionName.length = formatName.length();
            arrayList.add(tLRPC$TL_messageEntityMentionName);
        }
        if (TextUtils.isEmpty(tLRPC$User.username)) {
            return formatName;
        }
        if (i >= 0) {
            TLRPC$TL_messageEntityMentionName tLRPC$TL_messageEntityMentionName2 = new TLRPC$TL_messageEntityMentionName();
            tLRPC$TL_messageEntityMentionName2.user_id = tLRPC$User.id;
            tLRPC$TL_messageEntityMentionName2.offset = i + formatName.length() + 2;
            tLRPC$TL_messageEntityMentionName2.length = tLRPC$User.username.length() + 1;
            arrayList.add(tLRPC$TL_messageEntityMentionName2);
        }
        return String.format("%1$s (@%2$s)", new Object[]{formatName, tLRPC$User.username});
    }

    public void applyNewText() {
        TextPaint textPaint;
        if (!TextUtils.isEmpty(this.messageOwner.message)) {
            int[] iArr = null;
            TLRPC$User user = isFromUser() ? MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(this.messageOwner.from_id.user_id)) : null;
            TLRPC$Message tLRPC$Message = this.messageOwner;
            this.messageText = tLRPC$Message.message;
            if (tLRPC$Message.media instanceof TLRPC$TL_messageMediaGame) {
                textPaint = Theme.chat_msgGameTextPaint;
            } else {
                textPaint = Theme.chat_msgTextPaint;
            }
            if (allowsBigEmoji()) {
                iArr = new int[1];
            }
            this.messageText = Emoji.replaceEmoji(this.messageText, textPaint.getFontMetricsInt(), AndroidUtilities.dp(20.0f), false, iArr);
            checkEmojiOnly(iArr);
            generateLayout(user);
        }
    }

    private boolean allowsBigEmoji() {
        TLRPC$Peer tLRPC$Peer;
        if (!SharedConfig.allowBigEmoji) {
            return false;
        }
        TLRPC$Message tLRPC$Message = this.messageOwner;
        if (tLRPC$Message == null || (tLRPC$Peer = tLRPC$Message.peer_id) == null || (tLRPC$Peer.channel_id == 0 && tLRPC$Peer.chat_id == 0)) {
            return true;
        }
        MessagesController instance = MessagesController.getInstance(this.currentAccount);
        TLRPC$Peer tLRPC$Peer2 = this.messageOwner.peer_id;
        int i = tLRPC$Peer2.channel_id;
        if (i == 0) {
            i = tLRPC$Peer2.chat_id;
        }
        return !ChatObject.isActionBanned(instance.getChat(Integer.valueOf(i)), 8);
    }

    public void generateGameMessageText(TLRPC$User tLRPC$User) {
        TLRPC$MessageMedia tLRPC$MessageMedia;
        TLRPC$TL_game tLRPC$TL_game;
        if (tLRPC$User == null && isFromUser()) {
            tLRPC$User = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(this.messageOwner.from_id.user_id));
        }
        TLRPC$TL_game tLRPC$TL_game2 = null;
        MessageObject messageObject = this.replyMessageObject;
        if (!(messageObject == null || (tLRPC$MessageMedia = messageObject.messageOwner.media) == null || (tLRPC$TL_game = tLRPC$MessageMedia.game) == null)) {
            tLRPC$TL_game2 = tLRPC$TL_game;
        }
        if (tLRPC$TL_game2 != null) {
            if (tLRPC$User == null || tLRPC$User.id != UserConfig.getInstance(this.currentAccount).getClientUserId()) {
                this.messageText = replaceWithLink(LocaleController.formatString("ActionUserScoredInGame", NUM, LocaleController.formatPluralString("Points", this.messageOwner.action.score)), "un1", tLRPC$User);
            } else {
                this.messageText = LocaleController.formatString("ActionYouScoredInGame", NUM, LocaleController.formatPluralString("Points", this.messageOwner.action.score));
            }
            this.messageText = replaceWithLink(this.messageText, "un2", tLRPC$TL_game2);
        } else if (tLRPC$User == null || tLRPC$User.id != UserConfig.getInstance(this.currentAccount).getClientUserId()) {
            this.messageText = replaceWithLink(LocaleController.formatString("ActionUserScored", NUM, LocaleController.formatPluralString("Points", this.messageOwner.action.score)), "un1", tLRPC$User);
        } else {
            this.messageText = LocaleController.formatString("ActionYouScored", NUM, LocaleController.formatPluralString("Points", this.messageOwner.action.score));
        }
    }

    public boolean hasValidReplyMessageObject() {
        MessageObject messageObject = this.replyMessageObject;
        if (messageObject != null) {
            TLRPC$Message tLRPC$Message = messageObject.messageOwner;
            return !(tLRPC$Message instanceof TLRPC$TL_messageEmpty) && !(tLRPC$Message.action instanceof TLRPC$TL_messageActionHistoryClear);
        }
    }

    public void generatePaymentSentMessageText(TLRPC$User tLRPC$User) {
        if (tLRPC$User == null) {
            tLRPC$User = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf((int) getDialogId()));
        }
        String firstName = tLRPC$User != null ? UserObject.getFirstName(tLRPC$User) : "";
        MessageObject messageObject = this.replyMessageObject;
        if (messageObject == null || !(messageObject.messageOwner.media instanceof TLRPC$TL_messageMediaInvoice)) {
            LocaleController instance = LocaleController.getInstance();
            TLRPC$MessageAction tLRPC$MessageAction = this.messageOwner.action;
            this.messageText = LocaleController.formatString("PaymentSuccessfullyPaidNoItem", NUM, instance.formatCurrencyString(tLRPC$MessageAction.total_amount, tLRPC$MessageAction.currency), firstName);
            return;
        }
        LocaleController instance2 = LocaleController.getInstance();
        TLRPC$MessageAction tLRPC$MessageAction2 = this.messageOwner.action;
        this.messageText = LocaleController.formatString("PaymentSuccessfullyPaid", NUM, instance2.formatCurrencyString(tLRPC$MessageAction2.total_amount, tLRPC$MessageAction2.currency), firstName, this.replyMessageObject.messageOwner.media.title);
    }

    /* JADX WARNING: Failed to insert additional move for type inference */
    /* JADX WARNING: Incorrect type for immutable var: ssa=org.telegram.tgnet.TLRPC$User, code=org.telegram.tgnet.TLRPC$Chat, for r9v0, types: [org.telegram.tgnet.TLRPC$User] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void generatePinMessageText(org.telegram.tgnet.TLRPC$Chat r9, org.telegram.tgnet.TLRPC$Chat r10) {
        /*
            r8 = this;
            if (r9 != 0) goto L_0x0055
            if (r10 != 0) goto L_0x0055
            boolean r0 = r8.isFromUser()
            if (r0 == 0) goto L_0x001e
            int r9 = r8.currentAccount
            org.telegram.messenger.MessagesController r9 = org.telegram.messenger.MessagesController.getInstance(r9)
            org.telegram.tgnet.TLRPC$Message r0 = r8.messageOwner
            org.telegram.tgnet.TLRPC$Peer r0 = r0.from_id
            int r0 = r0.user_id
            java.lang.Integer r0 = java.lang.Integer.valueOf(r0)
            org.telegram.tgnet.TLRPC$User r9 = r9.getUser(r0)
        L_0x001e:
            if (r9 != 0) goto L_0x0055
            org.telegram.tgnet.TLRPC$Message r0 = r8.messageOwner
            org.telegram.tgnet.TLRPC$Peer r0 = r0.peer_id
            boolean r1 = r0 instanceof org.telegram.tgnet.TLRPC$TL_peerChannel
            if (r1 == 0) goto L_0x003d
            int r10 = r8.currentAccount
            org.telegram.messenger.MessagesController r10 = org.telegram.messenger.MessagesController.getInstance(r10)
            org.telegram.tgnet.TLRPC$Message r0 = r8.messageOwner
            org.telegram.tgnet.TLRPC$Peer r0 = r0.peer_id
            int r0 = r0.channel_id
            java.lang.Integer r0 = java.lang.Integer.valueOf(r0)
            org.telegram.tgnet.TLRPC$Chat r10 = r10.getChat(r0)
            goto L_0x0055
        L_0x003d:
            boolean r0 = r0 instanceof org.telegram.tgnet.TLRPC$TL_peerChat
            if (r0 == 0) goto L_0x0055
            int r10 = r8.currentAccount
            org.telegram.messenger.MessagesController r10 = org.telegram.messenger.MessagesController.getInstance(r10)
            org.telegram.tgnet.TLRPC$Message r0 = r8.messageOwner
            org.telegram.tgnet.TLRPC$Peer r0 = r0.peer_id
            int r0 = r0.chat_id
            java.lang.Integer r0 = java.lang.Integer.valueOf(r0)
            org.telegram.tgnet.TLRPC$Chat r10 = r10.getChat(r0)
        L_0x0055:
            org.telegram.messenger.MessageObject r0 = r8.replyMessageObject
            r1 = 2131624139(0x7f0e00cb, float:1.887545E38)
            java.lang.String r2 = "ActionPinnedNoText"
            java.lang.String r3 = "un1"
            if (r0 == 0) goto L_0x0289
            org.telegram.tgnet.TLRPC$Message r4 = r0.messageOwner
            boolean r5 = r4 instanceof org.telegram.tgnet.TLRPC$TL_messageEmpty
            if (r5 != 0) goto L_0x0289
            org.telegram.tgnet.TLRPC$MessageAction r4 = r4.action
            boolean r4 = r4 instanceof org.telegram.tgnet.TLRPC$TL_messageActionHistoryClear
            if (r4 == 0) goto L_0x006e
            goto L_0x0289
        L_0x006e:
            boolean r0 = r0.isMusic()
            if (r0 == 0) goto L_0x0089
            r0 = 2131624138(0x7f0e00ca, float:1.8875447E38)
            java.lang.String r1 = "ActionPinnedMusic"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r1, r0)
            if (r9 == 0) goto L_0x0080
            goto L_0x0081
        L_0x0080:
            r9 = r10
        L_0x0081:
            java.lang.CharSequence r9 = replaceWithLink(r0, r3, r9)
            r8.messageText = r9
            goto L_0x0297
        L_0x0089:
            org.telegram.messenger.MessageObject r0 = r8.replyMessageObject
            boolean r0 = r0.isVideo()
            if (r0 == 0) goto L_0x00a6
            r0 = 2131624146(0x7f0e00d2, float:1.8875463E38)
            java.lang.String r1 = "ActionPinnedVideo"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r1, r0)
            if (r9 == 0) goto L_0x009d
            goto L_0x009e
        L_0x009d:
            r9 = r10
        L_0x009e:
            java.lang.CharSequence r9 = replaceWithLink(r0, r3, r9)
            r8.messageText = r9
            goto L_0x0297
        L_0x00a6:
            org.telegram.messenger.MessageObject r0 = r8.replyMessageObject
            boolean r0 = r0.isGif()
            if (r0 == 0) goto L_0x00c3
            r0 = 2131624137(0x7f0e00c9, float:1.8875445E38)
            java.lang.String r1 = "ActionPinnedGif"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r1, r0)
            if (r9 == 0) goto L_0x00ba
            goto L_0x00bb
        L_0x00ba:
            r9 = r10
        L_0x00bb:
            java.lang.CharSequence r9 = replaceWithLink(r0, r3, r9)
            r8.messageText = r9
            goto L_0x0297
        L_0x00c3:
            org.telegram.messenger.MessageObject r0 = r8.replyMessageObject
            boolean r0 = r0.isVoice()
            if (r0 == 0) goto L_0x00e0
            r0 = 2131624147(0x7f0e00d3, float:1.8875465E38)
            java.lang.String r1 = "ActionPinnedVoice"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r1, r0)
            if (r9 == 0) goto L_0x00d7
            goto L_0x00d8
        L_0x00d7:
            r9 = r10
        L_0x00d8:
            java.lang.CharSequence r9 = replaceWithLink(r0, r3, r9)
            r8.messageText = r9
            goto L_0x0297
        L_0x00e0:
            org.telegram.messenger.MessageObject r0 = r8.replyMessageObject
            boolean r0 = r0.isRoundVideo()
            if (r0 == 0) goto L_0x00fd
            r0 = 2131624143(0x7f0e00cf, float:1.8875457E38)
            java.lang.String r1 = "ActionPinnedRound"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r1, r0)
            if (r9 == 0) goto L_0x00f4
            goto L_0x00f5
        L_0x00f4:
            r9 = r10
        L_0x00f5:
            java.lang.CharSequence r9 = replaceWithLink(r0, r3, r9)
            r8.messageText = r9
            goto L_0x0297
        L_0x00fd:
            org.telegram.messenger.MessageObject r0 = r8.replyMessageObject
            boolean r0 = r0.isSticker()
            if (r0 != 0) goto L_0x0275
            org.telegram.messenger.MessageObject r0 = r8.replyMessageObject
            boolean r0 = r0.isAnimatedSticker()
            if (r0 == 0) goto L_0x010f
            goto L_0x0275
        L_0x010f:
            org.telegram.messenger.MessageObject r0 = r8.replyMessageObject
            org.telegram.tgnet.TLRPC$Message r4 = r0.messageOwner
            org.telegram.tgnet.TLRPC$MessageMedia r4 = r4.media
            boolean r5 = r4 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaDocument
            if (r5 == 0) goto L_0x012e
            r0 = 2131624133(0x7f0e00c5, float:1.8875437E38)
            java.lang.String r1 = "ActionPinnedFile"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r1, r0)
            if (r9 == 0) goto L_0x0125
            goto L_0x0126
        L_0x0125:
            r9 = r10
        L_0x0126:
            java.lang.CharSequence r9 = replaceWithLink(r0, r3, r9)
            r8.messageText = r9
            goto L_0x0297
        L_0x012e:
            boolean r5 = r4 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaGeo
            if (r5 == 0) goto L_0x0147
            r0 = 2131624135(0x7f0e00c7, float:1.8875441E38)
            java.lang.String r1 = "ActionPinnedGeo"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r1, r0)
            if (r9 == 0) goto L_0x013e
            goto L_0x013f
        L_0x013e:
            r9 = r10
        L_0x013f:
            java.lang.CharSequence r9 = replaceWithLink(r0, r3, r9)
            r8.messageText = r9
            goto L_0x0297
        L_0x0147:
            boolean r5 = r4 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaGeoLive
            if (r5 == 0) goto L_0x0160
            r0 = 2131624136(0x7f0e00c8, float:1.8875443E38)
            java.lang.String r1 = "ActionPinnedGeoLive"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r1, r0)
            if (r9 == 0) goto L_0x0157
            goto L_0x0158
        L_0x0157:
            r9 = r10
        L_0x0158:
            java.lang.CharSequence r9 = replaceWithLink(r0, r3, r9)
            r8.messageText = r9
            goto L_0x0297
        L_0x0160:
            boolean r5 = r4 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaContact
            if (r5 == 0) goto L_0x0179
            r0 = 2131624132(0x7f0e00c4, float:1.8875435E38)
            java.lang.String r1 = "ActionPinnedContact"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r1, r0)
            if (r9 == 0) goto L_0x0170
            goto L_0x0171
        L_0x0170:
            r9 = r10
        L_0x0171:
            java.lang.CharSequence r9 = replaceWithLink(r0, r3, r9)
            r8.messageText = r9
            goto L_0x0297
        L_0x0179:
            boolean r5 = r4 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaPoll
            if (r5 == 0) goto L_0x01af
            org.telegram.tgnet.TLRPC$TL_messageMediaPoll r4 = (org.telegram.tgnet.TLRPC$TL_messageMediaPoll) r4
            org.telegram.tgnet.TLRPC$Poll r0 = r4.poll
            boolean r0 = r0.quiz
            if (r0 == 0) goto L_0x019a
            r0 = 2131624142(0x7f0e00ce, float:1.8875455E38)
            java.lang.String r1 = "ActionPinnedQuiz"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r1, r0)
            if (r9 == 0) goto L_0x0191
            goto L_0x0192
        L_0x0191:
            r9 = r10
        L_0x0192:
            java.lang.CharSequence r9 = replaceWithLink(r0, r3, r9)
            r8.messageText = r9
            goto L_0x0297
        L_0x019a:
            r0 = 2131624141(0x7f0e00cd, float:1.8875453E38)
            java.lang.String r1 = "ActionPinnedPoll"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r1, r0)
            if (r9 == 0) goto L_0x01a6
            goto L_0x01a7
        L_0x01a6:
            r9 = r10
        L_0x01a7:
            java.lang.CharSequence r9 = replaceWithLink(r0, r3, r9)
            r8.messageText = r9
            goto L_0x0297
        L_0x01af:
            boolean r5 = r4 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaPhoto
            if (r5 == 0) goto L_0x01c8
            r0 = 2131624140(0x7f0e00cc, float:1.8875451E38)
            java.lang.String r1 = "ActionPinnedPhoto"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r1, r0)
            if (r9 == 0) goto L_0x01bf
            goto L_0x01c0
        L_0x01bf:
            r9 = r10
        L_0x01c0:
            java.lang.CharSequence r9 = replaceWithLink(r0, r3, r9)
            r8.messageText = r9
            goto L_0x0297
        L_0x01c8:
            boolean r4 = r4 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaGame
            r5 = 1101004800(0x41a00000, float:20.0)
            r6 = 1
            r7 = 0
            if (r4 == 0) goto L_0x0215
            r0 = 2131624134(0x7f0e00c6, float:1.887544E38)
            java.lang.Object[] r1 = new java.lang.Object[r6]
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r4 = "🎮 "
            r2.append(r4)
            org.telegram.messenger.MessageObject r4 = r8.replyMessageObject
            org.telegram.tgnet.TLRPC$Message r4 = r4.messageOwner
            org.telegram.tgnet.TLRPC$MessageMedia r4 = r4.media
            org.telegram.tgnet.TLRPC$TL_game r4 = r4.game
            java.lang.String r4 = r4.title
            r2.append(r4)
            java.lang.String r2 = r2.toString()
            r1[r7] = r2
            java.lang.String r2 = "ActionPinnedGame"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            if (r9 == 0) goto L_0x01fc
            goto L_0x01fd
        L_0x01fc:
            r9 = r10
        L_0x01fd:
            java.lang.CharSequence r9 = replaceWithLink(r0, r3, r9)
            r8.messageText = r9
            android.text.TextPaint r10 = org.telegram.ui.ActionBar.Theme.chat_msgTextPaint
            android.graphics.Paint$FontMetricsInt r10 = r10.getFontMetricsInt()
            int r0 = org.telegram.messenger.AndroidUtilities.dp(r5)
            java.lang.CharSequence r9 = org.telegram.messenger.Emoji.replaceEmoji(r9, r10, r0, r7)
            r8.messageText = r9
            goto L_0x0297
        L_0x0215:
            java.lang.CharSequence r0 = r0.messageText
            if (r0 == 0) goto L_0x0266
            int r0 = r0.length()
            if (r0 <= 0) goto L_0x0266
            org.telegram.messenger.MessageObject r0 = r8.replyMessageObject
            java.lang.CharSequence r0 = r0.messageText
            int r1 = r0.length()
            r2 = 20
            if (r1 <= r2) goto L_0x0240
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.CharSequence r0 = r0.subSequence(r7, r2)
            r1.append(r0)
            java.lang.String r0 = "..."
            r1.append(r0)
            java.lang.String r0 = r1.toString()
        L_0x0240:
            android.text.TextPaint r1 = org.telegram.ui.ActionBar.Theme.chat_msgTextPaint
            android.graphics.Paint$FontMetricsInt r1 = r1.getFontMetricsInt()
            int r2 = org.telegram.messenger.AndroidUtilities.dp(r5)
            java.lang.CharSequence r0 = org.telegram.messenger.Emoji.replaceEmoji(r0, r1, r2, r7)
            r1 = 2131624145(0x7f0e00d1, float:1.8875461E38)
            java.lang.Object[] r2 = new java.lang.Object[r6]
            r2[r7] = r0
            java.lang.String r0 = "ActionPinnedText"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r0, r1, r2)
            if (r9 == 0) goto L_0x025e
            goto L_0x025f
        L_0x025e:
            r9 = r10
        L_0x025f:
            java.lang.CharSequence r9 = replaceWithLink(r0, r3, r9)
            r8.messageText = r9
            goto L_0x0297
        L_0x0266:
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r2, r1)
            if (r9 == 0) goto L_0x026d
            goto L_0x026e
        L_0x026d:
            r9 = r10
        L_0x026e:
            java.lang.CharSequence r9 = replaceWithLink(r0, r3, r9)
            r8.messageText = r9
            goto L_0x0297
        L_0x0275:
            r0 = 2131624144(0x7f0e00d0, float:1.887546E38)
            java.lang.String r1 = "ActionPinnedSticker"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r1, r0)
            if (r9 == 0) goto L_0x0281
            goto L_0x0282
        L_0x0281:
            r9 = r10
        L_0x0282:
            java.lang.CharSequence r9 = replaceWithLink(r0, r3, r9)
            r8.messageText = r9
            goto L_0x0297
        L_0x0289:
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r2, r1)
            if (r9 == 0) goto L_0x0290
            goto L_0x0291
        L_0x0290:
            r9 = r10
        L_0x0291:
            java.lang.CharSequence r9 = replaceWithLink(r0, r3, r9)
            r8.messageText = r9
        L_0x0297:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.messenger.MessageObject.generatePinMessageText(org.telegram.tgnet.TLRPC$User, org.telegram.tgnet.TLRPC$Chat):void");
    }

    public static void updateReactions(TLRPC$Message tLRPC$Message, TLRPC$TL_messageReactions tLRPC$TL_messageReactions) {
        TLRPC$TL_messageReactions tLRPC$TL_messageReactions2;
        if (tLRPC$Message != null && tLRPC$TL_messageReactions != null) {
            if (tLRPC$TL_messageReactions.min && (tLRPC$TL_messageReactions2 = tLRPC$Message.reactions) != null) {
                int size = tLRPC$TL_messageReactions2.results.size();
                int i = 0;
                int i2 = 0;
                while (true) {
                    if (i2 >= size) {
                        break;
                    }
                    TLRPC$TL_reactionCount tLRPC$TL_reactionCount = tLRPC$Message.reactions.results.get(i2);
                    if (tLRPC$TL_reactionCount.chosen) {
                        int size2 = tLRPC$TL_messageReactions.results.size();
                        while (true) {
                            if (i >= size2) {
                                break;
                            }
                            TLRPC$TL_reactionCount tLRPC$TL_reactionCount2 = tLRPC$TL_messageReactions.results.get(i);
                            if (tLRPC$TL_reactionCount.reaction.equals(tLRPC$TL_reactionCount2.reaction)) {
                                tLRPC$TL_reactionCount2.chosen = true;
                                break;
                            }
                            i++;
                        }
                    } else {
                        i2++;
                    }
                }
            }
            tLRPC$Message.reactions = tLRPC$TL_messageReactions;
            tLRPC$Message.flags |= 1048576;
        }
    }

    public boolean hasReactions() {
        TLRPC$TL_messageReactions tLRPC$TL_messageReactions = this.messageOwner.reactions;
        return tLRPC$TL_messageReactions != null && !tLRPC$TL_messageReactions.results.isEmpty();
    }

    public static void updatePollResults(TLRPC$TL_messageMediaPoll tLRPC$TL_messageMediaPoll, TLRPC$PollResults tLRPC$PollResults) {
        byte[] bArr;
        ArrayList arrayList;
        ArrayList<TLRPC$TL_pollAnswerVoters> arrayList2;
        if (tLRPC$TL_messageMediaPoll != null && tLRPC$PollResults != null) {
            if ((tLRPC$PollResults.flags & 2) != 0) {
                if (!tLRPC$PollResults.min || (arrayList2 = tLRPC$TL_messageMediaPoll.results.results) == null) {
                    arrayList = null;
                    bArr = null;
                } else {
                    int size = arrayList2.size();
                    arrayList = null;
                    bArr = null;
                    for (int i = 0; i < size; i++) {
                        TLRPC$TL_pollAnswerVoters tLRPC$TL_pollAnswerVoters = tLRPC$TL_messageMediaPoll.results.results.get(i);
                        if (tLRPC$TL_pollAnswerVoters.chosen) {
                            if (arrayList == null) {
                                arrayList = new ArrayList();
                            }
                            arrayList.add(tLRPC$TL_pollAnswerVoters.option);
                        }
                        if (tLRPC$TL_pollAnswerVoters.correct) {
                            bArr = tLRPC$TL_pollAnswerVoters.option;
                        }
                    }
                }
                TLRPC$PollResults tLRPC$PollResults2 = tLRPC$TL_messageMediaPoll.results;
                ArrayList<TLRPC$TL_pollAnswerVoters> arrayList3 = tLRPC$PollResults.results;
                tLRPC$PollResults2.results = arrayList3;
                if (arrayList != null || bArr != null) {
                    int size2 = arrayList3.size();
                    for (int i2 = 0; i2 < size2; i2++) {
                        TLRPC$TL_pollAnswerVoters tLRPC$TL_pollAnswerVoters2 = tLRPC$TL_messageMediaPoll.results.results.get(i2);
                        if (arrayList != null) {
                            int size3 = arrayList.size();
                            int i3 = 0;
                            while (true) {
                                if (i3 >= size3) {
                                    break;
                                } else if (Arrays.equals(tLRPC$TL_pollAnswerVoters2.option, (byte[]) arrayList.get(i3))) {
                                    tLRPC$TL_pollAnswerVoters2.chosen = true;
                                    arrayList.remove(i3);
                                    break;
                                } else {
                                    i3++;
                                }
                            }
                            if (arrayList.isEmpty()) {
                                arrayList = null;
                            }
                        }
                        if (bArr != null && Arrays.equals(tLRPC$TL_pollAnswerVoters2.option, bArr)) {
                            tLRPC$TL_pollAnswerVoters2.correct = true;
                            bArr = null;
                        }
                        if (arrayList == null && bArr == null) {
                            break;
                        }
                    }
                }
                tLRPC$TL_messageMediaPoll.results.flags |= 2;
            }
            if ((tLRPC$PollResults.flags & 4) != 0) {
                TLRPC$PollResults tLRPC$PollResults3 = tLRPC$TL_messageMediaPoll.results;
                tLRPC$PollResults3.total_voters = tLRPC$PollResults.total_voters;
                tLRPC$PollResults3.flags |= 4;
            }
            if ((tLRPC$PollResults.flags & 8) != 0) {
                TLRPC$PollResults tLRPC$PollResults4 = tLRPC$TL_messageMediaPoll.results;
                tLRPC$PollResults4.recent_voters = tLRPC$PollResults.recent_voters;
                tLRPC$PollResults4.flags |= 8;
            }
            if ((tLRPC$PollResults.flags & 16) != 0) {
                TLRPC$PollResults tLRPC$PollResults5 = tLRPC$TL_messageMediaPoll.results;
                tLRPC$PollResults5.solution = tLRPC$PollResults.solution;
                tLRPC$PollResults5.solution_entities = tLRPC$PollResults.solution_entities;
                tLRPC$PollResults5.flags |= 16;
            }
        }
    }

    public boolean isPollClosed() {
        if (this.type != 17) {
            return false;
        }
        return ((TLRPC$TL_messageMediaPoll) this.messageOwner.media).poll.closed;
    }

    public boolean isQuiz() {
        if (this.type != 17) {
            return false;
        }
        return ((TLRPC$TL_messageMediaPoll) this.messageOwner.media).poll.quiz;
    }

    public boolean isPublicPoll() {
        if (this.type != 17) {
            return false;
        }
        return ((TLRPC$TL_messageMediaPoll) this.messageOwner.media).poll.public_voters;
    }

    public boolean isPoll() {
        return this.type == 17;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:3:0x0008, code lost:
        r0 = (org.telegram.tgnet.TLRPC$TL_messageMediaPoll) r5.messageOwner.media;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean canUnvote() {
        /*
            r5 = this;
            int r0 = r5.type
            r1 = 0
            r2 = 17
            if (r0 == r2) goto L_0x0008
            return r1
        L_0x0008:
            org.telegram.tgnet.TLRPC$Message r0 = r5.messageOwner
            org.telegram.tgnet.TLRPC$MessageMedia r0 = r0.media
            org.telegram.tgnet.TLRPC$TL_messageMediaPoll r0 = (org.telegram.tgnet.TLRPC$TL_messageMediaPoll) r0
            org.telegram.tgnet.TLRPC$PollResults r2 = r0.results
            if (r2 == 0) goto L_0x003f
            java.util.ArrayList<org.telegram.tgnet.TLRPC$TL_pollAnswerVoters> r2 = r2.results
            boolean r2 = r2.isEmpty()
            if (r2 != 0) goto L_0x003f
            org.telegram.tgnet.TLRPC$Poll r2 = r0.poll
            boolean r2 = r2.quiz
            if (r2 == 0) goto L_0x0021
            goto L_0x003f
        L_0x0021:
            org.telegram.tgnet.TLRPC$PollResults r2 = r0.results
            java.util.ArrayList<org.telegram.tgnet.TLRPC$TL_pollAnswerVoters> r2 = r2.results
            int r2 = r2.size()
            r3 = 0
        L_0x002a:
            if (r3 >= r2) goto L_0x003f
            org.telegram.tgnet.TLRPC$PollResults r4 = r0.results
            java.util.ArrayList<org.telegram.tgnet.TLRPC$TL_pollAnswerVoters> r4 = r4.results
            java.lang.Object r4 = r4.get(r3)
            org.telegram.tgnet.TLRPC$TL_pollAnswerVoters r4 = (org.telegram.tgnet.TLRPC$TL_pollAnswerVoters) r4
            boolean r4 = r4.chosen
            if (r4 == 0) goto L_0x003c
            r0 = 1
            return r0
        L_0x003c:
            int r3 = r3 + 1
            goto L_0x002a
        L_0x003f:
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.messenger.MessageObject.canUnvote():boolean");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:3:0x0008, code lost:
        r0 = (org.telegram.tgnet.TLRPC$TL_messageMediaPoll) r5.messageOwner.media;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean isVoted() {
        /*
            r5 = this;
            int r0 = r5.type
            r1 = 0
            r2 = 17
            if (r0 == r2) goto L_0x0008
            return r1
        L_0x0008:
            org.telegram.tgnet.TLRPC$Message r0 = r5.messageOwner
            org.telegram.tgnet.TLRPC$MessageMedia r0 = r0.media
            org.telegram.tgnet.TLRPC$TL_messageMediaPoll r0 = (org.telegram.tgnet.TLRPC$TL_messageMediaPoll) r0
            org.telegram.tgnet.TLRPC$PollResults r2 = r0.results
            if (r2 == 0) goto L_0x0039
            java.util.ArrayList<org.telegram.tgnet.TLRPC$TL_pollAnswerVoters> r2 = r2.results
            boolean r2 = r2.isEmpty()
            if (r2 == 0) goto L_0x001b
            goto L_0x0039
        L_0x001b:
            org.telegram.tgnet.TLRPC$PollResults r2 = r0.results
            java.util.ArrayList<org.telegram.tgnet.TLRPC$TL_pollAnswerVoters> r2 = r2.results
            int r2 = r2.size()
            r3 = 0
        L_0x0024:
            if (r3 >= r2) goto L_0x0039
            org.telegram.tgnet.TLRPC$PollResults r4 = r0.results
            java.util.ArrayList<org.telegram.tgnet.TLRPC$TL_pollAnswerVoters> r4 = r4.results
            java.lang.Object r4 = r4.get(r3)
            org.telegram.tgnet.TLRPC$TL_pollAnswerVoters r4 = (org.telegram.tgnet.TLRPC$TL_pollAnswerVoters) r4
            boolean r4 = r4.chosen
            if (r4 == 0) goto L_0x0036
            r0 = 1
            return r0
        L_0x0036:
            int r3 = r3 + 1
            goto L_0x0024
        L_0x0039:
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.messenger.MessageObject.isVoted():boolean");
    }

    public long getPollId() {
        if (this.type != 17) {
            return 0;
        }
        return ((TLRPC$TL_messageMediaPoll) this.messageOwner.media).poll.id;
    }

    private TLRPC$Photo getPhotoWithId(TLRPC$WebPage tLRPC$WebPage, long j) {
        if (!(tLRPC$WebPage == null || tLRPC$WebPage.cached_page == null)) {
            TLRPC$Photo tLRPC$Photo = tLRPC$WebPage.photo;
            if (tLRPC$Photo != null && tLRPC$Photo.id == j) {
                return tLRPC$Photo;
            }
            for (int i = 0; i < tLRPC$WebPage.cached_page.photos.size(); i++) {
                TLRPC$Photo tLRPC$Photo2 = tLRPC$WebPage.cached_page.photos.get(i);
                if (tLRPC$Photo2.id == j) {
                    return tLRPC$Photo2;
                }
            }
        }
        return null;
    }

    private TLRPC$Document getDocumentWithId(TLRPC$WebPage tLRPC$WebPage, long j) {
        if (!(tLRPC$WebPage == null || tLRPC$WebPage.cached_page == null)) {
            TLRPC$Document tLRPC$Document = tLRPC$WebPage.document;
            if (tLRPC$Document != null && tLRPC$Document.id == j) {
                return tLRPC$Document;
            }
            for (int i = 0; i < tLRPC$WebPage.cached_page.documents.size(); i++) {
                TLRPC$Document tLRPC$Document2 = tLRPC$WebPage.cached_page.documents.get(i);
                if (tLRPC$Document2.id == j) {
                    return tLRPC$Document2;
                }
            }
        }
        return null;
    }

    public boolean isSupergroup() {
        int i;
        if (this.localSupergroup) {
            return true;
        }
        Boolean bool = this.cachedIsSupergroup;
        if (bool != null) {
            return bool.booleanValue();
        }
        TLRPC$Peer tLRPC$Peer = this.messageOwner.peer_id;
        if (tLRPC$Peer == null || (i = tLRPC$Peer.channel_id) == 0) {
            this.cachedIsSupergroup = Boolean.FALSE;
            return false;
        }
        TLRPC$Chat chat = getChat((AbstractMap<Integer, TLRPC$Chat>) null, (SparseArray<TLRPC$Chat>) null, i);
        if (chat == null) {
            return false;
        }
        Boolean valueOf = Boolean.valueOf(chat.megagroup);
        this.cachedIsSupergroup = valueOf;
        return valueOf.booleanValue();
    }

    private MessageObject getMessageObjectForBlock(TLRPC$WebPage tLRPC$WebPage, TLRPC$PageBlock tLRPC$PageBlock) {
        TLRPC$TL_message tLRPC$TL_message;
        if (tLRPC$PageBlock instanceof TLRPC$TL_pageBlockPhoto) {
            TLRPC$Photo photoWithId = getPhotoWithId(tLRPC$WebPage, ((TLRPC$TL_pageBlockPhoto) tLRPC$PageBlock).photo_id);
            if (photoWithId == tLRPC$WebPage.photo) {
                return this;
            }
            tLRPC$TL_message = new TLRPC$TL_message();
            TLRPC$TL_messageMediaPhoto tLRPC$TL_messageMediaPhoto = new TLRPC$TL_messageMediaPhoto();
            tLRPC$TL_message.media = tLRPC$TL_messageMediaPhoto;
            tLRPC$TL_messageMediaPhoto.photo = photoWithId;
        } else if (tLRPC$PageBlock instanceof TLRPC$TL_pageBlockVideo) {
            TLRPC$TL_pageBlockVideo tLRPC$TL_pageBlockVideo = (TLRPC$TL_pageBlockVideo) tLRPC$PageBlock;
            if (getDocumentWithId(tLRPC$WebPage, tLRPC$TL_pageBlockVideo.video_id) == tLRPC$WebPage.document) {
                return this;
            }
            TLRPC$TL_message tLRPC$TL_message2 = new TLRPC$TL_message();
            TLRPC$TL_messageMediaDocument tLRPC$TL_messageMediaDocument = new TLRPC$TL_messageMediaDocument();
            tLRPC$TL_message2.media = tLRPC$TL_messageMediaDocument;
            tLRPC$TL_messageMediaDocument.document = getDocumentWithId(tLRPC$WebPage, tLRPC$TL_pageBlockVideo.video_id);
            tLRPC$TL_message = tLRPC$TL_message2;
        } else {
            tLRPC$TL_message = null;
        }
        tLRPC$TL_message.message = "";
        tLRPC$TL_message.realId = getId();
        tLRPC$TL_message.id = Utilities.random.nextInt();
        TLRPC$Message tLRPC$Message = this.messageOwner;
        tLRPC$TL_message.date = tLRPC$Message.date;
        tLRPC$TL_message.peer_id = tLRPC$Message.peer_id;
        tLRPC$TL_message.out = tLRPC$Message.out;
        tLRPC$TL_message.from_id = tLRPC$Message.from_id;
        return new MessageObject(this.currentAccount, tLRPC$TL_message, false, true);
    }

    public ArrayList<MessageObject> getWebPagePhotos(ArrayList<MessageObject> arrayList, ArrayList<TLRPC$PageBlock> arrayList2) {
        TLRPC$WebPage tLRPC$WebPage;
        TLRPC$Page tLRPC$Page;
        if (arrayList == null) {
            arrayList = new ArrayList<>();
        }
        TLRPC$MessageMedia tLRPC$MessageMedia = this.messageOwner.media;
        if (tLRPC$MessageMedia == null || (tLRPC$WebPage = tLRPC$MessageMedia.webpage) == null || (tLRPC$Page = tLRPC$WebPage.cached_page) == null) {
            return arrayList;
        }
        if (arrayList2 == null) {
            arrayList2 = tLRPC$Page.blocks;
        }
        for (int i = 0; i < arrayList2.size(); i++) {
            TLRPC$PageBlock tLRPC$PageBlock = arrayList2.get(i);
            if (tLRPC$PageBlock instanceof TLRPC$TL_pageBlockSlideshow) {
                TLRPC$TL_pageBlockSlideshow tLRPC$TL_pageBlockSlideshow = (TLRPC$TL_pageBlockSlideshow) tLRPC$PageBlock;
                for (int i2 = 0; i2 < tLRPC$TL_pageBlockSlideshow.items.size(); i2++) {
                    arrayList.add(getMessageObjectForBlock(tLRPC$WebPage, tLRPC$TL_pageBlockSlideshow.items.get(i2)));
                }
            } else if (tLRPC$PageBlock instanceof TLRPC$TL_pageBlockCollage) {
                TLRPC$TL_pageBlockCollage tLRPC$TL_pageBlockCollage = (TLRPC$TL_pageBlockCollage) tLRPC$PageBlock;
                for (int i3 = 0; i3 < tLRPC$TL_pageBlockCollage.items.size(); i3++) {
                    arrayList.add(getMessageObjectForBlock(tLRPC$WebPage, tLRPC$TL_pageBlockCollage.items.get(i3)));
                }
            }
        }
        return arrayList;
    }

    public void createMessageSendInfo() {
        HashMap<String, String> hashMap;
        String str;
        TLRPC$Message tLRPC$Message = this.messageOwner;
        if (tLRPC$Message.message == null) {
            return;
        }
        if ((tLRPC$Message.id < 0 || isEditing()) && (hashMap = this.messageOwner.params) != null) {
            String str2 = hashMap.get("ve");
            if (str2 != null && (isVideo() || isNewGif() || isRoundVideo())) {
                VideoEditedInfo videoEditedInfo2 = new VideoEditedInfo();
                this.videoEditedInfo = videoEditedInfo2;
                if (!videoEditedInfo2.parseString(str2)) {
                    this.videoEditedInfo = null;
                } else {
                    this.videoEditedInfo.roundVideo = isRoundVideo();
                }
            }
            TLRPC$Message tLRPC$Message2 = this.messageOwner;
            if (tLRPC$Message2.send_state == 3 && (str = tLRPC$Message2.params.get("prevMedia")) != null) {
                SerializedData serializedData = new SerializedData(Base64.decode(str, 0));
                this.previousMedia = TLRPC$MessageMedia.TLdeserialize(serializedData, serializedData.readInt32(false), false);
                this.previousMessage = serializedData.readString(false);
                this.previousAttachPath = serializedData.readString(false);
                int readInt32 = serializedData.readInt32(false);
                this.previousMessageEntities = new ArrayList<>(readInt32);
                for (int i = 0; i < readInt32; i++) {
                    this.previousMessageEntities.add(TLRPC$MessageEntity.TLdeserialize(serializedData, serializedData.readInt32(false), false));
                }
                serializedData.cleanup();
            }
        }
    }

    public void measureInlineBotButtons() {
        int i;
        CharSequence charSequence;
        TLRPC$TL_messageReactions tLRPC$TL_messageReactions;
        if (!this.isRestrictedMessage) {
            this.wantedBotKeyboardWidth = 0;
            TLRPC$Message tLRPC$Message = this.messageOwner;
            if ((tLRPC$Message.reply_markup instanceof TLRPC$TL_replyInlineMarkup) || ((tLRPC$TL_messageReactions = tLRPC$Message.reactions) != null && !tLRPC$TL_messageReactions.results.isEmpty())) {
                Theme.createChatResources((Context) null, true);
                StringBuilder sb = this.botButtonsLayout;
                if (sb == null) {
                    this.botButtonsLayout = new StringBuilder();
                } else {
                    sb.setLength(0);
                }
            }
            TLRPC$Message tLRPC$Message2 = this.messageOwner;
            if (tLRPC$Message2.reply_markup instanceof TLRPC$TL_replyInlineMarkup) {
                for (int i2 = 0; i2 < this.messageOwner.reply_markup.rows.size(); i2++) {
                    TLRPC$TL_keyboardButtonRow tLRPC$TL_keyboardButtonRow = this.messageOwner.reply_markup.rows.get(i2);
                    int size = tLRPC$TL_keyboardButtonRow.buttons.size();
                    int i3 = 0;
                    for (int i4 = 0; i4 < size; i4++) {
                        TLRPC$KeyboardButton tLRPC$KeyboardButton = tLRPC$TL_keyboardButtonRow.buttons.get(i4);
                        StringBuilder sb2 = this.botButtonsLayout;
                        sb2.append(i2);
                        sb2.append(i4);
                        if (!(tLRPC$KeyboardButton instanceof TLRPC$TL_keyboardButtonBuy) || (this.messageOwner.media.flags & 4) == 0) {
                            charSequence = Emoji.replaceEmoji(tLRPC$KeyboardButton.text, Theme.chat_msgBotButtonPaint.getFontMetricsInt(), AndroidUtilities.dp(15.0f), false);
                        } else {
                            charSequence = LocaleController.getString("PaymentReceipt", NUM);
                        }
                        StaticLayout staticLayout = new StaticLayout(charSequence, Theme.chat_msgBotButtonPaint, AndroidUtilities.dp(2000.0f), Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
                        if (staticLayout.getLineCount() > 0) {
                            float lineWidth = staticLayout.getLineWidth(0);
                            float lineLeft = staticLayout.getLineLeft(0);
                            if (lineLeft < lineWidth) {
                                lineWidth -= lineLeft;
                            }
                            i3 = Math.max(i3, ((int) Math.ceil((double) lineWidth)) + AndroidUtilities.dp(4.0f));
                        }
                    }
                    this.wantedBotKeyboardWidth = Math.max(this.wantedBotKeyboardWidth, ((i3 + AndroidUtilities.dp(12.0f)) * size) + (AndroidUtilities.dp(5.0f) * (size - 1)));
                }
                return;
            }
            TLRPC$TL_messageReactions tLRPC$TL_messageReactions2 = tLRPC$Message2.reactions;
            if (tLRPC$TL_messageReactions2 != null) {
                int size2 = tLRPC$TL_messageReactions2.results.size();
                for (int i5 = 0; i5 < size2; i5++) {
                    TLRPC$TL_reactionCount tLRPC$TL_reactionCount = this.messageOwner.reactions.results.get(i5);
                    StringBuilder sb3 = this.botButtonsLayout;
                    sb3.append(0);
                    sb3.append(i5);
                    StaticLayout staticLayout2 = new StaticLayout(Emoji.replaceEmoji(String.format("%d %s", new Object[]{Integer.valueOf(tLRPC$TL_reactionCount.count), tLRPC$TL_reactionCount.reaction}), Theme.chat_msgBotButtonPaint.getFontMetricsInt(), AndroidUtilities.dp(15.0f), false), Theme.chat_msgBotButtonPaint, AndroidUtilities.dp(2000.0f), Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
                    if (staticLayout2.getLineCount() > 0) {
                        float lineWidth2 = staticLayout2.getLineWidth(0);
                        float lineLeft2 = staticLayout2.getLineLeft(0);
                        if (lineLeft2 < lineWidth2) {
                            lineWidth2 -= lineLeft2;
                        }
                        i = Math.max(0, ((int) Math.ceil((double) lineWidth2)) + AndroidUtilities.dp(4.0f));
                    } else {
                        i = 0;
                    }
                    this.wantedBotKeyboardWidth = Math.max(this.wantedBotKeyboardWidth, ((i + AndroidUtilities.dp(12.0f)) * size2) + (AndroidUtilities.dp(5.0f) * (size2 - 1)));
                }
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:2:0x0006, code lost:
        r0 = r0.photo;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean isVideoAvatar() {
        /*
            r1 = this;
            org.telegram.tgnet.TLRPC$Message r0 = r1.messageOwner
            org.telegram.tgnet.TLRPC$MessageAction r0 = r0.action
            if (r0 == 0) goto L_0x0014
            org.telegram.tgnet.TLRPC$Photo r0 = r0.photo
            if (r0 == 0) goto L_0x0014
            java.util.ArrayList<org.telegram.tgnet.TLRPC$VideoSize> r0 = r0.video_sizes
            boolean r0 = r0.isEmpty()
            if (r0 != 0) goto L_0x0014
            r0 = 1
            goto L_0x0015
        L_0x0014:
            r0 = 0
        L_0x0015:
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.messenger.MessageObject.isVideoAvatar():boolean");
    }

    public boolean isFcmMessage() {
        return this.localType != 0;
    }

    private TLRPC$User getUser(AbstractMap<Integer, TLRPC$User> abstractMap, SparseArray<TLRPC$User> sparseArray, int i) {
        TLRPC$User tLRPC$User;
        if (abstractMap != null) {
            tLRPC$User = abstractMap.get(Integer.valueOf(i));
        } else {
            tLRPC$User = sparseArray != null ? sparseArray.get(i) : null;
        }
        return tLRPC$User == null ? MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(i)) : tLRPC$User;
    }

    private TLRPC$Chat getChat(AbstractMap<Integer, TLRPC$Chat> abstractMap, SparseArray<TLRPC$Chat> sparseArray, int i) {
        TLRPC$Chat tLRPC$Chat;
        if (abstractMap != null) {
            tLRPC$Chat = abstractMap.get(Integer.valueOf(i));
        } else {
            tLRPC$Chat = sparseArray != null ? sparseArray.get(i) : null;
        }
        return tLRPC$Chat == null ? MessagesController.getInstance(this.currentAccount).getChat(Integer.valueOf(i)) : tLRPC$Chat;
    }

    /* JADX WARNING: Removed duplicated region for block: B:10:0x002c  */
    /* JADX WARNING: Removed duplicated region for block: B:13:0x0038  */
    /* JADX WARNING: Removed duplicated region for block: B:438:0x0b87  */
    /* JADX WARNING: Removed duplicated region for block: B:527:0x0d79  */
    /* JADX WARNING: Removed duplicated region for block: B:544:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:9:0x002a  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void updateMessageText(java.util.AbstractMap<java.lang.Integer, org.telegram.tgnet.TLRPC$User> r17, java.util.AbstractMap<java.lang.Integer, org.telegram.tgnet.TLRPC$Chat> r18, android.util.SparseArray<org.telegram.tgnet.TLRPC$User> r19, android.util.SparseArray<org.telegram.tgnet.TLRPC$Chat> r20) {
        /*
            r16 = this;
            r6 = r16
            r4 = r17
            r0 = r18
            r5 = r19
            r1 = r20
            org.telegram.tgnet.TLRPC$Message r2 = r6.messageOwner
            org.telegram.tgnet.TLRPC$Peer r2 = r2.from_id
            boolean r3 = r2 instanceof org.telegram.tgnet.TLRPC$TL_peerUser
            if (r3 == 0) goto L_0x0019
            int r2 = r2.user_id
            org.telegram.tgnet.TLRPC$User r2 = r6.getUser(r4, r5, r2)
            goto L_0x0027
        L_0x0019:
            boolean r3 = r2 instanceof org.telegram.tgnet.TLRPC$TL_peerChannel
            if (r3 == 0) goto L_0x0026
            int r2 = r2.channel_id
            org.telegram.tgnet.TLRPC$Chat r2 = r6.getChat(r0, r1, r2)
            r3 = r2
            r2 = 0
            goto L_0x0028
        L_0x0026:
            r2 = 0
        L_0x0027:
            r3 = 0
        L_0x0028:
            if (r2 == 0) goto L_0x002c
            r8 = r2
            goto L_0x002d
        L_0x002c:
            r8 = r3
        L_0x002d:
            org.telegram.tgnet.TLRPC$Message r3 = r6.messageOwner
            boolean r9 = r3 instanceof org.telegram.tgnet.TLRPC$TL_messageService
            java.lang.String r10 = ""
            r11 = 2
            r12 = 1
            r13 = 0
            if (r9 == 0) goto L_0x0b87
            org.telegram.tgnet.TLRPC$MessageAction r9 = r3.action
            if (r9 == 0) goto L_0x0d75
            boolean r14 = r9 instanceof org.telegram.tgnet.TLRPC$TL_messageActionGroupCall
            java.lang.String r15 = "un1"
            if (r14 == 0) goto L_0x00a5
            int r0 = r9.duration
            if (r0 == 0) goto L_0x0081
            r1 = 86400(0x15180, float:1.21072E-40)
            int r1 = r0 / r1
            if (r1 <= 0) goto L_0x0054
            java.lang.String r0 = "Days"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatPluralString(r0, r1)
            goto L_0x0070
        L_0x0054:
            int r1 = r0 / 3600
            if (r1 <= 0) goto L_0x005f
            java.lang.String r0 = "Hours"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatPluralString(r0, r1)
            goto L_0x0070
        L_0x005f:
            int r1 = r0 / 60
            if (r1 <= 0) goto L_0x006a
            java.lang.String r0 = "Minutes"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatPluralString(r0, r1)
            goto L_0x0070
        L_0x006a:
            java.lang.String r1 = "Seconds"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatPluralString(r1, r0)
        L_0x0070:
            r1 = 2131624119(0x7f0e00b7, float:1.8875409E38)
            java.lang.Object[] r2 = new java.lang.Object[r12]
            r2[r13] = r0
            java.lang.String r0 = "ActionGroupCallEnded"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r0, r1, r2)
            r6.messageText = r0
            goto L_0x0d75
        L_0x0081:
            boolean r0 = r16.isOut()
            if (r0 == 0) goto L_0x0094
            r0 = 2131624123(0x7f0e00bb, float:1.8875417E38)
            java.lang.String r1 = "ActionGroupCallStartedByYou"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r1, r0)
            r6.messageText = r0
            goto L_0x0d75
        L_0x0094:
            r0 = 2131624122(0x7f0e00ba, float:1.8875415E38)
            java.lang.String r1 = "ActionGroupCallStarted"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r1, r0)
            java.lang.CharSequence r0 = replaceWithLink(r0, r15, r8)
            r6.messageText = r0
            goto L_0x0d75
        L_0x00a5:
            boolean r14 = r9 instanceof org.telegram.tgnet.TLRPC$TL_messageActionInviteToGroupCall
            java.lang.String r7 = "un2"
            if (r14 == 0) goto L_0x0158
            int r0 = r9.user_id
            if (r0 != 0) goto L_0x00c7
            java.util.ArrayList<java.lang.Integer> r1 = r9.users
            int r1 = r1.size()
            if (r1 != r12) goto L_0x00c7
            org.telegram.tgnet.TLRPC$Message r0 = r6.messageOwner
            org.telegram.tgnet.TLRPC$MessageAction r0 = r0.action
            java.util.ArrayList<java.lang.Integer> r0 = r0.users
            java.lang.Object r0 = r0.get(r13)
            java.lang.Integer r0 = (java.lang.Integer) r0
            int r0 = r0.intValue()
        L_0x00c7:
            r1 = 2131624124(0x7f0e00bc, float:1.8875419E38)
            java.lang.String r2 = "ActionGroupCallYouInvited"
            r3 = 2131624120(0x7f0e00b8, float:1.887541E38)
            java.lang.String r9 = "ActionGroupCallInvited"
            if (r0 == 0) goto L_0x0118
            org.telegram.tgnet.TLRPC$User r4 = r6.getUser(r4, r5, r0)
            boolean r5 = r16.isOut()
            if (r5 == 0) goto L_0x00e9
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r2, r1)
            java.lang.CharSequence r0 = replaceWithLink(r0, r7, r4)
            r6.messageText = r0
            goto L_0x0d75
        L_0x00e9:
            int r1 = r6.currentAccount
            org.telegram.messenger.UserConfig r1 = org.telegram.messenger.UserConfig.getInstance(r1)
            int r1 = r1.getClientUserId()
            if (r0 != r1) goto L_0x0106
            r0 = 2131624121(0x7f0e00b9, float:1.8875413E38)
            java.lang.String r1 = "ActionGroupCallInvitedYou"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r1, r0)
            java.lang.CharSequence r0 = replaceWithLink(r0, r15, r8)
            r6.messageText = r0
            goto L_0x0d75
        L_0x0106:
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r9, r3)
            java.lang.CharSequence r0 = replaceWithLink(r0, r7, r4)
            r6.messageText = r0
            java.lang.CharSequence r0 = replaceWithLink(r0, r15, r8)
            r6.messageText = r0
            goto L_0x0d75
        L_0x0118:
            boolean r0 = r16.isOut()
            if (r0 == 0) goto L_0x0138
            java.lang.String r1 = org.telegram.messenger.LocaleController.getString(r2, r1)
            org.telegram.tgnet.TLRPC$Message r0 = r6.messageOwner
            org.telegram.tgnet.TLRPC$MessageAction r0 = r0.action
            java.util.ArrayList<java.lang.Integer> r3 = r0.users
            java.lang.String r2 = "un2"
            r0 = r16
            r4 = r17
            r5 = r19
            java.lang.CharSequence r0 = r0.replaceWithLink(r1, r2, r3, r4, r5)
            r6.messageText = r0
            goto L_0x0d75
        L_0x0138:
            java.lang.String r1 = org.telegram.messenger.LocaleController.getString(r9, r3)
            org.telegram.tgnet.TLRPC$Message r0 = r6.messageOwner
            org.telegram.tgnet.TLRPC$MessageAction r0 = r0.action
            java.util.ArrayList<java.lang.Integer> r3 = r0.users
            java.lang.String r2 = "un2"
            r0 = r16
            r4 = r17
            r5 = r19
            java.lang.CharSequence r0 = r0.replaceWithLink(r1, r2, r3, r4, r5)
            r6.messageText = r0
            java.lang.CharSequence r0 = replaceWithLink(r0, r15, r8)
            r6.messageText = r0
            goto L_0x0d75
        L_0x0158:
            boolean r14 = r9 instanceof org.telegram.tgnet.TLRPC$TL_messageActionGeoProximityReached
            if (r14 == 0) goto L_0x01ea
            org.telegram.tgnet.TLRPC$TL_messageActionGeoProximityReached r9 = (org.telegram.tgnet.TLRPC$TL_messageActionGeoProximityReached) r9
            org.telegram.tgnet.TLRPC$Peer r2 = r9.from_id
            int r2 = getPeerId(r2)
            if (r2 <= 0) goto L_0x016b
            org.telegram.tgnet.TLRPC$User r3 = r6.getUser(r4, r5, r2)
            goto L_0x0170
        L_0x016b:
            int r3 = -r2
            org.telegram.tgnet.TLRPC$Chat r3 = r6.getChat(r0, r1, r3)
        L_0x0170:
            org.telegram.tgnet.TLRPC$Peer r8 = r9.to_id
            int r8 = getPeerId(r8)
            int r14 = r6.currentAccount
            org.telegram.messenger.UserConfig r14 = org.telegram.messenger.UserConfig.getInstance(r14)
            int r14 = r14.getClientUserId()
            if (r8 != r14) goto L_0x019e
            r0 = 2131624163(0x7f0e00e3, float:1.8875498E38)
            java.lang.Object[] r1 = new java.lang.Object[r12]
            int r2 = r9.distance
            float r2 = (float) r2
            java.lang.String r2 = org.telegram.messenger.LocaleController.formatDistance(r2, r11)
            r1[r13] = r2
            java.lang.String r2 = "ActionUserWithinRadius"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            java.lang.CharSequence r0 = replaceWithLink(r0, r15, r3)
            r6.messageText = r0
            goto L_0x0d75
        L_0x019e:
            if (r8 <= 0) goto L_0x01a5
            org.telegram.tgnet.TLRPC$User r0 = r6.getUser(r4, r5, r8)
            goto L_0x01aa
        L_0x01a5:
            int r4 = -r8
            org.telegram.tgnet.TLRPC$Chat r0 = r6.getChat(r0, r1, r4)
        L_0x01aa:
            if (r2 != r14) goto L_0x01c8
            r1 = 2131624164(0x7f0e00e4, float:1.88755E38)
            java.lang.Object[] r2 = new java.lang.Object[r12]
            int r3 = r9.distance
            float r3 = (float) r3
            java.lang.String r3 = org.telegram.messenger.LocaleController.formatDistance(r3, r11)
            r2[r13] = r3
            java.lang.String r3 = "ActionUserWithinYouRadius"
            java.lang.String r1 = org.telegram.messenger.LocaleController.formatString(r3, r1, r2)
            java.lang.CharSequence r0 = replaceWithLink(r1, r15, r0)
            r6.messageText = r0
            goto L_0x0d75
        L_0x01c8:
            r1 = 2131624162(0x7f0e00e2, float:1.8875496E38)
            java.lang.Object[] r2 = new java.lang.Object[r12]
            int r4 = r9.distance
            float r4 = (float) r4
            java.lang.String r4 = org.telegram.messenger.LocaleController.formatDistance(r4, r11)
            r2[r13] = r4
            java.lang.String r4 = "ActionUserWithinOtherRadius"
            java.lang.String r1 = org.telegram.messenger.LocaleController.formatString(r4, r1, r2)
            java.lang.CharSequence r0 = replaceWithLink(r1, r7, r0)
            r6.messageText = r0
            java.lang.CharSequence r0 = replaceWithLink(r0, r15, r3)
            r6.messageText = r0
            goto L_0x0d75
        L_0x01ea:
            boolean r14 = r9 instanceof org.telegram.tgnet.TLRPC$TL_messageActionCustomAction
            if (r14 == 0) goto L_0x01f4
            java.lang.String r0 = r9.message
            r6.messageText = r0
            goto L_0x0d75
        L_0x01f4:
            boolean r14 = r9 instanceof org.telegram.tgnet.TLRPC$TL_messageActionChatCreate
            if (r14 == 0) goto L_0x021c
            boolean r0 = r16.isOut()
            if (r0 == 0) goto L_0x020b
            r0 = 2131624170(0x7f0e00ea, float:1.8875512E38)
            java.lang.String r1 = "ActionYouCreateGroup"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r1, r0)
            r6.messageText = r0
            goto L_0x0d75
        L_0x020b:
            r0 = 2131624113(0x7f0e00b1, float:1.8875397E38)
            java.lang.String r1 = "ActionCreateGroup"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r1, r0)
            java.lang.CharSequence r0 = replaceWithLink(r0, r15, r8)
            r6.messageText = r0
            goto L_0x0d75
        L_0x021c:
            boolean r14 = r9 instanceof org.telegram.tgnet.TLRPC$TL_messageActionChatDeleteUser
            if (r14 == 0) goto L_0x02b1
            boolean r0 = r16.isFromUser()
            if (r0 == 0) goto L_0x0256
            org.telegram.tgnet.TLRPC$Message r0 = r6.messageOwner
            org.telegram.tgnet.TLRPC$MessageAction r1 = r0.action
            int r1 = r1.user_id
            org.telegram.tgnet.TLRPC$Peer r0 = r0.from_id
            int r0 = r0.user_id
            if (r1 != r0) goto L_0x0256
            boolean r0 = r16.isOut()
            if (r0 == 0) goto L_0x0245
            r0 = 2131624172(0x7f0e00ec, float:1.8875516E38)
            java.lang.String r1 = "ActionYouLeftUser"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r1, r0)
            r6.messageText = r0
            goto L_0x0d75
        L_0x0245:
            r0 = 2131624129(0x7f0e00c1, float:1.887543E38)
            java.lang.String r1 = "ActionLeftUser"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r1, r0)
            java.lang.CharSequence r0 = replaceWithLink(r0, r15, r8)
            r6.messageText = r0
            goto L_0x0d75
        L_0x0256:
            org.telegram.tgnet.TLRPC$Message r0 = r6.messageOwner
            org.telegram.tgnet.TLRPC$MessageAction r0 = r0.action
            int r0 = r0.user_id
            org.telegram.tgnet.TLRPC$User r0 = r6.getUser(r4, r5, r0)
            boolean r1 = r16.isOut()
            if (r1 == 0) goto L_0x0277
            r1 = 2131624171(0x7f0e00eb, float:1.8875514E38)
            java.lang.String r2 = "ActionYouKickUser"
            java.lang.String r1 = org.telegram.messenger.LocaleController.getString(r2, r1)
            java.lang.CharSequence r0 = replaceWithLink(r1, r7, r0)
            r6.messageText = r0
            goto L_0x0d75
        L_0x0277:
            org.telegram.tgnet.TLRPC$Message r1 = r6.messageOwner
            org.telegram.tgnet.TLRPC$MessageAction r1 = r1.action
            int r1 = r1.user_id
            int r2 = r6.currentAccount
            org.telegram.messenger.UserConfig r2 = org.telegram.messenger.UserConfig.getInstance(r2)
            int r2 = r2.getClientUserId()
            if (r1 != r2) goto L_0x029a
            r0 = 2131624128(0x7f0e00c0, float:1.8875427E38)
            java.lang.String r1 = "ActionKickUserYou"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r1, r0)
            java.lang.CharSequence r0 = replaceWithLink(r0, r15, r8)
            r6.messageText = r0
            goto L_0x0d75
        L_0x029a:
            r1 = 2131624127(0x7f0e00bf, float:1.8875425E38)
            java.lang.String r2 = "ActionKickUser"
            java.lang.String r1 = org.telegram.messenger.LocaleController.getString(r2, r1)
            java.lang.CharSequence r0 = replaceWithLink(r1, r7, r0)
            r6.messageText = r0
            java.lang.CharSequence r0 = replaceWithLink(r0, r15, r8)
            r6.messageText = r0
            goto L_0x0d75
        L_0x02b1:
            boolean r14 = r9 instanceof org.telegram.tgnet.TLRPC$TL_messageActionChatAddUser
            if (r14 == 0) goto L_0x0417
            int r2 = r9.user_id
            if (r2 != 0) goto L_0x02d1
            java.util.ArrayList<java.lang.Integer> r3 = r9.users
            int r3 = r3.size()
            if (r3 != r12) goto L_0x02d1
            org.telegram.tgnet.TLRPC$Message r2 = r6.messageOwner
            org.telegram.tgnet.TLRPC$MessageAction r2 = r2.action
            java.util.ArrayList<java.lang.Integer> r2 = r2.users
            java.lang.Object r2 = r2.get(r13)
            java.lang.Integer r2 = (java.lang.Integer) r2
            int r2 = r2.intValue()
        L_0x02d1:
            r3 = 2131624166(0x7f0e00e6, float:1.8875504E38)
            java.lang.String r9 = "ActionYouAddUser"
            r11 = 2131624085(0x7f0e0095, float:1.887534E38)
            java.lang.String r12 = "ActionAddUser"
            if (r2 == 0) goto L_0x03d7
            org.telegram.tgnet.TLRPC$User r4 = r6.getUser(r4, r5, r2)
            org.telegram.tgnet.TLRPC$Message r5 = r6.messageOwner
            org.telegram.tgnet.TLRPC$Peer r5 = r5.peer_id
            int r5 = r5.channel_id
            if (r5 == 0) goto L_0x02ee
            org.telegram.tgnet.TLRPC$Chat r0 = r6.getChat(r0, r1, r5)
            goto L_0x02ef
        L_0x02ee:
            r0 = 0
        L_0x02ef:
            org.telegram.tgnet.TLRPC$Message r1 = r6.messageOwner
            org.telegram.tgnet.TLRPC$Peer r1 = r1.from_id
            if (r1 == 0) goto L_0x0366
            int r1 = r1.user_id
            if (r2 != r1) goto L_0x0366
            boolean r1 = org.telegram.messenger.ChatObject.isChannel(r0)
            if (r1 == 0) goto L_0x0310
            boolean r0 = r0.megagroup
            if (r0 != 0) goto L_0x0310
            r0 = 2131624708(0x7f0e0304, float:1.8876603E38)
            java.lang.String r1 = "ChannelJoined"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r1, r0)
            r6.messageText = r0
            goto L_0x0d75
        L_0x0310:
            org.telegram.tgnet.TLRPC$Message r0 = r6.messageOwner
            org.telegram.tgnet.TLRPC$Peer r0 = r0.peer_id
            int r0 = r0.channel_id
            if (r0 == 0) goto L_0x0342
            int r0 = r6.currentAccount
            org.telegram.messenger.UserConfig r0 = org.telegram.messenger.UserConfig.getInstance(r0)
            int r0 = r0.getClientUserId()
            if (r2 != r0) goto L_0x0331
            r0 = 2131624713(0x7f0e0309, float:1.8876613E38)
            java.lang.String r1 = "ChannelMegaJoined"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r1, r0)
            r6.messageText = r0
            goto L_0x0d75
        L_0x0331:
            r0 = 2131624087(0x7f0e0097, float:1.8875344E38)
            java.lang.String r1 = "ActionAddUserSelfMega"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r1, r0)
            java.lang.CharSequence r0 = replaceWithLink(r0, r15, r8)
            r6.messageText = r0
            goto L_0x0d75
        L_0x0342:
            boolean r0 = r16.isOut()
            if (r0 == 0) goto L_0x0355
            r0 = 2131624088(0x7f0e0098, float:1.8875346E38)
            java.lang.String r1 = "ActionAddUserSelfYou"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r1, r0)
            r6.messageText = r0
            goto L_0x0d75
        L_0x0355:
            r0 = 2131624086(0x7f0e0096, float:1.8875342E38)
            java.lang.String r1 = "ActionAddUserSelf"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r1, r0)
            java.lang.CharSequence r0 = replaceWithLink(r0, r15, r8)
            r6.messageText = r0
            goto L_0x0d75
        L_0x0366:
            boolean r1 = r16.isOut()
            if (r1 == 0) goto L_0x0378
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r9, r3)
            java.lang.CharSequence r0 = replaceWithLink(r0, r7, r4)
            r6.messageText = r0
            goto L_0x0d75
        L_0x0378:
            int r1 = r6.currentAccount
            org.telegram.messenger.UserConfig r1 = org.telegram.messenger.UserConfig.getInstance(r1)
            int r1 = r1.getClientUserId()
            if (r2 != r1) goto L_0x03c5
            org.telegram.tgnet.TLRPC$Message r1 = r6.messageOwner
            org.telegram.tgnet.TLRPC$Peer r1 = r1.peer_id
            int r1 = r1.channel_id
            if (r1 == 0) goto L_0x03b4
            if (r0 == 0) goto L_0x03a3
            boolean r0 = r0.megagroup
            if (r0 == 0) goto L_0x03a3
            r0 = 2131626052(0x7f0e0844, float:1.887933E38)
            java.lang.String r1 = "MegaAddedBy"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r1, r0)
            java.lang.CharSequence r0 = replaceWithLink(r0, r15, r8)
            r6.messageText = r0
            goto L_0x0d75
        L_0x03a3:
            r0 = 2131624674(0x7f0e02e2, float:1.8876534E38)
            java.lang.String r1 = "ChannelAddedBy"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r1, r0)
            java.lang.CharSequence r0 = replaceWithLink(r0, r15, r8)
            r6.messageText = r0
            goto L_0x0d75
        L_0x03b4:
            r0 = 2131624089(0x7f0e0099, float:1.8875348E38)
            java.lang.String r1 = "ActionAddUserYou"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r1, r0)
            java.lang.CharSequence r0 = replaceWithLink(r0, r15, r8)
            r6.messageText = r0
            goto L_0x0d75
        L_0x03c5:
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r12, r11)
            java.lang.CharSequence r0 = replaceWithLink(r0, r7, r4)
            r6.messageText = r0
            java.lang.CharSequence r0 = replaceWithLink(r0, r15, r8)
            r6.messageText = r0
            goto L_0x0d75
        L_0x03d7:
            boolean r0 = r16.isOut()
            if (r0 == 0) goto L_0x03f7
            java.lang.String r1 = org.telegram.messenger.LocaleController.getString(r9, r3)
            org.telegram.tgnet.TLRPC$Message r0 = r6.messageOwner
            org.telegram.tgnet.TLRPC$MessageAction r0 = r0.action
            java.util.ArrayList<java.lang.Integer> r3 = r0.users
            java.lang.String r2 = "un2"
            r0 = r16
            r4 = r17
            r5 = r19
            java.lang.CharSequence r0 = r0.replaceWithLink(r1, r2, r3, r4, r5)
            r6.messageText = r0
            goto L_0x0d75
        L_0x03f7:
            java.lang.String r1 = org.telegram.messenger.LocaleController.getString(r12, r11)
            org.telegram.tgnet.TLRPC$Message r0 = r6.messageOwner
            org.telegram.tgnet.TLRPC$MessageAction r0 = r0.action
            java.util.ArrayList<java.lang.Integer> r3 = r0.users
            java.lang.String r2 = "un2"
            r0 = r16
            r4 = r17
            r5 = r19
            java.lang.CharSequence r0 = r0.replaceWithLink(r1, r2, r3, r4, r5)
            r6.messageText = r0
            java.lang.CharSequence r0 = replaceWithLink(r0, r15, r8)
            r6.messageText = r0
            goto L_0x0d75
        L_0x0417:
            boolean r14 = r9 instanceof org.telegram.tgnet.TLRPC$TL_messageActionChatJoinedByLink
            if (r14 == 0) goto L_0x043f
            boolean r0 = r16.isOut()
            if (r0 == 0) goto L_0x042e
            r0 = 2131624126(0x7f0e00be, float:1.8875423E38)
            java.lang.String r1 = "ActionInviteYou"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r1, r0)
            r6.messageText = r0
            goto L_0x0d75
        L_0x042e:
            r0 = 2131624125(0x7f0e00bd, float:1.887542E38)
            java.lang.String r1 = "ActionInviteUser"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r1, r0)
            java.lang.CharSequence r0 = replaceWithLink(r0, r15, r8)
            r6.messageText = r0
            goto L_0x0d75
        L_0x043f:
            boolean r14 = r9 instanceof org.telegram.tgnet.TLRPC$TL_messageActionChatEditPhoto
            if (r14 == 0) goto L_0x04c9
            org.telegram.tgnet.TLRPC$Peer r2 = r3.peer_id
            if (r2 == 0) goto L_0x0450
            int r2 = r2.channel_id
            if (r2 == 0) goto L_0x0450
            org.telegram.tgnet.TLRPC$Chat r7 = r6.getChat(r0, r1, r2)
            goto L_0x0451
        L_0x0450:
            r7 = 0
        L_0x0451:
            boolean r0 = org.telegram.messenger.ChatObject.isChannel(r7)
            if (r0 == 0) goto L_0x047b
            boolean r0 = r7.megagroup
            if (r0 != 0) goto L_0x047b
            boolean r0 = r16.isVideoAvatar()
            if (r0 == 0) goto L_0x046e
            r0 = 2131624110(0x7f0e00ae, float:1.887539E38)
            java.lang.String r1 = "ActionChannelChangedVideo"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r1, r0)
            r6.messageText = r0
            goto L_0x0d75
        L_0x046e:
            r0 = 2131624108(0x7f0e00ac, float:1.8875386E38)
            java.lang.String r1 = "ActionChannelChangedPhoto"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r1, r0)
            r6.messageText = r0
            goto L_0x0d75
        L_0x047b:
            boolean r0 = r16.isOut()
            if (r0 == 0) goto L_0x04a1
            boolean r0 = r16.isVideoAvatar()
            if (r0 == 0) goto L_0x0494
            r0 = 2131624169(0x7f0e00e9, float:1.887551E38)
            java.lang.String r1 = "ActionYouChangedVideo"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r1, r0)
            r6.messageText = r0
            goto L_0x0d75
        L_0x0494:
            r0 = 2131624167(0x7f0e00e7, float:1.8875506E38)
            java.lang.String r1 = "ActionYouChangedPhoto"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r1, r0)
            r6.messageText = r0
            goto L_0x0d75
        L_0x04a1:
            boolean r0 = r16.isVideoAvatar()
            if (r0 == 0) goto L_0x04b8
            r0 = 2131624107(0x7f0e00ab, float:1.8875384E38)
            java.lang.String r1 = "ActionChangedVideo"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r1, r0)
            java.lang.CharSequence r0 = replaceWithLink(r0, r15, r8)
            r6.messageText = r0
            goto L_0x0d75
        L_0x04b8:
            r0 = 2131624105(0x7f0e00a9, float:1.887538E38)
            java.lang.String r1 = "ActionChangedPhoto"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r1, r0)
            java.lang.CharSequence r0 = replaceWithLink(r0, r15, r8)
            r6.messageText = r0
            goto L_0x0d75
        L_0x04c9:
            boolean r14 = r9 instanceof org.telegram.tgnet.TLRPC$TL_messageActionChatEditTitle
            if (r14 == 0) goto L_0x0534
            org.telegram.tgnet.TLRPC$Peer r2 = r3.peer_id
            if (r2 == 0) goto L_0x04da
            int r2 = r2.channel_id
            if (r2 == 0) goto L_0x04da
            org.telegram.tgnet.TLRPC$Chat r0 = r6.getChat(r0, r1, r2)
            goto L_0x04db
        L_0x04da:
            r0 = 0
        L_0x04db:
            boolean r1 = org.telegram.messenger.ChatObject.isChannel(r0)
            if (r1 == 0) goto L_0x04fc
            boolean r0 = r0.megagroup
            if (r0 != 0) goto L_0x04fc
            r0 = 2131624109(0x7f0e00ad, float:1.8875388E38)
            java.lang.String r1 = "ActionChannelChangedTitle"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r1, r0)
            org.telegram.tgnet.TLRPC$Message r1 = r6.messageOwner
            org.telegram.tgnet.TLRPC$MessageAction r1 = r1.action
            java.lang.String r1 = r1.title
            java.lang.String r0 = r0.replace(r7, r1)
            r6.messageText = r0
            goto L_0x0d75
        L_0x04fc:
            boolean r0 = r16.isOut()
            if (r0 == 0) goto L_0x0519
            r0 = 2131624168(0x7f0e00e8, float:1.8875508E38)
            java.lang.String r1 = "ActionYouChangedTitle"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r1, r0)
            org.telegram.tgnet.TLRPC$Message r1 = r6.messageOwner
            org.telegram.tgnet.TLRPC$MessageAction r1 = r1.action
            java.lang.String r1 = r1.title
            java.lang.String r0 = r0.replace(r7, r1)
            r6.messageText = r0
            goto L_0x0d75
        L_0x0519:
            r0 = 2131624106(0x7f0e00aa, float:1.8875382E38)
            java.lang.String r1 = "ActionChangedTitle"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r1, r0)
            org.telegram.tgnet.TLRPC$Message r1 = r6.messageOwner
            org.telegram.tgnet.TLRPC$MessageAction r1 = r1.action
            java.lang.String r1 = r1.title
            java.lang.String r0 = r0.replace(r7, r1)
            java.lang.CharSequence r0 = replaceWithLink(r0, r15, r8)
            r6.messageText = r0
            goto L_0x0d75
        L_0x0534:
            boolean r7 = r9 instanceof org.telegram.tgnet.TLRPC$TL_messageActionChatDeletePhoto
            if (r7 == 0) goto L_0x0581
            org.telegram.tgnet.TLRPC$Peer r2 = r3.peer_id
            if (r2 == 0) goto L_0x0545
            int r2 = r2.channel_id
            if (r2 == 0) goto L_0x0545
            org.telegram.tgnet.TLRPC$Chat r7 = r6.getChat(r0, r1, r2)
            goto L_0x0546
        L_0x0545:
            r7 = 0
        L_0x0546:
            boolean r0 = org.telegram.messenger.ChatObject.isChannel(r7)
            if (r0 == 0) goto L_0x055d
            boolean r0 = r7.megagroup
            if (r0 != 0) goto L_0x055d
            r0 = 2131624111(0x7f0e00af, float:1.8875392E38)
            java.lang.String r1 = "ActionChannelRemovedPhoto"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r1, r0)
            r6.messageText = r0
            goto L_0x0d75
        L_0x055d:
            boolean r0 = r16.isOut()
            if (r0 == 0) goto L_0x0570
            r0 = 2131624173(0x7f0e00ed, float:1.8875518E38)
            java.lang.String r1 = "ActionYouRemovedPhoto"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r1, r0)
            r6.messageText = r0
            goto L_0x0d75
        L_0x0570:
            r0 = 2131624148(0x7f0e00d4, float:1.8875468E38)
            java.lang.String r1 = "ActionRemovedPhoto"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r1, r0)
            java.lang.CharSequence r0 = replaceWithLink(r0, r15, r8)
            r6.messageText = r0
            goto L_0x0d75
        L_0x0581:
            boolean r7 = r9 instanceof org.telegram.tgnet.TLRPC$TL_messageActionTTLChange
            java.lang.String r14 = "MessageLifetimeChangedOutgoing"
            java.lang.String r11 = "MessageLifetimeChanged"
            java.lang.String r13 = "MessageLifetimeYouRemoved"
            java.lang.String r12 = "MessageLifetimeRemoved"
            if (r7 == 0) goto L_0x05fa
            int r0 = r9.ttl
            if (r0 == 0) goto L_0x05d4
            boolean r0 = r16.isOut()
            if (r0 == 0) goto L_0x05b2
            r0 = 2131626092(0x7f0e086c, float:1.887941E38)
            r1 = 1
            java.lang.Object[] r1 = new java.lang.Object[r1]
            org.telegram.tgnet.TLRPC$Message r2 = r6.messageOwner
            org.telegram.tgnet.TLRPC$MessageAction r2 = r2.action
            int r2 = r2.ttl
            java.lang.String r2 = org.telegram.messenger.LocaleController.formatTTLString(r2)
            r3 = 0
            r1[r3] = r2
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r14, r0, r1)
            r6.messageText = r0
            goto L_0x0d75
        L_0x05b2:
            r0 = 2
            r3 = 0
            java.lang.Object[] r0 = new java.lang.Object[r0]
            java.lang.String r1 = org.telegram.messenger.UserObject.getFirstName(r2)
            r0[r3] = r1
            org.telegram.tgnet.TLRPC$Message r1 = r6.messageOwner
            org.telegram.tgnet.TLRPC$MessageAction r1 = r1.action
            int r1 = r1.ttl
            java.lang.String r1 = org.telegram.messenger.LocaleController.formatTTLString(r1)
            r3 = 1
            r0[r3] = r1
            r1 = 2131626091(0x7f0e086b, float:1.8879408E38)
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r11, r1, r0)
            r6.messageText = r0
            goto L_0x0d75
        L_0x05d4:
            r3 = 1
            boolean r0 = r16.isOut()
            if (r0 == 0) goto L_0x05e6
            r0 = 2131626096(0x7f0e0870, float:1.8879419E38)
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r13, r0)
            r6.messageText = r0
            goto L_0x0d75
        L_0x05e6:
            java.lang.Object[] r0 = new java.lang.Object[r3]
            java.lang.String r1 = org.telegram.messenger.UserObject.getFirstName(r2)
            r2 = 0
            r0[r2] = r1
            r1 = 2131626094(0x7f0e086e, float:1.8879414E38)
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r12, r1, r0)
            r6.messageText = r0
            goto L_0x0d75
        L_0x05fa:
            boolean r7 = r9 instanceof org.telegram.tgnet.TLRPC$TL_messageActionSetMessagesTTL
            if (r7 == 0) goto L_0x06a0
            org.telegram.tgnet.TLRPC$TL_messageActionSetMessagesTTL r9 = (org.telegram.tgnet.TLRPC$TL_messageActionSetMessagesTTL) r9
            org.telegram.tgnet.TLRPC$Peer r2 = r3.peer_id
            if (r2 == 0) goto L_0x060d
            int r2 = r2.channel_id
            if (r2 == 0) goto L_0x060d
            org.telegram.tgnet.TLRPC$Chat r7 = r6.getChat(r0, r1, r2)
            goto L_0x060e
        L_0x060d:
            r7 = 0
        L_0x060e:
            if (r7 == 0) goto L_0x063c
            boolean r0 = r7.megagroup
            if (r0 != 0) goto L_0x063c
            int r0 = r9.period
            if (r0 == 0) goto L_0x062f
            r1 = 2131624151(0x7f0e00d7, float:1.8875474E38)
            r2 = 1
            java.lang.Object[] r2 = new java.lang.Object[r2]
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatTTLString(r0)
            r3 = 0
            r2[r3] = r0
            java.lang.String r0 = "ActionTTLChannelChanged"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r0, r1, r2)
            r6.messageText = r0
            goto L_0x0d75
        L_0x062f:
            r0 = 2131624152(0x7f0e00d8, float:1.8875476E38)
            java.lang.String r1 = "ActionTTLChannelDisabled"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r1, r0)
            r6.messageText = r0
            goto L_0x0d75
        L_0x063c:
            int r0 = r9.period
            if (r0 == 0) goto L_0x067c
            boolean r0 = r16.isOut()
            if (r0 == 0) goto L_0x065f
            r0 = 2131624154(0x7f0e00da, float:1.887548E38)
            r1 = 1
            java.lang.Object[] r1 = new java.lang.Object[r1]
            int r2 = r9.period
            java.lang.String r2 = org.telegram.messenger.LocaleController.formatTTLString(r2)
            r3 = 0
            r1[r3] = r2
            java.lang.String r2 = "ActionTTLYouChanged"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            r6.messageText = r0
            goto L_0x0d75
        L_0x065f:
            r1 = 1
            r3 = 0
            r0 = 2131624150(0x7f0e00d6, float:1.8875472E38)
            java.lang.Object[] r1 = new java.lang.Object[r1]
            int r2 = r9.period
            java.lang.String r2 = org.telegram.messenger.LocaleController.formatTTLString(r2)
            r1[r3] = r2
            java.lang.String r2 = "ActionTTLChanged"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            java.lang.CharSequence r0 = replaceWithLink(r0, r15, r8)
            r6.messageText = r0
            goto L_0x0d75
        L_0x067c:
            boolean r0 = r16.isOut()
            if (r0 == 0) goto L_0x068f
            r0 = 2131624155(0x7f0e00db, float:1.8875482E38)
            java.lang.String r1 = "ActionTTLYouDisabled"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r1, r0)
            r6.messageText = r0
            goto L_0x0d75
        L_0x068f:
            r0 = 2131624153(0x7f0e00d9, float:1.8875478E38)
            java.lang.String r1 = "ActionTTLDisabled"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r1, r0)
            java.lang.CharSequence r0 = replaceWithLink(r0, r15, r8)
            r6.messageText = r0
            goto L_0x0d75
        L_0x06a0:
            boolean r7 = r9 instanceof org.telegram.tgnet.TLRPC$TL_messageActionLoginUnknownLocation
            if (r7 == 0) goto L_0x0737
            int r0 = r3.date
            long r0 = (long) r0
            r2 = 1000(0x3e8, double:4.94E-321)
            long r0 = r0 * r2
            org.telegram.messenger.LocaleController r2 = org.telegram.messenger.LocaleController.getInstance()
            org.telegram.messenger.time.FastDateFormat r2 = r2.formatterDay
            if (r2 == 0) goto L_0x06e2
            org.telegram.messenger.LocaleController r2 = org.telegram.messenger.LocaleController.getInstance()
            org.telegram.messenger.time.FastDateFormat r2 = r2.formatterYear
            if (r2 == 0) goto L_0x06e2
            r2 = 2131628154(0x7f0e107a, float:1.8883593E38)
            r3 = 2
            java.lang.Object[] r7 = new java.lang.Object[r3]
            org.telegram.messenger.LocaleController r3 = org.telegram.messenger.LocaleController.getInstance()
            org.telegram.messenger.time.FastDateFormat r3 = r3.formatterYear
            java.lang.String r3 = r3.format((long) r0)
            r8 = 0
            r7[r8] = r3
            org.telegram.messenger.LocaleController r3 = org.telegram.messenger.LocaleController.getInstance()
            org.telegram.messenger.time.FastDateFormat r3 = r3.formatterDay
            java.lang.String r0 = r3.format((long) r0)
            r1 = 1
            r7[r1] = r0
            java.lang.String r0 = "formatDateAtTime"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r0, r2, r7)
            goto L_0x06f5
        L_0x06e2:
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            r0.append(r10)
            org.telegram.tgnet.TLRPC$Message r1 = r6.messageOwner
            int r1 = r1.date
            r0.append(r1)
            java.lang.String r0 = r0.toString()
        L_0x06f5:
            int r1 = r6.currentAccount
            org.telegram.messenger.UserConfig r1 = org.telegram.messenger.UserConfig.getInstance(r1)
            org.telegram.tgnet.TLRPC$User r1 = r1.getCurrentUser()
            if (r1 != 0) goto L_0x070b
            org.telegram.tgnet.TLRPC$Message r1 = r6.messageOwner
            org.telegram.tgnet.TLRPC$Peer r1 = r1.peer_id
            int r1 = r1.user_id
            org.telegram.tgnet.TLRPC$User r1 = r6.getUser(r4, r5, r1)
        L_0x070b:
            if (r1 == 0) goto L_0x0712
            java.lang.String r1 = org.telegram.messenger.UserObject.getFirstName(r1)
            goto L_0x0713
        L_0x0712:
            r1 = r10
        L_0x0713:
            r2 = 2131626418(0x7f0e09b2, float:1.8880072E38)
            r3 = 4
            java.lang.Object[] r3 = new java.lang.Object[r3]
            r4 = 0
            r3[r4] = r1
            r1 = 1
            r3[r1] = r0
            org.telegram.tgnet.TLRPC$Message r0 = r6.messageOwner
            org.telegram.tgnet.TLRPC$MessageAction r0 = r0.action
            java.lang.String r1 = r0.title
            r4 = 2
            r3[r4] = r1
            r1 = 3
            java.lang.String r0 = r0.address
            r3[r1] = r0
            java.lang.String r0 = "NotificationUnrecognizedDevice"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r0, r2, r3)
            r6.messageText = r0
            goto L_0x0d75
        L_0x0737:
            boolean r7 = r9 instanceof org.telegram.tgnet.TLRPC$TL_messageActionUserJoined
            if (r7 != 0) goto L_0x0b70
            boolean r7 = r9 instanceof org.telegram.tgnet.TLRPC$TL_messageActionContactSignUp
            if (r7 == 0) goto L_0x0741
            goto L_0x0b70
        L_0x0741:
            boolean r7 = r9 instanceof org.telegram.tgnet.TLRPC$TL_messageActionUserUpdatedPhoto
            if (r7 == 0) goto L_0x075c
            r0 = 2131626349(0x7f0e096d, float:1.8879932E38)
            r1 = 1
            java.lang.Object[] r1 = new java.lang.Object[r1]
            java.lang.String r2 = org.telegram.messenger.UserObject.getUserName(r2)
            r3 = 0
            r1[r3] = r2
            java.lang.String r2 = "NotificationContactNewPhoto"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            r6.messageText = r0
            goto L_0x0d75
        L_0x075c:
            boolean r7 = r9 instanceof org.telegram.tgnet.TLRPC$TL_messageEncryptedAction
            if (r7 == 0) goto L_0x07f8
            org.telegram.tgnet.TLRPC$DecryptedMessageAction r0 = r9.encryptedAction
            boolean r1 = r0 instanceof org.telegram.tgnet.TLRPC$TL_decryptedMessageActionScreenshotMessages
            if (r1 == 0) goto L_0x078d
            boolean r0 = r16.isOut()
            if (r0 == 0) goto L_0x077c
            r0 = 2131624157(0x7f0e00dd, float:1.8875486E38)
            r1 = 0
            java.lang.Object[] r1 = new java.lang.Object[r1]
            java.lang.String r2 = "ActionTakeScreenshootYou"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            r6.messageText = r0
            goto L_0x0d75
        L_0x077c:
            r0 = 2131624156(0x7f0e00dc, float:1.8875484E38)
            java.lang.String r1 = "ActionTakeScreenshoot"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r1, r0)
            java.lang.CharSequence r0 = replaceWithLink(r0, r15, r8)
            r6.messageText = r0
            goto L_0x0d75
        L_0x078d:
            boolean r1 = r0 instanceof org.telegram.tgnet.TLRPC$TL_decryptedMessageActionSetMessageTTL
            if (r1 == 0) goto L_0x0d75
            org.telegram.tgnet.TLRPC$TL_decryptedMessageActionSetMessageTTL r0 = (org.telegram.tgnet.TLRPC$TL_decryptedMessageActionSetMessageTTL) r0
            int r1 = r0.ttl_seconds
            if (r1 == 0) goto L_0x07d2
            boolean r1 = r16.isOut()
            if (r1 == 0) goto L_0x07b4
            r1 = 2131626092(0x7f0e086c, float:1.887941E38)
            r2 = 1
            java.lang.Object[] r2 = new java.lang.Object[r2]
            int r0 = r0.ttl_seconds
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatTTLString(r0)
            r3 = 0
            r2[r3] = r0
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r14, r1, r2)
            r6.messageText = r0
            goto L_0x0d75
        L_0x07b4:
            r1 = 2
            r3 = 0
            java.lang.Object[] r1 = new java.lang.Object[r1]
            java.lang.String r2 = org.telegram.messenger.UserObject.getFirstName(r2)
            r1[r3] = r2
            int r0 = r0.ttl_seconds
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatTTLString(r0)
            r3 = 1
            r1[r3] = r0
            r0 = 2131626091(0x7f0e086b, float:1.8879408E38)
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r11, r0, r1)
            r6.messageText = r0
            goto L_0x0d75
        L_0x07d2:
            r3 = 1
            boolean r0 = r16.isOut()
            if (r0 == 0) goto L_0x07e4
            r0 = 2131626096(0x7f0e0870, float:1.8879419E38)
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r13, r0)
            r6.messageText = r0
            goto L_0x0d75
        L_0x07e4:
            java.lang.Object[] r0 = new java.lang.Object[r3]
            java.lang.String r1 = org.telegram.messenger.UserObject.getFirstName(r2)
            r7 = 0
            r0[r7] = r1
            r1 = 2131626094(0x7f0e086e, float:1.8879414E38)
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r12, r1, r0)
            r6.messageText = r0
            goto L_0x0d75
        L_0x07f8:
            r7 = 0
            boolean r11 = r9 instanceof org.telegram.tgnet.TLRPC$TL_messageActionScreenshotTaken
            if (r11 == 0) goto L_0x0823
            boolean r0 = r16.isOut()
            if (r0 == 0) goto L_0x0812
            r0 = 2131624157(0x7f0e00dd, float:1.8875486E38)
            java.lang.Object[] r1 = new java.lang.Object[r7]
            java.lang.String r2 = "ActionTakeScreenshootYou"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            r6.messageText = r0
            goto L_0x0d75
        L_0x0812:
            r0 = 2131624156(0x7f0e00dc, float:1.8875484E38)
            java.lang.String r1 = "ActionTakeScreenshoot"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r1, r0)
            java.lang.CharSequence r0 = replaceWithLink(r0, r15, r8)
            r6.messageText = r0
            goto L_0x0d75
        L_0x0823:
            boolean r7 = r9 instanceof org.telegram.tgnet.TLRPC$TL_messageActionCreatedBroadcastList
            if (r7 == 0) goto L_0x0837
            r0 = 2131628085(0x7f0e1035, float:1.8883453E38)
            r1 = 0
            java.lang.Object[] r1 = new java.lang.Object[r1]
            java.lang.String r2 = "YouCreatedBroadcastList"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            r6.messageText = r0
            goto L_0x0d75
        L_0x0837:
            boolean r7 = r9 instanceof org.telegram.tgnet.TLRPC$TL_messageActionChannelCreate
            if (r7 == 0) goto L_0x086d
            org.telegram.tgnet.TLRPC$Peer r2 = r3.peer_id
            if (r2 == 0) goto L_0x0848
            int r2 = r2.channel_id
            if (r2 == 0) goto L_0x0848
            org.telegram.tgnet.TLRPC$Chat r7 = r6.getChat(r0, r1, r2)
            goto L_0x0849
        L_0x0848:
            r7 = 0
        L_0x0849:
            boolean r0 = org.telegram.messenger.ChatObject.isChannel(r7)
            if (r0 == 0) goto L_0x0860
            boolean r0 = r7.megagroup
            if (r0 == 0) goto L_0x0860
            r0 = 2131624114(0x7f0e00b2, float:1.8875399E38)
            java.lang.String r1 = "ActionCreateMega"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r1, r0)
            r6.messageText = r0
            goto L_0x0d75
        L_0x0860:
            r0 = 2131624112(0x7f0e00b0, float:1.8875395E38)
            java.lang.String r1 = "ActionCreateChannel"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r1, r0)
            r6.messageText = r0
            goto L_0x0d75
        L_0x086d:
            boolean r7 = r9 instanceof org.telegram.tgnet.TLRPC$TL_messageActionChatMigrateTo
            if (r7 == 0) goto L_0x087e
            r0 = 2131624130(0x7f0e00c2, float:1.8875431E38)
            java.lang.String r1 = "ActionMigrateFromGroup"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r1, r0)
            r6.messageText = r0
            goto L_0x0d75
        L_0x087e:
            boolean r7 = r9 instanceof org.telegram.tgnet.TLRPC$TL_messageActionChannelMigrateFrom
            if (r7 == 0) goto L_0x088f
            r0 = 2131624130(0x7f0e00c2, float:1.8875431E38)
            java.lang.String r1 = "ActionMigrateFromGroup"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r1, r0)
            r6.messageText = r0
            goto L_0x0d75
        L_0x088f:
            boolean r7 = r9 instanceof org.telegram.tgnet.TLRPC$TL_messageActionPinMessage
            if (r7 == 0) goto L_0x08a4
            if (r2 != 0) goto L_0x089e
            org.telegram.tgnet.TLRPC$Peer r3 = r3.peer_id
            int r3 = r3.channel_id
            org.telegram.tgnet.TLRPC$Chat r7 = r6.getChat(r0, r1, r3)
            goto L_0x089f
        L_0x089e:
            r7 = 0
        L_0x089f:
            r6.generatePinMessageText(r2, r7)
            goto L_0x0d75
        L_0x08a4:
            boolean r0 = r9 instanceof org.telegram.tgnet.TLRPC$TL_messageActionHistoryClear
            if (r0 == 0) goto L_0x08b5
            r0 = 2131625736(0x7f0e0708, float:1.8878688E38)
            java.lang.String r1 = "HistoryCleared"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r1, r0)
            r6.messageText = r0
            goto L_0x0d75
        L_0x08b5:
            boolean r0 = r9 instanceof org.telegram.tgnet.TLRPC$TL_messageActionGameScore
            if (r0 == 0) goto L_0x08be
            r6.generateGameMessageText(r2)
            goto L_0x0d75
        L_0x08be:
            boolean r0 = r9 instanceof org.telegram.tgnet.TLRPC$TL_messageActionPhoneCall
            if (r0 == 0) goto L_0x09d9
            org.telegram.tgnet.TLRPC$TL_messageActionPhoneCall r9 = (org.telegram.tgnet.TLRPC$TL_messageActionPhoneCall) r9
            org.telegram.tgnet.TLRPC$PhoneCallDiscardReason r0 = r9.reason
            boolean r0 = r0 instanceof org.telegram.tgnet.TLRPC$TL_phoneCallDiscardReasonMissed
            boolean r1 = r16.isFromUser()
            if (r1 == 0) goto L_0x091c
            org.telegram.tgnet.TLRPC$Message r1 = r6.messageOwner
            org.telegram.tgnet.TLRPC$Peer r1 = r1.from_id
            int r1 = r1.user_id
            int r2 = r6.currentAccount
            org.telegram.messenger.UserConfig r2 = org.telegram.messenger.UserConfig.getInstance(r2)
            int r2 = r2.getClientUserId()
            if (r1 != r2) goto L_0x091c
            if (r0 == 0) goto L_0x0900
            boolean r0 = r9.video
            if (r0 == 0) goto L_0x08f3
            r0 = 2131624617(0x7f0e02a9, float:1.8876419E38)
            java.lang.String r1 = "CallMessageVideoOutgoingMissed"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r1, r0)
            r6.messageText = r0
            goto L_0x0977
        L_0x08f3:
            r0 = 2131624611(0x7f0e02a3, float:1.8876407E38)
            java.lang.String r1 = "CallMessageOutgoingMissed"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r1, r0)
            r6.messageText = r0
            goto L_0x0977
        L_0x0900:
            boolean r0 = r9.video
            if (r0 == 0) goto L_0x0910
            r0 = 2131624616(0x7f0e02a8, float:1.8876417E38)
            java.lang.String r1 = "CallMessageVideoOutgoing"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r1, r0)
            r6.messageText = r0
            goto L_0x0977
        L_0x0910:
            r0 = 2131624610(0x7f0e02a2, float:1.8876405E38)
            java.lang.String r1 = "CallMessageOutgoing"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r1, r0)
            r6.messageText = r0
            goto L_0x0977
        L_0x091c:
            if (r0 == 0) goto L_0x093a
            boolean r0 = r9.video
            if (r0 == 0) goto L_0x092e
            r0 = 2131624615(0x7f0e02a7, float:1.8876415E38)
            java.lang.String r1 = "CallMessageVideoIncomingMissed"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r1, r0)
            r6.messageText = r0
            goto L_0x0977
        L_0x092e:
            r0 = 2131624609(0x7f0e02a1, float:1.8876403E38)
            java.lang.String r1 = "CallMessageIncomingMissed"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r1, r0)
            r6.messageText = r0
            goto L_0x0977
        L_0x093a:
            org.telegram.tgnet.TLRPC$PhoneCallDiscardReason r0 = r9.reason
            boolean r0 = r0 instanceof org.telegram.tgnet.TLRPC$TL_phoneCallDiscardReasonBusy
            if (r0 == 0) goto L_0x095c
            boolean r0 = r9.video
            if (r0 == 0) goto L_0x0950
            r0 = 2131624614(0x7f0e02a6, float:1.8876413E38)
            java.lang.String r1 = "CallMessageVideoIncomingDeclined"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r1, r0)
            r6.messageText = r0
            goto L_0x0977
        L_0x0950:
            r0 = 2131624608(0x7f0e02a0, float:1.88764E38)
            java.lang.String r1 = "CallMessageIncomingDeclined"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r1, r0)
            r6.messageText = r0
            goto L_0x0977
        L_0x095c:
            boolean r0 = r9.video
            if (r0 == 0) goto L_0x096c
            r0 = 2131624613(0x7f0e02a5, float:1.887641E38)
            java.lang.String r1 = "CallMessageVideoIncoming"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r1, r0)
            r6.messageText = r0
            goto L_0x0977
        L_0x096c:
            r0 = 2131624607(0x7f0e029f, float:1.8876398E38)
            java.lang.String r1 = "CallMessageIncoming"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r1, r0)
            r6.messageText = r0
        L_0x0977:
            int r0 = r9.duration
            if (r0 <= 0) goto L_0x0d75
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatCallDuration(r0)
            r1 = 2131624618(0x7f0e02aa, float:1.887642E38)
            r2 = 2
            java.lang.Object[] r2 = new java.lang.Object[r2]
            java.lang.CharSequence r3 = r6.messageText
            r4 = 0
            r2[r4] = r3
            r3 = 1
            r2[r3] = r0
            java.lang.String r3 = "CallMessageWithDuration"
            java.lang.String r1 = org.telegram.messenger.LocaleController.formatString(r3, r1, r2)
            r6.messageText = r1
            java.lang.String r1 = r1.toString()
            int r2 = r1.indexOf(r0)
            r3 = -1
            if (r2 == r3) goto L_0x0d75
            android.text.SpannableString r3 = new android.text.SpannableString
            java.lang.CharSequence r4 = r6.messageText
            r3.<init>(r4)
            int r0 = r0.length()
            int r0 = r0 + r2
            if (r2 <= 0) goto L_0x09ba
            int r4 = r2 + -1
            char r4 = r1.charAt(r4)
            r5 = 40
            if (r4 != r5) goto L_0x09ba
            int r2 = r2 + -1
        L_0x09ba:
            int r4 = r1.length()
            if (r0 >= r4) goto L_0x09ca
            char r1 = r1.charAt(r0)
            r4 = 41
            if (r1 != r4) goto L_0x09ca
            int r0 = r0 + 1
        L_0x09ca:
            org.telegram.ui.Components.TypefaceSpan r1 = new org.telegram.ui.Components.TypefaceSpan
            android.graphics.Typeface r4 = android.graphics.Typeface.DEFAULT
            r1.<init>(r4)
            r4 = 0
            r3.setSpan(r1, r2, r0, r4)
            r6.messageText = r3
            goto L_0x0d75
        L_0x09d9:
            boolean r0 = r9 instanceof org.telegram.tgnet.TLRPC$TL_messageActionPaymentSent
            if (r0 == 0) goto L_0x09eb
            long r0 = r16.getDialogId()
            int r1 = (int) r0
            org.telegram.tgnet.TLRPC$User r0 = r6.getUser(r4, r5, r1)
            r6.generatePaymentSentMessageText(r0)
            goto L_0x0d75
        L_0x09eb:
            boolean r0 = r9 instanceof org.telegram.tgnet.TLRPC$TL_messageActionBotAllowed
            if (r0 == 0) goto L_0x0a37
            org.telegram.tgnet.TLRPC$TL_messageActionBotAllowed r9 = (org.telegram.tgnet.TLRPC$TL_messageActionBotAllowed) r9
            java.lang.String r0 = r9.domain
            r1 = 2131624090(0x7f0e009a, float:1.887535E38)
            java.lang.String r2 = "ActionBotAllowed"
            java.lang.String r1 = org.telegram.messenger.LocaleController.getString(r2, r1)
            java.lang.String r2 = "%1$s"
            int r2 = r1.indexOf(r2)
            android.text.SpannableString r3 = new android.text.SpannableString
            r4 = 1
            java.lang.Object[] r4 = new java.lang.Object[r4]
            r5 = 0
            r4[r5] = r0
            java.lang.String r1 = java.lang.String.format(r1, r4)
            r3.<init>(r1)
            if (r2 < 0) goto L_0x0a33
            org.telegram.ui.Components.URLSpanNoUnderlineBold r1 = new org.telegram.ui.Components.URLSpanNoUnderlineBold
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>()
            java.lang.String r5 = "http://"
            r4.append(r5)
            r4.append(r0)
            java.lang.String r4 = r4.toString()
            r1.<init>(r4)
            int r0 = r0.length()
            int r0 = r0 + r2
            r4 = 33
            r3.setSpan(r1, r2, r0, r4)
        L_0x0a33:
            r6.messageText = r3
            goto L_0x0d75
        L_0x0a37:
            boolean r0 = r9 instanceof org.telegram.tgnet.TLRPC$TL_messageActionSecureValuesSent
            if (r0 == 0) goto L_0x0d75
            org.telegram.tgnet.TLRPC$TL_messageActionSecureValuesSent r9 = (org.telegram.tgnet.TLRPC$TL_messageActionSecureValuesSent) r9
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.util.ArrayList<org.telegram.tgnet.TLRPC$SecureValueType> r1 = r9.types
            int r1 = r1.size()
            r2 = 0
        L_0x0a49:
            if (r2 >= r1) goto L_0x0b44
            java.util.ArrayList<org.telegram.tgnet.TLRPC$SecureValueType> r3 = r9.types
            java.lang.Object r3 = r3.get(r2)
            org.telegram.tgnet.TLRPC$SecureValueType r3 = (org.telegram.tgnet.TLRPC$SecureValueType) r3
            int r7 = r0.length()
            if (r7 <= 0) goto L_0x0a5e
            java.lang.String r7 = ", "
            r0.append(r7)
        L_0x0a5e:
            boolean r7 = r3 instanceof org.telegram.tgnet.TLRPC$TL_secureValueTypePhone
            if (r7 == 0) goto L_0x0a70
            r3 = 2131624100(0x7f0e00a4, float:1.887537E38)
            java.lang.String r7 = "ActionBotDocumentPhone"
            java.lang.String r3 = org.telegram.messenger.LocaleController.getString(r7, r3)
            r0.append(r3)
            goto L_0x0b40
        L_0x0a70:
            boolean r7 = r3 instanceof org.telegram.tgnet.TLRPC$TL_secureValueTypeEmail
            if (r7 == 0) goto L_0x0a82
            r3 = 2131624094(0x7f0e009e, float:1.8875358E38)
            java.lang.String r7 = "ActionBotDocumentEmail"
            java.lang.String r3 = org.telegram.messenger.LocaleController.getString(r7, r3)
            r0.append(r3)
            goto L_0x0b40
        L_0x0a82:
            boolean r7 = r3 instanceof org.telegram.tgnet.TLRPC$TL_secureValueTypeAddress
            if (r7 == 0) goto L_0x0a94
            r3 = 2131624091(0x7f0e009b, float:1.8875352E38)
            java.lang.String r7 = "ActionBotDocumentAddress"
            java.lang.String r3 = org.telegram.messenger.LocaleController.getString(r7, r3)
            r0.append(r3)
            goto L_0x0b40
        L_0x0a94:
            boolean r7 = r3 instanceof org.telegram.tgnet.TLRPC$TL_secureValueTypePersonalDetails
            if (r7 == 0) goto L_0x0aa6
            r3 = 2131624095(0x7f0e009f, float:1.887536E38)
            java.lang.String r7 = "ActionBotDocumentIdentity"
            java.lang.String r3 = org.telegram.messenger.LocaleController.getString(r7, r3)
            r0.append(r3)
            goto L_0x0b40
        L_0x0aa6:
            boolean r7 = r3 instanceof org.telegram.tgnet.TLRPC$TL_secureValueTypePassport
            if (r7 == 0) goto L_0x0ab8
            r3 = 2131624098(0x7f0e00a2, float:1.8875366E38)
            java.lang.String r7 = "ActionBotDocumentPassport"
            java.lang.String r3 = org.telegram.messenger.LocaleController.getString(r7, r3)
            r0.append(r3)
            goto L_0x0b40
        L_0x0ab8:
            boolean r7 = r3 instanceof org.telegram.tgnet.TLRPC$TL_secureValueTypeDriverLicense
            if (r7 == 0) goto L_0x0aca
            r3 = 2131624093(0x7f0e009d, float:1.8875356E38)
            java.lang.String r7 = "ActionBotDocumentDriverLicence"
            java.lang.String r3 = org.telegram.messenger.LocaleController.getString(r7, r3)
            r0.append(r3)
            goto L_0x0b40
        L_0x0aca:
            boolean r7 = r3 instanceof org.telegram.tgnet.TLRPC$TL_secureValueTypeIdentityCard
            if (r7 == 0) goto L_0x0adb
            r3 = 2131624096(0x7f0e00a0, float:1.8875362E38)
            java.lang.String r7 = "ActionBotDocumentIdentityCard"
            java.lang.String r3 = org.telegram.messenger.LocaleController.getString(r7, r3)
            r0.append(r3)
            goto L_0x0b40
        L_0x0adb:
            boolean r7 = r3 instanceof org.telegram.tgnet.TLRPC$TL_secureValueTypeUtilityBill
            if (r7 == 0) goto L_0x0aec
            r3 = 2131624103(0x7f0e00a7, float:1.8875376E38)
            java.lang.String r7 = "ActionBotDocumentUtilityBill"
            java.lang.String r3 = org.telegram.messenger.LocaleController.getString(r7, r3)
            r0.append(r3)
            goto L_0x0b40
        L_0x0aec:
            boolean r7 = r3 instanceof org.telegram.tgnet.TLRPC$TL_secureValueTypeBankStatement
            if (r7 == 0) goto L_0x0afd
            r3 = 2131624092(0x7f0e009c, float:1.8875354E38)
            java.lang.String r7 = "ActionBotDocumentBankStatement"
            java.lang.String r3 = org.telegram.messenger.LocaleController.getString(r7, r3)
            r0.append(r3)
            goto L_0x0b40
        L_0x0afd:
            boolean r7 = r3 instanceof org.telegram.tgnet.TLRPC$TL_secureValueTypeRentalAgreement
            if (r7 == 0) goto L_0x0b0e
            r3 = 2131624101(0x7f0e00a5, float:1.8875372E38)
            java.lang.String r7 = "ActionBotDocumentRentalAgreement"
            java.lang.String r3 = org.telegram.messenger.LocaleController.getString(r7, r3)
            r0.append(r3)
            goto L_0x0b40
        L_0x0b0e:
            boolean r7 = r3 instanceof org.telegram.tgnet.TLRPC$TL_secureValueTypeInternalPassport
            if (r7 == 0) goto L_0x0b1f
            r3 = 2131624097(0x7f0e00a1, float:1.8875364E38)
            java.lang.String r7 = "ActionBotDocumentInternalPassport"
            java.lang.String r3 = org.telegram.messenger.LocaleController.getString(r7, r3)
            r0.append(r3)
            goto L_0x0b40
        L_0x0b1f:
            boolean r7 = r3 instanceof org.telegram.tgnet.TLRPC$TL_secureValueTypePassportRegistration
            if (r7 == 0) goto L_0x0b30
            r3 = 2131624099(0x7f0e00a3, float:1.8875368E38)
            java.lang.String r7 = "ActionBotDocumentPassportRegistration"
            java.lang.String r3 = org.telegram.messenger.LocaleController.getString(r7, r3)
            r0.append(r3)
            goto L_0x0b40
        L_0x0b30:
            boolean r3 = r3 instanceof org.telegram.tgnet.TLRPC$TL_secureValueTypeTemporaryRegistration
            if (r3 == 0) goto L_0x0b40
            r3 = 2131624102(0x7f0e00a6, float:1.8875374E38)
            java.lang.String r7 = "ActionBotDocumentTemporaryRegistration"
            java.lang.String r3 = org.telegram.messenger.LocaleController.getString(r7, r3)
            r0.append(r3)
        L_0x0b40:
            int r2 = r2 + 1
            goto L_0x0a49
        L_0x0b44:
            org.telegram.tgnet.TLRPC$Message r1 = r6.messageOwner
            org.telegram.tgnet.TLRPC$Peer r1 = r1.peer_id
            if (r1 == 0) goto L_0x0b51
            int r1 = r1.user_id
            org.telegram.tgnet.TLRPC$User r7 = r6.getUser(r4, r5, r1)
            goto L_0x0b52
        L_0x0b51:
            r7 = 0
        L_0x0b52:
            r1 = 2131624104(0x7f0e00a8, float:1.8875378E38)
            r2 = 2
            java.lang.Object[] r2 = new java.lang.Object[r2]
            java.lang.String r3 = org.telegram.messenger.UserObject.getFirstName(r7)
            r4 = 0
            r2[r4] = r3
            java.lang.String r0 = r0.toString()
            r3 = 1
            r2[r3] = r0
            java.lang.String r0 = "ActionBotDocuments"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r0, r1, r2)
            r6.messageText = r0
            goto L_0x0d75
        L_0x0b70:
            r3 = 1
            r0 = 2131626348(0x7f0e096c, float:1.887993E38)
            java.lang.Object[] r1 = new java.lang.Object[r3]
            java.lang.String r2 = org.telegram.messenger.UserObject.getUserName(r2)
            r4 = 0
            r1[r4] = r2
            java.lang.String r2 = "NotificationContactJoined"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r2, r0, r1)
            r6.messageText = r0
            goto L_0x0d75
        L_0x0b87:
            r4 = 0
            r6.isRestrictedMessage = r4
            java.util.ArrayList<org.telegram.tgnet.TLRPC$TL_restrictionReason> r0 = r3.restriction_reason
            java.lang.String r0 = org.telegram.messenger.MessagesController.getRestrictionReason(r0)
            boolean r1 = android.text.TextUtils.isEmpty(r0)
            if (r1 != 0) goto L_0x0b9d
            r6.messageText = r0
            r0 = 1
            r6.isRestrictedMessage = r0
            goto L_0x0d75
        L_0x0b9d:
            boolean r0 = r16.isMediaEmpty()
            if (r0 != 0) goto L_0x0d6f
            org.telegram.tgnet.TLRPC$Message r0 = r6.messageOwner
            org.telegram.tgnet.TLRPC$MessageMedia r1 = r0.media
            boolean r2 = r1 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaDice
            if (r2 == 0) goto L_0x0bb3
            java.lang.String r0 = r16.getDiceEmoji()
            r6.messageText = r0
            goto L_0x0d75
        L_0x0bb3:
            boolean r2 = r1 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaPoll
            if (r2 == 0) goto L_0x0bd9
            org.telegram.tgnet.TLRPC$TL_messageMediaPoll r1 = (org.telegram.tgnet.TLRPC$TL_messageMediaPoll) r1
            org.telegram.tgnet.TLRPC$Poll r0 = r1.poll
            boolean r0 = r0.quiz
            if (r0 == 0) goto L_0x0bcc
            r0 = 2131627041(0x7f0e0CLASSNAME, float:1.8881335E38)
            java.lang.String r1 = "QuizPoll"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r1, r0)
            r6.messageText = r0
            goto L_0x0d75
        L_0x0bcc:
            r0 = 2131626929(0x7f0e0bb1, float:1.8881108E38)
            java.lang.String r1 = "Poll"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r1, r0)
            r6.messageText = r0
            goto L_0x0d75
        L_0x0bd9:
            boolean r2 = r1 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaPhoto
            if (r2 == 0) goto L_0x0bff
            int r1 = r1.ttl_seconds
            if (r1 == 0) goto L_0x0bf2
            boolean r0 = r0 instanceof org.telegram.tgnet.TLRPC$TL_message_secret
            if (r0 != 0) goto L_0x0bf2
            r0 = 2131624373(0x7f0e01b5, float:1.8875924E38)
            java.lang.String r1 = "AttachDestructingPhoto"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r1, r0)
            r6.messageText = r0
            goto L_0x0d75
        L_0x0bf2:
            r0 = 2131624390(0x7f0e01c6, float:1.8875958E38)
            java.lang.String r1 = "AttachPhoto"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r1, r0)
            r6.messageText = r0
            goto L_0x0d75
        L_0x0bff:
            boolean r0 = r16.isVideo()
            if (r0 != 0) goto L_0x0d4b
            org.telegram.tgnet.TLRPC$Message r0 = r6.messageOwner
            org.telegram.tgnet.TLRPC$MessageMedia r0 = r0.media
            boolean r0 = r0 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaDocument
            if (r0 == 0) goto L_0x0c1f
            org.telegram.tgnet.TLRPC$Document r0 = r16.getDocument()
            boolean r0 = r0 instanceof org.telegram.tgnet.TLRPC$TL_documentEmpty
            if (r0 == 0) goto L_0x0c1f
            org.telegram.tgnet.TLRPC$Message r0 = r6.messageOwner
            org.telegram.tgnet.TLRPC$MessageMedia r0 = r0.media
            int r0 = r0.ttl_seconds
            if (r0 == 0) goto L_0x0c1f
            goto L_0x0d4b
        L_0x0c1f:
            boolean r0 = r16.isVoice()
            if (r0 == 0) goto L_0x0CLASSNAME
            r0 = 2131624370(0x7f0e01b2, float:1.8875918E38)
            java.lang.String r1 = "AttachAudio"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r1, r0)
            r6.messageText = r0
            goto L_0x0d75
        L_0x0CLASSNAME:
            boolean r0 = r16.isRoundVideo()
            if (r0 == 0) goto L_0x0CLASSNAME
            r0 = 2131624392(0x7f0e01c8, float:1.8875962E38)
            java.lang.String r1 = "AttachRound"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r1, r0)
            r6.messageText = r0
            goto L_0x0d75
        L_0x0CLASSNAME:
            org.telegram.tgnet.TLRPC$Message r0 = r6.messageOwner
            org.telegram.tgnet.TLRPC$MessageMedia r1 = r0.media
            boolean r2 = r1 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaGeo
            if (r2 != 0) goto L_0x0d3f
            boolean r2 = r1 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaVenue
            if (r2 == 0) goto L_0x0CLASSNAME
            goto L_0x0d3f
        L_0x0CLASSNAME:
            boolean r2 = r1 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaGeoLive
            if (r2 == 0) goto L_0x0CLASSNAME
            r0 = 2131624382(0x7f0e01be, float:1.8875942E38)
            java.lang.String r1 = "AttachLiveLocation"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r1, r0)
            r6.messageText = r0
            goto L_0x0d75
        L_0x0CLASSNAME:
            boolean r2 = r1 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaContact
            if (r2 == 0) goto L_0x0c8d
            r0 = 2131624372(0x7f0e01b4, float:1.8875922E38)
            java.lang.String r1 = "AttachContact"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r1, r0)
            r6.messageText = r0
            org.telegram.tgnet.TLRPC$Message r0 = r6.messageOwner
            org.telegram.tgnet.TLRPC$MessageMedia r0 = r0.media
            java.lang.String r0 = r0.vcard
            boolean r0 = android.text.TextUtils.isEmpty(r0)
            if (r0 != 0) goto L_0x0d75
            org.telegram.tgnet.TLRPC$Message r0 = r6.messageOwner
            org.telegram.tgnet.TLRPC$MessageMedia r0 = r0.media
            java.lang.String r0 = r0.vcard
            java.lang.CharSequence r0 = org.telegram.messenger.MessageObject.VCardData.parse(r0)
            r6.vCardData = r0
            goto L_0x0d75
        L_0x0c8d:
            boolean r2 = r1 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaGame
            if (r2 == 0) goto L_0x0CLASSNAME
            java.lang.String r0 = r0.message
            r6.messageText = r0
            goto L_0x0d75
        L_0x0CLASSNAME:
            boolean r0 = r1 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaInvoice
            if (r0 == 0) goto L_0x0ca1
            java.lang.String r0 = r1.description
            r6.messageText = r0
            goto L_0x0d75
        L_0x0ca1:
            boolean r0 = r1 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaUnsupported
            if (r0 == 0) goto L_0x0cb2
            r0 = 2131627737(0x7f0e0ed9, float:1.8882747E38)
            java.lang.String r1 = "UnsupportedMedia"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r1, r0)
            r6.messageText = r0
            goto L_0x0d75
        L_0x0cb2:
            boolean r0 = r1 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaDocument
            if (r0 == 0) goto L_0x0d75
            boolean r0 = r16.isSticker()
            if (r0 != 0) goto L_0x0d0c
            org.telegram.tgnet.TLRPC$Document r0 = r16.getDocument()
            r1 = 1
            boolean r0 = isAnimatedStickerDocument(r0, r1)
            if (r0 == 0) goto L_0x0cc8
            goto L_0x0d0c
        L_0x0cc8:
            boolean r0 = r16.isMusic()
            if (r0 == 0) goto L_0x0cdb
            r0 = 2131624389(0x7f0e01c5, float:1.8875956E38)
            java.lang.String r1 = "AttachMusic"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r1, r0)
            r6.messageText = r0
            goto L_0x0d75
        L_0x0cdb:
            boolean r0 = r16.isGif()
            if (r0 == 0) goto L_0x0cee
            r0 = 2131624377(0x7f0e01b9, float:1.8875932E38)
            java.lang.String r1 = "AttachGif"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r1, r0)
            r6.messageText = r0
            goto L_0x0d75
        L_0x0cee:
            org.telegram.tgnet.TLRPC$Document r0 = r16.getDocument()
            java.lang.String r0 = org.telegram.messenger.FileLoader.getDocumentFileName(r0)
            boolean r1 = android.text.TextUtils.isEmpty(r0)
            if (r1 != 0) goto L_0x0d00
            r6.messageText = r0
            goto L_0x0d75
        L_0x0d00:
            r0 = 2131624375(0x7f0e01b7, float:1.8875928E38)
            java.lang.String r1 = "AttachDocument"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r1, r0)
            r6.messageText = r0
            goto L_0x0d75
        L_0x0d0c:
            java.lang.String r0 = r16.getStickerChar()
            if (r0 == 0) goto L_0x0d33
            int r1 = r0.length()
            if (r1 <= 0) goto L_0x0d33
            r1 = 2
            java.lang.Object[] r1 = new java.lang.Object[r1]
            r2 = 0
            r1[r2] = r0
            r0 = 2131624393(0x7f0e01c9, float:1.8875964E38)
            java.lang.String r2 = "AttachSticker"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r2, r0)
            r2 = 1
            r1[r2] = r0
            java.lang.String r0 = "%s %s"
            java.lang.String r0 = java.lang.String.format(r0, r1)
            r6.messageText = r0
            goto L_0x0d75
        L_0x0d33:
            r0 = 2131624393(0x7f0e01c9, float:1.8875964E38)
            java.lang.String r1 = "AttachSticker"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r1, r0)
            r6.messageText = r0
            goto L_0x0d75
        L_0x0d3f:
            r0 = 2131624386(0x7f0e01c2, float:1.887595E38)
            java.lang.String r1 = "AttachLocation"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r1, r0)
            r6.messageText = r0
            goto L_0x0d75
        L_0x0d4b:
            org.telegram.tgnet.TLRPC$Message r0 = r6.messageOwner
            org.telegram.tgnet.TLRPC$MessageMedia r1 = r0.media
            int r1 = r1.ttl_seconds
            if (r1 == 0) goto L_0x0d63
            boolean r0 = r0 instanceof org.telegram.tgnet.TLRPC$TL_message_secret
            if (r0 != 0) goto L_0x0d63
            r0 = 2131624374(0x7f0e01b6, float:1.8875926E38)
            java.lang.String r1 = "AttachDestructingVideo"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r1, r0)
            r6.messageText = r0
            goto L_0x0d75
        L_0x0d63:
            r0 = 2131624396(0x7f0e01cc, float:1.887597E38)
            java.lang.String r1 = "AttachVideo"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r1, r0)
            r6.messageText = r0
            goto L_0x0d75
        L_0x0d6f:
            org.telegram.tgnet.TLRPC$Message r0 = r6.messageOwner
            java.lang.String r0 = r0.message
            r6.messageText = r0
        L_0x0d75:
            java.lang.CharSequence r0 = r6.messageText
            if (r0 != 0) goto L_0x0d7b
            r6.messageText = r10
        L_0x0d7b:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.messenger.MessageObject.updateMessageText(java.util.AbstractMap, java.util.AbstractMap, android.util.SparseArray, android.util.SparseArray):void");
    }

    public void setType() {
        int i = this.type;
        this.type = 1000;
        this.isRoundVideoCached = 0;
        TLRPC$Message tLRPC$Message = this.messageOwner;
        if ((tLRPC$Message instanceof TLRPC$TL_message) || (tLRPC$Message instanceof TLRPC$TL_messageForwarded_old2)) {
            if (this.isRestrictedMessage) {
                this.type = 0;
            } else if (this.emojiAnimatedSticker != null) {
                if (isSticker()) {
                    this.type = 13;
                } else {
                    this.type = 15;
                }
            } else if (isMediaEmpty()) {
                this.type = 0;
                if (TextUtils.isEmpty(this.messageText) && this.eventId == 0) {
                    this.messageText = "Empty message";
                }
            } else {
                TLRPC$MessageMedia tLRPC$MessageMedia = this.messageOwner.media;
                if (tLRPC$MessageMedia.ttl_seconds == 0 || (!(tLRPC$MessageMedia.photo instanceof TLRPC$TL_photoEmpty) && !(getDocument() instanceof TLRPC$TL_documentEmpty))) {
                    TLRPC$MessageMedia tLRPC$MessageMedia2 = this.messageOwner.media;
                    if (tLRPC$MessageMedia2 instanceof TLRPC$TL_messageMediaDice) {
                        this.type = 15;
                        if (tLRPC$MessageMedia2.document == null) {
                            tLRPC$MessageMedia2.document = new TLRPC$TL_document();
                            TLRPC$Document tLRPC$Document = this.messageOwner.media.document;
                            tLRPC$Document.file_reference = new byte[0];
                            tLRPC$Document.mime_type = "application/x-tgsdice";
                            tLRPC$Document.dc_id = Integer.MIN_VALUE;
                            tLRPC$Document.id = -2147483648L;
                            TLRPC$TL_documentAttributeImageSize tLRPC$TL_documentAttributeImageSize = new TLRPC$TL_documentAttributeImageSize();
                            tLRPC$TL_documentAttributeImageSize.w = 512;
                            tLRPC$TL_documentAttributeImageSize.h = 512;
                            this.messageOwner.media.document.attributes.add(tLRPC$TL_documentAttributeImageSize);
                        }
                    } else if (tLRPC$MessageMedia2 instanceof TLRPC$TL_messageMediaPhoto) {
                        this.type = 1;
                    } else if ((tLRPC$MessageMedia2 instanceof TLRPC$TL_messageMediaGeo) || (tLRPC$MessageMedia2 instanceof TLRPC$TL_messageMediaVenue) || (tLRPC$MessageMedia2 instanceof TLRPC$TL_messageMediaGeoLive)) {
                        this.type = 4;
                    } else if (isRoundVideo()) {
                        this.type = 5;
                    } else if (isVideo()) {
                        this.type = 3;
                    } else if (isVoice()) {
                        this.type = 2;
                    } else if (isMusic()) {
                        this.type = 14;
                    } else {
                        TLRPC$MessageMedia tLRPC$MessageMedia3 = this.messageOwner.media;
                        if (tLRPC$MessageMedia3 instanceof TLRPC$TL_messageMediaContact) {
                            this.type = 12;
                        } else if (tLRPC$MessageMedia3 instanceof TLRPC$TL_messageMediaPoll) {
                            this.type = 17;
                            this.checkedVotes = new ArrayList<>();
                        } else if (tLRPC$MessageMedia3 instanceof TLRPC$TL_messageMediaUnsupported) {
                            this.type = 0;
                        } else if (tLRPC$MessageMedia3 instanceof TLRPC$TL_messageMediaDocument) {
                            TLRPC$Document document = getDocument();
                            if (document == null || document.mime_type == null) {
                                this.type = 9;
                            } else if (isGifDocument(document, hasValidGroupId())) {
                                this.type = 8;
                            } else if (isSticker()) {
                                this.type = 13;
                            } else if (isAnimatedSticker()) {
                                this.type = 15;
                            } else {
                                this.type = 9;
                            }
                        } else if (tLRPC$MessageMedia3 instanceof TLRPC$TL_messageMediaGame) {
                            this.type = 0;
                        } else if (tLRPC$MessageMedia3 instanceof TLRPC$TL_messageMediaInvoice) {
                            this.type = 0;
                        }
                    }
                } else {
                    this.contentType = 1;
                    this.type = 10;
                }
            }
        } else if (tLRPC$Message instanceof TLRPC$TL_messageService) {
            TLRPC$MessageAction tLRPC$MessageAction = tLRPC$Message.action;
            if (tLRPC$MessageAction instanceof TLRPC$TL_messageActionLoginUnknownLocation) {
                this.type = 0;
            } else if ((tLRPC$MessageAction instanceof TLRPC$TL_messageActionChatEditPhoto) || (tLRPC$MessageAction instanceof TLRPC$TL_messageActionUserUpdatedPhoto)) {
                this.contentType = 1;
                this.type = 11;
            } else if (tLRPC$MessageAction instanceof TLRPC$TL_messageEncryptedAction) {
                TLRPC$DecryptedMessageAction tLRPC$DecryptedMessageAction = tLRPC$MessageAction.encryptedAction;
                if ((tLRPC$DecryptedMessageAction instanceof TLRPC$TL_decryptedMessageActionScreenshotMessages) || (tLRPC$DecryptedMessageAction instanceof TLRPC$TL_decryptedMessageActionSetMessageTTL)) {
                    this.contentType = 1;
                    this.type = 10;
                } else {
                    this.contentType = -1;
                    this.type = -1;
                }
            } else if (tLRPC$MessageAction instanceof TLRPC$TL_messageActionHistoryClear) {
                this.contentType = -1;
                this.type = -1;
            } else if (tLRPC$MessageAction instanceof TLRPC$TL_messageActionPhoneCall) {
                this.type = 16;
            } else {
                this.contentType = 1;
                this.type = 10;
            }
        }
        if (i != 1000 && i != this.type) {
            updateMessageText(MessagesController.getInstance(this.currentAccount).getUsers(), MessagesController.getInstance(this.currentAccount).getChats(), (SparseArray<TLRPC$User>) null, (SparseArray<TLRPC$Chat>) null);
            generateThumbs(false);
        }
    }

    public boolean checkLayout() {
        CharSequence charSequence;
        TextPaint textPaint;
        if (!(this.type != 0 || this.messageOwner.peer_id == null || (charSequence = this.messageText) == null || charSequence.length() == 0)) {
            if (this.layoutCreated) {
                if (Math.abs(this.generatedWithMinSize - (AndroidUtilities.isTablet() ? AndroidUtilities.getMinTabletSide() : AndroidUtilities.displaySize.x)) > AndroidUtilities.dp(52.0f) || this.generatedWithDensity != AndroidUtilities.density) {
                    this.layoutCreated = false;
                }
            }
            if (!this.layoutCreated) {
                this.layoutCreated = true;
                int[] iArr = null;
                TLRPC$User user = isFromUser() ? MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(this.messageOwner.from_id.user_id)) : null;
                if (this.messageOwner.media instanceof TLRPC$TL_messageMediaGame) {
                    textPaint = Theme.chat_msgGameTextPaint;
                } else {
                    textPaint = Theme.chat_msgTextPaint;
                }
                if (allowsBigEmoji()) {
                    iArr = new int[1];
                }
                this.messageText = Emoji.replaceEmoji(this.messageText, textPaint.getFontMetricsInt(), AndroidUtilities.dp(20.0f), false, iArr);
                checkEmojiOnly(iArr);
                generateLayout(user);
                return true;
            }
        }
        return false;
    }

    public void resetLayout() {
        this.layoutCreated = false;
    }

    public String getMimeType() {
        TLRPC$Document document = getDocument();
        if (document != null) {
            return document.mime_type;
        }
        TLRPC$MessageMedia tLRPC$MessageMedia = this.messageOwner.media;
        if (tLRPC$MessageMedia instanceof TLRPC$TL_messageMediaInvoice) {
            TLRPC$WebDocument tLRPC$WebDocument = ((TLRPC$TL_messageMediaInvoice) tLRPC$MessageMedia).photo;
            if (tLRPC$WebDocument != null) {
                return tLRPC$WebDocument.mime_type;
            }
            return "";
        } else if (tLRPC$MessageMedia instanceof TLRPC$TL_messageMediaPhoto) {
            return "image/jpeg";
        } else {
            if (!(tLRPC$MessageMedia instanceof TLRPC$TL_messageMediaWebPage) || tLRPC$MessageMedia.webpage.photo == null) {
                return "";
            }
            return "image/jpeg";
        }
    }

    public boolean canPreviewDocument() {
        return canPreviewDocument(getDocument());
    }

    public static boolean isGifDocument(WebFile webFile) {
        return webFile != null && (webFile.mime_type.equals("image/gif") || isNewGifDocument(webFile));
    }

    public static boolean isGifDocument(TLRPC$Document tLRPC$Document) {
        return isGifDocument(tLRPC$Document, false);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:1:0x0002, code lost:
        r0 = r2.mime_type;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean isGifDocument(org.telegram.tgnet.TLRPC$Document r2, boolean r3) {
        /*
            if (r2 == 0) goto L_0x0018
            java.lang.String r0 = r2.mime_type
            if (r0 == 0) goto L_0x0018
            java.lang.String r1 = "image/gif"
            boolean r0 = r0.equals(r1)
            if (r0 == 0) goto L_0x0010
            if (r3 == 0) goto L_0x0016
        L_0x0010:
            boolean r2 = isNewGifDocument((org.telegram.tgnet.TLRPC$Document) r2)
            if (r2 == 0) goto L_0x0018
        L_0x0016:
            r2 = 1
            goto L_0x0019
        L_0x0018:
            r2 = 0
        L_0x0019:
            return r2
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.messenger.MessageObject.isGifDocument(org.telegram.tgnet.TLRPC$Document, boolean):boolean");
    }

    public static boolean isDocumentHasThumb(TLRPC$Document tLRPC$Document) {
        if (tLRPC$Document != null && !tLRPC$Document.thumbs.isEmpty()) {
            int size = tLRPC$Document.thumbs.size();
            for (int i = 0; i < size; i++) {
                TLRPC$PhotoSize tLRPC$PhotoSize = tLRPC$Document.thumbs.get(i);
                if (tLRPC$PhotoSize != null && !(tLRPC$PhotoSize instanceof TLRPC$TL_photoSizeEmpty) && !(tLRPC$PhotoSize.location instanceof TLRPC$TL_fileLocationUnavailable)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean canPreviewDocument(TLRPC$Document tLRPC$Document) {
        String str;
        if (!(tLRPC$Document == null || (str = tLRPC$Document.mime_type) == null)) {
            String lowerCase = str.toLowerCase();
            if ((isDocumentHasThumb(tLRPC$Document) && (lowerCase.equals("image/png") || lowerCase.equals("image/jpg") || lowerCase.equals("image/jpeg"))) || (Build.VERSION.SDK_INT >= 26 && lowerCase.equals("image/heic"))) {
                for (int i = 0; i < tLRPC$Document.attributes.size(); i++) {
                    TLRPC$DocumentAttribute tLRPC$DocumentAttribute = tLRPC$Document.attributes.get(i);
                    if (tLRPC$DocumentAttribute instanceof TLRPC$TL_documentAttributeImageSize) {
                        TLRPC$TL_documentAttributeImageSize tLRPC$TL_documentAttributeImageSize = (TLRPC$TL_documentAttributeImageSize) tLRPC$DocumentAttribute;
                        if (tLRPC$TL_documentAttributeImageSize.w >= 6000 || tLRPC$TL_documentAttributeImageSize.h >= 6000) {
                            return false;
                        }
                        return true;
                    }
                }
            } else if (BuildVars.DEBUG_PRIVATE_VERSION) {
                String documentFileName = FileLoader.getDocumentFileName(tLRPC$Document);
                if ((!documentFileName.startsWith("tg_secret_sticker") || !documentFileName.endsWith("json")) && !documentFileName.endsWith(".svg")) {
                    return false;
                }
                return true;
            }
        }
        return false;
    }

    public static boolean isRoundVideoDocument(TLRPC$Document tLRPC$Document) {
        if (tLRPC$Document != null && "video/mp4".equals(tLRPC$Document.mime_type)) {
            boolean z = false;
            int i = 0;
            int i2 = 0;
            for (int i3 = 0; i3 < tLRPC$Document.attributes.size(); i3++) {
                TLRPC$DocumentAttribute tLRPC$DocumentAttribute = tLRPC$Document.attributes.get(i3);
                if (tLRPC$DocumentAttribute instanceof TLRPC$TL_documentAttributeVideo) {
                    i = tLRPC$DocumentAttribute.w;
                    i2 = tLRPC$DocumentAttribute.h;
                    z = tLRPC$DocumentAttribute.round_message;
                }
            }
            if (!z || i > 1280 || i2 > 1280) {
                return false;
            }
            return true;
        }
        return false;
    }

    public static boolean isNewGifDocument(WebFile webFile) {
        if (webFile != null && "video/mp4".equals(webFile.mime_type)) {
            int i = 0;
            int i2 = 0;
            for (int i3 = 0; i3 < webFile.attributes.size(); i3++) {
                TLRPC$DocumentAttribute tLRPC$DocumentAttribute = webFile.attributes.get(i3);
                if (!(tLRPC$DocumentAttribute instanceof TLRPC$TL_documentAttributeAnimated) && (tLRPC$DocumentAttribute instanceof TLRPC$TL_documentAttributeVideo)) {
                    i = tLRPC$DocumentAttribute.w;
                    i2 = tLRPC$DocumentAttribute.h;
                }
            }
            if (i > 1280 || i2 > 1280) {
                return false;
            }
            return true;
        }
        return false;
    }

    public static boolean isNewGifDocument(TLRPC$Document tLRPC$Document) {
        if (tLRPC$Document != null && "video/mp4".equals(tLRPC$Document.mime_type)) {
            boolean z = false;
            int i = 0;
            int i2 = 0;
            for (int i3 = 0; i3 < tLRPC$Document.attributes.size(); i3++) {
                TLRPC$DocumentAttribute tLRPC$DocumentAttribute = tLRPC$Document.attributes.get(i3);
                if (tLRPC$DocumentAttribute instanceof TLRPC$TL_documentAttributeAnimated) {
                    z = true;
                } else if (tLRPC$DocumentAttribute instanceof TLRPC$TL_documentAttributeVideo) {
                    i = tLRPC$DocumentAttribute.w;
                    i2 = tLRPC$DocumentAttribute.h;
                }
            }
            if (!z || i > 1280 || i2 > 1280) {
                return false;
            }
            return true;
        }
        return false;
    }

    public void generateThumbs(boolean z) {
        ArrayList<TLRPC$PhotoSize> arrayList;
        ArrayList<TLRPC$PhotoSize> arrayList2;
        ArrayList<TLRPC$PhotoSize> arrayList3;
        ArrayList<TLRPC$PhotoSize> arrayList4;
        ArrayList<TLRPC$PhotoSize> arrayList5;
        ArrayList<TLRPC$PhotoSize> arrayList6;
        ArrayList<TLRPC$PhotoSize> arrayList7;
        TLRPC$Message tLRPC$Message = this.messageOwner;
        if (tLRPC$Message instanceof TLRPC$TL_messageService) {
            TLRPC$MessageAction tLRPC$MessageAction = tLRPC$Message.action;
            if (tLRPC$MessageAction instanceof TLRPC$TL_messageActionChatEditPhoto) {
                TLRPC$Photo tLRPC$Photo = tLRPC$MessageAction.photo;
                if (!z) {
                    this.photoThumbs = new ArrayList<>(tLRPC$Photo.sizes);
                } else {
                    ArrayList<TLRPC$PhotoSize> arrayList8 = this.photoThumbs;
                    if (arrayList8 != null && !arrayList8.isEmpty()) {
                        for (int i = 0; i < this.photoThumbs.size(); i++) {
                            TLRPC$PhotoSize tLRPC$PhotoSize = this.photoThumbs.get(i);
                            int i2 = 0;
                            while (true) {
                                if (i2 >= tLRPC$Photo.sizes.size()) {
                                    break;
                                }
                                TLRPC$PhotoSize tLRPC$PhotoSize2 = tLRPC$Photo.sizes.get(i2);
                                if (!(tLRPC$PhotoSize2 instanceof TLRPC$TL_photoSizeEmpty) && tLRPC$PhotoSize2.type.equals(tLRPC$PhotoSize.type)) {
                                    tLRPC$PhotoSize.location = tLRPC$PhotoSize2.location;
                                    break;
                                }
                                i2++;
                            }
                        }
                    }
                }
                if (!(tLRPC$Photo.dc_id == 0 || (arrayList7 = this.photoThumbs) == null)) {
                    int size = arrayList7.size();
                    for (int i3 = 0; i3 < size; i3++) {
                        TLRPC$FileLocation tLRPC$FileLocation = this.photoThumbs.get(i3).location;
                        if (tLRPC$FileLocation != null) {
                            tLRPC$FileLocation.dc_id = tLRPC$Photo.dc_id;
                            tLRPC$FileLocation.file_reference = tLRPC$Photo.file_reference;
                        }
                    }
                }
                this.photoThumbsObject = this.messageOwner.action.photo;
            }
        } else if (this.emojiAnimatedSticker == null) {
            TLRPC$MessageMedia tLRPC$MessageMedia = tLRPC$Message.media;
            if (tLRPC$MessageMedia != null && !(tLRPC$MessageMedia instanceof TLRPC$TL_messageMediaEmpty)) {
                if (tLRPC$MessageMedia instanceof TLRPC$TL_messageMediaPhoto) {
                    TLRPC$Photo tLRPC$Photo2 = tLRPC$MessageMedia.photo;
                    if (!z || !((arrayList5 = this.photoThumbs) == null || arrayList5.size() == tLRPC$Photo2.sizes.size())) {
                        this.photoThumbs = new ArrayList<>(tLRPC$Photo2.sizes);
                    } else {
                        ArrayList<TLRPC$PhotoSize> arrayList9 = this.photoThumbs;
                        if (arrayList9 != null && !arrayList9.isEmpty()) {
                            for (int i4 = 0; i4 < this.photoThumbs.size(); i4++) {
                                TLRPC$PhotoSize tLRPC$PhotoSize3 = this.photoThumbs.get(i4);
                                if (tLRPC$PhotoSize3 != null) {
                                    int i5 = 0;
                                    while (true) {
                                        if (i5 < tLRPC$Photo2.sizes.size()) {
                                            TLRPC$PhotoSize tLRPC$PhotoSize4 = tLRPC$Photo2.sizes.get(i5);
                                            if (tLRPC$PhotoSize4 != null && !(tLRPC$PhotoSize4 instanceof TLRPC$TL_photoSizeEmpty) && tLRPC$PhotoSize4.type.equals(tLRPC$PhotoSize3.type)) {
                                                tLRPC$PhotoSize3.location = tLRPC$PhotoSize4.location;
                                                break;
                                            }
                                            i5++;
                                        } else {
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                    this.photoThumbsObject = this.messageOwner.media.photo;
                } else if (tLRPC$MessageMedia instanceof TLRPC$TL_messageMediaDocument) {
                    TLRPC$Document document = getDocument();
                    if (isDocumentHasThumb(document)) {
                        if (!z || (arrayList4 = this.photoThumbs) == null) {
                            ArrayList<TLRPC$PhotoSize> arrayList10 = new ArrayList<>();
                            this.photoThumbs = arrayList10;
                            arrayList10.addAll(document.thumbs);
                        } else if (!arrayList4.isEmpty()) {
                            updatePhotoSizeLocations(this.photoThumbs, document.thumbs);
                        }
                        this.photoThumbsObject = document;
                    }
                } else if (tLRPC$MessageMedia instanceof TLRPC$TL_messageMediaGame) {
                    TLRPC$Document tLRPC$Document = tLRPC$MessageMedia.game.document;
                    if (tLRPC$Document != null && isDocumentHasThumb(tLRPC$Document)) {
                        if (!z) {
                            ArrayList<TLRPC$PhotoSize> arrayList11 = new ArrayList<>();
                            this.photoThumbs = arrayList11;
                            arrayList11.addAll(tLRPC$Document.thumbs);
                        } else {
                            ArrayList<TLRPC$PhotoSize> arrayList12 = this.photoThumbs;
                            if (arrayList12 != null && !arrayList12.isEmpty()) {
                                updatePhotoSizeLocations(this.photoThumbs, tLRPC$Document.thumbs);
                            }
                        }
                        this.photoThumbsObject = tLRPC$Document;
                    }
                    TLRPC$Photo tLRPC$Photo3 = this.messageOwner.media.game.photo;
                    if (tLRPC$Photo3 != null) {
                        if (!z || (arrayList3 = this.photoThumbs2) == null) {
                            this.photoThumbs2 = new ArrayList<>(tLRPC$Photo3.sizes);
                        } else if (!arrayList3.isEmpty()) {
                            updatePhotoSizeLocations(this.photoThumbs2, tLRPC$Photo3.sizes);
                        }
                        this.photoThumbsObject2 = tLRPC$Photo3;
                    }
                    if (this.photoThumbs == null && (arrayList2 = this.photoThumbs2) != null) {
                        this.photoThumbs = arrayList2;
                        this.photoThumbs2 = null;
                        this.photoThumbsObject = this.photoThumbsObject2;
                        this.photoThumbsObject2 = null;
                    }
                } else if (tLRPC$MessageMedia instanceof TLRPC$TL_messageMediaWebPage) {
                    TLRPC$WebPage tLRPC$WebPage = tLRPC$MessageMedia.webpage;
                    TLRPC$Photo tLRPC$Photo4 = tLRPC$WebPage.photo;
                    TLRPC$Document tLRPC$Document2 = tLRPC$WebPage.document;
                    if (tLRPC$Photo4 != null) {
                        if (!z || (arrayList = this.photoThumbs) == null) {
                            this.photoThumbs = new ArrayList<>(tLRPC$Photo4.sizes);
                        } else if (!arrayList.isEmpty()) {
                            updatePhotoSizeLocations(this.photoThumbs, tLRPC$Photo4.sizes);
                        }
                        this.photoThumbsObject = tLRPC$Photo4;
                    } else if (tLRPC$Document2 != null && isDocumentHasThumb(tLRPC$Document2)) {
                        if (!z) {
                            ArrayList<TLRPC$PhotoSize> arrayList13 = new ArrayList<>();
                            this.photoThumbs = arrayList13;
                            arrayList13.addAll(tLRPC$Document2.thumbs);
                        } else {
                            ArrayList<TLRPC$PhotoSize> arrayList14 = this.photoThumbs;
                            if (arrayList14 != null && !arrayList14.isEmpty()) {
                                updatePhotoSizeLocations(this.photoThumbs, tLRPC$Document2.thumbs);
                            }
                        }
                        this.photoThumbsObject = tLRPC$Document2;
                    }
                }
            }
        } else if (TextUtils.isEmpty(this.emojiAnimatedStickerColor) && isDocumentHasThumb(this.emojiAnimatedSticker)) {
            if (!z || (arrayList6 = this.photoThumbs) == null) {
                ArrayList<TLRPC$PhotoSize> arrayList15 = new ArrayList<>();
                this.photoThumbs = arrayList15;
                arrayList15.addAll(this.emojiAnimatedSticker.thumbs);
            } else if (!arrayList6.isEmpty()) {
                updatePhotoSizeLocations(this.photoThumbs, this.emojiAnimatedSticker.thumbs);
            }
            this.photoThumbsObject = this.emojiAnimatedSticker;
        }
    }

    private static void updatePhotoSizeLocations(ArrayList<TLRPC$PhotoSize> arrayList, ArrayList<TLRPC$PhotoSize> arrayList2) {
        int size = arrayList.size();
        for (int i = 0; i < size; i++) {
            TLRPC$PhotoSize tLRPC$PhotoSize = arrayList.get(i);
            if (tLRPC$PhotoSize != null) {
                int size2 = arrayList2.size();
                int i2 = 0;
                while (true) {
                    if (i2 >= size2) {
                        break;
                    }
                    TLRPC$PhotoSize tLRPC$PhotoSize2 = arrayList2.get(i2);
                    if (!(tLRPC$PhotoSize2 instanceof TLRPC$TL_photoSizeEmpty) && !(tLRPC$PhotoSize2 instanceof TLRPC$TL_photoCachedSize) && tLRPC$PhotoSize2 != null && tLRPC$PhotoSize2.type.equals(tLRPC$PhotoSize.type)) {
                        tLRPC$PhotoSize.location = tLRPC$PhotoSize2.location;
                        break;
                    }
                    i2++;
                }
            }
        }
    }

    public CharSequence replaceWithLink(CharSequence charSequence, String str, ArrayList<Integer> arrayList, AbstractMap<Integer, TLRPC$User> abstractMap, SparseArray<TLRPC$User> sparseArray) {
        if (TextUtils.indexOf(charSequence, str) < 0) {
            return charSequence;
        }
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder("");
        for (int i = 0; i < arrayList.size(); i++) {
            TLRPC$User tLRPC$User = null;
            if (abstractMap != null) {
                tLRPC$User = abstractMap.get(arrayList.get(i));
            } else if (sparseArray != null) {
                tLRPC$User = sparseArray.get(arrayList.get(i).intValue());
            }
            if (tLRPC$User == null) {
                tLRPC$User = MessagesController.getInstance(this.currentAccount).getUser(arrayList.get(i));
            }
            if (tLRPC$User != null) {
                String userName = UserObject.getUserName(tLRPC$User);
                int length = spannableStringBuilder.length();
                if (spannableStringBuilder.length() != 0) {
                    spannableStringBuilder.append(", ");
                }
                spannableStringBuilder.append(userName);
                spannableStringBuilder.setSpan(new URLSpanNoUnderlineBold("" + tLRPC$User.id), length, userName.length() + length, 33);
            }
        }
        return TextUtils.replace(charSequence, new String[]{str}, new CharSequence[]{spannableStringBuilder});
    }

    public static CharSequence replaceWithLink(CharSequence charSequence, String str, TLObject tLObject) {
        String str2;
        String str3;
        int indexOf = TextUtils.indexOf(charSequence, str);
        if (indexOf < 0) {
            return charSequence;
        }
        if (tLObject instanceof TLRPC$User) {
            TLRPC$User tLRPC$User = (TLRPC$User) tLObject;
            str3 = UserObject.getUserName(tLRPC$User);
            str2 = "" + tLRPC$User.id;
        } else if (tLObject instanceof TLRPC$Chat) {
            TLRPC$Chat tLRPC$Chat = (TLRPC$Chat) tLObject;
            str3 = tLRPC$Chat.title;
            str2 = "" + (-tLRPC$Chat.id);
        } else if (tLObject instanceof TLRPC$TL_game) {
            str3 = ((TLRPC$TL_game) tLObject).title;
            str2 = "game";
        } else {
            str2 = "0";
            str3 = "";
        }
        String replace = str3.replace(10, ' ');
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(TextUtils.replace(charSequence, new String[]{str}, new String[]{replace}));
        spannableStringBuilder.setSpan(new URLSpanNoUnderlineBold("" + str2), indexOf, replace.length() + indexOf, 33);
        return spannableStringBuilder;
    }

    public String getExtension() {
        String fileName = getFileName();
        int lastIndexOf = fileName.lastIndexOf(46);
        String substring = lastIndexOf != -1 ? fileName.substring(lastIndexOf + 1) : null;
        if (substring == null || substring.length() == 0) {
            substring = getDocument().mime_type;
        }
        if (substring == null) {
            substring = "";
        }
        return substring.toUpperCase();
    }

    public String getFileName() {
        TLRPC$PhotoSize closestPhotoSizeWithSize;
        TLRPC$MessageMedia tLRPC$MessageMedia = this.messageOwner.media;
        if (tLRPC$MessageMedia instanceof TLRPC$TL_messageMediaDocument) {
            return FileLoader.getAttachFileName(getDocument());
        }
        if (!(tLRPC$MessageMedia instanceof TLRPC$TL_messageMediaPhoto)) {
            return tLRPC$MessageMedia instanceof TLRPC$TL_messageMediaWebPage ? FileLoader.getAttachFileName(tLRPC$MessageMedia.webpage.document) : "";
        }
        ArrayList<TLRPC$PhotoSize> arrayList = tLRPC$MessageMedia.photo.sizes;
        if (arrayList.size() <= 0 || (closestPhotoSizeWithSize = FileLoader.getClosestPhotoSizeWithSize(arrayList, AndroidUtilities.getPhotoSize())) == null) {
            return "";
        }
        return FileLoader.getAttachFileName(closestPhotoSizeWithSize);
    }

    public int getMediaType() {
        if (isVideo()) {
            return 2;
        }
        if (isVoice()) {
            return 1;
        }
        TLRPC$MessageMedia tLRPC$MessageMedia = this.messageOwner.media;
        if (tLRPC$MessageMedia instanceof TLRPC$TL_messageMediaDocument) {
            return 3;
        }
        return tLRPC$MessageMedia instanceof TLRPC$TL_messageMediaPhoto ? 0 : 4;
    }

    private static boolean containsUrls(CharSequence charSequence) {
        if (charSequence != null && charSequence.length() >= 2 && charSequence.length() <= 20480) {
            int length = charSequence.length();
            int i = 0;
            int i2 = 0;
            int i3 = 0;
            int i4 = 0;
            char c = 0;
            while (i < length) {
                char charAt = charSequence.charAt(i);
                if (charAt >= '0' && charAt <= '9') {
                    i2++;
                    if (i2 >= 6) {
                        return true;
                    }
                    i3 = 0;
                    i4 = 0;
                } else if (charAt == ' ' || i2 <= 0) {
                    i2 = 0;
                }
                if ((charAt != '@' && charAt != '#' && charAt != '/' && charAt != '$') || i != 0) {
                    if (i != 0) {
                        int i5 = i - 1;
                        if (charSequence.charAt(i5) != ' ') {
                            if (charSequence.charAt(i5) == 10) {
                            }
                        }
                    }
                    if (charAt != ':') {
                        if (charAt != '/') {
                            if (charAt == '.') {
                                if (i4 == 0 && c != ' ') {
                                    i4++;
                                }
                            } else if (charAt != ' ' && c == '.' && i4 == 1) {
                                return true;
                            }
                            i4 = 0;
                        } else if (i3 == 2) {
                            return true;
                        } else {
                            if (i3 == 1) {
                                i3++;
                            }
                        }
                        i++;
                        c = charAt;
                    } else if (i3 == 0) {
                        i3 = 1;
                        i++;
                        c = charAt;
                    }
                    i3 = 0;
                    i++;
                    c = charAt;
                }
                return true;
            }
        }
        return false;
    }

    /* JADX WARNING: Removed duplicated region for block: B:33:0x008e  */
    /* JADX WARNING: Removed duplicated region for block: B:46:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void generateLinkDescription() {
        /*
            r10 = this;
            java.lang.CharSequence r0 = r10.linkDescription
            if (r0 == 0) goto L_0x0005
            return
        L_0x0005:
            org.telegram.tgnet.TLRPC$Message r0 = r10.messageOwner
            org.telegram.tgnet.TLRPC$MessageMedia r0 = r0.media
            boolean r1 = r0 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaWebPage
            r2 = 1
            r3 = 0
            if (r1 == 0) goto L_0x0050
            org.telegram.tgnet.TLRPC$WebPage r1 = r0.webpage
            boolean r4 = r1 instanceof org.telegram.tgnet.TLRPC$TL_webPage
            if (r4 == 0) goto L_0x0050
            java.lang.String r1 = r1.description
            if (r1 == 0) goto L_0x0050
            android.text.Spannable$Factory r0 = android.text.Spannable.Factory.getInstance()
            org.telegram.tgnet.TLRPC$Message r1 = r10.messageOwner
            org.telegram.tgnet.TLRPC$MessageMedia r1 = r1.media
            org.telegram.tgnet.TLRPC$WebPage r1 = r1.webpage
            java.lang.String r1 = r1.description
            android.text.Spannable r0 = r0.newSpannable(r1)
            r10.linkDescription = r0
            org.telegram.tgnet.TLRPC$Message r0 = r10.messageOwner
            org.telegram.tgnet.TLRPC$MessageMedia r0 = r0.media
            org.telegram.tgnet.TLRPC$WebPage r0 = r0.webpage
            java.lang.String r0 = r0.site_name
            if (r0 == 0) goto L_0x0039
            java.lang.String r0 = r0.toLowerCase()
        L_0x0039:
            java.lang.String r1 = "instagram"
            boolean r1 = r1.equals(r0)
            if (r1 == 0) goto L_0x0043
            r0 = 1
            goto L_0x004e
        L_0x0043:
            java.lang.String r1 = "twitter"
            boolean r0 = r1.equals(r0)
            if (r0 == 0) goto L_0x004d
            r0 = 2
            goto L_0x004e
        L_0x004d:
            r0 = 0
        L_0x004e:
            r7 = r0
            goto L_0x0086
        L_0x0050:
            boolean r1 = r0 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaGame
            if (r1 == 0) goto L_0x006d
            org.telegram.tgnet.TLRPC$TL_game r1 = r0.game
            java.lang.String r1 = r1.description
            if (r1 == 0) goto L_0x006d
            android.text.Spannable$Factory r0 = android.text.Spannable.Factory.getInstance()
            org.telegram.tgnet.TLRPC$Message r1 = r10.messageOwner
            org.telegram.tgnet.TLRPC$MessageMedia r1 = r1.media
            org.telegram.tgnet.TLRPC$TL_game r1 = r1.game
            java.lang.String r1 = r1.description
            android.text.Spannable r0 = r0.newSpannable(r1)
            r10.linkDescription = r0
            goto L_0x0085
        L_0x006d:
            boolean r1 = r0 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaInvoice
            if (r1 == 0) goto L_0x0085
            java.lang.String r0 = r0.description
            if (r0 == 0) goto L_0x0085
            android.text.Spannable$Factory r0 = android.text.Spannable.Factory.getInstance()
            org.telegram.tgnet.TLRPC$Message r1 = r10.messageOwner
            org.telegram.tgnet.TLRPC$MessageMedia r1 = r1.media
            java.lang.String r1 = r1.description
            android.text.Spannable r0 = r0.newSpannable(r1)
            r10.linkDescription = r0
        L_0x0085:
            r7 = 0
        L_0x0086:
            java.lang.CharSequence r0 = r10.linkDescription
            boolean r0 = android.text.TextUtils.isEmpty(r0)
            if (r0 != 0) goto L_0x00d1
            java.lang.CharSequence r0 = r10.linkDescription
            boolean r0 = containsUrls(r0)
            if (r0 == 0) goto L_0x00a2
            java.lang.CharSequence r0 = r10.linkDescription     // Catch:{ Exception -> 0x009e }
            android.text.Spannable r0 = (android.text.Spannable) r0     // Catch:{ Exception -> 0x009e }
            org.telegram.messenger.AndroidUtilities.addLinks(r0, r2)     // Catch:{ Exception -> 0x009e }
            goto L_0x00a2
        L_0x009e:
            r0 = move-exception
            org.telegram.messenger.FileLog.e((java.lang.Throwable) r0)
        L_0x00a2:
            java.lang.CharSequence r0 = r10.linkDescription
            android.text.TextPaint r1 = org.telegram.ui.ActionBar.Theme.chat_msgTextPaint
            android.graphics.Paint$FontMetricsInt r1 = r1.getFontMetricsInt()
            r2 = 1101004800(0x41a00000, float:20.0)
            int r2 = org.telegram.messenger.AndroidUtilities.dp(r2)
            java.lang.CharSequence r0 = org.telegram.messenger.Emoji.replaceEmoji(r0, r1, r2, r3)
            r10.linkDescription = r0
            if (r7 == 0) goto L_0x00d1
            boolean r0 = r0 instanceof android.text.Spannable
            if (r0 != 0) goto L_0x00c5
            android.text.SpannableStringBuilder r0 = new android.text.SpannableStringBuilder
            java.lang.CharSequence r1 = r10.linkDescription
            r0.<init>(r1)
            r10.linkDescription = r0
        L_0x00c5:
            boolean r4 = r10.isOutOwner()
            java.lang.CharSequence r5 = r10.linkDescription
            r6 = 0
            r8 = 0
            r9 = 0
            addUrlsByPattern(r4, r5, r6, r7, r8, r9)
        L_0x00d1:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.messenger.MessageObject.generateLinkDescription():void");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:32:0x0079, code lost:
        if (r10.messageOwner.send_state == 0) goto L_0x007b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x007f, code lost:
        if (r10.messageOwner.id >= 0) goto L_0x0082;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void generateCaption() {
        /*
            r10 = this;
            java.lang.CharSequence r0 = r10.caption
            if (r0 != 0) goto L_0x00de
            boolean r0 = r10.isRoundVideo()
            if (r0 == 0) goto L_0x000c
            goto L_0x00de
        L_0x000c:
            boolean r0 = r10.isMediaEmpty()
            if (r0 != 0) goto L_0x00de
            org.telegram.tgnet.TLRPC$Message r0 = r10.messageOwner
            org.telegram.tgnet.TLRPC$MessageMedia r1 = r0.media
            boolean r1 = r1 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaGame
            if (r1 != 0) goto L_0x00de
            java.lang.String r0 = r0.message
            boolean r0 = android.text.TextUtils.isEmpty(r0)
            if (r0 != 0) goto L_0x00de
            org.telegram.tgnet.TLRPC$Message r0 = r10.messageOwner
            java.lang.String r0 = r0.message
            android.text.TextPaint r1 = org.telegram.ui.ActionBar.Theme.chat_msgTextPaint
            android.graphics.Paint$FontMetricsInt r1 = r1.getFontMetricsInt()
            r2 = 1101004800(0x41a00000, float:20.0)
            int r2 = org.telegram.messenger.AndroidUtilities.dp(r2)
            r3 = 0
            java.lang.CharSequence r0 = org.telegram.messenger.Emoji.replaceEmoji(r0, r1, r2, r3)
            r10.caption = r0
            org.telegram.tgnet.TLRPC$Message r0 = r10.messageOwner
            int r1 = r0.send_state
            r2 = 1
            if (r1 == 0) goto L_0x0042
            r0 = 0
            goto L_0x0049
        L_0x0042:
            java.util.ArrayList<org.telegram.tgnet.TLRPC$MessageEntity> r0 = r0.entities
            boolean r0 = r0.isEmpty()
            r0 = r0 ^ r2
        L_0x0049:
            if (r0 != 0) goto L_0x0082
            long r0 = r10.eventId
            r4 = 0
            int r6 = (r0 > r4 ? 1 : (r0 == r4 ? 0 : -1))
            if (r6 != 0) goto L_0x0081
            org.telegram.tgnet.TLRPC$Message r0 = r10.messageOwner
            org.telegram.tgnet.TLRPC$MessageMedia r0 = r0.media
            boolean r1 = r0 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaPhoto_old
            if (r1 != 0) goto L_0x0081
            boolean r1 = r0 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaPhoto_layer68
            if (r1 != 0) goto L_0x0081
            boolean r1 = r0 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaPhoto_layer74
            if (r1 != 0) goto L_0x0081
            boolean r1 = r0 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaDocument_old
            if (r1 != 0) goto L_0x0081
            boolean r1 = r0 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaDocument_layer68
            if (r1 != 0) goto L_0x0081
            boolean r0 = r0 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaDocument_layer74
            if (r0 != 0) goto L_0x0081
            boolean r0 = r10.isOut()
            if (r0 == 0) goto L_0x007b
            org.telegram.tgnet.TLRPC$Message r0 = r10.messageOwner
            int r0 = r0.send_state
            if (r0 != 0) goto L_0x0081
        L_0x007b:
            org.telegram.tgnet.TLRPC$Message r0 = r10.messageOwner
            int r0 = r0.id
            if (r0 >= 0) goto L_0x0082
        L_0x0081:
            r3 = 1
        L_0x0082:
            if (r3 == 0) goto L_0x00a6
            java.lang.CharSequence r0 = r10.caption
            boolean r0 = containsUrls(r0)
            if (r0 == 0) goto L_0x0099
            java.lang.CharSequence r0 = r10.caption     // Catch:{ Exception -> 0x0095 }
            android.text.Spannable r0 = (android.text.Spannable) r0     // Catch:{ Exception -> 0x0095 }
            r1 = 5
            org.telegram.messenger.AndroidUtilities.addLinks(r0, r1)     // Catch:{ Exception -> 0x0095 }
            goto L_0x0099
        L_0x0095:
            r0 = move-exception
            org.telegram.messenger.FileLog.e((java.lang.Throwable) r0)
        L_0x0099:
            boolean r4 = r10.isOutOwner()
            java.lang.CharSequence r5 = r10.caption
            r6 = 1
            r7 = 0
            r8 = 0
            r9 = 1
            addUrlsByPattern(r4, r5, r6, r7, r8, r9)
        L_0x00a6:
            java.lang.CharSequence r0 = r10.caption
            r10.addEntitiesToText(r0, r3)
            boolean r0 = r10.isVideo()
            if (r0 == 0) goto L_0x00c2
            boolean r1 = r10.isOutOwner()
            java.lang.CharSequence r2 = r10.caption
            r3 = 1
            r4 = 3
            int r5 = r10.getDuration()
            r6 = 0
            addUrlsByPattern(r1, r2, r3, r4, r5, r6)
            goto L_0x00de
        L_0x00c2:
            boolean r0 = r10.isMusic()
            if (r0 != 0) goto L_0x00ce
            boolean r0 = r10.isVoice()
            if (r0 == 0) goto L_0x00de
        L_0x00ce:
            boolean r1 = r10.isOutOwner()
            java.lang.CharSequence r2 = r10.caption
            r3 = 1
            r4 = 4
            int r5 = r10.getDuration()
            r6 = 0
            addUrlsByPattern(r1, r2, r3, r4, r5, r6)
        L_0x00de:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.messenger.MessageObject.generateCaption():void");
    }

    /* JADX WARNING: Removed duplicated region for block: B:77:0x01d7 A[ADDED_TO_REGION, Catch:{ Exception -> 0x01f0 }] */
    /* JADX WARNING: Removed duplicated region for block: B:91:0x0049 A[SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void addUrlsByPattern(boolean r16, java.lang.CharSequence r17, boolean r18, int r19, int r20, boolean r21) {
        /*
            r0 = r17
            r1 = r19
            r2 = 4
            r3 = 3
            r4 = 1
            if (r1 == r3) goto L_0x0034
            if (r1 != r2) goto L_0x000c
            goto L_0x0034
        L_0x000c:
            if (r1 != r4) goto L_0x0021
            java.util.regex.Pattern r5 = instagramUrlPattern     // Catch:{ Exception -> 0x01f0 }
            if (r5 != 0) goto L_0x001a
            java.lang.String r5 = "(^|\\s|\\()@[a-zA-Z\\d_.]{1,32}|(^|\\s|\\()#[\\w.]+"
            java.util.regex.Pattern r5 = java.util.regex.Pattern.compile(r5)     // Catch:{ Exception -> 0x01f0 }
            instagramUrlPattern = r5     // Catch:{ Exception -> 0x01f0 }
        L_0x001a:
            java.util.regex.Pattern r5 = instagramUrlPattern     // Catch:{ Exception -> 0x01f0 }
            java.util.regex.Matcher r5 = r5.matcher(r0)     // Catch:{ Exception -> 0x01f0 }
            goto L_0x0046
        L_0x0021:
            java.util.regex.Pattern r5 = urlPattern     // Catch:{ Exception -> 0x01f0 }
            if (r5 != 0) goto L_0x002d
            java.lang.String r5 = "(^|\\s)/[a-zA-Z@\\d_]{1,255}|(^|\\s|\\()@[a-zA-Z\\d_]{1,32}|(^|\\s|\\()#[^0-9][\\w.]+|(^|\\s)\\$[A-Z]{3,8}([ ,.]|$)"
            java.util.regex.Pattern r5 = java.util.regex.Pattern.compile(r5)     // Catch:{ Exception -> 0x01f0 }
            urlPattern = r5     // Catch:{ Exception -> 0x01f0 }
        L_0x002d:
            java.util.regex.Pattern r5 = urlPattern     // Catch:{ Exception -> 0x01f0 }
            java.util.regex.Matcher r5 = r5.matcher(r0)     // Catch:{ Exception -> 0x01f0 }
            goto L_0x0046
        L_0x0034:
            java.util.regex.Pattern r5 = videoTimeUrlPattern     // Catch:{ Exception -> 0x01f0 }
            if (r5 != 0) goto L_0x0040
            java.lang.String r5 = "\\b(?:(\\d{1,2}):)?(\\d{1,3}):([0-5][0-9])\\b"
            java.util.regex.Pattern r5 = java.util.regex.Pattern.compile(r5)     // Catch:{ Exception -> 0x01f0 }
            videoTimeUrlPattern = r5     // Catch:{ Exception -> 0x01f0 }
        L_0x0040:
            java.util.regex.Pattern r5 = videoTimeUrlPattern     // Catch:{ Exception -> 0x01f0 }
            java.util.regex.Matcher r5 = r5.matcher(r0)     // Catch:{ Exception -> 0x01f0 }
        L_0x0046:
            r6 = r0
            android.text.Spannable r6 = (android.text.Spannable) r6     // Catch:{ Exception -> 0x01f0 }
        L_0x0049:
            boolean r7 = r5.find()     // Catch:{ Exception -> 0x01f0 }
            if (r7 == 0) goto L_0x01f4
            int r7 = r5.start()     // Catch:{ Exception -> 0x01f0 }
            int r8 = r5.end()     // Catch:{ Exception -> 0x01f0 }
            r9 = 0
            r10 = 0
            r11 = 2
            if (r1 == r3) goto L_0x0141
            if (r1 != r2) goto L_0x0060
            goto L_0x0141
        L_0x0060:
            char r12 = r0.charAt(r7)     // Catch:{ Exception -> 0x01f0 }
            r13 = 47
            r14 = 35
            r15 = 64
            if (r1 == 0) goto L_0x007b
            if (r12 == r15) goto L_0x0072
            if (r12 == r14) goto L_0x0072
            int r7 = r7 + 1
        L_0x0072:
            char r12 = r0.charAt(r7)     // Catch:{ Exception -> 0x01f0 }
            if (r12 == r15) goto L_0x0087
            if (r12 == r14) goto L_0x0087
            goto L_0x0049
        L_0x007b:
            if (r12 == r15) goto L_0x0087
            if (r12 == r14) goto L_0x0087
            if (r12 == r13) goto L_0x0087
            r14 = 36
            if (r12 == r14) goto L_0x0087
            int r7 = r7 + 1
        L_0x0087:
            if (r1 != r4) goto L_0x00ce
            if (r12 != r15) goto L_0x00ad
            org.telegram.ui.Components.URLSpanNoUnderline r9 = new org.telegram.ui.Components.URLSpanNoUnderline     // Catch:{ Exception -> 0x01f0 }
            java.lang.StringBuilder r11 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x01f0 }
            r11.<init>()     // Catch:{ Exception -> 0x01f0 }
            java.lang.String r12 = "https://instagram.com/"
            r11.append(r12)     // Catch:{ Exception -> 0x01f0 }
            int r12 = r7 + 1
            java.lang.CharSequence r12 = r0.subSequence(r12, r8)     // Catch:{ Exception -> 0x01f0 }
            java.lang.String r12 = r12.toString()     // Catch:{ Exception -> 0x01f0 }
            r11.append(r12)     // Catch:{ Exception -> 0x01f0 }
            java.lang.String r11 = r11.toString()     // Catch:{ Exception -> 0x01f0 }
            r9.<init>(r11)     // Catch:{ Exception -> 0x01f0 }
            goto L_0x012e
        L_0x00ad:
            org.telegram.ui.Components.URLSpanNoUnderline r9 = new org.telegram.ui.Components.URLSpanNoUnderline     // Catch:{ Exception -> 0x01f0 }
            java.lang.StringBuilder r11 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x01f0 }
            r11.<init>()     // Catch:{ Exception -> 0x01f0 }
            java.lang.String r12 = "https://www.instagram.com/explore/tags/"
            r11.append(r12)     // Catch:{ Exception -> 0x01f0 }
            int r12 = r7 + 1
            java.lang.CharSequence r12 = r0.subSequence(r12, r8)     // Catch:{ Exception -> 0x01f0 }
            java.lang.String r12 = r12.toString()     // Catch:{ Exception -> 0x01f0 }
            r11.append(r12)     // Catch:{ Exception -> 0x01f0 }
            java.lang.String r11 = r11.toString()     // Catch:{ Exception -> 0x01f0 }
            r9.<init>(r11)     // Catch:{ Exception -> 0x01f0 }
            goto L_0x012e
        L_0x00ce:
            if (r1 != r11) goto L_0x0114
            if (r12 != r15) goto L_0x00f3
            org.telegram.ui.Components.URLSpanNoUnderline r9 = new org.telegram.ui.Components.URLSpanNoUnderline     // Catch:{ Exception -> 0x01f0 }
            java.lang.StringBuilder r11 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x01f0 }
            r11.<init>()     // Catch:{ Exception -> 0x01f0 }
            java.lang.String r12 = "https://twitter.com/"
            r11.append(r12)     // Catch:{ Exception -> 0x01f0 }
            int r12 = r7 + 1
            java.lang.CharSequence r12 = r0.subSequence(r12, r8)     // Catch:{ Exception -> 0x01f0 }
            java.lang.String r12 = r12.toString()     // Catch:{ Exception -> 0x01f0 }
            r11.append(r12)     // Catch:{ Exception -> 0x01f0 }
            java.lang.String r11 = r11.toString()     // Catch:{ Exception -> 0x01f0 }
            r9.<init>(r11)     // Catch:{ Exception -> 0x01f0 }
            goto L_0x012e
        L_0x00f3:
            org.telegram.ui.Components.URLSpanNoUnderline r9 = new org.telegram.ui.Components.URLSpanNoUnderline     // Catch:{ Exception -> 0x01f0 }
            java.lang.StringBuilder r11 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x01f0 }
            r11.<init>()     // Catch:{ Exception -> 0x01f0 }
            java.lang.String r12 = "https://twitter.com/hashtag/"
            r11.append(r12)     // Catch:{ Exception -> 0x01f0 }
            int r12 = r7 + 1
            java.lang.CharSequence r12 = r0.subSequence(r12, r8)     // Catch:{ Exception -> 0x01f0 }
            java.lang.String r12 = r12.toString()     // Catch:{ Exception -> 0x01f0 }
            r11.append(r12)     // Catch:{ Exception -> 0x01f0 }
            java.lang.String r11 = r11.toString()     // Catch:{ Exception -> 0x01f0 }
            r9.<init>(r11)     // Catch:{ Exception -> 0x01f0 }
            goto L_0x012e
        L_0x0114:
            char r11 = r0.charAt(r7)     // Catch:{ Exception -> 0x01f0 }
            if (r11 != r13) goto L_0x0133
            if (r18 == 0) goto L_0x012e
            org.telegram.ui.Components.URLSpanBotCommand r9 = new org.telegram.ui.Components.URLSpanBotCommand     // Catch:{ Exception -> 0x01f0 }
            java.lang.CharSequence r11 = r0.subSequence(r7, r8)     // Catch:{ Exception -> 0x01f0 }
            java.lang.String r11 = r11.toString()     // Catch:{ Exception -> 0x01f0 }
            if (r16 == 0) goto L_0x012a
            r12 = 1
            goto L_0x012b
        L_0x012a:
            r12 = 0
        L_0x012b:
            r9.<init>(r11, r12)     // Catch:{ Exception -> 0x01f0 }
        L_0x012e:
            r11 = r9
            r9 = r20
            goto L_0x01d5
        L_0x0133:
            org.telegram.ui.Components.URLSpanNoUnderline r9 = new org.telegram.ui.Components.URLSpanNoUnderline     // Catch:{ Exception -> 0x01f0 }
            java.lang.CharSequence r11 = r0.subSequence(r7, r8)     // Catch:{ Exception -> 0x01f0 }
            java.lang.String r11 = r11.toString()     // Catch:{ Exception -> 0x01f0 }
            r9.<init>(r11)     // Catch:{ Exception -> 0x01f0 }
            goto L_0x012e
        L_0x0141:
            java.lang.Class<android.text.style.URLSpan> r9 = android.text.style.URLSpan.class
            java.lang.Object[] r9 = r6.getSpans(r7, r8, r9)     // Catch:{ Exception -> 0x01f0 }
            android.text.style.URLSpan[] r9 = (android.text.style.URLSpan[]) r9     // Catch:{ Exception -> 0x01f0 }
            if (r9 == 0) goto L_0x0150
            int r9 = r9.length     // Catch:{ Exception -> 0x01f0 }
            if (r9 <= 0) goto L_0x0150
            goto L_0x0049
        L_0x0150:
            r5.groupCount()     // Catch:{ Exception -> 0x01f0 }
            int r9 = r5.start(r4)     // Catch:{ Exception -> 0x01f0 }
            int r12 = r5.end(r4)     // Catch:{ Exception -> 0x01f0 }
            int r13 = r5.start(r11)     // Catch:{ Exception -> 0x01f0 }
            int r11 = r5.end(r11)     // Catch:{ Exception -> 0x01f0 }
            int r14 = r5.start(r3)     // Catch:{ Exception -> 0x01f0 }
            int r15 = r5.end(r3)     // Catch:{ Exception -> 0x01f0 }
            java.lang.CharSequence r11 = r0.subSequence(r13, r11)     // Catch:{ Exception -> 0x01f0 }
            java.lang.Integer r11 = org.telegram.messenger.Utilities.parseInt(r11)     // Catch:{ Exception -> 0x01f0 }
            int r11 = r11.intValue()     // Catch:{ Exception -> 0x01f0 }
            java.lang.CharSequence r13 = r0.subSequence(r14, r15)     // Catch:{ Exception -> 0x01f0 }
            java.lang.Integer r13 = org.telegram.messenger.Utilities.parseInt(r13)     // Catch:{ Exception -> 0x01f0 }
            int r13 = r13.intValue()     // Catch:{ Exception -> 0x01f0 }
            if (r9 < 0) goto L_0x0194
            if (r12 < 0) goto L_0x0194
            java.lang.CharSequence r9 = r0.subSequence(r9, r12)     // Catch:{ Exception -> 0x01f0 }
            java.lang.Integer r9 = org.telegram.messenger.Utilities.parseInt(r9)     // Catch:{ Exception -> 0x01f0 }
            int r9 = r9.intValue()     // Catch:{ Exception -> 0x01f0 }
            goto L_0x0195
        L_0x0194:
            r9 = -1
        L_0x0195:
            int r11 = r11 * 60
            int r13 = r13 + r11
            if (r9 <= 0) goto L_0x019f
            int r9 = r9 * 60
            int r9 = r9 * 60
            int r13 = r13 + r9
        L_0x019f:
            r9 = r20
            if (r13 <= r9) goto L_0x01a5
            goto L_0x0049
        L_0x01a5:
            if (r1 != r3) goto L_0x01bf
            org.telegram.ui.Components.URLSpanNoUnderline r11 = new org.telegram.ui.Components.URLSpanNoUnderline     // Catch:{ Exception -> 0x01f0 }
            java.lang.StringBuilder r12 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x01f0 }
            r12.<init>()     // Catch:{ Exception -> 0x01f0 }
            java.lang.String r14 = "video?"
            r12.append(r14)     // Catch:{ Exception -> 0x01f0 }
            r12.append(r13)     // Catch:{ Exception -> 0x01f0 }
            java.lang.String r12 = r12.toString()     // Catch:{ Exception -> 0x01f0 }
            r11.<init>(r12)     // Catch:{ Exception -> 0x01f0 }
            goto L_0x01d5
        L_0x01bf:
            org.telegram.ui.Components.URLSpanNoUnderline r11 = new org.telegram.ui.Components.URLSpanNoUnderline     // Catch:{ Exception -> 0x01f0 }
            java.lang.StringBuilder r12 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x01f0 }
            r12.<init>()     // Catch:{ Exception -> 0x01f0 }
            java.lang.String r14 = "audio?"
            r12.append(r14)     // Catch:{ Exception -> 0x01f0 }
            r12.append(r13)     // Catch:{ Exception -> 0x01f0 }
            java.lang.String r12 = r12.toString()     // Catch:{ Exception -> 0x01f0 }
            r11.<init>(r12)     // Catch:{ Exception -> 0x01f0 }
        L_0x01d5:
            if (r11 == 0) goto L_0x0049
            if (r21 == 0) goto L_0x01eb
            java.lang.Class<android.text.style.ClickableSpan> r12 = android.text.style.ClickableSpan.class
            java.lang.Object[] r12 = r6.getSpans(r7, r8, r12)     // Catch:{ Exception -> 0x01f0 }
            android.text.style.ClickableSpan[] r12 = (android.text.style.ClickableSpan[]) r12     // Catch:{ Exception -> 0x01f0 }
            if (r12 == 0) goto L_0x01eb
            int r13 = r12.length     // Catch:{ Exception -> 0x01f0 }
            if (r13 <= 0) goto L_0x01eb
            r12 = r12[r10]     // Catch:{ Exception -> 0x01f0 }
            r6.removeSpan(r12)     // Catch:{ Exception -> 0x01f0 }
        L_0x01eb:
            r6.setSpan(r11, r7, r8, r10)     // Catch:{ Exception -> 0x01f0 }
            goto L_0x0049
        L_0x01f0:
            r0 = move-exception
            org.telegram.messenger.FileLog.e((java.lang.Throwable) r0)
        L_0x01f4:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.messenger.MessageObject.addUrlsByPattern(boolean, java.lang.CharSequence, boolean, int, int, boolean):void");
    }

    public static int[] getWebDocumentWidthAndHeight(TLRPC$WebDocument tLRPC$WebDocument) {
        if (tLRPC$WebDocument == null) {
            return null;
        }
        int size = tLRPC$WebDocument.attributes.size();
        int i = 0;
        while (i < size) {
            TLRPC$DocumentAttribute tLRPC$DocumentAttribute = tLRPC$WebDocument.attributes.get(i);
            if (tLRPC$DocumentAttribute instanceof TLRPC$TL_documentAttributeImageSize) {
                return new int[]{tLRPC$DocumentAttribute.w, tLRPC$DocumentAttribute.h};
            } else if (tLRPC$DocumentAttribute instanceof TLRPC$TL_documentAttributeVideo) {
                return new int[]{tLRPC$DocumentAttribute.w, tLRPC$DocumentAttribute.h};
            } else {
                i++;
            }
        }
        return null;
    }

    public static int getWebDocumentDuration(TLRPC$WebDocument tLRPC$WebDocument) {
        if (tLRPC$WebDocument == null) {
            return 0;
        }
        int size = tLRPC$WebDocument.attributes.size();
        for (int i = 0; i < size; i++) {
            TLRPC$DocumentAttribute tLRPC$DocumentAttribute = tLRPC$WebDocument.attributes.get(i);
            if (tLRPC$DocumentAttribute instanceof TLRPC$TL_documentAttributeVideo) {
                return tLRPC$DocumentAttribute.duration;
            }
            if (tLRPC$DocumentAttribute instanceof TLRPC$TL_documentAttributeAudio) {
                return tLRPC$DocumentAttribute.duration;
            }
        }
        return 0;
    }

    public static int[] getInlineResultWidthAndHeight(TLRPC$BotInlineResult tLRPC$BotInlineResult) {
        int[] webDocumentWidthAndHeight = getWebDocumentWidthAndHeight(tLRPC$BotInlineResult.content);
        if (webDocumentWidthAndHeight != null) {
            return webDocumentWidthAndHeight;
        }
        int[] webDocumentWidthAndHeight2 = getWebDocumentWidthAndHeight(tLRPC$BotInlineResult.thumb);
        if (webDocumentWidthAndHeight2 == null) {
            return new int[]{0, 0};
        }
        return webDocumentWidthAndHeight2;
    }

    public static int getInlineResultDuration(TLRPC$BotInlineResult tLRPC$BotInlineResult) {
        int webDocumentDuration = getWebDocumentDuration(tLRPC$BotInlineResult.content);
        return webDocumentDuration == 0 ? getWebDocumentDuration(tLRPC$BotInlineResult.thumb) : webDocumentDuration;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:2:0x000a, code lost:
        r0 = r5.photoThumbs;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean hasValidGroupId() {
        /*
            r5 = this;
            long r0 = r5.getGroupId()
            r2 = 0
            int r4 = (r0 > r2 ? 1 : (r0 == r2 ? 0 : -1))
            if (r4 == 0) goto L_0x0022
            java.util.ArrayList<org.telegram.tgnet.TLRPC$PhotoSize> r0 = r5.photoThumbs
            if (r0 == 0) goto L_0x0014
            boolean r0 = r0.isEmpty()
            if (r0 == 0) goto L_0x0020
        L_0x0014:
            boolean r0 = r5.isMusic()
            if (r0 != 0) goto L_0x0020
            boolean r0 = r5.isDocument()
            if (r0 == 0) goto L_0x0022
        L_0x0020:
            r0 = 1
            goto L_0x0023
        L_0x0022:
            r0 = 0
        L_0x0023:
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.messenger.MessageObject.hasValidGroupId():boolean");
    }

    public long getGroupIdForUse() {
        long j = this.localSentGroupId;
        return j != 0 ? j : this.messageOwner.grouped_id;
    }

    public long getGroupId() {
        long j = this.localGroupId;
        return j != 0 ? j : getGroupIdForUse();
    }

    public static void addLinks(boolean z, CharSequence charSequence) {
        addLinks(z, charSequence, true, false);
    }

    public static void addLinks(boolean z, CharSequence charSequence, boolean z2, boolean z3) {
        addLinks(z, charSequence, z2, z3, false);
    }

    public static void addLinks(boolean z, CharSequence charSequence, boolean z2, boolean z3, boolean z4) {
        if ((charSequence instanceof Spannable) && containsUrls(charSequence)) {
            if (charSequence.length() < 1000) {
                try {
                    AndroidUtilities.addLinks((Spannable) charSequence, 5, z4);
                } catch (Exception e) {
                    FileLog.e((Throwable) e);
                }
            } else {
                try {
                    AndroidUtilities.addLinks((Spannable) charSequence, 1, z4);
                } catch (Exception e2) {
                    FileLog.e((Throwable) e2);
                }
            }
            addUrlsByPattern(z, charSequence, z2, 0, 0, z3);
        }
    }

    public void resetPlayingProgress() {
        this.audioProgress = 0.0f;
        this.audioProgressSec = 0;
        this.bufferedProgress = 0.0f;
    }

    private boolean addEntitiesToText(CharSequence charSequence, boolean z) {
        return addEntitiesToText(charSequence, false, z);
    }

    public boolean addEntitiesToText(CharSequence charSequence, boolean z, boolean z2) {
        if (this.isRestrictedMessage) {
            ArrayList arrayList = new ArrayList();
            TLRPC$TL_messageEntityItalic tLRPC$TL_messageEntityItalic = new TLRPC$TL_messageEntityItalic();
            tLRPC$TL_messageEntityItalic.offset = 0;
            tLRPC$TL_messageEntityItalic.length = charSequence.length();
            arrayList.add(tLRPC$TL_messageEntityItalic);
            return addEntitiesToText(charSequence, arrayList, isOutOwner(), true, z, z2);
        }
        return addEntitiesToText(charSequence, this.messageOwner.entities, isOutOwner(), true, z, z2);
    }

    /* JADX WARNING: Removed duplicated region for block: B:111:0x0164  */
    /* JADX WARNING: Removed duplicated region for block: B:133:0x01fd  */
    /* JADX WARNING: Removed duplicated region for block: B:189:0x0202 A[SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean addEntitiesToText(java.lang.CharSequence r17, java.util.ArrayList<org.telegram.tgnet.TLRPC$MessageEntity> r18, boolean r19, boolean r20, boolean r21, boolean r22) {
        /*
            r0 = r17
            boolean r1 = r0 instanceof android.text.Spannable
            r2 = 0
            if (r1 != 0) goto L_0x0008
            return r2
        L_0x0008:
            r1 = r0
            android.text.Spannable r1 = (android.text.Spannable) r1
            int r3 = r17.length()
            java.lang.Class<android.text.style.URLSpan> r4 = android.text.style.URLSpan.class
            java.lang.Object[] r3 = r1.getSpans(r2, r3, r4)
            android.text.style.URLSpan[] r3 = (android.text.style.URLSpan[]) r3
            if (r3 == 0) goto L_0x001e
            int r4 = r3.length
            if (r4 <= 0) goto L_0x001e
            r4 = 1
            goto L_0x001f
        L_0x001e:
            r4 = 0
        L_0x001f:
            boolean r5 = r18.isEmpty()
            if (r5 == 0) goto L_0x0026
            return r4
        L_0x0026:
            if (r21 == 0) goto L_0x002a
            r10 = 2
            goto L_0x002f
        L_0x002a:
            if (r19 == 0) goto L_0x002e
            r10 = 1
            goto L_0x002f
        L_0x002e:
            r10 = 0
        L_0x002f:
            java.util.ArrayList r11 = new java.util.ArrayList
            r11.<init>()
            java.util.ArrayList r6 = new java.util.ArrayList
            r7 = r18
            r6.<init>(r7)
            org.telegram.messenger.-$$Lambda$MessageObject$I820DCLASSNAMEziFcaDnrgpCx2UN-A8 r7 = org.telegram.messenger.$$Lambda$MessageObject$I820DCLASSNAMEziFcaDnrgpCx2UNA8.INSTANCE
            java.util.Collections.sort(r6, r7)
            int r7 = r6.size()
            r8 = 0
        L_0x0045:
            r13 = 0
            if (r8 >= r7) goto L_0x0207
            java.lang.Object r14 = r6.get(r8)
            org.telegram.tgnet.TLRPC$MessageEntity r14 = (org.telegram.tgnet.TLRPC$MessageEntity) r14
            int r15 = r14.length
            if (r15 <= 0) goto L_0x0201
            int r15 = r14.offset
            if (r15 < 0) goto L_0x0201
            int r2 = r17.length()
            if (r15 < r2) goto L_0x005e
            goto L_0x0201
        L_0x005e:
            int r2 = r14.offset
            int r15 = r14.length
            int r2 = r2 + r15
            int r15 = r17.length()
            if (r2 <= r15) goto L_0x0072
            int r2 = r17.length()
            int r15 = r14.offset
            int r2 = r2 - r15
            r14.length = r2
        L_0x0072:
            if (r22 == 0) goto L_0x009c
            boolean r2 = r14 instanceof org.telegram.tgnet.TLRPC$TL_messageEntityBold
            if (r2 != 0) goto L_0x009c
            boolean r2 = r14 instanceof org.telegram.tgnet.TLRPC$TL_messageEntityItalic
            if (r2 != 0) goto L_0x009c
            boolean r2 = r14 instanceof org.telegram.tgnet.TLRPC$TL_messageEntityStrike
            if (r2 != 0) goto L_0x009c
            boolean r2 = r14 instanceof org.telegram.tgnet.TLRPC$TL_messageEntityUnderline
            if (r2 != 0) goto L_0x009c
            boolean r2 = r14 instanceof org.telegram.tgnet.TLRPC$TL_messageEntityBlockquote
            if (r2 != 0) goto L_0x009c
            boolean r2 = r14 instanceof org.telegram.tgnet.TLRPC$TL_messageEntityCode
            if (r2 != 0) goto L_0x009c
            boolean r2 = r14 instanceof org.telegram.tgnet.TLRPC$TL_messageEntityPre
            if (r2 != 0) goto L_0x009c
            boolean r2 = r14 instanceof org.telegram.tgnet.TLRPC$TL_messageEntityMentionName
            if (r2 != 0) goto L_0x009c
            boolean r2 = r14 instanceof org.telegram.tgnet.TLRPC$TL_inputMessageEntityMentionName
            if (r2 != 0) goto L_0x009c
            boolean r2 = r14 instanceof org.telegram.tgnet.TLRPC$TL_messageEntityTextUrl
            if (r2 == 0) goto L_0x00d0
        L_0x009c:
            if (r3 == 0) goto L_0x00d0
            int r2 = r3.length
            if (r2 <= 0) goto L_0x00d0
            r2 = 0
        L_0x00a2:
            int r15 = r3.length
            if (r2 >= r15) goto L_0x00d0
            r15 = r3[r2]
            if (r15 != 0) goto L_0x00aa
            goto L_0x00cd
        L_0x00aa:
            r15 = r3[r2]
            int r15 = r1.getSpanStart(r15)
            r12 = r3[r2]
            int r12 = r1.getSpanEnd(r12)
            int r5 = r14.offset
            if (r5 > r15) goto L_0x00bf
            int r9 = r14.length
            int r9 = r9 + r5
            if (r9 >= r15) goto L_0x00c6
        L_0x00bf:
            if (r5 > r12) goto L_0x00cd
            int r9 = r14.length
            int r5 = r5 + r9
            if (r5 < r12) goto L_0x00cd
        L_0x00c6:
            r5 = r3[r2]
            r1.removeSpan(r5)
            r3[r2] = r13
        L_0x00cd:
            int r2 = r2 + 1
            goto L_0x00a2
        L_0x00d0:
            org.telegram.ui.Components.TextStyleSpan$TextStyleRun r2 = new org.telegram.ui.Components.TextStyleSpan$TextStyleRun
            r2.<init>()
            int r5 = r14.offset
            r2.start = r5
            int r9 = r14.length
            int r5 = r5 + r9
            r2.end = r5
            boolean r5 = r14 instanceof org.telegram.tgnet.TLRPC$TL_messageEntityStrike
            if (r5 == 0) goto L_0x00e9
            r5 = 8
            r2.flags = r5
        L_0x00e6:
            r5 = 2
            goto L_0x015d
        L_0x00e9:
            boolean r5 = r14 instanceof org.telegram.tgnet.TLRPC$TL_messageEntityUnderline
            if (r5 == 0) goto L_0x00f2
            r5 = 16
            r2.flags = r5
            goto L_0x00e6
        L_0x00f2:
            boolean r5 = r14 instanceof org.telegram.tgnet.TLRPC$TL_messageEntityBlockquote
            if (r5 == 0) goto L_0x00fb
            r5 = 32
            r2.flags = r5
            goto L_0x00e6
        L_0x00fb:
            boolean r5 = r14 instanceof org.telegram.tgnet.TLRPC$TL_messageEntityBold
            if (r5 == 0) goto L_0x0103
            r5 = 1
            r2.flags = r5
            goto L_0x00e6
        L_0x0103:
            boolean r5 = r14 instanceof org.telegram.tgnet.TLRPC$TL_messageEntityItalic
            if (r5 == 0) goto L_0x010b
            r5 = 2
            r2.flags = r5
            goto L_0x015d
        L_0x010b:
            r5 = 2
            boolean r9 = r14 instanceof org.telegram.tgnet.TLRPC$TL_messageEntityCode
            if (r9 != 0) goto L_0x015a
            boolean r9 = r14 instanceof org.telegram.tgnet.TLRPC$TL_messageEntityPre
            if (r9 == 0) goto L_0x0115
            goto L_0x015a
        L_0x0115:
            boolean r9 = r14 instanceof org.telegram.tgnet.TLRPC$TL_messageEntityMentionName
            r12 = 64
            if (r9 == 0) goto L_0x0124
            if (r20 != 0) goto L_0x011f
            goto L_0x0201
        L_0x011f:
            r2.flags = r12
            r2.urlEntity = r14
            goto L_0x015d
        L_0x0124:
            boolean r9 = r14 instanceof org.telegram.tgnet.TLRPC$TL_inputMessageEntityMentionName
            if (r9 == 0) goto L_0x0131
            if (r20 != 0) goto L_0x012c
            goto L_0x0201
        L_0x012c:
            r2.flags = r12
            r2.urlEntity = r14
            goto L_0x015d
        L_0x0131:
            if (r22 == 0) goto L_0x0139
            boolean r9 = r14 instanceof org.telegram.tgnet.TLRPC$TL_messageEntityTextUrl
            if (r9 != 0) goto L_0x0139
            goto L_0x0201
        L_0x0139:
            boolean r9 = r14 instanceof org.telegram.tgnet.TLRPC$TL_messageEntityUrl
            if (r9 != 0) goto L_0x0141
            boolean r9 = r14 instanceof org.telegram.tgnet.TLRPC$TL_messageEntityTextUrl
            if (r9 == 0) goto L_0x014b
        L_0x0141:
            java.lang.String r9 = r14.url
            boolean r9 = org.telegram.messenger.browser.Browser.isPassportUrl(r9)
            if (r9 == 0) goto L_0x014b
            goto L_0x0201
        L_0x014b:
            boolean r9 = r14 instanceof org.telegram.tgnet.TLRPC$TL_messageEntityMention
            if (r9 == 0) goto L_0x0153
            if (r20 != 0) goto L_0x0153
            goto L_0x0201
        L_0x0153:
            r9 = 128(0x80, float:1.794E-43)
            r2.flags = r9
            r2.urlEntity = r14
            goto L_0x015d
        L_0x015a:
            r9 = 4
            r2.flags = r9
        L_0x015d:
            int r9 = r11.size()
            r12 = 0
        L_0x0162:
            if (r12 >= r9) goto L_0x01f6
            java.lang.Object r13 = r11.get(r12)
            org.telegram.ui.Components.TextStyleSpan$TextStyleRun r13 = (org.telegram.ui.Components.TextStyleSpan.TextStyleRun) r13
            int r14 = r2.start
            int r15 = r13.start
            if (r14 <= r15) goto L_0x01b4
            int r15 = r13.end
            if (r14 < r15) goto L_0x0175
            goto L_0x01b8
        L_0x0175:
            int r14 = r2.end
            if (r14 >= r15) goto L_0x0198
            org.telegram.ui.Components.TextStyleSpan$TextStyleRun r14 = new org.telegram.ui.Components.TextStyleSpan$TextStyleRun
            r14.<init>(r2)
            r14.merge(r13)
            int r12 = r12 + 1
            int r9 = r9 + 1
            r11.add(r12, r14)
            org.telegram.ui.Components.TextStyleSpan$TextStyleRun r14 = new org.telegram.ui.Components.TextStyleSpan$TextStyleRun
            r14.<init>(r13)
            int r15 = r2.end
            r14.start = r15
            r15 = 1
            int r12 = r12 + r15
            int r9 = r9 + r15
            r11.add(r12, r14)
            goto L_0x01ab
        L_0x0198:
            org.telegram.ui.Components.TextStyleSpan$TextStyleRun r14 = new org.telegram.ui.Components.TextStyleSpan$TextStyleRun
            r14.<init>(r2)
            r14.merge(r13)
            int r15 = r13.end
            r14.end = r15
            int r12 = r12 + 1
            int r9 = r9 + 1
            r11.add(r12, r14)
        L_0x01ab:
            int r14 = r2.start
            int r15 = r13.end
            r2.start = r15
            r13.end = r14
            goto L_0x01b8
        L_0x01b4:
            int r14 = r2.end
            if (r15 < r14) goto L_0x01ba
        L_0x01b8:
            r14 = 1
            goto L_0x01f2
        L_0x01ba:
            int r5 = r13.end
            if (r14 != r5) goto L_0x01c2
            r13.merge(r2)
            goto L_0x01ef
        L_0x01c2:
            if (r14 >= r5) goto L_0x01dc
            org.telegram.ui.Components.TextStyleSpan$TextStyleRun r5 = new org.telegram.ui.Components.TextStyleSpan$TextStyleRun
            r5.<init>(r13)
            r5.merge(r2)
            int r14 = r2.end
            r5.end = r14
            int r12 = r12 + 1
            int r9 = r9 + 1
            r11.add(r12, r5)
            int r5 = r2.end
            r13.start = r5
            goto L_0x01ef
        L_0x01dc:
            org.telegram.ui.Components.TextStyleSpan$TextStyleRun r5 = new org.telegram.ui.Components.TextStyleSpan$TextStyleRun
            r5.<init>(r2)
            int r14 = r13.end
            r5.start = r14
            int r12 = r12 + 1
            int r9 = r9 + 1
            r11.add(r12, r5)
            r13.merge(r2)
        L_0x01ef:
            r2.end = r15
            goto L_0x01b8
        L_0x01f2:
            int r12 = r12 + r14
            r5 = 2
            goto L_0x0162
        L_0x01f6:
            r14 = 1
            int r5 = r2.start
            int r9 = r2.end
            if (r5 >= r9) goto L_0x0202
            r11.add(r2)
            goto L_0x0202
        L_0x0201:
            r14 = 1
        L_0x0202:
            int r8 = r8 + 1
            r2 = 0
            goto L_0x0045
        L_0x0207:
            r14 = 1
            int r2 = r11.size()
            r12 = r4
            r9 = 0
        L_0x020e:
            if (r9 >= r2) goto L_0x03b5
            java.lang.Object r3 = r11.get(r9)
            r15 = r3
            org.telegram.ui.Components.TextStyleSpan$TextStyleRun r15 = (org.telegram.ui.Components.TextStyleSpan.TextStyleRun) r15
            org.telegram.tgnet.TLRPC$MessageEntity r3 = r15.urlEntity
            if (r3 == 0) goto L_0x0225
            int r4 = r3.offset
            int r3 = r3.length
            int r3 = r3 + r4
            java.lang.String r3 = android.text.TextUtils.substring(r0, r4, r3)
            goto L_0x0226
        L_0x0225:
            r3 = r13
        L_0x0226:
            org.telegram.tgnet.TLRPC$MessageEntity r4 = r15.urlEntity
            boolean r5 = r4 instanceof org.telegram.tgnet.TLRPC$TL_messageEntityBotCommand
            r8 = 33
            if (r5 == 0) goto L_0x023c
            org.telegram.ui.Components.URLSpanBotCommand r4 = new org.telegram.ui.Components.URLSpanBotCommand
            r4.<init>(r3, r10, r15)
            int r3 = r15.start
            int r5 = r15.end
            r1.setSpan(r4, r3, r5, r8)
            goto L_0x02c9
        L_0x023c:
            boolean r5 = r4 instanceof org.telegram.tgnet.TLRPC$TL_messageEntityHashtag
            if (r5 != 0) goto L_0x039f
            boolean r5 = r4 instanceof org.telegram.tgnet.TLRPC$TL_messageEntityMention
            if (r5 != 0) goto L_0x039f
            boolean r5 = r4 instanceof org.telegram.tgnet.TLRPC$TL_messageEntityCashtag
            if (r5 == 0) goto L_0x024a
            goto L_0x039f
        L_0x024a:
            boolean r5 = r4 instanceof org.telegram.tgnet.TLRPC$TL_messageEntityEmail
            if (r5 == 0) goto L_0x026c
            org.telegram.ui.Components.URLSpanReplacement r4 = new org.telegram.ui.Components.URLSpanReplacement
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            java.lang.String r6 = "mailto:"
            r5.append(r6)
            r5.append(r3)
            java.lang.String r3 = r5.toString()
            r4.<init>(r3, r15)
            int r3 = r15.start
            int r5 = r15.end
            r1.setSpan(r4, r3, r5, r8)
            goto L_0x02c9
        L_0x026c:
            boolean r5 = r4 instanceof org.telegram.tgnet.TLRPC$TL_messageEntityUrl
            if (r5 == 0) goto L_0x02a7
            java.lang.String r4 = r3.toLowerCase()
            java.lang.String r5 = "://"
            boolean r4 = r4.contains(r5)
            if (r4 != 0) goto L_0x029a
            org.telegram.ui.Components.URLSpanBrowser r4 = new org.telegram.ui.Components.URLSpanBrowser
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            java.lang.String r6 = "http://"
            r5.append(r6)
            r5.append(r3)
            java.lang.String r3 = r5.toString()
            r4.<init>(r3, r15)
            int r3 = r15.start
            int r5 = r15.end
            r1.setSpan(r4, r3, r5, r8)
            goto L_0x02c8
        L_0x029a:
            org.telegram.ui.Components.URLSpanBrowser r4 = new org.telegram.ui.Components.URLSpanBrowser
            r4.<init>(r3, r15)
            int r3 = r15.start
            int r5 = r15.end
            r1.setSpan(r4, r3, r5, r8)
            goto L_0x02c8
        L_0x02a7:
            boolean r5 = r4 instanceof org.telegram.tgnet.TLRPC$TL_messageEntityBankCard
            if (r5 == 0) goto L_0x02cd
            org.telegram.ui.Components.URLSpanNoUnderline r4 = new org.telegram.ui.Components.URLSpanNoUnderline
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            java.lang.String r6 = "card:"
            r5.append(r6)
            r5.append(r3)
            java.lang.String r3 = r5.toString()
            r4.<init>(r3, r15)
            int r3 = r15.start
            int r5 = r15.end
            r1.setSpan(r4, r3, r5, r8)
        L_0x02c8:
            r12 = 1
        L_0x02c9:
            r16 = 4
            goto L_0x03af
        L_0x02cd:
            boolean r5 = r4 instanceof org.telegram.tgnet.TLRPC$TL_messageEntityPhone
            if (r5 == 0) goto L_0x030a
            java.lang.String r4 = org.telegram.PhoneFormat.PhoneFormat.stripExceptNumbers(r3)
            java.lang.String r5 = "+"
            boolean r3 = r3.startsWith(r5)
            if (r3 == 0) goto L_0x02ec
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            r3.append(r5)
            r3.append(r4)
            java.lang.String r4 = r3.toString()
        L_0x02ec:
            org.telegram.ui.Components.URLSpanBrowser r3 = new org.telegram.ui.Components.URLSpanBrowser
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            java.lang.String r6 = "tel:"
            r5.append(r6)
            r5.append(r4)
            java.lang.String r4 = r5.toString()
            r3.<init>(r4, r15)
            int r4 = r15.start
            int r5 = r15.end
            r1.setSpan(r3, r4, r5, r8)
            goto L_0x02c8
        L_0x030a:
            boolean r3 = r4 instanceof org.telegram.tgnet.TLRPC$TL_messageEntityTextUrl
            if (r3 == 0) goto L_0x031f
            org.telegram.ui.Components.URLSpanReplacement r3 = new org.telegram.ui.Components.URLSpanReplacement
            org.telegram.tgnet.TLRPC$MessageEntity r4 = r15.urlEntity
            java.lang.String r4 = r4.url
            r3.<init>(r4, r15)
            int r4 = r15.start
            int r5 = r15.end
            r1.setSpan(r3, r4, r5, r8)
            goto L_0x02c9
        L_0x031f:
            boolean r3 = r4 instanceof org.telegram.tgnet.TLRPC$TL_messageEntityMentionName
            java.lang.String r5 = ""
            if (r3 == 0) goto L_0x0347
            org.telegram.ui.Components.URLSpanUserMention r3 = new org.telegram.ui.Components.URLSpanUserMention
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>()
            r4.append(r5)
            org.telegram.tgnet.TLRPC$MessageEntity r5 = r15.urlEntity
            org.telegram.tgnet.TLRPC$TL_messageEntityMentionName r5 = (org.telegram.tgnet.TLRPC$TL_messageEntityMentionName) r5
            int r5 = r5.user_id
            r4.append(r5)
            java.lang.String r4 = r4.toString()
            r3.<init>(r4, r10, r15)
            int r4 = r15.start
            int r5 = r15.end
            r1.setSpan(r3, r4, r5, r8)
            goto L_0x02c9
        L_0x0347:
            boolean r3 = r4 instanceof org.telegram.tgnet.TLRPC$TL_inputMessageEntityMentionName
            if (r3 == 0) goto L_0x0370
            org.telegram.ui.Components.URLSpanUserMention r3 = new org.telegram.ui.Components.URLSpanUserMention
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>()
            r4.append(r5)
            org.telegram.tgnet.TLRPC$MessageEntity r5 = r15.urlEntity
            org.telegram.tgnet.TLRPC$TL_inputMessageEntityMentionName r5 = (org.telegram.tgnet.TLRPC$TL_inputMessageEntityMentionName) r5
            org.telegram.tgnet.TLRPC$InputUser r5 = r5.user_id
            int r5 = r5.user_id
            r4.append(r5)
            java.lang.String r4 = r4.toString()
            r3.<init>(r4, r10, r15)
            int r4 = r15.start
            int r5 = r15.end
            r1.setSpan(r3, r4, r5, r8)
            goto L_0x02c9
        L_0x0370:
            int r3 = r15.flags
            r16 = 4
            r3 = r3 & 4
            if (r3 == 0) goto L_0x0390
            org.telegram.ui.Components.URLSpanMono r7 = new org.telegram.ui.Components.URLSpanMono
            int r5 = r15.start
            int r6 = r15.end
            r3 = r7
            r4 = r1
            r13 = r7
            r7 = r10
            r14 = 33
            r8 = r15
            r3.<init>(r4, r5, r6, r7, r8)
            int r3 = r15.start
            int r4 = r15.end
            r1.setSpan(r13, r3, r4, r14)
            goto L_0x03af
        L_0x0390:
            r14 = 33
            org.telegram.ui.Components.TextStyleSpan r3 = new org.telegram.ui.Components.TextStyleSpan
            r3.<init>(r15)
            int r4 = r15.start
            int r5 = r15.end
            r1.setSpan(r3, r4, r5, r14)
            goto L_0x03af
        L_0x039f:
            r14 = 33
            r16 = 4
            org.telegram.ui.Components.URLSpanNoUnderline r4 = new org.telegram.ui.Components.URLSpanNoUnderline
            r4.<init>(r3, r15)
            int r3 = r15.start
            int r5 = r15.end
            r1.setSpan(r4, r3, r5, r14)
        L_0x03af:
            int r9 = r9 + 1
            r13 = 0
            r14 = 1
            goto L_0x020e
        L_0x03b5:
            return r12
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.messenger.MessageObject.addEntitiesToText(java.lang.CharSequence, java.util.ArrayList, boolean, boolean, boolean, boolean):boolean");
    }

    static /* synthetic */ int lambda$addEntitiesToText$0(TLRPC$MessageEntity tLRPC$MessageEntity, TLRPC$MessageEntity tLRPC$MessageEntity2) {
        int i = tLRPC$MessageEntity.offset;
        int i2 = tLRPC$MessageEntity2.offset;
        if (i > i2) {
            return 1;
        }
        return i < i2 ? -1 : 0;
    }

    public boolean needDrawShareButton() {
        int i;
        String str;
        int i2;
        if (this.scheduled || this.eventId != 0) {
            return false;
        }
        if (this.messageOwner.fwd_from != null && !isOutOwner() && this.messageOwner.fwd_from.saved_from_peer != null && getDialogId() == ((long) UserConfig.getInstance(this.currentAccount).getClientUserId())) {
            return true;
        }
        int i3 = this.type;
        if (!(i3 == 13 || i3 == 15)) {
            TLRPC$MessageFwdHeader tLRPC$MessageFwdHeader = this.messageOwner.fwd_from;
            if (tLRPC$MessageFwdHeader != null && (tLRPC$MessageFwdHeader.from_id instanceof TLRPC$TL_peerChannel) && !isOutOwner()) {
                return true;
            }
            if (isFromUser()) {
                TLRPC$MessageMedia tLRPC$MessageMedia = this.messageOwner.media;
                if ((tLRPC$MessageMedia instanceof TLRPC$TL_messageMediaEmpty) || tLRPC$MessageMedia == null || ((tLRPC$MessageMedia instanceof TLRPC$TL_messageMediaWebPage) && !(tLRPC$MessageMedia.webpage instanceof TLRPC$TL_webPage))) {
                    return false;
                }
                TLRPC$User user = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(this.messageOwner.from_id.user_id));
                if (user != null && user.bot) {
                    return true;
                }
                if (!isOut()) {
                    TLRPC$Message tLRPC$Message = this.messageOwner;
                    TLRPC$MessageMedia tLRPC$MessageMedia2 = tLRPC$Message.media;
                    if ((tLRPC$MessageMedia2 instanceof TLRPC$TL_messageMediaGame) || (tLRPC$MessageMedia2 instanceof TLRPC$TL_messageMediaInvoice)) {
                        return true;
                    }
                    TLRPC$Peer tLRPC$Peer = tLRPC$Message.peer_id;
                    TLRPC$Chat tLRPC$Chat = null;
                    if (!(tLRPC$Peer == null || (i2 = tLRPC$Peer.channel_id) == 0)) {
                        tLRPC$Chat = getChat((AbstractMap<Integer, TLRPC$Chat>) null, (SparseArray<TLRPC$Chat>) null, i2);
                    }
                    if (!ChatObject.isChannel(tLRPC$Chat) || !tLRPC$Chat.megagroup || (str = tLRPC$Chat.username) == null || str.length() <= 0) {
                        return false;
                    }
                    TLRPC$MessageMedia tLRPC$MessageMedia3 = this.messageOwner.media;
                    if ((tLRPC$MessageMedia3 instanceof TLRPC$TL_messageMediaContact) || (tLRPC$MessageMedia3 instanceof TLRPC$TL_messageMediaGeo)) {
                        return false;
                    }
                    return true;
                }
            } else {
                TLRPC$Message tLRPC$Message2 = this.messageOwner;
                if ((!(tLRPC$Message2.from_id instanceof TLRPC$TL_peerChannel) && !tLRPC$Message2.post) || isSupergroup()) {
                    return false;
                }
                TLRPC$Message tLRPC$Message3 = this.messageOwner;
                if (tLRPC$Message3.peer_id.channel_id == 0 || ((tLRPC$Message3.via_bot_id != 0 || tLRPC$Message3.reply_to != null) && ((i = this.type) == 13 || i == 15))) {
                    return false;
                }
                return true;
            }
        }
        return false;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:2:0x0008, code lost:
        r0 = r0.webpage;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean isYouTubeVideo() {
        /*
            r2 = this;
            org.telegram.tgnet.TLRPC$Message r0 = r2.messageOwner
            org.telegram.tgnet.TLRPC$MessageMedia r0 = r0.media
            boolean r1 = r0 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaWebPage
            if (r1 == 0) goto L_0x0026
            org.telegram.tgnet.TLRPC$WebPage r0 = r0.webpage
            if (r0 == 0) goto L_0x0026
            java.lang.String r0 = r0.embed_url
            boolean r0 = android.text.TextUtils.isEmpty(r0)
            if (r0 != 0) goto L_0x0026
            org.telegram.tgnet.TLRPC$Message r0 = r2.messageOwner
            org.telegram.tgnet.TLRPC$MessageMedia r0 = r0.media
            org.telegram.tgnet.TLRPC$WebPage r0 = r0.webpage
            java.lang.String r0 = r0.site_name
            java.lang.String r1 = "YouTube"
            boolean r0 = r1.equals(r0)
            if (r0 == 0) goto L_0x0026
            r0 = 1
            goto L_0x0027
        L_0x0026:
            r0 = 0
        L_0x0027:
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.messenger.MessageObject.isYouTubeVideo():boolean");
    }

    public int getMaxMessageTextWidth() {
        TLRPC$WebPage tLRPC$WebPage;
        if (!AndroidUtilities.isTablet() || this.eventId == 0) {
            this.generatedWithMinSize = AndroidUtilities.isTablet() ? AndroidUtilities.getMinTabletSide() : AndroidUtilities.displaySize.x;
        } else {
            this.generatedWithMinSize = AndroidUtilities.dp(530.0f);
        }
        this.generatedWithDensity = AndroidUtilities.density;
        TLRPC$MessageMedia tLRPC$MessageMedia = this.messageOwner.media;
        int i = 0;
        if ((tLRPC$MessageMedia instanceof TLRPC$TL_messageMediaWebPage) && (tLRPC$WebPage = tLRPC$MessageMedia.webpage) != null && "telegram_background".equals(tLRPC$WebPage.type)) {
            try {
                Uri parse = Uri.parse(this.messageOwner.media.webpage.url);
                String lastPathSegment = parse.getLastPathSegment();
                if (parse.getQueryParameter("bg_color") != null) {
                    i = AndroidUtilities.dp(220.0f);
                } else if (lastPathSegment.length() == 6 || (lastPathSegment.length() == 13 && lastPathSegment.charAt(6) == '-')) {
                    i = AndroidUtilities.dp(200.0f);
                }
            } catch (Exception unused) {
            }
        } else if (isAndroidTheme()) {
            i = AndroidUtilities.dp(200.0f);
        }
        if (i != 0) {
            return i;
        }
        int dp = this.generatedWithMinSize - AndroidUtilities.dp((!needDrawAvatarInternal() || isOutOwner() || this.messageOwner.isThreadMessage) ? 80.0f : 132.0f);
        if (needDrawShareButton() && !isOutOwner()) {
            dp -= AndroidUtilities.dp(10.0f);
        }
        int i2 = dp;
        return this.messageOwner.media instanceof TLRPC$TL_messageMediaGame ? i2 - AndroidUtilities.dp(10.0f) : i2;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:36:0x0077, code lost:
        if ((r0.media instanceof org.telegram.tgnet.TLRPC$TL_messageMediaUnsupported) == false) goto L_0x007b;
     */
    /* JADX WARNING: Removed duplicated region for block: B:150:0x0320  */
    /* JADX WARNING: Removed duplicated region for block: B:153:0x0325  */
    /* JADX WARNING: Removed duplicated region for block: B:156:0x0336  */
    /* JADX WARNING: Removed duplicated region for block: B:193:0x03f6  */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x007e  */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x0088  */
    /* JADX WARNING: Removed duplicated region for block: B:55:0x00b4  */
    /* JADX WARNING: Removed duplicated region for block: B:65:0x00f4  */
    /* JADX WARNING: Removed duplicated region for block: B:68:0x0116  */
    /* JADX WARNING: Removed duplicated region for block: B:69:0x0119  */
    /* JADX WARNING: Removed duplicated region for block: B:74:0x0122 A[Catch:{ Exception -> 0x047a }] */
    /* JADX WARNING: Removed duplicated region for block: B:75:0x0140 A[Catch:{ Exception -> 0x047a }] */
    /* JADX WARNING: Removed duplicated region for block: B:79:0x0161  */
    /* JADX WARNING: Removed duplicated region for block: B:80:0x0163  */
    /* JADX WARNING: Removed duplicated region for block: B:83:0x0175  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void generateLayout(org.telegram.tgnet.TLRPC$User r30) {
        /*
            r29 = this;
            r1 = r29
            int r0 = r1.type
            if (r0 != 0) goto L_0x047e
            org.telegram.tgnet.TLRPC$Message r0 = r1.messageOwner
            org.telegram.tgnet.TLRPC$Peer r0 = r0.peer_id
            if (r0 == 0) goto L_0x047e
            java.lang.CharSequence r0 = r1.messageText
            boolean r0 = android.text.TextUtils.isEmpty(r0)
            if (r0 == 0) goto L_0x0016
            goto L_0x047e
        L_0x0016:
            r29.generateLinkDescription()
            java.util.ArrayList r0 = new java.util.ArrayList
            r0.<init>()
            r1.textLayoutBlocks = r0
            r2 = 0
            r1.textWidth = r2
            org.telegram.tgnet.TLRPC$Message r0 = r1.messageOwner
            int r3 = r0.send_state
            r4 = 1
            if (r3 == 0) goto L_0x002c
            r0 = 0
            goto L_0x0033
        L_0x002c:
            java.util.ArrayList<org.telegram.tgnet.TLRPC$MessageEntity> r0 = r0.entities
            boolean r0 = r0.isEmpty()
            r0 = r0 ^ r4
        L_0x0033:
            if (r0 != 0) goto L_0x007b
            long r5 = r1.eventId
            r7 = 0
            int r0 = (r5 > r7 ? 1 : (r5 == r7 ? 0 : -1))
            if (r0 != 0) goto L_0x0079
            org.telegram.tgnet.TLRPC$Message r0 = r1.messageOwner
            boolean r3 = r0 instanceof org.telegram.tgnet.TLRPC$TL_message_old
            if (r3 != 0) goto L_0x0079
            boolean r3 = r0 instanceof org.telegram.tgnet.TLRPC$TL_message_old2
            if (r3 != 0) goto L_0x0079
            boolean r3 = r0 instanceof org.telegram.tgnet.TLRPC$TL_message_old3
            if (r3 != 0) goto L_0x0079
            boolean r3 = r0 instanceof org.telegram.tgnet.TLRPC$TL_message_old4
            if (r3 != 0) goto L_0x0079
            boolean r3 = r0 instanceof org.telegram.tgnet.TLRPC$TL_messageForwarded_old
            if (r3 != 0) goto L_0x0079
            boolean r3 = r0 instanceof org.telegram.tgnet.TLRPC$TL_messageForwarded_old2
            if (r3 != 0) goto L_0x0079
            boolean r3 = r0 instanceof org.telegram.tgnet.TLRPC$TL_message_secret
            if (r3 != 0) goto L_0x0079
            org.telegram.tgnet.TLRPC$MessageMedia r0 = r0.media
            boolean r0 = r0 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaInvoice
            if (r0 != 0) goto L_0x0079
            boolean r0 = r29.isOut()
            if (r0 == 0) goto L_0x006d
            org.telegram.tgnet.TLRPC$Message r0 = r1.messageOwner
            int r0 = r0.send_state
            if (r0 != 0) goto L_0x0079
        L_0x006d:
            org.telegram.tgnet.TLRPC$Message r0 = r1.messageOwner
            int r3 = r0.id
            if (r3 < 0) goto L_0x0079
            org.telegram.tgnet.TLRPC$MessageMedia r0 = r0.media
            boolean r0 = r0 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaUnsupported
            if (r0 == 0) goto L_0x007b
        L_0x0079:
            r3 = 1
            goto L_0x007c
        L_0x007b:
            r3 = 0
        L_0x007c:
            if (r3 == 0) goto L_0x0088
            boolean r0 = r29.isOutOwner()
            java.lang.CharSequence r5 = r1.messageText
            addLinks(r0, r5, r4, r4)
            goto L_0x00a3
        L_0x0088:
            java.lang.CharSequence r0 = r1.messageText
            boolean r5 = r0 instanceof android.text.Spannable
            if (r5 == 0) goto L_0x00a3
            int r0 = r0.length()
            r5 = 1000(0x3e8, float:1.401E-42)
            if (r0 >= r5) goto L_0x00a3
            java.lang.CharSequence r0 = r1.messageText     // Catch:{ all -> 0x009f }
            android.text.Spannable r0 = (android.text.Spannable) r0     // Catch:{ all -> 0x009f }
            r5 = 4
            org.telegram.messenger.AndroidUtilities.addLinks(r0, r5)     // Catch:{ all -> 0x009f }
            goto L_0x00a3
        L_0x009f:
            r0 = move-exception
            org.telegram.messenger.FileLog.e((java.lang.Throwable) r0)
        L_0x00a3:
            boolean r0 = r29.isYouTubeVideo()
            if (r0 != 0) goto L_0x00f4
            org.telegram.messenger.MessageObject r0 = r1.replyMessageObject
            if (r0 == 0) goto L_0x00b4
            boolean r0 = r0.isYouTubeVideo()
            if (r0 == 0) goto L_0x00b4
            goto L_0x00f4
        L_0x00b4:
            org.telegram.messenger.MessageObject r0 = r1.replyMessageObject
            if (r0 == 0) goto L_0x0104
            boolean r0 = r0.isVideo()
            if (r0 == 0) goto L_0x00d1
            boolean r5 = r29.isOutOwner()
            java.lang.CharSequence r6 = r1.messageText
            r7 = 0
            r8 = 3
            org.telegram.messenger.MessageObject r0 = r1.replyMessageObject
            int r9 = r0.getDuration()
            r10 = 0
            addUrlsByPattern(r5, r6, r7, r8, r9, r10)
            goto L_0x0104
        L_0x00d1:
            org.telegram.messenger.MessageObject r0 = r1.replyMessageObject
            boolean r0 = r0.isMusic()
            if (r0 != 0) goto L_0x00e1
            org.telegram.messenger.MessageObject r0 = r1.replyMessageObject
            boolean r0 = r0.isVoice()
            if (r0 == 0) goto L_0x0104
        L_0x00e1:
            boolean r5 = r29.isOutOwner()
            java.lang.CharSequence r6 = r1.messageText
            r7 = 0
            r8 = 4
            org.telegram.messenger.MessageObject r0 = r1.replyMessageObject
            int r9 = r0.getDuration()
            r10 = 0
            addUrlsByPattern(r5, r6, r7, r8, r9, r10)
            goto L_0x0104
        L_0x00f4:
            boolean r11 = r29.isOutOwner()
            java.lang.CharSequence r12 = r1.messageText
            r13 = 0
            r14 = 3
            r15 = 2147483647(0x7fffffff, float:NaN)
            r16 = 0
            addUrlsByPattern(r11, r12, r13, r14, r15, r16)
        L_0x0104:
            java.lang.CharSequence r0 = r1.messageText
            boolean r3 = r1.addEntitiesToText(r0, r3)
            int r15 = r29.getMaxMessageTextWidth()
            org.telegram.tgnet.TLRPC$Message r0 = r1.messageOwner
            org.telegram.tgnet.TLRPC$MessageMedia r0 = r0.media
            boolean r0 = r0 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaGame
            if (r0 == 0) goto L_0x0119
            android.text.TextPaint r0 = org.telegram.ui.ActionBar.Theme.chat_msgGameTextPaint
            goto L_0x011b
        L_0x0119:
            android.text.TextPaint r0 = org.telegram.ui.ActionBar.Theme.chat_msgTextPaint
        L_0x011b:
            r14 = r0
            int r0 = android.os.Build.VERSION.SDK_INT     // Catch:{ Exception -> 0x047a }
            r13 = 24
            if (r0 < r13) goto L_0x0140
            java.lang.CharSequence r5 = r1.messageText     // Catch:{ Exception -> 0x047a }
            int r6 = r5.length()     // Catch:{ Exception -> 0x047a }
            android.text.StaticLayout$Builder r5 = android.text.StaticLayout.Builder.obtain(r5, r2, r6, r14, r15)     // Catch:{ Exception -> 0x047a }
            android.text.StaticLayout$Builder r5 = r5.setBreakStrategy(r4)     // Catch:{ Exception -> 0x047a }
            android.text.StaticLayout$Builder r5 = r5.setHyphenationFrequency(r2)     // Catch:{ Exception -> 0x047a }
            android.text.Layout$Alignment r6 = android.text.Layout.Alignment.ALIGN_NORMAL     // Catch:{ Exception -> 0x047a }
            android.text.StaticLayout$Builder r5 = r5.setAlignment(r6)     // Catch:{ Exception -> 0x047a }
            android.text.StaticLayout r5 = r5.build()     // Catch:{ Exception -> 0x047a }
            r12 = r5
            goto L_0x0153
        L_0x0140:
            android.text.StaticLayout r16 = new android.text.StaticLayout     // Catch:{ Exception -> 0x047a }
            java.lang.CharSequence r6 = r1.messageText     // Catch:{ Exception -> 0x047a }
            android.text.Layout$Alignment r9 = android.text.Layout.Alignment.ALIGN_NORMAL     // Catch:{ Exception -> 0x047a }
            r10 = 1065353216(0x3var_, float:1.0)
            r11 = 0
            r12 = 0
            r5 = r16
            r7 = r14
            r8 = r15
            r5.<init>(r6, r7, r8, r9, r10, r11, r12)     // Catch:{ Exception -> 0x047a }
            r12 = r16
        L_0x0153:
            int r5 = r12.getHeight()
            r1.textHeight = r5
            int r5 = r12.getLineCount()
            r1.linesCount = r5
            if (r0 < r13) goto L_0x0163
            r11 = 1
            goto L_0x016e
        L_0x0163:
            float r0 = (float) r5
            r5 = 1092616192(0x41200000, float:10.0)
            float r0 = r0 / r5
            double r5 = (double) r0
            double r5 = java.lang.Math.ceil(r5)
            int r0 = (int) r5
            r11 = r0
        L_0x016e:
            r10 = 0
            r8 = 0
            r9 = 0
            r16 = 0
        L_0x0173:
            if (r9 >= r11) goto L_0x0479
            int r0 = android.os.Build.VERSION.SDK_INT
            if (r0 < r13) goto L_0x017c
            int r5 = r1.linesCount
            goto L_0x0185
        L_0x017c:
            r5 = 10
            int r6 = r1.linesCount
            int r6 = r6 - r8
            int r5 = java.lang.Math.min(r5, r6)
        L_0x0185:
            r7 = r5
            org.telegram.messenger.MessageObject$TextLayoutBlock r6 = new org.telegram.messenger.MessageObject$TextLayoutBlock
            r6.<init>()
            r5 = 2
            if (r11 != r4) goto L_0x0201
            r6.textLayout = r12
            r6.textYOffset = r10
            r6.charactersOffset = r2
            java.lang.CharSequence r0 = r12.getText()
            int r0 = r0.length()
            r6.charactersEnd = r0
            int r0 = r1.emojiOnlyCount
            if (r0 == 0) goto L_0x01f0
            if (r0 == r4) goto L_0x01d9
            if (r0 == r5) goto L_0x01c2
            r5 = 3
            if (r0 == r5) goto L_0x01aa
            goto L_0x01f0
        L_0x01aa:
            int r0 = r1.textHeight
            r5 = 1082549862(0x40866666, float:4.2)
            int r17 = org.telegram.messenger.AndroidUtilities.dp(r5)
            int r0 = r0 - r17
            r1.textHeight = r0
            float r0 = r6.textYOffset
            int r5 = org.telegram.messenger.AndroidUtilities.dp(r5)
            float r5 = (float) r5
            float r0 = r0 - r5
            r6.textYOffset = r0
            goto L_0x01f0
        L_0x01c2:
            int r0 = r1.textHeight
            r5 = 1083179008(0x40900000, float:4.5)
            int r17 = org.telegram.messenger.AndroidUtilities.dp(r5)
            int r0 = r0 - r17
            r1.textHeight = r0
            float r0 = r6.textYOffset
            int r5 = org.telegram.messenger.AndroidUtilities.dp(r5)
            float r5 = (float) r5
            float r0 = r0 - r5
            r6.textYOffset = r0
            goto L_0x01f0
        L_0x01d9:
            int r0 = r1.textHeight
            r5 = 1084856730(0x40a9999a, float:5.3)
            int r17 = org.telegram.messenger.AndroidUtilities.dp(r5)
            int r0 = r0 - r17
            r1.textHeight = r0
            float r0 = r6.textYOffset
            int r5 = org.telegram.messenger.AndroidUtilities.dp(r5)
            float r5 = (float) r5
            float r0 = r0 - r5
            r6.textYOffset = r0
        L_0x01f0:
            int r0 = r1.textHeight
            r6.height = r0
            r2 = r6
            r4 = r8
            r6 = r9
            r8 = r11
            r5 = r12
            r18 = r14
            r17 = 24
            r24 = 2
            goto L_0x02e5
        L_0x0201:
            int r5 = r12.getLineStart(r8)
            int r17 = r8 + r7
            int r10 = r17 + -1
            int r10 = r12.getLineEnd(r10)
            if (r10 >= r5) goto L_0x021f
            r20 = r3
            r21 = r8
            r6 = r9
            r8 = r11
            r27 = r12
            r18 = r14
            r3 = 0
            r9 = 1
            r17 = 24
            goto L_0x0465
        L_0x021f:
            r6.charactersOffset = r5
            r6.charactersEnd = r10
            if (r3 == 0) goto L_0x0259
            if (r0 < r13) goto L_0x0259
            java.lang.CharSequence r0 = r1.messageText     // Catch:{ Exception -> 0x0453 }
            r17 = 1073741824(0x40000000, float:2.0)
            int r17 = org.telegram.messenger.AndroidUtilities.dp(r17)     // Catch:{ Exception -> 0x0453 }
            int r13 = r15 + r17
            android.text.StaticLayout$Builder r0 = android.text.StaticLayout.Builder.obtain(r0, r5, r10, r14, r13)     // Catch:{ Exception -> 0x0453 }
            android.text.StaticLayout$Builder r0 = r0.setBreakStrategy(r4)     // Catch:{ Exception -> 0x0453 }
            android.text.StaticLayout$Builder r0 = r0.setHyphenationFrequency(r2)     // Catch:{ Exception -> 0x0453 }
            android.text.Layout$Alignment r5 = android.text.Layout.Alignment.ALIGN_NORMAL     // Catch:{ Exception -> 0x0453 }
            android.text.StaticLayout$Builder r0 = r0.setAlignment(r5)     // Catch:{ Exception -> 0x0453 }
            android.text.StaticLayout r0 = r0.build()     // Catch:{ Exception -> 0x0453 }
            r6.textLayout = r0     // Catch:{ Exception -> 0x0453 }
            r2 = r6
            r28 = r7
            r4 = r8
            r25 = r9
            r26 = r11
            r5 = r12
            r18 = r14
            r17 = 24
            r24 = 2
            goto L_0x028f
        L_0x0259:
            android.text.StaticLayout r0 = new android.text.StaticLayout     // Catch:{ Exception -> 0x0453 }
            java.lang.CharSequence r13 = r1.messageText     // Catch:{ Exception -> 0x0453 }
            android.text.Layout$Alignment r17 = android.text.Layout.Alignment.ALIGN_NORMAL     // Catch:{ Exception -> 0x0453 }
            r20 = 1065353216(0x3var_, float:1.0)
            r21 = 0
            r22 = 0
            r23 = r5
            r24 = 2
            r5 = r0
            r2 = r6
            r6 = r13
            r13 = r7
            r7 = r23
            r4 = r8
            r8 = r10
            r10 = r9
            r9 = r14
            r25 = r10
            r10 = r15
            r26 = r11
            r11 = r17
            r27 = r12
            r12 = r20
            r28 = r13
            r17 = 24
            r13 = r21
            r18 = r14
            r14 = r22
            r5.<init>(r6, r7, r8, r9, r10, r11, r12, r13, r14)     // Catch:{ Exception -> 0x0447 }
            r2.textLayout = r0     // Catch:{ Exception -> 0x0447 }
            r5 = r27
        L_0x028f:
            int r0 = r5.getLineTop(r4)     // Catch:{ Exception -> 0x043f }
            float r0 = (float) r0     // Catch:{ Exception -> 0x043f }
            r2.textYOffset = r0     // Catch:{ Exception -> 0x043f }
            r6 = r25
            if (r6 == 0) goto L_0x029f
            float r0 = r0 - r16
            int r0 = (int) r0
            r2.height = r0     // Catch:{ Exception -> 0x0437 }
        L_0x029f:
            int r0 = r2.height     // Catch:{ Exception -> 0x0437 }
            android.text.StaticLayout r7 = r2.textLayout     // Catch:{ Exception -> 0x0437 }
            int r8 = r7.getLineCount()     // Catch:{ Exception -> 0x0437 }
            r9 = 1
            int r8 = r8 - r9
            int r7 = r7.getLineBottom(r8)     // Catch:{ Exception -> 0x0437 }
            int r0 = java.lang.Math.max(r0, r7)     // Catch:{ Exception -> 0x0437 }
            r2.height = r0     // Catch:{ Exception -> 0x0437 }
            float r7 = r2.textYOffset     // Catch:{ Exception -> 0x0437 }
            r8 = r26
            int r11 = r8 + -1
            if (r6 != r11) goto L_0x02e0
            android.text.StaticLayout r0 = r2.textLayout
            int r0 = r0.getLineCount()
            r9 = r28
            int r9 = java.lang.Math.max(r9, r0)
            int r0 = r1.textHeight     // Catch:{ Exception -> 0x02db }
            float r10 = r2.textYOffset     // Catch:{ Exception -> 0x02db }
            android.text.StaticLayout r11 = r2.textLayout     // Catch:{ Exception -> 0x02db }
            int r11 = r11.getHeight()     // Catch:{ Exception -> 0x02db }
            float r11 = (float) r11     // Catch:{ Exception -> 0x02db }
            float r10 = r10 + r11
            int r10 = (int) r10     // Catch:{ Exception -> 0x02db }
            int r0 = java.lang.Math.max(r0, r10)     // Catch:{ Exception -> 0x02db }
            r1.textHeight = r0     // Catch:{ Exception -> 0x02db }
            goto L_0x02e2
        L_0x02db:
            r0 = move-exception
            org.telegram.messenger.FileLog.e((java.lang.Throwable) r0)
            goto L_0x02e2
        L_0x02e0:
            r9 = r28
        L_0x02e2:
            r16 = r7
            r7 = r9
        L_0x02e5:
            java.util.ArrayList<org.telegram.messenger.MessageObject$TextLayoutBlock> r0 = r1.textLayoutBlocks
            r0.add(r2)
            android.text.StaticLayout r0 = r2.textLayout     // Catch:{ Exception -> 0x02fe }
            int r9 = r7 + -1
            float r10 = r0.getLineLeft(r9)     // Catch:{ Exception -> 0x02fe }
            r9 = 0
            if (r6 != 0) goto L_0x0308
            int r0 = (r10 > r9 ? 1 : (r10 == r9 ? 0 : -1))
            if (r0 < 0) goto L_0x0308
            r1.textXOffset = r10     // Catch:{ Exception -> 0x02fc }
            goto L_0x0308
        L_0x02fc:
            r0 = move-exception
            goto L_0x0300
        L_0x02fe:
            r0 = move-exception
            r9 = 0
        L_0x0300:
            if (r6 != 0) goto L_0x0304
            r1.textXOffset = r9
        L_0x0304:
            org.telegram.messenger.FileLog.e((java.lang.Throwable) r0)
            r10 = 0
        L_0x0308:
            android.text.StaticLayout r0 = r2.textLayout     // Catch:{ Exception -> 0x0311 }
            int r11 = r7 + -1
            float r0 = r0.getLineWidth(r11)     // Catch:{ Exception -> 0x0311 }
            goto L_0x0316
        L_0x0311:
            r0 = move-exception
            org.telegram.messenger.FileLog.e((java.lang.Throwable) r0)
            r0 = 0
        L_0x0316:
            double r11 = (double) r0
            double r11 = java.lang.Math.ceil(r11)
            int r0 = (int) r11
            int r11 = r15 + 80
            if (r0 <= r11) goto L_0x0321
            r0 = r15
        L_0x0321:
            int r11 = r8 + -1
            if (r6 != r11) goto L_0x0327
            r1.lastLineWidth = r0
        L_0x0327:
            float r12 = (float) r0
            float r13 = java.lang.Math.max(r9, r10)
            float r13 = r13 + r12
            double r13 = (double) r13
            double r13 = java.lang.Math.ceil(r13)
            int r13 = (int) r13
            r14 = 1
            if (r7 <= r14) goto L_0x03f6
            r10 = r0
            r20 = r3
            r3 = r13
            r9 = 0
            r12 = 0
            r14 = 0
            r19 = 0
        L_0x033f:
            if (r9 >= r7) goto L_0x03d3
            android.text.StaticLayout r0 = r2.textLayout     // Catch:{ Exception -> 0x034a }
            float r0 = r0.getLineWidth(r9)     // Catch:{ Exception -> 0x034a }
            r21 = r0
            goto L_0x0350
        L_0x034a:
            r0 = move-exception
            org.telegram.messenger.FileLog.e((java.lang.Throwable) r0)
            r21 = 0
        L_0x0350:
            android.text.StaticLayout r0 = r2.textLayout     // Catch:{ Exception -> 0x0357 }
            float r0 = r0.getLineLeft(r9)     // Catch:{ Exception -> 0x0357 }
            goto L_0x035c
        L_0x0357:
            r0 = move-exception
            org.telegram.messenger.FileLog.e((java.lang.Throwable) r0)
            r0 = 0
        L_0x035c:
            r22 = r0
            int r0 = r15 + 20
            float r0 = (float) r0
            int r0 = (r21 > r0 ? 1 : (r21 == r0 ? 0 : -1))
            if (r0 <= 0) goto L_0x036d
            float r0 = (float) r15
            r27 = r5
            r21 = 0
            r5 = r0
            r0 = 0
            goto L_0x0375
        L_0x036d:
            r27 = r5
            r5 = r21
            r0 = r22
            r21 = 0
        L_0x0375:
            int r22 = (r0 > r21 ? 1 : (r0 == r21 ? 0 : -1))
            if (r22 <= 0) goto L_0x038f
            r21 = r4
            float r4 = r1.textXOffset
            float r4 = java.lang.Math.min(r4, r0)
            r1.textXOffset = r4
            byte r4 = r2.directionFlags
            r22 = r7
            r7 = 1
            r4 = r4 | r7
            byte r4 = (byte) r4
            r2.directionFlags = r4
            r1.hasRtl = r7
            goto L_0x039a
        L_0x038f:
            r21 = r4
            r22 = r7
            byte r4 = r2.directionFlags
            r4 = r4 | 2
            byte r4 = (byte) r4
            r2.directionFlags = r4
        L_0x039a:
            if (r19 != 0) goto L_0x03ac
            r4 = 0
            int r7 = (r0 > r4 ? 1 : (r0 == r4 ? 0 : -1))
            if (r7 != 0) goto L_0x03ac
            android.text.StaticLayout r4 = r2.textLayout     // Catch:{ Exception -> 0x03aa }
            int r4 = r4.getParagraphDirection(r9)     // Catch:{ Exception -> 0x03aa }
            r7 = 1
            if (r4 != r7) goto L_0x03ac
        L_0x03aa:
            r19 = 1
        L_0x03ac:
            float r14 = java.lang.Math.max(r14, r5)
            float r0 = r0 + r5
            float r12 = java.lang.Math.max(r12, r0)
            double r4 = (double) r5
            double r4 = java.lang.Math.ceil(r4)
            int r4 = (int) r4
            int r10 = java.lang.Math.max(r10, r4)
            double r4 = (double) r0
            double r4 = java.lang.Math.ceil(r4)
            int r0 = (int) r4
            int r3 = java.lang.Math.max(r3, r0)
            int r9 = r9 + 1
            r4 = r21
            r7 = r22
            r5 = r27
            goto L_0x033f
        L_0x03d3:
            r21 = r4
            r27 = r5
            r22 = r7
            if (r19 == 0) goto L_0x03e0
            if (r6 != r11) goto L_0x03e5
            r1.lastLineWidth = r13
            goto L_0x03e5
        L_0x03e0:
            if (r6 != r11) goto L_0x03e4
            r1.lastLineWidth = r10
        L_0x03e4:
            r12 = r14
        L_0x03e5:
            int r0 = r1.textWidth
            double r2 = (double) r12
            double r2 = java.lang.Math.ceil(r2)
            int r2 = (int) r2
            int r0 = java.lang.Math.max(r0, r2)
            r1.textWidth = r0
            r3 = 0
            r9 = 1
            goto L_0x0434
        L_0x03f6:
            r20 = r3
            r21 = r4
            r27 = r5
            r22 = r7
            r3 = 0
            int r4 = (r10 > r3 ? 1 : (r10 == r3 ? 0 : -1))
            if (r4 <= 0) goto L_0x0420
            float r4 = r1.textXOffset
            float r4 = java.lang.Math.min(r4, r10)
            r1.textXOffset = r4
            int r4 = (r4 > r3 ? 1 : (r4 == r3 ? 0 : -1))
            if (r4 != 0) goto L_0x0411
            float r12 = r12 + r10
            int r0 = (int) r12
        L_0x0411:
            r9 = 1
            if (r8 == r9) goto L_0x0416
            r4 = 1
            goto L_0x0417
        L_0x0416:
            r4 = 0
        L_0x0417:
            r1.hasRtl = r4
            byte r4 = r2.directionFlags
            r4 = r4 | r9
            byte r4 = (byte) r4
            r2.directionFlags = r4
            goto L_0x0428
        L_0x0420:
            r9 = 1
            byte r4 = r2.directionFlags
            r4 = r4 | 2
            byte r4 = (byte) r4
            r2.directionFlags = r4
        L_0x0428:
            int r2 = r1.textWidth
            int r0 = java.lang.Math.min(r15, r0)
            int r0 = java.lang.Math.max(r2, r0)
            r1.textWidth = r0
        L_0x0434:
            int r0 = r21 + r22
            goto L_0x0467
        L_0x0437:
            r0 = move-exception
            r20 = r3
            r21 = r4
            r27 = r5
            goto L_0x044e
        L_0x043f:
            r0 = move-exception
            r20 = r3
            r21 = r4
            r27 = r5
            goto L_0x044c
        L_0x0447:
            r0 = move-exception
            r20 = r3
            r21 = r4
        L_0x044c:
            r6 = r25
        L_0x044e:
            r8 = r26
            r3 = 0
            r9 = 1
            goto L_0x0462
        L_0x0453:
            r0 = move-exception
            r20 = r3
            r21 = r8
            r6 = r9
            r8 = r11
            r27 = r12
            r18 = r14
            r3 = 0
            r9 = 1
            r17 = 24
        L_0x0462:
            org.telegram.messenger.FileLog.e((java.lang.Throwable) r0)
        L_0x0465:
            r0 = r21
        L_0x0467:
            int r2 = r6 + 1
            r9 = r2
            r11 = r8
            r14 = r18
            r3 = r20
            r12 = r27
            r2 = 0
            r4 = 1
            r10 = 0
            r13 = 24
            r8 = r0
            goto L_0x0173
        L_0x0479:
            return
        L_0x047a:
            r0 = move-exception
            org.telegram.messenger.FileLog.e((java.lang.Throwable) r0)
        L_0x047e:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.messenger.MessageObject.generateLayout(org.telegram.tgnet.TLRPC$User):void");
    }

    public boolean isOut() {
        return this.messageOwner.out;
    }

    public boolean isOutOwner() {
        TLRPC$Peer tLRPC$Peer;
        int i;
        TLRPC$Peer tLRPC$Peer2 = this.messageOwner.peer_id;
        TLRPC$Chat tLRPC$Chat = null;
        if (!(tLRPC$Peer2 == null || (i = tLRPC$Peer2.channel_id) == 0)) {
            tLRPC$Chat = getChat((AbstractMap<Integer, TLRPC$Chat>) null, (SparseArray<TLRPC$Chat>) null, i);
        }
        TLRPC$Message tLRPC$Message = this.messageOwner;
        if (!tLRPC$Message.out) {
            return false;
        }
        TLRPC$Peer tLRPC$Peer3 = tLRPC$Message.from_id;
        if (!(tLRPC$Peer3 instanceof TLRPC$TL_peerUser)) {
            if (!(tLRPC$Peer3 instanceof TLRPC$TL_peerChannel)) {
                return false;
            }
            if (ChatObject.isChannel(tLRPC$Chat) && !tLRPC$Chat.megagroup) {
                return false;
            }
        }
        TLRPC$Message tLRPC$Message2 = this.messageOwner;
        if (tLRPC$Message2.post) {
            return false;
        }
        if (tLRPC$Message2.fwd_from == null) {
            return true;
        }
        int clientUserId = UserConfig.getInstance(this.currentAccount).getClientUserId();
        if (getDialogId() == ((long) clientUserId)) {
            TLRPC$MessageFwdHeader tLRPC$MessageFwdHeader = this.messageOwner.fwd_from;
            TLRPC$Peer tLRPC$Peer4 = tLRPC$MessageFwdHeader.from_id;
            if (!((tLRPC$Peer4 instanceof TLRPC$TL_peerUser) && tLRPC$Peer4.user_id == clientUserId && ((tLRPC$Peer = tLRPC$MessageFwdHeader.saved_from_peer) == null || tLRPC$Peer.user_id == clientUserId))) {
                TLRPC$Peer tLRPC$Peer5 = tLRPC$MessageFwdHeader.saved_from_peer;
                if (tLRPC$Peer5 == null || tLRPC$Peer5.user_id != clientUserId) {
                    return false;
                }
                if (tLRPC$Peer4 == null || tLRPC$Peer4.user_id == clientUserId) {
                    return true;
                }
                return false;
            }
            return true;
        }
        TLRPC$Peer tLRPC$Peer6 = this.messageOwner.fwd_from.saved_from_peer;
        if (tLRPC$Peer6 == null || tLRPC$Peer6.user_id == clientUserId) {
            return true;
        }
        return false;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:6:0x0014, code lost:
        r0 = r5.messageOwner.fwd_from;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean needDrawAvatar() {
        /*
            r5 = this;
            boolean r0 = r5.isFromUser()
            if (r0 != 0) goto L_0x0021
            boolean r0 = r5.isFromGroup()
            if (r0 != 0) goto L_0x0021
            long r0 = r5.eventId
            r2 = 0
            int r4 = (r0 > r2 ? 1 : (r0 == r2 ? 0 : -1))
            if (r4 != 0) goto L_0x0021
            org.telegram.tgnet.TLRPC$Message r0 = r5.messageOwner
            org.telegram.tgnet.TLRPC$MessageFwdHeader r0 = r0.fwd_from
            if (r0 == 0) goto L_0x001f
            org.telegram.tgnet.TLRPC$Peer r0 = r0.saved_from_peer
            if (r0 == 0) goto L_0x001f
            goto L_0x0021
        L_0x001f:
            r0 = 0
            goto L_0x0022
        L_0x0021:
            r0 = 1
        L_0x0022:
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.messenger.MessageObject.needDrawAvatar():boolean");
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x001a, code lost:
        r0 = r5.messageOwner.fwd_from;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean needDrawAvatarInternal() {
        /*
            r5 = this;
            boolean r0 = r5.isFromChat()
            if (r0 == 0) goto L_0x000c
            boolean r0 = r5.isFromUser()
            if (r0 != 0) goto L_0x0027
        L_0x000c:
            boolean r0 = r5.isFromGroup()
            if (r0 != 0) goto L_0x0027
            long r0 = r5.eventId
            r2 = 0
            int r4 = (r0 > r2 ? 1 : (r0 == r2 ? 0 : -1))
            if (r4 != 0) goto L_0x0027
            org.telegram.tgnet.TLRPC$Message r0 = r5.messageOwner
            org.telegram.tgnet.TLRPC$MessageFwdHeader r0 = r0.fwd_from
            if (r0 == 0) goto L_0x0025
            org.telegram.tgnet.TLRPC$Peer r0 = r0.saved_from_peer
            if (r0 == 0) goto L_0x0025
            goto L_0x0027
        L_0x0025:
            r0 = 0
            goto L_0x0028
        L_0x0027:
            r0 = 1
        L_0x0028:
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.messenger.MessageObject.needDrawAvatarInternal():boolean");
    }

    public boolean isFromChat() {
        TLRPC$Peer tLRPC$Peer;
        int i;
        if (getDialogId() == ((long) UserConfig.getInstance(this.currentAccount).clientUserId)) {
            return true;
        }
        TLRPC$Peer tLRPC$Peer2 = this.messageOwner.peer_id;
        TLRPC$Chat tLRPC$Chat = null;
        if (!(tLRPC$Peer2 == null || (i = tLRPC$Peer2.channel_id) == 0)) {
            tLRPC$Chat = getChat((AbstractMap<Integer, TLRPC$Chat>) null, (SparseArray<TLRPC$Chat>) null, i);
        }
        if ((ChatObject.isChannel(tLRPC$Chat) && tLRPC$Chat.megagroup) || ((tLRPC$Peer = this.messageOwner.peer_id) != null && tLRPC$Peer.chat_id != 0)) {
            return true;
        }
        if (tLRPC$Peer == null || tLRPC$Peer.channel_id == 0 || tLRPC$Chat == null || !tLRPC$Chat.megagroup) {
            return false;
        }
        return true;
    }

    public static int getFromChatId(TLRPC$Message tLRPC$Message) {
        return getPeerId(tLRPC$Message.from_id);
    }

    public static int getPeerId(TLRPC$Peer tLRPC$Peer) {
        int i;
        if (tLRPC$Peer == null) {
            return 0;
        }
        if (tLRPC$Peer instanceof TLRPC$TL_peerChat) {
            i = tLRPC$Peer.chat_id;
        } else if (!(tLRPC$Peer instanceof TLRPC$TL_peerChannel)) {
            return tLRPC$Peer.user_id;
        } else {
            i = tLRPC$Peer.channel_id;
        }
        return -i;
    }

    public int getFromChatId() {
        return getFromChatId(this.messageOwner);
    }

    public int getChatId() {
        TLRPC$Peer tLRPC$Peer = this.messageOwner.peer_id;
        if (tLRPC$Peer instanceof TLRPC$TL_peerChat) {
            return tLRPC$Peer.chat_id;
        }
        if (tLRPC$Peer instanceof TLRPC$TL_peerChannel) {
            return tLRPC$Peer.channel_id;
        }
        return 0;
    }

    public boolean isFromUser() {
        TLRPC$Message tLRPC$Message = this.messageOwner;
        return (tLRPC$Message.from_id instanceof TLRPC$TL_peerUser) && !tLRPC$Message.post;
    }

    public boolean isFromGroup() {
        int i;
        TLRPC$Peer tLRPC$Peer = this.messageOwner.peer_id;
        TLRPC$Chat tLRPC$Chat = null;
        if (!(tLRPC$Peer == null || (i = tLRPC$Peer.channel_id) == 0)) {
            tLRPC$Chat = getChat((AbstractMap<Integer, TLRPC$Chat>) null, (SparseArray<TLRPC$Chat>) null, i);
        }
        return (this.messageOwner.from_id instanceof TLRPC$TL_peerChannel) && ChatObject.isChannel(tLRPC$Chat) && tLRPC$Chat.megagroup;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:2:0x0008, code lost:
        r0 = r0.fwd_from;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean isForwardedChannelPost() {
        /*
            r2 = this;
            org.telegram.tgnet.TLRPC$Message r0 = r2.messageOwner
            org.telegram.tgnet.TLRPC$Peer r1 = r0.from_id
            boolean r1 = r1 instanceof org.telegram.tgnet.TLRPC$TL_peerChannel
            if (r1 == 0) goto L_0x0012
            org.telegram.tgnet.TLRPC$MessageFwdHeader r0 = r0.fwd_from
            if (r0 == 0) goto L_0x0012
            int r0 = r0.channel_post
            if (r0 == 0) goto L_0x0012
            r0 = 1
            goto L_0x0013
        L_0x0012:
            r0 = 0
        L_0x0013:
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.messenger.MessageObject.isForwardedChannelPost():boolean");
    }

    public boolean isUnread() {
        return this.messageOwner.unread;
    }

    public boolean isContentUnread() {
        return this.messageOwner.media_unread;
    }

    public void setIsRead() {
        this.messageOwner.unread = false;
    }

    public int getUnradFlags() {
        return getUnreadFlags(this.messageOwner);
    }

    public static int getUnreadFlags(TLRPC$Message tLRPC$Message) {
        int i = !tLRPC$Message.unread ? 1 : 0;
        return !tLRPC$Message.media_unread ? i | 2 : i;
    }

    public void setContentIsRead() {
        this.messageOwner.media_unread = false;
    }

    public int getId() {
        return this.messageOwner.id;
    }

    public int getRealId() {
        TLRPC$Message tLRPC$Message = this.messageOwner;
        int i = tLRPC$Message.realId;
        return i != 0 ? i : tLRPC$Message.id;
    }

    public static int getMessageSize(TLRPC$Message tLRPC$Message) {
        TLRPC$Document tLRPC$Document;
        TLRPC$MessageMedia tLRPC$MessageMedia = tLRPC$Message.media;
        if (tLRPC$MessageMedia instanceof TLRPC$TL_messageMediaWebPage) {
            tLRPC$Document = tLRPC$MessageMedia.webpage.document;
        } else if (tLRPC$MessageMedia instanceof TLRPC$TL_messageMediaGame) {
            tLRPC$Document = tLRPC$MessageMedia.game.document;
        } else {
            tLRPC$Document = tLRPC$MessageMedia != null ? tLRPC$MessageMedia.document : null;
        }
        if (tLRPC$Document != null) {
            return tLRPC$Document.size;
        }
        return 0;
    }

    public int getSize() {
        return getMessageSize(this.messageOwner);
    }

    public long getIdWithChannel() {
        return getIdWithChannel(this.messageOwner);
    }

    public static void fixMessagePeer(ArrayList<TLRPC$Message> arrayList, int i) {
        if (arrayList != null && !arrayList.isEmpty() && i != 0) {
            for (int i2 = 0; i2 < arrayList.size(); i2++) {
                TLRPC$Message tLRPC$Message = arrayList.get(i2);
                if (tLRPC$Message instanceof TLRPC$TL_messageEmpty) {
                    TLRPC$TL_peerChannel tLRPC$TL_peerChannel = new TLRPC$TL_peerChannel();
                    tLRPC$Message.peer_id = tLRPC$TL_peerChannel;
                    tLRPC$TL_peerChannel.channel_id = i;
                }
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:2:0x0007, code lost:
        r4 = r4.channel_id;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static long getIdWithChannel(org.telegram.tgnet.TLRPC$Message r4) {
        /*
            int r0 = r4.id
            long r0 = (long) r0
            org.telegram.tgnet.TLRPC$Peer r4 = r4.peer_id
            if (r4 == 0) goto L_0x0010
            int r4 = r4.channel_id
            if (r4 == 0) goto L_0x0010
            long r2 = (long) r4
            r4 = 32
            long r2 = r2 << r4
            long r0 = r0 | r2
        L_0x0010:
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.messenger.MessageObject.getIdWithChannel(org.telegram.tgnet.TLRPC$Message):long");
    }

    public int getChannelId() {
        TLRPC$Peer tLRPC$Peer = this.messageOwner.peer_id;
        if (tLRPC$Peer != null) {
            return tLRPC$Peer.channel_id;
        }
        return 0;
    }

    public static boolean shouldEncryptPhotoOrVideo(TLRPC$Message tLRPC$Message) {
        int i;
        if (!(tLRPC$Message instanceof TLRPC$TL_message_secret)) {
            TLRPC$MessageMedia tLRPC$MessageMedia = tLRPC$Message.media;
            if (((tLRPC$MessageMedia instanceof TLRPC$TL_messageMediaPhoto) || (tLRPC$MessageMedia instanceof TLRPC$TL_messageMediaDocument)) && tLRPC$MessageMedia.ttl_seconds != 0) {
                return true;
            }
            return false;
        } else if (((tLRPC$Message.media instanceof TLRPC$TL_messageMediaPhoto) || isVideoMessage(tLRPC$Message)) && (i = tLRPC$Message.ttl) > 0 && i <= 60) {
            return true;
        } else {
            return false;
        }
    }

    public boolean shouldEncryptPhotoOrVideo() {
        return shouldEncryptPhotoOrVideo(this.messageOwner);
    }

    public static boolean isSecretPhotoOrVideo(TLRPC$Message tLRPC$Message) {
        int i;
        if (tLRPC$Message instanceof TLRPC$TL_message_secret) {
            if (((tLRPC$Message.media instanceof TLRPC$TL_messageMediaPhoto) || isRoundVideoMessage(tLRPC$Message) || isVideoMessage(tLRPC$Message)) && (i = tLRPC$Message.ttl) > 0 && i <= 60) {
                return true;
            }
            return false;
        } else if (!(tLRPC$Message instanceof TLRPC$TL_message)) {
            return false;
        } else {
            TLRPC$MessageMedia tLRPC$MessageMedia = tLRPC$Message.media;
            if (((tLRPC$MessageMedia instanceof TLRPC$TL_messageMediaPhoto) || (tLRPC$MessageMedia instanceof TLRPC$TL_messageMediaDocument)) && tLRPC$MessageMedia.ttl_seconds != 0) {
                return true;
            }
            return false;
        }
    }

    public static boolean isSecretMedia(TLRPC$Message tLRPC$Message) {
        if (tLRPC$Message instanceof TLRPC$TL_message_secret) {
            if (((tLRPC$Message.media instanceof TLRPC$TL_messageMediaPhoto) || isRoundVideoMessage(tLRPC$Message) || isVideoMessage(tLRPC$Message)) && tLRPC$Message.media.ttl_seconds != 0) {
                return true;
            }
            return false;
        } else if (!(tLRPC$Message instanceof TLRPC$TL_message)) {
            return false;
        } else {
            TLRPC$MessageMedia tLRPC$MessageMedia = tLRPC$Message.media;
            if (((tLRPC$MessageMedia instanceof TLRPC$TL_messageMediaPhoto) || (tLRPC$MessageMedia instanceof TLRPC$TL_messageMediaDocument)) && tLRPC$MessageMedia.ttl_seconds != 0) {
                return true;
            }
            return false;
        }
    }

    public boolean needDrawBluredPreview() {
        TLRPC$Message tLRPC$Message = this.messageOwner;
        if (tLRPC$Message instanceof TLRPC$TL_message_secret) {
            int max = Math.max(tLRPC$Message.ttl, tLRPC$Message.media.ttl_seconds);
            if (max <= 0 || (((!(this.messageOwner.media instanceof TLRPC$TL_messageMediaPhoto) && !isVideo() && !isGif()) || max > 60) && !isRoundVideo())) {
                return false;
            }
            return true;
        } else if (!(tLRPC$Message instanceof TLRPC$TL_message)) {
            return false;
        } else {
            TLRPC$MessageMedia tLRPC$MessageMedia = tLRPC$Message.media;
            if (((tLRPC$MessageMedia instanceof TLRPC$TL_messageMediaPhoto) || (tLRPC$MessageMedia instanceof TLRPC$TL_messageMediaDocument)) && tLRPC$MessageMedia.ttl_seconds != 0) {
                return true;
            }
            return false;
        }
    }

    public boolean isSecretMedia() {
        int i;
        TLRPC$Message tLRPC$Message = this.messageOwner;
        if (tLRPC$Message instanceof TLRPC$TL_message_secret) {
            if ((((tLRPC$Message.media instanceof TLRPC$TL_messageMediaPhoto) || isGif()) && (i = this.messageOwner.ttl) > 0 && i <= 60) || isVoice() || isRoundVideo() || isVideo()) {
                return true;
            }
            return false;
        } else if (!(tLRPC$Message instanceof TLRPC$TL_message)) {
            return false;
        } else {
            TLRPC$MessageMedia tLRPC$MessageMedia = tLRPC$Message.media;
            if (((tLRPC$MessageMedia instanceof TLRPC$TL_messageMediaPhoto) || (tLRPC$MessageMedia instanceof TLRPC$TL_messageMediaDocument)) && tLRPC$MessageMedia.ttl_seconds != 0) {
                return true;
            }
            return false;
        }
    }

    public static void setUnreadFlags(TLRPC$Message tLRPC$Message, int i) {
        boolean z = false;
        tLRPC$Message.unread = (i & 1) == 0;
        if ((i & 2) == 0) {
            z = true;
        }
        tLRPC$Message.media_unread = z;
    }

    public static boolean isUnread(TLRPC$Message tLRPC$Message) {
        return tLRPC$Message.unread;
    }

    public static boolean isContentUnread(TLRPC$Message tLRPC$Message) {
        return tLRPC$Message.media_unread;
    }

    public boolean isSavedFromMegagroup() {
        TLRPC$Peer tLRPC$Peer;
        TLRPC$MessageFwdHeader tLRPC$MessageFwdHeader = this.messageOwner.fwd_from;
        if (tLRPC$MessageFwdHeader == null || (tLRPC$Peer = tLRPC$MessageFwdHeader.saved_from_peer) == null || tLRPC$Peer.channel_id == 0) {
            return false;
        }
        return ChatObject.isMegagroup(MessagesController.getInstance(this.currentAccount).getChat(Integer.valueOf(this.messageOwner.fwd_from.saved_from_peer.channel_id)));
    }

    public static boolean isOut(TLRPC$Message tLRPC$Message) {
        return tLRPC$Message.out;
    }

    public long getDialogId() {
        return getDialogId(this.messageOwner);
    }

    public boolean canStreamVideo() {
        TLRPC$Document document = getDocument();
        if (document != null && !(document instanceof TLRPC$TL_documentEncrypted)) {
            if (SharedConfig.streamAllVideo) {
                return true;
            }
            for (int i = 0; i < document.attributes.size(); i++) {
                TLRPC$DocumentAttribute tLRPC$DocumentAttribute = document.attributes.get(i);
                if (tLRPC$DocumentAttribute instanceof TLRPC$TL_documentAttributeVideo) {
                    return tLRPC$DocumentAttribute.supports_streaming;
                }
            }
            if (!SharedConfig.streamMkv || !"video/x-matroska".equals(document.mime_type)) {
                return false;
            }
            return true;
        }
        return false;
    }

    public static long getDialogId(TLRPC$Message tLRPC$Message) {
        TLRPC$Peer tLRPC$Peer;
        if (tLRPC$Message.dialog_id == 0 && (tLRPC$Peer = tLRPC$Message.peer_id) != null) {
            int i = tLRPC$Peer.chat_id;
            if (i != 0) {
                tLRPC$Message.dialog_id = (long) (-i);
            } else {
                int i2 = tLRPC$Peer.channel_id;
                if (i2 != 0) {
                    tLRPC$Message.dialog_id = (long) (-i2);
                } else if (tLRPC$Message.from_id == null || isOut(tLRPC$Message)) {
                    tLRPC$Message.dialog_id = (long) tLRPC$Message.peer_id.user_id;
                } else {
                    tLRPC$Message.dialog_id = (long) tLRPC$Message.from_id.user_id;
                }
            }
        }
        return tLRPC$Message.dialog_id;
    }

    public boolean isSending() {
        TLRPC$Message tLRPC$Message = this.messageOwner;
        return tLRPC$Message.send_state == 1 && tLRPC$Message.id < 0;
    }

    public boolean isEditing() {
        TLRPC$Message tLRPC$Message = this.messageOwner;
        return tLRPC$Message.send_state == 3 && tLRPC$Message.id > 0;
    }

    public boolean isEditingMedia() {
        TLRPC$MessageMedia tLRPC$MessageMedia = this.messageOwner.media;
        if (tLRPC$MessageMedia instanceof TLRPC$TL_messageMediaPhoto) {
            if (tLRPC$MessageMedia.photo.id == 0) {
                return true;
            }
            return false;
        } else if (!(tLRPC$MessageMedia instanceof TLRPC$TL_messageMediaDocument) || tLRPC$MessageMedia.document.dc_id != 0) {
            return false;
        } else {
            return true;
        }
    }

    public boolean isSendError() {
        TLRPC$Message tLRPC$Message = this.messageOwner;
        return (tLRPC$Message.send_state == 2 && tLRPC$Message.id < 0) || (this.scheduled && tLRPC$Message.id > 0 && tLRPC$Message.date < ConnectionsManager.getInstance(this.currentAccount).getCurrentTime() + -60);
    }

    public boolean isSent() {
        TLRPC$Message tLRPC$Message = this.messageOwner;
        return tLRPC$Message.send_state == 0 || tLRPC$Message.id > 0;
    }

    public int getSecretTimeLeft() {
        TLRPC$Message tLRPC$Message = this.messageOwner;
        int i = tLRPC$Message.ttl;
        int i2 = tLRPC$Message.destroyTime;
        return i2 != 0 ? Math.max(0, i2 - ConnectionsManager.getInstance(this.currentAccount).getCurrentTime()) : i;
    }

    public String getSecretTimeString() {
        if (!isSecretMedia()) {
            return null;
        }
        int secretTimeLeft = getSecretTimeLeft();
        if (secretTimeLeft < 60) {
            return secretTimeLeft + "s";
        }
        return (secretTimeLeft / 60) + "m";
    }

    public String getDocumentName() {
        return FileLoader.getDocumentFileName(getDocument());
    }

    public static boolean isStickerDocument(TLRPC$Document tLRPC$Document) {
        if (tLRPC$Document != null) {
            for (int i = 0; i < tLRPC$Document.attributes.size(); i++) {
                if (tLRPC$Document.attributes.get(i) instanceof TLRPC$TL_documentAttributeSticker) {
                    return "image/webp".equals(tLRPC$Document.mime_type);
                }
            }
        }
        return false;
    }

    public static boolean isStickerHasSet(TLRPC$Document tLRPC$Document) {
        TLRPC$InputStickerSet tLRPC$InputStickerSet;
        if (tLRPC$Document != null) {
            for (int i = 0; i < tLRPC$Document.attributes.size(); i++) {
                TLRPC$DocumentAttribute tLRPC$DocumentAttribute = tLRPC$Document.attributes.get(i);
                if ((tLRPC$DocumentAttribute instanceof TLRPC$TL_documentAttributeSticker) && (tLRPC$InputStickerSet = tLRPC$DocumentAttribute.stickerset) != null && !(tLRPC$InputStickerSet instanceof TLRPC$TL_inputStickerSetEmpty)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isAnimatedStickerDocument(TLRPC$Document tLRPC$Document, boolean z) {
        if (tLRPC$Document != null && (("application/x-tgsticker".equals(tLRPC$Document.mime_type) && !tLRPC$Document.thumbs.isEmpty()) || "application/x-tgsdice".equals(tLRPC$Document.mime_type))) {
            if (z) {
                return true;
            }
            int size = tLRPC$Document.attributes.size();
            for (int i = 0; i < size; i++) {
                TLRPC$DocumentAttribute tLRPC$DocumentAttribute = tLRPC$Document.attributes.get(i);
                if (tLRPC$DocumentAttribute instanceof TLRPC$TL_documentAttributeSticker) {
                    return tLRPC$DocumentAttribute.stickerset instanceof TLRPC$TL_inputStickerSetShortName;
                }
            }
        }
        return false;
    }

    public static boolean canAutoplayAnimatedSticker(TLRPC$Document tLRPC$Document) {
        return isAnimatedStickerDocument(tLRPC$Document, true) && SharedConfig.getDevicePerformanceClass() != 0;
    }

    public static boolean isMaskDocument(TLRPC$Document tLRPC$Document) {
        if (tLRPC$Document != null) {
            for (int i = 0; i < tLRPC$Document.attributes.size(); i++) {
                TLRPC$DocumentAttribute tLRPC$DocumentAttribute = tLRPC$Document.attributes.get(i);
                if ((tLRPC$DocumentAttribute instanceof TLRPC$TL_documentAttributeSticker) && tLRPC$DocumentAttribute.mask) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isVoiceDocument(TLRPC$Document tLRPC$Document) {
        if (tLRPC$Document != null) {
            for (int i = 0; i < tLRPC$Document.attributes.size(); i++) {
                TLRPC$DocumentAttribute tLRPC$DocumentAttribute = tLRPC$Document.attributes.get(i);
                if (tLRPC$DocumentAttribute instanceof TLRPC$TL_documentAttributeAudio) {
                    return tLRPC$DocumentAttribute.voice;
                }
            }
        }
        return false;
    }

    public static boolean isVoiceWebDocument(WebFile webFile) {
        return webFile != null && webFile.mime_type.equals("audio/ogg");
    }

    public static boolean isImageWebDocument(WebFile webFile) {
        return webFile != null && !isGifDocument(webFile) && webFile.mime_type.startsWith("image/");
    }

    public static boolean isVideoWebDocument(WebFile webFile) {
        return webFile != null && webFile.mime_type.startsWith("video/");
    }

    public static boolean isMusicDocument(TLRPC$Document tLRPC$Document) {
        if (tLRPC$Document != null) {
            for (int i = 0; i < tLRPC$Document.attributes.size(); i++) {
                TLRPC$DocumentAttribute tLRPC$DocumentAttribute = tLRPC$Document.attributes.get(i);
                if (tLRPC$DocumentAttribute instanceof TLRPC$TL_documentAttributeAudio) {
                    return !tLRPC$DocumentAttribute.voice;
                }
            }
            if (!TextUtils.isEmpty(tLRPC$Document.mime_type)) {
                String lowerCase = tLRPC$Document.mime_type.toLowerCase();
                if (lowerCase.equals("audio/flac") || lowerCase.equals("audio/ogg") || lowerCase.equals("audio/opus") || lowerCase.equals("audio/x-opus+ogg") || (lowerCase.equals("application/octet-stream") && FileLoader.getDocumentFileName(tLRPC$Document).endsWith(".opus"))) {
                    return true;
                }
            }
        }
        return false;
    }

    public static TLRPC$VideoSize getDocumentVideoThumb(TLRPC$Document tLRPC$Document) {
        if (tLRPC$Document == null || tLRPC$Document.video_thumbs.isEmpty()) {
            return null;
        }
        return tLRPC$Document.video_thumbs.get(0);
    }

    public static boolean isVideoDocument(TLRPC$Document tLRPC$Document) {
        if (tLRPC$Document == null) {
            return false;
        }
        boolean z = false;
        int i = 0;
        int i2 = 0;
        boolean z2 = false;
        for (int i3 = 0; i3 < tLRPC$Document.attributes.size(); i3++) {
            TLRPC$DocumentAttribute tLRPC$DocumentAttribute = tLRPC$Document.attributes.get(i3);
            if (tLRPC$DocumentAttribute instanceof TLRPC$TL_documentAttributeVideo) {
                if (tLRPC$DocumentAttribute.round_message) {
                    return false;
                }
                i = tLRPC$DocumentAttribute.w;
                i2 = tLRPC$DocumentAttribute.h;
                z2 = true;
            } else if (tLRPC$DocumentAttribute instanceof TLRPC$TL_documentAttributeAnimated) {
                z = true;
            }
        }
        if (z && (i > 1280 || i2 > 1280)) {
            z = false;
        }
        if (SharedConfig.streamMkv && !z2 && "video/x-matroska".equals(tLRPC$Document.mime_type)) {
            z2 = true;
        }
        if (!z2 || z) {
            return false;
        }
        return true;
    }

    public TLRPC$Document getDocument() {
        TLRPC$Document tLRPC$Document = this.emojiAnimatedSticker;
        if (tLRPC$Document != null) {
            return tLRPC$Document;
        }
        return getDocument(this.messageOwner);
    }

    public static TLRPC$Document getDocument(TLRPC$Message tLRPC$Message) {
        TLRPC$MessageMedia tLRPC$MessageMedia = tLRPC$Message.media;
        if (tLRPC$MessageMedia instanceof TLRPC$TL_messageMediaWebPage) {
            return tLRPC$MessageMedia.webpage.document;
        }
        if (tLRPC$MessageMedia instanceof TLRPC$TL_messageMediaGame) {
            return tLRPC$MessageMedia.game.document;
        }
        if (tLRPC$MessageMedia != null) {
            return tLRPC$MessageMedia.document;
        }
        return null;
    }

    public static TLRPC$Photo getPhoto(TLRPC$Message tLRPC$Message) {
        TLRPC$MessageMedia tLRPC$MessageMedia = tLRPC$Message.media;
        if (tLRPC$MessageMedia instanceof TLRPC$TL_messageMediaWebPage) {
            return tLRPC$MessageMedia.webpage.photo;
        }
        if (tLRPC$MessageMedia != null) {
            return tLRPC$MessageMedia.photo;
        }
        return null;
    }

    public static boolean isStickerMessage(TLRPC$Message tLRPC$Message) {
        TLRPC$MessageMedia tLRPC$MessageMedia = tLRPC$Message.media;
        return tLRPC$MessageMedia != null && isStickerDocument(tLRPC$MessageMedia.document);
    }

    public static boolean isAnimatedStickerMessage(TLRPC$Message tLRPC$Message) {
        TLRPC$MessageMedia tLRPC$MessageMedia;
        boolean isSecretDialogId = DialogObject.isSecretDialogId(tLRPC$Message.dialog_id);
        if ((isSecretDialogId && tLRPC$Message.stickerVerified != 1) || (tLRPC$MessageMedia = tLRPC$Message.media) == null) {
            return false;
        }
        if (isAnimatedStickerDocument(tLRPC$MessageMedia.document, !isSecretDialogId || tLRPC$Message.out)) {
            return true;
        }
        return false;
    }

    public static boolean isLocationMessage(TLRPC$Message tLRPC$Message) {
        TLRPC$MessageMedia tLRPC$MessageMedia = tLRPC$Message.media;
        return (tLRPC$MessageMedia instanceof TLRPC$TL_messageMediaGeo) || (tLRPC$MessageMedia instanceof TLRPC$TL_messageMediaGeoLive) || (tLRPC$MessageMedia instanceof TLRPC$TL_messageMediaVenue);
    }

    public static boolean isMaskMessage(TLRPC$Message tLRPC$Message) {
        TLRPC$MessageMedia tLRPC$MessageMedia = tLRPC$Message.media;
        return tLRPC$MessageMedia != null && isMaskDocument(tLRPC$MessageMedia.document);
    }

    public static boolean isMusicMessage(TLRPC$Message tLRPC$Message) {
        TLRPC$MessageMedia tLRPC$MessageMedia = tLRPC$Message.media;
        if (tLRPC$MessageMedia instanceof TLRPC$TL_messageMediaWebPage) {
            return isMusicDocument(tLRPC$MessageMedia.webpage.document);
        }
        return tLRPC$MessageMedia != null && isMusicDocument(tLRPC$MessageMedia.document);
    }

    public static boolean isGifMessage(TLRPC$Message tLRPC$Message) {
        TLRPC$MessageMedia tLRPC$MessageMedia = tLRPC$Message.media;
        if (tLRPC$MessageMedia instanceof TLRPC$TL_messageMediaWebPage) {
            return isGifDocument(tLRPC$MessageMedia.webpage.document);
        }
        if (tLRPC$MessageMedia != null) {
            if (isGifDocument(tLRPC$MessageMedia.document, tLRPC$Message.grouped_id != 0)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isRoundVideoMessage(TLRPC$Message tLRPC$Message) {
        TLRPC$MessageMedia tLRPC$MessageMedia = tLRPC$Message.media;
        if (tLRPC$MessageMedia instanceof TLRPC$TL_messageMediaWebPage) {
            return isRoundVideoDocument(tLRPC$MessageMedia.webpage.document);
        }
        return tLRPC$MessageMedia != null && isRoundVideoDocument(tLRPC$MessageMedia.document);
    }

    public static boolean isPhoto(TLRPC$Message tLRPC$Message) {
        TLRPC$MessageMedia tLRPC$MessageMedia = tLRPC$Message.media;
        if (!(tLRPC$MessageMedia instanceof TLRPC$TL_messageMediaWebPage)) {
            return tLRPC$MessageMedia instanceof TLRPC$TL_messageMediaPhoto;
        }
        TLRPC$WebPage tLRPC$WebPage = tLRPC$MessageMedia.webpage;
        return (tLRPC$WebPage.photo instanceof TLRPC$TL_photo) && !(tLRPC$WebPage.document instanceof TLRPC$TL_document);
    }

    public static boolean isVoiceMessage(TLRPC$Message tLRPC$Message) {
        TLRPC$MessageMedia tLRPC$MessageMedia = tLRPC$Message.media;
        if (tLRPC$MessageMedia instanceof TLRPC$TL_messageMediaWebPage) {
            return isVoiceDocument(tLRPC$MessageMedia.webpage.document);
        }
        return tLRPC$MessageMedia != null && isVoiceDocument(tLRPC$MessageMedia.document);
    }

    public static boolean isNewGifMessage(TLRPC$Message tLRPC$Message) {
        TLRPC$MessageMedia tLRPC$MessageMedia = tLRPC$Message.media;
        if (tLRPC$MessageMedia instanceof TLRPC$TL_messageMediaWebPage) {
            return isNewGifDocument(tLRPC$MessageMedia.webpage.document);
        }
        return tLRPC$MessageMedia != null && isNewGifDocument(tLRPC$MessageMedia.document);
    }

    public static boolean isLiveLocationMessage(TLRPC$Message tLRPC$Message) {
        return tLRPC$Message.media instanceof TLRPC$TL_messageMediaGeoLive;
    }

    public static boolean isVideoMessage(TLRPC$Message tLRPC$Message) {
        TLRPC$MessageMedia tLRPC$MessageMedia = tLRPC$Message.media;
        if (tLRPC$MessageMedia instanceof TLRPC$TL_messageMediaWebPage) {
            return isVideoDocument(tLRPC$MessageMedia.webpage.document);
        }
        return tLRPC$MessageMedia != null && isVideoDocument(tLRPC$MessageMedia.document);
    }

    public static boolean isGameMessage(TLRPC$Message tLRPC$Message) {
        return tLRPC$Message.media instanceof TLRPC$TL_messageMediaGame;
    }

    public static boolean isInvoiceMessage(TLRPC$Message tLRPC$Message) {
        return tLRPC$Message.media instanceof TLRPC$TL_messageMediaInvoice;
    }

    public static TLRPC$InputStickerSet getInputStickerSet(TLRPC$Message tLRPC$Message) {
        TLRPC$Document tLRPC$Document;
        TLRPC$MessageMedia tLRPC$MessageMedia = tLRPC$Message.media;
        if (tLRPC$MessageMedia == null || (tLRPC$Document = tLRPC$MessageMedia.document) == null) {
            return null;
        }
        return getInputStickerSet(tLRPC$Document);
    }

    public static TLRPC$InputStickerSet getInputStickerSet(TLRPC$Document tLRPC$Document) {
        if (tLRPC$Document == null) {
            return null;
        }
        int size = tLRPC$Document.attributes.size();
        for (int i = 0; i < size; i++) {
            TLRPC$DocumentAttribute tLRPC$DocumentAttribute = tLRPC$Document.attributes.get(i);
            if (tLRPC$DocumentAttribute instanceof TLRPC$TL_documentAttributeSticker) {
                TLRPC$InputStickerSet tLRPC$InputStickerSet = tLRPC$DocumentAttribute.stickerset;
                if (tLRPC$InputStickerSet instanceof TLRPC$TL_inputStickerSetEmpty) {
                    return null;
                }
                return tLRPC$InputStickerSet;
            }
        }
        return null;
    }

    public static long getStickerSetId(TLRPC$Document tLRPC$Document) {
        if (tLRPC$Document == null) {
            return -1;
        }
        for (int i = 0; i < tLRPC$Document.attributes.size(); i++) {
            TLRPC$DocumentAttribute tLRPC$DocumentAttribute = tLRPC$Document.attributes.get(i);
            if (tLRPC$DocumentAttribute instanceof TLRPC$TL_documentAttributeSticker) {
                TLRPC$InputStickerSet tLRPC$InputStickerSet = tLRPC$DocumentAttribute.stickerset;
                if (tLRPC$InputStickerSet instanceof TLRPC$TL_inputStickerSetEmpty) {
                    return -1;
                }
                return tLRPC$InputStickerSet.id;
            }
        }
        return -1;
    }

    public static String getStickerSetName(TLRPC$Document tLRPC$Document) {
        if (tLRPC$Document == null) {
            return null;
        }
        for (int i = 0; i < tLRPC$Document.attributes.size(); i++) {
            TLRPC$DocumentAttribute tLRPC$DocumentAttribute = tLRPC$Document.attributes.get(i);
            if (tLRPC$DocumentAttribute instanceof TLRPC$TL_documentAttributeSticker) {
                TLRPC$InputStickerSet tLRPC$InputStickerSet = tLRPC$DocumentAttribute.stickerset;
                if (tLRPC$InputStickerSet instanceof TLRPC$TL_inputStickerSetEmpty) {
                    return null;
                }
                return tLRPC$InputStickerSet.short_name;
            }
        }
        return null;
    }

    public String getStickerChar() {
        TLRPC$Document document = getDocument();
        if (document == null) {
            return null;
        }
        Iterator<TLRPC$DocumentAttribute> it = document.attributes.iterator();
        while (it.hasNext()) {
            TLRPC$DocumentAttribute next = it.next();
            if (next instanceof TLRPC$TL_documentAttributeSticker) {
                return next.alt;
            }
        }
        return null;
    }

    public int getApproximateHeight() {
        int i;
        int i2;
        int i3;
        int i4;
        int i5 = this.type;
        int i6 = 0;
        if (i5 == 0) {
            int i7 = this.textHeight;
            TLRPC$MessageMedia tLRPC$MessageMedia = this.messageOwner.media;
            if ((tLRPC$MessageMedia instanceof TLRPC$TL_messageMediaWebPage) && (tLRPC$MessageMedia.webpage instanceof TLRPC$TL_webPage)) {
                i6 = AndroidUtilities.dp(100.0f);
            }
            int i8 = i7 + i6;
            return isReply() ? i8 + AndroidUtilities.dp(42.0f) : i8;
        } else if (i5 == 2) {
            return AndroidUtilities.dp(72.0f);
        } else {
            if (i5 == 12) {
                return AndroidUtilities.dp(71.0f);
            }
            if (i5 == 9) {
                return AndroidUtilities.dp(100.0f);
            }
            if (i5 == 4) {
                return AndroidUtilities.dp(114.0f);
            }
            if (i5 == 14) {
                return AndroidUtilities.dp(82.0f);
            }
            if (i5 == 10) {
                return AndroidUtilities.dp(30.0f);
            }
            if (i5 == 11) {
                return AndroidUtilities.dp(50.0f);
            }
            if (i5 == 5) {
                return AndroidUtilities.roundMessageSize;
            }
            if (i5 == 13 || i5 == 15) {
                float f = ((float) AndroidUtilities.displaySize.y) * 0.4f;
                if (AndroidUtilities.isTablet()) {
                    i = AndroidUtilities.getMinTabletSide();
                } else {
                    i = AndroidUtilities.displaySize.x;
                }
                float f2 = ((float) i) * 0.5f;
                TLRPC$Document document = getDocument();
                int size = document.attributes.size();
                int i9 = 0;
                while (true) {
                    if (i9 >= size) {
                        i2 = 0;
                        break;
                    }
                    TLRPC$DocumentAttribute tLRPC$DocumentAttribute = document.attributes.get(i9);
                    if (tLRPC$DocumentAttribute instanceof TLRPC$TL_documentAttributeImageSize) {
                        i6 = tLRPC$DocumentAttribute.w;
                        i2 = tLRPC$DocumentAttribute.h;
                        break;
                    }
                    i9++;
                }
                if (i6 == 0) {
                    i2 = (int) f;
                    i6 = AndroidUtilities.dp(100.0f) + i2;
                }
                float f3 = (float) i2;
                if (f3 > f) {
                    i6 = (int) (((float) i6) * (f / f3));
                    i2 = (int) f;
                }
                float f4 = (float) i6;
                if (f4 > f2) {
                    i2 = (int) (((float) i2) * (f2 / f4));
                }
                return i2 + AndroidUtilities.dp(14.0f);
            }
            if (AndroidUtilities.isTablet()) {
                i3 = AndroidUtilities.getMinTabletSide();
            } else {
                Point point = AndroidUtilities.displaySize;
                i3 = Math.min(point.x, point.y);
            }
            int i10 = (int) (((float) i3) * 0.7f);
            int dp = AndroidUtilities.dp(100.0f) + i10;
            if (i10 > AndroidUtilities.getPhotoSize()) {
                i10 = AndroidUtilities.getPhotoSize();
            }
            if (dp > AndroidUtilities.getPhotoSize()) {
                dp = AndroidUtilities.getPhotoSize();
            }
            TLRPC$PhotoSize closestPhotoSizeWithSize = FileLoader.getClosestPhotoSizeWithSize(this.photoThumbs, AndroidUtilities.getPhotoSize());
            if (closestPhotoSizeWithSize != null) {
                int i11 = (int) (((float) closestPhotoSizeWithSize.h) / (((float) closestPhotoSizeWithSize.w) / ((float) i10)));
                if (i11 == 0) {
                    i11 = AndroidUtilities.dp(100.0f);
                }
                if (i11 <= dp) {
                    dp = i11 < AndroidUtilities.dp(120.0f) ? AndroidUtilities.dp(120.0f) : i11;
                }
                if (needDrawBluredPreview()) {
                    if (AndroidUtilities.isTablet()) {
                        i4 = AndroidUtilities.getMinTabletSide();
                    } else {
                        Point point2 = AndroidUtilities.displaySize;
                        i4 = Math.min(point2.x, point2.y);
                    }
                    dp = (int) (((float) i4) * 0.5f);
                }
            }
            return dp + AndroidUtilities.dp(14.0f);
        }
    }

    public String getStickerEmoji() {
        TLRPC$Document document = getDocument();
        if (document == null) {
            return null;
        }
        for (int i = 0; i < document.attributes.size(); i++) {
            TLRPC$DocumentAttribute tLRPC$DocumentAttribute = document.attributes.get(i);
            if (tLRPC$DocumentAttribute instanceof TLRPC$TL_documentAttributeSticker) {
                String str = tLRPC$DocumentAttribute.alt;
                if (str == null || str.length() <= 0) {
                    return null;
                }
                return tLRPC$DocumentAttribute.alt;
            }
        }
        return null;
    }

    public boolean isVideoCall() {
        TLRPC$MessageAction tLRPC$MessageAction = this.messageOwner.action;
        return (tLRPC$MessageAction instanceof TLRPC$TL_messageActionPhoneCall) && tLRPC$MessageAction.video;
    }

    public boolean isAnimatedEmoji() {
        return this.emojiAnimatedSticker != null;
    }

    public boolean isDice() {
        return this.messageOwner.media instanceof TLRPC$TL_messageMediaDice;
    }

    public String getDiceEmoji() {
        if (!isDice()) {
            return null;
        }
        TLRPC$TL_messageMediaDice tLRPC$TL_messageMediaDice = (TLRPC$TL_messageMediaDice) this.messageOwner.media;
        if (TextUtils.isEmpty(tLRPC$TL_messageMediaDice.emoticon)) {
            return "🎲";
        }
        return tLRPC$TL_messageMediaDice.emoticon.replace("️", "");
    }

    public int getDiceValue() {
        TLRPC$MessageMedia tLRPC$MessageMedia = this.messageOwner.media;
        if (tLRPC$MessageMedia instanceof TLRPC$TL_messageMediaDice) {
            return ((TLRPC$TL_messageMediaDice) tLRPC$MessageMedia).value;
        }
        return -1;
    }

    public boolean isSticker() {
        int i = this.type;
        if (i != 1000) {
            return i == 13;
        }
        return isStickerDocument(getDocument());
    }

    public boolean isAnimatedSticker() {
        int i = this.type;
        boolean z = false;
        if (i != 1000) {
            return i == 15;
        }
        boolean isSecretDialogId = DialogObject.isSecretDialogId(getDialogId());
        if (isSecretDialogId && this.messageOwner.stickerVerified != 1) {
            return false;
        }
        TLRPC$Document document = getDocument();
        if (this.emojiAnimatedSticker != null || !isSecretDialogId || isOut()) {
            z = true;
        }
        return isAnimatedStickerDocument(document, z);
    }

    public boolean isAnyKindOfSticker() {
        int i = this.type;
        return i == 13 || i == 15;
    }

    public boolean shouldDrawWithoutBackground() {
        int i = this.type;
        return i == 13 || i == 15 || i == 5;
    }

    public boolean isLocation() {
        return isLocationMessage(this.messageOwner);
    }

    public boolean isMask() {
        return isMaskMessage(this.messageOwner);
    }

    public boolean isMusic() {
        return isMusicMessage(this.messageOwner);
    }

    public boolean isDocument() {
        return getDocument() != null && !isVideo() && !isMusic() && !isVoice() && !isAnyKindOfSticker();
    }

    public boolean isVoice() {
        return isVoiceMessage(this.messageOwner);
    }

    public boolean isVideo() {
        return isVideoMessage(this.messageOwner);
    }

    public boolean isPhoto() {
        return isPhoto(this.messageOwner);
    }

    public boolean isLiveLocation() {
        return isLiveLocationMessage(this.messageOwner);
    }

    public boolean isExpiredLiveLocation(int i) {
        TLRPC$Message tLRPC$Message = this.messageOwner;
        return tLRPC$Message.date + tLRPC$Message.media.period <= i;
    }

    public boolean isGame() {
        return isGameMessage(this.messageOwner);
    }

    public boolean isInvoice() {
        return isInvoiceMessage(this.messageOwner);
    }

    public boolean isRoundVideo() {
        if (this.isRoundVideoCached == 0) {
            this.isRoundVideoCached = (this.type == 5 || isRoundVideoMessage(this.messageOwner)) ? 1 : 2;
        }
        if (this.isRoundVideoCached == 1) {
            return true;
        }
        return false;
    }

    public boolean hasAttachedStickers() {
        TLRPC$MessageMedia tLRPC$MessageMedia = this.messageOwner.media;
        if (tLRPC$MessageMedia instanceof TLRPC$TL_messageMediaPhoto) {
            TLRPC$Photo tLRPC$Photo = tLRPC$MessageMedia.photo;
            if (tLRPC$Photo == null || !tLRPC$Photo.has_stickers) {
                return false;
            }
            return true;
        } else if (tLRPC$MessageMedia instanceof TLRPC$TL_messageMediaDocument) {
            return isDocumentHasAttachedStickers(tLRPC$MessageMedia.document);
        } else {
            return false;
        }
    }

    public static boolean isDocumentHasAttachedStickers(TLRPC$Document tLRPC$Document) {
        if (tLRPC$Document != null) {
            for (int i = 0; i < tLRPC$Document.attributes.size(); i++) {
                if (tLRPC$Document.attributes.get(i) instanceof TLRPC$TL_documentAttributeHasStickers) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isGif() {
        return isGifMessage(this.messageOwner);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:2:0x0008, code lost:
        r0 = r0.webpage.document;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean isWebpageDocument() {
        /*
            r2 = this;
            org.telegram.tgnet.TLRPC$Message r0 = r2.messageOwner
            org.telegram.tgnet.TLRPC$MessageMedia r0 = r0.media
            boolean r1 = r0 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaWebPage
            if (r1 == 0) goto L_0x0016
            org.telegram.tgnet.TLRPC$WebPage r0 = r0.webpage
            org.telegram.tgnet.TLRPC$Document r0 = r0.document
            if (r0 == 0) goto L_0x0016
            boolean r0 = isGifDocument((org.telegram.tgnet.TLRPC$Document) r0)
            if (r0 != 0) goto L_0x0016
            r0 = 1
            goto L_0x0017
        L_0x0016:
            r0 = 0
        L_0x0017:
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.messenger.MessageObject.isWebpageDocument():boolean");
    }

    public boolean isWebpage() {
        return this.messageOwner.media instanceof TLRPC$TL_messageMediaWebPage;
    }

    public boolean isNewGif() {
        return this.messageOwner.media != null && isNewGifDocument(getDocument());
    }

    public boolean isAndroidTheme() {
        TLRPC$WebPage tLRPC$WebPage;
        TLRPC$MessageMedia tLRPC$MessageMedia = this.messageOwner.media;
        if (!(tLRPC$MessageMedia == null || (tLRPC$WebPage = tLRPC$MessageMedia.webpage) == null || tLRPC$WebPage.attributes.isEmpty())) {
            int size = this.messageOwner.media.webpage.attributes.size();
            for (int i = 0; i < size; i++) {
                TLRPC$TL_webPageAttributeTheme tLRPC$TL_webPageAttributeTheme = this.messageOwner.media.webpage.attributes.get(i);
                ArrayList<TLRPC$Document> arrayList = tLRPC$TL_webPageAttributeTheme.documents;
                int size2 = arrayList.size();
                for (int i2 = 0; i2 < size2; i2++) {
                    if ("application/x-tgtheme-android".equals(arrayList.get(i2).mime_type)) {
                        return true;
                    }
                }
                if (tLRPC$TL_webPageAttributeTheme.settings != null) {
                    return true;
                }
            }
        }
        return false;
    }

    public String getMusicTitle() {
        return getMusicTitle(true);
    }

    public String getMusicTitle(boolean z) {
        TLRPC$Document document = getDocument();
        if (document != null) {
            int i = 0;
            while (i < document.attributes.size()) {
                TLRPC$DocumentAttribute tLRPC$DocumentAttribute = document.attributes.get(i);
                if (tLRPC$DocumentAttribute instanceof TLRPC$TL_documentAttributeAudio) {
                    if (!tLRPC$DocumentAttribute.voice) {
                        String str = tLRPC$DocumentAttribute.title;
                        if (str != null && str.length() != 0) {
                            return str;
                        }
                        String documentFileName = FileLoader.getDocumentFileName(document);
                        return (!TextUtils.isEmpty(documentFileName) || !z) ? documentFileName : LocaleController.getString("AudioUnknownTitle", NUM);
                    } else if (!z) {
                        return null;
                    } else {
                        return LocaleController.formatDateAudio((long) this.messageOwner.date, true);
                    }
                } else if ((tLRPC$DocumentAttribute instanceof TLRPC$TL_documentAttributeVideo) && tLRPC$DocumentAttribute.round_message) {
                    return LocaleController.formatDateAudio((long) this.messageOwner.date, true);
                } else {
                    i++;
                }
            }
            String documentFileName2 = FileLoader.getDocumentFileName(document);
            if (!TextUtils.isEmpty(documentFileName2)) {
                return documentFileName2;
            }
        }
        return LocaleController.getString("AudioUnknownTitle", NUM);
    }

    public int getDuration() {
        TLRPC$Document document = getDocument();
        if (document == null) {
            return 0;
        }
        int i = this.audioPlayerDuration;
        if (i > 0) {
            return i;
        }
        for (int i2 = 0; i2 < document.attributes.size(); i2++) {
            TLRPC$DocumentAttribute tLRPC$DocumentAttribute = document.attributes.get(i2);
            if (tLRPC$DocumentAttribute instanceof TLRPC$TL_documentAttributeAudio) {
                return tLRPC$DocumentAttribute.duration;
            }
            if (tLRPC$DocumentAttribute instanceof TLRPC$TL_documentAttributeVideo) {
                return tLRPC$DocumentAttribute.duration;
            }
        }
        return this.audioPlayerDuration;
    }

    public String getArtworkUrl(boolean z) {
        TLRPC$Document document = getDocument();
        if (document != null) {
            int size = document.attributes.size();
            int i = 0;
            while (i < size) {
                TLRPC$DocumentAttribute tLRPC$DocumentAttribute = document.attributes.get(i);
                if (!(tLRPC$DocumentAttribute instanceof TLRPC$TL_documentAttributeAudio)) {
                    i++;
                } else if (tLRPC$DocumentAttribute.voice) {
                    return null;
                } else {
                    String str = tLRPC$DocumentAttribute.performer;
                    String str2 = tLRPC$DocumentAttribute.title;
                    if (!TextUtils.isEmpty(str)) {
                        int i2 = 0;
                        while (true) {
                            String[] strArr = excludeWords;
                            if (i2 >= strArr.length) {
                                break;
                            }
                            str = str.replace(strArr[i2], " ");
                            i2++;
                        }
                    }
                    if (TextUtils.isEmpty(str) && TextUtils.isEmpty(str2)) {
                        return null;
                    }
                    try {
                        StringBuilder sb = new StringBuilder();
                        sb.append("athumb://itunes.apple.com/search?term=");
                        sb.append(URLEncoder.encode(str + " - " + str2, "UTF-8"));
                        sb.append("&entity=song&limit=4");
                        sb.append(z ? "&s=1" : "");
                        return sb.toString();
                    } catch (Exception unused) {
                    }
                }
            }
        }
        return null;
    }

    public String getMusicAuthor() {
        return getMusicAuthor(true);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:17:0x003d, code lost:
        if (r5.round_message != false) goto L_0x0026;
     */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x0042  */
    /* JADX WARNING: Removed duplicated region for block: B:78:0x0151 A[SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public java.lang.String getMusicAuthor(boolean r11) {
        /*
            r10 = this;
            org.telegram.tgnet.TLRPC$Document r0 = r10.getDocument()
            r1 = 2131624403(0x7f0e01d3, float:1.8875985E38)
            java.lang.String r2 = "AudioUnknownArtist"
            if (r0 == 0) goto L_0x0155
            r3 = 0
            r4 = 0
        L_0x000d:
            java.util.ArrayList<org.telegram.tgnet.TLRPC$DocumentAttribute> r5 = r0.attributes
            int r5 = r5.size()
            if (r3 >= r5) goto L_0x0155
            java.util.ArrayList<org.telegram.tgnet.TLRPC$DocumentAttribute> r5 = r0.attributes
            java.lang.Object r5 = r5.get(r3)
            org.telegram.tgnet.TLRPC$DocumentAttribute r5 = (org.telegram.tgnet.TLRPC$DocumentAttribute) r5
            boolean r6 = r5 instanceof org.telegram.tgnet.TLRPC$TL_documentAttributeAudio
            r7 = 1
            if (r6 == 0) goto L_0x0037
            boolean r4 = r5.voice
            if (r4 == 0) goto L_0x0028
        L_0x0026:
            r4 = 1
            goto L_0x0040
        L_0x0028:
            java.lang.String r0 = r5.performer
            boolean r3 = android.text.TextUtils.isEmpty(r0)
            if (r3 == 0) goto L_0x0036
            if (r11 == 0) goto L_0x0036
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r2, r1)
        L_0x0036:
            return r0
        L_0x0037:
            boolean r6 = r5 instanceof org.telegram.tgnet.TLRPC$TL_documentAttributeVideo
            if (r6 == 0) goto L_0x0040
            boolean r5 = r5.round_message
            if (r5 == 0) goto L_0x0040
            goto L_0x0026
        L_0x0040:
            if (r4 == 0) goto L_0x0151
            r5 = 0
            if (r11 != 0) goto L_0x0046
            return r5
        L_0x0046:
            boolean r6 = r10.isOutOwner()
            if (r6 != 0) goto L_0x0147
            org.telegram.tgnet.TLRPC$Message r6 = r10.messageOwner
            org.telegram.tgnet.TLRPC$MessageFwdHeader r6 = r6.fwd_from
            if (r6 == 0) goto L_0x0068
            org.telegram.tgnet.TLRPC$Peer r6 = r6.from_id
            boolean r7 = r6 instanceof org.telegram.tgnet.TLRPC$TL_peerUser
            if (r7 == 0) goto L_0x0068
            int r6 = r6.user_id
            int r7 = r10.currentAccount
            org.telegram.messenger.UserConfig r7 = org.telegram.messenger.UserConfig.getInstance(r7)
            int r7 = r7.getClientUserId()
            if (r6 != r7) goto L_0x0068
            goto L_0x0147
        L_0x0068:
            org.telegram.tgnet.TLRPC$Message r6 = r10.messageOwner
            org.telegram.tgnet.TLRPC$MessageFwdHeader r7 = r6.fwd_from
            if (r7 == 0) goto L_0x008c
            org.telegram.tgnet.TLRPC$Peer r8 = r7.from_id
            boolean r8 = r8 instanceof org.telegram.tgnet.TLRPC$TL_peerChannel
            if (r8 == 0) goto L_0x008c
            int r6 = r10.currentAccount
            org.telegram.messenger.MessagesController r6 = org.telegram.messenger.MessagesController.getInstance(r6)
            org.telegram.tgnet.TLRPC$Message r7 = r10.messageOwner
            org.telegram.tgnet.TLRPC$MessageFwdHeader r7 = r7.fwd_from
            org.telegram.tgnet.TLRPC$Peer r7 = r7.from_id
            int r7 = r7.channel_id
            java.lang.Integer r7 = java.lang.Integer.valueOf(r7)
            org.telegram.tgnet.TLRPC$Chat r6 = r6.getChat(r7)
            goto L_0x013b
        L_0x008c:
            if (r7 == 0) goto L_0x00ac
            org.telegram.tgnet.TLRPC$Peer r8 = r7.from_id
            boolean r8 = r8 instanceof org.telegram.tgnet.TLRPC$TL_peerChat
            if (r8 == 0) goto L_0x00ac
            int r6 = r10.currentAccount
            org.telegram.messenger.MessagesController r6 = org.telegram.messenger.MessagesController.getInstance(r6)
            org.telegram.tgnet.TLRPC$Message r7 = r10.messageOwner
            org.telegram.tgnet.TLRPC$MessageFwdHeader r7 = r7.fwd_from
            org.telegram.tgnet.TLRPC$Peer r7 = r7.from_id
            int r7 = r7.chat_id
            java.lang.Integer r7 = java.lang.Integer.valueOf(r7)
            org.telegram.tgnet.TLRPC$Chat r6 = r6.getChat(r7)
            goto L_0x013b
        L_0x00ac:
            if (r7 == 0) goto L_0x00ce
            org.telegram.tgnet.TLRPC$Peer r8 = r7.from_id
            boolean r8 = r8 instanceof org.telegram.tgnet.TLRPC$TL_peerUser
            if (r8 == 0) goto L_0x00ce
            int r6 = r10.currentAccount
            org.telegram.messenger.MessagesController r6 = org.telegram.messenger.MessagesController.getInstance(r6)
            org.telegram.tgnet.TLRPC$Message r7 = r10.messageOwner
            org.telegram.tgnet.TLRPC$MessageFwdHeader r7 = r7.fwd_from
            org.telegram.tgnet.TLRPC$Peer r7 = r7.from_id
            int r7 = r7.user_id
            java.lang.Integer r7 = java.lang.Integer.valueOf(r7)
            org.telegram.tgnet.TLRPC$User r6 = r6.getUser(r7)
        L_0x00ca:
            r9 = r6
            r6 = r5
            r5 = r9
            goto L_0x013b
        L_0x00ce:
            if (r7 == 0) goto L_0x00d5
            java.lang.String r7 = r7.from_name
            if (r7 == 0) goto L_0x00d5
            return r7
        L_0x00d5:
            org.telegram.tgnet.TLRPC$Peer r7 = r6.from_id
            boolean r8 = r7 instanceof org.telegram.tgnet.TLRPC$TL_peerChat
            if (r8 == 0) goto L_0x00f0
            int r6 = r10.currentAccount
            org.telegram.messenger.MessagesController r6 = org.telegram.messenger.MessagesController.getInstance(r6)
            org.telegram.tgnet.TLRPC$Message r7 = r10.messageOwner
            org.telegram.tgnet.TLRPC$Peer r7 = r7.from_id
            int r7 = r7.chat_id
            java.lang.Integer r7 = java.lang.Integer.valueOf(r7)
            org.telegram.tgnet.TLRPC$Chat r6 = r6.getChat(r7)
            goto L_0x013b
        L_0x00f0:
            boolean r8 = r7 instanceof org.telegram.tgnet.TLRPC$TL_peerChannel
            if (r8 == 0) goto L_0x0109
            int r6 = r10.currentAccount
            org.telegram.messenger.MessagesController r6 = org.telegram.messenger.MessagesController.getInstance(r6)
            org.telegram.tgnet.TLRPC$Message r7 = r10.messageOwner
            org.telegram.tgnet.TLRPC$Peer r7 = r7.from_id
            int r7 = r7.channel_id
            java.lang.Integer r7 = java.lang.Integer.valueOf(r7)
            org.telegram.tgnet.TLRPC$Chat r6 = r6.getChat(r7)
            goto L_0x013b
        L_0x0109:
            if (r7 != 0) goto L_0x0126
            org.telegram.tgnet.TLRPC$Peer r6 = r6.peer_id
            int r6 = r6.channel_id
            if (r6 == 0) goto L_0x0126
            int r6 = r10.currentAccount
            org.telegram.messenger.MessagesController r6 = org.telegram.messenger.MessagesController.getInstance(r6)
            org.telegram.tgnet.TLRPC$Message r7 = r10.messageOwner
            org.telegram.tgnet.TLRPC$Peer r7 = r7.peer_id
            int r7 = r7.channel_id
            java.lang.Integer r7 = java.lang.Integer.valueOf(r7)
            org.telegram.tgnet.TLRPC$Chat r6 = r6.getChat(r7)
            goto L_0x013b
        L_0x0126:
            int r6 = r10.currentAccount
            org.telegram.messenger.MessagesController r6 = org.telegram.messenger.MessagesController.getInstance(r6)
            org.telegram.tgnet.TLRPC$Message r7 = r10.messageOwner
            org.telegram.tgnet.TLRPC$Peer r7 = r7.from_id
            int r7 = r7.user_id
            java.lang.Integer r7 = java.lang.Integer.valueOf(r7)
            org.telegram.tgnet.TLRPC$User r6 = r6.getUser(r7)
            goto L_0x00ca
        L_0x013b:
            if (r5 == 0) goto L_0x0142
            java.lang.String r11 = org.telegram.messenger.UserObject.getUserName(r5)
            return r11
        L_0x0142:
            if (r6 == 0) goto L_0x0151
            java.lang.String r11 = r6.title
            return r11
        L_0x0147:
            r11 = 2131625638(0x7f0e06a6, float:1.887849E38)
            java.lang.String r0 = "FromYou"
            java.lang.String r11 = org.telegram.messenger.LocaleController.getString(r0, r11)
            return r11
        L_0x0151:
            int r3 = r3 + 1
            goto L_0x000d
        L_0x0155:
            java.lang.String r11 = org.telegram.messenger.LocaleController.getString(r2, r1)
            return r11
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.messenger.MessageObject.getMusicAuthor(boolean):java.lang.String");
    }

    public TLRPC$InputStickerSet getInputStickerSet() {
        return getInputStickerSet(this.messageOwner);
    }

    public boolean isForwarded() {
        return isForwardedMessage(this.messageOwner);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:11:0x001e, code lost:
        if (r1.channel_id != r0.channel_id) goto L_0x0020;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean needDrawForwarded() {
        /*
            r5 = this;
            org.telegram.tgnet.TLRPC$Message r0 = r5.messageOwner
            int r1 = r0.flags
            r1 = r1 & 4
            if (r1 == 0) goto L_0x0035
            org.telegram.tgnet.TLRPC$MessageFwdHeader r0 = r0.fwd_from
            if (r0 == 0) goto L_0x0035
            boolean r1 = r0.imported
            if (r1 != 0) goto L_0x0035
            org.telegram.tgnet.TLRPC$Peer r1 = r0.saved_from_peer
            if (r1 == 0) goto L_0x0020
            org.telegram.tgnet.TLRPC$Peer r0 = r0.from_id
            boolean r2 = r0 instanceof org.telegram.tgnet.TLRPC$TL_peerChannel
            if (r2 == 0) goto L_0x0035
            int r1 = r1.channel_id
            int r0 = r0.channel_id
            if (r1 == r0) goto L_0x0035
        L_0x0020:
            int r0 = r5.currentAccount
            org.telegram.messenger.UserConfig r0 = org.telegram.messenger.UserConfig.getInstance(r0)
            int r0 = r0.getClientUserId()
            long r0 = (long) r0
            long r2 = r5.getDialogId()
            int r4 = (r0 > r2 ? 1 : (r0 == r2 ? 0 : -1))
            if (r4 == 0) goto L_0x0035
            r0 = 1
            goto L_0x0036
        L_0x0035:
            r0 = 0
        L_0x0036:
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.messenger.MessageObject.needDrawForwarded():boolean");
    }

    public static boolean isForwardedMessage(TLRPC$Message tLRPC$Message) {
        return ((tLRPC$Message.flags & 4) == 0 || tLRPC$Message.fwd_from == null) ? false : true;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:4:0x000a, code lost:
        r0 = r6.messageOwner;
        r1 = r0.reply_to;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean isReply() {
        /*
            r6 = this;
            org.telegram.messenger.MessageObject r0 = r6.replyMessageObject
            if (r0 == 0) goto L_0x000a
            org.telegram.tgnet.TLRPC$Message r0 = r0.messageOwner
            boolean r0 = r0 instanceof org.telegram.tgnet.TLRPC$TL_messageEmpty
            if (r0 != 0) goto L_0x0024
        L_0x000a:
            org.telegram.tgnet.TLRPC$Message r0 = r6.messageOwner
            org.telegram.tgnet.TLRPC$TL_messageReplyHeader r1 = r0.reply_to
            if (r1 == 0) goto L_0x0024
            int r2 = r1.reply_to_msg_id
            if (r2 != 0) goto L_0x001c
            long r1 = r1.reply_to_random_id
            r3 = 0
            int r5 = (r1 > r3 ? 1 : (r1 == r3 ? 0 : -1))
            if (r5 == 0) goto L_0x0024
        L_0x001c:
            int r0 = r0.flags
            r0 = r0 & 8
            if (r0 == 0) goto L_0x0024
            r0 = 1
            goto L_0x0025
        L_0x0024:
            r0 = 0
        L_0x0025:
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.messenger.MessageObject.isReply():boolean");
    }

    public boolean isMediaEmpty() {
        return isMediaEmpty(this.messageOwner);
    }

    public boolean isMediaEmptyWebpage() {
        return isMediaEmptyWebpage(this.messageOwner);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:1:0x0002, code lost:
        r1 = r1.media;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean isMediaEmpty(org.telegram.tgnet.TLRPC$Message r1) {
        /*
            if (r1 == 0) goto L_0x0011
            org.telegram.tgnet.TLRPC$MessageMedia r1 = r1.media
            if (r1 == 0) goto L_0x0011
            boolean r0 = r1 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaEmpty
            if (r0 != 0) goto L_0x0011
            boolean r1 = r1 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaWebPage
            if (r1 == 0) goto L_0x000f
            goto L_0x0011
        L_0x000f:
            r1 = 0
            goto L_0x0012
        L_0x0011:
            r1 = 1
        L_0x0012:
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.messenger.MessageObject.isMediaEmpty(org.telegram.tgnet.TLRPC$Message):boolean");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:1:0x0002, code lost:
        r0 = r0.media;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean isMediaEmptyWebpage(org.telegram.tgnet.TLRPC$Message r0) {
        /*
            if (r0 == 0) goto L_0x000d
            org.telegram.tgnet.TLRPC$MessageMedia r0 = r0.media
            if (r0 == 0) goto L_0x000d
            boolean r0 = r0 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaEmpty
            if (r0 == 0) goto L_0x000b
            goto L_0x000d
        L_0x000b:
            r0 = 0
            goto L_0x000e
        L_0x000d:
            r0 = 1
        L_0x000e:
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.messenger.MessageObject.isMediaEmptyWebpage(org.telegram.tgnet.TLRPC$Message):boolean");
    }

    public boolean hasReplies() {
        TLRPC$TL_messageReplies tLRPC$TL_messageReplies = this.messageOwner.replies;
        return tLRPC$TL_messageReplies != null && tLRPC$TL_messageReplies.replies > 0;
    }

    public boolean canViewThread() {
        MessageObject messageObject;
        if (this.messageOwner.action != null) {
            return false;
        }
        if (hasReplies() || (((messageObject = this.replyMessageObject) != null && messageObject.messageOwner.replies != null) || getReplyTopMsgId() != 0)) {
            return true;
        }
        return false;
    }

    public boolean isComments() {
        TLRPC$TL_messageReplies tLRPC$TL_messageReplies = this.messageOwner.replies;
        return tLRPC$TL_messageReplies != null && tLRPC$TL_messageReplies.comments;
    }

    public boolean isLinkedToChat(int i) {
        TLRPC$TL_messageReplies tLRPC$TL_messageReplies = this.messageOwner.replies;
        return tLRPC$TL_messageReplies != null && (i == 0 || tLRPC$TL_messageReplies.channel_id == i);
    }

    public int getRepliesCount() {
        TLRPC$TL_messageReplies tLRPC$TL_messageReplies = this.messageOwner.replies;
        if (tLRPC$TL_messageReplies != null) {
            return tLRPC$TL_messageReplies.replies;
        }
        return 0;
    }

    public boolean canEditMessage(TLRPC$Chat tLRPC$Chat) {
        return canEditMessage(this.currentAccount, this.messageOwner, tLRPC$Chat, this.scheduled);
    }

    public boolean canEditMessageScheduleTime(TLRPC$Chat tLRPC$Chat) {
        return canEditMessageScheduleTime(this.currentAccount, this.messageOwner, tLRPC$Chat);
    }

    public boolean canForwardMessage() {
        return !(this.messageOwner instanceof TLRPC$TL_message_secret) && !needDrawBluredPreview() && !isLiveLocation() && this.type != 16;
    }

    public boolean canEditMedia() {
        if (isSecretMedia()) {
            return false;
        }
        TLRPC$MessageMedia tLRPC$MessageMedia = this.messageOwner.media;
        if (tLRPC$MessageMedia instanceof TLRPC$TL_messageMediaPhoto) {
            return true;
        }
        if (!(tLRPC$MessageMedia instanceof TLRPC$TL_messageMediaDocument) || isVoice() || isSticker() || isAnimatedSticker() || isRoundVideo()) {
            return false;
        }
        return true;
    }

    public boolean canEditMessageAnytime(TLRPC$Chat tLRPC$Chat) {
        return canEditMessageAnytime(this.currentAccount, this.messageOwner, tLRPC$Chat);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:55:0x00a0, code lost:
        r4 = r6.admin_rights;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean canEditMessageAnytime(int r4, org.telegram.tgnet.TLRPC$Message r5, org.telegram.tgnet.TLRPC$Chat r6) {
        /*
            r0 = 0
            if (r5 == 0) goto L_0x00a9
            org.telegram.tgnet.TLRPC$Peer r1 = r5.peer_id
            if (r1 == 0) goto L_0x00a9
            org.telegram.tgnet.TLRPC$MessageMedia r1 = r5.media
            r2 = 1
            if (r1 == 0) goto L_0x0028
            org.telegram.tgnet.TLRPC$Document r1 = r1.document
            boolean r1 = isRoundVideoDocument(r1)
            if (r1 != 0) goto L_0x00a9
            org.telegram.tgnet.TLRPC$MessageMedia r1 = r5.media
            org.telegram.tgnet.TLRPC$Document r1 = r1.document
            boolean r1 = isStickerDocument(r1)
            if (r1 != 0) goto L_0x00a9
            org.telegram.tgnet.TLRPC$MessageMedia r1 = r5.media
            org.telegram.tgnet.TLRPC$Document r1 = r1.document
            boolean r1 = isAnimatedStickerDocument(r1, r2)
            if (r1 != 0) goto L_0x00a9
        L_0x0028:
            org.telegram.tgnet.TLRPC$MessageAction r1 = r5.action
            if (r1 == 0) goto L_0x0030
            boolean r1 = r1 instanceof org.telegram.tgnet.TLRPC$TL_messageActionEmpty
            if (r1 == 0) goto L_0x00a9
        L_0x0030:
            boolean r1 = isForwardedMessage(r5)
            if (r1 != 0) goto L_0x00a9
            int r1 = r5.via_bot_id
            if (r1 != 0) goto L_0x00a9
            int r1 = r5.id
            if (r1 >= 0) goto L_0x003f
            goto L_0x00a9
        L_0x003f:
            org.telegram.tgnet.TLRPC$Peer r1 = r5.from_id
            boolean r3 = r1 instanceof org.telegram.tgnet.TLRPC$TL_peerUser
            if (r3 == 0) goto L_0x005e
            int r1 = r1.user_id
            org.telegram.tgnet.TLRPC$Peer r3 = r5.peer_id
            int r3 = r3.user_id
            if (r1 != r3) goto L_0x005e
            org.telegram.messenger.UserConfig r4 = org.telegram.messenger.UserConfig.getInstance(r4)
            int r4 = r4.getClientUserId()
            if (r1 != r4) goto L_0x005e
            boolean r4 = isLiveLocationMessage(r5)
            if (r4 != 0) goto L_0x005e
            return r2
        L_0x005e:
            if (r6 != 0) goto L_0x007b
            org.telegram.tgnet.TLRPC$Peer r4 = r5.peer_id
            int r4 = r4.channel_id
            if (r4 == 0) goto L_0x007b
            int r4 = org.telegram.messenger.UserConfig.selectedAccount
            org.telegram.messenger.MessagesController r4 = org.telegram.messenger.MessagesController.getInstance(r4)
            org.telegram.tgnet.TLRPC$Peer r6 = r5.peer_id
            int r6 = r6.channel_id
            java.lang.Integer r6 = java.lang.Integer.valueOf(r6)
            org.telegram.tgnet.TLRPC$Chat r6 = r4.getChat(r6)
            if (r6 != 0) goto L_0x007b
            return r0
        L_0x007b:
            boolean r4 = org.telegram.messenger.ChatObject.isChannel(r6)
            if (r4 == 0) goto L_0x0092
            boolean r4 = r6.megagroup
            if (r4 != 0) goto L_0x0092
            boolean r4 = r6.creator
            if (r4 != 0) goto L_0x0091
            org.telegram.tgnet.TLRPC$TL_chatAdminRights r4 = r6.admin_rights
            if (r4 == 0) goto L_0x0092
            boolean r4 = r4.edit_messages
            if (r4 == 0) goto L_0x0092
        L_0x0091:
            return r2
        L_0x0092:
            boolean r4 = r5.out
            if (r4 == 0) goto L_0x00a9
            if (r6 == 0) goto L_0x00a9
            boolean r4 = r6.megagroup
            if (r4 == 0) goto L_0x00a9
            boolean r4 = r6.creator
            if (r4 != 0) goto L_0x00a8
            org.telegram.tgnet.TLRPC$TL_chatAdminRights r4 = r6.admin_rights
            if (r4 == 0) goto L_0x00a9
            boolean r4 = r4.pin_messages
            if (r4 == 0) goto L_0x00a9
        L_0x00a8:
            return r2
        L_0x00a9:
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.messenger.MessageObject.canEditMessageAnytime(int, org.telegram.tgnet.TLRPC$Message, org.telegram.tgnet.TLRPC$Chat):boolean");
    }

    public static boolean canEditMessageScheduleTime(int i, TLRPC$Message tLRPC$Message, TLRPC$Chat tLRPC$Chat) {
        if (tLRPC$Chat == null && tLRPC$Message.peer_id.channel_id != 0 && (tLRPC$Chat = MessagesController.getInstance(i).getChat(Integer.valueOf(tLRPC$Message.peer_id.channel_id))) == null) {
            return false;
        }
        if (!ChatObject.isChannel(tLRPC$Chat) || tLRPC$Chat.megagroup || tLRPC$Chat.creator) {
            return true;
        }
        TLRPC$TL_chatAdminRights tLRPC$TL_chatAdminRights = tLRPC$Chat.admin_rights;
        if (tLRPC$TL_chatAdminRights == null || (!tLRPC$TL_chatAdminRights.edit_messages && !tLRPC$Message.out)) {
            return false;
        }
        return true;
    }

    public static boolean canEditMessage(int i, TLRPC$Message tLRPC$Message, TLRPC$Chat tLRPC$Chat, boolean z) {
        TLRPC$MessageMedia tLRPC$MessageMedia;
        TLRPC$MessageAction tLRPC$MessageAction;
        TLRPC$TL_chatAdminRights tLRPC$TL_chatAdminRights;
        TLRPC$TL_chatAdminRights tLRPC$TL_chatAdminRights2;
        TLRPC$TL_chatAdminRights tLRPC$TL_chatAdminRights3;
        int i2;
        if (z && tLRPC$Message.date < ConnectionsManager.getInstance(i).getCurrentTime() - 60) {
            return false;
        }
        if ((tLRPC$Chat == null || ((!tLRPC$Chat.left && !tLRPC$Chat.kicked) || (tLRPC$Chat.megagroup && tLRPC$Chat.has_link))) && tLRPC$Message != null && tLRPC$Message.peer_id != null && (((tLRPC$MessageMedia = tLRPC$Message.media) == null || (!isRoundVideoDocument(tLRPC$MessageMedia.document) && !isStickerDocument(tLRPC$Message.media.document) && !isAnimatedStickerDocument(tLRPC$Message.media.document, true) && !isLocationMessage(tLRPC$Message))) && (((tLRPC$MessageAction = tLRPC$Message.action) == null || (tLRPC$MessageAction instanceof TLRPC$TL_messageActionEmpty)) && !isForwardedMessage(tLRPC$Message) && tLRPC$Message.via_bot_id == 0 && tLRPC$Message.id >= 0))) {
            TLRPC$Peer tLRPC$Peer = tLRPC$Message.from_id;
            if ((tLRPC$Peer instanceof TLRPC$TL_peerUser) && (i2 = tLRPC$Peer.user_id) == tLRPC$Message.peer_id.user_id && i2 == UserConfig.getInstance(i).getClientUserId() && !isLiveLocationMessage(tLRPC$Message) && !(tLRPC$Message.media instanceof TLRPC$TL_messageMediaContact)) {
                return true;
            }
            if (tLRPC$Chat == null && tLRPC$Message.peer_id.channel_id != 0 && (tLRPC$Chat = MessagesController.getInstance(i).getChat(Integer.valueOf(tLRPC$Message.peer_id.channel_id))) == null) {
                return false;
            }
            TLRPC$MessageMedia tLRPC$MessageMedia2 = tLRPC$Message.media;
            if (tLRPC$MessageMedia2 != null && !(tLRPC$MessageMedia2 instanceof TLRPC$TL_messageMediaEmpty) && !(tLRPC$MessageMedia2 instanceof TLRPC$TL_messageMediaPhoto) && !(tLRPC$MessageMedia2 instanceof TLRPC$TL_messageMediaDocument) && !(tLRPC$MessageMedia2 instanceof TLRPC$TL_messageMediaWebPage)) {
                return false;
            }
            if (ChatObject.isChannel(tLRPC$Chat) && !tLRPC$Chat.megagroup && (tLRPC$Chat.creator || ((tLRPC$TL_chatAdminRights3 = tLRPC$Chat.admin_rights) != null && tLRPC$TL_chatAdminRights3.edit_messages))) {
                return true;
            }
            if (tLRPC$Message.out && tLRPC$Chat != null && tLRPC$Chat.megagroup && (tLRPC$Chat.creator || ((tLRPC$TL_chatAdminRights2 = tLRPC$Chat.admin_rights) != null && tLRPC$TL_chatAdminRights2.pin_messages))) {
                return true;
            }
            if (!z && Math.abs(tLRPC$Message.date - ConnectionsManager.getInstance(i).getCurrentTime()) > MessagesController.getInstance(i).maxEditTime) {
                return false;
            }
            if (tLRPC$Message.peer_id.channel_id == 0) {
                if (!tLRPC$Message.out) {
                    TLRPC$Peer tLRPC$Peer2 = tLRPC$Message.from_id;
                    if (!(tLRPC$Peer2 instanceof TLRPC$TL_peerUser) || tLRPC$Peer2.user_id != UserConfig.getInstance(i).getClientUserId()) {
                        return false;
                    }
                }
                TLRPC$MessageMedia tLRPC$MessageMedia3 = tLRPC$Message.media;
                if (!(tLRPC$MessageMedia3 instanceof TLRPC$TL_messageMediaPhoto) && (!(tLRPC$MessageMedia3 instanceof TLRPC$TL_messageMediaDocument) || isStickerMessage(tLRPC$Message) || isAnimatedStickerMessage(tLRPC$Message))) {
                    TLRPC$MessageMedia tLRPC$MessageMedia4 = tLRPC$Message.media;
                    if ((tLRPC$MessageMedia4 instanceof TLRPC$TL_messageMediaEmpty) || (tLRPC$MessageMedia4 instanceof TLRPC$TL_messageMediaWebPage) || tLRPC$MessageMedia4 == null) {
                        return true;
                    }
                    return false;
                }
                return true;
            } else if ((tLRPC$Chat != null && tLRPC$Chat.megagroup && tLRPC$Message.out) || (tLRPC$Chat != null && !tLRPC$Chat.megagroup && ((tLRPC$Chat.creator || ((tLRPC$TL_chatAdminRights = tLRPC$Chat.admin_rights) != null && (tLRPC$TL_chatAdminRights.edit_messages || (tLRPC$Message.out && tLRPC$TL_chatAdminRights.post_messages)))) && tLRPC$Message.post))) {
                TLRPC$MessageMedia tLRPC$MessageMedia5 = tLRPC$Message.media;
                if (!(tLRPC$MessageMedia5 instanceof TLRPC$TL_messageMediaPhoto) && (!(tLRPC$MessageMedia5 instanceof TLRPC$TL_messageMediaDocument) || isStickerMessage(tLRPC$Message) || isAnimatedStickerMessage(tLRPC$Message))) {
                    TLRPC$MessageMedia tLRPC$MessageMedia6 = tLRPC$Message.media;
                    if ((tLRPC$MessageMedia6 instanceof TLRPC$TL_messageMediaEmpty) || (tLRPC$MessageMedia6 instanceof TLRPC$TL_messageMediaWebPage) || tLRPC$MessageMedia6 == null) {
                        return true;
                    }
                }
                return true;
            }
        }
        return false;
    }

    public boolean canDeleteMessage(boolean z, TLRPC$Chat tLRPC$Chat) {
        return this.eventId == 0 && canDeleteMessage(this.currentAccount, z, this.messageOwner, tLRPC$Chat);
    }

    public static boolean canDeleteMessage(int i, boolean z, TLRPC$Message tLRPC$Message, TLRPC$Chat tLRPC$Chat) {
        TLRPC$TL_chatAdminRights tLRPC$TL_chatAdminRights;
        if (tLRPC$Message == null) {
            return false;
        }
        if (tLRPC$Message.id < 0) {
            return true;
        }
        if (tLRPC$Chat == null && tLRPC$Message.peer_id.channel_id != 0) {
            tLRPC$Chat = MessagesController.getInstance(i).getChat(Integer.valueOf(tLRPC$Message.peer_id.channel_id));
        }
        if (ChatObject.isChannel(tLRPC$Chat)) {
            if (!z || tLRPC$Chat.megagroup) {
                boolean z2 = tLRPC$Message.out;
                if (!z2 || !(tLRPC$Message instanceof TLRPC$TL_messageService)) {
                    if (!z) {
                        if (tLRPC$Message.id == 1) {
                            return false;
                        }
                        if (tLRPC$Chat.creator || (((tLRPC$TL_chatAdminRights = tLRPC$Chat.admin_rights) != null && (tLRPC$TL_chatAdminRights.delete_messages || (z2 && (tLRPC$Chat.megagroup || tLRPC$TL_chatAdminRights.post_messages)))) || (tLRPC$Chat.megagroup && z2 && (tLRPC$Message.from_id instanceof TLRPC$TL_peerUser)))) {
                            return true;
                        }
                        return false;
                    }
                    return true;
                } else if (tLRPC$Message.id == 1 || !ChatObject.canUserDoAdminAction(tLRPC$Chat, 13)) {
                    return false;
                } else {
                    return true;
                }
            } else {
                if (!tLRPC$Chat.creator) {
                    TLRPC$TL_chatAdminRights tLRPC$TL_chatAdminRights2 = tLRPC$Chat.admin_rights;
                    if (tLRPC$TL_chatAdminRights2 == null) {
                        return false;
                    }
                    if (tLRPC$TL_chatAdminRights2.delete_messages || tLRPC$Message.out) {
                        return true;
                    }
                    return false;
                }
                return true;
            }
        } else if (z || isOut(tLRPC$Message) || !ChatObject.isChannel(tLRPC$Chat)) {
            return true;
        } else {
            return false;
        }
    }

    public String getForwardedName() {
        TLRPC$MessageFwdHeader tLRPC$MessageFwdHeader = this.messageOwner.fwd_from;
        if (tLRPC$MessageFwdHeader == null) {
            return null;
        }
        TLRPC$Peer tLRPC$Peer = tLRPC$MessageFwdHeader.from_id;
        if (tLRPC$Peer instanceof TLRPC$TL_peerChannel) {
            TLRPC$Chat chat = MessagesController.getInstance(this.currentAccount).getChat(Integer.valueOf(this.messageOwner.fwd_from.from_id.channel_id));
            if (chat != null) {
                return chat.title;
            }
            return null;
        } else if (tLRPC$Peer instanceof TLRPC$TL_peerChat) {
            TLRPC$Chat chat2 = MessagesController.getInstance(this.currentAccount).getChat(Integer.valueOf(this.messageOwner.fwd_from.from_id.chat_id));
            if (chat2 != null) {
                return chat2.title;
            }
            return null;
        } else if (tLRPC$Peer instanceof TLRPC$TL_peerUser) {
            TLRPC$User user = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(this.messageOwner.fwd_from.from_id.user_id));
            if (user != null) {
                return UserObject.getUserName(user);
            }
            return null;
        } else {
            String str = tLRPC$MessageFwdHeader.from_name;
            if (str != null) {
                return str;
            }
            return null;
        }
    }

    public int getReplyMsgId() {
        TLRPC$TL_messageReplyHeader tLRPC$TL_messageReplyHeader = this.messageOwner.reply_to;
        if (tLRPC$TL_messageReplyHeader != null) {
            return tLRPC$TL_messageReplyHeader.reply_to_msg_id;
        }
        return 0;
    }

    public int getReplyTopMsgId() {
        TLRPC$TL_messageReplyHeader tLRPC$TL_messageReplyHeader = this.messageOwner.reply_to;
        if (tLRPC$TL_messageReplyHeader != null) {
            return tLRPC$TL_messageReplyHeader.reply_to_top_id;
        }
        return 0;
    }

    public int getReplyAnyMsgId() {
        TLRPC$TL_messageReplyHeader tLRPC$TL_messageReplyHeader = this.messageOwner.reply_to;
        if (tLRPC$TL_messageReplyHeader == null) {
            return 0;
        }
        int i = tLRPC$TL_messageReplyHeader.reply_to_top_id;
        if (i != 0) {
            return i;
        }
        return tLRPC$TL_messageReplyHeader.reply_to_msg_id;
    }

    public boolean isPrivateForward() {
        TLRPC$MessageFwdHeader tLRPC$MessageFwdHeader = this.messageOwner.fwd_from;
        return tLRPC$MessageFwdHeader != null && !TextUtils.isEmpty(tLRPC$MessageFwdHeader.from_name);
    }

    public boolean isImportedForward() {
        TLRPC$MessageFwdHeader tLRPC$MessageFwdHeader = this.messageOwner.fwd_from;
        return tLRPC$MessageFwdHeader != null && tLRPC$MessageFwdHeader.imported;
    }

    public int getSenderId() {
        TLRPC$Peer tLRPC$Peer;
        TLRPC$Message tLRPC$Message = this.messageOwner;
        TLRPC$MessageFwdHeader tLRPC$MessageFwdHeader = tLRPC$Message.fwd_from;
        if (tLRPC$MessageFwdHeader == null || (tLRPC$Peer = tLRPC$MessageFwdHeader.saved_from_peer) == null) {
            TLRPC$Peer tLRPC$Peer2 = tLRPC$Message.from_id;
            if (tLRPC$Peer2 instanceof TLRPC$TL_peerUser) {
                return tLRPC$Peer2.user_id;
            }
            if (tLRPC$Peer2 instanceof TLRPC$TL_peerChannel) {
                return -tLRPC$Peer2.channel_id;
            }
            if (tLRPC$Peer2 instanceof TLRPC$TL_peerChat) {
                return -tLRPC$Peer2.chat_id;
            }
            if (tLRPC$Message.post) {
                return tLRPC$Message.peer_id.channel_id;
            }
            return 0;
        }
        int i = tLRPC$Peer.user_id;
        if (i != 0) {
            TLRPC$Peer tLRPC$Peer3 = tLRPC$MessageFwdHeader.from_id;
            return tLRPC$Peer3 instanceof TLRPC$TL_peerUser ? tLRPC$Peer3.user_id : i;
        } else if (tLRPC$Peer.channel_id != 0) {
            if (isSavedFromMegagroup()) {
                TLRPC$Peer tLRPC$Peer4 = this.messageOwner.fwd_from.from_id;
                if (tLRPC$Peer4 instanceof TLRPC$TL_peerUser) {
                    return tLRPC$Peer4.user_id;
                }
            }
            TLRPC$MessageFwdHeader tLRPC$MessageFwdHeader2 = this.messageOwner.fwd_from;
            TLRPC$Peer tLRPC$Peer5 = tLRPC$MessageFwdHeader2.from_id;
            if (tLRPC$Peer5 instanceof TLRPC$TL_peerChannel) {
                return -tLRPC$Peer5.channel_id;
            }
            if (tLRPC$Peer5 instanceof TLRPC$TL_peerChat) {
                return -tLRPC$Peer5.chat_id;
            }
            return -tLRPC$MessageFwdHeader2.saved_from_peer.channel_id;
        } else {
            int i2 = tLRPC$Peer.chat_id;
            if (i2 == 0) {
                return 0;
            }
            TLRPC$Peer tLRPC$Peer6 = tLRPC$MessageFwdHeader.from_id;
            if (tLRPC$Peer6 instanceof TLRPC$TL_peerUser) {
                return tLRPC$Peer6.user_id;
            }
            if (tLRPC$Peer6 instanceof TLRPC$TL_peerChannel) {
                return -tLRPC$Peer6.channel_id;
            }
            return tLRPC$Peer6 instanceof TLRPC$TL_peerChat ? -tLRPC$Peer6.chat_id : -i2;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:2:0x0008, code lost:
        r0 = r0.webpage;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean isWallpaper() {
        /*
            r2 = this;
            org.telegram.tgnet.TLRPC$Message r0 = r2.messageOwner
            org.telegram.tgnet.TLRPC$MessageMedia r0 = r0.media
            boolean r1 = r0 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaWebPage
            if (r1 == 0) goto L_0x0018
            org.telegram.tgnet.TLRPC$WebPage r0 = r0.webpage
            if (r0 == 0) goto L_0x0018
            java.lang.String r0 = r0.type
            java.lang.String r1 = "telegram_background"
            boolean r0 = r1.equals(r0)
            if (r0 == 0) goto L_0x0018
            r0 = 1
            goto L_0x0019
        L_0x0018:
            r0 = 0
        L_0x0019:
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.messenger.MessageObject.isWallpaper():boolean");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:2:0x0008, code lost:
        r0 = r0.webpage;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean isTheme() {
        /*
            r2 = this;
            org.telegram.tgnet.TLRPC$Message r0 = r2.messageOwner
            org.telegram.tgnet.TLRPC$MessageMedia r0 = r0.media
            boolean r1 = r0 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaWebPage
            if (r1 == 0) goto L_0x0018
            org.telegram.tgnet.TLRPC$WebPage r0 = r0.webpage
            if (r0 == 0) goto L_0x0018
            java.lang.String r0 = r0.type
            java.lang.String r1 = "telegram_theme"
            boolean r0 = r1.equals(r0)
            if (r0 == 0) goto L_0x0018
            r0 = 1
            goto L_0x0019
        L_0x0018:
            r0 = 0
        L_0x0019:
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.messenger.MessageObject.isTheme():boolean");
    }

    public int getMediaExistanceFlags() {
        int i = this.attachPathExists ? 1 : 0;
        return this.mediaExists ? i | 2 : i;
    }

    public void applyMediaExistanceFlags(int i) {
        if (i == -1) {
            checkMediaExistance();
            return;
        }
        boolean z = false;
        this.attachPathExists = (i & 1) != 0;
        if ((i & 2) != 0) {
            z = true;
        }
        this.mediaExists = z;
    }

    public void checkMediaExistance() {
        TLRPC$Photo tLRPC$Photo;
        int i;
        this.attachPathExists = false;
        this.mediaExists = false;
        if (this.type == 1 && FileLoader.getClosestPhotoSizeWithSize(this.photoThumbs, AndroidUtilities.getPhotoSize()) != null) {
            File pathToMessage = FileLoader.getPathToMessage(this.messageOwner);
            if (needDrawBluredPreview()) {
                this.mediaExists = new File(pathToMessage.getAbsolutePath() + ".enc").exists();
            }
            if (!this.mediaExists) {
                this.mediaExists = pathToMessage.exists();
            }
        }
        if ((!this.mediaExists && this.type == 8) || (i = this.type) == 3 || i == 9 || i == 2 || i == 14 || i == 5) {
            String str = this.messageOwner.attachPath;
            if (str != null && str.length() > 0) {
                this.attachPathExists = new File(this.messageOwner.attachPath).exists();
            }
            if (!this.attachPathExists) {
                File pathToMessage2 = FileLoader.getPathToMessage(this.messageOwner);
                if (this.type == 3 && needDrawBluredPreview()) {
                    this.mediaExists = new File(pathToMessage2.getAbsolutePath() + ".enc").exists();
                }
                if (!this.mediaExists) {
                    this.mediaExists = pathToMessage2.exists();
                }
            }
        }
        if (!this.mediaExists) {
            TLRPC$Document document = getDocument();
            if (document == null) {
                int i2 = this.type;
                if (i2 == 0) {
                    TLRPC$PhotoSize closestPhotoSizeWithSize = FileLoader.getClosestPhotoSizeWithSize(this.photoThumbs, AndroidUtilities.getPhotoSize());
                    if (closestPhotoSizeWithSize != null) {
                        this.mediaExists = FileLoader.getPathToAttach(closestPhotoSizeWithSize, true).exists();
                    }
                } else if (i2 == 11 && (tLRPC$Photo = this.messageOwner.action.photo) != null && !tLRPC$Photo.video_sizes.isEmpty()) {
                    this.mediaExists = FileLoader.getPathToAttach(tLRPC$Photo.video_sizes.get(0), true).exists();
                }
            } else if (isWallpaper()) {
                this.mediaExists = FileLoader.getPathToAttach(document, true).exists();
            } else {
                this.mediaExists = FileLoader.getPathToAttach(document).exists();
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:52:0x00f8, code lost:
        r6 = (java.lang.String) r3.get(r5);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setQuery(java.lang.String r14) {
        /*
            r13 = this;
            boolean r0 = android.text.TextUtils.isEmpty(r14)
            if (r0 == 0) goto L_0x0007
            return
        L_0x0007:
            java.util.ArrayList r0 = new java.util.ArrayList
            r0.<init>()
            java.lang.String r14 = r14.trim()
            java.lang.String r14 = r14.toLowerCase()
            java.lang.String r1 = "\\P{L}+"
            java.lang.String[] r2 = r14.split(r1)
            java.util.ArrayList r3 = new java.util.ArrayList
            r3.<init>()
            org.telegram.tgnet.TLRPC$Message r4 = r13.messageOwner
            java.lang.String r4 = r4.message
            boolean r4 = android.text.TextUtils.isEmpty(r4)
            if (r4 != 0) goto L_0x0053
            org.telegram.tgnet.TLRPC$Message r4 = r13.messageOwner
            java.lang.String r4 = r4.message
            java.lang.String r4 = r4.trim()
            java.lang.String r4 = r4.toLowerCase()
            boolean r5 = r4.contains(r14)
            if (r5 == 0) goto L_0x0048
            boolean r5 = r0.contains(r14)
            if (r5 != 0) goto L_0x0048
            r0.add(r14)
            r13.handleFoundWords(r0, r2)
            return
        L_0x0048:
            java.lang.String[] r4 = r4.split(r1)
            java.util.List r4 = java.util.Arrays.asList(r4)
            r3.addAll(r4)
        L_0x0053:
            org.telegram.tgnet.TLRPC$Document r4 = r13.getDocument()
            if (r4 == 0) goto L_0x007f
            org.telegram.tgnet.TLRPC$Document r4 = r13.getDocument()
            java.lang.String r4 = org.telegram.messenger.FileLoader.getDocumentFileName(r4)
            java.lang.String r4 = r4.toLowerCase()
            boolean r5 = r4.contains(r14)
            if (r5 == 0) goto L_0x0074
            boolean r5 = r0.contains(r14)
            if (r5 != 0) goto L_0x0074
            r0.add(r14)
        L_0x0074:
            java.lang.String[] r4 = r4.split(r1)
            java.util.List r4 = java.util.Arrays.asList(r4)
            r3.addAll(r4)
        L_0x007f:
            org.telegram.tgnet.TLRPC$Message r4 = r13.messageOwner
            org.telegram.tgnet.TLRPC$MessageMedia r4 = r4.media
            boolean r5 = r4 instanceof org.telegram.tgnet.TLRPC$TL_messageMediaWebPage
            if (r5 == 0) goto L_0x00b3
            org.telegram.tgnet.TLRPC$WebPage r4 = r4.webpage
            boolean r5 = r4 instanceof org.telegram.tgnet.TLRPC$TL_webPage
            if (r5 == 0) goto L_0x00b3
            java.lang.String r5 = r4.title
            if (r5 != 0) goto L_0x0093
            java.lang.String r5 = r4.site_name
        L_0x0093:
            if (r5 == 0) goto L_0x00b3
            java.lang.String r4 = r5.toLowerCase()
            boolean r5 = r4.contains(r14)
            if (r5 == 0) goto L_0x00a8
            boolean r5 = r0.contains(r14)
            if (r5 != 0) goto L_0x00a8
            r0.add(r14)
        L_0x00a8:
            java.lang.String[] r4 = r4.split(r1)
            java.util.List r4 = java.util.Arrays.asList(r4)
            r3.addAll(r4)
        L_0x00b3:
            java.lang.String r4 = r13.getMusicAuthor()
            if (r4 == 0) goto L_0x00d7
            java.lang.String r4 = r4.toLowerCase()
            boolean r5 = r4.contains(r14)
            if (r5 == 0) goto L_0x00cc
            boolean r5 = r0.contains(r14)
            if (r5 != 0) goto L_0x00cc
            r0.add(r14)
        L_0x00cc:
            java.lang.String[] r14 = r4.split(r1)
            java.util.List r14 = java.util.Arrays.asList(r14)
            r3.addAll(r14)
        L_0x00d7:
            r14 = 0
            r1 = 0
        L_0x00d9:
            int r4 = r2.length
            if (r1 >= r4) goto L_0x0151
            r4 = r2[r1]
            int r5 = r4.length()
            r6 = 2
            if (r5 >= r6) goto L_0x00e6
            goto L_0x014e
        L_0x00e6:
            r5 = 0
        L_0x00e7:
            int r6 = r3.size()
            if (r5 >= r6) goto L_0x014e
            java.lang.Object r6 = r3.get(r5)
            boolean r6 = r0.contains(r6)
            if (r6 == 0) goto L_0x00f8
            goto L_0x014b
        L_0x00f8:
            java.lang.Object r6 = r3.get(r5)
            java.lang.String r6 = (java.lang.String) r6
            char r7 = r4.charAt(r14)
            int r7 = r6.indexOf(r7)
            if (r7 >= 0) goto L_0x0109
            goto L_0x014b
        L_0x0109:
            int r8 = r4.length()
            int r9 = r6.length()
            int r8 = java.lang.Math.max(r8, r9)
            if (r7 == 0) goto L_0x011b
            java.lang.String r6 = r6.substring(r7)
        L_0x011b:
            int r7 = r4.length()
            int r9 = r6.length()
            int r7 = java.lang.Math.min(r7, r9)
            r9 = 0
            r10 = 0
        L_0x0129:
            if (r9 >= r7) goto L_0x013a
            char r11 = r6.charAt(r9)
            char r12 = r4.charAt(r9)
            if (r11 != r12) goto L_0x013a
            int r10 = r10 + 1
            int r9 = r9 + 1
            goto L_0x0129
        L_0x013a:
            float r6 = (float) r10
            float r7 = (float) r8
            float r6 = r6 / r7
            double r6 = (double) r6
            r8 = 4602678819172646912(0x3feNUM, double:0.5)
            int r10 = (r6 > r8 ? 1 : (r6 == r8 ? 0 : -1))
            if (r10 < 0) goto L_0x014b
            java.lang.Object r6 = r3.get(r5)
            r0.add(r6)
        L_0x014b:
            int r5 = r5 + 1
            goto L_0x00e7
        L_0x014e:
            int r1 = r1 + 1
            goto L_0x00d9
        L_0x0151:
            r13.handleFoundWords(r0, r2)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.messenger.MessageObject.setQuery(java.lang.String):void");
    }

    private void handleFoundWords(ArrayList<String> arrayList, String[] strArr) {
        boolean z;
        if (!arrayList.isEmpty()) {
            boolean z2 = false;
            for (int i = 0; i < arrayList.size(); i++) {
                int i2 = 0;
                while (true) {
                    if (i2 >= strArr.length) {
                        break;
                    } else if (arrayList.get(i).contains(strArr[i2])) {
                        z2 = true;
                        break;
                    } else {
                        i2++;
                    }
                }
                if (z2) {
                    break;
                }
            }
            if (z2) {
                int i3 = 0;
                while (i3 < arrayList.size()) {
                    int i4 = 0;
                    while (true) {
                        if (i4 >= strArr.length) {
                            z = false;
                            break;
                        } else if (arrayList.get(i3).contains(strArr[i4])) {
                            z = true;
                            break;
                        } else {
                            i4++;
                        }
                    }
                    if (!z) {
                        arrayList.remove(i3);
                        i3--;
                    }
                    i3++;
                }
                if (arrayList.size() > 0) {
                    Collections.sort(arrayList, $$Lambda$MessageObject$FIPTetn54BHnKmJ0ffASkxxTiMQ.INSTANCE);
                    arrayList.clear();
                    arrayList.add(arrayList.get(0));
                }
            }
            this.highlightedWords = arrayList;
            String str = this.messageOwner.message;
            if (str != null) {
                String trim = str.replace(10, ' ').replaceAll(" +", " ").trim();
                int length = trim.length();
                int indexOf = trim.toLowerCase().indexOf(arrayList.get(0));
                if (indexOf < 0) {
                    indexOf = 0;
                }
                if (length > 200) {
                    int max = Math.max(0, indexOf - 100);
                    trim = trim.substring(max, Math.min(length, (indexOf - max) + indexOf + 100));
                }
                this.messageTrimmedToHighlight = trim;
            }
        }
    }

    static /* synthetic */ int lambda$handleFoundWords$1(String str, String str2) {
        return str2.length() - str.length();
    }

    public boolean hasHighlightedWords() {
        ArrayList<String> arrayList = this.highlightedWords;
        return arrayList != null && !arrayList.isEmpty();
    }

    public boolean equals(MessageObject messageObject) {
        return getId() == messageObject.getId() && getDialogId() == messageObject.getDialogId();
    }
}
