package org.groebl.sms.ui.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.MultiAutoCompleteTextView;
import com.android.ex.chips.BaseRecipientAdapter;
import com.android.ex.chips.RecipientEditTextView;
import org.groebl.sms.common.FontManager;
import org.groebl.sms.common.LiveViewManager;
import org.groebl.sms.common.TypefaceManager;
import org.groebl.sms.enums.QKPreference;
import org.groebl.sms.ui.ThemeManager;
import org.groebl.sms.ui.base.QKActivity;
import org.groebl.sms.ui.settings.SettingsFragment;

public class AutoCompleteContactView extends RecipientEditTextView {
    public static final String TAG = "AutoCompleteContactView";

    private QKActivity mContext;
    private BaseRecipientAdapter mAdapter;

    public AutoCompleteContactView(Context context) {
        this(context, null);
        init(context);
    }

    public AutoCompleteContactView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        mContext = (QKActivity) context;

        mAdapter = new BaseRecipientAdapter(BaseRecipientAdapter.QUERY_TYPE_PHONE, getContext());

        setThreshold(1);
        setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
        setAdapter(mAdapter);
        setOnItemClickListener(this);

        LiveViewManager.registerView(key -> {
            int fontFamily = FontManager.getFontFamily(mContext);
            int fontWeight = FontManager.getFontWeight(mContext, false);
            setTypeface(TypefaceManager.obtainTypeface(mContext, fontFamily, fontWeight));
        }, QKPreference.FONT_FAMILY, QKPreference.FONT_WEIGHT);

        LiveViewManager.registerView(QKPreference.FONT_SIZE, this, key -> {
            setTextSize(TypedValue.COMPLEX_UNIT_SP, FontManager.getTextSize(mContext, FontManager.TEXT_TYPE_PRIMARY));
        });

        LiveViewManager.registerView(QKPreference.BACKGROUND, this, key -> {
            setTextColor(ThemeManager.getTextOnBackgroundPrimary());
            setHintTextColor(ThemeManager.getTextOnBackgroundSecondary());
        });

        LiveViewManager.registerView(QKPreference.MOBILE_ONLY, this, key -> {
            if (mAdapter != null) {
                SharedPreferences prefs1 = mContext.getPrefs();
                mAdapter.setShowMobileOnly(prefs1.getBoolean(SettingsFragment.MOBILE_ONLY, false));
            }
        });
    }
}