package anonymous.predict.ipredict.predictor;

import anonymous.predict.ipredict.database.Sequence;

import java.util.List;

/**
 * Interface for all the predictors
 */
public abstract class Predictor {
	public static boolean isTSP = false;

	/**
	 * Represent the unique name of this predictor
	 * each subclass should overwrite this TAG
	 */
	protected String TAG;
	
	public Predictor(){
	}
	
	public Predictor(String tag) {
		this();
		this.TAG = tag;
	}
	
	/**
	 * Trains this predictor with the provided training data
	 * @return true on success
	 */
	public abstract Boolean Train(List<Sequence> trainingSequences);
	
	/**
	 * Predict the next element in the given sequence
	 * @param sequence to predict
	 */
	public abstract Sequence Predict(Sequence target);


	public abstract List<Sequence> Predict(Sequence target, int numberOfGuesses);

	/**
	 * Get the predictor's TAG (unique string identifier)
	 */
	public String getTAG() {
		return TAG;
	}
	
	/**
	 * Get the size of the predictor after training where the unit is an arbitrary value such as number of nodes (for graph and trees)
	 */
	public abstract long size();
	
	/**
	 * Get the size in bytes of the predictor after training, the size should be theoretical and not obtained from Java directly.
	 */
	public abstract float memoryUsage();
}
