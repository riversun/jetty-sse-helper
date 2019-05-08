# Overview
This is SSE(Server Sent Events) library for jetty.

- You can easily execute SSE with jetty.
Even containers other than jetty can be executed if servlet 3.0 or later.
- This library is based on http://github.com/mariomac's servlet sse library.

It is licensed under [MIT](https://opensource.org/licenses/MIT).

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.riversun/jetty-sse-helper/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.riversun/jetty-sse-helper)

# Dependencies

**Maven**

```XML
<dependency>
	<groupId>org.riversun</groupId>
	<artifactId>jetty-sse-helper</artifactId>
	<version>1.0.0</version>
</dependency>
```

# Example

This is a SSE demo.

<img src="https://user-images.githubusercontent.com/11747460/57308161-c9cc1c80-7120-11e9-92ea-dedc33f00731.gif">

- SSEServlet.java

```Java
@SuppressWarnings("serial")
public class SSEServlet extends HttpServlet {

    private final SSEHelper mPushHelper = new SSEHelper();

    /**
     * When receiving a request from JavaScript, add the requesting client to the PUSH (broadcast) target list
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("Added " + req + " to target to broadcast");
        mPushHelper.addTarget(req);
    }

    /**
     * Broadcast message when receiving POST request
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String msgToSend = req.getParameter("message");
        if (msgToSend != null && !msgToSend.isEmpty()) {

        } else {
            msgToSend = "No Message";
        }
        mPushHelper.broadcast("message", msgToSend);

        resp.setContentType("text/plain; charset=UTF-8");
        final PrintWriter out = resp.getWriter();
        out.println("OK");
        out.close();
    }

}
```

- StartServer.Java

```Java
public class StartServer {

    public void start() {

        final int port = 8080;

        ServletContextHandler servletContextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        ServletHolder sh = servletContextHandler.addServlet(SSEServlet.class, "/sse");
        sh.setAsyncSupported(true);

        final HandlerList hnList = new HandlerList();

        final ResourceHandler rh = new ResourceHandler();
        rh.setResourceBase(System.getProperty("user.dir") + "/htdocs");
        rh.setDirectoriesListed(false);
        rh.setWelcomeFiles(new String[] { "index.html" });
        rh.setCacheControl("no-store,no-cache,must-revalidate");
        hnList.addHandler(rh);

        hnList.addHandler(servletContextHandler);

        final Server server = new Server(port);
        server.setHandler(hnList);

        try {
            server.start();
            System.out.println("Server started on port:" + port);
            server.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new StartServer().start();
    }

}
```

Place client html file to **htdocs** folder on root of working directory.

- sse.html

```html
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Title</title>
</head>
<body>

<h1>Server Sent Event(SSE) Example</h1>
- Receive PUSH message from server and show that message<br>
- You can do send PUSH message from form below.<br>
- You can receive message on another browser you will open.
<hr>
<h3>Message to PUSH</h3>
<input id="text_message" value="Test message">
<button id="send_message">Send PUSH Message</button>
(You can push enter to send)
<br>
<br>
<small>（You can receive the message below even if you open <a href="sse.html" target="_blank">another browser</a> or tab）</small>
<hr>
<h3>Message received as PUSH from server</h3>
<div id="messages"></div>
<script>
    const eventSource = new EventSource('sse');//http://localhost:8080/sse
    const msgContent = document.querySelector('#messages');
    eventSource.addEventListener('message', (event) => {
        console.info('SSE: ' + event.data);
        msgContent.innerHTML += event.data + '<br>';
    });
    const msgText = document.querySelector('#text_message');
    const msgPostBtn = document.querySelector('#send_message');

    const funcSendData = (event) => {
        sendData('sse', {message: msgText.value});
        msgText.value='';
    };
    msgText.addEventListener('keypress', function (event) {

        if (event.key === 'Enter') {
            event.preventDefault();
            funcSendData(event);
        }
    });

    msgPostBtn.addEventListener('click', funcSendData);


    function sendData(url, data) {
        const XHR = new XMLHttpRequest();
        let urlEncodedData = '';
        const urlEncodedDataPairs = [];
        let name;

        for (name in data) {
            urlEncodedDataPairs.push(encodeURIComponent(name) + '=' + encodeURIComponent(data[name]));
        }

        urlEncodedData = urlEncodedDataPairs.join('&').replace(/%20/g, '+');

        XHR.addEventListener('load', function (event) {
        });
        XHR.addEventListener('error', function (event) {
        });

        XHR.open('POST', url);
        XHR.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
        XHR.send(urlEncodedData);
    }
</script>
</body>
</html>
```
