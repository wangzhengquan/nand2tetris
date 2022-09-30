/**
 * % java JackAnalyzer ~/wk/nand2tetris/projects/09/Tetris
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;

public class JackAnalyzer {
  public static void main(String[] args) {
    compile("/Users/wzq/wk/nand2tetris/projects/10/ArrayTest/main.jack");
    compile("/Users/wzq/wk/nand2tetris/projects/10/ExpressionLessSquare");
    compile("/Users/wzq/wk/nand2tetris/projects/10/Square");
    compile("/Users/wzq/wk/nand2tetris/projects/09/Tetris");

    // run(args);
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
      if (srcFiles == null)
        return;

      for (File file : srcFiles) {
        String name = file.getName();
        name = name.substring(0, name.lastIndexOf(".jack"));
        File destFile = new File(src, name + ".xml");
        try {
          System.out.println("generate " + destFile.getAbsolutePath());
          CompilationEngine compiler = new CompilationEngine(new FileInputStream(file), new FileOutputStream(destFile));
//          CompilationEngine compiler = new CompilationEngine(new FileInputStream(file), System.out);
          compiler.compile();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }

    } else {
      String dir = srcFile.getParent();
      String name = srcFile.getName();
      if (!name.endsWith(".jack")) {
        throw new IllegalArgumentException("It should be .jack file");
      }
      name = name.substring(0, name.lastIndexOf(".jack"));
      File destFile = new File(dir, name + ".xml");
      try {
        System.out.println("generate " + destFile.getAbsolutePath());
        CompilationEngine compiler = new CompilationEngine(new FileInputStream(srcFile), new FileOutputStream(destFile));
//        CompilationEngine compiler = new CompilationEngine(new FileInputStream(srcFile), System.out);
        compiler.compile();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}
