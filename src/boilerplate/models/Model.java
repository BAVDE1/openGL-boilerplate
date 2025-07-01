package boilerplate.models;

import boilerplate.rendering.ShaderProgram;
import boilerplate.rendering.buffers.VertexLayout;
import boilerplate.rendering.textures.Texture2d;
import boilerplate.utility.Logging;
import boilerplate.utility.MathUtils;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.*;
import org.lwjgl.opengl.GL45;

import java.io.File;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Model {
    public static final int MAX_BONE_INFLUENCE = 4;

    public static class NodeData {
        String name;
        Matrix4f transform;
        List<NodeData> children = new ArrayList<>();

        @Override
        public String toString() {
            return "NodeData(" +
                    "name='" + name + '\'' +
                    ", children=" + children +
                    ')';
        }
    }

    public static class VertexWeight {
        int boneId = -1;
        float weight = 0;

        public VertexWeight() {
        }

        public VertexWeight(int boneId, float weight) {
            this.boneId = boneId;
            this.weight = weight;
        }

        @Override
        public String toString() {
            return "VertexWeight(%s, %s)".formatted(boneId, weight);
        }
    }

    String modelFile;
    String directory;
    VertexLayout vertexLayout = defaultVertexLayout();
    public Animator animator = new Animator(this);

    private final NodeData rootNode = new NodeData();
    Mesh[] meshes;
    Material[] materials;
    HashMap<String, Bone> boneMap = new HashMap<>();
    private int boneCounter = 0;

    private boolean renderWireFrame = false;

    public Matrix4f modelTransform = new Matrix4f().identity();
    public boolean modelTransformChanged = true;

    public Matrix4f globalInverseTransform;

    public Model() {
    }

    public Model(VertexLayout vertexLayout) {
        this.vertexLayout = vertexLayout;
    }

    public void loadModel(String filePath, boolean flipTextures) {
        Logging.debug("Attempting to load model '%s'", filePath);

        File file = new File(filePath);
        if (!file.isFile()) {
            Logging.danger("Given filePath is not a valid file '%s'", filePath);
            return;
        }

        modelFile = filePath;
        directory = file.getParent();

        // https://assimp-docs.readthedocs.io/en/latest/usage/use_the_lib.html
        try (AIScene aiScene = Assimp.aiImportFile(filePath,
                Assimp.aiProcess_Triangulate |  // handles concave polygons
                        Assimp.aiProcess_GenSmoothNormals |
                        (flipTextures ? Assimp.aiProcess_FlipUVs : 0) |  // make upper left corner 0, 0
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

            processScene(aiScene);
        }
    }

    private void processScene(AIScene rootAiScene) {
        // materials
        materials = new Material[rootAiScene.mNumMaterials()];
        processMaterials(rootAiScene);

        // node hierarchy & model
        meshes = new Mesh[rootAiScene.mNumMeshes()];
        processNode(rootAiScene.mRootNode(), rootAiScene, rootNode);

        // animations
        animator.init(boneCounter, rootNode);
        processAnimations(rootAiScene);
    }

    /**
     * Recursively process a node and its children
     */
    private void processNode(AINode aiNode, AIScene rootAiScene, NodeData nodeDest) {
        if (aiNode == null) {
            Logging.warn("Node is null, scene: %s", rootAiScene);
            return;
        }

        // node hierarchy
        nodeDest.name = aiNode.mName().dataString();
        nodeDest.transform = MathUtils.AIMatrixToMatrix(aiNode.mTransformation());

        // meshes
        PointerBuffer allMeshes = rootAiScene.mMeshes();
        IntBuffer nodeMeshes = aiNode.mMeshes();  // indexes of scene's meshes
        if (allMeshes != null && nodeMeshes != null) {
            while (nodeMeshes.hasRemaining()) {
                int meshInx = nodeMeshes.get();
                try (AIMesh aiMesh = AIMesh.create(allMeshes.get(meshInx))) {
                    meshes[meshInx] = processMesh(aiMesh);
                }
            }
        }

        // process children
        PointerBuffer children = aiNode.mChildren();
        if (children == null) return;  // no children :(

        for (int i = 0; i < aiNode.mNumChildren(); i++) {
            try (AINode child = AINode.create(children.get(i))) {
                NodeData childNode = new NodeData();
                processNode(child, rootAiScene, childNode);
                nodeDest.children.add(childNode);
            }
        }
    }

    private Mesh processMesh(AIMesh aiMesh) {
        Mesh mesh = new Mesh(vertexLayout);
        mesh.indicesCount = findIndicesCount(aiMesh);
        mesh.allocateMemory(calculateVertexDataBytes(aiMesh), mesh.indicesCount * Integer.BYTES);

        processBones(mesh, aiMesh);
        processVertices(mesh, aiMesh);
        processFaces(mesh, aiMesh);
        processMeshMaterial(mesh, aiMesh);

        mesh.finalizeMesh();
        return mesh;
    }

    private int calculateVertexDataBytes(AIMesh aiMesh) {
        return aiMesh.mNumVertices() * vertexLayout.stride;
    }

    private int findIndicesCount(AIMesh aiMesh) {
        int count = 0;
        for (int fi = 0; fi < aiMesh.mNumFaces(); fi++) {
            count += aiMesh.mFaces().get(fi).mNumIndices();
        }
        return count;
    }

    /**
     * After process bones
     */
    private void processVertices(Mesh mesh, AIMesh aiMesh) {
        AIVector3D.Buffer allVertices = aiMesh.mVertices();
        AIVector3D.Buffer allNormals = aiMesh.mNormals();
        AIVector3D.Buffer allTexPos = aiMesh.mTextureCoords(0);

        // data checks
        if (allNormals == null && vertexLayout.hasElementWithHint(VertexLayout.HINT_NORMAL))
            throw new RuntimeException("Given vertex layout contains normals, but mesh data does not contain normals.");
        if (allTexPos == null && vertexLayout.hasElementWithHint(VertexLayout.HINT_TEX_POS))
            throw new RuntimeException("Given vertex layout contains texture coords, but mesh data does not contain texture coords.");

        // process
        for (int i = 0; i < aiMesh.mNumVertices(); i++) {
            processVertex(mesh, i, allVertices, allNormals, allTexPos);
        }
    }

    private void processFaces(Mesh mesh, AIMesh aiMesh) {
        AIFace.Buffer allFaces = aiMesh.mFaces();

        while (allFaces.hasRemaining()) {
            IntBuffer indices = allFaces.get().mIndices();
            while (indices.hasRemaining()) mesh.pushIndice(indices.get());
        }
    }

    private void processBones(Mesh mesh, AIMesh aiMesh) {
        PointerBuffer allBones = aiMesh.mBones();
        if (allBones == null) return;  // no bones

        while (allBones.hasRemaining()) {
            try (AIBone aiBone = AIBone.create(allBones.get())) {
                String boneName = aiBone.mName().dataString();
                Bone bone = boneMap.computeIfAbsent(boneName, _ -> new Bone(boneCounter++, aiBone));

                AIVertexWeight.Buffer weights = aiBone.mWeights();
                while (weights.hasRemaining()) {
                    AIVertexWeight aiWeight = weights.get();
                    int vertexId = aiWeight.mVertexId();
                    float weight = aiWeight.mWeight();
                    if (weight == 0) continue;  // no need to even add the bone

                    List<VertexWeight> vwList = mesh.vertexWeights.computeIfAbsent(vertexId, _ -> new ArrayList<>());
                    vwList.add(new VertexWeight(bone.id, weight));
                }
            }
        }
    }

    /**
     * After process bones
     */
    private void processAnimations(AIScene rootAIScene) {
        PointerBuffer allAnimations = rootAIScene.mAnimations();
        if (allAnimations == null) return;  // no animations

        while (allAnimations.hasRemaining()) {
            try (AIAnimation aiAnimation = AIAnimation.create(allAnimations.get())) {
                Animation animation = new Animation(aiAnimation, this);
                Logging.info("new animation, %s", animation.name);
                animator.addAnimation(animation);
            }
        }
    }

    /**
     * After process bones
     */
    private void processVertex(Mesh mesh, int vertexInx, AIVector3D.Buffer allVertices, AIVector3D.Buffer allNormals, AIVector3D.Buffer allTexPos) {
        for (VertexLayout.Element element : vertexLayout.elements) {
            switch (element.hint) {
                case (VertexLayout.HINT_POSITION) -> mesh.pushVector3D(allVertices.get(vertexInx));
                case (VertexLayout.HINT_NORMAL) -> mesh.pushVector3D(allNormals.get(vertexInx));
                case (VertexLayout.HINT_TEX_POS) -> mesh.pushVector2D(allTexPos.get(vertexInx));
                case (VertexLayout.HINT_BONE_IDS) -> pushVertexBoneIds(mesh, vertexInx);
                case (VertexLayout.HINT_BONE_WEIGHTS) -> pushVertexBoneWeights(mesh, vertexInx);
                default -> throw new RuntimeException("Element from given VertexLayout is missing a hint value.");
            }
        }
    }

    private void pushVertexBoneIds(Mesh mesh, int vertexInx) {
        List<VertexWeight> vwList = mesh.vertexWeights.get(vertexInx);
//        System.out.println(vwList);
        for (int i = 0; i < MAX_BONE_INFLUENCE; i++) {
            if (vwList != null && i < vwList.size()) mesh.pushInt(vwList.get(i).boneId);
            else mesh.pushInt(-1);
        }
    }

    private void pushVertexBoneWeights(Mesh mesh, int vertexInx) {
        List<VertexWeight> vwList = mesh.vertexWeights.get(vertexInx);
        for (int i = 0; i < MAX_BONE_INFLUENCE; i++) {
            if (vwList != null && i < vwList.size()) mesh.pushFloat(vwList.get(i).weight);
            else mesh.pushFloat(0);
        }
    }

    private void processMaterials(AIScene rootAiScene) {
        PointerBuffer allMaterials = rootAiScene.mMaterials();
        if (allMaterials == null) return;

        for (int mi = 0; mi < rootAiScene.mNumMaterials(); mi++) {
            try (AIMaterial material = AIMaterial.create(allMaterials.get(mi))) {
                materials[mi] = processMaterial(material);
            }
        }
    }

    private Material processMaterial(AIMaterial aiMaterial) {
        AIString path = AIString.calloc();
        Assimp.aiGetMaterialTexture(aiMaterial, Assimp.aiTextureType_DIFFUSE, 0, path, (IntBuffer) null, null, null, null, null, null);
        String textPath = path.dataString();
        Texture2d texture = null;
        if (!textPath.isEmpty()) {
            texture = new Texture2d(directory + "/" + textPath);
        }

        Vector4f ambient = getMaterialColour(aiMaterial, Assimp.AI_MATKEY_COLOR_AMBIENT);
        Vector4f diffuse = getMaterialColour(aiMaterial, Assimp.AI_MATKEY_COLOR_DIFFUSE);
        Vector4f specular = getMaterialColour(aiMaterial, Assimp.AI_MATKEY_COLOR_SPECULAR);
        return new Material(ambient, diffuse, specular, texture);
    }

    private Vector4f getMaterialColour(AIMaterial aiMaterial, String type) {
        AIColor4D colBuff = AIColor4D.create();
        Vector4f col = Material.DEFAULT_COLOUR;
        int result = Assimp.aiGetMaterialColor(aiMaterial, type, Assimp.aiTextureType_NONE, 0, colBuff);
        if (result == 0) col = new Vector4f(colBuff.r(), colBuff.g(), colBuff.b(), colBuff.a());
        return col;
    }

    private void processMeshMaterial(Mesh mesh, AIMesh aiMesh) {
        int matInx = aiMesh.mMaterialIndex();
        if (matInx >= 0 && matInx < materials.length) {
            mesh.setMaterial(materials[matInx]);
        }
    }

    public void updateAnimation(double dt) {
        animator.update((float) dt);
    }

    public void draw(ShaderProgram shaderProgram) {
        shaderProgram.bind();

        for (int i = 0; i < boneCounter; i++) {
            shaderProgram.uniformMatrix4f("finalBonesMatrices[%s]".formatted(i), animator.finalBoneMatrices[i]);
        }

        if (modelTransformChanged) {
            shaderProgram.uniformMatrix4f("model", modelTransform);
            modelTransformChanged = false;
        }
        for (Mesh mesh : meshes) mesh.draw();
    }

    public void renderWireFrame(boolean val) {
        if (val == renderWireFrame) return;
        renderWireFrame = val;
        int mode = val ? GL45.GL_LINES : GL45.GL_TRIANGLES;
        for (Mesh mesh : meshes) mesh.renderMode = mode;
    }

    public Bone getBone(String boneName) {
        if (!boneMap.containsKey(boneName)) return null;
        return boneMap.get(boneName);
    }

    public static VertexLayout defaultVertexLayout() {
        return new VertexLayout(
                new VertexLayout.Element(VertexLayout.TYPE_FLOAT, 3, VertexLayout.HINT_POSITION),
                new VertexLayout.Element(VertexLayout.TYPE_FLOAT, 3, VertexLayout.HINT_NORMAL),
                new VertexLayout.Element(VertexLayout.TYPE_FLOAT, 2, VertexLayout.HINT_TEX_POS),
                new VertexLayout.Element(VertexLayout.TYPE_INT, MAX_BONE_INFLUENCE, VertexLayout.HINT_BONE_IDS),
                new VertexLayout.Element(VertexLayout.TYPE_FLOAT, MAX_BONE_INFLUENCE, VertexLayout.HINT_BONE_WEIGHTS)
        );
    }

    @Override
    public String toString() {
        return "Model(" +
                "modelFile='" + modelFile + '\'' +
                ')';
    }
}
