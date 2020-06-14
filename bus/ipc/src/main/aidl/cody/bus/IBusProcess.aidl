// IBusProcess.aidl
package cody.bus;

import cody.bus.IBusListener;

interface IBusProcess {
    void post(String process, String group, String event, String type, String value);
    void register(IBusListener listener);
    void unregister(IBusListener listener);
}
