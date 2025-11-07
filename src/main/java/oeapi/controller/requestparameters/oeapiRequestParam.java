package oeapi.controller.requestparameters;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 *
 * @author itziar.urrutia
 */
public class oeapiRequestParam {

    public oeapiRequestParam() {

        //this.pageNumber = 0;
        //this.pageSize = 10;
    }

    private int pageSize = 100;
    private int pageNumber = 1;
    private String consumer;
    private String q;
    private final String[] sortDefault = {"name"};
    private String[] sort = {"name"};
    private String primaryCode;

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

    public Map.Entry<String, String> getFilter() {
        if (primaryCode != null) {
            return new AbstractMap.SimpleEntry<>("primaryCode", primaryCode);
        } else {
            return null;
        }

    }

    public Pageable toPageable(List<String> validSortAttributes) {

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

        return PageRequest.of(0, pageSize, Sort.by(orders));
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
