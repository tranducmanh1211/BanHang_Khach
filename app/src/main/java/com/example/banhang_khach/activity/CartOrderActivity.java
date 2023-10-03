package com.example.banhang_khach.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.banhang_khach.Adapter.CartOrderAdapter;
import com.example.banhang_khach.DTO.BillDTO;
import com.example.banhang_khach.DTO.CartOrderDTO;
import com.example.banhang_khach.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CartOrderActivity extends AppCompatActivity implements CartOrderAdapter.OnclickCheck{
    String TAG = "cartoderactivity";
    ListView rc_listcart;
    TextView tongtien;
    Button btnmuahang;
    ArrayList<CartOrderDTO> list;
    CartOrderAdapter adapter;
    double tongcart = 0;
    String layidhoadon = "";
    int s = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart_order);
        Anhxa();
        list = new ArrayList<>();
        adapter = new CartOrderAdapter(CartOrderActivity.this,list, this);
        rc_listcart.setAdapter(adapter);
        getdatacartorder();

    }


    public void Anhxa(){
        rc_listcart = findViewById(R.id.rc_view);
        tongtien = findViewById(R.id.tv_tonggia);
        btnmuahang = findViewById(R.id.btn_muahang);
    }

    public void getdatacartorder(){
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRefId = database.getReference("CartOrder");
        myRefId.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    CartOrderDTO categoryDTO = dataSnapshot.getValue(CartOrderDTO.class);
                    Log.d(TAG, "list : " + categoryDTO.getPrice());
                    if (auth.getUid().equals(categoryDTO.getIduser()) && (categoryDTO.getIdBill()).equals("")){
                        list.add(categoryDTO);
                    }
                }
                adapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onQuality(ArrayList<String> idcart) {
        btnmuahang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (s == 0){
                    Toast.makeText(CartOrderActivity.this, "Bạn chưa chọn sản phẩm nào!", Toast.LENGTH_SHORT).show();
                }else {
                    UUID uuid = UUID.randomUUID();
                    String udi = uuid.toString().trim();
                    FirebaseAuth auth = FirebaseAuth.getInstance();
                    DateFormat df = new SimpleDateFormat("HH:mm dd.MM.yyyy ");
                    String date = df.format(Calendar.getInstance().getTime());
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference myRef_Bill = database.getReference("BillProduct/" + udi);
                    BillDTO billDTO = new BillDTO(udi, auth.getUid(), tongcart,date, 1);
                    myRef_Bill.setValue(billDTO, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                            Toast.makeText(CartOrderActivity.this, "Cảm ơn bạn đã đặt hàng", Toast.LENGTH_SHORT).show();
                        }
                    });
                    for (int i = 0; i < idcart.size(); i++) {
                        DatabaseReference myRef = database.getReference("CartOrder/" + idcart.get(i));
                        Map<String, Object> mapcartoder = new HashMap<>();
                        mapcartoder.put("idBill", udi);
                        myRef.updateChildren(mapcartoder);
                    }
                }
            }
        });
    }

    @Override
    public void onCheckboxTrue(CartOrderDTO cartOrderDTO) {
        s++;
        Log.d(TAG, "s: " + s);
        tongcart += cartOrderDTO.getPrice();
        Log.d(TAG, "onCheckbox: " + tongcart);
        tongtien.setText("Tổng tiền: " +String.valueOf(tongcart));
    }

    @Override
    public void onCheckboxFalse(CartOrderDTO cartOrderDTO) {
        s--;
        Log.d(TAG, "s: " + s);
        tongcart -= cartOrderDTO.getPrice();
        Log.d(TAG, "onCheckbox: " + tongcart);
        tongtien.setText("Tổng tiền: " +String.valueOf(tongcart));
        layidhoadon = "";
    }

}