package nl.marcnolte.coffeecounter.contentprovider;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import java.util.Arrays;
import java.util.HashSet;

import nl.marcnolte.coffeecounter.database.DatabaseContract;
import nl.marcnolte.coffeecounter.database.DatabaseHelper;
import nl.marcnolte.coffeecounter.libraries.TimeHelper;

public class MyContentProvider extends ContentProvider
{
    /**
     * Debug Tag
     */
    private final String DEBUG_TAG = getClass().getSimpleName();

    /**
     * Authority
     */
    public static final String AUTHORITY = "nl.marcnolte.coffeecounter.contentprovider.MyContentProvider";

    /**
     * ContentUri
     */
    public static final String BASE_PATH = "resources";

    /**
     * UriMatcher
     */
    private static final int ENTRIES             = 1;
    private static final int ENTRIES_ID          = 2;
    private static final int ENTRIES_GROUPS      = 3;
    private static final int ENTRIES_GROUPS_DATE = 4;
    private static final int ENTRIES_AVERAGE     = 5;
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static
    {
        // Entries table
        sUriMatcher.addURI(AUTHORITY, BASE_PATH + "/" + DatabaseContract.Entries.TABLE_NAME,               ENTRIES);
        sUriMatcher.addURI(AUTHORITY, BASE_PATH + "/" + DatabaseContract.Entries.TABLE_NAME + "/#",        ENTRIES_ID);
        sUriMatcher.addURI(AUTHORITY, BASE_PATH + "/" + DatabaseContract.Entries.TABLE_NAME + "/groups",   ENTRIES_GROUPS);
        sUriMatcher.addURI(AUTHORITY, BASE_PATH + "/" + DatabaseContract.Entries.TABLE_NAME + "/groups/*", ENTRIES_GROUPS_DATE);
        sUriMatcher.addURI(AUTHORITY, BASE_PATH + "/" + DatabaseContract.Entries.TABLE_NAME + "/avg",      ENTRIES_AVERAGE);
    }

    /**
     * Database
     */
    private SQLiteDatabase db;
    private DatabaseHelper dbHelper;

    /**
     * Initialize content provider on startup. This method is called for all registered
     * content providers on the application main thread at application launch time.
     *
     * @return true if the provider was successfully loaded, false otherwise
     */
    @Override
    public boolean onCreate()
    {
        // Initialize result
        boolean result = true;

        // Initialize database helper
        dbHelper = new DatabaseHelper(getContext());

        // Initialize database
        db = dbHelper.getWritableDatabase();

        // Check database was initialized
        if (db == null)
        {
            result = false;
        }
        // Check database is readonly
        else if (db.isReadOnly())
        {
            db.close();
            db     = null;
            result = false;
        }

        return result;
    }

