package com.tughi.aggregator.ui;

import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.tughi.aggregator.R;
import com.tughi.aggregator.content.EntryColumns;
import com.tughi.aggregator.content.FeedColumns;
import com.tughi.aggregator.content.Uris;

/**
 * A {@link ListFragment} for feed entries.
 * The displayed entries depend on the provided entries {@link Uri}.
 */
public class EntryListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * The entries {@link Uri}
     */
    public static final String ARG_ENTRIES_URI = "uri";

    private static final int LOADER_FEED = 1;
    private static final int LOADER_ENTRIES = 2;

    private Context applicationContext;

    private Uri entriesUri;
    private Uri feedUri;
    private long feedId;

    private EntryListAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        applicationContext = getActivity().getApplicationContext();

        entriesUri = getArguments().getParcelable(ARG_ENTRIES_URI);
        feedId = Long.parseLong(entriesUri.getPathSegments().get(1));
        feedUri = Uris.newFeedUri(feedId);

        setListAdapter(adapter = new EntryListAdapter(applicationContext));

        getLoaderManager().initLoader(LOADER_FEED, null, this);
        getLoaderManager().initLoader(LOADER_ENTRIES, null, this);

        if (feedId > 0) {
            setHasOptionsMenu(true);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.entry_list_fragment, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();

        adapter.updateSections();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.entry_list_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mode:
                startActivity(new Intent(applicationContext, FeedUpdateModeActivity.class).setData(feedUri));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onListItemClick(ListView view, View itemView, int position, long id) {
        // mark entry as read
        new AsyncTask<Object, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Object... params) {
                final Context context = (Context) params[0];
                final long id = (Long) params[1];

                ContentValues values = new ContentValues();
                values.put(EntryColumns.FLAG_READ, true);
                return context.getContentResolver().update(Uris.newUserEntryUri(id), values, null, null) == 1;
            }
        }.execute(applicationContext, id);
    }

    private static final String ENTRY_SELECTION = EntryColumns.RO_FLAG_READ + " = 0";
    private static final String ENTRY_ORDER = EntryColumns.UPDATED + " ASC";

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case LOADER_FEED:
                return new CursorLoader(applicationContext, feedUri, null, null, null, null);
            case LOADER_ENTRIES:
                return new CursorLoader(applicationContext, entriesUri, EntryListAdapter.ENTRY_PROJECTION, ENTRY_SELECTION, null, ENTRY_ORDER);
        }

        // never happens
        throw new IllegalStateException();
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        switch (loader.getId()) {
            case LOADER_FEED:
                // update the activity title
                if (cursor.moveToFirst()) {
                    String title;
                    switch (cursor.getInt(cursor.getColumnIndex(FeedColumns.ID))) {
                        case -2:
                            title = getString(R.string.starred_feed);
                            break;
                        case -1:
                            title = getString(R.string.unread_feed);
                            break;
                        default:
                            title = cursor.getString(cursor.getColumnIndex(FeedColumns.TITLE));
                    }
                    getActivity().setTitle(title);
                }
                break;
            case LOADER_ENTRIES:
                adapter.swapCursor(cursor);
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (loader.getId() == LOADER_ENTRIES) {
            // release the loader's cursor
            adapter.swapCursor(null);
        }
    }

}
