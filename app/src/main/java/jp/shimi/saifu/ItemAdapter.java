package jp.shimi.saifu;

import java.util.Calendar;
import java.util.EventListener;
import java.util.List;

import jp.shimi.saifu.dialog.CheckDialogFragment;
import jp.shimi.saifu.dialog.DialogListener;
import jp.shimi.saifu.dialog.EditItemDialogFragment;
import jp.shimi.saifu.dialog.ItemMenuDialogFragment;
import jp.shimi.saufu.R;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

/**
 * アイテムリストを生成するアダプター
 */

public class ItemAdapter extends ArrayAdapter<ItemData>{
	private LayoutInflater inflater;
	private Context context;
	private ItemRemoveListener itemRemoveListener = null;
	private MoveItemListener moveItemListener = null;
	private class ViewHolder{
		Button btnCategory;
		TextView textItem;
		TextView textPrice;
		
		ViewHolder(View view){
			this.btnCategory = (Button) view.findViewById(R.id.buttonCategory);
    		this.textItem = (TextView) view.findViewById(R.id.txtItemName);
    		this.textPrice = (TextView) view.findViewById(R.id.textItemPrice);
		}
	}
	
	public ItemAdapter(Context context, int textViewResourceId, List<ItemData> objects) {
		super(context, textViewResourceId, objects);
		this.inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.context = context;
	}
	
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
    	ViewHolder holder;
    	if(convertView == null){
    		convertView = inflater.inflate(R.layout.item_row, null);
    		holder = new ViewHolder(convertView);
    		convertView.setTag(holder);
    	}else{
    		holder = (ViewHolder)convertView.getTag();
    	}
    	
    	final ItemData item = (ItemData)getItem(position);
    	if(item != null){
    		// 項目名のセット
    		if(item.getNumber() > 1){ // 複数個の場合は個数表示を末尾に追加
    			String strColor = context.getResources().getString(R.string.item_number_color);
    			String strNum = Integer.toString(item.getNumber());
    			String strHtml = "<font color=" +strColor+ "> ×" +strNum+ "</font>";
    			holder.textItem.setText(Html.fromHtml(item.getName() + strHtml));
    		}
    		else{
    			holder.textItem.setText(item.getName());
    		}
    		
    		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
    		
    		// 符号（プラスマイナス）を付ける
    		String sign = "";
    		int priceColor = context.getResources().getColor(R.color.minus);
    		String initUnit = context.getResources().getString(R.string.initial_unit_string);
    		String unit = pref.getString("unit_string", initUnit);
    		if(item.getPrice() > 0){
    			sign = "+";
    			priceColor = context.getResources().getColor(R.color.plus);
    		}
    		holder.textPrice.setText(sign + Integer.toString(item.getTotalPrice()) + unit);
    		holder.textPrice.setTextColor(priceColor);
    		
    		// 文字サイズの設定
    	    String initFontSize = context.getResources().getString(R.string.initial_font_size);
    		ViewGroup.LayoutParams params = holder.btnCategory.getLayoutParams();
    		params.width = (int) convertDpToPixel(Float.parseFloat(
    				pref.getString("char_size", initFontSize)), context);
    	    params.height = params.width;
    	    holder.btnCategory.setLayoutParams(params);
    		
    		holder.textItem.setTextSize(Integer.parseInt(pref.getString("char_size", initFontSize)));
    		holder.textPrice.setTextSize(Integer.parseInt(pref.getString("char_size", initFontSize)));
    		
    		// カテゴリー表示の背景色の設定
    		if(item.getCategory() > 0){
    			int color = context.getResources().getIdentifier("category_"+item.getCategory(),
    					"color", context.getPackageName());
    			holder.btnCategory.setBackgroundResource(color);
    		}
    		
    		// 背景色を交互に変更
			if(position % 2 == 0){
    			convertView.setBackgroundResource(R.drawable.list_item_color1);
    		}else{
    			convertView.setBackgroundResource(R.drawable.list_item_color2);
    		}
    	}
    	
