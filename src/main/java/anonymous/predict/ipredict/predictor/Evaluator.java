package anonymous.predict.ipredict.predictor;

import anonymous.predict.ipredict.database.*;
import anonymous.predict.ipredict.helpers.MemoryLogger;
import anonymous.predict.ipredict.predictor.profile.ProfileManager;
import anonymous.predict.ipredict.controllers.PredictionWorkflowController;
import anonymous.predict.ipredict.helpers.StatsLogger;
import anonymous.predict.ipredict.predictor.profile.Profile;

import anonymous.predict.ipredict.database.*;

import java.io.IOException;
import java.util.*;

/**
 * Evaluation framework
 */
public class Evaluator {

    //Sampling type
    public final static int HOLDOUT = 0;
    public final static int KFOLD = 1;
    public final static int RANDOMSAMPLING = 2;
    //public Stats stats;
    public StatsLogger stats;
    public List<StatsLogger> experiments;
    public List<String> datasets;
    public List<Integer> datasetsMaxCount;
    private List<Predictor> predictors; //list of predictors
    //statistics
    private long startTime;
    private long endTime;
    //Database
    private DatabaseHelper database;


    public Evaluator(String pathToDatasets) {
        predictors = new ArrayList<Predictor>();
        datasets = new ArrayList<String>();
        datasetsMaxCount = new ArrayList<Integer>();
        database = new DatabaseHelper(pathToDatasets);
    }

    /**
     * Tell whether the predicted sequence match the consequent sequence
     */
    public static Boolean isGoodPrediction(Sequence consequent, Sequence predicted) {

        Boolean hasError = false;

        for (Item it : predicted.getItems()) {

            Boolean isFound = false;
            for (Item re : consequent.getItems()) {
                if (re.val.equals(it.val))
                    isFound = true;
            }
            if (isFound == false)
                hasError = true;

        }


        return (hasError == false);
    }


    public static boolean isItOK (Integer code1, Integer code2) {
        if (code1 == code2)
            return true;
        if (code1 == -1 || code2 == -1)
            return true;
        boolean result = false;
        for (String key : SequenceDatabase.validResults.keySet()) {
            List<Integer> temp = SequenceDatabase.validResults.get(key);
            if (temp.contains(code1) && temp.contains(code2)) {
                result = true;
                break;
            }
        }
        return result;
    }

    //TODO!!!! Here is changed to concat!
    public static Boolean isGoodPredictionComplex(Sequence consequent, Sequence predicted) {

        Boolean hasError = false;

        for (Item it : predicted.getItems()) {

            Boolean isFound = false;
            for (Item re : consequent.getItems()) {
                if (isItOK(re.val, it.val))
                    isFound = true;
            }
            if (isFound == false)
                hasError = true;

        }

        return (hasError == false);
    }


    /**
     * Adds a Predictor to the list of predictors
     *
     * @param predictor
     */
    public void addPredictor(Predictor predictor) {
        predictors.add(predictor);
    }

    /**
     * Adds a dataset to the experiment
     *
     * @param format   Format of the Dataset
     * @param maxCount Maximum number of sequence to read in the dataset
     */
    public void addDataset(String format, int maxCount) {
        datasets.add(format);
        datasetsMaxCount.add(maxCount);
    }

