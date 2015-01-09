package nl.marcnolte.coffeecounter;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.Tab;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import nl.marcnolte.coffeecounter.contentprovider.MyContentProvider;
import nl.marcnolte.coffeecounter.database.DatabaseContract;

public class MainActivity extends ActionBarActivity implements
    ViewPager.OnPageChangeListener,
    ActionBar.TabListener,
    ConfirmDeleteDialog.ConfirmDeleteDialogListener
{
    private MainTabsPager mAdapter;
    private ViewPager     mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_counter);

        // Initialize tabs pager adapter
        mAdapter   = new MainTabsPager(getSupportFragmentManager());

        // Initialize view pager
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mAdapter);

        // Initialize action bar
        getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        for (String tab_name : mAdapter.TITLES)
        {
            getSupportActionBar().addTab(getSupportActionBar().newTab().setText(tab_name).setTabListener(this));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.action_settings:
                //openSettings();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
    {
        // Do stuff
    }

    @Override
    public void onPageScrollStateChanged(int state)
    {
        // Do stuff
    }

    @Override
    public void onPageSelected(int position)
    {
        getSupportActionBar().setSelectedNavigationItem(position);
    }

    @Override
    public void onTabSelected(Tab tab, FragmentTransaction ft)
    {
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(Tab tab, FragmentTransaction fragmentTransaction)
    {
        // Do stuff
    }

    @Override
    public void onTabReselected(Tab tab, FragmentTransaction fragmentTransaction)
    {
        // Do stuff
    }

    @Override
    public void onConfirmDeleteDialogPositiveClick(ConfirmDeleteDialog dialog, int entryID)
    {
        deleteEntry(entryID);
    }

    public void deleteEntry(int entryID)
    {
        // Build uri
        Uri.Builder uri = new Uri.Builder();
        uri.scheme("content");
        uri.authority(MyContentProvider.AUTHORITY);
        uri.path(MyContentProvider.BASE_PATH + "/" + DatabaseContract.Entries.TABLE_NAME);

        // Prep query
        String where       = DatabaseContract.Entries.COLUMN_NAME_ID + "= ?";
        String whereArgs[] = { String.valueOf(entryID) };

        int rowsDeleted = getContentResolver().delete(uri.build(), where, whereArgs);

        // Run query
        if (rowsDeleted > 0)
        {
            // Show toast feedback
            Toast.makeText(this, R.string.toast_entry_deleted, Toast.LENGTH_SHORT).show();
        }
    }
}
