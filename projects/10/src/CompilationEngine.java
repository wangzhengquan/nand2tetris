import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

public class CompilationEngine {
  private JackTokenizer tokenizer;
  private PrintWriter printWriter;
  private int ahead;

  public CompilationEngine(InputStream input, OutputStream output) {
    ahead = 0;
    try {
      tokenizer = new JackTokenizer(input);
      printWriter = new PrintWriter(new OutputStreamWriter(output, StandardCharsets.UTF_8), true);

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void compile() {
    // 每个compile方法,都默认在开始的时候预读取了一个token,结束的时候没有预读取了
    advance();
    compileClass();
    printWriter.close();
  }


  private void advance() {
    if (ahead == 0) {
      if (tokenizer.hasMoreToken()) tokenizer.advance();
      else throw new SyntaxError(tokenizer.row(), tokenizer.col());
    } else if (ahead == 1) {
      ahead--;
    } else throw new SyntaxError(tokenizer.row(), tokenizer.col(), "ahead=" + ahead);
  }

  private void back() {
    ahead++;
  }


  /**
   * compile a complete class
   */
  private void compileClass() {

    if (tokenizer.keyword().equals("class")) {
      printWriter.println("<class>");
      printWriter.println("<keyword> class </keyword>");
      advance();
      printWriter.println("<identifier> " + tokenizer.identifier() + " </identifier>");
      advance();
      if (tokenizer.symbol().equals("{"))
        printWriter.println("<symbol> { </symbol>");
      else throw new SyntaxError(tokenizer.row(), tokenizer.col());

      advance();
      // classVar
      while (tokenizer.keyword().equals("field") || tokenizer.keyword().equals("static")) {
        compileClassVarDec();
        advance();
      }
      //subroutine
      while (tokenizer.val().equals("constructor") || tokenizer.val().equals("function") || tokenizer.val().equals("method")) {
        compileSubroutine();
        advance();
      }

      if (tokenizer.symbol().equals("}"))
        printWriter.println("<symbol> } </symbol>");
      else throw new SyntaxError(tokenizer.row(), tokenizer.col());
      printWriter.println("</class>");
    } else throw new SyntaxError(tokenizer.row(), tokenizer.col());
  }

  /**
   * Compiles a static declaration or a field declaration.
   */
  private void compileClassVarDec() {
    printWriter.println("<classVarDec>");
    compileVarDec();
    printWriter.println("</classVarDec>");

  }

  private void compileVarDec() {
    // field | static | var
    printWriter.println("<keyword> " + tokenizer.keyword() + " </keyword>");
    // type
    advance();
    compileType();
    //varName
    advance();
    printWriter.println("<identifier> " + tokenizer.identifier() + " </identifier>");

    advance();
    while (tokenizer.val().equals(",")) {
      printWriter.println("<symbol> , </symbol>");
      advance();
      printWriter.println("<identifier> " + tokenizer.identifier() + " </identifier>");
      advance();
    }

    if (tokenizer.symbol().equals(";"))
      printWriter.println("<symbol> ; </symbol>");
    else throw new SyntaxError(tokenizer.row(), tokenizer.col(), "Missing ';'");
  }

  /**
   * Compiles a complete method, function, or constructor.
   */
  private void compileSubroutine() {
    printWriter.println("<subroutineDec>");
    // constructor | function | method
    printWriter.println("<keyword> " + tokenizer.keyword() + " </keyword>");
    //void | type
    advance();
    compileType();
    //subroutineName
    advance();
    printWriter.println("<identifier> " + tokenizer.identifier() + " </identifier >");
    //'(' parameterList ')'
    advance();
    if (tokenizer.symbol().equals("(")) {
      compileParameterList();
      advance();
    } else throw new SyntaxError(tokenizer.row(), tokenizer.col(), "expect '('");

    // subroutineBody
    if (tokenizer.symbol().equals("{")) {
      compileSubroutineBody();
    } else throw new SyntaxError(tokenizer.row(), tokenizer.col(), "expect '{'");


    printWriter.println("</subroutineDec>");
  }

  /**
   * Compiles a (possibly empty) parameter list, not including the enclosing ‘‘()’’.
   */
  public void compileParameterList() {
    printWriter.println("<symbol> ( </symbol>");
    printWriter.println("<parameterList>");
    advance();
    if (!tokenizer.val().equals(")")) {
      compileParameterVar();
      advance();
      while (tokenizer.symbol().equals(",")) {
        printWriter.println("<symbol> , </symbol>");
        advance();
        compileParameterVar();
        advance();
      }
    }
    printWriter.println("</parameterList>");
    if (tokenizer.symbol().equals(")"))
      printWriter.println("<symbol> ) </symbol>");
    else throw new SyntaxError(tokenizer.row(), tokenizer.col(), "expect ')'");
  }


  private void compileParameterVar() {
    //type
    compileType();
    //varName
    advance();
    printWriter.println("<identifier> " + tokenizer.identifier() + " </identifier>");
  }

  private void compileType() {
    if (tokenizer.tokenType() == JackTokenizer.TYPE_KEYWORD)
      printWriter.println("<keyword> " + tokenizer.typeKeyword() + " </keyword>");
    else if (tokenizer.tokenType() == JackTokenizer.IDENTIFIER)
      printWriter.println("<identifier> " + tokenizer.identifier() + " </identifier>");
    else throw new SyntaxError(tokenizer.row(), tokenizer.col());
  }


  private void compileSubroutineBody() {
    printWriter.println("<subroutineBody>");
    printWriter.println("<symbol> { </symbol>");
    advance();
    // local var
    while (tokenizer.val().equals("var")) {
      compileLocalVarDec();
      advance();
    }
    // statements
    if (tokenizer.tokenType() == JackTokenizer.STATEMENT_KEYWORD) {
      compileStatements();
      advance();
    }

    if (tokenizer.symbol().equals("}"))
      printWriter.println("<symbol> } </symbol>");
    else throw new SyntaxError(tokenizer.row(), tokenizer.col(), "expect '}'");
    printWriter.println("</subroutineBody>");
  }

  /**
   * Compiles a var declaration.
   */
  private void compileLocalVarDec() {
    printWriter.println("<varDec>");
    compileVarDec();
    printWriter.println("</varDec>");
  }

  /**
   * Compiles a sequence of statements, not including the enclosing {}.
   */
  private void compileStatements() {
    printWriter.println("<statements>");
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
          throw new SyntaxError(tokenizer.row(), tokenizer.col(), "unrecognized statement");
      }
      advance();
    }
    back();

    printWriter.println("</statements>");
  }

