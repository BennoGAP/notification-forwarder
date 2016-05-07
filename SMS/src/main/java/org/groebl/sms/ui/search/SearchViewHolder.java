package org.groebl.sms.ui.search;

import android.view.View;
import org.groebl.sms.R;
import org.groebl.sms.ui.base.ClickyViewHolder;
import org.groebl.sms.ui.base.QKActivity;
import org.groebl.sms.ui.view.AvatarView;
import org.groebl.sms.ui.view.QKTextView;

public class SearchViewHolder extends ClickyViewHolder<SearchData> {

    protected View root;
    protected AvatarView avatar;
    protected QKTextView name;
    protected QKTextView date;
    protected QKTextView snippet;

    public SearchViewHolder(QKActivity context, View view) {
        super(context, view);

        root = view;
        avatar = (AvatarView) view.findViewById(R.id.search_avatar);
        name = (QKTextView) view.findViewById(R.id.search_name);
        date = (QKTextView) view.findViewById(R.id.search_date);
        snippet = (QKTextView) view.findViewById(R.id.search_snippet);
    }
}
