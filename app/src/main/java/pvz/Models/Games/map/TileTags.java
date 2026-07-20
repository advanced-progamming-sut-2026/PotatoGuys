package pvz.Models.Games.map;

public enum TileTags {
    NORMAL("Normal"),
    GRAVE("Grave"),
    ICE("Ice"),
    SLIP_UP("Slip up"),
    SLIP_DOWN("Slip down"),
    WATER("Water"),
    LOW_TIDE("Low tide"),
    NECROMANCY_POTENTIAL("Necromancy");

    public final String typeName;

    TileTags(String typeName){
        this.typeName=typeName;
    }
}
