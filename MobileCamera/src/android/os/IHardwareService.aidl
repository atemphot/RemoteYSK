package android.os;

interface IHardwareService
{  
    // obsolete flashlight support
    boolean getFlashlightEnabled();
    void setFlashlightEnabled(boolean on);
}
