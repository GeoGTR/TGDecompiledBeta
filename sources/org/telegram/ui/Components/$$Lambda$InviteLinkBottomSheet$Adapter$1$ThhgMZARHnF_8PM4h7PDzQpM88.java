package org.telegram.ui.Components;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC$TL_error;

/* renamed from: org.telegram.ui.Components.-$$Lambda$InviteLinkBottomSheet$Adapter$1$Thhg-MZARHnF_8PM4h7PDzQpM88  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$InviteLinkBottomSheet$Adapter$1$ThhgMZARHnF_8PM4h7PDzQpM88 implements RequestDelegate {
    public static final /* synthetic */ $$Lambda$InviteLinkBottomSheet$Adapter$1$ThhgMZARHnF_8PM4h7PDzQpM88 INSTANCE = new $$Lambda$InviteLinkBottomSheet$Adapter$1$ThhgMZARHnF_8PM4h7PDzQpM88();

    private /* synthetic */ $$Lambda$InviteLinkBottomSheet$Adapter$1$ThhgMZARHnF_8PM4h7PDzQpM88() {
    }

    public final void run(TLObject tLObject, TLRPC$TL_error tLRPC$TL_error) {
        AndroidUtilities.runOnUIThread(
        /*  JADX ERROR: Method code generation error
            jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x0000: INVOKE  
              (r1v0 'tLObject' org.telegram.tgnet.TLObject)
              (r2v0 'tLRPC$TL_error' org.telegram.tgnet.TLRPC$TL_error)
             org.telegram.ui.Components.InviteLinkBottomSheet.Adapter.1.lambda$removeLink$3(org.telegram.tgnet.TLObject, org.telegram.tgnet.TLRPC$TL_error):void type: STATIC in method: org.telegram.ui.Components.-$$Lambda$InviteLinkBottomSheet$Adapter$1$Thhg-MZARHnF_8PM4h7PDzQpM88.run(org.telegram.tgnet.TLObject, org.telegram.tgnet.TLRPC$TL_error):void, dex: classes3.dex
            	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:256)
            	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:221)
            	at jadx.core.codegen.RegionGen.makeSimpleBlock(RegionGen.java:109)
            	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:55)
            	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
            	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
            	at jadx.core.codegen.MethodGen.addRegionInsns(MethodGen.java:211)
            	at jadx.core.codegen.MethodGen.addInstructions(MethodGen.java:204)
            	at jadx.core.codegen.ClassGen.addMethodCode(ClassGen.java:318)
            	at jadx.core.codegen.ClassGen.addMethod(ClassGen.java:271)
            	at jadx.core.codegen.ClassGen.lambda$addInnerClsAndMethods$2(ClassGen.java:240)
            	at java.util.stream.ForEachOps$ForEachOp$OfRef.accept(ForEachOps.java:183)
            	at java.util.ArrayList.forEach(ArrayList.java:1259)
            	at java.util.stream.SortedOps$RefSortingSink.end(SortedOps.java:395)
            	at java.util.stream.Sink$ChainedReference.end(Sink.java:258)
            	at java.util.stream.AbstractPipeline.copyInto(AbstractPipeline.java:483)
            	at java.util.stream.AbstractPipeline.wrapAndCopyInto(AbstractPipeline.java:472)
            	at java.util.stream.ForEachOps$ForEachOp.evaluateSequential(ForEachOps.java:150)
            	at java.util.stream.ForEachOps$ForEachOp$OfRef.evaluateSequential(ForEachOps.java:173)
            	at java.util.stream.AbstractPipeline.evaluate(AbstractPipeline.java:234)
            	at java.util.stream.ReferencePipeline.forEach(ReferencePipeline.java:485)
            	at jadx.core.codegen.ClassGen.addInnerClsAndMethods(ClassGen.java:236)
            	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:227)
            	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:112)
            	at jadx.core.codegen.ClassGen.makeClass(ClassGen.java:78)
            	at jadx.core.codegen.CodeGen.wrapCodeGen(CodeGen.java:44)
            	at jadx.core.codegen.CodeGen.generateJavaCode(CodeGen.java:33)
            	at jadx.core.codegen.CodeGen.generate(CodeGen.java:21)
            	at jadx.core.ProcessClass.generateCode(ProcessClass.java:61)
            	at jadx.core.dex.nodes.ClassNode.decompile(ClassNode.java:273)
            Caused by: jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x0005: INVOKE  
              (wrap: org.telegram.ui.Components.-$$Lambda$InviteLinkBottomSheet$Adapter$1$1YknZx5quR_82fD1BVYb-7a-S0w : 0x0002: CONSTRUCTOR  (r0v1 org.telegram.ui.Components.-$$Lambda$InviteLinkBottomSheet$Adapter$1$1YknZx5quR_82fD1BVYb-7a-S0w) = (r2v0 'tLRPC$TL_error' org.telegram.tgnet.TLRPC$TL_error) call: org.telegram.ui.Components.-$$Lambda$InviteLinkBottomSheet$Adapter$1$1YknZx5quR_82fD1BVYb-7a-S0w.<init>(org.telegram.tgnet.TLRPC$TL_error):void type: CONSTRUCTOR)
             org.telegram.messenger.AndroidUtilities.runOnUIThread(java.lang.Runnable):void type: STATIC in method: org.telegram.ui.Components.-$$Lambda$InviteLinkBottomSheet$Adapter$1$Thhg-MZARHnF_8PM4h7PDzQpM88.run(org.telegram.tgnet.TLObject, org.telegram.tgnet.TLRPC$TL_error):void, dex: classes3.dex
            	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:256)
            	at jadx.core.codegen.InsnGen.inlineMethod(InsnGen.java:924)
            	at jadx.core.codegen.InsnGen.makeInvoke(InsnGen.java:684)
            	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:368)
            	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:250)
            	... 29 more
            Caused by: jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x0002: CONSTRUCTOR  (r0v1 org.telegram.ui.Components.-$$Lambda$InviteLinkBottomSheet$Adapter$1$1YknZx5quR_82fD1BVYb-7a-S0w) = (r2v0 'tLRPC$TL_error' org.telegram.tgnet.TLRPC$TL_error) call: org.telegram.ui.Components.-$$Lambda$InviteLinkBottomSheet$Adapter$1$1YknZx5quR_82fD1BVYb-7a-S0w.<init>(org.telegram.tgnet.TLRPC$TL_error):void type: CONSTRUCTOR in method: org.telegram.ui.Components.-$$Lambda$InviteLinkBottomSheet$Adapter$1$Thhg-MZARHnF_8PM4h7PDzQpM88.run(org.telegram.tgnet.TLObject, org.telegram.tgnet.TLRPC$TL_error):void, dex: classes3.dex
            	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:256)
            	at jadx.core.codegen.InsnGen.addWrappedArg(InsnGen.java:123)
            	at jadx.core.codegen.InsnGen.addArg(InsnGen.java:107)
            	at jadx.core.codegen.InsnGen.generateMethodArguments(InsnGen.java:787)
            	at jadx.core.codegen.InsnGen.makeInvoke(InsnGen.java:728)
            	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:368)
            	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:231)
            	... 33 more
            Caused by: jadx.core.utils.exceptions.JadxRuntimeException: Expected class to be processed at this point, class: org.telegram.ui.Components.-$$Lambda$InviteLinkBottomSheet$Adapter$1$1YknZx5quR_82fD1BVYb-7a-S0w, state: NOT_LOADED
            	at jadx.core.dex.nodes.ClassNode.ensureProcessed(ClassNode.java:260)
            	at jadx.core.codegen.InsnGen.makeConstructor(InsnGen.java:606)
            	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:364)
            	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:231)
            	... 39 more
            */
        /*
            this = this;
            org.telegram.messenger.AndroidUtilities.runOnUIThread(new org.telegram.ui.Components.$$Lambda$InviteLinkBottomSheet$Adapter$1$1YknZx5quR_82fD1BVYb7aS0w(r2))
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.Components.$$Lambda$InviteLinkBottomSheet$Adapter$1$ThhgMZARHnF_8PM4h7PDzQpM88.run(org.telegram.tgnet.TLObject, org.telegram.tgnet.TLRPC$TL_error):void");
    }
}
