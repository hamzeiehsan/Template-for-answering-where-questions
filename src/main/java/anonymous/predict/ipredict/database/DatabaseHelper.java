package anonymous.predict.ipredict.database;

import anonymous.predict.ipredict.predictor.profile.Profile;
import anonymous.predict.ipredict.controllers.PredictionWorkflowController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collections;

public class DatabaseHelper {
    private static final Logger LOG = LoggerFactory.getLogger(DatabaseHelper.class);
    /**
     * Path to the datasets directory
     */
    private String basePath;
    //Database
    private SequenceDatabase database;

    /**
     * Main constructor, instantiate an empty database
     */
    public DatabaseHelper(String basePath) {
        this.basePath = basePath;
        this.database = new SequenceDatabase();
    }

    /**
     * Return an instance of the database
     *
     * @return
     */
    public SequenceDatabase getDatabase() {
        return database;
    }

    public void loadDataset(String fileName, int maxCount) {

        //Clearing the database
        if (database == null) {
            database = new SequenceDatabase();
        } else {
            database.clear();
        }

        //Tries to guess the format if it is a predefined dataset
        try {

            Format datasetFormat = Format.valueOf(fileName);
            loadPredefinedDataset(datasetFormat, maxCount);

        } catch (IllegalArgumentException e) {
            loadCustomDataset(fileName, maxCount);
        }

        //Shuffling the database
        Collections.shuffle(database.getSequences());
    }

    private void loadCustomDataset(String fileName, int maxCount) {
        try {

            database.loadFileCustomFormat(fileToPath(fileName), maxCount, Profile.paramInt("sequenceMinSize"), Profile.paramInt("sequenceMaxSize"));

        } catch (IOException e) {
            System.out.println("Could not load dataset, IOExeption");
            e.printStackTrace();
        }
    }

    /**
     * Edited... only Answering Where Questions sequence format supported.
     * Please check the IPredict original implementation for more predefined datasets...
     * Original sequence prediction implementation can be found in: https://github.com/tedgueniche/IPredict
     */
    private void loadPredefinedDataset(Format format, int maxCount) {

        //Loading the specified dataset (according to the format)
        try {
            switch (format) {
                case AWQS_COMP:
                    database.loadFileAWQsCOMPFormat(
                            fileToPath("comp-all-nf-coded.txt"),
                            fileToPath("comp-all-codes.txt"),
                            maxCount, Profile.paramInt("sequenceMinSize"),
                            Profile.paramInt("sequenceMaxSize"));
                    break;
                case AWQS_CON:
                    database.loadFileFormat(
                            fileToPath(PredictionWorkflowController.currentScenario.getPredictionClass() + "-nf-coded.txt")
                            , maxCount, Profile.paramInt("sequenceMinSize"),
                            Profile.paramInt("sequenceMaxSize"));
                    break;
                default:
                    LOG.error("Could not load dataset, unknown format.");
            }

        } catch (IOException e) {
            LOG.error("Could not load dataset, IOExeption");
            e.printStackTrace();
        }
    }

    /**
     * Return the path for the specified data set file
     *
     * @param filename Name of the data set file
     * @throws UnsupportedEncodingException
     */
    public String fileToPath(String filename) throws UnsupportedEncodingException {
        return basePath + File.separator + filename;
    }


    //Data sets
    public enum Format {
        AWQS_COMP, // TSP (type, scale and prominence) prediction of each class (e.g., scale): {type, scale, prominence} -> scale
        AWQS_CON, // Same class (e.g., scale) prediction: scale -> scale
        AWQS
    }

}
