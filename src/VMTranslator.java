// Sorry for this trash. I'm in hurry. I fall behind course pace.
import java.io.IOException;

public class VMTranslator {
    public static void main(String[] args) throws IOException {

        String infile = args[0];
        String outfile = args[0].replaceFirst("[.][^.]+$", ".asm");

        Parser parser = new Parser(infile);
        CodeWriter codeWriter = new CodeWriter(outfile);

        while(parser.hasMoreCommands()) {
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
