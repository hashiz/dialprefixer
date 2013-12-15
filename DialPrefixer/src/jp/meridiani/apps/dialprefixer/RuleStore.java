package jp.meridiani.apps.dialprefixer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jp.meridiani.apps.dialprefixer.RuleEntry.RuleColomuns;
import android.app.backup.BackupManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

public class RuleStore {

	private static RuleStore mInstance = null;
	
	private SQLiteDatabase mDB;
	private Context mContext;

	private static final String DEFAULT_RULE_FILE = "default_rule.tsv";

	private static final String DATABASE_NAME = "rulelist.db";
	private static final int    DATABASE_VERSION = 1;

	private static final String MISC_TABLE_NAME = "misc";
	private static final String COL_KEY         = "key";
	private static final String COL_VALUE       = "value";
	
	private static final String RULE_TABLE_NAME = "rulelist";
	private static final String COL_UUID        = RuleColomuns.UUID.toString();
	private static final String COL_RULEORDER   = RuleColomuns.RULEORDER.toString();
	private static final String COL_ENABLE      = RuleColomuns.ENABLE.toString();
	private static final String COL_USERRULE    = RuleColomuns.USERRULE.toString();
	private static final String COL_NAME        = RuleColomuns.NAME.toString();
	private static final String COL_ACTION      = RuleColomuns.ACTION.toString();
	private static final String COL_CONTINUE    = RuleColomuns.CONTINUE.toString();
	private static final String COL_PATTERN     = RuleColomuns.PATTERN.toString();
	private static final String COL_NEGATE      = RuleColomuns.NEGATE.toString();
	private static final String COL_REPLACEMENT = RuleColomuns.REPLACEMENT.toString();
	
	private static final String RULES_START = "<rules>";
	private static final String RULES_END   = "</rules>";

	private static class DBHelper extends SQLiteOpenHelper {

		private Context mContext;

		public DBHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
			mContext = context;
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(String.format(
					"CREATE TABLE %1$s ( _id INTEGER PRIMARY KEY AUTOINCREMENT, %2$s TEXT NOT NULL UNIQUE, %3$s TEXT);",
						MISC_TABLE_NAME, COL_KEY, COL_VALUE));

			db.execSQL(String.format(
					"CREATE TABLE %1$s ( _id INTEGER PRIMARY KEY AUTOINCREMENT, " +
								         "%2$s TEXT NOT NULL UNIQUE, " +
								         "%3$s INTEGER, " +
							             "%4$s, %5$s, %6$s, %7$s, %8$s, %9$s, %10$s, %11$s );",
							RULE_TABLE_NAME,	// 1
							COL_UUID,			// 2
							COL_RULEORDER,		// 3
							COL_NAME,			// 4
							COL_ENABLE,			// 5
							COL_USERRULE,		// 6
							COL_ACTION,			// 7
							COL_CONTINUE,		// 8
							COL_PATTERN,		// 9
							COL_NEGATE,			// 10
							COL_REPLACEMENT		// 11
						));

			loadDefaultRule(db);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		}

		private String join(String s, String[] a) {
			StringBuilder buf = new StringBuilder();
			String sep = "";
			for (String p : a) {
				buf.append(sep).append(p);
				sep = s;
			}
			return buf.toString();
		}

		private String getChars(String s, String c, int count) {
			StringBuilder buf = new StringBuilder();
			String sep = "";
			for (int i = 0; i < count; i++ ) {
				buf.append(sep).append(c);
				sep = s;
			}
			return buf.toString();
		}
		
