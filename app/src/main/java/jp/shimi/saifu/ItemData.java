package jp.shimi.saifu;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Calendar;

/**
 * アイテムのクラス
 */

public class ItemData implements Parcelable {
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

    public int getId(){
        return this.id;
    }

	public ItemData setId(int id) {
		this.id = id;
		return this;
	}

	public String getName(){
		return this.name;
	}

	public ItemData setName(String name) {
		this.name = name;
		return this;
	}
	
	public int getPrice(){
		return this.price;
	}

	public ItemData setPrice(int price) {
		this.price = price;
		return this;
	}

	public Calendar getDate(){
		return this.date;
	}

	public String getStringDate(){
		return DateChanger.ChangeToString(this.date);
	}

	public ItemData setDate(Calendar date) {
		this.date = date;
		return this;
	}
	
	public int getNumber(){
		return this.number;
	}

	public ItemData setNumber(int number) {
		this.number = number;
		return this;
	}
	
	public int getTotalPrice(){
		return this.price * this.number;
	}

	public int getCategory(){
		return this.category;
	}

	public ItemData setCategory(int category) {
		this.category = category;
		return this;
	}

    public int getWalletId(){ return this.walletId; }

	public ItemData setWalletId(int walletId) {
		this.walletId = walletId;
		return this;
	}

	public int getReverseItemId(){ return this.reverseItemId; }

	public ItemData setReverseItemId(int reverseItemId) {
		this.reverseItemId = reverseItemId;
		return this;
	}

    /*************************Parcelable****************************/
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(this.id);
		dest.writeString(this.name);
		dest.writeInt(this.price);
		dest.writeSerializable(this.date);
		dest.writeInt(this.number);
		dest.writeInt(this.category);
		dest.writeInt(this.walletId);
		dest.writeInt(this.reverseItemId);
	}

	protected ItemData(Parcel in) {
		this.id = in.readInt();
		this.name = in.readString();
		this.price = in.readInt();
		this.date = (Calendar) in.readSerializable();
		this.number = in.readInt();
		this.category = in.readInt();
		this.walletId = in.readInt();
		this.reverseItemId = in.readInt();
	}

	public static final Parcelable.Creator<ItemData> CREATOR = new Parcelable.Creator<ItemData>() {
		@Override
		public ItemData createFromParcel(Parcel source) {
			return new ItemData(source);
		}

		@Override
		public ItemData[] newArray(int size) {
			return new ItemData[size];
		}
	};
}