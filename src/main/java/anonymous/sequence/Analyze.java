package anonymous.sequence;

import anonymous.gazetteers.geonames.model.GeoNamesType;
import anonymous.gazetteers.geonames.model.QuestionAnswerScale;
import anonymous.gazetteers.osm.model.Address;
import anonymous.gazetteers.osm.model.OSMModel;
import anonymous.Config;
import anonymous.gazetteers.osm.scale.RelationType;
import anonymous.gazetteers.osm.scale.ScaleAnalysisModel;
import anonymous.gazetteers.osm.scale.ScaleType;
import anonymous.gazetteers.osm.scale.ScatterPoint;
import anonymous.parser.model.AnswersAnalyze;
import anonymous.parser.model.QueryAnalyze;
import anonymous.parser.model.ResultModel;
import anonymous.gazetteers.geonames.model.*;
import anonymous.gazetteers.osm.model.*;
import anonymous.gazetteers.osm.scale.*;
import anonymous.parser.model.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import fr.dudie.nominatim.model.BoundingBox;

import org.geonames.Toponym;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;

public class Analyze {
    private static final Logger LOG = LoggerFactory.getLogger(Analyze.class);
    private static List<String> stopWords = new LinkedList<>();
    private static Gson GSON = new Gson();

    static {
        stopWords.add("!");
        stopWords.add("\\?");
        stopWords.add("located");
        stopWords.add("am");
        stopWords.add("is");
        stopWords.add("are");
        stopWords.add("was");
        stopWords.add("were");
        stopWords.add("have");
        stopWords.add("has");
        stopWords.add("been");
        stopWords.add("the");
        stopWords.add("with");
        stopWords.add("an");
        stopWords.add("be");
        stopWords.add("of");
        stopWords.add("a");

    }

