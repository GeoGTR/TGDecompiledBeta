package org.telegram.tgnet;

public class TLObject {
    private static final ThreadLocal<NativeByteBuffer> sizeCalculator = new C06421();
    public boolean disableFree = false;
    public int networkType;

    /* renamed from: org.telegram.tgnet.TLObject$1 */
    static class C06421 extends ThreadLocal<NativeByteBuffer> {
        C06421() {
        }

        protected NativeByteBuffer initialValue() {
            return new NativeByteBuffer(true);
        }
    }

    public void readParams(AbstractSerializedData stream, boolean exception) {
    }

    public void serializeToStream(AbstractSerializedData stream) {
    }

    public TLObject deserializeResponse(AbstractSerializedData stream, int constructor, boolean exception) {
        return null;
    }

    public void freeResources() {
    }

    public int getObjectSize() {
        NativeByteBuffer byteBuffer = (NativeByteBuffer) sizeCalculator.get();
        byteBuffer.rewind();
        serializeToStream((AbstractSerializedData) sizeCalculator.get());
        return byteBuffer.length();
    }
}
