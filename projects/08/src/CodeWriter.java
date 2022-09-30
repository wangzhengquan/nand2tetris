import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

public class CodeWriter {
  private static final String CHARSET_NAME = "UTF-8";
  private PrintWriter out;
  private String fileName;
  private String functionName;
  private int arithmeticSymbolIdx = 0;
  private final static SymbolTable<String> segmentBaseAddrST = new SymbolTable<>();
  private final static SymbolTable<String> commandOperatorST = new SymbolTable<>();

  static {
    segmentBaseAddrST.put("local", "LCL");
    segmentBaseAddrST.put("argument", "ARG");
    segmentBaseAddrST.put("this", "THIS");
    segmentBaseAddrST.put("that", "THAT");
    segmentBaseAddrST.put("temp", "5");
  }

  static {
    commandOperatorST.put("add", "+");
    commandOperatorST.put("sub", "-");
    commandOperatorST.put("or", "|");
    commandOperatorST.put("and", "&");

    commandOperatorST.put("not", "!");
    commandOperatorST.put("neg", "-");

    commandOperatorST.put("eq", "JEQ");
    commandOperatorST.put("gt", "JGT");
    commandOperatorST.put("lt", "JLT");

  }

  public CodeWriter(String destDir, String fileName) {
    this.fileName = fileName;
    try {
      File dest = new File(destDir, fileName + ".asm");
      System.out.println("Generate " + dest.getAbsolutePath());
      out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(dest), CHARSET_NAME), false);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private CodeWriter() {
    this.fileName = "test";
    try {
      out = new PrintWriter(new OutputStreamWriter(System.out, CHARSET_NAME), true);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Closes the output file.
   */
  public void close() {
    out.close();
  }

  /**
   * Informs the code writer that the translation of a new VM file is started.
   *
   * @param fileName
   */
  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  /**
   * Writes the assembly code that is the translation of the given arithmetic command.
   *
   * @param command
   */
  public void writeArithmetic(String command) {
    out.println("// " + command);
    if (command.equals("neg") || command.equals("not")) {
      out.println("@SP");
      out.println("A=M-1");
      out.println("M=" + commandOperatorST.get(command) + "M");
    } else if (command.equals("add") || command.equals("sub") || command.equals("or") || command.equals("and")) {
      popD();
      out.println("A=A-1");
      out.println("M=M" + commandOperatorST.get(command) + "D");
    } else if (command.equals("eq") || command.equals("gt") || command.equals("lt")) {
      String label = fileName + "." + command + "." + arithmeticSymbolIdx;
      popD();
      out.println("A=A-1");
      out.println("D=M-D");
      out.println("M=-1");
      out.println("@" + label);
      out.println("D;" + commandOperatorST.get(command));
      //not eq
      out.println("@SP");
      out.println("A=M-1");
      out.println("M=0");
      out.println("(" + label + ")");
      arithmeticSymbolIdx++;
    } else {
      throw new IllegalArgumentException(command);
    }
    out.println("// end of " + command);
    out.println();
  }


  public void writePush(String segment, int index) {
    String line = "push " + segment + " " + index;
    out.println("// " + line);
    if (segment.equals("local") || segment.equals("argument") || segment.equals("this") || segment.equals("that")) {
      //addr = *segment + i, *SP = *addr, SP++
      dereferenceAddr(segmentBaseAddrST.get(segment), index);
      // D=*addr
      out.println("D=M");
    } else if (segment.equals("constant")) {
      // *SP = i, SP++
      //D=index
      out.println("@" + index);
      out.println("D=A");
    } else if (segment.equals("static")) {
      out.println("@" + fileName + "." + index);
      out.println("D=M");
    } else if (segment.equals("temp")) {
      // addr=5+i, *SP=*addr, SP++
      addr(segmentBaseAddrST.get(segment), index);
      out.println("D=M");
    } else if (segment.equals("pointer")) {
      //push pointer 0/1   ====>   *SP = THIS / THAT, SP++
      if (index == 0) {
        out.println("@THIS");
      } else if (index == 1) {
        out.println("@THAT");
      } else {
        throw new IllegalArgumentException(line);
      }
      out.println("D=M");
    } else {
      throw new IllegalArgumentException("Invalid segment\n" + line);
    }
    // push to stack
    pushD();
    out.println("// end of " + line);
    out.println();
  }


  public void writePop(String segment, int index) {
    String line = "pop " + segment + " " + index;
    out.println("// " + line);
    if (segment.equals("local") || segment.equals("argument") || segment.equals("this") || segment.equals("that")) {
      //addr = *segment + i, SP--, *addr = *SP
      dereferenceAddr(segmentBaseAddrST.get(segment), index);
      // put addr to tmp
      out.println("@R15");
      out.println("M=D");
      // *addr = *sp
      popD();
      assignDtoRefaddr("R15");
    } else if (segment.equals("static")) {
      popD();
      out.println("@" + fileName + "." + index);
      out.println("M=D");
    } else if (segment.equals("temp")) {
      // addr=5+i,SP--,*addr=*SP
      addr(segmentBaseAddrST.get(segment), index);
      // put addr to tmp
      out.println("@R15");
      out.println("M=D");

      popD();
      assignDtoRefaddr("R15");
    } else if (segment.equals("pointer")) {
      // pop pointer 0/1 ===> SP--, THIS/THAT = *SP
      popD();
      if (index == 0) {
        out.println("@THIS");
      } else if (index == 1) {
        out.println("@THAT");
      } else {
        throw new IllegalArgumentException(line);
      }
      out.println("M=D");
    } else {
      throw new IllegalArgumentException("Invalid segment\n" + line);
    }
    out.println("// end of " + line);
    out.println();
  }

  /**
   * Writes assembly code that effects the VM initialization, also called bootstrap code.
   * This code must be placed at the beginning of the output file.
   */
  public void writeInit() {
    out.println("// bootstrap code");
    out.println("@256");
    out.println("D=A");
    out.println("@SP");
    out.println("M=D");
    writeCall("Sys.init", 0);
    out.println("//  end of bootstrap code \n");
  }

  /**
   * Writes assembly code that effects the label command.
   *
   * @param name
   */
  public void writeLabel(String name) {
    out.println("(" + name + ")");
    //out.println("(" +fileName+ "." + functionName + "."+ name + ")");
  }

  /**
   * Writes assembly code that effects the goto command.
   *
   * @param label
   */
  public void writeGoto(String label) {
    out.println("// goto "+ label);
    out.println("@" + label);
    out.println("0;JMP");
  }

  /**
   * Writes assembly code that effects the if-goto command.
   *
   * @param label
   */
  public void writeIfGoto(String label) {
    out.println("// if-goto "+ label);
    popD();
    out.println("@" + label);
    out.println("D;JNE");
  }

  /**
   * Writes assembly code that effects the function command.
   *
   * @param functionName
   * @param nVars        n local variables
   */
  public void writeFunction(String functionName, int nVars) {
    out.println("// function " + functionName + " " + nVars);
    out.println("(" + functionName + ")");
    this.functionName = functionName;

    for (int i = 0; i < nVars; i++) {
      writePush("constant", 0);
    }
  }

  private int retIdx = 0;

  /**
   * Writes assembly code that effects the call command.
   *
   * @param functionName
   * @param nArgs
   */
  public void writeCall(String functionName, int nArgs) {
    String line = "call " + functionName + " " + nArgs;
    out.println("// " + line);
    String retAddr = this.fileName + "$ret." + retIdx++;

    // store return address
    out.println("@" + retAddr);
    out.println("D=A");
    pushD();
    // store LCL
    out.println("@LCL");
    out.println("D=M");
    pushD();
    // store ARG
    out.println("@ARG");
    out.println("D=M");
    pushD();
    // store THIS
    out.println("@THIS");
    out.println("D=M");
    pushD();
    // store THAT
    out.println("@THAT");
    out.println("D=M");
    pushD();

    // set new ARG and LCL
    // ARG=SP-5-nArgs
    out.println("@SP");
    out.println("D=M");
    out.println("@ARG");
    out.println("M=D");
    //ARG=ARG-5
    out.println("@5");
    out.println("D=A");
    out.println("@ARG");
    out.println("M=M-D");
    //ARG=ARG-nArgs
    out.println("@" + nArgs);
    out.println("D=A");
    out.println("@ARG");
    out.println("M=M-D");

    //LCL=SP
    out.println("@SP");
    out.println("D=M");
    out.println("@LCL");
    out.println("M=D");

    writeGoto(functionName);
    writeLabel(retAddr);
    out.println("// end of " + line + "\n");
  }

  /**
   * Writes assembly code that effects the return command.
   */
  public void writeReturn() {
    out.println("// return");
    // endFrame="LCL";
    String endFrame = "R13";
    assignYtoX(endFrame, "LCL");

    // retAddr = *(endFrame-5)
    String retAddr = "R14";
    dereferenceAddr(endFrame, -5);
    out.println("D=M");
    out.println("@" + retAddr);
    out.println("M=D");

    // *ARG=pop()   ===> put the return value at ARG[0]
    writePop("argument", 0);

    //SP=ARG+1
    out.println("@ARG");
    out.println("D=M");
    out.println("@SP");
    out.println("M=D+1");

    // THAT = *(endFrame-1)
    dereferenceAddr(endFrame, -1);
    out.println("D=M");
    out.println("@THAT");
    out.println("M=D");

    // THIS = *(endFrame-2)
    dereferenceAddr(endFrame, -2);
    out.println("D=M");
    out.println("@THIS");
    out.println("M=D");

    // ARG = *(endFrame-3)
    dereferenceAddr(endFrame, -3);
    out.println("D=M");
    out.println("@ARG");
    out.println("M=D");

    // LCL = *(endFrame-4)
    dereferenceAddr(endFrame, -4);
    out.println("D=M");
    out.println("@LCL");
    out.println("M=D");

    // goto retAddr
    out.println("@" + retAddr);
    out.println("A=M");
    out.println("0;JMP");

    out.println("// end of return\n");

  }

  /**
   * var Y;
   * var X = Y;
   */
  private void assignYtoX(String x, String y) {
    out.printf("// assign %s=%s\n", x, y);
    out.println("@" + y);
    out.println("D=M");
    out.println("@" + x);
    out.println("M=D");
    out.println();
  }


  private void assignValAtRefaddrToD(String pointer) {
    out.println("@" + pointer);
    out.println("A=M");
    out.println("D=M");
  }

  private void assignDtoRefaddr(String pointer) {
    out.println("// assign D to " + pointer);
    out.println("@" + pointer);
    out.println("A=M");
    out.println("M=D");
    out.println();
  }

  // AD = *segmentPointer + i
  private void dereferenceAddr(String segmentPointer, int index) {
    out.println("// dereferenceAddr ");
    boolean neg = index < 0;
    if (neg) {
      index = -index;
    }
    out.println("@" + index);
    out.println("D=A");
    out.println("@" + segmentPointer);
    if (neg) {
      out.println("AD=M-D");
    } else {
      out.println("AD=D+M");
    }
    out.println();

  }

  // AD = addr + i
  private void addr(String addr, int index) {
    out.println("// addr ");
    out.println("@" + index);
    out.println("D=A");
    out.println("@" + addr);
    out.println("AD=D+A");
    out.println();
  }

  /**
   * A=SP-- ; D=*SP
   */
  private void popD() {
    out.println("// popD ");
    // sp--
    out.println("@SP");
    out.println("AM=M-1");
    // D=*sp
    out.println("D=M");
    out.println();
  }

  /**
   * (*SP)=D; SP++
   */
  private void pushD() {
    out.println("// pushD ");
    //*sp=D
    out.println("@SP");
    out.println("A=M");
    out.println("M=D");
    // sp++
    out.println("@SP");
    out.println("M=M+1");
    out.println();
  }


  // out.println("");
  public static void main(String[] args) {
    CodeWriter code = new CodeWriter();
    code.writePush("constant", 17);
    code.writePush("local", 17);
    code.writePush("this", 17);
    code.writePush("that", 17);
    code.writePush("static", 17);
    code.writePush("temp", 2);
    code.writePush("pointer", 0);

    code.writePop("local", 17);
    code.writePop("this", 17);
    code.writePop("that", 17);
    code.writePop("static", 17);
    code.writePop("temp", 2);
    code.writePop("pointer", 0);

    code.writeArithmetic("sub");

    code.close();
  }
}
