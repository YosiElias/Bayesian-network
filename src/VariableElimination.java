import java.text.DecimalFormat;
import java.util.*;

public class VariableElimination {
    private String _qName;
    private String _qValue;
    private String[][] _evidence;
    private Map<String, String> _mapEvidence;
    private List<String> _order;
    private List<Factor> _factors;
    private String _pAns;
    private int _mulNum;
    private int _addNum;
    private BayesianNetwork _net;
    private final DecimalFormat _df = new DecimalFormat("#0.00000");

    public VariableElimination(String qName, String qValue, String[][] evidens, List<String> order, BayesianNetwork net) {
        _net = net;
        _evidence = evidens;
        _mapEvidence = new LinkedHashMap<String, String>();
        for (int i = 0; i < _evidence.length; i++) {
            _mapEvidence.put(_evidence[i][0], _evidence[i][1]);
        }
        _order = order;
        _qName = qName;
        _qValue = qValue;
        _pAns = VEalgo();
        _mulNum = 0;
        _addNum = 0;
    }

    //main function of vE algo

    /**
     * main function of vE algo
     * @return
     */
    private String VEalgo() {
        double ans = isCPT();
        if (ans != Double.MAX_VALUE){
            return "" + ans + "," + _addNum + "," + _mulNum;
        }
        Variable q = _net.getVar(_qName);
        //take all relevant variable:
        BayesBall b = new BayesBall(q, _evidence, _net);
        Map<String, Variable> relevantVar = b.BbAlgo();
        for (String varName:_net.getNames()) {
            if (!relevantVar.containsKey(varName))
                _net.getNames().remove(varName);
        }
        //reduce of given evidence:
        reduceCpts();
        creatFactors();
        for (int i = 0; i < _order.size(); i++) {
            String byVar = _order.get(i);
            List<Factor> relevant = relevantFactor(byVar);
            j_e(byVar,relevant);
        }
        return "***********";
    }

    private void j_e(String byVar, List<Factor> relevant) {
        while (relevant.size() != 1)
        {
            Factor fmin = minF(relevant);
            Factor fbig = minF(relevant);
            //Todo: add option to sort by 'Haski'
            Factor je_Factor = fbig.join(fmin, byVar);
            je_Factor = eliminate(je_Factor, byVar);
            relevant.add(je_Factor);
        }
    }

//    private Factor join(Factor f1, Factor f2) {


    private Factor minF(List<Factor> relevant) {
        Factor min = relevant.get(0);
        for (Factor f:relevant)
        {
            if (f.getNameV().size() < min.getNameV().size())
                min = f;
        }
        relevant.remove(min);
        return min;
    }

    /**
     *
     * @param byVar name of variable to factor by
     * @return list of relevant factor to this variable
     */
    private List<Factor> relevantFactor(String byVar) {
        List<Factor> relevant = new ArrayList<Factor>();
        for (Factor f:_factors)
        {
            if (f.getNameV().contains(byVar))
                relevant.add(f);
        }
        return relevant;
    }

    private void creatFactors() {
        _factors = new ArrayList<Factor>();
        for (String varName:_net.getNames())    //getNames is updated
        {
            Factor f = new Factor(_net.getVar(varName), _net);
            _factors.add(f);
        }
    }

    /**
     * update all net to relevant table without 'not needed' value
     */
    private void reduceCpts() {
        for (String varName:_net.getNames())
        {
            if (_mapEvidence.containsKey(varName)) //if the variable is evidence:
            {
                Variable v = _net.getVar(varName);
                v.updateTable(relevantRowsFromItSelf(null, varName, _mapEvidence.get(varName), v)); //update CPT of v itself
                for (Variable child:v.getChildren())
                {
                    child.updateTable(relevantRowsFromPrnt(null, varName, _mapEvidence.get(varName), child)); //update CPT of child of v
                }
            }
        }
    }




    /**
     * @return if in CPT return the value. else return Double.MAX_VALUE
     */
    private double isCPT() {
        Variable v = _net.getVar(_qName);
        //check hachala - du-kivunit, and than get parent==evidens
        for (int i = 0; i < _evidence.length; i++) {
            if (!v.getParents().contains(_net.getVar(_evidence[i][0])))
                return Double.MAX_VALUE;    //q not in CPT
        }
        if (_evidence.length < v.getParents().size())
            return Double.MAX_VALUE;    //q not in CPT. bicouse evidens C parent ->  evidens =< parent, and if evidens < parent there is a parent 'i' that is not evidens
        double P_q = pOfCPT(v);

        return P_q;
    }

