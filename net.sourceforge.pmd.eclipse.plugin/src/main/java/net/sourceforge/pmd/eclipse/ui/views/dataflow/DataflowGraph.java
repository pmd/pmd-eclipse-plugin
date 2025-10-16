/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.views.dataflow;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import net.sourceforge.pmd.lang.ast.Node;

/**
 * Builds a Dataflow Graph
 *
 * @author SebastianRaffel ( 08.06.2005 )
 */
public class DataflowGraph extends Composite {

    private List<NodeCanvas> nodes;
    private List<PathCanvas> paths;

    protected int nodeRadius = 12;
    protected int lineLength = 25;
    protected int rowHeight = 2 * nodeRadius + lineLength;

    protected Color nodeColor;
    protected Color textColor;
    protected boolean marked;
    protected Color markColor;
    protected Color markColor2;
    protected Color markColor3;

    /**
     * Inner class for creating Nodes, each Node is a Label and has its own
     * PaintListener
     *
     * @author SebastianRaffel ( 08.06.2005 )
     */
    private class NodeCanvas extends Canvas implements PaintListener {

        //private DataFlowNode node;
        private int radius;
        private Color nodeColor;
        private Color textColor;

        protected boolean marked;
        protected Color markColor;

        /**
         * Constructor
         *
         * @param parent,
         *            the parent Composite
         * @param inode,
         *            the DataFlowNode
         * @param coordinates,
         *            where to put the Label
         * @param nodeRadius,
         *            radius of the Node
         */
        NodeCanvas(Composite parent, /*DataFlowNode inode,*/ Point coordinates, int nodeRadius) {
            super(parent, SWT.NONE);

            //node = inode;
            radius = nodeRadius;

            Display display = parent.getDisplay();
            // Default Colors
            Color bgColor = display.getSystemColor(SWT.COLOR_WHITE); // new Color(null,255,255,255);
            nodeColor = display.getSystemColor(SWT.COLOR_GRAY); // new Color(null,128,128,128);
            textColor = display.getSystemColor(SWT.COLOR_WHITE); // new Color(null,255,255,255);
            markColor = display.getSystemColor(SWT.COLOR_RED); // new Color(null,192,0,0);

            // set location and size of the Label
            setLocation(coordinates);
            setSize(2 * radius + 1, 2 * radius + 1); // +1 to avoid cropping on right & bottom
            setBackground(bgColor);

            // we have our own Paint Listener
            addPaintListener(this);
        }

        // /**
        // * Set the Color for the Background, Node and Text in the Node
        // *
        // * @param backGround
        // * @param node
        // * @param text
        // */
        // public void setColors(Color backGround, Color node, Color text) {
        // if (backGround != null) {
        // bgColor = backGround;
        // setBackground(bgColor);
        // }
        // nodeColor = node;
        // textColor = text;
        // }

        /**
         * Returns the Text-Line of the Node, different Nodes can have the same
         * Line.
         *
         * @return line
         */
        public int getLine() {
            return 1; // node.getLine();
        }

        /**
         * Gets the Dataflow-Index of the Node.
         *
         * @return index
         */
        public int getIndex() {
            return 0; //node.getIndex();
        }

        /**
         * Checks, if this Node contains a variable of the given name.
         *
         * @param varName
         * @return true, if the Name of the Variable is found, false otherwise
         */
        public boolean containsVariable(String varName) {
            //List<VariableAccess> vars = node.getVariableAccess();
            //if (vars == null) {
            //    return false;
            //}
            //
            //for (VariableAccess va : vars) {
            //    if (va.getVariableName().equalsIgnoreCase(varName)) {
            //        return true;
            //    }
            //}
            return false;
        }

        /**
         * @return the DataFlowNode
         */
        public /*DataFlowNode*/ Object getINode() {
            return null; //node;
        }

        /**
         * Checks, if this Node has been marked during painting a Path.
         *
         * @return true if the Node has been marked, false otherwise
         */
        public boolean isMarked() {
            return marked;
        }

