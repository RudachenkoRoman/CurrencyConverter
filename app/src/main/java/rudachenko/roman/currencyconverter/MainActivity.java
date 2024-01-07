package rudachenko.roman.currencyconverter;
import androidx.appcompat.app.AppCompatActivity;
import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    TextView convertToTextView, convertFromTextView, convertRateTextView;
    EditText amountToConvert;
    ArrayList<String> arrayList;
    Dialog fromDialog;
    Dialog toDialog;
    Button convertButton;
    String convertFromValue, convertToValue, convertValue;
    String[] country = {"USD", "INR", "RUB"};
    private final int WIGHT = 800;
    private final int HEIGHT = 800;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        convertFromTextView = findViewById(R.id.convert_from);
        convertToTextView = findViewById(R.id.convert_to);
        amountToConvert = findViewById(R.id.amount_to_convert_text);
        convertRateTextView = findViewById(R.id.conversion_rate_text);
        convertButton = findViewById(R.id.conversion_button);

        arrayList = new ArrayList<>();
        arrayList.addAll(Arrays.asList(country));

        convertFromTextView.setOnClickListener(v -> {
            fromDialog = new Dialog(MainActivity.this);
            fromDialog.setContentView(R.layout.from_spinner);
            Objects.requireNonNull(fromDialog.getWindow()).setLayout(WIGHT,HEIGHT);
            fromDialog.show();

            EditText editText = fromDialog.findViewById(R.id.text_view);
            ListView listView = fromDialog.findViewById(R.id.list_view);

            ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1,arrayList);
            listView.setAdapter(adapter);

            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    adapter.getFilter().filter(s);
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
            listView.setOnItemClickListener((parent, view, position, id) -> {
                convertFromTextView.setText(adapter.getItem(position));
                fromDialog.dismiss();
                convertFromValue = adapter.getItem(position);
            });
        });

        convertToTextView.setOnClickListener(v -> {
            toDialog = new Dialog(MainActivity.this);
            toDialog.setContentView(R.layout.to_spinner);
            Objects.requireNonNull(toDialog.getWindow()).setLayout(WIGHT,HEIGHT);
            toDialog.show();

            EditText editText = toDialog.findViewById(R.id.text_view);
            ListView listView = toDialog.findViewById(R.id.list_view);

            ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1,arrayList);
            listView.setAdapter(adapter);

            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    adapter.getFilter().filter(s);
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
            listView.setOnItemClickListener((parent, view, position, id) -> {
                convertToTextView.setText(adapter.getItem(position));
                toDialog.dismiss();
                convertToValue = adapter.getItem(position);
            });
        });

        convertButton.setOnClickListener(v -> {
            try {
                if (convertFromTextView.getText().toString().equals("") || convertToTextView.getText().toString().equals("")) {
                    toastCategory();
                }else if(convertFromTextView.getText().toString().equals(convertToTextView.getText().toString())){
                    toastEquals();
                    convertRateTextView.setText("");
                }else {
                    if (amountToConvert.getText().toString().equals("")){
                        toastValue();
                        convertRateTextView.setText("");
                    }else {
                        double amountToConvert = Double.parseDouble(MainActivity.this.amountToConvert.getText().toString());
                        getConversionRate(convertFromValue,convertToValue,amountToConvert);
                    }
                }
            }catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
    private void toastCategory(){
        Toast toast = Toast.makeText(MainActivity.this, "Please select the currency in the categories \"Convert From\" and \"Convert To\"", Toast.LENGTH_SHORT);
        TextView r = (TextView) Objects.requireNonNull(toast.getView()).findViewById(android.R.id.message);
        r.setGravity(Gravity.CENTER);
        toast.show();
    }

    private void toastValue(){
        Toast.makeText(MainActivity.this, "Please enter a value", Toast.LENGTH_SHORT).show();
    }

    private void toastEquals(){
        Toast.makeText(MainActivity.this, "You have selected the same currencies", Toast.LENGTH_SHORT).show();
    }

    public void getConversionRate(String convertFromValue, String convertToValue, Double amountToConvert){

        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://free.currconv.com/api/v7/convert?q=" + convertFromValue + "_" + convertToValue + "&compact=ultra&apiKey=56020d7b9ffa1cf4260c";
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, response -> {
            JSONObject jsonObject;
            try {
                jsonObject = new JSONObject(response);
                Double conversionRateValue = round(((Double) jsonObject.get(convertFromValue + "_" + convertToValue)), 2);
                convertValue = String.valueOf(round((conversionRateValue * amountToConvert), 2));
                convertRateTextView.setText(convertValue);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> {

        });
        queue.add(stringRequest);
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}