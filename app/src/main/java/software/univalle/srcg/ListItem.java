package software.univalle.srcg;


import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class ListItem implements Item {
    private final String documento;
    private final String invitado;

    public ListItem(String documento, String invitado) {
        this.documento = documento;
        this.invitado = invitado;
    }

    @Override
    public int getViewType() {
        return TwoTextArrayAdapter.RowType.LIST_ITEM.ordinal();
    }

    @Override
    public View getView(LayoutInflater inflater, View convertView) {
        View view;
        if (convertView == null) {
            view = (View) inflater.inflate(R.layout.view_guest_entry, null);
            // Do some initialization
        } else {
            view = convertView;
        }

        TextView txt_documento = (TextView) view.findViewById(R.id.txt_guest_documento);
        TextView txt_invitado = (TextView) view.findViewById(R.id.txt_guest_invitado);
        txt_documento.setText(documento);
        txt_invitado.setText(invitado);

        return view;
    }

}