package com.karaoke.activity;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.karaoke.R;
import com.karaoke.data.SongInfo;

import java.util.ArrayList;

public class MidiFavoriteActivity extends BaseActivity {

    ListView lvMidi;
    FavoriteMidiDataAdapter midiRecommendAdapter;
    ArrayList<SongInfo> arrayMidi = new ArrayList<SongInfo>();
    int selectedSongItem;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_midi_recommend);

        selectedSongItem = -1;

        lvMidi = (ListView) findViewById(R.id.lvMidi);
        midiRecommendAdapter = new FavoriteMidiDataAdapter(MidiFavoriteActivity.this);
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

    private class FavoriteMidiDataAdapter extends BaseAdapter {

        Context mContext;
        int colorBackNormal;
        int colorBackHighlight;
        int colorTextNormal;
        int colorTextHighlight;

        public FavoriteMidiDataAdapter(Context context) {
            mContext = context;

            Resources resource = context.getResources();
            colorBackNormal = resource.getColor(R.color.white);
            colorBackHighlight = resource.getColor(R.color.list_item_back_highlight);
            colorTextNormal = resource.getColor(R.color.black);
            colorTextHighlight = resource.getColor(R.color.font_color_title);
        }

        @Override
        public int getCount() {
            return arrayMidi.size();
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

            SongInfo currentSongInfo = arrayMidi.get(position);
            TextView tvNo = (TextView) convertView.findViewById(R.id.tvNo);
            TextView tvSongName = (TextView) convertView.findViewById(R.id.tvMidiName);
            TextView tvArtistName = (TextView) convertView.findViewById(R.id.tvArtistName);

            tvNo.setText(String.valueOf(currentSongInfo.songNumber));
            tvSongName.setText(currentSongInfo.songTitle);
            tvArtistName.setText(currentSongInfo.songArtist);

            convertView.setTag(position);

            // Highlight selected item and Dehighlight old item
            if (selectedSongItem == position) {
                convertView.setBackgroundColor(colorBackHighlight);
                tvSongName.setTextColor(colorTextHighlight);
            } else {
                convertView.setBackgroundColor(colorBackNormal);
                tvSongName.setTextColor(colorTextNormal);
            }

            return convertView;
        }
    }
}
