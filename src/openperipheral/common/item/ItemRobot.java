package openperipheral.common.item;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import openperipheral.OpenPeripheral;
import openperipheral.codechicken.core.vec.Rotation;
import openperipheral.codechicken.core.vec.Vector3;
import openperipheral.common.config.ConfigSettings;
import openperipheral.common.entity.EntityRobot;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;

public class ItemRobot extends Item {

	public ItemRobot() {
		super(ConfigSettings.robotItemId);
		setMaxDamage(0);
		setMaxStackSize(1);
		setHasSubtypes(true);
		setCreativeTab(OpenPeripheral.tabOpenPeripheral);
		setUnlocalizedName("openperipheral.robot");
	}
	
	@Override
	public void registerIcons(IconRegister register) {
		itemIcon = register.registerIcon("openperipheral:robot");
	}
	
	/**
	 * Try to spawn a robot. If the controller he's linked up to is invalid
	 * send a chat message to the player letting them know
	 */
	@Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
		if (!world.isRemote) { 
			
			double radYaw = -Math.toRadians(player.rotationYawHead);
			
			Vector3 pos = new Vector3(0, 0, 2)
				.apply(new Rotation(radYaw, 0, 1, 0))
				.add(Vector3.fromEntity(player));
			
			int blockX = (int)pos.x;
			int blockY = (int)pos.y;
			int blockZ = (int)pos.z;
			
			if (world.isAirBlock(blockX, blockY, blockZ) &&
				world.isAirBlock(blockX, blockY+1, blockZ) &&
				world.isAirBlock(blockX, blockY+2, blockZ)) {

		    	EntityRobot robot = new EntityRobot(world);
		    	if (robot.setUpgradesFromStack(stack)) {
			    	robot.setPositionAndRotation(pos.x, pos.y + 0.5, pos.z, 0, 0);
			    	world.spawnEntityInWorld(robot);
			    	robot.playSound("openperipheral.robotready", 1F, 1F);
			    	stack.stackSize = 0;
			    	//player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
		    	} else {
		    		// make sure he's gone
		    		robot.setDead();
		    		// tell the player
		    		player.sendChatToPlayer("Unable to spawn robot. Are you sure he's linked up to an active controller?");
		    	}
			}
			
    	}
		return stack;
    }
	
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean par4) {
		if (stack.hasTagCompound()) {
			NBTTagCompound tag = stack.getTagCompound();
			if (tag.hasKey("robotId")) {
				list.add("Robot ID: " + tag.getInteger("robotId"));
				if (tag.hasKey("upgrades")) {
					NBTBase upgradesTag = tag.getTag("upgrades");
					if (upgradesTag instanceof NBTTagCompound) {
						Collection upgradesTags = ((NBTTagCompound)tag.getTag("upgrades")).getTags();
						for (Iterator iterator = upgradesTags.iterator(); iterator.hasNext();) {
							Object next = iterator.next();
							if (next instanceof NBTTagCompound) {
								NBTTagCompound upgradeTag = (NBTTagCompound)next;
								list.add("Upgrade: " + upgradeTag.getName());
							}
					    }
					}
				}
			}
		}
	}

}
