package openperipheral.common.item.meta;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.util.Icon;
import net.minecraft.world.World;
import net.minecraftforge.oredict.ShapedOreRecipe;
import openperipheral.OpenPeripheral;
import openperipheral.common.interfaces.IMetaItem;
import openperipheral.common.item.ItemGeneric;

public class MetaLCDScreen implements IMetaItem {

	private Icon icon;
	
	@Override
	public Icon getIcon() {
		return icon;
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return "openperipheral.lcdscreen";
	}

	@Override
	public boolean hitEntity(ItemStack itemStack, EntityLiving target, EntityLiving player) {
		return false;
	}

	@Override
	public boolean onItemUse(ItemStack itemStack, EntityPlayer player, World world, int x, int y, int z, int side, float par8, float par9, float par10) {
		return false;
	}

	@Override
	public ItemStack onItemRightClick(ItemStack itemStack, EntityPlayer player, World world) {
		return itemStack;
	}

	@Override
	public boolean displayInCreative() {
		return true;
	}

	@Override
	public void registerIcons(IconRegister register) {
		icon = 	register.registerIcon("openperipheral:lcdscreen");
	}

	@Override
	public void addRecipe() {
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(
				ItemGeneric.Metas.lcdScreen.newItemStack(),
				new Object[] {
					"ggg",
					"eee",
					"wpt",
					Character.valueOf('g'), new ItemStack(Block.thinGlass),
					Character.valueOf('e'), ItemGeneric.Metas.electrode.newItemStack(),
					Character.valueOf('w'), ItemGeneric.Metas.ribbonCable.newItemStack(),
					Character.valueOf('p'), ItemGeneric.Metas.preparedPCB.newItemStack(),
					Character.valueOf('t'), ItemGeneric.Metas.thinWire.newItemStack()
				}
		));
	}

}
