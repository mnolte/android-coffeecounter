package nl.marcnolte.coffeecounter;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.Loader;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorTreeAdapter;
import android.widget.TextView;

import java.text.DateFormat;

import nl.marcnolte.coffeecounter.libraries.TimeHelper;

public class EntryListAdapter extends CursorTreeAdapter
{
    private static EntryListFragment    mFragment;
    private static LayoutInflater       mInflater;
    private static DateFormat           mDateFormat;
    private static DateFormat           mTimeFormat;
    private SparseArray<String>         mGroupMap;

    private static class GroupViewHolder
    {
        public TextView tvDate;
        public TextView tvAmount;
    }
    private static class ChildViewHolder
    {
        public TextView tvTime;
    }

    /**
     * Constructor. The adapter will call {@link android.database.Cursor#requery()} on the cursor whenever
     * it changes so that the most recent data is always displayed.
     *
     * @param fragment  Interface to the fragment's global information.
     * @param context   Interface to application's global information.
     */
    public EntryListAdapter(EntryListFragment fragment, Context context)
    {
        super(null, context);

        mDateFormat = android.text.format.DateFormat.getDateFormat(context.getApplicationContext());
        mTimeFormat = android.text.format.DateFormat.getTimeFormat(context.getApplicationContext());
        mInflater   = LayoutInflater.from(context);
        mFragment   = fragment;
        mGroupMap   = new SparseArray<String>();
    }

    /**
     * Gets the Cursor for the children at the given group.
     *
     * @param groupCursor The cursor from which to get the data.
     *
     * @return Return the newly created cursor. (In this case null because the cursor is
     *         created by a loader).
     */
    @Override
    protected Cursor getChildrenCursor(Cursor groupCursor)
    {
        // Get group parameters
        int    groupPos    = groupCursor.getPosition();
        String groupDate   = groupCursor.getString(groupCursor.getColumnIndex("date"));

        // Add group to the map
        mGroupMap.put(groupPos, groupDate);

        // Start loading group data
        Loader loader = mFragment.getLoaderManager().getLoader(groupPos);
        if (loader != null && !loader.isReset())
        {
            mFragment.getLoaderManager().restartLoader(groupPos, null, mFragment);
        }
        else
        {
            mFragment.getLoaderManager().initLoader(groupPos, null, mFragment);
        }

        return null;
    }

    /**
     * Makes a new group view to hold the group data pointed to by cursor.
     *
     * @param context    Interface to application's global information
     * @param cursor     The group cursor from which to get the data.
     * @param isExpanded Whether the group is expanded.
     * @param parent     The parent to which the new view is attached to
     * @return The newly created view.
     */
    @Override
    protected View newGroupView(Context context, Cursor cursor, boolean isExpanded, ViewGroup parent)
    {
        final View            groupView       = mInflater.inflate(R.layout.fragment_list_group, parent, false);
        final GroupViewHolder groupViewHolder = new GroupViewHolder();

        groupViewHolder.tvDate   = (TextView) groupView.findViewById(R.id.groupDate);
        groupViewHolder.tvAmount = (TextView) groupView.findViewById(R.id.groupAmount);

        groupView.setTag(groupViewHolder);

        return groupView;
    }

    /**
     * Bind an existing view to the group data pointed to by cursor.
     *
     * @param view       Existing view, returned earlier by newGroupView.
     * @param context    Interface to application's global information
     * @param cursor     The cursor from which to get the data.
     * @param isExpanded Whether the group is expanded.
     */
    @Override
    protected void bindGroupView(View view, Context context, Cursor cursor, boolean isExpanded)
    {
        GroupViewHolder groupViewHolder = (GroupViewHolder) view.getTag();

        String date   = cursor.getString(cursor.getColumnIndexOrThrow("date"));
        String amount = cursor.getString(cursor.getColumnIndexOrThrow("amount"));

        groupViewHolder.tvDate.setText(TimeHelper.formatDate(date, mDateFormat));
        groupViewHolder.tvAmount.setText(amount);
    }

    /**
     * Makes a new child view to hold the data pointed to by cursor.
     *
     * @param context     Interface to application's global information
     * @param cursor      The cursor from which to get the data.
     * @param isLastChild Whether the child is the last child within its group.
     * @param parent      The parent to which the new view is attached to
     * @return the newly created view.
     */
    @Override
    protected View newChildView(Context context, Cursor cursor, boolean isLastChild, ViewGroup parent)
    {
        final View            childView       = mInflater.inflate(R.layout.fragment_list_group_item, parent, false);
        final ChildViewHolder childViewHolder = new ChildViewHolder();

        childViewHolder.tvTime   = (TextView) childView.findViewById(R.id.groupChildTime);

        childView.setTag(childViewHolder);

        return childView;
    }

    /**
     * Bind an existing view to the child data pointed to by cursor
     *
     * @param view        Existing view, returned earlier by newChildView
     * @param context     Interface to application's global information
     * @param cursor      The cursor from which to get the data.
     * @param isLastChild Whether the child is the last child within its group.
     */
    @Override
    protected void bindChildView(View view, Context context, Cursor cursor, boolean isLastChild)
    {
        ChildViewHolder childViewHolder = (ChildViewHolder) view.getTag();

        String time = cursor.getString(cursor.getColumnIndexOrThrow("time"));

        childViewHolder.tvTime.setText(TimeHelper.formatTime(time, mTimeFormat));
    }

    /**
     * Gets the map for groups. This map holds the position and corresponding date for each
     * group. So the loader can fetch the list items for each group.
     *
     * @return The group map.
     */
    public SparseArray<String> getGroupMap()
    {
        return mGroupMap;
    }
}
