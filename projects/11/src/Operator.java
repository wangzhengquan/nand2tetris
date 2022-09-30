public class Operator {
  private final static SymbolMap<Operator> operatorMap = new SymbolMap<>();


  static {
    operatorMap.put("|", new Operator("|", "or", 6));
    operatorMap.put("&", new Operator("&", "and", 8));

    operatorMap.put("=", new Operator("=", "eq", 9));
    operatorMap.put(">", new Operator(">", "gt", 10));
    operatorMap.put("<", new Operator("<", "lt", 10));

    operatorMap.put("+", new Operator("+", "add", 12));
    operatorMap.put("-", new Operator("-", "sub", 12));
    operatorMap.put("*", new Operator("*", "multiply", 13));
    operatorMap.put("/", new Operator("/", "divide", 13));

    //operatorMap.put("~", "not", 15);
    //operatorMap.put("-", "neg", 15);
  }

  public String symbol;
  public String command;
  public int precedence;

  public Operator(String symbol, String command, int precedence) {
    this.symbol = symbol;
    this.command = command;
    this.precedence = precedence;
  }

  public static Operator getOperator(String symbol) {
    return operatorMap.get(symbol);
  }


}
