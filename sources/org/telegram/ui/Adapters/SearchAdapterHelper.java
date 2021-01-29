package org.telegram.ui.Adapters;

import android.util.SparseArray;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.telegram.PhoneFormat.PhoneFormat;
import org.telegram.SQLite.SQLiteCursor;
import org.telegram.SQLite.SQLitePreparedStatement;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC$ChannelParticipant;
import org.telegram.tgnet.TLRPC$Chat;
import org.telegram.tgnet.TLRPC$ChatParticipant;
import org.telegram.tgnet.TLRPC$Peer;
import org.telegram.tgnet.TLRPC$TL_channelParticipantsAdmins;
import org.telegram.tgnet.TLRPC$TL_channelParticipantsBanned;
import org.telegram.tgnet.TLRPC$TL_channelParticipantsKicked;
import org.telegram.tgnet.TLRPC$TL_channelParticipantsSearch;
import org.telegram.tgnet.TLRPC$TL_channels_channelParticipants;
import org.telegram.tgnet.TLRPC$TL_channels_getParticipants;
import org.telegram.tgnet.TLRPC$TL_contact;
import org.telegram.tgnet.TLRPC$TL_contacts_found;
import org.telegram.tgnet.TLRPC$TL_contacts_search;
import org.telegram.tgnet.TLRPC$TL_error;
import org.telegram.tgnet.TLRPC$TL_groupCallParticipant;
import org.telegram.tgnet.TLRPC$User;

public class SearchAdapterHelper {
    private boolean allResultsAreGlobal;
    private boolean allowGlobalResults = true;
    private int channelLastReqId;
    private int channelReqId = 0;
    private int currentAccount = UserConfig.selectedAccount;
    private SearchAdapterHelperDelegate delegate;
    private ArrayList<TLObject> globalSearch = new ArrayList<>();
    private SparseArray<TLObject> globalSearchMap = new SparseArray<>();
    private ArrayList<TLObject> groupSearch = new ArrayList<>();
    private SparseArray<TLObject> groupSearchMap = new SparseArray<>();
    private ArrayList<HashtagObject> hashtags;
    private HashMap<String, HashtagObject> hashtagsByText;
    private boolean hashtagsLoadedFromDb = false;
    private String lastFoundChannel;
    private String lastFoundUsername = null;
    private int lastReqId;
    private ArrayList<TLObject> localSearchResults;
    private ArrayList<TLObject> localServerSearch = new ArrayList<>();
    private SparseArray<TLObject> phoneSearchMap = new SparseArray<>();
    private ArrayList<Object> phonesSearch = new ArrayList<>();
    private int reqId = 0;

    public static class HashtagObject {
        int date;
        String hashtag;
    }

    public interface SearchAdapterHelperDelegate {

        /* renamed from: org.telegram.ui.Adapters.SearchAdapterHelper$SearchAdapterHelperDelegate$-CC  reason: invalid class name */
        public final /* synthetic */ class CC {
            public static boolean $default$canApplySearchResults(SearchAdapterHelperDelegate searchAdapterHelperDelegate, int i) {
                return true;
            }

            public static SparseArray $default$getExcludeCallParticipants(SearchAdapterHelperDelegate searchAdapterHelperDelegate) {
                return null;
            }

            public static SparseArray $default$getExcludeUsers(SearchAdapterHelperDelegate searchAdapterHelperDelegate) {
                return null;
            }

            public static void $default$onSetHashtags(SearchAdapterHelperDelegate searchAdapterHelperDelegate, ArrayList arrayList, HashMap hashMap) {
            }
        }

        boolean canApplySearchResults(int i);

        SparseArray<TLRPC$TL_groupCallParticipant> getExcludeCallParticipants();

        SparseArray<TLRPC$User> getExcludeUsers();

        void onDataSetChanged(int i);

        void onSetHashtags(ArrayList<HashtagObject> arrayList, HashMap<String, HashtagObject> hashMap);
    }

    public SearchAdapterHelper(boolean z) {
        this.allResultsAreGlobal = z;
    }

    public void setAllowGlobalResults(boolean z) {
        this.allowGlobalResults = z;
    }

