package android.DB.model;

/**
 * テーブルクラス
 *
 * Created by gerog on 2016/09/19.
 */
public class Table {
    private String name;
    private Column[] columns;

    public Table(String name, Column[] columns){
        this.name = name;
        this.columns = columns;
    }

    public String getName(){
        return this.name;
    }

    public Column[] getColumns(){
        return this.columns;
    }

    // CREATEとかに使う文字列スキーマを生成
    public String getSQLSchema(){
        String s = "";
        for(Column column : columns){
            s += column.getName() + " "
                    + column.getType() + " "
                    + column.getConstraint() + ",";
        }
        return s.substring(0, s.length()-1);
    }
}
