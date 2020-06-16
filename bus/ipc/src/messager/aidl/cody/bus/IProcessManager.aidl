// IProcessManager.aidl
package cody.bus;

import cody.bus.IProcessCallback;
import cody.bus.EventWrapper;

interface IProcessManager {
    void post(in EventWrapper eventWrapper);
    void register(IProcessCallback callback);
    void unregister(IProcessCallback callback);
}
