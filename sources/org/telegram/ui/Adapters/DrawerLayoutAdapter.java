package org.telegram.ui.Adapters;

import android.content.Context;
import android.os.Build.VERSION;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.RecyclerView.ItemAnimator;
import androidx.recyclerview.widget.RecyclerView.LayoutParams;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import java.util.ArrayList;
import java.util.Collections;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.DividerCell;
import org.telegram.ui.Cells.DrawerActionCell;
import org.telegram.ui.Cells.DrawerAddCell;
import org.telegram.ui.Cells.DrawerProfileCell;
import org.telegram.ui.Cells.DrawerUserCell;
import org.telegram.ui.Cells.EmptyCell;
import org.telegram.ui.Components.RecyclerListView.Holder;
import org.telegram.ui.Components.RecyclerListView.SelectionAdapter;

public class DrawerLayoutAdapter extends SelectionAdapter {
    private ArrayList<Integer> accountNumbers = new ArrayList();
    private boolean accountsShowed;
    private ItemAnimator itemAnimator;
    private ArrayList<Item> items = new ArrayList(11);
    private Context mContext;
    private DrawerProfileCell profileCell;

    private class Item {
        public int icon;
        public int id;
        public String text;

        public Item(int i, String str, int i2) {
            this.icon = i2;
            this.id = i;
            this.text = str;
        }

        public void bind(DrawerActionCell drawerActionCell) {
            drawerActionCell.setTextAndIcon(this.text, this.icon);
        }
    }

    public DrawerLayoutAdapter(Context context, ItemAnimator itemAnimator) {
        this.mContext = context;
        this.itemAnimator = itemAnimator;
        boolean z = true;
        if (UserConfig.getActivatedAccountsCount() <= 1 || !MessagesController.getGlobalMainSettings().getBoolean("accountsShowed", true)) {
            z = false;
        }
        this.accountsShowed = z;
        Theme.createDialogsResources(context);
        resetItems();
    }

    private int getAccountRowsCount() {
        int size = this.accountNumbers.size() + 1;
        return this.accountNumbers.size() < 3 ? size + 1 : size;
    }

    public int getItemCount() {
        int size = this.items.size() + 2;
        return this.accountsShowed ? size + getAccountRowsCount() : size;
    }

    public void setAccountsShowed(boolean z, boolean z2) {
        if (this.accountsShowed != z && !this.itemAnimator.isRunning()) {
            this.accountsShowed = z;
            DrawerProfileCell drawerProfileCell = this.profileCell;
            if (drawerProfileCell != null) {
                drawerProfileCell.setAccountsShowed(this.accountsShowed, z2);
            }
            MessagesController.getGlobalMainSettings().edit().putBoolean("accountsShowed", this.accountsShowed).commit();
            if (!z2) {
                notifyDataSetChanged();
            } else if (this.accountsShowed) {
                notifyItemRangeInserted(2, getAccountRowsCount());
            } else {
                notifyItemRangeRemoved(2, getAccountRowsCount());
            }
        }
    }

    public boolean isAccountsShowed() {
        return this.accountsShowed;
    }

    public void notifyDataSetChanged() {
        resetItems();
        super.notifyDataSetChanged();
    }

