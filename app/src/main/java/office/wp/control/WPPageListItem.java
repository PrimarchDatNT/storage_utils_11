/*
 * 文件名称:          WPPageListItem.java
 *
 * 编译器:            android2.2
 * 时间:              上午10:24:57
 */
package office.wp.control;

import office.common.ICustomDialog;
import office.constant.EventConstant;
import office.fc.pdf.PDFHyperlinkInfo;
import office.fc.pdf.PDFLib;
import office.pdf.PDFFind;
import office.pdf.PDFPageListItem;
import office.pdf.RepaintAreaInfo;
import office.simpletext.control.SafeAsyncTask;
import office.system.IControl;
import office.system.beans.pagelist.APageListItem;
import office.system.beans.pagelist.APageListView;
import office.wp.view.PageRoot;
import office.wp.view.PageView;

import android.R.color;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.ProgressBar;

/**
 * word engine "PageListView" component item
 * <p>
 * <p>
 * Read版本:        Read V1.0
 * <p>
 * 作者:            ljj8494
 * <p>
 * 日期:            2013-1-8
 * <p>
 * 负责人:          ljj8494
 * <p>
 * 负责小组:
 * <p>
 * <p>
 */
public class WPPageListItem extends APageListItem
{
    private static final int BACKGROUND_COLOR = 0xFFFFFFFF;

    /**
     *
     * @param content
     * @param parentSize
     */
    public WPPageListItem(APageListView listView, IControl control, int pageWidth, int pageHeight)
    {
        super(listView, pageWidth, pageHeight);
        this.control = control;
        this.pageRoot= (PageRoot)listView.getModel();
        this.setBackgroundColor(BACKGROUND_COLOR);
    }

    /**
     *
     */
    public void onDraw(Canvas canvas)
    {
        PageView pv = pageRoot.getPageView(pageIndex);
        if (pv != null)
        {
            float zoom = listView.getZoom();
            canvas.save();
            canvas.translate(-pv.getX() * zoom, -pv.getY() * zoom);
            pv.drawForPrintMode(canvas, 0, 0, zoom);
            canvas.restore();
        }
    }

    /**
     *
     * @param pageIndex     page index (base 0)
     * @param pageWidth     page width of after scaled
     * @param pageHeight    page height of after scaled
     */
    public void setPageItemRawData(final int pIndex, int pageWidth, int pageHeight)
    {
        super.setPageItemRawData(pIndex, pageWidth, pageHeight);
        //final APageListItem own = this;
        if ((int)(listView.getZoom() * 100) == 100
            || (isInit && pIndex == 0))
        {
            listView.exportImage(this, null);
        }
        isInit = false;
    }

    /**
     * added reapint image view
     */
    protected void addRepaintImageView(Bitmap bmp)
    {
        postInvalidate();
        listView.exportImage(this, null);
    }

    /**
     * remove reapint image view
     */
    protected void removeRepaintImageView()
    {

    }


    /**
     *
     */
    public void dispose()
    {
        super.dispose();
        control = null;
        pageRoot = null;
    }
    //
    private boolean isInit = true;
    //
    private PageRoot pageRoot;

}
