package Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.gmach4u.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import Adapters.Client;

public class ClientRegister extends AppCompatActivity {
    private  EditText userName, userPassword, userEmail,userPhone;
    private Button regButton;
    private Button HaveAccount;
    String name, email, password, phone;
    DatabaseReference myRef;
    FirebaseAuth fireBaseAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_register);
        setUIViews();

        HaveAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ClientRegister.this, loginActivity.class);
                startActivity(intent);
            }
        });

        regButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(validate()){
                    String user_email=userEmail.getText().toString().trim();
                    String user_password=userPassword.getText().toString().trim();

                    fireBaseAuth.createUserWithEmailAndPassword(user_email,user_password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(task.isSuccessful()) {
                                        writeNewUser(task.getResult().getUser().getUid());
                                        Toast.makeText(ClientRegister.this, "Registration successful", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(ClientRegister.this,MainActivity.class));
                                    } else{
                                        Toast.makeText(ClientRegister.this, "Registration failed", Toast.LENGTH_SHORT).show();
                                        task.getException();
                                    }//end else
                                }//end Oncomplete
                            });//end email and password
                }//end if
            }//end on click
        });//end click listener



    }//end onCreate


    private void writeNewUser(String userId) {
        name = userName.getText().toString();
        email=userEmail.getText().toString().trim();
        password=userPassword.getText().toString().trim();
        phone = userPhone.getText().toString();
        Client user = new Client(name, email, phone,userId);

        myRef.child("Clients").child(fireBaseAuth.getUid()).child("details").setValue(user);
    }//end write new user

    private void setUIViews(){
        userName= (EditText) findViewById(R.id.fullName);
        userPassword= (EditText) findViewById(R.id.password1);
        userEmail= (EditText) findViewById(R.id.email1);
        regButton=(Button) findViewById(R.id.submit);
        HaveAccount=(Button) findViewById(R.id.LoginRegist);
        userPhone= (EditText) findViewById(R.id.Phone);
        fireBaseAuth= FirebaseAuth.getInstance();
        myRef = FirebaseDatabase.getInstance().getReference();

    }

    private boolean validate(){
        Boolean result=false;

        String name= userName.getText().toString();
        String password= userPassword.getText().toString();
        String email= userEmail.getText().toString();
        String phone= userPhone.getText().toString();

        if(name.isEmpty() && password.isEmpty() && email.isEmpty() && phone.isEmpty()){
            Toast.makeText(this,"please enter all the details",Toast.LENGTH_SHORT).show();
        }
        else if(!name.matches("^[a-zA-Z]+\\s[a-zA-Z\\s]+$")) {
            Toast.makeText(this, "Please enter your full name (in English)", Toast.LENGTH_SHORT).show();
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
        else{
            result=true;
        }
        return result;
    }
}//end class