/**
 * модуль отображения объекта который находится под мышью пользователя
 * @param mmap - карта нужна для того что бы позиционироваться по клику мыши на таблице
 * ( Во время инициализации необходимо произвести запрос и по приходу данных инсертить таблицу в элемент парент)
 */
function facils2(mmap, pelem, nWidth, nHeight)
{
	this.callsubmit = false;
	this.startrow=-1;

	this.FILLREQ = "FILLREQ";
	

	this.getcmd = function()
	{
		return this.FILLREQ + "#";
	}

	this.getrcmd = function(cmd)
	{
		if (this.callsubmit)
		{
			if (cmd == null)
				cmd = "";
			else if (cmd.length)
				cmd += "%20";
			return cmd + this.FILLREQ;
		}
		return cmd;
	}

	this.getrqparams = function(pars)
	{

		var issubmit = this.callsubmit;
		this.callsubmit = false;
		if (issubmit)
		{
			if (pars == null)
				pars = "";
			else if (pars.length)
				pars += "&";
			var rv = pars + 'SROW='+this.startrow+'&NROW=10';
			this.req = null;
			return rv;
		}
		return pars;
	}

	this.needsubmit = function()
	{
		return this.callsubmit;
	}

	this.gettbl = function()
	{
		return this.divtbl;
	}

	this.callcmd = function(cmd, calcText)
	{
		if (cmd != null)
		{
			if (cmd.toUpperCase() == this.FILLREQ)
			{
				if (!this.divtbl)
				{
					this.divtbl = this.inittable('divtbl', calcText);
					pelem.appendChild(this.divtbl);
				}
				else
					this.add2table(calcText);
			}
		}
	}

	this.initmod = function()
	{
		this.callsubmit = true;
	}

	this.timer_call = function()
	{
	}


	this.add2table = function(calcText)
	{
		var tokinazer = new create_tokinizer();
		var tok_obj = tokinazer.createtokenobj(calcText, ';')
		tok_obj = tokinazer.gettoken(tok_obj);
		var needupdate = tok_obj.token;

		this.divtbl.addbyString(tok_obj.tail, ';', null);

		if (this.divtbl.getHeadTable())
			this.startrow=this.divtbl.getDataTable().rows.length-1;
		else
			this.startrow=this.divtbl.getDataTable().rows.length;
		this.callsubmit = (needupdate.toLowerCase('true')=='true');
	}

	this.inittable = function(tblid, calcText)
	{
		var divtbl = new mtable2(tblid, nWidth, nHeight);

		var tokinazer = new create_tokinizer();
		var tok_obj = tokinazer.createtokenobj(calcText, ';')

		tok_obj = tokinazer.gettoken(tok_obj);
		var needupdate = tok_obj.token;


		tok_obj = tokinazer.gettoken(tok_obj);
		var nCntX = eval(tok_obj.token);
		
		divtbl.initbyString(tok_obj.tail, ';', null, nCntX);
		this.divtbl = divtbl;

		if (this.divtbl.getHeadTable())
			this.startrow=this.divtbl.getDataTable().rows.length-1;
		else
			this.startrow=this.divtbl.getDataTable().rows.length;

		this.callsubmit = (needupdate.toLowerCase('true')=='true');
		return divtbl;
	}
}


