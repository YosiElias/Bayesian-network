import java.text.DecimalFormat;
import java.util.*;

/**
 * This class is used to
 * create a Factor object
 * in order to use it in
 * the Variable Elimination algorithm
 */

public class Factor {
    private List<String> nameV;
    private List<String> commonNames;
    private List<String> nameDiffFromFmin;
    private String[][] _table;
    private BayesianNetwork _net;
    private Variable _v;
    private Map<String, Integer> _indexOriginalNames;
    private Map<String, Integer> _indexOtherNames;
    private Map<String, Integer> _indexNewNames;
    private Map<String, List<String>> _outcomFmin;
    private Map<String, List<String>> _outcomThis;
    private int _mulNum = 0;
    private int _addNum = 0;


    public Variable get_v() {
        return _v;
    }
    // First constructor:
    public Factor(Variable v, BayesianNetwork net){
        _v = v;
        _net = net;
        String [][] table = v.getTable();
        nameV = new ArrayList<String>();
        commonNames = new ArrayList<String>(); //names that are in this factor and in 'fmin' factor
        List<Variable> parents = v.getParents();
        for (int i = 0; i < parents.size(); i++) {
            nameV.add(parents.get(i).name);
        }
        nameV.add(v.name);
        nameV.add("_P_");
        List<Integer> oneValueColumns = oneValueColumns(table);
        _table = new String[table.length][table[0].length - oneValueColumns.size()];
        //deep copy & reduce one value columns:
        for (int r = 0; r < table.length; r++) {     //run on rows
            for (int c = 0, thisC = 0; c < table[0].length; c++) {        //run on columns
                if (!oneValueColumns.contains(c)) {
                    _table[r][thisC] = table[r][c];
                    thisC++;
                }
            }
        }
        _indexOriginalNames = new LinkedHashMap<String, Integer>();
        for (int i = 0; i < nameV.size(); i++) {
            _indexOriginalNames.put(nameV.get(i), i);
        }

    }
    // Second constructor:
    public Factor(String[][] table, List<String> names, BayesianNetwork net){
        _net = net;
        nameV = new ArrayList<String>();
        commonNames = new ArrayList<String>(); //names that are in this factor and in 'fmin' factor
        //deep copy of names:
        for (int i = 0; i < names.size(); i++) {
            nameV.add(names.get(i));
        }
        List<Integer> oneValueColumns = oneValueColumns(table);
        _table = new String[table.length][table[0].length - oneValueColumns.size()];
        //deep copy & reduce one value columns:
        for (int r = 0; r < table.length; r++) {     //run on rows
            for (int c = 0, thisC = 0; c < table[0].length; c++) {        //run on columns
                if (!oneValueColumns.contains(c)) {
                    _table[r][thisC] = table[r][c];
                    thisC++;
                }
            }
        }
        _indexOriginalNames = new LinkedHashMap<String, Integer>();
        for (int i = 0; i < nameV.size(); i++) {
            _indexOriginalNames.put(nameV.get(i), i);
        }
    }

    //checks if there is only one value in a column:
    private List<Integer> oneValueColumns(String[][] table) {
        List<Integer> ans  = new ArrayList<Integer>();
        List<String> nameToRemove = new ArrayList<String>();
        if (table[0].length != 2) { //only if not 2 columns
            for (int c = 0; c < table[0].length - 1; c++) {
                String value = table[0][c];
                int r;
                for (r = 0; r < table.length; r++) {
                    if (!table[r][c].equals(value))
                        break;
                }
                if (r == table.length) {  //all column is equals
                    ans.add(c);
                    nameToRemove.add(nameV.get(c));
                }
            }
        }
        for (String name : nameToRemove){
            nameV.remove(name);
        }
        return ans;
    }

    public List<String> getNameV() { return nameV; }