    /**
     * Start the controller using the prefered SamplingRate on the list of predictor
     *
     * @param samplingType     one of: HOLDOUT, RANDOMSAMPLING, KFOLD
     * @param param            The parameter associated with the sampling type
     * @param showDatasetStats show statistics about the dataset
     * @return
     */
    public StatsLogger Start(int samplingType, float param, boolean showResults, boolean showDatasetStats, boolean showExecutionStats) throws IOException {

        //Setting statsLogger
        List<String> statsColumns = new ArrayList<String>();
        statsColumns.add("Success");
        statsColumns.add("Failure");
        statsColumns.add("No Match");
        statsColumns.add("Too Small");
        statsColumns.add("Overall");
        statsColumns.add("Size (MB)");
        statsColumns.add("Train Time");
        statsColumns.add("Test Time");

        //Extracting the name of each predictor
        List<String> predictorNames = new ArrayList<String>();
        for (Predictor predictor : predictors) {
            predictorNames.add(predictor.getTAG());
        }

        for (int i = 0; i < datasets.size(); i++) {

            int maxCount = datasetsMaxCount.get(i);
            String format = datasets.get(i);

            //Loading the parameter profile
            ProfileManager.loadProfileByName(format.toString());

            //Loading the dataset
            database.loadDataset(format, maxCount);

            if (showDatasetStats) {
                System.out.println();
                SequenceStatsGenerator.prinStats(database.getDatabase(), format);
            }

            //Creating the statsLogger
            stats = new StatsLogger(statsColumns, predictorNames, false);

            //Saving current time for across time analysis
            startTime = System.currentTimeMillis();

            //For each predictor, do the sampling and do the training/testing
            for (int id = 0; id < predictors.size(); id++) {

                //Picking the sampling strategy
                switch (samplingType) {
                    case HOLDOUT:
                        Holdout(param, id);
                        break;

                    case KFOLD:
                        KFold((int) param, id);
                        break;

                    case RANDOMSAMPLING:
                        RandomSubSampling(param, id);
                        break;

                    default:
                        System.out.println("Unknown sampling type.");
                }
            }
            //Saving end time
            endTime = System.currentTimeMillis();

            finalizeStats(showExecutionStats);

            if (showResults == true) {
                System.out.println(stats.toString());
            }
        }

        return stats;
    }

    /**
     * Holdout method
     * Data are randomly partitioned into two sets (a training set and a test set) using a ratio.
     * The classifier is trained using the training set and evaluated using the test set.
     *
     * @param ratio to divide the training and test sets
     */
    public void Holdout(double ratio, int classifierId) {

        List<Sequence> trainingSequences = getDatabaseCopy();
        List<Sequence> testSequences = splitList(trainingSequences, ratio);

        //DEBUG
        //System.out.println("Dataset size: "+ (trainingSequences.size() + testSequences.size()));
        //System.out.println("Training: " + trainingSequences.size() + " and Test set: "+ testSequences.size());

        PrepareClassifier(trainingSequences, classifierId); //training (preparing) classifier

        //StartClassifier(testSequences, classifierId); //classification of the test sequence
        //StartClassifierMultipleGuess(testSequences, classifierId, MainController.numberOfGuesses); //classification of the test sequence
        //StartClassifierMultipleGuessAllOK(testSequences, classifierId, MainController.numberOfGuesses); //classification of the test sequence
        //StartClassifierMultipleGuessAllOKContent(testSequences, classifierId, MainController.numberOfGuesses); //classification of the test sequence
        StartClassifierCONCATMultipleGuessAllOK(testSequences, classifierId, PredictionWorkflowController.numberOfGuesses); //classification of the test sequence
        //StartClassifierCONCATMultipleGuessAllOKContent(testSequences, classifierId, MainController.numberOfGuesses); //classification of the test sequence
        // INCORRECT :: StartClassifierMultipleGuessComp(testSequences, classifierId, MainController.numberOfGuesses); //classification of the test sequence
    }

    /**
     * Random subsampling
     * Holdout method repeated 10 times
     *
     * @param ratio to use for the holdout method
     */
    public void RandomSubSampling(double ratio, int classifierId) {

        int k = 10;
        for (int i = 0; i < k; i++) {
            Holdout(ratio, classifierId);

            //Logging memory usage
            MemoryLogger.addUpdate();
        }

    }

