package io.github.tederiksson.imagereminders.models;

import io.realm.RealmObject;

/**
 * Created by Ted Eriksson on 30/01/16.
 */
public class ImageItem extends RealmObject {
    private String text;
    private String imageFile;
    private boolean done;
    private long timestamp;

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getImageFile() {
        return imageFile;
    }

    public void setImageFile(String imageFile) {
        this.imageFile = imageFile;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }
}
