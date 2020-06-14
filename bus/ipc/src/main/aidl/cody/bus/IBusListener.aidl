// IBusListener.aidl
package cody.bus;

interface IBusListener {
    String process();
    void onPost(String group, String event, String type, String value);
    void onPostInit(String group, String event, String type, String value);
}
