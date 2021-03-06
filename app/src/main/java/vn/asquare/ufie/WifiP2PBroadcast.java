package vn.asquare.ufie;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;
import android.widget.Toast;

import java.io.InputStream;
import java.lang.reflect.Method;

/**
 * Created by hungmai on 07/04/2016.
 */
public class WifiP2PBroadcast extends BroadcastReceiver implements WifiP2pManager.PeerListListener, P2PHandleNetwork.P2PHandleNetworkListener, WifiP2pManager.ChannelListener {

    WifiP2pManager mManager = null;
    WifiP2pManager.Channel mChannel = null;

    public Context mContext;

    public WifiP2PBroadcastListener mListener = null;

    public WifiP2pDevice mDevice;

    P2PHandleNetwork mP2PHandle;

    public WifiP2PBroadcast(MainActivity activity){
        mContext = activity;

        mP2PHandle = new P2PHandleNetwork();
        mP2PHandle.mListener = this;
    }

    public void setManager() {

        mManager = (WifiP2pManager) mContext.getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(mContext, mContext.getMainLooper(), this);
    }

    public void register(IntentFilter filter){

        mContext.registerReceiver(this, filter);
    }

    public void advertiseWifiP2P() {

        mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                // TODO Auto-generated method stub

            }

            @Override
            public void onFailure(int reason) {
                // TODO Auto-generated method stub

            }
        });
    }

    public void createGroup(){
        mManager.createGroup(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onFailure(int reason) {

            }
        });
    }

    public void removeGroup(){
        mManager.removeGroup(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailure(int reason) {

            }
        });
    }

    public void connectPeer(WifiP2pConfig config){
        mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                // TODO Auto-generated method stub
            }

            @Override
            public void onFailure(int reason) {
                // TODO Auto-generated method stub

            }
        });
    }

    public void disconnectFromPeer() {

        mManager.removeGroup(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailure(int reason) {

            }
        });

        //deletePersistentGroups();

        mP2PHandle.disconnect();
    }

    private void deletePersistentGroups(){
        try {
            Method[] methods = WifiP2pManager.class.getMethods();
            for (int i = 0; i < methods.length; i++) {
                if (methods[i].getName().equals("deletePersistentGroup")) {
                    // Delete any persistent group
                    for (int netid = 0; netid < 32; netid++) {
                        methods[i].invoke(mManager, mChannel, netid, null);
                    }
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub
        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            // Check to see if Wi-Fi is enabled and notify appropriate activity

            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED){

            }
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            // Call WifiP2pManager.requestPeers() to get a list of current peers
            Toast.makeText(mContext.getApplicationContext(), "Peer change", Toast.LENGTH_SHORT).show();
            mManager.requestPeers(mChannel, this);
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            // Respond to new connection or disconnections

            NetworkInfo networkInfo = (NetworkInfo)intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
            Toast.makeText(mContext.getApplicationContext(), "Connection change", Toast.LENGTH_SHORT).show();

//            NetworkInfo.State state = networkInfo.getState();
//            if (state == NetworkInfo.State.CONNECTED){
//
//                mManager.requestConnectionInfo(mChannel, (WifiP2pManager.ConnectionInfoListener)mP2PHandle);
//
//            }else if (state == NetworkInfo.State.DISCONNECTED){
//                if (mP2PHandle.isGroupOwner){
//                    mP2PHandle.checkConnection();
//                }else{
//                    mListener.onDisconnect();
//                }
//            }

            mP2PHandle.checkConnection();

        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            // Respond to this device's wifi state changing
            mDevice = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);

        } else if (WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION.equals(action)){
            //Toast.makeText(mActitity, "Discovery start", Toast.LENGTH_SHORT).show();

            int discoverState = intent.getIntExtra(WifiP2pManager.EXTRA_DISCOVERY_STATE, -1);

            if (discoverState == WifiP2pManager.WIFI_P2P_DISCOVERY_STARTED){
                Toast.makeText(mContext.getApplicationContext(), "Discovery start", Toast.LENGTH_SHORT).show();
            }else if (discoverState == WifiP2pManager.WIFI_P2P_DISCOVERY_STOPPED){
                Toast.makeText(mContext.getApplicationContext(), "Discovery stop", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onPeersAvailable(WifiP2pDeviceList peers) {
        // TODO Auto-generated method stub
        if (mListener != null){
            mListener.onPeers(peers);
        }
    }

    public void send(InputStream is){
        mP2PHandle.send(is);
    }

    @Override
    public void onConnectComplete() {
        // TODO Auto-generated method stub
        if (mListener != null){
            mListener.onConnection();
        }
    }

    @Override
    public void onDisconnectComplete() {
        mListener.onDisconnect();
        if(mP2PHandle.checkEmptyConnectionPeers()){
            //deletePersistentGroups();
        }
        Log.d("Disconnect", "Disconnected!!!");
    }

    @Override
    public void checkPingComplete(boolean isOK) {
        if (isOK){
            mManager.requestConnectionInfo(mChannel, (WifiP2pManager.ConnectionInfoListener)mP2PHandle);
        }
    }

    @Override
    public void onChannelDisconnected() {
        Toast.makeText(mContext, "On channel", Toast.LENGTH_SHORT).show();
        setManager();
    }

    public interface WifiP2PBroadcastListener{
        public void onPeers(WifiP2pDeviceList peers);
        public void onConnection();
        public void onDisconnect();
    }
}