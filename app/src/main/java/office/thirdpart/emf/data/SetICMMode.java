// Copyright 2002, FreeHEP.

package office.thirdpart.emf.data;

import java.io.IOException;

import office.thirdpart.emf.EMFConstants;
import office.thirdpart.emf.EMFInputStream;
import office.thirdpart.emf.EMFRenderer;
import office.thirdpart.emf.EMFTag;

/**
 * SetICMMode TAG.
 *
 * @author Mark Donszelmann
 * @version $Id: SetICMMode.java 10367 2007-01-22 19:26:48Z duns $
 */
public class SetICMMode extends EMFTag implements EMFConstants
{

    private int mode;

    public SetICMMode()
    {
        super(98, 1);
    }

    public SetICMMode(int mode)
    {
        this();
        this.mode = mode;
    }

    public EMFTag read(int tagID, EMFInputStream emf, int len) throws IOException
    {

        return new SetICMMode(emf.readDWORD());
    }

    public String toString()
    {
        return super.toString() + "\n  mode: " + mode;
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
