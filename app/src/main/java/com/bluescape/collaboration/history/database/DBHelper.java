package com.bluescape.collaboration.history.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.bluescape.AppConstants;

public class DBHelper extends SQLiteOpenHelper {

	public static final String TAG = DBHelper.class.getSimpleName();

	private static final String JSON_ARRAY = "jsonArray";
	final String WORKSPACES_HISTORY_TABLE_QUERY = "CREATE TABLE " + DBConstants.WORKSPACES_HISTORY_TABLE_NAME + "(" + DBConstants.JSON_PATH
													+ " TEXT ," + DBConstants.WORKSPACES_ID + " TEXT ," + DBConstants.JSON_ARRAY + " TEXT " + ");";

	public DBHelper(Context context) {
		super(context, DBConstants.DB_NAME, null, DBConstants.DB_VERSION);
	}

	public String getVisitedWorkspaceJSON(DBHelper mDbHelper, String workspaceId, String jsonPath) {
		SQLiteDatabase mSqLiteDatabase = mDbHelper.getReadableDatabase();
		String[] columns = { DBConstants.JSON_ARRAY };
		String whereClause = DBConstants.WORKSPACES_ID + "='" + workspaceId + "'" + " AND " + DBConstants.JSON_PATH + "='" + jsonPath + "'";
		Cursor mCursor = mSqLiteDatabase.query(DBConstants.WORKSPACES_HISTORY_TABLE_NAME, columns, whereClause, null, null, null, null);
		mCursor.moveToFirst();
		return mCursor.getString(0);
	}

	public void insertIntoWorkspacesHistory(DBHelper mDbHelper, String workspaceId, String jsonPath, String jsonArray) {
		SQLiteDatabase mSqLiteDatabase = mDbHelper.getWritableDatabase();
		ContentValues mContentValues = new ContentValues();
		mContentValues.put(DBConstants.WORKSPACES_ID, workspaceId);
		mContentValues.put(DBConstants.JSON_PATH, jsonPath);
		mContentValues.put(DBConstants.JSON_ARRAY, jsonArray);
		mSqLiteDatabase.insert(DBConstants.WORKSPACES_HISTORY_TABLE_NAME, null, mContentValues);
	}

	public boolean isAlreadyRetrievedWorkspaceJSON(DBHelper mDbHelper, String workspaceId, String jsonPath) {
		SQLiteDatabase mSqLiteDatabase = mDbHelper.getReadableDatabase();
		String[] columns = { DBConstants.WORKSPACES_ID };
		String whereClause = DBConstants.WORKSPACES_ID + "='" + workspaceId + "'" + " AND " + DBConstants.JSON_PATH + "='" + jsonPath + "'";
		Cursor mCursor = mSqLiteDatabase.query(DBConstants.WORKSPACES_HISTORY_TABLE_NAME, columns, whereClause, null, null, null, null);
		if (mCursor.getCount() > 0) {
			return AppConstants.SAVED;
		} else {
			return AppConstants.NOT_SAVED;
		}
	}

	@Override
	public void onCreate(SQLiteDatabase Sdb) {
		Sdb.execSQL(WORKSPACES_HISTORY_TABLE_QUERY);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

}
