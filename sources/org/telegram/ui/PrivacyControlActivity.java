package org.telegram.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import java.util.ArrayList;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.NotificationCenter.NotificationCenterDelegate;
import org.telegram.messenger.beta.R;
import org.telegram.messenger.support.widget.LinearLayoutManager;
import org.telegram.messenger.support.widget.RecyclerView.ViewHolder;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.InputUser;
import org.telegram.tgnet.TLRPC.PrivacyRule;
import org.telegram.tgnet.TLRPC.TL_account_privacyRules;
import org.telegram.tgnet.TLRPC.TL_account_setPrivacy;
import org.telegram.tgnet.TLRPC.TL_error;
import org.telegram.tgnet.TLRPC.TL_inputPrivacyKeyChatInvite;
import org.telegram.tgnet.TLRPC.TL_inputPrivacyKeyPhoneCall;
import org.telegram.tgnet.TLRPC.TL_inputPrivacyKeyStatusTimestamp;
import org.telegram.tgnet.TLRPC.TL_inputPrivacyValueAllowAll;
import org.telegram.tgnet.TLRPC.TL_inputPrivacyValueAllowContacts;
import org.telegram.tgnet.TLRPC.TL_inputPrivacyValueAllowUsers;
import org.telegram.tgnet.TLRPC.TL_inputPrivacyValueDisallowAll;
import org.telegram.tgnet.TLRPC.TL_inputPrivacyValueDisallowUsers;
import org.telegram.tgnet.TLRPC.TL_privacyValueAllowAll;
import org.telegram.tgnet.TLRPC.TL_privacyValueAllowUsers;
import org.telegram.tgnet.TLRPC.TL_privacyValueDisallowAll;
import org.telegram.tgnet.TLRPC.TL_privacyValueDisallowUsers;
import org.telegram.tgnet.TLRPC.User;
import org.telegram.ui.ActionBar.ActionBar.ActionBarMenuOnItemClick;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.AlertDialog.Builder;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.RadioCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.RecyclerListView.Holder;
import org.telegram.ui.Components.RecyclerListView.OnItemClickListener;
import org.telegram.ui.Components.RecyclerListView.SelectionAdapter;
import org.telegram.ui.GroupCreateActivity.GroupCreateActivityDelegate;
import org.telegram.ui.PrivacyUsersActivity.PrivacyActivityDelegate;

public class PrivacyControlActivity extends BaseFragment implements NotificationCenterDelegate {
    private static final int done_button = 1;
    private int alwaysShareRow;
    private ArrayList<Integer> currentMinus;
    private ArrayList<Integer> currentPlus;
    private int currentType;
    private int detailRow;
    private View doneButton;
    private boolean enableAnimation;
    private int everybodyRow;
    private int lastCheckedType = -1;
    private ListAdapter listAdapter;
    private RecyclerListView listView;
    private int myContactsRow;
    private int neverShareRow;
    private int nobodyRow;
    private int rowCount;
    private int rulesType;
    private int sectionRow;
    private int shareDetailRow;
    private int shareSectionRow;

    private static class LinkMovementMethodMy extends LinkMovementMethod {
        private LinkMovementMethodMy() {
        }

        public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {
            try {
                return super.onTouchEvent(widget, buffer, event);
            } catch (Throwable e) {
                FileLog.m3e(e);
                return false;
            }
        }
    }

    /* renamed from: org.telegram.ui.PrivacyControlActivity$1 */
    class C22351 extends ActionBarMenuOnItemClick {
        C22351() {
        }

