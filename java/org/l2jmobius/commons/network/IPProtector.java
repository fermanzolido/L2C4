/*
 * Copyright (c) 2013 L2jMobius
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR
 * IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.l2jmobius.commons.network;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

/**
 * IPProtector manages IP-based security policies, including concurrent
 * connection limits
 * and frequency of connection attempts to mitigate DDoS and connection
 * exhaustion attacks.
 * 
 * @author Mobius
 */
public class IPProtector {
    private static final Logger LOGGER = Logger.getLogger(IPProtector.class.getName());

    private final Map<String, AtomicInteger> _connectionsPerIP = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> _lastConnectionTime = new ConcurrentHashMap<>();

    private final int _maxConnectionsPerIP;
    private final long _minConnectionInterval;

    /**
     * Creates a new IPProtector with settings from the provided configuration.
     * 
     * @param config the network configuration
     */
    public IPProtector(ConnectionConfig config) {
        _maxConnectionsPerIP = config.maxConnectionsPerIP;
        _minConnectionInterval = config.minConnectionInterval;
    }

    /**
     * Checks if a new connection attempt from the given IP is allowed.
     * 
     * @param ip the remote IP address
     * @return true if connection is allowed, false otherwise
     */
    public boolean canConnect(String ip) {
        final long currentTime = System.currentTimeMillis();

        // Check connection interval
        final AtomicLong lastTime = _lastConnectionTime.computeIfAbsent(ip, k -> new AtomicLong(0));
        if ((currentTime - lastTime.get()) < _minConnectionInterval) {
            LOGGER.warning("IPProtector: IP " + ip + " rejected due to rapid connection attempts.");
            return false;
        }
        lastTime.set(currentTime);

        // Check concurrent connections
        final AtomicInteger count = _connectionsPerIP.computeIfAbsent(ip, k -> new AtomicInteger(0));
        if (count.incrementAndGet() > _maxConnectionsPerIP) {
            count.decrementAndGet();
            LOGGER.warning("IPProtector: IP " + ip + " rejected due to too many concurrent connections (" + count.get()
                    + ").");
            return false;
        }

        return true;
    }

    /**
     * Decrements the connection count for the given IP address when a connection is
     * closed.
     * 
     * @param ip the remote IP address
     */
    public void onDisconnect(String ip) {
        if (ip == null || ip.isEmpty()) {
            return;
        }

        _connectionsPerIP.computeIfPresent(ip, (k, v) -> {
            if (v.decrementAndGet() <= 0) {
                return null;
            }
            return v;
        });
    }
}
