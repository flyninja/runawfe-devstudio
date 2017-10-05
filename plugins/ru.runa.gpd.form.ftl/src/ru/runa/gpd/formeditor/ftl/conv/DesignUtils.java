package ru.runa.gpd.formeditor.ftl.conv;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Map;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.dom4j.Element;
import org.dom4j.io.DOMReader;
import org.dom4j.io.HTMLWriter;
import org.dom4j.io.OutputFormat;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import ru.runa.gpd.formeditor.BaseHtmlFormType;
import ru.runa.gpd.formeditor.WebServerUtils;
import ru.runa.gpd.formeditor.ftl.Component;
import ru.runa.gpd.lang.model.Variable;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;

public class DesignUtils {
    public static final String PARAMETERS_DELIM = "|";
    public static final String ATTR_COMPONENT_TYPE = "type";
    public static final String ATTR_COMPONENT_ID = "id";
    public static final String ATTR_COMPONENT_PARAMETERS = "parameters";
    public static final String ATTR_STYLE = "style";

    public static String getComponentHtmlElementName() {
        return WebServerUtils.useCKEditor() ? "ftl_component" : "img";
    }

    public static String transformFromHtml(String html, Map<String, Variable> variables, Map<Integer, Component> components) throws Exception {
        if (html.length() == 0) {
            return html;
        }
        Document document = BaseHtmlFormType.getDocument(new ByteArrayInputStream(html.getBytes(Charsets.UTF_8)));
        NodeList componentElements = document.getElementsByTagName(getComponentHtmlElementName());
        List<Node> nodes = Lists.newArrayListWithExpectedSize(componentElements.getLength());
        for (int i = 0; i < componentElements.getLength(); i++) {
            Node componentNode = componentElements.item(i);
            nodes.add(componentNode);
        }
        for (Node node : nodes) {
            try {
                int id = Integer.valueOf(node.getAttributes().getNamedItem(ATTR_COMPONENT_ID).getNodeValue());
                Component component = components.get(id);
                if (component == null) {
                    throw new Exception("Component not found by id " + id);
                }
                String ftl = component.toString();
                Text ftlText = document.createTextNode(ftl);
                node.getParentNode().replaceChild(ftlText, node);
            } catch (Exception e) {
                throw new Exception("Unable to convert component from design html", e);
            }
        }
        return toString(document);
        // return toStringUsingDom4j(document);
    }

    private static String toStringUsingDom4j(Document document) throws Exception {
        org.dom4j.Document doc = new DOMReader().read(document);
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        OutputFormat format = new OutputFormat("  ", true);
        format.setTrimText(true);
        format.setSuppressDeclaration(true);
        format.setEncoding(Charsets.UTF_8.name());
        HTMLWriter writer = new HTMLWriter(new OutputStreamWriter(buffer, Charsets.UTF_8.name()), format);
        Element body = doc.getRootElement().element("BODY");
        for (org.dom4j.Node node : (List<org.dom4j.Node>) body.elements()) {
            writer.write(node);
        }
        writer.flush();
        return new String(buffer.toByteArray(), Charsets.UTF_8.name());
    }

    private static String toString(Document document) throws Exception {
        XPathFactory xpathFactory = XPathFactory.newInstance();
        XPathExpression xpathExp = xpathFactory.newXPath().compile("//text()[normalize-space(.) = '']");
        NodeList emptyTextNodes = (NodeList) xpathExp.evaluate(document, XPathConstants.NODESET);
        for (int i = 0; i < emptyTextNodes.getLength(); i++) {
            Node emptyTextNode = emptyTextNodes.item(i);
            emptyTextNode.getParentNode().removeChild(emptyTextNode);
        }
        Node bodyNode = document.getElementsByTagName("BODY").item(0);
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, Charsets.UTF_8.name());
        StringBuilder b = new StringBuilder();
        NodeList rootNodes = bodyNode.getChildNodes();
        for (int i = 0; i < rootNodes.getLength(); i++) {
            if (b.length() != 0) {
                b.append(System.lineSeparator());
            }
            Node rootNode = rootNodes.item(i);
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            transformer.transform(new DOMSource(rootNode), new StreamResult(os));
            b.append(new String(os.toByteArray(), Charsets.UTF_8.name()));
        }
        return b.toString();
    }
}
