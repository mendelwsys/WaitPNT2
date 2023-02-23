package ru.ts.toykernel.test.converter.tonewformat;

import ru.ts.toykernel.proj.stream.def.StreamProjImpl;
import ru.ts.toykernel.drawcomp.IDrawObjRule;
import ru.ts.toykernel.drawcomp.ILayer;
import ru.ts.toykernel.drawcomp.rules.def.CnStyleRuleImpl;
import ru.ts.toykernel.drawcomp.rules.def.CommonStyle;
import ru.ts.factory.IFactory;
import ru.ts.factory.IInitAble;
import ru.ts.toykernel.consts.INameConverter;
import ru.ts.toykernel.consts.KernelConst;
import ru.ts.toykernel.attrs.AObjAttrsFactory;
import ru.ts.toykernel.attrs.IAttrs;
import ru.ts.toykernel.attrs.def.DefAttrImpl;
import ru.ts.toykernel.storages.INodeStorage;
import ru.ts.toykernel.filters.IBaseFilter;
import ru.ts.toykernel.filters.stream.NodeFilter;
import ru.ts.toykernel.filters.stream.NodeFilter2;
import ru.ts.toykernel.gui.panels.ViewPicturePanel;
import ru.ts.toykernel.gui.apps.SFViewer;
import ru.ts.toykernel.trans.InitAbleMapTransformer;
import ru.ts.toykernel.converters.*;
import ru.ts.utils.gui.elems.IViewProgress;
import ru.ts.gisutils.proj.transform.IMapTransformer;
import ru.ts.gisutils.algs.common.MPoint;

import java.io.DataOutputStream;
import java.io.PrintWriter;
import java.io.OutputStreamWriter;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 */
public class StreamProjImplConvert extends StreamProjImpl
{

	private static final String METANAME = "meta0";
	private static final String CTXNAME="Cont0";
	private static final String VIEWNAME="View0";
	List<String> trnms=new LinkedList<String>();
	private String convname;
	private int lrindex =0;

	/**
	 * Simple Project implementation
	 *
	 * @param projectlocation	- loaction of projectctx
	 * @param ruleFactory		- draw rule factory
	 * @param filterFactory	  - filter factory
	 * @param storagesfactory	- storages factory
	 * @param layerFactory	   - layer factory
	 * @param objectAttrsFactory - factory of object attributes
	 * @param nameConverter	  - converter of names
	 * @param progress		   - progress indicator
	 */
	public StreamProjImplConvert(String projectlocation, IFactory<IDrawObjRule> ruleFactory, IFactory<IBaseFilter> filterFactory, IFactory<INodeStorage> storagesfactory, IFactory<ILayer> layerFactory, AObjAttrsFactory objectAttrsFactory, INameConverter nameConverter, IViewProgress progress)
	{
		super(projectlocation, ruleFactory, filterFactory, storagesfactory, layerFactory, objectAttrsFactory, nameConverter, progress);
	}

	private void saveProjContx(PrintWriter prnwr)
		throws Exception
	{
		//PrintWriter prnwr = new PrintWriter(new OutputStreamWriter(dos,"WINDOWS-1251"));
		setClassHeader(prnwr,KernelConst.PROJCTXT_TAGNAME);

		setObjHeader(prnwr,KernelConst.PROJCTXT_TAGNAME,this.getClass(),CTXNAME);

		prnwr.println("<metainfo>"+METANAME+"</metainfo>");
		prnwr.println("<storage>"+storage.getObjName()+"</storage>");

		List<ILayer> ll = getLayerList();
		for (ILayer iLayer : ll)
			prnwr.println("<layer>" + iLayer.getObjName() + "</layer>");

		for (String trnm : trnms)
			prnwr.println("<transformer>" + trnm + "</transformer>");

		setObjEnd(prnwr,KernelConst.PROJCTXT_TAGNAME);
		setClassEnd(prnwr,KernelConst.PROJCTXT_TAGNAME);
	}

	private void saveDraw(PrintWriter prnwr)
		throws Exception
	{
		setClassHeader(prnwr,KernelConst.VIEWCNTRL_TAGNAME);

		setObjHeader(prnwr,KernelConst.VIEWCNTRL_TAGNAME,ViewPicturePanel.class,VIEWNAME);

		setParam(prnwr,"InitSz","1024 1024");
		prnwr.println("<converter>"+convname+"</converter>");
		prnwr.println("<projcont>"+CTXNAME+"</projcont>");

		setObjEnd(prnwr,KernelConst.VIEWCNTRL_TAGNAME);
		setClassEnd(prnwr,KernelConst.VIEWCNTRL_TAGNAME);
	}

