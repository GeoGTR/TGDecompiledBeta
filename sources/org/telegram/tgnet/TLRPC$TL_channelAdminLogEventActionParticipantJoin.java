package org.telegram.tgnet;

public class TLRPC$TL_channelAdminLogEventActionParticipantJoin extends TLRPC$ChannelAdminLogEventAction {
    public static int constructor = NUM;

    public void serializeToStream(AbstractSerializedData abstractSerializedData) {
        abstractSerializedData.writeInt32(constructor);
    }
}
