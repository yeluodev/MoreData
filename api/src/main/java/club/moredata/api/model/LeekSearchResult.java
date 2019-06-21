package club.moredata.api.model;

import club.moredata.common.entity.SearchResult;

import java.util.List;

/**
 * @author yeluodev1226
 */
public class LeekSearchResult<T> extends LeekResult<T> {

    private static final long serialVersionUID = 5746327772564335589L;
    private List<SearchResult<T>> searchResult;

    public List<SearchResult<T>> getSearchResult() {
        return searchResult;
    }

    public void setSearchResult(List<SearchResult<T>> searchResult) {
        this.searchResult = searchResult;
    }
}
