public interface Time_Snapshottable {
    // "Take a photo of yourself"
    Time_ActorMemento captureState();
    
    // "Restore yourself from this old photo"
    void restoreState(Time_ActorMemento memento);
}