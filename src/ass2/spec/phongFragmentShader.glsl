#version 130

//for shading textures
in vec2 texCoordV;
uniform sampler2D texSampler;

in vec3 N; //interpolated from vertex shader
in vec3 v; //interpolated from vertex shader
in vec4 diffuse; //diffuse lighting from vertex shader
in vec4 globalAmbient; //globalAmbient lighting from vertex shader
in vec4 ambient; //ambient lighting passed from vertex shader

void main (void) {
    vec3 lightDir = normalize(gl_LightSource[0].position.xyz - v);
    vec3 dirToView = normalize(-v);
    // normal has been interpolated so will need to be normalised
    vec3 normal = normalize(N);
    vec3 H = normalize(dirToView + lightDir);

    vec4 specular = vec4(0.0, 0.0, 0.0, 1.0);
    float NdotL = max(dot(normal, lightDir), 0.0);
    //compute the specular term if NdotL is larger than 0.0
    if(NdotL > 0.0) {
        float NdotH = max(dot(normal, H), 0.0);
        specular = gl_FrontMaterial.specular *
            gl_LightSource[0].specular *
            pow(NdotH, gl_FrontMaterial.shininess);
    }
    clamp(specular, 0, 1);

    gl_FragColor = texture(texSampler, texCoordV) * (gl_FrontMaterial.emission + globalAmbient + ambient + diffuse) + specular;
}