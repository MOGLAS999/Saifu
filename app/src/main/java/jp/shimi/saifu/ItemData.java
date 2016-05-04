package jp.shimi.saifu;

import java.util.Calendar;

public class ItemData{
	private String item;
	private int price;
	private Calendar date;
	private int number; // 個数 TODO:quantity等に変更すべき？
	private int category;
	private DateChanger dc;
	
	public ItemData(){
		this.item = "";
		this.price = 0;
		this.date = Calendar.getInstance();
		this.date.setTime(dc.ChangeToDate("2000/01/01"));
		this.number = 1;
		this.category = 0;
		this.dc = new DateChanger();
	}
	
	public ItemData(String item, int price, String date, int number, int category){
		this.item = item;
		this.price = price;
		this.number = number;
		this.category = category;
		
		this.dc = new DateChanger();
		this.date = Calendar.getInstance();
		if(dc.ChangeToDate(date) == null){
			this.date.setTime(dc.ChangeToDate("2000/01/01"));
		}
		else{
			this.date.setTime(dc.ChangeToDate(date));
		}
	}
	
	public ItemData(String item, int price, Calendar date, int number, int category){
		this.item = item;
		this.price = price;
		this.date = date;
		this.number = number;
		this.category = category;
	}
	
	public String GetItem(){
		return this.item;
	}
	
	public int GetPrice(){
		return this.price;
	}
	
	public Calendar GetDate(){
		return this.date;
	}
	
	public String GetStringDate(){
		return dc.ChangeToString(this.date);
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

}