    public boolean isSearchInProgress() {
        return (this.reqId == 0 && this.channelReqId == 0) ? false : true;
    }

    public void queryServerSearch(String str, boolean z, boolean z2, boolean z3, boolean z4, boolean z5, int i, boolean z6, int i2, int i3) {
        String str2;
        String str3 = str;
        int i4 = i;
        int i5 = i2;
        int i6 = i3;
        if (this.reqId != 0) {
            ConnectionsManager.getInstance(this.currentAccount).cancelRequest(this.reqId, true);
            this.reqId = 0;
        }
        if (this.channelReqId != 0) {
            ConnectionsManager.getInstance(this.currentAccount).cancelRequest(this.channelReqId, true);
            this.channelReqId = 0;
        }
        if (str3 == null) {
            this.groupSearch.clear();
            this.groupSearchMap.clear();
            this.globalSearch.clear();
            this.globalSearchMap.clear();
            this.localServerSearch.clear();
            this.phonesSearch.clear();
            this.phoneSearchMap.clear();
            this.lastReqId = 0;
            this.channelLastReqId = 0;
            this.delegate.onDataSetChanged(i6);
            return;
        }
        if (str.length() <= 0) {
            this.groupSearch.clear();
            this.groupSearchMap.clear();
            this.channelLastReqId = 0;
            this.delegate.onDataSetChanged(i6);
        } else if (i4 != 0) {
            TLRPC$TL_channels_getParticipants tLRPC$TL_channels_getParticipants = new TLRPC$TL_channels_getParticipants();
            if (i5 == 1) {
                tLRPC$TL_channels_getParticipants.filter = new TLRPC$TL_channelParticipantsAdmins();
            } else if (i5 == 3) {
                tLRPC$TL_channels_getParticipants.filter = new TLRPC$TL_channelParticipantsBanned();
            } else if (i5 == 0) {
                tLRPC$TL_channels_getParticipants.filter = new TLRPC$TL_channelParticipantsKicked();
            } else {
                tLRPC$TL_channels_getParticipants.filter = new TLRPC$TL_channelParticipantsSearch();
            }
            tLRPC$TL_channels_getParticipants.filter.q = str3;
            tLRPC$TL_channels_getParticipants.limit = 50;
            tLRPC$TL_channels_getParticipants.offset = 0;
            tLRPC$TL_channels_getParticipants.channel = MessagesController.getInstance(this.currentAccount).getInputChannel(i4);
            int i7 = this.channelLastReqId + 1;
            this.channelLastReqId = i7;
            ConnectionsManager instance = ConnectionsManager.getInstance(this.currentAccount);
            $$Lambda$SearchAdapterHelper$8RMFgd_MVod5GyKAdDzoUmKlig r15 = r0;
            $$Lambda$SearchAdapterHelper$8RMFgd_MVod5GyKAdDzoUmKlig r0 = new RequestDelegate(i7, str, z4, i3) {
                public final /* synthetic */ int f$1;
                public final /* synthetic */ String f$2;
                public final /* synthetic */ boolean f$3;
                public final /* synthetic */ int f$4;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                    this.f$3 = r4;
                    this.f$4 = r5;
                }

                public final void run(TLObject tLObject, TLRPC$TL_error tLRPC$TL_error) {
                    SearchAdapterHelper.this.lambda$queryServerSearch$1$SearchAdapterHelper(this.f$1, this.f$2, this.f$3, this.f$4, tLObject, tLRPC$TL_error);
                }
            };
            this.channelReqId = instance.sendRequest(tLRPC$TL_channels_getParticipants, r15, 2);
        } else {
            this.lastFoundChannel = str.toLowerCase();
        }
        if (z) {
            if (str.length() > 0) {
                TLRPC$TL_contacts_search tLRPC$TL_contacts_search = new TLRPC$TL_contacts_search();
                tLRPC$TL_contacts_search.q = str3;
                tLRPC$TL_contacts_search.limit = 50;
                int i8 = this.lastReqId + 1;
                this.lastReqId = i8;
                $$Lambda$SearchAdapterHelper$d_RT9an5_K2u2dcxdohpFvar_SWE r12 = r0;
                ConnectionsManager instance2 = ConnectionsManager.getInstance(this.currentAccount);
                $$Lambda$SearchAdapterHelper$d_RT9an5_K2u2dcxdohpFvar_SWE r02 = new RequestDelegate(i8, i3, z2, z5, z3, z4, str) {
                    public final /* synthetic */ int f$1;
                    public final /* synthetic */ int f$2;
                    public final /* synthetic */ boolean f$3;
                    public final /* synthetic */ boolean f$4;
                    public final /* synthetic */ boolean f$5;
                    public final /* synthetic */ boolean f$6;
                    public final /* synthetic */ String f$7;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                        this.f$3 = r4;
                        this.f$4 = r5;
                        this.f$5 = r6;
                        this.f$6 = r7;
                        this.f$7 = r8;
                    }

                    public final void run(TLObject tLObject, TLRPC$TL_error tLRPC$TL_error) {
                        SearchAdapterHelper.this.lambda$queryServerSearch$3$SearchAdapterHelper(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6, this.f$7, tLObject, tLRPC$TL_error);
                    }
                };
                this.reqId = instance2.sendRequest(tLRPC$TL_contacts_search, r12, 2);
            } else {
                this.globalSearch.clear();
                this.globalSearchMap.clear();
                this.localServerSearch.clear();
                this.lastReqId = 0;
                this.delegate.onDataSetChanged(i6);
                if (!z5 && z6 && str3.startsWith("+") && str.length() > 3) {
                    this.phonesSearch.clear();
                    this.phoneSearchMap.clear();
                    String stripExceptNumbers = PhoneFormat.stripExceptNumbers(str);
                    ArrayList<TLRPC$TL_contact> arrayList = ContactsController.getInstance(this.currentAccount).contacts;
                    int size = arrayList.size();
                    boolean z7 = false;
                    for (int i9 = 0; i9 < size; i9++) {
                        TLRPC$User user = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(arrayList.get(i9).user_id));
                        if (!(user == null || (str2 = user.phone) == null || !str2.startsWith(stripExceptNumbers))) {
                            if (!z7) {
                                z7 = user.phone.length() == stripExceptNumbers.length();
                            }
                            this.phonesSearch.add(user);
                            this.phoneSearchMap.put(user.id, user);
                        }
                    }
                    if (!z7) {
                        this.phonesSearch.add("section");
                        this.phonesSearch.add(stripExceptNumbers);
                    }
                    this.delegate.onDataSetChanged(i6);
                    return;
                }
                return;
            }
        }
        if (!z5) {
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$queryServerSearch$1 */
    public /* synthetic */ void lambda$queryServerSearch$1$SearchAdapterHelper(int i, String str, boolean z, int i2, TLObject tLObject, TLRPC$TL_error tLRPC$TL_error) {
        AndroidUtilities.runOnUIThread(new Runnable(i, tLRPC$TL_error, tLObject, str, z, i2) {
            public final /* synthetic */ int f$1;
            public final /* synthetic */ TLRPC$TL_error f$2;
            public final /* synthetic */ TLObject f$3;
            public final /* synthetic */ String f$4;
            public final /* synthetic */ boolean f$5;
            public final /* synthetic */ int f$6;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
                this.f$4 = r5;
                this.f$5 = r6;
                this.f$6 = r7;
            }

            public final void run() {
                SearchAdapterHelper.this.lambda$null$0$SearchAdapterHelper(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$null$0 */
    public /* synthetic */ void lambda$null$0$SearchAdapterHelper(int i, TLRPC$TL_error tLRPC$TL_error, TLObject tLObject, String str, boolean z, int i2) {
        if (i == this.channelLastReqId) {
            this.channelReqId = 0;
            if (tLRPC$TL_error == null) {
                TLRPC$TL_channels_channelParticipants tLRPC$TL_channels_channelParticipants = (TLRPC$TL_channels_channelParticipants) tLObject;
                this.lastFoundChannel = str.toLowerCase();
                MessagesController.getInstance(this.currentAccount).putUsers(tLRPC$TL_channels_channelParticipants.users, false);
                this.groupSearch.clear();
                this.groupSearchMap.clear();
                this.groupSearch.addAll(tLRPC$TL_channels_channelParticipants.participants);
                int clientUserId = UserConfig.getInstance(this.currentAccount).getClientUserId();
                int size = tLRPC$TL_channels_channelParticipants.participants.size();
                for (int i3 = 0; i3 < size; i3++) {
                    TLRPC$ChannelParticipant tLRPC$ChannelParticipant = tLRPC$TL_channels_channelParticipants.participants.get(i3);
                    if (z || tLRPC$ChannelParticipant.user_id != clientUserId) {
                        this.groupSearchMap.put(tLRPC$ChannelParticipant.user_id, tLRPC$ChannelParticipant);
                    } else {
                        this.groupSearch.remove(tLRPC$ChannelParticipant);
                    }
                }
                removeGroupSearchFromGlobal();
                ArrayList<TLObject> arrayList = this.localSearchResults;
                if (arrayList != null) {
                    mergeResults(arrayList);
                }
                this.delegate.onDataSetChanged(i2);
            }
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$queryServerSearch$3 */
    public /* synthetic */ void lambda$queryServerSearch$3$SearchAdapterHelper(int i, int i2, boolean z, boolean z2, boolean z3, boolean z4, String str, TLObject tLObject, TLRPC$TL_error tLRPC$TL_error) {
        AndroidUtilities.runOnUIThread(new Runnable(i, i2, tLRPC$TL_error, tLObject, z, z2, z3, z4, str) {
            public final /* synthetic */ int f$1;
            public final /* synthetic */ int f$2;
            public final /* synthetic */ TLRPC$TL_error f$3;
            public final /* synthetic */ TLObject f$4;
            public final /* synthetic */ boolean f$5;
            public final /* synthetic */ boolean f$6;
            public final /* synthetic */ boolean f$7;
            public final /* synthetic */ boolean f$8;
            public final /* synthetic */ String f$9;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
                this.f$4 = r5;
                this.f$5 = r6;
                this.f$6 = r7;
                this.f$7 = r8;
                this.f$8 = r9;
                this.f$9 = r10;
            }

            public final void run() {
                SearchAdapterHelper.this.lambda$null$2$SearchAdapterHelper(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6, this.f$7, this.f$8, this.f$9);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$null$2 */
    public /* synthetic */ void lambda$null$2$SearchAdapterHelper(int i, int i2, TLRPC$TL_error tLRPC$TL_error, TLObject tLObject, boolean z, boolean z2, boolean z3, boolean z4, String str) {
        TLRPC$User tLRPC$User;
        TLRPC$Chat tLRPC$Chat;
        ArrayList<TLRPC$Peer> arrayList;
        TLRPC$User tLRPC$User2;
        TLRPC$Chat tLRPC$Chat2;
        int i3 = i;
        int i4 = i2;
        int i5 = this.lastReqId;
        if (i3 == i5) {
            this.reqId = 0;
        }
        if (i3 == i5 && this.delegate.canApplySearchResults(i4) && tLRPC$TL_error == null) {
            TLRPC$TL_contacts_found tLRPC$TL_contacts_found = (TLRPC$TL_contacts_found) tLObject;
            this.globalSearch.clear();
            this.globalSearchMap.clear();
            this.localServerSearch.clear();
            MessagesController.getInstance(this.currentAccount).putChats(tLRPC$TL_contacts_found.chats, false);
            MessagesController.getInstance(this.currentAccount).putUsers(tLRPC$TL_contacts_found.users, false);
            MessagesStorage.getInstance(this.currentAccount).putUsersAndChats(tLRPC$TL_contacts_found.users, tLRPC$TL_contacts_found.chats, true, true);
            SparseArray sparseArray = new SparseArray();
            SparseArray sparseArray2 = new SparseArray();
            for (int i6 = 0; i6 < tLRPC$TL_contacts_found.chats.size(); i6++) {
                TLRPC$Chat tLRPC$Chat3 = tLRPC$TL_contacts_found.chats.get(i6);
                sparseArray.put(tLRPC$Chat3.id, tLRPC$Chat3);
            }
            for (int i7 = 0; i7 < tLRPC$TL_contacts_found.users.size(); i7++) {
                TLRPC$User tLRPC$User3 = tLRPC$TL_contacts_found.users.get(i7);
                sparseArray2.put(tLRPC$User3.id, tLRPC$User3);
            }
            for (int i8 = 0; i8 < 2; i8++) {
                if (i8 != 0) {
                    arrayList = tLRPC$TL_contacts_found.results;
                } else if (!this.allResultsAreGlobal) {
                } else {
                    arrayList = tLRPC$TL_contacts_found.my_results;
                }
                for (int i9 = 0; i9 < arrayList.size(); i9++) {
                    TLRPC$Peer tLRPC$Peer = arrayList.get(i9);
                    int i10 = tLRPC$Peer.user_id;
                    if (i10 != 0) {
                        tLRPC$User2 = (TLRPC$User) sparseArray2.get(i10);
                        tLRPC$Chat2 = null;
                    } else {
                        int i11 = tLRPC$Peer.chat_id;
                        if (i11 != 0) {
                            tLRPC$Chat2 = (TLRPC$Chat) sparseArray.get(i11);
                        } else {
                            int i12 = tLRPC$Peer.channel_id;
                            if (i12 != 0) {
                                tLRPC$Chat2 = (TLRPC$Chat) sparseArray.get(i12);
                            } else {
                                tLRPC$Chat2 = null;
                                tLRPC$User2 = null;
                            }
                        }
                        tLRPC$User2 = null;
                    }
                    if (tLRPC$Chat2 != null) {
                        if (z && ((!z2 || ChatObject.canAddBotsToChat(tLRPC$Chat2)) && (this.allowGlobalResults || !ChatObject.isNotInChat(tLRPC$Chat2)))) {
                            this.globalSearch.add(tLRPC$Chat2);
                            this.globalSearchMap.put(-tLRPC$Chat2.id, tLRPC$Chat2);
                        }
                    } else if (tLRPC$User2 != null && !z2 && ((z3 || !tLRPC$User2.bot) && ((z4 || !tLRPC$User2.self) && (this.allowGlobalResults || i8 != 1 || tLRPC$User2.contact)))) {
                        this.globalSearch.add(tLRPC$User2);
                        this.globalSearchMap.put(tLRPC$User2.id, tLRPC$User2);
                    }
                }
            }
            if (!this.allResultsAreGlobal) {
                for (int i13 = 0; i13 < tLRPC$TL_contacts_found.my_results.size(); i13++) {
                    TLRPC$Peer tLRPC$Peer2 = tLRPC$TL_contacts_found.my_results.get(i13);
                    int i14 = tLRPC$Peer2.user_id;
                    if (i14 != 0) {
                        tLRPC$User = (TLRPC$User) sparseArray2.get(i14);
                        tLRPC$Chat = null;
                    } else {
                        int i15 = tLRPC$Peer2.chat_id;
                        if (i15 != 0) {
                            tLRPC$Chat = (TLRPC$Chat) sparseArray.get(i15);
                        } else {
                            int i16 = tLRPC$Peer2.channel_id;
                            if (i16 != 0) {
                                tLRPC$Chat = (TLRPC$Chat) sparseArray.get(i16);
                            } else {
                                tLRPC$Chat = null;
                                tLRPC$User = null;
                            }
                        }
                        tLRPC$User = null;
                    }
                    if (tLRPC$Chat != null) {
                        if (z && (!z2 || ChatObject.canAddBotsToChat(tLRPC$Chat))) {
                            this.localServerSearch.add(tLRPC$Chat);
                            this.globalSearchMap.put(-tLRPC$Chat.id, tLRPC$Chat);
                        }
                    } else if (tLRPC$User != null && !z2 && ((z3 || !tLRPC$User.bot) && (z4 || !tLRPC$User.self))) {
                        this.localServerSearch.add(tLRPC$User);
                        this.globalSearchMap.put(tLRPC$User.id, tLRPC$User);
                    }
                }
            }
            removeGroupSearchFromGlobal();
            this.lastFoundUsername = str.toLowerCase();
            ArrayList<TLObject> arrayList2 = this.localSearchResults;
            if (arrayList2 != null) {
                mergeResults(arrayList2);
            }
            mergeExcludeResults();
            this.delegate.onDataSetChanged(i4);
        }
    }

    private void removeGroupSearchFromGlobal() {
        if (this.globalSearchMap.size() != 0) {
            int size = this.groupSearchMap.size();
            for (int i = 0; i < size; i++) {
                TLRPC$User tLRPC$User = (TLRPC$User) this.globalSearchMap.get(this.groupSearchMap.keyAt(i));
                if (tLRPC$User != null) {
                    this.globalSearch.remove(tLRPC$User);
                    this.localServerSearch.remove(tLRPC$User);
                    this.globalSearchMap.remove(tLRPC$User.id);
                }
            }
        }
    }

    public void clear() {
        this.globalSearch.clear();
        this.globalSearchMap.clear();
        this.localServerSearch.clear();
    }

    public void unloadRecentHashtags() {
        this.hashtagsLoadedFromDb = false;
    }

    public boolean loadRecentHashtags() {
        if (this.hashtagsLoadedFromDb) {
            return true;
        }
        MessagesStorage.getInstance(this.currentAccount).getStorageQueue().postRunnable(new Runnable() {
            public final void run() {
                SearchAdapterHelper.this.lambda$loadRecentHashtags$6$SearchAdapterHelper();
            }
        });
        return false;
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$loadRecentHashtags$6 */
    public /* synthetic */ void lambda$loadRecentHashtags$6$SearchAdapterHelper() {
        try {
            SQLiteCursor queryFinalized = MessagesStorage.getInstance(this.currentAccount).getDatabase().queryFinalized("SELECT id, date FROM hashtag_recent_v2 WHERE 1", new Object[0]);
            ArrayList arrayList = new ArrayList();
            HashMap hashMap = new HashMap();
            while (queryFinalized.next()) {
                HashtagObject hashtagObject = new HashtagObject();
                hashtagObject.hashtag = queryFinalized.stringValue(0);
                hashtagObject.date = queryFinalized.intValue(1);
                arrayList.add(hashtagObject);
                hashMap.put(hashtagObject.hashtag, hashtagObject);
            }
            queryFinalized.dispose();
            Collections.sort(arrayList, $$Lambda$SearchAdapterHelper$733hu53wtvf8mK0zV5cqPQBmD58.INSTANCE);
            AndroidUtilities.runOnUIThread(new Runnable(arrayList, hashMap) {
                public final /* synthetic */ ArrayList f$1;
                public final /* synthetic */ HashMap f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                public final void run() {
                    SearchAdapterHelper.this.lambda$null$5$SearchAdapterHelper(this.f$1, this.f$2);
                }
            });
        } catch (Exception e) {
            FileLog.e((Throwable) e);
        }
    }

    static /* synthetic */ int lambda$null$4(HashtagObject hashtagObject, HashtagObject hashtagObject2) {
        int i = hashtagObject.date;
        int i2 = hashtagObject2.date;
        if (i < i2) {
            return 1;
        }
        return i > i2 ? -1 : 0;
    }

    public void addGroupMembers(ArrayList<TLObject> arrayList) {
        this.groupSearch.clear();
        this.groupSearch.addAll(arrayList);
        int size = arrayList.size();
        for (int i = 0; i < size; i++) {
            TLObject tLObject = arrayList.get(i);
            if (tLObject instanceof TLRPC$ChatParticipant) {
                this.groupSearchMap.put(((TLRPC$ChatParticipant) tLObject).user_id, tLObject);
            } else if (tLObject instanceof TLRPC$ChannelParticipant) {
                this.groupSearchMap.put(((TLRPC$ChannelParticipant) tLObject).user_id, tLObject);
            }
        }
        removeGroupSearchFromGlobal();
    }

    public void mergeResults(ArrayList<TLObject> arrayList) {
        TLRPC$Chat tLRPC$Chat;
        this.localSearchResults = arrayList;
        if (this.globalSearchMap.size() != 0 && arrayList != null) {
            int size = arrayList.size();
            for (int i = 0; i < size; i++) {
                TLObject tLObject = arrayList.get(i);
                if (tLObject instanceof TLRPC$User) {
                    TLRPC$User tLRPC$User = (TLRPC$User) tLObject;
                    TLRPC$User tLRPC$User2 = (TLRPC$User) this.globalSearchMap.get(tLRPC$User.id);
                    if (tLRPC$User2 != null) {
                        this.globalSearch.remove(tLRPC$User2);
                        this.localServerSearch.remove(tLRPC$User2);
                        this.globalSearchMap.remove(tLRPC$User2.id);
                    }
                    TLObject tLObject2 = this.groupSearchMap.get(tLRPC$User.id);
                    if (tLObject2 != null) {
                        this.groupSearch.remove(tLObject2);
                        this.groupSearchMap.remove(tLRPC$User.id);
                    }
                    TLObject tLObject3 = this.phoneSearchMap.get(tLRPC$User.id);
                    if (tLObject3 != null) {
                        this.phonesSearch.remove(tLObject3);
                        this.phoneSearchMap.remove(tLRPC$User.id);
                    }
                } else if ((tLObject instanceof TLRPC$Chat) && (tLRPC$Chat = (TLRPC$Chat) this.globalSearchMap.get(-((TLRPC$Chat) tLObject).id)) != null) {
                    this.globalSearch.remove(tLRPC$Chat);
                    this.localServerSearch.remove(tLRPC$Chat);
                    this.globalSearchMap.remove(-tLRPC$Chat.id);
                }
            }
        }
    }

    public void mergeExcludeResults() {
        SearchAdapterHelperDelegate searchAdapterHelperDelegate = this.delegate;
        if (searchAdapterHelperDelegate != null) {
            SparseArray<TLRPC$User> excludeUsers = searchAdapterHelperDelegate.getExcludeUsers();
            if (excludeUsers != null) {
                int size = excludeUsers.size();
                for (int i = 0; i < size; i++) {
                    TLRPC$User tLRPC$User = (TLRPC$User) this.globalSearchMap.get(excludeUsers.keyAt(i));
                    if (tLRPC$User != null) {
                        this.globalSearch.remove(tLRPC$User);
                        this.localServerSearch.remove(tLRPC$User);
                        this.globalSearchMap.remove(tLRPC$User.id);
                    }
                }
            }
            SparseArray<TLRPC$TL_groupCallParticipant> excludeCallParticipants = this.delegate.getExcludeCallParticipants();
            if (excludeCallParticipants != null) {
                int size2 = excludeCallParticipants.size();
                for (int i2 = 0; i2 < size2; i2++) {
                    TLRPC$User tLRPC$User2 = (TLRPC$User) this.globalSearchMap.get(excludeCallParticipants.keyAt(i2));
                    if (tLRPC$User2 != null) {
                        this.globalSearch.remove(tLRPC$User2);
                        this.localServerSearch.remove(tLRPC$User2);
                        this.globalSearchMap.remove(tLRPC$User2.id);
                    }
                }
            }
        }
    }

    public void setDelegate(SearchAdapterHelperDelegate searchAdapterHelperDelegate) {
        this.delegate = searchAdapterHelperDelegate;
    }

    public void addHashtagsFromMessage(CharSequence charSequence) {
        if (charSequence != null) {
            Matcher matcher = Pattern.compile("(^|\\s)#[^0-9][\\w@.]+").matcher(charSequence);
            boolean z = false;
            while (matcher.find()) {
                int start = matcher.start();
                int end = matcher.end();
                if (!(charSequence.charAt(start) == '@' || charSequence.charAt(start) == '#')) {
                    start++;
                }
                String charSequence2 = charSequence.subSequence(start, end).toString();
                if (this.hashtagsByText == null) {
                    this.hashtagsByText = new HashMap<>();
                    this.hashtags = new ArrayList<>();
                }
                HashtagObject hashtagObject = this.hashtagsByText.get(charSequence2);
                if (hashtagObject == null) {
                    hashtagObject = new HashtagObject();
                    hashtagObject.hashtag = charSequence2;
                    this.hashtagsByText.put(charSequence2, hashtagObject);
                } else {
                    this.hashtags.remove(hashtagObject);
                }
                hashtagObject.date = (int) (System.currentTimeMillis() / 1000);
                this.hashtags.add(0, hashtagObject);
                z = true;
            }
            if (z) {
                putRecentHashtags(this.hashtags);
            }
        }
    }

    private void putRecentHashtags(ArrayList<HashtagObject> arrayList) {
        MessagesStorage.getInstance(this.currentAccount).getStorageQueue().postRunnable(new Runnable(arrayList) {
            public final /* synthetic */ ArrayList f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                SearchAdapterHelper.this.lambda$putRecentHashtags$7$SearchAdapterHelper(this.f$1);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$putRecentHashtags$7 */
    public /* synthetic */ void lambda$putRecentHashtags$7$SearchAdapterHelper(ArrayList arrayList) {
        int i;
        try {
            MessagesStorage.getInstance(this.currentAccount).getDatabase().beginTransaction();
            SQLitePreparedStatement executeFast = MessagesStorage.getInstance(this.currentAccount).getDatabase().executeFast("REPLACE INTO hashtag_recent_v2 VALUES(?, ?)");
            int i2 = 0;
            while (true) {
                if (i2 >= arrayList.size()) {
                    break;
                } else if (i2 == 100) {
                    break;
                } else {
                    HashtagObject hashtagObject = (HashtagObject) arrayList.get(i2);
                    executeFast.requery();
                    executeFast.bindString(1, hashtagObject.hashtag);
                    executeFast.bindInteger(2, hashtagObject.date);
                    executeFast.step();
                    i2++;
                }
            }
            executeFast.dispose();
            if (arrayList.size() > 100) {
                SQLitePreparedStatement executeFast2 = MessagesStorage.getInstance(this.currentAccount).getDatabase().executeFast("DELETE FROM hashtag_recent_v2 WHERE id = ?");
                for (i = 100; i < arrayList.size(); i++) {
                    executeFast2.requery();
                    executeFast2.bindString(1, ((HashtagObject) arrayList.get(i)).hashtag);
                    executeFast2.step();
                }
                executeFast2.dispose();
            }
            MessagesStorage.getInstance(this.currentAccount).getDatabase().commitTransaction();
        } catch (Exception e) {
            FileLog.e((Throwable) e);
        }
    }

    public void removeUserId(int i) {
        TLObject tLObject = this.globalSearchMap.get(i);
        if (tLObject != null) {
            this.globalSearch.remove(tLObject);
        }
        TLObject tLObject2 = this.groupSearchMap.get(i);
        if (tLObject2 != null) {
            this.groupSearch.remove(tLObject2);
        }
    }

    public ArrayList<TLObject> getGlobalSearch() {
        return this.globalSearch;
    }

    public ArrayList<Object> getPhoneSearch() {
        return this.phonesSearch;
    }

    public ArrayList<TLObject> getLocalServerSearch() {
        return this.localServerSearch;
    }

    public ArrayList<TLObject> getGroupSearch() {
        return this.groupSearch;
    }

    public ArrayList<HashtagObject> getHashtags() {
        return this.hashtags;
    }

    public String getLastFoundUsername() {
        return this.lastFoundUsername;
    }

    public String getLastFoundChannel() {
        return this.lastFoundChannel;
    }

    public void clearRecentHashtags() {
        this.hashtags = new ArrayList<>();
        this.hashtagsByText = new HashMap<>();
        MessagesStorage.getInstance(this.currentAccount).getStorageQueue().postRunnable(new Runnable() {
            public final void run() {
                SearchAdapterHelper.this.lambda$clearRecentHashtags$8$SearchAdapterHelper();
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$clearRecentHashtags$8 */
    public /* synthetic */ void lambda$clearRecentHashtags$8$SearchAdapterHelper() {
        try {
            MessagesStorage.getInstance(this.currentAccount).getDatabase().executeFast("DELETE FROM hashtag_recent_v2 WHERE 1").stepThis().dispose();
        } catch (Exception e) {
            FileLog.e((Throwable) e);
        }
    }

    /* renamed from: setHashtags */
    public void lambda$null$5(ArrayList<HashtagObject> arrayList, HashMap<String, HashtagObject> hashMap) {
        this.hashtags = arrayList;
        this.hashtagsByText = hashMap;
        this.hashtagsLoadedFromDb = true;
        this.delegate.onSetHashtags(arrayList, hashMap);
    }
}
