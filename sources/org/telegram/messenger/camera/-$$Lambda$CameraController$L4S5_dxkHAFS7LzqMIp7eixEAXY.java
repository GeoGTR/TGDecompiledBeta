package org.telegram.messenger.camera;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$CameraController$L4S5_dxkHAFS7LzqMIp7eixEAXY implements Runnable {
    private final /* synthetic */ CameraController f$0;
    private final /* synthetic */ CameraSession f$1;
    private final /* synthetic */ boolean f$2;

    public /* synthetic */ -$$Lambda$CameraController$L4S5_dxkHAFS7LzqMIp7eixEAXY(CameraController cameraController, CameraSession cameraSession, boolean z) {
        this.f$0 = cameraController;
        this.f$1 = cameraSession;
        this.f$2 = z;
    }

    public final void run() {
        this.f$0.lambda$stopVideoRecording$13$CameraController(this.f$1, this.f$2);
    }
}
