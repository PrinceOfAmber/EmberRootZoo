package elucent.roots.gui;

import java.util.ArrayList;

import org.lwjgl.opengl.GL11;

import elucent.roots.RegistryManager;
import elucent.roots.Roots;
import elucent.roots.Util;
import elucent.roots.component.ComponentBase;
import elucent.roots.component.ComponentManager;
import elucent.roots.item.DustPetal;
import elucent.roots.research.EnumRecipeType;
import elucent.roots.research.ResearchBase;
import elucent.roots.research.ResearchGroup;
import elucent.roots.research.ResearchManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;

public class GuiTabletPage extends GuiScreen {
	public double mouseX = 0;
	public double mouseY = 0; 
	public double smoothMouseX = 0;
	public double smoothMouseY = 0; 
	public int layer = 0;
	public double cycle = 0;
	public int currentPage = 0;
	public ResearchBase research = null;
	public ResearchGroup group = null;
	boolean showRightArrow = false;
	boolean showLeftArrow = true;
	EntityPlayer player = null;
	boolean renderTooltip = false;
	int tooltipX = 0;
	int tooltipY = 0;
	ItemStack tooltipStack = null;
	public GuiTabletPage(ResearchGroup g, ResearchBase r, EntityPlayer player){
		this.player = player;
		group = g;
		research = r;
	}
	public void markTooltipForRender(ItemStack stack, int x, int y){
		renderTooltip = true;
		tooltipX = x;
		tooltipY = y;
		tooltipStack = stack;
	}
	
	public void doRenderTooltip(){
		if (renderTooltip){
			this.renderToolTip(tooltipStack, tooltipX, tooltipY);
			renderTooltip = false;
		}
	}
	
	public void renderItemStackAt(ItemStack stack, int x, int y, int mouseX, int mouseY){
		this.itemRender.renderItemIntoGUI(stack, x, y);
		this.itemRender.renderItemOverlayIntoGUI(this.fontRendererObj, stack, x, y, stack.stackSize != 1 ? Integer.toString(stack.stackSize) : "");
		if (mouseX >= x && mouseY >= y && mouseX < x+16 && mouseY < y+16){
			this.markTooltipForRender(stack, mouseX, mouseY);
		}
	}
	
	@Override
	public void keyTyped(char typedChar, int keyCode){
		if (keyCode == 1){
			player.openGui(Roots.instance, 1, player.worldObj, (int)player.posX, (int)player.posY, (int)player.posZ);
		}
	}
	
	@Override
	public boolean doesGuiPauseGame(){
		return false;
	}
	
	@Override
	public void mouseClicked(int mouseX, int mouseY, int mouseButton){
		float basePosX = ((float)width/2.0f)-96;
		float basePosY = ((float)height/2.0f)-128;
		if (showLeftArrow){
			if (mouseX >= basePosX-16 && mouseX < basePosX+16 && mouseY >= basePosY+224 && mouseY < basePosY+240){
				this.currentPage --;
			}
		}
		if (showRightArrow){
			if (mouseX >= basePosX+176 && mouseX < basePosX+208 && mouseY >= basePosY+224 && mouseY < basePosY+240){
				this.currentPage ++;
			}
		}
	}
	
