package anonymous.sequence;

import anonymous.Config;
import anonymous.parser.model.ResultModel;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;

public class PostProcessing {
    private static final Logger LOG = LoggerFactory.getLogger(PostProcessing.class);
    private static Gson gson = new Gson();


    public static void main(String[] args) throws IOException {
        LOG.info("Started :: Reading the processed data");
        Map<Long, LinkedList<String>> types = readData(Config.SEQUENCE_OUTPUT_FOLDER + "type-traj-SPQA--data.txt");
        Map<Long, LinkedList<String>> scales = readData(Config.SEQUENCE_OUTPUT_FOLDER + "osm-prank.txt");
        Map<Long, LinkedList<String>> prominences = readData(Config.SEQUENCE_OUTPUT_FOLDER + "osm-importance.txt");
        LOG.info("PostProcessing ::: Filtering ::: Only Shared IDS");
        Set<Long> typeIds = types.keySet();
        Set<Long> scaleIds = scales.keySet();
        Set<Long> prominenceIds = prominences.keySet();
        Set<Long> sharedIds = new HashSet<>();

        for (Long id : scaleIds)
            if (typeIds.contains(id) && prominenceIds.contains(id))
                sharedIds.add(id);

        LOG.info("PostProcessing ::: Filtering ::: Only Same Q/A Sequences");
        Set<Long> validIds = new HashSet<>();
        Map<Long, LinkedList<String>> scalesFiltered = new HashMap<>();
        Map<Long, LinkedList<String>> typesFiltered = new HashMap<>();
        Map<Long, LinkedList<String>> prominencesFiltered = new HashMap<>();
        for (Long id : sharedIds) {
            LinkedList<String> typeSequence = types.get(id);
            int[] typeCounts = getQACounts(typeSequence, "Q-");
            LinkedList<String> scaleSequence = scales.get(id);
            int[] scaleCounts = getQACounts(scaleSequence, "Q-");
            LinkedList<String> prominenceSequence = prominences.get(id);
            int[] prominenceCounts = getQACounts(prominenceSequence, "Q-");

            //if (typeCounts[0] == scaleCounts [0] && scaleCounts[0] == prominenceCounts[0] && typeCounts[1] == scaleCounts [1] && scaleCounts[1] == prominenceCounts[1] && prominenceCounts[1] > 1)
            if (typeCounts[0] > 0 && scaleCounts[0] > 0 && prominenceCounts[0] > 0 && typeCounts[1] > 1 && scaleCounts[1] > 1 && prominenceCounts[1] > 1) {
                validIds.add(id);
                scalesFiltered.put(id, scaleSequence);
                typesFiltered.put(id, typeSequence);
                prominencesFiltered.put(id, prominenceSequence);
            }
        }

        LOG.info("PostProcessing ::: Writing the PostProcessing Output");

        writeData(scalesFiltered, Config.SEQUENCE_OUTPUT_FOLDER + "scale-all.txt", "", true); //all q-a scales
        writeData(scalesFiltered, Config.SEQUENCE_OUTPUT_FOLDER + "scale-Q.txt", "Q-", true); //only q scales
        writeData(scalesFiltered, Config.SEQUENCE_OUTPUT_FOLDER + "scale-A.txt", "Q-", false); //only a scales


        writeData(typesFiltered, Config.SEQUENCE_OUTPUT_FOLDER + "type-all.txt", "", true); //all q-a scales
        writeData(typesFiltered, Config.SEQUENCE_OUTPUT_FOLDER + "type-Q.txt", "Q-", true); //only q scales
        writeData(typesFiltered, Config.SEQUENCE_OUTPUT_FOLDER + "type-A.txt", "Q-", false); //only a scales

        writeData(prominencesFiltered, Config.SEQUENCE_OUTPUT_FOLDER + "prominence-all.txt", "", true); //all q-a scales
        writeData(prominencesFiltered, Config.SEQUENCE_OUTPUT_FOLDER + "prominence-Q.txt", "Q-", true); //only q scales
        writeData(prominencesFiltered, Config.SEQUENCE_OUTPUT_FOLDER + "prominence-A.txt", "Q-", false); //only a scales


        writeData(scales, Config.SEQUENCE_OUTPUT_FOLDER + "scale-nf-all.txt", "", true); //all q-a scales
        writeData(scales, Config.SEQUENCE_OUTPUT_FOLDER + "scale-nf-Q.txt", "Q-", true); //only q scales
        writeData(scales, Config.SEQUENCE_OUTPUT_FOLDER + "scale-nf-A.txt", "Q-", false); //only a scales


        writeData(types, Config.SEQUENCE_OUTPUT_FOLDER + "type-nf-all.txt", "", true); //all q-a scales
        writeData(types, Config.SEQUENCE_OUTPUT_FOLDER + "type-nf-Q.txt", "Q-", true); //only q scales
        writeData(types, Config.SEQUENCE_OUTPUT_FOLDER + "type-nf-A.txt", "Q-", false); //only a scales

        writeData(prominences, Config.SEQUENCE_OUTPUT_FOLDER + "prominence-nf-all.txt", "", true); //all q-a scales
        writeData(prominences, Config.SEQUENCE_OUTPUT_FOLDER + "prominence-nf-Q.txt", "Q-", true); //only q scales
        writeData(prominences, Config.SEQUENCE_OUTPUT_FOLDER + "prominence-nf-A.txt", "Q-", false); //only a scales


        writeTypesAsCodes(types, Config.SEQUENCE_OUTPUT_FOLDER + "type-nf-coded.txt", Config.SEQUENCE_OUTPUT_FOLDER + "type-codes.txt");
        writeOrdinals(scales, Config.SEQUENCE_OUTPUT_FOLDER + "scale-nf-coded.txt");
        writeOrdinals(prominences, Config.SEQUENCE_OUTPUT_FOLDER + "prominence-nf-coded.txt");

        List<Map<Long, LinkedList<String>>> typePromList = new LinkedList<>();
        typePromList.add(types);
        typePromList.add(prominences);
        Map<Long, LinkedList<String>> typeProm = getConcatenated(typePromList);
        LOG.info("Type Prom:: " + typeProm.size());
        writeConcatInCodes(typeProm, Config.SEQUENCE_OUTPUT_FOLDER + "con-type-prom-nf-coded.txt", Config.SEQUENCE_OUTPUT_FOLDER + "con-type-prom-codes.txt");


        List<Map<Long, LinkedList<String>>> scalePromList = new LinkedList<>();
        scalePromList.add(scales);
        scalePromList.add(prominences);
        Map<Long, LinkedList<String>> scaleProm = getConcatenated(scalePromList);
        LOG.info("Scale Prom:: " + scaleProm.size());
        writeConcatInCodes(scaleProm, Config.SEQUENCE_OUTPUT_FOLDER + "con-scale-prom-nf-coded.txt", Config.SEQUENCE_OUTPUT_FOLDER + "con-scale-prom-codes.txt");

        List<Map<Long, LinkedList<String>>> allPromList = new LinkedList<>();
        allPromList.add(types);
        allPromList.add(scales);
        allPromList.add(prominences);
        Map<Long, LinkedList<String>> allProm = getConcatenated(allPromList);
        LOG.info("AllProm:: " + allProm.size());
        writeConcatInCodes(allProm, Config.SEQUENCE_OUTPUT_FOLDER + "con-all-nf-coded.txt", Config.SEQUENCE_OUTPUT_FOLDER + "con-all-codes.txt");


        //write complexities...
        writeComplexityInCodes(allProm, Config.SEQUENCE_OUTPUT_FOLDER + "comp-all-nf-coded.txt", Config.SEQUENCE_OUTPUT_FOLDER + "comp-all-codes.txt", 3);
//        writeComplexityInCodes(scales, Config.SEQUENCE_OUTPUT_FOLDER + "comp-scale-nf-coded.txt", Config.SEQUENCE_OUTPUT_FOLDER + "comp-scale-codes.txt", 2);
//        writeComplexityInCodes(prominences, Config.SEQUENCE_OUTPUT_FOLDER + "comp-prom-nf-coded.txt", Config.SEQUENCE_OUTPUT_FOLDER + "comp-prom-codes.txt", 2);
//        writeComplexityInCodes(types, Config.SEQUENCE_OUTPUT_FOLDER + "comp-type-nf-coded.txt", Config.SEQUENCE_OUTPUT_FOLDER + "comp-type-codes.txt", 2);


        LOG.info("Extracting a sample with 650 question-answer pairs");
        Type resultType = new TypeToken<ArrayList<ResultModel>>() {
        }.getType();
        JsonReader reader = new JsonReader(new FileReader(Config.SEQUENCE_OUTPUT_FOLDER + "filtered-model.json"));
        List<ResultModel> models = gson.fromJson(reader, resultType);
        List<Long> allSharedIDs = new ArrayList<>();
        allSharedIDs.addAll(allProm.keySet());

        Set<Long> randIdsSet = new HashSet<>();
        List<Long> randIds = new ArrayList<>();

        Random objGenerator = new Random();
        while (randIdsSet.size() < 650) {
            int randomNumber = objGenerator.nextInt(allSharedIDs.size());
            randIdsSet.add(allSharedIDs.get(randomNumber));

            LOG.debug("Random No : " + randomNumber + "\tRandom ID: " + allSharedIDs.get(randomNumber));
        }
        randIds.addAll(randIdsSet);
        List<ResultModel> randModels = new ArrayList<>();
        for (ResultModel m : models)
            if (randIds.contains(m.getIdentifier()))
                randModels.add(m);
        LOG.info("Size Random Model: " + randModels.size());

        try (Writer writer = new FileWriter(Config.SEQUENCE_OUTPUT_FOLDER + "random-models.json")) {
            gson.toJson(models, writer);
        }

        LOG.info("Finished");
    }