    public static void main(String[] args) throws IOException {
        //TODO is the order correct based on the question!!!! It is a very IMPORTANT NOTE!!!
        LOG.info("Reading extraction...");
        Type resultType = new TypeToken<ArrayList<ResultModel>>() {
        }.getType();
        JsonReader reader = new JsonReader(new FileReader(Config.PARSER_2019_FILE_PATH));
        //List<ResultModel> results = GSON.fromJson(reader, resultType);

        LOG.info("Reading scales");
        JsonReader scaleReader = new JsonReader(new FileReader(Config.GAZETTEERS_FOLDER+"QA-A-Scale.json"));
        Type scaleMap = new TypeToken<HashMap<Integer, QuestionAnswerScale>>() {
        }.getType();
        Map<Integer, QuestionAnswerScale> scales = GSON.fromJson(scaleReader, scaleMap);

        JsonReader gTypeReader = new JsonReader(new FileReader(Config.GAZETTEERS_FOLDER+"detailed_geonames.json"));
        Type gType = new TypeToken<ArrayList<GeoNamesType>>() {
        }.getType();
        List<GeoNamesType> geonamesList = GSON.fromJson(gTypeReader, gType);
        Map<String, GeoNamesType> geonamesDetail = new HashMap<>();
        for (GeoNamesType gt : geonamesList)
            geonamesDetail.put(gt.getCODE(), gt);

        JsonReader disamReader = new JsonReader(new FileReader(Config.GAZETTEERS_FOLDER+"all-disambiguated-values.json"));
        Type disambType = new TypeToken<HashMap<Long, Map<String, Map<String, Toponym>>>>() {
        }.getType();
        Map<Long, Map<String, Map<String, Toponym>>> allDisambiguatedValues = GSON.fromJson(disamReader, disambType);

        Type osmType = new TypeToken<List<OSMModel>>() {
        }.getType();
        JsonReader osmReader = new JsonReader(new FileReader(Config.GAZETTEERS_FOLDER+"osm.json"));
        List<OSMModel> osmModels = GSON.fromJson(osmReader, osmType);

        Map<String, OSMModel> osmDictionary = new HashMap<>();
        for (OSMModel m : osmModels)
            osmDictionary.put(m.getName(), m);

        LOG.info("Reading results...");
        Type type = new TypeToken<ArrayList<ResultModel>>() {
        }.getType();

        List<ResultModel> tempModels = GSON.fromJson(reader, type);

        List<Long> validIds = new ArrayList<>();

        List<ResultModel> samples = new ArrayList<>();
        List<ResultModel> models = new ArrayList<>();
        Map<Long, ResultModel> filteredModels = new HashMap<>();
        for (ResultModel m : tempModels)
            if (m.getIdentifier() < 56722) {
                samples.add(m);
                if (isItToponymBasedWhere(m)) {
                    validIds.add(m.getIdentifier());
                    models.add(m);
                    filteredModels.put(m.getIdentifier(), m);
                }
            }
        tempModels = null;
        try (Writer writer = new FileWriter(Config.SEQUENCE_OUTPUT_FOLDER+"filtered-model.json")) {
            GSON.toJson(models, writer);
        }

        int counnnt = 0;
        for (ResultModel m : filteredModels.values()) {
            if (m.getAnswers() == null || (m.getAnswers().size() == 1 && m.getAnswers().get(0).startsWith("No Answer Present") )) {
                counnnt++;
            }
        }
        LOG.info("Without answers! "+ counnnt);


        int countNotFound = 0;
        int found = 0;
        Map<Integer, Integer> ambiguity = new HashMap<>();
        Map<String, Integer> values = new HashMap<>();
        Map<Integer, Integer> rank = new HashMap<>();
        Map<Integer, List<String>> rankValue = new HashMap<>();
        Map<Integer, Integer> rankResolved = new HashMap<>();
        Map<Integer, List<String>> rankValueResolved = new HashMap<>();
        Map<Long, List<OSMModel>> resolved = new HashMap<>();
        Map<Long, List<OSMModel>> allResolved = new HashMap<>();
        Set<Long> issueInIdentifier = new HashSet<>();

        Map<String, Map<Integer, Integer>> typeRankDependency = new HashMap<>();

        Map<String, Integer> generalCodeCounts = new HashMap<>();

        Map<Long, List<Map<Integer, String>>> osmRanks = new HashMap<>();
        Map<Long, List<Map<Integer, Double>>> osmImportance = new HashMap<>();
        Map<Long, List<Map<Integer, String>>> osmImportanceQualitative = new HashMap<>();

        for (Long identifier : allDisambiguatedValues.keySet()) {
            if (validIds.contains(identifier)) {
                for (String key : allDisambiguatedValues.get(identifier).keySet()) {
                    for (String name : allDisambiguatedValues.get(identifier).get(key).keySet()) {
                        boolean testCheck = false;
                        if (filteredModels.get(identifier).getQueryAnalyze().getPlaceName().contains(name))
                            testCheck = true;

                        Toponym t = allDisambiguatedValues.get(identifier).get(key).get(name);

                        issueInIdentifier.add(identifier);
                        OSMModel osmModel = osmDictionary.get(name);

                        Address best = findBest(t, osmModel.getAddresses());
                        if (best == null) {
                            LOG.debug("NOT FOUND: " + name);
                            countNotFound++;
                            if (testCheck)

                                validIds.remove(identifier);
                        } else {
                            if (!t.getFeatureCode().trim().equals("") && geonamesDetail.containsKey(t.getFeatureCode())) {
                                if (!generalCodeCounts.containsKey(geonamesDetail.get(t.getFeatureCode()).getGENERAL_CODE()))
                                    generalCodeCounts.put(geonamesDetail.get(t.getFeatureCode()).getGENERAL_CODE(), 1);
                                else
                                    generalCodeCounts.put(geonamesDetail.get(t.getFeatureCode()).getGENERAL_CODE(), generalCodeCounts.get(geonamesDetail.get(t.getFeatureCode()).getGENERAL_CODE()) + 1);

                            }

                            addToMap(ambiguity, osmModel.getAddresses().size());
                            for (Address a : osmModel.getAddresses()) {
                                addToMap(rank, a.getPlaceRank());
                                addToMap(a.getOsmType(), values);
                                addToMap(rankValue, a.getPlaceRank(), name);
                            }


                            found++;
                            if (!allResolved.containsKey(identifier))
                                allResolved.put(identifier, new ArrayList<OSMModel>());
                            List<OSMModel> allCurrent = allResolved.get(identifier);
                            List<Address> bestAddress = new ArrayList<>();
                            bestAddress.add(best);
                            OSMModel osm = new OSMModel(osmModel.getName(), new ArrayList<fr.dudie.nominatim.model.Address>());
                            osm.setAddresses(bestAddress);
                            allCurrent.add(osm);
                            allResolved.put(identifier, allCurrent);

                            if (t.getFeatureCode().trim() != "") {
                                if (!typeRankDependency.containsKey(t.getFeatureCode()))
                                    typeRankDependency.put(t.getFeatureCode(), new HashMap<Integer, Integer>());
                                if (!typeRankDependency.get(t.getFeatureCode()).containsKey(best.getPlaceRank()))
                                    typeRankDependency.get(t.getFeatureCode()).put(best.getPlaceRank(), 0);
                                typeRankDependency.get(t.getFeatureCode()).put(best.getPlaceRank(), typeRankDependency.get(t.getFeatureCode()).get(best.getPlaceRank()) + 1);
                            }
                            if (t.getFeatureCode().startsWith("PP")) {
                                addToMap(rankResolved, best.getPlaceRank());
                                addToMap(rankValueResolved, best.getPlaceRank(), name);
                                if (!resolved.containsKey(identifier))
                                    resolved.put(identifier, new ArrayList<OSMModel>());
                                List<OSMModel> current = resolved.get(identifier);
                                List<Address> addresses = new ArrayList<>();
                                addresses.add(best);
                                osmModel.setAddresses(addresses);
                                current.add(osmModel);
                                resolved.put(identifier, current);
                            }
                        }

                    }
                }
            }
            ResultModel m = filteredModels.get(identifier);
            List<OSMModel> os = allResolved.get(identifier);
            if (os != null && validIds.contains(identifier)) {
                List<Map<Integer, String>> pRank = getPlaceRank(m, os);
                List<Map<Integer, Double>> pImps = getPlaceImportance(m, os);
                if (pImps != null) {
                    osmImportance.put(identifier, pImps);
                    osmImportanceQualitative.put(identifier, getPlaceImportanceQualitative(m, os));

                }
                osmRanks.put(identifier, pRank);
            }
        }
        try (Writer writer = new FileWriter(Config.SEQUENCE_OUTPUT_FOLDER+"geonames-gcodes.json")) {
            GSON.toJson(generalCodeCounts, writer);
        }

        try (Writer writer = new FileWriter(Config.SEQUENCE_OUTPUT_FOLDER+"type-rank-dep.json")) {
            GSON.toJson(typeRankDependency, writer);
        }
        writeTheRelationMatrix("type-rank-dep", typeRankDependency);

        try (Writer writer = new FileWriter(Config.SEQUENCE_OUTPUT_FOLDER+"osm-rank.json")) {
            GSON.toJson(rank, writer);
        }
        try (Writer writer = new FileWriter(Config.SEQUENCE_OUTPUT_FOLDER+"osm-values.json")) {
            GSON.toJson(values, writer);
        }
        try (Writer writer = new FileWriter(Config.SEQUENCE_OUTPUT_FOLDER+"osm-ambiguity.json")) {
            GSON.toJson(ambiguity, writer);
        }
        try (Writer writer = new FileWriter(Config.SEQUENCE_OUTPUT_FOLDER+"osm-rank-values.json")) {
            GSON.toJson(rankValue, writer);
        }
        try (Writer writer = new FileWriter(Config.SEQUENCE_OUTPUT_FOLDER+"osm-resolved.json")) {
            GSON.toJson(resolved, writer);
        }
        try (Writer writer = new FileWriter(Config.SEQUENCE_OUTPUT_FOLDER+"osm-resolved-rank.json")) {
            GSON.toJson(rankResolved, writer);
        }
        try (Writer writer = new FileWriter(Config.SEQUENCE_OUTPUT_FOLDER+"osm-resolved-rankvalue.json")) {
            GSON.toJson(rankValueResolved, writer);
        }

        try (Writer writer = new FileWriter(Config.SEQUENCE_OUTPUT_FOLDER+"osm-prank.json")) {
            GSON.toJson(osmRanks, writer);
        }

        try (Writer writer = new FileWriter(Config.SEQUENCE_OUTPUT_FOLDER+"osm-importance.json")) {
            GSON.toJson(osmImportance, writer);
        }

        try (Writer writer = new FileWriter(Config.SEQUENCE_OUTPUT_FOLDER+"osm-importance-qualitative.json")) {
            GSON.toJson(osmImportanceQualitative, writer);
        }
        List<Double> distrQ = new ArrayList<>();
        List<Double> distrA = new ArrayList<>();
        List<Double> distr = new ArrayList<>();
        for (Long key : osmImportance.keySet()) {
            for (Map<Integer, Double> t : osmImportance.get(key)) {
                for (Integer loc : t.keySet()) {
                    if (loc == 1)
                        distrQ.add(t.get(loc));
                    else
                        distrA.add(t.get(loc));
                }
                distr.addAll(t.values());
            }
        }
        writeDistribution(distr, Config.SEQUENCE_OUTPUT_FOLDER+"impotance-distr.txt");
        writeDistribution(distrQ, Config.SEQUENCE_OUTPUT_FOLDER+"impotance-distr-Q.txt");
        writeDistribution(distrA, Config.SEQUENCE_OUTPUT_FOLDER+"impotance-distr-A.txt");

        writeTrajectory(osmRanks, Config.SEQUENCE_OUTPUT_FOLDER+"osm-prank.txt", true);
        writeTrajectory(osmImportanceQualitative, Config.SEQUENCE_OUTPUT_FOLDER+"osm-importance.txt", true);


        Map<Long, Map<Integer, String>> QATypeTrajectory = new HashMap<>();
        Map<Long, Map<Integer, String>> specialQATypeTrajectory = new HashMap<>();
        Long id = 0l, specialId = 0l;
        Map<Long, Long> spQATypeMapping = new HashMap<>();
        Map<Long, Long> QATypeMapping = new HashMap<>();

        //analyse the sequence!
        LOG.info("Analyzing the scale");
        List<ScaleAnalysisModel> scaleAnalysisModels = new ArrayList<>();
        for (QuestionAnswerScale s : scales.values()) {
            if (validIds.contains(s.getIdentifier()) && osmImportance.containsKey(s.getIdentifier())) {
                if (issueInIdentifier.contains(s.getIdentifier())) {
                    if (resolved.containsKey(s.getIdentifier()))
                        updateScale(s, filteredModels.get(s.getIdentifier()), resolved.get(s.getIdentifier()));
                }
                ScaleAnalysisModel scaleAnalysisModel = new ScaleAnalysisModel(s);
                //if (scaleAnalysisModel.getRawData().getScalesInQuestion() != null && scaleAnalysisModel.getRawData().getScalesInQuestion().size() == 1)
//                    if (scaleAnalysisModel.getRawData().getScalesInQuestion().values().contains(3)
                //|| scaleAnalysisModel.getRawData().getScalesInQuestion().values().contains(5)
//                            || scaleAnalysisModel.getRawData().getScalesInQuestion().values().contains(4)
//                            || scaleAnalysisModel.getRawData().getScalesInQuestion().values().contains(5)
//                            || scaleAnalysisModel.getRawData().getScalesInQuestion().values().contains(6)
                //|| scaleAnalysisModel.getRawData().getScalesInQuestion().values().contains(6)
//                        )//POINT QUESTIONS
                scaleAnalysisModels.add(scaleAnalysisModel);
                //TODO !!!!
                List<Map<Integer, String>> qaTypeTraj = typeTrajectory(filteredModels.get(s.getIdentifier()), allDisambiguatedValues.get(s.getIdentifier()));
                List<Map<Integer, String>> qaSpecial = typeSpecialTrajectory(filteredModels.get(s.getIdentifier()), allDisambiguatedValues.get(s.getIdentifier()));
                for (Map<Integer, String> qaTT : qaTypeTraj) {
                    id++;
                    QATypeTrajectory.put(s.getIdentifier(), qaTT);
                    QATypeMapping.put(s.getIdentifier(), s.getIdentifier());
                }
                for (Map<Integer, String> sQATT : qaSpecial) {
                    specialId++;
                    specialQATypeTrajectory.put(s.getIdentifier(), sQATT);
                    spQATypeMapping.put(s.getIdentifier(), s.getIdentifier());
                }
            }
        }

        writeTrajectory(QATypeTrajectory, "type-trajQA-");
        writeTrajectory(specialQATypeTrajectory, "type-traj-SPQA-");
        Map<RelationType, Integer> simpleAggregation = new HashMap<>();
        Map<ScaleType, Integer> qScaleType = new HashMap<>();
        Map<ScaleType, Integer> aScaleType = new HashMap<>();
        Map<String, Integer> aScaleSignatures = new HashMap<>();
        Map<String, Integer> qScaleSignatures = new HashMap<>();
        for (ScaleAnalysisModel m : scaleAnalysisModels) {
            addToMap(m.getqScaleType(), qScaleType);
            addAllToMap(m.getaScaleType(), aScaleType);
            addToMap(m.getqScaleSignature(), qScaleSignatures);
            addAllToMap(aScaleSignatures, m.getaScaleSignature());
            for (RelationType t : m.getScaleRelation()) {
                if (t.equals(RelationType.finerPoint))
                    LOG.info("FinerPoint: " + m.getRawData().getIdentifier());
                addToMap(t, simpleAggregation);
            }
        }
        LOG.info("Simple aggregation is done!");
        try (Writer writer = new FileWriter(Config.SEQUENCE_OUTPUT_FOLDER+"simple-aggregation.json")) {
            GSON.toJson(simpleAggregation, writer);
        }
        try (Writer writer = new FileWriter(Config.SEQUENCE_OUTPUT_FOLDER+"q-scale-types.json")) {
            GSON.toJson(qScaleType, writer);
        }
        try (Writer writer = new FileWriter(Config.SEQUENCE_OUTPUT_FOLDER+"a-scale-types.json")) {
            GSON.toJson(aScaleType, writer);
        }
        try (Writer writer = new FileWriter(Config.SEQUENCE_OUTPUT_FOLDER+"q-scale-signatures.json")) {
            GSON.toJson(qScaleSignatures, writer);
        }
        try (Writer writer = new FileWriter(Config.SEQUENCE_OUTPUT_FOLDER+"a-scale-signatures.json")) {
            GSON.toJson(aScaleSignatures, writer);
        }

        Map<RelationType, Integer> pairwiseAggregation = new HashMap<>();
        Map<ScaleType, Integer> qScaleTypeMultiple = new HashMap<>();
        Map<ScaleType, Integer> aScaleTypeMultiple = new HashMap<>();
        Map<String, Integer> aScaleSignaturesMultiple = new HashMap<>();
        Map<String, Integer> qScaleSignaturesMultiple = new HashMap<>();
        LOG.info("Multiple answers cross scale analysis...");
        for (ScaleAnalysisModel m : scaleAnalysisModels) {
            if (m.getaScaleSignature().size() > 1) {
                //qScale, aScale patterns in question
                addToMap(m.getqScaleType(), qScaleTypeMultiple);
                addAllToMap(m.getaScaleType(), aScaleTypeMultiple);
                addToMap(m.getqScaleSignature(), qScaleSignaturesMultiple);
                addAllToMap(aScaleSignaturesMultiple, m.getaScaleSignature());
                //pairwise comparision! relation
                for (int i = 0; i < m.getaScaleSignature().size(); i++)
                    for (int j = i + 1; j < m.getaScaleSignature().size(); j++) {
                        RelationType t = m.infer(m.getRawData().getScalesInAnswers().get(i), m.getaScaleType().get(i), m.getRawData().getScalesInAnswers().get(j), m.getaScaleType().get(j));
                        addToMap(t, pairwiseAggregation);
                    }
            }
        }

        try (Writer writer = new FileWriter(Config.SEQUENCE_OUTPUT_FOLDER+"pairwise-multiple.json")) {
            GSON.toJson(pairwiseAggregation, writer);
        }
        try (Writer writer = new FileWriter(Config.SEQUENCE_OUTPUT_FOLDER+"q-scale-types-multiple.json")) {
            GSON.toJson(qScaleTypeMultiple, writer);
        }
        try (Writer writer = new FileWriter(Config.SEQUENCE_OUTPUT_FOLDER+"a-scale-types-multiple.json")) {
            GSON.toJson(aScaleTypeMultiple, writer);
        }
        try (Writer writer = new FileWriter(Config.SEQUENCE_OUTPUT_FOLDER+"q-scale-signatures-multiple.json")) {
            GSON.toJson(qScaleSignaturesMultiple, writer);
        }
        try (Writer writer = new FileWriter(Config.SEQUENCE_OUTPUT_FOLDER+"a-scale-signatures-multiple.json")) {
            GSON.toJson(aScaleSignaturesMultiple, writer);
        }

        LOG.info("Situation-Activity questions");
        Map<ScaleType, Integer> aScaleTypeSituation = new HashMap<>();
        Map<String, Integer> aScaleSignaturesSituation = new HashMap<>();
        for (ScaleAnalysisModel m : scaleAnalysisModels) {
            if (m.getqScaleType() == ScaleType.noScale && (m.getaScaleType().contains(ScaleType.intervalScale) || m.getaScaleType().contains(ScaleType.oneScale))) {
                addAllToMap(m.getaScaleType(), aScaleTypeSituation);
                addAllToMap(aScaleSignaturesSituation, m.getaScaleSignature());
            }
        }
        try (Writer writer = new FileWriter(Config.SEQUENCE_OUTPUT_FOLDER+"a-scale-types-situation-queries.json")) {
            GSON.toJson(aScaleTypeSituation, writer);
        }
        try (Writer writer = new FileWriter(Config.SEQUENCE_OUTPUT_FOLDER+"a-scale-signatures-situation-queries.json")) {
            GSON.toJson(aScaleSignaturesSituation, writer);
        }

        LOG.info("No answer analysis"); //TODO not correct! Fix value of answer for no answer results!
        Map<ScaleType, Integer> qScaleTypeNoAnswer = new HashMap<>();
        Map<String, Integer> qScaleSignaturesNoAnswer = new HashMap<>();
        for (ScaleAnalysisModel m : scaleAnalysisModels) {
            if (m.getaScaleType().contains(ScaleType.noScale)) {
                addToMap(m.getqScaleType(), qScaleTypeNoAnswer);
                addToMap(m.getqScaleSignature(), qScaleSignaturesNoAnswer);
            }
        }
        try (Writer writer = new FileWriter(Config.SEQUENCE_OUTPUT_FOLDER+"q-scale-types-no-answer.json")) {
            GSON.toJson(qScaleTypeNoAnswer, writer);
        }
        try (Writer writer = new FileWriter(Config.SEQUENCE_OUTPUT_FOLDER+"q-scale-signatures-no-answer.json")) {
            GSON.toJson(qScaleSignaturesNoAnswer, writer);
        }

        //TODO: Change-analysis use change function!!! gradual, steep, and noChange

        //TODO: trajectory patterns analysis
        Map<Integer, Map<Long, Map<Integer, Integer>>> trajectoriesOfQuestion = new HashMap<>();
        Map<Integer, Map<Long, Map<Integer, Integer>>> trajectoriesOfAnswer = new HashMap<>();
        Map<Integer, Map<Long, Map<Integer, Integer>>> changeTrajectoriesOfQuestion = new HashMap<>();
        Map<Integer, Map<Long, Map<Integer, Integer>>> changeTrajectoriesOfAnswer = new HashMap<>();
        for (ScaleAnalysisModel m : scaleAnalysisModels) {
            addToTrajectory(trajectoriesOfQuestion, m.getRawData().getScalesInQuestion(), m.getRawData().getIdentifier());
            addToTrajectory(changeTrajectoriesOfQuestion, change(m.getRawData().getScalesInQuestion()), m.getRawData().getIdentifier());
            for (Map<Integer, Integer> aScales : m.getRawData().getScalesInAnswers()) {
                addToTrajectory(trajectoriesOfAnswer, aScales, m.getRawData().getIdentifier());
                addToTrajectory(changeTrajectoriesOfAnswer, change(aScales), m.getRawData().getIdentifier());
            }
        }
        LOG.info("Trajectories are ready!");
        LOG.info("Writing trajectories in a file");
        writeTrajectories("Q-", trajectoriesOfQuestion);
        writeTrajectories("Q-CH-", changeTrajectoriesOfQuestion);
        writeTrajectories("A-", trajectoriesOfAnswer);
        writeTrajectories("A-CH-", changeTrajectoriesOfAnswer);

        writeScatterPoints(Config.SEQUENCE_OUTPUT_FOLDER+"SCATTER.txt", toScatterPoints(scaleAnalysisModels, false));
        writeScatterPoints(Config.SEQUENCE_OUTPUT_FOLDER+"SCATTER-DIFF.txt", toScatterPoints(scaleAnalysisModels, true));
        //TODO: Do all above for three different dataset: Starting with "where", "what", and "which"! Relation between WH question and answer -- focus on where, what, and which questions

        int maxColAllA = 0, maxColAllQ = 0, maxCollQ = 0, maxCollA = 0;
        //TODO: Type-based analysis: summaries, relation
        Map<String, Integer> swQuestionTypeSummary = new HashMap<>();
        Map<String, Integer> swAnswerTypeSummary = new HashMap<>();
        Map<Long, List<String>> allATypeEncoding = new HashMap<>();
        Map<Long, List<String>> allQTypeEncoding = new HashMap<>();
        Map<Long, List<String>> aTypeEncoding = new HashMap<>();
        Map<Long, List<String>> qTypeEncoding = new HashMap<>();
        for (Long identifier : allDisambiguatedValues.keySet()) {
            if (validIds.contains(identifier)) {
                Map<String, Map<String, Toponym>> context = allDisambiguatedValues.get(identifier);
                Map<String, Toponym> qPlaces = context.get("question");
                List<String> qTypes = getTypeEncoding(qPlaces, new ArrayList<Integer>());
                //simple where question TODO!
                List<Integer> inQuestion = new ArrayList<>();
                for (Toponym t : qPlaces.values()) {
                    inQuestion.add(t.getGeoNameId());
                }
                populateTypeSummaries(qPlaces, swQuestionTypeSummary, new ArrayList<Integer>());
                List<String> aTypes = new ArrayList<>();
                for (int i = 0; i < context.size() - 1; i++) {
                    Map<String, Toponym> aPlaces = context.get("answer-" + i);
                    populateTypeSummaries(aPlaces, swAnswerTypeSummary, inQuestion);
                    aTypes.addAll(getTypeEncoding(aPlaces, inQuestion));
                    for (Toponym t : aPlaces.values())
                        inQuestion.add(t.getGeoNameId());
                }
                if (maxColAllA < aTypes.size())
                    maxColAllA = aTypes.size();
                if (maxColAllQ < qTypes.size())
                    maxColAllQ = qTypes.size();
                allATypeEncoding.put(identifier, aTypes);
                allQTypeEncoding.put(identifier, qTypes);
                if (validIds.contains(identifier) && osmImportance.containsKey(identifier)) {
                    aTypeEncoding.put(identifier, aTypes);
                    qTypes.addAll(aTypes);
                    qTypeEncoding.put(identifier, qTypes);

                    if (maxCollA < aTypes.size())
                        maxCollA = aTypes.size();
                    if (maxCollQ < qTypes.size())
                        maxCollQ = qTypes.size();
                }
            }
        }
        LOG.info("MAX AllQ, AllA, Q, A: " + maxColAllQ + "\t," + maxColAllA + "\t," + maxCollQ + "\t," + maxCollA);
        try (Writer writer = new FileWriter(Config.SEQUENCE_OUTPUT_FOLDER+"a-type-summary.json")) {
            GSON.toJson(swAnswerTypeSummary, writer);
        }
        try (Writer writer = new FileWriter(Config.SEQUENCE_OUTPUT_FOLDER+"q-type-summary.json")) {
            GSON.toJson(swQuestionTypeSummary, writer);
        }

        writeTypeEncoding("allQ-type-items", allQTypeEncoding);
        writeTypeEncoding("allA-type-items", allATypeEncoding);
        writeTypeEncoding("swQA-type-items", qTypeEncoding);
        writeTypeEncoding("swA-type-items", aTypeEncoding);
        //Maybe machine learning (clustering) can be used for categorizing
        // Value based and also change(shift) based is important!! (answer implicitly contain the information in the question is also a good idea!!!


        LOG.info("Calculating and writing type/scale/importance vs answer length...");
        Map<String, List<Integer>> importanceComplexityScatterPlot = new HashMap<>();
        Map<String, List<Integer>> importanceComplexityScatterPlotAnswer = new HashMap<>();
        for (Long key : osmImportanceQualitative.keySet()) {
            for (Map<Integer, String> val : osmImportanceQualitative.get(key)) {
                String qImp = val.get(1); //Q
                if (!importanceComplexityScatterPlot.containsKey(qImp))
                    importanceComplexityScatterPlot.put(qImp, new ArrayList<Integer>());
                List<Integer> current = importanceComplexityScatterPlot.get(qImp);
                current.add(val.size() - 1);
//                //TODO remember the change to #words
//                Integer temp = 0;
//                for (String answerString : filteredModels.get(key).getAnswers())
//                    temp += answerString.split(" ").length;
//                current.add(temp);

                importanceComplexityScatterPlot.put(qImp, current);

                for (Integer kk : val.keySet()) {
                    if (kk != 1) {
                        String aImp = val.get(kk);
                        if (!importanceComplexityScatterPlotAnswer.containsKey(aImp))
                            importanceComplexityScatterPlotAnswer.put(aImp, new ArrayList<Integer>());
                        List<Integer> aCurrent = importanceComplexityScatterPlotAnswer.get(aImp);
                        aCurrent.add(val.size() - 2);
                        importanceComplexityScatterPlotAnswer.put(aImp, aCurrent);
                    }
                }
            }
        }
        Map<String, List<Integer>> scaleComplexityScatterPlot = new HashMap<>();
        Map<String, Integer> scaleDistribution = new HashMap<>();
        Map<String, List<Integer>> scaleComplexityScatterPlotAnswer = new HashMap<>();
        Map<String, Integer> scaleDistributionAnswer = new HashMap<>();
        for (Long key : osmRanks.keySet()) {
            for (Map<Integer, String> val : osmRanks.get(key)) {
                String qImp = val.get(1); //Q
                if (!scaleComplexityScatterPlot.containsKey(qImp))
                    scaleComplexityScatterPlot.put(qImp, new ArrayList<Integer>());
                List<Integer> current = scaleComplexityScatterPlot.get(qImp);
                current.add(val.size() - 1);
//                //TODO remember the change to #words
//                Integer temp = 0;
//                for (String answerString : filteredModels.get(key).getAnswers())
//                    temp += answerString.split(" ").length;
//                current.add(temp);
                scaleComplexityScatterPlot.put(qImp, current);

                if (!scaleDistribution.containsKey(val.get(1)))
                    scaleDistribution.put(val.get(1), 0);
                scaleDistribution.put(val.get(1), scaleDistribution.get(val.get(1)) + 1);

                for (Integer kk : val.keySet()) {
                    if (kk != 1) {
                        String aImp = val.get(kk);
                        if (!scaleComplexityScatterPlotAnswer.containsKey(aImp))
                            scaleComplexityScatterPlotAnswer.put(aImp, new ArrayList<Integer>());
                        List<Integer> aCurrent = scaleComplexityScatterPlotAnswer.get(aImp);
                        aCurrent.add(val.size() - 2);
                        scaleComplexityScatterPlotAnswer.put(aImp, aCurrent);

                        if (!scaleDistributionAnswer.containsKey(val.get(kk)))
                            scaleDistributionAnswer.put(val.get(kk), 0);
                        scaleDistributionAnswer.put(val.get(kk), scaleDistributionAnswer.get(val.get(kk)) + 1);
                    }
                }
            }
        }
        writeDistribution(scaleDistribution, Config.SEQUENCE_OUTPUT_FOLDER+"osm-scale-distr-q.txt");
        writeDistribution(scaleDistributionAnswer, Config.SEQUENCE_OUTPUT_FOLDER+"osm-scale-distr-a.txt");

        Map<String, List<Integer>> typeComplexityScatterPlot = new HashMap<>();
        Map<String, List<Integer>> typeComplexityScatterPlotAnswer = new HashMap<>();
        for (Long key : specialQATypeTrajectory.keySet()) {
            for (String soT : specialQATypeTrajectory.get(key).values()) {
                if (soT.startsWith("Q-")) {

                    //TODO remember the change to #words
                    Integer temp = 0;
                    if (!typeComplexityScatterPlot.containsKey(soT))
                        typeComplexityScatterPlot.put(soT, new ArrayList<Integer>());
                    List<Integer> current = typeComplexityScatterPlot.get(soT);
                    current.add(specialQATypeTrajectory.get(key).size() - 1);
//                    for (String answerString : filteredModels.get(spQATypeMapping.get(key)).getAnswers())
//                        temp += answerString.split(" ").length;
//                    current.add(temp);
                    typeComplexityScatterPlot.put(soT, current);
                    //break;
                } else {
                    if (!typeComplexityScatterPlotAnswer.containsKey(soT))
                        typeComplexityScatterPlotAnswer.put(soT, new ArrayList<Integer>());
                    List<Integer> current = typeComplexityScatterPlotAnswer.get(soT);
                    current.add(specialQATypeTrajectory.get(key).size() - 2);
                    typeComplexityScatterPlotAnswer.put(soT, current);
                }
            }
        }

        writeComplexityScatters(importanceComplexityScatterPlot, Config.SEQUENCE_OUTPUT_FOLDER+"importance-complexity-scatter.txt");
        writeComplexityScatters(typeComplexityScatterPlot, Config.SEQUENCE_OUTPUT_FOLDER+"type-complexity-scatter.txt");
        writeComplexityScatters(scaleComplexityScatterPlot, Config.SEQUENCE_OUTPUT_FOLDER+"scale-complexity-scatter.txt");

        writeComplexityScatters(importanceComplexityScatterPlotAnswer, Config.SEQUENCE_OUTPUT_FOLDER+"importance-complexity-scatter-a.txt");
        writeComplexityScatters(typeComplexityScatterPlotAnswer, Config.SEQUENCE_OUTPUT_FOLDER+"type-complexity-scatter-a.txt");
        writeComplexityScatters(scaleComplexityScatterPlotAnswer, Config.SEQUENCE_OUTPUT_FOLDER+"scale-complexity-scatter-a.txt");
    }

