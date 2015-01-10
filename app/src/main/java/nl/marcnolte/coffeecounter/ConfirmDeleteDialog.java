package nl.marcnolte.coffeecounter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class ConfirmDeleteDialog extends DialogFragment
{
    public interface ConfirmDeleteDialogListener
    {
        public void onConfirmDeleteDialogPositiveClick(ConfirmDeleteDialog dialog, int entryID);
    }

    ConfirmDeleteDialogListener mListener;

    public static ConfirmDeleteDialog newInstance(int entryID)
    {
        ConfirmDeleteDialog dialogFragment = new ConfirmDeleteDialog();

        Bundle args = new Bundle();
        args.putInt("entryID", entryID);

        dialogFragment.setArguments(args);

        return dialogFragment;
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);

        // Verify that the host activity implements the callback interface
        try {
            mListener = (ConfirmDeleteDialogListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement ConfirmDeleteDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        super.onCreateDialog(savedInstanceState);

        final int entryID = getArguments().getInt("entryID");

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder
            .setTitle(R.string.dialog_entry_confirm_delete_title)
            .setMessage(R.string.dialog_entry_confirm_delete_message)
            .setPositiveButton(R.string.dialog_ok,     new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // Send the positive button event back to the host fragment
                    mListener.onConfirmDeleteDialogPositiveClick(ConfirmDeleteDialog.this, entryID);
                }
            })
            .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User cancelled the dialog
                    ConfirmDeleteDialog.this.getDialog().cancel();
                }
            });
        // Create and return the AlertDialog object
        return builder.create();
    }
}
