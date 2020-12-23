package anonymous.predict.methods.baselines;

import anonymous.predict.MapUtil;
import anonymous.predict.ipredict.controllers.PredictionWorkflowController;
import anonymous.predict.ipredict.database.Item;
import anonymous.predict.ipredict.database.Sequence;
import anonymous.predict.ipredict.predictor.Paramable;
import anonymous.predict.ipredict.predictor.Predictor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MostFrequentPatternPredictor extends Predictor {
    public Paramable parameters;
    Sequence frequentPattern = null;
    List<Sequence> frequentPatterns = new ArrayList<>();

    public MostFrequentPatternPredictor() {
        TAG = "MFP";
    }

    public static void main(String[] args) {
        MostFrequentPatternPredictor predictor = new MostFrequentPatternPredictor();

        //Training sequences
        List<Sequence> training = new ArrayList<Sequence>();

        //1 2 3 4
        Sequence seq1 = new Sequence(-1);
        seq1.addItem(new Item(1));
        seq1.addItem(new Item(2));
        seq1.addItem(new Item(3));
        seq1.addItem(new Item(4));
        training.add(seq1);

        //1 2 3 4
        Sequence seq11 = new Sequence(-1);
        seq11.addItem(new Item(1));
        seq11.addItem(new Item(2));
        seq11.addItem(new Item(3));
        seq11.addItem(new Item(4));
        training.add(seq11);

        //1 2 5 4
        Sequence seq2 = new Sequence(-1);
        seq2.addItem(new Item(1));
        seq2.addItem(new Item(2));
        seq2.addItem(new Item(5));
        seq2.addItem(new Item(4));
        training.add(seq2);

        //Training the predictor
        predictor.Train(training);

        //Testing sequence
        Sequence seqT = new Sequence(-1);
        seqT.addItem(new Item(2));
        seqT.addItem(new Item(3));

        //Actual prediction
        Sequence result = predictor.Predict(seqT);

        //Show results
        System.out.println(result.toString());
    }

    @Override
    public Boolean Train(List<Sequence> trainingSequences) {
        Map<String, Integer> patternsFrequency = new HashMap<>();
        Map<String, Integer> patternsId = new HashMap<>();
        for (Sequence s : trainingSequences) {
            String patt = "";
            for (Item item : s.getItems()) {
                patt += item.toString() + "-";
            }
            if (!patternsFrequency.containsKey(patt))
                patternsFrequency.put(patt, 0);
            patternsFrequency.put(patt, patternsFrequency.get(patt) + 1);
            patternsId.put(patt, trainingSequences.indexOf(s));
        }

        String patMax = null;
        Integer max = 0;
        for (String pat : patternsFrequency.keySet()) {
            if (patternsFrequency.get(pat) > max) {
                max = patternsFrequency.get(pat);
                patMax = pat;
            }
        }
        frequentPattern = trainingSequences.get(patternsId.get(patMax));

        Map<String, Integer> res = MapUtil.getKthValues(patternsFrequency, PredictionWorkflowController.numberOfGuesses);
        for (String pat : res.keySet()) {
            frequentPatterns.add(trainingSequences.get(patternsId.get(pat)));
        }

        return true;
    }

    @Override
    public Sequence Predict(Sequence target) {
        if (frequentPattern == null) {
            System.out.println("ISSUE : MMP predictor--> NULL frequent pattern!");
        }
        if (target.getItems().size() >= frequentPattern.getItems().size()) {
            return new Sequence(-1);
        }
        Sequence predicted = new Sequence(-1);
        predicted.addItem(frequentPattern.get(target.getItems().size()));
        return predicted;
    }

    @Override
    public List<Sequence> Predict(Sequence target, int numberOfGuesses) {
        List<Sequence> result = new ArrayList<>();
        for (int i = 0; i < numberOfGuesses; i++) {
            Sequence predicted = new Sequence(-1);
            if (target.getItems().size() < frequentPatterns.get(i).getItems().size()) {
                predicted.addItem(frequentPatterns.get(i).get(target.getItems().size()));
            }
            result.add(predicted);
        }
        return result;
    }

    @Override
    public long size() {
        return 0;
    }

    @Override
    public float memoryUsage() {
        return 0;
    }
}
