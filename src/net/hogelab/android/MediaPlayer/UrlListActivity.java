package net.hogelab.android.MediaPlayer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class UrlListActivity extends ListActivity {

	@SuppressWarnings("unused")
	private static final String	TAG = UrlListActivity.class.getSimpleName();

	private List<String>		mTitles = null;
	private List<String>		mUrls = null;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_urllist);

		loadUrlList();
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mTitles);
		setListAdapter(adapter);
	}


	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		sendResult(position);
	}


	private void loadUrlList() {
		List<String> titles = new ArrayList<String>();
		List<String> urls = new ArrayList<String>();

		try {
			AssetManager assetManager = getResources().getAssets();
			InputStream is = assetManager.open("urls.txt");
			InputStreamReader reader = new InputStreamReader(is);
			BufferedReader buffer = new BufferedReader(reader);

			String title = null;
			String url = null;

			String line = buffer.readLine();
			while (line != null) {
				if (line.length() > 0) {
					if (title != null) {
						url = line;
					} else {
						title = line;
					}
				} else {
					if (title != null && url != null) {
						titles.add(title);
						urls.add(url);

						title = null;
						url = null;
					}
				}

				line = buffer.readLine();
			}

			if (title != null && url != null) {
				titles.add(title);
				urls.add(url);
			}

			buffer.close();
			reader.close();
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		mTitles = titles;
		mUrls = urls;
	}


	public void sendResult(int location) {
		String url = mUrls.get(location);

		Intent data = new Intent();
		Bundle bundle = new Bundle();
		bundle.putString("mediaUrl", url);
		data.putExtras(bundle);

		setResult(RESULT_OK, data);
		//setResult(RESULT_CANCELED, data);
		finish();
	}
}
