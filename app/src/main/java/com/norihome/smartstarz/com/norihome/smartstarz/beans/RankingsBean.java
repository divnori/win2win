package com.norihome.smartstarz.com.norihome.smartstarz.beans;

/**
 * Created by User on 1/11/2017.
 */

public class RankingsBean implements Comparable<RankingsBean> {

    private int rank;

    private String groupName;

    private long smartScore;

    public int getRank() {
        return rank;}

    public void setRank(int rank) {
        this.rank = rank;}

    public String getGroupName() {
        return groupName;}

    public void setGroupName(String groupName) {
        this.groupName = groupName;}

    public long getSmartScore() {
        return smartScore;}

    public void setSmartScore(long smartScore) {
        this.smartScore = smartScore;}

    @Override
    public int compareTo(RankingsBean o) {
        if (this.getSmartScore() < o.getSmartScore())
            return 1;
        else if (this.getSmartScore() > o.getSmartScore())
            return -1;
        else
            return 0;
    }
}
