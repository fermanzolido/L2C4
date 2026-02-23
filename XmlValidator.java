import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

// Usamos el parser básico de Java para leer la cabecera xsi:noNamespaceSchemaLocation
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class XmlValidator {

    public static void main(String[] args) {
        String dataDir = "/home/fernando/L2J_Mobius/L2J_Mobius_C4_ScionsOfDestiny/dist/game/data";

        System.out.println("=== Iniciando Validación Masiva de XMLs contra XSD (Java) ===");

        int[] stats = { 0, 0, 0 }; // total, passed, failed

        try (Stream<Path> paths = Files.walk(Paths.get(dataDir))) {
            paths.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".xml"))
                    .forEach(xmlPath -> validateFile(xmlPath.toFile(), dataDir, stats));
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("\n=== Resumen de Validación ===");
        System.out.println("Total XML escaneados: " + stats[0]);
        System.out.println("Aprobados: " + stats[1]);
        System.out.println("Con Errores o Sin XSD: " + stats[2]);
    }

    private static void validateFile(File xmlFile, String dataDir, int[] stats) {
        stats[0]++;
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            dbFactory.setNamespaceAware(true);
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFile);

            Element root = doc.getDocumentElement();
            String xsdAttr = root.getAttributeNS("http://www.w3.org/2001/XMLSchema-instance",
                    "noNamespaceSchemaLocation");

            if (xsdAttr != null && !xsdAttr.isEmpty()) {
                File xsdFile = new File(dataDir, xsdAttr);
                if (!xsdFile.exists()) {
                    File altXsd = new File(dataDir + "/xsd/" + new File(xsdAttr).getName());
                    if (altXsd.exists()) {
                        xsdFile = altXsd;
                    } else {
                        System.out.println(
                                "[X] " + getRelativePath(xmlFile, dataDir) + " -> XSD no encontrado: " + xsdAttr);
                        stats[2]++;
                        return;
                    }
                }

                // Validar
                SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                Schema schema = factory.newSchema(xsdFile);
                Validator validator = schema.newValidator();
                validator.validate(new StreamSource(xmlFile));

                stats[1]++;
            } else {
                System.out.println("[X] " + getRelativePath(xmlFile, dataDir) + " -> No especifica XSD");
                stats[2]++;
            }
        } catch (Exception e) {
            System.out.println("[X] " + getRelativePath(xmlFile, dataDir) + " -> Error: " + e.getMessage());
            stats[2]++;
        }
    }

    private static String getRelativePath(File file, String base) {
        return new File(base).toURI().relativize(file.toURI()).getPath();
    }
}