    /**
     * Handle query requests from clients.
     *
     * @param uri           URI to query.
     * @param projection    List of columns to put into the cursor.
     * @param selection     Selection criteria to apply when filtering rows.
     * @param selectionArgs Selection criteria values to apply when filtering rows.
     * @param sortOrder     How the rows in the cursor should be sorted.
     *
     * @return a Cursor or null.
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder)
    {
        try
        {
            // Initialize query builder
            SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

            // Initialize cursor
            Cursor cursor;

            // Handle query depending on uri
            switch (sUriMatcher.match(uri))
            {
                // Entries table
                case ENTRIES:
                case ENTRIES_ID:
                case ENTRIES_GROUPS_DATE:
                    // @TODO Check for valid columns
                    // checkColumns(DatabaseContract.Entries.COLUMN_NAMES, projection);
                    // Set tables
                    queryBuilder.setTables(DatabaseContract.Entries.TABLE_NAME);
                    // Run query
                    cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
                    break;

                // Entries table (grouped by date)
                case ENTRIES_GROUPS:
                    // @TODO Check for valid columns
                    // checkColumns(DatabaseContract.Entries.COLUMN_NAMES, projection);
                    // Set tables
                    queryBuilder.setTables(DatabaseContract.Entries.TABLE_NAME);
                    // Group results by date
                    String groupBy = "DATE("  + DatabaseContract.Entries.COLUMN_NAME_DATETIME + ", '+" + TimeHelper.getOffset() + " SECONDS')";
                    // Run query
                    cursor = queryBuilder.query(db, projection, selection, selectionArgs, groupBy, null, sortOrder);
                    break;

                // Entries table (average)
                case ENTRIES_AVERAGE:
                    // @TODO Check for valid columns
                    // checkColumns(DatabaseContract.Entries.COLUMN_NAMES, projection);
                    // Set tables
                    queryBuilder.setTables(
                        "(" +
                            "SELECT " +
                                "DATE(" + DatabaseContract.Entries.COLUMN_NAME_DATETIME + ", '+" + TimeHelper.getOffset() + " SECONDS') AS date," +
                                "COUNT(*) AS amount" +
                            " FROM " + DatabaseContract.Entries.TABLE_NAME +
                            " GROUP BY DATE(" + DatabaseContract.Entries.COLUMN_NAME_DATETIME + ", '+" + TimeHelper.getOffset() + " SECONDS')" +
                        ") AS t"
                    );
                    // Run query
                    cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
                    break;

                // Unknown Uri
                default:
                    throw new IllegalArgumentException("Unknown URI: " + uri);
            }

            // Notify potential listeners
            cursor.setNotificationUri(getContext().getContentResolver(), uri);

            Log.d(DEBUG_TAG, "Query on uri: " + uri);

            return cursor;
        }
        catch(Exception e)
        {
            Log.e(DEBUG_TAG, e.getMessage());
            Log.e(DEBUG_TAG, Log.getStackTraceString(e));

            return null;
        }
    }

    @Override
    public String getType(Uri uri)
    {
        switch (sUriMatcher.match(uri))
        {
            // Entries table (multiple rows)
            case ENTRIES:
            case ENTRIES_GROUPS:
            case ENTRIES_GROUPS_DATE:
                return ContentResolver.CURSOR_DIR_BASE_TYPE  + "/" + DatabaseContract.DATABASE_NAME + "/" + DatabaseContract.Entries.TABLE_NAME;

            // Entries table (single row)
            case ENTRIES_ID:
                return ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + DatabaseContract.DATABASE_NAME + "/" + DatabaseContract.Entries.TABLE_NAME;

            // Unknown Uri
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values)
    {
        // Validate URI
        if (sUriMatcher.match(uri) != ENTRIES)
        {
            throw new IllegalArgumentException("Invalid URI: " + uri);
        }

        // Run query
        long id = db.insert(DatabaseContract.Entries.TABLE_NAME, DatabaseContract.Entries.COLUMN_NAME_NULLABLE, values);

        // Verify query was successful
        if (id > 0)
        {
            /* Backup database (for debugging)
            try {
                dbHelper.writeToSD();
            } catch(Exception e) {
                Log.e(DEBUG_TAG, e.getMessage());
            }
            */

            // Notify content change
            getContext().getContentResolver().notifyChange(uri, null);

            // Return uri
            return ContentUris.withAppendedId(uri, id);
        }
        else
        {
            // Throw error exception
            throw new SQLException("Error inserting into table: " + DatabaseContract.Entries.TABLE_NAME);
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs)
    {
        // Initialize rows deleted count
        int rowsDeleted = 0;

        // Handle deleting depending on uri
        switch (sUriMatcher.match(uri))
        {
            // Entries table (multiple rows)
            case ENTRIES:
                rowsDeleted = db.delete(DatabaseContract.Entries.TABLE_NAME, selection, selectionArgs);
                break;

            // Entries table (single row)
            case ENTRIES_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted = db.delete(DatabaseContract.Entries.TABLE_NAME, DatabaseContract.Entries.COLUMN_NAME_ID + "=" + id, null);
                } else {
                    rowsDeleted = db.delete(DatabaseContract.Entries.TABLE_NAME, DatabaseContract.Entries.COLUMN_NAME_ID + "=" + id + " and " + selection, selectionArgs);
                }
                break;

            // Unknown Uri
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        if (rowsDeleted > 0)
        {
            // Backup database (for debugging)
            try {
                dbHelper.writeToSD();
            } catch(Exception e) {
                Log.e(DEBUG_TAG, e.getMessage());
            }

            // Notify potential listeners
            getContext().getContentResolver().notifyChange(uri, null);
        }
        else
        {
            // Throw error exception
            throw new SQLException("Error deleting from table: " + DatabaseContract.Entries.TABLE_NAME);
        }

        // Return rows deleted
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs)
    {
        return 0;
    }

    /**
     * Check if all columns which are requested are available.
     *
     * @param available     Available columns.
     * @param projection    Requested columns.
     */
    private void checkColumns(String[] available, String[] projection)
    {
        if (projection != null)
        {
            HashSet<String> requestedColumns = new HashSet<String>(Arrays.asList(projection));
            HashSet<String> availableColumns = new HashSet<String>(Arrays.asList(available));
            if (!availableColumns.containsAll(requestedColumns))
            {
                throw new IllegalArgumentException("Unknown columns in projection");
            }
        }
    }
}