import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;

public class Ex1 {
    private static final String FILENAME_ALARM = "alarm_net.xml";
    private static final String FILENAME_BIG = "big_net.xml";


    public static void main(String[] args) throws Exception {
//        System.out.println(ReadFromXml.readBuild(FILENAME_BIG));
//        ReadFromXml.readBuild(FILENAME_ALARM);
//        BayesianNetwork net = ReadFromXml.readBuild(FILENAME_ALARM);
//        System.out.println(Arrays.toString(net.getVar("A").getTable()[4]));
//        System.out.println(net.getVar("A").getTable()[4][net.getVar("A").getTable()[4].length-1]);
//        System.out.println(net.getVar("A"));
        RunAll run = new RunAll("input.txt");
//        System.out.println(run.runTxt());
        ReadFromTxt.write("output.txt", run.runTxt());
//        System.out.println(run.runTxt());//yes  no  no  yes no

//        System.out.println(net.getVar("A").getParents().indexOf(net.getVar("E")));


//        DecimalFormat df = new DecimalFormat("#0.00000");
//        System.out.println(df.format(1.912385));

    }
}
