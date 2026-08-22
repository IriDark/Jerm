package github.iri.jerm;

import com.mojang.logging.LogUtils;
import net.neoforged.fml.common.Mod;
import net.neoforged.bus.api.IEventBus;
import org.slf4j.Logger;

@Mod(Jerm.MODID)
public class Jerm{
    public static final String MODID = "jerm";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Jerm(IEventBus modEventBus){
    }
}
