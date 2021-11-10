import java.util.ArrayList;
import java.util.List;

public class Factor {
    private List<String> nameV;
    private String[][] _table;
    private BayesianNetwork _net;
    private Variable _v;




    public Factor(Variable v, BayesianNetwork net){
        _v = v;
        _net = net;
        String [][] table = v.getTable();
        nameV = new ArrayList<String>();
        List<Variable> parents = v.getParents();
        for (int i = 0; i < parents.size(); i++) {
            nameV.add(parents.get(i).name);
        }
        nameV.add(v.name);
        nameV.add("_P_");
        if (nameV.size()!=table.length) System.err.println("Eror value: 'nameV' have to be the same size of 'table'");  //Todo: dbs
        _table = new String[table.length][table[0].length];
        //deep copy:
        for (int r = 0; r < _table.length; r++) {     //run on rows
            for (int c = 0; c < this._table[0].length; c++) {        //run on columns
                _table[r][c] = table[r][c];
            }
        }
    }


    public List<String> getNameV() { return nameV; }


    public Factor join(Factor fmin, String byVar) {
        int mulRows = this.sizeOfRows(fmin);
        int rows = this._table.length * mulRows;
        sizeOfClumns(fmin);
        int columns = nameV.size();
        String[][] newTable = new String[rows][columns];

        //----------------------------------------- Todo: cuntinu from here ------------------------------
    }

    private void sizeOfClumns(Factor fmin) {
        int addSum = 0;
        for (String fName:fmin.getNameV())
        {
            if (!this.getNameV().contains(fName))
            {
                this.nameV.add(nameV.size() - 2, fName);
                if (!nameV.get(nameV.size()-2).equals(_v.name)) System.err.println("Eror insert: insert 'fName' in the wrong place on 'nameV'");  //Todo: dbs
            }
        }
    }

    private int sizeOfRows(Factor fmin){
        int mulSum = 1;
        for (String fName:this.getNameV())
        {
            if (!fmin.getNameV().contains(fName))
                mulSum *= _net.getVar(fName).getOutCome().size();
        }
        return mulSum;
    }



}
