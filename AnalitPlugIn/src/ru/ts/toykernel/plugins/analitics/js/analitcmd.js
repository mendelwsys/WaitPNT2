/**
 * модуль вызова аналитики
  */
function analitcmd(mmap)
{
	this.callsubmit=false;

	this.ANALIT = "ANALIT";

	this.setsubmit=function()
	{
		this.callsubmit=true;
	}

	this.getcmd = function()
	{
		return this.ANALIT;
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
			return cmd + this.ANALIT;
		}
		else
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

	this.callcmd = function(cmd)
	{
		if (cmd == this.ANALIT)
		{
			mmap.update();
			mmap.draw_map();
		}
	}

	this.initmod = function()
	{
	}

	this.timer_call = function()
	{
	}

	this.ondblclick = function ()
	{
	}
}
