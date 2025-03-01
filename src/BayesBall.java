import java.util.*;

/**
 * This class implements the BayesBall algorithm
 * we learned it in class
 */

public class BayesBall {
    private Variable a;
    private List<String> given;
    private BayesianNetwork net;
    private Queue<Variable> qVar;
    private Queue<Integer> qFrom;
    private Map<String, Variable> visited;
    private Map<String, Variable> markedOnTop;
    private Map<String, Variable> markedOnBottom;
    private static final int fromCild =0, fromParent =1;

    /**
     *
     * @param a variable 'a' to start from
     * @param given list of given variable
     * @param net the all net
     */
    public BayesBall(Variable a, List<String> given, BayesianNetwork net) {
        qVar = new LinkedList<Variable>();
        qVar.add(a);
        qFrom = new LinkedList<Integer>();
        qFrom.add(fromCild);
        visited = new LinkedHashMap<String, Variable>();
        markedOnTop = new LinkedHashMap<String, Variable>();
        markedOnBottom = new LinkedHashMap<String, Variable>();
        this.given = given;
        this.a = a;
        this.net = net;

    }

    /**
     *
     * @param a variable 'a' to start from
     * @param evidenceArr array of given variable and thar values
     * @param net the all net
     */
    public BayesBall(Variable a, String[][] evidenceArr, BayesianNetwork net) {
        qVar = new LinkedList<Variable>();
        qVar.add(a);
        qFrom = new LinkedList<Integer>();
        qFrom.add(fromCild);
        visited = new LinkedHashMap<String, Variable>();
        markedOnTop = new LinkedHashMap<String, Variable>();
        markedOnBottom = new LinkedHashMap<String, Variable>();
        given = new LinkedList<String>();
        for (int i = 0; i < evidenceArr.length; i++)
        {
            this.given.add(evidenceArr[i][0]);
        }
        this.a = a;
        this.net = net;

    }



    /**
     * @return visited List represent all nodes that affect a given the List 'given'
     */
    public Map<String, Variable> BbAlgo() {
        while (qVar.size() != 0) {
            Variable j = qVar.poll();
            visited.put(j.name, j); //"color" 'v' as visited
            int visitFrom = qFrom.poll();

            if (!given.contains(j.name) && visitFrom == fromCild) {
                if (!markedOnTop.containsValue(j)){
                    markedOnTop.put(j.name, j);
                    for (Variable parent: j.getParents()){
                        qVar.add(parent);
                        qFrom.add(fromCild);
                    }
                }
                if (!markedOnBottom.containsValue(j)){
                    markedOnBottom.put(j.name, j);
                    for (Variable child: j.getChildren()){
                        qVar.add(child);
                        qFrom.add(fromParent);
                    }
                }
            }
            if (visitFrom == fromParent){
                if (given.contains(j.name) && !markedOnTop.containsValue(j)){
                    markedOnTop.put(j.name, j);
                    for (Variable parent: j.getParents()){
                        qVar.add(parent);
                        qFrom.add(fromCild);
                    }
                }
                if (!given.contains(j.name) && !markedOnBottom.containsValue(j)){
                    markedOnBottom.put(j.name, j);
                    for (Variable child: j.getChildren()){
                        qVar.add(child);
                        qFrom.add(fromParent);
                    }
                }
            }

        }
        return visited;
    }

    public String isDependence(Variable b){
        Map<String, Variable> dependence = BbAlgo();
        if (dependence.containsKey(b.name))
            return "no";
        else
            return "yes";
    }

    /**
     *
     * @param relevantVar list of dependence variable from BBAlgo
     * @return list of variable that are relevant for J&E Algo, i.e. all the parent and parent-parent of evidence and query variable
     */
    public Map<String,Variable> PrntFiltering(Map<String,Variable> relevantVar) {
        Map<String, Variable> newRelevantVar = new LinkedHashMap<String, Variable>();
        Queue<Variable> q_Var = new LinkedList<Variable>();
        for (int i = 0; i < given.size(); i++)
        {
            q_Var.add(net.getVar(given.get(i)));
        }
        q_Var.add(a);   //query variable

        while (q_Var.size() != 0) {
            Variable j = q_Var.poll();
            if (!newRelevantVar.containsValue(j)) {
                    newRelevantVar.put(j.name, j); //"color" 'v' as visited

                for (Variable parent : j.getParents()) {
                    if (relevantVar.containsKey(parent.name))
                        q_Var.add(parent);
                }
            }
        }
        return newRelevantVar;
    }
}