		private void loadDefaultRule(SQLiteDatabase db) {
			try {
				InputStream input = mContext.getResources().getAssets().open(DEFAULT_RULE_FILE);
				BufferedReader rdr = new BufferedReader(new InputStreamReader(input));
				String line;
				line = rdr.readLine();
				if (line == null) {
					return;
				}
				String[] colnames = line.split("\t");
				SQLiteStatement stm = db.compileStatement("INSERT INTO " + RULE_TABLE_NAME + 
						" ( " + join(",", colnames) +" ) " +
						"VALUES ( " + getChars(",", "?", colnames.length)+ " );");
				while ((line = rdr.readLine())!= null) {
					stm.bindAllArgsAsStrings(line.split("\t"));
					stm.executeInsert();
					stm.clearBindings();
				}
				stm.close();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
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

	public ArrayList<RuleEntry> listRules(boolean enableOnly) {
		ArrayList<RuleEntry> list = new ArrayList<RuleEntry>();
		String whereClause = null;
		String[] whereArgs = null;
		if (enableOnly) {
			whereClause = COL_ENABLE + "=?";
			whereArgs   = new String[] {Boolean.TRUE.toString()};
		}
		
		Cursor listCur = mDB.query(RULE_TABLE_NAME, null, whereClause, whereArgs, null, null, COL_RULEORDER);
		try {
			while (listCur.moveToNext()) {
				UUID uuid = UUID.fromString(listCur.getString(listCur.getColumnIndex(COL_UUID)));
				int order = listCur.getInt(listCur.getColumnIndex(COL_RULEORDER));
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
				values.put(COL_RULEORDER, ruleEntry.getOrder());
				mDB.update(RULE_TABLE_NAME, values,
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

	private RuleEntry loadRuleEntryInternal(RuleEntry ruleEntry) {
		String uuid = ruleEntry.getUuid().toString();
		Cursor dataCur = mDB.query(RULE_TABLE_NAME, null, COL_UUID + "=?", new String[]{uuid}, null, null, null);
		try {
			if (dataCur.moveToFirst()) {
				for (String col : dataCur.getColumnNames()) {
					String value = dataCur.getString(dataCur.getColumnIndex(col));
					ruleEntry.setValue(col, value);
				}
			}
		}
		finally {
			dataCur.close();
		}
		return ruleEntry;
	}

	public RuleEntry loadRuleEntry(UUID uuid) {
		RuleEntry ruleEntry = new RuleEntry(uuid);
		return loadRuleEntryInternal(ruleEntry);
	}

	public void storeRuleEntry(RuleEntry ruleEntry) {
		mDB.beginTransaction();

		if (ruleEntry.getOrder() == 0) {
			ruleEntry.setOrder(getMaxOrder()+1);
		}

		try {
			ContentValues values = new ContentValues();
			// update/insert
			values.clear();
			for (RuleColomuns col : RuleColomuns.values()) {
				if (col == RuleColomuns.UUID) {
					// skip uuid
					continue;
				}
				values.put(col.toString(), ruleEntry.getValue(col));
			}
			int rows = mDB.update(RULE_TABLE_NAME, // table name
									values,          // values
									String.format("%1$s=?", COL_UUID), // where clause
									new String[]{ruleEntry.getUuid().toString()}); // where args
			if (rows < 1) {
				values.put(COL_UUID, ruleEntry.getUuid().toString());
				mDB.insert(RULE_TABLE_NAME, null, values);
			}
			mDB.setTransactionSuccessful();
			requestBackup();
		}
		finally {
			mDB.endTransaction();
		}
	}

	public void deleteRule(UUID uuid) {
		mDB.beginTransaction();

		try {
			// delete rule
			mDB.delete(RULE_TABLE_NAME, COL_UUID+"=?", new String[]{uuid.toString()});

			mDB.setTransactionSuccessful();
			requestBackup();
		}
		finally {
			mDB.endTransaction();
		}
	}

	private int getMaxOrder() {
		Cursor listCur = mDB.rawQuery(String.format(
				"select max(%2$s) from %1$s;", RULE_TABLE_NAME, COL_RULEORDER),null);
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

	public void writeToText(BufferedWriter wtr) throws IOException {
		wtr.write(RULES_START); wtr.newLine();
		for (RuleEntry ruleEntry : listRules(false)) {
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