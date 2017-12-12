#ifndef _JNI_FUNC_H_
#define _JNI_FUNC_H_


#define MAKE_JNI_FUNC_NAME_FOR_SharedFuncLib(func)         Java_com_wangling_remotephone_SharedFuncLib_##func
#define MAKE_JNI_FUNC_NAME_FOR_MainListActivity(func)      Java_com_wangling_remotephone_MainListActivity_##func
#define MAKE_JNI_FUNC_NAME_FOR_MobileCameraActivity(func)  Java_com_wangling_remotephone_MobileCameraService_##func

#define MAKE_JNI_FUNC_NAME_FOR_SerialPort(func)         Java_com_wangling_remotephone_SerialPort_##func

#endif /* _JNI_FUNC_H_ */
