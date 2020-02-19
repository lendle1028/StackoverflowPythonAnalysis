/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rocks.imsofa.stackoverflowpythonanalysis;

import java.io.File;
import javax.script.ScriptEngine;
import org.apache.commons.io.FileUtils;
import org.renjin.primitives.matrix.Matrix;
import org.renjin.script.RenjinScriptEngineFactory;
import org.renjin.sexp.ListVector;
import org.renjin.sexp.Symbol;

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
//        org.renjin.primitives.matrix.DeferredMatrixProduct cooccur=(org.renjin.primitives.matrix.DeferredMatrixProduct) engine.eval("cooccur");
//        Matrix matrix=new Matrix(cooccur);
//        System.out.println(matrix.getElementAsDouble(0, 0));
        org.renjin.sexp.StringArrayVector tags=(org.renjin.sexp.StringArrayVector) engine.eval("as.vector(indTags[indTags$indTagRatio<0.5,]$indTagName)");
        for(int i=0; i<tags.length(); i++){
            String tag=tags.getElementAsString(i);
            System.out.println(engine.eval("tags[tags$"+tag+"==1,]").getClass());
        }
    }

}