package org.telegram.p005ui;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Timer;
import java.util.TimerTask;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.NotificationCenter.NotificationCenterDelegate;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.Utilities;
import org.telegram.messenger.beta.R;
import org.telegram.messenger.support.widget.LinearLayoutManager;
import org.telegram.messenger.support.widget.RecyclerView.ViewHolder;
import org.telegram.p005ui.ActionBar.ActionBarMenuItem;
import org.telegram.p005ui.ActionBar.ActionBarMenuItem.ActionBarMenuItemSearchListener;
import org.telegram.p005ui.ActionBar.BaseFragment;
import org.telegram.p005ui.ActionBar.CLASSNAMEActionBar.ActionBarMenuOnItemClick;
import org.telegram.p005ui.ActionBar.Theme;
import org.telegram.p005ui.ActionBar.ThemeDescription;
import org.telegram.p005ui.ActionBar.ThemeDescription.ThemeDescriptionDelegate;
import org.telegram.p005ui.Cells.TextCheckCell;
import org.telegram.p005ui.Cells.TextInfoPrivacyCell;
import org.telegram.p005ui.Cells.UserCell;
import org.telegram.p005ui.Components.EmptyTextProgressView;
import org.telegram.p005ui.Components.LayoutHelper;
import org.telegram.p005ui.Components.RecyclerListView;
import org.telegram.p005ui.Components.RecyclerListView.Holder;
import org.telegram.p005ui.Components.RecyclerListView.SelectionAdapter;
import org.telegram.tgnet.TLRPC.Chat;
import org.telegram.tgnet.TLRPC.ChatFull;
import org.telegram.tgnet.TLRPC.ChatParticipant;
import org.telegram.tgnet.TLRPC.TL_chatParticipant;
import org.telegram.tgnet.TLRPC.TL_chatParticipantAdmin;
import org.telegram.tgnet.TLRPC.TL_chatParticipantCreator;
import org.telegram.tgnet.TLRPC.User;

/* renamed from: org.telegram.ui.SetAdminsActivity */
public class SetAdminsActivity extends BaseFragment implements NotificationCenterDelegate {
    private int allAdminsInfoRow;
    private int allAdminsRow;
    private Chat chat;
    private int chat_id;
    private EmptyTextProgressView emptyView;
    private ChatFull info;
    private ListAdapter listAdapter;
    private RecyclerListView listView;
    private ArrayList<ChatParticipant> participants = new ArrayList();
    private int rowCount;
    private SearchAdapter searchAdapter;
    private ActionBarMenuItem searchItem;
    private boolean searchWas;
    private boolean searching;
    private int usersEndRow;
    private int usersStartRow;

    /* renamed from: org.telegram.ui.SetAdminsActivity$1 */
    class CLASSNAME extends ActionBarMenuOnItemClick {
        CLASSNAME() {
        }

        public void onItemClick(int id) {
            if (id == -1) {
                SetAdminsActivity.this.lambda$checkDiscard$70$PassportActivity();
            }
        }
    }

    /* renamed from: org.telegram.ui.SetAdminsActivity$2 */
    class CLASSNAME extends ActionBarMenuItemSearchListener {
        CLASSNAME() {
        }

        public void onSearchExpand() {
            SetAdminsActivity.this.searching = true;
            SetAdminsActivity.this.listView.setEmptyView(SetAdminsActivity.this.emptyView);
        }

        public void onSearchCollapse() {
            SetAdminsActivity.this.searching = false;
            SetAdminsActivity.this.searchWas = false;
            if (SetAdminsActivity.this.listView != null) {
                SetAdminsActivity.this.listView.setEmptyView(null);
                SetAdminsActivity.this.emptyView.setVisibility(8);
                if (SetAdminsActivity.this.listView.getAdapter() != SetAdminsActivity.this.listAdapter) {
                    SetAdminsActivity.this.listView.setAdapter(SetAdminsActivity.this.listAdapter);
                }
                SetAdminsActivity.this.fragmentView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
            }
            if (SetAdminsActivity.this.searchAdapter != null) {
                SetAdminsActivity.this.searchAdapter.search(null);
            }
        }

