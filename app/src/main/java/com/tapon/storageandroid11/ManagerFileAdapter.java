package com.tapon.storageandroid11;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tapon.storageandroid11.databinding.ItemFileListBinding;

import java.io.File;
import java.util.List;


public class ManagerFileAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context context;

    private Callback callback;
    private List<File> listItem;

    public ManagerFileAdapter(Context context) {
        this.context = context;
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public File getItemPosition(int position) {
        if (this.listItem == null || this.listItem.isEmpty()) {
            return null;
        }
        return this.listItem.get(position);
    }

    public void setListItem(List<File> listItem) {
        this.listItem = listItem;
        this.notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ItemHolder(ItemFileListBinding.inflate(LayoutInflater.from(this.context), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ItemHolder) {
            ((ItemHolder) holder).bindItem(position);
        }
    }

    @Override
    public int getItemCount() {
        return this.listItem == null ? 0 : this.listItem.size();
    }

    public interface Callback {

        void onClickItem(File item);

        void onClickEditItem(File item);

        void onClickShare(File item);

        void onClickDeleteItem(File item);
    }

    private final class ItemHolder extends RecyclerView.ViewHolder {

        private final ItemFileListBinding binding;

        public ItemHolder(@NonNull ItemFileListBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        @SuppressLint("SetTextI18n")
        public void bindItem(int position) {
            File item = listItem.get(position);
            if (item == null) {
                return;
            }

            this.binding.tvTitle.setText(item.getName());
            this.binding.ivIcon.setImageResource(item.isDirectory() ? R.drawable.ic_baseline_folder_24 : R.drawable.ic_baseline_insert_drive_file_24);

            String[] count = item.list();
            String size = item.isDirectory() ? context.getString(R.string.folder_item_count, count == null ? 0 : count.length) : Utils.convertBytes(item.length());

            this.binding.tvFileInfo.setText(size + "  |  " + Utils.formatDate(item.lastModified()) + "  |  " + Utils.getExtension(item));

            this.itemView.setOnClickListener(v -> {
                if (callback != null) {
                    callback.onClickItem(item);
                }
            });

            this.binding.ivRename.setOnClickListener(v -> {
                if (callback != null) {
                    callback.onClickEditItem(item);
                }
            });

            this.binding.ivShare.setOnClickListener(v -> {
                if (callback != null) {
                    callback.onClickShare(item);
                }
            });

            this.binding.ivDelete.setOnClickListener(v -> {
                if (callback != null) {
                    callback.onClickDeleteItem(item);
                }
            });
        }
    }

}
