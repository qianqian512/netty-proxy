package org.hum.nettyproxy.common.handler;

import org.hum.nettyproxy.common.util.AESCoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

public class EncryptPipeChannelHandler extends ChannelInboundHandlerAdapter {

	private static final Logger logger = LoggerFactory.getLogger(EncryptPipeChannelHandler.class);
	
	private Channel pipeChannel;

	public EncryptPipeChannelHandler(Channel channel) {
		this.pipeChannel = channel;
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		try {
			if (pipeChannel.isActive()) {
				ByteBuf bytebuff = (ByteBuf) msg;
				if (!bytebuff.hasArray()) {
					byte[] arr = new byte[bytebuff.readableBytes()];
					bytebuff.getBytes(0, arr);
					try {
						pipeChannel.writeAndFlush(Encryptor.encrypt(ctx.alloc().directBuffer(), arr));
					} catch (Exception e) {
						logger.error("encoding error", e);
					}
				}
			}
		} finally {
			ReferenceCountUtil.release(msg);
		}
	}
	
	public static class Encryptor {
		public static ByteBuf encrypt(ByteBuf byteBuf, byte[] bytes) {
			byte[] encrypt = AESCoder.encrypt(bytes);
			byteBuf.writeInt(encrypt.length);
			byteBuf.writeBytes(encrypt);
			return byteBuf;
		}
	}
}