        public void onTextChanged(EditText editText) {
            String text = editText.getText().toString();
            if (text.length() != 0) {
                SetAdminsActivity.this.searchWas = true;
                if (!(SetAdminsActivity.this.searchAdapter == null || SetAdminsActivity.this.listView.getAdapter() == SetAdminsActivity.this.searchAdapter)) {
                    SetAdminsActivity.this.listView.setAdapter(SetAdminsActivity.this.searchAdapter);
                    SetAdminsActivity.this.fragmentView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                }
                if (!(SetAdminsActivity.this.emptyView == null || SetAdminsActivity.this.listView.getEmptyView() == SetAdminsActivity.this.emptyView)) {
                    SetAdminsActivity.this.emptyView.showTextView();
                    SetAdminsActivity.this.listView.setEmptyView(SetAdminsActivity.this.emptyView);
                }
            }
            if (SetAdminsActivity.this.searchAdapter != null) {
                SetAdminsActivity.this.searchAdapter.search(text);
            }
        }
    }

    /* renamed from: org.telegram.ui.SetAdminsActivity$ListAdapter */
    private class ListAdapter extends SelectionAdapter {
        private Context mContext;

        public ListAdapter(Context context) {
            this.mContext = context;
        }

        public boolean isEnabled(ViewHolder holder) {
            int position = holder.getAdapterPosition();
            if (position == SetAdminsActivity.this.allAdminsRow) {
                return true;
            }
            if (position < SetAdminsActivity.this.usersStartRow || position >= SetAdminsActivity.this.usersEndRow || (((ChatParticipant) SetAdminsActivity.this.participants.get(position - SetAdminsActivity.this.usersStartRow)) instanceof TL_chatParticipantCreator)) {
                return false;
            }
            return true;
        }

        public int getItemCount() {
            return SetAdminsActivity.this.rowCount;
        }

        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view;
            switch (viewType) {
                case 0:
                    view = new TextCheckCell(this.mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case 1:
                    view = new TextInfoPrivacyCell(this.mContext);
                    break;
                default:
                    view = new UserCell(this.mContext, 1, 2, false);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
            }
            return new Holder(view);
        }

        public void onBindViewHolder(ViewHolder holder, int position) {
            boolean z = true;
            boolean z2 = false;
            switch (holder.getItemViewType()) {
                case 0:
                    TextCheckCell checkCell = holder.itemView;
                    SetAdminsActivity.this.chat = MessagesController.getInstance(SetAdminsActivity.this.currentAccount).getChat(Integer.valueOf(SetAdminsActivity.this.chat_id));
                    String string = LocaleController.getString("SetAdminsAll", R.string.SetAdminsAll);
                    if (SetAdminsActivity.this.chat == null || SetAdminsActivity.this.chat.admins_enabled) {
                        z = false;
                    }
                    checkCell.setTextAndCheck(string, z, false);
                    return;
                case 1:
                    TextInfoPrivacyCell privacyCell = holder.itemView;
                    if (position == SetAdminsActivity.this.allAdminsInfoRow) {
                        if (SetAdminsActivity.this.chat.admins_enabled) {
                            privacyCell.setText(LocaleController.getString("SetAdminsNotAllInfo", R.string.SetAdminsNotAllInfo));
                        } else {
                            privacyCell.setText(LocaleController.getString("SetAdminsAllInfo", R.string.SetAdminsAllInfo));
                        }
                        if (SetAdminsActivity.this.usersStartRow != -1) {
                            privacyCell.setBackgroundDrawable(Theme.getThemedDrawable(this.mContext, (int) R.drawable.greydivider, Theme.key_windowBackgroundGrayShadow));
                            return;
                        } else {
                            privacyCell.setBackgroundDrawable(Theme.getThemedDrawable(this.mContext, (int) R.drawable.greydivider_bottom, Theme.key_windowBackgroundGrayShadow));
                            return;
                        }
                    } else if (position == SetAdminsActivity.this.usersEndRow) {
                        privacyCell.setText(TtmlNode.ANONYMOUS_REGION_ID);
                        privacyCell.setBackgroundDrawable(Theme.getThemedDrawable(this.mContext, (int) R.drawable.greydivider_bottom, Theme.key_windowBackgroundGrayShadow));
                        return;
                    } else {
                        return;
                    }
                case 2:
                    boolean z3;
                    UserCell userCell = holder.itemView;
                    ChatParticipant part = (ChatParticipant) SetAdminsActivity.this.participants.get(position - SetAdminsActivity.this.usersStartRow);
                    userCell.setData(MessagesController.getInstance(SetAdminsActivity.this.currentAccount).getUser(Integer.valueOf(part.user_id)), null, null, 0);
                    SetAdminsActivity.this.chat = MessagesController.getInstance(SetAdminsActivity.this.currentAccount).getChat(Integer.valueOf(SetAdminsActivity.this.chat_id));
                    if ((part instanceof TL_chatParticipant) && (SetAdminsActivity.this.chat == null || SetAdminsActivity.this.chat.admins_enabled)) {
                        z3 = false;
                    } else {
                        z3 = true;
                    }
                    userCell.setChecked(z3, false);
                    if (SetAdminsActivity.this.chat == null || !SetAdminsActivity.this.chat.admins_enabled || part.user_id == UserConfig.getInstance(SetAdminsActivity.this.currentAccount).getClientUserId()) {
                        z2 = true;
                    }
                    userCell.setCheckDisabled(z2);
                    return;
                default:
                    return;
            }
        }

