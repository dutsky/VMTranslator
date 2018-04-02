import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Parser {
    BufferedReader reader;
    String line = "";

    public Parser(String fileName) {
        try {
           reader = new BufferedReader(new FileReader(fileName));
        } catch (IOException e) {
            System.err.println("IOException");
            e.printStackTrace();
            System.exit(1);
        }
    }

    public boolean hasMoreCommands() throws IOException {
        return reader.ready();
    }

    public void advance() throws IOException {
        if (!line.equals("")) {
            line = reader.readLine().replaceFirst("//.+", "").trim();
        }
        while(line.equals("")) {
            line = reader.readLine().replaceFirst("//.+", "").trim();
        }
    }

    public CTYPE commandType() {
        String command = line.split(" ")[0];
        switch (command) {
            case "add":
            case "sub":
            case "neg":
            case "eq":
            case "gt":
            case "lt":
            case "and":
            case "or":
            case "not":
                return CTYPE.C_ARITHMETIC;
            case "push":
                return CTYPE.C_PUSH;
            case "pop":
                return CTYPE.C_POP;
            case "label":
                return CTYPE.C_LABEL;
            case "goto":
                return CTYPE.C_GOTO;
            case "if-goto":
                return CTYPE.C_IF;
            case "function":
                return CTYPE.C_FUNCTION;
            case "call":
                return CTYPE.C_CALL;
            case "return":
                return CTYPE.C_RETURN;
        }
        return null;
    }

    public String arg1() {
        if (commandType() == CTYPE.C_ARITHMETIC) {
            return line.split(" ")[0];
        } else {
            return line.split(" ")[1];
        }
    }

    public int arg2() {
        return Integer.valueOf(line.split(" ")[2]);
    }
}
