package jp.shimi.saifu.dialog;

import java.util.Calendar;

import jp.shimi.saifu.DateChanger;
import jp.shimi.saifu.ItemData;
import jp.shimi.saufu.R;

import android.DB.MySQLiteAdapter;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

public class EditItemDialogFragment extends DialogFragment 
implements SelectCategoryDialogFragment.SelectedCategoryListener{
	LayoutInflater inflater;
	private EditItemDialogListener listener = null;

	// アイテムの新規作成
	public static EditItemDialogFragment newInstance(Calendar initDate, Context context) {
		EditItemDialogFragment fragment = new EditItemDialogFragment();

		MySQLiteAdapter dbAdapter = new MySQLiteAdapter(context);
		int nextItemId = dbAdapter.getMaxItemId() + 1;

		// 引数を設定
		Bundle args = new Bundle();
		ItemData itemData = new ItemData().setDate(initDate);
		args.putParcelable("itemData", itemData);
		args.putInt("itemId", nextItemId);
		args.putInt("editPosition", -1);
		fragment.setArguments(args);
		 
		return fragment;
	} 

	// アイテムの編集
	public static EditItemDialogFragment newInstance(ItemData itemData, int editedItemPosition) {
		EditItemDialogFragment fragment = new EditItemDialogFragment();
		  
		Bundle args = new Bundle();
		args.putParcelable("itemData", itemData);
		args.putInt("itemId", itemData.getId());
		args.putInt("editPosition", editedItemPosition);
		fragment.setArguments(args);
		 
		return fragment;
	} 
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState){
		inflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View layout = inflater.inflate(R.layout.diary_dialog,
        		(ViewGroup)getActivity().findViewById(R.id.diarydialog_layout));
        
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final int editPosition = getArguments().getInt("editPosition");
		final ItemData initItemData = getArguments().getParcelable("itemData");
		final int itemId = getArguments().getInt("itemId");

        String title = "項目の追加";
        if(editPosition >= 0) title = "項目の編集";
        builder.setTitle(title);
        builder.setView(layout);

        final Calendar dCalendar = initItemData.getDate();

		// コンポーネント読み込み
        final EditText editName = (EditText)layout.findViewById(R.id.editDialogItem);
        final EditText editPrice = (EditText)layout.findViewById(R.id.editDialogPrice);
        final EditText editDate = (EditText)layout.findViewById(R.id.editDialogDate);
        final EditText editNumber = (EditText)layout.findViewById(R.id.editDialogNumber);
        final TextView textTotalPrice = (TextView)layout.findViewById(R.id.textTotalPrice);
		final Button btnConsumptionTax = (Button)layout.findViewById(R.id.consumptionTaxButton);
        final Button btnCategory = (Button)layout.findViewById(R.id.categoryButton);
        
        textTotalPrice.setWidth(textTotalPrice.getWidth());
        
        // テキスト入力を監視
        editPrice.addTextChangedListener(new TextWatcher(){
			@Override
			public void afterTextChanged(Editable s){	
				setTotalPrice(layout);
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}
        });
        editNumber.addTextChangedListener(new TextWatcher(){
			@Override
			public void afterTextChanged(Editable s){	
				setTotalPrice(layout);
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}
        });
        
        // 編集の場合EditTextを元データで初期化する
        if(editPosition >= 0){        	
        	int itemPrice = initItemData.getPrice();
        	int itemNumber = initItemData.getNumber();
        	int itemCategory = initItemData.getCategory();
        	
        	editName.setText(initItemData.getName());
        	editPrice.setText(Integer.toString(Math.abs(itemPrice)));
        	editNumber.setText(Integer.toString(itemNumber));
        	btnCategory.setText(Integer.toString(itemCategory));
        	if(itemCategory > 0){
        		int color = getResources().getIdentifier("category_"+itemCategory, 
        				"color", getActivity().getPackageName());
				btnCategory.setBackgroundResource(color);
        	}
        	
        	if(itemNumber > 1){        		
        		long totalPrice = itemPrice * itemNumber;		
        		textTotalPrice.setText(Long.toString(totalPrice));
        	}
        }
        
        // カテゴリー(色)選択ダイアログ
        btnCategory.setOnClickListener(new OnClickListener(){
        	@Override
			public void onClick(View v) {
        		SelectCategoryDialogFragment newFragment;
        		newFragment = SelectCategoryDialogFragment.newInstance(0);
        		newFragment.setSelectedCategoryListener(EditItemDialogFragment.this);
        		newFragment.show(getActivity().getFragmentManager(), "select_category_dialog");
        	}
        });

		// 税込み価格計算ボタンの処理
		// ボタンを押すと価格×1.08する
		// TODO:消費税を期間ごとに設定できるようにする
		btnConsumptionTax.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String strPrice = editPrice.getText().toString();
				if(!strPrice.isEmpty() && strPrice != null){
					int price = Integer.parseInt(strPrice);
					price *= 1.08;
					editPrice.setText(Integer.toString(price));
				}
			}
		});
        
        editDate.setText(DateChanger.ChangeToString(dCalendar.getTime()));
        editDate.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener(){
					@Override
					public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
						dCalendar.set(Calendar.YEAR, year);
						dCalendar.set(Calendar.MONTH, monthOfYear);
						dCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
						editDate.setText(DateChanger.ChangeToString(dCalendar.getTime()));
					}
				};
				
				final DatePickerDialog datePickerDialog = new DatePickerDialog(
						getActivity(), dateSetListener, 
						dCalendar.get(Calendar.YEAR), 
						dCalendar.get(Calendar.MONTH),
						dCalendar.get(Calendar.DAY_OF_MONTH));
				
				datePickerDialog.show();
			}
    	});
        // プラマイ切り替えボタン
        final Button plusMinusButton = (Button)layout.findViewById(R.id.plusMinusButton);
        final int plusColor = getActivity().getResources().getColor(R.color.plus);
        final int minusColor = getActivity().getResources().getColor(R.color.minus);
        plusMinusButton.setTextColor(minusColor);
        if(editPosition >= 0 && initItemData.getPrice() > 0){
        	plusMinusButton.setText(getResources().getString(R.string.plus));
        	plusMinusButton.setTextColor(plusColor);
        }
        plusMinusButton.setOnClickListener(new OnClickListener() {
        	@Override
        	public void onClick(View v) {
        		if((String)plusMinusButton.getText() == getResources().getString(R.string.minus)){
        			plusMinusButton.setText(getResources().getString(R.string.plus));
        			plusMinusButton.setTextColor(plusColor);
        		}
        		else if((String)plusMinusButton.getText() == getResources().getString(R.string.plus)){
        			plusMinusButton.setText(getResources().getString(R.string.minus));
        			plusMinusButton.setTextColor(minusColor);
        		}
        	}
        });
        builder.setPositiveButton("OK", null);
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
        	@Override
			public void onClick(DialogInterface dialog, int which) {
				// Cancel時の処理　現在は空
			}
		});
        
        final AlertDialog alertDialog = builder.create();
        
        alertDialog.setOnShowListener(new OnShowListener(){
			@Override
			public void onShow(DialogInterface dialog) {
				// ポジティブボタンの動作処理
				Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
				positiveButton.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View v) {
		            	EditText etxtName   = (EditText)layout.findViewById(R.id.editDialogItem);
		            	EditText etxtPrice  = (EditText)layout.findViewById(R.id.editDialogPrice);
		            	EditText etxtNumber = (EditText)layout.findViewById(R.id.editDialogNumber);
		            	
		            	String errorMessage = "";
		            	if(etxtName.getText().toString().equals("")){
		            		errorMessage += "項目名を入力してください。";
		            	}
		            	else if(etxtName.getText().toString().equals("")){
		            		errorMessage += "同じ日付に同名の項目は追加できません。";
		            	}
		            	if(etxtPrice.getText().toString().equals("")){
		            		if(!errorMessage.equals("")) errorMessage += "\n";
		            		errorMessage += "価格を入力してください。";
		            	}
		            	if(etxtNumber.getText().toString().equals("")){
		            		if(!errorMessage.equals("")) errorMessage += "\n";
		            		errorMessage += "個数を入力してください。";
		            	}
		            	else if(Integer.parseInt(etxtNumber.getText().toString()) <= 0 ||
		            			Integer.parseInt(etxtNumber.getText().toString()) > 9999){
		            		if(!errorMessage.equals("")) errorMessage += "\n";
		            		errorMessage += "個数は1~9999までが有効です。";
		            	}
		            	//if(ItemIsE))
		            	
		            	//　エラーがある場合は警告ダイアログを表示
		            	if(!errorMessage.equals("")){
		            		SimpleMessageDialogFragment newFragment;
		            		newFragment = SimpleMessageDialogFragment.newInstance("警告", errorMessage);
		            		newFragment.show(getActivity().getFragmentManager(), "caution_dialog");
		            	}
		            	else{
		            		// 入力内容を取得
		            		String strName   = etxtName.getText().toString();
		            		String strPrice  = etxtPrice.getText().toString();
		            		String strNumber = etxtNumber.getText().toString();
		            		String strCategory = btnCategory.getText().toString();
				    	
		            		// 数値に変換
		            		int price = Integer.parseInt(strPrice);
		            		int number = Integer.parseInt(strNumber);
		            		if((String)plusMinusButton.getText() == getResources().getString(R.string.minus)){
		            			price *= (-1);
		            		}
		            		int category = Integer.parseInt(strCategory);
		            		if(category < 0) category = 0;
		            		
		            		// 項目を元のアクティビティに返す
		            		ItemData itemData = new ItemData(itemId, strName, price, editDate.getText().toString(),
		            				number, category, initItemData.getWalletId(), initItemData.getReverseItemId());
		            		if(editPosition > -1){ // 何も項目が変わっていない場合はレコード更新を行わない
								if(itemData.getName() == initItemData.getName() &&
										itemData.getPrice() == initItemData.getPrice() &&
										itemData.getDate() == initItemData.getDate() &&
										itemData.getNumber() == initItemData.getNumber() &&
										itemData.getCategory() == initItemData.getCategory() &&
										itemData.getWalletId() == initItemData.getWalletId() &&
										itemData.getReverseItemId() == initItemData.getReverseItemId()) {
									alertDialog.dismiss(); // ダイアログを閉じる
								}
		            		}
							listener.onReturnEditedItemData(itemData, initItemData.getDate(), editPosition);
							alertDialog.dismiss(); // ダイアログを閉じる
		            	}  	            	           
					}
				});
			}
        });
        
	    return alertDialog;
	}

	public interface EditItemDialogListener{

		public void onReturnEditedItemData(ItemData itemData, Calendar initDate, int editPosition);
	}

	/**
	 * リスナーを追加
	 */
	public void setEditItemDialogListener(EditItemDialogListener listener){
		this.listener = listener;
	}
	    
	/**
	 * リスナー削除
	 */
	public void removeEditItemDialogListener(){
	    this.listener = null;
	}

	private void setTotalPrice(View v){
		TextView textTotalPrice = (TextView)v.findViewById(R.id.textTotalPrice);
		EditText editPrice = (EditText)v.findViewById(R.id.editDialogPrice);
		EditText editNumber = (EditText)v.findViewById(R.id.editDialogNumber);
		
		if(editPrice.getText().toString().isEmpty() ||
				editNumber.getText().toString().isEmpty()){
			textTotalPrice.setText(R.string.total_price);
		}
		else{
			long itemPrice = Long.parseLong(editPrice.getText().toString());
    		long itemNumber = Long.parseLong(editNumber.getText().toString());
		
			long totalPrice = itemPrice * itemNumber;		
			textTotalPrice.setText(Long.toString(totalPrice));
		}
	}

	@Override
	public void SelectedCategory(int selectedCategoryNum) {
		final Button btnCategory = (Button)this.getDialog().findViewById(R.id.categoryButton);
		
		btnCategory.setText(Integer.toString(selectedCategoryNum));
		
		if(selectedCategoryNum == 0){
			btnCategory.setBackgroundDrawable(getResources().getDrawable(R.drawable.color_select_button));
		}
		else if(selectedCategoryNum > 0){
			int color = getResources().getIdentifier("category_"+selectedCategoryNum, 
					"color", getActivity().getPackageName());
		
			btnCategory.setBackgroundResource(color);
		}
		//GradientDrawable myButton = (GradientDrawable)btnCategory.getBackground();
		//myButton.setColor(color);
		//myButton.setStroke(1, getResources().getColor(R.color.category_button_border));
		
	}
}	
