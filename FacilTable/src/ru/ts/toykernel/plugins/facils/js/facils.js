

/**
 * модуль отображения объекта который находится под мышью пользователя
 * @param mmap - карта нужна для того что бы позиционироваться по клику мыши на таблице
 * ( Во время инициализации необходимо произвести запрос и по приходу данных инсертить таблицу в элемент парент)
 */
function facils(mmap,pelem)
{
	this.callsubmit=false;


	this.FILLREQ = "FILLREQ";

	this.getcmd = function()
	{
		return this.FILLREQ+"#";
	}

	this.getrcmd = function(cmd)
	{
		if (this.callsubmit)
		{
			if (cmd == null)
				cmd = "";
			else if (cmd.length)
				cmd += "%20";

			this.callsubmit=false;
			return cmd + this.FILLREQ;
		}
		return cmd;
	}

	this.getrqparams = function(pars)
	{
		return pars;
	}

	this.needsubmit = function()
	{
		return this.callsubmit;
	}

	this.callcmd = function(cmd, calcText)
	{
		if (cmd!=null && cmd.toUpperCase() == this.FILLREQ)
		{
			var tbl=this.inittable('divtbl',calcText);
			tbl.frame="box";
			tbl.cellpadding="5";
			tbl.style.position='relative';
			tbl.style.borderSpacing=0;
			tbl.style.fontSize='small';
			pelem.appendChild(tbl);
		}
	}

	this.initmod = function()
	{
		this.callsubmit=true;
	}

	this.timer_call = function()
	{
	}

	this.inittable= function(tblid,calcText)
	{
		var tbl=new mtable(tblid);

		var tokinazer = new create_tokinizer();
		var tok_obj=tokinazer.createtokenobj(calcText,';')
		tok_obj = tokinazer.gettoken(tok_obj);
		var nCntX = eval(tok_obj.token);

		tbl.initbyString(tok_obj.tail,';',null,nCntX);
		return tbl;
	}

	this.ondblclick = function (e)
	{
	}
}


