package com.example.androidchat;
//The import statments
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

import android.view.View;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;
import android.text.method.ScrollingMovementMethod;


public class ChatActivity extends Activity {
	//the fields
	private TextView heading;
    private TextView recvTxt;
    private EditText sendTxt;
    private Button sendBtn;
    private Chat theChat;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);
		
	// Getting the heading element
    heading = (TextView) findViewById(R.id.textviewHead1);
    
    // Getting the box where text will be received
    //   and making it scrollable
    recvTxt = (TextView) findViewById(R.id.textviewReceived);
    recvTxt.setMovementMethod(new ScrollingMovementMethod());

    // Getting the box where text will be sent from
    sendTxt = (EditText) findViewById(R.id.edittextSent);

    // Getting the button that will send the text to the server
    sendBtn = (Button) findViewById(R.id.sendButton);
    sendBtn.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
            if (theChat.chatStarted()) {
                String sendStr = sendTxt.getText().toString().trim();
                sendTxt.setText("");
                if (sendStr.trim().length() > 0)
                    theChat.addOutgoing(sendStr);
            }
        }
    });

    theChat = new Chat(this,heading,recvTxt);
    if (theChat.chatStarted())
        return;
    
    // Initializing the chat
    theChat.initChat();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.chat, menu);
		return true;
	}

}
