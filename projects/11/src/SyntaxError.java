public class SyntaxError extends RuntimeException {

  public SyntaxError() {
    super();
  }


  public SyntaxError(String message) {
    super(message);
  }


  public SyntaxError(String message, Throwable cause) {
    super(message, cause);
  }


  public SyntaxError(Throwable cause) {
    super(cause);
  }


}
