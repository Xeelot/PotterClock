package com.apps.xeelot.potterclock;


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

public class MarkerDialog extends DialogFragment {

    private boolean existing = false;

    public void setExisting(boolean exists) {
        existing = exists;
    }

    // Interface for retrieving an asynchronous create/update Marker
    interface MarkerDialogUpdate {
        void markerDialogUpdate(MarkerLocation ml);
    }
    static MarkerDialogUpdate markerDialogUpdate;
    void registerMarkerDialogUpdate(MarkerDialogUpdate callback) {
        markerDialogUpdate = callback;
    }

    // Interface for retrieving an asynchronous delete Marker
    interface MarkerDialogDelete {
        void markerDialogDelete(MarkerLocation ml);
    }
    static MarkerDialogDelete markerDialogDelete;
    void registerMarkerDialogDelete(MarkerDialogDelete callback) {
        markerDialogDelete = callback;
    }

    // Interface for retrieving an asynchronous cancel request
    interface MarkerDialogCancel {
        void markerDialogCancel();
    }
    static MarkerDialogCancel markerDialogCancel;
    void registerMarkerDialogCancel(MarkerDialogCancel callback) {
        markerDialogCancel = callback;
    }


    // Create the AlertDialog instance utilizing the DialogFragment class
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Create the builder and inflate the view
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.map_popup, null);

        // Setup the spinner
        Spinner spinner = (Spinner)view.findViewById(R.id.spinnerIndex);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(view.getContext(),
                R.array.index_names, R.layout.spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        // Setup the radius bar
        /*radiusSeek = (SeekBar)view.findViewById(R.id.seekRadius);
        radiusEdit = (EditText)view.findViewById(R.id.editRadius);
        radiusEdit.setText(radiusSeek.getProgress());
        radiusSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                radiusEdit.setText(seekBar.getProgress());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });
        radiusEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView e, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_DONE && e.getText().toString().length() > 0) {
                    radiusSeek.setProgress(Integer.parseInt(e.getText().toString()));
                    return true;
                }
                return false;
            }
        });*/

        // Set the view, title, and buttons
        builder.setView(view);
        builder.setTitle(R.string.new_marker);
        //TODO: update the title in onStart
        builder.setPositiveButton(R.string.create, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Do nothing, this is overwritten below
            }
        });
        builder.setNeutralButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Since we're deleting, the optional attributes shouldn't be important, call delete callback
                MarkerLocation ml = new MarkerLocation();
                markerDialogDelete.markerDialogDelete(ml);
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Call the cancel interface
                markerDialogCancel.markerDialogCancel();
            }
        });
        return builder.create();
    }

    // Override the onStart method to include validation before exiting
    @Override
    public void onStart() {
        super.onStart();
        final AlertDialog ad = (AlertDialog)getDialog();
        if(ad != null) {
            // Gather our resources
            Button posButton = ad.getButton(Dialog.BUTTON_POSITIVE);
            Button deleteButton = ad.getButton(Dialog.BUTTON_NEUTRAL);
            final EditText nameEdit = (EditText)ad.findViewById(R.id.editName);
            final EditText radiusEdit = (EditText)ad.findViewById(R.id.editRadius);
            final Spinner indexType = (Spinner)ad.findViewById(R.id.spinnerIndex);

            // Customize the buttons based on existing vs new knowledge
            if(existing) {

                posButton.setText(R.string.update);
            }
            else {
                deleteButton.setVisibility(View.INVISIBLE);
                deleteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Do nothing if invisible button is clicked
                    }
                });
            }
            // Overwrite the positive button handler with validation
            posButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    boolean closeDialog = true;
                    if(nameEdit.getText().length() < 3) {
                        closeDialog = false;
                        nameEdit.setError("Please enter valid name");
                    }
                    try {
                        if (Integer.parseInt(radiusEdit.getText().toString()) < 1) {
                            closeDialog = false;
                            radiusEdit.setError("Enter integer greater than 0");
                        }
                    } catch(Exception e) {
                        e.printStackTrace();
                        closeDialog = false;
                        radiusEdit.setError("Enter integer greater than 0");
                    }
                    if(indexType.getSelectedItemPosition() == Spinner.INVALID_POSITION) {
                        closeDialog = false;
                    }
                    if(closeDialog) {
                        ad.dismiss();
                        MarkerLocation ml = new MarkerLocation();
                        ml.setIndex(indexType.getSelectedItemPosition());
                        ml.setName(nameEdit.getText().toString());
                        ml.setRadius(Integer.parseInt(radiusEdit.getText().toString()));
                        markerDialogUpdate.markerDialogUpdate(ml);
                    }
                }
            });
        }
    }
}