        public int getItemViewType(int i) {
            if (i == SetAdminsActivity.this.allAdminsRow) {
                return 0;
            }
            if (i == SetAdminsActivity.this.allAdminsInfoRow || i == SetAdminsActivity.this.usersEndRow) {
                return 1;
            }
            return 2;
        }
    }

    /* renamed from: org.telegram.ui.SetAdminsActivity$SearchAdapter */
    public class SearchAdapter extends SelectionAdapter {
        private Context mContext;
        private ArrayList<ChatParticipant> searchResult = new ArrayList();
        private ArrayList<CharSequence> searchResultNames = new ArrayList();
        private Timer searchTimer;

        public SearchAdapter(Context context) {
            this.mContext = context;
        }

        public void search(final String query) {
            try {
                if (this.searchTimer != null) {
                    this.searchTimer.cancel();
                }
            } catch (Throwable e) {
                FileLog.m13e(e);
            }
            if (TextUtils.isEmpty(query)) {
                this.searchResult.clear();
                this.searchResultNames.clear();
                notifyDataSetChanged();
                return;
            }
            this.searchTimer = new Timer();
            this.searchTimer.schedule(new TimerTask() {
                public void run() {
                    try {
                        SearchAdapter.this.searchTimer.cancel();
                        SearchAdapter.this.searchTimer = null;
                    } catch (Throwable e) {
                        FileLog.m13e(e);
                    }
                    SearchAdapter.this.processSearch(query);
                }
            }, 200, 300);
        }

        private void processSearch(String query) {
            AndroidUtilities.runOnUIThread(new SetAdminsActivity$SearchAdapter$$Lambda$0(this, query));
        }

        final /* synthetic */ void lambda$processSearch$1$SetAdminsActivity$SearchAdapter(String query) {
            Utilities.searchQueue.postRunnable(new SetAdminsActivity$SearchAdapter$$Lambda$2(this, query, new ArrayList(SetAdminsActivity.this.participants)));
        }

        final /* synthetic */ void lambda$null$0$SetAdminsActivity$SearchAdapter(String query, ArrayList contactsCopy) {
            String search1 = query.trim().toLowerCase();
            if (search1.length() == 0) {
                updateSearchResults(new ArrayList(), new ArrayList());
                return;
            }
            String search2 = LocaleController.getInstance().getTranslitString(search1);
            if (search1.equals(search2) || search2.length() == 0) {
                search2 = null;
            }
            String[] search = new String[((search2 != null ? 1 : 0) + 1)];
            search[0] = search1;
            if (search2 != null) {
                search[1] = search2;
            }
            ArrayList<ChatParticipant> resultArray = new ArrayList();
            ArrayList<CharSequence> resultArrayNames = new ArrayList();
            for (int a = 0; a < contactsCopy.size(); a++) {
                ChatParticipant participant = (ChatParticipant) contactsCopy.get(a);
                User user = MessagesController.getInstance(SetAdminsActivity.this.currentAccount).getUser(Integer.valueOf(participant.user_id));
                if (user.var_id != UserConfig.getInstance(SetAdminsActivity.this.currentAccount).getClientUserId()) {
                    String name = ContactsController.formatName(user.first_name, user.last_name).toLowerCase();
                    String tName = LocaleController.getInstance().getTranslitString(name);
                    if (name.equals(tName)) {
                        tName = null;
                    }
                    int found = 0;
                    int length = search.length;
                    int i = 0;
                    while (i < length) {
                        String q = search[i];
                        if (name.startsWith(q) || name.contains(" " + q) || (tName != null && (tName.startsWith(q) || tName.contains(" " + q)))) {
                            found = 1;
                        } else if (user.username != null && user.username.startsWith(q)) {
                            found = 2;
                        }
                        if (found != 0) {
                            if (found == 1) {
                                resultArrayNames.add(AndroidUtilities.generateSearchName(user.first_name, user.last_name, q));
                            } else {
                                resultArrayNames.add(AndroidUtilities.generateSearchName("@" + user.username, null, "@" + q));
                            }
                            resultArray.add(participant);
                        } else {
                            i++;
                        }
                    }
                }
            }
            updateSearchResults(resultArray, resultArrayNames);
        }

