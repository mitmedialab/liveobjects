package edu.mit.media.obm.liveobjects.app;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import edu.mit.media.obm.shair.liveobjects.R;


/**
 * @author Valerio Panzica La Manna <vpanzica@mit.edu>
 */
public class CommentsFragment extends Fragment{


    AlertDialog.Builder mAddCommentAlert;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_comments, container, false);

        mAddCommentAlert = initAddCommentAlert();
        initAddCommentButton(rootView, mAddCommentAlert);


        return rootView;
    }

    private AlertDialog.Builder initAddCommentAlert() {
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        alert.setTitle("Add Comment");
        alert.setMessage("Message");

        // Set an EditText view to get the user input
        final EditText input = new EditText(getActivity());
        alert.setView(input);

        alert.setPositiveButton("Send", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //TODO send comment to the live object
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });

        return alert;

    }

    private void initAddCommentButton(View rootView, final AlertDialog.Builder addCommentAlert) {
        Button addCommentButton = (Button) rootView.findViewById(R.id.addCommentButton);
        addCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addCommentAlert.show();

            }
        });

    }


}