    public static int[] getQACounts(LinkedList<String> records, String q) {
        int[] result = new int[2];
        int qNum = 0;
        int aNum = 0;
        for (String record : records) {
            if (record.startsWith(q))
                qNum++;
            else
                aNum++;
        }
        result[0] = qNum;
        result[1] = aNum;
        return result;
    }

    public static Map<Long, LinkedList<String>> readData(String fileAddress) throws IOException {
        Map<Long, LinkedList<String>> result = new HashMap<>();
        File file = new File(fileAddress);
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String st;
        while ((st = reader.readLine()) != null) {
            if (st.trim() != "") {
                LinkedList<String> values = new LinkedList<>();
                String[] val = st.split(" ");
                long id = Long.parseLong(val[0]);
                for (String v : val)
                    values.add(v);
                result.put(id, values);
            }
        }
        return result;
    }

    public static void writeData(Map<Long, LinkedList<String>> data, String fileAddress, String required, boolean inclusion) throws IOException {
        File file = new File(fileAddress);
        StringBuilder stringBuilder = new StringBuilder();
        for (LinkedList<String> strs : data.values()) {
            int count = 0;
            if (isValid(strs) && isValidAnswer(strs, true)) {
                for (String str : strs) {
                    count++;
                    if (inclusion) {
                        if (str.contains(required) || count == 1) {
                            stringBuilder.append(str);
                            stringBuilder.append(' ');
                        }

                    } else {
                        if (!str.contains(required) || count == 1) {
                            stringBuilder.append(str);
                            stringBuilder.append(' ');
                        }
                    }
                }
                stringBuilder.append('\n');
            }
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(stringBuilder.toString());
        }
    }

