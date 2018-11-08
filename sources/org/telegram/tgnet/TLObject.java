package org.telegram.tgnet;

public class TLObject {
    private static final ThreadLocal<NativeByteBuffer> sizeCalculator = new C05211();
    public boolean disableFree = false;
    public int networkType;

    /* renamed from: org.telegram.tgnet.TLObject$1 */
    static class C05211 extends ThreadLocal<NativeByteBuffer> {
        C05211() {
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
