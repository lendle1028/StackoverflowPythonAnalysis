/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rocks.imsofa.stackoverflowpythonanalysis;

import java.io.File;
import java.util.Arrays;
import javax.script.ScriptEngine;
import org.apache.commons.io.FileUtils;
import org.renjin.script.RenjinScriptEngineFactory;
import org.renjin.sexp.ListVector;
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
        TagTreeNode python=new TagTreeNode("python", -1);
        for (int i = 0; i < tags.length(); i++) {
            String tag = tags.getElementAsString(i);
            int tagIndex=tagNames.indexOf(tag)+1;
            TagTreeNode directChild=new TagTreeNode(tag, tagIndex);
            python.addChildNode(directChild);
            Vector selfTaggingResult = tagResults.getElementAsVector(tag);
            System.out.println(tag+":"+tagIndex+Arrays.toString(directChild.getPath()));
            double base = 0;
            for (int j = 0; j < selfTaggingResult.length(); j++) {
                if (selfTaggingResult.getElementAsInt(j) == 1) {
                    base = base + 1;
                }
            }
            //((org.renjin.sexp.IntArrayVector) engine.eval("nrow(tags[tags$"+tag+"==1,])")).asReal();
            for (String otherTagName : tagNames) {
                if (otherTagName.equals(tag) == false) {
                    int otherTagIndex=tagNames.indexOf(otherTagName)+1;
                    Vector otherTaggingResult = tagResults.getElementAsVector(otherTagName);
                    double counted=0;
                    for (int j = 0; j < selfTaggingResult.length(); j++) {
                        if (selfTaggingResult.getElementAsInt(j) == 1 && otherTaggingResult.getElementAsInt(j)==1) {
                            counted = counted + 1;
                        }
                    }
                    //org.renjin.sexp.IntArrayVector nrows = (org.renjin.sexp.IntArrayVector) engine.eval("nrow(tags[tags$" + tag + "==1 & tags$" + tagName + "==1,])");
                    //double ratio = (nrows.asReal() / base);
                    double ratio=counted/base;
                    if (ratio > 0.1) {
                        TagTreeNode childNode=new TagTreeNode(otherTagName, otherTagIndex);
                        directChild.addChildNode(childNode);
                        System.out.println("\t" + otherTagName+":"+(otherTagIndex)+":"+Arrays.toString(childNode.getPath()));
                    }
                }
            }
        }
        System.out.println(python);
    }

}
