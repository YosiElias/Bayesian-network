import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;

public class Ex1 {
    private static final String FILENAME_ALARM = "alarm_net.xml";
    private static final String FILENAME_BIG = "big_net.xml";


    public static void main(String[] args) throws Exception {
//        System.out.println(ReadFromXml.readBuild(FILENAME_BIG));
        RunAll run = new RunAll("my_input.txt");
        System.out.println(run.runTxt());//yes  no  no  yes no
//
//        String[][] evidens = {{"A", "T"}, {"A", "T"},{"A", "T"},{"A", "T"}};
//        System.out.println(evidens.length);
//        String[][] a = new String[4][2];
//        System.out.println(a[0].length);

//        List<String> order = new ArrayList<String>();
//        order.add("1");
//        order.add("2");
//        System.out.println(order.get(0));

    }
}
