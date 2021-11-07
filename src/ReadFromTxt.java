import java.io.File;
import java.util.*;
// reading from Text File
// using Scanner Class
// return Queue of lines of the .txt file

public class ReadFromTxt
{
    public static Queue<String> read(String fileName) throws Exception {
        Queue<String> ans = new LinkedList<String>();
        // pass the path to the file as a parameter
        File file = new File(fileName);
        Scanner sc = new Scanner(file);

        while (sc.hasNextLine())
            ans.add(sc.nextLine());
    return ans;
    }
}