    /**
     * the main function for the join
     * @param fmin the small factor
     * @param byVar variable name to join by
     * @return the joined factor
     */
    public Factor join(Factor fmin, String byVar) {
        indexOfOther(fmin);
        outcomeOfOther(fmin);
        int mulRows = this.sizeOfRows(fmin);
        int rows = this._table.length * mulRows;
        sizeOfClumns(fmin);
        int columns = nameV.size();
        String[][] newTable = new String[rows][columns];
        //deep copy from original table of the factor:
        int r= 0, c= 0;
        for (; r < newTable.length; r++) {     //run on rows of new table and write value T/F etc.
            for (c=0; c < this._table[0].length -1; c++) {        //run on columns
                newTable[r][c] = this._table[r % this._table.length][c];    //write value in 'loop' of original table on the new table
            }
        }

        //continue write the table from other factor:
        boolean firstloop = true;
        int slice=0;    //"empty" mining because have to change is value at the start of loop
        for (int i = 0; i < nameDiffFromFmin.size(); i++) { //add all the names that not in original factor (this)
            List<String> outcomeOfMin = _outcomFmin.get(nameDiffFromFmin.get(i));
            if (firstloop)
            {
                firstloop = false;
                slice = rows / outcomeOfMin.size();
                //Self checks:
                if (slice==0) System.err.println("slice can't be 0!");
            }
            else
            {
                slice = slice / outcomeOfMin.size();
                //Self checks:
                if (slice % outcomeOfMin.size()!=0)     System.err.println("number of rows is incorrect ");
            }
            r=0;
            while (r<rows){ //run on all rows
                for (int out = 0; r < rows && out < outcomeOfMin.size(); out++) { //loop on all 'outComes'
                    for (int j = 0; r < rows && j < slice; j++) { //run on slice size
                        newTable[r][c] = outcomeOfMin.get(out % outcomeOfMin.size()); //init part of the first column to the first outcome of first var
                        r++;
                    }
                }
            }
            c++;  //go to the next var (in the next column)
        }

        //fill the value of 'P' in newTable:
        for (int i = 0; i < newTable.length; i++)      //run on rows of new table
        {
            String[] row = newTable[i];
            double pOfOriginal = getPofRowOriginal(row);
            double pOfFmin = getPofRowOther(row, fmin._table);
            newTable[i][newTable[0].length-1] = ""+pOfOriginal * pOfFmin;
            _mulNum++;
        }
        return new Factor(newTable,nameV, _net);
    }

    private void outcomeOfOther(Factor fmin) {
        _outcomFmin = new LinkedHashMap<String, List<String>>();
        for (int r = 0; r < fmin._table.length; r++)
        {
            for (int i = 0; i < _indexOtherNames.size() -1; i++) {
                String name = fmin.nameV.get(i);
                String out = fmin._table[r][_indexOtherNames.get(name)];
                if (_outcomFmin.containsKey(name))
                {
                    if (!_outcomFmin.get(name).contains(out))
                        _outcomFmin.get(name).add(out);
                }
                else
                {
                    _outcomFmin.put(name, new ArrayList<String>());
                    _outcomFmin.get(name).add(out);
                }
            }
        }
    }

    private void outcomeOfThis() {
        _outcomThis = new LinkedHashMap<String, List<String>>();
        for (int r = 0; r < _table.length; r++)
        {
            for (int i = 0; i < _indexOriginalNames.size() -1; i++) {
                String name = nameV.get(i);
                String out = _table[r][_indexOriginalNames.get(name)];
                if (_outcomThis.containsKey(name))
                {
                    if (!_outcomThis.get(name).contains(out))
                        _outcomThis.get(name).add(out);
                }
                else
                {
                    _outcomThis.put(name, new ArrayList<String>());
                    _outcomThis.get(name).add(out);
                }
            }
        }
    }


    private double getPofRowOther(String[] row, String[][] table) {
        int r;
        boolean find = false;
        for (r = 0; r < table.length && find == false; r++)    //run on rows of small table
        {
            int n;
            for (n = 0; n < nameV.size()-1; n++)  //run on all names in newTable
            {
                String name = nameV.get(n);
                if (_indexOtherNames.containsKey(name)) //if name is belong to this table:
                {
                    int c = _indexOtherNames.get(name);
                    if (!row[_indexNewNames.get(name)].equals(table[r][c]))
                        break;  //continue to next row
                }
            }
            if (n == nameV.size() - 1)  //i.e. all the name are equal
            {
                find = true;
                break;
            }
        }
        //Self checks:
        if (!find)
            System.err.println("not possible that not find any row appropriate");
        return Double.parseDouble(table[r][table[0].length -1]);
    }


    private double getPofRowOriginal(String[] row) {
        int r;
        boolean find = false;
        for (r = 0; r < this._table.length && find == false; r++)    //run on rows of small table
        {
            int n;
            for (n = 0; n < nameV.size()-1; n++)  //run on all names in newTable
            {
                String name = nameV.get(n);
                if (_indexOriginalNames.containsKey(name)) //if name is belong to this table:
                {
                    int c = _indexOriginalNames.get(name);
                    if (row[_indexNewNames.get(name)] != this._table[r][c])
                        break;  //continue to next row
                }
            }
            if (n == nameV.size() - 1)  //i.e. all the name are equal
            {
                find = true;
                break;
            }
        }
        //Self checks:
        if (!find) System.err.println("not possible that not find any row appropriate");
        return Double.parseDouble(this._table[r][_table[0].length -1]);
    }

    private void indexOfOther(Factor fmin) {
        _indexOtherNames = new LinkedHashMap<String, Integer>();
        for (int i = 0; i < fmin.getNameV().size(); i++) {
            _indexOtherNames.put(fmin.getNameV().get(i), i);
        }
    }

