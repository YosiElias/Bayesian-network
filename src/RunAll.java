import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class RunAll {
    public BayesianNetwork net;
    public Queue<String> qTxt;
    private BayesBall bbAlgo;
    String ans;

    public RunAll(String txtFile) throws Exception {
        this.qTxt = ReadFromTxt.read(txtFile);
        this.net = ReadFromXml.readBuild(qTxt.poll());
        this.ans = "";
    }


    public String runTxt() {
        while (qTxt.size() != 0){
            String line = (qTxt.poll());
            if (line.charAt(1) == '-')
                ans += go2BBAlgo(line);
        }
        return ans;
    }

    private String go2BBAlgo(String line) {
        List<String> given = new ArrayList<String>();
        Variable a =  net.getVar(""+line.charAt(0));
        Variable b =  net.getVar(""+line.charAt(2));
        for (int i = 3; i < line.length(); i++) {
            if (line.charAt(i) == '|' || line.charAt(i) == ',') {
                String varName = "";
                i++;    //go to start of word of name
                while (i < line.length() && line.charAt(i) != '='){  //create var name from the line
                    varName += line.charAt(i);
                    i++;
            }
            given.add(varName);
        }
    }
        bbAlgo = new BayesBall(a, given, net);
        return bbAlgo.isDependence(b) + "\n";
    }
}
