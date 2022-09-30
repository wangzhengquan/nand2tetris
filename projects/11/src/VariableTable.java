public class VariableTable {
  private final SymbolMap<Variable> classVarTable;
  private SymbolMap<Variable> subroutineVarTable;

  private int staticVarCount;
  private int fieldVarCount;
  private int argumentVarCount;
  private int localVarCount;

  public VariableTable() {
    classVarTable = new SymbolMap<>();
    staticVarCount = 0;
    fieldVarCount = 0;
  }

  public void startSubroutine(int argumentVarIdx) {
    subroutineVarTable = new SymbolMap<>();
    argumentVarCount = argumentVarIdx;
    localVarCount = 0;
  }

  public void put(String name, String type, String kind) throws Exception {
    switch (kind) {
      case Variable.KIND_STATIC:
        put(classVarTable, new Variable(name, type, kind, staticVarCount++));
        break;
      case Variable.KIND_FIELD:
        put(classVarTable, new Variable(name, type, kind, fieldVarCount++));
        break;
      case Variable.KIND_ARG:
        put(subroutineVarTable, new Variable(name, type, kind, argumentVarCount++));
        break;
      case Variable.KIND_LOCAL:
        put(subroutineVarTable, new Variable(name, type, kind, localVarCount++));
        break;
      default:
        throw new IllegalArgumentException("Invalid variable kind\n" + name);
    }
  }

  public static void put(SymbolMap<Variable> st, Variable var) throws Exception {
    if (st.contains(var.name)) {
      throw new Exception(" variable " + var.name + " is already defined in the scope");
    }
    st.put(var.name, var);
  }

  public int varCount(String kind) {
    switch (kind) {
      case Variable.KIND_STATIC:
        return staticVarCount;
      case Variable.KIND_FIELD:
        return fieldVarCount;
      case Variable.KIND_ARG:
        return argumentVarCount;
      case Variable.KIND_LOCAL:
        return localVarCount;
      default:
        throw new IllegalArgumentException("Invalid variable kind\n" + kind);
    }
  }

  public Variable get(String name) {
    Variable var = subroutineVarTable.get(name);
    if (var == null) {
      var = classVarTable.get(name);
    }
    return var;
  }

  public String kindOf(String name) {
    Variable var = this.get(name);
    if (var == null)
      return Variable.KIND_NONE;
    else
      return var.kind;
  }

  public String typeOf(String name) {
    return this.get(name).type;
  }

  public int indexOf(String name) {
    return this.get(name).index;
  }

  public static void main(String[] args) {
  }
}
