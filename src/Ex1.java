import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;

/**
 * This is the main class that runs
 * the entire program, reads and prints a txt file
 */

public class Ex1 {
    // For self checks:
//    private static final String FILENAME_ALARM = "alarm_net.xml";
//    private static final String FILENAME_BIG = "big_net.xml";


    public static void main(String[] args) throws Exception {
        RunAll run = new RunAll("input2.txt");
        ReadFromTxt.write("output.txt", run.runTxt());
    }
}
