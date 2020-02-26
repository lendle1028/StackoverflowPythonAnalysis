/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rocks.imsofa.stackoverflowpythonanalysis;

/**
 * store configurations and results of each round
 * @author lendle
 */
public class Round {
    private int centers=-1;
    private double TAG_BRANCHING_OCCURRENCE_THRESHOLD=-1;
    private double BRANCH_OCCURRENCE_THRESHOLD=-1;
    private double BRANCH_RATIO_THRESHOLD=-1;
    private double IND_TAG_RATIO_THRESHOLD_AS_STARTING_TAGS=-1;
    private double result=-1;
    private double kmeansResult=-1;
    
    public int getCenters() {
        return centers;
    }

    public void setCenters(int centers) {
        this.centers = centers;
    }

    public double getTAG_BRANCHING_OCCURRENCE_THRESHOLD() {
        return TAG_BRANCHING_OCCURRENCE_THRESHOLD;
    }

    public void setTAG_BRANCHING_OCCURRENCE_THRESHOLD(double TAG_BRANCHING_OCCURRENCE_THRESHOLD) {
        this.TAG_BRANCHING_OCCURRENCE_THRESHOLD = TAG_BRANCHING_OCCURRENCE_THRESHOLD;
    }

    public double getBRANCH_OCCURRENCE_THRESHOLD() {
        return BRANCH_OCCURRENCE_THRESHOLD;
    }

    public void setBRANCH_OCCURRENCE_THRESHOLD(double BRANCH_OCCURRENCE_THRESHOLD) {
        this.BRANCH_OCCURRENCE_THRESHOLD = BRANCH_OCCURRENCE_THRESHOLD;
    }

    public double getBRANCH_RATIO_THRESHOLD() {
        return BRANCH_RATIO_THRESHOLD;
    }

    public void setBRANCH_RATIO_THRESHOLD(double BRANCH_RATIO_THRESHOLD) {
        this.BRANCH_RATIO_THRESHOLD = BRANCH_RATIO_THRESHOLD;
    }

    public double getIND_TAG_RATIO_THRESHOLD_AS_STARTING_TAGS() {
        return IND_TAG_RATIO_THRESHOLD_AS_STARTING_TAGS;
    }

    public void setIND_TAG_RATIO_THRESHOLD_AS_STARTING_TAGS(double IND_TAG_RATIO_THRESHOLD_AS_STARTING_TAGS) {
        this.IND_TAG_RATIO_THRESHOLD_AS_STARTING_TAGS = IND_TAG_RATIO_THRESHOLD_AS_STARTING_TAGS;
    }

    public double getResult() {
        return result;
    }

    public void setResult(double result) {
        this.result = result;
    }

    public double getKmeansResult() {
        return kmeansResult;
    }

    public void setKmeansResult(double kmeansResult) {
        this.kmeansResult = kmeansResult;
    }
    
    
}
