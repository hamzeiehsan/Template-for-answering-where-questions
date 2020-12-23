package anonymous.predict.ipredict.predictor.CPT.CPTPlus;

import anonymous.predict.ipredict.database.Item;
import anonymous.predict.ipredict.database.Sequence;

import java.util.HashMap;
import java.util.List;

public interface FIF {

	public HashMap<Item, Integer> getItemFrequencies(List<Sequence> seqs);
	
	public List<List<Item>> findFrequentItemsets(List<Sequence> seqs, int minLength, int maxlength, int minSup);
}
