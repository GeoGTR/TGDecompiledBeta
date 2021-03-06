package org.telegram.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import org.telegram.messenger.AccountInstance;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.ImageReceiver;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.browser.Browser;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC$Document;
import org.telegram.tgnet.TLRPC$FileLocation;
import org.telegram.tgnet.TLRPC$MessageMedia;
import org.telegram.tgnet.TLRPC$TL_error;
import org.telegram.tgnet.TLRPC$TL_webPageEmpty;
import org.telegram.tgnet.TLRPC$User;
import org.telegram.tgnet.TLRPC$WebPage;
import org.telegram.tgnet.TLRPC$messages_Messages;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.Adapters.FiltersView;
import org.telegram.ui.Cells.ChatActionCell;
import org.telegram.ui.Cells.ContextLinkCell;
import org.telegram.ui.Cells.DialogCell;
import org.telegram.ui.Cells.GraySectionCell;
import org.telegram.ui.Cells.LoadingCell;
import org.telegram.ui.Cells.ProfileSearchCell;
import org.telegram.ui.Cells.SharedAudioCell;
import org.telegram.ui.Cells.SharedDocumentCell;
import org.telegram.ui.Cells.SharedLinkCell;
import org.telegram.ui.Cells.SharedMediaSectionCell;
import org.telegram.ui.Cells.SharedPhotoVideoCell;
import org.telegram.ui.Components.AlertsCreator;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.EmbedBottomSheet;
import org.telegram.ui.Components.FlickerLoadingView;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.SearchViewPager;
import org.telegram.ui.Components.StickerEmptyView;
import org.telegram.ui.PhotoViewer;

