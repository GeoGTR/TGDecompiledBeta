package org.telegram.ui.Components;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.util.SparseArray;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import java.util.ArrayList;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.ImageLocation;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.NotificationCenter.NotificationCenterDelegate;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.TLRPC.Chat;
import org.telegram.tgnet.TLRPC.FileLocation;
import org.telegram.tgnet.TLRPC.Photo;
import org.telegram.tgnet.TLRPC.PhotoSize;
import org.telegram.tgnet.TLRPC.TL_fileLocationToBeDeprecated;
import org.telegram.tgnet.TLRPC.TL_photoEmpty;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ProfileActivity;

public class ProfileGalleryView extends ViewPager implements NotificationCenterDelegate {
    private int currentAccount = UserConfig.selectedAccount;
    private final PointF downPoint = new PointF();
    private final GestureDetector gestureDetector;
    private ArrayList<ImageLocation> imagesLocations = new ArrayList();
    private ArrayList<Integer> imagesLocationsSizes = new ArrayList();
    private boolean isScrollingListView = true;
    private boolean isSwipingViewPager = true;
    private ProfileActivity parentActivity;
    private RecyclerListView parentListView;
    private ImageLocation prevImageLocation;
    private final SparseArray<RadialProgress2> radialProgresses = new SparseArray();
    private ArrayList<String> thumbsFileNames = new ArrayList();
    private ArrayList<ImageLocation> thumbsLocations = new ArrayList();
    private final int touchSlop;

    private class Item {
        private BackupImageView imageView;

        private Item() {
        }

        /* synthetic */ Item(ProfileGalleryView profileGalleryView, AnonymousClass1 anonymousClass1) {
            this();
        }
    }

