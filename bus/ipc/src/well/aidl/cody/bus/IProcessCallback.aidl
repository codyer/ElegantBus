// IProcessCallback.aidl
package cody.bus;

import cody.bus.EventWrapper;

interface IProcessCallback {
    String processName();
    void onPost(in EventWrapper eventWrapper);
    void onPostSticky(in EventWrapper eventWrapper);
}
