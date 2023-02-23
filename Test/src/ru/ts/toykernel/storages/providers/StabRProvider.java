package ru.ts.toykernel.storages.providers;

import ru.ts.toykernel.raster.providers.IRPRovider;
import ru.ts.toykernel.factory.BaseInitAble;
import ru.ts.toykernel.attrs.IDefAttr;
import ru.ts.toykernel.proj.ICliConfigProvider;
import ru.ts.toykernel.consts.KernelConst;
import ru.ts.utils.data.Pair;

import java.awt.image.BufferedImage;
import java.awt.*;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

/**
 * Тестовый провайдер растров для отладки соединения с клиентом
 * ru.ts.toykernel.storages.providers.StabRProvider
 */
public class StabRProvider extends BaseInitAble
		implements IRPRovider
{


	private String provName;
	private ICliConfigProvider cong_provider;

	public StabRProvider()
	{
	}

	public StabRProvider(String provName)
	{
		this.provName = provName;
	}

	public Object init(Object obj) throws Exception
	{
		IDefAttr attr=(IDefAttr)obj;
		if (attr.getName().equalsIgnoreCase("CONNECT"))
			provName=(String)attr.getValue();
		else
			if (attr.getName().equalsIgnoreCase(KernelConst.CONFPROVIDERS_TAGNAME))
				cong_provider=(ICliConfigProvider)attr.getValue();
		return null;
	}

	public void getRasterParameters(double[] dXdY, double[] szXszY, int[] nXnY) throws Exception
	{

		//todo StabConfigRovider.getProvider().getServBySession(cong_provider.getSession()).getRasterParamters(provName,dXdY,szXszY,nXnY);


	}

	public Pair<BufferedImage, String> getRawRasterByImgIndex(int[] imgindex, Map<String, String[]> args) throws Exception
	{
//TODO		ByteArrayOutputStream bos=new ByteArrayOutputStream();
//		BufferedImage img = null;
//		Pair<Boolean, String> pr = StabConfigRovider.getProvider().getServBySession(cong_provider.getSession()).getImmage(provName, imgindex, bos);
//		bos.flush();
//		bos.close();
//		if (pr.first)
//			img = ImageIO.read(new ByteArrayInputStream(bos.toByteArray()));
//		return new Pair<BufferedImage, String>(img,pr.second);
		return null;
	}

	public Pair<BufferedImage, String> getRawRaster(int[] iXiY, Map<String, String[]> args) throws Exception
	{
		throw new UnsupportedOperationException();
	}

	public Pair<InputStream, String> getStreamRawRaster(int[] iXiY, Map<String, String[]> args) throws Exception
	{
		throw new UnsupportedOperationException();
	}

	public Pair<Boolean, String> getStreamRawRaster(int[] iXiY, OutputStream os, Map<String, String[]> args) throws Exception
	{
		throw new UnsupportedOperationException();
	}

	public int[] getObjIndexByIndexImage(int[] iXiY) throws Exception
	{
		throw new UnsupportedOperationException();
	}

	public String getInitScript(double[] dXdY, double[] szXszY, int[] nXnY, int lrindex, Point rCenter, Point rSize) throws Exception
	{
		throw new UnsupportedOperationException();
	}

	public double[] getScaleRange()
	{
		return new double[]{-1,-1};
	}
}
