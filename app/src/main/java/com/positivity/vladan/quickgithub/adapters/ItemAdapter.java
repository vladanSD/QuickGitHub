package com.positivity.vladan.quickgithub.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.positivity.vladan.quickgithub.R;
import com.positivity.vladan.quickgithub.model.Item;
import com.positivity.vladan.quickgithub.utilities.ColorUtils;

import java.util.ArrayList;

/**
 * Created by Vladan on 4/21/2017.
 */

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder> {

    final private OnListItemClickInterface onListItemClickInterface;
    private static int viewHolderCount;
    private ArrayList<Item> list;



    //interface for handling item clicking
    public interface OnListItemClickInterface {
        void onListItemClick(int clickedItemIndex);
    }

    //const
    public ItemAdapter(ArrayList<Item>listOfItems, OnListItemClickInterface listener) {
        onListItemClickInterface = listener;
        list = listOfItems;
        viewHolderCount = 0;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        //setting row view
        Context context = viewGroup.getContext();
        int layoutIdForListItem = R.layout.number_list_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, viewGroup, shouldAttachToParentImmediately);

        //creating viewHolder
        ViewHolder holder = new ViewHolder(view);

        // setting the color of holders in list
        int color = 0;

        if ( (viewHolderCount & 1) == 0 ) { color = 1;} else { color = 2; }
        int backgroundColorForViewHolder = ColorUtils
                .getViewHolderBackgroundColorFromInstance(context, color);
        holder.itemView.setBackgroundColor(backgroundColorForViewHolder);


        viewHolderCount++;
        return holder;
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        final Item item = list.get(position);
        holder.bind(position, item.getName(), item.getDescription());



    }


    @Override
    public int getItemCount() {
        return list.size();
    }



    //VIEWHOLDER class
    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView numberOfItemView;
        TextView itemNameView;
        TextView itemDescriptionView;


        public ViewHolder(View itemView) {
            super(itemView);
            numberOfItemView = (TextView) itemView.findViewById(R.id.tv_item_number);
            itemNameView = (TextView) itemView.findViewById(R.id.tv_item_name);
            itemDescriptionView = (TextView) itemView.findViewById(R.id.tv_item_description);
            itemView.setOnClickListener(this);
        }


        void bind(int listIndex, String name, String desc) {
            numberOfItemView.setText(String.valueOf(listIndex));
            itemNameView.setText(name);
            itemDescriptionView.setText(desc);

        }



        @Override
        public void onClick(View v) {
            int clickedPosition = getAdapterPosition();
            onListItemClickInterface.onListItemClick(clickedPosition);
        }
    }

}