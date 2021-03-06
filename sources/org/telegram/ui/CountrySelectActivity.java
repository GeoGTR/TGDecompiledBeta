package org.telegram.ui;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.Utilities;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.Cells.DividerCell;
import org.telegram.ui.Cells.LetterSectionCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.Components.EmptyTextProgressView;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.CountrySelectActivity;

public class CountrySelectActivity extends BaseFragment {
    private CountrySelectActivityDelegate delegate;
    private EmptyTextProgressView emptyView;
    /* access modifiers changed from: private */
    public RecyclerListView listView;
    /* access modifiers changed from: private */
    public CountryAdapter listViewAdapter;
    /* access modifiers changed from: private */
    public boolean needPhoneCode;
    /* access modifiers changed from: private */
    public CountrySearchAdapter searchListViewAdapter;
    /* access modifiers changed from: private */
    public boolean searchWas;
    /* access modifiers changed from: private */
    public boolean searching;

    public static class Country {
        public String code;
        public String name;
        public String shortname;
    }

    public interface CountrySelectActivityDelegate {
        void didSelectCountry(String str, String str2);
    }

    public CountrySelectActivity(boolean z) {
        this.needPhoneCode = z;
    }

    public boolean onFragmentCreate() {
        return super.onFragmentCreate();
    }

    public void onFragmentDestroy() {
        super.onFragmentDestroy();
    }

