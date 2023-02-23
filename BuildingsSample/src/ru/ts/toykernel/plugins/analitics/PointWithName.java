package ru.ts.toykernel.plugins.analitics;

import ru.ts.gisutils.algs.common.IMCoordinate;
import ru.ts.gisutils.algs.common.MPoint;
import ru.ts.gisutils.algs.common.MPointZ;
import ru.ts.gisutils.algs.common.MPointZM;
import ru.ts.gisutils.geometry.ICoordinate;
import ru.ts.gisutils.geometry.IGetXY;

import java.awt.*;

public class PointWithName extends MPointZM
{
    private String pntName;

    public PointWithName() {
    }

    public PointWithName(IGetXY xy) {
        super(xy);
    }

    public PointWithName(ICoordinate crd) {
        super(crd);
    }

    public PointWithName(IMCoordinate crd) {
        super(crd);
    }

    public PointWithName(PointWithName crd) {
        super(crd);
        pntName = crd.getPntName();
    }

    public PointWithName(double x, double y, double z, double m) {
        super(x, y, z, m);
    }

    public PointWithName(MPointZM p) {
        super(p);
    }

    public PointWithName(MPointZ p) {
        super(p);
    }

    public PointWithName(MPoint p) {
        super(p);
    }

    public PointWithName(Point p) {
        super(p);
    }

    public PointWithName(Point.Double p) {
        super(p);
    }

    public String getPntName() {
        return pntName;
    }

    public void setPntName(String pntName) {
        this.pntName = pntName;
    }
}
