package elucent.roots.ritual;

import java.util.ArrayList;
import java.util.List;

import elucent.roots.RegistryManager;
import elucent.roots.Util;
import elucent.roots.tileentity.TileEntityAltar;
import elucent.roots.tileentity.TileEntityBrazier;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class RitualBase {
	public ArrayList<Block> blocks = new ArrayList<Block>();
	public ArrayList<BlockPos> positions = new ArrayList<BlockPos>();
	public ArrayList<ItemStack> incenses = new ArrayList<ItemStack>();
	public ArrayList<ItemStack> ingredients = new ArrayList<ItemStack>();
	public ArrayList<Object> extraCosts = new ArrayList<Object>();
	public Vec3d color = new Vec3d(255,255,255);
	public Vec3d secondaryColor = new Vec3d(255,255,255);
	public String name = "";
	
	public RitualBase(String parName, double r, double g, double b){
		name = parName;
		color = new Vec3d(r,g,b);
		secondaryColor = new Vec3d(r,g,b);
	}
	
	public RitualBase(String parName, double r, double g, double b, double r2, double g2, double b2){
		name = parName;
		color = new Vec3d(r,g,b);
		secondaryColor = new Vec3d(r2,g2,b2);
	}
	
	public RitualBase addBlock(Block b, int x, int y, int z){
		blocks.add(b);
		positions.add(new BlockPos(x,y,z));
		return this;
	}
	
	public RitualBase addIngredient(ItemStack i){
		ingredients.add(i);
		return this;
	}
	
	public RitualBase addIncense(ItemStack i){
		incenses.add(i);
		return this;
	}
	
	public void doEffect(World world, BlockPos pos, List<ItemStack> inventory, List<ItemStack> incenses){
		
	}
	
	public RitualBase addCost(Object cost){
		this.extraCosts.add(cost);
		return this;
	}
	
	public boolean matches(World world, BlockPos pos){
		if (positions.size() > 0){
			for (int i = 0; i < positions.size(); i ++){
				if (world.getBlockState(pos.add(positions.get(i).getX(),positions.get(i).getY(),positions.get(i).getZ())).getBlock() != blocks.get(i)){
					return false;
				}
			}
		}
		ArrayList<ItemStack> test = new ArrayList<ItemStack>();
		for (int i = -7; i < 8; i ++){
			for (int j = -7; j < 8; j ++){
				if (world.getBlockState(pos.add(i,0,j)).getBlock() == RegistryManager.brazier){
					if (world.getTileEntity(pos.add(i,0,j)) != null){
						TileEntityBrazier teb = (TileEntityBrazier)world.getTileEntity(pos.add(i,0,j));
						if (teb.burning){
							test.add(teb.heldItem);
						}
					}
				}
			}
		}
		return Util.itemListsMatchWithSize(incenses, test) && Util.itemListsMatchWithSize(ingredients,((TileEntityAltar)world.getTileEntity(pos)).inventory);
	}
}
