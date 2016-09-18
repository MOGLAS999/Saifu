package android.DB;

import android.DB.model.Column;
import android.DB.model.Table;
import android.content.ContentValues;

import jp.shimi.saifu.DayData;
import jp.shimi.saifu.ItemData;

/**
 * DBの名前とスキーマを定義するstaticクラス
 *
 * Created by gerog on 2016/09/19.
 */
public final class DBDefinition {
    static final public String DB_NAME = "Saifu.db";
    static final public String DAY_TABLE_NAME = "day_table";
    static final public String ITEM_TABLE_NAME = "item_table";
    static final public String WALLET_TABLE_NAME = "wallet_table";
    static final public int DB_VERSION = 5;

    // 日テーブル
    static final public Table DAY_TABLE = new Table(
            DAY_TABLE_NAME,
            new Column[] {
                    new Column("date","text", "primary key"),
                    new Column("balance", "integer")
            }
    );

    // アイテムテーブル
    static final public Table ITEM_TABLE = new Table(
            ITEM_TABLE_NAME,
            new Column[] {
                    new Column("id","integer", "primary key autoincrement"),
                    new Column("name", "text", "not null default '<<no_name>>'"),
                    new Column("price", "integer", "not null default 0"),
                    new Column("date", "text", "not null"),
                    new Column("number", "text", "not null default '<<no_name>>'"),
                    new Column("name", "integer"),
                    new Column("category", "integer"),
                    new Column("sequence", "integer", "not null"),
                    new Column("wallet_id", "integer", "not null default -1"),
                    new Column("reverse_item_id", "integer", "not null default -1") // 反転関係にあるアイテム(例：現金で電子マネーをチャージするなど)
            }
    );

    // サイフテーブル
    static final public Table WALLET_TABLE = new Table(
            DAY_TABLE_NAME,
            new Column[] {
                    new Column("id","integer", "primary key autoincrement"),
                    new Column("name", "text"),
                    //new Column("currency", "text")
            }
    );



}
