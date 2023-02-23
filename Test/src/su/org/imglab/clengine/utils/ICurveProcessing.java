package su.org.imglab.clengine.utils;

import ru.ts.toykernel.geom.IGisObject;

/**
 * Created by IntelliJ IDEA.
 * User: vladm
 * Date: 06.04.2007
 * Time: 14:26:12
 * 
 */
public interface ICurveProcessing
{
	double perform(IGisObject graphobject,String segkey) throws Exception;
}
