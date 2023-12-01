import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {

    static FileSystem fs;
    public static void main(String[] args) {
        InputStream stream = System.in;
        InputStreamReader streamReader = new InputStreamReader(stream);
        BufferedReader reader = new BufferedReader(streamReader);
        fs = new FileSystem();

        String line;
        String cmd;
        List<String> params;
        while (true) {
            try {
                printPrompt();
                line = reader.readLine();
                cmd = getCmd(line);
                params = getParams(line);

                switch (cmd) {
                    case "formata" -> fs = new FileSystem();
                    case "touch" -> fs.touch(params.get(0));
                    case "gravar" -> fs.gravar(params.get(0), String.join(" ", params.subList(1, params.size())));
                    case "cat" -> fs.cat(params.get(0));
                    case "rm" -> fs.deleteInode(params.get(0), false);
                    case "chown" -> fs.chown(params.get(0), params.get(1));
                    case "chmod" -> fs.chmod(params.get(0), params.get(1));
                    case "mkdir" -> fs.mkdir(params.get(0));
                    case "rmdir" -> fs.deleteInode(params.get(0), true);
                    case "cd" -> fs.cd(params.get(0));
                    case "ls" -> {
                        if (params.size() != 0) {
                            fs.ls(params.get(0));
                        }
                        else {
                            fs.ls();
                        }
                    }
                    case "adduser" -> fs.adduser(params.get(0));
                    case "rmuser" -> fs.rmuser(params.get(0));
                    case "lsuser" -> fs.lsuser();
                    case "loginuser" -> fs.loginuser(params.get(0));
                    default -> System.out.println("Invalid command.");
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static String getCmd(String line) {
        return line.split(" ")[0];
    }

    private static List<String> getParams(String line) {
        String[] s = line.split(" ");

        return new ArrayList<>(Arrays.asList(s).subList(1, s.length));
    }

    private static void printPrompt() {
        System.out.print(Utils.ANSI_BOLD + Utils.ANSI_GREEN + fs.user().getName() +"@simulator" + Utils.ANSI_RESET + ":" +
                         Utils.ANSI_BOLD + Utils.ANSI_PURPLE + "~"+ fs.path() + Utils.ANSI_RESET + "$ ");
    }
}