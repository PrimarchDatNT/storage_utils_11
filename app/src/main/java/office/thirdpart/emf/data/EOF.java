// Copyright 2002, FreeHEP.

package office.thirdpart.emf.data;

import java.io.IOException;

import office.thirdpart.emf.EMFInputStream;
import office.thirdpart.emf.EMFRenderer;
import office.thirdpart.emf.EMFTag;

/**
 * Rectangle TAG.
 *
 * @author Mark Donszelmann
 * @version $Id: EOF.java 10367 2007-01-22 19:26:48Z duns $
 */
public class EOF extends EMFTag
{

    public EOF()
    {
        super(14, 1);
    }

    public EMFTag read(int tagID, EMFInputStream emf, int len) throws IOException
    {

        /* int[] bytes = */emf.readUnsignedByte(len);
        return new EOF();
    }

    /**
     * displays the tag using the renderer
     *
     * @param renderer EMFRenderer storing the drawing session data
     */
    public void render(EMFRenderer renderer)
    {
        // do nothing
    }
}
