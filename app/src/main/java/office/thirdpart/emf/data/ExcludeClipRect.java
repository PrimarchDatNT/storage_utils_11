// Copyright 2002, FreeHEP.

package office.thirdpart.emf.data;

import java.io.IOException;

import office.java.awt.Rectangle;
import office.thirdpart.emf.EMFInputStream;
import office.thirdpart.emf.EMFTag;

/**
 * ExcludeClipRect TAG.
 *
 * @author Mark Donszelmann
 * @version $Id: ExcludeClipRect.java 10367 2007-01-22 19:26:48Z duns $
 */
public class ExcludeClipRect extends EMFTag
{

    private Rectangle bounds;

    public ExcludeClipRect()
    {
        super(29, 1);
    }

    public ExcludeClipRect(Rectangle bounds)
    {
        this();
        this.bounds = bounds;
    }

    public EMFTag read(int tagID, EMFInputStream emf, int len) throws IOException
    {

        return new ExcludeClipRect(emf.readRECTL());
    }

    public String toString()
    {
        return super.toString() + "\n  bounds: " + bounds;
    }
}
