package com.example.prm392_groupassignment.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.prm392_groupassignment.R;
import com.example.prm392_groupassignment.model.Goal;

import java.util.List;

public class GoalGridAdapter extends BaseAdapter {

    private Context context;
    private List<Goal> goals;
    private LayoutInflater inflater;

    public GoalGridAdapter(Context context, List<Goal> goals) {
        this.context = context;
        this.goals = goals;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return goals.size();
    }

    @Override
    public Object getItem(int position) {
        return goals.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        View view = convertView;
        if (view == null) {
            view = inflater.inflate(R.layout.grid_item_goal, parent, false);
            holder = new ViewHolder();
            holder.title = view.findViewById(R.id.item_goal_title);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        Goal goal = goals.get(position);
        holder.title.setText(goal.getGoalName());
        return view;
    }

    private static class ViewHolder {
        TextView title;
    }
}