  /**
   * Compiles a let statement.
   */
  private void compileLetStatement() {
    printWriter.println("<letStatement>");
    printWriter.println("<keyword> let </keyword>");
    //varName
    advance();
    printWriter.println("<identifier> " + tokenizer.identifier() + " </identifier>");
    //[ expression ]?
    advance();
    if (tokenizer.val().equals("[")) {
      printWriter.println("<symbol> [ </symbol>");
      advance();
      compileExpression();
      advance();
      if (tokenizer.symbol().equals("]"))
        printWriter.println("<symbol> ] </symbol>");
      else throw new SyntaxError(tokenizer.row(), tokenizer.col(), "expect ']'");
      advance();
    }

    if (tokenizer.operator().equals("=")) {
      printWriter.println("<symbol> = </symbol>");
    } else throw new SyntaxError(tokenizer.row(), tokenizer.col());

    advance();
    compileExpression();
    advance();
    if (tokenizer.symbol().equals(";"))
      printWriter.println("<symbol> ; </symbol>");
    else throw new SyntaxError(tokenizer.row(), tokenizer.col(), "Missing ';' ");
    printWriter.println("</letStatement>");

  }

  private void compileIfStatement() {
    printWriter.println("<ifStatement>");
    printWriter.println("<keyword> if </keyword>");
    advance();
    if (tokenizer.symbol().equals("(")) {
      printWriter.println("<symbol> ( </symbol>");
      advance();
    } else throw new SyntaxError(tokenizer.row(), tokenizer.col(), "expect '('");

    compileExpression();
    advance();
    if (tokenizer.symbol().equals(")")) {
      printWriter.println("<symbol> ) </symbol>");
      advance();
    } else throw new SyntaxError(tokenizer.row(), tokenizer.col(), "expect ')'");
    // if body
    if (tokenizer.symbol().equals("{")) {
      printWriter.println("<symbol> { </symbol>");
      advance();
    } else throw new SyntaxError(tokenizer.row(), tokenizer.col(), "expect '{'");

    compileStatements();
    advance();

    // end of if
    if (tokenizer.val().equals("}")) {
      printWriter.println("<symbol> } </symbol>");
      advance();
    } else throw new SyntaxError(tokenizer.row(), tokenizer.col(), "expect '}'");

    // else
    if (tokenizer.val().equals("else")) {
      printWriter.println("<keyword> else </keyword>");
      advance();
      if (tokenizer.symbol().equals("{")) {
        printWriter.println("<symbol> { </symbol>");
        advance();
      } else throw new SyntaxError(tokenizer.row(), tokenizer.col(), "expect '{'");

      compileStatements();
      advance();

      // end of if else
      if (tokenizer.symbol().equals("}"))
        printWriter.println("<symbol> } </symbol>");
      else throw new SyntaxError(tokenizer.row(), tokenizer.col(), "expect '}'");
    } else {
      back();
    }
    printWriter.println("</ifStatement>");
  }

