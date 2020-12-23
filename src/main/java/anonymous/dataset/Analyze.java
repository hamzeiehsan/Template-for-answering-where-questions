package anonymous.dataset;

import anonymous.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class Analyze {
    private static final Logger LOG = LoggerFactory.getLogger(Analyze.class);

    public static void main(String[] args) throws IOException {
        LOG.info("analyzing datasets -- extracting location questions from train and dev datasets...");
        try {
            DatasetParser.lightParse(Config.TRAIN_DATA_FILE_PATH, Config.PARSER_OUTPUT_FOLDER + "/train_location.json");
            DatasetParser.lightParse(Config.DEV_DATA_FILE_PATH, Config.PARSER_OUTPUT_FOLDER + "/dev_location.json");
        } catch (IOException e) {
            LOG.error("Please download MS MARCO v2.1 and put the train, dev and test dataset (json files) in the dataset folder. The data is not distributed in this package because of copyright.");
            throw new RuntimeException(e);
        }
        LOG.info("writing extracted records into files finished...");
    }
}
