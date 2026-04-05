package dev.codanor.render.render_object;

import dev.codanor.render.render_object.exceptions.NoSuchAttributeException;
import dev.codanor.render.render_object.exceptions.NoSuchVertexException;
import org.joml.Vector2d;

import java.util.*;

public class Mesh {

    public Mesh(List<Vector2d> localCoordinates) {
        Attrib attrib;

        _vertexSize = 2;

        _data = new float[localCoordinates.size() * 2];
        for (int index = 0; index < localCoordinates.size(); index++) {
            _data[index * 2] = (float) localCoordinates.get(index).x;
            _data[index * 2 + 1] = (float) localCoordinates.get(index).y;
        }

        _indices = new int[0];

        attrib = new Attrib("localCoordinates", new float[]{0, 0}, 0);

        _ATTRIBUTES = new ArrayList<>();

        _ATTRIBUTES.add(attrib);

        _ON_CHANGED_CALLBACKS = new ArrayList<>();
    }

    public class Attrib {

        private Attrib(String name, float[] stdData, int localIndex) {
            NAME = name;

            STD_DATA = stdData;
            SIZE = stdData.length;

            _LOCAL_INDEX = localIndex;
        }

        public final String NAME;

        public final float[] STD_DATA;
        public final int SIZE;

        private final int _LOCAL_INDEX;

    }
    public class Vertex {

        private Vertex(int index, float[] data, ArrayList<Attrib> attribs, int size) {
            _INDEX = index;
            _DATA = data;

            _ATTRIBS = attribs;
            _SIZE = size;
        }

        private final int _INDEX;
        private final float[] _DATA;

        private final ArrayList<Attrib> _ATTRIBS;
        private final int _SIZE;

        public Vertex setAttrib(String name, float[] data) {
            Attrib attrib;

            if (name == null) throw new NoSuchAttributeException("[MESH ERROR] : Attribute's name cannot be null!");

            attrib = null;

            for (Attrib currentAttrib : _ATTRIBS) if (Objects.equals(currentAttrib.NAME, name)) attrib = currentAttrib;

            if (attrib == null) throw new NoSuchAttributeException("[MESH ERROR] : This mesh does not contain an attribute of the specified name!");
            if (attrib.SIZE != data.length) throw new IllegalArgumentException("[MESH ERROR] : The size of the specified _data does not equal the size required for the specified attribute!");

            System.arraycopy(data, 0, _DATA, attrib._LOCAL_INDEX + _INDEX, attrib.SIZE);

            h_notifyChange();

            return this;
        }
        public float[] getAttrib(String name) {
            Attrib attrib;

            if (name == null) throw new NoSuchAttributeException("[MESH ERROR] : Attribute's name cannot be null!");

            attrib = null;

            for (Attrib currentAttrib : _ATTRIBS) if (Objects.equals(currentAttrib.NAME, name)) attrib = currentAttrib;

            if (attrib == null) throw new NoSuchAttributeException("[MESH ERROR] : This mesh does not contain an attribute of the specified name!");

            return Arrays.copyOfRange(_DATA, _INDEX + attrib._LOCAL_INDEX, _INDEX + attrib._LOCAL_INDEX + attrib.SIZE);
        }

        public Vertex resetAttrib(String name) {
            Attrib attrib;

            if (name == null) throw new NoSuchAttributeException("[MESH ERROR] : Attribute's name cannot be null!");

            attrib = null;

            for (Attrib currentAttrib : _ATTRIBS) if (Objects.equals(currentAttrib.NAME, name)) attrib = currentAttrib;

            if (attrib == null) throw new NoSuchAttributeException("[MESH ERROR] : This mesh does not contain an attribute of the specified name!");

            System.arraycopy(attrib.STD_DATA, 0, _DATA, attrib._LOCAL_INDEX + _INDEX, attrib.SIZE);

            h_notifyChange();

            return this;
        }

        public Vertex set(float[] data) {
            if (data.length != _SIZE) throw new IllegalArgumentException("[MESH ERROR] : The size of the specified _data is not the same size as the vertex!");

            System.arraycopy(data, 0, _DATA, _INDEX, _SIZE);

            h_notifyChange();

            return this;
        }
        public float[] get() {
            return Arrays.copyOfRange(_DATA, _INDEX, _INDEX + _SIZE);
        }

