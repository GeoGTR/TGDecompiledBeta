package org.telegram.ui.Components;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import androidx.viewpager.widget.ViewPager;
import java.util.ArrayList;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.ImageLocation;
import org.telegram.messenger.ImageReceiver;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.TLRPC$ChatFull;
import org.telegram.tgnet.TLRPC$Photo;
import org.telegram.tgnet.TLRPC$TL_fileLocationToBeDeprecated;
import org.telegram.tgnet.TLRPC$TL_photoStrippedSize;
import org.telegram.tgnet.TLRPC$VideoSize;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.Components.CircularViewPager;
import org.telegram.ui.ProfileActivity;

public class ProfileGalleryView extends CircularViewPager implements NotificationCenter.NotificationCenterDelegate {
    /* access modifiers changed from: private */
    public final ViewPagerAdapter adapter;
    /* access modifiers changed from: private */
    public final Callback callback;
    private TLRPC$ChatFull chatInfo;
    /* access modifiers changed from: private */
    public int currentAccount = UserConfig.selectedAccount;
    /* access modifiers changed from: private */
    public final long dialogId;
    private final PointF downPoint = new PointF();
    /* access modifiers changed from: private */
    public ArrayList<ImageLocation> imagesLocations = new ArrayList<>();
    /* access modifiers changed from: private */
    public ArrayList<Integer> imagesLocationsSizes = new ArrayList<>();
    private boolean isDownReleased;
    private boolean isScrollingListView = true;
    private boolean isSwipingViewPager = true;
    private final int parentClassGuid;
    private final RecyclerListView parentListView;
    private ArrayList<TLRPC$Photo> photos = new ArrayList<>();
    private ImageLocation prevImageLocation;
    /* access modifiers changed from: private */
    public final SparseArray<RadialProgress2> radialProgresses = new SparseArray<>();
    private boolean scrolledByUser;
    private int settingMainPhoto;
    private ArrayList<String> thumbsFileNames = new ArrayList<>();
    /* access modifiers changed from: private */
    public ArrayList<ImageLocation> thumbsLocations = new ArrayList<>();
    private final int touchSlop;
    private ArrayList<String> videoFileNames = new ArrayList<>();
    /* access modifiers changed from: private */
    public ArrayList<ImageLocation> videoLocations = new ArrayList<>();

    public interface Callback {
        void onDown(boolean z);

        void onPhotosLoaded();

        void onRelease();

        void onVideoSet();
    }

    private static class Item {
        /* access modifiers changed from: private */
        public BackupImageView imageView;

        private Item() {
        }
    }

