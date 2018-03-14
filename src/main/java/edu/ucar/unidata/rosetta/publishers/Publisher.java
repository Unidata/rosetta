package edu.ucar.unidata.rosetta.publishers;

/**
 * Created by lesserwhirls on 2/14/14.
 */
public interface Publisher {
    // every publisher needs a publish method that returns true if successful, false if not
    public boolean publish();

}
