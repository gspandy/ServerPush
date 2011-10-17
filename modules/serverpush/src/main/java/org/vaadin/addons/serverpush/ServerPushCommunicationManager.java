/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.vaadin.addons.serverpush;

import com.vaadin.Application;
import com.vaadin.terminal.Paintable;
import com.vaadin.terminal.gwt.server.CommunicationManager;

import java.util.Map;
import java.util.WeakHashMap;

public class ServerPushCommunicationManager extends CommunicationManager {

    private final transient Map<Application, Thread> applicationThreadMap = new WeakHashMap<Application, Thread>();

    public ServerPushCommunicationManager(Application application) {
        super(application);
    }

    @Override
    public void repaintRequested(Paintable.RepaintRequestEvent event) {
        super.repaintRequested(event);
        final Application app = getApplication();
        synchronized (this.applicationThreadMap) {
            if (!this.applicationThreadMap.containsKey(app)) {
                Thread updateThread = createUpdateThread(app);
                this.applicationThreadMap.put(app, updateThread);
                updateThread.start();
            }
        }
    }

    private Thread createUpdateThread(final Application app) {
        return new Thread() {
            public void run() {
                try {
                    synchronized (app) {
                        ServerPushBroadcasterFactory.getInstance().broadcastForApplication(app);
                    }
                } finally {
                    applicationThreadMap.remove(app);
                }
            }
        };
    }
}
