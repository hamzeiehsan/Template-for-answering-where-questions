package anonymous.gazetteers.geonames;

import anonymous.Config;
import anonymous.gazetteers.geonames.model.*;
import anonymous.gazetteers.geonames.model.*;
import anonymous.parser.model.AnswersAnalyze;
import anonymous.parser.model.ResultModel;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import org.geonames.Toponym;
import org.geonames.ToponymSearchResult;
import org.geonames.WebService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;

public class Analyze {
    private static final Logger LOG = LoggerFactory.getLogger(Analyze.class);
    public static List<Double> distances = new ArrayList<>();
    public static Map<String, Integer> scalesGeonames = new HashMap<>();
    private static Gson gson = new Gson();
    private static Set<String> combinations = new HashSet<>();

    static {
        List<String> wh = new ArrayList<>();
        wh.add("1");
        wh.add("2");
        wh.add("3");
        wh.add("4");
        wh.add("5");
        wh.add("6");
        wh.add("7");
        wh.add("8");

        List<String> pSemantics = new ArrayList<>();
        pSemantics.add("a");
        pSemantics.add("o");
        pSemantics.add("t");
        pSemantics.add("n");
        pSemantics.add("s");
        pSemantics.add("r");
        pSemantics.add("q");

        combinations.addAll(wh);
        combinations.addAll(pSemantics);

        List<String> tempComb = new ArrayList<>();
        for (String str : combinations) {
            for (String str2 : combinations)
                tempComb.add(str + str2);
        }

        for (String str : combinations)
            LOG.debug(str);

        List<String> tempComb2 = new ArrayList<>();
        for (String str : tempComb) {
            for (String str2 : combinations)
                tempComb2.add(str + str2);
        }
        combinations.addAll(tempComb2);
        for (String str : combinations)
            LOG.debug(str);

        LOG.debug("Size: " + combinations.size());
    }

