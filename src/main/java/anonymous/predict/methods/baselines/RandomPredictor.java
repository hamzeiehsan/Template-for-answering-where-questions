package anonymous.predict.methods.baselines;

import anonymous.predict.ipredict.database.Item;
import anonymous.predict.ipredict.database.Sequence;
import anonymous.predict.ipredict.predictor.Paramable;
import anonymous.predict.ipredict.predictor.Predictor;

import java.util.*;

public class RandomPredictor extends Predictor {
    private List<Item> listItems = new ArrayList<>();

    public Paramable parameters;

    public RandomPredictor () {
        TAG = "Random";
    }

    @Override
    public Boolean Train(List<Sequence> trainingSequences) {
        Set<Item> items = new HashSet<>();
        for (Sequence s : trainingSequences)
            items.addAll(s.getItems());
        listItems.addAll(items);
        return true;
    }

    @Override
    public Sequence Predict(Sequence target) {
        Random randomizer = new Random();
        int index = randomizer.nextInt(listItems.size()+1);
        if (index == 0) {
            return new Sequence(-1);
        }
        Sequence predicted = new Sequence(-1);
        predicted.addItem(listItems.get(index-1));
        return predicted;
    }

    @Override
    public List<Sequence> Predict(Sequence target, int numberOfGuesses) {
        Random randomizer = new Random();
        List<Sequence> result = new ArrayList<>();
        for (int i = 0; i < numberOfGuesses; i++) {
            int index = randomizer.nextInt(listItems.size() + 1);
            Sequence predicted = new Sequence(-1);
            if (index > 0)
                predicted.addItem(listItems.get(index - 1));
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

    public static void main(String[] args) {
        RandomPredictor predictor = new RandomPredictor();

        //Training sequences
        List<Sequence> training = new ArrayList<Sequence>();

        //1 2 3 4
        Sequence seq1 = new Sequence(-1);
        seq1.addItem(new Item(1));
        seq1.addItem(new Item(2));
        seq1.addItem(new Item(3));
        seq1.addItem(new Item(4));
        training.add(seq1);

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
}