    public boolean isEnabled(ViewHolder viewHolder) {
        int itemViewType = viewHolder.getItemViewType();
        return itemViewType == 3 || itemViewType == 4 || itemViewType == 5;
    }

    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View drawerProfileCell;
        if (i == 0) {
            drawerProfileCell = new DrawerProfileCell(this.mContext);
            this.profileCell = drawerProfileCell;
        } else if (i == 2) {
            drawerProfileCell = new DividerCell(this.mContext);
        } else if (i == 3) {
            drawerProfileCell = new DrawerActionCell(this.mContext);
        } else if (i == 4) {
            drawerProfileCell = new DrawerUserCell(this.mContext);
        } else if (i != 5) {
            drawerProfileCell = new EmptyCell(this.mContext, AndroidUtilities.dp(8.0f));
        } else {
            drawerProfileCell = new DrawerAddCell(this.mContext);
        }
        drawerProfileCell.setLayoutParams(new LayoutParams(-1, -2));
        return new Holder(drawerProfileCell);
    }

    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        int itemViewType = viewHolder.getItemViewType();
        if (itemViewType == 0) {
            ((DrawerProfileCell) viewHolder.itemView).setUser(MessagesController.getInstance(UserConfig.selectedAccount).getUser(Integer.valueOf(UserConfig.getInstance(UserConfig.selectedAccount).getClientUserId())), this.accountsShowed);
        } else if (itemViewType == 3) {
            i -= 2;
            if (this.accountsShowed) {
                i -= getAccountRowsCount();
            }
            DrawerActionCell drawerActionCell = (DrawerActionCell) viewHolder.itemView;
            ((Item) this.items.get(i)).bind(drawerActionCell);
            drawerActionCell.setPadding(0, 0, 0, 0);
        } else if (itemViewType == 4) {
            ((DrawerUserCell) viewHolder.itemView).setAccount(((Integer) this.accountNumbers.get(i - 2)).intValue());
        }
    }

    public int getItemViewType(int i) {
        if (i == 0) {
            return 0;
        }
        if (i == 1) {
            return 1;
        }
        i -= 2;
        if (this.accountsShowed) {
            if (i < this.accountNumbers.size()) {
                return 4;
            }
            if (this.accountNumbers.size() < 3) {
                if (i == this.accountNumbers.size()) {
                    return 5;
                }
                if (i == this.accountNumbers.size() + 1) {
                    return 2;
                }
            } else if (i == this.accountNumbers.size()) {
                return 2;
            }
            i -= getAccountRowsCount();
        }
        return this.items.get(i) == null ? 2 : 3;
    }

    private void resetItems() {
        this.accountNumbers.clear();
        Object obj = null;
        for (int i = 0; i < 3; i++) {
            if (UserConfig.getInstance(i).isClientActivated()) {
                this.accountNumbers.add(Integer.valueOf(i));
            }
        }
        Collections.sort(this.accountNumbers, -$$Lambda$DrawerLayoutAdapter$mi1sw6PViLc4Y6s0MqsHrA-JKuc.INSTANCE);
        this.items.clear();
        if (UserConfig.getInstance(UserConfig.selectedAccount).isClientActivated()) {
            if (!(VERSION.SDK_INT < 18 || TextUtils.isEmpty(UserConfig.getInstance(UserConfig.selectedAccount).walletConfig) || TextUtils.isEmpty(UserConfig.getInstance(UserConfig.selectedAccount).walletBlockchainName))) {
                obj = 1;
            }
            String str = "SavedMessages";
            String str2 = "Calls";
            String str3 = "Contacts";
            String str4 = "NewGroup";
            if (Theme.getEventType() == 0) {
                this.items.add(new Item(2, LocaleController.getString(str4, NUM), NUM));
                if (obj == null) {
                    this.items.add(new Item(3, LocaleController.getString("NewSecretChat", NUM), NUM));
                    this.items.add(new Item(4, LocaleController.getString("NewChannel", NUM), NUM));
                }
                this.items.add(new Item(6, LocaleController.getString(str3, NUM), NUM));
                this.items.add(new Item(10, LocaleController.getString(str2, NUM), NUM));
                this.items.add(new Item(11, LocaleController.getString(str, NUM), NUM));
                this.items.add(new Item(8, LocaleController.getString("Settings", NUM), NUM));
                if (obj != null) {
                    this.items.add(new Item(12, LocaleController.getString("Wallet", NUM), NUM));
                }
                this.items.add(null);
                this.items.add(new Item(7, LocaleController.getString("InviteFriends", NUM), NUM));
                this.items.add(new Item(9, LocaleController.getString("TelegramFAQ", NUM), NUM));
            } else {
                this.items.add(new Item(2, LocaleController.getString(str4, NUM), NUM));
                if (obj == null) {
                    this.items.add(new Item(3, LocaleController.getString("NewSecretChat", NUM), NUM));
                    this.items.add(new Item(4, LocaleController.getString("NewChannel", NUM), NUM));
                }
                this.items.add(new Item(6, LocaleController.getString(str3, NUM), NUM));
                this.items.add(new Item(10, LocaleController.getString(str2, NUM), NUM));
                this.items.add(new Item(11, LocaleController.getString(str, NUM), NUM));
                if (obj != null) {
                    this.items.add(new Item(12, LocaleController.getString("Wallet", NUM), NUM));
                }
                this.items.add(new Item(8, LocaleController.getString("Settings", NUM), NUM));
                this.items.add(null);
                this.items.add(new Item(7, LocaleController.getString("InviteFriends", NUM), NUM));
                this.items.add(new Item(9, LocaleController.getString("TelegramFAQ", NUM), NUM));
            }
        }
    }

    static /* synthetic */ int lambda$resetItems$0(Integer num, Integer num2) {
        long j = (long) UserConfig.getInstance(num.intValue()).loginTime;
        long j2 = (long) UserConfig.getInstance(num2.intValue()).loginTime;
        if (j > j2) {
            return 1;
        }
        return j < j2 ? -1 : 0;
    }

    public int getId(int i) {
        i -= 2;
        if (this.accountsShowed) {
            i -= getAccountRowsCount();
        }
        if (i < 0 || i >= this.items.size()) {
            return -1;
        }
        Item item = (Item) this.items.get(i);
        if (item != null) {
            return item.id;
        }
        return -1;
    }
}