        public String getFormatted() {
            float[] data;
            StringBuilder builder;
            int biggestAttribNameSize;

            data = get();
            builder = new StringBuilder("\n<------- VERTEX (" + ((_INDEX / _SIZE) + 1) + "/" + _DATA.length / _SIZE + ") (" + _INDEX / _SIZE + ") ------->\n");

            biggestAttribNameSize = 0;
            for (Attrib attrib : _ATTRIBS) if (attrib.NAME.length() >= biggestAttribNameSize) biggestAttribNameSize = attrib.NAME.length();

            for (int index = 0; index < _ATTRIBS.size(); index++) {
                Attrib attrib;

                attrib = _ATTRIBS.get(index);

                builder.append("|-> " + attrib.NAME);
                builder.append(" ".repeat(biggestAttribNameSize - attrib.NAME.length()));
                builder.append(" (" + index + ") : ");
                builder.append(Arrays.toString(Arrays.copyOfRange(_DATA, _INDEX + attrib._LOCAL_INDEX, _INDEX + attrib._LOCAL_INDEX + attrib.SIZE)) + '\n');
            }

            return builder.toString();
        }

    }
    public class Triangle {

        public Triangle(int[] indices, int index) {
            _INDICES = indices;
            _INDEX = index;
        }

        private final int[] _INDICES;
        private final int _INDEX;

        public void setVertex(int index, int to) {
            Vertex vertex;

            if (index > 2) throw new IndexOutOfBoundsException("[MESH ERROR] : A triangle can only ever have three indices!");

            vertex = getVertex(index);

            if (vertex == null) throw new NoSuchVertexException("[MESH ERROR] : This mesh does not contain a vertex at the specified index!");

            if (index != 0) if (vertex == getVertex(0)) throw new IllegalArgumentException("[MESH ERROR] : A triangle must consist of three different vertices!");
            if (index != 1) if (vertex == getVertex(1)) throw new IllegalArgumentException("[MESH ERROR] : A triangle must consist of three different vertices!");
            if (index != 2) if (vertex == getVertex(2)) throw new IllegalArgumentException("[MESH ERROR] : A triangle must consist of three different vertices!");

            _INDICES[_INDEX + index] = vertex._INDEX;

            h_notifyChange();
        }
        public Vertex getVertex(int index) {
            if (index > 2) throw new IndexOutOfBoundsException("[MESH ERROR] : A triangle can only ever have three indices!");

            return Mesh.this.getVertex(_INDEX + index);
        }

        public Vertex[] getVertices() {
            Vertex[] vertices;

            vertices = new Vertex[]{
                    getVertex(_INDEX),
                    getVertex(_INDEX + 1),
                    getVertex(_INDEX + 2),
            };

            return vertices;
        }
        public int[] getIndices() {
            int[] indices;

            System.arraycopy(_INDICES, _INDEX, (indices = new int[3]), 0, 3);

            return indices;
        }

    }

    private float[] _data;
    private int _vertexSize;

    private int[] _indices;

    private final ArrayList<Attrib> _ATTRIBUTES;

    private final ArrayList<Runnable> _ON_CHANGED_CALLBACKS;

    public Mesh addAttrib(String name, float[] stdData) {
        Attrib attrib;

        if (name == null) throw new IllegalArgumentException("[MESH ERROR] : Attribute's name cannot be null!");
        if (stdData == null) throw new IllegalArgumentException("[MESH ERROR] : Standard data cannot be null!");
        for (Attrib currentAttrib : _ATTRIBUTES) if (Objects.equals(currentAttrib.NAME, name)) throw new IllegalArgumentException("[MESH ERROR] : An attribute of the same name is already in this mesh!");

        attrib = new Attrib(name, stdData, _vertexSize);

        _vertexSize += attrib.SIZE;

        _ATTRIBUTES.add(attrib);

        // --- //

        int prevVertexSize, prevDataLength;
        float[] dataCopy;

        prevVertexSize = _vertexSize - attrib.SIZE;
        prevDataLength = _data.length;

        dataCopy = new float[(prevDataLength / prevVertexSize) * _vertexSize];

        for (int index = 0; index < prevDataLength / prevVertexSize; index++) {
            int oldBase, newBase;

            oldBase = index * prevVertexSize;
            newBase = index * _vertexSize;

            System.arraycopy(_data, oldBase, dataCopy, newBase, prevVertexSize);
            System.arraycopy(attrib.STD_DATA, 0, dataCopy, newBase + prevVertexSize, attrib.SIZE);
        }

        _data = dataCopy;

        h_notifyChange();

        return this;
    }