	private void saveApp(PrintWriter prnwr)
		throws Exception
	{
		setClassHeader(prnwr,KernelConst.APPLICATION_TAGNAME);

		setObjHeader(prnwr,KernelConst.APPLICATION_TAGNAME, SFViewer.class,"App0");
		prnwr.println("<viewctrl>"+VIEWNAME+"</viewctrl>");
		setObjEnd(prnwr,KernelConst.APPLICATION_TAGNAME);
		setClassEnd(prnwr,KernelConst.APPLICATION_TAGNAME);
	}

	protected void saveMetaInfo(DataOutputStream dos)
			throws Exception
	{

		PrintWriter prnwr = new PrintWriter(new OutputStreamWriter(dos,"WINDOWS-1251"));
		setClassHeader(prnwr,KernelConst.META_TAGNAME);
		setObjHeader(prnwr,KernelConst.META_TAGNAME,metaInfo.getClass(),METANAME);
		setParam(prnwr,"FORMAT VERSION",metaInfo.getFormatVersion());
		setParam(prnwr,"major",String.valueOf(metaInfo.getMajor()));
		setParam(prnwr,"minor",String.valueOf(metaInfo.getMinor()));
		setParam(prnwr,"projname",metaInfo.getProjName());
		setParam(prnwr,"boxColor",String.valueOf(Integer.toHexString(metaInfo.getBoxColor())));
		setParam(prnwr,"backColor",String.valueOf(Integer.toHexString(metaInfo.getBackgroundColor())));
		setParam(prnwr,"mapver",metaInfo.getS_mapversion());
		setParam(prnwr,"units",metaInfo.getS_MapUnitsName());

		setObjEnd(prnwr,KernelConst.META_TAGNAME);
		setClassEnd(prnwr,KernelConst.META_TAGNAME);


		setClassHeader(prnwr,KernelConst.TRANSFORMER_TAGNAME);
		int i=0;
		for (IMapTransformer iMapTransformer : transformers.values())
		{
			String objName = getObjName(iMapTransformer, "trans" + i);
			trnms.add(objName);
			setObjHeader(prnwr,KernelConst.TRANSFORMER_TAGNAME, InitAbleMapTransformer.class, objName);
			prnwr.println("<param Nm=\""+"wktsrc"+"\" Val=\'"+iMapTransformer.getSrcWKT()+"\'/>");
			prnwr.println("<param Nm=\""+"wktdst"+"\" Val=\'"+iMapTransformer.getDstWKT()+"\'/>");
			setObjEnd(prnwr,KernelConst.TRANSFORMER_TAGNAME);
			i++;
		}
		setClassEnd(prnwr,KernelConst.TRANSFORMER_TAGNAME);
		prnwr.flush();

		IProjConverter converter = (IProjConverter) convInitializer.createByTypeName(convInitializer.getS_convertertype());
		converter.initByBase64Point(convInitializer.getB_converter());
		converter.getAsShiftConverter().setBindP0(MPoint.getByBase64Point(convInitializer.getB_currentP0()));


//++DEBUG Проверка нового конвертора
			IScaledConverterCtrl scaledConv = converter.getAsScaledConverterCtrl();
			IShiftConverter shiftconverter = converter.getAsShiftConverter();
			MPoint pnt0 = shiftconverter.getBindP0();
			MPoint mPoint = scaledConv.increaseMap(1.0);
			MPoint drawpnt = new MPoint(pnt0.x * mPoint.x, pnt0.y  * mPoint.y);
			converter= new CrdConverterFactory.LinearConverterRSS
			(
						converter.getAsRotateConverter().getRotMatrix(),
						scaledConv.increaseMap(1.0),
						drawpnt
			);
			System.out.println("");
//--DEBUG


		setClassHeader(prnwr, KernelConst.CONVERTER_TAGNAME);
		convname =ConverterWriter(prnwr, converter,0);
		setClassEnd(prnwr,KernelConst.CONVERTER_TAGNAME);
		prnwr.flush();
	}