	public void drawQuad(VertexBuffer vertexbuffer, float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4, int minU, int minV, int maxU, int maxV)
    {
        float f = 0.00390625F;
        float f1 = 0.00390625F;
        vertexbuffer.pos((double)(x4 + 0.0F), (double)(y4 + 0.0F), (double)this.zLevel).tex((double)((float)(minU + 0) * f), (double)((float)(minV + maxV) * f1)).endVertex();
        vertexbuffer.pos((double)(x3 + 0.0F), (double)(y3 + 0.0F), (double)this.zLevel).tex((double)((float)(minU + maxU) * f), (double)((float)(minV + maxV) * f1)).endVertex();
        vertexbuffer.pos((double)(x2 + 0.0F), (double)(y2 + 0.0F), (double)this.zLevel).tex((double)((float)(minU + maxU) * f), (double)((float)(minV + 0) * f1)).endVertex();
        vertexbuffer.pos((double)(x1 + 0.0F), (double)(y1 + 0.0F), (double)this.zLevel).tex((double)((float)(minU + 0) * f), (double)((float)(minV + 0) * f1)).endVertex();
    }
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks){
		GlStateManager.color(1, 1, 1, 1);
		GlStateManager.disableLighting();
		RenderHelper.disableStandardItemLighting();
		RenderHelper.enableGUIStandardItemLighting();
		GlStateManager.enableAlpha();
		if (this.currentPage == 0){
			this.showLeftArrow = false;
		}
		else {
			this.showLeftArrow = true;
		}
		if (this.currentPage == this.research.info.size() - 1){
			this.showRightArrow = false;
		}
		else {
			this.showRightArrow = true;
		}
		cycle += 4.0;
		this.drawDefaultBackground();
		Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation("roots:textures/gui/tabletGui.png"));
		this.mouseX = mouseX;
		this.mouseY = mouseY;
		
		float basePosX = ((float)width/2.0f)-96;
		float basePosY = ((float)height/2.0f)-128;
		if (currentPage >= research.info.size()){
			currentPage = 0;
		}
		EnumRecipeType type = research.info.get(currentPage).recipe;
		GlStateManager.color(1, 1, 1, 1);
		if (type == EnumRecipeType.TYPE_NULL){
			Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation("roots:textures/gui/tabletGui.png"));
			this.drawTexturedModalRect(basePosX,basePosY,64,0,192,256);
			String title = I18n.format("roots.research."+group.name+"."+research.name+".page"+(this.currentPage+1)+"title.name");
			fontRendererObj.drawStringWithShadow(title, basePosX+96-(this.fontRendererObj.getStringWidth(title)/2.0f), basePosY+12, Util.intColor(255, 255, 255));
			ArrayList<String> info = research.info.get(currentPage).makeLines(I18n.format("roots.research."+group.name+"."+research.name+".page"+(this.currentPage+1)+"info"));
			for (int i = 0; i < info.size(); i ++){
				fontRendererObj.drawStringWithShadow(info.get(i),basePosX+16,basePosY+32+i*11,Util.intColor(255, 255, 255));
			}
		}
		if (type == EnumRecipeType.TYPE_CRAFTING){
			Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation("roots:textures/gui/tabletCrafting.png"));
			this.drawTexturedModalRect(basePosX,basePosY,0,0,192,256);
			if (research.info.get(currentPage).craftingRecipe.get(0) != null){
				this.renderItemStackAt(research.info.get(currentPage).craftingRecipe.get(0), (int)basePosX+32, (int)basePosY+32, mouseX, mouseY);
			}
			if (research.info.get(currentPage).craftingRecipe.get(1) != null){
				this.renderItemStackAt(research.info.get(currentPage).craftingRecipe.get(1), (int)basePosX+56, (int)basePosY+32, mouseX, mouseY);
			}
			if (research.info.get(currentPage).craftingRecipe.get(2) != null){
				this.renderItemStackAt(research.info.get(currentPage).craftingRecipe.get(2), (int)basePosX+80, (int)basePosY+32, mouseX, mouseY);
			}
			if (research.info.get(currentPage).craftingRecipe.get(3) != null){
				this.renderItemStackAt(research.info.get(currentPage).craftingRecipe.get(3), (int)basePosX+32, (int)basePosY+56, mouseX, mouseY);
			}
			if (research.info.get(currentPage).craftingRecipe.get(4) != null){
				this.renderItemStackAt(research.info.get(currentPage).craftingRecipe.get(4), (int)basePosX+56, (int)basePosY+56, mouseX, mouseY);
			}
			if (research.info.get(currentPage).craftingRecipe.get(5) != null){
				this.renderItemStackAt(research.info.get(currentPage).craftingRecipe.get(5), (int)basePosX+80, (int)basePosY+56, mouseX, mouseY);
			}
			if (research.info.get(currentPage).craftingRecipe.get(6) != null){
				this.renderItemStackAt(research.info.get(currentPage).craftingRecipe.get(6), (int)basePosX+32, (int)basePosY+80, mouseX, mouseY);
			}
			if (research.info.get(currentPage).craftingRecipe.get(7) != null){
				this.renderItemStackAt(research.info.get(currentPage).craftingRecipe.get(7), (int)basePosX+56, (int)basePosY+80, mouseX, mouseY);
			}
			if (research.info.get(currentPage).craftingRecipe.get(8) != null){
				this.renderItemStackAt(research.info.get(currentPage).craftingRecipe.get(8), (int)basePosX+80, (int)basePosY+80, mouseX, mouseY);
			}
			if (research.info.get(currentPage).craftingRecipe.get(9) != null){
				this.renderItemStackAt(research.info.get(currentPage).craftingRecipe.get(9), (int)basePosX+144, (int)basePosY+56, mouseX, mouseY);
			}
			ArrayList<String> info = research.info.get(currentPage).makeLines(I18n.format("roots.research."+group.name+"."+research.name+".page"+(this.currentPage+1)+"info"));
			for (int i = 0; i < info.size(); i ++){
				fontRendererObj.drawStringWithShadow(info.get(i),basePosX+16,basePosY+104+i*11,Util.intColor(255, 255, 255));
			}
			String title = I18n.format("roots.research."+group.name+"."+research.name+".page"+(this.currentPage+1)+"title.name");
			fontRendererObj.drawStringWithShadow(title, basePosX+96-(this.fontRendererObj.getStringWidth(title)/2.0f), basePosY+12, Util.intColor(255, 255, 255));
		}
		if (type == EnumRecipeType.TYPE_SMELTING){
			Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation("roots:textures/gui/tabletSmelting.png"));
			this.drawTexturedModalRect(basePosX,basePosY,0,0,192,256);
			if (research.info.get(currentPage).smeltingRecipe.get(0) != null){
				this.renderItemStackAt(research.info.get(currentPage).smeltingRecipe.get(0), (int)basePosX+56, (int)basePosY+40, mouseX, mouseY);
			}
			if (research.info.get(currentPage).smeltingRecipe.get(1) != null){
				this.renderItemStackAt(research.info.get(currentPage).smeltingRecipe.get(1), (int)basePosX+144, (int)basePosY+56, mouseX, mouseY);
			}
			ArrayList<String> info = research.info.get(currentPage).makeLines(I18n.format("roots.research."+group.name+"."+research.name+".page"+(this.currentPage+1)+"info"));
			for (int i = 0; i < info.size(); i ++){
				fontRendererObj.drawStringWithShadow(info.get(i),basePosX+16,basePosY+104+i*11,Util.intColor(255, 255, 255));
			}
			String title = I18n.format("roots.research."+group.name+"."+research.name+".page"+(this.currentPage+1)+"title.name");
			fontRendererObj.drawStringWithShadow(title, basePosX+96-(this.fontRendererObj.getStringWidth(title)/2.0f), basePosY+12, Util.intColor(255, 255, 255));
		}
		if (type == EnumRecipeType.TYPE_DISPLAY){
			Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation("roots:textures/gui/tabletDisplay.png"));
			this.drawTexturedModalRect(basePosX,basePosY,0,0,192,256);
			if (research.info.get(currentPage).displayItem != null){
				this.renderItemStackAt(research.info.get(currentPage).displayItem, (int)basePosX+88, (int)basePosY+48, mouseX, mouseY);
			}
			ArrayList<String> info = research.info.get(currentPage).makeLines(I18n.format("roots.research."+group.name+"."+research.name+".page"+(this.currentPage+1)+"info"));
			for (int i = 0; i < info.size(); i ++){
				fontRendererObj.drawStringWithShadow(info.get(i),basePosX+16,basePosY+80+i*11,Util.intColor(255, 255, 255));
			}
			String title = I18n.format("roots.research."+group.name+"."+research.name+".page"+(this.currentPage+1)+"title.name");
			fontRendererObj.drawStringWithShadow(title, basePosX+96-(this.fontRendererObj.getStringWidth(title)/2.0f), basePosY+12, Util.intColor(255, 255, 255));
		}
		if (type == EnumRecipeType.TYPE_ALTAR){
			Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation("roots:textures/gui/tabletAltar.png"));
			this.drawTexturedModalRect(basePosX,basePosY,0,0,192,256);
			
			for (int i = 0; i < research.info.get(currentPage).altarRecipe.blocks.size(); i ++){
				Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation("roots:textures/gui/tabletAltar.png"));
				int u = 192;
				int v = 240;
				int xShift = 0;
				int yShift = 0;
				this.drawTexturedModalRect(basePosX+93, basePosY+153, 192, 32, 16, 16);
				if (research.info.get(currentPage).altarRecipe.blocks.get(i) == RegistryManager.standingStoneT1){
					u = 192;
					v = 48;
					xShift = 8*research.info.get(currentPage).altarRecipe.positions.get(i).getX();
					yShift = 8*research.info.get(currentPage).altarRecipe.positions.get(i).getZ();
				}
				if (research.info.get(currentPage).altarRecipe.blocks.get(i) == RegistryManager.standingStoneT2){
					u = 192;
					v = 64;
					xShift = 8*research.info.get(currentPage).altarRecipe.positions.get(i).getX();
					yShift = 8*research.info.get(currentPage).altarRecipe.positions.get(i).getZ();
				}
				this.drawTexturedModalRect(basePosX+93+xShift, basePosY+153+yShift, u, v, 16, 16);
			}
			
			for (int i = 0; i < research.info.get(currentPage).altarRecipe.ingredients.size(); i ++){
				if (research.info.get(currentPage).altarRecipe.ingredients.get(i) != null){
					this.renderItemStackAt(research.info.get(currentPage).altarRecipe.ingredients.get(i), (int)basePosX+64+24*i, (int)basePosY+56, mouseX, mouseY);
				}
			}
			
			for (int i = 0; i < research.info.get(currentPage).altarRecipe.incenses.size(); i ++){
				if (research.info.get(currentPage).altarRecipe.incenses.get(i) != null){
					this.renderItemStackAt(research.info.get(currentPage).altarRecipe.incenses.get(i), (int)basePosX+76+16*i, (int)basePosY+88, mouseX, mouseY);
				}
			}
			
