package github.iri.jerm;

import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

@Mod(Jerm.MODID)
public class Jerm{
    public static final String MODID = "jerm";
    private static final Logger LOGGER = LogUtils.getLogger();

    public Jerm(){
        MinecraftForge.EVENT_BUS.register(this);
    }
}
