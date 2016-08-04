#pragma version(1)
#pragma rs java_package_name(com.github.wrdlbrnft.renderscriptsample)
#pragma rs_fp_relaxed

// Constant value used to calculate the dot product.
const static float3 gMonoMult = {0.299f, 0.587f, 0.114f};

// Determines the saturation level. Can be set at runtime with generated setter methods.
float saturationLevel = 0.0f;

// Definition of the saturation Kernel.
uchar4 __attribute__((kernel)) saturation(uchar4 in) {

    // Transform the input pixel with a value range of [0, 255] to
    // a float4 vector with a range of [0.0f, 1.0f]
    float4 f4 = rsUnpackColor8888(in);

    // Calculate the dot product of the rgb channels and the global constant we defined and
    // assign the result to each element in a float3 vector
    float3 dotVector = dot(f4.rgb, gMonoMult);

    // Mix the original color with dot product vector according to the saturationLevel.
    float3 newColor = mix(dotVector, f4.rgb, saturationLevel);

    // Transform the resulting color back to a uchar4 vector.
    // Since the input color is just a float3 instead of a float4 the alpha value will
    // be set to 255 or fully opaque.
    return rsPackColorTo8888(newColor);
}