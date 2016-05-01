package jp.shimi.saifu.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import java.util.EventListener;

public class DayMenuDialogFragment extends DialogFragment{
	private DayMenuDialogFragmentListener listener = null;
	
	public static DayMenuDialogFragment newInstance(String title) {
		DayMenuDialogFragment fragment = new DayMenuDialogFragment();
		  
		// 引数を設定
		Bundle args = new Bundle();
		args.putString("title", title);
		fragment.setArguments(args);
		 
		return fragment;
	} 
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstatnceState){
		CharSequence[] items = {"項目を追加", "削除"};	
		
		String title = getArguments().getString("title"); 
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	    builder.setTitle(title);
	    builder.setItems(items, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch(which){
					case 0:
						listener.doFirstClick();
						break;
					case 1:
						listener.doSecondClick();
						break;
					default:
	        			break;
				}
				dismiss();
			}
		});
	    
	    return builder.create();
	}
	
	/**
	 * リスナーを追加
	 */
	public void setDialogListener(DayMenuDialogFragmentListener listener){
	    this.listener = listener;
	}
	    
	/**
	 * リスナー削除
	 */
	public void removeDialogListener(){
	    this.listener = null;
	}

	public interface DayMenuDialogFragmentListener extends EventListener {

		/**
		 * リストの一段目が押されたイベントを通知
		 */
		public void doFirstClick();

		/**
		 * リストの二段目が押されたイベントを通知
		 */
		public void doSecondClick();

	}
}
