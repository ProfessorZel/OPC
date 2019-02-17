package commander.professor.opc;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class Spinner_adapter implements SpinnerAdapter {
    private LayoutInflater lInflater;
    private ArrayList<User_profile> users;

    Spinner_adapter(Context context, ArrayList<User_profile> profiles) {
        super();
        users = profiles;
        lInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getDropDownView(int position, View convertView,
                                ViewGroup parent) {

        return getCustomView(position, convertView, parent);
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {

    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {

    }

    @Override
    public int getCount() {
        return users.size();
    }

    @Override
    public Object getItem(int position) {
        return users.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        return getCustomView(position, convertView, parent);
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    private View getCustomView(int position, View view,
                               ViewGroup parent) {
        if (view==null){
            view = lInflater.inflate(R.layout.spinner_row_n, parent, false);
        }

        // ((TextView)view.findViewById(R.id.textViews)).setText(users.get(position).id);

        ((TextView)view.findViewById(R.id.spiner_name)).setText(users.get(position).name);
        TextView sub_text = view.findViewById(R.id.sub_text);
        sub_text.setText(String.format("%s : %d", users.get(position).rang, users.get(position).id));


        return view;
    }
}
