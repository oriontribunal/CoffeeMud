package com.planet_ink.coffee_mud.Abilities.Common;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;
import java.io.File;

public class Carpentry extends CommonSkill
{
	private static final int RCP_FINALNAME=0;
	private static final int RCP_LEVEL=1;
	private static final int RCP_TICKS=2;
	private static final int RCP_WOOD=3;
	private static final int RCP_VALUE=4;
	private static final int RCP_CLASSTYPE=5;
	private static final int RCP_MISCTYPE=6;
	private static final int RCP_CAPACITY=7;
	private static final int RCP_ARMORDMG=8;
	
	
	private Item building=null;
	private Item key=null;
	private boolean mending=false;
	private boolean messedUp=false;
	public Carpentry()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Carpentry";

		miscText="";
		triggerStrings.addElement("CARVE");
		triggerStrings.addElement("CARPENTRY");
		quality=Ability.INDIFFERENT;

		recoverEnvStats();
		//CMAble.addCharAbilityMapping("All",1,ID(),false);
	}
	
	public Environmental newInstance()
	{
		return new Carpentry();
	}
	
	public boolean tick(int tickID)
	{
		MOB mob=(MOB)affected;
		if((affected!=null)&&(affected instanceof MOB)&&(tickID==Host.MOB_TICK))
		{
			if(building==null)
				unInvoke();
		}
		return super.tick(tickID);
	}
	
	private static synchronized Vector loadRecipes()
	{
		Vector V=(Vector)Resources.getResource("CARPENTRY RECIPES");
		if(V==null)
		{
			StringBuffer str=Resources.getFile("resources"+File.separatorChar+"carpentry.txt");
			V=loadList(str);
			if(V.size()==0)
				Log.errOut("Carpentry","Recipes not found!");
			Resources.submitResource("CARPENTRY RECIPES",V);
		}
		return V;
	}
	
	public void unInvoke()
	{
		if((affected!=null)&&(affected instanceof MOB))
		{
			MOB mob=(MOB)affected;
			if(building!=null)
			{
				if(messedUp)
				{
					if(mending)
						mob.tell("You completely mess up mending "+building.name()+".");
					else
						mob.tell("You completely mess up carving "+building.name()+".");
				}
				else
				{
					if(mending)
						building.setUsesRemaining(100);
					else
					{
						mob.location().addItem(building);
						if(key!=null)
						{
							mob.location().addItem(key);
							key.setContainer(building);
						}
					}
				}
			}
			building=null;
			key=null;
			mending=false;
		}
		super.unInvoke();
	}
	
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(commands.size()==0)
		{
			mob.tell("Carve what? Enter \"carve list\" for a list, or \"carve mend <item>\".");
			return false;
		}
		Vector recipes=loadRecipes();
		String str=(String)commands.elementAt(0);
		String startStr=null;
		int completion=4;
		if(str.equalsIgnoreCase("list"))
		{
			StringBuffer buf=new StringBuffer(Util.padRight("Item",20)+" Wood required\n\r");
			for(int r=0;r<recipes.size();r++)
			{
				Vector V=(Vector)recipes.elementAt(r);
				if(V.size()>0)
				{
					String item=replacePercent((String)V.elementAt(0),"");
					int level=Util.s_int((String)V.elementAt(1));
					int wood=Util.s_int((String)V.elementAt(3));
					if(level<=mob.envStats().level())
						buf.append(Util.padRight(item,20)+" "+wood+"\n\r");
				}
			}
			mob.tell(str.toString());
			return true;
		}
		if(str.equalsIgnoreCase("mend"))
		{
			building=null;
			mending=false;
			key=null;
			messedUp=false;
			Vector newCommands=Util.parse(Util.combine(commands,1));
			building=getTarget(mob,mob.location(),givenTarget,newCommands,Item.WORN_REQ_UNWORNONLY);
			if(building==null) return false;
			if((building.material()&EnvResource.MATERIAL_MASK)!=EnvResource.MATERIAL_WOODEN)
			{
				mob.tell("That's not made of wood.  You don't know how to mend it.");
				return false;
			}
			if(!building.subjectToWearAndTear())
			{
				mob.tell("You can't mend "+building.name()+".");
				return false;
			}
			mending=true;
			if(!super.invoke(mob,commands,givenTarget,auto))
				return false;
			startStr="You start mending "+building.name()+".";
			displayText="You are mending "+building.name();
			verb="mending "+building.name();
		}
		else
		{
			building=null;
			mending=false;
			key=null;
			messedUp=false;
			String recipeName=Util.combine(commands,0);
			Vector foundRecipe=null;
			for(int r=0;r<recipes.size();r++)
			{
				Vector V=(Vector)recipes.elementAt(r);
				if(V.size()>0)
				{
					String item=(String)V.elementAt(RCP_FINALNAME);
					int level=Util.s_int((String)V.elementAt(RCP_LEVEL));
					if((level<=mob.envStats().level())
					&&(replacePercent(item,"").equalsIgnoreCase(recipeName)))
					{
						foundRecipe=V;
						break;
					}
				}
			}
			if(foundRecipe==null)
			{
				mob.tell("You don't know how to carve a '"+recipeName+"'.  Try \"carve list\" for a list.");
				return false;
			}
			int woodRequired=Util.s_int((String)foundRecipe.elementAt(RCP_WOOD));
			Item firstWood=null;
			int foundWood=0;
			for(int i=0;i<mob.location().numItems();i++)
			{
				Item I=mob.location().fetchItem(i);
				if((I instanceof EnvResource)
				&&((I.material()&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_WOODEN)
				&&(I.container()==null))
				{
					if(firstWood==null)firstWood=I;
					if(firstWood.material()==I.material())
						foundWood++;
				}
			}
			if(foundWood==0)
			{
				mob.tell("There is no wood here to make anything from!  You might need to put it down first.");
				return false;
			}
			if(foundWood<woodRequired)
			{
				mob.tell("You need "+woodRequired+" pounds of "+EnvResource.RESOURCE_DESCS[(firstWood.material()&EnvResource.RESOURCE_MASK)].toLowerCase()+" to construct a "+recipeName.toLowerCase()+".  There is not enough here.  Are you sure you set it all on the ground first?");
				return false;
			}
			if(!super.invoke(mob,commands,givenTarget,auto))
				return false;
			for(int i=mob.location().numItems()-1;i>=0;i--)
			{
				Item I=mob.location().fetchItem(i);
				if((I instanceof EnvResource)
				&&((I.material()&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_WOODEN)
				&&(I.container()==null)
				&&(I.material()==firstWood.material()))
					I.destroyThis();
			}
			building=CMClass.getItem((String)foundRecipe.elementAt(RCP_CLASSTYPE));
			if(building==null)
			{
				mob.tell("There's no such thing as a "+foundRecipe.elementAt(RCP_CLASSTYPE)+"!!!");
				return false;
			}
			startStr="You start carving "+building.name()+".";
			displayText="You are carving "+building.name();
			verb="carving "+building.name();
			completion=Util.s_int((String)foundRecipe.elementAt(this.RCP_TICKS))-(mob.envStats().level()*2);
			String itemName=replacePercent((String)foundRecipe.elementAt(RCP_FINALNAME),EnvResource.RESOURCE_DESCS[(firstWood.material()&EnvResource.RESOURCE_MASK)].toLowerCase());
			if(new String("aeiou").indexOf(itemName.charAt(0))>=0)
				itemName="an "+itemName;
			else
				itemName="a "+itemName;
			building.setName(itemName);
			building.setDisplayText(itemName+" is here");
			building.setDescription(itemName);
			building.baseEnvStats().setWeight(woodRequired);
			building.setBaseValue(Util.s_int((String)foundRecipe.elementAt(RCP_VALUE)));
			building.setMaterial(firstWood.material());
			building.baseEnvStats().setLevel(Util.s_int((String)foundRecipe.elementAt(RCP_LEVEL)));
			String misctype=(String)foundRecipe.elementAt(this.RCP_MISCTYPE);
			int capacity=Util.s_int((String)foundRecipe.elementAt(RCP_CAPACITY));
			int armordmg=Util.s_int((String)foundRecipe.elementAt(RCP_ARMORDMG));
			key=null;
			if(building instanceof Container)
			{
				((Container)building).setCapacity(capacity);
				if(misctype.equalsIgnoreCase("LID"))
					((Container)building).setLidsNLocks(true,false,false,false);
				else
				if(misctype.equalsIgnoreCase("LOCK"))
				{
					((Container)building).setLidsNLocks(true,false,true,false);
					((Container)building).setKeyName(new Double(Math.random()).toString());
					key=CMClass.getItem("GenKey");
					((Key)key).setKey(((Container)building).keyName());
				}
			}
			if(building instanceof Rideable)
			{
				if(misctype.equalsIgnoreCase("CHAIR"))
					((Rideable)building).setRideBasis(Rideable.RIDEABLE_SIT);
				else
				if(misctype.equalsIgnoreCase("TABLE"))
					((Rideable)building).setRideBasis(Rideable.RIDEABLE_TABLE);
				else
				if(misctype.equalsIgnoreCase("BED"))
					((Rideable)building).setRideBasis(Rideable.RIDEABLE_SLEEP);
			}
			if(building instanceof Weapon)
			{
				((Weapon)building).setWeaponType(Weapon.TYPE_BASHING);
				((Weapon)building).setWeaponClassification(Weapon.CLASS_BLUNT);
				for(int cl=0;cl<Weapon.classifictionDescription.length;cl++)
				{
					if(misctype.equalsIgnoreCase(Weapon.classifictionDescription[cl]))
						((Weapon)building).setWeaponClassification(cl);
				}
				building.baseEnvStats().setDamage(armordmg);
				((Weapon)building).setRawProperLocationBitmap(Item.WIELD|Item.HELD);
				((Weapon)building).setRawLogicalAnd((capacity>1));
			}
			if(building instanceof Armor)
			{
				((Armor)building).baseEnvStats().setArmor(armordmg);
				((Armor)building).setRawProperLocationBitmap(0);
				for(int wo=0;wo<Item.wornLocation.length;wo++)
				{
					String WO=Item.wornLocation[wo];
					if(misctype.equalsIgnoreCase(WO))
					{
						((Armor)building).setRawProperLocationBitmap(wo);
						((Armor)building).setRawLogicalAnd(false);
					}
					else
					if((misctype.toUpperCase().indexOf(WO+"||")>=0)
					||(misctype.toUpperCase().endsWith("||"+WO)))
					{
						((Armor)building).setRawProperLocationBitmap(building.rawProperLocationBitmap()|wo);
						((Armor)building).setRawLogicalAnd(false);
					}
					else
					if((misctype.toUpperCase().indexOf(WO+"&&")>=0)
					||(misctype.toUpperCase().endsWith("&&"+WO)))
					{
						((Armor)building).setRawProperLocationBitmap(building.rawProperLocationBitmap()|wo);
						((Armor)building).setRawLogicalAnd(true);
					}
				}
			}
			building.recoverEnvStats();
			building.text();
			building.recoverEnvStats();
		}
		
		
		messedUp=profficiencyCheck(0,auto);
		if(completion<4) completion=4;
		FullMsg msg=new FullMsg(mob,null,Affect.MSG_NOISYMOVEMENT,startStr);
		if(mob.location().okAffect(msg))
		{
			mob.location().send(mob,msg);
			beneficialAffect(mob,mob,completion);
		}
		return true;
	}
}
