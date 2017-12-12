# Copyright (C) 2009 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#


LOCAL_PATH:= $(call my-dir)



# lib colorspace
################################################################################
include $(CLEAR_VARS)

LOCAL_MODULE    := colorspace
LOCAL_C_INCLUDES += $(LOCAL_PATH)/colorspace
LOCAL_SRC_FILES := colorspace/colorspace.cpp

include $(BUILD_STATIC_LIBRARY)



# lib video_cv
################################################################################
include $(CLEAR_VARS)


OPENCV_LIB_TYPE:=STATIC
OPENCV_INSTALL_MODULES:=off
OPENCV_CAMERA_MODULES:=off

OPENCV_MK_PATH:=../../G_Projects/OpenCV-2.4.0/share/OpenCV/OpenCV.mk
include $(OPENCV_MK_PATH)


LOCAL_MODULE    := video_cv
LOCAL_C_INCLUDES += $(LOCAL_PATH)/colorspace
LOCAL_SRC_FILES := jni_if.cpp  video_cv.cpp
LOCAL_CPP_FEATURES += rtti exceptions
LOCAL_STATIC_LIBRARIES += colorspace
LOCAL_LDLIBS += -llog -ldl

include $(BUILD_SHARED_LIBRARY)



# lib serial_port
################################################################################
include $(CLEAR_VARS)

LOCAL_MODULE    := serial_port
LOCAL_C_INCLUDES += $(LOCAL_PATH)/serialport
LOCAL_SRC_FILES := serialport/SerialPort.c
LOCAL_LDLIBS    := -llog

include $(BUILD_SHARED_LIBRARY)