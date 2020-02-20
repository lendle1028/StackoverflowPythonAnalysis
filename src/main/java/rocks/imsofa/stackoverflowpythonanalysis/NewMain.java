/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rocks.imsofa.stackoverflowpythonanalysis;

import java.io.File;
import javax.script.ScriptEngine;
import org.apache.commons.io.FileUtils;
import org.renjin.script.RenjinScriptEngineFactory;
import org.renjin.sexp.IntBufferVector;
import org.renjin.sexp.ListVector;
import org.renjin.sexp.StringArrayVector;
import org.renjin.sexp.Vector;

/**
 *
 * @author lendle
 */
public class NewMain {

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
        org.renjin.sexp.StringArrayVector tags = (org.renjin.sexp.StringArrayVector) engine.eval("as.vector(indTags[indTags$indTagRatio<0.5,]$indTagName)");
        ListVector tagResults = (ListVector) engine.eval("tags");
//        org.renjin.sexp.IntBufferVector bufferVector=(org.renjin.sexp.IntBufferVector) (tagResults.get(0));
//        System.out.println(bufferVector.getElementAsInt(100));
        TagTreeNode python = new TagTreeNode("python", -1);
        for (int i = 0; i < tags.length(); i++) {
            String tag = tags.getElementAsString(i);
            int tagIndex = tagNames.indexOf(tag) + 1;
            TagTreeNode directChild = new TagTreeNode(tag, tagIndex);
            python.addChildNode(directChild);

            double base = countMatch(tagResults, directChild.getPath());
            processTagTreeNode(tagResults, tagNames, base, directChild);
//            for (String otherTagName : tagNames) {
//                if (otherTagName.equals(tag) == false) {
//                    int otherTagIndex = tagNames.indexOf(otherTagName) + 1;
//                    double counted = countMatch(tagResults, TagTreeNode.createPath(directChild, otherTagIndex));
//                    double ratio = counted / base;
//                    if (ratio > 0.1) {
//                        TagTreeNode childNode = new TagTreeNode(otherTagName, otherTagIndex);
//                        directChild.addChildNode(childNode);
//                    }
//                }
//            }
        }
        System.out.println(python);
    }

    private static void processTagTreeNode(ListVector tagResults, StringArrayVector tagNames, double base, TagTreeNode node) {
        for (String otherTagName : tagNames) {
            if (otherTagName.equals(node.getTagName()) == false) {
                int otherTagIndex = tagNames.indexOf(otherTagName) + 1;
                double counted = countMatch(tagResults, TagTreeNode.createPath(node, otherTagIndex));
                double ratio = counted / base;
                if (ratio > 0.1) {
                    TagTreeNode childNode = new TagTreeNode(otherTagName, otherTagIndex);
                    node.addChildNode(childNode);
                    //processTagTreeNode(tagResults, tagNames, counted, childNode);
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
