package nl.marcnolte.coffeecounter.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class DatabaseHelper extends SQLiteOpenHelper
{
    private final String DEBUG_TAG = getClass().getSimpleName();

    private Context mContext;

    public DatabaseHelper(Context context)
    {
        super(context, DatabaseContract.DATABASE_NAME, null, DatabaseContract.DATABASE_VERSION);

        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        String query = DatabaseContract.Entries.SQL_CREATE_TABLE;

        Log.i(DEBUG_TAG, "Database table created -> " + query);

        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        db.execSQL(DatabaseContract.Entries.SQL_DELETE_TABLE);

        // @TODO Remove (old table name)
        db.execSQL("DROP TABLE IF EXISTS entry");

        onCreate(db);

        Log.i(DEBUG_TAG, "Database version changed -> from " + oldVersion + " to " + newVersion);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        onUpgrade(db, oldVersion, newVersion);
    }

    public void writeToSD() throws IOException
    {
        String  dbPath;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
        {
            dbPath = mContext.getFilesDir().getAbsolutePath().replace("files", "databases") + File.separator;
        }
        else {
            dbPath = mContext.getFilesDir().getPath() + mContext.getPackageName() + "/databases/";
        }

        File sd = Environment.getExternalStorageDirectory();

        if (sd.canWrite())
        {
            String currentDBPath = DatabaseContract.DATABASE_NAME;
            String backupDBPath  = DatabaseContract.DATABASE_NAME + "_backup.db";
            File currentDB = new File(dbPath, currentDBPath);
            File backupDB  = new File(sd, backupDBPath);

            if (currentDB.exists())
            {
                FileChannel src = new FileInputStream(currentDB).getChannel();
                FileChannel dst = new FileOutputStream(backupDB).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
                Log.d(DEBUG_TAG, "Database exported to SD: " + backupDBPath);
            }
            else
            {
                Log.d(DEBUG_TAG, "Database exported to SD: " + backupDBPath);
            }
        }
        else
        {
            Log.e(DEBUG_TAG, "Database export failed");
        }
    }
}
