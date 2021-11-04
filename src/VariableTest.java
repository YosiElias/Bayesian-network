import static org.junit.jupiter.api.Assertions.*;

class VariableTest {

    @org.junit.jupiter.api.Test
    void makeTable() {
        BayesianNetwork net = new BayesianNetwork();
        Variable a = new Variable("A");
        a.addOutCome("T");
        a.addOutCome("F");
        Variable b = new Variable("B");
        b.addOutCome("T");
        b.addOutCome("F");
        b.addParents(a);
        a.addChildren(b);
        net.addVar(a);
        net.addVar(b);
        System.out.println(b);
        b.makeTable(new String[]{"0.9", "0.1", "0.2", "0.8"});

    }
}