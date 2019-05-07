/*
Copyright 2016 - Mario Macias Lloret

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
package org.riversun.sse;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author <a href="http://github.com/mariomac">Mario Macías</a>
 * @author Tom Misawa (riversun.org@gmail.com)
 */
public class SSEHelper {

    protected List<EventTarget> mEventTargetList = new CopyOnWriteArrayList<EventTarget>();

    public List<EventTarget> getTargetList() {
        return mEventTargetList;
    }

    public void addTarget(HttpServletRequest req) throws IOException {
        addTarget(new EventTarget(req));
    }

    public void addTarget(EventTarget eventTarget) throws IOException {
        mEventTargetList.add(eventTarget.ok().open());
    }

    public List<EventTarget> broadcast(String event, String data) {
        return broadcast(new MessageEvent(event, data));
    }

    public List<EventTarget> broadcast(MessageEvent messageEvent) {

        for (EventTarget eventTarget : mEventTargetList) {
            try {

                eventTarget.send(messageEvent);
            } catch (IOException e) {
                // This target is disconnected. Removing from targetList
                e.printStackTrace();
                mEventTargetList.remove(eventTarget);
            }
        }
        return mEventTargetList;
    }

    public void clearTargets() {

        for (EventTarget eventTarget : mEventTargetList) {
            try {
                eventTarget.close();
            } catch (Exception e) {
                // Uncontrolled exception when closing a dispatcher.
                // Removing anyway and ignoring.
            }
        }
        mEventTargetList.clear();

    }

    public static class EventTarget {

        private final AsyncContext mAsyncContext;
        private boolean mIsCompleted = false;

        public EventTarget(HttpServletRequest req) {
            mAsyncContext = req.startAsync();
            mAsyncContext.setTimeout(0);
            mAsyncContext.addListener(new AsyncListenerImpl());
        }

        public EventTarget ok() {
            final HttpServletResponse res = getAsyncResponse();
            res.setStatus(200);
            res.setContentType("text/event-stream");
            res.setCharacterEncoding("UTF-8");
            res.setHeader("Cache-Control", "no-cache");
            res.setHeader("Connection", "keep-alive");
            return this;
        }

        public EventTarget open() throws IOException {
            final ServletOutputStream os = getAsyncResponseStream();
            os.print("event: open\n\n");
            os.flush();

            return this;
        }

        public EventTarget send(String event, String data) throws IOException {

            final ServletOutputStream os = getAsyncResponseStream();

            os.print(new MessageEvent(event, data).toString());
            os.flush();

            return this;
        }

        public EventTarget send(MessageEvent messageEvent) throws IOException {

            try {
                final ServletOutputStream os = getAsyncResponseStream();
                os.print(messageEvent.toString());
                os.flush();
            } catch (Exception e) {
            }
            return this;
        }

        private HttpServletResponse getAsyncResponse() {
            final HttpServletResponse res = (HttpServletResponse) mAsyncContext.getResponse();
            return res;

        }

        private ServletOutputStream getAsyncResponseStream() throws IOException {
            final HttpServletResponse res = (HttpServletResponse) mAsyncContext.getResponse();
            ServletOutputStream os = res.getOutputStream();
            return os;
        }

        public void close() {
            if (!mIsCompleted) {
                mIsCompleted = true;
                mAsyncContext.complete();
            }
        }

        private class AsyncListenerImpl implements AsyncListener {
            @Override
            public void onComplete(AsyncEvent event) throws IOException {
                mIsCompleted = true;
                System.out.println("onComplete");
            }

            @Override
            public void onTimeout(AsyncEvent event) throws IOException {
            }

            @Override
            public void onError(AsyncEvent event) throws IOException {
            }

            @Override
            public void onStartAsync(AsyncEvent event) throws IOException {
            }
        }
    }

    public static class MessageEvent {
        private final String data;
        private final String event;
        private final Integer retry;
        private final String id;

        private String toStringCache;

        public MessageEvent(String event, String data) {
            this.data = data;
            this.event = event;
            this.retry = null;
            this.id = null;
            build();
        }

        public MessageEvent(String event, String data, Integer retry, String id) {
            this.data = data;
            this.event = event;
            this.retry = retry;
            this.id = id;
            build();
        }

        public void build() {
            StringBuilder sb = new StringBuilder();
            if (event != null) {
                sb.append("event: ").append(event.replace("\n", "")).append('\n');
            }
            if (data != null) {
                for (String s : data.split("\n")) {
                    sb.append("data: ").append(s).append('\n');
                }
            }
            if (retry != null) {
                sb.append("retry: ").append(retry).append('\n');
            }
            if (id != null) {
                sb.append("id: ").append(id.replace("\n", "")).append('\n');
            }

            // an empty line dispatches the event
            sb.append('\n');
            toStringCache = sb.toString();
        }

        public String toString() {
            return toStringCache;
        }

    }
}
