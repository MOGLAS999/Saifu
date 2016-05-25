package jp.shimi.saifu;

/**
 * Created by gerog on 2016/05/26.
 */
public class Wallet {
    private int id;
    private String name;
    private String currency; // 通貨

    public Wallet(){
        id = -1;
        name = "";
        currency = "円";
    }

    public Wallet(int id, String name, String currency){
        this.id = id;
        this.name = name;
        this.currency = currency;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCurrency() {
        return currency;
    }
}
