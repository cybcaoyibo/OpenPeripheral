package openperipheral.common.entity;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import openperipheral.OpenPeripheral;
import openperipheral.api.IRobot;
import openperipheral.api.IRobotMethod;
import openperipheral.api.IRobotUpgradeInstance;
import openperipheral.api.IRobotUpgradeProvider;
import openperipheral.api.RobotUpgradeManager;
import openperipheral.common.config.ConfigSettings;
import openperipheral.common.core.OPInventory;
import openperipheral.common.peripheral.SensorPeripheral;
import openperipheral.common.tileentity.TileEntityRobot;
import openperipheral.common.util.BlockUtils;
import openperipheral.common.util.InventoryUtils;

public class EntityRobot extends EntityCreature implements IRobot {

	/**
	 * the location and id of the controller
	 */
	private int controllerX = 0;
	private int controllerY = 0;
	private int controllerZ = 0;
	private String controllerUuid = "[none]";
	private boolean linkedToController = false;
	private int robotId = 0;

	/**
	 * The weapon spin used for rendering clientside
	 */
	private float weaponSpin = 0.f;
	
	private float weaponSpinSpeed = 0.5f;
	
	private float fuelLevel = 0;
	
	/**
	 * The main inventory
	 */
	protected OPInventory inventory = new OPInventory("robot", false, 16);

	/**
	 * map of method name to upgrade instance, so we know what object handles
	 * the called method
	 */
	private HashMap<String, IRobotUpgradeInstance> upgradeMethodMap;
	
	/**
	 * A map of currently installed instanceIds to module instance objects
	 */
	private HashMap<String, IRobotUpgradeInstance> upgradeInstances;
	
	public EntityRobot(World par1World) {
		super(par1World);
		upgradeMethodMap = new HashMap<String, IRobotUpgradeInstance>();
		upgradeInstances = new HashMap<String, IRobotUpgradeInstance>();
		this.health = this.getMaxHealth();
		this.setSize(1F, 3F);
		this.moveSpeed = 0.22F;
		this.getNavigator().setAvoidsWater(true);
		this.texture = String.format("%s/models/robot.png", ConfigSettings.TEXTURES_PATH);
	}

	@Override
	public float getMoveSpeed() {
		return moveSpeed;
	}

	@Override
	public void setMoveSpeed(float speed) {
		moveSpeed = speed;
	}
	
	@Override
	public float getWeaponSpinSpeed() {
		return weaponSpinSpeed;
	}

	@Override
	public void setWeaponSpinSpeed(float speed) {
		weaponSpinSpeed = speed;
	}
	
	@Override
	public void modifyWeaponSpinSpeed(float speed) {
		weaponSpinSpeed += speed;
	}
	
	/**
	 * Get the current fuel level
	 * @return the fuel level
	 */
	@Override
	public float getFuelLevel() {
		return fuelLevel;
	}

	/**
	 * set the fuel level
	 * @param fuel
	 */
	@Override
	public void setFuelLevel(float fuel) {
		fuelLevel = fuel;
	}
	
	/**
	 * add or remove fuel
	 * @param fuel
	 */
	@Override
	public void modifyFuelLevel(float fuel) {
		fuelLevel += fuel;
	}
	
	/**
	 * Get the inventory
	 */
	@Override
	public IInventory getInventory() {
		return inventory;
	}
	
	/**
	 * Path finding range
	 */
	@Override
	protected int func_96121_ay() {
		return 32;
	}

	/**
	 * doesn't appear to work
	 */
	@Override
	public boolean canBePushed() {
		return false;
	}

	//TODO: move to inventory module
	public void suckUp() {
		List<EntityItem> entities = worldObj.getEntitiesWithinAABB(EntityItem.class,
				AxisAlignedBB.getAABBPool().getAABB(posX - 2, posY - 2, posZ - 2, posX + 3, posY + 3, posZ + 3));

		for (EntityItem entity : entities) {

			if (entity.isDead) {
				continue;
			}

			ItemStack stack = entity.getEntityItem();

			if (stack != null) {

				InventoryUtils.insertItemIntoInventory(inventory, stack);
				if (stack.stackSize == 0) {
					entity.setDead();
				}
			}
		}
	}

