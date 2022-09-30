public class SyntaxError extends RuntimeException {

  public SyntaxError(int row, int col) {
    super("In row " + row + ", col " + col);
  }


  public SyntaxError(int row, int col, String message) {
    super("In row " + row + ", col " + col + System.lineSeparator() + message);
  }


  public SyntaxError(String message, Throwable cause) {
    super(message, cause);
  }


  public SyntaxError(Throwable cause) {
    super(cause);
  }


}
