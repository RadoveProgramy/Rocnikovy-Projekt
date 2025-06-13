package code;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.awt.geom.Line2D;
import java.io.*;
import java.util.*;

public class MapFilterLeto {
    public static Set<String> DEFAULT_UNCROSSABLE_OBJECTS = new HashSet<>(Arrays.asList("201", "203", "206", "301", "307", "411", "509.1",
            "515", "518", "520", "521", "522.1", "529", "708", "709", "714"));
    public static Set<String> mapUncrossableObjects;

    public static void main(String[] args) throws Exception {
        mapUncrossableObjects = new HashSet<>();
        System.out.println("Zadajte subor: ");
        Scanner scanner = new Scanner(System.in);
        String name = scanner.next();
        //name+=".omap";
        File file = new File(name);
        //File file = new File("final_test.omap");


        // Document creation
        // Factory that enables obtain a parser that produces DOM objects from XML
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        // concrete XML file parser
        DocumentBuilder builder = factory.newDocumentBuilder();

        // parsed document
        Document document = builder.parse(file);
        // Normalization
        document.getDocumentElement().normalize();

        // Získanie definícií symbolov
        Map<String, String> symbolDefinitions = getSymbolDefinitions(document);

        // Získanie použitých symbolov v objektoch
        Set<String> usedSymbols = getUsedSymbols(document);

        // Výpis použitého symbolu a jeho názvu
        for (String symbolId : usedSymbols) {
            String symbolName = symbolDefinitions.get(symbolId);
            if (symbolName != null) {
                System.out.println("Použitý symbol: " + symbolName + " (ID: " + symbolId + ")");
            }
        }

        for (String symbolId : usedSymbols) {
            if (symbolId != null) {
                String symbolName = symbolDefinitions.get(symbolId);
                if (DEFAULT_UNCROSSABLE_OBJECTS.contains(symbolName)) {
                    mapUncrossableObjects.add(symbolName);
                    System.out.println("Kód neprekonateľnej prekážky použitej na mape: " + symbolName);
                }
            }
        }

        System.out.println("________________________________________________");
        System.out.println();

        // Vypisovanie neprekonatelnych prekazok na usecke (ceste ROAD)
        Map<String, List<Line2D.Double>> roadSegments = getObjectSegments(document, "110");

        for (String uncrossableSymbol : symbolDefinitions.keySet()) {
            String code = symbolDefinitions.get(uncrossableSymbol);
            if (!DEFAULT_UNCROSSABLE_OBJECTS.contains(code)) continue;

            Map<String, List<Line2D.Double>> uncrossableSegments = getObjectSegments(document, uncrossableSymbol);

            for (Map.Entry<String, List<Line2D.Double>> entry : uncrossableSegments.entrySet()) {
                for (Map.Entry<String, List<Line2D.Double>> roadEntry : roadSegments.entrySet()) {
                    if (intersects(entry.getValue(), roadEntry.getValue())) {
                        System.out.println("Neprekonateľný objekt: " + code +
                                " pretína cestu (ROAD)");
                    }
                }
            }
        }
    }

    // Funkcia na získanie všetkých definícií symbolov z dokumentu
    private static Map<String, String> getSymbolDefinitions(Document document) {
        Map<String, String> symbolDefinitions = new HashMap<>();

        NodeList symbolNodes = document.getElementsByTagName("symbol");
        for (int i = 0; i < symbolNodes.getLength(); i++) {
            Element symbolElement = (Element) symbolNodes.item(i);
            String symbolId = symbolElement.getAttribute("id");
            String symbolCode = symbolElement.getAttribute("code");
            symbolDefinitions.put(symbolId, symbolCode);
        }
        return symbolDefinitions;
    }

    // Funkcia na zistenie pouzitych symbolov
    private static Set<String> getUsedSymbols(Document document) {
        Set<String> usedSymbols = new HashSet<>();

        NodeList parts = document.getElementsByTagName("part");
        for (int i = 0; i < parts.getLength(); i++) {
            Element part = (Element) parts.item(i);
            NodeList objects = part.getElementsByTagName("object");

            for (int j = 0; j < objects.getLength(); j++) {
                Element objectElement = (Element) objects.item(j);
                String symbolId = objectElement.getAttribute("symbol");
                if (!symbolId.isEmpty()) {
                    usedSymbols.add(symbolId);
                }
            }
        }
        return usedSymbols;
    }


    // Vracia mapu: symbol -> zoznam úsečiek (každá úsečka je pole Point2D)
    private static Map<String, List<Line2D.Double>> getObjectSegments(Document document, String symbolFilter) {
        Map<String, List<Line2D.Double>> objectSegments = new HashMap<>();
        NodeList objectNodes = document.getElementsByTagName("object");

        for (int i = 0; i < objectNodes.getLength(); i++) {
            Element objectElement = (Element) objectNodes.item(i);
            String symbol = objectElement.getAttribute("symbol");
            if (!symbol.equals(symbolFilter)) continue;

            NodeList coordsNodes = objectElement.getElementsByTagName("coords");
            if (coordsNodes.getLength() == 0) continue;
            String[] points = coordsNodes.item(0).getTextContent().trim().split(";");

            List<Line2D.Double> segments = new ArrayList<>();
            for (int j = 0; j < points.length - 1; j++) {
                String[] p1 = points[j].trim().split(" ");
                String[] p2 = points[j + 1].trim().split(" ");
                if (p1.length < 2 || p2.length < 2) continue;

                double x1 = Double.parseDouble(p1[0]);
                double y1 = Double.parseDouble(p1[1]);
                double x2 = Double.parseDouble(p2[0]);
                double y2 = Double.parseDouble(p2[1]);
                segments.add(new Line2D.Double(x1, y1, x2, y2));
            }

            objectSegments.put(symbol, segments);   // pridame (symbol, suradnice)
        }
        return objectSegments;
    }

    // Funkcia na zistenie pretinania objektov
    private static boolean intersects(List<Line2D.Double> a, List<Line2D.Double> b) {
        for (Line2D.Double s1 : a) {
            for (Line2D.Double s2 : b) {
                if (s1.intersectsLine(s2)) {
                    return true;
                }
            }
        }
        return false;
    }
}
