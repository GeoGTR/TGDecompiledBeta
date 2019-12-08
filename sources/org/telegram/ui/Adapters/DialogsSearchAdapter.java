package org.telegram.ui.Adapters;

import android.content.Context;
import android.text.TextUtils;
import android.util.LongSparseArray;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView.LayoutParams;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import org.telegram.SQLite.SQLitePreparedStatement;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.DispatchQueue;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.MediaDataController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.messenger.Utilities;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.Chat;
import org.telegram.tgnet.TLRPC.EncryptedChat;
import org.telegram.tgnet.TLRPC.Message;
import org.telegram.tgnet.TLRPC.Peer;
import org.telegram.tgnet.TLRPC.TL_dialog;
import org.telegram.tgnet.TLRPC.TL_error;
import org.telegram.tgnet.TLRPC.TL_inputPeerEmpty;
import org.telegram.tgnet.TLRPC.TL_messages_searchGlobal;
import org.telegram.tgnet.TLRPC.TL_topPeer;
import org.telegram.tgnet.TLRPC.User;
import org.telegram.tgnet.TLRPC.messages_Messages;
import org.telegram.ui.Adapters.SearchAdapterHelper.HashtagObject;
import org.telegram.ui.Adapters.SearchAdapterHelper.SearchAdapterHelperDelegate;
import org.telegram.ui.Adapters.SearchAdapterHelper.SearchAdapterHelperDelegate.-CC;
import org.telegram.ui.Cells.DialogCell;
import org.telegram.ui.Cells.GraySectionCell;
import org.telegram.ui.Cells.HashtagSearchCell;
import org.telegram.ui.Cells.HintDialogCell;
import org.telegram.ui.Cells.LoadingCell;
import org.telegram.ui.Cells.ProfileSearchCell;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.RecyclerListView.Holder;
import org.telegram.ui.Components.RecyclerListView.SelectionAdapter;

public class DialogsSearchAdapter extends SelectionAdapter {
    private int currentAccount = UserConfig.selectedAccount;
    private DialogsSearchAdapterDelegate delegate;
    private int dialogsType;
    private RecyclerListView innerListView;
    private String lastMessagesSearchString;
    private int lastReqId;
    private int lastSearchId = 0;
    private String lastSearchText;
    private Context mContext;
    private boolean messagesSearchEndReached;
    private int needMessagesSearch;
    private int nextSearchRate;
    private ArrayList<RecentSearchObject> recentSearchObjects = new ArrayList();
    private LongSparseArray<RecentSearchObject> recentSearchObjectsById = new LongSparseArray();
    private int reqId = 0;
    private SearchAdapterHelper searchAdapterHelper = new SearchAdapterHelper(false);
    private ArrayList<TLObject> searchResult = new ArrayList();
    private ArrayList<String> searchResultHashtags = new ArrayList();
    private ArrayList<MessageObject> searchResultMessages = new ArrayList();
    private ArrayList<CharSequence> searchResultNames = new ArrayList();
    private Runnable searchRunnable;
    private Runnable searchRunnable2;
    private boolean searchWas;
    private int selfUserId;

    private class DialogSearchResult {
        public int date;
        public CharSequence name;
        public TLObject object;

        private DialogSearchResult() {
        }

        /* synthetic */ DialogSearchResult(DialogsSearchAdapter dialogsSearchAdapter, AnonymousClass1 anonymousClass1) {
            this();
        }
    }

    public interface DialogsSearchAdapterDelegate {
        void didPressedOnSubDialog(long j);

        void needClearList();

        void needRemoveHint(int i);

        void searchStateChanged(boolean z);
    }

    protected static class RecentSearchObject {
        int date;
        long did;
        TLObject object;

        protected RecentSearchObject() {
        }
    }

    private class CategoryAdapterRecycler extends SelectionAdapter {
        public boolean isEnabled(ViewHolder viewHolder) {
            return true;
        }

        private CategoryAdapterRecycler() {
        }

        /* synthetic */ CategoryAdapterRecycler(DialogsSearchAdapter dialogsSearchAdapter, AnonymousClass1 anonymousClass1) {
            this();
        }

        public void setIndex(int i) {
            notifyDataSetChanged();
        }

        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            HintDialogCell hintDialogCell = new HintDialogCell(DialogsSearchAdapter.this.mContext);
            hintDialogCell.setLayoutParams(new LayoutParams(AndroidUtilities.dp(80.0f), AndroidUtilities.dp(86.0f)));
            return new Holder(hintDialogCell);
        }

        public void onBindViewHolder(ViewHolder viewHolder, int i) {
            Chat chat;
            HintDialogCell hintDialogCell = (HintDialogCell) viewHolder.itemView;
            TL_topPeer tL_topPeer = (TL_topPeer) MediaDataController.getInstance(DialogsSearchAdapter.this.currentAccount).hints.get(i);
            TL_dialog tL_dialog = new TL_dialog();
            Peer peer = tL_topPeer.peer;
            int i2 = peer.user_id;
            User user = null;
            if (i2 != 0) {
                user = MessagesController.getInstance(DialogsSearchAdapter.this.currentAccount).getUser(Integer.valueOf(tL_topPeer.peer.user_id));
                chat = null;
            } else {
                i2 = peer.channel_id;
                if (i2 != 0) {
                    i2 = -i2;
                    chat = MessagesController.getInstance(DialogsSearchAdapter.this.currentAccount).getChat(Integer.valueOf(tL_topPeer.peer.channel_id));
                } else {
                    int i3 = peer.chat_id;
                    if (i3 != 0) {
                        i2 = -i3;
                        chat = MessagesController.getInstance(DialogsSearchAdapter.this.currentAccount).getChat(Integer.valueOf(tL_topPeer.peer.chat_id));
                    } else {
                        i2 = 0;
                        chat = null;
                    }
                }
            }
            hintDialogCell.setTag(Integer.valueOf(i2));
            CharSequence firstName = user != null ? UserObject.getFirstName(user) : chat != null ? chat.title : "";
            hintDialogCell.setDialog(i2, true, firstName);
        }

