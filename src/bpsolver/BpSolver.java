package bpsolver;

public class BpSolver {
    public static void main(String[] args) {
        // TODO code application logic here
        
        Database database;
        
        database = new Database();
        
        Database.Node boxNode = new Database.Node(Database.Node.EnumType.BOX);
        Database.Node lineNode = new Database.Node(Database.Node.EnumType.LINE);
        
        boxNode.outgoingEdges.add(new Database.Edge(lineNode));
        
        database.nodes.add(boxNode);
        database.nodes.add(lineNode);
    }
    
}
