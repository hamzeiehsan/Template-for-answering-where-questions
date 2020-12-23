package anonymous.predict.ipredict.controllers;

import anonymous.predict.ipredict.database.Sequence;
import anonymous.predict.ipredict.predictor.TDAG.TDAGPredictor;
import anonymous.predict.ipredict.predictor.profile.DefaultProfile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReadingFiles {
    public static void main(String[] args) throws IOException {
        Map<Integer, String> scalesA = readData("sequences/prominence-nf-A.txt");
        Map<Integer, String> scalesQ = readData("sequences/prominence-nf-A.txt");

        Map<Integer, String> scales = new HashMap<>();
        for (Integer key : scalesQ.keySet()) {
            if (scalesA.containsKey(key)) {
                scales.put(key, scalesQ.get(key) + " " + scalesA.get(key));
            }
        }

        //initializing the CPT Plus predictor
        //MarkovAllKPredictor akom = new MarkovAllKPredictor();
        TDAGPredictor pred = new TDAGPredictor();

        //setting the experiment parameters
        DefaultProfile profile = new DefaultProfile();
        profile.Apply();

        //generating the training set
        List<Sequence> trainingSet = new ArrayList<Sequence>();
        for (Integer key : scales.keySet())
            trainingSet.add(Sequence.fromString(key, scales.get(key)));

        //training the model
        pred.Train(trainingSet);

        //predicting a sequence
        Sequence predicted = pred.Predict(Sequence.fromString(0, "7"));

        //output prediction
        System.out.println("Predicted symbol: " + predicted);

    }

    public static Map<Integer, String> readData(String fileAddress) throws IOException {
        Map<Integer, String> result = new HashMap<>();
        File file = new File(fileAddress);
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String st;
        while ((st = reader.readLine()) != null) {
            if (st.trim() != "") {

                String[] val = st.split(" ");
                Integer id = Integer.parseInt(val[0]);
                String values = "";
                int count = 0;
                if (val.length >= 2) {
                    for (String v : val) {
                        if (count != 0) {
                            v = v.replaceAll("Q-", "");
                            v = v.replaceAll("A-", "");
                            values += " " + v;
                        }
                        count++;
                    }
                    result.put(id, values.trim());
                }
            }
        }
        return result;
    }
}
