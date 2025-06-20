package code;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class MapFilterZima {

    public static Set<String> allUncrossableObjectsIds;
    public static Set<Element> mapUncrossableObjects;

    public static void main(String[] args) throws ParserConfigurationException, IOException, SAXException {
        allUncrossableObjectsIds = new HashSet<>();
        allUncrossableObjectsIds.addAll(Arrays.asList("201", "206", "301", "307", "411", "515",
                "518", "529", "708", "709", "714"));
        // may be added: 203, 509.1, 520, 521, 522.1, 533
        mapUncrossableObjects = new HashSet<>();

        Scanner scanner = new Scanner(System.in);

        //String OMAPFile = scanner.next();
        //File file = new File(OMAPFile);
        //File file = new File("test/OMAPFileTester.xml");  //temporary
        File file = new File("testing_omap_map.omap");

        // Factory that enables obtain a parser that produces DOM objects from XML
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        // concrete XML file parser
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

        // parsed document
        Document document = documentBuilder.parse(file);

        document.getDocumentElement().normalize();
        // document.getElementsByTagName returns NodeList instance
        NodeList fileObjects = document.getElementsByTagName("object");

        System.out.println("All objecst that given map contains:");
        System.out.println("____________________________________");
        int liner = 0;
        for (int i = 0; i < fileObjects.getLength(); i++) {
            Element element = (Element) fileObjects.item(i);
            System.out.print(element.getAttribute("name")+ " | ");
            if(liner == 5 || i+1 == fileObjects.getLength()){
                System.out.println();
            }
        }
        System.out.println("------------------------------------");
        System.out.println();
        System.out.println("All uncrossable objects that given map contains:");
        System.out.println("________________________________________________");
        liner = 0;
        for (int i = 0; i < fileObjects.getLength(); i++) {
            Element element = (Element) fileObjects.item(i);
            if(allUncrossableObjectsIds.contains(element.getAttribute("symbol"))){
                mapUncrossableObjects.add(element);
                System.out.print(element.getAttribute("name") + " | ");
                liner++;
            }
            if(liner == 5 || i+1 == fileObjects.getLength()){
                liner = 0;
                System.out.println();
            }
        }
        System.out.println("------------------------------------------------");
    }
}
