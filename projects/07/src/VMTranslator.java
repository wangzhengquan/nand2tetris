import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Scanner;

public class VMTranslator {
  private static final String CHARSET_NAME = "UTF-8";

  public static void translate(String src) {
    File srcFile = new File(src);
    if (srcFile.isDirectory()) {
      File[] srcFiles = srcFile.listFiles(new FilenameFilter() {
        public boolean accept(File dir, String name) {
          return name.endsWith(".vm");
        }
      });
      if (srcFiles == null)
        return;
      for (File file : srcFiles) {
//        String dir = src;
        String name = file.getName();
        name = name.substring(0, name.lastIndexOf(".vm"));
        translate(file.getAbsolutePath(), src, name);
      }

    } else {
      String dir = srcFile.getParent();
      String name = srcFile.getName();
      if (!name.endsWith(".vm")) {
        throw new IllegalArgumentException("It should be .vm file");
      }
      name = name.substring(0, name.lastIndexOf(".vm"));
      translate(src, dir, name);
    }
  }

  public static void translate(String src, String destDir, String name) {
    System.out.println("translate " + src);
    try {
      // first pass
      Scanner scanner = new Scanner(new java.io.BufferedInputStream(new FileInputStream(src)), CHARSET_NAME);
      CodeWriter code = new CodeWriter(destDir, name);
      while (scanner.hasNextLine()) {
        String line = scanner.nextLine().trim();
        if (line.isEmpty() || line.startsWith("//")) {
          continue;
        }
        Parser parser = new Parser(line);
        switch (parser.commandType()) {
          case Parser.C_ARITHMETIC:
            code.arithmetic(parser.command());
            break;
          case Parser.C_PUSH:
            code.push(parser.arg1(), parser.arg2());
            break;
          case Parser.C_POP:
            code.pop(parser.arg1(), parser.arg2());
            break;
          default:
            throw new IllegalArgumentException("Invalid command:" + line);
        }
      }

      code.close();
      scanner.close();

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) {

    if (args.length < 1) {
      System.out.println("Usage: java VMTranslator file ...");
      return;
    }
    for (String file : args) {
      VMTranslator.translate(file);
    }

  }

  public static void test1() {
    VMTranslator.translate("/Users/wzq/wk/nand2tetris/projects/07/MemoryAccess/BasicTest/BasicTest.vm");
    VMTranslator.translate("/Users/wzq/wk/nand2tetris/projects/07/MemoryAccess/test/PointerTest.vm");
    VMTranslator.translate("/Users/wzq/wk/nand2tetris/projects/07/MemoryAccess/PointerTest/PointerTest.vm");
    VMTranslator.translate("/Users/wzq/wk/nand2tetris/projects/07/MemoryAccess/StaticTest/StaticTest.vm");

    VMTranslator.translate("/Users/wzq/wk/nand2tetris/projects/07/StackArithmetic/SimpleAdd/SimpleAdd.vm");
    VMTranslator.translate("/Users/wzq/wk/nand2tetris/projects/07/StackArithmetic/StackTest/StackTest.vm");

  }


}