    private static void writeDistribution(Map<String, Integer> distr, String fileName) throws IOException {
        File file = new File(fileName);
        StringBuilder stringBuilder = new StringBuilder();
        for (String d : distr.keySet()) {
            stringBuilder.append(d);
            stringBuilder.append(' ');
            stringBuilder.append(distr.get(d));
            stringBuilder.append('\n');
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(stringBuilder.toString());
        }
    }

    private static void writeDistribution(List<Double> distr, String fileName) throws IOException {
        File file = new File(fileName);
        StringBuilder stringBuilder = new StringBuilder();
        for (Double d : distr) {
            stringBuilder.append(d);
            stringBuilder.append('\n');
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(stringBuilder.toString());

        }
    }

    private static List<Map<Integer, String>> getPlaceImportanceQualitative(ResultModel m, List<OSMModel> models) {
        List<Map<Integer, String>> finalResult = new ArrayList<>();
        Map<Integer, String> qu = new HashMap<>();
        List<Map<Integer, String>> anL = new ArrayList<>();
        OSMModel qModel = null;
        boolean isCorrect = false;
        for (OSMModel osmModel : models) {
            Map<String, Integer> locations = anonymous.gazetteers.geonames.Analyze.entityLocation(m.getQueryAnalyze().getPlaceName(), m.getQueryAnalyze().getSentence());

            if (locations.containsKey(osmModel.getName())) {
                qu.put(locations.get(osmModel.getName()), "Q-" + importanceTransformation(osmModel.getAddresses().get(0).getImportance()));
                qModel = osmModel;
                isCorrect = true;
                //break;
            }
        }
        if (!isCorrect)
            return null;
        for (AnswersAnalyze a : m.getAnswersAnalyze()) {
            Map<Integer, String> an = new HashMap<>();
            Map<String, Integer> aLocations = anonymous.gazetteers.geonames.Analyze.entityLocation(a.getPlaceName(), a.getSentence());
            for (OSMModel osmModel : models) {
                if (qModel == null || !osmModel.getName().equals(qModel.getName())) {
                    if (aLocations.containsKey(osmModel.getName())) {
                        an.put(aLocations.get(osmModel.getName()), "A-" + importanceTransformation(osmModel.getAddresses().get(0).getImportance()));
                    }
                }
            }
            anL.add(an);
        }

        for (Map<Integer, String> an : anL) {
            int count = 1;
            Map<Integer, String> result = new HashMap<>();

            SortedSet<Integer> qkeys = new TreeSet<>();
            qkeys.addAll(qu.keySet());

            for (Integer k : qkeys) {
                result.put(count, qu.get(k));
                count++;
            }

//            result.put(1, qu.get(1));
            SortedSet<Integer> key = new TreeSet<>();
            key.addAll(an.keySet());

            for (Integer k : key) {
                result.put(count, an.get(k));
                count++;
            }
            finalResult.add(result);

        }
        return finalResult;
    }