public class FilteredSearchView extends FrameLayout implements NotificationCenter.NotificationCenterDelegate {
    private static SpannableStringBuilder arrowSpan;
    RecyclerView.Adapter adapter;
    /* access modifiers changed from: private */
    public int animationIndex = -1;
    /* access modifiers changed from: private */
    public SearchViewPager.ChatPreviewDelegate chatPreviewDelegate;
    Runnable clearCurrentResultsRunnable = new Runnable() {
        public void run() {
            if (FilteredSearchView.this.isLoading) {
                FilteredSearchView.this.messages.clear();
                FilteredSearchView.this.sections.clear();
                FilteredSearchView.this.sectionArrays.clear();
                RecyclerView.Adapter adapter = FilteredSearchView.this.adapter;
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }
            }
        }
    };
    /* access modifiers changed from: private */
    public int columnsCount = 3;
    /* access modifiers changed from: private */
    public String currentDataQuery;
    int currentSearchDialogId;
    FiltersView.MediaFilterData currentSearchFilter;
    long currentSearchMaxDate;
    long currentSearchMinDate;
    String currentSearchString;
    private Delegate delegate;
    private OnlyUserFiltersAdapter dialogsAdapter;
    StickerEmptyView emptyView;
    /* access modifiers changed from: private */
    public boolean endReached;
    /* access modifiers changed from: private */
    public AnimatorSet floatingDateAnimation;
    /* access modifiers changed from: private */
    public final ChatActionCell floatingDateView;
    private Runnable hideFloatingDateRunnable = new Runnable() {
        public final void run() {
            FilteredSearchView.this.lambda$new$0$FilteredSearchView();
        }
    };
    boolean ignoreRequestLayout;
    /* access modifiers changed from: private */
    public boolean isLoading;
    int lastAccount;
    String lastMessagesSearchString;
    String lastSearchFilterQueryString;
    public final LinearLayoutManager layoutManager;
    private final FlickerLoadingView loadingView;
    ArrayList<TLObject> localTipChats = new ArrayList<>();
    ArrayList<FiltersView.DateData> localTipDates = new ArrayList<>();
    /* access modifiers changed from: private */
    public final MessageHashId messageHashIdTmp = new MessageHashId(0, 0);
    public ArrayList<MessageObject> messages = new ArrayList<>();
    public SparseArray<MessageObject> messagesById = new SparseArray<>();
    /* access modifiers changed from: private */
    public int nextSearchRate;
    Activity parentActivity;
    BaseFragment parentFragment;
    private int photoViewerClassGuid;
    private PhotoViewer.PhotoViewerProvider provider = new PhotoViewer.EmptyPhotoViewerProvider() {
        public int getTotalImageCount() {
            return FilteredSearchView.this.totalCount;
        }

        public boolean loadMore() {
            if (FilteredSearchView.this.endReached) {
                return true;
            }
            FilteredSearchView filteredSearchView = FilteredSearchView.this;
            filteredSearchView.search(filteredSearchView.currentSearchDialogId, filteredSearchView.currentSearchMinDate, filteredSearchView.currentSearchMaxDate, filteredSearchView.currentSearchFilter, filteredSearchView.lastMessagesSearchString, false);
            return true;
        }

        public PhotoViewer.PlaceProviderObject getPlaceForPhoto(MessageObject messageObject, TLRPC$FileLocation tLRPC$FileLocation, int i, boolean z) {
            ImageReceiver imageReceiver;
            View pinnedHeader;
            MessageObject messageObject2;
            if (messageObject == null) {
                return null;
            }
            RecyclerListView recyclerListView = FilteredSearchView.this.recyclerListView;
            int childCount = recyclerListView.getChildCount();
            for (int i2 = 0; i2 < childCount; i2++) {
                View childAt = recyclerListView.getChildAt(i2);
                int[] iArr = new int[2];
                if (childAt instanceof SharedPhotoVideoCell) {
                    SharedPhotoVideoCell sharedPhotoVideoCell = (SharedPhotoVideoCell) childAt;
                    imageReceiver = null;
                    int i3 = 0;
                    while (i3 < 6 && (messageObject2 = sharedPhotoVideoCell.getMessageObject(i3)) != null) {
                        if (messageObject2.getId() == messageObject.getId()) {
                            BackupImageView imageView = sharedPhotoVideoCell.getImageView(i3);
                            ImageReceiver imageReceiver2 = imageView.getImageReceiver();
                            imageView.getLocationInWindow(iArr);
                            imageReceiver = imageReceiver2;
                        }
                        i3++;
                    }
                } else {
                    if (childAt instanceof SharedDocumentCell) {
                        SharedDocumentCell sharedDocumentCell = (SharedDocumentCell) childAt;
                        if (sharedDocumentCell.getMessage().getId() == messageObject.getId()) {
                            BackupImageView imageView2 = sharedDocumentCell.getImageView();
                            ImageReceiver imageReceiver3 = imageView2.getImageReceiver();
                            imageView2.getLocationInWindow(iArr);
                            imageReceiver = imageReceiver3;
                        }
                    } else if (childAt instanceof ContextLinkCell) {
                        ContextLinkCell contextLinkCell = (ContextLinkCell) childAt;
                        MessageObject messageObject3 = (MessageObject) contextLinkCell.getParentObject();
                        if (!(messageObject3 == null || messageObject == null || messageObject3.getId() != messageObject.getId())) {
                            imageReceiver = contextLinkCell.getPhotoImage();
                            contextLinkCell.getLocationInWindow(iArr);
                        }
                    }
                    imageReceiver = null;
                }
                if (imageReceiver != null) {
                    PhotoViewer.PlaceProviderObject placeProviderObject = new PhotoViewer.PlaceProviderObject();
                    placeProviderObject.viewX = iArr[0];
                    placeProviderObject.viewY = iArr[1] - (Build.VERSION.SDK_INT >= 21 ? 0 : AndroidUtilities.statusBarHeight);
                    placeProviderObject.parentView = recyclerListView;
                    recyclerListView.getLocationInWindow(iArr);
                    placeProviderObject.animatingImageViewYOffset = -iArr[1];
                    placeProviderObject.imageReceiver = imageReceiver;
                    placeProviderObject.allowTakeAnimation = false;
                    placeProviderObject.radius = imageReceiver.getRoundRadius();
                    placeProviderObject.thumb = placeProviderObject.imageReceiver.getBitmapSafe();
                    placeProviderObject.parentView.getLocationInWindow(iArr);
                    placeProviderObject.clipTopAddition = 0;
                    if (PhotoViewer.isShowingImage(messageObject) && (pinnedHeader = recyclerListView.getPinnedHeader()) != null) {
                        boolean z2 = childAt instanceof SharedDocumentCell;
                        int dp = (z2 ? AndroidUtilities.dp(8.0f) + 0 : 0) - placeProviderObject.viewY;
                        if (dp > childAt.getHeight()) {
                            recyclerListView.scrollBy(0, -(dp + pinnedHeader.getHeight()));
                        } else {
                            int height = placeProviderObject.viewY - recyclerListView.getHeight();
                            if (z2) {
                                height -= AndroidUtilities.dp(8.0f);
                            }
                            if (height >= 0) {
                                recyclerListView.scrollBy(0, height + childAt.getHeight());
                            }
                        }
                    }
                    return placeProviderObject;
                }
            }
            return null;
        }

        public CharSequence getTitleFor(int i) {
            return FilteredSearchView.createFromInfoString(FilteredSearchView.this.messages.get(i));
        }

        public CharSequence getSubtitleFor(int i) {
            return LocaleController.formatDateAudio((long) FilteredSearchView.this.messages.get(i).messageOwner.date, false);
        }
    };
    public RecyclerListView recyclerListView;
    private int requestIndex;
    Runnable searchRunnable;
    public HashMap<String, ArrayList<MessageObject>> sectionArrays = new HashMap<>();
    public ArrayList<String> sections = new ArrayList<>();
    private SharedDocumentsAdapter sharedAudioAdapter;
    private SharedDocumentsAdapter sharedDocumentsAdapter;
    private SharedLinksAdapter sharedLinksAdapter;
    /* access modifiers changed from: private */
    public SharedPhotoVideoAdapter sharedPhotoVideoAdapter;
    private SharedDocumentsAdapter sharedVoiceAdapter;
    /* access modifiers changed from: private */
    public int totalCount;
    /* access modifiers changed from: private */
    public UiCallback uiCallback;

    public interface Delegate {
        void updateFiltersView(boolean z, ArrayList<TLObject> arrayList, ArrayList<FiltersView.DateData> arrayList2);
    }

    public interface UiCallback {
        boolean actionModeShowing();

        int getFolderId();

        void goToMessage(MessageObject messageObject);

        boolean isSelected(MessageHashId messageHashId);

        void showActionMode();

        void toggleItemSelection(MessageObject messageObject, View view, int i);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$0 */
    public /* synthetic */ void lambda$new$0$FilteredSearchView() {
        hideFloatingDateView(true);
    }

    public FilteredSearchView(BaseFragment baseFragment) {
        super(baseFragment.getParentActivity());
        this.parentFragment = baseFragment;
        Activity parentActivity2 = baseFragment.getParentActivity();
        this.parentActivity = parentActivity2;
        setBackgroundColor(Theme.getColor("windowBackgroundWhite"));
        AnonymousClass3 r0 = new RecyclerListView(parentActivity2) {
            /* access modifiers changed from: protected */
            public void dispatchDraw(Canvas canvas) {
                if (getAdapter() == FilteredSearchView.this.sharedPhotoVideoAdapter) {
                    for (int i = 0; i < getChildCount(); i++) {
                        if (getChildViewHolder(getChildAt(i)).getItemViewType() == 1) {
                            canvas.save();
                            canvas.translate(getChildAt(i).getX(), (getChildAt(i).getY() - ((float) getChildAt(i).getMeasuredHeight())) + ((float) AndroidUtilities.dp(2.0f)));
                            getChildAt(i).draw(canvas);
                            canvas.restore();
                            invalidate();
                        }
                    }
                }
                super.dispatchDraw(canvas);
            }

            public boolean drawChild(Canvas canvas, View view, long j) {
                if (getAdapter() == FilteredSearchView.this.sharedPhotoVideoAdapter && getChildViewHolder(view).getItemViewType() == 1) {
                    return true;
                }
                return super.drawChild(canvas, view, j);
            }
        };
        this.recyclerListView = r0;
        r0.setOnItemClickListener((RecyclerListView.OnItemClickListener) new RecyclerListView.OnItemClickListener() {
            public final void onItemClick(View view, int i) {
                FilteredSearchView.this.lambda$new$1$FilteredSearchView(view, i);
            }
        });
        this.recyclerListView.setOnItemLongClickListener((RecyclerListView.OnItemLongClickListenerExtended) new RecyclerListView.OnItemLongClickListenerExtended() {
            public boolean onItemClick(View view, int i, float f, float f2) {
                if (view instanceof SharedDocumentCell) {
                    boolean unused = FilteredSearchView.this.onItemLongClick(((SharedDocumentCell) view).getMessage(), view, 0);
                } else if (view instanceof SharedLinkCell) {
                    boolean unused2 = FilteredSearchView.this.onItemLongClick(((SharedLinkCell) view).getMessage(), view, 0);
                } else if (view instanceof SharedAudioCell) {
                    boolean unused3 = FilteredSearchView.this.onItemLongClick(((SharedAudioCell) view).getMessage(), view, 0);
                } else if (view instanceof ContextLinkCell) {
                    boolean unused4 = FilteredSearchView.this.onItemLongClick(((ContextLinkCell) view).getMessageObject(), view, 0);
                } else if (view instanceof DialogCell) {
                    if (!FilteredSearchView.this.uiCallback.actionModeShowing()) {
                        DialogCell dialogCell = (DialogCell) view;
                        if (dialogCell.isPointInsideAvatar(f, f2)) {
                            FilteredSearchView.this.chatPreviewDelegate.startChatPreview(dialogCell);
                            return true;
                        }
                    }
                    boolean unused5 = FilteredSearchView.this.onItemLongClick(((DialogCell) view).getMessage(), view, 0);
                }
                return true;
            }

            public void onMove(float f, float f2) {
                FilteredSearchView.this.chatPreviewDelegate.move(f2);
            }

            public void onLongClickRelease() {
                FilteredSearchView.this.chatPreviewDelegate.finish();
            }
        });
        this.recyclerListView.setPadding(0, 0, 0, AndroidUtilities.dp(3.0f));
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(parentActivity2);
        this.layoutManager = linearLayoutManager;
        this.recyclerListView.setLayoutManager(linearLayoutManager);
        AnonymousClass5 r02 = new FlickerLoadingView(parentActivity2) {
            public int getColumnsCount() {
                return FilteredSearchView.this.columnsCount;
            }
        };
        this.loadingView = r02;
        addView(r02);
        addView(this.recyclerListView);
        this.recyclerListView.setSectionsType(2);
        this.recyclerListView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            public void onScrollStateChanged(RecyclerView recyclerView, int i) {
                if (i == 1) {
                    AndroidUtilities.hideKeyboard(FilteredSearchView.this.parentActivity.getCurrentFocus());
                }
            }

            public void onScrolled(RecyclerView recyclerView, int i, int i2) {
                MessageObject messageObject;
                if (recyclerView.getAdapter() != null) {
                    FilteredSearchView filteredSearchView = FilteredSearchView.this;
                    if (filteredSearchView.adapter != null) {
                        int findFirstVisibleItemPosition = filteredSearchView.layoutManager.findFirstVisibleItemPosition();
                        int findLastVisibleItemPosition = FilteredSearchView.this.layoutManager.findLastVisibleItemPosition();
                        int abs = Math.abs(findLastVisibleItemPosition - findFirstVisibleItemPosition) + 1;
                        int itemCount = recyclerView.getAdapter().getItemCount();
                        if (!FilteredSearchView.this.isLoading && abs > 0 && findLastVisibleItemPosition >= itemCount - 10 && !FilteredSearchView.this.endReached) {
                            FilteredSearchView filteredSearchView2 = FilteredSearchView.this;
                            filteredSearchView2.search(filteredSearchView2.currentSearchDialogId, filteredSearchView2.currentSearchMinDate, filteredSearchView2.currentSearchMaxDate, filteredSearchView2.currentSearchFilter, filteredSearchView2.lastMessagesSearchString, false);
                        }
                        FilteredSearchView filteredSearchView3 = FilteredSearchView.this;
                        if (filteredSearchView3.adapter == filteredSearchView3.sharedPhotoVideoAdapter) {
                            if (i2 != 0 && !FilteredSearchView.this.messages.isEmpty() && TextUtils.isEmpty(FilteredSearchView.this.currentDataQuery)) {
                                FilteredSearchView.this.showFloatingDateView();
                            }
                            RecyclerView.ViewHolder findViewHolderForAdapterPosition = recyclerView.findViewHolderForAdapterPosition(findFirstVisibleItemPosition);
                            if (findViewHolderForAdapterPosition != null && findViewHolderForAdapterPosition.getItemViewType() == 0) {
                                View view = findViewHolderForAdapterPosition.itemView;
                                if ((view instanceof SharedPhotoVideoCell) && (messageObject = ((SharedPhotoVideoCell) view).getMessageObject(0)) != null) {
                                    FilteredSearchView.this.floatingDateView.setCustomDate(messageObject.messageOwner.date, false, true);
                                }
                            }
                        }
                    }
                }
            }
        });
        ChatActionCell chatActionCell = new ChatActionCell(parentActivity2);
        this.floatingDateView = chatActionCell;
        chatActionCell.setCustomDate((int) (System.currentTimeMillis() / 1000), false, false);
        chatActionCell.setAlpha(0.0f);
        chatActionCell.setOverrideColor("chat_mediaTimeBackground", "chat_mediaTimeText");
        chatActionCell.setTranslationY((float) (-AndroidUtilities.dp(48.0f)));
        addView(chatActionCell, LayoutHelper.createFrame(-2, -2.0f, 49, 0.0f, 4.0f, 0.0f, 0.0f));
        this.dialogsAdapter = new OnlyUserFiltersAdapter();
        this.sharedPhotoVideoAdapter = new SharedPhotoVideoAdapter(getContext());
        this.sharedDocumentsAdapter = new SharedDocumentsAdapter(getContext(), 1);
        this.sharedLinksAdapter = new SharedLinksAdapter(getContext());
        this.sharedAudioAdapter = new SharedDocumentsAdapter(getContext(), 4);
        this.sharedVoiceAdapter = new SharedDocumentsAdapter(getContext(), 2);
        StickerEmptyView stickerEmptyView = new StickerEmptyView(parentActivity2, r02, 1);
        this.emptyView = stickerEmptyView;
        addView(stickerEmptyView);
        this.recyclerListView.setEmptyView(this.emptyView);
        this.emptyView.setVisibility(8);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$1 */
    public /* synthetic */ void lambda$new$1$FilteredSearchView(View view, int i) {
        if (view instanceof SharedDocumentCell) {
            onItemClick(i, view, ((SharedDocumentCell) view).getMessage(), 0);
        } else if (view instanceof SharedLinkCell) {
            onItemClick(i, view, ((SharedLinkCell) view).getMessage(), 0);
        } else if (view instanceof SharedAudioCell) {
            onItemClick(i, view, ((SharedAudioCell) view).getMessage(), 0);
        } else if (view instanceof ContextLinkCell) {
            onItemClick(i, view, ((ContextLinkCell) view).getMessageObject(), 0);
        } else if (view instanceof DialogCell) {
            onItemClick(i, view, ((DialogCell) view).getMessage(), 0);
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v0, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v1, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v5, resolved type: android.text.SpannableStringBuilder} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v8, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v9, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v10, resolved type: java.lang.String} */
    /* JADX WARNING: Multi-variable type inference failed */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static java.lang.CharSequence createFromInfoString(org.telegram.messenger.MessageObject r5) {
        /*
            android.text.SpannableStringBuilder r0 = arrowSpan
            if (r0 != 0) goto L_0x0024
            android.text.SpannableStringBuilder r0 = new android.text.SpannableStringBuilder
            java.lang.String r1 = "-"
            r0.<init>(r1)
            arrowSpan = r0
            org.telegram.ui.Components.ColoredImageSpan r1 = new org.telegram.ui.Components.ColoredImageSpan
            android.content.Context r2 = org.telegram.messenger.ApplicationLoader.applicationContext
            r3 = 2131165973(0x7var_, float:1.7946178E38)
            android.graphics.drawable.Drawable r2 = androidx.core.content.ContextCompat.getDrawable(r2, r3)
            android.graphics.drawable.Drawable r2 = r2.mutate()
            r1.<init>(r2)
            r2 = 1
            r3 = 0
            r0.setSpan(r1, r3, r2, r3)
        L_0x0024:
            org.telegram.tgnet.TLRPC$Message r0 = r5.messageOwner
            org.telegram.tgnet.TLRPC$Peer r0 = r0.from_id
            int r0 = r0.user_id
            r1 = 0
            if (r0 == 0) goto L_0x0042
            int r0 = org.telegram.messenger.UserConfig.selectedAccount
            org.telegram.messenger.MessagesController r0 = org.telegram.messenger.MessagesController.getInstance(r0)
            org.telegram.tgnet.TLRPC$Message r2 = r5.messageOwner
            org.telegram.tgnet.TLRPC$Peer r2 = r2.from_id
            int r2 = r2.user_id
            java.lang.Integer r2 = java.lang.Integer.valueOf(r2)
            org.telegram.tgnet.TLRPC$User r0 = r0.getUser(r2)
            goto L_0x0043
        L_0x0042:
            r0 = r1
        L_0x0043:
            org.telegram.tgnet.TLRPC$Message r2 = r5.messageOwner
            org.telegram.tgnet.TLRPC$Peer r2 = r2.from_id
            int r2 = r2.chat_id
            if (r2 == 0) goto L_0x0060
            int r2 = org.telegram.messenger.UserConfig.selectedAccount
            org.telegram.messenger.MessagesController r2 = org.telegram.messenger.MessagesController.getInstance(r2)
            org.telegram.tgnet.TLRPC$Message r3 = r5.messageOwner
            org.telegram.tgnet.TLRPC$Peer r3 = r3.peer_id
            int r3 = r3.chat_id
            java.lang.Integer r3 = java.lang.Integer.valueOf(r3)
            org.telegram.tgnet.TLRPC$Chat r2 = r2.getChat(r3)
            goto L_0x0061
        L_0x0060:
            r2 = r1
        L_0x0061:
            if (r2 != 0) goto L_0x0081
            org.telegram.tgnet.TLRPC$Message r2 = r5.messageOwner
            org.telegram.tgnet.TLRPC$Peer r2 = r2.from_id
            int r2 = r2.channel_id
            if (r2 == 0) goto L_0x0080
            int r2 = org.telegram.messenger.UserConfig.selectedAccount
            org.telegram.messenger.MessagesController r2 = org.telegram.messenger.MessagesController.getInstance(r2)
            org.telegram.tgnet.TLRPC$Message r3 = r5.messageOwner
            org.telegram.tgnet.TLRPC$Peer r3 = r3.peer_id
            int r3 = r3.channel_id
            java.lang.Integer r3 = java.lang.Integer.valueOf(r3)
            org.telegram.tgnet.TLRPC$Chat r2 = r2.getChat(r3)
            goto L_0x0081
        L_0x0080:
            r2 = r1
        L_0x0081:
            org.telegram.tgnet.TLRPC$Message r3 = r5.messageOwner
            org.telegram.tgnet.TLRPC$Peer r3 = r3.peer_id
            int r3 = r3.channel_id
            if (r3 == 0) goto L_0x009e
            int r3 = org.telegram.messenger.UserConfig.selectedAccount
            org.telegram.messenger.MessagesController r3 = org.telegram.messenger.MessagesController.getInstance(r3)
            org.telegram.tgnet.TLRPC$Message r4 = r5.messageOwner
            org.telegram.tgnet.TLRPC$Peer r4 = r4.peer_id
            int r4 = r4.channel_id
            java.lang.Integer r4 = java.lang.Integer.valueOf(r4)
            org.telegram.tgnet.TLRPC$Chat r3 = r3.getChat(r4)
            goto L_0x009f
        L_0x009e:
            r3 = r1
        L_0x009f:
            if (r3 != 0) goto L_0x00c0
            org.telegram.tgnet.TLRPC$Message r3 = r5.messageOwner
            org.telegram.tgnet.TLRPC$Peer r3 = r3.peer_id
            int r3 = r3.chat_id
            if (r3 == 0) goto L_0x00bf
            int r3 = org.telegram.messenger.UserConfig.selectedAccount
            org.telegram.messenger.MessagesController r3 = org.telegram.messenger.MessagesController.getInstance(r3)
            org.telegram.tgnet.TLRPC$Message r5 = r5.messageOwner
            org.telegram.tgnet.TLRPC$Peer r5 = r5.peer_id
            int r5 = r5.chat_id
            java.lang.Integer r5 = java.lang.Integer.valueOf(r5)
            org.telegram.tgnet.TLRPC$Chat r5 = r3.getChat(r5)
            r3 = r5
            goto L_0x00c0
        L_0x00bf:
            r3 = r1
        L_0x00c0:
            if (r0 == 0) goto L_0x00eb
            if (r3 == 0) goto L_0x00eb
            android.text.SpannableStringBuilder r1 = new android.text.SpannableStringBuilder
            r1.<init>()
            java.lang.String r5 = r0.first_name
            java.lang.String r0 = r0.last_name
            java.lang.String r5 = org.telegram.messenger.ContactsController.formatName(r5, r0)
            android.text.SpannableStringBuilder r5 = r1.append(r5)
            r0 = 32
            android.text.SpannableStringBuilder r5 = r5.append(r0)
            android.text.SpannableStringBuilder r2 = arrowSpan
            android.text.SpannableStringBuilder r5 = r5.append(r2)
            android.text.SpannableStringBuilder r5 = r5.append(r0)
            java.lang.String r0 = r3.title
            r5.append(r0)
            goto L_0x00fa
        L_0x00eb:
            if (r0 == 0) goto L_0x00f6
            java.lang.String r5 = r0.first_name
            java.lang.String r0 = r0.last_name
            java.lang.String r1 = org.telegram.messenger.ContactsController.formatName(r5, r0)
            goto L_0x00fa
        L_0x00f6:
            if (r2 == 0) goto L_0x00fa
            java.lang.String r1 = r2.title
        L_0x00fa:
            if (r1 != 0) goto L_0x00fe
            java.lang.String r1 = ""
        L_0x00fe:
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.FilteredSearchView.createFromInfoString(org.telegram.messenger.MessageObject):java.lang.CharSequence");
    }

    public void search(int i, long j, long j2, FiltersView.MediaFilterData mediaFilterData, String str, boolean z) {
        int i2 = i;
        long j3 = j;
        long j4 = j2;
        FiltersView.MediaFilterData mediaFilterData2 = mediaFilterData;
        String str2 = str;
        Locale locale = Locale.ENGLISH;
        Object[] objArr = new Object[5];
        objArr[0] = Integer.valueOf(i);
        objArr[1] = Long.valueOf(j);
        objArr[2] = Long.valueOf(j2);
        objArr[3] = Integer.valueOf(mediaFilterData2 == null ? -1 : mediaFilterData2.filterType);
        objArr[4] = str2;
        String format = String.format(locale, "%d%d%d%d%s", objArr);
        String str3 = this.lastSearchFilterQueryString;
        boolean z2 = str3 != null && str3.equals(format);
        boolean z3 = !z2 && z;
        if (i2 == this.currentSearchDialogId && this.currentSearchMinDate == j3) {
            int i3 = (this.currentSearchMaxDate > j4 ? 1 : (this.currentSearchMaxDate == j4 ? 0 : -1));
        }
        this.currentSearchFilter = mediaFilterData2;
        this.currentSearchDialogId = i2;
        this.currentSearchMinDate = j3;
        this.currentSearchMaxDate = j4;
        this.currentSearchString = str2;
        Runnable runnable = this.searchRunnable;
        if (runnable != null) {
            AndroidUtilities.cancelRunOnUIThread(runnable);
        }
        AndroidUtilities.cancelRunOnUIThread(this.clearCurrentResultsRunnable);
        if (!z2 || !z) {
            long j5 = 0;
            if (z3 || (mediaFilterData2 == null && i2 == 0 && j3 == 0 && j4 == 0)) {
                this.messages.clear();
                this.sections.clear();
                this.sectionArrays.clear();
                this.isLoading = true;
                this.emptyView.setVisibility(0);
                RecyclerView.Adapter adapter2 = this.adapter;
                if (adapter2 != null) {
                    adapter2.notifyDataSetChanged();
                }
                this.requestIndex++;
                if (this.recyclerListView.getPinnedHeader() != null) {
                    this.recyclerListView.getPinnedHeader().setAlpha(0.0f);
                }
                this.localTipChats.clear();
                this.localTipDates.clear();
                if (!z3) {
                    return;
                }
            } else if (z && !this.messages.isEmpty()) {
                return;
            }
            this.isLoading = true;
            RecyclerView.Adapter adapter3 = this.adapter;
            if (adapter3 != null) {
                adapter3.notifyDataSetChanged();
            }
            if (!z2) {
                this.clearCurrentResultsRunnable.run();
                this.emptyView.showProgress(true, !z);
            }
            if (TextUtils.isEmpty(str)) {
                this.localTipDates.clear();
                this.localTipChats.clear();
                Delegate delegate2 = this.delegate;
                if (delegate2 != null) {
                    delegate2.updateFiltersView(false, (ArrayList<TLObject>) null, (ArrayList<FiltersView.DateData>) null);
                }
            }
            int i4 = this.requestIndex + 1;
            this.requestIndex = i4;
            $$Lambda$FilteredSearchView$zovqrbVqIgZNAl4sFfDpP8XURw r15 = r0;
            $$Lambda$FilteredSearchView$zovqrbVqIgZNAl4sFfDpP8XURw r0 = new Runnable(i, str, mediaFilterData, UserConfig.selectedAccount, j, j2, z2, this.uiCallback.getFolderId(), format, i4) {
                public final /* synthetic */ int f$1;
                public final /* synthetic */ int f$10;
                public final /* synthetic */ String f$2;
                public final /* synthetic */ FiltersView.MediaFilterData f$3;
                public final /* synthetic */ int f$4;
                public final /* synthetic */ long f$5;
                public final /* synthetic */ long f$6;
                public final /* synthetic */ boolean f$7;
                public final /* synthetic */ int f$8;
                public final /* synthetic */ String f$9;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                    this.f$3 = r4;
                    this.f$4 = r5;
                    this.f$5 = r6;
                    this.f$6 = r8;
                    this.f$7 = r10;
                    this.f$8 = r11;
                    this.f$9 = r12;
                    this.f$10 = r13;
                }

                public final void run() {
                    FilteredSearchView.this.lambda$search$4$FilteredSearchView(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6, this.f$7, this.f$8, this.f$9, this.f$10);
                }
            };
            this.searchRunnable = r15;
            if (!z2 || this.messages.isEmpty()) {
                j5 = 350;
            }
            AndroidUtilities.runOnUIThread(r15, j5);
            FiltersView.MediaFilterData mediaFilterData3 = mediaFilterData;
            if (mediaFilterData3 == null) {
                this.loadingView.setViewType(1);
                return;
            }
            int i5 = mediaFilterData3.filterType;
            if (i5 == 0) {
                if (!TextUtils.isEmpty(this.currentSearchString)) {
                    this.loadingView.setViewType(1);
                } else {
                    this.loadingView.setViewType(2);
                }
            } else if (i5 == 1) {
                this.loadingView.setViewType(3);
            } else if (i5 == 3 || i5 == 5) {
                this.loadingView.setViewType(4);
            } else if (i5 == 2) {
                this.loadingView.setViewType(5);
            }
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v3, resolved type: org.telegram.tgnet.TLRPC$TL_messages_searchGlobal} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v5, resolved type: org.telegram.tgnet.TLRPC$TL_messages_search} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v6, resolved type: org.telegram.tgnet.TLRPC$TL_messages_searchGlobal} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v7, resolved type: org.telegram.tgnet.TLRPC$TL_messages_searchGlobal} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v8, resolved type: org.telegram.tgnet.TLRPC$TL_messages_searchGlobal} */
    /* access modifiers changed from: private */
    /* JADX WARNING: Multi-variable type inference failed */
    /* renamed from: lambda$search$4 */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public /* synthetic */ void lambda$search$4$FilteredSearchView(int r19, java.lang.String r20, org.telegram.ui.Adapters.FiltersView.MediaFilterData r21, int r22, long r23, long r25, boolean r27, int r28, java.lang.String r29, int r30) {
        /*
            r18 = this;
            r12 = r18
            r7 = r19
            r8 = r20
            r9 = r21
            r10 = 20
            r11 = 0
            r13 = 1000(0x3e8, double:4.94E-321)
            r15 = 0
            r0 = 0
            if (r7 == 0) goto L_0x0074
            org.telegram.tgnet.TLRPC$TL_messages_search r1 = new org.telegram.tgnet.TLRPC$TL_messages_search
            r1.<init>()
            r1.q = r8
            r1.limit = r10
            if (r9 != 0) goto L_0x0023
            org.telegram.tgnet.TLRPC$TL_inputMessagesFilterEmpty r2 = new org.telegram.tgnet.TLRPC$TL_inputMessagesFilterEmpty
            r2.<init>()
            goto L_0x0025
        L_0x0023:
            org.telegram.tgnet.TLRPC$MessagesFilter r2 = r9.filter
        L_0x0025:
            r1.filter = r2
            org.telegram.messenger.AccountInstance r2 = org.telegram.messenger.AccountInstance.getInstance(r22)
            org.telegram.messenger.MessagesController r2 = r2.getMessagesController()
            org.telegram.tgnet.TLRPC$InputPeer r2 = r2.getInputPeer((int) r7)
            r1.peer = r2
            int r2 = (r23 > r15 ? 1 : (r23 == r15 ? 0 : -1))
            if (r2 <= 0) goto L_0x003e
            long r2 = r23 / r13
            int r3 = (int) r2
            r1.min_date = r3
        L_0x003e:
            int r2 = (r25 > r15 ? 1 : (r25 == r15 ? 0 : -1))
            if (r2 <= 0) goto L_0x0047
            long r2 = r25 / r13
            int r3 = (int) r2
            r1.max_date = r3
        L_0x0047:
            if (r27 == 0) goto L_0x006e
            java.lang.String r2 = r12.lastMessagesSearchString
            boolean r2 = r8.equals(r2)
            if (r2 == 0) goto L_0x006e
            java.util.ArrayList<org.telegram.messenger.MessageObject> r2 = r12.messages
            boolean r2 = r2.isEmpty()
            if (r2 != 0) goto L_0x006e
            java.util.ArrayList<org.telegram.messenger.MessageObject> r2 = r12.messages
            int r3 = r2.size()
            int r3 = r3 + -1
            java.lang.Object r2 = r2.get(r3)
            org.telegram.messenger.MessageObject r2 = (org.telegram.messenger.MessageObject) r2
            int r2 = r2.getId()
            r1.offset_id = r2
            goto L_0x0070
        L_0x006e:
            r1.offset_id = r11
        L_0x0070:
            r10 = r0
            r13 = r1
            goto L_0x0121
        L_0x0074:
            boolean r1 = android.text.TextUtils.isEmpty(r20)
            if (r1 != 0) goto L_0x0099
            java.util.ArrayList r17 = new java.util.ArrayList
            r17.<init>()
            java.util.ArrayList r4 = new java.util.ArrayList
            r4.<init>()
            java.util.ArrayList r5 = new java.util.ArrayList
            r5.<init>()
            org.telegram.messenger.MessagesStorage r0 = org.telegram.messenger.MessagesStorage.getInstance(r22)
            r1 = 0
            r2 = r20
            r3 = r17
            r6 = r28
            r0.localSearch(r1, r2, r3, r4, r5, r6)
            r0 = r17
        L_0x0099:
            org.telegram.tgnet.TLRPC$TL_messages_searchGlobal r1 = new org.telegram.tgnet.TLRPC$TL_messages_searchGlobal
            r1.<init>()
            r1.limit = r10
            r1.q = r8
            if (r9 != 0) goto L_0x00aa
            org.telegram.tgnet.TLRPC$TL_inputMessagesFilterEmpty r2 = new org.telegram.tgnet.TLRPC$TL_inputMessagesFilterEmpty
            r2.<init>()
            goto L_0x00ac
        L_0x00aa:
            org.telegram.tgnet.TLRPC$MessagesFilter r2 = r9.filter
        L_0x00ac:
            r1.filter = r2
            int r2 = (r23 > r15 ? 1 : (r23 == r15 ? 0 : -1))
            if (r2 <= 0) goto L_0x00b7
            long r2 = r23 / r13
            int r3 = (int) r2
            r1.min_date = r3
        L_0x00b7:
            int r2 = (r25 > r15 ? 1 : (r25 == r15 ? 0 : -1))
            if (r2 <= 0) goto L_0x00c0
            long r2 = r25 / r13
            int r3 = (int) r2
            r1.max_date = r3
        L_0x00c0:
            if (r27 == 0) goto L_0x0106
            java.lang.String r2 = r12.lastMessagesSearchString
            boolean r2 = r8.equals(r2)
            if (r2 == 0) goto L_0x0106
            java.util.ArrayList<org.telegram.messenger.MessageObject> r2 = r12.messages
            boolean r2 = r2.isEmpty()
            if (r2 != 0) goto L_0x0106
            java.util.ArrayList<org.telegram.messenger.MessageObject> r2 = r12.messages
            int r3 = r2.size()
            int r3 = r3 + -1
            java.lang.Object r2 = r2.get(r3)
            org.telegram.messenger.MessageObject r2 = (org.telegram.messenger.MessageObject) r2
            int r3 = r2.getId()
            r1.offset_id = r3
            int r3 = r12.nextSearchRate
            r1.offset_rate = r3
            org.telegram.tgnet.TLRPC$Message r2 = r2.messageOwner
            org.telegram.tgnet.TLRPC$Peer r2 = r2.peer_id
            int r3 = r2.channel_id
            if (r3 == 0) goto L_0x00f4
        L_0x00f2:
            int r2 = -r3
            goto L_0x00fb
        L_0x00f4:
            int r3 = r2.chat_id
            if (r3 == 0) goto L_0x00f9
            goto L_0x00f2
        L_0x00f9:
            int r2 = r2.user_id
        L_0x00fb:
            org.telegram.messenger.MessagesController r3 = org.telegram.messenger.MessagesController.getInstance(r22)
            org.telegram.tgnet.TLRPC$InputPeer r2 = r3.getInputPeer((int) r2)
            r1.offset_peer = r2
            goto L_0x0111
        L_0x0106:
            r1.offset_rate = r11
            r1.offset_id = r11
            org.telegram.tgnet.TLRPC$TL_inputPeerEmpty r2 = new org.telegram.tgnet.TLRPC$TL_inputPeerEmpty
            r2.<init>()
            r1.offset_peer = r2
        L_0x0111:
            int r2 = r1.flags
            r2 = r2 | 1
            r1.flags = r2
            org.telegram.ui.FilteredSearchView$UiCallback r2 = r12.uiCallback
            int r2 = r2.getFolderId()
            r1.folder_id = r2
            goto L_0x0070
        L_0x0121:
            r12.lastMessagesSearchString = r8
            r0 = r29
            r12.lastSearchFilterQueryString = r0
            java.util.ArrayList r11 = new java.util.ArrayList
            r11.<init>()
            java.lang.String r0 = r12.lastMessagesSearchString
            org.telegram.ui.Adapters.FiltersView.fillTipDates(r0, r11)
            org.telegram.tgnet.ConnectionsManager r14 = org.telegram.tgnet.ConnectionsManager.getInstance(r22)
            org.telegram.ui.-$$Lambda$FilteredSearchView$Pvh05aBp-yWsFCpaPRh1Rb15fOg r15 = new org.telegram.ui.-$$Lambda$FilteredSearchView$Pvh05aBp-yWsFCpaPRh1Rb15fOg
            r0 = r15
            r1 = r18
            r2 = r22
            r3 = r20
            r4 = r30
            r5 = r27
            r6 = r21
            r7 = r19
            r8 = r23
            r0.<init>(r2, r3, r4, r5, r6, r7, r8, r10, r11)
            r14.sendRequest(r13, r15)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.FilteredSearchView.lambda$search$4$FilteredSearchView(int, java.lang.String, org.telegram.ui.Adapters.FiltersView$MediaFilterData, int, long, long, boolean, int, java.lang.String, int):void");
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$null$3 */
    public /* synthetic */ void lambda$null$3$FilteredSearchView(int i, String str, int i2, boolean z, FiltersView.MediaFilterData mediaFilterData, int i3, long j, ArrayList arrayList, ArrayList arrayList2, TLObject tLObject, TLRPC$TL_error tLRPC$TL_error) {
        ArrayList arrayList3 = new ArrayList();
        if (tLRPC$TL_error == null) {
            TLRPC$messages_Messages tLRPC$messages_Messages = (TLRPC$messages_Messages) tLObject;
            int size = tLRPC$messages_Messages.messages.size();
            for (int i4 = 0; i4 < size; i4++) {
                MessageObject messageObject = new MessageObject(i, tLRPC$messages_Messages.messages.get(i4), false, true);
                messageObject.setQuery(str);
                arrayList3.add(messageObject);
            }
        }
        int i5 = i;
        String str2 = str;
        AndroidUtilities.runOnUIThread(new Runnable(i2, tLRPC$TL_error, tLObject, i, z, str, arrayList3, mediaFilterData, i3, j, arrayList, arrayList2) {
            public final /* synthetic */ int f$1;
            public final /* synthetic */ long f$10;
            public final /* synthetic */ ArrayList f$11;
            public final /* synthetic */ ArrayList f$12;
            public final /* synthetic */ TLRPC$TL_error f$2;
            public final /* synthetic */ TLObject f$3;
            public final /* synthetic */ int f$4;
            public final /* synthetic */ boolean f$5;
            public final /* synthetic */ String f$6;
            public final /* synthetic */ ArrayList f$7;
            public final /* synthetic */ FiltersView.MediaFilterData f$8;
            public final /* synthetic */ int f$9;

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
                this.f$10 = r11;
                this.f$11 = r13;
                this.f$12 = r14;
            }

            public final void run() {
                FilteredSearchView.this.lambda$null$2$FilteredSearchView(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6, this.f$7, this.f$8, this.f$9, this.f$10, this.f$11, this.f$12);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$null$2 */
    public /* synthetic */ void lambda$null$2$FilteredSearchView(int i, TLRPC$TL_error tLRPC$TL_error, TLObject tLObject, int i2, boolean z, String str, ArrayList arrayList, FiltersView.MediaFilterData mediaFilterData, int i3, long j, ArrayList arrayList2, ArrayList arrayList3) {
        String str2;
        String str3 = str;
        FiltersView.MediaFilterData mediaFilterData2 = mediaFilterData;
        ArrayList arrayList4 = arrayList2;
        if (i == this.requestIndex) {
            this.isLoading = false;
            boolean z2 = true;
            if (tLRPC$TL_error != null) {
                this.emptyView.title.setText(LocaleController.getString("SearchEmptyViewTitle2", NUM));
                this.emptyView.subtitle.setVisibility(0);
                this.emptyView.subtitle.setText(LocaleController.getString("SearchEmptyViewFilteredSubtitle2", NUM));
                this.emptyView.showProgress(false, true);
                return;
            }
            this.emptyView.showProgress(false);
            TLRPC$messages_Messages tLRPC$messages_Messages = (TLRPC$messages_Messages) tLObject;
            this.nextSearchRate = tLRPC$messages_Messages.next_rate;
            MessagesStorage.getInstance(i2).putUsersAndChats(tLRPC$messages_Messages.users, tLRPC$messages_Messages.chats, true, true);
            MessagesController.getInstance(i2).putUsers(tLRPC$messages_Messages.users, false);
            MessagesController.getInstance(i2).putChats(tLRPC$messages_Messages.chats, false);
            if (!z) {
                this.messages.clear();
                this.messagesById.clear();
                this.sections.clear();
                this.sectionArrays.clear();
            }
            this.totalCount = tLRPC$messages_Messages.count;
            this.currentDataQuery = str3;
            int size = arrayList.size();
            for (int i4 = 0; i4 < size; i4++) {
                MessageObject messageObject = (MessageObject) arrayList.get(i4);
                ArrayList arrayList5 = this.sectionArrays.get(messageObject.monthKey);
                if (arrayList5 == null) {
                    arrayList5 = new ArrayList();
                    this.sectionArrays.put(messageObject.monthKey, arrayList5);
                    this.sections.add(messageObject.monthKey);
                }
                arrayList5.add(messageObject);
                this.messages.add(messageObject);
                this.messagesById.put(messageObject.getId(), messageObject);
                if (PhotoViewer.getInstance().isVisible()) {
                    PhotoViewer.getInstance().addPhoto(messageObject, this.photoViewerClassGuid);
                }
            }
            if (this.messages.size() > this.totalCount) {
                this.totalCount = this.messages.size();
            }
            int size2 = this.messages.size();
            this.endReached = this.messages.size() >= this.totalCount;
            if (this.messages.isEmpty()) {
                if (mediaFilterData2 == null) {
                    this.emptyView.title.setText(LocaleController.getString("SearchEmptyViewTitle2", NUM));
                    this.emptyView.subtitle.setVisibility(8);
                } else if (TextUtils.isEmpty(this.currentDataQuery) && i3 == 0 && j == 0) {
                    this.emptyView.title.setText(LocaleController.getString("SearchEmptyViewTitle", NUM));
                    if (i3 == 0 && j == 0) {
                        int i5 = mediaFilterData2.filterType;
                        if (i5 == 1) {
                            str2 = LocaleController.getString("SearchEmptyViewFilteredSubtitleFiles", NUM);
                        } else if (i5 == 0) {
                            str2 = LocaleController.getString("SearchEmptyViewFilteredSubtitleMedia", NUM);
                        } else if (i5 == 2) {
                            str2 = LocaleController.getString("SearchEmptyViewFilteredSubtitleLinks", NUM);
                        } else if (i5 == 3) {
                            str2 = LocaleController.getString("SearchEmptyViewFilteredSubtitleMusic", NUM);
                        } else {
                            str2 = LocaleController.getString("SearchEmptyViewFilteredSubtitleVoice", NUM);
                        }
                        this.emptyView.subtitle.setVisibility(0);
                        this.emptyView.subtitle.setText(str2);
                    } else {
                        this.emptyView.subtitle.setVisibility(8);
                    }
                } else {
                    this.emptyView.title.setText(LocaleController.getString("SearchEmptyViewTitle2", NUM));
                    this.emptyView.subtitle.setVisibility(0);
                    this.emptyView.subtitle.setText(LocaleController.getString("SearchEmptyViewFilteredSubtitle2", NUM));
                }
            }
            if (mediaFilterData2 != null) {
                int i6 = mediaFilterData2.filterType;
                if (i6 != 0) {
                    if (i6 == 1) {
                        this.adapter = this.sharedDocumentsAdapter;
                    } else if (i6 == 2) {
                        this.adapter = this.sharedLinksAdapter;
                    } else if (i6 == 3) {
                        this.adapter = this.sharedAudioAdapter;
                    } else if (i6 == 5) {
                        this.adapter = this.sharedVoiceAdapter;
                    }
                } else if (TextUtils.isEmpty(this.currentDataQuery)) {
                    this.adapter = this.sharedPhotoVideoAdapter;
                } else {
                    this.adapter = this.dialogsAdapter;
                }
            } else {
                this.adapter = this.dialogsAdapter;
            }
            RecyclerView.Adapter adapter2 = this.recyclerListView.getAdapter();
            RecyclerView.Adapter adapter3 = this.adapter;
            if (adapter2 != adapter3) {
                this.recyclerListView.setAdapter(adapter3);
            }
            if (!z) {
                this.localTipChats.clear();
                if (arrayList4 != null) {
                    this.localTipChats.addAll(arrayList4);
                }
                if (str.length() >= 3 && (LocaleController.getString("SavedMessages", NUM).toLowerCase().startsWith(str3) || "saved messages".startsWith(str3))) {
                    int i7 = 0;
                    while (true) {
                        if (i7 < this.localTipChats.size()) {
                            if ((this.localTipChats.get(i7) instanceof TLRPC$User) && UserConfig.getInstance(UserConfig.selectedAccount).getCurrentUser().id == ((TLRPC$User) this.localTipChats.get(i7)).id) {
                                break;
                            }
                            i7++;
                        } else {
                            z2 = false;
                            break;
                        }
                    }
                    if (!z2) {
                        this.localTipChats.add(0, UserConfig.getInstance(UserConfig.selectedAccount).getCurrentUser());
                    }
                }
                this.localTipDates.clear();
                this.localTipDates.addAll(arrayList3);
                Delegate delegate2 = this.delegate;
                if (delegate2 != null) {
                    delegate2.updateFiltersView(TextUtils.isEmpty(this.currentDataQuery), this.localTipChats, this.localTipDates);
                }
            }
            final View view = null;
            final int i8 = -1;
            for (int i9 = 0; i9 < size; i9++) {
                View childAt = this.recyclerListView.getChildAt(i9);
                if (childAt instanceof FlickerLoadingView) {
                    i8 = this.recyclerListView.getChildAdapterPosition(childAt);
                    view = childAt;
                }
            }
            if (view != null) {
                this.recyclerListView.removeView(view);
            }
            if ((this.loadingView.getVisibility() == 0 && this.recyclerListView.getChildCount() == 0) || !(this.recyclerListView.getAdapter() == this.sharedPhotoVideoAdapter || view == null)) {
                final int i10 = i2;
                getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                    public boolean onPreDraw() {
                        FilteredSearchView.this.getViewTreeObserver().removeOnPreDrawListener(this);
                        int childCount = FilteredSearchView.this.recyclerListView.getChildCount();
                        AnimatorSet animatorSet = new AnimatorSet();
                        for (int i = 0; i < childCount; i++) {
                            View childAt = FilteredSearchView.this.recyclerListView.getChildAt(i);
                            if (view == null || FilteredSearchView.this.recyclerListView.getChildAdapterPosition(childAt) >= i8) {
                                childAt.setAlpha(0.0f);
                                ObjectAnimator ofFloat = ObjectAnimator.ofFloat(childAt, View.ALPHA, new float[]{0.0f, 1.0f});
                                ofFloat.setStartDelay((long) ((int) ((((float) Math.min(FilteredSearchView.this.recyclerListView.getMeasuredHeight(), Math.max(0, childAt.getTop()))) / ((float) FilteredSearchView.this.recyclerListView.getMeasuredHeight())) * 100.0f)));
                                ofFloat.setDuration(200);
                                animatorSet.playTogether(new Animator[]{ofFloat});
                            }
                        }
                        animatorSet.addListener(new AnimatorListenerAdapter() {
                            public void onAnimationEnd(Animator animator) {
                                NotificationCenter.getInstance(i10).onAnimationFinish(FilteredSearchView.this.animationIndex);
                            }
                        });
                        int unused = FilteredSearchView.this.animationIndex = NotificationCenter.getInstance(i10).setAnimationInProgress(FilteredSearchView.this.animationIndex, (int[]) null);
                        animatorSet.start();
                        View view = view;
                        if (view != null && view.getParent() == null) {
                            FilteredSearchView.this.recyclerListView.addView(view);
                            final RecyclerView.LayoutManager layoutManager = FilteredSearchView.this.recyclerListView.getLayoutManager();
                            if (layoutManager != null) {
                                layoutManager.ignoreView(view);
                                View view2 = view;
                                ObjectAnimator ofFloat2 = ObjectAnimator.ofFloat(view2, View.ALPHA, new float[]{view2.getAlpha(), 0.0f});
                                ofFloat2.addListener(new AnimatorListenerAdapter() {
                                    public void onAnimationEnd(Animator animator) {
                                        view.setAlpha(1.0f);
                                        layoutManager.stopIgnoringView(view);
                                        AnonymousClass7 r2 = AnonymousClass7.this;
                                        FilteredSearchView.this.recyclerListView.removeView(view);
                                    }
                                });
                                ofFloat2.start();
                            }
                        }
                        return true;
                    }
                });
            }
            this.adapter.notifyDataSetChanged();
        }
    }

    public void update() {
        RecyclerView.Adapter adapter2 = this.adapter;
        if (adapter2 != null) {
            adapter2.notifyDataSetChanged();
        }
    }

    public void setKeyboardHeight(int i, boolean z) {
        this.emptyView.setKeyboardHeight(i, z);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:6:0x0025, code lost:
        if (org.telegram.messenger.ChatObject.isChannel(r5, org.telegram.messenger.UserConfig.selectedAccount) != false) goto L_0x0029;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void messagesDeleted(int r10, java.util.ArrayList<java.lang.Integer> r11) {
        /*
            r9 = this;
            r0 = 0
            r1 = 0
            r2 = 0
        L_0x0003:
            java.util.ArrayList<org.telegram.messenger.MessageObject> r3 = r9.messages
            int r3 = r3.size()
            if (r1 >= r3) goto L_0x007f
            java.util.ArrayList<org.telegram.messenger.MessageObject> r3 = r9.messages
            java.lang.Object r3 = r3.get(r1)
            org.telegram.messenger.MessageObject r3 = (org.telegram.messenger.MessageObject) r3
            long r4 = r3.getDialogId()
            r6 = 0
            int r8 = (r4 > r6 ? 1 : (r4 == r6 ? 0 : -1))
            if (r8 >= 0) goto L_0x0028
            long r4 = -r4
            int r5 = (int) r4
            int r4 = org.telegram.messenger.UserConfig.selectedAccount
            boolean r4 = org.telegram.messenger.ChatObject.isChannel(r5, r4)
            if (r4 == 0) goto L_0x0028
            goto L_0x0029
        L_0x0028:
            r5 = 0
        L_0x0029:
            r4 = 1
            if (r5 != r10) goto L_0x007d
            r5 = 0
        L_0x002d:
            int r6 = r11.size()
            if (r5 >= r6) goto L_0x007d
            int r6 = r3.getId()
            java.lang.Object r7 = r11.get(r5)
            java.lang.Integer r7 = (java.lang.Integer) r7
            int r7 = r7.intValue()
            if (r6 != r7) goto L_0x007a
            java.util.ArrayList<org.telegram.messenger.MessageObject> r2 = r9.messages
            r2.remove(r1)
            android.util.SparseArray<org.telegram.messenger.MessageObject> r2 = r9.messagesById
            int r6 = r3.getId()
            r2.remove(r6)
            java.util.HashMap<java.lang.String, java.util.ArrayList<org.telegram.messenger.MessageObject>> r2 = r9.sectionArrays
            java.lang.String r6 = r3.monthKey
            java.lang.Object r2 = r2.get(r6)
            java.util.ArrayList r2 = (java.util.ArrayList) r2
            r2.remove(r3)
            int r2 = r2.size()
            if (r2 != 0) goto L_0x0072
            java.util.ArrayList<java.lang.String> r2 = r9.sections
            java.lang.String r6 = r3.monthKey
            r2.remove(r6)
            java.util.HashMap<java.lang.String, java.util.ArrayList<org.telegram.messenger.MessageObject>> r2 = r9.sectionArrays
            java.lang.String r6 = r3.monthKey
            r2.remove(r6)
        L_0x0072:
            int r1 = r1 + -1
            int r2 = r9.totalCount
            int r2 = r2 - r4
            r9.totalCount = r2
            r2 = 1
        L_0x007a:
            int r5 = r5 + 1
            goto L_0x002d
        L_0x007d:
            int r1 = r1 + r4
            goto L_0x0003
        L_0x007f:
            if (r2 == 0) goto L_0x0088
            androidx.recyclerview.widget.RecyclerView$Adapter r10 = r9.adapter
            if (r10 == 0) goto L_0x0088
            r10.notifyDataSetChanged()
        L_0x0088:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.FilteredSearchView.messagesDeleted(int, java.util.ArrayList):void");
    }

    private class SharedPhotoVideoAdapter extends RecyclerListView.SelectionAdapter {
        private Context mContext;

        public boolean isEnabled(RecyclerView.ViewHolder viewHolder) {
            return false;
        }

        public SharedPhotoVideoAdapter(Context context) {
            this.mContext = context;
        }

        public int getItemCount() {
            if (FilteredSearchView.this.messages.isEmpty()) {
                return 0;
            }
            return ((int) Math.ceil((double) (((float) FilteredSearchView.this.messages.size()) / ((float) FilteredSearchView.this.columnsCount)))) + (FilteredSearchView.this.endReached ^ true ? 1 : 0);
        }

        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            AnonymousClass2 r4;
            if (i == 0) {
                SharedPhotoVideoCell sharedPhotoVideoCell = new SharedPhotoVideoCell(this.mContext, 1);
                sharedPhotoVideoCell.setDelegate(new SharedPhotoVideoCell.SharedPhotoVideoCellDelegate() {
                    public void didClickItem(SharedPhotoVideoCell sharedPhotoVideoCell, int i, MessageObject messageObject, int i2) {
                        FilteredSearchView.this.onItemClick(i, sharedPhotoVideoCell, messageObject, i2);
                    }

                    public boolean didLongClickItem(SharedPhotoVideoCell sharedPhotoVideoCell, int i, MessageObject messageObject, int i2) {
                        if (!FilteredSearchView.this.uiCallback.actionModeShowing()) {
                            return FilteredSearchView.this.onItemLongClick(messageObject, sharedPhotoVideoCell, i2);
                        }
                        didClickItem(sharedPhotoVideoCell, i, messageObject, i2);
                        return true;
                    }
                });
                r4 = sharedPhotoVideoCell;
            } else if (i != 2) {
                AnonymousClass2 r42 = new FlickerLoadingView(this.mContext) {
                    public int getColumnsCount() {
                        return FilteredSearchView.this.columnsCount;
                    }
                };
                r42.setIsSingleCell(true);
                r42.setViewType(2);
                r4 = r42;
            } else {
                GraySectionCell graySectionCell = new GraySectionCell(this.mContext);
                graySectionCell.setBackgroundColor(Theme.getColor("graySection") & -NUM);
                r4 = graySectionCell;
            }
            r4.setLayoutParams(new RecyclerView.LayoutParams(-1, -2));
            return new RecyclerListView.Holder(r4);
        }

        public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {
            boolean z = true;
            if (viewHolder.getItemViewType() == 0) {
                FilteredSearchView filteredSearchView = FilteredSearchView.this;
                ArrayList<MessageObject> arrayList = filteredSearchView.messages;
                SharedPhotoVideoCell sharedPhotoVideoCell = (SharedPhotoVideoCell) viewHolder.itemView;
                sharedPhotoVideoCell.setItemsCount(filteredSearchView.columnsCount);
                sharedPhotoVideoCell.setIsFirst(i == 0);
                for (int i2 = 0; i2 < FilteredSearchView.this.columnsCount; i2++) {
                    int access$700 = (FilteredSearchView.this.columnsCount * i) + i2;
                    if (access$700 < arrayList.size()) {
                        MessageObject messageObject = arrayList.get(access$700);
                        sharedPhotoVideoCell.setItem(i2, FilteredSearchView.this.messages.indexOf(messageObject), messageObject);
                        if (FilteredSearchView.this.uiCallback.actionModeShowing()) {
                            FilteredSearchView.this.messageHashIdTmp.set(messageObject.getId(), messageObject.getDialogId());
                            sharedPhotoVideoCell.setChecked(i2, FilteredSearchView.this.uiCallback.isSelected(FilteredSearchView.this.messageHashIdTmp), true);
                        } else {
                            sharedPhotoVideoCell.setChecked(i2, false, true);
                        }
                    } else {
                        sharedPhotoVideoCell.setItem(i2, access$700, (MessageObject) null);
                    }
                }
                sharedPhotoVideoCell.requestLayout();
            } else if (viewHolder.getItemViewType() == 3) {
                DialogCell dialogCell = (DialogCell) viewHolder.itemView;
                dialogCell.useSeparator = i != getItemCount() - 1;
                MessageObject messageObject2 = FilteredSearchView.this.messages.get(i);
                if (dialogCell.getMessage() == null || dialogCell.getMessage().getId() != messageObject2.getId()) {
                    z = false;
                }
                dialogCell.setDialog(messageObject2.getDialogId(), messageObject2, messageObject2.messageOwner.date, false);
                if (FilteredSearchView.this.uiCallback.actionModeShowing()) {
                    FilteredSearchView.this.messageHashIdTmp.set(messageObject2.getId(), messageObject2.getDialogId());
                    dialogCell.setChecked(FilteredSearchView.this.uiCallback.isSelected(FilteredSearchView.this.messageHashIdTmp), z);
                    return;
                }
                dialogCell.setChecked(false, z);
            } else if (viewHolder.getItemViewType() == 1) {
                ((FlickerLoadingView) viewHolder.itemView).skipDrawItemsCount(FilteredSearchView.this.columnsCount - ((FilteredSearchView.this.columnsCount * ((int) Math.ceil((double) (((float) FilteredSearchView.this.messages.size()) / ((float) FilteredSearchView.this.columnsCount))))) - FilteredSearchView.this.messages.size()));
            }
        }

        public int getItemViewType(int i) {
            return i < ((int) Math.ceil((double) (((float) FilteredSearchView.this.messages.size()) / ((float) FilteredSearchView.this.columnsCount)))) ? 0 : 1;
        }
    }

    /* access modifiers changed from: private */
    public void onItemClick(int i, View view, MessageObject messageObject, int i2) {
        if (messageObject != null) {
            if (this.uiCallback.actionModeShowing()) {
                this.uiCallback.toggleItemSelection(messageObject, view, i2);
                return;
            }
            boolean z = view instanceof DialogCell;
            if (z) {
                this.uiCallback.goToMessage(messageObject);
                return;
            }
            int i3 = this.currentSearchFilter.filterType;
            if (i3 == 0) {
                if (z) {
                    this.uiCallback.goToMessage(((DialogCell) view).getMessage());
                    return;
                }
                PhotoViewer.getInstance().setParentActivity(this.parentActivity);
                PhotoViewer.getInstance().openPhoto(this.messages, i, 0, 0, this.provider);
                this.photoViewerClassGuid = PhotoViewer.getInstance().getClassGuid();
            } else if (i3 == 3 || i3 == 5) {
                if (view instanceof SharedAudioCell) {
                    ((SharedAudioCell) view).didPressedButton();
                }
            } else if (i3 == 1) {
                if (view instanceof SharedDocumentCell) {
                    SharedDocumentCell sharedDocumentCell = (SharedDocumentCell) view;
                    TLRPC$Document document = messageObject.getDocument();
                    if (sharedDocumentCell.isLoaded()) {
                        if (messageObject.canPreviewDocument()) {
                            PhotoViewer.getInstance().setParentActivity(this.parentActivity);
                            int indexOf = this.messages.indexOf(messageObject);
                            if (indexOf < 0) {
                                ArrayList arrayList = new ArrayList();
                                arrayList.add(messageObject);
                                PhotoViewer.getInstance().setParentActivity(this.parentActivity);
                                PhotoViewer.getInstance().openPhoto((ArrayList<MessageObject>) arrayList, 0, 0, 0, this.provider);
                                this.photoViewerClassGuid = PhotoViewer.getInstance().getClassGuid();
                                return;
                            }
                            PhotoViewer.getInstance().setParentActivity(this.parentActivity);
                            PhotoViewer.getInstance().openPhoto(this.messages, indexOf, 0, 0, this.provider);
                            this.photoViewerClassGuid = PhotoViewer.getInstance().getClassGuid();
                            return;
                        }
                        AndroidUtilities.openDocument(messageObject, this.parentActivity, this.parentFragment);
                    } else if (!sharedDocumentCell.isLoading()) {
                        AccountInstance.getInstance(UserConfig.selectedAccount).getFileLoader().loadFile(document, sharedDocumentCell.getMessage(), 0, 0);
                        sharedDocumentCell.updateFileExistIcon(true);
                    } else {
                        AccountInstance.getInstance(UserConfig.selectedAccount).getFileLoader().cancelLoadFile(document);
                        sharedDocumentCell.updateFileExistIcon(true);
                    }
                }
            } else if (i3 == 2) {
                try {
                    TLRPC$MessageMedia tLRPC$MessageMedia = messageObject.messageOwner.media;
                    String str = null;
                    TLRPC$WebPage tLRPC$WebPage = tLRPC$MessageMedia != null ? tLRPC$MessageMedia.webpage : null;
                    if (tLRPC$WebPage != null && !(tLRPC$WebPage instanceof TLRPC$TL_webPageEmpty)) {
                        if (tLRPC$WebPage.cached_page != null) {
                            ArticleViewer.getInstance().setParentActivity(this.parentActivity, this.parentFragment);
                            ArticleViewer.getInstance().open(messageObject);
                            return;
                        }
                        String str2 = tLRPC$WebPage.embed_url;
                        if (str2 == null || str2.length() == 0) {
                            str = tLRPC$WebPage.url;
                        } else {
                            openWebView(tLRPC$WebPage);
                            return;
                        }
                    }
                    if (str == null) {
                        str = ((SharedLinkCell) view).getLink(0);
                    }
                    if (str != null) {
                        openUrl(str);
                    }
                } catch (Exception e) {
                    FileLog.e((Throwable) e);
                }
            }
        }
    }

    private class SharedLinksAdapter extends RecyclerListView.SectionsAdapter {
        private Context mContext;
        private final SharedLinkCell.SharedLinkCellDelegate sharedLinkCellDelegate = new SharedLinkCell.SharedLinkCellDelegate() {
            public void needOpenWebView(TLRPC$WebPage tLRPC$WebPage) {
                FilteredSearchView.this.openWebView(tLRPC$WebPage);
            }

            public boolean canPerformActions() {
                return !FilteredSearchView.this.uiCallback.actionModeShowing();
            }

            public void onLinkPress(String str, boolean z) {
                if (z) {
                    BottomSheet.Builder builder = new BottomSheet.Builder(FilteredSearchView.this.parentActivity);
                    builder.setTitle(str);
                    builder.setItems(new CharSequence[]{LocaleController.getString("Open", NUM), LocaleController.getString("Copy", NUM)}, 
                    /*  JADX ERROR: Method code generation error
                        jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x0030: INVOKE  
                          (r6v3 'builder' org.telegram.ui.ActionBar.BottomSheet$Builder)
                          (wrap: java.lang.CharSequence[] : ?: FILLED_NEW_ARRAY  (r0v4 java.lang.CharSequence[]) = 
                          (wrap: java.lang.String : 0x0019: INVOKE  (r2v1 java.lang.String) = ("Open"), (NUM int) org.telegram.messenger.LocaleController.getString(java.lang.String, int):java.lang.String type: STATIC)
                          (wrap: java.lang.String : 0x0025: INVOKE  (r2v3 java.lang.String) = ("Copy"), (NUM int) org.telegram.messenger.LocaleController.getString(java.lang.String, int):java.lang.String type: STATIC)
                         elemType: java.lang.CharSequence)
                          (wrap: org.telegram.ui.-$$Lambda$FilteredSearchView$SharedLinksAdapter$1$G3IrzfZSqRDBKftthXdsXAeg2tk : 0x002d: CONSTRUCTOR  (r1v2 org.telegram.ui.-$$Lambda$FilteredSearchView$SharedLinksAdapter$1$G3IrzfZSqRDBKftthXdsXAeg2tk) = 
                          (r4v0 'this' org.telegram.ui.FilteredSearchView$SharedLinksAdapter$1 A[THIS])
                          (r5v0 'str' java.lang.String)
                         call: org.telegram.ui.-$$Lambda$FilteredSearchView$SharedLinksAdapter$1$G3IrzfZSqRDBKftthXdsXAeg2tk.<init>(org.telegram.ui.FilteredSearchView$SharedLinksAdapter$1, java.lang.String):void type: CONSTRUCTOR)
                         org.telegram.ui.ActionBar.BottomSheet.Builder.setItems(java.lang.CharSequence[], android.content.DialogInterface$OnClickListener):org.telegram.ui.ActionBar.BottomSheet$Builder type: VIRTUAL in method: org.telegram.ui.FilteredSearchView.SharedLinksAdapter.1.onLinkPress(java.lang.String, boolean):void, dex: classes.dex
                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:256)
                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:221)
                        	at jadx.core.codegen.RegionGen.makeSimpleBlock(RegionGen.java:109)
                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:55)
                        	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                        	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:98)
                        	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:142)
                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:62)
                        	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                        	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                        	at jadx.core.codegen.MethodGen.addRegionInsns(MethodGen.java:211)
                        	at jadx.core.codegen.MethodGen.addInstructions(MethodGen.java:204)
                        	at jadx.core.codegen.ClassGen.addMethodCode(ClassGen.java:318)
                        	at jadx.core.codegen.ClassGen.addMethod(ClassGen.java:271)
                        	at jadx.core.codegen.ClassGen.lambda$addInnerClsAndMethods$2(ClassGen.java:240)
                        	at java.util.stream.ForEachOps$ForEachOp$OfRef.accept(ForEachOps.java:183)
                        	at java.util.ArrayList.forEach(ArrayList.java:1259)
                        	at java.util.stream.SortedOps$RefSortingSink.end(SortedOps.java:395)
                        	at java.util.stream.Sink$ChainedReference.end(Sink.java:258)
                        	at java.util.stream.AbstractPipeline.copyInto(AbstractPipeline.java:483)
                        	at java.util.stream.AbstractPipeline.wrapAndCopyInto(AbstractPipeline.java:472)
                        	at java.util.stream.ForEachOps$ForEachOp.evaluateSequential(ForEachOps.java:150)
                        	at java.util.stream.ForEachOps$ForEachOp$OfRef.evaluateSequential(ForEachOps.java:173)
                        	at java.util.stream.AbstractPipeline.evaluate(AbstractPipeline.java:234)
                        	at java.util.stream.ReferencePipeline.forEach(ReferencePipeline.java:485)
                        	at jadx.core.codegen.ClassGen.addInnerClsAndMethods(ClassGen.java:236)
                        	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:227)
                        	at jadx.core.codegen.InsnGen.inlineAnonymousConstructor(InsnGen.java:676)
                        	at jadx.core.codegen.InsnGen.makeConstructor(InsnGen.java:607)
                        	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:364)
                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:231)
                        	at jadx.core.codegen.InsnGen.addWrappedArg(InsnGen.java:123)
                        	at jadx.core.codegen.InsnGen.addArg(InsnGen.java:107)
                        	at jadx.core.codegen.InsnGen.addArg(InsnGen.java:98)
                        	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:480)
                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:231)
                        	at jadx.core.codegen.ClassGen.addInsnBody(ClassGen.java:437)
                        	at jadx.core.codegen.ClassGen.addField(ClassGen.java:378)
                        	at jadx.core.codegen.ClassGen.addFields(ClassGen.java:348)
                        	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:226)
                        	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:112)
                        	at jadx.core.codegen.ClassGen.addInnerClass(ClassGen.java:249)
                        	at jadx.core.codegen.ClassGen.lambda$addInnerClsAndMethods$2(ClassGen.java:238)
                        	at java.util.stream.ForEachOps$ForEachOp$OfRef.accept(ForEachOps.java:183)
                        	at java.util.ArrayList.forEach(ArrayList.java:1259)
                        	at java.util.stream.SortedOps$RefSortingSink.end(SortedOps.java:395)
                        	at java.util.stream.Sink$ChainedReference.end(Sink.java:258)
                        	at java.util.stream.AbstractPipeline.copyInto(AbstractPipeline.java:483)
                        	at java.util.stream.AbstractPipeline.wrapAndCopyInto(AbstractPipeline.java:472)
                        	at java.util.stream.ForEachOps$ForEachOp.evaluateSequential(ForEachOps.java:150)
                        	at java.util.stream.ForEachOps$ForEachOp$OfRef.evaluateSequential(ForEachOps.java:173)
                        	at java.util.stream.AbstractPipeline.evaluate(AbstractPipeline.java:234)
                        	at java.util.stream.ReferencePipeline.forEach(ReferencePipeline.java:485)
                        	at jadx.core.codegen.ClassGen.addInnerClsAndMethods(ClassGen.java:236)
                        	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:227)
                        	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:112)
                        	at jadx.core.codegen.ClassGen.makeClass(ClassGen.java:78)
                        	at jadx.core.codegen.CodeGen.wrapCodeGen(CodeGen.java:44)
                        	at jadx.core.codegen.CodeGen.generateJavaCode(CodeGen.java:33)
                        	at jadx.core.codegen.CodeGen.generate(CodeGen.java:21)
                        	at jadx.core.ProcessClass.generateCode(ProcessClass.java:61)
                        	at jadx.core.dex.nodes.ClassNode.decompile(ClassNode.java:273)
                        Caused by: jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x002d: CONSTRUCTOR  (r1v2 org.telegram.ui.-$$Lambda$FilteredSearchView$SharedLinksAdapter$1$G3IrzfZSqRDBKftthXdsXAeg2tk) = 
                          (r4v0 'this' org.telegram.ui.FilteredSearchView$SharedLinksAdapter$1 A[THIS])
                          (r5v0 'str' java.lang.String)
                         call: org.telegram.ui.-$$Lambda$FilteredSearchView$SharedLinksAdapter$1$G3IrzfZSqRDBKftthXdsXAeg2tk.<init>(org.telegram.ui.FilteredSearchView$SharedLinksAdapter$1, java.lang.String):void type: CONSTRUCTOR in method: org.telegram.ui.FilteredSearchView.SharedLinksAdapter.1.onLinkPress(java.lang.String, boolean):void, dex: classes.dex
                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:256)
                        	at jadx.core.codegen.InsnGen.addWrappedArg(InsnGen.java:123)
                        	at jadx.core.codegen.InsnGen.addArg(InsnGen.java:107)
                        	at jadx.core.codegen.InsnGen.generateMethodArguments(InsnGen.java:787)
                        	at jadx.core.codegen.InsnGen.makeInvoke(InsnGen.java:728)
                        	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:368)
                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:250)
                        	... 64 more
                        Caused by: jadx.core.utils.exceptions.JadxRuntimeException: Expected class to be processed at this point, class: org.telegram.ui.-$$Lambda$FilteredSearchView$SharedLinksAdapter$1$G3IrzfZSqRDBKftthXdsXAeg2tk, state: NOT_LOADED
                        	at jadx.core.dex.nodes.ClassNode.ensureProcessed(ClassNode.java:260)
                        	at jadx.core.codegen.InsnGen.makeConstructor(InsnGen.java:606)
                        	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:364)
                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:231)
                        	... 70 more
                        */
                    /*
                        this = this;
                        if (r6 == 0) goto L_0x0041
                        org.telegram.ui.ActionBar.BottomSheet$Builder r6 = new org.telegram.ui.ActionBar.BottomSheet$Builder
                        org.telegram.ui.FilteredSearchView$SharedLinksAdapter r0 = org.telegram.ui.FilteredSearchView.SharedLinksAdapter.this
                        org.telegram.ui.FilteredSearchView r0 = org.telegram.ui.FilteredSearchView.this
                        android.app.Activity r0 = r0.parentActivity
                        r6.<init>(r0)
                        r6.setTitle(r5)
                        r0 = 2
                        java.lang.CharSequence[] r0 = new java.lang.CharSequence[r0]
                        r1 = 0
                        r2 = 2131626488(0x7f0e09f8, float:1.8880214E38)
                        java.lang.String r3 = "Open"
                        java.lang.String r2 = org.telegram.messenger.LocaleController.getString(r3, r2)
                        r0[r1] = r2
                        r1 = 1
                        r2 = 2131624987(0x7f0e041b, float:1.887717E38)
                        java.lang.String r3 = "Copy"
                        java.lang.String r2 = org.telegram.messenger.LocaleController.getString(r3, r2)
                        r0[r1] = r2
                        org.telegram.ui.-$$Lambda$FilteredSearchView$SharedLinksAdapter$1$G3IrzfZSqRDBKftthXdsXAeg2tk r1 = new org.telegram.ui.-$$Lambda$FilteredSearchView$SharedLinksAdapter$1$G3IrzfZSqRDBKftthXdsXAeg2tk
                        r1.<init>(r4, r5)
                        r6.setItems(r0, r1)
                        org.telegram.ui.FilteredSearchView$SharedLinksAdapter r5 = org.telegram.ui.FilteredSearchView.SharedLinksAdapter.this
                        org.telegram.ui.FilteredSearchView r5 = org.telegram.ui.FilteredSearchView.this
                        org.telegram.ui.ActionBar.BaseFragment r5 = r5.parentFragment
                        org.telegram.ui.ActionBar.BottomSheet r6 = r6.create()
                        r5.showDialog(r6)
                        goto L_0x0048
                    L_0x0041:
                        org.telegram.ui.FilteredSearchView$SharedLinksAdapter r6 = org.telegram.ui.FilteredSearchView.SharedLinksAdapter.this
                        org.telegram.ui.FilteredSearchView r6 = org.telegram.ui.FilteredSearchView.this
                        r6.openUrl(r5)
                    L_0x0048:
                        return
                    */
                    throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.FilteredSearchView.SharedLinksAdapter.AnonymousClass1.onLinkPress(java.lang.String, boolean):void");
                }

                /* access modifiers changed from: private */
                /* renamed from: lambda$onLinkPress$0 */
                public /* synthetic */ void lambda$onLinkPress$0$FilteredSearchView$SharedLinksAdapter$1(String str, DialogInterface dialogInterface, int i) {
                    if (i == 0) {
                        FilteredSearchView.this.openUrl(str);
                    } else if (i == 1) {
                        if (str.startsWith("mailto:")) {
                            str = str.substring(7);
                        } else if (str.startsWith("tel:")) {
                            str = str.substring(4);
                        }
                        AndroidUtilities.addToClipboard(str);
                    }
                }
            };

            public String getLetter(int i) {
                return null;
            }

            public int getPositionForScrollProgress(float f) {
                return 0;
            }

            public boolean isEnabled(int i, int i2) {
                return true;
            }

            public SharedLinksAdapter(Context context) {
                this.mContext = context;
            }

            public int getSectionCount() {
                int i = 0;
                if (FilteredSearchView.this.messages.isEmpty()) {
                    return 0;
                }
                if (FilteredSearchView.this.sections.isEmpty() && FilteredSearchView.this.isLoading) {
                    return 0;
                }
                int size = FilteredSearchView.this.sections.size();
                if (!FilteredSearchView.this.sections.isEmpty() && !FilteredSearchView.this.endReached) {
                    i = 1;
                }
                return size + i;
            }

            public int getCountForSection(int i) {
                int i2 = 1;
                if (i >= FilteredSearchView.this.sections.size()) {
                    return 1;
                }
                FilteredSearchView filteredSearchView = FilteredSearchView.this;
                int size = filteredSearchView.sectionArrays.get(filteredSearchView.sections.get(i)).size();
                if (i == 0) {
                    i2 = 0;
                }
                return size + i2;
            }

            public View getSectionHeaderView(int i, View view) {
                if (view == null) {
                    view = new GraySectionCell(this.mContext);
                    view.setBackgroundColor(Theme.getColor("graySection") & -NUM);
                }
                if (i == 0) {
                    view.setAlpha(0.0f);
                    return view;
                }
                if (i < FilteredSearchView.this.sections.size()) {
                    view.setAlpha(1.0f);
                    ((GraySectionCell) view).setText(LocaleController.formatSectionDate((long) ((MessageObject) FilteredSearchView.this.sectionArrays.get(FilteredSearchView.this.sections.get(i)).get(0)).messageOwner.date));
                }
                return view;
            }

            /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v1, resolved type: org.telegram.ui.Cells.GraySectionCell} */
            /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v3, resolved type: org.telegram.ui.Cells.SharedLinkCell} */
            /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v4, resolved type: org.telegram.ui.Components.FlickerLoadingView} */
            /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v5, resolved type: org.telegram.ui.Cells.GraySectionCell} */
            /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v6, resolved type: org.telegram.ui.Cells.GraySectionCell} */
            /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v7, resolved type: org.telegram.ui.Cells.GraySectionCell} */
            /* JADX WARNING: Multi-variable type inference failed */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public androidx.recyclerview.widget.RecyclerView.ViewHolder onCreateViewHolder(android.view.ViewGroup r3, int r4) {
                /*
                    r2 = this;
                    if (r4 == 0) goto L_0x0021
                    r3 = 1
                    if (r4 == r3) goto L_0x0014
                    org.telegram.ui.Components.FlickerLoadingView r4 = new org.telegram.ui.Components.FlickerLoadingView
                    android.content.Context r0 = r2.mContext
                    r4.<init>(r0)
                    r0 = 5
                    r4.setViewType(r0)
                    r4.setIsSingleCell(r3)
                    goto L_0x0028
                L_0x0014:
                    org.telegram.ui.Cells.SharedLinkCell r4 = new org.telegram.ui.Cells.SharedLinkCell
                    android.content.Context r0 = r2.mContext
                    r4.<init>(r0, r3)
                    org.telegram.ui.Cells.SharedLinkCell$SharedLinkCellDelegate r3 = r2.sharedLinkCellDelegate
                    r4.setDelegate(r3)
                    goto L_0x0028
                L_0x0021:
                    org.telegram.ui.Cells.GraySectionCell r4 = new org.telegram.ui.Cells.GraySectionCell
                    android.content.Context r3 = r2.mContext
                    r4.<init>(r3)
                L_0x0028:
                    androidx.recyclerview.widget.RecyclerView$LayoutParams r3 = new androidx.recyclerview.widget.RecyclerView$LayoutParams
                    r0 = -1
                    r1 = -2
                    r3.<init>((int) r0, (int) r1)
                    r4.setLayoutParams(r3)
                    org.telegram.ui.Components.RecyclerListView$Holder r3 = new org.telegram.ui.Components.RecyclerListView$Holder
                    r3.<init>(r4)
                    return r3
                */
                throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.FilteredSearchView.SharedLinksAdapter.onCreateViewHolder(android.view.ViewGroup, int):androidx.recyclerview.widget.RecyclerView$ViewHolder");
            }

            public void onBindViewHolder(int i, int i2, RecyclerView.ViewHolder viewHolder) {
                if (viewHolder.getItemViewType() != 2) {
                    ArrayList arrayList = FilteredSearchView.this.sectionArrays.get(FilteredSearchView.this.sections.get(i));
                    int itemViewType = viewHolder.getItemViewType();
                    boolean z = false;
                    if (itemViewType == 0) {
                        ((GraySectionCell) viewHolder.itemView).setText(LocaleController.formatSectionDate((long) ((MessageObject) arrayList.get(0)).messageOwner.date));
                    } else if (itemViewType == 1) {
                        if (i != 0) {
                            i2--;
                        }
                        final SharedLinkCell sharedLinkCell = (SharedLinkCell) viewHolder.itemView;
                        final MessageObject messageObject = (MessageObject) arrayList.get(i2);
                        final boolean z2 = sharedLinkCell.getMessage() != null && sharedLinkCell.getMessage().getId() == messageObject.getId();
                        if (i2 != arrayList.size() - 1 || (i == FilteredSearchView.this.sections.size() - 1 && FilteredSearchView.this.isLoading)) {
                            z = true;
                        }
                        sharedLinkCell.setLink(messageObject, z);
                        sharedLinkCell.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                            public boolean onPreDraw() {
                                sharedLinkCell.getViewTreeObserver().removeOnPreDrawListener(this);
                                if (FilteredSearchView.this.uiCallback.actionModeShowing()) {
                                    FilteredSearchView.this.messageHashIdTmp.set(messageObject.getId(), messageObject.getDialogId());
                                    sharedLinkCell.setChecked(FilteredSearchView.this.uiCallback.isSelected(FilteredSearchView.this.messageHashIdTmp), z2);
                                    return true;
                                }
                                sharedLinkCell.setChecked(false, z2);
                                return true;
                            }
                        });
                    }
                }
            }

            public int getItemViewType(int i, int i2) {
                if (i < FilteredSearchView.this.sections.size()) {
                    return (i == 0 || i2 != 0) ? 1 : 0;
                }
                return 2;
            }
        }

        private class SharedDocumentsAdapter extends RecyclerListView.SectionsAdapter {
            private int currentType;
            private Context mContext;

            public String getLetter(int i) {
                return null;
            }

            public int getPositionForScrollProgress(float f) {
                return 0;
            }

            public boolean isEnabled(int i, int i2) {
                return i == 0 || i2 != 0;
            }

            public SharedDocumentsAdapter(Context context, int i) {
                this.mContext = context;
                this.currentType = i;
            }

            public int getSectionCount() {
                int i = 0;
                if (FilteredSearchView.this.sections.isEmpty()) {
                    return 0;
                }
                int size = FilteredSearchView.this.sections.size();
                if (!FilteredSearchView.this.sections.isEmpty() && !FilteredSearchView.this.endReached) {
                    i = 1;
                }
                return size + i;
            }

            public int getCountForSection(int i) {
                int i2 = 1;
                if (i >= FilteredSearchView.this.sections.size()) {
                    return 1;
                }
                FilteredSearchView filteredSearchView = FilteredSearchView.this;
                int size = filteredSearchView.sectionArrays.get(filteredSearchView.sections.get(i)).size();
                if (i == 0) {
                    i2 = 0;
                }
                return size + i2;
            }

            public View getSectionHeaderView(int i, View view) {
                if (view == null) {
                    view = new GraySectionCell(this.mContext);
                    view.setBackgroundColor(Theme.getColor("graySection") & -NUM);
                }
                if (i == 0) {
                    view.setAlpha(0.0f);
                    return view;
                }
                if (i < FilteredSearchView.this.sections.size()) {
                    view.setAlpha(1.0f);
                    ((GraySectionCell) view).setText(LocaleController.formatSectionDate((long) ((MessageObject) FilteredSearchView.this.sectionArrays.get(FilteredSearchView.this.sections.get(i)).get(0)).messageOwner.date));
                }
                return view;
            }

            /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v4, resolved type: org.telegram.ui.Cells.SharedDocumentCell} */
            /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v8, resolved type: org.telegram.ui.Cells.SharedDocumentCell} */
            /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v9, resolved type: org.telegram.ui.Cells.SharedDocumentCell} */
            /* JADX WARNING: Multi-variable type inference failed */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public androidx.recyclerview.widget.RecyclerView.ViewHolder onCreateViewHolder(android.view.ViewGroup r4, int r5) {
                /*
                    r3 = this;
                    if (r5 == 0) goto L_0x0034
                    r4 = 2
                    r0 = 1
                    if (r5 == r0) goto L_0x002b
                    if (r5 == r4) goto L_0x0010
                    org.telegram.ui.FilteredSearchView$SharedDocumentsAdapter$1 r4 = new org.telegram.ui.FilteredSearchView$SharedDocumentsAdapter$1
                    android.content.Context r5 = r3.mContext
                    r4.<init>(r5, r0)
                    goto L_0x003b
                L_0x0010:
                    org.telegram.ui.Components.FlickerLoadingView r5 = new org.telegram.ui.Components.FlickerLoadingView
                    android.content.Context r1 = r3.mContext
                    r5.<init>(r1)
                    int r1 = r3.currentType
                    r2 = 4
                    if (r1 == r4) goto L_0x0024
                    if (r1 != r2) goto L_0x001f
                    goto L_0x0024
                L_0x001f:
                    r4 = 3
                    r5.setViewType(r4)
                    goto L_0x0027
                L_0x0024:
                    r5.setViewType(r2)
                L_0x0027:
                    r5.setIsSingleCell(r0)
                    goto L_0x0032
                L_0x002b:
                    org.telegram.ui.Cells.SharedDocumentCell r5 = new org.telegram.ui.Cells.SharedDocumentCell
                    android.content.Context r0 = r3.mContext
                    r5.<init>(r0, r4)
                L_0x0032:
                    r4 = r5
                    goto L_0x003b
                L_0x0034:
                    org.telegram.ui.Cells.GraySectionCell r4 = new org.telegram.ui.Cells.GraySectionCell
                    android.content.Context r5 = r3.mContext
                    r4.<init>(r5)
                L_0x003b:
                    androidx.recyclerview.widget.RecyclerView$LayoutParams r5 = new androidx.recyclerview.widget.RecyclerView$LayoutParams
                    r0 = -1
                    r1 = -2
                    r5.<init>((int) r0, (int) r1)
                    r4.setLayoutParams(r5)
                    org.telegram.ui.Components.RecyclerListView$Holder r5 = new org.telegram.ui.Components.RecyclerListView$Holder
                    r5.<init>(r4)
                    return r5
                */
                throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.FilteredSearchView.SharedDocumentsAdapter.onCreateViewHolder(android.view.ViewGroup, int):androidx.recyclerview.widget.RecyclerView$ViewHolder");
            }

            public void onBindViewHolder(int i, int i2, RecyclerView.ViewHolder viewHolder) {
                if (viewHolder.getItemViewType() != 2) {
                    ArrayList arrayList = FilteredSearchView.this.sectionArrays.get(FilteredSearchView.this.sections.get(i));
                    int itemViewType = viewHolder.getItemViewType();
                    boolean z = false;
                    if (itemViewType == 0) {
                        ((GraySectionCell) viewHolder.itemView).setText(LocaleController.formatSectionDate((long) ((MessageObject) arrayList.get(0)).messageOwner.date));
                    } else if (itemViewType == 1) {
                        if (i != 0) {
                            i2--;
                        }
                        final SharedDocumentCell sharedDocumentCell = (SharedDocumentCell) viewHolder.itemView;
                        final MessageObject messageObject = (MessageObject) arrayList.get(i2);
                        final boolean z2 = sharedDocumentCell.getMessage() != null && sharedDocumentCell.getMessage().getId() == messageObject.getId();
                        if (i2 != arrayList.size() - 1 || (i == FilteredSearchView.this.sections.size() - 1 && FilteredSearchView.this.isLoading)) {
                            z = true;
                        }
                        sharedDocumentCell.setDocument(messageObject, z);
                        sharedDocumentCell.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                            public boolean onPreDraw() {
                                sharedDocumentCell.getViewTreeObserver().removeOnPreDrawListener(this);
                                if (FilteredSearchView.this.uiCallback.actionModeShowing()) {
                                    FilteredSearchView.this.messageHashIdTmp.set(messageObject.getId(), messageObject.getDialogId());
                                    sharedDocumentCell.setChecked(FilteredSearchView.this.uiCallback.isSelected(FilteredSearchView.this.messageHashIdTmp), z2);
                                    return true;
                                }
                                sharedDocumentCell.setChecked(false, z2);
                                return true;
                            }
                        });
                    } else if (itemViewType == 3) {
                        if (i != 0) {
                            i2--;
                        }
                        final SharedAudioCell sharedAudioCell = (SharedAudioCell) viewHolder.itemView;
                        final MessageObject messageObject2 = (MessageObject) arrayList.get(i2);
                        final boolean z3 = sharedAudioCell.getMessage() != null && sharedAudioCell.getMessage().getId() == messageObject2.getId();
                        if (i2 != arrayList.size() - 1 || (i == FilteredSearchView.this.sections.size() - 1 && FilteredSearchView.this.isLoading)) {
                            z = true;
                        }
                        sharedAudioCell.setMessageObject(messageObject2, z);
                        sharedAudioCell.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                            public boolean onPreDraw() {
                                sharedAudioCell.getViewTreeObserver().removeOnPreDrawListener(this);
                                if (FilteredSearchView.this.uiCallback.actionModeShowing()) {
                                    FilteredSearchView.this.messageHashIdTmp.set(messageObject2.getId(), messageObject2.getDialogId());
                                    sharedAudioCell.setChecked(FilteredSearchView.this.uiCallback.isSelected(FilteredSearchView.this.messageHashIdTmp), z3);
                                    return true;
                                }
                                sharedAudioCell.setChecked(false, z3);
                                return true;
                            }
                        });
                    }
                }
            }

            public int getItemViewType(int i, int i2) {
                if (i >= FilteredSearchView.this.sections.size()) {
                    return 2;
                }
                if (i != 0 && i2 == 0) {
                    return 0;
                }
                int i3 = this.currentType;
                return (i3 == 2 || i3 == 4) ? 3 : 1;
            }
        }

        /* access modifiers changed from: private */
        public void openUrl(String str) {
            if (AndroidUtilities.shouldShowUrlInAlert(str)) {
                AlertsCreator.showOpenUrlAlert(this.parentFragment, str, true, true);
            } else {
                Browser.openUrl((Context) this.parentActivity, str);
            }
        }

        /* access modifiers changed from: private */
        public void openWebView(TLRPC$WebPage tLRPC$WebPage) {
            EmbedBottomSheet.show(this.parentActivity, tLRPC$WebPage.site_name, tLRPC$WebPage.description, tLRPC$WebPage.url, tLRPC$WebPage.embed_url, tLRPC$WebPage.embed_width, tLRPC$WebPage.embed_height, false);
        }

        /* access modifiers changed from: protected */
        public void onAttachedToWindow() {
            super.onAttachedToWindow();
            int i = UserConfig.selectedAccount;
            this.lastAccount = i;
            NotificationCenter.getInstance(i).addObserver(this, NotificationCenter.emojiDidLoad);
        }

        /* access modifiers changed from: protected */
        public void onDetachedFromWindow() {
            super.onDetachedFromWindow();
            NotificationCenter.getInstance(this.lastAccount).removeObserver(this, NotificationCenter.emojiDidLoad);
        }

        public void didReceivedNotification(int i, int i2, Object... objArr) {
            if (i == NotificationCenter.emojiDidLoad) {
                int childCount = this.recyclerListView.getChildCount();
                for (int i3 = 0; i3 < childCount; i3++) {
                    if (this.recyclerListView.getChildAt(i3) instanceof DialogCell) {
                        ((DialogCell) this.recyclerListView.getChildAt(i3)).update(0);
                    }
                    this.recyclerListView.getChildAt(i3).invalidate();
                }
            }
        }

        /* access modifiers changed from: private */
        public boolean onItemLongClick(MessageObject messageObject, View view, int i) {
            if (!this.uiCallback.actionModeShowing()) {
                this.uiCallback.showActionMode();
            }
            this.uiCallback.toggleItemSelection(messageObject, view, i);
            return true;
        }

        public static class MessageHashId {
            public long dialogId;
            public int messageId;

            public MessageHashId(int i, long j) {
                this.dialogId = j;
                this.messageId = i;
            }

            public void set(int i, long j) {
                this.dialogId = j;
                this.messageId = i;
            }

            public boolean equals(Object obj) {
                if (this == obj) {
                    return true;
                }
                if (obj == null || MessageHashId.class != obj.getClass()) {
                    return false;
                }
                MessageHashId messageHashId = (MessageHashId) obj;
                if (this.dialogId == messageHashId.dialogId && this.messageId == messageHashId.messageId) {
                    return true;
                }
                return false;
            }

            public int hashCode() {
                return this.messageId;
            }
        }

        class OnlyUserFiltersAdapter extends RecyclerListView.SelectionAdapter {
            public boolean isEnabled(RecyclerView.ViewHolder viewHolder) {
                return true;
            }

            OnlyUserFiltersAdapter() {
            }

            /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v1, resolved type: org.telegram.ui.Cells.DialogCell} */
            /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v3, resolved type: org.telegram.ui.Components.FlickerLoadingView} */
            /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v4, resolved type: org.telegram.ui.Cells.GraySectionCell} */
            /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v5, resolved type: org.telegram.ui.Cells.DialogCell} */
            /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v6, resolved type: org.telegram.ui.Cells.DialogCell} */
            /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v7, resolved type: org.telegram.ui.Cells.DialogCell} */
            /* JADX WARNING: Multi-variable type inference failed */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public androidx.recyclerview.widget.RecyclerView.ViewHolder onCreateViewHolder(android.view.ViewGroup r4, int r5) {
                /*
                    r3 = this;
                    r0 = 1
                    if (r5 == 0) goto L_0x002c
                    r1 = 3
                    if (r5 == r1) goto L_0x001c
                    org.telegram.ui.Cells.GraySectionCell r5 = new org.telegram.ui.Cells.GraySectionCell
                    android.content.Context r4 = r4.getContext()
                    r5.<init>(r4)
                    r4 = 2131627246(0x7f0e0cee, float:1.8881751E38)
                    java.lang.String r0 = "SearchMessages"
                    java.lang.String r4 = org.telegram.messenger.LocaleController.getString(r0, r4)
                    r5.setText(r4)
                    goto L_0x0037
                L_0x001c:
                    org.telegram.ui.Components.FlickerLoadingView r5 = new org.telegram.ui.Components.FlickerLoadingView
                    android.content.Context r4 = r4.getContext()
                    r5.<init>(r4)
                    r5.setIsSingleCell(r0)
                    r5.setViewType(r0)
                    goto L_0x0037
                L_0x002c:
                    org.telegram.ui.Cells.DialogCell r5 = new org.telegram.ui.Cells.DialogCell
                    r1 = 0
                    android.content.Context r4 = r4.getContext()
                    r2 = 0
                    r5.<init>(r1, r4, r0, r2)
                L_0x0037:
                    androidx.recyclerview.widget.RecyclerView$LayoutParams r4 = new androidx.recyclerview.widget.RecyclerView$LayoutParams
                    r0 = -1
                    r1 = -2
                    r4.<init>((int) r0, (int) r1)
                    r5.setLayoutParams(r4)
                    org.telegram.ui.Components.RecyclerListView$Holder r4 = new org.telegram.ui.Components.RecyclerListView$Holder
                    r4.<init>(r5)
                    return r4
                */
                throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.FilteredSearchView.OnlyUserFiltersAdapter.onCreateViewHolder(android.view.ViewGroup, int):androidx.recyclerview.widget.RecyclerView$ViewHolder");
            }

            public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {
                if (viewHolder.getItemViewType() == 0) {
                    final DialogCell dialogCell = (DialogCell) viewHolder.itemView;
                    final MessageObject messageObject = FilteredSearchView.this.messages.get(i);
                    dialogCell.setDialog(messageObject.getDialogId(), messageObject, messageObject.messageOwner.date, false);
                    final boolean z = true;
                    dialogCell.useSeparator = i != getItemCount() - 1;
                    if (dialogCell.getMessage() == null || dialogCell.getMessage().getId() != messageObject.getId()) {
                        z = false;
                    }
                    dialogCell.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                        public boolean onPreDraw() {
                            dialogCell.getViewTreeObserver().removeOnPreDrawListener(this);
                            if (FilteredSearchView.this.uiCallback.actionModeShowing()) {
                                FilteredSearchView.this.messageHashIdTmp.set(messageObject.getId(), messageObject.getDialogId());
                                dialogCell.setChecked(FilteredSearchView.this.uiCallback.isSelected(FilteredSearchView.this.messageHashIdTmp), z);
                                return true;
                            }
                            dialogCell.setChecked(false, z);
                            return true;
                        }
                    });
                }
            }

            public int getItemViewType(int i) {
                return i >= FilteredSearchView.this.messages.size() ? 3 : 0;
            }

            public int getItemCount() {
                if (FilteredSearchView.this.messages.isEmpty()) {
                    return 0;
                }
                return FilteredSearchView.this.messages.size() + (FilteredSearchView.this.endReached ^ true ? 1 : 0);
            }
        }

        /* access modifiers changed from: protected */
        public void onMeasure(int i, int i2) {
            RecyclerView.Adapter adapter2;
            int i3 = this.columnsCount;
            if (AndroidUtilities.isTablet()) {
                this.columnsCount = 3;
            } else if (getResources().getConfiguration().orientation == 2) {
                this.columnsCount = 6;
            } else {
                this.columnsCount = 3;
            }
            if (i3 != this.columnsCount && (adapter2 = this.adapter) == this.sharedPhotoVideoAdapter) {
                this.ignoreRequestLayout = true;
                adapter2.notifyDataSetChanged();
                this.ignoreRequestLayout = false;
            }
            super.onMeasure(i, i2);
        }

        public void requestLayout() {
            if (!this.ignoreRequestLayout) {
                super.requestLayout();
            }
        }

        public void setDelegate(Delegate delegate2, boolean z) {
            this.delegate = delegate2;
            if (z && delegate2 != null && !this.localTipChats.isEmpty()) {
                delegate2.updateFiltersView(false, this.localTipChats, this.localTipDates);
            }
        }

        public void setUiCallback(UiCallback uiCallback2) {
            this.uiCallback = uiCallback2;
        }

        /* access modifiers changed from: private */
        public void showFloatingDateView() {
            AndroidUtilities.cancelRunOnUIThread(this.hideFloatingDateRunnable);
            AndroidUtilities.runOnUIThread(this.hideFloatingDateRunnable, 650);
            if (this.floatingDateView.getTag() == null) {
                AnimatorSet animatorSet = this.floatingDateAnimation;
                if (animatorSet != null) {
                    animatorSet.cancel();
                }
                this.floatingDateView.setTag(1);
                AnimatorSet animatorSet2 = new AnimatorSet();
                this.floatingDateAnimation = animatorSet2;
                animatorSet2.setDuration(180);
                this.floatingDateAnimation.playTogether(new Animator[]{ObjectAnimator.ofFloat(this.floatingDateView, View.ALPHA, new float[]{1.0f}), ObjectAnimator.ofFloat(this.floatingDateView, View.TRANSLATION_Y, new float[]{0.0f})});
                this.floatingDateAnimation.setInterpolator(CubicBezierInterpolator.EASE_OUT);
                this.floatingDateAnimation.addListener(new AnimatorListenerAdapter() {
                    public void onAnimationEnd(Animator animator) {
                        AnimatorSet unused = FilteredSearchView.this.floatingDateAnimation = null;
                    }
                });
                this.floatingDateAnimation.start();
            }
        }

        private void hideFloatingDateView(boolean z) {
            AndroidUtilities.cancelRunOnUIThread(this.hideFloatingDateRunnable);
            if (this.floatingDateView.getTag() != null) {
                this.floatingDateView.setTag((Object) null);
                AnimatorSet animatorSet = this.floatingDateAnimation;
                if (animatorSet != null) {
                    animatorSet.cancel();
                    this.floatingDateAnimation = null;
                }
                if (z) {
                    AnimatorSet animatorSet2 = new AnimatorSet();
                    this.floatingDateAnimation = animatorSet2;
                    animatorSet2.setDuration(180);
                    this.floatingDateAnimation.playTogether(new Animator[]{ObjectAnimator.ofFloat(this.floatingDateView, View.ALPHA, new float[]{0.0f}), ObjectAnimator.ofFloat(this.floatingDateView, View.TRANSLATION_Y, new float[]{(float) (-AndroidUtilities.dp(48.0f))})});
                    this.floatingDateAnimation.setInterpolator(CubicBezierInterpolator.EASE_OUT);
                    this.floatingDateAnimation.addListener(new AnimatorListenerAdapter() {
                        public void onAnimationEnd(Animator animator) {
                            AnimatorSet unused = FilteredSearchView.this.floatingDateAnimation = null;
                        }
                    });
                    this.floatingDateAnimation.start();
                    return;
                }
                this.floatingDateView.setAlpha(0.0f);
            }
        }

        public void setChatPreviewDelegate(SearchViewPager.ChatPreviewDelegate chatPreviewDelegate2) {
            this.chatPreviewDelegate = chatPreviewDelegate2;
        }

        public ArrayList<ThemeDescription> getThemeDescriptions() {
            ArrayList<ThemeDescription> arrayList = new ArrayList<>();
            arrayList.add(new ThemeDescription(this, ThemeDescription.FLAG_BACKGROUND, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundWhite"));
            arrayList.add(new ThemeDescription(this, 0, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "dialogBackground"));
            arrayList.add(new ThemeDescription(this, 0, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundGray"));
            arrayList.add(new ThemeDescription((View) this.recyclerListView, ThemeDescription.FLAG_TEXTCOLOR, new Class[]{SharedDocumentCell.class}, new String[]{"nameTextView"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundWhiteBlackText"));
            arrayList.add(new ThemeDescription((View) this.recyclerListView, ThemeDescription.FLAG_TEXTCOLOR, new Class[]{SharedDocumentCell.class}, new String[]{"dateTextView"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundWhiteGrayText3"));
            arrayList.add(new ThemeDescription((View) this.recyclerListView, ThemeDescription.FLAG_PROGRESSBAR, new Class[]{SharedDocumentCell.class}, new String[]{"progressView"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "sharedMedia_startStopLoadIcon"));
            arrayList.add(new ThemeDescription((View) this.recyclerListView, ThemeDescription.FLAG_IMAGECOLOR, new Class[]{SharedDocumentCell.class}, new String[]{"statusImageView"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "sharedMedia_startStopLoadIcon"));
            arrayList.add(new ThemeDescription((View) this.recyclerListView, ThemeDescription.FLAG_CHECKBOX, new Class[]{SharedDocumentCell.class}, new String[]{"checkBox"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "checkbox"));
            arrayList.add(new ThemeDescription((View) this.recyclerListView, ThemeDescription.FLAG_CHECKBOXCHECK, new Class[]{SharedDocumentCell.class}, new String[]{"checkBox"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "checkboxCheck"));
            arrayList.add(new ThemeDescription((View) this.recyclerListView, ThemeDescription.FLAG_IMAGECOLOR, new Class[]{SharedDocumentCell.class}, new String[]{"thumbImageView"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "files_folderIcon"));
            arrayList.add(new ThemeDescription((View) this.recyclerListView, ThemeDescription.FLAG_TEXTCOLOR, new Class[]{SharedDocumentCell.class}, new String[]{"extTextView"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "files_iconText"));
            arrayList.add(new ThemeDescription((View) this.recyclerListView, 0, new Class[]{LoadingCell.class}, new String[]{"progressBar"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "progressCircle"));
            arrayList.add(new ThemeDescription((View) this.recyclerListView, ThemeDescription.FLAG_CHECKBOX, new Class[]{SharedAudioCell.class}, new String[]{"checkBox"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "checkbox"));
            arrayList.add(new ThemeDescription((View) this.recyclerListView, ThemeDescription.FLAG_CHECKBOXCHECK, new Class[]{SharedAudioCell.class}, new String[]{"checkBox"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "checkboxCheck"));
            RecyclerListView recyclerListView2 = this.recyclerListView;
            RecyclerListView recyclerListView3 = recyclerListView2;
            arrayList.add(new ThemeDescription(recyclerListView3, ThemeDescription.FLAG_TEXTCOLOR, new Class[]{SharedAudioCell.class}, Theme.chat_contextResult_titleTextPaint, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundWhiteBlackText"));
            arrayList.add(new ThemeDescription(this.recyclerListView, ThemeDescription.FLAG_TEXTCOLOR, new Class[]{SharedAudioCell.class}, Theme.chat_contextResult_descriptionTextPaint, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundWhiteGrayText2"));
            arrayList.add(new ThemeDescription((View) this.recyclerListView, ThemeDescription.FLAG_CHECKBOX, new Class[]{SharedLinkCell.class}, new String[]{"checkBox"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "checkbox"));
            arrayList.add(new ThemeDescription((View) this.recyclerListView, ThemeDescription.FLAG_CHECKBOXCHECK, new Class[]{SharedLinkCell.class}, new String[]{"checkBox"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "checkboxCheck"));
            arrayList.add(new ThemeDescription((View) this.recyclerListView, 0, new Class[]{SharedLinkCell.class}, new String[]{"titleTextPaint"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundWhiteBlackText"));
            arrayList.add(new ThemeDescription(this.recyclerListView, 0, new Class[]{SharedLinkCell.class}, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundWhiteLinkText"));
            arrayList.add(new ThemeDescription(this.recyclerListView, 0, new Class[]{SharedLinkCell.class}, Theme.linkSelectionPaint, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundWhiteLinkSelection"));
            arrayList.add(new ThemeDescription((View) this.recyclerListView, 0, new Class[]{SharedLinkCell.class}, new String[]{"letterDrawable"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "sharedMedia_linkPlaceholderText"));
            arrayList.add(new ThemeDescription((View) this.recyclerListView, ThemeDescription.FLAG_BACKGROUNDFILTER, new Class[]{SharedLinkCell.class}, new String[]{"letterDrawable"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "sharedMedia_linkPlaceholder"));
            arrayList.add(new ThemeDescription(this.recyclerListView, ThemeDescription.FLAG_CELLBACKGROUNDCOLOR | ThemeDescription.FLAG_SECTIONS, new Class[]{SharedMediaSectionCell.class}, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundWhite"));
            arrayList.add(new ThemeDescription((View) this.recyclerListView, ThemeDescription.FLAG_SECTIONS, new Class[]{SharedMediaSectionCell.class}, new String[]{"textView"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundWhiteBlackText"));
            arrayList.add(new ThemeDescription((View) this.recyclerListView, 0, new Class[]{SharedMediaSectionCell.class}, new String[]{"textView"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundWhiteBlackText"));
            arrayList.add(new ThemeDescription(this.recyclerListView, 0, new Class[]{DialogCell.class, ProfileSearchCell.class}, (Paint) null, Theme.avatarDrawables, (ThemeDescription.ThemeDescriptionDelegate) null, "avatar_text"));
            arrayList.add(new ThemeDescription(this.recyclerListView, 0, new Class[]{DialogCell.class}, Theme.dialogs_countPaint, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "chats_unreadCounter"));
            arrayList.add(new ThemeDescription(this.recyclerListView, 0, new Class[]{DialogCell.class}, Theme.dialogs_countGrayPaint, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "chats_unreadCounterMuted"));
            arrayList.add(new ThemeDescription(this.recyclerListView, 0, new Class[]{DialogCell.class}, Theme.dialogs_countTextPaint, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "chats_unreadCounterText"));
            arrayList.add(new ThemeDescription(this.recyclerListView, 0, new Class[]{DialogCell.class, ProfileSearchCell.class}, (Paint) null, new Drawable[]{Theme.dialogs_lockDrawable}, (ThemeDescription.ThemeDescriptionDelegate) null, "chats_secretIcon"));
            arrayList.add(new ThemeDescription(this.recyclerListView, 0, new Class[]{DialogCell.class, ProfileSearchCell.class}, (Paint) null, new Drawable[]{Theme.dialogs_groupDrawable, Theme.dialogs_broadcastDrawable, Theme.dialogs_botDrawable}, (ThemeDescription.ThemeDescriptionDelegate) null, "chats_nameIcon"));
            arrayList.add(new ThemeDescription(this.recyclerListView, 0, new Class[]{DialogCell.class, ProfileSearchCell.class}, (Paint) null, new Drawable[]{Theme.dialogs_scamDrawable, Theme.dialogs_fakeDrawable}, (ThemeDescription.ThemeDescriptionDelegate) null, "chats_draft"));
            arrayList.add(new ThemeDescription(this.recyclerListView, 0, new Class[]{DialogCell.class}, (Paint) null, new Drawable[]{Theme.dialogs_pinnedDrawable, Theme.dialogs_reorderDrawable}, (ThemeDescription.ThemeDescriptionDelegate) null, "chats_pinnedIcon"));
            TextPaint[] textPaintArr = Theme.dialogs_namePaint;
            arrayList.add(new ThemeDescription((View) this.recyclerListView, 0, new Class[]{DialogCell.class, ProfileSearchCell.class}, (String[]) null, new Paint[]{textPaintArr[0], textPaintArr[1], Theme.dialogs_searchNamePaint}, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "chats_name"));
            TextPaint[] textPaintArr2 = Theme.dialogs_nameEncryptedPaint;
            arrayList.add(new ThemeDescription((View) this.recyclerListView, 0, new Class[]{DialogCell.class, ProfileSearchCell.class}, (String[]) null, new Paint[]{textPaintArr2[0], textPaintArr2[1], Theme.dialogs_searchNameEncryptedPaint}, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "chats_secretName"));
            arrayList.add(new ThemeDescription(this.recyclerListView, 0, new Class[]{DialogCell.class}, Theme.dialogs_messagePaint[1], (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "chats_message_threeLines"));
            arrayList.add(new ThemeDescription(this.recyclerListView, 0, new Class[]{DialogCell.class}, Theme.dialogs_messagePaint[0], (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "chats_message"));
            arrayList.add(new ThemeDescription(this.recyclerListView, 0, new Class[]{DialogCell.class}, Theme.dialogs_messageNamePaint, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "chats_nameMessage_threeLines"));
            arrayList.add(new ThemeDescription(this.recyclerListView, 0, new Class[]{DialogCell.class}, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "chats_draft"));
            arrayList.add(new ThemeDescription((View) this.recyclerListView, 0, new Class[]{DialogCell.class}, (String[]) null, (Paint[]) Theme.dialogs_messagePrintingPaint, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "chats_actionMessage"));
            arrayList.add(new ThemeDescription(this.recyclerListView, 0, new Class[]{DialogCell.class}, Theme.dialogs_timePaint, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "chats_date"));
            arrayList.add(new ThemeDescription(this.recyclerListView, 0, new Class[]{DialogCell.class}, Theme.dialogs_pinnedPaint, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "chats_pinnedOverlay"));
            arrayList.add(new ThemeDescription(this.recyclerListView, 0, new Class[]{DialogCell.class}, Theme.dialogs_tabletSeletedPaint, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "chats_tabletSelectedOverlay"));
            arrayList.add(new ThemeDescription(this.recyclerListView, 0, new Class[]{DialogCell.class}, (Paint) null, new Drawable[]{Theme.dialogs_checkDrawable}, (ThemeDescription.ThemeDescriptionDelegate) null, "chats_sentCheck"));
            arrayList.add(new ThemeDescription(this.recyclerListView, 0, new Class[]{DialogCell.class}, (Paint) null, new Drawable[]{Theme.dialogs_checkReadDrawable, Theme.dialogs_halfCheckDrawable}, (ThemeDescription.ThemeDescriptionDelegate) null, "chats_sentReadCheck"));
            arrayList.add(new ThemeDescription(this.recyclerListView, 0, new Class[]{DialogCell.class}, (Paint) null, new Drawable[]{Theme.dialogs_clockDrawable}, (ThemeDescription.ThemeDescriptionDelegate) null, "chats_sentClock"));
            arrayList.add(new ThemeDescription(this.recyclerListView, 0, new Class[]{DialogCell.class}, Theme.dialogs_errorPaint, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "chats_sentError"));
            arrayList.add(new ThemeDescription(this.recyclerListView, 0, new Class[]{DialogCell.class}, (Paint) null, new Drawable[]{Theme.dialogs_errorDrawable}, (ThemeDescription.ThemeDescriptionDelegate) null, "chats_sentErrorIcon"));
            arrayList.add(new ThemeDescription(this.recyclerListView, 0, new Class[]{DialogCell.class, ProfileSearchCell.class}, (Paint) null, new Drawable[]{Theme.dialogs_verifiedCheckDrawable}, (ThemeDescription.ThemeDescriptionDelegate) null, "chats_verifiedCheck"));
            arrayList.add(new ThemeDescription(this.recyclerListView, 0, new Class[]{DialogCell.class, ProfileSearchCell.class}, (Paint) null, new Drawable[]{Theme.dialogs_verifiedDrawable}, (ThemeDescription.ThemeDescriptionDelegate) null, "chats_verifiedBackground"));
            arrayList.add(new ThemeDescription(this.recyclerListView, 0, new Class[]{DialogCell.class}, (Paint) null, new Drawable[]{Theme.dialogs_muteDrawable}, (ThemeDescription.ThemeDescriptionDelegate) null, "chats_muteIcon"));
            arrayList.add(new ThemeDescription(this.recyclerListView, 0, new Class[]{DialogCell.class}, (Paint) null, new Drawable[]{Theme.dialogs_mentionDrawable}, (ThemeDescription.ThemeDescriptionDelegate) null, "chats_mentionIcon"));
            arrayList.add(new ThemeDescription(this.recyclerListView, 0, new Class[]{DialogCell.class}, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "chats_archivePinBackground"));
            arrayList.add(new ThemeDescription(this.recyclerListView, 0, new Class[]{DialogCell.class}, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "chats_archiveBackground"));
            arrayList.add(new ThemeDescription(this.recyclerListView, 0, new Class[]{DialogCell.class}, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "chats_onlineCircle"));
            arrayList.add(new ThemeDescription(this.recyclerListView, 0, new Class[]{DialogCell.class}, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundWhite"));
            arrayList.add(new ThemeDescription((View) this.recyclerListView, ThemeDescription.FLAG_CHECKBOX, new Class[]{DialogCell.class}, new String[]{"checkBox"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundWhite"));
            arrayList.add(new ThemeDescription((View) this.recyclerListView, ThemeDescription.FLAG_CHECKBOXCHECK, new Class[]{DialogCell.class}, new String[]{"checkBox"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "checkboxCheck"));
            arrayList.add(new ThemeDescription((View) this.recyclerListView, ThemeDescription.FLAG_SECTIONS, new Class[]{GraySectionCell.class}, new String[]{"textView"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "key_graySectionText"));
            arrayList.add(new ThemeDescription(this.recyclerListView, ThemeDescription.FLAG_CELLBACKGROUNDCOLOR | ThemeDescription.FLAG_SECTIONS, new Class[]{GraySectionCell.class}, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "graySection"));
            arrayList.add(new ThemeDescription(this.emptyView.title, ThemeDescription.FLAG_TEXTCOLOR, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundWhiteBlackText"));
            arrayList.add(new ThemeDescription(this.emptyView.subtitle, ThemeDescription.FLAG_TEXTCOLOR, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundWhiteGrayText"));
            return arrayList;
        }
    }
