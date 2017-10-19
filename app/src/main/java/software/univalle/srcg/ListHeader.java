package software.univalle.srcg;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class ListHeader implements Item {
    private final String graduate;

    public ListHeader(String graduate) {
        this.graduate = graduate;
    }

    @Override
    public int getViewType() {
        return TwoTextArrayAdapter.RowType.HEADER_ITEM.ordinal();
    }

    @Override
    public View getView(LayoutInflater inflater, View convertView) {
        View view;
        if (convertView == null) {
            view = (View) inflater.inflate(R.layout.listview_header, null);
        } else {
            view = convertView;
        }

        TextView text = (TextView) view.findViewById(R.id.txt_graduate_info);
        text.setText(graduate);

        return view;
    }

}