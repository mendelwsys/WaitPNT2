package ru.ts.toykernel.plugins.analitics;

import ru.ts.factory.IInitAble;
import ru.ts.gisutils.algs.common.IMCoordinate;
import ru.ts.toykernel.geom.IBaseGisObject;
import ru.ts.toykernel.converters.ILinearConverter;
import ru.ts.toykernel.attrs.AObjAttrsFactory;

import java.util.Iterator;

/**
 * Поставщик рельефа
 */
public interface IReliefProvider extends IInitAble
{
	/**
	 * @return вернуть формулу вычисления параметра отображения над аттрибутами объекта
	 */
	public String getAttrFormula();

	/**
	 * @return вернуть формулу вычисления параметра отображения над аттрибутами объекта
	 */
	public void setAttrFormula(String formula);

	/**
	 * собственно вернуть рельеф над объектами
	 * @param baseObjects - объекты для построения рельефа
	 * @param converter - конвертер координат из координат проекта в координаты прорисовки
	 * @return - множество точек рельефа
	 * @throws Exception - ошибка построения рельефа
	 */
	public IMCoordinate[] getRelief(Iterator<IBaseGisObject> baseObjects, ILinearConverter converter) throws Exception;

	/**
	 * @return получить фабрику аттрибутов
	 */
	AObjAttrsFactory getObjFactory();
}