    public View createView(Context context) {
        this.actionBar.setBackButtonImage(NUM);
        int i = 1;
        this.actionBar.setAllowOverlayTitle(true);
        this.actionBar.setTitle(LocaleController.getString("ChooseCountry", NUM));
        this.actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            public void onItemClick(int i) {
                if (i == -1) {
                    CountrySelectActivity.this.finishFragment();
                }
            }
        });
        ActionBarMenuItem addItem = this.actionBar.createMenu().addItem(0, NUM);
        addItem.setIsSearchField(true);
        addItem.setActionBarMenuItemSearchListener(new ActionBarMenuItem.ActionBarMenuItemSearchListener() {
            public void onSearchExpand() {
                boolean unused = CountrySelectActivity.this.searching = true;
            }

            public void onSearchCollapse() {
                CountrySelectActivity.this.searchListViewAdapter.search((String) null);
                boolean unused = CountrySelectActivity.this.searching = false;
                boolean unused2 = CountrySelectActivity.this.searchWas = false;
                CountrySelectActivity.this.listView.setAdapter(CountrySelectActivity.this.listViewAdapter);
                CountrySelectActivity.this.listView.setFastScrollVisible(true);
            }

            public void onTextChanged(EditText editText) {
                String obj = editText.getText().toString();
                CountrySelectActivity.this.searchListViewAdapter.search(obj);
                if (obj.length() != 0) {
                    boolean unused = CountrySelectActivity.this.searchWas = true;
                }
            }
        });
        addItem.setSearchFieldHint(LocaleController.getString("Search", NUM));
        this.searching = false;
        this.searchWas = false;
        CountryAdapter countryAdapter = new CountryAdapter(context);
        this.listViewAdapter = countryAdapter;
        this.searchListViewAdapter = new CountrySearchAdapter(context, countryAdapter.getCountries());
        FrameLayout frameLayout = new FrameLayout(context);
        this.fragmentView = frameLayout;
        FrameLayout frameLayout2 = frameLayout;
        EmptyTextProgressView emptyTextProgressView = new EmptyTextProgressView(context);
        this.emptyView = emptyTextProgressView;
        emptyTextProgressView.showTextView();
        this.emptyView.setShowAtCenter(true);
        this.emptyView.setText(LocaleController.getString("NoResult", NUM));
        frameLayout2.addView(this.emptyView, LayoutHelper.createFrame(-1, -1.0f));
        RecyclerListView recyclerListView = new RecyclerListView(context);
        this.listView = recyclerListView;
        recyclerListView.setSectionsType(1);
        this.listView.setEmptyView(this.emptyView);
        this.listView.setVerticalScrollBarEnabled(false);
        this.listView.setFastScrollEnabled();
        this.listView.setLayoutManager(new LinearLayoutManager(context, 1, false));
        this.listView.setAdapter(this.listViewAdapter);
        RecyclerListView recyclerListView2 = this.listView;
        if (!LocaleController.isRTL) {
            i = 2;
        }
        recyclerListView2.setVerticalScrollbarPosition(i);
        frameLayout2.addView(this.listView, LayoutHelper.createFrame(-1, -1.0f));
        this.listView.setOnItemClickListener((RecyclerListView.OnItemClickListener) new RecyclerListView.OnItemClickListener() {
            public final void onItemClick(View view, int i) {
                CountrySelectActivity.this.lambda$createView$0$CountrySelectActivity(view, i);
            }
        });
        this.listView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            public void onScrollStateChanged(RecyclerView recyclerView, int i) {
                if (i == 1) {
                    AndroidUtilities.hideKeyboard(CountrySelectActivity.this.getParentActivity().getCurrentFocus());
                }
            }
        });
        return this.fragmentView;
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$createView$0 */
    public /* synthetic */ void lambda$createView$0$CountrySelectActivity(View view, int i) {
        Country country;
        CountrySelectActivityDelegate countrySelectActivityDelegate;
        if (!this.searching || !this.searchWas) {
            int sectionForPosition = this.listViewAdapter.getSectionForPosition(i);
            int positionInSectionForPosition = this.listViewAdapter.getPositionInSectionForPosition(i);
            if (positionInSectionForPosition >= 0 && sectionForPosition >= 0) {
                country = this.listViewAdapter.getItem(sectionForPosition, positionInSectionForPosition);
            } else {
                return;
            }
        } else {
            country = this.searchListViewAdapter.getItem(i);
        }
        if (i >= 0) {
            finishFragment();
            if (country != null && (countrySelectActivityDelegate = this.delegate) != null) {
                countrySelectActivityDelegate.didSelectCountry(country.name, country.shortname);
            }
        }
    }

    public void onResume() {
        super.onResume();
        CountryAdapter countryAdapter = this.listViewAdapter;
        if (countryAdapter != null) {
            countryAdapter.notifyDataSetChanged();
        }
    }

    public void setCountrySelectActivityDelegate(CountrySelectActivityDelegate countrySelectActivityDelegate) {
        this.delegate = countrySelectActivityDelegate;
    }

    public class CountryAdapter extends RecyclerListView.SectionsAdapter {
        private HashMap<String, ArrayList<Country>> countries = new HashMap<>();
        private Context mContext;
        private ArrayList<String> sortedCountries = new ArrayList<>();

        public CountryAdapter(Context context) {
            this.mContext = context;
            try {
                InputStream open = ApplicationLoader.applicationContext.getResources().getAssets().open("countries.txt");
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(open));
                while (true) {
                    String readLine = bufferedReader.readLine();
                    if (readLine == null) {
                        break;
                    }
                    String[] split = readLine.split(";");
                    Country country = new Country();
                    String str = split[2];
                    country.name = str;
                    country.code = split[0];
                    country.shortname = split[1];
                    String upperCase = str.substring(0, 1).toUpperCase();
                    ArrayList arrayList = this.countries.get(upperCase);
                    if (arrayList == null) {
                        arrayList = new ArrayList();
                        this.countries.put(upperCase, arrayList);
                        this.sortedCountries.add(upperCase);
                    }
                    arrayList.add(country);
                }
                bufferedReader.close();
                open.close();
            } catch (Exception e) {
                FileLog.e((Throwable) e);
            }
            Collections.sort(this.sortedCountries, $$Lambda$Ds7dtVnGrflEw4LvNOxA0cDT4Y.INSTANCE);
            for (ArrayList<Country> sort : this.countries.values()) {
                Collections.sort(sort, $$Lambda$CountrySelectActivity$CountryAdapter$dKjgdmEaEdtVLx1pfz_Hpxo_gOU.INSTANCE);
            }
        }

        public HashMap<String, ArrayList<Country>> getCountries() {
            return this.countries;
        }

        public Country getItem(int i, int i2) {
            if (i >= 0 && i < this.sortedCountries.size()) {
                ArrayList arrayList = this.countries.get(this.sortedCountries.get(i));
                if (i2 >= 0 && i2 < arrayList.size()) {
                    return (Country) arrayList.get(i2);
                }
            }
            return null;
        }

        public boolean isEnabled(int i, int i2) {
            return i2 < this.countries.get(this.sortedCountries.get(i)).size();
        }

        public int getSectionCount() {
            return this.sortedCountries.size();
        }

        public int getCountForSection(int i) {
            int size = this.countries.get(this.sortedCountries.get(i)).size();
            return i != this.sortedCountries.size() + -1 ? size + 1 : size;
        }

        /* JADX WARNING: Failed to insert additional move for type inference */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public android.view.View getSectionHeaderView(int r3, android.view.View r4) {
            /*
                r2 = this;
                if (r4 != 0) goto L_0x0012
                org.telegram.ui.Cells.LetterSectionCell r4 = new org.telegram.ui.Cells.LetterSectionCell
                android.content.Context r0 = r2.mContext
                r4.<init>(r0)
                r0 = 1111490560(0x42400000, float:48.0)
                int r0 = org.telegram.messenger.AndroidUtilities.dp(r0)
                r4.setCellHeight(r0)
            L_0x0012:
                r0 = r4
                org.telegram.ui.Cells.LetterSectionCell r0 = (org.telegram.ui.Cells.LetterSectionCell) r0
                java.util.ArrayList<java.lang.String> r1 = r2.sortedCountries
                java.lang.Object r3 = r1.get(r3)
                java.lang.String r3 = (java.lang.String) r3
                java.lang.String r3 = r3.toUpperCase()
                r0.setLetter(r3)
                return r4
            */
            throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.CountrySelectActivity.CountryAdapter.getSectionHeaderView(int, android.view.View):android.view.View");
        }

        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View view;
            if (i != 0) {
                view = new DividerCell(this.mContext);
                float f = 24.0f;
                int dp = AndroidUtilities.dp(LocaleController.isRTL ? 24.0f : 72.0f);
                int dp2 = AndroidUtilities.dp(8.0f);
                if (LocaleController.isRTL) {
                    f = 72.0f;
                }
                view.setPadding(dp, dp2, AndroidUtilities.dp(f), AndroidUtilities.dp(8.0f));
            } else {
                view = new TextSettingsCell(this.mContext);
                float f2 = 16.0f;
                int dp3 = AndroidUtilities.dp(LocaleController.isRTL ? 16.0f : 54.0f);
                if (LocaleController.isRTL) {
                    f2 = 54.0f;
                }
                view.setPadding(dp3, 0, AndroidUtilities.dp(f2), 0);
            }
            return new RecyclerListView.Holder(view);
        }

        public void onBindViewHolder(int i, int i2, RecyclerView.ViewHolder viewHolder) {
            String str;
            if (viewHolder.getItemViewType() == 0) {
                Country country = (Country) this.countries.get(this.sortedCountries.get(i)).get(i2);
                TextSettingsCell textSettingsCell = (TextSettingsCell) viewHolder.itemView;
                String str2 = country.name;
                if (CountrySelectActivity.this.needPhoneCode) {
                    str = "+" + country.code;
                } else {
                    str = null;
                }
                textSettingsCell.setTextAndValue(str2, str, false);
            }
        }

        public int getItemViewType(int i, int i2) {
            return i2 < this.countries.get(this.sortedCountries.get(i)).size() ? 0 : 1;
        }

        public String getLetter(int i) {
            int sectionForPosition = getSectionForPosition(i);
            if (sectionForPosition == -1) {
                sectionForPosition = this.sortedCountries.size() - 1;
            }
            return this.sortedCountries.get(sectionForPosition);
        }

        public int getPositionForScrollProgress(float f) {
            return (int) (((float) getItemCount()) * f);
        }
    }

    public class CountrySearchAdapter extends RecyclerListView.SelectionAdapter {
        private HashMap<String, ArrayList<Country>> countries;
        private Context mContext;
        private ArrayList<Country> searchResult;
        /* access modifiers changed from: private */
        public Timer searchTimer;

        public int getItemViewType(int i) {
            return 0;
        }

        public boolean isEnabled(RecyclerView.ViewHolder viewHolder) {
            return true;
        }

        public CountrySearchAdapter(Context context, HashMap<String, ArrayList<Country>> hashMap) {
            this.mContext = context;
            this.countries = hashMap;
        }

        public void search(final String str) {
            if (str == null) {
                this.searchResult = null;
                return;
            }
            try {
                Timer timer = this.searchTimer;
                if (timer != null) {
                    timer.cancel();
                }
            } catch (Exception e) {
                FileLog.e((Throwable) e);
            }
            Timer timer2 = new Timer();
            this.searchTimer = timer2;
            timer2.schedule(new TimerTask() {
                public void run() {
                    try {
                        CountrySearchAdapter.this.searchTimer.cancel();
                        Timer unused = CountrySearchAdapter.this.searchTimer = null;
                    } catch (Exception e) {
                        FileLog.e((Throwable) e);
                    }
                    CountrySearchAdapter.this.processSearch(str);
                }
            }, 100, 300);
        }

        /* access modifiers changed from: private */
        public void processSearch(String str) {
            Utilities.searchQueue.postRunnable(new Runnable(str) {
                public final /* synthetic */ String f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    CountrySelectActivity.CountrySearchAdapter.this.lambda$processSearch$0$CountrySelectActivity$CountrySearchAdapter(this.f$1);
                }
            });
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$processSearch$0 */
        public /* synthetic */ void lambda$processSearch$0$CountrySelectActivity$CountrySearchAdapter(String str) {
            if (str.trim().toLowerCase().length() == 0) {
                updateSearchResults(new ArrayList());
                return;
            }
            ArrayList arrayList = new ArrayList();
            ArrayList arrayList2 = this.countries.get(str.substring(0, 1).toUpperCase());
            if (arrayList2 != null) {
                Iterator it = arrayList2.iterator();
                while (it.hasNext()) {
                    Country country = (Country) it.next();
                    if (country.name.toLowerCase().startsWith(str)) {
                        arrayList.add(country);
                    }
                }
            }
            updateSearchResults(arrayList);
        }

        private void updateSearchResults(ArrayList<Country> arrayList) {
            AndroidUtilities.runOnUIThread(new Runnable(arrayList) {
                public final /* synthetic */ ArrayList f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    CountrySelectActivity.CountrySearchAdapter.this.lambda$updateSearchResults$1$CountrySelectActivity$CountrySearchAdapter(this.f$1);
                }
            });
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$updateSearchResults$1 */
        public /* synthetic */ void lambda$updateSearchResults$1$CountrySelectActivity$CountrySearchAdapter(ArrayList arrayList) {
            if (CountrySelectActivity.this.searching) {
                this.searchResult = arrayList;
                if (!(!CountrySelectActivity.this.searchWas || CountrySelectActivity.this.listView == null || CountrySelectActivity.this.listView.getAdapter() == CountrySelectActivity.this.searchListViewAdapter)) {
                    CountrySelectActivity.this.listView.setAdapter(CountrySelectActivity.this.searchListViewAdapter);
                    CountrySelectActivity.this.listView.setFastScrollVisible(false);
                }
                notifyDataSetChanged();
            }
        }

        public int getItemCount() {
            ArrayList<Country> arrayList = this.searchResult;
            if (arrayList == null) {
                return 0;
            }
            return arrayList.size();
        }

        public Country getItem(int i) {
            if (i < 0 || i >= this.searchResult.size()) {
                return null;
            }
            return this.searchResult.get(i);
        }

        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            return new RecyclerListView.Holder(new TextSettingsCell(this.mContext));
        }

        public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {
            String str;
            Country country = this.searchResult.get(i);
            TextSettingsCell textSettingsCell = (TextSettingsCell) viewHolder.itemView;
            String str2 = country.name;
            if (CountrySelectActivity.this.needPhoneCode) {
                str = "+" + country.code;
            } else {
                str = null;
            }
            boolean z = true;
            if (i == this.searchResult.size() - 1) {
                z = false;
            }
            textSettingsCell.setTextAndValue(str2, str, z);
        }
    }

    public ArrayList<ThemeDescription> getThemeDescriptions() {
        ArrayList<ThemeDescription> arrayList = new ArrayList<>();
        arrayList.add(new ThemeDescription(this.fragmentView, ThemeDescription.FLAG_BACKGROUND, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundWhite"));
        arrayList.add(new ThemeDescription(this.actionBar, ThemeDescription.FLAG_BACKGROUND, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "actionBarDefault"));
        arrayList.add(new ThemeDescription(this.listView, ThemeDescription.FLAG_LISTGLOWCOLOR, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "actionBarDefault"));
        arrayList.add(new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_ITEMSCOLOR, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "actionBarDefaultIcon"));
        arrayList.add(new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_TITLECOLOR, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "actionBarDefaultTitle"));
        arrayList.add(new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_SELECTORCOLOR, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "actionBarDefaultSelector"));
        arrayList.add(new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_SEARCH, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "actionBarDefaultSearch"));
        arrayList.add(new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_SEARCHPLACEHOLDER, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "actionBarDefaultSearchPlaceholder"));
        arrayList.add(new ThemeDescription(this.listView, ThemeDescription.FLAG_SELECTOR, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "listSelectorSDK21"));
        arrayList.add(new ThemeDescription(this.listView, 0, new Class[]{View.class}, Theme.dividerPaint, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "divider"));
        arrayList.add(new ThemeDescription(this.listView, ThemeDescription.FLAG_FASTSCROLL, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "fastScrollActive"));
        arrayList.add(new ThemeDescription(this.listView, ThemeDescription.FLAG_FASTSCROLL, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "fastScrollInactive"));
        arrayList.add(new ThemeDescription(this.listView, ThemeDescription.FLAG_FASTSCROLL, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "fastScrollText"));
        arrayList.add(new ThemeDescription(this.emptyView, ThemeDescription.FLAG_TEXTCOLOR, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "emptyListPlaceholder"));
        arrayList.add(new ThemeDescription((View) this.listView, 0, new Class[]{TextSettingsCell.class}, new String[]{"textView"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundWhiteBlackText"));
        arrayList.add(new ThemeDescription((View) this.listView, 0, new Class[]{TextSettingsCell.class}, new String[]{"valueTextView"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundWhiteValueText"));
        arrayList.add(new ThemeDescription((View) this.listView, ThemeDescription.FLAG_SECTIONS, new Class[]{LetterSectionCell.class}, new String[]{"textView"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundWhiteGrayText4"));
        return arrayList;
    }
}
