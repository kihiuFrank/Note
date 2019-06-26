package frank.google.com;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by Kihiu.
 */

public class MainActivity extends AppCompatActivity {
    public static final String NOTE_POSITION = "frank.google.com.NOTE_POSITION";
    public static final int POSITION_NOT_SET = -1;
    private NoteInfo mNote;
    private boolean isNewNote;
    private Spinner mSpinnerCourses;
    private EditText mTextNoteTitle;
    private EditText mTextNoteText;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private ImageView imageView;
    private Button btnPhoto;
    String currentPhotoPath;

    /**
     *  CAMERA
     *  */
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //ensuring there is a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            //create file to store the photo
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                //Error message.
            }


            if (photoFile != null) {
                Uri photoUri = FileProvider.getUriForFile(this,
                        "frank.google.com.fileProvider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private File createImageFile() throws IOException {
        //creating image filename
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName, /* prefix */
                ".jpg", /* suffix */
                storageDir     /* directory */
        );
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(currentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent result) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && requestCode == RESULT_OK) {
            Bundle extras = result.getExtras();
            Bitmap bitmap = (Bitmap) extras.get("data");
            imageView.setImageBitmap(bitmap);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mSpinnerCourses = findViewById(R.id.spinner_courses);
        btnPhoto = findViewById(R.id.btn_photo);

        List<CourseInfo> courses = DataManager.getInstance().getCourses();
        ArrayAdapter<CourseInfo> adapterCourses = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, courses);
        adapterCourses.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerCourses.setAdapter(adapterCourses);

        readDisplayStateValues();

        mTextNoteTitle = findViewById(R.id.text_note_title);
        mTextNoteText = findViewById(R.id.text_note_text);
        if (!isNewNote) {
            displayNote(mSpinnerCourses, mTextNoteTitle, mTextNoteText);
        }

        btnPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
                galleryAddPic();
            }
        });


    }



    private void displayNote(Spinner spinnerCourses, EditText textNoteTitle, EditText textNoteText) {
        List<CourseInfo> courses = DataManager.getInstance().getCourses();
        int courseIndex = courses.indexOf(mNote.getCourse());
        spinnerCourses.setSelection(courseIndex);
        textNoteTitle.setText(mNote.getTitle());
        textNoteText.setText(mNote.getText());
    }

    private void readDisplayStateValues() {
        Intent intent = getIntent();
        int position = intent.getIntExtra(NOTE_POSITION, POSITION_NOT_SET);
        isNewNote = position == POSITION_NOT_SET;

        if (!isNewNote) {
            mNote = DataManager.getInstance().getNotes().get(position);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_send_mail) {
            sendEmail();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void sendEmail() {
         CourseInfo course = (CourseInfo) mSpinnerCourses.getSelectedItem();
         String subject = mTextNoteTitle.getText().toString();
         String text = "Check out this email. Its fantastic \n\"" +
                 course.getTitle() + "\"\n" + mTextNoteText.getText().toString();

         Intent intent = new Intent(Intent.ACTION_SEND);
         intent.setType("message/rfc2822");
         intent.putExtra(Intent.EXTRA_SUBJECT,subject);
         intent.putExtra(Intent.EXTRA_TEXT,text);
         startActivity(intent);
    }
}
