package Models.LawnMower;

import Models.Zombies.Zombie;

public class LawnMower {
    private int row;
    private boolean available;
    private int killedZombiesCount;

    public LawnMower(int row) {
        this.row = row;
        this.available = true;
        this.killedZombiesCount = 0;
    }

    public void onZombieContact(Zombie zombie) {
        if (available) {
            System.out.println("The lawn mower in the row " + row + " is triggered and killed these zombies:");
            available = false;
        } else {
            System.out.println("The zombie ate your brain; LOSER!!!");
        }
    }


    public int getRow() {
        return row;
    }

    public boolean isAvailable() {
        return available;
    }

    public int getKilledZombiesCount() {
        return killedZombiesCount;
    }
}
