package com.roque.meza.navigationdrawerloginmysql;

import android.support.v7.app.AppCompatActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.TextInputEditText;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.roque.meza.navigationdrawerloginmysql.Utils.UserParcelable;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {
//    ประกาศตัวแปร
    private TextView loginLink;
    private ImageView imagePhoto;
    private TextInputEditText password;
    private TextInputEditText nombre;
    private TextInputEditText email;
    private Button registrar;
    private int request_code = 1;
    private Bitmap bitmap;
    private ProgressDialog progreso;
    RequestQueue requestQueue; //อนุญาตการเชื่อมต่อโดยตรงจากบริการบนเว็บ
    StringRequest stringRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);//layout

//        get event
        imagePhoto = (ImageView) findViewById(R.id.usuario_imagen_registro);//image
        loginLink = (TextView)findViewById(R.id.link_login);//black login
        email = (TextInputEditText)findViewById(R.id.correo_registro);//email
        password = (TextInputEditText)findViewById(R.id.password_registro);//password
        nombre = (TextInputEditText)findViewById(R.id.nombre_registro);//name
        registrar = (Button)findViewById(R.id.btn_registro_usuario);//button save data

        requestQueue = Volley.newRequestQueue(this);

//        Register sign in
        registrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Registrar();
            }
        });

//        image upload to mysql
        imagePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = null;//มีค่าว่างเปล่า
//                การตรวจสอบเวอร์ชันของแพลตฟอร์ม
                if(Build.VERSION.SDK_INT < 19){
//                    android 4.3  ขึ้นไป
                    i = new Intent();
                    i.setAction(Intent.ACTION_GET_CONTENT);
                }else {
                    //android 4.4 และดีกว่า
                    i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    i.addCategory(Intent.CATEGORY_OPENABLE);
                }
//                open file image
                i.setType("image/*");
                startActivityForResult(i, request_code);
            }
        });

//        ลงชื่อแล้วกดย้อนกลับ
        loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                finish();
            }
        });
    }

//    กรอกข้อมูลที Register แล้วไปเก็บใน mysql
    private void Registrar() {

//        เช็คความถูกต้องในการกรอกข้อมูล
        if (!validar()) return;

//        ส่วนของวงกลม เวลาโหลดจะขึ้นมา หลังจากเสร็จแล้วจะหายไป
        progreso = new ProgressDialog(this);
        progreso.setMessage("รอสักครู่...");//ข้อความว่า
        progreso.show();
//        url file php ทีใช้เชื่อมต่อ mysql
        String url = "http://119.59.103.121/app_mobile/test_sing_in/register_movil.php";
        stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                UserParcelable userParcelable = new UserParcelable();;
                Log.i("การตอบสนองของ JSON: ",""+response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if(jsonObject.names().get(0).equals("ลงทะเบียนสำเร็จ")){
                        nombre.setText("");// name
                        email.setText("");// email
                        password.setText("");// password

//                         ตัวแปรใน php สำคัญต้องตรงกัน
                        userParcelable.setId(jsonObject.getJSONArray("users").getJSONObject(0).getInt("id"));// id
                        userParcelable.setNombre(jsonObject.getJSONArray("users").getJSONObject(0).getString("name"));// name
                        userParcelable.setEmail(jsonObject.getJSONArray("users").getJSONObject(0).getString("email"));// email
                        userParcelable.setImage(jsonObject.getJSONArray("users").getJSONObject(0).getString("Personal"));//password

//                        toast แสดง 5 วิ
                        Toast.makeText(getApplicationContext(),jsonObject.getString("สำเร็จ"),Toast.LENGTH_SHORT).show();
                        progreso.dismiss();// ถ้าไม่เสร็จก้จะขึ้นวงกลมหมุน

//                        จากนั้นก็จะเปิด class MianActivity อัตโนมัติ
                        Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                        intent.putExtra("DATA_USER",userParcelable);
                        startActivity(intent);
                        finish();// ไม่รีเทรนค่ากลับ
                    }else{
//                        toast แสดง 5 วิ
                        Toast.makeText(getApplicationContext(),jsonObject.getString("เกิดข้อผิดพลาด"),Toast.LENGTH_SHORT).show();
//                        log แสดงข้อผิดพลาด
                        Log.i("การตอบสนองของ JSON: ",""+jsonObject.getString("เกิดข้อผิดพลาด"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                progreso.dismiss();//วงกลมหมุนจนกว่าจะโหลกเสร็จ
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(),"ไม่สามารถเชื่อมต่อ",Toast.LENGTH_SHORT).show();
                Log.i("ERROR: ",""+error.toString());
                progreso.dismiss();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {//เพื่อส่งข้อมูลโดย POST
                String sNombre = nombre.getText().toString();// name
                String sEmail = email.getText().toString();// email
                String sPassword =  password.getText().toString();// paSSWORD
                String  sImagePhoto = convertirImgString(bitmap);// image

//                ตัวแปรที่อยู่ file php เชื่อมต่อกับตัวแปร java ที่เป้น string
                Map<String,String> parametros = new HashMap<>();
                parametros.put("name",sNombre);
                parametros.put("email",sEmail);
                parametros.put("password",sPassword);
                parametros.put("Personal",sImagePhoto);
//                พารามิเตอร์เหล่านี้ถูกส่งไปยังบริการเว็บของเรา
                return parametros;
            }
        };

        requestQueue.add(stringRequest);
    }

//    ส่วนของ image
    private String convertirImgString(Bitmap bitmap) {

        String imagenString;
        ByteArrayOutputStream array=new ByteArrayOutputStream();
        if(bitmap!=null){
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,array);
            byte[] imagenByte=array.toByteArray();
            imagenString= Base64.encodeToString(imagenByte,Base64.DEFAULT);
        }else{
            imagenString = "ไม่มีภาพ"; // ข้อความนี้จะถูกส่งในกรณีที่ไม่มีภาพ
        }
//        imagenString
        return imagenString;
    }

//    เช็ึคความถูกต้องในกรอกข้อมูล
    private boolean validar() {
        boolean valid = true;

        String sNombre = nombre.getText().toString();
        String sPassword = password.getText().toString();
        String sEmail = email.getText().toString();

        if (sNombre.isEmpty() || sNombre.length() < 5) {
            nombre.setError("กรุณากรอกชื่อให้ถูกต้อง");
            valid = false;
        } else {
            nombre.setError(null);
        }

        if (sEmail.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(sEmail).matches()) {
            email.setError("ที่อยู่อีเมลไม่ถูกต้อง");
            valid = false;
        } else {
            email.setError(null);
        }

        if (sPassword.isEmpty() || password.length() < 6 || password.length() > 10) {
            password.setError("ป้อนตัวอักษรและตัวเลขระหว่าง 6 ถึง 10 ตัว");
            valid = false;
        } else {
            password.setError(null);
        }

        return valid;
    }

//    การอนุญาตให้เข้าถึงข้อรูปภาพ
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if(resultCode == RESULT_OK && requestCode == request_code){
            imagePhoto.setImageURI(data.getData());

            try{
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),data.getData());
                imagePhoto.setImageBitmap(bitmap);
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
