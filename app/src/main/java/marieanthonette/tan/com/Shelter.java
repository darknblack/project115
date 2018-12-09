package marieanthonette.tan.com;

public class Shelter {
    String name, address, link, days, capacity, userID;

    public Shelter() {

    }

    public Shelter(String name, String address, String link, String days, String capacity, String userID) {
        this.name = name;
        this.address = address;
        this.link = link;
        this.days = days;
        this.capacity = capacity;
        this.userID = userID;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getLink() { return link; }

    public String getDays() { return days; }

    public String getCapacity() { return capacity; }

    public String getUserID() { return userID; }
}
