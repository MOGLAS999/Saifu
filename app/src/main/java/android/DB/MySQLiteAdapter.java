package android.DB;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import jp.shimi.saifu.DateChanger;
import jp.shimi.saifu.DayData;
import jp.shimi.saifu.DayList;
import jp.shimi.saifu.ItemData;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MySQLiteAdapter {
	static final private String DB_NAME = "Saifu.db";
	static final private String DAY_TABLE_NAME = "day_table";
	static final private String ITEM_TABLE_NAME = "item_table";
	static final private String WALLET_TABLE_NAME = "wallet_table";
	static final private int DB_VERSION = 5;

	static final private String COMMA = ",";

	static final private String DAY_TABLE_SCHEMA =
			"date text primary key" + COMMA +
			"balance integer" /*+ COMMA +
			"difference integer not null" +*/;

	static final private String ITEM_TABLE_SCHEMA =
			"id integer primary key autoincrement" + COMMA +
			"name text not null default '<<no_name>>'" + COMMA +
			"price integer not null default 0" + COMMA +
			"date text not null"+ COMMA +
			"number integer" + COMMA +
			"category integer" + COMMA +
			"sequence integer not null" + COMMA +
			"wallet_id integer not null default -1" + COMMA +
			"reverse_item_id integer default -1"; // 反転関係にあるアイテム(例：現金で電子マネーをチャージするなど)

	static final private String WALLET_TABLE_SCHEMA =
			"id integer primary key autoincrement"+ COMMA +
			"name text" /*+ COMMA +
			"currency text"+*/;

	private static Context context;
	private static MySQLiteOpenHelper DBHelper;
	private static SQLiteDatabase db;
	
	public MySQLiteAdapter(Context context){
		this.context = context;
		this.DBHelper = new MySQLiteOpenHelper(context);
		this.db = DBHelper.getWritableDatabase();
	}
	
	// デストラクタ処理(SQLiteDatabaseの解放)
	@Override
	protected void finalize() throws Throwable{
		try {
			super.finalize();
		}finally{
			destruction();
		}
	}
	
	public void destruction(){
		this.db.close();
	}
	
	private static class MySQLiteOpenHelper extends SQLiteOpenHelper{
		public MySQLiteOpenHelper(Context context) {
			super(context, DB_NAME, null, DB_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			// TODO:DayはItemTableから生成できるので、後々不要になるかもしれない
			db.execSQL("CREATE TABLE " + DAY_TABLE_NAME +"(" + DAY_TABLE_SCHEMA + ");");
			
			db.execSQL("CREATE TABLE " + ITEM_TABLE_NAME +"(" + ITEM_TABLE_SCHEMA + ");");

			db.execSQL("CREATE TABLE " + WALLET_TABLE_NAME +"(" + WALLET_TABLE_SCHEMA + ");");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			if(oldVersion < newVersion){
				if(oldVersion <= 2){
					db.execSQL("ALTER TABLE " + ITEM_TABLE_NAME +
							 " ADD sequence integer default 0;"
							 );
				}
				if((oldVersion == 3 || oldVersion == 4) && newVersion == 5){
					// DayTableの更新
					// 旧バージョンと同じカラムのテンポラリーテーブル作成
					db.execSQL("CREATE TEMPORARY TABLE tmp_" + DAY_TABLE_NAME + " (" +
							"date text not null," +
							"balance integer" +
							//"difference integer not null" +
							");"
					);

					// 既存テーブルから作成したテンポラリーテーブルにデータを入れる
					db.execSQL("INSERT INTO tmp_" + DAY_TABLE_NAME + " SELECT " +
							"date," +
							"balance" +
							" FROM " + DAY_TABLE_NAME + ";");

					// 既存テーブル削除
					db.execSQL("DROP TABLE " + DAY_TABLE_NAME + ";");


					// ItemTableの更新
					// 旧バージョンと同じカラムのテンポラリーテーブル作成
					db.execSQL("CREATE TEMPORARY TABLE tmp_" + ITEM_TABLE_NAME + " (" +
							"date text not null,"+
							"name text,"+
							"price integer,"+
							"number integer,"+
							"category integer,"+
							"sequence integer not null"+
							");"
					);

					// 既存テーブルから作成したテンポラリーテーブルにデータを入れる
					db.execSQL("INSERT INTO tmp_" + ITEM_TABLE_NAME + " SELECT " +
							"date," +
							"name," +
							"price," +
							"number," +
							"category," +
							"sequence" +
							" FROM " + ITEM_TABLE_NAME + ";");

					// 既存テーブル削除
					db.execSQL("DROP TABLE " + ITEM_TABLE_NAME + ";");

					// テーブル作成
					// 2つ同時にテーブル作成が行われるので、両方をテンポラリーテーブルに入れてからCreateする。
					onCreate(db);

					// テンポラリーテーブルから新テーブルにデータを入れる
					db.execSQL("INSERT INTO " + DAY_TABLE_NAME + " SELECT " +
							"date," +
							"balance" +
							" FROM tmp_" + DAY_TABLE_NAME + ";");

					// テンポラリーテーブルから新テーブルにデータを入れる
					db.execSQL("INSERT INTO " + ITEM_TABLE_NAME +
							"(name, price, date, number, category, sequence, wallet_id, reverse_item_id)" +
							" SELECT " +
							"name," +
							"price," +
							"date," +
							"number," +
							"category," +
							"sequence," +
							"-1," +
							"-1" +
							" FROM tmp_" + ITEM_TABLE_NAME + ";");
				}
			}
		}
	}
	
	public static ContentValues getDayDataContentValues(DayData dayData){
		ContentValues values = new ContentValues();  
        values.put("date", dayData.getStringDate());  
        values.put("balance", dayData.getBalance()); 
        
        return values;
	}
	
	public static ContentValues getItemDataContentValues(ItemData itemData, int sequence){
		ContentValues values = new ContentValues();
		values.put("id", itemData.getId());
        values.put("name", itemData.getName());
        values.put("price", itemData.getPrice());
		values.put("date", itemData.getStringDate());
		values.put("number", itemData.getNumber());
        values.put("category", itemData.getCategory());
        values.put("sequence", sequence);
		values.put("wallet_id", itemData.getWalletId());
		values.put("reverse_item_id", itemData.getReverseItemId());
        
        return values;
	}

	/**
	 * 日付以外指定しない。新規追加時に使用する
	 */
	public static ContentValues getItemDataContentValues(Calendar date){
		ContentValues values = new ContentValues();
		values.put("date", DateChanger.ChangeToString(date));

		// 追加日時のレコード数から最後尾番号を取得
		long recodeCount = DatabaseUtils.queryNumEntries(db, ITEM_TABLE_NAME,"date = '?'",new String[]{ DateChanger.ChangeToString(date) });
		values.put("sequence", recodeCount);

		return values;
	}


	public static void insertDayData(DayData dayData){
		long recodeCount = DatabaseUtils.queryNumEntries(db, DAY_TABLE_NAME, 
				"date = '" + dayData.getStringDate() +"'");
		
		if(recodeCount == 0){
			db.insert(DAY_TABLE_NAME, null, getDayDataContentValues(dayData));
		}
	}
	
	public static void updateDayData(DayData dayData){
		db.update(DAY_TABLE_NAME, getDayDataContentValues(dayData), 
				"date = '" + dayData.getStringDate() + "'", null);
	}
	
	public static void deleteDayData(String deletedDate){
		db.delete(DAY_TABLE_NAME, "date == '" + deletedDate + "'", null);
		
		deleteItemDataByDate(deletedDate);
	}
	
	public static void insertItemData(ItemData itemData, int sequence){
		db.insert(ITEM_TABLE_NAME, null, getItemDataContentValues(itemData, sequence));

		Log.d("insertItemData", itemData.getName() + " is inserted where sequence = " + sequence);
	}

	public static ItemData addItemData(Calendar date){
		db.insert(ITEM_TABLE_NAME, null, getItemDataContentValues(date));

		return loadNewestItemData();
	}
	
	public static void updateItemData(ItemData itemData, int sequence){
		db.update(ITEM_TABLE_NAME, getItemDataContentValues(itemData, sequence), 
				"id = '" + itemData.getId() + "'" , null);
	}
	
	public static void deleteItemData(ItemData deletedData){
		db.delete(ITEM_TABLE_NAME, "id = '" + deletedData.getId() + "'" , null);
		
		updateItemDataOrder(deletedData.getStringDate());
	}
	
	public static void deleteItemDataByDate(String deletedDate){
		db.delete(ITEM_TABLE_NAME, "date = '" + deletedDate + "'", null);
	}

	// DB内のアイテムの並びを整理する
	public static void updateItemDataOrder(String date){
		Cursor c = db.query(ITEM_TABLE_NAME, 
				new String[] {"date", "sequence"},
				"date = '" + date + "'", null, null, null, "sequence ASC");
		
		boolean isEOF = c.moveToFirst();
		int sequenceCounter = 0;
		String where;
		while (isEOF) {
			ContentValues values = new ContentValues();
			values.put("sequence", sequenceCounter);   
		
	        where = "date = " + c.getString(0) + "' AND sequence = " + c.getInt(1) + "'";
			db.update(ITEM_TABLE_NAME, values, where, null);
			
			sequenceCounter++;
		    isEOF = c.moveToNext();
		}
		c.close();

		//FIXME:なんでここにdb.closeが????????
		//db.close();
		
	}

	/**
	 *  単一の日データを日付から取得する
	 */
	public static DayData loadDayData(Calendar date){
		List<DayData> dList = new ArrayList<DayData>();

		Cursor c = db.query(DAY_TABLE_NAME, new String[] {"date", "balance"},
				"date = '" + DateChanger.ChangeToString(date), null, null, null, null);

		boolean isEOF = c.moveToFirst();
		while (isEOF) {
			dList.add(new DayData(DateChanger.ChangeToCalendar(c.getString(0)), c.getInt(1)));
			isEOF = c.moveToNext();
		}
		c.close();

		if(dList.size() == 0) return null;
		else if(dList.size() == 1) return dList.get(0);
		else throw new IllegalArgumentException("date conflict");
	}


	public static DayList loadAllDayList(){
		DayList dayList = new DayList();

		Cursor c = db.query(DAY_TABLE_NAME, new String[] {"date", "balance"},
				null, null, null, null, "date ASC");
		
		boolean isEOF = c.moveToFirst();
		while (isEOF) {
			dayList.addData(new DayData(DateChanger.ChangeToCalendar(c.getString(0)), c.getInt(1)));
		    isEOF = c.moveToNext();
		}
		c.close();
		
		return dayList;
	}

    /**
     * 一定期間のアイテムを持ったDayListを取得する
     */
	public static DayList loadDayList(Calendar startDate, Calendar endDate){
		DayList dayList = new DayList();

		Cursor c = db.query(DAY_TABLE_NAME, new String[] {"date", "balance"},
				"BETWEEN date '" + DateChanger.ChangeToString(startDate) + "' AND '" + DateChanger.ChangeToString(endDate)
				, null, null, null, "date ASC");

		boolean isEOF = c.moveToFirst();
		while (isEOF) {
			dayList.addData(new DayData(DateChanger.ChangeToCalendar(c.getString(0)), c.getInt(1)));
			isEOF = c.moveToNext();
		}
		c.close();

		for(int i = 0; i < dayList.getListSize(); i++){
			Calendar date = dayList.getData(i).getDate();
			dayList.setItemList(date, loadItemData(date));
		}

		return dayList;
	}

	/**
	 *  日付ごとのアイテムのリストを取得する
	 */
	public static List<ItemData> loadItemData(Calendar date){
		List<ItemData> itemList = new ArrayList<ItemData>();

		Cursor c = db.query(ITEM_TABLE_NAME, 
				new String[] {"id", "name", "price", "date", "number", "category", "sequence", "wallet_id", "reverse_item_id"},
				"date = '" + DateChanger.ChangeToString(date) + "'", null, null, null, "sequence ASC");
		
		boolean isEOF = c.moveToFirst();
		while (isEOF) {
			itemList.add(new ItemData(c.getInt(0), c.getString(1), c.getInt(2), c.getString(3), c.getInt(4),
					c.getInt(5), c.getInt(7), c.getInt(8)));
			isEOF = c.moveToNext();
		}
		c.close();
		
		return itemList;
	}

	/**
	 *  単一のアイテムをIDから取得する
	 */
	public static ItemData loadItemData(int id){
		List<ItemData> itemList = new ArrayList<ItemData>();

		Cursor c = db.query(ITEM_TABLE_NAME,
				new String[] {"id", "name", "price", "date", "number", "category", "sequence", "wallet_id", "reverse_item_id"},
				"id = " + id , null, null, null, null);

		boolean isEOF = c.moveToFirst();
		while (isEOF) {
			itemList.add(new ItemData(c.getInt(0), c.getString(1), c.getInt(2), c.getString(3), c.getInt(4),
					c.getInt(5), c.getInt(7), c.getInt(8)));
			isEOF = c.moveToNext();
		}
		c.close();

		if(itemList.size() == 0) return null;
		else if(itemList.size() == 1) return itemList.get(0);
		else throw new IllegalArgumentException("id conflict");
	}

	/**
	 *  最新の(IDが最大の)アイテムを取得する
	 */
	public static ItemData loadNewestItemData(){
		return loadItemData(getMaxItemId());
	}
	
	public static void insertDayList(DayList dayList){
		db.delete("day_table", null, null);
		
		for(int i = 0; i < dayList.getListSize(); i++){
			DayData day = dayList.getData(i);
			ContentValues values = new ContentValues();
			values.put("date", day.getStringDate());
			values.put("balance", day.getBalance());
			db.insert(DAY_TABLE_NAME, null, values);
		}
	}

	/**
	 * アイテムIDからアイテムを探索してデータを更新する
	 * アイテムIDがない場合はアイテムを追加する
	 * @param dayData
     */
	public static void updateItemList(DayData dayData){
		for(int i = 0; i < dayData.getItemSize(); i++){
			ItemData item = dayData.getItemList().get(i);
			ContentValues values = new ContentValues();
			values.put("id", item.getId());
			values.put("name", item.getName());
			values.put("price", item.getPrice());
			values.put("date", item.getStringDate());
			values.put("number", item.getNumber());
			values.put("category", item.getCategory());
			values.put("sequence", i);
			values.put("wallet_id", item.getWalletId());
			values.put("reverse_item_id", item.getReverseItemId());

			long recodeCount = DatabaseUtils.queryNumEntries(db, ITEM_TABLE_NAME, "id = " + item.getId());
			Log.d("updateItemList", "id = "+item.getId()+": count = "+recodeCount);
			if(recodeCount == 0){
				db.insert(ITEM_TABLE_NAME, null, values);
			}
			else if(recodeCount == 1){
				db.update(ITEM_TABLE_NAME, values, "id = " + item.getId(), null);
			}
			else {
				Log.e("updateItemList", "Error");
			}
		}
	}
	
	public static void insertItemList(DayData dayData){
		for(int i = 0; i < dayData.getItemSize(); i++){
			ItemData item = dayData.getItemList().get(i);
			ContentValues values = new ContentValues();
			values.put("id", item.getId());
			values.put("name", item.getName());
			values.put("price", item.getPrice());
			values.put("date", item.getStringDate());
			values.put("number", item.getNumber());
			values.put("category", item.getCategory());
			values.put("sequence", i);
			values.put("wallet_id", item.getWalletId());
			values.put("reverse_item_id", item.getReverseItemId());
			db.insert(ITEM_TABLE_NAME, null, values);
		}
	}

	/**
	 * アイテムに現在つけられているIDの最大値を返す
	 *
	 * @return seq 現在アイテムに使用されているIDの最大値
     */
	public static int getMaxItemId(){
		Cursor c = db.query("sqlite_sequence", new String[] {"name", "seq"}, "name = '" + ITEM_TABLE_NAME + "'", null, null, null, null);

		int seq = -1;
		boolean isEOF = c.moveToFirst();
		while (isEOF) {
			seq = c.getInt(1);
			isEOF = c.moveToNext();
		}
		c.close();

		return seq;
	}
}
