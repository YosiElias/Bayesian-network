import java.text.DecimalFormat;
import java.util.*;

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

    public Factor(Variable v, BayesianNetwork net){
        _v = v;
        _net = net;
        String [][] table = v.getTable();
        nameV = new ArrayList<String>();
//        nameDiffFromFmin = new ArrayList<String>(); //names that not in this factor but yes in 'fmin' factor
        commonNames = new ArrayList<String>(); //names that are in this factor and in 'fmin' factor
        List<Variable> parents = v.getParents();
        for (int i = 0; i < parents.size(); i++) {
            nameV.add(parents.get(i).name);
        }
        nameV.add(v.name);
        nameV.add("_P_");
        if (nameV.size()!=table[0].length) System.err.println("Eror value: 'nameV' have to be the same size of 'table'");  //Todo: dbs
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

    private List<Integer> oneValueColumns(String[][] table) {
        List<Integer> ans  = new ArrayList<Integer>();
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
                    nameV.remove(c);
                }
            }
        } //Todo: --- try ---
        return ans;
    }

    public Factor(String[][] table, List<String> names, BayesianNetwork net){
        _net = net;
        nameV = new ArrayList<String>();
//        nameDiffFromFmin = new ArrayList<String>(); //names that not in this factor but yes in 'fmin' factor
        commonNames = new ArrayList<String>(); //names that are in this factor and in 'fmin' factor
        //deep copy of names:
        for (int i = 0; i < names.size(); i++) {
            nameV.add(names.get(i));
        }
        if (nameV.size()!=table[0].length && nameV.size()+1!=table.length)  //
            System.err.println("Eror value: 'nameV' have to be the same size of 'table'");  //Todo: dbs
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





    public List<String> getNameV() { return nameV; }


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
//        while (r<newTable.length)
//        {
            for (; r < newTable.length; r++) {     //run on rows of new table and write value T/F etc.
                for (c=0; c < this._table[0].length -1; c++) {        //run on columns
                    newTable[r][c] = this._table[r % this._table.length][c];    //write value in 'loop' of original table on the new table
                }
            }
//        }
        //continue write the table from other factor:

//        for (; r < this._table.length; r++) {     //run on rows
//            for (; c < this._table[0].length; c++) {        //run on columns
//                newTable[r][c] = this._table[r][c];
//            }
//        }
        boolean firstloop = true;
        int slice=0;    //"empty" mining because have to change is value at the start of loop
        for (int i = 0; i < nameDiffFromFmin.size(); i++) { //add all the names that not in original factor (this)
            List<String> outcomeOfMin = _outcomFmin.get(nameDiffFromFmin.get(i));
            if (firstloop)
            {
                firstloop = false;
                slice = rows / outcomeOfMin.size();
            }
            if (slice==0) System.err.println("slice can't be 0!");  //Todo: dbs, internal test
            else
            {
                slice = rows / outcomeOfMin.size();
                if (r % rows / outcomeOfMin.size()!=0)     System.err.println("number of rows is incorrect ");     //Todo: delete before submit
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
//            DecimalFormat df = new DecimalFormat("#0.00000");
            newTable[i][newTable[0].length-1] = ""+pOfOriginal * pOfFmin;   //Todo: maybe add it here -> df.format(
            _mulNum++;
        }


//        List<Integer> doneRows = new ArrayList<Integer>();
        //fill the value of 'P' in newTable:
//        for (int i = 0; i < newTable.length; i++)      //run on rows of new table
//        {
//            if (!doneRows.contains(i))  //only if we didn't done to fill this row
//            {
//                String[] valueOfCommon = getValueOfCommon(newTable, i, commonNames);
//                List<Integer> colmnofCommon = getColmnOfVnames(nameV, commonNames);
//                List<Integer> rowsOfcommon = getRowOfCommon(newTable, valueOfCommon, colmnofCommon);
//                for (int j = 0; j < rowsOfcommon.size(); j++)
//                { //add all this rows to done rows
//                    doneRows.add(rowsOfcommon.get(j));
//                }
//                for (int j = 0; j < rowsOfcommon.size(); j++)
//                {
//                    int row = rowsOfcommon.get(j);
//                    if (commonNames != nameV)   //if not all names are common:
//                    {
//                        for (int k = 0; k < newTable[row].length; k++) {    //run on columns of this row
//                            if (!colmnofCommon.contains(k)) //run only on columns that not in common
//                            {
//                                if (_indexOriginalNames.containsKey(nameV.get(k)))  //if the name belong to original factor (this):
//                                {
//                                    double
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
//
//        // Todo: dbs, only for testing.
//        System.out.println("********************** - Join by - "+byVar+" - "+this.nameV+" & "+fmin.nameV+"**********************");
//        for (int i = 0; i < newTable.length; i++) {
//            System.out.println("\n"+ Arrays.toString(newTable[i]));
//        }
//        System.out.println("**********************\n\n**********************");


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
                    if (row[_indexNewNames.get(name)] != table[r][c])
                        break;  //continue to next row  //Todo: check if this really break the loop of 'n'
                }
            }
            if (n == nameV.size() - 1)  //i.e. all the name are equal
            {
                find = true;
                break;  //Todo: check if this really break the loop of 'r'
            }
        }
        if (!find) System.err.println("not possible that not find any row appropriate");    //Todo: dbs
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
                        break;  //continue to next row  //Todo: check if this really break the loop of 'n'
                }
            }
            if (n == nameV.size() - 1)  //i.e. all the name are equal
            {
                find = true;
                break;  //Todo: check if this really break the loop of 'r'
            }
        }
        if (!find) System.err.println("not possible that not find any row appropriate");    //Todo: dbs
        return Double.parseDouble(this._table[r][_table[0].length -1]);
    }









    private void indexOfOther(Factor fmin) {
        _indexOtherNames = new LinkedHashMap<String, Integer>();
        for (int i = 0; i < fmin.getNameV().size(); i++) {
            _indexOtherNames.put(fmin.getNameV().get(i), i);
        }
    }

    private List<Integer> getRowOfCommon(String[][] newTable, String[] valueOfCommon, List<Integer> colmnOfCommon) {
        List<Integer> ans  = new ArrayList<Integer>();
        for (int r = 0; r < newTable.length; r++)
        {
            int i = 0;
            for (; i < colmnOfCommon.size(); i++)
            {
                if (newTable[r][colmnOfCommon.get(i)] != valueOfCommon[i])
                    break;  //continue to the next line
            }
            if (i == colmnOfCommon.size() - 1)  //i.e. all the value in this row
                ans.add(r);
        }
        return ans;
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
                this.nameV.add(nameV.size()-1, minNameV);   //Todo: nameV.size() - 2,  - check this change
//                if (!nameV.get(nameV.size()-2).equals(_v.name)) System.err.println("Eror insert: insert 'fName' in the wrong place on 'nameV'");  //Todo: dbs
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
        if (columns == 1)   //is empty factor   //Todo: testing for empty factor and check what do whis that after build it
        {
            if (nameV.get(0) != "_P_") System.err.println("The factor is not really empty");    //Todo: dbs
            return new Factor(this._table, this.nameV,_net);    //Todo: check if nameV really contain only 'P'
        }
//        _addNum = rows;
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
//        System.out.println("********************** - elimination by - "+byVar+ " - **********************");
////         Todo: dbs, only for testing.
//        for (int i = 0; i < newTable.length; i++) {
//            System.out.println("\n"+ Arrays.toString(newTable[i]));
//        }
//        System.out.println("**********************\n\n**********************");

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
                if (name != byVar) //if name is byVar name: //Todo: not need this if. dbs
                {
                    int c = _indexOriginalNames.get(name);
                    if (row[n] != this._table[r][c])     //row[_indexNewNames.get(name)]
                        break;  //continue to next row  //Todo: check if this really break the loop of 'n'
                }
            }
            if (n == nameV.size() - 1)  //i.e. all the name are equal
            {
                ans += Double.parseDouble(this._table[r][_table[0].length -1]);
                _addNum++;    //Todo: check if this is not the way noam want to count '_addNum'
            }
        }
        if (ans==0) System.err.println("not possible that not find any row appropriate");    //Todo: dbs
        _addNum--;
        return ans;
    }


    public double[] getValue(String qValue) {
        double[] ans = new double[2];
        ans[1] = 0;
        DecimalFormat df = new DecimalFormat("#0.00000");
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



}














