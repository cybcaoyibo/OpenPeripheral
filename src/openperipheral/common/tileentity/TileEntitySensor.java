package openperipheral.common.tileentity;

import dan200.computer.api.IHostedPeripheral;
import openperipheral.common.core.OPInventory;
import openperipheral.common.interfaces.IConditionalSlots;
import openperipheral.common.interfaces.IInventoryCallback;
import openperipheral.common.interfaces.IPeripheralProvider;
import openperipheral.common.interfaces.ISensorEnvironment;
import openperipheral.common.peripheral.SensorPeripheral;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class TileEntitySensor extends TileEntity implements IInventory, IInventoryCallback, IConditionalSlots, IPeripheralProvider, ISensorEnvironment {

	private final static float rotationSpeed = 3.0F;
	
	private float rotation;
	
	private SensorPeripheral peripheral;
	
	private OPInventory inventory = new OPInventory("sensor", false, 1);
	
	public TileEntitySensor() {
	}
	
	public float getRotation() {
		return rotation;
	}
	
	@Override
	public void updateEntity() {
		rotation = (rotation + rotationSpeed) % 360;
		peripheral.update();
	}

	@Override
	public boolean isTurtle() {
		return false;
	}

	@Override
	public Vec3 getLocation() {
		return worldObj.getWorldVec3Pool().getVecFromPool(xCoord, yCoord, zCoord);
	}

	@Override
	public World getWorld() {
		return worldObj;
	}

	@Override
	public IHostedPeripheral providePeripheral() {
		return peripheral;
	}

	@Override
	public boolean canTakeStack(int slotNumber, EntityPlayer player) {
		return true;
	}

	@Override
	public void onInventoryChanged(IInventory inventory) {
		
	}

	@Override
	public int getSizeInventory() {
		return inventory.getSizeInventory();
	}

	@Override
	public ItemStack getStackInSlot(int i) {
		return inventory.getStackInSlot(i);
	}

	@Override
	public ItemStack decrStackSize(int i, int j) {
		return inventory.decrStackSize(i, j);
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int i) {
		return inventory.getStackInSlotOnClosing(i);
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack) {
		inventory.setInventorySlotContents(i, itemstack);
	}

	@Override
	public String getInvName() {
		return inventory.getInvName();
	}

	@Override
	public boolean isInvNameLocalized() {
		return inventory.isInvNameLocalized();
	}

	@Override
	public int getInventoryStackLimit() {
		return inventory.getInventoryStackLimit();
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer) {
		return inventory.isUseableByPlayer(entityplayer);
	}

	@Override
	public void openChest() {
		
	}

	@Override
	public void closeChest() {
		
	}

	@Override
	public boolean isStackValidForSlot(int i, ItemStack itemstack) {
		return inventory.isStackValidForSlot(i, itemstack);
	}

	@Override
	public int getSensorRange() {
		return 5;
	}

}
