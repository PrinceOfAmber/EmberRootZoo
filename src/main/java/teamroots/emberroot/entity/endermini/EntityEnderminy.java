package teamroots.emberroot.entity.endermini;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.EnderTeleportEvent;
import teamroots.emberroot.Const;
import teamroots.emberroot.config.ConfigSpawnEntity;

/**
 * Original author: https://github.com/CrazyPants
 */
public class EntityEnderminy extends EntityMob {
  public static final String NAME = "enderminy";
  public static enum VariantColors {
    GREEN, BLUE, PURPLE;
    public String nameLower() {
      return this.name().toLowerCase();
    }
  }
  public static final DataParameter<Float> size = EntityDataManager.<Float> createKey(EntityEnderminy.class, DataSerializers.FLOAT);
  public static final DataParameter<Integer> variant = EntityDataManager.<Integer> createKey(EntityEnderminy.class, DataSerializers.VARINT);
  private static final int MAX_RND_TP_DISTANCE = 32;
  //  private static final int SCREAMING_INDEX = 30;
  private static final DataParameter<Boolean> SCREAMING_INDEX = EntityDataManager.<Boolean> createKey(EntityEnderminy.class, DataSerializers.BOOLEAN);
  private static final UUID attackingSpeedBoostModifierUUID = UUID.fromString("020E0DFB-87AE-4653-9556-831010E291B0");
  private static final AttributeModifier attackingSpeedBoostModifier = (new AttributeModifier(attackingSpeedBoostModifierUUID, "Attacking speed boost",
      6.2, 0)).setSaved(false);
  public static ConfigSpawnEntity config = new ConfigSpawnEntity(EntityEnderminy.class, EnumCreatureType.MONSTER);
  private boolean isAggressive;
  private boolean attackIfLookingAtPlayer = false;
  //  private boolean attackCreepers = Config.enderminyAttacksCreepers;
  private boolean groupAgroEnabled = true;
  public EntityEnderminy(World world) {
    super(world);
    stepHeight = 1.0F;
    tasks.addTask(0, new EntityAISwimming(this));
    tasks.addTask(2, new EntityAIAttackMelee(this, 1.0D, false));
    tasks.addTask(7, new EntityAIWander(this, 1.0D));
    tasks.addTask(8, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
    tasks.addTask(8, new EntityAILookIdle(this));
    targetTasks.addTask(1, new EntityAIHurtByTarget(this, false, new Class[0]));
    if (attackIfLookingAtPlayer) {
      targetTasks.addTask(2, new AIFindPlayer());
    }
    //    if(attackCreepers) {
    //      targetTasks.addTask(2, new EntityAINearestAttackableTarget<EntityCreeper>(this, EntityCreeper.class, true, true));
    //    }
  }
  @Override
  protected void applyEntityAttributes() {
    super.applyEntityAttributes();
    ConfigSpawnEntity.syncInstance(this, config.settings);
  }
  @Override
  protected void entityInit() {
    super.entityInit();
    dataManager.register(SCREAMING_INDEX, Boolean.valueOf(false));
    dataManager.register(variant, rand.nextInt(VariantColors.values().length));
    float sizeRand = MathHelper.nextFloat(world.rand, 0.45F, 1F);
    dataManager.register(size, sizeRand);
    setSize(0.6F * sizeRand, 2.9F * (sizeRand / 2));
  }
  //  @Override
  //  public boolean getCanSpawnHere() {
  //    boolean passedGrassCheck = true;
  //    if(Config.enderminySpawnOnlyOnGrass) {
  //      int i = MathHelper.floor(posX);
  //      int j = MathHelper.floor(getEntityBoundingBox().minY);
  //      int k = MathHelper.floor(posZ);
  //      passedGrassCheck = world.getBlockState(VecUtil.bpos(i, j - 1, k)).getBlock() == Blocks.GRASS;
  //    }
  //    return passedGrassCheck && posY > Config.enderminyMinSpawnY && super.getCanSpawnHere();
  //  }
  public Integer getVariant() {
    return dataManager.get(variant);
  }
  public Float getSizeSaved() {
    return dataManager.get(size);
  }
  public VariantColors getVariantEnum() {
    return VariantColors.values()[getVariant()];
  }
  /**
   * Checks to see if this enderman should be attacking this player
   */
  private boolean shouldAttackPlayer(EntityPlayer player) {
    ItemStack itemstack = player.inventory.armorInventory.get(3);
    //    3: Helmet, 2: Chestpiece, 1: Legs, 0: Boots
    if (itemstack != null && itemstack.getItem() == Item.getItemFromBlock(Blocks.PUMPKIN)) {
      return false;
    }
    else {
      Vec3d relativePlayerEyePos = new Vec3d(
          posX - player.posX,
          getEntityBoundingBox().minY + height / 2.0F - (player.posY + player.getEyeHeight()),
          posZ - player.posZ);
      double distance = relativePlayerEyePos.lengthVector();
      relativePlayerEyePos = relativePlayerEyePos.normalize();
      //NB: inverse of normal enderman, attack when this guy looks at the player instead of the other
      //way around
      Vec3d lookVec = getLook(1.0F).normalize();
      double dotTangent = -lookVec.dotProduct(relativePlayerEyePos);
      return dotTangent > 1.0D - 0.025D / distance;
    }
  }
  public void onLivingUpdate() {
    if (this.world.isRemote) {
      for (int i = 0; i < 2; ++i) {
        this.world.spawnParticle(EnumParticleTypes.PORTAL, this.posX + (this.rand.nextDouble() - 0.5D) * (double) this.width,
            this.posY + this.rand.nextDouble() * (double) this.height - 0.25D, this.posZ + (this.rand.nextDouble() - 0.5D) * (double) this.width,
            (this.rand.nextDouble() - 0.5D) * 2.0D, -this.rand.nextDouble(), (this.rand.nextDouble() - 0.5D) * 2.0D, new int[0]);
      }
    }
    isJumping = false;
    super.onLivingUpdate();
  }
  protected void updateAITasks() {
    if (isWet()) {
      attackEntityFrom(DamageSource.DROWN, 1.0F);
    }
    if (isScreaming() && !isAggressive && rand.nextInt(100) == 0) {
      setScreaming(false);
    }
    super.updateAITasks();
  }
  protected boolean teleportRandomly(int distance) {
    double d0 = posX + (rand.nextDouble() - 0.5D) * distance;
    double d1 = posY + rand.nextInt(distance + 1) - distance / 2;
    double d2 = posZ + (rand.nextDouble() - 0.5D) * distance;
    return teleportTo(d0, d1, d2);
  }
  protected boolean teleportRandomly() {
    return teleportRandomly(MAX_RND_TP_DISTANCE);
  }
  protected boolean teleportToEntity(Entity p_70816_1_) {
    Vec3d vec3 = new Vec3d(posX - p_70816_1_.posX, getEntityBoundingBox().minY + height / 2.0F - p_70816_1_.posY
        + p_70816_1_.getEyeHeight(), posZ - p_70816_1_.posZ);
    vec3 = vec3.normalize();
    double d0 = 16.0D;
    double d1 = posX + (rand.nextDouble() - 0.5D) * 8.0D - vec3.x * d0;
    double d2 = posY + (rand.nextInt(16) - 8) - vec3.y * d0;
    double d3 = posZ + (rand.nextDouble() - 0.5D) * 8.0D - vec3.z * d0;
    return teleportTo(d1, d2, d3);
  }
  protected boolean teleportTo(double x, double y, double z) {
    EnderTeleportEvent event = new EnderTeleportEvent(this, x, y, z, 0);
    if (MinecraftForge.EVENT_BUS.post(event)) { return false; }
    double d3 = posX;
    double d4 = posY;
    double d5 = posZ;
    posX = event.getTargetX();
    posY = event.getTargetY();
    posZ = event.getTargetZ();
    int xInt = MathHelper.floor(posX);
    int yInt = MathHelper.floor(posY);
    int zInt = MathHelper.floor(posZ);
    boolean flag = false;
    if (world.isBlockLoaded(new BlockPos(xInt, yInt, zInt))) {
      boolean foundGround = false;
      while (!foundGround && yInt > 0) {
        IBlockState bs = world.getBlockState(new BlockPos(xInt, yInt - 1, zInt));
        Block block = bs.getBlock();
        if (block.getMaterial(bs).blocksMovement()) {
          foundGround = true;
        }
        else {
          --posY;
          --yInt;
        }
      }
      if (foundGround) {
        setPosition(posX, posY, posZ);
        if (world.getCollisionBoxes(this, getEntityBoundingBox()).isEmpty() && !world.containsAnyLiquid(getEntityBoundingBox())) {
          flag = true;
        }
      }
    }
    if (!flag) {
      setPosition(d3, d4, d5);
      return false;
    }
    short short1 = 128;
    for (int l = 0; l < short1; ++l) {
      double d6 = l / (short1 - 1.0D);
      float f = (rand.nextFloat() - 0.5F) * 0.2F;
      float f1 = (rand.nextFloat() - 0.5F) * 0.2F;
      float f2 = (rand.nextFloat() - 0.5F) * 0.2F;
      double d7 = d3 + (posX - d3) * d6 + (rand.nextDouble() - 0.5D) * width * 2.0D;
      double d8 = d4 + (posY - d4) * d6 + rand.nextDouble() * height;
      double d9 = d5 + (posZ - d5) * d6 + (rand.nextDouble() - 0.5D) * width * 2.0D;
      world.spawnParticle(EnumParticleTypes.PORTAL, d7, d8, d9, f, f1, f2);
    }
    world.playSound(d3, d4, d5, SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.NEUTRAL, 1.0F, 1.0F, false);
    playSound(SoundEvents.ENTITY_ENDERMEN_TELEPORT, 1.0F, 1.0F);
    return true;
  }
  @Override
  protected SoundEvent getAmbientSound() {
    return isScreaming() ? SoundEvents.ENTITY_ENDERMEN_SCREAM : SoundEvents.ENTITY_ENDERMEN_AMBIENT;
  }
  @Override
  protected SoundEvent getHurtSound(DamageSource s) {
    return SoundEvents.ENTITY_ENDERMEN_HURT;
  }
  @Override
  protected SoundEvent getDeathSound() {
    return SoundEvents.ENTITY_ENDERMEN_DEATH;
  }
  /**
   * Called when the entity is attacked.
   */
  @Override
  public boolean attackEntityFrom(DamageSource damageSource, float p_70097_2_) {
    if (isEntityInvulnerable(damageSource)) { return false; }
    setScreaming(true);
    if (damageSource instanceof EntityDamageSourceIndirect) {
      isAggressive = false;
      for (int i = 0; i < 64; ++i) {
        if (teleportRandomly()) { return true; }
      }
      return super.attackEntityFrom(damageSource, p_70097_2_);
    }
    boolean res = super.attackEntityFrom(damageSource, p_70097_2_);
    if (damageSource instanceof EntityDamageSource && damageSource.getTrueSource() instanceof EntityPlayer &&
        getHealth() > 0
    //&& !ItemDarkSteelSword.isEquippedAndPowered((EntityPlayer) damageSource.getEntity(), 1)) {
    ) {
      isAggressive = true;
      if (rand.nextInt(3) == 0) {
        for (int i = 0; i < 64; ++i) {
          if (teleportRandomly(16)) {
            setAttackTarget((EntityPlayer) damageSource.getTrueSource());
            doGroupArgo();
            return true;
          }
        }
      }
    }
    if (res) {
      doGroupArgo();
    }
    return res;
  }
  private void doGroupArgo() {
    if (!groupAgroEnabled) { return; }
    if (!(getAttackTarget() instanceof EntityPlayer)) { return; }
    int range = 16;
    AxisAlignedBB bb = new AxisAlignedBB(posX - range, posY - range, posZ - range, posX + range, posY + range, posZ + range);
    List<EntityEnderminy> minies = world.getEntitiesWithinAABB(EntityEnderminy.class, bb);
    if (minies != null && !minies.isEmpty()) {
      for (EntityEnderminy miny : minies) {
        if (miny.getAttackTarget() == null) { //&& miny.canEntityBeSeen(this)) {
          miny.setAttackTarget(getAttackTarget());
        }
      }
    }
  }
  public boolean isScreaming() {
    return dataManager.get(SCREAMING_INDEX);
  }
  public void setScreaming(boolean p_70819_1_) {
    dataManager.set(SCREAMING_INDEX, Boolean.valueOf(p_70819_1_));
  }
  //  private final class ClosestEntityComparator implements Comparator<EntityCreeper> {
  //
  //    Vec3 pos = new Vec3(0, 0, 0);
  //
  //    @Override
  //    public int compare(EntityCreeper o1, EntityCreeper o2) {
  //      pos = new Vec3(posX, posY, posZ);
  //      double d1 = distanceSquared(o1.posX, o1.posY, o1.posZ, pos);
  //      double d2 = distanceSquared(o2.posX, o2.posY, o2.posZ, pos);
  //      return Double.compare(d1, d2);
  //    }
  //  }
  //
  //  public double distanceSquared(double x, double y, double z, Vec3 v2) {
  //    double dx, dy, dz;
  //    dx = x - v2.xCoord;
  //    dy = y - v2.yCoord;
  //    dz = z - v2.zCoord;
  //    return (dx * dx + dy * dy + dz * dz);
  //  }
  class AIFindPlayer extends EntityAINearestAttackableTarget<EntityPlayer> {
    private EntityPlayer targetPlayer;
    private int stareTimer;
    private int teleportDelay;
    private EntityEnderminy enderminy = EntityEnderminy.this;
    public AIFindPlayer() {
      super(EntityEnderminy.this, EntityPlayer.class, true);
    }
    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    @Override
    public boolean shouldExecute() {
      double d0 = getTargetDistance();
      List<EntityPlayer> list = taskOwner.world.getEntitiesWithinAABB(EntityPlayer.class, taskOwner.getEntityBoundingBox().expand(d0, 4.0D, d0), targetEntitySelector);
      Collections.sort(list, this.sorter);
      if (list.isEmpty()) {
        return false;
      }
      else {
        targetPlayer = (EntityPlayer) list.get(0);
        return true;
      }
    }
    /**
     * Execute a one shot task or start executing a continuous task
     */
    @Override
    public void startExecuting() {
      stareTimer = 5;
      teleportDelay = 0;
    }
    /**
     * Resets the task
     */
    @Override
    public void resetTask() {
      targetPlayer = null;
      enderminy.setScreaming(false);
      IAttributeInstance iattributeinstance = enderminy.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
      iattributeinstance.removeModifier(EntityEnderminy.attackingSpeedBoostModifier);
      super.resetTask();
    }
    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    @Override
    public boolean shouldContinueExecuting() {
      if (targetPlayer != null) {
        if (!enderminy.shouldAttackPlayer(targetPlayer)) {
          return false;
        }
        else {
          enderminy.isAggressive = true;
          enderminy.faceEntity(targetPlayer, 10.0F, 10.0F);
          return true;
        }
      }
      else {
        return super.shouldContinueExecuting();
      }
    }
    /**
     * Updates the task
     */
    @Override
    public void updateTask() {
      if (targetPlayer != null) {
        if (--stareTimer <= 0) {
          targetEntity = targetPlayer;
          targetPlayer = null;
          super.startExecuting();
          enderminy.playSound(SoundEvents.ENTITY_ENDERMEN_STARE, 1.0F, 1.0F);
          enderminy.setScreaming(true);
          IAttributeInstance iattributeinstance = enderminy.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
          iattributeinstance.applyModifier(EntityEnderminy.attackingSpeedBoostModifier);
        }
      }
      else {
        if (targetEntity != null) {
          if (targetEntity instanceof EntityPlayer && enderminy.shouldAttackPlayer((EntityPlayer) this.targetEntity)) {
            if (targetEntity.getDistanceSqToEntity(enderminy) < 16.0D) {
              enderminy.teleportRandomly();
            }
            teleportDelay = 0;
          }
          else if (targetEntity.getDistanceSqToEntity(enderminy) > 256.0D && this.teleportDelay++ >= 30 && enderminy.teleportToEntity(targetEntity)) {
            teleportDelay = 0;
          }
        }
        super.updateTask();
      }
    }
  }
  @Override
  public ResourceLocation getLootTable() {
    return new ResourceLocation(Const.MODID, "entity/ender_mini");//TODO: rng ender pearl
  }
}
