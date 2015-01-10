package nl.marcnolte.coffeecounter;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.util.SparseArray;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import nl.marcnolte.coffeecounter.contentprovider.MyContentProvider;
import nl.marcnolte.coffeecounter.database.DatabaseContract;
import nl.marcnolte.coffeecounter.libraries.TimeHelper;

public class EntryListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>
{
    /**
     * Debug Tag
     */
    private final String DEBUG_TAG = getClass().getSimpleName();

    /**
     * Fragment parameters
     */
    private EntryListAdapter   listAdapter;
    private ExpandableListView listView;

    /**
     * Loaders
     */
    private static final int URL_LOADER_GROUPS = -1;

    /**
     * Create a new instance of this fragment.
     *
     * @return New fragment instance.
     */
    public static EntryListFragment newInstance()
    {
        return new EntryListFragment();
    }

    public EntryListFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Initialize fragment view
        View rootView = inflater.inflate(R.layout.fragment_list, container, false);

        // Initialize list view
        listView = (ExpandableListView) rootView.findViewById(R.id.expandableList);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        // Initialize list adapter
        listAdapter = new EntryListAdapter(this, getActivity());
        listView.setAdapter(listAdapter);

        // Initialize loader
        initializeLoader(URL_LOADER_GROUPS);
    }

    @Override
    public void onResume()
    {
        super.onResume();

        // @TODO Reset loaders when user changes time or timezone while running the app

        // Register context menu
        registerForContextMenu(listView);
    }

    @Override
    public void onPause()
    {
        super.onPause();

        // Unregister context menu
        unregisterForContextMenu(listView);
    }

    /**
     * Instantiate and return a new Loader for the given ID.
     *
     * @param loaderID  The ID whose loader is to be created.
     * @param args      Any arguments supplied by the caller.
     *
     * @return Return a new Loader instance that is ready to start loading.
     */
    @Override
    public Loader<Cursor> onCreateLoader(int loaderID, Bundle args)
    {
        // Check loaderID is not the group list
        if (loaderID != URL_LOADER_GROUPS)
        {
            // Start loading group list items data
            // Get group map
            SparseArray<String> groupMap = listAdapter.getGroupMap();
            // Build uri
            Uri.Builder uri = new Uri.Builder();
            uri.scheme("content");
            uri.authority(MyContentProvider.AUTHORITY);
            uri.path(MyContentProvider.BASE_PATH + "/" + DatabaseContract.Entries.TABLE_NAME);
            // Prep query
            String projection[]    = {
                DatabaseContract.Entries.COLUMN_NAME_ID + " AS _id",
                "DATE("     + DatabaseContract.Entries.COLUMN_NAME_DATETIME + ", '+" + TimeHelper.getOffset() + " SECONDS') AS date",
                "TIME("     + DatabaseContract.Entries.COLUMN_NAME_DATETIME + ", '+" + TimeHelper.getOffset() + " SECONDS') AS time",
                "DATETIME(" + DatabaseContract.Entries.COLUMN_NAME_CREATED  + ", '+" + TimeHelper.getOffset() + " SECONDS') AS created"
            };
            String selection       = "DATE(" + DatabaseContract.Entries.COLUMN_NAME_DATETIME + ", '+" + TimeHelper.getOffset() + " SECONDS') = ?";
            String selectionArgs[] = { groupMap.get(loaderID) };
            String sortOrder       = DatabaseContract.Entries.COLUMN_NAME_DATETIME + " DESC";
            // Return a new CursorLoader
            return new CursorLoader(
                getActivity(),   // Parent activity context
                uri.build(),     // Table to query
                projection,      // Projection to return
                selection,       // Selection to return
                selectionArgs,   // Arguments on selection
                sortOrder        // Default sort order
            );
        }
        else
        {
            // Start loading group list data
            // Build uri
            Uri.Builder uri = new Uri.Builder();
            uri.scheme("content");
            uri.authority(MyContentProvider.AUTHORITY);
            uri.path(MyContentProvider.BASE_PATH + "/" + DatabaseContract.Entries.TABLE_NAME + "/groups");
            // Prep query
            String projection[] = {
                "ROWID AS _id",
                "DATE("  + DatabaseContract.Entries.COLUMN_NAME_DATETIME + ", '+" + TimeHelper.getOffset() + " SECONDS') AS date",
                "COUNT(" + DatabaseContract.Entries.COLUMN_NAME_DATETIME + ") AS amount"
            };
            String sortOrder    = DatabaseContract.Entries.COLUMN_NAME_DATETIME + " DESC";
            // Return a new CursorLoader
            return new CursorLoader(
                getActivity(),   // Parent activity context
                uri.build(),     // Table to query
                projection,      // Projection to return
                null,            // No selection clause
                null,            // No selection arguments
                sortOrder        // Default sort order
            );
        }
    }

    /**
     * Called when a previously created loader has finished its load.
     *
     * @param loader The Loader that has finished.
     * @param data   The data generated by the Loader.
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data)
    {
        // Get loader ID
        int loaderID = loader.getId();

        // Check loaderID is not the group list
        if (loaderID != URL_LOADER_GROUPS)
        {
            // Pass group list items data to adapter
            listAdapter.setChildrenCursor(loaderID, data);
        }
        else
        {
            // Pass group list data to adapter
            listAdapter.setGroupCursor(data);
        }
    }

    /**
     * Called when a previously created loader is being reset, and thus
     * making its data unavailable. The application should at this point
     * remove any references it has to the Loader's data.
     *
     * @param loader The Loader that is being reset.
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader)
    {
        // Get loader ID
        int loaderID = loader.getId();

        // Check loaderID is not the group list
        if (loaderID != URL_LOADER_GROUPS)
        {
            // Load the group children cursor
            listAdapter.setChildrenCursor(loaderID, null);
        }
        else
        {
            // Clear the group list cursor reference from the adapter
            listAdapter.setGroupCursor(null);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)
    {
        super.onCreateContextMenu(menu, v, menuInfo);

        ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo) menuInfo;

        int type  = ExpandableListView.getPackedPositionType(info.packedPosition);
        int group = ExpandableListView.getPackedPositionGroup(info.packedPosition);
        int child = ExpandableListView.getPackedPositionChild(info.packedPosition);

        // Only create a context menu for child items
        if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD)
        {
            menu.setHeaderTitle("Entry");
            menu.add(group, child, 0, "Delete");
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item)
    {
        if (item.getTitle() == "Delete")
        {
            entryDeleteDialog(item.getGroupId(), item.getItemId());
        }
        else
        {
            return false;
        }

        return true;
    }

    /**
     * Initialize loader
     *
     * @param loaderID  The ID whose loader is to be initialized.
     */
    public void initializeLoader(int loaderID)
    {
        Loader loader = getLoaderManager().getLoader(loaderID);
        if (loader != null && !loader.isReset())
        {
            getLoaderManager().restartLoader(loaderID, null, this);
        }
        else
        {
            getLoaderManager().initLoader(loaderID, null, this);
        }
    }

    /**
     * Show delete entry dialog
     */
    public void entryDeleteDialog(int groupPosition, int childPosition)
    {
        // Get row
        Cursor cursor = listAdapter.getChild(groupPosition, childPosition);
        int entryID = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.Entries.COLUMN_NAME_ID));

        Log.d(DEBUG_TAG, "Function entryDelete called on item: #" + entryID + " (groupPosition: " + groupPosition + ", childPosition: " + childPosition + ")");

        // Create and show an instance of the dialog fragment
        ConfirmDeleteDialog dialogFragment = new ConfirmDeleteDialog().newInstance(entryID);
        dialogFragment.show(getFragmentManager(), "EntryConfirmDeleteDialogFragment");
    }
}