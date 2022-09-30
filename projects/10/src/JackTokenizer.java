import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Queue;
import java.util.regex.Pattern;

public class JackTokenizer {
  private int row, col;

  public final static int KEYWORD = 1;
  public final static int STATEMENT_KEYWORD = 2;
  public final static int TYPE_KEYWORD = 3;
  public final static int CONST_KEYWORD = 4;
  public final static int OPERATOR = 5;
  public final static int SYMBOL = 6;
  public final static int IDENTIFIER = 8;
  public final static int INT_CONST = 10;
  public final static int STRING_CONST = 12;


  public final static Pattern identifierPattern = Pattern.compile("^_?[A-Za-z](\\w)*$");

  public final static SymbolSET symbolSet = new SymbolSET();
  public final static SymbolSET typeKeywordSet = new SymbolSET();
  public final static SymbolSET keywordSet = new SymbolSET();
  public final static SymbolSET constKeywordSet = new SymbolSET();
  public final static SymbolSET statementKeywordSet = new SymbolSET();
  public final static SymbolSET operatorSet = new SymbolSET();

//  private final static Map<String, Integer> operatorMap = new TreeMap<>();
//
//  static {
//    // <symbol, precedence>
//    operatorMap.put("~", 3); // Bitwise negation
//    operatorMap.put("-", 3); // Unary minus (negative sign)
//    operatorMap.put("+", 3); // Unary plus (positive sign)
//
//    operatorMap.put("*", 5); //Multiply
//    operatorMap.put("/", 5); //Divide
//    operatorMap.put("plus", 6); //Addition
//    operatorMap.put("minus", 6); //Subtraction
//
//    operatorMap.put("<", 8); //Less than
//    operatorMap.put(">", 8); //Greater than
//    operatorMap.put("==", 9); // Equal to
//    operatorMap.put("&", 10); // Bitwise AND
//    operatorMap.put("|", 12); //Bitwise OR
//
//    operatorMap.put("=", 16); // Simple assignment
//  }

  static {
    symbolSet.add("{");
    symbolSet.add("}");
    symbolSet.add("(");
    symbolSet.add(")");
    symbolSet.add("[");
    symbolSet.add("]");
    symbolSet.add(".");
    symbolSet.add(",");
    symbolSet.add(";");
  }

  static {
    operatorSet.add("+");
    operatorSet.add("-");
    operatorSet.add("*");
    operatorSet.add("/");
    operatorSet.add("&");
    operatorSet.add("|");
    operatorSet.add("<");
    operatorSet.add(">");
    operatorSet.add("=");
    operatorSet.add("~");
  }

  static {
    keywordSet.add("class");
    keywordSet.add("constructor");
    keywordSet.add("function");
    keywordSet.add("method");
    keywordSet.add("field");
    keywordSet.add("static");
    keywordSet.add("var");

  }

  static {
    statementKeywordSet.add("let");
    statementKeywordSet.add("do");
    statementKeywordSet.add("if");
    statementKeywordSet.add("else");
    statementKeywordSet.add("while");
    statementKeywordSet.add("return");
  }

  static {
    constKeywordSet.add("true");
    constKeywordSet.add("false");
    constKeywordSet.add("null");
    constKeywordSet.add("this");
  }

  static {
    typeKeywordSet.add("int");
    typeKeywordSet.add("char");
    typeKeywordSet.add("boolean");
    typeKeywordSet.add("void");
  }

  private StringBuilder tokenBuf = new StringBuilder();
  private Queue<String> tokenQueue = new ArrayDeque<>();

  private Iterator<String> tokenIterator;
  private String token;
//  private String nextToken;

  private Queue<Integer> readAheadQueue = new ArrayDeque<>();

  private BufferedInputStream bufIn;

  public JackTokenizer(String jackFile) throws IOException {
    this(new FileInputStream(jackFile));
  }


