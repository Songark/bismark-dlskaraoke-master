package com.karaoke.activity;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.content.Context;
import android.view.View;
import android.widget.TextView;
import android.view.ViewGroup;
import android.view.LayoutInflater;

import com.karaoke.R;
import com.karaoke.data.SongInfo;
import com.sheetmusic.MidiPlayer;
import com.tbtc.jftv.common.Global;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class MidiRecommendActivity extends BaseActivity {

    ListView lvMidi;
    RecommendMidiDataAdapter midiRecommendAdapter;
    ArrayList<SongInfo> arrayMidi = new ArrayList<SongInfo>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_midi_recommend);

        MidiPlayer.getPlayer().clearSelectedSongs();

        String path = Global.getRootSongsPath();
        File directory = new File(path);
        File[] files = directory.listFiles();
        for (int i = 0; i < files.length; i++)
        {
            String filePath = files[i].getAbsolutePath();
            String extension = filePath.substring(filePath.lastIndexOf("."));
            if (extension.toLowerCase().compareTo(".lyr") == 0) {
                Global.Debug("Loading Magicsing file: " + filePath);
                arrayMidi.add(new SongInfo(SongInfo.TYPE_MAGICSING, filePath));
            }
            else if (extension.toLowerCase().compareTo(".sok") == 0) {
                Global.Debug("Loading Kumyong file: " + filePath);
                arrayMidi.add(new SongInfo(SongInfo.TYPE_KUMYONG, filePath));
            }
        }

        lvMidi = (ListView) findViewById(R.id.lvMidi);
        midiRecommendAdapter = new RecommendMidiDataAdapter(MidiRecommendActivity.this);
        lvMidi.setAdapter(midiRecommendAdapter);
        lvMidi.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Notify MainActivity to grab new file
                SongInfo songInfo = arrayMidi.get(position);

                // Update UI
                // Toast.makeText(MidiRecommendActivity.this, "OnItemClicked", Toast.LENGTH_SHORT).show();
                int nIndex = MidiPlayer.getPlayer().getSelectedSongIndex(songInfo);
                if (nIndex == -1) {
                    MidiPlayer.getPlayer().addSelectedSong(songInfo);
                }
                else {
                    MidiPlayer.getPlayer().removeSelectedSong(nIndex);
                }
                midiRecommendAdapter.notifyDataSetChanged();
            }
        });
    }

    private class RecommendMidiDataAdapter extends BaseAdapter {

        Context mContext;
        int colorBackNormal;
        int colorBackHighlight;
        int colorTextNormal;
        int colorTextHighlight;

        public RecommendMidiDataAdapter(Context context) {
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
            ImageView tvReserveMark = (ImageView) convertView.findViewById(R.id.tvReservMark);

            tvNo.setText(String.valueOf(currentSongInfo.songNumber));
            tvSongName.setText(currentSongInfo.songTitle);
            tvArtistName.setText(currentSongInfo.songArtist);
            tvReserveMark.setVisibility(View.INVISIBLE);

            convertView.setTag(position);

            // Highlight selected item and Dehighlight old item
            if (MidiPlayer.getPlayer().getSelectedSongIndex(currentSongInfo) != -1) {
                convertView.setBackgroundColor(colorBackHighlight);
                tvSongName.setTextColor(colorTextHighlight);
                tvReserveMark.setVisibility(View.VISIBLE);
            } else {
                convertView.setBackgroundColor(colorBackNormal);
                tvSongName.setTextColor(colorTextNormal);
            }

            return convertView;
        }
    }
}
