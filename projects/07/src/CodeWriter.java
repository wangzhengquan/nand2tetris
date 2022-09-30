import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

public class CodeWriter {
  private static final String CHARSET_NAME = "UTF-8";
  private PrintWriter out;
  private String name;
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

  public CodeWriter(String destDir, String destName) {
    this.name = destName;
    try {
      File dest = new File(destDir, destName + ".asm");
      out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(dest), CHARSET_NAME), false);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private CodeWriter() {
    this.name = "test";
    try {
      out = new PrintWriter(new OutputStreamWriter(System.out, CHARSET_NAME), false);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void arithmetic(String command) {
    out.println("// " + command);
    if (command.equals("neg") || command.equals("not")) {
      out.println("@SP");
      out.println("A=M-1");
      out.println("M=" + commandOperatorST.get(command) + "M");
    } else if (command.equals("add") || command.equals("sub") || command.equals("or") || command.equals("and")) {
      popFromStackToD();
      out.println("A=A-1");
      out.println("M=M" + commandOperatorST.get(command) + "D");
    } else if (command.equals("eq") || command.equals("gt") || command.equals("lt")) {
      String label = name + "." + command + "." + arithmeticSymbolIdx;
      popFromStackToD();
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
    out.println();
  }


  public void push(String segment, int index) {
    String line = "// push " + segment + " " + index;
    out.println(line);
    if (segment.equals("local") || segment.equals("argument") || segment.equals("this") || segment.equals("that")) {
      //addr = *segmentPointer + i, *SP = *addr, SP++
      referenceAddr(segmentBaseAddrST.get(segment), index);
      // D=*addr
      out.println("D=M");
    } else if (segment.equals("constant")) {
      // *SP = i, SP++
      //D=index
      out.println("@" + index);
      out.println("D=A");
    } else if (segment.equals("static")) {
      out.println("@" + name + "." + index);
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
    pushDtoStack();
    out.println();
  }


  public void pop(String segment, int index) {
    String line = "// pop " + segment + " " + index;
    out.println(line);
    if (segment.equals("local") || segment.equals("argument") || segment.equals("this") || segment.equals("that")) {
      //addr = segmentPointer + i, SP--, *addr = *SP
      referenceAddr(segmentBaseAddrST.get(segment), index);
      // put addr to tmp
      out.println("@R15");
      out.println("M=D");
      // *addr = *sp
      popFromStackToD();
      assignDtoRefaddr("R15");
    } else if (segment.equals("static")) {
      popFromStackToD();
      out.println("@" + name + "." + index);
      out.println("M=D");
    } else if (segment.equals("temp")) {
      // addr=5+i,SP--,*addr=*SP
      addr(segmentBaseAddrST.get(segment), index);
      // put addr to tmp
      out.println("@R15");
      out.println("M=D");

      popFromStackToD();
      assignDtoRefaddr("R15");
    } else if (segment.equals("pointer")) {
      // pop pointer 0/1 ===> SP--, THIS/THAT = *SP
      popFromStackToD();
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
    out.println();
  }

  public void close() {
    out.close();
  }

  private void assignValAtRefaddrToD(String pointer) {
    out.println("@" + pointer);
    out.println("A=M");
    out.println("D=M");
  }

  private void assignDtoRefaddr(String pointer) {
    out.println("@" + pointer);
    out.println("A=M");
    out.println("M=D");
  }

  // AD = *segmentPointer + i
  private void referenceAddr(String segmentPointer, int index) {
    out.println("@" + index);
    out.println("D=A");
    out.println("@" + segmentPointer);
    out.println("AD=D+M");
  }

  // AD = addr + i
  private void addr(String addr, int index) {
    out.println("@" + index);
    out.println("D=A");
    out.println("@" + addr);
    out.println("AD=D+A");
  }

  /**
   * A=SP-- ; D=*SP
   */
  private void popFromStackToD() {
    // sp--
    out.println("@SP");
    out.println("AM=M-1");
    // D=*sp
//    out.println("A=M");
    out.println("D=M");
  }

  /**
   * (*SP)=D; SP++
   */
  private void pushDtoStack() {
    //*sp=D
    out.println("@SP");
    out.println("A=M");
    out.println("M=D");
    // sp++
    out.println("@SP");
    out.println("M=M+1");
  }


  // out.println("");
  public static void main(String[] args) {
    CodeWriter code = new CodeWriter();
    code.push("constant", 17);
    code.push("local", 17);
    code.push("this", 17);
    code.push("that", 17);
    code.push("static", 17);
    code.push("temp", 2);
    code.push("pointer", 0);

    code.pop("local", 17);
    code.pop("this", 17);
    code.pop("that", 17);
    code.pop("static", 17);
    code.pop("temp", 2);
    code.pop("pointer", 0);

    code.arithmetic("sub");

    code.close();
  }
}
