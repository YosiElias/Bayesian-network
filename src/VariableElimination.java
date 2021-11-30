import java.text.DecimalFormat;
import java.util.*;


/**
 * This class implements the
 * Variable Elimination Algorithm as we learned it in class.
 *
 * While reducing the number of operations
 * by using a baseball algorithm and further
 * reducing factors that are ancestors.
 */
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
    private List<String> _Names;
    private List<String> _independent;

    public VariableElimination(String qName, String qValue, String[][] evidens, List<String> order, BayesianNetwork net) {
        _net = net;
        _evidence = evidens;
        _mapEvidence = new LinkedHashMap<String, String>();
        for (int i = 0; i < _evidence.length; i++) {
            _mapEvidence.put(_evidence[i][0], _evidence[i][1]);
        }
        _Names = new ArrayList<String>();
        for (int i = 0; i < net.getNames().size(); i++) {
            _Names.add(net.getNames().get(i));
        }
        _order = order;
        _qName = qName;
        _qValue = qValue;
        _pAns = VEalgo();
        _mulNum = 0;
        _addNum = 0;
    }


    /**
     * main function of VE algo
     * @return
     */
    private String VEalgo() {
        double ans = isCPT();
        if (ans != Double.MAX_VALUE){
            return "" + ans + "," + _addNum + "," + _mulNum;
        }
        Variable q = _net.getVar(_qName);
        // Take all relevant variable:
        BayesBall b = new BayesBall(q, _evidence, _net);
        Map<String, Variable> relevantVar = b.BbAlgo();
        _independent = new ArrayList<String>(); //Independent variable from BBAlgo
        // Create independent list:
        for (int i = 0; i < _net.getNames().size(); i++) {
            String varName = _net.getNames().get(i);
            if (!relevantVar.containsKey(varName)) {
                _independent.add(varName);
            }
        }
        relevantVar = b.PrntFiltering(relevantVar);
        // Clean _Names from all not relevant variable, by filter on BBAlgo & par-parent:
        for (int i = 0; i < _net.getNames().size(); i++) {
            String varName = _net.getNames().get(i);
            if (!relevantVar.containsKey(varName)) {
                _Names.remove(varName);
            }
        }
        // Reduce of given evidence and the independent variable from CPT:
        reduceCpts();
        // Create factor from cleaned cpt's:
        creatFactors();
        // Make Join & Eliminate on factors by the given order:
        for (int i = 0; i < _order.size(); i++) {
            String byVar = _order.get(i);
            if (_Names.contains(byVar)) {
                List<Factor> relevant = relevantFactor(byVar, relevantVar);
                j_e(byVar, relevant);
            }
        }
        // Make Join & Eliminate on factors contain the qurie-variable:
        List<Factor> relevant = relevantFactor(_qName, relevantVar);
        for (int i = 0; i < _factors.size(); i++) { //remove not relevant factors from '_factor', i.e. evidence etc.
            Factor f = _factors.get(i);
            if (!relevant.contains(f))
                _factors.remove(f);
        }
        j_e(_qName,relevant);
        double[] finalTable = _factors.get(0).getValue(_qValue);
        double prob=Double.MAX_VALUE;
        if (finalTable[1] != 0) //there is anther value except of _qValue
        {
            int outSize_q = _net.getVar(_qName).getOutCome().size();
            _addNum += outSize_q - 1;
            prob = finalTable[0] / finalTable[1];   // normalize
        }
        DecimalFormat df = new DecimalFormat("#0.00000");
        if (prob==Double.MAX_VALUE) System.err.println("Error: not find prob");
        return  "" + df.format(prob) + "," + _addNum + "," + _mulNum;
    }

    private void j_e(String byVar, List<Factor> relevant) {
        Factor je_Factor;
        boolean onlyOne = false;
        if (relevant.size()==0)
            return;
        if (relevant.size()==1)
            onlyOne = true;
        while (relevant.size() > 1)
        {
            Factor fmin = minF(relevant);
            Factor fbig = minF(relevant);
            je_Factor = fbig.join(fmin, byVar);
            _mulNum += fbig.get_mulNum();
            relevant.add(je_Factor);
        }
        je_Factor = relevant.get(0).eliminate(byVar);
        _addNum += relevant.get(0).get_addNum();
        if (je_Factor.getNameV().size() != 1 || _factors.size()==0)   //remove factor if is nameV size = 1, because nameV = {'_P_'}
            if (onlyOne)
            {
                _factors.remove(0);
                _factors.add(je_Factor);
            }
            else
                _factors.add(je_Factor);
    }


    /**
     * @return min factor size to start with.
     */
    private Factor minF(List<Factor> relevant) {
        Factor min = relevant.get(0);
        for (Factor f:relevant)
        {
            if (f.getSizeOfTable() < min.getSizeOfTable())
                min = f;
            else if (f.getSizeOfTable() == min.getSizeOfTable())
                min = selectByHaski(f, min);
        }
        relevant.remove(min);
        _factors.remove(min);
        return min;
    }


    //sum all names in factor and return the one with min haski value
    private Factor selectByHaski(Factor f, Factor min) {
        int fSize = 0, minSize = 0;
        for (int i = 0; i < f.getNameV().size(); i++) {
            for (int j = 0; j < f.getNameV().get(i).length(); j++) {
                fSize += (int)f.getNameV().get(i).charAt(j);
            }
        }
        for (int i = 0; i < min.getNameV().size(); i++) {
            for (int j = 0; j < min.getNameV().get(i).length(); j++) {
                minSize += (int)min.getNameV().get(i).charAt(j);
            }
        }
        if (minSize<fSize)
            return min;
        else
            return f;
    }

    /**
     *
     * @param byVar name of variable to factor by
     * @param relevantVar
     * @return list of relevant factor to this variable
     */
    private List<Factor> relevantFactor(String byVar, Map<String, Variable> relevantVar) {
        List<Factor> relevant = new ArrayList<Factor>();
        for (Factor f:_factors)
        {
            if (f.getNameV().contains(byVar))   //if this factor create from other factor and not directly from variable
                relevant.add(f);
        }
        return relevant;
    }

    private void creatFactors() {
        _factors = new ArrayList<Factor>();
        for (String varName:_Names)    //getNames is updated
        {
            Variable v = _net.getVar(varName);
            boolean containIndependent = false;
            for (int i = 0; i < _independent.size(); i++) {
                for (Variable p: v.getParents())
                {
                    if (p.name.equals(_independent.get(i)))
                    {
                        containIndependent = true;
                        break;
                    }
                }
            }
            if (!containIndependent) {   //create factor only if not contain independent variable
                Factor f = new Factor(_net.getVar(varName), _net);
                _factors.add(f);
            }
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

        if (list==null){
            list = new LinkedList<Integer>();
            for (int r = 0; r < table.length; r++) { list.add(r); } //List of rows
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

        if (list==null){
            list = new LinkedList<Integer>();
            for (int r = 0; r < table.length; r++) { list.add(r); } //List of rows
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

}
