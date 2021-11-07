import java.util.Queue;

public class Ex1 {
    private static final String FILENAME_ALARM = "alarm_net.xml";
    private static final String FILENAME_BIG = "big_net.xml";


    public static void main(String[] args) throws Exception {
        System.out.println(ReadFromXml.readBuild(FILENAME_BIG));
//        RunAll run = new RunAll("input.txt");
//        System.out.println(run.runTxt());

    }
}
