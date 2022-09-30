import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Assembler {
  private static final String CHARSET_NAME = "UTF-8";

  Assembler() {
  }


  public static void compile(String src) {
    File srcFile = new File(src);
    String dest = srcFile.getParent();
    // + ".hack"
    String name = srcFile.getName();
    if (!name.endsWith(".asm")) {
      throw new IllegalArgumentException("It should be .asm file");
    }
    name = name.substring(0, name.indexOf(".asm")) + ".hack";
    dest = dest + File.separator + name;
//    System.out.println(dest);
//    System.out.println(name);
    compile(src, dest);
  }

  public static void compile(String src, String dest) {
    SymbolTable<Integer> labelST = new SymbolTable<Integer>();
    List<String> instructionList = new ArrayList<>();
    addPredefinedSymbols(labelST);


    try {
      // First pass
      Scanner scanner = new Scanner(new java.io.BufferedInputStream(new FileInputStream(src)), CHARSET_NAME);
      PrintWriter outTest = new PrintWriter(new OutputStreamWriter(new FileOutputStream(src + ".test"), CHARSET_NAME), false);
      int pc = 0;
      while (scanner.hasNextLine()) {
        String line = scanner.nextLine().trim();
        outTest.println(pc + " : " + line);
        if (line.isEmpty() || line.startsWith("//")) {
          continue;
        }
        if (line.startsWith("(")) {
          labelST.put(parseLabel(line), pc);
        } else {
          instructionList.add(line);
          pc++;
        }
      }
      outTest.close();

      // Second pass
      PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(dest), CHARSET_NAME), false);
      int variableIdx = 16;
      for (String line : instructionList) {
        Parser parser = new Parser(line);
        if (parser.isAInstruction()) {
          // A Instruction
          String var = parser.value();
          if (!isNumeric(var)) {
            Integer value = labelST.get(var);
            if (value == null) {
              value = variableIdx++;
              labelST.put(var, value);
            }
            var = value.toString();
          }
          out.println("0" + Code.value(var));
        } else {
          // C Instruction
          out.println("111" + Code.comp(parser.comp()) + Code.dest(parser.dest()) + Code.jump(parser.jump()));
        }
      }

      out.close();

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static boolean isNumeric(String token) {
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

  private static void addPredefinedSymbols(SymbolTable<Integer> labelST) {
    for (int i = 0; i < 16; i++) {
      labelST.put("R" + i, i);
    }
    labelST.put("SCREEN", 16384);
    labelST.put("KBD", 24576);
    labelST.put("SP", 0);
    labelST.put("LCL", 1);
    labelST.put("ARG", 2);
    labelST.put("THIS", 3);
    labelST.put("THAT", 4);
  }


  private static String parseLabel(String line) {
    StringBuilder buf = new StringBuilder();
    for (int i = 1; i < line.length(); i++) {
      char c = line.charAt(i);
      if (c == ' ' || c == '\t') {
        continue;
      }
      if (c == ')')
        break;
      buf.append(c);
    }
    return buf.toString();
  }

  public static void main(String[] args) {
//    run(args);
    test();
  }

  public static void run(String[] args) {

    if (args.length < 1) {
      System.out.println("Usage: java Assembler file ...");
      return;
    }
    for (String file : args) {
      Assembler.compile(file);
    }

  }

  public static void test() {
    // Users/wzq/wk/nand2tetris/projects/06/max
    Assembler.compile("/Users/wzq/wk/nand2tetris/projects/00/Add2.asm");
    Assembler.compile("/Users/wzq/wk/nand2tetris/projects/06/add/Add.asm");

    Assembler.compile("/Users/wzq/wk/nand2tetris/projects/06/max/MaxL.asm");
    Assembler.compile("/Users/wzq/wk/nand2tetris/projects/06/max/Max.asm");

    Assembler.compile("/Users/wzq/wk/nand2tetris/projects/06/rect/RectL.asm");
    Assembler.compile("/Users/wzq/wk/nand2tetris/projects/06/rect/Rect.asm");

    Assembler.compile("/Users/wzq/wk/nand2tetris/projects/06/pong/PongL.asm");
    Assembler.compile("/Users/wzq/wk/nand2tetris/projects/06/pong/Pong.asm");

    Assembler.compile("/Users/wzq/wk/nand2tetris/projects/09/Square/Square.asm");

  }
}
