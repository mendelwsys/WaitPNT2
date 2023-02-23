package ru.ts.tapp1;

import ru.ts.toykernel.storages.mem.MemEditableStorageLr;
import ru.ts.toykernel.consts.KernelConst;
import ru.ts.toykernel.geom.IEditableGisObject;
import ru.ts.toykernel.attrs.IAttrs;
import ru.ts.toykernel.attrs.def.DefAttrImpl;
import ru.ts.gisutils.algs.common.MPoint;

import java.util.Iterator;

/**
 * Тестирование редактируемого хранилища
 */
public class TestEditableStorage
{
	public static void main(String[] args) throws Exception
	{
//		MemEditableStorageLr.truncate("C:\\MAPDIR\\TEST_LR\\", "0");
//		t1("C:\\MAPDIR\\TEST_LR\\", "0");
//		t2("C:\\MAPDIR\\TEST_LR\\", "0");
		t4("C:\\MAPDIR\\TEST_LR\\", "0");

	}

	private static void t4(String folderlayers, String nodeId)
			throws Exception
	{
		MemEditableStorageLr stor = new MemEditableStorageLr(folderlayers, nodeId);
		Iterator<String> keys = stor.getCurvesIds();
		if (keys.hasNext())
		{
			String key = keys.next();
			IEditableGisObject edobj = stor.getEditableObject(key);

			edobj.addSegment(1,new MPoint[]{new MPoint(5,3),new MPoint(6,2)});
			edobj.add2Segment(0,1,new MPoint[]{new MPoint(15,13),new MPoint(16,12)});

			edobj.splitCurveByPoint(2);
			stor.commit();
			stor.rearange();
//			IBaseGisObject obj = stor.getBaseGisByCurveId(key);
			System.out.println("End Ok");
		}
	}

	private static void t3(String folderlayers, String nodeId)
			throws Exception
	{
		MemEditableStorageLr stor = new MemEditableStorageLr(folderlayers, nodeId);
		stor.rearange();
		System.out.println("End Ok");
	}

	private static void t2(String folderlayers, String nodeId)
			throws Exception
	{
		MemEditableStorageLr stor = new MemEditableStorageLr(folderlayers, nodeId);
		Iterator<String> keys = stor.getCurvesIds();
		if (keys.hasNext())
		{
			IEditableGisObject edobj = stor.getEditableObject(keys.next());

			IAttrs attrs = edobj.getObjAttrs();
			attrs.put("ObjName",new DefAttrImpl("ObjName","SUPPER_PUPPER"));

			edobj.addPoint(1,new MPoint(2,2));
			edobj.addPoint(-1,new MPoint(3,3));
			stor.commit();
			System.out.println("End Ok");
		}
	}

	private static void t1(String folderlayers, String nodeId)
			throws Exception
	{
		MemEditableStorageLr stor = new MemEditableStorageLr(folderlayers, nodeId);
		IEditableGisObject newobj = stor.createObject(KernelConst.POINT);
		newobj.addPoint(-1,new MPoint(1,1));
		stor.commit();
		stor.releaseStorage();
		System.out.println("End Ok");
	}
}
