package jp.shimi.saifu;

import java.util.Calendar;

public class ItemData{
	private int id;
	private String name;
	private int price;
	private Calendar date;
	private int number; // 個数 TODO:quantity等に変更すべき？
	private int category;
	private int walletId;
	private int reverseItemId;
	
	public ItemData(){
		this.id = -1;
		this.name = "";
		this.price = 0;
		this.date = Calendar.getInstance();
		this.date.setTime(DateChanger.ChangeToDate("2000/01/01"));
		this.number = 1;
		this.category = 0;
		this.walletId = -1;
		this.reverseItemId = -1;
	}
	
	public ItemData(int id, String name, int price, String date, int number, int category, int walletId, int reverseItemId){
		this(id, name, price, DateChanger.ChangeToCalendar(date), number, category, walletId, reverseItemId);
	}
	
	public ItemData(int id, String name, int price, Calendar date, int number, int category, int walletId){
		this.id = id;
		this.name = name;
		this.price = price;
		this.date = date;
		this.number = number;
		this.category = category;
		this.walletId = walletId;
		this.reverseItemId = -1;
	}

	public ItemData(int id, String name, int price, Calendar date, int number, int category, int walletId, int reverseItemId) {
		this.id = id;
		this.name = name;
		this.price = price;
		this.date = date;
		this.number = number;
		this.category = category;
		this.walletId = walletId;
		this.reverseItemId = reverseItemId;
	}

    public int GetId(){
        return this.id;
    }
	
	public String GetName(){
		return this.name;
	}
	
	public int GetPrice(){
		return this.price;
	}
	
	public Calendar GetDate(){
		return this.date;
	}
	
	public String GetStringDate(){
		return DateChanger.ChangeToString(this.date);
	}
	
	public int GetNumber(){
		return this.number;
	}
	
	public int GetTotalPrice(){
		return this.price * this.number;
	}
	
	public int GetCategory(){
		return this.category;
	}

    public int GetWalletId(){ return this.walletId; }

    public int GetReverseItemId(){ return this.reverseItemId; }

}