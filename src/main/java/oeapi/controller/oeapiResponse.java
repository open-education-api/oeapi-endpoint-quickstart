package oeapi.controller;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.List;
import oeapi.model.Ext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

/**
 *
 * @author itziar.urrutia
 */
@JsonPropertyOrder({"pageSize", "pageNumber", "hasPreviousPage", "hasNextPage", "totalPages", "items"})
public class oeapiResponse<T> {

    
    Logger logger = LoggerFactory.getLogger(oeapiResponse.class);

    
    private List<T> items;
    private int pageSize;
    private int pageNumber;

    //private Boolean hasPreviousPage;
    //private Boolean hasNextPage;
    private int totalPages;
    private Ext ext;

    public oeapiResponse(Page<T> page, List<T> items) {
        this.items = items;
        this.pageNumber = page.getNumber()+1;
        this.pageSize = page.getSize();
        this.totalPages = page.getTotalPages();
        
        try { 
          logger.debug("oeapiResponse(Page<T> page) pageNumber: "+this.pageNumber+", pageSize: "+this.pageSize+", totalPages: "+this.totalPages+", Items size: "+this.items.size());
        }  catch (Exception e)
             { logger.error("Error while debugging oeapiResponse attributes. "+e.getLocalizedMessage()); }        

    }

    public oeapiResponse(Page<T> page) {

        this.items = page.getContent();
        this.pageNumber = page.getNumber()+1;
        this.pageSize = page.getSize();
        this.totalPages = page.getTotalPages();
        
        try { 
          logger.debug("oeapiResponse(Page<T> page) pageNumber: "+this.pageNumber+", pageSize: "+this.pageSize+", totalPages: "+this.totalPages+", Items size: "+this.items.size());
        }  catch (Exception e)
             { logger.error("Error while debugging oeapiResponse attributes. "+e.getLocalizedMessage()); }

    }

    public oeapiResponse(List<T> items, Pageable pageable) {

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), items.size());

        Page<T> page;
        page = new PageImpl<>(items.subList(start, end), pageable, items.size());
        this.items = page.getContent();
        this.pageNumber = page.getNumber()+1;
        this.pageSize = page.getSize();
        this.totalPages = page.getTotalPages();
        
        try { 
          logger.debug("oeapiResponse(List<T> items, Pageable pageable) pageNumber: "+this.pageNumber+", pageSize: "+this.pageSize+", totalPages: "+this.totalPages+", Items size: "+this.items.size());
        }  catch (Exception e)
             { logger.error("Error while debugging oeapiResponse attributes. "+e.getLocalizedMessage()); }

    }

    /**
     * @return the items
     */
    public List<T> getItems() {
        return items;
    }

    /**
     * @param items the items to set
     */
    public void setItems(List<T> items) {
        this.items = items;
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
     * @return the hasPreviousPage
     */
    public Boolean getHasPreviousPage() {
        return this.getPageNumber() > 1;
    }

    /**
     * @return the hasNextPage
     */
    public Boolean getHasNextPage() {
        return this.getPageNumber() < this.getTotalPages();
    }

    /**
     * @return the totalPages
     */
    public int getTotalPages() {
        return this.totalPages;
    }

    /**
     * @param totalPages the totalPages to set
     */
    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    /**
     * @return the ext
     */
    public Ext getExt() {
        return ext;
    }

    /**
     * @param ext the ext to set
     */
    public void setExt(Ext ext) {
        this.ext = ext;
    }
}
