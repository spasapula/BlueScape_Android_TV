precision mediump float;

 varying vec4 vColor;
 varying vec2 vtexCoord;
 uniform sampler2D uSampler;

 void main() {
    vec4 texel = texture2D(uSampler, vtexCoord);
    if(texel.a < 0.5)
        discard;
    gl_FragColor = texture2D(uSampler, vtexCoord);
    gl_FragColor.rgb *= vColor.a;
 }