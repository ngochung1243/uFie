package vn.asquare.ufie;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.widget.ImageView;

public class ShowImageActivity extends Activity {

    ImageView imvReceiveImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.showimage_activity);
        imvReceiveImage = (ImageView)findViewById(R.id.imvReceiveImage);
        Intent mIntent = getIntent();
        String imagePath = mIntent.getStringExtra("ImagePath");

        final BitmapFactory.Options options = new BitmapFactory.Options();

        options.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(imagePath, options);
        Point size = new Point();
        Display display = getWindowManager().getDefaultDisplay();
        display.getSize(size);

        int imvWidth = size.x;
        int imvHeight = size.y;

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, imvWidth, imvHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        Bitmap oldbm = BitmapFactory.decodeFile(imagePath, options);

        Bitmap bm = MainActivity.createCorrectBitmap(imagePath, oldbm);
        imvReceiveImage.setImageBitmap(bm);
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
}
