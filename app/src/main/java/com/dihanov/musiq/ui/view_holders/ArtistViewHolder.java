package com.dihanov.musiq.ui.view_holders;

import android.content.Context;
import android.support.v7.widget.PopupMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.dihanov.musiq.R;
import com.dihanov.musiq.ui.adapters.AbstractAdapter;
import com.dihanov.musiq.ui.listeners.MyMenuItemClickListener;
import com.dihanov.musiq.util.Constants;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by dimitar.dihanov on 11/10/2017.
 */

public class ArtistViewHolder extends AbstractViewHolder {
    @BindView(R.id.title)
    TextView title;

    @BindView(R.id.count)
    TextView count;

    @BindView(R.id.thumbnail)
    ImageView thumbnail;

    @BindView(R.id.artist_overflow)
    ImageView overflow;

    public ArtistViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
    }

    @Override
    public void showPopupMenu(Context context, View view, AbstractAdapter adapter)  {
        // inflate menu
        PopupMenu popup = new PopupMenu(context, view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_artist, popup.getMenu());
        Menu menu = popup.getMenu();

        if(!this.getIsFavorited()){
            menu.findItem(R.id.action_remove_favorite).setVisible(false);
            menu.findItem(R.id.action_add_favourite).setVisible(true);
        } else {
            menu.findItem(R.id.action_remove_favorite).setVisible(true);
            menu.findItem(R.id.action_add_favourite).setVisible(false);
        }


        popup.setOnMenuItemClickListener(new MyMenuItemClickListener(this.getTitle().getText().toString(),
                this, Constants.FAVORITE_ARTISTS_KEY, adapter));
        popup.show();
    }

    @Override
    public ImageView getThumbnail() {
        return this.thumbnail;
    }

    @Override
    public TextView getTitle() {
        return title;
    }

    public TextView getCount() {
        return count;
    }

    public ImageView getOverflow() {
        return overflow;
    }
}
