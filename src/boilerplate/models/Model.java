package boilerplate.models;

import boilerplate.rendering.ShaderProgram;
import boilerplate.rendering.textures.Texture2d;
import boilerplate.utility.Logging;
import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.*;

import java.io.File;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

public class Model {
    String directory;
    Mesh[] meshes;
    Texture2d[] texturesLoaded;

    AIMesh[] processedMeshes;

    public Model() {
    }

    public void loadModel(String filePath) {
        File file = new File(filePath);
        if (!file.isFile()) {
            Logging.danger("Given filePath is not a valid file %s", filePath);
            return;
        }

        directory = file.getParent();

        // https://assimp-docs.readthedocs.io/en/latest/usage/use_the_lib.html
        try (AIScene scene = Assimp.aiImportFile(filePath,
                Assimp.aiProcess_Triangulate |  // handles concave polygons
                        Assimp.aiProcess_GenSmoothNormals |
                        Assimp.aiProcess_FlipUVs |  // make upper left corner 0, 0
                        Assimp.aiProcess_CalcTangentSpace)) {
            boolean failed = true;
            String failedMsg = "no message";
            if (scene == null) {
                failedMsg = "scene is null";
            } else if (scene.mFlags() == Assimp.AI_SCENE_FLAGS_INCOMPLETE) {
                failedMsg = "scene is flagged as incomplete";
            } else if (scene.mRootNode() == null) {
                failedMsg = "root node is missing";
            } else failed = false;
            if (failed) {
                Logging.danger("Failed to load scene, %s\n%s", failedMsg, Assimp.aiGetErrorString());
                return;
            }

            processedMeshes = new AIMesh[scene.mNumMeshes()];
            processNode(scene.mRootNode(), scene);
        }
    }

    /**
     * Recursively process a node and its children
     */
    private void processNode(AINode node, AIScene rootScene) {
        if (node == null) {
            Logging.warn("Node is null, scene: %s", rootScene);
            return;
        }

        // meshes
        PointerBuffer allMeshes = rootScene.mMeshes();
        IntBuffer nodeMeshes = node.mMeshes();  // indexes of scene's meshes
        if (allMeshes != null && nodeMeshes != null) {
            for (int i = 0; i < node.mNumMeshes(); i++) {
                int meshInx = nodeMeshes.get(i);
                if (processedMeshes[meshInx] != null) continue;  // already processed this one

                try (AIMesh mesh = AIMesh.create(allMeshes.get(meshInx))) {
                    processedMeshes[meshInx] = mesh;
                    processMesh(mesh, rootScene);
                }
            }
        }

        // process children
        PointerBuffer children = node.mChildren();
        if (children == null) return;  // no children

        for (int i = 0; i < node.mNumChildren(); i++) {
            try (AINode child = AINode.create(children.get(i))) {
                processNode(child, rootScene);
            }
        }
    }

    private void processMesh(AIMesh mesh, AIScene rootScene) {
        List<Float> vertices = new ArrayList<>();
        List<Float> normals = new ArrayList<>();

        processVerticesAndNormals(mesh, vertices, normals);
    }

    private void processVerticesAndNormals(AIMesh mesh, List<Float> vertices, List<Float> normals) {
        AIVector3D.Buffer allVertices = mesh.mVertices();
        AIVector3D.Buffer allNormals = mesh.mNormals();
        while (allVertices.hasRemaining()) {
            AIVector3D vertex = allVertices.get();
            vertices.add(vertex.x());
            vertices.add(vertex.y());
            vertices.add(vertex.z());
            if (allNormals != null) {
                AIVector3D normal = allNormals.get();
                normals.add(normal.x());
                normals.add(normal.y());
                normals.add(normal.z());
            }
        }
    }

    public void draw(ShaderProgram shaderProgram) {
        shaderProgram.bind();
        for (Mesh mesh : meshes) mesh.draw(shaderProgram);
    }
}
