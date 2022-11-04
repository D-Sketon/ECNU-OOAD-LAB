package gizmoball.ui.visualize;

import javafx.scene.paint.Color;
import lombok.Data;
import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.util.XMLResourceDescriptor;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGSVGElement;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Data
public class SVGNode {

    protected List<SVGPath> svgPaths;

    protected double height;

    protected double width;

    private static final String SVG_NAMESPACE = "http://www.w3.org/2000/svg";
    private static final SAXSVGDocumentFactory FACTORY = new SAXSVGDocumentFactory(XMLResourceDescriptor.getXMLParserClassName());

    public static SVGNode fromResource(InputStream in){
        if(in == null){
            return null;
        }
        try {
            SVGNode svg = new SVGNode();
            svg.svgPaths = new ArrayList<>();
            SVGDocument document = FACTORY.createSVGDocument(SVG_NAMESPACE, in);
            // 此处只解析iconfont下载的svg文件
            SVGSVGElement root = document.getRootElement();
            svg.height = Double.parseDouble(root.getAttribute("width"));
            svg.width = Double.parseDouble(root.getAttribute("height"));
            NodeList nodeList = root.getChildNodes();
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                Node fill = node.getAttributes().getNamedItem("fill");
                String d = node.getAttributes().getNamedItem("d").getTextContent(); // assume not null
                svg.svgPaths.add(new SVGPath(d, fill != null ? Color.valueOf(fill.getTextContent()) : Color.BLACK));
            }
            return svg;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
