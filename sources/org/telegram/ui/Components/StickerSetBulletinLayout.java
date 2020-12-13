package org.telegram.ui.Components;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import java.util.ArrayList;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.ImageLocation;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC$Document;
import org.telegram.tgnet.TLRPC$PhotoSize;
import org.telegram.tgnet.TLRPC$StickerSet;
import org.telegram.tgnet.TLRPC$StickerSetCovered;
import org.telegram.tgnet.TLRPC$TL_messages_stickerSet;
import org.telegram.ui.Components.Bulletin;

@SuppressLint({"ViewConstructor"})
public class StickerSetBulletinLayout extends Bulletin.TwoLineLayout {
    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public StickerSetBulletinLayout(Context context, TLObject tLObject, int i) {
        super(context);
        TLRPC$StickerSet tLRPC$StickerSet;
        ImageLocation imageLocation;
        TLObject tLObject2 = tLObject;
        int i2 = i;
        TLRPC$Document tLRPC$Document = null;
        if (tLObject2 instanceof TLRPC$TL_messages_stickerSet) {
            TLRPC$TL_messages_stickerSet tLRPC$TL_messages_stickerSet = (TLRPC$TL_messages_stickerSet) tLObject2;
            tLRPC$StickerSet = tLRPC$TL_messages_stickerSet.set;
            ArrayList<TLRPC$Document> arrayList = tLRPC$TL_messages_stickerSet.documents;
            if (arrayList != null && !arrayList.isEmpty()) {
                tLRPC$Document = arrayList.get(0);
            }
        } else if (tLObject2 instanceof TLRPC$StickerSetCovered) {
            TLRPC$StickerSetCovered tLRPC$StickerSetCovered = (TLRPC$StickerSetCovered) tLObject2;
            tLRPC$StickerSet = tLRPC$StickerSetCovered.set;
            TLRPC$Document tLRPC$Document2 = tLRPC$StickerSetCovered.cover;
            if (tLRPC$Document2 != null) {
                tLRPC$Document = tLRPC$Document2;
            } else if (!tLRPC$StickerSetCovered.covers.isEmpty()) {
                tLRPC$Document = tLRPC$StickerSetCovered.covers.get(0);
            }
        } else {
            throw new IllegalArgumentException("Invalid type of the given setObject: " + tLObject.getClass());
        }
        TLRPC$StickerSet tLRPC$StickerSet2 = tLRPC$StickerSet;
        if (tLRPC$Document != null) {
            TLObject closestPhotoSizeWithSize = FileLoader.getClosestPhotoSizeWithSize(tLRPC$StickerSet2.thumbs, 90);
            closestPhotoSizeWithSize = closestPhotoSizeWithSize == null ? tLRPC$Document : closestPhotoSizeWithSize;
            boolean z = closestPhotoSizeWithSize instanceof TLRPC$Document;
            if (z) {
                imageLocation = ImageLocation.getForDocument(FileLoader.getClosestPhotoSizeWithSize(tLRPC$Document.thumbs, 90), tLRPC$Document);
            } else {
                imageLocation = ImageLocation.getForSticker((TLRPC$PhotoSize) closestPhotoSizeWithSize, tLRPC$Document);
            }
            ImageLocation imageLocation2 = imageLocation;
            if (z && MessageObject.isAnimatedStickerDocument(tLRPC$Document, true)) {
                this.imageView.setImage(ImageLocation.getForDocument(tLRPC$Document), "50_50", imageLocation2, (String) null, 0, (Object) tLObject);
            } else if (imageLocation2 == null || imageLocation2.imageType != 1) {
                this.imageView.setImage(imageLocation2, "50_50", "webp", (Drawable) null, (Object) tLObject);
            } else {
                this.imageView.setImage(imageLocation2, "50_50", "tgs", (Drawable) null, (Object) tLObject);
            }
        } else {
            this.imageView.setImage((ImageLocation) null, (String) null, "webp", (Drawable) null, (Object) tLObject);
        }
        if (i2 != 0) {
            if (i2 != 1) {
                if (i2 == 2) {
                    if (tLRPC$StickerSet2.masks) {
                        this.titleTextView.setText(LocaleController.getString("AddMasksInstalled", NUM));
                        this.subtitleTextView.setText(LocaleController.formatString("AddMasksInstalledInfo", NUM, tLRPC$StickerSet2.title));
                        return;
                    }
                    this.titleTextView.setText(LocaleController.getString("AddStickersInstalled", NUM));
                    this.subtitleTextView.setText(LocaleController.formatString("AddStickersInstalledInfo", NUM, tLRPC$StickerSet2.title));
                }
            } else if (tLRPC$StickerSet2.masks) {
                this.titleTextView.setText(LocaleController.getString("MasksArchived", NUM));
                this.subtitleTextView.setText(LocaleController.formatString("MasksArchivedInfo", NUM, tLRPC$StickerSet2.title));
            } else {
                this.titleTextView.setText(LocaleController.getString("StickersArchived", NUM));
                this.subtitleTextView.setText(LocaleController.formatString("StickersArchivedInfo", NUM, tLRPC$StickerSet2.title));
            }
        } else if (tLRPC$StickerSet2.masks) {
            this.titleTextView.setText(LocaleController.getString("MasksRemoved", NUM));
            this.subtitleTextView.setText(LocaleController.formatString("MasksRemovedInfo", NUM, tLRPC$StickerSet2.title));
        } else {
            this.titleTextView.setText(LocaleController.getString("StickersRemoved", NUM));
            this.subtitleTextView.setText(LocaleController.formatString("StickersRemovedInfo", NUM, tLRPC$StickerSet2.title));
        }
    }
}