        private void updateSearchResults(ArrayList<ChatParticipant> users, ArrayList<CharSequence> names) {
            AndroidUtilities.runOnUIThread(new SetAdminsActivity$SearchAdapter$$Lambda$1(this, users, names));
        }

        final /* synthetic */ void lambda$updateSearchResults$2$SetAdminsActivity$SearchAdapter(ArrayList users, ArrayList names) {
            this.searchResult = users;
            this.searchResultNames = names;
            notifyDataSetChanged();
        }

        public boolean isEnabled(ViewHolder holder) {
            return true;
        }

        public int getItemCount() {
            return this.searchResult.size();
        }

        public ChatParticipant getItem(int i) {
            if (i < 0 || i >= this.searchResult.size()) {
                return null;
            }
            return (ChatParticipant) this.searchResult.get(i);
        }

        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new Holder(new UserCell(this.mContext, 1, 2, false));
        }

        public void onBindViewHolder(ViewHolder holder, int position) {
            boolean z;
            boolean z2 = false;
            ChatParticipant participant = getItem(position);
            User user = MessagesController.getInstance(SetAdminsActivity.this.currentAccount).getUser(Integer.valueOf(participant.user_id));
            String un = user.username;
            CharSequence username = null;
            CharSequence name = null;
            if (position < this.searchResult.size()) {
                name = (CharSequence) this.searchResultNames.get(position);
                if (name != null && un != null && un.length() > 0 && name.toString().startsWith("@" + un)) {
                    username = name;
                    name = null;
                }
            }
            UserCell userCell = holder.itemView;
            userCell.setData(user, name, username, 0);
            SetAdminsActivity.this.chat = MessagesController.getInstance(SetAdminsActivity.this.currentAccount).getChat(Integer.valueOf(SetAdminsActivity.this.chat_id));
            if ((participant instanceof TL_chatParticipant) && (SetAdminsActivity.this.chat == null || SetAdminsActivity.this.chat.admins_enabled)) {
                z = false;
            } else {
                z = true;
            }
            userCell.setChecked(z, false);
            if (SetAdminsActivity.this.chat == null || !SetAdminsActivity.this.chat.admins_enabled || participant.user_id == UserConfig.getInstance(SetAdminsActivity.this.currentAccount).getClientUserId()) {
                z2 = true;
            }
            userCell.setCheckDisabled(z2);
        }

