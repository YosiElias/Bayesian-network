import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * This class reads and writes
 * to a txt file using static functions
 */
public class ReadFromTxt
{
    /**
     * reading from Text File
     * using Scanner Class
     * return Queue of lines of the .txt file
     * @throws Exception
     */
    public static Queue<String> read(String fileName) throws Exception {
        Queue<String> ans = new LinkedList<String>();
        // pass the path to the file as a parameter
        File file = new File(fileName);
        Scanner sc = new Scanner(file);

        while (sc.hasNextLine())
            ans.add(sc.nextLine());
    return ans;
    }

    public static void write(String fileName, String str){
        try {
            FileWriter myWriter = new FileWriter(fileName);
            myWriter.write(str);
            myWriter.close();
        } catch (
                IOException e) {
            e.printStackTrace();
        }
    }
}
