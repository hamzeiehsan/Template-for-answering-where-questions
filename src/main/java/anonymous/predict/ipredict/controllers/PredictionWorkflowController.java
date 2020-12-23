package anonymous.predict.ipredict.controllers;

import anonymous.predict.ipredict.database.Sequence;
import anonymous.predict.ipredict.helpers.Algo;
import anonymous.predict.ipredict.helpers.StatsLogger;
import anonymous.predict.ipredict.predictor.CPT.CPT.CPTPredictor;
import anonymous.predict.ipredict.predictor.CPT.CPTPlus.CPTPlusPredictor;
import anonymous.predict.ipredict.predictor.DG.DGPredictor;
import anonymous.predict.ipredict.predictor.Evaluator;
import anonymous.predict.ipredict.predictor.LZ78.LZ78Predictor;
import anonymous.predict.ipredict.predictor.Markov.MarkovAllKPredictor;
import anonymous.predict.ipredict.predictor.Markov.MarkovFirstOrderPredictor;
import anonymous.predict.ipredict.predictor.Predictor;
import anonymous.predict.ipredict.predictor.TDAG.TDAGPredictor;
import anonymous.predict.methods.baselines.MostFrequentPatternPredictor;
import anonymous.predict.methods.baselines.RandomPredictor;
import anonymous.predict.scenario.Scenario;
import anonymous.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * This controller demonstrates how to compare all the predictors.
 * The results are expressed with various performance measures:
 * <p>
 * Success: is the ratio of successful predictions against the number of
 * wrong predictions, it is defined as (Number of success) / (Number of success +
 * number of failure)
 * <p>
 * Failure: is the inverse of the local accuracy: 1 - (Success)
 * <p>
 * No Match: is the ratio of unsuccessful prediction against the total num-
 * ber of tested sequences: (Number of sequence without prediction) / (number of
 * sequence)
 * <p>
 * Too Small: is the ratio of sequences too small to be used in the experimentation, it counts
 * any sequence with a length smaller than the parameter consequentSize.
 * <p>
 * Overall: is our main measure to evaluates the accuracy of a given
 * predictor. It is the number of successful prediction against the total number of
 * tested sequences. (Number of success) / ( number of sequence)
 */
public class PredictionWorkflowController {
    private static final Logger LOG = LoggerFactory.getLogger(PredictionWorkflowController.class);

    //edited!
    public static Map<Integer, Integer> dynamicWindowsSize = new HashMap<>();
    public static Integer numberOfGuesses = 1;
    public static List<Map<String, Map<Integer, List<Sequence>>>> testPredictions = new ArrayList<>();
    public static Integer testCase = 4943;
    public static Scenario currentScenario = null;
    public static Integer scenarioID = 0;

    public static void main(String[] args) throws IOException {
        //if (args.length < 1) {
        //	System.out.println("Missing required argument with data directory.");
        //	System.exit(1);
        //}

        args = new String[2];
        args[0] = "sequences";
        args[1] = "prediction";

        //TODO: initiate all scenarios and start configuration and prediction...
        List<Scenario>  scenarios = Scenario.generateAllScenarios();
        LOG.info("All prediction scenarios are generated, #scenario: " + scenarios.size());

        for (Scenario s : scenarios) {
            scenarioID++;
            LOG.info("\n+++++++++++++New Prediction Scenario+++++++++++++");
            PredictionWorkflowController.currentScenario = s;
            LOG.info(s.toString() + "\n");
            //instantiate the evaluator
            Evaluator evaluator = new Evaluator(args[0]);

            if (s.isOnlySameClassPrediction()) {
                // prediction based on all information (TSP)
                evaluator.addDataset("AWQS_CON", 10000); //TODO: need to configured... AWQS_CON vs. AWQS_COMP
                Predictor.isTSP = false;
            } else {
                // prediction based on all information (TSP)
                evaluator.addDataset("AWQS_COMP", 10000);
                Predictor.isTSP = true;
            }


            //Loading predictors
            //baselines
            evaluator.addPredictor(new RandomPredictor());
            evaluator.addPredictor(new MostFrequentPatternPredictor());


            //current sequence prediction methods...
            evaluator.addPredictor(new DGPredictor("DG"));
            evaluator.addPredictor(new TDAGPredictor());
            evaluator.addPredictor(new MarkovFirstOrderPredictor());
            evaluator.addPredictor(new MarkovAllKPredictor());
            evaluator.addPredictor(new LZ78Predictor());
            evaluator.addPredictor(new CPTPredictor("CPT"));
            evaluator.addPredictor(new CPTPlusPredictor("CPT+"));

            //Start the experiment
            int pos = 1;
            Map<String, LinkedList<Double>> overallAccuracies = new HashMap<>();
            for (int i = 1; i < 6; i++) {//number of guesses
                numberOfGuesses = i;
                LOG.info("\n-----------------------Number-Of-Guesses:" + numberOfGuesses + "-----------------------");
                StatsLogger results = evaluator.Start(Evaluator.KFOLD, 10, true, true, true);
                List<Algo> algorithms = results.getAlgorithms();
                for (Algo a : algorithms) {
                    if (!overallAccuracies.containsKey(a.name))
                        overallAccuracies.put(a.name, new LinkedList<Double>());
                    overallAccuracies.get(a.name).add(a.result.get("Overall") * 100);
                    s.addMethodPerformance(a.name, numberOfGuesses, a.result.get("Overall")*100);
                }
            }
            String outFileName = "/";
            outFileName += s.getPredictionClass()+"-"+s.getQuestionType();
            if (!s.isOnlySameClassPrediction())
                outFileName += "-TSP";
            if (!s.isOnlyContent())
                outFileName += "-STYLE";
            outFileName += ".csv";
            writeInCSV(overallAccuracies, args[1] + outFileName);
            LOG.info("Scenario is investigated completely and the result is available in: " + args[1] + outFileName);
        }

        LOG.debug("Writing aggregated results...");
        writeAggregatedResults(args[1] + "/aggregated-results.csv", scenarios);
        LOG.debug("Writing RMSEs");
        writeRMSEs(Config.PREDICTION_OUTPUT_FOLDER +"RMSEs-All.csv", scenarios, Arrays.asList(new String[]{"type", "scale", "prominence"}),
                Arrays.asList(new String[]{"all", "SWQs", "DWQs"}),
                Arrays.asList(new Boolean[]{true, false}), Arrays.asList(new Boolean[]{true, false}));

        writeRMSEs(Config.PREDICTION_OUTPUT_FOLDER +"RMSEs-Content.csv", scenarios, Arrays.asList(new String[]{"type", "scale", "prominence"}),
                Arrays.asList(new String[]{"all", "SWQs", "DWQs"}),
                Arrays.asList(new Boolean[]{true, false}), Arrays.asList(new Boolean[]{true}));

        writeRMSEs(Config.PREDICTION_OUTPUT_FOLDER +"RMSEs-ContentStyle.csv", scenarios, Arrays.asList(new String[]{"type", "scale", "prominence"}),
                Arrays.asList(new String[]{"all", "SWQs", "DWQs"}),
                Arrays.asList(new Boolean[]{true, false}), Arrays.asList(new Boolean[]{false}));

        writeRMSEs(Config.PREDICTION_OUTPUT_FOLDER +"RMSEs-Type.csv", scenarios, Arrays.asList(new String[]{"type"}),
                Arrays.asList(new String[]{"all", "SWQs", "DWQs"}),
                Arrays.asList(new Boolean[]{true, false}), Arrays.asList(new Boolean[]{true, false}));

        writeRMSEs(Config.PREDICTION_OUTPUT_FOLDER +"RMSEs-Scale.csv", scenarios, Arrays.asList(new String[]{"scale"}),
                Arrays.asList(new String[]{"all", "SWQs", "DWQs"}),
                Arrays.asList(new Boolean[]{true, false}), Arrays.asList(new Boolean[]{true, false}));

        writeRMSEs(Config.PREDICTION_OUTPUT_FOLDER +"RMSEs-Prominence.csv", scenarios, Arrays.asList(new String[]{"prominence"}),
                Arrays.asList(new String[]{"all", "SWQs", "DWQs"}),
                Arrays.asList(new Boolean[]{true, false}), Arrays.asList(new Boolean[]{true, false}));

        LOG.info("Prediction is finished");
    }

