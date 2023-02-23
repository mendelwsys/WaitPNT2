//++BUILD модуля обработки аналитики
var navigpictFolder = "mtable/pict";
var stbl = document.getElementById('site'); //TODO Пока не инициализируем таблицу активов, поскольку 1-ое долго , надо обеспечить скролинг
var parsel = stbl.rows[1].cells[1];
var fs3 = new facils3(null, parsel, screen.width / 2, screen.height, true, navigpictFolder);
rmanager.addmodule(fs3);
//	fs3.initmod(); //Есть мнение что приход строки инициализации таблицы с сервера блокирует инициализацию карты, поскольку эту инициализацию перебивает
//--BUILD
