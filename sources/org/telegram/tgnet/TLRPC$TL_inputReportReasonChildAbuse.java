package org.telegram.tgnet;

public class TLRPC$TL_inputReportReasonChildAbuse extends TLRPC$ReportReason {
    public static int constructor = -NUM;

    public void serializeToStream(AbstractSerializedData abstractSerializedData) {
        abstractSerializedData.writeInt32(constructor);
    }
}
