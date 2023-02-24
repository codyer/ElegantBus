// IProcessCallback.aidl
package cody.bus;

import cody.bus.EventWrapper;

interface IProcessCallback {
    String processName();
    void call(in EventWrapper eventWrapper, in int what);
}
