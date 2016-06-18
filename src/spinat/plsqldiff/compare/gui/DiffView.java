package spinat.plsqldiff.compare.gui;

import spinat.plsqldiff.compare.DisplayedToken;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.*;
import javax.swing.text.*;
import spinat.plsqldiff.scanner.Token;

public class DiffView extends JPanel {

    final JTextPane edpl;
    final JScrollPane jspl;
    final JTextPane edpr;
    final JScrollPane jspr;
    final StyledDocument leftDocument;
    final AbstractDocument adocl;
    final StyledDocument rightDocument;
    final AbstractDocument adocr;
    final Style defStyle;
    final Style hotStyle;
    final Style greenStyle;
    final JSplitPane sp;
    final RowNumbers rownumbersLeft;
    final RowNumbers rownumbersRight;
    final JLabel headerLeft;
    final JLabel headerRight;
    final DiffLines diffLines;

    /*
     this component is used for showing the differences of
     two documents. 
     outer object is jpanel, i.e. this
     the structure is:
     +--------------------------------------------+
     |  empty label  |  headerleft  | headerright |
     |diffLines      |        splitpane           |
     |               |     left        righz      |
     +--------------------------------------------+
    
     where left and right are JScrollpanes which contain
     a JTextPane. The two JScrollpanes are synced for horizontal
     scrolling.
     */
    
    final int linedistance= 20;
    