        public void onItemClick(int id) {
            if (id == -1) {
                PrivacyControlActivity.this.finishFragment();
            } else if (id == 1 && PrivacyControlActivity.this.getParentActivity() != null) {
                if (PrivacyControlActivity.this.currentType != 0 && PrivacyControlActivity.this.rulesType == 0) {
                    final SharedPreferences preferences = MessagesController.getGlobalMainSettings();
                    if (!preferences.getBoolean("privacyAlertShowed", false)) {
                        Builder builder = new Builder(PrivacyControlActivity.this.getParentActivity());
                        if (PrivacyControlActivity.this.rulesType == 1) {
                            builder.setMessage(LocaleController.getString("WhoCanAddMeInfo", R.string.WhoCanAddMeInfo));
                        } else {
                            builder.setMessage(LocaleController.getString("CustomHelp", R.string.CustomHelp));
                        }
                        builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
                        builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), new OnClickListener() {
                            public void onClick(DialogInterface dialogInterface, int i) {
                                PrivacyControlActivity.this.applyCurrentPrivacySettings();
                                preferences.edit().putBoolean("privacyAlertShowed", true).commit();
                            }
                        });
                        builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                        PrivacyControlActivity.this.showDialog(builder.create());
                        return;
                    }
                }
                PrivacyControlActivity.this.applyCurrentPrivacySettings();
            }
        }
    }

    /* renamed from: org.telegram.ui.PrivacyControlActivity$2 */
    class C22382 implements OnItemClickListener {
        C22382() {
        }

        public void onItemClick(View view, final int position) {
            boolean z = false;
            if (!(position == PrivacyControlActivity.this.nobodyRow || position == PrivacyControlActivity.this.everybodyRow)) {
                if (position != PrivacyControlActivity.this.myContactsRow) {
                    if (position == PrivacyControlActivity.this.neverShareRow || position == PrivacyControlActivity.this.alwaysShareRow) {
                        ArrayList<Integer> createFromArray;
                        if (position == PrivacyControlActivity.this.neverShareRow) {
                            createFromArray = PrivacyControlActivity.this.currentMinus;
                        } else {
                            createFromArray = PrivacyControlActivity.this.currentPlus;
                        }
                        if (createFromArray.isEmpty()) {
                            Bundle args = new Bundle();
                            args.putBoolean(position == PrivacyControlActivity.this.neverShareRow ? "isNeverShare" : "isAlwaysShare", true);
                            String str = "isGroup";
                            if (PrivacyControlActivity.this.rulesType != 0) {
                                z = true;
                            }
                            args.putBoolean(str, z);
                            GroupCreateActivity fragment = new GroupCreateActivity(args);
                            fragment.setDelegate(new GroupCreateActivityDelegate() {
                                public void didSelectUsers(ArrayList<Integer> ids) {
                                    int a;
                                    if (position == PrivacyControlActivity.this.neverShareRow) {
                                        PrivacyControlActivity.this.currentMinus = ids;
                                        for (a = 0; a < PrivacyControlActivity.this.currentMinus.size(); a++) {
                                            PrivacyControlActivity.this.currentPlus.remove(PrivacyControlActivity.this.currentMinus.get(a));
                                        }
                                    } else {
                                        PrivacyControlActivity.this.currentPlus = ids;
                                        for (a = 0; a < PrivacyControlActivity.this.currentPlus.size(); a++) {
                                            PrivacyControlActivity.this.currentMinus.remove(PrivacyControlActivity.this.currentPlus.get(a));
                                        }
                                    }
                                    PrivacyControlActivity.this.doneButton.setVisibility(0);
                                    PrivacyControlActivity.this.lastCheckedType = -1;
                                    PrivacyControlActivity.this.listAdapter.notifyDataSetChanged();
                                }
                            });
                            PrivacyControlActivity.this.presentFragment(fragment);
                        } else {
                            boolean z2 = PrivacyControlActivity.this.rulesType != 0;
                            if (position == PrivacyControlActivity.this.alwaysShareRow) {
                                z = true;
                            }
                            PrivacyUsersActivity fragment2 = new PrivacyUsersActivity(createFromArray, z2, z);
                            fragment2.setDelegate(new PrivacyActivityDelegate() {
                                public void didUpdatedUserList(ArrayList<Integer> ids, boolean added) {
                                    int a;
                                    if (position == PrivacyControlActivity.this.neverShareRow) {
                                        PrivacyControlActivity.this.currentMinus = ids;
                                        if (added) {
                                            for (a = 0; a < PrivacyControlActivity.this.currentMinus.size(); a++) {
                                                PrivacyControlActivity.this.currentPlus.remove(PrivacyControlActivity.this.currentMinus.get(a));
                                            }
                                        }
                                    } else {
                                        PrivacyControlActivity.this.currentPlus = ids;
                                        if (added) {
                                            for (a = 0; a < PrivacyControlActivity.this.currentPlus.size(); a++) {
                                                PrivacyControlActivity.this.currentMinus.remove(PrivacyControlActivity.this.currentPlus.get(a));
                                            }
                                        }
                                    }
                                    PrivacyControlActivity.this.doneButton.setVisibility(0);
                                    PrivacyControlActivity.this.listAdapter.notifyDataSetChanged();
                                }
                            });
                            PrivacyControlActivity.this.presentFragment(fragment2);
                        }
                    }
                }
            }
            int newType = PrivacyControlActivity.this.currentType;
            if (position == PrivacyControlActivity.this.nobodyRow) {
                newType = 1;
            } else if (position == PrivacyControlActivity.this.everybodyRow) {
                newType = 0;
            } else if (position == PrivacyControlActivity.this.myContactsRow) {
                newType = 2;
            }
            if (newType != PrivacyControlActivity.this.currentType) {
                PrivacyControlActivity.this.enableAnimation = true;
                PrivacyControlActivity.this.doneButton.setVisibility(0);
                PrivacyControlActivity.this.lastCheckedType = PrivacyControlActivity.this.currentType;
                PrivacyControlActivity.this.currentType = newType;
                PrivacyControlActivity.this.updateRows();
            }
        }
    }

    private class ListAdapter extends SelectionAdapter {
        private Context mContext;

        public ListAdapter(Context context) {
            this.mContext = context;
        }

        public boolean isEnabled(ViewHolder holder) {
            int position = holder.getAdapterPosition();
            if (!(position == PrivacyControlActivity.this.nobodyRow || position == PrivacyControlActivity.this.everybodyRow || position == PrivacyControlActivity.this.myContactsRow || position == PrivacyControlActivity.this.neverShareRow)) {
                if (position != PrivacyControlActivity.this.alwaysShareRow) {
                    return false;
                }
            }
            return true;
        }

        public int getItemCount() {
            return PrivacyControlActivity.this.rowCount;
        }

        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view;
            switch (viewType) {
                case 0:
                    view = new TextSettingsCell(this.mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case 1:
                    view = new TextInfoPrivacyCell(this.mContext);
                    break;
                case 2:
                    view = new HeaderCell(this.mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                default:
                    view = new RadioCell(this.mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
            }
            return new Holder(view);
        }

        public void onBindViewHolder(ViewHolder holder, int position) {
            boolean z = false;
            switch (holder.getItemViewType()) {
                case 0:
                    TextSettingsCell textCell = holder.itemView;
                    if (position == PrivacyControlActivity.this.alwaysShareRow) {
                        String value;
                        if (PrivacyControlActivity.this.currentPlus.size() != 0) {
                            value = LocaleController.formatPluralString("Users", PrivacyControlActivity.this.currentPlus.size());
                        } else {
                            value = LocaleController.getString("EmpryUsersPlaceholder", R.string.EmpryUsersPlaceholder);
                        }
                        String string;
                        if (PrivacyControlActivity.this.rulesType != 0) {
                            string = LocaleController.getString("AlwaysAllow", R.string.AlwaysAllow);
                            if (PrivacyControlActivity.this.neverShareRow != -1) {
                                z = true;
                            }
                            textCell.setTextAndValue(string, value, z);
                        } else {
                            string = LocaleController.getString("AlwaysShareWith", R.string.AlwaysShareWith);
                            if (PrivacyControlActivity.this.neverShareRow != -1) {
                                z = true;
                            }
                            textCell.setTextAndValue(string, value, z);
                        }
                        return;
                    } else if (position == PrivacyControlActivity.this.neverShareRow) {
                        String value2;
                        if (PrivacyControlActivity.this.currentMinus.size() != 0) {
                            value2 = LocaleController.formatPluralString("Users", PrivacyControlActivity.this.currentMinus.size());
                        } else {
                            value2 = LocaleController.getString("EmpryUsersPlaceholder", R.string.EmpryUsersPlaceholder);
                        }
                        if (PrivacyControlActivity.this.rulesType != 0) {
                            textCell.setTextAndValue(LocaleController.getString("NeverAllow", R.string.NeverAllow), value2, false);
                        } else {
                            textCell.setTextAndValue(LocaleController.getString("NeverShareWith", R.string.NeverShareWith), value2, false);
                        }
                        return;
                    } else {
                        return;
                    }
                case 1:
                    TextInfoPrivacyCell privacyCell = holder.itemView;
                    if (position == PrivacyControlActivity.this.detailRow) {
                        if (PrivacyControlActivity.this.rulesType == 2) {
                            privacyCell.setText(LocaleController.getString("WhoCanCallMeInfo", R.string.WhoCanCallMeInfo));
                        } else if (PrivacyControlActivity.this.rulesType == 1) {
                            privacyCell.setText(LocaleController.getString("WhoCanAddMeInfo", R.string.WhoCanAddMeInfo));
                        } else {
                            privacyCell.setText(LocaleController.getString("CustomHelp", R.string.CustomHelp));
                        }
                        privacyCell.setBackgroundDrawable(Theme.getThemedDrawable(this.mContext, R.drawable.greydivider, Theme.key_windowBackgroundGrayShadow));
                        return;
                    } else if (position == PrivacyControlActivity.this.shareDetailRow) {
                        if (PrivacyControlActivity.this.rulesType == 2) {
                            privacyCell.setText(LocaleController.getString("CustomCallInfo", R.string.CustomCallInfo));
                        } else if (PrivacyControlActivity.this.rulesType == 1) {
                            privacyCell.setText(LocaleController.getString("CustomShareInfo", R.string.CustomShareInfo));
                        } else {
                            privacyCell.setText(LocaleController.getString("CustomShareSettingsHelp", R.string.CustomShareSettingsHelp));
                        }
                        privacyCell.setBackgroundDrawable(Theme.getThemedDrawable(this.mContext, R.drawable.greydivider_bottom, Theme.key_windowBackgroundGrayShadow));
                        return;
                    } else {
                        return;
                    }
                case 2:
                    HeaderCell headerCell = holder.itemView;
                    if (position == PrivacyControlActivity.this.sectionRow) {
                        if (PrivacyControlActivity.this.rulesType == 2) {
                            headerCell.setText(LocaleController.getString("WhoCanCallMe", R.string.WhoCanCallMe));
                            return;
                        } else if (PrivacyControlActivity.this.rulesType == 1) {
                            headerCell.setText(LocaleController.getString("WhoCanAddMe", R.string.WhoCanAddMe));
                            return;
                        } else {
                            headerCell.setText(LocaleController.getString("LastSeenTitle", R.string.LastSeenTitle));
                            return;
                        }
                    } else if (position == PrivacyControlActivity.this.shareSectionRow) {
                        headerCell.setText(LocaleController.getString("AddExceptions", R.string.AddExceptions));
                        return;
                    } else {
                        return;
                    }
                case 3:
                    RadioCell radioCell = holder.itemView;
                    int checkedType = 0;
                    if (position == PrivacyControlActivity.this.everybodyRow) {
                        radioCell.setText(LocaleController.getString("LastSeenEverybody", R.string.LastSeenEverybody), PrivacyControlActivity.this.lastCheckedType == 0, true);
                        checkedType = 0;
                    } else if (position == PrivacyControlActivity.this.myContactsRow) {
                        radioCell.setText(LocaleController.getString("LastSeenContacts", R.string.LastSeenContacts), PrivacyControlActivity.this.lastCheckedType == 2, PrivacyControlActivity.this.nobodyRow != -1);
                        checkedType = 2;
                    } else if (position == PrivacyControlActivity.this.nobodyRow) {
                        radioCell.setText(LocaleController.getString("LastSeenNobody", R.string.LastSeenNobody), PrivacyControlActivity.this.lastCheckedType == 1, false);
                        checkedType = 1;
                    }
                    if (PrivacyControlActivity.this.lastCheckedType == checkedType) {
                        radioCell.setChecked(false, PrivacyControlActivity.this.enableAnimation);
                        return;
                    } else if (PrivacyControlActivity.this.currentType == checkedType) {
                        radioCell.setChecked(true, PrivacyControlActivity.this.enableAnimation);
                        return;
                    } else {
                        return;
                    }
                default:
                    return;
            }
        }

        public int getItemViewType(int position) {
            if (position != PrivacyControlActivity.this.alwaysShareRow) {
                if (position != PrivacyControlActivity.this.neverShareRow) {
                    if (position != PrivacyControlActivity.this.shareDetailRow) {
                        if (position != PrivacyControlActivity.this.detailRow) {
                            if (position != PrivacyControlActivity.this.sectionRow) {
                                if (position != PrivacyControlActivity.this.shareSectionRow) {
                                    if (!(position == PrivacyControlActivity.this.everybodyRow || position == PrivacyControlActivity.this.myContactsRow)) {
                                        if (position != PrivacyControlActivity.this.nobodyRow) {
                                            return 0;
                                        }
                                    }
                                    return 3;
                                }
                            }
                            return 2;
                        }
                    }
                    return 1;
                }
            }
            return 0;
        }
    }

    public PrivacyControlActivity(int type) {
        this.rulesType = type;
    }

    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        checkPrivacy();
        updateRows();
        NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.privacyRulesUpdated);
        return true;
    }

    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.privacyRulesUpdated);
    }

    public View createView(Context context) {
        this.actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        this.actionBar.setAllowOverlayTitle(true);
        if (this.rulesType == 2) {
            this.actionBar.setTitle(LocaleController.getString("Calls", R.string.Calls));
        } else if (this.rulesType == 1) {
            this.actionBar.setTitle(LocaleController.getString("GroupsAndChannels", R.string.GroupsAndChannels));
        } else {
            this.actionBar.setTitle(LocaleController.getString("PrivacyLastSeen", R.string.PrivacyLastSeen));
        }
        this.actionBar.setActionBarMenuOnItemClick(new C22351());
        int visibility = this.doneButton != null ? this.doneButton.getVisibility() : 8;
        this.doneButton = this.actionBar.createMenu().addItemWithWidth(1, R.drawable.ic_done, AndroidUtilities.dp(56.0f));
        this.doneButton.setVisibility(visibility);
        this.listAdapter = new ListAdapter(context);
        this.fragmentView = new FrameLayout(context);
        FrameLayout frameLayout = this.fragmentView;
        frameLayout.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
        this.listView = new RecyclerListView(context);
        this.listView.setLayoutManager(new LinearLayoutManager(context, 1, false));
        this.listView.setVerticalScrollBarEnabled(false);
        frameLayout.addView(this.listView, LayoutHelper.createFrame(-1, -1.0f));
        this.listView.setAdapter(this.listAdapter);
        this.listView.setOnItemClickListener(new C22382());
        return this.fragmentView;
    }

    public void didReceivedNotification(int id, int account, Object... args) {
        if (id == NotificationCenter.privacyRulesUpdated) {
            checkPrivacy();
        }
    }

    private void applyCurrentPrivacySettings() {
        int a;
        User user;
        InputUser inputUser;
        TL_account_setPrivacy req = new TL_account_setPrivacy();
        if (this.rulesType == 2) {
            req.key = new TL_inputPrivacyKeyPhoneCall();
        } else if (this.rulesType == 1) {
            req.key = new TL_inputPrivacyKeyChatInvite();
        } else {
            req.key = new TL_inputPrivacyKeyStatusTimestamp();
        }
        if (this.currentType != 0 && this.currentPlus.size() > 0) {
            TL_inputPrivacyValueAllowUsers rule = new TL_inputPrivacyValueAllowUsers();
            for (a = 0; a < this.currentPlus.size(); a++) {
                user = MessagesController.getInstance(this.currentAccount).getUser((Integer) this.currentPlus.get(a));
                if (user != null) {
                    inputUser = MessagesController.getInstance(this.currentAccount).getInputUser(user);
                    if (inputUser != null) {
                        rule.users.add(inputUser);
                    }
                }
            }
            req.rules.add(rule);
        }
        if (this.currentType != 1 && this.currentMinus.size() > 0) {
            TL_inputPrivacyValueDisallowUsers rule2 = new TL_inputPrivacyValueDisallowUsers();
            for (a = 0; a < this.currentMinus.size(); a++) {
                user = MessagesController.getInstance(this.currentAccount).getUser((Integer) this.currentMinus.get(a));
                if (user != null) {
                    inputUser = MessagesController.getInstance(this.currentAccount).getInputUser(user);
                    if (inputUser != null) {
                        rule2.users.add(inputUser);
                    }
                }
            }
            req.rules.add(rule2);
        }
        if (this.currentType == 0) {
            req.rules.add(new TL_inputPrivacyValueAllowAll());
        } else if (this.currentType == 1) {
            req.rules.add(new TL_inputPrivacyValueDisallowAll());
        } else if (this.currentType == 2) {
            req.rules.add(new TL_inputPrivacyValueAllowContacts());
        }
        AlertDialog progressDialog = null;
        if (getParentActivity() != null) {
            progressDialog = new AlertDialog(getParentActivity(), 1);
            progressDialog.setMessage(LocaleController.getString("Loading", R.string.Loading));
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }
        final AlertDialog progressDialogFinal = progressDialog;
        ConnectionsManager.getInstance(this.currentAccount).sendRequest(req, new RequestDelegate() {
            public void run(final TLObject response, final TL_error error) {
                AndroidUtilities.runOnUIThread(new Runnable() {
                    public void run() {
                        try {
                            if (progressDialogFinal != null) {
                                progressDialogFinal.dismiss();
                            }
                        } catch (Throwable e) {
                            FileLog.m3e(e);
                        }
                        if (error == null) {
                            PrivacyControlActivity.this.finishFragment();
                            TL_account_privacyRules rules = response;
                            MessagesController.getInstance(PrivacyControlActivity.this.currentAccount).putUsers(rules.users, false);
                            ContactsController.getInstance(PrivacyControlActivity.this.currentAccount).setPrivacyRules(rules.rules, PrivacyControlActivity.this.rulesType);
                            return;
                        }
                        PrivacyControlActivity.this.showErrorAlert();
                    }
                });
            }
        }, 2);
    }

    private void showErrorAlert() {
        if (getParentActivity() != null) {
            Builder builder = new Builder(getParentActivity());
            builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
            builder.setMessage(LocaleController.getString("PrivacyFloodControlError", R.string.PrivacyFloodControlError));
            builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), null);
            showDialog(builder.create());
        }
    }

    private void checkPrivacy() {
        this.currentPlus = new ArrayList();
        this.currentMinus = new ArrayList();
        ArrayList<PrivacyRule> privacyRules = ContactsController.getInstance(this.currentAccount).getPrivacyRules(this.rulesType);
        if (privacyRules != null) {
            if (privacyRules.size() != 0) {
                int type = -1;
                for (int a = 0; a < privacyRules.size(); a++) {
                    PrivacyRule rule = (PrivacyRule) privacyRules.get(a);
                    if (rule instanceof TL_privacyValueAllowUsers) {
                        this.currentPlus.addAll(rule.users);
                    } else if (rule instanceof TL_privacyValueDisallowUsers) {
                        this.currentMinus.addAll(rule.users);
                    } else if (rule instanceof TL_privacyValueAllowAll) {
                        type = 0;
                    } else if (rule instanceof TL_privacyValueDisallowAll) {
                        type = 1;
                    } else {
                        type = 2;
                    }
                }
                if (type != 0) {
                    if (type != -1 || this.currentMinus.size() <= 0) {
                        if (type != 2) {
                            if (type != -1 || this.currentMinus.size() <= 0 || this.currentPlus.size() <= 0) {
                                if (type == 1 || (type == -1 && this.currentPlus.size() > 0)) {
                                    this.currentType = 1;
                                }
                                if (this.doneButton != null) {
                                    this.doneButton.setVisibility(8);
                                }
                                updateRows();
                                return;
                            }
                        }
                        this.currentType = 2;
                        if (this.doneButton != null) {
                            this.doneButton.setVisibility(8);
                        }
                        updateRows();
                        return;
                    }
                }
                this.currentType = 0;
                if (this.doneButton != null) {
                    this.doneButton.setVisibility(8);
                }
                updateRows();
                return;
            }
        }
        this.currentType = 1;
    }

    private void updateRows() {
        this.rowCount = 0;
        int i = this.rowCount;
        this.rowCount = i + 1;
        this.sectionRow = i;
        i = this.rowCount;
        this.rowCount = i + 1;
        this.everybodyRow = i;
        i = this.rowCount;
        this.rowCount = i + 1;
        this.myContactsRow = i;
        if (this.rulesType == 0 || this.rulesType == 2) {
            i = this.rowCount;
            this.rowCount = i + 1;
            this.nobodyRow = i;
        } else {
            this.nobodyRow = -1;
        }
        i = this.rowCount;
        this.rowCount = i + 1;
        this.detailRow = i;
        i = this.rowCount;
        this.rowCount = i + 1;
        this.shareSectionRow = i;
        if (this.currentType != 1) {
            if (this.currentType != 2) {
                this.alwaysShareRow = -1;
                if (this.currentType != 0) {
                    if (this.currentType == 2) {
                        this.neverShareRow = -1;
                        i = this.rowCount;
                        this.rowCount = i + 1;
                        this.shareDetailRow = i;
                        if (this.listAdapter == null) {
                            this.listAdapter.notifyDataSetChanged();
                        }
                    }
                }
                i = this.rowCount;
                this.rowCount = i + 1;
                this.neverShareRow = i;
                i = this.rowCount;
                this.rowCount = i + 1;
                this.shareDetailRow = i;
                if (this.listAdapter == null) {
                    this.listAdapter.notifyDataSetChanged();
                }
            }
        }
        i = this.rowCount;
        this.rowCount = i + 1;
        this.alwaysShareRow = i;
        if (this.currentType != 0) {
            if (this.currentType == 2) {
                this.neverShareRow = -1;
                i = this.rowCount;
                this.rowCount = i + 1;
                this.shareDetailRow = i;
                if (this.listAdapter == null) {
                    this.listAdapter.notifyDataSetChanged();
                }
            }
        }
        i = this.rowCount;
        this.rowCount = i + 1;
        this.neverShareRow = i;
        i = this.rowCount;
        this.rowCount = i + 1;
        this.shareDetailRow = i;
        if (this.listAdapter == null) {
            this.listAdapter.notifyDataSetChanged();
        }
    }

    public void onResume() {
        super.onResume();
        this.lastCheckedType = -1;
        this.enableAnimation = false;
    }

    public ThemeDescription[] getThemeDescriptions() {
        ThemeDescription[] themeDescriptionArr = new ThemeDescription[17];
        themeDescriptionArr[0] = new ThemeDescription(this.listView, ThemeDescription.FLAG_CELLBACKGROUNDCOLOR, new Class[]{TextSettingsCell.class, HeaderCell.class, RadioCell.class}, null, null, null, Theme.key_windowBackgroundWhite);
        themeDescriptionArr[1] = new ThemeDescription(this.fragmentView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_windowBackgroundGray);
        themeDescriptionArr[2] = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_actionBarDefault);
        themeDescriptionArr[3] = new ThemeDescription(this.listView, ThemeDescription.FLAG_LISTGLOWCOLOR, null, null, null, null, Theme.key_actionBarDefault);
        themeDescriptionArr[4] = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_ITEMSCOLOR, null, null, null, null, Theme.key_actionBarDefaultIcon);
        themeDescriptionArr[5] = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_TITLECOLOR, null, null, null, null, Theme.key_actionBarDefaultTitle);
        themeDescriptionArr[6] = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_SELECTORCOLOR, null, null, null, null, Theme.key_actionBarDefaultSelector);
        themeDescriptionArr[7] = new ThemeDescription(this.listView, ThemeDescription.FLAG_SELECTOR, null, null, null, null, Theme.key_listSelector);
        themeDescriptionArr[8] = new ThemeDescription(this.listView, 0, new Class[]{View.class}, Theme.dividerPaint, null, null, Theme.key_divider);
        themeDescriptionArr[9] = new ThemeDescription(this.listView, ThemeDescription.FLAG_BACKGROUNDFILTER, new Class[]{TextInfoPrivacyCell.class}, null, null, null, Theme.key_windowBackgroundGrayShadow);
        themeDescriptionArr[10] = new ThemeDescription(this.listView, 0, new Class[]{TextSettingsCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText);
        themeDescriptionArr[11] = new ThemeDescription(this.listView, 0, new Class[]{TextSettingsCell.class}, new String[]{"valueTextView"}, null, null, null, Theme.key_windowBackgroundWhiteValueText);
        themeDescriptionArr[12] = new ThemeDescription(this.listView, 0, new Class[]{TextInfoPrivacyCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText4);
        themeDescriptionArr[13] = new ThemeDescription(this.listView, 0, new Class[]{HeaderCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlueHeader);
        themeDescriptionArr[14] = new ThemeDescription(this.listView, 0, new Class[]{RadioCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText);
        themeDescriptionArr[15] = new ThemeDescription(this.listView, ThemeDescription.FLAG_CHECKBOX, new Class[]{RadioCell.class}, new String[]{"radioButton"}, null, null, null, Theme.key_radioBackground);
        themeDescriptionArr[16] = new ThemeDescription(this.listView, ThemeDescription.FLAG_CHECKBOXCHECK, new Class[]{RadioCell.class}, new String[]{"radioButton"}, null, null, null, Theme.key_radioBackgroundChecked);
        return themeDescriptionArr;
    }
}