  public JackTokenizer(InputStream input) throws IOException {
    this.row = 0;
    this.col = 0;
    bufIn = new BufferedInputStream(input);
    int k = readNext();
    while (k != -1) {
      char c = (char) k;
      // System.out.print(k + " ");
      if (c == '/') {
        // deal with comment
        k = readNext();
        c = (char) k;
        if (c == '/') {
          // '//' comment
          readUntilEndOfLine();
          k = readNext();
          continue;
        } else if (c == '*') {
          // '/* */' comment
          readUntilEndOfComment();
          k = readNext();
          continue;
        } else {
          pushBack(k);
          c = '/';
        }
      }

      if (Character.isWhitespace(c)) {
        if (tokenBuf.length() > 0) {
          tokenQueue.add(tokenBuf.toString());
          tokenBuf = new StringBuilder();
        }
      } else if (c == '\"') {
        if (tokenBuf.length() > 0) {
          tokenQueue.add(tokenBuf.toString());
          tokenBuf = new StringBuilder();
        }
        tokenQueue.add(readStringConst());
      } else if (symbolSet.contains(String.valueOf(c)) || operatorSet.contains(String.valueOf(c))) {
        if (tokenBuf.length() > 0) {
          tokenQueue.add(tokenBuf.toString());
          tokenBuf = new StringBuilder();
        }
        tokenQueue.add(String.valueOf(c));
      } else {
        tokenBuf.append(c);
      }

      k = readNext();

    } // while end

    if (tokenBuf.length() > 0)
      tokenQueue.add(tokenBuf.toString());

    bufIn.close();
    // input.close();

    tokenIterator = tokenQueue.iterator();
  }

  private void pushBack(int k) {
    if (--col == -1) {
      row--;
      col = 0;
    }
    readAheadQueue.add(k);
  }


  private int readNext() throws IOException {
    int k;
    if (!readAheadQueue.isEmpty()) {
      k = readAheadQueue.poll();
    } else {
      k = bufIn.read();
    }
    if (k == '\n') {
      row++;
      col = 0;
    } else {
      col++;
    }
    return k;
  }

  public int row() {
    return row;
  }

  public int col() {
    return col;
  }

  public boolean hasMoreToken() {
    //System.lineSeparator();
    return tokenIterator.hasNext();
  }

  public void advance() {
    this.token = tokenIterator.next();
  }

  public int tokenType() {
    if (constKeywordSet.contains(token)) {
      return CONST_KEYWORD;
    } else if (statementKeywordSet.contains(token)) {
      return STATEMENT_KEYWORD;
    } else if (typeKeywordSet.contains(token)) {
      return TYPE_KEYWORD;
    } else if (keywordSet.contains(token)) {
      return KEYWORD;
    } else if (symbolSet.contains(token)) {
      return SYMBOL;
    } else if (operatorSet.contains(token)) {
      return OPERATOR;
    } else if (isStringConst(token)) {
      return STRING_CONST;
    } else if (isIdentifier(token)) {
      return IDENTIFIER;
    } else if (isIntegerConst(token)) {
      return INT_CONST;
    } else {
      throw new SyntaxError(row, col, "Unrecognized token " + token);
    }
  }

  public String val() {
    return token;
  }

  public String keyword() {
    if (tokenType() == KEYWORD)
      return token;
    else throw new SyntaxError(row, col, token + " is not a keyword, it is type of " + tokenType());
  }

  public String typeKeyword() {
    if (tokenType() == TYPE_KEYWORD)
      return token;
    else throw new SyntaxError(row, col, token + " is not a typeKeyword, it is type of " + tokenType());
  }

  public String statementKeyword() {
    if (tokenType() == STATEMENT_KEYWORD)
      return token;
    else throw new SyntaxError(row, col);
  }


  public String constKeyword() {
    if (tokenType() == CONST_KEYWORD)
      return token;
    else throw new SyntaxError(row, col);
  }

  public String stringConst() {
    if (tokenType() == STRING_CONST)
      return token.substring(1, token.length() - 1);
    else throw new SyntaxError(row, col);
  }

  public int intConst() {
    if (tokenType() == INT_CONST)
      return Integer.parseInt(token);
    else throw new SyntaxError(row, col, token + " is not a intConst, it is type of " + tokenType());
  }

  public String identifier() {
    if (tokenType() == IDENTIFIER)
      return token;
    else throw new SyntaxError(row, col, token + " is not a operator, it is type of " + tokenType());
  }

  public String operator() {
    if (tokenType() == OPERATOR)
      return token;
    else throw new SyntaxError(row, col, token + " is not a operator, it is type of " + tokenType());
  }

  public String symbol() {
    if (tokenType() == SYMBOL)
      return token;
    else throw new SyntaxError(row, col, token + " is not a symbol, it is type of " + tokenType());
  }

  private static boolean isIdentifier(String token) {
    //  System.out.println("|" + token + "|" + identifierPattern.matcher(token).matches());
    return identifierPattern.matcher(token).matches();
  }

  private static boolean isStringConst(String token) {
    return token.startsWith("\"") && token.endsWith("\"");
  }

  private static boolean isNumericConst(String token) {
    if (token == null) {
      return false;
    }
    try {
      Double.parseDouble(token);
    } catch (NumberFormatException nfe) {
      return false;
    }
    return true;
  }