    public DiffView()  {
        super();
        JPanel jpanel = this;
        jpanel.setLayout(new BorderLayout());
        
         Border headerBorder = BorderFactory.createEmptyBorder(3, 20, 3, 0);
        
        // the left side, the overview component
        {
            Box b = Box.createVerticalBox();
            JLabel dummyLabel = new JLabel(" ");
            dummyLabel.setBorder(headerBorder);
            b.add(dummyLabel);
            diffLines = new DiffLines(20);
            b.add(diffLines);
            jpanel.add(b, BorderLayout.WEST);
        }

        // the central part, the splitview
        sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        jpanel.add(sp, BorderLayout.CENTER);

        // the left side
        edpl = new JTextPane();
        // must be here, otherwise too late
        edpl.setEditorKit(new DiffEditorKit(linedistance));
        leftDocument = edpl.getStyledDocument();
        adocl = (AbstractDocument) leftDocument;
        jspl = new JScrollPane(edpl);
        jspl.setVerticalScrollBarPolicy(
                JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        jspl.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

        jspl.setPreferredSize(new Dimension(250, 145));
        jspl.setMinimumSize(new Dimension(10, 10));
        
        edpl.setEditable(false);

        JViewport jpl = new JViewport();
        rownumbersLeft = new RowNumbers(linedistance);
        jpl.setView(rownumbersLeft.compo);
        jspl.setRowHeader(jpl);
        JPanel pl1 = new JPanel();
        pl1.setLayout(new BorderLayout());
        headerLeft = new JLabel();
        // Insets is1 = new Insets(3, 10, 3, 0);

        headerLeft.setBorder(headerBorder);
        pl1.add(headerLeft, BorderLayout.NORTH);

        pl1.add(jspl, BorderLayout.CENTER);

        // the right side
        edpr = new JTextPane();
        // must be here, otherwise too late
        edpr.setEditorKit(new DiffEditorKit(linedistance));
        rightDocument = edpr.getStyledDocument();
        adocr = (AbstractDocument) rightDocument;
        jspr = new JScrollPane(edpr);
        jspr.setVerticalScrollBarPolicy(
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        jspr.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

        jspr.setPreferredSize(new Dimension(250, 145));
        jspr.setMinimumSize(new Dimension(10, 10));
        edpr.setEditable(false);

        rownumbersRight = new RowNumbers(linedistance);
        jspr.setRowHeaderView(rownumbersRight.compo);

        JPanel pl2 = new JPanel();
        pl2.setLayout(new BorderLayout());
        headerRight = new JLabel();
        headerRight.setBorder(headerBorder);
        pl2.add(headerRight, BorderLayout.NORTH);
        pl2.add(jspr, BorderLayout.CENTER);

        Style defaultStyle = StyleContext.getDefaultStyleContext().
                getStyle(StyleContext.DEFAULT_STYLE);
        defStyle = StyleContext.getDefaultStyleContext().addStyle("hot", defaultStyle);
        StyleConstants.setFontFamily(defStyle, Font.MONOSPACED);
        StyleConstants.setFontSize(defStyle, 12);
        StyleConstants.setForeground(defStyle, Color.black);
        hotStyle = StyleContext.getDefaultStyleContext().addStyle("hot", defStyle);
        StyleConstants.setBackground(hotStyle, Color.red);
        greenStyle = StyleContext.getDefaultStyleContext().addStyle("green", defStyle);
        StyleConstants.setBackground(greenStyle, Color.lightGray);

        sp.add(pl1, JSplitPane.LEFT);
        sp.add(pl2, JSplitPane.RIGHT);
        sp.setResizeWeight(0.5);

        // the right vertical scrollbar controls the vertical scrolling
        jspr.getViewport().addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                Point pr = jspr.getViewport().getViewPosition();
                // keep the x position of the left pane
                int x = jspl.getViewport().getViewPosition().x;
                Point newpl = new Point(x, pr.y);
                jspl.getViewport().setViewPosition(newpl);

                Rectangle r = jspr.getViewport().getBounds();
                int h = r.height;
                int th = jspr.getViewport().getViewSize().height;
                DiffView.this.diffLines.setSizes(th, h, pr.y);
            }
        });
    }

    static String repeatChar(char s, int n) {
        return new String(new char[n]).replace("\0", new String(new char[]{s}));
    }

    void append(StyledDocument doc, String s, Style style) {
        try {
          // doc.getLength() should be a good location
          // if it is abd things are really bad
          doc.insertString(doc.getLength(), s, style);
        } catch (BadLocationException e) {
            throw new Error(e);
        }
    }

    static int countNewLines(String str) {
        int count = 0;
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == '\n') {
                count++;
            }
        }
        return count;
    }

    int addOneLine(StyledDocument doc, ArrayList<String> lineNumberStrings, ArrayList<DisplayedToken> l) {
        String line;
        if (!l.isEmpty()) {
            DisplayedToken m = l.get(0);
            //System.out.println("Line header: " + m.token);
            Token t = m.token;
            if (t.getColno() > 0) {
                append(doc, repeatChar(' ', t.col), greenStyle);
                line = "";
            } else {
                line = "" + m.token.getLineno();
            }
        } else {
            line = "";
        }
        lineNumberStrings.add(line);

        int totalLines = 1; // at the end there implicte a new line
        for (DisplayedToken m : l) {
            //System.out.println(m.token);
            if (m.highlight) {
                append(doc, m.token.value, hotStyle);
            } else {
                append(doc, m.token.value, defStyle);
            }
            int c = countNewLines(m.token.value);
            totalLines += c;
        }
        append(doc, "\n", defStyle);
        return totalLines;
    }
    
    void clearDocument(StyledDocument doc) {
        try {
            doc.remove(0, doc.getLength());
        } catch (BadLocationException ex) {
            throw new Error(ex);
        }
    }

    public void setData(List<ArrayList<DisplayedToken>> a1,
            List<ArrayList<DisplayedToken>> a2,
            String leftHeaderText,
            String rightHeaderText
    )  {
        // the lines numbers to left and rigthof the documents
        ArrayList<String> linenoLeft = new ArrayList<>();
        ArrayList<String> linenoRight = new ArrayList<>();

        // the line numbers with changes
        ArrayList<Integer> linesWithDiffs = new ArrayList<>();

        clearDocument(leftDocument);
        clearDocument(rightDocument);

        int n1 = a1.size();
        for (int i = 0; i < n1; i++) {
            int p = linenoLeft.size();
            ArrayList<DisplayedToken> l = a1.get(i);
            boolean withchange = false;

            for (DisplayedToken t : l) {
                if (t.highlight) {
                    withchange = true;
                    break;
                }
            }
            int ll = addOneLine(leftDocument, linenoLeft, l);
            ArrayList<DisplayedToken> r = a2.get(i);
            if (!withchange) {
                for (DisplayedToken t : r) {
                    if (t.highlight) {
                        withchange = true;
                        break;
                    }

                }
            }
            int lr = addOneLine(rightDocument, linenoRight, r);
            int ml = Math.max(ll, lr);
            if (withchange) {
                linesWithDiffs.add(p);
            }
            for (int j = 1; j < ml; j++) {
                linenoLeft.add("");
                linenoRight.add("");
                if (withchange) {
                    linesWithDiffs.add(p + j);
                }
            }
            append(leftDocument, repeatChar('\n', ml - ll), defStyle);
            append(rightDocument, repeatChar('\n', ml - lr), defStyle);

        }
        rownumbersLeft.setLineNumbers(linenoLeft);
        rownumbersRight.setLineNumbers(linenoRight);
       
        headerLeft.setText(leftHeaderText);
        headerRight.setText(rightHeaderText);
        sp.setDividerLocation(0.5);

        edpl.setCaretPosition(0);
        edpr.setCaretPosition(0);
        //jspl.   getViewport().  setViewPosition(new Point(0,0));
        //jspr.getViewport().setViewPosition(new Point(0,0));
        jspr.revalidate();
        jspl.revalidate();

        this.diffLines.setData(linenoLeft.size(), linesWithDiffs);
    }

}
