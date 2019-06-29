package com.example.smi;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.DefaultLogger;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterAuthToken;
import com.twitter.sdk.android.core.TwitterConfig;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;
import com.twitter.sdk.android.core.models.User;

import retrofit2.Call;

public class TwitterLogin extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private TwitterLoginButton twitterLoginButton;

    private ImageView userProfileImageView;
    private TextView userDetailsLabel;


    //twitter auth client required for custom login
    private TwitterAuthClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Twitter.initialize(this);

        setContentView(R.layout.activity_twitter_login);
        try {
            client.cancelAuthorize();
        }catch (Exception e){

        }


        client = new TwitterAuthClient();

        //find the id of views
        twitterLoginButton = findViewById(R.id.default_twitter_login_button);

        userProfileImageView = findViewById(R.id.user_profile_image_view);
        userDetailsLabel = findViewById(R.id.user_details_label);

        //NOTE : calling default twitter login in OnCreate/OnResume to initialize twitter callback
        defaultLoginTwitter();

    }

    /**
     * method to do Default Twitter Login
     */
    public void defaultLoginTwitter() {
        //check if user is already authenticated or not
        if (getTwitterSession() == null) {

            //if user is not authenticated start authenticating
            twitterLoginButton.setCallback(new Callback<TwitterSession>() {
                @Override
                public void success(Result<TwitterSession> result) {

                    // Do something with result, which provides a TwitterSession for making API calls
                    TwitterSession twitterSession = result.data;

                    //call fetch email only when permission is granted
                    fetchTwitterEmail(twitterSession);

                }

                @Override
                public void failure(TwitterException exception) {
                    // Do something on failure
                    Toast.makeText(TwitterLogin.this, "Failed to authenticate. Please try again.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {

            //if user is already authenticated direct call fetch twitter email api
            Toast.makeText(this, "User already authenticated", Toast.LENGTH_SHORT).show();
            fetchTwitterEmail(getTwitterSession());
        }
    }

    public void customLoginTwitter(View view) {
        //check if user is already authenticated or not
        if (getTwitterSession() == null) {

            //if user is not authenticated start authenticating
            client.authorize(this, new Callback<TwitterSession>() {
                @Override
                public void success(Result<TwitterSession> result) {

                    // Do something with result, which provides a TwitterSession for making API calls
                    TwitterSession twitterSession = result.data;

                    //call fetch email only when permission is granted
                    fetchTwitterEmail(twitterSession);
                }

                @Override
                public void failure(TwitterException e) {
                    // Do something on failure
                    Toast.makeText(TwitterLogin.this, "Failed to authenticate. Please try again.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            //if user is already authenticated direct call fetch twitter email api
            Toast.makeText(this, "User already authenticated", Toast.LENGTH_SHORT).show();
            fetchTwitterEmail(getTwitterSession());
        }
    }

    /**
     * Before using this feature, ensure that “Request email addresses from users” is checked for your Twitter app.
     *
     * @param twitterSession user logged in twitter session
     */
    public void fetchTwitterEmail(final TwitterSession twitterSession) {
        client.requestEmail(twitterSession, new Callback<String>() {
            @Override
            public void success(Result<String> result) {
                //here it will give u only email and rest of other information u can get from TwitterSession
                userDetailsLabel.setText("User Id : " + twitterSession.getUserId() + "\nScreen Name : " + twitterSession.getUserName() + "\nEmail Id : " + result.data);
            }

            @Override
            public void failure(TwitterException exception) {
                Toast.makeText(TwitterLogin.this, "Failed to authenticate. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * call Verify Credentials API when Twitter Auth is successful else it will go in exception block
     * this metod will provide you User model which contain all user information
     *
     * @param view calling view
     */
    public void fetchTwitterImage(View view) {
        //check if user is already authenticated or not
        if (getTwitterSession() != null) {

            //fetch twitter image with other information if user is already authenticated

            //initialize twitter api client
            TwitterApiClient twitterApiClient = TwitterCore.getInstance().getApiClient();

            //Link for Help : https://developer.twitter.com/en/docs/accounts-and-users/manage-account-settings/api-reference/get-account-verify_credentials

            //pass includeEmail : true if you want to fetch Email as well
            Call<User> call = twitterApiClient.getAccountService().verifyCredentials(true, false, true);
            call.enqueue(new Callback<User>() {
                @Override
                public void success(Result<User> result) {
                    User user = result.data;
                    userDetailsLabel.setText("User Id : " + user.id + "\nUser Name : " + user.name + "\nEmail Id : " + user.email + "\nScreen Name : " + user.screenName);

                    String imageProfileUrl = user.profileImageUrl;
                    Log.e(TAG, "Data : " + imageProfileUrl);
                    //NOTE : User profile provided by twitter is very small in size i.e 48*48
                    //Link : https://developer.twitter.com/en/docs/accounts-and-users/user-profile-images-and-banners
                    //so if you want to get bigger size image then do the following:
                    imageProfileUrl = imageProfileUrl.replace("_normal", "");

                    ///load image using Picasso
                    Picasso.with(TwitterLogin.this)
                            .load(imageProfileUrl)
                            .placeholder(R.mipmap.ic_launcher_round)
                            .into(userProfileImageView);
                }

                @Override
                public void failure(TwitterException exception) {
                    Toast.makeText(TwitterLogin.this, "Failed to authenticate. Please try again.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            //if user is not authenticated first ask user to do authentication
            Toast.makeText(this, "First to Twitter auth to Verify Credentials.", Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * get authenticates user session
     *
     * @return twitter session
     */
    private TwitterSession getTwitterSession() {
        TwitterSession session = TwitterCore.getInstance().getSessionManager().getActiveSession();

        //NOTE : if you want to get token and secret too use uncomment the below code
        /*TwitterAuthToken authToken = session.getAuthToken();
        String token = authToken.token;
        String secret = authToken.secret;*/

        return session;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Pass the activity result to the twitterAuthClient.
        if (client != null)
            client.onActivityResult(requestCode, resultCode, data);

        // Pass the activity result to the login button.
        twitterLoginButton.onActivityResult(requestCode, resultCode, data);
    }


}