# Pathtracer in Java
This pathtracer was created entirely in Java, on and off over the course of about 6 months. It utilizes Java libraries (primarily swing) to render and receive input. It has a very powerful system of objects which allows easy control over the environment. Creation and material editting of objects is not yet present.

## Features
  Primarily built around a Monte-Carlo pathtracer
  - A complex material system, allowing for objects like metals, glasses, water, lights, and plastics
    - Supports diffuse and specular reflections,
    - Supports refraction and variable IORs
    - Supports plastics and other materials with probabilistic specularity
  - A BVH to accelerate pathtracing of 3d Meshs
    - Construction via a Surface Area Heuristic to construct BVHs well
  - A .obj model loader, currently not supporting textures, but supports normals
  - An implicit equation loader, able to load implicit surfaces with arbitrary surfaces
  - A builtin Rasterizer to allow position of a scene at a higher fps (`[` and `]` to enable / disable the pathtracer)
  - A screenshotter `k`
  - Control of Camera and objects is done through `wasd`, space and shift. (for movement). and arrow keys, `q` and `e` (for rotations)

## Preqs
  Java 25. (I think java 21 would work too but I haven't tried)
  Linux or Windows Machine
  Find your own models