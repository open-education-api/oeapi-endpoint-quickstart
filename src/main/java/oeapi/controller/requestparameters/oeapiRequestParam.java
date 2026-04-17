package oeapi.controller.requestparameters;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import oeapi.controller.oeapiController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 *
 * @author itziar.urrutia
 */
public class oeapiRequestParam {

    static Logger logger = LoggerFactory.getLogger(oeapiController.class);
    
    private Integer pageSize = 100;    // Unless said otherwise, 100 items per page
    private Integer pageNumber = 0;    // In SpringBoot using pageable first page is 0
    
    private String consumer;
    private String q;
    private final String[] sortDefault = {"name"};
    // private String[] sort = {"name"};
    private String[] sort;
    
    private String primaryCode;

    public oeapiRequestParam() {
    }    
    
    /**
     * @return the pageSize
     */
    public int getPageSize() {
        return pageSize;
    }

    /**
     * @param pageSize the pageSize to set
     */
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    /**
     * @return the pageNumber
     */
    public int getPageNumber() {
        return pageNumber;
    }

    /**
     * @param pageNumber the pageNumber to set
     */
    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
        
    }    

    /**
     * @return the consumer
     */
    public String getConsumer() {
        return consumer;
    }

    /**
     * @param consumer the consumer to set
     */
    public void setConsumer(String consumer) {
        this.consumer = consumer;
    }
    
    /**
     * @return the q
     */
    public String getQ() {
        return q;
    }

    /**
     * @param q the q to set
     */
    public void setQ(String q) {
        this.q = q;
    }

    /**
     * @return the sort
     */
    public String[] getSort() {
        return sort;
    }

    /**
     * @param sort the sort to set
     */
    public void setSort(String[] sort) {
        this.sort = sort;
    }
    
    /**
     * @return the primaryCode
     */
    public String getPrimaryCode() {
        return primaryCode;
    }

    /**
     * @param primaryCode the primaryCode to set
     */
    public void setPrimaryCode(String primaryCode) {
        this.primaryCode = primaryCode;
    }

    

    public Map.Entry<String, String> getFilter() {
        if (primaryCode != null) {
            return new AbstractMap.SimpleEntry<>("primaryCode", primaryCode);
        } else {
            return null;
        }

    }
    
    public Pageable toPageable() {
        this.pageNumber = Objects.requireNonNullElse(pageNumber, 1) < 1 ? 0 : Objects.requireNonNullElse(pageNumber, 1)-1;
        this.pageSize   = Objects.requireNonNullElse(pageSize, 100) ;
        
        logger.debug("Using toPageable() with (page,size): ("+this.pageNumber+","+this.pageSize+")"); 

        return PageRequest.of(this.pageNumber, this.pageSize);
    }


    public Pageable toPageable(List<String> validSortAttributes) {

        this.pageNumber = Objects.requireNonNullElse(pageNumber, 1) < 1 ? 0 : Objects.requireNonNullElse(pageNumber, 1)-1;
        this.pageSize   = Objects.requireNonNullElse(pageSize, 100);
        
        logger.debug("Using toPageable(List<String> validSortAttributes) with (page,size): ("+this.pageNumber+","+this.pageSize+")");    
        
        if (this.sort != null)  {
            
            List<Sort.Order> orders = new ArrayList<>();

            for (String sortItem : sort) {
                boolean descending = sortItem.startsWith("-");
                String attribute = descending ? sortItem.substring(1) : sortItem;

                // Validate the sort attribute against the valid list from the entity-specific request class
                if (validSortAttributes != null && !validSortAttributes.contains(attribute)) {
                    throw new IllegalArgumentException("Invalid sort attribute: " + attribute);
                }

                // Add the order to the list, with ascending or descending direction
                orders.add(new Sort.Order(
                        descending ? Sort.Direction.DESC : Sort.Direction.ASC,
                        attribute
                ));
            }

            if (orders.isEmpty()) {
                orders.add(Sort.Order.asc("name")); // Default sorting by name if no valid orders
            }
                        
            return PageRequest.of(this.pageNumber, this.pageSize, Sort.by(orders));
        }
        else 
         {  return PageRequest.of(this.pageNumber, this.pageSize, Sort.unsorted()); }

    }

    public Pageable toPageable(List<String> validSortAttributes, String[] sortDefault) {
        if (Arrays.equals(sort, this.sortDefault)) {
            sort = sortDefault;
        }
        sort = sortDefault;
        return toPageable(validSortAttributes);
        //toPageable(sortDefault);
    }
   

}
