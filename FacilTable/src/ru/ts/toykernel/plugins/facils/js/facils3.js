/**
 * модуль отображения объекта который находится под мышью пользователя
 * @param mmap - карта нужна для того что бы позиционироваться по клику мыши на таблице
 * ( Во время инициализации необходимо произвести запрос и по приходу данных инсертить таблицу в элемент парент)
 */
function facils3(mmap, pelem, nWidth, nHeight,navigation,pictdir)
{
	this.cmd4call = null;


	this.FILLREQ = "FILLREQ";//Инициализировать таблицу

	this.hreq=true;
	this.startrow=0;
	this.nrows=10;

	this.OBJBYRID="OBJBYRID";//
	this.rowid=-1;

	this.getcmd = function()
	{
		return this.FILLREQ + "#"+this.OBJBYRID+"#";
	}

	this.getcurrentPos= function()
	{
		return [this.startrow,this.nrows];
	}

	this.getrcmd = function(cmd)
	{
		if (this.cmd4call!=null)
		{
			if (cmd == null)
				cmd = "";
			else if (cmd.length)
				cmd += "%20";
			return cmd + this.cmd4call;
		}
		return cmd;
	}


	this.getrqparams = function(pars)
	{

		var cmd4call = this.cmd4call;
		this.cmd4call = null;
		if (cmd4call!=null)
		{
			if (pars == null)
				pars = "";
			else if (pars.length)
				pars += "&";

			if (cmd4call == this.FILLREQ)
				return pars + 'HREQ='+this.hreq+'&SROW='+this.startrow+'&NROW='+this.nrows;
			else if (cmd4call == this.OBJBYRID)
				return pars + 'OBJBYRID='+this.rowid;
		}
		return pars;
	}

	this.needsubmit = function()
	{
		return (this.cmd4call!=null);
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
					pelem.appendChild(this.inittable('divtbl', calcText));
				}
				else
				{

					var tokinazer = new create_tokinizer();
					var tok_obj = tokinazer.createtokenobj(calcText, ';')

					tok_obj = tokinazer.gettoken(tok_obj);
					var ishead=eval(tok_obj.token)

					tok_obj = tokinazer.gettoken(tok_obj);
					var startpos = eval(tok_obj.token);//начальная позиция

					tok_obj = tokinazer.gettoken(tok_obj);
					var nrows = eval(tok_obj.token);//кол-во присланных кортежей

					tok_obj = tokinazer.gettoken(tok_obj);
					var totalrows = eval(tok_obj.token);//всего кол-во кортежей

					if (ishead) //Прислан табличный заголовок
					{
						tok_obj = tokinazer.gettoken(tok_obj);
						var nCntX = eval(tok_obj.token);

						this.divtbl.clearallTable();
						this.divtbl.initbyString(tok_obj.tail, ';', null, nCntX);
					}
					else//Заголовок не прислан просто заменим данные таблицы
						this.divtbl.addbyString(tok_obj.tail, ';', null,true);

					if (this.frm)
						this.frm.setValues(startpos,nrows,totalrows);
				}
			}
		}
	}

	this.initmod = function(stratrow,nrows,hreq)
	{
		if (stratrow!=null)
			this.startrow=stratrow;
		if (nrows!=null)
			this.nrows=nrows;
		if (hreq!=null)
			this.hreq=hreq;

		this.cmd4call = this.FILLREQ;

		return [stratrow,nrows];
	}

	this.timer_call = function()
	{
	}


	this.inittable = function(tblid, calcText)
	{
		var divtbl = createmtable3(tblid, nWidth, nHeight);

		var parmod=this;
		divtbl.registerfunction
				(
				function(rowid)
		{
			parmod.rowid=rowid;
			parmod.cmd4call=parmod.OBJBYRID;

		}
		);

		var tokinazer = new create_tokinizer();
		var tok_obj = tokinazer.createtokenobj(calcText, ';')

		tok_obj = tokinazer.gettoken(tok_obj);
		if (!eval(tok_obj.token))
		{
			alert('Error: header not transmited by server')
			return null;
		}

		tok_obj = tokinazer.gettoken(tok_obj);
		var startpos = eval(tok_obj.token);//начальная позиция

		tok_obj = tokinazer.gettoken(tok_obj);
		var nrows = eval(tok_obj.token);//кол-во присланных кортежей

		tok_obj = tokinazer.gettoken(tok_obj);
		var totalrows = eval(tok_obj.token);//всего кол-во кортежей

		tok_obj = tokinazer.gettoken(tok_obj);
		var nCntX = eval(tok_obj.token);
		
		divtbl.initbyString(tok_obj.tail, ';', null, nCntX);

		this.divtbl = divtbl;

		if (navigation)
		{
		 	var rdiv=document.createElement('div');
			this.frm=createform(pictdir,startpos,nrows,totalrows,this);
			rdiv.appendChild(this.frm);
			rdiv.appendChild(divtbl);
			return rdiv;
		}

		return divtbl;
	}
}


