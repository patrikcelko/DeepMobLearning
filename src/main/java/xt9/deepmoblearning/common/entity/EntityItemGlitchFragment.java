package xt9.deepmoblearning.common.entity;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import xt9.deepmoblearning.DeepMobLearning;
import xt9.deepmoblearning.common.Registry;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * Created by xt9 on 2018-05-16.
 */
public class EntityItemGlitchFragment extends EntityItem {
    private ThreadLocalRandom rand = ThreadLocalRandom.current();
    private long progress = 0;

    public EntityItemGlitchFragment(World worldIn) {
        super(worldIn);
    }

    public EntityItemGlitchFragment(World worldIn, double x, double y, double z, ItemStack stack) {
        super(worldIn, x, y, z);
        setItem(stack);
        setPickupDelay(15);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        if(isInWater()) {
            AxisAlignedBB box = new AxisAlignedBB(posX - 1, posY - 1, posZ - 1, posX + 1, posY + 1, posZ + 1);
            List<EntityItem> goldEntities = world.getEntitiesWithinAABB(EntityItem.class, box).stream().filter((entityItem) -> isStackGold(entityItem.getItem())).collect(Collectors.toList());
            List<EntityItem> lapisEntities = world.getEntitiesWithinAABB(EntityItem.class, box).stream().filter((entityItem) -> isStackLapis(entityItem.getItem())).collect(Collectors.toList());
            List<EntityItem> fragmentEntities = world.getEntitiesWithinAABB(EntityItem.class, box).stream().filter((entityItem) -> isStackGlitchFragment(entityItem.getItem())).collect(Collectors.toList());

            progress++;
            boolean isValidEntities = goldEntities.size() > 0 && lapisEntities.size() > 0 && fragmentEntities.size() > 0;

            if (world.isRemote) {
                spawnFragmentParticles();

                if(isValidEntities) {
                    // Increase the amount of particles when all criterias are met
                    for (int i = 0; i < 3; i++) {
                        spawnFragmentParticles();
                    }
                }
            }

            if (!isValidEntities) {
                progress = 0;
                return;
            }

            if (!world.isRemote) {
                if (progress >= 35) {
                    EntityItem gold = goldEntities.get(goldEntities.size() - 1);
                    EntityItem lapis = lapisEntities.get(lapisEntities.size() - 1);
                    EntityItem fragment = fragmentEntities.get(fragmentEntities.size() - 1);

                    shrink(gold);
                    shrink(lapis);
                    shrink(fragment);

                    spawnIngot();
                }
            }
        }
    }

    private void spawnFragmentParticles() {
        DeepMobLearning.proxy.spawnSmokeParticle(world,
            posX + rand.nextDouble(-0.25D, 0.25D),
            posY + rand.nextDouble(-0.1D, 0.8D),
            posZ + rand.nextDouble(-0.25D, 0.25D),
            rand.nextDouble(-0.08, 0.08D),
            rand.nextDouble(-0.08D, 0.22D),
            rand.nextDouble(-0.08D, 0.08D),
            "cyan"
        );
    }

    private void spawnIngot() {
        EntityItem newItem = new EntityItem(world, posX, posY + 0.6D, posZ, new ItemStack(Registry.glitchInfusedIngot, 1));
        newItem.motionX = rand.nextDouble(-0.2D, 0.2D);
        newItem.motionY = 0;
        newItem.motionZ = rand.nextDouble(-0.2D, 0.2D);
        newItem.setDefaultPickupDelay();

        world.spawnEntity(newItem);
    }

    private void shrink(EntityItem entityItem) {
        entityItem.getItem().shrink(1);
        if(entityItem.getItem().getCount() <= 0) {
            entityItem.setDead();
        }
    }

    private boolean isStackGlitchFragment(ItemStack item) {
        return ItemStack.areItemsEqual(item, new ItemStack(Registry.glitchFragment)) && item.getCount() > 0;
    }

    private boolean isStackGold(ItemStack item) {
        return ItemStack.areItemsEqual(item, new ItemStack(Items.GOLD_INGOT)) && item.getCount() > 0;
    }

    private boolean isStackLapis(ItemStack item) {
        return ItemStack.areItemStacksEqual(item, new ItemStack(Items.DYE, item.getCount(), 4)) && item.getCount() > 0;
    }
}
