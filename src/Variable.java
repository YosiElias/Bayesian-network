import java.util.*;

public class Variable {
        public final String name;
        private List<Variable> parents;
        private List<Variable> children;
        private List<String> outCome;
        private List<List<String>> keyToTable;
        //        private Map<List<String>, Double> table;
        private String[][] table;



        public Variable(String name) {
                this.name = name;
                parents = new ArrayList<Variable>();
                children = new ArrayList<Variable>();
                outCome = new ArrayList<String>();
                keyToTable = new ArrayList<List<String>>();
        }
//        public Variable(Variable other) {
//                this.name = other.name;
//                parents = new ArrayList<Variable>();
//                children = new ArrayList<Variable>();
//                outCome = new ArrayList<String>();
//                keyToTable = new ArrayList<List<String>>();
////                table = new LinkedHashMap<List<String>, Double>();
//        }


        public void makeTable(String [] probaArr){
                int row = outCome.size(), col = 2 + parents.size();
                for (int i = 0; i < parents.size(); i++) {
                        row *= parents.get(i).outCome.size();   //at the example of A|E,B => 2*2*2
                }
                table = new String[row][col];

                col = 0;
                boolean firstloop = true;
                int slice;
                if (parents.size()!=0)  //first slice initial
                        slice = row / parents.get(0).outCome.size();
                else
                        slice = row / this.outCome.size();      //actually, slice=1
                for (Variable parent: parents) { //run on all the parents of V
                        if (!firstloop) {
                                slice = slice / parent.outCome.size();
                                if (row % parent.outCome.size()!=0)     System.err.println("number of rows is incorrect ");     //Todo: delete before submit
                        }
                        firstloop=false;
                        int r=0;
                        while (r<row){ //run on all rows
                                for (int out = 0; r < row && out < parent.outCome.size(); out++) { //loop on all 'outComes'
                                        for (int i = 0; r < row && i < slice; i++) { //run on slice size
                                                table[r][col] = parent.outCome.get(out % parent.outCome.size()); //init part of the first column to the first outcome of first var
                                                r++;
                                        }
                                }
                        }
                        col++;  //go to the next var (in the next column)
                }

                if (parents.size()!=0)
                        slice = slice / this.outCome.size();
                int r=0;
                while (r<row){ //run on rows of this V
                        for (int out = 0; r < row && out < this.outCome.size(); out++) { //loop on all 'outComes'
                                for (int i = 0; r < row && i < slice; i++) { //run on slice size
                                        table[r][col] = this.outCome.get(out % this.outCome.size()); //init part of the first column to the first outcome of first var
                                        r++;
                                }
                        }
                }
                col++;  //go to the next column, the probabilities columns:

                for (int i = 0; i < row; i++) {
                        table[i][col] = probaArr[i];
                }
        }



        public List<Variable> getParents() { return parents; }

        public void addParents(Variable parent) { this.parents.add(parent); }

        public List<Variable> getChildren() { return children; }

        public void addChildren(Variable children) { this.children.add(children); }

        public List<String> getOutCome() { return outCome; }

        public void addOutCome(String outCome) { this.outCome.add(outCome); }

        public String toString(){
                return "--------------------------------------------"+
                        "\nName: "+name+
                        "\noutCome: "+outCome+
                        "\nchildren: "+childrentoString()+
                        "\nparents: "+parentstoString()+
                        "\ntable:\n"+tabletoString();
        }

        private static String printRow(String[] row) {
                String ans = "";
                for (String i : row) {
                        ans += i;
                        ans += "\t";
                }
                ans += "\n";
                return ans;
        }


        private String tabletoString() {
               String ans = "";
               for (Variable p: parents)
                       ans += ""+p.name+"\t";
               ans += ""+this.name+"\t"+"P"+"\n";
                for (int i = 0; i < parents.size()+2; i++) {
                        ans += "-\t";
                }
                ans += "\n";
                for(String[] row : table) {
                        ans += printRow(row);
                }
                return ans;
        }

        private String parentstoString() {
                String ans = "";
                for (int i = 0; i < parents.size(); i++) {
                        ans += parents.get(i).name+", ";

                }
                return ans;
        }

        private String childrentoString() {
                String ans = "";
                for (int i = 0; i < children.size(); i++) {
                        ans += children.get(i).name+", ";

                }
                return ans;
        }


//        public Map<List<String>, Double> getTable() { return table; }

//        public void addTable(List<String> condition, Double probability) {
//                this.table.put(condition, probability);
//                this.keyToTable.add(condition);
//        }























}












