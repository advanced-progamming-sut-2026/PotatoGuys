package pvz.Models.Games.map;

public enum TileType {
    NORMAL("Normal"),
    GRAVE("Grave"),
    ICE("Ice"),
    SLIP_UP("Slip up"),
    SLIP_DOWN("Slip down"),
    WATER("Water"),
    LOW_TIDE("Low tide"),
    NECROMANCY_POTENTIAL("Necromancy");

    public final String typeName;

    TileType(String typeName){
        this.typeName=typeName;
    }
}
