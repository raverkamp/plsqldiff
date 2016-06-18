package spinat.plsqldiff.compare.gui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.JComponent;
import javax.swing.JPanel;

public class RowNumbers  {

    public final JComponent compo;

    String[] lineNumbers;
    final int linedist;
    int width = 40;

    public RowNumbers(int linedistance) {
        this.linedist = linedistance;
        lineNumbers = new String[]{};
        compo = new JPanel() {
            @Override
            public void paintComponent(Graphics gr1) {
                Graphics2D gr = (Graphics2D) gr1;
                Rectangle bounds = gr.getClipBounds();
                gr.clearRect(bounds.x, bounds.y, bounds.width, bounds.height);
                for (int i = 0; i < lineNumbers.length; i++) {
                    gr.drawString(lineNumbers[i], 2, (i + 1) * linedist);
                }
            }
        };
        compo.setMinimumSize(new Dimension(40, 100));
        compo.setPreferredSize(new Dimension(40, 100));
    }

    void fixIt() {
        int h = linedist * (lineNumbers.length + 1);
        Dimension d = new Dimension(width, h);
        compo.setMinimumSize(d);
        compo.setPreferredSize(d);
    }

    public void setLineNumbers(String[] lns) {
        lineNumbers = Arrays.copyOf(lns, lns.length);
        fixIt();
    }

    public void setLineNumbers(ArrayList<String> lns) {
        lineNumbers = lns.toArray(new String[0]);
        fixIt();
    }
}
