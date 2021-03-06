package com.cooldevs.ridealong.ViewHolder;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import de.hdodenhof.circleimageview.CircleImageView;
import com.cooldevs.ridealong.Interface.IRecycItemListerner;
import com.cooldevs.ridealong.R;

public class AllFriendViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    public CircleImageView all_friends_profile_image;
    public CircleImageView all_friends_locate_image;
    public TextView all_friends_txt_user_email;
    IRecycItemListerner iRecycItemListerner;

    public void setiRecycItemListerner(IRecycItemListerner iRecycItemListerner) {
        this.iRecycItemListerner = iRecycItemListerner;
    }

    // this needs to show image of user with email so  all_friends_profile_image and all_friends_txt_user_email
    public AllFriendViewHolder(@NonNull View itemView) {
        super(itemView);

        all_friends_profile_image=(CircleImageView) itemView.findViewById(R.id.all_friends_profile_image);
        all_friends_locate_image=(CircleImageView) itemView.findViewById(R.id.all_friends_locate_image); //locate image means user image

        all_friends_txt_user_email = (TextView) itemView.findViewById(R.id.all_friends_txt_user_email);

        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

        iRecycItemListerner.onItemClickListener(view,getAdapterPosition());

    }
}
