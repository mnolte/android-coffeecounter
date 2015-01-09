package nl.marcnolte.coffeecounter.database;

import android.provider.BaseColumns;

public final class DatabaseContract
{
    public  static final int    DATABASE_VERSION = 1;
    public  static final String DATABASE_NAME    = "CoffeeCounter.db";
    private static final String TYPE_INT         = " INTEGER";
    private static final String TYPE_TEXT        = " TEXT";
    private static final String SEP_COMMA        = ",";

    private DatabaseContract() {}

    public static abstract class Entries implements BaseColumns
    {
        /* Do not allow this class to be instantiated */
        private Entries() {}

        public static final String   TABLE_NAME            = "entries";
        public static final String   COLUMN_NAME_ID        = "_id";
        public static final String   COLUMN_NAME_DATETIME  = "datetime";
        public static final String   COLUMN_NAME_CREATED   = "created";
        public static final String   COLUMN_NAME_NULLABLE  = COLUMN_NAME_DATETIME;
        public static final String[] COLUMN_NAMES          = {
            COLUMN_NAME_ID,
            COLUMN_NAME_DATETIME,
            COLUMN_NAME_CREATED
        };

        public static final String   SQL_CREATE_TABLE      =
            "CREATE TABLE " + TABLE_NAME + "(" +
                COLUMN_NAME_ID       + TYPE_INT  + " PRIMARY KEY AUTOINCREMENT NOT NULL" + SEP_COMMA +
                COLUMN_NAME_DATETIME + TYPE_TEXT + " DEFAULT CURRENT_TIMESTAMP NOT NULL" + SEP_COMMA +
                COLUMN_NAME_CREATED  + TYPE_TEXT + " DEFAULT CURRENT_TIMESTAMP NOT NULL" +
                ")";

        public static final String SQL_DELETE_TABLE        =
            "DROP TABLE IF EXISTS " + TABLE_NAME;
    }
}
