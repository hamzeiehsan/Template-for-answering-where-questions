package anonymous.dataset;

import anonymous.dataset.model.basic.PassageModel;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

public class DatasetPassageParser {

    public static void run(String address) throws Exception {
        List<Long> ids = getLocationIDs(address);
        System.out.println("size of location questions: "+ids.size());
        writeLocationPassages(address, ids, address+".location.txt");
    }

    public static void writeLocationPassages(String address, List<Long> locationIds, String outFile) throws Exception {
        File fout = new File(outFile);
        FileOutputStream fos = new FileOutputStream(fout);
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
        File file = new File(address);
        JsonFactory f = new JsonFactory();
        ObjectMapper mapper = new ObjectMapper();
        JsonParser jp = f.createParser(file);
        JsonToken current;
        current = jp.nextToken();
        if (current != JsonToken.START_OBJECT) {
            System.out.println("Error: root should be object: quiting.");
            return;
        }
        while (jp.nextToken() != JsonToken.END_OBJECT) {
            String fieldName = jp.getCurrentName();
            // move from field name to field value
            current = jp.nextToken();
            if (fieldName.equals("passages")) {
                if (current == JsonToken.START_OBJECT) {
                    // For each of the records in the array
                    while (jp.nextToken() != JsonToken.END_OBJECT) {
                        String id = jp.getCurrentName();
                        JsonToken arr = jp.nextToken();
                        if (id != null && locationIds.contains(Long.parseLong(id))) {
                            if (arr == JsonToken.START_ARRAY) {
                                List<PassageModel> models = mapper.readValue(jp, new TypeReference<List<PassageModel>>(){});
                                for (PassageModel model : models) {
                                    bw.write(id + "\t|\t" + model.toString());
                                    bw.newLine();
                                }
                            } else {
                                System.out.println("Error: not an array !!!!!: skipping.");
                                jp.skipChildren();
                            }
                        } else {
//                            System.out.println("Not location question, let's skip!");
                            jp.skipChildren();
                        }
                    }
                } else {
                    System.out.println("records should be objects not arrays: skipping...");
                    jp.skipChildren();
                }
            } else {
                System.out.println("Unprocessed property: " + fieldName);
                jp.skipChildren();
            }
        }
        bw.close();
    }

    public static List<Long> getLocationIDs(String address) throws Exception {
        List<Long> values = new ArrayList<>();
        File file = new File(address);
        JsonFactory f = new JsonFactory();
        JsonParser jp = f.createParser(file);
        JsonToken current;
        current = jp.nextToken();
        if (current != JsonToken.START_OBJECT) {
            System.out.println("Error: root should be object: quiting.");
            return new ArrayList<>();
        }
        while (jp.nextToken() != JsonToken.END_OBJECT) {
            String fieldName = jp.getCurrentName();
            // move from field name to field value
            current = jp.nextToken();
            if (fieldName.equals("query_type")) {
                if (current == JsonToken.START_OBJECT) {
                    // For each of the records in the array
                    while (jp.nextToken() != JsonToken.END_OBJECT) {
                        String id = jp.getCurrentName();
                        String val = jp.getText();
                        if (val.equals("LOCATION")) {
                            values.add(Long.parseLong(id));
                        }
                    }
                } else {
                    System.out.println("Error: records should be an array: skipping.");
                    jp.skipChildren();
                }
            } else {
                System.out.println("Unprocessed property: " + fieldName);
                jp.skipChildren();
            }
        }
        return values;
    }
}
