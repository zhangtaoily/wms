package com.wms.job.service;

import com.wms.job.pojo.ProgressEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author li shuang
 */
@Slf4j
@Service
public class SSEService {
    private static final Map<String,SseEmitter> SSE_EMITTER_MAP = new ConcurrentHashMap<>();
    // 保存数据Map
    public static final Map<String, StringBuilder> USER_DATA_MAP = new ConcurrentHashMap<>();
    public SseEmitter crateSse(String uid) {
        SseEmitter sseEmitter = new SseEmitter(7200000L);
        sseEmitter.onCompletion(() -> {
            log.info("[{}]结束链接" , uid);
            SSE_EMITTER_MAP.remove(uid);
        });
        sseEmitter.onTimeout(() -> {
            log.info("[{}]链接超时",uid);
            SSE_EMITTER_MAP.remove(uid);
        });
        sseEmitter.onError(throwable -> {
            try{
                log.info("[{}]链接异常，{}",uid,throwable.toString());
                sseEmitter.send(SseEmitter.event()
                    .id(uid)
                    .name("发生异常")
                    .data("发生异常请重试")
                    .reconnectTime(3000));
                SSE_EMITTER_MAP.put(uid,sseEmitter);
            }catch (IOException e){
                e.printStackTrace();
            }
        });
        try{
            sseEmitter.send(SseEmitter.event().reconnectTime(5000));
        }catch (IOException e){
            e.printStackTrace();
        }
        SSE_EMITTER_MAP.put(uid,sseEmitter);
        log.info("[{}]创建sse连接成功!",uid);
        return sseEmitter;
    }

    public boolean sendMessage(String uid,String messageId,String message){
        return sendMessage(uid,messageId,message,"default");
    }
    public boolean sendMessage(String uid,String messageId,String message,String eventName){
        if(StringUtils.isEmpty(message)){
            log.info("[{}]参数异常，msg为空",uid);
            return false;
        }
        SseEmitter sseEmitter = SSE_EMITTER_MAP.get(uid);
        if(sseEmitter == null){
            log.info("[{}]sse连接不存在",uid);
            return  false;
        }
        try{
            sseEmitter.send(SseEmitter.event().id(messageId).reconnectTime(60000).data(message).name(eventName));
            log.info("用户{},消息ID：{}，推送成功：{}",uid,messageId,message);
            putMsgToMap(uid,message);
            return true;
        }catch (IOException e){
            SSE_EMITTER_MAP.remove(uid);
            log.info("用户{},消息ID：{}，消息推送失败：{}",uid,messageId,message);
            sseEmitter.complete();
            return false;
        }
    }

    public void closeSse(String uid){
        if(SSE_EMITTER_MAP.containsKey(uid)){
            SseEmitter sseEmitter = SSE_EMITTER_MAP.get(uid);
            sseEmitter.complete();
            SSE_EMITTER_MAP.remove(uid);
            USER_DATA_MAP.remove(uid);
        }else {
            log.info("用户{}连接已关闭",uid);
        }
    }
    public SseEmitter getEmitter(String uid){
        return SSE_EMITTER_MAP.get(uid);
    }
    public void sendMsgWithProgress(ProgressEvent progress){
        try {
            SseEmitter emitter = getEmitter(progress.getUid());
            emitter.send(SseEmitter.event()
                .data(progress)
                .name("progress"));
            putMsgToMap(progress.getUid(),progress.getMessage());
        } catch (IOException e) {
            // 处理异常
            log.info("sendMsgWithProgress方法,用户{}发送数据异常",progress.getUid());
        }
    }
    public void putMsgToMap(String uid,String message){
        // 将消息追加到 StringBuilder 中
        StringBuilder data = USER_DATA_MAP.get(uid);
        if (data != null) {
            data.append(message).append("\n");
        }else{
            USER_DATA_MAP.put(uid,new StringBuilder(message));
        }
    }


}
