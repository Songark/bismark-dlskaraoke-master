package com.karaoke.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.MainThread;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;


import com.karaoke.data.MediaFile;
import com.karaoke.data.SongInfo;
import com.sheetmusic.ClefSymbol;
import com.sheetmusic.FileUri;
import com.sheetmusic.MidiFile;
import com.sheetmusic.MidiFileException;
import com.sheetmusic.MidiOptions;
import com.sheetmusic.SheetMusic;
import com.sheetmusic.SheetMusicPlayer;
import com.sheetmusic.TimeSigSymbol;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import com.karaoke.EnvironmentApp;
import com.karaoke.R;
import com.karaoke.utils.FontChangeCrawler;
import com.karaoke.utils.RandomGen;
import com.karaoke.view.CustomHorizontalScrollView;
import com.tbtc.jftv.common.Global;
import com.tbtc.jftv.manage.GreetManager;
import com.tbtc.jftv.manage.MarkManager;
import com.tbtc.jftv.manage.SetupManager;
import com.sheetmusic.LyricsInfo;
import com.sheetmusic.MidiPlayer;
import com.tbtc.jftv.text.KTextLayout;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Random;
import java.util.zip.CRC32;

import jp.bismark.bssynth.sample.MainActivity;

import static com.sheetmusic.MidiPlayer.PLAYER_PLAYING;

public class MidiPlayerActivity extends BaseActivity implements MidiPlayer.OnMidiPlayListener {

    // BGV Settings
    static WeakReference<MidiPlayerActivity> mWeakPlayer;

    Handler mBGVUpdateHandler;
    ArrayList<String> bgvFiles = new ArrayList<String>();
    int currentBgvType;     // 0-jpg, 1-mp4
    int currentBgvId;       // size of bgvFiles
    int bgvInterval = 10;   // BGV Update Intervals(seconds)
    ImageLoader imageLoader;
    DisplayImageOptions imageOptions;
    ImageView bgvIv;
    Random fileSeedRandom;

    // UI Update Handler
    private Handler uiUpdateHandler;

    // Karaoke Mark
    private ImageView imgMark1;
    private ImageView imgMark2;
    private ImageView imgMarkText;


    // File to play
    ArrayList<SongInfo> selectedSongArray;
    private SongInfo curSongInfo;
    private MediaFile curMediaFile;

    private int key;
    private boolean isDestroyed;

    private long startedTime;
    private static int mTempoStep = 100; // Used for Midi Player
    private final static int TEMPO_STEP = 5;
    private final static int TEMPO_MIN = 100 - 7 * 5;
    private final static int TEMPO_MAX = 100 + 7 * 5;
    private final static float VOLUME_STEP = 0.1f;

    // ------------------- For playing midi with lyrics txt
    // ------------------------
    private int mCurColorIdx;
    private RelativeLayout frmBack;
    ProgressDialog mProgressDlg;

    // Midi File Information
    View lnSongInfoPanel;
    private TextView mTxtLblComposer, mTxtLblWriter, mTxtLblSinger, mTxtDotComposer, mTxtDotWriter,
            mTxtDotSinger;

    private TextView mTxtSubTitle, mTxtTitle, mTxtComposer, mTxtWriter, mTxtSinger;
    private View mLnSongInfo, mLnLyrics, mLnMark;
    private ImageView[] mTickImages = null;
    private LinearLayout mLnLyricView;
    private KTextLayout kTxtLayout;
    TextView mCurSecondsLabel, mMaxSecondsLabel;
    GreetManager mGreetManager;
    LyricsInfo mLyricsInfo;
    SeekBar midiPlayBar;
    long midiPlayBarTicks = 0;
    boolean songReplayFlag = false;
    boolean songLoadingFlag = false;

    // -------------------------------------------------------------------------

    private String Tag = "KaraokeMediaPlayer";

    //private SheetMusicActivity sheetMusicActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_midi_player);

        Intent intent = getIntent();
        selectedSongArray = MidiPlayer.getPlayer().getSelectedSongs();
        MidiPlayer.getPlayer().selectedSongIndex = 0;
        curSongInfo = selectedSongArray.get(0);
        Global.Debug("[MidiPlayerActivity::onCreate] curSongIndex-" + MidiPlayer.getPlayer().selectedSongIndex + ", curSongInfo-" + curSongInfo.songTitle + ", " + curSongInfo.songLyricname);

        // load ImageLoader for BGV
        imageLoader = ImageLoader.getInstance();
        imageLoader.init(ImageLoaderConfiguration.createDefault(getBaseContext()));
        imageOptions = new DisplayImageOptions.Builder().imageScaleType(ImageScaleType.EXACTLY)
                .cacheInMemory(true).cacheOnDisk(true).considerExifParams(true)
                .bitmapConfig(Bitmap.Config.RGB_565).build();

        mWeakPlayer = new WeakReference<MidiPlayerActivity>(this);
        frmBack = findViewById(R.id.frmBack);
        imgMark1 = (ImageView) findViewById(R.id.imgMark1);
        imgMark2 = (ImageView) findViewById(R.id.imgMark2);
        imgMarkText = (ImageView) findViewById(R.id.imgMarkText);

