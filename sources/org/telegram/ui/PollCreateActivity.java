package org.telegram.ui;

import android.animation.Animator;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.HashMap;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.Utilities;
import org.telegram.tgnet.SerializedData;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.SimpleTextView;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.PollEditTextCell;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Components.AlertsCreator;
import org.telegram.ui.Components.CombinedDrawable;
import org.telegram.ui.Components.EditTextBoldCursor;
import org.telegram.ui.Components.HintView;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.PollCreateActivity;

public class PollCreateActivity extends BaseFragment {
    private static final int MAX_ANSWER_LENGTH = 100;
    private static final int MAX_QUESTION_LENGTH = 255;
    private static final int done_button = 1;
    /* access modifiers changed from: private */
    public int addAnswerRow;
    /* access modifiers changed from: private */
    public boolean anonymousPoll;
    /* access modifiers changed from: private */
    public int anonymousRow;
    /* access modifiers changed from: private */
    public int answerHeaderRow;
    /* access modifiers changed from: private */
    public int answerSectionRow;
    /* access modifiers changed from: private */
    public int answerStartRow;
    /* access modifiers changed from: private */
    public String[] answers = new String[10];
    /* access modifiers changed from: private */
    public boolean[] answersChecks = new boolean[10];
    /* access modifiers changed from: private */
    public int answersCount;
    /* access modifiers changed from: private */
    public PollCreateActivityDelegate delegate;
    /* access modifiers changed from: private */
    public ActionBarMenuItem doneItem;
    private boolean hintShowed;
    private HintView hintView;
    /* access modifiers changed from: private */
    public ListAdapter listAdapter;
    /* access modifiers changed from: private */
    public RecyclerListView listView;
    /* access modifiers changed from: private */
    public boolean multipleChoise;
    /* access modifiers changed from: private */
    public int multipleRow;
    /* access modifiers changed from: private */
    public ChatActivity parentFragment;
    /* access modifiers changed from: private */
    public int questionHeaderRow;
    /* access modifiers changed from: private */
    public int questionRow;
    /* access modifiers changed from: private */
    public int questionSectionRow;
    /* access modifiers changed from: private */
    public String questionString;
    /* access modifiers changed from: private */
    public int quizOnly;
    /* access modifiers changed from: private */
    public boolean quizPoll;
    /* access modifiers changed from: private */
    public int quizRow;
    /* access modifiers changed from: private */
    public int requestFieldFocusAtPosition;
    /* access modifiers changed from: private */
    public int rowCount;
    /* access modifiers changed from: private */
    public int settingsHeaderRow;
    /* access modifiers changed from: private */
    public int settingsSectionRow;

    public interface PollCreateActivityDelegate {
        void sendPoll(TLRPC.TL_messageMediaPoll tL_messageMediaPoll, HashMap<String, String> hashMap, boolean z, int i);
    }

    static /* synthetic */ int access$2010(PollCreateActivity pollCreateActivity) {
        int i = pollCreateActivity.answersCount;
        pollCreateActivity.answersCount = i - 1;
        return i;
    }

    public class TouchHelperCallback extends ItemTouchHelper.Callback {
        public boolean isLongPressDragEnabled() {
            return true;
        }

        public void onSwiped(RecyclerView.ViewHolder viewHolder, int i) {
        }

