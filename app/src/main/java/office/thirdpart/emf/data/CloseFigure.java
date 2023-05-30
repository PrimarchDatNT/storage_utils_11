// Copyright 2002, FreeHEP.
package office.thirdpart.emf.data;

import java.io.IOException;

import office.thirdpart.emf.EMFInputStream;
import office.thirdpart.emf.EMFRenderer;
import office.thirdpart.emf.EMFTag;

/**
 * CloseFigure TAG.
 *
 * @author Mark Donszelmann
 * @version $Id: CloseFigure.java 10367 2007-01-22 19:26:48Z duns $
 */
public class CloseFigure extends EMFTag {

    public CloseFigure() {
        super(61, 1);
    }

    public EMFTag read(int tagID, EMFInputStream emf, int len)
            throws IOException {

        return this;
    }

    /**
     * displays the tag using the renderer
     *
     * @param renderer EMFRenderer storing the drawing session data
     */
    public void render(EMFRenderer renderer) {
        renderer.closeFigure();
    }
}