    public ProfileGalleryView(final Context context, final ProfileActivity profileActivity) {
        super(context);
        setVisibility(8);
        setOverScrollMode(2);
        this.touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        this.parentActivity = profileActivity;
        this.parentListView = this.parentActivity.getListView();
        setOffscreenPageLimit(2);
        this.gestureDetector = new GestureDetector(context, new OnGestureListener() {
            public boolean onDown(MotionEvent motionEvent) {
                return false;
            }

            public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent2, float f, float f2) {
                return false;
            }

            public void onLongPress(MotionEvent motionEvent) {
            }

            public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent2, float f, float f2) {
                return false;
            }

            public void onShowPress(MotionEvent motionEvent) {
            }

            public boolean onSingleTapUp(MotionEvent motionEvent) {
                int count = ProfileGalleryView.this.getAdapter().getCount();
                int currentItem = ProfileGalleryView.this.getCurrentItem();
                if (count <= 1) {
                    return false;
                }
                int i;
                if (motionEvent.getX() > ((float) (ProfileGalleryView.this.getWidth() / 3))) {
                    i = currentItem + 1;
                    if (i >= count) {
                        i = 0;
                    }
                } else {
                    i = currentItem - 1;
                    if (i < 0) {
                        i = count - 1;
                    }
                }
                ProfileGalleryView.this.setCurrentItem(i, false);
                return true;
            }
        });
        setAdapter(new PagerAdapter() {
            private final ArrayList<Item> objects = new ArrayList();

            public int getCount() {
                return this.objects.size();
            }

            public boolean isViewFromObject(View view, Object obj) {
                return ((Item) view.getTag(NUM)) == ((Item) obj);
            }

            public int getItemPosition(Object obj) {
                int indexOf = this.objects.indexOf((Item) obj);
                return indexOf == -1 ? -2 : indexOf;
            }

            public Item instantiateItem(ViewGroup viewGroup, final int i) {
                Item item = (Item) this.objects.get(i);
                if (item.imageView == null) {
                    item.imageView = new BackupImageView(context) {
                        private long initTime = SystemClock.elapsedRealtime();
                        private final Drawable placeholderDrawable = ContextCompat.getDrawable(context, NUM);
                        private RadialProgress2 radialProgress;
                        private ValueAnimator radialProgressHideAnimator;
                        private float radialProgressHideAnimatorStartValue;
                        private final int radialProgressSize = AndroidUtilities.dp(64.0f);

                        /* Access modifiers changed, original: protected */
                        public void onSizeChanged(int i, int i2, int i3, int i4) {
                            super.onSizeChanged(i, i2, i3, i4);
                            if (this.radialProgress != null) {
                                i3 = (profileActivity.getActionBar().getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0) + ActionBar.getCurrentActionBarHeight();
                                i4 = AndroidUtilities.dp2(80.0f);
                                RadialProgress2 radialProgress2 = this.radialProgress;
                                int i5 = this.radialProgressSize;
                                i2 = (i2 - i3) - i4;
                                radialProgress2.setProgressRect((i - i5) / 2, ((i2 - i5) / 2) + i3, (i + i5) / 2, i3 + ((i2 + i5) / 2));
                            }
                        }

                        /* Access modifiers changed, original: protected */
                        public void onDraw(Canvas canvas) {
                            super.onDraw(canvas);
                            if (this.radialProgress == null) {
                                return;
                            }
                            if (getImageReceiver().getDrawable() == this.placeholderDrawable) {
                                float elapsedRealtime = (float) (SystemClock.elapsedRealtime() - this.initTime);
                                if (elapsedRealtime <= 550.0f) {
                                    if (elapsedRealtime > 300.0f) {
                                        this.radialProgress.setOverrideAlpha(CubicBezierInterpolator.DEFAULT.getInterpolation((elapsedRealtime - 300.0f) / 250.0f));
                                    }
                                    postInvalidateOnAnimation();
                                }
                                if (this.radialProgress.getOverrideAlpha() > 0.0f) {
                                    this.radialProgress.draw(canvas);
                                }
                            } else if (this.radialProgressHideAnimator == null) {
                                this.radialProgressHideAnimatorStartValue = this.radialProgress.getOverrideAlpha();
                                this.radialProgressHideAnimator = ValueAnimator.ofFloat(new float[]{0.0f, 1.0f});
                                this.radialProgressHideAnimator.setDuration((long) (this.radialProgressHideAnimatorStartValue * 250.0f));
                                this.radialProgressHideAnimator.setInterpolator(CubicBezierInterpolator.DEFAULT);
                                this.radialProgressHideAnimator.addUpdateListener(new -$$Lambda$ProfileGalleryView$2$1$gNRFpm0niMTZhnvL1PAiuA8Z8_4(this));
                                this.radialProgressHideAnimator.addListener(new AnimatorListenerAdapter() {
                                    public void onAnimationEnd(Animator animator) {
                                        AnonymousClass1.this.radialProgress = null;
                                        ProfileGalleryView.this.radialProgresses.delete(i);
                                    }
                                });
                                this.radialProgressHideAnimator.start();
                            }
                        }

                        public /* synthetic */ void lambda$onDraw$0$ProfileGalleryView$2$1(ValueAnimator valueAnimator) {
                            this.radialProgress.setOverrideAlpha(AndroidUtilities.lerp(this.radialProgressHideAnimatorStartValue, 0.0f, valueAnimator.getAnimatedFraction()));
                        }
                    };
                    item.imageView.setTag(NUM, item);
                }
                viewGroup.removeView(item.imageView);
                viewGroup.addView(item.imageView);
                return item;
            }

            public void destroyItem(ViewGroup viewGroup, int i, Object obj) {
                viewGroup.removeView(((Item) obj).imageView);
                ProfileGalleryView.this.radialProgresses.delete(i);
            }

            public CharSequence getPageTitle(int i) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(i + 1);
                stringBuilder.append("/");
                stringBuilder.append(getCount());
                return stringBuilder.toString();
            }

            public void notifyDataSetChanged() {
                this.objects.clear();
                int size = ProfileGalleryView.this.imagesLocations.size();
                for (int i = 0; i < size; i++) {
                    this.objects.add(new Item(ProfileGalleryView.this, null));
                }
                super.notifyDataSetChanged();
            }
        });
        NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.dialogPhotosLoaded);
        NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.fileDidLoad);
        NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.FileLoadProgressChanged);
        MessagesController.getInstance(this.currentAccount).loadDialogPhotos((int) this.parentActivity.getAvatarDialogId(), 80, 0, true, this.parentActivity.getClassGuid());
    }

    public void onDestroy() {
        NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.dialogPhotosLoaded);
        NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.fileDidLoad);
        NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.FileLoadProgressChanged);
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        this.gestureDetector.onTouchEvent(motionEvent);
        if (this.parentListView.getScrollState() == 0 || this.isScrollingListView || !this.isSwipingViewPager) {
            int action = motionEvent.getAction();
            if (action == 0) {
                this.isScrollingListView = true;
                this.isSwipingViewPager = true;
                this.downPoint.set(motionEvent.getX(), motionEvent.getY());
            } else if (action == 2) {
                float x = motionEvent.getX() - this.downPoint.x;
                float y = motionEvent.getY() - this.downPoint.y;
                if (this.isSwipingViewPager && this.isScrollingListView) {
                    if (Math.abs(y) >= ((float) this.touchSlop) || Math.abs(x) >= ((float) this.touchSlop)) {
                        MotionEvent obtain;
                        if (Math.abs(y) > Math.abs(x)) {
                            this.isSwipingViewPager = false;
                            obtain = MotionEvent.obtain(motionEvent);
                            obtain.setAction(3);
                            super.onTouchEvent(obtain);
                            obtain.recycle();
                        } else {
                            this.isScrollingListView = false;
                            obtain = MotionEvent.obtain(motionEvent);
                            obtain.setAction(3);
                            this.parentListView.onTouchEvent(obtain);
                            obtain.recycle();
                        }
                    }
                } else if (this.isSwipingViewPager && !canScrollHorizontally(-1) && x > ((float) this.touchSlop)) {
                    return false;
                }
            }
            boolean onTouchEvent = this.isScrollingListView ? this.parentListView.onTouchEvent(motionEvent) | 0 : false;
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
        motionEvent = MotionEvent.obtain(motionEvent);
        motionEvent.setAction(3);
        super.onTouchEvent(motionEvent);
        motionEvent.recycle();
        return false;
    }

    public void initIfEmpty(ImageLocation imageLocation, ImageLocation imageLocation2) {
        if (!(imageLocation == null || imageLocation2 == null)) {
            ImageLocation imageLocation3 = this.prevImageLocation;
            if (!(imageLocation3 == null || imageLocation3.location.local_id == imageLocation.location.local_id)) {
                this.imagesLocations.clear();
                MessagesController.getInstance(this.currentAccount).loadDialogPhotos((int) this.parentActivity.getAvatarDialogId(), 80, 0, true, this.parentActivity.getClassGuid());
            }
            if (this.imagesLocations.isEmpty()) {
                this.prevImageLocation = imageLocation;
                this.thumbsFileNames.add("");
                this.imagesLocations.add(imageLocation);
                this.thumbsLocations.add(imageLocation2);
                this.imagesLocationsSizes.add(Integer.valueOf(-1));
                getAdapter().notifyDataSetChanged();
            }
        }
    }

    public ImageLocation getImageLocation(int i) {
        return (i < 0 || i >= this.imagesLocations.size()) ? null : (ImageLocation) this.imagesLocations.get(i);
    }

    public ImageLocation getThumbLocation(int i) {
        return (i < 0 || i >= this.thumbsLocations.size()) ? null : (ImageLocation) this.thumbsLocations.get(i);
    }

    public boolean hasImages() {
        return this.imagesLocations.isEmpty() ^ 1;
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

    public void didReceivedNotification(int i, int i2, Object... objArr) {
        int i3 = i;
        int i4 = 0;
        String str;
        RadialProgress2 radialProgress2;
        if (i3 == NotificationCenter.dialogPhotosLoaded) {
            i3 = ((Integer) objArr[3]).intValue();
            int intValue = ((Integer) objArr[0]).intValue();
            if (((long) intValue) == this.parentActivity.getAvatarDialogId() && this.parentActivity.getClassGuid() == i3) {
                boolean booleanValue = ((Boolean) objArr[2]).booleanValue();
                ArrayList arrayList = (ArrayList) objArr[4];
                this.thumbsFileNames.clear();
                this.imagesLocations.clear();
                this.thumbsLocations.clear();
                this.imagesLocationsSizes.clear();
                ImageLocation imageLocation = null;
                if (intValue < 0) {
                    Chat chat = MessagesController.getInstance(this.currentAccount).getChat(Integer.valueOf(-intValue));
                    ImageLocation forChat = ImageLocation.getForChat(chat, true);
                    if (forChat != null) {
                        this.thumbsFileNames.add("");
                        this.imagesLocations.add(forChat);
                        this.thumbsLocations.add(ImageLocation.getForChat(chat, false));
                        this.imagesLocationsSizes.add(Integer.valueOf(-1));
                    }
                    imageLocation = forChat;
                }
                for (int i5 = 0; i5 < arrayList.size(); i5++) {
                    Photo photo = (Photo) arrayList.get(i5);
                    if (!(photo == null || (photo instanceof TL_photoEmpty))) {
                        ArrayList arrayList2 = photo.sizes;
                        if (arrayList2 != null) {
                            int i6;
                            FileLocation fileLocation;
                            PhotoSize closestPhotoSizeWithSize = FileLoader.getClosestPhotoSizeWithSize(arrayList2, 640);
                            PhotoSize closestPhotoSizeWithSize2 = FileLoader.getClosestPhotoSizeWithSize(photo.sizes, 50);
                            if (imageLocation != null) {
                                Object obj;
                                for (i6 = 0; i6 < photo.sizes.size(); i6++) {
                                    fileLocation = ((PhotoSize) photo.sizes.get(i6)).location;
                                    int i7 = fileLocation.local_id;
                                    TL_fileLocationToBeDeprecated tL_fileLocationToBeDeprecated = imageLocation.location;
                                    if (i7 == tL_fileLocationToBeDeprecated.local_id && fileLocation.volume_id == tL_fileLocationToBeDeprecated.volume_id) {
                                        obj = 1;
                                        break;
                                    }
                                }
                                obj = null;
                                if (obj != null) {
                                }
                            }
                            if (closestPhotoSizeWithSize != null) {
                                i6 = photo.dc_id;
                                if (i6 != 0) {
                                    fileLocation = closestPhotoSizeWithSize.location;
                                    fileLocation.dc_id = i6;
                                    fileLocation.file_reference = photo.file_reference;
                                }
                                ImageLocation forPhoto = ImageLocation.getForPhoto(closestPhotoSizeWithSize, photo);
                                if (forPhoto != null) {
                                    this.imagesLocations.add(forPhoto);
                                    this.thumbsFileNames.add(FileLoader.getAttachFileName(closestPhotoSizeWithSize2));
                                    this.thumbsLocations.add(ImageLocation.getForPhoto(closestPhotoSizeWithSize2, photo));
                                    this.imagesLocationsSizes.add(Integer.valueOf(closestPhotoSizeWithSize.size));
                                }
                            }
                        }
                    }
                }
                getAdapter().notifyDataSetChanged();
                if (booleanValue) {
                    MessagesController.getInstance(this.currentAccount).loadDialogPhotos(intValue, 80, 0, false, this.parentActivity.getClassGuid());
                }
            }
        } else if (i3 == NotificationCenter.fileDidLoad) {
            str = (String) objArr[0];
            while (i4 < this.thumbsFileNames.size()) {
                if (((String) this.thumbsFileNames.get(i4)).equals(str)) {
                    radialProgress2 = (RadialProgress2) this.radialProgresses.get(i4);
                    if (radialProgress2 != null) {
                        radialProgress2.setProgress(1.0f, true);
                    }
                }
                i4++;
            }
        } else if (i3 == NotificationCenter.FileLoadProgressChanged) {
            str = (String) objArr[0];
            while (i4 < this.thumbsFileNames.size()) {
                if (((String) this.thumbsFileNames.get(i4)).equals(str)) {
                    radialProgress2 = (RadialProgress2) this.radialProgresses.get(i4);
                    if (radialProgress2 != null) {
                        radialProgress2.setProgress(((Float) objArr[1]).floatValue(), true);
                    }
                }
                i4++;
            }
        }
    }
}
