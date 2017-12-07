package com.example.androidchat;
import p2p.*;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/*
 * Course: CS 2302
 * Section: 02
 * Name: Nidhi Shah
 * Professor: Shaw	
 * Assignment #: Lab 19
 */
@SuppressLint("HandlerLeak")
public class Chat extends View {
    // Global data
    private boolean chatStarted = false;
    private P2PAndroid p2p = null;
    private String usernameStr = "";
    private String othernameStr = "";
    private String receiveStr = "";

    // Heading of chat and the main box where the chat converstation takes place
    private TextView heading;
    private TextView txtrecv;
    
    // EditText objects for username and opponent's username
    private EditText usernameText;
    private EditText othernameText;

    // AlertDialog to get username and opponent's username
    private AlertDialog alertDialog;

    // Button objects
    private Button btnOK;
    private Button btnCancel;

    // Loading the strings
    private String establishCommStr = getContext().getString(R.string.heading1b);
    private String converseWithStr = getContext().getString(R.string.heading1c);
    private String youSaidStr = getContext().getString(R.string.yousaidtext);
    private String otherSaidStr = getContext().getString(R.string.othersaidtext);
    private String quitStr = getContext().getString(R.string.quittext);
    private String messSentStr = getContext().getString(R.string.message_sent);
    
    // Handles various Handler messages:
    //   1 => receives incoming message
    //   2 => exits the chat
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case 1: // Get incoming messages
                new ReceiveP2P().execute();
                break;

            case 2: // Exits the chat
                System.exit(0);
                break;
            }
            super.handleMessage(msg);
        }
    };
    
    // The constructor
    public Chat(Context context) {
        super(context);
        
        heading = null;
        txtrecv = null;
    }
    
    // The constructor
    public Chat(Context context, TextView hdView, TextView txtView) {
        super(context);
        
        heading = hdView;
        txtrecv = txtView;
        
        addIncoming(receiveStr);
        
        if (chatStarted && p2p != null) {
            heading.setText(converseWithStr + " " + othernameStr + ":");
        }
    }

    // Returns true if the chat has started, and false otherwise
    public boolean chatStarted() {
        return chatStarted;
    }
    
    // Creates a dialog box that gets the user's name and his opponents name
    public void initChat() {
        LayoutInflater li = LayoutInflater.from(getContext());
        View promptsView = li.inflate(R.layout.prompts, null);
        
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
        alertDialogBuilder.setView(promptsView).setCancelable(false);

        usernameText = (EditText) promptsView
                      .findViewById(R.id.editTextDialogUserName);
        othernameText = (EditText) promptsView
                      .findViewById(R.id.editTextDialogOtherUserName);

        btnOK = (Button) promptsView.findViewById(R.id.okButton);
        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                usernameStr = usernameText.getText().toString().trim();
                othernameStr = othernameText.getText().toString().trim();
                if (usernameStr.length() == 0) {
                    usernameText.requestFocus();
                    usernameText.setError( getContext().getString(R.string.usernamereqtext) );
                } else if (othernameStr.length() == 0) {
                    othernameText.requestFocus();
                    othernameText.setError( getContext().getString(R.string.othernamereqtext) );
                }
                if (usernameStr.length() > 0 && othernameStr.length() > 0)  {
                    alertDialog.dismiss();
                }
            }
        });

        btnCancel = (Button) promptsView.findViewById(R.id.cancelButton);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.exit(0);
            }
        });

        alertDialog = alertDialogBuilder.create();
        alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                heading.setText(establishCommStr);
                invalidate();
                new StartP2P().execute();
            }
        });
        
        alertDialog.show();
    }

    // This asynchronous task starts the P2P session and when finished, in the
    // onPostExecute method it puts "Online" in the status textbox if successful.
    // It also enables the Load and Send buttons when the connection is made.
    private class StartP2P extends AsyncTask<Void, Void, String> {
       @Override
       protected String doInBackground(Void... params) {
          p2p = new P2PAndroid(usernameStr);    // start the p2p session
          String err = p2p.getErrorStatus();
          if (err == null || err.length() == 0) {
                 p2p.talkTo(othernameStr);         // determine who to talk to
              err = p2p.getErrorStatus();
          }
          return err;          // return any error message
       }

       @Override
       protected void onPostExecute(String statusMess) {
          // The P2P session started successfully if no error message
          if (statusMess == null || statusMess.equals("")) {
              chatStarted = true;
                handler.sendMessage(Message.obtain(handler, 1));
              heading.setText(converseWithStr + " " + othernameStr + ":");
          }
          else
             Toast.makeText(getContext(),statusMess,Toast.LENGTH_SHORT).show();
       }
    }

    // This asynchronous task attempts to send the byte array of the current image
    // to the P2P server.  If it succeeds then the onPostExecute method will
    // announce that the image was sent.
    private class SendP2P extends AsyncTask<String, Void, String> {
       @Override
       protected String doInBackground(String... params) {
          // Sending bytes to server
          p2p.sendString(params[0]);
          String errMess = p2p.getErrorStatus();
          if (errMess == null || errMess.equals(""))
             return messSentStr;
          return errMess;
       }
         
       @Override
       protected void onPostExecute(String result) {
          Toast.makeText(getContext(),result,Toast.LENGTH_SHORT).show();
       }
    }

    // This asynchronous task attempts to receive the byte array that stores
    // image data from the P2P server.  If it succeeds then the onPostExecute
    // method will will replace the lower image with the bytes that were received.
    private class ReceiveP2P extends AsyncTask<Void, Void, String> {
       @Override
       protected String doInBackground(Void... params) {
           return p2p.receiveString(false);
       }

       @Override
       protected void onPostExecute(String result) {
            if (result != null && result.trim().length() > 0) {
                addIncoming(othernameStr + " " + otherSaidStr + " " + result);
                   if (result.trim().equalsIgnoreCase(quitStr)) {
                       handler.sendMessageDelayed(Message.obtain(handler, 2),1000);
                   } else
                       handler.sendMessageDelayed(Message.obtain(handler, 1),1000);
                String errMess = p2p.getErrorStatus();
                // checking for an error message
                if (errMess != null && !errMess.equals(""))
                    Toast.makeText(getContext(),errMess,Toast.LENGTH_LONG).show();
            } else
                handler.sendMessageDelayed(Message.obtain(handler, 1),1000);
       }
    }

    // Adds input to the main conversation box
    //   - must be called on the UI thread
    private void addIncoming(String incomingLine) {
        String rcvStr = txtrecv.getText().toString();
        if (rcvStr.length() > 0)
            incomingLine = "\n\n" + incomingLine;

        txtrecv.append(incomingLine);
        receiveStr = txtrecv.getText().toString();

        final Layout layout = txtrecv.getLayout();
        if (layout != null) {
            int scrollDelta = layout.getLineBottom(txtrecv.getLineCount() - 1) 
                                    - txtrecv.getScrollY() - txtrecv.getHeight();
            if (scrollDelta > 0)
                txtrecv.scrollBy(0, scrollDelta);
        }
    }

    // Sends an outgoing message to the other user
    public void addOutgoing(String outgoingLine) {
        if (outgoingLine.length() > 0) {
            addIncoming("[ " + youSaidStr + " " + outgoingLine + " ]");
            new SendP2P().execute(outgoingLine);
            
            if (outgoingLine.trim().equalsIgnoreCase(quitStr)) {
                handler.sendMessageDelayed(Message.obtain(handler, 2),1000);
            }
        }
    }
}