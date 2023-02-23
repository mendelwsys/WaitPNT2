package ru.ts.conv.rdxf.test;

import org.kabeja.parser.ParserBuilder;
import org.kabeja.parser.Parser;
import org.kabeja.dxf.*;
import org.kabeja.dxf.helpers.Point;

import java.util.Iterator;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: vladm
 * Date: 14.07.2011
 * Time: 16:37:58
 * To change this template use File | Settings | File Templates.
 */
public class MMain
{
	public static void main(String[] args) throws Exception
	{
		Parser parser = ParserBuilder.createDefaultParser();
		parser.parse("G:\\BACK_UP\\$D\\DownLoads\\dxf\\kabeja-0.4\\samples\\dxf\\draft1.dxf");


		DXFDocument doc = parser.getDocument();

		doc.getBounds();

//		Iterator bit = doc.getDXFBlockIterator();
//		while (bit.hasNext())
//		{
//			DXFBlock block = (DXFBlock) bit.next();
//			String layerId=block.getLayerID();
//			System.out.println("layerId = " + layerId+"  name=" + block.getName());
//
//			DXFLayer lr = doc.getDXFLayer(layerId);
//			int cl=lr.getColor();
//
//			Iterator eit = block.getDXFEntitiesIterator();
//			while (eit.hasNext())
//			{
//				DXFEntity ent = (DXFEntity) eit.next();
//
//				if (ent.getType().equals(DXFConstants.ENTITY_TYPE_LINE))
//				{
//					byte[] rgb=ent.getColorRGB();
//					int w=ent.getLineWeight();
//					double th=ent.getThickness();
//
//					DXFLine dxfLine = (DXFLine) ent;
//					Point pntstart = dxfLine.getStartPoint();
//					Point pntend = dxfLine.getEndPoint();
//				}
//				System.out.println("typename = " + ent.getType());
//			}

		Iterator lit = doc.getDXFLayerIterator();
		while (lit.hasNext())
		{
			DXFLayer layer = (DXFLayer) lit.next();
			System.out.println("layername=" + layer.getName());

			Iterator eit = layer.getDXFEntityTypeIterator();
			while (eit.hasNext())
			{
				String type = (String) eit.next();
				List listent = layer.getDXFEntities(type);


				for (int i = 0; i < listent.size(); i++)
				{
					DXFEntity ent = (DXFEntity) listent.get(i);
//					System.out.println("typename = " + ent.getType());

					byte[] rgb = ent.getColorRGB();
					int w = ent.getLineWeight();
					double th = ent.getThickness();

					if (ent.getType().equals(DXFConstants.ENTITY_TYPE_LINE))
					{

						DXFLine dxfLine = (DXFLine) ent;
						Point pntstart = dxfLine.getStartPoint();
						Point pntend = dxfLine.getEndPoint();
					}
					else if (ent.getType().equals(DXFConstants.ENTITY_TYPE_POLYLINE))
					{
						DXFPolyline dxfPoly = (DXFPolyline) ent;
						int vcont=dxfPoly.getVertexCount();
						for (int j = 0; j < vcont; j++)
						{
							DXFVertex vx = dxfPoly.getVertex(j);

							double x = vx.getX();
							double y=vx.getY();
						}
					}
					else if (ent.getType().equals(DXFConstants.ENTITY_TYPE_CIRCLE))
					{
						DXFCircle crcl=(DXFCircle)ent;
						Point pt = crcl.getCenterPoint();
					}
					else if (ent.getType().equals(DXFConstants.ENTITY_TYPE_TEXT))
					{
						DXFText txt=(DXFText)ent;
						String text=txt.getText();
						System.out.println("text = " + text);
					}
					else if (ent.getType().equals(DXFConstants.ENTITY_TYPE_SOLID))
					{
						  DXFSolid sol=(DXFSolid)ent;
						  sol.getPoint1();
					}
					else if (ent.getType().equals(DXFConstants.ENTITY_TYPE_DIMENSION))
					{
						DXFDimension dim= (DXFDimension) ent;
						dim.getDimensionText();
					}
					else if (ent.getType().equals(DXFConstants.ENTITY_TYPE_INSERT))
					{
						DXFInsert inst= (DXFInsert) ent;
						String bid = inst.getBlockID();
						inst.getType();
					}
					else if (ent.getType().equals(DXFConstants.ENTITY_TYPE_VIEWPORT))
					{
						DXFViewport vp= (DXFViewport) ent;
						vp.getAspectRatio();
					}
					else
					{

						System.out.println("typename = " + ent.getType());
					}

				}
			}
		}
	}
}
