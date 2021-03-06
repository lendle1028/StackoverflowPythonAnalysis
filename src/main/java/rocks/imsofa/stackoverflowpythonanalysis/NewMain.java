/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rocks.imsofa.stackoverflowpythonanalysis;

import com.google.gson.Gson;
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
//    public static int TAG_BRANCHING_OCCURRENCE_THRESHOLD = 100;
//    public static double BRANCH_OCCURRENCE_THRESHOLD = 10;
//    public static double BRANCH_RATIO_THRESHOLD = 0.1d;
//    public static double IND_TAG_RATIO_THRESHOLD_AS_STARTING_TAGS=0.5d;
    //91 centers, 0.606568199088119 v.s. kmeans=0.55289058616877~0.586197342259639
//    public static int TAG_BRANCHING_OCCURRENCE_THRESHOLD = 200;
//    public static double BRANCH_OCCURRENCE_THRESHOLD = 15;
//    public static double BRANCH_RATIO_THRESHOLD = 0.05d;
//    public static double IND_TAG_RATIO_THRESHOLD_AS_STARTING_TAGS=0.5d;
    //91 centers, 0.606568199088119 v.s. kmeans=0.55289058616877~0.586197342259639
//    public static int TAG_BRANCHING_OCCURRENCE_THRESHOLD = 200;
//    public static double BRANCH_OCCURRENCE_THRESHOLD = 10;
//    public static double BRANCH_RATIO_THRESHOLD = 0.05d;
//    public static double IND_TAG_RATIO_THRESHOLD_AS_STARTING_TAGS=0.5d;
    //106 centers, 0.618722545450784 v.s. kmeans=0.600343634041933~0.626290108516266
//    public static int TAG_BRANCHING_OCCURRENCE_THRESHOLD = 200;
//    public static double BRANCH_OCCURRENCE_THRESHOLD = 20;
//    public static double BRANCH_RATIO_THRESHOLD = 0.01d;
//    public static double IND_TAG_RATIO_THRESHOLD_AS_STARTING_TAGS=0.5d;
    //81 centers, 0.587998420333592 v.s. kmeans=0.543936662752116~0.570525825373921
//    public static int TAG_BRANCHING_OCCURRENCE_THRESHOLD = 200;
//    public static double BRANCH_OCCURRENCE_THRESHOLD = 30;
//    public static double BRANCH_RATIO_THRESHOLD = 0.01d;
//    public static double IND_TAG_RATIO_THRESHOLD_AS_STARTING_TAGS=0.5d;
    //54 centers, 0.556980020200752 v.s. kmeans=0.427998055140778~0.464353053087802