        @Override
        public void paintControl(PaintEvent e) {
            // when we want to mark the Node,
            // we change the Drawing-Background
            e.gc.setBackground(marked ? markColor : nodeColor);

            // an area is filled with this BG-Color
            e.gc.fillArc(0, 0, 2 * radius, 2 * radius, 0, 360);
            // now outline it
            e.gc.drawArc(0, 0, 2 * radius, 2 * radius, 0, 360);

            // draw the Node's Index
            String indexString = "0"; //String.valueOf(node.getIndex());
            int xPos = radius - 2 - 4 * (indexString.length() / 2);
            int yPos = radius / 2;

            e.gc.setForeground(textColor);
            e.gc.drawString(indexString, xPos - 1, yPos - 1);
        }

        /**
         * Sets a Node as being marked and paints is in a different - marking -
         * Color.
         *
         * @param isMarked
         * @param color
         */
        public void mark(boolean isMarked, Color color) {
            marked = isMarked;
            markColor = color;
            redraw();
        }
    }

    /**
     * Inner class for creating Paths, we don't create our own labels here,
     * because SWT doesn't support transparency, so we can't overlay paths.
     *
     * @author SebastianRaffel ( 08.06.2005 )
     */
    private class PathCanvas implements PaintListener {

        private int index1;
        private int index2;
        private int radius;
        private Rectangle bounds;
        private Color lineColor;
        private int arrowWidth;
        private int arrowHeight;

        protected boolean marked;
        protected Color markColor;

        /**
         * Constructor, Indexes can be twisted (first index > second Index) as
         * well as y-Positions
         *
         * @param parent,
         *            the parent composite
         * @param nodeIndex1,
         *            the first Index
         * @param nodeIndex2,
         *            the second Index
         * @param x,
         *            the x-Position
         * @param y1,
         *            the upper y-Position
         * @param y2,
         *            the lower y Position
         * @param nodeRadius,
         *            the radius of the Node
         */
        PathCanvas(Composite parent, int nodeIndex1, int nodeIndex2, int x, int y1, int y2, int nodeRadius) {

            index1 = nodeIndex1;
            index2 = nodeIndex2;
            radius = nodeRadius;

            arrowWidth = 4;
            arrowHeight = 7;

            Display disp = parent.getDisplay();

            lineColor = disp.getSystemColor(SWT.COLOR_BLACK);
            markColor = new Color(null, 192, 0, 0);

            // set the bounds to put the Path into
            bounds = calculateBounds(x, y1, y2);
            parent.addPaintListener(this);
        }

        /**
         * @return the first Index
         */
        public int getIndex1() {
            return index1;
        }

        /**
         * @return the second Index
         */
        public int getIndex2() {
            return index2;
        }

        // /**
        // * Sets the Paths Color
        // *
        // * @param color
        // */
        // public void setColor(Color color) {
        // lineColor = color;
        // }

        /**
         * calculate the bounds, where to put the Path, this bounds are absolute
         * to the parent Element (e.g. we don't put it on 50,75 and draw from
         * 0,0, but we draw from 50,75)
         *
         * @param x
         * @param y1
         * @param y2
         * @return a Rectangle of coordinates
         */
        private Rectangle calculateBounds(int x, int y1, int y2) {
            int newX1 = 0;
            int newX2 = 0;
            int newY1 = 0;
            int newY2 = 0;

            // calculate by comparing the Nodes Indexes

            if (index1 < index2) {
                if (index2 - index1 == 1) {
                    // 1 -> 2
                    newX1 = x + radius - arrowWidth;
                    newX2 = x + radius + arrowWidth;
                    newY1 = y1 + 2 * radius;
                    newY2 = y2;
                } else if (index2 - index1 > 1) {
                    // 1 --\
                    // |
                    // 2 <-/
                    newY1 = y1 + radius - 1;
                    newY2 = y2 + radius;
                    int n = (index2 - index1) * 3 + 10;
                    n += Math.random() * 5;
                    newX1 = x - n;
                    newX2 = x;
                }
            } else {
                if (index1 - index2 == 1) {
                    // 1 <- 2
                    newX1 = x + radius - arrowWidth;
                    newX2 = x + radius + arrowWidth;
                    newY1 = y2 + 2 * radius;
                    newY2 = y1;
                } else if (index1 - index2 > 1) {
                    // 1 <-\
                    // |
                    // 2 --/
                    newY1 = y2 + radius - 1;
                    newY2 = y1 + radius;
                    int n = (index1 - index2) * 3 + 10;
                    n += Math.random() * 5;
                    newX1 = x + 2 * radius;
                    newX2 = x + 2 * radius + n;
                }
            }

            return new Rectangle(newX1, newY1, newX2, newY2);
        }

