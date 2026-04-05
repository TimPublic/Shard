package dev.timkloepper.render.objects.frame_buffer;

import dev.timkloepper.render.viewport.render_pipe.I_Image;

import java.util.HashMap;

public interface I_FrameBuffer {

    void bind();
    void unbind();

    void clear();

    void attach(String name, I_Image image);

    I_Image getAttachment(String name);
    HashMap<String, I_Image> getAttachments();

}