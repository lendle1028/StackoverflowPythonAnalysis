/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rocks.imsofa.stackoverflowpythonanalysis;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import javax.script.ScriptEngine;
import org.apache.commons.io.FileUtils;
import org.renjin.script.RenjinScriptEngineFactory;
import org.renjin.sexp.IntBufferVector;
import org.renjin.sexp.ListVector;
import org.renjin.sexp.StringArrayVector;

/**
 *
 * @author lendle
 */
public class NewMain {

    /**
     * if a tag occurs more than TAG_BRANCHING_OCCURRENCE_THRESHOLD times it can
     * branch; otherwise, the tag will be treated as a leaf node
     */
//    public static final int TAG_BRANCHING_OCCURRENCE_THRESHOLD = 100;
//    public static final double BRANCH_OCCURRENCE_THRESHOLD = 10;
//    public static final double BRANCH_RATIO_THRESHOLD = 0.1d;
    
    //91 centers, 0.606568199088119 v.s. kmeans=0.55289058616877~0.586197342259639
    public static final int TAG_BRANCHING_OCCURRENCE_THRESHOLD = 200;
    public static final double BRANCH_OCCURRENCE_THRESHOLD = 10;
    public static final double BRANCH_RATIO_THRESHOLD = 0.05d;
    
    //106 centers, 0.618722545450784 v.s. kmeans=0.600343634041933~0.626290108516266
//    public static final int TAG_BRANCHING_OCCURRENCE_THRESHOLD = 200;
//    public static final double BRANCH_OCCURRENCE_THRESHOLD = 20;
//    public static final double BRANCH_RATIO_THRESHOLD = 0.01d;
    
    //81 centers, 0.587998420333592 v.s. kmeans=0.543936662752116~0.570525825373921
//    public static final int TAG_BRANCHING_OCCURRENCE_THRESHOLD = 200;
//    public static final double BRANCH_OCCURRENCE_THRESHOLD = 30;
//    public static final double BRANCH_RATIO_THRESHOLD = 0.01d;
    
    //54 centers, 0.556980020200752 v.s. kmeans=0.427998055140778~0.464353053087802
//    public static final int TAG_BRANCHING_OCCURRENCE_THRESHOLD = 200;
//    public static final double BRANCH_OCCURRENCE_THRESHOLD = 50;
//    public static final double BRANCH_RATIO_THRESHOLD = 0.01d;
    
    //38 centers, 0.516166777901111 v.s. kmeans=0.385533109047687~0.425939075950038
//    public static final int TAG_BRANCHING_OCCURRENCE_THRESHOLD = 200;
//    public static final double BRANCH_OCCURRENCE_THRESHOLD = 100;
//    public static final double BRANCH_RATIO_THRESHOLD = 0.1d;
//    
    
    private static HashSet<String> tagTreeNodeSet = new HashSet<>();
    private static int tagVectorIndex = 1;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        // TODO code application logic here
        RenjinScriptEngineFactory factory = new RenjinScriptEngineFactory();
        ScriptEngine engine = factory.getScriptEngine();
        String script = FileUtils.readFileToString(new File("script.R"), "utf-8");
        engine.eval(script);
        //System.out.println(engine.eval("nrow(tags)").getClass());
        org.renjin.sexp.StringArrayVector tagNames = (org.renjin.sexp.StringArrayVector) engine.eval("as.character(tagNames)");
//        org.renjin.primitives.matrix.DeferredMatrixProduct cooccur=(org.renjin.primitives.matrix.DeferredMatrixProduct) engine.eval("cooccur");
//        Matrix matrix=new Matrix(cooccur);
//        System.out.println(matrix.getElementAsDouble(0, 0));
        org.renjin.sexp.StringArrayVector tags = (org.renjin.sexp.StringArrayVector) engine.eval("as.vector(indTags[indTags$indTagRatio>=0.5,]$indTagName)");
        ListVector tagResults = (ListVector) engine.eval("tags");
//        org.renjin.sexp.IntBufferVector bufferVector=(org.renjin.sexp.IntBufferVector) (tagResults.get(0));
//        System.out.println(bufferVector.getElementAsInt(100));
        TagTreeNode python = new TagTreeNode("python", -1);
        for (int i = 0; i < tags.length(); i++) {
            String tag = tags.getElementAsString(i);
            int tagIndex = tagNames.indexOf(tag) + 1;
            TagTreeNode directChild = new TagTreeNode(tag, tagIndex);
            String key = toUniqueKey(directChild);
            if (tagTreeNodeSet.contains(key)) {
                continue;
            } else {
                tagTreeNodeSet.add(key);
            }
            python.addChildNode(directChild);

            double base = countMatch(tagResults, directChild.getPath());
            processTagTreeNode(tagResults, tagNames, base, directChild);
        }
        System.out.println(python);
        //System.out.println(Arrays.deepToString(toRVectorNotation(python).toArray()));
        List<String> vectors1 = toRVectorNotation(python);
        List<String> vectors = new ArrayList<>();
        for (int i = 1; i < tagVectorIndex; i++) {
            vectors.add("TAGVECTOR" + i);
        }