    private static List<Map<Integer, Double>> getPlaceImportance(ResultModel m, List<OSMModel> models) {
        List<Map<Integer, Double>> finalResult = new ArrayList<>();
        Map<Integer, Double> qu = new HashMap<>();
        List<Map<Integer, Double>> anL = new ArrayList<>();
        OSMModel qModel = null;
        Set<String> names = new HashSet<>();
        boolean isCorrect = false;
        for (OSMModel osmModel : models) {
            Map<String, Integer> locations = anonymous.gazetteers.geonames.Analyze.entityLocation(m.getQueryAnalyze().getPlaceName(), m.getQueryAnalyze().getSentence());

            if (locations.containsKey(osmModel.getName())) {
                qu.put(locations.get(osmModel.getName()), osmModel.getAddresses().get(0).getImportance());
                qModel = osmModel;
                isCorrect = true;
                names.add(osmModel.getName());
                //break;
            }
        }
        if (!isCorrect)
            return null;
        for (AnswersAnalyze a : m.getAnswersAnalyze()) {
            Map<Integer, Double> an = new HashMap<>();
            Map<String, Integer> aLocations = anonymous.gazetteers.geonames.Analyze.entityLocation(a.getPlaceName(), a.getSentence());
            for (OSMModel osmModel : models) {
                if (qModel == null || !names.contains(osmModel.getName())) {
                    if (aLocations.containsKey(osmModel.getName())) {
                        an.put(aLocations.get(osmModel.getName()), osmModel.getAddresses().get(0).getImportance());
                    }
                }
            }
            anL.add(an);
        }

        for (Map<Integer, Double> an : anL) {
            int count = 1;
            Map<Integer, Double> result = new HashMap<>();
            SortedSet<Integer> qkey = new TreeSet<>();
            qkey.addAll(qu.keySet());

            for (Integer k : qkey) {
                result.put(count, qu.get(k));
                count++;
            }
//            result.put(1, qu.get(1));
            SortedSet<Integer> key = new TreeSet<>();
            key.addAll(an.keySet());

            for (Integer k : key) {
                result.put(count, an.get(k));
                count++;
            }
            finalResult.add(result);

        }
        return finalResult;
    }

