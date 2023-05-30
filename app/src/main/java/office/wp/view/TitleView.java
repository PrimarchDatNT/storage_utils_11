/*
 * 文件名称:          TitleView.java
 *
 * 编译器:            android2.2
 * 时间:              下午5:18:40
 */
package office.wp.view;

import office.constant.wp.WPViewConstant;
import office.simpletext.control.IWord;
import office.simpletext.model.IDocument;
import office.simpletext.model.IElement;
import office.simpletext.view.AbstractView;
import office.simpletext.view.IView;
import office.system.IControl;

/**
 * header、footer view
 * <p>
 * <p>
 * Read版本:        Read V1.0
 * <p>
 * 作者:            ljj8494
 * <p>
 * 日期:            2012-7-4
 * <p>
 * 负责人:          ljj8494
 * <p>
 * 负责小组:
 * <p>
 * <p>
 */
public class TitleView extends AbstractView
{

    /**
     *
     * @param elem
     */
    public TitleView(IElement elem)
    {
        super();
        this.elem = elem;
    }

    /**
     *
     */
    public short getType()
    {
        return WPViewConstant.TITLE_VIEW;
    }

    /**
     * 得到组件
     */
    public IWord getContainer()
    {
        if (pageRoot != null)
        {
            return pageRoot.getContainer();
        }
        return null;
    }

    /**
     * 得到组件
     */
    public IControl getControl()
    {
        if (pageRoot != null)
        {
            return pageRoot.getControl();
        }
        return null;
    }

    /**
     * 得到model
     */
    public IDocument getDocument()
    {
        if (pageRoot != null)
        {
            return pageRoot.getDocument();
        }
        return null;
    }
    /**
     *
     */
    public void setPageRoot(IView root)
    {
        pageRoot = root;
    }

    public void dispose()
    {
        super.dispose();
        pageRoot = null;
    }

    /**
     *
     */
    private IView pageRoot;
}
