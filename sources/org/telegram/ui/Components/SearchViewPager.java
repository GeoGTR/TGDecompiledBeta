package org.telegram.ui.Components;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import org.telegram.messenger.AccountInstance;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC$Chat;
import org.telegram.tgnet.TLRPC$MessageEntity;
import org.telegram.tgnet.TLRPC$ReplyMarkup;
import org.telegram.tgnet.TLRPC$User;
import org.telegram.tgnet.TLRPC$WebPage;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.BackDrawable;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.MenuDrawable;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.Adapters.DialogsSearchAdapter;
import org.telegram.ui.Adapters.FiltersView;
import org.telegram.ui.Cells.ContextLinkCell;
import org.telegram.ui.Cells.DialogCell;
import org.telegram.ui.Cells.SharedAudioCell;
import org.telegram.ui.Cells.SharedDocumentCell;
import org.telegram.ui.Cells.SharedLinkCell;
import org.telegram.ui.Cells.SharedPhotoVideoCell;
import org.telegram.ui.ChatActivity;
import org.telegram.ui.Components.SearchViewPager;
import org.telegram.ui.Components.ViewPagerFixed;
import org.telegram.ui.DialogsActivity;
import org.telegram.ui.FilteredSearchView;

public class SearchViewPager extends ViewPagerFixed implements FilteredSearchView.UiCallback {
    boolean attached;
    int currentAccount = UserConfig.selectedAccount;
    ArrayList<SearchResultsEnterAnimator> currentAnimators = new ArrayList<>();
    private ArrayList<FiltersView.MediaFilterData> currentSearchFilters = new ArrayList<>();
    public DialogsSearchAdapter dialogsSearchAdapter;
    public StickerEmptyView emptyView;
    private FilteredSearchView.Delegate filteredSearchViewDelegate;
    private final int folderId;
    private ActionBarMenuItem forwardItem;
    private ActionBarMenuItem gotoItem;
    private boolean isActionModeShowed;
    private int keyboardSize;
    /* access modifiers changed from: private */
    public boolean lastSearchScrolledToTop;
    String lastSearchString;
    /* access modifiers changed from: private */
    public FilteredSearchView noMediaFiltersSearchView;
    BaseFragment parent;
    public FrameLayout searchContainer;
    public RecyclerListView searchListView;
    /* access modifiers changed from: private */
    public LinearLayoutManager searchlayoutManager;
    private HashMap<FilteredSearchView.MessageHashId, MessageObject> selectedFiles = new HashMap<>();
    private NumberTextView selectedMessagesCountTextView;
    /* access modifiers changed from: private */
    public boolean showOnlyDialogsAdapter;

    public interface ChatPreviewDelegate {
        void finish();

        void move(float f);

        void startChatPreview(DialogCell dialogCell);
    }

    static /* synthetic */ boolean lambda$showActionMode$0(View view, MotionEvent motionEvent) {
        return true;
    }

    public SearchViewPager(Context context, final BaseFragment baseFragment, int i, int i2, int i3, final ChatPreviewDelegate chatPreviewDelegate) {
        super(context);
        this.folderId = i3;
        this.parent = baseFragment;
        this.dialogsSearchAdapter = new DialogsSearchAdapter(context, i, i2, i3) {
            public void notifyDataSetChanged() {
                RecyclerListView recyclerListView;
                int currentItemCount = getCurrentItemCount();
                super.notifyDataSetChanged();
                if (!SearchViewPager.this.lastSearchScrolledToTop && (recyclerListView = SearchViewPager.this.searchListView) != null) {
                    recyclerListView.scrollToPosition(0);
                    boolean unused = SearchViewPager.this.lastSearchScrolledToTop = true;
                }
                if (getItemCount() == 0 && currentItemCount != 0 && !isSearching()) {
                    SearchViewPager.this.emptyView.showProgress(false, false);
                }
            }
        };
        AnonymousClass2 r10 = new RecyclerListView(context) {
            /* access modifiers changed from: protected */
            public void dispatchDraw(Canvas canvas) {
                int childCount = getChildCount();
                for (int i = 0; i < childCount; i++) {
                    View childAt = getChildAt(i);
                    RecyclerView.ViewHolder childViewHolder = SearchViewPager.this.searchListView.getChildViewHolder(childAt);
                    if (childViewHolder != null && !childViewHolder.shouldIgnore()) {
                        int position = SearchViewPager.this.searchlayoutManager.getPosition(childAt);
                        int i2 = 0;
                        while (true) {
                            if (i2 >= SearchViewPager.this.currentAnimators.size()) {
                                childAt.setAlpha(1.0f);
                                break;
                            } else if (SearchViewPager.this.currentAnimators.get(i2).setup(childAt, position)) {
                                break;
                            } else {
                                i2++;
                            }
                        }
                    }
                }
                super.dispatchDraw(canvas);
            }
        };
        this.searchListView = r10;
        r10.setPivotY(0.0f);
        this.searchListView.setAdapter(this.dialogsSearchAdapter);
        this.searchListView.setVerticalScrollBarEnabled(true);
        this.searchListView.setInstantClick(true);
        this.searchListView.setVerticalScrollbarPosition(LocaleController.isRTL ? 1 : 2);
        RecyclerListView recyclerListView = this.searchListView;
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context, 1, false);
        this.searchlayoutManager = linearLayoutManager;
        recyclerListView.setLayoutManager(linearLayoutManager);
        this.searchListView.setAnimateEmptyView(true, 0);
        this.searchListView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            public void onScrollStateChanged(RecyclerView recyclerView, int i) {
                if (i == 1) {
                    AndroidUtilities.hideKeyboard(baseFragment.getParentActivity().getCurrentFocus());
                }
            }

