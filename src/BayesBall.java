import java.util.*;

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
     * @return visited List represent all nodes that affect a given the List 'given'
     */
    private Map<String, Variable> BbAlgo() {
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

}

