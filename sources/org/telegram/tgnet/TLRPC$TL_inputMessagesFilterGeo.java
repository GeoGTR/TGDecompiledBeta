package org.telegram.tgnet;

public class TLRPC$TL_inputMessagesFilterGeo extends TLRPC$MessagesFilter {
    public static int constructor = -NUM;

    public void serializeToStream(AbstractSerializedData abstractSerializedData) {
        abstractSerializedData.writeInt32(constructor);
    }
}
