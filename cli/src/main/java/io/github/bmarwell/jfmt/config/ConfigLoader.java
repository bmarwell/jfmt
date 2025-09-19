package io.github.bmarwell.jfmt.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ConfigLoader {

    public static Map<String, String> load(String resourcePath) {
        try (var in = ConfigLoader.class.getResourceAsStream(resourcePath)) {
            return loadConfig(in);
        } catch (IOException ioException) {
            throw new UncheckedIOException(ioException);
        } catch (SAXException | ParserConfigurationException xmlException) {
            throw new IllegalStateException(xmlException);
        }
    }

    public static Map<String, String> load(Path externalConfig) {
        try (var in = Files.newInputStream(externalConfig)) {
            return loadConfig(in);
        } catch (IOException ioException) {
            throw new UncheckedIOException(ioException);
        } catch (SAXException | ParserConfigurationException xmlException) {
            throw new IllegalStateException(xmlException);
        }
    }

    static Map<String, String> loadConfig(InputStream in)
        throws ParserConfigurationException, SAXException, IOException {
        Map<String, String> map = new ConcurrentHashMap<>();

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

        return Map.copyOf(map);
    }
}
