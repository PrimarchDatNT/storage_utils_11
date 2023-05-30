package office.ss.sheetbar;

import android.content.Context;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.tapon.storageandroid11.R;
import com.tapon.storageandroid11.Utils;

/* loaded from: classes.dex */
public class SheetButton extends LinearLayout {
    private static final int FONT_SIZE = 12;
    private static final int SHEET_BUTTON_MIN_WIDTH = 100;
    private boolean active;
    private final int sheetIndex;
    private TextView textView;

    public SheetButton(Context context, String str, int i) {
        super(context);
        setOrientation(HORIZONTAL);
        this.sheetIndex = i;
        init(context, str);
    }

    private void init(Context context, String str) {
        TextView textView = new TextView(context);
        this.textView = textView;
        textView.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.bottom_dialog_bg_radius_top));
        this.textView.setText(str);
        this.textView.setTextSize(12.0f);
        this.textView.setGravity(17);
        this.textView.setTextColor(ContextCompat.getColor(getContext(), R.color.blue));
        addView(this.textView, new LinearLayout.LayoutParams(Math.max((int) (this.textView.getPaint().measureText(str) + Utils.dip2px(context, 26.0f)), Utils.dip2px(context, 80.0f)), -1));
    }

    public void changeFocus(boolean z) {
        Context context;
        int i;
        this.active = z;
        this.textView.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.bottom_dialog_bg_radius_top));
        TextView textView = this.textView;
        if (z) {
            context = getContext();
            i = R.color.black;
        } else {
            context = getContext();
            i = R.color.blue;
        }
        textView.setTextColor(ContextCompat.getColor(context, i));
    }

    public int getSheetIndex() {
        return this.sheetIndex;
    }

    public void dispose() {
        this.textView = null;
    }
}