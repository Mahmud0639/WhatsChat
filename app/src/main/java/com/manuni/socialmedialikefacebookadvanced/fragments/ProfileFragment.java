package com.manuni.socialmedialikefacebookadvanced.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.manuni.socialmedialikefacebookadvanced.R;
import com.manuni.socialmedialikefacebookadvanced.activities.LoginActivity;
import com.manuni.socialmedialikefacebookadvanced.databinding.FragmentProfileBinding;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class ProfileFragment extends Fragment {
    public static final int REQUEST_CAMERA_PERMISSION = 101;
    public static final int REQUEST_STORAGE_PERMISSON = 102;


    private FirebaseAuth auth;
    private FirebaseUser firebaseUser;
    private DatabaseReference reference;
    private StorageReference storageReference;

    private String storageImagesPath = "Users_Profile_CoverImg/";



    private ProgressDialog dialog;
    private String[] cameraPermission;
    private String[] storagePermission;

    private String profileOrCoverPhoto;

    private Uri imageUri;

    private Context mContext;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }

    FragmentProfileBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(inflater,container,false);

        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference().child("Users");
        //storageReference = FirebaseStorage.getInstance().getReference();

        dialog = new ProgressDialog(getActivity());


        cameraPermission = new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};


        Query query = reference.orderByChild("email").equalTo(firebaseUser.getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                    String name = ""+dataSnapshot.child("name").getValue();
                    String email = ""+dataSnapshot.child("email").getValue();
                    String phone = ""+dataSnapshot.child("phone").getValue();
                    String image = ""+dataSnapshot.child("image").getValue();
                    String cover = ""+dataSnapshot.child("cover").getValue();

                    binding.nameTV.setText(name);
                    binding.emailTV.setText(email);
                    binding.phoneTV.setText(phone);
                    try {
                        Picasso.get().load(image).placeholder(R.drawable.ic_face).into(binding.avatarIV);
                    } catch (Exception e) {
                       Picasso.get().load(R.drawable.ic_face).into(binding.avatarIV);
                        e.printStackTrace();
                    }

                    try {
                        Picasso.get().load(cover).placeholder(R.drawable.ic_cover_photo_white).into(binding.coverPhoto);
                    } catch (Exception e) {
                        Picasso.get().load(R.drawable.ic_cover_photo_white).into(binding.coverPhoto);
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showEditProfileDialog();
            }
        });




        return binding.getRoot();
    }

    private void showEditProfileDialog() {
        String[] options = {"Edit Profile","Edit Cover Photo","Edit Name","Edit Phone"};

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Choose action");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (i==0){
                    dialog.setMessage("Updating Profile...");
                    profileOrCoverPhoto = "image";
                    showImagePickDialog();
                }else if (i==1){
                    dialog.setMessage("Updating cover photo...");
                    profileOrCoverPhoto = "cover";
                    showImagePickDialog();
                }else if (i==2){
                    dialog.setMessage("Updating Name...");
                    showNamePhoneUpdateDialog("name");
                }else if (i==3){
                    dialog.setMessage("Updating Phone...");
                    showNamePhoneUpdateDialog("phone");
                }
            }
        });
        builder.create().show();

    }

    private void showNamePhoneUpdateDialog(String key) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("Update "+key);

        LinearLayout layout = new LinearLayout(mContext);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(10,10,10,10);

        EditText editText = new EditText(mContext);
        editText.setHint("Enter "+key);
        layout.addView(editText);

        builder.setView(layout);

        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialog.show();
                String value = editText.getText().toString().trim();
                if (TextUtils.isEmpty(value)){
                    dialog.dismiss();
                    Toast.makeText(mContext, "Please give "+key, Toast.LENGTH_SHORT).show();
                }else {
                    HashMap<String,Object> values = new HashMap<>();
                    values.put(key,value);

                    reference.child(firebaseUser.getUid()).updateChildren(values).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            dialog.dismiss();
                            Toast.makeText(mContext, ""+key+" Updated Successfully!", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            dialog.dismiss();
                            Toast.makeText(mContext, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private boolean checkCameraPermission(){
        boolean result1 = ContextCompat.checkSelfPermission(mContext,Manifest.permission.CAMERA)== PackageManager.PERMISSION_GRANTED;
        boolean result2 = ContextCompat.checkSelfPermission(mContext,Manifest.permission.WRITE_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED;


        return result1 && result2;
    }

    private void requestCameraPermission(){
        requestPermissions(cameraPermission, REQUEST_CAMERA_PERMISSION);
    }


    private boolean checkStoragePermission(){
        boolean res = ContextCompat.checkSelfPermission(mContext,Manifest.permission.WRITE_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED;

        return res;
    }
    private void requestStoragePermission(){
        requestPermissions(storagePermission, REQUEST_STORAGE_PERMISSON);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_CAMERA_PERMISSION:{
                if (grantResults.length>0){
                    boolean cameraAccepted = grantResults[0]==PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1]==PackageManager.PERMISSION_GRANTED;

                    if (cameraAccepted && storageAccepted){
                        pickImageUsingCamera();
                    }else {
                        Toast.makeText(mContext, "Permission Required!", Toast.LENGTH_SHORT).show();
                    }
                }else {
                    Toast.makeText(mContext, "Cancelled.", Toast.LENGTH_SHORT).show();
                }

            }
            break;
            case REQUEST_STORAGE_PERMISSON:{
                if (grantResults.length>0){
                    boolean storageAccepted = grantResults[0]==PackageManager.PERMISSION_GRANTED;
                    if (storageAccepted){
                        pickImageFromGallery();
                    }else {
                        Toast.makeText(mContext, "Permission Required!", Toast.LENGTH_SHORT).show();
                    }
                }else {
                    Toast.makeText(mContext, "Cancelled.", Toast.LENGTH_SHORT).show();
                }
            }
            break;

        }
    }

    private void pickImageFromGallery() {
        Intent imagePickFromGallery = new Intent(Intent.ACTION_PICK);
        imagePickFromGallery.setType("image/*");
        resultLauncherForGallery.launch(imagePickFromGallery);
    }
    private ActivityResultLauncher<Intent> resultLauncherForGallery = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode()==Activity.RESULT_OK){
                Intent data = result.getData();
                imageUri = data.getData();
                //ekhane duitar khetrei same method ke call kora hoyeche karon amra jokhon edit profile a click korbo tokhon same vabe ei duita option e dekhabo
                //pick image from gallery ar holo image pick using camera. jekhan thekei image ta neoya hok na keno ..jodi user edit profile a click kore tahole
                //amader upore rakha String profileOrCover = "image" set hobe. ar jodi option 2 mane edit cover a click kore tahole String profileOrCover= "cover"
                //hobe..jar karone database a cover er child a cover and image er child a profile image upload hobe.
                uploadUserCoverOrImage(imageUri);
            }
        }
    });

    private void uploadUserCoverOrImage(Uri myImageUri) {
        dialog.show();

        String pathName = storageImagesPath+""+profileOrCoverPhoto+"_"+auth.getUid();
        StorageReference storageReference2 = FirebaseStorage.getInstance().getReference().child(pathName);
        storageReference2.putFile(myImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isSuccessful());
                Uri downloadUrl = uriTask.getResult();

                //check image is uploaded or not and url is received.
                if (uriTask.isSuccessful()){
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put(profileOrCoverPhoto,downloadUrl.toString());//download url ti uri type tai string e convert koresi
                    reference.child(firebaseUser.getUid()).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            dialog.dismiss();
                            Toast.makeText(mContext, "Image Updated Successfully!", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            dialog.dismiss();
                            Toast.makeText(mContext, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    });
                }else {
                    dialog.dismiss();
                    Toast.makeText(mContext, "Some error is getting.", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                dialog.dismiss();
                Toast.makeText(mContext, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void pickImageUsingCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE,"IMAGE TEMP TITLE");
        values.put(MediaStore.Images.Media.DESCRIPTION,"IMAGE TEMP DESC");

        imageUri = mContext.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);

        resultLauncherForCamera.launch(intent);
    }

    private ActivityResultLauncher<Intent> resultLauncherForCamera = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode()== Activity.RESULT_OK){
                //use the imageUri with what to do

                //ekhane duitar khetrei same method ke call kora hoyeche karon amra jokhon edit profile a click korbo tokhon same vabe ei duita option e dekhabo
                //pick image from gallery ar holo image pick using camera. jekhan thekei image ta neoya hok na keno ..jodi user edit profile a click kore tahole
                //amader upore rakha String profileOrCover = "image" set hobe. ar jodi option 2 mane edit cover a click kore tahole String profileOrCover= "cover"
                //hobe..jar karone database a cover er child a cover and image er child a profile image upload hobe.
                uploadUserCoverOrImage(imageUri);
            }
        }
    });

    private void showImagePickDialog() {
        String[] imageOptions = {"Camera","Gallery"};

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Pick Image");
        builder.setItems(imageOptions, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (i==0){
                    if (checkCameraPermission()){
                        pickImageUsingCamera();
                    }else {
                        requestCameraPermission();
                    }

                }else if(i==1){
                    if (checkStoragePermission()){
                        pickImageFromGallery();
                    }else {
                        requestStoragePermission();
                    }
                }
            }
        });
        builder.create().show();
    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);//to show option menu in fragment
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main,menu);
        super.onCreateOptionsMenu(menu,inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId()==R.id.logout){
            auth.signOut();
            checkUserStatus();
        }
        return super.onOptionsItemSelected(item);
    }

    private void checkUserStatus(){
        FirebaseUser user = auth.getCurrentUser();
        if (user != null){

        }else{
            startActivity(new Intent(mContext, LoginActivity.class));
            getActivity().finish();
        }
    }
}