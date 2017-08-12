package elucent.roots.entity;

import elucent.roots.Util;
import elucent.roots.model.ModelHolder;
import elucent.roots.model.entity.ModelSpriteGuardianSegment;
import elucent.roots.model.entity.ModelSpriteGuardianSegmentFirst;
import elucent.roots.model.entity.ModelSpriteGuardianSegmentLarge;
import elucent.roots.model.entity.ModelSpriteGuardianTail;
import elucent.roots.model.entity.ModelSpriteling;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;

public class RenderSpriteGuardian extends RenderLiving<EntitySpriteGuardian> {

	public RenderSpriteGuardian(RenderManager renderManager, ModelBase modelBase, float shadowSize) {
		super(renderManager, modelBase, shadowSize);
	}
	
	@Override
	public void renderModel(EntitySpriteGuardian entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor){
		boolean flag = !entity.isInvisible() || this.renderOutlines;
        boolean flag1 = !flag && !entity.isInvisibleToPlayer(Minecraft.getMinecraft().thePlayer);
        int j = entity.getBrightnessForRender(0f);
        int k = j % 65536;
        int l = j / 65536;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)k, (float)l);
        GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE);

        if (flag || flag1)
        {
            if (!this.bindEntityTexture(entity))
            {
                return;
            }

            this.mainModel.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor);
            for (int i = 1; i < 15; i ++){
            	if (i == 14){
            		((ModelSpriteGuardianTail)ModelHolder.entityModels.get("spriteguardiantail")).render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor,i);
                }
            	else if (i ==1){
            		((ModelSpriteGuardianSegmentFirst)ModelHolder.entityModels.get("spriteguardiansegmentfirst")).render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor,i);
            	}
            	else if (i ==2){
            		((ModelSpriteGuardianSegmentLarge)ModelHolder.entityModels.get("spriteguardiansegmentlarge")).render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor,i);
            	}
            	else {
            		((ModelSpriteGuardianSegment)ModelHolder.entityModels.get("spriteguardiansegment")).render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor,i);
            	}
            }
        }
        GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.color(1f, 1f, 1f, 1f);
	}
	
	@Override
	public boolean shouldRender(EntitySpriteGuardian entity, ICamera camera, double camX, double camY, double camZ){
		return true;
	}

	@Override
	protected ResourceLocation getEntityTexture(EntitySpriteGuardian entity) {
		return new ResourceLocation("roots:textures/entity/spriteling/spiritTexture.png");
	}
}
