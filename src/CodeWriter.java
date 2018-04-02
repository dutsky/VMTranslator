import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class CodeWriter {
    private int eq_index = 0;
    private int gt_index = 0;
    private int lt_index = 0;
    private String fileName;
    private BufferedWriter writer;
    private static Map<String, String> segments = new HashMap<>();
    static {
        segments.put("local", "LCL");
        segments.put("argument", "ARG");
        segments.put("this", "THIS");
        segments.put("that", "THAT");
    }

    public CodeWriter(String fileName) {
        this.fileName = fileName;
        try {
//            writer = new BufferedWriter(new FileWriter(fileName));
            PrintStream out = new PrintStream(new FileOutputStream(fileName));
            System.setOut(out);
        } catch (IOException e) {
            System.err.println("Can't write to file " + fileName);
            e.printStackTrace();
        }
    }

    public void writeArithmetic(String command) {
        System.out.println("// " + command);
        if (!command.equals("neg") && !command.equals("not")) {
            PopFromStack();
        }
        SPdec();
        System.out.println("A=M");
        switch (command) {
            case "add":
                System.out.println("M=D+M");
                break;
            case "sub":
                System.out.println("M=M-D");
                break;
            case "eq":
                compare("EQ", eq_index);
                eq_index++;
                break;
            case "gt":
                compare("GT", gt_index);
                gt_index++;
                break;
            case "lt":
                compare("LT", lt_index);
                lt_index++;
                break;
            case "and":
                System.out.println("M=D&M");
                break;
            case "or":
                System.out.println("M=D|M");
                break;
            case "not":
                System.out.println("M=!M");
                break;
            case "neg":
                System.out.println("M=-M");

        }
        SPinc();
    }

    public void writePushPop(CTYPE command, String segment, int index) {
        switch (command) {
            case C_PUSH:
                System.out.println("// push " + segment + " " + index);
                switch (segment) {
                    case "constant":
                        System.out.println("@" + index);
                        System.out.println("D=A");
                        PushToStack();
                        break;
                    case "static":
                        String shortName = fileName.substring(
                                fileName.lastIndexOf('/')+1,
                                fileName.lastIndexOf('.')+1);
                        System.out.println("@"+ shortName + index);
                        System.out.println("D=M");
                        PushToStack();
                        break;
                    case "local":
                    case "argument":
                    case "this":
                    case "that":
                        getSegOffsetAddress(segment, index);
                        System.out.println("D=M");
                        PushToStack();
                        break;
                    case "temp":
                        int offset = 5 + index;
                        System.out.println("@" + offset);
                        System.out.println("D=M");
                        PushToStack();
                        break;
                    case "pointer":
                        if (index == 0) {
                            System.out.println("@THIS");
                        } else {
                            System.out.println("@THAT");
                        }
                        System.out.println("D=M");
                        PushToStack();
                        break;
                }
                break;
            case C_POP:
                System.out.println("// pop " + segment + " " + index);
                switch (segment) {
                    case "static":
                        String shortName = fileName.substring(
                                fileName.lastIndexOf('/')+1,
                                fileName.lastIndexOf('.')+1);
                        System.out.println("@" + shortName + index);
                        putAddrToTemp();
                        PopFromStack();
                        fromDtoTempAddr();
                        break;
                    case "local":
                    case "argument":
                    case "this":
                    case "that":
                        getSegOffsetAddress(segment, index);
                        putAddrToTemp();
                        PopFromStack();
                        fromDtoTempAddr();
                        break;
                    case "temp":
                        int offset = 5 + index;
                        System.out.println("@" + offset);
                        putAddrToTemp();
                        PopFromStack();
                        fromDtoTempAddr();
                        break;
                    case "pointer":
                        if (index == 0) {
                            System.out.println("@THIS");
                        } else {
                            System.out.println("@THAT");
                        }
                        putAddrToTemp();
                        PopFromStack();
                        fromDtoTempAddr();
                        break;
                }
        }
    }

    private void compare(String command, int index) {
        System.out.println("D=M-D");
        System.out.println("@" + command + index);
        System.out.println("D;J" + command);
        System.out.println("@SP");
        System.out.println("A=M");
        System.out.println("M=0");
        System.out.println("@" + command + "CONT" + index);
        System.out.println("0;JMP");
        System.out.println("(" + command + index + ")");
        System.out.println("@SP");
        System.out.println("A=M");
        System.out.println("M=-1");
        System.out.println("(" + command + "CONT" + index + ")");
    }

    private void PushToStack() {
        System.out.println("@SP");
        System.out.println("A=M");
        System.out.println("M=D");
        SPinc();
    }

    private void SPinc() {
        System.out.println("@SP");
        System.out.println("M=M+1");
    }

    private void PopFromStack() {
        SPdec();
        System.out.println("@SP");
        System.out.println("A=M");
        System.out.println("D=M");
    }

    private void SPdec() {
        System.out.println("@SP");
        System.out.println("M=M-1");
    }

    private void putAddrToTemp() {
        System.out.println("D=A");
        System.out.println("@temp");
        System.out.println("M=D");
    }

    private void fromDtoTempAddr() {
        System.out.println("@temp");
        System.out.println("A=M");
        System.out.println("M=D");
    }

    private void getSegOffsetAddress(String segment, int index) {
        System.out.println("@" + index);
        System.out.println("D=A");
        System.out.println("@" + segments.get(segment));
        System.out.println("A=D+M");
    }

    public void writeInit() {
        System.out.println("// bootstrap");
        System.out.println("@256");
        System.out.println("D=A");
        System.out.println("@SP");
        System.out.println("M=D");
        System.out.println("@Sys.init");
        System.out.println("0;JMP");
    }

    public void writeLabel(String label) {
        System.out.println("// label");
        System.out.println("(" + label + ")");
    }

    public void writeGoto(String label) {
        System.out.println("// goto");
        System.out.println("@" + label);
        System.out.println("0;JMP");
    }

    public void writeIf(String label) {
        System.out.println("// if-goto");
        System.out.println("@SP");
        System.out.println("AM=M-1");
        System.out.println("D=M");
        System.out.println("@" + label);
        System.out.println("D;JNE");
    }

    public void writeFunction(String functionName, int numLocals) {
        System.out.println("// function " + functionName + " " + numLocals);
        writeLabel(functionName);
        for (int i = 0; i < numLocals; i++) {
            //getSegOffsetAddress("local", i);
            //System.out.println("D=M");
            System.out.println("@0");
            System.out.println("D=A");
            PushToStack();
        }
    }

    public void writeCall(String functionName, int numArgs) {
        System.out.println("// call " + functionName + " " + numArgs);
        // push return address
        System.out.println("@returnAddress");
        System.out.println("D=A");
        PushToStack();
        // push LCL
        System.out.println("@LCL");
        System.out.println("D=M");
        PushToStack();
        // push ARG
        System.out.println("@ARG");
        System.out.println("D=M");
        PushToStack();
        // push THIS
        System.out.println("@THIS");
        System.out.println("D=M");
        PushToStack();
        // push THAT
        System.out.println("@THAT");
        System.out.println("D=M");
        PushToStack();
        // ARG = SP - nArgs - 5
        System.out.println("@SP");
        System.out.println("D=M");
        System.out.println("@" + Integer.toString(numArgs));
        System.out.println("D=D-A");
        System.out.println("@5");
        System.out.println("D=D-A");
        System.out.println("@ARG");
        System.out.println("M=D");
        // LCL = SP
        System.out.println("@SP");
        System.out.println("D=M");
        System.out.println("@LCL");
        System.out.println("M=D");
        // goto g
        System.out.println("@" + functionName);
        System.out.println("0;JMP");
        // (returnAddress)
        writeLabel("returnAddress");
    }

    public void writeReturn() {
        System.out.println("// return");
        // frame = LCL
        System.out.println("@LCL");
        System.out.println("D=M");
        System.out.println("@endFrame");
        System.out.println("M=D");
        // retAddr = *(frame-5)
        System.out.println("@5");
        System.out.println("D=A");
        System.out.println("@endFrame");
        System.out.println("D=M-D");
        System.out.println("@retAddr");
        System.out.println("M=D");
        // *ARG = pop
        PopFromStack();
        System.out.println("@ARG");
        System.out.println("A=M");
        System.out.println("M=D");
        // SP = ARG + 1
        System.out.println("@ARG");
        System.out.println("D=M");
        System.out.println("@SP");
        System.out.println("M=D+1");
        // THAT = *(frame-1)
        System.out.println("@1");
        System.out.println("D=A");
        System.out.println("@endFrame");
        System.out.println("A=M-D");
        System.out.println("D=M");
        System.out.println("@THAT");
        System.out.println("M=D");
        // THIS = *(frame-2)
        System.out.println("@2");
        System.out.println("D=A");
        System.out.println("@endFrame");
        System.out.println("A=M-D");
        System.out.println("D=M");
        System.out.println("@THIS");
        System.out.println("M=D");
        // ARG = *(frame-3)
        System.out.println("@3");
        System.out.println("D=A");
        System.out.println("@endFrame");
        System.out.println("A=M-D");
        System.out.println("D=M");
        System.out.println("@ARG");
        System.out.println("M=D");
        // LCL = *(frame-4)
        System.out.println("@4");
        System.out.println("D=A");
        System.out.println("@endFrame");
        System.out.println("A=M-D");
        System.out.println("D=M");
        System.out.println("@LCL");
        System.out.println("M=D");
        // goto retAddr
        System.out.println("@retAddr");
        System.out.println("A=M");
        System.out.println("0;JMP");
    }
}
