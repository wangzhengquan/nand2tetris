public class Variable {
  public final static String KIND_STATIC = "static";
  public final static String KIND_FIELD = "field";
  public final static String KIND_ARG = "arg";
  public final static String KIND_LOCAL = "var";
  public final static String KIND_NONE = "none";

  public String name;
  public String type;
  public String kind; // STATIC, FIELD, ARG, or VAR
  public int index;

  public Variable(String name, String type, String kind, int index) {
    this.name = name;
    this.type = type;
    this.kind = kind;
    this.index = index;
  }
 

  public static void main(String[] args) {
  }
}