        uiUpdateHandler = new Handler();

        mGreetManager = new GreetManager((Activity)cntxt, this);
        mGreetManager.init();
        mTxtSubTitle = (TextView) findViewById(R.id.txtSubTitle);
        mTxtTitle = (TextView) findViewById(R.id.txtSongTitle);
        mTxtComposer = (TextView) findViewById(R.id.txtComposer);
        mTxtWriter = (TextView) findViewById(R.id.txtWriter);
        mTxtSinger = (TextView) findViewById(R.id.txtInfoSinger);
        mTxtLblComposer = (TextView) findViewById(R.id.txtLblComposer);
        mTxtLblWriter = (TextView) findViewById(R.id.txtLblWriter);
        mTxtLblSinger = (TextView) findViewById(R.id.txtLblSinger);
        mTxtDotComposer = (TextView) findViewById(R.id.txtDotComposer);
        mTxtDotWriter = (TextView) findViewById(R.id.txtDotWriter);
        mTxtDotSinger = (TextView) findViewById(R.id.txtDotSinger);

        lnSongInfoPanel = findViewById(R.id.lnSongInfoPanel);
        mLnSongInfo = findViewById(R.id.lnSongInfo);
        mLnLyrics = findViewById(R.id.lnLyrics);
        mLnMark = findViewById(R.id.lnMark);
        mLnLyricView = (LinearLayout) findViewById(R.id.lnLyricView);
        mTickImages = new ImageView[4];
        mTickImages[0] = (ImageView) findViewById(R.id.tick0);
        mTickImages[1] = (ImageView) findViewById(R.id.tick1);
        mTickImages[2] = (ImageView) findViewById(R.id.tick2);
        mTickImages[3] = (ImageView) findViewById(R.id.tick3);

        kTxtLayout = new KTextLayout(cntxt);
        kTxtLayout.setTypeface(getSimheiFont());
        kTxtLayout.setFontsize(getResources().getDimensionPixelSize(R.dimen.fontsize_lyric));
        MidiPlayer.getPlayer().setKTextLayout(kTxtLayout);
        MidiPlayer.getPlayer().setParent(this);
        mLnLyricView.addView(kTxtLayout);

