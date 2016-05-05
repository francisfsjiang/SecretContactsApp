package com.never.secretcontacts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.never.secretcontacts.util.CharacterParser;
import com.never.secretcontacts.util.Contact;
import com.never.secretcontacts.util.PinyinComparator;
import com.never.secretcontacts.util.SortAdapter;


public class MainActivity extends AppCompatActivity {

    private SearchView search_view_;
    private ListView sort_list_view_;
    private SideBar side_bar_;
    private TextView login_recommend_text_view_;

    /**
     * 显示字母的TextView
     */
    private TextView dialog_view_;
    private SortAdapter sort_adapter_;
//    private ClearEditText mClearEditText;

    /**
     * 汉字转换成拼音的类
     */
    private List<Contact> source_data_list_;

    /**
     * 根据拼音来排列ListView里面的数据类
     */
    private PinyinComparator pinyin_comparator_;

    private SyncService.SyncBinder sync_service_binder_;

    private ServiceConnection service_connection_ = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service_binder) {
            Log.i("service", "service connected");
            sync_service_binder_ = (SyncService.SyncBinder) service_binder;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i("service", "service disconnected");
        }
    };

    private BroadcastReceiver receiver_ = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("main", "receive from service");
            onResume();
        }
    };

    private IntentFilter receiver_filter_ = new IntentFilter();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //实例化汉字转拼音类

        pinyin_comparator_ = new PinyinComparator();
        character_parser_ = CharacterParser.getInstance();

        search_view_ = (SearchView) findViewById(R.id.search_view);
        login_recommend_text_view_ = (TextView) findViewById(R.id.login_recommend_text);
        side_bar_ = (SideBar) findViewById(R.id.side_bar);
        dialog_view_ = (TextView) findViewById(R.id.dialog);
        side_bar_.setTextView(dialog_view_);

        //设置右侧触摸监听
        side_bar_.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {

            @Override
            public void onTouchingLetterChanged(String s) {
                //该字母首次出现的位置
                int position = sort_adapter_.getPositionForSection(s.charAt(0));
                if (position != -1) {
                    sort_list_view_.setSelection(position);
                }

            }
        });

        sort_list_view_ = (ListView) findViewById(R.id.country_lvcountry);
        sort_list_view_.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                //这里要利用adapter.getItem(position)来获取当前position所对应的对象
                Intent intent = new Intent(MainActivity.this, ContactsEditActivity.class);
                intent.putExtra("contact_id", ((Contact) sort_adapter_.getItem(position)).getId());
                startActivity(intent);

            }
        });



//        mClearEditText = VClearEditText) findViewById(R.id.filter_edit);
//
//        //根据输入框输入值的改变来过滤搜索
//        mClearEditText.addTextChangedListener(new TextWatcher() {
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                //当输入框里面的值为空，更新为原来的列表，否则为过滤数据列表
//                filterData(s.toString());
//            }
//
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count,
//                                          int after) {
//
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//            }
//        });

        receiver_filter_.addAction("UPDATE_UI");
        registerReceiver(receiver_, receiver_filter_);



        Intent bind_intent = new Intent(this, SyncService.class);
        bindService(bind_intent, service_connection_, BIND_AUTO_CREATE);

        Intent service_intent = new Intent(this, SyncService.class);
        startService(service_intent);

    }


    @Override
    protected void onResume() {
        super.onResume();
        invalidateOptionsMenu();

        if(MyApp.checkLoginStatus()) {
            sort_list_view_.setVisibility(View.VISIBLE);
            side_bar_.setVisibility(View.VISIBLE);
            search_view_.setVisibility(View.VISIBLE);
            login_recommend_text_view_.setVisibility(View.GONE);
        }
        else {
            sort_list_view_.setVisibility(View.GONE);
            side_bar_.setVisibility(View.GONE);
            search_view_.setVisibility(View.GONE);
            login_recommend_text_view_.setVisibility(View.VISIBLE);
        }

        source_data_list_ = MyApp.contacts_manager_.getAllContacts();

        // 根据a-z进行排序源数据
        Collections.sort(source_data_list_, pinyin_comparator_);
        sort_adapter_ = new SortAdapter(this, source_data_list_);
        sort_list_view_.setAdapter(sort_adapter_);
    }

    private static final boolean[] menu_item_visible_unlogged        = {true, true, false, false, false, false, false, false};
    private static final boolean[] menu_item_visible_logged_no_key   = {false, false, true, true, false, true, true, true};
    private static final boolean[] menu_item_visible_logged_with_key = {false, false, true, true, true, false, true, true};


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        if(MyApp.checkLoginStatus()) {
            if (MyApp.checkKeyStatus()) {
                for (int i = 0; i < menu.size(); i++)
                    menu.getItem(i).setVisible(menu_item_visible_logged_with_key[i]);
            }
            else {
                for (int i = 0; i < menu.size(); i++)
                    menu.getItem(i).setVisible(menu_item_visible_logged_no_key[i]);
            }
        }
        else {
            for (int i = 0; i < menu.size(); i++)
                menu.getItem(i).setVisible(menu_item_visible_unlogged[i]);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.menu_login) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            return true;
        }
        else if (id == R.id.menu_register) {
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(intent);
            return true;
        }
        else if (id == R.id.menu_logout) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setMessage("确认退出吗？此操作将会清除您的密钥，您下次将需要密钥恢复密码来恢复密钥");
            builder.setTitle("提示");
            builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    MyApp.clearLoginStatus();
                    onResume();
                }
            });
            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.create().show();
            return true;
        }
        //noinspection SimplifiableIfStatement
        else if (id == R.id.menu_settings) {
            return true;
        }
        else if (id == R.id.menu_key) {
            Intent intent = new Intent(MainActivity.this, KeyActivity.class);
            startActivity(intent);
            return true;
        }
        else if (id == R.id.menu_new) {
            Intent intent = new Intent(MainActivity.this, ContactsEditActivity.class);
            intent.putExtra("contact_id", "");
            startActivity(intent);
            return true;
        }
        else if (id == R.id.menu_sync) {
            sync_service_binder_.syncNow();
        }

        return super.onOptionsItemSelected(item);
    }

    private CharacterParser character_parser_;
    /**
     * 根据输入框中的值来过滤数据并更新ListView
     * @param filterStr
     */
    private void filterData(String filterStr) {
        List<Contact> filterDateList = new ArrayList<Contact>();

        if (TextUtils.isEmpty(filterStr)) {
            filterDateList = source_data_list_;
        } else {
            filterDateList.clear();
            for (Contact sortModel : source_data_list_) {
                String name = sortModel.getName();
                if (name.toUpperCase().indexOf(
                        filterStr.toString().toUpperCase()) != -1
                        || character_parser_.getSelling(name).toUpperCase()
                        .startsWith(filterStr.toString().toUpperCase())) {
                    filterDateList.add(sortModel);
                }
            }
        }

        // 根据a-z进行排序
        Collections.sort(filterDateList, pinyin_comparator_);
        sort_adapter_.updateListView(filterDateList);
    }

    @Override
    protected void onDestroy() {
        unbindService(service_connection_);
        if (receiver_ != null) {
            unregisterReceiver(receiver_);
            receiver_ = null;
        }
        super.onDestroy();
    }

}