    /**
     * k-fold cross-validation
     * Data are partitioned in k exclusive subsets (folds) of same size.
     * Training and testing is done k times. For each time; a fold is used for testing
     * and the k-1 other folds for training
     */
    public void KFold(int k, int classifierId) {
        if (k < 2) {
            throw new RuntimeException("K needs to be 2 or more");
        }

        List<Sequence> dataSet = getDatabaseCopy();

        //calculating absolute ratio
        double relativeRatio = 1 / (double) k;
        int absoluteRatio = (int) (dataSet.size() * relativeRatio);

        //For each fold, it does training and testing
        for (int i = 0; i < k; i++) {

            //Partitioning database
            //
            int posStart = i * absoluteRatio; //start position of testing set
            int posEnd = posStart + absoluteRatio; //end position of testing set
            if (i == (k - 1)) { //if last fold we adjust the size to include all the left-over sequences
                posEnd = dataSet.size(); //special case
            }

            //declaring the sets
            List<Sequence> trainingSequences = new LinkedList<Sequence>();
            List<Sequence> testSequences = new LinkedList<Sequence>();

            //actual partitioning
            for (int j = 0; j < dataSet.size(); j++) {

                Sequence toAdd = dataSet.get(j);

                //is in testing set
                if (j >= posStart && j < posEnd) {
                    testSequences.add(toAdd);
                } else {
                    trainingSequences.add(toAdd);
                }
            }
            //
            //End of Partitioning

            PrepareClassifier(trainingSequences, classifierId); //training (preparing) classifier


            if (PredictionWorkflowController.currentScenario.isOnlyContent()) {
                if (PredictionWorkflowController.currentScenario.isOnlySameClassPrediction()) {
                    // only content, use only same class
                    StartClassifierMultipleGuessAllOKContent(testSequences, classifierId, PredictionWorkflowController.numberOfGuesses);
                } else {
                    // only content, use all classes (type, scale, prominence)
                    StartClassifierCONCATMultipleGuessAllOKContent(testSequences, classifierId, PredictionWorkflowController.numberOfGuesses);
                }
            } else {
                if (PredictionWorkflowController.currentScenario.isOnlySameClassPrediction()) {
                    // content + style, use only same class
                    StartClassifierMultipleGuessAllOK(testSequences, classifierId, PredictionWorkflowController.numberOfGuesses);
                } else {
                    // content + style, use all classes (type, scale, prominence)
                    StartClassifierCONCATMultipleGuessAllOK(testSequences, classifierId, PredictionWorkflowController.numberOfGuesses);

                }
            }

            //Logging memory usage
            MemoryLogger.addUpdate();
        }

    }

    /**
     * Display the stats for the experiment
     *
     * @param showExecutionStats
     */
    public void finalizeStats(boolean showExecutionStats) {

        //For each predictor, updates the stats
        for (Predictor predictor : predictors) {

            int success = (int) (stats.get("Success", predictor.getTAG()));
            int failure = (int) (stats.get("Failure", predictor.getTAG()));
            int noMatch = (int) (stats.get("No Match", predictor.getTAG()));
            int tooSmall = (int) (stats.get("Too Small", predictor.getTAG()));


            long matchingSize = success + failure; //For relative success (success / (success + failure))
            long testingSize = matchingSize + noMatch + tooSmall; //For global success (success / All_the_testing)

            stats.divide("Success", predictor.getTAG(), matchingSize);
            stats.divide("Failure", predictor.getTAG(), matchingSize);
            stats.divide("No Match", predictor.getTAG(), testingSize);
            stats.divide("Too Small", predictor.getTAG(), testingSize);

            stats.divide("Train Time", predictor.getTAG(), 100);
            stats.divide("Test Time", predictor.getTAG(), 100);

            //Adding overall success
            stats.set("Overall", predictor.getTAG(), success);
            stats.divide("Overall", predictor.getTAG(), testingSize);

            //Size of the predictor
            stats.set("Size (MB)", predictor.getTAG(), predictor.memoryUsage());
            stats.divide("Size (MB)", predictor.getTAG(), (100 * 1000 * 1000));


        }

        if (showExecutionStats) {
            //memory usage
            MemoryLogger.addUpdate();
            MemoryLogger.displayUsage();

            //Displaying the execution time
            System.out.println("Execution time: " + (endTime - startTime) / 1000 + " seconds");
        }
    }

    private void PrepareClassifier(List<Sequence> trainingSequences, int classifierId) {
        long start = System.currentTimeMillis(); //Training starting time

        predictors.get(classifierId).Train(trainingSequences); //actual training

        long end = System.currentTimeMillis(); //Training ending time
        double duration = (double) (end - start) / 1000;
        stats.set("Train Time", predictors.get(classifierId).getTAG(), duration);
    }