    /**
     *
     * @param newTable the table to search the value from
     * @param row the row to search at
     * @return array of string contain the value of 'names' we search for, order by the order of 'names'
     */
    private String[] getValueOfCommon(String[][] newTable, int row, List<String> names) {
        List<Integer> indexOfCommon = getColmnOfVnames(nameV, names);
        String[] ans = new String[indexOfCommon.size()];
        for (int j = 0; j < indexOfCommon.size(); j++)
        {
            ans[j] = newTable[row][indexOfCommon.get(j)];
        }
        return ans;
    }



    /**
     *
     * @param nameList list of name to search in it which index is any name from namesToSearch
     * @param namesToSearch names to search there index
     * @return index in nameList of name from namesToSearch by the order of the name in namesToSearch
     */
    private List<Integer> getColmnOfVnames(List<String> nameList, List<String> namesToSearch) {
        List<Integer>  ans  = new ArrayList<Integer>();
        for (int i = 0; i < namesToSearch.size(); i++) {
            ans.add(nameList.indexOf(namesToSearch.get(i)));
        }
        return ans;
    }


    private void sizeOfClumns(Factor fmin) {
        int addSum = 0;
        nameDiffFromFmin = new ArrayList<String>(); //names that not in this factor but yes in 'fmin' factor
        for (String minNameV :fmin.getNameV())
        {
            if (!this.getNameV().contains(minNameV))
            {
                nameDiffFromFmin.add(minNameV);
                this.nameV.add(nameV.size()-1, minNameV);
            }
            else
            {
                commonNames.add(minNameV);
            }
        }
        _indexNewNames = new LinkedHashMap<String, Integer>();
        for (int i = 0; i < nameV.size(); i++) {
            _indexNewNames.put(nameV.get(i), i);
        }
    }

    private int sizeOfRows(Factor fmin){
        int mulSum = 1;
        for (String minNameV :fmin.getNameV())
        {
            if (!this.getNameV().contains(minNameV))
            {
                int outSize = _outcomFmin.get(minNameV).size();
                mulSum *= outSize;
            }
        }
        return mulSum;
    }


    public int getSizeOfTable(){
        return this._table.length * this._table[0].length;
    }

    public Factor eliminate(String byVar) {
        outcomeOfThis();
        this.nameV.remove(byVar);
        int rows = this._table.length / _outcomThis.get(byVar).size();
        int columns = this._table[0].length -1;
        if (columns == 1)   //is empty factor
        {
            //Self checks:
            if (nameV.get(0) != "_P_") System.err.println("The factor is not really empty");
            return new Factor(this._table, this.nameV,_net);
        }
        String[][] newTable = new String[rows][columns];
        String out = _outcomThis.get(byVar).get(0);
        int byVarColumn = _indexOriginalNames.get(byVar);
        for (int r = 0, r1=0; r < this._table.length; r++)
        {
            if (_table[r][byVarColumn] == out)
            {   //take only one outcome from byVar
                for (int c = 0, c1 = 0; c < this._table[0].length - 1; c++) {
                    if (c != byVarColumn) {   //not copy byVarColumn
                        newTable[r1][c1] = this._table[r][c];
                        c1++;
                    }
                }
                r1++;
            }
        }

        for (int i = 0; i < newTable.length; i++)      //run on rows of new table
        {
            String[] row = newTable[i];
            double pOfElim = getPofElim(row, byVar);
            newTable[i][newTable[0].length-1] = ""+pOfElim;
        }
        this._indexOriginalNames.clear();
        this._outcomThis.clear();
        return new Factor(newTable, this.nameV, _net);
    }

    private double getPofElim(String[] row, String byVar) {
        int r;
        double ans = 0;
        for (r = 0; r < this._table.length; r++)    //run on rows of small table
        {
            int n;
            for (n = 0; n < nameV.size()-1; n++)  //run on all names in newTable
            {
                String name = nameV.get(n);
                if (name != byVar) //if name is byVar name:
                {
                    int c = _indexOriginalNames.get(name);
                    if (row[n] != this._table[r][c])
                        break;  //continue to next row
                }
            }
            if (n == nameV.size() - 1)  //i.e. all the name are equal
            {
                ans += Double.parseDouble(this._table[r][_table[0].length -1]);
                _addNum++;
            }
        }
        //Self checks:
        if (ans==0) System.err.println("not possible that not find any row appropriate");
        _addNum--;
        return ans;
    }


    public double[] getValue(String qValue) {
        double[] ans = new double[2];
        ans[1] = 0;
        for (int i = 0; i < this._table.length; i++) {
            ans[1] += Double.parseDouble(this._table[i][1]);
            if (this._table[i][0].equals(qValue))
                ans[0] =  Double.parseDouble(this._table[i][1]);
        }
        return ans;
    }
    public int get_mulNum() {
        return _mulNum;
    }

    public int get_addNum() {
        return _addNum;
    }

    //print for easy debug
    public String toString() {
        String ans = "";
        for (int i = 0; i < this._table.length; i++) {
            ans += Arrays.toString(this._table[i]) + "\n";
        }
        return ans;
    }

}














