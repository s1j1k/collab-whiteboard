package com.example.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeFalse;

import java.awt.Color;
import java.awt.GraphicsEnvironment;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.example.shared.ChatMessage;
import com.example.shared.CursorPosition;
import com.example.shared.DrawAction;
import com.example.shared.Tool;
import com.example.shared.UserListUpdate;
import com.example.shared.WhiteBoardClientInterface;

/**
 * Concurrency behaviour of the server's per-client outbound queues.
 * Clients are in-process fakes, so no network or RMI registry is needed.
 */
public class WhiteBoardServerTest {

    private static final int ACTIONS = 50;

    private WhiteBoardServer server;

    /** Records every draw action it receives, optionally slowly or failing. */
    static class FakeClient implements WhiteBoardClientInterface {
        final List<DrawAction> received = new CopyOnWriteArrayList<>();
        final List<List<String>> userLists = new CopyOnWriteArrayList<>();
        final CountDownLatch allReceived = new CountDownLatch(ACTIONS);
        final long delayMs;
        final boolean dead;

        FakeClient(long delayMs, boolean dead) {
            this.delayMs = delayMs;
            this.dead = dead;
        }

        @Override
        public void receiveDrawAction(DrawAction action) throws RemoteException {
            if (dead) {
                throw new RemoteException("client unreachable");
            }
            try {
                Thread.sleep(delayMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            received.add(action);
            allReceived.countDown();
        }

        @Override
        public void receiveUserListUpdate(UserListUpdate update) {
            userLists.add(update.getUsernames());
        }

        @Override public void onRegistrationApproved() {}
        @Override public void onRegistrationRejected(String reason) {}
        @Override public void receiveChatMessage(ChatMessage message) {}
        @Override public void updateCursorPosition(CursorPosition position) {}
        @Override public void kickedFromServer() {}
        @Override public void receiveCanvasImage(byte[] imageData) {}
        @Override public void onServerShutdown(String reason) {}
    }

    @Before
    public void setUp() throws Exception {
        // The server owns a (hidden) Swing window
        assumeFalse("needs a display", GraphicsEnvironment.isHeadless());
        server = new WhiteBoardServer(0, "host");
        // Approve joins programmatically instead of through a dialog
        server.setEventListener(username -> {});
    }

    @After
    public void tearDown() throws Exception {
        if (server != null) {
            server.notifyShutdown("test done");
            server.gui.dispose();
            UnicastRemoteObject.unexportObject(server, true);
        }
    }

    private void join(String username, FakeClient client) throws RemoteException {
        server.registerClient(username, client);
        server.approveClient(username);
    }

    private static DrawAction line(int i) {
        return new DrawAction(Tool.LINE, i, i, i + 1, i + 1, Color.BLACK, 2);
    }

    @Test
    public void slowClientDoesNotBlockBroadcastOrOtherClients() throws Exception {
        FakeClient fast = new FakeClient(0, false);
        FakeClient slow = new FakeClient(100, false);
        join("fast", fast);
        join("slow", slow);

        long start = System.nanoTime();
        for (int i = 0; i < ACTIONS; i++) {
            server.broadcastDrawAction(line(i));
        }
        long broadcastMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);

        // Sending only queues work: 50 actions to a 100 ms client would take 5 s if it blocked
        assertTrue("broadcast blocked for " + broadcastMs + " ms", broadcastMs < 1000);
        assertTrue("fast client starved", fast.allReceived.await(2, TimeUnit.SECONDS));
        assertTrue("slow client fell over", slow.allReceived.await(10, TimeUnit.SECONDS));
    }

    @Test
    public void everyClientReceivesActionsInBroadcastOrder() throws Exception {
        FakeClient a = new FakeClient(0, false);
        FakeClient b = new FakeClient(2, false);
        join("a", a);
        join("b", b);

        for (int i = 0; i < ACTIONS; i++) {
            server.broadcastDrawAction(line(i));
        }
        assertTrue(a.allReceived.await(5, TimeUnit.SECONDS));
        assertTrue(b.allReceived.await(5, TimeUnit.SECONDS));

        for (int i = 0; i < ACTIONS; i++) {
            assertEquals(i, a.received.get(i).getX1());
            assertEquals(i, b.received.get(i).getX1());
        }
    }

    @Test
    public void deadClientIsDroppedAndOthersCarryOn() throws Exception {
        FakeClient alive = new FakeClient(0, false);
        FakeClient dead = new FakeClient(0, true);
        join("alive", alive);
        join("dead", dead);

        for (int i = 0; i < ACTIONS; i++) {
            server.broadcastDrawAction(line(i));
        }
        assertTrue(alive.allReceived.await(5, TimeUnit.SECONDS));

        // The surviving client is told the dead one left
        long deadline = System.currentTimeMillis() + 5000;
        boolean dropped = false;
        while (!dropped && System.currentTimeMillis() < deadline) {
            List<List<String>> lists = alive.userLists;
            dropped = !lists.isEmpty() && !lists.get(lists.size() - 1).contains("dead");
            Thread.sleep(20);
        }
        assertTrue("dead client still listed", dropped);
        assertFalse(alive.userLists.get(alive.userLists.size() - 1).isEmpty());
    }
}
