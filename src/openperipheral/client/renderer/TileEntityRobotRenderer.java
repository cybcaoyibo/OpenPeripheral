package openperipheral.client.renderer;

import openperipheral.client.model.ModelBlockFrame;
import openperipheral.client.model.ModelPlayerInventory;
import openperipheral.client.model.ModelRobot;
import openperipheral.common.config.ConfigSettings;
import openperipheral.common.entity.EntityRobot;
import openperipheral.common.tileentity.TileEntityRobot;
import openperipheral.common.tileentity.TileEntitySensor;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

public class TileEntityRobotRenderer extends TileEntitySpecialRenderer {

	private ModelRobot model = new ModelRobot();
	private ModelBlockFrame frame = new ModelBlockFrame();
	private EntityRobot robot = new EntityRobot(null);
	
	@Override
	public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float partialTicks) {
		TileEntityRobot robotTE = (TileEntityRobot) tileEntity;
		GL11.glPushMatrix();
		GL11.glTranslatef((float) x + 0.5F, (float) y + 1.0f, (float) z + 0.5F);
		GL11.glRotatef(180.0F, 1.0F, 0.0F, 0.0F);
		bindTextureByName(ConfigSettings.TEXTURES_PATH + "/models/blockframe.png");
		frame.render();
		GL11.glPushMatrix();
		GL11.glScalef(0.1f, 0.1f, 0.1f);
		GL11.glTranslatef(0, 1f, 0);
		GL11.glRotatef(robotTE.getRenderRot(), 0F, 1.0F, 0.0F);
		bindTextureByName(ConfigSettings.TEXTURES_PATH + "/models/robot.png");
		model.render(robot, 0, 0, 0, 0, 0, 0.0625F);
		GL11.glPopMatrix();
		GL11.glPopMatrix();
	}

}