        @Override
        public void paintControl(PaintEvent e) {
            // change Back- and Foreground
            // because the line in drawn with the FG-Color,
            // and the triangle is filled with the BG-Color
            if (marked) {
                e.gc.setForeground(markColor);
                e.gc.setBackground(markColor);
            } else {
                e.gc.setForeground(lineColor);
                e.gc.setBackground(lineColor);
            }

            int width = bounds.width - bounds.x;

            if (index1 < index2) {
                if (index2 - index1 == 1) {
                    // 1 -> 2
                    e.gc.drawLine(bounds.x + width / 2, bounds.y, bounds.x + width / 2, bounds.height);
                    e.gc.fillPolygon(new int[] { bounds.x, bounds.height - arrowHeight, bounds.width,
                        bounds.height - arrowHeight, bounds.x + width / 2, bounds.height });
                } else if (index2 - index1 > 1) {
                    // 1 --\
                    // |
                    // 2 <-/
                    e.gc.drawPolyline(new int[] { bounds.x + width, bounds.y, bounds.x, bounds.y + width, bounds.x,
                        bounds.height - width, bounds.width, bounds.height });
                    e.gc.fillPolygon(new int[] { bounds.width, bounds.height,
                        bounds.width - arrowHeight - arrowWidth / 2 + 1, bounds.height - arrowWidth,
                        bounds.width - arrowWidth, bounds.height - arrowHeight - arrowWidth / 2 });
                }
            } else {
                if (index1 - index2 == 1) {
                    // 1 <- 2
                    e.gc.drawLine(bounds.x + width / 2, bounds.y, bounds.x + width / 2, bounds.height);
                    e.gc.fillPolygon(new int[] { bounds.x + arrowWidth, bounds.y, bounds.x, bounds.y + arrowHeight,
                        bounds.x + 2 * arrowWidth, bounds.y + arrowHeight });
                } else if (index1 - index2 > 1) {
                    // 1 <-\
                    // |
                    // 2 --/
                    e.gc.drawPolyline(new int[] { bounds.x, bounds.y, bounds.width, bounds.y + width, bounds.x + width,
                        bounds.height - width, bounds.x, bounds.height });
                    e.gc.fillPolygon(new int[] { bounds.x, bounds.y, bounds.x + arrowWidth,
                        bounds.y + arrowHeight + arrowWidth / 2, bounds.x + arrowHeight + arrowWidth / 2,
                        bounds.y + arrowWidth });
                }
            }
        }

        /**
         * Sets a Path a marked and color it
         *
         * @param isMarked
         * @param color
         */
        public void mark(boolean isMarked, Color color) {
            marked = isMarked;
            markColor = color;
        }
    }

    public DataflowGraph(Composite parent, Node node, int radius, int length, int height) {
        super(parent, SWT.NONE);

        if (node == null) {
            return;
        }

        nodeRadius = radius;
        lineLength = length;
        rowHeight = height;

        nodes = new ArrayList<>();
        paths = new ArrayList<>();

        Display display = parent.getDisplay();
        // Default Colors
        Color bgColor = display.getSystemColor(SWT.COLOR_WHITE); // new Color(null,255,255,255);
        nodeColor = display.getSystemColor(SWT.COLOR_GRAY); // new Color(null,192,192,192);
        textColor = display.getSystemColor(SWT.COLOR_BLACK); // new Color(null,0,0,0);
        markColor = new Color(null, 192, 0, 0);
        markColor2 = new Color(null, 128, 0, 128);
        markColor3 = new Color(null, 0, 0, 96);

        setSize(parent.getSize());
        setBackground(bgColor);

        //createDataflowGraph(/*node*/);
    }