  private void compileWhileStatement() {
    printWriter.println("<whileStatement>");
    printWriter.println("<keyword> while </keyword>");
    advance();
    if (tokenizer.symbol().equals("("))
      printWriter.println("<symbol> ( </symbol>");
    else throw new SyntaxError(tokenizer.row(), tokenizer.col(), "while statement expect '('");
    advance();
    compileExpression();
    advance();
    if (tokenizer.symbol().equals(")"))
      printWriter.println("<symbol> ) </symbol>");
    else throw new SyntaxError(tokenizer.row(), tokenizer.col(), "expect ')'");

    // while body
    advance();
    if (tokenizer.symbol().equals("{"))
      printWriter.println("<symbol> { </symbol>");
    else throw new SyntaxError(tokenizer.row(), tokenizer.col(), "expect '{'");
    advance();

    compileStatements();
    advance();
    if (tokenizer.symbol().equals("}"))
      printWriter.println("<symbol> } </symbol>");
    else throw new SyntaxError(tokenizer.row(), tokenizer.col(), "expect '}'");
    printWriter.println("</whileStatement>");

  }

  private void compileDoStatement() {
    printWriter.println("<doStatement>");
    printWriter.println("<keyword> do </keyword>");
    advance();
    printWriter.println("<identifier> " + tokenizer.identifier() + " </identifier>");
    advance();
    while (tokenizer.symbol().equals(".")) {
      printWriter.println("<symbol> . </symbol>");
      advance();
      printWriter.println("<identifier> " + tokenizer.identifier() + " </identifier>");
      advance();
    }

    if (tokenizer.symbol().equals("("))
      compileExpressionList();
    else throw new SyntaxError(tokenizer.row(), tokenizer.col(), "do expect '('");
    advance();
    if (tokenizer.symbol().equals(";"))
      printWriter.println("<symbol> ; </symbol>");
    else
      throw new SyntaxError(tokenizer.row(), tokenizer.col(), "Missing ';' " + tokenizer.val());


    printWriter.println("</doStatement>");

  }

  private void compileReturnStatement() {
    printWriter.println("<returnStatement>");
    printWriter.println("<keyword> return </keyword>");
    advance();
    if (!tokenizer.val().equals(";")) {
      compileExpression();
      advance();
    }

    if (tokenizer.val().equals(";"))
      printWriter.println("<symbol> ; </symbol>");
    else throw new SyntaxError(tokenizer.row(), tokenizer.col(), "Missing ';'");
    printWriter.println("</returnStatement>");

  }

  /**
   * Compiles an expression.
   */
  private void compileExpression() {
    printWriter.println("<expression>");
    compileTerm();

    advance();
    while (tokenizer.tokenType() == JackTokenizer.OPERATOR) {
      printWriter.println("<symbol> " + XMLUtil.escape(tokenizer.operator()) + " </symbol>");
      advance();
      compileTerm();
      advance();
    }
    back();
    printWriter.println("</expression>");
  }

