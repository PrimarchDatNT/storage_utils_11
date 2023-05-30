// Copyright 2002, FreeHEP.

package office.thirdpart.emf.data;

import android.graphics.Point;

import java.io.IOException;

import office.java.awt.Rectangle;
import office.thirdpart.emf.EMFInputStream;
import office.thirdpart.emf.EMFTag;

/**
 * PolyBezierTo16 TAG.
 *
 * @author Mark Donszelmann
 * @version $Id: PolyBezierTo16.java 10367 2007-01-22 19:26:48Z duns $
 */
public class PolyBezierTo16 extends PolyBezierTo
{

    public PolyBezierTo16()
    {
        super(88, 1, null, 0, null);
    }

    public PolyBezierTo16(Rectangle bounds, int numberOfPoints, Point[] points)
    {
        super(88, 1, bounds, numberOfPoints, points);
    }

    public EMFTag read(int tagID, EMFInputStream emf, int len) throws IOException
    {

        Rectangle r = emf.readRECTL();
        int n = emf.readDWORD();
        return new PolyBezierTo16(r, n, emf.readPOINTS(n));
    }
}
