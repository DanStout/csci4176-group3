package ca.dal.csci4176.journalit;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by WZ on 2017/3/16.
 */

public class RVAdapter extends RecyclerView.Adapter<RVAdapter.CardViewHolder>{
    public static class CardViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        TextView Date;
        TextView Note1;
        TextView Note2;
        ImageView Picture;

        CardViewHolder(View itemView) {
            super(itemView);
            cv = (CardView)itemView.findViewById(R.id.card_view);
            Date = (TextView)itemView.findViewById(R.id.date);
            Note1 = (TextView)itemView.findViewById(R.id.note1);
            Note2 = (TextView)itemView.findViewById(R.id.note2);
            Picture = (ImageView)itemView.findViewById(R.id.picture);
        }
    }

    List<Card> cards;

    public RVAdapter(List<Card> cards){
        this.cards = cards;
    }

    @Override
    public int getItemCount() {
        return cards.size();
    }

    @Override
    public CardViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card, viewGroup, false);
        CardViewHolder pvh = new CardViewHolder(v);
        return pvh;
    }

    @Override
    public void onBindViewHolder(CardViewHolder cardViewHolder, int i) {
        cardViewHolder.Date.setText(cards.get(i).date);
        cardViewHolder.Note1.setText(cards.get(i).note1);
        cardViewHolder.Note2.setText(cards.get(i).note2);
        cardViewHolder.Picture.setImageResource(cards.get(i).photoId);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }



}
