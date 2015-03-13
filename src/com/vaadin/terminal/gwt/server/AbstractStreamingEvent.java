/*
 * Copyright 2011 Vaadin Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.terminal.gwt.server;

import com.vaadin.terminal.StreamVariable.StreamingEvent;

/**
 * Abstract base class for StreamingEvent implementations.
 */
@SuppressWarnings("serial")
abstract class AbstractStreamingEvent implements StreamingEvent {
    private final String type;
    private final String filename;
    private final long contentLength;
    private final long bytesReceived;

    public final String getFileName() {
        return filename;
    }

    public final String getMimeType() {
        return type;
    }

    protected AbstractStreamingEvent(String filename, String type, long length,
            long bytesReceived) {
        this.filename = filename;
        this.type = type;
        contentLength = length;
        this.bytesReceived = bytesReceived;
    }

    public final long getContentLength() {
        return contentLength;
    }

    public final long getBytesReceived() {
        return bytesReceived;
    }

}
