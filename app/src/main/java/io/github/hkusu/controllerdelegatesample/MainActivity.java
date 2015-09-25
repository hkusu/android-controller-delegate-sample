package io.github.hkusu.controllerdelegatesample;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnEditorAction;
import de.greenrobot.event.EventBus;

public class MainActivity extends AppCompatActivity {

    @Bind(R.id.toolbar)
    Toolbar mToolBar;
    @Bind(R.id.editText)
    EditText mEditText;
    @Bind(R.id.button)
    Button mButton;
    @Bind(R.id.textView)
    TextView mTextView;
    @Bind(R.id.listView)
    ListView mListView;

    /** Todoデータ操作モデルのインスタンス */
    private TodoModel mTodoModel;
    /** Todoデータ表示用ListViewにセットするListAdapter */
    private TodoListAdapter mTodoListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this); // ButterKnife

        //mToolBar.setTitle(R.string.app_name);
        setSupportActionBar(mToolBar);

        // Todoデータ操作モデルのインスタンスを取得
        mTodoModel = TodoModel.getInstance();
        // 起動時にソフトウェアキーボードが表示されないようにする
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        // ListAdapterを作成
        mTodoListAdapter = new TodoListAdapter(
                this,
                R.layout.adapter_todo_list,
                mTodoModel.get() // ListViewに表示するデータセット
        );
        // ListViewにAdapterをセット
        mListView.setAdapter(mTodoListAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // 画面の初期表示
        updateView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this); // EventBus
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this); // EventBus
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this); // ButterKnife
    }

    /**
     * [登録]ボタン押下
     */
    @OnClick(R.id.button)
    public void onButtonClick() {
        // 入力内容が空の場合は何もしない
        if (mEditText.getText().toString().equals("")) {
            return;
        }
        // Todoデータを登録
        registerTodo();
    }

    /**
     * 入力エリアでEnter
     *
     * @param  event キーイベント
     * @return イベント処理結果(trueは消化済み)
     */
    @OnEditorAction(R.id.editText)
    public boolean onEditTextEditorAction(KeyEvent event) {
        // 入力内容が空の場合は何もしない
        if (mEditText.getText().toString().equals("")) {
            return true;
        }
        // 前半はソフトウェアキーボードのEnterキーの判定、後半は物理キーボードでの判定
        if (event == null || (event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
            // Todoデータを登録
            registerTodo();
        }
        return true;
    }

    /**
     * EventBusからの通知の購読（Realm上のTodoデータの変更）
     *
     * @param event EventBus用のイベントクラス
     */
    @SuppressWarnings("unused")
    public void onEventMainThread(TodoModel.ChangedEvent event) {
        // 画面の表示を更新
        updateView();
    }

    /**
     * EventBusからの通知の購読（削除ボタンの押下）
     *
     * @param event EventBus用のイベントクラス
     */
    @SuppressWarnings("unused")
    public void onEventMainThread(TodoListAdapter.RemoveButtonClickedEvent event) {
        // データ操作モデルを通して削除
        mTodoModel.removeById(event.getId());
    }

    /**
     * 画面での入力内容をRealmへ登録するPrivateメソッド
     */
    private void registerTodo() {
        // Todoデータを作成
        TodoEntity todoEntity = new TodoEntity();
        todoEntity.setText(mEditText.getText().toString());
        // データ操作モデルを通して登録
        mTodoModel.createOrUpdate(todoEntity);
        // 入力内容は空にする
        mEditText.setText(null);
        // ソフトウェアキーボードを隠す
        ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
    }

    /**
     * 画面の表示を更新するPrivateメソッド
     */
    private void updateView() {
        // データセットの変更があった旨をAdapterへ通知
        mTodoListAdapter.notifyDataSetChanged();
        // Todoデータの件数を更新
        mTextView.setText(String.valueOf(mTodoModel.getSize()));
    }
}
