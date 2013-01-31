package net.hogelab.android.MediaPlayer;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.text.SpannableStringBuilder;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;


public class MainActivity extends Activity {

	private static final int	URLLIST_REQUEST_CODE	= 1;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			if (requestCode == URLLIST_REQUEST_CODE) {
				Bundle bundle = data.getExtras();
				EditText editText = (EditText)findViewById(R.id.editMediaUrl);
				editText.setText(bundle.getString("mediaUrl"));
			}
		}
	}


	public void onUrlList(View view) {
		Intent intent = new Intent(this, UrlListActivity.class);
		startActivityForResult(intent, URLLIST_REQUEST_CODE);
	}


	public void onMediaPlay(View view) {
		EditText editText = (EditText)findViewById(R.id.editMediaUrl);
		SpannableStringBuilder builder = (SpannableStringBuilder)editText.getText();
		String url = builder.toString();

		Intent intent = new Intent(this, MediaPlayerActivity.class);
		intent.putExtra("mediaUrl", url);
		startActivity(intent);
	}
}
