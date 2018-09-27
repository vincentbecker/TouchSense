#include <jni.h>

extern "C"
JNIEXPORT jfloat JNICALL Java_ch_ethz_inf_vs_fingerforce_machinelearning_FeatureExtraction_meanFromJNI(
        JNIEnv *env,
        jobject /* this */,
        jbyteArray input) {
    int length = env->GetArrayLength(input);
    jfloat mean = 0;
    jboolean inputCopy = JNI_FALSE;
    jbyte* pointer = env->GetByteArrayElements(input, &inputCopy);
    for (int i = 0; i < length; i++) {
        const signed char current = *pointer++;
        mean += current;
    }
    // Calculate the mean
    mean = mean / length;

    env->ReleaseByteArrayElements(input, pointer, JNI_ABORT);
    return mean;
}