// IProcessManager.aidl
package cody.bus;

import cody.bus.EventWrapper;
import cody.bus.IProcessCallback;

interface IProcessManager {
    void register(IProcessCallback callback);
    void unregister(IProcessCallback callback);
    void resetSticky(in EventWrapper eventWrapper);
    void postToProcessManager(in EventWrapper eventWrapper);
}
