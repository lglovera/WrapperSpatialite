package com.example.llove.wrapperspatialite;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;

import org.json.JSONArray;
import org.json.JSONObject;
import org.spatialite.Cursor;
import org.spatialite.database.SQLiteDatabase;
import org.spatialite.database.SQLiteOpenHelper;

public class SpatialiteWrapper extends ReactContextBaseJavaModule {

    SQLiteOpenHelper eventsData;
    SQLiteDatabase db;

    public SpatialiteWrapper(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @ReactMethod
    public void createConnection(ReadableMap args, Callback success, Callback error) {
        try {
            SQLiteDatabase.loadLibs(this.getReactApplicationContext());

            eventsData = new SQLiteOpenHelper(this.getReactApplicationContext(), args.getString("ConnectionString"), null, 1) {
                @Override
                public void onCreate(SQLiteDatabase db) {

                }

                @Override
                public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

                }
            };

            db = eventsData.getWritableDatabase();

            if (!db.isOpen()) {
                error.invoke("Getting Database Failed for: " + args.getString("ConnectionString"));
            }

            success.invoke(db.isOpen());
        } catch (RuntimeException re) {
            error.invoke(re.getMessage());
        }
    }

    @ReactMethod
    public void executeQuery(ReadableMap args, Callback success, Callback error) {
        try {
            JSONObject rootObj = new JSONObject();
            Cursor cursor = db.rawQuery(args.getString("query"), null);
            String[] columnNames = cursor.getColumnNames();

            JSONArray jArray = new JSONArray();

            while (cursor.moveToNext()) {
                JSONObject obj = new JSONObject();

                for (String column : columnNames) {

                    switch (cursor.getType(cursor.getColumnIndex(column))) {
                        case Cursor.FIELD_TYPE_NULL:
                            obj.put(column, null);
                            break;
                        case Cursor.FIELD_TYPE_INTEGER:
                            obj.put(column, cursor.getInt(cursor.getColumnIndex(column)));
                            break;
                        case Cursor.FIELD_TYPE_FLOAT:
                            obj.put(column, cursor.getFloat(cursor.getColumnIndex(column)));
                            break;
                        case Cursor.FIELD_TYPE_STRING:
                            obj.put(column, cursor.getString(cursor.getColumnIndex(column)));
                            break;
                        case Cursor.FIELD_TYPE_BLOB:
                            obj.put(column, cursor.getBlob(cursor.getColumnIndex(column)));
                            break;
                    }

                    jArray.put(obj);
                }
            }

            success.invoke( rootObj );
        } catch (RuntimeException re) {
            error.invoke(re.getMessage());
        } catch (Exception re) {
            error.invoke(re.getMessage());
        }
    }

    @ReactMethod
    public void closeConnection(ReadableMap args, Callback success, Callback error) {
        try {
            db.close();
            success.invoke();
        } catch (RuntimeException re) {
            error.invoke(re.getMessage());
        }
    }

    @Override
    public String getName() {
        return "WrapperSpatialite";
    }
}
