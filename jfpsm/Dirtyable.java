package jfpsm;

/**
 * interface for objects that are saved, allows to know
 * when they have some pending changes that could be saved
 * @author Julien Gouesse
 *
 */
public interface Dirtyable {
    public boolean isDirty();
    public void markDirty();
    public void unmarkDirty();
}
