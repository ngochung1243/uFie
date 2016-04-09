package vn.asquare.ufie;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by hungmai on 07/04/2016.
 */
public class P2PHandleNetwork implements WifiP2pManager.ConnectionInfoListener, ServerSendSocket_Thread.ServerSocketListener {

    private static final int SOCKET_TIMEOUT = 5000;

    Context mContext;

    ServerReceiveSocket_Thread serverReceiveThread;
    ServerSendSocket_Thread serverSendThread;
    Socket mSendSocket;
    Socket mReceiveSocket;
    public P2PHandleNetworkListener mListener;

    public P2PHandleNetwork(Context context){
        mContext = context;
    }

    public void send(final InputStream is){
        try {
            final OutputStream os = mSendSocket.getOutputStream();
            FileTransferService.copyFile(is, os);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void disconnect(){
        if (serverSendThread != null){
            serverSendThread.stop();
            serverReceiveThread.stop();

            serverSendThread = null;
            serverReceiveThread = null;
        }

        mSendSocket = null;
        mReceiveSocket = null;
    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo info) {
        // TODO Auto-generated method stub
        if (info.groupOwnerAddress == null){
            return;
        }
        final String hostIP = info.groupOwnerAddress.getHostAddress();

        if (info.groupFormed){
            if (info.isGroupOwner){
                try {
                    if (serverSendThread == null && serverReceiveThread == null){
                        serverReceiveThread = new ServerReceiveSocket_Thread(this);
                        serverReceiveThread.start();

                        serverSendThread = new ServerSendSocket_Thread(this);
                        serverSendThread.start();
                    }
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    Toast.makeText(mContext.getApplicationContext(), "Error of create", Toast.LENGTH_SHORT).show();
                }
            }else {
                mSendSocket = new Socket();
                mReceiveSocket = new Socket();

                Runnable runnable = new Runnable() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        try {
                            mReceiveSocket.bind(null);
                            mReceiveSocket.connect(new InetSocketAddress(hostIP, ServerSendSocket_Thread.PORT), SOCKET_TIMEOUT);

                            mSendSocket.bind(null);
                            mSendSocket.connect(new InetSocketAddress(hostIP, ServerReceiveSocket_Thread.PORT), SOCKET_TIMEOUT);
                            ReceiveSocketAsync receiveThread = new ReceiveSocketAsync((ReceiveSocketAsync.SocketReceiverDataListener)mContext, mReceiveSocket, mContext);
                            receiveThread.start();
                            mListener.onConnectComplete();

                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();

                        }

                    }
                };
                Thread connect = new Thread(runnable);
                connect.start();
            }
        }
    }

    @Override
    public void onReceive_SendSocket(Socket sendSocket) {
        // TODO Auto-generated method stub
        mSendSocket = sendSocket;

    }

    @Override
    public void onReceive_ReceiveSocket(Socket receiveSocket) {
        // TODO Auto-generated method stub
        mReceiveSocket = receiveSocket;
        ReceiveSocketAsync receiveThread = new ReceiveSocketAsync((ReceiveSocketAsync.SocketReceiverDataListener)mContext, receiveSocket, mContext);
        receiveThread.start();
        mListener.onConnectComplete();
    }

    public interface P2PHandleNetworkListener{
        public void onConnectComplete();
    }
}