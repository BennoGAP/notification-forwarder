package org.groebl.sms.theme;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import org.groebl.sms.R;

import butterknife.Bind;
import butterknife.ButterKnife;

public class IconViewHolder extends RecyclerView.ViewHolder {

    @Bind(R.id.icon) protected ImageView mIcon;

    protected IconAdapter mIconAdapter;

    public IconViewHolder(IconAdapter iconAdapter, View view) {
        super(view);
        mIconAdapter = iconAdapter;
        ButterKnife.bind(this, view);
    }

    public void bind(int position) {
        int iconRes = mIconAdapter.getItem(position);
        mIcon.setImageResource(iconRes);
    }

}