        findViewById(R.id.btnMidiSheet).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                onBackPressed();
            }
        });

        // Pause
        findViewById(R.id.ivPause).setOnClickListener(new View.OnClickListener()  {
            @Override
            public void onClick(View v) {
                if (MidiPlayer.getPlayer().IsPlaying()) {
                    MidiPlayer.getPlayer().pause();
                }
            }
        });

        // Play
        findViewById(R.id.ivPlay).setOnClickListener(new View.OnClickListener()  {
            @Override
            public void onClick(View v) {
                if (MidiPlayer.getPlayer().IsPlaying() == false) {
                    MidiPlayer.getPlayer().play(curSongInfo.getLanguage());
                }
            }
        });

        // Stop
        findViewById(R.id.ivStop).setOnClickListener(new View.OnClickListener()  {
            @Override
            public void onClick(View v) {
                if (MidiPlayer.getPlayer().IsPlaying()) {
                    changePlay(PLAY_NONE);
                    MidiPlayer.getPlayer().stop();
                    MidiPlayer.getPlayer().seekByTick(0);
                }
            }
        });

        // Prev
        findViewById(R.id.ivPrev).setOnClickListener(new View.OnClickListener()  {
            @Override
            public void onClick(View v) {
                if (playPrevSong()) {
                    songReplayFlag = false;
                    changePlay(PLAY_NONE);
                }
            }
        });

        // Next
        findViewById(R.id.ivNext).setOnClickListener(new View.OnClickListener()  {
            @Override
            public void onClick(View v) {
                if (playNextSong()) {
                    songReplayFlag = false;
                    changePlay(PLAY_NONE);
                }
            }
        });

        // Replay
        final TextView txtReplayMarker = findViewById(R.id.tvReplayMark);
        findViewById(R.id.ivReplay).setOnClickListener(new View.OnClickListener()  {
            @Override
            public void onClick(View v) {
                changePlay(PLAY_NONE);
                MidiPlayer.getPlayer().stop();
                MidiPlayer.getPlayer().seekByTick(0);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                MidiPlayer.getPlayer().play(curSongInfo.getLanguage());
                // MidiPlayer.getPlayer().seekByTicks(1393 * 5);
            }
        });

        // Key Control Action
        findViewById(R.id.btnkeyplus).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int key = MidiPlayer.getPlayer().keyPlus();
                Toast.makeText(MidiPlayerActivity.this, "Key : " + Integer.toString(key), Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.btnkeyminus).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int key = MidiPlayer.getPlayer().keyMinus();
                Toast.makeText(MidiPlayerActivity.this, "Key : " + Integer.toString(key), Toast.LENGTH_SHORT).show();
            }
        });

        // Speed Control Action
        findViewById(R.id.btnspeedplus).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int speed = MidiPlayer.getPlayer().speedChange(true);
                Toast.makeText(MidiPlayerActivity.this, "Speed: " + Integer.toString(speed), Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.btnspeedminus).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int speed = MidiPlayer.getPlayer().speedChange(false);
                Toast.makeText(MidiPlayerActivity.this, "Speed: " + Integer.toString(speed), Toast.LENGTH_SHORT).show();
            }
        });

        // SeekBar Action
        mCurSecondsLabel = (TextView)findViewById(R.id.curticklabel);
        mMaxSecondsLabel = (TextView)findViewById(R.id.maxticklabel);

        midiPlayBar = (SeekBar)findViewById(R.id.seekBar);
        midiPlayBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressChangedValue = 0;

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressChangedValue = progress;
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                changePlay(PLAY_NONE);
                MidiPlayer.getPlayer().seekByTicks(progressChangedValue);
            }
        });

        // -------------------------------------------------------------

        // Change All fonts
        FontChangeCrawler fontChanger = new FontChangeCrawler(getLatoRegularFont());
        fontChanger.replaceFonts((ViewGroup)this.findViewById(R.id.mainView));

        // Start BGV Task
        startBGVTask();

        try {
            playSong();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void playSong() throws InterruptedException {
        // Prepare Midi File
        Global.Debug("[MidiPlayerActivity::playSong] begin");
        String selectedMidiFilePath = Global.getRootSongsPath() + curSongInfo.songMidiname;
        Uri uri = Uri.parse("file://" + selectedMidiFilePath);
        String title = uri.getLastPathSegment();
        FileUri file = new FileUri(uri, title);

        Global.Debug("[MidiPlayerActivity::playSong] selectedFile-" + selectedMidiFilePath);
        byte[] data;
        // Parse the MidiFile from the raw bytes
        data = file.getData(MidiPlayerActivity.this);
        int iRet = MidiPlayer.getPlayer().loadMidiFile(selectedMidiFilePath, data, curSongInfo);
        if (iRet > 0) {
            if (data == null || data.length <= 6 || !MidiFile.hasMidiHeader(data)) {
                Toast.makeText(MidiPlayerActivity.this.getBaseContext(),"Error: Unable to open song: " + file.toString(),
                        Toast.LENGTH_LONG).show();
                finish();
                return;
            }
        }
        else {
            finish();
            return;
        }

        // Prepare to play midi
        Global.Debug("[MidiPlayerActivity::playSong] reset Key & Speed");
        MidiPlayer.getPlayer().keyReset();
        MidiPlayer.getPlayer().speedReset();
        if (MidiPlayer.getPlayer().IsPlaying()) {
            MidiPlayer.getPlayer().stop();
            MidiPlayer.getPlayer().seekByTick(0);
        }

        Global.Debug("[MidiPlayerActivity::playSong] set curMediaFile");
        if (curMediaFile == null)
            curMediaFile = new MediaFile();

        curMediaFile.setFilePath(selectedMidiFilePath, curSongInfo);
        curMediaFile.setTitle(curSongInfo.songTitle);
        curMediaFile.setArtist(curSongInfo.songArtist);

        // Start play midi
        Global.Debug("[MidiPlayerActivity::playSong] playMedia");
        playMedia();

        Global.Debug("[MidiPlayerActivity::playSong] end");
    }

    public boolean playReplaySong()
    {
        if (MidiPlayer.getPlayer().selectedSongIndex < selectedSongArray.size()) {
            curSongInfo = selectedSongArray.get(MidiPlayer.getPlayer().selectedSongIndex);
            Global.Debug("[MidiPlayerActivity::playReplaySong] curSongInfo-" + curSongInfo.songTitle + ", " + curSongInfo.songLyricname + ", " + curSongInfo.songMidiname);
            try {
                MidiPlayer.getPlayer().stop();
                Thread.sleep(100);
                playSong();
                return true;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public boolean playNextSong()
    {
        if (songLoadingFlag) {
            Global.Debug("[MidiPlayerActivity::playNextSong] loading another song ...");
            return false;
        }

        Global.Debug("[MidiPlayerActivity::playNextSong] curSongIndex-" + (MidiPlayer.getPlayer().selectedSongIndex + 1) + ", count-" + selectedSongArray.size());
        if (MidiPlayer.getPlayer().selectedSongIndex < selectedSongArray.size() - 1) {
            MidiPlayer.getPlayer().selectedSongIndex++;
            curSongInfo = selectedSongArray.get(MidiPlayer.getPlayer().selectedSongIndex);
            Global.Debug("[MidiPlayerActivity::playNextSong] curSongInfo-" + curSongInfo.songTitle + ", " + curSongInfo.songLyricname + ", " + curSongInfo.songMidiname);
            try {
                MidiPlayer.getPlayer().stop();
                playSong();
                return true;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public boolean playPrevSong() {
        if (songLoadingFlag) {
            Global.Debug("[MidiPlayerActivity::playPrevSong] loading another song ...");
            return false;
        }

        Global.Debug("[MidiPlayerActivity::playPrevSong] curSongIndex-" + (MidiPlayer.getPlayer().selectedSongIndex + 1) + ", count-" + selectedSongArray.size());
        if (MidiPlayer.getPlayer().selectedSongIndex > 0) {
            MidiPlayer.getPlayer().selectedSongIndex--;
            curSongInfo = selectedSongArray.get(MidiPlayer.getPlayer().selectedSongIndex);
            Global.Debug("[MidiPlayerActivity::playPrevSong] curSongInfo-" + curSongInfo.songTitle + ", " + curSongInfo.songLyricname + ", " + curSongInfo.songMidiname);
            try {
                MidiPlayer.getPlayer().stop();
                playSong();
                return true;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    // ---------------------------- Midi Sheet Controll ----------------------
    // MidiSheet Panel
    LinearLayout viewSheetMusic;
    ImageView btnMidiSheet;
    Animation pushTopIn, pushTopOut;

    private SheetMusicPlayer sheetMusicPlayer;    /* The play/stop/rewind toolbar */
    private SheetMusic sheet;           /* The sheet music */
    private MidiOptions midiOptions;    /* The options for sheet music and snoound */
    private long midiCRC;               /* CRC of the midi bytes */

    Handler mMidisheetTrigger;
    long lStartTime;
    Runnable mMidisheetTriggerRunnable = new Runnable() {

        @Override
        public void run() {

        }
    };

    public void startMidiSheetController() {
        // Init Midisheet Panel
        viewSheetMusic = (LinearLayout) findViewById(R.id.viewSheetMusic);
        btnMidiSheet = (ImageView) findViewById(R.id.btnMidiSheet);
        pushTopIn = AnimationUtils.loadAnimation(cntxt, R.anim.push_top_in);
        pushTopOut = AnimationUtils.loadAnimation(cntxt, R.anim.push_top_out);

        pushTopIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                viewSheetMusic.setVisibility(View.VISIBLE);
            }
            @Override
            public void onAnimationEnd(Animation animation) {

            }
            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        pushTopOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }
            @Override
            public void onAnimationEnd(Animation animation) {
                viewSheetMusic.setVisibility(View.GONE);
            }
            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        lStartTime = System.currentTimeMillis();
        //mMidisheetTrigger = new Handler();
        //mMidisheetTrigger.post(mMidisheetTriggerRunnable);

        // Create Musicsheet controller, not used now
        sheetMusicPlayer = new SheetMusicPlayer(MidiPlayerActivity.this);
        viewSheetMusic.addView(sheetMusicPlayer);
        viewSheetMusic.requestLayout();

        // Create Real Musicsheet
        if (sheet != null) {
            viewSheetMusic.removeView(sheet);
        }
        sheet = new SheetMusic(MidiPlayerActivity.this);
        sheet.init(MidiPlayer.getPlayer().m_midifile, midiOptions);
        sheet.setPlayer(sheetMusicPlayer);
        viewSheetMusic.addView(sheet);
        sheetMusicPlayer.SetMidiFile(MidiPlayer.getPlayer().m_midifile, midiOptions, sheet);
        viewSheetMusic.requestLayout();
        sheet.callOnDraw();
    }

    //----------------------------- BGV Control Unit --------------------------
    int nScrollType = 1;   // 0: scroll left, 1: scroll right, 2: zoom in, 3: zoom out
    int idxBGVShowImage;
    ImageView[] ivBGV;
    Animation fadeIn, fadeOut;
    CustomHorizontalScrollView[] scrollViewBGV;
    VideoView videoViewBGV;
    
    public void startBGVTask() {
        // BGV
    	scrollViewBGV = new CustomHorizontalScrollView[2];
    	scrollViewBGV[0]= (CustomHorizontalScrollView) findViewById(R.id.scrollViewBGV1);
    	scrollViewBGV[1]= (CustomHorizontalScrollView) findViewById(R.id.scrollViewBGV2);

    	// Disable user scroll
    	scrollViewBGV[0].setScrollingEnabled(false);
    	scrollViewBGV[1].setScrollingEnabled(false);

    	ivBGV = new ImageView[2];
    	ivBGV[0] = (ImageView) findViewById(R.id.bgv1Iv);
    	ivBGV[1] = (ImageView) findViewById(R.id.bgv2Iv);
    	idxBGVShowImage = -1;

    	fadeIn = AnimationUtils.loadAnimation(cntxt, R.anim.image_fade_in);
    	fadeOut = AnimationUtils.loadAnimation(cntxt, R.anim.image_fade_out);
    	fadeIn.setAnimationListener(new Animation.AnimationListener() {
    	      @Override
    	      public void onAnimationStart(Animation animation) {
    	          scrollViewBGV[idxBGVShowImage].setVisibility(View.VISIBLE);
    	      }
    	      @Override
    	      public void onAnimationEnd(Animation animation) {
    	      }
    	      @Override
    	      public void onAnimationRepeat(Animation animation) {
    	      }
    	});

    	fadeOut.setAnimationListener(new Animation.AnimationListener() {
    	      @Override
    	      public void onAnimationStart(Animation animation) {
    	      }
    	      @Override
    	      public void onAnimationEnd(Animation animation) {
    	    	  scrollViewBGV[1 - idxBGVShowImage].setVisibility(View.GONE);
    	      }
    	      @Override
    	      public void onAnimationRepeat(Animation animation) {
    	      }
    	});

        String path = Global.getRootBackgroundsPath();
        File directory = new File(path);
        File[] files = directory.listFiles();
        bgvFiles.clear();
        for (int i = 0; i < files.length; i++)
        {
            String filePath = files[i].getAbsolutePath();
            String extension = filePath.substring(filePath.lastIndexOf(".")).toLowerCase();
            if (extension.compareTo(".jpg") == 0 || extension.compareTo(".mp4") == 0) {
                bgvFiles.add(filePath);
            }
        }

        mBGVUpdateHandler = new Handler();

        // Select Random BGV
        currentBgvId = RandomGen.getInstance().getNextIndex(bgvFiles.size());

        videoViewBGV = (VideoView) findViewById(R.id.videoView);
        // video finish listener
        videoViewBGV.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                switchCurrentBGV();
            }
        });

        switchCurrentBGV();
    }

    private void switchCurrentBGV() {
        if (bgvFiles.size() > 0) {
            currentBgvId = (currentBgvId + 1) % bgvFiles.size();
            String imgUrl = Uri.fromFile(new File(bgvFiles.get(currentBgvId))).toString();
            String extension = imgUrl.substring(imgUrl.lastIndexOf(".")).toLowerCase();
            int nPreShowImage = idxBGVShowImage;

            if (extension.compareTo(".jpg") == 0) {
                currentBgvType = 0;
                if (currentBgvId % 2 == 0) {
                    int nextBGVImage = (idxBGVShowImage + 1) % 2;
                    imageLoader.displayImage(imgUrl, ivBGV[nextBGVImage], imageOptions);
                    int maxScrollX = Math.abs(scrollViewBGV[nextBGVImage].getChildAt(0).getMeasuredWidth() - scrollViewBGV[nextBGVImage].getMeasuredWidth());
                    scrollViewBGV[nextBGVImage].scrollTo(maxScrollX, 0);
                    if (idxBGVShowImage >= 0) {
                        scrollViewBGV[idxBGVShowImage].startAnimation(fadeOut);
                    }
                    idxBGVShowImage = nextBGVImage;
                    scrollViewBGV[idxBGVShowImage].startAnimation(fadeIn);
                }
                else {
                    int nextBGVImage = (idxBGVShowImage + 1) % 2;
                    imageLoader.displayImage(imgUrl, ivBGV[nextBGVImage], imageOptions);
                    scrollViewBGV[nextBGVImage].scrollTo(0, 0);
                    if (idxBGVShowImage >= 0) {
                        scrollViewBGV[idxBGVShowImage].startAnimation(fadeOut);
                    }
                    idxBGVShowImage = nextBGVImage;
                    scrollViewBGV[idxBGVShowImage].startAnimation(fadeIn);
                }
            }
            else if (extension.compareTo(".mp4") == 0) {
                Uri uri = Uri.fromFile(new File(bgvFiles.get(currentBgvId)));
                videoViewBGV.setVideoURI(uri);

                currentBgvType = 1;
                if (idxBGVShowImage >= 0)
                    scrollViewBGV[idxBGVShowImage].startAnimation(fadeOut);
            }

            if (nPreShowImage == -1) {
                mBGVUpdateHandler.post(bgvMoveRunnable);
            }
        }
    }

    Runnable bgvMoveRunnable = new Runnable() {
        @Override
        public void run() {
            if (mWeakPlayer.get() != null) {
                if (currentBgvType == 0) {
                    int maxScrollX = Math.abs(scrollViewBGV[idxBGVShowImage].getChildAt(0).getMeasuredWidth() - scrollViewBGV[idxBGVShowImage].getMeasuredWidth());

                    if (currentBgvId % 2 == 0) {
                        int xpos = scrollViewBGV[idxBGVShowImage].getScrollX();
                        scrollViewBGV[idxBGVShowImage].scrollTo(xpos - 1, 0);
                        if (scrollViewBGV[idxBGVShowImage].getScrollX() <= 0) {
                            switchCurrentBGV();
                        }
                        mBGVUpdateHandler.postDelayed(bgvMoveRunnable, 50);
                    }
                    else {
                        int xpos = scrollViewBGV[idxBGVShowImage].getScrollX();

                        scrollViewBGV[idxBGVShowImage].scrollTo(xpos + 1, 0);
                        if (scrollViewBGV[idxBGVShowImage].getScrollX() >= maxScrollX) {
                            switchCurrentBGV();
                        }
                        mBGVUpdateHandler.postDelayed(bgvMoveRunnable, 50);
                    }
                }
                else if (currentBgvType == 1) {
                    if (videoViewBGV.isPlaying() == false) {
                        scrollViewBGV[0].setVisibility(View.INVISIBLE);
                        scrollViewBGV[1].setVisibility(View.INVISIBLE);
                        videoViewBGV.setVisibility(View.VISIBLE);
                        videoViewBGV.start();
                    }
                    mBGVUpdateHandler.postDelayed(bgvMoveRunnable, 50);
                }
            }

            long curTimeTicks = System.currentTimeMillis();
            if (Math.abs(curTimeTicks - midiPlayBarTicks) > 250) {
                midiPlayBarTicks = curTimeTicks;
                int nTotals = MidiPlayer.getPlayer().getTotalTicks();
                int nTicks = MidiPlayer.getPlayer().getCurrentTicks();
                if (MidiPlayer.getPlayer().IsPlaying()) {
                    int nMaxSeconds = (int)(MidiPlayer.getPlayer().getTicks2Millseconds(nTotals) / 1000);
                    int nCurSeconds = (int)(MidiPlayer.getPlayer().getTicks2Millseconds(nTicks) / 1000);
                    String maxMinutes = Integer.toString(nMaxSeconds / 60) + ":" + Integer.toString(nMaxSeconds % 60);
                    String curMinutes = Integer.toString(nCurSeconds / 60) + ":" + Integer.toString(nCurSeconds % 60);
                    mCurSecondsLabel.setText(curMinutes);
                    mMaxSecondsLabel.setText(maxMinutes);
                    midiPlayBar.setProgress(nTicks);
                }
                else if (MidiPlayer.getPlayer().getPlayState() == PLAYER_PLAYING) {
                    changePlay(PLAY_NONE);
                    mCurSecondsLabel.setText(mMaxSecondsLabel.getText());
                    midiPlayBar.setProgress(midiPlayBar.getMax());
                    if (songReplayFlag) {
                        if (playReplaySong()) {
                        }
                        else {
                            MidiPlayer.getPlayer().stop();
                        }
                    }
                    else {
                        if (playNextSong()) {
                        }
                        else {
                            MidiPlayer.getPlayer().stop();
                            MidiPlayer.getPlayer().seekByTick(0);
                        }
                    }
                }
            }
        }
    };

	//----------------------------- Play midi file ----------------------------
    private void playMedia() {
        String path = curMediaFile.getFilePath();
        if (TextUtils.isEmpty(path)) {
            return;
        }

        // Set Mark Options
        int iColor = Global.COLORS[mCurColorIdx];
        MarkManager.getManager().setColor(mCurColorIdx);
        MarkManager.getManager().setImageView(imgMark1, imgMark2, imgMarkText);
        MarkManager.getManager().setHandler(mLyricsHandler);

        mCurColorIdx++;
        if (mCurColorIdx >= Global.COLORS.length)
            mCurColorIdx = 0;

        // Midi Options
        mTxtSubTitle.setTextColor(iColor);
        mTxtTitle.setTextColor(iColor);
        kTxtLayout.setRedColor(iColor);
        kTxtLayout.setRubyOnOff(true);
        kTxtLayout.setShadow(false);

        // Prepare to play midi
        try {
            frmBack.setVisibility(View.VISIBLE);
            mTxtSubTitle.setText("");
            mTxtTitle.setText("");

            songLoadingFlag = true;
            Global.Debug("Start thread for load midi...");
            showProgressDialog();
            mLoadMidiThread = new Thread(mLoadMidiRunnable);
            mLoadMidiThread.start();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    // ---------------------------------- Midi with Lyrics --------------------------------
    private static final int STEP_SONGLIST = 0;
    private static final int STEP_SELECTSONG = 1;
    private static final int STEP_PLAY = 2;
    private static final int STEP_SETUP = 3;
    private static final int STEP_RECLIST = 4;
    private static final int STEP_FAVORITE = 5;

    private static final int PLAY_SONGINFO = 0;
    private static final int PLAY_LYRICS = 1;
    private static final int PLAY_MARK = 2;
    private static final int PLAY_NONE = 3;

    private int mStep;
    private int mTickIdx = 0;
    private Handler mLyricsHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (mStep != STEP_PLAY)
                return;

            switch (msg.what) {
                case 0: // 4,3,2,1 시작
                    // mLstLayout.clearTitle();

                    mTickIdx = 3;
                    changePlay(PLAY_LYRICS);
                    mTickImages[mTickIdx].setVisibility(View.VISIBLE);
                    break;
                case 1: // 4,3,2,1 변화
                    if (mTickIdx < 0 || mTickIdx > 3)
                        return;
                    mTickImages[mTickIdx].setVisibility(View.INVISIBLE);
                    mTickIdx--;
                    if (mTickIdx >= 0)
                        mTickImages[mTickIdx].setVisibility(View.VISIBLE);
                    break;
                case 2: // 노래 재생 완료
                    // stopMusic();

                    checkScore();
                    break;
                case 3: // 간주 플레이 시작
                    // kTxtLayout.initLyrics();
                    mGreetManager.show("<간  주>");
                    mTickIdx = 3;
                    changePlay(PLAY_NONE);
                    break;
                case 4: // 간주 플레이 끝
                    changePlay(PLAY_LYRICS);

                    break;
                case 5: // 가사 플레이 끝
                    changePlay(PLAY_NONE);
                    break;
                case 6: // 점수 현시 끝
                    finish();
                    break;
                case 7: // 가사부분 현시
                    changePlay(PLAY_NONE);
                    break;
                case 8: // 가사부분 현시
                    changePlay(PLAY_LYRICS);
                    break;
            }
            refreshLayout();
        }
    };

    private Thread mLoadMidiThread;
    private Runnable mLoadMidiRunnable = new Runnable() {
        @Override
        public void run() {

            Global.Debug("[mLoadMidiRunnable::run] begin");
            Global.Debug(curMediaFile.getLyricsFilePath() + ", " + curMediaFile.getFilePath());

            Global.Debug("[mLoadMidiRunnable::run] loadLyricsInfo");
            mLyricsInfo = LyricsInfo.loadLyricsInfo(curMediaFile.getLyricsFilePath(), curSongInfo);

            if (mLyricsInfo != null) {
                Global.Debug("[mLoadMidiRunnable::run] set File to Bismark");
                MidiPlayer.getPlayer().setPortSelection(5);
                int nResult = MainActivity.getPlayer().SetFile(curMediaFile.getFilePath());
                if (nResult != 0) {
                    Global.Debug("[mLoadMidiRunnable::run] failed to set File");
                    mLoadMidiHandler.sendEmptyMessage(0);
                    songLoadingFlag = false;
                    return;
                }

                Global.Debug("[mLoadMidiRunnable::run] set Lyrics-" + mLyricsInfo.getSubTitle());
                mLoadMidiHandler.sendEmptyMessage(0);
                MidiPlayer.getPlayer().setLyricsInfo(mLyricsInfo);

                Global.Debug("[mLoadMidiRunnable::run] set Lyrics successfully");
                int nMaxTicks = MidiPlayer.getPlayer().getTotalTicks();
                midiPlayBar.setMax(nMaxTicks);

                Global.Debug("[mLoadMidiRunnable::run] load Midi successfully");
                mLoadMidiHandler.sendEmptyMessage(1);

                while (MidiPlayer.getPlayer().IsPlaying() == false) {
                    try {
                        Thread.sleep(100);
                    }
                    catch (Exception ex) {
                        break;
                    }
                }
            }
            else {
                Global.Debug("Failed to load lyric file!");
                mLoadMidiHandler.sendEmptyMessage(2);
            }
            songLoadingFlag = false;
            Global.Debug("[mLoadMidiRunnable::run] end");
        }
    };

    private void showSongInfo() {
        String strTitle = "";
        String strSinger = "";
        if (TextUtils.isEmpty(strTitle)) {

        }

        mTxtTitle.setText(strTitle);
        mTxtSubTitle.setVisibility(View.GONE);

        mTxtComposer.setText(" ");
        mTxtLblComposer.setText(" ");
        mTxtDotComposer.setText(" ");

        mTxtWriter.setText("");
        mTxtLblWriter.setText(" ");
        mTxtDotWriter.setText(" ");

        mTxtSinger.setText(strSinger);
        mTxtLblSinger.setText(R.string.singer);
        mTxtDotSinger.setText(R.string.seperator);

        changePlay(PLAY_SONGINFO);
    }

    private Handler mLoadMidiHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    // Show Titles
                    changePlay(PLAY_SONGINFO);

                    String strTitle = mLyricsInfo.getTitle();
                    String strSubTitle = mLyricsInfo.getSubTitle();
                    String strComposer = mLyricsInfo.getComposer();
                    String strWriter = mLyricsInfo.getWriter();
                    String strSinger = mLyricsInfo.getSinger();
                    if (!mLyricsInfo.getSubsinger().equals(""))
                        strSinger += "(" + mLyricsInfo.getSubsinger() + ")";

                    mTxtTitle.setText(strTitle);
                    mTxtSubTitle.setText(strSubTitle);
                    mTxtSubTitle.setVisibility(strSubTitle.equals("") ? View.GONE : View.VISIBLE);

                    mTxtComposer.setText(strComposer);
                    mTxtWriter.setText(strWriter);
                    mTxtSinger.setText(strSinger);

                    Global.Debug("mTxtSubTitle.setText: " + strSubTitle + ", " + mTxtSubTitle.getText());

                    if ("".equals(strComposer)) {
                        mTxtLblComposer.setText(" ");
                        mTxtDotComposer.setText(" ");
                    } else {
                        mTxtLblComposer.setText(R.string.composer);
                        mTxtDotComposer.setText(R.string.seperator);
                    }

                    if ("".equals(strWriter)) {
                        mTxtLblWriter.setText(" ");
                        mTxtDotWriter.setText(" ");
                    } else {
                        mTxtLblWriter.setText(R.string.writer);
                        mTxtDotWriter.setText(R.string.seperator);
                    }

                    if ("".equals(strSinger)) {
                        mTxtLblSinger.setText(" ");
                        mTxtDotSinger.setText(" ");
                    } else {
                        mTxtLblSinger.setText(R.string.singer);
                        mTxtDotSinger.setText(R.string.seperator);
                    }
                    for (int i = 0; i < 3; i++) {
                        mTickImages[i].setVisibility(View.INVISIBLE);
                    }
                    mTickImages[3].setVisibility(View.VISIBLE);
                    changeStep(STEP_PLAY);
                    hideProgressDialog();
                    break;
                case 1:
                    if (mLoadMidiThread != null) {
                        mLoadMidiThread.interrupt();
                        mLoadMidiThread = null;
                    }
                    playMusic();
                    break;
                default:
                    hideProgressDialog();
                    Toast.makeText(cntxt, R.string.err_load_midi, Toast.LENGTH_LONG).show();
                    changeStep(STEP_SELECTSONG);

                    break;
            }

        }
    };

    private int mCurrentPlayStep = PLAY_NONE;

    public boolean isPlayLyrics() {
        return mLnLyrics.getVisibility() == View.VISIBLE ? true : false;
    }

    public void changePlay(int iType) {
        if (iType == PLAY_SONGINFO) {
            Global.Debug("changePlay: PLAY_SONGINFO");
            mLnLyrics.setVisibility(View.INVISIBLE);
            mLnMark.setVisibility(View.GONE);
            mLnSongInfo.setVisibility(View.VISIBLE);
        } else if (iType == PLAY_LYRICS) {
            Global.Debug("changePlay: PLAY_LYRICS");
            mGreetManager.hide();
            mLnSongInfo.setVisibility(View.VISIBLE);
            mLnMark.setVisibility(View.GONE);
            mLnLyrics.setVisibility(View.VISIBLE);
        } else if (iType == PLAY_MARK) {
            Global.Debug("changePlay: PLAY_MARK");
            mLnLyrics.setVisibility(View.GONE);
            mLnSongInfo.setVisibility(View.GONE);
            mLnMark.setVisibility(View.VISIBLE);
        } else if (iType == PLAY_NONE) {
            Global.Debug("changePlay: PLAY_NONE");
            mLnLyrics.setVisibility(View.GONE);
            mLnSongInfo.setVisibility(View.VISIBLE);
            mLnMark.setVisibility(View.GONE);
        }
        mCurrentPlayStep = iType;
    }

    private void changeStep(int iStep) {
        if (iStep == STEP_SELECTSONG) {
            mStep = STEP_SELECTSONG;
        } else if (iStep == STEP_SONGLIST || iStep == STEP_RECLIST || iStep == STEP_FAVORITE) {
            mStep = iStep;
        } else if (iStep == STEP_PLAY) {
            mStep = STEP_PLAY;
        } else if (iStep == STEP_SETUP) {
            mStep = STEP_SETUP;
        }

        refreshLayout();
    }

    private void playMusic() {
        Global.Debug("[MidiPlayerActivity::playMusic] Play Midi start");
        MidiPlayer.getPlayer().play(curSongInfo.getLanguage());
        mGreetManager.show("<전  주>");
        SetupManager.getManager().startPlay();
        MainActivity.getPlayer().Start();
    }

    public void refreshLayout() {
        if (frmBack != null)
            frmBack.requestLayout();
    }

    @Override
    public void onPlayLyrics() {
        mLyricsHandler.sendEmptyMessage(0);
    }

    public void onSetVisibleLyrics(boolean isHide)
    {
        if (isHide)
            mLyricsHandler.sendEmptyMessage(7);
        else
            mLyricsHandler.sendEmptyMessage(8);
    }

    @Override
    public void onPlayTick() {
        mLyricsHandler.sendEmptyMessage(1);
    }

    @Override
    public void onCompleted() {
        mLyricsHandler.sendEmptyMessage(2);
    }

    @Override
    public void onPlayInterlude() {
        mLyricsHandler.sendEmptyMessage(3);
    }

    @Override
    public void onEndInterlude() {
        mLyricsHandler.sendEmptyMessage(4);
    }

    @Override
    public void onEndLyrics() {
        mLyricsHandler.sendEmptyMessage(5);
    }


    private void checkScore() {
        boolean requireCheckScore = true;

        if (requireCheckScore) {
            int minScore = 98;//settings.getInt("min_score", 10);
            int currentScore = -1;

            /*if (mRecorder != null) {
                currentScore = (int) mRecorder.getScore();
            }*/

            MarkManager.getManager().init();
            if (/* !mMidiInfo.isEvent() && */MarkManager.getManager().start(currentScore, minScore)) {
                changePlay(PLAY_MARK);
            } else {
                mLyricsHandler.sendEmptyMessageDelayed(6, 500);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        if (sheetMusicPlayer != null) {
            sheetMusicPlayer.Stop();
        }
        MidiPlayer.getPlayer().stop();

        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (sheetMusicPlayer != null) {
            sheetMusicPlayer.Stop();
        }
        mBGVUpdateHandler.removeCallbacks(bgvMoveRunnable);
        MidiPlayer.getPlayer().stop();
        MidiPlayer.getPlayer().seekByTick(0);

        super.onBackPressed();
    }

    // --------------------------------------------------------------------------------------
}
