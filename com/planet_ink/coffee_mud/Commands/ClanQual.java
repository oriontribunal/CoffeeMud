package com.planet_ink.coffee_mud.Commands;
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
   Copyright 2000-2006 Bo Zimmerman

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
public class ClanQual extends BaseClanner
{
	public ClanQual(){}

	private String[] access={getScr("ClanQual","cmd")};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		boolean skipChecks=mob.Name().equals(mob.getClanID());
		commands.setElementAt(getAccessWords()[0],0);

		StringBuffer msg=new StringBuffer("");
		if((mob.getClanID()==null)
		||(mob.getClanID().equalsIgnoreCase(""))
		||(CMLib.clans().getClan(mob.getClanID())==null))
		{
			msg.append(getScr("ClanQual","nomember"));
		}
		else
		{
			Clan C=CMLib.clans().getClan(mob.getClanID());
			if((!skipChecks)&&(!goForward(mob,C,commands,Clan.FUNC_CLANPREMISE,false)))
			{
				msg.append(getScr("ClanQual","nopos",C.typeName()));
			}
			else
			{
				try
				{
					String premise="?";
					while(premise.equals("?"))
					{
						if((skipChecks)&&(commands.size()>1))
							premise=CMParms.combine(commands,1);
						else
						if(mob.session()!=null)
							premise=mob.session().prompt("Describe your "+C.typeName()+"'s Qualification Code (?)\n\r: ","");

						if(premise.equals("?"))
							mob.tell(CMLib.masking().maskHelp("\n\r","disallow"));
						else
						if(premise.length()>0)
						{
							mob.tell(getScr("ClanQual","qualicationsfollow",CMLib.masking().maskDesc(premise)));
							if((mob.session()!=null)&&(mob.session().confirm("Is this correct (Y/n)?","Y")))
							{
								commands.addElement(premise);
								if(skipChecks||goForward(mob,C,commands,Clan.FUNC_CLANPREMISE,true))
								{
									C.setAcceptanceSettings(premise);
									C.update();
									clanAnnounce(mob,"The qualifications of "+C.typeName()+" "+C.clanID()+" have been changed.");
									return false;
								}
							}
							else
								premise="?";
						}
						else
							return false;
					}
				}
				catch(java.io.IOException e)
				{
				}
			}
		}
		mob.tell(msg.toString());
		return false;
	}
	
	public boolean canBeOrdered(){return false;}

	
}
