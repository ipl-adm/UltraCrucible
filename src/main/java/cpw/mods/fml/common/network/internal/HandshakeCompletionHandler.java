package cpw.mods.fml.common.network.internal;

import org.apache.logging.log4j.Level;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.network.handshake.NetworkDispatcher;
import cpw.mods.fml.common.network.internal.FMLMessage.CompleteHandshake;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.ChannelHandler.Sharable;

@Sharable
public class HandshakeCompletionHandler extends SimpleChannelInboundHandler<FMLMessage.CompleteHandshake> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, CompleteHandshake msg) throws Exception
    {
        // Cauldron start - attempt to fix race condition with attr being null
        io.netty.util.Attribute<NetworkDispatcher> attr = ctx.channel().attr(NetworkDispatcher.FML_DISPATCHER);
        if (attr != null)
        {
            NetworkDispatcher dispatcher = attr.getAndRemove();
            if (dispatcher != null) dispatcher.completeHandshake(msg.target);
        }
        // Cauldron end
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception
    {
        FMLLog.log(Level.ERROR, cause, "HandshakeCompletionHandler exception");
        super.exceptionCaught(ctx, cause);
    }
}