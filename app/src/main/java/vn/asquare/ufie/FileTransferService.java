package vn.asquare.ufie;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by hungmai on 07/04/2016.
 */
public class FileTransferService {

    public static boolean copyFile(final InputStream inputStream, final OutputStream out) {
        byte buf[] = new byte[1024];
        int len;

        try {
            while ((len = inputStream.read(buf)) != -1) {
                out.write(buf, 0, len);
            }

            inputStream.close();
            out.close();

        } catch (IOException e) {

        }
        return true;
    }
}
