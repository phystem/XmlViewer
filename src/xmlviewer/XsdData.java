/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xmlviewer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author Phystem
 */
public class XsdData {

    Map<String, List<String>> enums = new HashMap<>();
    Map<String, String> elementEnum = new HashMap<>();

    File xsd;

    public XsdData(File xsd) {
        this.xsd = xsd;
        loadEnums(xsd);
    }

    private void loadEnums(File xsd) {
        try {
            // Setup classes to parse XSD file for complex types
            DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
            domFactory.setValidating(false);
            domFactory.setNamespaceAware(true);
            domFactory.setIgnoringComments(true);
            domFactory.setIgnoringElementContentWhitespace(true);
            DocumentBuilder db = domFactory.newDocumentBuilder();
            Document doc = db.parse(new FileInputStream(xsd));

            // Given the id, go to correct place in XSD to get all the parameters
            XPath xpath = XPathFactory.newInstance().newXPath();
            NamespaceContext nsContext = new NamespaceContext() {

                @Override
                public String getNamespaceURI(String prefix) {
                    return "http://www.w3.org/2001/XMLSchema";
                }

                @Override
                public String getPrefix(String namespaceURI) {
                    return "xs";
                }

                @Override
                public Iterator getPrefixes(String namespaceURI) {
                    Set s = new HashSet();
                    s.add("xs");
                    return s.iterator();
                }

            };

            xpath.setNamespaceContext((NamespaceContext) nsContext);

            NodeList result = (NodeList) xpath.evaluate("//*[@name][xs:restriction/xs:enumeration]", doc, XPathConstants.NODESET);

            for (int i = 0; i < result.getLength(); i++) {
                Element e = (Element) result.item(i);
                String nodeName = e.getAttribute("name");
                NodeList elements = (NodeList) xpath.evaluate("//xs:element[@type='" + nodeName + "']", doc, XPathConstants.NODESET);
                for (int j = 0; j < elements.getLength(); j++) {
                    Element element = (Element) elements.item(j);
                    elementEnum.put(element.getAttribute("name"), nodeName);
                }
                enums.put(nodeName, new ArrayList<>());
                NodeList enumList = e.getElementsByTagName("xs:enumeration");
                for (int j = 0; j < enumList.getLength(); j++) {
                    Element enumVal = (Element) enumList.item(j);
                    enums.get(nodeName).add(enumVal.getAttribute("value"));
                }
            }

        } catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException ex) {
            Logger.getLogger(XsdData.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Boolean validate(Document document) {
        try {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Source schemaFile = new StreamSource(xsd);
            Schema schema = factory.newSchema(schemaFile);
            Validator validator = schema.newValidator();
            validator.validate(new DOMSource(document));
            return true;
        } catch (SAXException | IOException ex) {
            Logger.getLogger(XsdData.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    public List<String> getEnum(String elementName) {
        if (elementEnum.containsKey(elementName)) {
            return enums.get(elementEnum.get(elementName));
        }
        return null;
    }
}
