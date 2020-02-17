package com.crysis.bookshelf;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.yzq.zxinglibrary.android.CaptureActivity;
import com.yzq.zxinglibrary.common.Constant;

import android.os.Handler;
import android.os.Message;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.SubMenu;
import android.view.View;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.MenuItem;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    //书架列表BookShelves
    public ArrayList<String> Bookshelves;

    //标签列表Labels
    public ArrayList<String> Labels;
    //被选中的标签
    public int label_selected;

    // 书本列表BookList
    public ArrayList<Bookitem> bookitemList;

    // 添加的书籍
    Bookitem externBookitem = new Bookitem();

    //侧滑菜单
    Menu nav_menu;

    // ListOperator实例
    ListOperator listOperator = new ListOperator();

    // 手动输入的ISBN码
    String isbn_human = null;

    // 添加的书架
    String bookshelf_add = "";

    //被选中的书架，默认为“所有”（0）
    private static int spinner_bookshelf_selected = 0;

    // 声明浮动菜单及其子按钮
    FloatingActionMenu materialDesignFAM;
    FloatingActionButton floatingActionButton1, floatingActionButton2, floatingActionButton3, floatingActionButton4;
    private String[] isbn_test = {"9787553623511"};

    // 书架Spinner
    Spinner spinner_bookshelf;

    // 设置相机请求常量
    public static final int REQUEST_CODE_SCAN = 1;
    // 设置由MainActivity的listviewItem到BookDetail的请求常量
    public static final int REQUEST_CODE_ADD = 2;
    // 设置添加书籍请求常量
    public static final int REQUEST_CODE_DETAIL = 3;
    // 设置保存添加书籍成功返回的结果常量
    public static final int RESULT_ADD_OK = 4;
    // 设置取消保存添加书籍返回的结果常量
    public static final int RESULT_ADD_CANCEL = 5;
    // 设置从BookDetail返回的结果常量
    public final static int RESULT_DETAIL_DELETE = 6;
    // 设置保存修改书籍成功返回的结果常量
    public final static int RESULT_ADD_CHANGE = 7;
    // 设置手动录入添加书籍的请求常量
    public final static int REQUEST_MANUAL_ADD = 8;
    // 设置手动录入添加书籍的结果常量
    public final static int RESULT_MANUAL_ADD_OK = 9;

    // ListViewAdapter全局
    private ListViewAdapter adapter;

    // 声明搜索视图
    SearchView searchView;



    // 调用测试

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 主界面
        setContentView(R.layout.activity_main);
        // 工具栏
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //初始化书架数据
        initBookShelf();

        // 默认初始化书架“所有”的书籍数据
        initBookItemList(spinner_bookshelf_selected);

        //初始化标签列表
        initLabel();
        label_selected = Labels.size();


        // 实例化一个Listview
        adapter = new ListViewAdapter(MainActivity.this, R.layout.item_booklist_listview, bookitemList);
        ListView listview = findViewById(R.id.mylistview);
        listview.setAdapter(adapter);

        // ListViewitem长按事件——删除书籍
        listview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Bookitem bookitem_selected = bookitemList.get(position);
                if(spinner_bookshelf.getSelectedItem().equals("所有")) {
                    if (bookitem_selected.getTitle() != null)
                        Toast.makeText(MainActivity.this, "删除了 " + " " + bookitem_selected.getTitle(), Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(MainActivity.this, "删除了无名书籍", Toast.LENGTH_SHORT).show();
                    bookitemList.remove(position);
                    adapter.notifyDataSetChanged();
                    //数据持久化
                    String file = "Bookshelf" + spinner_bookshelf_selected + ".dat";
                    listOperator.save(MainActivity.this, bookitemList, file);
                }
                else{
                    Toast.makeText(MainActivity.this, "请设置书架为“所有”再进行删除", Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });

        // ListViewItem点击事件——跳转至书籍详情界面
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 点击的是headerView或者footView
                if(id == -1)
                    return;
                int realPosition = (int)id;
                Bookitem bookitem;
                bookitem = bookitemList.get(realPosition);
                Toast.makeText(MainActivity.this, "点击了" + bookitem.getTitle().toString(), Toast.LENGTH_LONG).show();
                Intent intent = new Intent(MainActivity.this, BookDetail.class);
                intent.putExtra("bookitem", bookitem);
                intent.putExtra("Position", realPosition);
                startActivityForResult(intent, REQUEST_CODE_DETAIL);
            }
        });

        // 绑定Spinner组件
        spinner_bookshelf = findViewById(R.id.select_bookshelf);
//        final String[] bookshelfs = {"所有","默认书架","添加书架"};
        //创建ArrayAdapter对象
        final ArrayAdapter<String> spinner_adapter = new ArrayAdapter<String>(this,R.layout.simple_spinner_list_item,Bookshelves);
        spinner_bookshelf.setAdapter(spinner_adapter);
        /**选项选择监听*/
        spinner_bookshelf.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //当选中的书架不是“添加书架”时，记录本次选中的书架，并重新加载menu
