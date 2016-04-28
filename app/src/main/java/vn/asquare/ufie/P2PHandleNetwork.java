package vn.asquare.ufie;

import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by hungmai on 07/04/2016.
 */
public class P2PHandleNetwork implements WifiP2pManager.ConnectionInfoListener, ServerSendSocket_Thread.ServerSocketListener, ReceiveSocketAsync.onReceivedDataListener {

    private static final int SOCKET_TIMEOUT = 5000;

    ServerReceiveSocket_Thread serverReceiveThread;
    ServerSendSocket_Thread serverSendThread;
    List<Socket> mSendSockets = new ArrayList<>();
    List<Socket> mReceiveSockets = new ArrayList<>();

    ReceiveSocketAsync receiveThread;

    public P2PHandleNetworkListener mListener;
    public ReceiveSocketAsync.SocketReceiverDataListener mReceiveDataListener;

    public P2PHandleNetwork(){
        mReceiveDataListener = null;
    }

    public void setReceiveDataListener(ReceiveSocketAsync.SocketReceiverDataListener listener){
        mReceiveDataListener = listener;
        receiveThread.mReceiveListener = listener;
    }

    public void send(final InputStream is){
        try {
            for (Socket mSendSocket:mSendSockets) {
                if (!mSendSocket.isClosed()){
                    final OutputStream os = mSendSocket.getOutputStream();
                    FileTransferService.sendFile(is, os);
                }
            }

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

        closeAllSockets();
    }

    private void closeAllSockets(){
        for(Socket mSendSocket:mSendSockets){
            try {
                mSendSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        for(Socket mReceiveSocket:mReceiveSockets) {
            try {
                mReceiveSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        mSendSockets.clear();
        mReceiveSockets.clear();
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

                    MainActivity.mState = MainActivity.State.StateActive;

                    if (serverSendThread == null && serverReceiveThread == null){
                        serverReceiveThread = new ServerReceiveSocket_Thread(this);
                        serverReceiveThread.start();

                        serverSendThread = new ServerSendSocket_Thread(this);
                        serverSendThread.start();
                    }
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                }
            }else {

                MainActivity.mState = MainActivity.State.StatePassive;

                final Socket mSendSocket = new Socket();
                final Socket mReceiveSocket = new Socket();

                final int position = mReceiveSockets.size();

                receiveThread = new ReceiveSocketAsync(this, mReceiveDataListener, mReceiveSocket, position);

                mSendSockets.add(mSendSocket);
                mReceiveSockets.add(mReceiveSocket);

                Runnable runnable = new Runnable() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        try {
                            mReceiveSocket.bind(null);
                            mReceiveSocket.connect(new InetSocketAddress(hostIP, ServerSendSocket_Thread.PORT), SOCKET_TIMEOUT);

                            mSendSocket.bind(null);
                            mSendSocket.connect(new InetSocketAddress(hostIP, ServerReceiveSocket_Thread.PORT), SOCKET_TIMEOUT);

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

    private void sendCodeReceivedData(OutputStream os){
        String receivedCode = "250 ";
        InputStream stream = new ByteArrayInputStream(receivedCode.getBytes(StandardCharsets.UTF_8));
        FileTransferService.sendCode(stream, os);
    }

    @Override
    public void onReceive_SendSocket(Socket sendSocket) {
        // TODO Auto-generated method stub
        Socket mSendSocket = sendSocket;

        mSendSockets.add(mSendSocket);
    }

    @Override
    public void onReceive_ReceiveSocket(Socket receiveSocket) {
        // TODO Auto-generated method stub
        Socket mReceiveSocket = receiveSocket;

        int position = mReceiveSockets.size();

        mReceiveSockets.add(mReceiveSocket);

        receiveThread = new ReceiveSocketAsync(this, mReceiveDataListener, receiveSocket, position);
        receiveThread.start();

        mListener.onConnectComplete();
    }

    @Override
    public void onCompleteReceivedData(int peer) {
        try {
            sendCodeReceivedData(mSendSockets.get(peer).getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public interface P2PHandleNetworkListener{
        public void onConnectComplete();
    }
}