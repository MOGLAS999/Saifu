package jp.shimi.saifu;

import java.util.Calendar;
import java.util.EventListener;
import java.util.List;

import jp.shimi.saifu.dialog.CheckDialogFragment;
import jp.shimi.saifu.dialog.DayMenuDialogFragment;
import jp.shimi.saifu.dialog.EditItemDialogFragment;
import jp.shimi.saufu.R;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.MeasureSpec;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

/**
 * 日リストを生成するアダプター
 */

public class DayAdapter extends ArrayAdapter<DayData>
		implements ItemAdapter.ItemRemoveListener,
		ItemAdapter.MoveItemListener{
	private LayoutInflater inflater;
	private Context context;
	private DayAdapterListener listener;
	private ItemAdapter.MoveItemListener moveItemListener = null;
	private static final double LATEST_BALANCE_FONTSIZE_RATE = 1.3;

	private class ViewHolder{
		TextView textDate;
		LinearLayout listItem;
		TextView textBalance;
		
		ViewHolder(View view){
			this.textDate = (TextView) view.findViewById(R.id.txtDate);
			this.listItem = (LinearLayout) view.findViewById(R.id.lstItem);
    		this.textBalance = (TextView) view.findViewById(R.id.txtBalance);
		}
	} 
	
    public DayAdapter(Context context, int textViewResourceId, List<DayData> objects) {
		super(context, textViewResourceId, objects);
		this.inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.context = context;
	}

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
    	ViewHolder holder;
    	
    	if(convertView == null){
    		convertView = inflater.inflate(R.layout.day, null);
    		holder = new ViewHolder(convertView);
    		convertView.setTag(holder);
    	}else{
    		holder = (ViewHolder)convertView.getTag();
    	}
    	
    	final DayData day = (DayData)getItem(position);
    	if(day != null){	        
    		holder.textDate.setText(day.getStringDate() + getWeekByDate(day.getDate()));
    		// 各日の日付部分がクリックされた時のイベント
    		holder.textDate.setOnClickListener(new View.OnClickListener() {
        		public void onClick(View view){            			
        			// 追加・削除ダイアログを生成
        			DayMenuDialog dialog = new DayMenuDialog(day, position);
        			dialog.CreateDialog();
        		}
			});
    		
    		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
    		
    		String initUnit = context.getResources().getString(R.string.initial_unit_string);
    		String unit = pref.getString("unit_string", initUnit);
    		
    		holder.textBalance.setText("残金    " + day.getStringBalance() + " " + unit);
    		
    		String initFontSize = context.getResources().getString(R.string.initial_font_size);
    		int textSize = Integer.parseInt(pref.getString("char_size", initFontSize));
    		holder.textDate.setTextSize(textSize);
    		if(position == getCount() - 1){
    			holder.textBalance.setTextSize(textSize*(float)LATEST_BALANCE_FONTSIZE_RATE);
    		}else{
    			holder.textBalance.setTextSize(textSize);
    		}
    		    	
    		ItemAdapter adapter = new ItemAdapter(context, 0, day.getItemList());
    		adapter.setItemRemoveListener(DayAdapter.this);
    		adapter.setMoveItemListener(DayAdapter.this);
    		
    		holder.listItem.removeAllViews();
    		for(int i = 0; i < adapter.getCount(); i++){		
    			holder.listItem.addView(adapter.getView(i, null, holder.listItem));
    		}
    	}
    	return convertView;
    }
    
    private class DayMenuDialog implements DayMenuDialogFragment.DayMenuDialogFragmentListener,
    CheckDialogFragment.CheckDialogFragmentListener{
    	DayData day;
    	int position;
    	
    	DayMenuDialog(DayData day, int position){
    		this.day = day;
    		this.position = position;
    	}
    	
    	public void CreateDialog(){
    		// 編集・削除ダイアログを生成        			       			
			DayMenuDialogFragment newFragment = 
					DayMenuDialogFragment.newInstance(day.getStringDate());
			newFragment.setDialogListener(DayMenuDialog.this);
			//newFragment.setCancelable(false);
			newFragment.show(((Activity)context).getFragmentManager(), "day_menu_dialog");
    	}
    	
    	@Override
    	public void doFirstClick() {
			// アイテムの新規作成ダイアログを生成
			EditItemDialogFragment newFragment;
			newFragment = EditItemDialogFragment.newInstance(day.getDate(), context);
			newFragment.setEditItemDialogListener((MainActivity)context);
			newFragment.show(((Activity)context).getFragmentManager(), "edit_item_dialog");
    	}

    	@Override
    	public void doSecondClick() {
    		//　削除確認ダイアログを表示
    		CheckDialogFragment newFragment;
    		newFragment = CheckDialogFragment.newInstance("警告", day.getStringDate()+"を削除しますか？");
    		newFragment.setCheckDialogFragmentListener(DayMenuDialog.this);
    		newFragment.show(((Activity)context).getFragmentManager(), "check_day_delete_dialog");
    	}

		@Override
		public void ClickedPositiveButton() {
			Calendar cal = day.getDate();
    		remove(day);   
    		listener.onDayDeleted(cal);
		}

		@Override
		public void ClickedNegativeButton() {}
    }
    
    /**
     * リストビューの高さを内容に応じて変更
     */
    public void SetListViewHeightBasedOnItem(ListView listView){
		ItemAdapter listAdapter = (ItemAdapter) listView.getAdapter();
		
		int totalHeight = 0;
		int desiredWidth = MeasureSpec.makeMeasureSpec(listView.getWidth(), MeasureSpec.AT_MOST);
		
		for(int i = 0; i < listAdapter.getCount(); i++){
			View listItem = listAdapter.getView(i, null, listView);
			listItem.measure(desiredWidth, MeasureSpec.UNSPECIFIED);
			totalHeight += listItem.getMeasuredHeight();
		}
		
		ViewGroup.LayoutParams params = listView.getLayoutParams();
		params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
		listView.setLayoutParams(params);
		listView.requestLayout();
	}
    
    public String getWeekByDate(Calendar cal){
    	switch(cal.get(Calendar.DAY_OF_WEEK)){
    		case Calendar.SUNDAY:    return "(日)";
    		case Calendar.MONDAY:    return "(月)";
    		case Calendar.TUESDAY:   return "(火)";
    		case Calendar.WEDNESDAY: return "(水)";
    		case Calendar.THURSDAY:  return "(木)";
    		case Calendar.FRIDAY:    return "(金)";
    		case Calendar.SATURDAY:  return "(土)";
    	}
    	
    	return null;
    }

	@Override
	public void removeItem(Calendar deletedDate) {
		listener.onDayItemDeleted(deletedDate);
		Log.d("removeItem", "passed");
	}

	@Override
	public void upItem(ItemData item, int itemPosition) {
		moveItemListener.upItem(item, itemPosition);		
	}

	@Override
	public void downItem(ItemData item, int itemPosition) {
		moveItemListener.downItem(item, itemPosition);		
	}


	public void setMoveItemListener(ItemAdapter.MoveItemListener listener){
		this.moveItemListener = listener;
	}

	public void removeMoveItemListener(){
		this.moveItemListener = null;
	}


	/**
	 * リスナーを追加
	 */
	public void setDayAdapterListener(DayAdapterListener listener){
		this.listener = listener;
	}

	/**
	 * リスナー削除
	 */
	public void removeDayAdapterListener(){
		this.listener = null;
	}

	public interface DayAdapterListener extends EventListener {
		/**
		 * 日データのアイテムが削除されたイベントを通知
		 */
		void onDayItemDeleted(Calendar deletedDate);

		/**
		 * 日データが削除されたイベントを通知
		 */
		void onDayDeleted(Calendar deletedDate);
	}
}