	public String ConverterWriter(PrintWriter prnwr, ILinearConverter converter,int i) throws Exception
	{
		List<ILinearConverter> convch = converter.getConverterChain();
		List<String> convnames=new LinkedList<String>();
		for (int j = 0; j < convch.size(); j++)
		{
			ILinearConverter conv = convch.get(j);
			if (conv instanceof IRotateConverter)
				convnames.add(setByRotate(prnwr, (IRotateConverter)conv,j));
			else if ((conv instanceof IShiftConverter))
				convnames.add(setByShift(prnwr, (IShiftConverter)conv,j));
			else if ((conv instanceof IScaledConverterCtrl))
				convnames.add(setByScaled(prnwr, (IScaledConverterCtrl)conv,j));
			else
				convnames.add(ConverterWriter(prnwr,conv,i+1));
		}
		String objName = getObjName(converter, "PROJ"+i);
		setObjHeader(prnwr,KernelConst.CONVERTER_TAGNAME,converter.getClass(),objName);
		for (String convname : convnames)
			prnwr.println("<"+KernelConst.CONVERTER_TAGNAME+">" + convname + "</"+KernelConst.CONVERTER_TAGNAME+">");
		setObjEnd(prnwr,KernelConst.CONVERTER_TAGNAME);
		return objName;
	}

	private String setByScaled(PrintWriter prnwr, IScaledConverterCtrl scc,int index) throws Exception
	{
		String objName = getObjName(scc, "scale"+index);
		setObjHeader(prnwr, KernelConst.CONVERTER_TAGNAME,scc.getClass(), objName);
		MPoint point = scc.getUnitsOnPixel();
		setParam(prnwr,"initscale",1.0/ point.getX()+" "+1.0/ point.getY());
		setObjEnd(prnwr,KernelConst.CONVERTER_TAGNAME);
		return objName;
	}

	private String setByShift(PrintWriter prnwr, IShiftConverter shc,int index) throws Exception
	{
		String objName = getObjName(shc, "shift"+index);
		setObjHeader(prnwr, KernelConst.CONVERTER_TAGNAME,shc.getClass(), objName);
		setParam(prnwr,"bindp",shc.getBindP0().getX()+" "+shc.getBindP0().getY());
		setObjEnd(prnwr,KernelConst.CONVERTER_TAGNAME);
		return objName;
	}

	private String setByRotate(PrintWriter prnwr, IRotateConverter rc,int index) throws Exception
	{
		String objName = getObjName(rc, "rot"+index);
		setObjHeader(prnwr, KernelConst.CONVERTER_TAGNAME,rc.getClass(), objName);
		String mval="";
		double[] val = rc.getRotMatrix();
		for (int j = 0; j < val.length; j++)
		{
			if (j!=0)
				mval+=" ";
			mval+=String.valueOf(val[j]);
		}
		setParam(prnwr,"matrix", mval);
		setObjEnd(prnwr,KernelConst.CONVERTER_TAGNAME);
		return objName;
	}

	public String getObjName(Object rc, String rotname)
	{
		String lrotname=null;
		if (rc instanceof IInitAble)
			lrotname=((IInitAble)rc).getObjName();
		return (lrotname!=null)?lrotname:rotname;
	}

	private void setClassHeader(PrintWriter prnwr, String tagname)
	{
		prnwr.println("<"+ tagname+"s>");
	}

	private void setObjHeader(PrintWriter prnwr,String tagname,Class cl,String objname)
	{
		prnwr.println("<"+tagname+">");
		prnwr.println("<"+tagname+"-name>"+objname+"</"+tagname+"-name>");
		prnwr.println("<class-name>"+cl.getName()+"</class-name>");
		prnwr.println("<params>");
	}

	private void setParam(PrintWriter prnwr,String name,String val)
	{
		prnwr.println("<param Nm=\""+name+"\" Val=\""+val+"\"/>");
	}

	private void setObjEnd(PrintWriter prnwr,String tagname)
	{
		prnwr.println("</params>");
		prnwr.println("</"+ tagname+">");
	}

	private void setClassEnd(PrintWriter prnwr, String tagname)
	{
		prnwr.println("</"+tagname+"s>");
	}

	public void savetoStream(DataOutputStream dos) throws Exception
	{
		PrintWriter prnwr = new PrintWriter(new OutputStreamWriter(dos,"WINDOWS-1251"));
		prnwr.println("<?xml version=\"1.0\" encoding=\"WINDOWS-1251\" ?>");
		prnwr.println("<project>");
		prnwr.flush();
		super.savetoStream(dos);
		dos.flush();
		prnwr = new PrintWriter(new OutputStreamWriter(dos,"WINDOWS-1251"));
		prnwr.println("</project>");
		prnwr.flush();
	}