    public static void main(String[] args) throws Exception {
        LOG.info("Reading results...");
        Type type = new TypeToken<ArrayList<ResultModel>>() {
        }.getType();

        JsonReader reader = new JsonReader(new FileReader(Config.PARSER_2019_FILE_PATH));
        List<ResultModel> tempModels = gson.fromJson(reader, type);
        LOG.info("focusing only on train and dev");
        List<ResultModel> models = new ArrayList<>();
        for (ResultModel m : tempModels)
            if (m.getIdentifier() < 56722)
                models.add(m);
        tempModels = null;

        Type mapType = new TypeToken<Map<String, Integer>>() {
        }.getType();

        LOG.info("Generating Summaries...");
        SummaryModel questionSummaries = new SummaryModel();
        SummaryModel answersSummaries = new SummaryModel();
        SummaryModel allSummaries = new SummaryModel();
        List<CodeModel> questions = new ArrayList<>();
        List<CodeModel> answers = new ArrayList<>();
        Map<String, Set<Long>> questionsUnique = new HashMap<>();
        Map<String, Set<Long>> answersUnique = new HashMap<>();
        Set<String> qCodes = new HashSet<>();


        Map<Long, QuestionAnswerScale> qaScales = new HashMap<>();
        Set<String> aCodes = new HashSet<>();
        for (ResultModel m : models) {
            questionSummaries.addAllPlaceName(m.getQueryAnalyze().getPlaceName());
            allSummaries.addAllPlaceName(m.getQueryAnalyze().getPlaceName());
            questionSummaries.addAllPlaceType(m.getQueryAnalyze().getPlaceType());
            allSummaries.addAllPlaceType(m.getQueryAnalyze().getPlaceType());
            questionSummaries.addAllQuality(m.getQueryAnalyze().getQuality());
            allSummaries.addAllQuality(m.getQueryAnalyze().getQuality());
            questionSummaries.addAllActivities(m.getQueryAnalyze().getActivity());
            allSummaries.addAllActivities(m.getQueryAnalyze().getActivity());
            questionSummaries.addAllSpatialRelations(m.getQueryAnalyze().getSpatialRelation());
            allSummaries.addAllSpatialRelations(m.getQueryAnalyze().getSpatialRelation());
            questionSummaries.addAllSituations(m.getQueryAnalyze().getSituations());
            allSummaries.addAllSituations(m.getQueryAnalyze().getSituations());
            questions.add(new CodeModel(m.getIdentifier(), m.getQuery(), m.getQueryAnalyze().getCode()));
            addToMap(questionsUnique, m.getQueryAnalyze().getCode(), m.getIdentifier());
            qCodes.add(m.getQueryAnalyze().getCode());
            QuestionAnswerScale qaScale = new QuestionAnswerScale();
            qaScale.setIdentifier(m.getIdentifier());

            //answers
            int acounter = 0;
            for (AnswersAnalyze answer : m.getAnswersAnalyze()) {
                qaScale.createAnswerLevel(acounter);
                answersSummaries.addAllPlaceName(answer.getPlaceName());
                answersSummaries.addAllPlaceType(answer.getPlaceType());
                answersSummaries.addAllQuality(answer.getQuality());
                answersSummaries.addAllActivities(answer.getActivity());
                answersSummaries.addAllSpatialRelations(answer.getSpatialRelation());
                answersSummaries.addAllSituations(answer.getSituations());
                allSummaries.addAllPlaceName(answer.getPlaceName());
                allSummaries.addAllPlaceType(answer.getPlaceType());
                allSummaries.addAllQuality(answer.getQuality());
                allSummaries.addAllActivities(answer.getActivity());
                allSummaries.addAllSpatialRelations(answer.getSpatialRelation());
                allSummaries.addAllSituations(answer.getSituations());
                answers.add(new CodeModel(m.getIdentifier(), answer.getSentence(), answer.getCode()));
                addToMap(answersUnique, answer.getCode(), m.getIdentifier());
                aCodes.add(answer.getCode());
                acounter++;
            }
            qaScales.put(m.getIdentifier(), qaScale);

        }
        Summaries summaries = new Summaries();
        summaries.setAll(allSummaries);
        summaries.setQuestions(questionSummaries);
        summaries.setAnswers(answersSummaries);
        Map<String, Integer> qCodesCounted = new HashMap<>();
        doSummarize(questionsUnique, qCodesCounted);
        Map<String, Integer> aCodesCounted = new HashMap<>();
        doSummarize(answersUnique, aCodesCounted);
        LOG.info("Writing summaries...");
        try (Writer writer = new FileWriter(Config.PARSER_OUTPUT_FOLDER + "results-summaries.json")) {
            gson.toJson(summaries, writer);
        }
        LOG.info("Writing Q-Codes...");
        try (Writer writer = new FileWriter(Config.PARSER_OUTPUT_FOLDER + "results-qcodes.json")) {
            gson.toJson(questions, writer);
        }
        try (Writer writer = new FileWriter(Config.PARSER_OUTPUT_FOLDER + "results-qcodes-unique.json")) {
            gson.toJson(questionsUnique, writer);
        }
        try (Writer writer = new FileWriter(Config.PARSER_OUTPUT_FOLDER + "results-just-qcodes.json")) {
            gson.toJson(qCodes, writer);
        }
        try (Writer writer = new FileWriter(Config.PARSER_OUTPUT_FOLDER + "results-qcodes-counted.json")) {
            gson.toJson(qCodesCounted, writer);
        }
        writeMapAsCSV(qCodesCounted, Config.PARSER_OUTPUT_FOLDER + "qCodeCounted.csv");
        LOG.info("Writing A-Codes...");
        try (Writer writer = new FileWriter(Config.PARSER_OUTPUT_FOLDER + "results-acodes.json")) {
            gson.toJson(answers, writer);
        }
        try (Writer writer = new FileWriter(Config.PARSER_OUTPUT_FOLDER + "results-acodes-unique.json")) {
            gson.toJson(answersUnique, writer);
        }
        try (Writer writer = new FileWriter(Config.PARSER_OUTPUT_FOLDER+"results-just-acodes.json")) {
            gson.toJson(aCodes, writer);
        }
        try (Writer writer = new FileWriter(Config.PARSER_OUTPUT_FOLDER+"results-acodes-counted.json")) {
            gson.toJson(aCodesCounted, writer);
        }
        writeMapAsCSV(aCodesCounted, Config.GAZETTEERS_FOLDER+"aCodeCounted.csv");
        writeSummaryAsCSV(summaries, Config.GAZETTEERS_FOLDER+"summaries");


        //read from previous ones!
        LOG.info("Read Current database export...");
        Type dType = new TypeToken<ArrayList<ToponymExportModel>>() {
        }.getType();
        LOG.info("Reading: "+Config.GAZETTEERS_FOLDER+"geonamesexports.json");
        JsonReader dReader = new JsonReader(new FileReader(Config.GAZETTEERS_FOLDER+"geonamesexports.json"));
        List<ToponymExportModel> exports = gson.fromJson(dReader, dType);
        Type dictType = new TypeToken<Map<String, ToponymExportModel>>() {
        }.getType();
        LOG.info("Reading: "+Config.GAZETTEERS_FOLDER+Config.GAZETTEERS_FOLDER+"geonames.json");
        JsonReader dictReader = new JsonReader(new FileReader(Config.GAZETTEERS_FOLDER+"geonames.json"));
        Map<String, ToponymExportModel> database = gson.fromJson(dictReader, dictType);

        Set<PlaceLocation> rawAllPLs = new HashSet<>();
        Set<PlaceLocation> resolvedPLs = new HashSet<>();
        Set<PlaceLocation> resolvedPLsBefore = new HashSet<>();
        Set<PlaceLocation> unResolvedPLs = new HashSet<>();
        try {
            LOG.info("Connecting to Geonames to fetch the reamining toponym information");
            LOG.info("Geocoding...");//consider errors, not found, toponym ambiguity issue and its statistics, dynamic writing in the process... etc.
            WebService.setUserName(Config.GEONAMES_API_KEY); //setting for geonames account!
            WebService.setGeoNamesServer("http://api.geonames.org/");
            for (String pName : allSummaries.getPlaceNames().keySet()) {
                if (pName.contains(" ") && database.containsKey(pName) && database.get(pName).getResult().getToponyms().size() == 0) {
                    ToponymSearchResult value = Search.simpleSearch(pName);
                    ToponymExportModel exp = new ToponymExportModel();
                    exp.setName(pName);
                    exp.setResult(value);
                    database.put(pName, exp);
                }
                boolean working = true;
                LOG.debug("Place name: " + pName);
                ToponymSearchResult result = null;
                if (pName.length() > 1 && !database.containsKey(pName)) {
                    LOG.info("Place name: " + pName + " [Geocoding...]");
                    try {
                        result = Search.search(pName);
                    } catch (Exception e) {
                        working = false;
                        LOG.info("Error!");
                        Thread.sleep(4000000);
                    }
                    if (working == false)
                        result = Search.search(pName);
                    ToponymExportModel export = new ToponymExportModel();
                    export.setName(pName);
                    export.setResult(result);
                    appendStrToFile(Config.GAZETTEERS_FOLDER+"geoname-export", gson.toJson(export) + ",");

                }
            }
        } catch (Exception e) {
            LOG.error("Error in connecting to Geonames...");
        }

        long allCounts = 0l, matchedCounts = 0l, notMatchedCount = 0l, fuzzyMatchedCount = 0l;
        Map<Integer, Integer> ambiguity = new HashMap<>();
        for (String name : database.keySet()) {
            ToponymSearchResult topoResult = database.get(name).getResult();
            if (topoResult.getToponyms() == null || topoResult.getToponyms().size() == 0) {
                LOG.debug("Place name: " + name + " is not localized!");
                notMatchedCount++;
                addToMap(ambiguity, 0);
            } else {
                addToMap(ambiguity, topoResult.getToponyms().size());
                allCounts += topoResult.getToponyms().size();
                for (Toponym t : topoResult.getToponyms()) {
                    PlaceLocation pl = new PlaceLocation();
                    pl.setName(t.getName());
                    pl.setLatitude(t.getLatitude());
                    pl.setLongitude(t.getLongitude());
                    rawAllPLs.add(pl);
                    if (!t.getName().toLowerCase().trim().equals(name.toLowerCase().trim())) {
                        LOG.debug("Not exactly matched -- Name:\t" + name + "\tGN-Name:\t" + t.getName());
                        fuzzyMatchedCount++;
                    } else {
                        LOG.debug("Matched identically: " + name);
                        matchedCounts++;
                    }
                }
            }
        }

        try (Writer writer = new FileWriter(Config.GAZETTEERS_FOLDER+"geonames.json")) {
            gson.toJson(database, writer);
        }


        writeCoordinateToCSV(Config.GAZETTEERS_FOLDER+"toponym-all-pls.csv", rawAllPLs);
        LOG.info("All:\t" + allCounts + "\nMatched:\t" + matchedCounts + "\nFuzzy Matched:\t" + fuzzyMatchedCount + "\nNot Matched:\t" + notMatchedCount);
        LOG.info("Writing ambiguity information...");
        try (Writer writer = new FileWriter(Config.GAZETTEERS_FOLDER+"toponym-ambiguity.json")) {
            gson.toJson(ambiguity, writer);
        }
        Set<String> featureClass = new HashSet<>();
        //disambiguation...
        List<DisambiguatedLocations> locations = new ArrayList<>();
        int i = 0;
        int counter = 0;
        int count0 = 0;
        int correctByDefault = 0;
        int correctCounter = 0;
        int correctCounterPlace = 0;
        int errors = 0;
        int counterNotAnswered = 0;
        Map<String, Integer> disambiguatedCodes = new HashMap<>();
        Map<String, Map<String, Integer>> disambiguatedValues = new HashMap<>();
        Map<Long, Map<String, Map<String, Toponym>>> allDisambiguatedValues = new HashMap<>();
        LOG.info("Now, disambiguating the toponyms...");
        for (ResultModel m : models) {
            boolean fast = false;
            LOG.debug("Now disambiguating - " + i++);
            Set<String> placeNamesSet = new HashSet<>();
            placeNamesSet.addAll(m.getQueryAnalyze().getPlaceName());
            Map<Integer, List<String>> answerPlaceName = new HashMap<>();
            int ac = 0;
            for (AnswersAnalyze a : m.getAnswersAnalyze()) {
                if (a.getPlaceName().size() < 3) {
                    fast = true;
                }
                placeNamesSet.addAll(a.getPlaceName());
                answerPlaceName.put(ac, new ArrayList<String>());
                answerPlaceName.get(ac).addAll(a.getPlaceName());

                ac++;
            }
            List<String> placeNames = new ArrayList<>();
            placeNames.addAll(placeNamesSet);
            try {
                if (placeNames.size() > 1) {
                    for (String name : placeNames) {
                        if (name.length() > 1) {
                            List<Toponym> res = database.get(name).getResult().getToponyms();
                            if (res != null) {
                                for (Toponym t : res) {
                                    PlaceLocation pl = new PlaceLocation();
                                    pl.setName(t.getName());
                                    pl.setLatitude(t.getLatitude());
                                    pl.setLongitude(t.getLongitude());
                                    resolvedPLsBefore.add(pl);
                                }
                            }
                        }
                    }
                    Map<String, Toponym> disambiguatedOnes = new HashMap<>();
                    if (fast)
                        disambiguatedOnes = Disambiguation.disambiguate(placeNames, database);
                    else
                        disambiguatedOnes = Disambiguation.disambiguate2(placeNames, database);
                    DisambiguatedLocations loc = new DisambiguatedLocations();
                    loc.setRecordIdentifier(m.getIdentifier());
                    loc.setPlaces(disambiguatedOnes);
                    locations.add(loc);
                    correctCounter++;
                    correctCounterPlace += placeNames.size();
                    QuestionAnswerScale qaScale = qaScales.get(m.getIdentifier());
                    if (disambiguatedOnes != null && disambiguatedOnes.size() > 0) {
                        populateDisambiguation(disambiguatedOnes, disambiguatedValues, disambiguatedCodes);
                        populateDisambiguation(m, disambiguatedOnes, allDisambiguatedValues);
                    }
                    for (String key : disambiguatedOnes.keySet()) {
                        Toponym t = disambiguatedOnes.get(key);
                        if (t.getFeatureCode() != null && scalesGeonames.containsKey(t.getFeatureCode())) {
                            for (Integer aKey : answerPlaceName.keySet()) {
                                Map<String, Integer> aIndexOf = entityLocation(answerPlaceName.get(aKey), m.getAnswersAnalyze().get(aKey).getSentence());
                                if (answerPlaceName.get(aKey).contains(key) && aIndexOf.get(key) != null) {
                                    qaScale.addAnswerScale(scalesGeonames.get(t.getFeatureCode()), aIndexOf.get(key), aKey);
                                }
                            }
                            Map<String, Integer> qIndexOf = entityLocation(m.getQueryAnalyze().getPlaceName(), m.getQueryAnalyze().getSentence());
                            if (m.getQueryAnalyze().getPlaceName().contains(key) && qIndexOf.get(key) != null) {
                                qaScale.addQuestionScale(qIndexOf.get(key), scalesGeonames.get(t.getFeatureCode()));
                            }

                        }
                        PlaceLocation pl = new PlaceLocation();
                        pl.setName(t.getName());
                        pl.setLatitude(t.getLatitude());
                        pl.setLongitude(t.getLongitude());
                        resolvedPLs.add(pl);
                        featureClass.add(t.getFeatureCode());
                    }
                    qaScales.put(m.getIdentifier(), qaScale);
                } else {
                    if (placeNames.size() == 1) {
                        counter++;
                        List<Toponym> res = database.get(placeNames.get(0)).getResult().getToponyms();
                        if (res != null && res.size() == 1) {
                            correctByDefault++;
                            Toponym t = res.get(0);
                            PlaceLocation pl = new PlaceLocation();
                            pl.setName(t.getName());
                            pl.setLatitude(t.getLatitude());
                            pl.setLongitude(t.getLongitude());
                            featureClass.add(t.getFeatureCode());
                            resolvedPLs.add(pl);
                            resolvedPLsBefore.add(pl);
                        } else if (res != null) {
                            for (Toponym t : res) {
                                PlaceLocation pl = new PlaceLocation();
                                pl.setName(t.getName());
                                pl.setLatitude(t.getLatitude());
                                pl.setLongitude(t.getLongitude());
                                unResolvedPLs.add(pl);
                            }
                            if (m.getAnswers() != null && m.getAnswers().size() == 1 && m.getAnswers().get(0).equals("No Answer Present."))
                                counterNotAnswered++;
                        }
                    } else {
                        count0++;
                        if (m.getAnswers() != null && m.getAnswers().size() == 1 && m.getAnswers().get(0).equals("No Answer Present."))
                            counterNotAnswered++;
                    }
                }
            } catch (Throwable e) {
                LOG.error("Error in disambiguation:: query identifier:: " + m.getIdentifier());
                errors++;
            }
            //}
        }

        writeCoordinateToCSV(Config.GAZETTEERS_FOLDER+"toponym-resolved-pls.csv", resolvedPLs);
        writeCoordinateToCSV(Config.GAZETTEERS_FOLDER+"toponym-unresolved-pls.csv", unResolvedPLs);
        writeCoordinateToCSV(Config.GAZETTEERS_FOLDER+"toponym-resolved-before-pls.csv", resolvedPLsBefore);
        LOG.info("Number of questions/answers with 0, 1, correct by default place names: " + count0 + ", " + counter + ", " + correctByDefault);
        LOG.info("Number of disambiguated records: " + correctCounter + "\nNumber of disambiguated places: " + correctCounterPlace);
        LOG.info("Number of errors in disambiguation: " + errors);
        LOG.info("Number of ambigous ones which are not answered:: \t" + counterNotAnswered);
        LOG.info("Writing disambiguation results...");
        try (Writer writer = new FileWriter(Config.GAZETTEERS_FOLDER+"disambiguated-codes.json")) {
            gson.toJson(disambiguatedCodes, writer);
        }
        try (Writer writer = new FileWriter(Config.GAZETTEERS_FOLDER+"disambiguated-values.json")) {
            gson.toJson(disambiguatedValues, writer);
        }

        try (Writer writer = new FileWriter(Config.GAZETTEERS_FOLDER+"all-disambiguated-values.json")) {
            gson.toJson(allDisambiguatedValues, writer);
        }

        try (Writer writer = new FileWriter(Config.GAZETTEERS_FOLDER+"toponym-disambiguated.json")) {
            gson.toJson(locations, writer);
        }
        writeDistances();
        try (Writer writer = new FileWriter(Config.GAZETTEERS_FOLDER+"featureclass.json")) {
            gson.toJson(featureClass, writer);
        }
        LOG.info("Scale mapping...");
        try (Writer writer = new FileWriter(Config.GAZETTEERS_FOLDER+"QA-A-Scale.json")) {
            gson.toJson(qaScales, writer);
        }

        Map<Integer, Integer> scales = new HashMap<>();
        scales.put(0, 0);
        scales.put(1, 0);
        scales.put(2, 0);
        scales.put(3, 0);
        scales.put(4, 0);
        scales.put(5, 0);
        scales.put(6, 0);
        scales.put(7, 0);
        scales.put(8, 0);
        scales.put(9, 0);
        Map<Integer, Integer> qScales = new HashMap<>();
        scales.put(0, 0);
        qScales.put(1, 0);
        qScales.put(2, 0);
        qScales.put(3, 0);
        qScales.put(4, 0);
        qScales.put(5, 0);
        qScales.put(6, 0);
        qScales.put(7, 0);
        qScales.put(8, 0);
        qScales.put(9, 0);
        Map<Integer, Integer> aScales = new HashMap<>();
        aScales.put(0, 0);
        aScales.put(1, 0);
        aScales.put(2, 0);
        aScales.put(3, 0);
        aScales.put(4, 0);
        aScales.put(5, 0);
        aScales.put(6, 0);
        aScales.put(7, 0);
        aScales.put(8, 0);
        aScales.put(9, 0);
        Map<Long, Integer> qaScalesSize = new HashMap<>();
        Map<Long, Integer> qaScalesASize = new HashMap<>();
        Map<Long, Integer> qaScalesQSize = new HashMap<>();
        int counter_without_scale = 0;
        int counter_with_1scale = 0;
        int counter_with_MoreThan1scale = 0;
        for (QuestionAnswerScale qaScale : qaScales.values()) {
            qaScalesSize.put(qaScale.getIdentifier(), qaScale.getScales().size());
            qaScalesASize.put(qaScale.getIdentifier(), qaScale.getAScaleUnique());
            qaScalesQSize.put(qaScale.getIdentifier(), qaScale.getQScaleUnique());
            if (qaScale.getScales().size() == 0)
                counter_without_scale++;
            else if (qaScale.getScales().size() == 1)
                counter_with_1scale++;
            else
                counter_with_MoreThan1scale++;
            for (Integer scale : qaScale.getScalesInQuestion().values()) {
                scales.put(scale, scales.get(scale) + 1);
                qScales.put(scale, qScales.get(scale) + 1);
            }
            for (Map<Integer, Integer> aScale : qaScale.getScalesInAnswers()) {
                for (Integer scale : aScale.values()) {
                    scales.put(scale, scales.get(scale) + 1);
                    aScales.put(scale, aScales.get(scale) + 1);
                }
            }
        }
        LOG.info("Scale mapping...");
        try (Writer writer = new FileWriter(Config.GAZETTEERS_FOLDER+"QA-A-Scale-summary-all.json")) {
            gson.toJson(scales, writer);
        }
        try (Writer writer = new FileWriter(Config.GAZETTEERS_FOLDER+"QA-A-Scale-summary-q.json")) {
            gson.toJson(qScales, writer);
        }
        try (Writer writer = new FileWriter(Config.GAZETTEERS_FOLDER+"QA-A-Scale-summary-a.json")) {
            gson.toJson(aScales, writer);
        }
        LOG.info("without scale: " + counter_without_scale);
        LOG.info("one scale: " + counter_with_1scale);
        LOG.info("morethan1 scale: " + counter_with_MoreThan1scale);
        writeLongMapAsCSV(qaScalesSize, Config.GAZETTEERS_FOLDER+"QA-All-Scales-SIZE");
        writeLongMapAsCSV(qaScalesQSize, Config.GAZETTEERS_FOLDER+"QA-ANS-Scales-SIZE");
        writeLongMapAsCSV(qaScalesASize, Config.GAZETTEERS_FOLDER+"QA-QUE-Scales-SIZE");


        LOG.info("Finished!");
    }

