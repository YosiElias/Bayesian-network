import java.util.*;

public class ProbTable {

    private Map<List<String>, Double> table;

    public static void main(String[] args) {
        Map<List<String>, Double> table = new LinkedHashMap<List<String>, Double>();
        List<String> l = new ArrayList<String>();
        l.add("A");
        l.add("C");
        l.add("B");
        table.put(l, 0.4);
//        if (l.contains("A"))
//            System.out.println(table.get(l));
        double[][] d = new double[2][3];
//        if (8.0%2.0 ==0)
        System.out.println(9.0%2.0 ==0);
    }
    public void setTable(Map<List<String>, Double> table) {
        this.table = table;
    }

}
