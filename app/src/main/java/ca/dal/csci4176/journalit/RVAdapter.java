package ca.dal.csci4176.journalit;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import ca.dal.csci4176.journalit.models.DailyEntry;
import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;

public class RVAdapter extends RealmRecyclerViewAdapter<DailyEntry, RVAdapter.CardViewHolder>
{
    private Context mCtx;

    public class CardViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        CardView cv;
        TextView date;
        TextView note1;
        TextView note2;
        ImageView picture;

        private DailyEntry entry;

        CardViewHolder(View itemView)
        {
            super(itemView);
            itemView.setOnClickListener(this);

            cv = (CardView) itemView.findViewById(R.id.card_view);
            date = (TextView) itemView.findViewById(R.id.date);
            note1 = (TextView) itemView.findViewById(R.id.note1);
            note2 = (TextView) itemView.findViewById(R.id.note2);
            picture = (ImageView) itemView.findViewById(R.id.picture);
        }

        void bindToEntry(DailyEntry entry)
        {
            date.setText(entry.getDateFormatted());
            note1.setText(entry.getText());
            note2.setText("Second Line Here");
            picture.setImageResource(R.mipmap.ic_launcher);

            this.entry = entry;
        }

        @Override
        public void onClick(View v)
        {
            mCtx.startActivity(DailyEntryActivity.getIntent(mCtx, entry.getKey()));
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
        CardViewHolder hold = new CardViewHolder(v);
        return hold;
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