    private static void populateDisambiguation(ResultModel m, Map<String, Toponym> disambiguatedOnes, Map<Long, Map<String, Map<String, Toponym>>> allDisambiguatedValues) {

        Map<String, Map<String, Toponym>> values = new HashMap<>();

        Map<String, Toponym> qMap = new HashMap<>();
        for (String pName : m.getQueryAnalyze().getPlaceName()) {
            if (disambiguatedOnes.containsKey(pName)) {

                qMap.put(pName, disambiguatedOnes.get(pName));
            }
        }
        values.put("question", qMap);

        for (int i = 0; i < m.getAnswersAnalyze().size(); i++) {
            Map<String, Toponym> aMap = new HashMap<>();
            for (String pName : m.getAnswersAnalyze().get(i).getPlaceName()) {
                if (disambiguatedOnes.containsKey(pName)) {
                    aMap.put(pName, disambiguatedOnes.get(pName));
                }
            }
            values.put("answer-" + i, aMap);
        }
        allDisambiguatedValues.put(m.getIdentifier(), values);
    }

    public static void appendStrToFile(String fileName, String str) {
        try {
            // Open given file in append mode.
            BufferedWriter out = new BufferedWriter(
                    new FileWriter(fileName, true));
            out.write(str);
            out.close();
        } catch (IOException e) {
            LOG.error("exception occurred", e);
        }
    }

