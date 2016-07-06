package jp.shimi.saifu;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.util.Log;

/**
 * 日ごとのデータ
 */
public class DayData{
	private Calendar date;
	private List<ItemData> itemList;
	private int balance;
	
	public DayData(){
		this.date = Calendar.getInstance();
		this.date.setTime(DateChanger.ChangeToDate("2000/01/01"));
		this.itemList = new ArrayList<ItemData>();
		this.balance = 0;
	}
	
	public DayData(Calendar date, int balance){
		this.date = date;
		this.itemList = new ArrayList<ItemData>();
		this.balance = balance;
	}

	public Calendar getDate(){
		return this.date;
	}
	
	public List<ItemData> getItemList(){
		return this.itemList;
	}
	
	public int getBalance(){
		return this.balance;
	}
	
	public int setBalance(int balance){
		return this.balance = balance;
	}
	
	public void addBalance(int price){
		this.balance += price;
	}
	
	public String getStringDate(){
		return DateChanger.ChangeToString(this.date);
	}
	
	public String getStringBalance(){
		return Integer.toString(this.balance);
	}
	
	public void addItem(ItemData newItem){
		this.itemList.add(newItem);
	}
	
	public void addItem(ItemData newItem, int position){
		this.itemList.add(position, newItem);
	}

	public void addItemList(List<ItemData> newItemList){
		this.itemList.addAll(newItemList);
	}

	public void addItemList(List<ItemData> newItemList, int position){
		this.itemList.addAll(position, newItemList);
	}
	
	public void setItem(int index, ItemData newItem){
		this.itemList.set(index, newItem);
	}
	
	public void setItemList(List<ItemData> itemList){
		this.itemList = itemList;
	}
	
	public void removeItem(int index){
		this.itemList.remove(index);
	}
	
	public int getItemSize(){
		return this.itemList.size();
	}
	
	public int getDifference(){
		int dif = 0;
		for(int i = 0; i < this.itemList.size(); i++){
			dif += this.itemList.get(i).getPrice();
		}
		return dif;
	}
	
	public void exchangeItemPosition(int pos1, int pos2){
		ItemData tmp = this.itemList.get(pos1);
		this.itemList.set(pos1, this.itemList.get(pos2));
		this.itemList.set(pos2, tmp);
	}
	
	public void upItemPosition(int position){
		if(this.itemList.size() > 1 && position > 0){
			exchangeItemPosition(position, position - 1);
		}
	}
	
	public void downItemPosition(int position){
		if(this.itemList.size() > 1 && position < this.itemList.size() - 1){
			exchangeItemPosition(position, position + 1);
		}
	}

	// 名前で検索するのは危険
	/*public boolean itemIsExist(String name){
		for(int i = 0; i < this.itemList.size(); i++){
			if(this.itemList.get(i).getName().equals(name)){
				return true;
			}
		}
		return false;
	}*/

	public boolean itemIsExist(int itemId){
		for(int i = 0; i < this.itemList.size(); i++){
			if(this.itemList.get(i).getId() == itemId){
				return true;
			}
		}
		return false;
	}
	
	public void showListLog(){
		for(int i = 0; i < this.itemList.size(); i++){
			Log.d("ShowListLog", i+":"+this.itemList.get(i).getName()+":"+this.itemList.get(i).getPrice());
		}
	}

}