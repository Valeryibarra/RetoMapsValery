package miprimerproyecto.co.retovalery;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class DialogInput extends AppCompatDialogFragment {


    private EditText et_nombre_lugar;
    private iDialogInterfaceActions iDialogInterfaceActions;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialogo_et, null);
        et_nombre_lugar = view.findViewById(R.id.et_nombre_lugar);
        builder.setView(view).setTitle("Nuevo Lugar").setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        }).setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                iDialogInterfaceActions.createNewPlace(et_nombre_lugar.getText().toString());
            }
        });
        return builder.create();
    }


    public interface iDialogInterfaceActions {
        void createNewPlace(String newPlace);
    }

    public void onAttach(Context context){
        super.onAttach(context);
        try{
            iDialogInterfaceActions = (iDialogInterfaceActions) context;
        }catch (ClassCastException e){
            throw new ClassCastException(context.toString()+" DialogInput" );
        }
    }


}
