package jp.meridiani.apps.dialprefixer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jp.meridiani.apps.dialprefixer.RuleEntry.Key;

import android.app.backup.BackupManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class RuleStore {

	private static RuleStore mInstance = null;
	
	private SQLiteDatabase mDB;
	private Context mContext;

	private static final String DATABASE_NAME = "rulelist.db";
	private static final int    DATABASE_VERSION = 1;
	  
	private static final String MISC_TABLE_NAME = "misc";
	private static final String COL_KEY         = "key";
	private static final String COL_VALUE       = "value";
	
	private static final String LIST_TABLE_NAME = "rulelist";
	private static final String COL_UUID        = "uuid";
	private static final String COL_ORDER       = "order";
	
	private static final String DATA_TABLE_NAME = "ruledata";
	private static final String COL_ENABLE      = "enable";
	private static final String COL_USERRULE    = "userrule";
	private static final String COL_NAME        = "name";
	private static final String COL_UUID        = "uuid";
	private static final String COL_ORDER       = "order";
	private static final String COL_UUID        = "uuid";
	private static final String COL_ORDER       = "order";
	private static final String COL_UUID        = "uuid";
	private static final String COL_ORDER       = "order";
	
	private static final String KEY_ENABLERULES = "EnableRules";

	private static final String RULES_START = "<rules>";
	private static final String RULES_END   = "</rules>";
	
	private static class DBHelper extends SQLiteOpenHelper {

		public DBHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(String.format(
					"CREATE TABLE %1$s ( _id INTEGER PRIMARY KEY AUTOINCREMENT, %2$s TEXT NOT NULL UNIQUE, %3$s TEXT);",
						MISC_TABLE_NAME, COL_KEY, COL_VALUE));

			db.execSQL(String.format(
					"CREATE TABLE %1$s ( _id INTEGER PRIMARY KEY AUTOINCREMENT, %2$s TEXT NOT NULL UNIQUE, %3$s INTEGER);",
						LIST_TABLE_NAME, COL_UUID, COL_ORDER));

			db.execSQL(String.format(
					"CREATE TABLE %1$s ( _id INTEGER PRIMARY KEY AUTOINCREMENT, %2$s TEXT NOT NULL, %3$s TEXT NOT NULL, %4$s TEXT NOT NULL);",
						DATA_TABLE_NAME, COL_UUID, COL_KEY, COL_VALUE));
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		}
	}

	private RuleStore(Context context) {
		mDB = new DBHelper(context).getWritableDatabase();
		mContext = context;
	}

	public static synchronized RuleStore getInstance(Context context) {
		if ( mInstance == null ) {
			mInstance = new RuleStore(context);
		}
		return mInstance;
	}

	public ArrayList<RuleEntry> listRules() {
		ArrayList<RuleEntry> list = new ArrayList<RuleEntry>();

		Cursor listCur = mDB.query(LIST_TABLE_NAME, null, null, null, null, null, COL_ORDER);
		try {
			while (listCur.moveToNext()) {
				UUID uuid = UUID.fromString(listCur.getString(listCur.getColumnIndex(COL_UUID)));
				int order = listCur.getInt(listCur.getColumnIndex(COL_ORDER));
				RuleEntry ruleEntry = new RuleEntry(uuid);
				ruleEntry.setOrder(order);
				loadRuleEntryInternal(ruleEntry);
				list.add(ruleEntry);
			}
		}
		finally {
			listCur.close();
		}
		return list;
	}

	public void updateOrder(List<RuleEntry> list) {

		mDB.beginTransaction();

		try {
			ContentValues values = new ContentValues();
			for (RuleEntry ruleEntry : list) {
				// update/insert list
				values.clear();
				values.put(COL_ORDER, ruleEntry.getOrder());
				mDB.update(LIST_TABLE_NAME, values,
						String.format("%1$s=?", COL_UUID),
						new String[]{ruleEntry.getUuid().toString()});
			}
			mDB.setTransactionSuccessful();
			requestBackup();
		}
		finally {
			mDB.endTransaction();
		}
	}

	private RuleEntry loadRuleEntryInternal(RuleEntry profile) {
		String uuid = profile.getUuid().toString();
		Cursor dataCur = mDB.query(DATA_TABLE_NAME, null, COL_UUID + "=?", new String[]{uuid}, null, null, null);
		try {
			while (dataCur.moveToNext()) {
				String key = dataCur.getString(dataCur.getColumnIndex(COL_KEY));
				String value = dataCur.getString(dataCur.getColumnIndex(COL_VALUE));
				profile.setValue(key, value);
			}
		}
		finally {
			dataCur.close();
		}
		return profile;
	}

	public RuleEntry loadRuleEntry(UUID uuid) {
		Cursor listCur = mDB.query(LIST_TABLE_NAME, null, COL_UUID + "=?", new String[] {uuid.toString()}, null, null, null);
		try {
			if (listCur.moveToFirst()) {
				int order = listCur.getInt(listCur.getColumnIndex(COL_ORDER));
				RuleEntry ruleEntry = new RuleEntry(uuid);
				ruleEntry.setOrder(order);
				return loadRuleEntryInternal(ruleEntry);
			}
		}
		finally {
			listCur.close();
		}
		return null;
	}

	public void storeRuleEntry(RuleEntry ruleEntry) {
		mDB.beginTransaction();

		try {
			ContentValues values = new ContentValues();
			// update/insert list
			{
				values.clear();
				values.put(COL_ORDER, ruleEntry.getOrder());
				int rows = mDB.update(LIST_TABLE_NAME, values,
						String.format("%1$s=?", COL_UUID),
						new String[]{ruleEntry.getUuid().toString()});
				if (rows < 1) {
					values.put(COL_UUID, ruleEntry.getUuid().toString());
					mDB.insert(LIST_TABLE_NAME, null, values);
				}
			}
			// update/insert data
			for (Key key : RuleEntry.listDataKeys()) {
				values.clear();
				values.put(COL_VALUE, ruleEntry.getValue(key));
				int rows = mDB.update(DATA_TABLE_NAME, values,
						String.format("%1$s=? and %2$s=?", COL_UUID, COL_KEY),
						new String[]{ruleEntry.getUuid().toString(), key.name()});
				if (rows < 1) {
					values.put(COL_UUID, ruleEntry.getUuid().toString());
					values.put(COL_KEY, key.name());
					mDB.insert(DATA_TABLE_NAME, null, values);
				}
			}
			mDB.setTransactionSuccessful();
			requestBackup();
		}
		finally {
			mDB.endTransaction();
		}
	}

	public void deleteProfile(UUID profileId) {
		mDB.beginTransaction();

		try {
			// delete existent profile

			// delete data
			mDB.delete(DATA_TABLE_NAME, COL_UUID+"=?", new String[]{profileId.toString()});

			// delete list
			mDB.delete(LIST_TABLE_NAME, COL_UUID+"=?", new String[]{profileId.toString()});

			mDB.setTransactionSuccessful();
			requestBackup();
		}
		finally {
			mDB.endTransaction();
		}
	}

	private int getMaxOrder() {
		Cursor listCur = mDB.rawQuery(String.format(
				"select max(%2$s) from %1$s;", LIST_TABLE_NAME, COL_ORDER),null);
		try {
			if (listCur.moveToFirst()) {
				return listCur.getInt(0);
			}
		}
		finally {
			listCur.close();
		}
		return 0;
	}

	public boolean isEnableRules() {
		Cursor cur = mDB.query(MISC_TABLE_NAME, null, COL_KEY + "=?", new String[] {KEY_ENABLERULES}, null, null, null);
		try {
			if (cur.moveToFirst()) {
				return Boolean.parseBoolean(cur.getString(cur.getColumnIndex(COL_VALUE)));
			}
		}
		finally {
			cur.close();
		}
		return false;
	}

	public void setEnableRules(boolean enable) {
		mDB.beginTransaction();

		try {
			ContentValues values = new ContentValues();

			// update/insert data
			values.put(COL_KEY,   KEY_ENABLERULES);
			values.put(COL_VALUE, Boolean.toString(enable));
			int rows = mDB.update(MISC_TABLE_NAME, values,
					String.format("%1$s=?", COL_KEY),
					new String[]{KEY_ENABLERULES});
			if (rows < 1) {
				values.put(COL_KEY,   KEY_ENABLERULES);
				values.put(COL_VALUE, Boolean.toString(enable));
				mDB.insert(MISC_TABLE_NAME, null, values);
			}

			mDB.setTransactionSuccessful();
		}
		finally {
			mDB.endTransaction();
		}
	}

	public RuleEntry newRuleEntry() {
		RuleEntry ruleEntry = new RuleEntry();
		ruleEntry.setOrder(getMaxOrder()+1);
		return ruleEntry;
	}

	public void writeToText(BufferedWriter wtr) throws IOException {
		wtr.write(RULES_START); wtr.newLine();
		for (RuleEntry ruleEntry : listRules()) {
			ruleEntry.writeToText(wtr);
		}
		wtr.write(RULES_END); wtr.newLine();
	}

	public void readFromText(BufferedReader rdr) throws IOException {
    	String line;
		while ((line = rdr.readLine()) != null) {
			if (RULES_START.equals(line)) {
				RuleEntry ruleEntry;
	            while ((ruleEntry = RuleEntry.createFromText(rdr)) != null) {
	            	storeRuleEntry(ruleEntry);
	            }
			}
		}
	}

	private void requestBackup() {
		BackupManager.dataChanged(mContext.getPackageName());
	}
}