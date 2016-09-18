package jp.shimi.saifu;

import java.util.Calendar;

//import jp.shimi.saifu.dialog.EditItemDialog;
import jp.shimi.saifu.dialog.EditItemDialogFragment;
import jp.shimi.saifu.setting.Preferences;
import jp.shimi.saufu.R;

import android.os.Bundle;
import android.DB.MySQLiteAdapter;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;

// TODO:アイテムの追加・変更時にデータベースにすぐ登録させるようにする
// TODO:アイテムリストの取得をすべてデータベースから直接行う
// TODO:DB更新後に、DBの変更をViewに反映させる。（特に日付をまたいだアイテムの移動）

// FIXME:アイテム編集時に、日付が変わった際に変更が反映されない。
// FIXME:アイテム削除時に、変更がDBに反映されない。

public class MainActivity extends FragmentActivity
implements OnClickListener, DayAdapter.DayAdapterListener,
ItemAdapter.MoveItemListener,
EditItemDialogFragment.EditItemDialogListener{

	private Button button1;
	private ListView listView;
	private DayList lDay = new DayList(); // 表示専用なので、DBから取得するとき以外は追加・変更・削除は厳禁　TODO

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);

		loadDayDataFromDB();
		loadItemDataFromDB();
		
		lDay.UpdateBalance(0);
		
		button1 = (Button)findViewById(R.id.addButton1);
        button1.setOnClickListener(this);
        
        listView = (ListView)findViewById(R.id.diaryListView);

        // 初期残金設定ダイアログ
        if(lDay.getListSize() == 0){
			MySQLiteAdapter dbAdapter = new MySQLiteAdapter(this);
			int nextId = dbAdapter.getMaxItemId() + 1;

			createItemEditDialog(new ItemData(nextId, "初期残金", 0, Calendar.getInstance(), 1, 1, -1, -1), -1);
        }
        
        // リストビューの表示
        updateListViewAndScroll(lDay.getListSize() - 1);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.diary, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		switch(item.getItemId()){
			case R.id.action_settings:
				Intent intent = new Intent(this, Preferences.class);
				startActivity(intent);
				return true;
			/*case R.id.dayjump_calendar:
				CalendarDialogFragment newFragment;
				newFragment = CalendarDialogFragment.newInstance();
				newFragment.show(getFragmentManager(), "calendar_dialog");
				return true;*/
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * アイテム追加ボタンクリック時処理
 	 */
	@Override
	public void onClick(View v){
		// アイテムを当日の日付で新規作成
		createItemCreateDialog(Calendar.getInstance());
	}
	
	private void updateListView(){
		DayAdapter adapter = new DayAdapter(MainActivity.this, 0, lDay.getList());
		adapter.setDayAdapterListener(this);
		adapter.setMoveItemListener(this);
		listView.setAdapter(adapter);
	}

	/**
	 * 最新の(listSize)件分を表示する。
	 * listSize:表示する件数
	 */
	private void updateListView(int listSize){
		int startPos = (lDay.getListSize() <= listSize)? 0 : lDay.getListSize() - listSize - 1;
		int endPos = lDay.getListSize() - 1;
		updateListView(startPos, endPos);
	}

	private void updateListView(int startPos, int endPos){
		DayAdapter adapter = new DayAdapter(MainActivity.this, 0, lDay.getList().subList(startPos, endPos));
		adapter.setDayAdapterListener(this);
		adapter.setMoveItemListener(this);
		listView.setAdapter(adapter);
	}

	/**
	 * listViewの表示をlDayの状態と同期し、positionの項目までスクロールする
	 */
	private void updateListViewAndScroll(int position){
		updateListView();
			
		if(position > listView.getCount() - 1){
			listView.setSelection(listView.getCount() - 1);
		}
		else if(position < 0){
			listView.setSelection(0);
		}
		else{
			listView.setSelection(position);
		}
	}

	/**
	 * listViewの表示をlDayの状態と同期し、保持していた元の位置までスクロールする
	 */
	private void updateListViewWithNoScroll(){
		if(listView.getChildCount() > 0){
			int position = listView.getFirstVisiblePosition();
			int yOffset = listView.getChildAt(0).getTop();
		
			updateListView();
		
			listView.setSelectionFromTop(position, yOffset);
		}
	}

	/**
	 * アイテム編集ダイアログを生成する
 	 */
	private void createItemEditDialog(ItemData editItem, int editPosition){
		EditItemDialogFragment newFragment;
		newFragment = EditItemDialogFragment.newInstance(editItem, editPosition);
		newFragment.setEditItemDialogListener(this);
		//newFragment.setCancelable(false);
		newFragment.show(getFragmentManager(), "edit_item_dialog");
	}

	/**
	 * アイテム新規作成ダイアログを生成する
 	 */
	private void createItemCreateDialog(Calendar date){
		EditItemDialogFragment newFragment;
		newFragment = EditItemDialogFragment.newInstance(date, this);
		newFragment.setEditItemDialogListener(this);
		//newFragment.setCancelable(false);
		newFragment.show(getFragmentManager(), "edit_item_dialog");
	}
	
	/**
	 * EditItemDialogFragmentで返される値を受け取る
	 * @param itemData 変更後のデータ
	 * @param initDate 変更前の日付
	 * @param editPosition 
	 */
	@Override
	public void onReturnEditedItemData(ItemData itemData, Calendar initDate, int editPosition){
		int d = lDay.getDataPositionByDate(itemData.getDate());
		if(d < 0){ 
			DayData newDay = new DayData(itemData.getDate(), 0);
			lDay.addDataByDate(newDay);
			insertDayDataToDB(newDay);
			Log.d("insertDayData", newDay.getStringDate()+" Passed");
		}

		if (editPosition >= 0) {
			// 編集モード
			if (itemData.getDate().equals(initDate)) {
				//lDay.SetItemData(initDate, itemData, editPosition);
				// 削除処理は各ArrayAdapterで行うので、追加のみ行う
				if (editPosition + 1 == lDay.getListSize()) {
					lDay.updateItemData(itemData);
				} else {
					lDay.updateItemData(itemData);
				}
			} else {
				lDay.updateItemData(itemData);
			}
		} else {
			lDay.addItemData(itemData.getDate(), itemData);
		}

		// 項目が編集された日にスクロールする
		updateListViewAndScroll(lDay.getDataPositionByDate(itemData.getDate()));

		if (!initDate.equals(itemData.getDate()) && d >= 0) {
			updateDayDataToDB(lDay.getData(initDate));
		}
		updateDayDataToDB(lDay.getData(itemData.getDate()));
	}
	
	@Override
	public void onDayDeleted(Calendar deletedDate) {
		lDay.UpdateBalance(lDay.getNextDate(deletedDate));
		
		//　削除された日の前日にスクロールする
		updateListViewAndScroll(lDay.getDataPositionByDate(lDay.getBeforeDate(deletedDate)));
		
		deleteDayDataFromDB(deletedDate);
	}	
	
	@Override
	public void onDayItemDeleted(Calendar deletedDate) {
		lDay.CheckItemListSize();
		lDay.UpdateBalance(deletedDate);

		updateListViewWithNoScroll();

		int position = lDay.getDataPositionByDate(deletedDate);
		if(position == -1){
			deleteDayDataFromDB(deletedDate);
		}
		else{
			updateDayDataToDB(lDay.getData(deletedDate));
		}
	}
	
	@Override
	public void upItem(ItemData item, int itemPosition) {
		lDay.getData(item.getDate()).upItemPosition(itemPosition);
		updateListViewWithNoScroll();
		updateDayDataToDB(lDay.getData(item.getDate()));
	}

	@Override
	public void downItem(ItemData item, int itemPosition) {
		lDay.getData(item.getDate()).downItemPosition(itemPosition);
		updateListViewWithNoScroll();
		updateDayDataToDB(lDay.getData(item.getDate()));
	}

	public void loadDayDataFromDB(){
		MySQLiteAdapter dbAdapter = new MySQLiteAdapter(this);
		lDay = dbAdapter.loadAllDayList();
	}
	
	public void loadItemDataFromDB(){
		MySQLiteAdapter dbAdapter = new MySQLiteAdapter(this);

		for(int i = 0; i < lDay.getListSize(); i++){
			Calendar date = lDay.getData(i).getDate();
			lDay.setItemList(date, dbAdapter.loadItemData(date));
		}
	}
	
	public void insertDayDataToDB(DayData dayData){
		MySQLiteAdapter dbAdapter = new MySQLiteAdapter(this);

		dbAdapter.insertDayData(dayData);
	}
	
	public void insertItemDataToDB(ItemData itemData, int sequence){
		MySQLiteAdapter dbAdapter = new MySQLiteAdapter(this);

		dbAdapter.insertItemData(itemData, sequence);
	}

	/**
	 * 渡した日データのアイテムリストでアイテムテーブル内の指定日のデータを再初期化する。
 	 */
	public void updateDayDataToDB(DayData dayData){
		MySQLiteAdapter dbAdapter = new MySQLiteAdapter(this);

		Log.d("updateDayDataToDB", dayData.getStringDate());
		for(ItemData i : dayData.getItemList())
			Log.d("updateDayDataToDB", i.getId() +":"+ i.getName());
		//FIXME:Updateはリストの状態そのままに更新するわけではない。削除処理は検知されていない。
		dbAdapter.updateItemList(dayData);
	}
	
	public void deleteDayDataFromDB(Calendar date){
		MySQLiteAdapter dbAdapter = new MySQLiteAdapter(this);

		dbAdapter.deleteDayData(DateChanger.ChangeToString(date));
	}
}
