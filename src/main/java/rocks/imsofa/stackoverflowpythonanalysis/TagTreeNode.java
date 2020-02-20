/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rocks.imsofa.stackoverflowpythonanalysis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author lendle
 */
public class TagTreeNode {

    private String tagName = null;
    private int tagIndex = -1;
    private TagTreeNode parentNode = null;
    private List<TagTreeNode> childNodes = new ArrayList<>();

    public TagTreeNode(String tagName, int tagIndex) {
        this.tagName = tagName;
        this.tagIndex = tagIndex;
    }

    public void addChildNode(TagTreeNode childNode) {
        childNode.parentNode = this;
        this.childNodes.add(childNode);
    }

    public String getTagName() {
        return tagName;
    }

    public int getTagIndex() {
        return tagIndex;
    }

    public TagTreeNode getParentNode() {
        return parentNode;
    }

    public static int[] createPath(TagTreeNode parent, int childTagIndex) {
        int[] parentPath = parent.getPath();
        int[] path = new int[parentPath.length + 1];
        System.arraycopy(parentPath, 0, path, 0, parentPath.length);
        path[path.length - 1] = childTagIndex;
        return path;
    }

    public int[] getPath() {
        if (parentNode == null) {
            return new int[]{this.tagIndex};
        } else {
            return createPath(parentNode, this.tagIndex);
        }
    }

    public List<TagTreeNode> getChildNodes() {
        return new ArrayList<>(this.childNodes);
    }

    @Override
    public int hashCode() {
        int[] myPath = this.getPath();
        Arrays.sort(myPath);
        String myPathString = Arrays.toString(myPath);
        return myPathString.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TagTreeNode other = (TagTreeNode) obj;
        int[] myPath = this.getPath();
        int[] otherPath = other.getPath();
        Arrays.sort(myPath);
        Arrays.sort(otherPath);
        String myPathString = Arrays.toString(myPath);
        String otherPathString = Arrays.toString(otherPath);
        if (!Objects.equals(myPathString, otherPathString)) {
            return false;
        }
        return true;
    }

    public String toString() {
        int[] path = this.getPath();
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < path.length - 1; i++) {
            buffer.append("\t");
        }
        buffer.append(this.getTagName() + ":" + this.getTagIndex() + ":" + Arrays.toString(path));
        for (TagTreeNode child : this.childNodes) {
            buffer.append("\n");
            buffer.append(child.toString());
        }
        return buffer.toString();
    }
}