	//TODO: move to inventory module
	public void dropAll() {
		for (int i = 0; i < inventory.getSizeInventory(); i++) {
			ItemStack stack = inventory.getStackInSlot(i);
			if (stack != null) {
				EntityItem entityitem = new EntityItem(worldObj, posX, posY, posZ, stack);
				Vec3 lookVec = this.getLookVec();
				entityitem.setVelocity(lookVec.xCoord * 0.3, lookVec.yCoord * 0.3, lookVec.zCoord * 0.3);
				if (stack.hasTagCompound()) {
					entityitem.getEntityItem().setTagCompound((NBTTagCompound) stack.getTagCompound().copy());
				}
				entityitem.delayBeforeCanPickup = 10;
				worldObj.spawnEntityInWorld(entityitem);
			}
			inventory.setInventorySlotContents(i, null);
		}
	}

	public void onEntityUpdate() {
		super.onEntityUpdate();
		if (!worldObj.isRemote) {
			// check we can still find the controller
			TileEntityRobot controller = (TileEntityRobot) getController();
			if (controller != null) {
				controller.registerRobot(robotId, this);
			}
			linkedToController = controller != null;
			
			// update all the modules
			for (IRobotUpgradeInstance upgradeInstance : upgradeInstances.values()) {
				upgradeInstance.update();
			}
			
		} else {
			this.weaponSpin += getWeaponSpinSpeed();
		}
	}

	// TODO: add this as a packed boolean in the dataWatcher.
	// lets change the model if he's not linked up!
	@Override
	public boolean isLinkedToController() {
		return linkedToController;
	}

	protected void updateAITasks() {
		//temporarily store the pitch, because the super method sets it to 0
		float oldPitch = rotationPitch;
		super.updateAITasks();
		this.rotationPitch = oldPitch;
	}

	protected boolean isAIEnabled() {
		return true;
	}

	@Override
	protected void playStepSound(int par1, int par2, int par3, int par4) {
		this.playSound("openperipheral.robotstepping", 1F, 1F);
	}

	@Override
	protected String getDeathSound() {
		return "openperipheral.robotdead";
	}

	@Override
	protected String getHurtSound() {
		return "openperipheral.robothurt";
	}

	@Override
	public int getMaxHealth() {
		return 40;
	}

	@Override
	protected boolean canDespawn() {
		return false;
	}

	@Override
	public TileEntity getController() {
		if (worldObj.blockExists(controllerX, controllerY, controllerZ)) {
			TileEntity tile = worldObj.getBlockTileEntity(controllerX, controllerY, controllerZ);
			if (tile != null && tile instanceof TileEntityRobot) {
				if (((TileEntityRobot) tile).getUuid().equals(controllerUuid)) {
					return tile;
				}
			}
		}
		return null;
	}

	public float getWeaponSpin() {
		return weaponSpin;
	}

	@Override
	public float getEyeHeight() {
		return 2.2f;
	}

	@Override
	public Vec3 getLocation() {
		return worldObj.getWorldVec3Pool().getVecFromPool(posX, posY, posZ);
	}

	@Override
	public World getWorld() {
		return worldObj;
	}

	public boolean setUpgradesFromStack(ItemStack stack) {

		upgradeMethodMap.clear();

		/**
		 * This is where we'd read the available tags and decide which upgrades
		 * to apply
		 */
		if (stack.hasTagCompound()) {
			NBTTagCompound tag = (NBTTagCompound) stack.getTagCompound().copy();
			controllerX = tag.getInteger("controllerX");
			controllerY = tag.getInteger("controllerY");
			controllerZ = tag.getInteger("controllerZ");
			controllerUuid = tag.getString("controllerUuid");
			robotId = tag.getInteger("robotId");
			TileEntityRobot controller = (TileEntityRobot) getController();
			if (controller == null) {
				return false;
			}
			controller.registerRobot(robotId, this);
			readUpgradesFromNBT(tag);
		}

		for (IRobotUpgradeProvider supplier : RobotUpgradeManager.getProviders()) {
			IRobotUpgradeInstance instance = supplier.provideUpgradeInstance(this);

		}
		return true;
	}

