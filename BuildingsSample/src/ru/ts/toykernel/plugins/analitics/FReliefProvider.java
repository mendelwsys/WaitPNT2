package ru.ts.toykernel.plugins.analitics;

import ru.ts.gisutils.algs.common.IMCoordinate;
import ru.ts.gisutils.algs.common.MPoint;
import ru.ts.toykernel.attrs.AObjAttrsFactory;
import ru.ts.toykernel.attrs.IAttrs;
import ru.ts.toykernel.attrs.IDefAttr;
import ru.ts.toykernel.consts.KernelConst;
import ru.ts.toykernel.converters.ILinearConverter;
import ru.ts.toykernel.factory.BaseInitAble;
import ru.ts.toykernel.geom.IBaseGisObject;
import ru.ts.toykernel.geom.IGisObject;
import ru.ts.utils.data.Pair;

import java.util.*;

public class FReliefProvider
        extends BaseInitAble implements IReliefProvider
{

    private Map<String, Map<String, Double>> analitparams;

    public FReliefProvider() {
    }

    public AObjAttrsFactory getObjFactory()
    {

        return null;
    }

    public Object init(Object obj) throws Exception {
        return null;
    }

    public String getAttrFormula() {
        return null;
    }

    @Override
    public void setAttrFormula(String formula) {
    }

    public void setAnalitParams(Map<String, List<Pair<String, Double>>> analitparams)
    {
        this.analitparams=new HashMap<String, Map<String, Double>>();
        for (String key : analitparams.keySet())
        {
            List<Pair<String, Double>> obj_Val_List = analitparams.get(key);
            if (key.length()>0 && obj_Val_List.size()>0)
            {
                Map<String, Double> objName2val = this.analitparams.get(key);
                if (objName2val==null)
                    this.analitparams.put(key,objName2val=new HashMap<String, Double>());

                for (Pair<String, Double> stringDoublePair : obj_Val_List)
                    objName2val.put(stringDoublePair.first, stringDoublePair.second);
            }
        }
    }

    public IMCoordinate[] getRelief(Iterator<IBaseGisObject> baseObjects, ILinearConverter converter) throws Exception
    {
        List<IMCoordinate> rl = new LinkedList<IMCoordinate>();

        while (baseObjects.hasNext())
        {
            final IBaseGisObject iBaseGisObject = baseObjects.next();
            String objId=iBaseGisObject.getCurveId();
            String[] layerName2Object=objId.split("#\\$");
            if (layerName2Object!=null && layerName2Object.length>=2)
            {
                Map<String, Double> objName2val = analitparams.get(layerName2Object[0]);
                if (objName2val!=null)
                {
                    Double calcvalue = objName2val.get(layerName2Object[1]);
                    if (calcvalue != null) {
                        MPoint pnt = ((IGisObject) iBaseGisObject).getMidlePoint();
                        PointWithName npnt = new PointWithName(converter.getDstPointByPoint(pnt));
                        npnt.setM(calcvalue);
                        IAttrs objAttrs = iBaseGisObject.getObjAttrs();
                        if (objAttrs!=null)
                        {
                            IDefAttr iDefAttr = objAttrs.get(KernelConst.ATTR_CURVE_NAME);
                            if (iDefAttr!=null)
                            {
                                Object value = iDefAttr.getValue();
                                if (value !=null)
                                    npnt.setPntName(value.toString());
                            }
                        }
                        rl.add(npnt);
                    }
                }
            }
        }
        return rl.toArray(new IMCoordinate[rl.size()]);
    }


}
