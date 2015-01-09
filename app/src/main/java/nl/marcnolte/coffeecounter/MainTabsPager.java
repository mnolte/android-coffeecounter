package nl.marcnolte.coffeecounter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

final public class MainTabsPager extends FragmentPagerAdapter
{
    final public String[] TITLES = { "Counter", "Entries" };

    public MainTabsPager(FragmentManager fm)
    {
        super(fm);
    }

    @Override
    public Fragment getItem(int index)
    {
        switch (index)
        {
            case 0: return CounterFragment.newInstance();
            case 1: return EntryListFragment.newInstance();
        }
        return null;
    }

    @Override
    public int getCount()
    {
        return TITLES.length;
    }
}