package Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ArrayAdapter;
import com.example.gmach4u.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import Adapters.Supplier;
import android.widget.Spinner;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class SupplierRegister extends AppCompatActivity implements View.OnClickListener{
    private  EditText userName, userPassword, userEmail,userPhone, userAddress, userOpeningTime;
    private ImageView img;
    private Button signUpButton, upImgButton, cameraButton;
    private Spinner userCategory, userLocation;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference myRef;
    private String name, email, password, phone,category, location, address, openingTime;
    private StorageTask uploadTask;
    private StorageReference storageRef;
    private static final int GET_FROM_GALLERY = 3;
    private static final int GET_FROM_CAMERA = 0;
    byte[] byteData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_supplier_register);
        setUIViews();
    }//end onCreate

    private void setUIViews(){
        //set text
        userName= (EditText) findViewById(R.id.GmachNameInput);
        userEmail= (EditText) findViewById(R.id.EmailInput);
        userPassword= (EditText) findViewById(R.id.PasswordInput);
        userPhone= (EditText) findViewById(R.id.PhoneNumberInput);
        userAddress= (EditText) findViewById(R.id.AddressInput);
        userOpeningTime= (EditText) findViewById(R.id.OpeningTimeInput);
        //set button
        signUpButton=(Button) findViewById(R.id.signUpSupplier);
        cameraButton=(Button) findViewById(R.id.takePicture);
        upImgButton = (Button) findViewById(R.id.uploadImg);
        signUpButton.setOnClickListener((View.OnClickListener) this);
        cameraButton.setOnClickListener((View.OnClickListener) this);
        upImgButton.setOnClickListener((View.OnClickListener) this);
        //set imageView
        img = (ImageView) findViewById(R.id.suppImg);
        //set firebase
        firebaseAuth= FirebaseAuth.getInstance();
        myRef = FirebaseDatabase.getInstance().getReference();
        storageRef = FirebaseStorage.getInstance().getReference("Images");
        //set spinner
        userCategory = (Spinner) findViewById(R.id.CategoryInput);
        ArrayAdapter<String> categoriesAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.categoriesArray));
        userCategory.setAdapter(categoriesAdapter);

        userLocation = (Spinner) findViewById(R.id.LocationInput);
        ArrayAdapter<String> locationsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.locationsArray));
        userLocation.setAdapter(locationsAdapter);
    }//end setUIViews

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.signUpSupplier) {
            signUp();
        }
        if(v.getId() == R.id.uploadImg){
            if (uploadTask != null && uploadTask.isInProgress())
                makeToast("upload in progress");
            else chooseUploadImg();
        }
        if(v.getId() == R.id.takePicture){
            if (uploadTask != null && uploadTask.isInProgress())
                makeToast("upload in progress");
            else takePicture();
        }
    }//end onClick

    private void signUp(){
        name = userName.getText().toString();
        phone = userPhone.getText().toString();
        address = userAddress.getText().toString();
        openingTime = userOpeningTime.getText().toString();
        email=userEmail.getText().toString().trim();
        password=userPassword.getText().toString().trim();
        location = userLocation.getSelectedItem().toString();
        category = userCategory.getSelectedItem().toString();
        if(!validate()) { return; }
        firebaseAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()) {
                    String userId = task.getResult().getUser().getUid();
                    Supplier user = new Supplier(name, email, phone,address,openingTime,category,location,userId);
                    myRef.child("Suppliers").child(firebaseAuth.getUid()).child("details").setValue(user);
                    //upload img
                    if(byteData != null){
                        String path = userId + "/" + "main";
                        uploadTask = storageRef.child(path).putBytes(byteData);
                    }
                    makeToast("Registration success");
                    startActivity(new Intent(SupplierRegister.this,MainSupplier.class));
                    finish();
                } else {
                    makeToast("Registration failed");
                }//end else
            }//end On complete
        });//end create user
    }//end setButton

    private void chooseUploadImg() {
        //open gallery
        Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(i, GET_FROM_GALLERY);
    }//end up img
    private void takePicture(){
        Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(i, GET_FROM_CAMERA);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK) {
            if (requestCode == GET_FROM_CAMERA) {
                Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                img.setImageBitmap(bitmap);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                byteData = stream.toByteArray();
            }
            if(requestCode == GET_FROM_GALLERY){
                Uri imguri = data.getData();
                img.setImageURI(imguri);
                Bitmap bitmap = null;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imguri);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    byteData = baos.toByteArray();
                }
                catch (FileNotFoundException e) { e.printStackTrace();}
                catch (IOException e) { e.printStackTrace();}
            }
        }
    }// end on act result

    private void makeToast(String m){
        Toast.makeText(SupplierRegister.this, m, Toast.LENGTH_SHORT).show();
    }
    private boolean validate (){
        Boolean result=false;

        if(name.isEmpty() || password.isEmpty() || email.isEmpty() || phone.isEmpty() || address.isEmpty()){
            Toast.makeText(this,"please enter all the details",Toast.LENGTH_SHORT).show();
        }
        else if(!email.matches("^\\w+@[a-zA-Z_]+?\\.[a-zA-Z]{2,3}$")){
            Toast.makeText(this, "Please enter valid Email", Toast.LENGTH_SHORT).show();
        }
        else if(password.length() < 6){
            Toast.makeText(this, "Password length must be at least 6 characters", Toast.LENGTH_SHORT).show();
        }
        else if(!phone.matches("^[0-9]+$") || phone.length() != 10){
            Toast.makeText(this, "Please enter valid phone number", Toast.LENGTH_SHORT).show();

        }
        else if(!address.matches("^[a-zA-Z]+\\s[a-zA-Z\\s]+$")){
            Toast.makeText(this, "Please enter city and street", Toast.LENGTH_SHORT).show();
        }
        else if(userCategory.getSelectedItem().toString().equals("Search by category")){
            //Toast.makeText(this, "Please select category", Toast.LENGTH_SHORT).show();
            Toast.makeText(this, "Please select catagory", Toast.LENGTH_SHORT).show();
        }
        else if(userLocation.getSelectedItem().toString().equals("Search by location")){
            //Toast.makeText(this, "Please select category", Toast.LENGTH_SHORT).show();
            Toast.makeText(this, "Please select location", Toast.LENGTH_SHORT).show();
        }

        else{
            result=true;
        }
        return result;
    }
}//end class