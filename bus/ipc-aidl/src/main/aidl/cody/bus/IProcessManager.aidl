// IProcessManager.aidl
package cody.bus;

import cody.bus.EventWrapper;
import cody.bus.IProcessCallback;

interface IProcessManager {
    void post(in EventWrapper eventWrapper);
    void register(IProcessCallback callback);
    void unregister(IProcessCallback callback);
}
