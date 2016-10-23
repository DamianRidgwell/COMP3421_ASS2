#version 130

//for textures
out vec2 texCoordV;

out vec3 N; //normal vector relative to camera to be sent to fragment shader
out vec3 v; //position vector relative to camera to be sent to fragment shader
out vec4 diffuse; //diffuse lighting passed on to fragment shader
out vec4 globalAmbient; //globalAmbient lighting passed on to fragment shader
out vec4 ambient; //ambient lighting passed on to fragment shader

//assuming there is only one light source
void main() {
    vec3 v, N, lightDir;

    //convert position into eye space coordinates
    v = vec3(gl_ModelViewMatrix * gl_Vertex);
    //convert normal into eye space and normalise
    N = normalize(gl_NormalMatrix * gl_Normal);

    gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;

    //ambient light calculations
    vec4 globalAmbient = gl_LightModel.ambient * gl_FrontMaterial.ambient;
    vec4 ambient = gl_LightSource[0].ambient * gl_FrontMaterial.ambient;

    //get the vector to the light
   lightDir = normalize(gl_LightSource[0].position.xyz - v);

   float NdotL = max(dot(N, lightDir), 0.0);

   vec4 diffuse = NdotL * gl_FrontMaterial.diffuse * gl_LightSource[0].diffuse;

   //pass on texture coord to be used in fragment shading
   texCoordV = vec2(gl_MultiTexCoord0);
}