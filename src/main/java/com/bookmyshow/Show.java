import java.util.*;

public class Show {
    int showid;
    String showname;
    int showprice;
    ArrayList<Integer> seats = new ArrayList<>(100);

    Show(int showid, String showname,int showprice) {
        this.showid = showid;
        this.showname = showname;
        this.showprice = showprice;
        for (int i = 1; i <= 100; i++) {
            seats.add(-1);
        }
    }
}
