package org.telegram.tgnet;

public class TLRPC$TL_messageMediaEmpty extends TLRPC$MessageMedia {
    public static int constructor = NUM;

    public void serializeToStream(AbstractSerializedData abstractSerializedData) {
        abstractSerializedData.writeInt32(constructor);
    }
}
