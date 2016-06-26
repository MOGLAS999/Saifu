package jp.shimi.saifu.dialog;

import java.util.Calendar;

import jp.shimi.saifu.DateChanger;
import jp.shimi.saifu.ItemData;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

public class EditItemDialog implements DialogListener{
	ItemData initItemData = new ItemData();
	int editPosition;
	Context context;

	// TODO:新規ITEMを作成する場合は、最新IDを与えるべき
	public EditItemDialog(Context context, Calendar initDate){
		this(context, new ItemData().setDate(initDate), -1);
	}
	
	/*public EditItemDialog(Context context, Calendar initDate, int editPosition,
			String itemName, int itemPrice, int itemNumber, int itemCategory){
		this.context = context;
		this.itemData.setDate(initDate);
		this.itemData.setName(initDate);
		this.itemData.setPrice(initDate);
		this.itemData.setDate(initDate);
		this.itemData.setDate(initDate);
		this.editPosition = editPosition;
		this.itemName = itemName;
		this.itemPrice = itemPrice;
		this.itemNumber = itemNumber;
		this.itemCategory = itemCategory;
	}*/
	
	public EditItemDialog(Context context, ItemData editItem, int editPosition){
		this.initItemData = editItem;
		this.context = context;
		this.editPosition = editPosition;
	}
	
	public void CreateDialog(){
		SubCreateDialog(null);  
	}
	
	public void CreateDialog(DialogListener listener){
		SubCreateDialog(listener);
	}
	
	private void SubCreateDialog(DialogListener listener){
		// アイテム編集ダイアログを生成     
		if(((Activity)context).getFragmentManager().findFragmentByTag("edit_item_dialog") == null){
			EditItemDialogFragment newFragment;
			if(this.editPosition == -1){
				newFragment = EditItemDialogFragment.newInstance(initItemData.getDate(), 0);
			}else{
				newFragment = EditItemDialogFragment.newInstance(initItemData, editPosition);
			}
			if(listener == null){
				newFragment.setDialogListener(EditItemDialog.this);
			}else{
				newFragment.setDialogListener(listener);
			}
			//newFragment.setCancelable(false);
			newFragment.show(((Activity)context).getFragmentManager(), "edit_item_dialog");
		}
	}

	@Override
	public void doPositiveClick() {
		// TODO 自動生成されたメソッド・スタブ
		
	}

	@Override
	public void doNegativeClick() {
		// TODO 自動生成されたメソッド・スタブ
		
	}
    	
}