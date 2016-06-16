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

public class MySQLiteAdapter {
	static final private String DB_NAME = "Saifu.db";
	static final private String DAY_TABLE_NAME = "day_table";
	static final private String ITEM_TABLE_NAME = "item_table";
	static final private String WALLET_TABLE_NAME = "wallet_table";
	static final private int DB_VERSION = 4;
	
	Context context;
	MySQLiteOpenHelper DBHelper;
	SQLiteDatabase db;
	
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
	
	private void destruction(){
		this.db.close();
	}
	
	private static class MySQLiteOpenHelper extends SQLiteOpenHelper{
		public MySQLiteOpenHelper(Context context) {
			super(context, DB_NAME, null, DB_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			final String COMMA = ",";

			db.execSQL("create table " + DAY_TABLE_NAME +"(" +
					"date text primary key" + COMMA +
					"balance integer" +
					//"difference integer not null" +
					");"
			);
			// TODO:DayはItemTableから生成できるので、後々不要になるかもしれない
			
			db.execSQL("create table " + ITEM_TABLE_NAME +"(" +
					"id integer primary key autoincrement"+ COMMA +
					"name text not null"+ COMMA +
					"price integer not null"+ COMMA +
					"date text not null"+ COMMA +
					"number integer"+ COMMA +
					"category integer"+ COMMA +
					"sequence integer not null"+ COMMA +
					"walletId integer not null"+ COMMA +
					"reverseItemId integer"+ // 反転関係にあるアイテム(例：現金で電子マネーをチャージするなど)
					");"
			);

			db.execSQL("create table " + WALLET_TABLE_NAME +"(" +
					"id integer primary key autoincrement"+ COMMA +
					"name text" + //COMMA +
					//"currency text"+
					");"
			);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			if(oldVersion < newVersion){
				 if(oldVersion <= 2){
					 db.execSQL("alter table " + ITEM_TABLE_NAME +
							 " add sequence integer default 0;"
							 );
				 }
			}
		}
	}
	
	public ContentValues getDayDataContentValues(DayData dayData){
		ContentValues values = new ContentValues();  
        values.put("date", dayData.GetStringDate());  
        values.put("balance", dayData.GetBalance()); 
        
        return values;
	}
	
	public ContentValues getItemDataContentValues(ItemData itemData, int sequence){
		ContentValues values = new ContentValues();
		values.put("id", itemData.GetId());
        values.put("name", itemData.GetName());
        values.put("price", itemData.GetPrice());
		values.put("date", itemData.GetStringDate());
		values.put("number", itemData.GetNumber());
        values.put("category", itemData.GetCategory());
        values.put("sequence", sequence);
		values.put("walletId", itemData.GetWalletId());
		values.put("reverseItemId", itemData.GetReverseItemId());
        
        return values;
	}
	
	public void insertDayData(DayData dayData){
		long recodeCount = DatabaseUtils.queryNumEntries(db, DAY_TABLE_NAME, 
				"date = '" + dayData.GetStringDate() +"'");
		
		if(recodeCount == 0){
			db.insert(DAY_TABLE_NAME, null, getDayDataContentValues(dayData));
		}
	}
	
	public void updateDayData(DayData dayData){
		db.update(DAY_TABLE_NAME, getDayDataContentValues(dayData), 
				"date = '" + dayData.GetStringDate() + "'", null);
	}
	
	public void deleteDayData(String deletedDate){
		db.delete(DAY_TABLE_NAME, "date == '" + deletedDate + "'", null);
		
		deleteItemDataByDate(deletedDate);
	}
	
	public void insertItemData(ItemData itemData, int order){		
		db.insert(ITEM_TABLE_NAME, null, getItemDataContentValues(itemData, order));
	}
	
	public void updateItemData(ItemData itemData, int sequence){
		db.update(ITEM_TABLE_NAME, getItemDataContentValues(itemData, sequence), 
				"date = '" + itemData.GetStringDate() + "'" +
						" AND sequence = " + Integer.toString(sequence), null);
	}
	
	public void deleteItemData(String deletedDate, int deleteSequence){
		db.delete(ITEM_TABLE_NAME, "date = '" + deletedDate + "'" 
				+ " AND sequence = " + Integer.toString(deleteSequence), null);
		
		updateItemDataOrder(deletedDate);
	}
	
	public void deleteItemDataByDate(String deletedDate){
		db.delete(ITEM_TABLE_NAME, "date = '" + deletedDate + "'", null);
	}
	
	public void updateItemDataOrder(String date){
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
			 
		db.close();
		
	}
	
	public DayList loadDayData(){
		DayList dayList = new DayList();

		Cursor c = db.query(DAY_TABLE_NAME, new String[] {"date", "balance"},
				null, null, null, null, "date ASC");
		
		boolean isEOF = c.moveToFirst();
		while (isEOF) {
			dayList.AddData(new DayData(DateChanger.ChangeToCalendar(c.getString(0)), c.getInt(1)));
		    isEOF = c.moveToNext();
		}
		c.close();
		
		return dayList;
	}
	
	public List<ItemData> loadItemData(Calendar date){
		List<ItemData> itemList = new ArrayList<ItemData>();

		Cursor c = db.query(ITEM_TABLE_NAME, 
				new String[] {"id", "name", "price", "date", "number", "category", "sequence", "walletId", "reverseItemId"},
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
	
	public void insertDayList(DayList dayList){
		db.delete("day_table", null, null);
		
		for(int i = 0; i < dayList.GetListSize(); i++){
			DayData day = dayList.GetData(i);
			ContentValues values = new ContentValues();
			values.put("date", day.GetStringDate());
			values.put("balance", day.GetBalance());
			db.insert(DAY_TABLE_NAME, null, values);
		}
	}
	
	public void insertItemList(DayData dayData){
		for(int i = 0; i < dayData.GetItemSize(); i++){
			ItemData item = dayData.GetItemList().get(i);
			ContentValues values = new ContentValues();
			values.put("id", item.GetId());
			values.put("name", item.GetName());
			values.put("price", item.GetPrice());
			values.put("date", item.GetStringDate());
			values.put("number", item.GetNumber());
			values.put("category", item.GetCategory());
			values.put("sequence", i);
			values.put("walletId", item.GetWalletId());
			values.put("reverseItemId", item.GetReverseItemId());
			db.insert(ITEM_TABLE_NAME, null, values);
		}
	}
	
	public void saveDayList(DayList dayList){
		db.execSQL("drop table " + DAY_TABLE_NAME);
		db.execSQL("drop table " + ITEM_TABLE_NAME);
		this.DBHelper.onCreate(db);
		
		insertDayList(dayList);
		
		for(int i = 0; i < dayList.GetListSize(); i++){
			insertItemList(dayList.GetData(i));
		}
	}
}