    public static boolean isValid(LinkedList<String> strs) {
        for (String s : strs) {
            if (s.startsWith("Q-"))
                return true;
        }
        return false;
    }

    public static boolean isValidAnswer(LinkedList<String> strs, boolean isType) {
        for (String s : strs) {
            if (s.startsWith("A-"))
                return true;
            else if (isType && !s.startsWith("Q-") && strs.indexOf(s) > 0)
                return true;
        }
        return false;
    }

    public static void writeOrdinals(Map<Long, LinkedList<String>> data, String fileAddress) throws IOException {
        File file = new File(fileAddress);
        StringBuilder stringBuilder = new StringBuilder();

        for (LinkedList<String> strs : data.values()) {
            int countQ = 0;
            if (isCorrect(strs, true) && isValid(strs) && isValidAnswer(strs, false)) {
                for (String key : strs) {
                    if (key.contains("Q-"))
                        countQ++;
                    key = key.replaceAll("Q-", "");
                    key = key.replaceAll("A-", "");
                    stringBuilder.append(key);
                    stringBuilder.append(' ');
                }
                stringBuilder.append(countQ);
                stringBuilder.append('\n');
            }
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(stringBuilder.toString());
        }
    }


    public static Map<Long, LinkedList<String>> getConcatenated(List<Map<Long, LinkedList<String>>> data) {
        Map<Long, LinkedList<String>> result = new HashMap<>();
        int factors = data.size();
        Map<Long, LinkedList<String>> factor0 = data.get(0);

        Set<Long> sharedIds = factor0.keySet();
        for (int i = 1; i < factors; i++) {
            sharedIds = intersection(sharedIds, data.get(i).keySet());
        }
        Set<Long> validIds = new HashSet<>();
        for (Long id : sharedIds) {
            boolean isOk = true;
            int size0 = factor0.get(id).size();
            for (int i = 1; i < factors; i++) {
                int sizei = data.get(i).get(id).size();
                if (sizei != size0) {
                    isOk = false;
                    break;
                }
            }
            if (isOk)
                validIds.add(id);
        }
        LOG.info("#Valid_IDs :: " + validIds.size());

        for (Long id : validIds) {
            LinkedList<String> concatenated = new LinkedList<>();
            for (int i = 0; i < factor0.get(id).size(); i++) {
                String val = "";
                boolean isID = false;
                if (i != 0) {
                    for (int k = 0; k < factors; k++) {
                        if (!data.get(k).get(id).get(i).trim().equals("Q-") &&
                                !data.get(k).get(id).get(i).trim().equals("A-") &&
                                !data.get(k).get(id).get(i).trim().equals("")
                        )
                            val += data.get(k).get(id).get(i) + "\t";
                        else
                            val += "UNK\t";
                    }
                } else {
                    isID = true;
                    val = factor0.get(id).get(0);
                }
                val = val.replaceAll("A-", "");
                if (!isID && val.trim().split("\t").length < factors) {
                    LinkedList<String> record0 = data.get(0).get(id);
                    LinkedList<String> record1 = data.get(1).get(id);
                    LinkedList<String> record2 = new LinkedList<>();
                    if (factors == 3)
                        record2 = data.get(2).get(id);
                    LOG.info("wait here for me :D");
                }
                concatenated.add(val.trim());
            }
            result.put(id, concatenated);
        }
        return result;
    }


