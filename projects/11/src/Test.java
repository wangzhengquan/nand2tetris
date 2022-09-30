import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test {
  public static void main(String[] args) {

//    System.out.println("nihao hello\rworld");
//    void x = testRegularExpression();
    while (true) {
      try {
        int a = System.in.read();
        System.out.println(a);
        
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }


  public static void testSwitch() {

    String expression = "b";

    switch (expression) {

      case "a":
        System.out.println("Small Size");
        break;

      case "b":
        System.out.println("Large Size");
        break;

      // default case
      default:
        System.out.println("Unknown Size");
    }
  }

  public static void testRegularExpression() {
    Pattern pattern = Pattern.compile("^_?[A-Za-z](\\w)*$");
    Matcher matcher = pattern.matcher("main");
//    System.out.println(matcher.matches());
    boolean matchFound = false;
    if (matcher.matches()) {
      //while (matcher.find()) {
      System.out.println("matcher.groupCount() +" + matcher.groupCount());
      System.out.println("I found the text " + matcher.group() + " starting at index " +
          matcher.start() + " and ending at index " + matcher.end());
      matchFound = true;
    }
    if (!matchFound) {
      System.out.println("No match found.");
    }
  }
}
