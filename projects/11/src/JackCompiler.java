/**
 * % java JackAnalyzer ~/wk/nand2tetris/projects/09/Tetris
 */

import java.io.File;
import java.io.FilenameFilter;

public class JackCompiler {
  public static void main(String[] args) {
//    run(args);
    test();
  }

  public static void test() {
    compile("/Users/wzq/wk/nand2tetris/projects/11/Seven");
    compile("/Users/wzq/wk/nand2tetris/projects/11/ConvertToBin");
    compile("/Users/wzq/wk/nand2tetris/projects/11/Test2");
    compile("/Users/wzq/wk/nand2tetris/projects/11/Square");
    compile("/Users/wzq/wk/nand2tetris/projects/11/Pong");
    compile("/Users/wzq/wk/nand2tetris/projects/11/Average");
    compile("/Users/wzq/wk/nand2tetris/projects/11/ComplexArrays");
    compile("/Users/wzq/wk/nand2tetris/projects/09/Tetris");
    compile("/Users/wzq/wk/nand2tetris/projects/11/Test");
    compile("/Users/wzq/wk/nand2tetris/projects/11/Test2");
    compile("/Users/wzq/wk/nand2tetris/test");
  }

  public static void run(String[] args) {

    if (args.length < 1) {
      System.out.println("Usage: java JackAnalyzer file ...");
      return;
    }
    for (String file : args) {
      compile(file);
    }
  }

  public static void compile(String src) {
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
      if (srcFiles == null || srcFiles.length == 0) {
        throw new IllegalArgumentException("There is no jack file");
      }

      for (File file : srcFiles) {
        String name = file.getName();
        name = name.substring(0, name.lastIndexOf(".jack"));
        File destFile = new File(src, name + ".vm");
        System.out.println("generate " + destFile.getAbsolutePath());
        CompilationEngine compiler = new CompilationEngine(file, destFile);
        compiler.compile();
      }

    } else {
      String dir = srcFile.getParent();
      String name = srcFile.getName();
      if (!name.endsWith(".jack")) {
        throw new IllegalArgumentException("It should be .jack file");
      }
      name = name.substring(0, name.lastIndexOf(".jack"));
      File destFile = new File(dir, name + ".vm");
      System.out.println("generate " + destFile.getAbsolutePath());
      CompilationEngine compiler = new CompilationEngine(srcFile, destFile);
//        CompilationEngine compiler = new CompilationEngine(new FileInputStream(srcFile), System.out);
      compiler.compile();
    }
  }
}
