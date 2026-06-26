package Models;

import java.util.ArrayList;
import java.util.List;

public class AppContext {
    List<String> allUsernames;
    private static AppContext instance;

    private AppContext(){

    }

    public static AppContext getInstance(){
        if (instance==null){
            instance=new AppContext();
            instance.loadSavedData();
        }
        return instance;
    }

    private void loadSavedData(){
        allUsernames=new ArrayList<>();
    }

    public List<String> getAllUsernames(){
        return allUsernames;
    }
}