	private void readUpgradesFromNBT(NBTTagCompound tag) {

		// make sure we remove any instances, because we're refreshing
		// all of the data
		this.tasks.taskEntries.clear();
		upgradeInstances.clear();
		upgradeMethodMap.clear();

		// if we've got an upgrades nbt tag
		if (tag.hasKey("upgrades")) {

			NBTTagCompound upgradesTag = tag.getCompoundTag("upgrades");
			Collection upgradesTags = upgradesTag.getTags();

			for (Iterator iterator = upgradesTags.iterator(); iterator.hasNext();) {
				Object next = iterator.next();
				if (next instanceof NBTTagCompound) {

					// get the tag and its name
					NBTTagCompound upgradeTag = (NBTTagCompound) next;
					String name = upgradeTag.getName();

					// get the relevant upgrade supplier
					IRobotUpgradeProvider supplier = RobotUpgradeManager.getSupplierById(name);
					if (supplier != null) {

						// create a new instance
						IRobotUpgradeInstance instance = supplier.provideUpgradeInstance(this);

						// let it read the nbt tag
						instance.readFromNBT(upgradeTag);

						// add all the methods to our method map
						for (IRobotMethod method : supplier.getMethods()) {
							upgradeMethodMap.put(method.getLuaName(), instance);
						}

						// add the instance to our instances list
						upgradeInstances.put(supplier.getUpgradeId(), instance);

						// append all the AI tasks
						HashMap<Integer, EntityAIBase> aiTasks = instance.getAITasks();
						if (aiTasks != null) {
							for (Entry<Integer, EntityAIBase> entry : aiTasks.entrySet()) {
								this.tasks.addTask(entry.getKey(), entry.getValue());
							}
						}
					}
				}
			}
		}
	}

	public IRobotUpgradeInstance getInstanceForLuaMethod(String methodName) {
		return upgradeMethodMap.get(methodName);
	}

	@Override
	public EntityCreature getEntity() {
		return this;
	}

	@Override
	public void fireEvent(String eventName) {
		fireEvent(eventName, null);
	}

	// TODO: get controller and fire the event
	@Override
	public void fireEvent(String eventName, Object[] args) {

	}

	public void setDead() {
		TileEntityRobot controller = (TileEntityRobot) getController();
		if (controller != null) {
			controller.unregisterRobot(robotId);
		}
		super.setDead();
	}

	public void readEntityFromNBT(NBTTagCompound tag) {
		super.readEntityFromNBT(tag);
		controllerX = tag.getInteger("controllerX");
		controllerY = tag.getInteger("controllerY");
		controllerZ = tag.getInteger("controllerZ");
		controllerUuid = tag.getString("controllerUuid");
		robotId = tag.getInteger("robotId");
		inventory.readFromNBT(tag);
		readUpgradesFromNBT(tag);
	}

	public void writeEntityToNBT(NBTTagCompound tag) {
		super.writeEntityToNBT(tag);
		tag.setInteger("controllerX", controllerX);
		tag.setInteger("controllerY", controllerY);
		tag.setInteger("controllerZ", controllerZ);
		tag.setString("controllerUuid", controllerUuid);
		tag.setInteger("robotId", robotId);
		inventory.writeToNBT(tag);
		NBTTagCompound upgrades = new NBTTagCompound();
		tag.setCompoundTag("upgrades", upgrades);
		for (Entry<String, IRobotUpgradeInstance> entry : upgradeInstances.entrySet()) {
			NBTTagCompound instanceTag = new NBTTagCompound();
			entry.getValue().writeToNBT(instanceTag);
			upgrades.setCompoundTag(entry.getKey(), instanceTag);
		}
	}

	/**
	 * If the player is sneaking and they click lets dismantle the robot
	 * TODO: only dismantle if owner or OP
	 */
	@Override
	public boolean interact(EntityPlayer player) {
		if (player.isSneaking() && !worldObj.isRemote) {
			ItemStack robot = new ItemStack(OpenPeripheral.Items.robot);
			NBTTagCompound tag = new NBTTagCompound();
			this.writeEntityToNBT(tag);
			robot.setTagCompound(tag);
			setDead();
			BlockUtils.dropItemStackInWorld(worldObj, posX, posY, posZ, robot);
			return true;
		}
		return false;
	}
}
