package experties.com.handytask.activities;

        import android.content.Intent;
        import android.graphics.Bitmap;
        import android.graphics.BitmapFactory;
        import android.support.v7.app.ActionBarActivity;
        import android.os.Bundle;
        import android.util.Log;
        import android.view.Menu;
        import android.view.MenuItem;
        import android.view.View;
        import android.widget.Button;
        import android.widget.EditText;
        import android.widget.ImageView;
        import android.widget.ListView;
        import android.widget.TextView;
        import android.widget.Toast;

        import com.parse.GetCallback;
        import com.parse.ParseException;
        import com.parse.ParseQuery;
        import com.parse.ParseUser;
        import com.pubnub.api.*;
        import org.json.*;

        import java.util.ArrayList;

        import experties.com.handytask.R;
        import experties.com.handytask.adapters.ChatListAdapter;
        import experties.com.handytask.models.ChatMessage;
        import experties.com.handytask.models.ParseTask;


public class ChatActivity extends ActionBarActivity {
    public static final String CHAT_DIV = "|||";

    private Pubnub pubnub;
    private String otherUserPhoneNumber;
    private String thisUsername; // local user's username
    private String otherUsername; // username of the person you are chatting with
    private boolean thisUserIsSeller;
    private String chatChannel; // name of the chat channel, made from the two usernames.
    private byte[] thisUserPhoto;
    private byte[] otherUserPhoto;
    private String taskId;
    private ParseTask task;

