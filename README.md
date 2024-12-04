# Android notepad


/*

基础功能

*/

1.首先定义了四个菜单项：menu_add, menu_paste, menu_search, 和 menu_setting

<?xml version="1.0" encoding="utf-8"?>
<menu xmlns:android="http://schemas.android.com/apk/res/android">
    <!--  This is our one standard application action (creating a new note). -->
    <item android:id="@+id/menu_add"
          android:icon="@drawable/ic_menu_compose"
          android:title="@string/menu_add"
          android:alphabeticShortcut='a'
          android:showAsAction="always" />
    <!--  If there is currently data in the clipboard, this adds a PASTE menu item to the menu
          so that the user can paste in the data.. -->
    <item android:id="@+id/menu_paste"
          android:icon="@drawable/ic_menu_compose"
          android:title="@string/menu_paste"
          android:alphabeticShortcut='p' />

    <item android:id="@+id/menu_search"
        android:title="@string/menu_search" />

    <item android:id="@+id/menu_setting"
        android:title="@string/menu_setting" />
</menu>

![菜单栏2024-12-01 164617](https://github.com/user-attachments/assets/010e7ba0-29a0-4921-bb10-22a9201ba8e8)




2.笔记列表显示时间戳

private static final String[] PROJECTION = new String[] {
NotePad.Notes._ID, // 0
NotePad.Notes.COLUMN_NAME_TITLE, // 1
NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE
}

// 定义一个字符串数组，用于存储要在视图中显示的光标列的名称。
// 初始化为标题列和时间戳。

String[] dataColumns =
{ NotePad.Notes.COLUMN_NAME_TITLE,NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE } ;


--格式化日期：从 Cursor 中获取长整型的修改时间，使用 SimpleDateFormat 将时间格式化为 "yyyy-MM-dd HH:mm" 格式，将格式化后的时间字符串设置到对应的 TextView 上

 int[] viewIDs = { android.R.id.text1, R.id.text_modification_date };

        // Creates the backing adapter for the ListView.
        SimpleCursorAdapter adapter
            = new SimpleCursorAdapter(
                      this,                             // The Context for the ListView
                      R.layout.noteslist_item,          // Points to the XML for a list item
                      cursor,                           // The cursor to get items from
                      dataColumns,
                      viewIDs
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


--设置时间戳的位置
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="?android:attr/listPreferredItemHeight"
    android:orientation="horizontal">

    <TextView
        android:id="@android:id/text1"
        android:layout_width="0dp"
        android:layout_height="match_parent"
        android:layout_weight="1"
        android:textAppearance="?android:attr/textAppearanceLarge"
        android:gravity="center_vertical"
        android:paddingLeft="5dip"
        android:singleLine="true" />

    <TextView
        android:id="@+id/text_modification_date"
        android:layout_width="wrap_content"
        android:layout_height="match_parent"
        android:gravity="center_vertical"
        android:paddingRight="5dip"
        android:text="aaa"/>

</LinearLayout>

![时间戳 2024-12-01 160013](https://github.com/user-attachments/assets/c56ec0ec-142d-417b-bddc-3dd68d0b2d98)



3.笔记内容搜索功能

onCreate在活动创建时调用，并利用setContentView(R.layout.activity_search) 设置当前活动的布局文件为 activity_search，findViewById 方法用于获取布局文件中的视图组件。

--设置搜索框的监听器
OnEditorActionListener，当用户在搜索框中按下回车键时触发，如果按下的是回车键（EditorInfo.IME_ACTION_SEARCH），则获取搜索框中的文本

--查询并更新列表视图
构建 SQL 查询语句，通过标题或内容包含搜索内容来筛选笔记，使用 managedQuery 方法执行查询，返回一个 Cursor 对象

--设置适配器并绑定数据到列表视图
使用一个 SimpleCursorAdapter 来将数据绑定到 ListView 的每一项

具体实现代码如下：

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


![搜索 2024-12-01 160334](https://github.com/user-attachments/assets/edcb0606-b5a3-43ac-a02b-ace3c6fbc96a)


/*

 拓展功能：改变文字颜色、更改背景


*/

##改变文本颜色

根据点击的颜色块的ID（通过view.getId()获取），将对应的颜色值赋值给临时变量selectedColor，并且保存颜色值到SharedPreferences
使用Toast.makeText(this, "设置成功", Toast.LENGTH_SHORT).show();显示一个短暂的提示信息，告知用户设置成功

    private static final int SELECT_IMAGE_REQUEST_CODE = 1;

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        sharedPreferences = getSharedPreferences("setting", MODE_PRIVATE);

        // 获取上一次保存的颜色值，如果没有则默认为黑色
        selectedColor = sharedPreferences.getInt("selectedColor", Color.BLACK);



        findViewById(R.id.btSelectBg).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.setType("image/*");
                startActivityForResult(intent, SELECT_IMAGE_REQUEST_CODE);
            }
        });
    }

 public void onColorClick(View view) {
        int id = view.getId();
        if (id == R.id.viewBlue) {
            selectedColor = getResources().getColor(android.R.color.holo_blue_light);
        } else if (id == R.id.viewGreen) {
            selectedColor = getResources().getColor(android.R.color.holo_green_light);
        } else if (id == R.id.viewOrange) {
            selectedColor = getResources().getColor(android.R.color.holo_orange_light);
        } else if (id == R.id.viewRed) {
            selectedColor = getResources().getColor(android.R.color.holo_red_light);
        } else if (id == R.id.viewBlack) {
            selectedColor = getResources().getColor(android.R.color.black);
        }

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("selectedColor", selectedColor);
        editor.apply();

        Toast.makeText(this, "设置成功", Toast.LENGTH_SHORT).show();
    }

