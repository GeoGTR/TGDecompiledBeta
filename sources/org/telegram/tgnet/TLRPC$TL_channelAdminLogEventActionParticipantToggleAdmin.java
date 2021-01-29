package org.telegram.tgnet;

public class TLRPC$TL_channelAdminLogEventActionParticipantToggleAdmin extends TLRPC$ChannelAdminLogEventAction {
    public static int constructor = -NUM;
    public TLRPC$ChannelParticipant new_participant;
    public TLRPC$ChannelParticipant prev_participant;

    public void readParams(AbstractSerializedData abstractSerializedData, boolean z) {
        this.prev_participant = TLRPC$ChannelParticipant.TLdeserialize(abstractSerializedData, abstractSerializedData.readInt32(z), z);
        this.new_participant = TLRPC$ChannelParticipant.TLdeserialize(abstractSerializedData, abstractSerializedData.readInt32(z), z);
    }

    public void serializeToStream(AbstractSerializedData abstractSerializedData) {
        abstractSerializedData.writeInt32(constructor);
        this.prev_participant.serializeToStream(abstractSerializedData);
        this.new_participant.serializeToStream(abstractSerializedData);
    }
}
