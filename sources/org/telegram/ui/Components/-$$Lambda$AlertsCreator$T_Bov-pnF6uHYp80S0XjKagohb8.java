package org.telegram.ui.Components;

import org.telegram.ui.Components.NumberPicker.OnScrollListener;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$AlertsCreator$T_Bov-pnF6uHYp80S0XjKagohb8 implements OnScrollListener {
    private final /* synthetic */ boolean f$0;
    private final /* synthetic */ NumberPicker f$1;
    private final /* synthetic */ NumberPicker f$2;
    private final /* synthetic */ NumberPicker f$3;

    public /* synthetic */ -$$Lambda$AlertsCreator$T_Bov-pnF6uHYp80S0XjKagohb8(boolean z, NumberPicker numberPicker, NumberPicker numberPicker2, NumberPicker numberPicker3) {
        this.f$0 = z;
        this.f$1 = numberPicker;
        this.f$2 = numberPicker2;
        this.f$3 = numberPicker3;
    }

    public final void onScrollStateChange(NumberPicker numberPicker, int i) {
        AlertsCreator.lambda$createDatePickerDialog$14(this.f$0, this.f$1, this.f$2, this.f$3, numberPicker, i);
    }
}
