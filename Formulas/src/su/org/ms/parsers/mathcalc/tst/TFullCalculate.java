package su.org.ms.parsers.mathcalc.tst;

import su.org.ms.parsers.mathcalc.Parser;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: 12.08.2007
 * Time: 13:59:05
 */
public class TFullCalculate
{
    public static void main(String[] args)
    {

//		String[] main_tail="a+b?c:d".split("[?]");
//		String[] tail=main_tail[1].split(":");
        try
        {
//Тестовая имплементация интерфейса
            IGetFormulaByNameTest expretiontest = new IGetFormulaByNameTest();
//Инстанцируем парсер только ОДИН РАЗ на все приложения (args сейчас не используются)
            Parser pr = Parser.createParser(args);
//Взять тестовые имена
            String[] tstnm=expretiontest.getTestNames();
            for (String parname : tstnm)
            { //Для каждого тестового имени вычислить формулу
                String formula = expretiontest.getFormulaByName(parname); //Получить формулу по имени (TODO Этот метод должен быть заимплементин для получения формул из БД)
                double calcvalue = pr.calculate(formula, expretiontest);//Вычислить значение формулы

                double templvalue = expretiontest.getTestValueByName(parname);//Взять предвычисленное значение тестовой формулы
                //Сравнить вычисленное и предвычисленное значение, разница должна быть 0.0 кроме выражений где есть random()
                System.out.println(formula + " diff = " + (calcvalue - templvalue) + " calcvalue:" + calcvalue);
            }
			System.out.println("");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
	}
	
}
