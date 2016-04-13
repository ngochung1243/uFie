package vn.asquare.ufie;

import android.content.Context;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

/**
 * Created by hungmai on 07/04/2016.
 */
public class ReceiveSocketAsync implements Runnable{

    static final int PORT = 9000;

    Socket mReceiveSocket;

    SocketReceiverDataListener mReceiveListener;

    Context mContext;

    Thread t;

    public ReceiveSocketAsync(SocketReceiverDataListener receiveListener, Socket receiveSocket, Context context) {
        // TODO Auto-generated constructor stub
        mReceiveSocket = receiveSocket;
        mReceiveListener = receiveListener;
        mContext = context;
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        try {
            InputStream receiveInputStream = mReceiveSocket.getInputStream();
            while (true){
                if (mReceiveSocket.isClosed()){
                    break;
                }

                ByteArrayOutputStream os = new ByteArrayOutputStream();

                FileTransferService.receiveFile(receiveInputStream, os);

                os.flush();

                if (os.size() > 0){
                    ((SocketReceiverDataListener)mContext).onReceiveData(os.toByteArray());
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
    }
}