package vn.asquare.ufie;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class MainActivity extends AppCompatActivity implements ReceiveSocketAsync.SocketReceiverDataListener, WifiP2PBroardcast.WifiP2PBroadcastListener {

    public static enum State{
        StateDefault,
        StateActive,
        StatePassive
    }

    Context mContext;

    public static WifiP2pManager mManager;
    public static WifiP2pManager.Channel mChannel;
    public static WifiP2PBroardcast mBroadcast;
    public static IntentFilter filter = new IntentFilter();

    ListView lvImage;
    ImageListAdapter lvImageAdapter;
    List<String> image_urls = new ArrayList<String>();

    public boolean firstDiscover = true;
    ProgressDialog mProgess;
    public static State mState;
    String mImagePath;
    String compressPath;
    Uri mImageUri;
    Uri compressUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;
        mProgess = new ProgressDialog(this){
            @Override
            public void onBackPressed() {
                if (mProgess.isShowing()){
                    mProgess.dismiss();
                }
            };
        };

        mState = State.StateDefault;

        lvImage = (ListView)findViewById(R.id.lvImage);
        lvImageAdapter = new ImageListAdapter(this, R.layout.listview_item, image_urls);
        lvImage.setAdapter(lvImageAdapter);
        lvImage.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // TODO Auto-generated method stub
                Intent imageViewIntent = new Intent(getApplicationContext(), ShowImageActivity.class);
                imageViewIntent.putExtra("ImagePath", image_urls.get(position));
                startActivity(imageViewIntent);
            }
        });

        setManager();
    }

    private void setManager(){
        mManager = (WifiP2pManager) getSystemService(WIFI_P2P_SERVICE);

        mChannel = mManager.initialize(this, getMainLooper(), null);

        advertiseWifiP2P();

        mBroadcast = new WifiP2PBroardcast(mManager, mChannel, this);

        mBroadcast.mListener = this;

        filter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        filter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        filter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        filter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        filter.addAction(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION);

        registerReceiver(mBroadcast, filter);
    }

    private void advertiseWifiP2P(){
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

    private void disconnectFromPeer(){
        mManager.removeGroup(mChannel, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                // TODO Auto-generated method stub
                mManager.cancelConnect(mChannel, new WifiP2pManager.ActionListener() {

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

            @Override
            public void onFailure(int reason) {
                // TODO Auto-generated method stub

            }
        });
    }

    public class ImageListAdapter extends ArrayAdapter<String> {
        Context mContext;
        int mResource;
        List<String> image_urls;

        public ImageListAdapter(Context context, int resource,
                                List<String> objects) {
            super(context, resource, objects);
            // TODO Auto-generated constructor stub
            mContext = context;
            mResource = resource;
            image_urls = objects;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            View v = convertView;
            LayoutInflater inflater = getLayoutInflater();
            v = inflater.inflate(mResource, null);
            ImageView imageView = (ImageView)v.findViewById(R.id.imvIcon);
            TextView txtView = (TextView)v.findViewById(R.id.tvLabel);
            txtView.setText("image_" + position);

            final int THUMBSIZE = 64;
            Bitmap ThumbImage = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(image_urls.get(position)),
                    THUMBSIZE, THUMBSIZE);

            Bitmap bm = createCorrectBitmap(image_urls.get(position), ThumbImage);

            imageView.setImageBitmap(bm);
            return v;
        }
    }



    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
    }

    private File createImageFile(String prefix, String suffix) throws IOException {
        // Create an image file name
//		 String mprefix = prefix;
//
//		 if (prefix == null){
//			 String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
//			 mprefix = "JPEG_" + timeStamp;
//		 }
//
//		 File storageDir = Environment.getExternalStoragePublicDirectory(
//				 Environment.DIRECTORY_PICTURES);
//		 File image = new File(storageDir, mprefix + suffix);
//
//		 image.createNewFile();

        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(image)));

        // Save a file: path for use with ACTION_VIEW intents
        mImagePath = image.getAbsolutePath();
        mImageUri = Uri.fromFile(image);

        // Save a file: path for use with ACTION_VIEW intents
        return image;
    }

    private File getLastFromDCIM() {
        try {
            //Samsungs:
            File folder = new File(Environment.getExternalStorageDirectory() + File.separator + "DCIM/Camera");
            if(!folder.exists()){ //other phones:
                File[] subfolders = new File(Environment.getExternalStorageDirectory() + File.separator + "DCIM").listFiles();
                for(File subfolder : subfolders){
                    if(subfolder.getAbsolutePath().contains("100")){
                        folder = subfolder;
                        break;
                    }
                }
                if(!folder.exists())
                    return null;
            }

            File[] images = folder.listFiles();
            File latestSavedImage = images[0];
            for (int i = 1; i < images.length; ++i) {
                if (images[i].lastModified() > latestSavedImage.lastModified()) {
                    latestSavedImage = images[i];
                }
            }

            return latestSavedImage;
            //success = latestSavedImage.delete();
            //sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, latestUri));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    public static Bitmap createCorrectBitmap(String filePath, Bitmap oldbm){

        Bitmap newbm;

        int rotate = 0;
        try {
            File imageFile = new File(filePath);
            ExifInterface exif = new ExifInterface(
                    imageFile.getAbsolutePath());
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Matrix matrix = new Matrix();
        matrix.postRotate(rotate);
        newbm = Bitmap.createBitmap(oldbm , 0, 0, oldbm.getWidth(), oldbm.getHeight(), matrix, true);

        return newbm;
    }

    private void compressToGzipFile(String sourceFilePath, String desFilePath){

        byte[] buffer = new byte[1024];

        try{

            GZIPOutputStream gzos =
                    new GZIPOutputStream(new FileOutputStream(sourceFilePath));

            FileInputStream in =
                    new FileInputStream(desFilePath);

            int len;
            while ((len = in.read(buffer)) > 0) {
                gzos.write(buffer, 0, len);
            }

            in.close();

            gzos.finish();
            gzos.close();

        }catch(IOException ex){
            ex.printStackTrace();
        }
    }

    private void decompressFromGzipFile(String sourceFilePath, String desFilePath){
        byte[] buffer = new byte[1024];

        try{

            GZIPInputStream gzis =
                    new GZIPInputStream(new FileInputStream(sourceFilePath));

            FileOutputStream out =
                    new FileOutputStream(desFilePath);

            int len;
            while ((len = gzis.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }

            gzis.close();
            out.close();

        }catch(IOException ex){
            ex.printStackTrace();
        }
    }

    private void sendImageCapture(){
//		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        //intent.setType("image/*");
//        File photoFile = null;
//        try {
//            photoFile = createImageFile(null, ".jpg");
//            if (photoFile != null) {
//            	if (photoFile.exists()){
//            		mImagePath = photoFile.getAbsolutePath();
//            		mImageUri = Uri.fromFile(photoFile);
//                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
//                    startActivityForResult(takePictureIntent, 100);
//            	}
//            }
//        } catch (IOException ex) {
//            // Error occurred while creating the File
//        }

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile(null, null);
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        mImageUri);
                startActivityForResult(takePictureIntent, 100);
            }
        }
    }

    private void sendImageInGalery(){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        if (resultCode == RESULT_OK){
            if (requestCode == 100){

                mProgess.setTitle("Send Picture");
                mProgess.setMessage("Wait for send...");
                mProgess.show();

//				File file = new File(mImagePath);
//				Bitmap bm = null;
//				if (file.exists()) {
//					bm = BitmapFactory.decodeFile(file.getAbsolutePath());
//					//deleteLastFromDCIM();
//				}
//				// final Bitmap bm = (Bitmap) data.getExtras().get("data");
//
//				ByteArrayOutputStream osBm = new ByteArrayOutputStream();
//
//				bm.compress(CompressFormat.PNG, 0, osBm);
//
//				byte[] barray = osBm.toByteArray();
//
//				ByteArrayInputStream isBm = new ByteArrayInputStream(barray);

                //File imageFile = getLastFromDCIM();
                //mImageUri = Uri.fromFile(imageFile);




                Thread send = new Thread(new Runnable() {

                    @Override
                    public void run() {

                        try {
                            ContentResolver cr = getContentResolver();
                            InputStream is;

                            is = cr.openInputStream(mImageUri);
                            mBroadcast.send(is);

                            File image = new File(mImagePath);
                            image.delete();
                            mContext.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(image)));
                            mProgess.dismiss();

                        } catch (FileNotFoundException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                });
                send.start();

//				ContentResolver cr = getContentResolver();
//				cr.notifyChange(mImageUri, null);
//				try {
//					Bitmap bm = android.provider.MediaStore.Images.Media.getBitmap(cr, mImageUri);
//
//					ByteArrayOutputStream osBm = new ByteArrayOutputStream();
//
//					bm.compress(CompressFormat.PNG, 0, osBm);
//
//					byte[] barray = osBm.toByteArray();
//
//					ByteArrayInputStream isBm = new ByteArrayInputStream(barray);
//
//					mBroadcast.send(isBm);
//
//				} catch (FileNotFoundException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}

//				try {
//					File compressFile = createImageFile(mImageUri.getLastPathSegment(), ".gz");
//					File imageFile = new File(mImagePath);
//					if (imageFile.exists()){
//						compressUri = Uri.fromFile(compressFile);
//
//						String compressPath = compressUri.getPath();
//						String imagePath = mImageUri.getPath();
//
//						compressToGzipFile(imagePath, compressPath);
//					}
//
//
//					Thread send = new Thread(new Runnable() {
//
//						@Override
//						public void run() {
//
//							ContentResolver cr = getContentResolver();
//							InputStream is;
//							try {
//								is = cr.openInputStream(compressUri);
//								mBroadcast.send(is);
//								mProgess.dismiss();
//								advertiseWifiP2P();
//
//							} catch (FileNotFoundException e) {
//								// TODO Auto-generated catch block
//								e.printStackTrace();
//							}
//						}
//					});
//					send.start();
//				} catch (IOException e1) {
//					// TODO Auto-generated catch block
//					e1.printStackTrace();
//				}

            }else if (requestCode == 20){

                mBroadcast.mListener = this;

                if (mState == State.StateActive){
                    mProgess.setTitle("Receive Picture");
                    mProgess.setMessage("Wait for picture...");
                    mProgess.show();
                }else if (mState == State.StatePassive)
                    sendImageCapture();
                //sendImageInGalery();
                mState = State.StateDefault;
            }
        }
    }

    @Override
    public void onReceiveData(String path) {
        Handler hd = new Handler(mContext.getMainLooper());
        String imagePath;
        try {
            File imageFile = createImageFile(null, ".jpg");
            //String imagePath = imageFile.getAbsolutePath();
            imagePath = path;
            //decompressFromGzipFile(path, imagePath);

            image_urls.add(imagePath);

            hd.post(new Runnable() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    if (mProgess.isShowing()){
                        mProgess.dismiss();
                    }
                    lvImageAdapter.notifyDataSetChanged();
                }
            });

            disconnectFromPeer();

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // TODO Auto-generated method stub
        MenuInflater inflate = getMenuInflater();
        inflate.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        if (item.getItemId() == R.id.mItem_Browser){
            Intent intent = new Intent(this, BrowserActivity.class);
            startActivityForResult(intent, 20);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnection() {
        // TODO Auto-generated method stub
        if (mState == State.StatePassive){
            sendImageCapture();
            //sendImageInGalery();
        }
    }

    @Override
    public void onPeers(WifiP2pDeviceList peers) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onDisconnect() {
        // TODO Auto-generated method stub
        advertiseWifiP2P();
    }
}