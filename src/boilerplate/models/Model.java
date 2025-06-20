package boilerplate.models;

import boilerplate.rendering.ShaderProgram;
import boilerplate.rendering.buffers.VertexLayout;
import boilerplate.rendering.textures.Texture2d;
import boilerplate.utility.Logging;
import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.*;

import java.io.File;
import java.nio.IntBuffer;

public class Model {
    String directory;
    VertexLayout vertexLayout = defaultVertexLayout();

    Mesh[] meshes;
    Texture2d[] texturesLoaded;

    public Model() {
    }

    public Model(VertexLayout vertexLayout) {
        this.vertexLayout = vertexLayout;
    }

    public void loadModel(String filePath) {
        Logging.debug("Attempting to load model %s", filePath);

        File file = new File(filePath);
        if (!file.isFile()) {
            Logging.danger("Given filePath is not a valid file %s", filePath);
            return;
        }

        directory = file.getParent();

        // https://assimp-docs.readthedocs.io/en/latest/usage/use_the_lib.html
        try (AIScene aiScene = Assimp.aiImportFile(filePath,
                Assimp.aiProcess_Triangulate |  // handles concave polygons
                        Assimp.aiProcess_GenSmoothNormals |
                        Assimp.aiProcess_FlipUVs |  // make upper left corner 0, 0
                        Assimp.aiProcess_CalcTangentSpace)) {
            boolean failed = true;
            String failedMsg = "no message";
            if (aiScene == null) {
                failedMsg = "scene is null";
            } else if (aiScene.mFlags() == Assimp.AI_SCENE_FLAGS_INCOMPLETE) {
                failedMsg = "scene is flagged as incomplete";
            } else if (aiScene.mRootNode() == null) {
                failedMsg = "root node is missing";
            } else failed = false;
            if (failed) {
                Logging.danger("Failed to load scene, %s\n%s", failedMsg, Assimp.aiGetErrorString());
                return;
            }

            meshes = new Mesh[aiScene.mNumMeshes()];
            processNode(aiScene.mRootNode(), aiScene);
        }
    }

    /**
     * Recursively process a node and its children
     */
    private void processNode(AINode node, AIScene rootAiScene) {
        if (node == null) {
            Logging.warn("Node is null, scene: %s", rootAiScene);
            return;
        }

        // meshes
        PointerBuffer allMeshes = rootAiScene.mMeshes();
        IntBuffer nodeMeshes = node.mMeshes();  // indexes of scene's meshes
        if (allMeshes != null && nodeMeshes != null) {
            for (int i = 0; i < node.mNumMeshes(); i++) {
                int meshInx = nodeMeshes.get(i);
                if (meshes[meshInx] != null) continue;  // already processed this one lol

                try (AIMesh aiMesh = AIMesh.create(allMeshes.get(meshInx))) {
                    meshes[meshInx] = processMesh(aiMesh, rootAiScene);
                }
            }
        }

        // process children
        PointerBuffer children = node.mChildren();
        if (children == null) return;  // no children :(

        for (int i = 0; i < node.mNumChildren(); i++) {
            try (AINode child = AINode.create(children.get(i))) {
                processNode(child, rootAiScene);
            }
        }
    }

    private Mesh processMesh(AIMesh aiMesh, AIScene rootAiScene) {
        Mesh mesh = new Mesh(vertexLayout);
        mesh.allocateMemory(calculateVertexDataBytes(aiMesh), calculateIndicesBytes(aiMesh));
        processVertices(mesh, aiMesh);
        processFaces(mesh, aiMesh);
        processTextures(mesh, aiMesh);
        mesh.finalizeMesh();
        return mesh;
    }

    private int calculateVertexDataBytes(AIMesh aiMesh) {
        return aiMesh.mNumVertices() * vertexLayout.stride;
    }

    private int calculateIndicesBytes(AIMesh aiMesh) {
        int bytes = 0;
        for (int fi = 0; fi < aiMesh.mNumFaces(); fi++) {
            AIFace face = aiMesh.mFaces().get(fi);
            bytes += face.mNumIndices() * Integer.BYTES;
        }
        return bytes;
    }

    private void processVertices(Mesh mesh, AIMesh aiMesh) {
        mesh.vertexCount = aiMesh.mNumVertices();
        AIVector3D.Buffer allVertices = aiMesh.mVertices();
        AIVector3D.Buffer allNormals = aiMesh.mNormals();
        AIVector3D.Buffer allTexPos = aiMesh.mTextureCoords(0);

        while (allVertices.hasRemaining()) {
            processVertex(allVertices.get(), mesh, allNormals, allTexPos);
        }
    }

    private void processFaces(Mesh mesh, AIMesh aiMesh) {
        AIFace.Buffer allFaces = aiMesh.mFaces();

        while (allFaces.hasRemaining()) {
            IntBuffer indices = allFaces.get().mIndices();
            while (indices.hasRemaining()) mesh.pushIndice(indices.get());
        }
    }

    private void processVertex(AIVector3D vertex, Mesh mesh, AIVector3D.Buffer allNormals, AIVector3D.Buffer allTexPos) {
        for (VertexLayout.Element element : vertexLayout.elements) {
            switch (element.hint) {
                case (VertexLayout.HINT_POSITION) -> mesh.pushVector3D(vertex);
                case (VertexLayout.HINT_NORMAL) -> {
                    if (allNormals == null)
                        throw new RuntimeException("Given vertex layout requires normals, but mesh data does not contain normals.");
                    mesh.pushVector3D(allNormals.get());
                }
                case (VertexLayout.HINT_TEX_POS) -> {
                    if (allTexPos == null)
                        throw new RuntimeException("Given vertex layout requires texture coords, but mesh data does not contain texture coords.");
                    mesh.pushVector2D(allTexPos.get());
                }
                case (VertexLayout.HINT_NULL) ->
                        throw new RuntimeException("Element from given VertexLayout is missing a hint value.");
            }
        }
    }

    public void processTextures(Mesh mesh, AIMesh aiMesh) {

    }

    public void draw(ShaderProgram shaderProgram) {
        shaderProgram.bind();
        for (Mesh mesh : meshes) mesh.draw();
    }

    public static VertexLayout defaultVertexLayout() {
        return new VertexLayout(
                new VertexLayout.Element(VertexLayout.TYPE_FLOAT, 3, VertexLayout.HINT_POSITION),
                new VertexLayout.Element(VertexLayout.TYPE_FLOAT, 3, VertexLayout.HINT_NORMAL),
                new VertexLayout.Element(VertexLayout.TYPE_FLOAT, 2, VertexLayout.HINT_TEX_POS)
        );
    }
}
