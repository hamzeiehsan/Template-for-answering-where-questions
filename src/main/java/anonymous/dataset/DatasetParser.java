package anonymous.dataset;

import anonymous.dataset.model.BigDataModel;
import anonymous.dataset.model.basic.DataModel;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import anonymous.dataset.model.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Modified by [anonymous] [anonymous] on 20/01/2020.
 * parsing MS MARCO dataset
 */
public class DatasetParser {
    private static final Logger LOG = LoggerFactory.getLogger(DatasetParser.class);


    public static List<DataModel> parse(String rawAddress, String passageAddress, String outAddress) throws IOException {
        Map<Long, List<String>> passages = readPassages(passageAddress);

        List<DataModel> datasetV21 = new ArrayList<>();
        BigDataModel dataModels = readBigDataModel(rawAddress);
        Integer size = 0;
        for (String key : dataModels.getQuery_type().keySet()) {
            if (dataModels.getQuery_type().get(key).equals("LOCATION")) {
                size++;
                DataModel dm = new DataModel();
                dm.setQuery(dataModels.getQuery().get(key));
                dm.setQueryId(dataModels.getQuery_id().get(key));
                dm.setAnswers(dataModels.getAnswers().get(key));
                dm.setQueryType("LOCATION");
                List<String> ps = passages.get(Long.parseLong(key));
                dm.setPassages(ps);
                datasetV21.add(dm);
            }
        }
        System.out.println("Result: the size of location queries: " + size);
        System.out.println("Writing word counts for answers");

        return datasetV21;
    }

    public static List<DataModel> lightParse(String rawAddress, String outAddress) throws IOException {
        BigDataModel dataModels = readBigDataModel(rawAddress);

        List<DataModel> datasetV21 = new ArrayList<>();
        for (String key : dataModels.getQuery_type().keySet()) {
            if (dataModels.getQuery_type().get(key).equals("LOCATION")) {

                DataModel dm = new DataModel();
                dm.setQuery(dataModels.getQuery().get(key));
                dm.setQueryId(dataModels.getQuery_id().get(key));
                dm.setAnswers(dataModels.getAnswers().get(key));
                dm.setQueryType(dataModels.getQuery_type().get(key));
                datasetV21.add(dm);
            }
        }
        LOG.info("Result: the size of location queries: " + datasetV21.size());
        LOG.info("Writing word counts for answers");
        writeIntoFile(datasetV21, outAddress);
        return datasetV21;
    }

    private static BigDataModel readBigDataModel(String rawAddress) throws IOException {
        Gson gson = new Gson();

        JsonReader reader = new JsonReader(new FileReader(rawAddress));
        BigDataModel dataModels = gson.fromJson(reader, BigDataModel.class);
        LOG.info("Size of Data Model: " + dataModels.getQuery_type().size());
        LOG.info("count LOCATIONS");

        return dataModels;
    }

    private static void writeIntoFile(Object dataset, String outAddress) throws IOException {
        Gson gson = new Gson();

        try (Writer writer = new FileWriter(outAddress)) {
            gson.toJson(dataset, writer);
        }
    }

    private static Map<Long, List<String>> readPassages(String passageAddress) {
        Map<Long, List<String>> passages = new HashMap<>();
        try {
            File file=new File(passageAddress);    //creates a new file instance
            FileReader fr=new FileReader(file);   //reads the file
            BufferedReader br=new BufferedReader(fr);  //creates a buffering character input stream
            String line;
            while((line=br.readLine())!=null) {
                String[] vals = line.split("\t|\t");
                if (vals.length != 3) {
                    System.out.println("Error in the structure of the passage file...");
                } else {
                    Long id = Long.parseLong(vals[0]);
                    String passage = vals[2];
                    if (!passages.containsKey(id))
                        passages.put(id, new ArrayList<String>());
                    passages.get(id).add(passage);
                }
            }
            fr.close();    //closes the stream and release the resources
        }
        catch(IOException e) {
            e.printStackTrace();
        }
        return passages;
    }
}
