package com.roque.meza.navigationdrawerloginmysql;

import android.support.v7.app.AppCompatActivity;
import android.support.design.widget.TextInputEditText;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.roque.meza.navigationdrawerloginmysql.Utils.UserParcelable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    //    ประกาศตัวแปร
    private Button acceder;
    private TextView registrar;
    private TextInputEditText email;
    private TextInputEditText password;
    private ProgressDialog progreso;
    private RequestQueue requestQueue;
    StringRequest stringRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

//        git event
        email = (TextInputEditText) findViewById(R.id.etusuario);// email
        password = (TextInputEditText) findViewById(R.id.etpass);// password
        acceder = (Button) findViewById(R.id.btn_acceder);//button login
        registrar = (TextView) findViewById(R.id.signup);// button register

        requestQueue = Volley.newRequestQueue(this);

//        เมื่อกดปุ่ม Register
        registrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                finish();
            }
        });

//        เมื่อกดปุ่ม login
        acceder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iniciar();
            }
        });
    }

    private void iniciar() {
//        เช็คความถูกต้องในการกรอกข้อมูล
        if (!validar()) return;

        progreso = new ProgressDialog(this);
        progreso.setMessage("รอสักครู่...");
        progreso.show();// วงกลมหมุนรอ โหลดเสร็จก็จะหายไป
//        url file php login
        String url = "http://119.59.103.121/app_mobile/test_sing_in/login_movil.php";

        stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                UserParcelable userParcelable = new UserParcelable();
                ;
                Log.i("RESPUESTA JSON: ", "" + response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (jsonObject.names().get(0).equals("สำเร็จ")) {
                        email.setText("");// email
                        password.setText("");// password

                        userParcelable.setId(jsonObject.getJSONArray("users").getJSONObject(0).getInt("id"));// id
                        userParcelable.setEmail(jsonObject.getJSONArray("users").getJSONObject(0).getString("email"));// email
                        userParcelable.setNombre(jsonObject.getJSONArray("users").getJSONObject(0).getString("name"));// name
                        userParcelable.setImage(jsonObject.getJSONArray("users").getJSONObject(0).getString("Personal"));// image

//                        toaet 5 วิ
                        Toast.makeText(getApplicationContext(), jsonObject.getString("สำเร็จ"), Toast.LENGTH_SHORT).show();
                        progreso.dismiss();

                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.putExtra("DATA_USER", userParcelable);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(getApplicationContext(), jsonObject.getString("error"), Toast.LENGTH_SHORT).show();
                        Log.i("RESPUESTA JSON: ", "" + jsonObject.getString("error"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                progreso.dismiss();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "ไม่สามารถเชื่อมต่อ", Toast.LENGTH_SHORT).show();
                progreso.dismiss();//แถบโหลดข้อมูล
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {//เพื่อส่งข้อมูลโดย POST
                String sEmail = email.getText().toString();
                String sPassword = password.getText().toString();

                Map<String, String> parametros = new HashMap<>();
                parametros.put("email", sEmail);// email
                parametros.put("password", sPassword);// password

                return parametros;
            }
        };

        requestQueue.add(stringRequest);
    }

    private boolean validar() {
        boolean valid = true;

        String sEmail = email.getText().toString();
        String sPassword = password.getText().toString();

//        เช็คค่าความว่างเปล่า
        if (sEmail.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(sEmail).matches()) {
            email.setError("ป้อนที่อยู่อีเมลที่ถูกต้อง");
            valid = false;
        } else {
            email.setError(null);
        }

        if (sPassword.isEmpty() || password.length() < 4 || password.length() > 10) {
            password.setError("ระหว่าง 4 ถึง 10 ตัวอักษรและตัวเลข");
            valid = false;
        } else {
            password.setError(null);
        }

        return valid;
    }
}//Main Class
