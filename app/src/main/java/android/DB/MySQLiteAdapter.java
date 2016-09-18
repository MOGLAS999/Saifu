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
import android.util.Log;

/**
 * MySQLiteOpenHelperのラッパークラス
 * DBに対してSQLの実行を行う
 */
public class MySQLiteAdapter {

	private MySQLiteOpenHelper DBHelper;
	private SQLiteDatabase db;
	
	public MySQLiteAdapter(Context context){
		this.DBHelper = MySQLiteOpenHelper.getInstance(context);
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
	public ContentValues getItemDataContentValues(Calendar date){
		ContentValues values = new ContentValues();
		values.put("date", DateChanger.ChangeToString(date));

		// 追加日時のレコード数から最後尾番号を取得
		long recodeCount = DatabaseUtils.queryNumEntries(db, DBDefinition.ITEM_TABLE_NAME,"date = '?'",new String[]{ DateChanger.ChangeToString(date) });
		values.put("sequence", recodeCount);

		return values;
	}


	public void insertDayData(DayData dayData){
		long recodeCount = DatabaseUtils.queryNumEntries(db, DBDefinition.DAY_TABLE_NAME,
				"date = '" + dayData.getStringDate() +"'");

		if(recodeCount == 0){
			db.insert(DBDefinition.DAY_TABLE_NAME, null, getDayDataContentValues(dayData));
		}
	}

	public void updateDayData(DayData dayData){
		db.update(DBDefinition.DAY_TABLE_NAME, getDayDataContentValues(dayData),
				"date = '" + dayData.getStringDate() + "'", null);
	}

	public void deleteDayData(String deletedDate){
		db.delete(DBDefinition.DAY_TABLE_NAME, "date == '" + deletedDate + "'", null);

		deleteItemDataByDate(deletedDate);
	}

	public void insertItemData(ItemData itemData, int sequence){
		db.insert(DBDefinition.ITEM_TABLE_NAME, null, getItemDataContentValues(itemData, sequence));

		Log.d("insertItemData", itemData.getName() + " is inserted where sequence = " + sequence);
	}

	public ItemData addItemData(Calendar date){
		db.insert(DBDefinition.ITEM_TABLE_NAME, null, getItemDataContentValues(date));

		return loadNewestItemData();
	}

	public void updateItemData(ItemData itemData, int sequence){
		db.update(DBDefinition.ITEM_TABLE_NAME, getItemDataContentValues(itemData, sequence),
				"id = '" + itemData.getId() + "'" , null);
	}
	
	public void deleteItemData(ItemData deletedData){
		db.delete(DBDefinition.ITEM_TABLE_NAME, "id = '" + deletedData.getId() + "'" , null);
		
		updateItemDataOrder(deletedData.getStringDate());
	}
	
	public void deleteItemDataByDate(String deletedDate){
		db.delete(DBDefinition.ITEM_TABLE_NAME, "date = '" + deletedDate + "'", null);
	}

	// DB内のアイテムの並びを整理する
	public void updateItemDataOrder(String date){
		Cursor c = db.query(DBDefinition.ITEM_TABLE_NAME, 
				new String[] {"date", "sequence"},
				"date = '" + date + "'", null, null, null, "sequence ASC");
		
		boolean isEOF = c.moveToFirst();
		int sequenceCounter = 0;
		String where;
		while (isEOF) {
			ContentValues values = new ContentValues();
			values.put("sequence", sequenceCounter);   
		
	        where = "date = " + c.getString(0) + "' AND sequence = " + c.getInt(1) + "'";
			db.update(DBDefinition.ITEM_TABLE_NAME, values, where, null);
			
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
	public DayData loadDayData(Calendar date){
		List<DayData> dList = new ArrayList<DayData>();

		Cursor c = db.query(DBDefinition.DAY_TABLE_NAME, new String[] {"date", "balance"},
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


	public DayList loadAllDayList(){
		DayList dayList = new DayList();

		Cursor c = db.query(DBDefinition.DAY_TABLE_NAME, new String[] {"date", "balance"},
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
	public DayList loadDayList(Calendar startDate, Calendar endDate){
		DayList dayList = new DayList();

		Cursor c = db.query(DBDefinition.DAY_TABLE_NAME, new String[] {"date", "balance"},
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
	public List<ItemData> loadItemData(Calendar date){
		List<ItemData> itemList = new ArrayList<ItemData>();

		Cursor c = db.query(DBDefinition.ITEM_TABLE_NAME, 
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
	public ItemData loadItemData(int id){
		List<ItemData> itemList = new ArrayList<ItemData>();

		Cursor c = db.query(DBDefinition.ITEM_TABLE_NAME,
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
	public ItemData loadNewestItemData(){
		return loadItemData(getMaxItemId());
	}
	
	public void insertDayList(DayList dayList){
		db.delete("DBDefinition.DAY_TABLE", null, null);
		
		for(int i = 0; i < dayList.getListSize(); i++){
			DayData day = dayList.getData(i);
			ContentValues values = new ContentValues();
			values.put("date", day.getStringDate());
			values.put("balance", day.getBalance());
			db.insert(DBDefinition.DAY_TABLE_NAME, null, values);
		}
	}

	/**
	 * アイテムIDからアイテムを探索してデータを更新する
	 * アイテムIDがない場合はアイテムを追加する
	 * @param dayData
     */
	public void updateItemList(DayData dayData){
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

			long recodeCount = DatabaseUtils.queryNumEntries(db, DBDefinition.ITEM_TABLE_NAME, "id = " + item.getId());
			Log.d("updateItemList", "id = "+item.getId()+": count = "+recodeCount);
			if(recodeCount == 0){
				db.insert(DBDefinition.ITEM_TABLE_NAME, null, values);
			}
			else if(recodeCount == 1){
				db.update(DBDefinition.ITEM_TABLE_NAME, values, "id = " + item.getId(), null);
			}
			else {
				Log.e("updateItemList", "Error");
			}
		}
	}
	
	public void insertItemList(DayData dayData){
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
			db.insert(DBDefinition.ITEM_TABLE_NAME, null, values);
		}
	}

	/**
	 * アイテムに現在つけられているIDの最大値を返す
	 *
	 * @return seq 現在アイテムに使用されているIDの最大値
     */
	public int getMaxItemId(){
		Cursor c = db.query("sqlite_sequence", new String[] {"name", "seq"}, "name = '" + DBDefinition.ITEM_TABLE_NAME + "'", null, null, null, null);

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
