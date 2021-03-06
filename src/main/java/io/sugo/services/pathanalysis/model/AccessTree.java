package io.sugo.services.pathanalysis.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import io.sugo.services.pathanalysis.PathAnalysisConstant;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 */
public class AccessTree implements Serializable {

    private static final long serialVersionUID = 1004553939220527137L;

    private static final java.text.DecimalFormat df = new java.text.DecimalFormat("#.0000");

    private AccessTree parent;

    private TreeNode leaf;

    private int weight;

    private float rate = 0.0f;

    private List<AccessTree> children = Lists.newLinkedList();

    public AccessTree(TreeNode leaf, AccessTree parent, int weight) {
        this.leaf = leaf;
        this.parent = parent;
        this.weight = weight;
    }

    public void addChild(AccessTree child) {
        children.add(child);
    }

    public void increaseWeight() {
        weight++;
    }

    private void prune() {
        // First, descent sort by weight.
        children.sort((tom, jack) ->
                tom.getWeight() > jack.getWeight() ? -1 :
                        tom.getWeight() < jack.getWeight() ? 1 : 0);

        // Second, merge trees into a tree which beyond the number of 'MAX_CHILDREN'.
        /*if (children.size() > PathAnalysisConstant.MAX_CHILDREN) {
            List<AccessTree> tailChildren = children.subList(PathAnalysisConstant.MAX_CHILDREN,
                    children.size());
            AccessTree mergedTree = merge(tailChildren);
            tailChildren.clear();
            children.add(mergedTree);
        }*/

        // Third, add tree which indicate user loss if necessary.
        if (children.size() > 0) {
            int totalWeight = 0;
            Set<String> lostUsers = new HashSet(leaf.getUserIds());
            for (AccessTree child : children) {
                totalWeight += child.getWeight();
                lostUsers.removeAll(child.getLeaf().getUserIds());
            }
            if (weight > totalWeight) {
                int lostWeight = weight - totalWeight;
                AccessTree lossTree = new AccessTree(new TreeNode(PathAnalysisConstant.LEAF_NAME_LOSS,
                        children.get(0).getLeaf().getLayer(), PathAnalysisConstant.NODE_TYPE_LOSS),
                        this, lostWeight);
                lossTree.getLeaf().setUserIds(lostUsers);
                children.add(lossTree);
            }

            // Last, calculate the rate of each child.
            for (AccessTree child : children) {
                float rate = Float.valueOf(df.format(child.getWeight() * 1.0f / weight));
                child.setRate(rate);
            }
        }
    }

    private AccessTree merge(List<AccessTree> trees) {
        int totalWeight = 0;
        Set<String> userIds = Sets.newHashSet();
        for (AccessTree tree : trees) {
            totalWeight += tree.getWeight();
            userIds.addAll(tree.getLeaf().getUserIds());
        }

        return new AccessTree(new TreeNode(PathAnalysisConstant.LEAF_NAME_OTHER,
                trees.get(0).getLeaf().getLayer()).setUserIds(userIds), this, totalWeight);
    }

    @JsonProperty
    public List<AccessTree> getChildren() {
        prune();
        return children;
    }

    @JsonProperty("stayTime")
    public double getAverageStayTime() {
        return leaf.getTotalStayTime() * 1.0 / weight;
    }

    @JsonProperty
    public float getRate() {
        return rate;
    }

    @JsonProperty
    public int getWeight() {
        return weight;
    }

    @JsonUnwrapped
    public TreeNode getLeaf() {
        return leaf;
    }

    public AccessTree getParent() {
        return parent;
    }

    public void setRate(float rate) {
        this.rate = rate;
    }
}
