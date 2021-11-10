import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BayesianNetwork {

    // the underlying map recording all variables.
    private Map<String, Variable> net;
    private List<String> names;

    /**
     * The constructor.
     */
    public BayesianNetwork() {
        net = new LinkedHashMap<String, Variable>();
        names = new ArrayList<String>();
    }

//    //copy constructor
//    public BayesianNetwork(BayesianNetwork other) {
//        net = new LinkedHashMap<String, Variable>();
//        for (String varName:other.getNames()) {
//            Variable v = new Variable(other.getVar(varName))
//        }
//        names = new ArrayList<String>();
//    }


    public Variable getVar(String name) {
        if (net.containsKey(name))
            return net.get(name);
        else
            throw new RuntimeException("No such variable: " + name);
    }

    public void addVar(Variable v) {
        this.net.put(v.name, v);
        this.names.add(v.name);
    }

    public List<String> getNames() { return names; }

    @Override
    public String toString() {
        String ans = "";
        for (String name : names)
           ans += net.get(name) + "\n";
        return ans;
    }
}
