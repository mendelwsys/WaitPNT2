package ru.ts.toykernel.plugins.analitics.tst;

import ru.ts.toykernel.attrs.IAttrs;
import ru.ts.toykernel.attrs.def.DefAttrImpl;
import ru.ts.toykernel.storages.IBaseStorage;
import ru.ts.toykernel.plugins.analitics.AnalitObjAttrsFactory;

import java.util.Random;

/**
 * Тестовая фабрика аттрибутов для проверки роботоспосбности модуля аналитики
 * ru.ts.toykernel.plugins.analitics.tst.TestAttrsFactory
 */
public class TestAttrsFactory extends AnalitObjAttrsFactory
{
	private Random rnd=new Random();
	public IAttrs createLocaleByGisObjId(String objId, IBaseStorage storage, IAttrs boundAttrs) throws Exception
	{
		if (boundAttrs.get("TESTATTR")==null)
			boundAttrs.put("TESTATTR",new DefAttrImpl("TESTATTR", String.valueOf(100*rnd.nextDouble())));
		return boundAttrs;
	}
}