    public static void writeTypesAsCodes(Map<Long, LinkedList<String>> data, String fileAddress, String fileAddressCode) throws IOException {
        File file = new File(fileAddress);
        StringBuilder stringBuilder = new StringBuilder();

        File fileCode = new File(fileAddressCode);
        StringBuilder stringBuilderCode = new StringBuilder();

        Set<String> keys = new TreeSet<>();

        for (LinkedList<String> strs : data.values()) {
            for (String key : strs) {
                if (strs.indexOf(key) > 0) {
                    key = key.replaceAll("Q-", "");
                    keys.add(key.trim());
                }
            }
        }

        List<String> keysList = new ArrayList<>();
        keysList.addAll(keys);
        for (String key : keysList) {
            stringBuilderCode.append(keysList.indexOf(key));
            stringBuilderCode.append(' ');
            stringBuilderCode.append(key);
            stringBuilderCode.append('\n');
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileCode))) {
            writer.write(stringBuilderCode.toString());
        }
        for (LinkedList<String> strs : data.values()) {
            if (isCorrect(strs, false) && isValid(strs) && isValidAnswer(strs, true)) {
                int countQ = 0;
                for (String key : strs) {
                    if (key.contains("Q-"))
                        countQ++;
                    if (strs.indexOf(key) > 0) {
                        key = key.replaceAll("Q-", "");
                        stringBuilder.append(keysList.indexOf(key.trim()));
                    } else
                        stringBuilder.append(key.trim());
                    stringBuilder.append(' ');
                }
                stringBuilder.append(countQ);
                stringBuilder.append('\n');
            }
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(stringBuilder.toString());
        }
    }


    public static void writeComplexityInCodes(Map<Long, LinkedList<String>> data, String fileAddress, String fileAddressCode, int num) throws IOException {
        File file = new File(fileAddress);
        StringBuilder stringBuilder = new StringBuilder();

        File fileCode = new File(fileAddressCode);
        StringBuilder stringBuilderCode = new StringBuilder();

        Set<String> keys = new TreeSet<>();

        for (LinkedList<String> strs : data.values()) {
            for (String key : strs) {
                if (strs.indexOf(key) > 0) {
                    key = key.replaceAll("Q- ", "Q-UNK");
                    key = key.replaceAll("A- ", "A-UNK");
                    key = key.replaceAll("Q-", "");
                    if (key.trim().split("\t").length < num) {
                        LOG.debug("faulty sequence... remove it...");
                    } else {
                        keys.add(key.trim());
                    }
                }
            }
        }

        List<String> keysList = new ArrayList<>();
        keysList.addAll(keys);
        for (String key : keysList) {
            stringBuilderCode.append(keysList.indexOf(key));
            stringBuilderCode.append(' ');
            stringBuilderCode.append(key);
            stringBuilderCode.append('\n');
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileCode))) {
            writer.write(stringBuilderCode.toString());
        }
        for (Long id : data.keySet()) {
            LinkedList<String> strs = data.get(id);
            int countQ = 0;
            String localString = "";
            for (String key : strs) {
                if (key.contains("Q-"))
                    countQ++;
                if (strs.indexOf(key) > 0) {
                    key = key.replaceAll("Q- ", "Q-UNK");

                    key = key.replaceAll("A- ", "A-UNK");
                    if (key.trim().equals(""))
                        key = "UNK";
                    key = key.replaceAll("Q-", "");
                    if (key.split("\t").length == num) {
                        localString+=keysList.indexOf(key.trim())+" ";
                        stringBuilder.append(keysList.indexOf(key.trim()));
                        stringBuilder.append(' ');
                    }
                }
            }
            if (localString.trim().split(" ").length < (strs.size() - countQ)) {
                LOG.info("Wait here...");
            }
            stringBuilder.append(strs.size() - countQ - 1);

            stringBuilder.append('\n');
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(stringBuilder.toString());
        }
    }

    public static void writeConcatInCodes(Map<Long, LinkedList<String>> data, String fileAddress, String fileAddressCode) throws IOException {
        File file = new File(fileAddress);
        StringBuilder stringBuilder = new StringBuilder();

        File fileCode = new File(fileAddressCode);
        StringBuilder stringBuilderCode = new StringBuilder();

        Set<String> keys = new TreeSet<>();

        for (Long id : data.keySet()) {
            LinkedList<String> strs = data.get(id);
            for (String key : strs) {
                if (strs.indexOf(key) > 0) {
                    String key_replaced = key.replaceAll("Q- ", "Q-UNK ");
                    key_replaced = key_replaced.replaceAll("Q-", "");
                    if (key_replaced.split("\t").length < 3) {
                        LOG.debug("missmatch -- check the sequence...");
                    } else {
                        keys.add(key_replaced.trim());
                    }
                }
            }
        }

        List<String> keysList = new ArrayList<>();
        keysList.addAll(keys);
        for (String key : keysList) {
            stringBuilderCode.append(keysList.indexOf(key));
            stringBuilderCode.append(' ');
            stringBuilderCode.append(key);
            stringBuilderCode.append('\n');
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileCode))) {
            writer.write(stringBuilderCode.toString());
        }
        for (LinkedList<String> strs : data.values()) {
            int countQ = 0;
            for (String key : strs) {
                if (key.contains("Q-"))
                    countQ++;
                if (strs.indexOf(key) > 0) {
                    key = key.replaceAll("Q- ", "Q-UNK ");
                    key = key.replaceAll("Q-", "");
                    if (key.split("\t").length == 3)
                        stringBuilder.append(keysList.indexOf(key.trim()));
                    else
                        LOG.debug(key);
                } else
                    stringBuilder.append(key.trim());
                stringBuilder.append(' ');
            }
            stringBuilder.append(countQ);
            stringBuilder.append('\n');
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(stringBuilder.toString());
        }
    }

    public static boolean isCorrect(List<String> strs, boolean isOrdinal) {
        String string = "";
        for (String s : strs)
            string += s + " ";
        string = string.trim();
        if (isOrdinal) {
            return string.contains("Q-") && string.contains("A-");
        } else {
            int qs = string.split("Q-").length;
            return strs.size() > qs && qs > 1;
        }
    }

    public static Set<Long> intersection(Set<Long> a, Set<Long> b) {
        // unnecessary; just an optimization to iterate over the smaller set
        if (a.size() > b.size()) {
            return intersection(b, a);
        }

        Set<Long> results = new HashSet<>();

        for (Long element : a) {
            if (b.contains(element)) {
                results.add(element);
            }
        }

        return results;
    }
}