        for (String str : vectors1) {
            System.out.println(str);
        }
        String output = "clusters=matrix(c(" + String.join(",", vectors.toArray(new String[0])) + "), byrow=TRUE, nrow=" + (tagVectorIndex - 1) + ")";
        System.out.println(output);
        
        Set<Integer> tagIndicies=getAllNodesAsIndicies(python);
        System.out.println(Arrays.deepToString(tagIndicies.toArray()).replace("[", "(").replace("]", ")"));

//        List<double[]> centers = getAllNodesAsVectors(tagResults, python);
//        List<double[]> tagsd = new ArrayList<>();
//        for (int i = 0; i < tagResults.maxElementLength(); i++) {
//            double[] tagValue = new double[tagResults.length()];
//            for (int j = 0; j < tagResults.length(); j++) {
//                IntBufferVector bufferVector = (org.renjin.sexp.IntBufferVector) (tagResults.get(j));
//                tagValue[j] = bufferVector.getElementAsDouble(i);
//            }
//            tagsd.add(tagValue);
//        }
//
//        Map<Integer, double[]> clusterResult = new HashMap<>();
//        for (int i = 0; i < tagsd.size(); i++) {
//            double minDistance = Double.MAX_VALUE;
//            double[] closestCenter = null;
//            for (double[] center : centers) {
//                double distance = calculateDistance(tagsd.get(i), center);
//                if (distance < minDistance) {
//                    closestCenter = center;
//                    minDistance = distance;
//                }
//            }
//            clusterResult.put(i, closestCenter);
//        }
//        
//        double totalDistance=0;
//        double betweenDistance=0;
//        for (int i = 0; i < tagsd.size(); i++) {
//            for (int j = i + 1; j < tagsd.size(); j++) {
//                double [] point1=tagsd.get(i);
//                double [] point2=tagsd.get(j);
//                double distance=calculateDistance(point1, point2);
//                totalDistance=totalDistance+distance;
//                if(clusterResult.get(i)!=clusterResult.get(j)){
//                    betweenDistance=betweenDistance+distance;
//                }
//            }
//            System.out.println(i);
//        }
//        System.out.println(betweenDistance/totalDistance);
    }

    private static Set<Integer> getAllNodesAsIndicies(TagTreeNode node) {
        HashSet<Integer> ret = new HashSet<>();
        if (node.getPath().length > 1) {
            ret.add(node.getTagIndex());
        }
        for (TagTreeNode child : node.getChildNodes()) {
            ret.addAll(getAllNodesAsIndicies(child));
        }
        return ret;
    }
    
    private static double calculateDistance(double[] point1, double[] point2) {
        double sum = 0;
        for (int i = 0; i < point1.length; i++) {
            sum = sum + Math.pow(point1[i] - point2[i], 2);
        }
        sum = sum / point1.length;
        return Math.sqrt(sum);
    }

    private static List<double[]> getAllNodesAsVectors(ListVector tagResults, TagTreeNode node) {
        List<double[]> ret = new ArrayList<>();
        if (node.getPath().length > 1) {
            double[] value = new double[tagResults.length()];
            int[] path = node.getPath();
            for (int i : path) {
                if (i != -1) {
                    value[i - 1] = 1;
                }
            }
            ret.add(value);
        }
        for (TagTreeNode child : node.getChildNodes()) {
            ret.addAll(getAllNodesAsVectors(tagResults, child));
        }
        return ret;
    }

    private static List<String> toRVectorNotation(TagTreeNode node) {
        List<String> vectors = new ArrayList<>();
        for (TagTreeNode childNode : node.getChildNodes()) {
            List<String> subExpressions = toRVectorNotation(childNode);
            vectors.addAll(subExpressions);
        }
        int[] path = node.getPath();
        if (path.length > 1 /*&& node.getChildNodes().isEmpty()*/) {
            StringBuffer ret = new StringBuffer();
            String varName = "TAGVECTOR" + (tagVectorIndex++);
            ret.append(varName + "=as.vector(rep(0, times=217))\r\n");
            for (int i : path) {
                if (i != -1) {
                    ret.append(varName + "[" + i + "]=1\r\n");
                }
            }
            vectors.add(ret.toString());
        }
        return vectors;
    }

    private static String toUniqueKey(TagTreeNode node) {
        int[] path = node.getPath();
        TreeSet<Integer> set = new TreeSet<>();
        for (int index : path) {
            set.add(index);
        }
        Integer[] array = set.toArray(new Integer[0]);
        int[] newArray = new int[array.length];
        for (int i = 0; i < array.length; i++) {
            newArray[i] = array[i];
        }
        return Arrays.toString(newArray);
    }

    private static boolean isLoop(TagTreeNode node) {
        int[] path = node.getPath();
        for (int i = 0; i < path.length; i++) {
            for (int j = i + 1; j < path.length; j++) {
                if (path[i] == path[j]) {
                    return true;
                }
            }
        }
        return false;
    }

    private static void processTagTreeNode(ListVector tagResults, StringArrayVector tagNames, double base, TagTreeNode node) {
        if (base < TAG_BRANCHING_OCCURRENCE_THRESHOLD) {
            return;
        }
        //System.out.println(node.getTagName() + ":" + node.getPath().length + ":" + base);
        for (String otherTagName : tagNames) {
            if (otherTagName.equals(node.getTagName()) == false) {
                int otherTagIndex = tagNames.indexOf(otherTagName) + 1;
                double counted = countMatch(tagResults, TagTreeNode.createPath(node, otherTagIndex));
                double ratio = counted / base;
                if (counted > BRANCH_OCCURRENCE_THRESHOLD && ratio > BRANCH_RATIO_THRESHOLD) {
                    TagTreeNode childNode = new TagTreeNode(otherTagName, otherTagIndex);
                    node.addChildNode(childNode);
                    String key = toUniqueKey(childNode);
//                    System.out.println(Arrays.toString(childNode.getPath())+":"+tagTreeNodeSet.contains(key));
//                    if(!tagTreeNodeSet.contains(key)){
//                        System.out.println(Arrays.deepToString(tagTreeNodeSet.toArray()));
//                    }
                    if (isLoop(childNode) || tagTreeNodeSet.contains(key)) {
                        node.removeChildNode(childNode);
                        continue;
                    }
                    tagTreeNodeSet.add(key);
                    processTagTreeNode(tagResults, tagNames, counted, childNode);
                }
            }
        }
    }

    private static double countMatch(ListVector tagResults, int[] path) {
        int nrows = ((org.renjin.sexp.IntBufferVector) (tagResults.get(0))).length();
        double count = 0;
        outer:
        for (int i = 0; i < nrows; i++) {
            for (int j = 0; j < path.length; j++) {
                if (path[j] == -1) {
                    continue;
                } else {
                    //check the corresponding column j for the given row i
                    IntBufferVector bufferVector = (org.renjin.sexp.IntBufferVector) (tagResults.get(path[j] - 1));
                    if (bufferVector.getElementAsInt(i) == 0) {
                        continue outer;
                    }
                }
            }
            count = count + 1;
        }
        return count;
    }

}
