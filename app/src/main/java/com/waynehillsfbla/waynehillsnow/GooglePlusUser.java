package com.waynehillsfbla.waynehillsnow;

import android.view.View;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.plus.People;

/**
 * Created by Entity on 4/1/2015.
 */
public abstract class GooglePlusUser implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        ResultCallback<People.LoadPeopleResult>, View.OnClickListener {


    private GoogleApiClient mGoogleApiClient;



}
