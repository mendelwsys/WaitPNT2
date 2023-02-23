package ru.ts.apps.rbuilders.app0.kernel.rules;

import ru.ts.toykernel.drawcomp.rules.def.stream.InscriptionRule;
import ru.ts.toykernel.drawcomp.ILayer;
import ru.ts.toykernel.drawcomp.ITextParamPainter;
import ru.ts.toykernel.drawcomp.painters.def.DefInscriptPainter;
import ru.ts.toykernel.geom.IBaseGisObject;
import ru.ts.toykernel.converters.ILinearConverter;
import ru.ts.apps.rbuilders.app0.kernel.painters.DefInscriptPainter2;

import java.awt.*;

/**
 * создан для предотвращения преждевременноого сброса пула записи
 * Используется для генерации растра из векторной информации
 * (Выдает в качестве кульмана DefInscriptPainter2, который имеет буфер без сброса,
 * что дает возможность прорисовать всю подложку при генерации)
 *
 * TODO Есть несколько тонких моментов которые надо проверить
 *
 * 1. При измении масштаба при генерации подложки надо все таки сбрасывать пул или cacheTextParamPainter - TODO не сбрасывает
 * 2. При включенном генераторе, необходимо загружать пул при перезапуске сервера, поскольку при генерации
 * мы рискуем на стыках растра сгенерировать другую надпись, поскольку не имеем информации
 * о сгенерированной надписи на соседнем растре перед остановкой сервер. TODO не загружает
 * 3. !!!Генератор должен разделяться всеми клиентами !!!!, что бы не было конфликтов по генерации растра
 *
 * ru.ts.apps.rbuilders.app0.kernel.rules.IncriptionRule2
 */
public class IncriptionRule2 extends InscriptionRule
{
	protected void pointPainter(Paint paintfill, Integer linecolor, Stroke stroke, Integer radPnt, ILayer lr, IBaseGisObject obj, Integer composite, Image pointImg, Point central) throws Exception
	{

		if (cacheTextParamPainter == null || !(cacheTextParamPainter instanceof DefInscriptPainter))
			cacheTextParamPainter=new DefInscriptPainter2();
		super.pointPainter(paintfill,linecolor, stroke,radPnt,lr,obj, composite, null, central);
	}

	public boolean isVisibleLayer(ILayer lr, ILinearConverter converter)
	{
		ITextParamPainter l_cacheTextParamPainter = cacheTextParamPainter;
		boolean b = super.isVisibleLayer(lr, converter);
		cacheTextParamPainter=l_cacheTextParamPainter;
		return b;
	}
}
