import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test {
  public static void main(String[] args) {
    System.out.println();
    System.out.println("nihao hello\rworld");

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
