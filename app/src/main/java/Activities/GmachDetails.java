package Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Toast;

import com.example.gmach4u.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.EventListener;

import Adapters.ProductItem;
import Adapters.Supplier;

public class GmachDetails extends AppCompatActivity {
    private EditText GmachName, GmachAdress, GmachEmail, GmachOpeningHours,GmachPhone;
    private ImageView img;
    private Button viewPrudocts;
  //  private Button chat;
    private Supplier s;
    private DatabaseReference ref;
    private StorageReference storageRef;
    private RatingBar ratingBar;
    private ImageButton call;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gmach_details);
        setUIViews();
        Intent intent=getIntent();
        String key=intent.getStringExtra("key");
        ref= FirebaseDatabase.getInstance().getReference("Suppliers").child(key).child("details");
        ref.addListenerForSingleValueEvent(new ValueEventListener(){
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if(snapshot.exists()){
                    s = snapshot.getValue(Supplier.class);
                    GmachAdress.setText(s.getAddress());
                    GmachEmail.setText(s.getEmail());
                    GmachOpeningHours.setText(s.getOpeningTime());
                    GmachPhone.setText(s.getPhone());
                    GmachName.setText(s.getName());
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float v, boolean fromUser) {
                int rating=(int) v;
                String messege =null;
                switch (rating){
                    case 1:
                        messege="sorry to hear that:(";
                        break;
                    case 2:
                        messege="you can do better then this";
                        break;
                    case 3:
                        messege="good enough!";
                        break;
                    case 4:
                        messege="Great! Thank you";
                        break;
                    case 5:
                        messege="Awesome! you are the best";
                        break;
                }
                Toast.makeText(GmachDetails.this,messege,Toast.LENGTH_SHORT).show();
            }
        });
//        chat.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                startActivity(new Intent(GmachDetails.this,Chat.class));
//            }
//        });
        viewPrudocts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(GmachDetails.this,GmachStockClient.class);
                i.putExtra("key", s.getId());
                startActivity(i);
            }
        });
        call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String s= "tel:" + GmachPhone.getText().toString();
                Intent intent=new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse("tel:0304958756"));
//                startActivity(intent);
            }
        });

        //set img
        String path = "Images/" + key + "/main";
        final long ONE_MEGABYTE = (long) Math.pow(1024, 10);
        storageRef.child(path).getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                img.setImageBitmap(bitmap);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) { }
        });
    }//end on create


    private void setUIViews(){
        ratingBar=(RatingBar) findViewById(R.id.ratingBar);
        GmachAdress= (EditText) findViewById(R.id.gAdress);
        GmachEmail= (EditText) findViewById(R.id.gEmail1);
        GmachOpeningHours= (EditText) findViewById(R.id.gopeninghours);
        GmachPhone= (EditText) findViewById(R.id.gPhone);
        GmachName = (EditText) findViewById(R.id.gmachName);
        viewPrudocts=(Button) findViewById(R.id.viewprudocts);
   //     chat=(Button) findViewById(R.id.chat1);
        call=(ImageButton) findViewById(R.id.callbtn);
        img = (ImageView) findViewById(R.id.gmachImg);
        storageRef = FirebaseStorage.getInstance().getReference();
    }
}