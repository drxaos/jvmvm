/*
 * 02/10/2009
 *
 * LineHighlightManager - Manages line highlights.
 * 
 * This library is distributed under a modified BSD license.  See the included
 * RSyntaxTextArea.License.txt file for details.
 */
package org.fife.ui.rtextarea;

import javax.swing.text.BadLocationException;
import javax.swing.text.Position;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Manages line highlights in an <code>RTextArea</code>.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class CustomLineHighlightManager extends LineHighlightManager {

    private RTextArea textArea;
    private List<LineHighlightInfo> lineHighlights;


    /**
     * Constructor.
     *
     * @param textArea The parent text area.
     */
    public CustomLineHighlightManager(RTextArea textArea) {
        super(textArea);
        this.textArea = textArea;
    }


    /**
     * Highlights the specified line.
     *
     * @param line  The line to highlight.
     * @param color The color to highlight with.
     * @return A tag for the highlight.
     * @throws BadLocationException If <code>line</code> is not a valid line
     *                              number.
     * @see #removeLineHighlight(Object)
     */
    public Object addLineHighlight(int line, Color color)
            throws BadLocationException {
        int offs = textArea.getLineStartOffset(line);
        LineHighlightInfo lhi = new LineHighlightInfo(
                textArea.getDocument().createPosition(offs), color);
        if (lineHighlights == null) {
            lineHighlights = new ArrayList<LineHighlightInfo>(1);
        }
        int index = Collections.binarySearch(lineHighlights, lhi);
        if (index < 0) { // Common case
            index = -(index + 1);
        }
        lineHighlights.add(index, lhi);
        repaintLine(lhi);
        return lhi;
    }


    /**
     * Paints any highlighted lines in the specified line range.
     *
     * @param g The graphics context.
     */
    public void paintLineHighlights(Graphics g) {

        int count = lineHighlights == null ? 0 : lineHighlights.size();
        if (count > 0) {

            int docLen = textArea.getDocument().getLength();
            Rectangle vr = textArea.getVisibleRect();
            int lineHeight = textArea.getLineHeight();

            try {

                for (int i = 0; i < count; i++) {
                    LineHighlightInfo lhi = lineHighlights.get(i);
                    int offs = lhi.getOffset();
                    if (offs >= 0 && offs <= docLen) {
                        int y = textArea.yForLineContaining(offs);
                        if (y > vr.y - lineHeight) {
                            if (y < vr.y + vr.height) {
                                g.setColor(lhi.getColor());
                                g.fillRect(0, y, textArea.getWidth(), lineHeight);
//                                g.fillRect(20, y, 50, lineHeight);
                            } else {
                                break; // Out of visible rect
                            }
                        }
                    }
                }

            } catch (BadLocationException ble) { // Never happens
                ble.printStackTrace();
            }
        }

    }


    /**
     * Removes all line highlights.
     *
     * @see #removeLineHighlight(Object)
     */
    public void removeAllLineHighlights() {
        if (lineHighlights != null) {
            lineHighlights.clear();
            textArea.repaint();
        }
    }


    /**
     * Removes a line highlight.
     *
     * @param tag The tag of the line highlight to remove.
     * @see #addLineHighlight(int, Color)
     */
    public void removeLineHighlight(Object tag) {
        if (tag instanceof LineHighlightInfo) {
            lineHighlights.remove(tag);
            repaintLine((LineHighlightInfo) tag);
        }
    }


    /**
     * Repaints the line pointed to by the specified highlight information.
     *
     * @param lhi The highlight information.
     */
    private void repaintLine(LineHighlightInfo lhi) {
        int offs = lhi.getOffset();
        // May be > length if they deleted text including the highlight
        if (offs >= 0 && offs <= textArea.getDocument().getLength()) {
            try {
                int y = textArea.yForLineContaining(offs);
                if (y > -1) {
                    textArea.repaint(0, y,
                            textArea.getWidth(), textArea.getLineHeight());
                }
            } catch (BadLocationException ble) {
                ble.printStackTrace(); // Never happens
            }
        }
    }


    /**
     * Information about a line highlight.
     */
    public static class LineHighlightInfo implements Comparable<LineHighlightInfo> {

        private Position offs;
        private Color color;

        private int spaceStart;
        private int spaceEnd;

        public LineHighlightInfo(Position offs, Color c) {
            this.offs = offs;
            this.color = c;
        }

        public int compareTo(LineHighlightInfo o) {
            if (o != null) {
                return offs.getOffset() - o.getOffset();
            }
            return -1;
        }

        public void setColor(Color color) {
            this.color = color;
        }

        public int getSpaceStart() {
            return spaceStart;
        }

        public void setSpaceStart(int spaceStart) {
            this.spaceStart = spaceStart;
        }

        public int getSpaceEnd() {
            return spaceEnd;
        }

        public void setSpaceEnd(int spaceEnd) {
            this.spaceEnd = spaceEnd;
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (o instanceof LineHighlightInfo) {
                return offs.getOffset() == ((LineHighlightInfo) o).getOffset();
            }
            return false;
        }

        public Color getColor() {
            return color;
        }

        public int getOffset() {
            return offs.getOffset();
        }

        @Override
        public int hashCode() {
            return getOffset();
        }

    }


}