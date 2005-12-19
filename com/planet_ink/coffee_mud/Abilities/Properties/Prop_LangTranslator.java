package com.planet_ink.coffee_mud.Abilities.Properties;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;


import java.util.*;

/* 
   Copyright 2000-2005 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
public class Prop_LangTranslator extends Property
{
	public String ID() { return "Prop_LangTranslator"; }
	public String name(){return "Language Translator";}
	public int quality(){return BENEFICIAL_SELF;};
	protected int canAffectCode(){return CAN_MOBS|CAN_ITEMS|CAN_ROOMS;}
	protected MOB mob=null;
    protected DVector langs=new DVector(2);

	public String accountForYourself()
	{ return "Translates spoken language";	}

	public void setMiscText(String text)
	{
		super.setMiscText(text);
		Vector V=CMParms.parse(text);
		langs.clear();
		int lastpct=100;
		for(int v=0;v<V.size();v++)
		{
			String s=(String)V.elementAt(v);
			if(s.endsWith("%")) s=s.substring(0,s.length()-1);
			if(CMath.isNumber(s))
				lastpct=CMath.s_int(s);
			else
			{
				Ability A=CMClass.getAbility(s);
				if(A!=null) langs.addElement(s,new Integer(lastpct));
			}
		}
	}

	protected String getMsgFromAffect(String msg)
	{
		if(msg==null) return null;
		int start=msg.indexOf("'");
		int end=msg.lastIndexOf("'");
		if((start>0)&&(end>start))
			return msg.substring(start+1,end).trim();
		return null;
	}
	protected String subStitute(String affmsg, String msg)
	{
		if(affmsg==null) return null;
		int start=affmsg.indexOf("'");
		int end=affmsg.lastIndexOf("'");
		if((start>0)&&(end>start))
			return affmsg.substring(0,start+1)+msg+affmsg.substring(end);
		return affmsg;
	}
	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if(msg.tool() instanceof Ability)
		{
			if(text().length()>0)
			{
				int t=langs.indexOf(msg.tool().ID());
				if(t<0) return;
				Integer I=(Integer)langs.elementAt(t,2);
				if(CMLib.dice().rollPercentage()>I.intValue())
					return;
			}
			if(affected instanceof MOB)
				mob=(MOB)affected;
			else
			if((mob==null)||(!mob.Name().equals(affected.name())))
			{
				mob=CMClass.getMOB("StdMOB");
				mob.setName(affected.name());
			}
			if((msg.tool().ID().equals("Fighter_SmokeSignals"))
			&&(msg.sourceCode()==CMMsg.NO_EFFECT)
			&&(msg.targetCode()==CMMsg.NO_EFFECT)
			&&(msg.othersMessage()!=null))
			{
				if(!(affected instanceof MOB))
					mob.setLocation(CMLib.utensils().roomLocation(affected));
				CMLib.commands().postSay(mob,null,"The smoke signals seem to say '"+msg.othersMessage()+"'.",false,false);
			}
			else
			if(((msg.sourceMinor()==CMMsg.TYP_SPEAK)
			   ||(msg.sourceMinor()==CMMsg.TYP_TELL)
			   ||(CMath.bset(msg.sourceCode(),CMMsg.MASK_CHANNEL)))
			&&(msg.sourceMessage()!=null)
			&&(((Ability)msg.tool()).classificationCode()==Ability.LANGUAGE))
			{
				String str=getMsgFromAffect(msg.sourceMessage());
				if(str!=null)
				{
					if(!(affected instanceof MOB))
						mob.setLocation(CMLib.utensils().roomLocation(affected));
					if(CMath.bset(msg.sourceCode(),CMMsg.MASK_CHANNEL))
						msg.addTrailerMsg(CMClass.getMsg(mob,null,null,CMMsg.TYP_SPEAK,"<S-NAME> say(s) '"+msg.source().name()+" said \""+subStitute(msg.othersMessage(),str)+"\" in "+msg.tool().name()+"'"));
					else
					if(msg.amITarget(null)&&(msg.targetMessage()!=null))
						msg.addTrailerMsg(CMClass.getMsg(mob,null,null,CMMsg.TYP_SPEAK,"<S-NAME> say(s) '"+msg.source().name()+" said \""+subStitute(msg.targetMessage(),str)+"\" in "+msg.tool().name()+"'"));
					else
					if(msg.othersMessage()!=null)
						msg.addTrailerMsg(CMClass.getMsg(mob,msg.target(),null,CMMsg.TYP_SPEAK,"<S-NAME> say(s) '"+msg.source().name()+" said \""+subStitute(msg.othersMessage(),str)+"\" in "+msg.tool().name()+"'"));
				}
			}
		}
	}
}