        public int getItemCount() {
            return MediaDataController.getInstance(DialogsSearchAdapter.this.currentAccount).hints.size();
        }
    }

    public long getItemId(int i) {
        return (long) i;
    }

    public DialogsSearchAdapter(Context context, int i, int i2) {
        this.searchAdapterHelper.setDelegate(new SearchAdapterHelperDelegate() {
            public /* synthetic */ SparseArray<User> getExcludeUsers() {
                return -CC.$default$getExcludeUsers(this);
            }

            public void onDataSetChanged() {
                DialogsSearchAdapter.this.searchWas = true;
                if (!(DialogsSearchAdapter.this.searchAdapterHelper.isSearchInProgress() || DialogsSearchAdapter.this.delegate == null)) {
                    DialogsSearchAdapter.this.delegate.searchStateChanged(false);
                }
                DialogsSearchAdapter.this.notifyDataSetChanged();
            }

            public void onSetHashtags(ArrayList<HashtagObject> arrayList, HashMap<String, HashtagObject> hashMap) {
                for (int i = 0; i < arrayList.size(); i++) {
                    DialogsSearchAdapter.this.searchResultHashtags.add(((HashtagObject) arrayList.get(i)).hashtag);
                }
                if (DialogsSearchAdapter.this.delegate != null) {
                    DialogsSearchAdapter.this.delegate.searchStateChanged(false);
                }
                DialogsSearchAdapter.this.notifyDataSetChanged();
            }
        });
        this.mContext = context;
        this.needMessagesSearch = i;
        this.dialogsType = i2;
        this.selfUserId = UserConfig.getInstance(this.currentAccount).getClientUserId();
        loadRecentSearch();
        MediaDataController.getInstance(this.currentAccount).loadHints(true);
    }

    public RecyclerListView getInnerListView() {
        return this.innerListView;
    }

    public void setDelegate(DialogsSearchAdapterDelegate dialogsSearchAdapterDelegate) {
        this.delegate = dialogsSearchAdapterDelegate;
    }

    public boolean isMessagesSearchEndReached() {
        return this.messagesSearchEndReached;
    }

    public void loadMoreSearchMessages() {
        searchMessagesInternal(this.lastMessagesSearchString);
    }

    public String getLastSearchString() {
        return this.lastMessagesSearchString;
    }

    private void searchMessagesInternal(String str) {
        if (!(this.needMessagesSearch == 0 || (TextUtils.isEmpty(this.lastMessagesSearchString) && TextUtils.isEmpty(str)))) {
            if (this.reqId != 0) {
                ConnectionsManager.getInstance(this.currentAccount).cancelRequest(this.reqId, true);
                this.reqId = 0;
            }
            if (TextUtils.isEmpty(str)) {
                this.searchResultMessages.clear();
                this.lastReqId = 0;
                this.lastMessagesSearchString = null;
                this.searchWas = false;
                notifyDataSetChanged();
                DialogsSearchAdapterDelegate dialogsSearchAdapterDelegate = this.delegate;
                if (dialogsSearchAdapterDelegate != null) {
                    dialogsSearchAdapterDelegate.searchStateChanged(false);
                }
                return;
            }
            TL_messages_searchGlobal tL_messages_searchGlobal = new TL_messages_searchGlobal();
            tL_messages_searchGlobal.limit = 20;
            tL_messages_searchGlobal.q = str;
            if (!str.equals(this.lastMessagesSearchString) || this.searchResultMessages.isEmpty()) {
                tL_messages_searchGlobal.offset_rate = 0;
                tL_messages_searchGlobal.offset_id = 0;
                tL_messages_searchGlobal.offset_peer = new TL_inputPeerEmpty();
            } else {
                int i;
                ArrayList arrayList = this.searchResultMessages;
                MessageObject messageObject = (MessageObject) arrayList.get(arrayList.size() - 1);
                tL_messages_searchGlobal.offset_id = messageObject.getId();
                tL_messages_searchGlobal.offset_rate = this.nextSearchRate;
                Peer peer = messageObject.messageOwner.to_id;
                int i2 = peer.channel_id;
                if (i2 == 0) {
                    i2 = peer.chat_id;
                    if (i2 == 0) {
                        i = peer.user_id;
                        tL_messages_searchGlobal.offset_peer = MessagesController.getInstance(this.currentAccount).getInputPeer(i);
                    }
                }
                i = -i2;
                tL_messages_searchGlobal.offset_peer = MessagesController.getInstance(this.currentAccount).getInputPeer(i);
            }
            this.lastMessagesSearchString = str;
            int i3 = this.lastReqId + 1;
            this.lastReqId = i3;
            this.reqId = ConnectionsManager.getInstance(this.currentAccount).sendRequest(tL_messages_searchGlobal, new -$$Lambda$DialogsSearchAdapter$jvBeESgA8AL2rHOVVbFgj80mlPg(this, i3, tL_messages_searchGlobal), 2);
        }
    }

    public /* synthetic */ void lambda$searchMessagesInternal$1$DialogsSearchAdapter(int i, TL_messages_searchGlobal tL_messages_searchGlobal, TLObject tLObject, TL_error tL_error) {
        AndroidUtilities.runOnUIThread(new -$$Lambda$DialogsSearchAdapter$tzGJ1M1cHM4tSDBBFCCxa18ZhzA(this, i, tL_error, tLObject, tL_messages_searchGlobal));
    }

    public /* synthetic */ void lambda$null$0$DialogsSearchAdapter(int i, TL_error tL_error, TLObject tLObject, TL_messages_searchGlobal tL_messages_searchGlobal) {
        if (i == this.lastReqId && tL_error == null) {
            messages_Messages messages_messages = (messages_Messages) tLObject;
            boolean z = true;
            MessagesStorage.getInstance(this.currentAccount).putUsersAndChats(messages_messages.users, messages_messages.chats, true, true);
            MessagesController.getInstance(this.currentAccount).putUsers(messages_messages.users, false);
            MessagesController.getInstance(this.currentAccount).putChats(messages_messages.chats, false);
            if (tL_messages_searchGlobal.offset_id == 0) {
                this.searchResultMessages.clear();
            }
            this.nextSearchRate = messages_messages.next_rate;
            for (i = 0; i < messages_messages.messages.size(); i++) {
                Message message = (Message) messages_messages.messages.get(i);
                this.searchResultMessages.add(new MessageObject(this.currentAccount, message, false));
                long dialogId = MessageObject.getDialogId(message);
                ConcurrentHashMap concurrentHashMap = message.out ? MessagesController.getInstance(this.currentAccount).dialogs_read_outbox_max : MessagesController.getInstance(this.currentAccount).dialogs_read_inbox_max;
                Integer num = (Integer) concurrentHashMap.get(Long.valueOf(dialogId));
                if (num == null) {
                    num = Integer.valueOf(MessagesStorage.getInstance(this.currentAccount).getDialogReadMax(message.out, dialogId));
                    concurrentHashMap.put(Long.valueOf(dialogId), num);
                }
                message.unread = num.intValue() < message.id;
            }
            this.searchWas = true;
            if (messages_messages.messages.size() == 20) {
                z = false;
            }
            this.messagesSearchEndReached = z;
            notifyDataSetChanged();
        }
        DialogsSearchAdapterDelegate dialogsSearchAdapterDelegate = this.delegate;
        if (dialogsSearchAdapterDelegate != null) {
            dialogsSearchAdapterDelegate.searchStateChanged(false);
        }
        this.reqId = 0;
    }

    public boolean hasRecentRearch() {
        int i = this.dialogsType;
        return (i == 4 || i == 5 || i == 6 || (this.recentSearchObjects.isEmpty() && MediaDataController.getInstance(this.currentAccount).hints.isEmpty())) ? false : true;
    }

    public boolean isRecentSearchDisplayed() {
        if (!(this.needMessagesSearch == 2 || this.searchWas || (this.recentSearchObjects.isEmpty() && MediaDataController.getInstance(this.currentAccount).hints.isEmpty()))) {
            int i = this.dialogsType;
            if (!(i == 4 || i == 5 || i == 6)) {
                return true;
            }
        }
        return false;
    }

    public void loadRecentSearch() {
        MessagesStorage.getInstance(this.currentAccount).getStorageQueue().postRunnable(new -$$Lambda$DialogsSearchAdapter$EdS8aWM1r9L4_WkQYwpXcCyFRfM(this));
    }

    /* JADX WARNING: Removed duplicated region for block: B:60:0x0031 A:{SYNTHETIC} */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x008f A:{Catch:{ Exception -> 0x016d }} */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x008f A:{Catch:{ Exception -> 0x016d }} */
    /* JADX WARNING: Removed duplicated region for block: B:60:0x0031 A:{SYNTHETIC} */
    /* JADX WARNING: Removed duplicated region for block: B:60:0x0031 A:{SYNTHETIC} */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x008f A:{Catch:{ Exception -> 0x016d }} */
    public /* synthetic */ void lambda$loadRecentSearch$4$DialogsSearchAdapter() {
        /*
        r13 = this;
        r0 = r13.currentAccount;	 Catch:{ Exception -> 0x016d }
        r0 = org.telegram.messenger.MessagesStorage.getInstance(r0);	 Catch:{ Exception -> 0x016d }
        r0 = r0.getDatabase();	 Catch:{ Exception -> 0x016d }
        r1 = "SELECT did, date FROM search_recent WHERE 1";
        r2 = 0;
        r3 = new java.lang.Object[r2];	 Catch:{ Exception -> 0x016d }
        r0 = r0.queryFinalized(r1, r3);	 Catch:{ Exception -> 0x016d }
        r1 = new java.util.ArrayList;	 Catch:{ Exception -> 0x016d }
        r1.<init>();	 Catch:{ Exception -> 0x016d }
        r3 = new java.util.ArrayList;	 Catch:{ Exception -> 0x016d }
        r3.<init>();	 Catch:{ Exception -> 0x016d }
        r4 = new java.util.ArrayList;	 Catch:{ Exception -> 0x016d }
        r4.<init>();	 Catch:{ Exception -> 0x016d }
        r5 = new java.util.ArrayList;	 Catch:{ Exception -> 0x016d }
        r5.<init>();	 Catch:{ Exception -> 0x016d }
        r5 = new java.util.ArrayList;	 Catch:{ Exception -> 0x016d }
        r5.<init>();	 Catch:{ Exception -> 0x016d }
        r6 = new android.util.LongSparseArray;	 Catch:{ Exception -> 0x016d }
        r6.<init>();	 Catch:{ Exception -> 0x016d }
    L_0x0031:
        r7 = r0.next();	 Catch:{ Exception -> 0x016d }
        r8 = 32;
        if (r7 == 0) goto L_0x00a5;
    L_0x0039:
        r9 = r0.longValue(r2);	 Catch:{ Exception -> 0x016d }
        r7 = (int) r9;	 Catch:{ Exception -> 0x016d }
        r11 = r9 >> r8;
        r8 = (int) r11;	 Catch:{ Exception -> 0x016d }
        r11 = 1;
        if (r7 == 0) goto L_0x0071;
    L_0x0044:
        if (r7 <= 0) goto L_0x005d;
    L_0x0046:
        r8 = r13.dialogsType;	 Catch:{ Exception -> 0x016d }
        r12 = 2;
        if (r8 == r12) goto L_0x008c;
    L_0x004b:
        r8 = java.lang.Integer.valueOf(r7);	 Catch:{ Exception -> 0x016d }
        r8 = r1.contains(r8);	 Catch:{ Exception -> 0x016d }
        if (r8 != 0) goto L_0x008c;
    L_0x0055:
        r7 = java.lang.Integer.valueOf(r7);	 Catch:{ Exception -> 0x016d }
        r1.add(r7);	 Catch:{ Exception -> 0x016d }
        goto L_0x006f;
    L_0x005d:
        r7 = -r7;
        r8 = java.lang.Integer.valueOf(r7);	 Catch:{ Exception -> 0x016d }
        r8 = r3.contains(r8);	 Catch:{ Exception -> 0x016d }
        if (r8 != 0) goto L_0x008c;
    L_0x0068:
        r7 = java.lang.Integer.valueOf(r7);	 Catch:{ Exception -> 0x016d }
        r3.add(r7);	 Catch:{ Exception -> 0x016d }
    L_0x006f:
        r7 = 1;
        goto L_0x008d;
    L_0x0071:
        r7 = r13.dialogsType;	 Catch:{ Exception -> 0x016d }
        if (r7 == 0) goto L_0x007a;
    L_0x0075:
        r7 = r13.dialogsType;	 Catch:{ Exception -> 0x016d }
        r12 = 3;
        if (r7 != r12) goto L_0x008c;
    L_0x007a:
        r7 = java.lang.Integer.valueOf(r8);	 Catch:{ Exception -> 0x016d }
        r7 = r4.contains(r7);	 Catch:{ Exception -> 0x016d }
        if (r7 != 0) goto L_0x008c;
    L_0x0084:
        r7 = java.lang.Integer.valueOf(r8);	 Catch:{ Exception -> 0x016d }
        r4.add(r7);	 Catch:{ Exception -> 0x016d }
        goto L_0x006f;
    L_0x008c:
        r7 = 0;
    L_0x008d:
        if (r7 == 0) goto L_0x0031;
    L_0x008f:
        r7 = new org.telegram.ui.Adapters.DialogsSearchAdapter$RecentSearchObject;	 Catch:{ Exception -> 0x016d }
        r7.<init>();	 Catch:{ Exception -> 0x016d }
        r7.did = r9;	 Catch:{ Exception -> 0x016d }
        r8 = r0.intValue(r11);	 Catch:{ Exception -> 0x016d }
        r7.date = r8;	 Catch:{ Exception -> 0x016d }
        r5.add(r7);	 Catch:{ Exception -> 0x016d }
        r8 = r7.did;	 Catch:{ Exception -> 0x016d }
        r6.put(r8, r7);	 Catch:{ Exception -> 0x016d }
        goto L_0x0031;
    L_0x00a5:
        r0.dispose();	 Catch:{ Exception -> 0x016d }
        r0 = new java.util.ArrayList;	 Catch:{ Exception -> 0x016d }
        r0.<init>();	 Catch:{ Exception -> 0x016d }
        r7 = r4.isEmpty();	 Catch:{ Exception -> 0x016d }
        r9 = ",";
        if (r7 != 0) goto L_0x00e9;
    L_0x00b5:
        r7 = new java.util.ArrayList;	 Catch:{ Exception -> 0x016d }
        r7.<init>();	 Catch:{ Exception -> 0x016d }
        r10 = r13.currentAccount;	 Catch:{ Exception -> 0x016d }
        r10 = org.telegram.messenger.MessagesStorage.getInstance(r10);	 Catch:{ Exception -> 0x016d }
        r4 = android.text.TextUtils.join(r9, r4);	 Catch:{ Exception -> 0x016d }
        r10.getEncryptedChatsInternal(r4, r7, r1);	 Catch:{ Exception -> 0x016d }
        r4 = 0;
    L_0x00c8:
        r10 = r7.size();	 Catch:{ Exception -> 0x016d }
        if (r4 >= r10) goto L_0x00e9;
    L_0x00ce:
        r10 = r7.get(r4);	 Catch:{ Exception -> 0x016d }
        r10 = (org.telegram.tgnet.TLRPC.EncryptedChat) r10;	 Catch:{ Exception -> 0x016d }
        r10 = r10.id;	 Catch:{ Exception -> 0x016d }
        r10 = (long) r10;	 Catch:{ Exception -> 0x016d }
        r10 = r10 << r8;
        r10 = r6.get(r10);	 Catch:{ Exception -> 0x016d }
        r10 = (org.telegram.ui.Adapters.DialogsSearchAdapter.RecentSearchObject) r10;	 Catch:{ Exception -> 0x016d }
        r11 = r7.get(r4);	 Catch:{ Exception -> 0x016d }
        r11 = (org.telegram.tgnet.TLObject) r11;	 Catch:{ Exception -> 0x016d }
        r10.object = r11;	 Catch:{ Exception -> 0x016d }
        r4 = r4 + 1;
        goto L_0x00c8;
    L_0x00e9:
        r4 = r3.isEmpty();	 Catch:{ Exception -> 0x016d }
        if (r4 != 0) goto L_0x0130;
    L_0x00ef:
        r4 = new java.util.ArrayList;	 Catch:{ Exception -> 0x016d }
        r4.<init>();	 Catch:{ Exception -> 0x016d }
        r7 = r13.currentAccount;	 Catch:{ Exception -> 0x016d }
        r7 = org.telegram.messenger.MessagesStorage.getInstance(r7);	 Catch:{ Exception -> 0x016d }
        r3 = android.text.TextUtils.join(r9, r3);	 Catch:{ Exception -> 0x016d }
        r7.getChatsInternal(r3, r4);	 Catch:{ Exception -> 0x016d }
        r3 = 0;
    L_0x0102:
        r7 = r4.size();	 Catch:{ Exception -> 0x016d }
        if (r3 >= r7) goto L_0x0130;
    L_0x0108:
        r7 = r4.get(r3);	 Catch:{ Exception -> 0x016d }
        r7 = (org.telegram.tgnet.TLRPC.Chat) r7;	 Catch:{ Exception -> 0x016d }
        r8 = r7.id;	 Catch:{ Exception -> 0x016d }
        r8 = -r8;
        r10 = (long) r8;	 Catch:{ Exception -> 0x016d }
        r8 = r7.migrated_to;	 Catch:{ Exception -> 0x016d }
        if (r8 == 0) goto L_0x0125;
    L_0x0116:
        r7 = r6.get(r10);	 Catch:{ Exception -> 0x016d }
        r7 = (org.telegram.ui.Adapters.DialogsSearchAdapter.RecentSearchObject) r7;	 Catch:{ Exception -> 0x016d }
        r6.remove(r10);	 Catch:{ Exception -> 0x016d }
        if (r7 == 0) goto L_0x012d;
    L_0x0121:
        r5.remove(r7);	 Catch:{ Exception -> 0x016d }
        goto L_0x012d;
    L_0x0125:
        r8 = r6.get(r10);	 Catch:{ Exception -> 0x016d }
        r8 = (org.telegram.ui.Adapters.DialogsSearchAdapter.RecentSearchObject) r8;	 Catch:{ Exception -> 0x016d }
        r8.object = r7;	 Catch:{ Exception -> 0x016d }
    L_0x012d:
        r3 = r3 + 1;
        goto L_0x0102;
    L_0x0130:
        r3 = r1.isEmpty();	 Catch:{ Exception -> 0x016d }
        if (r3 != 0) goto L_0x015f;
    L_0x0136:
        r3 = r13.currentAccount;	 Catch:{ Exception -> 0x016d }
        r3 = org.telegram.messenger.MessagesStorage.getInstance(r3);	 Catch:{ Exception -> 0x016d }
        r1 = android.text.TextUtils.join(r9, r1);	 Catch:{ Exception -> 0x016d }
        r3.getUsersInternal(r1, r0);	 Catch:{ Exception -> 0x016d }
    L_0x0143:
        r1 = r0.size();	 Catch:{ Exception -> 0x016d }
        if (r2 >= r1) goto L_0x015f;
    L_0x0149:
        r1 = r0.get(r2);	 Catch:{ Exception -> 0x016d }
        r1 = (org.telegram.tgnet.TLRPC.User) r1;	 Catch:{ Exception -> 0x016d }
        r3 = r1.id;	 Catch:{ Exception -> 0x016d }
        r3 = (long) r3;	 Catch:{ Exception -> 0x016d }
        r3 = r6.get(r3);	 Catch:{ Exception -> 0x016d }
        r3 = (org.telegram.ui.Adapters.DialogsSearchAdapter.RecentSearchObject) r3;	 Catch:{ Exception -> 0x016d }
        if (r3 == 0) goto L_0x015c;
    L_0x015a:
        r3.object = r1;	 Catch:{ Exception -> 0x016d }
    L_0x015c:
        r2 = r2 + 1;
        goto L_0x0143;
    L_0x015f:
        r0 = org.telegram.ui.Adapters.-$$Lambda$DialogsSearchAdapter$TgrSEhniISqCg6ct5i9NTHhT7C8.INSTANCE;	 Catch:{ Exception -> 0x016d }
        java.util.Collections.sort(r5, r0);	 Catch:{ Exception -> 0x016d }
        r0 = new org.telegram.ui.Adapters.-$$Lambda$DialogsSearchAdapter$YAAaRoGgRkDmshNt90P0fNwfz-U;	 Catch:{ Exception -> 0x016d }
        r0.<init>(r13, r5, r6);	 Catch:{ Exception -> 0x016d }
        org.telegram.messenger.AndroidUtilities.runOnUIThread(r0);	 Catch:{ Exception -> 0x016d }
        goto L_0x0171;
    L_0x016d:
        r0 = move-exception;
        org.telegram.messenger.FileLog.e(r0);
    L_0x0171:
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.Adapters.DialogsSearchAdapter.lambda$loadRecentSearch$4$DialogsSearchAdapter():void");
    }

    static /* synthetic */ int lambda$null$2(RecentSearchObject recentSearchObject, RecentSearchObject recentSearchObject2) {
        int i = recentSearchObject.date;
        int i2 = recentSearchObject2.date;
        if (i < i2) {
            return 1;
        }
        return i > i2 ? -1 : 0;
    }

    public /* synthetic */ void lambda$null$3$DialogsSearchAdapter(ArrayList arrayList, LongSparseArray longSparseArray) {
        setRecentSearch(arrayList, longSparseArray);
    }

    public void putRecentSearch(long j, TLObject tLObject) {
        Object obj = (RecentSearchObject) this.recentSearchObjectsById.get(j);
        if (obj == null) {
            obj = new RecentSearchObject();
            this.recentSearchObjectsById.put(j, obj);
        } else {
            this.recentSearchObjects.remove(obj);
        }
        this.recentSearchObjects.add(0, obj);
        obj.did = j;
        obj.object = tLObject;
        obj.date = (int) (System.currentTimeMillis() / 1000);
        notifyDataSetChanged();
        MessagesStorage.getInstance(this.currentAccount).getStorageQueue().postRunnable(new -$$Lambda$DialogsSearchAdapter$EHNVdrp_nz-CUR77EsM44jqsFBg(this, j));
    }

    public /* synthetic */ void lambda$putRecentSearch$5$DialogsSearchAdapter(long j) {
        try {
            SQLitePreparedStatement executeFast = MessagesStorage.getInstance(this.currentAccount).getDatabase().executeFast("REPLACE INTO search_recent VALUES(?, ?)");
            executeFast.requery();
            executeFast.bindLong(1, j);
            executeFast.bindInteger(2, (int) (System.currentTimeMillis() / 1000));
            executeFast.step();
            executeFast.dispose();
        } catch (Exception e) {
            FileLog.e(e);
        }
    }

    public void clearRecentSearch() {
        this.recentSearchObjectsById = new LongSparseArray();
        this.recentSearchObjects = new ArrayList();
        notifyDataSetChanged();
        MessagesStorage.getInstance(this.currentAccount).getStorageQueue().postRunnable(new -$$Lambda$DialogsSearchAdapter$It_IWmufaVpNeuW4BM4G8iqCxcU(this));
    }

    public /* synthetic */ void lambda$clearRecentSearch$6$DialogsSearchAdapter() {
        try {
            MessagesStorage.getInstance(this.currentAccount).getDatabase().executeFast("DELETE FROM search_recent WHERE 1").stepThis().dispose();
        } catch (Exception e) {
            FileLog.e(e);
        }
    }

    public void addHashtagsFromMessage(CharSequence charSequence) {
        this.searchAdapterHelper.addHashtagsFromMessage(charSequence);
    }

    private void setRecentSearch(ArrayList<RecentSearchObject> arrayList, LongSparseArray<RecentSearchObject> longSparseArray) {
        this.recentSearchObjects = arrayList;
        this.recentSearchObjectsById = longSparseArray;
        for (int i = 0; i < this.recentSearchObjects.size(); i++) {
            RecentSearchObject recentSearchObject = (RecentSearchObject) this.recentSearchObjects.get(i);
            TLObject tLObject = recentSearchObject.object;
            if (tLObject instanceof User) {
                MessagesController.getInstance(this.currentAccount).putUser((User) recentSearchObject.object, true);
            } else if (tLObject instanceof Chat) {
                MessagesController.getInstance(this.currentAccount).putChat((Chat) recentSearchObject.object, true);
            } else if (tLObject instanceof EncryptedChat) {
                MessagesController.getInstance(this.currentAccount).putEncryptedChat((EncryptedChat) recentSearchObject.object, true);
            }
        }
        notifyDataSetChanged();
    }

    private void searchDialogsInternal(String str, int i) {
        if (this.needMessagesSearch != 2) {
            MessagesStorage.getInstance(this.currentAccount).getStorageQueue().postRunnable(new -$$Lambda$DialogsSearchAdapter$jlz_Jg93hVH6t3KHP9A-P2d1MzU(this, str, i));
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:96:0x024a A:{LOOP_END, Catch:{ Exception -> 0x0692 }, LOOP:2: B:66:0x018c->B:96:0x024a} */
    /* JADX WARNING: Removed duplicated region for block: B:273:0x01df A:{SYNTHETIC} */
    /* JADX WARNING: Removed duplicated region for block: B:186:0x04fa A:{LOOP_END, Catch:{ Exception -> 0x0692 }, LOOP:6: B:147:0x0384->B:186:0x04fa} */
    /* JADX WARNING: Removed duplicated region for block: B:292:0x03cf A:{SYNTHETIC} */
    /* JADX WARNING: Removed duplicated region for block: B:249:0x066b A:{LOOP_END, Catch:{ Exception -> 0x0692 }, LOOP:10: B:220:0x05bd->B:249:0x066b} */
    /* JADX WARNING: Removed duplicated region for block: B:302:0x060e A:{SYNTHETIC} */
    public /* synthetic */ void lambda$searchDialogsInternal$8$DialogsSearchAdapter(java.lang.String r25, int r26) {
        /*
        r24 = this;
        r1 = r24;
        r0 = "SavedMessages";
        r2 = NUM; // 0x7f0e0945 float:1.887985E38 double:1.053163329E-314;
        r0 = org.telegram.messenger.LocaleController.getString(r0, r2);	 Catch:{ Exception -> 0x0692 }
        r0 = r0.toLowerCase();	 Catch:{ Exception -> 0x0692 }
        r2 = r25.trim();	 Catch:{ Exception -> 0x0692 }
        r2 = r2.toLowerCase();	 Catch:{ Exception -> 0x0692 }
        r3 = r2.length();	 Catch:{ Exception -> 0x0692 }
        r4 = -1;
        if (r3 != 0) goto L_0x0035;
    L_0x001e:
        r1.lastSearchId = r4;	 Catch:{ Exception -> 0x0692 }
        r0 = new java.util.ArrayList;	 Catch:{ Exception -> 0x0692 }
        r0.<init>();	 Catch:{ Exception -> 0x0692 }
        r2 = new java.util.ArrayList;	 Catch:{ Exception -> 0x0692 }
        r2.<init>();	 Catch:{ Exception -> 0x0692 }
        r3 = new java.util.ArrayList;	 Catch:{ Exception -> 0x0692 }
        r3.<init>();	 Catch:{ Exception -> 0x0692 }
        r4 = r1.lastSearchId;	 Catch:{ Exception -> 0x0692 }
        r1.updateSearchResults(r0, r2, r3, r4);	 Catch:{ Exception -> 0x0692 }
        return;
    L_0x0035:
        r3 = org.telegram.messenger.LocaleController.getInstance();	 Catch:{ Exception -> 0x0692 }
        r3 = r3.getTranslitString(r2);	 Catch:{ Exception -> 0x0692 }
        r5 = r2.equals(r3);	 Catch:{ Exception -> 0x0692 }
        r6 = 0;
        if (r5 != 0) goto L_0x004a;
    L_0x0044:
        r5 = r3.length();	 Catch:{ Exception -> 0x0692 }
        if (r5 != 0) goto L_0x004b;
    L_0x004a:
        r3 = r6;
    L_0x004b:
        r5 = 1;
        r7 = 0;
        if (r3 == 0) goto L_0x0051;
    L_0x004f:
        r8 = 1;
        goto L_0x0052;
    L_0x0051:
        r8 = 0;
    L_0x0052:
        r8 = r8 + r5;
        r8 = new java.lang.String[r8];	 Catch:{ Exception -> 0x0692 }
        r8[r7] = r2;	 Catch:{ Exception -> 0x0692 }
        if (r3 == 0) goto L_0x005b;
    L_0x0059:
        r8[r5] = r3;	 Catch:{ Exception -> 0x0692 }
    L_0x005b:
        r3 = new java.util.ArrayList;	 Catch:{ Exception -> 0x0692 }
        r3.<init>();	 Catch:{ Exception -> 0x0692 }
        r9 = new java.util.ArrayList;	 Catch:{ Exception -> 0x0692 }
        r9.<init>();	 Catch:{ Exception -> 0x0692 }
        r10 = new java.util.ArrayList;	 Catch:{ Exception -> 0x0692 }
        r10.<init>();	 Catch:{ Exception -> 0x0692 }
        r11 = new java.util.ArrayList;	 Catch:{ Exception -> 0x0692 }
        r11.<init>();	 Catch:{ Exception -> 0x0692 }
        r12 = new android.util.LongSparseArray;	 Catch:{ Exception -> 0x0692 }
        r12.<init>();	 Catch:{ Exception -> 0x0692 }
        r13 = r1.currentAccount;	 Catch:{ Exception -> 0x0692 }
        r13 = org.telegram.messenger.MessagesStorage.getInstance(r13);	 Catch:{ Exception -> 0x0692 }
        r13 = r13.getDatabase();	 Catch:{ Exception -> 0x0692 }
        r14 = "SELECT did, date FROM dialogs ORDER BY date DESC LIMIT 600";
        r15 = new java.lang.Object[r7];	 Catch:{ Exception -> 0x0692 }
        r13 = r13.queryFinalized(r14, r15);	 Catch:{ Exception -> 0x0692 }
    L_0x0086:
        r14 = r13.next();	 Catch:{ Exception -> 0x0692 }
        r15 = 32;
        if (r14 == 0) goto L_0x0101;
    L_0x008e:
        r4 = r13.longValue(r7);	 Catch:{ Exception -> 0x0692 }
        r14 = new org.telegram.ui.Adapters.DialogsSearchAdapter$DialogSearchResult;	 Catch:{ Exception -> 0x0692 }
        r14.<init>(r1, r6);	 Catch:{ Exception -> 0x0692 }
        r7 = 1;
        r6 = r13.intValue(r7);	 Catch:{ Exception -> 0x0692 }
        r14.date = r6;	 Catch:{ Exception -> 0x0692 }
        r12.put(r4, r14);	 Catch:{ Exception -> 0x0692 }
        r6 = (int) r4;	 Catch:{ Exception -> 0x0692 }
        r4 = r4 >> r15;
        r5 = (int) r4;	 Catch:{ Exception -> 0x0692 }
        if (r6 == 0) goto L_0x00e6;
    L_0x00a6:
        if (r6 <= 0) goto L_0x00c9;
    L_0x00a8:
        r4 = r1.dialogsType;	 Catch:{ Exception -> 0x0692 }
        r5 = 4;
        if (r4 != r5) goto L_0x00b2;
    L_0x00ad:
        r4 = r1.selfUserId;	 Catch:{ Exception -> 0x0692 }
        if (r6 != r4) goto L_0x00b2;
    L_0x00b1:
        goto L_0x00ce;
    L_0x00b2:
        r4 = r1.dialogsType;	 Catch:{ Exception -> 0x0692 }
        r5 = 2;
        if (r4 == r5) goto L_0x00ce;
    L_0x00b7:
        r4 = java.lang.Integer.valueOf(r6);	 Catch:{ Exception -> 0x0692 }
        r4 = r3.contains(r4);	 Catch:{ Exception -> 0x0692 }
        if (r4 != 0) goto L_0x00ce;
    L_0x00c1:
        r4 = java.lang.Integer.valueOf(r6);	 Catch:{ Exception -> 0x0692 }
        r3.add(r4);	 Catch:{ Exception -> 0x0692 }
        goto L_0x00ce;
    L_0x00c9:
        r4 = r1.dialogsType;	 Catch:{ Exception -> 0x0692 }
        r5 = 4;
        if (r4 != r5) goto L_0x00d3;
    L_0x00ce:
        r4 = -1;
        r5 = 1;
        r6 = 0;
        r7 = 0;
        goto L_0x0086;
    L_0x00d3:
        r4 = -r6;
        r5 = java.lang.Integer.valueOf(r4);	 Catch:{ Exception -> 0x0692 }
        r5 = r9.contains(r5);	 Catch:{ Exception -> 0x0692 }
        if (r5 != 0) goto L_0x00ce;
    L_0x00de:
        r4 = java.lang.Integer.valueOf(r4);	 Catch:{ Exception -> 0x0692 }
        r9.add(r4);	 Catch:{ Exception -> 0x0692 }
        goto L_0x00ce;
    L_0x00e6:
        r4 = r1.dialogsType;	 Catch:{ Exception -> 0x0692 }
        if (r4 == 0) goto L_0x00ef;
    L_0x00ea:
        r4 = r1.dialogsType;	 Catch:{ Exception -> 0x0692 }
        r6 = 3;
        if (r4 != r6) goto L_0x00ce;
    L_0x00ef:
        r4 = java.lang.Integer.valueOf(r5);	 Catch:{ Exception -> 0x0692 }
        r4 = r10.contains(r4);	 Catch:{ Exception -> 0x0692 }
        if (r4 != 0) goto L_0x00ce;
    L_0x00f9:
        r4 = java.lang.Integer.valueOf(r5);	 Catch:{ Exception -> 0x0692 }
        r10.add(r4);	 Catch:{ Exception -> 0x0692 }
        goto L_0x00ce;
    L_0x0101:
        r13.dispose();	 Catch:{ Exception -> 0x0692 }
        r2 = r0.startsWith(r2);	 Catch:{ Exception -> 0x0692 }
        if (r2 == 0) goto L_0x012b;
    L_0x010a:
        r2 = r1.currentAccount;	 Catch:{ Exception -> 0x0692 }
        r2 = org.telegram.messenger.UserConfig.getInstance(r2);	 Catch:{ Exception -> 0x0692 }
        r2 = r2.getCurrentUser();	 Catch:{ Exception -> 0x0692 }
        r4 = new org.telegram.ui.Adapters.DialogsSearchAdapter$DialogSearchResult;	 Catch:{ Exception -> 0x0692 }
        r5 = 0;
        r4.<init>(r1, r5);	 Catch:{ Exception -> 0x0692 }
        r5 = NUM; // 0x7fffffff float:NaN double:1.060997895E-314;
        r4.date = r5;	 Catch:{ Exception -> 0x0692 }
        r4.name = r0;	 Catch:{ Exception -> 0x0692 }
        r4.object = r2;	 Catch:{ Exception -> 0x0692 }
        r0 = r2.id;	 Catch:{ Exception -> 0x0692 }
        r5 = (long) r0;	 Catch:{ Exception -> 0x0692 }
        r12.put(r5, r4);	 Catch:{ Exception -> 0x0692 }
        r0 = 1;
        goto L_0x012c;
    L_0x012b:
        r0 = 0;
    L_0x012c:
        r2 = r3.isEmpty();	 Catch:{ Exception -> 0x0692 }
        r4 = ";;;";
        r5 = ",";
        r6 = "@";
        r13 = " ";
        if (r2 != 0) goto L_0x0269;
    L_0x013a:
        r2 = r1.currentAccount;	 Catch:{ Exception -> 0x0692 }
        r2 = org.telegram.messenger.MessagesStorage.getInstance(r2);	 Catch:{ Exception -> 0x0692 }
        r2 = r2.getDatabase();	 Catch:{ Exception -> 0x0692 }
        r14 = java.util.Locale.US;	 Catch:{ Exception -> 0x0692 }
        r7 = "SELECT data, status, name FROM users WHERE uid IN(%s)";
        r18 = r0;
        r15 = 1;
        r0 = new java.lang.Object[r15];	 Catch:{ Exception -> 0x0692 }
        r15 = r14;
        r3 = android.text.TextUtils.join(r5, r3);	 Catch:{ Exception -> 0x0692 }
        r14 = 0;
        r0[r14] = r3;	 Catch:{ Exception -> 0x0692 }
        r0 = java.lang.String.format(r15, r7, r0);	 Catch:{ Exception -> 0x0692 }
        r3 = new java.lang.Object[r14];	 Catch:{ Exception -> 0x0692 }
        r0 = r2.queryFinalized(r0, r3);	 Catch:{ Exception -> 0x0692 }
    L_0x015f:
        r2 = r0.next();	 Catch:{ Exception -> 0x0692 }
        if (r2 == 0) goto L_0x0263;
    L_0x0165:
        r2 = 2;
        r3 = r0.stringValue(r2);	 Catch:{ Exception -> 0x0692 }
        r2 = org.telegram.messenger.LocaleController.getInstance();	 Catch:{ Exception -> 0x0692 }
        r2 = r2.getTranslitString(r3);	 Catch:{ Exception -> 0x0692 }
        r7 = r3.equals(r2);	 Catch:{ Exception -> 0x0692 }
        if (r7 == 0) goto L_0x0179;
    L_0x0178:
        r2 = 0;
    L_0x0179:
        r7 = r3.lastIndexOf(r4);	 Catch:{ Exception -> 0x0692 }
        r14 = -1;
        if (r7 == r14) goto L_0x0187;
    L_0x0180:
        r7 = r7 + 3;
        r7 = r3.substring(r7);	 Catch:{ Exception -> 0x0692 }
        goto L_0x0188;
    L_0x0187:
        r7 = 0;
    L_0x0188:
        r15 = r8.length;	 Catch:{ Exception -> 0x0692 }
        r14 = 0;
        r20 = 0;
    L_0x018c:
        if (r14 >= r15) goto L_0x025d;
    L_0x018e:
        r21 = r15;
        r15 = r8[r14];	 Catch:{ Exception -> 0x0692 }
        r22 = r3.startsWith(r15);	 Catch:{ Exception -> 0x0692 }
        if (r22 != 0) goto L_0x01da;
    L_0x0198:
        r22 = r14;
        r14 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x0692 }
        r14.<init>();	 Catch:{ Exception -> 0x0692 }
        r14.append(r13);	 Catch:{ Exception -> 0x0692 }
        r14.append(r15);	 Catch:{ Exception -> 0x0692 }
        r14 = r14.toString();	 Catch:{ Exception -> 0x0692 }
        r14 = r3.contains(r14);	 Catch:{ Exception -> 0x0692 }
        if (r14 != 0) goto L_0x01dc;
    L_0x01af:
        if (r2 == 0) goto L_0x01cd;
    L_0x01b1:
        r14 = r2.startsWith(r15);	 Catch:{ Exception -> 0x0692 }
        if (r14 != 0) goto L_0x01dc;
    L_0x01b7:
        r14 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x0692 }
        r14.<init>();	 Catch:{ Exception -> 0x0692 }
        r14.append(r13);	 Catch:{ Exception -> 0x0692 }
        r14.append(r15);	 Catch:{ Exception -> 0x0692 }
        r14 = r14.toString();	 Catch:{ Exception -> 0x0692 }
        r14 = r2.contains(r14);	 Catch:{ Exception -> 0x0692 }
        if (r14 == 0) goto L_0x01cd;
    L_0x01cc:
        goto L_0x01dc;
    L_0x01cd:
        if (r7 == 0) goto L_0x01d7;
    L_0x01cf:
        r14 = r7.startsWith(r15);	 Catch:{ Exception -> 0x0692 }
        if (r14 == 0) goto L_0x01d7;
    L_0x01d5:
        r14 = 2;
        goto L_0x01dd;
    L_0x01d7:
        r14 = r20;
        goto L_0x01dd;
    L_0x01da:
        r22 = r14;
    L_0x01dc:
        r14 = 1;
    L_0x01dd:
        if (r14 == 0) goto L_0x024a;
    L_0x01df:
        r2 = 0;
        r3 = r0.byteBufferValue(r2);	 Catch:{ Exception -> 0x0692 }
        if (r3 == 0) goto L_0x025d;
    L_0x01e6:
        r7 = r3.readInt32(r2);	 Catch:{ Exception -> 0x0692 }
        r7 = org.telegram.tgnet.TLRPC.User.TLdeserialize(r3, r7, r2);	 Catch:{ Exception -> 0x0692 }
        r3.reuse();	 Catch:{ Exception -> 0x0692 }
        r2 = r7.id;	 Catch:{ Exception -> 0x0692 }
        r2 = (long) r2;	 Catch:{ Exception -> 0x0692 }
        r2 = r12.get(r2);	 Catch:{ Exception -> 0x0692 }
        r2 = (org.telegram.ui.Adapters.DialogsSearchAdapter.DialogSearchResult) r2;	 Catch:{ Exception -> 0x0692 }
        r3 = r7.status;	 Catch:{ Exception -> 0x0692 }
        if (r3 == 0) goto L_0x020e;
    L_0x01fe:
        r3 = r7.status;	 Catch:{ Exception -> 0x0692 }
        r19 = r11;
        r22 = r14;
        r14 = 1;
        r11 = r0.intValue(r14);	 Catch:{ Exception -> 0x0692 }
        r14 = r22;
        r3.expires = r11;	 Catch:{ Exception -> 0x0692 }
        goto L_0x0210;
    L_0x020e:
        r19 = r11;
    L_0x0210:
        r3 = 1;
        if (r14 != r3) goto L_0x021e;
    L_0x0213:
        r3 = r7.first_name;	 Catch:{ Exception -> 0x0692 }
        r14 = r7.last_name;	 Catch:{ Exception -> 0x0692 }
        r3 = org.telegram.messenger.AndroidUtilities.generateSearchName(r3, r14, r15);	 Catch:{ Exception -> 0x0692 }
        r2.name = r3;	 Catch:{ Exception -> 0x0692 }
        goto L_0x0245;
    L_0x021e:
        r3 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x0692 }
        r3.<init>();	 Catch:{ Exception -> 0x0692 }
        r3.append(r6);	 Catch:{ Exception -> 0x0692 }
        r14 = r7.username;	 Catch:{ Exception -> 0x0692 }
        r3.append(r14);	 Catch:{ Exception -> 0x0692 }
        r3 = r3.toString();	 Catch:{ Exception -> 0x0692 }
        r14 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x0692 }
        r14.<init>();	 Catch:{ Exception -> 0x0692 }
        r14.append(r6);	 Catch:{ Exception -> 0x0692 }
        r14.append(r15);	 Catch:{ Exception -> 0x0692 }
        r14 = r14.toString();	 Catch:{ Exception -> 0x0692 }
        r15 = 0;
        r3 = org.telegram.messenger.AndroidUtilities.generateSearchName(r3, r15, r14);	 Catch:{ Exception -> 0x0692 }
        r2.name = r3;	 Catch:{ Exception -> 0x0692 }
    L_0x0245:
        r2.object = r7;	 Catch:{ Exception -> 0x0692 }
        r18 = r18 + 1;
        goto L_0x025f;
    L_0x024a:
        r20 = r2;
        r19 = r11;
        r2 = r22;
        r2 = r2 + 1;
        r15 = r21;
        r23 = r14;
        r14 = r2;
        r2 = r20;
        r20 = r23;
        goto L_0x018c;
    L_0x025d:
        r19 = r11;
    L_0x025f:
        r11 = r19;
        goto L_0x015f;
    L_0x0263:
        r19 = r11;
        r0.dispose();	 Catch:{ Exception -> 0x0692 }
        goto L_0x026d;
    L_0x0269:
        r18 = r0;
        r19 = r11;
    L_0x026d:
        r0 = r9.isEmpty();	 Catch:{ Exception -> 0x0692 }
        if (r0 != 0) goto L_0x0331;
    L_0x0273:
        r0 = r1.currentAccount;	 Catch:{ Exception -> 0x0692 }
        r0 = org.telegram.messenger.MessagesStorage.getInstance(r0);	 Catch:{ Exception -> 0x0692 }
        r0 = r0.getDatabase();	 Catch:{ Exception -> 0x0692 }
        r2 = java.util.Locale.US;	 Catch:{ Exception -> 0x0692 }
        r3 = "SELECT data, name FROM chats WHERE uid IN(%s)";
        r7 = 1;
        r11 = new java.lang.Object[r7];	 Catch:{ Exception -> 0x0692 }
        r7 = android.text.TextUtils.join(r5, r9);	 Catch:{ Exception -> 0x0692 }
        r9 = 0;
        r11[r9] = r7;	 Catch:{ Exception -> 0x0692 }
        r2 = java.lang.String.format(r2, r3, r11);	 Catch:{ Exception -> 0x0692 }
        r3 = new java.lang.Object[r9];	 Catch:{ Exception -> 0x0692 }
        r0 = r0.queryFinalized(r2, r3);	 Catch:{ Exception -> 0x0692 }
    L_0x0295:
        r2 = r0.next();	 Catch:{ Exception -> 0x0692 }
        if (r2 == 0) goto L_0x032e;
    L_0x029b:
        r2 = 1;
        r3 = r0.stringValue(r2);	 Catch:{ Exception -> 0x0692 }
        r2 = org.telegram.messenger.LocaleController.getInstance();	 Catch:{ Exception -> 0x0692 }
        r2 = r2.getTranslitString(r3);	 Catch:{ Exception -> 0x0692 }
        r7 = r3.equals(r2);	 Catch:{ Exception -> 0x0692 }
        if (r7 == 0) goto L_0x02af;
    L_0x02ae:
        r2 = 0;
    L_0x02af:
        r7 = r8.length;	 Catch:{ Exception -> 0x0692 }
        r9 = 0;
    L_0x02b1:
        if (r9 >= r7) goto L_0x0295;
    L_0x02b3:
        r11 = r8[r9];	 Catch:{ Exception -> 0x0692 }
        r15 = r3.startsWith(r11);	 Catch:{ Exception -> 0x0692 }
        if (r15 != 0) goto L_0x02f1;
    L_0x02bb:
        r15 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x0692 }
        r15.<init>();	 Catch:{ Exception -> 0x0692 }
        r15.append(r13);	 Catch:{ Exception -> 0x0692 }
        r15.append(r11);	 Catch:{ Exception -> 0x0692 }
        r15 = r15.toString();	 Catch:{ Exception -> 0x0692 }
        r15 = r3.contains(r15);	 Catch:{ Exception -> 0x0692 }
        if (r15 != 0) goto L_0x02f1;
    L_0x02d0:
        if (r2 == 0) goto L_0x02ee;
    L_0x02d2:
        r15 = r2.startsWith(r11);	 Catch:{ Exception -> 0x0692 }
        if (r15 != 0) goto L_0x02f1;
    L_0x02d8:
        r15 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x0692 }
        r15.<init>();	 Catch:{ Exception -> 0x0692 }
        r15.append(r13);	 Catch:{ Exception -> 0x0692 }
        r15.append(r11);	 Catch:{ Exception -> 0x0692 }
        r15 = r15.toString();	 Catch:{ Exception -> 0x0692 }
        r15 = r2.contains(r15);	 Catch:{ Exception -> 0x0692 }
        if (r15 == 0) goto L_0x02ee;
    L_0x02ed:
        goto L_0x02f1;
    L_0x02ee:
        r9 = r9 + 1;
        goto L_0x02b1;
    L_0x02f1:
        r2 = 0;
        r3 = r0.byteBufferValue(r2);	 Catch:{ Exception -> 0x0692 }
        if (r3 == 0) goto L_0x0295;
    L_0x02f8:
        r7 = r3.readInt32(r2);	 Catch:{ Exception -> 0x0692 }
        r7 = org.telegram.tgnet.TLRPC.Chat.TLdeserialize(r3, r7, r2);	 Catch:{ Exception -> 0x0692 }
        r3.reuse();	 Catch:{ Exception -> 0x0692 }
        if (r7 == 0) goto L_0x0295;
    L_0x0305:
        r2 = r7.deactivated;	 Catch:{ Exception -> 0x0692 }
        if (r2 != 0) goto L_0x0295;
    L_0x0309:
        r2 = org.telegram.messenger.ChatObject.isChannel(r7);	 Catch:{ Exception -> 0x0692 }
        if (r2 == 0) goto L_0x0315;
    L_0x030f:
        r2 = org.telegram.messenger.ChatObject.isNotInChat(r7);	 Catch:{ Exception -> 0x0692 }
        if (r2 != 0) goto L_0x0295;
    L_0x0315:
        r2 = r7.id;	 Catch:{ Exception -> 0x0692 }
        r2 = -r2;
        r2 = (long) r2;	 Catch:{ Exception -> 0x0692 }
        r2 = r12.get(r2);	 Catch:{ Exception -> 0x0692 }
        r2 = (org.telegram.ui.Adapters.DialogsSearchAdapter.DialogSearchResult) r2;	 Catch:{ Exception -> 0x0692 }
        r3 = r7.title;	 Catch:{ Exception -> 0x0692 }
        r9 = 0;
        r3 = org.telegram.messenger.AndroidUtilities.generateSearchName(r3, r9, r11);	 Catch:{ Exception -> 0x0692 }
        r2.name = r3;	 Catch:{ Exception -> 0x0692 }
        r2.object = r7;	 Catch:{ Exception -> 0x0692 }
        r18 = r18 + 1;
        goto L_0x0295;
    L_0x032e:
        r0.dispose();	 Catch:{ Exception -> 0x0692 }
    L_0x0331:
        r0 = r10.isEmpty();	 Catch:{ Exception -> 0x0692 }
        if (r0 != 0) goto L_0x0516;
    L_0x0337:
        r0 = r1.currentAccount;	 Catch:{ Exception -> 0x0692 }
        r0 = org.telegram.messenger.MessagesStorage.getInstance(r0);	 Catch:{ Exception -> 0x0692 }
        r0 = r0.getDatabase();	 Catch:{ Exception -> 0x0692 }
        r2 = java.util.Locale.US;	 Catch:{ Exception -> 0x0692 }
        r3 = "SELECT q.data, u.name, q.user, q.g, q.authkey, q.ttl, u.data, u.status, q.layer, q.seq_in, q.seq_out, q.use_count, q.exchange_id, q.key_date, q.fprint, q.fauthkey, q.khash, q.in_seq_no, q.admin_id, q.mtproto_seq FROM enc_chats as q INNER JOIN users as u ON q.user = u.uid WHERE q.uid IN(%s)";
        r7 = 1;
        r9 = new java.lang.Object[r7];	 Catch:{ Exception -> 0x0692 }
        r5 = android.text.TextUtils.join(r5, r10);	 Catch:{ Exception -> 0x0692 }
        r7 = 0;
        r9[r7] = r5;	 Catch:{ Exception -> 0x0692 }
        r2 = java.lang.String.format(r2, r3, r9);	 Catch:{ Exception -> 0x0692 }
        r3 = new java.lang.Object[r7];	 Catch:{ Exception -> 0x0692 }
        r0 = r0.queryFinalized(r2, r3);	 Catch:{ Exception -> 0x0692 }
    L_0x0359:
        r2 = r0.next();	 Catch:{ Exception -> 0x0692 }
        if (r2 == 0) goto L_0x050e;
    L_0x035f:
        r2 = 1;
        r3 = r0.stringValue(r2);	 Catch:{ Exception -> 0x0692 }
        r2 = org.telegram.messenger.LocaleController.getInstance();	 Catch:{ Exception -> 0x0692 }
        r2 = r2.getTranslitString(r3);	 Catch:{ Exception -> 0x0692 }
        r5 = r3.equals(r2);	 Catch:{ Exception -> 0x0692 }
        if (r5 == 0) goto L_0x0373;
    L_0x0372:
        r2 = 0;
    L_0x0373:
        r5 = r3.lastIndexOf(r4);	 Catch:{ Exception -> 0x0692 }
        r7 = -1;
        if (r5 == r7) goto L_0x0381;
    L_0x037a:
        r5 = r5 + 2;
        r5 = r3.substring(r5);	 Catch:{ Exception -> 0x0692 }
        goto L_0x0382;
    L_0x0381:
        r5 = 0;
    L_0x0382:
        r7 = 0;
        r9 = 0;
    L_0x0384:
        r10 = r8.length;	 Catch:{ Exception -> 0x0692 }
        if (r7 >= r10) goto L_0x0503;
    L_0x0387:
        r10 = r8[r7];	 Catch:{ Exception -> 0x0692 }
        r11 = r3.startsWith(r10);	 Catch:{ Exception -> 0x0692 }
        if (r11 != 0) goto L_0x03cc;
    L_0x038f:
        r11 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x0692 }
        r11.<init>();	 Catch:{ Exception -> 0x0692 }
        r11.append(r13);	 Catch:{ Exception -> 0x0692 }
        r11.append(r10);	 Catch:{ Exception -> 0x0692 }
        r11 = r11.toString();	 Catch:{ Exception -> 0x0692 }
        r11 = r3.contains(r11);	 Catch:{ Exception -> 0x0692 }
        if (r11 != 0) goto L_0x03cc;
    L_0x03a4:
        if (r2 == 0) goto L_0x03c2;
    L_0x03a6:
        r11 = r2.startsWith(r10);	 Catch:{ Exception -> 0x0692 }
        if (r11 != 0) goto L_0x03cc;
    L_0x03ac:
        r11 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x0692 }
        r11.<init>();	 Catch:{ Exception -> 0x0692 }
        r11.append(r13);	 Catch:{ Exception -> 0x0692 }
        r11.append(r10);	 Catch:{ Exception -> 0x0692 }
        r11 = r11.toString();	 Catch:{ Exception -> 0x0692 }
        r11 = r2.contains(r11);	 Catch:{ Exception -> 0x0692 }
        if (r11 == 0) goto L_0x03c2;
    L_0x03c1:
        goto L_0x03cc;
    L_0x03c2:
        if (r5 == 0) goto L_0x03cd;
    L_0x03c4:
        r11 = r5.startsWith(r10);	 Catch:{ Exception -> 0x0692 }
        if (r11 == 0) goto L_0x03cd;
    L_0x03ca:
        r9 = 2;
        goto L_0x03cd;
    L_0x03cc:
        r9 = 1;
    L_0x03cd:
        if (r9 == 0) goto L_0x04fa;
    L_0x03cf:
        r11 = 0;
        r2 = r0.byteBufferValue(r11);	 Catch:{ Exception -> 0x0692 }
        if (r2 == 0) goto L_0x03e2;
    L_0x03d6:
        r3 = r2.readInt32(r11);	 Catch:{ Exception -> 0x0692 }
        r3 = org.telegram.tgnet.TLRPC.EncryptedChat.TLdeserialize(r2, r3, r11);	 Catch:{ Exception -> 0x0692 }
        r2.reuse();	 Catch:{ Exception -> 0x0692 }
        goto L_0x03e3;
    L_0x03e2:
        r3 = 0;
    L_0x03e3:
        r2 = 6;
        r2 = r0.byteBufferValue(r2);	 Catch:{ Exception -> 0x0692 }
        if (r2 == 0) goto L_0x03f7;
    L_0x03ea:
        r5 = 0;
        r7 = r2.readInt32(r5);	 Catch:{ Exception -> 0x0692 }
        r7 = org.telegram.tgnet.TLRPC.User.TLdeserialize(r2, r7, r5);	 Catch:{ Exception -> 0x0692 }
        r2.reuse();	 Catch:{ Exception -> 0x0692 }
        goto L_0x03f8;
    L_0x03f7:
        r7 = 0;
    L_0x03f8:
        if (r3 == 0) goto L_0x0503;
    L_0x03fa:
        if (r7 == 0) goto L_0x0503;
    L_0x03fc:
        r2 = r3.id;	 Catch:{ Exception -> 0x0692 }
        r14 = (long) r2;	 Catch:{ Exception -> 0x0692 }
        r11 = 32;
        r14 = r14 << r11;
        r2 = r12.get(r14);	 Catch:{ Exception -> 0x0692 }
        r2 = (org.telegram.ui.Adapters.DialogsSearchAdapter.DialogSearchResult) r2;	 Catch:{ Exception -> 0x0692 }
        r5 = 2;
        r14 = r0.intValue(r5);	 Catch:{ Exception -> 0x0692 }
        r3.user_id = r14;	 Catch:{ Exception -> 0x0692 }
        r5 = 3;
        r14 = r0.byteArrayValue(r5);	 Catch:{ Exception -> 0x0692 }
        r3.a_or_b = r14;	 Catch:{ Exception -> 0x0692 }
        r15 = 4;
        r5 = r0.byteArrayValue(r15);	 Catch:{ Exception -> 0x0692 }
        r3.auth_key = r5;	 Catch:{ Exception -> 0x0692 }
        r5 = 5;
        r5 = r0.intValue(r5);	 Catch:{ Exception -> 0x0692 }
        r3.ttl = r5;	 Catch:{ Exception -> 0x0692 }
        r5 = 8;
        r5 = r0.intValue(r5);	 Catch:{ Exception -> 0x0692 }
        r3.layer = r5;	 Catch:{ Exception -> 0x0692 }
        r5 = 9;
        r5 = r0.intValue(r5);	 Catch:{ Exception -> 0x0692 }
        r3.seq_in = r5;	 Catch:{ Exception -> 0x0692 }
        r5 = 10;
        r5 = r0.intValue(r5);	 Catch:{ Exception -> 0x0692 }
        r3.seq_out = r5;	 Catch:{ Exception -> 0x0692 }
        r5 = 11;
        r5 = r0.intValue(r5);	 Catch:{ Exception -> 0x0692 }
        r14 = r5 >> 16;
        r14 = (short) r14;	 Catch:{ Exception -> 0x0692 }
        r3.key_use_count_in = r14;	 Catch:{ Exception -> 0x0692 }
        r5 = (short) r5;	 Catch:{ Exception -> 0x0692 }
        r3.key_use_count_out = r5;	 Catch:{ Exception -> 0x0692 }
        r5 = 12;
        r16 = r12;
        r11 = r0.longValue(r5);	 Catch:{ Exception -> 0x0692 }
        r3.exchange_id = r11;	 Catch:{ Exception -> 0x0692 }
        r5 = 13;
        r5 = r0.intValue(r5);	 Catch:{ Exception -> 0x0692 }
        r3.key_create_date = r5;	 Catch:{ Exception -> 0x0692 }
        r5 = 14;
        r11 = r0.longValue(r5);	 Catch:{ Exception -> 0x0692 }
        r3.future_key_fingerprint = r11;	 Catch:{ Exception -> 0x0692 }
        r5 = 15;
        r5 = r0.byteArrayValue(r5);	 Catch:{ Exception -> 0x0692 }
        r3.future_auth_key = r5;	 Catch:{ Exception -> 0x0692 }
        r5 = 16;
        r5 = r0.byteArrayValue(r5);	 Catch:{ Exception -> 0x0692 }
        r3.key_hash = r5;	 Catch:{ Exception -> 0x0692 }
        r5 = 17;
        r5 = r0.intValue(r5);	 Catch:{ Exception -> 0x0692 }
        r3.in_seq_no = r5;	 Catch:{ Exception -> 0x0692 }
        r5 = 18;
        r5 = r0.intValue(r5);	 Catch:{ Exception -> 0x0692 }
        if (r5 == 0) goto L_0x0486;
    L_0x0484:
        r3.admin_id = r5;	 Catch:{ Exception -> 0x0692 }
    L_0x0486:
        r5 = 19;
        r5 = r0.intValue(r5);	 Catch:{ Exception -> 0x0692 }
        r3.mtproto_seq = r5;	 Catch:{ Exception -> 0x0692 }
        r5 = r7.status;	 Catch:{ Exception -> 0x0692 }
        if (r5 == 0) goto L_0x049b;
    L_0x0492:
        r5 = r7.status;	 Catch:{ Exception -> 0x0692 }
        r11 = 7;
        r11 = r0.intValue(r11);	 Catch:{ Exception -> 0x0692 }
        r5.expires = r11;	 Catch:{ Exception -> 0x0692 }
    L_0x049b:
        r5 = 1;
        if (r9 != r5) goto L_0x04c9;
    L_0x049e:
        r5 = new android.text.SpannableStringBuilder;	 Catch:{ Exception -> 0x0692 }
        r9 = r7.first_name;	 Catch:{ Exception -> 0x0692 }
        r10 = r7.last_name;	 Catch:{ Exception -> 0x0692 }
        r9 = org.telegram.messenger.ContactsController.formatName(r9, r10);	 Catch:{ Exception -> 0x0692 }
        r5.<init>(r9);	 Catch:{ Exception -> 0x0692 }
        r2.name = r5;	 Catch:{ Exception -> 0x0692 }
        r5 = r2.name;	 Catch:{ Exception -> 0x0692 }
        r5 = (android.text.SpannableStringBuilder) r5;	 Catch:{ Exception -> 0x0692 }
        r9 = new android.text.style.ForegroundColorSpan;	 Catch:{ Exception -> 0x0692 }
        r10 = "chats_secretName";
        r10 = org.telegram.ui.ActionBar.Theme.getColor(r10);	 Catch:{ Exception -> 0x0692 }
        r9.<init>(r10);	 Catch:{ Exception -> 0x0692 }
        r10 = r2.name;	 Catch:{ Exception -> 0x0692 }
        r10 = r10.length();	 Catch:{ Exception -> 0x0692 }
        r11 = 33;
        r12 = 0;
        r5.setSpan(r9, r12, r10, r11);	 Catch:{ Exception -> 0x0692 }
        goto L_0x04f0;
    L_0x04c9:
        r5 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x0692 }
        r5.<init>();	 Catch:{ Exception -> 0x0692 }
        r5.append(r6);	 Catch:{ Exception -> 0x0692 }
        r9 = r7.username;	 Catch:{ Exception -> 0x0692 }
        r5.append(r9);	 Catch:{ Exception -> 0x0692 }
        r5 = r5.toString();	 Catch:{ Exception -> 0x0692 }
        r9 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x0692 }
        r9.<init>();	 Catch:{ Exception -> 0x0692 }
        r9.append(r6);	 Catch:{ Exception -> 0x0692 }
        r9.append(r10);	 Catch:{ Exception -> 0x0692 }
        r9 = r9.toString();	 Catch:{ Exception -> 0x0692 }
        r10 = 0;
        r5 = org.telegram.messenger.AndroidUtilities.generateSearchName(r5, r10, r9);	 Catch:{ Exception -> 0x0692 }
        r2.name = r5;	 Catch:{ Exception -> 0x0692 }
    L_0x04f0:
        r2.object = r3;	 Catch:{ Exception -> 0x0692 }
        r10 = r19;
        r10.add(r7);	 Catch:{ Exception -> 0x0692 }
        r18 = r18 + 1;
        goto L_0x0508;
    L_0x04fa:
        r16 = r12;
        r10 = r19;
        r15 = 4;
        r7 = r7 + 1;
        goto L_0x0384;
    L_0x0503:
        r16 = r12;
        r10 = r19;
        r15 = 4;
    L_0x0508:
        r19 = r10;
        r12 = r16;
        goto L_0x0359;
    L_0x050e:
        r16 = r12;
        r10 = r19;
        r0.dispose();	 Catch:{ Exception -> 0x0692 }
        goto L_0x051a;
    L_0x0516:
        r16 = r12;
        r10 = r19;
    L_0x051a:
        r0 = r18;
        r2 = new java.util.ArrayList;	 Catch:{ Exception -> 0x0692 }
        r2.<init>(r0);	 Catch:{ Exception -> 0x0692 }
        r0 = 0;
    L_0x0522:
        r3 = r16.size();	 Catch:{ Exception -> 0x0692 }
        if (r0 >= r3) goto L_0x0540;
    L_0x0528:
        r3 = r16;
        r5 = r3.valueAt(r0);	 Catch:{ Exception -> 0x0692 }
        r5 = (org.telegram.ui.Adapters.DialogsSearchAdapter.DialogSearchResult) r5;	 Catch:{ Exception -> 0x0692 }
        r7 = r5.object;	 Catch:{ Exception -> 0x0692 }
        if (r7 == 0) goto L_0x053b;
    L_0x0534:
        r7 = r5.name;	 Catch:{ Exception -> 0x0692 }
        if (r7 == 0) goto L_0x053b;
    L_0x0538:
        r2.add(r5);	 Catch:{ Exception -> 0x0692 }
    L_0x053b:
        r0 = r0 + 1;
        r16 = r3;
        goto L_0x0522;
    L_0x0540:
        r3 = r16;
        r0 = org.telegram.ui.Adapters.-$$Lambda$DialogsSearchAdapter$FVguPxj8QpbjyrNjyvgW9r4iI6c.INSTANCE;	 Catch:{ Exception -> 0x0692 }
        java.util.Collections.sort(r2, r0);	 Catch:{ Exception -> 0x0692 }
        r0 = new java.util.ArrayList;	 Catch:{ Exception -> 0x0692 }
        r0.<init>();	 Catch:{ Exception -> 0x0692 }
        r5 = new java.util.ArrayList;	 Catch:{ Exception -> 0x0692 }
        r5.<init>();	 Catch:{ Exception -> 0x0692 }
        r7 = 0;
    L_0x0552:
        r9 = r2.size();	 Catch:{ Exception -> 0x0692 }
        if (r7 >= r9) goto L_0x056b;
    L_0x0558:
        r9 = r2.get(r7);	 Catch:{ Exception -> 0x0692 }
        r9 = (org.telegram.ui.Adapters.DialogsSearchAdapter.DialogSearchResult) r9;	 Catch:{ Exception -> 0x0692 }
        r11 = r9.object;	 Catch:{ Exception -> 0x0692 }
        r0.add(r11);	 Catch:{ Exception -> 0x0692 }
        r9 = r9.name;	 Catch:{ Exception -> 0x0692 }
        r5.add(r9);	 Catch:{ Exception -> 0x0692 }
        r7 = r7 + 1;
        goto L_0x0552;
    L_0x056b:
        r2 = r1.dialogsType;	 Catch:{ Exception -> 0x0692 }
        r7 = 2;
        if (r2 == r7) goto L_0x068c;
    L_0x0570:
        r2 = r1.currentAccount;	 Catch:{ Exception -> 0x0692 }
        r2 = org.telegram.messenger.MessagesStorage.getInstance(r2);	 Catch:{ Exception -> 0x0692 }
        r2 = r2.getDatabase();	 Catch:{ Exception -> 0x0692 }
        r7 = "SELECT u.data, u.status, u.name, u.uid FROM users as u INNER JOIN contacts as c ON u.uid = c.uid";
        r9 = 0;
        r11 = new java.lang.Object[r9];	 Catch:{ Exception -> 0x0692 }
        r2 = r2.queryFinalized(r7, r11);	 Catch:{ Exception -> 0x0692 }
    L_0x0583:
        r7 = r2.next();	 Catch:{ Exception -> 0x0692 }
        if (r7 == 0) goto L_0x0689;
    L_0x0589:
        r7 = 3;
        r9 = r2.intValue(r7);	 Catch:{ Exception -> 0x0692 }
        r11 = (long) r9;	 Catch:{ Exception -> 0x0692 }
        r9 = r3.indexOfKey(r11);	 Catch:{ Exception -> 0x0692 }
        if (r9 < 0) goto L_0x0596;
    L_0x0595:
        goto L_0x0583;
    L_0x0596:
        r9 = 2;
        r11 = r2.stringValue(r9);	 Catch:{ Exception -> 0x0692 }
        r12 = org.telegram.messenger.LocaleController.getInstance();	 Catch:{ Exception -> 0x0692 }
        r12 = r12.getTranslitString(r11);	 Catch:{ Exception -> 0x0692 }
        r15 = r11.equals(r12);	 Catch:{ Exception -> 0x0692 }
        if (r15 == 0) goto L_0x05aa;
    L_0x05a9:
        r12 = 0;
    L_0x05aa:
        r15 = r11.lastIndexOf(r4);	 Catch:{ Exception -> 0x0692 }
        r7 = -1;
        if (r15 == r7) goto L_0x05b8;
    L_0x05b1:
        r15 = r15 + 3;
        r15 = r11.substring(r15);	 Catch:{ Exception -> 0x0692 }
        goto L_0x05b9;
    L_0x05b8:
        r15 = 0;
    L_0x05b9:
        r7 = r8.length;	 Catch:{ Exception -> 0x0692 }
        r9 = 0;
        r16 = 0;
    L_0x05bd:
        if (r9 >= r7) goto L_0x067b;
    L_0x05bf:
        r14 = r8[r9];	 Catch:{ Exception -> 0x0692 }
        r18 = r11.startsWith(r14);	 Catch:{ Exception -> 0x0692 }
        if (r18 != 0) goto L_0x0609;
    L_0x05c7:
        r18 = r3;
        r3 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x0692 }
        r3.<init>();	 Catch:{ Exception -> 0x0692 }
        r3.append(r13);	 Catch:{ Exception -> 0x0692 }
        r3.append(r14);	 Catch:{ Exception -> 0x0692 }
        r3 = r3.toString();	 Catch:{ Exception -> 0x0692 }
        r3 = r11.contains(r3);	 Catch:{ Exception -> 0x0692 }
        if (r3 != 0) goto L_0x060b;
    L_0x05de:
        if (r12 == 0) goto L_0x05fc;
    L_0x05e0:
        r3 = r12.startsWith(r14);	 Catch:{ Exception -> 0x0692 }
        if (r3 != 0) goto L_0x060b;
    L_0x05e6:
        r3 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x0692 }
        r3.<init>();	 Catch:{ Exception -> 0x0692 }
        r3.append(r13);	 Catch:{ Exception -> 0x0692 }
        r3.append(r14);	 Catch:{ Exception -> 0x0692 }
        r3 = r3.toString();	 Catch:{ Exception -> 0x0692 }
        r3 = r12.contains(r3);	 Catch:{ Exception -> 0x0692 }
        if (r3 == 0) goto L_0x05fc;
    L_0x05fb:
        goto L_0x060b;
    L_0x05fc:
        if (r15 == 0) goto L_0x0606;
    L_0x05fe:
        r3 = r15.startsWith(r14);	 Catch:{ Exception -> 0x0692 }
        if (r3 == 0) goto L_0x0606;
    L_0x0604:
        r3 = 2;
        goto L_0x060c;
    L_0x0606:
        r3 = r16;
        goto L_0x060c;
    L_0x0609:
        r18 = r3;
    L_0x060b:
        r3 = 1;
    L_0x060c:
        if (r3 == 0) goto L_0x066b;
    L_0x060e:
        r16 = r4;
        r4 = 0;
        r7 = r2.byteBufferValue(r4);	 Catch:{ Exception -> 0x0692 }
        if (r7 == 0) goto L_0x0680;
    L_0x0617:
        r9 = r7.readInt32(r4);	 Catch:{ Exception -> 0x0692 }
        r9 = org.telegram.tgnet.TLRPC.User.TLdeserialize(r7, r9, r4);	 Catch:{ Exception -> 0x0692 }
        r7.reuse();	 Catch:{ Exception -> 0x0692 }
        r7 = r9.status;	 Catch:{ Exception -> 0x0692 }
        if (r7 == 0) goto L_0x062f;
    L_0x0626:
        r7 = r9.status;	 Catch:{ Exception -> 0x0692 }
        r11 = 1;
        r12 = r2.intValue(r11);	 Catch:{ Exception -> 0x0692 }
        r7.expires = r12;	 Catch:{ Exception -> 0x0692 }
    L_0x062f:
        r7 = 1;
        if (r3 != r7) goto L_0x063f;
    L_0x0632:
        r3 = r9.first_name;	 Catch:{ Exception -> 0x0692 }
        r11 = r9.last_name;	 Catch:{ Exception -> 0x0692 }
        r3 = org.telegram.messenger.AndroidUtilities.generateSearchName(r3, r11, r14);	 Catch:{ Exception -> 0x0692 }
        r5.add(r3);	 Catch:{ Exception -> 0x0692 }
        r14 = 0;
        goto L_0x0667;
    L_0x063f:
        r3 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x0692 }
        r3.<init>();	 Catch:{ Exception -> 0x0692 }
        r3.append(r6);	 Catch:{ Exception -> 0x0692 }
        r11 = r9.username;	 Catch:{ Exception -> 0x0692 }
        r3.append(r11);	 Catch:{ Exception -> 0x0692 }
        r3 = r3.toString();	 Catch:{ Exception -> 0x0692 }
        r11 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x0692 }
        r11.<init>();	 Catch:{ Exception -> 0x0692 }
        r11.append(r6);	 Catch:{ Exception -> 0x0692 }
        r11.append(r14);	 Catch:{ Exception -> 0x0692 }
        r11 = r11.toString();	 Catch:{ Exception -> 0x0692 }
        r14 = 0;
        r3 = org.telegram.messenger.AndroidUtilities.generateSearchName(r3, r14, r11);	 Catch:{ Exception -> 0x0692 }
        r5.add(r3);	 Catch:{ Exception -> 0x0692 }
    L_0x0667:
        r0.add(r9);	 Catch:{ Exception -> 0x0692 }
        goto L_0x0681;
    L_0x066b:
        r16 = r4;
        r4 = 0;
        r14 = 0;
        r17 = 1;
        r9 = r9 + 1;
        r4 = r16;
        r16 = r3;
        r3 = r18;
        goto L_0x05bd;
    L_0x067b:
        r18 = r3;
        r16 = r4;
        r4 = 0;
    L_0x0680:
        r14 = 0;
    L_0x0681:
        r17 = 1;
        r4 = r16;
        r3 = r18;
        goto L_0x0583;
    L_0x0689:
        r2.dispose();	 Catch:{ Exception -> 0x0692 }
    L_0x068c:
        r2 = r26;
        r1.updateSearchResults(r0, r5, r10, r2);	 Catch:{ Exception -> 0x0692 }
        goto L_0x0696;
    L_0x0692:
        r0 = move-exception;
        org.telegram.messenger.FileLog.e(r0);
    L_0x0696:
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.Adapters.DialogsSearchAdapter.lambda$searchDialogsInternal$8$DialogsSearchAdapter(java.lang.String, int):void");
    }

    static /* synthetic */ int lambda$null$7(DialogSearchResult dialogSearchResult, DialogSearchResult dialogSearchResult2) {
        int i = dialogSearchResult.date;
        int i2 = dialogSearchResult2.date;
        if (i < i2) {
            return 1;
        }
        return i > i2 ? -1 : 0;
    }

    private void updateSearchResults(ArrayList<TLObject> arrayList, ArrayList<CharSequence> arrayList2, ArrayList<User> arrayList3, int i) {
        AndroidUtilities.runOnUIThread(new -$$Lambda$DialogsSearchAdapter$-rG-J91Hu2opSK8sjEhFEpnZiuA(this, i, arrayList, arrayList3, arrayList2));
    }

    public /* synthetic */ void lambda$updateSearchResults$9$DialogsSearchAdapter(int i, ArrayList arrayList, ArrayList arrayList2, ArrayList arrayList3) {
        if (i == this.lastSearchId) {
            this.searchWas = true;
            for (int i2 = 0; i2 < arrayList.size(); i2++) {
                TLObject tLObject = (TLObject) arrayList.get(i2);
                if (tLObject instanceof User) {
                    MessagesController.getInstance(this.currentAccount).putUser((User) tLObject, true);
                } else if (tLObject instanceof Chat) {
                    MessagesController.getInstance(this.currentAccount).putChat((Chat) tLObject, true);
                } else if (tLObject instanceof EncryptedChat) {
                    MessagesController.getInstance(this.currentAccount).putEncryptedChat((EncryptedChat) tLObject, true);
                }
            }
            MessagesController.getInstance(this.currentAccount).putUsers(arrayList2, true);
            this.searchResult = arrayList;
            this.searchResultNames = arrayList3;
            this.searchAdapterHelper.mergeResults(this.searchResult);
            notifyDataSetChanged();
            if (this.delegate != null) {
                if (getItemCount() != 0 || (this.searchRunnable2 == null && !this.searchAdapterHelper.isSearchInProgress())) {
                    this.delegate.searchStateChanged(false);
                } else {
                    this.delegate.searchStateChanged(true);
                }
            }
        }
    }

    public boolean isHashtagSearch() {
        return this.searchResultHashtags.isEmpty() ^ 1;
    }

    public void clearRecentHashtags() {
        this.searchAdapterHelper.clearRecentHashtags();
        this.searchResultHashtags.clear();
        notifyDataSetChanged();
    }

    public void searchDialogs(String str) {
        String str2 = str;
        if (str2 == null || !str2.equals(this.lastSearchText)) {
            this.lastSearchText = str2;
            if (this.searchRunnable != null) {
                Utilities.searchQueue.cancelRunnable(this.searchRunnable);
                this.searchRunnable = null;
            }
            Runnable runnable = this.searchRunnable2;
            if (runnable != null) {
                AndroidUtilities.cancelRunOnUIThread(runnable);
                this.searchRunnable2 = null;
            }
            CharSequence trim = str2 != null ? str.trim() : null;
            if (TextUtils.isEmpty(trim)) {
                this.searchAdapterHelper.unloadRecentHashtags();
                this.searchResult.clear();
                this.searchResultNames.clear();
                this.searchResultHashtags.clear();
                this.searchAdapterHelper.mergeResults(null);
                if (this.needMessagesSearch != 2) {
                    this.searchAdapterHelper.queryServerSearch(null, true, true, true, true, 0, this.dialogsType == 0, 0);
                }
                this.searchWas = false;
                this.lastSearchId = -1;
                searchMessagesInternal(null);
                notifyDataSetChanged();
            } else {
                if (this.needMessagesSearch != 2 && trim.startsWith("#") && trim.length() == 1) {
                    this.messagesSearchEndReached = true;
                    if (this.searchAdapterHelper.loadRecentHashtags()) {
                        this.searchResultMessages.clear();
                        this.searchResultHashtags.clear();
                        ArrayList hashtags = this.searchAdapterHelper.getHashtags();
                        for (int i = 0; i < hashtags.size(); i++) {
                            this.searchResultHashtags.add(((HashtagObject) hashtags.get(i)).hashtag);
                        }
                        DialogsSearchAdapterDelegate dialogsSearchAdapterDelegate = this.delegate;
                        if (dialogsSearchAdapterDelegate != null) {
                            dialogsSearchAdapterDelegate.searchStateChanged(false);
                        }
                    }
                    notifyDataSetChanged();
                } else {
                    this.searchResultHashtags.clear();
                    notifyDataSetChanged();
                }
                int i2 = this.lastSearchId + 1;
                this.lastSearchId = i2;
                DispatchQueue dispatchQueue = Utilities.searchQueue;
                -$$Lambda$DialogsSearchAdapter$5hlgRbUoNpWg__zB2HOstsRRhkE -__lambda_dialogssearchadapter_5hlgrbuonpwg__zb2hostsrrhke = new -$$Lambda$DialogsSearchAdapter$5hlgRbUoNpWg__zB2HOstsRRhkE(this, trim, i2, str2);
                this.searchRunnable = -__lambda_dialogssearchadapter_5hlgrbuonpwg__zb2hostsrrhke;
                dispatchQueue.postRunnable(-__lambda_dialogssearchadapter_5hlgrbuonpwg__zb2hostsrrhke, 300);
            }
        }
    }

    public /* synthetic */ void lambda$searchDialogs$11$DialogsSearchAdapter(String str, int i, String str2) {
        this.searchRunnable = null;
        searchDialogsInternal(str, i);
        -$$Lambda$DialogsSearchAdapter$bhs4AL0tZyn7iTR6w_7zHv9HkA0 -__lambda_dialogssearchadapter_bhs4al0tzyn7itr6w_7zhv9hka0 = new -$$Lambda$DialogsSearchAdapter$bhs4AL0tZyn7iTR6w_7zHv9HkA0(this, i, str, str2);
        this.searchRunnable2 = -__lambda_dialogssearchadapter_bhs4al0tzyn7itr6w_7zhv9hka0;
        AndroidUtilities.runOnUIThread(-__lambda_dialogssearchadapter_bhs4al0tzyn7itr6w_7zhv9hka0);
    }

    public /* synthetic */ void lambda$null$10$DialogsSearchAdapter(int i, String str, String str2) {
        this.searchRunnable2 = null;
        if (i == this.lastSearchId) {
            if (this.needMessagesSearch != 2) {
                this.searchAdapterHelper.queryServerSearch(str, true, this.dialogsType != 4, true, this.dialogsType != 4, 0, this.dialogsType == 0, 0);
            }
            searchMessagesInternal(str2);
        }
    }

    public int getItemCount() {
        int i;
        int size;
        if (isRecentSearchDisplayed()) {
            i = 0;
            size = !this.recentSearchObjects.isEmpty() ? this.recentSearchObjects.size() + 1 : 0;
            if (!MediaDataController.getInstance(this.currentAccount).hints.isEmpty()) {
                i = 2;
            }
            return size + i;
        } else if (!this.searchResultHashtags.isEmpty()) {
            return this.searchResultHashtags.size() + 1;
        } else {
            size = this.searchResult.size();
            i = this.searchAdapterHelper.getLocalServerSearch().size();
            int size2 = this.searchAdapterHelper.getGlobalSearch().size();
            int size3 = this.searchAdapterHelper.getPhoneSearch().size();
            int size4 = this.searchResultMessages.size();
            size += i;
            if (size2 != 0) {
                size += size2 + 1;
            }
            if (size3 != 0) {
                size += size3;
            }
            if (size4 != 0) {
                size += (size4 + 1) + (this.messagesSearchEndReached ^ 1);
            }
            return size;
        }
    }

    /* JADX WARNING: Missing block: B:11:0x0045, code skipped:
            if (r0 != null) goto L_0x0061;
     */
    /* JADX WARNING: Missing block: B:15:0x005f, code skipped:
            if (r0 != null) goto L_0x0061;
     */
    public java.lang.Object getItem(int r11) {
        /*
        r10 = this;
        r0 = r10.isRecentSearchDisplayed();
        r1 = 0;
        r2 = 0;
        if (r0 == 0) goto L_0x0064;
    L_0x0008:
        r0 = r10.currentAccount;
        r0 = org.telegram.messenger.MediaDataController.getInstance(r0);
        r0 = r0.hints;
        r0 = r0.isEmpty();
        if (r0 != 0) goto L_0x0017;
    L_0x0016:
        r2 = 2;
    L_0x0017:
        if (r11 <= r2) goto L_0x0063;
    L_0x0019:
        r11 = r11 + -1;
        r11 = r11 - r2;
        r0 = r10.recentSearchObjects;
        r0 = r0.size();
        if (r11 >= r0) goto L_0x0063;
    L_0x0024:
        r0 = r10.recentSearchObjects;
        r11 = r0.get(r11);
        r11 = (org.telegram.ui.Adapters.DialogsSearchAdapter.RecentSearchObject) r11;
        r11 = r11.object;
        r0 = r11 instanceof org.telegram.tgnet.TLRPC.User;
        if (r0 == 0) goto L_0x0048;
    L_0x0032:
        r0 = r10.currentAccount;
        r0 = org.telegram.messenger.MessagesController.getInstance(r0);
        r1 = r11;
        r1 = (org.telegram.tgnet.TLRPC.User) r1;
        r1 = r1.id;
        r1 = java.lang.Integer.valueOf(r1);
        r0 = r0.getUser(r1);
        if (r0 == 0) goto L_0x0062;
    L_0x0047:
        goto L_0x0061;
    L_0x0048:
        r0 = r11 instanceof org.telegram.tgnet.TLRPC.Chat;
        if (r0 == 0) goto L_0x0062;
    L_0x004c:
        r0 = r10.currentAccount;
        r0 = org.telegram.messenger.MessagesController.getInstance(r0);
        r1 = r11;
        r1 = (org.telegram.tgnet.TLRPC.Chat) r1;
        r1 = r1.id;
        r1 = java.lang.Integer.valueOf(r1);
        r0 = r0.getChat(r1);
        if (r0 == 0) goto L_0x0062;
    L_0x0061:
        r11 = r0;
    L_0x0062:
        return r11;
    L_0x0063:
        return r1;
    L_0x0064:
        r0 = r10.searchResultHashtags;
        r0 = r0.isEmpty();
        if (r0 != 0) goto L_0x0078;
    L_0x006c:
        if (r11 <= 0) goto L_0x0077;
    L_0x006e:
        r0 = r10.searchResultHashtags;
        r11 = r11 + -1;
        r11 = r0.get(r11);
        return r11;
    L_0x0077:
        return r1;
    L_0x0078:
        r0 = r10.searchAdapterHelper;
        r0 = r0.getGlobalSearch();
        r3 = r10.searchAdapterHelper;
        r3 = r3.getLocalServerSearch();
        r4 = r10.searchAdapterHelper;
        r4 = r4.getPhoneSearch();
        r5 = r10.searchResult;
        r5 = r5.size();
        r6 = r3.size();
        r7 = r4.size();
        r8 = r0.isEmpty();
        if (r8 == 0) goto L_0x00a0;
    L_0x009e:
        r8 = 0;
        goto L_0x00a6;
    L_0x00a0:
        r8 = r0.size();
        r8 = r8 + 1;
    L_0x00a6:
        r9 = r10.searchResultMessages;
        r9 = r9.isEmpty();
        if (r9 == 0) goto L_0x00af;
    L_0x00ae:
        goto L_0x00b7;
    L_0x00af:
        r2 = r10.searchResultMessages;
        r2 = r2.size();
        r2 = r2 + 1;
    L_0x00b7:
        if (r11 < 0) goto L_0x00c2;
    L_0x00b9:
        if (r11 >= r5) goto L_0x00c2;
    L_0x00bb:
        r0 = r10.searchResult;
        r11 = r0.get(r11);
        return r11;
    L_0x00c2:
        r11 = r11 - r5;
        if (r11 < 0) goto L_0x00cc;
    L_0x00c5:
        if (r11 >= r6) goto L_0x00cc;
    L_0x00c7:
        r11 = r3.get(r11);
        return r11;
    L_0x00cc:
        r11 = r11 - r6;
        if (r11 < 0) goto L_0x00d6;
    L_0x00cf:
        if (r11 >= r7) goto L_0x00d6;
    L_0x00d1:
        r11 = r4.get(r11);
        return r11;
    L_0x00d6:
        r11 = r11 - r7;
        if (r11 <= 0) goto L_0x00e2;
    L_0x00d9:
        if (r11 >= r8) goto L_0x00e2;
    L_0x00db:
        r11 = r11 + -1;
        r11 = r0.get(r11);
        return r11;
    L_0x00e2:
        r11 = r11 - r8;
        if (r11 <= 0) goto L_0x00f0;
    L_0x00e5:
        if (r11 >= r2) goto L_0x00f0;
    L_0x00e7:
        r0 = r10.searchResultMessages;
        r11 = r11 + -1;
        r11 = r0.get(r11);
        return r11;
    L_0x00f0:
        return r1;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.Adapters.DialogsSearchAdapter.getItem(int):java.lang.Object");
    }

    public boolean isGlobalSearch(int i) {
        if (isRecentSearchDisplayed() || !this.searchResultHashtags.isEmpty()) {
            return false;
        }
        ArrayList globalSearch = this.searchAdapterHelper.getGlobalSearch();
        ArrayList localServerSearch = this.searchAdapterHelper.getLocalServerSearch();
        int size = this.searchResult.size();
        int size2 = localServerSearch.size();
        int size3 = this.searchAdapterHelper.getPhoneSearch().size();
        int size4 = globalSearch.isEmpty() ? 0 : globalSearch.size() + 1;
        int size5 = this.searchResultMessages.isEmpty() ? 0 : this.searchResultMessages.size() + 1;
        if (i >= 0 && i < size) {
            return false;
        }
        i -= size;
        if (i >= 0 && i < size2) {
            return false;
        }
        i -= size2;
        if (i > 0 && i < size3) {
            return false;
        }
        i -= size3;
        if (i > 0 && i < size4) {
            return true;
        }
        i -= size4;
        if (i <= 0 || i < size5) {
        }
        return false;
    }

    public boolean isEnabled(ViewHolder viewHolder) {
        int itemViewType = viewHolder.getItemViewType();
        return (itemViewType == 1 || itemViewType == 3) ? false : true;
    }

    public /* synthetic */ void lambda$onCreateViewHolder$12$DialogsSearchAdapter(View view, int i) {
        DialogsSearchAdapterDelegate dialogsSearchAdapterDelegate = this.delegate;
        if (dialogsSearchAdapterDelegate != null) {
            dialogsSearchAdapterDelegate.didPressedOnSubDialog((long) ((Integer) view.getTag()).intValue());
        }
    }

    public /* synthetic */ boolean lambda$onCreateViewHolder$13$DialogsSearchAdapter(View view, int i) {
        DialogsSearchAdapterDelegate dialogsSearchAdapterDelegate = this.delegate;
        if (dialogsSearchAdapterDelegate != null) {
            dialogsSearchAdapterDelegate.needRemoveHint(((Integer) view.getTag()).intValue());
        }
        return true;
    }

    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = null;
        switch (i) {
            case 0:
                view = new ProfileSearchCell(this.mContext);
                break;
            case 1:
                view = new GraySectionCell(this.mContext);
                break;
            case 2:
                view = new DialogCell(this.mContext, false, true);
                break;
            case 3:
                view = new LoadingCell(this.mContext);
                break;
            case 4:
                view = new HashtagSearchCell(this.mContext);
                break;
            case 5:
                View anonymousClass2 = new RecyclerListView(this.mContext) {
                    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
                        if (!(getParent() == null || getParent().getParent() == null)) {
                            getParent().getParent().requestDisallowInterceptTouchEvent(true);
                        }
                        return super.onInterceptTouchEvent(motionEvent);
                    }
                };
                anonymousClass2.setTag(Integer.valueOf(9));
                anonymousClass2.setItemAnimator(null);
                anonymousClass2.setLayoutAnimation(null);
                AnonymousClass3 anonymousClass3 = new LinearLayoutManager(this.mContext) {
                    public boolean supportsPredictiveItemAnimations() {
                        return false;
                    }
                };
                anonymousClass3.setOrientation(0);
                anonymousClass2.setLayoutManager(anonymousClass3);
                anonymousClass2.setAdapter(new CategoryAdapterRecycler(this, null));
                anonymousClass2.setOnItemClickListener(new -$$Lambda$DialogsSearchAdapter$DZkEHCwRy7JqjbUQmUNPYIVHu-I(this));
                anonymousClass2.setOnItemLongClickListener(new -$$Lambda$DialogsSearchAdapter$VmJg1wMYhOLJS8dwKIzHrQMjS0A(this));
                this.innerListView = anonymousClass2;
                view = anonymousClass2;
                break;
            case 6:
                view = new TextCell(this.mContext, 16);
                break;
        }
        if (i == 5) {
            view.setLayoutParams(new LayoutParams(-1, AndroidUtilities.dp(86.0f)));
        } else {
            view.setLayoutParams(new LayoutParams(-1, -2));
        }
        return new Holder(view);
    }

    /* JADX WARNING: Removed duplicated region for block: B:150:0x036e  */
    public void onBindViewHolder(androidx.recyclerview.widget.RecyclerView.ViewHolder r17, int r18) {
        /*
        r16 = this;
        r1 = r16;
        r0 = r17;
        r2 = r18;
        r3 = r17.getItemViewType();
        r4 = 2;
        r5 = 0;
        r6 = 0;
        r7 = 1;
        if (r3 == 0) goto L_0x017e;
    L_0x0010:
        if (r3 == r7) goto L_0x00a9;
    L_0x0012:
        if (r3 == r4) goto L_0x0088;
    L_0x0014:
        r8 = 4;
        if (r3 == r8) goto L_0x0069;
    L_0x0017:
        r8 = 5;
        if (r3 == r8) goto L_0x0059;
    L_0x001a:
        r4 = 6;
        if (r3 == r4) goto L_0x001f;
    L_0x001d:
        goto L_0x0374;
    L_0x001f:
        r2 = r1.getItem(r2);
        r2 = (java.lang.String) r2;
        r0 = r0.itemView;
        r0 = (org.telegram.ui.Cells.TextCell) r0;
        r3 = "windowBackgroundWhiteBlueText2";
        r0.setColors(r5, r3);
        r3 = NUM; // 0x7f0e00aa float:1.8875382E38 double:1.0531622406E-314;
        r4 = new java.lang.Object[r7];
        r5 = org.telegram.PhoneFormat.PhoneFormat.getInstance();
        r7 = new java.lang.StringBuilder;
        r7.<init>();
        r8 = "+";
        r7.append(r8);
        r7.append(r2);
        r2 = r7.toString();
        r2 = r5.format(r2);
        r4[r6] = r2;
        r2 = "AddContactByPhone";
        r2 = org.telegram.messenger.LocaleController.formatString(r2, r3, r4);
        r0.setText(r2, r6);
        goto L_0x0374;
    L_0x0059:
        r0 = r0.itemView;
        r0 = (org.telegram.ui.Components.RecyclerListView) r0;
        r0 = r0.getAdapter();
        r0 = (org.telegram.ui.Adapters.DialogsSearchAdapter.CategoryAdapterRecycler) r0;
        r2 = r2 / r4;
        r0.setIndex(r2);
        goto L_0x0374;
    L_0x0069:
        r0 = r0.itemView;
        r0 = (org.telegram.ui.Cells.HashtagSearchCell) r0;
        r3 = r1.searchResultHashtags;
        r4 = r2 + -1;
        r3 = r3.get(r4);
        r3 = (java.lang.CharSequence) r3;
        r0.setText(r3);
        r3 = r1.searchResultHashtags;
        r3 = r3.size();
        if (r2 == r3) goto L_0x0083;
    L_0x0082:
        r6 = 1;
    L_0x0083:
        r0.setNeedDivider(r6);
        goto L_0x0374;
    L_0x0088:
        r0 = r0.itemView;
        r0 = (org.telegram.ui.Cells.DialogCell) r0;
        r3 = r16.getItemCount();
        r3 = r3 - r7;
        if (r2 == r3) goto L_0x0094;
    L_0x0093:
        r6 = 1;
    L_0x0094:
        r0.useSeparator = r6;
        r2 = r1.getItem(r2);
        r2 = (org.telegram.messenger.MessageObject) r2;
        r3 = r2.getDialogId();
        r5 = r2.messageOwner;
        r5 = r5.date;
        r0.setDialog(r3, r2, r5);
        goto L_0x0374;
    L_0x00a9:
        r0 = r0.itemView;
        r0 = (org.telegram.ui.Cells.GraySectionCell) r0;
        r3 = r16.isRecentSearchDisplayed();
        r5 = NUM; // 0x7f0e02d6 float:1.887651E38 double:1.0531625153E-314;
        r8 = "ClearButton";
        if (r3 == 0) goto L_0x00ef;
    L_0x00b8:
        r3 = r1.currentAccount;
        r3 = org.telegram.messenger.MediaDataController.getInstance(r3);
        r3 = r3.hints;
        r3 = r3.isEmpty();
        if (r3 != 0) goto L_0x00c7;
    L_0x00c6:
        goto L_0x00c8;
    L_0x00c7:
        r4 = 0;
    L_0x00c8:
        if (r2 >= r4) goto L_0x00d8;
    L_0x00ca:
        r2 = NUM; // 0x7f0e0288 float:1.8876352E38 double:1.053162477E-314;
        r3 = "ChatHints";
        r2 = org.telegram.messenger.LocaleController.getString(r3, r2);
        r0.setText(r2);
        goto L_0x0374;
    L_0x00d8:
        r2 = NUM; // 0x7f0e08db float:1.8879636E38 double:1.0531632767E-314;
        r3 = "Recent";
        r2 = org.telegram.messenger.LocaleController.getString(r3, r2);
        r3 = org.telegram.messenger.LocaleController.getString(r8, r5);
        r4 = new org.telegram.ui.Adapters.-$$Lambda$DialogsSearchAdapter$941fnPDSgReuKOmz7WsSoxVuOTY;
        r4.<init>(r1);
        r0.setText(r2, r3, r4);
        goto L_0x0374;
    L_0x00ef:
        r3 = r1.searchResultHashtags;
        r3 = r3.isEmpty();
        if (r3 != 0) goto L_0x010e;
    L_0x00f7:
        r2 = NUM; // 0x7f0e0515 float:1.8877676E38 double:1.0531627994E-314;
        r3 = "Hashtags";
        r2 = org.telegram.messenger.LocaleController.getString(r3, r2);
        r3 = org.telegram.messenger.LocaleController.getString(r8, r5);
        r4 = new org.telegram.ui.Adapters.-$$Lambda$DialogsSearchAdapter$honrBco-zV9w0fwaI91SKdwfMI0;
        r4.<init>(r1);
        r0.setText(r2, r3, r4);
        goto L_0x0374;
    L_0x010e:
        r3 = r1.searchAdapterHelper;
        r3 = r3.getGlobalSearch();
        r4 = r1.searchResult;
        r4 = r4.size();
        r5 = r1.searchAdapterHelper;
        r5 = r5.getLocalServerSearch();
        r5 = r5.size();
        r8 = r1.searchAdapterHelper;
        r8 = r8.getPhoneSearch();
        r8 = r8.size();
        r9 = r3.isEmpty();
        if (r9 == 0) goto L_0x0135;
    L_0x0134:
        goto L_0x013b;
    L_0x0135:
        r3 = r3.size();
        r6 = r3 + 1;
    L_0x013b:
        r3 = r1.searchResultMessages;
        r3 = r3.isEmpty();
        if (r3 == 0) goto L_0x0144;
    L_0x0143:
        goto L_0x0149;
    L_0x0144:
        r3 = r1.searchResultMessages;
        r3.size();
    L_0x0149:
        r4 = r4 + r5;
        r2 = r2 - r4;
        if (r2 < 0) goto L_0x015d;
    L_0x014d:
        if (r2 >= r8) goto L_0x015d;
    L_0x014f:
        r2 = NUM; // 0x7f0e0867 float:1.88794E38 double:1.0531632194E-314;
        r3 = "PhoneNumberSearch";
        r2 = org.telegram.messenger.LocaleController.getString(r3, r2);
        r0.setText(r2);
        goto L_0x0374;
    L_0x015d:
        r2 = r2 - r8;
        if (r2 < 0) goto L_0x0170;
    L_0x0160:
        if (r2 >= r6) goto L_0x0170;
    L_0x0162:
        r2 = NUM; // 0x7f0e04ea float:1.887759E38 double:1.053162778E-314;
        r3 = "GlobalSearch";
        r2 = org.telegram.messenger.LocaleController.getString(r3, r2);
        r0.setText(r2);
        goto L_0x0374;
    L_0x0170:
        r2 = NUM; // 0x7f0e095e float:1.8879901E38 double:1.0531633414E-314;
        r3 = "SearchMessages";
        r2 = org.telegram.messenger.LocaleController.getString(r3, r2);
        r0.setText(r2);
        goto L_0x0374;
    L_0x017e:
        r0 = r0.itemView;
        r8 = r0;
        r8 = (org.telegram.ui.Cells.ProfileSearchCell) r8;
        r0 = r1.getItem(r2);
        r3 = r0 instanceof org.telegram.tgnet.TLRPC.User;
        if (r3 == 0) goto L_0x0194;
    L_0x018b:
        r0 = (org.telegram.tgnet.TLRPC.User) r0;
        r3 = r0.username;
        r10 = r3;
        r9 = r5;
        r11 = r9;
        r3 = r0;
        goto L_0x01e3;
    L_0x0194:
        r3 = r0 instanceof org.telegram.tgnet.TLRPC.Chat;
        if (r3 == 0) goto L_0x01b5;
    L_0x0198:
        r3 = r1.currentAccount;
        r3 = org.telegram.messenger.MessagesController.getInstance(r3);
        r0 = (org.telegram.tgnet.TLRPC.Chat) r0;
        r9 = r0.id;
        r9 = java.lang.Integer.valueOf(r9);
        r3 = r3.getChat(r9);
        if (r3 != 0) goto L_0x01ad;
    L_0x01ac:
        goto L_0x01ae;
    L_0x01ad:
        r0 = r3;
    L_0x01ae:
        r3 = r0.username;
        r9 = r0;
        r10 = r3;
        r3 = r5;
        r11 = r3;
        goto L_0x01e3;
    L_0x01b5:
        r3 = r0 instanceof org.telegram.tgnet.TLRPC.EncryptedChat;
        if (r3 == 0) goto L_0x01df;
    L_0x01b9:
        r3 = r1.currentAccount;
        r3 = org.telegram.messenger.MessagesController.getInstance(r3);
        r0 = (org.telegram.tgnet.TLRPC.EncryptedChat) r0;
        r0 = r0.id;
        r0 = java.lang.Integer.valueOf(r0);
        r0 = r3.getEncryptedChat(r0);
        r3 = r1.currentAccount;
        r3 = org.telegram.messenger.MessagesController.getInstance(r3);
        r9 = r0.user_id;
        r9 = java.lang.Integer.valueOf(r9);
        r3 = r3.getUser(r9);
        r11 = r0;
        r9 = r5;
        r10 = r9;
        goto L_0x01e3;
    L_0x01df:
        r3 = r5;
        r9 = r3;
        r10 = r9;
        r11 = r10;
    L_0x01e3:
        r0 = r16.isRecentSearchDisplayed();
        if (r0 == 0) goto L_0x01f9;
    L_0x01e9:
        r0 = r16.getItemCount();
        r0 = r0 - r7;
        if (r2 == r0) goto L_0x01f2;
    L_0x01f0:
        r0 = 1;
        goto L_0x01f3;
    L_0x01f2:
        r0 = 0;
    L_0x01f3:
        r8.useSeparator = r0;
        r0 = r5;
        r13 = 1;
        goto L_0x030f;
    L_0x01f9:
        r0 = r1.searchAdapterHelper;
        r0 = r0.getGlobalSearch();
        r12 = r1.searchAdapterHelper;
        r12 = r12.getPhoneSearch();
        r13 = r1.searchResult;
        r13 = r13.size();
        r14 = r1.searchAdapterHelper;
        r14 = r14.getLocalServerSearch();
        r14 = r14.size();
        r15 = r12.size();
        if (r15 <= 0) goto L_0x0228;
    L_0x021b:
        r5 = r15 + -1;
        r5 = r12.get(r5);
        r5 = r5 instanceof java.lang.String;
        if (r5 == 0) goto L_0x0228;
    L_0x0225:
        r5 = r15 + -2;
        goto L_0x0229;
    L_0x0228:
        r5 = r15;
    L_0x0229:
        r12 = r0.isEmpty();
        if (r12 == 0) goto L_0x0231;
    L_0x022f:
        r0 = 0;
        goto L_0x0236;
    L_0x0231:
        r0 = r0.size();
        r0 = r0 + r7;
    L_0x0236:
        r12 = r16.getItemCount();
        r12 = r12 - r7;
        if (r2 == r12) goto L_0x024a;
    L_0x023d:
        r5 = r5 + r13;
        r5 = r5 + r14;
        r5 = r5 - r7;
        if (r2 == r5) goto L_0x024a;
    L_0x0242:
        r13 = r13 + r0;
        r13 = r13 + r15;
        r13 = r13 + r14;
        r13 = r13 - r7;
        if (r2 == r13) goto L_0x024a;
    L_0x0248:
        r0 = 1;
        goto L_0x024b;
    L_0x024a:
        r0 = 0;
    L_0x024b:
        r8.useSeparator = r0;
        r0 = r1.searchResult;
        r0 = r0.size();
        r5 = "@";
        if (r2 >= r0) goto L_0x028c;
    L_0x0257:
        r0 = r1.searchResultNames;
        r0 = r0.get(r2);
        r0 = (java.lang.CharSequence) r0;
        if (r0 == 0) goto L_0x030d;
    L_0x0261:
        if (r3 == 0) goto L_0x030d;
    L_0x0263:
        r2 = r3.username;
        if (r2 == 0) goto L_0x030d;
    L_0x0267:
        r2 = r2.length();
        if (r2 <= 0) goto L_0x030d;
    L_0x026d:
        r2 = r0.toString();
        r10 = new java.lang.StringBuilder;
        r10.<init>();
        r10.append(r5);
        r5 = r3.username;
        r10.append(r5);
        r5 = r10.toString();
        r2 = r2.startsWith(r5);
        if (r2 == 0) goto L_0x030d;
    L_0x0288:
        r5 = r0;
    L_0x0289:
        r0 = 0;
        goto L_0x030e;
    L_0x028c:
        r0 = r1.searchAdapterHelper;
        r0 = r0.getLastFoundUsername();
        r2 = android.text.TextUtils.isEmpty(r0);
        if (r2 != 0) goto L_0x030c;
    L_0x0298:
        if (r3 == 0) goto L_0x02a3;
    L_0x029a:
        r2 = r3.first_name;
        r12 = r3.last_name;
        r2 = org.telegram.messenger.ContactsController.formatName(r2, r12);
        goto L_0x02a9;
    L_0x02a3:
        if (r9 == 0) goto L_0x02a8;
    L_0x02a5:
        r2 = r9.title;
        goto L_0x02a9;
    L_0x02a8:
        r2 = 0;
    L_0x02a9:
        r12 = 33;
        r13 = "windowBackgroundWhiteBlueText4";
        r14 = -1;
        if (r2 == 0) goto L_0x02ce;
    L_0x02b0:
        r15 = org.telegram.messenger.AndroidUtilities.indexOfIgnoreCase(r2, r0);
        if (r15 == r14) goto L_0x02ce;
    L_0x02b6:
        r5 = new android.text.SpannableStringBuilder;
        r5.<init>(r2);
        r2 = new android.text.style.ForegroundColorSpan;
        r10 = org.telegram.ui.ActionBar.Theme.getColor(r13);
        r2.<init>(r10);
        r0 = r0.length();
        r0 = r0 + r15;
        r5.setSpan(r2, r15, r0, r12);
        r0 = r5;
        goto L_0x030d;
    L_0x02ce:
        if (r10 == 0) goto L_0x030c;
    L_0x02d0:
        r2 = r0.startsWith(r5);
        if (r2 == 0) goto L_0x02da;
    L_0x02d6:
        r0 = r0.substring(r7);
    L_0x02da:
        r2 = new android.text.SpannableStringBuilder;	 Catch:{ Exception -> 0x0305 }
        r2.<init>();	 Catch:{ Exception -> 0x0305 }
        r2.append(r5);	 Catch:{ Exception -> 0x0305 }
        r2.append(r10);	 Catch:{ Exception -> 0x0305 }
        r5 = org.telegram.messenger.AndroidUtilities.indexOfIgnoreCase(r10, r0);	 Catch:{ Exception -> 0x0305 }
        if (r5 == r14) goto L_0x0303;
    L_0x02eb:
        r0 = r0.length();	 Catch:{ Exception -> 0x0305 }
        if (r5 != 0) goto L_0x02f4;
    L_0x02f1:
        r0 = r0 + 1;
        goto L_0x02f6;
    L_0x02f4:
        r5 = r5 + 1;
    L_0x02f6:
        r14 = new android.text.style.ForegroundColorSpan;	 Catch:{ Exception -> 0x0305 }
        r13 = org.telegram.ui.ActionBar.Theme.getColor(r13);	 Catch:{ Exception -> 0x0305 }
        r14.<init>(r13);	 Catch:{ Exception -> 0x0305 }
        r0 = r0 + r5;
        r2.setSpan(r14, r5, r0, r12);	 Catch:{ Exception -> 0x0305 }
    L_0x0303:
        r5 = r2;
        goto L_0x0289;
    L_0x0305:
        r0 = move-exception;
        org.telegram.messenger.FileLog.e(r0);
        r5 = r10;
        goto L_0x0289;
    L_0x030c:
        r0 = 0;
    L_0x030d:
        r5 = 0;
    L_0x030e:
        r13 = 0;
    L_0x030f:
        if (r3 == 0) goto L_0x0323;
    L_0x0311:
        r2 = r3.id;
        r10 = r1.selfUserId;
        if (r2 != r10) goto L_0x0323;
    L_0x0317:
        r0 = NUM; // 0x7f0e0945 float:1.887985E38 double:1.053163329E-314;
        r2 = "SavedMessages";
        r0 = org.telegram.messenger.LocaleController.getString(r2, r0);
        r5 = 0;
        r14 = 1;
        goto L_0x0324;
    L_0x0323:
        r14 = 0;
    L_0x0324:
        if (r9 == 0) goto L_0x036b;
    L_0x0326:
        r2 = r9.participants_count;
        if (r2 == 0) goto L_0x036b;
    L_0x032a:
        r2 = org.telegram.messenger.ChatObject.isChannel(r9);
        if (r2 == 0) goto L_0x033d;
    L_0x0330:
        r2 = r9.megagroup;
        if (r2 != 0) goto L_0x033d;
    L_0x0334:
        r2 = r9.participants_count;
        r10 = "Subscribers";
        r2 = org.telegram.messenger.LocaleController.formatPluralString(r10, r2);
        goto L_0x0345;
    L_0x033d:
        r2 = r9.participants_count;
        r10 = "Members";
        r2 = org.telegram.messenger.LocaleController.formatPluralString(r10, r2);
    L_0x0345:
        r10 = r5 instanceof android.text.SpannableStringBuilder;
        r12 = ", ";
        if (r10 == 0) goto L_0x0356;
    L_0x034b:
        r4 = r5;
        r4 = (android.text.SpannableStringBuilder) r4;
        r4 = r4.append(r12);
        r4.append(r2);
        goto L_0x036b;
    L_0x0356:
        r10 = android.text.TextUtils.isEmpty(r5);
        if (r10 != 0) goto L_0x0369;
    L_0x035c:
        r10 = 3;
        r10 = new java.lang.CharSequence[r10];
        r10[r6] = r5;
        r10[r7] = r12;
        r10[r4] = r2;
        r2 = android.text.TextUtils.concat(r10);
    L_0x0369:
        r12 = r2;
        goto L_0x036c;
    L_0x036b:
        r12 = r5;
    L_0x036c:
        if (r3 == 0) goto L_0x036f;
    L_0x036e:
        r9 = r3;
    L_0x036f:
        r10 = r11;
        r11 = r0;
        r8.setData(r9, r10, r11, r12, r13, r14);
    L_0x0374:
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.Adapters.DialogsSearchAdapter.onBindViewHolder(androidx.recyclerview.widget.RecyclerView$ViewHolder, int):void");
    }

    public /* synthetic */ void lambda$onBindViewHolder$14$DialogsSearchAdapter(View view) {
        DialogsSearchAdapterDelegate dialogsSearchAdapterDelegate = this.delegate;
        if (dialogsSearchAdapterDelegate != null) {
            dialogsSearchAdapterDelegate.needClearList();
        }
    }

    public /* synthetic */ void lambda$onBindViewHolder$15$DialogsSearchAdapter(View view) {
        DialogsSearchAdapterDelegate dialogsSearchAdapterDelegate = this.delegate;
        if (dialogsSearchAdapterDelegate != null) {
            dialogsSearchAdapterDelegate.needClearList();
        }
    }

    public int getItemViewType(int i) {
        int i2 = 1;
        int i3;
        if (isRecentSearchDisplayed()) {
            i3 = !MediaDataController.getInstance(this.currentAccount).hints.isEmpty() ? 2 : 0;
            if (i > i3) {
                return 0;
            }
            if (i == i3 || i % 2 == 0) {
                return 1;
            }
            return 5;
        } else if (this.searchResultHashtags.isEmpty()) {
            ArrayList globalSearch = this.searchAdapterHelper.getGlobalSearch();
            int size = this.searchResult.size();
            int size2 = this.searchAdapterHelper.getLocalServerSearch().size();
            int size3 = this.searchAdapterHelper.getPhoneSearch().size();
            i3 = globalSearch.isEmpty() ? 0 : globalSearch.size() + 1;
            int size4 = this.searchResultMessages.isEmpty() ? 0 : this.searchResultMessages.size() + 1;
            if (i >= 0 && i < size) {
                return 0;
            }
            i -= size;
            if (i >= 0 && i < size2) {
                return 0;
            }
            i -= size2;
            if (i < 0 || i >= size3) {
                i -= size3;
                if (i >= 0 && i < i3) {
                    return i == 0 ? 1 : 0;
                } else {
                    i -= i3;
                    if (i < 0 || i >= size4) {
                        return 3;
                    }
                    return i == 0 ? 1 : 2;
                }
            } else {
                Object item = getItem(i);
                if (!(item instanceof String)) {
                    return 0;
                }
                if ("section".equals((String) item)) {
                    return 1;
                }
                return 6;
            }
        } else {
            if (i != 0) {
                i2 = 4;
            }
            return i2;
        }
    }
}
