package pl.allegro.tech.opel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class DanglingNodesFinder {
    Result find(Map<OpelNode, TreeNode> opelNodeTreeNodeMap) {
        // Build in-degree map: count how many times each node is depended on
        Map<OpelNode, Integer> inDegree = new HashMap<>();
        for (OpelNode node : opelNodeTreeNodeMap.keySet()) {
            inDegree.put(node, 0);
        }
        for (TreeNode treeNode : opelNodeTreeNodeMap.values()) {
            for (TreeNode dep : treeNode.dependencies) {
                inDegree.computeIfPresent(dep.node, (_, v) -> v + 1);
            }
        }
        // Nodes with in-degree 0 are not depended on by any other node
        List<TreeNode> dangling = new ArrayList<>();
        for (Map.Entry<OpelNode, Integer> entry : inDegree.entrySet()) {
            if (entry.getValue() == 0) {
                OpelNode node = entry.getKey();
                if (!(node instanceof ProgramNode) &&
                    !(node instanceof GraphBuildingOpelNodeVisitor.ExternalIdentifierExpressionNode)) {
                    dangling.add(opelNodeTreeNodeMap.get(node));
                }
            }
        }
        return new Result(dangling);
    }

    record Result(Collection<TreeNode> danglingNodes) {
    }
}
