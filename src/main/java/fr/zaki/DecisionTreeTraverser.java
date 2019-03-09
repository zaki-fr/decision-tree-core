package fr.zaki;

import fr.zaki.results.PathResult;
import fr.zaki.schema.Labels;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.logging.Log;
import org.neo4j.procedure.*;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Stream;

public class DecisionTreeTraverser {

    // This field declares that we need a GraphDatabaseService
    // as context when any procedure in this class is invoked
    @Context
    public GraphDatabaseService db;
    // This gives us a log instance that outputs messages to the
    // standard log, normally found under `data/log/console.log`
    @Context
    public Log log;

    private static final DecisionTreeEvaluator decisionTreeEvaluator = new DecisionTreeEvaluator();

    @Procedure(name = "fr.zaki.traverse.DecisionTreeExpression", mode = Mode.READ)
    @Description("CALL fr.zaki.traverse.DecisionTreeExpression(tree, facts) - traverse decision tree")
    public Stream<PathResult> traverseDecisionTree(@Name("tree") String id, @Name("facts") Map<String, String> facts) throws IOException {
        // Which Decision Tree are we interested in?
        Node tree = db.findNode(Labels.Tree, "id", id);
        if ( tree != null) {
            // Find the paths by traversing this graph and the facts given
            return decisionPath(tree, facts);
        }
        return null;
    }

    private Stream<PathResult> decisionPath(Node tree, Map<String, String> facts) {
        DecisionTreeExpanderExpression decisionTreeExpander = new DecisionTreeExpanderExpression();
        decisionTreeExpander.setParameters(facts, log);
        decisionTreeEvaluator.setParameters(facts, log);
        TraversalDescription myTraversal = db.traversalDescription()
                .depthFirst()
                .expand(decisionTreeExpander)
                .evaluator(decisionTreeEvaluator);

        return myTraversal.traverse(tree).stream().map(PathResult::new);
    }

    @Procedure(name = "fr.zaki.traverse.DecisionTreeScript", mode = Mode.READ)
    @Description("CALL fr.zaki.traverse.DecisionTreeScript(tree, facts) - traverse decision tree")
    public Stream<PathResult> traverseDecisionTreeTwo(@Name("tree") String id, @Name("facts") Map<String, String> facts) throws IOException {
        // Which Decision Tree are we interested in?
        Node tree = db.findNode(Labels.Tree, "id", id);
        if ( tree != null) {
            // Find the paths by traversing this graph and the facts given
            return decisionPathTwo(tree, facts);
        }
        return null;
    }

    private Stream<PathResult> decisionPathTwo(Node tree, Map<String, String> facts) {
        DecisionTreeExpanderScript decisionTreeExpanderScript = new DecisionTreeExpanderScript();
        decisionTreeExpanderScript.setParameters(facts, log);
        decisionTreeEvaluator.setParameters(facts, log);
        TraversalDescription myTraversal = db.traversalDescription()
                .depthFirst()
                .expand(decisionTreeExpanderScript)
                .evaluator(decisionTreeEvaluator);

        return myTraversal.traverse(tree).stream().map(PathResult::new);
    }
}
