fancy pathtracer i wrote over a month or so
has
  - generally a robust environment system
    - .obj model loader
    - implicit surface loader
    - individual control over all objects
  - bvh (not SAH)
  - specular reflection lerp (for metallic surfaces)
  - specular reflection chance (for plastic-like materials)
  - diffuse reflection
    - with cosine weighted rays
  - has refraction for glass-like surfaces
    - no internal diffuse refraction
  - color sample accumlation
  - a rasterizer built in so you can move around at a reasonable framerate while setting up the camera
  - \[ and \] to toggle pathtracing
  - k to save it to a png named saved.png
  - automatic file compacting of meshes to remove texture and normal stuff cuz i don't use them