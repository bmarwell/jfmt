package io.github.bmarwell.jdtfmt.config;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ConfigLoader {

    public static final String DEFAULT_CONFIG = "default-config.xml";

    public static Map<String, String> load() {
        Map<String, String> map = new HashMap<>();

        try (var in = ConfigLoader.class.getResourceAsStream(DEFAULT_CONFIG)) {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(in);

            NodeList settings = doc.getElementsByTagName("setting");
            for (int i = 0; i < settings.getLength(); i++) {
                Element setting = (Element) settings.item(i);
                String id = setting.getAttribute("id");
                String value = setting.getAttribute("value");
                map.put(id, value);
            }

            return map;

        } catch (IOException ioException) {
            throw new UncheckedIOException(ioException);
        } catch (SAXException | ParserConfigurationException xmlException) {
            throw new IllegalStateException(xmlException);
        }

    }
}
