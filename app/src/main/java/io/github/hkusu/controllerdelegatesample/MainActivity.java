package io.github.hkusu.controllerdelegatesample;

import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;

public class MainActivity extends AppCompatActivity {

    @Bind(R.id.toolbar)
    Toolbar mToolbar;
    @Bind(R.id.todoEditText)
    EditText mTodoEditText;
    @Bind(R.id.createButton)
    Button mCreateButton;
    @Bind(R.id.countTextView)
    TextView mCountTextView;
    @Bind(R.id.todoListView)
    ListView mTodoListView;

    /** Todoデータ表示用ListViewにセットするListAdapter */
    private TodoListAdapter mTodoListAdapter;
    /** ユーザイベントをハンドリングするController */
    private UserEventController mUserEventController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this); // ButterKnife

        // ToolBarの設定
        mToolbar.setTitle(R.string.app_name);
        setSupportActionBar(mToolbar);

        // ListAdapterを作成
        mTodoListAdapter = new TodoListAdapter(
                this,
                R.layout.adapter_todo_list,
                TodoModel.getInstance().get() // ListViewに表示するデータセット
        );
        // ListViewにAdapterをセット
        mTodoListView.setAdapter(mTodoListAdapter);

        // 起動時にソフトウェアキーボードが表示されないようにする
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        // Controllerを作成
        mUserEventController = new UserEventController(this);
        mUserEventController.onCreate();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mUserEventController.onStart();
        // 画面の初期表示
        updateView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mUserEventController.onResume();
        EventBus.getDefault().register(this); // EventBus
    }

    @Override
    protected void onPause() {
        super.onPause();
        mUserEventController.onPause();
        EventBus.getDefault().unregister(this); // EventBus
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUserEventController.onDestroy();
        ButterKnife.unbind(this); // ButterKnife
    }

    /**
     * EventBusからの通知の購読（Realm上のTodoデータの変更）*Viewの操作を伴う為メインスレッドで受ける*
     *
     * @param event EventBus用のイベントクラス
     */
    @SuppressWarnings("unused")
    public void onEventMainThread(TodoModel.ChangedEvent event) {
        // 画面の表示を更新
        updateView();
    }

    /**
     * 画面の表示を更新するPrivateメソッド
     */
    @MainThread
    private void updateView() {
        // データセットの変更があった旨をAdapterへ通知
        mTodoListAdapter.notifyDataSetChanged();
        // Todoデータの件数を更新
        mCountTextView.setText(String.valueOf(TodoModel.getInstance().getSize()));
    }
}
