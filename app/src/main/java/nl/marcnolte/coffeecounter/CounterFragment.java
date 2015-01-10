package nl.marcnolte.coffeecounter;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import nl.marcnolte.coffeecounter.contentprovider.MyContentProvider;
import nl.marcnolte.coffeecounter.database.DatabaseContract;
import nl.marcnolte.coffeecounter.libraries.NumberHelper;
import nl.marcnolte.coffeecounter.libraries.TimeHelper;

public class CounterFragment extends Fragment implements
    View.OnClickListener,
    LoaderManager.LoaderCallbacks<Cursor>
{
    /**
     * Debug Tag
     */
    private final String DEBUG_TAG = getClass().getSimpleName();

    /**
     * Loaders
     */
    private static final int URL_LOADER_TODAY = 0;
    private static final int URL_LOADER_STATS = 1;

    /**
     * Broadcast receivers
     */
    private final TimeChangeReceiver mTimeChangeReceiver = new TimeChangeReceiver();

    /**
     * Layout elements
     */
    private TextView tvCounterAmount;
    private TextView tvMinAmount;
    private TextView tvMaxAmount;
    private TextView tvAverageAmount;

    /**
     * Timekeeping
     */
    String today = null;

    /**
     * Create a new instance of this fragment.
     *
     * @return A new instance of fragment MyListFragment.
     */
    public static CounterFragment newInstance()
    {
        return new CounterFragment();
    }

    public CounterFragment()
    {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Initialize view
        View rootView = inflater.inflate(R.layout.fragment_counter, container, false);

        // Initialize fields
        tvCounterAmount = (TextView) rootView.findViewById(R.id.fragment_counter_amount);
        tvMinAmount = (TextView) rootView.findViewById(R.id.fragment_counter_details_min_amount);
        tvMaxAmount = (TextView) rootView.findViewById(R.id.fragment_counter_details_max_amount);
        tvAverageAmount = (TextView) rootView.findViewById(R.id.fragment_counter_details_average_amount);

        // Initialize buttons
        Button btnCounterIncrement = (Button) rootView.findViewById(R.id.fragment_counter_button_count);
        btnCounterIncrement.setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        // Initialize loaders
        initializeLoader(URL_LOADER_TODAY);
        initializeLoader(URL_LOADER_STATS);
    }

    @Override
    public void onResume()
    {
        super.onResume();

        Log.d(DEBUG_TAG, "onResume called");

        // Make sure current day is visible
        if (today == null || !TimeHelper.isToday(today))
        {
            initializeLoader(URL_LOADER_TODAY);
        }

        // Create intent filter
        IntentFilter mIntendFilter = new IntentFilter();
        mIntendFilter.addAction(Intent.ACTION_TIME_TICK);
        // @TODO Reset loaders when user changes time or timezone while running the app
        //mIntendFilter.addAction(Intent.ACTION_TIME_CHANGED);
        //mIntendFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);

        // Register broadcast receivers
        getActivity().registerReceiver(mTimeChangeReceiver, mIntendFilter);
    }

    @Override
    public void onPause()
    {
        super.onPause();

        Log.d(DEBUG_TAG, "onPause called");

        // Unregister broadcast receivers
        getActivity().unregisterReceiver(mTimeChangeReceiver);
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.fragment_counter_button_count:
                new IncrementCounterTask(){
                    @Override
                    protected void onPostExecute(Void result)
                    {
                        // Show toast feedback
                        Toast.makeText(getActivity(), R.string.toast_counter_incremented, Toast.LENGTH_SHORT).show();
                    }
                }.execute();
                break;
        }
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
        // Prepare uri
        Uri.Builder uriBuilder = new Uri.Builder();
        uriBuilder.scheme("content");
        uriBuilder.authority(MyContentProvider.AUTHORITY);
        uriBuilder.path(MyContentProvider.BASE_PATH + "/" + DatabaseContract.Entries.TABLE_NAME);

        // Handle loader depending on loaderID
        switch(loaderID)
        {
            case URL_LOADER_TODAY:
            {
                // Update timekeeper
                today = TimeHelper.getDate("local");
                // Prep query
                Uri    uri             = uriBuilder.build();
                String projection[]    = { "COUNT(*) AS amount" };
                String selection       = "DATETIME(" + DatabaseContract.Entries.COLUMN_NAME_DATETIME + ", '+" + TimeHelper.getOffset() + " SECONDS') BETWEEN ? AND ?";
                String selectionArgs[] = { TimeHelper.getDatetime("sod", "local"), TimeHelper.getDatetime("eod", "local") };
                // Return a new CursorLoader
                return new CursorLoader(getActivity(), uri, projection, selection, selectionArgs, null);
            }

            case URL_LOADER_STATS:
            {
                // Prep query
                Uri    uri             = uriBuilder.appendPath("avg").build();
                String projection[]    = { "MIN(amount) AS minimum", "MAX(amount) AS maximum", "AVG(amount) AS average" };
                // Return a new CursorLoader
                return new CursorLoader(getActivity(), uri, projection, null, null, null);
            }

            default:
                return null;
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

        // Handle loader depending on loaderID
        switch(loaderID)
        {
            case URL_LOADER_TODAY:
                // Set cursor to first row
                if (data.moveToFirst())
                {
                    // Update text views
                    String amount = data.getString(data.getColumnIndexOrThrow("amount"));
                    tvCounterAmount.setText(amount);
                }
                break;

            case URL_LOADER_STATS:
                // Set cursor to first row
                if (data.moveToFirst())
                {
                    // Update text views
                    tvMinAmount.setText(data.getString(data.getColumnIndexOrThrow("minimum")));
                    tvMaxAmount.setText(data.getString(data.getColumnIndexOrThrow("maximum")));
                    tvAverageAmount.setText(NumberHelper.getDecimal(data.getDouble(data.getColumnIndexOrThrow("average")), 0, 1));
                }
                break;
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
        // Cursor is already being closed by loader, so just remove references to cursor data here
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

    private class IncrementCounterTask extends AsyncTask<Void, Void, Void>
    {
        private final String DEBUG_TAG = getClass().getSimpleName();

        @Override
        protected Void doInBackground(Void... params)
        {
            Uri.Builder uriBuilder = new Uri.Builder();
            uriBuilder.scheme("content");
            uriBuilder.authority(MyContentProvider.AUTHORITY);
            uriBuilder.path(MyContentProvider.BASE_PATH + "/" + DatabaseContract.Entries.TABLE_NAME);

            ContentValues values = new ContentValues();
            values.clear();
            values.put(DatabaseContract.Entries.COLUMN_NAME_DATETIME, TimeHelper.getDatetime("now", "UTC"));

            getActivity().getContentResolver().insert(uriBuilder.build(), values);

            return null;
        }
    }

    public class TimeChangeReceiver extends BroadcastReceiver
    {
        private final String DEBUG_TAG = getClass().getSimpleName();

        @Override
        public void onReceive(final Context context, Intent intent)
        {
            Log.d(DEBUG_TAG, "Time changed");

            // Make sure current day is visible
            if (today == null || !TimeHelper.isToday(today))
            {
                Log.d("MyDateChangeReceiver", "Date changed");
                initializeLoader(URL_LOADER_TODAY);
            }
        }
    }
}