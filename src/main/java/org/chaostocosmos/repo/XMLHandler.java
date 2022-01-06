package org.chaostocosmos.repo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * XMLHandler 
 */
public class XMLHandler {

    File xmlFile;
    DocumentBuilder documentBuilder;
    Document root;
    Element rootElement;

    /**
     * Constructor
     * @param xmlFile
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public XMLHandler(File xmlFile) throws ParserConfigurationException, SAXException, IOException {
        this.xmlFile = xmlFile;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        documentBuilder = factory.newDocumentBuilder();
        this.root = documentBuilder.parse(xmlFile);
        this.rootElement = this.root.getDocumentElement();
    }

    /**
     * Get XML file object
     * @return
     */
    public File getXMLFile() {
        return this.xmlFile;
    }

    /**
     * Save document 
     * @param outputFile
     * @param charset
     * @throws TransformerException
     * @throws IOException
     */
    public synchronized void saveDocument(File outputFile, String charset) throws TransformerException, IOException {
        if(!outputFile.exists()) {
            outputFile.getParentFile().mkdirs();
            outputFile.createNewFile();
        }
        TransformerFactory transformerFactory = TransformerFactory.newInstance();        
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.ENCODING, charset);        
        StreamResult result = new StreamResult(new FileOutputStream(outputFile));
        transformer.transform(new DOMSource(this.root), result);
    }

    /**
     * Remove not needed POM tag
     * @param saveFile
     * @throws TransformerException
     * @throws IOException
     */
    public void removeNotNeededAndSave(File saveFile) throws TransformerException, IOException {
        this.getStream(this.getRootElement().getChildNodes())
            .filter(n -> List.of("modelVersion", "groupId", "artifactId", "version", "name", "dependencies")
                            .stream()
                            .allMatch(e -> !e.equals(n.getTagName())))
            .filter(n -> n != null)
            .forEach(n -> {
                if(this.rootElement != null) {
                    this.rootElement.removeChild(n);
                }                
            });
        saveDocument(saveFile, "UTF-8");
    }

    /**
     * Get root element
     * @return
     */
    public synchronized Element getRootElement() {
        return this.rootElement;
    }

    /**
     * Clone root document
     * @param deepCopy
     * @return
     */
    public synchronized Document cloneRootDocument(boolean deepCopy) {
        return (Document)this.root.cloneNode(deepCopy);
    }

    /**
     * Get node names to List
     * @return
     */
    public List<String> getNodeNames() {
        Element element = root.getDocumentElement();
        NodeList nodeList = element.getElementsByTagName("node");
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            Element ele = (Element) node;
        }
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node instanceof Element) {
                Element element2 = (Element) nodeList.item(i);
            }
        }
        return null;
    }

    /**
     * Write document to get String return value
     * @param doc
     * @return
     * @throws TransformerException
     */
    public String writeDocumentToString(Document doc) throws TransformerException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();        
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        StringWriter sWriter = new StringWriter();
        StreamResult result = new StreamResult(sWriter);
        //transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        //transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        //transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes")
        transformer.transform(new DOMSource(doc), result);
        return sWriter.toString();
    }

    /**
     * Find element by tag
     * @param path
     * @return
     */
    public synchronized Element findElementByTag(String path) {
        return findElementByTag(path, this.root.getDocumentElement());
    }

    /**
     * Find element by tag
     * @param path
     * @param element
     * @return
     */
    public Element findElementByTag(final String path, Element element) {
        int idx = path.indexOf(".");
        if(idx == -1) {
            int idx2 = path.indexOf("#");
            if(idx2 == -1) {
                return getStream(element.getChildNodes(), path).findFirst().orElse(null);
            } else {
                String tag = path.substring(0, idx2);
                String path1 = path.substring(idx2+1);
                String[] att = path1.split("=", -1);
                return getStream(element.getChildNodes(), tag, att[0], att[1]).findFirst().orElse(null);
            }                        
        } else {
            String token = path.substring(0, idx);
            int idx2 = token.indexOf("#");
            Element ele;
            if(idx2 == -1) {
                String tag = token.substring(0, idx);
                return getStream(element.getChildNodes(), tag).map(e1 -> findElementByTag(path.substring(idx+1), e1)).filter(a -> a != null).findAny().orElse(null);
            } else { 
                String tag = token.substring(0, idx2);
                String[] attr = token.substring(idx2+1).split("=", -1);
                ele = getStream(element.getChildNodes(), tag, attr[0], attr[1]).map(e1 -> findElementByTag(path.substring(idx+1), e1)).filter(a -> a != null).findFirst().orElse(null);
            }
            if(ele == null) {
                //throw new RuntimeException("Specified Tag or Attribute pair might be wrong. Token: "+token);
            }
            return ele;
        }        
    }

    /**
     * Find element
     * @param path
     * @return
     */
    public Element findElement(String path) {
        return findElement(path, this.rootElement);
    }

    /**
     * Find element
     * @param path
     * @param element
     * @return
     */
    private Element findElement(String path, Element element) {
        int idx = path.indexOf(".");
        if(idx == -1) {
            String[] s = path.split("=", -1);
            Element el = getStream(element.getChildNodes()).filter(e -> e.getAttribute(s[0]).equals(s[1])).findFirst().orElse(null);
            return el != null ? el : null;
        } else {
            NodeList nodeList = element.getChildNodes();
            String path1 = path.substring(0, idx);
            //System.out.println(path1);
            String[] s = path1.split("=", -1);            
            Element element1 = getStream(nodeList).filter(e -> e.getAttribute(s[0]).equals(s[1])).findFirst().orElse(null);
            if(element1 == null) {
                return null;
            }
            return findElement(path.substring(path.indexOf(".")+1), element1);
        }
    }

    /**
     * Get stream
     * @param list
     * @return
     */
    public Stream<Element> getStream(NodeList list) {
        return IntStream.range(0, list.getLength())
                    .mapToObj(list::item)
                    .filter(n -> n instanceof Element)
                    .map(n -> (Element)n);
    }

    /**
     * Get stream
     * @param list
     * @param tag
     * @return
     */
    public Stream<Element> getStream(NodeList list, String tag) {
        return IntStream.range(0, list.getLength())
                    .mapToObj(list::item)
                    .filter(n -> n instanceof Element && ((Element)n).getTagName().equals(tag))
                    .map(n -> (Element)n);
    }

    /**
     * Get stream
     * @param list
     * @param attribute
     * @param value
     * @return
     */
    public Stream<Element> getStream(NodeList list, String attribute, String value) {
        return IntStream.range(0, list.getLength())
                    .mapToObj(list::item)
                    .filter(n -> n instanceof Element && ((Element)n).getAttribute(attribute).equals(value))
                    .map(e -> (Element)e);
    }

    /**
     * Get stream
     * @param list
     * @param tag
     * @param attribute
     * @param value
     * @return
     */
    public Stream<Element> getStream(NodeList list, String tag, String attribute, String value) {
        return IntStream.range(0, list.getLength())
                    .mapToObj(list::item)
                    .filter(n -> n instanceof Element && ((Element)n).getAttribute(attribute).equals(value) && ((Element)n).getTagName().equals(tag))
                    .map(e -> (Element)e);
    }

    /**
     * Get element
     * @param list
     * @param path
     * @return
     */
    public Stream<Element> getElement(NodeList list, String path) {
        int idx = path.indexOf(".");
        if(idx == -1) {
            String tag = path;
            return IntStream.range(0, list.getLength()).mapToObj(list::item).filter(n -> n instanceof Element && ((Element)n).getTagName().equals(tag)).map(n1 -> (Element)n1);
        } else {            
            String tag = path.substring(0, idx);
            path = path.substring(idx+1);
            return getElement(IntStream.range(0, list.getLength()).mapToObj(list::item).filter(n -> n instanceof Element && ((Element)n).getTagName().equals(tag)).map(n1 -> (Element)n1).findFirst().get().getChildNodes(), path);
        }
    }
}