    private void StartClassifier(List<Sequence> testSequences, int classifierId) {

        long start = System.currentTimeMillis(); //Testing starting time

        //for each sequence; it classifies it and evaluates it
        for (Sequence target : testSequences) {

            //if sequence is long enough
            if (target.size() > (Profile.paramInt("consequentSize"))) {
                Integer winSize = Profile.paramInt("windowSize");
                Integer conseqSize = Profile.paramInt("consequentSize");

                //TODO --> ID, sequence of question, and sequence of answer!
                if (PredictionWorkflowController.dynamicWindowsSize.size() > 0) {
                    //    System.out.println("WELL DONE SIZE OF DYNAMIC WINDOW SIZE IS :: " + MainController.dynamicWindowsSize.size());
                    Integer size = PredictionWorkflowController.dynamicWindowsSize.get(target.getId());
                    if (size == null)
                        System.out.println("wait here!");
                    conseqSize = target.size() - size;
                }
                Sequence consequent = target.getLastItems(conseqSize, 0); //the lasts actual items in target
                Sequence finalTarget = target.getLastItems(winSize, conseqSize);
                //System.out.println("Sizes:: "+conseqSize+"\t"+finalTarget.getItems().size()+"\t"+target.getItems().size()+"\t"+consequent.getItems().size());
                Sequence predicted = predictors.get(classifierId).Predict(finalTarget);
                //if no sequence is returned, it means that they is no match for this sequence
                if (predicted.size() == 0) {
                    stats.inc("No Match", predictors.get(classifierId).getTAG());
                }
                //evaluates the prediction
                else if (isGoodPrediction(consequent, predicted)) {
                    stats.inc("Success", predictors.get(classifierId).getTAG());
                } else {
                    stats.inc("Failure", predictors.get(classifierId).getTAG());
                }

            }
            //sequence is too small
            else {
                stats.inc("Too Small", predictors.get(classifierId).getTAG());
            }
        }

        long end = System.currentTimeMillis(); //Training ending time
        double duration = (double) (end - start) / 1000;
        stats.set("Test Time", predictors.get(classifierId).getTAG(), duration);
    }


    private void StartClassifierMultipleGuessComp(List<Sequence> testSequences, int classifierId, int numberOfGuesses) { //ONLY FOR COMPLEXITY

        long start = System.currentTimeMillis(); //Testing starting time

        //for each sequence; it classifies it and evaluates it
        for (Sequence target : testSequences) {
            //if sequence is long enough
            if (target.size() > (Profile.paramInt("consequentSize"))) {
                Integer winSize = Profile.paramInt("windowSize");
                Integer conseqSize = Profile.paramInt("consequentSize");

                int pos = 1;
                //while (conseqSize > 0) {
                    Sequence consequent = target.getLastItems(1, 0); //the lasts actual items in target
                    Sequence finalTarget = target.getLastItems(target.size()-1, 1);
                    //System.out.println("Sizes:: "+conseqSize+"\t"+finalTarget.getItems().size()+"\t"+target.getItems().size()+"\t"+consequent.getItems().size());
                    List<Sequence> predicteds = predictors.get(classifierId).Predict(finalTarget, numberOfGuesses);
                    //if no sequence is returned, it means that they is no match for this sequence
                    boolean isOK = false;
                    for (Sequence predicted : predicteds) {
                        if (predicted.size() == 0) {
                            stats.inc("No Match", predictors.get(classifierId).getTAG());
                        }
                        //evaluates the prediction
                        else if (isGoodPrediction(consequent, predicted)) {
                            stats.inc("Success", predictors.get(classifierId).getTAG());
                            isOK = true;
                            break;
                        }
                        //else {
                        //   stats.inc("Failure", predictors.get(classifierId).getTAG());
                        //}
                    }
                    if (!isOK)
                        stats.inc("Failure", predictors.get(classifierId).getTAG());
                    if (target.getId() == PredictionWorkflowController.testCase) {
                        System.out.println("Found The testcase:-- Prediction based on " + predictors.get(classifierId).getTAG() + " is " + isOK +" POS: "+pos);
                        if (!(PredictionWorkflowController.testPredictions.size() >= pos))
                            PredictionWorkflowController.testPredictions.add(new HashMap<>());
                        if (!PredictionWorkflowController.testPredictions.get(pos-1).containsKey(predictors.get(classifierId).getTAG()))
                            PredictionWorkflowController.testPredictions.get(pos-1).put(predictors.get(classifierId).getTAG(), new HashMap<>());
                        //if(!MainController.testPrediction.get(predictors.get(classifierId).getTAG()).containsKey(numberOfGuesses))
                        PredictionWorkflowController.testPredictions.get(pos-1).get(predictors.get(classifierId).getTAG()).put(numberOfGuesses, predicteds);
                    }
                    //conseqSize--;
                    //pos++;
                //}
            }
        }

        long end = System.currentTimeMillis(); //Training ending time
        double duration = (double) (end - start) / 1000;
        stats.set("Test Time", predictors.get(classifierId).getTAG(), duration);
    }


