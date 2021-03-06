package com.dihanov.musiq.ui.main.main_fragments.now_playing;

import android.view.View;

import com.dihanov.musiq.R;
import com.dihanov.musiq.di.app.App;
import com.dihanov.musiq.interfaces.ArtistDetailsIntentShowableImpl;
import com.dihanov.musiq.models.RecentTracksWrapper;
import com.dihanov.musiq.models.Track;
import com.dihanov.musiq.service.LastFmApiClient;
import com.dihanov.musiq.ui.adapters.RecentlyScrobbledAdapter;
import com.dihanov.musiq.ui.main.AlbumDetailsPopupWindow;
import com.dihanov.musiq.util.AppLog;
import com.dihanov.musiq.util.Constants;
import com.dihanov.musiq.util.TrackLoveManager;
import com.jakewharton.rxbinding2.view.RxView;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by dimitar.dihanov on 2/6/2018.
 */

public class NowPlayingPresenter extends ArtistDetailsIntentShowableImpl implements NowPlayingContract.Presenter {
    private static final long DELAY_IN_MILLIS = 500;
    private static final int RECENT_SCROBBLES_LIMIT = 20;
    private static final String TAG = NowPlayingPresenter.class.getSimpleName();

    private final LastFmApiClient lastFmApiClient;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private NowPlayingContract.View nowPlayingFragment;
    private TrackLoveManager trackLoveManager;

    @Inject
    public NowPlayingPresenter(LastFmApiClient lastFmApiClient) {
        this.lastFmApiClient = lastFmApiClient;
    }


    @Override
    public void takeView(NowPlayingContract.View view) {
        this.nowPlayingFragment = view;
        this.trackLoveManager = new TrackLoveManager(lastFmApiClient, view);
    }

    @Override
    public void leaveView() {
        this.nowPlayingFragment = null;
    }

    @Override
    public void loveTrack(String artistName, String trackName) {
       this.trackLoveManager.loveTrack(artistName, trackName);
    }

    @Override
    public void loadRecentScrobbles(NowPlayingContract.View nowPlayingFragment) {
        lastFmApiClient.getLastFmApiService()
                .getUserRecentTracks(App.getSharedPreferences().getString(Constants.USERNAME, ""), RECENT_SCROBBLES_LIMIT, 1)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<RecentTracksWrapper>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        compositeDisposable.add(d);
                    }

                    @Override
                    public void onNext(RecentTracksWrapper recentTracksWrapper) {
                        if(recentTracksWrapper == null){
                            return;
                        }

                        if(nowPlayingFragment == null ||
                                recentTracksWrapper == null ||
                                recentTracksWrapper.getRecenttracks() == null ||
                                recentTracksWrapper.getRecenttracks().getTrack() == null){
                            return;
                        }

                        List<Track> result = recentTracksWrapper.getRecenttracks().getTrack();

                        RecentlyScrobbledAdapter adapter =
                                new RecentlyScrobbledAdapter(result, nowPlayingFragment.getMainActivity(), NowPlayingPresenter.this);


                        nowPlayingFragment.setRecyclerViewAdapter(adapter);
                    }

                    @Override
                    public void onError(Throwable e) {
                        AppLog.log(TAG, e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        compositeDisposable.clear();
                    }
                });
    }

    @Override
    public void setClickListenerFetchEntireAlbumInfo(View view, String artistName, String albumName) {
        AlbumDetailsPopupWindow albumDetailsPopupWindow = new AlbumDetailsPopupWindow(lastFmApiClient, nowPlayingFragment.getMainActivity());
        albumDetailsPopupWindow.showPopupWindow(nowPlayingFragment.getMainActivity(), view, artistName, albumName, R.id.main_content);
    }

    @Override
    public void addOnArtistResultClickedListener(View view, String artistName) {
        RxView.clicks(view)
                .debounce(DELAY_IN_MILLIS, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(click -> this.showArtistDetailsIntent(artistName, nowPlayingFragment.getMainActivity()));
    }

    @Override
    public void unloveTrack(String artistName, String trackName) {
        this.trackLoveManager.unloveTrack(artistName, trackName);
    }
}
