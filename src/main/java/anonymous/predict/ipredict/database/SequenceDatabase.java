package anonymous.predict.ipredict.database;

import anonymous.predict.ipredict.predictor.profile.Profile;
import anonymous.predict.ipredict.controllers.PredictionWorkflowController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;


public class SequenceDatabase {
    public static Map<String, List<Integer>> validResults = new HashMap<>();
    public static Map<Integer, String> reverseResults = new HashMap<>();
    private List<Sequence> sequences = new ArrayList<Sequence>();
    private static final Logger LOG = LoggerFactory.getLogger(SequenceDatabase.class);

    public SequenceDatabase() {
    }

    //Getter
    public List<Sequence> getSequences() {
        return sequences;
    }

    //Setter
    public void setSequences(List<Sequence> newSequences) {
        this.sequences = new ArrayList<Sequence>(newSequences);
    }

    public int size() {
        return sequences.size();
    }

    public void clear() {
        sequences.clear();
    }

    public void loadFileCustomFormat(String filepath, int maxCount, int minSize, int maxSize) throws IOException {

        String line;
        BufferedReader reader = null;

        try {
            //Opening the file
            reader = new BufferedReader(new FileReader(filepath));

            //For each line in the files -- up to the end of the file or the max number of sequences
            int count = 0;
            while ((line = reader.readLine()) != null && count < maxCount) {

                //Spliting into items
                String[] split = line.split(" ");

                //Checks the size requirements of this sequence
                if (split.length >= minSize && split.length <= maxSize) {

                    Sequence sequence = new Sequence(-1);
                    for (String value : split) {
                        Item item = new Item(Integer.valueOf(value)); //adding current val to current sequence
                        sequence.addItem(item);
                    }

                    //Saving the sequence
                    sequences.add(sequence);
                    count++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }


    public void loadFileAWQsCOMPFormat(String filePath, String codePath, int maxCount, int minSize, int maxSize) throws IOException {
        reverseResults = new HashMap<>();
        loadFileFormat(filePath, maxCount, minSize, maxSize);
        String thisLineCode;
        BufferedReader myInputCode = null;
        try {
            FileInputStream fin = new FileInputStream(new File(codePath));
            myInputCode = new BufferedReader(new InputStreamReader(fin));
            int i = 0;
            while ((thisLineCode = myInputCode.readLine()) != null) {
                String[] split = thisLineCode.trim().split(" ");
                if (split.length != 2) {
                    System.out.println("A very small sequence is found... length: "+split.length);
                } else {
                    int code = Integer.parseInt(split[0]);
                    String spl = split[1];
                    String fixedVal = "";
                    if (PredictionWorkflowController.currentScenario.getPredictionClass().equals("type"))
                        fixedVal = spl.split("\t")[0]; //0 type, 1 scale, 2 prominence
                    else if (PredictionWorkflowController.currentScenario.getPredictionClass().equals("scale"))
                        fixedVal = spl.split("\t")[1]; //0 type, 1 scale, 2 prominence
                    else
                        fixedVal = spl.split("\t")[2]; //0 type, 1 scale, 2 prominence
                    if (!reverseResults.containsKey(code))
                        reverseResults.put(code, fixedVal);
                    else
                        if (!reverseResults.get(code).equals(fixedVal))
                            System.out.println("Reverse results could not be found!!!!");
                    if (!SequenceDatabase.validResults.containsKey(fixedVal))
                        SequenceDatabase.validResults.put(fixedVal, new ArrayList<>());
                    SequenceDatabase.validResults.get(fixedVal).add(code);
                }
            }
            LOG.debug("Length of the code dictionary:: "+SequenceDatabase.validResults.size() +" Reverse: " + reverseResults.size());

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (myInputCode != null) {
                myInputCode.close();
            }
        }

    }


    public void loadFileFormat(String filePath, int maxCount, int minSize, int maxSize) throws IOException {
        String thisLine;
        BufferedReader myInput = null;
        try {
            FileInputStream fin = new FileInputStream(new File(filePath));
            myInput = new BufferedReader(new InputStreamReader(fin));
            int i = 0;
            while ((thisLine = myInput.readLine()) != null) {
                String[] split = thisLine.trim().split(" ");

                if (maxCount == i) {
                    break;
                }
                Sequence sequence = new Sequence(-1);
                int counter = 0;
                boolean isSWQ = false;
                for (String val : split) {
                    int value = Integer.valueOf(val);
                    if (counter == 0) {
                        sequence.setID(value);
                    } else if (counter == split.length - 1) {
                        PredictionWorkflowController.dynamicWindowsSize.put(sequence.getId(), value); //adding size of question sequence for each sequence!
                        if (value == 1)
                            isSWQ = true;
                    } else {
                        sequence.addItem(new Item(value));
                    }
                    counter++;
                }

                if (sequence.size() >= 2 && sequence.size() <= 20) { //all
                    if (PredictionWorkflowController.currentScenario.getQuestionType().equals("SWQs")) {
                        //System.out.println("Filtering only simple where questions...");
                        if (isSWQ) {
                            sequences.add(sequence);
                            i++;
                        }
                    } else if (PredictionWorkflowController.currentScenario.getQuestionType().equals("DWQs")) {
                        //System.out.println("Filtering only detailed where questions...");
                        if (!isSWQ) {
                            sequences.add(sequence);
                            i++;
                        }
                    } else {
                        //System.out.println("Adding all questions (SWQs + DWQs)...");
                        sequences.add(sequence);
                        i++;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (myInput != null) {
                myInput.close();
            }
        }
    }


    public void loadFileSimpleFormat(String filepath, int maxCount, int minSize, int maxSize) throws IOException {
        String thisLine;
        BufferedReader myInput = null;
        try {
            System.out.println("reading file: "+filepath);
            FileInputStream fin = new FileInputStream(new File(filepath));
            myInput = new BufferedReader(new InputStreamReader(fin));
            int i = 0;
            while ((thisLine = myInput.readLine()) != null) {
                Set<Integer> alreadySeen = new HashSet<Integer>();
                String[] split = thisLine.trim().split(" ");

                if (maxCount == i) {
                    break;
                }
                Sequence sequence = new Sequence(-1);
                int lastValue = 0;
                for (String val : split) {
                    int value = Integer.valueOf(val);

                    if (Profile.paramInt("removeDuplicatesMethod") == 2) {

                        if (alreadySeen.contains(value)) {
                            continue;
                        } else {
                            alreadySeen.add(value);
                        }
                    } else if (Profile.paramInt("removeDuplicatesMethod") == 1) {
                        //approach B
                        if (lastValue == value) {
                            continue;
                        }
                        lastValue = value;
                    }
                    sequence.addItem(new Item(value));
                }
                if (sequence.size() >= minSize && sequence.size() <= maxSize) {
                    sequences.add(sequence);
                    i++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (myInput != null) {
                myInput.close();
            }
        }

    }

}