    	convertView.setOnClickListener(new View.OnClickListener() {
    		public void onClick(View view){            			
    			ItemMenuDialog dialog = new ItemMenuDialog(item, position, getCount());
    			dialog.CreateDialog();
    		}
		});
    	
    	return convertView;
    }

    public interface MoveItemListener extends EventListener{
		public void upItem(ItemData item, int itemPosition);
		public void downItem(ItemData item, int itemPosition);
	}
    
    private class ItemMenuDialog implements ItemMenuDialogFragment.ClickedMenuListener, 
    CheckDialogFragment.CheckDialogFragmentListener{
    	ItemData item;
    	int position;
    	int dayListSize;
    	
    	ItemMenuDialog(ItemData item, int position, int dayListSize){
    		this.item = item;
    		this.position = position;
    		this.dayListSize = dayListSize;
    	}
    	
    	public void CreateDialog(){
    		boolean upIsPossible = dayListSize > 1 && position > 0;
    		boolean downIsPossible = dayListSize > 1 && position < dayListSize - 1;
    		
    		// 編集・削除ダイアログを生成        			       			
			ItemMenuDialogFragment newFragment = 
					ItemMenuDialogFragment.newInstance(item.getName(), upIsPossible, downIsPossible);
			newFragment.setClickedMenuListener(ItemMenuDialog.this);
			newFragment.show(((Activity)context).getFragmentManager(), "item_menu_dialog");
    	}
    	
    	@Override
		public void doEditClick() {
			// アイテムの編集ダイアログを生成
			EditItemDialogFragment newFragment;
			newFragment = EditItemDialogFragment.newInstance(item, position);
			newFragment.setEditItemDialogListener((MainActivity)context);
			newFragment.show(((Activity)context).getFragmentManager(), "edit_item_dialog");
		}

		@Override
		public void doDeleteClick() {
			//　削除確認ダイアログを表示
    		CheckDialogFragment newFragment;
    		newFragment = CheckDialogFragment.newInstance("警告", item.getName()+"を削除しますか？");
    		newFragment.setCheckDialogFragmentListener(ItemMenuDialog.this);
    		newFragment.show(((Activity)context).getFragmentManager(), "check_item_delete_dialog");
		}

		@Override
		public void doUpClick() {
			moveItemListener.upItem(item, position);
		}

		@Override
		public void doDownClick() {
			moveItemListener.downItem(item, position);
		}
    	
    	@Override
		public void ClickedPositiveButton() {
    		Calendar c = item.getDate();
    		remove(item);
    		itemRemoveListener.removeItem(c);
		}

		@Override
		public void ClickedNegativeButton() {}

    	
    	private class EditItemDialogListener implements DialogListener{

    		@Override
    		public void doPositiveClick() {
    			Calendar c = item.getDate();
        		remove(item);
        		itemRemoveListener.removeItem(c);
    		}

			@Override
			public void doNegativeClick() {
				// TODO 自動生成されたメソッド・スタブ
			}
    	}
    }

    public void setMoveItemListener(MoveItemListener listener){
	    this.moveItemListener = listener;
	}

	public void removeMoveItemListener(){
	    this.moveItemListener = null;
	}

    
    /**
	 * リスナーを追加
	 */
	public void setItemRemoveListener(ItemRemoveListener listener){
	    this.itemRemoveListener = listener;
	}
	    
	/**
	 * リスナー削除
	 */
	public void removeItemRemoveListener(){
	    this.itemRemoveListener = null;
	}

	public interface ItemRemoveListener extends EventListener{

		/**
		 * アイテムが削除されたイベントを通知
		 */
		public void removeItem(Calendar deletedDate);

	}

	/**                                                                                                                
	 * This method convets dp unit to equivalent device specific value in pixels.
	 *
	 * @param dp A value in dp(Device independent pixels) unit which will be converted to pixels
	 * @param context Context to get resources and device specific display metrics
	 * @return A float value to represent Pixels equivalent to dp according to device
	 */
	public static float convertDpToPixel(float dp, Context context){
	    Resources resources = context.getResources();
	    DisplayMetrics metrics = resources.getDisplayMetrics();
	    float px = dp * (metrics.densityDpi /160f);
	    return px;
	}
}