//    public static int TAG_BRANCHING_OCCURRENCE_THRESHOLD = 200;
//    public static double BRANCH_OCCURRENCE_THRESHOLD = 50;
//    public static double BRANCH_RATIO_THRESHOLD = 0.01d;
//    public static double IND_TAG_RATIO_THRESHOLD_AS_STARTING_TAGS=0.5d;
    //38 centers, 0.516166777901111 v.s. kmeans=0.385533109047687~0.425939075950038
    private static double TAG_BRANCHING_OCCURRENCE_THRESHOLD = 200;
    private static double BRANCH_OCCURRENCE_THRESHOLD = 100;
    private static double BRANCH_RATIO_THRESHOLD = 0.1d;
    private static double IND_TAG_RATIO_THRESHOLD_AS_STARTING_TAGS = 0.5d;

    //hold configuration parameters: TAG_BRANCHING_OCCURRENCE_THRESHOLD, BRANCH_OCCURRENCE_THRESHOLD, BRANCH_RATIO_THRESHOLD, IND_TAG_RATIO_THRESHOLD_AS_STARTING_TAGS
    private static double[][] configurations = new double[][]{
        {200, 100, 0.1d, 0.5d},//38 centers, 0.516166777901111 v.s. kmeans=0.385533109047687~0.425939075950038
        {200, 50, 0.01d, 0.5d},//54 centers, 0.556980020200752 v.s. kmeans=0.427998055140778~0.464353053087802
        {200, 30, 0.01d, 0.5d}//54 centers, 0.556980020200752 v.s. kmeans=0.427998055140778~0.464353053087802
    };

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
        org.renjin.sexp.StringArrayVector tagNames = (org.renjin.sexp.StringArrayVector) engine.eval("as.character(tagNames)");
        Map<Integer, Double> kmeansResultCache = new HashMap<Integer, Double>();//# centers=>kmeans results for faster calculation
        List<Round> results=new ArrayList<>();
        //generate configurations
        List<double[]> newConfs = new ArrayList<>();
        double[] values_1 = new double[]{100, 70, 50, 30, 10, 1};//values of parameter 1
        double[] values_2 = new double[]{0.1, 0.05, 0.01, 0.005};//values of parameter 2
        double[] values_3 = new double[]{0.5, 0.4, 0.3};//values of parameter 3
        for (int i = 0; i < values_1.length; i++) {
            for (int j = 0; j < values_2.length; j++) {
                for (int k = 0; k < values_3.length; k++) {
                    newConfs.add(new double[]{
                        200, values_1[i], values_2[j], values_3[k]
                    });
                }
            }
        }
        configurations = newConfs.toArray(new double[0][0]);

        for (int configIndex = 0; configIndex < configurations.length; configIndex++) {
            StringBuffer rCodes = new StringBuffer();
            tagTreeNodeSet.clear();
            tagVectorIndex = 1;
            //reset configurations
            TAG_BRANCHING_OCCURRENCE_THRESHOLD = configurations[configIndex][0];
            BRANCH_OCCURRENCE_THRESHOLD = configurations[configIndex][1];
            BRANCH_RATIO_THRESHOLD = configurations[configIndex][2];
            IND_TAG_RATIO_THRESHOLD_AS_STARTING_TAGS = configurations[configIndex][3];

            org.renjin.sexp.StringArrayVector tags = (org.renjin.sexp.StringArrayVector) engine.eval("as.vector(indTags[indTags$indTagRatio>=" + IND_TAG_RATIO_THRESHOLD_AS_STARTING_TAGS + ",]$indTagName)");
            ListVector tagResults = (ListVector) engine.eval("tags");
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
            //print out the knowledge tree
            //System.out.println(python);
            List<String> vectors1 = toRVectorNotation(python);//vectors keep a string list of vector construction codes for each tag for R
            List<String> vectors = new ArrayList<>();
            for (int i = 1; i < tagVectorIndex; i++) {
                vectors.add("TAGVECTOR" + i);
            }

            for (String str : vectors1) {
                //System.out.println(str);
                rCodes.append(str).append("\r\n");
            }

            //construct a matrix to be used by kmeans
            String output = "clusters=matrix(c(" + String.join(",", vectors.toArray(new String[0])) + "), byrow=TRUE, nrow=" + (tagVectorIndex - 1) + ")";
            //System.out.println(output);
            rCodes.append(output).append("\r\n");

            Set<Integer> tagIndicies = getAllNodesAsIndicies(python);
            //System.out.println(Arrays.deepToString(tagIndicies.toArray()).replace("[", "(").replace("]", ")"));
            //output result
            rCodes.append("k=kmeans(tags, clusters, iter.max=1)\r\n");
            rCodes.append("k0=k$betweenss/k$totss\r\n");
            rCodes.append("str=paste(\"centers=" + vectors.size() + ", " + configurations[configIndex][0]
                    + ", " + configurations[configIndex][1]
                    + ", " + configurations[configIndex][2]
                    + ", " + configurations[configIndex][3] + ": \", k0)\r\n");
            rCodes.append("print(str)\r\n");
            try {
                engine.eval(rCodes.toString());
                org.renjin.primitives.R$primitive$$div$deferred_dd k = (org.renjin.primitives.R$primitive$$div$deferred_dd) engine.eval("k0");
                Round round = new Round();
                round.setCenters(vectors.size());
                round.setBRANCH_OCCURRENCE_THRESHOLD(configurations[configIndex][1]);
                round.setBRANCH_RATIO_THRESHOLD(configurations[configIndex][2]);
                round.setTAG_BRANCHING_OCCURRENCE_THRESHOLD(configurations[configIndex][0]);
                round.setIND_TAG_RATIO_THRESHOLD_AS_STARTING_TAGS(configurations[configIndex][3]);
                round.setResult(k.asReal());
                rCodes.delete(0, rCodes.length());
                if (kmeansResultCache.containsKey(vectors.size())) {
                    round.setKmeansResult(kmeansResultCache.get(vectors.size()));
                } else {
                    rCodes.append("k1=kmeans(tags, " + vectors.size() + ")\r\n");
                    rCodes.append("k2=kmeans(tags, " + vectors.size() + ")\r\n");
                    rCodes.append("k3=kmeans(tags, " + vectors.size() + ")\r\n");
                    rCodes.append("k123=(k1$betweenss/k1$totss+k2$betweenss/k2$totss+k3$betweenss/k3$totss)/3\r\n");
                    engine.eval(rCodes.toString());
                    org.renjin.primitives.R$primitive$$div$deferred_dd k1 = (org.renjin.primitives.R$primitive$$div$deferred_dd) engine.eval("k123");
                    round.setKmeansResult(k1.asReal());
                    kmeansResultCache.put(vectors.size(), k1.asReal());
                }
                results.add(round);
                System.out.println("kmeans: "+round.getKmeansResult());
            } catch (Exception e) {
                System.out.println("fail on centers=" + vectors.size());
            }
//            rCodes.append("print(\"k="+vectors.size()+"\")\r\n");
//            rCodes.append("print(k$betweenss/k$totss)\r\n");
            //org.renjin.primitives.R$primitive$$div$deferred_dd k=(org.renjin.primitives.R$primitive$$div$deferred_dd) engine.eval("k$betweenss/k$totss");
            //System.out.println(k.asReal());

            //original kmeans for comparison
//            rCodes.append("k1=kmeans(tags, "+vectors.size()+")\r\n");
//            rCodes.append("k2=kmeans(tags, "+vectors.size()+")\r\n");
//            rCodes.append("k3=kmeans(tags, "+vectors.size()+")\r\n");
//            rCodes.append("k123=(k1$betweenss/k1$totss+k2$betweenss/k2$totss+k3$betweenss/k3$totss)/3\r\n");
//            rCodes.append("paste(\"k="+vectors.size()+"\", k0, \"v.s.\" ,k123)\r\n");
            //org.renjin.primitives.R$primitive$$div$deferred_dd k1=(org.renjin.primitives.R$primitive$$div$deferred_dd) engine.eval("k1$betweenss/k1$totss");
            //System.out.println(k1.asReal());
        }
        
        System.out.println(new Gson().toJson(results));
        //System.out.println(rCodes.toString());

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
