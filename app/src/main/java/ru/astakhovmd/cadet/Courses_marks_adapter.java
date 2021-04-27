package ru.astakhovmd.cadet;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class Courses_marks_adapter extends BaseAdapter {
    private LayoutInflater lInflater;
    private ArrayList<Courses_marks_ID> objects;



    public Courses_marks_adapter(Context _context, ArrayList<Courses_marks_ID> products) {
        objects = products;
        lInflater = (LayoutInflater) _context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return objects.size();
    }

    @Override
    public Object getItem(int position) {
        return objects.get(position);
    }

    @Override
    public long getItemId(int position) {
        return objects.get(position).id;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if (view == null){
            view = lInflater.inflate(R.layout.my_id_courses, parent, false);
        }

        ((TextView) view.findViewById(R.id.course_id)).setText(String.format("%d", objects.get(position).id));
        ((TextView) view.findViewById(R.id.course_name)).setText(objects.get(position).name);
        ((TextView) view.findViewById(R.id.course_author)).setText(objects.get(position).author);
        ((TextView) view.findViewById(R.id.course_date)).setText(objects.get(position).date);
        ((TextView) view.findViewById(R.id.course_mark)).setText(objects.get(position).mark);
        return view;

    }
}
