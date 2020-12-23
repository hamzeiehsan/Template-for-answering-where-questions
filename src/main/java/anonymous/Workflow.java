package anonymous;

import anonymous.gazetteers.geonames.Analyze;
import anonymous.predict.ipredict.controllers.PredictionWorkflowController;
import anonymous.sequence.PostProcessing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Modified by [anonymous] [anonymous] on 20/01/2020.
 * The complete cycle of proposed analysis:
 * (1) reading dataset, reading AGILE2020 results (geographic questions)
 * (2) filtering only geographic where-questions -> toponym-based where questions
 * (3) transforming into type (GEONAMES), scale (8 levels) and prominence (7 levels)
 * (4) writing into sequence format and constructing predictive model
 */
public class Workflow
{
    private static final Logger LOG = LoggerFactory.getLogger(Workflow.class);

    static {

    }

    public static void main( String[] args ) throws Exception {
        LOG.info("****************Dataset package is running****************");
        anonymous.dataset.Analyze.main(args);
        LOG.info("****************Parser package is running****************");
        Analyze.main(args);
        LOG.info("****************Gazetteers package is running****************");
        anonymous.gazetteers.osm.Analyze.main(args);
        LOG.info("****************Sequence package is running****************");
        anonymous.sequence.Analyze.main(args);
        PostProcessing.main(args);
        LOG.info("****************Prediction package is running****************");
        PredictionWorkflowController.main(args);
    }
}