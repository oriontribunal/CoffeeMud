package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer_IceHealing extends Prayer
{
	public String ID() { return "Prayer_IceHealing"; }
	public String name(){ return "Ice Healing";}
	public String displayText(){ return "(Ice Healing)";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int quality(){ return BENEFICIAL_SELF;}
	public long flags(){return Ability.FLAG_HOLY|Ability.FLAG_UNHOLY;}
	public Environmental newInstance(){	return new Prayer_IceHealing();}


	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();

		if(canBeUninvoked())
			mob.tell("The aura of ice healing around you fades.");
	}

	public boolean okAffect(Environmental myHost, Affect affect)
	{
		if(!super.okAffect(myHost,affect))
			return false;

		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;
		if((affect.amITarget(mob))
		   &&((affect.sourceMinor()==affect.TYP_COLD)
			   ||(affect.sourceMinor()==affect.TYP_WATER))
		   &&(Util.bset(affect.targetCode(),Affect.MASK_HURT)))
		{
			int recovery=(int)Math.round(Util.div((affect.targetCode()-Affect.MASK_HURT),2.0));
			mob.location().show(mob,null,Affect.MSG_OK_VISUAL,"The icey attack heals <S-NAME> "+recovery+" points.");
			mob.curState().adjHitPoints(recovery,mob.maxState());
			return false;
		}
		return true;
	}
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;
		if(target.fetchAffect(ID())!=null)
		{
			mob.tell("You already healed by ice.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> "+prayWord(mob)+" for icey healing.^?");
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				mob.location().show(target,null,Affect.MSG_OK_VISUAL,"An aura surrounds <S-NAME>.");
				beneficialAffect(mob,target,0);
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> "+prayWord(mob)+" for icey healing, but <S-HIS-HER> plea is not answered.");


		// return whether it worked
		return success;
	}
}