    @Override
    public void addMouseListener(final MouseListener listener) {
        if (nodes != null) {
            Iterator<NodeCanvas> nodeIterator = nodes.iterator();
            for (int i = 0; nodeIterator.hasNext(); i++) {
                final int row = i;
                NodeCanvas node = nodeIterator.next();
                node.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseDown(MouseEvent e) {
                        e.y += row * DataflowGraphViewer.ROW_HEIGHT;
                        listener.mouseDown(e);
                    }
                });
            }
        }
        super.addMouseListener(listener);
    }

    /**
     * Set the Graph Node-Radius, and Length of the (direct) Lines from one Node
     * to another
     *
     * @param radius
     * @param length
     */
    public void setGraphData(int radius, int length) {
        nodeRadius = radius;
        lineLength = length;

        redraw();
    }

    /**
     * Returns the graphical Node of the given Index
     *
     * @param index
     * @return the NodeCanvas
     */
    private NodeCanvas getNode(int index) {
        if (nodes == null) {
            return null;
        }

        for (NodeCanvas node : nodes) {
            if (node.getIndex() == index) {
                return node;
            }
        }

        return null;
    }

    /**
     * Checks, if a Path in the Graph has been marked
     *
     * @return true, if a path is coloured, false otherwise
     */
    public boolean isMarked() {
        return marked;
    }

    /**
     * Un-marks a path, sets all colours to normal.
     */
    public void demark() {
        for (NodeCanvas node : nodes) {
            node.mark(false, null);
        }

        for (PathCanvas path : paths) {
            path.mark(false, null);
        }
        redraw();

        marked = false;
    }

    /**
     * Marks one single node.
     * 
     * @param index
     *            index of the node
     */
    public void markNode(int index) {
        NodeCanvas node = getNode(index);
        node.mark(true, markColor3);
    }

    /**
     * Marks a Path from the given first line to the second line <br>
     * Given are two Lines in the Text and a Variable, where an anomaly has
     * occurred, the method colors the nodes, that lie at the lines and
     * calculates and also colors _one possible_ Path between them.
     *
     * @param line1
     * @param line2
     * @param varName
     */
    public void markPath(int line1, int line2, String varName) {
        if (nodes == null || paths == null) {
            return;
        }

        // twist the Lines if needed
        if (line1 > line2) {
            int temp = line1;
            line1 = line2;
            line2 = temp;
        }

        // an Anomaly can have multiple starting points
        // but - so we say here - only one ending point
        //List<DataFlowNode> startNodes = new ArrayList<>();
        //DataFlowNode endNode = null;

        for (NodeCanvas node : nodes) { // first we clear all nodes not needed

            if (!node.containsVariable(varName)) {
                node.mark(false, null);
                continue;
            }

            if (node.getLine() == line1) {
                // if a Node is set at the given
                // first Line we color it and add it
                // as starting Node
                node.mark(true, markColor);
                //startNodes.add(node.getINode());
            } else if (node.getLine() == line2) {
                // ... else, if we found a Node at the ending Line, we mark it
                // and set it as ending Node
                node.mark(true, markColor);
                //endNode = node.getINode();
            } else {
                node.mark(false, null);
            }
        }

        for (PathCanvas deMarkedPath : paths) { // we then clear all Paths
            deMarkedPath.mark(false, null);
        }

        // ... to mark some of them again
        List<PathCanvas> pathsToMark = new ArrayList<>();
        //for (DataFlowNode start : startNodes) {
        //
        //    // from every starting Node we search for a Path to the ending node
        //    List<PathCanvas> pathList = findPath(start, endNode, new ArrayList<DataFlowNode>());
        //    // we get a List of PathCanvas, that build up the searched Path
        //    for (PathCanvas currentPath : pathList) {
        //        // if some PathCanvas are already found and
        //        // set to mark, we don't want to mark them again
        //        if (!pathsToMark.contains(currentPath)) {
        //            pathsToMark.add(currentPath);
        //        }
        //    }
        //}

        // now we have a clear List of Paths that we can color
        for (int m = 0; m < pathsToMark.size(); m++) {
            PathCanvas markedPath = pathsToMark.get(m);
            markedPath.mark(true, markColor);

            // the PathList contains Paths from the beginning to end,
            // like (1,2),(2,3),...,(12,13); we search for the Nodes
            // that are "visited" and mark them in another color,
            // so we can see them as "stopovers" (like 2,3,...,12)
            if (m < pathsToMark.size() - 1) {
                NodeCanvas markedNode = getNode(markedPath.getIndex2());
                if (!markedNode.isMarked()) {
                    markedNode.mark(true, markColor2);
                }
            }
        }

        redraw();
        marked = true;
    }
}
