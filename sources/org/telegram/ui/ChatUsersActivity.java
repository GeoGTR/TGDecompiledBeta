package org.telegram.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.LayoutParams;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.NotificationCenter.NotificationCenterDelegate;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.Utilities;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.ChannelParticipant;
import org.telegram.tgnet.TLRPC.ChannelParticipantsFilter;
import org.telegram.tgnet.TLRPC.Chat;
import org.telegram.tgnet.TLRPC.ChatFull;
import org.telegram.tgnet.TLRPC.ChatParticipant;
import org.telegram.tgnet.TLRPC.TL_channelFull;
import org.telegram.tgnet.TLRPC.TL_channelParticipant;
import org.telegram.tgnet.TLRPC.TL_channelParticipantAdmin;
import org.telegram.tgnet.TLRPC.TL_channelParticipantBanned;
import org.telegram.tgnet.TLRPC.TL_channelParticipantCreator;
import org.telegram.tgnet.TLRPC.TL_channelParticipantSelf;
import org.telegram.tgnet.TLRPC.TL_channelParticipantsAdmins;
import org.telegram.tgnet.TLRPC.TL_channelParticipantsBanned;
import org.telegram.tgnet.TLRPC.TL_channelParticipantsBots;
import org.telegram.tgnet.TLRPC.TL_channelParticipantsContacts;
import org.telegram.tgnet.TLRPC.TL_channelParticipantsKicked;
import org.telegram.tgnet.TLRPC.TL_channelParticipantsRecent;
import org.telegram.tgnet.TLRPC.TL_channels_channelParticipants;
import org.telegram.tgnet.TLRPC.TL_channels_editBanned;
import org.telegram.tgnet.TLRPC.TL_channels_getParticipants;
import org.telegram.tgnet.TLRPC.TL_chatAdminRights;
import org.telegram.tgnet.TLRPC.TL_chatBannedRights;
import org.telegram.tgnet.TLRPC.TL_chatParticipant;
import org.telegram.tgnet.TLRPC.TL_chatParticipantAdmin;
import org.telegram.tgnet.TLRPC.TL_chatParticipantCreator;
import org.telegram.tgnet.TLRPC.TL_error;
import org.telegram.tgnet.TLRPC.Updates;
import org.telegram.tgnet.TLRPC.User;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.AlertDialog.Builder;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.ActionBar.ThemeDescription.ThemeDescriptionDelegate;
import org.telegram.ui.Adapters.SearchAdapterHelper;
import org.telegram.ui.Adapters.SearchAdapterHelper.HashtagObject;
import org.telegram.ui.Adapters.SearchAdapterHelper.SearchAdapterHelperDelegate;
import org.telegram.ui.Adapters.SearchAdapterHelper.SearchAdapterHelperDelegate.-CC;
import org.telegram.ui.Cells.GraySectionCell;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.ManageChatTextCell;
import org.telegram.ui.Cells.ManageChatUserCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.TextCheckCell2;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.Components.EmptyTextProgressView;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.RecyclerListView.Holder;
import org.telegram.ui.Components.RecyclerListView.SelectionAdapter;

public class ChatUsersActivity extends BaseFragment implements NotificationCenterDelegate {
    public static final int TYPE_ADMIN = 1;
    public static final int TYPE_BANNED = 0;
    public static final int TYPE_KICKED = 3;
    public static final int TYPE_USERS = 2;
    private static final int done_button = 1;
    private static final int search_button = 0;
    private int addNew2Row;
    private int addNewRow;
    private int addNewSectionRow;
    private int addUsersRow;
    private int blockedEmptyRow;
    private int botEndRow;
    private int botHeaderRow;
    private int botStartRow;
    private ArrayList<TLObject> bots = new ArrayList();
    private boolean botsEndReached;
    private SparseArray<TLObject> botsMap = new SparseArray();
    private int changeInfoRow;
    private int chatId = this.arguments.getInt("chat_id");
    private ArrayList<TLObject> contacts = new ArrayList();
    private boolean contactsEndReached;
    private int contactsEndRow;
    private int contactsHeaderRow;
    private SparseArray<TLObject> contactsMap = new SparseArray();
    private int contactsStartRow;
    private Chat currentChat = MessagesController.getInstance(this.currentAccount).getChat(Integer.valueOf(this.chatId));
    private TL_chatBannedRights defaultBannedRights = new TL_chatBannedRights();
    private int delayResults;
    private ChatUsersActivityDelegate delegate;
    private ActionBarMenuItem doneItem;
    private int embedLinksRow;
    private EmptyTextProgressView emptyView;
    private boolean firstLoaded;
    private ChatFull info;
    private String initialBannedRights;
    private boolean isChannel;
    private RecyclerListView listView;
    private ListAdapter listViewAdapter;
    private boolean loadingUsers;
    private int membersHeaderRow;
    private boolean needOpenSearch = this.arguments.getBoolean("open_search");
    private ArrayList<TLObject> participants = new ArrayList();
    private int participantsDivider2Row;
    private int participantsDividerRow;
    private int participantsEndRow;
    private int participantsInfoRow;
    private SparseArray<TLObject> participantsMap = new SparseArray();
    private int participantsStartRow;
    private int permissionsSectionRow;
    private int pinMessagesRow;
    private int recentActionsRow;
    private int removedUsersRow;
    private int restricted1SectionRow;
    private int rowCount;
    private ActionBarMenuItem searchItem;
    private SearchAdapter searchListViewAdapter;
    private boolean searchWas;
    private boolean searching;
    private int selectType = this.arguments.getInt("selectType");
    private int sendMediaRow;
    private int sendMessagesRow;
    private int sendPollsRow;
    private int sendStickersRow;
    private int type = this.arguments.getInt("type");

    public interface ChatUsersActivityDelegate {
        void didAddParticipantToList(int i, TLObject tLObject);
    }

    private class ListAdapter extends SelectionAdapter {
        private Context mContext;

        public ListAdapter(Context context) {
            this.mContext = context;
        }

        public boolean isEnabled(ViewHolder viewHolder) {
            int itemViewType = viewHolder.getItemViewType();
            if (itemViewType == 7) {
                return ChatObject.canBlockUsers(ChatUsersActivity.this.currentChat);
            }
            boolean z = false;
            if (itemViewType == 0) {
                User currentUser = ((ManageChatUserCell) viewHolder.itemView).getCurrentUser();
                if (!(currentUser == null || currentUser.self)) {
                    z = true;
                }
                return z;
            }
            if (itemViewType == 0 || itemViewType == 2 || itemViewType == 6) {
                z = true;
            }
            return z;
        }

        public int getItemCount() {
            if (!ChatUsersActivity.this.loadingUsers || ChatUsersActivity.this.firstLoaded) {
                return ChatUsersActivity.this.rowCount;
            }
            return 0;
        }

