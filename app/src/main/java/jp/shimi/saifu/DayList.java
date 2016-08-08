package jp.shimi.saifu;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class DayList {
	private  List<DayData> dataList;
	
	public DayList(){
		this.dataList = new ArrayList<DayData>();
	}
	
	public DayList(List<DayData> dataList){
		this.dataList = dataList;
	}
	
	public List<DayData> getList(){
		return this.dataList;
	}
	
	// 末尾にデータを追加
	public void addData(DayData dd){
		this.dataList.add(dd);
		UpdateBalance(getListSize()-1);
	}
	
	// 指定した位置にデータを追加
	public void addData(int index, DayData dd){
		this.dataList.add(index, dd);
		UpdateBalance(index);
	}
	
	// 日付に基づいた位置にデータを追加
	public void addDataByDate(DayData dd){
		for(int i = 0; i < this.dataList.size(); i++){
			if(dd.getDate().compareTo(this.getData(i).getDate()) < 0){
				this.dataList.add(i, dd);
				UpdateBalance(i);
				return;
			}
		}
		this.addData(dd);
		UpdateBalance(getListSize()-1);
	}	
	
	// 末尾にリストをデータとして追加
	public void addList(DayList addList){
		this.dataList.addAll(addList.dataList);
	}
		
	// 指定した位置にリストをデータとして追加
	public void addList(int index, DayList addList){
		this.dataList.addAll(index, addList.dataList);
	}
		
	// 指定した位置にデータを上書き
	public void setData(int index, DayData newData){
		this.dataList.set(index, newData);
	}
	
	// 指定した位置のデータを削除
	public void removeData(int index){
		this.dataList.remove(index);
		UpdateBalance(index);
	}
	
	// データを全削除
	public void ClearList(){
		this.dataList.clear();
	}
		
	// 指定した位置のデータを返す
	public DayData getData(int index){
		if(index < 0 || index >= this.getListSize())
			return null;
		return this.dataList.get(index);
		
	}
	
	public DayData getData(Calendar date){
		int pos = getDataPositionByDate(date);
		if(pos == -1){
			return null;
		}else{
			return this.dataList.get(pos);
		}
	}
	
	// データの個数を返す
	public int getListSize(){
		return this.dataList.size();
	}
	
	// 指定した位置のデータの持つアイテムの数を返す
	public int getItemListSize(int index){
		return this.getData(index).getItemList().size();
	}
	
	// 指定した日付のデータの位置を返す
	public int getDataPositionByDate(Calendar date){
		for(int i = 0; i < this.dataList.size(); i++){
			//Log.d("getDayData", dc.ChangetoString(this.dataList.get(i).getDate()) +"=?"+ dc.ChangetoString(date));
			if(DateChanger.ChangeToString(this.dataList.get(i).getDate()).equals(DateChanger.ChangeToString(date))){
				//Log.d("Check", "if pass");
				return i;
			}
		}
		return -1;
	}
	
	// 指定した日データの日付のデータを上書き
	public void UpdateData(DayData newData){
		for(int i = 0; i < this.dataList.size(); i++){
			if(this.dataList.get(i).getDate() == newData.getDate()){
				this.dataList.set(i, newData);
				UpdateBalance(i);
			}
		}
	}
	
	// 指定した日にアイテムデータを追加する
	public void addItemData(Calendar date, ItemData newItem){
		int pos = getDataPositionByDate(date);
		if(pos < 0){
			//Log.d("addItemData", "Error pos == "+ pos);
		}
		else{
			this.dataList.get(pos).addItem(newItem);
			UpdateBalance(pos);
		}
	}
	
	public void addItemData(ItemData newItem){
		int pos = getDataPositionByDate(newItem.getDate());
		if(pos < 0){
			//Log.d("addItemData", "Error pos == "+ pos);
		}
		else{
			this.dataList.get(pos).addItem(newItem);
			UpdateBalance(pos);
		}
	}
	
	// 指定した日の指定した位置にアイテムデータを追加する
	public void addItemData(Calendar date, ItemData newItem, int position){
		int pos = getDataPositionByDate(date);
		if(pos < 0){
			//Log.d("addItemData", "Error pos == "+ pos);
		}
		else{
			this.dataList.get(pos).addItem(newItem, position);
			UpdateBalance(pos);
		}
	}
	
	// 指定した日にアイテムデータを上書きする
	/*public void setItemData(Calendar date, ItemData newItem, int itemPos){
		int pos = getDataPositionByDate(date);
		if(pos < 0){
			//Log.d("addItemData", "Error pos == "+ pos);
		}
		else{
			this.dataList.get(pos).setItem(itemPos, newItem);
			UpdateBalance(pos);
		}
	}*/

	/**
	 * アイテムを更新する(idで一致を判断する)
	 * TODO:位置が指定できていない。前のアイテムが消えていない
     */
	public void updateItemData(ItemData updateItem){
		int count = 0;
		for(ItemData i : getData(updateItem.getDate()).getItemList()){
			if(i.getId() == updateItem.getId()){
				getData(updateItem.getDate()).getItemList().set(count, updateItem);
			}
			count++;
		}
		int pos = getDataPositionByDate(updateItem.getDate());
		UpdateBalance(pos);
	}
	
	// 指定した日にアイテムリストを上書きする
	public void setItemList(Calendar date, List<ItemData> itemList){
		this.getData(date).setItemList(itemList);
	}
	
	// 指定した日の指定した位置のアイテムデータを削除する
	public void removeItemData(Calendar date, int itemPos){
		int pos = getDataPositionByDate(date);
		if(pos < 0){
			//Log.d("addItemData", "Error pos == "+ pos);
		}
		else{
			this.dataList.get(pos).removeItem(itemPos);
			UpdateBalance(pos);
		}
	}
	
	// 指定した日付(位置)以降の残金を計算する
	public void UpdateBalance(int index){
		if(index < 0){
			//Log.d("UpdateBalance", "error");
		}else{
			for(int i = index; i < getListSize(); i++){
				int balance;
				if(i == 0) balance = 0;
				else balance = getData(i-1).getBalance();
				for(int j = 0; j < getData(i).getItemList().size(); j++){
					balance += getData(i).getItemList().get(j).getTotalPrice();
				}
				getData(i).setBalance(balance);
			}
		}
	}
	
	public void UpdateBalance(Calendar changedDate){
		UpdateBalance(getDataPositionByDate(changedDate));
	}
	
	// データのアイテムリストのサイズを確認し、サイズが0なら削除してそれ以降を再計算する
	public void CheckItemListSize(){
		int zeroPos = -1;
		for(int i = 0; i < getListSize(); i++){
			if(getItemListSize(i) <= 0){
				removeData(i);
				if(zeroPos == -1){
					zeroPos = i;
				}
			}
		}
		UpdateBalance(zeroPos);
	}
	
	// 指定した日付の次の日付を返す
	public Calendar getNextDate(Calendar date){
		if(getListSize() == 0) return null;
		if(getListSize() == 1) return getData(0).getDate();
		for(int i = 1; i < getListSize(); i++){
			DayData d1 = getData(i-1);
			DayData d2 = getData(i);
			
			if(d1.getDate().equals(date)){
				return d2.getDate();
			}
			else if(d1.getDate().before(date) && d2.getDate().after(date)){
				return d2.getDate();
			}
		}
		return getData(getListSize()-1).getDate();
	}
	
	// 指定した日付の前の日付を返す
	public Calendar getBeforeDate(Calendar date){
		if(getListSize() == 0) return null;
		if(getListSize() == 1) return getData(0).getDate();
		for(int i = 1; i < getListSize(); i++){
			DayData d1 = getData(i-1);
			DayData d2 = getData(i);
				
			if(d2.getDate().equals(date)){
				return d1.getDate();
			}
			else if(d1.getDate().before(date) && d2.getDate().after(date)){
				return d1.getDate();
			}
		}
		return getData(getListSize() - 1).getDate();
	}
	
	// 指定した日に指定したIDのアイテムがあるかどうかを返す
	public boolean ItemIsExist(Calendar date, int itemId){
		DayData dd = getData(date);
		if(dd == null){
			return false;
		}
		else{
			if(dd.itemIsExist(itemId)){
				return true;
			}
			else return false;
		}
	}
}
