package com.karaoke.activity;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.karaoke.R;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class MidiSearchActivity extends BaseActivity {

    private EditText keywordEdt;
    private View search_clear;
    private String currentKeyword;
    private UpdateHandler updateHandler;

    ListView lvMidi;
    MidiDataAdapter midiRecommendAdapter;

    int selectedSongItem;

    private static class UpdateHandler extends Handler {
        public static final int UPDATE = 0;

        private WeakReference<MidiSearchActivity> mWeakActivity;

        public UpdateHandler(MidiSearchActivity activity) {
            mWeakActivity = new WeakReference<MidiSearchActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            MidiSearchActivity activity = mWeakActivity.get();
            if (activity == null) {
                return;
            }

            if (msg.what == UPDATE) {
                activity.searchSong();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_midi_search);

        // Search Elements
        keywordEdt = (EditText) findViewById(R.id.search_edit);
        keywordEdt.setHint(R.string.search_hint);
        hideKeyboard(keywordEdt);

        search_clear = findViewById(R.id.search_clear);
        search_clear.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                keywordEdt.setText("");
            }
        });

        updateHandler = new UpdateHandler(MidiSearchActivity.this);
        keywordEdt.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                updateHandler.removeMessages(0);
                updateHandler.sendEmptyMessageDelayed(0, 2000);
                currentKeyword = keywordEdt.getText().toString();
                if (TextUtils.isEmpty(currentKeyword)) {
                    search_clear.setVisibility(View.GONE);
                } else {
                    search_clear.setVisibility(View.VISIBLE);
                }
            }
        });

        selectedSongItem = -1;

        lvMidi = (ListView) findViewById(R.id.lvMidi);
        midiRecommendAdapter = new MidiDataAdapter(MidiSearchActivity.this);
        lvMidi.setAdapter(midiRecommendAdapter);
        lvMidi.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Notify MainActivity to grab new file

                // Update UI
                // Toast.makeText(MidiRecommendActivity.this, "OnItemClicked", Toast.LENGTH_SHORT).show();
                selectedSongItem = position;
                midiRecommendAdapter.notifyDataSetChanged();
            }
        });
    }

    // copy array from other

    // Filter song with keyword input
    private void searchSong() {
        String keyword = keywordEdt.getText().toString().trim();
        if (TextUtils.isEmpty(keyword)) {
        } else {
            keyword = keyword.toLowerCase();

        }

        midiRecommendAdapter.notifyDataSetChanged();
    }

    private class MidiDataAdapter extends BaseAdapter {

        Context mContext;
        int colorBackNormal;
        int colorBackHighlight;
        int colorTextNormal;
        int colorTextHighlight;

        public MidiDataAdapter(Context context) {
            mContext = context;

            Resources resource = context.getResources();
            colorBackNormal = resource.getColor(R.color.white);
            colorBackHighlight = resource.getColor(R.color.list_item_back_highlight);
            colorTextNormal = resource.getColor(R.color.black);
            colorTextHighlight = resource.getColor(R.color.font_color_title);
        }

        @Override
        public int getCount() {
            return 0;
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.item_midi,
                        null);
            }


            convertView.setTag(position);


            return convertView;
        }
    }
}
