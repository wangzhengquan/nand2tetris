public class Parser {

  public final static int C_ARITHMETIC = 1;
  public final static int C_PUSH = 2;
  public final static int C_POP = 3;
  public final static int C_LABEL = 4;
  public final static int C_GOTO = 5;
  public final static int C_IF_GOTO = 6;
  public final static int C_FUNCTION = 7;
  public final static int C_RETURN = 8;
  public final static int C_CALL = 9;
  public final static SymbolTable<Integer> commandTypeST = new SymbolTable<>();

  static {
    //Arithmetic/Logical commands
    commandTypeST.put("add", C_ARITHMETIC);
    commandTypeST.put("sub", C_ARITHMETIC);
    commandTypeST.put("neg", C_ARITHMETIC);
    commandTypeST.put("or", C_ARITHMETIC);
    commandTypeST.put("and", C_ARITHMETIC);
    commandTypeST.put("not", C_ARITHMETIC);
    commandTypeST.put("eq", C_ARITHMETIC);
    commandTypeST.put("gt", C_ARITHMETIC);
    commandTypeST.put("lt", C_ARITHMETIC);

    // Memory access commands
    commandTypeST.put("push", C_PUSH);
    commandTypeST.put("pop", C_POP);

    // Branching commands
    commandTypeST.put("label", C_LABEL);
    commandTypeST.put("goto", C_GOTO);
    commandTypeST.put("if-goto", C_IF_GOTO);

    //Function commands
    commandTypeST.put("function", C_FUNCTION);
    commandTypeST.put("return", C_RETURN);
    commandTypeST.put("call", C_CALL);
  }

  private int argsLength = 0;
  public String args[] = new String[3];

  public Parser(String line) {
    int n = line.length();
    StringBuilder buf = new StringBuilder();
    for (int i = 0; i < n; i++) {
      char c = line.charAt(i);
      if (c == ' ' || c == '\t') {
        if (buf.length() > 0) {
          args[argsLength++] = buf.toString();
          buf = new StringBuilder();
        }
        continue;
      }
      if (c == '/' && line.charAt(i + 1) == '/')
        break;

      buf.append(c);
    }
    if (buf.length() > 0) {
      args[argsLength++] = buf.toString();
    }
  }


  public int commandType() {
    return commandTypeST.get(command());
  }

  public String command() {
    return args[0];
  }

  public String[] args() {
    return args;
  }

  public String arg1() {
    if (argsLength > 1)
      return args[1];
    else return null;
  }

  public int arg2() {
    if (argsLength > 2)
      return Integer.parseInt(args[2]);
    return 0;
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append('{');
    for (int i = 0; i < this.argsLength; i++) {
      if (i != 0)
        sb.append(',');
      sb.append(args[i]);

    }
    sb.append('}');
    return sb.toString();
  }

  public static void main(String[] args) {
    Parser parser;
    parser = new Parser("push local 0 // Get bar’s base address");
    System.out.println(parser);
    parser = new Parser("push constant 2");
    System.out.println(parser);
    parser = new Parser("add");
    System.out.println(parser);
    parser = new Parser("pop pointer 1 // Set that’s base to (bar+2)");
    System.out.println(parser);
    parser = new Parser("push constant 19");
    System.out.println(parser);
    parser = new Parser("pop that 0 // *(bar+2)=19");
    System.out.println(parser);

    System.out.println();

    parser = new Parser("push argument 0 // Get b’s base address");
    System.out.println(parser);
    parser = new Parser("pop pointer 0 // Point the this segment to b");
    System.out.println(parser);
    parser = new Parser("push argument 1 // Get r’s value");
    System.out.println(parser);
    parser = new Parser("pop this 2 // Set b’s third field to r");
    System.out.println(parser);


  }


}
