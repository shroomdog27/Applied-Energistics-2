package appeng.bootstrap.components;

import com.google.common.base.Preconditions;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.block.Block;
import net.minecraft.client.render.RenderLayer;

/**
 * Sets the rendering type for a block.
 */
public class RenderTypeComponent implements IClientSetupComponent {

    private final Block block;

    private final RenderLayer renderType;

    public RenderTypeComponent(Block block, RenderLayer renderType) {
        this.block = block;
        this.renderType = Preconditions.checkNotNull(renderType);
    }

    @Override
    public void setup() {
        BlockRenderLayerMap.INSTANCE.putBlock(block, renderType);
    }

}