  /**
   * Compiles a term. This routine is faced with a slight difficulty when trying to decide between some of the alternative parsing rules.
   * Specifically, if the current token is an identifier, the routine must distinguish between a variable, an array entry, and a subroutine call. A single look-ahead token, which may be one of ‘‘[’’, ‘‘(’’, or ‘‘.’’ suffices to dis- tinguish between the three possi- bilities. Any other token is not part of this term and should not be advanced over.
   */
  private void compileTerm() {
    printWriter.println("<term>");

    switch (tokenizer.tokenType()) {
      case JackTokenizer.CONST_KEYWORD:
        printWriter.println("<keyword> " + tokenizer.constKeyword() + " </keyword>");
        break;
      case JackTokenizer.OPERATOR:
        //unaryOp
        if (tokenizer.operator().equals("-") || tokenizer.operator().equals("~")) {
          printWriter.println("<symbol> " + tokenizer.operator() + " </symbol>");
          advance();
          compileTerm();
        }
        break;
      case JackTokenizer.SYMBOL:

        if (tokenizer.symbol().equals("(")) {
          printWriter.println("<symbol> ( </symbol>");
          advance();
          compileExpression();
          advance();
          if (tokenizer.symbol().equals(")"))
            printWriter.println("<symbol> ) </symbol>");
          else throw new SyntaxError(tokenizer.row(), tokenizer.col(), "Missing ')'");
        }
        break;
      case JackTokenizer.IDENTIFIER:
        printWriter.println("<identifier> " + tokenizer.identifier() + " </identifier>");
        advance();
        if (tokenizer.val().equals(".")) {
          printWriter.println("<symbol> . </symbol>");
          advance();
          printWriter.println("<identifier> " + tokenizer.identifier() + " </identifier>");
          advance();
        }

        if (tokenizer.val().equals("[")) {
          //varName[ expression ]
          printWriter.println("<symbol> [ </symbol>");
          advance();
          compileExpression();
          advance();
          if (tokenizer.symbol().equals("]"))
            printWriter.println("<symbol> ] </symbol>");
          else throw new SyntaxError(tokenizer.row(), tokenizer.col(), "expect ']'");
        } else if (tokenizer.val().equals("(")) {
          //subroutineCall
          compileExpressionList();

        } else {
          back();
        }
        break;
      case JackTokenizer.INT_CONST:
        printWriter.println("<integerConstant> " + tokenizer.intConst() + " </integerConstant>");
        break;
      case JackTokenizer.STRING_CONST:
        printWriter.println("<stringConstant> " + tokenizer.stringConst() + " </stringConstant>");
        break;
      default:
        throw new SyntaxError(tokenizer.row(), tokenizer.col());
    }
    printWriter.println("</term>");
  }

  /**
   * Compiles a ( possibly empty) comma-separated list of expressions.
   */
  private void compileExpressionList() {
    printWriter.println("<symbol> ( </symbol>");
    printWriter.println("<expressionList>");

    int i = 0;
    advance();
    while (!tokenizer.val().equals(")")) {
      if (tokenizer.val().equals(",")) {
        printWriter.println("<symbol> , </symbol>");
        advance();
      }
      compileExpression();
      advance();
      i++;
    }
    printWriter.println("</expressionList>");
    if (tokenizer.symbol().equals(")"))
      printWriter.println("<symbol> ) </symbol>");
    else throw new SyntaxError(tokenizer.row(), tokenizer.col(), "expect ')'");

  }

  private void compileExpressionList2() {
    printWriter.println("<symbol> ( </symbol>");
    printWriter.println("<expressionList>");

    advance();
    if (!tokenizer.val().equals(")")) {
      compileExpression();
      advance();
      while (tokenizer.val().equals(",")) {
        printWriter.println("<symbol> , </symbol>");
        advance();
        compileExpression();
        advance();
      }
    }
    printWriter.println("</expressionList>");
    if (tokenizer.symbol().equals(")"))
      printWriter.println("<symbol> ) </symbol>");
    else throw new SyntaxError(tokenizer.row(), tokenizer.col(), "expect ')'");

  }


  public static void main(String[] args) {
    testWhileStatement();
    testSubroutine();
  }


  private static void testLetStatement() {
    System.out.println("---------testLetStatement------");
    String let = "let x = x*(x+1);";
    ByteArrayInputStream in = new ByteArrayInputStream(let.getBytes(StandardCharsets.UTF_8));
    CompilationEngine compiler = new CompilationEngine(in, System.out);
    compiler.advance();
    compiler.compileLetStatement();

  }

  private static void testWhileStatement() {
    System.out.println("---------testWhileStatement------");
    String let = "while(count < 100 & ~(i=9) ) {if(x=1){let z=100; while(z>0){let z=z-1;}} let lim=lime+10; }";
    ByteArrayInputStream in = new ByteArrayInputStream(let.getBytes(StandardCharsets.UTF_8));
    CompilationEngine compiler = new CompilationEngine(in, System.out);
    compiler.advance();
    compiler.compileWhileStatement();

  }

  private static void testSubroutine() {
    System.out.println("---------testSubroutine------");
    String let = "function void main(int a, int b) {var int m, k; var boolean g; do System.out.println(\"Hello world\");}";
    ByteArrayInputStream in = new ByteArrayInputStream(let.getBytes(StandardCharsets.UTF_8));
    CompilationEngine compiler = new CompilationEngine(in, System.out);
    compiler.advance();
    compiler.compileSubroutine();

  }
}
