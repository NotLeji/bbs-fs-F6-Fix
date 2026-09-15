package mchorse.bbs_mod.utils.resources;

import mchorse.bbs_mod.BBSModClient;
import mchorse.bbs_mod.BBSSettings;
import mchorse.bbs_mod.graphics.texture.Texture;
import net.minecraft.client.MinecraftClient;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MultiLinkThread
{
    private static ExecutorService executor;

    private static synchronized ExecutorService getExecutor()
    {
        if (executor == null)
        {
            executor = Executors.newFixedThreadPool(Math.max(1, Runtime.getRuntime().availableProcessors() - 1));
        }

        return executor;
    }

    /**
     * Get stream for multi resource location
     */
    public static Pixels getStreamForMultiLink(MultiLink multi) throws IOException
    {
        if (multi.children.isEmpty())
        {
            throw new IOException("Given MultiLink is empty!");
        }

        try
        {
            if (BBSSettings.multiskinMultiThreaded.get())
            {
                add(multi);

                return null;
            }
            else
            {
                clear();

                return TextureProcessor.process(multi);
            }
        }
        catch (Exception e)
        {
            throw new IOException(e);
        }
    }

    public static void add(MultiLink location)
    {
        getExecutor().submit(() ->
        {
            try
            {
                Pixels pixels = TextureProcessor.process(location);

                MinecraftClient.getInstance().execute(() ->
                {
                    Texture newTexture = BBSModClient.getTextures().createTexture(location);

                    newTexture.bind();
                    newTexture.uploadTexture(pixels);

                    if (newTexture.isMipmap())
                    {
                        newTexture.generateMipmap();
                    }
                });
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        });
    }

    public static synchronized void clear()
    {
        if (executor != null)
        {
            executor.shutdownNow();
            executor = null;
        }
    }
}