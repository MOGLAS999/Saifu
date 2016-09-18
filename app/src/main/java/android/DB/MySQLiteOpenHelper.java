package android.DB;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * SQLiteOpenHelperのサブクラス
 * Singleton
 *
 * Created by gerog on 2016/09/19.
 */
public class MySQLiteOpenHelper extends SQLiteOpenHelper {
    private static MySQLiteOpenHelper singleton = null;

    public static synchronized MySQLiteOpenHelper getInstance(Context context) {
        if(singleton == null) {
            singleton = new MySQLiteOpenHelper(context);
        }
        return singleton;
    }

    private MySQLiteOpenHelper(Context context) {
        super(context, DBDefinition.DB_NAME, null, DBDefinition.DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO:DayはItemTableから生成できるので、後々不要になるかもしれない
        db.execSQL("CREATE TABLE " + DBDefinition.DAY_TABLE_NAME +"(" + DBDefinition.DAY_TABLE.getSQLSchema() + ");");

        db.execSQL("CREATE TABLE " + DBDefinition.ITEM_TABLE_NAME +"(" + DBDefinition.ITEM_TABLE.getSQLSchema() + ");");

        db.execSQL("CREATE TABLE " + DBDefinition.WALLET_TABLE_NAME +"(" + DBDefinition.WALLET_TABLE.getSQLSchema() + ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if(oldVersion < newVersion){
            if(oldVersion <= 2){
                db.execSQL("ALTER TABLE " + DBDefinition.ITEM_TABLE_NAME +
                        " ADD sequence integer default 0;"
                );
            }
            if((oldVersion == 3 || oldVersion == 4) && newVersion == 5){
                // DayTableの更新
                // 旧バージョンと同じカラムのテンポラリーテーブル作成
                db.execSQL("CREATE TEMPORARY TABLE tmp_" + DBDefinition.DAY_TABLE_NAME + " (" +
                        "date text not null," +
                        "balance integer" +
                        //"difference integer not null" +
                        ");"
                );

                // 既存テーブルから作成したテンポラリーテーブルにデータを入れる
                db.execSQL("INSERT INTO tmp_" + DBDefinition.DAY_TABLE_NAME + " SELECT " +
                        "date," +
                        "balance" +
                        " FROM " + DBDefinition.DAY_TABLE_NAME + ";");

                // 既存テーブル削除
                db.execSQL("DROP TABLE " + DBDefinition.DAY_TABLE_NAME + ";");


                // ItemTableの更新
                // 旧バージョンと同じカラムのテンポラリーテーブル作成
                db.execSQL("CREATE TEMPORARY TABLE tmp_" + DBDefinition.ITEM_TABLE_NAME + " (" +
                        "date text not null,"+
                        "name text,"+
                        "price integer,"+
                        "number integer,"+
                        "category integer,"+
                        "sequence integer not null"+
                        ");"
                );

                // 既存テーブルから作成したテンポラリーテーブルにデータを入れる
                db.execSQL("INSERT INTO tmp_" + DBDefinition.ITEM_TABLE_NAME + " SELECT " +
                        "date," +
                        "name," +
                        "price," +
                        "number," +
                        "category," +
                        "sequence" +
                        " FROM " + DBDefinition.ITEM_TABLE_NAME + ";");

                // 既存テーブル削除
                db.execSQL("DROP TABLE " + DBDefinition.ITEM_TABLE_NAME + ";");

                // テーブル作成
                // 2つ同時にテーブル作成が行われるので、両方をテンポラリーテーブルに入れてからCreateする。
                onCreate(db);

                // テンポラリーテーブルから新テーブルにデータを入れる
                db.execSQL("INSERT INTO " + DBDefinition.DAY_TABLE_NAME + " SELECT " +
                        "date," +
                        "balance" +
                        " FROM tmp_" + DBDefinition.DAY_TABLE_NAME + ";");

                // テンポラリーテーブルから新テーブルにデータを入れる
                db.execSQL("INSERT INTO " + DBDefinition.ITEM_TABLE_NAME +
                        "(name, price, date, number, category, sequence, wallet_id, reverse_item_id)" +
                        " SELECT " +
                        "name," +
                        "price," +
                        "date," +
                        "number," +
                        "category," +
                        "sequence," +
                        "-1," +
                        "-1" +
                        " FROM tmp_" + DBDefinition.ITEM_TABLE_NAME + ";");
            }
        }
    }
}
