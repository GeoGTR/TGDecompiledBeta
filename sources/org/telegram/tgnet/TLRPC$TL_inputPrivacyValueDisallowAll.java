package org.telegram.tgnet;

public class TLRPC$TL_inputPrivacyValueDisallowAll extends TLRPC$InputPrivacyRule {
    public static int constructor = -NUM;

    public void serializeToStream(AbstractSerializedData abstractSerializedData) {
        abstractSerializedData.writeInt32(constructor);
    }
}