    private void StartClassifierMultipleGuessAllOK(List<Sequence> testSequences, int classifierId, int numberOfGuesses) {

        long start = System.currentTimeMillis(); //Testing starting time

        //for each sequence; it classifies it and evaluates it
        for (Sequence target : testSequences) {
            //if sequence is long enough
            if (target.size() > (Profile.paramInt("consequentSize"))) {
                Integer winSize = Profile.paramInt("windowSize");
                Integer conseqSize = Profile.paramInt("consequentSize");

                //TODO --> ID, sequence of question, and sequence of answer!
                if (PredictionWorkflowController.dynamicWindowsSize.size() > 0) {
                    //    System.out.println("WELL DONE SIZE OF DYNAMIC WINDOW SIZE IS :: " + MainController.dynamicWindowsSize.size());
                    Integer size = PredictionWorkflowController.dynamicWindowsSize.get(target.getId());
                    conseqSize = target.size() - size;
                    winSize = size;
                }
                int pos = 1;
                boolean isOK = false;
                while (conseqSize > 0) {
                    isOK = false;
                    Sequence consequent = target.getLastItems(conseqSize, conseqSize-1); //the lasts actual items in target
                    Sequence finalTarget = target.getLastItems(winSize, conseqSize);
                    //System.out.println("Sizes:: "+conseqSize+"\t"+finalTarget.getItems().size()+"\t"+target.getItems().size()+"\t"+consequent.getItems().size());
                    List<Sequence> predicteds = predictors.get(classifierId).Predict(finalTarget, numberOfGuesses);
                    //if no sequence is returned, it means that they is no match for this sequence

                    for (Sequence predicted : predicteds) {
                        if (predicted.size() == 0) {
                            stats.inc("No Match", predictors.get(classifierId).getTAG());
                        }
                        //evaluates the prediction
                        else if (isGoodPrediction(consequent, predicted)) {
                            //stats.inc("Success", predictors.get(classifierId).getTAG());
                            isOK = true;
                            break;
                        }
                        //else {
                        //   stats.inc("Failure", predictors.get(classifierId).getTAG());
                        //}
                    }
                    if (!isOK) {
                        stats.inc("Failure", predictors.get(classifierId).getTAG());
                        break;
                    }
//                    if (target.getId() == MainController.testCase) {
//                        System.out.println("Found The testcase:-- Prediction based on " + predictors.get(classifierId).getTAG() + " is " + isOK +" POS: "+pos);
//                        if (!(MainController.testPredictions.size() >= pos))
//                            MainController.testPredictions.add(new HashMap<>());
//                        if (!MainController.testPredictions.get(pos-1).containsKey(predictors.get(classifierId).getTAG()))
//                            MainController.testPredictions.get(pos-1).put(predictors.get(classifierId).getTAG(), new HashMap<>());
//                        //if(!MainController.testPrediction.get(predictors.get(classifierId).getTAG()).containsKey(numberOfGuesses))
//                        MainController.testPredictions.get(pos-1).get(predictors.get(classifierId).getTAG()).put(numberOfGuesses, predicteds);
//                    }
                    conseqSize--;
                    pos++;
                }
                if(isOK)
                    stats.inc("Success", predictors.get(classifierId).getTAG());
            }
        }

        long end = System.currentTimeMillis(); //Training ending time
        double duration = (double) (end - start) / 1000;
        stats.set("Test Time", predictors.get(classifierId).getTAG(), duration);
    }

