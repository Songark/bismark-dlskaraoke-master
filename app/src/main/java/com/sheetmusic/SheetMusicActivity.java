/*
 * Copyright (c) 2011-2012 Madhav Vaidyanathan
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License version 2.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 */

package com.sheetmusic;

import android.app.Activity;
import android.content.SharedPreferences;
import android.net.Uri;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.zip.CRC32;

/** @class SheetMusicActivity
 *
 * The SheetMusicActivity is the main activity. The main components are:
 * - MidiPlayer : The buttons and speed bar at the top.
 * - SheetMusic : For highlighting the sheet music notes during playback.
 *
 */
public class SheetMusicActivity {

    public static final String MidiTitleID = "MidiTitleID";
    public static final int settingsRequestCode = 1;
    
    private SheetMusicPlayer player;   /* The play/stop/rewind toolbar */
    private SheetMusic sheet;    /* The sheet music */
    private LinearLayout layout; /* THe layout */
    private MidiFile midifile;   /* The midi file to play */
    private MidiOptions options; /* The options for sheet music and snoound */
    private long midiCRC;      /* CRC of the midi bytes */

    private Activity activity;

    public SheetMusicActivity(Activity activity0){
        activity=activity0;
    }
     /** Create this SheetMusicActivity.  
      * The Intent should have two parameters:
      * - data: The uri of the midi file to open.
      * - MidiTitleID: The title of the song (String)
      */

    public void onCreate(String tempFile) {
        ClefSymbol.LoadImages(activity);
        TimeSigSymbol.LoadImages(activity);

        Uri uri = Uri.parse("file://" + tempFile);
        String title = uri.getLastPathSegment();

        FileUri file = new FileUri(uri, title);

        byte[] data;
        // Parse the MidiFile from the raw bytes
       try {
            data = file.getData(activity);
            midifile = new MidiFile(data, title);
            if (data == null || data.length <= 6 || !MidiFile.hasMidiHeader(data)) {
                Toast.makeText(activity.getBaseContext(),"Error: Unable to open song: " + file.toString(), Toast.LENGTH_LONG).show();
                return;
            }
        }
        catch (MidiFileException e) {
            Toast.makeText(activity.getBaseContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            activity.finish();
            return;
        }

        // Initialize the settings (MidiOptions).
        // If previous settings have been saved, used those
        options = new MidiOptions(midifile);
        CRC32 crc = new CRC32();
        crc.update(data); 
        midiCRC = crc.getValue();
        SharedPreferences settings = activity.getPreferences(0);
        options.scrollVert = settings.getBoolean("scrollVert", false);
        options.shade1Color = settings.getInt("shade1Color", options.shade1Color);
        options.shade2Color = settings.getInt("shade2Color", options.shade2Color);
        options.showPiano = settings.getBoolean("showPiano", true);
        String json = settings.getString("" + midiCRC, null);
        MidiOptions savedOptions = MidiOptions.fromJson(json);
        if (savedOptions != null) {
            options.merge(savedOptions);
        }
        createView();
        createSheetMusic(options);
    }
    
    /* Create the MidiPlayer and Piano views */
    void createView() {
        layout = new LinearLayout(activity);
        layout.setOrientation(LinearLayout.VERTICAL);
        player = new SheetMusicPlayer(activity);
        layout.addView(player);
        activity.setContentView(layout);  //is it needed?
        layout.requestLayout();
    }

    /** Create the SheetMusic view with the given options */
    private void 
    createSheetMusic(MidiOptions options) {
        if (sheet != null) {
            layout.removeView(sheet);
        }
        sheet = new SheetMusic(activity);
        sheet.init(midifile, options);
        sheet.setPlayer(player);
        layout.addView(sheet);
        player.SetMidiFile(midifile, options, sheet);
        layout.requestLayout();
        sheet.callOnDraw();
    }

    /** This is the callback when the SettingsActivity is finished.
     *  Get the modified MidiOptions (passed as a parameter in the Intent).
     *  Save the MidiOptions.  The key is the CRC checksum of the midi data,
     *  and the value is a JSON dump of the MidiOptions.
     *  Finally, re-create the SheetMusic View with the new options.
     */
   /* @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent intent) {
        if (requestCode != settingsRequestCode) {
            return;
        }
            // Check whether the default instruments have changed.
        for (int i = 0; i < options.instruments.length; i++) {
            if (options.instruments[i] !=  
                midifile.getTracks().get(i).getInstrument()) {
                options.useDefaultInstruments = false;
            }
        }
        // Save the options. 
        SharedPreferences.Editor editor = getPreferences(0).edit();
        editor.putBoolean("scrollVert", options.scrollVert);
        editor.putInt("shade1Color", options.shade1Color);
        editor.putInt("shade2Color", options.shade2Color);
        editor.putBoolean("showPiano", options.showPiano);
        String json = options.toJson();
        if (json != null) {
            editor.putString("" + midiCRC, json);
        }
        editor.commit();

        // Recreate the sheet music with the new options
        createSheetMusic(options);
    }*/

    /** When this activity resumes, redraw all the views */
   /* @Override
    protected void onResume() {
        super.onResume();
        layout.requestLayout();
        player.invalidate();
        if (sheet != null) {
            sheet.invalidate();
        }
        layout.requestLayout();
    }*/

    /** When this activity pauses, stop the music */
/*    @Override
    protected void onPause() {
        if (player != null) {
            player.Pause();
        }
        super.onPause();
    } */
}

