# Android notepad

/*

 基础功能


*/

##笔记列表显示时间戳

private static final String[] PROJECTION = new String[] {
NotePad.Notes._ID, // 0
NotePad.Notes.COLUMN_NAME_TITLE, // 1
NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE
}

// 定义一个字符串数组，用于存储要在视图中显示的光标列的名称。
// 初始化为标题列和时间戳。
String[] dataColumns =
{ NotePad.Notes.COLUMN_NAME_TITLE,NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE } ;




##笔记内容搜索功能

onCreate 方法是活动的生命周期方法之一，在活动创建时调用，并利用setContentView(R.layout.activity_search) 设置当前活动的布局文件为 activity_search，findViewById 方法用于获取布局文件中的视图组件。

--设置搜索框的监听器
OnEditorActionListener，当用户在搜索框中按下回车键时触发，如果按下的是回车键（EditorInfo.IME_ACTION_SEARCH），则获取搜索框中的文本

--查询并更新列表视图
构建 SQL 查询语句，通过标题或内容包含搜索内容来筛选笔记，使用 managedQuery 方法执行查询，返回一个 Cursor 对象

--设置适配器并绑定数据到列表视图
使用一个 SimpleCursorAdapter 来将数据绑定到 ListView 的每一项





/*

 拓展功能：UI美化：改变文字颜色、选择背景图片


*/

##改变文字颜色

根据点击的颜色块的ID（通过view.getId()获取），将对应的颜色值赋值给临时变量selectedColor，并且保存颜色值到SharedPreferences
使用Toast.makeText(this, "设置成功", Toast.LENGTH_SHORT).show();显示一个短暂的提示信息，告知用户设置成功



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