        public int getItemViewType(int i) {
            return 0;
        }
    }

    public SetAdminsActivity(Bundle args) {
        super(args);
        this.chat_id = args.getInt("chat_id");
    }

    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.chatInfoDidLoad);
        NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.updateInterfaces);
        return true;
    }

    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.chatInfoDidLoad);
        NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.updateInterfaces);
    }

    public View createView(Context context) {
        this.searching = false;
        this.searchWas = false;
        this.actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        this.actionBar.setAllowOverlayTitle(true);
        this.actionBar.setTitle(LocaleController.getString("SetAdminsTitle", R.string.SetAdminsTitle));
        this.actionBar.setActionBarMenuOnItemClick(new CLASSNAME());
        this.searchItem = this.actionBar.createMenu().addItem(0, (int) R.drawable.ic_ab_search).setIsSearchField(true).setActionBarMenuItemSearchListener(new CLASSNAME());
        this.searchItem.setSearchFieldHint(LocaleController.getString("Search", R.string.Search));
        this.listAdapter = new ListAdapter(context);
        this.searchAdapter = new SearchAdapter(context);
        this.fragmentView = new FrameLayout(context);
        FrameLayout frameLayout = this.fragmentView;
        this.fragmentView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
        this.listView = new RecyclerListView(context);
        this.listView.setLayoutManager(new LinearLayoutManager(context, 1, false));
        this.listView.setVerticalScrollBarEnabled(false);
        frameLayout.addView(this.listView, LayoutHelper.createFrame(-1, -1.0f));
        this.listView.setAdapter(this.listAdapter);
        this.listView.setOnItemClickListener(new SetAdminsActivity$$Lambda$0(this));
        this.emptyView = new EmptyTextProgressView(context);
        this.emptyView.setVisibility(8);
        this.emptyView.setShowAtCenter(true);
        this.emptyView.setText(LocaleController.getString("NoResult", R.string.NoResult));
        frameLayout.addView(this.emptyView, LayoutHelper.createFrame(-1, -1.0f));
        this.emptyView.showTextView();
        updateRowsIds();
        return this.fragmentView;
    }

    final /* synthetic */ void lambda$createView$0$SetAdminsActivity(View view, int position) {
        boolean z = true;
        boolean z2;
        if (view instanceof UserCell) {
            ChatParticipant participant;
            UserCell userCell = (UserCell) view;
            this.chat = MessagesController.getInstance(this.currentAccount).getChat(Integer.valueOf(this.chat_id));
            int index = -1;
            if (this.listView.getAdapter() == this.searchAdapter) {
                participant = this.searchAdapter.getItem(position);
                if (participant != null) {
                    for (int a = 0; a < this.participants.size(); a++) {
                        if (((ChatParticipant) this.participants.get(a)).user_id == participant.user_id) {
                            index = a;
                            break;
                        }
                    }
                } else {
                    return;
                }
            }
            index = position - this.usersStartRow;
            if (index >= 0 && index < this.participants.size()) {
                participant = (ChatParticipant) this.participants.get(index);
            } else {
                return;
            }
            if (index != -1 && !(participant instanceof TL_chatParticipantCreator)) {
                ChatParticipant newParticipant;
                if (participant instanceof TL_chatParticipant) {
                    newParticipant = new TL_chatParticipantAdmin();
                    newParticipant.user_id = participant.user_id;
                    newParticipant.date = participant.date;
                    newParticipant.inviter_id = participant.inviter_id;
                } else {
                    newParticipant = new TL_chatParticipant();
                    newParticipant.user_id = participant.user_id;
                    newParticipant.date = participant.date;
                    newParticipant.inviter_id = participant.inviter_id;
                }
                this.participants.set(index, newParticipant);
                index = this.info.participants.participants.indexOf(participant);
                if (index != -1) {
                    this.info.participants.participants.set(index, newParticipant);
                }
                if (this.listView.getAdapter() == this.searchAdapter) {
                    this.searchAdapter.searchResult.set(position, newParticipant);
                }
                participant = newParticipant;
                z2 = ((participant instanceof TL_chatParticipant) && (this.chat == null || this.chat.admins_enabled)) ? false : true;
                userCell.setChecked(z2, true);
                if (this.chat != null && this.chat.admins_enabled) {
                    MessagesController instance = MessagesController.getInstance(this.currentAccount);
                    int i = this.chat_id;
                    int i2 = participant.user_id;
                    if (participant instanceof TL_chatParticipant) {
                        z = false;
                    }
                    instance.toggleUserAdmin(i, i2, z);
                }
            }
        } else if (position == this.allAdminsRow) {
            this.chat = MessagesController.getInstance(this.currentAccount).getChat(Integer.valueOf(this.chat_id));
            if (this.chat != null) {
                Chat chat = this.chat;
                if (this.chat.admins_enabled) {
                    z2 = false;
                } else {
                    z2 = true;
                }
                chat.admins_enabled = z2;
                TextCheckCell textCheckCell = (TextCheckCell) view;
                if (this.chat.admins_enabled) {
                    z = false;
                }
                textCheckCell.setChecked(z);
                MessagesController.getInstance(this.currentAccount).toggleAdminMode(this.chat_id, this.chat.admins_enabled);
            }
        }
    }

    public void didReceivedNotification(int id, int account, Object... args) {
        if (id == NotificationCenter.chatInfoDidLoad) {
            ChatFull chatFull = args[0];
            if (chatFull.var_id == this.chat_id) {
                this.info = chatFull;
                updateChatParticipants();
                updateRowsIds();
            }
        } else if (id == NotificationCenter.updateInterfaces) {
            int mask = ((Integer) args[0]).intValue();
            if (((mask & 2) != 0 || (mask & 1) != 0 || (mask & 4) != 0) && this.listView != null) {
                int count = this.listView.getChildCount();
                for (int a = 0; a < count; a++) {
                    View child = this.listView.getChildAt(a);
                    if (child instanceof UserCell) {
                        ((UserCell) child).update(mask);
                    }
                }
            }
        }
    }

    public void onResume() {
        super.onResume();
        if (this.listAdapter != null) {
            this.listAdapter.notifyDataSetChanged();
        }
    }

    public void setChatInfo(ChatFull chatParticipants) {
        this.info = chatParticipants;
        updateChatParticipants();
    }

    private int getChatAdminParticipantType(ChatParticipant participant) {
        if (participant instanceof TL_chatParticipantCreator) {
            return 0;
        }
        if (participant instanceof TL_chatParticipantAdmin) {
            return 1;
        }
        return 2;
    }

    private void updateChatParticipants() {
        if (this.info != null && this.participants.size() != this.info.participants.participants.size()) {
            this.participants.clear();
            this.participants.addAll(this.info.participants.participants);
            try {
                Collections.sort(this.participants, new SetAdminsActivity$$Lambda$1(this));
            } catch (Throwable e) {
                FileLog.m13e(e);
            }
        }
    }

    final /* synthetic */ int lambda$updateChatParticipants$1$SetAdminsActivity(ChatParticipant lhs, ChatParticipant rhs) {
        int type1 = getChatAdminParticipantType(lhs);
        int type2 = getChatAdminParticipantType(rhs);
        if (type1 > type2) {
            return 1;
        }
        if (type1 < type2) {
            return -1;
        }
        if (type1 == type2) {
            User user1 = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(rhs.user_id));
            User user2 = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(lhs.user_id));
            int status1 = 0;
            int status2 = 0;
            if (!(user1 == null || user1.status == null)) {
                status1 = user1.status.expires;
            }
            if (!(user2 == null || user2.status == null)) {
                status2 = user2.status.expires;
            }
            if (status1 <= 0 || status2 <= 0) {
                if (status1 >= 0 || status2 >= 0) {
                    if ((status1 < 0 && status2 > 0) || (status1 == 0 && status2 != 0)) {
                        return -1;
                    }
                    if (status2 < 0 && status1 > 0) {
                        return 1;
                    }
                    if (status2 == 0 && status1 != 0) {
                        return 1;
                    }
                } else if (status1 > status2) {
                    return 1;
                } else {
                    if (status1 < status2) {
                        return -1;
                    }
                    return 0;
                }
            } else if (status1 > status2) {
                return 1;
            } else {
                if (status1 < status2) {
                    return -1;
                }
                return 0;
            }
        }
        return 0;
    }

    private void updateRowsIds() {
        this.rowCount = 0;
        int i = this.rowCount;
        this.rowCount = i + 1;
        this.allAdminsRow = i;
        i = this.rowCount;
        this.rowCount = i + 1;
        this.allAdminsInfoRow = i;
        if (this.info != null) {
            this.usersStartRow = this.rowCount;
            this.rowCount += this.participants.size();
            i = this.rowCount;
            this.rowCount = i + 1;
            this.usersEndRow = i;
            if (!(this.searchItem == null || this.searchWas)) {
                this.searchItem.setVisibility(0);
            }
        } else {
            this.usersStartRow = -1;
            this.usersEndRow = -1;
            if (this.searchItem != null) {
                this.searchItem.setVisibility(8);
            }
        }
        if (this.listAdapter != null) {
            this.listAdapter.notifyDataSetChanged();
        }
    }

    public ThemeDescription[] getThemeDescriptions() {
        ThemeDescriptionDelegate cellDelegate = new SetAdminsActivity$$Lambda$2(this);
        r10 = new ThemeDescription[32];
        r10[0] = new ThemeDescription(this.listView, ThemeDescription.FLAG_CELLBACKGROUNDCOLOR, new Class[]{TextCheckCell.class, UserCell.class}, null, null, null, Theme.key_windowBackgroundWhite);
        r10[1] = new ThemeDescription(this.fragmentView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_windowBackgroundGray);
        r10[2] = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_actionBarDefault);
        r10[3] = new ThemeDescription(this.listView, ThemeDescription.FLAG_LISTGLOWCOLOR, null, null, null, null, Theme.key_actionBarDefault);
        r10[4] = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_ITEMSCOLOR, null, null, null, null, Theme.key_actionBarDefaultIcon);
        r10[5] = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_TITLECOLOR, null, null, null, null, Theme.key_actionBarDefaultTitle);
        r10[6] = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_SELECTORCOLOR, null, null, null, null, Theme.key_actionBarDefaultSelector);
        r10[7] = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_SEARCH, null, null, null, null, Theme.key_actionBarDefaultSearch);
        r10[8] = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_SEARCHPLACEHOLDER, null, null, null, null, Theme.key_actionBarDefaultSearchPlaceholder);
        r10[9] = new ThemeDescription(this.listView, ThemeDescription.FLAG_SELECTOR, null, null, null, null, Theme.key_listSelector);
        r10[10] = new ThemeDescription(this.listView, 0, new Class[]{View.class}, Theme.dividerPaint, null, null, Theme.key_divider);
        r10[11] = new ThemeDescription(this.emptyView, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, Theme.key_emptyListPlaceholder);
        r10[12] = new ThemeDescription(this.listView, 0, new Class[]{TextCheckCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText);
        r10[13] = new ThemeDescription(this.listView, 0, new Class[]{TextCheckCell.class}, new String[]{"checkBox"}, null, null, null, Theme.key_switchTrack);
        r10[14] = new ThemeDescription(this.listView, 0, new Class[]{TextCheckCell.class}, new String[]{"checkBox"}, null, null, null, Theme.key_switchTrackChecked);
        r10[15] = new ThemeDescription(this.listView, ThemeDescription.FLAG_BACKGROUNDFILTER, new Class[]{TextInfoPrivacyCell.class}, null, null, null, Theme.key_windowBackgroundGrayShadow);
        r10[16] = new ThemeDescription(this.listView, 0, new Class[]{TextInfoPrivacyCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText4);
        r10[17] = new ThemeDescription(this.listView, 0, new Class[]{UserCell.class}, null, null, null, Theme.key_checkboxSquareUnchecked);
        r10[18] = new ThemeDescription(this.listView, 0, new Class[]{UserCell.class}, null, null, null, Theme.key_checkboxSquareDisabled);
        r10[19] = new ThemeDescription(this.listView, 0, new Class[]{UserCell.class}, null, null, null, Theme.key_checkboxSquareBackground);
        r10[20] = new ThemeDescription(this.listView, 0, new Class[]{UserCell.class}, null, null, null, Theme.key_checkboxSquareCheck);
        r10[21] = new ThemeDescription(this.listView, 0, new Class[]{UserCell.class}, new String[]{"nameTextView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText);
        r10[22] = new ThemeDescription(this.listView, 0, new Class[]{UserCell.class}, new String[]{"statusColor"}, null, null, cellDelegate, Theme.key_windowBackgroundWhiteGrayText);
        r10[23] = new ThemeDescription(this.listView, 0, new Class[]{UserCell.class}, new String[]{"statusOnlineColor"}, null, null, cellDelegate, Theme.key_windowBackgroundWhiteBlueText);
        r10[24] = new ThemeDescription(this.listView, 0, new Class[]{UserCell.class}, null, new Drawable[]{Theme.avatar_broadcastDrawable, Theme.avatar_savedDrawable}, null, Theme.key_avatar_text);
        r10[25] = new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_avatar_backgroundRed);
        r10[26] = new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_avatar_backgroundOrange);
        r10[27] = new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_avatar_backgroundViolet);
        r10[28] = new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_avatar_backgroundGreen);
        r10[29] = new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_avatar_backgroundCyan);
        r10[30] = new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_avatar_backgroundBlue);
        r10[31] = new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_avatar_backgroundPink);
        return r10;
    }

    final /* synthetic */ void lambda$getThemeDescriptions$2$SetAdminsActivity() {
        if (this.listView != null) {
            int count = this.listView.getChildCount();
            for (int a = 0; a < count; a++) {
                View child = this.listView.getChildAt(a);
                if (child instanceof UserCell) {
                    ((UserCell) child).update(0);
                }
            }
        }
    }
}
