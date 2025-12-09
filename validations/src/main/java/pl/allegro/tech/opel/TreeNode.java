package pl.allegro.tech.opel;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

class TreeNode {
    final OpelNode node;
    final List<TreeNode> dependencies = new ArrayList<>();

    TreeNode(OpelNode node, List<TreeNode> dependencies) {
        this.node = node;
        this.dependencies.addAll(dependencies);
    }

    @Override
    public String toString() {
        return "TreeNode{" +
                "node=" + node +
                ", dependencies=" + dependencies.stream().map(it -> it.node).collect(Collectors.toSet()) +
                '}';
    }
}
