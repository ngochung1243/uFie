package vn.asquare.ufie;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by hungmai on 07/04/2016.
 */
public class ReceiveSocketAsync implements Runnable{

    static final int PORT = 9000;

    Socket mReceiveSocket;

    public SocketReceiverDataListener mReceiveListener;

    onReceivedDataListener mReceivedDataListener;

    Thread t;

    public ReceiveSocketAsync(onReceivedDataListener receivedDataListener, SocketReceiverDataListener receiveListener, Socket receiveSocket) {
        // TODO Auto-generated constructor stub
        mReceivedDataListener = receivedDataListener;
        mReceiveSocket = receiveSocket;
        mReceiveListener = receiveListener;
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        try {
            InputStream receiveInputStream = mReceiveSocket.getInputStream();
            OutputStream receivedOutputStream = mReceiveSocket.getOutputStream();

            while (true){
                if (mReceiveSocket.isClosed()){
                    break;
                }

                ByteArrayOutputStream os = new ByteArrayOutputStream();

                int received = FileTransferService.receiveFile(receiveInputStream, os);
                if (received == 1){
                    mReceiveListener.onCompleteSendData();
                }else if (received == 0){
                    os.flush();

                    if (os.size() > 0){
                        if (mReceiveListener != null){
                            mReceiveListener.onReceiveData(os.toByteArray());

                            mReceivedDataListener.onCompleteReceivedData();
                        }
                    }
                }
            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void start(){
        t = new Thread(this);
        t.start();
    }

    public void stop(){
        t.interrupt();
    }

    public interface SocketReceiverDataListener{
        public void onReceiveData(byte[] data);
        public void onCompleteSendData();
    }

    public interface onReceivedDataListener{
        public void onCompleteReceivedData();
    }
}