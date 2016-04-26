package com.never.secretcontacts;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.TextView;

import com.never.secretcontacts.util.Contact;
import com.never.secretcontacts.util.ContactItem;

public class ContactsEditActivity extends AppCompatActivity {

//    private List<View> item_view_list_ = new ArrayList<>();

    private Button new_item_button_;
    private Button change_name_button_;

    private TextView contact_name_view_;

    private LinearLayout item_list_phone_;
    private LinearLayout item_list_email_;
    private LinearLayout item_list_address_;
    private LinearLayout item_list_memo_;

    private String contact_id_;

    private String contact_name_;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts_edit);

        item_list_phone_ = (LinearLayout) findViewById(R.id.item_list_phone);
        item_list_email_ = (LinearLayout) findViewById(R.id.item_list_email);
        item_list_address_ = (LinearLayout) findViewById(R.id.item_list_address);
        item_list_memo_ = (LinearLayout) findViewById(R.id.item_list_memo);

        change_name_button_ = (Button) findViewById(R.id.change_name_button);
        change_name_button_.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ContactsEditActivity.this);
                builder.setTitle("请输入新联系人名称");

                final EditText edit_text = new EditText(ContactsEditActivity.this);
                edit_text.setText(contact_name_);
                builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (edit_text.getText().toString().equals("")) {
                            edit_text.setError("不能为空");
                        }
                        else {
                            dialog.dismiss();
                            contact_name_ = edit_text.getText().toString();
                            contact_name_view_.setText(contact_name_);
                        }
                    }
                });
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.setView(edit_text);
                builder.show();
            }
        });
        contact_name_view_ = (TextView) findViewById(R.id.contact_name);

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
                        addNewItemView(ContactItem.ItemType.values()[which]);
                    }
                });
                builder.show();
            }
        });

        Intent intent = getIntent();
        contact_id_ = intent.getStringExtra("contact_id");

        if(contact_id_.equals("")) {
            contact_name_ = "新建联系人";
            contact_name_view_.setText(contact_name_);
            addNewItemView(ContactItem.ItemType.PHONE);
            addNewItemView(ContactItem.ItemType.EMAIL);
            addNewItemView(ContactItem.ItemType.ADDRESS);
            addNewItemView(ContactItem.ItemType.MEMO);
        }
        else {

        }
    }

    private void loadContactInfo() {

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

    private void saveContact() {
        Contact contact = new Contact(contact_name_);
    }

}
