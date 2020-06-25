/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rocks.imsofa.stackoverflowpythonanalysis.crawlers;

import com.github.kevinsawicki.http.HttpRequest;
import com.google.gson.Gson;
import java.io.File;
import java.util.Map;
import org.codehaus.plexus.util.FileUtils;

/**
 *
 * @author lendle
 */
public class Crawler {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        // TODO code application logic here
        String baseUrl="https://api.stackexchange.com/2.2/questions?key=TsELULFShNfv3LtUcuTk4Q((&order=desc&sort=votes&min=10&site=stackoverflow&tagged=javascript&pagesize=100";
        File storageFolder=new File("python_questions");
        if(!storageFolder.exists() || !storageFolder.isDirectory()){
            storageFolder.mkdirs();
        }
        Gson gson=new Gson();
        for(int i=1; i<=1000;){
            System.out.println("page="+i);
            String url=baseUrl+"&page="+i;
            String body=HttpRequest.get(url).trustAllCerts().trustAllHosts().acceptGzipEncoding().uncompress(true).body();
            Map json=gson.fromJson(body, Map.class);
            if(json.containsKey("backoff")){
                int backoffSeconds=Integer.valueOf(""+json.get("backoff"));
                System.out.println("backoff="+backoffSeconds);
                Thread.sleep((backoffSeconds+1)*1000);
                continue;
            }
            File file=new File(storageFolder, ""+i+".json");
            FileUtils.fileWrite(file.getAbsolutePath(), "utf-8", body);
            
            Boolean hasMore=(Boolean) json.get("has_more");
            if(hasMore){
                Thread.sleep(3000);
                i++;
            }else{
                break;
            }
        }
    }
    
}
