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
        String infile = args[0];
        File file = new File(infile);
        List<File> fileList = new ArrayList<>();

        if (file.isDirectory()) {
            FilenameFilter filenameFilter = (dir, name) -> name.matches(".*.vm");
            fileList.addAll(Arrays.asList(Objects.requireNonNull(file.listFiles(filenameFilter))));
        } else {
            fileList.add(file.getAbsoluteFile());
        }

        for (File _file : fileList) {
            Parser parser = new Parser(_file.getAbsolutePath());
            CodeWriter codeWriter = new CodeWriter(_file.getAbsolutePath().replaceFirst("[.][^.]+$", ".asm"));

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
                        codeWriter.writeLabel(parser.arg1());
                        break;
                    case C_GOTO:
                        codeWriter.writeGoto(parser.arg1());
                        break;
                    case C_IF:
                        codeWriter.writeIf(parser.arg1());
                        break;
                    case C_FUNCTION:
                        codeWriter.writeFunction(parser.arg1(), parser.arg2());
                        break;
                    case C_CALL:
                        codeWriter.writeCall(parser.arg1(), parser.arg2());
                        break;
                    case C_RETURN:
                        codeWriter.writeReturn();
                        break;
                }
            }
        }
    }
}