    private static void writeComplexityScatters(Map<String, List<Integer>> data, String fileName) throws IOException {
        File file = new File(fileName);
        StringBuilder stringBuilder = new StringBuilder();
        int id = 0;
        for (String key : data.keySet()) {
            id++;
            for (Integer complexity : data.get(key)) {
                stringBuilder.append(id);
                stringBuilder.append(' ');
                stringBuilder.append(key);
                stringBuilder.append(' ');
                stringBuilder.append(complexity);
                stringBuilder.append('\n');
            }
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(stringBuilder.toString());
        }
    }

    private static void writeTrajectory(Map<Long, List<Map<Integer, String>>> osmRanks, String s, boolean all) throws IOException {
        File file = new File(s);
        Integer idCounter = 0;
        StringBuilder stringBuilder = new StringBuilder();

        for (Long identifier : osmRanks.keySet()) {
            List<Map<Integer, String>> ranks = osmRanks.get(identifier);
            for (Map<Integer, String> rs : ranks) {
                if (rs.values().contains("Q--1") || rs.values().contains(null))
                    break;
                idCounter++;
                stringBuilder.append(identifier);
                stringBuilder.append(' ');
                //stringBuilder.append(identifier);
                //stringBuilder.append(' ');
                SortedSet<Integer> locations = new TreeSet<>();
                locations.addAll(rs.keySet());
                for (Integer l : locations) {
                    if (rs.get(l) != null && !rs.get(l).contains("--1")) { //ONLY WHEN YOU ARE SURE!
                        stringBuilder.append(rs.get(l));
                        stringBuilder.append(' ');
                    }
                }
                stringBuilder.append('\n');
            }
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(stringBuilder.toString());
        }

    }

    private static void populateTypeSummaries(Map<String, Toponym> values, Map<String, Integer> map, List<Integer> notValidID) {
        for (Toponym t : values.values()) {
            if (!notValidID.contains(t.getGeoNameId()) && t.getFeatureCode() != null && !t.getFeatureCode().equals("")) {
                if (!map.containsKey(t.getFeatureCode()))
                    map.put(t.getFeatureCode(), 0);
                map.put(t.getFeatureCode(), map.get(t.getFeatureCode()) + 1);
            }
        }
    }