//			for (int i = 0; i < research.info.get(currentPage).altarRecipe.extraCosts.size(); i ++){
//				research.info.get(currentPage).altarRecipe.extraCosts.get(i).renderInGUI(this, basePosX, basePosY);
//			}
			String title = I18n.format("roots.research."+group.name+"."+research.name+".page"+(this.currentPage+1)+"title.name");
			fontRendererObj.drawStringWithShadow(title, basePosX+96-(this.fontRendererObj.getStringWidth(title)/2.0f), basePosY+12, Util.intColor(255, 255, 255));
		}
		if (type == EnumRecipeType.TYPE_MORTAR){
			Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation("roots:textures/gui/tabletMortar.png"));
			this.drawTexturedModalRect(basePosX,basePosY,0,0,192,256);
			for (int i = 0; i < research.info.get(currentPage).mortarRecipe.materials.size(); i ++){
				this.renderItemStackAt(research.info.get(currentPage).mortarRecipe.materials.get(i), (int)basePosX+24+i*16, (int)basePosY+56, mouseX, mouseY);
			}
			ComponentBase comp = ComponentManager.getComponentFromName(research.info.get(currentPage).mortarRecipe.effectResult);
			ItemStack stack = new ItemStack(RegistryManager.dustPetal);
			DustPetal.createData(stack, null, research.info.get(currentPage).mortarRecipe.effectResult, new ArrayList<ItemStack>());
			this.renderItemStackAt(stack, (int)basePosX+144, (int)basePosY+56, mouseX, mouseY);
			ArrayList<String> info = research.info.get(currentPage).makeLines(I18n.format("roots.research."+group.name+"."+research.name+".page"+(this.currentPage+1)+"info"));
			for (int i = 0; i < info.size(); i ++){
				fontRendererObj.drawStringWithShadow(info.get(i),basePosX+16,basePosY+96+i*11,Util.intColor(255, 255, 255));
			}
			String title = I18n.format("roots.research."+group.name+"."+research.name+".page"+(this.currentPage+1)+"title.name");
			if (research.info.get(currentPage).mortarRecipe.disabled){
				title = TextFormatting.RED + I18n.format("roots.research.disabled.name");
			}
			fontRendererObj.drawStringWithShadow(title, basePosX+96-(this.fontRendererObj.getStringWidth(title)/2.0f), basePosY+12, Util.intColor(255, 255, 255));
		}
		Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation("roots:textures/gui/tabletGui.png"));
		if (showLeftArrow){
			if (mouseX >= basePosX-16 && mouseX < basePosX+16 && mouseY >= basePosY+224 && mouseY < basePosY+240){
				this.drawTexturedModalRect(basePosX-16, basePosY+224, 32, 80, 32, 16);
			}
			else {
				this.drawTexturedModalRect(basePosX-16, basePosY+224, 32, 64, 32, 16);
			}
		}
		if (showRightArrow){
			if (mouseX >= basePosX+176 && mouseX < basePosX+208 && mouseY >= basePosY+224 && mouseY < basePosY+240){
				this.drawTexturedModalRect(basePosX+176, basePosY+224, 0, 80, 32, 16);
			}
			else {
				this.drawTexturedModalRect(basePosX+176, basePosY+224, 0, 64, 32, 16);
			}
		}
		
		doRenderTooltip();
		GlStateManager.color(1f, 1f, 1f, 1f);
		
		GlStateManager.enableBlend();
		Tessellator tessellator = Tessellator.getInstance();
        VertexBuffer vertexbuffer = tessellator.getBuffer();
        vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX);
	    tessellator.draw();
	    GlStateManager.disableBlend();
		RenderHelper.enableStandardItemLighting();
		GlStateManager.enableLighting();
		GlStateManager.disableAlpha();
	}
}
