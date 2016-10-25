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
//    Map<String, String> elementEnum = new HashMap<>();

    File xsd;

    public XsdData(File xsd) {
        this.xsd = xsd;
        loadEnums(xsd);
    }

    private void loadEnums(File xsd) {
        try {
            DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
            domFactory.setValidating(false);
            domFactory.setNamespaceAware(true);
            domFactory.setIgnoringComments(true);
            domFactory.setIgnoringElementContentWhitespace(true);
            DocumentBuilder db = domFactory.newDocumentBuilder();
            Document doc = db.parse(new FileInputStream(xsd));

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

            NodeList elements = (NodeList) xpath.evaluate("//xs:element[@type]", doc, XPathConstants.NODESET);

            for (int i = 0; i < elements.getLength(); i++) {
                Element element = (Element) elements.item(i);
                List<String> enumVals = getEnums(doc, xpath, element.getAttribute("type"));
                if (enumVals != null && !enumVals.isEmpty()) {
                    enums.put(element.getAttribute("name"), enumVals);
                }
            }
        } catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException ex) {
            Logger.getLogger(XsdData.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private List<String> getEnums(Document doc, XPath xpath, String type) {
        List<String> vals = new ArrayList<>();
        try {
            Element element = (Element) xpath.evaluate("//xs:simpleType[@name='" + type + "']", doc, XPathConstants.NODE);
            if (element != null) {
                NodeList unions = element.getElementsByTagName("xs:union");
                if (unions != null && unions.getLength() > 0) {
                    String[] memebers
                            = ((Element) unions.item(0)).getAttribute("memberTypes").split(" ");
                    for (String memeber : memebers) {
                        vals.addAll(getEnums(doc, xpath, memeber));
                    }
                } else {
                    NodeList enumerations = element.getElementsByTagName("xs:enumeration");
                    if (enumerations != null && enumerations.getLength() > 0) {
                        for (int i = 0; i < enumerations.getLength(); i++) {
                            Element enumVal = (Element) enumerations.item(i);
                            vals.add(enumVal.getAttribute("value"));
                        }
                        return vals;
                    }
                }
            }
        } catch (XPathExpressionException ex) {
            Logger.getLogger(XsdData.class.getName()).log(Level.SEVERE, null, ex);
        }
        return vals;
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
        if (enums.containsKey(elementName)) {
            return enums.get(elementName);
        }
        return null;
    }


}
