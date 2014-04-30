package org.wso2.carbon.event.input.adaptor.file.internal.listener;

import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListenerAdapter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.event.input.adaptor.core.InputEventAdaptorListener;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FileTailerListener extends TailerListenerAdapter {

    private String fileName;

    boolean isFileFound = true;
    private Map<String, InputEventAdaptorListener> inpuEventListenerMap = new ConcurrentHashMap<String, InputEventAdaptorListener>();
    private static final Log log = LogFactory.getLog(FileTailerListener.class);
    private volatile InputEventAdaptorListener[] inputEventAdaptorListeners;

    public FileTailerListener(String fileName) {
        this.fileName = fileName;
    }


    @Override
    public void init(Tailer tailer) {
        super.init(tailer);
    }

    @Override
    public void handle(String line) {
        super.handle(line);
        if (line == null || line.isEmpty()) {
            return;
        }
        isFileFound = true;
        for (InputEventAdaptorListener id : inputEventAdaptorListeners) {
            id.onEventCall(line + " fileName: " + fileName + "\n");
        }
    }

    @Override
    public void fileNotFound() {
        if (isFileFound) {
            isFileFound = false;
            log.warn(" fileName: " + fileName + " not found, will retry continuously.");
        }
        log.debug("File  " + fileName + " not found");
        super.fileNotFound();
    }

    @Override
    public void handle(Exception ex) {
        log.error("Exception occurred : ", ex);
        super.handle(ex);
    }

    public synchronized void addListener(String id, InputEventAdaptorListener inputListener) {
        inpuEventListenerMap.put(id, inputListener);
        inputEventAdaptorListeners = inpuEventListenerMap.values().toArray(new InputEventAdaptorListener[inpuEventListenerMap.size()]);
    }

    public synchronized void removeListener(String id) {
        inpuEventListenerMap.remove(id);
        inputEventAdaptorListeners = inpuEventListenerMap.values().toArray(new InputEventAdaptorListener[inpuEventListenerMap.size()]);
    }

    public boolean hasOneSubscriber() {
        return inpuEventListenerMap.size() == 1;
    }

    public boolean hasNoSubscriber() {
        return inpuEventListenerMap.isEmpty();
    }

}