    private void StartClassifierMultipleGuessAllOKContent(List<Sequence> testSequences, int classifierId, int numberOfGuesses) {

        long start = System.currentTimeMillis(); //Testing starting time

        //for each sequence; it classifies it and evaluates it
        for (Sequence target : testSequences) {
            //if sequence is long enough
            if (target.size() > (Profile.paramInt("consequentSize"))) {
                Integer winSize = Profile.paramInt("windowSize");
                Integer conseqSize = Profile.paramInt("consequentSize");

                //TODO --> ID, sequence of question, and sequence of answer!
                if (PredictionWorkflowController.dynamicWindowsSize.size() > 0) {
                    //    System.out.println("WELL DONE SIZE OF DYNAMIC WINDOW SIZE IS :: " + MainController.dynamicWindowsSize.size());
                    Integer size = PredictionWorkflowController.dynamicWindowsSize.get(target.getId());
                    if (size == null)
                        System.out.println("wait here!");
                    conseqSize = target.size() - size;
                }
                int pos = 1;

                Map<Sequence, Boolean> contents = new HashMap<>();
                for (int kkk = conseqSize; kkk > 0; kkk--) {
                    Sequence consequent = target.getLastItems(kkk, kkk-1); //the lasts actual items in target
                    contents.put(consequent, false);
                }

                boolean isOK = false;
                while (conseqSize > 0) {
                    isOK = false;

                    Sequence finalTarget = target.getLastItems(winSize, conseqSize);
                    //System.out.println("Sizes:: "+conseqSize+"\t"+finalTarget.getItems().size()+"\t"+target.getItems().size()+"\t"+consequent.getItems().size());
                    List<Sequence> predicteds = predictors.get(classifierId).Predict(finalTarget, numberOfGuesses);
                    //if no sequence is returned, it means that they is no match for this sequence

                    for (Sequence predicted : predicteds) {
                        if (predicted.size() == 0) {
                            stats.inc("No Match", predictors.get(classifierId).getTAG());
                        }
                        //evaluates the prediction
                        else if (isGoodPredictionOrNot(contents, predicted)) {
                            //stats.inc("Success", predictors.get(classifierId).getTAG());
                            isOK = true;
                            break;
                        }
                        //else {
                        //   stats.inc("Failure", predictors.get(classifierId).getTAG());
                        //}
                    }
                    if (!isOK) {
                        stats.inc("Failure", predictors.get(classifierId).getTAG());
                        break;
                    }
//                    if (target.getId() == MainController.testCase) {
//                        System.out.println("Found The testcase:-- Prediction based on " + predictors.get(classifierId).getTAG() + " is " + isOK +" POS: "+pos);
//                        if (!(MainController.testPredictions.size() >= pos))
//                            MainController.testPredictions.add(new HashMap<>());
//                        if (!MainController.testPredictions.get(pos-1).containsKey(predictors.get(classifierId).getTAG()))
//                            MainController.testPredictions.get(pos-1).put(predictors.get(classifierId).getTAG(), new HashMap<>());
//                        //if(!MainController.testPrediction.get(predictors.get(classifierId).getTAG()).containsKey(numberOfGuesses))
//                        MainController.testPredictions.get(pos-1).get(predictors.get(classifierId).getTAG()).put(numberOfGuesses, predicteds);
//                    }
                    conseqSize--;
                    pos++;
                }
                if(isOK)
                    stats.inc("Success", predictors.get(classifierId).getTAG());
            }
        }

        long end = System.currentTimeMillis(); //Training ending time
        double duration = (double) (end - start) / 1000;
        stats.set("Test Time", predictors.get(classifierId).getTAG(), duration);
    }

    private boolean isGoodPredictionOrNotComplex(Map<Sequence,Boolean> contents, Sequence predicted) {
        boolean correct = false;
        for (Sequence seq : contents.keySet()) {
            if (isGoodPredictionComplex(seq, predicted)) {
                contents.put(seq, true);
                correct = true;
            }
        }
        return correct;
    }


