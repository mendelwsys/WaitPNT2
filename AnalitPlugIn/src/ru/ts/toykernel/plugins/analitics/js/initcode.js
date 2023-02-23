//++BUILD модуля обработки аналитики
var anal = new analitcmd(mmap);
rmanager.addmodule(anal);

var l_clmna = null;
if (menu2menusstruct == null)
	menu2menusstruct = new Array();
if ((l_clmna = menu2menusstruct['Сервис']) == null)
{
	l_clmna = new Array();
	menu2menusstruct['Сервис'] = l_clmna;
}
l_clmna.push(new menustruct('Аналитика', function()
{
	anal.setsubmit()
}));
//--BUILD
