package com.planet_ink.coffee_mud.Abilities.Prayers;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer extends StdAbility
{
	public String ID() { return "Prayer"; }
	public String name(){ return "a Prayer";}
	public String displayText(){ return "";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){ return INDIFFERENT;}
	private static final String[] triggerStrings = {"PRAY","PR"};
	public String[] triggerStrings(){return triggerStrings;}
	public int classificationCode(){return Ability.PRAYER;}

	protected int affectType(boolean auto){
		int affectType=Affect.MSG_CAST_VERBAL_SPELL;
		if(quality()==Ability.MALICIOUS)
			affectType=Affect.MSG_CAST_ATTACK_VERBAL_SPELL;
		if(auto) affectType=affectType|Affect.MASK_GENERAL;
		return affectType;
	}
	public Environmental newInstance(){	return new Prayer();}

	protected String prayWord(MOB mob)
	{
		if(mob.getMyDeity()!=null)
			return "pray(s) to "+mob.getMyDeity().name();
		else
			return "pray(s)";
	}

	protected String prayForWord(MOB mob)
	{
		if(mob.getMyDeity()!=null)
			return "pray(s) for "+mob.getMyDeity().name();
		else
			return "pray(s)";
	}

	protected String inTheNameOf(MOB mob)
	{
		if(mob.getMyDeity()!=null)
			return " in the name of "+mob.getMyDeity().name();
		return "";
	}
	protected String hisHerDiety(MOB mob)
	{
		if(mob.getMyDeity()!=null)
			return mob.getMyDeity().name();
		return "<S-HIS-HER> god";
	}
	protected String ofDiety(MOB mob)
	{
		if(mob.getMyDeity()!=null)
			return " of "+mob.getMyDeity().name();
		return "";
	}
	protected String prayingWord(MOB mob)
	{
		if(mob.getMyDeity()!=null)
			return "praying to "+mob.getMyDeity().name();
		else
			return "praying";
	}

	public boolean appropriateToMyAlignment(int alignment)
	{
		int qual=0;
		if(Util.bset(flags(),Ability.FLAG_HOLY))
		{
			if(!Util.bset(flags(),Ability.FLAG_UNHOLY))
				qual=1;
		}
		else
		if(Util.bset(flags(),Ability.FLAG_UNHOLY))
			qual=2;
		switch(qual)
		{
		case 0:
			if((alignment>350)&&(alignment<650)) 
				return true;
			break;
		case 1:
			if(alignment>650) return true;
			break;
		case 2:
			if(alignment<350) return true;
			break;
		}
		return false;
	}
	public void helpProfficiency(MOB mob)
	{

		Ability A=(Ability)mob.fetchAbility(this.ID());
		if(A==null) return;
		if(A.appropriateToMyAlignment(mob.getAlignment()))
		{
			super.helpProfficiency(mob);
			return;
		}
		return;
	}
}
