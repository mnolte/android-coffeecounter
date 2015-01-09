package nl.marcnolte.coffeecounter;

import android.content.ContentValues;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
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

public class CounterFragment extends Fragment implements View.OnClickListener
{
    /**
     * Debug Tag
     */
    private final String DEBUG_TAG = getClass().getSimpleName();

    /**
     * Layout elements
     */
    private TextView   tvCounterAmount;
    private TextView   tvMinAmount;
    private TextView   tvMaxAmount;
    private TextView   tvAverageAmount;
    private MyObserver uriObserver;

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
        tvMinAmount     = (TextView) rootView.findViewById(R.id.fragment_counter_details_min_amount);
        tvMaxAmount     = (TextView) rootView.findViewById(R.id.fragment_counter_details_max_amount);
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

        setCounter();
        setStatistics();
    }

    @Override
    public void onResume()
    {
        super.onResume();

        // Register counter observer
        uriObserver = new MyObserver(new Handler());
        getActivity().getContentResolver().registerContentObserver(buildUri(null), true, uriObserver);
    }

    @Override
    public void onPause()
    {
        super.onPause();

        // Unregister observer
        getActivity().getContentResolver().unregisterContentObserver(uriObserver);
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
                incrementCounter();
                break;
        }
    }

    /**
     * Build uri
     */
    public static Uri buildUri(String appendToPath)
    {
        Uri.Builder uriBuilder = new Uri.Builder();
        uriBuilder.scheme("content");
        uriBuilder.authority(MyContentProvider.AUTHORITY);
        uriBuilder.path(MyContentProvider.BASE_PATH + "/" + DatabaseContract.Entries.TABLE_NAME);
        if (appendToPath != null)
        {
            uriBuilder.appendPath(appendToPath);
        }

        return uriBuilder.build();
    }

    /**
     * Increment counter
     */
    public void incrementCounter()
    {
        // Set values
        ContentValues values = new ContentValues();
        values.clear();
        values.put(DatabaseContract.Entries.COLUMN_NAME_DATETIME, TimeHelper.getDatetime("now", "UTC"));

        // Run query
        getActivity().getContentResolver().insert(buildUri(null), values);

        // Show toast feedback
        Toast.makeText(getActivity(), R.string.toast_counter_incremented, Toast.LENGTH_SHORT).show();
    }

    /**
     * Set counter
     */
    public void setCounter()
    {
        // Prep query
        String projection[]    = { "COUNT(*) AS amount" };
        String selection       = "DATETIME(" + DatabaseContract.Entries.COLUMN_NAME_DATETIME  + ", '+" + TimeHelper.getOffset() + " SECONDS') BETWEEN ? AND ?";
        String selectionArgs[] = { TimeHelper.getDatetime("sod", "local"), TimeHelper.getDatetime("end", "local") };
        // Run query
        Cursor cursor = getActivity().getContentResolver().query(buildUri(null), projection, selection, selectionArgs, null);
        if (cursor.moveToFirst())
        {
            // Set counter value
            tvCounterAmount.setText(cursor.getString(cursor.getColumnIndexOrThrow("amount")));
        }
        cursor.close();
    }

    /**
     * Set statistics
     */
    public void setStatistics()
    {
        // Prep query
        String projection[] = { "MIN(amount) AS minimum", "MAX(amount) AS maximum", "AVG(amount) AS average"  };
        // Run query
        Cursor cursor = getActivity().getContentResolver().query(buildUri("avg"), projection, null, null, null);
        if (cursor.moveToFirst())
        {
            // Set results
            // @TODO Add total to statistics
            tvMinAmount.setText(cursor.getString(cursor.getColumnIndexOrThrow("minimum")));
            tvMaxAmount.setText(cursor.getString(cursor.getColumnIndexOrThrow("maximum")));
            tvAverageAmount.setText(NumberHelper.getDecimal(cursor.getDouble(cursor.getColumnIndexOrThrow("average")), 0, 1));
        }
        cursor.close();
    }

    private class MyObserver extends ContentObserver
    {
        /**
         * Creates a new content observer instance.
         *
         * @param handler The handler to run onChange on, or null if none.
         */
        public MyObserver(Handler handler)
        {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange)
        {
            onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri)
        {
            setCounter();
            setStatistics();
        }
    }
}

