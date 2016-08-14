package org.groebl.sms.ui.search;

import android.content.AsyncQueryHandler;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Telephony;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import org.groebl.sms.R;
import org.groebl.sms.common.utils.KeyboardUtils;
import org.groebl.sms.data.Contact;
import org.groebl.sms.ui.base.QKFragment;
import org.groebl.sms.ui.base.RecyclerCursorAdapter;
import org.groebl.sms.ui.messagelist.MessageListActivity;
import org.groebl.sms.ui.view.MessageListRecyclerView;
import org.groebl.sms.ui.view.QKEditText;
import org.groebl.sms.ui.view.QKTextView;

import java.util.HashMap;
import java.util.regex.Pattern;

public class SearchFragment extends QKFragment implements RecyclerCursorAdapter.ItemClickListener<SearchData> {
    public static final String TAG = "SearchFragment";

    private AsyncQueryHandler mQueryHandler;

    // Track which TextView's show which Contact objects so that we can update
    // appropriately when the Contact gets fully loaded.
    private HashMap<Contact, QKTextView> mContactMap = new HashMap<>();

    private SharedPreferences mPrefs;
    private Resources mRes;
    private Cursor mCursor;
    private QKEditText mQuery;
    private String mSearchString;
    private LinearLayoutManager mLayoutManager;
    private MessageListRecyclerView mRecyclerView;
    private SearchAdapter mAdapter;
    private Pattern mPattern;

    public SearchFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPrefs = mContext.getPrefs();
        mRes = mContext.getResources();

        // When the query completes cons up a new adapter and set our list adapter to that.
        mQueryHandler = new AsyncQueryHandler(mContext.getContentResolver()) {
            protected void onQueryComplete(int token, Object cookie, Cursor c) {

                mAdapter.changeCursor(c);
                mAdapter.setQuery(mSearchString);
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        mQuery = (QKEditText) view.findViewById(R.id.search_query);
        mQuery.setTextChangedListener(s -> {
            mSearchString = s.toString();
            query();
        });
        mQuery.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                mSearchString = mQuery.getText().toString();
                query();

                // Hide the keyboard when the user makes a query
                mQuery.clearFocus();
                KeyboardUtils.hide(mContext, mQuery);
                return true;
            }
            return false;
        });

        mLayoutManager = new LinearLayoutManager(mContext);
        mAdapter = new SearchAdapter(mContext);
        mAdapter.setItemClickListener(this);


        mRecyclerView = (MessageListRecyclerView) view.findViewById(R.id.search_list);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        new Handler().postDelayed(() -> KeyboardUtils.showAndFocus(mContext, mQuery), 50);
        return view;
    }

    private void query() {
        Contact.addListener(mContactListener);

        // don't pass a projection since the search uri ignores it
        Uri uri = Telephony.MmsSms.SEARCH_URI.buildUpon().appendQueryParameter("pattern", mSearchString).build();

        // kick off a query for the threads which match the search string
        mQueryHandler.startQuery(0, null, uri, null, null, null, null);
    }


    Contact.UpdateListener mContactListener = updated -> {
        QKTextView tv = mContactMap.get(updated);
        if (tv != null) {
            tv.setText(updated.getNameAndNumber());
        }
    };

    @Override
    public void onStop() {
        super.onStop();
        Contact.removeListener(mContactListener);
    }

    @Override
    public void onItemClick(SearchData data, View view) {
        MessageListActivity.launch(mContext, data.threadId, data.rowId, mSearchString, true);
        mContext.finish();
    }

    @Override
    public void onItemLongClick(SearchData data, View view) {

    }
}
