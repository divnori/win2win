package com.norihome.smartstarz;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.norihome.smartstarz.com.norihome.smartstarz.beans.RankingsBean;

/**
 * Created by User on 12/28/2016.
 */

class RankingsCustomAdapter extends ArrayAdapter<RankingsBean> {
    public RankingsCustomAdapter(Context context, RankingsBean[] rankings) {
        super(context, R.layout.custom_rankingrow , rankings);
        System.out.println("RankingsCustomAdapter created");
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        System.out.println("RankingsCustomAdapter :: getView method called for position" + position);
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View customView = inflater.inflate(R.layout.custom_rankingrow, parent, false);

        RankingsBean singleRanking = getItem(position);
        TextView rankingNumber = (TextView) customView.findViewById(R.id.text_rankingNumber);
        TextView groupName = (TextView) customView.findViewById(R.id.text_groupName);
        TextView smartScore = (TextView) customView.findViewById(R.id.text_smartScore);


        //rankingNumber.setText(singleRanking.getDate());
        rankingNumber.setText("" + singleRanking.getRank());
        groupName.setText(singleRanking.getGroupName());
        smartScore.setText(""+singleRanking.getSmartScore());


        return customView;
    }

}
