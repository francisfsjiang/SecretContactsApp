package com.never.secretcontacts;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.never.secretcontacts.util.ContactItem;

public class ContactsEditActivity extends AppCompatActivity {

//    private List<View> item_view_list_ = new ArrayList<>();

    private Button new_item_button_;

    private LinearLayout item_list_phone_;
    private LinearLayout item_list_email_;
    private LinearLayout item_list_address_;
    private LinearLayout item_list_memo_;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts_edit);

        item_list_phone_ = (LinearLayout) findViewById(R.id.item_list_phone);
        item_list_email_ = (LinearLayout) findViewById(R.id.item_list_email);
        item_list_address_ = (LinearLayout) findViewById(R.id.item_list_address);
        item_list_memo_ = (LinearLayout) findViewById(R.id.item_list_memo);

        new_item_button_ = (Button) findViewById(R.id.contact_new_item_button);
        new_item_button_.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ContactsEditActivity.this);
                builder.setTitle("选择要添加的项目");
                //    指定下拉列表的显示数据
                final String[] cities = getResources().getStringArray(R.array.contact_new_item_select_option);
                //    设置一个下拉的列表选择项
                builder.setItems(cities, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        Toast.makeText(ContactsEditActivity.this, "选择的城市为：" + which, Toast.LENGTH_SHORT).show();
                    }
                });
                builder.show();
            }
        });

    }

    private void addNewItemView(ContactItem.ItemType type) {
        addNewItemView(type, null);
    }

    private void addNewItemView(ContactItem item) {
        addNewItemView(ContactItem.ItemType.values()[item.type], item);
    }

    private void addNewItemView(ContactItem.ItemType type, ContactItem item) {
        LinearLayout new_item_view =(LinearLayout)getLayoutInflater().inflate(
                R.layout.content_contacts_item,
                null
        );
        Spinner new_spinner = (Spinner)new_item_view.getChildAt(0);
        EditText new_edit_text = (EditText) ((TextInputLayout)new_item_view.getChildAt(1)).getChildAt(0);
        switch (type){
            case PHONE:
                setSpinnerAndEditText(
                        new_spinner,
                        new_edit_text,
                        R.array.spinner_phone_type,
                        R.string.prompt_phone,
                        InputType.TYPE_CLASS_PHONE,
                        1,
                        true
                        );
                item_list_phone_.addView(new_item_view);
                break;
            case EMAIL:
                setSpinnerAndEditText(
                        new_spinner,
                        new_edit_text,
                        R.array.spinner_email_type,
                        R.string.prompt_email,
                        InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS,
                        1,
                        true
                );
                item_list_email_.addView(new_item_view);
                break;
            case ADDRESS:
                setSpinnerAndEditText(
                        new_spinner,
                        new_edit_text,
                        R.array.spinner_address_type,
                        R.string.prompt_address,
                        InputType.TYPE_CLASS_TEXT,
                        1,
                        true
                );
                item_list_address_.addView(new_item_view);
                break;
            case MEMO:
                setSpinnerAndEditText(
                        new_spinner,
                        new_edit_text,
                        R.array.spinner_memo_type,
                        R.string.prompt_memo,
                        InputType.TYPE_CLASS_TEXT,
                        10,
                        false
                );
                item_list_memo_.addView(new_item_view);
                break;
        }
        if (item != null) {
            new_spinner.setSelection(item.inner_type);
            new_edit_text.setText(item.content);
        }
    }

    private void setSpinnerAndEditText(Spinner spinner,
                                       EditText edit_text,
                                       int spinner_select_id,
                                       int hint_id,
                                       int input_type,
                                       int max_lines,
                                       boolean single_line) {

        spinner.setAdapter(getSpinnerArrayAdapter(spinner_select_id));
        edit_text.setHint(hint_id);
        edit_text.setInputType(input_type);
        edit_text.setMaxLines(max_lines);
        edit_text.setSingleLine(single_line);
    }

    private ArrayAdapter<String> getSpinnerArrayAdapter (int res_id) {
        return new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                getResources().getStringArray(res_id)
        );
    }
}