//                if (spinner_bookshelf.getSelectedItem() != "添加书架")
//                {
//                    spinner_bookshelf_selected = position;
//                    invalidateOptionsMenu();
//                }

                //根据选中的书架设置listviewAdapter
                //选中的书架为“添加书架”时
                if (position == (Bookshelves.size() - 1)){
                    showInputDialogForAddingBookshelf("请输入书架名称");
                    //当用户输入书架名称后，添加书架；当用户未输入书架名称时，不添加书架，设置选中的书架为上一次选中的书架
//                    if (bookshelf_add != "") {
//                        Log.d("onItemSelected", "run3");
//                        //在spinner_bookshelf中添加新的书架
//                        ArrayList<String> new_bookshelf = insertBookshelf(Bookshelves, bookshelf_add);
//                        Bookshelves = new_bookshelf;
//                        //String[] new_bookshelfs = insert(bookshelfs, bookshelf_add);
//                        ArrayAdapter<String> spinner_new_adapter = new ArrayAdapter<String>(MainActivity.this, R.layout.simple_spinner_list_item, Bookshelves);
//                        spinner_bookshelf.setAdapter(spinner_new_adapter);
//
//                        //重新设置listviewAdapter
//                        initBookItemList(Bookshelves.size() - 2);
//                        adapter = new ListViewAdapter(MainActivity.this, R.layout.item_booklist_listview, bookitemList);
//                    }else {
//                        spinner_bookshelf.setSelection(spinner_bookshelf_selected);
//                    }
                }
                //选中的书架不是“添加书架”时，加载选中的书架
                else {
                    spinner_bookshelf_selected = position;
                    invalidateOptionsMenu();

                    initBookItemList(position);
                    //如果当前处于标签筛选下，则对书籍进行筛选
                    if (label_selected != Labels.size()){
                        for (int i = bookitemList.size() - 1; i >= 0; i--)
                        {
                            if (!bookitemList.get(i).getLabels().equals(Labels.get(label_selected)))
                                bookitemList.remove(i);
                        }
                    }
                    adapter = new ListViewAdapter(MainActivity.this, R.layout.item_booklist_listview, bookitemList);

                    //重新加载listview
                    ListView listview = findViewById(R.id.mylistview);
                    listview.setAdapter(adapter);
                }

            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        // 绑定浮动按钮组件
        materialDesignFAM = findViewById(R.id.material_design_android_floating_action_menu);
        floatingActionButton1 = findViewById(R.id.material_design_floating_action_menu_item1);
        floatingActionButton2 = findViewById(R.id.material_design_floating_action_menu_item2);
        floatingActionButton3 = findViewById(R.id.material_design_floating_action_menu_item3);
        floatingActionButton4 = findViewById(R.id.material_design_floating_action_menu_item4);
        // 添加书籍
        floatingActionButton1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //TODO
                //调用zxing扫码及其自带界面
                materialDesignFAM.close(true);
                Intent intent = new Intent(MainActivity.this, CaptureActivity.class);
                startActivityForResult(intent, REQUEST_CODE_SCAN);
            }
        });

        // 批量添加书籍
        floatingActionButton2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //TODO
                //等待对话框
                /* 等待Dialog具有屏蔽其他控件的交互能力
                 * @setCancelable 为使屏幕不可点击，设置为不可取消(false)
                 * 下载等事件完成后，主动调用函数关闭该Dialog
                 */
                materialDesignFAM.close(true);
                final ProgressDialog waitingDialog=
                        new ProgressDialog(MainActivity.this);
                waitingDialog.setTitle("提示");
                waitingDialog.setMessage("正在获取书籍数据...");
                waitingDialog.setIndeterminate(true);
                waitingDialog.setCancelable(false);
                waitingDialog.show();

                //测试listview刷新
                @SuppressLint("HandlerLeak")
                Handler handler = new Handler() {
                    public void handleMessage(Message msg) {
                        // 获取书籍数据成功，等待dialog隐藏
                        waitingDialog.setCancelable(true);
                        waitingDialog.hide();
                        // 输出测试
                        System.out.println("=============================" + externBookitem.getAuthor());
                        System.out.println("==========" + externBookitem.getTranslator());
                        System.out.println("==========" + externBookitem.getPubDate());
                        // 跳转至第BookEdit界面，并传输数据
                        // 创建意图对象
                        Intent intent = new Intent(MainActivity.this, Editbook.class);
                        // 设置传递键值对
                        intent.putExtra("Title", externBookitem.getTitle());
                        intent.putExtra("Author", externBookitem.getAuthor());
                        intent.putExtra("Translator", externBookitem.getTranslator());
                        intent.putExtra("Publisher", externBookitem.getPublisher());
                        intent.putExtra("PubYear", externBookitem.getPubYear());
                        intent.putExtra("PubMonth", externBookitem.getPubMonth());
                        intent.putExtra("ISBN", externBookitem.getISBN());
                        intent.putExtra("Website", externBookitem.getWebsite());
                        intent.putExtra("ImageURL", externBookitem.getImageURL());
                        // 激活意图
                        startActivityForResult(intent, REQUEST_CODE_ADD);
                    }
                };
                //手动输入ISBN码调用腾讯API
                //getBookInfo_Human(handler);
                getBookInfo(handler, isbn_test[0]);
            }
        });

        // 手动输入ISBN码添加书籍
        floatingActionButton3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //TODO
                //手动录入书籍信息
                materialDesignFAM.close(true);
                showInputDialog();
            }
        });

        // 手动输入添加书籍
        floatingActionButton4.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //TODO
                //手动录入书籍信息
                materialDesignFAM.close(true);
                Intent intent = new Intent(MainActivity.this, Editbook.class);
                intent.putExtra("Manual", "111");
                startActivityForResult(intent, REQUEST_MANUAL_ADD);
            }
        });

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        nav_menu = navigationView.getMenu();
        initNav_menu();


        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

    }

    // 重载OnActivityResult，处理返回参数Intent data
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //重新加载书架
        initBookShelf();
        //重新加载侧滑菜单
        updateNav_menu();
        ArrayAdapter<String> spinner_new_adapter = new ArrayAdapter<String>(MainActivity.this, R.layout.simple_spinner_list_item , Bookshelves);
        spinner_bookshelf.setAdapter(spinner_new_adapter);
        spinner_bookshelf.setSelection(spinner_bookshelf_selected);

        // 扫描二维码/条码回传&& resultCode == RESULT_OK
        if (requestCode == REQUEST_CODE_SCAN) {
            // 扫码结束返回至主界面MainActivity后，需要跳转至Editbook界面
            if (data != null) {
                String content = data.getStringExtra(Constant.CODED_CONTENT);
//                Toast.makeText(MainActivity.this,content,Toast.LENGTH_SHORT).show();

                //Toast提示，告知用户当前请求状态
                Toast.makeText(MainActivity.this, "正在获取书籍数据，请稍候" ,Toast.LENGTH_SHORT).show();

                //测试listview刷新
                @SuppressLint("HandlerLeak")
                Handler handler = new Handler() {
                    public void handleMessage(Message msg) {
                        // Toast提示，告知用户当前请求状态
                        Toast.makeText(MainActivity.this, "数据获取成功" ,Toast.LENGTH_SHORT).show();
                        // 输出测试
                        System.out.println("=============================" + externBookitem.getAuthor());
                        System.out.println("==========" + externBookitem.getTranslator());
                        System.out.println("==========" + externBookitem.getPubDate());
                        // 跳转至第BookEdit界面，并传输数据
                        // 创建意图对象
                        Intent intent = new Intent(MainActivity.this, Editbook.class);
                        // 设置传递键值对
                        intent.putExtra("Title", externBookitem.getTitle());
                        intent.putExtra("Author", externBookitem.getAuthor());
                        intent.putExtra("Translator", externBookitem.getTranslator());
                        intent.putExtra("Publisher", externBookitem.getPublisher());
                        intent.putExtra("PubYear", externBookitem.getPubYear());
                        intent.putExtra("PubMonth", externBookitem.getPubMonth());
                        intent.putExtra("ISBN", externBookitem.getISBN());
                        intent.putExtra("Website", externBookitem.getWebsite());
                        intent.putExtra("ImageURL", externBookitem.getImageURL());
                        // 激活意图
                        startActivityForResult(intent, REQUEST_CODE_ADD);
                    }
                };
                //调用腾讯API
                getBookInfo(handler, content.toString());
            }
        }
        /***添加书籍成功***/
        if (resultCode == RESULT_ADD_OK && requestCode == REQUEST_CODE_ADD)
        {
            // 新建Bookitem对象用于接收
            Bookitem bookitem = new Bookitem();
            bookitem.setTitle(data.getStringExtra("Title"));
            bookitem.setAuthor(data.getStringExtra("Author"));
            bookitem.setTranslator(data.getStringExtra("Translator"));
            bookitem.setPubDate(data.getStringExtra("PubDate"));
            bookitem.setPublisher(data.getStringExtra("Publisher"));
            if(!data.getStringExtra("Author").contains("著") && !data.getStringExtra("Author").equals(""))
                bookitem.setAuthor_Publisher(data.getStringExtra("Author") + " 著| " + data.getStringExtra("Publisher"));
            else
                bookitem.setAuthor_Publisher(data.getStringExtra("Author") + data.getStringExtra("Publisher"));
            bookitem.setPubYear(data.getStringExtra("PubYear"));
            bookitem.setPubMonth(data.getStringExtra("PubMonth"));
            bookitem.setPubDate(data.getStringExtra("PubYear") + "-" + data.getStringExtra("PubMonth"));
            bookitem.setWebsite(data.getStringExtra("Website"));
            bookitem.setISBN(data.getStringExtra("ISBN"));
            bookitem.setReadingStatus_Text(data.getStringExtra("ReadingStatus_Text"));
            if(data.getStringExtra("ReadingStatus").equals("0"))
                bookitem.setReadingStatus(0);
            if(data.getStringExtra("ReadingStatus").equals("1"))
                bookitem.setReadingStatus(1);
            if(data.getStringExtra("ReadingStatus").equals("0"))
                bookitem.setReadingStatus(2);
            bookitem.setBookShelfName(data.getStringExtra("Bookshelf"));

            // 接收图片
            Bundle bundle_image = data.getBundleExtra("bmp");
            Bitmap bitmap_receive = (Bitmap) bundle_image.getParcelable("bmp");
            // Bitmap转换为byte[]再放入bookitem中
            Bitmap2Byte bitmap2Byte = new Bitmap2Byte();
            bookitem.setBitmap_byte(bitmap2Byte.bitmap2Bytes(bitmap_receive));

            // 添加书籍
            bookitemList.add(bookitem);
            adapter.notifyDataSetChanged();

            // 数据持久化
            String file = "Bookshelf" + spinner_bookshelf_selected + ".dat";
            listOperator.save(MainActivity.this, bookitemList, file);
        }
        /***取消添加书籍***/
        if(resultCode == RESULT_ADD_CANCEL && requestCode == REQUEST_CODE_ADD) {
            Toast.makeText(MainActivity.this,"取消添加书籍",Toast.LENGTH_SHORT).show();
        }
        /***从书籍详情界面删除书籍***/
        if(resultCode == RESULT_DETAIL_DELETE  && requestCode == REQUEST_CODE_DETAIL)
        {
            Bookitem bookitem;
            String DeleteBookitem = data.getStringExtra("DeleteBookitem");
            int DeletePosition = -1;
            DeletePosition = data.getIntExtra("DeletePosition", -1);
            bookitemList.remove(DeletePosition);
            adapter.notifyDataSetChanged();
            // 数据持久化
            String file = "Bookshelf" + spinner_bookshelf_selected + ".dat";
            listOperator.save(MainActivity.this, bookitemList, file);
        }
        /***从书籍详情界面跳转至编辑界面并修改***/
        if(resultCode == RESULT_ADD_CHANGE && requestCode == REQUEST_CODE_DETAIL) {
            int editpostion = data.getIntExtra("change_position", -1);
            // 新建Bookitem对象用于接收
            Bookitem bookitem = new Bookitem();
            bookitem = (Bookitem) data.getSerializableExtra("change_bookitem");
            if(!bookitem.getAuthor().contains("著") && !bookitem.getAuthor().equals(""))
                bookitem.setAuthor_Publisher(bookitem.getAuthor() + " 著| " + bookitem.getPublisher());
            else
                bookitem.setAuthor_Publisher(bookitem.getAuthor() + "| " + bookitem.getPublisher());
//            if(data.getStringExtra("ReadingStatus").equals("0"))
//                bookitem.setReadingStatus(0);
//            if(data.getStringExtra("ReadingStatus").equals("1"))
//                bookitem.setReadingStatus(1);
//            if(data.getStringExtra("ReadingStatus").equals("0"))
//                bookitem.setReadingStatus(2);
//            bookitem.setBookShelfName(data.getStringExtra("Bookshelf"));

            // 接收byte[]形式的图片
            byte[] bitmap_receive = bookitem.getBitmap_byte();
            bookitem.setBitmap_byte(bitmap_receive);

            String position = String.valueOf(editpostion);
            Toast.makeText(MainActivity.this, position, Toast.LENGTH_LONG).show();
            Toast.makeText(MainActivity.this, bookitem.getTitle().toString(), Toast.LENGTH_LONG).show();
            if(editpostion != -1) {
                // 先删除修改位置的item
                bookitemList.remove(editpostion);
                // 添加书籍
                bookitemList.add(editpostion, bookitem);
                adapter.notifyDataSetChanged();
                // 数据持久化
                String file = "Bookshelf" + spinner_bookshelf_selected + ".dat";
                listOperator.save(MainActivity.this, bookitemList, file);
            }
            else{
                Toast.makeText(MainActivity.this, "修改书籍信息失败", Toast.LENGTH_LONG).show();
            }
        }
        /***手动添加***/
        if(resultCode == RESULT_MANUAL_ADD_OK && requestCode == REQUEST_MANUAL_ADD)
        {
            Bookitem bookitem_manual = new Bookitem();
            bookitem_manual = (Bookitem) data.getSerializableExtra("manual_bookitem");
            bookitem_manual.setImageId(R.drawable.book_cover_default);
            if(bookitem_manual.getTitle().equals(""))
                bookitem_manual.setTitle("Default Title");
            if(!bookitem_manual.getAuthor().contains("著") && !bookitem_manual.getAuthor().equals(""))
                bookitem_manual.setAuthor_Publisher(bookitem_manual.getAuthor() + " 著| " + bookitem_manual.getAuthor());
            else
                bookitem_manual.setAuthor_Publisher(bookitem_manual.getAuthor() + "| " +  bookitem_manual.getAuthor());
            bookitemList.add(bookitem_manual);
            adapter.notifyDataSetChanged();
            // 数据持久化
            String file = "Bookshelf" + spinner_bookshelf_selected + ".dat";
            listOperator.save(MainActivity.this, bookitemList, file);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    // Toolbar菜单工具栏点击事件
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu); //设置相应的顶部toolbar
        MenuItem searchItem = menu.findItem(R.id.menu_main_search);
        //通过MenuItem得到SearchView
        searchView = (SearchView) MenuItemCompat.getActionView(searchItem);

        searchView.setSubmitButtonEnabled(true);
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                ListView listview = findViewById(R.id.mylistview);
                listview.setAdapter(adapter);
                return false;
            }
        });
        //搜索点击事件
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                Toast.makeText(MainActivity.this, s, Toast.LENGTH_SHORT).show();
                if(!s.equals("")) {
                    ArrayList<Bookitem> bookitemList_Result = new ArrayList<>();
                    String keyword = s;
                    int size = bookitemList.size();
                    int[] search_result = new int[size];
                    for (int book = 0; book < size; book++) {
                        search_result[book] = 0;
                        if (bookitemList.get(book).getAuthor() != null) {
                            String Author = bookitemList.get(book).getAuthor();
                            if (Author.contains(keyword))
                                search_result[book] = 1;
                        }
                        if (bookitemList.get(book).getTranslator() != null) {
                            String Translator = bookitemList.get(book).getTranslator();
                            if (Translator.contains(keyword))
                                search_result[book] = 1;
                        }
                        if (bookitemList.get(book).getTitle() != null) {
                            String Title = bookitemList.get(book).getTitle();
                            if (Title.contains(keyword))
                                search_result[book] = 1;
                        }
                        if (bookitemList.get(book).getPublisher() != null) {
                            String Publisher = bookitemList.get(book).getPublisher();
                            if (Publisher.contains(keyword))
                                search_result[book] = 1;
                        }
                        if (bookitemList.get(book).getPubDate() != null) {
                            String PubDate = bookitemList.get(book).getPubDate();
                            if (PubDate.contains(keyword))
                                search_result[book] = 1;
                        }
                        if (bookitemList.get(book).getBookShelfName() != null) {
                            String BookShelfName = bookitemList.get(book).getBookShelfName();
                            if (BookShelfName.contains(keyword))
                                search_result[book] = 1;
                        }
                        // 标签要做额外处理
                        if (bookitemList.get(book).getLabels() != null) {
                            String Labels = bookitemList.get(book).getLabels();
                            if (Labels.contains(keyword))
                                search_result[book] = 1;
                        }
                        // 标签要做额外处理
                        if (bookitemList.get(book).getISBN() != null) {
                            String ISBN = bookitemList.get(book).getISBN();
                            if (ISBN.contains(keyword))
                                search_result[book] = 1;
                        }
                        if (bookitemList.get(book).getNotes() != null) {
                            String Notes = bookitemList.get(book).getNotes();
                            if (Notes.contains(keyword))
                                search_result[book] = 1;
                        }
                        if (bookitemList.get(book).getReadingStatus_Text() != null) {
                            String ReadingStatus = bookitemList.get(book).getReadingStatus_Text();
                            if (ReadingStatus.contains(keyword))
                                search_result[book] = 1;
                        }
                        if (bookitemList.get(book).getWebsite() != null) {
                            String Website = bookitemList.get(book).getWebsite();
                            if (Website.contains(keyword))
                                search_result[book] = 1;
                        }
                    }
                    for(int j = 0; j < size; j++) {
                        if(search_result[j] == 1) {
                            bookitemList_Result.add(bookitemList.get(j));
                        }
                    }
                    ListViewAdapter search_adapter = new ListViewAdapter(MainActivity.this, R.layout.item_booklist_listview, bookitemList_Result);
                    ListView listview = findViewById(R.id.mylistview);
                    listview.setAdapter(search_adapter);
                }
                else {
                    ListView listview = findViewById(R.id.mylistview);
                    listview.setAdapter(adapter);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
        //搜索图标按钮(打开搜索框的按钮)的点击事件
        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("On Pressed Search","Open");
            }
        });
        return true;
    }

    @Override
    public  boolean onPrepareOptionsMenu(Menu menu) {
        //当spinner_bookshelf被选中的不是“添加书架”
        if (spinner_bookshelf_selected != Bookshelves.size() - 1){
            //当spinner_bookshelf被选中的是“所有”
            if (spinner_bookshelf_selected == 0) {
                menu.findItem(R.id.menu_main_delete_bookshelf).setVisible(false);
                menu.findItem(R.id.menu_main_rename_bookshelf).setVisible(false);
            }else{
                menu.findItem(R.id.menu_main_delete_bookshelf).setVisible(true);
                menu.findItem(R.id.menu_main_rename_bookshelf).setVisible(true);
            }
            spinner_bookshelf.setSelection(spinner_bookshelf_selected);
        }
        //标签被选中时，显示“重命名标签”和“删除标签”
        if(label_selected != Labels.size()){
            menu.findItem(R.id.menu_main_delete_label).setVisible(true);
            menu.findItem(R.id.menu_main_rename_label).setVisible(true);
        }else{
            menu.findItem(R.id.menu_main_delete_label).setVisible(false);
            menu.findItem(R.id.menu_main_rename_label).setVisible(false);
        }
        return super.onPrepareOptionsMenu(menu);


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        booknameComparator bc = new booknameComparator();//书名排序
        authorComparator ac = new authorComparator();//作者排序
        timeComparator tc = new timeComparator();//时间排序
        publisherComparator prc = new publisherComparator();//出版社排序

        //排序
        if(id == R.id.menu_main_sort) {
            showSingleChoiceDialog();
        }

        //noinspection SimplifiableIfStatement
        if (id == R.id.menu_main_rename_bookshelf) {
            Collections.sort(bookitemList, bc);
            adapter.notifyDataSetChanged();

            showInputDialogForRenameBookshelf("重命名书架");
            return true;
        }
        else if(id == R.id.menu_main_delete_bookshelf)
        {
            Collections.sort(bookitemList, ac);
            adapter.notifyDataSetChanged();

            Bookshelves = deleteBookshelf(Bookshelves);
            //将选中的书架加载为“所有”
            spinner_bookshelf_selected = 0;
            invalidateOptionsMenu();
            initBookItemList(spinner_bookshelf_selected);
            adapter = new ListViewAdapter(MainActivity.this, R.layout.item_booklist_listview, bookitemList);
            //重新加载listview
            ListView listview = findViewById(R.id.mylistview);
            listview.setAdapter(adapter);
            return true;
        }
        else if(id == R.id.menu_main_rename_label)
        {
            Collections.sort(bookitemList, tc);
            adapter.notifyDataSetChanged();

            showInputDialogForRenameLabel();
            return true;
        }
        else if(id == R.id.menu_main_delete_label)
        {
            Collections.sort(bookitemList, prc);
            adapter.notifyDataSetChanged();

            //保存即将被删除的标签
            String label_previous = Labels.get(label_selected);
            //删除标签
            Labels.remove(label_selected);
            //遍历除了“添加书架”以外的书架中的书籍，如果有标签为被删除的标签，重置其为“”
            for (int i = 0; i < Bookshelves.size() - 1; i++){
                String file = "Bookshelf" + i + ".dat";;
                ArrayList<Bookitem> bookshelf = listOperator.load(MainActivity.this, file);
                for(int j = 0; j < bookshelf.size() - 1; j++){
                    if(bookshelf.get(j).getLabels().equals(label_previous))
                        bookshelf.get(j).setLabels("");
                }
                listOperator.save(MainActivity.this, bookshelf, file);
            }
            //重新加载侧滑菜单
            updateNav_menu();

            //数据持久化
            String file = "Labels.dat";
            listOperator.save(MainActivity.this, Labels, file);

            //将当前选中的标签设为Labels.size()，即没有标签被选中
            label_selected = Labels.size();

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // 导航框点击事件
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_bookshelf) {
            //选中的标签为Labels.size()，即没有标签被选中
            label_selected = Labels.size();
            //重新加载
            updateNav_menu();
            initBookItemList(spinner_bookshelf_selected);
            adapter = new ListViewAdapter(MainActivity.this, R.layout.item_booklist_listview, bookitemList);
            //重新加载listview
            ListView listview = findViewById(R.id.mylistview);
            listview.setAdapter(adapter);
        }
        else if (id == R.id.nav_search) {
            SearchView searchView = findViewById(R.id.menu_main_search);
            searchView.setSubmitButtonEnabled(true);
            searchView.onActionViewExpanded();
//            searchView.setOnCloseListener(new SearchView.OnCloseListener() {
//                @Override
//                public boolean onClose() {
//                    ListViewAdapter new_adapter = new ListViewAdapter(MainActivity.this, R.layout.item_booklist_listview, bookitemList);
//                    ListView listview = findViewById(R.id.mylistview);
//                    listview.setAdapter(new_adapter);
//                    return false;
//                }
//            });

        }
        //对标签栏进行响应
        else if (0 <= id && id < Labels.size() - 1) {
            label_selected = id;
            //刷新菜单使“重命名标签”和“删除标签”可见
            invalidateOptionsMenu();
            //为了防止之前点击某个标签的影响，重新加载当前书架中的所有书籍
            initBookItemList(spinner_bookshelf_selected);
            //筛选标签为选中标签的书籍
            for (int i = bookitemList.size() - 1; i >= 0; i--)
            {
                if (!bookitemList.get(i).getLabels().equals(Labels.get(label_selected)))
                    bookitemList.remove(i);
            }
            adapter = new ListViewAdapter(MainActivity.this, R.layout.item_booklist_listview, bookitemList);
            //重新加载listview
            ListView listview = findViewById(R.id.mylistview);
            listview.setAdapter(adapter);
            //Toast.makeText(MainActivity.this, Labels.get(id), Toast.LENGTH_SHORT).show();
        }
        else if (id == Labels.size() - 1) {
            label_selected = Labels.size();
            showInputDialogForAddingLabel();
            //Toast.makeText(MainActivity.this, Labels.get(id), Toast.LENGTH_SHORT).show();
        }
//        else if (id == R.id.nav_default_labels) {
//
//        }
//        else if (id == R.id.nav_add_labels) {
//
//        }
//        else if (id == R.id.nav_setting) {
//
//        }
//        else if (id == R.id.nav_about) {
//
//        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //初始化书架数据
    private void initBookShelf(){
        Bookshelves = listOperator.loadBookshelf(MainActivity.this);
        //如果没有书架数据，则创建默认的书架及书架数据文件
        if (Bookshelves == null){
            Bookshelves = new ArrayList<>();
            Bookshelves.add("所有");
            Bookshelves.add("默认书架");
            Bookshelves.add("添加书架");
            listOperator.save(MainActivity.this, Bookshelves, "Bookshelves.dat");
            //Toast.makeText(MainActivity.this, "加载书架数据失败", Toast.LENGTH_LONG).show();
        }
    }

    // 初始化书籍数据
    private void initBookItemList(int Bookshelf){
        String file = "Bookshelf" + Bookshelf + ".dat";
        bookitemList = listOperator.load(MainActivity.this, file);
        //如果没有该书架的书籍数据，则创建该书架的书籍数据文件
        if(bookitemList == null){
            bookitemList = new ArrayList<Bookitem>();
            listOperator.save(MainActivity.this, bookitemList, file);
            //Toast.makeText(MainActivity.this, "加载用户数据失败", Toast.LENGTH_LONG).show();
        }
        //初始化
//        listOperator.save(MainActivity.this, bookitemList, file);
    }

    //初始化标签数据
    private void initLabel(){
        Labels = listOperator.loadLabel(MainActivity.this);
        //如果没有标签数据，则创建默认的标签及标签数据文件
        if (Labels == null){
            Labels = new ArrayList<>();
            Labels.add("测试标签");
            Labels.add("默认标签");
            Labels.add("添加新标签");
            listOperator.save(MainActivity.this, Labels, "Labels.dat");
            //Toast.makeText(MainActivity.this, "加载书架数据失败", Toast.LENGTH_LONG).show();
        }
    }

    private void initNav_menu(){
        int i = 0;
        SubMenu subMenu_label = nav_menu.addSubMenu(1, i, 0, "标签");
        for (i = 0; i < Labels.size(); i++){
            subMenu_label.add(1, i, 0, Labels.get(i));
        }

        SubMenu subMenu_setting = nav_menu.addSubMenu(2, i, 0,"其他");
        i++;
        subMenu_setting.add(2, i, 0, "设置");
        i++;
        subMenu_setting.add(2, i, 0, "关于");

        //添加图标
        for (i = 0; i < Labels.size() - 1; i++){
            subMenu_label.getItem(i).setIcon(R.drawable.ic_label);
        }
        subMenu_label.getItem(i).setIcon(R.drawable.ic_add_gray);
        subMenu_setting.getItem(0).setIcon(R.drawable.ic_setting);
        subMenu_setting.getItem(1).setIcon(R.drawable.ic_add_gray);
    }

    private void updateNav_menu(){
        //删除原本已显示的标签栏和设置栏
        nav_menu.removeGroup(1);
        nav_menu.removeGroup(2);

        //重新加载
        initNav_menu();
    }


    // 调用腾讯API并解析
    private void getBookInfo(final Handler handler, final String isbn){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Tecent_ISBN_API tecent_isbn_api = new Tecent_ISBN_API();
                externBookitem = tecent_isbn_api.getBookbyISBN(MainActivity.this, isbn);
                handler.sendEmptyMessage(1);
            }
        }).start();
    }

    // 四种排序类
    //重载比较方法 在排序上使用
    //按书名排序
    public class booknameComparator implements Comparator<Bookitem> {
        @Override
        public int compare(Bookitem o1, Bookitem o2) {
            return (Collator.getInstance().compare(o1.getTitle(),o2.getTitle()));
        }
    }
    //按作者排序
    public class authorComparator implements Comparator<Bookitem>{
        @Override
        public int compare(Bookitem o1, Bookitem o2) {
            return (Collator.getInstance().compare(o1.getAuthor(),o2.getAuthor()));
        }
    }
    //按出版时间排序
    public class timeComparator implements Comparator<Bookitem>{
        @Override
        public int compare(Bookitem o1, Bookitem o2) {
            if( (Collator.getInstance().compare(o1.getPubYear(),o2.getPubYear())) == 0) {
                return Collator.getInstance().compare(o1.getPubMonth(), o2.getPubMonth());
            }
            else
                return Collator.getInstance().compare(o1.getPubYear(),o2.getPubYear());
        }
    }
    //按出版社排序
    public class publisherComparator implements Comparator<Bookitem>{
        @Override
        public int compare(Bookitem o2, Bookitem o1) {
            return (Collator.getInstance().compare(o1.getPublisher(),o2.getPublisher()));
        }
    }
    //ComparatorEnd

    // 手动输入对话框类For ISBN
    private void showInputDialog(){
        /*@setView 装入一个EditView
         */
        final EditText editText = new EditText(MainActivity.this);
        AlertDialog.Builder inputDialog = new AlertDialog.Builder(MainActivity.this);
        inputDialog.setTitle("请手动输入书籍ISBN码").setView(editText);
        inputDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (editText.getText().toString().length() == 13) {
                            //测试listview刷新
                            @SuppressLint("HandlerLeak")
                            Handler handler = new Handler() {
                                public void handleMessage(Message msg) {
                                    // 输出测试
                                    System.out.println("=============================" + externBookitem.getAuthor());
                                    System.out.println("==========" + externBookitem.getTranslator());
                                    System.out.println("==========" + externBookitem.getPubDate());
                                    // 跳转至第BookEdit界面，并传输数据
                                    // 创建意图对象
                                    Intent intent = new Intent(MainActivity.this, Editbook.class);
                                    // 设置传递键值对
                                    intent.putExtra("Title", externBookitem.getTitle());
                                    intent.putExtra("Author", externBookitem.getAuthor());
                                    intent.putExtra("Translator", externBookitem.getTranslator());
                                    intent.putExtra("Publisher", externBookitem.getPublisher());
                                    intent.putExtra("PubYear", externBookitem.getPubYear());
                                    intent.putExtra("PubMonth", externBookitem.getPubMonth());
                                    intent.putExtra("ISBN", externBookitem.getISBN());
                                    intent.putExtra("Website", externBookitem.getWebsite());
                                    intent.putExtra("ImageURL", externBookitem.getImageURL());
                                    // 激活意图
                                    startActivityForResult(intent, REQUEST_CODE_ADD);
                                }
                            };
                            //手动输入ISBN码调用腾讯API
                            //getBookInfo_Human(handler);
                            getBookInfo(handler, editText.getText().toString());
                        }
                        else {
                            Toast.makeText(MainActivity.this, "输入的ISBN码有误，请重新输入添加", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).show();
    }

    // 手动输入对话框类For Search
    private void showInputDialogForSearch(String title){
        /*@setView 装入一个EditView
         */
        final EditText editText = new EditText(MainActivity.this);
        AlertDialog.Builder inputDialog = new AlertDialog.Builder(MainActivity.this);
        inputDialog.setTitle(title.toString()).setView(editText);
        inputDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (editText.getText().toString() != null) {
                            Toast.makeText(MainActivity.this, editText.getText().toString(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }).show();
    }

    // 手动输入对话框类ForAddingBookshelf
    private void showInputDialogForAddingBookshelf(String title){
        /*@setView 装入一个EditView
         */
        final EditText editText = new EditText(MainActivity.this);
        AlertDialog.Builder inputDialog = new AlertDialog.Builder(MainActivity.this);
        inputDialog.setTitle(title.toString()).setView(editText);
        inputDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //将spinner_bookshelf的当前选中项设为点击“添加书架”之前选中的书架
                spinner_bookshelf.setSelection(spinner_bookshelf_selected);
            }
        });
        inputDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String input = editText.getText().toString().trim();

                        //当前用户未输入书架名时，提示“用户书架名称不能为空，书架添加失败”
                        //并将spinner_bookshelf的当前选中项设为点击“添加书架”之前选中的书架
                        if (input.isEmpty())
                        {
                            Toast.makeText(MainActivity.this, "书架名称不能为空，书架添加失败", Toast.LENGTH_SHORT).show();
                            spinner_bookshelf.setSelection(spinner_bookshelf_selected);
                        }else{
                            bookshelf_add = editText.getText().toString();
                            //当用户输入书架名称后，添加书架
                            //在spinner_bookshelf中添加新的书架
                            ArrayList<String> new_bookshelf = insertBookshelf(Bookshelves, bookshelf_add);
                            Bookshelves = new_bookshelf;
                            ArrayAdapter<String> spinner_new_adapter = new ArrayAdapter<String>(MainActivity.this, R.layout.simple_spinner_list_item, Bookshelves);
                            spinner_bookshelf.setAdapter(spinner_new_adapter);
                            //设置当前选中的书架为新添加的书架
                            spinner_bookshelf_selected = Bookshelves.size() - 2;
                            spinner_bookshelf.setSelection(spinner_bookshelf_selected);

                            //重新设置listviewAdapter
                            initBookItemList(Bookshelves.size() - 2);
                            adapter = new ListViewAdapter(MainActivity.this, R.layout.item_booklist_listview, bookitemList);
                            //重新加载listview
                            ListView listview = findViewById(R.id.mylistview);
                            listview.setAdapter(adapter);

                            //数据持久化，包括书架数据Bookshelves.dat和新建书架内的书籍数据的持久化
                            String file = "Bookshelves.dat" ;
                            listOperator.save(MainActivity.this, Bookshelves, file);
                            initBookItemList(spinner_bookshelf_selected);
                        }
