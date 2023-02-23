package su.org.imglab.clengine.utils;


import ru.ts.gisutils.algs.common.MPoint;
import ru.ts.gisutils.algs.common.GeomAlgs;
import ru.ts.toykernel.geom.IGisObject;
import ru.ts.toykernel.consts.INameConverter;
import ru.ts.toykernel.consts.DefNameConverter;
import ru.ts.toykernel.consts.KernelConst;

/**
 * Created by IntelliJ IDEA.
 * User: vladm
 * Date: 06.04.2007
 * Time: 14:37:46
 */
public class NearSegPointImpl
		implements ICurveProcessing
{
	private MPoint objpnt;
	private INameConverter nameconverter;
	private MPoint neartest = new MPoint();
	private double distance = -1;

	public NearSegPointImpl(MPoint objpnt, INameConverter nameConverter)
	{
		this.objpnt = objpnt;
		if (nameConverter==null)
			nameConverter=new DefNameConverter();
		this.nameconverter = nameConverter;

	}

	/**
	 * Выполнить проекцию (Вычислить расстояние от точки до сегмента линии)
	 *
	 * @param graphobject - линия
	 * @param segkey	  - идентификатор сегмента
	 * @return мимимально достигнутое расстояние до сегмента
	 * @throws Exception -
	 */
	public double perform(IGisObject graphobject, String segkey) throws Exception
	{

		if (!graphobject.getGeotype().equals(nameconverter.codeAttrNm2StorAttrNm(KernelConst.LINESTRING)))
			return -1;

		MPoint[] seg = graphobject.getSegmentById(Integer.valueOf(segkey));

		MPoint l_nearest = new MPoint();
		double l_distance = GeomAlgs.getShortestDistanceToSeg(seg, objpnt, l_nearest);
		if (distance == -1 || distance > l_distance)
		{
			neartest = l_nearest;
			distance = l_distance;
		}
		return distance;
	}

	public MPoint getNeartest()
	{
		return neartest;
	}

}
