package edu.ucar.unidata.rosetta.init.resources;

import edu.ucar.unidata.rosetta.domain.resources.RosettaResource;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * @author oxelson@ucar.edu
 *
 * A generic class to to populate a model object that extends RosettaResources
 * with resource data held in the resource XML files.
 *
 * @param <T> A class that extends RosettaResources
 */
public class ResourcePopulator<T extends RosettaResource> {

    private static final Logger logger = Logger.getLogger(ResourcePopulator.class);

    private T rosettaResource;

    /**
     * Returns the RosettaResource object.
     *
     * @return The RosettaResource object.
     */
    public RosettaResource getRosettaResource() {
        return (RosettaResource) this.rosettaResource;
    }

    /**
     * Sets the RosettaResource object.
     *
     * @param rosettaResource The RosettaResource object.
     */
    public void setRosettaResource(T rosettaResource) {
        this.rosettaResource = rosettaResource;
    }

    /**
     * Populates model objects that extend RosettaResource with the supplied data gleaned
     * from the resource XML file(s). Using reflection, the proper setter method of the
     * model objects are invoked with the relevant XML data attribute.
     *
     * @param resourceMap   The data gleaned from the resource XML files.
     * @return  A list of populated RosettaResource objects.
     * @throws IllegalAccessException   If unable to invoke the setter method on the RosettaResource object.
     * @throws NoSuchMethodException    If the RosettaResource object doesn't contain the required setter method.
     * @throws InvocationTargetException  If unable to locate the target method to invoke.
     */
    public List<RosettaResource> populate(Map<String, List<String>> resourceMap) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException{
        // Get the resource object to populate.
        RosettaResource rosettaResource = getRosettaResource();

        List<RosettaResource> resources = new ArrayList<>();

        // Map holds resource child element data.
        Iterator<Map.Entry<String, List<String>>> iter = resourceMap.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, List<String>> keyValuePair = iter.next();
            String k = keyValuePair.getKey();
            // The setter method.
            String setterMethodName = "set" + k.substring(0, 1).toUpperCase() + k.substring(1);

            // Get the setter method.
            Method setter = (Method) rosettaResource.getClass().getMethod(setterMethodName, String.class);
            // The value(s) to assign to the setter method.
            List<String> valueList = keyValuePair.getValue();

            // Invoke the setter depending on how many items are in the list.
            if (valueList.size() > 1) {
                for (Object v : valueList) {
                    String value = (String) v;
                    setter.invoke(rosettaResource, value); // Use setter to add value to RosettaResource object.
                }
            } else {
                setter.invoke(rosettaResource, (String) valueList.get(0)); // Use setter to add value.
            }
            iter.remove(); // Avoids a ConcurrentModificationException
        }
        resources.add(rosettaResource);
        return resources;
    }
}