    /**
     * @return value of specific rows from CPT
     */
    private double pOfCPT(Variable v) {
        String[][] table = v.getTable();
        List<Variable> parent = v.getParents();
        int rowNum = table.length;
        List<Integer> rows = new LinkedList<Integer>();
        for (int r = 0; r < table.length; r++) { rows.add(r); } //List of rows to manage 'mahakav'
        int i = 0;
        while (rowNum>1 && i<parent.size()){
            Variable p = parent.get(i);
            rows = relevantRowsFromPrnt(rows, p.name, valueOfEvidns(p.name), v);
            rowNum = rows.size();
            i++;
        }
        rows = relevantRowsFromItSelf(rows, _qName, _qValue, v);
        rowNum = rows.size();
        return Double.parseDouble(table[rows.get(0)][table[0].length-1]);
    }

    /**
     *
     * @param list list of rows index that relevant
     * @return relevant rows from value of v itself
     */
    private List<Integer> relevantRowsFromItSelf(List<Integer> list, String qName, String qValue, Variable v) {
        String[][] table = v.getTable();
        List<Integer> newList = new ArrayList<Integer>();

        if (list==null){    //Todo: check if get into this if (need to get into)
            list = new LinkedList<Integer>();
            for (int r = 0; r < table.length; r++) { list.add(r); } //List of rows to manage 'mahakav'
        }

        int prntIndex = table[0].length - 2;
        for (int i = 0; i < list.size(); i++) {
            int r = list.get(i);
            if (table[r][prntIndex].equals(qValue))
                newList.add(r);
        }
        return newList;
    }

    /**
     *
     * @param list  list of rows index that relevant
     * @param namePrnt name of parent 'p'
     * @param valuePrnt value evidens for parent 'p'
     * @param v = q, the variable to reduce is table
     * @return relevant rows from all rows by value of parent 'p'
     */
    private List<Integer> relevantRowsFromPrnt(List<Integer> list, String namePrnt, String valuePrnt, Variable v){
        List<Integer> newList = new ArrayList<Integer>();
        int prntIndex = v.getParents().indexOf(_net.getVar(namePrnt));
        String[][] table = v.getTable();

        if (list==null){    //Todo: check if get into this if (need to get into)
            list = new LinkedList<Integer>();
            for (int r = 0; r < table.length; r++) { list.add(r); } //List of rows to manage 'mahakav'
        }

        for (int i = 0; i < list.size(); i++) {
            int r = list.get(i);
            if (table[r][prntIndex].equals(valuePrnt))
                newList.add(r);
        }
        return newList;
    }


    /**
     *
     * @param namePrnt name of parent
     * @return value that we looking for for this variable
     */
    private String valueOfEvidns(String namePrnt) {
        for (int i = 0; i < _evidence.length; i++) {
            if (_evidence[i][0].equals(namePrnt))
                return _evidence[i][1];
        }
        return null;
    }


    public String get_pAns() {
        this._pAns = VEalgo();
        return this._pAns;
    }














//    /**
//     *
//     * @param v q
//     * @return full value of double, need to format to 5 num after '.'
//     */
//    private double pOfq(Variable v) {
//        String[][] table = v.getTable();
//        int rOfAns = -1;    //Todo: it is kind of Test for not init value at the loops
//        List<Integer> rows = new LinkedList<Integer>();
//        for (int r = 0; r < table.length; r++) { rows.add(r); } //List of rows to manage 'mahakav'
//
//
//        for (int r = 0; r < table.length; r++)
//        {
//            if (rows.contains(r))
//            {
//                for (int c = 0; c < table[0].length -1; c++)  //run on all the columns of table of v (=q)
//                {
//                    if (c != table[0].length-2) {
////                        if (!table[c][r].equals(valueOfEvidns(c, v.getParents())))
////                            break;
//                    }
//                    else if (c==table[0].length-2){
//                        if (table[c][r].equals(_qValue))
//                            rOfAns = r; //save r of ans
//                    }
//                }
//            }
//        }
//        if (rOfAns == -1) System.err.println("Eror: 'rOfAns' not get chang in the loops");  //Todo: delete before submit
//        return Double.parseDouble(table[rOfAns][table[0].length]);
//    }

}
