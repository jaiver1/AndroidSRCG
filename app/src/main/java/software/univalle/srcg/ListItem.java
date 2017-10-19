package software.univalle.srcg;


import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import java.util.Locale;

public class ListItem implements Item {
    private final String documento;
    private final String invitado;
    private final Boolean asistencia;

    public ListItem(String documento, String invitado, String asistencia) {
        this.documento = documento;
        this.invitado = invitado;
        this.asistencia = Boolean.parseBoolean(asistencia);
    }

    @Override
    public int getViewType() {
        return TwoTextArrayAdapter.RowType.LIST_ITEM.ordinal();
    }

    @Override
    public View getView(LayoutInflater inflater, View convertView) {
        View view;
        if (convertView == null) {
            if (asistencia) {
                view = (View) inflater.inflate(R.layout.view_guest_disabled_entry, null);
            } else {
                view = (View) inflater.inflate(R.layout.view_guest_entry, null);
            }
        } else {
            view = convertView;
        }

        if (asistencia) {
            TextView txt_documento = (TextView) view.findViewById(R.id.txt_guest_disabled_documento);
            TextView txt_invitado = (TextView) view.findViewById(R.id.txt_guest_disabled_invitado);
            txt_documento.setText(documento.toLowerCase(new Locale("es_CO")));
            txt_documento.setPaintFlags(txt_documento.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            txt_invitado.setText(invitado.toLowerCase(new Locale("es_CO")));
            txt_invitado.setPaintFlags(txt_invitado.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            TextView txt_documento = (TextView) view.findViewById(R.id.txt_guest_documento);
            TextView txt_invitado = (TextView) view.findViewById(R.id.txt_guest_invitado);
            txt_documento.setText(documento);
            txt_invitado.setText(invitado);

        }

        return view;
    }

}