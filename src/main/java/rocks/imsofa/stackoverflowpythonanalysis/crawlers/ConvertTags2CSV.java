/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rocks.imsofa.stackoverflowpythonanalysis.crawlers;

import com.google.gson.Gson;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author lendle
 */
public class ConvertTags2CSV {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        // TODO code application logic here
        File baseFolder = new File("python_questions");
        //first, collect tags
        Set<String> tags = new HashSet<>();
        Gson gson = new Gson();
        for (File file : baseFolder.listFiles()) {
            if (!file.getName().endsWith(".json")) {
                continue;
            }
            String json = FileUtils.readFileToString(file, "utf-8");
            Map questions = gson.fromJson(json, Map.class);
            List items = (List) questions.get("items");
            for (int i = 0; i < items.size(); i++) {
                Map item = (Map) items.get(0);
                List tagList = (List) item.get("tags");
                for (int j = 0; j < tagList.size(); j++) {
                    String tag = (String) tagList.get(j);
                    if (tag.length() >= 3 && Character.isLetter(tag.charAt(0))) {
                        tags.add((String) tagList.get(j));
                    }
                }
            }
        }
        //second, record tag along with their order number
        List<String> orderedTags = new ArrayList<>(tags);
        //output headers
        List<String> headers = new ArrayList<>(orderedTags);
        headers.remove("id");
        System.out.println(headers.size());
        headers.add(0, "id");
        File csvFile = new File("result.csv");
        File csvFile0or1 = new File("result0or1.csv");
        FileUtils.write(csvFile, String.join(",", headers.toArray(new String[0])), "utf-8");
        FileUtils.write(csvFile0or1, String.join(",", headers.toArray(new String[0])), "utf-8");
        //output contents
        for (File file : baseFolder.listFiles()) {
            if (!file.getName().endsWith(".json")) {
                continue;
            }
            System.out.println(file);
            String json = FileUtils.readFileToString(file, "utf-8");
            Map questions = gson.fromJson(json, Map.class);
            List items = (List) questions.get("items");
            for (int i = 0; i < items.size(); i++) {
                Map item = (Map) items.get(i);
                List<String> tagList = (List) item.get("tags");
                Map<String, Object> values = new HashMap<>();//store position index of tags
                Map<String, Object> values0or1 = new HashMap<>();//store only 0 or 1
                Long id=Double.valueOf(""+item.get("question_id")).longValue();
                values.put("id", id);
                values0or1.put("id", id);
                for (String tag : orderedTags) {
                    values.put(tag, 0);
                    values0or1.put(tag, 0);
                }
                for (int j = 0; j < tagList.size(); j++) {
                    String tag = tagList.get(j);
                    if (tags.contains(tag)) {
                        values.put(tagList.get(j), j + 1);
                        values0or1.put(tagList.get(j), 1);
                    }
                }
                List<String> list = new ArrayList<>();
                for (String header : headers) {
                    list.add("" + values.get(header));
                }
                FileUtils.write(csvFile, "\r\n" + String.join(",", list.toArray(new String[0])), "utf-8", true);

                List<String> list0or1 = new ArrayList<>();
                for (String header : headers) {
                    list0or1.add("" + values0or1.get(header));
                }
                FileUtils.write(csvFile0or1, "\r\n" + String.join(",", list0or1.toArray(new String[0])), "utf-8", true);
            }
            System.out.println("\t......finished");
        }
        
    }

}