    public ProfileGalleryView(Context context, long j, ActionBar actionBar, RecyclerListView recyclerListView, ProfileActivity.AvatarImageView avatarImageView, int i, Callback callback2) {
        super(context);
        setVisibility(8);
        setOverScrollMode(2);
        setOffscreenPageLimit(2);
        this.dialogId = j;
        this.parentListView = recyclerListView;
        this.parentClassGuid = i;
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(context, avatarImageView, actionBar);
        this.adapter = viewPagerAdapter;
        setAdapter((CircularViewPager.Adapter) viewPagerAdapter);
        this.touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        this.callback = callback2;
        addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            public void onPageScrollStateChanged(int i) {
            }

            public void onPageSelected(int i) {
            }

            public void onPageScrolled(int i, float f, int i2) {
                ImageLocation imageLocation;
                if (i2 == 0) {
                    int realPosition = ProfileGalleryView.this.adapter.getRealPosition(i);
                    ProfileGalleryView.this.getCurrentItemView();
                    int childCount = ProfileGalleryView.this.getChildCount();
                    for (int i3 = 0; i3 < childCount; i3++) {
                        View childAt = ProfileGalleryView.this.getChildAt(i3);
                        if (childAt instanceof BackupImageView) {
                            int realPosition2 = ProfileGalleryView.this.adapter.getRealPosition(ProfileGalleryView.this.adapter.imageViews.indexOf(childAt));
                            ImageReceiver imageReceiver = ((BackupImageView) childAt).getImageReceiver();
                            boolean allowStartAnimation = imageReceiver.getAllowStartAnimation();
                            if (realPosition2 == realPosition) {
                                if (!allowStartAnimation) {
                                    imageReceiver.setAllowStartAnimation(true);
                                    imageReceiver.startAnimation();
                                }
                                ImageLocation imageLocation2 = (ImageLocation) ProfileGalleryView.this.videoLocations.get(realPosition2);
                                if (imageLocation2 != null) {
                                    FileLoader.getInstance(ProfileGalleryView.this.currentAccount).setForceStreamLoadingFile(imageLocation2.location, "mp4");
                                }
                            } else if (allowStartAnimation) {
                                AnimatedFileDrawable animation = imageReceiver.getAnimation();
                                if (!(animation == null || (imageLocation = (ImageLocation) ProfileGalleryView.this.videoLocations.get(realPosition2)) == null)) {
                                    animation.seekTo(imageLocation.videoSeekTo, false, true);
                                }
                                imageReceiver.setAllowStartAnimation(false);
                                imageReceiver.stopAnimation();
                            }
                        }
                    }
                }
            }
        });
        NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.dialogPhotosLoaded);
        NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.fileDidLoad);
        NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.FileLoadProgressChanged);
        NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.reloadDialogPhotos);
        MessagesController.getInstance(this.currentAccount).loadDialogPhotos((int) j, 80, 0, true, i);
    }

    public void onDestroy() {
        NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.dialogPhotosLoaded);
        NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.fileDidLoad);
        NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.FileLoadProgressChanged);
        NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.reloadDialogPhotos);
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childAt = getChildAt(i);
            if (childAt instanceof BackupImageView) {
                BackupImageView backupImageView = (BackupImageView) childAt;
                if (backupImageView.getImageReceiver().hasStaticThumb()) {
                    Drawable drawable = backupImageView.getImageReceiver().getDrawable();
                    if (drawable instanceof AnimatedFileDrawable) {
                        ((AnimatedFileDrawable) drawable).removeSecondParentView(backupImageView);
                    }
                }
            }
        }
    }

    public void setAnimatedFileMaybe(AnimatedFileDrawable animatedFileDrawable) {
        if (animatedFileDrawable != null) {
            int childCount = getChildCount();
            for (int i = 0; i < childCount; i++) {
                View childAt = getChildAt(i);
                if (childAt instanceof BackupImageView) {
                    ViewPagerAdapter viewPagerAdapter = this.adapter;
                    if (viewPagerAdapter.getRealPosition(viewPagerAdapter.imageViews.indexOf(childAt)) == 0) {
                        BackupImageView backupImageView = (BackupImageView) childAt;
                        AnimatedFileDrawable animation = backupImageView.getImageReceiver().getAnimation();
                        if (animation != animatedFileDrawable) {
                            if (animation != null) {
                                animation.removeSecondParentView(backupImageView);
                            }
                            backupImageView.setImageDrawable(animatedFileDrawable);
                            animatedFileDrawable.addSecondParentView(this);
                            animatedFileDrawable.setInvalidateParentViewWithSecond(true);
                        }
                    }
                }
            }
        }
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        int i;
        if (this.parentListView.getScrollState() == 0 || this.isScrollingListView || !this.isSwipingViewPager) {
            int action = motionEvent.getAction();
            if (action == 0) {
                this.isScrollingListView = true;
                this.isSwipingViewPager = true;
                this.scrolledByUser = true;
                this.downPoint.set(motionEvent.getX(), motionEvent.getY());
                if (this.adapter.getCount() > 1) {
                    this.callback.onDown(motionEvent.getX() < ((float) getWidth()) / 3.0f);
                }
                this.isDownReleased = false;
            } else if (action == 1) {
                if (!this.isDownReleased) {
                    int count = this.adapter.getCount();
                    int currentItem = getCurrentItem();
                    if (count > 1) {
                        if (motionEvent.getX() > ((float) getWidth()) / 3.0f) {
                            i = this.adapter.getExtraCount();
                            int i2 = currentItem + 1;
                            if (i2 < count - i) {
                                i = i2;
                            }
                        } else {
                            int extraCount = this.adapter.getExtraCount();
                            int i3 = -1 + currentItem;
                            i = i3 < extraCount ? (count - extraCount) - 1 : i3;
                        }
                        this.callback.onRelease();
                        setCurrentItem(i, false);
                    }
                }
            } else if (action == 2) {
                float x = motionEvent.getX() - this.downPoint.x;
                float y = motionEvent.getY() - this.downPoint.y;
                boolean z = Math.abs(y) >= ((float) this.touchSlop) || Math.abs(x) >= ((float) this.touchSlop);
                if (z) {
                    this.isDownReleased = true;
                    this.callback.onRelease();
                }
                boolean z2 = this.isSwipingViewPager;
                if (!z2 || !this.isScrollingListView) {
                    if (z2 && !canScrollHorizontally(-1) && x > ((float) this.touchSlop)) {
                        return false;
                    }
                } else if (z) {
                    if (Math.abs(y) > Math.abs(x)) {
                        this.isSwipingViewPager = false;
                        MotionEvent obtain = MotionEvent.obtain(motionEvent);
                        obtain.setAction(3);
                        super.onTouchEvent(obtain);
                        obtain.recycle();
                    } else {
                        this.isScrollingListView = false;
                        MotionEvent obtain2 = MotionEvent.obtain(motionEvent);
                        obtain2.setAction(3);
                        this.parentListView.onTouchEvent(obtain2);
                        obtain2.recycle();
                    }
                }
            }
            boolean onTouchEvent = this.isScrollingListView ? this.parentListView.onTouchEvent(motionEvent) : false;
            if (this.isSwipingViewPager) {
                onTouchEvent |= super.onTouchEvent(motionEvent);
            }
            if (action == 1 || action == 3) {
                this.isScrollingListView = false;
                this.isSwipingViewPager = false;
            }
            return onTouchEvent;
        }
        this.isSwipingViewPager = false;
        MotionEvent obtain3 = MotionEvent.obtain(motionEvent);
        obtain3.setAction(3);
        super.onTouchEvent(obtain3);
        obtain3.recycle();
        return false;
    }

    public void setChatInfo(TLRPC$ChatFull tLRPC$ChatFull) {
        this.chatInfo = tLRPC$ChatFull;
        if (!this.photos.isEmpty() && this.photos.get(0) == null && this.chatInfo != null && FileLoader.isSamePhoto(this.imagesLocations.get(0).location, this.chatInfo.chat_photo)) {
            this.photos.set(0, this.chatInfo.chat_photo);
            if (!this.chatInfo.chat_photo.video_sizes.isEmpty()) {
                TLRPC$VideoSize tLRPC$VideoSize = this.chatInfo.chat_photo.video_sizes.get(0);
                this.videoLocations.set(0, ImageLocation.getForPhoto(tLRPC$VideoSize, this.chatInfo.chat_photo));
                this.videoFileNames.set(0, FileLoader.getAttachFileName(tLRPC$VideoSize));
                this.callback.onPhotosLoaded();
            } else {
                this.videoLocations.set(0, (Object) null);
                this.videoFileNames.add(0, (Object) null);
            }
            this.adapter.notifyDataSetChanged();
        }
    }

    public boolean initIfEmpty(ImageLocation imageLocation, ImageLocation imageLocation2) {
        if (imageLocation == null || imageLocation2 == null || this.settingMainPhoto != 0) {
            return false;
        }
        ImageLocation imageLocation3 = this.prevImageLocation;
        if (imageLocation3 == null || imageLocation3.location.local_id != imageLocation.location.local_id) {
            this.imagesLocations.clear();
            MessagesController.getInstance(this.currentAccount).loadDialogPhotos((int) this.dialogId, 80, 0, true, this.parentClassGuid);
        }
        if (!this.imagesLocations.isEmpty()) {
            return false;
        }
        this.prevImageLocation = imageLocation;
        this.thumbsFileNames.add((Object) null);
        this.videoFileNames.add((Object) null);
        this.imagesLocations.add(imageLocation);
        this.thumbsLocations.add(imageLocation2);
        this.videoLocations.add((Object) null);
        this.photos.add((Object) null);
        this.imagesLocationsSizes.add(-1);
        getAdapter().notifyDataSetChanged();
        return true;
    }

    public ImageLocation getImageLocation(int i) {
        if (i < 0 || i >= this.imagesLocations.size()) {
            return null;
        }
        ImageLocation imageLocation = this.videoLocations.get(i);
        if (imageLocation != null) {
            return imageLocation;
        }
        return this.imagesLocations.get(i);
    }

    public ImageLocation getRealImageLocation(int i) {
        if (i < 0 || i >= this.imagesLocations.size()) {
            return null;
        }
        return this.imagesLocations.get(i);
    }

    public boolean hasImages() {
        return !this.imagesLocations.isEmpty();
    }

    public BackupImageView getCurrentItemView() {
        ViewPagerAdapter viewPagerAdapter = this.adapter;
        if (viewPagerAdapter == null || viewPagerAdapter.objects.isEmpty()) {
            return null;
        }
        return ((Item) this.adapter.objects.get(getCurrentItem())).imageView;
    }

    public boolean isLoadingCurrentVideo() {
        BackupImageView currentItemView;
        if (this.videoLocations.get(getRealPosition()) == null || (currentItemView = getCurrentItemView()) == null) {
            return false;
        }
        AnimatedFileDrawable animation = currentItemView.getImageReceiver().getAnimation();
        if (animation == null || !animation.hasBitmap()) {
            return true;
        }
        return false;
    }

    public float getCurrentItemProgress() {
        AnimatedFileDrawable animation;
        BackupImageView currentItemView = getCurrentItemView();
        if (currentItemView == null || (animation = currentItemView.getImageReceiver().getAnimation()) == null) {
            return 0.0f;
        }
        return animation.getCurrentProgress();
    }

    public boolean isCurrentItemVideo() {
        return this.videoLocations.get(getRealPosition()) != null;
    }

    public ImageLocation getCurrentVideoLocation(ImageLocation imageLocation, ImageLocation imageLocation2) {
        TLRPC$TL_fileLocationToBeDeprecated tLRPC$TL_fileLocationToBeDeprecated;
        if (imageLocation == null) {
            return null;
        }
        int i = 0;
        while (i < 2) {
            ArrayList<ImageLocation> arrayList = i == 0 ? this.thumbsLocations : this.imagesLocations;
            int size = arrayList.size();
            for (int i2 = 0; i2 < size; i2++) {
                ImageLocation imageLocation3 = arrayList.get(i2);
                if (!(imageLocation3 == null || (tLRPC$TL_fileLocationToBeDeprecated = imageLocation3.location) == null)) {
                    int i3 = imageLocation3.dc_id;
                    if (i3 == imageLocation.dc_id) {
                        int i4 = tLRPC$TL_fileLocationToBeDeprecated.local_id;
                        TLRPC$TL_fileLocationToBeDeprecated tLRPC$TL_fileLocationToBeDeprecated2 = imageLocation.location;
                        if (i4 == tLRPC$TL_fileLocationToBeDeprecated2.local_id && tLRPC$TL_fileLocationToBeDeprecated.volume_id == tLRPC$TL_fileLocationToBeDeprecated2.volume_id) {
                            return this.videoLocations.get(i2);
                        }
                    }
                    if (i3 == imageLocation2.dc_id) {
                        int i5 = tLRPC$TL_fileLocationToBeDeprecated.local_id;
                        TLRPC$TL_fileLocationToBeDeprecated tLRPC$TL_fileLocationToBeDeprecated3 = imageLocation2.location;
                        if (i5 == tLRPC$TL_fileLocationToBeDeprecated3.local_id && tLRPC$TL_fileLocationToBeDeprecated.volume_id == tLRPC$TL_fileLocationToBeDeprecated3.volume_id) {
                            return this.videoLocations.get(i2);
                        }
                    } else {
                        continue;
                    }
                }
            }
            i++;
        }
        return null;
    }

    public void resetCurrentItem() {
        setCurrentItem(this.adapter.getExtraCount(), false);
    }

    public int getRealCount() {
        return this.photos.size();
    }

    public int getRealPosition(int i) {
        return this.adapter.getRealPosition(i);
    }

    public int getRealPosition() {
        return this.adapter.getRealPosition(getCurrentItem());
    }

    public TLRPC$Photo getPhoto(int i) {
        if (i < 0 || i >= this.photos.size()) {
            return null;
        }
        return this.photos.get(i);
    }

    public void replaceFirstPhoto(TLRPC$Photo tLRPC$Photo, TLRPC$Photo tLRPC$Photo2) {
        int indexOf;
        if (!this.photos.isEmpty() && (indexOf = this.photos.indexOf(tLRPC$Photo)) >= 0) {
            this.photos.set(indexOf, tLRPC$Photo2);
        }
    }

    public void finishSettingMainPhoto() {
        this.settingMainPhoto--;
    }

    public void startMovePhotoToBegin(int i) {
        if (i > 0 && i < this.photos.size()) {
            this.settingMainPhoto++;
            this.photos.remove(i);
            this.photos.add(0, this.photos.get(i));
            this.thumbsFileNames.remove(i);
            this.thumbsFileNames.add(0, this.thumbsFileNames.get(i));
            ArrayList<String> arrayList = this.videoFileNames;
            arrayList.add(0, arrayList.remove(i));
            this.videoLocations.remove(i);
            this.videoLocations.add(0, this.videoLocations.get(i));
            this.imagesLocations.remove(i);
            this.imagesLocations.add(0, this.imagesLocations.get(i));
            this.thumbsLocations.remove(i);
            this.thumbsLocations.add(0, this.thumbsLocations.get(i));
            this.imagesLocationsSizes.remove(i);
            this.imagesLocationsSizes.add(0, this.imagesLocationsSizes.get(i));
            this.prevImageLocation = this.imagesLocations.get(0);
        }
    }

    public void commitMoveToBegin() {
        this.adapter.notifyDataSetChanged();
        resetCurrentItem();
    }

    public boolean removePhotoAtIndex(int i) {
        if (i < 0 || i >= this.photos.size()) {
            return false;
        }
        this.photos.remove(i);
        this.thumbsFileNames.remove(i);
        this.videoFileNames.remove(i);
        this.videoLocations.remove(i);
        this.imagesLocations.remove(i);
        this.thumbsLocations.remove(i);
        this.imagesLocationsSizes.remove(i);
        this.radialProgresses.delete(i);
        if (i == 0 && !this.imagesLocations.isEmpty()) {
            this.prevImageLocation = this.imagesLocations.get(0);
        }
        this.adapter.notifyDataSetChanged();
        return this.photos.isEmpty();
    }

    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        if (this.parentListView.getScrollState() != 0) {
            return false;
        }
        if (!(getParent() == null || getParent().getParent() == null)) {
            getParent().getParent().requestDisallowInterceptTouchEvent(canScrollHorizontally(-1));
        }
        return super.onInterceptTouchEvent(motionEvent);
    }

    private void loadNeighboringThumbs() {
        int size = this.thumbsLocations.size();
        if (size > 1) {
            int i = 0;
            while (true) {
                int i2 = 2;
                if (size <= 2) {
                    i2 = 1;
                }
                if (i < i2) {
                    FileLoader.getInstance(this.currentAccount).loadFile(this.thumbsLocations.get(i == 0 ? 1 : size - 1), (Object) null, (String) null, 0, 1);
                    i++;
                } else {
                    return;
                }
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:51:0x0174, code lost:
        if (r3 != false) goto L_0x01f2;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void didReceivedNotification(int r17, int r18, java.lang.Object... r19) {
        /*
            r16 = this;
            r0 = r16
            r1 = r17
            int r2 = org.telegram.messenger.NotificationCenter.dialogPhotosLoaded
            r3 = 2
            r4 = 1
            r5 = 0
            if (r1 != r2) goto L_0x0227
            r1 = 3
            r1 = r19[r1]
            java.lang.Integer r1 = (java.lang.Integer) r1
            int r1 = r1.intValue()
            r2 = r19[r5]
            java.lang.Integer r2 = (java.lang.Integer) r2
            int r7 = r2.intValue()
            long r8 = (long) r7
            long r10 = r0.dialogId
            int r2 = (r8 > r10 ? 1 : (r8 == r10 ? 0 : -1))
            if (r2 != 0) goto L_0x02d4
            int r2 = r0.parentClassGuid
            if (r2 != r1) goto L_0x02d4
            r1 = r19[r3]
            java.lang.Boolean r1 = (java.lang.Boolean) r1
            boolean r1 = r1.booleanValue()
            r2 = 4
            r2 = r19[r2]
            java.util.ArrayList r2 = (java.util.ArrayList) r2
            java.util.ArrayList<java.lang.String> r3 = r0.thumbsFileNames
            r3.clear()
            java.util.ArrayList<java.lang.String> r3 = r0.videoFileNames
            r3.clear()
            java.util.ArrayList<org.telegram.messenger.ImageLocation> r3 = r0.imagesLocations
            r3.clear()
            java.util.ArrayList<org.telegram.messenger.ImageLocation> r3 = r0.videoLocations
            r3.clear()
            java.util.ArrayList<org.telegram.messenger.ImageLocation> r3 = r0.thumbsLocations
            r3.clear()
            java.util.ArrayList<org.telegram.tgnet.TLRPC$Photo> r3 = r0.photos
            r3.clear()
            java.util.ArrayList<java.lang.Integer> r3 = r0.imagesLocationsSizes
            r3.clear()
            r3 = 0
            if (r7 >= 0) goto L_0x00ed
            int r6 = r0.currentAccount
            org.telegram.messenger.MessagesController r6 = org.telegram.messenger.MessagesController.getInstance(r6)
            int r8 = -r7
            java.lang.Integer r8 = java.lang.Integer.valueOf(r8)
            org.telegram.tgnet.TLRPC$Chat r6 = r6.getChat(r8)
            org.telegram.messenger.ImageLocation r8 = org.telegram.messenger.ImageLocation.getForChat(r6, r4)
            if (r8 == 0) goto L_0x00ee
            java.util.ArrayList<org.telegram.messenger.ImageLocation> r9 = r0.imagesLocations
            r9.add(r8)
            java.util.ArrayList<org.telegram.messenger.ImageLocation> r9 = r0.thumbsLocations
            org.telegram.messenger.ImageLocation r6 = org.telegram.messenger.ImageLocation.getForChat(r6, r5)
            r9.add(r6)
            java.util.ArrayList<java.lang.String> r6 = r0.thumbsFileNames
            r6.add(r3)
            org.telegram.tgnet.TLRPC$ChatFull r6 = r0.chatInfo
            if (r6 == 0) goto L_0x00d3
            org.telegram.tgnet.TLRPC$TL_fileLocationToBeDeprecated r9 = r8.location
            org.telegram.tgnet.TLRPC$Photo r6 = r6.chat_photo
            boolean r6 = org.telegram.messenger.FileLoader.isSamePhoto(r9, r6)
            if (r6 == 0) goto L_0x00d3
            java.util.ArrayList<org.telegram.tgnet.TLRPC$Photo> r6 = r0.photos
            org.telegram.tgnet.TLRPC$ChatFull r9 = r0.chatInfo
            org.telegram.tgnet.TLRPC$Photo r9 = r9.chat_photo
            r6.add(r9)
            org.telegram.tgnet.TLRPC$ChatFull r6 = r0.chatInfo
            org.telegram.tgnet.TLRPC$Photo r6 = r6.chat_photo
            java.util.ArrayList<org.telegram.tgnet.TLRPC$VideoSize> r6 = r6.video_sizes
            boolean r6 = r6.isEmpty()
            if (r6 != 0) goto L_0x00c8
            org.telegram.tgnet.TLRPC$ChatFull r6 = r0.chatInfo
            org.telegram.tgnet.TLRPC$Photo r6 = r6.chat_photo
            java.util.ArrayList<org.telegram.tgnet.TLRPC$VideoSize> r6 = r6.video_sizes
            java.lang.Object r6 = r6.get(r5)
            org.telegram.tgnet.TLRPC$VideoSize r6 = (org.telegram.tgnet.TLRPC$VideoSize) r6
            java.util.ArrayList<org.telegram.messenger.ImageLocation> r9 = r0.videoLocations
            org.telegram.tgnet.TLRPC$ChatFull r10 = r0.chatInfo
            org.telegram.tgnet.TLRPC$Photo r10 = r10.chat_photo
            org.telegram.messenger.ImageLocation r10 = org.telegram.messenger.ImageLocation.getForPhoto((org.telegram.tgnet.TLRPC$VideoSize) r6, (org.telegram.tgnet.TLRPC$Photo) r10)
            r9.add(r10)
            java.util.ArrayList<java.lang.String> r9 = r0.videoFileNames
            java.lang.String r6 = org.telegram.messenger.FileLoader.getAttachFileName(r6)
            r9.add(r6)
            goto L_0x00e2
        L_0x00c8:
            java.util.ArrayList<org.telegram.messenger.ImageLocation> r6 = r0.videoLocations
            r6.add(r3)
            java.util.ArrayList<java.lang.String> r6 = r0.videoFileNames
            r6.add(r3)
            goto L_0x00e2
        L_0x00d3:
            java.util.ArrayList<org.telegram.tgnet.TLRPC$Photo> r6 = r0.photos
            r6.add(r3)
            java.util.ArrayList<java.lang.String> r6 = r0.videoFileNames
            r6.add(r3)
            java.util.ArrayList<org.telegram.messenger.ImageLocation> r6 = r0.videoLocations
            r6.add(r3)
        L_0x00e2:
            java.util.ArrayList<java.lang.Integer> r6 = r0.imagesLocationsSizes
            r9 = -1
            java.lang.Integer r9 = java.lang.Integer.valueOf(r9)
            r6.add(r9)
            goto L_0x00ee
        L_0x00ed:
            r8 = r3
        L_0x00ee:
            r6 = 0
        L_0x00ef:
            int r9 = r2.size()
            if (r6 >= r9) goto L_0x01fb
            java.lang.Object r9 = r2.get(r6)
            org.telegram.tgnet.TLRPC$Photo r9 = (org.telegram.tgnet.TLRPC$Photo) r9
            if (r9 == 0) goto L_0x01f4
            boolean r10 = r9 instanceof org.telegram.tgnet.TLRPC$TL_photoEmpty
            if (r10 != 0) goto L_0x01f4
            java.util.ArrayList<org.telegram.tgnet.TLRPC$PhotoSize> r10 = r9.sizes
            if (r10 != 0) goto L_0x0107
            goto L_0x01f4
        L_0x0107:
            r11 = 50
            org.telegram.tgnet.TLRPC$PhotoSize r10 = org.telegram.messenger.FileLoader.getClosestPhotoSizeWithSize(r10, r11)
            java.util.ArrayList<org.telegram.tgnet.TLRPC$PhotoSize> r11 = r9.sizes
            int r11 = r11.size()
            r12 = 0
        L_0x0114:
            if (r12 >= r11) goto L_0x0127
            java.util.ArrayList<org.telegram.tgnet.TLRPC$PhotoSize> r13 = r9.sizes
            java.lang.Object r13 = r13.get(r12)
            org.telegram.tgnet.TLRPC$PhotoSize r13 = (org.telegram.tgnet.TLRPC$PhotoSize) r13
            boolean r14 = r13 instanceof org.telegram.tgnet.TLRPC$TL_photoStrippedSize
            if (r14 == 0) goto L_0x0124
            r10 = r13
            goto L_0x0127
        L_0x0124:
            int r12 = r12 + 1
            goto L_0x0114
        L_0x0127:
            if (r8 == 0) goto L_0x0178
            java.util.ArrayList<org.telegram.tgnet.TLRPC$PhotoSize> r11 = r9.sizes
            int r11 = r11.size()
            r12 = 0
        L_0x0130:
            if (r12 >= r11) goto L_0x0173
            java.util.ArrayList<org.telegram.tgnet.TLRPC$PhotoSize> r13 = r9.sizes
            java.lang.Object r13 = r13.get(r12)
            org.telegram.tgnet.TLRPC$PhotoSize r13 = (org.telegram.tgnet.TLRPC$PhotoSize) r13
            org.telegram.tgnet.TLRPC$FileLocation r13 = r13.location
            if (r13 == 0) goto L_0x016e
            int r14 = r13.local_id
            org.telegram.tgnet.TLRPC$TL_fileLocationToBeDeprecated r15 = r8.location
            int r4 = r15.local_id
            if (r14 != r4) goto L_0x016e
            long r13 = r13.volume_id
            long r3 = r15.volume_id
            int r15 = (r13 > r3 ? 1 : (r13 == r3 ? 0 : -1))
            if (r15 != 0) goto L_0x016e
            java.util.ArrayList<org.telegram.tgnet.TLRPC$Photo> r3 = r0.photos
            r3.set(r5, r9)
            java.util.ArrayList<org.telegram.tgnet.TLRPC$VideoSize> r3 = r9.video_sizes
            boolean r3 = r3.isEmpty()
            if (r3 != 0) goto L_0x016c
            java.util.ArrayList<org.telegram.messenger.ImageLocation> r3 = r0.videoLocations
            java.util.ArrayList<org.telegram.tgnet.TLRPC$VideoSize> r4 = r9.video_sizes
            java.lang.Object r4 = r4.get(r5)
            org.telegram.tgnet.TLRPC$VideoSize r4 = (org.telegram.tgnet.TLRPC$VideoSize) r4
            org.telegram.messenger.ImageLocation r4 = org.telegram.messenger.ImageLocation.getForPhoto((org.telegram.tgnet.TLRPC$VideoSize) r4, (org.telegram.tgnet.TLRPC$Photo) r9)
            r3.set(r5, r4)
        L_0x016c:
            r3 = 1
            goto L_0x0174
        L_0x016e:
            int r12 = r12 + 1
            r3 = 0
            r4 = 1
            goto L_0x0130
        L_0x0173:
            r3 = 0
        L_0x0174:
            if (r3 == 0) goto L_0x0178
            goto L_0x01f2
        L_0x0178:
            java.util.ArrayList<org.telegram.tgnet.TLRPC$PhotoSize> r3 = r9.sizes
            r4 = 640(0x280, float:8.97E-43)
            org.telegram.tgnet.TLRPC$PhotoSize r3 = org.telegram.messenger.FileLoader.getClosestPhotoSizeWithSize(r3, r4)
            if (r3 == 0) goto L_0x01f2
            int r4 = r9.dc_id
            if (r4 == 0) goto L_0x018e
            org.telegram.tgnet.TLRPC$FileLocation r11 = r3.location
            r11.dc_id = r4
            byte[] r4 = r9.file_reference
            r11.file_reference = r4
        L_0x018e:
            org.telegram.messenger.ImageLocation r4 = org.telegram.messenger.ImageLocation.getForPhoto((org.telegram.tgnet.TLRPC$PhotoSize) r3, (org.telegram.tgnet.TLRPC$Photo) r9)
            if (r4 == 0) goto L_0x01f2
            java.util.ArrayList<org.telegram.messenger.ImageLocation> r11 = r0.imagesLocations
            r11.add(r4)
            java.util.ArrayList<java.lang.String> r4 = r0.thumbsFileNames
            boolean r11 = r10 instanceof org.telegram.tgnet.TLRPC$TL_photoStrippedSize
            if (r11 == 0) goto L_0x01a1
            r11 = r3
            goto L_0x01a2
        L_0x01a1:
            r11 = r10
        L_0x01a2:
            java.lang.String r11 = org.telegram.messenger.FileLoader.getAttachFileName(r11)
            r4.add(r11)
            java.util.ArrayList<org.telegram.messenger.ImageLocation> r4 = r0.thumbsLocations
            org.telegram.messenger.ImageLocation r10 = org.telegram.messenger.ImageLocation.getForPhoto((org.telegram.tgnet.TLRPC$PhotoSize) r10, (org.telegram.tgnet.TLRPC$Photo) r9)
            r4.add(r10)
            java.util.ArrayList<org.telegram.tgnet.TLRPC$VideoSize> r4 = r9.video_sizes
            boolean r4 = r4.isEmpty()
            if (r4 != 0) goto L_0x01d6
            java.util.ArrayList<org.telegram.tgnet.TLRPC$VideoSize> r4 = r9.video_sizes
            java.lang.Object r4 = r4.get(r5)
            org.telegram.tgnet.TLRPC$VideoSize r4 = (org.telegram.tgnet.TLRPC$VideoSize) r4
            java.util.ArrayList<org.telegram.messenger.ImageLocation> r10 = r0.videoLocations
            org.telegram.messenger.ImageLocation r11 = org.telegram.messenger.ImageLocation.getForPhoto((org.telegram.tgnet.TLRPC$VideoSize) r4, (org.telegram.tgnet.TLRPC$Photo) r9)
            r10.add(r11)
            java.util.ArrayList<java.lang.String> r10 = r0.videoFileNames
            java.lang.String r4 = org.telegram.messenger.FileLoader.getAttachFileName(r4)
            r10.add(r4)
            r10 = 0
            goto L_0x01e1
        L_0x01d6:
            java.util.ArrayList<org.telegram.messenger.ImageLocation> r4 = r0.videoLocations
            r10 = 0
            r4.add(r10)
            java.util.ArrayList<java.lang.String> r4 = r0.videoFileNames
            r4.add(r10)
        L_0x01e1:
            java.util.ArrayList<org.telegram.tgnet.TLRPC$Photo> r4 = r0.photos
            r4.add(r9)
            java.util.ArrayList<java.lang.Integer> r4 = r0.imagesLocationsSizes
            int r3 = r3.size
            java.lang.Integer r3 = java.lang.Integer.valueOf(r3)
            r4.add(r3)
            goto L_0x01f5
        L_0x01f2:
            r10 = 0
            goto L_0x01f5
        L_0x01f4:
            r10 = r3
        L_0x01f5:
            int r6 = r6 + 1
            r3 = r10
            r4 = 1
            goto L_0x00ef
        L_0x01fb:
            r16.loadNeighboringThumbs()
            androidx.viewpager.widget.PagerAdapter r2 = r16.getAdapter()
            r2.notifyDataSetChanged()
            boolean r2 = r0.scrolledByUser
            if (r2 != 0) goto L_0x020c
            r16.resetCurrentItem()
        L_0x020c:
            if (r1 == 0) goto L_0x021e
            int r1 = r0.currentAccount
            org.telegram.messenger.MessagesController r6 = org.telegram.messenger.MessagesController.getInstance(r1)
            r8 = 80
            r9 = 0
            r11 = 0
            int r12 = r0.parentClassGuid
            r6.loadDialogPhotos(r7, r8, r9, r11, r12)
        L_0x021e:
            org.telegram.ui.Components.ProfileGalleryView$Callback r1 = r0.callback
            if (r1 == 0) goto L_0x02d4
            r1.onPhotosLoaded()
            goto L_0x02d4
        L_0x0227:
            int r2 = org.telegram.messenger.NotificationCenter.fileDidLoad
            r4 = 1065353216(0x3var_, float:1.0)
            if (r1 != r2) goto L_0x0264
            r1 = r19[r5]
            java.lang.String r1 = (java.lang.String) r1
        L_0x0231:
            java.util.ArrayList<java.lang.String> r2 = r0.thumbsFileNames
            int r2 = r2.size()
            if (r5 >= r2) goto L_0x02d4
            java.util.ArrayList<java.lang.String> r2 = r0.videoFileNames
            java.lang.Object r2 = r2.get(r5)
            java.lang.String r2 = (java.lang.String) r2
            if (r2 != 0) goto L_0x024b
            java.util.ArrayList<java.lang.String> r2 = r0.thumbsFileNames
            java.lang.Object r2 = r2.get(r5)
            java.lang.String r2 = (java.lang.String) r2
        L_0x024b:
            if (r2 == 0) goto L_0x0261
            boolean r2 = android.text.TextUtils.equals(r1, r2)
            if (r2 == 0) goto L_0x0261
            android.util.SparseArray<org.telegram.ui.Components.RadialProgress2> r2 = r0.radialProgresses
            java.lang.Object r2 = r2.get(r5)
            org.telegram.ui.Components.RadialProgress2 r2 = (org.telegram.ui.Components.RadialProgress2) r2
            if (r2 == 0) goto L_0x0261
            r3 = 1
            r2.setProgress(r4, r3)
        L_0x0261:
            int r5 = r5 + 1
            goto L_0x0231
        L_0x0264:
            int r2 = org.telegram.messenger.NotificationCenter.FileLoadProgressChanged
            if (r1 != r2) goto L_0x02b8
            r1 = r19[r5]
            java.lang.String r1 = (java.lang.String) r1
        L_0x026c:
            java.util.ArrayList<java.lang.String> r2 = r0.thumbsFileNames
            int r2 = r2.size()
            if (r5 >= r2) goto L_0x02d4
            java.util.ArrayList<java.lang.String> r2 = r0.videoFileNames
            java.lang.Object r2 = r2.get(r5)
            java.lang.String r2 = (java.lang.String) r2
            if (r2 != 0) goto L_0x0286
            java.util.ArrayList<java.lang.String> r2 = r0.thumbsFileNames
            java.lang.Object r2 = r2.get(r5)
            java.lang.String r2 = (java.lang.String) r2
        L_0x0286:
            if (r2 == 0) goto L_0x02b4
            boolean r2 = android.text.TextUtils.equals(r1, r2)
            if (r2 == 0) goto L_0x02b4
            android.util.SparseArray<org.telegram.ui.Components.RadialProgress2> r2 = r0.radialProgresses
            java.lang.Object r2 = r2.get(r5)
            org.telegram.ui.Components.RadialProgress2 r2 = (org.telegram.ui.Components.RadialProgress2) r2
            if (r2 == 0) goto L_0x02b4
            r6 = 1
            r7 = r19[r6]
            java.lang.Long r7 = (java.lang.Long) r7
            r8 = r19[r3]
            java.lang.Long r8 = (java.lang.Long) r8
            long r9 = r7.longValue()
            float r7 = (float) r9
            long r8 = r8.longValue()
            float r8 = (float) r8
            float r7 = r7 / r8
            float r7 = java.lang.Math.min(r4, r7)
            r2.setProgress(r7, r6)
            goto L_0x02b5
        L_0x02b4:
            r6 = 1
        L_0x02b5:
            int r5 = r5 + 1
            goto L_0x026c
        L_0x02b8:
            int r2 = org.telegram.messenger.NotificationCenter.reloadDialogPhotos
            if (r1 != r2) goto L_0x02d4
            int r1 = r0.settingMainPhoto
            if (r1 == 0) goto L_0x02c1
            return
        L_0x02c1:
            int r1 = r0.currentAccount
            org.telegram.messenger.MessagesController r2 = org.telegram.messenger.MessagesController.getInstance(r1)
            long r3 = r0.dialogId
            int r3 = (int) r3
            r4 = 80
            r5 = 0
            r7 = 1
            int r8 = r0.parentClassGuid
            r2.loadDialogPhotos(r3, r4, r5, r7, r8)
        L_0x02d4:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.Components.ProfileGalleryView.didReceivedNotification(int, int, java.lang.Object[]):void");
    }

    public class ViewPagerAdapter extends CircularViewPager.Adapter {
        private final Context context;
        /* access modifiers changed from: private */
        public final ArrayList<BackupImageView> imageViews = new ArrayList<>();
        /* access modifiers changed from: private */
        public final ArrayList<Item> objects = new ArrayList<>();
        /* access modifiers changed from: private */
        public final ActionBar parentActionBar;
        /* access modifiers changed from: private */
        public final ProfileActivity.AvatarImageView parentAvatarImageView;
        /* access modifiers changed from: private */
        public final Paint placeholderPaint;

        public ViewPagerAdapter(Context context2, ProfileActivity.AvatarImageView avatarImageView, ActionBar actionBar) {
            this.context = context2;
            this.parentAvatarImageView = avatarImageView;
            this.parentActionBar = actionBar;
            Paint paint = new Paint(1);
            this.placeholderPaint = paint;
            paint.setColor(-16777216);
        }

        public int getCount() {
            return this.objects.size();
        }

        public boolean isViewFromObject(View view, Object obj) {
            return view == ((Item) obj).imageView;
        }

        public int getItemPosition(Object obj) {
            int indexOf = this.objects.indexOf((Item) obj);
            if (indexOf == -1) {
                return -2;
            }
            return indexOf;
        }

        public Item instantiateItem(ViewGroup viewGroup, int i) {
            Item item = this.objects.get(i);
            if (item.imageView == null) {
                BackupImageView unused = item.imageView = new BackupImageView(this, this.context, i) {
                    private long firstDrawTime = -1;
                    private boolean isVideo;
                    /* access modifiers changed from: private */
                    public RadialProgress2 radialProgress;
                    private ValueAnimator radialProgressHideAnimator;
                    private float radialProgressHideAnimatorStartValue;
                    private final int radialProgressSize = AndroidUtilities.dp(64.0f);
                    final /* synthetic */ ViewPagerAdapter this$1;
                    final /* synthetic */ int val$position;

                    {
                        int i;
                        boolean z;
                        AnonymousClass1 r11;
                        ViewPagerAdapter viewPagerAdapter = r17;
                        int i2 = r19;
                        this.this$1 = viewPagerAdapter;
                        this.val$position = i2;
                        boolean z2 = true;
                        getImageReceiver().setAllowDecodeSingleFrame(true);
                        int realPosition = viewPagerAdapter.getRealPosition(i2);
                        String str = null;
                        if (realPosition == 0) {
                            Drawable drawable = r17.parentAvatarImageView.getImageReceiver().getDrawable();
                            if (drawable instanceof AnimatedFileDrawable) {
                                AnimatedFileDrawable animatedFileDrawable = (AnimatedFileDrawable) drawable;
                                if (animatedFileDrawable.hasBitmap()) {
                                    setImageDrawable(drawable);
                                    animatedFileDrawable.addSecondParentView(this);
                                    animatedFileDrawable.setInvalidateParentViewWithSecond(true);
                                    z2 = false;
                                    r11 = this;
                                    i = realPosition;
                                    z = false;
                                }
                            }
                            ImageLocation imageLocation = (ImageLocation) ProfileGalleryView.this.videoLocations.get(realPosition);
                            this.isVideo = imageLocation != null;
                            if (imageLocation != null && imageLocation.imageType == 2) {
                                str = "g";
                            }
                            Bitmap bitmap = r17.parentAvatarImageView.getImageReceiver().getBitmap();
                            int intValue = ((Integer) ProfileGalleryView.this.imagesLocationsSizes.get(realPosition)).intValue();
                            setImageMedia((ImageLocation) ProfileGalleryView.this.videoLocations.get(realPosition), str, (ImageLocation) ProfileGalleryView.this.imagesLocations.get(realPosition), (String) null, bitmap, intValue, 1, "avatar_" + ProfileGalleryView.this.dialogId);
                            r11 = this;
                            i = realPosition;
                            z = false;
                        } else {
                            ImageLocation imageLocation2 = (ImageLocation) ProfileGalleryView.this.videoLocations.get(realPosition);
                            r11 = this;
                            r11.isVideo = imageLocation2 != null;
                            String str2 = ((ImageLocation) ProfileGalleryView.this.thumbsLocations.get(realPosition)).photoSize instanceof TLRPC$TL_photoStrippedSize ? "b" : str;
                            int intValue2 = ((Integer) ProfileGalleryView.this.imagesLocationsSizes.get(realPosition)).intValue();
                            StringBuilder sb = new StringBuilder();
                            sb.append("avatar_");
                            i = realPosition;
                            sb.append(ProfileGalleryView.this.dialogId);
                            z = false;
                            setImageMedia(imageLocation2, (String) null, (ImageLocation) ProfileGalleryView.this.imagesLocations.get(realPosition), (String) null, (ImageLocation) ProfileGalleryView.this.thumbsLocations.get(realPosition), str2, (String) null, intValue2, 1, sb.toString());
                            z2 = true;
                        }
                        if (z2) {
                            int i3 = i;
                            RadialProgress2 radialProgress2 = (RadialProgress2) ProfileGalleryView.this.radialProgresses.get(i3);
                            r11.radialProgress = radialProgress2;
                            if (radialProgress2 == null) {
                                RadialProgress2 radialProgress22 = new RadialProgress2(r11);
                                r11.radialProgress = radialProgress22;
                                radialProgress22.setOverrideAlpha(0.0f);
                                r11.radialProgress.setIcon(10, z, z);
                                r11.radialProgress.setColors(NUM, NUM, -1, -1);
                                ProfileGalleryView.this.radialProgresses.append(i3, r11.radialProgress);
                            }
                            postInvalidateOnAnimation();
                        }
                        getImageReceiver().setDelegate(new ImageReceiver.ImageReceiverDelegate() {
                            public void didSetImage(ImageReceiver imageReceiver, boolean z, boolean z2, boolean z3) {
                            }

                            public void onAnimationReady(ImageReceiver imageReceiver) {
                                ProfileGalleryView.this.callback.onVideoSet();
                            }
                        });
                        getImageReceiver().setCrossfadeAlpha((byte) 2);
                    }

                    /* access modifiers changed from: protected */
                    public void onSizeChanged(int i, int i2, int i3, int i4) {
                        super.onSizeChanged(i, i2, i3, i4);
                        if (this.radialProgress != null) {
                            int currentActionBarHeight = (this.this$1.parentActionBar.getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0) + ActionBar.getCurrentActionBarHeight();
                            int dp2 = AndroidUtilities.dp2(80.0f);
                            RadialProgress2 radialProgress2 = this.radialProgress;
                            int i5 = this.radialProgressSize;
                            int i6 = (i2 - currentActionBarHeight) - dp2;
                            radialProgress2.setProgressRect((i - i5) / 2, ((i6 - i5) / 2) + currentActionBarHeight, (i + i5) / 2, currentActionBarHeight + ((i6 + i5) / 2));
                        }
                    }

                    /* access modifiers changed from: protected */
                    public void onDraw(Canvas canvas) {
                        if (this.radialProgress != null) {
                            Drawable drawable = getImageReceiver().getDrawable();
                            long j = 0;
                            if (drawable == null || (this.isVideo && (!(drawable instanceof AnimatedFileDrawable) || ((AnimatedFileDrawable) drawable).getDurationMs() <= 0))) {
                                if (this.firstDrawTime < 0) {
                                    this.firstDrawTime = System.currentTimeMillis();
                                } else {
                                    long currentTimeMillis = System.currentTimeMillis() - this.firstDrawTime;
                                    long j2 = this.isVideo ? 250 : 750;
                                    if (currentTimeMillis <= 250 + j2 && currentTimeMillis > j2) {
                                        this.radialProgress.setOverrideAlpha(CubicBezierInterpolator.DEFAULT.getInterpolation(((float) (currentTimeMillis - j2)) / 250.0f));
                                    }
                                }
                                postInvalidateOnAnimation();
                            } else if (this.radialProgressHideAnimator == null) {
                                if (this.radialProgress.getProgress() < 1.0f) {
                                    this.radialProgress.setProgress(1.0f, true);
                                    j = 100;
                                }
                                this.radialProgressHideAnimatorStartValue = this.radialProgress.getOverrideAlpha();
                                ValueAnimator ofFloat = ValueAnimator.ofFloat(new float[]{0.0f, 1.0f});
                                this.radialProgressHideAnimator = ofFloat;
                                ofFloat.setStartDelay(j);
                                this.radialProgressHideAnimator.setDuration((long) (this.radialProgressHideAnimatorStartValue * 250.0f));
                                this.radialProgressHideAnimator.setInterpolator(CubicBezierInterpolator.DEFAULT);
                                this.radialProgressHideAnimator.addUpdateListener(
                                /*  JADX ERROR: Method code generation error
                                    jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x0069: INVOKE  
                                      (wrap: android.animation.ValueAnimator : 0x0062: IGET  (r0v27 android.animation.ValueAnimator) = 
                                      (r8v0 'this' org.telegram.ui.Components.ProfileGalleryView$ViewPagerAdapter$1 A[THIS])
                                     org.telegram.ui.Components.ProfileGalleryView.ViewPagerAdapter.1.radialProgressHideAnimator android.animation.ValueAnimator)
                                      (wrap: org.telegram.ui.Components.-$$Lambda$ProfileGalleryView$ViewPagerAdapter$1$AuUduz5kSDZmsNXWu9tg6jQwiqE : 0x0066: CONSTRUCTOR  (r1v5 org.telegram.ui.Components.-$$Lambda$ProfileGalleryView$ViewPagerAdapter$1$AuUduz5kSDZmsNXWu9tg6jQwiqE) = 
                                      (r8v0 'this' org.telegram.ui.Components.ProfileGalleryView$ViewPagerAdapter$1 A[THIS])
                                     call: org.telegram.ui.Components.-$$Lambda$ProfileGalleryView$ViewPagerAdapter$1$AuUduz5kSDZmsNXWu9tg6jQwiqE.<init>(org.telegram.ui.Components.ProfileGalleryView$ViewPagerAdapter$1):void type: CONSTRUCTOR)
                                     android.animation.ValueAnimator.addUpdateListener(android.animation.ValueAnimator$AnimatorUpdateListener):void type: VIRTUAL in method: org.telegram.ui.Components.ProfileGalleryView.ViewPagerAdapter.1.onDraw(android.graphics.Canvas):void, dex: classes3.dex
                                    	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:256)
                                    	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:221)
                                    	at jadx.core.codegen.RegionGen.makeSimpleBlock(RegionGen.java:109)
                                    	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:55)
                                    	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                                    	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                                    	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:98)
                                    	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:142)
                                    	at jadx.core.codegen.RegionGen.connectElseIf(RegionGen.java:175)
                                    	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:152)
                                    	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:62)
                                    	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                                    	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                                    	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:98)
                                    	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:142)
                                    	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:62)
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
                                    	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:429)
                                    	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:231)
                                    	at jadx.core.codegen.InsnGen.inlineMethod(InsnGen.java:924)
                                    	at jadx.core.codegen.InsnGen.makeInvoke(InsnGen.java:684)
                                    	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:368)
                                    	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:250)
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
                                    Caused by: jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x0066: CONSTRUCTOR  (r1v5 org.telegram.ui.Components.-$$Lambda$ProfileGalleryView$ViewPagerAdapter$1$AuUduz5kSDZmsNXWu9tg6jQwiqE) = 
                                      (r8v0 'this' org.telegram.ui.Components.ProfileGalleryView$ViewPagerAdapter$1 A[THIS])
                                     call: org.telegram.ui.Components.-$$Lambda$ProfileGalleryView$ViewPagerAdapter$1$AuUduz5kSDZmsNXWu9tg6jQwiqE.<init>(org.telegram.ui.Components.ProfileGalleryView$ViewPagerAdapter$1):void type: CONSTRUCTOR in method: org.telegram.ui.Components.ProfileGalleryView.ViewPagerAdapter.1.onDraw(android.graphics.Canvas):void, dex: classes3.dex
                                    	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:256)
                                    	at jadx.core.codegen.InsnGen.addWrappedArg(InsnGen.java:123)
                                    	at jadx.core.codegen.InsnGen.addArg(InsnGen.java:107)
                                    	at jadx.core.codegen.InsnGen.generateMethodArguments(InsnGen.java:787)
                                    	at jadx.core.codegen.InsnGen.makeInvoke(InsnGen.java:728)
                                    	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:368)
                                    	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:250)
                                    	... 95 more
                                    Caused by: jadx.core.utils.exceptions.JadxRuntimeException: Expected class to be processed at this point, class: org.telegram.ui.Components.-$$Lambda$ProfileGalleryView$ViewPagerAdapter$1$AuUduz5kSDZmsNXWu9tg6jQwiqE, state: NOT_LOADED
                                    	at jadx.core.dex.nodes.ClassNode.ensureProcessed(ClassNode.java:260)
                                    	at jadx.core.codegen.InsnGen.makeConstructor(InsnGen.java:606)
                                    	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:364)
                                    	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:231)
                                    	... 101 more
                                    */
                                /*
                                    this = this;
                                    org.telegram.ui.Components.RadialProgress2 r0 = r8.radialProgress
                                    if (r0 == 0) goto L_0x00ca
                                    org.telegram.messenger.ImageReceiver r0 = r8.getImageReceiver()
                                    android.graphics.drawable.Drawable r0 = r0.getDrawable()
                                    r1 = 1132068864(0x437a0000, float:250.0)
                                    r2 = 0
                                    if (r0 == 0) goto L_0x007c
                                    boolean r4 = r8.isVideo
                                    if (r4 == 0) goto L_0x0022
                                    boolean r4 = r0 instanceof org.telegram.ui.Components.AnimatedFileDrawable
                                    if (r4 == 0) goto L_0x007c
                                    org.telegram.ui.Components.AnimatedFileDrawable r0 = (org.telegram.ui.Components.AnimatedFileDrawable) r0
                                    int r0 = r0.getDurationMs()
                                    if (r0 <= 0) goto L_0x007c
                                L_0x0022:
                                    android.animation.ValueAnimator r0 = r8.radialProgressHideAnimator
                                    if (r0 != 0) goto L_0x00b4
                                    org.telegram.ui.Components.RadialProgress2 r0 = r8.radialProgress
                                    float r0 = r0.getProgress()
                                    r4 = 1065353216(0x3var_, float:1.0)
                                    int r0 = (r0 > r4 ? 1 : (r0 == r4 ? 0 : -1))
                                    if (r0 >= 0) goto L_0x003a
                                    org.telegram.ui.Components.RadialProgress2 r0 = r8.radialProgress
                                    r2 = 1
                                    r0.setProgress(r4, r2)
                                    r2 = 100
                                L_0x003a:
                                    org.telegram.ui.Components.RadialProgress2 r0 = r8.radialProgress
                                    float r0 = r0.getOverrideAlpha()
                                    r8.radialProgressHideAnimatorStartValue = r0
                                    r0 = 2
                                    float[] r0 = new float[r0]
                                    r0 = {0, NUM} // fill-array
                                    android.animation.ValueAnimator r0 = android.animation.ValueAnimator.ofFloat(r0)
                                    r8.radialProgressHideAnimator = r0
                                    r0.setStartDelay(r2)
                                    android.animation.ValueAnimator r0 = r8.radialProgressHideAnimator
                                    float r2 = r8.radialProgressHideAnimatorStartValue
                                    float r2 = r2 * r1
                                    long r1 = (long) r2
                                    r0.setDuration(r1)
                                    android.animation.ValueAnimator r0 = r8.radialProgressHideAnimator
                                    org.telegram.ui.Components.CubicBezierInterpolator r1 = org.telegram.ui.Components.CubicBezierInterpolator.DEFAULT
                                    r0.setInterpolator(r1)
                                    android.animation.ValueAnimator r0 = r8.radialProgressHideAnimator
                                    org.telegram.ui.Components.-$$Lambda$ProfileGalleryView$ViewPagerAdapter$1$AuUduz5kSDZmsNXWu9tg6jQwiqE r1 = new org.telegram.ui.Components.-$$Lambda$ProfileGalleryView$ViewPagerAdapter$1$AuUduz5kSDZmsNXWu9tg6jQwiqE
                                    r1.<init>(r8)
                                    r0.addUpdateListener(r1)
                                    android.animation.ValueAnimator r0 = r8.radialProgressHideAnimator
                                    org.telegram.ui.Components.ProfileGalleryView$ViewPagerAdapter$1$2 r1 = new org.telegram.ui.Components.ProfileGalleryView$ViewPagerAdapter$1$2
                                    r1.<init>()
                                    r0.addListener(r1)
                                    android.animation.ValueAnimator r0 = r8.radialProgressHideAnimator
                                    r0.start()
                                    goto L_0x00b4
                                L_0x007c:
                                    long r4 = r8.firstDrawTime
                                    int r0 = (r4 > r2 ? 1 : (r4 == r2 ? 0 : -1))
                                    if (r0 >= 0) goto L_0x0089
                                    long r0 = java.lang.System.currentTimeMillis()
                                    r8.firstDrawTime = r0
                                    goto L_0x00b1
                                L_0x0089:
                                    long r2 = java.lang.System.currentTimeMillis()
                                    long r4 = r8.firstDrawTime
                                    long r2 = r2 - r4
                                    boolean r0 = r8.isVideo
                                    r4 = 250(0xfa, double:1.235E-321)
                                    if (r0 == 0) goto L_0x0098
                                    r6 = r4
                                    goto L_0x009a
                                L_0x0098:
                                    r6 = 750(0x2ee, double:3.705E-321)
                                L_0x009a:
                                    long r4 = r4 + r6
                                    int r0 = (r2 > r4 ? 1 : (r2 == r4 ? 0 : -1))
                                    if (r0 > 0) goto L_0x00b1
                                    int r0 = (r2 > r6 ? 1 : (r2 == r6 ? 0 : -1))
                                    if (r0 <= 0) goto L_0x00b1
                                    org.telegram.ui.Components.RadialProgress2 r0 = r8.radialProgress
                                    org.telegram.ui.Components.CubicBezierInterpolator r4 = org.telegram.ui.Components.CubicBezierInterpolator.DEFAULT
                                    long r2 = r2 - r6
                                    float r2 = (float) r2
                                    float r2 = r2 / r1
                                    float r1 = r4.getInterpolation(r2)
                                    r0.setOverrideAlpha(r1)
                                L_0x00b1:
                                    r8.postInvalidateOnAnimation()
                                L_0x00b4:
                                    r3 = 0
                                    r4 = 0
                                    int r0 = r8.getWidth()
                                    float r5 = (float) r0
                                    int r0 = r8.getHeight()
                                    float r6 = (float) r0
                                    org.telegram.ui.Components.ProfileGalleryView$ViewPagerAdapter r0 = r8.this$1
                                    android.graphics.Paint r7 = r0.placeholderPaint
                                    r2 = r9
                                    r2.drawRect(r3, r4, r5, r6, r7)
                                L_0x00ca:
                                    super.onDraw(r9)
                                    org.telegram.ui.Components.RadialProgress2 r0 = r8.radialProgress
                                    if (r0 == 0) goto L_0x00df
                                    float r0 = r0.getOverrideAlpha()
                                    r1 = 0
                                    int r0 = (r0 > r1 ? 1 : (r0 == r1 ? 0 : -1))
                                    if (r0 <= 0) goto L_0x00df
                                    org.telegram.ui.Components.RadialProgress2 r0 = r8.radialProgress
                                    r0.draw(r9)
                                L_0x00df:
                                    return
                                */
                                throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.Components.ProfileGalleryView.ViewPagerAdapter.AnonymousClass1.onDraw(android.graphics.Canvas):void");
                            }

                            /* access modifiers changed from: private */
                            /* renamed from: lambda$onDraw$0 */
                            public /* synthetic */ void lambda$onDraw$0$ProfileGalleryView$ViewPagerAdapter$1(ValueAnimator valueAnimator) {
                                this.radialProgress.setOverrideAlpha(AndroidUtilities.lerp(this.radialProgressHideAnimatorStartValue, 0.0f, valueAnimator.getAnimatedFraction()));
                            }
                        };
                        this.imageViews.set(i, item.imageView);
                    }
                    if (item.imageView.getParent() == null) {
                        viewGroup.addView(item.imageView);
                    }
                    return item;
                }

                public void destroyItem(ViewGroup viewGroup, int i, Object obj) {
                    BackupImageView access$500 = ((Item) obj).imageView;
                    if (access$500.getImageReceiver().hasStaticThumb()) {
                        Drawable drawable = access$500.getImageReceiver().getDrawable();
                        if (drawable instanceof AnimatedFileDrawable) {
                            ((AnimatedFileDrawable) drawable).removeSecondParentView(access$500);
                        }
                    }
                    viewGroup.removeView(access$500);
                }

                public CharSequence getPageTitle(int i) {
                    return (getRealPosition(i) + 1) + "/" + (getCount() - (getExtraCount() * 2));
                }

                public void notifyDataSetChanged() {
                    this.objects.clear();
                    int size = ProfileGalleryView.this.imagesLocations.size() + (getExtraCount() * 2);
                    for (int i = 0; i < size; i++) {
                        this.objects.add(new Item());
                        this.imageViews.add((Object) null);
                    }
                    super.notifyDataSetChanged();
                }

                public int getExtraCount() {
                    if (ProfileGalleryView.this.imagesLocations.size() >= 2) {
                        return ProfileGalleryView.this.getOffscreenPageLimit();
                    }
                    return 0;
                }
            }
        }
