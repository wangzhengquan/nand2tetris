public class Code {
  private final static SymbolTable<String> compST = new SymbolTable<>();
  private final static SymbolTable<String> destST = new SymbolTable<>();
  private final static SymbolTable<String> jumpST = new SymbolTable<>();

  static {
    //comp
    compST.put("0", "0101010");
    compST.put("1", "0111111");
    compST.put("-1", "0111010");
    compST.put("D", "0001100");
    compST.put("A", "0110000");
    compST.put("M", "1110000");
    compST.put("!D", "0001101");
    compST.put("!A", "0110001");
    compST.put("!M", "1110001");
    compST.put("-D", "0001111");
    compST.put("-A", "0110011");
    compST.put("-M", "1110011");
    compST.put("D+1", "0011111");
    compST.put("A+1", "0110111");
    compST.put("M+1", "1110111");
    compST.put("D-1", "0001110");
    compST.put("A-1", "0110010");
    compST.put("M-1", "1110010");
    compST.put("D+A", "0000010");
    compST.put("D+M", "1000010");
    compST.put("D-A", "0010011");
    compST.put("D-M", "1010011");
    compST.put("A-D", "0000111");
    compST.put("M-D", "1000111");
    compST.put("D&A", "0000000");
    compST.put("D&M", "1000000");
    compST.put("D|A", "0010101");
    compST.put("D|M", "1010101");

    //dest
    destST.put("M", "001");
    destST.put("D", "010");
    destST.put("MD", "011");
    destST.put("A", "100");
    destST.put("AM", "101");
    destST.put("AD", "110");
    destST.put("AMD", "111");

    //jump
    jumpST.put("JGT", "001");
    jumpST.put("JEQ", "010");
    jumpST.put("JGE", "011");
    jumpST.put("JLT", "100");
    jumpST.put("JNE", "101");
    jumpST.put("JLE", "110");
    jumpST.put("JMP", "111");

  }

  public static String value(String value) {
    String s = Integer.toBinaryString(Integer.parseInt(value));
//    System.out.println("s=" + s);
    int n = 15 - s.length();
    if (n > 0) {
      StringBuilder buf = new StringBuilder();
      for (int i = 0; i < n; i++)
        buf.append("0");
      buf.append(s);
      return buf.toString();
    } else if (n < 0) {
      return s.substring(-n);
    }
    return s;
  }

  public static String dest(String dest) {
    if (dest == null) {
      return "000";
    }
    return destST.get(dest);
  }

  public static String comp(String comp) {
    return compST.get(comp);
  }

  public static String jump(String jump) {
    if (jump == null)
      return "000";
    return jumpST.get(jump);
  }

  public static void main(String[] args) {
    String s = Code.value((int) Math.pow(2, 15) - 1 + "");
    System.out.println(s);
    System.out.println(s.length());
  }
}
