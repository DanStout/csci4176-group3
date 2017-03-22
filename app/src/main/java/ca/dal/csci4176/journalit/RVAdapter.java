package ca.dal.csci4176.journalit;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import ca.dal.csci4176.journalit.models.BulletItem;
import ca.dal.csci4176.journalit.models.DailyEntry;
import io.realm.OrderedRealmCollection;
import io.realm.RealmList;
import io.realm.RealmRecyclerViewAdapter;

public class RVAdapter extends RealmRecyclerViewAdapter<DailyEntry, RVAdapter.CardViewHolder>
{
    private Context mCtx;

    public class CardViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        @BindView(R.id.card_view)
        CardView cv;

        @BindView(R.id.date)
        TextView date;

        @BindView(R.id.note1)
        TextView note1;

        @BindView(R.id.note2)
        TextView note2;

        @BindView(R.id.picture)
        ImageView picture;

        private DailyEntry entry;

        CardViewHolder(View itemView)
        {
            super(itemView);
            itemView.setOnClickListener(this);
            ButterKnife.bind(this, itemView);
        }

        void bindToEntry(DailyEntry entry)
        {
            date.setText(entry.getDateFormatted());

            RealmList<BulletItem> notes = entry.getNotes();
            if (notes.size() > 0)
            {
                note1.setText(notes.get(0).getText());

                if (notes.size() > 1)
                {
                    note2.setText(notes.get(1).getText());
                }
            }
            setPhoto(entry.getPhotoPath());
            this.entry = entry;
        }

        private void setPhoto(String path)
        {
            if (path != null)
            {
                Bitmap full = BitmapFactory.decodeFile(path);
                if (full != null)
                {
                    Bitmap thumb = ThumbnailUtils.extractThumbnail(full, 150, 150);
                    picture.setImageBitmap(thumb);
                    return;
                }
            }
            picture.setImageResource(R.mipmap.ic_launcher);
        }

        @Override
        public void onClick(View v)
        {
            mCtx.startActivity(DailyEntryActivity.getIntent(mCtx, entry));
        }
    }


    RVAdapter(Context context, OrderedRealmCollection<DailyEntry> entries)
    {
        super(entries, true);
        mCtx = context;
    }

    @Override
    public CardViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card, parent, false);
        return new CardViewHolder(v);
    }

    @Override
    public void onBindViewHolder(CardViewHolder holder, int position)
    {
        DailyEntry ent = getItem(position);
        holder.bindToEntry(ent);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView)
    {
        super.onAttachedToRecyclerView(recyclerView);
    }
}
