package org.telegram.tgnet;

public class TLRPC$TL_privacyKeyAddedByPhone extends TLRPC$PrivacyKey {
    public static int constructor = NUM;

    public void serializeToStream(AbstractSerializedData abstractSerializedData) {
        abstractSerializedData.writeInt32(constructor);
    }
}
