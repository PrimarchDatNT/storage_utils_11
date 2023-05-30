package com.tapon.storageandroid11;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.text.format.Formatter;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.MediaStoreSignature;
import com.tapon.storageandroid11.databinding.ItemFileListBinding;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class MediaAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context context;

    private List<MediaFile> listItem;

    private Callback callback;

    public MediaAdapter(Context context) {
        this.context = context;
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public void setListItem(List<MediaFile> listItem) {
        this.listItem = listItem;
        this.notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(this.context);
        return new MediaViewHolder(ItemFileListBinding.inflate(inflater, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MediaViewHolder) {
            ((MediaViewHolder) holder).bindItem(position);
        }
    }

    @Override
    public int getItemCount() {
        return this.listItem == null ? 0 : this.listItem.size();
    }

    public interface Callback {

        void onItemClick(int position, MediaFile item);

        void onClickShareItem(int position, MediaFile item);

        void onClickEditItem(int position, MediaFile item);

        void onClickDeleteItem(int position, MediaFile item);
    }

    class MediaViewHolder extends RecyclerView.ViewHolder {

        private final ItemFileListBinding binding;

        public MediaViewHolder(@NonNull ItemFileListBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        @SuppressLint("SetTextI18n")
        public void bindItem(int position) {
            MediaFile item = listItem.get(position);
            if (item == null) {
                return;
            }

            if (item.uri.toString().contains("audio")) {
                Bitmap audioThumb = null;
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        audioThumb = ThumbnailUtils.createAudioThumbnail(new File(item.path), new Size(64, 64), null);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (audioThumb == null) {
                    this.binding.ivIcon.setImageResource(R.drawable.ic_sharp_audiotrack_24);
                    return;
                }
                this.binding.ivIcon.setImageBitmap(audioThumb);
            } else {
                Glide.with(context)
                        .load(item.uri)
                        .centerCrop()
                        .signature(new MediaStoreSignature(item.mime, item.time, 0))
                        .thumbnail(0.35f)
                        .error(R.drawable.ic_sharp_broken_image_24)
                        .into(this.binding.ivIcon);
            }

            this.binding.tvTitle.setText(item.name);
            this.binding.tvFileInfo.setText(item.mime + " | " + Formatter.formatFileSize(context, item.size) + " | " + Utils.formatDate(item.time));

            this.itemView.setOnClickListener(v -> {
                if (callback != null) {
                    callback.onItemClick(position, item);
                }
            });

            this.binding.ivRename.setOnClickListener(v -> {
                if (callback != null) {
                    callback.onClickEditItem(position, item);
                }
            });

            this.binding.ivShare.setOnClickListener(v -> {
                if (callback != null) {
                    callback.onClickShareItem(position, item);
                }
            });

            this.binding.ivDelete.setOnClickListener(v -> {
                if (callback != null) {
                    callback.onClickDeleteItem(position, item);
                }
            });
        }
    }
}
