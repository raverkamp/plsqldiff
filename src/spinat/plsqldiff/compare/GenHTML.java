package spinat.plsqldiff.compare;


import java.io.FileWriter;
import java.io.Writer;
import java.util.ArrayList;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import org.xml.sax.helpers.AttributesImpl;
import spinat.plsqldiff.scanner.Token;

public class GenHTML {
        static void addChars(TransformerHandler hd, String s) throws Exception {
        char[] a = s.toCharArray();
        hd.characters(a, 0, a.length);
    }
    
    // fill a line in the source (left or right, does not matter
    public static void fill1(TransformerHandler hd, ArrayList<DisplayedToken> al ) throws Exception {
           start(hd,"td", "lineno");
           if (!al.isEmpty()) {
               DisplayedToken m = al.get(0);
               addChars(hd, ""+m.token.getLineno());
           }
           
            ende(hd,"td");
            start(hd,"td", "code");

            if (!al.isEmpty()) {
               DisplayedToken m = al.get(0);
               Token t = m.token;
              if (t.getColno()>0) {
                  start(hd,"span","pad");
                  addChars(hd, "                                                                                                                                                                                                                       ".substring(0,t.getColno()));
                  ende(hd,"span");
              }       
           }
           
            for (DisplayedToken m : al) {
                if (m.highlight) {
                    start(hd, "span", "hot");
                    addChars(hd, m.token.value);
                    ende(hd, "span");
                } else {
                    addChars(hd, m.token.value);
                }
            }
            ende(hd, "td");
           
    }
    
    public static void addStyleSection(TransformerHandler hd) throws Exception {
        AttributesImpl a = new AttributesImpl();
        a.addAttribute("","","type","STRING","text/css");
        a.addAttribute("","","media","STRING","screen");
        hd.startElement("","","style", a);
        StringBuilder b = new StringBuilder();
        b.append(".code {white-space: pre-wrap;  font-family: monospace;}\n");
        b.append(".hot {background-color:#FF0000;}\n");
        b.append(".pad {background-color:#CCCCCC;}\n");
        b.append(".lineno {background-color:#CCCCCC;}\n");
        addChars(hd, b.toString());
        hd.endElement("","","style");
    }
    
    public static void start(TransformerHandler hd,String element,String cssclass) throws Exception {
        AttributesImpl a = new AttributesImpl();
        a.addAttribute("","","class","STRING",cssclass);
        hd.startElement("","",element, a);
    }
    
    public static void ende(TransformerHandler hd,String element) throws Exception {
         hd.endElement("","",element);
    }
    
    
    public static void printDiff(String fileName, CompareResult rs) throws Exception {
        printDiff(fileName,rs,"links","rechts");
    }
    

    public static void printDiff(String fileName, 
            CompareResult rs,
            String fileleft,String fileright) throws Exception {
      
        FileWriter w = new FileWriter(fileName);
        TransformerHandler hd = start(w);
        hd.startDocument();
        AttributesImpl atts = new AttributesImpl();
        hd.startElement("", "", "html", atts);
        hd.startElement("", "", "head", atts);
        addStyleSection(hd);
        hd.endElement("", "", "head");
        hd.startElement("", "", "body", atts);
        // PrintStream ps = new PrintStream(res);
        
        AttributesImpl tableatts = new AttributesImpl();
        tableatts.addAttribute("","","width","STRING","80%");
        tableatts.addAttribute("","","rules","STRING","rows");
        hd.startElement("","","h3",atts);
        if (rs.distance==0) {
          addChars(hd, "Files are equal");
        } else {
          addChars(hd, "Edit distance is " + rs.distance);
        }
        hd.endElement("","","h3");
        
        hd.startElement("", "", "table", tableatts);
        hd.startElement("", "", "tr", atts);
        {
            hd.startElement("", "", "th", atts);
            addChars(hd, "LineNo");
            hd.endElement("", "", "th");

            hd.startElement("", "", "th", atts);
            addChars(hd, fileleft);
            hd.endElement("", "", "th");

            hd.startElement("", "", "th", atts);
            addChars(hd, "LineNo");
            hd.endElement("", "", "th");

            hd.startElement("", "", "th", atts);
            addChars(hd, fileright);
            hd.endElement("", "", "th");

        }
        hd.endElement("", "", "tr");

        //ps.println("<table  cellpadding=\"0\"  cellspacing=\"0\"><tr><th>links</th><th>rechts</th></tr>");
        int n1 = rs.lines1.size();
        for (int i = 0; i < n1; i++) {
            hd.startElement("", "", "tr", atts);
            
            
            fill1(hd,(ArrayList) rs.lines1.get(i));
            fill1(hd,(ArrayList) rs.lines2.get(i));
            hd.endElement("", "", "tr");
            //ps.println("<td style=\"white-space: pre;  font-family: monospace;\">");

        }
        hd.endElement("", "", "table");
        hd.endElement("", "", "body");
        hd.endElement("", "", "html");
        hd.endDocument();
        w.close();
    }

    public static TransformerHandler start(Writer sw) throws TransformerConfigurationException {
        StreamResult streamResult = new StreamResult(sw);
        SAXTransformerFactory tf = (SAXTransformerFactory) SAXTransformerFactory.newInstance();

        TransformerHandler hd = tf.newTransformerHandler();
        Transformer serializer = hd.getTransformer();
        serializer.setOutputProperty(OutputKeys.ENCODING, "ISO-8859-1");
     
        hd.setResult(streamResult);
        return hd;
    }    

}
