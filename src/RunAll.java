import java.util.ArrayList;
import java.util.Arrays;
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
            if (line.charAt(0) == 'P')
                ans += go2VareLim(line);
        }
        return ans;
    }

    private String go2VareLim(String line) {
        int i=2;
        String qName = "";
        while (line.charAt(i) != '=') {
            qName += line.charAt(i);
            i++;
        }
        Variable q =  net.getVar(qName);
        i++;    //skip the char '='
        String qValue = "";
        while (line.charAt(i) != '|') {
            qValue += line.charAt(i);
            i++;
        }
        int nE = numOfE(line, i);
        String[][] evidens = new String[nE][2];
        i++;    //skip the char '|'
        for (int j = 0; j < evidens.length; j++) {
            String eName = "";
            while (line.charAt(i) != '=') {
                eName += line.charAt(i);
                i++;
            }
            evidens[j][0] = eName;
            i++;    //skip the char '='
            String eValue = "";
            while (line.charAt(i) != ',' && line.charAt(i) != ')') {
                eValue += line.charAt(i);
                i++;
            }
            evidens[j][1] = eValue;
            i++;    //skip the char ')' OR char ','
        }
        i++;    //skip the char ' '
        List<String> order = new ArrayList<String>();
        while (i<line.length()) {
            String ordName = "";
            while (i<line.length() && line.charAt(i) != '-') {
                ordName += line.charAt(i);
                i++;
            }
            i++;    //skip the char '-'
            order.add(ordName); //order.get(0) is the first at order and so on
        }

        //Todo: tester printing: (to delete before submit)
//        System.out.println("\nQ: "+qName+"\tQ value: "+qValue);
//        for (int j = 0; j < evidens.length; j++) {
//            System.out.println("\n"+Arrays.toString(evidens[j]));
//        }
//        System.out.println("\norder: "+Arrays.toString(order.toArray()));

        return "";
    }

    private int numOfE(String line, int i) {
        int num = 0;
        while (line.charAt(i) != ')'){
            if (line.charAt(i) == '=')
                num++;
        i++;
        }
        return num;
    }

    private String go2BBAlgo(String line) {
        List<String> given = new ArrayList<String>();
        int i=0;
        String aName = "";
        while (line.charAt(i) != '-') {
            aName += line.charAt(i);
            i++;
        }
        i++;    //skip the char '-'
        String bName = "";
        while (line.charAt(i) != '|') {
            bName += line.charAt(i);
            i++;
        }
        Variable a =  net.getVar(aName);
        Variable b =  net.getVar(bName);
        for (; i < line.length(); i++) {
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
