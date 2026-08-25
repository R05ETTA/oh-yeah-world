package com.ohyeah.ohyeahmod.client.sound;

import com.ohyeah.ohyeahmod.registry.ModSoundEvents;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;

/** 围巾罗单一环境音池的时长权重与重复内容归一化。 */
@OnlyIn(Dist.CLIENT)
public final class PinkScarfAmbientCatalog {
    private static final double[] DURATIONS_SECONDS = {
            1.920D, 5.312D, 3.669D, 3.541D, 4.245D, 3.179D, 9.237D, 4.053D,
            3.115D, 2.368D, 5.717D, 6.507D, 3.584D, 5.205D, 3.947D, 27.904D,
            14.016D, 14.485D, 26.411D, 26.411D, 22.720D, 9.579D, 2.645D, 5.739D,
            3.947D, 15.915D, 11.904D, 8.768D, 9.408D, 5.547D, 6.443D, 11.648D,
            9.472D, 16.640D, 2.688D, 8.747D, 9.152D, 5.547D, 7.147D, 10.005D,
            10.240D, 13.440D, 6.613D, 14.101D, 10.240D, 10.645D, 3.840D, 2.603D,
            9.813D, 5.184D, 2.603D, 13.141D
    };

    private static ClientSoundManager.AmbientCatalog catalog;

    public static ClientSoundManager.AmbientCatalog get() {
        if (catalog == null) {
            catalog = create();
        }
        return catalog;
    }

    private static ClientSoundManager.AmbientCatalog create() {
        if (ModSoundEvents.TIANSULUO_PS_AMBIENT_VARIANTS.size() != DURATIONS_SECONDS.length) {
            throw new IllegalStateException("Pink Scarf ambient event and duration counts differ");
        }

        List<ClientSoundManager.AmbientVoice> voices = new ArrayList<>(DURATIONS_SECONDS.length);
        for (int index = 0; index < DURATIONS_SECONDS.length; index++) {
            int displayIndex = index + 1;
            double duration = DURATIONS_SECONDS[index];
            double weight = duration <= 6.0D ? 6.0D : duration <= 12.0D ? 3.0D : 1.0D;
            String repeatKey = displayIndex == 19 || displayIndex == 20
                    ? "tiansuluo_ps:ambient_duplicate_19_20"
                    : "tiansuluo_ps:ambient_" + displayIndex;
            if (displayIndex == 19 || displayIndex == 20) {
                // 两个 OGG 的解码波形完全相同；各取一半权重，合计仍等于一条长语音。
                weight *= 0.5D;
            }
            voices.add(new ClientSoundManager.AmbientVoice(
                    repeatKey,
                    ModSoundEvents.TIANSULUO_PS_AMBIENT_VARIANTS.get(index).get(),
                    weight,
                    duration > 12.0D
            ));
        }
        return new ClientSoundManager.AmbientCatalog("tiansuluo_ps:ambient", voices);
    }

    private PinkScarfAmbientCatalog() {
    }
}
