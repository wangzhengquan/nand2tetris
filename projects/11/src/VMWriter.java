import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

public class VMWriter {
  public final static String SEG_CONST = "constant";
  public final static String SEG_ARG = "argument";
  public final static String SEG_LOCAL = "local";
  public final static String SEG_STATIC = "static";
  public final static String SEG_THIS = "this";
  public final static String SEG_THAT = "that";
  public final static String SEG_POINTER = "pointer";
  public final static String SEG_TEMP = "temp";

  private VariableTable variableTable;
  private final static SymbolSET segmentSet = new SymbolSET();
//  private static SymbolMap<String> operatorMap = new SymbolMap<>();
//
//  static {
//    operatorMap.put("+", "add");
//    operatorMap.put("-", "sub");
//
//    operatorMap.put("|", "or");
//    operatorMap.put("&", "and");
//    //operatorMap.put("~", "not");
//    //operatorMap.put("-", "neg");
//
//    operatorMap.put("=", "eq");
//    operatorMap.put(">", "gt");
//    operatorMap.put("<", "lt");
//  }

  static {
    segmentSet.add(SEG_CONST);
    segmentSet.add(SEG_ARG);
    segmentSet.add(SEG_LOCAL);
    segmentSet.add(SEG_STATIC);
    segmentSet.add(SEG_THIS);
    segmentSet.add(SEG_THAT);
    segmentSet.add(SEG_POINTER);
    segmentSet.add(SEG_TEMP);
  }

  private PrintWriter printWriter;

  public VMWriter(File vmFile, VariableTable variableTable) {
    try {
      this.printWriter = new PrintWriter(new OutputStreamWriter(new FileOutputStream(vmFile), StandardCharsets.UTF_8), true);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    this.variableTable = variableTable;
  }

  public void writePushVar(String name) {
    writePushVar(variableTable.get(name));
  }

  public void writePushVar(Variable var) {
    switch (var.kind) {
      case Variable.KIND_STATIC:
        writePush(SEG_STATIC, var.index);
        break;
      case Variable.KIND_FIELD:
        writePush(SEG_THIS, var.index);
        break;
      case Variable.KIND_ARG:
        writePush(SEG_ARG, var.index);
        break;
      case Variable.KIND_LOCAL:
        writePush(SEG_LOCAL, var.index);
        break;
      default:
        throw new IllegalArgumentException("Invalid variable kind\n" + var.name);
    }
  }

  public void writePopVar(String name) {
    writePopVar(variableTable.get(name));
  }

  public void writePopVar(Variable var) {
    switch (var.kind) {
      case Variable.KIND_STATIC:
        writePop(SEG_STATIC, var.index);
        break;
      case Variable.KIND_FIELD:
        writePop(SEG_THIS, var.index);
        break;
      case Variable.KIND_ARG:
        writePop(SEG_ARG, var.index);
        break;
      case Variable.KIND_LOCAL:
        writePop(SEG_LOCAL, var.index);
        break;
      default:
        throw new IllegalArgumentException("Invalid variable kind\n" + var.name);
    }
  }

  /**
   * @param segment (constant, argument, local, static, this, that, pointer, temp)
   * @param index
   */
  public void writePush(String segment, int index) {
    if (!segmentSet.contains(segment)) {
      throw new IllegalArgumentException("Invalid segment\n" + segment);
    }
    if (segment.equals(SEG_CONST) && index < 0) {
      writePush(VMWriter.SEG_CONST, -index);
      writeArithmetic("neg");
    } else {
      printWriter.println("push " + segment + " " + index);
    }

  }


  public void writePop(String segment, int index) {
    if (!segmentSet.contains(segment)) {
      throw new IllegalArgumentException("Invalid segment\n" + segment);
    }
    printWriter.println("pop " + segment + " " + index);
  }

  /**
   * command (add, sub, neg, eq, gt, lt, and, or, not)
   */
  public void writeArithmetic(String symbol) {
    switch (symbol) {
      case "*":
        writeCall("Math.multiply", 2);
        break;
      case "/":
        writeCall("Math.divide", 2);
        break;
      default:
        Operator op = Operator.getOperator(symbol);
        if (op == null)
          printWriter.println(symbol);
        else
          printWriter.println(op.command);
        break;
    }
  }

  public void writeLabel(String label) {
    printWriter.println("label " + label);
  }

  public void writeGoto(String label) {
    printWriter.println("goto " + label);
  }

  public void writeIfGoto(String label) {
    printWriter.println("if-goto " + label);
  }

  public void writeCall(String functionName, int nArgs) {
    printWriter.println("call " + functionName + " " + nArgs);
  }

  public void writeFunction(String functionName, int nVars) {
    printWriter.println("function " + functionName + " " + nVars);
  }

  public void writeReturn() {
    printWriter.println("return");
  }

  public void writePushStringConst(String str) {
    writePush(VMWriter.SEG_CONST, str.length());
    writeCall("String.new", 1);
    for (int i = 0; i < str.length(); i++) {
      writePush(VMWriter.SEG_CONST, str.charAt(i));
      writeCall("String.appendChar", 2);
    }
  }

  public void writeComment(String comment) {
    //printWriter.println("// " + comment);
  }

  public void write(String str) {
    printWriter.print(str);
  }

  public void close() {
    this.printWriter.close();
  }


  public static void main(String[] args) {
  }
}
