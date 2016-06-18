package spinat.plsqldiff.compare.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;
import javax.swing.JComponent;

public class DiffLines extends JComponent {

    private int totalLines;
    private ArrayList<Integer> linesWithChanges;

    private static class Range {

        final int from;
        final int to;

        public Range(int from, int to) {
            this.from = from;
            this.to = to;
        }
    }

    private ArrayList<Range> ranges;

    public DiffLines(int linedist) {
        this.totalLines = 0;
        this.setMinimumSize(new Dimension(40, 100));
        this.setPreferredSize(new Dimension(40, 100));
        this.setBackground(Color.red);
        linesWithChanges = null;
    }

    private int totalHeight;
    private int viewHeight;
    private int yposition;

    public void setSizes(int totalHeight, int viewHeight, int yposition) {
        this.totalHeight = totalHeight;
        this.viewHeight = viewHeight;
        this.yposition = yposition;
        this.repaint();
    }

    public void setData(int totalLines, ArrayList<Integer> linesWithChanges) {
        this.totalLines = totalLines;
        this.linesWithChanges = new ArrayList<>();
        this.linesWithChanges.addAll(linesWithChanges);
        this.ranges = new ArrayList<>();
        int p = 0;
        while (p < linesWithChanges.size()) {
            int start = p;
            int rangestart = linesWithChanges.get(p);
            while (p + 1 < linesWithChanges.size() && linesWithChanges.get(p + 1) == rangestart + (p + 1 - start)) {
                p++;
            }
            Range r = new Range(rangestart, linesWithChanges.get(p));
            ranges.add(r);
            p++;
        }
        this.repaint();
    }

    public void clearData() {
        this.ranges = null;
    }

    // highlight the visible section, i.e. paint it in another color
    @Override
    public void paintComponent(Graphics gr1) {
        if (this.ranges == null || totalLines <= 0) {
            return;
        }
        int w = this.getWidth();
        int h = this.getHeight();
        if (h <= 1) {
            return;
        }
        Graphics2D gr = (Graphics2D) gr1;
        Rectangle bounds = gr.getClipBounds();
        gr.clearRect(bounds.x, bounds.y, bounds.width, bounds.height);

        {
            gr.setColor(Color.PINK);
            for (Range range : this.ranges) {
                double ypos = (double) h * ((double) range.from / (double) totalLines);
                double height = Math.max(1.0, (double) h * ((double) (range.to - range.from + 1) / (double) totalLines));
                gr.fillRect(0, (int) ypos, w, (int) height);
            }
        }
        
        double hightlight_ypos = h * ((double) this.yposition / (double) this.totalHeight);
        double highlight_height = h * ((double) this.viewHeight) / ((double) this.totalHeight);
        gr.setClip(0,(int)hightlight_ypos,w,(int)highlight_height);
        gr.setColor(Color.red);
        for (Range range : this.ranges) {
            double ypos = (double) h * ((double) range.from / (double) totalLines);
            double height = Math.max(1.0, (double) h * ((double) (range.to - range.from + 1) / (double) totalLines));
            gr.fillRect(0, (int) ypos, w, (int) height);
        }

        Color c = new Color(200, 200, 200, 100);
        gr.setColor(c);
        double y = h * ((double) this.yposition / (double) this.totalHeight);
        double hh = h * ((double) this.viewHeight) / ((double) this.totalHeight);
    
        gr.setColor(Color.BLACK);
        gr.setStroke(new BasicStroke(3));
        gr.drawRect(0, (int) y, w, (int) (hh));

    }
}