        public TouchHelperCallback() {
        }

        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            if (viewHolder.getItemViewType() != 5) {
                return ItemTouchHelper.Callback.makeMovementFlags(0, 0);
            }
            return ItemTouchHelper.Callback.makeMovementFlags(3, 0);
        }

        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder viewHolder2) {
            if (viewHolder.getItemViewType() != viewHolder2.getItemViewType()) {
                return false;
            }
            PollCreateActivity.this.listAdapter.swapElements(viewHolder.getAdapterPosition(), viewHolder2.getAdapterPosition());
            return true;
        }

        public void onChildDraw(Canvas canvas, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float f, float f2, int i, boolean z) {
            super.onChildDraw(canvas, recyclerView, viewHolder, f, f2, i, z);
        }

        public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int i) {
            if (i != 0) {
                PollCreateActivity.this.listView.cancelClickRunnables(false);
                viewHolder.itemView.setPressed(true);
            }
            super.onSelectedChanged(viewHolder, i);
        }

        public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            super.clearView(recyclerView, viewHolder);
            viewHolder.itemView.setPressed(false);
        }
    }

    public PollCreateActivity(ChatActivity chatActivity, Boolean bool) {
        int i = 1;
        this.answersCount = 1;
        this.anonymousPoll = true;
        this.requestFieldFocusAtPosition = -1;
        this.parentFragment = chatActivity;
        if (bool != null) {
            this.quizPoll = bool.booleanValue();
            this.quizOnly = !this.quizPoll ? 2 : i;
        }
    }

    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        updateRows();
        return true;
    }

    public View createView(Context context) {
        this.actionBar.setBackButtonImage(NUM);
        if (this.quizOnly == 1) {
            this.actionBar.setTitle(LocaleController.getString("NewQuiz", NUM));
        } else {
            this.actionBar.setTitle(LocaleController.getString("NewPoll", NUM));
        }
        if (AndroidUtilities.isTablet()) {
            this.actionBar.setOccupyStatusBar(false);
        }
        this.actionBar.setAllowOverlayTitle(true);
        this.actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            public void onItemClick(int i) {
                if (i == -1) {
                    if (PollCreateActivity.this.checkDiscard()) {
                        PollCreateActivity.this.finishFragment();
                    }
                } else if (i == 1) {
                    if (!PollCreateActivity.this.quizPoll || PollCreateActivity.this.doneItem.getAlpha() == 1.0f) {
                        TLRPC.TL_messageMediaPoll tL_messageMediaPoll = new TLRPC.TL_messageMediaPoll();
                        tL_messageMediaPoll.poll = new TLRPC.TL_poll();
                        tL_messageMediaPoll.poll.multiple_choice = PollCreateActivity.this.multipleChoise;
                        tL_messageMediaPoll.poll.quiz = PollCreateActivity.this.quizPoll;
                        tL_messageMediaPoll.poll.public_voters = !PollCreateActivity.this.anonymousPoll;
                        TLRPC.TL_poll tL_poll = tL_messageMediaPoll.poll;
                        PollCreateActivity pollCreateActivity = PollCreateActivity.this;
                        tL_poll.question = pollCreateActivity.getFixedString(pollCreateActivity.questionString);
                        SerializedData serializedData = new SerializedData(10);
                        for (int i2 = 0; i2 < PollCreateActivity.this.answers.length; i2++) {
                            PollCreateActivity pollCreateActivity2 = PollCreateActivity.this;
                            if (!TextUtils.isEmpty(pollCreateActivity2.getFixedString(pollCreateActivity2.answers[i2]))) {
                                TLRPC.TL_pollAnswer tL_pollAnswer = new TLRPC.TL_pollAnswer();
                                PollCreateActivity pollCreateActivity3 = PollCreateActivity.this;
                                tL_pollAnswer.text = pollCreateActivity3.getFixedString(pollCreateActivity3.answers[i2]);
                                tL_pollAnswer.option = new byte[1];
                                tL_pollAnswer.option[0] = (byte) (tL_messageMediaPoll.poll.answers.size() + 48);
                                tL_messageMediaPoll.poll.answers.add(tL_pollAnswer);
                                if ((PollCreateActivity.this.multipleChoise || PollCreateActivity.this.quizPoll) && PollCreateActivity.this.answersChecks[i2]) {
                                    serializedData.writeByte(tL_pollAnswer.option[0]);
                                }
                            }
                        }
                        HashMap hashMap = new HashMap();
                        hashMap.put("answers", Utilities.bytesToHex(serializedData.toByteArray()));
                        tL_messageMediaPoll.results = new TLRPC.TL_pollResults();
                        if (PollCreateActivity.this.parentFragment.isInScheduleMode()) {
                            AlertsCreator.createScheduleDatePickerDialog(PollCreateActivity.this.getParentActivity(), PollCreateActivity.this.parentFragment.getDialogId(), new AlertsCreator.ScheduleDatePickerDelegate(tL_messageMediaPoll, hashMap) {
                                private final /* synthetic */ TLRPC.TL_messageMediaPoll f$1;
                                private final /* synthetic */ HashMap f$2;

                                {
                                    this.f$1 = r2;
                                    this.f$2 = r3;
                                }

                                public final void didSelectDate(boolean z, int i) {
                                    PollCreateActivity.AnonymousClass1.this.lambda$onItemClick$0$PollCreateActivity$1(this.f$1, this.f$2, z, i);
                                }
                            });
                            return;
                        }
                        PollCreateActivity.this.delegate.sendPoll(tL_messageMediaPoll, hashMap, true, 0);
                        PollCreateActivity.this.finishFragment();
                        return;
                    }
                    int i3 = 0;
                    for (int i4 = 0; i4 < PollCreateActivity.this.answersChecks.length; i4++) {
                        PollCreateActivity pollCreateActivity4 = PollCreateActivity.this;
                        if (!TextUtils.isEmpty(pollCreateActivity4.getFixedString(pollCreateActivity4.answers[i4])) && PollCreateActivity.this.answersChecks[i4]) {
                            i3++;
                        }
                    }
                    if (i3 <= 0) {
                        PollCreateActivity.this.showQuizHint();
                    }
                }
            }

            public /* synthetic */ void lambda$onItemClick$0$PollCreateActivity$1(TLRPC.TL_messageMediaPoll tL_messageMediaPoll, HashMap hashMap, boolean z, int i) {
                PollCreateActivity.this.delegate.sendPoll(tL_messageMediaPoll, hashMap, z, i);
                PollCreateActivity.this.finishFragment();
            }
        });
        this.doneItem = this.actionBar.createMenu().addItem(1, (CharSequence) LocaleController.getString("Create", NUM).toUpperCase());
        this.listAdapter = new ListAdapter(context);
        this.fragmentView = new FrameLayout(context);
        this.fragmentView.setBackgroundColor(Theme.getColor("windowBackgroundGray"));
        FrameLayout frameLayout = (FrameLayout) this.fragmentView;
        this.listView = new RecyclerListView(context) {
            /* access modifiers changed from: protected */
            public void requestChildOnScreen(View view, View view2) {
                if (view instanceof PollEditTextCell) {
                    super.requestChildOnScreen(view, view2);
                }
            }

            public boolean requestChildRectangleOnScreen(View view, Rect rect, boolean z) {
                rect.bottom += AndroidUtilities.dp(60.0f);
                return super.requestChildRectangleOnScreen(view, rect, z);
            }

            /* access modifiers changed from: protected */
            public void onMeasure(int i, int i2) {
                super.onMeasure(i, i2);
            }

            public void requestLayout() {
                super.requestLayout();
            }
        };
        this.listView.setVerticalScrollBarEnabled(false);
        ((DefaultItemAnimator) this.listView.getItemAnimator()).setDelayAnimations(false);
        this.listView.setLayoutManager(new LinearLayoutManager(context, 1, false));
        new ItemTouchHelper(new TouchHelperCallback()).attachToRecyclerView(this.listView);
        frameLayout.addView(this.listView, LayoutHelper.createFrame(-1, -1, 51));
        this.listView.setAdapter(this.listAdapter);
        this.listView.setOnItemClickListener((RecyclerListView.OnItemClickListener) new RecyclerListView.OnItemClickListener() {
            public final void onItemClick(View view, int i) {
                PollCreateActivity.this.lambda$createView$0$PollCreateActivity(view, i);
            }
        });
        this.hintView = new HintView(context, 4);
        this.hintView.setText(LocaleController.getString("PollTapToSelect", NUM));
        this.hintView.setAlpha(0.0f);
        this.hintView.setVisibility(4);
        frameLayout.addView(this.hintView, LayoutHelper.createFrame(-2, -2.0f, 51, 19.0f, 0.0f, 19.0f, 0.0f));
        checkDoneButton();
        return this.fragmentView;
    }

    public /* synthetic */ void lambda$createView$0$PollCreateActivity(View view, int i) {
        boolean z;
        if (i == this.addAnswerRow) {
            addNewField();
        } else if (view instanceof TextCheckCell) {
            TextCheckCell textCheckCell = (TextCheckCell) view;
            boolean z2 = this.quizPoll;
            if (i == this.anonymousRow) {
                z = !this.anonymousPoll;
                this.anonymousPoll = z;
            } else {
                int i2 = this.multipleRow;
                if (i == i2) {
                    z = !this.multipleChoise;
                    this.multipleChoise = z;
                    if (this.multipleChoise && z2) {
                        this.quizPoll = false;
                        RecyclerView.ViewHolder findViewHolderForAdapterPosition = this.listView.findViewHolderForAdapterPosition(this.quizRow);
                        if (findViewHolderForAdapterPosition != null) {
                            ((TextCheckCell) findViewHolderForAdapterPosition.itemView).setChecked(false);
                        } else {
                            this.listAdapter.notifyItemChanged(this.quizRow);
                        }
                    }
                } else if (this.quizOnly == 0) {
                    boolean z3 = !z2;
                    this.quizPoll = z3;
                    if (this.quizPoll && this.multipleChoise) {
                        this.multipleChoise = false;
                        RecyclerView.ViewHolder findViewHolderForAdapterPosition2 = this.listView.findViewHolderForAdapterPosition(i2);
                        if (findViewHolderForAdapterPosition2 != null) {
                            ((TextCheckCell) findViewHolderForAdapterPosition2.itemView).setChecked(false);
                        } else {
                            this.listAdapter.notifyItemChanged(this.multipleRow);
                        }
                    }
                    if (this.quizPoll) {
                        int i3 = 0;
                        boolean z4 = false;
                        while (true) {
                            boolean[] zArr = this.answersChecks;
                            if (i3 >= zArr.length) {
                                break;
                            }
                            if (z4) {
                                zArr[i3] = false;
                            } else if (zArr[i3]) {
                                z4 = true;
                            }
                            i3++;
                        }
                    }
                    z = z3;
                } else {
                    return;
                }
            }
            if (this.hintShowed && !this.quizPoll) {
                this.hintView.hide();
            }
            this.listView.getChildCount();
            for (int i4 = this.answerStartRow; i4 < this.answerStartRow + this.answersCount; i4++) {
                RecyclerView.ViewHolder findViewHolderForAdapterPosition3 = this.listView.findViewHolderForAdapterPosition(i4);
                if (findViewHolderForAdapterPosition3 != null) {
                    View view2 = findViewHolderForAdapterPosition3.itemView;
                    if (view2 instanceof PollEditTextCell) {
                        PollEditTextCell pollEditTextCell = (PollEditTextCell) view2;
                        pollEditTextCell.setShowCheckBox(this.quizPoll, true);
                        pollEditTextCell.setChecked(this.answersChecks[i4 - this.answerStartRow], z2);
                        if (pollEditTextCell.getTop() > AndroidUtilities.dp(40.0f) && i == this.quizRow && !this.hintShowed) {
                            this.hintView.showForView(pollEditTextCell.getCheckBox(), true);
                            this.hintShowed = true;
                        }
                    }
                }
            }
            textCheckCell.setChecked(z);
            checkDoneButton();
        }
    }

    public void onResume() {
        super.onResume();
        ListAdapter listAdapter2 = this.listAdapter;
        if (listAdapter2 != null) {
            listAdapter2.notifyDataSetChanged();
        }
    }

    /* access modifiers changed from: private */
    public String getFixedString(String str) {
        if (TextUtils.isEmpty(str)) {
            return str;
        }
        String charSequence = AndroidUtilities.getTrimmedString(str).toString();
        while (charSequence.contains("\n\n\n")) {
            charSequence = charSequence.replace("\n\n\n", "\n\n");
        }
        while (charSequence.startsWith("\n\n\n")) {
            charSequence = charSequence.replace("\n\n\n", "\n\n");
        }
        return charSequence;
    }

    /* access modifiers changed from: private */
    public void showQuizHint() {
        this.listView.getChildCount();
        for (int i = this.answerStartRow; i < this.answerStartRow + this.answersCount; i++) {
            RecyclerView.ViewHolder findViewHolderForAdapterPosition = this.listView.findViewHolderForAdapterPosition(i);
            if (findViewHolderForAdapterPosition != null) {
                View view = findViewHolderForAdapterPosition.itemView;
                if (view instanceof PollEditTextCell) {
                    PollEditTextCell pollEditTextCell = (PollEditTextCell) view;
                    if (pollEditTextCell.getTop() > AndroidUtilities.dp(40.0f)) {
                        this.hintView.showForView(pollEditTextCell.getCheckBox(), true);
                        return;
                    }
                } else {
                    continue;
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x0083  */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x0086  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void checkDoneButton() {
        /*
            r7 = this;
            boolean r0 = r7.quizPoll
            r1 = 0
            if (r0 == 0) goto L_0x0025
            r0 = 0
            r2 = 0
        L_0x0007:
            boolean[] r3 = r7.answersChecks
            int r3 = r3.length
            if (r0 >= r3) goto L_0x0026
            java.lang.String[] r3 = r7.answers
            r3 = r3[r0]
            java.lang.String r3 = r7.getFixedString(r3)
            boolean r3 = android.text.TextUtils.isEmpty(r3)
            if (r3 != 0) goto L_0x0022
            boolean[] r3 = r7.answersChecks
            boolean r3 = r3[r0]
            if (r3 == 0) goto L_0x0022
            int r2 = r2 + 1
        L_0x0022:
            int r0 = r0 + 1
            goto L_0x0007
        L_0x0025:
            r2 = 0
        L_0x0026:
            java.lang.String r0 = r7.questionString
            java.lang.String r0 = r7.getFixedString(r0)
            boolean r0 = android.text.TextUtils.isEmpty(r0)
            r3 = 1
            if (r0 != 0) goto L_0x0070
            java.lang.String r0 = r7.questionString
            int r0 = r0.length()
            r4 = 255(0xff, float:3.57E-43)
            if (r0 <= r4) goto L_0x003e
            goto L_0x0070
        L_0x003e:
            r0 = 0
            r4 = 0
        L_0x0040:
            java.lang.String[] r5 = r7.answers
            int r6 = r5.length
            if (r0 >= r6) goto L_0x0064
            r5 = r5[r0]
            java.lang.String r5 = r7.getFixedString(r5)
            boolean r5 = android.text.TextUtils.isEmpty(r5)
            if (r5 != 0) goto L_0x0061
            java.lang.String[] r5 = r7.answers
            r5 = r5[r0]
            int r5 = r5.length()
            r6 = 100
            if (r5 <= r6) goto L_0x005f
            r4 = 0
            goto L_0x0064
        L_0x005f:
            int r4 = r4 + 1
        L_0x0061:
            int r0 = r0 + 1
            goto L_0x0040
        L_0x0064:
            r0 = 2
            if (r4 < r0) goto L_0x0070
            boolean r0 = r7.quizPoll
            if (r0 == 0) goto L_0x006e
            if (r2 >= r3) goto L_0x006e
            goto L_0x0070
        L_0x006e:
            r0 = 1
            goto L_0x0071
        L_0x0070:
            r0 = 0
        L_0x0071:
            org.telegram.ui.ActionBar.ActionBarMenuItem r4 = r7.doneItem
            boolean r5 = r7.quizPoll
            if (r5 == 0) goto L_0x0079
            if (r2 == 0) goto L_0x007b
        L_0x0079:
            if (r0 == 0) goto L_0x007c
        L_0x007b:
            r1 = 1
        L_0x007c:
            r4.setEnabled(r1)
            org.telegram.ui.ActionBar.ActionBarMenuItem r1 = r7.doneItem
            if (r0 == 0) goto L_0x0086
            r0 = 1065353216(0x3var_, float:1.0)
            goto L_0x0088
        L_0x0086:
            r0 = 1056964608(0x3var_, float:0.5)
        L_0x0088:
            r1.setAlpha(r0)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.PollCreateActivity.checkDoneButton():void");
    }

    /* access modifiers changed from: private */
    public void updateRows() {
        this.rowCount = 0;
        int i = this.rowCount;
        this.rowCount = i + 1;
        this.questionHeaderRow = i;
        int i2 = this.rowCount;
        this.rowCount = i2 + 1;
        this.questionRow = i2;
        int i3 = this.rowCount;
        this.rowCount = i3 + 1;
        this.questionSectionRow = i3;
        int i4 = this.rowCount;
        this.rowCount = i4 + 1;
        this.answerHeaderRow = i4;
        int i5 = this.answersCount;
        if (i5 != 0) {
            int i6 = this.rowCount;
            this.answerStartRow = i6;
            this.rowCount = i6 + i5;
        } else {
            this.answerStartRow = -1;
        }
        if (this.answersCount != this.answers.length) {
            int i7 = this.rowCount;
            this.rowCount = i7 + 1;
            this.addAnswerRow = i7;
        } else {
            this.addAnswerRow = -1;
        }
        int i8 = this.rowCount;
        this.rowCount = i8 + 1;
        this.answerSectionRow = i8;
        int i9 = this.rowCount;
        this.rowCount = i9 + 1;
        this.settingsHeaderRow = i9;
        TLRPC.Chat currentChat = this.parentFragment.getCurrentChat();
        if (!ChatObject.isChannel(currentChat) || currentChat.megagroup) {
            int i10 = this.rowCount;
            this.rowCount = i10 + 1;
            this.anonymousRow = i10;
        } else {
            this.anonymousRow = -1;
        }
        if (this.quizOnly != 1) {
            int i11 = this.rowCount;
            this.rowCount = i11 + 1;
            this.multipleRow = i11;
        } else {
            this.multipleRow = -1;
        }
        if (this.quizOnly == 0) {
            int i12 = this.rowCount;
            this.rowCount = i12 + 1;
            this.quizRow = i12;
        } else {
            this.quizRow = -1;
        }
        int i13 = this.rowCount;
        this.rowCount = i13 + 1;
        this.settingsSectionRow = i13;
    }

    public boolean onBackPressed() {
        return checkDiscard();
    }

    /* access modifiers changed from: private */
    public boolean checkDiscard() {
        boolean isEmpty = TextUtils.isEmpty(getFixedString(this.questionString));
        if (isEmpty) {
            int i = 0;
            while (i < this.answersCount && (isEmpty = TextUtils.isEmpty(getFixedString(this.answers[i])))) {
                i++;
            }
        }
        if (!isEmpty) {
            AlertDialog.Builder builder = new AlertDialog.Builder((Context) getParentActivity());
            builder.setTitle(LocaleController.getString("CancelPollAlertTitle", NUM));
            builder.setMessage(LocaleController.getString("CancelPollAlertText", NUM));
            builder.setPositiveButton(LocaleController.getString("PassportDiscard", NUM), new DialogInterface.OnClickListener() {
                public final void onClick(DialogInterface dialogInterface, int i) {
                    PollCreateActivity.this.lambda$checkDiscard$1$PollCreateActivity(dialogInterface, i);
                }
            });
            builder.setNegativeButton(LocaleController.getString("Cancel", NUM), (DialogInterface.OnClickListener) null);
            showDialog(builder.create());
        }
        return isEmpty;
    }

    public /* synthetic */ void lambda$checkDiscard$1$PollCreateActivity(DialogInterface dialogInterface, int i) {
        finishFragment();
    }

    public void setDelegate(PollCreateActivityDelegate pollCreateActivityDelegate) {
        this.delegate = pollCreateActivityDelegate;
    }

    /* access modifiers changed from: private */
    public void setTextLeft(View view, int i) {
        String str = "windowBackgroundWhiteRedText5";
        if (view instanceof HeaderCell) {
            HeaderCell headerCell = (HeaderCell) view;
            if (i == -1) {
                String str2 = this.questionString;
                int length = 255 - (str2 != null ? str2.length() : 0);
                if (((float) length) <= 76.5f) {
                    headerCell.setText2(String.format("%d", new Object[]{Integer.valueOf(length)}));
                    SimpleTextView textView2 = headerCell.getTextView2();
                    if (length >= 0) {
                        str = "windowBackgroundWhiteGrayText3";
                    }
                    textView2.setTextColor(Theme.getColor(str));
                    textView2.setTag(str);
                    return;
                }
                headerCell.setText2("");
                return;
            }
            headerCell.setText2("");
        } else if ((view instanceof PollEditTextCell) && i >= 0) {
            PollEditTextCell pollEditTextCell = (PollEditTextCell) view;
            String[] strArr = this.answers;
            int length2 = 100 - (strArr[i] != null ? strArr[i].length() : 0);
            if (((float) length2) <= 30.0f) {
                pollEditTextCell.setText2(String.format("%d", new Object[]{Integer.valueOf(length2)}));
                SimpleTextView textView22 = pollEditTextCell.getTextView2();
                if (length2 >= 0) {
                    str = "windowBackgroundWhiteGrayText3";
                }
                textView22.setTextColor(Theme.getColor(str));
                textView22.setTag(str);
                return;
            }
            pollEditTextCell.setText2("");
        }
    }

    /* access modifiers changed from: private */
    public void addNewField() {
        boolean[] zArr = this.answersChecks;
        int i = this.answersCount;
        zArr[i] = false;
        this.answersCount = i + 1;
        if (this.answersCount == this.answers.length) {
            this.listAdapter.notifyItemRemoved(this.addAnswerRow);
        }
        this.listAdapter.notifyItemInserted(this.addAnswerRow);
        updateRows();
        this.requestFieldFocusAtPosition = (this.answerStartRow + this.answersCount) - 1;
        this.listAdapter.notifyItemChanged(this.answerSectionRow);
    }

    private class ListAdapter extends RecyclerListView.SelectionAdapter {
        private Context mContext;

        public ListAdapter(Context context) {
            this.mContext = context;
        }

        public int getItemCount() {
            return PollCreateActivity.this.rowCount;
        }

        public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {
            int itemViewType = viewHolder.getItemViewType();
            boolean z = true;
            if (itemViewType != 0) {
                boolean z2 = false;
                if (itemViewType == 6) {
                    TextCheckCell textCheckCell = (TextCheckCell) viewHolder.itemView;
                    if (i == PollCreateActivity.this.anonymousRow) {
                        String string = LocaleController.getString("PollAnonymous", NUM);
                        boolean access$1000 = PollCreateActivity.this.anonymousPoll;
                        if (!(PollCreateActivity.this.multipleRow == -1 && PollCreateActivity.this.quizRow == -1)) {
                            z2 = true;
                        }
                        textCheckCell.setTextAndCheck(string, access$1000, z2);
                        textCheckCell.setEnabled(true, (ArrayList<Animator>) null);
                    } else if (i == PollCreateActivity.this.multipleRow) {
                        String string2 = LocaleController.getString("PollMultiple", NUM);
                        boolean access$900 = PollCreateActivity.this.multipleChoise;
                        if (PollCreateActivity.this.quizRow != -1) {
                            z2 = true;
                        }
                        textCheckCell.setTextAndCheck(string2, access$900, z2);
                        textCheckCell.setEnabled(true, (ArrayList<Animator>) null);
                    } else if (i == PollCreateActivity.this.quizRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("PollQuiz", NUM), PollCreateActivity.this.quizPoll, false);
                        if (PollCreateActivity.this.quizOnly != 0) {
                            z = false;
                        }
                        textCheckCell.setEnabled(z, (ArrayList<Animator>) null);
                    }
                } else if (itemViewType == 2) {
                    TextInfoPrivacyCell textInfoPrivacyCell = (TextInfoPrivacyCell) viewHolder.itemView;
                    textInfoPrivacyCell.setBackgroundDrawable(Theme.getThemedDrawable(this.mContext, NUM, "windowBackgroundGrayShadow"));
                    if (i == PollCreateActivity.this.settingsSectionRow) {
                        if (PollCreateActivity.this.quizOnly != 0) {
                            textInfoPrivacyCell.setText((CharSequence) null);
                        } else {
                            textInfoPrivacyCell.setText(LocaleController.getString("QuizInfo", NUM));
                        }
                    } else if (10 - PollCreateActivity.this.answersCount <= 0) {
                        textInfoPrivacyCell.setText(LocaleController.getString("AddAnOptionInfoMax", NUM));
                    } else {
                        textInfoPrivacyCell.setText(LocaleController.formatString("AddAnOptionInfo", NUM, LocaleController.formatPluralString("Option", 10 - PollCreateActivity.this.answersCount)));
                    }
                } else if (itemViewType == 3) {
                    TextCell textCell = (TextCell) viewHolder.itemView;
                    textCell.setColors((String) null, "windowBackgroundWhiteBlueText4");
                    Drawable drawable = this.mContext.getResources().getDrawable(NUM);
                    Drawable drawable2 = this.mContext.getResources().getDrawable(NUM);
                    drawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor("switchTrackChecked"), PorterDuff.Mode.MULTIPLY));
                    drawable2.setColorFilter(new PorterDuffColorFilter(Theme.getColor("checkboxCheck"), PorterDuff.Mode.MULTIPLY));
                    textCell.setTextAndIcon(LocaleController.getString("AddAnOption", NUM), (Drawable) new CombinedDrawable(drawable, drawable2), false);
                }
            } else {
                HeaderCell headerCell = (HeaderCell) viewHolder.itemView;
                if (i == PollCreateActivity.this.questionHeaderRow) {
                    headerCell.setText(LocaleController.getString("Question", NUM));
                } else if (i == PollCreateActivity.this.answerHeaderRow) {
                    if (PollCreateActivity.this.quizOnly == 1) {
                        headerCell.setText(LocaleController.getString("QuizAnswers", NUM));
                    } else {
                        headerCell.setText(LocaleController.getString("PollOptions", NUM));
                    }
                } else if (i == PollCreateActivity.this.settingsHeaderRow) {
                    headerCell.setText(LocaleController.getString("Settings", NUM));
                }
            }
        }

        public void onViewAttachedToWindow(RecyclerView.ViewHolder viewHolder) {
            int itemViewType = viewHolder.getItemViewType();
            if (itemViewType == 0 || itemViewType == 5) {
                PollCreateActivity.this.setTextLeft(viewHolder.itemView, viewHolder.getAdapterPosition() == PollCreateActivity.this.questionHeaderRow ? -1 : 0);
            }
            if (itemViewType == 4) {
                PollEditTextCell pollEditTextCell = (PollEditTextCell) viewHolder.itemView;
                pollEditTextCell.setTag(1);
                pollEditTextCell.setTextAndHint(PollCreateActivity.this.questionString != null ? PollCreateActivity.this.questionString : "", LocaleController.getString("QuestionHint", NUM), false);
                pollEditTextCell.setTag((Object) null);
            } else if (itemViewType == 5) {
                int adapterPosition = viewHolder.getAdapterPosition();
                PollEditTextCell pollEditTextCell2 = (PollEditTextCell) viewHolder.itemView;
                pollEditTextCell2.setTag(1);
                pollEditTextCell2.setTextAndHint(PollCreateActivity.this.answers[adapterPosition - PollCreateActivity.this.answerStartRow], LocaleController.getString("OptionHint", NUM), true);
                pollEditTextCell2.setTag((Object) null);
                if (PollCreateActivity.this.requestFieldFocusAtPosition == adapterPosition) {
                    EditTextBoldCursor textView = pollEditTextCell2.getTextView();
                    textView.requestFocus();
                    AndroidUtilities.showKeyboard(textView);
                    int unused = PollCreateActivity.this.requestFieldFocusAtPosition = -1;
                }
                PollCreateActivity pollCreateActivity = PollCreateActivity.this;
                pollCreateActivity.setTextLeft(viewHolder.itemView, adapterPosition - pollCreateActivity.answerStartRow);
            }
        }

        public void onViewDetachedFromWindow(RecyclerView.ViewHolder viewHolder) {
            if (viewHolder.getItemViewType() == 4) {
                EditTextBoldCursor textView = ((PollEditTextCell) viewHolder.itemView).getTextView();
                if (textView.isFocused()) {
                    textView.clearFocus();
                    AndroidUtilities.hideKeyboard(textView);
                }
            }
        }

        public boolean isEnabled(RecyclerView.ViewHolder viewHolder) {
            int adapterPosition = viewHolder.getAdapterPosition();
            return adapterPosition == PollCreateActivity.this.addAnswerRow || adapterPosition == PollCreateActivity.this.anonymousRow || adapterPosition == PollCreateActivity.this.multipleRow || (PollCreateActivity.this.quizOnly == 0 && adapterPosition == PollCreateActivity.this.quizRow);
        }

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r8v1, resolved type: org.telegram.ui.Cells.HeaderCell} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r8v6, resolved type: org.telegram.ui.Cells.PollEditTextCell} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r8v7, resolved type: org.telegram.ui.Cells.TextCheckCell} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v10, resolved type: org.telegram.ui.Cells.HeaderCell} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r8v9, resolved type: org.telegram.ui.Cells.HeaderCell} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r8v10, resolved type: org.telegram.ui.Cells.HeaderCell} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r8v11, resolved type: org.telegram.ui.Cells.HeaderCell} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r8v12, resolved type: org.telegram.ui.Cells.HeaderCell} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r8v13, resolved type: org.telegram.ui.Cells.HeaderCell} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r8v14, resolved type: org.telegram.ui.Cells.HeaderCell} */
        /* JADX WARNING: type inference failed for: r8v3, types: [org.telegram.ui.Cells.ShadowSectionCell] */
        /* JADX WARNING: type inference failed for: r8v4, types: [org.telegram.ui.Cells.TextInfoPrivacyCell] */
        /* JADX WARNING: type inference failed for: r8v5, types: [org.telegram.ui.Cells.TextCell, android.view.View] */
        /* JADX WARNING: type inference failed for: r8v8, types: [org.telegram.ui.Cells.PollEditTextCell, org.telegram.ui.PollCreateActivity$ListAdapter$2, android.widget.FrameLayout] */
        /* JADX WARNING: Multi-variable type inference failed */
        /* JADX WARNING: Unknown variable types count: 2 */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public androidx.recyclerview.widget.RecyclerView.ViewHolder onCreateViewHolder(android.view.ViewGroup r7, int r8) {
            /*
                r6 = this;
                java.lang.String r7 = "windowBackgroundWhite"
                if (r8 == 0) goto L_0x0096
                r0 = 1
                if (r8 == r0) goto L_0x008e
                r1 = 2
                if (r8 == r1) goto L_0x0086
                r1 = 3
                if (r8 == r1) goto L_0x0077
                r1 = 4
                if (r8 == r1) goto L_0x005f
                r1 = 6
                if (r8 == r1) goto L_0x0050
                org.telegram.ui.PollCreateActivity$ListAdapter$2 r8 = new org.telegram.ui.PollCreateActivity$ListAdapter$2
                android.content.Context r1 = r6.mContext
                org.telegram.ui.-$$Lambda$PollCreateActivity$ListAdapter$xPCTWTnLONqvpxDTUQukvNxp8wU r2 = new org.telegram.ui.-$$Lambda$PollCreateActivity$ListAdapter$xPCTWTnLONqvpxDTUQukvNxp8wU
                r2.<init>()
                r8.<init>(r1, r2)
                int r7 = org.telegram.ui.ActionBar.Theme.getColor(r7)
                r8.setBackgroundColor(r7)
                org.telegram.ui.PollCreateActivity$ListAdapter$3 r7 = new org.telegram.ui.PollCreateActivity$ListAdapter$3
                r7.<init>(r8)
                r8.addTextWatcher(r7)
                r8.setShowNextButton(r0)
                org.telegram.ui.Components.EditTextBoldCursor r7 = r8.getTextView()
                int r0 = r7.getImeOptions()
                r0 = r0 | 5
                r7.setImeOptions(r0)
                org.telegram.ui.-$$Lambda$PollCreateActivity$ListAdapter$fHXG5XRT2mioX4wywMDd12jepYQ r0 = new org.telegram.ui.-$$Lambda$PollCreateActivity$ListAdapter$fHXG5XRT2mioX4wywMDd12jepYQ
                r0.<init>(r8)
                r7.setOnEditorActionListener(r0)
                org.telegram.ui.-$$Lambda$PollCreateActivity$ListAdapter$0Cdu-jo6aZB372n9nZKgtvUqB-g r0 = new org.telegram.ui.-$$Lambda$PollCreateActivity$ListAdapter$0Cdu-jo6aZB372n9nZKgtvUqB-g
                r0.<init>()
                r7.setOnKeyListener(r0)
                goto L_0x00ab
            L_0x0050:
                org.telegram.ui.Cells.TextCheckCell r8 = new org.telegram.ui.Cells.TextCheckCell
                android.content.Context r0 = r6.mContext
                r8.<init>(r0)
                int r7 = org.telegram.ui.ActionBar.Theme.getColor(r7)
                r8.setBackgroundColor(r7)
                goto L_0x00ab
            L_0x005f:
                org.telegram.ui.Cells.PollEditTextCell r8 = new org.telegram.ui.Cells.PollEditTextCell
                android.content.Context r0 = r6.mContext
                r1 = 0
                r8.<init>(r0, r1)
                int r7 = org.telegram.ui.ActionBar.Theme.getColor(r7)
                r8.setBackgroundColor(r7)
                org.telegram.ui.PollCreateActivity$ListAdapter$1 r7 = new org.telegram.ui.PollCreateActivity$ListAdapter$1
                r7.<init>(r8)
                r8.addTextWatcher(r7)
                goto L_0x00ab
            L_0x0077:
                org.telegram.ui.Cells.TextCell r8 = new org.telegram.ui.Cells.TextCell
                android.content.Context r0 = r6.mContext
                r8.<init>(r0)
                int r7 = org.telegram.ui.ActionBar.Theme.getColor(r7)
                r8.setBackgroundColor(r7)
                goto L_0x00ab
            L_0x0086:
                org.telegram.ui.Cells.TextInfoPrivacyCell r8 = new org.telegram.ui.Cells.TextInfoPrivacyCell
                android.content.Context r7 = r6.mContext
                r8.<init>(r7)
                goto L_0x00ab
            L_0x008e:
                org.telegram.ui.Cells.ShadowSectionCell r8 = new org.telegram.ui.Cells.ShadowSectionCell
                android.content.Context r7 = r6.mContext
                r8.<init>(r7)
                goto L_0x00ab
            L_0x0096:
                org.telegram.ui.Cells.HeaderCell r8 = new org.telegram.ui.Cells.HeaderCell
                android.content.Context r1 = r6.mContext
                r2 = 0
                r3 = 21
                r4 = 15
                r5 = 1
                r0 = r8
                r0.<init>(r1, r2, r3, r4, r5)
                int r7 = org.telegram.ui.ActionBar.Theme.getColor(r7)
                r8.setBackgroundColor(r7)
            L_0x00ab:
                androidx.recyclerview.widget.RecyclerView$LayoutParams r7 = new androidx.recyclerview.widget.RecyclerView$LayoutParams
                r0 = -1
                r1 = -2
                r7.<init>((int) r0, (int) r1)
                r8.setLayoutParams(r7)
                org.telegram.ui.Components.RecyclerListView$Holder r7 = new org.telegram.ui.Components.RecyclerListView$Holder
                r7.<init>(r8)
                return r7
            */
            throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.PollCreateActivity.ListAdapter.onCreateViewHolder(android.view.ViewGroup, int):androidx.recyclerview.widget.RecyclerView$ViewHolder");
        }

        public /* synthetic */ void lambda$onCreateViewHolder$0$PollCreateActivity$ListAdapter(View view) {
            int adapterPosition;
            if (view.getTag() == null) {
                view.setTag(1);
                PollEditTextCell pollEditTextCell = (PollEditTextCell) view.getParent();
                RecyclerView.ViewHolder findContainingViewHolder = PollCreateActivity.this.listView.findContainingViewHolder(pollEditTextCell);
                if (findContainingViewHolder != null && (adapterPosition = findContainingViewHolder.getAdapterPosition()) != -1) {
                    int access$2500 = adapterPosition - PollCreateActivity.this.answerStartRow;
                    PollCreateActivity.this.listAdapter.notifyItemRemoved(adapterPosition);
                    int i = access$2500 + 1;
                    System.arraycopy(PollCreateActivity.this.answers, i, PollCreateActivity.this.answers, access$2500, (PollCreateActivity.this.answers.length - 1) - access$2500);
                    System.arraycopy(PollCreateActivity.this.answersChecks, i, PollCreateActivity.this.answersChecks, access$2500, (PollCreateActivity.this.answersChecks.length - 1) - access$2500);
                    PollCreateActivity.this.answers[PollCreateActivity.this.answers.length - 1] = null;
                    PollCreateActivity.this.answersChecks[PollCreateActivity.this.answersChecks.length - 1] = false;
                    PollCreateActivity.access$2010(PollCreateActivity.this);
                    if (PollCreateActivity.this.answersCount == PollCreateActivity.this.answers.length - 1) {
                        PollCreateActivity.this.listAdapter.notifyItemInserted((PollCreateActivity.this.answerStartRow + PollCreateActivity.this.answers.length) - 1);
                    }
                    RecyclerView.ViewHolder findViewHolderForAdapterPosition = PollCreateActivity.this.listView.findViewHolderForAdapterPosition(adapterPosition - 1);
                    EditTextBoldCursor textView = pollEditTextCell.getTextView();
                    if (findViewHolderForAdapterPosition != null) {
                        View view2 = findViewHolderForAdapterPosition.itemView;
                        if (view2 instanceof PollEditTextCell) {
                            ((PollEditTextCell) view2).getTextView().requestFocus();
                            textView.clearFocus();
                            PollCreateActivity.this.checkDoneButton();
                            PollCreateActivity.this.updateRows();
                            PollCreateActivity.this.listAdapter.notifyItemChanged(PollCreateActivity.this.answerSectionRow);
                        }
                    }
                    if (textView.isFocused()) {
                        AndroidUtilities.hideKeyboard(textView);
                    }
                    textView.clearFocus();
                    PollCreateActivity.this.checkDoneButton();
                    PollCreateActivity.this.updateRows();
                    PollCreateActivity.this.listAdapter.notifyItemChanged(PollCreateActivity.this.answerSectionRow);
                }
            }
        }

        public /* synthetic */ boolean lambda$onCreateViewHolder$1$PollCreateActivity$ListAdapter(PollEditTextCell pollEditTextCell, TextView textView, int i, KeyEvent keyEvent) {
            int adapterPosition;
            if (i != 5) {
                return false;
            }
            RecyclerView.ViewHolder findContainingViewHolder = PollCreateActivity.this.listView.findContainingViewHolder(pollEditTextCell);
            if (!(findContainingViewHolder == null || (adapterPosition = findContainingViewHolder.getAdapterPosition()) == -1)) {
                int access$2500 = adapterPosition - PollCreateActivity.this.answerStartRow;
                if (access$2500 == PollCreateActivity.this.answersCount - 1 && PollCreateActivity.this.answersCount < 10) {
                    PollCreateActivity.this.addNewField();
                } else if (access$2500 == PollCreateActivity.this.answersCount - 1) {
                    AndroidUtilities.hideKeyboard(pollEditTextCell.getTextView());
                } else {
                    RecyclerView.ViewHolder findViewHolderForAdapterPosition = PollCreateActivity.this.listView.findViewHolderForAdapterPosition(adapterPosition + 1);
                    if (findViewHolderForAdapterPosition != null) {
                        View view = findViewHolderForAdapterPosition.itemView;
                        if (view instanceof PollEditTextCell) {
                            ((PollEditTextCell) view).getTextView().requestFocus();
                        }
                    }
                }
            }
            return true;
        }

        static /* synthetic */ boolean lambda$onCreateViewHolder$2(PollEditTextCell pollEditTextCell, View view, int i, KeyEvent keyEvent) {
            EditTextBoldCursor editTextBoldCursor = (EditTextBoldCursor) view;
            if (i != 67 || keyEvent.getAction() != 0 || editTextBoldCursor.length() != 0) {
                return false;
            }
            pollEditTextCell.callOnDelete();
            return true;
        }

        public int getItemViewType(int i) {
            if (i == PollCreateActivity.this.questionHeaderRow || i == PollCreateActivity.this.answerHeaderRow || i == PollCreateActivity.this.settingsHeaderRow) {
                return 0;
            }
            if (i == PollCreateActivity.this.questionSectionRow) {
                return 1;
            }
            if (i == PollCreateActivity.this.answerSectionRow || i == PollCreateActivity.this.settingsSectionRow) {
                return 2;
            }
            if (i == PollCreateActivity.this.addAnswerRow) {
                return 3;
            }
            if (i == PollCreateActivity.this.questionRow) {
                return 4;
            }
            return (i == PollCreateActivity.this.anonymousRow || i == PollCreateActivity.this.multipleRow || i == PollCreateActivity.this.quizRow) ? 6 : 5;
        }

        public void swapElements(int i, int i2) {
            int access$2500 = i - PollCreateActivity.this.answerStartRow;
            int access$25002 = i2 - PollCreateActivity.this.answerStartRow;
            if (access$2500 >= 0 && access$25002 >= 0 && access$2500 < PollCreateActivity.this.answersCount && access$25002 < PollCreateActivity.this.answersCount) {
                String str = PollCreateActivity.this.answers[access$2500];
                PollCreateActivity.this.answers[access$2500] = PollCreateActivity.this.answers[access$25002];
                PollCreateActivity.this.answers[access$25002] = str;
                boolean z = PollCreateActivity.this.answersChecks[access$2500];
                PollCreateActivity.this.answersChecks[access$2500] = PollCreateActivity.this.answersChecks[access$25002];
                PollCreateActivity.this.answersChecks[access$25002] = z;
                notifyItemMoved(i, i2);
            }
        }
    }

    public ThemeDescription[] getThemeDescriptions() {
        return new ThemeDescription[]{new ThemeDescription(this.listView, ThemeDescription.FLAG_CELLBACKGROUNDCOLOR, new Class[]{HeaderCell.class, TextCell.class, PollEditTextCell.class, TextCheckCell.class}, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundWhite"), new ThemeDescription(this.fragmentView, ThemeDescription.FLAG_BACKGROUND, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundGray"), new ThemeDescription(this.actionBar, ThemeDescription.FLAG_BACKGROUND, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "actionBarDefault"), new ThemeDescription(this.listView, ThemeDescription.FLAG_LISTGLOWCOLOR, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "actionBarDefault"), new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_ITEMSCOLOR, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "actionBarDefaultIcon"), new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_TITLECOLOR, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "actionBarDefaultTitle"), new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_SELECTORCOLOR, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "actionBarDefaultSelector"), new ThemeDescription((View) this.listView, 0, new Class[]{HeaderCell.class}, new String[]{"textView"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundWhiteBlueHeader"), new ThemeDescription((View) this.listView, ThemeDescription.FLAG_CHECKTAG, new Class[]{HeaderCell.class}, new String[]{"textView2"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundWhiteRedText5"), new ThemeDescription((View) this.listView, ThemeDescription.FLAG_CHECKTAG, new Class[]{HeaderCell.class}, new String[]{"textView2"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundWhiteGrayText3"), new ThemeDescription((View) this.listView, ThemeDescription.FLAG_TEXTCOLOR, new Class[]{PollEditTextCell.class}, new String[]{"textView"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundWhiteBlackText"), new ThemeDescription((View) this.listView, ThemeDescription.FLAG_HINTTEXTCOLOR, new Class[]{PollEditTextCell.class}, new String[]{"textView"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundWhiteHintText"), new ThemeDescription((View) this.listView, ThemeDescription.FLAG_HINTTEXTCOLOR, new Class[]{PollEditTextCell.class}, new String[]{"deleteImageView"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundWhiteGrayIcon"), new ThemeDescription((View) this.listView, ThemeDescription.FLAG_HINTTEXTCOLOR, new Class[]{PollEditTextCell.class}, new String[]{"moveImageView"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundWhiteGrayIcon"), new ThemeDescription((View) this.listView, ThemeDescription.FLAG_USEBACKGROUNDDRAWABLE | ThemeDescription.FLAG_DRAWABLESELECTEDSTATE, new Class[]{PollEditTextCell.class}, new String[]{"deleteImageView"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "stickers_menuSelector"), new ThemeDescription((View) this.listView, ThemeDescription.FLAG_CHECKTAG, new Class[]{PollEditTextCell.class}, new String[]{"textView2"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundWhiteRedText5"), new ThemeDescription((View) this.listView, 0, new Class[]{PollEditTextCell.class}, new String[]{"checkBox"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundWhiteGrayIcon"), new ThemeDescription((View) this.listView, 0, new Class[]{PollEditTextCell.class}, new String[]{"checkBox"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "checkboxCheck"), new ThemeDescription((View) this.listView, 0, new Class[]{TextCheckCell.class}, new String[]{"textView"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundWhiteBlackText"), new ThemeDescription((View) this.listView, 0, new Class[]{TextCheckCell.class}, new String[]{"valueTextView"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundWhiteGrayText2"), new ThemeDescription((View) this.listView, 0, new Class[]{TextCheckCell.class}, new String[]{"checkBox"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "switchTrack"), new ThemeDescription((View) this.listView, 0, new Class[]{TextCheckCell.class}, new String[]{"checkBox"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "switchTrackChecked"), new ThemeDescription(this.listView, ThemeDescription.FLAG_SELECTOR, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "listSelectorSDK21"), new ThemeDescription(this.listView, 0, new Class[]{View.class}, Theme.dividerPaint, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "divider"), new ThemeDescription((View) this.listView, 0, new Class[]{TextCell.class}, new String[]{"textView"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundWhiteBlueText4"), new ThemeDescription((View) this.listView, ThemeDescription.FLAG_BACKGROUNDFILTER, new Class[]{TextCell.class}, new String[]{"imageView"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "switchTrackChecked"), new ThemeDescription((View) this.listView, 0, new Class[]{TextCell.class}, new String[]{"imageView"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "checkboxCheck")};
    }
}