    private ListView lvChat;
    private ArrayList<ChatMessage> mMessages;
    private ChatListAdapter mAdapter;
    private EditText etMessageToSend;
    private Button btnSend;
    private Button btnAcceptOffer;
    private Button btnDeclineOffer;
    private TextView tvOtherUserUsername;
    private ImageView ivOtherUserPhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        pubnub = new Pubnub("pub-c-b0ac15ff-9430-4b40-a2f5-919cf57bf1c4", "sub-c-6b77ceae-c35f-11e4-b54a-0619f8945a4f");
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser == null) {
            // if the user is not logged in, start the Login Activity with the "next" extra set.
            Intent i = new Intent(ChatActivity.this, LoginActivity.class);
            i.putExtra("next", "chat");
            startActivity(i);
            return;
        }

        // get extra data from intent
        //taskId = getIntent().getStringExtra("taskId"); // TEMP - For testing, we'll just hardcode this for now.
        taskId = "08a54aQJ30"; // TEMP for testing
        thisUsername =  currentUser.getUsername();

        ParseQuery<ParseTask> taskQuery = ParseQuery.getQuery(ParseTask.class);
        taskQuery.getInBackground(taskId, new GetCallback<ParseTask>() {
            @Override
            public void done(ParseTask parseTask, ParseException e) {
                // Got the task object information. Now we need to get the task owner information.
                if (e == null) {
                    ChatActivity.this.task = parseTask; // store the task
                    ParseQuery<ParseUser> ownerQuery = ParseQuery.getQuery(ParseUser.class);
                    //ownerQuery.getInBackground() // LEFT OFF
                } else {
                    Toast.makeText(ChatActivity.this, getResources().getString(R.string.Unable_to_get_data_from_the_database_), Toast.LENGTH_SHORT).show();
                }
            }
        });

        otherUsername =  getIntent().getStringExtra("otherUsername");
        otherUserPhoneNumber = getIntent().getStringExtra("otherUserPhoneNumber");
        thisUserPhoto = getIntent().getByteArrayExtra("thisUserPhoto");
        otherUserPhoto = getIntent().getByteArrayExtra("otherUserPhoto");

        if (thisUsername.compareTo(otherUsername) <= 0) { // chatChannel is derived from alphabetical order of the two usernames
            chatChannel = thisUsername + CHAT_DIV + otherUsername;
        } else {
            chatChannel = otherUsername + CHAT_DIV + thisUsername;
        }

        etMessageToSend = (EditText) findViewById(R.id.etMessageToSend);
        btnSend = (Button) findViewById(R.id.btnSend);
        tvOtherUserUsername = (TextView) findViewById(R.id.tvOtherUserUsername);
        ivOtherUserPhoto = (ImageView) findViewById(R.id.ivOtherUserPhoto);
        btnAcceptOffer = (Button) findViewById(R.id.btnAcceptOffer);
        btnDeclineOffer = (Button) findViewById(R.id.btnDeclineOffer);

        tvOtherUserUsername.setText(otherUsername);
        //ivTODO.setImageBitmap(BitmapFactory.decodeByteArray(thisUserPhoto, 0, thisUserPhoto.length));
        //ivOtherUserPhoto.setImageBitmap(BitmapFactory.decodeByteArray(otherUserPhoto, 0, otherUserPhoto.length));

        lvChat = (ListView) findViewById(R.id.lvChat);
        mMessages = new ArrayList<ChatMessage>();
        mAdapter = new ChatListAdapter(ChatActivity.this, thisUsername, mMessages);
        lvChat.setAdapter(mAdapter);

        subscribeToChannel();
        refreshAndPopulateFromHistory();
    }

    public void subscribeToChannel() {
        // Subcribe to the channel
        try {
            pubnub.subscribe(chatChannel, new Callback() {
                        @Override
                        public void connectCallback(String channel, Object message) {
                            Log.e("XXXXXXXXXXXX", "SUBSCRIBE : CONNECT on channel:" + channel
                                    + " : " + message.getClass() + " : "
                                    + message.toString());
                        }

                        @Override
                        public void disconnectCallback(String channel, Object message) {
                            Log.e("XXXXXXXXXXXX", "SUBSCRIBE : DISCONNECT on channel:" + channel
                                    + " : " + message.getClass() + " : "
                                    + message.toString());
                        }

                        public void reconnectCallback(String channel, Object message) {
                            Log.e("XXXXXXXXXXXX", "SUBSCRIBE : RECONNECT on channel:" + channel
                                    + " : " + message.getClass() + " : "
                                    + message.toString());
                        }

                        @Override
                        public void successCallback(String channel, Object message) {
                            Log.e("XXXXXXXXXXXX", "SUBSCRIBE : " + channel + " : "
                                    + message.getClass() + " : " + message.toString());
                            addChatMessage((String)message);
                        }

                        @Override
                        public void errorCallback(String channel, PubnubError error) {
                            Log.e("XXXXXXXXXXXX", "SUBSCRIBE : ERROR on channel " + channel
                                    + " : " + error.toString());
                        }
                    }
            );
        } catch (PubnubException e) {
            e.printStackTrace();
        }
    }

    public void onSendClick(View v) {
        Callback callback = new Callback() {
            public void successCallback(String channel, Object response) {
                Log.e("XXXXXXXXXXXX",response.toString());
            }
            public void errorCallback(String channel, PubnubError error) {
                Log.e("XXXXXXXXXXXX",error.toString());
            }
        };
        pubnub.publish(this.chatChannel, thisUsername + CHAT_DIV + etMessageToSend.getText().toString() , callback);
        etMessageToSend.setText(""); // blank out text field
    }

    public void refreshAndPopulateFromHistory() {
        Callback callback = new Callback() {
            public void successCallback(String channel, Object response) {
                Log.e("XXXXXXXXXXXX", "SUCCESS HISTORY " + response.toString());
                try {
                    for (int i = 0; i < ((JSONArray) response).getJSONArray(0).length(); i++) {
                        String message = ((JSONArray) response).getJSONArray(0).getString(i);
                        addChatMessage(message);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
            public void errorCallback(String channel, PubnubError error) {
                Log.e("XXXXXXXXXXXX", "ERROR HISTORY " + error.toString());
            }
        };

        mMessages.clear();
        pubnub.history(chatChannel, 100, callback);
    }

    public void addChatMessage(String message) {
        final String msg = message;

        // the list view can only be updated from the UI thread, so put this code in a Runnable.
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String username;
                String text;
                username = msg.substring(0, msg.indexOf(CHAT_DIV));
                text = msg.substring(msg.indexOf(CHAT_DIV) + CHAT_DIV.length());

                mMessages.add(new ChatMessage(username, text));
                mAdapter.notifyDataSetChanged();
                lvChat.invalidate(); // redraw listview
            }
        });
    }



    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }*/


    public void onDeclineOfferClick(View v) {
        Intent i = new Intent();
        i.putExtra("offerResponse", "decline");
        setResult(RESULT_OK, i);
        finish();
    }

    public void onAcceptOfferClick(View v) {
        Intent i = new Intent();
        i.putExtra("offerResponse", "accept");
        setResult(RESULT_OK, i);
        finish();
    }
}
