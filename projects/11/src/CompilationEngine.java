import java.io.File;
import java.io.IOException;
import java.util.Stack;

public class CompilationEngine {
  private JackTokenizer tokenizer;
  //  private PrintWriter printWriter;
  private VMWriter vmWriter;
  private int ahead;

  private VariableTable variableTable;
  private String fileName;

  private static class Subroutine {

    private final static String KIND_CONSTRUCTOR = "constructor";
    private final static String KIND_FUNCTION = "function";
    private final static String KIND_METHOD = "method";

    String name, type, kind;
    private int ifLabelIndex, whileLabelIndex;

    Subroutine() {
    }

    Subroutine(String name, String type, String kind) {
      this.name = name;
      this.type = type;
      this.kind = kind;
    }
  }

  // private Stack<> routineCallStack = new Stack<>();
  Subroutine routine;

  public CompilationEngine(File srcFile, File destFile) {
    ahead = 0;
    try {
      tokenizer = new JackTokenizer(srcFile);
      variableTable = new VariableTable();
      vmWriter = new VMWriter(destFile, variableTable);
      this.fileName = tokenizer.getFileName();
      this.routine = new Subroutine();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }


  public void compile() {
    // 每个compile方法,都默认在开始的时候预读取了一个token,结束的时候没有预读取了
    advance();
    compileClass();
    vmWriter.close();
  }


  private void advance() {
    if (ahead == 0) {
      if (tokenizer.hasMoreToken()) tokenizer.advance();
      else reportError("");
    } else if (ahead == 1) {
      ahead--;
    } else reportError("ahead=" + ahead);
  }

  private void back() {
    ahead++;
  }


  /**
   * compile a complete class
   */
  private void compileClass() {

    if (tokenizer.keyword().equals("class")) {
      advance();
      String className = tokenizer.identifier();
      if (!className.equals(fileName)) {
        reportError("The class name doesn't match the file name.");
      }
      vmWriter.writeComment("class className");
      advance();
      if (!tokenizer.symbol().equals("{"))
        reportError("");

      advance();
      // classVar
      while (tokenizer.val().equals("field") || tokenizer.val().equals("static")) {
        compileClassVarDec();
        advance();
      }
      //Subroutine
      while (tokenizer.val().equals(Subroutine.KIND_CONSTRUCTOR) || tokenizer.val().equals(Subroutine.KIND_FUNCTION) || tokenizer.val().equals(Subroutine.KIND_METHOD)) {
        compileSubroutine();
        advance();
      }

      if (!tokenizer.symbol().equals("}")) {
        reportError("");
      }
    } else reportError("");
  }

  /**
   * Compiles a static declaration or a field declaration.
   */
  private void compileClassVarDec() {
    compileVarDec();

  }

  private void compileVarDec() {
    // field | static | var
    String kind = tokenizer.keyword();
    // type
    advance();
    String type = compileType();
    //varName
    advance();
    String varName = tokenizer.identifier();
    try {
      variableTable.put(varName, type, kind);
    } catch (Exception e) {
      reportError(e.getMessage());
    }
    advance();
    while (tokenizer.val().equals(",")) {
      advance();
      varName = tokenizer.identifier();
      try {
        variableTable.put(varName, type, kind);
      } catch (Exception e) {

        reportError(e.getMessage());
      }
      advance();
    }

    if (!tokenizer.symbol().equals(";"))
      reportError("Missing ';'");
  }

  /**
   * Compiles a complete method, function, or constructor.
   */
  private void compileSubroutine() {

    // constructor | function | method
    String kind = tokenizer.keyword();
    if (kind.equals(Subroutine.KIND_METHOD))
      variableTable.startSubroutine(1);
    else variableTable.startSubroutine(0);


    //void | type
    advance();
    String type = compileType();
    //SubroutineName
    advance();
    String subroutineName = this.fileName + "." + tokenizer.identifier();
    //'(' parameterList ')'
    advance();
    if (tokenizer.symbol().equals("(")) {
      compileParameterList();
      advance();
    } else reportError("expect '('");

    // SubroutineBody
    if (tokenizer.symbol().equals("{")) {
      vmWriter.writeComment(kind + " " + type + " " + subroutineName);
      routine.name = subroutineName;
      routine.type = type;
      routine.kind = kind;
      routine.ifLabelIndex = 0;
      routine.whileLabelIndex = 0;
      compileSubroutineBody();
    } else reportError("expect '{'");
  }

  /**
   * Compiles a (possibly empty) parameter list, not including the enclosing ‘‘()’’.
   */
  public void compileParameterList() {
    advance();
    if (!tokenizer.val().equals(")")) {
      compileParameterVar();
      advance();
      while (tokenizer.symbol().equals(",")) {
        advance();
        compileParameterVar();
        advance();
      }
    }
    if (!tokenizer.symbol().equals(")"))
      reportError("expect ')'");
  }


  private void compileParameterVar() {
    String kind = Variable.KIND_ARG;
    //type
    String type = compileType();
    //varName
    advance();
    String varName = tokenizer.identifier();
    try {
      this.variableTable.put(varName, type, kind);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private String compileType() {
    if (tokenizer.tokenType() == JackTokenizer.TYPE_KEYWORD || tokenizer.tokenType() == JackTokenizer.IDENTIFIER)
      return tokenizer.val();
    else {
      reportError("");
      return null;
    }
  }


  private void compileSubroutineBody() {
    advance();
    // local var
    while (tokenizer.val().equals("var")) {
      compileLocalVarDec();
      advance();
    }

    int nVar = variableTable.varCount(Variable.KIND_LOCAL);
    vmWriter.writeFunction(routine.name, nVar);
    if (routine.kind.equals(Subroutine.KIND_METHOD)) {
      // set THIS address
      // push argument 0
      vmWriter.writePush(VMWriter.SEG_ARG, 0);
      // pop pointer 0
      vmWriter.writePop(VMWriter.SEG_POINTER, 0);
    } else if (routine.kind.equals(Subroutine.KIND_CONSTRUCTOR)) {
      vmWriter.writePush(VMWriter.SEG_CONST, variableTable.varCount(Variable.KIND_FIELD));
      vmWriter.writeCall("Memory.alloc", 1);
      // assign alloc address to THIS
      vmWriter.writePop(VMWriter.SEG_POINTER, 0);
    }
    // statements
    if (tokenizer.tokenType() == JackTokenizer.STATEMENT_KEYWORD) {
      compileStatements();
      advance();
    }

    if (!tokenizer.symbol().equals("}"))
      reportError("expect '}'");
  }

  /**
   * Compiles a var declaration.
   */
  private void compileLocalVarDec() {
    compileVarDec();
  }

  /**
   * Compiles a sequence of statements, not including the enclosing {}.
   */
  private void compileStatements() {
    while (tokenizer.tokenType() == JackTokenizer.STATEMENT_KEYWORD) {
      switch (tokenizer.statementKeyword()) {
        case "let":
          compileLetStatement();
          break;
        case "if":
          compileIfStatement();
          break;
        case "while":
          compileWhileStatement();
          break;
        case "do":
          compileDoStatement();
          break;
        case "return":
          compileReturnStatement();
          break;
        default:
          reportError("unrecognized statement");
      }
      advance();
    }
    back();

  }

  /**
   * Compiles a let statement.
   */
  private void compileLetStatement() {
    boolean assignToArray = false;
    //varName
    advance();
    String varName = tokenizer.identifier();

    // arr[ expression ]
    advance();
    if (tokenizer.val().equals("[")) {
      vmWriter.writePushVar(varName);
      advance();
      compileExpression();
      advance();
      if (!tokenizer.symbol().equals("]"))
        reportError("expect ']'");
      // compute arr[expression] address
      vmWriter.writeArithmetic("add");
      assignToArray = true;
      advance();
    }

    if (!tokenizer.operator().equals("="))
      reportError("");

    advance();
    compileExpression();
    if (assignToArray) {
      vmWriter.writePop(VMWriter.SEG_TEMP, 0);
      vmWriter.writePop(VMWriter.SEG_POINTER, 1);
      vmWriter.writePush(VMWriter.SEG_TEMP, 0);
      vmWriter.writePop(VMWriter.SEG_THAT, 0);
    } else {
      vmWriter.writePopVar(varName);
    }

    advance();
    if (!tokenizer.symbol().equals(";"))
      reportError("Missing ';' ");

  }

  private void compileIfStatement() {
    compileIfStatement3();
  }

  private void compileIfStatement3() {
    int labelIndex = routine.ifLabelIndex++;
    int ifBranchIdx = 0;
    advance();
    if (tokenizer.symbol().equals("(")) {
      advance();
      compileExpression();
      advance();
      if (tokenizer.symbol().equals(")")) {
        advance();
      } else reportError("expect ')'");
    } else reportError("expect '('");


    // if body
    if (tokenizer.symbol().equals("{")) {
      vmWriter.writeArithmetic("not");
      vmWriter.writeIfGoto("IF_ELSE_" + labelIndex + "_" + ifBranchIdx);
      advance();
      compileStatements();
      advance();
      vmWriter.writeGoto("IF_END" + labelIndex);
      if (tokenizer.val().equals("}")) {
        advance();
      } else reportError("expect '}'");
    } else reportError("expect '{'");

    boolean hasElse = false;
    boolean elseEnd = false;

    // else if
    while (tokenizer.val().equals("else")) {
      if (elseEnd)
        reportError("illegal start of expression ,else end");
      hasElse = true;
      advance();
      if (tokenizer.val().equals("if")) {
        vmWriter.writeLabel("IF_ELSE_" + labelIndex + "_" + ifBranchIdx);
        ifBranchIdx++;
        // else if
        advance();
        if (tokenizer.symbol().equals("(")) {
          // else if expression
          advance();
          compileExpression();
          advance();
          if (tokenizer.symbol().equals(")")) {
            advance();
          } else reportError("expect ')'");
        } else reportError("expect '('");

        if (tokenizer.symbol().equals("{")) {
          // else if body
          vmWriter.writeArithmetic("not");
          vmWriter.writeIfGoto("IF_ELSE_" + labelIndex + "_" + ifBranchIdx);
          advance();
          compileStatements();
          advance();
          vmWriter.writeGoto("IF_END" + labelIndex);
          if (!tokenizer.symbol().equals("}"))
            reportError("expect '}'");
        } else reportError("expect '{'");
        advance();
      } else if (tokenizer.val().equals("{")) {
        // else
        vmWriter.writeLabel("IF_ELSE_" + labelIndex + "_" + ifBranchIdx);
        ifBranchIdx++;
        advance();
        compileStatements();
        advance();
        elseEnd = true;
        if (!tokenizer.symbol().equals("}"))
          reportError("expect '}'");
        advance();
      } else {
        reportError("expect '{'");
      }
    }
    vmWriter.writeLabel("IF_END" + labelIndex);
    if (!hasElse) {
      vmWriter.writeLabel("IF_ELSE_" + labelIndex + "_" + ifBranchIdx);
      ifBranchIdx++;
    }
    back();
  }

  private void compileIfStatement1() {
    int labelIndex = routine.ifLabelIndex++;
    advance();
    if (tokenizer.symbol().equals("(")) {
      advance();
    } else reportError("expect '('");

    compileExpression();
    advance();
    if (tokenizer.symbol().equals(")")) {
      advance();
    } else reportError("expect ')'");
    vmWriter.writeIfGoto("IF_TRUE" + labelIndex);
    vmWriter.writeGoto("IF_FALSE" + labelIndex);
    // if body
    if (tokenizer.symbol().equals("{")) {
      vmWriter.writeLabel("IF_TRUE" + labelIndex);
      advance();
    } else reportError("expect '{'");

    compileStatements();
    advance();

    if (tokenizer.val().equals("}")) {
      advance();
    } else reportError("expect '}'");

    // else
    if (tokenizer.val().equals("else")) {
      vmWriter.writeGoto("IF_END" + labelIndex);
      vmWriter.writeLabel("IF_FALSE" + labelIndex);
      advance();
      if (tokenizer.symbol().equals("{")) {
        advance();
      } else reportError("expect '{'");

      compileStatements();
      advance();

      // end of if else
      if (tokenizer.symbol().equals("}"))
        vmWriter.writeLabel("IF_END" + labelIndex);
      else reportError("expect '}'");
    } else {
      vmWriter.writeLabel("IF_FALSE" + labelIndex);
      back();
    }

  }


  private void compileWhileStatement() {
    int labelIndex = routine.whileLabelIndex++;
    advance();
    if (tokenizer.symbol().equals("("))
      advance();
    else reportError("while statement expect '('");

    vmWriter.writeLabel("WHILE_EXP" + labelIndex);
    compileExpression();
    vmWriter.writeArithmetic("not");
    vmWriter.writeIfGoto("WHILE_END" + labelIndex);

    advance();
    if (tokenizer.symbol().equals(")"))
      advance();
    else reportError("expect ')'");

    // while body
    if (tokenizer.symbol().equals("{"))
      advance();
    else reportError("expect '{'");

    compileStatements();
    advance();
    if (tokenizer.symbol().equals("}")) {
      vmWriter.writeGoto("WHILE_EXP" + labelIndex);
    } else reportError("expect '}'");
    vmWriter.writeLabel("WHILE_END" + labelIndex);

  }


  private void compileDoStatement() {
    advance();
    String name = tokenizer.identifier();
    advance();
    String functionName = null;
    if (tokenizer.symbol().equals(".")) {
      advance();
      functionName = tokenizer.identifier();
      advance();
    }

    if (tokenizer.symbol().equals("(")) {
      if (functionName != null) {
        Variable var = variableTable.get(name);
        if (var != null) {
          // object.method()
          vmWriter.writePushVar(var);
          int nArg = compileExpressionList();
          vmWriter.writeCall(var.type + "." + functionName, nArg + 1);
        } else {
          //static call
          int nArg = compileExpressionList();
          vmWriter.writeCall(name + "." + functionName, nArg);
        }

      } else {
        //method()
        String methodName = this.fileName + "." + name;
        if (routine.kind.equals(Subroutine.KIND_FUNCTION)) {
          reportError(
              methodName + " called as a method from within the function " + routine.name);
        }
        vmWriter.writePush(VMWriter.SEG_POINTER, 0);
        int nArg = compileExpressionList();
        vmWriter.writeCall(methodName, nArg + 1);
      }
    } else reportError("do expect '('");
    // drop return val
    vmWriter.writePop(VMWriter.SEG_TEMP, 0);
    advance();
    if (!tokenizer.symbol().equals(";"))
      reportError("Missing ';' " + tokenizer.val());
  }

  private void compileReturnStatement() {
    advance();
    if (tokenizer.val().equals(";")) {
      // return void;
      if (!routine.type.equals("void")) {
        reportError("In subroutine " + routine.name + ", A non-void function must return a value. ");
      }
      vmWriter.writePush(VMWriter.SEG_CONST, 0);
    } else {
      if (routine.type.equals("void")) {
        reportError("In subroutine " + routine.name + ", A void function can not return a value. ");
      }
      compileExpression();
      advance();
    }
    vmWriter.writeReturn();
    if (!tokenizer.val().equals(";"))
      reportError("Missing ';'");

  }

  /**
   * Compiles an expression.
   */
  private void compileExpression() {
    Stack<String> stack = new Stack<>();
    compileTerm();
    advance();
    while (tokenizer.tokenType() == JackTokenizer.OPERATOR) {
      String op = tokenizer.operator();

      while (!stack.isEmpty() && Operator.getOperator(stack.peek()).precedence >= Operator.getOperator(op).precedence) {
        vmWriter.writeArithmetic(stack.pop());
      }
      stack.push(op);

      advance();
      compileTerm();
      advance();
    }
    while (!stack.isEmpty()) {
      vmWriter.writeArithmetic(stack.pop());
    }

    back();
  }

  /**
   * Compiles a term. This routine is faced with a slight difficulty when trying to decide between some of the alternative parsing rules.
   * Specifically, if the current token is an identifier, the routine must distinguish between a variable, an array entry, and a Subroutine call. A single look-ahead token, which may be one of ‘‘[’’, ‘‘(’’, or ‘‘.’’ suffices to dis- tinguish between the three possi- bilities. Any other token is not part of this term and should not be advanced over.
   */
  private void compileTerm() {
//    Stack<String> stack = new Stack<String>();
    switch (tokenizer.tokenType()) {
      case JackTokenizer.CONST_KEYWORD:
        String token = tokenizer.constKeyword();
        if (token.equals("this")) {
          vmWriter.writePush(VMWriter.SEG_POINTER, 0);
        } else if (token.equals("true")) {
          vmWriter.writePush(VMWriter.SEG_CONST, 0);
          vmWriter.writeArithmetic("not");
        } else if (token.equals("false") || token.equals("null"))
          vmWriter.writePush(VMWriter.SEG_CONST, 0);
        else reportError("Unknown const keyword " + token);

        break;
      case JackTokenizer.OPERATOR:
        //unaryOp
        String op = tokenizer.operator();
        advance();
        compileTerm();
        if (op.equals("-")) {
          vmWriter.writeArithmetic("neg");
        } else if (op.equals("~")) {
          vmWriter.writeArithmetic("not");
        } else
          reportError("Unknown Operator " + op);
        break;
      case JackTokenizer.SYMBOL:
        // (expression)
        if (tokenizer.symbol().equals("(")) {
          advance();
          compileExpression();
          advance();
          if (!tokenizer.symbol().equals(")"))
            reportError("Missing ')'");
        }
        break;
      case JackTokenizer.IDENTIFIER:
        String name = tokenizer.identifier();
        advance();
        String functionName = null;
        if (tokenizer.val().equals(".")) {
          advance();
          functionName = tokenizer.identifier();
          advance();
        }

        if (tokenizer.val().equals("[")) {
          //arr[ expression ]
          advance();
          compileExpression();
          advance();
          if (!tokenizer.symbol().equals("]"))
            reportError("expect ']'");
          // compute the arr[expression] address
          vmWriter.writePushVar(name);
          vmWriter.writeArithmetic("add");
          vmWriter.writePop(VMWriter.SEG_POINTER, 1);
          vmWriter.writePush(VMWriter.SEG_THAT, 0);
        } else if (tokenizer.val().equals("(")) {
          //SubroutineCall
          if (functionName != null) {
            Variable var = variableTable.get(name);
            if (var != null) {
              // object.method()
              vmWriter.writePushVar(var);
              int nArg = compileExpressionList();
              vmWriter.writeCall(var.type + "." + functionName, nArg + 1);
            } else {
              //static call
              int nArg = compileExpressionList();
              vmWriter.writeCall(name + "." + functionName, nArg);
            }

          } else {
            //method()
            vmWriter.writePush(VMWriter.SEG_POINTER, 0);
            int nArg = compileExpressionList();
            vmWriter.writeCall(this.fileName + "." + name, nArg + 1);
          }
        } else {
          vmWriter.writePushVar(name);
          back();
        }
        break;
      case JackTokenizer.INT_CONST:
        vmWriter.writePush(VMWriter.SEG_CONST, tokenizer.intConst());
        break;
      case JackTokenizer.STRING_CONST:
        vmWriter.writePushStringConst(tokenizer.stringConst());
        break;
      default:
        reportError("");
    }
  }

  /**
   * Compiles a ( possibly empty) comma-separated list of expressions.
   */
  private int compileExpressionList() {
    int i = 0;
    advance();
    while (!tokenizer.val().equals(")")) {
      if (tokenizer.val().equals(",")) {
        advance();
      }
      compileExpression();
      advance();
      i++;
    }
    if (!tokenizer.symbol().equals(")"))
      reportError("expect ')'");
    return i;
  }

  private void reportError(String msg) {
    String errorMsg = "In file " + fileName + ".jack (row " + (tokenizer.row() + 1) + ", col " + (tokenizer.col() + 1) + ") " + ", In subroutine " + routine.name;
    if (msg != null && !msg.isEmpty()) {
      errorMsg = errorMsg + ", " + msg;
    }
    throw new SyntaxError(errorMsg);
  }

}
