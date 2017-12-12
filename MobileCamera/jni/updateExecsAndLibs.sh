#!/bin/bash

cd $(dirname $0)

cp -frv ../../MobileCamera_Cam/libs/* ../libs


echo libs/armeabi/libpocketsphinx_jni.so
cp -f  ../libpocketsphinx_jni.so       ../libs/armeabi/libpocketsphinx_jni.so

echo libs/armeabi-v7a/libpocketsphinx_jni.so
cp -f  ../libpocketsphinx_jni_v7a.so   ../libs/armeabi-v7a/libpocketsphinx_jni.so


echo libs/armeabi/libqxsignature-v1.1.0.so
cp -f  ../libqxsignature-v1.1.0.so     ../libs/armeabi/libqxsignature-v1.1.0.so

echo libs/armeabi-v7a/libqxsignature-v1.1.0.so
cp -f  ../libqxsignature-v1.1.0.so     ../libs/armeabi-v7a/libqxsignature-v1.1.0.so

echo Done.