    private boolean isGoodPredictionOrNot(Map<Sequence,Boolean> contents, Sequence predicted) {
        boolean correct = false;
        for (Sequence seq : contents.keySet()) {
            if (isGoodPrediction(seq, predicted)) {
                contents.put(seq, true);
                correct = true;
            }
        }
        return correct;
    }

    private void StartClassifierCONCATMultipleGuessAllOK(List<Sequence> testSequences, int classifierId, int numberOfGuesses) {

        long start = System.currentTimeMillis(); //Testing starting time

        //for each sequence; it classifies it and evaluates it
        for (Sequence target : testSequences) {
            //if sequence is long enough
            if (target.size() > (Profile.paramInt("consequentSize"))) {
                Integer winSize = Profile.paramInt("windowSize");
                Integer conseqSize = Profile.paramInt("consequentSize");

                //TODO --> ID, sequence of question, and sequence of answer!
                if (PredictionWorkflowController.dynamicWindowsSize.size() > 0) {
                    //    System.out.println("WELL DONE SIZE OF DYNAMIC WINDOW SIZE IS :: " + MainController.dynamicWindowsSize.size());
                    Integer size = PredictionWorkflowController.dynamicWindowsSize.get(target.getId());
                    if (size == null)
                        System.out.println("wait here!");
                    conseqSize = target.size() - size;
                }
                int pos = 1;
                boolean isOK = false;
                while (conseqSize > 0) {
                    isOK = false;
                    Sequence consequent = target.getLastItems(conseqSize, conseqSize-1); //the lasts actual items in target
                    Sequence finalTarget = target.getLastItems(winSize, conseqSize);
                    //System.out.println("Sizes:: "+conseqSize+"\t"+finalTarget.getItems().size()+"\t"+target.getItems().size()+"\t"+consequent.getItems().size());
                    List<Sequence> predicteds = predictors.get(classifierId).Predict(finalTarget, numberOfGuesses);
                    //if no sequence is returned, it means that they is no match for this sequence

                    for (Sequence predicted : predicteds) {
                        if (predicted.size() == 0) {
                            stats.inc("No Match", predictors.get(classifierId).getTAG());
                        }
                        //evaluates the prediction
                        else if (isGoodPredictionComplex(consequent, predicted)) {
                            //stats.inc("Success", predictors.get(classifierId).getTAG());
                            isOK = true;
                            break;
                        }
                        //else {
                        //   stats.inc("Failure", predictors.get(classifierId).getTAG());
                        //}
                    }
                    if (!isOK) {
                        stats.inc("Failure", predictors.get(classifierId).getTAG());
                        break;
                    }
//                    if (target.getId() == MainController.testCase) {
//                        System.out.println("Found The testcase:-- Prediction based on " + predictors.get(classifierId).getTAG() + " is " + isOK +" POS: "+pos);
//                        if (!(MainController.testPredictions.size() >= pos))
//                            MainController.testPredictions.add(new HashMap<>());
//                        if (!MainController.testPredictions.get(pos-1).containsKey(predictors.get(classifierId).getTAG()))
//                            MainController.testPredictions.get(pos-1).put(predictors.get(classifierId).getTAG(), new HashMap<>());
//                        //if(!MainController.testPrediction.get(predictors.get(classifierId).getTAG()).containsKey(numberOfGuesses))
//                        MainController.testPredictions.get(pos-1).get(predictors.get(classifierId).getTAG()).put(numberOfGuesses, predicteds);
//                    }
                    conseqSize--;
                    pos++;
                }
                if(isOK)
                    stats.inc("Success", predictors.get(classifierId).getTAG());
            }
        }

        long end = System.currentTimeMillis(); //Training ending time
        double duration = (double) (end - start) / 1000;
        stats.set("Test Time", predictors.get(classifierId).getTAG(), duration);
    }