    public Mesh addVertex(Map<String, float[]> dataPerAttrib) {
        float[] dataCopy;

        dataCopy = new float[_data.length + _vertexSize];

        System.arraycopy(_data, 0, dataCopy, 0, _data.length);

        for (Attrib attrib : _ATTRIBUTES) {
            float[] data;

            data = dataPerAttrib.get(attrib.NAME);

            if (data == null) data = attrib.STD_DATA;
            else if (attrib.SIZE != data.length) throw new IllegalArgumentException("[MESH ERROR] : The size of the specified data is not equal to the size of the specified attribute!");

            System.arraycopy(data, 0, dataCopy, _data.length + attrib._LOCAL_INDEX, attrib.SIZE);
        }

        _data = dataCopy;

        h_notifyChange();

        return this;
    }
    public Mesh addVertex() {
        float[] dataCopy;

        dataCopy = new float[_data.length + _vertexSize];

        System.arraycopy(_data, 0, dataCopy, 0, _data.length);

        for (Attrib attrib : _ATTRIBUTES) {
            System.arraycopy(attrib.STD_DATA, 0, dataCopy, _data.length + attrib._LOCAL_INDEX, attrib.SIZE);
        }

        _data = dataCopy;

        h_notifyChange();

        return this;
    }

    public Mesh addTriangle(List<Vertex> vertices) {
        int[] vertexIndices;
        int[] indicesCopy;

        if (vertices.size() != 3) throw new IllegalArgumentException("[MESH ERROR] : A triangle can only ever have three vertices!");

        vertexIndices = new int[3];
        for (int index = 0; index < 3; index++) vertexIndices[index] = vertices.get(index)._INDEX;

        return addTriangle(vertexIndices);
    }
    public Mesh addTriangle(int[] vertexIndices) {
        int[] indicesCopy;

        if (vertexIndices.length != 3) throw new IllegalArgumentException("[MESH ERROR] : A triangle must consist of exactly three vertices!");

        if (vertexIndices[1] == vertexIndices[0] || vertexIndices[1] == vertexIndices[2] || vertexIndices[0] == vertexIndices[2]) throw new IllegalArgumentException("[MESH ERROR] : A triangle must consist of three different vertices!");

        indicesCopy = new int[_indices.length + 3];

        for (int index : vertexIndices) if (getVertex(index) == null) throw new NoSuchVertexException("[MESH ERROR] : This mesh does not contain a vertex at this index!");

        System.arraycopy(_indices, 0, indicesCopy, 0, _indices.length);
        System.arraycopy(vertexIndices, 0, indicesCopy, _indices.length, 3);

        _indices = indicesCopy;

        h_notifyChange();

        return this;
    }

    public int getAttribSize(String name) {
        Attrib attrib;

        attrib = null;

        for (Attrib currentAttrib : _ATTRIBUTES) if (Objects.equals(currentAttrib.NAME, name)) attrib = currentAttrib;

        if (attrib == null) throw new NoSuchAttributeException("[MESH ERROR] : An attribute with the specified name could not be found in this mesh!");

        return attrib.SIZE;
    }
    public int getVertexSize() {
        return _vertexSize;
    }

    public float[] getData() {
        return _data;
    }
    public int[] getIndices() {
        return _indices;
    }

    public Collection<String> getAttribNames() {
        HashSet<String> names;

        names = new HashSet<>();

        _ATTRIBUTES.forEach((attrib) -> names.add(attrib.NAME));

        return names;
    }

    public Vertex getVertex(int index) {
        if (index >= _data.length / _vertexSize) return null;

        return new Vertex(index * _vertexSize, _data, _ATTRIBUTES, _vertexSize);
    }
    public Triangle getTriangle(int index) {
        if (index >= _indices.length / 3) throw new IndexOutOfBoundsException("[MESH ERROR] : This mesh does not contain a triangle at this index!");

        return new Triangle(_indices, index);
    }

    public String getFormatted() {
        StringBuilder builder;

        builder = new StringBuilder("\n<------- MESH DATA ------->\n");
        for (int counter = 0; counter < _data.length / _vertexSize; counter++) builder.append(getVertex(counter).getFormatted());

        return builder.toString();
    }

    protected Runnable p_onAddedToRenderObject(Runnable onChangedCallback) {
        if (onChangedCallback == null) throw new RuntimeException();

        _ON_CHANGED_CALLBACKS.add(onChangedCallback);

        return () -> _ON_CHANGED_CALLBACKS.remove(onChangedCallback);
    }

    private void h_notifyChange() {
        for (Runnable callback : _ON_CHANGED_CALLBACKS) callback.run();
    }

}