  private static boolean isIntegerConst(String token) {
    if (token == null) {
      return false;
    }
    try {
      Integer.parseInt(token);
    } catch (NumberFormatException nfe) {
      return false;
    }
    return true;
  }

  private String readStringConst() throws IOException {
    StringBuilder buf = new StringBuilder();
    buf.append("\"");
    int k = readNext();
    while (k != -1) {
      char c = (char) k;
      if (c == '\n') {
        throw new SyntaxError(row, col);
      }
      if (c == '\"') {
        break;
      }
      buf.append(c);
      k = readNext();
    }
    buf.append("\"");
    return buf.toString();
  }

  private void readUntilEndOfComment() throws IOException {
    int k = readNext();
    while (k != -1) {
      char c = (char) k;
      if (c == '*') {
        c = (char) readNext();
        if (c == '/') break;
      }
      k = readNext();
    }
  }

  private void readUntilEndOfLine() throws IOException {
    int k = readNext();
    while (k != -1) {
      char c = (char) k;
      if (c == '\n') break;
      k = readNext();
    }
  }

  public static void parse(String src) {
    File srcFile = new File(src);
    if (!srcFile.exists()) {
      throw new IllegalArgumentException("File dos not exists : " + src + "\n");
    }
    if (srcFile.isDirectory()) {
      File[] srcFiles = srcFile.listFiles(new FilenameFilter() {
        public boolean accept(File dir, String name) {
          return name.endsWith(".jack");
        }
      });
      if (srcFiles == null)
        return;

      for (File file : srcFiles) {
        String name = file.getName();
        name = name.substring(0, name.lastIndexOf(".jack"));
        File destFile = new File(src, name + "T.xml.out");
        try {
          System.out.println("generate " + destFile.getAbsolutePath());
          parse(new FileInputStream(file), new FileOutputStream(destFile));
        } catch (IOException e) {
          e.printStackTrace();
        }
      }

    } else {

      String dir = srcFile.getParent();
      String name = srcFile.getName();
      if (!name.endsWith(".jack")) {
        throw new IllegalArgumentException("It should be .jack file");
      }
      name = name.substring(0, name.lastIndexOf(".jack"));
      File destFile = new File(dir, name + "T.xml.out");
      try {
        System.out.println("generate " + destFile.getAbsolutePath());
        parse(new FileInputStream(srcFile), new FileOutputStream(destFile));
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public static void parse(InputStream in, OutputStream out) throws IOException {
    JackTokenizer tokenizer = new JackTokenizer(in);
    PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8), false);
    // out = new PrintWriter(new OutputStreamWriter(System.out, "UTF-8"), true);
    printWriter.println("<tokens>");
    while (tokenizer.hasMoreToken()) {
      tokenizer.advance();
      switch (tokenizer.tokenType()) {
        case KEYWORD:
          printWriter.println("<keyword> " + tokenizer.keyword() + " </keyword>");
          break;
        case CONST_KEYWORD:
          printWriter.println("<keyword> " + tokenizer.constKeyword() + " </keyword>");
          break;
        case STATEMENT_KEYWORD:
          printWriter.println("<keyword> " + tokenizer.statementKeyword() + " </keyword>");
          break;
        case TYPE_KEYWORD:
          printWriter.println("<keyword> " + tokenizer.typeKeyword() + " </keyword>");
          break;
        case SYMBOL:
          printWriter.println("<symbol> " + XMLUtil.escape(tokenizer.symbol()) + " </symbol>");
          break;
        case OPERATOR:
          printWriter.println("<symbol> " + XMLUtil.escape(tokenizer.operator()) + " </symbol>");
          break;
        case IDENTIFIER:
          printWriter.println("<identifier> " + tokenizer.identifier() + " </identifier>");
          break;
        case INT_CONST:
          printWriter.println("<integerConstant> " + tokenizer.intConst() + " </integerConstant>");
          break;
        case STRING_CONST:
          printWriter.println("<stringConstant> " + tokenizer.stringConst() + " </stringConstant>");
          break;
        default:
          throw new SyntaxError(tokenizer.row(), tokenizer.col());
      }
    }
    printWriter.println("</tokens>");
    printWriter.close();
  }

  public static void main(String[] args) {
    JackTokenizer.parse("/Users/wzq/wk/nand2tetris/projects/10/ArrayTest/main.jack");
    JackTokenizer.parse("/Users/wzq/wk/nand2tetris/projects/10/ExpressionLessSquare");
    JackTokenizer.parse("/Users/wzq/wk/nand2tetris/projects/10/Square");
  }
}
