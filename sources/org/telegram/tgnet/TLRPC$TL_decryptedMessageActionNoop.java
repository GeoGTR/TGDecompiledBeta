package org.telegram.tgnet;

public class TLRPC$TL_decryptedMessageActionNoop extends TLRPC$DecryptedMessageAction {
    public static int constructor = -NUM;

    public void serializeToStream(AbstractSerializedData abstractSerializedData) {
        abstractSerializedData.writeInt32(constructor);
    }
}