    private static List<String> getTypeEncoding(Map<String, Toponym> values, List<Integer> notValidID) {
        List<String> str = new LinkedList<String>();
        for (Toponym t : values.values()) {
            if (!notValidID.contains(t.getGeoNameId())) {
                if (t.getFeatureCode() != null && !t.getFeatureCode().trim().equals("")) {
                    str.add(t.getFeatureCode());
                }
            }
        }
        return str;
    }

    private static void addToMap(RelationType t, Map<RelationType, Integer> data) {
        if (!data.containsKey(t))
            data.put(t, 0);
        data.put(t, data.get(t) + 1);
    }

    private static void addToMap(ScaleType t, Map<ScaleType, Integer> data) {
        if (!data.containsKey(t))
            data.put(t, 0);
        data.put(t, data.get(t) + 1);
    }

    private static void addAllToMap(List<ScaleType> t, Map<ScaleType, Integer> data) {
        Set<ScaleType> set = new HashSet<>();
        set.addAll(t);
        for (ScaleType s : set) {
            if (!data.containsKey(s))
                data.put(s, 0);
            data.put(s, data.get(s) + 1);
        }
    }

    private static void addToMap(String t, Map<String, Integer> data) {
        if (!data.containsKey(t))
            data.put(t, 0);
        data.put(t, data.get(t) + 1);
    }

    private static void addAllToMap(Map<String, Integer> data, List<String> t) {
        Set<String> set = new HashSet<>();
        set.addAll(t);
        for (String s : set) {
            if (!data.containsKey(s))
                data.put(s, 0);
            data.put(s, data.get(s) + 1);
        }
    }

    private static void addToTrajectory(Map<Integer, Map<Long, Map<Integer, Integer>>> map, Map<Integer, Integer> value, Long identifier) {
        if (!map.containsKey(value.size()))
            map.put(value.size(), new HashMap<Long, Map<Integer, Integer>>());
        Map<Long, Map<Integer, Integer>> tempArr = map.get(value.size());
        tempArr.put(identifier, value);
        map.put(value.size(), tempArr);

        //mapping.put(value.size(),)
    }

    private static Map<Integer, Integer> change(Map<Integer, Integer> input) {
        if (input.size() <= 1)
            return new HashMap<>();
        Map<Integer, Integer> changes = new HashMap<>();
        SortedSet<Integer> ints = new TreeSet<>();
        ints.addAll(input.keySet());
        Iterator<Integer> iterator = ints.iterator();
        Integer first = iterator.next();
        while (iterator.hasNext()) {
            Integer next = iterator.next();
            changes.put(first, input.get(next) - input.get(first));
            first = next;
        }
        return changes;
    }

    public static void writeTypeEncoding(String fileName, Map<Long, List<String>> typeEncoding) throws IOException {
        File file = new File(Config.GAZETTEERS_FOLDER + fileName + ".txt");

        SortedSet<String> allEncodingTypes = new TreeSet<>();
        for (List<String> str : typeEncoding.values())
            allEncodingTypes.addAll(str);


        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("ID");
        stringBuilder.append(' ');
        for (String key : allEncodingTypes) {
            stringBuilder.append(key);
            stringBuilder.append(' ');
        }
        stringBuilder.append('\n');
        for (Long id : typeEncoding.keySet()) {
            List<String> val = typeEncoding.get(id);
            stringBuilder.append(id);
            stringBuilder.append(' ');
            for (String key : allEncodingTypes) {
                if (val.contains(key)) {
                    int count = 0;
                    for (String v : val) {
                        if (v.equals(key))
                            count++;
                    }
                    stringBuilder.append(count);
                    stringBuilder.append(' ');
                } else {
                    stringBuilder.append(0);
                    stringBuilder.append(' ');
                }
            }
//            stringBuilder.append(id);
//            stringBuilder.append(' ');
//            for (String str : typeEncoding.get(id)) {
//                stringBuilder.append(str);
//                stringBuilder.append(' ');
//            }
            stringBuilder.append('\n');
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(stringBuilder.toString());
        }
    }

