package jp.shimi.saifu;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.util.Log;

/**
 * 日付と文字列を相互に変換するためのユーティリティクラス
 */
public class DateChanger {
	private static final SimpleDateFormat YYYYMMDD = new SimpleDateFormat("yyyy/MM/dd");

	private DateChanger(){};
	
	public static String ChangeToString(Date dateDate){
		return YYYYMMDD.format(dateDate);
	}
	
	public static String ChangeToString(Calendar calenderDate){
		return YYYYMMDD.format(calenderDate.getTime());
	}
	
	public static Date ChangeToDate(String stringDate){
		try{
			return YYYYMMDD.parse(stringDate);
		} catch (java.text.ParseException e) {
			Log.d("ParseError", stringDate);
			return null;
		}
	}
	
	public static Calendar ChangeToCalendar(String stringDate){
		Calendar cal = Calendar.getInstance();
		Date d = ChangeToDate(stringDate);
		if(d == null){
			return null;
		}else{
			cal.setTime(d);
			return cal;
		} 
	}
}
