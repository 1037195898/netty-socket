package com.socket;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@ChannelHandler.Sharable
public class ActionChannelAdapter extends SimpleChannelInboundHandler<ActionData<?>> {

    private Map<Integer, ActionHandler<?>> actionMapping = new ConcurrentHashMap<>();

    private Set<SessionListener> listeners = new CopyOnWriteArraySet<>();

    public Map<String, Long> sessionVerify = new HashMap<>();
    /**
     * 已经被出现闲置状态的isSession
     */
    private Map<String, Integer> iosIdle = new HashMap<>();


    public void addSessionListener(SessionListener sessionListener) {
        listeners.add(sessionListener);
    }

    /**
     * 注册动作
     *
     * @param action  动作
     * @param handler 处理器
     */
    public void registerAction(int action, ActionHandler<?> handler) {
        if (actionMapping.containsKey(action)) {
            throw new RuntimeException("动作处理器[" + action + "]已经注册");
        }
        actionMapping.put(action, handler);
        if (handler instanceof SessionListener) {
            listeners.add((SessionListener) handler);
        }

    }

    /**
     * 删除动作
     *
     * @param action 动作
     */
    public void removeAction(int action) {
        ActionHandler<?> handler = actionMapping.remove(action);
        if (handler instanceof SessionListener) {
            listeners.remove(handler);
        }
    }

    /**
     * 获取动作
     *
     * @param action 动作
     * @return
     */
    public ActionHandler<?> getAction(int action) {
        return actionMapping.get(action);
    }

    /**
     * 执行一个  监听器
     *
     * @param data    数据
     * @param session 通信ioSession
     * @param message 未被解析的数据
     * @throws Exception
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void executeActionMapping(ActionData data, ChannelHandlerContext session, Object message) throws Exception {
        int action = data.getAction();
//		log.info("检测处理器["+action+"]");
        if (!actionMapping.containsKey(action)) {
//			throw new RuntimeException("动作处理器["+action+"]未注册");
            for (SessionListener listener : listeners) {
                listener.notRegAction(session, message);
            }
            return;
        }
        actionMapping.get(action).execute(data, session);
    }

    /**
     * 新的客户端连接事件
     * @param ctx
     * @throws Exception
     */
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        super.handlerAdded(ctx);
        System.out.println("新建连接");
        sessionVerify.put(ctx.channel().id().asLongText(), (long) 0);
        for (SessionListener listener : listeners) {
            listener.sessionCreated(ctx);
        }
    }

    /**
     * 处理器移除事件(断开连接)
     * @param ctx
     * @throws Exception
     */
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        super.handlerRemoved(ctx);
        System.out.println("断开连接");
        iosIdle.remove(ctx.channel().id().asLongText());
        sessionVerify.remove(ctx.channel().id().asLongText());
        for (SessionListener listener : listeners) {
            listener.sessionClosed(ctx);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);
        if (evt instanceof IdleStateEvent) { // 空闲状态
            IdleStateEvent e = (IdleStateEvent) evt;
            if (e.state() == IdleState.ALL_IDLE) {
                String id = ctx.channel().id().asLongText();
                if (iosIdle.containsKey(id)) {
                    Integer count = iosIdle.get(id);
                    if (count < 3) {
                        count++;
                        ctx.writeAndFlush(new ActionData<>(1));
                        iosIdle.put(id, count);
                    } else {
                        iosIdle.remove(id);
                        ctx.channel().close();
                        LoggerFactory.getLogger(getClass()).info("有用户空闲被关闭=[" + id + " , 用户数据=" + ctx + "]");
                    }
                } else {
                    iosIdle.put(id, 1);
                    ctx.writeAndFlush(new ActionData<>(1));
                }
                for (SessionListener listener : listeners) {
                    listener.sessionIdle(ctx, e.state());
                }
            }
        }
//        System.out.println("userEventTriggered|" + evt);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ActionData<?> msg) throws Exception {
        for (SessionListener listener : listeners) {
            listener.messageReceived(ctx, msg);
        }
        iosIdle.remove(ctx.channel().id().asLongText());
        if (sessionVerify.containsKey(ctx.channel().id().asLongText()) && msg.getVerify() > sessionVerify.get(ctx.channel().id().asLongText())) {
            sessionVerify.put(ctx.channel().id().asLongText(), msg.getVerify());
            executeActionMapping(msg, ctx, msg);
        }
    }

    /**
     * 异常发生事件
     * @param ctx
     * @param cause
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
//        logger.error("client caught exception", cause);
        if (cause instanceof IOException) {
            iosIdle.remove(ctx.channel().id().asLongText());
            sessionVerify.remove(ctx.channel().id().asLongText());
        }
        for (SessionListener listener : listeners) {
            listener.exceptionCaught(ctx, cause);
        }
        ctx.close();
    }

}
