package anonymous.gazetteers.osm.scale;

import anonymous.gazetteers.geonames.model.QuestionAnswerScale;
import anonymous.gazetteers.geonames.model.*;

import java.io.Serializable;
import java.util.*;

public class ScaleAnalysisModel implements Serializable {
    private QuestionAnswerScale rawData;
    private ScaleType qScaleType;
    private List<ScaleType> aScaleType;
    private List<RelationType> scaleRelation;
    private String qScaleSignature = "";
    private List<String> aScaleSignature = new ArrayList<>();

    public ScaleAnalysisModel(QuestionAnswerScale rawData) {
        this.rawData = rawData;
        this.qScaleType = listToScaleType(rawData.getScalesInQuestion());
        this.aScaleType = new ArrayList<>();
        this.scaleRelation = new ArrayList<>();
        this.qScaleSignature = scaleSignature(this.rawData.getScalesInQuestion(), this.qScaleType.name()+": ");
        for (Map<Integer, Integer> aS : rawData.getScalesInAnswers()) {
            ScaleType scaleOfA = listToScaleType(aS);
            this.aScaleType.add(scaleOfA);
            this.scaleRelation.add(infer(rawData.getScalesInQuestion(), this.qScaleType, aS, scaleOfA));
            this.aScaleSignature.add(scaleSignature(aS, scaleOfA.name()+": "));
        }
    }

    public QuestionAnswerScale getRawData() {
        return rawData;
    }

    public void setRawData(QuestionAnswerScale rawData) {
        this.rawData = rawData;
    }

    public ScaleType getqScaleType() {
        return qScaleType;
    }

    public void setqScaleType(ScaleType qScaleType) {
        this.qScaleType = qScaleType;
    }

    public List<ScaleType> getaScaleType() {
        return aScaleType;
    }

    public void setaScaleType(List<ScaleType> aScaleType) {
        this.aScaleType = aScaleType;
    }

    public List<RelationType> getScaleRelation() {
        return scaleRelation;
    }

    public void setScaleRelation(List<RelationType> scaleRelation) {
        this.scaleRelation = scaleRelation;
    }

    public String getqScaleSignature() {
        return qScaleSignature;
    }

    public void setqScaleSignature(String qScaleSignature) {
        this.qScaleSignature = qScaleSignature;
    }

    public List<String> getaScaleSignature() {
        return aScaleSignature;
    }

    public void setaScaleSignature(List<String> aScaleSignature) {
        this.aScaleSignature = aScaleSignature;
    }

    private ScaleType listToScaleType(Map<Integer, Integer> data) {
        if (data == null)
            return ScaleType.noScale;
        SortedSet<Integer> temp = new TreeSet<>();
        temp.addAll(data.values());
        if (temp.size() == 0)
            return ScaleType.noScale;
        else if (temp.size() == 1)
            return ScaleType.oneScale;
        return ScaleType.intervalScale;
    }

    public RelationType infer(Map<Integer, Integer> q, ScaleType qS, Map<Integer, Integer> a, ScaleType aS) {
        SortedSet<Integer> qSorted = new TreeSet<>();
        SortedSet<Integer> aSorted = new TreeSet<>();
        qSorted.addAll(q.values());
        aSorted.addAll(a.values());
        //TODO first q-a (n || p || i)->(n || p || i) qS->aS
        if (qS.equals(ScaleType.noScale)) {
            if (aS.equals(ScaleType.noScale)) //noToNo
                return RelationType.noToNo;
            else if (aS.equals(ScaleType.oneScale)) //noToPoint
                return RelationType.noToPoint;
            else //noToInterval
                return RelationType.noToInterval;
        }

        else if (qS.equals(ScaleType.oneScale)) {//point
            Integer qScale = qSorted.first();
            if (aS.equals(ScaleType.noScale)) //pointToNo
                return RelationType.pointToNo;
            else if (aS.equals(ScaleType.oneScale)) {//pointToPoint 3cases
                Integer aScale = aSorted.first();
                if (aScale == qScale)
                    return RelationType.equalPoint;
                else if (aScale > qScale)
                    return RelationType.coarserPoint;
                else
                    return RelationType.finerPoint;
            } else {//pointToInterval 5cases
                Integer aMinScale = aSorted.first();
                Integer aMaxScale = aSorted.last();
                if (qScale > aMaxScale)
                    return RelationType.leftOutInterval;
                else if (qScale < aMinScale)
                    return RelationType.rightOutInterval;
                else if (qScale == aMinScale)
                    return RelationType.rightInterval;
                else if (qScale == aMaxScale)
                    return RelationType.leftInterval;
                else
                    return RelationType.containInterval;
            }
        }

        else {
            Integer qMinScale = qSorted.first();
            Integer qMaxScale = qSorted.last();
            if (aS.equals(ScaleType.noScale)) //intervalToNo!
                return RelationType.intervalToNo;
            else if (aS.equals(ScaleType.oneScale)) {
                //intervalToPoint 5cases!
                Integer aScale = aSorted.first();
                if (aScale > qMaxScale)
                    return RelationType.outRightPoint;
                else if (aScale == qMaxScale)
                    return RelationType.rightPoint;
                else if (aScale < qMinScale)
                    return RelationType.outLeftPoint;
                else if (aScale == qMinScale)
                    return RelationType.leftPoint;
                else
                    return RelationType.inPoint;
            } else {//intervalToInterval 13cases!
                Integer aMinScale = aSorted.first();
                Integer aMaxScale = aSorted.last();

                if (aMinScale == qMinScale) {//equal,leftInside, leftContain
                    if (qMaxScale == qMaxScale)
                        return RelationType.equalInterval;
                    else if (aMaxScale > qMaxScale)
                        return RelationType.leftContain;
                    else
                        return RelationType.leftInside;
                } else if (aMinScale > qMinScale) {//inside, rightInside, rightMeet, rightOverlap, rightOutside
                    if (aMinScale > qMaxScale)
                        return RelationType.rightOutside;
                    else if (aMinScale == qMaxScale)
                        return RelationType.rightMeet;
                    else {
                        if (aMaxScale == qMaxScale)
                            return RelationType.rightInside;
                        else if (aMaxScale > qMaxScale)
                            return RelationType.rightOverlap;
                        else
                            return RelationType.inside;
                    }
                } else {//contain, leftMeet, rightContain, leftOverlap, leftOutside
                    if (qMinScale > aMaxScale)
                        return RelationType.leftOutside;
                    else if (qMinScale == aMinScale)
                        return RelationType.leftMeet;
                    else {
                        if (aMaxScale == qMaxScale)
                            return RelationType.rightContain;
                        else if (aMaxScale > qMaxScale)
                            return RelationType.contain;
                        else
                            return RelationType.leftOverlap;
                    }
                }
            }
        }
    }

    private String scaleSignature (Map<Integer, Integer> scales, String base) {
        SortedSet<Integer> ints = new TreeSet<>();
        ints.addAll(scales.keySet());
        for (Integer location : ints)
            base += scales.get(location);
        return base;
    }
}