    public static void doSummarize(Map<String, Set<Long>> detailed, Map<String, Integer> coarse) {
        for (String key : detailed.keySet())
            coarse.put(key, detailed.get(key).size());
    }

    public static Map<String, ToponymExportModel> toMap(List<ToponymExportModel> exports) {
        Map<String, ToponymExportModel> result = new HashMap<>();
        for (ToponymExportModel e : exports)
            result.put(e.getName(), e);
        return result;
    }


    public static void addToMap(Map<Integer, Integer> data, Integer value) {
        if (!data.containsKey(value))
            data.put(value, 0);
        data.put(value, data.get(value) + 1);

    }

    public static void addToMap(Map<String, Set<Long>> data, String value, Long id) {
        if (!data.containsKey(value))
            data.put(value, new HashSet<Long>());
        Set<Long> list = data.get(value);
        list.add(id);
        data.put(value, list);
    }

    public static void writeDistances() throws FileNotFoundException {
        PrintWriter pw = new PrintWriter(new File(Config.GAZETTEERS_FOLDER+"toponym-disambiguated-dist.csv"));
        StringBuilder sb = new StringBuilder();
        for (Double d : distances) {
            sb.append(d);
            sb.append('\n');
        }
        pw.write(sb.toString());
        pw.close();
    }