    private void StartClassifierCONCATMultipleGuessAllOKContent(List<Sequence> testSequences, int classifierId, int numberOfGuesses) {

        long start = System.currentTimeMillis(); //Testing starting time

        //for each sequence; it classifies it and evaluates it
        for (Sequence target : testSequences) {
            //if sequence is long enough
            if (target.size() > (Profile.paramInt("consequentSize"))) {
                Integer winSize = Profile.paramInt("windowSize");
                Integer conseqSize = Profile.paramInt("consequentSize");

                //TODO --> ID, sequence of question, and sequence of answer!
                if (PredictionWorkflowController.dynamicWindowsSize.size() > 0) {
                    //    System.out.println("WELL DONE SIZE OF DYNAMIC WINDOW SIZE IS :: " + MainController.dynamicWindowsSize.size());
                    Integer size = PredictionWorkflowController.dynamicWindowsSize.get(target.getId());
                    if (size == null)
                        System.out.println("wait here!");
                    conseqSize = target.size() - size;
                }

                Map<Sequence, Boolean> contents = new HashMap<>();
                for (int kkk = conseqSize; kkk > 0; kkk--) {
                    Sequence consequent = target.getLastItems(kkk, kkk-1); //the lasts actual items in target
                    contents.put(consequent, false);
                }

                int pos = 1;
                boolean isOK = false;
                while (conseqSize > 0) {
                    isOK = false;

                    Sequence finalTarget = target.getLastItems(winSize, conseqSize);
                    //System.out.println("Sizes:: "+conseqSize+"\t"+finalTarget.getItems().size()+"\t"+target.getItems().size()+"\t"+consequent.getItems().size());
                    List<Sequence> predicteds = predictors.get(classifierId).Predict(finalTarget, numberOfGuesses);
                    //if no sequence is returned, it means that they is no match for this sequence

                    for (Sequence predicted : predicteds) {
                        if (predicted.size() == 0) {
                            stats.inc("No Match", predictors.get(classifierId).getTAG());
                        }
                        //evaluates the prediction
                        else if (isGoodPredictionOrNotComplex(contents, predicted)) {
                            //stats.inc("Success", predictors.get(classifierId).getTAG());
                            isOK = true;
                            break;
                        }
                        //else {
                        //   stats.inc("Failure", predictors.get(classifierId).getTAG());
                        //}
                    }
                    if (!isOK) {
                        stats.inc("Failure", predictors.get(classifierId).getTAG());
                        break;
                    }
//                    if (target.getId() == MainController.testCase) {
//                        System.out.println("Found The testcase:-- Prediction based on " + predictors.get(classifierId).getTAG() + " is " + isOK +" POS: "+pos);
//                        if (!(MainController.testPredictions.size() >= pos))
//                            MainController.testPredictions.add(new HashMap<>());
//                        if (!MainController.testPredictions.get(pos-1).containsKey(predictors.get(classifierId).getTAG()))
//                            MainController.testPredictions.get(pos-1).put(predictors.get(classifierId).getTAG(), new HashMap<>());
//                        //if(!MainController.testPrediction.get(predictors.get(classifierId).getTAG()).containsKey(numberOfGuesses))
//                        MainController.testPredictions.get(pos-1).get(predictors.get(classifierId).getTAG()).put(numberOfGuesses, predicteds);
//                    }
                    conseqSize--;
                    pos++;
                }
                if(isOK)
                    stats.inc("Success", predictors.get(classifierId).getTAG());
            }
        }

        long end = System.currentTimeMillis(); //Training ending time
        double duration = (double) (end - start) / 1000;
        stats.set("Test Time", predictors.get(classifierId).getTAG(), duration);
    }


    private List<Sequence> splitList(List<Sequence> toSplit, double absoluteRatio) {

        int relativeRatio = (int) (toSplit.size() * absoluteRatio); //absolute ratio: [0.0-1.0]

        List<Sequence> sub = toSplit.subList(relativeRatio, toSplit.size());
        List<Sequence> two = new ArrayList<Sequence>(sub);
        sub.clear();

        return two;
    }

    private List<Sequence> getDatabaseCopy() {
        return new ArrayList<Sequence>(database.getDatabase().getSequences().subList(0, database.getDatabase().size()));
    }

}
