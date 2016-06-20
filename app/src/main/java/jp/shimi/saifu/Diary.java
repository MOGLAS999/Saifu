package jp.shimi.saifu;

import java.util.Calendar;

import jp.shimi.saifu.dialog.CheckNameDialogFragment;
import jp.shimi.saifu.dialog.EditItemDialog;
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

public class Diary extends FragmentActivity 
implements OnClickListener, DayAdapter.DayAdapterListener,
ItemAdapter.MoveItemListener, CheckNameDialogFragment.ClickedNamePositiveButtonListener{
	private Button button1;
	private ListView listView;
	private DayList lDay = new DayList();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.diary);
		
		LoaddayDataFromDB();
		LoadItemDataFromDB();
		
		lDay.UpdateBalance(0);
		
		button1 = (Button)findViewById(R.id.addButton1);
        button1.setOnClickListener(this);
        
        listView = (ListView)findViewById(R.id.diaryListView);
        
        // 初期残金設定ダイアログ
        if(lDay.getListSize() == 0){
			// IDを与える
        	EditItemDialog dialog = new EditItemDialog(this, new ItemData(0, "初期残金", 0, Calendar.getInstance(), 1, 1, -1, -1), 0);
			dialog.CreateDialog();
        }
        
        // リストビューの表示
        UpdateListViewAndScroll(lDay.getListSize() - 1);
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
	
	@Override
	public void onClick(View v){
		EditItemDialog dialog = new EditItemDialog(this, Calendar.getInstance());
		dialog.CreateDialog();
	}	
	
	private void UpdateListView(){
		DayAdapter adapter = new DayAdapter(Diary.this, 0, lDay.getList());
		adapter.setDayAdapterListener(this);
		adapter.setMoveItemListener(this);
		listView.setAdapter(adapter);
	}

	/*
	* 最新の(listSize)件分を表示する。
	* listSize:表示する件数
	**/
	private void UpdateListView(int listSize){
		int startPos = (lDay.getListSize() <= listSize)? 0 : lDay.getListSize() - listSize - 1;
		int endPos = lDay.getListSize() - 1;
		UpdateListView(startPos, endPos);
	}

	private void UpdateListView(int startPos, int endPos){
		DayAdapter adapter = new DayAdapter(Diary.this, 0, lDay.getList().subList(startPos, endPos));
		adapter.setDayAdapterListener(this);
		adapter.setMoveItemListener(this);
		listView.setAdapter(adapter);
	}
	
	//　listViewの表示をlDayの状態と同期し、positionの項目までスクロールする
	private void UpdateListViewAndScroll(int position){
		UpdateListView();
			
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
	
	//　listViewの表示をlDayの状態と同期し、保持していた元の位置までスクロールする
	private void UpdateListViewWithNoScroll(){
		if(listView.getChildCount() > 0){
			int position = listView.getFirstVisiblePosition();
			int yOffset = listView.getChildAt(0).getTop();
		
			UpdateListView();
		
			listView.setSelectionFromTop(position, yOffset);
		}
	}
	
	/**
	 * EditItemDialogで返される値を受け取る
	 * @param itemData 変更後のデータ
	 * @param initDate 変更前の日付
	 * @param editPosition 
	 */
	public void onReturnValue(ItemData itemData, Calendar initDate, int editPosition){
		int d = lDay.getDataPositionByDate(itemData.getDate());
		if(d < 0){ 
			DayData newDay = new DayData(itemData.getDate(), 0);
			lDay.addDataByDate(newDay);
			InsertDayDataToDB(newDay);
			Log.d("InsertDayData", newDay.getStringDate()+" Passed");
		}
		
		/*if(lDay.ItemIsExist(itemData.getDate(), itemData.getItem())){
			String title = "警告";
			String text = itemData.getStringDate() + "には既に" + itemData.getItem() + "が存在します。\n"
				+ "項目名を再編集してください。";
			
			CheckNameDialogFragment newFragment;
			newFragment = CheckNameDialogFragment.newInstance(title, text, itemData, editPosition);
			newFragment.setClickedNamePositiveButtonListener(this);
			newFragment.show(getFragmentManager(), "name_check_dialog");
		}*/
		//else{
			if(editPosition >= 0){
				// 編集モード
				if(itemData.getDate().equals(initDate)){
					//lDay.SetItemData(initDate, itemData, editPosition);
					// 削除処理は各ArrayAdapterで行うので、追加のみ行う
					if(editPosition + 1 == lDay.getListSize()){
						lDay.addItemData(itemData.getDate(), itemData);
					}else{
						lDay.addItemData(itemData.getDate(), itemData, editPosition+1);
					}
				}else{
					//lDay.RemoveItemData(initDate, editPosition);
					lDay.addItemData(itemData.getDate(), itemData);
				}
			}else{
				lDay.addItemData(itemData.getDate(), itemData);
			}
	
			// 項目が編集された日にスクロールする
			UpdateListViewAndScroll(lDay.getDataPositionByDate(itemData.getDate()));
		
			if(!initDate.equals(itemData.getDate()) && d >= 0){
				UpdateDayDataToDB(lDay.getData(initDate));
			}
			UpdateDayDataToDB(lDay.getData(itemData.getDate()));
		//}
	}
	
	@Override
	public void ClickedNamePositiveButton(ItemData itemData, int editPosition) {
		EditItemDialog dialog = new EditItemDialog(this, itemData, editPosition);
		dialog.CreateDialog();
	}
	
	@Override
	public void onDayDeleted(Calendar deletedDate) {
		lDay.UpdateBalance(lDay.getNextDate(deletedDate));
		
		//　削除された日の前日にスクロールする
		UpdateListViewAndScroll(lDay.getDataPositionByDate(lDay.getBeforeDate(deletedDate)));
		
		DeleteDayDataFromDB(deletedDate);
	}	
	
	@Override
	public void onDayItemDeleted(Calendar deletedDate) {
		lDay.CheckItemListSize();
		lDay.UpdateBalance(deletedDate);

		UpdateListViewWithNoScroll();
		
		int position = lDay.getDataPositionByDate(deletedDate);
		if(position == -1){
			DeleteDayDataFromDB(deletedDate);
		}
		else{
			UpdateDayDataToDB(lDay.getData(deletedDate));
		}
	}
	
	@Override
	public void upItem(ItemData item, int itemPosition) {
		lDay.getData(item.getDate()).upItemPosition(itemPosition);
		UpdateListViewWithNoScroll();
		UpdateDayDataToDB(lDay.getData(item.getDate()));
	}

	@Override
	public void downItem(ItemData item, int itemPosition) {
		lDay.getData(item.getDate()).downItemPosition(itemPosition);
		UpdateListViewWithNoScroll();
		UpdateDayDataToDB(lDay.getData(item.getDate()));
	}
	
	/*public boolean ItemIsExistInDay(Calendar date, String name){
		return lDay.ItemIsExist(date, name);
	}*/
	
	public void LoaddayDataFromDB(){
		MySQLiteAdapter dbAdapter = new MySQLiteAdapter(this);
		lDay = dbAdapter.loadDayData();
	}
	
	public void LoadItemDataFromDB(){		
		MySQLiteAdapter dbAdapter = new MySQLiteAdapter(this);
		
		for(int i = 0; i < lDay.getListSize(); i++){
			Calendar date = lDay.getData(i).getDate();
			lDay.setItemList(date, dbAdapter.loadItemData(date));
		}
	}
	
	public void InsertDayDataToDB(DayData dayData){
		MySQLiteAdapter dbAdapter = new MySQLiteAdapter(this);
		
		dbAdapter.insertDayData(dayData);
	}
	
	public void InsertItemDataToDB(ItemData itemData, int sequence){
		MySQLiteAdapter dbAdapter = new MySQLiteAdapter(this);
		
		dbAdapter.insertItemData(itemData, sequence);
	}
	
	// 渡した日データのアイテムリストでアイテムテーブル内の指定日のデータを再初期化する。
	public void UpdateDayDataToDB(DayData dayData){
		MySQLiteAdapter dbAdapter = new MySQLiteAdapter(this);
		
		Log.d("UpdateDayDataToDB", dayData.getStringDate());
		DeleteDayDataFromDB(dayData.getDate());
		InsertDayDataToDB(dayData);
		dbAdapter.insertItemList(dayData);
	}
	
	public void DeleteDayDataFromDB(Calendar date){
		MySQLiteAdapter dbAdapter = new MySQLiteAdapter(this);
		
		dbAdapter.deleteDayData(DateChanger.ChangeToString(date));
	}
	
	public void DeleteItemDataFromDB(ItemData itemData, int sequence){
		MySQLiteAdapter dbAdapter = new MySQLiteAdapter(this);
		
		dbAdapter.deleteItemData(DateChanger.ChangeToString(itemData.getDate()), sequence);
	}
	
	public void ReinitializationToDB(){
		MySQLiteAdapter dbAdapter = new MySQLiteAdapter(this);
		
		dbAdapter.saveDayList(lDay);
	}
}
