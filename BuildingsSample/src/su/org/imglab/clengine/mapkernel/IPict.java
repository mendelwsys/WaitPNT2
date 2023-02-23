package su.org.imglab.clengine.mapkernel;

import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: 13.12.2008
 * Time: 18:42:52
 *
 */
public interface IPict
{
	void drawObject(Graphics graphics, Point central, boolean isselected);
    int[] getSizeXY();
}