        public /* synthetic */ boolean lambda$onCreateViewHolder$0$ChatUsersActivity$ListAdapter(ManageChatUserCell manageChatUserCell, boolean z) {
            return ChatUsersActivity.this.createMenuForParticipant(ChatUsersActivity.this.listViewAdapter.getItem(((Integer) manageChatUserCell.getTag()).intValue()), z ^ 1);
        }

        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View manageChatUserCell;
            String str = "windowBackgroundWhite";
            boolean z = true;
            switch (i) {
                case 0:
                    Context context = this.mContext;
                    int i2 = 6;
                    int i3 = (ChatUsersActivity.this.type == 0 || ChatUsersActivity.this.type == 3) ? 7 : 6;
                    if (!(ChatUsersActivity.this.type == 0 || ChatUsersActivity.this.type == 3)) {
                        i2 = 2;
                    }
                    if (ChatUsersActivity.this.selectType != 0) {
                        z = false;
                    }
                    manageChatUserCell = new ManageChatUserCell(context, i3, i2, z);
                    manageChatUserCell.setBackgroundColor(Theme.getColor(str));
                    manageChatUserCell.setDelegate(new -$$Lambda$ChatUsersActivity$ListAdapter$eixJJWW-1mDLHNoI-EEjSfsGLFc(this));
                    break;
                case 1:
                    manageChatUserCell = new TextInfoPrivacyCell(this.mContext);
                    break;
                case 2:
                    manageChatUserCell = new ManageChatTextCell(this.mContext);
                    manageChatUserCell.setBackgroundColor(Theme.getColor(str));
                    break;
                case 3:
                    manageChatUserCell = new ShadowSectionCell(this.mContext);
                    break;
                case 4:
                    manageChatUserCell = new FrameLayout(this.mContext) {
                        /* Access modifiers changed, original: protected */
                        public void onMeasure(int i, int i2) {
                            super.onMeasure(i, MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(i2) - AndroidUtilities.dp(56.0f), NUM));
                        }
                    };
                    manageChatUserCell.setBackgroundDrawable(Theme.getThemedDrawable(this.mContext, NUM, "windowBackgroundGrayShadow"));
                    LinearLayout linearLayout = new LinearLayout(this.mContext);
                    linearLayout.setOrientation(1);
                    manageChatUserCell.addView(linearLayout, LayoutHelper.createFrame(-2, -2.0f, 17, 20.0f, 0.0f, 20.0f, 0.0f));
                    ImageView imageView = new ImageView(this.mContext);
                    imageView.setImageResource(NUM);
                    imageView.setScaleType(ScaleType.CENTER);
                    String str2 = "emptyListPlaceholder";
                    imageView.setColorFilter(new PorterDuffColorFilter(Theme.getColor(str2), Mode.MULTIPLY));
                    linearLayout.addView(imageView, LayoutHelper.createLinear(-2, -2, 1));
                    TextView textView = new TextView(this.mContext);
                    textView.setText(LocaleController.getString("NoBlockedUsers", NUM));
                    textView.setTextColor(Theme.getColor(str2));
                    textView.setTextSize(1, 16.0f);
                    textView.setGravity(1);
                    textView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
                    linearLayout.addView(textView, LayoutHelper.createLinear(-2, -2, 1, 0, 10, 0, 0));
                    textView = new TextView(this.mContext);
                    if (ChatUsersActivity.this.isChannel) {
                        textView.setText(LocaleController.getString("NoBlockedChannel2", NUM));
                    } else {
                        textView.setText(LocaleController.getString("NoBlockedGroup2", NUM));
                    }
                    textView.setTextColor(Theme.getColor(str2));
                    textView.setTextSize(1, 15.0f);
                    textView.setGravity(1);
                    linearLayout.addView(textView, LayoutHelper.createLinear(-2, -2, 1, 0, 10, 0, 0));
                    manageChatUserCell.setLayoutParams(new LayoutParams(-1, -1));
                    break;
                case 5:
                    View headerCell = new HeaderCell(this.mContext, false, 21, 11, false);
                    headerCell.setBackgroundColor(Theme.getColor(str));
                    headerCell.setHeight(43);
                    break;
                case 6:
                    manageChatUserCell = new TextSettingsCell(this.mContext);
                    manageChatUserCell.setBackgroundColor(Theme.getColor(str));
                    break;
                case 7:
                    manageChatUserCell = new TextCheckCell2(this.mContext);
                    manageChatUserCell.setBackgroundColor(Theme.getColor(str));
                    break;
                default:
                    manageChatUserCell = new GraySectionCell(this.mContext);
                    break;
            }
            return new Holder(manageChatUserCell);
        }

        public void onBindViewHolder(ViewHolder viewHolder, int i) {
            String str = "windowBackgroundGrayShadow";
            boolean z = false;
            boolean z2;
            String access$2900;
            String string;
            switch (viewHolder.getItemViewType()) {
                case 0:
                    int i2;
                    int i3;
                    int i4;
                    TL_chatBannedRights tL_chatBannedRights;
                    boolean z3;
                    boolean z4;
                    ManageChatUserCell manageChatUserCell = (ManageChatUserCell) viewHolder.itemView;
                    manageChatUserCell.setTag(Integer.valueOf(i));
                    TLObject item = getItem(i);
                    if (item instanceof ChannelParticipant) {
                        ChannelParticipant channelParticipant = (ChannelParticipant) item;
                        i2 = channelParticipant.user_id;
                        i3 = channelParticipant.kicked_by;
                        i4 = channelParticipant.promoted_by;
                        tL_chatBannedRights = channelParticipant.banned_rights;
                        z3 = channelParticipant instanceof TL_channelParticipantBanned;
                        z4 = channelParticipant instanceof TL_channelParticipantCreator;
                        z2 = channelParticipant instanceof TL_channelParticipantAdmin;
                    } else {
                        ChatParticipant chatParticipant = (ChatParticipant) item;
                        i2 = chatParticipant.user_id;
                        z4 = chatParticipant instanceof TL_chatParticipantCreator;
                        z2 = chatParticipant instanceof TL_chatParticipantAdmin;
                        tL_chatBannedRights = null;
                        i3 = 0;
                        i4 = 0;
                        z3 = false;
                    }
                    User user = MessagesController.getInstance(ChatUsersActivity.this.currentAccount).getUser(Integer.valueOf(i2));
                    if (user == null) {
                        return;
                    }
                    CharSequence formatString;
                    if (ChatUsersActivity.this.type == 3) {
                        access$2900 = ChatUsersActivity.this.formatUserPermissions(tL_chatBannedRights);
                        if (i != ChatUsersActivity.this.participantsEndRow - 1) {
                            z = true;
                        }
                        manageChatUserCell.setData(user, null, access$2900, z);
                        return;
                    } else if (ChatUsersActivity.this.type == 0) {
                        formatString = (!z3 || MessagesController.getInstance(ChatUsersActivity.this.currentAccount).getUser(Integer.valueOf(i3)) == null) ? null : LocaleController.formatString("UserRemovedBy", NUM, ContactsController.formatName(MessagesController.getInstance(ChatUsersActivity.this.currentAccount).getUser(Integer.valueOf(i3)).first_name, MessagesController.getInstance(ChatUsersActivity.this.currentAccount).getUser(Integer.valueOf(i3)).last_name));
                        if (i != ChatUsersActivity.this.participantsEndRow - 1) {
                            z = true;
                        }
                        manageChatUserCell.setData(user, null, formatString, z);
                        return;
                    } else if (ChatUsersActivity.this.type == 1) {
                        formatString = z4 ? LocaleController.getString("ChannelCreator", NUM) : (!z2 || MessagesController.getInstance(ChatUsersActivity.this.currentAccount).getUser(Integer.valueOf(i4)) == null) ? null : LocaleController.formatString("EditAdminPromotedBy", NUM, ContactsController.formatName(MessagesController.getInstance(ChatUsersActivity.this.currentAccount).getUser(Integer.valueOf(i4)).first_name, MessagesController.getInstance(ChatUsersActivity.this.currentAccount).getUser(Integer.valueOf(i4)).last_name));
                        if (i != ChatUsersActivity.this.participantsEndRow - 1) {
                            z = true;
                        }
                        manageChatUserCell.setData(user, null, formatString, z);
                        return;
                    } else if (ChatUsersActivity.this.type == 2) {
                        if (i != ChatUsersActivity.this.participantsEndRow - 1) {
                            z = true;
                        }
                        manageChatUserCell.setData(user, null, null, z);
                        return;
                    } else {
                        return;
                    }
                case 1:
                    TextInfoPrivacyCell textInfoPrivacyCell = (TextInfoPrivacyCell) viewHolder.itemView;
                    if (i != ChatUsersActivity.this.participantsInfoRow) {
                        return;
                    }
                    if (ChatUsersActivity.this.type == 0 || ChatUsersActivity.this.type == 3) {
                        String str2 = "NoBlockedChannel2";
                        String str3 = "NoBlockedGroup2";
                        if (ChatObject.canBlockUsers(ChatUsersActivity.this.currentChat)) {
                            if (ChatUsersActivity.this.isChannel) {
                                textInfoPrivacyCell.setText(LocaleController.getString(str2, NUM));
                            } else {
                                textInfoPrivacyCell.setText(LocaleController.getString(str3, NUM));
                            }
                        } else if (ChatUsersActivity.this.isChannel) {
                            textInfoPrivacyCell.setText(LocaleController.getString(str2, NUM));
                        } else {
                            textInfoPrivacyCell.setText(LocaleController.getString(str3, NUM));
                        }
                        textInfoPrivacyCell.setBackgroundDrawable(Theme.getThemedDrawable(this.mContext, NUM, str));
                        return;
                    } else if (ChatUsersActivity.this.type == 1) {
                        if (ChatUsersActivity.this.addNewRow != -1) {
                            if (ChatUsersActivity.this.isChannel) {
                                textInfoPrivacyCell.setText(LocaleController.getString("ChannelAdminsInfo", NUM));
                            } else {
                                textInfoPrivacyCell.setText(LocaleController.getString("MegaAdminsInfo", NUM));
                            }
                            textInfoPrivacyCell.setBackgroundDrawable(Theme.getThemedDrawable(this.mContext, NUM, str));
                            return;
                        }
                        textInfoPrivacyCell.setText("");
                        textInfoPrivacyCell.setBackgroundDrawable(Theme.getThemedDrawable(this.mContext, NUM, str));
                        return;
                    } else if (ChatUsersActivity.this.type == 2) {
                        if (ChatUsersActivity.this.isChannel && ChatUsersActivity.this.selectType == 0) {
                            textInfoPrivacyCell.setText(LocaleController.getString("ChannelMembersInfo", NUM));
                        } else {
                            textInfoPrivacyCell.setText("");
                        }
                        textInfoPrivacyCell.setBackgroundDrawable(Theme.getThemedDrawable(this.mContext, NUM, str));
                        return;
                    } else {
                        return;
                    }
                case 2:
                    ManageChatTextCell manageChatTextCell = (ManageChatTextCell) viewHolder.itemView;
                    manageChatTextCell.setColors("windowBackgroundWhiteGrayIcon", "windowBackgroundWhiteBlackText");
                    if (i == ChatUsersActivity.this.addNewRow) {
                        String str4 = "windowBackgroundWhiteBlueButton";
                        str = "windowBackgroundWhiteBlueIcon";
                        if (ChatUsersActivity.this.type == 3) {
                            manageChatTextCell.setColors(str, str4);
                            string = LocaleController.getString("ChannelAddException", NUM);
                            if (ChatUsersActivity.this.participantsStartRow != -1) {
                                z = true;
                            }
                            manageChatTextCell.setText(string, null, NUM, z);
                            return;
                        } else if (ChatUsersActivity.this.type == 0) {
                            manageChatTextCell.setText(LocaleController.getString("ChannelBlockUser", NUM), null, NUM, false);
                            return;
                        } else if (ChatUsersActivity.this.type == 1) {
                            manageChatTextCell.setColors(str, str4);
                            manageChatTextCell.setText(LocaleController.getString("ChannelAddAdmin", NUM), null, NUM, true);
                            return;
                        } else if (ChatUsersActivity.this.type == 2) {
                            manageChatTextCell.setColors(str, str4);
                            if (ChatUsersActivity.this.isChannel) {
                                manageChatTextCell.setText(LocaleController.getString("AddSubscriber", NUM), null, NUM, true);
                                return;
                            } else {
                                manageChatTextCell.setText(LocaleController.getString("AddMember", NUM), null, NUM, true);
                                return;
                            }
                        } else {
                            return;
                        }
                    } else if (i == ChatUsersActivity.this.recentActionsRow) {
                        manageChatTextCell.setText(LocaleController.getString("EventLog", NUM), null, NUM, false);
                        return;
                    } else if (i == ChatUsersActivity.this.addNew2Row) {
                        manageChatTextCell.setText(LocaleController.getString("ChannelInviteViaLink", NUM), null, NUM, false);
                        return;
                    } else {
                        return;
                    }
                case 3:
                    if (i == ChatUsersActivity.this.addNewSectionRow || (ChatUsersActivity.this.type == 3 && i == ChatUsersActivity.this.participantsDividerRow && ChatUsersActivity.this.addNewRow == -1 && ChatUsersActivity.this.participantsStartRow == -1)) {
                        viewHolder.itemView.setBackgroundDrawable(Theme.getThemedDrawable(this.mContext, NUM, str));
                        return;
                    } else {
                        viewHolder.itemView.setBackgroundDrawable(Theme.getThemedDrawable(this.mContext, NUM, str));
                        return;
                    }
                case 5:
                    HeaderCell headerCell = (HeaderCell) viewHolder.itemView;
                    if (i == ChatUsersActivity.this.restricted1SectionRow) {
                        if (ChatUsersActivity.this.type == 0) {
                            i = ChatUsersActivity.this.info != null ? ChatUsersActivity.this.info.kicked_count : ChatUsersActivity.this.participants.size();
                            if (i != 0) {
                                headerCell.setText(LocaleController.formatPluralString("RemovedUser", i));
                                return;
                            } else {
                                headerCell.setText(LocaleController.getString("ChannelBlockedUsers", NUM));
                                return;
                            }
                        }
                        headerCell.setText(LocaleController.getString("ChannelRestrictedUsers", NUM));
                        return;
                    } else if (i == ChatUsersActivity.this.permissionsSectionRow) {
                        headerCell.setText(LocaleController.getString("ChannelPermissionsHeader", NUM));
                        return;
                    } else {
                        return;
                    }
                case 6:
                    TextSettingsCell textSettingsCell = (TextSettingsCell) viewHolder.itemView;
                    string = LocaleController.getString("ChannelBlacklist", NUM);
                    Object[] objArr = new Object[1];
                    objArr[0] = Integer.valueOf(ChatUsersActivity.this.info != null ? ChatUsersActivity.this.info.kicked_count : 0);
                    textSettingsCell.setTextAndValue(string, String.format("%d", objArr), false);
                    return;
                case 7:
                    TextCheckCell2 textCheckCell2 = (TextCheckCell2) viewHolder.itemView;
                    boolean z5;
                    if (i == ChatUsersActivity.this.changeInfoRow) {
                        access$2900 = LocaleController.getString("UserRestrictionsChangeInfo", NUM);
                        z5 = !ChatUsersActivity.this.defaultBannedRights.change_info && TextUtils.isEmpty(ChatUsersActivity.this.currentChat.username);
                        textCheckCell2.setTextAndCheck(access$2900, z5, false);
                    } else if (i == ChatUsersActivity.this.addUsersRow) {
                        textCheckCell2.setTextAndCheck(LocaleController.getString("UserRestrictionsInviteUsers", NUM), ChatUsersActivity.this.defaultBannedRights.invite_users ^ 1, true);
                    } else if (i == ChatUsersActivity.this.pinMessagesRow) {
                        access$2900 = LocaleController.getString("UserRestrictionsPinMessages", NUM);
                        z5 = !ChatUsersActivity.this.defaultBannedRights.pin_messages && TextUtils.isEmpty(ChatUsersActivity.this.currentChat.username);
                        textCheckCell2.setTextAndCheck(access$2900, z5, true);
                    } else if (i == ChatUsersActivity.this.sendMessagesRow) {
                        textCheckCell2.setTextAndCheck(LocaleController.getString("UserRestrictionsSend", NUM), ChatUsersActivity.this.defaultBannedRights.send_messages ^ 1, true);
                    } else if (i == ChatUsersActivity.this.sendMediaRow) {
                        textCheckCell2.setTextAndCheck(LocaleController.getString("UserRestrictionsSendMedia", NUM), ChatUsersActivity.this.defaultBannedRights.send_media ^ 1, true);
                    } else if (i == ChatUsersActivity.this.sendStickersRow) {
                        textCheckCell2.setTextAndCheck(LocaleController.getString("UserRestrictionsSendStickers", NUM), ChatUsersActivity.this.defaultBannedRights.send_stickers ^ 1, true);
                    } else if (i == ChatUsersActivity.this.embedLinksRow) {
                        textCheckCell2.setTextAndCheck(LocaleController.getString("UserRestrictionsEmbedLinks", NUM), ChatUsersActivity.this.defaultBannedRights.embed_links ^ 1, true);
                    } else if (i == ChatUsersActivity.this.sendPollsRow) {
                        textCheckCell2.setTextAndCheck(LocaleController.getString("UserRestrictionsSendPolls", NUM), ChatUsersActivity.this.defaultBannedRights.send_polls ^ 1, true);
                    }
                    if (i == ChatUsersActivity.this.sendMediaRow || i == ChatUsersActivity.this.sendStickersRow || i == ChatUsersActivity.this.embedLinksRow || i == ChatUsersActivity.this.sendPollsRow) {
                        z2 = (ChatUsersActivity.this.defaultBannedRights.send_messages || ChatUsersActivity.this.defaultBannedRights.view_messages) ? false : true;
                        textCheckCell2.setEnabled(z2);
                    } else if (i == ChatUsersActivity.this.sendMessagesRow) {
                        textCheckCell2.setEnabled(ChatUsersActivity.this.defaultBannedRights.view_messages ^ 1);
                    }
                    if (!ChatObject.canBlockUsers(ChatUsersActivity.this.currentChat)) {
                        textCheckCell2.setIcon(0);
                        return;
                    } else if ((i != ChatUsersActivity.this.addUsersRow || ChatObject.canUserDoAdminAction(ChatUsersActivity.this.currentChat, 3)) && ((i != ChatUsersActivity.this.pinMessagesRow || ChatObject.canUserDoAdminAction(ChatUsersActivity.this.currentChat, 0)) && ((i != ChatUsersActivity.this.changeInfoRow || ChatObject.canUserDoAdminAction(ChatUsersActivity.this.currentChat, 1)) && (TextUtils.isEmpty(ChatUsersActivity.this.currentChat.username) || !(i == ChatUsersActivity.this.pinMessagesRow || i == ChatUsersActivity.this.changeInfoRow))))) {
                        textCheckCell2.setIcon(0);
                        return;
                    } else {
                        textCheckCell2.setIcon(NUM);
                        return;
                    }
                case 8:
                    GraySectionCell graySectionCell = (GraySectionCell) viewHolder.itemView;
                    if (i == ChatUsersActivity.this.membersHeaderRow) {
                        if (!ChatObject.isChannel(ChatUsersActivity.this.currentChat) || ChatUsersActivity.this.currentChat.megagroup) {
                            graySectionCell.setText(LocaleController.getString("ChannelOtherMembers", NUM));
                            return;
                        } else {
                            graySectionCell.setText(LocaleController.getString("ChannelOtherSubscribers", NUM));
                            return;
                        }
                    } else if (i == ChatUsersActivity.this.botHeaderRow) {
                        graySectionCell.setText(LocaleController.getString("ChannelBots", NUM));
                        return;
                    } else if (i != ChatUsersActivity.this.contactsHeaderRow) {
                        return;
                    } else {
                        if (!ChatObject.isChannel(ChatUsersActivity.this.currentChat) || ChatUsersActivity.this.currentChat.megagroup) {
                            graySectionCell.setText(LocaleController.getString("GroupContacts", NUM));
                            return;
                        } else {
                            graySectionCell.setText(LocaleController.getString("ChannelContacts", NUM));
                            return;
                        }
                    }
                default:
                    return;
            }
        }

        public void onViewRecycled(ViewHolder viewHolder) {
            View view = viewHolder.itemView;
            if (view instanceof ManageChatUserCell) {
                ((ManageChatUserCell) view).recycle();
            }
        }

        public int getItemViewType(int i) {
            if (i == ChatUsersActivity.this.addNewRow || i == ChatUsersActivity.this.addNew2Row || i == ChatUsersActivity.this.recentActionsRow) {
                return 2;
            }
            if ((i >= ChatUsersActivity.this.participantsStartRow && i < ChatUsersActivity.this.participantsEndRow) || ((i >= ChatUsersActivity.this.botStartRow && i < ChatUsersActivity.this.botEndRow) || (i >= ChatUsersActivity.this.contactsStartRow && i < ChatUsersActivity.this.contactsEndRow))) {
                return 0;
            }
            if (i == ChatUsersActivity.this.addNewSectionRow || i == ChatUsersActivity.this.participantsDividerRow || i == ChatUsersActivity.this.participantsDivider2Row) {
                return 3;
            }
            if (i == ChatUsersActivity.this.restricted1SectionRow || i == ChatUsersActivity.this.permissionsSectionRow) {
                return 5;
            }
            if (i == ChatUsersActivity.this.participantsInfoRow) {
                return 1;
            }
            if (i == ChatUsersActivity.this.blockedEmptyRow) {
                return 4;
            }
            if (i == ChatUsersActivity.this.removedUsersRow) {
                return 6;
            }
            if (i == ChatUsersActivity.this.changeInfoRow || i == ChatUsersActivity.this.addUsersRow || i == ChatUsersActivity.this.pinMessagesRow || i == ChatUsersActivity.this.sendMessagesRow || i == ChatUsersActivity.this.sendMediaRow || i == ChatUsersActivity.this.sendStickersRow || i == ChatUsersActivity.this.embedLinksRow || i == ChatUsersActivity.this.sendPollsRow) {
                return 7;
            }
            if (i == ChatUsersActivity.this.membersHeaderRow || i == ChatUsersActivity.this.contactsHeaderRow || i == ChatUsersActivity.this.botHeaderRow) {
                return 8;
            }
            return 0;
        }

        public TLObject getItem(int i) {
            if (i >= ChatUsersActivity.this.participantsStartRow && i < ChatUsersActivity.this.participantsEndRow) {
                return (TLObject) ChatUsersActivity.this.participants.get(i - ChatUsersActivity.this.participantsStartRow);
            }
            if (i < ChatUsersActivity.this.contactsStartRow || i >= ChatUsersActivity.this.contactsEndRow) {
                return (i < ChatUsersActivity.this.botStartRow || i >= ChatUsersActivity.this.botEndRow) ? null : (TLObject) ChatUsersActivity.this.bots.get(i - ChatUsersActivity.this.botStartRow);
            } else {
                return (TLObject) ChatUsersActivity.this.contacts.get(i - ChatUsersActivity.this.contactsStartRow);
            }
        }
    }

    private class SearchAdapter extends SelectionAdapter {
        private int contactsStartRow;
        private int globalStartRow;
        private int groupStartRow;
        private Context mContext;
        private SearchAdapterHelper searchAdapterHelper;
        private ArrayList<TLObject> searchResult = new ArrayList();
        private ArrayList<CharSequence> searchResultNames = new ArrayList();
        private Timer searchTimer;
        private int totalCount;

        public SearchAdapter(Context context) {
            this.mContext = context;
            this.searchAdapterHelper = new SearchAdapterHelper(true);
            this.searchAdapterHelper.setDelegate(new SearchAdapterHelperDelegate(ChatUsersActivity.this) {
                public /* synthetic */ SparseArray<User> getExcludeUsers() {
                    return -CC.$default$getExcludeUsers(this);
                }

                public void onSetHashtags(ArrayList<HashtagObject> arrayList, HashMap<String, HashtagObject> hashMap) {
                }

                public void onDataSetChanged() {
                    SearchAdapter.this.notifyDataSetChanged();
                }
            });
        }

        public void searchDialogs(final String str) {
            try {
                if (this.searchTimer != null) {
                    this.searchTimer.cancel();
                }
            } catch (Exception e) {
                FileLog.e(e);
            }
            if (TextUtils.isEmpty(str)) {
                this.searchResult.clear();
                this.searchResultNames.clear();
                this.searchAdapterHelper.mergeResults(null);
                this.searchAdapterHelper.queryServerSearch(null, ChatUsersActivity.this.type != 0, false, true, false, ChatObject.isChannel(ChatUsersActivity.this.currentChat) ? ChatUsersActivity.this.chatId : 0, ChatUsersActivity.this.type);
                notifyDataSetChanged();
                return;
            }
            this.searchTimer = new Timer();
            this.searchTimer.schedule(new TimerTask() {
                public void run() {
                    try {
                        SearchAdapter.this.searchTimer.cancel();
                        SearchAdapter.this.searchTimer = null;
                    } catch (Exception e) {
                        FileLog.e(e);
                    }
                    SearchAdapter.this.processSearch(str);
                }
            }, 200, 300);
        }

        private void processSearch(String str) {
            AndroidUtilities.runOnUIThread(new -$$Lambda$ChatUsersActivity$SearchAdapter$8wLJrzbgJTNGMYxrB39CvgXIq50(this, str));
        }

        public /* synthetic */ void lambda$processSearch$1$ChatUsersActivity$SearchAdapter(String str) {
            ArrayList arrayList = null;
            ArrayList arrayList2 = (ChatObject.isChannel(ChatUsersActivity.this.currentChat) || ChatUsersActivity.this.info == null) ? null : new ArrayList(ChatUsersActivity.this.info.participants.participants);
            if (ChatUsersActivity.this.selectType == 1) {
                arrayList = new ArrayList(ContactsController.getInstance(ChatUsersActivity.this.currentAccount).contacts);
            }
            this.searchAdapterHelper.queryServerSearch(str, ChatUsersActivity.this.selectType != 0, false, true, false, ChatObject.isChannel(ChatUsersActivity.this.currentChat) ? ChatUsersActivity.this.chatId : 0, ChatUsersActivity.this.type);
            if (arrayList2 != null || arrayList != null) {
                Utilities.searchQueue.postRunnable(new -$$Lambda$ChatUsersActivity$SearchAdapter$tzi0wgzIkIjs_rrUbYg-604OKoQ(this, str, arrayList2, arrayList));
            }
        }

        /* JADX WARNING: Removed duplicated region for block: B:51:0x0143 A:{LOOP_END, LOOP:1: B:27:0x00b5->B:51:0x0143} */
        /* JADX WARNING: Removed duplicated region for block: B:94:0x0108 A:{SYNTHETIC} */
        /* JADX WARNING: Removed duplicated region for block: B:86:0x0236 A:{LOOP_END, LOOP:3: B:64:0x01ae->B:86:0x0236} */
        /* JADX WARNING: Removed duplicated region for block: B:99:0x01fa A:{SYNTHETIC} */
        /* JADX WARNING: Missing block: B:36:0x00f1, code skipped:
            if (r15.contains(r6.toString()) != false) goto L_0x0105;
     */
        /* JADX WARNING: Missing block: B:73:0x01e8, code skipped:
            if (r7.contains(r2.toString()) != false) goto L_0x01f7;
     */
        public /* synthetic */ void lambda$null$0$ChatUsersActivity$SearchAdapter(java.lang.String r20, java.util.ArrayList r21, java.util.ArrayList r22) {
            /*
            r19 = this;
            r0 = r19;
            r1 = r21;
            r2 = r22;
            r3 = r20.trim();
            r3 = r3.toLowerCase();
            r4 = r3.length();
            if (r4 != 0) goto L_0x0027;
        L_0x0014:
            r1 = new java.util.ArrayList;
            r1.<init>();
            r2 = new java.util.ArrayList;
            r2.<init>();
            r3 = new java.util.ArrayList;
            r3.<init>();
            r0.updateSearchResults(r1, r2, r3);
            return;
        L_0x0027:
            r4 = org.telegram.messenger.LocaleController.getInstance();
            r4 = r4.getTranslitString(r3);
            r5 = r3.equals(r4);
            if (r5 != 0) goto L_0x003b;
        L_0x0035:
            r5 = r4.length();
            if (r5 != 0) goto L_0x003c;
        L_0x003b:
            r4 = 0;
        L_0x003c:
            r5 = 0;
            r7 = 1;
            if (r4 == 0) goto L_0x0042;
        L_0x0040:
            r8 = 1;
            goto L_0x0043;
        L_0x0042:
            r8 = 0;
        L_0x0043:
            r8 = r8 + r7;
            r8 = new java.lang.String[r8];
            r8[r5] = r3;
            if (r4 == 0) goto L_0x004c;
        L_0x004a:
            r8[r7] = r4;
        L_0x004c:
            r3 = new java.util.ArrayList;
            r3.<init>();
            r4 = new java.util.ArrayList;
            r4.<init>();
            r9 = new java.util.ArrayList;
            r9.<init>();
            r11 = "@";
            r12 = " ";
            if (r1 == 0) goto L_0x0157;
        L_0x0061:
            r13 = 0;
        L_0x0062:
            r14 = r21.size();
            if (r13 >= r14) goto L_0x0157;
        L_0x0068:
            r14 = r1.get(r13);
            r14 = (org.telegram.tgnet.TLRPC.ChatParticipant) r14;
            r15 = org.telegram.ui.ChatUsersActivity.this;
            r15 = r15.currentAccount;
            r15 = org.telegram.messenger.MessagesController.getInstance(r15);
            r5 = r14.user_id;
            r5 = java.lang.Integer.valueOf(r5);
            r5 = r15.getUser(r5);
            r15 = r5.id;
            r10 = org.telegram.ui.ChatUsersActivity.this;
            r10 = r10.currentAccount;
            r10 = org.telegram.messenger.UserConfig.getInstance(r10);
            r10 = r10.getClientUserId();
            if (r15 != r10) goto L_0x0096;
        L_0x0094:
            goto L_0x014f;
        L_0x0096:
            r10 = r5.first_name;
            r15 = r5.last_name;
            r10 = org.telegram.messenger.ContactsController.formatName(r10, r15);
            r10 = r10.toLowerCase();
            r15 = org.telegram.messenger.LocaleController.getInstance();
            r15 = r15.getTranslitString(r10);
            r16 = r10.equals(r15);
            if (r16 == 0) goto L_0x00b1;
        L_0x00b0:
            r15 = 0;
        L_0x00b1:
            r6 = r8.length;
            r7 = 0;
            r17 = 0;
        L_0x00b5:
            if (r7 >= r6) goto L_0x014f;
        L_0x00b7:
            r1 = r8[r7];
            r18 = r10.startsWith(r1);
            if (r18 != 0) goto L_0x0103;
        L_0x00bf:
            r18 = r6;
            r6 = new java.lang.StringBuilder;
            r6.<init>();
            r6.append(r12);
            r6.append(r1);
            r6 = r6.toString();
            r6 = r10.contains(r6);
            if (r6 != 0) goto L_0x0105;
        L_0x00d6:
            if (r15 == 0) goto L_0x00f4;
        L_0x00d8:
            r6 = r15.startsWith(r1);
            if (r6 != 0) goto L_0x0105;
        L_0x00de:
            r6 = new java.lang.StringBuilder;
            r6.<init>();
            r6.append(r12);
            r6.append(r1);
            r6 = r6.toString();
            r6 = r15.contains(r6);
            if (r6 == 0) goto L_0x00f4;
        L_0x00f3:
            goto L_0x0105;
        L_0x00f4:
            r6 = r5.username;
            if (r6 == 0) goto L_0x0100;
        L_0x00f8:
            r6 = r6.startsWith(r1);
            if (r6 == 0) goto L_0x0100;
        L_0x00fe:
            r6 = 2;
            goto L_0x0106;
        L_0x0100:
            r6 = r17;
            goto L_0x0106;
        L_0x0103:
            r18 = r6;
        L_0x0105:
            r6 = 1;
        L_0x0106:
            if (r6 == 0) goto L_0x0143;
        L_0x0108:
            r10 = 1;
            if (r6 != r10) goto L_0x0117;
        L_0x010b:
            r6 = r5.first_name;
            r5 = r5.last_name;
            r1 = org.telegram.messenger.AndroidUtilities.generateSearchName(r6, r5, r1);
            r4.add(r1);
            goto L_0x013f;
        L_0x0117:
            r6 = new java.lang.StringBuilder;
            r6.<init>();
            r6.append(r11);
            r5 = r5.username;
            r6.append(r5);
            r5 = r6.toString();
            r6 = new java.lang.StringBuilder;
            r6.<init>();
            r6.append(r11);
            r6.append(r1);
            r1 = r6.toString();
            r6 = 0;
            r1 = org.telegram.messenger.AndroidUtilities.generateSearchName(r5, r6, r1);
            r4.add(r1);
        L_0x013f:
            r9.add(r14);
            goto L_0x014f;
        L_0x0143:
            r17 = r10;
            r7 = r7 + 1;
            r1 = r21;
            r17 = r6;
            r6 = r18;
            goto L_0x00b5;
        L_0x014f:
            r13 = r13 + 1;
            r1 = r21;
            r5 = 0;
            r7 = 1;
            goto L_0x0062;
        L_0x0157:
            if (r2 == 0) goto L_0x0244;
        L_0x0159:
            r1 = 0;
        L_0x015a:
            r5 = r22.size();
            if (r1 >= r5) goto L_0x0244;
        L_0x0160:
            r5 = r2.get(r1);
            r5 = (org.telegram.tgnet.TLRPC.TL_contact) r5;
            r6 = org.telegram.ui.ChatUsersActivity.this;
            r6 = r6.currentAccount;
            r6 = org.telegram.messenger.MessagesController.getInstance(r6);
            r5 = r5.user_id;
            r5 = java.lang.Integer.valueOf(r5);
            r5 = r6.getUser(r5);
            r6 = r5.id;
            r7 = org.telegram.ui.ChatUsersActivity.this;
            r7 = r7.currentAccount;
            r7 = org.telegram.messenger.UserConfig.getInstance(r7);
            r7 = r7.getClientUserId();
            if (r6 != r7) goto L_0x0190;
        L_0x018c:
            r2 = 1;
            r15 = 0;
            goto L_0x023e;
        L_0x0190:
            r6 = r5.first_name;
            r7 = r5.last_name;
            r6 = org.telegram.messenger.ContactsController.formatName(r6, r7);
            r6 = r6.toLowerCase();
            r7 = org.telegram.messenger.LocaleController.getInstance();
            r7 = r7.getTranslitString(r6);
            r10 = r6.equals(r7);
            if (r10 == 0) goto L_0x01ab;
        L_0x01aa:
            r7 = 0;
        L_0x01ab:
            r10 = r8.length;
            r13 = 0;
            r14 = 0;
        L_0x01ae:
            if (r13 >= r10) goto L_0x018c;
        L_0x01b0:
            r15 = r8[r13];
            r17 = r6.startsWith(r15);
            if (r17 != 0) goto L_0x01f7;
        L_0x01b8:
            r2 = new java.lang.StringBuilder;
            r2.<init>();
            r2.append(r12);
            r2.append(r15);
            r2 = r2.toString();
            r2 = r6.contains(r2);
            if (r2 != 0) goto L_0x01f7;
        L_0x01cd:
            if (r7 == 0) goto L_0x01eb;
        L_0x01cf:
            r2 = r7.startsWith(r15);
            if (r2 != 0) goto L_0x01f7;
        L_0x01d5:
            r2 = new java.lang.StringBuilder;
            r2.<init>();
            r2.append(r12);
            r2.append(r15);
            r2 = r2.toString();
            r2 = r7.contains(r2);
            if (r2 == 0) goto L_0x01eb;
        L_0x01ea:
            goto L_0x01f7;
        L_0x01eb:
            r2 = r5.username;
            if (r2 == 0) goto L_0x01f8;
        L_0x01ef:
            r2 = r2.startsWith(r15);
            if (r2 == 0) goto L_0x01f8;
        L_0x01f5:
            r14 = 2;
            goto L_0x01f8;
        L_0x01f7:
            r14 = 1;
        L_0x01f8:
            if (r14 == 0) goto L_0x0236;
        L_0x01fa:
            r2 = 1;
            if (r14 != r2) goto L_0x020a;
        L_0x01fd:
            r6 = r5.first_name;
            r7 = r5.last_name;
            r6 = org.telegram.messenger.AndroidUtilities.generateSearchName(r6, r7, r15);
            r4.add(r6);
            r15 = 0;
            goto L_0x0232;
        L_0x020a:
            r6 = new java.lang.StringBuilder;
            r6.<init>();
            r6.append(r11);
            r7 = r5.username;
            r6.append(r7);
            r6 = r6.toString();
            r7 = new java.lang.StringBuilder;
            r7.<init>();
            r7.append(r11);
            r7.append(r15);
            r7 = r7.toString();
            r15 = 0;
            r6 = org.telegram.messenger.AndroidUtilities.generateSearchName(r6, r15, r7);
            r4.add(r6);
        L_0x0232:
            r3.add(r5);
            goto L_0x023e;
        L_0x0236:
            r2 = 1;
            r15 = 0;
            r13 = r13 + 1;
            r2 = r22;
            goto L_0x01ae;
        L_0x023e:
            r1 = r1 + 1;
            r2 = r22;
            goto L_0x015a;
        L_0x0244:
            r0.updateSearchResults(r3, r4, r9);
            return;
            */
            throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.ChatUsersActivity$SearchAdapter.lambda$null$0$ChatUsersActivity$SearchAdapter(java.lang.String, java.util.ArrayList, java.util.ArrayList):void");
        }

        private void updateSearchResults(ArrayList<TLObject> arrayList, ArrayList<CharSequence> arrayList2, ArrayList<TLObject> arrayList3) {
            AndroidUtilities.runOnUIThread(new -$$Lambda$ChatUsersActivity$SearchAdapter$52VhVFqnI_lH_BHMdKeSzUCSF-M(this, arrayList, arrayList2, arrayList3));
        }

        public /* synthetic */ void lambda$updateSearchResults$2$ChatUsersActivity$SearchAdapter(ArrayList arrayList, ArrayList arrayList2, ArrayList arrayList3) {
            this.searchResult = arrayList;
            this.searchResultNames = arrayList2;
            this.searchAdapterHelper.mergeResults(this.searchResult);
            if (!ChatObject.isChannel(ChatUsersActivity.this.currentChat)) {
                arrayList = this.searchAdapterHelper.getGroupSearch();
                arrayList.clear();
                arrayList.addAll(arrayList3);
            }
            notifyDataSetChanged();
        }

        public boolean isEnabled(ViewHolder viewHolder) {
            return viewHolder.getItemViewType() != 1;
        }

        public int getItemCount() {
            int size = this.searchResult.size();
            int size2 = this.searchAdapterHelper.getGlobalSearch().size();
            int size3 = this.searchAdapterHelper.getGroupSearch().size();
            int i = 0;
            if (size != 0) {
                i = 0 + (size + 1);
            }
            if (size2 != 0) {
                i += size2 + 1;
            }
            return size3 != 0 ? i + (size3 + 1) : i;
        }

        public void notifyDataSetChanged() {
            this.totalCount = 0;
            int size = this.searchAdapterHelper.getGroupSearch().size();
            if (size != 0) {
                this.groupStartRow = 0;
                this.totalCount += size + 1;
            } else {
                this.groupStartRow = -1;
            }
            int size2 = this.searchResult.size();
            if (size2 != 0) {
                size = this.totalCount;
                this.contactsStartRow = size;
                this.totalCount = size + (size2 + 1);
            } else {
                this.contactsStartRow = -1;
            }
            size2 = this.searchAdapterHelper.getGlobalSearch().size();
            if (size2 != 0) {
                size = this.totalCount;
                this.globalStartRow = size;
                this.totalCount = size + (size2 + 1);
            } else {
                this.globalStartRow = -1;
            }
            super.notifyDataSetChanged();
        }

        /* JADX WARNING: Missing block: B:26:0x0061, code skipped:
            return null;
     */
        public org.telegram.tgnet.TLObject getItem(int r3) {
            /*
            r2 = this;
            r0 = r2.searchAdapterHelper;
            r0 = r0.getGroupSearch();
            r0 = r0.size();
            r1 = 0;
            if (r0 == 0) goto L_0x0024;
        L_0x000d:
            r0 = r0 + 1;
            if (r0 <= r3) goto L_0x0023;
        L_0x0011:
            if (r3 != 0) goto L_0x0014;
        L_0x0013:
            return r1;
        L_0x0014:
            r0 = r2.searchAdapterHelper;
            r0 = r0.getGroupSearch();
            r3 = r3 + -1;
            r3 = r0.get(r3);
            r3 = (org.telegram.tgnet.TLObject) r3;
            return r3;
        L_0x0023:
            r3 = r3 - r0;
        L_0x0024:
            r0 = r2.searchResult;
            r0 = r0.size();
            if (r0 == 0) goto L_0x003f;
        L_0x002c:
            r0 = r0 + 1;
            if (r0 <= r3) goto L_0x003e;
        L_0x0030:
            if (r3 != 0) goto L_0x0033;
        L_0x0032:
            return r1;
        L_0x0033:
            r0 = r2.searchResult;
            r3 = r3 + -1;
            r3 = r0.get(r3);
            r3 = (org.telegram.tgnet.TLObject) r3;
            return r3;
        L_0x003e:
            r3 = r3 - r0;
        L_0x003f:
            r0 = r2.searchAdapterHelper;
            r0 = r0.getGlobalSearch();
            r0 = r0.size();
            if (r0 == 0) goto L_0x0061;
        L_0x004b:
            r0 = r0 + 1;
            if (r0 <= r3) goto L_0x0061;
        L_0x004f:
            if (r3 != 0) goto L_0x0052;
        L_0x0051:
            return r1;
        L_0x0052:
            r0 = r2.searchAdapterHelper;
            r0 = r0.getGlobalSearch();
            r3 = r3 + -1;
            r3 = r0.get(r3);
            r3 = (org.telegram.tgnet.TLObject) r3;
            return r3;
        L_0x0061:
            return r1;
            */
            throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.ChatUsersActivity$SearchAdapter.getItem(int):org.telegram.tgnet.TLObject");
        }

        public /* synthetic */ boolean lambda$onCreateViewHolder$3$ChatUsersActivity$SearchAdapter(ManageChatUserCell manageChatUserCell, boolean z) {
            if (!(getItem(((Integer) manageChatUserCell.getTag()).intValue()) instanceof ChannelParticipant)) {
                return false;
            }
            return ChatUsersActivity.this.createMenuForParticipant((ChannelParticipant) getItem(((Integer) manageChatUserCell.getTag()).intValue()), z ^ 1);
        }

        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View graySectionCell;
            if (i != 0) {
                graySectionCell = new GraySectionCell(this.mContext);
            } else {
                graySectionCell = new ManageChatUserCell(this.mContext, 2, 2, ChatUsersActivity.this.selectType == 0);
                graySectionCell.setBackgroundColor(Theme.getColor("windowBackgroundWhite"));
                graySectionCell.setDelegate(new -$$Lambda$ChatUsersActivity$SearchAdapter$a-9pjhx3qaGVfHCKv5RFU4cCrRc(this));
            }
            return new Holder(graySectionCell);
        }

        /* JADX WARNING: Removed duplicated region for block: B:76:0x018a  */
        /* JADX WARNING: Removed duplicated region for block: B:63:0x014f  */
        /* JADX WARNING: Removed duplicated region for block: B:67:0x0168 A:{Catch:{ Exception -> 0x0181 }} */
        /* JADX WARNING: Removed duplicated region for block: B:76:0x018a  */
        /* JADX WARNING: Removed duplicated region for block: B:63:0x014f  */
        /* JADX WARNING: Removed duplicated region for block: B:67:0x0168 A:{Catch:{ Exception -> 0x0181 }} */
        /* JADX WARNING: Removed duplicated region for block: B:76:0x018a  */
        /* JADX WARNING: Removed duplicated region for block: B:39:0x00ed  */
        /* JADX WARNING: Removed duplicated region for block: B:63:0x014f  */
        /* JADX WARNING: Removed duplicated region for block: B:67:0x0168 A:{Catch:{ Exception -> 0x0181 }} */
        /* JADX WARNING: Removed duplicated region for block: B:76:0x018a  */
        public void onBindViewHolder(androidx.recyclerview.widget.RecyclerView.ViewHolder r14, int r15) {
            /*
            r13 = this;
            r0 = r14.getItemViewType();
            r1 = 1;
            if (r0 == 0) goto L_0x0088;
        L_0x0007:
            if (r0 == r1) goto L_0x000b;
        L_0x0009:
            goto L_0x01bc;
        L_0x000b:
            r14 = r14.itemView;
            r14 = (org.telegram.ui.Cells.GraySectionCell) r14;
            r0 = r13.groupStartRow;
            if (r15 != r0) goto L_0x0064;
        L_0x0013:
            r15 = org.telegram.ui.ChatUsersActivity.this;
            r15 = r15.type;
            if (r15 != 0) goto L_0x0029;
        L_0x001b:
            r15 = NUM; // 0x7f0d0208 float:1.874317E38 double:1.0531300345E-314;
            r0 = "ChannelBlockedUsers";
            r15 = org.telegram.messenger.LocaleController.getString(r0, r15);
            r14.setText(r15);
            goto L_0x01bc;
        L_0x0029:
            r15 = org.telegram.ui.ChatUsersActivity.this;
            r15 = r15.type;
            r0 = 3;
            if (r15 != r0) goto L_0x0040;
        L_0x0032:
            r15 = NUM; // 0x7f0d0245 float:1.8743293E38 double:1.0531300646E-314;
            r0 = "ChannelRestrictedUsers";
            r15 = org.telegram.messenger.LocaleController.getString(r0, r15);
            r14.setText(r15);
            goto L_0x01bc;
        L_0x0040:
            r15 = org.telegram.ui.ChatUsersActivity.this;
            r15 = r15.isChannel;
            if (r15 == 0) goto L_0x0056;
        L_0x0048:
            r15 = NUM; // 0x7f0d024d float:1.874331E38 double:1.0531300685E-314;
            r0 = "ChannelSubscribers";
            r15 = org.telegram.messenger.LocaleController.getString(r0, r15);
            r14.setText(r15);
            goto L_0x01bc;
        L_0x0056:
            r15 = NUM; // 0x7f0d0223 float:1.8743224E38 double:1.053130048E-314;
            r0 = "ChannelMembers";
            r15 = org.telegram.messenger.LocaleController.getString(r0, r15);
            r14.setText(r15);
            goto L_0x01bc;
        L_0x0064:
            r0 = r13.globalStartRow;
            if (r15 != r0) goto L_0x0076;
        L_0x0068:
            r15 = NUM; // 0x7f0d047b float:1.8744441E38 double:1.053130344E-314;
            r0 = "GlobalSearch";
            r15 = org.telegram.messenger.LocaleController.getString(r0, r15);
            r14.setText(r15);
            goto L_0x01bc;
        L_0x0076:
            r0 = r13.contactsStartRow;
            if (r15 != r0) goto L_0x01bc;
        L_0x007a:
            r15 = NUM; // 0x7f0d02d9 float:1.8743593E38 double:1.0531301377E-314;
            r0 = "Contacts";
            r15 = org.telegram.messenger.LocaleController.getString(r0, r15);
            r14.setText(r15);
            goto L_0x01bc;
        L_0x0088:
            r0 = r13.getItem(r15);
            r2 = r0 instanceof org.telegram.tgnet.TLRPC.User;
            if (r2 == 0) goto L_0x0093;
        L_0x0090:
            r0 = (org.telegram.tgnet.TLRPC.User) r0;
            goto L_0x00c8;
        L_0x0093:
            r2 = r0 instanceof org.telegram.tgnet.TLRPC.ChannelParticipant;
            if (r2 == 0) goto L_0x00ae;
        L_0x0097:
            r2 = org.telegram.ui.ChatUsersActivity.this;
            r2 = r2.currentAccount;
            r2 = org.telegram.messenger.MessagesController.getInstance(r2);
            r0 = (org.telegram.tgnet.TLRPC.ChannelParticipant) r0;
            r0 = r0.user_id;
            r0 = java.lang.Integer.valueOf(r0);
            r0 = r2.getUser(r0);
            goto L_0x00c8;
        L_0x00ae:
            r2 = r0 instanceof org.telegram.tgnet.TLRPC.ChatParticipant;
            if (r2 == 0) goto L_0x01bc;
        L_0x00b2:
            r2 = org.telegram.ui.ChatUsersActivity.this;
            r2 = r2.currentAccount;
            r2 = org.telegram.messenger.MessagesController.getInstance(r2);
            r0 = (org.telegram.tgnet.TLRPC.ChatParticipant) r0;
            r0 = r0.user_id;
            r0 = java.lang.Integer.valueOf(r0);
            r0 = r2.getUser(r0);
        L_0x00c8:
            r2 = r0.username;
            r3 = r13.searchAdapterHelper;
            r3 = r3.getGroupSearch();
            r3 = r3.size();
            r4 = 0;
            r5 = 0;
            if (r3 == 0) goto L_0x00e6;
        L_0x00d8:
            r3 = r3 + r1;
            if (r3 <= r15) goto L_0x00e5;
        L_0x00db:
            r3 = r13.searchAdapterHelper;
            r3 = r3.getLastFoundChannel();
            r6 = r3;
            r3 = r15;
            r15 = 1;
            goto L_0x00e9;
        L_0x00e5:
            r15 = r15 - r3;
        L_0x00e6:
            r3 = r15;
            r6 = r5;
            r15 = 0;
        L_0x00e9:
            r7 = "@";
            if (r15 != 0) goto L_0x012a;
        L_0x00ed:
            r8 = r13.searchResult;
            r8 = r8.size();
            if (r8 == 0) goto L_0x012a;
        L_0x00f5:
            r8 = r8 + r1;
            if (r8 <= r3) goto L_0x0129;
        L_0x00f8:
            r15 = r13.searchResultNames;
            r8 = r3 + -1;
            r15 = r15.get(r8);
            r15 = (java.lang.CharSequence) r15;
            if (r15 == 0) goto L_0x0126;
        L_0x0104:
            r8 = android.text.TextUtils.isEmpty(r2);
            if (r8 != 0) goto L_0x0126;
        L_0x010a:
            r8 = r15.toString();
            r9 = new java.lang.StringBuilder;
            r9.<init>();
            r9.append(r7);
            r9.append(r2);
            r9 = r9.toString();
            r8 = r8.startsWith(r9);
            if (r8 == 0) goto L_0x0126;
        L_0x0123:
            r8 = r5;
            r5 = r15;
            goto L_0x0127;
        L_0x0126:
            r8 = r15;
        L_0x0127:
            r15 = 1;
            goto L_0x012b;
        L_0x0129:
            r3 = r3 - r8;
        L_0x012a:
            r8 = r5;
        L_0x012b:
            r9 = 33;
            r10 = "windowBackgroundWhiteBlueText4";
            r11 = -1;
            if (r15 != 0) goto L_0x0187;
        L_0x0132:
            if (r2 == 0) goto L_0x0187;
        L_0x0134:
            r15 = r13.searchAdapterHelper;
            r15 = r15.getGlobalSearch();
            r15 = r15.size();
            if (r15 == 0) goto L_0x0187;
        L_0x0140:
            r15 = r15 + r1;
            if (r15 <= r3) goto L_0x0187;
        L_0x0143:
            r15 = r13.searchAdapterHelper;
            r15 = r15.getLastFoundUsername();
            r5 = r15.startsWith(r7);
            if (r5 == 0) goto L_0x0153;
        L_0x014f:
            r15 = r15.substring(r1);
        L_0x0153:
            r1 = new android.text.SpannableStringBuilder;	 Catch:{ Exception -> 0x0181 }
            r1.<init>();	 Catch:{ Exception -> 0x0181 }
            r1.append(r7);	 Catch:{ Exception -> 0x0181 }
            r1.append(r2);	 Catch:{ Exception -> 0x0181 }
            r5 = r2.toLowerCase();	 Catch:{ Exception -> 0x0181 }
            r5 = r5.indexOf(r15);	 Catch:{ Exception -> 0x0181 }
            if (r5 == r11) goto L_0x0188;
        L_0x0168:
            r15 = r15.length();	 Catch:{ Exception -> 0x0181 }
            if (r5 != 0) goto L_0x0171;
        L_0x016e:
            r15 = r15 + 1;
            goto L_0x0173;
        L_0x0171:
            r5 = r5 + 1;
        L_0x0173:
            r7 = new android.text.style.ForegroundColorSpan;	 Catch:{ Exception -> 0x0181 }
            r12 = org.telegram.ui.ActionBar.Theme.getColor(r10);	 Catch:{ Exception -> 0x0181 }
            r7.<init>(r12);	 Catch:{ Exception -> 0x0181 }
            r15 = r15 + r5;
            r1.setSpan(r7, r5, r15, r9);	 Catch:{ Exception -> 0x0181 }
            goto L_0x0188;
        L_0x0181:
            r15 = move-exception;
            org.telegram.messenger.FileLog.e(r15);
            r1 = r2;
            goto L_0x0188;
        L_0x0187:
            r1 = r5;
        L_0x0188:
            if (r6 == 0) goto L_0x01ae;
        L_0x018a:
            r15 = org.telegram.messenger.UserObject.getUserName(r0);
            r8 = new android.text.SpannableStringBuilder;
            r8.<init>(r15);
            r15 = r15.toLowerCase();
            r15 = r15.indexOf(r6);
            if (r15 == r11) goto L_0x01ae;
        L_0x019d:
            r2 = new android.text.style.ForegroundColorSpan;
            r5 = org.telegram.ui.ActionBar.Theme.getColor(r10);
            r2.<init>(r5);
            r5 = r6.length();
            r5 = r5 + r15;
            r8.setSpan(r2, r15, r5, r9);
        L_0x01ae:
            r14 = r14.itemView;
            r14 = (org.telegram.ui.Cells.ManageChatUserCell) r14;
            r15 = java.lang.Integer.valueOf(r3);
            r14.setTag(r15);
            r14.setData(r0, r8, r1, r4);
        L_0x01bc:
            return;
            */
            throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.ChatUsersActivity$SearchAdapter.onBindViewHolder(androidx.recyclerview.widget.RecyclerView$ViewHolder, int):void");
        }

        public void onViewRecycled(ViewHolder viewHolder) {
            View view = viewHolder.itemView;
            if (view instanceof ManageChatUserCell) {
                ((ManageChatUserCell) view).recycle();
            }
        }

        public int getItemViewType(int i) {
            return (i == this.globalStartRow || i == this.groupStartRow || i == this.contactsStartRow) ? 1 : 0;
        }
    }

    public ChatUsersActivity(Bundle bundle) {
        super(bundle);
        Chat chat = this.currentChat;
        if (chat != null) {
            TL_chatBannedRights tL_chatBannedRights = chat.default_banned_rights;
            if (tL_chatBannedRights != null) {
                TL_chatBannedRights tL_chatBannedRights2 = this.defaultBannedRights;
                tL_chatBannedRights2.view_messages = tL_chatBannedRights.view_messages;
                tL_chatBannedRights2.send_stickers = tL_chatBannedRights.send_stickers;
                tL_chatBannedRights2.send_media = tL_chatBannedRights.send_media;
                tL_chatBannedRights2.embed_links = tL_chatBannedRights.embed_links;
                tL_chatBannedRights2.send_messages = tL_chatBannedRights.send_messages;
                tL_chatBannedRights2.send_games = tL_chatBannedRights.send_games;
                tL_chatBannedRights2.send_inline = tL_chatBannedRights.send_inline;
                tL_chatBannedRights2.send_gifs = tL_chatBannedRights.send_gifs;
                tL_chatBannedRights2.pin_messages = tL_chatBannedRights.pin_messages;
                tL_chatBannedRights2.send_polls = tL_chatBannedRights.send_polls;
                tL_chatBannedRights2.invite_users = tL_chatBannedRights.invite_users;
                tL_chatBannedRights2.change_info = tL_chatBannedRights.change_info;
            }
        }
        this.initialBannedRights = ChatObject.getBannedRightsString(this.defaultBannedRights);
        boolean z = ChatObject.isChannel(this.currentChat) && !this.currentChat.megagroup;
        this.isChannel = z;
    }

    private void updateRows() {
        this.currentChat = MessagesController.getInstance(this.currentAccount).getChat(Integer.valueOf(this.chatId));
        Chat chat = this.currentChat;
        if (chat != null) {
            this.recentActionsRow = -1;
            this.addNewRow = -1;
            this.addNew2Row = -1;
            this.addNewSectionRow = -1;
            this.restricted1SectionRow = -1;
            this.participantsStartRow = -1;
            this.participantsDividerRow = -1;
            this.participantsDivider2Row = -1;
            this.participantsEndRow = -1;
            this.participantsInfoRow = -1;
            this.blockedEmptyRow = -1;
            this.permissionsSectionRow = -1;
            this.sendMessagesRow = -1;
            this.sendMediaRow = -1;
            this.sendStickersRow = -1;
            this.sendPollsRow = -1;
            this.embedLinksRow = -1;
            this.addUsersRow = -1;
            this.pinMessagesRow = -1;
            this.changeInfoRow = -1;
            this.removedUsersRow = -1;
            this.contactsHeaderRow = -1;
            this.contactsStartRow = -1;
            this.contactsEndRow = -1;
            this.botHeaderRow = -1;
            this.botStartRow = -1;
            this.botEndRow = -1;
            this.membersHeaderRow = -1;
            int i = 0;
            this.rowCount = 0;
            int i2 = this.type;
            int i3;
            if (i2 == 3) {
                i = this.rowCount;
                this.rowCount = i + 1;
                this.permissionsSectionRow = i;
                i = this.rowCount;
                this.rowCount = i + 1;
                this.sendMessagesRow = i;
                i = this.rowCount;
                this.rowCount = i + 1;
                this.sendMediaRow = i;
                i = this.rowCount;
                this.rowCount = i + 1;
                this.sendStickersRow = i;
                i = this.rowCount;
                this.rowCount = i + 1;
                this.sendPollsRow = i;
                i = this.rowCount;
                this.rowCount = i + 1;
                this.embedLinksRow = i;
                i = this.rowCount;
                this.rowCount = i + 1;
                this.addUsersRow = i;
                i = this.rowCount;
                this.rowCount = i + 1;
                this.pinMessagesRow = i;
                i = this.rowCount;
                this.rowCount = i + 1;
                this.changeInfoRow = i;
                if (ChatObject.isChannel(chat)) {
                    i3 = this.rowCount;
                    this.rowCount = i3 + 1;
                    this.participantsDivider2Row = i3;
                    i3 = this.rowCount;
                    this.rowCount = i3 + 1;
                    this.removedUsersRow = i3;
                }
                i3 = this.rowCount;
                this.rowCount = i3 + 1;
                this.participantsDividerRow = i3;
                if (ChatObject.canBlockUsers(this.currentChat)) {
                    i3 = this.rowCount;
                    this.rowCount = i3 + 1;
                    this.addNewRow = i3;
                }
                if (!this.participants.isEmpty()) {
                    i3 = this.rowCount;
                    this.participantsStartRow = i3;
                    this.rowCount = i3 + this.participants.size();
                    this.participantsEndRow = this.rowCount;
                }
                if (!(this.addNewRow == -1 && this.participantsStartRow == -1)) {
                    i3 = this.rowCount;
                    this.rowCount = i3 + 1;
                    this.addNewSectionRow = i3;
                }
            } else if (i2 == 0) {
                if (ChatObject.canBlockUsers(chat)) {
                    i3 = this.rowCount;
                    this.rowCount = i3 + 1;
                    this.addNewRow = i3;
                    if (!this.participants.isEmpty()) {
                        i3 = this.rowCount;
                        this.rowCount = i3 + 1;
                        this.participantsInfoRow = i3;
                    }
                }
                if (!this.participants.isEmpty()) {
                    i3 = this.rowCount;
                    this.rowCount = i3 + 1;
                    this.restricted1SectionRow = i3;
                    i3 = this.rowCount;
                    this.participantsStartRow = i3;
                    this.rowCount = i3 + this.participants.size();
                    this.participantsEndRow = this.rowCount;
                }
                if (this.participantsStartRow == -1) {
                    ActionBarMenuItem actionBarMenuItem = this.searchItem;
                    if (actionBarMenuItem != null) {
                        actionBarMenuItem.setVisibility(4);
                    }
                    i3 = this.rowCount;
                    this.rowCount = i3 + 1;
                    this.blockedEmptyRow = i3;
                } else if (this.participantsInfoRow == -1) {
                    i3 = this.rowCount;
                    this.rowCount = i3 + 1;
                    this.participantsInfoRow = i3;
                } else {
                    i3 = this.rowCount;
                    this.rowCount = i3 + 1;
                    this.addNewSectionRow = i3;
                }
            } else {
                int i4 = 1;
                if (i2 == 1) {
                    if (ChatObject.isChannel(chat) && this.currentChat.megagroup) {
                        ChatFull chatFull = this.info;
                        if (chatFull == null || chatFull.participants_count <= 200) {
                            i3 = this.rowCount;
                            this.rowCount = i3 + 1;
                            this.recentActionsRow = i3;
                            i3 = this.rowCount;
                            this.rowCount = i3 + 1;
                            this.addNewSectionRow = i3;
                        }
                    }
                    if (ChatObject.canAddAdmins(this.currentChat)) {
                        i3 = this.rowCount;
                        this.rowCount = i3 + 1;
                        this.addNewRow = i3;
                    }
                    if (!this.participants.isEmpty()) {
                        i3 = this.rowCount;
                        this.participantsStartRow = i3;
                        this.rowCount = i3 + this.participants.size();
                        this.participantsEndRow = this.rowCount;
                    }
                    i3 = this.rowCount;
                    this.rowCount = i3 + 1;
                    this.participantsInfoRow = i3;
                } else if (i2 == 2) {
                    if (!this.contacts.isEmpty()) {
                        i3 = this.rowCount;
                        this.rowCount = i3 + 1;
                        this.contactsHeaderRow = i3;
                        i3 = this.rowCount;
                        this.contactsStartRow = i3;
                        this.rowCount = i3 + this.contacts.size();
                        this.contactsEndRow = this.rowCount;
                        i = 1;
                    }
                    if (this.bots.isEmpty()) {
                        i4 = i;
                    } else {
                        i3 = this.rowCount;
                        this.rowCount = i3 + 1;
                        this.botHeaderRow = i3;
                        i3 = this.rowCount;
                        this.botStartRow = i3;
                        this.rowCount = i3 + this.bots.size();
                        this.botEndRow = this.rowCount;
                    }
                    if (!this.participants.isEmpty()) {
                        if (i4 != 0) {
                            i3 = this.rowCount;
                            this.rowCount = i3 + 1;
                            this.membersHeaderRow = i3;
                        }
                        i3 = this.rowCount;
                        this.participantsStartRow = i3;
                        this.rowCount = i3 + this.participants.size();
                        this.participantsEndRow = this.rowCount;
                    }
                    i3 = this.rowCount;
                    if (i3 != 0) {
                        this.rowCount = i3 + 1;
                        this.participantsInfoRow = i3;
                    }
                }
            }
        }
    }

    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.chatInfoDidLoad);
        loadChatParticipants(0, 200);
        return true;
    }

    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.chatInfoDidLoad);
    }

    /* JADX WARNING: Missing block: B:25:0x00b9, code skipped:
            if (r1 != 3) goto L_0x011b;
     */
    public android.view.View createView(android.content.Context r10) {
        /*
        r9 = this;
        r0 = 0;
        r9.searching = r0;
        r9.searchWas = r0;
        r1 = r9.actionBar;
        r2 = NUM; // 0x7var_f4 float:1.7945073E38 double:1.0529356236E-314;
        r1.setBackButtonImage(r2);
        r1 = r9.actionBar;
        r2 = 1;
        r1.setAllowOverlayTitle(r2);
        r1 = r9.type;
        r3 = 2;
        r4 = 3;
        if (r1 != r4) goto L_0x0029;
    L_0x0019:
        r1 = r9.actionBar;
        r5 = NUM; // 0x7f0d023a float:1.874327E38 double:1.053130059E-314;
        r6 = "ChannelPermissions";
        r5 = org.telegram.messenger.LocaleController.getString(r6, r5);
        r1.setTitle(r5);
        goto L_0x00a5;
    L_0x0029:
        if (r1 != 0) goto L_0x003a;
    L_0x002b:
        r1 = r9.actionBar;
        r5 = NUM; // 0x7f0d0206 float:1.8743165E38 double:1.0531300335E-314;
        r6 = "ChannelBlacklist";
        r5 = org.telegram.messenger.LocaleController.getString(r6, r5);
        r1.setTitle(r5);
        goto L_0x00a5;
    L_0x003a:
        if (r1 != r2) goto L_0x004b;
    L_0x003c:
        r1 = r9.actionBar;
        r5 = NUM; // 0x7f0d0201 float:1.8743155E38 double:1.053130031E-314;
        r6 = "ChannelAdministrators";
        r5 = org.telegram.messenger.LocaleController.getString(r6, r5);
        r1.setTitle(r5);
        goto L_0x00a5;
    L_0x004b:
        if (r1 != r3) goto L_0x00a5;
    L_0x004d:
        r1 = r9.selectType;
        if (r1 != 0) goto L_0x0073;
    L_0x0051:
        r1 = r9.isChannel;
        if (r1 == 0) goto L_0x0064;
    L_0x0055:
        r1 = r9.actionBar;
        r5 = NUM; // 0x7f0d024d float:1.874331E38 double:1.0531300685E-314;
        r6 = "ChannelSubscribers";
        r5 = org.telegram.messenger.LocaleController.getString(r6, r5);
        r1.setTitle(r5);
        goto L_0x00a5;
    L_0x0064:
        r1 = r9.actionBar;
        r5 = NUM; // 0x7f0d0223 float:1.8743224E38 double:1.053130048E-314;
        r6 = "ChannelMembers";
        r5 = org.telegram.messenger.LocaleController.getString(r6, r5);
        r1.setTitle(r5);
        goto L_0x00a5;
    L_0x0073:
        if (r1 != r2) goto L_0x0084;
    L_0x0075:
        r1 = r9.actionBar;
        r5 = NUM; // 0x7f0d01f8 float:1.8743137E38 double:1.0531300266E-314;
        r6 = "ChannelAddAdmin";
        r5 = org.telegram.messenger.LocaleController.getString(r6, r5);
        r1.setTitle(r5);
        goto L_0x00a5;
    L_0x0084:
        if (r1 != r3) goto L_0x0095;
    L_0x0086:
        r1 = r9.actionBar;
        r5 = NUM; // 0x7f0d0207 float:1.8743167E38 double:1.053130034E-314;
        r6 = "ChannelBlockUser";
        r5 = org.telegram.messenger.LocaleController.getString(r6, r5);
        r1.setTitle(r5);
        goto L_0x00a5;
    L_0x0095:
        if (r1 != r4) goto L_0x00a5;
    L_0x0097:
        r1 = r9.actionBar;
        r5 = NUM; // 0x7f0d01f9 float:1.874314E38 double:1.053130027E-314;
        r6 = "ChannelAddException";
        r5 = org.telegram.messenger.LocaleController.getString(r6, r5);
        r1.setTitle(r5);
    L_0x00a5:
        r1 = r9.actionBar;
        r5 = new org.telegram.ui.ChatUsersActivity$1;
        r5.<init>();
        r1.setActionBarMenuOnItemClick(r5);
        r1 = r9.selectType;
        if (r1 != 0) goto L_0x00bb;
    L_0x00b3:
        r1 = r9.type;
        if (r1 == r3) goto L_0x00bb;
    L_0x00b7:
        if (r1 == 0) goto L_0x00bb;
    L_0x00b9:
        if (r1 != r4) goto L_0x011b;
    L_0x00bb:
        r1 = new org.telegram.ui.ChatUsersActivity$SearchAdapter;
        r1.<init>(r10);
        r9.searchListViewAdapter = r1;
        r1 = r9.actionBar;
        r1 = r1.createMenu();
        r5 = NUM; // 0x7var_fe float:1.7945093E38 double:1.0529356285E-314;
        r5 = r1.addItem(r0, r5);
        r5 = r5.setIsSearchField(r2);
        r6 = new org.telegram.ui.ChatUsersActivity$2;
        r6.<init>();
        r5 = r5.setActionBarMenuItemSearchListener(r6);
        r9.searchItem = r5;
        r5 = r9.type;
        if (r5 != r4) goto L_0x00f1;
    L_0x00e2:
        r5 = r9.searchItem;
        r6 = NUM; // 0x7f0d0246 float:1.8743295E38 double:1.053130065E-314;
        r7 = "ChannelSearchException";
        r6 = org.telegram.messenger.LocaleController.getString(r7, r6);
        r5.setSearchFieldHint(r6);
        goto L_0x00ff;
    L_0x00f1:
        r5 = r9.searchItem;
        r6 = NUM; // 0x7f0d0883 float:1.8746534E38 double:1.053130854E-314;
        r7 = "Search";
        r6 = org.telegram.messenger.LocaleController.getString(r7, r6);
        r5.setSearchFieldHint(r6);
    L_0x00ff:
        r5 = r9.type;
        if (r5 != r4) goto L_0x011b;
    L_0x0103:
        r5 = NUM; // 0x7var_ float:1.7945134E38 double:1.0529356384E-314;
        r6 = NUM; // 0x42600000 float:56.0 double:5.50185432E-315;
        r6 = org.telegram.messenger.AndroidUtilities.dp(r6);
        r7 = NUM; // 0x7f0d0358 float:1.874385E38 double:1.0531302005E-314;
        r8 = "Done";
        r7 = org.telegram.messenger.LocaleController.getString(r8, r7);
        r1 = r1.addItemWithWidth(r2, r5, r6, r7);
        r9.doneItem = r1;
    L_0x011b:
        r1 = new android.widget.FrameLayout;
        r1.<init>(r10);
        r9.fragmentView = r1;
        r1 = r9.fragmentView;
        r5 = "windowBackgroundGray";
        r5 = org.telegram.ui.ActionBar.Theme.getColor(r5);
        r1.setBackgroundColor(r5);
        r1 = r9.fragmentView;
        r1 = (android.widget.FrameLayout) r1;
        r5 = new org.telegram.ui.Components.EmptyTextProgressView;
        r5.<init>(r10);
        r9.emptyView = r5;
        r5 = r9.type;
        if (r5 == 0) goto L_0x0140;
    L_0x013c:
        if (r5 == r3) goto L_0x0140;
    L_0x013e:
        if (r5 != r4) goto L_0x014e;
    L_0x0140:
        r4 = r9.emptyView;
        r5 = NUM; // 0x7f0d05d9 float:1.8745151E38 double:1.053130517E-314;
        r6 = "NoResult";
        r5 = org.telegram.messenger.LocaleController.getString(r6, r5);
        r4.setText(r5);
    L_0x014e:
        r4 = r9.emptyView;
        r4.setShowAtCenter(r2);
        r4 = r9.emptyView;
        r5 = -NUM; // 0xffffffffbvar_ float:-1.0 double:NaN;
        r6 = -1;
        r7 = org.telegram.ui.Components.LayoutHelper.createFrame(r6, r5);
        r1.addView(r4, r7);
        r4 = new org.telegram.ui.Components.RecyclerListView;
        r4.<init>(r10);
        r9.listView = r4;
        r4 = r9.listView;
        r7 = r9.emptyView;
        r4.setEmptyView(r7);
        r4 = r9.listView;
        r7 = new androidx.recyclerview.widget.LinearLayoutManager;
        r7.<init>(r10, r2, r0);
        r4.setLayoutManager(r7);
        r0 = r9.listView;
        r4 = new org.telegram.ui.ChatUsersActivity$ListAdapter;
        r4.<init>(r10);
        r9.listViewAdapter = r4;
        r0.setAdapter(r4);
        r10 = r9.listView;
        r0 = org.telegram.messenger.LocaleController.isRTL;
        if (r0 == 0) goto L_0x018a;
    L_0x0189:
        goto L_0x018b;
    L_0x018a:
        r2 = 2;
    L_0x018b:
        r10.setVerticalScrollbarPosition(r2);
        r10 = r9.listView;
        r0 = org.telegram.ui.Components.LayoutHelper.createFrame(r6, r5);
        r1.addView(r10, r0);
        r10 = r9.listView;
        r0 = new org.telegram.ui.-$$Lambda$ChatUsersActivity$khtj9GNbB7ZWEJr1LsZ9PJA147k;
        r0.<init>(r9);
        r10.setOnItemClickListener(r0);
        r10 = r9.listView;
        r0 = new org.telegram.ui.-$$Lambda$ChatUsersActivity$cHkO58q9VIRK-9Bta8j6CMlPSwo;
        r0.<init>(r9);
        r10.setOnItemLongClickListener(r0);
        r10 = r9.searchItem;
        if (r10 == 0) goto L_0x01b9;
    L_0x01af:
        r10 = r9.listView;
        r0 = new org.telegram.ui.ChatUsersActivity$3;
        r0.<init>();
        r10.setOnScrollListener(r0);
    L_0x01b9:
        r10 = r9.loadingUsers;
        if (r10 == 0) goto L_0x01c3;
    L_0x01bd:
        r10 = r9.emptyView;
        r10.showProgress();
        goto L_0x01c8;
    L_0x01c3:
        r10 = r9.emptyView;
        r10.showTextView();
    L_0x01c8:
        r9.updateRows();
        r10 = r9.fragmentView;
        return r10;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.ChatUsersActivity.createView(android.content.Context):android.view.View");
    }

    public /* synthetic */ void lambda$createView$5$ChatUsersActivity(View view, int i) {
        Bundle bundle;
        TL_chatBannedRights tL_chatBannedRights;
        int isChecked;
        TL_chatBannedRights tL_chatBannedRights2;
        TLObject tLObject;
        TL_chatAdminRights tL_chatAdminRights;
        int i2;
        boolean z;
        int i3 = i;
        Object obj = this.listView.getAdapter() == this.listViewAdapter ? 1 : null;
        int i4 = 3;
        if (obj != null) {
            String str = "type";
            String str2 = "chat_id";
            ChatUsersActivity chatUsersActivity;
            if (i3 == this.addNewRow) {
                i3 = this.type;
                String str3 = "selectType";
                if (i3 == 0 || i3 == 3) {
                    bundle = new Bundle();
                    bundle.putInt(str2, this.chatId);
                    bundle.putInt(str, 2);
                    if (this.type == 0) {
                        i4 = 2;
                    }
                    bundle.putInt(str3, i4);
                    chatUsersActivity = new ChatUsersActivity(bundle);
                    chatUsersActivity.setInfo(this.info);
                    presentFragment(chatUsersActivity);
                } else if (i3 == 1) {
                    bundle = new Bundle();
                    bundle.putInt(str2, this.chatId);
                    bundle.putInt(str, 2);
                    bundle.putInt(str3, 1);
                    chatUsersActivity = new ChatUsersActivity(bundle);
                    chatUsersActivity.setDelegate(new -$$Lambda$ChatUsersActivity$RHVQKwjVgC2Fj6Os2cWfGRwR2VY(this));
                    chatUsersActivity.setInfo(this.info);
                    presentFragment(chatUsersActivity);
                } else if (i3 == 2) {
                    bundle = new Bundle();
                    bundle.putBoolean("onlyUsers", true);
                    bundle.putBoolean("destroyAfterSelect", true);
                    bundle.putBoolean("returnAsResult", true);
                    bundle.putBoolean("needForwardCount", false);
                    String str4 = "selectAlertString";
                    if (this.isChannel) {
                        bundle.putString(str4, LocaleController.getString("ChannelAddTo", NUM));
                        bundle.putInt("channelId", this.currentChat.id);
                    } else {
                        if (!ChatObject.isChannel(this.currentChat)) {
                            bundle.putInt(str2, this.currentChat.id);
                        }
                        bundle.putString(str4, LocaleController.getString("AddToTheGroup", NUM));
                    }
                    ContactsActivity contactsActivity = new ContactsActivity(bundle);
                    contactsActivity.setDelegate(new -$$Lambda$ChatUsersActivity$2Od6Pvar_WsFgSKk8i5ZAWdLCLASSNAMEU(this));
                    presentFragment(contactsActivity);
                }
                return;
            } else if (i3 == this.recentActionsRow) {
                presentFragment(new ChannelAdminLogActivity(this.currentChat));
                return;
            } else if (i3 == this.removedUsersRow) {
                bundle = new Bundle();
                bundle.putInt(str2, this.chatId);
                bundle.putInt(str, 0);
                chatUsersActivity = new ChatUsersActivity(bundle);
                chatUsersActivity.setInfo(this.info);
                presentFragment(chatUsersActivity);
                return;
            } else if (i3 == this.addNew2Row) {
                presentFragment(new GroupInviteActivity(this.chatId));
                return;
            } else if (i3 > this.permissionsSectionRow && i3 <= this.changeInfoRow) {
                TextCheckCell2 textCheckCell2 = (TextCheckCell2) view;
                if (!textCheckCell2.isEnabled()) {
                    return;
                }
                if (textCheckCell2.hasIcon()) {
                    if (TextUtils.isEmpty(this.currentChat.username) || !(i3 == this.pinMessagesRow || i3 == this.changeInfoRow)) {
                        Toast.makeText(getParentActivity(), LocaleController.getString("EditCantEditPermissions", NUM), 0).show();
                    } else {
                        Toast.makeText(getParentActivity(), LocaleController.getString("EditCantEditPermissionsPublic", NUM), 0).show();
                    }
                    return;
                }
                textCheckCell2.setChecked(textCheckCell2.isChecked() ^ 1);
                if (i3 == this.changeInfoRow) {
                    tL_chatBannedRights = this.defaultBannedRights;
                    tL_chatBannedRights.change_info ^= 1;
                } else if (i3 == this.addUsersRow) {
                    tL_chatBannedRights = this.defaultBannedRights;
                    tL_chatBannedRights.invite_users ^= 1;
                } else if (i3 == this.pinMessagesRow) {
                    tL_chatBannedRights = this.defaultBannedRights;
                    tL_chatBannedRights.pin_messages ^= 1;
                } else {
                    isChecked = textCheckCell2.isChecked() ^ 1;
                    if (i3 == this.sendMessagesRow) {
                        tL_chatBannedRights = this.defaultBannedRights;
                        tL_chatBannedRights.send_messages ^= 1;
                    } else if (i3 == this.sendMediaRow) {
                        tL_chatBannedRights = this.defaultBannedRights;
                        tL_chatBannedRights.send_media ^= 1;
                    } else if (i3 == this.sendStickersRow) {
                        tL_chatBannedRights = this.defaultBannedRights;
                        i4 = tL_chatBannedRights.send_stickers ^ 1;
                        tL_chatBannedRights.send_inline = i4;
                        tL_chatBannedRights.send_gifs = i4;
                        tL_chatBannedRights.send_games = i4;
                        tL_chatBannedRights.send_stickers = i4;
                    } else if (i3 == this.embedLinksRow) {
                        tL_chatBannedRights = this.defaultBannedRights;
                        tL_chatBannedRights.embed_links ^= 1;
                    } else if (i3 == this.sendPollsRow) {
                        tL_chatBannedRights = this.defaultBannedRights;
                        tL_chatBannedRights.send_polls ^= 1;
                    }
                    ViewHolder findViewHolderForAdapterPosition;
                    if (isChecked != 0) {
                        tL_chatBannedRights = this.defaultBannedRights;
                        if (tL_chatBannedRights.view_messages && !tL_chatBannedRights.send_messages) {
                            tL_chatBannedRights.send_messages = true;
                            findViewHolderForAdapterPosition = this.listView.findViewHolderForAdapterPosition(this.sendMessagesRow);
                            if (findViewHolderForAdapterPosition != null) {
                                ((TextCheckCell2) findViewHolderForAdapterPosition.itemView).setChecked(false);
                            }
                        }
                        tL_chatBannedRights = this.defaultBannedRights;
                        if (tL_chatBannedRights.view_messages || tL_chatBannedRights.send_messages) {
                            tL_chatBannedRights = this.defaultBannedRights;
                            if (!tL_chatBannedRights.send_media) {
                                tL_chatBannedRights.send_media = true;
                                findViewHolderForAdapterPosition = this.listView.findViewHolderForAdapterPosition(this.sendMediaRow);
                                if (findViewHolderForAdapterPosition != null) {
                                    ((TextCheckCell2) findViewHolderForAdapterPosition.itemView).setChecked(false);
                                }
                            }
                        }
                        tL_chatBannedRights = this.defaultBannedRights;
                        if (tL_chatBannedRights.view_messages || tL_chatBannedRights.send_messages) {
                            tL_chatBannedRights = this.defaultBannedRights;
                            if (!tL_chatBannedRights.send_polls) {
                                tL_chatBannedRights.send_polls = true;
                                findViewHolderForAdapterPosition = this.listView.findViewHolderForAdapterPosition(this.sendPollsRow);
                                if (findViewHolderForAdapterPosition != null) {
                                    ((TextCheckCell2) findViewHolderForAdapterPosition.itemView).setChecked(false);
                                }
                            }
                        }
                        tL_chatBannedRights = this.defaultBannedRights;
                        if (tL_chatBannedRights.view_messages || tL_chatBannedRights.send_messages) {
                            tL_chatBannedRights = this.defaultBannedRights;
                            if (!tL_chatBannedRights.send_stickers) {
                                tL_chatBannedRights.send_inline = true;
                                tL_chatBannedRights.send_gifs = true;
                                tL_chatBannedRights.send_games = true;
                                tL_chatBannedRights.send_stickers = true;
                                findViewHolderForAdapterPosition = this.listView.findViewHolderForAdapterPosition(this.sendStickersRow);
                                if (findViewHolderForAdapterPosition != null) {
                                    ((TextCheckCell2) findViewHolderForAdapterPosition.itemView).setChecked(false);
                                }
                            }
                        }
                        tL_chatBannedRights = this.defaultBannedRights;
                        if (tL_chatBannedRights.view_messages || tL_chatBannedRights.send_messages) {
                            tL_chatBannedRights = this.defaultBannedRights;
                            if (!tL_chatBannedRights.embed_links) {
                                tL_chatBannedRights.embed_links = true;
                                findViewHolderForAdapterPosition = this.listView.findViewHolderForAdapterPosition(this.embedLinksRow);
                                if (findViewHolderForAdapterPosition != null) {
                                    ((TextCheckCell2) findViewHolderForAdapterPosition.itemView).setChecked(false);
                                }
                            }
                        }
                    } else {
                        tL_chatBannedRights = this.defaultBannedRights;
                        if (!(tL_chatBannedRights.embed_links && tL_chatBannedRights.send_inline && tL_chatBannedRights.send_media && tL_chatBannedRights.send_polls)) {
                            tL_chatBannedRights = this.defaultBannedRights;
                            if (tL_chatBannedRights.send_messages) {
                                tL_chatBannedRights.send_messages = false;
                                findViewHolderForAdapterPosition = this.listView.findViewHolderForAdapterPosition(this.sendMessagesRow);
                                if (findViewHolderForAdapterPosition != null) {
                                    ((TextCheckCell2) findViewHolderForAdapterPosition.itemView).setChecked(true);
                                }
                            }
                        }
                    }
                }
                return;
            }
        }
        TLObject item;
        ChannelParticipant channelParticipant;
        int i5;
        if (obj != null) {
            item = this.listViewAdapter.getItem(i3);
            if (item instanceof ChannelParticipant) {
                channelParticipant = (ChannelParticipant) item;
                i5 = channelParticipant.user_id;
                tL_chatBannedRights2 = channelParticipant.banned_rights;
                TL_chatAdminRights tL_chatAdminRights2 = channelParticipant.admin_rights;
                boolean z2 = !((channelParticipant instanceof TL_channelParticipantAdmin) || (channelParticipant instanceof TL_channelParticipantCreator)) || channelParticipant.can_edit;
                if (item instanceof TL_channelParticipantCreator) {
                    tL_chatAdminRights2 = new TL_chatAdminRights();
                    tL_chatAdminRights2.add_admins = true;
                    tL_chatAdminRights2.pin_messages = true;
                    tL_chatAdminRights2.invite_users = true;
                    tL_chatAdminRights2.ban_users = true;
                    tL_chatAdminRights2.delete_messages = true;
                    tL_chatAdminRights2.edit_messages = true;
                    tL_chatAdminRights2.post_messages = true;
                    tL_chatAdminRights2.change_info = true;
                }
                tLObject = item;
                tL_chatAdminRights = tL_chatAdminRights2;
                i2 = i5;
                z = z2;
            } else if (item instanceof ChatParticipant) {
                TL_chatAdminRights tL_chatAdminRights3;
                isChecked = ((ChatParticipant) item).user_id;
                z = this.currentChat.creator;
                if (item instanceof TL_chatParticipantCreator) {
                    tL_chatAdminRights3 = new TL_chatAdminRights();
                    tL_chatAdminRights3.add_admins = true;
                    tL_chatAdminRights3.pin_messages = true;
                    tL_chatAdminRights3.invite_users = true;
                    tL_chatAdminRights3.ban_users = true;
                    tL_chatAdminRights3.delete_messages = true;
                    tL_chatAdminRights3.edit_messages = true;
                    tL_chatAdminRights3.post_messages = true;
                    tL_chatAdminRights3.change_info = true;
                } else {
                    tL_chatAdminRights3 = null;
                }
                tLObject = item;
                i2 = isChecked;
                tL_chatAdminRights = tL_chatAdminRights3;
                tL_chatBannedRights2 = null;
            } else {
                tLObject = item;
                tL_chatBannedRights2 = null;
                tL_chatAdminRights = tL_chatBannedRights2;
                z = false;
                i2 = 0;
            }
        } else {
            item = this.searchListViewAdapter.getItem(i3);
            if (item instanceof User) {
                User user = (User) item;
                MessagesController.getInstance(this.currentAccount).putUser(user, false);
                i3 = user.id;
                isChecked = i3;
                item = getAnyParticipant(i3);
            } else {
                if (!((item instanceof ChannelParticipant) || (item instanceof ChatParticipant))) {
                    item = null;
                }
                isChecked = 0;
            }
            if (item instanceof ChannelParticipant) {
                if (!(item instanceof TL_channelParticipantCreator)) {
                    channelParticipant = (ChannelParticipant) item;
                    i5 = channelParticipant.user_id;
                    boolean z3 = !((channelParticipant instanceof TL_channelParticipantAdmin) || (channelParticipant instanceof TL_channelParticipantCreator)) || channelParticipant.can_edit;
                    TL_chatBannedRights tL_chatBannedRights3 = channelParticipant.banned_rights;
                    tLObject = item;
                    tL_chatAdminRights = channelParticipant.admin_rights;
                    TL_chatBannedRights tL_chatBannedRights4 = tL_chatBannedRights3;
                    i2 = i5;
                    z = z3;
                    tL_chatBannedRights2 = tL_chatBannedRights4;
                } else {
                    return;
                }
            } else if (item instanceof ChatParticipant) {
                if (!(item instanceof TL_chatParticipantCreator)) {
                    isChecked = ((ChatParticipant) item).user_id;
                    z = this.currentChat.creator;
                    tLObject = item;
                    i2 = isChecked;
                    tL_chatBannedRights2 = null;
                    tL_chatAdminRights = tL_chatBannedRights2;
                } else {
                    return;
                }
            } else if (item == null) {
                tLObject = item;
                i2 = isChecked;
                tL_chatBannedRights2 = null;
                tL_chatAdminRights = tL_chatBannedRights2;
                z = true;
            } else {
                tLObject = item;
                i2 = isChecked;
                tL_chatBannedRights2 = null;
                tL_chatAdminRights = tL_chatBannedRights2;
                z = false;
            }
        }
        if (i2 != 0) {
            i3 = this.selectType;
            if (i3 == 0) {
                boolean z4;
                boolean z5;
                i3 = this.type;
                if (i3 == 1) {
                    z4 = i2 != UserConfig.getInstance(this.currentAccount).getClientUserId() && (this.currentChat.creator || z);
                } else if (i3 == 0 || i3 == 3) {
                    z4 = ChatObject.canBlockUsers(this.currentChat);
                } else {
                    z5 = false;
                    i3 = this.type;
                    if (i3 != 0 || ((i3 != 1 && this.isChannel) || (this.type == 2 && this.selectType == 0))) {
                        bundle = new Bundle();
                        bundle.putInt("user_id", i2);
                        presentFragment(new ProfileActivity(bundle));
                    } else {
                        TL_chatBannedRights tL_chatBannedRights5;
                        if (tL_chatBannedRights2 == null) {
                            tL_chatBannedRights = new TL_chatBannedRights();
                            tL_chatBannedRights.view_messages = true;
                            tL_chatBannedRights.send_stickers = true;
                            tL_chatBannedRights.send_media = true;
                            tL_chatBannedRights.embed_links = true;
                            tL_chatBannedRights.send_messages = true;
                            tL_chatBannedRights.send_games = true;
                            tL_chatBannedRights.send_inline = true;
                            tL_chatBannedRights.send_gifs = true;
                            tL_chatBannedRights.pin_messages = true;
                            tL_chatBannedRights.send_polls = true;
                            tL_chatBannedRights.invite_users = true;
                            tL_chatBannedRights.change_info = true;
                            tL_chatBannedRights5 = tL_chatBannedRights;
                        } else {
                            tL_chatBannedRights5 = tL_chatBannedRights2;
                        }
                        TLObject tLObject2 = tLObject;
                        ChatRightsEditActivity chatRightsEditActivity = new ChatRightsEditActivity(i2, this.chatId, tL_chatAdminRights, this.defaultBannedRights, tL_chatBannedRights5, this.type == 1 ? 0 : 1, z5, tLObject == null);
                        chatRightsEditActivity.setDelegate(new -$$Lambda$ChatUsersActivity$JcTkCAgYxQopJ0jZbNv1_IBjprA(this, tLObject2));
                        presentFragment(chatRightsEditActivity);
                    }
                }
                z5 = z4;
                i3 = this.type;
                if (i3 != 0) {
                }
                bundle = new Bundle();
                bundle.putInt("user_id", i2);
                presentFragment(new ProfileActivity(bundle));
            } else if (i3 != 3 && i3 != 1) {
                removeUser(i2);
            } else if (this.selectType != 1 && z && ((tLObject instanceof TL_channelParticipantAdmin) || (tLObject instanceof TL_chatParticipantAdmin))) {
                User user2 = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(i2));
                Builder builder = new Builder(getParentActivity());
                builder.setTitle(LocaleController.getString("AppName", NUM));
                builder.setMessage(LocaleController.formatString("AdminWillBeRemoved", NUM, ContactsController.formatName(user2.first_name, user2.last_name)));
                builder.setPositiveButton(LocaleController.getString("OK", NUM), new -$$Lambda$ChatUsersActivity$CXAjWFCq-svxR_-Wkak6D9yuAyU(this, user2, tLObject, tL_chatAdminRights, tL_chatBannedRights2, z));
                builder.setNegativeButton(LocaleController.getString("Cancel", NUM), null);
                showDialog(builder.create());
            } else {
                openRightsEdit(i2, tLObject, tL_chatAdminRights, tL_chatBannedRights2, z, this.selectType == 1 ? 0 : 1, false);
            }
        }
    }

    public /* synthetic */ void lambda$null$1$ChatUsersActivity(int i, TLObject tLObject) {
        if (tLObject != null && this.participantsMap.get(i) == null) {
            this.participants.add(tLObject);
            Collections.sort(this.participants, new -$$Lambda$ChatUsersActivity$M5Fh2EarJwZERsobLDwzO3z4iPk(this));
            updateRows();
            this.listViewAdapter.notifyDataSetChanged();
        }
    }

    public /* synthetic */ int lambda$null$0$ChatUsersActivity(TLObject tLObject, TLObject tLObject2) {
        int channelAdminParticipantType = getChannelAdminParticipantType(tLObject);
        int channelAdminParticipantType2 = getChannelAdminParticipantType(tLObject2);
        if (channelAdminParticipantType > channelAdminParticipantType2) {
            return 1;
        }
        return channelAdminParticipantType < channelAdminParticipantType2 ? -1 : 0;
    }

    public /* synthetic */ void lambda$null$2$ChatUsersActivity(User user, String str, ContactsActivity contactsActivity) {
        if (user != null && user.bot && this.isChannel) {
            openRightsEdit(user.id, null, null, null, true, 0, true);
        } else {
            MessagesController.getInstance(this.currentAccount).addUserToChat(this.chatId, user, null, str != null ? Utilities.parseInt(str).intValue() : 0, null, this, null);
        }
    }

    public /* synthetic */ void lambda$null$3$ChatUsersActivity(User user, TLObject tLObject, TL_chatAdminRights tL_chatAdminRights, TL_chatBannedRights tL_chatBannedRights, boolean z, DialogInterface dialogInterface, int i) {
        openRightsEdit(user.id, tLObject, tL_chatAdminRights, tL_chatBannedRights, z, this.selectType == 1 ? 0 : 1, false);
    }

    public /* synthetic */ void lambda$null$4$ChatUsersActivity(TLObject tLObject, int i, TL_chatAdminRights tL_chatAdminRights, TL_chatBannedRights tL_chatBannedRights) {
        if (tLObject instanceof ChannelParticipant) {
            ChannelParticipant channelParticipant = (ChannelParticipant) tLObject;
            channelParticipant.admin_rights = tL_chatAdminRights;
            channelParticipant.banned_rights = tL_chatBannedRights;
            updateParticipantWithRights(channelParticipant, tL_chatAdminRights, tL_chatBannedRights, 0, false);
        }
    }

    public /* synthetic */ boolean lambda$createView$6$ChatUsersActivity(View view, int i) {
        if (getParentActivity() == null) {
            return false;
        }
        Adapter adapter = this.listView.getAdapter();
        Adapter adapter2 = this.listViewAdapter;
        return adapter == adapter2 && createMenuForParticipant(adapter2.getItem(i), false);
    }

    private void openRightsEdit2(int i, int i2, TLObject tLObject, TL_chatAdminRights tL_chatAdminRights, TL_chatBannedRights tL_chatBannedRights, boolean z, int i3, boolean z2) {
        ChatRightsEditActivity chatRightsEditActivity = new ChatRightsEditActivity(i, this.chatId, tL_chatAdminRights, this.defaultBannedRights, tL_chatBannedRights, i3, true, false);
        int i4 = i2;
        chatRightsEditActivity.setDelegate(new -$$Lambda$ChatUsersActivity$Kyu7zszJl0VfD2ZAoEcfYKjDAYw(this, i3, i, i2));
        presentFragment(chatRightsEditActivity);
    }

    public /* synthetic */ void lambda$openRightsEdit2$7$ChatUsersActivity(int i, int i2, int i3, int i4, TL_chatAdminRights tL_chatAdminRights, TL_chatBannedRights tL_chatBannedRights) {
        if (i == 0) {
            for (int i5 = 0; i5 < this.participants.size(); i5++) {
                TLObject tLObject = (TLObject) this.participants.get(i5);
                if (tLObject instanceof ChannelParticipant) {
                    if (((ChannelParticipant) tLObject).user_id == i2) {
                        ChannelParticipant tL_channelParticipantAdmin;
                        if (i4 == 1) {
                            tL_channelParticipantAdmin = new TL_channelParticipantAdmin();
                        } else {
                            tL_channelParticipantAdmin = new TL_channelParticipant();
                        }
                        tL_channelParticipantAdmin.admin_rights = tL_chatAdminRights;
                        tL_channelParticipantAdmin.banned_rights = tL_chatBannedRights;
                        tL_channelParticipantAdmin.inviter_id = UserConfig.getInstance(this.currentAccount).getClientUserId();
                        tL_channelParticipantAdmin.user_id = i2;
                        tL_channelParticipantAdmin.date = i3;
                        this.participants.set(i5, tL_channelParticipantAdmin);
                        return;
                    }
                } else if (tLObject instanceof ChatParticipant) {
                    ChatParticipant tL_chatParticipantAdmin;
                    ChatParticipant chatParticipant = (ChatParticipant) tLObject;
                    if (i4 == 1) {
                        tL_chatParticipantAdmin = new TL_chatParticipantAdmin();
                    } else {
                        tL_chatParticipantAdmin = new TL_chatParticipant();
                    }
                    tL_chatParticipantAdmin.user_id = chatParticipant.user_id;
                    tL_chatParticipantAdmin.date = chatParticipant.date;
                    tL_chatParticipantAdmin.inviter_id = chatParticipant.inviter_id;
                    int indexOf = this.info.participants.participants.indexOf(chatParticipant);
                    if (indexOf >= 0) {
                        this.info.participants.participants.set(indexOf, tL_chatParticipantAdmin);
                    }
                    loadChatParticipants(0, 200);
                }
            }
        } else if (i == 1 && i4 == 0) {
            removeParticipants(i2);
        }
    }

    private void openRightsEdit(int i, TLObject tLObject, TL_chatAdminRights tL_chatAdminRights, TL_chatBannedRights tL_chatBannedRights, boolean z, int i2, boolean z2) {
        ChatRightsEditActivity chatRightsEditActivity = new ChatRightsEditActivity(i, this.chatId, tL_chatAdminRights, this.defaultBannedRights, tL_chatBannedRights, i2, z, tLObject == null);
        chatRightsEditActivity.setDelegate(new -$$Lambda$ChatUsersActivity$x7vDWsxGmRxzLBu7fqQngDb3TOM(this, tLObject));
        presentFragment(chatRightsEditActivity, z2);
    }

    public /* synthetic */ void lambda$openRightsEdit$8$ChatUsersActivity(TLObject tLObject, int i, TL_chatAdminRights tL_chatAdminRights, TL_chatBannedRights tL_chatBannedRights) {
        if (tLObject instanceof ChannelParticipant) {
            ChannelParticipant channelParticipant = (ChannelParticipant) tLObject;
            channelParticipant.admin_rights = tL_chatAdminRights;
            channelParticipant.banned_rights = tL_chatBannedRights;
        }
        removeSelfFromStack();
    }

    private void removeUser(int i) {
        if (ChatObject.isChannel(this.currentChat)) {
            MessagesController.getInstance(this.currentAccount).deleteUserFromChat(this.chatId, MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(i)), null);
            finishFragment();
        }
    }

    private TLObject getAnyParticipant(int i) {
        for (int i2 = 0; i2 < 3; i2++) {
            SparseArray sparseArray;
            if (i2 == 0) {
                sparseArray = this.contactsMap;
            } else if (i2 == 1) {
                sparseArray = this.botsMap;
            } else {
                sparseArray = this.participantsMap;
            }
            TLObject tLObject = (TLObject) sparseArray.get(i);
            if (tLObject != null) {
                return tLObject;
            }
        }
        return null;
    }

    private void removeParticipants(TLObject tLObject) {
        if (tLObject instanceof ChatParticipant) {
            removeParticipants(((ChatParticipant) tLObject).user_id);
        } else if (tLObject instanceof ChannelParticipant) {
            removeParticipants(((ChannelParticipant) tLObject).user_id);
        }
    }

    private void removeParticipants(int i) {
        Object obj = null;
        for (int i2 = 0; i2 < 3; i2++) {
            SparseArray sparseArray;
            ArrayList arrayList;
            if (i2 == 0) {
                sparseArray = this.contactsMap;
                arrayList = this.contacts;
            } else if (i2 == 1) {
                sparseArray = this.botsMap;
                arrayList = this.bots;
            } else {
                sparseArray = this.participantsMap;
                arrayList = this.participants;
            }
            TLObject tLObject = (TLObject) sparseArray.get(i);
            if (tLObject != null) {
                sparseArray.remove(i);
                arrayList.remove(tLObject);
                obj = 1;
            }
        }
        if (obj != null) {
            updateRows();
            this.listViewAdapter.notifyDataSetChanged();
        }
    }

    private void updateParticipantWithRights(ChannelParticipant channelParticipant, TL_chatAdminRights tL_chatAdminRights, TL_chatBannedRights tL_chatBannedRights, int i, boolean z) {
        Object obj = null;
        for (int i2 = 0; i2 < 3; i2++) {
            SparseArray sparseArray;
            if (i2 == 0) {
                sparseArray = this.contactsMap;
            } else if (i2 == 1) {
                sparseArray = this.botsMap;
            } else {
                sparseArray = this.participantsMap;
            }
            TLObject tLObject = (TLObject) sparseArray.get(channelParticipant.user_id);
            if (tLObject instanceof ChannelParticipant) {
                channelParticipant = (ChannelParticipant) tLObject;
                channelParticipant.admin_rights = tL_chatAdminRights;
                channelParticipant.banned_rights = tL_chatBannedRights;
                if (z) {
                    channelParticipant.promoted_by = UserConfig.getInstance(this.currentAccount).getClientUserId();
                }
            }
            if (z && tLObject != null && obj == null) {
                ChatUsersActivityDelegate chatUsersActivityDelegate = this.delegate;
                if (chatUsersActivityDelegate != null) {
                    chatUsersActivityDelegate.didAddParticipantToList(i, tLObject);
                    obj = 1;
                }
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:123:0x0291  */
    /* JADX WARNING: Removed duplicated region for block: B:122:0x0290 A:{RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:122:0x0290 A:{RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:123:0x0291  */
    /* JADX WARNING: Removed duplicated region for block: B:123:0x0291  */
    /* JADX WARNING: Removed duplicated region for block: B:122:0x0290 A:{RETURN} */
    private boolean createMenuForParticipant(org.telegram.tgnet.TLObject r22, boolean r23) {
        /*
        r21 = this;
        r10 = r21;
        r6 = r22;
        if (r6 == 0) goto L_0x02c7;
    L_0x0006:
        r1 = r10.selectType;
        if (r1 == 0) goto L_0x000c;
    L_0x000a:
        goto L_0x02c7;
    L_0x000c:
        r1 = r6 instanceof org.telegram.tgnet.TLRPC.ChannelParticipant;
        if (r1 == 0) goto L_0x0023;
    L_0x0010:
        r1 = r6;
        r1 = (org.telegram.tgnet.TLRPC.ChannelParticipant) r1;
        r3 = r1.user_id;
        r4 = r1.can_edit;
        r5 = r1.banned_rights;
        r7 = r1.admin_rights;
        r1 = r1.date;
        r9 = r5;
        r8 = r7;
        r7 = r1;
        r1 = r4;
    L_0x0021:
        r4 = r3;
        goto L_0x003e;
    L_0x0023:
        r1 = r6 instanceof org.telegram.tgnet.TLRPC.ChatParticipant;
        if (r1 == 0) goto L_0x0039;
    L_0x0027:
        r1 = r6;
        r1 = (org.telegram.tgnet.TLRPC.ChatParticipant) r1;
        r3 = r1.user_id;
        r1 = r1.date;
        r4 = r10.currentChat;
        r4 = org.telegram.messenger.ChatObject.canAddAdmins(r4);
        r7 = r1;
        r1 = r4;
        r8 = 0;
        r9 = 0;
        goto L_0x0021;
    L_0x0039:
        r1 = 0;
        r4 = 0;
        r7 = 0;
        r8 = 0;
        r9 = 0;
    L_0x003e:
        if (r4 == 0) goto L_0x02c5;
    L_0x0040:
        r3 = r10.currentAccount;
        r3 = org.telegram.messenger.UserConfig.getInstance(r3);
        r3 = r3.getClientUserId();
        if (r4 != r3) goto L_0x004e;
    L_0x004c:
        goto L_0x02c5;
    L_0x004e:
        r3 = r10.type;
        r5 = NUM; // 0x7f0d036a float:1.8743887E38 double:1.0531302094E-314;
        r11 = "EditAdminRights";
        r12 = "dialogRedIcon";
        r13 = "dialogTextRed2";
        r15 = 2;
        r14 = 1;
        if (r3 != r15) goto L_0x01be;
    L_0x005d:
        r3 = r10.currentAccount;
        r3 = org.telegram.messenger.MessagesController.getInstance(r3);
        r2 = java.lang.Integer.valueOf(r4);
        r3 = r3.getUser(r2);
        r2 = r10.currentChat;
        r2 = org.telegram.messenger.ChatObject.canAddAdmins(r2);
        if (r2 == 0) goto L_0x0083;
    L_0x0073:
        r2 = r6 instanceof org.telegram.tgnet.TLRPC.TL_channelParticipant;
        if (r2 != 0) goto L_0x0081;
    L_0x0077:
        r2 = r6 instanceof org.telegram.tgnet.TLRPC.TL_channelParticipantBanned;
        if (r2 != 0) goto L_0x0081;
    L_0x007b:
        r2 = r6 instanceof org.telegram.tgnet.TLRPC.TL_chatParticipant;
        if (r2 != 0) goto L_0x0081;
    L_0x007f:
        if (r1 == 0) goto L_0x0083;
    L_0x0081:
        r2 = 1;
        goto L_0x0084;
    L_0x0083:
        r2 = 0;
    L_0x0084:
        r15 = r6 instanceof org.telegram.tgnet.TLRPC.TL_channelParticipantAdmin;
        if (r15 != 0) goto L_0x0094;
    L_0x0088:
        r0 = r6 instanceof org.telegram.tgnet.TLRPC.TL_channelParticipantCreator;
        if (r0 != 0) goto L_0x0094;
    L_0x008c:
        r0 = r6 instanceof org.telegram.tgnet.TLRPC.TL_chatParticipantCreator;
        if (r0 != 0) goto L_0x0094;
    L_0x0090:
        r0 = r6 instanceof org.telegram.tgnet.TLRPC.TL_chatParticipantAdmin;
        if (r0 == 0) goto L_0x0096;
    L_0x0094:
        if (r1 == 0) goto L_0x0099;
    L_0x0096:
        r17 = 1;
        goto L_0x009b;
    L_0x0099:
        r17 = 0;
    L_0x009b:
        if (r15 != 0) goto L_0x00a4;
    L_0x009d:
        r0 = r6 instanceof org.telegram.tgnet.TLRPC.TL_chatParticipantAdmin;
        if (r0 == 0) goto L_0x00a2;
    L_0x00a1:
        goto L_0x00a4;
    L_0x00a2:
        r0 = 0;
        goto L_0x00a5;
    L_0x00a4:
        r0 = 1;
    L_0x00a5:
        if (r23 != 0) goto L_0x00bc;
    L_0x00a7:
        r1 = new java.util.ArrayList;
        r1.<init>();
        r15 = new java.util.ArrayList;
        r15.<init>();
        r16 = new java.util.ArrayList;
        r16.<init>();
        r18 = r15;
        r15 = r1;
        r1 = r16;
        goto L_0x00c0;
    L_0x00bc:
        r1 = 0;
        r15 = 0;
        r18 = 0;
    L_0x00c0:
        if (r2 == 0) goto L_0x00ed;
    L_0x00c2:
        if (r23 == 0) goto L_0x00c5;
    L_0x00c4:
        return r14;
    L_0x00c5:
        if (r0 == 0) goto L_0x00cc;
    L_0x00c7:
        r0 = org.telegram.messenger.LocaleController.getString(r11, r5);
        goto L_0x00d5;
    L_0x00cc:
        r0 = NUM; // 0x7f0d08db float:1.8746713E38 double:1.0531308976E-314;
        r2 = "SetAsAdmin";
        r0 = org.telegram.messenger.LocaleController.getString(r2, r0);
    L_0x00d5:
        r15.add(r0);
        r0 = NUM; // 0x7var_ float:1.7944754E38 double:1.052935546E-314;
        r0 = java.lang.Integer.valueOf(r0);
        r1.add(r0);
        r0 = 0;
        r2 = java.lang.Integer.valueOf(r0);
        r5 = r18;
        r5.add(r2);
        goto L_0x00ef;
    L_0x00ed:
        r5 = r18;
    L_0x00ef:
        r0 = r10.currentChat;
        r0 = org.telegram.messenger.ChatObject.canBlockUsers(r0);
        if (r0 == 0) goto L_0x0164;
    L_0x00f7:
        if (r17 == 0) goto L_0x0164;
    L_0x00f9:
        if (r23 == 0) goto L_0x00fc;
    L_0x00fb:
        return r14;
    L_0x00fc:
        r0 = r10.isChannel;
        if (r0 != 0) goto L_0x0144;
    L_0x0100:
        r0 = r10.currentChat;
        r0 = org.telegram.messenger.ChatObject.isChannel(r0);
        if (r0 == 0) goto L_0x0125;
    L_0x0108:
        r0 = NUM; // 0x7f0d01f0 float:1.874312E38 double:1.0531300226E-314;
        r2 = "ChangePermissions";
        r0 = org.telegram.messenger.LocaleController.getString(r2, r0);
        r15.add(r0);
        r0 = NUM; // 0x7var_ float:1.7944758E38 double:1.052935547E-314;
        r0 = java.lang.Integer.valueOf(r0);
        r1.add(r0);
        r0 = java.lang.Integer.valueOf(r14);
        r5.add(r0);
    L_0x0125:
        r0 = NUM; // 0x7f0d04f3 float:1.8744685E38 double:1.0531304035E-314;
        r2 = "KickFromGroup";
        r0 = org.telegram.messenger.LocaleController.getString(r2, r0);
        r15.add(r0);
        r0 = NUM; // 0x7var_a float:1.794476E38 double:1.0529355475E-314;
        r0 = java.lang.Integer.valueOf(r0);
        r1.add(r0);
        r2 = 2;
        r0 = java.lang.Integer.valueOf(r2);
        r5.add(r0);
        goto L_0x0162;
    L_0x0144:
        r0 = NUM; // 0x7var_a float:1.794476E38 double:1.0529355475E-314;
        r2 = 2;
        r11 = NUM; // 0x7f0d0243 float:1.874329E38 double:1.0531300636E-314;
        r14 = "ChannelRemoveUser";
        r11 = org.telegram.messenger.LocaleController.getString(r14, r11);
        r15.add(r11);
        r0 = java.lang.Integer.valueOf(r0);
        r1.add(r0);
        r0 = java.lang.Integer.valueOf(r2);
        r5.add(r0);
    L_0x0162:
        r11 = 1;
        goto L_0x0165;
    L_0x0164:
        r11 = 0;
    L_0x0165:
        if (r5 == 0) goto L_0x01bc;
    L_0x0167:
        r0 = r5.isEmpty();
        if (r0 == 0) goto L_0x016e;
    L_0x016d:
        goto L_0x01bc;
    L_0x016e:
        r14 = new org.telegram.ui.ActionBar.AlertDialog$Builder;
        r0 = r21.getParentActivity();
        r14.<init>(r0);
        r0 = r5.size();
        r0 = new java.lang.CharSequence[r0];
        r0 = r15.toArray(r0);
        r2 = r0;
        r2 = (java.lang.CharSequence[]) r2;
        r1 = org.telegram.messenger.AndroidUtilities.toIntArray(r1);
        r0 = new org.telegram.ui.-$$Lambda$ChatUsersActivity$fQaNHUdSBmy7dmN9qR2gFkfx4CQ;
        r23 = r0;
        r19 = r12;
        r12 = r1;
        r1 = r21;
        r20 = r13;
        r13 = r2;
        r2 = r5;
        r5 = r17;
        r6 = r22;
        r0.<init>(r1, r2, r3, r4, r5, r6, r7, r8, r9);
        r14.setItems(r13, r12, r0);
        r0 = r14.create();
        r10.showDialog(r0);
        if (r11 == 0) goto L_0x01b9;
    L_0x01a8:
        r1 = r15.size();
        r2 = 1;
        r1 = r1 - r2;
        r2 = org.telegram.ui.ActionBar.Theme.getColor(r20);
        r3 = org.telegram.ui.ActionBar.Theme.getColor(r19);
        r0.setItemColor(r1, r2, r3);
    L_0x01b9:
        r2 = 1;
        goto L_0x02c4;
    L_0x01bc:
        r0 = 0;
        return r0;
    L_0x01be:
        r19 = r12;
        r20 = r13;
        r0 = 3;
        r2 = NUM; // 0x7f0d0213 float:1.8743192E38 double:1.05313004E-314;
        r7 = "ChannelDeleteFromList";
        if (r3 != r0) goto L_0x01f6;
    L_0x01ca:
        r0 = r10.currentChat;
        r0 = org.telegram.messenger.ChatObject.canBlockUsers(r0);
        if (r0 == 0) goto L_0x01f6;
    L_0x01d2:
        if (r23 == 0) goto L_0x01d6;
    L_0x01d4:
        r0 = 1;
        return r0;
    L_0x01d6:
        r0 = 1;
        r1 = 2;
        r3 = new java.lang.CharSequence[r1];
        r5 = NUM; // 0x7f0d0218 float:1.8743202E38 double:1.0531300424E-314;
        r11 = "ChannelEditPermissions";
        r5 = org.telegram.messenger.LocaleController.getString(r11, r5);
        r11 = 0;
        r3[r11] = r5;
        r2 = org.telegram.messenger.LocaleController.getString(r7, r2);
        r3[r0] = r2;
        r2 = new int[r1];
        r2 = {NUM, NUM};
        r11 = r2;
        r12 = r3;
    L_0x01f3:
        r7 = 0;
        goto L_0x028e;
    L_0x01f6:
        r0 = r10.type;
        if (r0 != 0) goto L_0x0239;
    L_0x01fa:
        r0 = r10.currentChat;
        r0 = org.telegram.messenger.ChatObject.canBlockUsers(r0);
        if (r0 == 0) goto L_0x0239;
    L_0x0202:
        if (r23 == 0) goto L_0x0206;
    L_0x0204:
        r0 = 1;
        return r0;
    L_0x0206:
        r0 = 2;
        r1 = new java.lang.CharSequence[r0];
        r0 = r10.currentChat;
        r0 = org.telegram.messenger.ChatObject.canAddUsers(r0);
        if (r0 == 0) goto L_0x0225;
    L_0x0211:
        r0 = r10.isChannel;
        if (r0 == 0) goto L_0x021b;
    L_0x0215:
        r0 = NUM; // 0x7f0d01fc float:1.8743145E38 double:1.0531300285E-314;
        r3 = "ChannelAddToChannel";
        goto L_0x0220;
    L_0x021b:
        r0 = NUM; // 0x7f0d01fd float:1.8743147E38 double:1.053130029E-314;
        r3 = "ChannelAddToGroup";
    L_0x0220:
        r0 = org.telegram.messenger.LocaleController.getString(r3, r0);
        goto L_0x0226;
    L_0x0225:
        r0 = 0;
    L_0x0226:
        r3 = 0;
        r1[r3] = r0;
        r0 = org.telegram.messenger.LocaleController.getString(r7, r2);
        r2 = 1;
        r1[r2] = r0;
        r0 = 2;
        r0 = new int[r0];
        r0 = {NUM, NUM};
        r11 = r0;
        r12 = r1;
        goto L_0x01f3;
    L_0x0239:
        r2 = 1;
        r0 = r10.type;
        if (r0 != r2) goto L_0x028b;
    L_0x023e:
        r0 = r10.currentChat;
        r0 = org.telegram.messenger.ChatObject.canAddAdmins(r0);
        if (r0 == 0) goto L_0x028b;
    L_0x0246:
        if (r1 == 0) goto L_0x028b;
    L_0x0248:
        if (r23 == 0) goto L_0x024b;
    L_0x024a:
        return r2;
    L_0x024b:
        r0 = r10.currentChat;
        r0 = r0.creator;
        r2 = NUM; // 0x7f0d0244 float:1.8743291E38 double:1.053130064E-314;
        r3 = "ChannelRemoveUserAdmin";
        if (r0 != 0) goto L_0x0273;
    L_0x0256:
        r0 = r6 instanceof org.telegram.tgnet.TLRPC.TL_channelParticipantCreator;
        if (r0 != 0) goto L_0x0260;
    L_0x025a:
        if (r1 == 0) goto L_0x0260;
    L_0x025c:
        r0 = 1;
        r1 = 2;
        r7 = 0;
        goto L_0x0276;
    L_0x0260:
        r0 = 1;
        r1 = new java.lang.CharSequence[r0];
        r2 = org.telegram.messenger.LocaleController.getString(r3, r2);
        r7 = 0;
        r1[r7] = r2;
        r2 = new int[r0];
        r3 = NUM; // 0x7var_a float:1.794476E38 double:1.0529355475E-314;
        r2[r7] = r3;
        r12 = r1;
        goto L_0x0289;
    L_0x0273:
        r0 = 1;
        r7 = 0;
        r1 = 2;
    L_0x0276:
        r12 = new java.lang.CharSequence[r1];
        r5 = org.telegram.messenger.LocaleController.getString(r11, r5);
        r12[r7] = r5;
        r2 = org.telegram.messenger.LocaleController.getString(r3, r2);
        r12[r0] = r2;
        r2 = new int[r1];
        r2 = {NUM, NUM};
    L_0x0289:
        r11 = r2;
        goto L_0x028e;
    L_0x028b:
        r7 = 0;
        r11 = 0;
        r12 = 0;
    L_0x028e:
        if (r12 != 0) goto L_0x0291;
    L_0x0290:
        return r7;
    L_0x0291:
        r7 = new org.telegram.ui.ActionBar.AlertDialog$Builder;
        r0 = r21.getParentActivity();
        r7.<init>(r0);
        r13 = new org.telegram.ui.-$$Lambda$ChatUsersActivity$3Xtm1JbPQyTJuTz1uE37UNSwNmw;
        r0 = r13;
        r1 = r21;
        r2 = r12;
        r3 = r4;
        r4 = r8;
        r5 = r22;
        r6 = r9;
        r0.<init>(r1, r2, r3, r4, r5, r6);
        r7.setItems(r12, r11, r13);
        r0 = r7.create();
        r10.showDialog(r0);
        r1 = r10.type;
        r2 = 1;
        if (r1 != r2) goto L_0x02c4;
    L_0x02b7:
        r1 = r12.length;
        r1 = r1 - r2;
        r3 = org.telegram.ui.ActionBar.Theme.getColor(r20);
        r4 = org.telegram.ui.ActionBar.Theme.getColor(r19);
        r0.setItemColor(r1, r3, r4);
    L_0x02c4:
        return r2;
    L_0x02c5:
        r0 = 0;
        return r0;
    L_0x02c7:
        r0 = 0;
        return r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.ChatUsersActivity.createMenuForParticipant(org.telegram.tgnet.TLObject, boolean):boolean");
    }

    public /* synthetic */ void lambda$createMenuForParticipant$10$ChatUsersActivity(ArrayList arrayList, User user, int i, boolean z, TLObject tLObject, int i2, TL_chatAdminRights tL_chatAdminRights, TL_chatBannedRights tL_chatBannedRights, DialogInterface dialogInterface, int i3) {
        ArrayList arrayList2 = arrayList;
        User user2 = user;
        TLObject tLObject2 = tLObject;
        int i4 = i3;
        if (((Integer) arrayList2.get(i4)).intValue() == 2) {
            MessagesController.getInstance(this.currentAccount).deleteUserFromChat(this.chatId, user2, null);
            removeParticipants(i);
            if (this.searchItem != null && this.actionBar.isSearchFieldVisible()) {
                this.actionBar.closeSearchField();
                return;
            }
            return;
        }
        int i5 = i;
        if (z && ((tLObject2 instanceof TL_channelParticipantAdmin) || (tLObject2 instanceof TL_chatParticipantAdmin))) {
            Builder builder = new Builder(getParentActivity());
            builder.setTitle(LocaleController.getString("AppName", NUM));
            builder.setMessage(LocaleController.formatString("AdminWillBeRemoved", NUM, ContactsController.formatName(user2.first_name, user2.last_name)));
            builder.setPositiveButton(LocaleController.getString("OK", NUM), new -$$Lambda$ChatUsersActivity$O8J2IXCyrtbbKi3IDFtrFWZr1zo(this, i, i2, tLObject, tL_chatAdminRights, tL_chatBannedRights, z, arrayList, i3));
            builder.setNegativeButton(LocaleController.getString("Cancel", NUM), null);
            showDialog(builder.create());
            return;
        }
        openRightsEdit2(i, i2, tLObject, tL_chatAdminRights, tL_chatBannedRights, z, ((Integer) arrayList2.get(i4)).intValue(), false);
    }

    public /* synthetic */ void lambda$null$9$ChatUsersActivity(int i, int i2, TLObject tLObject, TL_chatAdminRights tL_chatAdminRights, TL_chatBannedRights tL_chatBannedRights, boolean z, ArrayList arrayList, int i3, DialogInterface dialogInterface, int i4) {
        openRightsEdit2(i, i2, tLObject, tL_chatAdminRights, tL_chatBannedRights, z, ((Integer) arrayList.get(i3)).intValue(), false);
    }

    public /* synthetic */ void lambda$createMenuForParticipant$15$ChatUsersActivity(CharSequence[] charSequenceArr, int i, TL_chatAdminRights tL_chatAdminRights, TLObject tLObject, TL_chatBannedRights tL_chatBannedRights, DialogInterface dialogInterface, int i2) {
        int i3 = i;
        TLObject tLObject2 = tLObject;
        int i4 = i2;
        int i5 = this.type;
        ChatRightsEditActivity chatRightsEditActivity;
        if (i5 == 1) {
            if (i4 == 0 && charSequenceArr.length == 2) {
                chatRightsEditActivity = new ChatRightsEditActivity(i, this.chatId, tL_chatAdminRights, null, null, 0, true, false);
                chatRightsEditActivity.setDelegate(new -$$Lambda$ChatUsersActivity$Kj7_HFBJQnmhoJfYSpDZ4Apo2QA(this, tLObject2));
                presentFragment(chatRightsEditActivity);
                return;
            }
            MessagesController.getInstance(this.currentAccount).setUserAdminRole(this.chatId, MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(i)), new TL_chatAdminRights(), 1 ^ this.isChannel, this, false);
            removeParticipants(i3);
        } else if (i5 == 0 || i5 == 3) {
            int i6;
            TLObject tLObject3;
            int i7;
            if (i4 == 0) {
                i5 = this.type;
                if (i5 == 3) {
                    chatRightsEditActivity = new ChatRightsEditActivity(i, this.chatId, null, this.defaultBannedRights, tL_chatBannedRights, 1, true, false);
                    chatRightsEditActivity.setDelegate(new -$$Lambda$ChatUsersActivity$SpD_UzoJRx-1Vha6XOMDXgDySdk(this, tLObject2));
                    presentFragment(chatRightsEditActivity);
                } else if (i5 == 0) {
                    i7 = 1;
                    i6 = i4;
                    tLObject3 = tLObject2;
                    MessagesController.getInstance(this.currentAccount).addUserToChat(this.chatId, MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(i)), null, 0, null, this, null);
                }
                i6 = i4;
                tLObject3 = tLObject2;
                i7 = 1;
            } else {
                i6 = i4;
                tLObject3 = tLObject2;
                i7 = 1;
                if (i6 == 1) {
                    TL_channels_editBanned tL_channels_editBanned = new TL_channels_editBanned();
                    tL_channels_editBanned.user_id = MessagesController.getInstance(this.currentAccount).getInputUser(i3);
                    tL_channels_editBanned.channel = MessagesController.getInstance(this.currentAccount).getInputChannel(this.chatId);
                    tL_channels_editBanned.banned_rights = new TL_chatBannedRights();
                    ConnectionsManager.getInstance(this.currentAccount).sendRequest(tL_channels_editBanned, new -$$Lambda$ChatUsersActivity$hkguo5IPnumabgmW6dzftkQ4W7k(this));
                    if (this.searchItem != null && this.actionBar.isSearchFieldVisible()) {
                        this.actionBar.closeSearchField();
                    }
                }
            }
            if ((i6 == 0 && this.type == 0) || i6 == i7) {
                removeParticipants(tLObject3);
            }
        } else if (i4 == 0) {
            MessagesController.getInstance(this.currentAccount).deleteUserFromChat(this.chatId, MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(i)), null);
        }
    }

    public /* synthetic */ void lambda$null$11$ChatUsersActivity(TLObject tLObject, int i, TL_chatAdminRights tL_chatAdminRights, TL_chatBannedRights tL_chatBannedRights) {
        if (tLObject instanceof ChannelParticipant) {
            ChannelParticipant channelParticipant = (ChannelParticipant) tLObject;
            channelParticipant.admin_rights = tL_chatAdminRights;
            channelParticipant.banned_rights = tL_chatBannedRights;
            updateParticipantWithRights(channelParticipant, tL_chatAdminRights, tL_chatBannedRights, 0, false);
        }
    }

    public /* synthetic */ void lambda$null$12$ChatUsersActivity(TLObject tLObject, int i, TL_chatAdminRights tL_chatAdminRights, TL_chatBannedRights tL_chatBannedRights) {
        if (tLObject instanceof ChannelParticipant) {
            ChannelParticipant channelParticipant = (ChannelParticipant) tLObject;
            channelParticipant.admin_rights = tL_chatAdminRights;
            channelParticipant.banned_rights = tL_chatBannedRights;
            updateParticipantWithRights(channelParticipant, tL_chatAdminRights, tL_chatBannedRights, 0, false);
        }
    }

    public /* synthetic */ void lambda$null$14$ChatUsersActivity(TLObject tLObject, TL_error tL_error) {
        if (tLObject != null) {
            Updates updates = (Updates) tLObject;
            MessagesController.getInstance(this.currentAccount).processUpdates(updates, false);
            if (!updates.chats.isEmpty()) {
                AndroidUtilities.runOnUIThread(new -$$Lambda$ChatUsersActivity$TL00i85qYgTnK9XaK_vPzl4PMDk(this, updates), 1000);
            }
        }
    }

    public /* synthetic */ void lambda$null$13$ChatUsersActivity(Updates updates) {
        MessagesController.getInstance(this.currentAccount).loadFullChat(((Chat) updates.chats.get(0)).id, 0, true);
    }

    public void didReceivedNotification(int i, int i2, Object... objArr) {
        if (i == NotificationCenter.chatInfoDidLoad) {
            ChatFull chatFull = (ChatFull) objArr[0];
            boolean booleanValue = ((Boolean) objArr[2]).booleanValue();
            if (chatFull.id != this.chatId) {
                return;
            }
            if (!booleanValue || !ChatObject.isChannel(this.currentChat)) {
                this.info = chatFull;
                AndroidUtilities.runOnUIThread(new -$$Lambda$ChatUsersActivity$b2pnZ2-tpwva-CAqUoh2e_JlpdY(this));
            }
        }
    }

    public /* synthetic */ void lambda$didReceivedNotification$16$ChatUsersActivity() {
        loadChatParticipants(0, 200);
    }

    public boolean onBackPressed() {
        return checkDiscard();
    }

    public void setDelegate(ChatUsersActivityDelegate chatUsersActivityDelegate) {
        this.delegate = chatUsersActivityDelegate;
    }

    private boolean checkDiscard() {
        if (ChatObject.getBannedRightsString(this.defaultBannedRights).equals(this.initialBannedRights)) {
            return true;
        }
        Builder builder = new Builder(getParentActivity());
        builder.setTitle(LocaleController.getString("UserRestrictionsApplyChanges", NUM));
        if (this.isChannel) {
            builder.setMessage(LocaleController.getString("ChannelSettingsChangedAlert", NUM));
        } else {
            builder.setMessage(LocaleController.getString("GroupSettingsChangedAlert", NUM));
        }
        builder.setPositiveButton(LocaleController.getString("ApplyTheme", NUM), new -$$Lambda$ChatUsersActivity$236mIhGiuKn_O04cfPssFb-GqQI(this));
        builder.setNegativeButton(LocaleController.getString("PassportDiscard", NUM), new -$$Lambda$ChatUsersActivity$NlV7J_LuBKgy9IVFfSXvs9YcOwA(this));
        showDialog(builder.create());
        return false;
    }

    public /* synthetic */ void lambda$checkDiscard$17$ChatUsersActivity(DialogInterface dialogInterface, int i) {
        processDone();
    }

    public /* synthetic */ void lambda$checkDiscard$18$ChatUsersActivity(DialogInterface dialogInterface, int i) {
        finishFragment();
    }

    public boolean hasSelectType() {
        return this.selectType != 0;
    }

    private String formatUserPermissions(TL_chatBannedRights tL_chatBannedRights) {
        if (tL_chatBannedRights == null) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder();
        boolean z = tL_chatBannedRights.view_messages;
        if (z && this.defaultBannedRights.view_messages != z) {
            stringBuilder.append(LocaleController.getString("UserRestrictionsNoRead", NUM));
        }
        z = tL_chatBannedRights.send_messages;
        String str = ", ";
        if (z && this.defaultBannedRights.send_messages != z) {
            if (stringBuilder.length() != 0) {
                stringBuilder.append(str);
            }
            stringBuilder.append(LocaleController.getString("UserRestrictionsNoSend", NUM));
        }
        z = tL_chatBannedRights.send_media;
        if (z && this.defaultBannedRights.send_media != z) {
            if (stringBuilder.length() != 0) {
                stringBuilder.append(str);
            }
            stringBuilder.append(LocaleController.getString("UserRestrictionsNoSendMedia", NUM));
        }
        z = tL_chatBannedRights.send_stickers;
        if (z && this.defaultBannedRights.send_stickers != z) {
            if (stringBuilder.length() != 0) {
                stringBuilder.append(str);
            }
            stringBuilder.append(LocaleController.getString("UserRestrictionsNoSendStickers", NUM));
        }
        z = tL_chatBannedRights.send_polls;
        if (z && this.defaultBannedRights.send_polls != z) {
            if (stringBuilder.length() != 0) {
                stringBuilder.append(str);
            }
            stringBuilder.append(LocaleController.getString("UserRestrictionsNoSendPolls", NUM));
        }
        z = tL_chatBannedRights.embed_links;
        if (z && this.defaultBannedRights.embed_links != z) {
            if (stringBuilder.length() != 0) {
                stringBuilder.append(str);
            }
            stringBuilder.append(LocaleController.getString("UserRestrictionsNoEmbedLinks", NUM));
        }
        z = tL_chatBannedRights.invite_users;
        if (z && this.defaultBannedRights.invite_users != z) {
            if (stringBuilder.length() != 0) {
                stringBuilder.append(str);
            }
            stringBuilder.append(LocaleController.getString("UserRestrictionsNoInviteUsers", NUM));
        }
        z = tL_chatBannedRights.pin_messages;
        if (z && this.defaultBannedRights.pin_messages != z) {
            if (stringBuilder.length() != 0) {
                stringBuilder.append(str);
            }
            stringBuilder.append(LocaleController.getString("UserRestrictionsNoPinMessages", NUM));
        }
        boolean z2 = tL_chatBannedRights.change_info;
        if (z2 && this.defaultBannedRights.change_info != z2) {
            if (stringBuilder.length() != 0) {
                stringBuilder.append(str);
            }
            stringBuilder.append(LocaleController.getString("UserRestrictionsNoChangeInfo", NUM));
        }
        if (stringBuilder.length() != 0) {
            stringBuilder.replace(0, 1, stringBuilder.substring(0, 1).toUpperCase());
            stringBuilder.append('.');
        }
        return stringBuilder.toString();
    }

    private void processDone() {
        if (this.type == 3) {
            if (!ChatObject.getBannedRightsString(this.defaultBannedRights).equals(this.initialBannedRights)) {
                MessagesController.getInstance(this.currentAccount).setDefaultBannedRole(this.chatId, this.defaultBannedRights, ChatObject.isChannel(this.currentChat), this);
                Chat chat = MessagesController.getInstance(this.currentAccount).getChat(Integer.valueOf(this.chatId));
                if (chat != null) {
                    chat.default_banned_rights = this.defaultBannedRights;
                }
            }
            finishFragment();
        }
    }

    public void setInfo(ChatFull chatFull) {
        this.info = chatFull;
    }

    private int getChannelAdminParticipantType(TLObject tLObject) {
        if ((tLObject instanceof TL_channelParticipantCreator) || (tLObject instanceof TL_channelParticipantSelf)) {
            return 0;
        }
        return ((tLObject instanceof TL_channelParticipantAdmin) || (tLObject instanceof TL_channelParticipant)) ? 1 : 2;
    }

    private void loadChatParticipants(int i, int i2) {
        if (!this.loadingUsers) {
            this.contactsEndReached = false;
            this.botsEndReached = false;
            loadChatParticipants(i, i2, true);
        }
    }

    private void loadChatParticipants(int i, int i2, boolean z) {
        int i3 = 0;
        if (ChatObject.isChannel(this.currentChat)) {
            this.loadingUsers = true;
            EmptyTextProgressView emptyTextProgressView = this.emptyView;
            if (!(emptyTextProgressView == null || this.firstLoaded)) {
                emptyTextProgressView.showProgress();
            }
            ListAdapter listAdapter = this.listViewAdapter;
            if (listAdapter != null) {
                listAdapter.notifyDataSetChanged();
            }
            TL_channels_getParticipants tL_channels_getParticipants = new TL_channels_getParticipants();
            tL_channels_getParticipants.channel = MessagesController.getInstance(this.currentAccount).getInputChannel(this.chatId);
            int i4 = this.type;
            if (i4 == 0) {
                tL_channels_getParticipants.filter = new TL_channelParticipantsKicked();
            } else if (i4 == 1) {
                tL_channels_getParticipants.filter = new TL_channelParticipantsAdmins();
            } else if (i4 == 2) {
                ChatFull chatFull = this.info;
                if (chatFull != null && chatFull.participants_count <= 200) {
                    Chat chat = this.currentChat;
                    if (chat != null && chat.megagroup) {
                        tL_channels_getParticipants.filter = new TL_channelParticipantsRecent();
                    }
                }
                if (this.selectType == 1) {
                    if (this.contactsEndReached) {
                        tL_channels_getParticipants.filter = new TL_channelParticipantsRecent();
                    } else {
                        this.delayResults = 2;
                        tL_channels_getParticipants.filter = new TL_channelParticipantsContacts();
                        this.contactsEndReached = true;
                        loadChatParticipants(0, 200, false);
                    }
                } else if (!this.contactsEndReached) {
                    this.delayResults = 3;
                    tL_channels_getParticipants.filter = new TL_channelParticipantsContacts();
                    this.contactsEndReached = true;
                    loadChatParticipants(0, 200, false);
                } else if (this.botsEndReached) {
                    tL_channels_getParticipants.filter = new TL_channelParticipantsRecent();
                } else {
                    tL_channels_getParticipants.filter = new TL_channelParticipantsBots();
                    this.botsEndReached = true;
                    loadChatParticipants(0, 200, false);
                }
            } else if (i4 == 3) {
                tL_channels_getParticipants.filter = new TL_channelParticipantsBanned();
            }
            tL_channels_getParticipants.filter.q = "";
            tL_channels_getParticipants.offset = i;
            tL_channels_getParticipants.limit = i2;
            ConnectionsManager.getInstance(this.currentAccount).bindRequestToGuid(ConnectionsManager.getInstance(this.currentAccount).sendRequest(tL_channels_getParticipants, new -$$Lambda$ChatUsersActivity$Y--8f3xmJeRa38Ol4oqrzxeP_cE(this, tL_channels_getParticipants)), this.classGuid);
            return;
        }
        this.loadingUsers = false;
        this.participants.clear();
        this.bots.clear();
        this.contacts.clear();
        this.participantsMap.clear();
        this.contactsMap.clear();
        this.botsMap.clear();
        i = this.type;
        if (i == 1) {
            ChatFull chatFull2 = this.info;
            if (chatFull2 != null) {
                i = chatFull2.participants.participants.size();
                while (i3 < i) {
                    ChatParticipant chatParticipant = (ChatParticipant) this.info.participants.participants.get(i3);
                    if ((chatParticipant instanceof TL_chatParticipantCreator) || (chatParticipant instanceof TL_chatParticipantAdmin)) {
                        this.participants.add(chatParticipant);
                    }
                    this.participantsMap.put(chatParticipant.user_id, chatParticipant);
                    i3++;
                }
            }
        } else if (i == 2 && this.info != null) {
            i = UserConfig.getInstance(this.currentAccount).clientUserId;
            i2 = this.info.participants.participants.size();
            while (i3 < i2) {
                ChatParticipant chatParticipant2 = (ChatParticipant) this.info.participants.participants.get(i3);
                if (this.selectType == 0 || chatParticipant2.user_id != i) {
                    if (this.selectType == 1) {
                        if (ContactsController.getInstance(this.currentAccount).isContact(chatParticipant2.user_id)) {
                            this.contacts.add(chatParticipant2);
                            this.contactsMap.put(chatParticipant2.user_id, chatParticipant2);
                        } else {
                            this.participants.add(chatParticipant2);
                            this.participantsMap.put(chatParticipant2.user_id, chatParticipant2);
                        }
                    } else if (ContactsController.getInstance(this.currentAccount).isContact(chatParticipant2.user_id)) {
                        this.contacts.add(chatParticipant2);
                        this.contactsMap.put(chatParticipant2.user_id, chatParticipant2);
                    } else {
                        User user = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(chatParticipant2.user_id));
                        if (user == null || !user.bot) {
                            this.participants.add(chatParticipant2);
                            this.participantsMap.put(chatParticipant2.user_id, chatParticipant2);
                        } else {
                            this.bots.add(chatParticipant2);
                            this.botsMap.put(chatParticipant2.user_id, chatParticipant2);
                        }
                    }
                }
                i3++;
            }
        }
        ListAdapter listAdapter2 = this.listViewAdapter;
        if (listAdapter2 != null) {
            listAdapter2.notifyDataSetChanged();
        }
        updateRows();
        listAdapter2 = this.listViewAdapter;
        if (listAdapter2 != null) {
            listAdapter2.notifyDataSetChanged();
        }
    }

    public /* synthetic */ void lambda$loadChatParticipants$22$ChatUsersActivity(TL_channels_getParticipants tL_channels_getParticipants, TLObject tLObject, TL_error tL_error) {
        AndroidUtilities.runOnUIThread(new -$$Lambda$ChatUsersActivity$t_bocq-wtm1w1cWgy4AcRPPWgyQ(this, tL_error, tLObject, tL_channels_getParticipants));
    }

    public /* synthetic */ void lambda$null$21$ChatUsersActivity(TL_error tL_error, TLObject tLObject, TL_channels_getParticipants tL_channels_getParticipants) {
        if (tL_error == null) {
            int i;
            ArrayList arrayList;
            SparseArray sparseArray;
            TL_channels_channelParticipants tL_channels_channelParticipants = (TL_channels_channelParticipants) tLObject;
            MessagesController.getInstance(this.currentAccount).putUsers(tL_channels_channelParticipants.users, false);
            int clientUserId = UserConfig.getInstance(this.currentAccount).getClientUserId();
            if (this.selectType != 0) {
                for (i = 0; i < tL_channels_channelParticipants.participants.size(); i++) {
                    if (((ChannelParticipant) tL_channels_channelParticipants.participants.get(i)).user_id == clientUserId) {
                        tL_channels_channelParticipants.participants.remove(i);
                        break;
                    }
                }
            }
            EmptyTextProgressView emptyTextProgressView;
            if (this.type == 2) {
                this.delayResults--;
                ChannelParticipantsFilter channelParticipantsFilter = tL_channels_getParticipants.filter;
                if (channelParticipantsFilter instanceof TL_channelParticipantsContacts) {
                    arrayList = this.contacts;
                    sparseArray = this.contactsMap;
                } else if (channelParticipantsFilter instanceof TL_channelParticipantsBots) {
                    arrayList = this.bots;
                    sparseArray = this.botsMap;
                } else {
                    arrayList = this.participants;
                    sparseArray = this.participantsMap;
                }
                if (this.delayResults <= 0) {
                    emptyTextProgressView = this.emptyView;
                    if (emptyTextProgressView != null) {
                        emptyTextProgressView.showTextView();
                    }
                }
            } else {
                arrayList = this.participants;
                sparseArray = this.participantsMap;
                sparseArray.clear();
                emptyTextProgressView = this.emptyView;
                if (emptyTextProgressView != null) {
                    emptyTextProgressView.showTextView();
                }
            }
            arrayList.clear();
            arrayList.addAll(tL_channels_channelParticipants.participants);
            i = tL_channels_channelParticipants.participants.size();
            for (int i2 = 0; i2 < i; i2++) {
                ChannelParticipant channelParticipant = (ChannelParticipant) tL_channels_channelParticipants.participants.get(i2);
                sparseArray.put(channelParticipant.user_id, channelParticipant);
            }
            if (this.type == 2) {
                int size = this.participants.size();
                int i3 = 0;
                while (i3 < size) {
                    ChannelParticipant channelParticipant2 = (ChannelParticipant) this.participants.get(i3);
                    if (this.contactsMap.get(channelParticipant2.user_id) != null || this.botsMap.get(channelParticipant2.user_id) != null) {
                        this.participants.remove(i3);
                        this.participantsMap.remove(channelParticipant2.user_id);
                        i3--;
                        size--;
                    }
                    i3++;
                }
            }
            try {
                if ((this.type == 0 || this.type == 3 || this.type == 2) && this.currentChat != null && this.currentChat.megagroup && (this.info instanceof TL_channelFull) && this.info.participants_count <= 200) {
                    Collections.sort(arrayList, new -$$Lambda$ChatUsersActivity$Uoo5TFGenWAJNIdhBR06NIVkP34(this, ConnectionsManager.getInstance(this.currentAccount).getCurrentTime()));
                } else if (this.type == 1) {
                    Collections.sort(this.participants, new -$$Lambda$ChatUsersActivity$oEyzBH5aLCJzw_8L9_0EYkmb1WM(this));
                }
            } catch (Exception e) {
                FileLog.e(e);
            }
        }
        if (this.type != 2 || this.delayResults <= 0) {
            this.loadingUsers = false;
            this.firstLoaded = true;
        }
        updateRows();
        ListAdapter listAdapter = this.listViewAdapter;
        if (listAdapter != null) {
            listAdapter.notifyDataSetChanged();
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:10:0x003b  */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x004d A:{SKIP} */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x0058 A:{SKIP} */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x0063 A:{SKIP} */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x006c A:{SKIP} */
    public /* synthetic */ int lambda$null$19$ChatUsersActivity(int r4, org.telegram.tgnet.TLObject r5, org.telegram.tgnet.TLObject r6) {
        /*
        r3 = this;
        r5 = (org.telegram.tgnet.TLRPC.ChannelParticipant) r5;
        r6 = (org.telegram.tgnet.TLRPC.ChannelParticipant) r6;
        r0 = r3.currentAccount;
        r0 = org.telegram.messenger.MessagesController.getInstance(r0);
        r5 = r5.user_id;
        r5 = java.lang.Integer.valueOf(r5);
        r5 = r0.getUser(r5);
        r0 = r3.currentAccount;
        r0 = org.telegram.messenger.MessagesController.getInstance(r0);
        r6 = r6.user_id;
        r6 = java.lang.Integer.valueOf(r6);
        r6 = r0.getUser(r6);
        r0 = 50000; // 0xCLASSNAME float:7.0065E-41 double:2.47033E-319;
        r1 = 0;
        if (r5 == 0) goto L_0x0038;
    L_0x002a:
        r2 = r5.status;
        if (r2 == 0) goto L_0x0038;
    L_0x002e:
        r5 = r5.self;
        if (r5 == 0) goto L_0x0035;
    L_0x0032:
        r5 = r4 + r0;
        goto L_0x0039;
    L_0x0035:
        r5 = r2.expires;
        goto L_0x0039;
    L_0x0038:
        r5 = 0;
    L_0x0039:
        if (r6 == 0) goto L_0x0048;
    L_0x003b:
        r2 = r6.status;
        if (r2 == 0) goto L_0x0048;
    L_0x003f:
        r6 = r6.self;
        if (r6 == 0) goto L_0x0045;
    L_0x0043:
        r4 = r4 + r0;
        goto L_0x0049;
    L_0x0045:
        r4 = r2.expires;
        goto L_0x0049;
    L_0x0048:
        r4 = 0;
    L_0x0049:
        r6 = -1;
        r0 = 1;
        if (r5 <= 0) goto L_0x0056;
    L_0x004d:
        if (r4 <= 0) goto L_0x0056;
    L_0x004f:
        if (r5 <= r4) goto L_0x0052;
    L_0x0051:
        return r0;
    L_0x0052:
        if (r5 >= r4) goto L_0x0055;
    L_0x0054:
        return r6;
    L_0x0055:
        return r1;
    L_0x0056:
        if (r5 >= 0) goto L_0x0061;
    L_0x0058:
        if (r4 >= 0) goto L_0x0061;
    L_0x005a:
        if (r5 <= r4) goto L_0x005d;
    L_0x005c:
        return r0;
    L_0x005d:
        if (r5 >= r4) goto L_0x0060;
    L_0x005f:
        return r6;
    L_0x0060:
        return r1;
    L_0x0061:
        if (r5 >= 0) goto L_0x0065;
    L_0x0063:
        if (r4 > 0) goto L_0x0069;
    L_0x0065:
        if (r5 != 0) goto L_0x006a;
    L_0x0067:
        if (r4 == 0) goto L_0x006a;
    L_0x0069:
        return r6;
    L_0x006a:
        if (r4 >= 0) goto L_0x006e;
    L_0x006c:
        if (r5 > 0) goto L_0x0072;
    L_0x006e:
        if (r4 != 0) goto L_0x0073;
    L_0x0070:
        if (r5 == 0) goto L_0x0073;
    L_0x0072:
        return r0;
    L_0x0073:
        return r1;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.ChatUsersActivity.lambda$null$19$ChatUsersActivity(int, org.telegram.tgnet.TLObject, org.telegram.tgnet.TLObject):int");
    }

    public /* synthetic */ int lambda$null$20$ChatUsersActivity(TLObject tLObject, TLObject tLObject2) {
        int channelAdminParticipantType = getChannelAdminParticipantType(tLObject);
        int channelAdminParticipantType2 = getChannelAdminParticipantType(tLObject2);
        if (channelAdminParticipantType > channelAdminParticipantType2) {
            return 1;
        }
        return channelAdminParticipantType < channelAdminParticipantType2 ? -1 : 0;
    }

    public void onResume() {
        super.onResume();
        AndroidUtilities.requestAdjustResize(getParentActivity(), this.classGuid);
        AndroidUtilities.removeAdjustResize(getParentActivity(), this.classGuid);
        ListAdapter listAdapter = this.listViewAdapter;
        if (listAdapter != null) {
            listAdapter.notifyDataSetChanged();
        }
    }

    /* Access modifiers changed, original: protected */
    public void onTransitionAnimationEnd(boolean z, boolean z2) {
        if (z && !z2 && this.needOpenSearch) {
            this.searchItem.openSearch(true);
        }
    }

    public ThemeDescription[] getThemeDescriptions() {
        -$$Lambda$ChatUsersActivity$k3QtTVQ7OOhhTPXiWjlZygn9NL4 -__lambda_chatusersactivity_k3qttvq7oohhtpxiwjlzygn9nl4 = new -$$Lambda$ChatUsersActivity$k3QtTVQ7OOhhTPXiWjlZygn9NL4(this);
        ThemeDescription[] themeDescriptionArr = new ThemeDescription[36];
        themeDescriptionArr[0] = new ThemeDescription(this.listView, ThemeDescription.FLAG_CELLBACKGROUNDCOLOR, new Class[]{ManageChatUserCell.class, ManageChatTextCell.class, TextCheckCell2.class, TextSettingsCell.class}, null, null, null, "windowBackgroundWhite");
        themeDescriptionArr[1] = new ThemeDescription(this.fragmentView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, "windowBackgroundGray");
        themeDescriptionArr[2] = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, "actionBarDefault");
        themeDescriptionArr[3] = new ThemeDescription(this.listView, ThemeDescription.FLAG_LISTGLOWCOLOR, null, null, null, null, "actionBarDefault");
        themeDescriptionArr[4] = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_ITEMSCOLOR, null, null, null, null, "actionBarDefaultIcon");
        themeDescriptionArr[5] = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_TITLECOLOR, null, null, null, null, "actionBarDefaultTitle");
        themeDescriptionArr[6] = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_SELECTORCOLOR, null, null, null, null, "actionBarDefaultSelector");
        themeDescriptionArr[7] = new ThemeDescription(this.listView, ThemeDescription.FLAG_SELECTOR, null, null, null, null, "listSelectorSDK21");
        themeDescriptionArr[8] = new ThemeDescription(this.listView, 0, new Class[]{View.class}, Theme.dividerPaint, null, null, "divider");
        View view = this.listView;
        View view2 = view;
        themeDescriptionArr[9] = new ThemeDescription(view2, ThemeDescription.FLAG_BACKGROUNDFILTER, new Class[]{TextInfoPrivacyCell.class}, null, null, null, "windowBackgroundGrayShadow");
        view = this.listView;
        Class[] clsArr = new Class[]{TextInfoPrivacyCell.class};
        String[] strArr = new String[1];
        strArr[0] = "textView";
        themeDescriptionArr[10] = new ThemeDescription(view, 0, clsArr, strArr, null, null, null, "windowBackgroundWhiteGrayText4");
        view = this.listView;
        view2 = view;
        themeDescriptionArr[11] = new ThemeDescription(view2, ThemeDescription.FLAG_BACKGROUNDFILTER, new Class[]{ShadowSectionCell.class}, null, null, null, "windowBackgroundGrayShadow");
        themeDescriptionArr[12] = new ThemeDescription(this.listView, 0, new Class[]{HeaderCell.class}, new String[]{"textView"}, null, null, null, "windowBackgroundWhiteBlueHeader");
        themeDescriptionArr[13] = new ThemeDescription(this.listView, 0, new Class[]{GraySectionCell.class}, new String[]{"textView"}, null, null, null, "key_graySectionText");
        view = this.listView;
        view2 = view;
        themeDescriptionArr[14] = new ThemeDescription(view2, ThemeDescription.FLAG_CELLBACKGROUNDCOLOR, new Class[]{GraySectionCell.class}, null, null, null, "graySection");
        themeDescriptionArr[15] = new ThemeDescription(this.listView, 0, new Class[]{TextSettingsCell.class}, new String[]{"textView"}, null, null, null, "windowBackgroundWhiteBlackText");
        view = this.listView;
        clsArr = new Class[]{TextSettingsCell.class};
        strArr = new String[1];
        strArr[0] = "valueTextView";
        themeDescriptionArr[16] = new ThemeDescription(view, 0, clsArr, strArr, null, null, null, "windowBackgroundWhiteValueText");
        themeDescriptionArr[17] = new ThemeDescription(this.listView, 0, new Class[]{TextCheckCell2.class}, new String[]{"textView"}, null, null, null, "windowBackgroundWhiteBlackText");
        themeDescriptionArr[18] = new ThemeDescription(this.listView, 0, new Class[]{TextCheckCell2.class}, new String[]{"valueTextView"}, null, null, null, "windowBackgroundWhiteGrayText2");
        view = this.listView;
        clsArr = new Class[]{TextCheckCell2.class};
        strArr = new String[1];
        strArr[0] = "checkBox";
        themeDescriptionArr[19] = new ThemeDescription(view, 0, clsArr, strArr, null, null, null, "switch2Track");
        themeDescriptionArr[20] = new ThemeDescription(this.listView, 0, new Class[]{TextCheckCell2.class}, new String[]{"checkBox"}, null, null, null, "switch2TrackChecked");
        themeDescriptionArr[21] = new ThemeDescription(this.listView, 0, new Class[]{ManageChatUserCell.class}, new String[]{"nameTextView"}, null, null, null, "windowBackgroundWhiteBlackText");
        ThemeDescriptionDelegate themeDescriptionDelegate = -__lambda_chatusersactivity_k3qttvq7oohhtpxiwjlzygn9nl4;
        themeDescriptionArr[22] = new ThemeDescription(this.listView, 0, new Class[]{ManageChatUserCell.class}, new String[]{"statusColor"}, null, null, themeDescriptionDelegate, "windowBackgroundWhiteGrayText");
        themeDescriptionArr[23] = new ThemeDescription(this.listView, 0, new Class[]{ManageChatUserCell.class}, new String[]{"statusOnlineColor"}, null, null, themeDescriptionDelegate, "windowBackgroundWhiteBlueText");
        themeDescriptionArr[24] = new ThemeDescription(this.listView, 0, new Class[]{ManageChatUserCell.class}, null, new Drawable[]{Theme.avatar_broadcastDrawable, Theme.avatar_savedDrawable}, null, "avatar_text");
        -$$Lambda$ChatUsersActivity$k3QtTVQ7OOhhTPXiWjlZygn9NL4 -__lambda_chatusersactivity_k3qttvq7oohhtpxiwjlzygn9nl42 = -__lambda_chatusersactivity_k3qttvq7oohhtpxiwjlzygn9nl4;
        themeDescriptionArr[25] = new ThemeDescription(null, 0, null, null, null, -__lambda_chatusersactivity_k3qttvq7oohhtpxiwjlzygn9nl42, "avatar_backgroundRed");
        themeDescriptionArr[26] = new ThemeDescription(null, 0, null, null, null, -__lambda_chatusersactivity_k3qttvq7oohhtpxiwjlzygn9nl42, "avatar_backgroundOrange");
        themeDescriptionArr[27] = new ThemeDescription(null, 0, null, null, null, -__lambda_chatusersactivity_k3qttvq7oohhtpxiwjlzygn9nl42, "avatar_backgroundViolet");
        themeDescriptionArr[28] = new ThemeDescription(null, 0, null, null, null, -__lambda_chatusersactivity_k3qttvq7oohhtpxiwjlzygn9nl42, "avatar_backgroundGreen");
        themeDescriptionArr[29] = new ThemeDescription(null, 0, null, null, null, -__lambda_chatusersactivity_k3qttvq7oohhtpxiwjlzygn9nl42, "avatar_backgroundCyan");
        themeDescriptionArr[30] = new ThemeDescription(null, 0, null, null, null, -__lambda_chatusersactivity_k3qttvq7oohhtpxiwjlzygn9nl42, "avatar_backgroundBlue");
        themeDescriptionArr[31] = new ThemeDescription(null, 0, null, null, null, -__lambda_chatusersactivity_k3qttvq7oohhtpxiwjlzygn9nl42, "avatar_backgroundPink");
        view = this.listView;
        View view3 = view;
        themeDescriptionArr[32] = new ThemeDescription(view3, ThemeDescription.FLAG_CHECKTAG, new Class[]{ManageChatTextCell.class}, new String[]{"textView"}, null, null, null, "windowBackgroundWhiteBlackText");
        view = this.listView;
        int i = ThemeDescription.FLAG_CHECKTAG;
        clsArr = new Class[]{ManageChatTextCell.class};
        strArr = new String[1];
        strArr[0] = "imageView";
        themeDescriptionArr[33] = new ThemeDescription(view, i, clsArr, strArr, null, null, null, "windowBackgroundWhiteGrayIcon");
        view = this.listView;
        view3 = view;
        themeDescriptionArr[34] = new ThemeDescription(view3, ThemeDescription.FLAG_CHECKTAG, new Class[]{ManageChatTextCell.class}, new String[]{"imageView"}, null, null, null, "windowBackgroundWhiteBlueButton");
        view = this.listView;
        view3 = view;
        themeDescriptionArr[35] = new ThemeDescription(view3, ThemeDescription.FLAG_CHECKTAG, new Class[]{ManageChatTextCell.class}, new String[]{"textView"}, null, null, null, "windowBackgroundWhiteBlueIcon");
        return themeDescriptionArr;
    }

    public /* synthetic */ void lambda$getThemeDescriptions$23$ChatUsersActivity() {
        RecyclerListView recyclerListView = this.listView;
        if (recyclerListView != null) {
            int childCount = recyclerListView.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View childAt = this.listView.getChildAt(i);
                if (childAt instanceof ManageChatUserCell) {
                    ((ManageChatUserCell) childAt).update(0);
                }
            }
        }
    }
}
