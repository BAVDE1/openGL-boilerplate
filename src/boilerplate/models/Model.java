package boilerplate.models;

import boilerplate.rendering.ShaderProgram;
import boilerplate.rendering.textures.Texture2d;
import boilerplate.utility.Logging;
import org.lwjgl.assimp.AIScene;
import org.lwjgl.assimp.Assimp;

import java.io.File;

public class Model {
    String directory;
    Mesh[] meshes;
    Texture2d[] texturesLoaded;

    public Model() {
    }

    public void loadModel(String filePath) {
        File f = new File(filePath);
        if (!f.isFile()) {
            Logging.danger("Given filePath is not a valid file %s", filePath);
            return;
        }

        // https://assimp-docs.readthedocs.io/en/latest/usage/use_the_lib.html
        try (AIScene scene = Assimp.aiImportFile(filePath,
                Assimp.aiProcess_Triangulate |  // handles concave polygons
                        Assimp.aiProcess_GenSmoothNormals |
                        Assimp.aiProcess_FlipUVs |  // make upper left corner 0, 0
                        Assimp.aiProcess_CalcTangentSpace)) {
            if (scene == null) {
                Logging.danger("Failed to load model\n%s", Assimp.aiGetErrorString());
                return;
            }
            processScene(scene);
        }
    }

    public void processScene(AIScene scene) {

    }

    public void draw(ShaderProgram shaderProgram) {
        shaderProgram.bind();
        for (Mesh mesh : meshes) mesh.draw(shaderProgram);
    }
}
