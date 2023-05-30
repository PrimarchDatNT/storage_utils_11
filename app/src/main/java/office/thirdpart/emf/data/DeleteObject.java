// Copyright 2002, FreeHEP.

package office.thirdpart.emf.data;

import java.io.IOException;

import office.thirdpart.emf.EMFInputStream;
import office.thirdpart.emf.EMFRenderer;
import office.thirdpart.emf.EMFTag;

/**
 * DeleteObject TAG.
 *
 * @author Mark Donszelmann
 * @version $Id: DeleteObject.java 10367 2007-01-22 19:26:48Z duns $
 */
public class DeleteObject extends EMFTag
{

    private int index;

    public DeleteObject()
    {
        super(40, 1);
    }

    public DeleteObject(int index)
    {
        this();
        this.index = index;
    }

    public EMFTag read(int tagID, EMFInputStream emf, int len) throws IOException
    {

        return new DeleteObject(emf.readDWORD());
    }

    public String toString()
    {
        return super.toString() + "\n  index: 0x" + Integer.toHexString(index);
    }

    /**
     * displays the tag using the renderer
     *
     * @param renderer EMFRenderer storing the drawing session data
     */
    public void render(EMFRenderer renderer)
    {
        renderer.storeGDIObject(index, null);
    }
}
