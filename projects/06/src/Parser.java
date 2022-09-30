public class Parser {
  private String dest;
  private String comp;
  private String jump;
  private int type;
  private String value;

  public Parser(String line) {
    if (line.startsWith("@")) {
      // A instruction
      parseAInstruction(line);
    } else {
      // C instruction
      parseCInstruction(line);
    }
  }

  private void parseCInstruction(String line) {
    StringBuilder buf = new StringBuilder();
    int j = 0;
    // dest comp jump
    String[] values = new String[3];
    int len = line.length();
    for (int i = 0; i < len; i++) {
      char c = line.charAt(i);

      if (c == ' ' || c == '\t')
        continue;

      if (c == '/' && line.charAt(i + 1) == '/')
        break;

      if (c == '=') {
        if (buf.length() > 0) {
          j = 0;
          values[j] = buf.toString();
          buf = new StringBuilder();
        }
      } else if (c == ';') {
        if (buf.length() > 0) {
          j = 1;
          values[j] = buf.toString();
          buf = new StringBuilder();
        }
      } else
        buf.append(c);
    }

    if (buf.length() > 0) {
      j++;
      values[j] = buf.toString();
    }
    this.type = 1;
    this.dest = values[0];
    this.comp = values[1];
    this.jump = values[2];
  }

  private void parseAInstruction(String line) {
    StringBuilder buf = new StringBuilder();
    for (int i = 1; i < line.length(); i++) {
      char c = line.charAt(i);
      if (c == ' ' || c == '\t') {
        break;
      }
      buf.append(c);
    }

    this.type = 0;
    this.value = buf.toString();
  }

  public boolean isAInstruction() {
    return type == 0;
  }

  public String value() {
    return value;
  }

  public String dest() {
    return dest;
  }

  public String comp() {
    return comp;
  }

  public String jump() {
    return jump;
  }

  public int type() {
    return type;
  }

  public String toString() {
    if (this.isAInstruction())
      return "Instruction{" +
          "type=" + type +
          ", value='" + value + '\'' +
          '}';
    else
      return "Instruction{" +
          "type=" + type +
          ", dest='" + dest + '\'' +
          ", comp='" + comp + '\'' +
          ", jump='" + jump + '\'' +
          '}';
  }

  //List<Instruction> instuctionList = parser.getInstructionList();
  public static void main(String[] args) {

  }
}
