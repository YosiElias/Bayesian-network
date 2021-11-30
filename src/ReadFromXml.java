import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


/**
 * This class reads from
 * an xml file using a static function
 */
public class ReadFromXml {


    public static BayesianNetwork readBuild(String fileName) {
        BayesianNetwork net = new BayesianNetwork();
        // Instantiate the Factory
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try {

            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

            // parse XML file
            DocumentBuilder db = dbf.newDocumentBuilder();

            Document doc = db.parse(new File(fileName));

            doc.getDocumentElement().normalize();


            // get <VARIABLE>
            NodeList listVar = doc.getElementsByTagName("VARIABLE");
            int i=0;
            Node node = listVar.item(i);
            while (node != null){

                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    String name = element.getElementsByTagName("NAME").item(0).getTextContent();
                    Variable v = new Variable(name);
                    NodeList listOut = element.getElementsByTagName("OUTCOME");
                    for (int j = 0; j < listOut.getLength(); j++) {
                        v.addOutCome(element.getElementsByTagName("OUTCOME").item(j).getTextContent());
                    }
                    // get text
                    net.addVar(v);
                }
                i++;
                node = listVar.item(i);
            }


            // get <DEFINITION>
            NodeList listDef = doc.getElementsByTagName("DEFINITION");
            i=0;
            Node nodeDef = listDef.item(i);
            while (nodeDef != null){

                if (nodeDef.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) nodeDef;
                    String name = element.getElementsByTagName("FOR").item(0).getTextContent();
                    Variable v = net.getVar(name);

                    if (element.getElementsByTagName("GIVEN") != null) {
                        NodeList listGiv = element.getElementsByTagName("GIVEN");
                        for (int j = 0; j < listGiv.getLength(); j++) {
                            String parentName = element.getElementsByTagName("GIVEN").item(j).getTextContent();
                            Variable parent = net.getVar(parentName);
                            v.addParents(parent);
                            parent.addChildren(v);
                        }
                    }

                    String[] table = element.getElementsByTagName("TABLE").item(0).getTextContent().split(" ");
                    v.makeTable(table);
                }
                i++;
                nodeDef = listDef.item(i);
            }

        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
        return net;
    }


}