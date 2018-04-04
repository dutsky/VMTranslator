// Sorry for this trash.
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class VMTranslator {
    public static void main(String[] args) throws IOException {
        File infile = new File(args[0]);
        String infileName;
        String outfileName;
        List<File> fileList = new ArrayList<>();

        if (infile.isDirectory()) {
            FilenameFilter filenameFilter = (dir, name) -> name.matches(".*.vm");
            fileList.addAll(Arrays.asList(Objects.requireNonNull(infile.listFiles(filenameFilter))));
            outfileName = infile.getAbsolutePath() + File.separator + infile.getName() + ".asm";
        } else {
            fileList.add(infile.getAbsoluteFile());
            outfileName = infile.getAbsolutePath().replaceFirst("[.][^.]+$", ".asm");
        }

        File outfile = new File(outfileName);
        if (outfile.exists()) {
            outfile.delete();
        }

        CodeWriter bootstrapWriter = new CodeWriter(outfileName);
        bootstrapWriter.writeInit();

        for (File file : fileList) {
            String functionName = "";
            infileName = file.getAbsolutePath();
            Parser parser = new Parser(infileName);
            CodeWriter codeWriter = new CodeWriter(outfileName);

            while (parser.hasMoreCommands()) {
                parser.advance();
                switch (parser.commandType()) {
                    case C_PUSH:
                        codeWriter.writePushPop(CTYPE.C_PUSH, parser.arg1(), parser.arg2());
                        break;
                    case C_POP:
                        codeWriter.writePushPop(CTYPE.C_POP, parser.arg1(), parser.arg2());
                        break;
                    case C_ARITHMETIC:
                        codeWriter.writeArithmetic(parser.arg1());
                        break;
                    case C_LABEL:
                        if (!functionName.equals("")) {
                            codeWriter.writeLabel(functionName + "$" + parser.arg1());
                        } else {
                            codeWriter.writeLabel(parser.arg1());
                        }
                        break;
                    case C_GOTO:
                        if (!functionName.equals("")) {
                            codeWriter.writeGoto(functionName + "$" + parser.arg1());
                        } else {
                            codeWriter.writeGoto(parser.arg1());
                        }
                        break;
                    case C_IF:
                        codeWriter.writeIf(parser.arg1());
                        break;
                    case C_FUNCTION:
                        functionName = parser.arg1();
                        codeWriter.writeFunction(parser.arg1(), parser.arg2());
                        break;
                    case C_CALL:
                        codeWriter.writeCall(parser.arg1(), parser.arg2());
                        break;
                    case C_RETURN:
                        codeWriter.writeReturn();
                        functionName = "";
                        break;
                }
            }
        }
    }
}
