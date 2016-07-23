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
        if (!isInEditMode()) {
            init(context);
        }
    }

    public AutoCompleteContactView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            init(context);
        }
    }

    private void init(Context context) {
        mContext = (QKActivity) context;

        mAdapter = new BaseRecipientAdapter(BaseRecipientAdapter.QUERY_TYPE_PHONE, getContext());

        setThreshold(1);
        setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
        setAdapter(mAdapter);
        setOnItemClickListener(this);

        LiveViewManager.registerView(QKPreference.FONT_FAMILY, this, key -> {
            setTypeface(FontManager.getFont(mContext));
        });

        LiveViewManager.registerView(QKPreference.FONT_WEIGHT, this, key -> {
            setTypeface(FontManager.getFont(mContext));
        });

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