            public void onScrolled(RecyclerView recyclerView, int i, int i2) {
                int abs = Math.abs(SearchViewPager.this.searchlayoutManager.findLastVisibleItemPosition() - SearchViewPager.this.searchlayoutManager.findFirstVisibleItemPosition()) + 1;
                int itemCount = recyclerView.getAdapter().getItemCount();
                if (abs > 0 && SearchViewPager.this.searchlayoutManager.findLastVisibleItemPosition() == itemCount - 1 && !SearchViewPager.this.dialogsSearchAdapter.isMessagesSearchEndReached()) {
                    SearchViewPager.this.dialogsSearchAdapter.loadMoreSearchMessages();
                }
            }
        });
        FilteredSearchView filteredSearchView = new FilteredSearchView(this.parent);
        this.noMediaFiltersSearchView = filteredSearchView;
        filteredSearchView.setUiCallback(this);
        this.noMediaFiltersSearchView.setVisibility(8);
        this.noMediaFiltersSearchView.setChatPreviewDelegate(chatPreviewDelegate);
        FrameLayout frameLayout = new FrameLayout(context);
        this.searchContainer = frameLayout;
        frameLayout.addView(this.searchListView);
        this.searchContainer.addView(this.noMediaFiltersSearchView);
        FlickerLoadingView flickerLoadingView = new FlickerLoadingView(context);
        flickerLoadingView.setViewType(1);
        AnonymousClass4 r12 = new StickerEmptyView(context, flickerLoadingView, 1) {
            public void setVisibility(int i) {
                if (SearchViewPager.this.noMediaFiltersSearchView.getTag() != null) {
                    super.setVisibility(8);
                } else {
                    super.setVisibility(i);
                }
            }
        };
        this.emptyView = r12;
        r12.title.setText(LocaleController.getString("NoResult", NUM));
        this.emptyView.subtitle.setVisibility(8);
        this.emptyView.setVisibility(8);
        this.emptyView.addView(flickerLoadingView, 0);
        this.emptyView.showProgress(true, false);
        this.searchContainer.addView(this.emptyView);
        this.searchListView.setEmptyView(this.emptyView);
        setAdapter(new ViewPagerFixed.Adapter() {
            public int getItemViewType(int i) {
                if (i == 0) {
                    return 1;
                }
                return i + 2;
            }

            public String getItemTitle(int i) {
                if (i == 0) {
                    return LocaleController.getString("SearchAllChatsShort", NUM);
                }
                return FiltersView.filters[i - 1].title;
            }

            public int getItemCount() {
                if (SearchViewPager.this.showOnlyDialogsAdapter) {
                    return 1;
                }
                return 1 + FiltersView.filters.length;
            }

            public View createView(int i) {
                if (i == 1) {
                    return SearchViewPager.this.searchContainer;
                }
                FilteredSearchView filteredSearchView = new FilteredSearchView(SearchViewPager.this.parent);
                filteredSearchView.setChatPreviewDelegate(chatPreviewDelegate);
                filteredSearchView.setUiCallback(SearchViewPager.this);
                return filteredSearchView;
            }

            public void bindView(View view, int i, int i2) {
                SearchViewPager searchViewPager = SearchViewPager.this;
                searchViewPager.search(view, i, searchViewPager.lastSearchString, true);
            }
        });
    }

    public void onTextChanged(String str) {
        this.lastSearchString = str;
        search(getCurrentView(), getCurrentPosition(), str, !this.attached);
    }

    /* access modifiers changed from: private */
    public void search(View view, int i, String str, boolean z) {
        boolean z2;
        View view2 = view;
        long j = 0;
        long j2 = 0;
        int i2 = 0;
        for (int i3 = 0; i3 < this.currentSearchFilters.size(); i3++) {
            FiltersView.MediaFilterData mediaFilterData = this.currentSearchFilters.get(i3);
            int i4 = mediaFilterData.filterType;
            if (i4 == 4) {
                TLObject tLObject = mediaFilterData.chat;
                if (tLObject instanceof TLRPC$User) {
                    i2 = ((TLRPC$User) tLObject).id;
                } else if (tLObject instanceof TLRPC$Chat) {
                    i2 = -((TLRPC$Chat) tLObject).id;
                }
            } else if (i4 == 6) {
                FiltersView.DateData dateData = mediaFilterData.dateData;
                long j3 = dateData.minDate;
                long j4 = dateData.maxDate;
                j = j3;
                j2 = j4;
            }
        }
        boolean z3 = true;
        if (view2 == this.searchContainer) {
            if (i2 == 0 && j == 0 && j2 == 0) {
                this.lastSearchScrolledToTop = false;
                this.dialogsSearchAdapter.searchDialogs(str);
                this.dialogsSearchAdapter.setFiltersDelegate(this.filteredSearchViewDelegate, false);
                this.noMediaFiltersSearchView.animate().setListener((Animator.AnimatorListener) null).cancel();
                this.noMediaFiltersSearchView.setDelegate((FilteredSearchView.Delegate) null, false);
                if (z) {
                    this.emptyView.showProgress(!this.dialogsSearchAdapter.isSearching(), false);
                    this.emptyView.showProgress(this.dialogsSearchAdapter.isSearching(), false);
                } else if (!this.dialogsSearchAdapter.hasRecentSearch()) {
                    this.emptyView.showProgress(this.dialogsSearchAdapter.isSearching(), true);
                }
                if (z) {
                    this.noMediaFiltersSearchView.setVisibility(8);
                } else if (this.noMediaFiltersSearchView.getVisibility() != 8) {
                    this.noMediaFiltersSearchView.animate().alpha(0.0f).setListener(new AnimatorListenerAdapter() {
                        public void onAnimationEnd(Animator animator) {
                            SearchViewPager.this.noMediaFiltersSearchView.setVisibility(8);
                        }
                    }).setDuration(150).start();
                }
                this.noMediaFiltersSearchView.setTag((Object) null);
            } else {
                String str2 = str;
                this.noMediaFiltersSearchView.setTag(1);
                this.noMediaFiltersSearchView.setDelegate(this.filteredSearchViewDelegate, false);
                this.noMediaFiltersSearchView.animate().setListener((Animator.AnimatorListener) null).cancel();
                if (z) {
                    this.noMediaFiltersSearchView.setVisibility(0);
                    this.noMediaFiltersSearchView.setAlpha(1.0f);
                    z2 = z;
                } else {
                    if (this.noMediaFiltersSearchView.getVisibility() != 0) {
                        this.noMediaFiltersSearchView.setVisibility(0);
                        this.noMediaFiltersSearchView.setAlpha(0.0f);
                    } else {
                        z3 = z;
                    }
                    this.noMediaFiltersSearchView.animate().alpha(1.0f).setDuration(150).start();
                    z2 = z3;
                }
                this.noMediaFiltersSearchView.search(i2, j, j2, (FiltersView.MediaFilterData) null, str, z2);
                this.emptyView.setVisibility(8);
            }
            this.emptyView.setKeyboardHeight(this.keyboardSize, false);
            this.noMediaFiltersSearchView.setKeyboardHeight(this.keyboardSize, false);
            return;
        }
        String str3 = str;
        FilteredSearchView filteredSearchView = (FilteredSearchView) view2;
        filteredSearchView.setKeyboardHeight(this.keyboardSize, false);
        filteredSearchView.search(i2, j, j2, FiltersView.filters[i - 1], str, z);
    }

    public void onResume() {
        DialogsSearchAdapter dialogsSearchAdapter2 = this.dialogsSearchAdapter;
        if (dialogsSearchAdapter2 != null) {
            dialogsSearchAdapter2.notifyDataSetChanged();
        }
    }

    public void removeSearchFilter(FiltersView.MediaFilterData mediaFilterData) {
        this.currentSearchFilters.remove(mediaFilterData);
    }

    public ArrayList<FiltersView.MediaFilterData> getCurrentSearchFilters() {
        return this.currentSearchFilters;
    }

    public void clear() {
        this.currentSearchFilters.clear();
    }

    public void setFilteredSearchViewDelegate(FilteredSearchView.Delegate delegate) {
        this.filteredSearchViewDelegate = delegate;
    }

    private void showActionMode(boolean z) {
        if (this.isActionModeShowed != z) {
            if (z && !this.parent.getActionBar().actionModeIsExist("search_view_pager")) {
                ActionBarMenu createActionMode = this.parent.getActionBar().createActionMode(true, "search_view_pager");
                NumberTextView numberTextView = new NumberTextView(createActionMode.getContext());
                this.selectedMessagesCountTextView = numberTextView;
                numberTextView.setTextSize(18);
                this.selectedMessagesCountTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
                this.selectedMessagesCountTextView.setTextColor(Theme.getColor("actionBarActionModeDefaultIcon"));
                createActionMode.addView(this.selectedMessagesCountTextView, LayoutHelper.createLinear(0, -1, 1.0f, 72, 0, 0, 0));
                this.selectedMessagesCountTextView.setOnTouchListener($$Lambda$SearchViewPager$TDsWTBkeEmn4ywq1WEkaayHK0U.INSTANCE);
                this.gotoItem = createActionMode.addItemWithWidth(200, NUM, AndroidUtilities.dp(54.0f), LocaleController.getString("AccDescrGoToMessage", NUM));
                this.forwardItem = createActionMode.addItemWithWidth(201, NUM, AndroidUtilities.dp(54.0f), LocaleController.getString("Forward", NUM));
            }
            if (this.parent.getActionBar().getBackButton().getDrawable() instanceof MenuDrawable) {
                this.parent.getActionBar().setBackButtonDrawable(new BackDrawable(false));
            }
            this.isActionModeShowed = z;
            if (z) {
                AndroidUtilities.hideKeyboard(this.parent.getParentActivity().getCurrentFocus());
                this.parent.getActionBar().showActionMode();
                this.selectedMessagesCountTextView.setNumber(this.selectedFiles.size(), false);
                this.gotoItem.setVisibility(0);
                this.forwardItem.setVisibility(0);
                return;
            }
            this.parent.getActionBar().hideActionMode();
            this.selectedFiles.clear();
            for (int i = 0; i < getChildCount(); i++) {
                if (getChildAt(i) instanceof FilteredSearchView) {
                    ((FilteredSearchView) getChildAt(i)).update();
                }
            }
            FilteredSearchView filteredSearchView = this.noMediaFiltersSearchView;
            if (filteredSearchView != null) {
                filteredSearchView.update();
            }
            int size = this.viewsByType.size();
            for (int i2 = 0; i2 < size; i2++) {
                View valueAt = this.viewsByType.valueAt(i2);
                if (valueAt instanceof FilteredSearchView) {
                    ((FilteredSearchView) valueAt).update();
                }
            }
        }
    }

    public void onActionBarItemClick(int i) {
        if (i == 200) {
            if (this.selectedFiles.size() == 1) {
                goToMessage(this.selectedFiles.values().iterator().next());
            }
        } else if (i == 201) {
            Bundle bundle = new Bundle();
            bundle.putBoolean("onlySelect", true);
            bundle.putInt("dialogsType", 3);
            DialogsActivity dialogsActivity = new DialogsActivity(bundle);
            dialogsActivity.setDelegate(new DialogsActivity.DialogsActivityDelegate() {
                public final void didSelectDialogs(DialogsActivity dialogsActivity, ArrayList arrayList, CharSequence charSequence, boolean z) {
                    SearchViewPager.this.lambda$onActionBarItemClick$1$SearchViewPager(dialogsActivity, arrayList, charSequence, z);
                }
            });
            this.parent.presentFragment(dialogsActivity);
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onActionBarItemClick$1 */
    public /* synthetic */ void lambda$onActionBarItemClick$1$SearchViewPager(DialogsActivity dialogsActivity, ArrayList arrayList, CharSequence charSequence, boolean z) {
        DialogsActivity dialogsActivity2 = dialogsActivity;
        ArrayList arrayList2 = arrayList;
        ArrayList arrayList3 = new ArrayList();
        for (FilteredSearchView.MessageHashId messageHashId : this.selectedFiles.keySet()) {
            arrayList3.add(this.selectedFiles.get(messageHashId));
        }
        this.selectedFiles.clear();
        showActionMode(false);
        if (arrayList.size() > 1 || ((Long) arrayList2.get(0)).longValue() == ((long) AccountInstance.getInstance(this.currentAccount).getUserConfig().getClientUserId()) || charSequence != null) {
            for (int i = 0; i < arrayList.size(); i++) {
                long longValue = ((Long) arrayList2.get(i)).longValue();
                if (charSequence != null) {
                    AccountInstance.getInstance(this.currentAccount).getSendMessagesHelper().sendMessage(charSequence.toString(), longValue, (MessageObject) null, (MessageObject) null, (TLRPC$WebPage) null, true, (ArrayList<TLRPC$MessageEntity>) null, (TLRPC$ReplyMarkup) null, (HashMap<String, String>) null, true, 0);
                }
                AccountInstance.getInstance(this.currentAccount).getSendMessagesHelper().sendMessage(arrayList3, longValue, true, 0);
            }
            dialogsActivity.finishFragment();
            return;
        }
        long longValue2 = ((Long) arrayList2.get(0)).longValue();
        int i2 = (int) longValue2;
        int i3 = (int) (longValue2 >> 32);
        Bundle bundle = new Bundle();
        bundle.putBoolean("scrollToTopOnResume", true);
        if (i2 == 0) {
            bundle.putInt("enc_id", i3);
        } else if (i2 > 0) {
            bundle.putInt("user_id", i2);
        } else if (i2 < 0) {
            bundle.putInt("chat_id", -i2);
        }
        if (i2 == 0 || AccountInstance.getInstance(this.currentAccount).getMessagesController().checkCanOpenChat(bundle, dialogsActivity2)) {
            ChatActivity chatActivity = new ChatActivity(bundle);
            dialogsActivity2.presentFragment(chatActivity, true);
            chatActivity.showFieldPanelForForward(true, arrayList3);
        }
    }

    public void goToMessage(MessageObject messageObject) {
        Bundle bundle = new Bundle();
        int dialogId = (int) messageObject.getDialogId();
        int dialogId2 = (int) (messageObject.getDialogId() >> 32);
        if (dialogId == 0) {
            bundle.putInt("enc_id", dialogId2);
        } else if (dialogId > 0) {
            bundle.putInt("user_id", dialogId);
        } else if (dialogId < 0) {
            TLRPC$Chat chat = AccountInstance.getInstance(this.currentAccount).getMessagesController().getChat(Integer.valueOf(-dialogId));
            if (!(chat == null || chat.migrated_to == null)) {
                bundle.putInt("migrated_to", dialogId);
                dialogId = -chat.migrated_to.channel_id;
            }
            bundle.putInt("chat_id", -dialogId);
        }
        bundle.putInt("message_id", messageObject.getId());
        this.parent.presentFragment(new ChatActivity(bundle));
        showActionMode(false);
    }

    public int getFolderId() {
        return this.folderId;
    }

    public boolean actionModeShowing() {
        return this.isActionModeShowed;
    }

    public void hideActionMode() {
        showActionMode(false);
    }

    public void toggleItemSelection(MessageObject messageObject, View view, int i) {
        FilteredSearchView.MessageHashId messageHashId = new FilteredSearchView.MessageHashId(messageObject.getId(), messageObject.getDialogId());
        if (this.selectedFiles.containsKey(messageHashId)) {
            this.selectedFiles.remove(messageHashId);
        } else if (this.selectedFiles.size() < 100) {
            this.selectedFiles.put(messageHashId, messageObject);
        } else {
            return;
        }
        int i2 = 0;
        if (this.selectedFiles.size() == 0) {
            showActionMode(false);
        } else {
            this.selectedMessagesCountTextView.setNumber(this.selectedFiles.size(), true);
            ActionBarMenuItem actionBarMenuItem = this.gotoItem;
            if (actionBarMenuItem != null) {
                if (this.selectedFiles.size() != 1) {
                    i2 = 8;
                }
                actionBarMenuItem.setVisibility(i2);
            }
        }
        if (view instanceof SharedDocumentCell) {
            ((SharedDocumentCell) view).setChecked(this.selectedFiles.containsKey(messageHashId), true);
        } else if (view instanceof SharedPhotoVideoCell) {
            ((SharedPhotoVideoCell) view).setChecked(i, this.selectedFiles.containsKey(messageHashId), true);
        } else if (view instanceof SharedLinkCell) {
            ((SharedLinkCell) view).setChecked(this.selectedFiles.containsKey(messageHashId), true);
        } else if (view instanceof SharedAudioCell) {
            ((SharedAudioCell) view).setChecked(this.selectedFiles.containsKey(messageHashId), true);
        } else if (view instanceof ContextLinkCell) {
            ((ContextLinkCell) view).setChecked(this.selectedFiles.containsKey(messageHashId), true);
        } else if (view instanceof DialogCell) {
            ((DialogCell) view).setChecked(this.selectedFiles.containsKey(messageHashId), true);
        }
    }

    public boolean isSelected(FilteredSearchView.MessageHashId messageHashId) {
        return this.selectedFiles.containsKey(messageHashId);
    }

    public void showActionMode() {
        showActionMode(true);
    }

    /* access modifiers changed from: protected */
    public void onItemSelected(View view, View view2, int i, int i2) {
        boolean z = true;
        if (i == 0) {
            if (this.noMediaFiltersSearchView.getVisibility() == 0) {
                this.noMediaFiltersSearchView.setDelegate(this.filteredSearchViewDelegate, false);
                this.dialogsSearchAdapter.setFiltersDelegate((FilteredSearchView.Delegate) null, false);
            } else {
                this.noMediaFiltersSearchView.setDelegate((FilteredSearchView.Delegate) null, false);
                this.dialogsSearchAdapter.setFiltersDelegate(this.filteredSearchViewDelegate, true);
            }
        } else if (view instanceof FilteredSearchView) {
            if (i2 != 0 || this.noMediaFiltersSearchView.getVisibility() == 0) {
                z = false;
            }
            ((FilteredSearchView) view).setDelegate(this.filteredSearchViewDelegate, z);
        }
        if (view2 instanceof FilteredSearchView) {
            ((FilteredSearchView) view2).setDelegate((FilteredSearchView.Delegate) null, false);
            return;
        }
        this.dialogsSearchAdapter.setFiltersDelegate((FilteredSearchView.Delegate) null, false);
        this.noMediaFiltersSearchView.setDelegate((FilteredSearchView.Delegate) null, false);
    }

    public void getThemeDescriptors(ArrayList<ThemeDescription> arrayList) {
        ArrayList<ThemeDescription> arrayList2 = arrayList;
        for (int i = 0; i < getChildCount(); i++) {
            if (getChildAt(i) instanceof FilteredSearchView) {
                arrayList2.addAll(((FilteredSearchView) getChildAt(i)).getThemeDescriptions());
            }
        }
        int size = this.viewsByType.size();
        for (int i2 = 0; i2 < size; i2++) {
            View valueAt = this.viewsByType.valueAt(i2);
            if (valueAt instanceof FilteredSearchView) {
                arrayList2.addAll(((FilteredSearchView) valueAt).getThemeDescriptions());
            }
        }
        FilteredSearchView filteredSearchView = this.noMediaFiltersSearchView;
        if (filteredSearchView != null) {
            arrayList2.addAll(filteredSearchView.getThemeDescriptions());
        }
        arrayList2.add(new ThemeDescription(this.emptyView.title, ThemeDescription.FLAG_TEXTCOLOR, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundWhiteBlackText"));
        arrayList2.add(new ThemeDescription(this.emptyView.subtitle, ThemeDescription.FLAG_TEXTCOLOR, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundWhiteGrayText"));
    }

    public void updateColors() {
        for (int i = 0; i < getChildCount(); i++) {
            if (getChildAt(i) instanceof FilteredSearchView) {
                RecyclerListView recyclerListView = ((FilteredSearchView) getChildAt(i)).recyclerListView;
                int childCount = recyclerListView.getChildCount();
                for (int i2 = 0; i2 < childCount; i2++) {
                    View childAt = recyclerListView.getChildAt(i2);
                    if (childAt instanceof DialogCell) {
                        ((DialogCell) childAt).update(0);
                    }
                }
            }
        }
        int size = this.viewsByType.size();
        for (int i3 = 0; i3 < size; i3++) {
            View valueAt = this.viewsByType.valueAt(i3);
            if (valueAt instanceof FilteredSearchView) {
                RecyclerListView recyclerListView2 = ((FilteredSearchView) valueAt).recyclerListView;
                int childCount2 = recyclerListView2.getChildCount();
                for (int i4 = 0; i4 < childCount2; i4++) {
                    View childAt2 = recyclerListView2.getChildAt(i4);
                    if (childAt2 instanceof DialogCell) {
                        ((DialogCell) childAt2).update(0);
                    }
                }
            }
        }
        FilteredSearchView filteredSearchView = this.noMediaFiltersSearchView;
        if (filteredSearchView != null) {
            RecyclerListView recyclerListView3 = filteredSearchView.recyclerListView;
            int childCount3 = recyclerListView3.getChildCount();
            for (int i5 = 0; i5 < childCount3; i5++) {
                View childAt3 = recyclerListView3.getChildAt(i5);
                if (childAt3 instanceof DialogCell) {
                    ((DialogCell) childAt3).update(0);
                }
            }
        }
    }

    public void reset() {
        setPosition(0);
        if (this.dialogsSearchAdapter.getItemCount() > 0) {
            this.searchlayoutManager.scrollToPositionWithOffset(0, 0);
        }
        this.viewsByType.clear();
    }

    public void setPosition(int i) {
        super.setPosition(i);
        this.viewsByType.clear();
        ViewPagerFixed.TabsView tabsView = this.tabsView;
        if (tabsView != null) {
            tabsView.selectTabWithId(i, 1.0f);
        }
        invalidate();
    }

    public void setKeyboardHeight(int i) {
        this.keyboardSize = i;
        boolean z = getVisibility() == 0 && getAlpha() > 0.0f;
        for (int i2 = 0; i2 < getChildCount(); i2++) {
            if (getChildAt(i2) instanceof FilteredSearchView) {
                ((FilteredSearchView) getChildAt(i2)).setKeyboardHeight(i, z);
            } else if (getChildAt(i2) == this.searchContainer) {
                this.emptyView.setKeyboardHeight(i, z);
                this.noMediaFiltersSearchView.setKeyboardHeight(i, z);
            }
        }
    }

    public void showOnlyDialogsAdapter(boolean z) {
        this.showOnlyDialogsAdapter = z;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0077, code lost:
        if (org.telegram.messenger.ChatObject.isChannel(r6, r10.currentAccount) != false) goto L_0x007b;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void messagesDeleted(int r11, java.util.ArrayList<java.lang.Integer> r12) {
        /*
            r10 = this;
            android.util.SparseArray<android.view.View> r0 = r10.viewsByType
            int r0 = r0.size()
            r1 = 0
            r2 = 0
        L_0x0008:
            if (r2 >= r0) goto L_0x001e
            android.util.SparseArray<android.view.View> r3 = r10.viewsByType
            java.lang.Object r3 = r3.valueAt(r2)
            android.view.View r3 = (android.view.View) r3
            boolean r4 = r3 instanceof org.telegram.ui.FilteredSearchView
            if (r4 == 0) goto L_0x001b
            org.telegram.ui.FilteredSearchView r3 = (org.telegram.ui.FilteredSearchView) r3
            r3.messagesDeleted(r11, r12)
        L_0x001b:
            int r2 = r2 + 1
            goto L_0x0008
        L_0x001e:
            r0 = 0
        L_0x001f:
            int r2 = r10.getChildCount()
            if (r0 >= r2) goto L_0x0039
            android.view.View r2 = r10.getChildAt(r0)
            boolean r2 = r2 instanceof org.telegram.ui.FilteredSearchView
            if (r2 == 0) goto L_0x0036
            android.view.View r2 = r10.getChildAt(r0)
            org.telegram.ui.FilteredSearchView r2 = (org.telegram.ui.FilteredSearchView) r2
            r2.messagesDeleted(r11, r12)
        L_0x0036:
            int r0 = r0 + 1
            goto L_0x001f
        L_0x0039:
            org.telegram.ui.FilteredSearchView r0 = r10.noMediaFiltersSearchView
            r0.messagesDeleted(r11, r12)
            java.util.HashMap<org.telegram.ui.FilteredSearchView$MessageHashId, org.telegram.messenger.MessageObject> r0 = r10.selectedFiles
            boolean r0 = r0.isEmpty()
            if (r0 != 0) goto L_0x00d5
            r0 = 0
            java.util.HashMap<org.telegram.ui.FilteredSearchView$MessageHashId, org.telegram.messenger.MessageObject> r2 = r10.selectedFiles
            java.util.Set r2 = r2.keySet()
            java.util.Iterator r2 = r2.iterator()
        L_0x0051:
            boolean r3 = r2.hasNext()
            if (r3 == 0) goto L_0x00d5
            java.lang.Object r3 = r2.next()
            org.telegram.ui.FilteredSearchView$MessageHashId r3 = (org.telegram.ui.FilteredSearchView.MessageHashId) r3
            java.util.HashMap<org.telegram.ui.FilteredSearchView$MessageHashId, org.telegram.messenger.MessageObject> r4 = r10.selectedFiles
            java.lang.Object r4 = r4.get(r3)
            org.telegram.messenger.MessageObject r4 = (org.telegram.messenger.MessageObject) r4
            long r5 = r4.getDialogId()
            r7 = 0
            int r9 = (r5 > r7 ? 1 : (r5 == r7 ? 0 : -1))
            if (r9 >= 0) goto L_0x007a
            long r5 = -r5
            int r6 = (int) r5
            int r5 = r10.currentAccount
            boolean r5 = org.telegram.messenger.ChatObject.isChannel(r6, r5)
            if (r5 == 0) goto L_0x007a
            goto L_0x007b
        L_0x007a:
            r6 = 0
        L_0x007b:
            if (r6 != r11) goto L_0x009f
            r5 = 0
        L_0x007e:
            int r6 = r12.size()
            if (r5 >= r6) goto L_0x009f
            int r6 = r4.getId()
            java.lang.Object r7 = r12.get(r5)
            java.lang.Integer r7 = (java.lang.Integer) r7
            int r7 = r7.intValue()
            if (r6 != r7) goto L_0x009c
            java.util.ArrayList r0 = new java.util.ArrayList
            r0.<init>()
            r0.add(r3)
        L_0x009c:
            int r5 = r5 + 1
            goto L_0x007e
        L_0x009f:
            if (r0 == 0) goto L_0x0051
            int r3 = r0.size()
            r4 = 0
        L_0x00a6:
            if (r4 >= r3) goto L_0x00b4
            java.util.HashMap<org.telegram.ui.FilteredSearchView$MessageHashId, org.telegram.messenger.MessageObject> r5 = r10.selectedFiles
            java.lang.Object r6 = r0.get(r4)
            r5.remove(r6)
            int r4 = r4 + 1
            goto L_0x00a6
        L_0x00b4:
            org.telegram.ui.Components.NumberTextView r3 = r10.selectedMessagesCountTextView
            java.util.HashMap<org.telegram.ui.FilteredSearchView$MessageHashId, org.telegram.messenger.MessageObject> r4 = r10.selectedFiles
            int r4 = r4.size()
            r5 = 1
            r3.setNumber(r4, r5)
            org.telegram.ui.ActionBar.ActionBarMenuItem r3 = r10.gotoItem
            if (r3 == 0) goto L_0x0051
            java.util.HashMap<org.telegram.ui.FilteredSearchView$MessageHashId, org.telegram.messenger.MessageObject> r4 = r10.selectedFiles
            int r4 = r4.size()
            if (r4 != r5) goto L_0x00ce
            r4 = 0
            goto L_0x00d0
        L_0x00ce:
            r4 = 8
        L_0x00d0:
            r3.setVisibility(r4)
            goto L_0x0051
        L_0x00d5:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.Components.SearchViewPager.messagesDeleted(int, java.util.ArrayList):void");
    }

    public void runResultsEnterAnimation() {
        final HashSet hashSet = new HashSet();
        int childCount = this.searchListView.getChildCount();
        final View view = null;
        for (int i = 0; i < childCount; i++) {
            View childAt = this.searchListView.getChildAt(i);
            int position = this.searchlayoutManager.getPosition(childAt);
            if (childAt instanceof FlickerLoadingView) {
                view = childAt;
            } else {
                hashSet.add(Integer.valueOf(position));
            }
        }
        if (view != null) {
            this.searchListView.removeView(view);
        }
        this.searchListView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            public boolean onPreDraw() {
                SearchViewPager.this.searchListView.getViewTreeObserver().removeOnPreDrawListener(this);
                int childCount = SearchViewPager.this.searchListView.getChildCount();
                for (int i = 0; i < childCount; i++) {
                    View childAt = SearchViewPager.this.searchListView.getChildAt(i);
                    int position = SearchViewPager.this.searchlayoutManager.getPosition(childAt);
                    if (!hashSet.contains(Integer.valueOf(position))) {
                        SearchResultsEnterAnimator searchResultsEnterAnimator = new SearchResultsEnterAnimator();
                        childAt.setAlpha(0.0f);
                        searchResultsEnterAnimator.position = position;
                        searchResultsEnterAnimator.valueAnimator.setStartDelay((long) ((int) ((((float) Math.min(SearchViewPager.this.searchListView.getMeasuredHeight(), Math.max(0, childAt.getTop()))) / ((float) SearchViewPager.this.searchListView.getMeasuredHeight())) * 100.0f)));
                        searchResultsEnterAnimator.valueAnimator.setDuration(200);
                        searchResultsEnterAnimator.valueAnimator.start();
                    }
                }
                View view = view;
                if (view != null && view.getParent() == null) {
                    SearchViewPager.this.searchListView.addView(view);
                    final RecyclerView.LayoutManager layoutManager = SearchViewPager.this.searchListView.getLayoutManager();
                    if (layoutManager != null) {
                        layoutManager.ignoreView(view);
                        View view2 = view;
                        ObjectAnimator ofFloat = ObjectAnimator.ofFloat(view2, View.ALPHA, new float[]{view2.getAlpha(), 0.0f});
                        ofFloat.addListener(new AnimatorListenerAdapter() {
                            public void onAnimationEnd(Animator animator) {
                                view.setAlpha(1.0f);
                                layoutManager.stopIgnoringView(view);
                                AnonymousClass7 r2 = AnonymousClass7.this;
                                SearchViewPager.this.searchListView.removeView(view);
                            }
                        });
                        ofFloat.start();
                    }
                }
                return true;
            }
        });
    }

    public void cancelEnterAnimation() {
        while (this.currentAnimators.size() > 0) {
            SearchResultsEnterAnimator searchResultsEnterAnimator = this.currentAnimators.get(0);
            searchResultsEnterAnimator.valueAnimator.cancel();
            this.currentAnimators.remove(searchResultsEnterAnimator);
        }
    }

    public ViewPagerFixed.TabsView getTabsView() {
        return this.tabsView;
    }

    private class SearchResultsEnterAnimator {
        int position;
        float progress;
        final ValueAnimator valueAnimator;

        private SearchResultsEnterAnimator() {
            ValueAnimator ofFloat = ValueAnimator.ofFloat(new float[]{0.0f, 1.0f});
            this.valueAnimator = ofFloat;
            ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                    SearchViewPager.SearchResultsEnterAnimator.this.lambda$new$0$SearchViewPager$SearchResultsEnterAnimator(valueAnimator);
                }
            });
            ofFloat.addListener(new AnimatorListenerAdapter(SearchViewPager.this) {
                public void onAnimationEnd(Animator animator) {
                    SearchResultsEnterAnimator searchResultsEnterAnimator = SearchResultsEnterAnimator.this;
                    SearchViewPager.this.currentAnimators.remove(searchResultsEnterAnimator);
                }
            });
            SearchViewPager.this.currentAnimators.add(this);
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$new$0 */
        public /* synthetic */ void lambda$new$0$SearchViewPager$SearchResultsEnterAnimator(ValueAnimator valueAnimator2) {
            this.progress = ((Float) valueAnimator2.getAnimatedValue()).floatValue();
            SearchViewPager.this.searchListView.invalidate();
        }

        public boolean setup(View view, int i) {
            if (this.position != i) {
                return false;
            }
            view.setAlpha(this.progress);
            return true;
        }
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.attached = true;
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.attached = false;
    }
}