	protected void savelayers(DataOutputStream dos)
			throws Exception
	{
		PrintWriter prnwr = new PrintWriter(new OutputStreamWriter(dos,"WINDOWS-1251"));
		prnwr.println("<rules>");
		int i=0;
		for (ILayer layer : layers)
		{
			prnwr.println("\t<rule>");
			IDrawObjRule rule = layer.getDrawRule();

			prnwr.println("\t\t<rule-name>R"+(i++)+"</rule-name>");
			prnwr.println("\t\t<class-name>");
			prnwr.println("\t\t\t"+rule.getClass().getCanonicalName());
			prnwr.println("\t\t</class-name>");
			prnwr.println("\t\t<params>");
			if (rule instanceof CnStyleRuleImpl)
			{
				CnStyleRuleImpl ruleimpl = (CnStyleRuleImpl) rule;
				CommonStyle cs = ruleimpl.getDefStyle();
					prnwr.println("\t\t\t<param Nm=\"HiRange\" Val=\""+cs.getScaleHiRange()+"\"/>");
					prnwr.println("\t\t\t<param Nm=\"LowRange\" Val=\""+cs.getScaleLowRange()+"\"/>");
					prnwr.println("\t\t\t<param Nm=\"ColorLine\" Val=\""+cs.getsHexColorLine()+"\"/>");
					prnwr.println("\t\t\t<param Nm=\"ColorFill\" Val=\""+cs.getsHexColorFill()+"\"/>");
					prnwr.println("\t\t\t<param Nm=\"LineStyle\" Val=\""+cs.getsHexLineStyle()+"\"/>");
					prnwr.println("\t\t\t<param Nm=\"LineThickness\" Val=\""+cs.getsLineThickness()+"\"/>");
				    if (ruleimpl.isTextMode())
					{
						prnwr.println("\t\t\t<param Nm=\"TextMode\" Val=\""+ruleimpl.isTextMode()+"\"/>");
						Font ft = ruleimpl.getFont();
						if (ft!=null)
						{
							prnwr.println("\t\t\t<param Nm=\"FTName\" Val=\""+ft.getName()+"\"/>");
							prnwr.println("\t\t\t<param Nm=\"FTStyle\" Val=\""+ft.getStyle()+"\"/>");
							prnwr.println("\t\t\t<param Nm=\"FTSize\" Val=\""+ft.getSize()+"\"/>");
						}
					}
			}
			prnwr.println("\t\t</params>");

			prnwr.println("\t</rule>");
		}
		prnwr.println("</rules>");
		prnwr.flush();

		prnwr.println("<filters>");
		i=0;
		for (ILayer layer : layers)
		{

			prnwr.println("\t<filter>");
			IBaseFilter filter = layer.getFilters().get(0);
			prnwr.println("\t\t<filter-name>F"+(i++)+"</filter-name>");
			prnwr.println("\t\t<class-name>");
			prnwr.println("\t\t\t"+filter.getClass().getCanonicalName()+"");
			prnwr.println("\t\t</class-name>");
			prnwr.println("\t\t<params>");
			if (filter instanceof NodeFilter)
				prnwr.println("\t\t\t<param Val=\""+((NodeFilter)filter).getNodeId()+"\"/>");
			if (filter instanceof NodeFilter2)
			{
				java.util.List<String> ids = ((NodeFilter2) filter).getNodesId();
				for (String id : ids)
					prnwr.println("\t\t\t<param Val=\""+ id +"\"/>");
			}
			prnwr.println("\t\t</params>");
			prnwr.println("\t</filter>");
		}
		prnwr.println("</filters>");
		prnwr.flush();

		prnwr.println("<layers>");
		i=0;
		for (ILayer layer : layers)
		{
			int curI = i++;
			IAttrs lrattr = layer.getLrAttrs();
			prnwr.println("\t<layer>");

			String lrname = layer.getObjName();
			if (lrname==null || lrname.length()==0)
			{
				lrname="L"+(lrindex++);
				IAttrs attrs = layer.getLrAttrs();
				attrs.put(KernelConst.LAYER_NAME,new DefAttrImpl(KernelConst.LAYER_NAME,lrname));
				layer.setLrAttrs(attrs);
			}

			prnwr.println("\t\t<layer-name>"+lrname+"</layer-name>");
			prnwr.println("\t\t<class-name>");
			prnwr.println("\t\t\t"+layer.getClass().getCanonicalName());
			prnwr.println("\t\t</class-name>");
			prnwr.print("\t\t");
			setParam(prnwr,"visible",(lrattr.get(KernelConst.LAYER_VISIBLE).getValue()).toString());
			prnwr.println("\t\t<storage>MAIN_STORAGE</storage>");
			prnwr.println("\t\t<filter>F"+ curI +"</filter>");
			prnwr.println("\t\t<rule>R"+curI+"</rule>");
			prnwr.println("\t</layer>");
		}
		prnwr.println("</layers>");
		prnwr.flush();
		saveProjContx(prnwr);
		saveDraw(prnwr);
		saveApp(prnwr);
		prnwr.flush();
	}

}
