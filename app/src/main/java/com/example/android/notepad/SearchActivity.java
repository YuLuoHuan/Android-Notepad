package com.example.android.notepad;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

public class SearchActivity extends Activity {

    private static final String[] PROJECTION = new String[]{
            NotePad.Notes._ID,
            NotePad.Notes.COLUMN_NAME_TITLE,
            NotePad.Notes.COLUMN_NAME_NOTE,
            NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE
    };

    private EditText editText;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        editText = findViewById(R.id.editText);
        listView = findViewById(R.id.listView);


        Intent intent = getIntent();
        if (intent.getData() == null) {
            intent.setData(NotePad.Notes.CONTENT_URI);
        }

        // 搜索框 回车搜索
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    String searchText = editText.getText().toString();

                    // 通过标题或内容包含搜索内容来筛选笔记
                    String selection = NotePad.Notes.COLUMN_NAME_TITLE + " LIKE '%" + searchText + "%'" +
                            " OR " + NotePad.Notes.COLUMN_NAME_NOTE + " LIKE '%" + searchText + "%'";

                    Cursor cursor = managedQuery(
                            getIntent().getData(),
                            PROJECTION,
                            selection,
                            null,
                            NotePad.Notes.DEFAULT_SORT_ORDER
                    );

                    SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                            SearchActivity.this,
                            R.layout.noteslist_item,
                            cursor,
                            new String[] { NotePad.Notes.COLUMN_NAME_TITLE, NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE },
                            new int[] { android.R.id.text1, R.id.text_modification_date }
                    );
                    adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
                        @Override
                        public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                            if (columnIndex == cursor.getColumnIndex(NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE)) {
                                long modificationTime = cursor.getLong(columnIndex);
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                                String formattedTime = sdf.format(new Date(modificationTime));
                                ((TextView) view).setText(formattedTime);
                                return true;
                            }
                            return false;
                        }
                    });

                    listView.setAdapter(adapter);

                    return true;
                }
                return false;
            }
        });
    }
}