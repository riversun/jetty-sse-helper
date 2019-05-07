package org.riversun.sse;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public abstract class SSEBroadcastServlet extends HttpServlet {

    private final SSEHelper mPushHelper = new SSEHelper();

    @Override
    protected final void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        mPushHelper.addTarget(req);
    }

    public void broadcast(String event, String data) {
        mPushHelper.broadcast(event, data);
    }

}
