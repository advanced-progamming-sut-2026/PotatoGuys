package pvz.Models.User;

public enum Gender {
    MALE,
    FEMALE;

    public static Gender getGender(String gender){
        switch (gender.toLowerCase()){
            case "male":
                return MALE;
            case "female":
                return FEMALE;
            default:
                return null;
        }
    }
}