![文字颜色改变 2024-12-01 161619](https://github.com/user-attachments/assets/a28e9b51-5aa7-4baf-bc49-b01537b2f879)


##背景图片

--获取图片
    创建一个新的 Intent 对象，使用liveFolderIntent.setData(NotePad.Notes.LIVE_FOLDER_URI)设置了一个数据 URI，指向内容提供者支持的文件夹

    //先从资源文件中获取实时文件夹的名称，将这个名称作为附加字符串添加到 liveFolderIntent 中
    
    String foldername = getString(R.string.live_folder_name);
    liveFolderIntent.putExtra(LiveFolders.EXTRA_LIVE_FOLDER_NAME, foldername);

    //从当前上下文中加载图标资源 R.drawable.live_folder_notes。然后，将这个图标资源作为附加资源添加到 liveFolderIntent 中
    
    ShortcutIconResource foldericon = Intent.ShortcutIconResource.fromContext(this, R.drawable.live_folder_notes);
    liveFolderIntent.putExtra(LiveFolders.EXTRA_LIVE_FOLDER_ICON, foldericon);
    liveFolderIntent.putExtra(LiveFolders.EXTRA_LIVE_FOLDER_DISPLAY_MODE, LiveFolders.DISPLAY_MODE_LIST)


--对图片进行大小和尺寸的检查与调整
  getFileSize(this, selectedImageUri) 是一个自定义方法，用于获取文件大小，再通过 selectedImageUri 获取图片的URI，并检查其大小是否超过5MB。然后使用 MediaStore.Images.Media.getBitmap 从内容提供者中加载位图，计算缩放比例 (scaleFactor)，确保图片的最大边不超过700像素， Matrix 类进行图像缩放。建缩放后的位图 (Bitmap.createBitmap)

 @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SELECT_IMAGE_REQUEST_CODE && resultCode == RESULT_OK && data!= null) {
            Uri selectedImageUri = data.getData();
            if (selectedImageUri!= null) {
                long fileSize = 0;
                try {
                    fileSize = getFileSize(this, selectedImageUri);
                } catch (Exception e) {
                    Toast.makeText(this, e.getMessage().toString(), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (fileSize > 5 * 1024 * 1024) {
                    Toast.makeText(this, "文件不能超过 5MB", Toast.LENGTH_SHORT).show();
                    return;
                }


                try {
                    Bitmap originalBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
                    float scaleFactor;
                    int originalWidth = originalBitmap.getWidth();
                    int originalHeight = originalBitmap.getHeight();

                    if (originalWidth > originalHeight && originalWidth > 700) {
                        scaleFactor = (float) 700 / originalWidth;
                    } else if (originalHeight > originalWidth && originalHeight > 700) {
                        scaleFactor = (float) 700 / originalHeight;
                    } else {
                        scaleFactor = 1;
                    }

                    Matrix matrix = new Matrix();
                    matrix.postScale(scaleFactor, scaleFactor);

                    Bitmap scaledBitmap = Bitmap.createBitmap(originalBitmap, 0, 0, originalWidth, originalHeight, matrix, true);

                    saveBg(scaledBitmap);

                    if (!originalBitmap.isRecycled()) {
                        originalBitmap.recycle();
                    }
                    originalBitmap = null;

                    if (!scaledBitmap.isRecycled()) {
                        scaledBitmap.recycle();
                    }
                    scaledBitmap = null;

                    Toast.makeText(this, "设置成功", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(this, "选择背景图片失败", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

![更改背景 2024-12-01 173843](https://github.com/user-attachments/assets/561a3c88-96c7-49e7-8ba9-44f2a09bd4a1)


--用LinearLayout定义了一个垂直方向的线性布局，一个编辑文本框（EditText）和一个列表视图（ListView）
  <?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/main"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".SearchActivity"
    android:orientation="vertical">

    <EditText
        android:id="@+id/editText"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:hint="Search title or content"
        android:imeOptions="actionSearch"
        android:inputType="text" />

    <ListView
        android:id="@+id/listView"
        android:layout_width="match_parent"
        android:layout_height="wrap_content">
    </ListView>

</LinearLayout>


--创建了一个垂直排列的线性布局（LinearLayout），TextView：显示“选择颜色”的文本；Button：用于选择背景图片
android:layout_width="wrap_content" 和 android:layout_height="wrap_content"：根据内容调整视图大小。
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/main"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".SettingActivity"
    android:orientation="vertical"
    android:padding="10dp">

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="选择颜色"/>

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginTop="10dp"
        android:orientation="horizontal">

        <View
            android:id="@+id/viewBlue"
            android:layout_width="20dp"
            android:layout_height="20dp"
            android:background="@android:color/holo_blue_light"
            android:onClick="onColorClick"/>

        <View
            android:id="@+id/viewGreen"
            android:layout_width="20dp"
            android:layout_height="20dp"
            android:layout_marginLeft="10dp"
            android:background="@android:color/holo_green_light"
            android:onClick="onColorClick"/>

        <View
            android:id="@+id/viewOrange"
            android:layout_width="20dp"
            android:layout_height="20dp"
            android:layout_marginLeft="10dp"
            android:background="@android:color/holo_orange_light"
            android:onClick="onColorClick"/>

        <View
            android:id="@+id/viewRed"
            android:layout_width="20dp"
            android:layout_height="20dp"
            android:layout_marginLeft="10dp"
            android:background="@android:color/holo_red_light"
            android:onClick="onColorClick"/>

        <View
            android:id="@+id/viewBlack"
            android:layout_width="20dp"
            android:layout_height="20dp"
            android:layout_marginLeft="10dp"
            android:background="@android:color/black"
            android:onClick="onColorClick"/>

    </LinearLayout>

    <Button
        android:id="@+id/btSelectBg"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginTop="30dp"
        android:text="选择图片" />
</LinearLayout>

![设置界面  2024-12-01 164647](https://github.com/user-attachments/assets/308e2542-07b5-42c1-8bc3-faeb462526f0)


