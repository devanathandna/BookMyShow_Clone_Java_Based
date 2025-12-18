import java.util.List;

public class Theatre {
    int theatreid;
    String location;
    String theatrename;
    int totalseats;
    int availableseats;
    List<Show> shows;
    int tax;
    double ticketprice;

    Theatre(int theatreid,String location,String theatrename,int totalseats,int tax){
        this.theatreid=theatreid;
        this.location=location;
        this.theatrename=theatrename;
        this.totalseats=totalseats;
        this.availableseats=totalseats;
        this.tax = tax;
    }

    void SetTicketPrice(){
        this.ticketprice = this.shows.get(0).showprice * tax/10;
    }

    void Showdetails(){
        for(Show show:this.shows){
            System.out.println("Show ID: "+show.showid+" Show Name: "+show.showname+" Show Price: "+show.showprice);
        }
    }

    

    
}
