import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

public class VMTranslator {
  private static final String CHARSET_NAME = "UTF-8";

  private static boolean isSysFile(File file) {
    return file.getName().equals("Sys.vm");
  }

  private static boolean containsSysFile(File[] files) {
    for (File file : files) {
      if (file.getName().equals("Sys.vm"))
        return true;
    }
    return false;
  }

  public static void translate(String src) {
    File srcFile = new File(src);
    if (!srcFile.exists()) {
      throw new IllegalArgumentException("File dos not exists : " + src + "\n");
    }
    if (srcFile.isDirectory()) {
      File[] srcFiles = srcFile.listFiles(new FilenameFilter() {
        public boolean accept(File dir, String name) {
          return name.endsWith(".vm");
        }
      });
      if (srcFiles == null)
        return;
      CodeWriter code = new CodeWriter(src, srcFile.getName());
      if (containsSysFile(srcFiles))
        code.writeInit();
      for (File file : srcFiles) {
        String name = file.getName();
        name = name.substring(0, name.lastIndexOf(".vm"));
        code.setFileName(name);
        try {
//          System.out.println("translate " + file);
          translate(new FileInputStream(file), code);
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
      code.close();

    } else {

      String dir = srcFile.getParent();
      String name = srcFile.getName();
      if (!name.endsWith(".vm")) {
        throw new IllegalArgumentException("It should be .vm file:" + src + "\n");
      }
      name = name.substring(0, name.lastIndexOf(".vm"));
      CodeWriter code = new CodeWriter(dir, name);

      if (isSysFile(srcFile))
        code.writeInit();
      try {
//        System.out.println("translate " + srcFile);
        translate(new FileInputStream(srcFile), code);
      } catch (IOException e) {
        e.printStackTrace();
      }
      code.close();
    }
  }

  private static void translate(InputStream in, CodeWriter out) {

    Scanner scanner = new Scanner(new java.io.BufferedInputStream(in), CHARSET_NAME);
    while (scanner.hasNextLine()) {
      String line = scanner.nextLine().trim();
      if (line.isEmpty() || line.startsWith("//")) {
        continue;
      }
      Parser parser = new Parser(line);
      switch (parser.commandType()) {
        case Parser.C_ARITHMETIC:
          out.writeArithmetic(parser.command());
          break;
        case Parser.C_PUSH:
          out.writePush(parser.arg1(), parser.arg2());
          break;
        case Parser.C_POP:
          out.writePop(parser.arg1(), parser.arg2());
          break;
        case Parser.C_CALL:
          out.writeCall(parser.arg1(), parser.arg2());
          break;
        case Parser.C_FUNCTION:
          out.writeFunction(parser.arg1(), parser.arg2());
          break;
        case Parser.C_GOTO:
          out.writeGoto(parser.arg1());
          break;
        case Parser.C_IF_GOTO:
          out.writeIfGoto(parser.arg1());
          break;
        case Parser.C_LABEL:
          out.writeLabel(parser.arg1());
          break;
        case Parser.C_RETURN:
          out.writeReturn();
          break;

        default:
          throw new IllegalArgumentException("Invalid command:" + line);
      }
    }
    scanner.close();


  }


  public static void main(String[] args) {
    run(args);
    // test2();
  }

  public static void run(String[] args) {

    if (args.length < 1) {
      System.out.println("Usage: java VMTranslator file ...");
      return;
    }
    for (String file : args) {
      VMTranslator.translate(file);
    }

  }

  public static void test2() {
    VMTranslator.translate("/Users/wzq/wk/nand2tetris/projects/08/ProgramFlow/BasicLoop");
    VMTranslator.translate("/Users/wzq/wk/nand2tetris/projects/08/ProgramFlow/FibonacciSeries");
    VMTranslator.translate("/Users/wzq/wk/nand2tetris/projects/08/FunctionCalls/SimpleFunction");
    VMTranslator.translate("/Users/wzq/wk/nand2tetris/projects/08/FunctionCalls/FibonacciElement");
    VMTranslator.translate("/Users/wzq/wk/nand2tetris/projects/08/FunctionCalls/StaticsTest");
    VMTranslator.translate("/Users/wzq/wk/nand2tetris/projects/08/FunctionCalls/NestedCall");
//    VMTranslator.translate("");

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
