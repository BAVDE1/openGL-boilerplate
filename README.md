# some 2d openGL "boilerplate"

abstraction and demonstration of openGl and glfw (from the lwjgl module).

---

lwjgl: https://www.lwjgl.org/ \
openGL: https://docs.gl/

Also please see: References.md

---

### some information

VBO has its own `VertexBuffer`, VAO has `VertexArray`, layouts for `VertexArray` can be created with `VertexArray.Layout`.

To pass data onto the `VertexBuffer` use a `BufferBuilder`.

### Layout

Create a `VertexArray`'s layout with `VertexArray.Layout`.

An optional static default layout can be set and used to quickly create that layout wherever.

### BufferBuilder2f

Used for batching things & abstraction of float arrays.

Can be initialized with a set buffer size (amount of floats it can hold) or can auto-resize itself.

The 2f signifies that it assumes every vertex starts with 2 vertices at location 0 (for x and y position)
To allow for any VAO layout, call `BufferBuilder2f.setAdditionalVertFloats(n)` where n is the number of floats in the vertex's `Layout` minus 2 (again, as x and y are assumed).

> Separations can be used for batching collections of "disconnected" vertices into one buffer.
> 
> A separation between a new collection of vertices and the existing vertices in the buffer is simply 2 extra in-between vertices: the last vertex on the buffer and the first vertex of the collection being added.
> 
> For an example, each letter represents a unique vertex. current buffer: `abc`, vertices to be added: `def`. after adding the new `def` vertices with a separation, the buffer will look like: `abccddef`. The separation is the `cd` located between the first `c` and the last `d`.

`BufferBuildsr` also keeps track of some useful stats like:
* float count
* vertex count
* separations count
* buffer size
* fullness percentage
* is auto resizing

And some other lower level functions:
* `getFloatsSlice`  (Returns a slice of the current floats in the buffer)
* `getLastVertices`  (Returns the last N vertices in the buffer)
* `pushRawVertices`  (push floats onto the end of the buffer with safety checks)
* `setFloatsUnsafe`  (set floats anywhere in the buffer with no safety checks)
* `resizeBufferAndKeepElements` & `resizeBufferAndWipe`
* `appendBuffer` & `prependBuffer`

### Shape2d

Easy creation of a few 2d shapes:
* Rect
* Rect outline
* Line

These all return a `Shape2d.Poly` object that contains a list of that shapes' points.

> The list of 2d points in a `Shape2d.Poly` can be sorted with Shape2d.sortPoints()
> 
> This sorts all points in a clockwise direction.

`BufferBuilder2f` can put polygons into their buffer with `pushPolygon` (pushes every point retaining their order) and `pushPolygonSorted` (pushes every point by this pattern: first, last, first+1, last-1, first+2, last-2...).

A shape can also be given a ShapeMode. ShapeMode is to accommodate custom vertex layouts, since without a mode the entire vertex of a point will only be its `x` and `y`.

> ShapeModes assume the `x` and `y` will always be the first 2 floats in a vertex.

ShapeModes:
* Append  (appends a list of floats to the end of each vertex)
* Unpack  (Unpacks the list of list of floats to the end of each vertex (wraps). So point 0 of the shape will append `list[0]` to its vertex, and point 1 will append `list[1]`, point 2 `list[2]`, and so on)
* AppendUnpack  (appends and then unpacks)
* UnpackAppend  (unpacks and then appends)

### Text Rendering

`FontManager` manages the currently loaded fonts that can be used in any `TextRenderer`.
the `FontManager`'s fonts should be loaded at runtime before any text rendering is attempted. use `init()` before loading any font, and `generateAndBindAllFonts()` to complete the loading.

`FontManager` also contains the shader and vertex layout all `TextRenderer`s use.
But these are set up and initialized automatically.

`TextRenderer` takes any number `TextRenderer.TextObject`s and buffers them all together for easy rendering. A `TextRenderer`s buffer is only rebuilt when one of its `TextObjects` is modified. or if one is added to removed.

Some `TextObject` values that can be set include:
* string
* font
* pos
* scale
* text colour
* text alignment
* line y spacing
* bg colour (with an alpha of 0, a bg won't be added)
* bg margin
* are bg lines seamless (for if line y spacing is greater than 0)

**Note:** there is 1 default font thats loaded (at font id 0) before anything else (also italicised fonts are kinda broken atm)

### Shader Helper

`ShaderHelper` instance loads, compiles & links shaders from any directory or file. Recommended minimum of 1 vertex and 1 fragment shader.

Multiple shaders can be in one file, they just need to be separated with a `//--- <SHADER_TYPE>` line (and use the `multi` function)

Supported shader types are `VERT`, `TESC`, `TECE`, `GEOM`, `FRAG`.

Can also be used to send uniforms to its attached shaders.

### Renderer

`Renderer` is just a state machine that handles currently bound `VertexBuffers`, `VertexArrays` and `ShaderHelpers`

It can also be used do "draw" buffers, instanced buffers, and `TextRenderers`. (stats from a related BufferBuilder can be used to provide values for rendering)

### Texture

`Texture` loads an image from a file path or a `BufferedImage` that can be bound and used.

Use texture slots that are > 1.

**Note:** Texture slot 1 is reserved by the `FontManager` so don't use that.

### Circles

in this project, they need their own VAO/VBO cause they're special and are instanced.

### Logger

theres a logger cause why not. also it has some pretty colours :)

---

supports windows & linux
