package modele;

import java.util.List;

public class Solution {
    private List<String> itineraire;
    private double distance;

    public Solution(List<String> itineraire, double distance) {
        this.itineraire = itineraire;
        this.distance = distance;
    }

    public List<String> getItineraire() {
        return itineraire;
    }

    public double getDistance() {
        return distance;
    }

    public void setItineraire(List<String> itineraire) {
        this.itineraire = itineraire;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    @Override
    public String toString() {
        return "Distance: " + String.format("%.2f", distance) + " km, Itinéraire: " + itineraire;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Solution solution = (Solution) obj;
        return Double.compare(solution.distance, distance) == 0 &&
                itineraire.equals(solution.itineraire);
    }
}