    private static void writeInCSV(Map<String, LinkedList<Double>> accuracies, String fileAddress) throws IOException {
        StringBuilder builder = new StringBuilder();
        File file = new File(fileAddress);
        for (String alg : accuracies.keySet()) {
            builder.append(alg);
            builder.append(',');
            LinkedList<Double> vals = accuracies.get(alg);
            for (Double d : vals) {
                builder.append(d);
                builder.append(',');
            }
            builder.append('\n');
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(builder.toString());
        }
    }

    public static void writeAggregatedResults (String file, List<Scenario> scenarios) throws IOException {
        StringBuilder builder = new StringBuilder();
        builder.append("scenario id");
        builder.append(',');
        builder.append("generic class");
        builder.append(',');
        builder.append("question type");
        builder.append(',');
        builder.append("approach");
        builder.append(',');
        builder.append("eval process");
        builder.append(',');
        builder.append("method");
        builder.append(',');
        builder.append("#guesses");
        builder.append(',');
        builder.append("overall accuracy");
        builder.append(',');
        builder.append("best performance");
        builder.append(',');
        builder.append("performance difference");
        builder.append('\n');
        int scenarioID = 1;
        for (Scenario s : scenarios){
            builder.append(s.getCSVResults(scenarioID));
            scenarioID++;
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(builder.toString());
        }
    }

    public static void writeRMSEs(String file, List<Scenario> scenarios,
                                  List<String> genericClasses, List<String> queryTypes,
                                  List<Boolean> sameClass, List<Boolean> onlyContent) throws IOException {
        Map<String, Double> RMSEs = new HashMap<>();
        int secnarioCounter = 0;
        for (Scenario s : scenarios) {
            if (queryTypes.contains(s.getQuestionType())){
                if (genericClasses.contains(s.getPredictionClass())) {
                    if (sameClass.contains(s.isOnlySameClassPrediction())) {
                        if (onlyContent.contains(s.isOnlyContent())) {
                            secnarioCounter++;
                            Map<String, Double> ses = s.getMethodSEs();
                            for (String method : ses.keySet()){
                                if (!RMSEs.keySet().contains(method))
                                    RMSEs.put(method, 0d);
                                RMSEs.put(method, RMSEs.get(method)+ ses.get(method));
                            }
                        }
                    }
                }
            }
        }
        LOG.info("Valid scenarios: "+secnarioCounter);
        for (String method : RMSEs.keySet()) {
            RMSEs.put(method, Math.sqrt(RMSEs.get(method)/(secnarioCounter*5))); //5 is the number of guesses scenarios
        }
        StringBuilder builder = new StringBuilder();
        builder.append("Method");
        builder.append(',');
        builder.append("RMSE");
        builder.append('\n');
        for (String method : RMSEs.keySet()) {
            builder.append(method);
            builder.append(',');
            builder.append(RMSEs.get(method));
            builder.append('\n');
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(builder.toString());
        }
    }
}
