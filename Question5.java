import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.*;

public class Question5 extends JFrame {
    private final Graph graph = new Graph();
    private final DrawingPanel drawingPanel = new DrawingPanel(graph);
    private final JComboBox<Node> srcNodeBox = new JComboBox<>();
    private final JComboBox<Node> destNodeBox = new JComboBox<>();
    private final JTextField costField = new JTextField();
    private final JTextField bandwidthField = new JTextField();

    public Question5() {
        setTitle("Network Topology Optimizer");
        setSize(1000, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new GridLayout(6, 2, 5, 5));

        JTextField nodeNameField = new JTextField();
        JButton addNodeBtn = new JButton("Add Node");
        JButton addEdgeBtn = new JButton("Add Edge");
        JButton shortestPathBtn = new JButton("Find Shortest Path");
        JButton optimizeBtn = new JButton("Optimize Network");

        leftPanel.add(new JLabel("Node Name:"));
        leftPanel.add(nodeNameField);
        leftPanel.add(addNodeBtn);
        leftPanel.add(new JLabel("Source Node:"));
        leftPanel.add(srcNodeBox);
        leftPanel.add(new JLabel("Destination Node:"));
        leftPanel.add(destNodeBox);
        leftPanel.add(new JLabel("Cost:"));
        leftPanel.add(costField);
        leftPanel.add(new JLabel("Bandwidth:"));
        leftPanel.add(bandwidthField);
        leftPanel.add(addEdgeBtn);
        leftPanel.add(shortestPathBtn);
        leftPanel.add(optimizeBtn);

        add(leftPanel, BorderLayout.WEST);

        JPanel centerPanel = new JPanel(new GridLayout(1, 1));
        centerPanel.add(drawingPanel);
        add(centerPanel, BorderLayout.CENTER);

        addNodeBtn.addActionListener(e -> {
            String nodeName = nodeNameField.getText().trim();
            if (!nodeName.isEmpty()) {
                int x = new Random().nextInt(500) + 50;
                int y = new Random().nextInt(300) + 50;
                Node newNode = new Node(nodeName, x, y);
                graph.addNode(newNode);
                srcNodeBox.addItem(newNode);
                destNodeBox.addItem(newNode);
                drawingPanel.repaint();
            }
        });

        addEdgeBtn.addActionListener(e -> {
            Node src = (Node) srcNodeBox.getSelectedItem();
            Node dest = (Node) destNodeBox.getSelectedItem();
            if (src != null && dest != null && !src.equals(dest)) {
                try {
                    int cost = Integer.parseInt(costField.getText());
                    int bandwidth = Integer.parseInt(bandwidthField.getText());
                    graph.addEdge(src, dest, cost, bandwidth);
                    drawingPanel.repaint();
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Invalid cost or bandwidth value");
                }
            }
        });

        shortestPathBtn.addActionListener(e -> {
            Node src = (Node) srcNodeBox.getSelectedItem();
            Node dest = (Node) destNodeBox.getSelectedItem();
            if (src != null && dest != null) {
                List<Node> path = graph.shortestPath(src, dest);
                JOptionPane.showMessageDialog(this, "Shortest Path: " + path);
            }
        });

        optimizeBtn.addActionListener(e -> {
            Set<Edge> mst = graph.minimumSpanningTree();
            drawingPanel.setGraph(new Graph(mst)); // Updated Graph constructor to accept Set<Edge>
            drawingPanel.repaint();
        });
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Question5().setVisible(true));
    }
}
class DrawingPanel extends JPanel {
    private Graph graph;
    public DrawingPanel(Graph graph) {
        this.graph = graph;
        setPreferredSize(new Dimension(800, 400));
        setBackground(Color.WHITE);
    }

    public void setGraph(Graph graph) {
        this.graph = graph;
    }
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Draw edges
        g.setColor(Color.BLUE);
        for (Edge edge : graph.getEdges()) {
            g.drawLine(edge.source.x, edge.source.y, edge.destination.x, edge.destination.y);
            int midX = (edge.source.x + edge.destination.x) / 2;
            int midY = (edge.source.y + edge.destination.y) / 2;
            g.drawString("Cost: " + edge.cost + ", Bandwidth: " + edge.bandwidth, midX, midY);
        }

        g.setColor(Color.RED);
        for (Node node : graph.getNodes()) {
            g.fillOval(node.x - 10, node.y - 10, 20, 20);
            g.setColor(Color.BLACK);
            g.drawString(node.name, node.x - 10, node.y - 15);
            g.setColor(Color.RED);
        }
    }
}
class Node {
    String name;
    int x, y;
    public Node(String name, int x, int y) {
        this.name = name;
        this.x = x;
        this.y = y;
    }
    @Override
    public String toString() {
        return name;
    }
}
class Edge {
    Node source, destination;
    int cost, bandwidth;
    public Edge(Node source, Node destination, int cost, int bandwidth) {
        this.source = source;
        this.destination = destination;
        this.cost = cost;
        this.bandwidth = bandwidth;
    }
}

class Graph {
    private final Set<Node> nodes = new HashSet<>();
    private final Set<Edge> edges = new HashSet<>();

    public void addNode(Node node) {
        nodes.add(node);
    }

    public void addEdge(Node source, Node destination, int cost, int bandwidth) {
        edges.add(new Edge(source, destination, cost, bandwidth));
    }

    public Set<Edge> getEdges() {
        return edges;
    }

    public Set<Node> getNodes() {
        return nodes;
    }
    public List<Node> shortestPath(Node source, Node destination) {
        return new ArrayList<>();
    }
    public Set<Edge> minimumSpanningTree() {
        return new HashSet<>();
    }
    public Graph(Set<Edge> mstEdges) {
        this.edges.addAll(mstEdges);
    }

    public Graph() {}
}
