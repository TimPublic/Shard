package dev.codanor.render.pipe;

import dev.codanor.render.specs.buffers.I_Image;
import dev.codanor.render.viewport.BatchContainer;
import dev.codanor.render.viewport.Camera2D;
import dev.codanor.util.Indexer;

import java.util.*;

public class RenderPipe {

    public RenderPipe(String compositionShaderPath) {
        _compiled = false;

        _PASS_INDEXER = new Indexer();
        _PASSES = new HashMap<>();

        INPUT_PASS = new PassBuilder((_INPUT_PASS_INSTANCE = new InputPass(_PASS_INDEXER.get()))).done();
        COMPOSITION_PASS = new PassBuilder(new CompositionPass(_PASS_INDEXER.get(), compositionShaderPath)).done();

        _RENDER_ORDER = new ArrayList<>();
    }

    public class PassBuilder {

        public PassBuilder(A_RenderPass pass) {
            HashMap<String, I_Image> inputMap;

            inputMap = new HashMap<>();

            for (String inputName : pass.getInputs()) inputMap.put(inputName, null);

            _PASSES.put((_ID = pass.getId()), (_DATA = new PassData(
                    _ID,
                    pass,
                    new HashSet<>(),
                    inputMap,
                    new ArrayList<>()
            )));
        }

        private final int _ID;
        private final PassData _DATA;

        public PassBuilder reads(int from) {
            PassData passData;

            if (from == COMPOSITION_PASS) throw new IllegalArgumentException();

            passData = _PASSES.get(from);

            if (passData == null) throw new IllegalArgumentException();

            _DATA.readsFrom.add(passData.pass);
            passData.blackboard.add(_DATA.inputMap);

            return this;
        }

        public int done() {
            return _ID;
        }

    }

    public record RenderData(
            Camera2D camera,
            BatchContainer container,
            Collection<String> layers
    ) {}
    private record PassData(
            int id,
            A_RenderPass pass,
            HashSet<A_RenderPass> readsFrom,
            HashMap<String, I_Image> inputMap,
            ArrayList<HashMap<String, I_Image>> blackboard
    ) {}

    private boolean _compiled;

    private final Indexer _PASS_INDEXER;
    private final HashMap<Integer, PassData> _PASSES;

    public final int INPUT_PASS, COMPOSITION_PASS;
    private final InputPass _INPUT_PASS_INSTANCE;

    private final ArrayList<A_RenderPass> _RENDER_ORDER;

    public PassBuilder addPass(String shaderPath) {
        return new PassBuilder(
                new PostProcessPass(_PASS_INDEXER.get(), shaderPath)
        );
    }

    public void compositionReadsFrom(int from) {
        PassData passData;

        if (from == COMPOSITION_PASS) throw new IllegalArgumentException();

        passData = _PASSES.get(from);

        if (passData == null) throw new IllegalArgumentException();

        _PASSES.get(COMPOSITION_PASS).readsFrom.add(passData.pass);
        passData.blackboard.add(_PASSES.get(COMPOSITION_PASS).inputMap);
    }

    public void compile() {
        HashMap<A_RenderPass, HashSet<A_RenderPass>> dependencies;
        HashMap<A_RenderPass, ArrayList<HashMap<String, I_Image>>> globalBlackboard;

        if (_compiled) throw new IllegalStateException();

        dependencies = new HashMap<>();
        globalBlackboard = new HashMap<>();

        for (PassData data : _PASSES.values()) {
            dependencies.put(data.pass, data.readsFrom);
            globalBlackboard.put(data.pass, data.blackboard);
        }

        // --- //

        while (!dependencies.isEmpty()) {
            boolean found;
            Iterator<A_RenderPass> iterator;

            found = false;
            iterator = dependencies.keySet().iterator();

            while (iterator.hasNext()) {
                A_RenderPass pass;

                if (dependencies.get((pass = iterator.next())).isEmpty()) {
                    HashMap<String, I_Image> output;

                    output = pass.getImages();

                    iterator.remove();

                    for (HashMap<String, I_Image> passInputs : globalBlackboard.get(pass)) {
                        for (String inputName : passInputs.keySet()) {
                            I_Image image;

                            image = output.get(inputName);

                            if (image == null) continue;

                            if (passInputs.put(inputName, image) != null) throw new IllegalStateException();
                        }
                    }
                    for (HashSet<A_RenderPass> reads : dependencies.values()) reads.remove(pass);

                    _RENDER_ORDER.add(pass);

                    found = true;
                }
            }

            if (!found) throw new IllegalStateException();
        }

        for (PassData data : _PASSES.values()) if (data.inputMap.containsValue(null)) throw new IllegalStateException();

        _compiled = true;
    }

    public void render(RenderData data) {
        _INPUT_PASS_INSTANCE.prepare(data.camera, data.container, data.layers);

        for (A_RenderPass pass : _RENDER_ORDER) pass.render(_PASSES.get(pass.getId()).inputMap);
    }

}