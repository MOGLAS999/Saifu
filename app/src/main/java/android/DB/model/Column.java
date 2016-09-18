package android.DB.model;

/**
 * カラムクラス
 *
 * Created by gerog on 2016/09/19.
 */
// TODO:TypeとConstraintは列挙型にできるか？
public class Column{
    private String name;
    private String type;
    private String constraint;

    public Column(String name, String type){
        this.name = name;
        this.type = type;
        this.constraint = "";
    }

    public Column(String name, String type, String constraint){
        this(name, type);
        this.constraint = constraint;
    }

    public String getName(){
        return this.name;
    }

    public String getType(){
        return this.type;
    }

    public String getConstraint(){
        return this.constraint;
    }
}