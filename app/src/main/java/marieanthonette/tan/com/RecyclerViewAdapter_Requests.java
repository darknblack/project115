package marieanthonette.tan.com;

import android.content.Context;
import android.net.Uri;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class RecyclerViewAdapter_Requests extends RecyclerView.Adapter<RecyclerViewAdapter_Requests.ViewHolder>{

    private static final String TAG = "RecyclerViewAdapter_Requests";

    private ArrayList<String> mName = new ArrayList<>();
    private ArrayList<String> mImages = new ArrayList<>();
    private ArrayList<String> mInquirer = new ArrayList<>();
    private ArrayList<String> mAddress = new ArrayList<>();

    private Context mContext;

    public RecyclerViewAdapter_Requests(Context context,
                                   ArrayList<String> imageNames,
                                   ArrayList<String> images,
                                   ArrayList<String> inquirer,
                                   ArrayList<String> address) {
        mContext = context;
        mName = imageNames;
        mImages = images;
        mInquirer = inquirer;
        mAddress = address;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_request_list_item, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {

        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(mImages.get(position));

        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(mContext)
                        .asBitmap()
                        .load(uri.toString())
                        .into(holder.image);
            }
        });

        holder.address.setText(mAddress.get(position));
        holder.imageName.setText(mName.get(position));
        holder.inquirerName.setText("Inquirer: " + mInquirer.get(position));
    }

    @Override
    public int getItemCount() {
        return mName.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder{

        ImageView image;
        TextView imageName, inquirerName, address;
        ConstraintLayout parentLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.imageThumb);
            inquirerName = itemView.findViewById(R.id.shelterAddress);
            imageName = itemView.findViewById(R.id.shelterName);
            address = itemView.findViewById(R.id.address);
            parentLayout = itemView.findViewById(R.id.parent_layout);
        }
    }
}
