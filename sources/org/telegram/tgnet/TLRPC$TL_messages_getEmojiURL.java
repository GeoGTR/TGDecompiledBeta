package org.telegram.tgnet;

public class TLRPC$TL_messages_getEmojiURL extends TLObject {
    public static int constructor = -NUM;
    public String lang_code;

    public TLObject deserializeResponse(AbstractSerializedData abstractSerializedData, int i, boolean z) {
        return TLRPC$TL_emojiURL.TLdeserialize(abstractSerializedData, i, z);
    }

    public void serializeToStream(AbstractSerializedData abstractSerializedData) {
        abstractSerializedData.writeInt32(constructor);
        abstractSerializedData.writeString(this.lang_code);
    }
}
