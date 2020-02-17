package com.crysis.bookshelf;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.klinker.android.sliding.SlidingActivity;


public class BookDetail extends SlidingActivity{
    private Bookitem mBook;
    private int Bookitem_Position;
    @Override
    public void init(Bundle savedInstanceState) {
        //<init> function instead of onCreate
        Intent intent = getIntent();
        mBook = (Bookitem) intent.getSerializableExtra("bookitem");
        Bookitem_Position = intent.getIntExtra("Position", -1);
        setTitle(mBook.getTitle());

        setPrimaryColors(
                getResources().getColor(R.color.colorPrimary),
                getResources().getColor(R.color.colorPrimaryDark)
        );
        setContent(R.layout.activity_book_detail);

        setHeaderContent(R.layout.activity_book_detail_header);
        setFab(
                getResources().getColor(R.color.colorAccent),
                R.drawable.ic_edit,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent_to_edit = new Intent(BookDetail.this,Editbook.class);
                        //Bundle bundle = new Bundle();
                        intent_to_edit.putExtra("bookitem_from_detail", mBook);
                        intent_to_edit.putExtra("edit_position", Bookitem_Position);
                        intent_to_edit.putExtra("Manual", "222");
                        startActivityForResult(intent_to_edit, MainActivity.REQUEST_CODE_DETAIL);
                    }
                }
        );

        setHeader();
        setBookDetail();
    }

    // 重载OnActivityResult，处理返回参数Intent data
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == MainActivity.RESULT_ADD_CHANGE && requestCode == MainActivity.REQUEST_CODE_DETAIL)
        {
            Bookitem bookitem_for_change;
            int change_position;
            Intent intent = new Intent(BookDetail.this, MainActivity.class);
            bookitem_for_change = (Bookitem) data.getSerializableExtra("change_bookitem");
            change_position = data.getIntExtra("change_position", -1);
            intent.putExtra("change_bookitem", bookitem_for_change);
            intent.putExtra("change_position", change_position);
            setResult(MainActivity.RESULT_ADD_CHANGE, intent);
            finish();
        }
    }


    private void setHeader() {
        //获取bookitem图片的byte数据并转化成Bitmap
        Bitmap2Byte bitmap2Byte = new Bitmap2Byte();
        //绑定组件
        ImageView bookCover = (ImageView) findViewById(R.id.book_detail_cover);
        byte[] temp;
        if(mBook.getBitmap_byte() != null) {
            temp = mBook.getBitmap_byte();
            Bitmap bitmap = bitmap2Byte.byte2Bitmap(temp);
            bookCover.setImageBitmap(bitmap);
        }
        else if(mBook.getImageId() != 0){
            bookCover.setImageResource(mBook.getImageId());
        }
    }


    private void setBookDetail() {
        TextView book_author = (TextView)findViewById(R.id.book_info_author_content);
        book_author.setText(mBook.getAuthor());

        TextView book_translator = (TextView)findViewById(R.id.book_info_translator_content);
        book_translator.setText(mBook.getTranslator());

        TextView book_publisher = (TextView)findViewById(R.id.book_info_publisher_content);
        book_publisher.setText(mBook.getPublisher());

        TextView book_pubdate = (TextView)findViewById(R.id.book_info_pubtime_content);
        book_pubdate.setText(mBook.getPubDate());

        TextView book_isbn = (TextView)findViewById(R.id.book_info_isbn_content);
        book_isbn.setText(mBook.getISBN());

        TextView book_reading_status = (TextView)findViewById(R.id.book_detail_reading_status_content);
//        CSVOP op = new CSVOP();
//        Settings settings = op.importSettings();
//        String[] status = settings.getReadState();
//        mBook.setReadState(0);
        book_reading_status.setText(mBook.getReadingStatus_Text());

        TextView book_bookshelf = (TextView)findViewById(R.id.book_detail_bookshelf_content);
        book_bookshelf.setText(mBook.getBookShelfName());

        TextView book_notes = (TextView)findViewById(R.id.book_detail_notes_content);
        book_notes.setText(mBook.getNotes());

        TextView book_labels = (TextView)findViewById(R.id.book_detail_labels_content);
        book_labels.setText(mBook.getLabels());

        TextView book_website = (TextView)findViewById(R.id.book_detail_website_content);
        book_website.setText(mBook.getWebsite());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_bookdetail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.book_detail_menu_delete:
                Intent intent = new Intent(BookDetail.this, MainActivity.class);
                intent.putExtra("DeleteBookitem", mBook.getISBN());
                intent.putExtra("DeletePosition", Bookitem_Position);
                setResult(MainActivity.RESULT_DETAIL_DELETE, intent);
                finish();
        }
        return super.onOptionsItemSelected(item);
    }

}
