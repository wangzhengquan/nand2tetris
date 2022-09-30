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

  private static class Token {
    private String val;
    private int row;
    private int col;

    public Token(String val, int row, int col) {
      this.val = val;
      this.row = row;
      this.col = col;
    }

    public String val() {
      return val;
    }

    public int row() {
      return row;
    }

    public int col() {
      return col;
    }

    public String toString() {
      return val;
    }
  }

  private StringBuilder tokenBuf = new StringBuilder();
  private Queue<Token> tokenQueue = new ArrayDeque<>();

  private Iterator<Token> tokenIterator;
  private Token token;
  private String fileName;
//  private String nextToken;

  private Queue<Integer> readAheadQueue = new ArrayDeque<>();

  private BufferedInputStream bufIn;

  public JackTokenizer(File jackFile) throws IOException {
    fileName = jackFile.getName().substring(0, jackFile.getName().lastIndexOf(".jack"));
    tokenize(new FileInputStream(jackFile));
  }

  private JackTokenizer(InputStream in) throws IOException {
    fileName = "Test";
    tokenize(in);
  }


  private void tokenize(InputStream input) throws IOException {
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
          tokenQueue.add(new Token(tokenBuf.toString(), row, col));
          tokenBuf = new StringBuilder();
        }
      } else if (c == '\"') {
        if (tokenBuf.length() > 0) {
          tokenQueue.add(new Token(tokenBuf.toString(), row, col));
          tokenBuf = new StringBuilder();
        }
        tokenQueue.add(new Token(readStringConst(), row, col));
      } else if (symbolSet.contains(String.valueOf(c)) || operatorSet.contains(String.valueOf(c))) {
        if (tokenBuf.length() > 0) {
          tokenQueue.add(new Token(tokenBuf.toString(), row, col));
          tokenBuf = new StringBuilder();
        }
        tokenQueue.add(new Token(String.valueOf(c), row, col));
      } else {
        tokenBuf.append(c);
      }

      k = readNext();

    } // while end

    if (tokenBuf.length() > 0)
      tokenQueue.add(new Token(tokenBuf.toString(), row, col));

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

  public String getFileName() {
    return fileName;
  }

  public int row() {
    return token.row();
  }

  public int col() {
    return token.col();
  }

  public boolean hasMoreToken() {
    //System.lineSeparator();
    return tokenIterator.hasNext();
  }

  public void advance() {
    this.token = tokenIterator.next();
  }

  public int tokenType() {
    if (constKeywordSet.contains(token.val)) {
      return CONST_KEYWORD;
    } else if (statementKeywordSet.contains(token.val)) {
      return STATEMENT_KEYWORD;
    } else if (typeKeywordSet.contains(token.val)) {
      return TYPE_KEYWORD;
    } else if (keywordSet.contains(token.val)) {
      return KEYWORD;
    } else if (symbolSet.contains(token.val)) {
      return SYMBOL;
    } else if (operatorSet.contains(token.val)) {
      return OPERATOR;
    } else if (isStringConst(token.val)) {
      return STRING_CONST;
    } else if (isIdentifier(token.val)) {
      return IDENTIFIER;
    } else if (isIntegerConst(token.val)) {
      return INT_CONST;
    } else {
      reportError("Unrecognized token " + token);
      return -1;
    }
  }

  public String val() {
    return token.val;
  }

  public String keyword() {
    if (tokenType() == KEYWORD)
      return token.val;
    else {
      reportError(token + " is not a keyword, it is type of " + tokenType());
      return null;
    }
  }

  public String typeKeyword() {
    if (tokenType() == TYPE_KEYWORD)
      return token.val;
    else {
      reportError(token + " is not a typeKeyword, it is type of " + tokenType());
      return null;
    }
  }

  public String statementKeyword() {
    if (tokenType() == STATEMENT_KEYWORD)
      return token.val;
    else {
      reportError("");
      return null;
    }
  }


  public String constKeyword() {
    if (tokenType() == CONST_KEYWORD)
      return token.val;
    else {
      reportError("");
      return null;
    }
  }

  public String stringConst() {
    if (tokenType() == STRING_CONST)
      return token.val.substring(1, token.val.length() - 1);
    else {
      reportError("");
      return null;
    }
  }

  public int intConst() {
    if (tokenType() == INT_CONST)
      return Integer.parseInt(token.val);
    else {
      reportError(token + " is not a intConst, it is type of " + tokenType());
      return -1;
    }
  }

  public String identifier() {
    if (tokenType() == IDENTIFIER)
      return token.val;
    else {
      reportError(token + " is not a operator, it is type of " + tokenType());
      return null;
    }
  }

  public String operator() {
    if (tokenType() == OPERATOR)
      return token.val;
    else {
      reportError(token + " is not a operator, it is type of " + tokenType());
      return null;
    }
  }

  public String symbol() {
    if (tokenType() == SYMBOL)
      return token.val;
    else {
      reportError(token + " is not a symbol, it is type of " + tokenType());
      return null;
    }
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
        reportError("readStringConst");
        //return null;
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
          throw new IllegalArgumentException();
      }
    }
    printWriter.println("</tokens>");
    printWriter.close();
  }

  private void reportError(String msg) {
    String errorMsg = "In file " + fileName + ".jack (row " + (row() + 1) + ", col " + (col() + 1) + ") ";
    if (msg != null && !msg.isEmpty()) {
      errorMsg = errorMsg + ", " + msg;
    }
    throw new SyntaxError(errorMsg);
  }

  public static void main(String[] args) {
    JackTokenizer.parse("/Users/wzq/wk/nand2tetris/projects/10/ArrayTest/main.jack");
    JackTokenizer.parse("/Users/wzq/wk/nand2tetris/projects/10/ExpressionLessSquare");
    JackTokenizer.parse("/Users/wzq/wk/nand2tetris/projects/10/Square");
  }
}
