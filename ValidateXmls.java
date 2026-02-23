import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ValidateXmls {

    public static void main(String[] args) {
        String dataDirectoryPath = "/home/fernando/L2J_Mobius/L2J_Mobius_C4_ScionsOfDestiny/dist/game/data";
        if (args.length > 0) {
            dataDirectoryPath = args[0];
        }

        System.out.println("=== Iniciando Validación Masiva de XMLs contra XSD (Java) ===");
        int totalFiles = 0;
        int passed = 0;
        List<String[]> failed = new ArrayList<>();

        Path dataDir = Paths.get(dataDirectoryPath);

        try (Stream<Path> paths = Files.walk(dataDir)) {
            List<Path> xmlFiles = paths.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".xml"))
                    .collect(Collectors.toList());

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            dbFactory.setNamespaceAware(true);
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

            for (Path xmlPath : xmlFiles) {
                totalFiles++;
                File xmlFile = xmlPath.toFile();
                try {
                    Document doc = dBuilder.parse(xmlFile);
                    Element rootElement = doc.getDocumentElement();
                    String xsdAttr = rootElement.getAttributeNS("http://www.w3.org/2001/XMLSchema-instance",
                            "noNamespaceSchemaLocation");

                    if (xsdAttr != null && !xsdAttr.isEmpty()) {
                        File xsdFile = new File(xmlFile.getParentFile(), xsdAttr);
                        if (!xsdFile.exists()) {
                            xsdFile = new File(dataDir.toFile(), xsdAttr);
                        }
                        if (!xsdFile.exists()) {
                            // Fallbacks
                            xsdFile = new File(new File(dataDir.toFile(), "xsd"), new File(xsdAttr).getName());
                        }

                        if (xsdFile.exists()) {
                            Schema schema = schemaFactory.newSchema(xsdFile);
                            Validator validator = schema.newValidator();
                            validator.validate(new StreamSource(xmlFile));
                            passed++;
                        } else {
                            failed.add(new String[] { xmlPath.toString(), "XSD no encontrado: " + xsdAttr });
                        }
                    } else {
                        failed.add(new String[] { xmlPath.toString(),
                                "No especifica XSD (xsi:noNamespaceSchemaLocation faltante)" });
                    }
                } catch (Exception e) {
                    failed.add(new String[] { xmlPath.toString(), "Error de parseo/validación: " + e.getMessage() });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("\n=== Resumen de Validación ===");
        System.out.println("Total XML escaneados: " + totalFiles);
        System.out.println("Aprobados: " + passed);
        System.out.println("Con Errores o Sin XSD (" + failed.size() + "):");

        if (!failed.isEmpty()) {
            System.out.println("\n--- Detalles de Errores ---");
            for (String[] errorDetail : failed) {
                String relPath = dataDir.relativize(Paths.get(errorDetail[0])).toString();
                System.out.println("[X] " + relPath + " -> " + errorDetail[1]);
            }
        }
    }
}