    public static void writeCoordinateToCSV(String fileAddress, Set<PlaceLocation> locations) throws FileNotFoundException {
        PrintWriter pw = new PrintWriter(new File(fileAddress));
        StringBuilder sb = new StringBuilder();
        sb.append("name");
        sb.append(',');
        sb.append("latitude");
        sb.append(',');
        sb.append("longitude");
        sb.append(',');
        sb.append("count");
        sb.append('\n');
        for (PlaceLocation pl : locations) {
            sb.append(pl.getName());
            sb.append(',');
            sb.append(pl.getLatitude());
            sb.append(',');
            sb.append(pl.getLongitude());
            sb.append(',');
            sb.append(pl.getCount());
            sb.append('\n');
        }
        pw.write(sb.toString());
        pw.close();
    }

    public static void writeSummaryAsCSV(Summaries summaries, String fileAddress) throws FileNotFoundException {
        writeSummaryModel(summaries.getAll(), fileAddress + "-all");
        writeSummaryModel(summaries.getAnswers(), fileAddress + "-as");
        writeSummaryModel(summaries.getQuestions(), fileAddress + "-qs");
    }

    public static void writeSummaryModel(SummaryModel model, String fileAddress) throws FileNotFoundException {
        writeMapAsCSV(model.getPlaceNames(), fileAddress + "-PN.csv");
        writeMapAsCSV(model.getPlaceTypes(), fileAddress + "-PT.csv");
        writeMapAsCSV(model.getQualities(), fileAddress + "-Q.csv");
        writeMapAsCSV(model.getActivities(), fileAddress + "-A.csv");
        writeMapAsCSV(model.getSituations(), fileAddress + "-S.csv");
        writeMapAsCSV(model.getSpatialRelations(), fileAddress + "-SR.csv");
    }

