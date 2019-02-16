package edu.aem.training.upshot.services;

/**
 * A simple service interface
 */
public interface TextFinderService {
    
    /**
     * @return the name of the underlying JCR repository implementation
     */

    String[] find(String[] findWhatList, String[] findWhereList, String queryApi);

}