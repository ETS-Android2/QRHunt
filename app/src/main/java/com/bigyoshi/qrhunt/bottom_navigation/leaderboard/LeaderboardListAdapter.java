package com.bigyoshi.qrhunt.bottom_navigation.leaderboard;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bigyoshi.qrhunt.R;

import org.w3c.dom.Text;

import java.util.List;
import java.util.Map;
import java.util.Objects;

// yes, i know ideally we are supposed to use objects blag lah la jglksjdfldsf
public class LeaderboardListAdapter extends ArrayAdapter<Map<String, Object>> {

    public LeaderboardListAdapter(@NonNull Context context, int resource, @NonNull List<Map<String, Object>> objects) {
        super(context, resource, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Map<String, Object> leaderBoardPlayerInfo = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.leaderboard_list_content, parent, false);
        }

        TextView rank = (TextView) convertView.findViewById(R.id.rank_num);
        rank.setText(String.valueOf(position + 4));

        TextView username = (TextView) convertView.findViewById(R.id.rank_username);
        username.setText((String) leaderBoardPlayerInfo.get("username"));
        TextView bestUnique = (TextView) convertView.findViewById(R.id.rank_highest_unique);
        bestUnique.setText(String.valueOf((Long) leaderBoardPlayerInfo.get("bestUniqueQr")));
        TextView numScanned = (TextView) convertView.findViewById(R.id.rank_num_scans);
        numScanned.setText(String.valueOf((Long) leaderBoardPlayerInfo.get("numScanned")));
        TextView totalScore = (TextView) convertView.findViewById(R.id.rank_score);
        totalScore.setText(String.valueOf((Long) leaderBoardPlayerInfo.get("totalScore")));

        return convertView;
    }
}