    public static void writeMapAsCSV(Map<String, Integer> map, String fileAddress) throws FileNotFoundException {
        PrintWriter pw = new PrintWriter(new File(fileAddress));
        StringBuilder sb = new StringBuilder();
        sb.append("Key");
        sb.append(',');
        sb.append("Value");
        sb.append('\n');
        for (String key : map.keySet()) {
            sb.append(key);
            sb.append(',');
            sb.append(map.get(key));
            sb.append('\n');
        }
        pw.write(sb.toString());
        pw.close();
    }

    public static void writeLongMapAsCSV(Map<Long, Integer> map, String fileAddress) throws FileNotFoundException {
        PrintWriter pw = new PrintWriter(new File(fileAddress));
        StringBuilder sb = new StringBuilder();
        sb.append("Key");
        sb.append(',');
        sb.append("Value");
        sb.append('\n');
        for (Long key : map.keySet()) {
            sb.append(key);
            sb.append(',');
            sb.append(map.get(key));
            sb.append('\n');
        }
        pw.write(sb.toString());
        pw.close();
    }


    public static void writeVectorSpaceAsCSV(List<String> encodes, String fileName) throws FileNotFoundException {
        PrintWriter pw = new PrintWriter(new File(fileName));
        StringBuilder sb = new StringBuilder();
        for (String encode : encodes) {
            sb.append(encode);
            sb.append(',');
            for (String mode : combinations) {
                sb.append((encode.split(mode).length - 1));
                sb.append(',');
            }
            sb.setLength(sb.length() - 1);
            sb.append('\n');

        }
        pw.write(sb.toString());
        pw.close();
    }