    private static void writeTrajectory(String fileName, Map<Long, Map<Integer, Integer>> trajectories) throws IOException {
        File file = new File(Config.SEQUENCE_OUTPUT_FOLDER + fileName + "-data.txt");
        File fileTime = new File(Config.SEQUENCE_OUTPUT_FOLDER + fileName + "-time.txt");
        File fileMapping = new File(Config.SEQUENCE_OUTPUT_FOLDER + fileName + "-mapping.txt");
        Integer idCounter = 0;
        StringBuilder stringBuilder = new StringBuilder();
        StringBuilder stringBuilderTime = new StringBuilder();
        StringBuilder stringBuilderMapping = new StringBuilder();
        for (Long identifier : trajectories.keySet()) {
            idCounter++;
            stringBuilderMapping.append(idCounter);
            stringBuilderMapping.append(' ');
            stringBuilderMapping.append(identifier);
            stringBuilder.append(idCounter);
            stringBuilderTime.append(idCounter);
            SortedSet<Integer> ints = new TreeSet<>();
            Map<Integer, Integer> temp = null;
            if (trajectories.get(identifier).values().contains(0)) {
                temp = new HashMap<>();
                Integer tempoKey = 1;
                for (Integer tempo : temp.values()) {
                    if (tempo != 0) {
                        temp.put(tempoKey, tempo);
                        tempoKey++;
                    }
                }
            }
            if (temp == null)
                temp = trajectories.get(identifier);
            ints.addAll(temp.keySet());
            int k = 1;
            for (Integer i : ints) {
                stringBuilderTime.append(' ');
                stringBuilderTime.append(k);
                k++;
                stringBuilder.append(' ');
                stringBuilder.append(temp.get(i));
            }
            stringBuilder.append('\n');
            stringBuilderTime.append('\n');
            stringBuilderMapping.append('\n');
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(stringBuilder.toString());
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileTime))) {
            writer.write(stringBuilderTime.toString());
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileMapping))) {
            writer.write(stringBuilderMapping.toString());
        }
    }

    private static void writeTrajectories(Map<Integer, Map<Long, Map<Integer, String>>> trajectories, String baseName) throws IOException {
        Map<Long, Map<Integer, String>> all = new HashMap<>();
        for (Integer key : trajectories.keySet()) {
            writeTrajectory(trajectories.get(key), baseName + "-" + key);
            all.putAll(trajectories.get(key));
        }
        writeTrajectory(all, baseName + "-all");
    }


    private static void writeTrajectory(Map<Long, Map<Integer, String>> trajectories, String fileName) throws IOException {
        File file = new File(Config.SEQUENCE_OUTPUT_FOLDER + fileName + "-data.txt");
        File fileTime = new File(Config.SEQUENCE_OUTPUT_FOLDER+ fileName + "-time.txt");
        File fileMapping = new File(Config.SEQUENCE_OUTPUT_FOLDER+ fileName + "-mapping.txt");
        Integer idCounter = 0;
        StringBuilder stringBuilder = new StringBuilder();
        StringBuilder stringBuilderTime = new StringBuilder();
        StringBuilder stringBuilderMapping = new StringBuilder();
        for (Long identifier : trajectories.keySet()) {
            idCounter++;
            stringBuilderMapping.append(idCounter);
            stringBuilderMapping.append(' ');
            stringBuilderMapping.append(identifier);
            stringBuilder.append(identifier);
            stringBuilderTime.append(idCounter);
            SortedSet<Integer> ints = new TreeSet<>();
            Map<Integer, String> temp = null;
            if (trajectories.get(identifier).values().contains(0)) {
                temp = new HashMap<>();
                Integer tempoKey = 1;
                for (String tempo : temp.values()) {
                    if (tempo != "")
                        tempo = "NN";
                    temp.put(tempoKey, tempo);
                    tempoKey++;
                }
            }
            if (temp == null)
                temp = trajectories.get(identifier);
            ints.addAll(temp.keySet());
            int k = 1;
            for (Integer i : ints) {
                stringBuilderTime.append(' ');
                stringBuilderTime.append(k);
                k++;
                stringBuilder.append(' ');
                stringBuilder.append(temp.get(i));
            }
            stringBuilder.append('\n');
            stringBuilderTime.append('\n');
            stringBuilderMapping.append('\n');
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(stringBuilder.toString());
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileTime))) {
            writer.write(stringBuilderTime.toString());
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileMapping))) {
            writer.write(stringBuilderMapping.toString());
        }
    }

    private static void writeTrajectories(String baseName, Map<Integer, Map<Long, Map<Integer, Integer>>> trajectories) throws IOException {
        Map<Long, Map<Integer, Integer>> all = new HashMap<>();
        for (Integer key : trajectories.keySet()) {
            writeTrajectory(baseName + "-" + key, trajectories.get(key));
            all.putAll(trajectories.get(key));
        }
        writeTrajectory(baseName + "-all", all);
    }

    private static void writeScatterPoints(String fileName, Map<Integer, ScatterPoint> pointMap) throws IOException {
        StringBuilder builder = new StringBuilder();
        File file = new File(fileName);
        for (ScatterPoint p : pointMap.values()) {
            builder.append(p.getX());
            builder.append(' ');
            builder.append(p.getY());
            builder.append('\n');
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(builder.toString());
        }
    }

    private static Map<Integer, ScatterPoint> toScatterPoints(List<ScaleAnalysisModel> models, boolean difference) {
        Map<Integer, ScatterPoint> points = new HashMap<>();
        for (ScaleAnalysisModel m : models) {
            QuestionAnswerScale rawData = m.getRawData();
            if (rawData.getScalesInQuestion().size() == 1) {
                Integer qScale = rawData.getScalesInQuestion().values().iterator().next();
                for (Map<Integer, Integer> aScales : rawData.getScalesInAnswers()) {
                    SortedSet<Integer> keys = new TreeSet<>();
                    keys.addAll(aScales.keySet());
                    for (Integer key : keys) {
                        int x = aScales.get(key);
                        if (difference)
                            x = x - qScale;
//                        ScatterPoint tempScatter = new ScatterPoint();
//                        tempScatter.setX(x);
//                        tempScatter.setY(1);
                        if (!points.containsKey(x)) {
                            ScatterPoint tempScatter = new ScatterPoint();
                            tempScatter.setX(x);
                            tempScatter.setY(1);
                            points.put(x, tempScatter);
                        } else {
                            ScatterPoint tempScatter = points.get(x);
                            tempScatter.setY(tempScatter.getY() + 1);
                            points.put(x, tempScatter);
                        }
                    }
                }
            }
        }
        return points;
    }

    private static ResultModel findByIdentifier(List<ResultModel> models, Long identifier) {
        for (ResultModel m : models) {
            if (m.getIdentifier().equals(identifier))
                return m;
        }
        return null;
    }

    public static boolean isItToponymBasedWhere(ResultModel model) {
        if (model == null) {
            LOG.info("ERROR!");
            return false;
        }
        if ((model.getQuery().startsWith("where") || model.getQuery().startsWith("Where")) &&
                !model.getQueryAnalyze().getCode().matches("t") && !model.getQueryAnalyze().getCode().matches("s") && !model.getQueryAnalyze().getCode().matches("q") &&
                !model.getQueryAnalyze().getCode().matches("o") &&
                model.getAnswersAnalyze().size() == 1 && !model.getAnswersAnalyze().get(0).getCode().equals("oo")) {
            //TODO!!!
            String query = model.getQuery();
            query = query.replaceAll("where", "");
            query = query.replaceAll("Where", "");
            query = query.trim();
            if (!model.getQueryAnalyze().getCode().contains("t") && !model.getQueryAnalyze().getCode().contains("a") && !model.getQueryAnalyze().getCode().contains("s") && !model.getQueryAnalyze().getCode().contains("q") && !model.getQueryAnalyze().getCode().contains("2") && !model.getQueryAnalyze().getCode().contains("o"))
                if (model.getAnswersAnalyze().size() > 0 && model.getAnswersAnalyze().get(0).getCode().contains("n")) //&& !model.getAnswersAnalyze().get(0).getCode().contains("t") && !model.getAnswersAnalyze().get(0).getCode().contains("q"))
                    return true;
            else
                LOG.debug("QUERY: " + query + " --- " + model.getIdentifier() + " --- " + model.getQuery());
        }
        return false;
    }

    public static void addToMap(Map<Integer, Integer> data, Integer value) {
        if (!data.containsKey(value))
            data.put(value, 0);
        data.put(value, data.get(value) + 1);

    }

    public static void addToMap(Map<Integer, List<String>> data, Integer rank, String str) {
        if (!data.containsKey(rank))
            data.put(rank, new ArrayList<String>());
        List<String> current = data.get(rank);
        current.add(str);
        data.put(rank, current);

    }

    public static Address findBest(Toponym geoNames, List<Address> addresses) {
        if (addresses.size() == 0) {
            LOG.debug("SIZE = 0 -- NotResolved: " + geoNames.getName());
            return null;
        }
        if (addresses.size() == 1) {
            LOG.debug("SIZE = 1 -- NotAmbiguous: " + geoNames.getName());
            return addresses.get(0);
        }
        Map<Integer, Address> containPoint = new HashMap<>();
        Map<Integer, Double> centroidDistance = new HashMap<>();
        for (int i = 0; i < addresses.size(); i++) {
            Address addr = addresses.get(i);
            if (inside(addr.getBoundingBox(), geoNames.getLatitude(), geoNames.getLongitude()))
                containPoint.put(i, addr);
            centroidDistance.put(i, distance(geoNames.getLatitude(), geoNames.getLongitude(), addr.getLatitude(), addr.getLongitude(), addr.getImportance()));
        }
        if (containPoint.size() == 1) {
            LOG.debug("SIZE = " + addresses.size() + " -- RESOLVED BY CONTAIN: " + geoNames.getName());
            for (Address addr : containPoint.values())
                return addr;
        }
        if (containPoint.size() > 1) {
            double importance = 0d;
            Address best = null;
            for (Integer id : containPoint.keySet()) {
                if (addresses.get(id).getImportance() > importance) {
                    importance = addresses.get(id).getImportance();
                    best = addresses.get(id);
                }
            }
            LOG.debug("SIZE = " + addresses.size() + " -- RESOLVED BY MIN_D_CONTAIN: " + geoNames.getName());
            return best;
        }
        double minDistance = 10000000d;
        Address best = null;
        for (Integer id : centroidDistance.keySet()) {
            if (centroidDistance.get(id) < minDistance) {
                minDistance = centroidDistance.get(id);
                best = addresses.get(id);
            }
        }
        if (minDistance > 70) {
            return null;
        }

        LOG.debug("SIZE = " + addresses.size() + " -- RESOLVED BY MIN DISTANCE: " + geoNames.getName());
        return best;
    }

    private static boolean inside(BoundingBox b, Double lat, Double lng) {
        if (b.getEast() >= lng && b.getWest() <= lng && b.getNorth() >= lat && b.getSouth() <= lat)
            return true;
        return false;
    }

    private static Double distance(Double lat1, Double lon1, Double lat2, Double lon2, Double importance) {
        if ((lat1 == lat2) && (lon1 == lon2)) {
            return 0d;
        }
        else {
            double theta = lon1 - lon2;
            double dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(theta));
            dist = Math.acos(dist);
            dist = Math.toDegrees(dist);
            dist = dist * 60 * 1.1515;
            dist = dist * 1.609344;

            return (dist/Math.pow(importance, 2));
        }
    }

    private static void updateScale(QuestionAnswerScale s, ResultModel m, List<OSMModel> models) {
        for (OSMModel osmModel : models) {

            Map<String, Integer> locations = anonymous.gazetteers.geonames.Analyze.entityLocation(m.getQueryAnalyze().getPlaceName(), m.getQueryAnalyze().getSentence());
            if (locations.containsKey(osmModel.getName())) {
                Map<Integer, Integer> sq = s.getScalesInQuestion();
                sq.put(locations.get(osmModel.getName()), mapPPLScale(osmModel));
                s.setScalesInQuestion(sq);
            }

            for (AnswersAnalyze a : m.getAnswersAnalyze()) {
                Map<String, Integer> aLocations = anonymous.gazetteers.geonames.Analyze.entityLocation(a.getPlaceName(), a.getSentence());
                if (aLocations.containsKey(osmModel.getName())) {
                    for (int i = 0; i < s.getScalesInAnswers().size(); i++) {
                        Map<Integer, Integer> sa = s.getScalesInAnswers().get(i);
                        if (sa.size() == aLocations.size()) {
                            sa.put(aLocations.get(osmModel.getName()), mapPPLScale(osmModel));
                            LinkedList<Map<Integer, Integer>> current = s.getScalesInAnswers();
                            current.set(i, sa);
                            s.setScalesInAnswers(current);
                            break;
                        }
                    }
                }
            }

            //TODO in one or multiple answer
            //TODO find location -- if 1 not check!
            //TODO correct scale at the location
        }
    }

    private static List<Map<Integer, String>> getPlaceRank(ResultModel m, List<OSMModel> models) {//TODO only for simple where! (ALSO NOT USED USE IT!!)
        List<Map<Integer, String>> finalResult = new ArrayList<>();
        Map<Integer, String> qu = new HashMap<>();
        List<Map<Integer, String>> anL = new ArrayList<>();
        OSMModel qModel = null;
        Set<String> names = new HashSet<>();
        for (OSMModel osmModel : models) {
            Map<String, Integer> locations = anonymous.gazetteers.geonames.Analyze.entityLocation(m.getQueryAnalyze().getPlaceName(), m.getQueryAnalyze().getSentence());
            if (locations.containsKey(osmModel.getName())) {
                qu.put(locations.get(osmModel.getName()), "Q-" + rankTransformation(osmModel.getAddresses().get(0).getPlaceRank()));
                qModel = osmModel;
                names.add(osmModel.getName());
                //break;
            }
        }
        for (AnswersAnalyze a : m.getAnswersAnalyze()) {
            Map<Integer, String> an = new HashMap<>();
            Map<String, Integer> aLocations = anonymous.gazetteers.geonames.Analyze.entityLocation(a.getPlaceName(), a.getSentence());
            for (OSMModel osmModel : models) {
                if (qModel == null || !names.contains(osmModel.getName())) {
                    if (aLocations.containsKey(osmModel.getName())) {
                        an.put(aLocations.get(osmModel.getName()), "A-" + rankTransformation(osmModel.getAddresses().get(0).getPlaceRank()));
                    }
                }
            }
            anL.add(an);
        }

        for (Map<Integer, String> an : anL) {
            Map<Integer, String> result = new HashMap<>();
            int count = 1;
            SortedSet<Integer> qkey = new TreeSet<>();
            qkey.addAll(qu.keySet());

            for (Integer k : qkey) {
                result.put(count, qu.get(k));
                count++;
            }

            SortedSet<Integer> key = new TreeSet<>();
            key.addAll(an.keySet());

            for (Integer k : key) {
                result.put(count, an.get(k));
                count++;
            }
            finalResult.add(result);

        }
        return finalResult;
    }


    private static int mapPPLScale(OSMModel osmModel) {
        if (osmModel.getAddresses().get(0).getPlaceRank() <= 10)
            return 8;
        else if (osmModel.getAddresses().get(0).getPlaceRank() <= 15)
            return 7;
        else if (osmModel.getAddresses().get(0).getPlaceRank() <= 19)
            return 6;
        return 0;
    }

    private static void writeTheRelationMatrix(String fileName, Map<String, Map<Integer, Integer>> deps) throws IOException {
        File file = new File(Config.SEQUENCE_OUTPUT_FOLDER + fileName + ".txt");
        SortedSet<String> types = new TreeSet<>();
        SortedSet<Integer> ranks = new TreeSet<>();
        types.addAll(deps.keySet());
        for (Map<Integer, Integer> v : deps.values())
            ranks.addAll(v.keySet());

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append('-');
        stringBuilder.append(' ');
        for (Integer rank : ranks) {
            stringBuilder.append(rank);
            stringBuilder.append(' ');
        }
        stringBuilder.append('\n');
        for (String type : deps.keySet()) {
            stringBuilder.append(type);
            stringBuilder.append(' ');
            for (Integer rank : ranks) {
                if (deps.get(type).containsKey(rank))
                    stringBuilder.append(deps.get(type).get(rank));
                else
                    stringBuilder.append(0);
                stringBuilder.append(' ');
            }
            stringBuilder.append('\n');
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(stringBuilder.toString());
        }
    }

    public static List<Map<Integer, String>> typeTrajectory(ResultModel m, Map<String, Map<String, Toponym>> ambg) {
        List<Map<Integer, String>> result = new ArrayList<>();
        Map<String, Toponym> ambgs = new HashMap<>();
        if (ambg != null) {
            for (Map<String, Toponym> amb : ambg.values())
                ambgs.putAll(amb);
            for (AnswersAnalyze a : m.getAnswersAnalyze()) {
                String st = m.getQueryAnalyze().getSentence() + " " + a.getSentence();
                List<String> pNS = new ArrayList<>();
                pNS.addAll(m.getQueryAnalyze().getPlaceName());
                pNS.addAll(a.getPlaceName());
                Map<String, Integer> locations = anonymous.gazetteers.geonames.Analyze.entityLocation(pNS, st);
                SortedSet<Integer> keyset = new TreeSet<>();
                keyset.addAll(locations.values());
                int count = 0;
                Map<Integer, String> lPValues = new HashMap<>();
                for (Integer i : keyset) {
                    for (String p : locations.keySet()) {
                        if (locations.get(p) == i) {
                            lPValues.put(count, ambgs.get(p).getFeatureCode());
                            count++;
                        }
                    }
                }
                result.add(lPValues);
            }
        }
        return result;
    }

    public static List<Map<Integer, String>> typeSpecialTrajectory(ResultModel m, Map<String, Map<String, Toponym>> ambg) {
        List<Map<Integer, String>> result = new ArrayList<>();
        if (ambg != null) {
            Map<String, Toponym> ambgs = new HashMap<>();
            for (Map<String, Toponym> amb : ambg.values())
                ambgs.putAll(amb);

            int count = 0;
            Map<Integer, String> qPValues = new HashMap<>();
            QueryAnalyze q = m.getQueryAnalyze();
            String qst = q.getSentence();
            List<String> qpNS = new ArrayList<>();
            //pNS.addAll(m.getQueryAnalyze().getPlaceName());
            qpNS.addAll(q.getPlaceName());
            Map<String, Integer> qlocations = anonymous.gazetteers.geonames.Analyze.entityLocation(qpNS, qst);
            SortedSet<Integer> qkeyset = new TreeSet<>();
            qkeyset.addAll(qlocations.values());

            for (Integer i : qkeyset) {
                for (String p : qlocations.keySet()) {
                    if (qlocations.get(p) == i) {
                        qPValues.put(count, "Q-" + ambgs.get(p).getFeatureCode());
                        count++;
                    }
                }
            }
            for (AnswersAnalyze a : m.getAnswersAnalyze()) {
                String st = a.getSentence();
                List<String> pNS = new ArrayList<>();
                pNS.addAll(a.getPlaceName());
                pNS.removeAll(qpNS);
                Map<String, Integer> locations = anonymous.gazetteers.geonames.Analyze.entityLocation(pNS, st);
                SortedSet<Integer> keyset = new TreeSet<>();
                keyset.addAll(locations.values());

                Map<Integer, String> lPValues = new HashMap<>();
                for (Integer i : keyset) {
                    for (String p : locations.keySet()) {
                        if (locations.get(p) == i) {
                            lPValues.put(count, ambgs.get(p).getFeatureCode());
                            count++;
                        }
                    }
                }
                lPValues.putAll(qPValues);
                result.add(lPValues);
            }
        }
        return result;
    }

    private static Integer importanceTransformation(Double importance) {
        if (importance <= 0.2767999) {
            return 1;
        } else if (importance <= 0.4302333) {
            return 2;
        } else if (importance <= 0.5400298) {
            return 3;
        } else if (importance <= 0.6421170) {
            return 4;
        } else if (importance <= 0.7651070) {
            return 5;
        } else if (importance <= 0.8887710) {
            return 6;
        }
        return 7;
    }

    private static Integer rankTransformation(Integer rank) {
        if (rank == 2)
            return 10;
        else if (rank == 4)
            return 9;
        else if (rank == 8)
            return 8;
        else if (rank == 10)
            return 8;
        else if (rank == 12)
            return 7;
        else if (rank == 16)
            return 6;
        else if (rank == 17)
            return 6;
        else if (rank == 18)
            return 5;
        else if (rank == 20)
            return 5;
        else if (rank == 22)
            return 4;
        else if (rank == 26)
            return 4;
        else if (rank == 27)
            return 3;
        else if (rank == 28)
            return 3;
        else
            return -1;
    }
}