//                        if (!editText.getText().toString().equals("")) {
//                            bookshelf_add = editText.getText().toString();
//                        }
                    }
                }).show();
    }

    private void showInputDialogForRenameBookshelf(String title){
        /*@setView 装入一个EditView
         */
        final EditText editText = new EditText(MainActivity.this);
        AlertDialog.Builder inputDialog = new AlertDialog.Builder(MainActivity.this);

        //设置editText显示的内容为当前书架的名字，并将光标移到最后
        editText.setText(Bookshelves.get(spinner_bookshelf_selected));
        editText.setSelection(Bookshelves.get(spinner_bookshelf_selected).length());

        inputDialog.setTitle(title.toString()).setView(editText);
        inputDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        inputDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String input = editText.getText().toString().trim();
                        if (input.isEmpty()) {
                            Toast.makeText(MainActivity.this, "书架名称不能为空，书架重命名失败", Toast.LENGTH_SHORT).show();
                        } else {
                            //重命名当前的书架
                            Bookshelves.set(spinner_bookshelf_selected, input);
                            //重新显示spinner_bookshelf
                            ArrayAdapter<String> spinner_new_adapter = new ArrayAdapter<String>(MainActivity.this, R.layout.simple_spinner_list_item, Bookshelves);
                            spinner_bookshelf.setAdapter(spinner_new_adapter);
                            //设置当前选中的书架为重命名后的书架
                            spinner_bookshelf.setSelection(spinner_bookshelf_selected);
                            //数据持久化书架数据Bookshelves.dat
                            String file = "Bookshelves.dat";
                            listOperator.save(MainActivity.this, Bookshelves, file);
                        }
                    }
                }).show();
    }

    // 手动输入对话框类ForAddingLabel
    private void showInputDialogForAddingLabel(){
        String title = "添加新标签";
        final EditText editText = new EditText(MainActivity.this);
        editText.setHint("输入新的标签的名称");
        AlertDialog.Builder inputDialog = new AlertDialog.Builder(MainActivity.this);
        inputDialog.setTitle(title).setView(editText);
        inputDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
//                //将spinner_bookshelf的当前选中项设为点击“添加书架”之前选中的书架
//                spinner_bookshelf.setSelection(spinner_bookshelf_selected);
            }
        });
        inputDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String input = editText.getText().toString().trim();

                        //当前用户未输入新标签名时，提示“标签名称不能为空，添加失败”
                        if (input.isEmpty())
                        {
                            Toast.makeText(MainActivity.this, "标签名称不能为空，添加失败", Toast.LENGTH_SHORT).show();
                        }else{
                            String label_add = editText.getText().toString();
                            ArrayList<String> new_label = insertLabel(Labels, label_add);
                            Labels = new_label;
                            //更新侧滑菜单的标签栏
                            updateNav_menu();

                            //数据持久化，包括书架数据Bookshelves.dat和新建书架内的书籍数据的持久化
                            String file = "Labels.dat" ;
                            listOperator.save(MainActivity.this, Labels, file);
                        }
                    }
                }).show();
    }

    private void showInputDialogForRenameLabel(){
        String title = "重命名标签";
        //保存重命名之前的标签
        final String label_previous = Labels.get(label_selected);
        final EditText editText = new EditText(MainActivity.this);
        AlertDialog.Builder inputDialog = new AlertDialog.Builder(MainActivity.this);

        //设置editText显示的内容为当前标签的名字，并将光标移到最后
        editText.setText(Labels.get(label_selected));
        editText.setSelection(Labels.get(label_selected).length());

        inputDialog.setTitle(title).setView(editText);
        inputDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        inputDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String input = editText.getText().toString().trim();
                        if (input.isEmpty()) {
                            Toast.makeText(MainActivity.this, "标签名称不能为空，标签重命名失败", Toast.LENGTH_SHORT).show();
                        } else {
                            //重命名当前的标签
                            Labels.set(label_selected, input);
                            //重新加载侧滑菜单
                            updateNav_menu();

                            //遍历除了“添加书架”以外的书架中的书籍，重命名其标签，数据持久化
                            for (int i = 0; i < Bookshelves.size() - 1; i++){
                                String file = "Bookshelf" + i + ".dat";
                                ArrayList<Bookitem> bookshelf = listOperator.load(MainActivity.this, file);
                                for(int j = 0; j < bookshelf.size() - 1; j++){
                                    if(bookshelf.get(j).getLabels().equals(label_previous))
                                        bookshelf.get(j).setLabels(input);
                                }
                                listOperator.save(MainActivity.this, bookshelf, file);
                            }

                            //数据持久化
                            String file = "Labels.dat";
                            listOperator.save(MainActivity.this, Labels, file);
                        }
                    }
                }).show();
    }

    // 追加新字符串
    //往字符串数组追加新数据
    private static String[] insert(String[] arr, String str) {
        int size = arr.length;  //获取数组长度
        String[] tmp = new String[size + 1];  //新建临时字符串数组，在原来基础上长度加一
        for (int i = 0; i < size; i++){  //先遍历将原来的字符串数组数据添加到临时字符串数组
            tmp[i] = arr[i];
        }
        tmp[size] = str;  //在最后添加上需要追加的数据
        return tmp;  //返回拼接完成的字符串数组
    }
    //在Bookshelves中添加新书架
    private static ArrayList<String> insertBookshelf(ArrayList<String> old, String new_bookshelf){
        ArrayList<String> tem = old;
        tem.set(old.size() - 1, new_bookshelf);
        tem.add("添加书架");
        return tem;
    }

    //删除当前选中的书架
    private ArrayList<String> deleteBookshelf(ArrayList<String> bs){
        for (int i = spinner_bookshelf_selected; i < bs.size() - 1; i++)
            bs.set(i, bs.get(i + 1));
        bs.remove(bs.size() - 1);

        //数据持久化
        String file = "Bookshelves.dat";
        listOperator.save(MainActivity.this, Bookshelves, file);
        //将当前选中的书架之后的书架数据重新排序，以符合Bookshlves中相应的数据
        for (int i = spinner_bookshelf_selected; i < bs.size() - 2; i ++){
            String fileName = "Bookshelf" + (i + 1) + ".dat";
            ArrayList<Bookitem> bookshelf = listOperator.load(MainActivity.this, fileName);
            fileName = "Bookshelf" + i + ".dat";
            listOperator.save(MainActivity.this, bookshelf, fileName);
        }
        //将最后一个书架重置为空
        String fileName = "Bookshelf" + (bs.size() - 2) + ".dat";
        ArrayList<Bookitem> bookshelf = new ArrayList<Bookitem>();
        listOperator.save(MainActivity.this, bookshelf, fileName);

        return bs;
    }

    //添加新标签
    private static ArrayList<String> insertLabel(ArrayList<String> old, String new_Label){
        ArrayList<String> tem = old;
        tem.set(old.size() - 1, new_Label);
        tem.add("添加新标签");
        return tem;
    }

    // 单选对话框类
    int yourChoice;
    private void showSingleChoiceDialog(){
        final booknameComparator bc = new booknameComparator();//书名排序
        final authorComparator ac = new authorComparator();//作者排序
        final publisherComparator prc = new publisherComparator();//出版社排序
        final timeComparator tc = new timeComparator();//时间排序
        final String[] items = { "按书名排序","按作者排序","按出版社排序","按出版时间排序" };
        yourChoice = -1;
        AlertDialog.Builder singleChoiceDialog =
                new AlertDialog.Builder(MainActivity.this);
        singleChoiceDialog.setTitle("选择排序依据");
        // 第二个参数是默认选项，此处设置为0
        singleChoiceDialog.setSingleChoiceItems(items, 0,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        yourChoice = which;
                    }
                });
        singleChoiceDialog.setPositiveButton("排序",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (yourChoice == 0) {
                            Collections.sort(bookitemList, bc);
                            adapter.notifyDataSetChanged();
                        }
                        else if(yourChoice == 1) {
                            Collections.sort(bookitemList, ac);
                            adapter.notifyDataSetChanged();
                        }
                        else if(yourChoice == 2) {
                            Collections.sort(bookitemList, prc);
                            adapter.notifyDataSetChanged();
                        }
                        else if(yourChoice == 3) {
                            Collections.sort(bookitemList, tc);
                            adapter.notifyDataSetChanged();
                        }
                    }
                });
        singleChoiceDialog.show();
    }
}