    public static void writeVectorSpaceAsCSV(Set<String> encodes, String fileName) throws FileNotFoundException {
        PrintWriter pw = new PrintWriter(new File(fileName));
        StringBuilder sb = new StringBuilder();
        for (String encode : encodes) {
            sb.append(encode);
            sb.append(',');
            for (String mode : combinations) {
                sb.append((encode.split(mode).length - 1));
                sb.append(',');
            }
            sb.setLength(sb.length() - 1);
            sb.append('\n');

        }
        pw.write(sb.toString());
        pw.close();
    }

    public static Map<String, Integer> entityLocation(List<String> entities, String input) {
        Map<String, Integer> locations = new HashMap<>();
        Map<String, Integer> temp = new HashMap<>();
        for (String str : entities) {
            temp.put(str, input.toLowerCase().indexOf(str.toLowerCase().trim()));
        }
        SortedSet<Integer> indexOf = new TreeSet<>();
        indexOf.addAll(temp.values());
        int counter = 0;
        for (Integer i : indexOf) {
            for (String str : temp.keySet()) {
                if (temp.get(str).equals(i)) {
                    counter++;
                    locations.put(str, counter);
                    break;
                }
            }
        }
        return locations;
    }

    private static void populateDisambiguation(Map<String, Toponym> disambiguatedOnes, Map<String, Map<String, Integer>> values, Map<String, Integer> counts) {
        for (String p : disambiguatedOnes.keySet()) {
            if (!values.containsKey(disambiguatedOnes.get(p).getFeatureCode())) {
                values.put(disambiguatedOnes.get(p).getFeatureCode(), new HashMap<String, Integer>());
                counts.put(disambiguatedOnes.get(p).getFeatureCode(), 0);
            }
            counts.put(disambiguatedOnes.get(p).getFeatureCode(), counts.get(disambiguatedOnes.get(p).getFeatureCode()) + 1);
            Map<String, Integer> current = values.get(disambiguatedOnes.get(p).getFeatureCode());
            if (!current.containsKey(p)) {
                current.put(p, 0);
            }
            current.put(p, current.get(p) + 1);
            values.put(disambiguatedOnes.get(p).getFeatureCode(), current);
        }
    }
}